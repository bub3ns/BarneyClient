/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ItemStack
 *  net.minecraft.NbtCompound
 *  net.minecraft.Text
 *  net.minecraft.NbtComponent
 *  net.minecraft.LoreComponent
 *  net.minecraft.DataComponentTypes
 */
package moscow.rockstar.items.tooltip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import moscow.rockstar.items.DonorItemParser;
import moscow.rockstar.items.ItemMetadataUtils;
import moscow.rockstar.items.config.ItemConfigProcessor;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.DataComponentTypes;

public final class ItemTooltipParser {
    private static final String PUBLIC_BUKKIT_VALUES_KEY = "PublicBukkitValues";
    private static final String DON_ITEM_KEY = "minecraft:don-item";
    private static final String FTID_KEY = "minecraft:ftid";
    private static final long CACHE_TTL_MILLIS = 60000L;
    private static final Map<String, Long> MISMATCH_REPORT_TIMES = new HashMap<String, Long>();
    private static Map<String, String> CUSTOM_ID_ALIASES;
    private static Map<String, String> DISPLAY_NAME_ALIASES;

    private ItemTooltipParser() {
    }

    public static String getCustomItemId(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return null;
        }
        NbtComponent class_92792 = (NbtComponent)class_17992.get(DataComponentTypes.CUSTOM_DATA);
        if (class_92792 == null) {
            return null;
        }
        NbtCompound class_24872 = class_92792.copyNbt();
        if (class_24872.contains(PUBLIC_BUKKIT_VALUES_KEY, 10)) {
            NbtCompound class_24873 = class_24872.getCompound(PUBLIC_BUKKIT_VALUES_KEY);
            if (class_24873.contains(DON_ITEM_KEY, 8)) {
                return class_24873.getString(DON_ITEM_KEY);
            }
            if (class_24873.contains(FTID_KEY, 8)) {
                return class_24873.getString(FTID_KEY);
            }
        }
        if (class_24872.contains(DON_ITEM_KEY, 8)) {
            return class_24872.getString(DON_ITEM_KEY);
        }
        if (class_24872.contains("don-item", 8)) {
            return class_24872.getString("don-item");
        }
        return null;
    }

    public static boolean hasCustomItemId(ItemStack class_17992) {
        return ItemTooltipParser.getCustomItemId(class_17992) != null;
    }

    public static boolean matchesCustomItemId(ItemStack class_17992, String string, ItemStack class_17993) {
        String string2;
        String string3 = string2 = string != null && !string.isBlank() ? string : ItemTooltipParser.resolveDisplayName(class_17992);
        if (string2 == null) {
            return false;
        }
        String string4 = ItemTooltipParser.normalizeIdentifier(ItemTooltipParser.getCustomItemId(class_17992));
        String string5 = ItemTooltipParser.normalizeIdentifier(ItemTooltipParser.getCustomItemId(class_17993));
        if (string4 != null && string4.equals(string5)) {
            return true;
        }
        String string6 = ItemTooltipParser.resolveDisplayName(class_17993);
        if (string6 != null) {
            return string6.equalsIgnoreCase(string2);
        }
        if (ItemTooltipParser.getCustomIdAliases().containsKey(string2)) {
            return false;
        }
        return ItemMetadataUtils.cleanDisplayName(class_17993).equalsIgnoreCase(string2);
    }

    public static String resolveDisplayName(ItemStack class_17992) {
        String string;
        for (String object2 : ItemTooltipParser.getTooltipLines(class_17992)) {
            String string2 = object2.toLowerCase(Locale.ROOT);
            for (Map.Entry<String, String> entry : ItemTooltipParser.getCustomIdAliases().entrySet()) {
                if (!string2.contains(entry.getValue().toLowerCase(Locale.ROOT))) continue;
                return entry.getKey();
            }
        }
        String string3 = ItemTooltipParser.normalizeIdentifier(ItemTooltipParser.getCustomItemId(class_17992));
        if (string3 != null && (string = ItemTooltipParser.getDisplayNameAliases().get(string3)) != null) {
            return string;
        }
        DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        if (donorItem != null && donorItem.getMetadataSource() == DonorItemParser.MetadataSource.LORE_TEXT) {
            return donorItem.getRawName();
        }
        return null;
    }

    public static void reportItemMismatch(String string, ItemStack class_17992) {
        if (string == null || string.isBlank()) {
            return;
        }
        String string2 = ItemMetadataUtils.cleanDisplayName(class_17992);
        if (!string2.toLowerCase(Locale.ROOT).contains(string.toLowerCase(Locale.ROOT))) {
            return;
        }
        long l = System.currentTimeMillis();
        Long l2 = MISMATCH_REPORT_TIMES.get(string);
        if (l2 != null && l - l2 < 60000L) {
            return;
        }
        MISMATCH_REPORT_TIMES.put(string, l);
        String string3 = ItemTooltipParser.resolveDisplayName(class_17992);
        Object object = string3 != null ? "\u044d\u0442\u043e " + string3 : "\u043d\u0435\u0442 \u0441\u0435\u0440\u0432\u0435\u0440\u043d\u043e\u0433\u043e \u043e\u043f\u0438\u0441\u0430\u043d\u0438\u044f";
        Notification.warning(Text.of((String)(string + ": \u043b\u043e\u0442 \u00ab" + string2 + "\u00bb \u043d\u0435 \u0442\u043e\u0442 \u043f\u0440\u0435\u0434\u043c\u0435\u0442 (" + (String)object + ") \u2014 \u043f\u0440\u043e\u043f\u0443\u0441\u043a\u0430\u044e")));
    }

    private static String normalizeIdentifier(String string) {
        return string == null || string.isBlank() ? null : DonorItemParser.normalizeDonorIdentifier(string);
    }

    private static List<String> getTooltipLines(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return List.of();
        }
        LoreComponent class_92902 = (LoreComponent)class_17992.get(DataComponentTypes.LORE);
        if (class_92902 == null) {
            return List.of();
        }
        ArrayList<String> arrayList = new ArrayList<String>(class_92902.lines().size());
        for (Text class_25612 : class_92902.lines()) {
            arrayList.add(class_25612.getString());
        }
        return arrayList;
    }

    private static Map<String, String> getCustomIdAliases() {
        if (CUSTOM_ID_ALIASES == null) {
            CUSTOM_ID_ALIASES = ItemConfigProcessor.getItemDefinitionsById(ItemConfigProcessor.ConfigProfile.FUNTIME);
        }
        return CUSTOM_ID_ALIASES;
    }

    private static Map<String, String> getDisplayNameAliases() {
        if (DISPLAY_NAME_ALIASES != null) {
            return DISPLAY_NAME_ALIASES;
        }
        HashMap<String, String> hashMap = new HashMap<String, String>();
        for (ItemConfigProcessor.CategoryDefinition categoryDefinition : ItemConfigProcessor.getCategoryDefinitions(ItemConfigProcessor.ConfigProfile.FUNTIME)) {
            for (ItemConfigProcessor.ItemDefinition itemDefinition : categoryDefinition.getItems()) {
                String string = ItemTooltipParser.normalizeIdentifier(ItemTooltipParser.getCustomItemId(itemDefinition.getStack()));
                if (string == null || itemDefinition.getCustomName() == null) continue;
                hashMap.put(string, itemDefinition.getCustomName());
            }
        }
        DISPLAY_NAME_ALIASES = hashMap;
        return DISPLAY_NAME_ALIASES;
    }
}
