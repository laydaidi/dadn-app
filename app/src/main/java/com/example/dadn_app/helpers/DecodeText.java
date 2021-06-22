package com.example.dadn_app.helpers;

import android.content.Context;
import android.util.Log;

import com.google.mediapipe.formats.proto.ClassificationProto;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DecodeText {
    private WordHelper wordHelper;
    private Context context;
    private List<HandPatternBuffer> listBuffer;
    private MappingPattern mappingPattern;
    private HashMap<String, String> labelMapping;
    private Long timerBegin;

    public DecodeText(Context context) {
        this.context = context;
        this.listBuffer = new ArrayList<HandPatternBuffer>();
        this.mappingPattern = new MappingPattern();


        wordHelper = new WordHelper(this.context);
        wordHelper.loadWordDescriptor("word_description.csv");
        labelMapping = new HashMap<>();
        this.loadLabelMapping("label_mapping.csv");
    }

    public String decode(HandPatternBuffer handBuffer) {
        listBuffer.add(handBuffer);
        // TODO: decode here
        // Every time received hand buffer, add it into buffer list
        // After that, check buffer list, and update mappingPattern
        // Use mapping pattern to find word
        // If match, return word and clear buffer
        // If no word match, continue to store current buffer, waiting for the next
        // If more than 1000ms and no word match, clear buffer
        // Exception: If the recent buffer's pattern combined with old buffer's patterns do not match any pattern list, delete old buffer's patterns.


        // update mapping pattern
        for (Integer index: handBuffer.patternIndex) {
            String fullPattern = labelMapping.get(String.valueOf(handBuffer.patternIndex.get(index)));
            String hand = fullPattern.substring(0, fullPattern.indexOf("_"));
            String pattern = fullPattern.substring(fullPattern.indexOf("_")+1);
            Log.v("HAND", hand);
            Log.v("PATTERN", pattern);

            if (hand.equals("Right")) {
                mappingPattern.rightPatterns.add(pattern);
            } else if (hand.equals("Left")) {
                mappingPattern.leftPatterns.add(pattern);
            }
        }

        // check valid pattern list
        // valid if mappingPattern.leftPatterns is subarray of any LeftPattern list in WordHelper
        // and mapping.rightPatterns is subarray of any RightPattern list in WordHelper
        // invalid otherwise
        if(!wordHelper.checkValidLeftPattern(mappingPattern)) {
            mappingPattern.leftPatterns.remove(0);
        }
        if(!wordHelper.checkValidRightPattern(mappingPattern)) {
            mappingPattern.rightPatterns.remove(0);
        }

        // find word
        String word = wordHelper.getWord(mappingPattern);


        if(word != null && !word.equals("")) { // match
            listBuffer.clear();
            timerBegin = null;
            mappingPattern = new MappingPattern();
        }

        // check timeout
        if(timerBegin == null) {
            timerBegin = System.currentTimeMillis();
        } else {
            Long timeElapsed = System.currentTimeMillis() - timerBegin;
            if (timeElapsed > 1000) { // timeout
                listBuffer.clear();
                timerBegin = null;
                mappingPattern = new MappingPattern();
            }
        }

        return word;
    }

    private void loadLabelMapping(String fileName) {
        try {
            InputStreamReader is = new InputStreamReader(context.getAssets().open(fileName));
            CSVReader csvReader = new CSVReader(is);
            String[] values = null;
            boolean isHeader = true;
            while ((values = csvReader.readNext()) != null) {
                labelMapping.put(values[0], values[1].substring(values[1].indexOf("_")+1));
            }

            for(int i=0; i<9; i++) {
                Log.v("LABELMAPPING", labelMapping.get(String.valueOf(i)));
            }

        } catch (CsvValidationException e) {
            Log.d("LOAD_CSV_ERROR", e.toString());
        } catch (IOException e) {
            Log.d("LOAD_CSV_ERROR", e.toString());
        }
    }
}
