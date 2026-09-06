/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.PlayerEntity
 *  net.minecraft.SlotActionType
 *  net.minecraft.ItemStack
 *  net.minecraft.Text
 *  net.minecraft.Screen
 *  net.minecraft.HandledScreen
 *  net.minecraft.GameMessageS2CPacket
 */
package moscow.rockstar.modules.other.market.purchase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.events.dispatch.EventListenerCoordinator;
import moscow.rockstar.items.ItemMetadataUtils;
import moscow.rockstar.items.catalog.ItemCatalog;
import moscow.rockstar.market.MarketInventoryAnalyzer;
import moscow.rockstar.market.ScoreboardBalanceTracker;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.other.autobuy.PurchaseTracker;
import moscow.rockstar.modules.other.market.purchase.PurchaseEntry;
import moscow.rockstar.modules.other.market.purchase.PurchaseMode;
import moscow.rockstar.modules.other.market.purchase.PurchaseStatus;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.ActionSetting;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.ui.screens.ItemConfigurationScreen;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.render.ScreenRenderEvent;
import pyrock.utility.render.CustomDrawContext;

@ModuleInfo(name="Auto Buy", category=ModuleCategory.OTHER, description="modules.descriptions.auto_buy")
public class AutoBuy
extends Module {
    private PurchaseStatus purchaseStatus = PurchaseStatus.IDLE;
    private static final long searchTimeout = 3000L;
    private static final long purchaseTimeout = 90000L;
    private final Timer cooldownTimer = new Timer();
    private final Timer actionTimer = new Timer();
    private long lastPurchaseTime = System.currentTimeMillis();
    private ItemCatalog.CatalogEntry currentListing;
    private final EventListenerCoordinator priceCache = EventListenerCoordinator.getInstance();
    private final PurchaseTracker purchaseTracker = new PurchaseTracker();
    private final ScoreboardBalanceTracker marketState = new ScoreboardBalanceTracker();
    private final ActionSetting openMenuAction = new ActionSetting(this, "Open menu").withAction(() -> minecraftClient.setScreen(new ItemConfigurationScreen()));
    private final ActionSetting parsePricesAction = new ActionSetting(this, "Parse prices").withAction(this::processState);
    private final Pattern purchaseMessagePattern = Pattern.compile("\u0412\u044b \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043a\u0443\u043f\u0438\u043b\u0438\\s+(.+?)(?:\\s+x(\\d+))?\\s+\u0437\u0430\\s+\\$([\\d,]+)!", 66);
    private final List<PurchaseEntry> purchaseHistory = new ArrayList<PurchaseEntry>();
    private PurchaseMode purchaseMode = PurchaseMode.IDLE;
    private final EventListener<ReceivePacketEvent> onReceivePacketEvent = receivePacketEvent -> {
        Object object = receivePacketEvent.getPacket();
        if (object instanceof GameMessageS2CPacket) {
            Object object2;
            GameMessageS2CPacket class_74392 = (GameMessageS2CPacket)object;
            object = class_74392.content().getString();
            this.purchaseTracker.handlePurchaseMessage((String)object);
            if (((String)object).contains("\u0423 \u0412\u0430\u0441 \u043a\u0443\u043f\u0438\u043b\u0438")) {
                object2 = ((String)object).substring(((String)object).indexOf("\u043a\u0443\u043f\u0438\u043b\u0438") + "\u043a\u0443\u043f\u0438\u043b\u0438".length()).trim();
                this.findCatalogEntry((String)object2).ifPresent(catalogEntry -> ItemCatalog.removePurchaseCount(catalogEntry.getId(), Math.max(1, catalogEntry.getSellQuantity())));
            }
            if (((String)object).contains("\u0412\u044b \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043a\u0443\u043f\u0438\u043b\u0438")) {
                this.lastPurchaseTime = System.currentTimeMillis();
                object2 = this.purchaseMessagePattern.matcher((CharSequence)object);
                if (((Matcher)object2).find()) {
                    ItemStack class_17992;
                    String string = ((Matcher)object2).group(1).trim();
                    String string2 = ((Matcher)object2).group(2);
                    String string3 = ((Matcher)object2).group(3);
                    int n = string2 != null ? Integer.parseInt(string2) : 1;
                    long l = Long.parseLong(string3.replace(",", ""));
                    ItemCatalog.CatalogEntry catalogEntry2 = this.currentListing != null ? this.currentListing : (ItemCatalog.CatalogEntry)this.findCatalogEntry(string).orElse(null);
                    this.currentListing = null;
                    ItemStack class_17993 = class_17992 = catalogEntry2 != null ? catalogEntry2.getStack() : ItemStack.EMPTY;
                    if (catalogEntry2 != null) {
                        ItemCatalog.addPurchaseCount(catalogEntry2.getId(), n);
                    }
                    this.purchaseHistory.add(new PurchaseEntry(string, n, l, class_17992));
                    if (this.purchaseHistory.size() > 10) {
                        this.purchaseHistory.removeFirst();
                    }
                }
                this.purchaseStatus = PurchaseStatus.ACTIVE;
                this.cooldownTimer.reset();
            }
        }
    };
    private final EventListener<ScreenRenderEvent> onScreenRenderEvent = screenRenderEvent -> {
        HandledScreen BlockStateProviderType;
        Object object = AutoBuy.minecraftClient.currentScreen;
        if (!(object instanceof HandledScreen) || !MarketInventoryAnalyzer.isPriceOrMarketText((BlockStateProviderType = (HandledScreen)object).getTitle().getString())) {
            return;
        }
        object = RockstarDrawContext.create(screenRenderEvent.getContext(), AutoBuy.minecraftClient.currentScreen == null ? -1 : (int)UiUtils.mousePosition().getX(), AutoBuy.minecraftClient.currentScreen == null ? -1 : (int)UiUtils.mousePosition().getY(), MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
        this.render((CustomDrawContext)object);
    };

    private void updatePurchaseState(boolean bl) {
        if (AutoBuy.minecraftClient.player == null || AutoBuy.minecraftClient.world == null
                || AutoBuy.minecraftClient.interactionManager == null) {
            if (this.purchaseMode == PurchaseMode.PARSING) {
                this.resetPurchaseState();
            }
            return;
        }
        if (bl) {
            return;
        }
        if (this.purchaseMode == PurchaseMode.IDLE) {
            if (this.isAutoBuyReady() && this.purchaseTracker.isPurchaseReady()) {
                this.purchaseMode = PurchaseMode.PARSING;
                this.resetStateSecondary();
            }
            return;
        }
        if (!this.purchaseTracker.isPurchaseReady()) {
            this.resetPurchaseState();
        }
    }

    private void resetPurchaseState() {
        this.purchaseMode = PurchaseMode.IDLE;
        this.lastPurchaseTime = System.currentTimeMillis();
        this.purchaseStatus = PurchaseStatus.ACTIVE;
        this.actionTimer.reset();
    }

    private void resetStatePrimary() {
        for (ItemCatalog.CatalogEntry catalogEntry : ItemCatalog.getEntries()) {
            if (catalogEntry.getPriceMode() != ItemCatalog.PriceMode.PERCENTAGE || this.priceCache.parsePrice(catalogEntry.getId()) > 0.0) continue;
            Notification.warning(Text.of((String)(ItemCatalog.getDisplayName(catalogEntry) + ": \u043d\u0435\u0442 \u0446\u0435\u043d\u044b \u0440\u044b\u043d\u043a\u0430 \u2014 \u00ab\u043d\u0438\u0436\u0435 \u0440\u044b\u043d\u043a\u0430\u00bb \u043d\u0435 \u0441\u0440\u0430\u0431\u043e\u0442\u0430\u0435\u0442, \u0441\u043f\u0430\u0440\u0441\u0438 \u0446\u0435\u043d\u044b")));
        }
    }

    private void resetStateSecondary() {
        for (ItemCatalog.CatalogEntry catalogEntry : ItemCatalog.getEntries()) {
            if (catalogEntry.getSellPrice() > 0L || this.priceCache.parsePrice(catalogEntry.getId()) > 0.0 || ItemCatalog.getOwnedQuantity(catalogEntry) <= 0) continue;
            Notification.warning(Text.of((String)(ItemCatalog.getDisplayName(catalogEntry) + ": \u043d\u0435\u0442 \u0446\u0435\u043d\u044b \u043f\u0440\u043e\u0434\u0430\u0436\u0438 \u2014 \u0441\u043f\u0430\u0440\u0441\u0438 \u0446\u0435\u043d\u044b \u0438\u043b\u0438 \u0437\u0430\u0434\u0430\u0439 \u00ab\u0426\u0435\u043d\u0430 \u043f\u0440\u043e\u0434\u0430\u0436\u0438\u00bb")));
        }
    }

    private Optional<ItemCatalog.CatalogEntry> findCatalogEntry(String string) {
        String string2;
        String string3 = string2 = string == null ? "" : string.trim();
        if (string2.isEmpty()) {
            return Optional.empty();
        }
        return ItemCatalog.getEntries().stream().filter(catalogEntry -> {
            if (catalogEntry.getCustomName() != null) {
                return AutoBuy.isValidCondition(catalogEntry.getCustomName(), string2);
            }
            return AutoBuy.isValidCondition(ItemMetadataUtils.cleanDisplayName(catalogEntry.getStack()), string2) || AutoBuy.isValidCondition(ItemMetadataUtils.readItemModelWithCustomData(catalogEntry.getStack()), string2);
        }).findFirst();
    }

    private static boolean isValidCondition(String string, String string2) {
        String string3;
        String string4 = string3 = string == null ? "" : string.trim();
        if (string3.isEmpty()) {
            return false;
        }
        return string3.equalsIgnoreCase(string2) || string2.contains(string3) || string3.contains(string2);
    }

    private boolean isAutoBuyReady() {
        List<ItemCatalog.CatalogEntry> list = ItemCatalog.getEntries();
        if (list.isEmpty()) {
            return true;
        }
        for (ItemCatalog.CatalogEntry catalogEntry : list) {
            if (ItemCatalog.hasRequiredQuantity(catalogEntry)) continue;
            return System.currentTimeMillis() - this.lastPurchaseTime >= 90000L;
        }
        return true;
    }

    private void resetStateTertiary() {
        if (this.purchaseTracker.isPurchaseReady()) {
            return;
        }
        if (AutoBuy.minecraftClient.currentScreen != null && !(AutoBuy.minecraftClient.currentScreen instanceof HandledScreen)) {
            return;
        }
        if (this.actionTimer.hasElapsed(3000L)) {
            AutoBuy.minecraftClient.player.networkHandler.sendChatCommand("ah");
            this.actionTimer.reset();
        }
    }

    private void processState() {
        if (this.priceCache.startPriceUpdate()) {
        }
    }

    @Override
    public void onTick() {
        HandledScreen BlockStateProviderType;
        boolean bl;
        boolean bl2 = this.priceCache.isPriceUpdateRunning();
        this.updatePurchaseState(bl2);
        boolean bl3 = this.purchaseMode == PurchaseMode.PARSING;
        boolean bl4 = bl = bl3 || bl2;
        if (bl3) {
            this.purchaseTracker.startPurchaseSearch();
            return;
        }
        if (bl2) {
            return;
        }
        if (this.priceCache.consumePriceUpdateReady()) {
            this.resetStatePrimary();
            this.purchaseStatus = PurchaseStatus.ACTIVE;
            this.cooldownTimer.reset();
            this.actionTimer.reset();
            return;
        }
        if (this.priceCache.refreshPricesIfDue()) {
            return;
        }
        Screen class_4372 = AutoBuy.minecraftClient.currentScreen;
        if (!(class_4372 instanceof HandledScreen) || !MarketInventoryAnalyzer.isPriceOrMarketText((BlockStateProviderType = (HandledScreen)class_4372).getTitle().getString())) {
            this.resetStateTertiary();
            return;
        }
        this.actionTimer.reset();
        long l = (long)((float)AutoBuy.minecraftClient.player.networkHandler.getPlayerListEntry(AutoBuy.minecraftClient.player.getUuid()).getLatency() * 2.5f + MathUtils.interpolateRandomDouble(24.0, 59.0));
        MarketInventoryAnalyzer.MarketAnalysis marketAnalysis = MarketInventoryAnalyzer.analyzeMarketInventory(BlockStateProviderType, false, null);
        MarketInventoryAnalyzer.MarketItem marketItem = null;
        for (MarketInventoryAnalyzer.MarketItem marketItem2 : marketAnalysis.getItems()) {
            MarketInventoryAnalyzer.MarketItem marketItem3;
            long l2 = marketItem2.getPrice();
            int n = Math.max(1, marketItem2.getCount());
            long l3 = l2 / (long)n;
            double d = marketItem2.getEffectivePrice() / (double)n;
            double d2 = this.priceCache.getPriceUpdateResolver().resolve(marketItem2);
            if (!this.marketState.isBalanceCacheValid(l2) || !ItemCatalog.isListingAffordable(marketItem3 = new MarketInventoryAnalyzer.MarketItem(marketItem2.getSlot(), marketItem2.getStack(), l3, n, marketItem2.getMaxDurability(), marketItem2.getCurrentDurability(), d), d2) || marketItem != null && !(marketItem3.getEffectivePrice() < marketItem.getEffectivePrice())) continue;
            marketItem = marketItem3;
        }
        switch (this.purchaseStatus.ordinal()) {
            case 1: {
                this.clearStateForclass465AndLongAndInner(BlockStateProviderType, l, marketItem);
                break;
            }
            case 2: {
                this.resetStatePrimary(BlockStateProviderType, l, marketItem);
                break;
            }
        }
    }

    private void clearStateForclass465AndLongAndInner(HandledScreen<?> BlockStateProviderType, long l, MarketInventoryAnalyzer.MarketItem marketItem) {
        if (marketItem != null) {
            this.purchaseStatus = PurchaseStatus.FAILED;
            this.cooldownTimer.reset();
            return;
        }
        if (this.cooldownTimer.hasElapsed(l + 200L)) {
            AutoBuy.minecraftClient.interactionManager.clickSlot(BlockStateProviderType.getScreenHandler().syncId, 49, 0, SlotActionType.PICKUP, (PlayerEntity)AutoBuy.minecraftClient.player);
            this.cooldownTimer.reset();
        }
    }

    private void resetStatePrimary(HandledScreen<?> BlockStateProviderType, long l, MarketInventoryAnalyzer.MarketItem marketItem) {
        if (marketItem == null) {
            this.purchaseStatus = PurchaseStatus.ACTIVE;
            this.cooldownTimer.reset();
            return;
        }
        if (!ItemCatalog.isListingAffordable(marketItem, this.priceCache.getPriceUpdateResolver().resolve(marketItem))) {
            this.purchaseStatus = PurchaseStatus.ACTIVE;
            this.cooldownTimer.reset();
            return;
        }
        if (!this.marketState.isBalanceCacheValid(marketItem.getPrice() * (long)Math.max(1, marketItem.getCount()))) {
            this.purchaseStatus = PurchaseStatus.ACTIVE;
            this.cooldownTimer.reset();
            return;
        }
        if (this.cooldownTimer.hasElapsed(l)) {
            this.currentListing = ItemCatalog.findEntry(marketItem.getStack());
            AutoBuy.minecraftClient.interactionManager.clickSlot(BlockStateProviderType.getScreenHandler().syncId, marketItem.getSlot(), 0, SlotActionType.QUICK_MOVE, (PlayerEntity)AutoBuy.minecraftClient.player);
            this.cooldownTimer.reset();
            this.purchaseStatus = PurchaseStatus.ACTIVE;
        }
    }

    private void render(CustomDrawContext customDrawContext) {
        List<PurchaseEntry> list = this.getField();
        int n = 270;
        int n2 = (int)(INSTANCE.height() / 2.0f) - 105;
        int n3 = 16;
        customDrawContext.drawClientRect(n, n2 - 5, 120.0f, list.size() * 20 + 8, 255.0f, 1.0f, 7.0f, 6.0f);
        int n4 = 0;
        for (PurchaseEntry purchaseEntry : list) {
            customDrawContext.drawItem(purchaseEntry.getItemStack(), (float)(n + 4), (float)(n2 + n4), 1.0f);
            customDrawContext.drawText(Font.MEDIUM.metrics(7.0f), Text.of((String)purchaseEntry.getItemName()), n + n3 + 6, n2 + n4 + 2);
            customDrawContext.drawText(Font.REGULAR.metrics(7.0f), Text.of((String)(MathUtils.formatCurrencyAmount(purchaseEntry.getTotalPrice()) + " x" + purchaseEntry.getPurchaseAmount())), n + n3 + 6, n2 + n4 + 10);
            n4 += 20;
        }
    }

    @Override
    public void onEnable() {
        this.purchaseTracker.setPriceResolver(catalogEntry -> (long)this.priceCache.parsePrice(catalogEntry.getId()));
        this.lastPurchaseTime = System.currentTimeMillis();
        this.actionTimer.reset();
        this.purchaseMode = PurchaseMode.IDLE;
        this.purchaseStatus = PurchaseStatus.ACTIVE;
        this.resetStatePrimary();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.purchaseTracker.resetPurchase();
        this.marketState.clearCachedBalance();
        ItemCatalog.clearPurchaseCounts();
        this.currentListing = null;
        this.purchaseMode = PurchaseMode.IDLE;
        this.purchaseStatus = PurchaseStatus.IDLE;
        super.onDisable();
    }

    @Generated
    public List<PurchaseEntry> getField() {
        return this.purchaseHistory;
    }
}
