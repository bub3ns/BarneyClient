/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.world;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.util.math.BlockPos;

public final class BlockPositionOffset {
    private final int x;
    private final int y;
    private final int z;

    public BlockPositionOffset(int n, int n2, int n3) {
        this.x = n;
        this.y = n2;
        this.z = n3;
    }

    public BlockPos toBlockPosition() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public static BlockPositionOffset fromBlockPosition(BlockPos adminsky) {
        return new BlockPositionOffset(adminsky.getX(), adminsky.getY(), adminsky.getZ());
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

