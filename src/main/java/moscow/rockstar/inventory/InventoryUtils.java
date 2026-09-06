/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.PlayerEntity
 *  net.minecraft.SlotActionType
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Packet
 *  net.minecraft.CloseHandledScreenC2SPacket
 *  net.minecraft.UpdateSelectedSlotC2SPacket
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.inventory;

import java.util.function.Predicate;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.items.rules.ArmorSlotRule;
import moscow.rockstar.items.rules.HotbarSlotRules;
import moscow.rockstar.items.rules.InventorySlotRule;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.OffhandRule;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import org.jetbrains.annotations.NotNull;

public final class InventoryUtils
implements ClientAccess {
    public static HotbarSlot hotbarSlot(int n) {
        return new HotbarSlot(n);
    }

    public static InventorySlotRule inventorySlot(int n) {
        return new InventorySlotRule(n);
    }

    public static ArmorSlotRule armorSlot(int n) {
        return new ArmorSlotRule(n);
    }

    public static ArmorSlotRule helmetRule() {
        return InventoryUtils.armorSlot(3);
    }

    public static ArmorSlotRule chestplateRule() {
        return InventoryUtils.armorSlot(2);
    }

    public static ArmorSlotRule leggingsRule() {
        return InventoryUtils.armorSlot(1);
    }

    public static ArmorSlotRule bootsRule() {
        return InventoryUtils.armorSlot(0);
    }

    public static OffhandRule offhandRule() {
        return new OffhandRule();
    }

    public static boolean isHelmetItem(Item class_17922) {
        return InventoryUtils.offhandRule().matchesItem(class_17922);
    }

    public static boolean isHelmetStack(Predicate<ItemStack> predicate) {
        return InventoryUtils.offhandRule().matchesStack(predicate);
    }

    public static boolean isHelmetEmpty() {
        return InventoryUtils.offhandRule().isEmpty();
    }

    public static void swapItemRules(ItemRule itemRule, ItemRule itemRule2) {
        if (minecraftClient.getNetworkHandler() == null) {
            return;
        }
        itemRule.clickSelectedSlot();
        itemRule2.clickSelectedSlot();
        if (!itemRule2.isEmpty()) {
            itemRule.clickSelectedSlot();
        }
        minecraftClient.getNetworkHandler().sendPacket((Packet)new CloseHandledScreenC2SPacket(0));
    }

    public static void quickMoveItem(int n) {
        if (minecraftClient.getNetworkHandler() == null) {
            return;
        }
        InventoryUtils.minecraftClient.interactionManager.clickSlot(InventoryUtils.minecraftClient.player.currentScreenHandler.syncId, n, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)InventoryUtils.minecraftClient.player);
    }

    public static void swapSlots(int n, int n2) {
        InventoryUtils.swapSlots(n, n2, false);
    }

    public static void swapSlots(int n, int n2, boolean bl) {
        if (minecraftClient.getNetworkHandler() == null) {
            return;
        }
        InventoryUtils.minecraftClient.interactionManager.clickSlot(InventoryUtils.minecraftClient.player.currentScreenHandler.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryUtils.minecraftClient.player);
        InventoryUtils.minecraftClient.interactionManager.clickSlot(InventoryUtils.minecraftClient.player.currentScreenHandler.syncId, n2, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryUtils.minecraftClient.player);
        if (bl) {
            InventoryUtils.minecraftClient.interactionManager.clickSlot(InventoryUtils.minecraftClient.player.currentScreenHandler.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryUtils.minecraftClient.player);
        }
    }

    public static void swapMenuSlots(int n, int n2) {
        InventoryUtils.minecraftClient.interactionManager.clickSlot(InventoryUtils.minecraftClient.player.playerScreenHandler.syncId, InventoryUtils.toContainerSlot(n), 0, SlotActionType.PICKUP, (PlayerEntity)InventoryUtils.minecraftClient.player);
        InventoryUtils.minecraftClient.interactionManager.clickSlot(InventoryUtils.minecraftClient.player.playerScreenHandler.syncId, InventoryUtils.toContainerSlot(n2), 0, SlotActionType.PICKUP, (PlayerEntity)InventoryUtils.minecraftClient.player);
        InventoryUtils.minecraftClient.interactionManager.clickSlot(InventoryUtils.minecraftClient.player.playerScreenHandler.syncId, InventoryUtils.toContainerSlot(n), 0, SlotActionType.PICKUP, (PlayerEntity)InventoryUtils.minecraftClient.player);
    }

    public static void swapWithPickup(int n, int n2) {
        if (minecraftClient.getNetworkHandler() == null) {
            return;
        }
        InventoryUtils.minecraftClient.interactionManager.clickSlot(InventoryUtils.minecraftClient.player.currentScreenHandler.syncId, n, 1, SlotActionType.PICKUP, (PlayerEntity)InventoryUtils.minecraftClient.player);
        InventoryUtils.minecraftClient.interactionManager.clickSlot(InventoryUtils.minecraftClient.player.currentScreenHandler.syncId, n2, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryUtils.minecraftClient.player);
    }

    public static void swapWithPutback(int n, int n2) {
        if (minecraftClient.getNetworkHandler() == null) {
            return;
        }
        InventoryUtils.minecraftClient.interactionManager.clickSlot(InventoryUtils.minecraftClient.player.currentScreenHandler.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryUtils.minecraftClient.player);
        InventoryUtils.minecraftClient.interactionManager.clickSlot(InventoryUtils.minecraftClient.player.currentScreenHandler.syncId, n2, 1, SlotActionType.PICKUP, (PlayerEntity)InventoryUtils.minecraftClient.player);
        InventoryUtils.minecraftClient.interactionManager.clickSlot(InventoryUtils.minecraftClient.player.currentScreenHandler.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryUtils.minecraftClient.player);
    }

    public static void dropItem(int n, int n2) {
        if (minecraftClient.getNetworkHandler() == null) {
            return;
        }
        InventoryUtils.minecraftClient.interactionManager.clickSlot(InventoryUtils.minecraftClient.player.currentScreenHandler.syncId, n, n2, SlotActionType.SWAP, (PlayerEntity)InventoryUtils.minecraftClient.player);
    }

    public static boolean moveItemToHotbar(ItemRule itemRule, int n) {
        HotbarSlot hotbarSlot = InventoryUtils.hotbarSlot(n);
        InventoryUtils.swapItemRules(itemRule, hotbarSlot);
        return true;
    }

    public static boolean moveItemToArmor(ItemRule itemRule, int n) {
        ArmorSlotRule armorSlotRule = InventoryUtils.armorSlot(n);
        InventoryUtils.swapItemRules(itemRule, armorSlotRule);
        return true;
    }

    public static void moveItemToOffhand(ItemRule itemRule) {
        OffhandRule offhandRule = InventoryUtils.offhandRule();
        InventoryUtils.swapItemRules(itemRule, offhandRule);
    }

    @NotNull
    public static HotbarSlot getSelectedHotbarSlot() {
        if (InventoryUtils.minecraftClient.player == null || InventoryUtils.minecraftClient.player.getInventory() == null) {
            return new HotbarSlot(0);
        }
        return InventoryUtils.hotbarSlot(InventoryUtils.minecraftClient.player.getInventory().selectedSlot);
    }

    public static void setSelectedHotbarSlot(int n) {
        if (InventoryUtils.minecraftClient.player == null || InventoryUtils.minecraftClient.player.getInventory() == null || minecraftClient.getNetworkHandler() == null || InventoryUtils.getSelectedHotbarSlot().getSlotIndex() == n) {
            return;
        }
        if (n < 0 || n > 8) {
            throw new IllegalArgumentException("Hotbar slot ID must be between 0 and 8");
        }
        InventoryUtils.minecraftClient.player.getInventory().selectedSlot = n;
    }

    public static void syncSelectedHotbarSlot(int n) {
        if (InventoryUtils.minecraftClient.player == null || InventoryUtils.minecraftClient.player.getInventory() == null || minecraftClient.getNetworkHandler() == null || InventoryUtils.getSelectedHotbarSlot().getSlotIndex() == n) {
            return;
        }
        if (n < 0 || n > 8) {
            throw new IllegalArgumentException("Hotbar slot ID must be between 0 and 8");
        }
        minecraftClient.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(InventoryUtils.minecraftClient.player.getInventory().selectedSlot));
    }

    public static void setSelectedHotbarSlot(HotbarSlot hotbarSlot) {
        InventoryUtils.setSelectedHotbarSlot(hotbarSlot.getSlotIndex());
    }

    public static boolean selectHotbarItem(Item class_17922) {
        HotbarSlot hotbarSlot = (HotbarSlot)new HotbarSlotRules().findByItem(class_17922);
        if (hotbarSlot != null) {
            InventoryUtils.setSelectedHotbarSlot(hotbarSlot);
            return true;
        }
        return false;
    }

    public static int findInventorySlot(Predicate<ItemStack> predicate) {
        if (InventoryUtils.minecraftClient.player == null || InventoryUtils.minecraftClient.player.currentScreenHandler == null) {
            return -1;
        }
        for (int i = 0; i < InventoryUtils.minecraftClient.player.currentScreenHandler.slots.size(); ++i) {
            ItemStack class_17992 = InventoryUtils.minecraftClient.player.currentScreenHandler.getSlot(i).getStack();
            if (!predicate.test(class_17992)) continue;
            return i;
        }
        return -1;
    }

    public static int findItemSlot(Item class_17922) {
        return InventoryUtils.findInventorySlot(class_17992 -> class_17992.getItem() == class_17922);
    }

    private static int toContainerSlot(int n) {
        if (n >= 0 && n <= 8) {
            return 36 + n;
        }
        return n;
    }

    @Generated
    private InventoryUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

