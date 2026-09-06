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

@ScreenController(description="char_typed")
public class CharTypedEvent
extends EventCancellable
implements ClientAccess {
    private final int codePoint;
    private final int mods;

    public CharTypedEvent(int n, int n2) {
        this.codePoint = n;
        this.mods = n2;
    }

    public String getChar() {
        return new String(Character.toChars(this.codePoint));
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
        return CharTypedEvent.minecraftClient.currentScreen != null;
    }

    @Generated
    public int getCodePoint() {
        return this.codePoint;
    }

    @Generated
    public int getMods() {
        return this.mods;
    }
}

