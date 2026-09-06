/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.LivingEntity
 *  net.minecraft.FireworkRocketEntity
 *  net.minecraft.Vec3d
 */
package pyrock.events.game;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.events.Event;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;

@ScreenController(description="firework")
public class FireworkEvent
extends Event {
    private final LivingEntity entity;
    private Vec3d velocity;
    private final FireworkRocketEntity rocketEntity;

    @Generated
    public LivingEntity getEntity() {
        return this.entity;
    }

    @Generated
    public Vec3d getVelocity() {
        return this.velocity;
    }

    @Generated
    public FireworkRocketEntity getRocketEntity() {
        return this.rocketEntity;
    }

    @Generated
    public void setVelocity(Vec3d VanillaChestLootTableGenerator) {
        this.velocity = VanillaChestLootTableGenerator;
    }

    @Generated
    public FireworkEvent(LivingEntity class_13092, Vec3d VanillaChestLootTableGenerator, FireworkRocketEntity class_16712) {
        this.entity = class_13092;
        this.velocity = VanillaChestLootTableGenerator;
        this.rocketEntity = class_16712;
    }
}

