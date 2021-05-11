package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dadn_app.R;
import com.example.dadn_app.helpers.Helper;

import org.json.JSONException;
import org.json.JSONObject;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void onClickBtnStart(View view) {

        SharedPreferences prefs = getSharedPreferences("com.example.dadn_app", Context.MODE_PRIVATE);
        RequestQueue queue = Volley.newRequestQueue(this);

        Helper.resetToken(
            queue,
            prefs,
            () -> this.switchToTextSpeech(),
            () -> this.switchToLogin()
        );
    }

    private void switchToLogin() {
        Intent i = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(i);
    }

    private void switchToTextSpeech() {
        Intent i = new Intent(WelcomeActivity.this, TextSpeechActivity.class);
        startActivity(i);
    }
}