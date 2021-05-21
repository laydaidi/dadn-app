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
    final String clientId = UUID.randomUUID().toString();
    final String serverUri = "tcp://industrial.api.ubidots.com:1883";
    final String baseTopic = "/v1.6/devices/bkiot/";
    final String username = "BBFF-aQZpnM5D7nUbgsbG3IBQOWiWAEILZB";
    final String password = "";

    // Adafruit server
//    final String serverUri = "tcp://io.adafruit.com:1883";
//    final String baseTopic = "binh234/feeds/multidisciplinary.";
//    final String username = "binh234";
//    final String password = "aio_JQLU03df6FP2wyxsGYcIUibSpmA6";

    final String subscriptionTopic = baseTopic + "bk-iotesp/lv";
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
                    data.put("value", 100);
                    data.put("unit", "");
                    mqttHelper.publishData(baseTopic + "bk-iotspeaker", data);
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
            int value = Math.round(Float.parseFloat(mqttMessage.toString()));
            if (topic.equals(subscriptionTopic)) {
                if (value == 0) {
                    // Stop stream
                    esp32Helper.disconnect();
                    // Notify buzzer
                    notifyBuzzer(500);
                } else {
                    // Connect and start streaming
                    esp32Helper.connect();
                    // Notify buzzer
                    notifyBuzzer(1000);
                }
            }
//            data = new JSONObject(mqttMessage.toString());
//            // Receive data from ESP32 CAM
//            if (topic.equals(subscriptionTopic)) {
//                // Cam closed
//                if (data.getInt("value") == 0) {
//                    // Stop stream
//                    esp32Helper.disconnect();
//                    // Notify buzzer
//                    notifyBuzzer(500);
//                } else if (data.getInt("value") == 1) { // Cam opened
//                    // Connect and start streaming
//                    esp32Helper.connect();
//                    // Notify buzzer
//                    notifyBuzzer(1000);
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notifyBuzzer(int value) {
        JSONObject buzzerData = new JSONObject();
        try {
            buzzerData.put("id", "2");
            buzzerData.put("name", "SPEAKER");
            buzzerData.put("value", value);
            buzzerData.put("unit", "");
            mqttHelper.publishData(baseTopic + "bk-iotspeaker", buzzerData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void notifyESP32(String value) {
        JSONObject ESP32Data = new JSONObject();
        try {
            ESP32Data.put("id", "99");
            ESP32Data.put("name", "ESP32");
            ESP32Data.put("value", value);
            ESP32Data.put("unit", "");
            mqttHelper.publishData(baseTopic + "bk-iotesp", ESP32Data);
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
