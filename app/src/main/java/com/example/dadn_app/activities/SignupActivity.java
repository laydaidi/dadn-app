package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

public class SignupActivity extends AppCompatActivity {

    private EditText editUsername, editEmail, editPhone, editPassword, editRepassword;
    private Button btnSignup;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        txtStatus = (TextView) findViewById(R.id.signupStatusText);

        editUsername = (EditText) findViewById(R.id.editSignupUsername);
        editEmail = (EditText) findViewById(R.id.editSignupEmail);
        editPhone = (EditText) findViewById(R.id.editSignupPhone);
        editPassword = (EditText) findViewById(R.id.editSignupPassword);
        editRepassword = (EditText) findViewById(R.id.editSignupRepassword);

        btnSignup = (Button) findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(v -> onClickSignup());
    }

    public void onClickForgetPassword(View view) {
        Intent i = new Intent(SignupActivity.this, ForgotPasswordActivity.class);
        startActivity(i);
        finish();
    }

    public void onClickLogin(View view) {
        Intent i = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    private void onClickSignup() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Helper.buildAPIURL("/users/signup");

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("username", editUsername.getText().toString());
        requestObject.put("email", editEmail.getText().toString());
        requestObject.put("phone", editPhone.getText().toString());
        requestObject.put("pass", editPassword.getText().toString());
        requestObject.put("repass", editRepassword.getText().toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(requestObject),
            response -> {
                Helper.hideStatus(txtStatus);
                Intent i = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(i);
            },
            error -> {
                if (error == null || error.networkResponse == null) {
                    Helper.showStatus(txtStatus, "Cant connect to the server");
                    return;
                }
                if (error.networkResponse.statusCode == 406 || error.networkResponse.statusCode == 403) {
                    try {
                        JSONObject res = new JSONObject(new String(error.networkResponse.data));
                        String message = res.get("message").toString();
                        Helper.showStatus(txtStatus, message);
                    } catch (JSONException e) {
                        Helper.showStatus(txtStatus, "Response is invalid");
                    }
                    return;
                }
                Helper.showStatus(txtStatus, "Cant connect to the server");
            }
        );
        queue.add(request);
    }
}