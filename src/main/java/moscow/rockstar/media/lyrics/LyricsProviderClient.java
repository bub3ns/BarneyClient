/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package moscow.rockstar.media.lyrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.media.lyrics.LyricsMatchService;
import moscow.rockstar.media.lyrics.LyricsParser;
import moscow.rockstar.media.lyrics.LyricsTimeline;
import moscow.rockstar.network.http.HttpResourceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LyricsProviderClient {
    static final Logger LOGGER = LoggerFactory.getLogger((String)"rockstar-lyrics");
    private static final String LRCLIB_API_BASE_URL = "https://lrclib.net/api";
    private static final String NETEASE_API_BASE_URL = "https://music.163.com/api";
    private static final String LRCLIB_USER_AGENT = "Barney/2.0 (https://github.com/barney)";
    private static final String BROWSER_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";
    private static final long REQUEST_TIMEOUT_MILLIS = 8000L;
    private static final ProviderBackoff LRCLIB_BACKOFF = new ProviderBackoff("lrclib");
    private static final ProviderBackoff NETEASE_BACKOFF = new ProviderBackoff("netease");

    public static LyricsTimeline fetchLyrics(String string, String string2) {
        return LyricsProviderClient.fetchLyricsWithDeadline(string, string2, 0L);
    }

    public static LyricsTimeline fetchLyricsWithDeadline(String string, String string2, long l) {
        if (string == null || string2 == null || string.isBlank() || string2.isBlank()) {
            return LyricsTimeline.empty();
        }
        long l2 = System.currentTimeMillis();
        LOGGER.info("[lyrics] \u0438\u0449\u0443 \u00ab{} - {}\u00bb ({} \u0441)", new Object[]{string, string2, l});
        LyricsTimeline lyricsTimeline = LyricsProviderClient.fetchFromLrcLib(string, string2, l, LyricsProviderClient.getRequestDeadline());
        if (lyricsTimeline != null && !lyricsTimeline.isEmpty() && lyricsTimeline.hasTiming()) {
            return LyricsProviderClient.logProviderResult("lrclib", string, string2, lyricsTimeline, l2);
        }
        LyricsTimeline lyricsTimeline2 = LyricsProviderClient.fetchFromNetease(string, string2, l, LyricsProviderClient.getRequestDeadline());
        if (lyricsTimeline2 != null && !lyricsTimeline2.isEmpty() && lyricsTimeline2.hasTiming()) {
            return LyricsProviderClient.logProviderResult("netease", string, string2, lyricsTimeline2, l2);
        }
        if (lyricsTimeline != null && !lyricsTimeline.isEmpty()) {
            return LyricsProviderClient.logProviderResult("lrclib (\u0431\u0435\u0437 \u0442\u0430\u0439\u043c\u043a\u043e\u0434\u043e\u0432)", string, string2, lyricsTimeline, l2);
        }
        String string3 = LyricsParser.buildSearchUrl(string, string2);
        if (string3 != null && !string3.isBlank()) {
            return LyricsProviderClient.logProviderResult("genius (\u0431\u0435\u0437 \u0442\u0430\u0439\u043c\u043a\u043e\u0434\u043e\u0432)", string, string2, LyricsTimeline.parsePlainLyrics(string3), l2);
        }
        LOGGER.info("[lyrics] {} - {}: \u043d\u0435 \u043d\u0430\u0448\u043b\u043e\u0441\u044c \u043d\u0438 \u0432 \u043e\u0434\u043d\u043e\u043c \u0438\u0441\u0442\u043e\u0447\u043d\u0438\u043a\u0435 ({} \u043c\u0441)", new Object[]{string, string2, System.currentTimeMillis() - l2});
        return LyricsTimeline.empty();
    }

    private static LyricsTimeline logProviderResult(String string, String string2, String string3, LyricsTimeline lyricsTimeline, long l) {
        LOGGER.info("[lyrics] {} - {}: {} \u0441\u0442\u0440\u043e\u043a \u0438\u0437 {} ({} \u043c\u0441)", new Object[]{string2, string3, lyricsTimeline.getLines().size(), string, System.currentTimeMillis() - l});
        return lyricsTimeline;
    }

    private static void logProviderResponse(String string, String string2, int n, Exception exception) {
        if (exception != null) {
            LOGGER.warn("[lyrics] {} \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d ({}): {}", new Object[]{string, string2, exception.toString()});
        } else {
            LOGGER.warn("[lyrics] {} \u043e\u0442\u0432\u0435\u0442\u0438\u043b {} ({})", new Object[]{string, n, string2});
        }
    }

    private static long getRequestDeadline() {
        return System.currentTimeMillis() + 8000L;
    }

    private static boolean isDeadlineExpired(long l) {
        return System.currentTimeMillis() >= l;
    }

    private static LyricsTimeline fetchFromNetease(String string, String string2, long l, long l2) {
        if (!NETEASE_BACKOFF.isAvailable()) {
            return null;
        }
        JsonArray jsonArray = LyricsProviderClient.searchNeteaseSongs(string + " " + string2, l2);
        String string3 = LyricsProviderClient.normalizeArtistName(string);
        if (!(jsonArray != null && !jsonArray.isEmpty() || string3.isBlank() || string3.equalsIgnoreCase(string.trim()))) {
            jsonArray = LyricsProviderClient.searchNeteaseSongs(string3 + " " + string2, l2);
        }
        if (jsonArray == null) {
            return null;
        }
        long l3 = -1L;
        double d = Double.NEGATIVE_INFINITY;
        for (JsonElement jsonElement : jsonArray) {
            double d2;
            JsonObject jsonObject;
            if (!jsonElement.isJsonObject() || !(jsonObject = jsonElement.getAsJsonObject()).has("id") || jsonObject.get("id").isJsonNull() || !((d2 = LyricsProviderClient.scoreLyricsResult(LyricsProviderClient.normalizeSongResult(jsonObject), string, string2, l)) > d)) continue;
            d = d2;
            l3 = jsonObject.get("id").getAsLong();
        }
        return l3 < 0L ? null : LyricsProviderClient.fetchNeteaseLyrics(l3, l2);
    }

    private static JsonArray searchNeteaseSongs(String string, long l) {
        JsonObject jsonObject = LyricsProviderClient.requestJson("https://music.163.com/api/search/get?type=1&limit=10&s=" + LyricsProviderClient.urlEncode(string), l);
        if (jsonObject == null || !jsonObject.has("result") || !jsonObject.get("result").isJsonObject()) {
            return null;
        }
        JsonObject jsonObject2 = jsonObject.getAsJsonObject("result");
        return jsonObject2.has("songs") && jsonObject2.get("songs").isJsonArray() ? jsonObject2.getAsJsonArray("songs") : null;
    }

    private static LyricsTimeline fetchNeteaseLyrics(long l, long l2) {
        JsonObject jsonObject = LyricsProviderClient.requestJson("https://music.163.com/api/song/lyric?lv=1&kv=1&tv=-1&id=" + l, l2);
        if (jsonObject == null || !jsonObject.has("lrc") || !jsonObject.get("lrc").isJsonObject()) {
            return null;
        }
        String string = LyricsProviderClient.readJsonString(jsonObject.getAsJsonObject("lrc"), "lyric");
        if (string.isBlank()) {
            return null;
        }
        LyricsTimeline lyricsTimeline = LyricsTimeline.parseTimedLyrics(string);
        return lyricsTimeline.isEmpty() ? null : lyricsTimeline;
    }

    private static JsonObject normalizeSongResult(JsonObject jsonObject) {
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("trackName", LyricsProviderClient.readJsonString(jsonObject, "name"));
        StringBuilder stringBuilder = new StringBuilder();
        if (jsonObject.has("artists") && jsonObject.get("artists").isJsonArray()) {
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("artists")) {
                String string;
                if (!jsonElement.isJsonObject() || (string = LyricsProviderClient.readJsonString(jsonElement.getAsJsonObject(), "name")).isBlank()) continue;
                if (!stringBuilder.isEmpty()) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(string);
            }
        }
        jsonObject2.addProperty("artistName", stringBuilder.toString());
        if (jsonObject.has("duration") && !jsonObject.get("duration").isJsonNull()) {
            jsonObject2.addProperty("duration", (Number)((double)jsonObject.get("duration").getAsLong() / 1000.0));
        }
        return jsonObject2;
    }

    private static JsonObject requestJson(String string, long l) {
        if (LyricsProviderClient.isDeadlineExpired(l)) {
            return null;
        }
        try {
            HttpResourceClient.HttpResponse httpResponse = HttpResourceClient.get(string, "User-Agent", BROWSER_USER_AGENT, "Referer", "https://music.163.com");
            if (!httpResponse.isSuccess()) {
                LyricsProviderClient.logProviderResponse("netease", string, httpResponse.getStatus(), null);
                NETEASE_BACKOFF.recordFailure();
                return null;
            }
            NETEASE_BACKOFF.resetFailures();
            JsonElement jsonElement = JsonParser.parseString((String)httpResponse.getBody());
            return jsonElement.isJsonObject() ? jsonElement.getAsJsonObject() : null;
        }
        catch (Exception exception) {
            LyricsProviderClient.logProviderResponse("netease", string, 0, exception);
            NETEASE_BACKOFF.recordFailure();
            return null;
        }
    }

    private static LyricsTimeline fetchFromLrcLib(String string, String string2, long l, long l2) {
        if (!LRCLIB_BACKOFF.isAvailable()) {
            return null;
        }
        ArrayList<JsonArray> arrayList = new ArrayList<JsonArray>();
        if (!LyricsProviderClient.appendLrcLibSearchResults(arrayList, "track_name=" + LyricsProviderClient.urlEncode(string2) + "&artist_name=" + LyricsProviderClient.urlEncode(string), l2)) {
            return null;
        }
        JsonObject jsonObject = LyricsProviderClient.selectBestLyricsResult((JsonArray)arrayList.getFirst(), string, string2, l, true);
        if (jsonObject != null) {
            return LyricsProviderClient.parseLyricsResponse(jsonObject, true);
        }
        String string3 = LyricsProviderClient.normalizeArtistName(string);
        if (!string3.isBlank() && !string3.equalsIgnoreCase(string.trim())) {
            if (!LyricsProviderClient.appendLrcLibSearchResults(arrayList, "track_name=" + LyricsProviderClient.urlEncode(string2) + "&artist_name=" + LyricsProviderClient.urlEncode(string3), l2)) {
                return null;
            }
            jsonObject = LyricsProviderClient.selectBestLyricsResult((JsonArray)arrayList.getLast(), string, string2, l, true);
            if (jsonObject != null) {
                return LyricsProviderClient.parseLyricsResponse(jsonObject, true);
            }
        }
        if (!LyricsProviderClient.appendLrcLibSearchResults(arrayList, "track_name=" + LyricsProviderClient.urlEncode(string2), l2)) {
            return null;
        }
        jsonObject = LyricsProviderClient.selectBestLyricsResult((JsonArray)arrayList.getLast(), string, string2, l, true);
        if (jsonObject != null) {
            return LyricsProviderClient.parseLyricsResponse(jsonObject, true);
        }
        if (!LyricsProviderClient.appendLrcLibSearchResults(arrayList, "q=" + LyricsProviderClient.urlEncode((string3.isBlank() ? string : string3) + " " + string2), l2)) {
            return null;
        }
        jsonObject = LyricsProviderClient.selectBestLyricsResult((JsonArray)arrayList.getLast(), string, string2, l, true);
        if (jsonObject != null) {
            return LyricsProviderClient.parseLyricsResponse(jsonObject, true);
        }
        JsonObject jsonObject2 = null;
        double d = Double.NEGATIVE_INFINITY;
        for (JsonArray jsonArray : arrayList) {
            double d2;
            JsonObject jsonObject3 = LyricsProviderClient.selectBestLyricsResult(jsonArray, string, string2, l, false);
            if (jsonObject3 == null || !((d2 = LyricsProviderClient.scoreLyricsResult(jsonObject3, string, string2, l)) > d)) continue;
            d = d2;
            jsonObject2 = jsonObject3;
        }
        return jsonObject2 == null ? null : LyricsProviderClient.parseLyricsResponse(jsonObject2, false);
    }

    private static boolean appendLrcLibSearchResults(List<JsonArray> list, String string, long l) {
        JsonArray jsonArray = LyricsProviderClient.requestLrcLibSearch(string, l);
        if (jsonArray == null) {
            return false;
        }
        list.add(jsonArray);
        return true;
    }

    private static JsonArray requestLrcLibSearch(String string, long l) {
        if (LyricsProviderClient.isDeadlineExpired(l)) {
            return null;
        }
        try {
            HttpResourceClient.HttpResponse httpResponse = HttpResourceClient.get("https://lrclib.net/api/search?" + string, "User-Agent", LRCLIB_USER_AGENT);
            if (!httpResponse.isSuccess()) {
                LyricsProviderClient.logProviderResponse("lrclib", "https://lrclib.net/api/search?" + string, httpResponse.getStatus(), null);
                LRCLIB_BACKOFF.recordFailure();
                return null;
            }
            LRCLIB_BACKOFF.resetFailures();
            return JsonParser.parseString((String)httpResponse.getBody()).getAsJsonArray();
        }
        catch (Exception exception) {
            LyricsProviderClient.logProviderResponse("lrclib", "https://lrclib.net/api/search?" + string, 0, exception);
            LRCLIB_BACKOFF.recordFailure();
            return null;
        }
    }

    static JsonObject selectBestLyricsResult(JsonArray jsonArray, String string, String string2, long l, boolean bl) {
        JsonObject jsonObject = null;
        double d = Double.NEGATIVE_INFINITY;
        String string3 = bl ? "syncedLyrics" : "plainLyrics";
        for (JsonElement jsonElement : jsonArray) {
            double d2;
            JsonObject jsonObject2;
            if (!jsonElement.isJsonObject() || !LyricsProviderClient.hasNonBlankJsonField(jsonObject2 = jsonElement.getAsJsonObject(), string3) || !((d2 = LyricsProviderClient.scoreLyricsResult(jsonObject2, string, string2, l)) > d)) continue;
            d = d2;
            jsonObject = jsonObject2;
        }
        return jsonObject;
    }

    private static double scoreLyricsResult(JsonObject jsonObject, String string, String string2, long l) {
        return LyricsMatchService.scoreLyricsMatch(jsonObject, string, string2, l);
    }

    private static String normalizeArtistName(String string) {
        return LyricsMatchService.extractPrimaryTitle(string);
    }

    private static LyricsTimeline parseLyricsResponse(JsonObject jsonObject, boolean bl) {
        String string = jsonObject.get(bl ? "syncedLyrics" : "plainLyrics").getAsString();
        return bl ? LyricsTimeline.parseTimedLyrics(string) : LyricsTimeline.parsePlainLyrics(string);
    }

    private static boolean hasNonBlankJsonField(JsonObject jsonObject, String string) {
        return jsonObject.has(string) && !jsonObject.get(string).isJsonNull() && !jsonObject.get(string).getAsString().isBlank();
    }

    private static String readJsonString(JsonObject jsonObject, String string) {
        return LyricsMatchService.getJsonString(jsonObject, string);
    }

    private static String urlEncode(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    static final class ProviderBackoff {
        private static final int MAX_CONSECUTIVE_FAILURES = 3;
        private static final long COOLDOWN_MILLIS = 300000L;
        private final String providerName;
        private int consecutiveFailures;
        private long blockedUntilMillis;

        ProviderBackoff(String string) {
            this.providerName = string;
        }

        synchronized boolean isAvailable() {
            if (System.currentTimeMillis() < this.blockedUntilMillis) {
                return false;
            }
            this.blockedUntilMillis = 0L;
            return true;
        }

        synchronized void resetFailures() {
            this.consecutiveFailures = 0;
            this.blockedUntilMillis = 0L;
        }

        synchronized void recordFailure() {
            if (++this.consecutiveFailures < 3) {
                return;
            }
            this.consecutiveFailures = 0;
            this.blockedUntilMillis = System.currentTimeMillis() + 300000L;
            LOGGER.warn("[lyrics] {} \u043e\u0431\u043e\u0440\u0432\u0430\u043b \u0441\u043e\u0435\u0434\u0438\u043d\u0435\u043d\u0438\u0435 {} \u0440\u0430\u0437\u0430 \u043f\u043e\u0434\u0440\u044f\u0434 \u2014 \u043f\u0440\u043e\u043f\u0443\u0441\u043a\u0430\u0435\u043c \u0435\u0433\u043e {} \u043c\u0438\u043d\u0443\u0442", new Object[]{this.providerName, 3, 5L});
        }
    }
}

