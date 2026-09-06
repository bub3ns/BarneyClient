/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.ItemStack
 */
package moscow.rockstar.items.rules;

import lombok.Generated;
import moscow.rockstar.items.rules.ItemRule;
import net.minecraft.item.ItemStack;

public class InventorySlotRule
extends ItemRule {
    private final int inventorySlot;

    public InventorySlotRule(int n) {
        if (n < 0 || n > 26) {
            throw new IllegalArgumentException("Inventory Slot ID must be between 0 and 26");
        }
        this.inventorySlot = n;
    }

    @Override
    public ItemStack getItemStack() {
        if (InventorySlotRule.minecraftClient.player == null || InventorySlotRule.minecraftClient.player.getInventory() == null) {
            return ItemStack.EMPTY;
        }
        return InventorySlotRule.minecraftClient.player.getInventory().getStack(this.inventorySlot + 9);
    }

    @Override
    public int getClickSlot() {
        return this.inventorySlot + 9;
    }

    @Generated
    public int getInventorySlot() {
        return this.inventorySlot;
    }
}

