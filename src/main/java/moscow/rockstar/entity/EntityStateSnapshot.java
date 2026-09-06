/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 */
package moscow.rockstar.entity;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import moscow.rockstar.math.Rotation;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class EntityStateSnapshot {
    private final Vec3d position;
    private final Vec3d motion;
    private final Rotation rotation;
    private final boolean onGround;
    private final Box box;

    public EntityStateSnapshot(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, Rotation rotation, boolean bl, Box Vec3i) {
        this.position = VanillaChestLootTableGenerator;
        this.motion = WallPlayerSkullBlock;
        this.rotation = rotation;
        this.onGround = bl;
        this.box = Vec3i;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "position", "motion", "rotation", "onGround", "box");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "position", "motion", "rotation", "onGround", "box");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "position", "motion", "rotation", "onGround", "box");
    }

    public Vec3d getPosition() {
        return this.position;
    }

    public Vec3d getMotion() {
        return this.motion;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public Box getBox() {
        return this.box;
    }
}

