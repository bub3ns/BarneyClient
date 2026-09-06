/*
 * 1:1 port of rockstar/ilIlil/IiiIiIiII (superclass rockstar/ilIlil/IiiIIiiiI -> MenuScreenBase).
 *
 * This is the Inventory Builder screen: a 318x149 panel with a preset list on the left
 * (118px wide) and, on the right, a name field + action row on top of a 41-slot inventory
 * grid.  It is opened by the InventoryBuilder module's "open" action setting.
 */
package moscow.rockstar.ui.screens;
import moscow.rockstar.ui.localization.Localization;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.mixin.accessors.DrawContextAccessor;
import moscow.rockstar.modules.other.inventory.InventoryBuilder;
import moscow.rockstar.modules.other.inventory.InventoryPresetEntry;
import moscow.rockstar.modules.other.inventory.InventoryPresetLayout;
import moscow.rockstar.modules.other.inventory.InventoryTraitLimit;
import moscow.rockstar.modules.visuals.menu.Menu;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.state.UiScissorStack;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.settings.StringSetting;
import moscow.rockstar.settings.TimeSetting;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.animation.Transition;
import moscow.rockstar.ui.color.ColorPickerScreen;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.DragMode;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.input.ScrollMode;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.layout.ItemGrid;
import moscow.rockstar.ui.layout.JustifyContent;
import moscow.rockstar.ui.text.EditableTextComponent;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.TextComponent;
import moscow.rockstar.ui.widgets.controls.ScrollBar;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

public class InventoryBuilderScreen
extends MenuScreenBase {
    /** ORIGINAL: private static final IIii I = IIii.I(160L, IiiiIiiII.IIII) */
    private static final Motion ACTIVE_MOTION = Motion.resolveMotionMotionFromLongAndEasing(160L, Easing.easeOutCubic);
    private static final float HEADER_HEIGHT = 22.0f;
    private static final float ICON_BUTTON_SIZE = 14.0f;
    private static final float ACTION_GAP = 4.0f;
    private static final float NAME_FIELD_RESERVE = 50.0f;
    private static final float NAME_FIELD_GAP = 4.0f;
    private static final float CONTENT_PADDING_X = 11.0f;
    private static final float CONTENT_PADDING_Y = 10.5f;
    private static final float PRESET_LIST_WIDTH = 118.0f;
    private static final float PRESET_ROW_HEIGHT = 15.0f;
    private static final float PRESET_LIST_TOP_INSET = 5.0f;
    private static final float PANEL_CORNER_RADIUS = 12.0f;
    private static final float PRESET_LIST_PADDING = 4.0f;
    private static final float PRESET_ROW_PADDING_X = 3.5f;
    private static final float HEADER_PADDING_X = 7.5f;
    private static final int PANEL_RADIUS = 12;
    private static final int ROW_FONT_SIZE = 7;
    private static final float SHADOW_TEXT_SCALE_X = 0.8f;
    private static final float SHADOW_TEXT_SCALE_Y = 1.2f;
    private static final int SLOT_COLUMNS = 9;
    private static final float SLOT_SIZE = 18.0f;
    private static final float SLOT_GAP = 2.0f;
    private static final float SLOT_PITCH = 20.0f;
    private static final float SLOT_ROW_GAP = 6.0f;
    private static final float GRID_WIDTH = 178.0f;
    private static final float GRID_TOP_OFFSET = 24.0f;
    private static final float GRID_HOTBAR_OFFSET = 88.0f;
    private static final float GRID_HEIGHT = 106.0f;

    /** ORIGINAL field iIiI */
    private float panelWidth;
    /** ORIGINAL field iIii */
    private float panelHeight;
    /** ORIGINAL field iiII */
    private float panelX;
    /** ORIGINAL field iiIi */
    private float panelY;
    /** ORIGINAL field iiiI */
    private float rightWidth;
    /** ORIGINAL field I Lrockstar/ilIlil/IIIIIiiiI$i; */
    InventoryPresetLayout selectedPreset;
    /** ORIGINAL field I Lrockstar/ilIlil/IIIIIiiiI$I; (the stack held on the cursor) */
    InventoryPresetEntry heldEntry;
    /** ORIGINAL field Ii I */
    int selectedSlot = -1;
    /** ORIGINAL field iI I */
    int hoveredSlot = -1;
    /** ORIGINAL field I Ljava/lang/String; */
    private String pickerSearchText = "";
    /** ORIGINAL field I Lrockstar/ilIlil/iii; */
    private Component presetListContainer;
    /** ORIGINAL field i Lrockstar/ilIlil/iii; */
    private Component detailContainer;
    /** ORIGINAL field II Lrockstar/ilIlil/iii; */
    private Component rootContainer;
    /** ORIGINAL field I Lrockstar/ilIlil/IIiII; */
    private EditableTextComponent nameField;
    /** ORIGINAL field Ii Lrockstar/ilIlil/iii; */
    private Component activeWindow;
    /** ORIGINAL field I Lrockstar/ilIlil/IiiIiIiII$I; */
    private SlotGrid slotGrid;
    /** ORIGINAL field I Ljava/util/Map; */
    private final Map<InventoryPresetLayout, Component> presetRows = new IdentityHashMap<InventoryPresetLayout, Component>();
    /** ORIGINAL field I Ljava/util/List; */
    private List<InventoryPresetLayout> lastPresetOrder = List.of();
    /** ORIGINAL field i Ljava/util/List; */
    private final List<Runnable> settingSync = new ArrayList<Runnable>();
    /** ORIGINAL field II Ljava/util/List; */
    private final List<ColorPickerScreen> popups = new ArrayList<ColorPickerScreen>();
    /** ORIGINAL field i Lrockstar/ilIlil/IIIIIiiiI$I; */
    private InventoryPresetEntry settingsEntry;
    /** ORIGINAL field II Lrockstar/ilIlil/IIIIIiiiI$I; */
    private InventoryPresetEntry previewEntry;
    /**
     * ORIGINAL field iI Lrockstar/ilIlil/iii;. Dead in the original too - it is only ever
     * assigned null (from iI()). Kept so the reset path stays instruction-for-instruction.
     */
    private Component unusedWindow;
    /** ORIGINAL field ii Lrockstar/ilIlil/iii;. Also only ever assigned null in the original. */
    private Component pickerWindow;
    /** ORIGINAL field I Z */
    private boolean presetsDirty;
    /** ORIGINAL field i Z */
    private boolean settingsDirty;
    /** ORIGINAL field Ii Ljava/util/List; */
    private List<InventoryPresetEntry> catalog;
    /** ORIGINAL field iI Ljava/util/List; */
    private List<String> catalogSearchKeys;
    /** ORIGINAL field i Ljava/lang/String; */
    private String lastSearchQuery;
    /** ORIGINAL field ii Ljava/util/List; */
    private List<InventoryPresetEntry> lastSearchResults;
    /** ORIGINAL field i Ljava/util/Map; */
    private final Map<InventoryPresetEntry, ItemStack> stackCache = new IdentityHashMap<InventoryPresetEntry, ItemStack>();

    public InventoryBuilderScreen() {
        InventoryBuilder.loadPresetFiles();
        if (!InventoryBuilder.listPresetNames().isEmpty()) {
            this.selectedPreset = InventoryBuilder.listPresetNames().getFirst();
        }
    }

    @Override
    protected boolean lowDrawBatching() {
        return true;
    }

    @Override
    @Compile(obfuscation=4)
    protected void initializeScreen() {
        super.initializeScreen();
        this.clearRoots();
        this.overlays.clear();
        this.rightWidth = 200.0f;
        this.panelWidth = 118.0f + this.rightWidth;
        this.panelHeight = 149.0f;
        this.panelX = Math.round(((float)this.width - this.panelWidth) / 2.0f);
        this.panelY = Math.round(((float)this.height - this.panelHeight) / 2.0f);
        this.rootContainer = new Component(){

            @Override
            protected void drawChildren(RockstarDrawContext drawContext, float f) {
                UiScissorStack.push(drawContext.getMatrices(), this.x(), this.y(), this.w(), this.h());
                super.drawChildren(drawContext, f);
                UiScissorStack.pop();
            }
        }.horizontal().motion(Motion.motion3).size(this.panelWidth, this.panelHeight).renderHook(this::drawPanelBackground);
        this.rootContainer.snapPosition();
        this.rootContainer.snapSize();
        this.rootContainer.snapAt(this.panelX, this.panelY);
        this.rootContainer.add(this.buildPresetPanel());
        this.rootContainer.add(this.buildDetailPanel());
        this.add(this.rootContainer);
        this.lastPresetOrder = List.of();
        this.refreshPresetRows(true);
        this.rebuildDetail();
    }

    /** ORIGINAL: private float I() */
    private float panelLeft() {
        return this.rootContainer == null ? this.panelX : this.rootContainer.x();
    }

    /** ORIGINAL: private float i() */
    private float panelTop() {
        return this.rootContainer == null ? this.panelY : this.rootContainer.y();
    }

    /** ORIGINAL: private void I(III, iii) - the main panel chrome. */
    private void drawPanelBackground(RockstarDrawContext drawContext, Component component) {
        float x = component.x();
        float y = component.y();
        float w = component.w();
        float h = component.h();
        ColorRGBA border = this.borderColor();
        drawContext.drawClientRect(x, y, w, h, 1.0f, 0.0f, 2.0f, 12.0f, false, true);
        float divider = x + 118.0f - 1.0f;
        drawContext.drawRect(divider, y + 1.0f, 1.0f, h - 2.0f, border);
        drawContext.drawRect(x + 1.0f, y + 22.0f - 1.0f, 116.0f, 1.0f, border);
        drawContext.drawRect(divider + 1.0f, y + 22.0f - 1.0f, w - 118.0f - 1.0f, 1.0f, border);
        drawContext.drawRoundedBorder(x, y, w, h, 0.5f, WidgetState.uniform(12.0f), border);
    }

    /** ORIGINAL: private iii I() - the left preset column. */
    private Component buildPresetPanel() {
        TextComponent header = new TextComponent().height(22.0f).fillWidth().padding(Insets.symmetric(0.0f, 7.5f)).text(Font.MEDIUM.metrics(7.0f), Localization.translate("presets"), textComponent -> this.textColor()).draggable(DragMode.BOTH);
        float footerHeight = 23.0f;
        this.presetListContainer = new Component().vertical().gap(2.0f).padding(Insets.of(4.0f, 5.0f, 0.0f, 4.0f)).width(118.0f).height(Math.max(0.0f, this.panelHeight - 22.0f - footerHeight)).scrollable().computeLayout(ScrollMode.AUTO).configureLayoutState(this::configureScrollBar);
        Component footer = new Component().vertical().width(118.0f).height(footerHeight).padding(Insets.of(0.0f, 4.0f, 4.0f, 4.0f)).add(new TextComponent().height(15.0f).fillWidth().radius(3.0f).text(Font.MEDIUM.metrics(7.0f), Localization.translate("inventory_builder.new_preset"), textComponent -> this.textColor().mix(ColorPalette.ACCENT_COLOR, 0.6f).mulAlpha(0.75f + 0.25f * textComponent.hover())).textAlign(Alignment.CENTER).background(textComponent -> InventoryBuilderScreen.blend(this.rowBaseColor(), ColorPalette.ACCENT_COLOR, 0.05f + 0.045f * textComponent.hover())).cursor(Cursor.HAND).onClick(this::createPreset));
        return new Component().vertical().width(118.0f).height(this.panelHeight).add(header).add(this.presetListContainer).add(footer);
    }

    /** ORIGINAL: private iii i() - the right column (name row + slot grid host). */
    private Component buildDetailPanel() {
        this.nameField = new EditableTextComponent(Font.REGULAR.metrics(7.0f), this.selectedPreset == null ? "" : this.selectedPreset.layoutName, this::renamePreset);
        this.nameField.placeholder(Localization.translate("inventory_builder.name"));
        this.nameField.width(this.rightWidth - 8.0f - 50.0f - 4.0f);
        this.nameField.height(14.0f);
        Component actions = new Component().horizontal().alignment(Alignment.CENTER).gap(4.0f).add(this.createIconButton("plus", ColorPalette.ACCENT_COLOR, this::openItemPickerForPreset)).add(this.createIconButton("play", new ColorRGBA(96.0f, 208.0f, 118.0f), this::startBuild).visibleWhen(this::isBuilderIdle)).add(this.createIconButton("xmark", new ColorRGBA(228.0f, 92.0f, 92.0f), this::stopBuild).visibleWhen(this::isBuilderRunning)).add(this.createIconButton("trash", new ColorRGBA(228.0f, 92.0f, 92.0f), this::deleteSelectedPreset));
        actions.snapSize();
        Component headerRow = new Component().horizontal().alignment(Alignment.CENTER).overflowMode(JustifyContent.SPACE_BETWEEN).padding(Insets.uniform(4.0f)).height(22.0f).width(this.rightWidth).add(this.nameField).add(actions);
        headerRow.snapSize();
        this.detailContainer = new Component().vertical().alignment(Alignment.CENTER).overflowMode(JustifyContent.CENTER).padding(Insets.of(10.5f, 11.0f, 10.5f, 11.0f)).size(this.rightWidth, Math.max(0.0f, this.panelHeight - 22.0f));
        this.detailContainer.snapSize();
        Component panel = new Component().vertical().size(this.rightWidth, this.panelHeight).add(headerRow).add(this.detailContainer);
        panel.snapSize();
        return panel;
    }

    /** ORIGINAL: private Iii I(String, ColorRGBA, Runnable) */
    private TextComponent createIconButton(String icon, ColorRGBA accent, Runnable action) {
        return new TextComponent().size(14.0f, 14.0f).padding(3.0f).radius(3.0f).icon(icon, 8.0f, textComponent -> this.textColor().mix(accent, 0.6f).mulAlpha(0.72f + 0.28f * textComponent.hover())).background(textComponent -> InventoryBuilderScreen.blend(this.buttonBaseColor(), accent, 0.05f + 0.05f * textComponent.hover() + 0.03f * textComponent.press())).cursor(Cursor.HAND).onClick(action);
    }

    /** ORIGINAL: private void I() */
    private void createPreset() {
        InventoryPresetLayout layout = new InventoryPresetLayout(Localization.translate("inventory_builder.preset") + " " + (InventoryBuilder.listPresetNames().size() + 1));
        InventoryBuilder.listPresetNames().add(layout);
        this.selectedPreset = layout;
        this.selectedSlot = -1;
        InventoryBuilder.savePresets();
        this.presetsDirty = true;
    }

    /** ORIGINAL: private void I(boolean) */
    private void refreshPresetRows(boolean force) {
        if (this.presetListContainer == null) {
            return;
        }
        List<InventoryPresetLayout> presets = InventoryBuilder.listPresetNames();
        if (!force && InventoryBuilderScreen.sameOrder(this.lastPresetOrder, presets)) {
            return;
        }
        ArrayList<UiNode> rows = new ArrayList<UiNode>();
        for (InventoryPresetLayout layout : presets) {
            rows.add(this.presetRows.computeIfAbsent(layout, this::createPresetRow));
        }
        this.presetListContainer.updateChildren(rows);
        this.lastPresetOrder = new ArrayList<InventoryPresetLayout>(presets);
    }

    /** ORIGINAL: private iii I(IIIIIiiiI$i) */
    private Component createPresetRow(InventoryPresetLayout layout) {
        TextComponent name = new TextComponent().fill().fade().text(Font.MEDIUM.metrics(7.0f), () -> layout.layoutName, textComponent -> this.textColor().mix(ColorPalette.ACCENT_COLOR, 0.5f * textComponent.sig("active")).mulAlpha(0.6f + 0.4f * textComponent.sig("active"))).bind("active", () -> this.selectedPreset == layout, ACTIVE_MOTION).interactive(false);
        TextComponent count = new TextComponent().width(18.0f).text(Font.REGULAR.metrics(7.0f), () -> String.valueOf(layout.getFilledSlotCount()), textComponent -> this.textColor().mulAlpha(0.42f)).textAlign(Alignment.END).interactive(false);
        return new Component().horizontal().alignment(Alignment.CENTER).height(15.0f).fillWidth().padding(Insets.symmetric(0.0f, 3.5f)).cornerRadius(3.0f).background(component -> InventoryBuilderScreen.blend(this.rowBaseColor(), ColorPalette.ACCENT_COLOR, 0.05f * component.sig("active") + 0.025f * component.hover())).bind("active", () -> this.selectedPreset == layout, ACTIVE_MOTION).cursor(Cursor.HAND).add(name).add(count).onClick((action, x, y) -> {
            if (action == PointerAction.RIGHT_CLICK) {
                this.openPresetContextMenu(layout, x, y);
                return;
            }
            if (this.selectedPreset == layout) {
                return;
            }
            this.selectedPreset = layout;
            this.selectedSlot = -1;
            this.presetsDirty = true;
        });
    }

    /** ORIGINAL: private void I(IIIIIiiiI$i, float, float) */
    private void openPresetContextMenu(InventoryPresetLayout layout, float x, float y) {
        ColorPickerScreen menu = new ColorPickerScreen(x, y, 108.0f).addTextRow("inventory_builder.preset").addSeparator().addActionOption(Localization.translate("inventory_builder.duplicate"), "copy", popup -> {
            InventoryPresetLayout copy = layout.copyWithName(layout.layoutName + " (" + Localization.translate("inventory_builder.copy_suffix") + ")");
            List<InventoryPresetLayout> presets = InventoryBuilder.listPresetNames();
            presets.add(presets.indexOf(layout) + 1, copy);
            this.selectedPreset = copy;
            this.selectedSlot = -1;
            InventoryBuilder.savePresets();
            this.presetsDirty = true;
            popup.setOpen(false);
        }).addActionOption(Localization.translate("inventory_builder.clear"), "xmark", popup -> {
            layout.clearSlots();
            if (this.selectedPreset == layout) {
                this.selectedSlot = -1;
            }
            InventoryBuilder.savePresets();
            popup.setOpen(false);
        }).addActionOption(Localization.translate("remove"), "trash", popup -> {
            this.presetRows.remove(layout);
            InventoryBuilder.listPresetNames().remove(layout);
            if (this.selectedPreset == layout) {
                this.selectedPreset = InventoryBuilder.listPresetNames().isEmpty() ? null : InventoryBuilder.listPresetNames().getFirst();
                this.selectedSlot = -1;
                this.heldEntry = null;
                this.presetsDirty = true;
            }
            InventoryBuilder.savePresets();
            popup.setOpen(false);
        });
        this.popups.add(menu);
    }

    /** ORIGINAL: private void i() */
    private void rebuildDetail() {
        if (this.nameField != null) {
            this.nameField.setText(this.selectedPreset == null ? "" : this.selectedPreset.layoutName);
        }
        if (this.detailContainer == null) {
            return;
        }
        if (this.selectedPreset == null) {
            this.slotGrid = null;
            this.detailContainer.replaceChildren(List.of(new TextComponent().fill().text(Font.REGULAR.metrics(8.0f), Localization.translate("inventory_builder.create_preset"), textComponent -> this.textColor().mulAlpha(0.38f)).textAlign(Alignment.CENTER).interactive(false)));
            return;
        }
        this.slotGrid = new SlotGrid().init();
        this.detailContainer.replaceChildren(List.of(this.slotGrid));
    }

    /** ORIGINAL: the IIiII change listener installed by i(). */
    private void renamePreset(String value) {
        if (this.selectedPreset == null) {
            return;
        }
        this.selectedPreset.layoutName = value.isBlank() ? Localization.translate("inventory_builder.preset") : value;
        InventoryBuilder.savePresets();
    }

    /** ORIGINAL: private iii I(float, float) - the floating draggable sub-window. */
    private Component createWindow(float w, float h) {
        Component window = new Component().vertical().padding(Insets.of(9.0f, 9.0f, 9.0f, 9.0f)).gap(6.0f).cornerRadius(11.0f).size(w, h).draggable(DragMode.BOTH).renderHook(this::drawWindowBackground).enter(Transition.SCALE_UP).exit(Transition.PROGRESS_ONLY);
        window.snapPosition();
        window.snapSize();
        window.snapAt(Math.max(4.0f, Math.min((float)this.width - w - 4.0f, this.panelLeft() + (this.panelWidth - w) / 2.0f)), Math.max(4.0f, Math.min((float)this.height - h - 4.0f, this.panelTop() + (this.panelHeight - h) / 2.0f)));
        return window;
    }

    private void drawWindowBackground(RockstarDrawContext drawContext, Component component) {
        drawContext.drawShadow(component.x(), component.y(), component.w(), component.h(), 10.0f, WidgetState.uniform(11.0f), ColorPalette.BLACK.mulAlpha(0.5f));
        drawContext.drawRoundedRect(component.x(), component.y(), component.w(), component.h(), WidgetState.uniform(11.0f), this.panelFillColor());
        drawContext.drawRoundedBorder(component.x(), component.y(), component.w(), component.h(), 0.5f, WidgetState.uniform(11.0f), this.borderColor());
        WidgetBatchRenderer.flushCurrentBatch();
    }

    /** ORIGINAL: private Iii I(String) */
    private TextComponent createWindowTitle(String title) {
        return new TextComponent().fillWidth().height(9.0f).text(Font.MEDIUM.metrics(8.0f), title, textComponent -> this.textColor().mulAlpha(0.85f)).interactive(false);
    }

    /** ORIGINAL: private void II() - the "choose an item" picker window. */
    private void openItemPicker() {
        this.pickerSearchText = "";
        Component window = this.createWindow(214.0f, 206.0f);
        window.add(this.createWindowTitle(Localization.translate("inventory_builder.pick_item")));
        EditableTextComponent search = new EditableTextComponent(Font.REGULAR.metrics(7.0f), "", value -> {
            this.pickerSearchText = value == null ? "" : value;
        });
        search.placeholder(Localization.translate("search"));
        search.background(field -> this.rowBaseColor());
        search.radius(3.0f);
        search.fillWidth();
        search.height(15.0f);
        window.add(search);
        ItemGrid<InventoryPresetEntry> grid = new ItemGrid<InventoryPresetEntry>(this::filteredCatalog, this::stackFor, entry -> false, entry -> {
            this.heldEntry = entry.copy();
            this.previewEntry = null;
            this.activeWindow = null;
            window.beginExit(0.0f);
        });
        grid.onItemHover(entry -> {
            this.previewEntry = entry;
        });
        grid.setZOffset(500);
        grid.setCellSize(18.0f);
        grid.setBackgroundColorProvider(itemGrid -> this.rowBaseColor());
        grid.fillWidth();
        grid.setHeight(150.0f);
        window.add(grid);
        this.activeWindow = this.openWindow(window);
    }

    /**
     * ORIGINAL: void I(IIIIIiiiI$I) - the per-entry condition popup opened by a middle click.
     *
     * OMITTED vs the original: the minimum-duration row cannot pass its clamp function
     * (Integer -> min(n, 720)); the remapped TimeSetting has no equivalent setter.
     */
    void openEntrySettings(InventoryPresetEntry entry) {
        this.settingsEntry = entry;
        this.settingSync.clear();
        ArrayList<Setting> settings = new ArrayList<Setting>();
        SettingOwner owner = () -> settings;
        int limit = this.stackLimit(entry);
        if (limit <= 1) {
            entry.requiredAmount = 1;
        } else {
            NumberSetting amount = new NumberSetting(owner, "inventory_builder.amount").setMinValue(1.0f).setMaxValue(limit).setStep(1.0f).setValue(entry.requiredAmount);
            this.settingSync.add(() -> {
                entry.requiredAmount = (int)amount.getValue();
            });
        }
        StringSetting price = new StringSetting(owner, "inventory_builder.max_price_each").setValue(String.valueOf(entry.maximumPrice)).setNumericOnly(true);
        this.settingSync.add(() -> {
            entry.maximumPrice = Math.max(0, this.parseInt(price.getValue(), 0));
        });
        if (entry.isItemDamageable()) {
            NumberSetting durability = new NumberSetting(owner, "inventory_builder.min_durability").setMinValue(0.0f).setMaxValue(100.0f).setStep(1.0f).setValue(entry.minimumDurabilityPercent).setUnit("%");
            this.settingSync.add(() -> {
                entry.minimumDurabilityPercent = (int)durability.getValue();
            });
        }
        if (entry.requiredDuration > 0) {
            TimeSetting duration = new TimeSetting(owner, "inventory_builder.min_duration").setUnitEnabled(TimeSetting.Unit.HOURS, false).setMaximumMinutes(13).setSeconds(entry.minimumDuration);
            this.settingSync.add(() -> {
                entry.minimumDuration = duration.getTotalSeconds();
            });
        }
        boolean enchanted = entry.createItemStack().hasEnchantments();
        String singular = Localization.translate(enchanted ? "enchantment" : "effect");
        String plural = enchanted ? "enchantments" : "effects";
        MultiBooleanSetting flags = null;
        for (InventoryTraitLimit trait : entry.traitLimits) {
            if (trait.maximumCount() > 0) continue;
            String name = trait.traitName();
            if (flags == null) {
                flags = new MultiBooleanSetting(owner, plural);
            }
            MultiBooleanSetting.Option option = new MultiBooleanSetting.Option(flags, name);
            if (entry.requiredTraitCounts.containsKey(name)) {
                option.select();
            }
            this.settingSync.add(() -> {
                if (option.isSelected()) {
                    entry.requiredTraitCounts.put(name, 0);
                } else {
                    entry.requiredTraitCounts.remove(name);
                }
            });
        }
        ArrayList<InventoryTraitLimit> counted = new ArrayList<InventoryTraitLimit>();
        for (InventoryTraitLimit trait : entry.traitLimits) {
            if (trait.maximumCount() <= 0) continue;
            counted.add(trait);
        }
        boolean singleVariant = entry.variantProperties.size() == 1;
        for (InventoryTraitLimit trait : singleVariant ? List.<InventoryTraitLimit>of() : counted) {
            String name = trait.traitName();
            if (!entry.requiredTraitCounts.containsKey(name)) continue;
            NumberSetting slider = new NumberSetting(owner, name).setMinValue(0.0f).setMaxValue(trait.maximumCount()).setStep(1.0f).setValue(Math.max(1, entry.requiredTraitCounts.get(name)));
            this.settingSync.add(() -> {
                int available = this.availableTraitCount(entry, name, trait.maximumCount());
                int clamped = Math.min(available, (int)slider.getValue());
                if (clamped != (int)slider.getValue()) {
                    slider.setValue(clamped);
                }
                if (clamped <= 0) {
                    entry.requiredTraitCounts.remove(name);
                    this.settingsDirty = true;
                } else {
                    entry.requiredTraitCounts.put(name, clamped);
                }
            });
        }
        ColorPickerScreen popup = new ColorPickerScreen(this.panelLeft() + this.panelWidth + 8.0f, this.panelTop(), 150.0f).addTextRow(entry.getFormattedDisplayName()).addSeparator();
        for (Setting setting : settings) {
            popup.addSetting(setting);
        }
        boolean canAdd = !singleVariant && counted.stream().anyMatch(trait -> !entry.requiredTraitCounts.containsKey(trait.traitName()) && this.availableTraitCount(entry, trait.traitName(), trait.maximumCount()) > 0);
        boolean canRemove = !singleVariant && counted.stream().anyMatch(trait -> entry.requiredTraitCounts.containsKey(trait.traitName()));
        if (canAdd || canRemove) {
            popup.addSeparator();
        }
        if (canAdd) {
            popup.addActionOption("+ " + singular, "plus", parent -> this.openAddTraitMenu(entry, counted, parent, singular));
        }
        if (canRemove) {
            popup.addActionOption(Localization.translate("inventory_builder.remove_trait") + " " + singular.toLowerCase(Locale.ROOT), "trash", parent -> this.openRemoveTraitMenu(entry, counted, parent, singular));
        }
        this.popups.add(popup);
        this.pickerWindow = null;
    }

    /** ORIGINAL: private void I(IIIIIiiiI$I, List, IiIIiiIii, String) */
    private void openAddTraitMenu(InventoryPresetEntry entry, List<InventoryTraitLimit> limits, ColorPickerScreen parent, String label) {
        ArrayList<InventoryTraitLimit> available = new ArrayList<InventoryTraitLimit>();
        for (InventoryTraitLimit trait : limits) {
            if (entry.requiredTraitCounts.containsKey(trait.traitName()) || this.availableTraitCount(entry, trait.traitName(), trait.maximumCount()) <= 0) continue;
            available.add(trait);
        }
        if (available.isEmpty()) {
            return;
        }
        ColorPickerScreen menu = new ColorPickerScreen(parent.getX() + 154.0f, parent.getY(), 130.0f).addTextRow(label).addSeparator();
        for (InventoryTraitLimit trait : available) {
            int count = this.availableTraitCount(entry, trait.traitName(), trait.maximumCount());
            menu.addActionOption(trait.traitName() + "  " + count, "check", popup -> {
                entry.requiredTraitCounts.put(trait.traitName(), count);
                InventoryBuilder.savePresets();
                popup.setOpen(false);
                parent.setOpen(false);
                this.settingsDirty = true;
            });
        }
        this.popups.add(menu);
    }

    /** ORIGINAL: private void i(IIIIIiiiI$I, List, IiIIiiIii, String) */
    private void openRemoveTraitMenu(InventoryPresetEntry entry, List<InventoryTraitLimit> limits, ColorPickerScreen parent, String label) {
        ArrayList<InventoryTraitLimit> present = new ArrayList<InventoryTraitLimit>();
        for (InventoryTraitLimit trait : limits) {
            if (!entry.requiredTraitCounts.containsKey(trait.traitName())) continue;
            present.add(trait);
        }
        if (present.isEmpty()) {
            return;
        }
        ColorPickerScreen menu = new ColorPickerScreen(parent.getX() + 154.0f, parent.getY(), 130.0f).addTextRow(label).addSeparator();
        for (InventoryTraitLimit trait : present) {
            menu.addActionOption(trait.traitName() + "  " + String.valueOf(entry.requiredTraitCounts.get(trait.traitName())), "trash", popup -> {
                entry.requiredTraitCounts.remove(trait.traitName());
                InventoryBuilder.savePresets();
                popup.setOpen(false);
                parent.setOpen(false);
                this.settingsDirty = true;
            });
        }
        this.popups.add(menu);
    }

    /** ORIGINAL: private void Ii() - builds the searchable item catalogue once. */
    private void buildCatalog() {
        this.catalog = new ArrayList<InventoryPresetEntry>();
        this.catalogSearchKeys = new ArrayList<String>();
        ArrayList<InventoryPresetEntry> known = new ArrayList<InventoryPresetEntry>(InventoryBuilder.listPresetFiles());
        known.sort(Comparator.comparingInt((InventoryPresetEntry entry) -> InventoryBuilder.findPresetIndex(entry.categoryName)));
        for (InventoryPresetEntry entry : known) {
            this.catalog.add(entry);
            this.catalogSearchKeys.add((entry.getFullSearchText() + " " + entry.getFormattedDisplayName()).toLowerCase(Locale.ROOT));
        }
        LinkedHashSet<String> seenPotions = new LinkedHashSet<String>();
        for (Potion potion : Registries.POTION) {
            Identifier potionId = Registries.POTION.getId(potion);
            if (potionId == null) continue;
            InventoryPresetEntry entry = new InventoryPresetEntry(Registries.ITEM.getId(Items.POTION), "");
            entry.potionEffectId = potionId.toString();
            entry.searchQuery = this.potionDisplayName(potion, entry);
            entry.customNameMatching = true;
            if (!seenPotions.add(entry.searchQuery.toLowerCase(Locale.ROOT))) continue;
            this.catalog.add(entry);
            this.catalogSearchKeys.add((entry.searchQuery + " " + String.valueOf(potionId)).toLowerCase(Locale.ROOT));
        }
        LinkedHashSet<String> knownNames = new LinkedHashSet<String>();
        for (InventoryPresetEntry entry : InventoryBuilder.listPresetFiles()) {
            knownNames.add(entry.getFullSearchText().toLowerCase(Locale.ROOT));
        }
        for (Item item : Registries.ITEM) {
            if (item == Items.POTION || item.getDefaultStack().isEmpty()) continue;
            Identifier itemId = Registries.ITEM.getId(item);
            String name = Text.translatable((String)item.getTranslationKey()).getString();
            if (knownNames.contains(name.toLowerCase(Locale.ROOT))) continue;
            this.catalog.add(new InventoryPresetEntry(itemId, name));
            this.catalogSearchKeys.add((name + " " + String.valueOf(itemId)).toLowerCase(Locale.ROOT));
        }
    }

    /** ORIGINAL: private List I() */
    private List<InventoryPresetEntry> filteredCatalog() {
        if (this.catalog == null) {
            this.buildCatalog();
        }
        String query = this.pickerSearchText.trim().toLowerCase(Locale.ROOT);
        if (query.equals(this.lastSearchQuery)) {
            return this.lastSearchResults;
        }
        ArrayList<InventoryPresetEntry> results = new ArrayList<InventoryPresetEntry>();
        for (int i = 0; i < this.catalog.size(); ++i) {
            if (!query.isEmpty() && !this.catalogSearchKeys.get(i).contains(query)) continue;
            results.add(this.catalog.get(i));
        }
        this.lastSearchQuery = query;
        this.lastSearchResults = results;
        return results;
    }

    /** ORIGINAL: private class_1799 I(IIIIIiiiI$I) */
    private ItemStack stackFor(InventoryPresetEntry entry) {
        return this.stackCache.computeIfAbsent(entry, InventoryPresetEntry::createItemStack);
    }

    /** ORIGINAL: private IIIIIiiiI I() */
    private InventoryBuilder builderModule() {
        return RockstarClient.create().getModuleRegistry().getModule(InventoryBuilder.class);
    }

    /** ORIGINAL: private boolean I() */
    private boolean isBuilderRunning() {
        InventoryBuilder module = this.builderModule();
        return module != null && module.isBuilderReady();
    }

    /** ORIGINAL: private synthetic boolean i() - the "play" button's visibility gate. */
    private boolean isBuilderIdle() {
        return !this.isBuilderRunning();
    }

    /** ORIGINAL: private synthetic void IiI() - the "plus" button. */
    private void openItemPickerForPreset() {
        if (this.selectedPreset != null) {
            this.openItemPicker();
        }
    }

    /** ORIGINAL: private synthetic void IIi() - the "play" button. */
    private void startBuild() {
        InventoryBuilder module = this.builderModule();
        if (module == null || this.selectedPreset == null) {
            return;
        }
        this.close();
        module.startBuild(this.selectedPreset);
    }

    /** ORIGINAL: private synthetic void III() - the "xmark" button. */
    private void stopBuild() {
        InventoryBuilder module = this.builderModule();
        if (module != null) {
            module.cancelBuild(Localization.translate("inventory_builder.stopped_manually"));
        }
    }

    /** ORIGINAL: private synthetic void ii() - the "trash" button. */
    private void deleteSelectedPreset() {
        if (this.selectedPreset == null) {
            return;
        }
        this.presetRows.remove(this.selectedPreset);
        InventoryBuilder.listPresetNames().remove(this.selectedPreset);
        this.selectedPreset = InventoryBuilder.listPresetNames().isEmpty() ? null : InventoryBuilder.listPresetNames().getFirst();
        this.selectedSlot = -1;
        this.heldEntry = null;
        InventoryBuilder.savePresets();
        this.presetsDirty = true;
    }

    /** ORIGINAL: private String I(class_1842, IIIIIiiiI$I) */
    private String potionDisplayName(Potion potion, InventoryPresetEntry entry) {
        String name;
        List<StatusEffectInstance> effects = potion.getEffects();
        if (!effects.isEmpty() && !(name = effects.getFirst().getEffectType().value().getName().getString()).isBlank()) {
            return "\u0417\u0435\u043b\u044c\u0435 " + name.toLowerCase(Locale.ROOT);
        }
        return entry.createItemStack().getName().getString();
    }

    /** ORIGINAL: private int I(IIIIIiiiI$I, String, int) */
    private int availableTraitCount(InventoryPresetEntry entry, String trait, int maximum) {
        if (!entry.variantProperties.isEmpty()) {
            int best = 0;
            for (Map<String, Integer> variant : entry.variantProperties) {
                boolean matches = true;
                for (Map.Entry<String, Integer> required : entry.requiredTraitCounts.entrySet()) {
                    if (required.getKey().equals(trait) || required.getValue() <= 0 || variant.getOrDefault(required.getKey(), 0) >= required.getValue()) continue;
                    matches = false;
                    break;
                }
                if (!matches) continue;
                best = Math.max(best, variant.getOrDefault(trait, 0));
            }
            return Math.min(maximum, best);
        }
        if (entry.budgetLimit > 0) {
            int used = 0;
            for (Map.Entry<String, Integer> required : entry.requiredTraitCounts.entrySet()) {
                if (required.getKey().equals(trait)) continue;
                used += Math.max(0, required.getValue());
            }
            return Math.max(0, Math.min(maximum, entry.budgetLimit - used));
        }
        return maximum;
    }

    /** ORIGINAL: boolean I(IIIIIiiiI$I, IIIIIiiiI$I) - identity used for stacking. */
    boolean sameEntry(InventoryPresetEntry a, InventoryPresetEntry b) {
        return a.itemId.equals(b.itemId) && a.getFullSearchText().equalsIgnoreCase(b.getFullSearchText());
    }

    /** ORIGINAL: int I(IIIIIiiiI$I) */
    int stackLimit(InventoryPresetEntry entry) {
        return entry.getStackLimit();
    }

    /** ORIGINAL: private int I(String, int) */
    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        }
        catch (Exception exception) {
            return fallback;
        }
    }

    /** ORIGINAL: private static boolean I(float x8) */
    private static boolean overlaps(float ax, float ay, float aw, float ah, float bx, float by, float bw, float bh) {
        return ax < bx + bw && bx < ax + aw && ay < by + bh && by < ay + ah;
    }

    /** ORIGINAL: private static boolean I(List, List) - identity-wise order comparison. */
    private static boolean sameOrder(List<?> a, List<?> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); ++i) {
            if (a.get(i) == b.get(i)) continue;
            return false;
        }
        return true;
    }

    /** ORIGINAL: private ColorRGBA I() */
    private ColorRGBA textColor() {
        return ColorPalette.getPrimaryTextColor();
    }

    /** ORIGINAL: private ColorRGBA i() */
    private ColorRGBA panelFillColor() {
        return ColorPalette.getPanelColor().withAlpha(242.25f);
    }

    /** ORIGINAL: private ColorRGBA II() */
    private ColorRGBA buttonBaseColor() {
        return ColorPalette.getPanelColor().withAlpha(102.0f);
    }

    /** ORIGINAL: ColorRGBA Ii() */
    ColorRGBA rowBaseColor() {
        return this.buttonBaseColor();
    }

    /** ORIGINAL: ColorRGBA iI() */
    ColorRGBA borderColor() {
        return ColorPalette.BORDER_COLOR.withAlpha(89.25f);
    }

    /** ORIGINAL: static ColorRGBA I(ColorRGBA, ColorRGBA, float) */
    static ColorRGBA blend(ColorRGBA base, ColorRGBA accent, float amount) {
        return base.mix(accent.withAlpha(base.getAlpha()), amount);
    }

    /** ORIGINAL: private void I(IiII) - the preset list scrollbar. */
    private void configureScrollBar(ScrollBar scrollBar) {
        scrollBar.offset(-3.0f).padding(2.0f).thickness(2.0f).minThumbLength(18.0f).cornerRadius(1.0f).hideDelay(1100.0f).thumbColor(bar -> this.textColor().withAlpha(255.0f * (0.28f + 0.24f * bar.hoverProgress() + 0.28f * bar.dragProgress())));
    }

    @Override
    public void render(RockstarDrawContext drawContext) {
        this.refreshPresetRows(false);
        if (this.presetsDirty) {
            this.presetsDirty = false;
            this.rebuildDetail();
        }
        if (this.settingsDirty) {
            this.settingsDirty = false;
            InventoryPresetEntry entry = this.settingsEntry;
            this.settingSync.clear();
            this.popups.forEach(popup -> popup.setOpen(false));
            if (entry != null) {
                this.openEntrySettings(entry);
            }
        }
        if (!this.popups.isEmpty() && !this.settingSync.isEmpty()) {
            this.settingSync.forEach(Runnable::run);
        }
        super.render(drawContext);
    }

    @Override
    protected void afterRender(RockstarDrawContext drawContext) {
        this.popups.removeIf(popup -> !popup.isOpen() && popup.getOpenAnimation().getValue() <= 0.01f);
        for (ColorPickerScreen popup : this.popups) {
            popup.render(drawContext);
        }
        FontMetrics hintFont = Font.REGULAR.metrics(6.0f);
        String hint = Localization.translate("inventory_builder.hint");
        float hintWidth = hintFont.measureText(hint);
        float hintHeight = hintFont.getFontTopOffset();
        float hintX = this.panelLeft() + (this.panelWidth - hintWidth) / 2.0f;
        float hintY = this.panelTop() + this.panelHeight + 9.0f;
        boolean covered = this.activeWindow != null && this.activeWindow.alive() && InventoryBuilderScreen.overlaps(hintX, hintY, hintWidth, hintHeight, this.activeWindow.x(), this.activeWindow.y(), this.activeWindow.w(), this.activeWindow.h());
        for (ColorPickerScreen popup : this.popups) {
            if (covered) break;
            covered = InventoryBuilderScreen.overlaps(hintX, hintY, hintWidth, hintHeight, popup.getX(), popup.getY(), popup.getWidth(), popup.getHeight());
        }
        if (!covered) {
            drawContext.drawText(hintFont, hint, hintX, hintY, this.textColor().mulAlpha(0.6f));
        }
        float mouseX = drawContext.mouseX();
        float mouseY = drawContext.mouseY();
        InventoryPresetEntry tooltipEntry = this.previewEntry != null && this.activeWindow != null && this.activeWindow.alive() ? this.previewEntry : this.hoveredEntry();
        if (tooltipEntry != null && this.heldEntry == null) {
            FontMetrics font = Font.REGULAR.metrics(7.0f);
            String label = tooltipEntry.getFormattedDisplayName();
            float width = font.measureText(label) + 10.0f;
            float height = 13.0f;
            float x = Math.min(mouseX + 9.0f, (float)this.width - width - 3.0f);
            float y = Math.max(3.0f, mouseY - height - 3.0f);
            drawContext.drawShadow(x, y, width, height, 8.0f, WidgetState.uniform(4.0f), ColorPalette.BLACK.mulAlpha(0.45f));
            drawContext.drawRoundedRect(x, y, width, height, WidgetState.uniform(4.0f), this.panelFillColor());
            drawContext.drawRoundedBorder(x, y, width, height, 0.5f, WidgetState.uniform(4.0f), this.borderColor());
            drawContext.drawText(font, label, x + 5.0f, y + height / 2.0f - font.getFontTopOffset() / 2.0f, this.textColor().mulAlpha(0.9f));
        }
        if (this.heldEntry == null) {
            return;
        }
        drawContext.drawItem(this.heldEntry.createItemStack(), mouseX - 8.0f, mouseY - 8.0f, 1.0f);
        if (this.heldEntry.requiredAmount > 1) {
            FontMetrics font = Font.REGULAR.metrics(7.0f);
            String label = String.valueOf(this.heldEntry.requiredAmount);
            drawContext.drawTextWithShadow(font, label, mouseX + 7.0f - font.measureText(label), mouseY + 7.0f - font.getFontTopOffset(), ColorPalette.WHITE, ColorPalette.BLACK, 0.8f, 0.8f, 1.2f);
        }
    }

    /** ORIGINAL: private IIIIIiiiI$I I() */
    private InventoryPresetEntry hoveredEntry() {
        if (this.selectedPreset == null || this.hoveredSlot < 0 || this.hoveredSlot >= 41) {
            return null;
        }
        return this.selectedPreset.slotEntries[this.hoveredSlot];
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, PointerAction action) {
        float x = (float)mouseX;
        float y = (float)mouseY;
        if (!this.popups.isEmpty()) {
            for (ColorPickerScreen popup : new ArrayList<ColorPickerScreen>(this.popups)) {
                popup.mouseClicked(mouseX, mouseY, action);
                if (popup.contains(mouseX, mouseY)) continue;
                popup.setOpen(false);
            }
            return;
        }
        if (this.closeActiveWindow(x, y)) {
            return;
        }
        if (this.heldEntry != null && action == PointerAction.LEFT_CLICK && (this.slotGrid == null || !this.slotGrid.contains(x, y))) {
            this.heldEntry = null;
            InventoryBuilder.savePresets();
            return;
        }
        super.onMouseClicked(mouseX, mouseY, action);
    }

    /** ORIGINAL: private void iI() - drops the item-picker state. */
    private void resetPicker() {
        this.pickerWindow = null;
        this.unusedWindow = null;
        this.settingsEntry = null;
        this.settingSync.clear();
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, PointerAction action) {
        for (ColorPickerScreen popup : this.popups) {
            popup.mouseReleased(mouseX, mouseY, action);
        }
        super.onMouseReleased(mouseX, mouseY, action);
    }

    /** ORIGINAL: private boolean I(float, float) */
    private boolean closeActiveWindow(float x, float y) {
        if (this.activeWindow == null) {
            return false;
        }
        if (!this.activeWindow.alive()) {
            this.activeWindow = null;
            return false;
        }
        if (this.activeWindow.contains(x, y)) {
            return false;
        }
        this.activeWindow.beginExit(0.0f);
        if (this.activeWindow == this.pickerWindow) {
            this.resetPicker();
        }
        this.activeWindow = null;
        InventoryBuilder.savePresets();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            if (this.activeWindow != null && this.activeWindow.alive()) {
                this.activeWindow.beginExit(0.0f);
                if (this.activeWindow == this.pickerWindow) {
                    this.resetPicker();
                }
                this.activeWindow = null;
                InventoryBuilder.savePresets();
                return true;
            }
            if (this.heldEntry != null) {
                this.heldEntry = null;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void renderBackground(DrawContext drawContext, int mouseX, int mouseY, float tickDelta) {
    }

    @Override
    public void close() {
        this.heldEntry = null;
        InventoryBuilder.savePresets();
        super.close();
        Menu.updateMenuState();
    }

    /** ORIGINAL: the private inner class rockstar/ilIlil/IiiIiIiII$I - the 41 slot grid. */
    final class SlotGrid
    extends UiNode {
        /** ORIGINAL field I Lrockstar/ilIlil/IiIII; - the button held down for paint-drag. */
        private PointerAction dragAction;
        /** ORIGINAL field I Ljava/util/Set; */
        private final Set<Integer> paintedSlots = new LinkedHashSet<Integer>();

        SlotGrid() {
            this.cursor(Cursor.HAND);
        }

        SlotGrid init() {
            this.size(178.0f, 106.0f);
            this.snapSize();
            return this;
        }

        private float slotX(int index) {
            if (index >= 36) {
                int column = index - 36;
                return this.x() + (float)(column == 4 ? 5 : column) * 20.0f;
            }
            return this.x() + (float)(index % 9) * 20.0f;
        }

        private float slotY(int index) {
            if (index >= 36) {
                return this.y();
            }
            if (index < 27) {
                return this.y() + 24.0f + (float)(index / 9) * 20.0f;
            }
            return this.y() + 88.0f;
        }

        private int slotAt(float x, float y) {
            for (int i = 0; i < 41; ++i) {
                float sx = this.slotX(i);
                float sy = this.slotY(i);
                if (!(x >= sx) || !(x <= sx + 18.0f) || !(y >= sy) || !(y <= sy + 18.0f)) continue;
                return i;
            }
            return -1;
        }

        @Override
        protected void onTick(float delta, float mouseX, float mouseY) {
            InventoryBuilderScreen.this.hoveredSlot = this.inFlow() && this.contains(mouseX, mouseY) ? this.slotAt(mouseX, mouseY) : -1;
            if (this.dragAction != PointerAction.RIGHT_CLICK || InventoryBuilderScreen.this.heldEntry == null || InventoryBuilderScreen.this.selectedPreset == null) {
                return;
            }
            int index = this.slotAt(mouseX, mouseY);
            if (index < 0 || this.paintedSlots.contains(index)) {
                return;
            }
            InventoryPresetEntry existing = InventoryBuilderScreen.this.selectedPreset.slotEntries[index];
            if (existing == null) {
                InventoryPresetEntry placed = InventoryBuilderScreen.this.heldEntry.copy();
                placed.requiredAmount = 1;
                InventoryBuilderScreen.this.selectedPreset.slotEntries[index] = placed;
            } else if (InventoryBuilderScreen.this.sameEntry(existing, InventoryBuilderScreen.this.heldEntry) && existing.requiredAmount < InventoryBuilderScreen.this.stackLimit(existing)) {
                ++existing.requiredAmount;
            } else {
                return;
            }
            this.paintedSlots.add(index);
            if (--InventoryBuilderScreen.this.heldEntry.requiredAmount <= 0) {
                InventoryBuilderScreen.this.heldEntry = null;
            }
            InventoryBuilder.savePresets();
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
            int i;
            InventoryPresetEntry entry;
            WidgetState radius = WidgetState.uniform(3.0f);
            ColorRGBA border = InventoryBuilderScreen.this.borderColor();
            MatrixStack matrices = drawContext.getMatrices();
            for (i = 0; i < 41; ++i) {
                float x = this.slotX(i);
                float y = this.slotY(i);
                boolean selected = InventoryBuilderScreen.this.selectedSlot == i;
                float tint = 0.05f * (selected ? 1.0f : 0.0f) + 0.03f * (InventoryBuilderScreen.this.hoveredSlot == i ? 1.0f : 0.0f);
                drawContext.drawRoundedRect(x, y, 18.0f, 18.0f, radius, InventoryBuilderScreen.blend(InventoryBuilderScreen.this.rowBaseColor(), ColorPalette.ACCENT_COLOR, tint).mulAlpha(alpha));
                drawContext.drawRoundedBorder(x, y, 18.0f, 18.0f, 0.5f, radius, (selected ? ColorPalette.ACCENT_COLOR.withAlpha(140.25f) : border).mulAlpha(alpha));
            }
            WidgetBatchRenderer.flushCurrentBatch();
            for (i = 0; i < 41; ++i) {
                entry = InventoryBuilderScreen.this.selectedPreset == null ? null : InventoryBuilderScreen.this.selectedPreset.slotEntries[i];
                if (entry == null) continue;
                float cx = this.slotX(i) + 9.0f;
                float cy = this.slotY(i) + 9.0f;
                ItemRenderUtils.translateAndScale(matrices, cx, cy, alpha);
                drawContext.drawBatchItem(entry.createItemStack(), cx - 8.0f, cy - 8.0f);
                ItemRenderUtils.popMatrix(matrices);
                DiffuseLighting.disableGuiDepthLighting();
            }
            ((DrawContextAccessor)((Object)drawContext)).getVertexConsumers().draw();
            FontMetrics font = Font.REGULAR.metrics(7.0f);
            for (i = 0; i < 41; ++i) {
                entry = InventoryBuilderScreen.this.selectedPreset == null ? null : InventoryBuilderScreen.this.selectedPreset.slotEntries[i];
                if (entry == null || entry.requiredAmount <= 1) continue;
                String label = String.valueOf(entry.requiredAmount);
                drawContext.drawTextWithShadow(font, label, this.slotX(i) + 18.0f - 1.5f - font.measureText(label), this.slotY(i) + 18.0f - 1.5f - font.getFontTopOffset(), ColorPalette.WHITE.mulAlpha(alpha), ColorPalette.BLACK.mulAlpha(alpha), 0.8f, 0.8f, 1.2f);
            }
        }

        @Override
        public boolean mouseClicked(float x, float y, PointerAction action) {
            if (!this.inFlow() || !this.contains(x, y) || InventoryBuilderScreen.this.selectedPreset == null) {
                return false;
            }
            int index = this.slotAt(x, y);
            if (index < 0) {
                return false;
            }
            InventoryPresetEntry existing = InventoryBuilderScreen.this.selectedPreset.slotEntries[index];
            if (action == PointerAction.MIDDLE_CLICK) {
                if (existing != null) {
                    InventoryBuilderScreen.this.selectedSlot = index;
                    InventoryBuilderScreen.this.openEntrySettings(existing);
                }
                return true;
            }
            if (net.minecraft.client.gui.screen.Screen.hasShiftDown()) {
                if (existing == null) {
                    return true;
                }
                InventoryBuilderScreen.this.selectedPreset.slotEntries[index] = null;
                if (InventoryBuilderScreen.this.selectedSlot == index) {
                    InventoryBuilderScreen.this.selectedSlot = -1;
                }
                InventoryBuilder.savePresets();
                return true;
            }
            if (action == PointerAction.LEFT_CLICK) {
                if (InventoryBuilderScreen.this.heldEntry == null) {
                    if (existing == null) {
                        return true;
                    }
                    InventoryBuilderScreen.this.heldEntry = existing;
                    InventoryBuilderScreen.this.selectedPreset.slotEntries[index] = null;
                    InventoryBuilderScreen.this.selectedSlot = -1;
                } else if (existing == null) {
                    InventoryBuilderScreen.this.selectedPreset.slotEntries[index] = InventoryBuilderScreen.this.heldEntry;
                    InventoryBuilderScreen.this.selectedSlot = index;
                    InventoryBuilderScreen.this.heldEntry = null;
                } else if (InventoryBuilderScreen.this.sameEntry(existing, InventoryBuilderScreen.this.heldEntry)) {
                    int moved = Math.min(InventoryBuilderScreen.this.heldEntry.requiredAmount, InventoryBuilderScreen.this.stackLimit(existing) - existing.requiredAmount);
                    existing.requiredAmount += moved;
                    InventoryBuilderScreen.this.heldEntry.requiredAmount -= moved;
                    if (InventoryBuilderScreen.this.heldEntry.requiredAmount <= 0) {
                        InventoryBuilderScreen.this.heldEntry = null;
                    }
                    InventoryBuilderScreen.this.selectedSlot = index;
                } else {
                    InventoryBuilderScreen.this.selectedPreset.slotEntries[index] = InventoryBuilderScreen.this.heldEntry;
                    InventoryBuilderScreen.this.heldEntry = existing;
                    InventoryBuilderScreen.this.selectedSlot = index;
                }
                InventoryBuilder.savePresets();
                return true;
            }
            if (action == PointerAction.RIGHT_CLICK) {
                if (InventoryBuilderScreen.this.heldEntry == null) {
                    if (existing == null) {
                        return true;
                    }
                    if (existing.requiredAmount <= 1) {
                        InventoryBuilderScreen.this.heldEntry = existing;
                        InventoryBuilderScreen.this.selectedPreset.slotEntries[index] = null;
                        InventoryBuilderScreen.this.selectedSlot = -1;
                    } else {
                        int half = (existing.requiredAmount + 1) / 2;
                        InventoryBuilderScreen.this.heldEntry = existing.copy();
                        InventoryBuilderScreen.this.heldEntry.requiredAmount = half;
                        existing.requiredAmount -= half;
                    }
                } else if (existing == null) {
                    InventoryPresetEntry placed = InventoryBuilderScreen.this.heldEntry.copy();
                    placed.requiredAmount = 1;
                    InventoryBuilderScreen.this.selectedPreset.slotEntries[index] = placed;
                    if (--InventoryBuilderScreen.this.heldEntry.requiredAmount <= 0) {
                        InventoryBuilderScreen.this.heldEntry = null;
                    }
                    InventoryBuilderScreen.this.selectedSlot = index;
                } else if (InventoryBuilderScreen.this.sameEntry(existing, InventoryBuilderScreen.this.heldEntry) && existing.requiredAmount < InventoryBuilderScreen.this.stackLimit(existing)) {
                    ++existing.requiredAmount;
                    if (--InventoryBuilderScreen.this.heldEntry.requiredAmount <= 0) {
                        InventoryBuilderScreen.this.heldEntry = null;
                    }
                    InventoryBuilderScreen.this.selectedSlot = index;
                }
                this.dragAction = PointerAction.RIGHT_CLICK;
                this.paintedSlots.clear();
                this.paintedSlots.add(index);
                InventoryBuilder.savePresets();
                return true;
            }
            return false;
        }

        @Override
        public void mouseReleased(float x, float y, PointerAction action) {
            this.dragAction = null;
            this.paintedSlots.clear();
        }

        @Override
        public boolean mouseScrolled(float x, float y, float horizontal, float vertical) {
            if (!this.inFlow() || !this.contains(x, y) || InventoryBuilderScreen.this.selectedPreset == null) {
                return false;
            }
            int index = this.slotAt(x, y);
            if (index < 0) {
                return false;
            }
            InventoryPresetEntry entry = InventoryBuilderScreen.this.selectedPreset.slotEntries[index];
            if (entry == null) {
                return false;
            }
            int step = net.minecraft.client.gui.screen.Screen.hasShiftDown() ? 10 : 1;
            entry.requiredAmount = Math.max(1, Math.min(InventoryBuilderScreen.this.stackLimit(entry), entry.requiredAmount + (vertical > 0.0f ? step : -step)));
            InventoryBuilderScreen.this.selectedSlot = index;
            InventoryBuilder.savePresets();
            return true;
        }
    }
}
