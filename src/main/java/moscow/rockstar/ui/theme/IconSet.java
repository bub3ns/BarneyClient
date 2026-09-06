/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.theme;

import lombok.Generated;

public enum IconSet {
    MENU("icons/batched/menu.png", 96.0f, 16.0f, 16.0f),
    BIG_MENU("icons/batched/bigmenu.png", 120.0f, 20.0f, 20.0f),
    ALERTS("icons/batched/alerts.png", 36.0f, 12.0f, 12.0f);
    private final String resourcePath;
    private final float atlasSize;
    private final float iconWidth;
    private final float iconHeight;
    public float iconOffsetCursor;

    @Generated
    public String getResourcePath() {
        return this.resourcePath;
    }

    @Generated
    public float getAtlasSize() {
        return this.atlasSize;
    }

    @Generated
    public float getIconWidth() {
        return this.iconWidth;
    }

    @Generated
    public float getIconHeight() {
        return this.iconHeight;
    }

    @Generated
    public float getIconOffsetCursor() {
        return this.iconOffsetCursor;
    }

    @Generated
    private IconSet(String string2, float f, float f2, float f3) {
        this.resourcePath = string2;
        this.atlasSize = f;
        this.iconWidth = f2;
        this.iconHeight = f3;
    }
}

