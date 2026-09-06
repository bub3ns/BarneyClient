/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.theme;

import lombok.Generated;
import pyrock.utility.render.ColorRGBA;

public enum ColorTheme {
    DARK(new ColorRGBA(255.0f, 255.0f, 255.0f), new ColorRGBA(12.0f, 12.0f, 12.0f), new ColorRGBA(24.0f, 24.0f, 27.0f), new ColorRGBA(32.0f, 32.0f, 32.0f), ColorRGBA.BLACK),
    LIGHT(new ColorRGBA(10.0f, 10.0f, 10.0f), new ColorRGBA(229.0f, 229.0f, 229.0f), new ColorRGBA(255.0f, 255.0f, 255.0f), new ColorRGBA(32.0f, 32.0f, 32.0f), ColorRGBA.WHITE);
    private final ColorRGBA primaryTextColor;
    private final ColorRGBA mutedTextColor;
    private final ColorRGBA panelColor;
    private final ColorRGBA borderColor;
    private final ColorRGBA accentColor;

    @Generated
    public ColorRGBA getPrimaryTextColor() {
        return this.primaryTextColor;
    }

    @Generated
    public ColorRGBA getSecondaryTextColor() {
        return this.mutedTextColor;
    }

    @Generated
    public ColorRGBA getPanelColor() {
        return this.panelColor;
    }

    @Generated
    public ColorRGBA getBorderColor() {
        return this.borderColor;
    }

    @Generated
    public ColorRGBA getAccentColor() {
        return this.accentColor;
    }

    @Generated
    private ColorTheme(ColorRGBA colorRGBA, ColorRGBA colorRGBA2, ColorRGBA colorRGBA3, ColorRGBA colorRGBA4, ColorRGBA colorRGBA5) {
        this.primaryTextColor = colorRGBA;
        this.mutedTextColor = colorRGBA2;
        this.panelColor = colorRGBA3;
        this.borderColor = colorRGBA4;
        this.accentColor = colorRGBA5;
    }
}

