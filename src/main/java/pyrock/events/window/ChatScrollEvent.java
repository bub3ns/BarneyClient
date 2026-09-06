/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.window;

import lombok.Generated;
import moscow.rockstar.events.Event;

public class ChatScrollEvent
extends Event {
    private final float x;
    private final float y;
    private final float horizontal;
    private final float vertical;

    @Generated
    public float getX() {
        return this.x;
    }

    @Generated
    public float getY() {
        return this.y;
    }

    @Generated
    public float getHorizontal() {
        return this.horizontal;
    }

    @Generated
    public float getVertical() {
        return this.vertical;
    }

    @Generated
    public ChatScrollEvent(float f, float f2, float f3, float f4) {
        this.x = f;
        this.y = f2;
        this.horizontal = f3;
        this.vertical = f4;
    }
}

