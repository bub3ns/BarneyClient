/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  okhttp3.OkHttpClient
 *  okhttp3.OkHttpClient$Builder
 *  okhttp3.Request
 *  okhttp3.Request$Builder
 *  okhttp3.Response
 *  okhttp3.WebSocket
 *  okhttp3.WebSocketListener
 *  okio.ByteString
 */
package moscow.rockstar.audio;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.ModuleContract;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class SpeechRecognitionClient {
    private static final String DEEPGRAM_STREAM_URL = "wss://api.deepgram.com/v1/listen?model=nova-2&language=ru&encoding=linear16&sample_rate=16000&interim_results=true&punctuate=true&smart_format=true&endpointing=1000";
    private final String apiKey;
    private final OkHttpClient httpClient;
    private WebSocket webSocket;
    private TargetDataLine microphone;
    private Thread captureThread;
    private volatile boolean capturing;
    private Consumer<String> transcriptConsumer = string -> {};
    private Consumer<String> finalTranscriptConsumer = string -> {};

    public SpeechRecognitionClient(String string2) {
        this.apiKey = Objects.requireNonNull(string2, "apiKey");
        this.httpClient = new OkHttpClient.Builder().readTimeout(0L, TimeUnit.MILLISECONDS).build();
    }

    public SpeechRecognitionClient withTranscriptConsumer(Consumer<String> consumer) {
        this.transcriptConsumer = consumer != null ? consumer : string -> {};
        return this;
    }

    public SpeechRecognitionClient withFinalTranscriptConsumer(Consumer<String> consumer) {
        this.finalTranscriptConsumer = consumer != null ? consumer : string -> {};
        return this;
    }

    public void startRecognition() throws Exception {
        if (this.capturing) {
            return;
        }
        this.capturing = true;
        StringBuilder stringBuilder = new StringBuilder(DEEPGRAM_STREAM_URL);
        for (ModuleContract moduleContract : RockstarClient.create().getModuleRegistry().getModules()) {
            stringBuilder.append("&keywords=" + moduleContract.getName() + ":5");
        }
        Request request = new Request.Builder().url(stringBuilder.toString()).addHeader("Authorization", "Token " + this.apiKey).build();
        this.webSocket = this.httpClient.newWebSocket(request, new WebSocketListener(){

            public void onOpen(WebSocket webSocket, Response response) {
                try {
                    SpeechRecognitionClient.this.startMicrophoneCapture(webSocket);
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                    SpeechRecognitionClient.this.stopRecognition();
                }
            }

            public void onMessage(WebSocket webSocket, String string) {
                SpeechRecognitionClient.this.handleTranscriptMessage(string);
            }

            public void onMessage(WebSocket webSocket, ByteString byteString) {
            }

            public void onFailure(WebSocket webSocket, Throwable throwable, Response response) {
                System.err.println("[Deepgram] WS failure: " + String.valueOf(throwable));
                SpeechRecognitionClient.this.stopRecognition();
            }

            public void onClosing(WebSocket webSocket, int n, String string) {
                webSocket.close(1000, null);
            }

            public void onClosed(WebSocket webSocket, int n, String string) {
            }
        });
    }

    public void stopRecognition() {
        this.capturing = false;
        try {
            if (this.webSocket != null) {
                this.webSocket.send("{\"type\":\"CloseStream\"}");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            if (this.captureThread != null) {
                this.captureThread.join(500L);
                this.captureThread = null;
            }
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
        try {
            if (this.microphone != null) {
                this.microphone.stop();
                this.microphone.close();
                this.microphone = null;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            if (this.webSocket != null) {
                this.webSocket.close(1000, "bye");
                this.webSocket = null;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    void startMicrophoneCapture(WebSocket webSocket) throws Exception {
        AudioFormat audioFormat = new AudioFormat(16000.0f, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        this.microphone = (TargetDataLine)AudioSystem.getLine(info);
        this.microphone.open(audioFormat);
        this.microphone.start();
        this.captureThread = new Thread(() -> {
            byte[] byArray = new byte[3200];
            long l = System.currentTimeMillis();
            while (this.capturing && this.microphone.isOpen()) {
                int n = this.microphone.read(byArray, 0, byArray.length);
                if (n > 0) {
                    webSocket.send(ByteString.of((byte[])byArray, (int)0, (int)n));
                    l = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - l <= 4000L) continue;
                webSocket.send("{\"type\":\"KeepAlive\"}");
                l = System.currentTimeMillis();
            }
        }, "Deepgram-MicPump");
        this.captureThread.setDaemon(true);
        this.captureThread.start();
    }

    void handleTranscriptMessage(String string) {
        try {
            JsonObject jsonObject = JsonParser.parseString((String)string).getAsJsonObject();
            if (!jsonObject.has("type") || !"Results".equals(jsonObject.get("type").getAsString())) {
                return;
            }
            boolean bl = jsonObject.has("is_final") && jsonObject.get("is_final").getAsBoolean();
            JsonObject jsonObject2 = jsonObject.getAsJsonObject("channel");
            if (jsonObject2 == null) {
                return;
            }
            JsonArray jsonArray = jsonObject2.getAsJsonArray("alternatives");
            if (jsonArray == null || jsonArray.size() == 0) {
                return;
            }
            String string2 = jsonArray.get(0).getAsJsonObject().get("transcript").getAsString();
            if (string2 == null || string2.isBlank()) {
                return;
            }
            if (bl) {
                this.finalTranscriptConsumer.accept(string2);
            } else {
                this.transcriptConsumer.accept(string2);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

