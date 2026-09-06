/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 *  net.minecraft.BlockView
 *  net.minecraft.BlockState
 */
package moscow.rockstar.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.world.BlockDropResolver;
import net.minecraft.item.ItemStack;
import net.minecraft.world.BlockView;
import net.minecraft.block.BlockState;

public final class BlockItemValidator {
    public static final int MAX_BREAK_TIME_TICKS = 200;

    private BlockItemValidator() {
    }

    public static int estimateBreakTime(BlockState class_26802, BlockPos adminsky) {
        boolean bl;
        float f;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            return Integer.MAX_VALUE;
        }
        if (class_26802.isAir()) {
            return 0;
        }
        float f2 = class_26802.getHardness((BlockView)client.world, adminsky);
        if (f2 < 0.0f) {
            return Integer.MAX_VALUE;
        }
        if (f2 == 0.0f) {
            return 1;
        }
        BlockDropResolver.ToolSlotSelection toolSlotSelection = BlockDropResolver.selectToolSlot(class_26802);
        ItemStack class_17992 = toolSlotSelection != null ? client.player.getInventory().getStack(toolSlotSelection.getSlotIndex()) : ItemStack.EMPTY;
        float f3 = BlockDropResolver.getMiningSpeed(class_17992, class_26802);
        float f4 = f3 / f2 / (f = (bl = BlockItemValidator.requiresCorrectTool(class_17992, class_26802)) ? 30.0f : 100.0f);
        if (f4 <= 0.0f) {
            return Integer.MAX_VALUE;
        }
        int n = (int)Math.ceil(1.0 / (double)f4);
        return n;
    }

    public static int estimateBreakTime(BlockState class_26802) {
        return BlockItemValidator.estimateBreakTime(class_26802, BlockPos.ORIGIN);
    }

    public static boolean requiresCorrectTool(ItemStack class_17992, BlockState class_26802) {
        if (!class_26802.isToolRequired()) {
            return true;
        }
        return class_17992.isSuitableFor(class_26802);
    }
}

