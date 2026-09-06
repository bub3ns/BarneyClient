/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Hand
 *  net.minecraft.ItemStack
 *  net.minecraft.Direction
 */
package pyrock.events.game;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import moscow.rockstar.events.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

@ScreenController(description="block_place")
public class BlockPlaceEvent
extends Event {
    private final BlockPos placePos;
    private final BlockPos hitPos;
    private final Direction side;
    private final Hand hand;
    private final ItemStack stack;

    @Generated
    public BlockPos getPlacePos() {
        return this.placePos;
    }

    @Generated
    public BlockPos getHitPos() {
        return this.hitPos;
    }

    @Generated
    public Direction getSide() {
        return this.side;
    }

    @Generated
    public Hand getHand() {
        return this.hand;
    }

    @Generated
    public ItemStack getStack() {
        return this.stack;
    }

    @Generated
    public BlockPlaceEvent(BlockPos adminsky, BlockPos adminsky2, Direction class_23502, Hand class_12682, ItemStack class_17992) {
        this.placePos = adminsky;
        this.hitPos = adminsky2;
        this.side = class_23502;
        this.hand = class_12682;
        this.stack = class_17992;
    }
}

