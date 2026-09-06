/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.ItemStack
 */
package moscow.rockstar.inventory;

import lombok.Generated;
import moscow.rockstar.items.rules.ItemRule;
import net.minecraft.item.ItemStack;

public class HotbarSlot
extends ItemRule {
    private final int slotIndex;

    public HotbarSlot(int n) {
        if (n < 0 || n > 8) {
            throw new IllegalArgumentException("Hotbar Slot ID must be between 0 and 8");
        }
        this.slotIndex = n;
    }

    @Override
    public ItemStack getItemStack() {
        if (HotbarSlot.minecraftClient.player == null || HotbarSlot.minecraftClient.player.getInventory() == null) {
            return ItemStack.EMPTY;
        }
        return HotbarSlot.minecraftClient.player.getInventory().getStack(this.slotIndex);
    }

    @Override
    public int getClickSlot() {
        return 36 + this.slotIndex;
    }

    @Generated
    public int getSlotIndex() {
        return this.slotIndex;
    }
}

