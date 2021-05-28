package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SearchView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dadn_app.R;
import com.example.dadn_app.helpers.Helper;
import com.example.dadn_app.helpers.Record;
import com.example.dadn_app.helpers.RecordAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.google.gson.Gson;

public class LibraryActivity extends AppCompatActivity {
    ListView recordListView;
    ArrayList<Record> recordList;
    RecordAdapter recordAdapter;

    SearchView editLibSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        // prepare data
//        recordList = new ArrayList<Record>(Arrays.asList(
//                new Record("Hello", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Good byte", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Run", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Eat", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Sleep", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Write", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Read", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("See", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Hear", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Thank you", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Hello", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Good byte", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Run", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Eat", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Sleep", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Write", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Read", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("See", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Hear", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24),
//                new Record("Thank you", R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24)
//        ));
//        recordList = new ArrayList<Record>();
        this.loadRecordList();


//        // bind view to variable
//        recordListView = (ListView) findViewById(R.id.libraryRecordListView);
//
//        recordAdapter = new RecordAdapter(this, R.layout.library_record, recordList);
//        recordListView.setAdapter(recordAdapter);

        editLibSearch = (SearchView) findViewById(R.id.editLibSearch);
        editLibSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String text = newText;
                recordAdapter.filter(text);
                return false;
            }
        });
    }

    public void onClickBtnBack(View view) {
        Intent i = new Intent(LibraryActivity.this, TextSpeechActivity.class);
        startActivity(i);
        finish();
    }

    private void loadRecordList() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Helper.buildAPIURL("/lessons/list");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        recordList = new ArrayList<Record>();
                        JSONArray jArray = (JSONArray)response.getJSONArray("lessons");

                        if (jArray != null) {
                            for (int i=0;i<jArray.length();i++){
                                JSONObject jsonobject = jArray.getJSONObject(i);
                                String name = jsonobject.getString("name");
                                recordList.add(new Record(name, R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24));
                            }
                        }

                        // bind view to variable
                        recordListView = (ListView) findViewById(R.id.libraryRecordListView);

                        recordAdapter = new RecordAdapter(this, R.layout.library_record, recordList);
                        recordListView.setAdapter(recordAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if (error == null || error.networkResponse == null) {
                        Helper.showToast(getApplicationContext(), "Cant connect to the server");
                        return;
                    }
                    int statusCode = error.networkResponse.statusCode;
                    try {
                        String message = (new JSONObject(new String(error.networkResponse.data))).getString("message");
                        if (statusCode == 403) {
                            if (message.equals("Invalid token")) {
                                // Reset token here
                                Helper.resetToken(
                                        queue,
                                        getSharedPreferences("com.example.dadn_app", Context.MODE_PRIVATE),
                                        this::loadRecordList,
                                        this::switchToLogin
                                );
                            } else {
                                Helper.showToast(getApplicationContext(), message);
                            }
                        } else if (statusCode == 401) {
                            Helper.showToast(getApplicationContext(), message);
                        } else {
                            Helper.showToast(getApplicationContext(), "Cant connect to the server");
                        }
                    } catch (JSONException e) {
                        this.switchToLogin();
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return Helper.buildAuthorizationHeader();
            }
        };
        queue.add(request);
    }

    private void switchToLogin() {
        Intent i = new Intent(LibraryActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}