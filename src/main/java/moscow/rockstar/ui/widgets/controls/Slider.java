/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.widgets.controls;

import java.util.function.Function;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.animation.AnimatedValue;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.PointerAction;
import pyrock.utility.render.ColorRGBA;

public class Slider
extends UiNode {
    private final ValueProvider valueProvider;
    private final ValueConsumer valueConsumer;
    private final float minimumValue;
    private final float maximumValue;
    private float stepSize;
    private Function<Slider, ColorRGBA> trackColorProvider = slider -> new ColorRGBA(35.0f, 32.0f, 50.0f);
    private Function<Slider, ColorRGBA> filledTrackColorProvider = slider -> ColorPalette.ACCENT_COLOR;
    private Function<Slider, ColorRGBA> thumbBorderColorProvider = slider -> ColorPalette.ACCENT_COLOR;
    private Function<Slider, ColorRGBA> thumbFillColorProvider;
    private float trackHeight = 2.5f;
    private float thumbRadius = 4.0f;
    private float thumbInset = 1.2f;
    private final AnimatedValue animatedValue = new AnimatedValue(Motion.motion);
    private boolean dragging;

    public Slider(ValueProvider valueProvider, ValueConsumer valueConsumer, float f, float f2) {
        this.valueProvider = valueProvider;
        this.valueConsumer = valueConsumer;
        this.minimumValue = f;
        this.maximumValue = f2;
        this.setSize(120.0f, 10.0f);
        this.cursor(Cursor.HAND);
        this.onClick(this::handlePointerAction);
    }

    public Slider setStepSize(float f) {
        this.stepSize = f;
        return this;
    }

    public Slider setTrackColor(ColorRGBA colorRGBA) {
        this.trackColorProvider = slider -> colorRGBA;
        return this;
    }

    public Slider setTrackColorProvider(Function<Slider, ColorRGBA> function) {
        this.trackColorProvider = function;
        return this;
    }

    public Slider setFilledTrackColor(ColorRGBA colorRGBA) {
        this.filledTrackColorProvider = slider -> colorRGBA;
        return this;
    }

    public Slider setFilledTrackColorProvider(Function<Slider, ColorRGBA> function) {
        this.filledTrackColorProvider = function;
        return this;
    }

    public Slider setThumbBorderColor(ColorRGBA colorRGBA) {
        this.thumbBorderColorProvider = slider -> colorRGBA;
        return this;
    }

    public Slider setThumbBorderColorProvider(Function<Slider, ColorRGBA> function) {
        this.thumbBorderColorProvider = function;
        return this;
    }

    public Slider setThumbFillColor(ColorRGBA colorRGBA) {
        this.thumbFillColorProvider = slider -> colorRGBA;
        return this;
    }

    public Slider setThumbFillColorProvider(Function<Slider, ColorRGBA> function) {
        this.thumbFillColorProvider = function;
        return this;
    }

    public Slider setTrackHeight(float f) {
        this.trackHeight = f;
        return this;
    }

    public Slider setThumbRadius(float f) {
        this.thumbRadius = f;
        return this;
    }

    public Slider setThumbInset(float f) {
        this.thumbInset = f;
        return this;
    }

    public Slider setAnimationMotion(Motion motion) {
        if (motion != null) {
            this.animatedValue.motion(motion);
        }
        return this;
    }

    public Slider setWidth(float f) {
        super.width(f);
        return this;
    }

    public Slider setHeight(float f) {
        super.height(f);
        return this;
    }

    public Slider setSize(float f, float f2) {
        super.size(f, f2);
        return this;
    }

    public Slider fillWidthNode() {
        super.fillWidth();
        return this;
    }

    public Slider fillHeightNode() {
        super.fillHeight();
        return this;
    }

    private void handlePointerAction(PointerAction pointerAction, float f, float f2) {
        if (pointerAction != PointerAction.LEFT_CLICK) {
            return;
        }
        this.dragging = true;
        this.updateValueFromMouse(f);
    }

    private void updateValueFromMouse(float f) {
        float f2 = (f - this.x()) / Math.max(1.0f, this.w());
        f2 = Math.max(0.0f, Math.min(1.0f, f2));
        float f3 = this.minimumValue + (this.maximumValue - this.minimumValue) * f2;
        if (this.stepSize > 0.0f) {
            f3 = (float)Math.round(f3 / this.stepSize) * this.stepSize;
        }
        this.valueConsumer.accept(f3);
    }

    @Override
    public void mouseReleased(float f, float f2, PointerAction pointerAction) {
        this.dragging = false;
        super.mouseReleased(f, f2, pointerAction);
    }

    @Override
    protected void onTick(float f, float f2, float f3) {
        this.animatedValue.setTarget(this.valueProvider.get());
        this.animatedValue.update(f);
        if (this.dragging && !this.pressed()) {
            this.dragging = false;
        }
        if (this.dragging) {
            this.updateValueFromMouse(f2);
        }
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        float f2;
        float f3 = this.x();
        float f4 = this.y();
        float f5 = this.w();
        float f6 = this.h();
        float f7 = this.maximumValue - this.minimumValue;
        float f8 = this.animatedValue.getCurrent();
        float f9 = f7 <= 0.0f ? 0.0f : (f8 - this.minimumValue) / f7;
        f9 = Math.max(0.0f, Math.min(1.0f, f9));
        float f10 = f5 * f9;
        float f11 = f4 + f6 / 2.0f - this.trackHeight / 2.0f;
        float f12 = this.trackHeight / 2.0f - 1.0f;
        ColorRGBA colorRGBA = this.trackColorProvider.apply(this);
        ColorRGBA colorRGBA2 = this.filledTrackColorProvider.apply(this);
        ColorRGBA colorRGBA3 = this.thumbBorderColorProvider.apply(this);
        ColorRGBA colorRGBA4 = this.thumbFillColorProvider != null ? this.thumbFillColorProvider.apply(this) : colorRGBA;
        float f13 = 2.5f;
        float f14 = Math.max(0.0f, f10 - f13);
        float f15 = f3 + Math.min(f5, f10 + f13);
        float f16 = Math.max(0.0f, f3 + f5 - f15);
        if (f16 > 0.0f && colorRGBA != null && colorRGBA.getAlpha() > 0.0f) {
            drawContext.drawRoundedRect(f15, f11, f16, this.trackHeight, WidgetState.right(f12, f12), colorRGBA);
        }
        if (f14 > 0.0f && colorRGBA2 != null && colorRGBA2.getAlpha() > 0.0f) {
            drawContext.drawRoundedRect(f3, f11, f14, this.trackHeight, WidgetState.left(f12, f12), colorRGBA2);
        }
        float f17 = f3 + f10;
        float f18 = f4 + f6 / 2.0f;
        if (colorRGBA3 != null && colorRGBA3.getAlpha() > 0.0f) {
            drawContext.drawRoundedBorder(f17 - this.thumbRadius, f18 - this.thumbRadius, this.thumbRadius * 2.0f, this.thumbRadius * 2.0f, this.thumbInset / 2.0f, WidgetState.uniform(this.thumbRadius), colorRGBA3);
        }
        if ((f2 = this.thumbRadius - this.thumbInset) > 0.0f && colorRGBA4 != null && colorRGBA4.getAlpha() > 0.0f) {
            drawContext.drawRoundedRect(f17 - f2, f18 - f2, f2 * 2.0f, f2 * 2.0f, WidgetState.uniform(f2), colorRGBA4);
        }
    }






    public static interface ValueProvider {
        public float get();
    }

    public static interface ValueConsumer {
        public void accept(float var1);
    }
}

