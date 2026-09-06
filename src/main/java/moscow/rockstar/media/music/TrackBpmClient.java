/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package moscow.rockstar.media.music;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import moscow.rockstar.media.lyrics.LyricsMatchService;
import moscow.rockstar.network.http.HttpResourceClient;

public class TrackBpmClient {
    private static final String DEEZER_API_BASE_URL = "https://api.deezer.com";

    public static float findTrackBpm(String string, String string2) {
        return TrackBpmClient.findTrackBpm(string, string2, 0L);
    }

    public static float findTrackBpm(String string, String string2, long l) {
        String string3;
        if (string == null || string2 == null || string.isBlank() || string2.isBlank()) {
            return 0.0f;
        }
        long l2 = TrackBpmClient.searchTrackId("artist:\"" + string + "\" track:\"" + string2 + "\"", string, string2, l);
        if (l2 == 0L) {
            l2 = TrackBpmClient.searchTrackId(string + " " + string2, string, string2, l);
        }
        if (l2 == 0L && !(string3 = LyricsMatchService.extractPrimaryTitle(string)).isBlank() && !string3.equalsIgnoreCase(string.trim())) {
            l2 = TrackBpmClient.searchTrackId(string3 + " " + string2, string, string2, l);
        }
        if (l2 == 0L) {
            return 0.0f;
        }
        return TrackBpmClient.fetchTrackBpm(l2);
    }

    private static long searchTrackId(String string, String string2, String string3, long l) {
        try {
            String string4 = TrackBpmClient.fetchJson("https://api.deezer.com/search?limit=10&q=" + TrackBpmClient.encodeQuery(string));
            if (string4 == null) {
                return 0L;
            }
            JsonArray jsonArray = JsonParser.parseString((String)string4).getAsJsonObject().getAsJsonArray("data");
            if (jsonArray == null || jsonArray.isEmpty()) {
                return 0L;
            }
            long l2 = 0L;
            double d = Double.NEGATIVE_INFINITY;
            for (JsonElement jsonElement : jsonArray) {
                double d2;
                JsonObject jsonObject;
                if (!jsonElement.isJsonObject() || !(jsonObject = jsonElement.getAsJsonObject()).has("id") || jsonObject.get("id").isJsonNull() || !((d2 = LyricsMatchService.scoreLyricsMatch(TrackBpmClient.extractTrackMetadata(jsonObject), string2, string3, l)) > d)) continue;
                d = d2;
                l2 = jsonObject.get("id").getAsLong();
            }
            return l2;
        }
        catch (Exception exception) {
            return 0L;
        }
    }

    private static JsonObject extractTrackMetadata(JsonObject jsonObject) {
        JsonObject jsonObject2 = jsonObject.has("artist") && jsonObject.get("artist").isJsonObject() ? jsonObject.getAsJsonObject("artist") : null;
        double d = jsonObject.has("duration") && !jsonObject.get("duration").isJsonNull() ? jsonObject.get("duration").getAsDouble() : 0.0;
        return LyricsMatchService.createLyricsMetadata(LyricsMatchService.getJsonString(jsonObject, "title"), LyricsMatchService.getJsonString(jsonObject2, "name"), d);
    }

    private static float fetchTrackBpm(long l) {
        try {
            String string = TrackBpmClient.fetchJson("https://api.deezer.com/track/" + l);
            if (string == null) {
                return 0.0f;
            }
            JsonObject jsonObject = JsonParser.parseString((String)string).getAsJsonObject();
            if (!jsonObject.has("bpm") || jsonObject.get("bpm").isJsonNull()) {
                return 0.0f;
            }
            float f = jsonObject.get("bpm").getAsFloat();
            return f >= 40.0f && f <= 250.0f ? f : 0.0f;
        }
        catch (Exception exception) {
            return 0.0f;
        }
    }

    private static String fetchJson(String string) {
        try {
            HttpResourceClient.HttpResponse httpResponse = HttpResourceClient.get(string, new String[0]);
            return httpResponse.isSuccess() ? httpResponse.getBody() : null;
        }
        catch (Exception exception) {
            return null;
        }
    }

    private static String encodeQuery(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }
}

