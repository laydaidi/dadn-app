package com.example.dadn_app.helpers;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MQTTHelper {
    // First mqtt server
    final String serverUri = "tcp://io.adafruit.com:1883";
    final String clientId = UUID.randomUUID().toString();
    final String baseTopic = "CSE_BBC/feeds/";
    final String subscriptionTopic = "";
    final String username = "CSE_BBC";
    final String password = "aio_YWqQ75LLnzE66cGrbMWNhCka1Xhb";

    // Second mqtt server
    final String serverUri1 = "tcp://io.adafruit.com:1883";
    final String clientId1 = UUID.randomUUID().toString();
    final String baseTopic1 = "NPNLab_BBC_phake/feeds/";
    final String subscriptionTopic1 = baseTopic1 + "bk-iot-esp32-cam";
    final String username1 = "NPNLab_BBC_phake";
    final String password1 = "aio_WiPw41RLrnYJv18ohgVpCbnuZcN9"; // 70a-Yz49Ne72

    MqttAndroidClient mqttAndroidClient;
    MqttAndroidClient mqttAndroidClient1;
    ESP32Helper esp32Helper = ESP32Helper.getHelper();
    private static final MutableLiveData<Boolean> isConnected = new MutableLiveData<Boolean>(false);
    public static MQTTHelper mqttHelper;

    private MQTTHelper(Context context) {
        isConnected.postValue(false);
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.d("mqtt", s);
                JSONObject data = new JSONObject();
                notifyBuzzer("100");
            }

            @Override
            public void connectionLost(Throwable throwable) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.d("Mqtt", topic + "--" + mqttMessage.toString());
                processData(topic, mqttMessage);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });

        mqttAndroidClient1 = new MqttAndroidClient(context, serverUri1, clientId1);
        mqttAndroidClient1.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.d("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.d("Mqtt", topic + "--" + mqttMessage.toString());
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
        Log.d("mqtt", "Create MQTT connection");
        connect(mqttAndroidClient, username, password, subscriptionTopic);
        connect(mqttAndroidClient1, username1, password1, subscriptionTopic1);
    }

    private synchronized void connect(MqttAndroidClient client, String username, String password, String subscriptionTopic) {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());
        try {
            client.connect(mqttConnectOptions, null, new
                    IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.d("mqtt", client.getServerURI() + " - Success");
                            DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                            disconnectedBufferOptions.setBufferEnabled(true);
                            disconnectedBufferOptions.setBufferSize(100);
                            disconnectedBufferOptions.setPersistBuffer(false);
                            disconnectedBufferOptions.setDeleteOldestMessages(false);
                            client.setBufferOpts(disconnectedBufferOptions);
                            subscribeToTopic(client, subscriptionTopic);
                            isConnected.postValue(true);
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable
                                exception) {
                            Log.d("Mqtt", "Failed to connect to: " + client.getServerURI() + "\n" +
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
            mqttAndroidClient1.disconnect();
            mqttHelper = null;
            isConnected.postValue(false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static LiveData<Boolean> isConnected() {
        return isConnected;
    }

    private void processData(String topic, MqttMessage mqttMessage) {
        JSONObject data;
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
            mqttHelper.publishData(baseTopic + "bk-iot-speaker", buzzerData);
            if (!value.equals("0")) {
                new Timer().schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    // Turn off buzzer after 1 second
                                    buzzerData.put("data", "0");
                                    mqttHelper.publishData(baseTopic + "bk-iot-speaker", buzzerData);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        1000
                );
            }
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
            mqttHelper.publishData(mqttAndroidClient1, baseTopic1 + "bk-iot-esp32-cam", ESP32Data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void publishData(String topic, JSONObject data) {
        publishData(mqttAndroidClient, topic, data);
    }

    public void publishData(MqttAndroidClient client, String topic, JSONObject data) {
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(true);
        byte[] b = data.toString().getBytes(StandardCharsets.UTF_8);
        msg.setPayload(b);
        try {
            client.publish(topic, msg);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribeToTopic(String topic) {
        subscribeToTopic(mqttAndroidClient, topic);
    }

    public void subscribeToTopic(MqttAndroidClient client, String topic) {
        try {
            client.subscribe(topic, 0, null, new
                    IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.d("Mqtt", "Subscribed! - " + client.getServerURI());
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable
                                exception) {
                            Log.d("Mqtt", "Subscribed fail! - " + client.getServerURI());
                        }
                    });
        } catch (MqttException ex) {
            Log.d("Mqtt", "Exceptions in subscribing");
            ex.printStackTrace();
        }
    }
}
