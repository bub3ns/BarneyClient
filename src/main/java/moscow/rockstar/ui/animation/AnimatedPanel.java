/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.ui.animation;

import java.util.function.Function;
import java.util.function.Supplier;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.animation.AnimatedValue;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.ItemGrid;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.utility.render.ColorRGBA;

public class AnimatedPanel
extends UiNode {
    private final ValueProvider xValueProvider;
    private final ValueProvider yValueProvider;
    private final PositionConsumer positionConsumer;
    private final float xStart;
    private final float xEnd;
    private final float yStart;
    private final float yEnd;
    private final AnimatedValue xAnimation = new AnimatedValue(Motion.resolveMotionMotionFromLongAndEasing(400L, Easing.easeOutOvershoot));
    private final AnimatedValue yAnimation = new AnimatedValue(Motion.resolveMotionMotionFromLongAndEasing(400L, Easing.easeOutOvershoot));
    private boolean pointerDragging;
    private Function<AnimatedPanel, ColorRGBA> backgroundColorProvider = animatedPanel -> ColorPalette.MUTED_PANEL_COLOR;
    private Function<AnimatedPanel, ColorRGBA> borderColorProvider = animatedPanel -> ColorPalette.BORDER_COLOR;
    private Function<AnimatedPanel, ColorRGBA> gridColorProvider = animatedPanel -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.05f + 0.03f * animatedPanel.hover());
    private Function<AnimatedPanel, ColorRGBA> indicatorColorProvider = animatedPanel -> ColorPalette.ACCENT_COLOR.mulAlpha(0.85f + 0.15f * animatedPanel.hover());
    private float gridSpacing = 3.0f;
    private int gridDivisions = 10;
    private float indicatorSize = 3.0f;
    private float indicatorRadius = 1.5f;
    private Supplier<String> labelSupplier;
    private FontMetrics labelFont;
    private Function<AnimatedPanel, ColorRGBA> labelColorProvider = animatedPanel -> ColorPalette.ACCENT_COLOR;

    public AnimatedPanel(ValueProvider valueProvider, ValueProvider valueProvider2, PositionConsumer positionConsumer, float f, float f2, float f3, float f4) {
        this.xValueProvider = valueProvider;
        this.yValueProvider = valueProvider2;
        this.positionConsumer = positionConsumer;
        this.xStart = f;
        this.xEnd = f2;
        this.yStart = f3;
        this.yEnd = f4;
        this.xAnimation.snapTo(valueProvider.get());
        this.yAnimation.snapTo(valueProvider2.get());
        this.snapSize();
        this.cursor(Cursor.CROSSHAIR);
        this.onClick(this::handlePointerAction);
    }

    public AnimatedPanel setBackgroundColor(ColorRGBA colorRGBA) {
        this.backgroundColorProvider = animatedPanel -> colorRGBA;
        return this;
    }

    public AnimatedPanel setBackgroundColorProvider(Function<AnimatedPanel, ColorRGBA> function) {
        this.backgroundColorProvider = function;
        return this;
    }

    public AnimatedPanel setBorderColor(ColorRGBA colorRGBA) {
        this.borderColorProvider = animatedPanel -> colorRGBA;
        return this;
    }

    public AnimatedPanel setBorderColorProvider(Function<AnimatedPanel, ColorRGBA> function) {
        this.borderColorProvider = function;
        return this;
    }

    public AnimatedPanel setGridColor(ColorRGBA colorRGBA) {
        this.gridColorProvider = animatedPanel -> colorRGBA;
        return this;
    }

    public AnimatedPanel setGridColorProvider(Function<AnimatedPanel, ColorRGBA> function) {
        this.gridColorProvider = function;
        return this;
    }

    public AnimatedPanel setIndicatorColor(ColorRGBA colorRGBA) {
        this.indicatorColorProvider = animatedPanel -> colorRGBA;
        return this;
    }

    public AnimatedPanel setIndicatorColorProvider(Function<AnimatedPanel, ColorRGBA> function) {
        this.indicatorColorProvider = function;
        return this;
    }

    public AnimatedPanel setGridSpacing(float f) {
        this.gridSpacing = f;
        return this;
    }

    public AnimatedPanel setGridDivisions(int n) {
        this.gridDivisions = Math.max(1, n);
        return this;
    }

    public AnimatedPanel setIndicatorSize(float f) {
        this.indicatorSize = f;
        return this;
    }

    public AnimatedPanel setIndicatorRadius(float f) {
        this.indicatorRadius = f;
        return this;
    }

    public AnimatedPanel setLabel(FontMetrics fontMetrics, Supplier<String> supplier) {
        this.labelFont = fontMetrics;
        this.labelSupplier = supplier;
        return this;
    }

    public AnimatedPanel setLabelColor(ColorRGBA colorRGBA) {
        this.labelColorProvider = animatedPanel -> colorRGBA;
        return this;
    }

    public AnimatedPanel setLabelColorProvider(Function<AnimatedPanel, ColorRGBA> function) {
        this.labelColorProvider = function;
        return this;
    }

    public AnimatedPanel setAnimationMotion(Motion motion) {
        if (motion != null) {
            this.xAnimation.motion(motion);
            this.yAnimation.motion(motion);
        }
        return this;
    }

    public AnimatedPanel setWidth(float f) {
        super.width(f);
        return this;
    }

    public AnimatedPanel setHeight(float f) {
        super.height(f);
        return this;
    }

    public AnimatedPanel setSize(float f, float f2) {
        super.size(f, f2);
        return this;
    }

    public AnimatedPanel fillWidthNode() {
        super.fillWidth();
        return this;
    }

    public AnimatedPanel fillHeightNode() {
        super.fillHeight();
        return this;
    }

    private void handlePointerAction(PointerAction pointerAction, float f, float f2) {
        if (pointerAction != PointerAction.LEFT_CLICK) {
            return;
        }
        this.pointerDragging = true;
        this.updatePositionFromPointer(f, f2);
    }

    private void updatePositionFromPointer(float f, float f2) {
        float f3 = AnimatedPanel.clampProgress((f - this.x()) / Math.max(1.0f, this.w()));
        float f4 = AnimatedPanel.clampProgress((f2 - this.y()) / Math.max(1.0f, this.h()));
        this.positionConsumer.accept(this.xStart + (this.xEnd - this.xStart) * f3, this.yStart + (this.yEnd - this.yStart) * f4);
    }

    @Override
    public void mouseReleased(float f, float f2, PointerAction pointerAction) {
        this.pointerDragging = false;
        super.mouseReleased(f, f2, pointerAction);
    }

    @Override
    protected void measure() {
        if (!this.explicitH) {
            this.prefH = this.w();
        }
    }

    @Override
    protected void onTick(float f, float f2, float f3) {
        this.xAnimation.setTarget(this.xValueProvider.get());
        this.xAnimation.update(f);
        this.yAnimation.setTarget(this.yValueProvider.get());
        this.yAnimation.update(f);
        if (this.pointerDragging) {
            this.updatePositionFromPointer(f2, f3);
        }
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        Object object;
        float f2;
        float f3 = this.x();
        float f4 = this.y();
        float f5 = this.w();
        float f6 = this.h();
        WidgetState widgetState = WidgetState.uniform(this.gridSpacing);
        ColorRGBA colorRGBA = this.backgroundColorProvider.apply(this);
        if (colorRGBA != null && colorRGBA.getAlpha() > 0.0f) {
            drawContext.drawRoundedRect(f3, f4, f5, f6, widgetState, colorRGBA);
        }
        MatrixStack class_45872 = drawContext.getMatrices();
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)class_45872, (float)f3, (float)f4, (float)f5, (float)f6);
        ColorRGBA colorRGBA2 = this.gridColorProvider.apply(this);
        if (colorRGBA2 != null && colorRGBA2.getAlpha() > 0.0f) {
            for (int i = 1; i < this.gridDivisions; ++i) {
                f2 = (float)i / (float)this.gridDivisions;
                drawContext.drawRect(f3 + f5 * f2, f4, 1.0f, f6, colorRGBA2);
                drawContext.drawRect(f3, f4 + f6 * f2, f5, 1.0f, colorRGBA2);
            }
        }
        float f7 = AnimatedPanel.clampProgress(AnimatedPanel.interpolateProgress(this.xAnimation.getCurrent(), this.xStart, this.xEnd));
        f2 = AnimatedPanel.clampProgress(AnimatedPanel.interpolateProgress(this.yAnimation.getCurrent(), this.yStart, this.yEnd));
        float f8 = f3 + f5 * f7;
        float f9 = f4 + f6 * f2;
        ColorRGBA colorRGBA3 = this.indicatorColorProvider.apply(this);
        float f10 = this.indicatorSize;
        AnimatedPanel.drawPositiveRect(drawContext, f8 - 0.5f, f4, 1.0f, f9 - f10 - f4, colorRGBA3);
        AnimatedPanel.drawPositiveRect(drawContext, f8 - 0.5f, f9 + f10, 1.0f, f4 + f6 - (f9 + f10), colorRGBA3);
        AnimatedPanel.drawPositiveRect(drawContext, f3, f9 - 0.5f, f8 - f10 - f3, 1.0f, colorRGBA3);
        AnimatedPanel.drawPositiveRect(drawContext, f8 + f10, f9 - 0.5f, f3 + f5 - (f8 + f10), 1.0f, colorRGBA3);
        drawContext.drawRoundedBorder(f8 - this.indicatorSize, f9 - this.indicatorSize, this.indicatorSize * 2.0f, this.indicatorSize * 2.0f, this.indicatorRadius / 2.0f, WidgetState.uniform(this.indicatorSize), colorRGBA3);
        if (this.labelSupplier != null && this.labelFont != null && (object = this.labelSupplier.get()) != null && !((String)object).isEmpty()) {
            float f11 = 3.0f;
            float f12 = this.labelFont.measureText((String)object) + f11 * 2.0f;
            float f13 = this.labelFont.getFontTopOffset() + f11 * 1.4f;
            float f14 = f3 + f5 - f12 - 2.0f;
            float f15 = f4 + 2.0f;
            drawContext.drawRoundedRect(f14, f15, f12, f13, WidgetState.uniform(2.5f), ColorPalette.PANEL_COLOR.mulAlpha(0.65f));
            drawContext.drawText(this.labelFont, (String)object, f14 + f11, f15 + f13 / 2.0f - this.labelFont.getFontTopOffset() / 2.0f, this.labelColorProvider.apply(this));
        }
        moscow.rockstar.render.state.UiScissorStack.pop();
        object = this.borderColorProvider.apply(this);
        if (object != null && ((ColorRGBA)object).getAlpha() > 0.0f) {
            drawContext.drawRoundedBorder(f3, f4, f5, f6, 0.5f, widgetState, (ColorRGBA)object);
        }
    }

    private static void drawPositiveRect(RockstarDrawContext drawContext, float f, float f2, float f3, float f4, ColorRGBA colorRGBA) {
        if (f3 > 0.0f && f4 > 0.0f) {
            drawContext.drawRect(f, f2, f3, f4, colorRGBA);
        }
    }

    private static float interpolateProgress(float f, float f2, float f3) {
        return f3 - f2 == 0.0f ? 0.0f : (f - f2) / (f3 - f2);
    }

    private static float clampProgress(float f) {
        return f < 0.0f ? 0.0f : (f > 1.0f ? 1.0f : f);
    }






    public static interface ValueProvider {
        public float get();
    }

    public static interface PositionConsumer {
        public void accept(float var1, float var2);
    }
}

