/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.GameMessageS2CPacket
 */
package moscow.rockstar.modules.player.farming.hud;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.market.ScoreboardBalanceTracker;
import moscow.rockstar.modules.player.farming.core.FarmModeBase;
import moscow.rockstar.modules.player.farming.hud.FarmDisplayCategory;
import moscow.rockstar.modules.player.farming.hud.FarmMetric;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import pyrock.events.game.BlockBreakEvent;
import pyrock.events.network.ReceivePacketEvent;

public class FarmHudMetrics
implements ClientAccess {
    private static final long WINDOW_INTERVAL_MILLIS = 5000L;
    public static final int WINDOW_CAPACITY = 36;
    private static final long MIN_RATE_INTERVAL_MILLIS = 1000L;
    private static final String PURCHASE_MESSAGE_PREFIX = "\u0423 \u0412\u0430\u0441 \u043a\u0443\u043f\u0438\u043b\u0438";
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("(\\d[\\d.,\\u00A0]*\\d|\\d)\\s*(kkk|kk|k|\u043a\u043a\u043a|\u043a\u043a|\u043a|m|\u043c)?", 66);
    private final ScoreboardBalanceTracker profitTracker = new ScoreboardBalanceTracker();
    private Supplier<FarmModeBase> farmModeSupplier;
    private final long[] blockCountHistory = new long[36];
    private final long[] profitChangeHistory = new long[36];
    private boolean tracking;
    private long trackingStartedAt;
    private long trackingStoppedAt;
    private long blocksMined;
    private long profitChange;
    private long purchasesValue;
    private long initialProfitTotal = -1L;
    private long latestProfitTotal = -1L;
    private long lastProfitPollAt;
    private int currentBucket;
    private int bucketCount;
    private long currentBucketStartedAt;
    private final EventListener<BlockBreakEvent> blockBreakListener = blockBreakEvent -> {
        if (!this.tracking) {
            return;
        }
        FarmModeBase farmModeBase = this.farmModeSupplier.get();
        if (farmModeBase == null || farmModeBase.getFarmDisplayCategory() != FarmDisplayCategory.BLOCKS) {
            return;
        }
        this.recordBlockBreak();
    };
    private final EventListener<ReceivePacketEvent> chatListener = receivePacketEvent -> {
        Object object;
        if (!this.tracking || !((object = receivePacketEvent.getPacket()) instanceof GameMessageS2CPacket)) {
            return;
        }
        GameMessageS2CPacket class_74392 = (GameMessageS2CPacket)object;
        object = class_74392.content().getString();
        if (!((String)object).contains(PURCHASE_MESSAGE_PREFIX)) {
            return;
        }
        long l = FarmHudMetrics.parseCurrencyAmount((String)object);
        if (l > 0L) {
            this.purchasesValue += l;
        }
    };

    public FarmHudMetrics(Supplier<FarmModeBase> supplier) {
        this.farmModeSupplier = supplier;
    }

    public void startTracking() {
        this.tracking = true;
        this.trackingStartedAt = System.currentTimeMillis();
        this.trackingStoppedAt = 0L;
        this.blocksMined = 0L;
        this.profitChange = 0L;
        this.purchasesValue = 0L;
        this.initialProfitTotal = -1L;
        this.latestProfitTotal = -1L;
        this.lastProfitPollAt = 0L;
        this.profitTracker.clearCachedBalance();
        Arrays.fill(this.blockCountHistory, 0L);
        Arrays.fill(this.profitChangeHistory, 0L);
        this.currentBucket = 0;
        this.bucketCount = 1;
        this.currentBucketStartedAt = this.trackingStartedAt;
    }

    public void stopTracking() {
        this.tracking = false;
        this.trackingStoppedAt = System.currentTimeMillis();
    }

    public void updateMetrics() {
        if (!this.tracking) {
            return;
        }
        this.advanceWindow();
        this.updateProfitHistory();
    }

    public void recordBlockBreak() {
        this.addBlockCount(1);
    }

    public void addBlockCount(int n) {
        if (!this.tracking || n <= 0) {
            return;
        }
        this.blocksMined += (long)n;
        int n2 = this.currentBucket;
        this.blockCountHistory[n2] = this.blockCountHistory[n2] + (long)n;
    }

    public long getElapsedMillis() {
        if (this.trackingStartedAt == 0L) {
            return 0L;
        }
        return (this.tracking ? System.currentTimeMillis() : this.trackingStoppedAt) - this.trackingStartedAt;
    }

    public double getBlocksPerHour() {
        return this.toPerHourRate(this.blocksMined);
    }

    public double getProfitPerHour() {
        return this.toPerHourRate(this.profitChange);
    }

    private double toPerHourRate(double d) {
        long l = this.getElapsedMillis();
        if (l < 1000L) {
            return 0.0;
        }
        return d / ((double)l / 3600000.0);
    }

    public long[] getMetricHistory(FarmMetric farmMetric) {
        long[] lArray = farmMetric == FarmMetric.PROFIT_CHANGE ? this.profitChangeHistory : this.blockCountHistory;
        int n = Math.max(0, Math.min(this.bucketCount, 36) - 1);
        long[] lArray2 = new long[n];
        for (int i = 0; i < n; ++i) {
            int n2 = Math.floorMod(this.currentBucket - n + i, 36);
            lArray2[i] = lArray[n2];
        }
        return lArray2;
    }

    public static float getWindowDurationSeconds() {
        return 5.0f;
    }

    private void advanceWindow() {
        long l = System.currentTimeMillis();
        while (l - this.currentBucketStartedAt >= 5000L) {
            this.currentBucketStartedAt += 5000L;
            this.currentBucket = (this.currentBucket + 1) % 36;
            this.blockCountHistory[this.currentBucket] = 0L;
            this.profitChangeHistory[this.currentBucket] = 0L;
            if (this.bucketCount >= 36) continue;
            ++this.bucketCount;
        }
    }

    private void updateProfitHistory() {
        long l = System.currentTimeMillis();
        if (l - this.lastProfitPollAt < 1000L) {
            return;
        }
        this.lastProfitPollAt = l;
        long l2 = this.profitTracker.getCachedBalance();
        if (l2 < 0L) {
            return;
        }
        if (this.initialProfitTotal < 0L) {
            this.initialProfitTotal = l2;
            this.latestProfitTotal = l2;
            return;
        }
        long l3 = l2 - this.latestProfitTotal;
        this.latestProfitTotal = l2;
        this.profitChange = l2 - this.initialProfitTotal;
        if (l3 != 0L) {
            int n = this.currentBucket;
            this.profitChangeHistory[n] = this.profitChangeHistory[n] + l3;
        }
    }

    private static long parseCurrencyAmount(String string) {
        Matcher matcher = CURRENCY_PATTERN.matcher(string);
        long l = -1L;
        while (matcher.find()) {
            long l2;
            String string2 = matcher.group(1).replaceAll("[^\\d]", "");
            if (string2.isEmpty()) continue;
            try {
                l2 = Long.parseLong(string2);
            }
            catch (NumberFormatException numberFormatException) {
                continue;
            }
            String string3 = matcher.group(2);
            if (string3 != null) {
                l2 *= (switch (string3.toLowerCase(Locale.ROOT)) {
                    case "k", "\u043a" -> 1000L;
                    case "kk", "\u043a\u043a", "m", "\u043c" -> 1000000L;
                    case "kkk", "\u043a\u043a\u043a" -> 1000000000L;
                    default -> 1L;
                });
            }
            l = Math.max(l, l2);
        }
        return l;
    }

    public static String formatSignedCurrency(long l) {
        String string = l < 0L ? "-" : "+";
        return string + FarmHudMetrics.formatCurrency(Math.abs(l));
    }

    public static String formatCurrency(double d) {
        double d2 = Math.abs(d);
        if (d2 >= 1.0E9) {
            return FarmHudMetrics.formatCompactNumber(d / 1.0E9) + "kkk";
        }
        if (d2 >= 1000000.0) {
            return FarmHudMetrics.formatCompactNumber(d / 1000000.0) + "kk";
        }
        if (d2 >= 1000.0) {
            return FarmHudMetrics.formatCompactNumber(d / 1000.0) + "k";
        }
        return String.valueOf(Math.round(d));
    }

    private static String formatCompactNumber(double d) {
        String string = String.format(Locale.ROOT, "%.1f", d);
        return string.endsWith(".0") ? string.substring(0, string.length() - 2) : string;
    }

    public static String formatElapsedTime(long l) {
        long l2 = Math.max(0L, l) / 1000L;
        long l3 = l2 / 3600L;
        long l4 = l2 % 3600L / 60L;
        long l5 = l2 % 60L;
        return l3 > 0L ? String.format(Locale.ROOT, "%d:%02d:%02d", l3, l4, l5) : String.format(Locale.ROOT, "%d:%02d", l4, l5);
    }

    @Generated
    public ScoreboardBalanceTracker getProfitTracker() {
        return this.profitTracker;
    }

    @Generated
    public Supplier<FarmModeBase> getFarmModeSupplier() {
        return this.farmModeSupplier;
    }

    @Generated
    public long[] getBlockCountHistory() {
        return this.blockCountHistory;
    }

    @Generated
    public long[] getProfitChangeHistory() {
        return this.profitChangeHistory;
    }

    @Generated
    public boolean isTracking() {
        return this.tracking;
    }

    @Generated
    public long getTrackingStartedAt() {
        return this.trackingStartedAt;
    }

    @Generated
    public long getTrackingStoppedAt() {
        return this.trackingStoppedAt;
    }

    @Generated
    public long getBlocksMined() {
        return this.blocksMined;
    }

    @Generated
    public long getProfitChange() {
        return this.profitChange;
    }

    @Generated
    public long getPurchasesValue() {
        return this.purchasesValue;
    }

    @Generated
    public long getInitialProfitTotal() {
        return this.initialProfitTotal;
    }

    @Generated
    public long getLatestProfitTotal() {
        return this.latestProfitTotal;
    }

    @Generated
    public long getLastProfitPollAt() {
        return this.lastProfitPollAt;
    }

    @Generated
    public int getCurrentBucket() {
        return this.currentBucket;
    }

    @Generated
    public int getBucketCount() {
        return this.bucketCount;
    }

    @Generated
    public long getCurrentBucketStartedAt() {
        return this.currentBucketStartedAt;
    }

    @Generated
    public EventListener<BlockBreakEvent> getBlockBreakListener() {
        return this.blockBreakListener;
    }

    @Generated
    public EventListener<ReceivePacketEvent> getChatListener() {
        return this.chatListener;
    }
}
