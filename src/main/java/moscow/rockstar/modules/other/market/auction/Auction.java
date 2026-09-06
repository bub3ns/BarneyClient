/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.EquippableComponent
 *  net.minecraft.EquipmentSlot
 *  net.minecraft.PlayerEntity
 *  net.minecraft.ScreenHandler
 *  net.minecraft.GenericContainerScreenHandler
 *  net.minecraft.SlotActionType
 *  net.minecraft.Slot
 *  net.minecraft.Item
 *  net.minecraft.Item$TooltipContext
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.LingeringPotionItem
 *  net.minecraft.PotionItem
 *  net.minecraft.SplashPotionItem
 *  net.minecraft.TooltipType
 *  net.minecraft.Enchantment
 *  net.minecraft.Enchantments
 *  net.minecraft.World
 *  net.minecraft.Text
 *  net.minecraft.Packet
 *  net.minecraft.CloseHandledScreenC2SPacket
 *  net.minecraft.HandledScreen
 *  net.minecraft.RegistryKey
 *  net.minecraft.GameMessageS2CPacket
 *  net.minecraft.DataComponentTypes
 */
package moscow.rockstar.modules.other.market.auction;
import moscow.rockstar.ui.localization.Localization;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.items.DonorItemParser;
import moscow.rockstar.items.EnchantmentUtils;
import moscow.rockstar.items.ItemMetadataUtils;
import moscow.rockstar.market.MarketInventoryAnalyzer;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.mixin.accessors.HandledScreenAccessor;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.other.market.auction.AuctionEntry;
import moscow.rockstar.modules.other.market.auction.AuctionListing;
import moscow.rockstar.modules.other.market.auction.AuctionPrice;
import moscow.rockstar.modules.other.market.auction.AuctionSlot;
import moscow.rockstar.modules.other.market.auction.PriceMode;
import moscow.rockstar.modules.other.market.resell.SellMode;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.colors.GradientColors;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.settings.StringSetting;
import moscow.rockstar.ui.color.ColorPickerScreen;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.notifications.ItemNotification;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.util.Timer;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.registry.RegistryKey;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.component.DataComponentTypes;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.render.HudRenderEvent;
import pyrock.events.render.ScreenRenderEvent;
import pyrock.events.window.ContainerClickEvent;
import pyrock.events.window.ContainerReleaseEvent;
import pyrock.events.window.KeyPressEvent;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auction", category=ModuleCategory.OTHER)
public class Auction
extends Module {
    private final List<AuctionEntry> auctionListings = new ArrayList<AuctionEntry>();
    private double averageListingPrice = 0.0;
    private double minimumListingPrice = Double.MAX_VALUE;
    private static final long MIN_SEARCH_DELAY = 100L;
    private static final long MAX_SEARCH_DELAY = 5000L;
    private static final double PRICE_EPSILON = 1.6;
    private static final Pattern pageCountPattern = Pattern.compile("\\b(\\d+)\\s*/\\s*(\\d+)\\b");
    private static final String SEARCH_COMMAND = "/ah search auto confirm";
    private static final long SCREEN_TIMEOUT = 3000L;
    private String searchQueryText = "";
    private int bestPriceSlot = -1;
    private double bestExchangeRate = -1.0;
    private long exchangeBalance = -1L;
    private long coinBalance = -1L;
    private ColorSetting lowPriceColorSetting;
    private ColorSetting highPriceColorSetting;
    private BooleanSetting showAboveAverageSetting;
    private BooleanSetting fastBuySetting;
    private IntegerSetting searchKeySetting;
    private BooleanSetting autoConfirmSetting;
    private BooleanSetting autoSellSetting;
    private ModeSetting autoSellModeSetting;
    private ModeSetting.Option funTimeSellOption;
    private ModeSetting.Option holyWorldSellOption;
    private StringSetting autoSellPagesSetting;
    private IntegerSetting autoSellBindSetting;
    private ModeSetting priceModeSetting;
    private ModeSetting.Option perUnitPriceOption;
    private ModeSetting.Option totalPriceOption;
    private MultiBooleanSetting armorFilter;
    private MultiBooleanSetting.Option noSpikeArmorOption;
    private MultiBooleanSetting.Option noProtectionFiveOption;
    private MultiBooleanSetting.Option noDurabilityArmorOption;
    private NumberSetting minimumDurabilitySetting;
    private MultiBooleanSetting.Option noRepairArmorOption;
    private MultiBooleanSetting pickaxeFilter;
    private MultiBooleanSetting.Option silkTouchPickaxeOption;
    private MultiBooleanSetting.Option noMegaPickaxeOption;
    private MultiBooleanSetting.Option noMinerPickaxeOption;
    private MultiBooleanSetting.Option noRepairPickaxeOption;
    private MultiBooleanSetting.Option noMagnetPickaxeOption;
    private MultiBooleanSetting.Option noExperiencePickaxeOption;
    private MultiBooleanSetting potionFilter;
    private MultiBooleanSetting.Option noLevelThreePotionOption;
    private MultiBooleanSetting.Option noCombinedPotionOption;
    private MultiBooleanSetting.Option noUnderSixMinutesPotionOption;
    private boolean autoSellKeyHeld = false;
    private boolean priceOverlayVisible = false;
    private final Timer searchCooldownTimer = new Timer();
    private final Timer purchaseActionTimer = new Timer();
    private final Timer auctionTargetTimer = new Timer();
    private final List<AuctionPrice> pendingPriceHistory = new ArrayList<AuctionPrice>();
    private PriceMode pricingState = PriceMode.IDLE;
    private Item selectedItemType;
    private ItemStack selectedItemStack = ItemStack.EMPTY;
    private String selectedItemDisplayName = "";
    private int configuredSearchPages = 1;
    private int currentSearchPage;
    private int purchaseSlot = -1;
    private static final Item[] sellableItemTypes = new Item[]{Items.TRIDENT, Items.DIAMOND_SWORD, Items.NETHERITE_AXE, Items.GOLDEN_APPLE, Items.TOTEM_OF_UNDYING, Items.ENDER_PEARL, Items.ELYTRA, Items.SHIELD, Items.BOW, Items.CROSSBOW, Items.FIREWORK_ROCKET, Items.ENCHANTED_GOLDEN_APPLE};
    private final ColorPickerScreen colorPicker = new ColorPickerScreen(0.0f, 0.0f);
    private boolean sellingInitialized;
    private SellMode sellMode = SellMode.OTHER;
    private final EventListener<KeyPressEvent> keyPressListener = keyPressEvent -> {
        int n = keyPressEvent.getKey();
        int n2 = keyPressEvent.getAction();
        boolean bl = this.autoSellKeyHeld = n == 340 && (n2 == 2 || n2 == 1 || n2 == 0);
        if (this.autoSellBindSetting.isIntValid(n) && n2 == 1 && Auction.minecraftClient.currentScreen == null) {
            this.startAutoSellSearch();
            return;
        }
        if (this.searchKeySetting.isIntValid(n) && n2 == 1 && Auction.minecraftClient.currentScreen == null) {
            ItemStack class_17992 = Auction.minecraftClient.player.getMainHandStack();
            if (class_17992.isEmpty()) {
                RockstarClient.create().getUiComponentProcessor().enqueueNotification(new ItemNotification(Localization.translate("auction.hold_item_main_hand"), sellableItemTypes[MathUtils.RANDOM.nextInt(sellableItemTypes.length)]));
                return;
            }
            DonorItemParser.DonorItem donorItem = DonorItemParser.parseDonorItem(class_17992);
            String string = donorItem != null ? donorItem.getDisplayName(class_17992) : ItemMetadataUtils.cleanDisplayName(class_17992);
            Auction.minecraftClient.player.networkHandler.sendChatCommand("ah search " + string);
        }
    };
    private final EventListener<ReceivePacketEvent> receivePacketListener = receivePacketEvent -> {
        if (!this.autoConfirmSetting.isEnabled() || Auction.minecraftClient.player == null) {
            return;
        }
        Packet<?> class_25962 = receivePacketEvent.getPacket();
        if (!(class_25962 instanceof GameMessageS2CPacket)) {
            return;
        }
        GameMessageS2CPacket class_74392 = (GameMessageS2CPacket)class_25962;
        if (!this.normalizeChatText(class_74392.content()).contains(SEARCH_COMMAND)) {
            return;
        }
        if (!this.searchCooldownTimer.hasElapsed(3000L)) {
            return;
        }
        this.searchCooldownTimer.reset();
        Auction.minecraftClient.player.networkHandler.sendChatCommand(SEARCH_COMMAND.substring(1));
    };
    private final EventListener<HudRenderEvent> hudRenderListener = hudRenderEvent -> {
        if (Auction.minecraftClient.currentScreen == null) {
            this.searchQueryText = "";
            this.colorPicker.setOpen(false);
            if (this.colorPicker.getOpenAnimation().getValue() > 0.0f) {
                this.drawAuctionOverlay(hudRenderEvent.getContext());
            }
        }
    };
    private final EventListener<ScreenRenderEvent> screenRenderListener = screenRenderEvent -> {
        Object object = Auction.minecraftClient.currentScreen;
        if (!(object instanceof HandledScreen)) {
            return;
        }
        HandledScreen BlockStateProviderType = (HandledScreen)object;
        if (!this.isSearchResultValid(BlockStateProviderType.getTitle().getString())) {
            return;
        }
        object = this.searchQueryText.toLowerCase(Locale.ROOT);
        boolean bl = this.hasKeyword((String)object, "\u0438\u043d\u0432\u0438\u0437", "\u043d\u0435\u0432\u0438\u0434", "invis");
        boolean bl2 = !bl && this.isAuctionReady();
        this.colorPicker.setOpen(bl2);
        HandledScreenAccessor handledScreenAccessor = (HandledScreenAccessor)BlockStateProviderType;
        try {
            java.util.Iterator<AuctionEntry> auctionIterator = this.auctionListings.iterator();
            while (auctionIterator.hasNext()) {
                Slot class_17353;
                AuctionEntry auctionEntry = auctionIterator.next();
                if (!this.showAboveAverageSetting.isEnabled() && auctionEntry.getEffectivePrice() > this.averageListingPrice || (class_17353 = BlockStateProviderType.getScreenHandler().getSlot(auctionEntry.getSlotId())) == null) continue;
                int n = handledScreenAccessor.getX() + class_17353.x;
                int n2 = handledScreenAccessor.getY() + class_17353.y;
                ColorRGBA colorRGBA = this.formatPriceColor(auctionEntry.getEffectivePrice());
                screenRenderEvent.getContext().drawRoundedRect((float)n, (float)n2, 16.0f, 16.0f, WidgetState.uniform(1.0f), new GradientColors(colorRGBA.withAlpha(0.0f), colorRGBA.withAlpha(0.8f * colorRGBA.getAlpha()), colorRGBA.withAlpha(0.0f), colorRGBA.withAlpha(0.8f * colorRGBA.getAlpha())));
            }
            if (this.isItemNameMatch(this.searchQueryText) && this.bestPriceSlot != -1 && this.exchangeBalance != -1L && (double)this.exchangeBalance >= this.bestExchangeRate) {
                Slot bestPriceSlot = BlockStateProviderType.getScreenHandler().getSlot(this.bestPriceSlot);
                if (bestPriceSlot != null) {
                    int n = handledScreenAccessor.getX() + bestPriceSlot.x;
                    int n3 = handledScreenAccessor.getY() + bestPriceSlot.y;
                    ColorRGBA colorRGBA = new ColorRGBA(60.0f, 255.0f, 120.0f, 220.0f);
                    screenRenderEvent.getContext().drawRoundedRect((float)n, (float)n3, 16.0f, 16.0f, WidgetState.uniform(1.0f), new GradientColors(colorRGBA.withAlpha(0.0f), colorRGBA.withAlpha(0.85f * colorRGBA.getAlpha()), colorRGBA.withAlpha(0.0f), colorRGBA.withAlpha(0.85f * colorRGBA.getAlpha())));
                }
            }
        }
        catch (Exception exception) {
            this.finishAuctionSession();
        }
        if (bl2 || this.colorPicker.getOpenAnimation().getValue() > 0.0f) {
            this.drawAuctionOverlay(screenRenderEvent.getContext());
        }
    };
    private final EventListener<ContainerClickEvent> containerClickListener = containerClickEvent -> {
        Object object;
        this.colorPicker.mouseClicked(containerClickEvent.getX(), containerClickEvent.getY(), PointerAction.fromButtonCode(containerClickEvent.getButton()));
        if (this.fastBuySetting.isEnabled() && this.autoSellKeyHeld && (object = Auction.minecraftClient.currentScreen) instanceof HandledScreen) {
            Slot class_17352;
            HandledScreen BlockStateProviderType = (HandledScreen)object;
            HandledScreenAccessor screenAccessor = (HandledScreenAccessor)BlockStateProviderType;
            if (this.isSearchResultValid(this.searchQueryText) && (class_17352 = screenAccessor.getFocusedSlot()) != null && class_17352.hasStack()) {
                AuctionEntry auctionEntry2 = this.auctionListings.stream().filter(auctionEntry -> auctionEntry.getSlotId() == class_17352.id).findFirst().orElse(null);
                if (auctionEntry2 == null) {
                    return;
                }
                double d = this.minimumListingPrice * 2.0;
                if (auctionEntry2.getEffectivePrice() > d) {
                    RockstarClient.create().getUiComponentProcessor().enqueueNotification(new ItemNotification(Localization.translate("auction.price_above_min"), sellableItemTypes[MathUtils.RANDOM.nextInt(sellableItemTypes.length)]));
                    return;
                }
                this.priceOverlayVisible = true;
            }
        }
    };
    private final EventListener<ContainerReleaseEvent> containerReleaseListener = containerReleaseEvent -> this.colorPicker.mouseReleased(containerReleaseEvent.getX(), containerReleaseEvent.getY(), PointerAction.fromButtonCode(containerReleaseEvent.getButton()));

    public Auction() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.lowPriceColorSetting = new ColorSetting(this, "modules.settings.auction.low_price_color").setColor(new ColorRGBA(60.0f, 255.0f, 60.0f, 250.0f));
        this.highPriceColorSetting = new ColorSetting(this, "modules.settings.auction.high_price_color").setColor(new ColorRGBA(255.0f, 255.0f, 60.0f, 250.0f));
        this.showAboveAverageSetting = new BooleanSetting(this, "modules.settings.auction.show_yellow");
        this.fastBuySetting = new BooleanSetting(this, "modules.settings.auction.fast_buy");
        this.searchKeySetting = new IntegerSetting(this, "modules.settings.auction.search");
        this.autoConfirmSetting = new BooleanSetting(this, "modules.settings.auction.auto_confirm");
        this.autoSellSetting = new BooleanSetting(this, "modules.settings.auction.auto_sell");
        this.autoSellModeSetting = new ModeSetting((SettingOwner)this, "modules.settings.auction.auto_sell.mode", () -> !this.autoSellSetting.isEnabled());
        this.funTimeSellOption = new ModeSetting.Option(this.autoSellModeSetting, "modules.settings.auction.auto_sell.mode.funtime").select();
        this.holyWorldSellOption = new ModeSetting.Option(this.autoSellModeSetting, "modules.settings.auction.auto_sell.mode.holyworld");
        this.autoSellPagesSetting = new StringSetting((SettingOwner)this, "modules.settings.auction.auto_sell.pages", () -> !this.autoSellSetting.isEnabled()).setValue("1").setNumericOnly(true);
        this.autoSellBindSetting = new IntegerSetting(this, "modules.settings.auction.auto_sell.bind", () -> !this.autoSellSetting.isEnabled());
        this.priceModeSetting = new ModeSetting(this, "modules.settings.auction.price_mode");
        this.perUnitPriceOption = new ModeSetting.Option(this.priceModeSetting, "modules.settings.auction.price_mode.per_unit");
        this.totalPriceOption = new ModeSetting.Option(this.priceModeSetting, "modules.settings.auction.price_mode.total");
        this.armorFilter = new MultiBooleanSetting((SettingOwner)this, "modules.settings.auction.armor", () -> !this.isArmorModeActive());
        this.noSpikeArmorOption = new MultiBooleanSetting.Option(this.armorFilter, "modules.settings.auction.armor.no_spike").select();
        this.noProtectionFiveOption = new MultiBooleanSetting.Option(this.armorFilter, "modules.settings.auction.armor.no_prot5").select();
        this.noDurabilityArmorOption = new MultiBooleanSetting.Option(this.armorFilter, "modules.settings.auction.armor.no_durability");
        this.minimumDurabilitySetting = new NumberSetting((SettingOwner)this, "modules.settings.auction.armor.min_durability", () -> !this.noDurabilityArmorOption.isSelected() || !this.isArmorModeActive()).setMinValue(1.0f).setMaxValue(100.0f).setStep(1.0f).setValue(100.0f).setUnit("%");
        this.noRepairArmorOption = new MultiBooleanSetting.Option(this.armorFilter, "modules.settings.auction.armor.no_repair");
        this.pickaxeFilter = new MultiBooleanSetting((SettingOwner)this, "modules.settings.auction.pickaxe", () -> !this.isPickaxeModeActive());
        this.silkTouchPickaxeOption = new MultiBooleanSetting.Option(this.pickaxeFilter, "modules.settings.auction.pickaxe.silk_touch");
        this.noMegaPickaxeOption = new MultiBooleanSetting.Option(this.pickaxeFilter, "modules.settings.auction.pickaxe.noMega");
        this.noMinerPickaxeOption = new MultiBooleanSetting.Option(this.pickaxeFilter, "modules.settings.auction.pickaxe.noMiner");
        this.noRepairPickaxeOption = new MultiBooleanSetting.Option(this.pickaxeFilter, "modules.settings.auction.armor.no_repair");
        this.noMagnetPickaxeOption = new MultiBooleanSetting.Option(this.pickaxeFilter, "modules.settings.auction.pickaxe.noMagnit");
        this.noExperiencePickaxeOption = new MultiBooleanSetting.Option(this.pickaxeFilter, "modules.settings.auction.pickaxe.noExp");
        this.potionFilter = new MultiBooleanSetting((SettingOwner)this, "modules.settings.auction.potions", () -> !this.isPotionModeActive());
        this.noLevelThreePotionOption = new MultiBooleanSetting.Option(this.potionFilter, "modules.settings.auction.potions.no_level3");
        this.noCombinedPotionOption = new MultiBooleanSetting.Option(this.potionFilter, "modules.settings.auction.potions.no_combined");
        this.noUnderSixMinutesPotionOption = new MultiBooleanSetting.Option(this.potionFilter, "modules.settings.auction.potions.no_under_6min");
    }

    private void initializeSellingState() {
        if (this.sellingInitialized) {
            return;
        }
        this.sellingInitialized = true;
        for (Setting setting : this.getSettings()) {
            if (!(setting instanceof MultiBooleanSetting)) continue;
            MultiBooleanSetting multiBooleanSetting = (MultiBooleanSetting)setting;
            this.colorPicker.addSetting(multiBooleanSetting);
        }
    }

    @Override
    public void onTick() {
        if (this.pricingState != PriceMode.IDLE) {
            this.startSearch();
            return;
        }
        Object object = Auction.minecraftClient.currentScreen;
        if (!(object instanceof HandledScreen)) {
            this.finishAuctionSession();
            this.priceOverlayVisible = false;
            return;
        }
        HandledScreen BlockStateProviderType = (HandledScreen)object;
        String screenTitle = BlockStateProviderType.getTitle().getString();
        this.searchQueryText = screenTitle;
        if (this.priceOverlayVisible && this.isAuctionScreen(BlockStateProviderType)) {
            ScreenHandler class_17032 = Auction.minecraftClient.player.currentScreenHandler;
            if (class_17032 instanceof GenericContainerScreenHandler) {
                GenericContainerScreenHandler class_17072 = (GenericContainerScreenHandler)class_17032;
                Auction.minecraftClient.interactionManager.clickSlot(class_17072.syncId, 11, 0, SlotActionType.PICKUP, (PlayerEntity)Auction.minecraftClient.player);
                this.priceOverlayVisible = false;
            }
            return;
        }
        if (!this.isSearchResultValid(screenTitle)) {
            this.finishAuctionSession();
            return;
        }
        this.sendAuctionSearch(screenTitle);
        this.parseAuctionListings(BlockStateProviderType);
        super.onTick();
    }

    private String normalizeChatText(Text class_25612) {
        return class_25612.getString().replaceAll("(?i)\u00a7[0-9a-fk-or]", "").replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private boolean isAuctionReady() {
        for (Setting setting : this.getSettings()) {
            MultiBooleanSetting multiBooleanSetting;
            if (!(setting instanceof MultiBooleanSetting) || !(multiBooleanSetting = (MultiBooleanSetting)setting).hasValidSettingValue()) continue;
            return true;
        }
        return false;
    }

    private void drawAuctionOverlay(CustomDrawContext customDrawContext) {
        this.initializeSellingState();
        RockstarDrawContext drawContext = RockstarDrawContext.create(customDrawContext, Auction.minecraftClient.currentScreen == null ? -1 : (int)UiUtils.mousePosition().getX(), Auction.minecraftClient.currentScreen == null ? -1 : (int)UiUtils.mousePosition().getY(), MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
        this.colorPicker.setWidth(120.0f);
        this.colorPicker.setPosition(10.0f, INSTANCE.height() / 2.0f - this.colorPicker.getHeight() / 2.0f);
        this.colorPicker.render(drawContext);
    }

    private ColorRGBA formatPriceColor(double d) {
        double d2 = this.averageListingPrice - this.minimumListingPrice;
        float f = d2 > 0.0 ? (float)((d - this.minimumListingPrice) / d2) : 0.0f;
        f = Math.max(0.0f, Math.min(1.0f, f));
        ColorRGBA colorRGBA = this.lowPriceColorSetting.getColor();
        ColorRGBA colorRGBA2 = this.highPriceColorSetting.getColor();
        ColorRGBA colorRGBA3 = colorRGBA.mix(colorRGBA2, f);
        float f2 = 1.0f - f * 0.6f;
        return colorRGBA3.mulAlpha(f2);
    }

    private boolean isAuctionScreen(HandledScreen<?> BlockStateProviderType) {
        String string = MarketInventoryAnalyzer.normalizeMarketText(BlockStateProviderType.getTitle().getString());
        if (string.contains("\u043f\u043e\u043a\u0443\u043f\u043a") || string.contains("\u043f\u043e\u0434\u0442\u0432\u0435\u0440")) {
            return true;
        }
        return !MarketInventoryAnalyzer.isMarketScreen(BlockStateProviderType) && !this.isSearchResultValid(BlockStateProviderType.getTitle().getString());
    }

    private boolean isSearchResultValid(String string) {
        if (MarketInventoryAnalyzer.isPriceOrMarketText(string)) {
            return true;
        }
        return this.hasKeyword(MarketInventoryAnalyzer.normalizeMarketText(string), "\u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a", "\u0448\u043b\u0435\u043c", "\u043f\u043e\u043d\u043e\u0436", "\u0431\u043e\u0442\u0438\u043d", "\u043a\u0438\u0440\u043a", "\u0437\u0435\u043b\u044c");
    }

    private boolean isItemNameMatch(String string) {
        return MarketInventoryAnalyzer.isRecognizedMarketText(string);
    }

    @Override
    public void onDisable() {
        this.resetSearchResults();
        this.finishAuctionSession();
        super.onDisable();
    }

    private void startAutoSellSearch() {
        if (!this.autoSellSetting.isEnabled() || Auction.minecraftClient.player == null || Auction.minecraftClient.world == null) {
            return;
        }
        ItemStack class_17992 = Auction.minecraftClient.player.getMainHandStack();
        if (class_17992.isEmpty()) {
            RockstarClient.create().getUiComponentProcessor().enqueueNotification(new ItemNotification(Localization.translate("auction.take_item_in_hand"), sellableItemTypes[MathUtils.RANDOM.nextInt(sellableItemTypes.length)]));
            return;
        }
        this.selectedItemType = class_17992.getItem();
        this.selectedItemStack = class_17992.copy();
        this.selectedItemDisplayName = ItemMetadataUtils.cleanDisplayName(class_17992).trim();
        if (this.selectedItemDisplayName.isEmpty()) {
            RockstarClient.create().getUiComponentProcessor().enqueueNotification(new ItemNotification(Localization.translate("auction.item_name_failed"), sellableItemTypes[MathUtils.RANDOM.nextInt(sellableItemTypes.length)]));
            return;
        }
        this.configuredSearchPages = this.getConfiguredSearchPageCount();
        this.currentSearchPage = 0;
        this.purchaseSlot = -1;
        this.pendingPriceHistory.clear();
        this.pricingState = PriceMode.SEARCHING;
        this.purchaseActionTimer.reset();
        this.auctionTargetTimer.reset();
    }

    private void startSearch() {
        if (Auction.minecraftClient.player == null || Auction.minecraftClient.world == null) {
            this.resetSearchResults();
            return;
        }
        switch (this.pricingState.ordinal()) {
            case 1: {
                this.parseSearchResults();
                break;
            }
            case 2: {
                this.submitPurchase();
                break;
            }
            case 3: {
                this.retryPurchase();
                break;
            }
        }
    }

    private void parseSearchResults() {
        if (!this.purchaseActionTimer.hasElapsed(100L)) {
            return;
        }
        this.resetPricingState();
        Auction.minecraftClient.player.networkHandler.sendChatCommand("ah search " + this.selectedItemDisplayName);
        this.setPriceState(PriceMode.PROCESSING);
    }

    private void submitPurchase() {
        ScreenHandler class_17032 = this.getOpenAuctionContainer();
        if (class_17032 == null) {
            if (this.auctionTargetTimer.hasElapsed(5000L)) {
                Notification.warning((Text)Text.literal((String)Localization.translate("auction.open_failed")));
                this.resetSearchResults();
            }
            return;
        }
        if (!this.purchaseActionTimer.hasElapsed(100L)) {
            return;
        }
        ++this.currentSearchPage;
        this.pendingPriceHistory.addAll(this.readAuctionEntries(class_17032));
        if (this.currentSearchPage >= this.configuredSearchPages) {
            this.finishPurchase();
            return;
        }
        AuctionSlot auctionSlot = this.getAuctionSlot(class_17032);
        if (auctionSlot != null && auctionSlot.getCurrentCount() >= auctionSlot.getTotalCount()) {
            this.finishPurchase();
            return;
        }
        int n = this.getAuctionPage(class_17032);
        if (n == -1) {
            this.finishPurchase();
            return;
        }
        this.purchaseSlot = auctionSlot == null ? -1 : auctionSlot.getCurrentCount() + 1;
        Auction.minecraftClient.interactionManager.clickSlot(class_17032.syncId, n, 0, SlotActionType.PICKUP, (PlayerEntity)Auction.minecraftClient.player);
        this.setPriceState(PriceMode.COMPLETING);
    }

    private void retryPurchase() {
        ScreenHandler class_17032 = this.getOpenAuctionContainer();
        if (class_17032 == null) {
            this.finishPurchase();
            return;
        }
        AuctionSlot auctionSlot = this.getAuctionSlot(class_17032);
        if (auctionSlot != null && this.purchaseSlot != -1 && auctionSlot.getCurrentCount() >= this.purchaseSlot) {
            this.setPriceState(PriceMode.PROCESSING);
            return;
        }
        if (this.purchaseSlot == -1 && this.purchaseActionTimer.hasElapsed(100L)) {
            this.setPriceState(PriceMode.PROCESSING);
            return;
        }
        if (this.auctionTargetTimer.hasElapsed(5000L)) {
            this.finishPurchase();
        }
    }

    private void finishPurchase() {
        AuctionListing auctionListing = this.summarizeAuction(this.pendingPriceHistory);
        if (auctionListing.getUnits() > 0) {
            long l = Math.round(auctionListing.getAveragePrice());
            int n = Math.max(1, this.selectedItemStack.getCount());
            long l2 = l * (long)n;
            this.resetPricingState();
            Auction.minecraftClient.player.networkHandler.sendChatCommand("ah sell " + l2);
            Notification.info((Text)Text.literal((String)Localization.translateFormatted("auction.listed", this.selectedItemType.getName().getString(), n, this.formatPrice(l2), this.formatPrice(l))));
        } else {
            Notification.warning((Text)Text.literal((String)Localization.translate("auction.no_prices")));
            this.resetPricingState();
        }
        this.resetSearchResults();
    }

    private void setPriceState(PriceMode priceMode) {
        this.pricingState = priceMode;
        this.purchaseActionTimer.reset();
        this.auctionTargetTimer.reset();
    }

    private void resetSearchResults() {
        this.pricingState = PriceMode.IDLE;
        this.selectedItemType = null;
        this.selectedItemStack = ItemStack.EMPTY;
        this.selectedItemDisplayName = "";
        this.configuredSearchPages = 1;
        this.currentSearchPage = 0;
        this.purchaseSlot = -1;
        this.pendingPriceHistory.clear();
    }

    private boolean isAuctionItemValid(ItemStack class_17992) {
        EquippableComponent class_101922 = (EquippableComponent)class_17992.get(DataComponentTypes.EQUIPPABLE);
        if (class_101922 == null || class_17992.getMaxDamage() <= 0) {
            return false;
        }
        return switch (class_101922.slot()) {
            case EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET -> true;
            default -> false;
        };
    }

    private boolean isAuctionItemValidWithLore(ItemStack class_17992, List<Text> list) {
        List<String> list2;
        boolean bl;
        Item class_17922 = class_17992.getItem();
        if (this.sellMode == SellMode.ARMOR && this.isAuctionItemValid(class_17992)) {
            double d;
            if (this.noSpikeArmorOption.isSelected() && EnchantmentUtils.hasEnchantments(class_17992, Enchantments.THORNS)) {
                return true;
            }
            if (this.noProtectionFiveOption.isSelected() && EnchantmentUtils.getEnchantmentLevel(class_17992, (RegistryKey<Enchantment>)Enchantments.PROTECTION) < 5) {
                return true;
            }
            if (this.noDurabilityArmorOption.isSelected() && class_17992.getMaxDamage() > 0 && (d = (double)(class_17992.getMaxDamage() - class_17992.getDamage()) / (double)class_17992.getMaxDamage() * 100.0) < (double)this.minimumDurabilitySetting.getValue()) {
                return true;
            }
            if (this.noRepairArmorOption.isSelected() && !EnchantmentUtils.hasEnchantments(class_17992, Enchantments.MENDING)) {
                return true;
            }
        }
        if (this.sellMode == SellMode.PICKAXE && class_17922.getTranslationKey().contains("pickaxe")) {
            if (this.silkTouchPickaxeOption.isSelected() && !EnchantmentUtils.hasEnchantments(class_17992, Enchantments.SILK_TOUCH)) {
                return true;
            }
            if (this.noRepairPickaxeOption.isSelected() && !EnchantmentUtils.hasEnchantments(class_17992, Enchantments.MENDING)) {
                return true;
            }
            if (this.noMagnetPickaxeOption.isSelected() && !EnchantmentUtils.containsEnchantmentText(list, "\u043c\u0430\u0433\u043d\u0438\u0442")) {
                return true;
            }
            if (this.noMegaPickaxeOption.isSelected() && !EnchantmentUtils.containsEnchantmentText(list, "\u043c\u0435\u0433\u0430-\u0431\u0443\u043b\u044c\u0434\u043e\u0437\u0435\u0440")) {
                return true;
            }
            if (this.noExperiencePickaxeOption.isSelected() && !EnchantmentUtils.containsEnchantmentLevel(list, "\u043e\u043f\u044b\u0442\u043d\u044b\u0439", 3)) {
                return true;
            }
            if (this.noMinerPickaxeOption.isSelected() && !EnchantmentUtils.containsEnchantmentLevel(list, "\u0431\u0443\u043b\u044c\u0434\u043e\u0437\u0435\u0440", 2)) {
                return true;
            }
        }
        if (this.sellMode == SellMode.POTION && (class_17922 instanceof PotionItem || class_17922 instanceof SplashPotionItem || class_17922 instanceof LingeringPotionItem) && !(bl = this.isPickaxeItemValid(list2 = list.stream().map(class_25612 -> class_25612.getString().toLowerCase(Locale.ROOT)).toList()))) {
            boolean bl2 = this.isPotionListValid(list2);
            if (this.noLevelThreePotionOption.isSelected() && bl2 && !this.isArmorItemValid(list2)) {
                return true;
            }
            if (this.noCombinedPotionOption.isSelected() && bl2 && !this.isPotionItemSelected(list2)) {
                return true;
            }
            return this.noUnderSixMinutesPotionOption.isSelected() && this.isItemCategoryValid(list2);
        }
        return false;
    }

    private boolean isPotionListValid(List<String> list) {
        for (String string : list) {
            String string2 = string.toLowerCase(Locale.ROOT);
            if (string2.contains("\u0441\u0438\u043b\u0430") && !string2.contains("\u0443\u0440\u043e\u043d") || string2.contains("\u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c") && !string2.contains("\u0430\u0442\u0430\u043a\u0438")) {
                return true;
            }
            if (!string2.contains("strength") && (!string2.contains("speed") || string2.contains("attack"))) continue;
            return true;
        }
        return false;
    }

    private boolean isArmorModeActive() {
        return this.sellMode == SellMode.ARMOR || Auction.minecraftClient.currentScreen == RockstarClient.create().getMinecraftScreen();
    }

    private boolean isPickaxeModeActive() {
        return this.sellMode == SellMode.PICKAXE || Auction.minecraftClient.currentScreen == RockstarClient.create().getMinecraftScreen();
    }

    private boolean isPotionModeActive() {
        if (this.sellMode == SellMode.POTION) {
            String string = this.searchQueryText.toLowerCase(Locale.ROOT);
            return !this.hasKeyword(string, "\u0438\u043d\u0432\u0438\u0437", "\u043d\u0435\u0432\u0438\u0434", "invis");
        }
        return Auction.minecraftClient.currentScreen == RockstarClient.create().getMinecraftScreen();
    }

    private boolean hasKeyword(String string, String ... stringArray) {
        for (String string2 : stringArray) {
            if (!string.contains(string2)) continue;
            return true;
        }
        return false;
    }

    private void sendAuctionSearch(String string) {
        if (this.isPotionNameExcluded(MarketInventoryAnalyzer.normalizeMarketText(string))) {
            return;
        }
        if (this.isPotionNameExcluded(this.selectedItemDisplayName.toLowerCase(Locale.ROOT))) {
            return;
        }
        this.sellMode = SellMode.OTHER;
    }

    private boolean isPotionNameExcluded(String string) {
        if (string.isEmpty()) {
            return false;
        }
        if (this.hasKeyword(string, "\u0437\u0435\u043b\u044c", "\u0431\u0430\u0444", "\u0431\u0430\u0444\u0444", "\u0441\u0438\u043b\u0430", "\u0441\u0438\u043b\u044b", "\u0441\u0438\u043b\u043a\u0430", "\u0441\u0438\u043b\u044c\u043a\u0430", "\u0441\u043a\u043e\u0440\u043e\u0441\u0442", "\u0438\u043d\u0432\u0438\u0437", "\u0438\u043d\u0432\u0438\u0437\u043a", "\u043d\u0435\u0432\u0438\u0434")) {
            this.sellMode = SellMode.POTION;
            return true;
        }
        if (this.hasKeyword(string, "\u0448\u043b\u0435\u043c", "\u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a", "\u043f\u043e\u043d\u043e\u0436", "\u0431\u043e\u0442\u0438\u043d", "\u0431\u0440\u043e\u043d")) {
            this.sellMode = SellMode.ARMOR;
            return true;
        }
        if (this.hasKeyword(string, "\u043a\u0438\u0440\u043a", "\u043a\u0438\u0440\u043a\u0430", "\u043a\u0438\u0440\u043a\u0438", "\u043a\u0438\u0440\u043e\u043a", "\u0448\u0430\u0445\u0442", "\u0431\u0443\u0440")) {
            this.sellMode = SellMode.PICKAXE;
            return true;
        }
        return false;
    }

    private boolean isArmorItemValid(List<String> list) {
        int n = 0;
        for (String string : list) {
            string = string.toLowerCase(Locale.ROOT);
            if (++n > 2) break;
            if ((string.contains("\u0441\u0438\u043b") || string.contains("\u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c")) && !string.contains("iii") && !string.contains("\u0443\u0441\u0438\u043b\u0435\u043d\u043d")) continue;
            return true;
        }
        return false;
    }

    private boolean isPickaxeItemValid(List<String> list) {
        for (String string : list) {
            if (!string.contains("\u0438\u043d\u0432\u0438\u0437") && !string.contains("\u043d\u0435\u0432\u0438\u0434")) continue;
            return true;
        }
        return false;
    }

    private boolean isPotionItemSelected(List<String> list) {
        boolean bl = list.stream().anyMatch(string -> string.contains("\u0441\u0438\u043b\u0430"));
        boolean bl2 = list.stream().anyMatch(string -> string.contains("\u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c"));
        return bl && bl2;
    }

    private boolean isItemCategoryValid(List<String> list) {
        for (String string : list) {
            int n;
            if (!string.contains("\u0441\u0438\u043b\u0430") || (n = this.parsePageCount(string)) == -1 || n >= 6) continue;
            return true;
        }
        return false;
    }

    private int parsePageCount(String string) {
        try {
            Pattern pattern = Pattern.compile("(\\d+):(\\d+)");
            Matcher matcher = pattern.matcher(string);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return -1;
    }

    private void parseAuctionListings(HandledScreen<?> BlockStateProviderType) {
        this.auctionListings.clear();
        this.minimumListingPrice = Double.MAX_VALUE;
        this.averageListingPrice = 0.0;
        boolean bl = this.isItemNameMatch(this.searchQueryText);
        this.bestPriceSlot = -1;
        this.bestExchangeRate = -1.0;
        double d = 0.0;
        int n = 0;
        int n2 = BlockStateProviderType.getScreenHandler().slots.size() - 36;
        for (int i = 0; i < n2; ++i) {
            int n3;
            int n4;
            List<Text> tooltipLines;
            Slot class_17352 = BlockStateProviderType.getScreenHandler().getSlot(i);
            if (class_17352 == null || !class_17352.hasStack()) continue;
            ItemStack class_17992 = class_17352.getStack();
            tooltipLines = class_17992.getTooltip(Item.TooltipContext.create((World)Auction.minecraftClient.world), (PlayerEntity)Auction.minecraftClient.player, (TooltipType)(Auction.minecraftClient.options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC));
            if (this.isAuctionItemValidWithLore(class_17992, tooltipLines)) continue;
            long l = -1L;
            long l2 = -1L;
            long l3 = -1L;
            long l4 = -1L;
            if (bl) {
                for (Text class_25612 : tooltipLines) {
                    String string = MarketInventoryAnalyzer.stripFormattingCodes(class_25612.getString());
                    String string2 = string.toLowerCase(Locale.ROOT);
                    if (string2.contains("\u0431\u0438\u0440\u0436\u0430 \u0431\u0430\u043b\u0430\u043d\u0441")) {
                        l3 = this.parsePriceText(string);
                        continue;
                    }
                    if (string2.contains("\u043c\u043e\u043d\u0435\u0442")) {
                        l4 = this.parsePriceText(string);
                        continue;
                    }
                    if (!string2.contains("\u043a\u0443\u0440\u0441")) continue;
                    l2 = this.parsePriceText(string);
                }
            } else {
                l = MarketInventoryAnalyzer.findPriceInLore(tooltipLines);
            }
            if (l3 != -1L) {
                this.exchangeBalance = l3;
            }
            if (l4 != -1L) {
                this.coinBalance = l4;
            }
            Object object = null;
            long l5 = -1L;
            if (bl) {
                if (l2 > 0L) {
                    object = (double)(-l2);
                    l5 = l2;
                    if (this.exchangeBalance != -1L && this.exchangeBalance < l2) continue;
                    if ((double)l2 > this.bestExchangeRate) {
                        this.bestExchangeRate = l2;
                        this.bestPriceSlot = class_17352.id;
                    }
                }
            } else if (l > 0L) {
                int n5 = Math.max(1, class_17992.getCount());
                n4 = class_17992.getMaxDamage();
                n3 = n4 - class_17992.getDamage();
                double d2 = this.priceModeSetting.isSelected(this.perUnitPriceOption) ? (double)l / (double)n5 : (double)l;
                double d3 = 1.0;
                if (n4 > 0) {
                    d3 = (double)n3 / (double)n4;
                    d3 = Math.max(0.1, d3);
                }
                object = d2 / d3;
                l5 = l;
            }
            if (object == null) continue;
            int n6 = class_17992.getCount();
            n4 = class_17992.getMaxDamage();
            n3 = n4 - class_17992.getDamage();
            this.auctionListings.add(new AuctionEntry(class_17352.id, l5, n6, n4, n3, (Double)object));
            d += ((Double)object).doubleValue();
            ++n;
            if (!((Double)object < this.minimumListingPrice)) continue;
            this.minimumListingPrice = (Double)object;
        }
        if (n > 0) {
            this.averageListingPrice = d / (double)n;
        } else {
            this.finishAuctionSession();
        }
    }

    private void finishAuctionSession() {
        this.auctionListings.clear();
        this.averageListingPrice = 0.0;
        this.minimumListingPrice = Double.MAX_VALUE;
        this.sellMode = SellMode.OTHER;
        this.bestPriceSlot = -1;
        this.bestExchangeRate = -1.0;
        this.exchangeBalance = -1L;
        this.coinBalance = -1L;
    }

    private List<AuctionPrice> readAuctionEntries(ScreenHandler class_17032) {
        ArrayList<AuctionPrice> arrayList = new ArrayList<AuctionPrice>();
        int n = Math.max(0, class_17032.slots.size() - 36);
        for (int i = 0; i < n; ++i) {
            int n2;
            double d;
            ItemStack class_17992;
            Slot class_17352 = (Slot)class_17032.slots.get(i);
            if (class_17352 == null || !class_17352.hasStack() || (class_17992 = class_17352.getStack()).getItem() != this.selectedItemType || (d = this.getItemPrice(class_17992, n2 = Math.max(1, class_17992.getCount()))) <= 0.0) continue;
            arrayList.add(new AuctionPrice(Math.round(d * (double)n2), n2, d));
        }
        return arrayList;
    }

    private AuctionListing summarizeAuction(List<AuctionPrice> list) {
        if (list.isEmpty()) {
            return new AuctionListing(0, 0, 0L, 0);
        }
        List<AuctionPrice> list2 = list.stream().sorted(Comparator.comparingDouble(AuctionPrice::getUnitPrice)).toList();
        double d = list2.getFirst().getUnitPrice();
        double d2 = d * 1.6;
        long l = 0L;
        int n = 0;
        int n2 = 0;
        for (AuctionPrice auctionPrice : list2) {
            if (auctionPrice.getUnitPrice() > d2 && n2 > 0) break;
            l += auctionPrice.getTotalPrice();
            n += auctionPrice.getItemCount();
            ++n2;
        }
        if (n2 == 1 && list2.size() > 1) {
            AuctionPrice auctionPrice = list2.get(1);
            l += auctionPrice.getTotalPrice();
            n += auctionPrice.getItemCount();
            ++n2;
        }
        return new AuctionListing(n2, n, l, list.size() - n2);
    }

    private double getItemPrice(ItemStack class_17992, int n) {
        double d;
        if (this.autoSellModeSetting.isSelected(this.holyWorldSellOption) && (d = this.getAverageItemPrice(class_17992)) > 0.0) {
            return d;
        }
        for (Text class_25612 : this.getItemLore(class_17992)) {
            long l;
            String string = class_25612.getString();
            if (!string.contains("$") || string.contains("%") || (l = this.parsePriceText(string)) <= 0L) continue;
            return (double)l / (double)n;
        }
        return -1.0;
    }

    private double getAverageItemPrice(ItemStack class_17992) {
        for (Text class_25612 : this.getItemLore(class_17992)) {
            long l;
            String string = class_25612.getString();
            String string2 = string.toLowerCase(Locale.ROOT);
            if (!string2.contains("\u0446\u0435\u043d\u0430 \u0437\u0430 1") && !string2.contains("price per 1") || (l = this.parsePrice(string)) <= 0L) continue;
            return l;
        }
        return -1.0;
    }

    private int getAuctionPage(ScreenHandler class_17032) {
        int n = Math.max(0, class_17032.slots.size() - 36);
        for (int i = 0; i < n; ++i) {
            ItemStack class_17992;
            Slot class_17352 = (Slot)class_17032.slots.get(i);
            if (class_17352 == null || !class_17352.hasStack() || !this.isSellableItem(class_17992 = class_17352.getStack())) continue;
            return i;
        }
        return -1;
    }

    private AuctionSlot getAuctionSlot(ScreenHandler class_17032) {
        AuctionSlot auctionSlot;
        if (Auction.minecraftClient.currentScreen != null && (auctionSlot = this.parseAuctionSlot(Auction.minecraftClient.currentScreen.getTitle().getString())) != null) {
            return auctionSlot;
        }
        int n = Math.max(0, class_17032.slots.size() - 36);
        for (int i = 0; i < n; ++i) {
            AuctionSlot auctionSlot2;
            Slot class_17352 = (Slot)class_17032.slots.get(i);
            if (class_17352 == null || !class_17352.hasStack() || (auctionSlot2 = this.createAuctionSlot(class_17352.getStack())) == null) continue;
            return auctionSlot2;
        }
        return null;
    }

    private AuctionSlot createAuctionSlot(ItemStack class_17992) {
        for (Text class_25612 : this.getItemLore(class_17992)) {
            AuctionSlot auctionSlot = this.parseAuctionSlot(class_25612.getString());
            if (auctionSlot == null) continue;
            return auctionSlot;
        }
        return null;
    }

    private AuctionSlot parseAuctionSlot(String string) {
        Matcher matcher = pageCountPattern.matcher(string);
        if (!matcher.find()) {
            return null;
        }
        try {
            return new AuctionSlot(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        }
        catch (NumberFormatException numberFormatException) {
            return null;
        }
    }

    private boolean isSellableItem(ItemStack class_17992) {
        Object object = class_17992.getName().getString().toLowerCase(Locale.ROOT);
        for (Text class_25612 : this.getItemLore(class_17992)) {
            object = (String)object + " " + class_25612.getString().toLowerCase(Locale.ROOT);
        }
        return ((String)object).contains("\u0441\u043b\u0435\u0434") || ((String)object).contains("\u0434\u0430\u043b\u0435\u0435") || ((String)object).contains("\u0432\u043f\u0435\u0440") || ((String)object).contains("next");
    }

    private List<Text> getItemLore(ItemStack class_17992) {
        return class_17992.getTooltip(Item.TooltipContext.create((World)Auction.minecraftClient.world), (PlayerEntity)Auction.minecraftClient.player, (TooltipType)(Auction.minecraftClient.options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC));
    }

    private long parsePrice(String string) {
        int n = string.lastIndexOf(58);
        return this.parsePriceText(n == -1 ? string : string.substring(n + 1));
    }

    private int getConfiguredSearchPageCount() {
        try {
            String string = this.autoSellPagesSetting.getValue();
            if (string == null || string.isBlank()) {
                return 1;
            }
            String string2 = string.replaceAll("[^\\d]", "");
            if (string2.isEmpty()) {
                return 1;
            }
            return Math.max(1, Integer.parseInt(string2));
        }
        catch (NumberFormatException numberFormatException) {
            return 1;
        }
    }

    private ScreenHandler getOpenAuctionContainer() {
        if (!(Auction.minecraftClient.currentScreen instanceof HandledScreen)) {
            return null;
        }
        if (Auction.minecraftClient.player == null) {
            return null;
        }
        ScreenHandler class_17032 = Auction.minecraftClient.player.currentScreenHandler;
        if (class_17032 == Auction.minecraftClient.player.playerScreenHandler) {
            return null;
        }
        return class_17032;
    }

    private void resetPricingState() {
        if (Auction.minecraftClient.player == null) {
            return;
        }
        if (Auction.minecraftClient.player.currentScreenHandler != null && Auction.minecraftClient.player.currentScreenHandler != Auction.minecraftClient.player.playerScreenHandler) {
            Auction.minecraftClient.player.networkHandler.sendPacket((Packet)new CloseHandledScreenC2SPacket(Auction.minecraftClient.player.currentScreenHandler.syncId));
        }
        if (Auction.minecraftClient.currentScreen instanceof HandledScreen) {
            Auction.minecraftClient.player.closeHandledScreen();
        }
    }

    private String formatPrice(long l) {
        return String.format(Locale.US, "%,d", l);
    }

    private long parsePriceText(String string) {
        return MarketInventoryAnalyzer.parsePriceText(string);
    }

    @Generated
    public List<AuctionEntry> getAuctionEntries() {
        return this.auctionListings;
    }

    @Generated
    public double getAverageListingPrice() {
        return this.averageListingPrice;
    }

    @Generated
    public double getMinimumListingPrice() {
        return this.minimumListingPrice;
    }

    @Generated
    public String getSearchQueryText() {
        return this.searchQueryText;
    }

    @Generated
    public int getBestPriceSlot() {
        return this.bestPriceSlot;
    }

    @Generated
    public double getAveragePrice() {
        return this.bestExchangeRate;
    }

    @Generated
    public long getExchangeBalance() {
        return this.exchangeBalance;
    }

    @Generated
    public long getCoinBalance() {
        return this.coinBalance;
    }

    @Generated
    public ColorSetting getLowPriceColor() {
        return this.lowPriceColorSetting;
    }

    @Generated
    public ColorSetting getHighPriceColor() {
        return this.highPriceColorSetting;
    }

    @Generated
    public BooleanSetting getShowYellowSetting() {
        return this.showAboveAverageSetting;
    }

    @Generated
    public BooleanSetting getFastBuySetting() {
        return this.fastBuySetting;
    }

    @Generated
    public IntegerSetting getSearchSetting() {
        return this.searchKeySetting;
    }

    @Generated
    public BooleanSetting getAutoConfirmSetting() {
        return this.autoConfirmSetting;
    }

    @Generated
    public BooleanSetting getAutoSellSetting() {
        return this.autoSellSetting;
    }

    @Generated
    public ModeSetting getAutoSellModeSetting() {
        return this.autoSellModeSetting;
    }

    @Generated
    public ModeSetting.Option getFuntimeOption() {
        return this.funTimeSellOption;
    }

    @Generated
    public ModeSetting.Option getHolyworldOption() {
        return this.holyWorldSellOption;
    }

    @Generated
    public StringSetting getPagesSetting() {
        return this.autoSellPagesSetting;
    }

    @Generated
    public IntegerSetting getBindSetting() {
        return this.autoSellBindSetting;
    }

    @Generated
    public ModeSetting getPriceModeSetting() {
        return this.priceModeSetting;
    }

    @Generated
    public ModeSetting.Option getPerUnitOption() {
        return this.perUnitPriceOption;
    }

    @Generated
    public ModeSetting.Option getTotalOption() {
        return this.totalPriceOption;
    }

    @Generated
    public MultiBooleanSetting getArmorSetting() {
        return this.armorFilter;
    }

    @Generated
    public MultiBooleanSetting.Option getNoSpikeOption() {
        return this.noSpikeArmorOption;
    }

    @Generated
    public MultiBooleanSetting.Option getNoProtFiveOption() {
        return this.noProtectionFiveOption;
    }

    @Generated
    public MultiBooleanSetting.Option getNoDurabilityOption() {
        return this.noDurabilityArmorOption;
    }

    @Generated
    public NumberSetting getMinDurabilitySetting() {
        return this.minimumDurabilitySetting;
    }

    @Generated
    public MultiBooleanSetting.Option getNoRepairOption() {
        return this.noRepairArmorOption;
    }

    @Generated
    public MultiBooleanSetting getPickaxeSetting() {
        return this.pickaxeFilter;
    }

    @Generated
    public MultiBooleanSetting.Option getSilkTouchSetting() {
        return this.silkTouchPickaxeOption;
    }

    @Generated
    public MultiBooleanSetting.Option getNoMegaSetting() {
        return this.noMegaPickaxeOption;
    }

    @Generated
    public MultiBooleanSetting.Option getNoMinerSetting() {
        return this.noMinerPickaxeOption;
    }

    @Generated
    public MultiBooleanSetting.Option getPickaxeNoRepairOption() {
        return this.noRepairPickaxeOption;
    }

    @Generated
    public MultiBooleanSetting.Option getNoMagnetSetting() {
        return this.noMagnetPickaxeOption;
    }

    @Generated
    public MultiBooleanSetting.Option getNoExperienceSetting() {
        return this.noExperiencePickaxeOption;
    }

    @Generated
    public MultiBooleanSetting getPotionsSetting() {
        return this.potionFilter;
    }

    @Generated
    public MultiBooleanSetting.Option getNoLevelThreeOption() {
        return this.noLevelThreePotionOption;
    }

    @Generated
    public MultiBooleanSetting.Option getNoCombinedOption() {
        return this.noCombinedPotionOption;
    }

    @Generated
    public MultiBooleanSetting.Option getNoUnderSixMinutesOption() {
        return this.noUnderSixMinutesPotionOption;
    }

    @Generated
    public boolean isAuctionModeActive() {
        return this.autoSellKeyHeld;
    }

    @Generated
    public boolean isPriceFilterActive() {
        return this.priceOverlayVisible;
    }

    @Generated
    public Timer getCooldownTimer() {
        return this.searchCooldownTimer;
    }

    @Generated
    public Timer getActionTimer() {
        return this.purchaseActionTimer;
    }

    @Generated
    public Timer getTargetTimer() {
        return this.auctionTargetTimer;
    }

    @Generated
    public List<AuctionPrice> getPendingPrices() {
        return this.pendingPriceHistory;
    }

    @Generated
    public PriceMode getPriceState() {
        return this.pricingState;
    }

    @Generated
    public Item getSelectedItem() {
        return this.selectedItemType;
    }

    @Generated
    public ItemStack getSelectedStack() {
        return this.selectedItemStack;
    }

    @Generated
    public String getSearchQuery() {
        return this.selectedItemDisplayName;
    }

    @Generated
    public int getConfiguredSearchPages() {
        return this.configuredSearchPages;
    }

    @Generated
    public int getCurrentSearchPage() {
        return this.currentSearchPage;
    }

    /** NOT an override: the original {@code rockstar/ilIlil/IIIIIIIiI} declares no keybind
     *  getter, so the inherited {@link Module#getSavedKeyBind()} must stay visible. */
    @Generated
    public int getPurchaseSlot() {
        return this.purchaseSlot;
    }

    @Generated
    public ColorPickerScreen getColorPicker() {
        return this.colorPicker;
    }

    @Generated
    public boolean isSellingEnabled() {
        return this.sellingInitialized;
    }

    @Generated
    public SellMode getSellCategory() {
        return this.sellMode;
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
    public EventListener<HudRenderEvent> getHudRenderListener() {
        return this.hudRenderListener;
    }

    @Generated
    public EventListener<ScreenRenderEvent> getScreenRenderListener() {
        return this.screenRenderListener;
    }

    @Generated
    public EventListener<ContainerClickEvent> getContainerClickListener() {
        return this.containerClickListener;
    }

    @Generated
    public EventListener<ContainerReleaseEvent> getContainerReleaseListener() {
        return this.containerReleaseListener;
    }
}
