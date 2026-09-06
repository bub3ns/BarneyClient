/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.PlayerEntity
 *  net.minecraft.SlotActionType
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 */
package moscow.rockstar.items.rules;

import java.util.function.Predicate;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.inventory.InventoryUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class ItemRule
implements ClientAccess {
    public abstract ItemStack getItemStack();

    public abstract int getClickSlot();

    public int getScreenHandlerSyncId() {
        if (ItemRule.minecraftClient.player == null || ItemRule.minecraftClient.player.currentScreenHandler == null) {
            return 0;
        }
        return ItemRule.minecraftClient.player.currentScreenHandler.syncId;
    }

    public Item getItem() {
        return this.getItemStack().getItem();
    }

    public boolean isEmpty() {
        return this.getItemStack().isEmpty();
    }

    public boolean matchesItem(Item class_17922) {
        return this.getItemStack().getItem() == class_17922;
    }

    public boolean matchesStack(Predicate<ItemStack> predicate) {
        return predicate.test(this.getItemStack());
    }

    public void transferTo(ItemRule itemRule) {
        InventoryUtils.swapItemRules(this, itemRule);
    }

    public void click() {
        InventoryUtils.moveItemToOffhand(this);
    }

    public void clickSelectedSlot() {
        if (ItemRule.minecraftClient.interactionManager == null) {
            return;
        }
        ItemRule.minecraftClient.interactionManager.clickSlot(this.getScreenHandlerSyncId(), this.getClickSlot(), 0, SlotActionType.PICKUP, (PlayerEntity)ItemRule.minecraftClient.player);
    }
}
