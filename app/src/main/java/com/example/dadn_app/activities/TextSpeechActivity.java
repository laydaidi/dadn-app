package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.dadn_app.R;
import com.example.dadn_app.helpers.MQTTHelper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class TextSpeechActivity extends AppCompatActivity {

    TextToSpeech tts;
    Switch btnSwitch;
    MQTTHelper mqttHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_speech);

        btnSwitch = (Switch) findViewById(R.id.btnSwitch);

        tts = new TextToSpeech(TextSpeechActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.ENGLISH);
                    Toast.makeText(TextSpeechActivity.this, "Text to Speech supported", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TextSpeechActivity.this, "Text to Speech not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tts.speak("Hello World", TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(TextSpeechActivity.this, "speak", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Connect to MQTT server
        startMQTT();
    }

    public void onClickLibrary(View view) {
        Intent i = new Intent(TextSpeechActivity.this, LibraryActivity.class);
        startActivity(i);
    }

    public void onClickBtnSetting(View view) {
        Intent i = new Intent(TextSpeechActivity.this, SettingsActivity.class);
        startActivity(i);
    }

    private void startMQTT() {
        mqttHelper = new MQTTHelper(getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
                JSONObject data = new JSONObject();
                try {
                    JSONArray array = new JSONArray();
                    array.put(1);
                    array.put(1024);
                    data.put("id", 1234);
                    data.put("value", array);
                    mqttHelper.sendData("sensor/RP3", data);
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
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
        mqttHelper.connect();
    }
}