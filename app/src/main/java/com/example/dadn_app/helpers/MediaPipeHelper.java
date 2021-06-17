package com.example.dadn_app.helpers;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.formats.proto.ClassificationProto;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.glutil.EglManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaPipeHelper {

    private static final String DEBUG_TAG = "MediaPipe";
    private static final String BINARY_GRAPH_NAME = "hand_tracking_mobile_gpu.binarypb";
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "hand_landmarks";
    private static final String OUTPUT_CLASSIFICATION_STREAM_NAME = "handedness";
    private static final String INPUT_NUM_HANDS_SIDE_PACKET_NAME = "num_hands";
    private static final int NUM_HANDS = 2;

    private EglManager eglManager;
    private FrameProcessor processor;
    private BitmapConverter converter;
    private Context context;
    private ESP32Helper esp32Helper;
    private BmpProducer bitmapProducer;
    private HandPatternRecognitionHelper handPatternRecognitionHelper;
    private static MutableLiveData<HandPatternBuffer> listHandPattern;
    List<ClassificationProto.ClassificationList> multiHandClassification;
    List<LandmarkProto.NormalizedLandmarkList> multiHandLandmarks;

    static {
        System.loadLibrary("mediapipe_jni");
        System.loadLibrary("opencv_java3");
    }

    public MediaPipeHelper(Context context, ESP32Helper esp32Helper) {
        this.context = context;
        this.esp32Helper = esp32Helper;
        listHandPattern = new MutableLiveData<HandPatternBuffer>();

        AndroidAssetUtil.initializeNativeAssetManager(context);
        eglManager = new EglManager(null);
        processor = new FrameProcessor(
                context,
                eglManager.getNativeContext(),
                BINARY_GRAPH_NAME,
                INPUT_VIDEO_STREAM_NAME,
                null
        );

        AndroidPacketCreator packetCreator = processor.getPacketCreator();
        Map<String, Packet> inputSidePackets = new HashMap<>();
        inputSidePackets.put(INPUT_NUM_HANDS_SIDE_PACKET_NAME, packetCreator.createInt32(NUM_HANDS));
        processor.setInputSidePackets(inputSidePackets);

        processor.addPacketCallback(OUTPUT_CLASSIFICATION_STREAM_NAME, (packet_classification) -> {
            multiHandClassification = PacketGetter.getProtoVector(packet_classification, ClassificationProto.ClassificationList.parser());

        });

        processor.addPacketCallback(OUTPUT_LANDMARKS_STREAM_NAME, (packet_landmark) -> {
            multiHandLandmarks = PacketGetter.getProtoVector(packet_landmark, LandmarkProto.NormalizedLandmarkList.parser());

            for (ClassificationProto.ClassificationList classificationList: multiHandClassification) {
                String hasLabel = classificationList.getClassification(0).getLabel();
                float score = classificationList.getClassification(0).getScore();
                Log.v("CLASS-LABEL", String.valueOf(hasLabel));
                Log.v("CLASS-SCORE", String.valueOf(score));
            }

            Log.v(DEBUG_TAG, "[TS:" + packet_landmark.getTimestamp() + "] " + getMultiHandLandmarksDebugString(multiHandLandmarks, multiHandClassification));
        });
    }

    public void initialize() {
        handPatternRecognitionHelper = new HandPatternRecognitionHelper(this.context.getApplicationContext());

        // converter = new ExternalTextureConverter(eglManager.getContext(), 2);
        converter = new BitmapConverter(eglManager.getContext());
        converter.setConsumer(processor);

        // Bitmap bmp = BitmapFactory.decodeByteArray(frame, 0, frame.length);

        bitmapProducer = new BmpProducer(this.context.getApplicationContext(), this.esp32Helper);
        bitmapProducer.setCustomFrameAvailableListener(converter);

        // converter.setSurfaceTextureAndAttachToGLContext(surfaceTexture, surfaceTextureWidth, surfaceTextureHeight);
    }

    public void suspend() {
        bitmapProducer.removeListener();
        converter.removeConsumer(processor);
        handPatternRecognitionHelper.close();
    }

    private String getMultiHandLandmarksDebugString(List<LandmarkProto.NormalizedLandmarkList> multiHandLandmarks,
                                                    List<ClassificationProto.ClassificationList> multiHandClassification) {
        if (multiHandLandmarks.isEmpty()) {
            return "No hand landmarks";
        }
        List<Integer> listPatternIndex = new ArrayList<Integer>();
        String multiHandLandmarksStr = "Number of hands detected: " + multiHandLandmarks.size() + "\n";
        int handIndex = 0;
        for (LandmarkProto.NormalizedLandmarkList landmarks : multiHandLandmarks) {
            multiHandLandmarksStr += "\t#Hand landmarks for hand[" + handIndex + "]: " + landmarks.getLandmarkCount() + "\n";
            int landmarkIndex = 0;

            float[][][] distance_buffer = new float[1][21][21];
            int row_index = 0;
            for(LandmarkProto.NormalizedLandmark firstLandmark: landmarks.getLandmarkList()) {
                int column_index = 0;
                for (LandmarkProto.NormalizedLandmark secondLandmark: landmarks.getLandmarkList()) {
                    if (column_index >= row_index) {
                        distance_buffer[0][row_index][column_index] = (float) Math.sqrt(Math.pow(firstLandmark.getX() - secondLandmark.getX(), 2) +
                                                                                        Math.pow(firstLandmark.getY() - secondLandmark.getY(), 2) +
                                                                                        Math.pow(firstLandmark.getZ() - secondLandmark.getZ(), 2));
                        distance_buffer[0][column_index][row_index] = distance_buffer[0][row_index][column_index];
                    }
                    column_index++;
                }
                row_index++;
            }

            int index = handPatternRecognitionHelper.doInference(distance_buffer);
            listPatternIndex.add(index);
            Log.v("RESULT", String.valueOf(index));

            ++handIndex;
        }

        HandPatternBuffer newBuffer = new HandPatternBuffer(listPatternIndex, multiHandLandmarks, multiHandClassification);
        listHandPattern.postValue(newBuffer);

        return multiHandLandmarksStr;
    }

    public static MutableLiveData<HandPatternBuffer> retrievePatternBuffer() {
        return listHandPattern;
    }

}
