/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.render;

import lombok.Generated;
import moscow.rockstar.events.Event;
import pyrock.utility.render.CustomDrawContext;

public class ScreenRenderEvent
extends Event {
    private final CustomDrawContext context;
    private final float tickDelta;

    @Generated
    public CustomDrawContext getContext() {
        return this.context;
    }

    @Generated
    public float getTickDelta() {
        return this.tickDelta;
    }

    @Generated
    public ScreenRenderEvent(CustomDrawContext customDrawContext, float f) {
        this.context = customDrawContext;
        this.tickDelta = f;
    }
}

