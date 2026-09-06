/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  lombok.Generated
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Enchantment
 *  net.minecraft.Text
 *  net.minecraft.RegistryKey
 *  net.minecraft.RegistryEntry
 *  net.minecraft.ItemEnchantmentsComponent
 *  net.minecraft.DataComponentTypes
 */
package moscow.rockstar.items;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.DataComponentTypes;

public final class EnchantmentUtils {
    public static void collectEnchantments(ItemStack class_17992, Object2IntMap<RegistryEntry<Enchantment>> object2IntMap) {
        object2IntMap.clear();
        if (!class_17992.isEmpty()) {
            Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> entries = class_17992.getItem() == Items.ENCHANTED_BOOK
                ? class_17992.get(DataComponentTypes.STORED_ENCHANTMENTS).getEnchantmentEntries()
                : class_17992.getEnchantments().getEnchantmentEntries();
            for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : entries) {
                object2IntMap.put(entry.getKey(), entry.getIntValue());
            }
        }
    }

    @SafeVarargs
    public static boolean hasEnchantments(ItemStack class_17992, RegistryKey<Enchantment> ... class_5321Array) {
        if (class_17992.isEmpty()) {
            return false;
        }
        Object2IntArrayMap object2IntArrayMap = new Object2IntArrayMap();
        EnchantmentUtils.collectEnchantments(class_17992, (Object2IntMap<RegistryEntry<Enchantment>>)object2IntArrayMap);
        for (RegistryKey<Enchantment> class_53212 : class_5321Array) {
            if (EnchantmentUtils.hasEnchantment((Object2IntMap<RegistryEntry<Enchantment>>)object2IntArrayMap, class_53212)) continue;
            return false;
        }
        return true;
    }

    public static boolean containsEnchantmentText(List<Text> list, String string) {
        for (Text class_25612 : list) {
            String string2 = class_25612.getString().toLowerCase();
            if (!string2.contains(string.toLowerCase())) continue;
            return true;
        }
        return false;
    }

    public static boolean containsEnchantmentLevel(List<Text> list, String string, int n) {
        for (Text class_25612 : list) {
            String string2 = class_25612.getString().toLowerCase();
            if (!string2.contains(string.toLowerCase())) continue;
            if (n <= 1) {
                return true;
            }
            if (n == 2 && (string2.contains("ii") || string2.contains(" 2"))) {
                return true;
            }
            if (n == 3 && (string2.contains("iii") || string2.contains(" 3"))) {
                return true;
            }
            if (n == 4 && (string2.contains("iv") || string2.contains(" 4"))) {
                return true;
            }
            if (n == 5 && (string2.contains("v") || string2.contains(" 5"))) {
                return true;
            }
            String string3 = String.valueOf(n);
            if (!string2.contains(" " + string3) && !string2.contains(string3 + " ")) continue;
            return true;
        }
        return false;
    }

    public static int getEnchantmentLevel(ItemStack class_17992, RegistryKey<Enchantment> class_53212) {
        if (class_17992.isEmpty()) {
            return 0;
        }
        Object2IntArrayMap object2IntArrayMap = new Object2IntArrayMap();
        EnchantmentUtils.collectEnchantments(class_17992, (Object2IntMap<RegistryEntry<Enchantment>>)object2IntArrayMap);
        return EnchantmentUtils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)object2IntArrayMap, class_53212);
    }

    public static int getEnchantmentLevel(Object2IntMap<RegistryEntry<Enchantment>> object2IntMap, RegistryKey<Enchantment> class_53212) {
        for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable(object2IntMap)) {
            if (!((RegistryEntry)entry.getKey()).matchesKey(class_53212)) continue;
            return entry.getIntValue();
        }
        return 0;
    }

    private static boolean hasEnchantment(Object2IntMap<RegistryEntry<Enchantment>> object2IntMap, RegistryKey<Enchantment> class_53212) {
        for (RegistryEntry<Enchantment> class_68802 : object2IntMap.keySet()) {
            if (!class_68802.matchesKey(class_53212)) continue;
            return true;
        }
        return false;
    }

    @Generated
    private EnchantmentUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
