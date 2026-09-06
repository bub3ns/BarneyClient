package moscow.rockstar.ui.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistItemProvider;
import moscow.rockstar.modules.other.assist.Assist;
import moscow.rockstar.modules.player.inventory.InventoryMove;
import moscow.rockstar.modules.visuals.menu.Menu;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.settings.SettingComponent;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.KeyBindingUtil;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import moscow.rockstar.ui.screens.AssistGroup;
import moscow.rockstar.ui.screens.AssistItem;
import moscow.rockstar.ui.screens.AssistItemDetailBounds;
import moscow.rockstar.ui.screens.AssistItemDetailRenderer;
import moscow.rockstar.ui.screens.AssistItemSelection;
import moscow.rockstar.ui.screens.AssistItemSettingsState;
import moscow.rockstar.ui.screens.AssistSearchResult;
import moscow.rockstar.ui.screens.MinecraftScreenBase;
import moscow.rockstar.ui.settings.SettingSnapshotCache;
import moscow.rockstar.ui.state.ScrollOffset;
import moscow.rockstar.ui.state.TextScrollState;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.TextInputField;
import moscow.rockstar.ui.text.TextLayout;
import moscow.rockstar.ui.text.TextLayoutManager;
import moscow.rockstar.ui.widgets.settings.NumberSettingComponent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import pyrock.events.window.MouseEvent;
import ua.mintantileak.spk.Compile;

public class AssistScreen extends MinecraftScreenBase implements ClientAccess, WindowMetricsProvider {
    private static final float DIALOG_WIDTH = 238.0f;
    private static final float DIALOG_HEIGHT = 223.0f;
    private static final float DETAIL_PANEL_WIDTH = 132.0f;
    private static final float BACKDROP_BLUR_RADIUS = 5.0f;

    private int pointerX;
    private int pointerY;
    private ItemCategory selectedCategory = ItemCategory.ALL;
    private float categoryTabsX;
    private float categoryTabsY;
    private float categoryTabsWidth;
    private float categoryTabsHeight;
    private AssistItemProvider selectedAssistItem;
    private AssistItemProvider detailAssistItem;
    private AssistItemProvider pendingAssistItem;
    private float dialogX;
    private float dialogY;
    private float detailPanelX;
    private float detailPanelY;
    private float detailPanelWidth;
    private float detailPanelHeight;
    private float detailContentX;
    private float detailContentY;
    private float detailContentWidth;
    private float detailContentHeight;
    private float searchHeaderX;
    private float searchHeaderY;
    private float searchHeaderWidth;
    private float searchHeaderHeight;

    private final Animation panelAnimation = new Animation(300L, 0.0f, Easing.easeOutBack);
    private final Animation categoryAnimation = new Animation(200L, 0.0f, Easing.easeInOutCubicBezier);
    private final Animation detailAnimation = new Animation(200L, 0.0f, Easing.easeInOutCubicBezier);
    private final Animation detailPanelAnimation = new Animation(300L, 0.0f, Easing.easeInOutCubicPolynomial);
    private final Animation dialogSizeAnimation = new Animation(300L, 0.0f, Easing.easeInOutCubicPolynomial);
    private final Animation listFadeAnimation = new Animation(300L, 0.0f, Easing.easeInOutCubicPolynomial);
    private final Animation itemFadeAnimation = new Animation(200L, 0.0f, Easing.easeInOutCubicBezier);
    private final Animation searchFadeAnimation = new Animation(200L, 0.0f, Easing.easeInOutCubicBezier);
    private boolean searchActive;
    private boolean layoutInitializationPending = true;
    private final List<AssistGroup> assistGroups = new ArrayList<>();
    private final ScrollOffset categoryScroll = new ScrollOffset();
    private final ScrollOffset assistItemScroll = new ScrollOffset();
    private final TextInputField searchField = new TextInputField(Font.REGULAR.metrics(7.0f));
    private final Map<AssistItemProvider, TextScrollState> labelScrollStates = new HashMap<>();
    private final Map<AssistItemProvider, Animation> itemAnimations = new HashMap<>();
    private final Map<ItemCategory, Animation> categoryAnimations = new HashMap<>();
    private final Map<AssistItemProvider, AssistItemSettingsState> itemSettingsStates = new HashMap<>();
    private final Map<AssistItemProvider, Animation> removalAnimations = new HashMap<>();
    private final Map<AssistItemProvider, Animation> additionAnimations = new HashMap<>();
    private final Map<AssistItem, Animation> groupAnimations = new HashMap<>();
    private final Map<AssistItemProvider, Animation> selectionAnimations = new HashMap<>();
    private final TextLayout searchLayout = new TextLayout();
    private final TextLayoutManager layoutManager = new TextLayoutManager();
    private final AssistItemDetailRenderer detailRenderer = new AssistItemDetailRenderer();
    private final EventListener<MouseEvent> mouseEventListener = mouseEvent -> {
        if (mouseEvent.getAction() != 1 || mouseEvent.getButton() < 3) {
            return;
        }
        if (this.pendingAssistItem != null) {
            this.pendingAssistItem.setKeyCode(KeyBindingUtil.withCurrentModifiers(mouseEvent.getButton()));
            this.pendingAssistItem = null;
            return;
        }
        AssistItemSettingsState settingsState = this.selectedAssistItem == null
            ? null : this.itemSettingsStates.get(this.selectedAssistItem);
        if (settingsState != null && settingsState.isCapturingKeyBinding()) {
            settingsState.assignKeyCode(KeyBindingUtil.withCurrentModifiers(mouseEvent.getButton()));
            settingsState.cancelKeyBindingCapture();
            return;
        }
        if (this.searchActive) {
            return;
        }
        AssistItemSelection selection = this.layoutManager.findAssistItemAt(this.pointerX, this.pointerY,
            this.dialogX, this.dialogY, DIALOG_WIDTH, DIALOG_HEIGHT, this.assistItemScroll,
            this.selectedCategory, this.getActiveAssistItems());
        if (selection != null && UiUtils.contains(selection.getKeyBindX(), selection.getKeyBindY(),
            selection.getKeyBindWidth(), selection.getKeyBindHeight(), this.pointerX, this.pointerY)) {
            selection.getProvider().setKeyCode(KeyBindingUtil.withCurrentModifiers(mouseEvent.getButton()));
        }
    };

    private List<AssistItemProvider> getAssistItems() {
        Assist assist = RockstarClient.create().getModuleRegistry().getModule(Assist.class);
        return assist == null ? List.of() : assist.getAssistItems();
    }

    @Compile(obfuscation = 4)
    protected final void init() {
        this.searchActive = false;
        this.layoutInitializationPending = true;
        this.searchField.setPlaceholder("Поиск");
        this.rebuildAssistGroups();
        RockstarClient.create().getEventBus().registerListeners(this);
        super.init();
    }

    @Override
    public void render(RockstarDrawContext drawContext) {
        this.pointerX = drawContext.mouseX();
        this.pointerY = drawContext.mouseY();
        this.panelAnimation.setReverse(true);
        this.removeFinishedItems();
        this.finishItemAdditions();
        this.listFadeAnimation.setReverse(this.searchActive);
        if (this.searchActive) {
            this.categoryScroll.update();
        } else {
            this.assistItemScroll.update();
        }
        boolean detailVisible = this.selectedAssistItem != null && !this.searchActive;
        this.detailPanelAnimation.setReverse(detailVisible);
        if (this.selectedAssistItem != null) {
            this.detailAssistItem = this.selectedAssistItem;
        }
        float dialogWidth = DIALOG_WIDTH;
        if (detailVisible || this.detailPanelAnimation.getValue() > 0.01f) {
            dialogWidth += 137.0f * this.detailPanelAnimation.getValue();
        }
        if (this.layoutInitializationPending) {
            this.dialogSizeAnimation.setValue(dialogWidth);
            this.layoutInitializationPending = false;
        } else {
            this.dialogSizeAnimation.update(dialogWidth);
        }
        this.dialogX = INSTANCE.width() / 2.0f - this.dialogSizeAnimation.getValue() / 2.0f;
        this.dialogY = INSTANCE.height() / 2.0f - 111.5f;
        ItemRenderUtils.translateAndScale(drawContext.getMatrices(), this.dialogX + 119.0f,
            this.dialogY + 111.5f, 0.5f + 0.5f * this.panelAnimation.getValue());
        this.renderPanelBackground(drawContext, this.dialogX, this.dialogY, DIALOG_HEIGHT);
        float searchOpacity = this.listFadeAnimation.getValue();
        float listOpacity = 1.0f - searchOpacity;
        if (listOpacity > 0.01f) {
            TextLayoutManager.LayoutBounds categoryBounds = this.layoutManager.renderCategoryTabs(drawContext,
                this.dialogX, this.dialogY, DIALOG_WIDTH, listOpacity, this.panelAnimation,
                this.selectedCategory, this.categoryAnimations, this.categoryAnimation);
            this.categoryTabsX = categoryBounds.getX();
            this.categoryTabsY = categoryBounds.getY();
            this.categoryTabsWidth = categoryBounds.getWidth();
            this.categoryTabsHeight = categoryBounds.getHeight();
            this.layoutManager.renderAssistItemGrid(drawContext, this.dialogX, this.dialogY, DIALOG_WIDTH,
                DIALOG_HEIGHT, listOpacity, this.panelAnimation, this.assistItemScroll, this.selectedCategory,
                this.getActiveAssistItems(), this.selectedAssistItem, this.pendingAssistItem,
                this.pointerX, this.pointerY, this.labelScrollStates, this.itemAnimations,
                this.selectionAnimations, provider -> this.getRemovalProgress(provider) * this.getAdditionProgress(provider));
        }
        if (searchOpacity > 0.01f) {
            TextLayout.LayoutBounds searchBounds = this.searchLayout.renderSearchHeader(drawContext,
                this.dialogX, this.dialogY, DIALOG_WIDTH, searchOpacity, this.panelAnimation,
                this.searchFadeAnimation, this.itemFadeAnimation, this.searchField);
            this.searchHeaderX = searchBounds.getX();
            this.searchHeaderY = searchBounds.getY();
            this.searchHeaderWidth = searchBounds.getWidth();
            this.searchHeaderHeight = searchBounds.getHeight();
            this.searchLayout.renderAssistItemGrid(drawContext, this.dialogX, this.dialogY,
                DIALOG_WIDTH, DIALOG_HEIGHT, searchOpacity, this.panelAnimation, this.assistGroups,
                this.categoryScroll, this.groupAnimations, this.searchField, this::isAssistItemAlreadySelected);
        }
        ItemRenderUtils.popMatrix(drawContext.getMatrices());
        if (this.detailPanelAnimation.getValue() > 0.01f) {
            this.renderDetailPanel(drawContext);
        }
    }

    private void renderPanelBackground(RockstarDrawContext drawContext, float x, float y, float height) {
        float alpha = this.panelAnimation.getValue();
        drawContext.drawShadow(x, y, DIALOG_WIDTH, height, 25.0f, WidgetState.uniform(11.0f),
            ColorPalette.BLACK.mulAlpha(0.5f * alpha));
        drawContext.drawBlurredRect(x, y, DIALOG_WIDTH, height, BACKDROP_BLUR_RADIUS, 3.0f,
            WidgetState.uniform(11.0f), ColorPalette.WHITE.mulAlpha(alpha));
        drawContext.drawSquircle(x, y, DIALOG_WIDTH, height, 3.0f, WidgetState.uniform(11.0f),
            ColorPalette.PANEL_COLOR.mulAlpha(alpha));
        drawContext.drawSquircleBorder(x, y, DIALOG_WIDTH, height, 0.5f, 3.0f,
            WidgetState.uniform(11.0f), ColorPalette.BORDER_COLOR.mulAlpha(alpha));
    }

    private void renderDetailPanel(RockstarDrawContext drawContext) {
        if (this.detailPanelAnimation.getValue() <= 0.01f) {
            return;
        }
        AssistItemProvider provider = this.selectedAssistItem != null
            ? this.selectedAssistItem : this.detailAssistItem;
        if (provider == null) {
            return;
        }
        AssistItemSettingsState settingsState = this.itemSettingsStates.computeIfAbsent(provider,
            item -> new AssistItemSettingsState(item, () -> this.startItemRemoval(item)));
        AssistItemDetailBounds bounds = this.detailRenderer.render(drawContext, this.dialogX, this.dialogY,
            DIALOG_WIDTH, 5.0f, DETAIL_PANEL_WIDTH, 18.0f, 5.0f, this.panelAnimation,
            this.detailPanelAnimation, this.detailAnimation, settingsState, provider,
            this.getRemovalProgress(provider));
        this.detailPanelX = bounds.getPanelX();
        this.detailPanelY = bounds.getPanelY();
        this.detailPanelWidth = bounds.getPanelWidth();
        this.detailPanelHeight = bounds.getPanelHeight();
        this.detailContentX = bounds.getContentX();
        this.detailContentY = bounds.getContentY();
        this.detailContentWidth = bounds.getContentWidth();
        this.detailContentHeight = bounds.getContentHeight();
    }

    public void tick() {
        InventoryMove.resetInputState();
        super.tick();
    }

    public boolean shouldPause() {
        return false;
    }

    public void renderBackground(DrawContext drawContext, int mouseX, int mouseY, float delta) {
    }

    public void close() {
        RockstarClient.create().getEventBus().unregisterListeners(this);
        moscow.rockstar.modules.config.ModuleConfigurationStore.saveConfiguration();
        super.close();
        Menu.updateMenuState();
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, PointerAction pointerAction) {
        if (this.pendingAssistItem != null) {
            this.pendingAssistItem.setKeyCode(KeyBindingUtil.withCurrentModifiers(pointerAction.getButtonCode()));
            this.pendingAssistItem = null;
            return;
        }
        AssistItemSettingsState settingsState = this.selectedAssistItem == null
            ? null : this.itemSettingsStates.get(this.selectedAssistItem);
        if (settingsState != null && settingsState.isCapturingKeyBinding()) {
            settingsState.assignKeyCode(KeyBindingUtil.withCurrentModifiers(pointerAction.getButtonCode()));
            settingsState.cancelKeyBindingCapture();
            return;
        }
        if (this.searchActive) {
            this.searchField.mouseClicked(mouseX, mouseY, pointerAction);
            if (pointerAction == PointerAction.LEFT_CLICK) {
                if (UiUtils.contains(this.searchHeaderX, this.searchHeaderY, this.searchHeaderWidth,
                    this.searchHeaderHeight, (int)mouseX, (int)mouseY)) {
                    this.searchActive = false;
                    return;
                }
                AssistSearchResult result = this.searchLayout.findAssistItemAt((float)mouseX, (float)mouseY,
                    this.dialogX, this.dialogY, DIALOG_WIDTH, DIALOG_HEIGHT, this.categoryScroll,
                    this.assistGroups, this.searchField, this::isAssistItemAlreadySelected);
                if (result != null) {
                    AssistItemProvider provider = result.getItem().getProvider();
                    if (provider != null && this.getAssistItems().stream().noneMatch(existing ->
                        existing.getSettingKey().equals(provider.getSettingKey()))) {
                        this.getAssistItems().add(provider);
                        this.addAssistItem(provider);
                    }
                }
            }
            return;
        }

        FontMetrics fontMetrics = Font.REGULAR.metrics(7.0f);
        float tabX = this.dialogX + 7.0f;
        float tabY = this.dialogY + 24.0f;
        for (ItemCategory category : ItemCategory.values()) {
            float tabWidth = fontMetrics.measureText(category.getCategoryLabel()) + 8.0f;
            if (UiUtils.contains(tabX, tabY, tabWidth, 13.0f, (int)mouseX, (int)mouseY)) {
                this.selectedCategory = category;
                this.assistItemScroll.reset();
                this.selectedAssistItem = null;
                break;
            }
            tabX += tabWidth + 4.0f;
        }

        AssistItemSelection selection = this.layoutManager.findAssistItemAt((float)mouseX, (float)mouseY,
            this.dialogX, this.dialogY, DIALOG_WIDTH, DIALOG_HEIGHT, this.assistItemScroll,
            this.selectedCategory, this.getActiveAssistItems());
        if (pointerAction == PointerAction.RIGHT_CLICK && selection != null) {
            if (UiUtils.contains(selection.getKeyBindX(), selection.getKeyBindY(), selection.getKeyBindWidth(),
                selection.getKeyBindHeight(), mouseX, mouseY)) {
                selection.getProvider().setKeyCode(-1);
            } else {
                this.openAssistItem(selection.getProvider());
            }
            return;
        }
        if (this.selectedAssistItem != null && UiUtils.contains(this.detailPanelX, this.detailPanelY,
            this.detailPanelWidth, this.detailPanelHeight, mouseX, mouseY)) {
            settingsState = this.itemSettingsStates.get(this.selectedAssistItem);
            if (settingsState != null) {
                if (UiUtils.contains(this.detailContentX, this.detailContentY, this.detailContentWidth,
                    this.detailContentHeight, mouseX, mouseY)) {
                    if (pointerAction == PointerAction.LEFT_CLICK) {
                        settingsState.beginKeyBindingCapture();
                        return;
                    }
                    if (pointerAction == PointerAction.RIGHT_CLICK) {
                        settingsState.clearKeyBinding();
                        settingsState.cancelKeyBindingCapture();
                        return;
                    }
                }
                for (SettingComponent component : settingsState.getSettingComponents()) {
                    component.mouseClicked(mouseX, mouseY, pointerAction);
                }
            }
            return;
        }
        if (selection != null) {
            if (UiUtils.contains(selection.getKeyBindX(), selection.getKeyBindY(), selection.getKeyBindWidth(),
                selection.getKeyBindHeight(), mouseX, mouseY)) {
                if (pointerAction == PointerAction.LEFT_CLICK) {
                    this.pendingAssistItem = selection.getProvider();
                } else if (pointerAction != PointerAction.RIGHT_CLICK) {
                    selection.getProvider().setKeyCode(KeyBindingUtil.withCurrentModifiers(pointerAction.getButtonCode()));
                }
                return;
            }
            if (pointerAction == PointerAction.LEFT_CLICK) {
                this.openAssistItem(selection.getProvider());
            }
            return;
        }
        if (pointerAction == PointerAction.LEFT_CLICK && UiUtils.contains(this.categoryTabsX, this.categoryTabsY,
            this.categoryTabsWidth, this.categoryTabsHeight, (int)mouseX, (int)mouseY)) {
            this.selectedAssistItem = null;
            this.searchActive = true;
            this.categoryScroll.reset();
            this.searchField.setText("");
            this.rebuildAssistGroups();
            return;
        }
        super.onMouseClicked(mouseX, mouseY, pointerAction);
    }

    @Override
    public void onMouseReleased(double mouseX, double mouseY, PointerAction pointerAction) {
        if (this.searchActive) {
            this.searchField.mouseReleased(mouseX, mouseY, pointerAction);
        }
        if (this.selectedAssistItem != null) {
            AssistItemSettingsState settingsState = this.itemSettingsStates.get(this.selectedAssistItem);
            if (settingsState != null) {
                for (SettingComponent component : settingsState.getSettingComponents()) {
                    component.mouseReleased(mouseX, mouseY, pointerAction);
                }
            }
        }
        super.onMouseReleased(mouseX, mouseY, pointerAction);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.searchActive) {
            this.categoryScroll.scrollBy((float)verticalAmount);
            return true;
        }
        if (UiUtils.contains(this.dialogX, this.dialogY, DIALOG_WIDTH, DIALOG_HEIGHT,
            (int)mouseX, (int)mouseY)) {
            this.assistItemScroll.scrollBy((float)verticalAmount);
            return true;
        }
        if (this.selectedAssistItem != null && UiUtils.contains(this.detailPanelX, this.detailPanelY,
            this.detailPanelWidth, this.detailPanelHeight, mouseX, mouseY)) {
            AssistItemSettingsState settingsState = this.itemSettingsStates.get(this.selectedAssistItem);
            if (settingsState != null) {
                for (SettingComponent component : settingsState.getSettingComponents()) {
                    component.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
                }
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.pendingAssistItem == null && !this.isDetailPanelActive()) {
            if (Screen.hasControlDown() && keyCode == 90 && SettingSnapshotCache.isCollectionCacheReady()) {
                return true;
            }
            if (Screen.hasControlDown() && keyCode == 89 && SettingSnapshotCache.isCollectionProcessorCacheTargetReady()) {
                return true;
            }
        }
        if (this.pendingAssistItem != null) {
            if (keyCode == 256) {
                this.pendingAssistItem = null;
            } else if (keyCode == 261) {
                this.pendingAssistItem.setKeyCode(-1);
                this.pendingAssistItem = null;
            } else {
                int encodedKey = KeyBindingUtil.encodeKeyPress(keyCode, modifiers);
                if (encodedKey == Integer.MIN_VALUE) {
                    return true;
                }
                this.pendingAssistItem.setKeyCode(encodedKey);
                this.pendingAssistItem = null;
            }
            return true;
        }
        if (this.searchActive) {
            if (this.searchField.isFocused()) {
                if (keyCode == 256) {
                    this.searchField.setFocused(false);
                    return true;
                }
                this.searchField.keyPressed(keyCode, scanCode, modifiers);
                return true;
            }
            if (keyCode == 256) {
                this.searchActive = false;
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (this.selectedAssistItem != null) {
            AssistItemSettingsState settingsState = this.itemSettingsStates.get(this.selectedAssistItem);
            if (settingsState != null && settingsState.isCapturingKeyBinding()) {
                if (keyCode == 256) {
                    settingsState.cancelKeyBindingCapture();
                } else if (keyCode == 261) {
                    settingsState.clearKeyBinding();
                    settingsState.cancelKeyBindingCapture();
                } else {
                    int encodedKey = KeyBindingUtil.encodeKeyPress(keyCode, modifiers);
                    if (encodedKey == Integer.MIN_VALUE) {
                        return true;
                    }
                    settingsState.assignKeyCode(encodedKey);
                    settingsState.cancelKeyBindingCapture();
                }
                return true;
            }
            if (settingsState != null) {
                for (SettingComponent component : settingsState.getSettingComponents()) {
                    component.keyPressed(keyCode, scanCode, modifiers);
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        int encodedKey = KeyBindingUtil.encodeKeyRelease(keyCode, modifiers);
        if (encodedKey != Integer.MIN_VALUE) {
            if (this.pendingAssistItem != null) {
                this.pendingAssistItem.setKeyCode(encodedKey);
                this.pendingAssistItem = null;
                return true;
            }
            if (this.selectedAssistItem != null) {
                AssistItemSettingsState settingsState = this.itemSettingsStates.get(this.selectedAssistItem);
                if (settingsState != null && settingsState.isCapturingKeyBinding()) {
                    settingsState.assignKeyCode(encodedKey);
                    settingsState.cancelKeyBindingCapture();
                    return true;
                }
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private boolean isDetailPanelActive() {
        if (this.selectedAssistItem == null) {
            return false;
        }
        AssistItemSettingsState settingsState = this.itemSettingsStates.get(this.selectedAssistItem);
        return settingsState != null && settingsState.isCapturingKeyBinding();
    }

    public boolean charTyped(char character, int modifiers) {
        if (this.searchActive && this.searchField.charTyped(character, modifiers)) {
            return true;
        }
        if (this.selectedAssistItem != null) {
            AssistItemSettingsState settingsState = this.itemSettingsStates.get(this.selectedAssistItem);
            if (settingsState != null) {
                for (SettingComponent component : settingsState.getSettingComponents()) {
                    if (component.charTyped(character, modifiers)) {
                        return true;
                    }
                }
            }
        }
        return super.charTyped(character, modifiers);
    }

    private List<AssistItemProvider> getActiveAssistItems() {
        ArrayList<AssistItemProvider> activeProviders = new ArrayList<>(this.getAssistItems());
        activeProviders.removeIf(provider -> !provider.isAvailable());
        return activeProviders;
    }

    private boolean isAssistItemAlreadySelected(AssistItem assistItem) {
        AssistItemProvider provider = assistItem.getProvider();
        return provider != null && this.getAssistItems().stream()
            .anyMatch(selected -> selected.getSettingKey().equals(provider.getSettingKey()));
    }

    private void rebuildAssistGroups() {
        this.assistGroups.clear();
        Assist assist = RockstarClient.create().getModuleRegistry().getModule(Assist.class);
        List<AssistItemProvider> providers = assist == null ? List.of() : assist.getAssistSettings();
        for (ItemCategory category : ItemCategory.values()) {
            if (category == ItemCategory.ALL) {
                continue;
            }
            ArrayList<AssistItem> items = new ArrayList<>();
            for (AssistItemProvider provider : providers) {
                if (provider.getCategory() != category || !provider.isAvailable()) {
                    continue;
                }
                items.add(new AssistItem(provider, provider.getDisplayName(), provider.getItemStack(), provider.getKeyCode()));
            }
            if (!items.isEmpty()) {
                this.assistGroups.add(new AssistGroup(category.getCategoryLabel(), items));
            }
        }
    }

    private void openAssistItem(AssistItemProvider provider) {
        this.selectedAssistItem = provider;
        this.itemSettingsStates.computeIfAbsent(provider,
            item -> new AssistItemSettingsState(item, () -> this.startItemRemoval(item)));
    }

    private void startItemRemoval(AssistItemProvider provider) {
        if (provider == null) {
            return;
        }
        Animation animation = this.removalAnimations.computeIfAbsent(provider,
            ignored -> new Animation(200L, 1.0f, Easing.easeInOutCubicBezier));
        animation.setValue(1.0f);
        animation.update(0.0f);
    }

    private void removeFinishedItems() {
        if (this.removalAnimations.isEmpty()) {
            return;
        }
        ArrayList<AssistItemProvider> removedProviders = new ArrayList<>();
        for (Map.Entry<AssistItemProvider, Animation> entry : this.removalAnimations.entrySet()) {
            Animation animation = entry.getValue();
            animation.update(0.0f);
            if (animation.isAtTarget() && animation.getValue() <= 0.01f) {
                removedProviders.add(entry.getKey());
            }
        }
        for (AssistItemProvider provider : removedProviders) {
            this.getAssistItems().remove(provider);
            this.itemSettingsStates.remove(provider);
            this.itemAnimations.remove(provider);
            this.labelScrollStates.remove(provider);
            this.removalAnimations.remove(provider);
            this.additionAnimations.remove(provider);
            this.selectionAnimations.remove(provider);
            if (this.selectedAssistItem == provider) {
                this.selectedAssistItem = null;
                NumberSettingComponent.clearInteractionTargets();
            }
        }
    }

    private float getRemovalProgress(AssistItemProvider provider) {
        Animation animation = this.removalAnimations.get(provider);
        return animation == null ? 1.0f : animation.getValue();
    }

    private void finishItemAdditions() {
        if (this.additionAnimations.isEmpty()) {
            return;
        }
        ArrayList<AssistItemProvider> addedProviders = new ArrayList<>();
        for (Map.Entry<AssistItemProvider, Animation> entry : this.additionAnimations.entrySet()) {
            Animation animation = entry.getValue();
            animation.update(1.0f);
            if (animation.isAtTarget() && animation.getValue() >= 0.99f) {
                addedProviders.add(entry.getKey());
            }
        }
        for (AssistItemProvider provider : addedProviders) {
            this.additionAnimations.remove(provider);
        }
    }

    private float getAdditionProgress(AssistItemProvider provider) {
        Animation animation = this.additionAnimations.get(provider);
        return animation == null ? 1.0f : animation.getValue();
    }

    public void addAssistItem(AssistItemProvider provider) {
        Animation animation = new Animation(300L, 0.0f, Easing.easeInOutCubicPolynomial);
        this.additionAnimations.put(provider, animation);
        this.rebuildAssistGroups();
    }
}
