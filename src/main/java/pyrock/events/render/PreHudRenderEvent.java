/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.render;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.events.Event;
import pyrock.utility.render.CustomDrawContext;

@ScreenController(description="pre_render_2d")
public class PreHudRenderEvent
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
    public PreHudRenderEvent(CustomDrawContext customDrawContext, float f) {
        this.context = customDrawContext;
        this.tickDelta = f;
    }
}

