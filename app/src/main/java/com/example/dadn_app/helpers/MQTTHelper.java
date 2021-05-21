package com.example.dadn_app.helpers;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MQTTHelper {
    final String serverUri = "tcp://m14.cloudmqtt.com:17755";
    final String clientId = UUID.randomUUID().toString();
    final String subscriptionTopic = "NPNLab_BBC/feeds/+";
    final String username = "bvuiwhey";
    final String password = "70a-Yz49Ne72";
    MqttAndroidClient mqttAndroidClient;
    ESP32Helper esp32Helper = ESP32Helper.getHelper();

    public static MQTTHelper mqttHelper;

    private MQTTHelper(Context context) {
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
                JSONObject data = new JSONObject();
                try {
                    data.put("id", "3");
                    data.put("name", "SPEAKER");
                    data.put("data", "100");
                    data.put("unit", "");
                    mqttHelper.publishData("NPNLab_BBC/feeds/bk-iotspeaker", data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void connectionLost(Throwable throwable) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Mqtt", topic + "--" + mqttMessage.toString());
                processData(topic, mqttMessage);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
    }

    public static MQTTHelper getHelper(Context context) {
        if (mqttHelper == null) {
            mqttHelper = new MQTTHelper(context);
        }
        return mqttHelper;
    }

    public static MQTTHelper getHelper() {
        return mqttHelper;
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    public synchronized void connect() {
        if (isConnected()) {
            return;
        }
        Log.d("mqtt", "Create MQTT connection");
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new
                    IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.d("mqtt", "Success");
                            DisconnectedBufferOptions disconnectedBufferOptions = new
                                    DisconnectedBufferOptions();
                            disconnectedBufferOptions.setBufferEnabled(true);
                            disconnectedBufferOptions.setBufferSize(100);
                            disconnectedBufferOptions.setPersistBuffer(false);
                            disconnectedBufferOptions.setDeleteOldestMessages(false);
                            mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                            subscribeToTopic(subscriptionTopic);
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable
                                exception) {
                            Log.w("Mqtt", "Failed to connect to: " + serverUri + "\n" +
                                    exception.toString());
                        }
                    });
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void disconnect() {
        try {
            mqttAndroidClient.disconnect();
            mqttHelper = null;
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return mqttAndroidClient.isConnected();
    }

    private void processData(String topic, MqttMessage mqttMessage) {
        JSONObject data = null;
        try {
            data = new JSONObject(mqttMessage.toString());
            // Receive data from ESP32 CAM
            if (data.getString("id").equals("99")) {
                // Cam closed
                if (data.getString("data").equals("0")) {
                    // Stop stream
                    esp32Helper.disconnect();
                    // Notify buzzer
                    notifyBuzzer("500");
                } else if (data.getString("data").equals("1")) { // Cam opened
                    // Connect and start streaming
                    esp32Helper.connect();
                    // Notify buzzer
                    notifyBuzzer("1000");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void notifyBuzzer(String value) {
        JSONObject buzzerData = new JSONObject();
        try {
            buzzerData.put("id", "2");
            buzzerData.put("name", "SPEAKER");
            buzzerData.put("data", value);
            buzzerData.put("unit", "");
            mqttHelper.publishData("NPNLab_BBC/feeds/bk-iotspeaker", buzzerData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void notifyESP32(String value) {
        JSONObject ESP32Data = new JSONObject();
        try {
            ESP32Data.put("id", "99");
            ESP32Data.put("name", "ESP32");
            ESP32Data.put("data", value);
            ESP32Data.put("unit", "");
            mqttHelper.publishData("NPNLab_BBC/feeds/bk-iotesp", ESP32Data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void publishData(String topic, JSONObject data) {
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(true);
        byte[] b = data.toString().getBytes(StandardCharsets.UTF_8);
        msg.setPayload(b);
        try {
            mqttAndroidClient.publish(topic, msg);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribeToTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new
                    IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.w("Mqtt", "Subscribed!");
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable
                                exception) {
                            Log.w("Mqtt", "Subscribed fail!");
                        }
                    });
        } catch (MqttException ex) {
            Log.w("Mqtt", "Exceptions in subscribing");
            ex.printStackTrace();
        }
    }
}
