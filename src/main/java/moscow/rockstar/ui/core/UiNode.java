/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.MatrixStack
 *  org.lwjgl.glfw.GLFW
 */
package moscow.rockstar.ui.core;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.network.http.client.ReactorNettyClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.animation.AnimatedValue;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.animation.Transition;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.DragMode;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.ItemGrid;
import moscow.rockstar.ui.text.ValueFormatter;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import pyrock.utility.render.ColorRGBA;

public abstract class UiNode {
    protected Motion motion = Motion.motion;
    protected final AnimatedValue x = new AnimatedValue(this.motion);
    protected final AnimatedValue y = new AnimatedValue(this.motion);
    protected final AnimatedValue w = new AnimatedValue(this.motion);
    protected final AnimatedValue h = new AnimatedValue(this.motion);
    protected float prefW;
    protected float prefH;
    protected float minW = 0.0f;
    protected float minH = 0.0f;
    protected float maxW = Float.MAX_VALUE;
    protected float maxH = Float.MAX_VALUE;
    protected boolean explicitW;
    protected boolean explicitH;
    protected boolean fillW;
    protected boolean fillH;
    protected UiNode parent;
    private final AnimatedValue hover = new AnimatedValue(0.0f, Motion.resolveMotionMotionFromLongAndEasing(300L, Easing.easeInOutCubicBezier));
    private final AnimatedValue press = new AnimatedValue(0.0f, Motion.motion5);
    private Map<String, AnimatedValue> signals;
    private Map<String, SignalValueProvider> bindings;
    protected boolean interactive = true;
    private boolean modal;
    private boolean pressed;
    private PointerAction pressedButton;
    private boolean hovered;
    boolean hoverable = true;
    protected LifecyclePhase phase = LifecyclePhase.VISIBLE;
    final AnimatedValue appear = new AnimatedValue(1.0f, Motion.resolveMotionMotionFromLongAndEasing(240L, Easing.easeOutCubic));
    private Transition enterT = Transition.PROGRESS_ONLY;
    private Transition exitT = Transition.PROGRESS_ONLY;
    private float appearDelay;
    private float pendingTarget = 1.0f;
    private boolean lifeStarted = true;
    private final Transition.State tweak = new Transition.State();
    private Runnable leftClick;
    private Consumer<PointerAction> buttonClick;
    private PointerClickHandler posClick;
    public static boolean ANY_DRAGGING = false;
    static boolean DRAW_CLIP = false;
    static float DRAW_CLIP_MIN = 0.0f;
    static float DRAW_CLIP_MAX = 0.0f;
    private DragMode dragMode = DragMode.NONE;
    private boolean dragging;
    private DragMode dragAxis = DragMode.BOTH;
    private float dragStartMx;
    private float dragStartMy;
    private float dragStartX;
    private float dragStartY;
    private boolean manuallyPositioned;
    private Cursor cursor;
    private boolean centerX;
    private boolean centerY;
    private BooleanSupplier visibleWhen;
    private boolean visInit;
    private BooleanSupplier snapPosition;
    private BooleanSupplier snapSize;
    private boolean forcedSnap;
    private boolean snappedThisFrame;
    private boolean ignoreSnap;
    private BooleanSupplier stickyWhen;
    private boolean collapse;
    private static int layoutEpoch;
    private static UiNode spotlight;
    private AnimatedValue spotlightAnim;
    private boolean onSpotlightPath;
    private int seenEpoch;
    private float shakeLeft;
    private float shakeElapsed;
    private float shakeAmp = 1.5f;
    private float shakeDur = 450.0f;
    private float blurAmount;
    private ColorRGBA blurTint;
    private float glassAlpha = -1.0f;
    private boolean glassOutline;

    public UiNode blur(float f) {
        this.blurAmount = Math.max(0.0f, f);
        return this;
    }

    public UiNode blur(float f, ColorRGBA colorRGBA) {
        this.blurTint = colorRGBA;
        return this.blur(f);
    }

    public UiNode glass() {
        return this.glass(1.0f, true);
    }

    public UiNode glass(float f, boolean bl) {
        this.glassAlpha = Math.max(0.0f, f);
        this.glassOutline = bl;
        return this;
    }

    protected WidgetState shapeRadius() {
        return WidgetState.NONE;
    }

    protected float shapeSquircle() {
        return 0.0f;
    }

    public UiNode motion(Motion motion) {
        this.motion = motion;
        this.x.motion(motion);
        this.y.motion(motion);
        this.w.motion(motion);
        this.h.motion(motion);
        return this;
    }

    public UiNode width(float f) {
        this.prefW = f;
        this.explicitW = true;
        this.w.setTarget(this.clampW(f));
        return this;
    }

    public UiNode height(float f) {
        this.prefH = f;
        this.explicitH = true;
        this.h.setTarget(this.clampH(f));
        return this;
    }

    public UiNode size(float f, float f2) {
        this.width(f);
        this.height(f2);
        return this;
    }

    public UiNode minSize(float f, float f2) {
        this.minW = f;
        this.minH = f2;
        return this;
    }

    public UiNode maxSize(float f, float f2) {
        this.maxW = f;
        this.maxH = f2;
        return this;
    }

    public UiNode minWidth(float f) {
        this.minW = f;
        return this;
    }

    public UiNode minHeight(float f) {
        this.minH = f;
        return this;
    }

    public UiNode fillWidth() {
        this.fillW = true;
        return this;
    }

    public UiNode fillHeight() {
        this.fillH = true;
        return this;
    }

    public UiNode fill() {
        this.fillH = true;
        this.fillW = true;
        return this;
    }

    public UiNode at(float f, float f2) {
        this.x.setTarget(f);
        this.y.setTarget(f2);
        return this;
    }

    public UiNode snapAt(float f, float f2) {
        this.x.snapTo(f);
        this.y.snapTo(f2);
        return this;
    }

    public UiNode snapToSize(float f, float f2) {
        this.prefW = f;
        this.explicitW = true;
        this.w.snapTo(this.clampW(f));
        this.prefH = f2;
        this.explicitH = true;
        this.h.snapTo(this.clampH(f2));
        return this;
    }

    final void rideWith(float f, float f2) {
        if (this.ignoreSnap) {
            return;
        }
        this.x.advance(f);
        this.y.advance(f2);
    }

    public UiNode enter(Transition transition) {
        this.enterT = transition;
        return this;
    }

    public UiNode exit(Transition transition) {
        this.exitT = transition;
        return this;
    }

    public UiNode transition(Transition transition) {
        this.enterT = transition;
        this.exitT = transition;
        return this;
    }

    public UiNode lifeMotion(Motion motion) {
        this.appear.motion(motion);
        return this;
    }

    public UiNode onClick(Runnable runnable) {
        this.leftClick = runnable;
        return this;
    }

    public UiNode onClick(Consumer<PointerAction> consumer) {
        this.buttonClick = consumer;
        return this;
    }

    public UiNode onClick(PointerClickHandler pointerClickHandler) {
        this.posClick = pointerClickHandler;
        return this;
    }

    public UiNode interactive(boolean bl) {
        this.interactive = bl;
        return this;
    }

    public UiNode modal() {
        this.modal = true;
        return this;
    }

    boolean isModal() {
        return this.modal;
    }

    public UiNode draggable(DragMode dragMode) {
        this.dragMode = dragMode == null ? DragMode.NONE : dragMode;
        return this;
    }

    public UiNode draggable(boolean bl) {
        this.dragMode = bl ? DragMode.BOTH : DragMode.NONE;
        return this;
    }

    public UiNode cursor(Cursor cursor) {
        this.cursor = cursor;
        return this;
    }

    public UiNode hoverMotion(Motion motion) {
        if (motion != null) {
            this.hover.motion(motion);
        }
        return this;
    }

    public UiNode center() {
        this.centerY = true;
        this.centerX = true;
        return this;
    }

    public UiNode centerX() {
        this.centerX = true;
        return this;
    }

    public UiNode centerY() {
        this.centerY = true;
        return this;
    }

    public UiNode visibleWhen(BooleanSupplier booleanSupplier) {
        this.visibleWhen = booleanSupplier;
        return this;
    }

    public UiNode visibleWhen(BooleanSupplier booleanSupplier, Motion motion) {
        this.visibleWhen = booleanSupplier;
        if (motion != null) {
            this.appear.motion(motion);
        }
        return this;
    }

    public UiNode visibleWhen(BooleanSupplier booleanSupplier, Easing easing, long l) {
        return this.visibleWhen(booleanSupplier, Motion.resolveMotionMotionFromLongAndEasing(l, easing));
    }

    public UiNode snapPosition(BooleanSupplier booleanSupplier) {
        this.snapPosition = booleanSupplier;
        return this;
    }

    public UiNode snapPosition() {
        this.snapPosition = () -> true;
        return this;
    }

    public UiNode animatePosition() {
        this.ignoreSnap = true;
        return this;
    }

    public UiNode snapSize(BooleanSupplier booleanSupplier) {
        this.snapSize = booleanSupplier;
        return this;
    }

    public UiNode snapSize() {
        this.snapSize = () -> true;
        return this;
    }

    public UiNode sticky(BooleanSupplier booleanSupplier) {
        this.stickyWhen = booleanSupplier;
        return this;
    }

    public UiNode sticky() {
        this.stickyWhen = () -> true;
        return this;
    }

    public UiNode collapse() {
        this.collapse = true;
        return this;
    }

    public UiNode collapse(boolean bl) {
        this.collapse = bl;
        return this;
    }

    public UiNode shakeConfig(float f, float f2) {
        this.shakeAmp = f;
        this.shakeDur = Math.max(1.0f, f2);
        return this;
    }

    public void shake() {
        this.shakeLeft = this.shakeDur;
        this.shakeElapsed = 0.0f;
    }

    public boolean shaking() {
        return this.shakeLeft > 0.0f;
    }

    public float shakeAmount() {
        return this.shakeLeft > 0.0f ? this.shakeLeft / this.shakeDur : 0.0f;
    }

    public UiNode bind(String string, BooleanSupplier booleanSupplier) {
        return this.bind(string, () -> booleanSupplier.getAsBoolean() ? 1.0f : 0.0f);
    }

    public UiNode bind(String string, SignalValueProvider signalValueProvider) {
        if (this.bindings == null) {
            this.bindings = new HashMap<String, SignalValueProvider>();
        }
        this.bindings.put(string, signalValueProvider);
        this.signal(string);
        return this;
    }

    public UiNode bind(String string, BooleanSupplier booleanSupplier, Motion motion) {
        return this.bind(string, () -> booleanSupplier.getAsBoolean() ? 1.0f : 0.0f, motion);
    }

    public UiNode bind(String string, SignalValueProvider signalValueProvider, Motion motion) {
        this.bind(string, signalValueProvider);
        this.signalMotion(string, motion);
        return this;
    }

    public UiNode bind(String string, BooleanSupplier booleanSupplier, long l) {
        return this.bind(string, () -> booleanSupplier.getAsBoolean() ? 1.0f : 0.0f, l);
    }

    public UiNode bind(String string, SignalValueProvider signalValueProvider, long l) {
        this.bind(string, signalValueProvider);
        this.signalMotion(string, Motion.withLinearEasing(l));
        return this;
    }

    public UiNode signalMotion(String string, Motion motion) {
        if (motion != null) {
            this.signal(string).motion(motion);
        }
        return this;
    }

    public float hover() {
        return this.hover.getCurrent();
    }

    public boolean hovered() {
        return this.hovered;
    }

    public float press() {
        return this.press.getCurrent();
    }

    public float appear() {
        return this.appear.getCurrent();
    }

    public boolean dragging() {
        return this.dragging;
    }

    public boolean pressed() {
        return this.pressed;
    }

    public float sig(String string) {
        AnimatedValue animatedValue = this.signals == null ? null : this.signals.get(string);
        return animatedValue == null ? 0.0f : animatedValue.getCurrent();
    }

    public float sig(String string, Easing easing) {
        float f = this.sig(string);
        return easing == null ? f : easing.ease(f, 0.0f, 1.0f, 1.0f);
    }

    public AnimatedValue signal(String string2) {
        if (this.signals == null) {
            this.signals = new HashMap<String, AnimatedValue>();
        }
        return this.signals.computeIfAbsent(string2, string -> new AnimatedValue(0.0f, Motion.motion5));
    }

    public float x() {
        return this.x.getCurrent();
    }

    public float y() {
        return this.y.getCurrent();
    }

    public float w() {
        return this.w.getCurrent();
    }

    public float h() {
        return this.h.getCurrent();
    }

    public UiNode parent() {
        return this.parent;
    }

    public LifecyclePhase phase() {
        return this.phase;
    }

    public boolean alive() {
        return this.phase != LifecyclePhase.DISCARDED;
    }

    public void close() {
        this.pressed = false;
        this.pressedButton = null;
        if (this.dragging) {
            this.dragging = false;
            ANY_DRAGGING = false;
        }
        this.beginExit(0.0f);
    }

    public void discard() {
        this.phase = LifecyclePhase.DISCARDED;
        this.appear.snapTo(0.0f);
        this.pressed = false;
        this.pressedButton = null;
        this.hovered = false;
        this.spotlightAnim = null;
        this.onSpotlightPath = false;
        if (spotlight == this) {
            spotlight = null;
        }
        if (this.dragging) {
            this.dragging = false;
            ANY_DRAGGING = false;
        }
    }

    public float life() {
        float f = this.appear.getCurrent();
        return f < 0.0f ? 0.0f : (f > 1.0f ? 1.0f : f);
    }

    public boolean inFlow() {
        if (this.phase == LifecyclePhase.DISCARDED || this.phase == LifecyclePhase.HIDDEN) {
            return false;
        }
        if (this.phase == LifecyclePhase.EXITING) {
            return this.collapse;
        }
        return true;
    }

    protected float clampW(float f) {
        return Math.max(this.minW, Math.min(this.maxW, f));
    }

    protected float clampH(float f) {
        return Math.max(this.minH, Math.min(this.maxH, f));
    }

    protected float collapseScale() {
        if (!this.collapse) {
            return 1.0f;
        }
        float f = this.appear.getCurrent();
        return f < 0.0f ? 0.0f : (f > 1.0f ? 1.0f : f);
    }

    protected float rawDesiredH() {
        return this.clampH(this.prefH);
    }

    public float desiredW() {
        return this.clampW(this.prefW);
    }

    public float desiredH() {
        return this.rawDesiredH() * this.collapseScale();
    }

    public void setSlot(float f, float f2, float f3, float f4) {
        boolean bl;
        boolean bl2 = this.epochSnap();
        boolean bl3 = bl2 || !this.ignoreSnap && (this.forcedSnap || this.snapPosition != null && this.snapPosition.getAsBoolean());
        this.forcedSnap = false;
        this.snappedThisFrame = bl3;
        if (bl3) {
            this.x.snapTo(f);
            this.y.snapTo(f2);
        } else {
            this.x.setTarget(f);
            this.y.setTarget(f2);
        }
        boolean bl4 = bl = bl2 || this.snapSize != null && this.snapSize.getAsBoolean();
        if (bl) {
            this.w.snapTo(this.clampW(f3));
        } else {
            this.w.setTarget(this.clampW(f3));
        }
        if (this.collapse && (this.phase == LifecyclePhase.ENTERING || this.phase == LifecyclePhase.EXITING)) {
            this.h.snapTo(this.rawDesiredH());
        } else if (bl) {
            this.h.snapTo(this.clampH(f4));
        } else {
            this.h.setTarget(this.clampH(f4));
        }
    }

    boolean snappedThisFrame() {
        return this.snappedThisFrame;
    }

    void forceSnap() {
        this.forcedSnap = true;
    }

    public void snapSubtree() {
        this.forceSnap();
    }

    public static void invalidateLayout() {
        ++layoutEpoch;
    }

    public static void spotlight(UiNode uiNode) {
        if (spotlight == uiNode) {
            return;
        }
        UiNode uiNode2 = spotlight;
        spotlight = uiNode;
        if (uiNode2 != null) {
            uiNode2.spotlightAnim().setTarget(0.0f);
        }
        if (uiNode != null) {
            uiNode.spotlightAnim().setTarget(1.0f);
            UiNode uiNode3 = uiNode;
            while (uiNode3 != null) {
                uiNode3.onSpotlightPath = true;
                uiNode3 = uiNode3.parent;
            }
        }
    }

    public static UiNode spotlight() {
        return spotlight;
    }

    private AnimatedValue spotlightAnim() {
        if (this.spotlightAnim == null) {
            this.spotlightAnim = new AnimatedValue(0.0f, Motion.resolveMotionMotionFromLongAndEasing(200L, Easing.easeOutQuart));
        }
        return this.spotlightAnim;
    }

    public float spotlightAmount() {
        if (this.spotlightAnim == null) {
            return 0.0f;
        }
        float f = this.spotlightAnim.getCurrent();
        return f < 0.0f ? 0.0f : (f > 1.0f ? 1.0f : f);
    }

    private void stepSpotlight(float f) {
        if (this.spotlightAnim == null) {
            return;
        }
        this.spotlightAnim.update(f);
        if (this == spotlight || this.spotlightAnim.getCurrent() > 0.002f) {
            return;
        }
        this.spotlightAnim = null;
        UiNode uiNode = this;
        while (uiNode != null) {
            uiNode.onSpotlightPath = false;
            uiNode = uiNode.parent;
        }
        uiNode = spotlight;
        while (uiNode != null) {
            uiNode.onSpotlightPath = true;
            uiNode = uiNode.parent;
        }
    }

    protected final boolean epochSnap() {
        return this.seenEpoch != layoutEpoch;
    }

    public boolean isSticky() {
        return this.stickyWhen != null && this.stickyWhen.getAsBoolean();
    }

    protected void measure() {
    }

    final void primeSize() {
        if (!this.w.isAnimating()) {
            this.w.snapTo(this.clampW(this.desiredW()));
        }
        if (!this.h.isAnimating()) {
            this.h.snapTo(this.clampH(this.desiredH()));
        }
    }

    public final void prepareRoot() {
        this.measure();
        this.primeSize();
    }

    /** Prepares a root or overlay for a screen-sized layout pass. */
    public final void prepareLayout(float availableWidth, float availableHeight) {
        this.measure();
        this.primeSize();
        this.centerWithin(availableWidth, availableHeight);
    }

    final void centerWithin(float f, float f2) {
        float f3;
        if (this.manuallyPositioned) {
            return;
        }
        boolean bl = this.epochSnap();
        if (this.centerX) {
            f3 = (f - this.desiredW()) / 2.0f;
            if (bl) {
                this.x.snapTo(f3);
            } else {
                this.x.setTarget(f3);
            }
        }
        if (this.centerY) {
            f3 = (f2 - this.desiredH()) / 2.0f;
            if (bl) {
                this.y.snapTo(f3);
            } else {
                this.y.setTarget(f3);
            }
        }
    }

    public void beginEnter(float f) {
        this.phase = LifecyclePhase.ENTERING;
        this.appear.snapTo(0.0f);
        this.appearDelay = Math.max(0.0f, f);
        this.pendingTarget = 1.0f;
        boolean bl = this.lifeStarted = f <= 0.0f;
        if (this.lifeStarted) {
            this.appear.setTarget(1.0f);
        }
        this.forceSnap();
    }

    public void beginExit(float f) {
        this.phase = LifecyclePhase.EXITING;
        this.appearDelay = Math.max(0.0f, f);
        this.pendingTarget = 0.0f;
        boolean bl = this.lifeStarted = f <= 0.0f;
        if (this.lifeStarted) {
            this.appear.setTarget(0.0f);
        }
    }

    private void advanceLife(float f) {
        if (!this.lifeStarted) {
            this.appearDelay -= f;
            if (this.appearDelay <= 0.0f) {
                this.lifeStarted = true;
                this.appear.setTarget(this.pendingTarget);
            }
        }
        if (this.phase == LifecyclePhase.ENTERING && this.appear.getCurrent() >= 0.999f) {
            this.phase = LifecyclePhase.VISIBLE;
        }
        if (this.phase == LifecyclePhase.EXITING && this.lifeStarted && this.appear.getCurrent() <= 0.001f) {
            this.phase = this.visibleWhen != null ? LifecyclePhase.HIDDEN : LifecyclePhase.DISCARDED;
        }
    }

    private void applyVisibility() {
        boolean bl;
        boolean bl2 = this.visibleWhen.getAsBoolean();
        if (!this.visInit) {
            this.visInit = true;
            if (!bl2) {
                this.phase = LifecyclePhase.HIDDEN;
                this.appear.snapTo(0.0f);
            }
            return;
        }
        boolean bl3 = bl = this.phase == LifecyclePhase.ENTERING || this.phase == LifecyclePhase.VISIBLE;
        if (bl2 && !bl) {
            this.beginEnter(0.0f);
        } else if (!bl2 && bl) {
            this.beginExit(0.0f);
        }
    }

    public final void tick(float f, float f2, float f3) {
        this.dropLostPress(f2, f3);
        if (this.visibleWhen != null) {
            this.applyVisibility();
        }
        if (this.dragging) {
            this.applyDrag(f2, f3);
        }
        this.hovered = this.hoverable && this.interactive && this.inFlow() && this.contains(f2, f3);
        this.hover.setTarget(this.hovered ? 1.0f : 0.0f);
        this.press.setTarget(this.pressed && this.hovered ? 1.0f : 0.0f);
        if (this.hovered && this.cursor != null) {
            moscow.rockstar.ui.input.CursorManager.request((Cursor)this.cursor);
        }
        if (this.bindings != null) {
            for (Map.Entry<String, SignalValueProvider> object : this.bindings.entrySet()) {
                this.signal(object.getKey()).setTarget(object.getValue().getValue());
            }
        }
        this.advanceLife(f);
        this.x.update(f);
        this.y.update(f);
        this.w.update(f);
        this.h.update(f);
        this.hover.update(f);
        this.press.update(f);
        this.appear.update(f);
        this.stepSpotlight(f);
        if (this.signals != null) {
            for (AnimatedValue animatedValue : this.signals.values()) {
                animatedValue.update(f);
            }
        }
        if (this.shakeLeft > 0.0f) {
            this.shakeLeft -= f;
            this.shakeElapsed += f;
        }
        this.onTick(f, f2, f3);
        this.seenEpoch = layoutEpoch;
        this.hoverable = true;
    }

    protected void onTick(float f, float f2, float f3) {
    }

    public final void draw(RockstarDrawContext drawContext, float f) {
        boolean bl;
        boolean bl2;
        float f2;
        float f3;
        float f4;
        Transition transition;
        this.tweak.reset();
        Transition transition2 = transition = this.phase == LifecyclePhase.EXITING ? this.exitT : this.enterT;
        if (transition != null) {
            transition.apply(this.appear.getCurrent(), this, this.tweak);
        }
        if ((f4 = (f + (1.0f - f) * (f3 = this.spotlightAmount())) * this.tweak.progress) <= 0.003f && !this.onSpotlightPath) {
            return;
        }
        if (DRAW_CLIP && ((f2 = this.y.getCurrent()) + this.h.getCurrent() < DRAW_CLIP_MIN || f2 > DRAW_CLIP_MAX)) {
            return;
        }
        MatrixStack class_45872 = drawContext.getMatrices();
        boolean bl3 = bl2 = this.collapse && this.appear.getCurrent() < 0.999f;
        if (bl2) {
            moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)class_45872, (float)this.x.getCurrent(), (float)this.y.getCurrent(), (float)this.w.getCurrent(), (float)Math.max(0.0f, this.desiredH()));
        }
        float f5 = bl2 ? (1.0f - this.collapseScale()) * this.h.getCurrent() : 0.0f;
        float f6 = 0.0f;
        if (this.shakeLeft > 0.0f) {
            f6 = (float)Math.sin(this.shakeElapsed * 0.05f) * this.shakeAmp * (this.shakeLeft / this.shakeDur);
        }
        boolean bl4 = bl = this.tweak.offsetX != 0.0f || this.tweak.offsetY != 0.0f || this.tweak.scale != 1.0f || f5 != 0.0f || f6 != 0.0f;
        if (bl) {
            class_45872.push();
            if (f6 != 0.0f) {
                class_45872.translate(f6, 0.0f, 0.0f);
            }
            if (this.tweak.offsetX != 0.0f || this.tweak.offsetY != 0.0f) {
                class_45872.translate(this.tweak.offsetX, this.tweak.offsetY, 0.0f);
            }
            if (f5 != 0.0f) {
                class_45872.translate(0.0f, -f5, 0.0f);
            }
            if (this.tweak.scale != 1.0f) {
                float f7 = this.x.getCurrent() + this.w.getCurrent() / 2.0f;
                float f8 = this.y.getCurrent() + this.h.getCurrent() / 2.0f;
                class_45872.translate(f7, f8, 0.0f);
                class_45872.scale(this.tweak.scale, this.tweak.scale, 1.0f);
                class_45872.translate(-f7, -f8, 0.0f);
            }
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f4);
        if (f3 > 0.0f) {
            this.drawSpotlightBackdrop(drawContext, (1.0f - f) * f3);
        }
        if (this.blurAmount > 0.0f || this.glassAlpha >= 0.0f) {
            this.drawBackdrop(drawContext);
        }
        this.drawSelf(drawContext, f4);
        this.drawChildren(drawContext, f4);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f);
        if (bl) {
            class_45872.pop();
        }
        if (bl2) {
            moscow.rockstar.render.state.UiScissorStack.pop();
        }
    }

    private void drawBackdrop(RockstarDrawContext drawContext) {
        ColorRGBA colorRGBA;
        float f = this.x.getCurrent();
        float f2 = this.y.getCurrent();
        float f3 = this.w.getCurrent();
        float f4 = this.h.getCurrent();
        if (f3 <= 0.0f || f4 <= 0.0f) {
            return;
        }
        WidgetState widgetState = this.shapeRadius();
        float f5 = this.shapeSquircle();
        if (this.glassAlpha >= 0.0f) {
            drawContext.drawClientRect(f, f2, f3, f4, this.glassAlpha, 0.0f, f5, widgetState.topLeftRadius(), this.glassOutline);
            return;
        }
        ColorRGBA colorRGBA2 = colorRGBA = this.blurTint == null ? ColorRGBA.WHITE : this.blurTint;
        if (f5 > 0.0f) {
            drawContext.drawBlurredRect(f, f2, f3, f4, this.blurAmount, f5, widgetState, colorRGBA);
        } else {
            drawContext.drawBlurredRect(f, f2, f3, f4, this.blurAmount, widgetState, colorRGBA);
        }
    }

    protected float backdropRadius() {
        return 0.0f;
    }

    private void drawSpotlightBackdrop(RockstarDrawContext drawContext, float f) {
        if (f <= 0.01f) {
            return;
        }
        float f2 = this.x.getCurrent();
        float f3 = this.y.getCurrent();
        float f4 = this.w.getCurrent();
        float f5 = this.h.getCurrent();
        if (f4 <= 0.0f || f5 <= 0.0f) {
            return;
        }
        float f6 = this.backdropRadius();
        float f7 = Math.min(f6 > 0.0f ? f6 : 7.0f, Math.min(f4, f5) / 2.0f);
        drawContext.drawSquircle(f2, f3, f4, f5, 3.0f, WidgetState.uniform(f7), ColorPalette.getPanelColor().mulAlpha(0.85f * f));
    }

    protected void drawSelf(RockstarDrawContext drawContext, float f) {
    }

    protected void drawChildren(RockstarDrawContext drawContext, float f) {
    }

    public boolean contains(float f, float f2) {
        float f3 = this.x.getCurrent();
        float f4 = this.y.getCurrent();
        float f5 = this.h.getCurrent();
        if (this.collapse && this.appear.getCurrent() < 0.999f) {
            f5 = Math.max(0.0f, this.desiredH());
        }
        return f >= f3 && f <= f3 + this.w.getCurrent() && f2 >= f4 && f2 <= f4 + f5;
    }

    private UiNode dragRoot() {
        UiNode uiNode = this;
        while (uiNode.parent != null) {
            uiNode = uiNode.parent;
        }
        return uiNode;
    }

    private void applyDrag(float f, float f2) {
        if (this.dragAxis == DragMode.BOTH || this.dragAxis == DragMode.HORIZONTAL) {
            this.x.snapTo(this.dragStartX + f - this.dragStartMx);
        }
        if (this.dragAxis == DragMode.BOTH || this.dragAxis == DragMode.VERTICAL) {
            this.y.snapTo(this.dragStartY + f2 - this.dragStartMy);
        }
    }

    public boolean mouseClicked(float f, float f2, PointerAction pointerAction) {
        boolean bl;
        if (!(this.interactive && this.inFlow() && this.contains(f, f2))) {
            return false;
        }
        ValueFormatter.commitActiveEditor(this);
        boolean bl2 = bl = this.dragMode != DragMode.NONE && pointerAction == PointerAction.LEFT_CLICK;
        if (bl) {
            UiNode uiNode = this.dragRoot();
            uiNode.dragging = true;
            uiNode.dragAxis = this.dragMode;
            uiNode.dragStartMx = f;
            uiNode.dragStartMy = f2;
            uiNode.dragStartX = uiNode.x.getCurrent();
            uiNode.dragStartY = uiNode.y.getCurrent();
            uiNode.manuallyPositioned = true;
            ANY_DRAGGING = true;
        }
        this.pressed = true;
        this.pressedButton = pointerAction;
        try {
            if (pointerAction == PointerAction.LEFT_CLICK && this.leftClick != null) {
                this.leftClick.run();
            }
            if (this.buttonClick != null) {
                this.buttonClick.accept(pointerAction);
            }
            if (this.posClick != null) {
                this.posClick.handleClick(pointerAction, f, f2);
            }
        }
        catch (Throwable throwable) {
            RockstarClient.LOGGER.error("[ui] click handler failed on {}", (Object)this.getClass().getSimpleName(), (Object)throwable);
        }
        return bl || this.leftClick != null || this.buttonClick != null || this.posClick != null;
    }

    public void mouseReleased(float f, float f2, PointerAction pointerAction) {
        this.pressed = false;
        this.pressedButton = null;
        if (this.dragging) {
            this.dragging = false;
            ANY_DRAGGING = false;
        }
    }

    private void dropLostPress(float f, float f2) {
        PointerAction pointerAction = this.pressed
            ? this.pressedButton
            : (this.dragging ? PointerAction.LEFT_CLICK : null);
        if (pointerAction == null) {
            return;
        }
        long l = MinecraftClient.getInstance().getWindow().getHandle();
        if (GLFW.glfwGetMouseButton((long)l, (int)pointerAction.getButtonCode()) == 1) {
            return;
        }
        this.mouseReleased(f, f2, pointerAction);
    }

    public boolean mouseScrolled(float f, float f2, float f3, float f4) {
        return false;
    }

    public boolean keyPressed(int n, int n2, int n3) {
        return false;
    }

    public boolean keyReleased(int n, int n2, int n3) {
        return false;
    }

    public boolean charTyped(char c, int n) {
        return false;
    }

    public static enum LifecyclePhase {
        ENTERING,
        VISIBLE,
        EXITING,
        DISCARDED,
        HIDDEN;
}

    public static interface PointerClickHandler {
        public void handleClick(PointerAction var1, float var2, float var3);
    }

    public static interface SignalValueProvider {
        public float getValue();
    }
}

