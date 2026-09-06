/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.PlayerEntity
 *  net.minecraft.PlayerInventory
 *  net.minecraft.ScreenHandler
 *  net.minecraft.SlotActionType
 *  net.minecraft.Slot
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.Screen
 *  net.minecraft.HandledScreen
 */
package moscow.rockstar.modules.other.autobuy;

import java.util.function.ToLongFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.items.catalog.ItemCatalog;
import moscow.rockstar.market.MarketInventoryAnalyzer;
import moscow.rockstar.math.MathUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public class PurchaseTracker
implements ClientAccess {
    private static final long SEARCH_TIMEOUT_MILLIS = 5000L;
    private static final long GUI_TIMEOUT_MILLIS = 8000L;
    private static final long ACTION_DELAY_MILLIS = 900L;
    private static final int MAX_RETRY_COUNT = 3;
    private static final long STALE_SEARCH_TIMEOUT_MILLIS = 60000L;
    private static final String STORE_NAME = "\u0425\u0440\u0430\u043d\u0438\u043b\u0438\u0449\u0435";
    private ToLongFunction<ItemCatalog.CatalogEntry> priceResolver = catalogEntry -> 0L;
    private PurchaseState purchaseState = PurchaseState.IDLE;
    private ItemCatalog.CatalogEntry activeEntry;
    private boolean purchaseConfirmed;
    private boolean containerPrepared;
    private int clickIndex;
    private int retryCount;
    private boolean searchTimedOut;
    private boolean inventoryScanActive;
    private long purchaseStartTime;
    private long lastActionTime;
    private long nextActionTime;
    private long lastSearchTime = System.currentTimeMillis();
    private static final Pattern pricePattern = Pattern.compile("\\$([\\d.,]+)");

    public void setPriceResolver(ToLongFunction<ItemCatalog.CatalogEntry> toLongFunction) {
        if (toLongFunction != null) {
            this.priceResolver = toLongFunction;
        }
    }

    public boolean isPurchaseInProgress() {
        return this.purchaseState != PurchaseState.IDLE;
    }

    public boolean isPurchaseReady() {
        return this.isPurchaseInProgress() || this.findPurchaseCandidate() != null || this.hasSearchTimedOut();
    }

    private boolean hasSearchTimedOut() {
        return this.searchTimedOut && System.currentTimeMillis() - this.lastSearchTime >= 60000L;
    }

    public void resetPurchase() {
        this.closeCurrentScreen();
        this.activeEntry = null;
        this.containerPrepared = false;
        this.inventoryScanActive = false;
        this.searchTimedOut = false;
        this.retryCount = 0;
        this.setPurchaseState(PurchaseState.IDLE);
    }

    public void startPurchaseSearch() {
        if (PurchaseTracker.minecraftClient.player == null || PurchaseTracker.minecraftClient.world == null || PurchaseTracker.minecraftClient.interactionManager == null) {
            return;
        }
        try {
            this.advancePurchaseState();
        }
        catch (Exception exception) {
            this.resetPurchase();
        }
    }

    private void advancePurchaseState() {
        switch (this.purchaseState.ordinal()) {
            case 0: {
                ItemCatalog.CatalogEntry catalogEntry = this.findPurchaseCandidate();
                if (catalogEntry != null) {
                    this.activeEntry = catalogEntry;
                    this.containerPrepared = false;
                    this.closeCurrentScreen();
                    this.setPurchaseState(PurchaseState.FIND_LISTING);
                    return;
                }
                if (this.hasSearchTimedOut()) {
                    this.lastSearchTime = this.getLastSearchTime();
                    this.inventoryScanActive = true;
                    this.activeEntry = null;
                    this.containerPrepared = false;
                    this.closeCurrentScreen();
                    this.setPurchaseState(PurchaseState.OPEN_INVENTORY);
                    return;
                }
                this.inventoryScanActive = false;
                this.activeEntry = null;
                break;
            }
            case 1: {
                if (!this.hasElapsed(this.getLastActionTime())) {
                    return;
                }
                if (this.countOwnedItems(this.activeEntry) <= 0) {
                    if (this.containerPrepared) {
                        this.lastActionTime = this.getLastSearchTime();
                        this.setPurchaseState(PurchaseState.WAIT_FOR_RESULT);
                    } else {
                        this.resetSearch();
                    }
                    return;
                }
                if (!this.isChatMessage("ah sellgui " + this.getEntryPurchaseTime())) {
                    return;
                }
                this.setPurchaseState(PurchaseState.OPEN_LISTING);
                break;
            }
            case 2: {
                if (this.getCurrentScreen() != null) {
                    this.retryCount = 0;
                    this.setPurchaseState(PurchaseState.SELECT_LISTING);
                    break;
                }
                if (!this.hasElapsed(5000L)) break;
                if (++this.retryCount <= 3) {
                    this.setPurchaseState(PurchaseState.FIND_LISTING);
                    break;
                }
                this.retryCount = 0;
                this.lastActionTime = this.getLastSearchTime();
                this.setPurchaseState(PurchaseState.WAIT_FOR_RESULT);
                break;
            }
            case 3: {
                this.openPurchaseScreen();
                break;
            }
            case 4: {
                this.handlePurchaseScreen();
                break;
            }
            case 5: {
                if (this.purchaseConfirmed) {
                    this.purchaseConfirmed = false;
                    this.setPurchaseState(PurchaseState.FIND_LISTING);
                    break;
                }
                if (this.getLastSearchTime() - this.lastActionTime < 8000L) break;
                this.setPurchaseState(PurchaseState.OPEN_INVENTORY);
                break;
            }
            case 6: {
                if (!this.hasElapsed(this.getLastActionTime())) {
                    return;
                }
                if (!this.isChatMessage("ah")) {
                    return;
                }
                this.setPurchaseState(PurchaseState.COLLECT_PURCHASE);
                break;
            }
            case 7: {
                HandledScreen<?> BlockStateProviderType = this.getCurrentScreen();
                if (BlockStateProviderType != null) {
                    int n = this.findSlotByName(BlockStateProviderType.getScreenHandler(), STORE_NAME);
                    if (n != -1) {
                        if (!this.hasElapsed(this.getLastActionTime())) break;
                        PurchaseTracker.minecraftClient.interactionManager.clickSlot(BlockStateProviderType.getScreenHandler().syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)PurchaseTracker.minecraftClient.player);
                        this.clickIndex = 0;
                        this.setPurchaseState(PurchaseState.FINISH_PURCHASE);
                        break;
                    }
                    if (!this.hasElapsed(5000L)) break;
                    this.returnToSearch();
                    break;
                }
                if (!this.hasElapsed(5000L)) break;
                this.returnToSearch();
                break;
            }
            case 8: {
                this.handleConfirmationScreen();
            }
        }
    }

    private void openPurchaseScreen() {
        HandledScreen<?> BlockStateProviderType = this.getCurrentScreen();
        if (BlockStateProviderType == null) {
            this.setPurchaseState(PurchaseState.FIND_LISTING);
            return;
        }
        if (!this.hasElapsed(this.getLastActionTime())) {
            return;
        }
        ScreenHandler class_17032 = BlockStateProviderType.getScreenHandler();
        int n = this.findFirstContainerSlot(class_17032);
        if (n == -1 || this.findMatchingSlot(class_17032) == -1) {
            this.setPurchaseState(PurchaseState.CONFIRM_PURCHASE);
            return;
        }
        this.clickPurchaseSlot(class_17032, n, Math.max(1, this.activeEntry.getSellQuantity()));
        this.sendNextPurchaseAction();
    }

    private void clickPurchaseSlot(ScreenHandler class_17032, int n, int n2) {
        int n3 = n2 + 64;
        for (int i = n2; i > 0 && n3-- > 0; --i) {
            ItemStack class_17992 = class_17032.getCursorStack();
            if (!this.matchesPurchaseEntry(class_17992)) {
                int n4 = this.findMatchingSlot(class_17032);
                if (n4 == -1) break;
                PurchaseTracker.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n4, 0, SlotActionType.PICKUP, (PlayerEntity)PurchaseTracker.minecraftClient.player);
                class_17992 = class_17032.getCursorStack();
                if (class_17992.isEmpty()) break;
            }
            PurchaseTracker.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n, 1, SlotActionType.PICKUP, (PlayerEntity)PurchaseTracker.minecraftClient.player);
        }
        if (!class_17032.getCursorStack().isEmpty()) {
            int n5 = this.findPlayerContainerSlot(class_17032);
            if (n5 == -1) {
                n5 = this.findMatchingSlot(class_17032);
            }
            if (n5 != -1) {
                PurchaseTracker.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n5, 0, SlotActionType.PICKUP, (PlayerEntity)PurchaseTracker.minecraftClient.player);
            }
        }
    }

    private void handlePurchaseScreen() {
        HandledScreen<?> BlockStateProviderType = this.getCurrentScreen();
        if (BlockStateProviderType == null) {
            this.containerPrepared = true;
            this.searchTimedOut = true;
            this.lastActionTime = this.getLastSearchTime();
            this.setPurchaseState(PurchaseState.WAIT_FOR_RESULT);
            return;
        }
        if (!this.hasElapsed(this.getLastActionTime())) {
            return;
        }
        int n = this.findSlotForItem(BlockStateProviderType.getScreenHandler(), Items.LIME_DYE);
        if (n != -1) {
            PurchaseTracker.minecraftClient.interactionManager.clickSlot(BlockStateProviderType.getScreenHandler().syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)PurchaseTracker.minecraftClient.player);
            this.containerPrepared = true;
            this.searchTimedOut = true;
            this.closeCurrentScreen();
            this.lastActionTime = this.getLastSearchTime();
            this.setPurchaseState(PurchaseState.WAIT_FOR_RESULT);
        } else if (this.hasElapsed(5000L)) {
            this.containerPrepared = true;
            this.searchTimedOut = true;
            this.closeCurrentScreen();
            this.lastActionTime = this.getLastSearchTime();
            this.setPurchaseState(PurchaseState.WAIT_FOR_RESULT);
        }
    }

    private void handleConfirmationScreen() {
        HandledScreen<?> BlockStateProviderType = this.getCurrentScreen();
        if (BlockStateProviderType == null) {
            this.setPurchaseState(this.activeEntry == null ? PurchaseState.IDLE : PurchaseState.FIND_LISTING);
            return;
        }
        if (!this.hasElapsed(this.getLastActionTime())) {
            return;
        }
        int n = this.findOtherContainerSlot(BlockStateProviderType.getScreenHandler());
        if (n != -1) {
            PurchaseTracker.minecraftClient.interactionManager.clickSlot(BlockStateProviderType.getScreenHandler().syncId, n, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)PurchaseTracker.minecraftClient.player);
            ++this.clickIndex;
            this.sendNextPurchaseAction();
        } else {
            this.closeCurrentScreen();
            if (this.activeEntry == null) {
                this.setPurchaseState(PurchaseState.IDLE);
            } else if (this.clickIndex == 0 && this.countOwnedItems(this.activeEntry) <= 0) {
                this.resetSearch();
            } else {
                this.setPurchaseState(PurchaseState.FIND_LISTING);
            }
        }
    }

    private void returnToSearch() {
        if (this.activeEntry != null) {
            this.lastActionTime = this.getLastSearchTime();
            this.setPurchaseState(PurchaseState.WAIT_FOR_RESULT);
        } else {
            this.resetSearch();
        }
    }

    private void resetSearch() {
        this.activeEntry = null;
        this.containerPrepared = false;
        this.setPurchaseState(PurchaseState.IDLE);
    }

    private ItemCatalog.CatalogEntry findPurchaseCandidate() {
        for (ItemCatalog.CatalogEntry catalogEntry : ItemCatalog.getEntries()) {
            int n;
            int n2 = n = this.inventoryScanActive ? 1 : catalogEntry.getResellThreshold();
            if (this.getAvailableQuantity(catalogEntry) <= 0L || this.countOwnedItems(catalogEntry) < n) continue;
            return catalogEntry;
        }
        return null;
    }

    private long getEntryPurchaseTime() {
        return Math.max(1L, this.getAvailableQuantity(this.activeEntry));
    }

    private long getAvailableQuantity(ItemCatalog.CatalogEntry catalogEntry) {
        if (catalogEntry == null) {
            return 0L;
        }
        if (catalogEntry.getSellPrice() > 0L) {
            return catalogEntry.getSellPrice();
        }
        long l = this.priceResolver.applyAsLong(catalogEntry);
        return l > 0L ? l : 0L;
    }

    public void handlePurchaseMessage(String string) {
        if (string.contains("\u0423 \u0412\u0430\u0441 \u043a\u0443\u043f\u0438\u043b\u0438")) {
            this.purchaseConfirmed = true;
        } else if (string.contains("\u0441\u043b\u0438\u0448\u043a\u043e\u043c \u0434\u043e\u0440\u043e\u0433\u043e")) {
            this.resetSearch();
        }
    }

    private boolean matchesPurchaseEntry(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return false;
        }
        if (this.activeEntry == null) {
            return ItemCatalog.findEntry(class_17992) != null;
        }
        return ItemCatalog.matchesEntry(class_17992, this.activeEntry);
    }

    private int countOwnedItems(ItemCatalog.CatalogEntry catalogEntry) {
        PlayerInventory class_16612 = PurchaseTracker.minecraftClient.player.getInventory();
        int n = 0;
        for (int i = 0; i < class_16612.size(); ++i) {
            ItemStack class_17992 = class_16612.getStack(i);
            if (!ItemCatalog.matchesEntry(class_17992, catalogEntry)) continue;
            n += class_17992.getCount();
        }
        return n;
    }

    private int findFirstContainerSlot(ScreenHandler class_17032) {
        for (Slot class_17352 : class_17032.slots) {
            if (class_17352.inventory == PurchaseTracker.minecraftClient.player.getInventory() || class_17352.hasStack()) continue;
            return class_17352.id;
        }
        return -1;
    }

    private int findPlayerContainerSlot(ScreenHandler class_17032) {
        for (Slot class_17352 : class_17032.slots) {
            if (class_17352.inventory != PurchaseTracker.minecraftClient.player.getInventory() || class_17352.hasStack()) continue;
            return class_17352.id;
        }
        return -1;
    }

    private int findMatchingSlot(ScreenHandler class_17032) {
        for (Slot class_17352 : class_17032.slots) {
            if (class_17352.inventory != PurchaseTracker.minecraftClient.player.getInventory() || !this.matchesPurchaseEntry(class_17352.getStack())) continue;
            return class_17352.id;
        }
        return -1;
    }

    private int findOtherContainerSlot(ScreenHandler class_17032) {
        for (Slot class_17352 : class_17032.slots) {
            if (class_17352.inventory == PurchaseTracker.minecraftClient.player.getInventory() || !this.matchesPurchaseEntry(class_17352.getStack())) continue;
            return class_17352.id;
        }
        return -1;
    }

    private int findSlotForItem(ScreenHandler class_17032, Item class_17922) {
        for (Slot class_17352 : class_17032.slots) {
            if (class_17352.inventory == PurchaseTracker.minecraftClient.player.getInventory() || !class_17352.getStack().isOf(class_17922)) continue;
            return class_17352.id;
        }
        return -1;
    }

    private int findSlotByName(ScreenHandler class_17032, String string) {
        for (Slot class_17352 : class_17032.slots) {
            if (class_17352.inventory == PurchaseTracker.minecraftClient.player.getInventory() || !class_17352.hasStack() || !class_17352.getStack().getName().getString().contains(string)) continue;
            return class_17352.id;
        }
        return -1;
    }

    private HandledScreen<?> getCurrentScreen() {
        HandledScreen BlockStateProviderType;
        Screen class_4372 = PurchaseTracker.minecraftClient.currentScreen;
        return class_4372 instanceof HandledScreen ? (BlockStateProviderType = (HandledScreen)class_4372) : null;
    }

    private void closeCurrentScreen() {
        if (PurchaseTracker.minecraftClient.currentScreen != null && PurchaseTracker.minecraftClient.player != null) {
            PurchaseTracker.minecraftClient.player.closeHandledScreen();
        }
    }

    private boolean isChatMessage(String string) {
        if (this.getLastSearchTime() - this.nextActionTime < 900L) {
            return false;
        }
        this.closeCurrentScreen();
        PurchaseTracker.minecraftClient.player.networkHandler.sendChatCommand(string);
        this.nextActionTime = this.getLastSearchTime();
        return true;
    }

    private long getLastActionTime() {
        return (long)((float)PurchaseTracker.minecraftClient.player.networkHandler.getPlayerListEntry(PurchaseTracker.minecraftClient.player.getUuid()).getLatency() * 2.5f + MathUtils.interpolateRandomDouble(24.0, 59.0));
    }

    private long getLastSearchTime() {
        return System.currentTimeMillis();
    }

    private boolean hasElapsed(long l) {
        return this.getLastSearchTime() - this.purchaseStartTime >= l;
    }

    private void sendNextPurchaseAction() {
        this.purchaseStartTime = this.getLastSearchTime();
    }

    private void setPurchaseState(PurchaseState purchaseState) {
        this.purchaseState = purchaseState;
        this.sendNextPurchaseAction();
    }

    private static long parsePrice(String string) {
        String string2;
        Matcher matcher = pricePattern.matcher(string);
        if (matcher.find() && !(string2 = matcher.group(1).replaceAll("[^\\d]", "")).isEmpty()) {
            try {
                return Long.parseLong(string2);
            }
            catch (NumberFormatException numberFormatException) {
                return 0L;
            }
        }
        return 0L;
    }

    private boolean isPurchaseEnvironmentReady() {
        HandledScreen<?> BlockStateProviderType = this.getCurrentScreen();
        return BlockStateProviderType != null && MarketInventoryAnalyzer.isPriceOrMarketText(BlockStateProviderType.getTitle().getString());
    }

    @Generated
    public PurchaseState getPurchaseState() {
        return this.purchaseState;
    }

    static enum PurchaseState {
        IDLE,
        FIND_LISTING,
        OPEN_LISTING,
        SELECT_LISTING,
        CONFIRM_PURCHASE,
        WAIT_FOR_RESULT,
        OPEN_INVENTORY,
        COLLECT_PURCHASE,
        FINISH_PURCHASE;
}
}

