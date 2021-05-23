package com.example.dadn_app.helpers;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class ESP32Helper {
    public static ESP32Helper helper = null;
    private final String serverUri = "ws://192.168.4.1:8888";
    private WebSocket ws;
    private static final ImageBuffer imageBuffer = SharedImageBuffer.getImageBuffer();
    private static final MutableLiveData<Boolean> isActive = new MutableLiveData<Boolean>(false);

    private ESP32Helper() {
        createWebSocketClient();
    }

    public static ESP32Helper getHelper() {
        if (helper == null) {
            helper = new ESP32Helper();
        }
        return helper;
    }

    public static LiveData<Boolean> isActive() {
        return isActive;
    }

    private void createWebSocketClient() {
        Log.w("WebSocket", "Start connection");
        WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);
        try {
            // Connect to server
            ws = factory.createSocket(serverUri);

            // Register a listener to receive WebSocket events.
            ws.addListener(new WebSocketAdapter() {
                @Override
                public void onConnected(WebSocket webSocket, Map<String, List<String>> headers) {
                    Log.w("WebSocket", "Session is starting");
                    isActive.postValue(true);
                }

                @Override
                public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {
                    Log.w("WebSocket", "Error");
                    Log.w("WebSocket", cause.toString());
                }

                @Override
                public void onDisconnected(WebSocket websocket,
                                           WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                                           boolean closedByServer) throws Exception {
                    Log.w("WebSocket", "Close");
                    isActive.postValue(false);
                }

                @Override
                public void onTextMessage(WebSocket websocket, String message) throws Exception {
                    // Received a text message.
                    JSONObject data = new JSONObject(message);
                    String type = data.getString("type");

                    Log.w("WebSocket", type);
//                    Log.w("WebSocket", message);
                    if (type.equals("frame")) {
                        String frame = data.getString("frame");
                        if (Base64.isBase64(frame)) {
                            byte[] bytesImage = Base64.decodeBase64(frame);
                            imageBuffer.put(bytesImage, true);
                        } else {
                            imageBuffer.put(frame.getBytes(), true);
                        }
                    }
                }

                @Override
                public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                    // Received a binary message.
                    Log.w("WebSocket", "Binary");
                    JSONObject data = new JSONObject(new String(binary));
                    String type = data.getString("type");

                    Log.w("WebSocket", type);
//                    Log.w("WebSocket", message);
                    if (type.equals("frame")) {
                        String frame = data.getString("data");
                        if (Base64.isBase64(frame)) {
                            byte[] bytesImage = Base64.decodeBase64(frame);
                            imageBuffer.put(bytesImage, true);
                        } else {
                            imageBuffer.put(frame.getBytes(), true);
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] getImage() {
      return imageBuffer.get();
    };

    public boolean isConnected() {
        return ws.isOpen();
    }

    public void connect() {
        if (isConnected()) {
            return;
        }
        // Connect to the server and perform an opening handshake.
        ws.connectAsynchronously();
    }

    public void disconnect() {
        if (!isConnected()) {
            return;
        }
        ws.disconnect();
    }
}
