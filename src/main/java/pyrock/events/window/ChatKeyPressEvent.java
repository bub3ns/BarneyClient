/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.window;

import lombok.Generated;
import moscow.rockstar.events.Event;

public class ChatKeyPressEvent
extends Event {
    private final int keyCode;
    private final int scanCode;
    private final int modifiers;

    @Generated
    public ChatKeyPressEvent(int n, int n2, int n3) {
        this.keyCode = n;
        this.scanCode = n2;
        this.modifiers = n3;
    }

    @Generated
    public int getKeyCode() {
        return this.keyCode;
    }

    @Generated
    public int getScanCode() {
        return this.scanCode;
    }

    @Generated
    public int getModifiers() {
        return this.modifiers;
    }
}

