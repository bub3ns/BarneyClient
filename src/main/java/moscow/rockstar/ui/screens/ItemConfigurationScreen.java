/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.StatusEffect
 *  net.minecraft.StatusEffectInstance
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.PotionItem
 *  net.minecraft.PotionContentsComponent
 *  net.minecraft.Enchantment
 *  net.minecraft.DrawContext
 *  net.minecraft.MatrixStack
 *  net.minecraft.RegistryEntry
 *  net.minecraft.RotationAxis
 *  net.minecraft.RegistryKeys
 *  net.minecraft.ItemEnchantmentsComponent
 *  net.minecraft.ItemEnchantmentsComponent$Builder
 *  net.minecraft.DataComponentTypes
 */
package moscow.rockstar.ui.screens;
import moscow.rockstar.ui.localization.Localization;

import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import moscow.rockstar.api.commands.MiningCommandService;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.dispatch.EventListenerCoordinator;
import moscow.rockstar.items.catalog.ItemCatalog;
import moscow.rockstar.items.config.ItemConfigProcessor;
import moscow.rockstar.items.recipes.RecipeItemResolver;
import moscow.rockstar.math.MathUtils;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.other.market.purchase.AutoBuy;
import moscow.rockstar.modules.other.market.purchase.PurchaseEntry;
import moscow.rockstar.modules.player.inventory.InventoryMove;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.animation.Transition;
import moscow.rockstar.ui.color.ColorPickerHost;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.InteractiveComponent;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.layout.JustifyContent;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.EditableTextComponent;
import moscow.rockstar.ui.text.TextComponent;
import moscow.rockstar.ui.text.ValueFormatter;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.DataComponentTypes;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

public class ItemConfigurationScreen
extends ColorPickerHost {
    private static final float CONTENT_PADDING = 11.0f;
    private static final float PANEL_CORNER_RADIUS = 7.0f;
    private static final float SECTION_GAP = 10.0f;
    private static final float PANEL_SHADOW_RADIUS = 25.0f;
    private static final float MAIN_PANEL_HEIGHT = 236.0f;
    private static final float CONTENT_TOP_PADDING = 32.0f;
    private static final float CONTROL_ICON_SIZE = 11.0f;
    private static final float ITEM_LIST_WIDTH = 193.0f;
    private static final float CATALOG_LIST_WIDTH = 154.0f;
    private static final float SETTING_ROW_HEIGHT = 40.0f;
    private static final float ITEM_NAME_MAX_WIDTH = 30.0f;
    private static final float TIMING_ROW_HEIGHT = 19.0f;
    private static final float COMPACT_CONTROL_HEIGHT = 9.0f;
    private static final float BASE_FONT_SIZE = 8.0f;
    private static final float MAIN_PANEL_WIDTH = 562.0f;
    private static final Motion PANEL_LIFE_MOTION = Motion.resolveMotionMotionFromLongAndEasing(300L, Easing.easeOutCubic);
    private static final Transition PANEL_ENTRY_TRANSITION = (f, uiNode, state) -> {
        state.progress = f;
        state.offsetY = (1.0f - f) * 6.0f;
    };
    private static boolean CONFIGURATION_LOADED;
    private Section selectedSection = Section.PURCHASE;
    private Section renderedSection;
    private ItemConfigProcessor.ConfigProfile selectedConfigProfile = ItemConfigProcessor.ConfigProfile.FUNTIME;
    private ItemConfigProcessor.ItemDefinition selectedItem;
    private String catalogSearchText = "";
    private long maxPurchasePrice = 25000L;
    private boolean usePercentageDiscount;
    private double discountPercentage = 20.0;
    private long resalePrice;
    private int lotQuantity = 1;
    private int resaleThreshold = 10;
    private String serverIdInput = "";
    private final LinkedHashMap<RegistryEntry<Enchantment>, Integer> enchantmentLevels = new LinkedHashMap();
    private final LinkedHashMap<RegistryEntry<StatusEffect>, Integer> statusEffectAmplifiers = new LinkedHashMap();
    private boolean strictEnchantmentMatching;
    private boolean effectSelectorOpen;
    private String effectSearchText = "";
    private Component sectionContent;
    private Component catalogItems;
    private Component configuredPurchases;
    private Component purchaseHistory;
    private Component serverIdList;
    private Component enchantmentLevelList;
    private Component statusEffectList;
    private Component effectSelector;
    private String catalogContentSignature = "";
    private String configuredPurchaseSignature = "";
    private String purchaseHistorySignature = "";
    private String serverIdListSignature = "";
    private String enchantmentListSignature = "";
    private String statusEffectListSignature = "";
    private String effectSelectorSignature = "";

    @Override
    @Compile(obfuscation=4)
    protected void initializeScreen() {
        if (!CONFIGURATION_LOADED) {
            ItemConfigProcessor.loadConfiguration();
            CONFIGURATION_LOADED = true;
        }
        super.initializeScreen();
        this.clearRoots();
        this.renderedSection = null;
        Component component = new Component().vertical().gap(9.0f).alignment(Alignment.CENTER).center();
        component.add(this.createSectionTabs());
        this.sectionContent = new Component().horizontal().gap(10.0f).height(236.0f).width(562.0f).overflowMode(JustifyContent.CENTER);
        component.add(this.sectionContent);
        this.add(component);
        this.overlays.clear();
        this.openWindow(this.createEffectSelector());
    }

    @Override
    public void render(RockstarDrawContext drawContext) {
        this.refreshDynamicContent();
        super.render(drawContext);
    }

    private Component createSectionTabs() {
        Component component2 = new Component().horizontal().gap(2.0f).padding(Insets.uniform(2.0f)).cornerRadius(7.0f).renderHook((drawContext, component) -> {
            drawContext.drawBlurredRect(component.x(), component.y(), component.w(), component.h(), 5.0f, 3.0f, WidgetState.uniform(7.0f), ColorPalette.WHITE);
            drawContext.drawSquircle(component.x(), component.y(), component.w(), component.h(), 3.0f, WidgetState.uniform(7.0f), ColorPalette.PANEL_COLOR.mulAlpha(0.55f));
        });
        component2.add(this.createSectionTab(Localization.translate("itemconfig.tab.purchase"), Section.PURCHASE));
        component2.add(this.createSectionTab(Localization.translate("itemconfig.tab.activity"), Section.ACTIVITY));
        component2.add(this.createSectionTab(Localization.translate("settings"), Section.SETTINGS));
        return component2;
    }

    private TextComponent createSectionTab(String string, Section section) {
        TextComponent textComponent2 = new TextComponent().text(Font.SEMIBOLD.metrics(8.0f), string, textComponent -> this.selectedSection == section ? ColorPalette.PRIMARY_TEXT_COLOR : ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.5f)).padding(Insets.symmetric(4.0f, 10.0f)).radius(5.0f).cursor(Cursor.HAND);
        textComponent2.background(textComponent -> this.selectedSection == section ? ColorPalette.MUTED_PANEL_COLOR : ColorPalette.WHITE.mulAlpha(0.05f * textComponent.hover()));
        textComponent2.onClick(() -> {
            this.selectedSection = section;
        });
        return textComponent2;
    }

    private Component createPanel(float f, String string) {
        return new Component().vertical().width(f).fillHeight().gap(4.0f).padding(Insets.of(32.0f, 11.0f, 11.0f, 11.0f)).transition(PANEL_ENTRY_TRANSITION).lifeMotion(PANEL_LIFE_MOTION).snapPosition().renderHook((drawContext, component) -> this.drawPanelBackground(drawContext, component, string));
    }

    private void drawPanelBackground(RockstarDrawContext drawContext, Component component, String string) {
        float f = component.x();
        float f2 = component.y();
        float f3 = component.w();
        float f4 = component.h();
        drawContext.drawShadow(f, f2, f3, f4, 25.0f, WidgetState.uniform(11.0f), ColorPalette.BLACK.mulAlpha(0.15f));
        drawContext.drawBlurredRect(f, f2, f3, f4, 5.0f, 3.0f, WidgetState.uniform(11.0f), ColorPalette.WHITE);
        drawContext.drawClientRect(f, f2, f3, f4, 1.0f, 0.0f, 3.0f, 11.0f, true);
        drawContext.drawText(Font.SEMIBOLD.metrics(8.0f), string, f + 11.0f, f2 + 9.5f, ColorPalette.PRIMARY_TEXT_COLOR);
        drawContext.drawRect(f + 1.0f, f2 + 25.0f - 1.0f, f3 - 2.0f, 1.0f, ColorPalette.BORDER_COLOR);
    }

    private Component createScrollableList(float f) {
        return new Component().vertical().gap(4.0f).fillWidth().height(f).scrollable().configureLayoutState(scrollBar2 -> scrollBar2.offset(2.0f).padding(1.0f, 4.0f).thickness(2.0f).thumbColor(scrollBar -> ColorRGBA.BLACK.mix(ColorRGBA.WHITE, 0.3f).withAlpha(255.0f * (0.3f + 0.3f * scrollBar.hoverProgress() + 0.3f * scrollBar.dragProgress()))));
    }

    private List<UiNode> buildSectionContent(Section section) {
        this.statusEffectList = null;
        this.enchantmentLevelList = null;
        this.serverIdList = null;
        this.purchaseHistory = null;
        this.configuredPurchases = null;
        this.catalogItems = null;
        ArrayList<UiNode> arrayList = new ArrayList<UiNode>();
        switch (section.ordinal()) {
            case 0: {
                arrayList.add(this.createPurchasePanel());
                arrayList.add(this.createCatalogPanel());
                arrayList.add(this.createSettingsPanel());
                break;
            }
            case 1: {
                arrayList.add(this.createActivityPanel());
                break;
            }
            case 2: {
                arrayList.add(this.createAutomationPanel());
            }
        }
        return arrayList;
    }

    private Component createPurchasePanel() {
        Component component = this.createPanel(160.0f, Localization.translate("itemconfig.tab.purchase"));
        this.configuredPurchases = this.createScrollableList(193.0f);
        component.add(this.configuredPurchases);
        return component;
    }

    private Component createConfiguredPurchaseRow(ItemCatalog.CatalogEntry catalogEntry) {
        Component component2 = new Component().horizontal().alignment(Alignment.CENTER).gap(6.0f).fillWidth().height(26.0f).padding(Insets.horizontal(1.0f)).cornerRadius(5.0f).background(component -> ColorPalette.WHITE.mulAlpha(0.04f * component.hover()));
        component2.add(new TextComponent().size(17.0f, 17.0f).paint((drawContext, textComponent) -> ItemConfigurationScreen.drawItemStack(drawContext, catalogEntry.getStack(), textComponent.x() + textComponent.w() / 2.0f - 6.8f, textComponent.y() + textComponent.h() / 2.0f - 6.8f, 0.85f)));
        Component component3 = new Component().vertical().gap(1.0f).fillWidth();
        component3.add(new TextComponent().text(Font.MEDIUM.metrics(8.0f), ItemConfigurationScreen.getCatalogEntryDisplayName(catalogEntry), ColorPalette.PRIMARY_TEXT_COLOR).fillWidth().height(9.0f).fade());
        component3.add(new TextComponent().text(Font.REGULAR.metrics(7.0f), () -> ItemConfigurationScreen.formatCatalogEntryDetails(catalogEntry), textComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.5f)).fillWidth().height(7.0f).fade());
        component2.add(component3);
        component2.add(new TextComponent().size(14.0f, 14.0f).cursor(Cursor.HAND).onClick(() -> ItemCatalog.removeEntry(catalogEntry.getId())).paint((drawContext, textComponent) -> ItemConfigurationScreen.drawRemoveIcon(drawContext, textComponent, 3.5f, 1.4f, ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.35f + 0.4f * textComponent.hover()))));
        return component2;
    }

    private Component createCatalogPanel() {
        Component component = this.createPanel(216.0f, Localization.translate("itemconfig.catalog"));
        EditableTextComponent searchField = new EditableTextComponent(Font.REGULAR.metrics(8.0f), this.catalogSearchText, string -> {
            this.catalogSearchText = string;
        });
        searchField.placeholder(Localization.translate("itemconfig.search_placeholder"));
        searchField.fillWidth();
        searchField.height(15.0f);
        searchField.padding(4.0f);
        searchField.background(ColorPalette.WHITE.mulAlpha(0.07f));
        searchField.radius(3.0f);
        searchField.textColor(ColorPalette.PRIMARY_TEXT_COLOR);
        component.add(searchField);
        component.add(this.createConfigProfileTabs());
        this.catalogItems = this.createScrollableList(154.0f);
        component.add(this.catalogItems);
        return component;
    }

    private Component createConfigProfileTabs() {
        Component component = new Component().horizontal().gap(3.0f).height(16.0f);
        for (ItemConfigProcessor.ConfigProfile configProfile : ItemConfigProcessor.ConfigProfile.values()) {
            TextComponent textComponent2 = new TextComponent().text(Font.MEDIUM.metrics(7.0f), configProfile.getDisplayName(), textComponent -> this.selectedConfigProfile == configProfile ? ColorPalette.HIGHLIGHT_COLOR : ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.45f)).padding(Insets.symmetric(3.0f, 7.0f)).radius(4.0f).cursor(Cursor.HAND);
            textComponent2.background(textComponent -> this.selectedConfigProfile == configProfile ? ColorPalette.ACCENT_COLOR : ColorPalette.WHITE.mulAlpha(0.05f * textComponent.hover()));
            textComponent2.onClick(() -> {
                this.selectedConfigProfile = configProfile;
            });
            component.add(textComponent2);
        }
        return component;
    }

    private List<UiNode> buildCatalogGrid() {
        ArrayList<UiNode> arrayList = new ArrayList<UiNode>();
        String string = this.catalogSearchText.trim().toLowerCase();
        for (ItemConfigProcessor.CategoryDefinition categoryDefinition : ItemConfigProcessor.getCategoryDefinitions(this.selectedConfigProfile)) {
            List<ItemConfigProcessor.ItemDefinition> list = categoryDefinition.getItems().stream().filter(itemDefinition -> string.isEmpty() || ItemConfigurationScreen.getItemDisplayName(itemDefinition).toLowerCase().contains(string)).toList();
            if (list.isEmpty()) continue;
            arrayList.add(new TextComponent().text(Font.SEMIBOLD.metrics(7.0f), categoryDefinition.getDisplayName().toUpperCase(), textComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.45f)).fillWidth().height(10.0f).textInset(1.0f));
            Component component = new Component().columns(5).gap(3.0f).fillWidth();
            for (ItemConfigProcessor.ItemDefinition itemDefinition2 : list) {
                component.add(this.createCatalogItemTile(itemDefinition2));
            }
            arrayList.add(component);
        }
        if (arrayList.isEmpty()) {
            arrayList.add(this.createEmptyStateText(Localization.translate(string.isEmpty() ? "itemconfig.no_items" : "itemconfig.nothing_found")));
        }
        return arrayList;
    }

    private Component createCatalogItemTile(ItemConfigProcessor.ItemDefinition itemDefinition) {
        Component component2 = new Component().vertical().fillWidth().height(40.0f).cornerRadius(7.0f).cursor(Cursor.HAND).padding(Insets.of(6.0f, 2.0f, 4.0f, 2.0f)).lifeMotion(PANEL_LIFE_MOTION);
        component2.background(component -> this.isSelectedItem(itemDefinition) ? ColorPalette.ACCENT_COLOR.mulAlpha(0.22f) : ColorPalette.WHITE.mulAlpha(0.04f + 0.05f * component.hover()));
        component2.renderHook((drawContext, component) -> {
            if (this.isItemConfigured(itemDefinition)) {
                drawContext.drawRoundedRect(component.x() + component.w() - 7.0f, component.y() + 5.0f, 3.0f, 3.0f, WidgetState.uniform(1.5f), ColorPalette.ACCENT_COLOR);
            }
        });
        component2.add(new TextComponent().fillWidth().height(18.0f).paint((drawContext, textComponent) -> ItemConfigurationScreen.drawItemStack(drawContext, itemDefinition.getStack(), textComponent.x() + textComponent.w() / 2.0f - 8.0f, textComponent.y() + textComponent.h() / 2.0f - 8.0f, 1.0f)));
        TextComponent textComponent2 = new TextComponent().text(Font.REGULAR.metrics(6.0f), ItemConfigurationScreen.getItemDisplayName(itemDefinition), textComponent -> this.isSelectedItem(itemDefinition) ? ColorPalette.PRIMARY_TEXT_COLOR : ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.8f)).textAlign(Alignment.CENTER).fillWidth().fillHeight().textInset(1.0f);
        if (Font.REGULAR.metrics(6.0f).measureText(ItemConfigurationScreen.getItemDisplayName(itemDefinition)) > 30.0f) {
            textComponent2.fade();
        }
        component2.add(textComponent2);
        component2.onClick(() -> {
            this.selectedItem = itemDefinition;
            this.maxPurchasePrice = 25000L;
            this.resalePrice = 0L;
            this.lotQuantity = 1;
            this.resaleThreshold = 10;
            this.enchantmentLevels.clear();
            this.statusEffectAmplifiers.clear();
            for (StatusEffectInstance class_12932 : RecipeItemResolver.getEffects(itemDefinition.getStack())) {
                this.statusEffectAmplifiers.put((RegistryEntry<StatusEffect>)class_12932.getEffectType(), class_12932.getAmplifier() + 1);
            }
            this.strictEnchantmentMatching = false;
            this.effectSelectorOpen = false;
        });
        return component2;
    }

    private Component createSettingsPanel() {
        Component component = this.createPanel(166.0f, Localization.translate("itemconfig.setup"));
        Component component2 = this.createScrollableList(193.0f);
        component.add(component2);
        TextComponent textComponent2 = new TextComponent().text(Font.REGULAR.metrics(8.0f), Localization.translate("itemconfig.pick_item"), textComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.4f)).textAlign(Alignment.CENTER).fillWidth().height(60.0f);
        textComponent2.visibleWhen(() -> this.selectedItem == null);
        component2.add(textComponent2);
        Component component3 = new Component().vertical().gap(6.0f).fillWidth();
        component3.visibleWhen(() -> this.selectedItem != null);
        Component component4 = new Component().horizontal().alignment(Alignment.CENTER).gap(7.0f).fillWidth().height(16.0f);
        component4.add(new TextComponent().size(14.0f, 16.0f).paint((drawContext, textComponent) -> {
            if (this.selectedItem != null) {
                ItemConfigurationScreen.drawItemStack(drawContext, this.selectedItem.getStack(), textComponent.x(), textComponent.y() + textComponent.h() / 2.0f - 6.8f, 0.85f);
            }
        }));
        component4.add(new TextComponent().text(Font.MEDIUM.metrics(8.0f), () -> this.selectedItem == null ? "" : ItemConfigurationScreen.getItemDisplayName(this.selectedItem), textComponent -> ColorPalette.PRIMARY_TEXT_COLOR).fillWidth().height(10.0f).fade());
        component3.add(component4);
        component3.add(this.createDivider());
        component3.add(this.createLabeledRow(Localization.translate("itemconfig.below_market"), new InteractiveComponent(() -> this.usePercentageDiscount).size(15.0f, 9.0f).onClick(() -> {
            this.usePercentageDiscount = !this.usePercentageDiscount;
        })));
        Component component5 = this.createLabeledRow(Localization.translate("itemconfig.max_price"), new ValueFormatter(Font.MEDIUM.metrics(8.0f), () -> this.maxPurchasePrice, f -> {
            this.maxPurchasePrice = (long)f;
        }, 0.0f, 1.0E9f).setSuffix(() -> "$").setSuffixColor(ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.5f)).setTextColor(ColorPalette.PRIMARY_TEXT_COLOR));
        component5.visibleWhen(() -> !this.usePercentageDiscount);
        component3.add(component5);
        Component component6 = this.createLabeledRow(Localization.translate("itemconfig.percent_below"), new ValueFormatter(Font.MEDIUM.metrics(8.0f), () -> (float)this.discountPercentage, f -> {
            this.discountPercentage = f;
        }, 0.0f, 95.0f).setSuffix(() -> "%").setSuffixColor(ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.5f)).setTextColor(ColorPalette.PRIMARY_TEXT_COLOR));
        component6.visibleWhen(() -> this.usePercentageDiscount);
        component3.add(component6);
        this.statusEffectList = new Component().vertical().gap(3.0f).fillWidth();
        this.statusEffectList.visibleWhen(this::selectedItemIsPotion);
        component3.add(this.statusEffectList);
        TextComponent textComponent3 = new TextComponent().text(Font.MEDIUM.metrics(8.0f), Localization.translate("itemconfig.add_effect"), textComponent -> ColorPalette.ACCENT_COLOR).textAlign(Alignment.CENTER).fillWidth().height(15.0f).radius(5.0f).cursor(Cursor.HAND);
        textComponent3.background(textComponent -> ColorPalette.ACCENT_COLOR.mulAlpha(0.12f + 0.1f * textComponent.hover()));
        textComponent3.onClick(() -> {
            this.effectSearchText = "";
            this.effectSelectorOpen = true;
        });
        textComponent3.visibleWhen(this::selectedItemIsPotion);
        component3.add(textComponent3);
        Component component7 = this.createLabeledRow(Localization.translate("itemconfig.strict_enchants"), new InteractiveComponent(() -> this.strictEnchantmentMatching).size(15.0f, 9.0f).onClick(() -> {
            this.strictEnchantmentMatching = !this.strictEnchantmentMatching;
        }));
        component7.visibleWhen(this::selectedItemSupportsEnchantments);
        component3.add(component7);
        this.enchantmentLevelList = new Component().vertical().gap(3.0f).fillWidth();
        this.enchantmentLevelList.visibleWhen(this::selectedItemSupportsEnchantments);
        component3.add(this.enchantmentLevelList);
        TextComponent textComponent4 = new TextComponent().text(Font.MEDIUM.metrics(8.0f), Localization.translate("itemconfig.add_enchant"), textComponent -> ColorPalette.ACCENT_COLOR).textAlign(Alignment.CENTER).fillWidth().height(15.0f).radius(5.0f).cursor(Cursor.HAND);
        textComponent4.background(textComponent -> ColorPalette.ACCENT_COLOR.mulAlpha(0.12f + 0.1f * textComponent.hover()));
        textComponent4.onClick(() -> {
            this.effectSearchText = "";
            this.effectSelectorOpen = true;
        });
        textComponent4.visibleWhen(this::selectedItemSupportsEnchantments);
        component3.add(textComponent4);
        component3.add(this.createDivider());
        component3.add(new TextComponent().text(Font.SEMIBOLD.metrics(7.0f), Localization.translate("itemconfig.section_sale"), textComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.45f)).fillWidth().height(9.0f).textInset(1.0f));
        component3.add(this.createLabeledRow(Localization.translate("itemconfig.sale_price"), new ValueFormatter(Font.MEDIUM.metrics(8.0f), () -> this.resalePrice, f -> {
            this.resalePrice = (long)f;
        }, 0.0f, 1.0E9f).setSuffix(() -> this.resalePrice > 0L ? "$" : "").setSuffixColor(ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.5f)).setTextColor(ColorPalette.PRIMARY_TEXT_COLOR).setDisplayValue(() -> this.resalePrice > 0L ? moscow.rockstar.util.NumberFormatting.formatDecimal(this.resalePrice) : Localization.translate("itemconfig.market"))));
        component3.add(this.createLabeledRow(Localization.translate("itemconfig.lot_amount"), new ValueFormatter(Font.MEDIUM.metrics(8.0f), () -> this.lotQuantity, f -> {
            this.lotQuantity = Math.max(1, (int)f);
        }, 1.0f, 64.0f).setSuffix(() -> " " + Localization.translate("itemconfig.pcs")).setSuffixColor(ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.5f)).setTextColor(ColorPalette.PRIMARY_TEXT_COLOR)));
        component3.add(this.createLabeledRow(Localization.translate("itemconfig.resale_threshold"), new ValueFormatter(Font.MEDIUM.metrics(8.0f), () -> this.resaleThreshold, f -> {
            this.resaleThreshold = Math.max(1, (int)f);
        }, 1.0f, 999.0f).setSuffix(() -> " " + Localization.translate("itemconfig.pcs")).setSuffixColor(ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.5f)).setTextColor(ColorPalette.PRIMARY_TEXT_COLOR)));
        component3.add(this.createDivider());
        TextComponent textComponent5 = new TextComponent().text(Font.SEMIBOLD.metrics(8.0f), Localization.translate("itemconfig.add_to_purchase"), ColorPalette.HIGHLIGHT_COLOR).textAlign(Alignment.CENTER).fillWidth().height(20.0f).radius(6.0f).cursor(Cursor.HAND);
        textComponent5.background(textComponent -> ColorPalette.ACCENT_COLOR.mulAlpha(textComponent.hovered() ? 1.0f : 0.85f));
        textComponent5.onClick(this::saveItemConfiguration);
        component3.add(textComponent5);
        component2.add(component3);
        return component;
    }

    private Component createLabeledRow(String string, UiNode uiNode) {
        return new Component().horizontal().alignment(Alignment.CENTER).overflowMode(JustifyContent.SPACE_BETWEEN).fillWidth().height(14.0f).add(new TextComponent().text(Font.REGULAR.metrics(8.0f), string, textComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.6f))).add(uiNode);
    }

    private void saveItemConfiguration() {
        if (this.selectedItem == null) {
            return;
        }
        ItemStack class_17992 = this.selectedItem.getStack().copy();
        if (class_17992.getItem() instanceof PotionItem && !this.statusEffectAmplifiers.isEmpty()) {
            List<StatusEffectInstance> effects = new ArrayList<>();
            this.statusEffectAmplifiers.forEach((effect, amplifier) -> ItemConfigurationScreen.addStatusEffectInstance(effects, effect, amplifier));
            class_17992.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Optional.empty(), Optional.empty(), effects, Optional.empty()));
        }
        if (!this.enchantmentLevels.isEmpty()) {
            ItemEnchantmentsComponent.Builder enchantmentBuilder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
            this.enchantmentLevels.forEach(enchantmentBuilder::set);
            class_17992.set(class_17992.isOf(Items.ENCHANTED_BOOK) ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS, enchantmentBuilder.build());
        }
        ItemCatalog.registerConfiguredEntry(class_17992, this.maxPurchasePrice, this.selectedItem.getCustomName(), this.selectedItem.getItemId(), this.usePercentageDiscount ? ItemCatalog.PriceMode.PERCENTAGE : ItemCatalog.PriceMode.MAX_PRICE, this.discountPercentage, this.strictEnchantmentMatching, this.resalePrice, this.lotQuantity, this.resaleThreshold);
        this.selectedItem = null;
    }

    private boolean selectedItemIsPotion() {
        return this.selectedItem != null && this.selectedItem.getStack().getItem() instanceof PotionItem;
    }

    private boolean selectedItemSupportsEnchantments() {
        if (this.selectedItem == null) {
            return false;
        }
        ItemStack class_17992 = this.selectedItem.getStack();
        return class_17992.isDamageable() || class_17992.isOf(Items.ENCHANTED_BOOK) || class_17992.isOf(Items.BOOK);
    }

    private static String getEnchantmentDisplayName(RegistryEntry<Enchantment> class_68802) {
        return class_68802.value().description().getString();
    }

    private List<RegistryEntry<Enchantment>> getAvailableEnchantments() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return List.of();
        }
        ArrayList<RegistryEntry<Enchantment>> arrayList = new ArrayList<RegistryEntry<Enchantment>>();
        client.world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).streamEntries().forEach(arrayList::add);
        return arrayList;
    }

    private Component createEffectSelector() {
        Component component2 = new Component().vertical().width(190.0f).height(190.0f).cornerRadius(9.0f).gap(7.0f).padding(Insets.uniform(9.0f)).at((float)(this.width - 190) / 2.0f, (float)(this.height - 190) / 2.0f).visibleWhen(() -> this.effectSelectorOpen).renderHook((drawContext, component) -> {
            drawContext.drawShadow(component.x(), component.y(), component.w(), component.h(), 25.0f, WidgetState.uniform(9.0f), ColorPalette.BLACK.mulAlpha(0.2f));
            drawContext.drawBlurredRect(component.x(), component.y(), component.w(), component.h(), 5.0f, 3.0f, WidgetState.uniform(9.0f), ColorPalette.WHITE);
            drawContext.drawClientRect(component.x(), component.y(), component.w(), component.h(), 1.0f, 0.0f, 3.0f, 9.0f, true);
        });
        Component component3 = new Component().horizontal().alignment(Alignment.CENTER).overflowMode(JustifyContent.SPACE_BETWEEN).fillWidth().height(12.0f);
        component3.add(new TextComponent().text(Font.SEMIBOLD.metrics(8.0f), () -> Localization.translate(this.selectedItemIsPotion() ? "effect" : "enchantment"), textComponent -> ColorPalette.PRIMARY_TEXT_COLOR));
        component3.add(new TextComponent().size(11.0f, 11.0f).cursor(Cursor.HAND).onClick(() -> {
            this.effectSelectorOpen = false;
        }).paint((drawContext, textComponent) -> ItemConfigurationScreen.drawRemoveIcon(drawContext, textComponent, 3.0f, 1.3f, ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.45f + 0.4f * textComponent.hover()))));
        component2.add(component3);
        EditableTextComponent effectSearchField = new EditableTextComponent(Font.REGULAR.metrics(8.0f), "", string -> {
            this.effectSearchText = string;
        });
        effectSearchField.placeholder(Localization.translate("itemconfig.search_placeholder"));
        effectSearchField.fillWidth();
        effectSearchField.height(15.0f);
        effectSearchField.padding(4.0f);
        effectSearchField.background(ColorPalette.WHITE.mulAlpha(0.07f));
        effectSearchField.radius(5.0f);
        effectSearchField.textColor(ColorPalette.PRIMARY_TEXT_COLOR);
        component2.add(effectSearchField);
        this.effectSelector = this.createScrollableList(131.0f);
        component2.add(this.effectSelector);
        return component2;
    }

    private TextComponent createEnchantmentOption(RegistryEntry<Enchantment> class_68802) {
        TextComponent textComponent2 = new TextComponent().text(Font.REGULAR.metrics(7.0f), ItemConfigurationScreen.getEnchantmentDisplayName(class_68802), textComponent -> this.enchantmentLevels.containsKey(class_68802) ? ColorPalette.ACCENT_COLOR : ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.85f)).fillWidth().height(14.0f).radius(4.0f).textInset(4.0f).cursor(Cursor.HAND);
        textComponent2.background(textComponent -> ColorPalette.WHITE.mulAlpha(0.05f * textComponent.hover()));
        textComponent2.onClick(() -> {
            this.enchantmentLevels.putIfAbsent(class_68802, 1);
            this.effectSelectorOpen = false;
        });
        return textComponent2;
    }

    private Component createEnchantmentLevelRow(RegistryEntry<Enchantment> class_68802) {
        int n = Math.max(1, class_68802.value().getMaxLevel());
        Component component = new Component().horizontal().alignment(Alignment.CENTER).overflowMode(JustifyContent.SPACE_BETWEEN).fillWidth().height(13.0f);
        component.add(new TextComponent().text(Font.REGULAR.metrics(7.0f), ItemConfigurationScreen.getEnchantmentDisplayName(class_68802), textComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.85f)).fillWidth().height(9.0f).fade());
        Component component2 = new Component().horizontal().alignment(Alignment.CENTER).gap(5.0f);
        component2.add(new ValueFormatter(Font.MEDIUM.metrics(8.0f), () -> this.enchantmentLevels.getOrDefault(class_68802, 1).intValue(), f -> this.enchantmentLevels.put(class_68802, Math.max(1, Math.min(n, (int)f))), 1.0f, n).setTextColor(ColorPalette.PRIMARY_TEXT_COLOR));
        component2.add(new TextComponent().size(9.0f, 11.0f).cursor(Cursor.HAND).onClick(() -> this.enchantmentLevels.remove(class_68802)).paint((drawContext, textComponent) -> ItemConfigurationScreen.drawRemoveIcon(drawContext, textComponent, 2.6f, 1.2f, ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.4f + 0.4f * textComponent.hover()))));
        component.add(component2);
        return component;
    }

    private List<RegistryEntry<StatusEffect>> getAvailableStatusEffects() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return List.of();
        }
        ArrayList<RegistryEntry<StatusEffect>> arrayList = new ArrayList<RegistryEntry<StatusEffect>>();
        client.world.getRegistryManager().getOrThrow(RegistryKeys.STATUS_EFFECT).streamEntries().forEach(arrayList::add);
        return arrayList;
    }

    private static String getStatusEffectDisplayName(RegistryEntry<StatusEffect> class_68802) {
        return class_68802.value().getName().getString();
    }

    private TextComponent createStatusEffectOption(RegistryEntry<StatusEffect> class_68802) {
        TextComponent textComponent2 = new TextComponent().text(Font.REGULAR.metrics(7.0f), ItemConfigurationScreen.getStatusEffectDisplayName(class_68802), textComponent -> this.statusEffectAmplifiers.containsKey(class_68802) ? ColorPalette.ACCENT_COLOR : ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.85f)).fillWidth().height(14.0f).radius(4.0f).textInset(4.0f).cursor(Cursor.HAND);
        textComponent2.background(textComponent -> ColorPalette.WHITE.mulAlpha(0.05f * textComponent.hover()));
        textComponent2.onClick(() -> {
            this.statusEffectAmplifiers.putIfAbsent(class_68802, 1);
            this.effectSelectorOpen = false;
        });
        return textComponent2;
    }

    private Component createStatusEffectLevelRow(RegistryEntry<StatusEffect> class_68802) {
        Component component = new Component().horizontal().alignment(Alignment.CENTER).overflowMode(JustifyContent.SPACE_BETWEEN).fillWidth().height(13.0f);
        component.add(new TextComponent().text(Font.REGULAR.metrics(7.0f), ItemConfigurationScreen.getStatusEffectDisplayName(class_68802), textComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.85f)).fillWidth().height(9.0f).fade());
        Component component2 = new Component().horizontal().alignment(Alignment.CENTER).gap(5.0f);
        component2.add(new ValueFormatter(Font.MEDIUM.metrics(8.0f), () -> this.statusEffectAmplifiers.getOrDefault(class_68802, 1).intValue(), f -> this.statusEffectAmplifiers.put(class_68802, Math.max(1, Math.min(255, (int)f))), 1.0f, 255.0f).setTextColor(ColorPalette.PRIMARY_TEXT_COLOR));
        component2.add(new TextComponent().size(9.0f, 11.0f).cursor(Cursor.HAND).onClick(() -> this.statusEffectAmplifiers.remove(class_68802)).paint((drawContext, textComponent) -> ItemConfigurationScreen.drawRemoveIcon(drawContext, textComponent, 2.6f, 1.2f, ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.4f + 0.4f * textComponent.hover()))));
        component.add(component2);
        return component;
    }

    private Component createActivityPanel() {
        Component component = this.createPanel(300.0f, Localization.translate("itemconfig.tab.activity"));
        this.purchaseHistory = this.createScrollableList(193.0f);
        component.add(this.purchaseHistory);
        return component;
    }

    private Component createPurchaseHistoryRow(PurchaseEntry purchaseEntry) {
        Component component = new Component().horizontal().alignment(Alignment.CENTER).gap(7.0f).fillWidth().height(24.0f).padding(Insets.horizontal(1.0f));
        component.add(new TextComponent().size(17.0f, 17.0f).paint((drawContext, textComponent) -> ItemConfigurationScreen.drawItemStack(drawContext, purchaseEntry.getItemStack(), textComponent.x() + textComponent.w() / 2.0f - 6.8f, textComponent.y() + textComponent.h() / 2.0f - 6.8f, 0.85f)));
        component.add(new TextComponent().text(Font.MEDIUM.metrics(8.0f), purchaseEntry.getItemName() + (String)(purchaseEntry.getPurchaseAmount() > 1 ? " x" + purchaseEntry.getPurchaseAmount() : ""), ColorPalette.PRIMARY_TEXT_COLOR).fillWidth().height(9.0f).fade());
        component.add(new TextComponent().text(Font.SEMIBOLD.metrics(8.0f), "-" + MathUtils.formatCurrencyAmount(purchaseEntry.getTotalPrice()), textComponent -> new ColorRGBA(255.0f, 105.0f, 105.0f)).textAlign(Alignment.END).width(58.0f).height(9.0f));
        return component;
    }

    private Component createAutomationPanel() {
        Component panel = this.createPanel(264.0f, Localization.translate("settings"));
        panel.add(this.createEmptyStateText(Localization.translate("itemconfig.automation_disabled")));
        return panel;
    }

    private static String getAutomationStatus() {
        return Localization.translate("itemconfig.automation_inactive");
    }

    

    private static void drawItemStack(RockstarDrawContext drawContext, ItemStack class_17992, float f, float f2, float f3) {
        float f4 = RenderSystem.getShaderColor()[3];
        if (f4 >= 0.999f) {
            drawContext.drawItem(class_17992, f, f2, f3);
            return;
        }
        drawContext.drawItem(class_17992, f, f2, f3);
    }

    private static void drawRemoveIcon(RockstarDrawContext drawContext, TextComponent textComponent, float f, float f2, ColorRGBA colorRGBA) {
        MatrixStack class_45872 = drawContext.getMatrices();
        class_45872.push();
        class_45872.translate(textComponent.x() + textComponent.w() / 2.0f, textComponent.y() + textComponent.h() / 2.0f, 0.0f);
        class_45872.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45.0f));
        drawContext.drawRect(-f, -f2 / 2.0f, f * 2.0f, f2, colorRGBA);
        drawContext.drawRect(-f2 / 2.0f, -f, f2, f * 2.0f, colorRGBA);
        class_45872.pop();
    }

    /*
     * WARNING - void declaration
     */
    private void refreshDynamicContent() {
        if (this.selectedSection != this.renderedSection) {
            this.renderedSection = this.selectedSection;
            this.sectionContent.updateChildren(this.buildSectionContent(this.selectedSection));
            this.statusEffectListSignature = "";
            this.enchantmentListSignature = "";
            this.purchaseHistorySignature = "";
            this.configuredPurchaseSignature = "";
            this.catalogContentSignature = "";
        }
        if (this.catalogItems != null) {
            String catalogSignature = this.selectedConfigProfile.name() + "|" + this.catalogSearchText;
            if (!catalogSignature.equals(this.catalogContentSignature)) {
                this.catalogContentSignature = catalogSignature;
                this.catalogItems.updateChildren(this.buildCatalogGrid());
            }
        }
        if (this.configuredPurchases != null) {
            List<ItemCatalog.CatalogEntry> catalogEntries = ItemCatalog.getEntries();
            String configuredSignature = catalogEntries.stream().map(ItemCatalog.CatalogEntry::getId).reduce("", (left, right) -> left + "," + right);
            if (!configuredSignature.equals(this.configuredPurchaseSignature)) {
                this.configuredPurchaseSignature = configuredSignature;
                List<UiNode> rows = new ArrayList<>();
                if (catalogEntries.isEmpty()) {
                    rows.add(this.createEmptyStateText(Localization.translate("itemconfig.empty_pick")));
                } else {
                    for (ItemCatalog.CatalogEntry catalogEntry : catalogEntries) {
                        rows.add(this.createConfiguredPurchaseRow(catalogEntry));
                    }
                }
                this.configuredPurchases.updateChildren(rows);
            }
        }
        if (this.purchaseHistory != null) {
            List<PurchaseEntry> history = ItemConfigurationScreen.getPurchaseHistory();
            String historySignature = history.size() + "|" + (history.isEmpty() ? "" : history.get(history.size() - 1).getItemName());
            if (!historySignature.equals(this.purchaseHistorySignature)) {
                this.purchaseHistorySignature = historySignature;
                List<UiNode> rows = new ArrayList<>();
                if (history.isEmpty()) {
                    rows.add(this.createEmptyStateText(Localization.translate("itemconfig.no_purchases")));
                } else {
                    for (int index = history.size() - 1; index >= 0; index--) {
                        rows.add(this.createPurchaseHistoryRow(history.get(index)));
                    }
                }
                this.purchaseHistory.updateChildren(rows);
            }
        }
        if (this.enchantmentLevelList != null) {
            String signature = this.enchantmentLevels.keySet().toString();
            if (!signature.equals(this.enchantmentListSignature)) {
                this.enchantmentListSignature = signature;
                List<UiNode> rows = new ArrayList<>();
                for (RegistryEntry<Enchantment> enchantment : this.enchantmentLevels.keySet()) {
                    rows.add(this.createEnchantmentLevelRow(enchantment));
                }
                this.enchantmentLevelList.updateChildren(rows);
            }
        }
        if (this.statusEffectList != null) {
            String signature = this.statusEffectAmplifiers.keySet().toString();
            if (!signature.equals(this.statusEffectListSignature)) {
                this.statusEffectListSignature = signature;
                List<UiNode> rows = new ArrayList<>();
                for (RegistryEntry<StatusEffect> effect : this.statusEffectAmplifiers.keySet()) {
                    rows.add(this.createStatusEffectLevelRow(effect));
                }
                this.statusEffectList.updateChildren(rows);
            }
        }
        if (this.effectSelector != null) {
            String signature = this.effectSelectorOpen + "|" + this.selectedItemIsPotion() + "|" + this.effectSearchText;
            if (!signature.equals(this.effectSelectorSignature)) {
                this.effectSelectorSignature = signature;
                List<UiNode> rows = new ArrayList<>();
                if (this.effectSelectorOpen) {
                    String search = this.effectSearchText.trim().toLowerCase();
                    if (this.selectedItemIsPotion()) {
                        for (RegistryEntry<StatusEffect> effect : this.getAvailableStatusEffects()) {
                            if (search.isEmpty() || ItemConfigurationScreen.getStatusEffectDisplayName(effect).toLowerCase().contains(search)) {
                                rows.add(this.createStatusEffectOption(effect));
                            }
                        }
                    } else {
                        for (RegistryEntry<Enchantment> enchantment : this.getAvailableEnchantments()) {
                            if (search.isEmpty() || ItemConfigurationScreen.getEnchantmentDisplayName(enchantment).toLowerCase().contains(search)) {
                                rows.add(this.createEnchantmentOption(enchantment));
                            }
                        }
                    }
                }
                this.effectSelector.updateChildren(rows);
            }
        }
    }

    private TextComponent createDivider() {
        return new TextComponent().fillWidth().height(1.0f).paint((drawContext, textComponent) -> drawContext.drawRect(textComponent.x() - 11.0f, textComponent.y(), textComponent.w() + 22.0f, 1.0f, ColorPalette.BORDER_COLOR));
    }

    private TextComponent createEmptyStateText(String string) {
        return new TextComponent().text(Font.REGULAR.metrics(8.0f), string, textComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.4f)).textAlign(Alignment.CENTER).fillWidth().height(40.0f);
    }

    private boolean isSelectedItem(ItemConfigProcessor.ItemDefinition itemDefinition) {
        return this.selectedItem != null && this.selectedItem.getItemId().equals(itemDefinition.getItemId());
    }

    private boolean isItemConfigured(ItemConfigProcessor.ItemDefinition itemDefinition) {
        for (ItemCatalog.CatalogEntry catalogEntry : ItemCatalog.getEntries()) {
            if (!catalogEntry.getId().equals(itemDefinition.getItemId())) continue;
            return true;
        }
        return false;
    }

    private static String getItemDisplayName(ItemConfigProcessor.ItemDefinition itemDefinition) {
        return itemDefinition.getCustomName() != null ? itemDefinition.getCustomName() : itemDefinition.getStack().getName().getString();
    }

    private static String getCatalogEntryDisplayName(ItemCatalog.CatalogEntry catalogEntry) {
        return catalogEntry.getCustomName() != null ? catalogEntry.getCustomName() : catalogEntry.getStack().getName().getString();
    }

    private static String formatCatalogEntryDetails(ItemCatalog.CatalogEntry catalogEntry) {
        String string = catalogEntry.getPriceMode() == ItemCatalog.PriceMode.PERCENTAGE ? "-" + (int)catalogEntry.getPercentage() + Localization.translate("itemconfig.percent_of_market") : Localization.translate("itemconfig.up_to") + " " + MathUtils.formatCurrencyAmount(catalogEntry.getMaxPrice());
        long l = (long)EventListenerCoordinator.getInstance().parsePrice(catalogEntry.getId());
        return l > 0L ? string + " \u00b7 " + Localization.translate("itemconfig.market") + " " + MathUtils.formatCurrencyAmount(l) : string;
    }

    private static String ellipsizeText(FontMetrics fontMetrics, String string, float f) {
        if (fontMetrics.measureText(string) <= f) {
            return string;
        }
        while (string.length() > 1 && fontMetrics.measureText(string + "\u2026") > f) {
            string = string.substring(0, string.length() - 1);
        }
        return string + "\u2026";
    }

    private static List<PurchaseEntry> getPurchaseHistory() {
        AutoBuy autoBuy = RockstarClient.create().getModuleRegistry().getModule(AutoBuy.class);
        return autoBuy == null ? List.of() : autoBuy.getField();
    }

    public void tick() {
        InventoryMove.resetInputState();
        super.tick();
    }

    public boolean shouldPauseGame() {
        return true;
    }

    @Override
    public void close() {
        super.close();
        MinecraftClient.getInstance().setScreen(RockstarClient.create().getMinecraftScreen());
    }

    private static /* synthetic */ void addStatusEffectInstance(List list, RegistryEntry class_68802, Integer n) {
        list.add(new StatusEffectInstance(class_68802, 3600, Math.max(0, n - 1)));
    }

    static enum Section {
        PURCHASE,
        ACTIVITY,
        SETTINGS;
}
}
