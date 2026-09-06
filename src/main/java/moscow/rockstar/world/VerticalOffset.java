/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec3d
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.world;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import moscow.rockstar.entity.CollisionProbe;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class VerticalOffset
implements CollisionProbe {
    private final int y;

    public VerticalOffset(int n) {
        this.y = n;
    }

    @Override
    public boolean matchesCoordinates(int n, int n2, int n3) {
        return this.y == n2;
    }

    @Override
    public double distanceSquaredTo(int n, int n2, int n3) {
        return Math.abs(this.y - n2);
    }

    @Override
    @Nullable
    public Vec3d toBlockCenter() {
        return null;
    }

    @Override
    public final String toString() {
        return moscow.rockstar.util.RecordValueSupport.toString(this, "y");
    }

    @Override
    public final int hashCode() {
        return moscow.rockstar.util.RecordValueSupport.hashCode(this, "y");
    }

    @Override
    public final boolean equals(Object object) {
        return moscow.rockstar.util.RecordValueSupport.equals(this, object, "y");
    }

    public int getY() {
        return this.y;
    }
}

