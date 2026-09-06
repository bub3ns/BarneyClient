/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  lombok.Generated
 *  net.minecraft.StatusEffect
 *  net.minecraft.StatusEffectInstance
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ScreenHandler
 *  net.minecraft.SlotActionType
 *  net.minecraft.PlayerScreenHandler
 *  net.minecraft.Item
 *  net.minecraft.Item$TooltipContext
 *  net.minecraft.ItemStack
 *  net.minecraft.TooltipType
 *  net.minecraft.Potion
 *  net.minecraft.World
 *  net.minecraft.Text
 *  net.minecraft.Packet
 *  net.minecraft.CloseHandledScreenC2SPacket
 *  net.minecraft.Identifier
 *  net.minecraft.HandledScreen
 *  net.minecraft.GameMessageS2CPacket
 *  net.minecraft.Registries
 */
package moscow.rockstar.modules.other.inventory;
import moscow.rockstar.ui.localization.Localization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.items.ItemMetadataUtils;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.other.inventory.InventoryBuildState;
import moscow.rockstar.modules.other.inventory.InventoryPresetEntry;
import moscow.rockstar.modules.other.inventory.InventoryPresetLayout;
import moscow.rockstar.modules.other.inventory.InventoryTraitLimit;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.settings.ActionSetting;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.ui.screens.InventoryBuilderScreen;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.registry.Registries;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.window.KeyPressEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Inventory Builder", category=ModuleCategory.OTHER, description="modules.descriptions.inventory_builder")
public class InventoryBuilder
extends Module {
    public static final int PRESET_SLOT_COUNT = 41;
    public static final int HOTBAR_SLOT_START = 36;
    public static final int LAST_PRESET_SLOT_INDEX = 40;
    private static final List<String> PRESET_NAMES = List.of("gear", "spheres", "runes", "explosives", "potions", "backpacks", "other");
    private static final List<InventoryPresetLayout> loadedPresets = new ArrayList<InventoryPresetLayout>();
    private static final File presetDirectory = new File(new File(moscow.rockstar.core.ClientPaths.gameDirectory(), "presets"), "invbuilder");
    private static final String PRESET_FILE_EXTENSION = ".rock";
    private static final File legacyPresetFile = new File(moscow.rockstar.core.ClientPaths.gameDirectory(), "loadouts.json");
    private static final Pattern reservedFileNamePattern = Pattern.compile("(?i)(con|prn|aux|nul|com\\d|lpt\\d)");
    private static boolean presetsLoaded;
    private ActionSetting open;
    private ActionSetting stop;
    private NumberSetting delay;
    private NumberSetting attempts;
    private NumberSetting pages;
    private BooleanSetting sort;
    private static final Pattern listingItemNamePattern;
    private static final Pattern listingChatPattern;
    private static final Pattern durationPattern;
    private static final Pattern availableSlotsPattern;
    private static final long MAX_DELAY_JITTER_MILLIS = 50L;
    private long delayJitterMillis;
    private final Timer buildCooldownTimer = new Timer();
    private final Timer buildActionTimer = new Timer();
    private final Deque<int[]> containerActionHistory = new ArrayDeque<int[]>();
    private boolean searchResultDetected;
    private int searchPage;
    private int pageRetryCount;
    private boolean awaitingSearchResponse;
    private int currentPageOffset;
    private int currentItemIndex;
    private InventoryBuildState buildState = InventoryBuildState.IDLE;
    private InventoryPresetLayout activePreset;
    private int activeSlotIndex;
    private int searchAttempts;
    private int purchasedAmount;
    private int selectedPurchaseSlot = -1;
    private int previousSortSlot = -1;
    private long currentListingPrice;
    private static final int MAX_EMPTY_SEARCH_PAGES = 3;
    private static final int INITIAL_AUCTION_SLOT = 8;
    private int lastAuctionSlot = -1;
    private int currentAuctionSlot = -1;
    private int fallbackHotbarSlot = 8;
    private int containerPageStage;
    private int pendingContainerSlot = -1;
    private String pendingSlotDescription = "";
    private int stalledSlotChecks;
    private final Set<Integer> occupiedPresetSlots = new HashSet<Integer>();
    private int completedItemCount;
    private final Set<String> purchasedListingKeys = new HashSet<String>();
    private String statusMessage = "";
    private volatile boolean purchaseResponseReceived;
    private volatile String responseMessage = "";
    private int insufficientFundsResponses;
    private final EventListener<KeyPressEvent> keyPressListener = keyPressEvent -> {
        if (!this.isBuilderReady() || InventoryBuilder.minecraftClient.currentScreen == null) {
            return;
        }
        if (keyPressEvent.getKey() != 256 || keyPressEvent.getAction() != 1) {
            return;
        }
        this.cancelBuild(Localization.translate("inventory_builder.cancel_esc"));
    };
    private final EventListener<ReceivePacketEvent> receivePacketListener = receivePacketEvent -> {
        boolean bl;
        if (this.buildState == InventoryBuildState.IDLE) {
            return;
        }
        Object object = receivePacketEvent.getPacket();
        if (!(object instanceof GameMessageS2CPacket)) {
            return;
        }
        GameMessageS2CPacket class_74392 = (GameMessageS2CPacket)object;
        object = class_74392.content().getString().toLowerCase(Locale.ROOT);
        if (this.buildState == InventoryBuildState.SEARCH_RESULTS) {
            if (((String)object).contains("\u043d\u0435 \u0431\u044b\u043b\u043e \u043d\u0430\u0439\u0434\u0435\u043d\u043e") || ((String)object).contains("\u043d\u0438\u0447\u0435\u0433\u043e \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e")) {
                this.searchResultDetected = true;
            }
            return;
        }
        if (this.buildState != InventoryBuildState.PURCHASE_PENDING && this.buildState != InventoryBuildState.PURCHASE_CONFIRMED && this.buildState != InventoryBuildState.NEXT_PAGE) {
            return;
        }
        if ((((String)object).contains("\u043d\u0435\u0434\u043e\u0441\u0442\u0430\u0442\u043e\u0447\u043d\u043e") || ((String)object).contains("\u043d\u0435 \u0445\u0432\u0430\u0442\u0430\u0435\u0442")) && (((String)object).contains("\u0441\u0440\u0435\u0434\u0441\u0442\u0432") || ((String)object).contains("\u0434\u0435\u043d\u0435\u0433") || ((String)object).contains("\u043c\u043e\u043d\u0435\u0442"))) {
            ++this.insufficientFundsResponses;
            this.purchaseResponseReceived = true;
            this.responseMessage = Localization.translate("inventory_builder.not_enough_money_lot");
            return;
        }
        boolean bl2 = bl = ((String)object).contains("\u0443\u0436\u0435 \u043a\u0443\u043f") || ((String)object).contains("\u0443\u0436\u0435 \u043f\u0440\u043e\u0434\u0430") || ((String)object).contains("\u0443\u0441\u043f\u0435\u043b") || ((String)object).contains("\u043f\u0435\u0440\u0435\u043a\u0443\u043f") || ((String)object).contains("\u043b\u043e\u0442") && (((String)object).contains("\u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d") || ((String)object).contains("\u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442") || ((String)object).contains("\u0441\u043d\u044f\u0442") || ((String)object).contains("\u0438\u0441\u0442\u0451\u043a") || ((String)object).contains("\u0438\u0441\u0442\u0435\u043a"));
        if (bl) {
            this.purchaseResponseReceived = true;
            this.responseMessage = Localization.translate("inventory_builder.listing_sniped");
        }
    };
    private int pendingPurchaseAmount;
    private static final List<InventoryPresetEntry> catalogEntries;

    public static String formatPresetName(String string) {
        return string == null ? "" : string.trim();
    }

    public static int findPresetIndex(String string) {
        int n = string == null ? -1 : PRESET_NAMES.indexOf(string);
        return n < 0 ? PRESET_NAMES.size() : n;
    }

    public InventoryBuilder() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.open = new ActionSetting(this, "modules.settings.inventory_builder.open").withAction(() -> minecraftClient.setScreen(new InventoryBuilderScreen()));
        this.stop = new ActionSetting((SettingOwner)this, "modules.settings.inventory_builder.stop", () -> !this.isBuilderReady()).withAction(() -> this.cancelBuild(Localization.translate("inventory_builder.stopped_manually")));
        this.delay = new NumberSetting(this, "modules.settings.inventory_builder.delay").setMinValue(100.0f).setMaxValue(1500.0f).setStep(50.0f).setValue(300.0f).setUnit("ms");
        this.attempts = new NumberSetting(this, "modules.settings.inventory_builder.attempts").setMinValue(1.0f).setMaxValue(15.0f).setStep(1.0f).setValue(6.0f);
        this.pages = new NumberSetting(this, "modules.settings.inventory_builder.pages").setMinValue(1.0f).setMaxValue(30.0f).setStep(1.0f).setValue(6.0f);
        this.sort = new BooleanSetting(this, "modules.settings.inventory_builder.sort");
    }

    public boolean isBuilderReady() {
        return this.buildState != InventoryBuildState.IDLE;
    }

    public void startBuild(InventoryPresetLayout inventoryPresetLayout) {
        if (InventoryBuilder.minecraftClient.player == null || inventoryPresetLayout == null) {
            return;
        }
        if (inventoryPresetLayout.getFilledSlotCount() == 0) {
            Notification.warning(Text.of((String)Localization.translate("inventory_builder.preset_empty")));
            return;
        }
        this.activePreset = inventoryPresetLayout;
        this.activeSlotIndex = 0;
        this.searchAttempts = 0;
        this.completedItemCount = 0;
        this.currentListingPrice = 0L;
        this.containerActionHistory.clear();
        this.occupiedPresetSlots.clear();
        this.purchasedListingKeys.clear();
        this.statusMessage = "";
        this.purchaseResponseReceived = false;
        this.insufficientFundsResponses = 0;
        this.stalledSlotChecks = 0;
        this.pendingContainerSlot = -1;
        this.containerPageStage = 0;
        this.searchResultDetected = false;
        this.buildState = InventoryBuildState.SEARCHING;
        this.buildActionTimer.reset();
        this.buildCooldownTimer.reset();
        if (!this.isEnabled()) {
            this.enable();
        }
        Notification.info(Text.of((String)Localization.translateFormatted("inventory_builder.build_started", inventoryPresetLayout.layoutName, inventoryPresetLayout.getFilledSlotCount(), InventoryBuilder.formatSlotCount(inventoryPresetLayout.getFilledSlotCount()))));
    }

    public void cancelBuild(String string) {
        if (this.buildState == InventoryBuildState.IDLE) {
            return;
        }
        this.buildState = InventoryBuildState.IDLE;
        this.activePreset = null;
        this.containerActionHistory.clear();
        Notification.info(Text.of((String)Localization.translateFormatted("inventory_builder.build_stopped", string, this.completedItemCount)));
    }

    private void finishBuild() {
        if (this.buildState == InventoryBuildState.IDLE) {
            return;
        }
        String string = this.activePreset == null ? "" : this.activePreset.layoutName;
        this.buildState = InventoryBuildState.IDLE;
        this.activePreset = null;
        this.containerActionHistory.clear();
        Notification.info(Text.of((String)Localization.translateFormatted("inventory_builder.build_finished", string, this.completedItemCount, InventoryBuilder.formatItemCount(this.completedItemCount))));
    }

    private static String formatSlotCount(int n) {
        int n2 = n % 100;
        int n3 = n % 10;
        if (n2 >= 11 && n2 <= 14) {
            return Localization.translate("inventory_builder.slots_many");
        }
        return n3 == 1 ? Localization.translate("inventory_builder.slot_one") : (n3 >= 2 && n3 <= 4 ? Localization.translate("inventory_builder.slots_few") : Localization.translate("inventory_builder.slots_many"));
    }

    private static String formatItemCount(int n) {
        int n2 = n % 100;
        int n3 = n % 10;
        if (n2 >= 11 && n2 <= 14) {
            return Localization.translate("inventory_builder.items_many");
        }
        return n3 == 1 ? Localization.translate("inventory_builder.item_one") : (n3 >= 2 && n3 <= 4 ? Localization.translate("inventory_builder.items_few") : Localization.translate("inventory_builder.items_many"));
    }

    @Override
    public void onTick() {
        if (this.buildState == InventoryBuildState.IDLE) {
            return;
        }
        if (InventoryBuilder.minecraftClient.player == null || InventoryBuilder.minecraftClient.world == null) {
            this.cancelBuild(Localization.translate("inventory_builder.no_player"));
            return;
        }
        if (!this.buildCooldownTimer.hasElapsed(Math.max(50L, (long)this.delay.getValue() + this.delayJitterMillis))) {
            return;
        }
        this.buildCooldownTimer.reset();
        this.delayJitterMillis = ThreadLocalRandom.current().nextLong(-50L, 51L);
        if (this.purchaseResponseReceived) {
            this.purchaseResponseReceived = false;
            if (this.insufficientFundsResponses >= 3) {
                this.cancelBuild(Localization.translate("inventory_builder.no_money"));
                return;
            }
            if (this.buildState == InventoryBuildState.PURCHASE_PENDING || this.buildState == InventoryBuildState.PURCHASE_CONFIRMED || this.buildState == InventoryBuildState.NEXT_PAGE) {
                this.notifyBuildFailure(this.responseMessage.isEmpty() ? Localization.translate("inventory_builder.listing_not_bought") : this.responseMessage);
                return;
            }
        }
        switch (this.buildState.ordinal()) {
            case 1: {
                this.advanceLayout();
                break;
            }
            case 2: {
                this.processSearchResults();
                break;
            }
            case 3: {
                this.processPurchaseResult();
                break;
            }
            case 4: {
                this.handleBuildFailure();
                break;
            }
            case 5: {
                this.handleBuildComplete();
                break;
            }
            case 6: {
                this.resetCurrentItem();
                break;
            }
        }
    }

    private void advanceLayout() {
        while (this.activeSlotIndex < 41 && (this.activePreset.slotEntries[this.activeSlotIndex] == null || this.isIndexValid(this.activeSlotIndex))) {
            ++this.activeSlotIndex;
        }
        if (this.activeSlotIndex >= 41) {
            if (this.sort.isEnabled()) {
                this.buildState = InventoryBuildState.SORTING;
                this.containerActionHistory.clear();
                this.previousSortSlot = -1;
                this.currentItemIndex = 0;
                this.buildActionTimer.reset();
                this.closeAuctionContainer();
            } else {
                this.finishBuild();
            }
            return;
        }
        InventoryPresetEntry inventoryPresetEntry = this.activePreset.slotEntries[this.activeSlotIndex];
        int n = this.calculateEntryCount(inventoryPresetEntry) - this.getRequiredCount(inventoryPresetEntry);
        if (n <= 0) {
            this.finalizeBuild();
            return;
        }
        if (this.searchAttempts >= (int)this.attempts.getValue()) {
            Notification.warning(Text.of((String)Localization.translateFormatted("inventory_builder.buy_failed", inventoryPresetEntry.getFormattedDisplayName(), n)));
            this.finalizeBuild();
            return;
        }
        this.closeAuctionContainer();
        this.currentPageOffset = 0;
        this.searchPage = 0;
        this.pageRetryCount = 0;
        this.awaitingSearchResponse = false;
        InventoryBuilder.minecraftClient.player.networkHandler.sendChatCommand("ah search " + this.formatSearchQuery(inventoryPresetEntry));
        this.buildState = InventoryBuildState.SEARCH_RESULTS;
        this.buildActionTimer.reset();
    }

    private void processSearchResults() {
        int n;
        int n2;
        int n3;
        InventoryPresetEntry inventoryPresetEntry;
        int n4;
        if (this.searchResultDetected) {
            this.searchResultDetected = false;
            InventoryPresetEntry inventoryPresetEntry2 = this.activePreset.slotEntries[this.activeSlotIndex];
            if (inventoryPresetEntry2 != null) {
                Notification.warning(Text.of((String)Localization.translateFormatted("inventory_builder.not_found_auction", inventoryPresetEntry2.getFormattedDisplayName())));
            }
            this.finalizeBuild();
            this.buildState = InventoryBuildState.SEARCHING;
            return;
        }
        if (this.buildActionTimer.hasElapsed(6000L)) {
            ++this.searchAttempts;
            this.buildState = InventoryBuildState.SEARCHING;
            return;
        }
        ScreenHandler class_17032 = this.getAuctionContainer();
        if (class_17032 == null) {
            return;
        }
        if (this.getOpenContainerTitle().contains("\u043f\u043e\u043a\u0443\u043f\u043a")) {
            this.buildState = InventoryBuildState.PURCHASE_PENDING;
            this.buildActionTimer.reset();
            return;
        }
        int[] nArray = this.findAvailableSlotRange();
        if (this.searchPage > 0) {
            boolean bl;
            boolean bl2 = bl = nArray[1] > 0;
            if (bl && nArray[0] != this.searchPage) {
                return;
            }
            if (!bl && !this.buildActionTimer.hasElapsed(700L)) {
                return;
            }
            this.searchPage = 0;
            this.awaitingSearchResponse = false;
            this.buildActionTimer.reset();
        }
        if ((n4 = this.calculateEntryCount(inventoryPresetEntry = this.activePreset.slotEntries[this.activeSlotIndex]) - this.getRequiredCount(inventoryPresetEntry)) <= 0) {
            this.closeAuctionContainer();
            this.finalizeBuild();
            this.buildState = InventoryBuildState.SEARCHING;
            return;
        }
        int n5 = class_17032.slots.size() - 36;
        if (!this.awaitingSearchResponse) {
            n3 = 1;
            for (n2 = 0; n2 < n5; ++n2) {
                if (class_17032.getSlot(n2).getStack().isEmpty()) continue;
                n3 = 0;
                break;
            }
            if (n3 != 0 || !this.buildActionTimer.hasElapsed(Math.max(400L, (long)this.delay.getValue()))) {
                return;
            }
            this.awaitingSearchResponse = true;
        }
        if (inventoryPresetEntry.requiredAmount > 1 && this.pageRetryCount < 8) {
            n3 = this.findSortingSlot(class_17032, n5);
            if (n3 >= 0 && !this.isDiscountApproved(class_17032.getSlot(n3).getStack())) {
                ++this.pageRetryCount;
                InventoryBuilder.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n3, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryBuilder.minecraftClient.player);
                this.awaitingSearchResponse = false;
                this.buildActionTimer.reset();
                return;
            }
            if (n3 >= 0) {
                this.pageRetryCount = 0;
            }
        }
        n3 = -1;
        n2 = 0;
        long l = Long.MAX_VALUE;
        String string = "";
        for (n = 0; n < n5; ++n) {
            String string2;
            int n6;
            long l2;
            List<Text> list;
            ItemStack class_17992 = class_17032.getSlot(n).getStack();
            if (!this.isItemMatch(class_17992, inventoryPresetEntry) || !this.isListingValid(class_17992, list = this.getItemLore(class_17992), inventoryPresetEntry) || (l2 = this.extractListingPrice(list, n6 = Math.max(1, class_17992.getCount()))) < 0L || inventoryPresetEntry.maximumPrice > 0L && l2 > inventoryPresetEntry.maximumPrice || this.purchasedListingKeys.contains(string2 = this.formatListingKey(class_17992, list, l2)) || l2 >= l) continue;
            l = l2;
            n3 = n;
            n2 = n6;
            string = string2;
        }
        if (n3 >= 0) {
            this.purchasedAmount = this.getRequiredCount(inventoryPresetEntry);
            this.selectedPurchaseSlot = -1;
            this.pendingPurchaseAmount = 0;
            this.currentListingPrice = l;
            this.statusMessage = string;
            this.purchaseResponseReceived = false;
            this.containerActionHistory.clear();
            if (n2 <= n4) {
                InventoryBuilder.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n3, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryBuilder.minecraftClient.player);
            } else {
                InventoryBuilder.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n3, 1, SlotActionType.PICKUP, (PlayerEntity)InventoryBuilder.minecraftClient.player);
                this.pendingPurchaseAmount = n4;
            }
            this.buildState = InventoryBuildState.PURCHASE_PENDING;
            this.buildActionTimer.reset();
            return;
        }
        n = this.findNextPageSlot(class_17032, n5);
        if (n >= 0 && this.currentPageOffset + 1 < (int)this.pages.getValue() && (nArray[1] <= 0 || nArray[0] < nArray[1])) {
            ++this.currentPageOffset;
            this.searchPage = nArray[0] + 1;
            InventoryBuilder.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryBuilder.minecraftClient.player);
            this.buildActionTimer.reset();
            return;
        }
        Notification.warning(Text.of((String)Localization.translateFormatted(this.purchasedListingKeys.isEmpty() ? "inventory_builder.no_matching_listings" : "inventory_builder.all_listings_taken", inventoryPresetEntry.getFormattedDisplayName())));
        this.closeAuctionContainer();
        this.finalizeBuild();
        this.buildState = InventoryBuildState.SEARCHING;
    }

    private void processPurchaseResult() {
        int n;
        if (this.buildActionTimer.hasElapsed(5000L)) {
            this.notifyBuildFailure(Localization.translate("inventory_builder.listing_sniped"));
            return;
        }
        ScreenHandler class_17032 = this.getAuctionContainer();
        if (class_17032 == null || !this.getOpenContainerTitle().contains("\u043f\u043e\u043a\u0443\u043f\u043a")) {
            return;
        }
        int n2 = class_17032.slots.size() - 36;
        int n3 = -1;
        int n4 = -1;
        int n5 = -1;
        int n6 = -1;
        int n7 = Integer.MAX_VALUE;
        int n8 = -1;
        boolean[] blArray = new boolean[n2];
        for (n = 0; n < n2; ++n) {
            ItemStack class_17992 = class_17032.getSlot(n).getStack();
            if (class_17992.isEmpty()) continue;
            String string = class_17992.getName().getString().toLowerCase(Locale.ROOT);
            Matcher matcher = listingItemNamePattern.matcher(string);
            Matcher matcher2 = listingChatPattern.matcher(string);
            boolean bl = false;
            if (matcher.find()) {
                int n9 = Integer.parseInt(matcher.group(1));
                if (n9 == 1) {
                    n3 = n;
                } else if (n9 == 10) {
                    n4 = n;
                }
                bl = true;
            } else if (matcher2.find()) {
                if (Integer.parseInt(matcher2.group(1)) == 1) {
                    n5 = n;
                }
                bl = true;
            } else if (string.contains("\u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0434") || string.contains("\u043a\u0443\u043f\u0438\u0442\u044c") || string.contains("\u043f\u0440\u0438\u043e\u0431\u0440\u0435\u0441\u0442")) {
                n6 = n;
            }
            if (!bl) continue;
            blArray[n] = true;
            n7 = Math.min(n7, n);
            n8 = Math.max(n8, n);
        }
        if (n6 == -1 && n8 != -1) {
            for (n = n7 + 1; n < n8; ++n) {
                if (blArray[n] || class_17032.getSlot(n).getStack().isEmpty()) continue;
                n6 = n;
                break;
            }
        }
        if (n6 == -1) {
            this.handlePurchaseResponse(class_17032, n2, Localization.translate("inventory_builder.no_confirm_button"));
            return;
        }
        if (this.pendingPurchaseAmount > 1 && n3 == -1 && n4 == -1) {
            this.handlePurchaseResponse(class_17032, n2, Localization.translate("inventory_builder.no_amount_buttons"));
            return;
        }
        this.selectedPurchaseSlot = n6;
        if (this.pendingPurchaseAmount > 1) {
            this.updatePlacement(this.pendingPurchaseAmount - 1, n3, n4, n5);
            this.pendingPurchaseAmount = 0;
            this.buildState = InventoryBuildState.PURCHASE_CONFIRMED;
            this.buildActionTimer.reset();
            return;
        }
        this.pendingPurchaseAmount = 0;
        InventoryBuilder.minecraftClient.interactionManager.clickSlot(class_17032.syncId, this.selectedPurchaseSlot, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryBuilder.minecraftClient.player);
        this.buildState = InventoryBuildState.NEXT_PAGE;
        this.buildActionTimer.reset();
    }

    private void handlePurchaseResponse(ScreenHandler class_17032, int n, String string) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            ItemStack class_17992 = class_17032.getSlot(i).getStack();
            if (class_17992.isEmpty()) continue;
            stringBuilder.append(i).append('=').append(class_17992.getName().getString()).append("; ");
        }
        RockstarClient.LOGGER.warn("[InventoryBuilder] purchase menu: {} | slots: {}", (Object)string, (Object)stringBuilder);
        Notification.warning(Text.of((String)Localization.translateFormatted("inventory_builder.purchase_menu_skipped", string)));
        ++this.searchAttempts;
        this.pendingPurchaseAmount = 0;
        this.containerActionHistory.clear();
        this.closeAuctionContainer();
        this.buildState = InventoryBuildState.SEARCHING;
        this.buildActionTimer.reset();
    }

    private void updatePlacement(int n, int n2, int n3, int n4) {
        int n5;
        int n6;
        if (n <= 0) {
            return;
        }
        if (n3 == -1) {
            for (int i = 0; i < n && n2 != -1; ++i) {
                this.containerActionHistory.add(new int[]{n2, 0});
            }
            return;
        }
        int n7 = n / 10;
        int n8 = n % 10;
        boolean bl = n8 > 0 && n4 != -1;
        boolean bl2 = n8 == 0 || n2 != -1;
        int n9 = bl ? n7 + 1 + (10 - n8) : Integer.MAX_VALUE;
        int n10 = n6 = bl2 ? n7 + n8 : Integer.MAX_VALUE;
        if (n9 <= n6) {
            int n11;
            for (n11 = 0; n11 <= n7; ++n11) {
                this.containerActionHistory.add(new int[]{n3, 0});
            }
            for (n11 = 0; n11 < 10 - n8; ++n11) {
                this.containerActionHistory.add(new int[]{n4, 0});
            }
            return;
        }
        for (n5 = 0; n5 < n7; ++n5) {
            this.containerActionHistory.add(new int[]{n3, 0});
        }
        for (n5 = 0; n5 < n8 && n2 != -1; ++n5) {
            this.containerActionHistory.add(new int[]{n2, 0});
        }
    }

    private void handleBuildFailure() {
        ScreenHandler class_17032 = this.getAuctionContainer();
        if (class_17032 == null || !this.getOpenContainerTitle().contains("\u043f\u043e\u043a\u0443\u043f\u043a")) {
            this.containerActionHistory.clear();
            this.buildState = InventoryBuildState.NEXT_PAGE;
            this.buildActionTimer.reset();
            return;
        }
        int[] nArray = this.containerActionHistory.poll();
        if (nArray != null) {
            this.performRecordedClick(class_17032, nArray);
            return;
        }
        InventoryBuilder.minecraftClient.interactionManager.clickSlot(class_17032.syncId, this.selectedPurchaseSlot, 0, SlotActionType.PICKUP, (PlayerEntity)InventoryBuilder.minecraftClient.player);
        this.buildState = InventoryBuildState.NEXT_PAGE;
        this.buildActionTimer.reset();
    }

    private void handleBuildComplete() {
        if (!this.buildActionTimer.hasElapsed(1200L)) {
            return;
        }
        InventoryPresetEntry inventoryPresetEntry = this.activePreset.slotEntries[this.activeSlotIndex];
        int n = this.getRequiredCount(inventoryPresetEntry);
        if (n <= this.purchasedAmount) {
            if (!this.buildActionTimer.hasElapsed(2500L)) {
                return;
            }
            this.notifyBuildFailure(Localization.translate("inventory_builder.listing_sniped"));
            return;
        }
        this.searchAttempts = 0;
        this.insufficientFundsResponses = 0;
        this.completedItemCount += n - this.purchasedAmount;
        Notification.info(Text.of((String)(Localization.translateFormatted("inventory_builder.bought", n - this.purchasedAmount, inventoryPresetEntry.getFormattedDisplayName()) + (String)(this.currentListingPrice > 0L ? " " + Localization.translateFormatted("inventory_builder.bought_for", MathUtils.formatCurrencyAmount(this.currentListingPrice)) : ""))));
        this.currentListingPrice = 0L;
        this.statusMessage = "";
        if (this.calculateEntryCount(inventoryPresetEntry) - n > 0 && this.getAuctionContainer() != null && !this.getOpenContainerTitle().contains("\u043f\u043e\u043a\u0443\u043f\u043a")) {
            this.initializeBuildState();
            return;
        }
        this.closeAuctionContainer();
        this.buildState = InventoryBuildState.SEARCHING;
        this.buildActionTimer.reset();
    }

    private void notifyBuildFailure(String string) {
        String string2;
        ++this.searchAttempts;
        this.pendingPurchaseAmount = 0;
        this.containerActionHistory.clear();
        if (!this.statusMessage.isEmpty()) {
            this.purchasedListingKeys.add(this.statusMessage);
        }
        this.statusMessage = "";
        this.currentListingPrice = 0L;
        InventoryPresetEntry inventoryPresetEntry = this.activePreset == null || this.activeSlotIndex < 0 || this.activeSlotIndex >= 41 ? null : this.activePreset.slotEntries[this.activeSlotIndex];
        String string3 = string2 = inventoryPresetEntry == null ? "" : ": " + inventoryPresetEntry.getFormattedDisplayName();
        if (this.searchAttempts >= (int)this.attempts.getValue()) {
            Notification.warning(Text.of((String)(string + string2 + " " + Localization.translate("inventory_builder.skipping_item"))));
            this.closeAuctionContainer();
            this.finalizeBuild();
            this.buildState = InventoryBuildState.SEARCHING;
            this.buildActionTimer.reset();
            return;
        }
        Notification.warning(Text.of((String)(string + string2 + " " + Localization.translate("inventory_builder.taking_next"))));
        if (this.getAuctionContainer() != null && !this.getOpenContainerTitle().contains("\u043f\u043e\u043a\u0443\u043f\u043a")) {
            this.initializeBuildState();
            return;
        }
        this.closeAuctionContainer();
        this.buildState = InventoryBuildState.SEARCHING;
        this.buildActionTimer.reset();
    }

    private void initializeBuildState() {
        this.currentPageOffset = 0;
        this.searchPage = 0;
        this.awaitingSearchResponse = false;
        this.buildState = InventoryBuildState.SEARCH_RESULTS;
        this.buildActionTimer.reset();
    }

    private void finalizeBuild() {
        ++this.activeSlotIndex;
        this.searchAttempts = 0;
        this.purchasedListingKeys.clear();
        this.statusMessage = "";
    }

    private String formatListingKey(ItemStack class_17992, List<Text> list, long l) {
        StringBuilder stringBuilder = new StringBuilder(ItemMetadataUtils.cleanDisplayName(class_17992).trim().toLowerCase(Locale.ROOT));
        stringBuilder.append('|').append(class_17992.getCount()).append('|').append(l);
        for (Text class_25612 : list) {
            String string = class_25612.getString();
            String string2 = string.toLowerCase(Locale.ROOT);
            if (!string2.contains("\u043f\u0440\u043e\u0434\u0430\u0432") && !string2.contains("\u0432\u043b\u0430\u0434\u0435\u043b")) continue;
            stringBuilder.append('|').append(string.trim());
            break;
        }
        return stringBuilder.toString();
    }

    private void resetCurrentItem() {
        if (this.buildActionTimer.hasElapsed(20000L) || this.currentItemIndex > 400) {
            this.cancelBuild(Localization.translate(this.currentItemIndex > 400 ? "inventory_builder.layout_mismatch" : "inventory_builder.layout_stuck"));
            return;
        }
        if (this.getAuctionContainer() != null) {
            this.closeAuctionContainer();
            return;
        }
        PlayerScreenHandler class_17232 = InventoryBuilder.minecraftClient.player.playerScreenHandler;
        if (this.containerPageStage > 0) {
            this.processContainer((ScreenHandler)class_17232);
            return;
        }
        if (!class_17232.getCursorStack().isEmpty()) {
            this.resetContainerState((ScreenHandler)class_17232);
            return;
        }
        for (int i = 0; i < 41; ++i) {
            int n;
            int n2;
            InventoryPresetEntry inventoryPresetEntry = this.activePreset.slotEntries[i];
            if (inventoryPresetEntry == null || this.occupiedPresetSlots.contains(n2 = this.getPresetSlot(i))) continue;
            ItemStack class_17992 = class_17232.getSlot(n2).getStack();
            boolean bl = this.isItemMatch(class_17992, inventoryPresetEntry);
            int n3 = n = bl ? class_17992.getCount() : 0;
            if (bl && n == inventoryPresetEntry.requiredAmount) continue;
            if (!bl) {
                int n4 = this.calculatePurchaseAmount((ScreenHandler)class_17232, inventoryPresetEntry, n2, 0, class_17992);
                if (n4 < 0) continue;
                this.processContainerSlot((ScreenHandler)class_17232, n4, n2);
                return;
            }
            if (!this.isItemEligible((ScreenHandler)class_17232, inventoryPresetEntry, n2, class_17992, n)) continue;
            return;
        }
        this.finishBuild();
    }

    private void processContainerSlot(ScreenHandler class_17032, int n, int n2) {
        ++this.currentItemIndex;
        this.rememberPendingSlot(class_17032, n2);
        if (n2 >= 36 && n2 <= 44) {
            this.clickContainerSlot(class_17032, n, n2 - 36);
            return;
        }
        if (n >= 36 && n <= 44) {
            this.clickContainerSlot(class_17032, n2, n - 36);
            return;
        }
        this.lastAuctionSlot = n;
        this.currentAuctionSlot = n2;
        this.fallbackHotbarSlot = this.getContainerSlotIndex(class_17032, n, n2);
        this.containerPageStage = 1;
        this.clickContainerSlot(class_17032, this.lastAuctionSlot, this.fallbackHotbarSlot);
    }

    private void processContainer(ScreenHandler class_17032) {
        ++this.currentItemIndex;
        if (this.containerPageStage == 1) {
            this.containerPageStage = 2;
            this.clickContainerSlot(class_17032, this.currentAuctionSlot, this.fallbackHotbarSlot);
            return;
        }
        this.containerPageStage = 0;
        this.clickContainerSlot(class_17032, this.lastAuctionSlot, this.fallbackHotbarSlot);
    }

    private void clickContainerSlot(ScreenHandler class_17032, int n, int n2) {
        InventoryBuilder.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n, n2, SlotActionType.SWAP, (PlayerEntity)InventoryBuilder.minecraftClient.player);
        this.buildActionTimer.reset();
    }

    private int getContainerSlotIndex(ScreenHandler class_17032, int n, int n2) {
        int n3;
        for (n3 = 44; n3 >= 36; --n3) {
            if (n3 == n || n3 == n2 || this.isPageValid(n3) || !class_17032.getSlot(n3).getStack().isEmpty()) continue;
            return n3 - 36;
        }
        for (n3 = 44; n3 >= 36; --n3) {
            if (n3 == n || n3 == n2 || this.isPageValid(n3)) continue;
            return n3 - 36;
        }
        return 8;
    }

    private boolean isItemEligible(ScreenHandler class_17032, InventoryPresetEntry inventoryPresetEntry, int n, ItemStack class_17992, int n2) {
        if (n2 > inventoryPresetEntry.requiredAmount) {
            ++this.currentItemIndex;
            this.rememberPendingSlot(class_17032, n);
            this.processAlternateSlot(class_17032, n, 0);
            return true;
        }
        int n3 = this.calculatePurchaseAmount(class_17032, inventoryPresetEntry, n, n2, class_17992);
        if (n3 < 0) {
            return false;
        }
        ++this.currentItemIndex;
        this.rememberPendingSlot(class_17032, n);
        this.processAlternateSlot(class_17032, n3, 0);
        return true;
    }

    private void resetContainerState(ScreenHandler class_17032) {
        int n;
        ItemStack class_17992 = class_17032.getCursorStack();
        for (n = 0; n < 41; ++n) {
            int n2;
            int n3;
            InventoryPresetEntry inventoryPresetEntry = this.activePreset.slotEntries[n];
            if (inventoryPresetEntry == null || this.occupiedPresetSlots.contains(n3 = this.getPresetSlot(n)) || !this.isItemMatch(class_17992, inventoryPresetEntry)) continue;
            ItemStack class_17993 = class_17032.getSlot(n3).getStack();
            int n4 = n2 = this.isItemMatch(class_17993, inventoryPresetEntry) ? class_17993.getCount() : 0;
            if (n2 >= inventoryPresetEntry.requiredAmount || !class_17993.isEmpty() && n2 == 0 || n2 > 0 && !ItemStack.areItemsAndComponentsEqual((ItemStack)class_17993, (ItemStack)class_17992)) continue;
            boolean bl = n2 + class_17992.getCount() <= inventoryPresetEntry.requiredAmount;
            this.processAlternateSlot(class_17032, n3, bl ? 0 : 1);
            return;
        }
        n = this.getContainerPage(class_17032);
        if (n < 0) {
            this.cancelBuild(Localization.translate("inventory_builder.no_free_slot"));
            return;
        }
        this.processAlternateSlot(class_17032, n, 0);
    }

    private int calculatePurchaseAmount(ScreenHandler class_17032, InventoryPresetEntry inventoryPresetEntry, int n, int n2, ItemStack class_17992) {
        for (int i = 5; i <= 45; ++i) {
            ItemStack class_17993;
            if (i == n || !this.isItemMatch(class_17993 = class_17032.getSlot(i).getStack(), inventoryPresetEntry) || this.isContainerSlotValid(class_17032, i) || n2 > 0 && !ItemStack.areItemsAndComponentsEqual((ItemStack)class_17992, (ItemStack)class_17993)) continue;
            return i;
        }
        return -1;
    }

    private void processAlternateSlot(ScreenHandler class_17032, int n, int n2) {
        if (this.pendingContainerSlot == n && this.getContainerSlotDescription(class_17032, n).equals(this.pendingSlotDescription)) {
            if (++this.stalledSlotChecks >= 3) {
                Notification.warning(Text.of((String)Localization.translateFormatted("inventory_builder.slot_not_responding", n)));
                this.occupiedPresetSlots.add(n);
                this.stalledSlotChecks = 0;
            }
        } else {
            this.stalledSlotChecks = 0;
        }
        this.pendingContainerSlot = n;
        this.pendingSlotDescription = this.getContainerSlotDescription(class_17032, n);
        InventoryBuilder.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n, n2, SlotActionType.PICKUP, (PlayerEntity)InventoryBuilder.minecraftClient.player);
        this.buildActionTimer.reset();
    }

    private String getContainerSlotDescription(ScreenHandler class_17032, int n) {
        ItemStack class_17992 = class_17032.getSlot(n).getStack();
        return class_17992.isEmpty() ? "-" : String.valueOf(Registries.ITEM.getId(class_17992.getItem())) + "x" + class_17992.getCount();
    }

    private void rememberPendingSlot(ScreenHandler class_17032, int n) {
        this.pendingContainerSlot = n;
        this.pendingSlotDescription = this.getContainerSlotDescription(class_17032, n);
    }

    private void performRecordedClick(ScreenHandler class_17032, int[] nArray) {
        SlotActionType class_17132 = nArray.length > 2 && nArray[2] == 1 ? SlotActionType.SWAP : SlotActionType.PICKUP;
        InventoryBuilder.minecraftClient.interactionManager.clickSlot(class_17032.syncId, nArray[0], nArray[1], class_17132, (PlayerEntity)InventoryBuilder.minecraftClient.player);
    }

    private int getPresetSlot(int n) {
        if (n < 27) {
            return 9 + n;
        }
        if (n < 36) {
            return 36 + (n - 27);
        }
        if (n < 40) {
            return 5 + (n - 36);
        }
        return 45;
    }

    private boolean isContainerSlotValid(ScreenHandler class_17032, int n) {
        for (int i = 0; i < 41; ++i) {
            ItemStack class_17992;
            InventoryPresetEntry inventoryPresetEntry = this.activePreset.slotEntries[i];
            if (inventoryPresetEntry == null || this.getPresetSlot(i) != n || !this.isItemMatch(class_17992 = class_17032.getSlot(n).getStack(), inventoryPresetEntry) || class_17992.getCount() != inventoryPresetEntry.requiredAmount) continue;
            return true;
        }
        return false;
    }

    private boolean isEntryCompatible(InventoryPresetEntry inventoryPresetEntry, InventoryPresetEntry inventoryPresetEntry2) {
        return inventoryPresetEntry.itemId.equals((Object)inventoryPresetEntry2.itemId) && inventoryPresetEntry.getFullSearchText().equalsIgnoreCase(inventoryPresetEntry2.getFullSearchText());
    }

    private int calculateEntryCount(InventoryPresetEntry inventoryPresetEntry) {
        int n = 0;
        for (InventoryPresetEntry inventoryPresetEntry2 : this.activePreset.slotEntries) {
            if (inventoryPresetEntry2 == null || !this.isEntryCompatible(inventoryPresetEntry2, inventoryPresetEntry)) continue;
            n += inventoryPresetEntry2.requiredAmount;
        }
        return n;
    }

    private boolean isIndexValid(int n) {
        InventoryPresetEntry inventoryPresetEntry = this.activePreset.slotEntries[n];
        for (int i = 0; i < n; ++i) {
            InventoryPresetEntry inventoryPresetEntry2 = this.activePreset.slotEntries[i];
            if (inventoryPresetEntry2 == null || !this.isEntryCompatible(inventoryPresetEntry2, inventoryPresetEntry)) continue;
            return true;
        }
        return false;
    }

    private int getContainerPage(ScreenHandler class_17032) {
        int n;
        for (n = 9; n <= 44; ++n) {
            if (!class_17032.getSlot(n).getStack().isEmpty() || this.isPageValid(n)) continue;
            return n;
        }
        for (n = 9; n <= 44; ++n) {
            if (!class_17032.getSlot(n).getStack().isEmpty()) continue;
            return n;
        }
        return -1;
    }

    private boolean isPageValid(int n) {
        for (int i = 0; i < 41; ++i) {
            if (this.activePreset.slotEntries[i] == null || this.getPresetSlot(i) != n) continue;
            return true;
        }
        return false;
    }

    private int findSortingSlot(ScreenHandler class_17032, int n) {
        for (int i = 0; i < n; ++i) {
            ItemStack class_17992 = class_17032.getSlot(i).getStack();
            if (class_17992.isEmpty() || !class_17992.getName().getString().toLowerCase(Locale.ROOT).contains("\u0441\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u043a\u0430")) continue;
            return i;
        }
        return -1;
    }

    private boolean isDiscountApproved(ItemStack class_17992) {
        for (Text class_25612 : this.getItemLore(class_17992)) {
            String string = class_25612.getString().trim();
            String string2 = string.toLowerCase(Locale.ROOT);
            if (!string2.contains("\u0434\u0435\u0448\u0435\u0432") || !string2.contains("\u0435\u0434")) continue;
            return string.startsWith("\u2713") || string.startsWith("\u2714");
        }
        return false;
    }

    private int[] findAvailableSlotRange() {
        Matcher matcher = availableSlotsPattern.matcher(this.getOpenContainerTitle());
        if (matcher.find()) {
            return new int[]{Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))};
        }
        return new int[]{1, 0};
    }

    private int findNextPageSlot(ScreenHandler class_17032, int n) {
        for (int i = 0; i < n; ++i) {
            ItemStack class_17992 = class_17032.getSlot(i).getStack();
            if (class_17992.isEmpty() || !class_17992.getName().getString().toLowerCase(Locale.ROOT).contains("\u0441\u043b\u0435\u0434\u0443\u044e\u0449\u0430\u044f \u0441\u0442\u0440\u0430\u043d\u0438\u0446\u0430")) continue;
            return i;
        }
        return -1;
    }

    private String formatSearchQuery(InventoryPresetEntry inventoryPresetEntry) {
        if (!inventoryPresetEntry.alternateSearchQuery.isBlank()) {
            return inventoryPresetEntry.alternateSearchQuery;
        }
        if (!inventoryPresetEntry.potionEffectId.isBlank()) {
            String string;
            Potion class_18422;
            Identifier potionId = Identifier.tryParse(inventoryPresetEntry.potionEffectId);
            Potion class_18423 = class_18422 = potionId == null ? null : Registries.POTION.get(potionId);
            if (class_18422 != null && !class_18422.getEffects().isEmpty() && !(string = class_18422.getEffects().getFirst().getEffectType().value().getName().getString()).isBlank()) {
                return "\u0417\u0435\u043b\u044c\u0435 " + string.toLowerCase(Locale.ROOT);
            }
        }
        String displayName = inventoryPresetEntry.getFormattedDisplayName().trim();
        if (!displayName.isBlank()) {
            return displayName;
        }
        return Text.translatable((String)((Item)Registries.ITEM.get(inventoryPresetEntry.itemId)).getTranslationKey()).getString();
    }

    private int getRequiredCount(InventoryPresetEntry inventoryPresetEntry) {
        int n = 0;
        for (int i = 0; i < InventoryBuilder.minecraftClient.player.getInventory().size(); ++i) {
            ItemStack class_17992 = InventoryBuilder.minecraftClient.player.getInventory().getStack(i);
            if (!this.isItemMatch(class_17992, inventoryPresetEntry) || !this.isListingValid(class_17992, this.getItemLore(class_17992), inventoryPresetEntry)) continue;
            n += class_17992.getCount();
        }
        return n;
    }

    private boolean isItemMatch(ItemStack class_17992, InventoryPresetEntry inventoryPresetEntry) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return false;
        }
        if (class_17992.getItem() != Registries.ITEM.get(inventoryPresetEntry.itemId)) {
            return false;
        }
        String string = ItemMetadataUtils.cleanDisplayName(class_17992).trim().toLowerCase(Locale.ROOT);
        if (inventoryPresetEntry.exactNameMatch) {
            return string.equals(inventoryPresetEntry.getFullSearchText().trim().toLowerCase(Locale.ROOT));
        }
        String string2 = inventoryPresetEntry.getMatchText();
        return string2.isBlank() || string.contains(string2.toLowerCase(Locale.ROOT));
    }

    private int countMatchingItems(List<Text> list, String string) {
        String string2 = string.toLowerCase(Locale.ROOT);
        for (Text class_25612 : list) {
            String string3 = class_25612.getString().trim();
            int n = string3.toLowerCase(Locale.ROOT).indexOf(string2);
            if (n < 0) continue;
            String string4 = string3.substring(n + string.length()).trim();
            int n2 = string4.indexOf(32);
            if (n2 > 0) {
                string4 = string4.substring(0, n2);
            }
            return (string4 = string4.replaceAll("[^IVX]", "")).isEmpty() ? 0 : this.parseRomanQuantity(string4);
        }
        return -1;
    }

    /*
     * Enabled aggressive block sorting
     */
    private int parseRomanQuantity(String string) {
        int n = 0;
        int n2 = 0;
        int n3 = string.length() - 1;
        while (n3 >= 0) {
            int n4 = switch (string.charAt(n3)) {
                case 'I' -> 1;
                case 'V' -> 5;
                case 'X' -> 10;
                default -> 0;
            };
            n += n4 < n2 ? -n4 : n4;
            n2 = Math.max(n2, n4);
            --n3;
        }
        return n;
    }

    private int sumEffectDuration(List<Text> list) {
        int n = 0;
        Matcher matcher = durationPattern.matcher("");
        for (Text class_25612 : list) {
            matcher.reset(class_25612.getString());
            while (matcher.find()) {
                n = Math.max(n, Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2)));
            }
        }
        return n;
    }

    private boolean isListingValid(ItemStack class_17992, List<Text> list, InventoryPresetEntry inventoryPresetEntry) {
        float f;
        if (inventoryPresetEntry.minimumDurabilityPercent > 0 && class_17992.isDamageable() && class_17992.getMaxDamage() > 0 && (f = (float)(class_17992.getMaxDamage() - class_17992.getDamage()) * 100.0f / (float)class_17992.getMaxDamage()) < (float)inventoryPresetEntry.minimumDurabilityPercent) {
            return false;
        }
        for (Map.Entry<String, Integer> entry : inventoryPresetEntry.requiredTraitCounts.entrySet()) {
            int n = this.countMatchingItems(list, entry.getKey());
            if (n >= 0 && n >= entry.getValue()) continue;
            return false;
        }
        return inventoryPresetEntry.minimumDuration <= 0 || this.sumEffectDuration(list) >= inventoryPresetEntry.minimumDuration;
    }

    private long extractListingPrice(List<Text> list, int n) {
        long l = -1L;
        long l2 = -1L;
        for (Text class_25612 : list) {
            int n2;
            long l3;
            String string = class_25612.getString();
            String string2 = string.toLowerCase(Locale.ROOT);
            if (!string2.contains("\u0446\u0435\u043d\u0430") && !string2.contains("\u0441\u0442\u043e\u0438\u043c\u043e\u0441\u0442\u044c") || (l3 = this.parseCurrencyAmount((n2 = string.indexOf(58)) < 0 ? string : string.substring(n2 + 1))) < 0L) continue;
            if (string2.contains("\u0435\u0434")) {
                l = l3;
                continue;
            }
            l2 = l3;
        }
        if (l >= 0L) {
            return l;
        }
        if (l2 >= 0L) {
            return n > 1 ? l2 / (long)n : l2;
        }
        return -1L;
    }

    private long parseCurrencyAmount(String string) {
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (!Character.isDigit(c)) continue;
            stringBuilder.append(c);
        }
        if (stringBuilder.isEmpty()) {
            return -1L;
        }
        try {
            return Long.parseLong(stringBuilder.toString());
        }
        catch (NumberFormatException numberFormatException) {
            return -1L;
        }
    }

    private List<Text> getItemLore(ItemStack class_17992) {
        return class_17992.getTooltip(Item.TooltipContext.create((World)InventoryBuilder.minecraftClient.world), (PlayerEntity)InventoryBuilder.minecraftClient.player, (TooltipType)TooltipType.BASIC);
    }

    private String getOpenContainerTitle() {
        return InventoryBuilder.minecraftClient.currentScreen == null ? "" : InventoryBuilder.minecraftClient.currentScreen.getTitle().getString().toLowerCase(Locale.ROOT);
    }

    private ScreenHandler getAuctionContainer() {
        if (!(InventoryBuilder.minecraftClient.currentScreen instanceof HandledScreen)) {
            return null;
        }
        ScreenHandler class_17032 = InventoryBuilder.minecraftClient.player.currentScreenHandler;
        if (class_17032 == null || class_17032 == InventoryBuilder.minecraftClient.player.playerScreenHandler) {
            return null;
        }
        return class_17032;
    }

    private void closeAuctionContainer() {
        ScreenHandler class_17032 = this.getAuctionContainer();
        if (class_17032 == null) {
            return;
        }
        InventoryBuilder.minecraftClient.player.networkHandler.sendPacket((Packet)new CloseHandledScreenC2SPacket(class_17032.syncId));
        InventoryBuilder.minecraftClient.player.closeHandledScreen();
    }

    @Override
    public void onDisable() {
        this.buildState = InventoryBuildState.IDLE;
        this.activePreset = null;
        this.containerActionHistory.clear();
        super.onDisable();
    }

    public static void loadAuctionCatalog() {
        if (!catalogEntries.isEmpty()) {
            return;
        }
        try (InputStream inputStream = InventoryBuilder.class.getResourceAsStream("/assets/rockstar/auction_items.json");){
            if (inputStream == null) {
                return;
            }
            JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
            if (jsonObject == null || !jsonObject.has("items")) {
                return;
            }
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("items")) {
                JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                Identifier class_29602 = Identifier.tryParse((String)jsonObject2.get("id").getAsString());
                if (class_29602 == null) continue;
                InventoryPresetEntry inventoryPresetEntry = new InventoryPresetEntry(class_29602, jsonObject2.get("query").getAsString());
                inventoryPresetEntry.categoryName = jsonObject2.has("category") ? jsonObject2.get("category").getAsString() : "";
                inventoryPresetEntry.matchText = jsonObject2.has("match") ? jsonObject2.get("match").getAsString() : "";
                inventoryPresetEntry.textureValue = jsonObject2.has("texture") ? jsonObject2.get("texture").getAsString() : "";
                inventoryPresetEntry.colorValue = jsonObject2.has("color") ? jsonObject2.get("color").getAsInt() : -1;
                inventoryPresetEntry.requiredDuration = jsonObject2.has("duration") ? jsonObject2.get("duration").getAsInt() : 0;
                inventoryPresetEntry.exactNameMatch = !jsonObject2.has("exact") || jsonObject2.get("exact").getAsBoolean();
                inventoryPresetEntry.budgetLimit = jsonObject2.has("budget") ? jsonObject2.get("budget").getAsInt() : 0;
                inventoryPresetEntry.requiredStackCount = jsonObject2.has("stack") ? jsonObject2.get("stack").getAsInt() : 0;
                inventoryPresetEntry.alternateSearchQuery = jsonObject2.has("search") ? jsonObject2.get("search").getAsString() : "";
                inventoryPresetEntry.customNameMatching = true;
                if (jsonObject2.has("traits")) {
                    for (JsonElement jsonElement2 : jsonObject2.getAsJsonArray("traits")) {
                        JsonObject traitObject = jsonElement2.getAsJsonObject();
                        inventoryPresetEntry.traitLimits.add(new InventoryTraitLimit(traitObject.get("name").getAsString(), traitObject.get("max").getAsInt()));
                    }
                }
                if (jsonObject2.has("variants")) {
                    for (JsonElement jsonElement2 : jsonObject2.getAsJsonArray("variants")) {
                        Map<String, Integer> variantProperties = new LinkedHashMap<>();
                        for (Map.Entry<String, JsonElement> entry : jsonElement2.getAsJsonObject().entrySet()) {
                            variantProperties.put(entry.getKey(), entry.getValue().getAsInt());
                        }
                        if (variantProperties.isEmpty()) continue;
                        inventoryPresetEntry.variantProperties.add(variantProperties);
                    }
                }
                InventoryBuilder.mergeSingleVariantProperties(inventoryPresetEntry);
                catalogEntries.add(inventoryPresetEntry);
            }
        }
        catch (Exception exception) {
            System.err.println("Error reading auction catalog: " + exception.getMessage());
        }
    }

    private static void mergeSingleVariantProperties(InventoryPresetEntry inventoryPresetEntry) {
        if (inventoryPresetEntry.variantProperties.size() != 1) {
            return;
        }
        inventoryPresetEntry.requiredTraitCounts.putAll(inventoryPresetEntry.variantProperties.getFirst());
    }

    private static void applyCatalogDefaults(InventoryPresetEntry inventoryPresetEntry) {
        for (InventoryPresetEntry inventoryPresetEntry2 : catalogEntries) {
            if (!inventoryPresetEntry2.itemId.equals((Object)inventoryPresetEntry.itemId) || !inventoryPresetEntry2.getFullSearchText().equalsIgnoreCase(inventoryPresetEntry.getFullSearchText())) continue;
            inventoryPresetEntry.searchQuery = inventoryPresetEntry2.searchQuery;
            inventoryPresetEntry.matchText = inventoryPresetEntry2.matchText;
            inventoryPresetEntry.customNameMatching = inventoryPresetEntry2.customNameMatching;
            inventoryPresetEntry.exactNameMatch = inventoryPresetEntry2.exactNameMatch;
            inventoryPresetEntry.textureValue = inventoryPresetEntry2.textureValue;
            inventoryPresetEntry.potionEffectId = inventoryPresetEntry2.potionEffectId;
            inventoryPresetEntry.colorValue = inventoryPresetEntry2.colorValue;
            inventoryPresetEntry.requiredDuration = inventoryPresetEntry2.requiredDuration;
            inventoryPresetEntry.budgetLimit = inventoryPresetEntry2.budgetLimit;
            inventoryPresetEntry.requiredStackCount = inventoryPresetEntry2.requiredStackCount;
            inventoryPresetEntry.alternateSearchQuery = inventoryPresetEntry2.alternateSearchQuery;
            inventoryPresetEntry.traitLimits.clear();
            inventoryPresetEntry.traitLimits.addAll(inventoryPresetEntry2.traitLimits);
            inventoryPresetEntry.variantProperties.clear();
            for (Map<String, Integer> map : inventoryPresetEntry2.variantProperties) {
                inventoryPresetEntry.variantProperties.add(new LinkedHashMap<String, Integer>(map));
            }
            inventoryPresetEntry.requiredTraitCounts.keySet().removeIf(string -> inventoryPresetEntry.traitLimits.stream().noneMatch(inventoryTraitLimit -> inventoryTraitLimit.getTraitName().equals(string)));
            InventoryBuilder.mergeSingleVariantProperties(inventoryPresetEntry);
            return;
        }
    }

    public static void loadPresetFiles() {
        InventoryBuilder.loadAuctionCatalog();
        if (presetsLoaded) {
            return;
        }
        presetsLoaded = true;
        loadedPresets.clear();
        File[] fileArray = presetDirectory.listFiles((file, string) -> string.toLowerCase(Locale.ROOT).endsWith(PRESET_FILE_EXTENSION));
        if (fileArray == null || fileArray.length == 0) {
            InventoryBuilder.migrateLegacyPresets();
            return;
        }
        LinkedHashMap<String, Integer> linkedHashMap = new LinkedHashMap<String, Integer>();
        ArrayList<InventoryPresetLayout> arrayList = new ArrayList<InventoryPresetLayout>();
        for (File file2 : fileArray) {
            String string2 = file2.getName();
            try (FileReader fileReader = new FileReader(file2, StandardCharsets.UTF_8);){
                JsonObject jsonObject = JsonParser.parseReader(fileReader).getAsJsonObject();
                InventoryPresetLayout inventoryPresetLayout2 = InventoryBuilder.loadLayout(jsonObject, string2.substring(0, string2.length() - PRESET_FILE_EXTENSION.length()));
                if (inventoryPresetLayout2 == null) continue;
                inventoryPresetLayout2.description = string2;
                linkedHashMap.put(string2, jsonObject.has("order") ? jsonObject.get("order").getAsInt() : Integer.MAX_VALUE);
                arrayList.add(inventoryPresetLayout2);
            }
            catch (Exception exception) {
                RockstarClient.LOGGER.warn("[InventoryBuilder] preset {} is not readable: {}", (Object)string2, (Object)exception.getMessage());
            }
        }
        arrayList.sort(Comparator.comparingInt((InventoryPresetLayout inventoryPresetLayout) -> linkedHashMap.getOrDefault(inventoryPresetLayout.description, Integer.MAX_VALUE)).thenComparing(inventoryPresetLayout -> inventoryPresetLayout.description, String.CASE_INSENSITIVE_ORDER));
        loadedPresets.addAll(arrayList);
    }

    private static void migrateLegacyPresets() {
        if (!legacyPresetFile.exists()) {
            return;
        }
        ArrayList<InventoryPresetLayout> arrayList = new ArrayList<InventoryPresetLayout>();
        try (FileReader reader = new FileReader(legacyPresetFile, StandardCharsets.UTF_8)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            if (jsonObject != null && jsonObject.has("presets")) {
                for (JsonElement jsonElement : jsonObject.getAsJsonArray("presets")) {
                    InventoryPresetLayout inventoryPresetLayout = InventoryBuilder.loadLayout(jsonElement.getAsJsonObject(), Localization.translate("inventory_builder.preset"));
                    if (inventoryPresetLayout == null) continue;
                    arrayList.add(inventoryPresetLayout);
                }
            }
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.warn("[InventoryBuilder] legacy presets are not readable: {}", (Object)exception.getMessage());
            return;
        }
        loadedPresets.addAll(arrayList);
        if (!InventoryBuilder.savePresets()) {
            return;
        }
        File[] fileArray = presetDirectory.listFiles((file, string) -> string.toLowerCase(Locale.ROOT).endsWith(PRESET_FILE_EXTENSION));
        if (fileArray == null || fileArray.length < arrayList.size()) {
            RockstarClient.LOGGER.warn("[InventoryBuilder] preset migration failed, the old file was kept");
            return;
        }
        if (!legacyPresetFile.delete()) {
            legacyPresetFile.deleteOnExit();
        }
        RockstarClient.LOGGER.info("[InventoryBuilder] presets migrated to {} ({} of them)", (Object)presetDirectory, (Object)arrayList.size());
    }

    public static boolean savePresets() {
        if (!presetsLoaded) {
            return false;
        }
        try {
            Files.createDirectories(presetDirectory.toPath(), new FileAttribute[0]);
            HashSet<String> hashSet = new HashSet<String>();
            for (int i = 0; i < loadedPresets.size(); ++i) {
                InventoryPresetLayout layout = loadedPresets.get(i);
                String string2 = InventoryBuilder.formatLayoutEntries(layout, hashSet);
                hashSet.add(string2.toLowerCase(Locale.ROOT));
                if (!layout.description.isEmpty() && !layout.description.equalsIgnoreCase(string2)) {
                    new File(presetDirectory, layout.description).delete();
                }
                layout.description = string2;
                JsonObject jsonObject = InventoryBuilder.serializeLayout(layout);
                jsonObject.addProperty("order", (Number)i);
                moscow.rockstar.util.JsonFiles.write(new File(presetDirectory, string2), jsonObject);
            }
            File[] fileArray = presetDirectory.listFiles((file, string) -> string.toLowerCase(Locale.ROOT).endsWith(PRESET_FILE_EXTENSION));
            if (fileArray != null) {
                for (File file2 : fileArray) {
                    if (hashSet.contains(file2.getName().toLowerCase(Locale.ROOT))) continue;
                    file2.delete();
                }
            }
            return true;
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.warn("[InventoryBuilder] presets were not saved: {}", (Object)exception.getMessage());
            return false;
        }
    }

    public static String serializeLayoutJson(InventoryPresetLayout inventoryPresetLayout) {
        return InventoryBuilder.serializeLayout(inventoryPresetLayout).toString();
    }

    public static boolean isPresetNameValid(String string, String string2) {
        if (string2 == null || string2.isBlank()) {
            return false;
        }
        try {
            InventoryBuilder.loadPresetFiles();
            String string3 = string == null || string.isBlank() ? Localization.translate("inventory_builder.preset") : string.trim();
            InventoryPresetLayout inventoryPresetLayout = InventoryBuilder.loadLayout(JsonParser.parseString(string2).getAsJsonObject(), string3);
            if (inventoryPresetLayout == null || inventoryPresetLayout.getFilledSlotCount() == 0) {
                return false;
            }
            inventoryPresetLayout.layoutName = InventoryBuilder.normalizePresetName(string3);
            inventoryPresetLayout.description = "";
            loadedPresets.add(inventoryPresetLayout);
            return InventoryBuilder.savePresets();
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.warn("[InventoryBuilder] shared preset is not readable: {}", (Object)exception.getMessage());
            return false;
        }
    }

    private static String normalizePresetName(String string) {
        String candidate = string;
        for (int i = 2; InventoryBuilder.isPresetPathValid(candidate) && i < 100; ++i) {
            candidate = string + " " + i;
        }
        return candidate;
    }

    private static boolean isPresetPathValid(String string) {
        for (InventoryPresetLayout inventoryPresetLayout : loadedPresets) {
            if (!inventoryPresetLayout.layoutName.equalsIgnoreCase(string)) continue;
            return true;
        }
        return false;
    }

    private static InventoryPresetLayout loadLayout(JsonObject jsonObject, String string) {
        if (jsonObject == null || !jsonObject.has("slots")) {
            return null;
        }
        String string2 = jsonObject.has("name") ? jsonObject.get("name").getAsString() : "";
        InventoryPresetLayout inventoryPresetLayout = new InventoryPresetLayout(string2.isBlank() ? string : string2);
        for (JsonElement jsonElement : jsonObject.getAsJsonArray("slots")) {
            JsonObject jsonObject3 = jsonElement.getAsJsonObject();
            int n = jsonObject3.get("slot").getAsInt();
            if (n < 0 || n >= 41) continue;
            InventoryPresetEntry inventoryPresetEntry = new InventoryPresetEntry();
            Identifier class_29602 = Identifier.tryParse((String)jsonObject3.get("item").getAsString());
            if (class_29602 == null) continue;
            inventoryPresetEntry.itemId = class_29602;
            inventoryPresetEntry.searchQuery = jsonObject3.has("query") ? jsonObject3.get("query").getAsString() : "";
            inventoryPresetEntry.matchText = jsonObject3.has("match") ? jsonObject3.get("match").getAsString() : "";
            inventoryPresetEntry.customNameMatching = jsonObject3.has("custom") && jsonObject3.get("custom").getAsBoolean();
            inventoryPresetEntry.requiredAmount = jsonObject3.has("amount") ? jsonObject3.get("amount").getAsInt() : 1;
            inventoryPresetEntry.minimumDurabilityPercent = jsonObject3.has("durability") ? jsonObject3.get("durability").getAsInt() : 0;
            inventoryPresetEntry.minimumDuration = jsonObject3.has("minDuration") ? jsonObject3.get("minDuration").getAsInt() : 0;
            inventoryPresetEntry.maximumPrice = jsonObject3.has("price") ? jsonObject3.get("price").getAsLong() : 0L;
            inventoryPresetEntry.textureValue = jsonObject3.has("texture") ? jsonObject3.get("texture").getAsString() : "";
            inventoryPresetEntry.colorValue = jsonObject3.has("color") ? jsonObject3.get("color").getAsInt() : -1;
            inventoryPresetEntry.potionEffectId = jsonObject3.has("potion") ? jsonObject3.get("potion").getAsString() : "";
            inventoryPresetEntry.exactNameMatch = jsonObject3.has("exact") && jsonObject3.get("exact").getAsBoolean();
            inventoryPresetEntry.requiredDuration = jsonObject3.has("duration") ? jsonObject3.get("duration").getAsInt() : 0;
            inventoryPresetEntry.budgetLimit = jsonObject3.has("budget") ? jsonObject3.get("budget").getAsInt() : 0;
            inventoryPresetEntry.requiredStackCount = jsonObject3.has("stack") ? jsonObject3.get("stack").getAsInt() : 0;
            String string3 = inventoryPresetEntry.alternateSearchQuery = jsonObject3.has("search") ? jsonObject3.get("search").getAsString() : "";
            if (jsonObject3.has("variants")) {
                for (JsonElement variantElement : jsonObject3.getAsJsonArray("variants")) {
                    Map<String, Integer> variantProperties = new LinkedHashMap<>();
                    for (Map.Entry<String, JsonElement> entry : variantElement.getAsJsonObject().entrySet()) {
                        variantProperties.put(entry.getKey(), entry.getValue().getAsInt());
                    }
                    if (variantProperties.isEmpty()) continue;
                    inventoryPresetEntry.variantProperties.add(variantProperties);
                }
            }
            if (jsonObject3.has("required")) {
                for (Map.Entry<String, JsonElement> entry : jsonObject3.getAsJsonObject("required").entrySet()) {
                    inventoryPresetEntry.requiredTraitCounts.put(entry.getKey(), entry.getValue().getAsInt());
                }
            }
            if (jsonObject3.has("options")) {
                for (JsonElement optionElement : jsonObject3.getAsJsonArray("options")) {
                    JsonObject optionObject = optionElement.getAsJsonObject();
                    inventoryPresetEntry.traitLimits.add(new InventoryTraitLimit(optionObject.get("name").getAsString(), optionObject.get("max").getAsInt()));
                }
            }
            InventoryBuilder.applyCatalogDefaults(inventoryPresetEntry);
            inventoryPresetEntry.requiredAmount = Math.max(1, Math.min(inventoryPresetEntry.requiredAmount, inventoryPresetEntry.getStackLimit()));
            inventoryPresetLayout.slotEntries[n] = inventoryPresetEntry;
        }
        return inventoryPresetLayout;
    }

    private static JsonObject serializeLayout(InventoryPresetLayout inventoryPresetLayout) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", inventoryPresetLayout.layoutName);
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < 41; ++i) {
            InventoryPresetEntry inventoryPresetEntry = inventoryPresetLayout.slotEntries[i];
            if (inventoryPresetEntry == null) continue;
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("slot", (Number)i);
            jsonObject2.addProperty("item", inventoryPresetEntry.itemId.toString());
            jsonObject2.addProperty("query", inventoryPresetEntry.searchQuery);
            jsonObject2.addProperty("match", inventoryPresetEntry.matchText);
            jsonObject2.addProperty("custom", Boolean.valueOf(inventoryPresetEntry.customNameMatching));
            jsonObject2.addProperty("amount", (Number)inventoryPresetEntry.requiredAmount);
            jsonObject2.addProperty("durability", (Number)inventoryPresetEntry.minimumDurabilityPercent);
            jsonObject2.addProperty("minDuration", (Number)inventoryPresetEntry.minimumDuration);
            jsonObject2.addProperty("price", (Number)inventoryPresetEntry.maximumPrice);
            jsonObject2.addProperty("texture", inventoryPresetEntry.textureValue);
            jsonObject2.addProperty("color", (Number)inventoryPresetEntry.colorValue);
            jsonObject2.addProperty("potion", inventoryPresetEntry.potionEffectId);
            jsonObject2.addProperty("exact", Boolean.valueOf(inventoryPresetEntry.exactNameMatch));
            jsonObject2.addProperty("duration", (Number)inventoryPresetEntry.requiredDuration);
            jsonObject2.addProperty("budget", (Number)inventoryPresetEntry.budgetLimit);
            jsonObject2.addProperty("stack", (Number)inventoryPresetEntry.requiredStackCount);
            jsonObject2.addProperty("search", inventoryPresetEntry.alternateSearchQuery);
            JsonObject jsonObject3 = new JsonObject();
            inventoryPresetEntry.requiredTraitCounts.forEach((arg_0, arg_1) -> ((JsonObject)jsonObject3).addProperty(arg_0, arg_1));
            jsonObject2.add("required", (JsonElement)jsonObject3);
            JsonArray jsonArray2 = new JsonArray();
            for (Map<String, Integer> map : inventoryPresetEntry.variantProperties) {
                Object object = new JsonObject();
                map.forEach((arg_0, arg_1) -> ((JsonObject)object).addProperty(arg_0, arg_1));
                jsonArray2.add((JsonElement)object);
            }
            jsonObject2.add("variants", (JsonElement)jsonArray2);
            JsonArray jsonArray3 = new JsonArray();
            for (Object object : inventoryPresetEntry.traitLimits) {
                JsonObject jsonObject4 = new JsonObject();
                jsonObject4.addProperty("name", ((InventoryTraitLimit)object).getTraitName());
                jsonObject4.addProperty("max", (Number)((InventoryTraitLimit)object).getMaximumCount());
                jsonArray3.add((JsonElement)jsonObject4);
            }
            jsonObject2.add("options", (JsonElement)jsonArray3);
            jsonArray.add((JsonElement)jsonObject2);
        }
        jsonObject.add("slots", (JsonElement)jsonArray);
        return jsonObject;
    }

    private static String formatLayoutEntries(InventoryPresetLayout inventoryPresetLayout, Set<String> set) {
        String string = InventoryBuilder.sanitizePresetName(inventoryPresetLayout.layoutName);
        String string2 = string + PRESET_FILE_EXTENSION;
        int n = 2;
        while (set.contains(string2.toLowerCase(Locale.ROOT))) {
            string2 = string + "-" + n + PRESET_FILE_EXTENSION;
            ++n;
        }
        return string2;
    }

    private static String sanitizePresetName(String string) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] nameCharacters = (string == null ? "" : string).trim().toCharArray();
        int n = nameCharacters.length;
        for (int i = 0; i < n; ++i) {
            char c = nameCharacters[i];
            boolean bl = c == ' ';
            if (bl && (stringBuilder.isEmpty() || stringBuilder.charAt(stringBuilder.length() - 1) == ' ')) continue;
            boolean bl3 = bl || c == '-' || c == '_' || Character.isLetterOrDigit(c);
            stringBuilder.append(bl3 ? c : (char)'_');
            if (stringBuilder.length() >= 48) break;
        }
        String sanitizedName = stringBuilder.toString().trim();
        if (sanitizedName.isEmpty()) {
            sanitizedName = "preset";
        }
        return reservedFileNamePattern.matcher(sanitizedName).matches() ? sanitizedName + "_" : sanitizedName;
    }

    @Generated
    public ActionSetting getOpenAction() {
        return this.open;
    }

    @Generated
    public ActionSetting getStopAction() {
        return this.stop;
    }

    @Generated
    public NumberSetting getDelaySetting() {
        return this.delay;
    }

    @Generated
    public NumberSetting getAttemptsSetting() {
        return this.attempts;
    }

    @Generated
    public NumberSetting getPagesSetting() {
        return this.pages;
    }

    @Generated
    public BooleanSetting getSortSetting() {
        return this.sort;
    }

    @Generated
    public long getDelayJitterMillis() {
        return this.delayJitterMillis;
    }

    @Generated
    public Timer getCooldownTimer() {
        return this.buildCooldownTimer;
    }

    @Generated
    public Timer getActionTimer() {
        return this.buildActionTimer;
    }

    @Generated
    public Deque<int[]> getContainerActionHistory() {
        return this.containerActionHistory;
    }

    @Generated
    public boolean isSearchResultDetected() {
        return this.searchResultDetected;
    }

    @Generated
    public int getSearchPage() {
        return this.searchPage;
    }

    @Generated
    public int getPageRetryCount() {
        return this.pageRetryCount;
    }

    @Generated
    public boolean isAwaitingSearchResponse() {
        return this.awaitingSearchResponse;
    }

    @Generated
    public int getCurrentPageOffset() {
        return this.currentPageOffset;
    }

    @Generated
    public int getCurrentItemIndex() {
        return this.currentItemIndex;
    }

    @Generated
    public InventoryBuildState getBuildState() {
        return this.buildState;
    }

    @Generated
    public InventoryPresetLayout getActivePreset() {
        return this.activePreset;
    }

    @Generated
    public int getActiveSlotIndex() {
        return this.activeSlotIndex;
    }

    @Generated
    public int getSearchAttempts() {
        return this.searchAttempts;
    }

    @Generated
    public int getPurchasedAmount() {
        return this.purchasedAmount;
    }

    @Generated
    public int getSelectedPurchaseSlot() {
        return this.selectedPurchaseSlot;
    }

    @Generated
    public int getPreviousSortSlot() {
        return this.previousSortSlot;
    }

    @Generated
    public long getCurrentListingPrice() {
        return this.currentListingPrice;
    }

    @Generated
    public int getLastAuctionSlot() {
        return this.lastAuctionSlot;
    }

    @Generated
    public int getCurrentAuctionSlot() {
        return this.currentAuctionSlot;
    }

    @Generated
    public int getFallbackHotbarSlot() {
        return this.fallbackHotbarSlot;
    }

    @Generated
    public int getContainerPageStage() {
        return this.containerPageStage;
    }

    @Generated
    public int getPendingContainerSlot() {
        return this.pendingContainerSlot;
    }

    @Generated
    public String getPendingSlotDescription() {
        return this.pendingSlotDescription;
    }

    @Generated
    public int getStalledSlotChecks() {
        return this.stalledSlotChecks;
    }

    @Generated
    public Set<Integer> getOccupiedPresetSlots() {
        return this.occupiedPresetSlots;
    }

    @Generated
    public int getCompletedItemCount() {
        return this.completedItemCount;
    }

    @Generated
    public Set<String> getPurchasedListingKeys() {
        return this.purchasedListingKeys;
    }

    @Generated
    public String getStatusMessage() {
        return this.statusMessage;
    }

    @Generated
    public boolean isPurchaseResponseReceived() {
        return this.purchaseResponseReceived;
    }

    @Generated
    public String getResponseMessage() {
        return this.responseMessage;
    }

    @Generated
    public int getInsufficientFundsResponses() {
        return this.insufficientFundsResponses;
    }

    @Generated
    public EventListener<KeyPressEvent> getKeyPressListener() {
        return this.keyPressListener;
    }

    @Generated
    public EventListener<ReceivePacketEvent> getReceivePacketListener() {
        return this.receivePacketListener;
    }

    @Generated
    public int getPendingPurchaseAmount() {
        return this.pendingPurchaseAmount;
    }

    @Generated
    public static List<InventoryPresetLayout> listPresetNames() {
        return loadedPresets;
    }

    @Generated
    public static List<InventoryPresetEntry> listPresetFiles() {
        return catalogEntries;
    }

    static {
        listingItemNamePattern = Pattern.compile("\u0443\u0432\u0435\u043b\u0438\u0447\u0438\u0442\u044c\\D+(\\d+)");
        listingChatPattern = Pattern.compile("\u0443\u043c\u0435\u043d\u044c\u0448\u0438\u0442\u044c\\D+(\\d+)");
        durationPattern = Pattern.compile("\\((\\d{1,2}):(\\d{2})\\)");
        availableSlotsPattern = Pattern.compile("\\((\\d+)\\s*/\\s*(\\d+)\\)");
        catalogEntries = new ArrayList<InventoryPresetEntry>();
    }
}
