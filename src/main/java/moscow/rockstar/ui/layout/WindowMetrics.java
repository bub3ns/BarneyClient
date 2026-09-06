/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.layout;

import moscow.rockstar.core.ClientAccess;

public class WindowMetrics
implements ClientAccess {
    public Number getWidth() {
        return minecraftClient.getWindow().getScaledWidth();
    }

    public Number getHeight() {
        return minecraftClient.getWindow().getScaledHeight();
    }

    public Number getScaleFactor() {
        return minecraftClient.getWindow().getScaleFactor();
    }

    public float width() {
        return minecraftClient.getWindow().getScaledWidth();
    }

    public float height() {
        return minecraftClient.getWindow().getScaledHeight();
    }

    public double scaleFactor() {
        return minecraftClient.getWindow().getScaleFactor();
    }
}

