/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec2f
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.ui.widgets.controls;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import moscow.rockstar.network.http.client.ReactorNettyClient;
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
import net.minecraft.util.math.Vec2f;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.utility.render.ColorRGBA;

public class Vector2Slider
extends UiNode {
    private final Supplier<Vec2f> firstPointSupplier;
    private final Supplier<Vec2f> secondPointSupplier;
    private final Consumer<Vec2f> firstPointConsumer;
    private final Consumer<Vec2f> secondPointConsumer;
    private final AnimatedValue firstPointX = new AnimatedValue(Motion.resolveMotionMotionFromLongAndEasing(500L, Easing.easeOutOvershoot));
    private final AnimatedValue firstPointY = new AnimatedValue(Motion.resolveMotionMotionFromLongAndEasing(500L, Easing.easeOutOvershoot));
    private final AnimatedValue secondPointX = new AnimatedValue(Motion.resolveMotionMotionFromLongAndEasing(500L, Easing.easeOutOvershoot));
    private final AnimatedValue secondPointY = new AnimatedValue(Motion.resolveMotionMotionFromLongAndEasing(500L, Easing.easeOutOvershoot));
    private boolean draggingFirstPoint;
    private boolean draggingSecondPoint;
    private Function<Vector2Slider, ColorRGBA> backgroundColorProvider = vector2Slider -> ColorPalette.MUTED_PANEL_COLOR;
    private Function<Vector2Slider, ColorRGBA> borderColorProvider = vector2Slider -> ColorPalette.BORDER_COLOR;
    private Function<Vector2Slider, ColorRGBA> guideColorProvider = vector2Slider -> ColorRGBA.WHITE.mulAlpha(0.55f + 0.15f * vector2Slider.hover());
    private Function<Vector2Slider, ColorRGBA> connectorColorProvider = vector2Slider -> ColorRGBA.WHITE;
    private Function<Vector2Slider, ColorRGBA> firstPointColorProvider = vector2Slider -> ColorRGBA.WHITE.mulAlpha(0.45f);
    private Function<Vector2Slider, ColorRGBA> secondPointColorProvider = vector2Slider -> ColorRGBA.WHITE;
    private float cornerRadius = 6.0f;
    private float guideCornerRadius = 4.0f;
    private float pointScale = 0.17f;
    private float pointPadding = 3.0f;
    private float secondPointRadius = 2.0f;
    private float dashLength = 3.0f;
    private float dashGap = 3.0f;
    private float curveThickness = 1.5f;
    private float endpointThickness = 1.0f;

    public Vector2Slider(Supplier<Vec2f> supplier, Supplier<Vec2f> supplier2, Consumer<Vec2f> consumer, Consumer<Vec2f> consumer2) {
        this.firstPointSupplier = supplier;
        this.secondPointSupplier = supplier2;
        this.firstPointConsumer = consumer;
        this.secondPointConsumer = consumer2;
        Vec2f VanillaAdventureTabAdvancementGenerator = supplier.get();
        Vec2f MagmaBlock = supplier2.get();
        this.firstPointX.snapTo(VanillaAdventureTabAdvancementGenerator.x);
        this.firstPointY.snapTo(VanillaAdventureTabAdvancementGenerator.y);
        this.secondPointX.snapTo(MagmaBlock.x);
        this.secondPointY.snapTo(MagmaBlock.y);
        this.snapSize();
        this.cursor(Cursor.HAND);
        this.onClick(this::beginDrag);
    }

    public Vector2Slider setBackgroundColor(ColorRGBA colorRGBA) {
        this.backgroundColorProvider = vector2Slider -> colorRGBA;
        return this;
    }

    public Vector2Slider setBorderColor(ColorRGBA colorRGBA) {
        this.borderColorProvider = vector2Slider -> colorRGBA;
        return this;
    }

    public Vector2Slider setGuideColor(ColorRGBA colorRGBA) {
        this.guideColorProvider = vector2Slider -> colorRGBA;
        return this;
    }

    public Vector2Slider setGuideColorProvider(Function<Vector2Slider, ColorRGBA> function) {
        this.guideColorProvider = function;
        return this;
    }

    public Vector2Slider setConnectorColor(ColorRGBA colorRGBA) {
        this.connectorColorProvider = vector2Slider -> colorRGBA;
        return this;
    }

    public Vector2Slider setFirstPointColor(ColorRGBA colorRGBA) {
        this.firstPointColorProvider = vector2Slider -> colorRGBA;
        return this;
    }

    public Vector2Slider setSecondPointColor(ColorRGBA colorRGBA) {
        this.secondPointColorProvider = vector2Slider -> colorRGBA;
        return this;
    }

    public Vector2Slider setCornerRadius(float f) {
        this.cornerRadius = f;
        return this;
    }

    public Vector2Slider setGuideCornerRadius(float f) {
        this.guideCornerRadius = f;
        return this;
    }

    public Vector2Slider setPointScale(float f) {
        this.pointScale = f;
        return this;
    }

    public Vector2Slider setPointPadding(float f) {
        this.pointPadding = f;
        return this;
    }

    public Vector2Slider setSecondPointRadius(float f) {
        this.secondPointRadius = f;
        return this;
    }

    public Vector2Slider setDashPattern(float f, float f2) {
        this.dashLength = f;
        this.dashGap = f2;
        return this;
    }

    public Vector2Slider setCurveThickness(float f) {
        this.curveThickness = f;
        return this;
    }

    public Vector2Slider setEndpointThickness(float f) {
        this.endpointThickness = f;
        return this;
    }

    public Vector2Slider setAnimationMotion(Motion motion) {
        if (motion != null) {
            this.firstPointX.motion(motion);
            this.firstPointY.motion(motion);
            this.secondPointX.motion(motion);
            this.secondPointY.motion(motion);
        }
        return this;
    }

    public Vector2Slider setWidth(float f) {
        super.width(f);
        return this;
    }

    public Vector2Slider setHeight(float f) {
        super.height(f);
        return this;
    }

    public Vector2Slider setSize(float f, float f2) {
        super.size(f, f2);
        return this;
    }

    @Override
    public Vector2Slider fillWidth() {
        super.fillWidth();
        return this;
    }

    @Override
    public Vector2Slider fillHeight() {
        super.fillHeight();
        return this;
    }

    private float getPointRadius() {
        return Math.min(this.w(), this.h()) * this.pointScale;
    }

    private float getInnerLeft() {
        return this.x() + this.getPointRadius();
    }

    private float getInnerTop() {
        return this.y() + this.getPointRadius();
    }

    private float getInnerWidth() {
        return Math.max(1.0f, this.w() - this.getPointRadius() * 2.0f);
    }

    private float getInnerHeight() {
        return Math.max(1.0f, this.h() - this.getPointRadius() * 2.0f);
    }

    private float mapNormalizedX(float f) {
        return this.getInnerLeft() + f * this.getInnerWidth();
    }

    private float mapNormalizedY(float f) {
        return this.getInnerTop() + f * this.getInnerHeight();
    }

    private float normalizeX(float f) {
        return (f - this.getInnerLeft()) / this.getInnerWidth();
    }

    private float normalizeY(float f) {
        return (f - this.getInnerTop()) / this.getInnerHeight();
    }

    private void beginDrag(PointerAction pointerAction, float f, float f2) {
        float f3;
        if (pointerAction != PointerAction.LEFT_CLICK) {
            return;
        }
        float f4 = Vector2Slider.distanceToPoint(f, f2, this.mapNormalizedX(this.firstPointX.getCurrent()), this.mapNormalizedY(this.firstPointY.getCurrent()));
        if (f4 <= (f3 = Vector2Slider.distanceToPoint(f, f2, this.mapNormalizedX(this.secondPointX.getCurrent()), this.mapNormalizedY(this.secondPointY.getCurrent())))) {
            this.draggingFirstPoint = true;
            this.updateFirstPoint(f, f2);
        } else {
            this.draggingSecondPoint = true;
            this.updateSecondPoint(f, f2);
        }
    }

    private void updateFirstPoint(float f, float f2) {
        this.firstPointConsumer.accept(this.createNormalizedPoint(f, f2));
    }

    private void updateSecondPoint(float f, float f2) {
        this.secondPointConsumer.accept(this.createNormalizedPoint(f, f2));
    }

    private Vec2f createNormalizedPoint(float f, float f2) {
        float f3 = this.pointPadding + 1.0f;
        float f4 = Vector2Slider.clampToRange(f, this.x() + f3, this.x() + this.w() - f3);
        float f5 = Vector2Slider.clampToRange(f2, this.y() + f3, this.y() + this.h() - f3);
        return new Vec2f(this.normalizeX(f4), this.normalizeY(f5));
    }

    @Override
    public void mouseReleased(float f, float f2, PointerAction pointerAction) {
        this.draggingFirstPoint = false;
        this.draggingSecondPoint = false;
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
        if (this.draggingFirstPoint) {
            this.updateFirstPoint(f2, f3);
        } else if (this.draggingSecondPoint) {
            this.updateSecondPoint(f2, f3);
        }
        Vec2f VanillaAdventureTabAdvancementGenerator = this.firstPointSupplier.get();
        Vec2f MagmaBlock = this.secondPointSupplier.get();
        this.firstPointX.setTarget(VanillaAdventureTabAdvancementGenerator.x);
        this.firstPointX.update(f);
        this.firstPointY.setTarget(VanillaAdventureTabAdvancementGenerator.y);
        this.firstPointY.update(f);
        this.secondPointX.setTarget(MagmaBlock.x);
        this.secondPointX.update(f);
        this.secondPointY.setTarget(MagmaBlock.y);
        this.secondPointY.update(f);
        if (this.draggingFirstPoint || this.draggingSecondPoint) {
            moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.CROSSHAIR);
        }
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        float f2 = this.x();
        float f3 = this.y();
        float f4 = this.w();
        float f5 = this.h();
        WidgetState widgetState = WidgetState.uniform(this.cornerRadius);
        ColorRGBA colorRGBA = this.backgroundColorProvider.apply(this);
        if (colorRGBA != null && colorRGBA.getAlpha() > 0.0f) {
            drawContext.drawRoundedRect(f2, f3, f4, f5, widgetState, colorRGBA);
        }
        MatrixStack class_45872 = drawContext.getMatrices();
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)class_45872, (float)f2, (float)f3, (float)f4, (float)f5);
        float f6 = this.getInnerLeft();
        float f7 = this.getInnerTop();
        float f8 = this.getInnerWidth();
        float f9 = this.getInnerHeight();
        drawContext.drawDashedBorder(f6, f7, f8, f9, 0.5f, WidgetState.uniform(this.guideCornerRadius), this.dashLength, this.dashGap, this.guideColorProvider.apply(this), 0.0f, 1.0f, 1.0f, 0.0f);
        Vec2f VanillaAdventureTabAdvancementGenerator = new Vec2f(this.mapNormalizedX(0.0f), this.mapNormalizedY(1.0f));
        Vec2f MagmaBlock = new Vec2f(this.mapNormalizedX(1.0f), this.mapNormalizedY(0.0f));
        Vec2f VanillaHusbandryTabAdvancementGenerator = new Vec2f(this.mapNormalizedX(this.firstPointX.getCurrent()), this.mapNormalizedY(this.firstPointY.getCurrent()));
        Vec2f BlockMirror = new Vec2f(this.mapNormalizedX(this.secondPointX.getCurrent()), this.mapNormalizedY(this.secondPointY.getCurrent()));
        ColorRGBA colorRGBA2 = this.firstPointColorProvider.apply(this);
        drawContext.drawSmoothBezier(f2, f3, f4, f5, VanillaAdventureTabAdvancementGenerator, VanillaAdventureTabAdvancementGenerator, VanillaHusbandryTabAdvancementGenerator, VanillaHusbandryTabAdvancementGenerator, this.endpointThickness, colorRGBA2);
        drawContext.drawSmoothBezier(f2, f3, f4, f5, MagmaBlock, MagmaBlock, BlockMirror, BlockMirror, this.endpointThickness, colorRGBA2);
        drawContext.drawSmoothBezier(f2, f3, f4, f5, VanillaAdventureTabAdvancementGenerator, VanillaHusbandryTabAdvancementGenerator, BlockMirror, MagmaBlock, this.curveThickness, this.connectorColorProvider.apply(this));
        ColorRGBA colorRGBA3 = this.secondPointColorProvider.apply(this);
        Vector2Slider.drawOutlinedPoint(drawContext, VanillaAdventureTabAdvancementGenerator, this.pointPadding, colorRGBA3);
        Vector2Slider.drawFilledPoint(drawContext, MagmaBlock, this.secondPointRadius, colorRGBA3);
        Vector2Slider.drawFilledPoint(drawContext, VanillaHusbandryTabAdvancementGenerator, this.pointPadding, colorRGBA3);
        Vector2Slider.drawFilledPoint(drawContext, BlockMirror, this.pointPadding, colorRGBA3);
        moscow.rockstar.render.state.UiScissorStack.pop();
        ColorRGBA colorRGBA4 = this.borderColorProvider.apply(this);
        if (colorRGBA4 != null && colorRGBA4.getAlpha() > 0.0f) {
            drawContext.drawRoundedBorder(f2, f3, f4, f5, 0.5f, widgetState, colorRGBA4);
        }
    }

    private static void drawOutlinedPoint(RockstarDrawContext drawContext, Vec2f VanillaAdventureTabAdvancementGenerator, float f, ColorRGBA colorRGBA) {
        drawContext.drawRoundedBorder(VanillaAdventureTabAdvancementGenerator.x - f, VanillaAdventureTabAdvancementGenerator.y - f, f * 2.0f, f * 2.0f, 1.0f, WidgetState.uniform(f), colorRGBA);
    }

    private static void drawFilledPoint(RockstarDrawContext drawContext, Vec2f VanillaAdventureTabAdvancementGenerator, float f, ColorRGBA colorRGBA) {
        drawContext.drawRoundedRect(VanillaAdventureTabAdvancementGenerator.x - f, VanillaAdventureTabAdvancementGenerator.y - f, f * 2.0f, f * 2.0f, WidgetState.uniform(f), colorRGBA);
    }

    private static float distanceToPoint(float f, float f2, float f3, float f4) {
        float f5 = f - f3;
        float f6 = f2 - f4;
        return (float)Math.sqrt(f5 * f5 + f6 * f6);
    }

    private static float clampToRange(float f, float f2, float f3) {
        return f < f2 ? f2 : (f > f3 ? f3 : f);
    }





}

