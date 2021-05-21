package com.example.dadn_app.helpers;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;

import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.glutil.EglManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaPipeHelper {

    private static final String DEBUG_TAG = "MediaPipe";
    private static final String BINARY_GRAPH_NAME = "hand_tracking_mobile_gpu.binarypb";
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "hand_landmarks";
    private static final String INPUT_NUM_HANDS_SIDE_PACKET_NAME = "num_hands";
    private static final int NUM_HANDS = 2;

    private EglManager eglManager;
    private FrameProcessor processor;

    private ExternalTextureConverter converter;
    private SurfaceTexture frameTexture;


    static {
        System.loadLibrary("mediapipe_jni");
        System.loadLibrary("opencv_java3");
    }

    public void initialize(Context context, SurfaceTexture surfaceTexture, int surfaceTextureWidth, int surfaceTextureHeight) {
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

        processor.addPacketCallback(OUTPUT_LANDMARKS_STREAM_NAME, (packet) -> {
            Log.v("MediaPipe", "Received multi-hand landmarks packet.");
            List<LandmarkProto.NormalizedLandmarkList> multiHandLandmarks = PacketGetter.getProtoVector(packet, LandmarkProto.NormalizedLandmarkList.parser());
            Log.v(DEBUG_TAG, "[TS:" + packet.getTimestamp() + "] " + getMultiHandLandmarksDebugString(multiHandLandmarks));
        });

        frameTexture = surfaceTexture;

        converter = new ExternalTextureConverter(eglManager.getContext(), 2);
        converter.setConsumer(processor);
        converter.setSurfaceTextureAndAttachToGLContext(frameTexture, surfaceTextureWidth, surfaceTextureHeight);
    }

    private String getMultiHandLandmarksDebugString(List<LandmarkProto.NormalizedLandmarkList> multiHandLandmarks) {
        if (multiHandLandmarks.isEmpty()) {
            return "No hand landmarks";
        }
        String multiHandLandmarksStr = "Number of hands detected: " + multiHandLandmarks.size() + "\n";
        int handIndex = 0;
        for (LandmarkProto.NormalizedLandmarkList landmarks : multiHandLandmarks) {
            multiHandLandmarksStr += "\t#Hand landmarks for hand[" + handIndex + "]: " + landmarks.getLandmarkCount() + "\n";
            int landmarkIndex = 0;
            for (LandmarkProto.NormalizedLandmark landmark : landmarks.getLandmarkList()) {
                multiHandLandmarksStr += "\t\tLandmark [" + landmarkIndex + "]: (" + landmark.getX() + ", " + landmark.getY() + ", " + landmark.getZ() + ")\n";
                ++landmarkIndex;
            }
            ++handIndex;
        }
        return multiHandLandmarksStr;
    }

}
