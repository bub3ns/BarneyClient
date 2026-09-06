/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.VoxelShape
 *  net.minecraft.BlockState
 */
package pyrock.events.game;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import pyrock.events.EventCancellable;

@ScreenController(description="collision_shape")
public class CollisionShapeEvent
extends EventCancellable {
    private final BlockState state;
    private final BlockPos pos;
    private VoxelShape shape;

    @Generated
    public CollisionShapeEvent(BlockState class_26802, BlockPos adminsky, VoxelShape class_2652) {
        this.state = class_26802;
        this.pos = adminsky;
        this.shape = class_2652;
    }

    @Generated
    public BlockState getState() {
        return this.state;
    }

    @Generated
    public BlockPos getPos() {
        return this.pos;
    }

    @Generated
    public VoxelShape getShape() {
        return this.shape;
    }

    @Generated
    public void setShape(VoxelShape class_2652) {
        this.shape = class_2652;
    }
}

