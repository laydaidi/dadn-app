package com.example.dadn_app.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.dadn_app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecordAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private List<Record> recordList;
    private List<Record> recordSearchList;

    public RecordAdapter(Context context, int layout, List<Record> recordList) {
        this.context = context;
        this.layout = layout;
        this.recordList = recordList;
        this.recordSearchList = new ArrayList<Record>();
        this.recordSearchList.addAll(this.recordList);
    }

    @Override
    public int getCount() {
        return recordSearchList.size();
    }

    @Override
    public Object getItem(int position) {
        return recordSearchList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(layout, null); // bind xml layout to view variable

        // bind views to variables
        TextView txt = (TextView) convertView.findViewById(R.id.libraryRecordText);
        ImageButton audioImageBtn = (ImageButton) convertView.findViewById(R.id.libraryRecordAudioImage);
        ImageButton videoImageBtn = (ImageButton) convertView.findViewById(R.id.libraryRecordVideoImage);

        // assign values
        Record record = recordSearchList.get(position);
        txt.setText(record.getTxt());
        audioImageBtn.setImageResource(record.getAudioImage());
        videoImageBtn.setImageResource(record.getVideoImage());

        setClickListener(videoImageBtn, position, parent);
        setClickListener(audioImageBtn, position, parent);

        return convertView;
    }

    private void setClickListener(View view, final int position, final ViewGroup parent){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // this part is important, it lets ListView handle the clicks
                ((ListView) parent).performItemClick(v, position, 0);
            }
        });
    }

    // filter records
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        recordSearchList.clear();
        if (charText.length() == 0) {
            recordSearchList.addAll(recordList);
        } else {
            for (Record r : recordList) {
                if (r.getTxt().toLowerCase(Locale.getDefault()).contains(charText)) {
                    recordSearchList.add(r);
                }
            }
        }
        notifyDataSetChanged();
    }
}
