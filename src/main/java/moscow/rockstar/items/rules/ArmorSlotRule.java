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

public class ArmorSlotRule
extends ItemRule {
    private final int armorSlot;

    public ArmorSlotRule(int n) {
        if (n < 0 || n > 3) {
            throw new IllegalArgumentException("Armor Slot Index must be between 0 and 3");
        }
        this.armorSlot = n;
    }

    @Override
    public ItemStack getItemStack() {
        if (ArmorSlotRule.minecraftClient.player == null || ArmorSlotRule.minecraftClient.player.getInventory() == null) {
            return ItemStack.EMPTY;
        }
        return ArmorSlotRule.minecraftClient.player.getInventory().getArmorStack(this.armorSlot);
    }

    @Override
    public int getClickSlot() {
        return 8 - this.armorSlot;
    }

    @Generated
    public int getArmorSlot() {
        return this.armorSlot;
    }
}

