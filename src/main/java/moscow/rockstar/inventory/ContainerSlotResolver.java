/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.PlayerEntity
 *  net.minecraft.GenericContainerScreenHandler
 *  net.minecraft.SlotActionType
 *  net.minecraft.Item$TooltipContext
 *  net.minecraft.ItemStack
 *  net.minecraft.TooltipType
 *  net.minecraft.World
 *  net.minecraft.NbtCompound
 *  net.minecraft.NbtList
 *  net.minecraft.NbtElement
 *  net.minecraft.Text
 *  net.minecraft.RegistryWrapper$WrapperLookup
 */
package moscow.rockstar.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.server.PartySize;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.world.World;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryWrapper;

public final class ContainerSlotResolver
implements ClientAccess {
    public static void clickContainerSlot(GenericContainerScreenHandler class_17072, int n) {
        if (ContainerSlotResolver.minecraftClient.interactionManager == null || n < 0 || n >= class_17072.slots.size()) {
            return;
        }
        ContainerSlotResolver.minecraftClient.interactionManager.clickSlot(class_17072.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)ContainerSlotResolver.minecraftClient.player);
        ContainerSlotResolver.minecraftClient.player.currentScreenHandler.onSlotClick(n, 0, SlotActionType.PICKUP, (PlayerEntity)ContainerSlotResolver.minecraftClient.player);
    }

    public static int getPartyMemberSlot(int n) {
        return switch (n) {
            case 1 -> 20;
            case 2 -> 21;
            case 3 -> 22;
            case 4 -> 29;
            case 5 -> 30;
            case 6 -> 31;
            case 7 -> 38;
            case 8 -> 39;
            case 9 -> 40;
            default -> -1;
        };
    }

    public static int getPartyOptionSlot(int n) {
        return switch (n) {
            case 1 -> 11;
            case 2 -> 12;
            case 3 -> 13;
            default -> 10;
        };
    }

    public static int findLightItemSlot(GenericContainerScreenHandler class_17072, String string) {
        String string2 = ContainerSlotResolver.normalizeItemText(string);
        if (string2.isBlank()) {
            return -1;
        }
        for (int i = 0; i < class_17072.slots.size(); ++i) {
            String string3;
            String string4;
            if (!class_17072.getSlot(i).hasStack() || !(string4 = ContainerSlotResolver.normalizeItemText(string3 = ContainerSlotResolver.getItemSearchText(class_17072.getSlot(i).getStack()))).contains(string2) || !string4.contains("\u043b\u0430\u0439\u0442") || string4.contains("#")) continue;
            return i;
        }
        return -1;
    }

    public static List<Integer> findPartyOptionSlots(GenericContainerScreenHandler class_17072) {
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        for (int i = 0; i < class_17072.slots.size(); ++i) {
            String string;
            if (!class_17072.getSlot(i).hasStack() || (string = ContainerSlotResolver.normalizeItemText(ContainerSlotResolver.getItemSearchText(class_17072.getSlot(i).getStack()))).contains("#") || !string.contains("\u043b\u0430\u0439\u0442") || !string.contains("\u0441\u043e\u043b\u043e") && !string.contains("\u0434\u0443\u043e") && !string.contains("\u0442\u0440\u0438\u043e") && !string.contains("\u043a\u043b\u0430\u043d")) continue;
            arrayList.add(i);
        }
        return arrayList;
    }

    public static int getPartySizeIndex(PartySize partySize) {
        return switch (partySize) {
            case PartySize.SOLO -> 0;
            case PartySize.DUO -> 1;
            case PartySize.TRIO -> 2;
            case PartySize.CLAN -> 3;
            default -> -1;
        };
    }

    public static int findNumberedLightItem(GenericContainerScreenHandler class_17072, String string, int n) {
        if (n <= 0) {
            return -1;
        }
        String string2 = ContainerSlotResolver.normalizeItemText(string);
        for (int i = 0; i < class_17072.slots.size(); ++i) {
            String string3;
            if (!class_17072.getSlot(i).hasStack() || !ContainerSlotResolver.containsExactNumber(string3 = ContainerSlotResolver.normalizeItemText(ContainerSlotResolver.getItemSearchText(class_17072.getSlot(i).getStack())), n) || !string3.contains("\u043b\u0430\u0439\u0442") || !string2.isBlank() && !string3.contains(string2)) continue;
            return i;
        }
        return -1;
    }

    public static int getPartyMemberSlot(PartySize partySize, int n) {
        int firstSlot = switch (partySize) {
            case SOLO -> 1;
            case DUO -> 13;
            case TRIO -> 27;
            case CLAN -> 40;
            default -> -1;
        };
        if (firstSlot < 0 || ContainerSlotResolver.getPartySizeForSlot(n) != partySize) {
            return -1;
        }
        return 18 + (n - firstSlot);
    }

    public static PartySize getPartySizeForSlot(int n) {
        if (n >= 1 && n <= 12) {
            return PartySize.SOLO;
        }
        if (n >= 13 && n <= 26) {
            return PartySize.DUO;
        }
        if (n >= 27 && n <= 39) {
            return PartySize.TRIO;
        }
        if (n >= 40) {
            return PartySize.CLAN;
        }
        return PartySize.UNKNOWN;
    }

    private static boolean containsExactNumber(String string, int n) {
        String string2 = "#" + n;
        int n2 = 0;
        while (n2 < string.length()) {
            int n3 = string.indexOf(string2, n2);
            if (n3 < 0) {
                return false;
            }
            int n4 = n3 + string2.length();
            if (n4 == string.length() || !Character.isDigit(string.charAt(n4))) {
                return true;
            }
            n2 = n4;
        }
        return false;
    }

    private static String getItemSearchText(ItemStack class_17992) {
        StringBuilder stringBuilder = new StringBuilder(class_17992.getName().getString());
        try {
            if (ContainerSlotResolver.minecraftClient.world == null) {
                return stringBuilder.toString();
            }
            for (Text tooltipLine : class_17992.getTooltip(Item.TooltipContext.create((World)ContainerSlotResolver.minecraftClient.world), (PlayerEntity)ContainerSlotResolver.minecraftClient.player, (TooltipType)TooltipType.BASIC)) {
                stringBuilder.append(' ').append(tooltipLine.getString());
            }
            NbtElement class_25202 = class_17992.toNbt((RegistryWrapper.WrapperLookup)ContainerSlotResolver.minecraftClient.world.getRegistryManager());
            if (!(class_25202 instanceof NbtCompound itemNbt) || !itemNbt.contains("components", 10)) {
                return stringBuilder.toString();
            }
            NbtCompound class_24872 = itemNbt.getCompound("components");
            ContainerSlotResolver.appendComponentText(stringBuilder, class_24872, "minecraft:custom_name");
            ContainerSlotResolver.appendComponentText(stringBuilder, class_24872, "minecraft:item_name");
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

    private static String normalizeItemText(String string) {
        if (string == null) {
            return "";
        }
        return string.replaceAll("\u00a7.", "").replace('\u0451', '\u0435').replace('\u0401', '\u0415').toLowerCase(Locale.ROOT);
    }

    @Generated
    private ContainerSlotResolver() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
