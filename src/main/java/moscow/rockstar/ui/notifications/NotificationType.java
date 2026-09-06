/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.notifications;

import lombok.Generated;
import moscow.rockstar.ui.theme.IconStyle;
import pyrock.utility.render.ColorRGBA;

public enum NotificationType {
    SUCCESS("success", new ColorRGBA(237.0f, 255.0f, 249.0f), new ColorRGBA(98.0f, 255.0f, 0.0f), new ColorRGBA(171.0f, 255.0f, 132.0f), IconStyle.SUCCESS_ICON),
    ERROR("error", ColorRGBA.RED, ColorRGBA.RED, ColorRGBA.RED, IconStyle.ERROR_ICON),
    INFO("info", new ColorRGBA(234.0f, 179.0f, 8.0f), new ColorRGBA(234.0f, 179.0f, 8.0f), new ColorRGBA(234.0f, 179.0f, 8.0f), IconStyle.INFO_ICON);
    private final String key;
    private final ColorRGBA backgroundColor;
    private final ColorRGBA accentColor;
    private final ColorRGBA textColor;
    private final IconStyle iconStyle;

    public static NotificationType fromKey(String string) {
        for (NotificationType notificationType : NotificationType.values()) {
            if (!notificationType.getKey().equalsIgnoreCase(string)) continue;
            return notificationType;
        }
        return INFO;
    }

    @Generated
    public String getKey() {
        return this.key;
    }

    @Generated
    public ColorRGBA getBackgroundColor() {
        return this.backgroundColor;
    }

    @Generated
    public ColorRGBA getAccentColor() {
        return this.accentColor;
    }

    @Generated
    public ColorRGBA getTextColor() {
        return this.textColor;
    }

    @Generated
    public IconStyle getIconStyle() {
        return this.iconStyle;
    }

    @Generated
    private NotificationType(String string2, ColorRGBA colorRGBA, ColorRGBA colorRGBA2, ColorRGBA colorRGBA3, IconStyle iconStyle) {
        this.key = string2;
        this.backgroundColor = colorRGBA;
        this.accentColor = colorRGBA2;
        this.textColor = colorRGBA3;
        this.iconStyle = iconStyle;
    }
}

