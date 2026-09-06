/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package pyrock.events.game;

import lombok.Generated;
import moscow.rockstar.api.annotations.ScreenController;
import net.minecraft.util.math.BlockPos;
import pyrock.events.EventCancellable;

@ScreenController(description="block_break")
public class BlockBreakEvent
extends EventCancellable {
    private final BlockPos blockPos;

    public BlockBreakEvent(BlockPos adminsky) {
        this.blockPos = adminsky;
    }

    @Generated
    public BlockPos getBlockPos() {
        return this.blockPos;
    }
}

