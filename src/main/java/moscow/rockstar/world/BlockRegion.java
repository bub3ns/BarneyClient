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

public final class BlockRegion
implements CollisionProbe {
    private final int x;
    private final int y;
    private final int z;
    private final int radius;

    public BlockRegion(BlockPos adminsky, int n) {
        this(adminsky.getX(), adminsky.getY(), adminsky.getZ(), n);
    }

    public BlockRegion(int n, int n2, int n3, int n4) {
        this.x = n;
        this.y = n2;
        this.z = n3;
        this.radius = n4;
    }

    @Override
    public boolean matchesCoordinates(int n, int n2, int n3) {
        long l = this.x - n;
        long l2 = this.y - n2;
        long l3 = this.z - n3;
        return l * l + l2 * l2 + l3 * l3 <= (long)this.radius * (long)this.radius;
    }

    @Override
    public double distanceSquaredTo(int n, int n2, int n3) {
        double d = this.x - n;
        double d2 = this.y - n2;
        double d3 = this.z - n3;
        double d4 = Math.sqrt(d * d + d2 * d2 + d3 * d3);
        return Math.max(0.0, d4 - (double)this.radius);
    }

    @Override
    public Vec3d toBlockCenter() {
        return new Vec3d((double)this.x + 0.5, (double)this.y, (double)this.z + 0.5);
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "x", "y", "z", "radius");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "x", "y", "z", "radius");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "x", "y", "z", "radius");
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

    public int getRadius() {
        return this.radius;
    }
}

