package com.example.dadn_app.helpers;

import android.content.Context;
import android.util.Log;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.opencsv.validators.RowMustHaveSameNumberOfColumnsAsFirstRowValidator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordHelper {
    private HashMap<String, HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>>> wordDescriptor;
    Context context;

    public WordHelper(Context context) {
        this.context = context;
        wordDescriptor = new HashMap<>();
    }

    public void loadWordDescriptor(String fileName) {
        try {
            List<List<String>> descriptions = new ArrayList<>();
            InputStreamReader is = new InputStreamReader(context.getAssets().open(fileName));
            CSVReader csvReader = new CSVReader(is);
            String[] values = null;
            boolean isHeader = true;
            while ((values = csvReader.readNext()) != null) {
                if (isHeader) {
                    isHeader = false;
                } else{
                    descriptions.add(Arrays.asList(values));
                }
            }

            for(List<String> dt: descriptions) {
                this.addWord(dt.get(0), dt.get(1), dt.get(2), dt.get(3), dt.get(4), dt.get(5));
            }

            // test
            for(List<String> dt: descriptions) {
                String[] params = new String[] {dt.get(1),dt.get(2),dt.get(3),dt.get(4),dt.get(5)};
                for(String param: params) {
                    Log.d("PARAMS", param);
                }
                String word = this.getWord(params);
                Log.d("CSV_DATA", word);
            }



        } catch (CsvValidationException e) {
            Log.d("LOAD_CSV_ERROR", e.toString());
        } catch (IOException e) {
            Log.d("LOAD_CSV_ERROR", e.toString());
        }

    }

    private void addWord(String word, String leftPattern, String rightPattern, String action, String position, String direction) {
        HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>> temp4 = wordDescriptor.get(leftPattern);
        HashMap<String, HashMap<String, HashMap<String, String>>> temp3 = temp4 != null ? temp4.get(rightPattern) : null;
        HashMap<String, HashMap<String, String>> temp2 = temp3 != null ? temp3.get(action) : null;
        HashMap<String, String> temp1 = temp2 != null ? temp2.get(position) : null;

        if (temp1 == null) {
            temp1 = new HashMap<>();
            temp1.put(direction, word);
            if(temp2 == null) {
                temp2 = new HashMap<>();
                temp2.put(position, temp1);
                if(temp3 == null) {
                    temp3 = new HashMap<>();
                    temp3.put(action, temp2);
                    if(temp4 == null) {
                        temp4 = new HashMap<>();
                        temp4.put(rightPattern, temp3);
                        wordDescriptor.put(leftPattern, temp4);
                    } else {
                        temp4.put(rightPattern, temp3);
                    }
                } else {
                    temp3.put(action, temp2);
                }
            } else {
                temp2.put(position, temp1);
            }
        } else {
            temp1.put(direction, word);
        }


    }

    private String getWord(String[] descriptions) {
        Object value = wordDescriptor;
        Log.d("NULL_DESCRIPTOR", String.valueOf(value == null));
        for(String key: descriptions) {
            value = ((HashMap)value).get(key);
            if (value == null) {
                return "<UNDEFINE>";
            }
        }
        return (String)value;
    }
}