/*
 * Port of the original dropdown-mode menu screen: rockstar/ilIlil/IiiIiIiii.
 *
 * The original extends rockstar/ilIlil/IIiI (ColorPickerHost) DIRECTLY - it is
 * NOT a child of rockstar/ilIlil/IiiIIiiiI (MenuScreenBase) - and implements
 * rockstar/ilIlil/IiiIiIIiI (OverlayElement). It declares its own `closing`
 * flag (field `II Z`), which is why this class does not extend MenuScreenBase.
 */
package moscow.rockstar.ui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleContract;
import moscow.rockstar.modules.ModuleRegistry;
import moscow.rockstar.modules.config.ModuleConfigurationStore;
import moscow.rockstar.modules.player.inventory.InventoryMove;
import moscow.rockstar.modules.visuals.audio.Sounds;
import moscow.rockstar.modules.visuals.menu.Menu;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.overlay.OverlayElement;
import moscow.rockstar.render.target.RenderTarget;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.RangeSetting;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.animation.Transition;
import moscow.rockstar.ui.color.ColorPickerHost;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.InteractiveComponent;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.KeyBindingUtil;
import moscow.rockstar.ui.input.KeyDisplayFormatter;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.layout.JustifyContent;
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.settings.SettingPanel;
import moscow.rockstar.ui.settings.SettingSnapshotCache;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.TextComponent;
import moscow.rockstar.ui.tooltip.HudTooltipWindow;
import moscow.rockstar.ui.widgets.controls.ScrollBar;
import moscow.rockstar.ui.widgets.settings.KeyBindingControl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import pyrock.events.render.HudRenderEvent;
import pyrock.events.render.MenuRenderEvent;
import pyrock.events.render.PostMenuRenderEvent;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

public class DropdownMenuScreen
extends ColorPickerHost
implements OverlayElement {
    private static final Motion ROW_MOTION = Motion.resolveMotionMotionFromLongAndEasing(140L, Easing.easeOutCubic);
    private static final long SIGNAL_DURATION = 140L;
    private static final Easing SIGNAL_EASING = Easing.easeOutCubic;
    private static final ColorRGBA SHAKE_COLOR = new ColorRGBA(255.0f, 120.0f, 120.0f);
    private static final float OPEN_FADE_MILLIS = 300.0f;
    private static final float HIDE_FADE_MILLIS = 300.0f;
    private static final float CLOSE_DURATION_MILLIS = 1600.0f;
    private static final float CLOSE_FIRST_PHASE = 0.4f;
    private IntConsumer historyNavigator;
    private SettingPanel settingPanel;
    private final Map<ModuleContract, UiNode> moduleComponents = new HashMap<ModuleContract, UiNode>();
    private final Map<Setting, UiNode> settingComponents = new HashMap<Setting, UiNode>();
    private final Map<ModuleCategory, Component> categoryPanels = new HashMap<ModuleCategory, Component>();
    private final Map<ModuleCategory, Float> savedScrollOffsets = new HashMap<ModuleCategory, Float>();
    private final Map<ModuleCategory, float[]> scrollRestores = new HashMap<ModuleCategory, float[]>();
    private final Map<ModuleContract, ModuleRow> moduleRows = new HashMap<ModuleContract, ModuleRow>();
    private final Map<ModuleCategory, ModuleContract[]> expandedModules = new HashMap<ModuleCategory, ModuleContract[]>();
    private final Map<ModuleCategory, UiNode> categorySpacers = new HashMap<ModuleCategory, UiNode>();
    private final Map<ModuleCategory, List<ModuleContract>> categoryModules = new HashMap<ModuleCategory, List<ModuleContract>>();
    private final Map<ModuleContract, List<Setting>> moduleSettings = new HashMap<ModuleContract, List<Setting>>();
    private boolean listDirty;
    private int moduleRegistryVersion;
    private Runnable historyRecorder;
    private UiNode highlightNode;
    private long highlightDeadline;
    private PendingReveal pendingReveal;
    private ModuleContract keybindTargetModule;
    private long openTime;
    private float hideProgress;
    private long lastFrameTime;
    private boolean initialized;
    private boolean closing;
    private long closeTime;
    Vec3d cameraPosition;
    private Vec3d viewDirection;
    Vec3d upDirection;
    Vec3d rightDirection;
    static DropdownMenuScreen INSTANCE;
    private final List<TooltipEntry> tooltipEntries = new ArrayList<TooltipEntry>();
    private final HudTooltipWindow tooltip = new HudTooltipWindow(Font.REGULAR.metrics(10.0f), 10.0f, 300L, Easing.easeOutBack).build();
    static final RenderTarget uiRenderTarget = new RenderTarget(false).enableLinearFiltering();
    static boolean panelCaptured;
    static boolean worldQuadDrawn;

    @Override
    protected boolean lowDrawBatching() {
        return true;
    }

    @Override
    @Compile(obfuscation=1)
    public void onMouseClicked(double d, double d2, PointerAction pointerAction) {
        if (KeyBindingControl.handlePointerAction(pointerAction)) {
            this.saveClientConfiguration();
            return;
        }
        if (this.keybindTargetModule != null) {
            if (pointerAction == PointerAction.LEFT_CLICK) {
                this.keybindTargetModule = null;
                return;
            }
            if (pointerAction != PointerAction.MIDDLE_CLICK) {
                this.keybindTargetModule.setKeyBind(KeyBindingUtil.withCurrentModifiers(pointerAction.getButtonCode()));
                this.keybindTargetModule = null;
                this.saveClientConfiguration();
                return;
            }
        }
        if (this.historyNavigator != null && (pointerAction == PointerAction.BUTTON_4 || pointerAction == PointerAction.BUTTON_5)) {
            this.historyNavigator.accept(pointerAction == PointerAction.BUTTON_4 ? -1 : 1);
            return;
        }
        if (this.settingPanel != null && this.overlays.stream().noneMatch(uiNode -> uiNode.alive() && uiNode.contains((float)d, (float)d2))) {
            this.settingPanel.handleOutsideClick((float)d, (float)d2);
        }
        super.onMouseClicked(d, d2, pointerAction);
    }

    @Override
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
                this.saveClientConfiguration();
                return true;
            }
            int n4 = KeyBindingUtil.encodeKeyPress(n, n3);
            if (n4 == Integer.MIN_VALUE) {
                return true;
            }
            this.keybindTargetModule.setKeyBind(n4);
            this.keybindTargetModule = null;
            this.saveClientConfiguration();
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

    @Override
    public boolean keyReleased(int n, int n2, int n3) {
        int n4;
        if (this.keybindTargetModule != null && (n4 = KeyBindingUtil.encodeKeyRelease(n, n3)) != Integer.MIN_VALUE) {
            this.keybindTargetModule.setKeyBind(n4);
            this.keybindTargetModule = null;
            this.saveClientConfiguration();
            return true;
        }
        return super.keyReleased(n, n2, n3);
    }

    private void saveClientConfiguration() {
        ModuleConfigurationStore.saveConfiguration();
    }

    @Override
    @Compile(obfuscation=1)
    public void render(RockstarDrawContext drawContext) {
        this.updatePanelAnimation();
        this.updateHoverSpotlight();
        InventoryMove.resetInputState();
        if (!this.closing) {
            this.refreshLists();
        }
        float f = this.closing ? 1.0f : 0.7f + 0.3f * this.openProgress();
        boolean bl = Math.abs(f - 1.0f) > 1.0E-4f;
        if (bl) {
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate((float)this.width / 2.0f, (float)this.height / 2.0f, 0.0f);
            drawContext.getMatrices().scale(f, f, 1.0f);
            drawContext.getMatrices().translate((float)(-this.width) / 2.0f, (float)(-this.height) / 2.0f, 0.0f);
        }
        // ORIGINAL: rockstar/ilIlil/IiiIiIIIi.I(this, ctx) - inlined here exactly as
        // ModuleSettingsScreen.render already inlines it in this tree.
        MinecraftClient client = MinecraftClient.getInstance();
        boolean capture = this.isClosing() && (client == null || client.currentScreen == null);
        WidgetBatchRenderer.flushCurrentBatch();
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
        return "panel";
    }

    @Override
    public float getOpenProgress() {
        return this.openProgress();
    }

    @Override
    public float getClosingProgress() {
        return this.closing ? this.closeProgress() : 0.0f;
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
        return this.closing ? 1.0f : 0.7f + 0.3f * this.openProgress();
    }

    @Override
    public List<OverlayElement.OverlayBounds> getOverlayBounds() {
        ArrayList<OverlayElement.OverlayBounds> arrayList = new ArrayList<OverlayElement.OverlayBounds>(this.categoryPanels.size());
        for (ModuleCategory moduleCategory : ModuleCategory.values()) {
            Component component = this.categoryPanels.get(moduleCategory);
            if (component == null) continue;
            arrayList.add(new OverlayElement.OverlayBounds(moduleCategory.name().toLowerCase(Locale.ROOT), component.x(), component.y(), component.w(), component.h()));
        }
        return arrayList;
    }

    public static DropdownMenuScreen getInstance() {
        return INSTANCE;
    }

    private float openProgress() {
        float f = Math.min(1.0f, Math.max(0.0f, (float)(System.currentTimeMillis() - this.openTime) / 300.0f));
        return Easing.easeOutBack.ease(f, 0.0f, 1.0f, 1.0f);
    }

    private void updatePanelAnimation() {
        long l = System.currentTimeMillis();
        if (this.closing) {
            this.hideProgress = 0.0f;
            this.lastFrameTime = l;
            this.contentAlpha = 1.0f;
            return;
        }
        float f = this.lastFrameTime == 0L ? 16.0f : Math.min(64.0f, (float)(l - this.lastFrameTime));
        this.lastFrameTime = l;
        int n = RockstarClient.create().getModuleRegistry().getModule(Menu.class).getHideKeySetting().getValue();
        float f2 = DropdownMenuScreen.isHideKeyPressed(n) ? 1.0f : 0.0f;
        float f3 = f / 300.0f;
        if (this.hideProgress < f2) {
            this.hideProgress = Math.min(f2, this.hideProgress + f3);
        } else if (this.hideProgress > f2) {
            this.hideProgress = Math.max(f2, this.hideProgress - f3);
        }
        this.contentAlpha = 1.0f - Easing.easeInOutCubicBezier.ease(this.hideProgress, 0.0f, 1.0f, 1.0f);
    }

    private void updateHoverSpotlight() {
        if (this.closing || this.contentAlpha >= 0.999f) {
            UiNode.spotlight(null);
            return;
        }
        long l = MinecraftClient.getInstance().getWindow().getHandle();
        boolean bl = GLFW.glfwGetMouseButton((long)l, (int)0) == 1;
        if (bl && UiNode.spotlight() != null) {
            return;
        }
        UiNode uiNode = null;
        for (Map.Entry<Setting, UiNode> entry : this.settingComponents.entrySet()) {
            UiNode uiNode2 = entry.getValue();
            if (!uiNode2.inFlow() || !uiNode2.hovered() || !DropdownMenuScreen.isSpotlightSetting(entry.getKey())) continue;
            uiNode = uiNode2;
            break;
        }
        UiNode.spotlight(uiNode);
    }

    private static boolean isHideKeyPressed(int n) {
        return KeyBindingUtil.isPressed(n);
    }

    @Override
    public void removed() {
        this.closeMenu();
        super.removed();
    }

    @Compile(obfuscation=1)
    public void closeMenu() {
        if (this.closing) {
            return;
        }
        this.closing = true;
        UiNode.spotlight(null);
        this.saveClientConfiguration();
        this.closeTime = System.currentTimeMillis();
        panelCaptured = false;
        worldQuadDrawn = false;
        MinecraftClient client = MinecraftClient.getInstance();
        Camera camera = client.gameRenderer.getCamera();
        if (camera != null && client.player != null) {
            Vec3d direction;
            double d = Math.toRadians(camera.getYaw());
            double d2 = Math.toRadians(camera.getPitch());
            this.viewDirection = direction = new Vec3d(-Math.sin(d) * Math.cos(d2), -Math.sin(d2), Math.cos(d) * Math.cos(d2)).normalize();
            this.upDirection = direction.crossProduct(new Vec3d(0.0, 1.0, 0.0)).normalize();
            this.rightDirection = this.upDirection.crossProduct(direction).normalize();
            this.cameraPosition = camera.getPos().add(direction.multiply(1.5));
        }
        INSTANCE = this;
    }

    float closeProgress() {
        return Math.min(1.0f, Math.max(0.0f, (float)(System.currentTimeMillis() - this.closeTime) / 1600.0f));
    }

    static void onHudRender(HudRenderEvent hudRenderEvent) {
        MinecraftClient client = MinecraftClient.getInstance();
        float f = client.getWindow().getScaledWidth();
        float f2 = client.getWindow().getScaledHeight();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, uiRenderTarget.getColorAttachment());
        Matrix4f matrix4f = hudRenderEvent.getContext().getMatrices().peek().getPositionMatrix();
        int n = ColorRGBA.WHITE.getRGB();
        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(matrix4f, 0.0f, 0.0f, 0.0f).texture(0.0f, 1.0f).color(n);
        buffer.vertex(matrix4f, 0.0f, f2, 0.0f).texture(0.0f, 0.0f).color(n);
        buffer.vertex(matrix4f, f, f2, 0.0f).texture(1.0f, 0.0f).color(n);
        buffer.vertex(matrix4f, f, 0.0f, 0.0f).texture(1.0f, 1.0f).color(n);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    @Compile(obfuscation=4)
    protected void init() {
        super.init();
        this.openTime = System.currentTimeMillis();
        this.closing = false;
        this.keybindTargetModule = null;
        KeyBindingControl.cancelActiveControl();
        this.hideProgress = 0.0f;
        this.lastFrameTime = 0L;
        this.contentAlpha = 1.0f;
        this.listDirty = true;
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        this.clearRoots();
        this.moduleComponents.clear();
        this.settingComponents.clear();
        this.categoryPanels.clear();
        this.moduleRows.clear();
        this.expandedModules.clear();
        this.categorySpacers.clear();
        this.categoryModules.clear();
        this.moduleSettings.clear();
        this.tooltipEntries.clear();
        this.highlightNode = null;
        this.pendingReveal = null;
        HashMap<Component, ModuleCategory> categoryByPanel = new HashMap<Component, ModuleCategory>();
        Component root = new Component().layout(Layout.ROW).gap(10.0f).height(243.0f).center().renderHook((drawContext, component) -> {
            for (UiNode uiNode : component.children()) {
                drawContext.drawShadow(uiNode.x(), uiNode.y(), uiNode.w(), uiNode.h(), 25.0f, WidgetState.uniform(11.0f), ColorPalette.BLACK.mulAlpha(0.15f));
            }
            for (UiNode uiNode : component.children()) {
                drawContext.drawBlurredRect(uiNode.x(), uiNode.y(), uiNode.w(), uiNode.h(), 5.0f, 3.0f, WidgetState.uniform(11.0f), ColorPalette.WHITE);
            }
            for (UiNode uiNode : component.children()) {
                if (!(uiNode instanceof Component)) continue;
                Component panel = (Component)uiNode;
                ModuleCategory moduleCategory = categoryByPanel.get(panel);
                if (moduleCategory == null) continue;
                this.drawCategoryPanel(drawContext, panel, moduleCategory);
            }
        });
        ArrayList<ModuleContract[]> expandedSlots = new ArrayList<ModuleContract[]>();
        ArrayList<ModuleContract[]> history = new ArrayList<ModuleContract[]>();
        int[] historyIndex = new int[]{-1};
        this.historyRecorder = () -> {
            this.listDirty = true;
            ModuleContract[] snapshot = new ModuleContract[expandedSlots.size()];
            for (int i = 0; i < expandedSlots.size(); ++i) {
                snapshot[i] = expandedSlots.get(i)[0];
            }
            while (history.size() > historyIndex[0] + 1) {
                history.remove(history.size() - 1);
            }
            history.add(snapshot);
            historyIndex[0] = history.size() - 1;
        };
        for (ModuleCategory category : ModuleCategory.values()) {
            Component panel = new Component().width(115.0f).configureLayoutState(DropdownMenuScreen::configureScrollBar).padding(Insets.of(25.0f, 0.0f, 1.0f, 0.0f)).scrollable().fillHeight();
            ModuleContract[] slot = new ModuleContract[]{null};
            expandedSlots.add(slot);
            this.expandedModules.put(category, slot);
            this.categoryPanels.put(category, panel);
            categoryByPanel.put(panel, category);
            List<ModuleContract> modules = RockstarClient.create().getModuleRegistry().getModules().stream().sorted(Comparator.comparing(ModuleContract::getName)).filter(moduleContract -> moduleContract.getCategory() == category && moduleContract.isAvailable()).toList();
            TextComponent spacer = new TextComponent().height(2.0f).visibleWhen(() -> slot[0] == null);
            this.categorySpacers.put(category, spacer);
            panel.add(spacer);
            for (ModuleContract module : modules) {
                ModuleRow row = this.createModuleRow(category, module);
                this.moduleRows.put(module, row);
                panel.add(row.header());
                panel.add(row.divider());
                panel.add(row.settings());
            }
            this.categoryModules.put(category, modules);
            root.add(panel);
        }
        this.historyRecorder.run();
        this.historyNavigator = n -> {
            int n2 = historyIndex[0] + n;
            if (n2 < 0 || n2 >= history.size()) {
                return;
            }
            historyIndex[0] = n2;
            this.listDirty = true;
            ModuleContract[] snapshot = history.get(n2);
            for (int i = 0; i < expandedSlots.size(); ++i) {
                expandedSlots.get(i)[0] = snapshot[i];
            }
        };
        BiConsumer<ModuleContract, Setting> biConsumer = (moduleContract, setting) -> {
            if (!this.expandedModules.containsKey(moduleContract.getCategory())) {
                return;
            }
            this.expandCategoryModule(moduleContract.getCategory(), moduleContract);
            this.pendingReveal = new PendingReveal(moduleContract, setting);
        };
        this.settingPanel = new SettingPanel(root, biConsumer);
        this.add(root);
        this.add(this.settingPanel);
        this.add(this.settingPanel.getKeybindList());
        this.add(this.settingPanel.getSettingsContainer());
    }

    @Compile(obfuscation=1)
    private ModuleRow createModuleRow(ModuleCategory moduleCategory, ModuleContract moduleContract) {
        ModuleContract[] slot = this.expandedModules.get(moduleCategory);
        TextComponent label = new TextComponent().text(Font.REGULAR.metrics(8.0f), () -> this.getModuleLabel(moduleContract), DropdownMenuScreen::moduleLabelColor).bind("enabled", moduleContract::isEnabled, 140L).bind("open", () -> slot[0] == moduleContract, 140L).height(6.0f).animatePosition().motion(ROW_MOTION).interactive(false);
        TextComponent backIcon = new TextComponent().size(18.0f, 18.0f).icon("back", 6.0f, ColorPalette.PRIMARY_TEXT_COLOR).cursor(Cursor.HAND).onClick(() -> this.collapseCategory(moduleCategory)).visibleWhen(() -> slot[0] == moduleContract);
        TextComponent checkIcon = new TextComponent().size(6.0f, 6.0f).icon("check", 6.0f, DropdownMenuScreen::checkIconColor).bind("enabled", () -> moduleContract.isEnabled() && this.keybindTargetModule != moduleContract, 140L).visibleWhen(() -> slot[0] != moduleContract).interactive(false);
        TextComponent toggle = new InteractiveComponent(moduleContract::isEnabled).setActiveColorProvider(() -> ColorPalette.MUTED_PANEL_COLOR).size(13.0f, 8.0f).onClick(moduleContract::toggle).visibleWhen(() -> slot[0] == moduleContract);
        Component settings = new Component().vertical().fillWidth().motion(ROW_MOTION).snapPosition().enter(Transition.slideUp(20.0f));
        settings.visibleWhen(() -> slot[0] == moduleContract);
        Component header = new Component().layout(Layout.ROW).alignment(Alignment.CENTER).overflowMode(JustifyContent.SPACE_BETWEEN).padding(Insets.of(0.0f, 9.0f, 0.0f, 9.0f)).height(18.0f).cursor(Cursor.HAND).add(new Component().layout(Layout.ROW).alignment(Alignment.CENTER).gap(-9.0f).add(backIcon).add(label)).add(checkIcon).add(toggle);
        header.onClick((pointerAction, f, f2) -> this.onModuleRowClick(moduleContract, slot, label, moduleCategory, header, pointerAction, f, f2));
        header.fillWidth().visibleWhen(() -> slot[0] == null || slot[0] == moduleContract).snapPosition().sticky(() -> slot[0] == moduleContract);
        TextComponent divider = new TextComponent().fillWidth().height(1.0f).background(ColorPalette.BORDER_COLOR).visibleWhen(() -> slot[0] == moduleContract).snapPosition().sticky(() -> slot[0] == moduleContract);
        this.moduleComponents.put(moduleContract, header);
        this.tooltipEntries.add(new TooltipEntry(header, moduleContract::getDescription));
        return new ModuleRow(header, divider, settings);
    }

    private void onModuleRowClick(ModuleContract moduleContract, ModuleContract[] slot, TextComponent label, ModuleCategory moduleCategory, Component header, PointerAction pointerAction, float f, float f2) {
        if (pointerAction == PointerAction.MIDDLE_CLICK) {
            this.keybindTargetModule = this.keybindTargetModule == moduleContract ? null : moduleContract;
            return;
        }
        if (pointerAction == PointerAction.RIGHT_CLICK) {
            if (slot[0] == moduleContract) {
                return;
            }
            if (moduleContract.getSettings().isEmpty()) {
                this.shakeNode(label);
            } else {
                this.expandCategoryModule(moduleCategory, moduleContract);
            }
            return;
        }
        if (pointerAction != PointerAction.LEFT_CLICK) {
            return;
        }
        if (slot[0] == moduleContract) {
            if (f < header.x() + header.w() / 2.0f) {
                this.collapseCategory(moduleCategory);
            } else {
                moduleContract.toggle();
            }
        } else {
            moduleContract.toggle();
        }
    }

    private String getModuleLabel(ModuleContract moduleContract) {
        if (this.keybindTargetModule != moduleContract) {
            return moduleContract.getName();
        }
        int n = KeyBindingUtil.currentModifiers();
        if (n != 0) {
            return Localization.translate("key") + ": " + KeyBindingUtil.modifierPrefix(n) + "...";
        }
        int n2 = moduleContract.getKeyBind();
        return n2 == -1 ? Localization.translate("menu.binding") : Localization.translate("key") + ": " + KeyDisplayFormatter.formatKey(n2);
    }

    private static ColorRGBA moduleLabelColor(TextComponent textComponent) {
        ColorRGBA colorRGBA = ColorPalette.PRIMARY_TEXT_COLOR.mix(ColorPalette.ACCENT_COLOR, textComponent.sig("enabled", SIGNAL_EASING) * (1.0f - textComponent.sig("open", SIGNAL_EASING))).mulAlpha(0.75f + 0.25f * textComponent.sig("enabled", SIGNAL_EASING));
        float f = textComponent.shakeAmount();
        return f > 0.0f ? colorRGBA.mix(SHAKE_COLOR.withAlpha(colorRGBA.getAlpha()), f) : colorRGBA;
    }

    private static ColorRGBA checkIconColor(TextComponent textComponent) {
        return ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(textComponent.sig("enabled", SIGNAL_EASING));
    }

    private static void configureScrollBar(ScrollBar scrollBar2) {
        scrollBar2.offset(3.0f).padding(1.0f, 6.0f).thickness(2.5f).thumbColor(scrollBar -> ColorRGBA.BLACK.mix(ColorRGBA.WHITE, 0.3f).withAlpha(255.0f * (0.32f + 0.28f * scrollBar.hoverProgress() + 0.3f * scrollBar.dragProgress())));
    }

    @Compile(obfuscation=1)
    private void refreshLists() {
        if (!this.initialized) {
            return;
        }
        int n = ModuleRegistry.getRegistryInstanceCount();
        if (n != this.moduleRegistryVersion) {
            this.moduleRegistryVersion = n;
            this.listDirty = true;
        }
        if (!this.listDirty) {
            return;
        }
        this.listDirty = false;
        ModuleRegistry moduleRegistry = RockstarClient.create().getModuleRegistry();
        if (this.keybindTargetModule != null && !moduleRegistry.getModules().contains(this.keybindTargetModule)) {
            this.keybindTargetModule = null;
        }
        for (ModuleCategory category : ModuleCategory.values()) {
            List<ModuleContract> previous;
            Component panel = this.categoryPanels.get(category);
            ModuleContract[] slot = this.expandedModules.get(category);
            if (panel == null || slot == null) continue;
            List<ModuleContract> modules = moduleRegistry.getModules().stream().sorted(Comparator.comparing(ModuleContract::getName)).filter(moduleContract -> moduleContract.getCategory() == category && moduleContract.isAvailable()).toList();
            if (!DropdownMenuScreen.sameOrder(this.categoryModules.get(category), modules)) {
                if (slot[0] != null && !modules.contains(slot[0])) {
                    ModuleContract replacement = DropdownMenuScreen.findByName(modules, slot[0]);
                    slot[0] = replacement;
                    if (replacement == null) {
                        this.rememberScroll(category);
                    }
                }
                if ((previous = this.categoryModules.get(category)) != null) {
                    for (ModuleContract stale : previous) {
                        if (modules.contains(stale)) continue;
                        ModuleRow row = this.moduleRows.get(stale);
                        if (row != null) {
                            panel.children().removeAll(List.of(row.header(), row.divider(), row.settings()));
                        }
                        this.removeModuleEntries(stale);
                    }
                }
                ArrayList<UiNode> rebuilt = new ArrayList<UiNode>();
                UiNode spacer = this.categorySpacers.get(category);
                if (spacer != null) {
                    rebuilt.add(spacer);
                }
                for (ModuleContract module : modules) {
                    ModuleRow row = this.moduleRows.computeIfAbsent(module, moduleContract -> this.createModuleRow(category, moduleContract));
                    rebuilt.add(row.header());
                    rebuilt.add(row.divider());
                    rebuilt.add(row.settings());
                }
                panel.updateChildren(rebuilt);
                this.categoryModules.put(category, modules);
            }
            if (slot[0] == null) continue;
            ModuleRow expandedRow = this.moduleRows.get(slot[0]);
            if (expandedRow == null) continue;
            this.syncSettings(slot[0], expandedRow.settings());
        }
    }

    @Compile(obfuscation=1)
    private void syncSettings(ModuleContract moduleContract, Component component) {
        List<Setting> settings = moduleContract.getSettings();
        List<Setting> previous = this.moduleSettings.get(moduleContract);
        if (DropdownMenuScreen.sameOrder(previous, settings)) {
            return;
        }
        ArrayList<UiNode> rebuilt = new ArrayList<UiNode>();
        for (Setting setting : settings) {
            UiNode existing = this.settingComponents.get(setting);
            if (existing == null) {
                Component built = DropdownMenuScreen.createSettingComponent(setting);
                this.settingComponents.put(setting, built);
                this.tooltipEntries.add(new TooltipEntry(built, () -> Localization.translateOrBlank(setting.getDescriptionKey())));
                existing = built;
            }
            rebuilt.add(existing);
        }
        if (previous != null) {
            for (Setting setting : previous) {
                if (settings.contains(setting)) continue;
                UiNode removed = this.settingComponents.remove(setting);
                if (removed == null) continue;
                component.children().remove(removed);
                this.tooltipEntries.removeIf(tooltipEntry -> tooltipEntry.view() == removed);
            }
        }
        component.updateChildren(rebuilt);
        this.moduleSettings.put(moduleContract, new ArrayList<Setting>(settings));
    }

    @Compile(obfuscation=1)
    private void removeModuleEntries(ModuleContract moduleContract) {
        List<Setting> settings;
        ModuleRow row = this.moduleRows.remove(moduleContract);
        this.moduleComponents.remove(moduleContract);
        if (row != null) {
            this.tooltipEntries.removeIf(tooltipEntry -> tooltipEntry.view() == row.header());
        }
        if ((settings = this.moduleSettings.remove(moduleContract)) != null) {
            for (Setting setting : settings) {
                UiNode uiNode = this.settingComponents.remove(setting);
                if (uiNode == null) continue;
                this.tooltipEntries.removeIf(tooltipEntry -> tooltipEntry.view() == uiNode);
            }
        }
    }

    private static ModuleContract findByName(List<ModuleContract> list, ModuleContract moduleContract) {
        for (ModuleContract candidate : list) {
            if (!candidate.getName().equals(moduleContract.getName())) continue;
            return candidate;
        }
        return null;
    }

    private static boolean sameOrder(List<?> list, List<?> list2) {
        if (list == null) {
            return list2.isEmpty();
        }
        if (list.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i) == list2.get(i)) continue;
            return false;
        }
        return true;
    }

    @Compile(obfuscation=1)
    private void drawTooltip(RockstarDrawContext drawContext) {
        if (this.contentAlpha <= 0.01f) {
            return;
        }
        float f = drawContext.mouseX();
        float f2 = drawContext.mouseY();
        String string = "";
        boolean bl = this.settingPanel != null && (this.settingPanel.contains(f, f2) || this.settingPanel.getKeybindList().inFlow() && this.settingPanel.getKeybindList().contains(f, f2) || this.settingPanel.getSettingsContainer().inFlow() && this.settingPanel.getSettingsContainer().contains(f, f2)) || this.overlays.stream().anyMatch(uiNode -> uiNode.alive() && uiNode.contains(f, f2));
        if (!bl) {
            for (TooltipEntry tooltipEntry : this.tooltipEntries) {
                if (!tooltipEntry.view().hovered()) continue;
                string = tooltipEntry.text().get();
                break;
            }
        }
        this.tooltip.setPosition((float)this.width / 2.0f, (float)this.height / 2.0f - 145.0f);
        if (!string.contains(".description")) {
            this.tooltip.setText(string);
            this.tooltip.render(drawContext);
        }
    }

    private void drawCategoryPanel(RockstarDrawContext drawContext, Component component, ModuleCategory moduleCategory) {
        drawContext.drawClientRect(component.x(), component.y(), component.w(), component.h(), 1.0f, 0.0f, 3.0f, 11.0f, !this.closing);
        float f = 9.0f;
        float f2 = component.x() + 8.0f;
        float f3 = component.y() + 8.0f;
        drawContext.drawShadow(f2, f3, f, f, 11.0f, WidgetState.uniform(f / 2.0f), ColorPalette.VIBRANT_ACCENT_COLOR);
        drawContext.drawIcon("category/" + moduleCategory.getDisplayName().toLowerCase(), f2, f3, f, ColorPalette.ACCENT_COLOR);
        drawContext.drawText(Font.SEMIBOLD.metrics(8.0f), moduleCategory.getDisplayName(), component.x() + 21.0f, component.y() + 9.5f, ColorPalette.PRIMARY_TEXT_COLOR);
        drawContext.drawRect(component.x() + 1.0f, component.y() + 24.0f, component.w() - 2.0f, 1.0f, ColorPalette.BORDER_COLOR);
        this.drawHighlight(drawContext, component);
    }

    private void expandCategoryModule(ModuleCategory moduleCategory, ModuleContract moduleContract) {
        ModuleContract[] slot = this.expandedModules.get(moduleCategory);
        if (slot == null || slot[0] == moduleContract) {
            return;
        }
        Component panel = this.categoryPanels.get(moduleCategory);
        if (panel != null && slot[0] == null) {
            this.savedScrollOffsets.put(moduleCategory, Float.valueOf(panel.scrollOffset2()));
        }
        this.scrollRestores.remove(moduleCategory);
        slot[0] = moduleContract;
        this.historyRecorder.run();
    }

    private void collapseCategory(ModuleCategory moduleCategory) {
        ModuleContract[] slot = this.expandedModules.get(moduleCategory);
        if (slot == null || slot[0] == null) {
            return;
        }
        slot[0] = null;
        this.historyRecorder.run();
        this.rememberScroll(moduleCategory);
    }

    private void rememberScroll(ModuleCategory moduleCategory) {
        Float f = this.savedScrollOffsets.remove(moduleCategory);
        if (f != null && f.floatValue() > 0.5f) {
            this.scrollRestores.put(moduleCategory, new float[]{f.floatValue(), 6.0f});
        }
    }

    private void applyScrollRestores() {
        if (this.scrollRestores.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<ModuleCategory, float[]>> iterator = this.scrollRestores.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ModuleCategory, float[]> entry = iterator.next();
            Component panel = this.categoryPanels.get(entry.getKey());
            float[] state = entry.getValue();
            if (panel == null || panel.scrollOffset2() >= state[0] - 0.5f) {
                iterator.remove();
                continue;
            }
            if ((state[1] = state[1] - 1.0f) <= 0.0f) {
                iterator.remove();
            }
            panel.scrollToImmediate(state[0]);
        }
    }

    @Override
    @Compile(obfuscation=1)
    protected void afterRender(RockstarDrawContext drawContext) {
        this.drawTooltip(drawContext);
        this.applyScrollRestores();
        PendingReveal reveal = this.pendingReveal;
        if (reveal == null) {
            return;
        }
        UiNode target = reveal.setting != null ? this.settingComponents.get(reveal.setting) : this.moduleComponents.get(reveal.module);
        if (target == null) {
            if (--reveal.retries <= 0) {
                this.pendingReveal = null;
            }
            return;
        }
        if (--reveal.delay > 0) {
            return;
        }
        Component panel = this.categoryPanels.get(reveal.module.getCategory());
        if (panel != null) {
            if (reveal.setting != null) {
                panel.scrollToChild(target, 5.0f);
            } else {
                panel.resetScroll();
            }
        }
        this.highlightNode = target;
        this.highlightDeadline = System.currentTimeMillis() + 1600L;
        this.pendingReveal = null;
    }

    private void drawHighlight(RockstarDrawContext drawContext, Component component) {
        if (this.highlightNode == null) {
            return;
        }
        long l = this.highlightDeadline - System.currentTimeMillis();
        if (l <= 0L) {
            this.highlightNode = null;
            return;
        }
        if (!this.highlightNode.inFlow() || !DropdownMenuScreen.isDescendantOf(this.highlightNode, component)) {
            return;
        }
        float f = Math.min(1.0f, (float)l / 400.0f);
        float f2 = this.highlightNode.isSticky() ? 0.0f : component.scrollOffset2();
        float f3 = this.highlightNode.y() - f2;
        float f4 = this.highlightNode.h();
        float f5 = component.y() + 25.0f;
        float f6 = component.y() + component.h() - 1.0f;
        if (f3 < f5) {
            f4 -= f5 - f3;
            f3 = f5;
        }
        if (f3 + f4 > f6) {
            f4 = f6 - f3;
        }
        if (f4 <= 0.0f) {
            return;
        }
        drawContext.drawRoundedRect(component.x() + 2.0f, f3, component.w() - 4.0f, f4, WidgetState.uniform(4.0f), ColorPalette.ACCENT_COLOR.mulAlpha(0.18f * f));
    }

    private static boolean isDescendantOf(UiNode uiNode, UiNode uiNode2) {
        for (UiNode node = uiNode; node != null; node = node.parent()) {
            if (node != uiNode2) continue;
            return true;
        }
        return false;
    }

    private void shakeNode(UiNode uiNode) {
        if (uiNode.shaking()) {
            return;
        }
        uiNode.shake();
        if (RockstarClient.create().getModuleRegistry().getModule(Sounds.class).isEnabled()) {
            // ORIGINAL: iiIiIIIII.ii.I(1.0f, 1.0f) -> the "rockstar:critical" sound at
            // volume 1.0 / pitch 1.0. SoundEffectPlayer has no playCritical helper in
            // this tree, so the same PositionedSoundInstance is built inline.
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                client.getSoundManager().play(PositionedSoundInstance.master(
                        SoundEvent.of(Identifier.of("rockstar", "critical")), 1.0f, 1.0f));
            }
        }
    }

    /**
     * ORIGINAL: rockstar/ilIlil/IiiIiiIIi#I (Lrockstar/ilIlil/IIiiiIIII;)Z.
     * The set of setting types the hover spotlight applies to.
     */
    private static boolean isSpotlightSetting(Setting setting) {
        return setting instanceof BooleanSetting || setting instanceof NumberSetting || setting instanceof RangeSetting || setting instanceof MultiBooleanSetting || setting instanceof ModeSetting;
    }

    /** ORIGINAL: rockstar/ilIlil/IiiIiiIIi#I (Lrockstar/ilIlil/IIiiiIIII;)Lrockstar/ilIlil/iii;. */
    private static Component createSettingComponent(Setting setting) {
        return DropdownMenuScreen.createRawSettingComponent(setting).collapse().visibleWhen(setting::hasValidSettingValue, Easing.easeOutQuart, 220L);
    }

    /** ORIGINAL: rockstar/ilIlil/IiiIiiIIi#i (Lrockstar/ilIlil/IIiiiIIII;)Lrockstar/ilIlil/iii;. */
    private static Component createRawSettingComponent(Setting setting) {
        Component component = setting.buildComponent();
        if (component == null) {
            // ORIGINAL falls back to `new IiiIiiIII(setting)`, an adapter around the
            // legacy SettingWidget-based renderer (rockstar/ilIlil/IiiIiiIii +
            // rockstar/ilIlil/iIIIiIIiI). That subsystem has no counterpart in this
            // tree; every Setting here overrides buildComponent(), so this branch is
            // unreachable in practice.
            return new Component();
        }
        return component.fillWidth().padding(0.0f, 9.0f);
    }

    static {
        RockstarClient.create().getEventBus().registerListeners(new EventListeners());
    }

    /** ORIGINAL: rockstar/ilIlil/IiiIiIiii$II - the pending scroll/highlight request. */
    static final class PendingReveal {
        final ModuleContract module;
        final Setting setting;
        int delay = 2;
        int retries = 40;

        PendingReveal(ModuleContract moduleContract, Setting setting) {
            this.module = moduleContract;
            this.setting = setting;
        }
    }

    /** ORIGINAL: rockstar/ilIlil/IiiIiIiii$Ii - header/divider/settings triple. */
    record ModuleRow(Component header, TextComponent divider, Component settings) {
    }

    /** ORIGINAL: rockstar/ilIlil/IiiIiIiii$i - a hover-description entry. */
    record TooltipEntry(UiNode view, Supplier<String> text) {
    }

    /** ORIGINAL: rockstar/ilIlil/IiiIiIiii$I - the close-transition capture listeners. */
    static final class EventListeners {
        final EventListener<HudRenderEvent> hudListener = hudRenderEvent -> {
            DropdownMenuScreen screen = INSTANCE;
            if (screen == null) {
                return;
            }
            MinecraftClient client = MinecraftClient.getInstance();
            if (screen.closeProgress() >= 1.0f) {
                INSTANCE = null;
                panelCaptured = false;
                worldQuadDrawn = false;
                return;
            }
            if (client.currentScreen != null) {
                worldQuadDrawn = false;
                return;
            }
            if (!panelCaptured) {
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
                panelCaptured = true;
            }
            if (!worldQuadDrawn) {
                DropdownMenuScreen.onHudRender(hudRenderEvent);
            }
            worldQuadDrawn = false;
        };
        final EventListener<Render3DEvent> worldListener = EventListener.withPriority(Integer.MIN_VALUE, render3DEvent -> {
            DropdownMenuScreen screen = INSTANCE;
            if (screen == null || !panelCaptured || screen.cameraPosition == null) {
                return;
            }
            MinecraftClient client = MinecraftClient.getInstance();
            float closingProgress = screen.closeProgress();
            float firstPhaseProgress = Math.min(1.0f, closingProgress / 0.4f);
            float easedProgress = Easing.easeInBack.ease(firstPhaseProgress, 0.0f, 1.0f, 1.0f);
            float opacity = 1.0f - easedProgress;
            float panelOpacity = opacity;
            float fovScale = (float)(Math.tan(Math.toRadians(((Integer)client.options.getFov().getValue()).intValue()) / 2.0) / Math.tan(Math.toRadians(110.0) / 2.0));
            float quadHeight = (3.3f + opacity) * fovScale;
            float quadWidth = quadHeight * ((float)client.getWindow().getFramebufferWidth() / (float)client.getWindow().getFramebufferHeight());
            Vec3d cameraPosition = client.gameRenderer.getCamera().getPos();
            Vec3d panelCenter = screen.cameraPosition;
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
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
            worldQuadDrawn = true;
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.setShaderTexture(0, 0);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            if (closingProgress >= 1.0f) {
                INSTANCE = null;
                panelCaptured = false;
            }
        });

        EventListeners() {
        }
    }
}
