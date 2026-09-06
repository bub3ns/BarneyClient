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
import moscow.rockstar.events.Event;

@ScreenController(description="mouse_move")
public class MouseMoveEvent
extends Event
implements ClientAccess {
    private final float x;
    private final float y;
    private final float dx;
    private final float dy;

    public MouseMoveEvent(double d, double d2, double d3, double d4) {
        double d5 = minecraftClient.getWindow().getScaleFactor();
        this.x = (float)(d / d5);
        this.y = (float)(d2 / d5);
        this.dx = (float)(d3 / d5);
        this.dy = (float)(d4 / d5);
    }

    public boolean isScreenOpen() {
        return MouseMoveEvent.minecraftClient.currentScreen != null;
    }

    @Generated
    public float getX() {
        return this.x;
    }

    @Generated
    public float getY() {
        return this.y;
    }

    @Generated
    public float getDx() {
        return this.dx;
    }

    @Generated
    public float getDy() {
        return this.dy;
    }
}

