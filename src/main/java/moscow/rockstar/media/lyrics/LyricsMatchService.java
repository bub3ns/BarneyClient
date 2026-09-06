/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package moscow.rockstar.media.lyrics;

import com.google.gson.JsonObject;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class LyricsMatchService {
    private static final double MIN_ARTIST_SIMILARITY = 0.62;
    private static final Pattern TITLE_SEPARATOR_PATTERN = Pattern.compile("(?iu)\\s*(?:[,;/&\u00d7+]|\\bfeat\\.?\\b|\\bft\\.?\\b|\\bvs\\.?\\b|\\bwith\\b|\\bx\\b|\\b\u0438\\b)\\s*");

    private LyricsMatchService() {
    }

    public static JsonObject createLyricsMetadata(String string, String string2, double d) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("trackName", string == null ? "" : string);
        jsonObject.addProperty("artistName", string2 == null ? "" : string2);
        if (d > 0.0) {
            jsonObject.addProperty("duration", (Number)d);
        }
        return jsonObject;
    }

    public static double scoreLyricsMatch(JsonObject jsonObject, String string, String string2, long l) {
        double d;
        boolean bl;
        String string3 = LyricsMatchService.normalizeText(string2);
        String string4 = LyricsMatchService.normalizeText(LyricsMatchService.getJsonString(jsonObject, "trackName"));
        double d2 = LyricsMatchService.levenshteinSimilarity(string3, string4);
        boolean bl2 = bl = string4.equals(string3) || string4.startsWith(string3 + " ") || string3.startsWith(string4 + " ");
        if (!bl && d2 < 0.55) {
            return Double.NEGATIVE_INFINITY;
        }
        if (string4.equals(string3)) {
            d2 += 1.0;
        } else if (bl) {
            d2 += 0.85;
        }
        double d3 = -1.0;
        double d4 = 0.0;
        if (l > 0L && jsonObject.has("duration") && !jsonObject.get("duration").isJsonNull()) {
            d3 = Math.abs(jsonObject.get("duration").getAsDouble() - (double)l);
            double d5 = d4 = d3 <= 2.0 ? 2.0 : Math.max(-1.0, 1.0 - d3 / 15.0);
        }
        if (!(!((d = LyricsMatchService.calculateTextSimilarity(string, LyricsMatchService.getJsonString(jsonObject, "artistName"))) < 0.62) || d3 >= 0.0 && d3 <= 2.0)) {
            return Double.NEGATIVE_INFINITY;
        }
        return d2 * 1000.0 + d * 900.0 + d4 * 1200.0;
    }

    public static double calculateTextSimilarity(String string, String string2) {
        String string3 = LyricsMatchService.normalizeText(string);
        String string4 = LyricsMatchService.normalizeText(string2);
        if (string3.isEmpty() || string4.isEmpty()) {
            return 1.0;
        }
        if (string3.equals(string4) || string3.contains(string4) || string4.contains(string3)) {
            return 1.0;
        }
        Set<String> set = LyricsMatchService.tokenizeTitle(string);
        for (String string5 : LyricsMatchService.tokenizeTitle(string2)) {
            if (!set.contains(string5)) continue;
            return 1.0;
        }
        return LyricsMatchService.levenshteinSimilarity(string3, string4);
    }

    public static String extractPrimaryTitle(String string) {
        if (string == null || string.isBlank()) {
            return "";
        }
        String[] stringArray = TITLE_SEPARATOR_PATTERN.split(string.trim());
        return stringArray.length == 0 ? string.trim() : stringArray[0].trim();
    }

    private static Set<String> tokenizeTitle(String string) {
        HashSet<String> hashSet = new HashSet<String>();
        for (String string2 : TITLE_SEPARATOR_PATTERN.split(string == null ? "" : string)) {
            String string3 = LyricsMatchService.normalizeText(string2);
            if (string3.isEmpty()) continue;
            hashSet.add(string3);
        }
        return hashSet;
    }

    public static String getJsonString(JsonObject jsonObject, String string) {
        return jsonObject != null && jsonObject.has(string) && !jsonObject.get(string).isJsonNull() ? jsonObject.get(string).getAsString() : "";
    }

    public static String normalizeText(String string) {
        return Normalizer.normalize(string == null ? "" : string, Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).replace('\u0451', '\u0435').replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }

    public static double levenshteinSimilarity(String string, String string2) {
        int n;
        if (string.equals(string2)) {
            return 1.0;
        }
        if (string.isEmpty() || string2.isEmpty()) {
            return 0.0;
        }
        int[] nArray = new int[string2.length() + 1];
        int[] nArray2 = new int[string2.length() + 1];
        for (n = 0; n <= string2.length(); ++n) {
            nArray[n] = n;
        }
        for (n = 1; n <= string.length(); ++n) {
            nArray2[0] = n;
            for (int i = 1; i <= string2.length(); ++i) {
                int n2 = nArray[i - 1] + (string.charAt(n - 1) == string2.charAt(i - 1) ? 0 : 1);
                nArray2[i] = Math.min(Math.min(nArray[i] + 1, nArray2[i - 1] + 1), n2);
            }
            int[] nArray3 = nArray;
            nArray = nArray2;
            nArray2 = nArray3;
        }
        return 1.0 - (double)nArray[string2.length()] / (double)Math.max(string.length(), string2.length());
    }
}

