/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.window;

import lombok.Generated;
import moscow.rockstar.events.Event;

public class MouseEvent
extends Event {
    private final int button;
    private final int action;

    @Generated
    public int getButton() {
        return this.button;
    }

    @Generated
    public int getAction() {
        return this.action;
    }

    @Generated
    public MouseEvent(int n, int n2) {
        this.button = n;
        this.action = n2;
    }
}

