/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.window;

import lombok.Generated;
import moscow.rockstar.events.Event;

public class ChatTypeEvent
extends Event {
    private final char text;
    private final int modifiers;

    @Generated
    public char getText() {
        return this.text;
    }

    @Generated
    public int getModifiers() {
        return this.modifiers;
    }

    @Generated
    public ChatTypeEvent(char c, int n) {
        this.text = c;
        this.modifiers = n;
    }
}

