package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.dadn_app.R;

public class FotgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotgot_password);
    }

    public void onClickBtnBack(View view) {
        Intent i = new Intent(FotgotPasswordActivity.this, LoginActivity.class);
        startActivity(i);
    }
}