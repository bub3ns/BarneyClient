package moscow.rockstar.util;

/** Formatting helpers for numeric values shown in the client UI. */
public final class NumberFormatting {
    private NumberFormatting() {
    }

    public static String formatDecimal(double value) {
        if (value == (int) value) {
            return String.valueOf((int) value);
        }
        String formatted = String.format("%.2f", value)
            .replace(",", ".")
            .replaceAll("\\.?0+$", "");
        return formatted.endsWith(".") ? formatted.substring(0, formatted.length() - 1) : formatted;
    }

    public static String formatOneDecimal(double value) {
        return String.format("%.1f", value).replace(",", ".");
    }
}
