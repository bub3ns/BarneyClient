/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Hand
 *  net.minecraft.PlayerEntity
 *  net.minecraft.GenericContainerScreenHandler
 *  net.minecraft.SlotActionType
 *  net.minecraft.Item$TooltipContext
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.TooltipType
 *  net.minecraft.World
 *  net.minecraft.NbtCompound
 *  net.minecraft.NbtList
 *  net.minecraft.NbtElement
 *  net.minecraft.Text
 *  net.minecraft.Packet
 *  net.minecraft.UpdateSelectedSlotC2SPacket
 *  net.minecraft.PlayerInteractItemC2SPacket
 *  net.minecraft.RegistryWrapper$WrapperLookup
 */
package moscow.rockstar.inventory;

import java.util.Locale;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.world.World;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.registry.RegistryWrapper;

public final class ContainerItemCache
implements ClientAccess {
    public static boolean openServerSelector() {
        int n2;
        if (ContainerItemCache.minecraftClient.player == null || ContainerItemCache.minecraftClient.world == null || ContainerItemCache.minecraftClient.interactionManager == null) {
            return false;
        }
        ItemRuleCollection<HotbarSlot> itemRuleCollection = ItemRuleSets.getHotbarRules();
        HotbarSlot hotbarSlot = itemRuleCollection.findByItem(Items.COMPASS);
        if (hotbarSlot == null) {
            return false;
        }
        ContainerItemCache.minecraftClient.player.getInventory().selectedSlot = n2 = hotbarSlot.getSlotIndex();
        ContainerItemCache.minecraftClient.player.networkHandler.sendPacket((Packet)new UpdateSelectedSlotC2SPacket(n2));
        ContainerItemCache.minecraftClient.interactionManager.interactItem(ContainerItemCache.minecraftClient.player, Hand.MAIN_HAND);
        return true;
    }

    public static boolean isServerSelectionScreen(String string) {
        String string2 = ContainerItemCache.normalizeItemText(string);
        return string2.contains("\u0432\u044b\u0431\u043e\u0440 \u0441\u0435\u0440\u0432\u0435\u0440\u0430") || string2.contains("\u0432\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0441\u0435\u0440\u0432\u0435\u0440");
    }

    public static boolean isWorldSelectionScreen(String string, boolean bl) {
        String string2 = ContainerItemCache.normalizeItemText(string);
        if (!string2.contains("\u0432\u044b\u0431\u043e\u0440 \u043c\u0438\u0440\u0430")) {
            return false;
        }
        return !bl || string2.contains("\u0433\u0440\u0438\u0444");
    }

    public static int findWorldEntrySlot(GenericContainerScreenHandler class_17072, boolean bl) {
        int n;
        int n2 = ContainerItemCache.getContainerSlotCount(class_17072);
        int n3 = -1;
        for (n = 0; n < n2; ++n) {
            if (!class_17072.getSlot(n).hasStack()) continue;
            ItemStack class_17992 = class_17072.getSlot(n).getStack();
            String string = ContainerItemCache.normalizeItemText(ContainerItemCache.getItemSearchText(class_17992));
            if (bl) {
                if (class_17992.getItem() == Items.CRAFTING_TABLE && (string.contains("\u0433\u0440\u0438\u0444") || string.contains("\u0432\u044b\u0436\u0438\u0432"))) {
                    return n;
                }
                if (n3 != -1 || !string.contains("\u0433\u0440\u0438\u0444")) continue;
                n3 = n;
                continue;
            }
            if (!string.contains("\u0430\u043d\u0430\u0440\u0445")) continue;
            return n;
        }
        if (bl) {
            for (n = 0; n < n2; ++n) {
                if (!class_17072.getSlot(n).hasStack() || class_17072.getSlot(n).getStack().getItem() != Items.CRAFTING_TABLE) continue;
                return n;
            }
        }
        return n3;
    }

    public static NavigationResult resolveNavigation(GenericContainerScreenHandler class_17072, String string, int n, boolean bl) {
        if (n <= 0) {
            return NavigationResult.NO_ACTION;
        }
        int n2 = ContainerItemCache.findPageSlot(class_17072, n, bl);
        if (n2 != -1) {
            ContainerItemCache.clickContainerSlot(class_17072, n2);
            return NavigationResult.ITEM_SELECTED;
        }
        int n3 = ContainerItemCache.findItemOnPage(class_17072, string, n);
        if (n3 != -1) {
            ContainerItemCache.clickContainerSlot(class_17072, n3);
            return NavigationResult.ITEM_SELECTED;
        }
        int n4 = Integer.MAX_VALUE;
        int n5 = -1;
        int n6 = ContainerItemCache.getContainerSlotCount(class_17072);
        for (int i = 0; i < n6; ++i) {
            int n7;
            if (!class_17072.getSlot(i).hasStack() || (n7 = ContainerItemCache.parsePageNumber(class_17072.getSlot(i).getStack(), bl)) <= 0) continue;
            n4 = Math.min(n4, n7);
            n5 = Math.max(n5, n7);
        }
        if (n5 > 0) {
            if (n < n4 && ContainerItemCache.hasServerPages(string)) {
                return ContainerItemCache.selectNavigationPage(class_17072, false) ? NavigationResult.PAGE_NAVIGATION_NEEDED : NavigationResult.NO_ACTION;
            }
            if (n > n5 && ContainerItemCache.isForwardNavigationAllowed(string)) {
                return ContainerItemCache.selectNavigationPage(class_17072, true) ? NavigationResult.PAGE_NAVIGATION_NEEDED : NavigationResult.NO_ACTION;
            }
            return NavigationResult.NO_ACTION;
        }
        if (ContainerItemCache.isForwardNavigationAllowed(string)) {
            return ContainerItemCache.selectNavigationPage(class_17072, true) ? NavigationResult.PAGE_NAVIGATION_NEEDED : NavigationResult.NO_ACTION;
        }
        return NavigationResult.NO_ACTION;
    }

    public static void clickContainerSlot(GenericContainerScreenHandler class_17072, int n) {
        if (ContainerItemCache.minecraftClient.player == null || ContainerItemCache.minecraftClient.interactionManager == null || n < 0 || n >= class_17072.slots.size()) {
            return;
        }
        ContainerItemCache.minecraftClient.interactionManager.clickSlot(class_17072.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)ContainerItemCache.minecraftClient.player);
        ContainerItemCache.minecraftClient.player.currentScreenHandler.onSlotClick(n, 0, SlotActionType.PICKUP, (PlayerEntity)ContainerItemCache.minecraftClient.player);
    }

    private static int findPageSlot(GenericContainerScreenHandler class_17072, int n, boolean bl) {
        int n2 = ContainerItemCache.getContainerSlotCount(class_17072);
        for (int i = 0; i < n2; ++i) {
            if (!class_17072.getSlot(i).hasStack() || ContainerItemCache.parsePageNumber(class_17072.getSlot(i).getStack(), bl) != n) continue;
            return i;
        }
        return -1;
    }

    private static int findItemOnPage(GenericContainerScreenHandler class_17072, String string, int n) {
        int n2;
        int n3 = ContainerItemCache.getNextPageNumber(string);
        if (n3 <= 0) {
            return -1;
        }
        int n4 = n - (n3 - 1) * 32;
        if (n4 < 1 || n4 > 32) {
            return -1;
        }
        int n5 = (n4 - 1) / 8;
        int n6 = (n4 - 1) % 8;
        if (n6 >= 4) {
            ++n6;
        }
        if ((n2 = n5 * 9 + n6) < 0 || n2 >= ContainerItemCache.getContainerSlotCount(class_17072) || !class_17072.getSlot(n2).hasStack()) {
            return -1;
        }
        if (class_17072.getSlot(n2).getStack().getItem() == Items.ARROW) {
            return -1;
        }
        return n2;
    }

    private static boolean selectNavigationPage(GenericContainerScreenHandler class_17072, boolean bl) {
        int n;
        int n2 = n = bl ? ContainerItemCache.findNextPageSlot(class_17072) : ContainerItemCache.findPreviousPageSlot(class_17072);
        if (n == -1) {
            return false;
        }
        ContainerItemCache.clickContainerSlot(class_17072, n);
        return true;
    }

    private static int findNextPageSlot(GenericContainerScreenHandler class_17072) {
        int n = ContainerItemCache.getContainerSlotCount(class_17072);
        int n2 = -1;
        for (int i = 0; i < n; ++i) {
            ItemStack class_17992;
            if (!class_17072.getSlot(i).hasStack() || (class_17992 = class_17072.getSlot(i).getStack()).getItem() != Items.ARROW && class_17992.getItem() != Items.SPECTRAL_ARROW) continue;
            String string = ContainerItemCache.normalizeItemText(ContainerItemCache.getItemSearchText(class_17992));
            if (string.contains("\u0441\u043b\u0435\u0434") || string.contains("\u0434\u0430\u043b\u044c") || string.contains("next")) {
                return i;
            }
            if (i % 9 < 4) continue;
            n2 = i;
        }
        return n2;
    }

    private static int findPreviousPageSlot(GenericContainerScreenHandler class_17072) {
        int n = ContainerItemCache.getContainerSlotCount(class_17072);
        int n2 = -1;
        for (int i = 0; i < n; ++i) {
            ItemStack class_17992;
            if (!class_17072.getSlot(i).hasStack() || (class_17992 = class_17072.getSlot(i).getStack()).getItem() != Items.ARROW && class_17992.getItem() != Items.SPECTRAL_ARROW) continue;
            String string = ContainerItemCache.normalizeItemText(ContainerItemCache.getItemSearchText(class_17992));
            if (string.contains("\u043f\u0440\u0435\u0434") || string.contains("\u043d\u0430\u0437\u0430\u0434") || string.contains("previous") || string.contains("back")) {
                return i;
            }
            if (i % 9 > 4 || n2 != -1) continue;
            n2 = i;
        }
        return n2;
    }

    private static int parsePageNumber(ItemStack class_17992, boolean bl) {
        String string = ContainerItemCache.normalizeItemText(ContainerItemCache.getItemSearchText(class_17992));
        int n = ContainerItemCache.parseNumberForward(string, string.indexOf(35) + 1);
        if (n > 0 && string.indexOf(35) >= 0) {
            return n;
        }
        String string2 = bl ? "\u0433\u0440\u0438\u0444" : "\u0430\u043d\u0430\u0440\u0445";
        int n2 = string.indexOf(string2);
        if (n2 >= 0 && (n = ContainerItemCache.parseNumberForward(string, n2 + string2.length())) > 0) {
            return n;
        }
        String string3 = bl ? "grief" : "anarchy";
        n2 = string.indexOf(string3);
        if (n2 >= 0 && (n = ContainerItemCache.parseNumberForward(string, n2 + string3.length())) > 0) {
            return n;
        }
        return -1;
    }

    private static boolean isForwardNavigationAllowed(String string) {
        int n = ContainerItemCache.getNextPageNumber(string);
        int n2 = ContainerItemCache.getPreviousPageNumber(string);
        return n <= 0 || n2 <= 0 || n < n2;
    }

    private static boolean hasServerPages(String string) {
        return ContainerItemCache.getNextPageNumber(string) > 1;
    }

    private static int getNextPageNumber(String string) {
        String string2 = ContainerItemCache.normalizeItemText(string);
        int n = string2.indexOf(47);
        if (n == -1) {
            return -1;
        }
        return ContainerItemCache.parseNumberBackward(string2, n);
    }

    private static int getPreviousPageNumber(String string) {
        String string2 = ContainerItemCache.normalizeItemText(string);
        int n = string2.indexOf(47);
        if (n == -1) {
            return -1;
        }
        return ContainerItemCache.parseNumberForward(string2, n + 1);
    }

    private static int getContainerSlotCount(GenericContainerScreenHandler class_17072) {
        return Math.max(0, class_17072.slots.size() - 36);
    }

    private static String getItemSearchText(ItemStack class_17992) {
        StringBuilder stringBuilder = new StringBuilder(class_17992.getName().getString());
        try {
            if (ContainerItemCache.minecraftClient.world == null) {
                return stringBuilder.toString();
            }
            for (Text tooltipLine : class_17992.getTooltip(Item.TooltipContext.create((World)ContainerItemCache.minecraftClient.world), (PlayerEntity)ContainerItemCache.minecraftClient.player, (TooltipType)TooltipType.BASIC)) {
                stringBuilder.append(' ').append(tooltipLine.getString());
            }
            NbtElement class_25202 = class_17992.toNbt((RegistryWrapper.WrapperLookup)ContainerItemCache.minecraftClient.world.getRegistryManager());
            if (!(class_25202 instanceof NbtCompound itemNbt) || !itemNbt.contains("components", 10)) {
                return stringBuilder.toString();
            }
            NbtCompound class_24872 = itemNbt.getCompound("components");
            ContainerItemCache.appendComponentText(stringBuilder, class_24872, "minecraft:custom_name");
            ContainerItemCache.appendComponentText(stringBuilder, class_24872, "minecraft:item_name");
            if (class_24872.contains("minecraft:lore", 9)) {
                NbtList class_24992 = class_24872.getList("minecraft:lore", 8);
                for (int i = 0; i < class_24992.size(); ++i) {
                    stringBuilder.append(' ').append(class_24992.getString(i));
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return stringBuilder.toString();
    }

    private static void appendComponentText(StringBuilder stringBuilder, NbtCompound class_24872, String string) {
        if (class_24872.contains(string, 8)) {
            stringBuilder.append(' ').append(class_24872.getString(string));
        }
    }

    private static int parseNumberForward(String string, int n) {
        if (n < 0) {
            return -1;
        }
        for (int i = n; i < string.length(); ++i) {
            int n2;
            if (!Character.isDigit(string.charAt(i))) continue;
            for (n2 = i + 1; n2 < string.length() && Character.isDigit(string.charAt(n2)); ++n2) {
            }
            return ContainerItemCache.parsePageNumber(string.substring(i, n2));
        }
        return -1;
    }

    private static int parseNumberBackward(String string, int n) {
        int n2;
        for (n2 = n - 1; n2 >= 0 && !Character.isDigit(string.charAt(n2)); --n2) {
        }
        if (n2 < 0) {
            return -1;
        }
        int n3 = n2 + 1;
        while (n2 >= 0 && Character.isDigit(string.charAt(n2))) {
            --n2;
        }
        return ContainerItemCache.parsePageNumber(string.substring(n2 + 1, n3));
    }

    private static int parsePageNumber(String string) {
        try {
            return Integer.parseInt(string);
        }
        catch (NumberFormatException numberFormatException) {
            return -1;
        }
    }

    private static String normalizeItemText(String string) {
        if (string == null) {
            return "";
        }
        return string.replaceAll("\u00a7.", "").replace('\u0451', '\u0435').replace('\u0401', '\u0415').toLowerCase(Locale.ROOT);
    }

    @Generated
    private ContainerItemCache() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static enum NavigationResult {
        NO_ACTION,
        PAGE_NAVIGATION_NEEDED,
        ITEM_SELECTED;
}
}

