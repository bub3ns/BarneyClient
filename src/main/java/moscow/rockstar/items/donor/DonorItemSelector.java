/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 */
package moscow.rockstar.items.donor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.items.DonorItemParser;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class DonorItemSelector
implements ClientAccess {
    private static final Map<String, Integer> SPHERE_NAME_PRIORITY = Map.of("\u0410\u0440\u0435\u0441\u0430", 1, "\u0425\u0430\u043e\u0441\u0430", 2, "\u0422\u0438\u0442\u0430\u043d\u0430", 3, "\u0413\u0438\u0434\u0440\u044b", 4, "\u0411\u0435\u0441\u0442\u0438\u0438", 5, "\u0418\u043a\u0430\u0440\u0430", 6, "\u0421\u0430\u0442\u0438\u0440\u0430", 7, "\u042d\u0440\u0438\u0434\u0430", 8, "\u041c\u043e\u0440\u043e\u0437\u0430", 9);
    private static final Map<String, Integer> TALISMAN_NAME_PRIORITY = Map.of("\u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", 1, "\u041a\u0430\u0440\u0430\u0442\u0435\u043b\u044f", 2, "\u042f\u0440\u043e\u0441\u0442\u0438", 3, "\u0420\u0430\u0437\u0434\u043e\u0440\u0430", 4, "\u0422\u0438\u0440\u0430\u043d\u0430", 5, "\u0414\u0435\u043c\u043e\u043d\u0430", 6, "\u0412\u0438\u0445\u0440\u044f", 7, "\u041c\u0440\u0430\u043a\u0430", 8, "\u0413\u0440\u0438\u043d\u0447\u0430", 9);
    private static final Map<String, Integer> SPECIAL_TALISMAN_PRIORITY = Map.of("Infinity", 1, "Eternity", 2, "Stinger", 3);
    private static final Map<String, Integer> SPECIAL_SPHERE_PRIORITY = Map.of("Armortality", 3, "Eternity", 4, "Immortal", 5, "Stinger", 6, "Flash", 7, "Cerber", 8);
    private static final Map<String, Integer> LEGACY_TALISMAN_PRIORITY = Map.of("\u041a\u0440\u0443\u0448\u0438\u0442\u0435\u043b\u044f", 1, "\u041a\u0430\u0440\u0430\u0442\u0435\u043b\u044f", 2, "\u042f\u0440\u043e\u0441\u0442\u0438", 3, "\u0414\u0435\u0434\u0430\u043b\u0430", 4, "\u0413\u0440\u0430\u043d\u0438", 5, "\u0413\u0430\u0440\u043c\u043e\u043d\u0438\u0438", 6, "\u0415\u0445\u0438\u0434\u043d\u044b", 7, "\u0422\u0440\u0438\u0442\u043e\u043d\u0430", 8, "\u0424\u0435\u043d\u0438\u043a\u0441\u0430", 9);
    private static final Map<String, Integer> LEGACY_SPHERE_PRIORITY = Map.of("\u0410\u043d\u0434\u0440\u043e\u043c\u0435\u0434\u044b", 1, "\u0422\u0438\u0442\u0430\u043d\u0430", 2, "\u0410\u043f\u043e\u043b\u043b\u043e\u043d\u0430", 3, "\u0410\u0441\u0442\u0440\u0435\u044f", 4, "\u041e\u0441\u0438\u0440\u0438\u0441\u0430", 5, "\u041f\u0430\u043d\u0434\u043e\u0440\u044b", 6, "\u0425\u0438\u043c\u0435\u0440\u044b", 7);
    private static final Map<String, Integer> ATTRIBUTE_TALISMAN_PRIORITY = Map.of("attribute-item-tkryshitela", 1, "attribute-item-tkaratela", 2, "attribute-item-tjarosti", 3, "attribute-item-trazdora", 4, "attribute-item-ttirana", 5, "attribute-item-tdemona", 6, "attribute-item-tvihra", 7, "attribute-item-tmraka", 8, "attribute-item-tgrincha", 9);
    private static final Map<String, Integer> ATTRIBUTE_SPHERE_PRIORITY = Map.of("attribute-item-saresa", 1, "attribute-item-shaosa", 2, "attribute-item-stitana", 3, "attribute-item-sgidri", 4, "attribute-item-sbestii", 5, "attribute-item-sikara", 6, "attribute-item-ssatira", 7, "attribute-item-serida", 8, "attribute-item-smoroza", 9);
    private static final int UNKNOWN_PRIORITY = 999;

    public static int getItemPriority(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty() || class_17992.getItem() != Items.TOTEM_OF_UNDYING) {
            return Integer.MAX_VALUE;
        }
        DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        if (donorItem == null) {
            return class_17992.hasEnchantments() ? 1 : 0;
        }
        if (donorItem.isTalismanCategory()) {
            String string = donorItem.getRawName();
            if (string == null || string.isEmpty()) {
                return class_17992.hasEnchantments() ? 1 : 0;
            }
            int n = DonorItemSelector.getDonorPriority(donorItem);
            if (n == 999) {
                return 1000;
            }
            return 100 - n;
        }
        return 50;
    }

    public static int getMetadataPriority(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return 999;
        }
        DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
        return donorItem != null ? DonorItemSelector.getDonorPriority(donorItem) : 999;
    }

    public static int getDonorPriority(DonorItemParser.DonorItem donorItem) {
        if (donorItem == null) {
            return 999;
        }
        return switch (donorItem.getMetadataSource()) {
            case DonorItemParser.MetadataSource.LORE_TEXT -> DonorItemSelector.getLorePriority(donorItem);
            case DonorItemParser.MetadataSource.LEGACY_TAG -> DonorItemSelector.getLegacyTagPriority(donorItem);
            case DonorItemParser.MetadataSource.STRUCTURED_DATA, DonorItemParser.MetadataSource.DONOR_TAG -> DonorItemSelector.getSpecialMetadataPriority(donorItem);
            case DonorItemParser.MetadataSource.SPOOKY_DATA -> DonorItemSelector.getAttributeTagPriority(donorItem);
            default -> 999;
        };
    }

    private static int getSpecialMetadataPriority(DonorItemParser.DonorItem donorItem) {
        String string = donorItem.getRawName();
        if (donorItem.isTalismanCategory()) {
            if (string != null && SPECIAL_TALISMAN_PRIORITY.containsKey(string)) {
                return SPECIAL_TALISMAN_PRIORITY.get(string);
            }
            DonorItemParser.Rarity rarity = donorItem.getRarity();
            if (rarity != null) {
                return switch (rarity) {
                    case DonorItemParser.Rarity.MYTHICAL -> 4;
                    case DonorItemParser.Rarity.LEGENDARY -> 5;
                    case DonorItemParser.Rarity.EPIC -> 6;
                    case DonorItemParser.Rarity.NORMAL -> 7;
                    default -> 999;
                };
            }
            return 999;
        }
        if (donorItem.isSphereCategory()) {
            if (string != null && SPECIAL_SPHERE_PRIORITY.containsKey(string)) {
                return SPECIAL_SPHERE_PRIORITY.get(string);
            }
            int n = donorItem.getMaximumBonus();
            return n > 0 ? 10 - n : 999;
        }
        return 999;
    }

    private static int getLorePriority(DonorItemParser.DonorItem donorItem) {
        String string = donorItem.getRawName();
        if (string == null) {
            return 999;
        }
        if (donorItem.isSphereCategory()) {
            return SPHERE_NAME_PRIORITY.getOrDefault(string, 999);
        }
        if (donorItem.isTalismanCategory()) {
            return TALISMAN_NAME_PRIORITY.getOrDefault(string, 999);
        }
        return 999;
    }

    private static int getLegacyTagPriority(DonorItemParser.DonorItem donorItem) {
        String string = donorItem.getRawName();
        if (string == null) {
            return 999;
        }
        if (donorItem.isSphereCategory()) {
            return LEGACY_SPHERE_PRIORITY.getOrDefault(string, 999);
        }
        if (donorItem.isTalismanCategory()) {
            return LEGACY_TALISMAN_PRIORITY.getOrDefault(string, 999);
        }
        return 999;
    }

    private static int getAttributeTagPriority(DonorItemParser.DonorItem donorItem) {
        String string = donorItem.getRawName();
        if (string == null) {
            return 999;
        }
        if (donorItem.isSphereCategory()) {
            return ATTRIBUTE_SPHERE_PRIORITY.getOrDefault(string, 999);
        }
        if (donorItem.isTalismanCategory()) {
            return ATTRIBUTE_TALISMAN_PRIORITY.getOrDefault(string, 999);
        }
        return 999;
    }

    private static int getFallbackPriority(DonorItemParser.DonorItem donorItem) {
        if (donorItem == null) {
            return 999;
        }
        if ("Cerber".equals(donorItem.getRawName())) {
            return 200;
        }
        int n = donorItem.getArmorBonus();
        int n2 = donorItem.getDamageBonus();
        if (n >= 3) {
            return 10 - n;
        }
        if (n2 >= 3) {
            return 20 - n2;
        }
        if (donorItem.hasKnownDonorName()) {
            return 30 + SPECIAL_SPHERE_PRIORITY.getOrDefault(donorItem.getRawName(), 999);
        }
        if (n == 2) {
            return 40 - n;
        }
        return 50 - n2;
    }

    public static ItemStack selectPreferredSphere(List<ItemStack> list) {
        return DonorItemSelector.selectMatchingItem(list, DonorItemParser.DonorItem::isSphereCategory, Comparator.comparingInt(DonorItemSelector::getMetadataPriority));
    }

    public static ItemStack selectHighestPrioritySphere(List<ItemStack> list) {
        return DonorItemSelector.selectMaximumPriorityItem(list, DonorItemParser.DonorItem::isSphereCategory, Comparator.comparingInt(DonorItemSelector::getMetadataPriority));
    }

    public static ItemStack selectPreferredTalisman(List<ItemStack> list) {
        return DonorItemSelector.selectMatchingItem(list, DonorItemParser.DonorItem::isTalismanCategory, Comparator.comparingInt(DonorItemSelector::getMetadataPriority));
    }

    public static ItemStack selectHighestPriorityTalisman(List<ItemStack> list) {
        return DonorItemSelector.selectMaximumPriorityItem(list, DonorItemParser.DonorItem::isTalismanCategory, Comparator.comparingInt(DonorItemSelector::getMetadataPriority));
    }

    public static ItemStack selectKnownTalisman(List<ItemStack> list) {
        boolean bl = list.stream().anyMatch(class_17992 -> class_17992.getItem() == Items.TOTEM_OF_UNDYING && DonorItemParser.parseDonorItem(class_17992) == null);
        return bl ? null : DonorItemSelector.selectHighestPriorityTalisman(list);
    }

    public static ItemStack selectFastSphere(List<ItemStack> list) {
        return DonorItemSelector.selectMatchingItem(list, donorItem -> donorItem.isSphereCategory() && donorItem.getSpeedBonus() >= 3, Comparator.comparingInt(class_17992 -> 10 - DonorItemParser.parseDonorItem(class_17992).getSpeedBonus()));
    }

    public static ItemStack selectFallbackSphere(List<ItemStack> list) {
        return DonorItemSelector.selectMatchingItem(list, donorItem -> donorItem.isSphereCategory() && donorItem.getSpeedBonus() < 3, Comparator.comparingInt(class_17992 -> DonorItemSelector.getFallbackPriority(DonorItemParser.parseDonorItem(class_17992))));
    }

    public static ItemStack selectAlternateSphere(List<ItemStack> list) {
        return DonorItemSelector.selectMatchingItemExcluding(list, donorItem -> donorItem.isSphereCategory() && donorItem.getSpeedBonus() < 3, Comparator.comparingInt(class_17992 -> DonorItemSelector.getFallbackPriority(DonorItemParser.parseDonorItem(class_17992))), DonorItemSelector.selectFallbackSphere(list));
    }

    private static ItemStack selectMatchingItem(List<ItemStack> list, Predicate<DonorItemParser.DonorItem> predicate, Comparator<ItemStack> comparator) {
        return DonorItemSelector.selectMatchingItemExcluding(list, predicate, comparator, null);
    }

    private static ItemStack selectMatchingItemExcluding(List<ItemStack> list, Predicate<DonorItemParser.DonorItem> predicate, Comparator<ItemStack> comparator, ItemStack class_17992) {
        return list.stream().filter(class_17993 -> {
            DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(class_17993);
            return donorItem != null && predicate.test(donorItem) && (class_17992 == null || !ItemStack.areEqual((ItemStack)class_17993, (ItemStack)class_17992));
        }).min(comparator).orElse(null);
    }

    private static ItemStack selectMaximumPriorityItem(List<ItemStack> list, Predicate<DonorItemParser.DonorItem> predicate, Comparator<ItemStack> comparator) {
        return list.stream().filter(class_17992 -> {
            DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
            return donorItem != null && predicate.test(donorItem);
        }).max(comparator).orElse(null);
    }

    @Generated
    private DonorItemSelector() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

