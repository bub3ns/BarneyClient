package moscow.rockstar.events.dispatch;

import java.util.concurrent.atomic.AtomicBoolean;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.items.catalog.ItemCatalog;
import moscow.rockstar.market.MarketPriceResolver;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.util.Timer;
import net.minecraft.text.Text;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.player.ClientPlayerTickEvent;

/**
 * Coordinates asynchronous market-price refreshes for the purchase module.
 */
public final class EventListenerCoordinator {
    private static final long REFRESH_INTERVAL_MILLIS = 30_000L;
    private static final long REFRESH_SETTLE_MILLIS = 250L;
    private static EventListenerCoordinator instance;

    private final MarketPriceResolver priceResolver = new MarketPriceResolver();
    private final Timer refreshTimer = new Timer();
    private final AtomicBoolean refreshInProgress = new AtomicBoolean();
    private volatile boolean priceRefreshReady;

    private final EventListener<ReceivePacketEvent> marketMessageListener = event -> {
        // Market screens are parsed by MarketInventoryAnalyzer. Packet events are
        // intentionally observed only as a refresh boundary; no server command or
        // remote payload is executed here.
    };

    private final EventListener<ClientPlayerTickEvent> refreshTickListener = event -> {
        if (!this.refreshInProgress.get()) {
            return;
        }
        if (this.refreshTimer.hasElapsed(REFRESH_SETTLE_MILLIS)) {
            this.refreshInProgress.set(false);
            this.priceRefreshReady = true;
        }
    };

    private EventListenerCoordinator() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    public static EventListenerCoordinator getInstance() {
        if (instance == null) {
            instance = new EventListenerCoordinator();
        }
        return instance;
    }

    public boolean isPriceUpdateRunning() {
        return this.refreshInProgress.get();
    }

    public boolean consumePriceUpdateReady() {
        boolean ready = this.priceRefreshReady;
        this.priceRefreshReady = false;
        return ready;
    }

    public double parsePrice(String text) {
        return this.priceResolver.parse(text);
    }

    public boolean startPriceUpdate() {
        if (ItemCatalog.getEntries().isEmpty()) {
            Notification.warning(Text.of("Список предметов пуст!"));
            return false;
        }
        if (!this.refreshInProgress.compareAndSet(false, true)) {
            Notification.warning(Text.of("Парсинг уже идёт."));
            return false;
        }
        this.priceRefreshReady = false;
        this.priceResolver.refresh(ItemCatalog.getEntries());
        this.refreshTimer.reset();
        return true;
    }

    public boolean refreshPricesIfDue() {
        if (!this.refreshTimer.hasElapsed(REFRESH_INTERVAL_MILLIS)
            || this.isPriceUpdateRunning()
            || ItemCatalog.getEntries().isEmpty()) {
            return false;
        }
        Notification.info(Text.of("Автопарс: обновляю цены рынка…"));
        return this.startPriceUpdate();
    }

    public long getMillisUntilPriceRefresh() {
        if (this.isPriceUpdateRunning()) {
            return 0L;
        }
        return Math.max(0L, REFRESH_INTERVAL_MILLIS - this.refreshTimer.getElapsedMillis());
    }

    public MarketPriceResolver getPriceUpdateResolver() {
        return this.priceResolver;
    }
}
