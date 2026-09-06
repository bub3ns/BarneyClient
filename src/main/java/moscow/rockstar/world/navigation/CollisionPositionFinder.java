/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.world.navigation;

import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.world.BlockPositionOffset;
import moscow.rockstar.world.navigation.BlockNavigationFactory;
import org.jetbrains.annotations.Nullable;

public final class CollisionPositionFinder {
    private CollisionPositionFinder() {
    }

    public static BlockPositionOffset findPlayerStandingPosition(MinecraftClient client) {
        BlockNavigationFactory blockNavigationFactory;
        if (client.player == null) {
            return new BlockPositionOffset(0, 0, 0);
        }
        BlockPos adminsky = client.player.getBlockPos();
        int n = adminsky.getX();
        int n2 = adminsky.getY();
        int n3 = adminsky.getZ();
        try {
            blockNavigationFactory = new BlockNavigationFactory();
        }
        catch (IllegalStateException illegalStateException) {
            return new BlockPositionOffset(n, n2, n3);
        }
        if (client.player.isTouchingWater() && blockNavigationFactory.isOpenSpace(n, n2, n3)) {
            return new BlockPositionOffset(n, n2, n3);
        }
        if (client.player.isClimbing() && !blockNavigationFactory.isWalkable(n, n2, n3)) {
            return new BlockPositionOffset(n, n2, n3);
        }
        if (blockNavigationFactory.hasSolidSupport(n, n2 - 1, n3)
            && blockNavigationFactory.isOpenSpace(n, n2, n3)
            && blockNavigationFactory.isOpenSpace(n, n2 + 1, n3)) {
            return new BlockPositionOffset(n, n2 + 1, n3);
        }
        if (blockNavigationFactory.isWalkable(n, n2, n3)) {
            return new BlockPositionOffset(n, n2, n3);
        }
        BlockPositionOffset blockPositionOffset = CollisionPositionFinder.findBestOverlappingBlock(blockNavigationFactory, client, n2);
        if (blockPositionOffset != null) {
            return blockPositionOffset;
        }
        BlockPositionOffset blockPositionOffset2 = CollisionPositionFinder.findBestOverlappingBlock(blockNavigationFactory, client, n2 + 1);
        return blockPositionOffset2 != null ? blockPositionOffset2 : new BlockPositionOffset(n, n2, n3);
    }

    @Nullable
    public static BlockPositionOffset findAlternateStandingPosition(MinecraftClient client) {
        BlockNavigationFactory blockNavigationFactory;
        if (client.player == null) {
            return null;
        }
        BlockPos adminsky = client.player.getBlockPos();
        int n = adminsky.getX();
        int n2 = adminsky.getY();
        int n3 = adminsky.getZ();
        try {
            blockNavigationFactory = new BlockNavigationFactory();
        }
        catch (IllegalStateException illegalStateException) {
            return null;
        }
        if (blockNavigationFactory.hasSolidSupport(n, n2 - 1, n3)
            && blockNavigationFactory.isOpenSpace(n, n2, n3)
            && blockNavigationFactory.isOpenSpace(n, n2 + 1, n3)) {
            return null;
        }
        if (blockNavigationFactory.isWalkable(n, n2, n3)) {
            return null;
        }
        BlockPositionOffset blockPositionOffset = CollisionPositionFinder.findBestOverlappingBlock(blockNavigationFactory, client, n2);
        return blockPositionOffset != null ? blockPositionOffset : CollisionPositionFinder.findBestOverlappingBlock(blockNavigationFactory, client, n2 + 1);
    }

    @Nullable
    private static BlockPositionOffset findBestOverlappingBlock(BlockNavigationFactory blockNavigationFactory, MinecraftClient client, int n) {
        double d = client.player.getX();
        double d2 = client.player.getZ();
        double d3 = (double)client.player.getWidth() * 0.5;
        int n2 = (int)Math.floor(d - d3);
        int n3 = (int)Math.floor(d + d3);
        int n4 = (int)Math.floor(d2 - d3);
        int n5 = (int)Math.floor(d2 + d3);
        BlockPositionOffset blockPositionOffset = null;
        double d4 = 0.0;
        for (int i = n2; i <= n3; ++i) {
            for (int j = n4; j <= n5; ++j) {
                double d5;
                double d6;
                double d7;
                if (!blockNavigationFactory.isWalkable(i, n, j)
                    || !((d7 = (d6 = Math.min((double)(i + 1), d + d3) - Math.max((double)i, d - d3))
                    * (d5 = Math.min((double)(j + 1), d2 + d3) - Math.max((double)j, d2 - d3))) > d4)) continue;
                d4 = d7;
                blockPositionOffset = new BlockPositionOffset(i, n, j);
            }
        }
        return blockPositionOffset;
    }
}
