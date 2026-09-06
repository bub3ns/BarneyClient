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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class BlockCollisionProbe
implements CollisionProbe {
    private final int x;
    private final int y;
    private final int z;

    public BlockCollisionProbe(BlockPos adminsky) {
        this(adminsky.getX(), adminsky.getY(), adminsky.getZ());
    }

    public BlockCollisionProbe(int n, int n2, int n3) {
        this.x = n;
        this.y = n2;
        this.z = n3;
    }

    @Override
    public boolean matchesCoordinates(int n, int n2, int n3) {
        return this.x == n && this.y == n2 && this.z == n3;
    }

    @Override
    public double distanceSquaredTo(int n, int n2, int n3) {
        double d = this.x - n;
        double d2 = this.y - n2;
        double d3 = this.z - n3;
        double d4 = Math.abs(d);
        double d5 = Math.abs(d3);
        double d6 = Math.min(d4, d5);
        double d7 = Math.abs(d4 - d5);
        return d6 * 1.41421356 + d7 + Math.abs(d2);
    }

    @Override
    public Vec3d toBlockCenter() {
        return new Vec3d((double)this.x + 0.5, (double)this.y, (double)this.z + 0.5);
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "x", "y", "z");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "x", "y", "z");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "x", "y", "z");
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }
}

