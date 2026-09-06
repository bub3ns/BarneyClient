/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  lombok.Generated
 */
package moscow.rockstar.render.colors;

import com.google.gson.JsonObject;
import java.awt.Color;
import java.util.Arrays;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.ui.animation.AnimatedColor;
import moscow.rockstar.ui.theme.ColorTheme;
import pyrock.utility.render.ColorRGBA;

public final class ColorPalette {
    public static final ColorRGBA RED = new ColorRGBA(255.0f, 0.0f, 0.0f);
    public static final ColorRGBA GREEN = new ColorRGBA(0.0f, 255.0f, 0.0f);
    public static final ColorRGBA BLUE = new ColorRGBA(0.0f, 0.0f, 255.0f);
    public static final ColorRGBA WHITE = new ColorRGBA(255.0f, 255.0f, 255.0f);
    public static final ColorRGBA BLACK = new ColorRGBA(0.0f, 0.0f, 0.0f);
    public static final int LAST_COLOR_CHANNEL_INDEX = 2;
    private static final ColorRGBA ACCENT_PURPLE = new ColorRGBA(144.0f, 107.0f, 255.0f);
    private static final int ACCENT_COLOR_INDEX = 0;
    private static final int PANEL_COLOR_INDEX = 1;
    private static final int MUTED_PANEL_COLOR_INDEX = 2;
    private static final int BORDER_COLOR_INDEX = 3;
    private static final int VIBRANT_ACCENT_COLOR_INDEX = 4;
    private static final int PRIMARY_TEXT_COLOR_INDEX = 5;
    private static final int PANEL_BACKGROUND_COLOR_INDEX = 6;
    private static final int DARK_BACKGROUND_COLOR_INDEX = 7;
    private static final int HIGHLIGHT_COLOR_INDEX = 8;
    private static final ColorRGBA LIGHT_REFERENCE_COLOR = new ColorRGBA(255.0f, 255.0f, 255.0f);
    private static final ColorRGBA DARK_REFERENCE_COLOR = new ColorRGBA(16.0f, 14.0f, 20.0f);
    private static final float LUMINANCE_FLOOR = 0.58f;
    private static final float LUMINANCE_CEILING = 0.7f;
    private static final ColorRGBA[] BASE_PALETTE_COLORS = new ColorRGBA[]{ACCENT_PURPLE, new ColorRGBA(24.0f, 21.0f, 29.0f, 229.5f), new ColorRGBA(24.0f, 21.0f, 29.0f, 102.0f), new ColorRGBA(61.0f, 54.0f, 71.0f, 63.75f), new ColorRGBA(77.0f, 0.0f, 255.0f), new ColorRGBA(255.0f, 255.0f, 255.0f), new ColorRGBA(26.0f, 23.0f, 31.0f), new ColorRGBA(5.0f, 4.0f, 7.0f)};
    public static final ColorRGBA ACCENT_COLOR = new DynamicPaletteColor(0);
    public static final ColorRGBA PANEL_COLOR = new DynamicPaletteColor(1);
    public static final ColorRGBA MUTED_PANEL_COLOR = new DynamicPaletteColor(2);
    public static final ColorRGBA BORDER_COLOR = new DynamicPaletteColor(3);
    public static final ColorRGBA VIBRANT_ACCENT_COLOR = new DynamicPaletteColor(4);
    public static final ColorRGBA PRIMARY_TEXT_COLOR = new DynamicPaletteColor(5);
    public static final ColorRGBA HIGHLIGHT_COLOR = new DynamicPaletteColor(8);
    private static final ThemeColorSettings themeColorSettings = new ThemeColorSettings(ACCENT_PURPLE, BASE_PALETTE_COLORS[1], BASE_PALETTE_COLORS[6], BASE_PALETTE_COLORS[5], BASE_PALETTE_COLORS[3], BASE_PALETTE_COLORS[7], BASE_PALETTE_COLORS[5], BASE_PALETTE_COLORS[5], 7.0f, 0.5f, 0.8f, 0.2f, 25.0f, 0.08f, 2.0f, 0.0f, 1.0f, 1.0f);
    private static final AnimatedColor animatedAccentColor = new AnimatedColor(500L, ACCENT_PURPLE);
    private static final float[] accentHsb = ColorPalette.convertToHsb(ACCENT_PURPLE);
    private static final ColorRGBA[] resolvedPaletteColors = ColorPalette.createPaletteSnapshot();
    private static final ColorRGBA[] themeAdjustedColors = ColorPalette.createPaletteSnapshot();
    private static ColorRGBA currentAccentColor = ACCENT_PURPLE;
    private static long lastPaletteUpdateMillis;
    private static int paletteStateHash;

    public static ColorRGBA getCurrentAccentColor() {
        return currentAccentColor;
    }

    public static void setCurrentAccentColor(ColorRGBA colorRGBA) {
        if (colorRGBA != null) {
            currentAccentColor = colorRGBA.withAlpha(255.0f);
        }
    }

    public static void applyThemeColorSettings(ThemeColorSettings settings) {
        if (settings == null) {
            return;
        }
        if (settings.getAccentColor() != null && settings.getAccentColor().getAlpha() > 0.0f) {
            ColorPalette.setCurrentAccentColor(settings.getAccentColor());
        }
        animatedAccentColor.setCurrentColor(currentAccentColor);
        paletteStateHash = -1;
        themeColorSettings.copyFrom(settings);
    }

    public static ThemeColorSettings getThemeColorSettings() {
        ColorPalette.refreshPalette();
        return themeColorSettings;
    }

    public static ColorRGBA getAccentColor() {
        return ColorPalette.getResolvedPaletteColor(0);
    }

    public static ColorRGBA getPanelColor() {
        return ColorPalette.getResolvedPaletteColor(1);
    }

    public static ColorRGBA getPanelBackgroundColor() {
        return ColorPalette.getResolvedPaletteColor(6);
    }

    public static ColorRGBA getPrimaryTextColor() {
        return ColorPalette.getResolvedPaletteColor(5);
    }

    public static ColorRGBA getDarkBackgroundColor() {
        return ColorPalette.getResolvedPaletteColor(7);
    }

    public static ColorRGBA getHighlightColor() {
        return ColorPalette.getResolvedPaletteColor(8);
    }

    public static ColorRGBA blendWithContrastBackground(ColorRGBA colorRGBA) {
        return ColorPalette.blendByBrightness(colorRGBA, LIGHT_REFERENCE_COLOR, DARK_REFERENCE_COLOR);
    }

    public static ColorRGBA blendByBrightness(ColorRGBA colorRGBA, ColorRGBA colorRGBA2, ColorRGBA colorRGBA3) {
        return colorRGBA2.mix(colorRGBA3, ColorPalette.getBrightnessBlendFactor(colorRGBA));
    }

    public static float getBrightnessBlendFactor(ColorRGBA colorRGBA) {
        return ColorPalette.calculateBrightnessBlendFactor(colorRGBA, ColorPalette.getPanelColor());
    }

    private static float calculateBrightnessBlendFactor(ColorRGBA colorRGBA, ColorRGBA colorRGBA2) {
        float f = Math.clamp(colorRGBA.getAlpha() / 255.0f, 0.0f, 1.0f);
        float f2 = colorRGBA.getRed() * f + colorRGBA2.getRed() * (1.0f - f);
        float f3 = colorRGBA.getGreen() * f + colorRGBA2.getGreen() * (1.0f - f);
        float f4 = colorRGBA.getBlue() * f + colorRGBA2.getBlue() * (1.0f - f);
        float f5 = (0.299f * f2 + 0.587f * f3 + 0.114f * f4) / 255.0f;
        return Math.clamp((f5 - 0.58f) / 0.120000005f, 0.0f, 1.0f);
    }

    public static ColorRGBA getShadowColor() {
        return ColorRGBA.BLACK.withAlpha(255.0f * (ColorPalette.getActiveTheme() == ColorTheme.DARK ? 0.08f : 0.05f));
    }

    private static ColorTheme getActiveTheme() {
        return RockstarClient.create().getColorTheme();
    }

    static ColorRGBA getResolvedPaletteColor(int n) {
        ColorPalette.refreshPalette();
        return themeAdjustedColors[n];
    }

    private static void refreshPalette() {
        long l = System.currentTimeMillis();
        if (l == lastPaletteUpdateMillis) {
            return;
        }
        lastPaletteUpdateMillis = l;
        animatedAccentColor.setTargetColor(currentAccentColor);
        ColorRGBA colorRGBA = animatedAccentColor.getColor();
        ColorTheme colorTheme = ColorPalette.getActiveTheme();
        int n = 31 * colorRGBA.getRGB() + colorTheme.ordinal();
        if (n == paletteStateHash) {
            return;
        }
        paletteStateHash = n;
        float[] fArray = ColorPalette.convertToHsb(colorRGBA);
        float f = fArray[0] - accentHsb[0];
        float f2 = accentHsb[1] == 0.0f ? 1.0f : fArray[1] / accentHsb[1];
        ColorPalette.resolvedPaletteColors[0] = colorRGBA;
        for (int i = 1; i < BASE_PALETTE_COLORS.length; ++i) {
            ColorRGBA colorRGBA2 = BASE_PALETTE_COLORS[i];
            float[] fArray2 = ColorPalette.convertToHsb(colorRGBA2);
            if (fArray2[1] == 0.0f) {
                ColorPalette.resolvedPaletteColors[i] = colorRGBA2;
                continue;
            }
            float f3 = fArray2[0] + f;
            ColorPalette.resolvedPaletteColors[i] = ColorRGBA.fromHSB(f3 - (float)Math.floor(f3), Math.clamp(fArray2[1] * f2, 0.0f, 1.0f), fArray2[2]).withAlpha(colorRGBA2.getAlpha());
        }
        System.arraycopy(resolvedPaletteColors, 0, themeAdjustedColors, 0, resolvedPaletteColors.length);
        if (colorTheme == ColorTheme.LIGHT) {
            ColorPalette.themeAdjustedColors[1] = ColorTheme.LIGHT.getSecondaryTextColor().withAlpha(resolvedPaletteColors[1].getAlpha());
            ColorPalette.themeAdjustedColors[2] = ColorTheme.LIGHT.getSecondaryTextColor().withAlpha(resolvedPaletteColors[2].getAlpha());
            ColorPalette.themeAdjustedColors[3] = ColorTheme.LIGHT.getBorderColor().withAlpha(resolvedPaletteColors[3].getAlpha());
            ColorPalette.themeAdjustedColors[5] = ColorTheme.LIGHT.getPrimaryTextColor().withAlpha(resolvedPaletteColors[5].getAlpha());
            ColorPalette.themeAdjustedColors[6] = ColorTheme.LIGHT.getPanelColor().withAlpha(resolvedPaletteColors[6].getAlpha());
            ColorPalette.themeAdjustedColors[7] = ColorTheme.LIGHT.getAccentColor().withAlpha(resolvedPaletteColors[7].getAlpha());
        }
        ColorPalette.themeAdjustedColors[8] = LIGHT_REFERENCE_COLOR.mix(DARK_REFERENCE_COLOR, ColorPalette.calculateBrightnessBlendFactor(themeAdjustedColors[0], themeAdjustedColors[1]));
        themeColorSettings.updatePaletteColors(currentAccentColor, themeAdjustedColors[1], themeAdjustedColors[6], themeAdjustedColors[5], themeAdjustedColors[3], themeAdjustedColors[7], themeAdjustedColors[5], themeAdjustedColors[5]);
    }

    private static ColorRGBA[] createPaletteSnapshot() {
        ColorRGBA[] colorRGBAArray = Arrays.copyOf(BASE_PALETTE_COLORS, 9);
        colorRGBAArray[8] = LIGHT_REFERENCE_COLOR;
        return colorRGBAArray;
    }

    private static float[] convertToHsb(ColorRGBA colorRGBA) {
        return Color.RGBtoHSB(Math.round(colorRGBA.getRed()), Math.round(colorRGBA.getGreen()), Math.round(colorRGBA.getBlue()), null);
    }

    @Generated
    private ColorPalette() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static {
        paletteStateHash = -1;
    }

    static final class DynamicPaletteColor
    extends ColorRGBA {
        private final int paletteIndex;

        DynamicPaletteColor(int n) {
            super(0.0f, 0.0f, 0.0f);
            this.paletteIndex = n;
        }

        private ColorRGBA resolveColor() {
            return ColorPalette.getResolvedPaletteColor(this.paletteIndex);
        }

        @Override
        public float getRed() {
            return this.resolveColor().getRed();
        }

        @Override
        public float getGreen() {
            return this.resolveColor().getGreen();
        }

        @Override
        public float getBlue() {
            return this.resolveColor().getBlue();
        }

        @Override
        public float getAlpha() {
            return this.resolveColor().getAlpha();
        }

        @Override
        public int getRGB() {
            return this.resolveColor().getRGB();
        }

        @Override
        public String toHex() {
            return this.resolveColor().toHex();
        }

        @Override
        public ColorRGBA withAlpha(float f) {
            return this.resolveColor().withAlpha(f);
        }

        @Override
        public ColorRGBA mulAlpha(float f) {
            return this.resolveColor().mulAlpha(f);
        }

        @Override
        public ColorRGBA mix(ColorRGBA colorRGBA, float f) {
            return this.resolveColor().mix(colorRGBA, f);
        }

        @Override
        public float getHue() {
            return this.resolveColor().getHue();
        }

        @Override
        public float getSaturation() {
            return this.resolveColor().getSaturation();
        }

        @Override
        public float getBrightness() {
            return this.resolveColor().getBrightness();
        }

        @Override
        public JsonObject toJson() {
            return this.resolveColor().toJson();
        }

        @Override
        public boolean equals(Object object) {
            return this.resolveColor().equals(object);
        }

        @Override
        public int hashCode() {
            return this.resolveColor().hashCode();
        }

        @Override
        public String toString() {
            return this.resolveColor().toString();
        }
    }
}
