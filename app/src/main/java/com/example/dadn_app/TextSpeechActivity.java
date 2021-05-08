package com.example.dadn_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class TextSpeechActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_speech);
    }

    public void txtLibraryOnClick(View view) {
        Intent i = new Intent(TextSpeechActivity.this, LibraryActivity.class);
        startActivity(i);
    }
}