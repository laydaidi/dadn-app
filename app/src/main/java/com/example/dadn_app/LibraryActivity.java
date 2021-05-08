package com.example.dadn_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LibraryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
    }

    public void btnBackOnClick(View view) {
        Intent i = new Intent(LibraryActivity.this, TextSpeechActivity.class);
        startActivity(i);
    }
}