/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.player;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import pyrock.events.EventCancellable;

@ScreenController(description="trace")
public class TraceEvent
extends EventCancellable {
    private float yaw;
    private float pitch;

    @Generated
    public void setYaw(float f) {
        this.yaw = f;
    }

    @Generated
    public void setPitch(float f) {
        this.pitch = f;
    }

    @Generated
    public float getYaw() {
        return this.yaw;
    }

    @Generated
    public float getPitch() {
        return this.pitch;
    }

    @Generated
    public TraceEvent(float f, float f2) {
        this.yaw = f;
        this.pitch = f2;
    }
}

