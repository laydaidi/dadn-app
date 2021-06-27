package com.example.dadn_app.helpers;

import android.content.Context;
import android.util.Log;

import com.google.mediapipe.formats.proto.ClassificationProto;
import com.google.mediapipe.formats.proto.LandmarkProto;
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
    private HashMap<Integer, String> labelMapping;
    private HashMap<String, float[]> positionMapping;
    private Long timerBegin;

    public DecodeText(Context context) {
        this.context = context;
        this.listBuffer = new ArrayList<HandPatternBuffer>();
        this.mappingPattern = new MappingPattern();


        wordHelper = new WordHelper(this.context);
        wordHelper.loadWordDescriptor("word_description.csv");
        labelMapping = new HashMap<>();
        this.loadLabelMapping("label_mapping.csv");
        positionMapping = new HashMap<>();
        this.loadPositionMapping("position_mapping.csv");
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
        if(handBuffer.patternIndex.size() != handBuffer.patternHandedness.size()) {
            return "";
        }

        String coordinateDebug = "";
        mappingPattern.position = "[,]";
        mappingPattern.direction = "[,]";
        mappingPattern.action = "";

        for (int i=0; i < handBuffer.patternIndex.size(); i++) {  // i is hand index (left/right)
            // update left/right patterns
            String pattern = labelMapping.get(handBuffer.patternIndex.get(i));
//            Log.v("PATTERN", pattern);
            String hand = handBuffer.patternHandedness.get(i).getClassification(0).getLabel();
//            Log.v("HAND", hand);
            if (hand.equals("Right")) {
                mappingPattern.rightPatterns.add(pattern);
            } else if (hand.equals("Left")) {
                mappingPattern.leftPatterns.add(pattern);
            }

            // coordinate debug
            LandmarkProto.NormalizedLandmarkList landmarkList = handBuffer.patternLandmarks.get(i);
            coordinateDebug += hand + " landmarks:\n";
            int index = 0;
            for(LandmarkProto.NormalizedLandmark landmark: landmarkList.getLandmarkList()) {
                coordinateDebug += "\tLandmark " + "[" + index + "]: " + landmark.getX() + "\t" + landmark.getY() + "\t" + landmark.getZ() + "\n";
                index += 1;
            }

            // find hand bounding box
            float[] handBoundingBox = this.getHandBoundingBox(landmarkList);


            // update direction
            String direction = this.getDirection(landmarkList, handBoundingBox);
            if(!direction.equals("")) {
                if(hand.equals("Right")) {
                    mappingPattern.direction = new StringBuffer(mappingPattern.direction)
                            .insert(mappingPattern.direction.indexOf(",")+1,direction)
                            .toString();
                } else if(hand.equals("Left")) {
                   mappingPattern.direction = new StringBuffer(mappingPattern.direction)
                           .insert(mappingPattern.direction.indexOf("[")+1, direction)
                           .toString();
                }
            }

            // update position
            float[] palmBoundingBox = this.getPalmBoundingBox(landmarkList);
            mappingPattern.position = this.getPosition(landmarkList, palmBoundingBox);

            // update action
            mappingPattern.action = "";
        }
        Log.v("LANDMARKS", coordinateDebug);


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
                labelMapping.put(Integer.valueOf(values[0]), values[1]);
            }

//            for(int i=0; i<9; i++) {
//                Log.v("LABELMAPPING", labelMapping.get(i));
//            }

        } catch (CsvValidationException e) {
            Log.d("LOAD_CSV_ERROR", e.toString());
        } catch (IOException e) {
            Log.d("LOAD_CSV_ERROR", e.toString());
        }
    }

    private void loadPositionMapping(String fileName) {
        try {
            InputStreamReader is = new InputStreamReader(context.getAssets().open(fileName));
            CSVReader csvReader = new CSVReader(is);
            String[] values = null;
            boolean isHeader = true;
            while ((values = csvReader.readNext()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                positionMapping.put(String.valueOf(values[0]),
                        new float[] {
                                Float.valueOf(values[1]),
                                Float.valueOf(values[2]),
                                Float.valueOf(values[3]),
                                Float.valueOf(values[4])
                        });
            }

//            Log.v("POSITIONMAPPING",
//                    positionMapping.get("forehead")[0] + "," +
//                            positionMapping.get("forehead")[1] + "," +
//                            positionMapping.get("forehead")[2] + "," +
//                            positionMapping.get("forehead")[3]
//            );


        } catch (CsvValidationException e) {
            Log.d("LOAD_CSV_ERROR", e.toString());
        } catch (IOException e) {
            Log.d("LOAD_CSV_ERROR", e.toString());
        }
    }

    private float[] getHandBoundingBox(LandmarkProto.NormalizedLandmarkList landmarkList) {
        float HAND_TOP = 0.0f;
        float HAND_BOTTOM = 1.0f;
        float HAND_LEFT = 1.0f;
        float HAND_RIGHT = 0.0f;
        float HAND_NEAR = 1.0f;
        float HAND_FAR = -1.0f;
        for(LandmarkProto.NormalizedLandmark landmark: landmarkList.getLandmarkList()) {
            if (landmark.getX() < HAND_LEFT) {
                HAND_LEFT = landmark.getX();
            }
            if (landmark.getX() > HAND_RIGHT) {
                HAND_RIGHT = landmark.getX();
            }
            if (landmark.getY() > HAND_TOP) {
                HAND_TOP = landmark.getY();
            }
            if (landmark.getY() < HAND_BOTTOM) {
                HAND_BOTTOM = landmark.getY();
            }
            if (landmark.getZ() < HAND_NEAR) {
                HAND_NEAR = landmark.getZ();
            }
            if (landmark.getZ() > HAND_FAR) {
                HAND_FAR = landmark.getZ();
            }
        }

        float [] handBoundingBox = new float[] {HAND_LEFT, HAND_RIGHT, HAND_TOP, HAND_BOTTOM, HAND_NEAR, HAND_FAR};
        return handBoundingBox;

    }

    private float[] getPalmBoundingBox(LandmarkProto.NormalizedLandmarkList landmarkList) {
        float PALM_TOP = 0.0f;
        float PALM_BOTTOM = 1.0f;
        float PALM_LEFT = 1.0f;
        float PALM_RIGHT = 0.0f;
        float PALM_NEAR = 1.0f;
        float PALM_FAR = -1.0f;
        for(LandmarkProto.NormalizedLandmark landmark: landmarkList.getLandmarkList()) {
            if (landmark.equals(landmarkList.getLandmark(0)) ||
                    landmark.equals(landmarkList.getLandmark(5)) ||
                    landmark.equals(landmarkList.getLandmark(17))) {
                if (landmark.getX() < PALM_LEFT) {
                    PALM_LEFT = landmark.getX();
                }
                if (landmark.getX() > PALM_RIGHT) {
                    PALM_RIGHT = landmark.getX();
                }
                if (landmark.getY() > PALM_TOP) {
                    PALM_TOP = landmark.getY();
                }
                if (landmark.getY() < PALM_BOTTOM) {
                    PALM_BOTTOM = landmark.getY();
                }
                if (landmark.getZ() < PALM_NEAR) {
                    PALM_NEAR = landmark.getZ();
                }
                if (landmark.getZ() > PALM_FAR) {
                    PALM_FAR = landmark.getZ();
                }
            }
        }

        float [] palmBoundingBox = new float[] {PALM_LEFT, PALM_RIGHT, PALM_TOP, PALM_BOTTOM, PALM_NEAR, PALM_FAR};
        return palmBoundingBox;
    }

    private String getDirection(LandmarkProto.NormalizedLandmarkList landmarkList, float[] handBoundingBox) {
        // get direction based on distance between INDEX_FINGER_TIP (index = 8) and WRIST (index = 0)
        float HAND_LEFT = handBoundingBox[0];
        float HAND_RIGHT = handBoundingBox[1];
        float HAND_TOP = handBoundingBox[2];
        float HAND_BOTTOM = handBoundingBox[3];
        float HAND_NEAR = handBoundingBox[4];
        float HAND_FAR = handBoundingBox[5];

        float DIRECTION_THRESHOLD = 0.8f;
        String direction = "";
        float distance_ratio_x = (landmarkList.getLandmark(8).getX() - landmarkList.getLandmark(0).getX()) / (HAND_RIGHT - HAND_LEFT);
        float distance_ratio_y = (landmarkList.getLandmark(8).getY() - landmarkList.getLandmark(0).getY()) / (HAND_TOP - HAND_BOTTOM);
        float distance_ratio_z = (landmarkList.getLandmark(8).getZ() - landmarkList.getLandmark(0).getZ()) / (HAND_FAR - HAND_NEAR);
        if(distance_ratio_x > DIRECTION_THRESHOLD) {
            direction = "right";
        }
        if(distance_ratio_x < -DIRECTION_THRESHOLD) {
            direction = "left";
        }
        if(distance_ratio_y > DIRECTION_THRESHOLD) {
            direction = "up";
        }
        if(distance_ratio_y < -DIRECTION_THRESHOLD) {
            direction = "down";
        }
        if(distance_ratio_z > DIRECTION_THRESHOLD) {
            direction = "back";
        }
        if(distance_ratio_z < -DIRECTION_THRESHOLD) {
            direction = "front";
        }

        return direction;
    }

    private String getPosition(LandmarkProto.NormalizedLandmarkList landmarkList, float[] palmBoundingBox) {
        float HEAD_THRESHOLD = 0.5f;
        float MOUTH_THRESHOLD = 0.25f;
        float CHEST_THRESHOLD = 0.125f;

        float width = palmBoundingBox[1] - palmBoundingBox[0];
        float height = palmBoundingBox[2] - palmBoundingBox[3];
        float diagonal = (float)Math.sqrt(width*width + height*height);

        ArrayList<String> matchPositions = new ArrayList<>();
        for(String position: positionMapping.keySet()) {
            float[] coords = positionMapping.get(position);
            float TOP = coords[0];
            float BOTTOM = coords[1];
            float LEFT = coords[2];
            float RIGHT = coords[3];

            if(palmBoundingBox[0] >= LEFT &&
                    palmBoundingBox[1] <= RIGHT &&
                    palmBoundingBox[2] <= TOP &&
                    palmBoundingBox[3] >= BOTTOM) {
                boolean valid = false;
                if(position == "forehead") {
                    if(diagonal > HEAD_THRESHOLD) {
                        valid = true;
                    }
                } else if (position == "mouth") {
                    if(diagonal > MOUTH_THRESHOLD && diagonal <= HEAD_THRESHOLD) {
                        valid = true;
                    }
                } else if (position == "chest") {
                    if(diagonal <= MOUTH_THRESHOLD) {
                        valid = true;
                    }
                } else {
                    valid = true;
                }

                if(valid) {
                    matchPositions.add(position);
                }
            }
        }

        Log.v("NUM-MATCH-POSITION", String.valueOf(matchPositions.size()));

        for(String position: matchPositions) {
            Log.v("MATCH-POSITION", position);
        }

        return "";
    }
}
