/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Window
 *  net.minecraft.Identifier
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.ui.settings;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import moscow.rockstar.api.data.SettingDataStore;
import moscow.rockstar.api.settings.SettingEntry;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.items.assist.AssistItemProvider;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.modules.config.ModuleConfigurationStore;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.other.assist.Assist;
import moscow.rockstar.network.http.client.JavaNetHttpClient;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.target.RenderTextureRegistry;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.animation.AnimatedValue;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.animation.Transition;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.layout.ItemGrid;
import moscow.rockstar.ui.layout.JustifyContent;
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.localization.Language;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.search.SearchMatcher;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.TextComponent;
import moscow.rockstar.ui.text.TextInputField;
import moscow.rockstar.ui.theme.ColorTheme;
import moscow.rockstar.ui.widgets.settings.KeyBindingControl;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.utility.render.ColorRGBA;

public class SettingPanel
extends Component
implements SettingOwner {
    private static final float PANEL_WIDTH = 94.0f;
    private static final float PANEL_HEIGHT = 24.0f;
    private static final float KEYBIND_PANEL_WIDTH = 207.0f;
    private static final float KEYBIND_PANEL_HEIGHT = 24.0f;
    private static final float CONTROL_GAP = 4.0f;
    private static final float KEYBIND_ROW_HEIGHT = 18.0f;
    private static final float TEXT_PADDING = 10.0f;
    private static final float KEYBIND_LIST_MAX_HEIGHT = 150.0f;
    private static final float PANEL_LEFT_PADDING = 6.0f;
    private static final float PANEL_RIGHT_PADDING = 6.0f;
    private static final float SETTINGS_COLUMN_WIDTH = 115.0f;
    private static final float BORDER_WIDTH = 1.0f;
    private static final float SHADER_INSET = 16.0f;
    private static final long VISIBILITY_TIMEOUT_MILLIS = 400L;
    private static final FontMetrics HEADER_FONT = Font.MEDIUM.metrics(9.0f);
    private final AnimatedValue selectedRowAnimation = new AnimatedValue(Motion.resolveMotionMotionFromFloatAndFloat(300.0f, 28.0f));
    private final UiNode anchorNode;
    private final BiConsumer<ModuleContract, Setting> settingSelectionCallback;
    private final UiNode mainHeaderNode;
    private final UiNode mainHeaderActionNode;
    private final UiNode searchButton;
    private final UiNode searchField;
    private final UiNode clearSearchButton;
    private final Component keybindListContainer;
    private final List<Setting> settings = new ArrayList<Setting>();
    private final ModeSetting languageSetting;
    private final ColorSetting accentColorSetting;
    private final BooleanSetting autoSaveSetting;
    private final Component settingsContainer;
    private boolean settingsPanelOpen;
    private boolean embeddedSettingsMode;
    private UiNode embeddedSettingsNode;
    private int languageIndex;
    private long accentColorChangedAtMillis;
    private long visibilityDeadlineMillis;
    private static final float KEYBIND_WINDOW_WIDTH = 207.0f;
    private static final float KEYBIND_HEADER_HEIGHT = 14.0f;
    private static final float KEYBIND_ITEM_HEIGHT = 18.0f;
    private static final float KEYBIND_WINDOW_MAX_HEIGHT = 150.0f;
    private static final float KEYBIND_LIST_PADDING = 7.0f;
    private static final float NO_OFFSET = 0.0f;
    private static final float SEARCH_FONT_SIZE = 9.0f;
    private static final float SEARCH_TEXT_PADDING = 2.0f;
    private static final float KEYBIND_ROW_GAP = 6.0f;
    private static final long KEYBIND_ANIMATION_MILLIS = 300L;
    private static final Transition settingsPanelTransition = (f, uiNode, state) -> {
        state.progress = Math.min(1.0f, Math.max(0.0f, f));
        state.scale = 0.7f + 0.3f * f;
    };
    private final UiNode keybindHeaderNode;
    private final UiNode keybindHeaderActionNode;
    private final Component settingsView;
    private final Component keybindView;
    private boolean keybindPanelOpen;
    private final List<KeybindEntry> keybindEntries = new ArrayList<KeybindEntry>();
    private TextInputField keybindSearchLayout;
    private boolean keybindSearchOpen;
    private long keybindSearchOpenedAtMillis;
    private boolean keybindSearchReady;
    private String keybindSearchText = null;
    private int selectedKeybindIndex;
    private UiNode embeddedKeybindNode;
    private FontMetrics embeddedKeybindFont;
    private final List<SettingSearchEntry> searchResults = new ArrayList<SettingSearchEntry>();
    private static final String QWERTY_LAYOUT = "qwertyuiop[]asdfghjkl;'zxcvbnm,./";
    private static final String RUSSIAN_KEYBOARD_LAYOUT = "\u0439\u0446\u0443\u043a\u0435\u043d\u0433\u0448\u0449\u0437\u0445\u044a\u0444\u044b\u0432\u0430\u043f\u0440\u043e\u043b\u0434\u0436\u044d\u044f\u0447\u0441\u043c\u0438\u0442\u044c\u0431\u044e.";

    private static String getProfileSubtitle() {
        return "LEEK";
    }

    private static String getLocalUsername() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.getSession() == null ? "" : client.getSession().getUsername();
    }

    public SettingPanel(UiNode uiNode2, BiConsumer<ModuleContract, Setting> biConsumer) {
        this.anchorNode = uiNode2;
        this.settingSelectionCallback = biConsumer;
        TextComponent textComponent2 = new TextComponent().size(12.0f, 12.0f).interactive(false).paint((drawContext, textComponent) -> {
            float f = textComponent.w() / 2.0f;
            drawContext.drawRoundedTexture(SettingPanel.getAvatarTexture(), textComponent.x(), textComponent.y(), textComponent.w(), textComponent.h(), WidgetState.uniform(f), ColorPalette.WHITE);
        });
        Component component2 = new Component().vertical().gap(2.0f).alignment(Alignment.START).interactive(false).add(new TextComponent().text(Font.MEDIUM.metrics(7.0f), SettingPanel::getLocalUsername, textComponent -> ColorPalette.PRIMARY_TEXT_COLOR).interactive(false)).add(new TextComponent().text(Font.REGULAR.metrics(6.0f), SettingPanel::getProfileSubtitle, textComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.5f)).interactive(false));
        this.mainHeaderNode = new Component().horizontal().alignment(Alignment.CENTER).gap(4.0f).fillWidth().add(textComponent2).add(component2);
        this.mainHeaderActionNode = this.createIconButton("setting", this::toggleSettingsPanelVisibility);
        this.searchButton = this.createIconButton("search", () -> this.setSearchOpen(true));
        this.clearSearchButton = this.createIconButton("xmark", () -> this.setSearchOpen(false));
        this.searchField = new TextComponent().fillWidth().height(24.0f).cursor(Cursor.IBEAM).onClick((PointerAction pointerAction, float f, float f2) -> this.getSearchEditor().mouseClicked(f, f2, pointerAction)).paint((drawContext, textComponent) -> {
            TextInputField textInputField = this.getSearchEditor();
            textInputField.setBounds(textComponent.x() - 4.0f, textComponent.y(), textComponent.w() + 4.0f, textComponent.h());
            textInputField.setTextColor(ColorPalette.PRIMARY_TEXT_COLOR);
            textInputField.setOpacity(1.0f);
            textInputField.render(drawContext);
        });
        this.horizontal().alignment(Alignment.CENTER).overflowMode(JustifyContent.START).gap(6.0f).padding(Insets.of(0.0f, 8.0f, 0.0f, 6.0f)).width(94.0f).height(24.0f).onClick(() -> {}).add(this.mainHeaderNode).add(this.mainHeaderActionNode).add(this.searchButton).renderHook((drawContext, component) -> {
            this.refreshRenderResources();
            if (this.embeddedSettingsMode) {
                return;
            }
            drawContext.drawShadow(component.x(), component.y(), component.w(), component.h(), 10.0f, WidgetState.uniform(5.0f), ColorPalette.BLACK.mulAlpha(0.15f));
            this.drawPanelSurface(drawContext, component, 8.0f);
        });
        SettingPanel.disableTransitionsRecursively(this);
        this.mainHeaderNode.snapPosition();
        this.mainHeaderActionNode.snapPosition();
        this.searchButton.snapPosition(() -> !this.keybindSearchOpen);
        this.searchField.exit(Transition.HIDDEN).lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(160L, Easing.easeOutCubic));
        this.clearSearchButton.exit(Transition.HIDDEN).lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(160L, Easing.easeOutCubic));
        this.keybindListContainer = new Component(){

            @Override
            public boolean mouseScrolled(float f, float f2, float f3, float f4) {
                return super.mouseScrolled(f, f2, f3, f4) || this.inFlow() && this.contains(f, f2);
            }
        }.vertical().width(207.0f).maxSize(207.0f, 150.0f).onClick(() -> {}).scrollable().configureLayoutState(scrollBar2 -> scrollBar2.offset(3.0f).padding(7.0f, 6.0f).thickness(2.5f).thumbColor(scrollBar -> ColorRGBA.BLACK.mix(ColorRGBA.WHITE, 0.3f).withAlpha(255.0f * (0.32f + 0.28f * scrollBar.hoverProgress() + 0.3f * scrollBar.dragProgress())))).visibleWhen(() -> this.keybindSearchOpen && this.keybindSearchReady && !this.searchResults.isEmpty(), Easing.easeOutQuart, 240L).collapse().renderHook((drawContext, component) -> {
            if (this.embeddedSettingsMode) {
                drawContext.drawShadow(component.x(), component.y(), component.w(), component.h(), 10.0f, WidgetState.uniform(11.0f), ColorPalette.BLACK.mulAlpha(0.5f));
                this.drawEmbeddedContent(drawContext, component, 11.0f);
            } else {
                drawContext.drawShadow(component.x(), component.y(), component.w(), component.h(), 10.0f, WidgetState.uniform(11.0f), ColorPalette.BLACK.mulAlpha(0.5f));
                this.drawPanelSurface(drawContext, component, 11.0f);
            }
        });
        this.autoSaveSetting = new BooleanSetting(this, "configs.autosave").setActiveExtra(ModuleConfigurationStore.isAutoSaveEnabled());
        this.languageSetting = new ModeSetting(this, "modules.settings.interface.language");
        // new ModeSetting.Option(this.languageSetting, "\u0420\u0443\u0441\u0441\u043a\u0438\u0439"); // Русский
        new ModeSetting.Option(this.languageSetting, "English");
        // new ModeSetting.Option(this.languageSetting, "\u0423\u043a\u0440\u0430\u0457\u043d\u0441\u044c\u043a\u0430"); // Українська
        // new ModeSetting.Option(this.languageSetting, "polski");
        this.languageIndex = SettingPanel.getLanguageIndex(Localization.getLanguage());
        this.languageSetting.select(this.languageSetting.getOptions().get(this.languageIndex));
        this.accentColorSetting = new ColorSetting(this, "theme.colors.accent").setAlphaEnabled(false).setColor(ColorPalette.getCurrentAccentColor());
        Transition transition = (f, uiNode, state) -> {
            float f2;
            state.progress = 1.0f;
            state.scale = f2 = Math.max(0.0f, f);
            state.offsetX = -(1.0f - f2) * uiNode.w() / 2.0f;
            state.offsetY = (1.0f - f2) * uiNode.h() / 2.0f;
        };
        this.settingsContainer = new Component(){

            @Override
            public boolean mouseScrolled(float f, float f2, float f3, float f4) {
                return super.mouseScrolled(f, f2, f3, f4) || this.inFlow() && this.contains(f, f2);
            }

            @Override
            protected void drawChildren(RockstarDrawContext drawContext, float f) {
                moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)this.x(), (float)this.y(), (float)this.w(), (float)this.h());
                super.drawChildren(drawContext, f);
                moscow.rockstar.render.state.UiScissorStack.pop();
            }
        }.vertical().width(115.0f).padding(Insets.symmetric(6.0f, 0.0f)).onClick(() -> {}).visibleWhen(() -> this.settingsPanelOpen && !this.keybindSearchOpen).transition(transition).lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(300L, Easing.easeOutBack)).add(this.createKeybindButton()).add(SettingPanel.createSettingComponent(this.autoSaveSetting)).add(SettingPanel.createSettingComponent(this.accentColorSetting)).add(SettingPanel.createSettingComponent(this.languageSetting)).renderHook((drawContext, component) -> {
            drawContext.drawShadow(component.x(), component.y(), component.w(), component.h(), 10.0f, WidgetState.uniform(11.0f), ColorPalette.BLACK.mulAlpha(0.5f));
            this.drawPanelSurface(drawContext, component, 11.0f);
        });
        this.keybindHeaderNode = new TextComponent().text(Font.MEDIUM.metrics(8.0f), () -> Localization.translate("profile.binds.title"), textComponent -> ColorPalette.PRIMARY_TEXT_COLOR).interactive(false);
        this.keybindHeaderActionNode = this.createIconButton("xmark", () -> this.setKeybindPanelOpen(false));
        Component component3 = new Component().horizontal().alignment(Alignment.CENTER).overflowMode(JustifyContent.SPACE_BETWEEN).fillWidth().height(14.0f).padding(Insets.symmetric(0.0f, 9.0f)).add(this.keybindHeaderNode).add(this.keybindHeaderActionNode);
        this.keybindView = new Component(){

            @Override
            public boolean mouseScrolled(float f, float f2, float f3, float f4) {
                return super.mouseScrolled(f, f2, f3, f4) || this.inFlow() && this.contains(f, f2);
            }
        }.vertical().fillWidth().maxSize(207.0f, 150.0f).onClick(() -> {}).scrollable().configureLayoutState(scrollBar2 -> scrollBar2.offset(3.0f).padding(1.0f, 6.0f).thickness(2.5f).thumbColor(scrollBar -> ColorRGBA.BLACK.mix(ColorRGBA.WHITE, 0.3f).withAlpha(255.0f * (0.32f + 0.28f * scrollBar.hoverProgress() + 0.3f * scrollBar.dragProgress()))));
        this.settingsView = new Component(){

            @Override
            protected void drawChildren(RockstarDrawContext drawContext, float f) {
                moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)this.x(), (float)this.y(), (float)this.w(), (float)this.h());
                super.drawChildren(drawContext, f);
                moscow.rockstar.render.state.UiScissorStack.pop();
            }
        }.vertical().fillWidth().padding(Insets.of(7.0f, 0.0f, 0.0f, 0.0f)).gap(2.0f).enter(Transition.PROGRESS_ONLY).exit(Transition.HIDDEN).lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(220L, Easing.easeOutCubic)).add(component3).add(this.keybindView);
        this.settingsView.renderHook((drawContext, component) -> {
            if (!this.embeddedSettingsMode) {
                return;
            }
            boolean bl = RockstarClient.create().getColorTheme() == ColorTheme.DARK;
            ColorRGBA colorRGBA = (bl ? ColorPalette.getPanelColor() : ColorPalette.getPanelBackgroundColor()).withAlpha(255.0f);
            drawContext.drawShadow(component.x(), component.y(), component.w(), component.h(), 10.0f, WidgetState.uniform(8.0f), ColorPalette.BLACK.mulAlpha(0.15f));
            drawContext.drawRoundedRect(component.x(), component.y(), component.w(), component.h(), WidgetState.uniform(8.0f), colorRGBA);
            drawContext.drawRoundedBorder(component.x(), component.y(), component.w(), component.h(), 0.5f, WidgetState.uniform(8.0f), ColorPalette.BORDER_COLOR.withAlpha(89.25f));
        });
        SettingPanel.snapLayoutRecursively(this.settingsView);
    }

    /**
     * ORIGINAL: rockstar/ilIlil/IiiIiiIIi#I (Lrockstar/ilIlil/IIiiiIIII;)Lrockstar/ilIlil/iii;.
     */
    private static Component createSettingComponent(Setting setting) {
        return SettingPanel.createRawSettingComponent(setting).collapse().visibleWhen(setting::hasValidSettingValue, Easing.easeOutQuart, 220L);
    }

    /**
     * ORIGINAL: rockstar/ilIlil/IiiIiiIIi#i (Lrockstar/ilIlil/IIiiiIIII;)Lrockstar/ilIlil/iii;.
     * The setting's own component is stretched to the panel width and inset by 9px
     * horizontally; without this the row measures at its natural width and its
     * label/value text runs past the panel edge.
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

    private Component createKeybindButton() {
        Component component = new Component().horizontal().alignment(Alignment.CENTER).gap(6.0f).fillWidth().height(20.0f).padding(Insets.symmetric(0.0f, 9.0f)).cursor(Cursor.HAND);
        TextComponent textComponent2 = new TextComponent().size(8.0f, 8.0f).icon("keyboard", 8.0f, textComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.5f + 0.4f * component.hover())).interactive(false);
        TextComponent textComponent3 = new TextComponent().text(Font.REGULAR.metrics(8.0f), () -> Localization.translate("profile.binds.title"), textComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * component.hover())).fill().interactive(false);
        return component.add(textComponent2).add(textComponent3).onClick(() -> {
            this.setSettingsPanelOpen(false);
            this.setKeybindPanelOpen(true);
        });
    }

    public Component getKeybindList() {
        return this.keybindListContainer;
    }

    public Component getSettingsContainer() {
        return this.settingsContainer;
    }

    public SettingPanel showEmbeddedSettings(UiNode uiNode) {
        this.embeddedSettingsMode = true;
        this.embeddedSettingsNode = uiNode;
        this.replaceChildren(List.of());
        this.settingsView.transition(settingsPanelTransition);
        return this;
    }

    public SettingPanel configureEmbeddedKeybinds(UiNode uiNode, FontMetrics fontMetrics) {
        this.embeddedKeybindNode = uiNode;
        this.embeddedKeybindFont = fontMetrics;
        return this;
    }

    public TextInputField getSearchEditor() {
        if (this.keybindSearchLayout == null) {
            this.keybindSearchLayout = new TextInputField(this.embeddedKeybindFont != null ? this.embeddedKeybindFont : HEADER_FONT);
        }
        return this.keybindSearchLayout;
    }

    private static ColorRGBA getPanelColor() {
        ColorRGBA colorRGBA = ColorPalette.getPanelColor().withAlpha(173.40001f);
        return colorRGBA.mix(ColorPalette.PRIMARY_TEXT_COLOR.withAlpha(colorRGBA.getAlpha()), 0.035f);
    }

    private void refreshRenderResources() {
        if (this.anchorNode == null || !this.isRenderWindowVisible()) {
            return;
        }
        WidgetBatchRenderer.flushCurrentBatch();
        ShaderRenderer.textureRegistry.render(RenderTextureRegistry.EMBEDDED_SLOT, 1.0f);
    }

    private boolean isRenderWindowVisible() {
        boolean bl = this.keybindSearchOpen || this.settingsPanelOpen || !this.embeddedSettingsMode && this.keybindPanelOpen;
        long l = System.currentTimeMillis();
        if (bl) {
            this.visibilityDeadlineMillis = l + 400L;
        }
        return l < this.visibilityDeadlineMillis;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawPanelSurface(RockstarDrawContext drawContext, UiNode uiNode, float f) {
        WidgetState widgetState = WidgetState.uniform(f);
        boolean bl = this.isRenderWindowVisible();
        if (bl) {
            ShaderRenderer.setRequestedTextureId(1);
        }
        try {
            drawContext.drawBlurredRect(uiNode.x(), uiNode.y(), uiNode.w(), uiNode.h(), 5.0f, 3.0f, widgetState, ColorPalette.WHITE);
            drawContext.drawClientRect(uiNode.x(), uiNode.y(), uiNode.w(), uiNode.h(), 1.0f, 0.0f, 3.0f, f, true);
        }
        finally {
            if (bl) {
                ShaderRenderer.resetRequestedTexture();
            }
        }
    }

    private void drawEmbeddedContent(RockstarDrawContext drawContext, UiNode uiNode, float f) {
        boolean bl;
        WidgetState widgetState = WidgetState.uniform(f);
        boolean bl2 = bl = this.anchorNode != null && this.isRenderWindowVisible() && !WidgetBatchRenderer.textureRenderingActive && ShaderRenderer.textureRegistry.hasTexture(1);
        if (bl) {
            WidgetBatchRenderer.flushCurrentBatch();
            float f2 = 16.0f;
            drawContext.drawShader(1, uiNode.x(), uiNode.y(), uiNode.w(), uiNode.h(), 1.5f, 1.0f, 1.2f, this.anchorNode.x() + f2, this.anchorNode.y() + f2, this.anchorNode.w() - 2.0f * f2, this.anchorNode.h() - 2.0f * f2, widgetState, ColorPalette.WHITE);
        }
        drawContext.drawRoundedRect(uiNode.x(), uiNode.y(), uiNode.w(), uiNode.h(), widgetState, bl ? SettingPanel.getPanelColor() : SettingPanel.getPanelColor().withAlpha(255.0f));
        drawContext.drawRoundedBorder(uiNode.x(), uiNode.y(), uiNode.w(), uiNode.h(), 0.5f, widgetState, ColorPalette.BORDER_COLOR.withAlpha(89.25f));
    }

    public void openKeybindSearch() {
        this.setSearchOpen(true);
    }

    public void closeKeybindSearch() {
        this.setSearchOpen(false);
    }

    public void toggleSettingsPanel() {
        this.toggleSettingsPanelVisibility();
    }

    @Override
    public List<Setting> getSettings() {
        return this.settings;
    }

    private static int getLanguageIndex(Language language) {
        return 0;
        /*
        return switch (language) {
            case Language.EN_US -> 1;
            case Language.UK_UA -> 2;
            case Language.PL_PL -> 3;
            default -> 0;
        };
        */
    }

    private void toggleSettingsPanelVisibility() {
        this.setSettingsPanelOpen(!this.settingsPanelOpen);
    }

    private void setSettingsPanelOpen(boolean bl) {
        if (this.settingsPanelOpen == bl) {
            return;
        }
        this.settingsContainer.lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(300L, bl ? Easing.easeOutBack : Easing.easeInBack));
        this.settingsPanelOpen = bl;
        if (!bl) {
            this.accentColorSetting.closePicker();
            return;
        }
        this.accentColorSetting.updateColor(ColorPalette.getCurrentAccentColor());
        this.languageIndex = SettingPanel.getLanguageIndex(Localization.getLanguage());
        this.languageSetting.select(this.languageSetting.getOptions().get(this.languageIndex));
        this.autoSaveSetting.setValueInternal(ModuleConfigurationStore.isAutoSaveEnabled());
    }

    public void handleOutsideClick(float f, float f2) {
        boolean bl;
        boolean bl2 = bl = !this.embeddedSettingsMode && this.contains(f, f2) || this.embeddedSettingsMode && this.embeddedSettingsNode != null && this.embeddedSettingsNode.contains(f, f2);
        if (this.settingsPanelOpen && !bl && !this.settingsContainer.contains(f, f2)) {
            this.setSettingsPanelOpen(false);
        }
        if (this.keybindPanelOpen && !this.contains(f, f2)) {
            this.setKeybindPanelOpen(false);
        }
        if (!(!this.keybindSearchOpen || this.embeddedKeybindNode == null || this.embeddedKeybindNode.contains(f, f2) || this.keybindListContainer.inFlow() && this.keybindListContainer.contains(f, f2))) {
            this.setSearchOpen(false);
        }
    }

    public void openKeybindPanel() {
        this.setKeybindPanelOpen(true);
    }

    public boolean isKeybindPanelActive() {
        return this.keybindPanelOpen || this.isEmbeddedKeybindAnimationComplete();
    }

    private boolean isEmbeddedKeybindAnimationComplete() {
        return this.embeddedSettingsMode && this.settingsView.phase() == UiNode.LifecyclePhase.EXITING;
    }

    private void setKeybindPanelOpen(boolean bl) {
        if (this.keybindPanelOpen == bl) {
            return;
        }
        this.keybindPanelOpen = bl;
        Motion motion = bl ? Motion.motion2 : Motion.resolveMotionMotionFromLongAndEasing(320L, Easing.easeOutQuart);
        this.motion(motion);
        if (bl) {
            this.setSettingsPanelOpen(false);
            this.setSearchOpen(false);
            this.rebuildKeybindEntries();
            float f = Math.min(150.0f, (float)Math.max(1, this.keybindEntries.size()) * 18.0f + 6.0f);
            float f2 = 23.0f + f;
            this.width(207.0f).height(f2).padding(Insets.NONE);
            if (this.embeddedSettingsMode) {
                Window PackageInfo10412 = MinecraftClient.getInstance().getWindow();
                this.snapToSize(207.0f, f2);
                this.snapAt(((float)PackageInfo10412.getScaledWidth() - 207.0f) / 2.0f, ((float)PackageInfo10412.getScaledHeight() - f2) / 2.0f);
                this.settingsView.lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(300L, Easing.easeOutBack));
            }
            this.updateChildren(List.of(this.settingsView));
        } else if (this.embeddedSettingsMode) {
            this.settingsView.lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(300L, Easing.easeInBack));
            this.updateChildren(List.of());
        } else {
            this.mainHeaderNode.enter(Transition.PROGRESS_ONLY).lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(220L, Easing.easeOutCubic));
            this.mainHeaderActionNode.enter(Transition.PROGRESS_ONLY).lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(220L, Easing.easeOutCubic));
            this.searchButton.enter(Transition.PROGRESS_ONLY).lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(220L, Easing.easeOutCubic));
            this.width(94.0f).height(24.0f).padding(Insets.of(0.0f, 8.0f, 0.0f, 6.0f));
            this.updateChildren(List.of(this.mainHeaderNode, this.mainHeaderActionNode, this.searchButton));
        }
    }

    private void rebuildKeybindEntries() {
        SettingDataStore settingDataStore;
        this.keybindEntries.clear();
        List<ModuleContract> list = RockstarClient.create().getModuleRegistry().getModules().stream().filter(ModuleContract::isAvailable).sorted(Comparator.comparing(ModuleContract::getName)).toList();
        for (ModuleContract object42 : list) {
            if (object42.getKeyBind() != -1) {
                this.keybindEntries.add(new KeybindEntry(List.of(object42.getName()), object42::getKeyBind, n -> {
                    object42.setKeyBind(n);
                    SettingPanel.saveSettings();
                }));
            }
            for (Setting setting : object42.getSettings()) {
                IntegerSetting integerSetting;
                if (!(setting instanceof IntegerSetting) || !(integerSetting = (IntegerSetting)setting).hasValidSettingValue() || integerSetting.getValue() == -1) continue;
                this.keybindEntries.add(new KeybindEntry(List.of(object42.getName(), Localization.translate(setting.getName())), integerSetting::getValue, n -> {
                    integerSetting.setValue(n);
                    SettingPanel.saveSettings();
                }));
            }
        }
        Assist assist = RockstarClient.create().getModuleRegistry().getModule(Assist.class);
        if (assist != null) {
            for (AssistItemProvider provider : assist.getAssistSettings()) {
                if (provider.getKeyCode() == -1) continue;
                this.keybindEntries.add(new KeybindEntry(List.of("Assist", "Macros", Localization.translate(provider.getSettingKey())), provider::getKeyCode, n -> {
                    provider.setKeyCode(n);
                    SettingPanel.saveSettings();
                }));
            }
        }
        if ((settingDataStore = RockstarClient.create().getSettingDataStore()) != null) {
            for (SettingEntry settingEntry : settingDataStore.getBindings()) {
                if (settingEntry.getKeyCode() == -1) continue;
                String string = settingEntry.getCommand();
                this.keybindEntries.add(new KeybindEntry(List.of(string), () -> SettingPanel.findKeyCode(string), n -> SettingPanel.updateKeyBinding(string, n)));
            }
        }
        List<UiNode> keybindNodes = new ArrayList<>(this.keybindEntries.size() + 1);
        if (this.keybindEntries.isEmpty()) {
            keybindNodes.add(this.createEmptyKeybindList());
        }
        for (KeybindEntry keybindEntry : this.keybindEntries) {
            keybindNodes.add(this.createKeybindRow(keybindEntry));
        }
        TextComponent textComponent = new TextComponent().fillWidth().height(6.0f).interactive(false);
        SettingPanel.snapLayoutRecursively(textComponent);
        keybindNodes.add(textComponent);
        this.keybindView.updateChildren(keybindNodes);
    }

    private static void saveSettings() {
        ModuleConfigurationStore.saveConfiguration();
    }

    private static void reloadClientSettings() {
        ModuleConfigurationStore.saveConfiguration();
    }

    private static int findKeyCode(String string) {
        SettingDataStore settingDataStore = RockstarClient.create().getSettingDataStore();
        if (settingDataStore != null) {
            for (SettingEntry settingEntry : settingDataStore.getBindings()) {
                if (!settingEntry.getCommand().equalsIgnoreCase(string)) continue;
                return settingEntry.getKeyCode();
            }
        }
        return -1;
    }

    private static void updateKeyBinding(String string, int n) {
        SettingDataStore settingDataStore = RockstarClient.create().getSettingDataStore();
        if (settingDataStore == null) {
            return;
        }
        settingDataStore.removeBindingByCommand(string);
        if (n != -1) {
            settingDataStore.registerBinding(string, n);
        }
        SettingPanel.reloadClientSettings();
    }

    private Component createEmptyKeybindList() {
        TextComponent textComponent2 = new TextComponent().fill().interactive(false).text(Font.REGULAR.metrics(8.0f), () -> Localization.translate("commands.bind.list_empty"), textComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.5f));
        Component component = new Component().horizontal().alignment(Alignment.CENTER).height(18.0f).fillWidth().padding(Insets.symmetric(0.0f, 9.0f)).add(textComponent2);
        SettingPanel.snapLayoutRecursively(component);
        return component;
    }

    private Component createKeybindRow(KeybindEntry keybindEntry) {
        KeyBindingControl keyBindingControl = new KeyBindingControl(Font.REGULAR.metrics(7.0f), keybindEntry.getKeyCodeSupplier(), keybindEntry.getKeyCodeConsumer());
        TextComponent textComponent2 = new TextComponent().fill().interactive(false).paint((drawContext, textComponent) -> this.drawKeybindPath(drawContext, textComponent, keybindEntry.getPathSegments()));
        Component component = ((Component)new Component().height(18.0f).fillWidth().padding(Insets.symmetric(0.0f, 9.0f)).add(textComponent2).add(keyBindingControl).gap(5.0f).layout(Layout.ROW).overflowMode(JustifyContent.SPACE_BETWEEN).alignment(Alignment.CENTER).onClick(keyBindingControl::handlePointerClick)).cursor(Cursor.HAND);
        SettingPanel.snapLayoutRecursively(component);
        return component;
    }

    private void drawKeybindPath(RockstarDrawContext drawContext, TextComponent textComponent, List<String> list) {
        FontMetrics fontMetrics = Font.REGULAR.metrics(8.0f);
        float f = textComponent.y() + textComponent.h() / 2.0f - fontMetrics.getFontTopOffset() / 2.0f;
        float f2 = textComponent.x();
        for (int i = 0; i < list.size(); ++i) {
            boolean bl = i == list.size() - 1;
            String string = list.get(i);
            drawContext.drawText(fontMetrics, string, f2, f, ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(bl ? 1.0f : 0.5f));
            f2 += fontMetrics.measureText(string);
            if (bl) continue;
            float f3 = f2 + 3.0f;
            drawContext.drawRoundedRect(f3, textComponent.y() + textComponent.h() / 2.0f - 1.0f, 2.0f, 2.0f, WidgetState.uniform(1.0f), ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.5f));
            f2 = f3 + 2.0f + 4.0f;
        }
    }

    public boolean isSearchOpen() {
        return this.keybindSearchOpen;
    }

    private boolean isEmbeddedSearchReady() {
        return this.embeddedKeybindNode != null && this.keybindListContainer.phase() == UiNode.LifecyclePhase.EXITING;
    }

    private TextComponent createIconButton(String string, Runnable runnable) {
        return new TextComponent().size(8.0f, 8.0f).textInset(1.0f).icon(string, 8.0f, textComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.5f + 0.4f * textComponent.hover())).cursor(Cursor.HAND).onClick(runnable);
    }

    private static void disableTransitionsRecursively(UiNode uiNode) {
        uiNode.enter(Transition.NO_OP);
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            for (UiNode uiNode2 : component.children()) {
                SettingPanel.disableTransitionsRecursively(uiNode2);
            }
        }
    }

    private static void snapLayoutRecursively(UiNode uiNode) {
        uiNode.snapPosition().snapSize();
        if (uiNode instanceof Component) {
            Component component = (Component)uiNode;
            for (UiNode uiNode2 : component.children()) {
                SettingPanel.snapLayoutRecursively(uiNode2);
            }
        }
    }

    @Override
    protected void drawChildren(RockstarDrawContext drawContext, float f) {
        if (!(!this.embeddedSettingsMode || this.keybindPanelOpen || this.keybindSearchOpen || this.isEmbeddedKeybindAnimationComplete() || this.isEmbeddedSearchReady())) {
            return;
        }
        if (this.embeddedSettingsMode || !this.keybindPanelOpen) {
            super.drawChildren(drawContext, f);
            return;
        }
        MatrixStack class_45872 = drawContext.getMatrices();
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)class_45872, (float)this.x(), (float)this.y(), (float)this.w(), (float)this.h());
        super.drawChildren(drawContext, f);
        moscow.rockstar.render.state.UiScissorStack.pop();
    }

    @Override
    public boolean mouseClicked(float f, float f2, PointerAction pointerAction) {
        if (this.embeddedSettingsMode && !this.keybindPanelOpen) {
            return false;
        }
        return super.mouseClicked(f, f2, pointerAction);
    }

    private void setSearchOpen(boolean bl) {
        Motion motion;
        if (this.keybindSearchOpen == bl) {
            return;
        }
        this.keybindSearchOpen = bl;
        Motion motion2 = motion = bl ? Motion.motion2 : Motion.resolveMotionMotionFromLongAndEasing(320L, Easing.easeOutQuart);
        if (this.embeddedKeybindNode == null) {
            this.motion(motion);
        }
        this.searchButton.motion(motion);
        this.keybindListContainer.lifeMotion(bl ? Motion.resolveMotionMotionFromLongAndEasing(180L, Easing.easeOutQuart) : Motion.resolveMotionMotionFromLongAndEasing(260L, Easing.easeOutQuart));
        if (bl) {
            this.setSettingsPanelOpen(false);
            this.setKeybindPanelOpen(false);
            this.keybindSearchOpenedAtMillis = System.currentTimeMillis();
            if (this.embeddedKeybindNode == null) {
                this.width(207.0f).height(24.0f).padding(Insets.symmetric(0.0f, 10.0f));
                this.updateChildren(List.of(this.searchButton, this.searchField, this.clearSearchButton));
            }
            this.getSearchEditor().clear();
            this.getSearchEditor().setPlaceholder("Search");
            this.getSearchEditor().setFocused(true);
            this.selectedKeybindIndex = 0;
            this.updateSearchResults();
            this.keybindSearchText = "";
        } else if (this.embeddedKeybindNode != null) {
            this.getSearchEditor().clear();
            this.getSearchEditor().setFocused(false);
        } else {
            this.mainHeaderNode.enter(Transition.PROGRESS_ONLY).lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(220L, Easing.easeOutCubic));
            this.mainHeaderActionNode.enter(Transition.PROGRESS_ONLY).lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(220L, Easing.easeOutCubic));
            this.width(94.0f).height(24.0f).padding(Insets.of(0.0f, 8.0f, 0.0f, 6.0f));
            this.updateChildren(List.of(this.mainHeaderNode, this.mainHeaderActionNode, this.searchButton));
            this.getSearchEditor().setFocused(false);
        }
    }

    private String getSearchText() {
        String string = this.getSearchEditor().getText();
        return string == null ? "" : string.trim();
    }

    private static String convertKeyboardLayout(String string) {
        StringBuilder stringBuilder = new StringBuilder(string.length());
        for (char c : string.toCharArray()) {
            int n = RUSSIAN_KEYBOARD_LAYOUT.indexOf(c);
            if (n >= 0) {
                stringBuilder.append(QWERTY_LAYOUT.charAt(n));
                continue;
            }
            n = QWERTY_LAYOUT.indexOf(c);
            stringBuilder.append(n >= 0 ? RUSSIAN_KEYBOARD_LAYOUT.charAt(n) : c);
        }
        return stringBuilder.toString();
    }

    private static int fuzzyMatchScore(String string, String string2, String string3) {
        String normalizedName = SearchMatcher.normalize(string);
        return Math.min(SearchMatcher.scoreNormalized(normalizedName, string2), SearchMatcher.scoreNormalized(normalizedName, string3));
    }

    private void updateSearchResults() {
        int n;
        String string = this.getSearchText();
        String string2 = SearchMatcher.normalize(string);
        String string3 = SearchMatcher.normalize(SettingPanel.convertKeyboardLayout(string.toLowerCase()));
        this.searchResults.clear();
        List<ModuleContract> list = RockstarClient.create().getModuleRegistry().getModules().stream().filter(ModuleContract::isAvailable).sorted(Comparator.comparing(ModuleContract::getName, String.CASE_INSENSITIVE_ORDER)).toList();
        if (string2.isEmpty()) {
            for (ModuleContract moduleContract : list) {
                this.searchResults.add(new SettingSearchEntry(moduleContract, null, moduleContract.getName(), 0));
            }
            this.renderSearchResults();
            return;
        }
        ModuleContract moduleContract = null;
        int n2 = Integer.MAX_VALUE;
        for (ModuleContract object : list) {
            n = SettingPanel.fuzzyMatchScore(object.getName(), string2, string3);
            if (n > 10 || n >= n2) continue;
            moduleContract = object;
            n2 = n;
        }
        if (moduleContract != null) {
            this.searchResults.add(new SettingSearchEntry(moduleContract, null, moduleContract.getName(), n2));
            int n3 = 0;
            for (Setting setting : moduleContract.getSettings()) {
                if (!setting.hasValidSettingValue()) continue;
                this.searchResults.add(new SettingSearchEntry(moduleContract, setting, Localization.translate(setting.getName()), 100 + n3++));
            }
            this.renderSearchResults();
            return;
        }
        for (ModuleContract moduleContract2 : list) {
            boolean bl;
            n = SettingPanel.fuzzyMatchScore(moduleContract2.getName(), string2, string3);
            boolean bl2 = bl = n != Integer.MAX_VALUE;
            if (bl) {
                this.searchResults.add(new SettingSearchEntry(moduleContract2, null, moduleContract2.getName(), n));
            }
            int n3 = 0;
            for (Setting setting : moduleContract2.getSettings()) {
                if (!setting.hasValidSettingValue()) continue;
                String string4 = Localization.translate(setting.getName());
                int n4 = SettingPanel.fuzzyMatchScore(string4, string2, string3);
                if (n4 != Integer.MAX_VALUE) {
                    this.searchResults.add(new SettingSearchEntry(moduleContract2, setting, string4, 1000 + n4));
                } else if (bl) {
                    this.searchResults.add(new SettingSearchEntry(moduleContract2, setting, string4, 2000 + n + n3));
                }
                ++n3;
            }
        }
        this.searchResults.sort(Comparator.comparingInt(SettingSearchEntry::getMatchScore).thenComparing(settingSearchEntry -> settingSearchEntry.getModule().getName(), String.CASE_INSENSITIVE_ORDER).thenComparing(SettingSearchEntry::getDisplayName, String.CASE_INSENSITIVE_ORDER));
        this.renderSearchResults();
    }

    private void renderSearchResults() {
        if (this.selectedKeybindIndex >= this.searchResults.size()) {
            this.selectedKeybindIndex = Math.max(0, this.searchResults.size() - 1);
        }
        ArrayList<TextComponent> arrayList = new ArrayList<TextComponent>(this.searchResults.size() + 2);
        arrayList.add(SettingPanel.createListSpacer());
        for (int i = 0; i < this.searchResults.size(); ++i) {
            arrayList.add(this.createSearchResultRow(this.searchResults.get(i), i));
        }
        arrayList.add(SettingPanel.createListSpacer());
        this.keybindListContainer.updateChildren(arrayList);
    }

    private static TextComponent createListSpacer() {
        return new TextComponent().fillWidth().height(6.0f).interactive(false);
    }

    private TextComponent createSearchResultRow(SettingSearchEntry settingSearchEntry, int n) {
        return new TextComponent().fillWidth().height(18.0f).cursor(Cursor.HAND).hoverMotion(Motion.withLinearEasing(70L)).bind("sel", () -> n == this.selectedKeybindIndex, Motion.resolveMotionMotionFromLongAndEasing(160L, Easing.easeOutQuart)).onClick(() -> this.activateSearchResult(settingSearchEntry, true)).paint((drawContext, textComponent) -> this.drawSearchResultRow(drawContext, textComponent, settingSearchEntry, n));
    }

    private void drawSearchResultRow(RockstarDrawContext drawContext, TextComponent textComponent, SettingSearchEntry settingSearchEntry, int n) {
        float f;
        float f2;
        String string;
        if (textComponent.hover() > 0.5f) {
            this.selectedKeybindIndex = n;
        }
        FontMetrics fontMetrics = Font.REGULAR.metrics(8.0f);
        FontMetrics fontMetrics2 = Font.REGULAR.metrics(7.0f);
        String string2 = "TAB - \u041e\u0442\u043a\u0440\u044b\u0442\u044c";
        float f3 = 5.0f;
        float f4 = textComponent.x() + textComponent.w() - 10.0f - f3;
        float f5 = f4 - 3.0f - fontMetrics2.measureText(string2);
        float f6 = textComponent.y() + textComponent.h() / 2.0f - fontMetrics.getFontTopOffset() / 2.0f;
        float f7 = textComponent.x() + 10.0f;
        if (settingSearchEntry.hasSetting()) {
            string = settingSearchEntry.getModule().getName();
            drawContext.drawText(fontMetrics, string, f7, f6, ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.5f));
            f2 = f7 + fontMetrics.measureText(string) + 2.0f;
            drawContext.drawRoundedRect(f2, textComponent.y() + textComponent.h() / 2.0f - 1.0f, 2.0f, 2.0f, WidgetState.uniform(1.0f), ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.5f));
            f = f2 + 2.0f + 4.0f;
        } else {
            f = f7;
        }
        string = settingSearchEntry.getDisplayName();
        f2 = textComponent.sig("sel");
        float f8 = textComponent.x() + textComponent.w() - 10.0f - f;
        float f9 = f5 - 6.0f - f;
        float f10 = f8 + (f9 - f8) * f2;
        if (fontMetrics.measureText(string) > f10) {
            drawContext.drawFadeText(fontMetrics, string, f, f6, ColorPalette.PRIMARY_TEXT_COLOR, 0.0f, 8.0f, f10);
        } else {
            drawContext.drawText(fontMetrics, string, f, f6, ColorPalette.PRIMARY_TEXT_COLOR);
        }
        if (n == this.selectedKeybindIndex) {
            this.selectedRowAnimation.setTarget(textComponent.y());
            float f11 = this.selectedRowAnimation.getCurrent();
            ColorRGBA colorRGBA = ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.5f);
            drawContext.drawText(fontMetrics2, string2, f5, f11 + textComponent.h() / 2.0f - fontMetrics2.getFontTopOffset() / 2.0f, colorRGBA);
            drawContext.drawIcon("arrows", f4, f11 + textComponent.h() / 2.0f - f3 / 2.0f, f3, colorRGBA);
        }
    }

    private void moveSearchSelection(int n) {
        if (this.searchResults.isEmpty()) {
            return;
        }
        this.selectedKeybindIndex = Math.max(0, Math.min(this.searchResults.size() - 1, this.selectedKeybindIndex + n));
    }

    private void activateSearchResult(SettingSearchEntry settingSearchEntry, boolean bl) {
        if (bl) {
            if (this.settingSelectionCallback != null) {
                this.settingSelectionCallback.accept(settingSearchEntry.getModule(), settingSearchEntry.getSetting());
            }
            this.setSearchOpen(false);
        } else if (settingSearchEntry.getSetting() == null) {
            settingSearchEntry.getModule().toggle();
        } else {
            Setting setting = settingSearchEntry.getSetting();
            if (setting instanceof BooleanSetting) {
                BooleanSetting booleanSetting = (BooleanSetting)setting;
                booleanSetting.toggle();
            }
        }
    }

    private void activateSelectedSearchResult(boolean bl) {
        if (!this.searchResults.isEmpty()) {
            this.activateSearchResult(this.searchResults.get(this.selectedKeybindIndex), bl);
        }
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (this.keybindPanelOpen) {
            if (super.keyPressed(n, n2, n3)) {
                return true;
            }
            if (n == 256) {
                this.setKeybindPanelOpen(false);
            }
            return true;
        }
        if (!this.keybindSearchOpen) {
            if (n == 70 && (n3 & 2) != 0 && !KeyBindingControl.isControlActive()) {
                this.setSearchOpen(true);
                return true;
            }
            if (this.settingsPanelOpen && n == 256) {
                this.setSettingsPanelOpen(false);
                return true;
            }
            return false;
        }
        switch (n) {
            case 256: {
                this.setSearchOpen(false);
                break;
            }
            case 258: {
                this.activateSelectedSearchResult(true);
                break;
            }
            case 257: 
            case 335: {
                this.activateSelectedSearchResult(false);
                break;
            }
            case 265: {
                this.moveSearchSelection(-1);
                break;
            }
            case 264: {
                this.moveSearchSelection(1);
                break;
            }
            default: {
                this.getSearchEditor().keyPressed(n, n2, n3);
            }
        }
        return true;
    }

    @Override
    public boolean charTyped(char c, int n) {
        if (!this.keybindSearchOpen) {
            return false;
        }
        this.getSearchEditor().charTyped(c, n);
        return true;
    }

    @Override
    public boolean mouseScrolled(float f, float f2, float f3, float f4) {
        return super.mouseScrolled(f, f2, f3, f4) || this.contains(f, f2);
    }

    @Override
    public void mouseReleased(float f, float f2, PointerAction pointerAction) {
        super.mouseReleased(f, f2, pointerAction);
        if (this.keybindSearchLayout != null) {
            this.keybindSearchLayout.mouseReleased(f, f2, pointerAction);
        }
    }

    @Override
    protected void onTick(float f, float f2, float f3) {
        Object object;
        super.onTick(f, f2, f3);
        this.selectedRowAnimation.update(f);
        float f4 = this.desiredW();
        float f5 = this.desiredH();
        boolean bl = this.keybindSearchReady = this.keybindSearchOpen && System.currentTimeMillis() - this.keybindSearchOpenedAtMillis > 140L;
        if ((this.keybindSearchOpen || this.isEmbeddedSearchReady()) && this.embeddedKeybindNode == null) {
            Window window = MinecraftClient.getInstance().getWindow();
            float f6 = this.searchResults.isEmpty() ? 0.0f : Math.min(150.0f, (float)this.searchResults.size() * 18.0f + 12.0f);
            float f7 = f5 + (f6 > 0.0f ? 6.0f + f6 : 0.0f);
            this.at(((float)window.getScaledWidth() - f4) / 2.0f, ((float)window.getScaledHeight() - f7) / 2.0f);
        } else if (this.keybindPanelOpen || this.isEmbeddedKeybindAnimationComplete()) {
            Window window = MinecraftClient.getInstance().getWindow();
            this.at(((float)window.getScaledWidth() - f4) / 2.0f, ((float)window.getScaledHeight() - f5) / 2.0f);
        } else {
            this.at(this.anchorNode.x() + (this.anchorNode.w() - f4) / 2.0f, this.anchorNode.y() + this.anchorNode.h() + 10.0f);
        }
        if (this.embeddedKeybindNode != null) {
            this.keybindListContainer.snapAt(this.anchorNode.x() + (this.anchorNode.w() - 207.0f) / 2.0f, this.anchorNode.y() + (this.anchorNode.h() - this.keybindListContainer.h()) / 2.0f);
        } else {
            this.keybindListContainer.snapAt(this.x(), this.y() + this.h() + 6.0f);
        }
        if (this.settingsPanelOpen) {
            UiNode settingsAnchor = this.embeddedSettingsNode != null ? this.embeddedSettingsNode : this.mainHeaderActionNode;
            if (this.embeddedSettingsMode && this.embeddedSettingsNode != null) {
                this.settingsContainer.snapAt(settingsAnchor.x() + settingsAnchor.w() + 5.0f, settingsAnchor.y() + settingsAnchor.h() - this.settingsContainer.h());
            } else {
                this.settingsContainer.snapAt(settingsAnchor.x(), settingsAnchor.y() + settingsAnchor.h() / 2.0f - this.settingsContainer.h() / 2.0f);
            }
        }
        this.applySettingsChanges();
        if (!this.keybindSearchOpen) {
            return;
        }
        if (!this.getSearchEditor().isFocused()) {
            this.getSearchEditor().setFocused(true);
        }
        String searchText = this.getSearchEditor().getText();
        if (!searchText.equals(this.keybindSearchText)) {
            this.keybindSearchText = searchText;
            this.updateSearchResults();
        }
        if (this.selectedKeybindIndex >= this.searchResults.size()) {
            this.selectedKeybindIndex = Math.max(0, this.searchResults.size() - 1);
        }
    }

    private void applySettingsChanges() {
        ColorRGBA colorRGBA;
        int n = this.languageSetting.getOptions().indexOf(this.languageSetting.getSelectedOption());
        if (n != this.languageIndex && n >= 0) {
            this.languageIndex = n;
            Localization.setLanguage(Language.EN_US);
            /*
            Localization.setLanguage(switch (n) {
                case 1 -> Language.EN_US;
                case 2 -> Language.UK_UA;
                case 3 -> Language.PL_PL;
                default -> Language.RU_RU;
            });
            */
            ModuleConfigurationStore.saveConfiguration();
        }
        if (this.autoSaveSetting.isEnabled() != ModuleConfigurationStore.isAutoSaveEnabled()) {
            ModuleConfigurationStore.setAutoSaveEnabled(this.autoSaveSetting.isEnabled());
            SettingPanel.reloadClientSettings();
        }
        ColorRGBA colorRGBA2 = colorRGBA = this.accentColorSetting.getColor() == null ? null : this.accentColorSetting.getColor().withAlpha(255.0f);
        if (colorRGBA != null && !colorRGBA.equals(ColorPalette.getCurrentAccentColor())) {
            ColorPalette.setCurrentAccentColor(colorRGBA);
            this.accentColorChangedAtMillis = System.currentTimeMillis();
        }
        if (this.accentColorChangedAtMillis != 0L && System.currentTimeMillis() - this.accentColorChangedAtMillis > 600L) {
            this.accentColorChangedAtMillis = 0L;
            ModuleConfigurationStore.saveConfiguration();
        }
    }

    public static Identifier getAvatarTexture() {
        return RockstarClient.resourceId("icon.png");
    }

    static final class KeybindEntry {
        private final List<String> pathSegments;
        private final IntSupplier keyCodeSupplier;
        private final IntConsumer keyCodeConsumer;

        KeybindEntry(List<String> list, IntSupplier intSupplier, IntConsumer intConsumer) {
            this.pathSegments = list;
            this.keyCodeSupplier = intSupplier;
            this.keyCodeConsumer = intConsumer;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "pathSegments", "keyCodeSupplier", "keyCodeConsumer");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "pathSegments", "keyCodeSupplier", "keyCodeConsumer");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "pathSegments", "keyCodeSupplier", "keyCodeConsumer");
        }

        public List<String> getPathSegments() {
            return this.pathSegments;
        }

        public IntSupplier getKeyCodeSupplier() {
            return this.keyCodeSupplier;
        }

        public IntConsumer getKeyCodeConsumer() {
            return this.keyCodeConsumer;
        }
    }

    static final class SettingSearchEntry {
        private final ModuleContract module;
        private final Setting setting;
        private final String displayName;
        private final int matchScore;

        SettingSearchEntry(ModuleContract moduleContract, Setting setting, String string, int n) {
            this.module = moduleContract;
            this.setting = setting;
            this.displayName = string;
            this.matchScore = n;
        }

        boolean hasSetting() {
            return this.setting != null;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "module", "setting", "displayName", "matchScore");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "module", "setting", "displayName", "matchScore");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "module", "setting", "displayName", "matchScore");
        }

        public ModuleContract getModule() {
            return this.module;
        }

        public Setting getSetting() {
            return this.setting;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public int getMatchScore() {
            return this.matchScore;
        }
    }
}
