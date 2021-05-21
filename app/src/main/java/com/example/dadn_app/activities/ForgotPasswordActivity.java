package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dadn_app.R;
import com.example.dadn_app.helpers.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editUsername, editEmail, editPhone;
    private Button btnResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotgot_password);

        editUsername = (EditText) findViewById(R.id.editForgotUsername);
        editEmail = (EditText) findViewById(R.id.editForgotEmail);
        editPhone = (EditText) findViewById(R.id.editForgotPhone);

        btnResetPassword = (Button) findViewById(R.id.btnResetPassword);
        btnResetPassword.setOnClickListener(v -> onClickBtnResetPassword());
    }

    public void onClickBtnBack(View view) {
        Intent i = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    private void onClickBtnResetPassword() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Helper.buildAPIURL("/users/reset-password");

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("username", editUsername.getText().toString());
        requestObject.put("email", editEmail.getText().toString());
        requestObject.put("phone", editPhone.getText().toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(requestObject),
            response -> {
                Helper.showToast(getApplicationContext(), "Your password is reset to 123456");
            },
            error -> {
                if (error.networkResponse.statusCode == 401) {
                    try {
                        JSONObject res = new JSONObject(new String(error.networkResponse.data));
                        String message = res.get("message").toString();
                        Helper.showToast(getApplicationContext(), message);
                    } catch (JSONException e) {
                        Helper.showToast(getApplicationContext(), "Response is invalid");
                    }
                } else {
                    Helper.showToast(getApplicationContext(), "Cant connect to the server");
                }
            }
        );
        queue.add(request);
    }
}