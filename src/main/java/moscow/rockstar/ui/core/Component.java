/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.ui.core;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.animation.Transition;
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
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.widgets.controls.ScrollBar;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.utility.render.ColorRGBA;

public class Component
extends UiNode {
    private final List<UiNode> children = new ArrayList<UiNode>();
    private Layout layout = Layout.COLUMN;
    private float gap = 0.0f;
    private Insets padding = Insets.NONE;
    private int columns = 1;
    private boolean wrapContent = false;
    private Alignment alignment = Alignment.STRETCH;
    private JustifyContent overflowMode = JustifyContent.START;
    private boolean scrollEnabled = false;
    private boolean uniformLayout = false;
    private float scrollOffset = 0.0f;
    private float targetScrollOffset = 0.0f;
    private float exitStagger = 0.0f;
    private float scrollStep = 24.0f;
    private Function<Component, ColorRGBA> backgroundColor;
    private WidgetState cornerRadius = WidgetState.NONE;
    private float squircleRadius = 0.0f;
    private RenderHook renderHook;
    private float lastX;
    private float lastY;
    private boolean initialized;
    private float contentHeight = 0.0f;
    private float contentWidth = 0.0f;
    private float viewportSize = 0.0f;
    private float stickySpace = 0.0f;
    private float contentX;
    private float contentY;
    private float viewportWidth;
    private float viewportHeight;
    private final ScrollBar layoutState = new ScrollBar(this);

    public Component layout(Layout layout) {
        this.layout = layout == null ? Layout.COLUMN : layout;
        return this;
    }

    public Component vertical() {
        return this.layout(Layout.COLUMN);
    }

    public Component horizontal() {
        return this.layout(Layout.ROW);
    }

    public Component gap(float f) {
        this.gap = f;
        return this;
    }

    public Component padding(Insets insets) {
        this.padding = insets == null ? Insets.NONE : insets;
        return this;
    }

    public Component padding(float f) {
        this.padding = Insets.uniform(f);
        return this;
    }

    public Component padding(float f, float f2) {
        this.padding = Insets.symmetric(f, f2);
        return this;
    }

    public Component columns(int n) {
        this.columns = Math.max(1, n);
        return this;
    }

    public Component wrapContent() {
        this.wrapContent = true;
        return this;
    }

    public Component wrapContent(boolean bl) {
        this.wrapContent = bl;
        return this;
    }

    public Component alignment(Alignment alignment) {
        this.alignment = alignment == null ? Alignment.STRETCH : alignment;
        return this;
    }

    public Component overflowMode(JustifyContent justifyContent) {
        this.overflowMode = justifyContent == null ? JustifyContent.START : justifyContent;
        return this;
    }

    public Component scrollable() {
        this.scrollEnabled = true;
        return this;
    }

    public Component scrollable(boolean bl) {
        this.scrollEnabled = bl;
        return this;
    }

    public Component scrollStep(float f) {
        this.scrollStep = f;
        return this;
    }

    public Component uniformLayout() {
        this.uniformLayout = true;
        return this;
    }

    public Component uniformLayout(boolean bl) {
        this.uniformLayout = bl;
        return this;
    }

    public Component exitStagger(float f) {
        this.exitStagger = Math.max(0.0f, f);
        return this;
    }

    public Component computeLayout(ScrollMode scrollMode) {
        this.layoutState.mode(scrollMode);
        return this;
    }

    public Component configureLayoutState(Consumer<ScrollBar> consumer) {
        if (consumer != null) {
            consumer.accept(this.layoutState);
        }
        return this;
    }

    public ScrollBar layoutState() {
        return this.layoutState;
    }

    public Component background(Function<Component, ColorRGBA> function) {
        this.backgroundColor = function;
        return this;
    }

    public Component background(ColorRGBA colorRGBA) {
        this.backgroundColor = component -> colorRGBA;
        return this;
    }

    public Component cornerRadius(float f) {
        this.cornerRadius = WidgetState.uniform(f);
        return this;
    }

    public Component cornerRadius(WidgetState widgetState) {
        this.cornerRadius = widgetState == null ? WidgetState.NONE : widgetState;
        return this;
    }

    public Component squircle(float f) {
        this.squircleRadius = f;
        return this;
    }

    @Override
    protected float backdropRadius() {
        return this.cornerRadius.topLeftRadius();
    }

    @Override
    protected WidgetState shapeRadius() {
        return this.cornerRadius;
    }

    @Override
    protected float shapeSquircle() {
        return this.squircleRadius;
    }

    @Override
    public Component blur(float f) {
        super.blur(f);
        return this;
    }

    @Override
    public Component blur(float f, ColorRGBA colorRGBA) {
        super.blur(f, colorRGBA);
        return this;
    }

    @Override
    public Component glass() {
        super.glass();
        return this;
    }

    @Override
    public Component glass(float f, boolean bl) {
        super.glass(f, bl);
        return this;
    }

    public Component renderHook(RenderHook renderHook) {
        this.renderHook = renderHook;
        return this;
    }

    public List<UiNode> children() {
        return this.children;
    }

    @Override
    public Component motion(Motion motion) {
        super.motion(motion);
        return this;
    }

    @Override
    public Component width(float f) {
        super.width(f);
        return this;
    }

    @Override
    public Component height(float f) {
        super.height(f);
        return this;
    }

    @Override
    public Component size(float f, float f2) {
        super.size(f, f2);
        return this;
    }

    @Override
    public Component minSize(float f, float f2) {
        super.minSize(f, f2);
        return this;
    }

    @Override
    public Component maxSize(float f, float f2) {
        super.maxSize(f, f2);
        return this;
    }

    @Override
    public Component minWidth(float f) {
        super.minWidth(f);
        return this;
    }

    @Override
    public Component minHeight(float f) {
        super.minHeight(f);
        return this;
    }

    @Override
    public Component fillWidth() {
        super.fillWidth();
        return this;
    }

    @Override
    public Component fillHeight() {
        super.fillHeight();
        return this;
    }

    @Override
    public Component fill() {
        super.fill();
        return this;
    }

    @Override
    public Component at(float f, float f2) {
        super.at(f, f2);
        return this;
    }

    @Override
    public Component enter(Transition transition) {
        super.enter(transition);
        return this;
    }

    @Override
    public Component exit(Transition transition) {
        super.exit(transition);
        return this;
    }

    @Override
    public Component transition(Transition transition) {
        super.transition(transition);
        return this;
    }

    @Override
    public Component lifeMotion(Motion motion) {
        super.lifeMotion(motion);
        return this;
    }

    @Override
    public Component onClick(Runnable runnable) {
        super.onClick(runnable);
        return this;
    }

    @Override
    public Component onClick(Consumer<PointerAction> consumer) {
        super.onClick(consumer);
        return this;
    }

    @Override
    public Component onClick(UiNode.PointerClickHandler pointerClickHandler) {
        super.onClick(pointerClickHandler);
        return this;
    }

    @Override
    public Component interactive(boolean bl) {
        super.interactive(bl);
        return this;
    }

    @Override
    public Component modal() {
        super.modal();
        return this;
    }

    @Override
    public Component draggable(DragMode dragMode) {
        super.draggable(dragMode);
        return this;
    }

    @Override
    public Component draggable(boolean bl) {
        super.draggable(bl);
        return this;
    }

    @Override
    public Component cursor(Cursor cursor) {
        super.cursor(cursor);
        return this;
    }

    @Override
    public Component hoverMotion(Motion motion) {
        super.hoverMotion(motion);
        return this;
    }

    @Override
    public Component center() {
        super.center();
        return this;
    }

    @Override
    public Component centerX() {
        super.centerX();
        return this;
    }

    @Override
    public Component centerY() {
        super.centerY();
        return this;
    }

    @Override
    public Component visibleWhen(BooleanSupplier booleanSupplier) {
        super.visibleWhen(booleanSupplier);
        return this;
    }

    @Override
    public Component visibleWhen(BooleanSupplier booleanSupplier, Motion motion) {
        super.visibleWhen(booleanSupplier, motion);
        return this;
    }

    @Override
    public Component visibleWhen(BooleanSupplier booleanSupplier, Easing easing, long l) {
        super.visibleWhen(booleanSupplier, easing, l);
        return this;
    }

    @Override
    public Component snapPosition(BooleanSupplier booleanSupplier) {
        super.snapPosition(booleanSupplier);
        return this;
    }

    @Override
    public Component snapPosition() {
        super.snapPosition();
        return this;
    }

    @Override
    public void snapSubtree() {
        super.snapSubtree();
        for (UiNode uiNode : this.children) {
            uiNode.snapSubtree();
        }
    }

    @Override
    public Component animatePosition() {
        super.animatePosition();
        return this;
    }

    @Override
    public Component sticky(BooleanSupplier booleanSupplier) {
        super.sticky(booleanSupplier);
        return this;
    }

    @Override
    public Component sticky() {
        super.sticky();
        return this;
    }

    @Override
    public Component collapse() {
        super.collapse();
        return this;
    }

    @Override
    public Component collapse(boolean bl) {
        super.collapse(bl);
        return this;
    }

    @Override
    public Component bind(String string, BooleanSupplier booleanSupplier) {
        super.bind(string, booleanSupplier);
        return this;
    }

    @Override
    public Component bind(String string, UiNode.SignalValueProvider signalValueProvider) {
        super.bind(string, signalValueProvider);
        return this;
    }

    @Override
    public Component bind(String string, BooleanSupplier booleanSupplier, Motion motion) {
        super.bind(string, booleanSupplier, motion);
        return this;
    }

    @Override
    public Component bind(String string, UiNode.SignalValueProvider signalValueProvider, Motion motion) {
        super.bind(string, signalValueProvider, motion);
        return this;
    }

    @Override
    public Component bind(String string, BooleanSupplier booleanSupplier, long l) {
        super.bind(string, booleanSupplier, l);
        return this;
    }

    @Override
    public Component bind(String string, UiNode.SignalValueProvider signalValueProvider, long l) {
        super.bind(string, signalValueProvider, l);
        return this;
    }

    @Override
    public Component signalMotion(String string, Motion motion) {
        super.signalMotion(string, motion);
        return this;
    }

    public Component add(UiNode uiNode) {
        if (uiNode == null || uiNode == this) {
            return this;
        }
        uiNode.parent = this;
        uiNode.beginEnter(0.0f);
        this.children.add(uiNode);
        return this;
    }

    public Component addAll(List<? extends UiNode> list) {
        for (UiNode uiNode : list) {
            this.add(uiNode);
        }
        return this;
    }

    public Component remove(UiNode uiNode) {
        if (uiNode != null && this.children.contains(uiNode)) {
            uiNode.beginExit(0.0f);
        }
        return this;
    }

    public void clear() {
        for (UiNode uiNode : this.children) {
            uiNode.beginExit(0.0f);
        }
    }

    public Component replaceChildren(List<? extends UiNode> list) {
        for (UiNode uiNode : this.children) {
            uiNode.parent = null;
        }
        this.children.clear();
        this.targetScrollOffset = 0.0f;
        this.scrollOffset = 0.0f;
        if (list != null) {
            this.addAll(list);
        }
        return this;
    }

    /*
     * WARNING - void declaration
     */
    public Component updateChildren(List<? extends UiNode> list) {
        int var4_6 = 0;
        IdentityHashMap<UiNode, Boolean> identityHashMap = new IdentityHashMap<UiNode, Boolean>();
        for (UiNode uiNode : list) {
            identityHashMap.put(uiNode, Boolean.TRUE);
        }
        ArrayList arrayList = new ArrayList();
        boolean n = false;
        for (UiNode uiNode : this.children) {
            if (uiNode.phase() == UiNode.LifecyclePhase.DISCARDED || identityHashMap.containsKey(uiNode)) continue;
            ++var4_6;
        }
        int n2 = 0;
        for (UiNode uiNode : this.children) {
            if (uiNode.phase() == UiNode.LifecyclePhase.DISCARDED || identityHashMap.containsKey(uiNode)) continue;
            uiNode.beginExit(this.exitProgress(n2++, (int)var4_6));
            arrayList.add(uiNode);
        }
        IdentityHashMap<UiNode, Boolean> identityHashMap2 = new IdentityHashMap<UiNode, Boolean>();
        for (UiNode uiNode : this.children) {
            identityHashMap2.put(uiNode, Boolean.TRUE);
        }
        int n3 = list.size();
        for (int i = 0; i < n3; ++i) {
            UiNode uiNode = list.get(i);
            if (identityHashMap2.containsKey(uiNode)) {
                if (uiNode.phase() == UiNode.LifecyclePhase.DISCARDED || uiNode.phase() == UiNode.LifecyclePhase.HIDDEN) {
                    uiNode.parent = this;
                    uiNode.beginEnter(this.exitProgress(i, n3));
                } else if (uiNode.phase() == UiNode.LifecyclePhase.EXITING) {
                    uiNode.beginEnter(this.exitProgress(i, n3));
                }
                arrayList.add(uiNode);
                continue;
            }
            uiNode.parent = this;
            uiNode.beginEnter(this.exitProgress(i, n3));
            arrayList.add(uiNode);
        }
        this.children.clear();
        this.children.addAll(arrayList);
        this.targetScrollOffset = 0.0f;
        return this;
    }

    private float exitProgress(int n, int n2) {
        if (this.exitStagger <= 0.0f || n2 <= 1) {
            return 0.0f;
        }
        return this.exitStagger * ((float)n / (float)(n2 - 1));
    }

    @Override
    public float desiredW() {
        return this.explicitW ? this.clampW(this.prefW) : this.clampW(this.contentHeight >= 0.0f ? this.contentWidth() : this.prefW);
    }

    @Override
    protected float rawDesiredH() {
        return this.explicitH ? this.clampH(this.prefH) : this.clampH(this.contentHeight());
    }

    private float contentWidth() {
        return (this.layout.isVertical() ? this.contentWidth : this.contentHeight) + this.padding.horizontalSize();
    }

    private float contentHeight() {
        return (this.layout.isVertical() ? this.contentHeight : this.contentWidth) + this.padding.verticalSize();
    }

    @Override
    protected void measure() {
        if (this.uniformLayout) {
            this.measureUniform();
            return;
        }
        boolean bl = this.layout.isVertical();
        int n2 = Math.max(1, this.columns);
        ArrayList<UiNode> arrayList = new ArrayList<UiNode>(this.children.size());
        for (UiNode uiNode : this.children) {
            if (uiNode.phase() == UiNode.LifecyclePhase.HIDDEN) continue;
            uiNode.measure();
            if (!uiNode.inFlow()) continue;
            arrayList.add(uiNode);
        }
        int n3 = arrayList.size();
        if (n3 == 0) {
            this.contentHeight = 0.0f;
            this.contentWidth = 0.0f;
            return;
        }
        if (this.wrapContent) {
            float f;
            float f2 = f = bl ? this.h.getCurrent() - this.padding.verticalSize() : this.w.getCurrent() - this.padding.horizontalSize();
            if (f <= 0.0f) {
                f = Float.MAX_VALUE;
            }
            this.layoutFlow(arrayList, bl, f, false, 0.0f, 0.0f);
            return;
        }
        int n4 = (n3 + n2 - 1) / n2;
        float[] fArray = new float[n4];
        float f = 0.0f;
        for (int i = 0; i < n3; ++i) {
            UiNode child = arrayList.get(i);
            int row = i / n2;
            float mainSize = bl ? child.desiredH() : child.desiredW();
            float crossSize = bl ? child.desiredW() : child.desiredH();
            fArray[row] = Math.max(fArray[row], mainSize);
            f = Math.max(f, crossSize);
        }
        float f4 = (float)(n4 - 1) * this.gap;
        for (float rowSize : fArray) {
            f4 += rowSize;
        }
        this.contentHeight = f4;
        this.contentWidth = (float)n2 * f + (float)(n2 - 1) * this.gap;
    }

    @Override
    protected void onTick(float f, float f2, float f3) {
        float f4;
        this.children.removeIf(uiNode -> uiNode.phase() == UiNode.LifecyclePhase.DISCARDED);
        if (this.phase == UiNode.LifecyclePhase.HIDDEN) {
            this.initialized = false;
            return;
        }
        if (this.initialized) {
            float f5 = this.x.getCurrent() - this.lastX;
            f4 = this.y.getCurrent() - this.lastY;
            if (f5 != 0.0f || f4 != 0.0f) {
                for (UiNode object : this.children) {
                    object.rideWith(f5, f4);
                }
            }
        }
        this.lastX = this.x.getCurrent();
        this.lastY = this.y.getCurrent();
        this.initialized = true;
        if (this.inFlow()) {
            this.layoutChildren();
        }
        boolean bl = this.epochSnap();
        if (!this.explicitW && !this.fillW) {
            f4 = this.clampW(this.contentWidth());
            if (bl) {
                this.w.snapTo(f4);
            } else {
                this.w.setTarget(f4);
            }
        }
        if (!this.explicitH && !this.fillH) {
            f4 = this.clampH(this.contentHeight());
            if (bl) {
                this.h.snapTo(f4);
            } else {
                this.h.setTarget(f4);
            }
        }
        if (this.scrollEnabled) {
            f4 = Math.max(0.0f, this.contentHeight - this.viewportSize);
            this.targetScrollOffset = Math.max(0.0f, Math.min(this.targetScrollOffset, f4));
            this.scrollOffset = bl ? this.targetScrollOffset : (this.scrollOffset += (this.targetScrollOffset - this.scrollOffset) * Math.min(1.0f, f * 0.02f));
            if (Math.abs(this.targetScrollOffset - this.scrollOffset) < 0.05f) {
                this.scrollOffset = this.targetScrollOffset;
            }
        } else {
            this.targetScrollOffset = 0.0f;
            this.scrollOffset = 0.0f;
        }
        boolean bl2 = this.layout.isVertical();
        float f6 = 0.0f;
        for (UiNode f8 : this.children) {
            if (!f8.inFlow() || !f8.isSticky()) continue;
            f6 += bl2 ? f8.h() : f8.w();
        }
        this.stickySpace = f6;
        float f5 = f2;
        float f7 = f3;
        if (bl2) {
            f7 += this.scrollOffset;
        } else {
            f5 += this.scrollOffset;
        }
        boolean bl3 = this.hoverable && this.inFlow();
        int n = this.modalIndex();
        for (int i = 0; i < this.children.size(); ++i) {
            UiNode uiNode2 = this.children.get(i);
            boolean bl4 = uiNode2.hoverable = bl3 && i >= n && this.isWithinViewport(uiNode2, f2, f3);
            if (uiNode2.isSticky()) {
                uiNode2.tick(f, f2, f3);
                continue;
            }
            uiNode2.tick(f, f5, f7);
        }
        this.layoutState.tick(f, f2, f3);
    }

    private int modalIndex() {
        for (int i = this.children.size() - 1; i >= 0; --i) {
            UiNode uiNode = this.children.get(i);
            if (!uiNode.isModal() || !uiNode.inFlow()) continue;
            return i;
        }
        return 0;
    }

    public Layout getLayout() {
        return this.layout;
    }

    public boolean isScrollOverflowing() {
        return this.scrollEnabled && this.contentHeight > this.viewportSize + 0.5f;
    }

    public float contentX() {
        return this.contentX;
    }

    public float contentY() {
        return this.contentY;
    }

    public float viewportWidth() {
        return this.viewportWidth;
    }

    public float viewportHeight() {
        return this.viewportHeight;
    }

    public float viewportSize() {
        return this.viewportSize;
    }

    public float stickySpace() {
        return this.stickySpace;
    }

    public float measuredHeight() {
        return this.contentHeight;
    }

    public float scrollOffset() {
        return this.scrollOffset;
    }

    public float maxScrollOffset() {
        return Math.max(0.0f, this.contentHeight - this.viewportSize);
    }

    public void scrollTo(float f) {
        float f2 = this.maxScrollOffset();
        this.targetScrollOffset = this.scrollOffset = Math.max(0.0f, Math.min(f, f2));
    }

    public float scrollOffset2() {
        return this.scrollOffset;
    }

    public void resetScroll() {
        this.targetScrollOffset = 0.0f;
        this.layoutState.show();
    }

    public void scrollToImmediate(float f) {
        if (!this.scrollEnabled) {
            return;
        }
        this.scrollOffset = this.targetScrollOffset = Math.max(0.0f, f);
        this.layoutState.show();
    }

    public void scrollToChild(UiNode uiNode, float f) {
        if (!this.scrollEnabled || uiNode == null) {
            return;
        }
        boolean bl = this.layout.isVertical();
        float f2 = bl ? this.y.getCurrent() + this.padding.top : this.x.getCurrent() + this.padding.left;
        float f3 = (bl ? uiNode.y() : uiNode.x()) - f2;
        this.targetScrollOffset = Math.max(0.0f, Math.min(f3 - this.stickySpace - f, this.maxScrollOffset()));
        this.layoutState.show();
    }

    private boolean isWithinViewport(UiNode uiNode, float f, float f2) {
        if (!this.scrollEnabled) {
            return true;
        }
        boolean bl = this.layout.isVertical();
        float f3 = uiNode.isSticky() ? 0.0f : this.stickySpace;
        float f4 = this.contentX + (bl ? 0.0f : f3);
        float f5 = this.contentY + (bl ? f3 : 0.0f);
        float f6 = this.viewportWidth - (bl ? 0.0f : f3);
        float f7 = this.viewportHeight - (bl ? f3 : 0.0f);
        return f >= f4 && f <= f4 + f6 && f2 >= f5 && f2 <= f5 + f7;
    }

    private void measureUniform() {
        float f = 0.0f;
        float f2 = 0.0f;
        for (UiNode uiNode : this.children) {
            if (uiNode.phase() == UiNode.LifecyclePhase.HIDDEN) continue;
            uiNode.measure();
            if (!uiNode.inFlow()) continue;
            f = Math.max(f, uiNode.desiredH());
            f2 = Math.max(f2, uiNode.desiredW());
        }
        this.contentHeight = f;
        this.contentWidth = f2;
    }

    private void layoutUniform() {
        float f;
        float f2 = this.x.getCurrent() + this.padding.left;
        float f3 = this.y.getCurrent() + this.padding.top;
        float f4 = Math.max(0.0f, this.w.getCurrent() - this.padding.horizontalSize());
        this.viewportSize = f = Math.max(0.0f, this.h.getCurrent() - this.padding.verticalSize());
        this.contentX = f2;
        this.contentY = f3;
        this.viewportWidth = f4;
        this.viewportHeight = f;
        boolean bl = this.isAutoSize(true);
        boolean bl2 = this.isAutoSize(false);
        float f5 = 0.0f;
        float f6 = 0.0f;
        for (UiNode uiNode : this.children) {
            if (!uiNode.inFlow()) continue;
            float f7 = uiNode.fillW || this.alignment == Alignment.STRETCH ? f4 : (bl ? uiNode.desiredW() : Math.min(f4, uiNode.desiredW()));
            float f8 = uiNode.fillH ? f : (bl2 ? uiNode.desiredH() : Math.min(f, uiNode.desiredH()));
            if (this.snappedThisFrame()) {
                uiNode.forceSnap();
            }
            uiNode.setSlot(f2, f3, f7, f8);
            f5 = Math.max(f5, f8);
            f6 = Math.max(f6, f7);
        }
        this.contentHeight = f5;
        this.contentWidth = f6;
    }

    private boolean isAutoSize(boolean bl) {
        return bl ? !this.explicitW && !this.fillW && this.maxW == Float.MAX_VALUE : !this.explicitH && !this.fillH && this.maxH == Float.MAX_VALUE;
    }

    private boolean isAutoCrossSize(boolean bl) {
        return bl ? !this.explicitH && !this.fillH && this.maxH == Float.MAX_VALUE : !this.explicitW && !this.fillW && this.maxW == Float.MAX_VALUE;
    }

    private void layoutChildren() {
        float f;
        float f2;
        float f3;
        if (this.uniformLayout) {
            this.layoutUniform();
            return;
        }
        boolean bl = this.layout.isVertical();
        float f4 = this.x.getCurrent() + this.padding.left;
        float f5 = this.y.getCurrent() + this.padding.top;
        float f6 = Math.max(0.0f, this.w.getCurrent() - this.padding.horizontalSize());
        float f7 = Math.max(0.0f, this.h.getCurrent() - this.padding.verticalSize());
        float f8 = bl ? f7 : f6;
        float f9 = bl ? f6 : f7;
        this.viewportSize = f8;
        this.contentX = f4;
        this.contentY = f5;
        this.viewportWidth = f6;
        this.viewportHeight = f7;
        int n2 = Math.max(1, this.columns);
        float f10 = (f9 - (float)(n2 - 1) * this.gap) / (float)n2;
        if (f10 < 0.0f) {
            f10 = 0.0f;
        }
        ArrayList<UiNode> arrayList = new ArrayList<UiNode>(this.children.size());
        for (UiNode uiNode : this.children) {
            if (!uiNode.inFlow()) continue;
            arrayList.add(uiNode);
        }
        int n3 = arrayList.size();
        if (n3 == 0) {
            this.contentHeight = 0.0f;
            this.contentWidth = 0.0f;
            return;
        }
        if (this.wrapContent) {
            this.layoutFlow(arrayList, bl, f8, true, f4, f5);
            return;
        }
        int n4 = (n3 + n2 - 1) / n2;
        float[] fArray = new float[n4];
        float f11 = 0.0f;
        for (int i = 0; i < n3; ++i) {
            UiNode child = arrayList.get(i);
            int row = i / n2;
            float mainSize = bl ? child.desiredH() : child.desiredW();
            float crossSize = bl ? child.desiredW() : child.desiredH();
            fArray[row] = Math.max(fArray[row], mainSize);
            f11 = Math.max(f11, crossSize);
        }
        float f13 = (float)(n4 - 1) * this.gap;
        for (float rowSize : fArray) {
            f13 += rowSize;
        }
        this.contentHeight = f13;
        this.contentWidth = (float)n2 * (this.alignment == Alignment.STRETCH ? f10 : f11) + (float)(n2 - 1) * this.gap;
        if (!(this.explicitW || this.fillW || this.explicitH || this.fillH)) {
            this.contentWidth = (float)n2 * f11 + (float)(n2 - 1) * this.gap;
        }
        float f14 = Math.max(0.0f, f8 - f13);
        int fillChildCount = 0;
        if (n2 == 1) {
            for (UiNode uiNode : arrayList) {
                if (!(bl ? uiNode.fillH : uiNode.fillW)) continue;
                ++fillChildCount;
            }
        }
        float f15 = f3 = fillChildCount > 0 && f14 > 0.0f ? f14 / (float)fillChildCount : 0.0f;
        if (f3 > 0.0f) {
            f14 = 0.0f;
        }
        float f16 = 1.0f;
        if (n2 == 1 && fillChildCount > 0 && f13 > f8 && !this.isAutoCrossSize(bl)) {
            f2 = 0.0f;
            for (UiNode uiNode : arrayList) {
                if (!(bl ? uiNode.fillH : uiNode.fillW)) continue;
                f2 += bl ? uiNode.desiredH() : uiNode.desiredW();
            }
            f = f13 - f8;
            if (f2 > 0.0f) {
                f16 = Math.max(0.0f, (f2 - f) / f2);
                this.contentHeight = f13 - Math.min(f, f2);
            }
        }
        f2 = 0.0f;
        f = 0.0f;
        switch (this.overflowMode) {
            case CENTER: {
                f2 = f14 / 2.0f;
                break;
            }
            case END: {
                f2 = f14;
                break;
            }
            case SPACE_BETWEEN: {
                f = n4 > 1 ? f14 / (float)(n4 - 1) : 0.0f;
                break;
            }
            case SPACE_AROUND: {
                f = f14 / (float)n4;
                f2 = f / 2.0f;
                break;
            }
            case SPACE_EVENLY: {
                f2 = f = f14 / (float)(n4 + 1);
                break;
            }
        }
        boolean bl2 = this.isAutoSize(bl);
        float f17 = f2;
        for (int i = 0; i < n3; ++i) {
            float f18;
            float f19;
            float f20;
            float f21;
            float f22;
            UiNode uiNode = (UiNode)arrayList.get(i);
            int n5 = i / n2;
            int n6 = i % n2;
            boolean bl3 = bl ? uiNode.fillW : uiNode.fillH;
            boolean bl4 = bl ? uiNode.fillH : uiNode.fillW;
            float f23 = bl ? uiNode.desiredW() : uiNode.desiredH();
            float f24 = (float)n6 * (f10 + this.gap);
            float f25 = bl3 || this.alignment == Alignment.STRETCH ? f10 : (bl2 ? f23 : Math.min(f10, f23));
            float f26 = switch (this.alignment) {
                case Alignment.CENTER -> (f10 - f25) / 2.0f;
                case Alignment.END -> f10 - f25;
                default -> 0.0f;
            };
            float f27 = f24 + f26;
            float f28 = bl4 ? f3 : 0.0f;
            float f29 = f22 = bl4 ? fArray[n5] * f16 : fArray[n5];
            float f30 = bl4 ? f22 + f28 : (bl ? uiNode.desiredH() : uiNode.desiredW());
            float f31 = f17;
            if (this.layout.isReversed()) {
                f31 = f8 - f31 - f30;
            }
            if (bl) {
                f21 = f4 + f27;
                f20 = f5 + f31;
                f19 = f25;
                f18 = f30;
            } else {
                f21 = f4 + f31;
                f20 = f5 + f27;
                f19 = f30;
                f18 = f25;
            }
            if (this.snappedThisFrame()) {
                uiNode.forceSnap();
            }
            uiNode.setSlot(f21, f20, f19, f18);
            if (n6 != n2 - 1 && i != n3 - 1) continue;
            f17 += f22 + f28 + this.gap + f;
        }
    }

    private void layoutFlow(List<UiNode> list, boolean bl, float f, boolean bl2, float f2, float f3) {
        float f4 = 0.0f;
        float f5 = 0.0f;
        float f6 = 0.0f;
        float f7 = 0.0f;
        boolean bl3 = true;
        for (UiNode uiNode : list) {
            float f8;
            float f9 = bl ? uiNode.desiredH() : uiNode.desiredW();
            float f10 = bl ? uiNode.desiredW() : uiNode.desiredH();
            float f11 = f8 = bl3 ? 0.0f : f4 + this.gap;
            if (!bl3 && f8 + f9 > f) {
                f5 += f6 + this.gap;
                f6 = 0.0f;
                f8 = 0.0f;
                bl3 = true;
            }
            if (bl2) {
                float f12;
                float f13;
                float f14;
                float f15;
                if (bl) {
                    f15 = f2 + f5;
                    f14 = f3 + f8;
                    f13 = f10;
                    f12 = f9;
                } else {
                    f15 = f2 + f8;
                    f14 = f3 + f5;
                    f13 = f9;
                    f12 = f10;
                }
                if (this.snappedThisFrame()) {
                    uiNode.forceSnap();
                }
                uiNode.setSlot(f15, f14, f13, f12);
            }
            f4 = f8 + f9;
            f6 = Math.max(f6, f10);
            f7 = Math.max(f7, f4);
            bl3 = false;
        }
        this.contentHeight = f7;
        this.contentWidth = f5 + f6;
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        ColorRGBA colorRGBA;
        if (this.backgroundColor != null && (colorRGBA = this.backgroundColor.apply(this)) != null && colorRGBA.getAlpha() > 0.0f) {
            if (this.squircleRadius > 0.0f) {
                drawContext.drawSquircle(this.x.getCurrent(), this.y.getCurrent(), this.w.getCurrent(), this.h.getCurrent(), this.squircleRadius, this.cornerRadius, colorRGBA);
            } else {
                drawContext.drawRoundedRect(this.x.getCurrent(), this.y.getCurrent(), this.w.getCurrent(), this.h.getCurrent(), this.cornerRadius, colorRGBA);
            }
        }
        if (this.renderHook != null) {
            this.renderHook.render(drawContext, this);
        }
    }

    @Override
    protected void drawChildren(RockstarDrawContext drawContext, float f) {
        boolean bl;
        float f2;
        float f3;
        MatrixStack matrices;
        boolean bl2 = this.layout.isVertical();
        float f4 = this.x.getCurrent() + this.padding.left;
        float f5 = this.y.getCurrent() + this.padding.top;
        float f6 = Math.max(0.0f, this.w.getCurrent() - this.padding.horizontalSize());
        float f7 = Math.max(0.0f, this.h.getCurrent() - this.padding.verticalSize());
        float f8 = 0.0f;
        boolean bl3 = false;
        for (UiNode uiNode2 : this.children) {
            if (!uiNode2.inFlow() || !uiNode2.isSticky()) continue;
            bl3 = true;
            f8 += bl2 ? uiNode2.h() : uiNode2.w();
        }
        boolean bl4 = this.scrollEnabled;
        matrices = drawContext.getMatrices();
        if (bl4) {
            float f9 = f4 + (bl2 ? 0.0f : f8);
            f3 = f5 + (bl2 ? f8 : 0.0f);
            f2 = Math.max(0.0f, f6 - (bl2 ? 0.0f : f8));
            float f10 = Math.max(0.0f, f7 - (bl2 ? f8 : 0.0f));
            moscow.rockstar.render.state.UiScissorStack.push(matrices, (float)f9, (float)f3, (float)f2, (float)f10);
        }
        boolean bl5 = bl = this.scrollOffset != 0.0f;
        if (bl) {
            matrices.push();
            matrices.translate(bl2 ? 0.0f : -this.scrollOffset, bl2 ? -this.scrollOffset : 0.0f, 0.0f);
        }
        f3 = this.scrollOffset - this.scrollStep;
        f2 = this.scrollOffset + this.viewportSize + 24.0f;
        boolean bl6 = this.scrollEnabled && bl2;
        boolean bl7 = DRAW_CLIP;
        float f11 = DRAW_CLIP_MIN;
        float f12 = DRAW_CLIP_MAX;
        if (bl6) {
            DRAW_CLIP = true;
            DRAW_CLIP_MIN = f5 + this.scrollOffset - this.scrollStep;
            DRAW_CLIP_MAX = f5 + this.scrollOffset + this.viewportSize + 24.0f;
        }
        for (UiNode uiNode3 : this.children) {
            float f13;
            if (uiNode3.isSticky()) continue;
            float f14 = bl2 ? uiNode3.y() - f5 : uiNode3.x() - f4;
            float f15 = f13 = bl2 ? uiNode3.h() : uiNode3.w();
            if (this.scrollEnabled && (f14 + f13 < f3 || f14 > f2)) continue;
            uiNode3.draw(drawContext, f);
        }
        if (bl6) {
            DRAW_CLIP = bl7;
            DRAW_CLIP_MIN = f11;
            DRAW_CLIP_MAX = f12;
        }
        if (bl) {
            matrices.pop();
        }
        if (bl4) {
            moscow.rockstar.render.state.UiScissorStack.pop();
        }
        if (bl3) {
            if (bl4) {
                moscow.rockstar.render.state.UiScissorStack.push(matrices, (float)f4, (float)f5, (float)f6, (float)f7);
            }
            for (UiNode uiNode3 : this.children) {
                if (!uiNode3.isSticky()) continue;
                uiNode3.draw(drawContext, f);
            }
            if (bl4) {
                moscow.rockstar.render.state.UiScissorStack.pop();
            }
        }
        this.layoutState.render(drawContext, f);
    }

    @Override
    public boolean mouseClicked(float f, float f2, PointerAction pointerAction) {
        if (!this.inFlow()) {
            return false;
        }
        if (this.layoutState.handleClick(f, f2, pointerAction == PointerAction.LEFT_CLICK)) {
            return true;
        }
        if (!this.contains(f, f2)) {
            return false;
        }
        int n = this.modalIndex();
        for (int i = this.children.size() - 1; i >= n; --i) {
            UiNode uiNode = this.children.get(i);
            if (!uiNode.inFlow() || !uiNode.isSticky() || !this.isWithinViewport(uiNode, f, f2) || !uiNode.mouseClicked(f, f2, pointerAction)) continue;
            return true;
        }
        float f3 = f2;
        float f4 = f;
        if (this.layout.isVertical()) {
            f3 += this.scrollOffset;
        } else {
            f4 += this.scrollOffset;
        }
        for (int i = this.children.size() - 1; i >= n; --i) {
            UiNode uiNode = this.children.get(i);
            if (!uiNode.inFlow() || uiNode.isSticky() || !this.isWithinViewport(uiNode, f, f2) || !uiNode.mouseClicked(f4, f3, pointerAction)) continue;
            return true;
        }
        return super.mouseClicked(f, f2, pointerAction);
    }

    @Override
    public void mouseReleased(float f, float f2, PointerAction pointerAction) {
        super.mouseReleased(f, f2, pointerAction);
        this.layoutState.hide();
        float f3 = f2;
        float f4 = f;
        if (this.layout.isVertical()) {
            f3 += this.scrollOffset;
        } else {
            f4 += this.scrollOffset;
        }
        for (UiNode uiNode : this.children) {
            if (uiNode.isSticky()) {
                uiNode.mouseReleased(f, f2, pointerAction);
                continue;
            }
            uiNode.mouseReleased(f4, f3, pointerAction);
        }
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (!this.inFlow()) {
            return false;
        }
        int n4 = this.modalIndex();
        for (int i = this.children.size() - 1; i >= n4; --i) {
            if (!this.children.get(i).keyPressed(n, n2, n3)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int n, int n2, int n3) {
        if (!this.inFlow()) {
            return false;
        }
        int n4 = this.modalIndex();
        for (int i = this.children.size() - 1; i >= n4; --i) {
            if (!this.children.get(i).keyReleased(n, n2, n3)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char c, int n) {
        if (!this.inFlow()) {
            return false;
        }
        int n2 = this.modalIndex();
        for (int i = this.children.size() - 1; i >= n2; --i) {
            if (!this.children.get(i).charTyped(c, n)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(float f, float f2, float f3, float f4) {
        if (!this.inFlow() || !this.contains(f, f2)) {
            return false;
        }
        float f5 = f2;
        float f6 = f;
        if (this.layout.isVertical()) {
            f5 += this.scrollOffset;
        } else {
            f6 += this.scrollOffset;
        }
        int n = this.modalIndex();
        for (int i = this.children.size() - 1; i >= n; --i) {
            float f7;
            UiNode uiNode = this.children.get(i);
            float f8 = uiNode.isSticky() ? f : f6;
            float f9 = f7 = uiNode.isSticky() ? f2 : f5;
            if (!uiNode.mouseScrolled(f8, f7, f3, f4)) continue;
            return true;
        }
        if (n > 0) {
            return true;
        }
        if (this.scrollEnabled && this.contentHeight > this.viewportSize + 0.5f) {
            float f10 = this.layout.isVertical() ? f4 : f3;
            this.targetScrollOffset -= f10 * 22.0f;
            this.layoutState.show();
            return true;
        }
        return false;
    }

    public static interface RenderHook {
        public void render(RockstarDrawContext var1, Component var2);
    }
}
