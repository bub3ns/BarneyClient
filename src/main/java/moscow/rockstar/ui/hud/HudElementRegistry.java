package moscow.rockstar.ui.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.events.render.HudLayerDispatcher;
import moscow.rockstar.mixin.accessors.DrawContextAccessor;
import moscow.rockstar.api.data.ClientConfigManager;
import moscow.rockstar.modules.visuals.hud.ArmorHud;
import moscow.rockstar.modules.visuals.hud.CustomHotbarHud;
import moscow.rockstar.modules.visuals.hud.DynamicIslandHud;
import moscow.rockstar.modules.visuals.hud.EffectsHud;
import moscow.rockstar.modules.visuals.hud.ItemBindsHud;
import moscow.rockstar.modules.visuals.hud.KeybindsHud;
import moscow.rockstar.modules.visuals.hud.PlayerHud;
import moscow.rockstar.modules.visuals.hud.StaffListHud;
import moscow.rockstar.modules.visuals.hud.TargetHud;
import moscow.rockstar.modules.visuals.hud.TotemCounterHud;
import moscow.rockstar.modules.visuals.hud.WorldHud;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.ui.color.ColorPickerScreen;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.CursorManager;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.NotificationType;
import moscow.rockstar.util.Timer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.VertexConsumerProvider;
import org.lwjgl.opengl.GL11;
import pyrock.events.render.ChatRenderEvent;
import pyrock.events.window.ChatClickEvent;
import pyrock.events.window.ChatReleaseEvent;
import pyrock.events.window.ChatScrollEvent;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

/**
 * Owns the HUD elements, their visibility partition, snap guides and move history.
 * 1:1 with rockstar/ilIlil/IiIiIIIii.
 */
public final class HudElementRegistry
implements ClientAccess {
    private final List<HudElement> showing = new ArrayList<HudElement>();
    private final List<HudElement> hidden = new ArrayList<HudElement>();
    private final List<HudElement> showingView = Collections.unmodifiableList(this.showing);
    private final List<HudElement> hiddenView = Collections.unmodifiableList(this.hidden);
    private final List<HudElement> elements = new NotifyingElementList();
    /**
     * The original manager keeps dedicated public fields for these two elements alongside the
     * list, because other subsystems reach them directly rather than by name lookup
     * (obf {@code IiIiIIIii.I Lrockstar/ilIlil/IiIiiIIII;} and
     * {@code IiIiIIIii.I Lrockstar/ilIlil/IiIiIiIIi;}).
     */
    public DynamicIslandHud dynamicIslandHud;
    public CustomHotbarHud customHotbarHud;
    /**
     * ORIGINAL: rockstar/ilIlil/IiIiIIIii#ii (List&lt;IiIIiiIii&gt;) — the open context/right-click
     * popup menus, exposed there as IIi().
     *
     * <p>IiIIiiIii itself IS ported in this tree, but under the misleading name
     * {@link moscow.rockstar.ui.color.ColorPickerScreen} (it is not a colour picker — it is the
     * popup-menu builder: same three constructors, same open animation, same static transient-node
     * list, same builder surface). It is used as such by Auction, ColorPresetsScreen, ColorSetting
     * and InGameHudMixin, so this file reuses it rather than duplicating the class (a second copy
     * would fork the static {@code transientNodes} overlay list and break nested colour pickers).</p>
     */
    private final List<ColorPickerScreen> popups = new ArrayList<ColorPickerScreen>();
    private final HudMoveHistory moveHistory = new HudMoveHistory();
    private final HudSnapGuideOverlay snapGuides = new HudSnapGuideOverlay();
    private String hoveredTooltipKey = "";
    private final Timer noElementsCooldown = new Timer();
    private float lastWindowWidth;
    private float lastWindowHeight;
    private boolean defaultsRegistered;

    private final EventListener<ChatClickEvent> chatClickListener = event ->
        this.mouseClicked(event.getX(), event.getY(), event.getButton());
    private final EventListener<ChatReleaseEvent> chatReleaseListener = event ->
        this.mouseReleased(event.getX(), event.getY(), event.getButton());

    /**
     * ORIGINAL: rockstar/ilIlil/IiIiIIIii#i — the ChatRenderEvent listener. Rebuilds the draw
     * context exactly like the HUD pass does and draws the popups. This is the ONLY render path
     * for popups while the chat screen is open, because the HudRenderEvent pass skips them when
     * {@code currentScreen instanceof ChatScreen}.
     */
    private final EventListener<ChatRenderEvent> chatRenderListener = event -> {
        RockstarDrawContext drawContext = RockstarDrawContext.create(event.getContext(),
                minecraftClient.currentScreen == null ? -1 : (int)UiUtils.mousePosition().getX(),
                minecraftClient.currentScreen == null ? -1 : (int)UiUtils.mousePosition().getY(),
                MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
        this.drawPopups(drawContext);
    };

    /** ORIGINAL: rockstar/ilIlil/IiIiIIIii#II — the ChatScrollEvent listener. */
    private final EventListener<ChatScrollEvent> chatScrollListener = event -> {
        for (ColorPickerScreen menu : this.popups) {
            menu.mouseScrolled(event.getX(), event.getY(), event.getHorizontal(), event.getVertical());
        }
    };

    public HudElementRegistry() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    /**
     * ORIGINAL rockstar/ilIlil/IiIiIIIii#I ()V — constructs the 11 built-in elements in this exact
     * order and adds them in one addAll. It does NOT set a position and does NOT set showing;
     * both come from the persisted client config (and from Interface.ElementOption.toggle(),
     * which centres an element the first time it is enabled while still at 0,0).
     */
    public void registerDefaultElements() {
        if (this.defaultsRegistered) {
            return;
        }
        this.defaultsRegistered = true;
        List<HudElement> defaults = new ArrayList<HudElement>();
        defaults.add(new EffectsHud());        // [0]  obf IiIiIiIiI  "hud.effects"        icon "hud/potion"
        defaults.add(new KeybindsHud());       // [1]  obf IiIiIiiII  "hud.keybinds"
        defaults.add(new TargetHud());         // [2]  obf IiIiIiiiI  "hud.targethud"      icon "hud/target"
        defaults.add(new ItemBindsHud());      // [3]  obf IiIiIiIii  "hud.item_binds"     icon "hud/clock"
        this.dynamicIslandHud = new DynamicIslandHud();
        defaults.add(this.dynamicIslandHud);   // [4]  obf IiIiiIIII  "hud.dynamic_island"
        defaults.add(new WorldHud());          // [5]  obf IiiIIiIIi  "hud.world"          icon "hud/world"
        defaults.add(new PlayerHud());         // [6]  obf IiiIIiIII  "hud.player"         icon "hud/player"
        defaults.add(new ArmorHud());          // [7]  obf IiiIIIiii                       icon "hud/armor"
        defaults.add(new TotemCounterHud());   // [8]  obf IiIiIiiii  "hud.totem_counter"  icon "hud/hotbar"
        defaults.add(new StaffListHud());      // [9]  obf IiIiIiiIi  "hud.staff_list"     icon "hud/staff"
        this.customHotbarHud = new CustomHotbarHud();
        defaults.add(this.customHotbarHud);    // [10] obf IiIiIiIIi  "hud.custom_hotbar"
        this.elements.addAll(defaults);
    }

    /**
     * The per-frame HUD pass. 1:1 with the HudRenderEvent lambda of rockstar/ilIlil/IiIiIIIii.
     *
     * <p>In the original this body is one of the manager's own five EventListener fields; in this
     * tree it is still driven by moscow/rockstar/events/render/HudRenderListener, which is wired
     * through ModuleRegistry. Same event, same frequency, same arguments.</p>
     */
    public void render(CustomDrawContext originalContext, float tickDelta) {
        if (originalContext == null) {
            return;
        }
        RockstarDrawContext drawContext = RockstarDrawContext.create(originalContext,
                minecraftClient.currentScreen == null ? -1 : (int)UiUtils.mousePosition().getX(),
                minecraftClient.currentScreen == null ? -1 : (int)UiUtils.mousePosition().getY(),
                tickDelta);
        this.hoveredTooltipKey = "";
        this.handleWindowResize();
        this.snapGuides.rebuild();
        HudLayerDispatcher.pre(drawContext, this.elements.size());
        try (WidgetBatchRenderer batch = WidgetBatchRenderer.beginGeometryBatch();){
            for (HudElement element : this.elements) {
                element.render(drawContext);
                if (!(element.getSelecting().getValue() >= 0.0f)) {
                    continue;
                }
                float base = element.getAnimation().getValue() * element.getVisible().getValue();
                float scale = 0.5f + base * 0.5f - 0.05f * element.getSelecting().getValue();
                element.getLoadingAnim().setDuration(1500L);
                element.getLoadingAnim().update(1.0f);
                if (element.getLoadingAnim().getValue() == 1.0f) {
                    element.getLoadingAnim().setValue(0.0f);
                }
                ItemRenderUtils.translateAndScale(drawContext.getMatrices(),
                        element.getX() + element.getWidth() / 2.0f,
                        element.getY() + element.getHeight() / 2.0f, scale);
                // ORIGINAL height arg: element instanceof IiIiIIiii ? Math.max(20f, h) : h
                //   (IiIiIIiii is an abstract immediate-mode HudElement base with no remapped
                //    counterpart and no subclasses, so the plain height is exact.)
                drawContext.drawLoadingRect(element.getX(), element.getY(), element.getWidth(), element.getHeight(),
                        element.getLoadingAnim().getValue() * 2.2f - 0.5f,
                        WidgetState.uniform(7.0f),
                        ColorRGBA.WHITE.withAlpha(100.0f * element.getSelecting().getValue()));
                ItemRenderUtils.popMatrix(drawContext.getMatrices());
            }
            this.snapGuides.draw(drawContext);
            // ORIGINAL: the hover tooltip window (obf IiIIiIiiI) is positioned at (width/2, 30) and drawn
            //           with this.hoveredTooltipKey unless it contains ".description". No remapped counterpart.
        }
        HudLayerDispatcher.post(drawContext, this.elements.size());
        boolean chatOpen = minecraftClient.currentScreen instanceof ChatScreen;
        for (ColorPickerScreen popup : this.popups) {
            if (chatOpen) {
                continue;
            }
            popup.setOpen(false);
        }
        if (!chatOpen && !this.popups.isEmpty()) {
            this.drawPopups(drawContext);
        }
        if (!chatOpen) {
            for (HudElement element : this.elements) {
                if (!element.isDragging()) {
                    continue;
                }
                element.onMouseReleased(element.getX(), element.getY(), PointerAction.LEFT_CLICK);
            }
            CursorManager.request(Cursor.ARROW);
        }
        this.popups.removeIf(popup -> popup.getOpenAnimation().getValue() == 0.0f && !popup.isOpen());
    }

    /**
     * ORIGINAL: private void I(Lrockstar/ilIlil/III;)V — flush the vertex consumers, disable the
     * depth test, translate 2000 units towards the viewer and draw every popup inside one geometry
     * batch, then restore the previous depth-test state.
     */
    private void drawPopups(RockstarDrawContext drawContext) {
        VertexConsumerProvider.Immediate immediate = ((DrawContextAccessor)((Object)drawContext)).getVertexConsumers();
        immediate.draw();
        boolean depthWasEnabled = GL11.glIsEnabled((int)2929);
        RenderSystem.disableDepthTest();
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(0.0f, 0.0f, 2000.0f);
        try (WidgetBatchRenderer batch = WidgetBatchRenderer.beginGeometryBatch();){
            for (ColorPickerScreen popup : this.popups) {
                popup.setVerticalOffset(10.0f).render(drawContext);
            }
        }
        finally {
            immediate.draw();
            drawContext.getMatrices().pop();
            if (depthWasEnabled) {
                RenderSystem.enableDepthTest();
            } else {
                RenderSystem.disableDepthTest();
            }
        }
    }

    /** ORIGINAL: private void i() — reanchors every element when the scaled window size changes. */
    private void handleWindowResize() {
        float width = WindowMetricsProvider.INSTANCE.width();
        float height = WindowMetricsProvider.INSTANCE.height();
        if (this.lastWindowWidth == 0.0f || this.lastWindowHeight == 0.0f) {
            this.lastWindowWidth = width;
            this.lastWindowHeight = height;
            return;
        }
        if (width == this.lastWindowWidth && height == this.lastWindowHeight) {
            return;
        }
        for (HudElement element : this.elements) {
            element.reanchor(this.lastWindowWidth, this.lastWindowHeight, width, height);
        }
        this.lastWindowWidth = width;
        this.lastWindowHeight = height;
    }

    /**
     * ORIGINAL: the ChatClickEvent lambda. Walks FORWARD over the registration-ordered list,
     * hands the click to every element it passes and stops at the first hovered+showing or
     * dragging one.
     */
    public void mouseClicked(double mouseX, double mouseY, int button) {
        PointerAction action = PointerAction.fromButtonCode(button);
        for (ColorPickerScreen popup : this.popups) {
            popup.mouseClicked(mouseX, mouseY, action);
            if (popup.contains((float)mouseX, (float)mouseY)) {
                return;
            }
            popup.setOpen(false);
        }
        for (HudElement element : this.elements) {
            element.onMouseClicked(mouseX, mouseY, action);
            if ((!element.isHovered((float)mouseX, (float)mouseY) || !element.isShowing()) && !element.isDragging()) {
                continue;
            }
            return;
        }
        // ORIGINAL: `if (button == 1 && this.I((float)mouseX, (float)mouseY)) return;` — a guard that
        // lets the waypoint chat listener (obf IiIIiIIIi) and one module (obf iIIIIiII) claim the
        // right-click first. Omitted: the remapped ChatWaypointListener has no popup of its own and
        // exposes no such hit-test, so there is nothing to delegate to.
        if (button == 1 && !this.hidden().isEmpty()) {
            ColorPickerScreen menu = new ColorPickerScreen((float)mouseX, (float)mouseY, 90.0f, 6.0f)
                    .setHeightAndConstrain(120.0f)
                    .addTextRow(Localization.translate("whatadd"), 8, true);
            for (HudElement element : this.hidden()) {
                menu.addActionOption(Localization.translate(element.getName()), element.getIcon(), popup -> {
                    element.pos((float)mouseX, (float)mouseY);
                    element.setShowing(true);
                    popup.setOpen(false);
                    ClientConfigManager.getInstance().save("client");
                });
            }
            this.popups.add(menu);
        } else if (button == 1 && this.hidden().isEmpty() && this.noElementsCooldown.hasElapsed(600L)) {
            RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.ERROR,
                    Localization.translate("hud.no_elements"),
                    Localization.translate("hud.no_elements.desc"));
            this.noElementsCooldown.reset();
        }
    }

    /** ORIGINAL: the ChatReleaseEvent lambda. */
    public void mouseReleased(double mouseX, double mouseY, int button) {
        PointerAction action = PointerAction.fromButtonCode(button);
        for (ColorPickerScreen popup : this.popups) {
            popup.mouseReleased(mouseX, mouseY, action);
            if (!popup.contains((float)mouseX, (float)mouseY)) {
                continue;
            }
            return;
        }
        for (HudElement element : this.elements) {
            element.onMouseReleased(mouseX, mouseY, action);
        }
    }

    /** ORIGINAL: public boolean I(int, int, int) — routes a key press to the top-most open popup. */
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (int i = this.popups.size() - 1; i >= 0; --i) {
            ColorPickerScreen popup = this.popups.get(i);
            if (!popup.isOpen() || !popup.dispatchKeyPressed(keyCode, scanCode, modifiers)) {
                continue;
            }
            return true;
        }
        return false;
    }

    /** ORIGINAL: public boolean I(char, int) — routes a typed character to the top-most open popup. */
    public boolean charTyped(char character, int modifiers) {
        for (int i = this.popups.size() - 1; i >= 0; --i) {
            ColorPickerScreen popup = this.popups.get(i);
            if (!popup.isOpen() || !popup.charTyped(character, modifiers)) {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * ORIGINAL: public boolean I(int, int) — Ctrl+Z / Ctrl+Y over the HUD.
     *
     * <p>The competing undo-domain leg (obf module IIiIiiIIi via IIiiiIIIi.I/.i) is still omitted
     * because that module has no counterpart in this tree; the context-menu leg is restored.</p>
     */
    public boolean keyPressed(int key, int modifiers) {
        if ((modifiers & 2) == 0) {
            return false;
        }
        boolean redo = key == 89;
        if (!redo && key != 90) {
            return false;
        }
        // ORIGINAL: top-most-first loop over this.popups; any open menu swallows the key,
        //           and menu.redo()/menu.undo() (IiIIiiIii.i()Z / .I()Z) short-circuits.
        boolean anyPopupOpen = false;
        for (int i = this.popups.size() - 1; i >= 0; --i) {
            ColorPickerScreen popup = this.popups.get(i);
            if (!popup.isOpen()) {
                continue;
            }
            anyPopupOpen = true;
            if (!(redo ? popup.isColorPickerScreenTargetReady() : popup.isColorPickerReady())) {
                continue;
            }
            return true;
        }
        if (anyPopupOpen) {
            return true;
        }
        long timestamp = redo ? this.moveHistory.lastRedoTime() : this.moveHistory.lastUndoTime();
        if (timestamp != Long.MIN_VALUE) {
            if (redo) {
                this.moveHistory.redo();
            } else {
                this.moveHistory.undo();
            }
            return true;
        }
        return false;
    }

    /** ORIGINAL: void I(IiIiIIiII) — repartition only if the element belongs to this registry. */
    void notifyShowingChanged(HudElement element) {
        if (!this.elements.contains(element)) {
            return;
        }
        this.rebuildVisibilityLists();
    }

    /** ORIGINAL: void II() */
    void rebuildVisibilityLists() {
        this.showing.clear();
        this.hidden.clear();
        for (HudElement element : this.elements) {
            if (element.isShowing()) {
                this.showing.add(element);
                continue;
            }
            this.hidden.add(element);
        }
    }

    @SuppressWarnings(value={"unchecked"})
    public <T extends HudElement> T getByName(String name) {
        return (T)this.elements.stream()
                .filter(element -> element.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public List<HudElement> elements() {
        return this.elements;
    }

    /**
     * ORIGINAL: rockstar/ilIlil/IiIiIIIii#IIi() — the mutable list of open popup menus.
     * See the field comment for why the element type is named ColorPickerScreen.
     */
    public List<ColorPickerScreen> popups() {
        return this.popups;
    }

    public List<HudElement> showing() {
        return this.showingView;
    }

    public List<HudElement> hidden() {
        return this.hiddenView;
    }

    public List<HudElement> showingElements() {
        return this.showing;
    }

    public List<HudElement> hiddenElements() {
        return this.hidden;
    }

    public HudMoveHistory moveHistory() {
        return this.moveHistory;
    }

    public HudSnapGuideOverlay snapGuides() {
        return this.snapGuides;
    }

    public String getHoveredTooltipKey() {
        return this.hoveredTooltipKey;
    }

    public void setHoveredTooltipKey(String hoveredTooltipKey) {
        this.hoveredTooltipKey = hoveredTooltipKey;
    }

    public void add(HudElement element) {
        if (element != null && !this.elements.contains(element)) {
            this.elements.add(element);
        }
    }

    public boolean remove(HudElement element) {
        return element != null && this.elements.remove(element);
    }

    public void removeIf(Predicate<? super HudElement> predicate) {
        this.elements.removeIf(predicate);
    }

    /** ORIGINAL: rockstar/ilIlil/IiIiIIIii$I — an ArrayList that repartitions on every mutation. */
    private final class NotifyingElementList
    extends ArrayList<HudElement> {
        @Override
        public boolean add(HudElement element) {
            boolean changed = super.add(element);
            if (changed) {
                HudElementRegistry.this.rebuildVisibilityLists();
            }
            return changed;
        }

        @Override
        public void add(int index, HudElement element) {
            super.add(index, element);
            HudElementRegistry.this.rebuildVisibilityLists();
        }

        @Override
        public boolean addAll(Collection<? extends HudElement> collection) {
            boolean changed = super.addAll(collection);
            if (changed) {
                HudElementRegistry.this.rebuildVisibilityLists();
            }
            return changed;
        }

        @Override
        public boolean addAll(int index, Collection<? extends HudElement> collection) {
            boolean changed = super.addAll(index, collection);
            if (changed) {
                HudElementRegistry.this.rebuildVisibilityLists();
            }
            return changed;
        }

        @Override
        public HudElement remove(int index) {
            HudElement removed = super.remove(index);
            HudElementRegistry.this.rebuildVisibilityLists();
            return removed;
        }

        @Override
        public boolean remove(Object object) {
            boolean changed = super.remove(object);
            if (changed) {
                HudElementRegistry.this.rebuildVisibilityLists();
            }
            return changed;
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            boolean changed = super.removeAll(collection);
            if (changed) {
                HudElementRegistry.this.rebuildVisibilityLists();
            }
            return changed;
        }

        @Override
        public boolean removeIf(Predicate<? super HudElement> predicate) {
            boolean changed = super.removeIf(predicate);
            if (changed) {
                HudElementRegistry.this.rebuildVisibilityLists();
            }
            return changed;
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            boolean changed = super.retainAll(collection);
            if (changed) {
                HudElementRegistry.this.rebuildVisibilityLists();
            }
            return changed;
        }

        @Override
        public HudElement set(int index, HudElement element) {
            HudElement previous = super.set(index, element);
            HudElementRegistry.this.rebuildVisibilityLists();
            return previous;
        }

        @Override
        public void clear() {
            if (this.isEmpty()) {
                return;
            }
            super.clear();
            HudElementRegistry.this.rebuildVisibilityLists();
        }
    }
}
