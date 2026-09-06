/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  lombok.Generated
 *  net.minecraft.PlayerInventory
 *  net.minecraft.ItemStack
 *  net.minecraft.PotionItem
 *  net.minecraft.Enchantment
 *  net.minecraft.RegistryKey
 *  net.minecraft.RegistryEntry
 */
package moscow.rockstar.items.catalog;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Generated;
import moscow.rockstar.items.DonorItemParser;
import moscow.rockstar.items.EnchantmentUtils;
import moscow.rockstar.items.ItemMetadataUtils;
import moscow.rockstar.items.recipes.RecipeItemResolver;
import moscow.rockstar.items.tooltip.ItemTooltipParser;
import moscow.rockstar.market.MarketInventoryAnalyzer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public final class ItemCatalog {
    private static final List<CatalogEntry> entries = new ArrayList<CatalogEntry>();
    private static final Map<String, Integer> purchaseCounts = new HashMap<String, Integer>();

    public static void registerEntry(ItemStack class_17992, long l, String string, String string2, PriceMode priceMode, double d) {
        ItemCatalog.registerConfiguredEntry(class_17992, l, string, string2, priceMode, d, false, 0L, 1, 10);
    }

    public static void registerEntryWithStrictEnchants(ItemStack class_17992, long l, String string, String string2, PriceMode priceMode, double d, boolean bl) {
        ItemCatalog.registerConfiguredEntry(class_17992, l, string, string2, priceMode, d, bl, 0L, 1, 10);
    }

    public static void registerConfiguredEntry(ItemStack class_17992, long l, String string, String string2, PriceMode priceMode, double d, boolean bl, long l2, int n, int n2) {
        entries.removeIf(catalogEntry -> catalogEntry.getId() != null && catalogEntry.getId().equals(string2));
        entries.add(new CatalogEntry(class_17992.copy(), l, string, string2, priceMode, d, bl, Math.max(0L, l2), Math.max(1, n), Math.max(1, n2)));
    }

    public static List<CatalogEntry> getEntries() {
        return List.copyOf(entries);
    }

    public static void clearEntries() {
        entries.clear();
    }

    public static void removeEntry(String string) {
        entries.removeIf(catalogEntry -> catalogEntry.getId() != null && catalogEntry.getId().equals(string));
        purchaseCounts.remove(string);
    }

    public static void addPurchaseCount(String string, int n) {
        if (string == null || n <= 0) {
            return;
        }
        purchaseCounts.merge(string, n, Integer::sum);
    }

    public static void removePurchaseCount(String string, int n) {
        if (string == null || n <= 0) {
            return;
        }
        purchaseCounts.merge(string, -n, Integer::sum);
        if (purchaseCounts.getOrDefault(string, 0) <= 0) {
            purchaseCounts.remove(string);
        }
    }

    public static void clearPurchaseCounts() {
        purchaseCounts.clear();
    }

    public static int getPurchaseCount(String string) {
        return string == null ? 0 : purchaseCounts.getOrDefault(string, 0);
    }

    public static int getOwnedQuantity(CatalogEntry catalogEntry) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return 0;
        }
        PlayerInventory class_16612 = client.player.getInventory();
        int n = 0;
        for (int i = 0; i < class_16612.size(); ++i) {
            ItemStack class_17992 = class_16612.getStack(i);
            if (!ItemCatalog.matchesEntry(class_17992, catalogEntry)) continue;
            n += class_17992.getCount();
        }
        return n;
    }

    public static boolean hasRequiredQuantity(CatalogEntry catalogEntry) {
        return ItemCatalog.getPurchaseCount(catalogEntry.getId()) >= Math.max(1, catalogEntry.getResellThreshold());
    }

    public static String getDisplayName(CatalogEntry catalogEntry) {
        return catalogEntry.getCustomName() != null ? catalogEntry.getCustomName() : ItemMetadataUtils.readItemModelWithCustomData(catalogEntry.getStack());
    }

    public static CatalogEntry findEntry(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return null;
        }
        for (CatalogEntry catalogEntry : entries) {
            if (!ItemCatalog.matchesEntry(class_17992, catalogEntry)) continue;
            return catalogEntry;
        }
        return null;
    }

    public static boolean isListingAffordable(MarketInventoryAnalyzer.MarketItem marketItem, double d) {
        ItemStack class_17992 = marketItem.getStack();
        if (class_17992 == null || class_17992.isEmpty()) {
            return false;
        }
        for (CatalogEntry catalogEntry : entries) {
            if (!ItemCatalog.matchesEntry(class_17992, catalogEntry) || ItemCatalog.hasRequiredQuantity(catalogEntry)) continue;
            if (catalogEntry.getPriceMode() == PriceMode.MAX_PRICE) {
                if (marketItem.getPrice() > catalogEntry.getMaxPrice()) {
                    continue;
                }
            } else if (catalogEntry.getPriceMode() == PriceMode.PERCENTAGE) {
                if (d <= 0.0) continue;
                double d2 = d * (1.0 - catalogEntry.getPercentage() / 100.0);
                if (marketItem.getEffectivePrice() > d2) continue;
            }
            return true;
        }
        return false;
    }

    public static boolean matchesEntry(ItemStack class_17992, CatalogEntry catalogEntry) {
        boolean bl;
        if (class_17992 == null || class_17992.isEmpty()) {
            return false;
        }
        ItemStack class_17993 = catalogEntry.getStack();
        if (class_17993 == null || class_17993.isEmpty()) {
            return false;
        }
        if (!class_17993.isOf(class_17992.getItem())) {
            return false;
        }
        if (class_17993.getItem() instanceof PotionItem) {
            return RecipeItemResolver.containsAllEffects(class_17993, class_17992);
        }
        Object2IntArrayMap object2IntArrayMap = new Object2IntArrayMap();
        EnchantmentUtils.collectEnchantments(class_17993, (Object2IntMap<RegistryEntry<Enchantment>>)object2IntArrayMap);
        boolean bl2 = bl = catalogEntry.isStrictEnchants() || !object2IntArrayMap.isEmpty();
        if (bl && !ItemCatalog.matchesEnchantments(class_17992, (Object2IntMap<RegistryEntry<Enchantment>>)object2IntArrayMap, catalogEntry.isStrictEnchants())) {
            return false;
        }
        if (ItemTooltipParser.hasCustomItemId(class_17993)) {
            if (ItemTooltipParser.matchesCustomItemId(class_17993, catalogEntry.getCustomName(), class_17992)) {
                return true;
            }
            ItemTooltipParser.reportItemMismatch(ItemCatalog.getDisplayName(catalogEntry), class_17992);
            return false;
        }
        DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(class_17993);
        if (donorItem == null && bl) {
            return true;
        }
        DonorItemParser.DonorItem donorItem2 = DonorItemParser.parseDonorItem(class_17992);
        if (donorItem != null && donorItem2 != null && donorItem.matchesIdentity(donorItem2)) {
            return true;
        }
        return ItemCatalog.matchesDisplayName(class_17992, catalogEntry);
    }

    private static boolean matchesDisplayName(ItemStack class_17992, CatalogEntry catalogEntry) {
        String string = ItemMetadataUtils.cleanDisplayName(class_17992);
        if (catalogEntry.getCustomName() != null) {
            return string.contains(catalogEntry.getCustomName());
        }
        return string.contains(ItemMetadataUtils.cleanDisplayName(catalogEntry.getStack())) || string.contains(ItemMetadataUtils.readItemModelWithCustomData(catalogEntry.getStack()));
    }

    private static boolean matchesEnchantments(ItemStack class_17992, Object2IntMap<RegistryEntry<Enchantment>> object2IntMap, boolean bl) {
        Object2IntArrayMap object2IntArrayMap = new Object2IntArrayMap();
        EnchantmentUtils.collectEnchantments(class_17992, (Object2IntMap<RegistryEntry<Enchantment>>)object2IntArrayMap);
        if (bl && object2IntMap.size() != object2IntArrayMap.size()) {
            return false;
        }
        for (Object2IntMap.Entry entry : object2IntMap.object2IntEntrySet()) {
            int n = ItemCatalog.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)object2IntArrayMap, (RegistryEntry<Enchantment>)((RegistryEntry)entry.getKey()));
            if (!(bl ? n != entry.getIntValue() : n < entry.getIntValue())) continue;
            return false;
        }
        return true;
    }

    private static int getEnchantmentLevel(Object2IntMap<RegistryEntry<Enchantment>> object2IntMap, RegistryEntry<Enchantment> class_68802) {
        for (Object2IntMap.Entry entry : object2IntMap.object2IntEntrySet()) {
            if (!ItemCatalog.sameEnchantment((RegistryEntry<Enchantment>)((RegistryEntry)entry.getKey()), class_68802)) continue;
            return entry.getIntValue();
        }
        return 0;
    }

    private static boolean sameEnchantment(RegistryEntry<Enchantment> class_68802, RegistryEntry<Enchantment> class_68803) {
        if (class_68802.equals(class_68803)) {
            return true;
        }
        Optional optional = class_68802.getKey();
        Optional optional2 = class_68803.getKey();
        return optional.isPresent() && optional2.isPresent() && ((RegistryKey)optional.get()).equals(optional2.get());
    }

    @Generated
    private ItemCatalog() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static enum PriceMode {
        MAX_PRICE,
        PERCENTAGE;
}

    public static final class CatalogEntry {
        private final ItemStack stack;
        private final long maxPrice;
        private final String customName;
        private final String id;
        private final PriceMode priceMode;
        private final double percentage;
        private final boolean strictEnchants;
        private final long sellPrice;
        private final int sellQuantity;
        private final int resellThreshold;

        public CatalogEntry(ItemStack class_17992, long l, String string, String string2, PriceMode priceMode, double d, boolean bl, long l2, int n, int n2) {
            this.stack = class_17992;
            this.maxPrice = l;
            this.customName = string;
            this.id = string2;
            this.priceMode = priceMode;
            this.percentage = d;
            this.strictEnchants = bl;
            this.sellPrice = l2;
            this.sellQuantity = n;
            this.resellThreshold = n2;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "stack", "maxPrice", "customName", "id", "priceMode", "percentage", "strictEnchants", "sellPrice", "sellQuantity", "resellThreshold");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "stack", "maxPrice", "customName", "id", "priceMode", "percentage", "strictEnchants", "sellPrice", "sellQuantity", "resellThreshold");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "stack", "maxPrice", "customName", "id", "priceMode", "percentage", "strictEnchants", "sellPrice", "sellQuantity", "resellThreshold");
        }

        public ItemStack getStack() {
            return this.stack;
        }

        public long getMaxPrice() {
            return this.maxPrice;
        }

        public String getCustomName() {
            return this.customName;
        }

        public String getId() {
            return this.id;
        }

        public PriceMode getPriceMode() {
            return this.priceMode;
        }

        public double getPercentage() {
            return this.percentage;
        }

        public boolean isStrictEnchants() {
            return this.strictEnchants;
        }

        public long getSellPrice() {
            return this.sellPrice;
        }

        public int getSellQuantity() {
            return this.sellQuantity;
        }

        public int getResellThreshold() {
            return this.resellThreshold;
        }
    }
}

