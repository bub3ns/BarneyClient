/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec3d
 */
package moscow.rockstar.world;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import moscow.rockstar.entity.CollisionProbe;
import net.minecraft.util.math.Vec3d;

public final class BlockOffset
implements CollisionProbe {
    private final int x;
    private final int z;

    public BlockOffset(int n, int n2) {
        this.x = n;
        this.z = n2;
    }

    @Override
    public boolean matchesCoordinates(int n, int n2, int n3) {
        return this.x == n && this.z == n3;
    }

    @Override
    public double distanceSquaredTo(int n, int n2, int n3) {
        double d = this.x - n;
        double d2 = this.z - n3;
        double d3 = Math.abs(d);
        double d4 = Math.abs(d2);
        double d5 = Math.min(d3, d4);
        double d6 = Math.abs(d3 - d4);
        return d5 * 1.41421356 + d6;
    }

    @Override
    public Vec3d toBlockCenter() {
        return new Vec3d((double)this.x + 0.5, 64.0, (double)this.z + 0.5);
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "x", "z");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "x", "z");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "x", "z");
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }
}

