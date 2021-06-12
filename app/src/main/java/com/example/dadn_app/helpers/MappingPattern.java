package com.example.dadn_app.helpers;

import java.util.ArrayList;
import java.util.List;

public class MappingPattern {
    public List<String> leftPatterns;
    public List<String> rightPatterns;
    public String action;
    public String position;
    public String direction;

    public MappingPattern() {
        clear();
    }

    public void clear() {
        leftPatterns = new ArrayList<String>();
        rightPatterns = new ArrayList<String>();
        action = "";
        position = "";
        direction = "";
    }
}
