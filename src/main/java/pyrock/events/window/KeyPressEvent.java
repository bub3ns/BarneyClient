/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.window;

import lombok.Generated;
import moscow.rockstar.events.Event;

public class KeyPressEvent
extends Event {
    private final int action;
    private final int key;

    @Generated
    public int getAction() {
        return this.action;
    }

    @Generated
    public int getKey() {
        return this.key;
    }

    @Generated
    public KeyPressEvent(int n, int n2) {
        this.action = n;
        this.key = n2;
    }
}

