package org.emoncms.myapps;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import android.content.Context;

public class MQTTHelper{

	private static final String TAG = MQTTHelper.class.getName();
	private static Context mContext;

	void MQTTClient(Context context) {
		mContext = context;
	}

	private static MqttClient sMqttClient;

	private static MqttClient getClient() {
		return sMqttClient;
	}

	public static boolean isConnected() {
        if(sMqttClient == null) return false;
        return sMqttClient.isConnected();
    }

	public static boolean connect(String url, String port, String client) {
		try {
			MemoryPersistence persistance = new MemoryPersistence();
            if(sMqttClient == null || !sMqttClient.isConnected()) {
			    sMqttClient = new MqttClient("tcp://" + url + ":" + port, client, persistance);
                sMqttClient.connect();
            }

			sMqttClient.setCallback(new MqttCallback() {

				@Override
				public void connectionLost(Throwable throwable) {
					throwable.printStackTrace();
				}

				@Override
				public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    MQTTController.MqttMessageHandler(s, mqttMessage);
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
					try {
						MqttMessage message = iMqttDeliveryToken.getMessage();
					} catch (MqttException e) {
						e.printStackTrace();
					}
				}
			});
			return true;
		} catch (MqttException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean publish(String topic, String payload) {
		if(sMqttClient == null) {
			return false;
		}
		MqttMessage message = new MqttMessage(payload.getBytes());
		try {
			sMqttClient.publish(topic, message);
			return true;
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
			return false;
		} catch (MqttException e) {
			e.printStackTrace();
			return false;
		}
	}

    public static boolean publish(String topic, Boolean state) {
        return publish(topic, state.toString());
    }

    public static boolean publish(String topic, int value) {
        return publish(topic, Integer.toString(value));
    }

	public static void subscribe(String topic) {
		try {
			sMqttClient.subscribe(topic);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	public void unsubscribe(String topic) {
		try {
			sMqttClient.unsubscribe(topic);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

    public void disconnect() {
        try {
            sMqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}