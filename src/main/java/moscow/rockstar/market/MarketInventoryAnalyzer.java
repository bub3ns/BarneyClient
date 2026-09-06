/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Slot
 *  net.minecraft.Item$TooltipContext
 *  net.minecraft.ItemStack
 *  net.minecraft.TooltipType
 *  net.minecraft.World
 *  net.minecraft.Text
 *  net.minecraft.HandledScreen
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.market;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import moscow.rockstar.auth.migration.TokenMigration;
import moscow.rockstar.core.ClientAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.jetbrains.annotations.NotNull;

public final class MarketInventoryAnalyzer
implements ClientAccess {
    private static final Pattern NUMERIC_TEXT_PATTERN = Pattern.compile("^[^\\p{L}\\d]{0,3}\\d[\\d.,\\u00A0 ]*[^\\p{L}\\d]{0,3}$");
    private static final long REQUEST_TIMEOUT_MILLIS = 100L;
    private static final Pattern NUMBER_TOKEN_PATTERN = Pattern.compile("\\d[\\d.,\\u00A0 ]*\\d|\\d");
    private static final Pattern SEARCH_LABEL_PATTERN = Pattern.compile("(?:^|\\s)(?:\u043f\u043e\u0438\u0441\u043a|\u043f\u043e\u0438\u0441|\u043f\u043e\u0438|\u043f\u043e|\u043f)\\s*:", 66);
    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)\u00a7[0-9a-fk-or]");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    public static MarketAnalysis analyzeMarketInventory(HandledScreen<?> BlockStateProviderType, boolean bl, Predicate<ItemStack> predicate) {
        ArrayList<MarketItem> arrayList = new ArrayList<MarketItem>();
        MarketItem marketItem = null;
        double d = 0.0;
        double d2 = Double.MAX_VALUE;
        for (int i = 0; i < BlockStateProviderType.getScreenHandler().slots.size() - 36; ++i) {
            Slot class_17352 = BlockStateProviderType.getScreenHandler().getSlot(i);
            if (class_17352 == null || !class_17352.hasStack()) continue;
            ItemStack class_17992 = class_17352.getStack();
            if (predicate != null && !predicate.test(class_17992)) continue;
            List<Text> list = class_17992.getTooltip(Item.TooltipContext.create((World)MarketInventoryAnalyzer.minecraftClient.world), (PlayerEntity)MarketInventoryAnalyzer.minecraftClient.player, (TooltipType)(MarketInventoryAnalyzer.minecraftClient.options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC));
            long l = MarketInventoryAnalyzer.findPriceInLore(list);
            if (l <= 0L) continue;
            int n = Math.max(1, class_17992.getCount());
            int n2 = class_17992.getMaxDamage();
            int n3 = n2 - class_17992.getDamage();
            double d3 = bl ? (double)l / (double)n : (double)l;
            double d4 = d3 / MarketInventoryAnalyzer.calculateUnitPrice(n2, n3);
            MarketItem marketItem2 = new MarketItem(class_17352.id, class_17992, l, n, n2, n3, d4);
            arrayList.add(marketItem2);
            d += d4;
            if (!(d4 < d2)) continue;
            d2 = d4;
            marketItem = marketItem2;
        }
        double d5 = arrayList.isEmpty() ? 0.0 : d / (double)arrayList.size();
        return new MarketAnalysis(arrayList, d5, d2, marketItem);
    }

    public static MarketSummary summarizeMarketOffers(HandledScreen<?> BlockStateProviderType, Predicate<ItemStack> predicate) {
        long l = -1L;
        long l2 = -1L;
        long l3 = -1L;
        int n = -1;
        ArrayList<ExchangeOffer> arrayList = new ArrayList<ExchangeOffer>();
        for (int i = 0; i < BlockStateProviderType.getScreenHandler().slots.size() - 36; ++i) {
            Slot class_17352 = BlockStateProviderType.getScreenHandler().getSlot(i);
            if (class_17352 == null || !class_17352.hasStack()) continue;
            ItemStack class_17992 = class_17352.getStack();
            if (predicate != null && !predicate.test(class_17992)) continue;
            List<Text> list = class_17992.getTooltip(Item.TooltipContext.create((World)MarketInventoryAnalyzer.minecraftClient.world), (PlayerEntity)MarketInventoryAnalyzer.minecraftClient.player, (TooltipType)(MarketInventoryAnalyzer.minecraftClient.options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC));
            long l4 = -1L;
            long l5 = -1L;
            long l6 = -1L;
            for (Text class_25612 : list) {
                String string = class_25612.getString();
                String string2 = string.toLowerCase(Locale.ROOT);
                if (string2.contains("\u0431\u0438\u0440\u0436\u0430 \u0431\u0430\u043b\u0430\u043d\u0441")) {
                    l5 = MarketInventoryAnalyzer.parseLargestNumber(string);
                }
                if (string2.contains("\u043c\u043e\u043d\u0435\u0442")) {
                    l6 = MarketInventoryAnalyzer.parseLargestNumber(string);
                }
                if (!string2.contains("\u043a\u0443\u0440\u0441")) continue;
                l4 = MarketInventoryAnalyzer.parseLargestNumber(string);
            }
            if (l5 != -1L) {
                l = l5;
            }
            if (l6 != -1L) {
                l2 = l6;
            }
            if (l4 <= 0L) continue;
            arrayList.add(new ExchangeOffer(class_17352.id, l4));
            if (l4 <= l3) continue;
            l3 = l4;
            n = class_17352.id;
        }
        return new MarketSummary(l, l2, l3, n, arrayList);
    }

    public static long findPriceInLore(List<Text> list) {
        long l = -1L;
        for (Text class_25612 : list) {
            long l2;
            String string = MarketInventoryAnalyzer.stripFormattingCodes(class_25612.getString());
            if (string.isEmpty() || string.contains("%")) continue;
            if (MarketInventoryAnalyzer.isPriceLabel(string.toLowerCase(Locale.ROOT), string)) {
                l2 = MarketInventoryAnalyzer.parseLargestNumber(string);
                if (l2 <= 0L) continue;
                return l2;
            }
            if (!NUMERIC_TEXT_PATTERN.matcher(string).matches() || (l2 = MarketInventoryAnalyzer.parseLargestNumber(string)) < 100L) continue;
            l = Math.max(l, l2);
        }
        return l;
    }

    private static boolean isPriceLabel(String string, String string2) {
        boolean bl = string.contains("\u0446\u0435\u043d\u0430") || string.contains("\u0446e\u043d\u0430") || string.contains("\u0446\u0435\u043da") || string.contains("\u0446e\u043da") || string.contains("$") || string.contains("price") || string.contains("\u0441\u0442\u043e\u0438\u043c");
        return bl && !string2.contains("%");
    }

    public static long parsePriceText(String string) {
        return MarketInventoryAnalyzer.parseLargestNumber(MarketInventoryAnalyzer.stripFormattingCodes(string));
    }

    private static long parseLargestNumber(String string) {
        Matcher matcher = NUMBER_TOKEN_PATTERN.matcher(string);
        long l = -1L;
        while (matcher.find()) {
            String string2 = matcher.group().replaceAll("[^\\d]", "");
            if (string2.isEmpty()) continue;
            try {
                l = Math.max(l, Long.parseLong(string2));
            }
            catch (NumberFormatException numberFormatException) {}
        }
        return l;
    }

    private static double calculateUnitPrice(int n, int n2) {
        if (n <= 0) {
            return 1.0;
        }
        return Math.max(0.1, (double)n2 / (double)n);
    }

    public static String normalizeMarketText(String string) {
        return MarketInventoryAnalyzer.stripFormattingCodes(string).toLowerCase(Locale.ROOT);
    }

    public static String stripFormattingCodes(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        String string2 = FORMATTING_CODE_PATTERN.matcher(string).replaceAll("");
        StringBuilder stringBuilder = new StringBuilder(string2.length());
        string2.codePoints().forEach(n -> {
            if (!MarketInventoryAnalyzer.isIgnorableCodePoint(n)) {
                stringBuilder.appendCodePoint(n);
            }
        });
        return WHITESPACE_PATTERN.matcher(stringBuilder).replaceAll(" ").trim();
    }

    private static boolean isIgnorableCodePoint(int n) {
        int n2 = Character.getType(n);
        return n2 == 18 || n2 == 15 || n2 == 16 || n2 == 19 || n2 == 0;
    }

    public static boolean isMarketText(@NotNull String string) {
        return string.contains("\u0430\u0443\u043a\u0446\u0438\u043e\u043d") || string.contains("\u043f\u043e\u0438\u0441\u043a") || MarketInventoryAnalyzer.containsSearchLabel(string) || SEARCH_LABEL_PATTERN.matcher(string).find();
    }

    public static boolean containsSearchLabel(@NotNull String string) {
        return string.contains("\u0434\u043e\u043d\u043c\u0430\u0440\u043a\u0435\u0442") || string.contains("\u0434\u043e\u043d \u043c\u0430\u0440\u043a\u0435\u0442");
    }

    public static boolean isPriceOrMarketText(@NotNull String string) {
        String string2 = MarketInventoryAnalyzer.normalizeMarketText(string);
        return MarketInventoryAnalyzer.isMarketText(string2)
            || MarketInventoryAnalyzer.isPriceLabel(string2, string2)
            || NUMERIC_TEXT_PATTERN.matcher(string2).matches();
    }

    public static boolean isMarketScreen(HandledScreen<?> BlockStateProviderType) {
        return BlockStateProviderType != null
            && MarketInventoryAnalyzer.isPriceOrMarketText(BlockStateProviderType.getTitle().getString());
    }

    public static boolean isRecognizedMarketText(@NotNull String string) {
        String string2 = MarketInventoryAnalyzer.normalizeMarketText(string);
        if (MarketInventoryAnalyzer.containsSearchLabel(string2)) {
            return true;
        }
        return MarketInventoryAnalyzer.isPriceOrMarketText(string2);
    }

    @Generated
    private MarketInventoryAnalyzer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final class MarketItem {
        private final int slot;
        private final ItemStack stack;
        private final long price;
        private final int count;
        private final int maxDurability;
        private final int currentDurability;
        private final double effectivePrice;

        public MarketItem(int n, ItemStack class_17992, long l, int n2, int n3, int n4, double d) {
            this.slot = n;
            this.stack = class_17992;
            this.price = l;
            this.count = n2;
            this.maxDurability = n3;
            this.currentDurability = n4;
            this.effectivePrice = d;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "slot", "stack", "price", "count", "maxDurability", "currentDurability", "effectivePrice");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "slot", "stack", "price", "count", "maxDurability", "currentDurability", "effectivePrice");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "slot", "stack", "price", "count", "maxDurability", "currentDurability", "effectivePrice");
        }

        public int getSlot() {
            return this.slot;
        }

        public ItemStack getStack() {
            return this.stack;
        }

        public long getPrice() {
            return this.price;
        }

        public int getCount() {
            return this.count;
        }

        public int getMaxDurability() {
            return this.maxDurability;
        }

        public int getCurrentDurability() {
            return this.currentDurability;
        }

        public double getEffectivePrice() {
            return this.effectivePrice;
        }
    }

    public static final class MarketAnalysis {
        private final List<MarketItem> items;
        private final double averageEffectivePrice;
        private final double minimumEffectivePrice;
        private final MarketItem cheapest;

        public MarketAnalysis(List<MarketItem> list, double d, double d2, MarketItem marketItem) {
            this.items = list;
            this.averageEffectivePrice = d;
            this.minimumEffectivePrice = d2;
            this.cheapest = marketItem;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "items", "averageEffectivePrice", "minimumEffectivePrice", "cheapest");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "items", "averageEffectivePrice", "minimumEffectivePrice", "cheapest");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "items", "averageEffectivePrice", "minimumEffectivePrice", "cheapest");
        }

        public List<MarketItem> getItems() {
            return this.items;
        }

        public double getAverageEffectivePrice() {
            return this.averageEffectivePrice;
        }

        public double getMinimumEffectivePrice() {
            return this.minimumEffectivePrice;
        }

        public MarketItem getCheapest() {
            return this.cheapest;
        }
    }

    public static final class ExchangeOffer {
        private final int slot;
        private final long rate;

        public ExchangeOffer(int n, long l) {
            this.slot = n;
            this.rate = l;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "slot", "rate");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "slot", "rate");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "slot", "rate");
        }

        public int getSlot() {
            return this.slot;
        }

        public long getRate() {
            return this.rate;
        }
    }

    public static final class MarketSummary {
        private final long exchangeBalance;
        private final long balance;
        private final long bestRate;
        private final int bestSlot;
        private final List<ExchangeOffer> allOffers;

        public MarketSummary(long l, long l2, long l3, int n, List<ExchangeOffer> list) {
            this.exchangeBalance = l;
            this.balance = l2;
            this.bestRate = l3;
            this.bestSlot = n;
            this.allOffers = list;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "exchangeBalance", "balance", "bestRate", "bestSlot", "allOffers");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "exchangeBalance", "balance", "bestRate", "bestSlot", "allOffers");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "exchangeBalance", "balance", "bestRate", "bestSlot", "allOffers");
        }

        public long getExchangeBalance() {
            return this.exchangeBalance;
        }

        public long getBalance() {
            return this.balance;
        }

        public long getBestRate() {
            return this.bestRate;
        }

        public int getBestSlot() {
            return this.bestSlot;
        }

        public List<ExchangeOffer> getAllOffers() {
            return this.allOffers;
        }
    }
}
