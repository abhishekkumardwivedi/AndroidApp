package org.emoncms.myapps;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.StringTokenizer;


public class MQTTController {

    private static final String TAG = MQTTController.class.getName();

    private static String MAC_ADDRESS = "123456";
    private static final String TOPIC_TOKEN_KEY = "Kwh/" + MAC_ADDRESS + "/token";
    private static final String TOPIC_POWER_RATING = "Kwh/" + MAC_ADDRESS;
    private static final String TOPIC_INDIVIDUAL_STATE = "Kwh/" + MAC_ADDRESS + "/load";
    private static final String TOPIC_INDIVIDUAL_LOAD = "Kwh/" + MAC_ADDRESS + "/pchart";
    public static final String TOPIC_DEVICE_REGISTER = "Kwh/" + MAC_ADDRESS + "/apikey";

    private static final String TOPIC_SWITCH_STATUS = "";
    private static final String DEFAULT_BROKER = "192.168.0.101";
    private static final String DEFAULT_PORT = "8883";
    private static String CLIENT_ID = "testClient123";

    private static CallBack pChartUpdateListener;
    private static CallBack deviceStateListener;

    private Context mContext;

    private static final String TOPIC_INVESTIGATION_MODE = "Kwh/" + MAC_ADDRESS +"/token";

    public MQTTController(Context context) {
        mContext = context;
    }

    public void startEnergyUpdates() {
        setupMqttClient();
    }

    public void setupMqttClient() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(mContext);
                String broker = sharedPreferences.getString("emoncms_broker_url", DEFAULT_BROKER);
                String port = sharedPreferences.getString("emoncms_broker_port", DEFAULT_PORT);
                String client = CLIENT_ID;

                if(MQTTHelper.connect(broker, port, client)) {
                    Thread t = new Thread(subscriber);
                    t.start();
                } else {
                    Looper.prepare();
                }
            }
        }).start();
    }

    public void setInvestigationMode(boolean isChecked) {
        if(!MQTTHelper.isConnected()) {
           setupMqttClient();
        }
        if(MQTTHelper.isConnected()) {
            MQTTHelper.publish(TOPIC_INVESTIGATION_MODE, isChecked);
        } else {
            Toast.makeText(mContext, "Server unreachable!\n", Toast.LENGTH_SHORT);
        }
    }

    public void registerDevice(String key) {
        if(!MQTTHelper.isConnected()) {
            setupMqttClient();
        }
        if(MQTTHelper.isConnected()) {
            MQTTHelper.publish(TOPIC_DEVICE_REGISTER, key);
        } else {
            Toast.makeText(mContext, "Server unreachable!\n", Toast.LENGTH_SHORT);
        }
    }

    public static interface CallBack {
        void updateMessage(String [] msg);
    }

    public void registerPChartDataListener(CallBack callback) {
        pChartUpdateListener = callback;
    }

    public void registerDeviceStateListener(CallBack callback) {
        deviceStateListener = callback;
    }

    public void unRegisterAllListeners() {
        pChartUpdateListener = null;
        deviceStateListener = null;
    }

    public static void MqttMessageHandler(String s, MqttMessage mqttMessage) {

        String [] data = parseMqttMessage(mqttMessage);
        if (s.equals(TOPIC_INDIVIDUAL_LOAD) && pChartUpdateListener != null) {
            pChartUpdateListener.updateMessage(data);
        } else if (s.equals(TOPIC_INDIVIDUAL_STATE) && deviceStateListener != null) {
            deviceStateListener.updateMessage(data);
        }
    }

    private static String[] parseMqttMessage(MqttMessage mqttPayload) {
        StringTokenizer st = new StringTokenizer(mqttPayload.toString().replace(" ",""), "{//}//,");
        String[] data = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            data[i] = st.nextToken();
            i++;
        }
        return data;
    }

    static private Runnable subscriber  = new Runnable() {
        @Override
        public void run() {
            MQTTHelper.subscribe(TOPIC_INDIVIDUAL_LOAD);
            MQTTHelper.subscribe(TOPIC_INDIVIDUAL_STATE);
        }
    };
}