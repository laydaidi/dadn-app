package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.dadn_app.R;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
    }

    public void onClickForgetPassword(View view) {
        Intent i = new Intent(SignupActivity.this, FotgotPasswordActivity.class);
        startActivity(i);
    }

    public void onClickLogin(View view) {
        Intent i = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(i);
    }
}