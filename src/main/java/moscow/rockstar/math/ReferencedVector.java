/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Vec3i
 *  net.minecraft.Vec3d
 *  org.joml.Vector3f
 */
package moscow.rockstar.math;

import lombok.Generated;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class ReferencedVector
extends Vec3d {
    private final Vec3d referencePosition;

    public ReferencedVector(double d, double d2, double d3, Vec3d VanillaChestLootTableGenerator) {
        super(d, d2, d3);
        this.referencePosition = VanillaChestLootTableGenerator;
    }

    public ReferencedVector(Vector3f vector3f, Vec3d VanillaChestLootTableGenerator) {
        super(vector3f);
        this.referencePosition = VanillaChestLootTableGenerator;
    }

    public ReferencedVector(Vec3i class_23822, Vec3d VanillaChestLootTableGenerator) {
        super(class_23822);
        this.referencePosition = VanillaChestLootTableGenerator;
    }

    @Generated
    public Vec3d getReferencePosition() {
        return this.referencePosition;
    }
}

