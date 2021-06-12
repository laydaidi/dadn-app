package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dadn_app.R;
import com.example.dadn_app.helpers.BmpProducer;
import com.example.dadn_app.helpers.DecodeText;
import com.example.dadn_app.helpers.ESP32Helper;
import com.example.dadn_app.helpers.HandPatternBuffer;
import com.example.dadn_app.helpers.Helper;
import com.example.dadn_app.helpers.MQTTHelper;
import com.example.dadn_app.helpers.MediaPipeHelper;
import com.example.dadn_app.helpers.WordHelper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

public class TextSpeechActivity extends AppCompatActivity {

    TextToSpeech tts;
    Switch btnSwitch;
    TextView txt;
    MQTTHelper mqttHelper;
    ESP32Helper esp32Helper;
    MediaPipeHelper mediaPipeHelper;
    DecodeText textDecoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_speech);

        btnSwitch = (Switch) findViewById(R.id.btnSwitch);
        txt = (TextView) findViewById(R.id.textView2);
        txt.setText("Xin chào");

        tts = new TextToSpeech(TextSpeechActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(new Locale("vi"));
                    Helper.showToast(getApplicationContext(), "Text to Speech supported");
                } else {
                    Helper.showToast(getApplicationContext(), "Text to Speech not supported");
                }
            }
        });

        btnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tts.speak(txt.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
//                    Helper.showToast(getApplicationContext(), "Speak");
                }
            }
        });

        Context context = getApplicationContext();
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        String bssid = wifiInfo.getBSSID();
        int ip = wifiInfo.getIpAddress();
        try {
            @SuppressLint("DefaultLocale")
            String ipAddress = String.format("%d.%d.%d.%d", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
            Log.d("Wifi-SSID", ssid);
            Log.d("Wifi-BSSID", bssid);
            Log.d("Wifi-IP", ipAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Connect to MQTT server
        startMQTT();

        // Start translation
        esp32Helper = ESP32Helper.getHelper();
        mediaPipeHelper = new MediaPipeHelper(getApplicationContext(), esp32Helper);
        textDecoder = new DecodeText(getApplicationContext());
        startSign2Text();
    }

    public void onClickLibrary(View view) {
        Intent i = new Intent(TextSpeechActivity.this, LibraryActivity.class);
        startActivity(i);
        finish();
    }

    public void onClickBtnSetting(View view) {
        Intent i = new Intent(TextSpeechActivity.this, SettingsActivity.class);
        startActivity(i);
        finish();
    }

    private void startMQTT() {
        mqttHelper = MQTTHelper.getHelper(getApplicationContext());
        mqttHelper.connect();

        isServerConnected().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean success) {
                if (success) {
                    // Do something here
                }
            }
        });
    }

    private void startSign2Text() {
        ESP32Helper.isActive().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean active) {
                if (active) {
                    mediaPipeHelper.initialize();
                    startDecode();
                } else {
                    mediaPipeHelper.suspend();
                }
            }
        });
    }

    private void startDecode() {
        MediaPipeHelper.retrievePatternBuffer().observe(this, new Observer<HandPatternBuffer> () {
            @Override
            public void onChanged(HandPatternBuffer handPatternBuffer) {
                speakOut(textDecoder.decode(handPatternBuffer));
            }
        });
    }

    private void speakOut(String decodeString) {
        this.tts.speak(decodeString, TextToSpeech.QUEUE_FLUSH, null);
    }

    private LiveData<Boolean> isServerConnected() {
        return MQTTHelper.isConnected();
    }
}