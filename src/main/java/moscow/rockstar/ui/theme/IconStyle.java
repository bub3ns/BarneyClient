/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.theme;

import lombok.Generated;
import moscow.rockstar.ui.theme.IconSet;

public enum IconStyle {
    COMBAT_ICON(IconSet.MENU),
    MOVEMENT_ICON(IconSet.MENU),
    VISUALS_ICON(IconSet.MENU),
    PLAYER_ICON(IconSet.MENU),
    OTHER_ICON(IconSet.MENU),
    COMBAT_SELECTED_ICON(IconSet.BIG_MENU),
    MOVEMENT_SELECTED_ICON(IconSet.BIG_MENU),
    VISUALS_SELECTED_ICON(IconSet.BIG_MENU),
    PLAYER_SELECTED_ICON(IconSet.BIG_MENU),
    OTHER_SELECTED_ICON(IconSet.BIG_MENU),
    SUCCESS_ICON(IconSet.ALERTS),
    ERROR_ICON(IconSet.ALERTS),
    INFO_ICON(IconSet.ALERTS),
    ALERT_ICON(IconSet.MENU);
    private final IconSet iconSet;
    public final float iconOffset;

    private IconStyle(IconSet iconSet) {
        this.iconSet = iconSet;
        this.iconOffset = iconSet.iconOffsetCursor;
        iconSet.iconOffsetCursor += iconSet.getIconHeight();
    }

    @Generated
    public IconSet getIconSet() {
        return this.iconSet;
    }

    @Generated
    public float getIconOffset() {
        return this.iconOffset;
    }
}

