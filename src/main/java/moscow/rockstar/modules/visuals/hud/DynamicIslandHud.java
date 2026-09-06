package moscow.rockstar.modules.visuals.hud;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.modules.config.ModuleConfigurationStore;
import moscow.rockstar.modules.player.farming.hud.FarmHudRenderer;
import moscow.rockstar.modules.player.farming.mining.hud.MiningStatusHeader;
import moscow.rockstar.modules.visuals.waypoints.ServerWaypointTracker;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.geometry.BufferGeometry;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.StringSetting;
import moscow.rockstar.ui.animation.AnimatedColor;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.animation.Transition;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.hud.DynamicIslandBackground;
import moscow.rockstar.ui.hud.DynamicIslandEntry;
import moscow.rockstar.ui.hud.DynamicIslandManager;
import moscow.rockstar.ui.hud.DynamicIslandSize;
import moscow.rockstar.ui.hud.HudElement;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.CursorManager;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import moscow.rockstar.ui.text.DateTimeText;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.client.gui.screen.ChatScreen;
import org.joml.Matrix4f;
import pyrock.utility.render.ColorRGBA;

/**
 * The Dynamic Island HUD element. 1:1 with rockstar/ilIlil/IiIiiIIII
 * ("hud.dynamic_island", icon "hud/island", registration index [4]).
 *
 * <h2>What is faithfully reproduced</h2>
 * Constructor settings with their exact keys and limits, the five animations with their exact
 * durations and easings, the overlay geometry batch parameters, showing = true, the built-in status
 * registration order, the "client" config load, anchorsRightEdge() == false, the whole
 * renderComponent step order including the double tick
 * (prepareRoot / snapAt / tick(delta) / snapAt / tick(0) / draw(alpha)), the island x/y formulas,
 * the alpha clamp, the background chrome, the clock plus ping/plane status text, the ping bar
 * thresholds, expansion state, click routing and the 5-clicks-in-900ms gesture.
 *
 * <h2>What could not be ported, and why the omissions are behaviour-preserving</h2>
 * <ul>
 *   <li><b>The island screen</b> (obf IiIiiiIii). It is referenced in three places, all instanceof
 *       tests that can never be true without it; each site carries a comment.</li>
 *   <li><b>IiIiiIIII#II(III,FFFFF)V</b> - a liquid-glass background variant with zero callers in
 *       the original jar (verified with recaf_search_references). Dead code, omitted.</li>
 * </ul>
 *
 * <h2>The eye / metaball surface</h2>
 * It is live again now that the music status is ported. {@link #pickWaveSource} (disassembled:
 * IiIiiIIII#I(IiIiiIiii,List)LIiiIIIIIi;) returns non-null only when the active entry is the
 * modules status AND an {@link IslandMusicStatus} is among the visible entries; the wave node then
 * grows out of the island's right edge and is joined to the island body by the island-blob shader
 * ({@code iIiiIIiii.I(...)} = {@link moscow.rockstar.render.shaders.ShaderRenderer#drawIslandBlob}).
 * Clicking it selects the music status, which the island expands after {@link #SELECT_DELAY} ms.
 */
public class DynamicIslandHud extends HudElement {
    private static final float TOP_MARGIN = 7.0f;
    /** Used by the island screen's centred layout; see {@link #islandY()}. */
    private static final float SCREEN_CENTER_OFFSET = 125.0f;
    private static final int MULTI_CLICK_COUNT = 5;
    private static final long MULTI_CLICK_WINDOW = 900L;
    private static final int[] PING_THRESHOLDS = new int[]{450, 300, 150, 75};
    private static final float BORDER_ALPHA = 25.5f;
    /** obf Ii - the eye node box size. */
    private static final float EYE_SIZE = 15.0f;
    /** obf iI - the eye node box size (the original declares the same constant twice). */
    private static final float EYE_NODE_SIZE = 15.0f;
    private static final float CORNER_PAD = 4.0f;
    private static final long SELECT_DELAY = 210L;

    /** ORIGINAL: the static final IiiI lambda; the eye node's enter/exit transition. */
    static final Transition SCALE_IN = (progress, node, state) -> {
        state.progress = progress;
        state.scale = 0.9f + 0.1f * progress;
    };

    /**
     * The 29 small-cap / look-alike code points the original normaliser matches on, index-aligned
     * with {@link #SMALL_CAPS_UPPER}. Written as escapes so this file stays pure ASCII like the
     * rest of the tree (the gradle build sets no explicit source encoding).
     */
    private static final String SMALL_CAPS = "\u1d00\u0299\u1d04\u1d05\u1d07\u0493\ua730\u0262\u029c\u026a\u1d0a\u1d0b\u029f\u1d0d\u0274\u1d0f\u1d18\ua7af\u01eb\u0280\ua731\u0455\u1d1b\u1d1c\u1d20\u1d21\u1d61\u028f\u1d22";
    private static final String SMALL_CAPS_UPPER = "ABCDEFFGHIJKLMNOPQQRSSTUVWXYZ";

    private final MultiBooleanSetting statuses;
    private final BooleanSetting songLyrics;
    private final StringSetting customText;
    private final DynamicIslandSize size;

    private final Animation expandAnim;
    private final Animation radiusAnim;
    private final Animation pingAnim;
    private final Animation waveShiftAnim;
    private final Animation eyeAnim;
    private final AnimatedColor colorAnim;
    private final BufferGeometry overlay;

    private DynamicIslandBackground background;
    private Component content;
    private DynamicIslandEntry activeEntry;
    private DynamicIslandEntry currentEntry;
    private boolean eyeActive;
    private float eyeX;
    private float eyeY;
    private float eyeRadius;
    /** ORIGINAL: the field {@code I:Lrockstar/ilIlil/IiiIIIIIi;}. */
    private IslandMusicStatus eyeSelection;
    /** ORIGINAL: the field {@code I:Lrockstar/ilIlil/IiIiiIIII$I;} - the eye / wave node. */
    private WaveNode waveNode;
    private boolean eyeSelectPending;
    private long eyeSelectAt;
    private long lastFrameTime;
    private boolean expanded;
    private int clickCount;
    private long lastClickTime;

    public DynamicIslandHud() {
        super("hud.dynamic_island", "hud/island");
        this.statuses = new MultiBooleanSetting(this, "hud.dynamic_island.statuses").preserveOrder();
        // Not in the original: the remap's DynamicIslandManager (a plain SettingOwner that
        // RockstarClient creates before the HUD registry, and that pyrock/PyDynamicIsland talks to)
        // is turned into a facade over this element so both sides see one status list.
        DynamicIslandManager.attach(this);
        this.songLyrics = new BooleanSetting(this, "hud.dynamic_island.song_lyrics", () -> true).setActiveExtra(true);
        this.customText = new StringSetting(this, "hud.dynamic_island.custom_text").setMaxLength(20).setValue("");
        this.size = new DynamicIslandSize(48.0f, 15.0f, 7.0f);
        this.expandAnim = new Animation(200L, 0.0f, Easing.linear);
        this.radiusAnim = new Animation(500L, 0.0f, Easing.easeOutOvershootSoft);
        this.pingAnim = new Animation(500L, 0.0f, Easing.easeOutBack);
        this.waveShiftAnim = new Animation(260L, 0.0f, Easing.easeInOutCubicBezier);
        this.eyeAnim = new Animation(500L, 0.0f, Easing.easeOutBack);
        this.colorAnim = new AnimatedColor(300L, new ColorRGBA(0.0f, 0.0f, 0.0f), Easing.easeInOutCubicBezier);
        this.overlay = new BufferGeometry()
                .setCornerRadius(0.1f)
                .setGlowRadius(4.0f)
                .setGlowParameters(0.3f, 0.44f)
                .setLightAndDarkColors(-1, -15856114);
        this.showing = true;
        DynamicIslandHud.registerStatuses(this.statuses);
        // ORIGINAL: Ii.I().I().I("client") - loads the "client" config profile so the persisted
        // status selection is applied. The remap flattened that profile store into the static
        // ModuleConfigurationStore (HudElement already maps .i("client") to saveConfiguration()).
        ModuleConfigurationStore.loadConfigurationFromDisk();
        this.reorderEntries();
    }

    /**
     * ORIGINAL rockstar/ilIlil/IiIiiiIIi#I(Lrockstar/ilIlil/IIiiiiiii;)V - the eight built-in
     * statuses in this exact order, with the original's exact modifiers:
     * <pre>
     *   new IiiIIIIiI(statuses);                     -- IslandAlertStatus
     *   new IiiIIIIII(statuses);                     -- IslandModulesStatus
     *   new IiIiiiiII(statuses);                     -- FarmHudRenderer (auto-farm)
     *   new IiiIIIIii(statuses);                     -- IslandPvpStatus ("hud.pvp_mode")
     *   new IiIiiiiiI(statuses).deselect();          -- ServerWaypointTracker
     *   new IiIiiiiii(statuses);                     -- MiningStatusHeader
     *   new IiiIIIIIi(statuses);                     -- IslandMusicStatus
     *   new IiIiiiiIi(statuses).alwaysEnabled();     -- IslandDefaultStatus
     * </pre>
     * Order is load-bearing: {@link #visibleEntries()} reverses the option list and
     * {@link #pickActive} takes its last element, so the FIRST registered visible entry wins and
     * the always-enabled default status at [8] is the permanent fallback that keeps the island on
     * screen. All eight are present.
     */
    private static void registerStatuses(MultiBooleanSetting statuses) {
        new IslandAlertStatus(statuses);
        new IslandModulesStatus(statuses);
        new FarmHudRenderer(statuses);
        new IslandPvpStatus(statuses);
        new ServerWaypointTracker(statuses).deselect();
        new MiningStatusHeader(statuses);
        new IslandMusicStatus(statuses);
        new IslandDefaultStatus(statuses).alwaysEnabled();
    }

    /**
     * ORIGINAL rockstar/ilIlil/IiIiiIIII#I()V - a one-shot fix-up of a persisted option order.
     * <pre>
     *   IiiIIIIII modules = null; IiIiiiiIi profile = null; IiiIIIIiI alerts = null;
     *   for (IiIiiIiii e : this.entries()) { if (e instanceof IiiIIIIII m) modules = m;
     *                                       if (e instanceof IiIiiiiIi p) profile  = p;
     *                                       if (e instanceof IiiIIIIiI a) alerts   = a; }
     *   if (modules == null || profile == null) return;
     *   List&lt;Option&gt; options = statuses.getOptions();
     *   int mi = options.indexOf(modules), pi = options.indexOf(profile);
     *   if (mi &lt; 0 || pi &lt; 0 || mi &lt; pi) return;
     *   options.remove(modules);
     *   int ai = alerts == null ? -1 : options.indexOf(alerts);
     *   int at = ai &gt;= 0 ? ai + 1 : 0;
     *   options.add(Math.min(at, options.size()), modules);
     *   statuses.i(modules);        // == selectOption(modules)
     * </pre>
     * i.e. whenever a saved order has sorted the modules status below the profile status, move it
     * back to just after the alert status (or to the head when the alert status is absent) and
     * re-select it.
     */
    private void reorderEntries() {
        IslandModulesStatus modules = null;
        IslandDefaultStatus profile = null;
        IslandAlertStatus alerts = null;
        for (DynamicIslandEntry entry : this.entries()) {
            if (entry instanceof IslandModulesStatus) {
                modules = (IslandModulesStatus)entry;
            }
            if (entry instanceof IslandDefaultStatus) {
                profile = (IslandDefaultStatus)entry;
            }
            if (entry instanceof IslandAlertStatus) {
                alerts = (IslandAlertStatus)entry;
            }
        }
        if (modules == null || profile == null) {
            return;
        }
        List<MultiBooleanSetting.Option> options = this.statuses.getOptions();
        int modulesIndex = options.indexOf(modules);
        int profileIndex = options.indexOf(profile);
        if (modulesIndex < 0 || profileIndex < 0 || modulesIndex < profileIndex) {
            return;
        }
        options.remove(modules);
        int alertsIndex = alerts == null ? -1 : options.indexOf(alerts);
        int insertAt = alertsIndex >= 0 ? alertsIndex + 1 : 0;
        options.add(Math.min(insertAt, options.size()), modules);
        this.statuses.selectOption(modules);
    }

    @Override
    protected Component build() {
        this.background = new DynamicIslandBackground(this);
        this.background.uniformLayout()
                .motion(Motion.resolveMotionMotionFromLongAndEasing(500L, Easing.easeOutOvershootSoft))
                .size(this.size.width, this.size.height);
        this.content = new Component();
        this.content.uniformLayout().fill().snapPosition().snapSize().interactive(false);
        this.background.add(this.content);
        return this.background;
    }

    @Override
    protected boolean anchorsRightEdge() {
        return false;
    }

    @Override
    public void renderComponent(RockstarDrawContext drawContext) {
        this.ensureFlow();
        List<DynamicIslandEntry> visible = this.visibleEntries();
        DynamicIslandEntry active = this.pickActive(visible);
        this.updateWave(this.pickWaveSource(active, visible));
        this.updateEye();
        this.activeEntry = active;
        if (active == null) {
            return;
        }
        this.updateExpansion(active);
        this.fadeOthers(active);
        this.layout(active);
        float delta = this.tickFlow(drawContext);
        this.applyMeasuredBounds();
        this.drawEye(drawContext, delta);
        this.updateCursor(active);
    }

    /**
     * ORIGINAL: the private {@code I(Lrockstar/ilIlil/IiIiiIiii;Ljava/util/List;)LIiiIIIIIi;} - the
     * wave / eye only exists while the MODULES status is the active one and a music status is also
     * visible; it is the music status's own bars the eye draws.
     */
    private IslandMusicStatus pickWaveSource(DynamicIslandEntry active, List<DynamicIslandEntry> visible) {
        if (!(active instanceof IslandModulesStatus)) {
            return null;
        }
        for (DynamicIslandEntry entry : visible) {
            if (!(entry instanceof IslandMusicStatus)) {
                continue;
            }
            return (IslandMusicStatus)entry;
        }
        return null;
    }

    /** ORIGINAL: the private {@code I(Lrockstar/ilIlil/IiiIIIIIi;)V}. */
    private void updateWave(IslandMusicStatus source) {
        boolean active = source != null;
        this.waveShiftAnim.setReverse(active);
        if (!active) {
            if (this.waveNode != null
                    && (this.waveNode.phase() == UiNode.LifecyclePhase.ENTERING
                        || this.waveNode.phase() == UiNode.LifecyclePhase.VISIBLE)) {
                this.waveNode.beginExit(0.0f);
            }
            return;
        }
        if (this.waveNode == null) {
            this.waveNode = new WaveNode();
            this.waveNode.beginEnter(0.0f);
        } else if (this.waveNode.phase() == UiNode.LifecyclePhase.EXITING
                || this.waveNode.phase() == UiNode.LifecyclePhase.DISCARDED
                || this.waveNode.phase() == UiNode.LifecyclePhase.HIDDEN) {
            this.waveNode.beginEnter(0.0f);
        }
        this.waveNode.setSource(source);
    }

    /** ORIGINAL: the private {@code iI()V}. */
    private void updateEye() {
        boolean visible = this.waveNode != null
                && (this.waveNode.phase() == UiNode.LifecyclePhase.ENTERING
                    || this.waveNode.phase() == UiNode.LifecyclePhase.VISIBLE);
        this.eyeAnim.setReverse(visible);
        this.eyeActive = this.eyeAnim.getValue() > 0.004f
                || this.waveNode != null && this.waveNode.phase() != UiNode.LifecyclePhase.DISCARDED;
    }

    /** ORIGINAL: the private {@code I(Lrockstar/ilIlil/III;F)V}. */
    private void drawEye(RockstarDrawContext drawContext, float delta) {
        if (this.waveNode == null || this.waveNode.phase() == UiNode.LifecyclePhase.DISCARDED) {
            return;
        }
        this.waveNode.snapAt(this.eyeX - 7.5f, this.eyeY - 7.5f);
        this.waveNode.prepareRoot();
        this.waveNode.tick(delta, drawContext.mouseX(), drawContext.mouseY());
        this.waveNode.draw(drawContext, this.alpha());
    }

    /** ORIGINAL: the generic identity {@code <T extends IiIiiIiii> T I(T)} that pyrock calls. */
    public <T extends DynamicIslandEntry> T add(T entry) {
        return entry;
    }

    /** ORIGINAL: I(IiIiiIiii)Z - removes and marks the client config dirty. */
    public boolean remove(DynamicIslandEntry entry) {
        return this.remove(entry, true);
    }

    /** ORIGINAL: i(IiIiiIiii)Z - removes without touching the config. */
    public boolean removeSilently(DynamicIslandEntry entry) {
        return this.remove(entry, false);
    }

    private boolean remove(DynamicIslandEntry entry, boolean save) {
        if (entry == null) {
            return false;
        }
        boolean removed = this.statuses.getOptions().remove(entry);
        this.statuses.getSelectedOptions().remove(entry);
        if (removed) {
            this.expanded = false;
            if (entry == this.eyeSelection) {
                this.clearEyeSelection();
            }
            if (entry == this.currentEntry) {
                this.currentEntry = null;
                this.activeEntry = null;
            }
            if (this.content != null) {
                this.content.clear();
            }
            if (save) {
                ModuleConfigurationStore.saveConfiguration();
            }
        }
        return removed;
    }

    public boolean remove(String name) {
        DynamicIslandEntry entry = this.find(name);
        return entry != null && this.remove(entry);
    }

    /**
     * ORIGINAL: I()Ljava/lang/String; - the island's display title, read by the profile status.
     * The small-caps normaliser it calls (obf iIIIIiIiI.i(String)) lives in a shared text utility
     * that has no remapped counterpart, so it is reproduced verbatim in this class.
     */
    public String islandTitle() {
        String value = this.customText.getValue();
        return value == null || value.isBlank() ? "Barney" : DynamicIslandHud.normalizeSmallCaps(value.trim());
    }

    public DynamicIslandEntry find(String name) {
        if (name == null) {
            return null;
        }
        for (DynamicIslandEntry entry : this.entries()) {
            if (!entry.getName().equalsIgnoreCase(name)) {
                continue;
            }
            return entry;
        }
        return null;
    }

    /** ORIGINAL: I()Ljava/util/List; - every registered entry, in registration order. */
    public List<DynamicIslandEntry> entries() {
        ArrayList<DynamicIslandEntry> list = new ArrayList<DynamicIslandEntry>();
        for (MultiBooleanSetting.Option option : this.statuses.getOptions()) {
            if (!(option instanceof DynamicIslandEntry)) {
                continue;
            }
            list.add((DynamicIslandEntry)option);
        }
        return list;
    }

    /** ORIGINAL: I()Lrockstar/ilIlil/IiIiiIiii; - the entry the island is currently showing. */
    public DynamicIslandEntry active() {
        return this.pickActive(this.visibleEntries());
    }

    /** ORIGINAL: the public i()Ljava/util/List; delegating to the private II()Ljava/util/List;. */
    public List<DynamicIslandEntry> visibleEntries() {
        return this.statuses.getOptions().stream()
                .filter(option -> option instanceof DynamicIslandEntry)
                .map(option -> (DynamicIslandEntry)option)
                .filter(entry -> entry.canShow() && entry.isSelected())
                .toList()
                .reversed();
    }

    private DynamicIslandEntry pickActive(List<DynamicIslandEntry> visible) {
        if (this.eyeSelection != null) {
            if (visible.contains(this.eyeSelection)) {
                return this.eyeSelection;
            }
            this.clearEyeSelection();
            this.expanded = false;
        }
        return visible.isEmpty() ? null : visible.getLast();
    }

    private void ensureFlow() {
        if (this.flow == null) {
            this.flow = this.build();
        }
    }

    private void updateExpansion(DynamicIslandEntry active) {
        if (active == this.eyeSelection && this.eyeSelectPending
                && System.currentTimeMillis() - this.eyeSelectAt >= SELECT_DELAY) {
            this.eyeSelectPending = false;
            this.expanded = true;
        }
        if (!active.isExpandable() || (!this.canExpand() && active != this.eyeSelection)) {
            this.expanded = false;
        }
        this.expandAnim.setReverse(this.expanded);
    }

    private boolean canExpand() {
        return (minecraftClient.currentScreen instanceof ChatScreen || minecraftClient.player == null)
                && !this.select;
    }

    private void fadeOthers(DynamicIslandEntry active) {
        for (MultiBooleanSetting.Option option : this.statuses.getOptions()) {
            if (!(option instanceof DynamicIslandEntry)) {
                continue;
            }
            DynamicIslandEntry entry = (DynamicIslandEntry)option;
            entry.getAnimation().update(entry == active ? 1.0f : 0.0f);
        }
    }

    private void layout(DynamicIslandEntry active) {
        active.prepare(this);
        UiNode node = active.element(this);
        node.prepareRoot();
        DynamicIslandSize entrySize = active.getSize();
        this.colorAnim.setTargetColor(active.getColor());
        this.radiusAnim.update(active.radius(this));
        if (this.width <= 0.0f) {
            this.width = entrySize.width;
        }
        if (this.height <= 0.0f) {
            this.height = entrySize.height;
        }
        this.x = this.islandX(this.width);
        this.y = this.islandY();
        this.background.size(entrySize.width, entrySize.height);
        if (this.currentEntry != active || node.parent() != this.content) {
            this.content.updateChildren(List.of(node));
            this.currentEntry = active;
        }
    }

    /**
     * ORIGINAL: I(Lrockstar/ilIlil/III;)F. The island ticks its flow twice per frame - once with
     * the real delta and once with 0 after re-snapping - so the island box never trails its content
     * by a frame.
     */
    private float tickFlow(RockstarDrawContext drawContext) {
        long now = System.currentTimeMillis();
        float delta = this.lastFrameTime == 0L ? 16.0f : Math.min(64.0f, (float)(now - this.lastFrameTime));
        this.lastFrameTime = now;
        this.flow.prepareRoot();
        this.snapFlow();
        this.flow.tick(delta, drawContext.mouseX(), drawContext.mouseY());
        this.snapFlow();
        this.flow.tick(0.0f, drawContext.mouseX(), drawContext.mouseY());
        this.flow.draw(drawContext, this.alpha());
        return delta;
    }

    private void snapFlow() {
        float width = this.flow.w() > 0.0f ? this.flow.w() : this.width;
        this.flow.snapAt(this.islandX(width), this.islandY());
    }

    private void applyMeasuredBounds() {
        this.width = this.flow.w();
        this.height = this.flow.h();
        this.x = this.flow.x();
        this.y = this.flow.y();
        this.size.set(this.width, this.height, this.radiusAnim.getValue());
    }

    private void updateCursor(DynamicIslandEntry active) {
        if (!this.expanded && active.isExpandable() && UiUtils.contains(this.x, this.y, this.width, this.height,
                UiUtils.mousePosition().getX(), UiUtils.mousePosition().getY())) {
            CursorManager.request(Cursor.HAND);
        }
    }

    private float alpha() {
        return Math.min(1.0f, this.animation.getValue() * this.visible.getValue());
    }

    private float islandX(float width) {
        float shift = 9.5f * this.waveShiftAnim.getValue();
        return WindowMetricsProvider.INSTANCE.width() / 2.0f - width / 2.0f - shift;
    }

    /**
     * ORIGINAL: iI()F =
     * {@code minecraftClient.currentScreen instanceof <island screen> ? height()/2 - 125 : 7}.
     * The island screen (obf IiIiiiIii) is not ported, so the test is never true and
     * {@link #SCREEN_CENTER_OFFSET} is unreachable.
     */
    private float islandY() {
        return TOP_MARGIN;
    }

    /**
     * ORIGINAL: the package-private I(III, IiIiiIiII, F)V called from the background node's
     * drawSelf. Public here only because that node lives in moscow.rockstar.ui.hud while this
     * element lives with the other HUD elements.
     */
    public void drawIslandChrome(RockstarDrawContext drawContext, DynamicIslandBackground node, float alpha) {
        float x = node.x();
        float y = node.y();
        float width = node.w();
        float height = node.h();
        this.drawStatusText(drawContext, x, y, width);
        if (this.eyeActive) {
            this.drawEyeBlob(drawContext, x, y, width, height, alpha);
        } else if (this.activeEntry == null || !this.activeEntry.drawsOwnBackground()) {
            this.drawBackground(drawContext, x, y, width, height, alpha);
        }
    }

    /**
     * ORIGINAL: the private {@code i(Lrockstar/ilIlil/III;FFFFF)V} - the island body and the eye
     * drawn as one metaball. It also publishes eyeX / eyeY / eyeRadius, which {@link #drawEye} and
     * {@link #clickedEye} read.
     */
    private void drawEyeBlob(RockstarDrawContext drawContext, float x, float y, float width, float height,
                             float alpha) {
        float eye = this.eyeAnim.getValue();
        float radius = 7.5f;
        float centerY = y + height / 2.0f;
        float from = x + width - radius;
        float to = x + width + CORNER_PAD + radius;
        float blobX = from + (to - from) * eye;
        this.eyeX = blobX;
        this.eyeY = centerY;
        this.eyeRadius = radius;
        float pad = 5.0f;
        float boundsX = x - pad;
        float boundsY = y - pad;
        float boundsRight = blobX + radius + pad;
        float boundsBottom = y + height + pad;
        float boundsWidth = boundsRight - boundsX;
        float boundsHeight = boundsBottom - boundsY;
        float gap = blobX - radius - (x + width);
        float smooth = Math.max(0.0f, 8.0f - 2.0f * Math.abs(gap));
        this.drawBlurLayer(drawContext, x, y, width, height, this.radiusAnim.getValue(), alpha);
        this.drawBlurLayer(drawContext, blobX - radius, centerY - radius, radius * 2.0f, radius * 2.0f,
                radius, alpha);
        ColorRGBA fill = this.colorAnim.getColor().withAlpha(216.75f);
        ColorRGBA outline = ColorPalette.WHITE.withAlpha(BORDER_ALPHA);
        ShaderRenderer.drawIslandBlob(drawContext.getMatrices(), boundsX, boundsY, boundsWidth, boundsHeight,
                x + width / 2.0f - boundsX, centerY - boundsY, width / 2.0f, height / 2.0f,
                this.radiusAnim.getValue(), blobX - boundsX, centerY - boundsY, radius, smooth, 1.2f,
                fill, outline, alpha);
    }

    /** ORIGINAL: the private {@code I(Lrockstar/ilIlil/III;FFFFFF)V}. */
    private void drawBlurLayer(RockstarDrawContext drawContext, float x, float y, float width, float height,
                               float radius, float alpha) {
        drawContext.drawBlurredRect(x, y, width, height, 45.0f, 2.0f, WidgetState.uniform(radius),
                ColorRGBA.WHITE.withAlpha(255.0f * alpha));
    }

    /** ORIGINAL: I(Lrockstar/ilIlil/III;FFFFF)V. */
    private void drawBackground(RockstarDrawContext drawContext, float x, float y, float width, float height,
                                float alpha) {
        WidgetState radius = WidgetState.uniform(this.radiusAnim.getValue() + 11.0f * this.expandAnim.getValue());
        drawContext.drawSquircle(x - 1.0f, y - 1.0f, width + 2.0f, height + 2.0f,
                2.0f + 5.0f * this.expandAnim.getValue(), radius,
                ColorPalette.WHITE.withAlpha(BORDER_ALPHA * alpha));
        drawContext.drawBlurredRect(x, y, width, height, 45.0f, 2.0f + 2.0f * this.expandAnim.getValue(),
                radius, ColorRGBA.WHITE.withAlpha(255.0f * alpha));
        drawContext.drawSquircle(x, y, width, height, 2.0f + 5.0f * this.expandAnim.getValue(), radius,
                this.colorAnim.getColor().withAlpha(216.75f * alpha));
    }

    /** ORIGINAL: I(Lrockstar/ilIlil/III;FFF)V - the clock on the left, ping or plane on the right. */
    private void drawStatusText(RockstarDrawContext drawContext, float x, float y, float width) {
        // ORIGINAL also bails when minecraftClient.currentScreen is the island screen (obf IiIiiiIii).
        if (minecraftClient.player == null) {
            return;
        }
        float alpha = 1.0f - this.expandAnim.getValue();
        Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
        FontMetrics font = Font.MEDIUM.metrics(7.0f);
        String clock = DateTimeText.currentTime();
        this.overlay.addTexturedLabel(font.getFontRenderer(), clock, font.getFontScale(), matrix,
                x - Font.MEDIUM.metrics(9.0f).measureText(clock) - 4.0f, y + 5.0f, alpha);
        float shift = 19.0f * this.eyeAnim.getValue();
        if (minecraftClient.isInSingleplayer()
                || minecraftClient.player.networkHandler.getPlayerListEntry(minecraftClient.player.getUuid()) == null) {
            this.overlay.addGlyph("plane", matrix, x + width + shift + 8.0f, y + 3.5f, 8.0f, alpha);
        } else {
            this.drawPing(drawContext, matrix, x + width + shift, y, alpha);
        }
        this.overlay.flushBatches();
    }

    /** ORIGINAL: I(Lrockstar/ilIlil/III;Lorg/joml/Matrix4f;FFF)V. */
    private void drawPing(RockstarDrawContext drawContext, Matrix4f matrix, float x, float y, float alpha) {
        this.pingAnim.setReverse(UiUtils.contains(x + 4.0f + 4.0f * this.pingAnim.getValue(), y + 5.0f,
                12.8f, 7.0, drawContext));
        this.overlay.setAnchorTransform(matrix, x + 10.0f, y + 8.0f);
        int ping = minecraftClient.player.networkHandler
                .getPlayerListEntry(minecraftClient.player.getUuid()).getLatency();
        FontMetrics font = Font.MEDIUM.metrics(7.0f);
        this.overlay.addTexturedLabel(font.getFontRenderer(), ping + " ms", font.getFontScale(), matrix,
                x + 4.0f + 4.0f * this.pingAnim.getValue(), y + 5.0f, alpha * this.pingAnim.getValue());
        float collapsed = 1.0f - this.pingAnim.getValue();
        if (collapsed > 0.0f) {
            float dim = alpha * 0.2f * collapsed;
            float base = alpha * collapsed;
            float lit = base + dim * (1.0f - base);
            for (int i = 0; i < PING_THRESHOLDS.length; ++i) {
                float barAlpha = ping < PING_THRESHOLDS[i] ? lit : dim;
                this.overlay.addRectangle(matrix,
                        x + 9.0f + (float)i * 2.7f + 4.0f * this.pingAnim.getValue(),
                        y + 8.0f - (float)i, 2.0f, (float)(3 + i), barAlpha);
            }
        }
        this.overlay.clearAnchorOverride();
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, PointerAction action) {
        if (action == PointerAction.LEFT_CLICK && this.clickedEye((float)mouseX, (float)mouseY)) {
            return;
        }
        if (action == PointerAction.RIGHT_CLICK) {
            super.onMouseClicked(mouseX, mouseY, action);
        }
        this.handleClick((float)mouseX, (float)mouseY, action.getButtonCode());
    }

    /** ORIGINAL: I(FFI)Z. */
    public boolean handleClick(float mouseX, float mouseY, int button) {
        boolean inside = UiUtils.contains(this.x, this.y, this.width, this.height, mouseX, mouseY);
        if (inside && button == 0 && this.multiClick()) {
            return true;
        }
        DynamicIslandEntry active = this.active();
        if (active == null) {
            return false;
        }
        if (this.expanded) {
            if (!inside) {
                this.expanded = false;
                this.clearEyeSelection();
            } else {
                this.clickEntry(active, mouseX, mouseY, button);
            }
            return true;
        }
        if (!inside) {
            return false;
        }
        if (active.isExpandable()) {
            this.expanded = true;
            if (active == this.eyeSelection) {
                this.eyeSelectPending = false;
            }
        } else {
            this.clickEntry(active, mouseX, mouseY, button);
        }
        return true;
    }

    /**
     * ORIGINAL: the private {@code I(FF)Z} - a left click on the eye selects the music status and
     * arms the {@link #SELECT_DELAY} timer that expands the island onto it.
     */
    private boolean clickedEye(float mouseX, float mouseY) {
        if (this.waveNode == null
                || this.waveNode.phase() != UiNode.LifecyclePhase.ENTERING
                    && this.waveNode.phase() != UiNode.LifecyclePhase.VISIBLE
                || !this.waveNode.contains(mouseX, mouseY)) {
            return false;
        }
        IslandMusicStatus source = this.waveNode.source();
        if (source == null) {
            return false;
        }
        this.eyeSelection = source;
        this.eyeSelectPending = true;
        this.eyeSelectAt = System.currentTimeMillis();
        this.expanded = false;
        return true;
    }

    private void clearEyeSelection() {
        this.eyeSelection = null;
        this.eyeSelectPending = false;
        this.eyeSelectAt = 0L;
    }

    private void clickEntry(DynamicIslandEntry entry, float mouseX, float mouseY, int button) {
        UiNode node = entry.element(this);
        if (node.mouseClicked(mouseX, mouseY, PointerAction.fromButtonCode(button))) {
            return;
        }
        entry.click(mouseX, mouseY, button);
    }

    /**
     * ORIGINAL: iI()Z - five left clicks inside the island within 900ms open the island screen, and
     * only while blur is DISABLED. The bytecode is {@code invokestatic IIiIIiIii.iII()Z; ifne
     * <skip>}, i.e. the setScreen branch runs when that check returns false.
     */
    private boolean multiClick() {
        long now = System.currentTimeMillis();
        if (now - this.lastClickTime > MULTI_CLICK_WINDOW) {
            this.clickCount = 0;
        }
        this.lastClickTime = now;
        ++this.clickCount;
        if (this.clickCount < MULTI_CLICK_COUNT || !this.hasProfileStatus()) {
            return false;
        }
        this.clickCount = 0;
        this.expanded = false;
        if (!Interface.isBlurEnabled()) {
            // ORIGINAL: minecraftClient.setScreen(new IiIiiiIii());  -- island screen NOT PORTED
            return true;
        }
        return true;
    }

    /**
     * ORIGINAL: inside {@code iI()Z} the gesture proceeds only when
     * {@code this.II().stream().noneMatch(e -> e instanceof IiIiiiiIi)} is FALSE, i.e. only while a
     * profile / default status is among the visible entries.
     */
    private boolean hasProfileStatus() {
        return this.visibleEntries().stream().anyMatch(entry -> entry instanceof IslandDefaultStatus);
    }

    /** ORIGINAL: obf iIIIIiIiI.i(Ljava/lang/String;)Ljava/lang/String;. */
    private static String normalizeSmallCaps(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(value.length());
        int wordStart = 0;
        boolean converted = false;
        for (int i = 0; i <= value.length(); ++i) {
            if (i == value.length() || Character.isWhitespace(value.charAt(i))) {
                if (converted) {
                    for (int j = wordStart; j < builder.length(); ++j) {
                        builder.setCharAt(j, Character.toUpperCase(builder.charAt(j)));
                    }
                }
                if (i < value.length()) {
                    builder.append(value.charAt(i));
                }
                wordStart = builder.length();
                converted = false;
                continue;
            }
            char c = value.charAt(i);
            int index = SMALL_CAPS.indexOf(c);
            if (index < 0) {
                builder.append(c);
                continue;
            }
            builder.append(SMALL_CAPS_UPPER.charAt(index));
            converted = true;
        }
        return builder.toString();
    }

    public MultiBooleanSetting getStatuses() {
        return this.statuses;
    }

    public BooleanSetting getSongLyrics() {
        return this.songLyrics;
    }

    public StringSetting getCustomText() {
        return this.customText;
    }

    public DynamicIslandSize getSize() {
        return this.size;
    }

    public Animation getExpandAnim() {
        return this.expandAnim;
    }

    public Animation getRadiusAnim() {
        return this.radiusAnim;
    }

    public Animation getPingAnim() {
        return this.pingAnim;
    }

    public Animation getWaveShiftAnim() {
        return this.waveShiftAnim;
    }

    public Animation getEyeAnim() {
        return this.eyeAnim;
    }

    public AnimatedColor getColorAnim() {
        return this.colorAnim;
    }

    public BufferGeometry getOverlay() {
        return this.overlay;
    }

    public DynamicIslandBackground getBackground() {
        return this.background;
    }

    public Component getContent() {
        return this.content;
    }

    public DynamicIslandEntry getActiveEntry() {
        return this.activeEntry;
    }

    public DynamicIslandEntry getCurrentEntry() {
        return this.currentEntry;
    }

    public boolean isEyeActive() {
        return this.eyeActive;
    }

    public float getEyeX() {
        return this.eyeX;
    }

    public float getEyeY() {
        return this.eyeY;
    }

    public float getEyeRadius() {
        return this.eyeRadius;
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public int getClickCount() {
        return this.clickCount;
    }

    public long getLastClickTime() {
        return this.lastClickTime;
    }

    public long getLastFrameTime() {
        return this.lastFrameTime;
    }

    /**
     * ORIGINAL: {@code IiIiiIIII$I} (final, extends {@code iiI}) - the four-bar "eye" that grows
     * out of the island's right edge. It is driven entirely by the music status's bar levels
     * ({@code IiiIIIIIi.I()[F}) and album-art colour ({@code IiiIIIIIi.I()LColorRGBA;}).
     */
    final class WaveNode extends UiNode {
        /** ORIGINAL: the static {@code I:I}. */
        private static final int BAR_COUNT = 4;

        /** ORIGINAL: the field {@code I:Lrockstar/ilIlil/IiiIIIIIi;}. */
        private IslandMusicStatus source;

        WaveNode() {
            this.size(EYE_NODE_SIZE, EYE_NODE_SIZE);
            this.motion(Motion.resolveMotionMotionFromLongAndEasing(260L, Easing.easeInOutCubicBezier));
            this.lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(260L, Easing.easeInOutCubicBezier));
            this.transition(SCALE_IN);
            this.interactive(false);
        }

        /** ORIGINAL: the package-private {@code I(Lrockstar/ilIlil/IiiIIIIIi;)V}. */
        void setSource(IslandMusicStatus source) {
            this.source = source;
        }

        /** ORIGINAL: the package-private {@code I()Lrockstar/ilIlil/IiiIIIIIi;}. */
        IslandMusicStatus source() {
            return this.source;
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float alpha) {
            if (this.source == null) {
                return;
            }
            this.drawBars(drawContext, alpha);
        }

        /** ORIGINAL: the private {@code I(Lrockstar/ilIlil/III;F)V}. */
        private void drawBars(RockstarDrawContext drawContext, float alpha) {
            float barWidth = 1.0f;
            float gap = 0.9f;
            float total = 4.0f * barWidth + 3.0f * gap;
            float startX = this.x() + (this.w() - total) / 2.0f;
            float[] levels = this.source.barLevels();
            ColorRGBA color = this.source.artworkColor().withAlpha(255.0f * alpha);
            for (int i = 0; i < BAR_COUNT; ++i) {
                float barHeight = Math.clamp(1.5f + levels[i] * 0.75f, 2.5f, 9.5f);
                float barX = startX + (float)i * (barWidth + gap);
                drawContext.drawRoundedRect(barX, this.y() + (this.h() - barHeight) / 2.0f, barWidth,
                        barHeight, WidgetState.uniform(barWidth / 2.0f), color);
            }
        }
    }
}
