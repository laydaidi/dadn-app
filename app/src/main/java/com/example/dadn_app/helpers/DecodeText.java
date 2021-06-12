package com.example.dadn_app.helpers;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class DecodeText {
    private WordHelper wordHelper;
    private Context context;
    private List<HandPatternBuffer> listBuffer;

    public DecodeText(Context context) {
        this.context = context;
        this.listBuffer = new ArrayList<HandPatternBuffer>();
        wordHelper = new WordHelper(context);
        wordHelper.loadWordDescriptor("word_description.csv");
    }

    public String decode(HandPatternBuffer handBuffer) {
        listBuffer.add(handBuffer);
        // TODO: decode here

        return "";
    }
}
