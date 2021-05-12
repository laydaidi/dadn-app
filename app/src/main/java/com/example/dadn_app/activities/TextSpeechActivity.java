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

import java.util.Locale;

public class TextSpeechActivity extends AppCompatActivity {

    TextToSpeech tts;
    Switch btnSwitch;

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
                if(isChecked) {
                    tts.speak("Hello World", TextToSpeech.QUEUE_FLUSH, null);
                    Toast.makeText(TextSpeechActivity.this, "speak", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void onClickLibrary(View view) {
        Intent i = new Intent(TextSpeechActivity.this, LibraryActivity.class);
        startActivity(i);
    }

    public void onClickBtnSetting(View view) {
        Intent i = new Intent(TextSpeechActivity.this, SettingsActivity.class);
        startActivity(i);
    }
}