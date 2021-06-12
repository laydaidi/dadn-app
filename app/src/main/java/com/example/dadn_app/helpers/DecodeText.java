package com.example.dadn_app.helpers;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class DecodeText {
    private WordHelper wordHelper;
    private Context context;
    private List<HandPatternBuffer> listBuffer;
    private MappingPattern mappingPattern;

    public DecodeText(Context context) {
        this.context = context;
        this.listBuffer = new ArrayList<HandPatternBuffer>();
        this.mappingPattern = new MappingPattern();

        wordHelper = new WordHelper(context);
        wordHelper.loadWordDescriptor("word_description.csv");
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

        return "";
    }
}
