/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec3d
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.entity;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public interface CollisionProbe {
    public boolean matchesCoordinates(int var1, int var2, int var3);

    public double distanceSquaredTo(int var1, int var2, int var3);

    @Nullable
    public Vec3d toBlockCenter();
}

