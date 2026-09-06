/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 */
package pyrock.events.game;

import lombok.Generated;
import moscow.rockstar.events.Event;
import net.minecraft.entity.Entity;

public class PostAttackEvent
extends Event {
    private final Entity entity;

    public PostAttackEvent(Entity class_12972) {
        this.entity = class_12972;
    }

    @Generated
    public Entity getEntity() {
        return this.entity;
    }
}

