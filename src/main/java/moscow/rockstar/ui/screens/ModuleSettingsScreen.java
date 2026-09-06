/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Vec3d
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.DrawContext
 *  net.minecraft.Camera
 *  net.minecraft.Screen
 *  net.minecraft.MatrixStack
 *  net.minecraft.BuiltBuffer
 *  org.joml.Matrix4f
 *  org.lwjgl.glfw.GLFW
 */
package moscow.rockstar.ui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.modules.ModuleRegistry;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.player.inventory.InventoryMove;
import moscow.rockstar.modules.visuals.audio.Sounds;
import moscow.rockstar.modules.visuals.audio.SoundEffectPlayer;
import moscow.rockstar.modules.visuals.menu.Menu;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.network.session.BotPacketListener;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.overlay.OverlayElement;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.target.RenderTarget;
import moscow.rockstar.render.target.RenderTextureRegistry;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.TextLabelSetting;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
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
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.search.SearchMatcher;
import moscow.rockstar.ui.screens.AssistScreen;
import moscow.rockstar.ui.screens.EspSettingsScreen;
import moscow.rockstar.ui.screens.ItemConfigurationScreen;
import moscow.rockstar.ui.settings.SettingPanel;
import moscow.rockstar.ui.settings.SettingSnapshotCache;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.ScrollingTextComponent;
import moscow.rockstar.ui.text.TextComponent;
import moscow.rockstar.ui.text.TextInputField;
import moscow.rockstar.ui.widgets.controls.ScrollBar;
import moscow.rockstar.ui.widgets.settings.KeyBindingControl;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import pyrock.events.render.HudRenderEvent;
import pyrock.events.render.MenuRenderEvent;
import pyrock.events.render.PostMenuRenderEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

public class ModuleSettingsScreen
extends MenuScreenBase
implements OverlayElement {
    private static final Motion CATEGORY_HOVER_MOTION = Motion.resolveMotionMotionFromLongAndEasing(160L, Easing.easeOutCubic);
    private static final float MAX_PANEL_WIDTH = 488.0f;
    private static final float MAX_PANEL_HEIGHT = 318.0f;
    private static final float SIDEBAR_WIDTH = 33.0f;
    private static final float CATEGORY_COLUMN_WIDTH = 101.0f;
    private static final float HEADER_HEIGHT = 24.0f;
    private static final float LOGO_INSET = 11.0f;
    private static final float PANEL_PADDING = 10.5f;
    private static final float COLUMN_GAP = 2.0f;
    private static final float SETTING_GAP = 2.0f;
    private static final float TITLE_BAR_HEIGHT = 22.0f;
    private static final float LOGO_CENTER_OFFSET = 5.5f;
    private static final float CORNER_RADIUS = 5.0f;
    private static final float KEYBIND_WIDTH = 72.0f;
    private static final float SETTING_PADDING = 5.0f;
    private static final float BORDER_WIDTH = 1.0f;
    private static final float TEXT_PADDING = 3.0f;
    private static final float DISABLED_TEXT_ALPHA = 0.68f;
    private static final float ANIMATION_STEP = 5.0f;
    private static final float INDICATOR_WIDTH = 2.0f;
    private static final float SCROLLBAR_OFFSET = -3.0f;
    private static final float PANEL_LEFT_OFFSET = -9.0f;
    private static final float HEADER_TEXT_SIZE = 7.0f;
    private static final float SMALL_COMPONENT_GAP = 5.0f;
    private static final float FULL_CIRCLE_DEGREES = 90.0f;
    private static final float CATEGORY_ICON_SIZE = 17.0f;
    private static final float SMALL_PADDING = 5.0f;
    private static final float LABEL_GAP = 7.0f;
    private static final float SETTING_TEXT_SIZE = 6.0f;
    private static final float FADE_DURATION_MILLIS = 300.0f;
    private static final float DISABLED_COMPONENT_ALPHA = 0.35f;
    private static final float KEYBIND_HEIGHT = 12.0f;
    private static final float SELECTION_FADE_MILLIS = 300.0f;
    private static final float CONTENT_TEXTURE_SIZE = 1600.0f;
    private static final float HOVER_ALPHA = 0.4f;
    private final Map<ModuleContract, Component> moduleComponentCache = new IdentityHashMap<ModuleContract, Component>();
    private final Map<Setting, Component> settingComponentCache = new IdentityHashMap<Setting, Component>();
    private ModuleCategory selectedCategory = ModuleCategory.COMBAT;
    private ModuleContract selectedModule;
    private ModuleContract keybindTargetModule;
    private Component moduleList;
    private float panelY;
    private Component settingsList;
    private Component settingsScrollContainer;
    private Component draggedComponent;
    private Component searchContainer;
    private TextComponent searchField;
    private TextComponent settingsButton;
    private TextInputField searchTextLayout;
    private KeyBindingControl keybindControl;
    private SettingPanel settingPanel;
    private UiNode categoryList;
    private Component settingsContent;
    private long settingPanelDeadline;
    private float panelWidth;
    private float panelHeight;
    private float sidebarWidth;
    private float categoryColumnWidth;
    private List<ModuleContract> visibleModules;
    private List<Setting> visibleSettings;
    private int moduleRegistryVersion = -1;
    private String searchQuery = "";
    private float settingsWidth;
    private long lastFrameTime;
    private long selectionAnimationStart;
    private long searchAnimationStart;
    Vec3d cameraPosition;
    private Vec3d viewDirection;
    Vec3d upDirection;
    Vec3d rightDirection;
    private float headerHeight;
    private float contentColumnWidth;
    private float selectionCornerRadius;
    private float settingsScrollOffset = Float.NaN;
    private float lastPanelX = Float.NaN;
    private float lastPanelY = Float.NaN;
    private float settingsRevealProgress;
    private float panelX;
    static final RenderTarget uiRenderTarget = new RenderTarget(false).enableLinearFiltering();
    static boolean panelOpen;
    static boolean settingsPanelOpen;
    static ModuleSettingsScreen INSTANCE;

    protected boolean lowDrawBatching() {
        return true;
    }

    public ModuleSettingsScreen() {
        this.visibleModules = List.of();
        this.visibleSettings = List.of();
    }

    @Compile(obfuscation=4)
    protected void init() {
        super.init();
        this.closing = false;
        this.contentAlpha = 1.0f;
        this.keybindTargetModule = null;
        this.settingsRevealProgress = 0.0f;
        this.lastFrameTime = 0L;
        this.selectionAnimationStart = System.currentTimeMillis();
        this.settingsScrollOffset = Float.NaN;
        ShaderRenderer.textureRegistry.invalidateAll();
        this.clearRoots();
        this.panelWidth = Math.min(488.0f, Math.max(360.0f, (float)this.width - 12.0f));
        this.panelHeight = Math.min(318.0f, Math.max(235.0f, (float)this.height - 12.0f));
        this.panelX = Math.round(((float)this.width - this.panelWidth) / 2.0f);
        this.panelY = Math.round(((float)this.height - this.panelHeight) / 2.0f);
        this.sidebarWidth = this.panelWidth * 33.0f / 488.0f;
        this.categoryColumnWidth = this.panelWidth * 101.0f / 488.0f;
        this.settingsWidth = this.panelWidth - this.sidebarWidth - this.categoryColumnWidth;
        this.contentColumnWidth = Math.max(80.0f, (this.settingsWidth - 22.0f - 5.0f - 5.0f) / 2.0f);
        this.headerHeight = this.panelHeight * 24.0f / 318.0f;
        if (this.searchTextLayout == null) {
            this.searchTextLayout = new TextInputField(Font.MEDIUM.metrics(7.0f));
            this.searchTextLayout.setPlaceholder(Localization.translate("search"));
        }
        Component component2 = new Component(){

            @Override
            protected void drawChildren(RockstarDrawContext drawContext, float f) {
                moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)this.x(), (float)this.y(), (float)this.w(), (float)this.h());
                super.drawChildren(drawContext, f);
                moscow.rockstar.render.state.UiScissorStack.pop();
            }
        }.horizontal().motion(Motion.motion3).size(this.panelWidth, this.panelHeight).renderHook((drawContext, component) -> this.drawPanelSurface(drawContext, component));
        component2.snapPosition();
        component2.snapSize();
        component2.snapAt(this.panelX, this.panelY);
        component2.add(this.createCategorySidebar());
        component2.add(this.createModuleList());
        this.searchContainer = this.createSettingsHeader();
        component2.add(this.searchContainer);
        this.draggedComponent = component2;
        this.add(component2);
        this.settingPanel = new SettingPanel(component2, this::selectModuleSetting).showEmbeddedSettings(this.settingsButton).configureEmbeddedKeybinds(this.searchField, Font.MEDIUM.metrics(7.0f));
        this.add(this.settingPanel);
        this.add(this.settingPanel.getKeybindList());
        this.add(this.settingPanel.getSettingsContainer());
        this.visibleModules = List.of();
        this.visibleSettings = List.of();
        this.searchQuery = "\u0000";
        this.refreshModuleList(true);
        this.refreshSettingsList(true);
    }

    @Compile(obfuscation=1)
    private Component createCategorySidebar() {
        TextComponent textComponent2 = new TextComponent().width(this.sidebarWidth).height(this.headerHeight).paint((drawContext, textComponent) -> drawContext.drawIcon("logo", textComponent.x() + textComponent.w() / 2.0f - 5.5f, textComponent.y() + 11.0f, 11.0f, ColorPalette.ACCENT_COLOR)).draggable(DragMode.BOTH);
        Component component = new Component().vertical().alignment(Alignment.CENTER).gap(3.0f).width(this.sidebarWidth);
        for (ModuleCategory moduleCategory : ModuleCategory.values()) {
            TextComponent textComponent3 = new TextComponent().size(17.0f, 17.0f).padding(4.0f).icon("category/" + moduleCategory.getDisplayName().toLowerCase(), 9.0f, textComponent -> this.getOverlayAccentColor().mix(ColorPalette.ACCENT_COLOR, textComponent.sig("selected")).mulAlpha(0.62f + 0.38f * textComponent.sig("selected"))).background(textComponent -> ModuleSettingsScreen.interpolateColor(this.getPanelBackgroundColor(), ColorPalette.ACCENT_COLOR, 0.025f * textComponent.hover())).radius(4.0f).bind("selected", () -> this.selectedCategory == moduleCategory, CATEGORY_HOVER_MOTION).cursor(Cursor.HAND).onClick(() -> this.selectCategory(moduleCategory));
            component.add(textComponent3);
        }
        Component component2 = new Component().vertical().alignment(Alignment.CENTER).gap(9.0f).add(textComponent2).add(component);
        this.settingsButton = new TextComponent().size(17.0f, 17.0f).padding(4.0f).icon("setting", 9.0f, textComponent -> this.getOverlayAccentColor().mulAlpha(0.58f + 0.35f * textComponent.hover())).background(textComponent -> ModuleSettingsScreen.interpolateColor(this.getPanelBackgroundColor(), ColorPalette.ACCENT_COLOR, 0.025f * textComponent.hover())).radius(4.0f).cursor(Cursor.HAND).onClick(this::toggleSettingsPanel);
        return new Component().vertical().alignment(Alignment.CENTER).overflowMode(JustifyContent.SPACE_BETWEEN).padding(Insets.of(0.0f, 0.0f, 8.0f, 0.0f)).width(this.sidebarWidth).fillHeight().draggable(DragMode.BOTH).add(component2).add(this.settingsButton);
    }

    @Compile(obfuscation=1)
    private Component createModuleList() {
        TextComponent textComponent2 = new TextComponent().height(this.headerHeight).fillWidth().padding(Insets.of(5.0f, 8.0f, 1.0f, 8.0f)).text(Font.MEDIUM.metrics(9.0f), () -> this.selectedCategory.getDisplayName(), textComponent -> this.getOverlayAccentColor()).draggable(DragMode.BOTH);
        this.moduleList = new Component().vertical().gap(2.0f).padding(Insets.of(1.0f, 5.0f, 5.0f, 4.0f)).width(this.categoryColumnWidth).height(Math.max(0.0f, this.panelHeight - this.headerHeight)).scrollable().computeLayout(ScrollMode.AUTO).configureLayoutState(this::onScrollBarChanged);
        return new Component().vertical().width(this.categoryColumnWidth).height(this.panelHeight).add(textComponent2).add(this.moduleList);
    }

    @Compile(obfuscation=1)
    private Component createSettingsHeader() {
        this.searchField = new TextComponent().size(81.0f, 12.0f).radius(3.0f).cursor(Cursor.IBEAM).onClick((pointerAction, f, f2) -> {
            if (pointerAction != PointerAction.LEFT_CLICK || this.settingPanel == null) {
                return;
            }
            if (this.settingPanel.isSearchOpen()) {
                this.settingPanel.getSearchEditor().mouseClicked(f, f2, pointerAction);
            } else {
                this.settingPanel.openKeybindSearch();
            }
        }).paint((drawContext, textComponent) -> {
            drawContext.drawRoundedRect(textComponent.x(), textComponent.y(), textComponent.w(), 12.0f, WidgetState.uniform(3.0f), ModuleSettingsScreen.interpolateColor(this.getPanelBorderColor().withAlpha(173.40001f), this.getOverlayAccentColor(), 0.035f + 0.035f * textComponent.hover()));
            drawContext.drawIcon("search", textComponent.x() + 3.5f, textComponent.y() + 3.5f, 5.0f, this.getOverlayAccentColor().mulAlpha(0.48f));
            if (this.settingPanel == null) {
                return;
            }
            TextInputField textInputField = this.settingPanel.getSearchEditor();
            textInputField.setBounds(textComponent.x() + 8.5f, textComponent.y(), textComponent.w() - 10.5f, 12.0f);
            textInputField.setPlaceholder(Localization.translate("search"));
            textInputField.setTextColor(this.getOverlayAccentColor().mulAlpha(0.72f));
            textInputField.setOpacity(1.0f);
            textInputField.render(drawContext);
        });
        Component component2 = new Component(){

            @Override
            protected void drawChildren(RockstarDrawContext drawContext, float f) {
                moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)this.x(), (float)(this.y() - 1.0f), (float)this.w(), (float)(this.h() + 2.0f));
                super.drawChildren(drawContext, f);
                moscow.rockstar.render.state.UiScissorStack.pop();
            }
        }.vertical().alignment(Alignment.END).gap(1.0f).size(72.0f, 11.0f).add(new TextComponent().height(5.0f).fillWidth().text(Font.MEDIUM.metrics(6.0f), this::getProfileUsername, textComponent -> this.getOverlayAccentColor()).textAlign(Alignment.END).interactive(false)).add(new TextComponent().height(5.0f).fillWidth().text(Font.REGULAR.metrics(6.0f), this::getProfileTag, textComponent -> this.getOverlayAccentColor().mulAlpha(0.52f)).textAlign(Alignment.END).interactive(false));
        TextComponent textComponent2 = new TextComponent().size(12.0f, 12.0f).interactive(false).paint((drawContext, textComponent) -> drawContext.drawRoundedTexture(SettingPanel.getAvatarTexture(), textComponent.x(), textComponent.y(), textComponent.w(), textComponent.h(), WidgetState.uniform(textComponent.w() / 2.0f), ColorPalette.WHITE));
        Component component3 = new Component().horizontal().alignment(Alignment.CENTER).gap(3.0f).add(component2).add(textComponent2);
        Component component4 = new Component().horizontal().alignment(Alignment.CENTER).overflowMode(JustifyContent.SPACE_BETWEEN).padding(Insets.of(0.0f, 7.0f, 0.0f, 5.0f)).height(this.headerHeight).fillWidth().draggable(DragMode.BOTH).add(this.searchField).add(component3);
        component4.snapSize();
        TextComponent textComponent3 = new TextComponent().height(12.0f).text(Font.MEDIUM.metrics(12.0f), this::getSelectedModuleTitle, textComponent -> this.getOverlayAccentColor()).interactive(false);
        this.keybindControl = new KeyBindingControl(Font.REGULAR.metrics(7.0f), this::getSelectedModuleKeybind, this::setSelectedModuleKeybind);
        UiNode uiNode = new Component().horizontal().alignment(Alignment.CENTER).height(12.0f).visibleWhen(() -> this.selectedModule != null).cursor(Cursor.HAND).add(this.keybindControl).onClick(this.keybindControl::handlePointerClick);
        Component component5 = new Component().horizontal().alignment(Alignment.CENTER).gap(3.0f).fill().add(textComponent3).add(uiNode);
        TextComponent textComponent4 = new TextComponent().size(16.0f, 16.0f).padding(3.5f).icon("xmark", 9.0f, textComponent -> this.getOverlayAccentColor().mulAlpha(0.8f + 0.2f * textComponent.hover())).radius(4.0f).cursor(Cursor.HAND).visibleWhen(() -> this.selectedModule != null).onClick(() -> this.selectModule(null));
        Component component6 = new Component().horizontal().alignment(Alignment.CENTER).overflowMode(JustifyContent.SPACE_BETWEEN).height(12.0f).fillWidth().add(component5).add(textComponent4);
        ScrollingTextComponent scrollingTextComponent2 = new ScrollingTextComponent(Font.REGULAR.metrics(7.0f), this::getSelectedModuleDescription).setColorProvider(scrollingTextComponent -> this.getOverlayAccentColor().mulAlpha(0.58f)).fadeOut().fillWidth();
        scrollingTextComponent2.interactive(false);
        Component component7 = new Component().vertical().gap(1.0f).height(22.0f).fillWidth().renderHook((drawContext, component) -> this.drawSettingsBackdrop(drawContext, component)).add(component6).add(scrollingTextComponent2);
        this.settingsList = new Component().vertical().gap(5.0f).padding(Insets.of(27.5f, 0.0f, 2.0f, 0.0f)).fillWidth();
        this.settingsList.snapSize();
        float f3 = Math.max(0.0f, this.panelHeight - this.headerHeight);
        float f4 = Math.max(80.0f, f3 - 10.5f - 2.0f);
        this.settingsScrollContainer = new Component().vertical().padding(Insets.of(0.0f, 5.0f, 0.0f, 0.0f)).width(this.settingsWidth - 22.0f).height(f4).scrollable().scrollStep(80.0f).computeLayout(ScrollMode.AUTO).configureLayoutState(scrollBar -> {
            this.onScrollBarChanged((ScrollBar)scrollBar);
            scrollBar.offset(-9.0f).padding(2.0f, 5.0f);
        }).add(this.settingsList);
        this.settingsScrollContainer.snapSize();
        Component component8 = new Component().uniformLayout().padding(Insets.of(10.5f, 11.0f, 2.0f, 11.0f)).size(this.settingsWidth, f3).add(this.settingsScrollContainer).add(this.createMenuGrid()).add(this.createSelectedModulePlaceholder()).add(component7);
        component8.snapSize();
        Component component9 = new Component().vertical().size(this.settingsWidth, this.panelHeight).add(component4).add(component8);
        component9.snapSize();
        return component9;
    }

    @Compile(obfuscation=1)
    private Component createMenuGrid() {
        Component component = new Component().vertical().columns(2).gap(5.0f).add(this.createMenuTile("menu/swing", () -> "Swing Animations", ColorPresetsScreen::new)).add(this.createMenuTile("menu/builder", () -> "Inventory Builder", InventoryBuilderScreen::new)).add(this.createMenuTile("menu/esp", () -> "ESP", EspSettingsScreen::new)).add(this.createMenuTile("menu/assist", () -> Localization.translate("menu.modern.shortcuts.item_binds"), AssistScreen::new)).add(this.createMenuTile("menu/autobuy", () -> "Auto Buy", ItemConfigurationScreen::new));
        component.snapSize();
        FontMetrics fontMetrics = Font.REGULAR.metrics(9.0f);
        TextComponent textComponent2 = new TextComponent().text(fontMetrics, () -> Localization.translate("menu.modern.shortcuts"), textComponent -> this.getOverlayAccentColor().mulAlpha(0.9f)).interactive(false);
        return new Component().vertical().alignment(Alignment.CENTER).overflowMode(JustifyContent.CENTER).gap(6.0f).padding(Insets.of(0.0f, 0.0f, this.headerHeight + 10.5f - 2.0f + fontMetrics.getFontTopOffset() + 6.0f, 0.0f)).fill().interactive(false).visibleWhen(() -> this.selectedModule == null, Easing.easeOutQuart, 220L).add(textComponent2).add(component);
    }

    @Compile(obfuscation=1)
    private Component createSelectedModulePlaceholder() {
        TextComponent textComponent2 = new TextComponent().text(Font.REGULAR.metrics(9.0f), () -> Localization.translate("menu.modern.no_settings"), textComponent -> this.getOverlayAccentColor().mulAlpha(0.55f)).interactive(false);
        return new Component().vertical().alignment(Alignment.CENTER).overflowMode(JustifyContent.CENTER).fill().interactive(false).visibleWhen(this::isSelectedModuleReady, Easing.easeOutQuart, 220L).add(textComponent2);
    }

    private boolean isSelectedModuleReady() {
        if (this.selectedModule == null) {
            return false;
        }
        for (Setting setting : this.selectedModule.getSettings()) {
            if (!setting.hasValidSettingValue()) continue;
            return false;
        }
        return true;
    }

    @Compile(obfuscation=1)
    private Component createMenuTile(String string, Supplier<String> supplier, Supplier<Screen> supplier2) {
        Component component2 = new Component();
        TextComponent textComponent2 = new TextComponent().fill().fade().text(Font.REGULAR.metrics(7.0f), supplier, textComponent -> this.getOverlayAccentColor().mulAlpha(0.72f + 0.28f * component2.hover())).interactive(false);
        TextComponent textComponent3 = new TextComponent().size(7.0f, 7.0f).icon(string, 7.0f, textComponent -> this.getOverlayAccentColor().mulAlpha(0.62f + 0.38f * component2.hover())).interactive(false);
        return component2.horizontal().alignment(Alignment.CENTER).size(90.0f, 17.0f).padding(Insets.of(0.0f, 5.0f, 0.0f, 6.0f)).cornerRadius(4.0f).background(component -> ModuleSettingsScreen.interpolateColor(ColorPalette.getPanelBackgroundColor().mulAlpha(0.4f), ColorPalette.ACCENT_COLOR, 0.05f * component.hover())).cursor(Cursor.HAND).add(textComponent2).add(textComponent3).onClick(() -> this.openScreen((Screen)supplier2.get()));
    }

    @Compile(obfuscation=1)
    private void openScreen(Screen class_4372) {
        if (class_4372 == null) {
            return;
        }
        if (this.keybindControl != null) {
            this.keybindControl.cancelCapture();
        }
        MinecraftClient.getInstance().setScreen(class_4372);
    }

    @Compile(obfuscation=1)
    private void drawPanelSurface(RockstarDrawContext drawContext, Component component) {
        float f = component.x();
        float f2 = component.y();
        float f3 = component.w();
        float f4 = component.h();
        ColorRGBA colorRGBA = this.getPanelHighlightColor();
        drawContext.drawClientRect(f, f2, f3, f4, 1.0f, 0.0f, 2.0f, 12.0f, false, true);
        float f5 = f + this.sidebarWidth - 1.0f;
        float f6 = f + this.sidebarWidth + this.categoryColumnWidth - 1.0f;
        drawContext.drawRect(f5, f2 + 1.0f, 1.0f, f4 - 2.0f, colorRGBA);
        drawContext.drawRect(f6, f2 + 1.0f, 1.0f, f4 - 2.0f, colorRGBA);
        drawContext.drawRect(f6 + 1.0f, f2 + this.headerHeight - 1.0f, f3 - this.sidebarWidth - this.categoryColumnWidth - 1.0f, 1.0f, colorRGBA);
        if (!this.closing) {
            drawContext.drawRoundedBorder(f, f2, f3, f4, 0.5f, WidgetState.uniform(12.0f), colorRGBA);
        }
    }

    @Compile(obfuscation=1)
    private void drawSettingsBackdrop(RockstarDrawContext drawContext, Component component) {
        if (this.closing || this.selectedModule == null || this.settingsList == null) {
            return;
        }
        WidgetBatchRenderer.flushCurrentBatch();
        ShaderRenderer.textureRegistry.render(RenderTextureRegistry.MAIN_SLOT, 1.0f);
        float f = component.x() - 11.0f;
        float f2 = component.x() + component.w() + 11.0f;
        float f3 = component.y() - 10.5f;
        float f4 = component.h() + 10.5f + 22.0f;
        float f5 = 1.5f;
        float f6 = 0.5f;
        float f7 = 1.0f;
        WidgetState widgetState = WidgetState.uniform(0.0f);
        ColorRGBA colorRGBA = ColorPalette.WHITE;
        float f8 = 16.0f;
        float f9 = this.panelX + f8;
        float f10 = this.panelY + f8;
        float f11 = this.panelWidth - 2.0f * f8;
        float f12 = this.panelHeight - 2.0f * f8;
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)f, (float)(f3 - 2.0f), (float)(f2 - f), (float)(f4 + 6.0f));
        drawContext.drawBackdropBlur(f, f3, f2 - f, f4, f5, f6, f7, f9, f10, f11, f12, widgetState, colorRGBA);
        moscow.rockstar.render.state.UiScissorStack.pop();
    }

    @Compile(obfuscation=1)
    private void selectCategory(ModuleCategory moduleCategory) {
        if (this.selectedCategory == moduleCategory) {
            return;
        }
        if (this.keybindControl != null) {
            this.keybindControl.cancelCapture();
        }
        this.selectedCategory = moduleCategory;
        this.keybindTargetModule = null;
        this.visibleModules = List.of();
        this.refreshModuleList(true);
    }

    @Compile(obfuscation=1)
    private void toggleSettingsPanel() {
        if (this.settingPanel != null) {
            this.settingPanel.toggleSettingsPanel();
        }
    }

    @Compile(obfuscation=1)
    private void selectModuleSetting(ModuleContract moduleContract, Setting setting) {
        UiNode uiNode;
        if (moduleContract == null) {
            return;
        }
        if (this.keybindControl != null) {
            this.keybindControl.cancelCapture();
        }
        this.selectedCategory = moduleContract.getCategory();
        this.selectedModule = moduleContract;
        this.visibleModules = List.of();
        this.visibleSettings = List.of();
        this.refreshModuleList(true);
        this.refreshSettingsList(true);
        if (this.settingsScrollContainer != null) {
            this.settingsScrollContainer.resetScroll();
        }
        this.categoryList = uiNode = setting != null ? (UiNode)this.settingComponentCache.get(setting) : (UiNode)this.moduleComponentCache.get(moduleContract);
        this.settingsContent = setting != null ? this.settingsScrollContainer : this.moduleList;
        this.selectionCornerRadius = setting != null ? 7.0f : 3.0f;
        this.settingPanelDeadline = uiNode == null ? 0L : System.currentTimeMillis() + 1600L;
    }

    protected void afterRender(RockstarDrawContext drawContext) {
        this.drawSelectionHighlight(drawContext);
    }

    @Compile(obfuscation=1)
    private void drawSelectionHighlight(RockstarDrawContext drawContext) {
        float f;
        if (this.categoryList == null) {
            return;
        }
        long l = this.settingPanelDeadline - System.currentTimeMillis();
        if (l <= 0L || !this.categoryList.inFlow()) {
            this.categoryList = null;
            return;
        }
        float f2 = this.settingsContent == null ? 0.0f : this.settingsContent.scrollOffset2();
        float f3 = this.categoryList.x();
        float f4 = this.categoryList.y() - f2;
        float f5 = this.categoryList.w();
        float f6 = this.categoryList.h();
        if (this.settingsContent != null) {
            f = this.settingsContent.y();
            float f7 = this.settingsContent.y() + this.settingsContent.h();
            if (f4 < f) {
                f6 -= f - f4;
                f4 = f;
            }
            if (f4 + f6 > f7) {
                f6 = f7 - f4;
            }
        }
        if (f5 <= 0.0f || f6 <= 0.0f) {
            return;
        }
        f = Math.min(1.0f, (float)l / 400.0f);
        drawContext.drawRoundedRect(f3, f4, f5, f6, WidgetState.uniform(this.selectionCornerRadius), ColorPalette.ACCENT_COLOR.mulAlpha(0.18f * f));
    }

    private List<ModuleContract> getModulesForCategory(ModuleCategory moduleCategory) {
        return this.getAvailableModules().stream().filter(moduleContract -> moduleContract.getCategory() == moduleCategory).toList();
    }

    private List<ModuleContract> getAvailableModules() {
        return RockstarClient.create().getModuleRegistry().getModules().stream().filter(ModuleContract::isAvailable).sorted(Comparator.comparing(ModuleContract::getName, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    @Compile(obfuscation=1)
    private void refreshModuleList(boolean bl) {
        IdentityHashMap<ModuleContract, Integer> searchScores;
        List<ModuleContract> list;
        if (this.moduleList == null) {
            return;
        }
        this.refreshModuleRegistry();
        String string = SearchMatcher.normalize(this.searchTextLayout == null ? "" : this.searchTextLayout.getText());
        List<ModuleContract> list2 = list = string.isEmpty() ? this.getModulesForCategory(this.selectedCategory) : this.getAvailableModules();
        if (!bl && ModuleSettingsScreen.hasSameModuleSelection(this.visibleModules, list) && string.equals(this.searchQuery)) {
            return;
        }
        ArrayList<ModuleContract> arrayList = new ArrayList<ModuleContract>(list);
        if (!string.isEmpty()) {
            searchScores = new IdentityHashMap<ModuleContract, Integer>();
            for (ModuleContract moduleContract2 : arrayList) {
                searchScores.put(moduleContract2, SearchMatcher.scoreNormalized(SearchMatcher.normalize(moduleContract2.getName()), string));
            }
            arrayList.removeIf(moduleContract -> searchScores.get(moduleContract) == Integer.MAX_VALUE);
            arrayList.sort(Comparator.comparingInt((ModuleContract moduleContract) -> searchScores.get(moduleContract)).thenComparing(ModuleContract::getName, String.CASE_INSENSITIVE_ORDER));
        }
        this.applyModuleSearchResults(string, arrayList);
        List<UiNode> moduleRows = new ArrayList<UiNode>();
        for (ModuleContract moduleContract2 : arrayList) {
            moduleRows.add(this.moduleComponentCache.computeIfAbsent(moduleContract2, this::createModuleRow));
        }
        this.moduleList.updateChildren(moduleRows);
        this.visibleModules = new ArrayList<ModuleContract>(list);
        this.searchQuery = string;
    }

    @Compile(obfuscation=1)
    private void applyModuleSearchResults(String string, List<ModuleContract> list) {
        ModuleContract moduleContract;
        boolean bl;
        boolean bl2 = bl = !this.searchQuery.isEmpty() && !this.searchQuery.equals("\u0000");
        if (string.isEmpty()) {
            if (bl && this.keybindTargetModule != null && this.selectedModule != this.keybindTargetModule) {
                this.selectModule(this.keybindTargetModule);
            }
            this.keybindTargetModule = null;
            return;
        }
        if (!bl) {
            this.keybindTargetModule = this.selectedModule;
        }
        ModuleContract moduleContract2 = moduleContract = list.isEmpty() ? null : list.getFirst();
        if (this.selectedModule != moduleContract) {
            this.selectModule(moduleContract);
        }
    }

    @Compile(obfuscation=1)
    private Component createModuleRow(ModuleContract moduleContract) {
        TextComponent textComponent2 = new TextComponent().fill().fade().text(Font.MEDIUM.metrics(7.0f), () -> this.getModuleDisplayText(moduleContract), textComponent -> this.getOverlayAccentColor().mix(ColorPalette.ACCENT_COLOR, 0.5f * textComponent.sig("enabled")).mulAlpha(0.6f + 0.3f * textComponent.sig("enabled") + 0.1f * textComponent.sig("selected"))).bind("enabled", moduleContract::isEnabled, CATEGORY_HOVER_MOTION).bind("selected", () -> this.selectedModule == moduleContract, CATEGORY_HOVER_MOTION).interactive(false);
        Component component2 = new Component().horizontal().alignment(Alignment.CENTER).height(15.0f).fillWidth().padding(Insets.symmetric(0.0f, 5.0f)).cornerRadius(3.0f).background(component -> ModuleSettingsScreen.interpolateColor(ColorPalette.getPanelBackgroundColor().mulAlpha(0.4f), ColorPalette.ACCENT_COLOR, 0.08f * component.sig("enabled") + 0.018f * component.sig("selected") + 0.025f * component.hover())).bind("enabled", moduleContract::isEnabled, CATEGORY_HOVER_MOTION).bind("selected", () -> this.selectedModule == moduleContract, CATEGORY_HOVER_MOTION).cursor(Cursor.HAND).add(textComponent2).onClick((pointerAction, f, f2) -> this.handleModulePointerAction(moduleContract, pointerAction));
        return component2;
    }

    private String getModuleDisplayText(ModuleContract moduleContract) {
        if (this.keybindTargetModule != moduleContract) {
            return moduleContract.getName();
        }
        int n = moscow.rockstar.ui.input.KeyBindingUtil.currentModifiers();
        if (n != 0) {
            return Localization.translate("key") + ": " + moscow.rockstar.ui.input.KeyBindingUtil.modifierPrefix(n) + "...";
        }
        return moduleContract.getKeyBind() == -1 ? Localization.translate("menu.binding") : Localization.translate("key") + ": " + moscow.rockstar.ui.input.KeyDisplayFormatter.formatKey(moduleContract.getKeyBind());
    }

    private void handleModulePointerAction(ModuleContract moduleContract, PointerAction pointerAction) {
        switch (pointerAction) {
            case LEFT_CLICK: {
                moduleContract.toggle();
                break;
            }
            case RIGHT_CLICK: {
                if (this.selectedModule == moduleContract) break;
                this.selectModule(moduleContract);
                break;
            }
            case MIDDLE_CLICK: {
                this.keybindTargetModule = this.keybindTargetModule == moduleContract ? null : moduleContract;
            }
        }
    }

    private void selectModule(ModuleContract moduleContract) {
        if (this.keybindControl != null) {
            this.keybindControl.cancelCapture();
        }
        this.selectedModule = moduleContract;
        this.keybindTargetModule = null;
        this.visibleSettings = List.of();
        this.refreshSettingsList(true);
        if (this.settingsScrollContainer != null) {
            this.settingsScrollContainer.resetScroll();
        }
    }

    @Compile(obfuscation=1)
    public void refreshModuleRegistry() {
        int n = ModuleRegistry.getRegistryInstanceCount();
        if (n == this.moduleRegistryVersion) {
            return;
        }
        this.moduleRegistryVersion = n;
        List<ModuleContract> list = RockstarClient.create().getModuleRegistry().getModules();
        if (this.keybindTargetModule != null && !ModuleSettingsScreen.containsModuleIdentity(list, this.keybindTargetModule)) {
            this.keybindTargetModule = null;
        }
        if (this.keybindTargetModule != null && !ModuleSettingsScreen.containsModuleIdentity(list, this.keybindTargetModule)) {
            this.keybindTargetModule = ModuleSettingsScreen.findReplacementModule(list, this.keybindTargetModule);
        }
        IdentityHashMap<ModuleContract, Boolean> identityHashMap = new IdentityHashMap<ModuleContract, Boolean>();
        for (ModuleContract object : list) {
            identityHashMap.put(object, Boolean.TRUE);
        }
        this.moduleComponentCache.keySet().removeIf(moduleContract -> !identityHashMap.containsKey(moduleContract));
        if (this.selectedModule == null || identityHashMap.containsKey(this.selectedModule)) {
            return;
        }
        for (Setting setting : this.visibleSettings) {
            this.settingComponentCache.remove(setting);
        }
        this.selectModule(ModuleSettingsScreen.findReplacementModule(list, this.selectedModule));
    }

    private static boolean containsModuleIdentity(List<ModuleContract> list, ModuleContract moduleContract) {
        for (ModuleContract moduleContract2 : list) {
            if (moduleContract2 != moduleContract) continue;
            return true;
        }
        return false;
    }

    private static ModuleContract findReplacementModule(List<ModuleContract> list, ModuleContract moduleContract) {
        for (ModuleContract moduleContract2 : list) {
            if (moduleContract2.getCategory() != moduleContract.getCategory() || !moduleContract2.getName().equals(moduleContract.getName())) continue;
            return moduleContract2;
        }
        return null;
    }

    @Compile(obfuscation=1)
    private void refreshSettingsList(boolean bl) {
        List<Setting> settings;
        if (this.settingsList == null) {
            return;
        }
        settings = this.selectedModule == null ? List.of() : this.selectedModule.getSettings();
        if (!bl && ModuleSettingsScreen.hasSameModuleSelection(this.visibleSettings, settings)) {
            return;
        }
        if (!this.visibleSettings.isEmpty()) {
            IdentityHashMap<Setting, Boolean> activeSettings = new IdentityHashMap<Setting, Boolean>();
            for (Setting setting : settings) {
                activeSettings.put(setting, Boolean.TRUE);
            }
            for (Setting setting : this.visibleSettings) {
                if (activeSettings.containsKey(setting)) continue;
                this.settingComponentCache.remove(setting);
            }
        }
        List<UiNode> rows = new ArrayList<UiNode>();
        List<UiNode> leftColumn = new ArrayList<UiNode>();
        List<UiNode> rightColumn = new ArrayList<UiNode>();
        List<Setting> columnSettings = new ArrayList<Setting>();
        int n = 0;
        for (Setting setting : settings) {
            if (setting instanceof TextLabelSetting) {
                this.addSettingsColumns(rows, leftColumn, rightColumn, columnSettings);
                n = 0;
                rows.add(this.settingComponentCache.computeIfAbsent(setting, this::createSettingComponent));
                continue;
            }
            (n++ % 2 == 0 ? leftColumn : rightColumn).add(this.settingComponentCache.computeIfAbsent(setting, this::createSettingComponent));
            columnSettings.add(setting);
        }
        this.addSettingsColumns(rows, leftColumn, rightColumn, columnSettings);
        this.settingsList.replaceChildren(rows);
        this.visibleSettings = new ArrayList<Setting>(settings);
        ShaderRenderer.textureRegistry.invalidateAll();
    }

    @Compile(obfuscation=1)
    private void addSettingsColumns(List<UiNode> list, List<UiNode> list2, List<UiNode> list3, List<Setting> list4) {
        if (list2.isEmpty() && list3.isEmpty()) {
            return;
        }
        List<Setting> list5 = List.copyOf(list4);
        Component component = new Component().vertical().gap(5.0f).width(this.contentColumnWidth).addAll(list2);
        Component component2 = new Component().vertical().gap(5.0f).width(this.contentColumnWidth).addAll(list3);
        component.snapSize();
        component2.snapSize();
        Component component3 = new Component().horizontal().gap(5.0f).alignment(Alignment.START).fillWidth().visibleWhen(() -> ModuleSettingsScreen.containsValidSetting(list5), Easing.easeOutQuart, 220L).collapse().add(component).add(component2);
        component3.snapSize();
        list.add(component3);
        list2.clear();
        list3.clear();
        list4.clear();
    }

    private static boolean containsValidSetting(List<Setting> list) {
        for (Setting setting : list) {
            if (!setting.hasValidSettingValue()) continue;
            return true;
        }
        return false;
    }

    private Component createSettingComponent(Setting setting) {
        return setting instanceof TextLabelSetting ? this.wrapLabelSetting(setting) : this.wrapEditableSetting(setting);
    }

    /**
     * ORIGINAL: rockstar/ilIlil/IiiIiiIIi#i (Lrockstar/ilIlil/IIiiiIIII;)Lrockstar/ilIlil/iii;.
     * The setting's own component is always stretched to the card width and inset by
     * 9px horizontally; without this the row measures at its natural width and its
     * label/value text runs past the card edge.
     */
    private static Component createRawSettingComponent(Setting setting) {
        Component component = setting.buildComponent();
        if (component == null) {
            // ORIGINAL falls back to `new IiiIiiIII(setting)`, an adapter around the
            // legacy SettingWidget-based renderer. Every Setting here overrides
            // buildComponent(), so this branch is unreachable in practice.
            return new Component();
        }
        return component.fillWidth().padding(0.0f, 9.0f);
    }

    @Compile(obfuscation=1)
    private Component wrapLabelSetting(Setting setting) {
        Component component = ModuleSettingsScreen.createRawSettingComponent(setting);
        component.padding(Insets.NONE);
        component.snapSize();
        Component component2 = new Component().vertical().fillWidth().visibleWhen(setting::hasValidSettingValue, Easing.easeOutQuart, 220L).collapse().add(component);
        component2.snapSize();
        return component2;
    }

    @Compile(obfuscation=1)
    private Component wrapEditableSetting(Setting setting) {
        Component component2 = ModuleSettingsScreen.createRawSettingComponent(setting);
        component2.snapSize();
        Component component3 = new Component().vertical().fillWidth().padding(Insets.vertical(4.0f)).cornerRadius(7.0f).background(component -> ColorPalette.getPanelBackgroundColor().mulAlpha(0.4f)).visibleWhen(setting::hasValidSettingValue, Easing.easeOutQuart, 220L).collapse().add(component2);
        component3.snapSize();
        return component3;
    }

    private String getSelectedModuleTitle() {
        return this.selectedModule == null ? "" : Localization.translateFormatted("menu.modern.settings", this.selectedModule.getName());
    }

    private String getSelectedModuleDescription() {
        return this.selectedModule == null ? "" : this.selectedModule.getDescription();
    }

    private String getProfileUsername() {
        return MinecraftClient.getInstance().getSession() == null ? "" : MinecraftClient.getInstance().getSession().getUsername();
    }

    private String getProfileTag() {
        return "LEEK";
    }

    private int getSelectedModuleKeybind() {
        return this.selectedModule == null ? -1 : this.selectedModule.getKeyBind();
    }

    private void setSelectedModuleKeybind(int n) {
        if (this.selectedModule == null) {
            return;
        }
        this.selectedModule.setKeyBind(n);
        this.resetSelectionState();
    }

    @Override
    @Compile(obfuscation=1)
    public void render(RockstarDrawContext drawContext) {
        boolean bl;
        float f;
        this.updatePanelAnimation();
        this.updateHoverSpotlight();
        InventoryMove.resetInputState();
        if (!this.closing) {
            this.refreshModuleList(false);
            this.refreshSettingsList(false);
        }
        if (this.settingsScrollContainer != null && (f = this.settingsScrollContainer.scrollOffset2()) != this.settingsScrollOffset) {
            this.settingsScrollOffset = f;
            ShaderRenderer.textureRegistry.invalidateAll();
        }
        if (System.currentTimeMillis() - this.selectionAnimationStart < 400L) {
            ShaderRenderer.textureRegistry.invalidateAll();
        }
        if (this.draggedComponent != null) {
            this.panelX = this.draggedComponent.x();
            this.panelY = this.draggedComponent.y();
            if (this.panelX != this.lastPanelX || this.panelY != this.lastPanelY) {
                this.lastPanelX = this.panelX;
                this.lastPanelY = this.panelY;
            ShaderRenderer.textureRegistry.invalidateAll();
            }
        }
        f = this.closing ? 1.0f : 0.7f + 0.3f * this.getSelectionAnimationProgress();
        boolean bl2 = bl = Math.abs(f - 1.0f) > 1.0E-4f;
        if (bl) {
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate((float)this.width / 2.0f, (float)this.height / 2.0f, 0.0f);
            drawContext.getMatrices().scale(f, f, 1.0f);
            drawContext.getMatrices().translate((float)(-this.width) / 2.0f, (float)(-this.height) / 2.0f, 0.0f);
        }
        WidgetBatchRenderer.flushCurrentBatch();
        MinecraftClient client = MinecraftClient.getInstance();
        boolean capture = this.closing && (client == null || client.currentScreen == null);
        RockstarClient.create().getEventBus().post(new MenuRenderEvent(
            drawContext, drawContext.tickDelta(), this.getOverlayName(),
            this.getTransitionProgress(), capture
        ));
        WidgetBatchRenderer.flushCurrentBatch();
        super.render(drawContext);
        WidgetBatchRenderer.flushCurrentBatch();
        RockstarClient.create().getEventBus().post(new PostMenuRenderEvent(
            drawContext, drawContext.tickDelta(), this.getOverlayName(),
            this.getTransitionProgress(), capture
        ));
        WidgetBatchRenderer.flushCurrentBatch();
        if (bl) {
            drawContext.getMatrices().pop();
        }
    }

    @Override
    public String getOverlayName() {
        return "modern";
    }

    @Override
    public float getOpenProgress() {
        return this.getSelectionAnimationProgress();
    }

    @Override
    public float getClosingProgress() {
        return this.closing ? this.getAnimationDelta() : 0.0f;
    }

    @Override
    public boolean isClosing() {
        return this.closing;
    }

    @Override
    public float getContentAlpha() {
        return this.contentAlpha;
    }

    @Override
    public float getOverlayScale() {
        return this.closing ? 1.0f : 0.7f + 0.3f * this.getSelectionAnimationProgress();
    }

    @Override
    public List<OverlayElement.OverlayBounds> getOverlayBounds() {
        return List.of(new OverlayElement.OverlayBounds("window", this.panelX, this.panelY, this.panelWidth, this.panelHeight));
    }

    public static ModuleSettingsScreen getInstance() {
        return INSTANCE;
    }

    /**
     * Captures the closing screen into the framebuffer used by the original
     * world-space transition. This runs after the normal screen has been
     * removed, while the screen object is retained in INSTANCE.
     */
    public static void renderClosingHud(HudRenderEvent hudRenderEvent) {
        ModuleSettingsScreen screen = INSTANCE;
        if (screen == null) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (screen.getAnimationDelta() >= 1.0f) {
            INSTANCE = null;
            panelOpen = false;
            settingsPanelOpen = false;
            return;
        }
        if (client.currentScreen != null) {
            return;
        }
        if (!panelOpen) {
            uiRenderTarget.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            uiRenderTarget.beginPass(true);
            WidgetBatchRenderer.textureRenderingActive = true;
            try {
                screen.render(RockstarDrawContext.create(hudRenderEvent.getContext(), -1, -1, hudRenderEvent.getTickDelta()));
            }
            finally {
                WidgetBatchRenderer.textureRenderingActive = false;
                uiRenderTarget.endPass();
            }
            panelOpen = true;
        }
        if (!settingsPanelOpen) {
            ModuleSettingsScreen.onHudRender(hudRenderEvent);
        }
    }

    /**
     * Draws the captured screen as the original camera-facing 3D transition
     * quad. Keeping this on the render-world event is important: drawing the
     * closing screen directly from the HUD makes it a flat 2D overlay.
     */
    public static void renderClosingWorld(Render3DEvent render3DEvent) {
        ModuleSettingsScreen screen = INSTANCE;
        if (screen == null || !panelOpen || screen.cameraPosition == null
            || screen.upDirection == null || screen.rightDirection == null) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        float closingProgress = screen.getAnimationDelta();
        float firstPhaseProgress = Math.min(1.0f, closingProgress / 0.4f);
        float easedProgress = Easing.easeInBack.ease(firstPhaseProgress, 0.0f, 1.0f, 1.0f);
        float opacity = 1.0f - easedProgress;
        float panelOpacity = opacity;
        float fovScale = (float)(Math.tan(Math.toRadians(((Integer)client.options.getFov().getValue()).intValue()) / 2.0) / Math.tan(Math.toRadians(110.0) / 2.0));
        float quadHeight = (3.3f + opacity) * fovScale;
        float quadWidth = quadHeight * ((float)client.getWindow().getFramebufferWidth() / (float)client.getWindow().getFramebufferHeight());
        Vec3d cameraPosition = render3DEvent.getCamera().getPos();
        Vec3d panelCenter = screen.cameraPosition;
        // closeScreen() stores the camera basis in the same order as the
        // original: upDirection is the horizontal in-plane axis and
        // rightDirection is the vertical in-plane axis.
        Vec3d halfWidth = screen.upDirection.multiply((double)quadWidth / 2.0);
        Vec3d halfHeight = screen.rightDirection.multiply((double)quadHeight / 2.0);
        Vec3d firstCorner = panelCenter.subtract(halfWidth).add(halfHeight).subtract(cameraPosition);
        Vec3d secondCorner = panelCenter.subtract(halfWidth).subtract(halfHeight).subtract(cameraPosition);
        Vec3d thirdCorner = panelCenter.add(halfWidth).subtract(halfHeight).subtract(cameraPosition);
        Vec3d fourthCorner = panelCenter.add(halfWidth).add(halfHeight).subtract(cameraPosition);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, uiRenderTarget.getColorAttachment());
        Matrix4f matrix = render3DEvent.getMatrices().peek().getPositionMatrix();
        int color = ColorRGBA.WHITE.withAlpha(255.0f * panelOpacity).getRGB();
        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(matrix, (float)firstCorner.x, (float)firstCorner.y, (float)firstCorner.z).texture(0.0f, 1.0f).color(color);
        buffer.vertex(matrix, (float)secondCorner.x, (float)secondCorner.y, (float)secondCorner.z).texture(0.0f, 0.0f).color(color);
        buffer.vertex(matrix, (float)thirdCorner.x, (float)thirdCorner.y, (float)thirdCorner.z).texture(1.0f, 0.0f).color(color);
        buffer.vertex(matrix, (float)fourthCorner.x, (float)fourthCorner.y, (float)fourthCorner.z).texture(1.0f, 1.0f).color(color);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        settingsPanelOpen = true;
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (closingProgress >= 1.0f) {
            INSTANCE = null;
            panelOpen = false;
        }
    }

    private float getSelectionAnimationProgress() {
        float f = Math.min(1.0f, Math.max(0.0f, (float)(System.currentTimeMillis() - this.selectionAnimationStart) / 300.0f));
        return Easing.easeOutBack.ease(f, 0.0f, 1.0f, 1.0f);
    }

    private void updatePanelAnimation() {
        long l = System.currentTimeMillis();
        if (this.closing) {
            this.settingsRevealProgress = 0.0f;
            this.lastFrameTime = l;
            this.contentAlpha = 1.0f;
            return;
        }
        float f = this.lastFrameTime == 0L ? 16.0f : Math.min(64.0f, (float)(l - this.lastFrameTime));
        this.lastFrameTime = l;
        Menu menu = RockstarClient.create().getModuleRegistry().getModule(Menu.class);
        int n = menu == null || menu.getHideKeySetting() == null ? -1 : menu.getHideKeySetting().getValue();
        float f2 = ModuleSettingsScreen.isHideKeyPressed(n) ? 1.0f : 0.0f;
        float f3 = f / 300.0f;
        if (this.settingsRevealProgress < f2) {
            this.settingsRevealProgress = Math.min(f2, this.settingsRevealProgress + f3);
        } else if (this.settingsRevealProgress > f2) {
            this.settingsRevealProgress = Math.max(f2, this.settingsRevealProgress - f3);
        }
        this.contentAlpha = 1.0f - Easing.easeInOutCubicBezier.ease(this.settingsRevealProgress, 0.0f, 1.0f, 1.0f);
    }

    private void updateHoverSpotlight() {
        boolean bl;
        if (this.closing || this.contentAlpha >= 0.999f) {
            UiNode.spotlight(null);
            return;
        }
        long l = MinecraftClient.getInstance().getWindow().getHandle();
        boolean bl2 = bl = GLFW.glfwGetMouseButton((long)l, (int)0) == 1;
        if (bl && UiNode.spotlight() != null) {
            return;
        }
        Component component = null;
        for (Map.Entry<Setting, Component> entry : this.settingComponentCache.entrySet()) {
            Component component2 = entry.getValue();
            if (!component2.inFlow() || !component2.hovered() || !ModuleSettingsScreen.isSpotlightSetting(entry.getKey())) continue;
            component = component2;
            break;
        }
        UiNode.spotlight(component);
    }

    /**
     * ORIGINAL: rockstar/ilIlil/IiiIiiIIi#I (Lrockstar/ilIlil/IIiiiIIII;)Z.
     * The set of setting types the hover spotlight applies to - NOT a validity check.
     */
    private static boolean isSpotlightSetting(Setting setting) {
        return setting instanceof moscow.rockstar.settings.BooleanSetting || setting instanceof moscow.rockstar.settings.NumberSetting || setting instanceof moscow.rockstar.settings.RangeSetting || setting instanceof moscow.rockstar.settings.MultiBooleanSetting || setting instanceof moscow.rockstar.settings.ModeSetting;
    }

    private static boolean isHideKeyPressed(int n) {
        return moscow.rockstar.ui.input.KeyBindingUtil.isPressed(n);
    }

    public void tick() {
        InventoryMove.resetInputState();
        super.tick();
    }

    @Override
    @Compile(obfuscation=1)
    public void onMouseClicked(double d, double d2, PointerAction pointerAction) {
        if (KeyBindingControl.handlePointerAction(pointerAction)) {
            this.resetSelectionState();
            return;
        }
        boolean bl = this.overlays.stream().anyMatch(uiNode -> uiNode.alive() && uiNode.contains((float)d, (float)d2));
        if (this.settingPanel != null && !bl) {
            this.settingPanel.handleOutsideClick((float)d, (float)d2);
        }
        if (this.keybindTargetModule != null) {
            if (pointerAction == PointerAction.LEFT_CLICK) {
                this.keybindTargetModule = null;
                return;
            }
            if (pointerAction != PointerAction.MIDDLE_CLICK) {
                // ORIGINAL rockstar/ilIlil/IiiIiIiiI#onMouseClicked emits
                //   PointerAction.getButtonCode() -> KeyBindingUtil.withCurrentModifiers(int)
                // encode(button, 0) hard-zeroes the modifier nibble.
                this.keybindTargetModule.setKeyBind(moscow.rockstar.ui.input.KeyBindingUtil.withCurrentModifiers(pointerAction.getButtonCode()));
                this.keybindTargetModule = null;
                this.resetSelectionState();
                return;
            }
        }
        super.onMouseClicked(d, d2, pointerAction);
    }

    public boolean keyReleased(int n, int n2, int n3) {
        int n4;
        if (this.keybindTargetModule != null && (n4 = moscow.rockstar.ui.input.KeyBindingUtil.encodeKeyRelease(n, n3)) != Integer.MIN_VALUE) {
            this.keybindTargetModule.setKeyBind(n4);
            this.keybindTargetModule = null;
            this.resetSelectionState();
            return true;
        }
        return super.keyReleased(n, n2, n3);
    }

    @Override
    public void onMouseReleased(double d, double d2, PointerAction pointerAction) {
        boolean bl;
        boolean bl2 = bl = pointerAction == PointerAction.LEFT_CLICK && this.draggedComponent != null && this.draggedComponent.dragging();
        if (this.searchTextLayout != null) {
            this.searchTextLayout.mouseReleased(d, d2, pointerAction);
        }
        super.onMouseReleased(d, d2, pointerAction);
        if (bl) {
            this.releasePointerCapture();
        }
    }

    private void releasePointerCapture() {
        float f;
        float f2;
        float f3 = this.draggedComponent.x();
        float f4 = this.draggedComponent.y();
        float f5 = this.draggedComponent.w();
        float f6 = this.draggedComponent.h();
        float f7 = Math.max(0.0f, f3);
        float f8 = Math.max(0.0f, f4);
        float f9 = Math.min((float)this.width, f3 + f5);
        float f10 = Math.min((float)this.height, f4 + f6);
        float f11 = Math.max(0.0f, f9 - f7);
        float f12 = 1.0f - f11 * (f2 = Math.max(0.0f, f10 - f8)) / (f = Math.max(1.0f, f5 * f6));
        if (f12 < 0.35f) {
            return;
        }
        float f13 = Math.round(((float)this.width - f5) / 2.0f);
        float f14 = Math.round(((float)this.height - f6) / 2.0f);
        this.draggedComponent.at(f13, f14);
    }

    @Compile(obfuscation=1)
    public boolean keyPressed(int n, int n2, int n3) {
        if (this.keybindTargetModule == null && !KeyBindingControl.isControlActive()) {
            if (Screen.hasControlDown() && n == 90 && SettingSnapshotCache.isCollectionCacheReady()) {
                return true;
            }
            if (Screen.hasControlDown() && n == 89 && SettingSnapshotCache.isCollectionProcessorCacheTargetReady()) {
                return true;
            }
        }
        if (this.keybindTargetModule != null) {
            if (n == 256 || n == 261) {
                this.keybindTargetModule.setKeyBind(-1);
                this.keybindTargetModule = null;
                this.resetSelectionState();
                return true;
            }
            int n4 = moscow.rockstar.ui.input.KeyBindingUtil.encodeKeyPress(n, n3);
            if (n4 == Integer.MIN_VALUE) {
                return true;
            }
            this.keybindTargetModule.setKeyBind(n4);
            this.keybindTargetModule = null;
            this.resetSelectionState();
            return true;
        }
        if (this.settingPanel != null && !this.settingPanel.isSearchOpen() && Screen.hasControlDown() && n == 70) {
            this.settingPanel.openKeybindSearch();
            return true;
        }
        if (super.keyPressed(n, n2, n3)) {
            return true;
        }
        if (Menu.isMenuIndexValid(n)) {
            this.close();
            return true;
        }
        return false;
    }

    @Compile(obfuscation=1)
    public void close() {
        Menu menu;
        Sounds sounds;
        if (this.closing) {
            return;
        }
        if (this.searchTextLayout != null) {
            this.searchTextLayout.setFocused(false);
        }
        if ((sounds = RockstarClient.create().getModuleRegistry().getModule(Sounds.class)) != null && sounds.isEnabled()) {
            SoundEffectPlayer.playClickGuiOpen(sounds.getVolume());
        }
        if ((menu = RockstarClient.create().getModuleRegistry().getModule(Menu.class)) != null && menu.isEnabled()) {
            menu.disable();
        }
        super.close();
    }

    public void removed() {
        this.closeScreen();
        super.removed();
    }

    @Compile(obfuscation=1)
    public void closeScreen() {
        if (this.closing) {
            return;
        }
        this.closing = true;
        UiNode.spotlight(null);
        this.resetSelectionState();
        this.searchAnimationStart = System.currentTimeMillis();
        panelOpen = false;
        settingsPanelOpen = false;
        MinecraftClient client = MinecraftClient.getInstance();
        Camera class_41842 = client.gameRenderer.getCamera();
        if (class_41842 != null && client.player != null) {
            Vec3d VanillaChestLootTableGenerator;
            double d = Math.toRadians(class_41842.getYaw());
            double d2 = Math.toRadians(class_41842.getPitch());
            this.viewDirection = VanillaChestLootTableGenerator = new Vec3d(-Math.sin(d) * Math.cos(d2), -Math.sin(d2), Math.cos(d) * Math.cos(d2)).normalize();
            this.upDirection = VanillaChestLootTableGenerator.crossProduct(new Vec3d(0.0, 1.0, 0.0)).normalize();
            this.rightDirection = this.upDirection.crossProduct(VanillaChestLootTableGenerator).normalize();
            this.cameraPosition = class_41842.getPos().add(VanillaChestLootTableGenerator.multiply(1.5));
        }
        INSTANCE = this;
    }

    float getAnimationDelta() {
        return Math.min(1.0f, Math.max(0.0f, (float)(System.currentTimeMillis() - this.searchAnimationStart) / 1600.0f));
    }

    static void onHudRender(HudRenderEvent hudRenderEvent) {
        MinecraftClient client = MinecraftClient.getInstance();
        float f = client.getWindow().getScaledWidth();
        float f2 = client.getWindow().getScaledHeight();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (int)uiRenderTarget.getColorAttachment());
        Matrix4f matrix4f = hudRenderEvent.getContext().getMatrices().peek().getPositionMatrix();
        int n = ColorRGBA.WHITE.getRGB();
        BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        class_2872.vertex(matrix4f, 0.0f, 0.0f, 0.0f).texture(0.0f, 1.0f).color(n);
        class_2872.vertex(matrix4f, 0.0f, f2, 0.0f).texture(0.0f, 0.0f).color(n);
        class_2872.vertex(matrix4f, f, f2, 0.0f).texture(1.0f, 0.0f).color(n);
        class_2872.vertex(matrix4f, f, 0.0f, 0.0f).texture(1.0f, 1.0f).color(n);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    private void resetSelectionState() {
        moscow.rockstar.modules.config.ModuleConfigurationStore.saveConfiguration();
    }

    private ColorRGBA getOverlayAccentColor() {
        return ColorPalette.getPrimaryTextColor();
    }

    private ColorRGBA getPanelBackgroundColor() {
        return ColorPalette.getPanelColor().withAlpha(102.0f);
    }

    private ColorRGBA getPanelBorderColor() {
        return this.getPanelBackgroundColor();
    }

    private ColorRGBA getPanelHighlightColor() {
        return ColorPalette.BORDER_COLOR.withAlpha(89.25f);
    }

    private void onScrollBarChanged(ScrollBar scrollBar2) {
        scrollBar2.offset(-3.0f).padding(2.0f).thickness(2.0f).minThumbLength(18.0f).cornerRadius(1.0f).hideDelay(1100.0f).thumbColor(scrollBar -> this.getOverlayAccentColor().withAlpha(255.0f * (0.28f + 0.24f * scrollBar.hoverProgress() + 0.28f * scrollBar.dragProgress())));
    }

    private static ColorRGBA interpolateColor(ColorRGBA colorRGBA, ColorRGBA colorRGBA2, float f) {
        return colorRGBA.mix(colorRGBA2.withAlpha(colorRGBA.getAlpha()), f);
    }

    private static boolean hasSameModuleSelection(List<?> list, List<?> list2) {
        if (list.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i) == list2.get(i)) continue;
            return false;
        }
        return true;
    }

    private static String getLocalizedText(String string) {
        if (string == null || string.isBlank()) {
            return "";
        }
        String string2 = string.toLowerCase();
        return Character.toUpperCase(string2.charAt(0)) + string2.substring(1);
    }

    public boolean shouldPause() {
        return false;
    }

    public void renderBackground(DrawContext ServerConfigException, int n, int n2, float f) {
    }

}
