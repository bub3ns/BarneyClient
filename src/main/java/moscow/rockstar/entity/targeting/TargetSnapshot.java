/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.Vec3d
 */
package moscow.rockstar.entity.targeting;

import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class TargetSnapshot {
    Entity entity;
    boolean visible;
    boolean targetReady;
    Vec3d position;

    @Generated
    public Entity getEntity() {
        return this.entity;
    }

    @Generated
    public boolean isVisible() {
        return this.visible;
    }

    @Generated
    public boolean isTargetReady() {
        return this.targetReady;
    }

    @Generated
    public Vec3d getPosition() {
        return this.position;
    }

    @Generated
    public TargetSnapshot(Entity class_12972, boolean bl, boolean bl2, Vec3d VanillaChestLootTableGenerator) {
        this.entity = class_12972;
        this.visible = bl;
        this.targetReady = bl2;
        this.position = VanillaChestLootTableGenerator;
    }
}

