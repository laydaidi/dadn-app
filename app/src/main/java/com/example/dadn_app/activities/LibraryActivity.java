package com.example.dadn_app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.Locale;
import java.util.Map;


public class LibraryActivity extends AppCompatActivity {
    ListView recordListView;
    ArrayList<Record> recordList;
    RecordAdapter recordAdapter;

    SearchView editLibSearch;

    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        tts = new TextToSpeech(LibraryActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(new Locale("vi"));
//                    Helper.showToast(getApplicationContext(), "Text to Speech supported");
                } else {
//                    Helper.showToast(getApplicationContext(), "Text to Speech not supported");
                }
            }
        });

        this.loadRecordList();

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

//    public void onClickVideoImage(View view) {
//        Intent i = new Intent(LibraryActivity.this, LibraryVideoActivity.class);
//        i.putExtra("id", recordList.get(view.getId()).getId());
//        startActivity(i);
//        finish();
//    }

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
                                String id = jsonobject.getString("id");
                                Log.d("LESSON ID:", id);
                                recordList.add(new Record(name, R.drawable.ic_baseline_campaign_24, R.drawable.ic_video_24, id));
                            }
                        }

                        // bind view to variable
                        recordListView = (ListView) findViewById(R.id.libraryRecordListView);

                        recordAdapter = new RecordAdapter(this, R.layout.library_record, recordList);
                        recordListView.setAdapter(recordAdapter);

                        recordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                long viewId = view.getId();
                                Record record = (Record) recordAdapter.getItem(position);
                                if (viewId == R.id.libraryRecordVideoImage) {
                                    Intent i = new Intent(LibraryActivity.this, LibraryVideoActivity.class);
                                    i.putExtra("id", record.getId());
                                    startActivity(i);
                                    finish();
                                } else if (viewId == R.id.libraryRecordAudioImage) {
                                    tts.speak(record.getTxt(), TextToSpeech.QUEUE_FLUSH, null);
                                }
                            }
                        });


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