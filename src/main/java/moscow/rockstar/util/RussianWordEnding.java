package moscow.rockstar.util;

/** Small inflection helper used by Russian status notifications. */
public final class RussianWordEnding {
    private RussianWordEnding() {
    }

    public static String notificationSuffix(String word) {
        if (word.endsWith("а") || word.endsWith("a")) {
            return "а";
        }
        if (word.endsWith("y")) {
            return "о";
        }
        if (word.endsWith("ю") || word.endsWith("u")) {
            return "o";
        }
        if (word.endsWith("я")) {
            return "а";
        }
        if (word.endsWith("ы") || word.endsWith("и")) {
            return "ы";
        }
        return "";
    }
}
