/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.window;

import lombok.Generated;
import moscow.rockstar.events.Event;

public class MouseScrollEvent
extends Event {
    private final double verticalAmount;

    @Generated
    public double getVerticalAmount() {
        return this.verticalAmount;
    }

    @Generated
    public MouseScrollEvent(double d) {
        this.verticalAmount = d;
    }
}

