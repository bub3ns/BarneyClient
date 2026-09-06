/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.game;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import pyrock.events.EventCancellable;

@ScreenController(description="rotate_camera")
public class RotateCameraEvent
extends EventCancellable {
    private float deltaYaw;
    private float deltaPitch;

    @Generated
    public void setDeltaYaw(float f) {
        this.deltaYaw = f;
    }

    @Generated
    public void setDeltaPitch(float f) {
        this.deltaPitch = f;
    }

    @Generated
    public float getDeltaYaw() {
        return this.deltaYaw;
    }

    @Generated
    public float getDeltaPitch() {
        return this.deltaPitch;
    }

    @Generated
    public RotateCameraEvent(float f, float f2) {
        this.deltaYaw = f;
        this.deltaPitch = f2;
    }
}

