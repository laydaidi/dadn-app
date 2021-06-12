package com.example.dadn_app.helpers;

import com.google.mediapipe.formats.proto.ClassificationProto;
import com.google.mediapipe.formats.proto.LandmarkProto;

import java.util.List;

public class HandPatternBuffer {
    public List<Integer> patternIndex;
    public List<LandmarkProto.NormalizedLandmarkList> patternLandmarks;
    public List<ClassificationProto.ClassificationList> patternHandedness;

    public HandPatternBuffer(List<Integer> patternIndex,
            List<LandmarkProto.NormalizedLandmarkList> patternLandmarks,
            List<ClassificationProto.ClassificationList> patternHandedness) {
        this.patternIndex = patternIndex;
        this.patternLandmarks = patternLandmarks;
        this.patternHandedness = patternHandedness;
    }
}

