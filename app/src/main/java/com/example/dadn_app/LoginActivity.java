package com.example.dadn_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void btnCancelOnClick(View view) {
        Intent i = new Intent(LoginActivity.this, WelcomeActivity.class);
        startActivity(i);
    }

    public void btnLoginOnClick(View view) {
        Intent i = new Intent(LoginActivity.this, TextSpeechActivity.class);
        startActivity(i);
    }
}