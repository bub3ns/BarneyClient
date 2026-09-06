package moscow.rockstar.render.colors;

import pyrock.utility.render.ColorRGBA;

/**
 * The palette and compositor settings used by the client UI.
 *
 * <p>This used to be represented by a JSON-array class in the decompiler
 * output.  It is a fixed, typed value object: eight colors followed by the
 * numeric rendering settings.</p>
 */
public final class ThemeColorSettings {
    private ColorRGBA accentColor;
    private ColorRGBA panelColor;
    private ColorRGBA panelBackgroundColor;
    private ColorRGBA primaryTextColor;
    private ColorRGBA borderColor;
    private ColorRGBA darkBackgroundColor;
    private ColorRGBA secondaryTextColor;
    private ColorRGBA highlightColor;

    private float cornerRadius;
    private float panelOpacity;
    private float overlayAlphaMinimum;
    private float overlayAlphaMaximum;
    private float blurRadius;
    private float glassDistortion;
    private float glassScale;
    private float glassOffset;
    private float glassOpacity;
    private float glassSaturation;

    public ThemeColorSettings(
            ColorRGBA accentColor,
            ColorRGBA panelColor,
            ColorRGBA panelBackgroundColor,
            ColorRGBA primaryTextColor,
            ColorRGBA borderColor,
            ColorRGBA darkBackgroundColor,
            ColorRGBA secondaryTextColor,
            ColorRGBA highlightColor,
            float cornerRadius,
            float panelOpacity,
            float overlayAlphaMinimum,
            float overlayAlphaMaximum,
            float blurRadius,
            float glassDistortion,
            float glassScale,
            float glassOffset,
            float glassOpacity,
            float glassSaturation) {
        this.accentColor = accentColor;
        this.panelColor = panelColor;
        this.panelBackgroundColor = panelBackgroundColor;
        this.primaryTextColor = primaryTextColor;
        this.borderColor = borderColor;
        this.darkBackgroundColor = darkBackgroundColor;
        this.secondaryTextColor = secondaryTextColor;
        this.highlightColor = highlightColor;
        this.cornerRadius = cornerRadius;
        this.panelOpacity = panelOpacity;
        this.overlayAlphaMinimum = overlayAlphaMinimum;
        this.overlayAlphaMaximum = overlayAlphaMaximum;
        this.blurRadius = blurRadius;
        this.glassDistortion = glassDistortion;
        this.glassScale = glassScale;
        this.glassOffset = glassOffset;
        this.glassOpacity = glassOpacity;
        this.glassSaturation = glassSaturation;
    }

    public void copyFrom(ThemeColorSettings settings) {
        if (settings == null) {
            return;
        }
        this.accentColor = settings.accentColor;
        this.panelColor = settings.panelColor;
        this.panelBackgroundColor = settings.panelBackgroundColor;
        this.primaryTextColor = settings.primaryTextColor;
        this.borderColor = settings.borderColor;
        this.darkBackgroundColor = settings.darkBackgroundColor;
        this.secondaryTextColor = settings.secondaryTextColor;
        this.highlightColor = settings.highlightColor;
        this.cornerRadius = settings.cornerRadius;
        this.panelOpacity = settings.panelOpacity;
        this.overlayAlphaMinimum = settings.overlayAlphaMinimum;
        this.overlayAlphaMaximum = settings.overlayAlphaMaximum;
        this.blurRadius = settings.blurRadius;
        this.glassDistortion = settings.glassDistortion;
        this.glassScale = settings.glassScale;
        this.glassOffset = settings.glassOffset;
        this.glassOpacity = settings.glassOpacity;
        this.glassSaturation = settings.glassSaturation;
    }

    public void updatePaletteColors(
            ColorRGBA accentColor,
            ColorRGBA panelColor,
            ColorRGBA panelBackgroundColor,
            ColorRGBA primaryTextColor,
            ColorRGBA borderColor,
            ColorRGBA darkBackgroundColor,
            ColorRGBA secondaryTextColor,
            ColorRGBA highlightColor) {
        this.accentColor = accentColor;
        this.panelColor = panelColor;
        this.panelBackgroundColor = panelBackgroundColor;
        this.primaryTextColor = primaryTextColor;
        this.borderColor = borderColor;
        this.darkBackgroundColor = darkBackgroundColor;
        this.secondaryTextColor = secondaryTextColor;
        this.highlightColor = highlightColor;
    }

    public ColorRGBA getAccentColor() {
        return this.accentColor;
    }

    public ColorRGBA getPanelColor() {
        return this.panelColor;
    }

    public ColorRGBA getPanelBackgroundColor() {
        return this.panelBackgroundColor;
    }

    public ColorRGBA getPrimaryTextColor() {
        return this.primaryTextColor;
    }

    public ColorRGBA getBorderColor() {
        return this.borderColor;
    }

    public ColorRGBA getDarkBackgroundColor() {
        return this.darkBackgroundColor;
    }

    public ColorRGBA getSecondaryTextColor() {
        return this.secondaryTextColor;
    }

    public ColorRGBA getHighlightColor() {
        return this.highlightColor;
    }

    public float getCornerRadius() {
        return this.cornerRadius;
    }

    public float getPanelOpacity() {
        return this.panelOpacity;
    }

    public float getOverlayAlphaMinimum() {
        return this.overlayAlphaMinimum;
    }

    public float getOverlayAlphaMaximum() {
        return this.overlayAlphaMaximum;
    }

    public float getBlurRadius() {
        return this.blurRadius;
    }

    public float getGlassDistortion() {
        return this.glassDistortion;
    }

    public float getGlassScale() {
        return this.glassScale;
    }

    public float getGlassOffset() {
        return this.glassOffset;
    }

    public float getGlassOpacity() {
        return this.glassOpacity;
    }

    public float getGlassSaturation() {
        return this.glassSaturation;
    }
}
