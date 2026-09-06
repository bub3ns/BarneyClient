/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.window;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.core.ClientAccess;
import pyrock.events.EventCancellable;

@ScreenController(description="scroll")
public class ScrollEvent
extends EventCancellable
implements ClientAccess {
    private final double horizontal;
    private final double vertical;
    private final float x;
    private final float y;

    public ScrollEvent(double d, double d2) {
        this.horizontal = d;
        this.vertical = d2;
        double d3 = minecraftClient.getWindow().getScaleFactor();
        this.x = (float)(ScrollEvent.minecraftClient.mouse.getX() / d3);
        this.y = (float)(ScrollEvent.minecraftClient.mouse.getY() / d3);
    }

    public boolean isUp() {
        return this.vertical > 0.0;
    }

    public boolean isDown() {
        return this.vertical < 0.0;
    }

    public boolean isScreenOpen() {
        return ScrollEvent.minecraftClient.currentScreen != null;
    }

    @Generated
    public double getHorizontal() {
        return this.horizontal;
    }

    @Generated
    public double getVertical() {
        return this.vertical;
    }

    @Generated
    public float getX() {
        return this.x;
    }

    @Generated
    public float getY() {
        return this.y;
    }
}

