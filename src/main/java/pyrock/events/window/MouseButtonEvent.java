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
import moscow.rockstar.network.http.client.UrlConnectionClient;
import pyrock.events.EventCancellable;

@ScreenController(description="mouse")
public class MouseButtonEvent
extends EventCancellable
implements ClientAccess {
    private final int button;
    private final int action;
    private final int mods;
    private final float x;
    private final float y;

    public MouseButtonEvent(int n, int n2, int n3) {
        this.button = n;
        this.action = n2;
        this.mods = n3;
        double d = minecraftClient.getWindow().getScaleFactor();
        this.x = (float)(MouseButtonEvent.minecraftClient.mouse.getX() / d);
        this.y = (float)(MouseButtonEvent.minecraftClient.mouse.getY() / d);
    }

    public String getName() {
        return moscow.rockstar.ui.input.KeyDisplayFormatter.formatRawKey(this.button);
    }

    public boolean isPress() {
        return this.action == 1;
    }

    public boolean isRelease() {
        return this.action == 0;
    }

    public boolean isLeft() {
        return this.button == 0;
    }

    public boolean isRight() {
        return this.button == 1;
    }

    public boolean isMiddle() {
        return this.button == 2;
    }

    public boolean isShift() {
        return (this.mods & 1) != 0;
    }

    public boolean isCtrl() {
        return (this.mods & 2) != 0;
    }

    public boolean isAlt() {
        return (this.mods & 4) != 0;
    }

    public boolean isScreenOpen() {
        return MouseButtonEvent.minecraftClient.currentScreen != null;
    }

    @Generated
    public int getButton() {
        return this.button;
    }

    @Generated
    public int getAction() {
        return this.action;
    }

    @Generated
    public int getMods() {
        return this.mods;
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
