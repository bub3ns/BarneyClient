/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 */
package pyrock.events.game;

import lombok.Generated;
import net.minecraft.entity.Entity;
import pyrock.events.EventCancellable;

public class AttackEvent
extends EventCancellable {
    private final Entity entity;

    @Generated
    public Entity getEntity() {
        return this.entity;
    }

    @Generated
    public AttackEvent(Entity class_12972) {
        this.entity = class_12972;
    }
}

