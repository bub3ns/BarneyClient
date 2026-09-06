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

@ScreenController(description="hud_render")
public class HudLayerRenderEvent
extends Event {
    private final CustomDrawContext context;
    private final float tickDelta;
    private final int count;
    private final boolean editing;

    @Generated
    public CustomDrawContext getContext() {
        return this.context;
    }

    @Generated
    public float getTickDelta() {
        return this.tickDelta;
    }

    @Generated
    public int getCount() {
        return this.count;
    }

    @Generated
    public boolean isEditing() {
        return this.editing;
    }

    @Generated
    public HudLayerRenderEvent(CustomDrawContext customDrawContext, float f, int n, boolean bl) {
        this.context = customDrawContext;
        this.tickDelta = f;
        this.count = n;
        this.editing = bl;
    }
}

