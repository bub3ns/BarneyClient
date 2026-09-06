/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 */
package moscow.rockstar.items.rules;

import moscow.rockstar.items.rules.ItemRule;
import net.minecraft.item.ItemStack;

public class OffhandRule
extends ItemRule {
    @Override
    public ItemStack getItemStack() {
        if (OffhandRule.minecraftClient.player == null || OffhandRule.minecraftClient.player.getInventory() == null) {
            return ItemStack.EMPTY;
        }
        return (ItemStack)OffhandRule.minecraftClient.player.getInventory().offHand.getFirst();
    }

    @Override
    public int getClickSlot() {
        return 45;
    }
}

