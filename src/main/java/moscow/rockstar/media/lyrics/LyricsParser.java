/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.jsoup.Jsoup
 *  org.jsoup.nodes.Document
 *  org.jsoup.nodes.Element
 *  org.jsoup.nodes.Node
 *  org.jsoup.nodes.TextNode
 *  org.jsoup.select.Elements
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package moscow.rockstar.media.lyrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import moscow.rockstar.network.http.HttpResourceClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LyricsParser {
    private static final Logger lyricLogger = LoggerFactory.getLogger((String)"rockstar-lyrics");
    private static final String geniusSearchEndpoint = "https://genius.com/api/search/song?per_page=10&q=";
    private static final String browserUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";

    public static String buildSearchUrl(String string, String string2) {
        if (string == null || string2 == null || string.isBlank() || string2.isBlank()) {
            return null;
        }
        try {
            SongUrl songUrl = LyricsParser.parseSongUrl(string, string2);
            if (songUrl == null) {
                lyricLogger.info("[lyrics] genius: {} - {} \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d", (Object)string, (Object)string2);
                return null;
            }
            return LyricsParser.removeLyricsAnnotations(songUrl.url);
        }
        catch (IOException iOException) {
            lyricLogger.warn("[lyrics] genius \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d: {}", (Object)iOException.toString());
        }
        catch (Exception exception) {
            lyricLogger.warn("[lyrics] genius: {}", (Object)exception.toString());
        }
        return null;
    }

    private static SongUrl parseSongUrl(String string, String string2) throws IOException {
        LinkedHashSet<String> searchQueries = new LinkedHashSet<String>();
        searchQueries.add(string + " " + string2);
        String string3 = LyricsParser.decodeHtmlEntities(string2);
        if (!string3.equals(string2)) {
            searchQueries.add(string + " " + string3);
        }
        SongUrl songUrl = null;
        int n = -1;
        for (String string4 : searchQueries) {
            JsonObject jsonObject = LyricsParser.parseJson(geniusSearchEndpoint + LyricsParser.normalizeWhitespace(string4));
            for (JsonObject jsonObject2 : LyricsParser.parseSongResults(jsonObject)) {
                int n2 = LyricsParser.getJsonInt(jsonObject2, string, string2);
                String string5 = LyricsParser.getJsonString(jsonObject2, "url");
                if (string5.isBlank() || n2 <= n) continue;
                n = n2;
                songUrl = new SongUrl(string5);
            }
            if (n < 250) continue;
            break;
        }
        return n >= 100 ? songUrl : null;
    }

    private static JsonObject parseJson(String string) throws IOException {
        HttpResourceClient.HttpResponse httpResponse = HttpResourceClient.get(string, "User-Agent", browserUserAgent, "Accept-Language", "ru,en-US;q=0.9,en;q=0.8", "Accept", "application/json, text/plain, */*", "Referer", "https://genius.com/");
        if (!httpResponse.isSuccess()) {
            return null;
        }
        try {
            return JsonParser.parseString((String)httpResponse.getBody()).getAsJsonObject();
        }
        catch (Exception exception) {
            return null;
        }
    }

    private static List<JsonObject> parseSongResults(JsonObject jsonObject) {
        ArrayList<JsonObject> arrayList = new ArrayList<JsonObject>();
        if (jsonObject == null) {
            return arrayList;
        }
        JsonObject jsonObject2 = jsonObject.getAsJsonObject("response");
        if (jsonObject2 == null) {
            return arrayList;
        }
        LyricsParser.appendSongUrls(jsonObject2.getAsJsonArray("hits"), arrayList);
        JsonArray jsonArray = jsonObject2.getAsJsonArray("sections");
        if (jsonArray != null) {
            for (JsonElement jsonElement : jsonArray) {
                if (!jsonElement.isJsonObject()) continue;
                LyricsParser.appendSongUrls(jsonElement.getAsJsonObject().getAsJsonArray("hits"), arrayList);
            }
        }
        return arrayList;
    }

    private static void appendSongUrls(JsonArray jsonArray, List<JsonObject> list) {
        if (jsonArray == null) {
            return;
        }
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject;
            if (!jsonElement.isJsonObject() || (jsonObject = jsonElement.getAsJsonObject().getAsJsonObject("result")) == null) continue;
            list.add(jsonObject);
        }
    }

    private static int getJsonInt(JsonObject jsonObject, String string, String string2) {
        String string3;
        int n;
        String string4 = LyricsParser.stripHtmlTags(string2);
        String string5 = LyricsParser.stripHtmlTags(LyricsParser.getJsonString(jsonObject, "title"));
        if (string4.isEmpty() || string5.isEmpty()) {
            return -1;
        }
        if (string5.equals(string4)) {
            n = 160;
        } else if (string5.startsWith(string4) || string5.contains(string4)) {
            n = 125;
        } else if (string4.contains(string5)) {
            n = 90;
        } else {
            return -1;
        }
        String string6 = LyricsParser.getJsonString(jsonObject, "artist_names");
        if (string6.isBlank()) {
            JsonObject primaryArtist = jsonObject.getAsJsonObject("primary_artist");
            string6 = LyricsParser.getJsonString(primaryArtist, "name");
        }
        string3 = LyricsParser.stripHtmlTags(string);
        String string7 = LyricsParser.stripHtmlTags(string6);
        int n2 = 0;
        if (!string3.isEmpty() && string7.equals(string3)) {
            n2 = 140;
        } else if (!string3.isEmpty() && (string7.contains(string3) || string3.contains(string7))) {
            n2 = 100;
        }
        if (string7.startsWith("genius ") && n2 == 0) {
            n2 -= 100;
        }
        return n + n2;
    }

    private static String getJsonString(JsonObject jsonObject, String string) {
        if (jsonObject == null || !jsonObject.has(string) || jsonObject.get(string).isJsonNull()) {
            return "";
        }
        try {
            return jsonObject.get(string).getAsString();
        }
        catch (Exception exception) {
            return "";
        }
    }

    private static String stripHtmlTags(String string) {
        if (string == null) {
            return "";
        }
        String string2 = Normalizer.normalize(string, Normalizer.Form.NFKD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).replace('\u0451', '\u0435').replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
        return string2.replaceAll("\\s+", " ");
    }

    private static String decodeHtmlEntities(String string) {
        if (string == null) {
            return "";
        }
        return string.replaceAll("(?i)\\s*[\\[(](feat\\.?|ft\\.?|featuring|prod\\.?|with)\\b.*?[\\])]", "").replaceAll("(?i)\\s*[-\u2013\u2014]\\s*(remaster(?:ed)?|live|radio edit|single version|official audio).*$", "").trim();
    }

    private static String normalizeWhitespace(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    private static String removeLyricsAnnotations(String string) throws IOException {
        HttpResourceClient.HttpResponse httpResponse = HttpResourceClient.get(string, "User-Agent", browserUserAgent, "Accept-Language", "ru,en-US;q=0.9,en;q=0.8", "Accept", "text/html,application/xhtml+xml");
        return httpResponse.isSuccess() ? LyricsParser.fetchLyrics(httpResponse.getBody()) : null;
    }

    static String fetchLyrics(String string) {
        if (string == null || string.isBlank()) {
            return null;
        }
        Document document = Jsoup.parse((String)string);
        Elements elements = document.select("[data-lyrics-container=true]");
        if (elements.isEmpty()) {
            elements = document.select("div[class*=Lyrics__Container]");
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Element element : elements) {
            Element element2 = element.clone();
            element2.select("[data-exclude-from-selection], button, script, style, svg").remove();
            for (Element element3 : element2.select("br")) {
                element3.after((Node)new TextNode("\n"));
                element3.remove();
            }
            String string2 = LyricsParser.normalizeLineBreaks(element2.wholeText());
            if (string2.isBlank()) continue;
            if (!stringBuilder.isEmpty()) {
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(string2);
        }
        return stringBuilder.isEmpty() ? null : stringBuilder.toString();
    }

    private static String normalizeLineBreaks(String string) {
        String[] stringArray = string.replace('\u00a0', ' ').replace("\r", "").split("\n", -1);
        StringBuilder stringBuilder = new StringBuilder();
        boolean bl = true;
        for (String string2 : stringArray) {
            String string3 = string2.strip().replaceAll("[ \\t]+", " ");
            if (string3.isEmpty()) {
                if (bl || stringBuilder.isEmpty()) continue;
                stringBuilder.append('\n');
                bl = true;
                continue;
            }
            if (!stringBuilder.isEmpty() && !bl) {
                stringBuilder.append('\n');
            }
            stringBuilder.append(string3);
            bl = false;
        }
        return stringBuilder.toString().strip();
    }

    static final class SongUrl {
        final String url;

        SongUrl(String string) {
            this.url = string;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "url");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "url");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "url");
        }

        public String getUrl() {
            return this.url;
        }
    }
}
