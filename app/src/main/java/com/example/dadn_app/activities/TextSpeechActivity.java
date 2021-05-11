package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.dadn_app.R;

public class TextSpeechActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_speech);
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