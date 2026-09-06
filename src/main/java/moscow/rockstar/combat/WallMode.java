/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.BlockView
 *  net.minecraft.DoorBlock
 *  net.minecraft.TrapdoorBlock
 *  net.minecraft.BlockState
 *  net.minecraft.RaycastContext$ShapeType
 */
package moscow.rockstar.combat;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.world.RaycastContext;

public enum WallMode {
    NONE,
    ALL,
    DOORS_AND_TRAPDOORS,
    NON_SOLID_BLOCKS,
    REACHABLE_WALLS;

    public boolean shouldIgnoreBlock(BlockView class_19222, BlockPos adminsky, BlockState class_26802) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> false;
            case 2 -> {
                if (class_26802.getBlock() instanceof DoorBlock || class_26802.getBlock() instanceof TrapdoorBlock) {
                    yield true;
                }
                yield false;
            }
            case 3 -> {
                if (!class_26802.isFullCube(class_19222, adminsky)) {
                    yield true;
                }
                yield false;
            }
            case 1, 4 -> true;
        };
    }

    public RaycastContext.ShapeType getRaycastShape() {
        return this == DOORS_AND_TRAPDOORS ? RaycastContext.ShapeType.OUTLINE : RaycastContext.ShapeType.COLLIDER;
    }

    public boolean usesDirectRaycast() {
        return this == ALL || this == REACHABLE_WALLS;
    }
}

