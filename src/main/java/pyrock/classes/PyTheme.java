/*
 * Decompiled with CFR 0.152.
 */
package pyrock.classes;

import java.util.Locale;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.colors.ColorPalette;
import pyrock.utility.render.ColorRGBA;

public class PyTheme {
    public ColorRGBA color(String string) {
        if (string == null) {
            return ColorPalette.PRIMARY_TEXT_COLOR;
        }
        return switch (string.toLowerCase(Locale.ROOT)) {
            case "accent" -> ColorPalette.ACCENT_COLOR;
            case "background", "bg" -> ColorPalette.getPanelColor();
            case "second", "additional" -> ColorPalette.MUTED_PANEL_COLOR;
            case "outline", "border" -> ColorPalette.BORDER_COLOR;
            case "shadow" -> ColorPalette.VIBRANT_ACCENT_COLOR;
            case "on_accent", "onaccent" -> ColorPalette.HIGHLIGHT_COLOR;
            case "flat" -> ColorPalette.getDarkBackgroundColor();
            case "separator" -> ColorPalette.getShadowColor();
            case "white" -> ColorPalette.WHITE;
            case "black" -> ColorPalette.BLACK;
            default -> ColorPalette.PRIMARY_TEXT_COLOR;
        };
    }

    public ColorRGBA accent() {
        return ColorPalette.ACCENT_COLOR;
    }

    public ColorRGBA text() {
        return ColorPalette.PRIMARY_TEXT_COLOR;
    }

    public ColorRGBA background() {
        return ColorPalette.getPanelColor();
    }

    public ColorRGBA second() {
        return ColorPalette.MUTED_PANEL_COLOR;
    }

    public ColorRGBA outline() {
        return ColorPalette.BORDER_COLOR;
    }

    public ColorRGBA onAccent() {
        return ColorPalette.HIGHLIGHT_COLOR;
    }

    public ColorRGBA readable(ColorRGBA colorRGBA) {
        return colorRGBA == null ? ColorPalette.PRIMARY_TEXT_COLOR : ColorPalette.blendWithContrastBackground(colorRGBA);
    }

    public void setAccent(ColorRGBA colorRGBA) {
        if (colorRGBA != null) {
            ColorPalette.setCurrentAccentColor(colorRGBA);
        }
    }

    public String name() {
        try {
            return RockstarClient.create().getColorTheme().name().toLowerCase(Locale.ROOT);
        }
        catch (Throwable throwable) {
            return "dark";
        }
    }

    public boolean dark() {
        return "dark".equals(this.name());
    }
}
