package org.emoncms.myapps;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.StringTokenizer;

public class MQTTController {

    private static final String TAG = MQTTController.class.getName();

    private static String MAC_ADDRESS;
    private static String TOPIC_POWER_RATING;
    private static String TOPIC_INDIVIDUAL_STATE;
    private static String TOPIC_INDIVIDUAL_LOAD;
    private static String TOPIC_TOPUP_CREDIT;
    public static String TOPIC_TOKEN_KEY;
    public static String TOPIC_DEVICE_REGISTER;

    private static final String TOPIC_SWITCH_STATUS = "";
    private static final String DEFAULT_BROKER = "192.168.0.101";
    private static final String DEFAULT_PORT = "8883";
    private static String CLIENT_ID = "testClient123";

    private static CallBack pChartUpdateListener;
    private static CallBack deviceStateListener;
    private static CallBack topupCreditListener;

    private Context mContext;

    private static final String TOPIC_INVESTIGATION_MODE = "Kwh/" + MAC_ADDRESS +"/token";

    public MQTTController(Context context) {
        mContext = context;
        startEnergyUpdates();
    }

    private void startEnergyUpdates() {
        setupMqttClient();
        //If want to tell anything to server here?
    }

    private void setupMqttClient() {
        Log.d(TAG, "Lets connect ... ");
        WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        MAC_ADDRESS = wifiManager.getConnectionInfo().getMacAddress();
        configTopics();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(mContext);
                String broker = sharedPreferences.getString("emoncms_broker_url", DEFAULT_BROKER);
                String port = sharedPreferences.getString("emoncms_broker_port", DEFAULT_PORT);
                String client = CLIENT_ID;

                if(MQTTHelper.connect(broker, port, client)) {
                    Log.d(TAG, "connected!!");
                    Thread t = new Thread(subscriber);
                    t.start();
                } else {
                    Looper.prepare();
                    Log.d(TAG, "failed to connect!!");
                }
            }
        }).start();
    }

    private void configTopics() {
        TOPIC_TOKEN_KEY = "Kwh/" + MAC_ADDRESS + "/token";
        TOPIC_POWER_RATING = "Kwh/" + MAC_ADDRESS;
        TOPIC_INDIVIDUAL_STATE = "Kwh/" + MAC_ADDRESS + "/load";
        TOPIC_INDIVIDUAL_LOAD = "Kwh/" + MAC_ADDRESS + "/pchart";
        TOPIC_DEVICE_REGISTER = "Kwh/" + MAC_ADDRESS + "/apikey";
        TOPIC_TOPUP_CREDIT = "Kwh/" + MAC_ADDRESS + "/credit";
    }

    public void setInvestigationMode(boolean isChecked) {
        if(!MQTTHelper.isConnected()) {
           setupMqttClient();
        }
        if(MQTTHelper.isConnected()) {
            MQTTHelper.publish(TOPIC_INVESTIGATION_MODE, isChecked);
        } else {
            Looper.prepare();
            Toast.makeText(mContext, "Server unreachable!\n", Toast.LENGTH_SHORT);
        }
    }

    private void registerDevice(String key) {
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

    public void registerTopupCreditListener(CallBack callBack) {
        topupCreditListener = callBack;
    }

    public void unRegisterAllListeners() {
        pChartUpdateListener = null;
        deviceStateListener = null;
        topupCreditListener = null;
    }

    public static void MqttMessageHandler(String s, MqttMessage mqttMessage) {
        String [] data = parseMqttMessage(mqttMessage);
        if (s.equals(TOPIC_INDIVIDUAL_LOAD) && pChartUpdateListener != null) {
            pChartUpdateListener.updateMessage(data);
        } else if (s.equals(TOPIC_INDIVIDUAL_STATE) && deviceStateListener != null) {
            deviceStateListener.updateMessage(data);
        } else if (s.equals(TOPIC_TOPUP_CREDIT) && topupCreditListener != null) {
            topupCreditListener.updateMessage(data);
        };
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
            MQTTHelper.subscribe(TOPIC_TOPUP_CREDIT);
        }
    };
}