/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.LivingEntity
 */
package pyrock.events.game;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import net.minecraft.entity.LivingEntity;
import pyrock.events.EventCancellable;

@ScreenController(description="entity_jump")
public class EntityJumpEvent
extends EventCancellable {
    private final LivingEntity entity;

    @Generated
    public LivingEntity getEntity() {
        return this.entity;
    }

    @Generated
    public EntityJumpEvent(LivingEntity class_13092) {
        this.entity = class_13092;
    }
}

