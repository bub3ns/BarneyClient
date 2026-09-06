/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.DamageSource
 *  net.minecraft.LivingEntity
 *  org.jetbrains.annotations.Nullable
 */
package pyrock.events.game;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.events.Event;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

@ScreenController(description="entity_death")
public class EntityDeathEvent
extends Event {
    private final LivingEntity entity;
    private final DamageSource source;

    public EntityDeathEvent(LivingEntity class_13092, DamageSource class_12822) {
        this.entity = class_13092;
        this.source = class_12822;
    }

    public EntityDeathEvent(LivingEntity class_13092) {
        this.entity = class_13092;
        this.source = null;
    }

    @Nullable
    public LivingEntity getKillerEntity() {
        return this.entity.getPrimeAdversary();
    }

    @Generated
    public LivingEntity getEntity() {
        return this.entity;
    }

    @Generated
    public DamageSource getSource() {
        return this.source;
    }
}

