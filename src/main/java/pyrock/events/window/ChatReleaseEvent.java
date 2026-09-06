/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.window;

import lombok.Generated;
import moscow.rockstar.events.Event;

public class ChatReleaseEvent
extends Event {
    private final float x;
    private final float y;
    private final int button;

    @Generated
    public float getX() {
        return this.x;
    }

    @Generated
    public float getY() {
        return this.y;
    }

    @Generated
    public int getButton() {
        return this.button;
    }

    @Generated
    public ChatReleaseEvent(float f, float f2, int n) {
        this.x = f;
        this.y = f2;
        this.button = n;
    }
}

