/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.Camera
 */
package pyrock.events.render;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.events.Event;
import net.minecraft.entity.Entity;
import net.minecraft.client.render.Camera;

@ScreenController(description="camera_update")
public class CameraUpdateEvent
extends Event {
    private final Camera camera;
    private final Entity focusedEntity;
    private final boolean thirdPerson;
    private final boolean inverseView;
    private final float tickDelta;

    @Generated
    public Camera getCamera() {
        return this.camera;
    }

    @Generated
    public Entity getFocusedEntity() {
        return this.focusedEntity;
    }

    @Generated
    public boolean isThirdPerson() {
        return this.thirdPerson;
    }

    @Generated
    public boolean isInverseView() {
        return this.inverseView;
    }

    @Generated
    public float getTickDelta() {
        return this.tickDelta;
    }

    @Generated
    public CameraUpdateEvent(Camera class_41842, Entity class_12972, boolean bl, boolean bl2, float f) {
        this.camera = class_41842;
        this.focusedEntity = class_12972;
        this.thirdPerson = bl;
        this.inverseView = bl2;
        this.tickDelta = f;
    }
}

