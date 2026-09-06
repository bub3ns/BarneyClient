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

public class RangeSlider
extends UiNode {
    private final ValueProvider lowerValueProvider;
    private final ValueProvider upperValueProvider;
    private final ValueConsumer lowerValueConsumer;
    private final ValueConsumer upperValueConsumer;
    private final float minimumValue;
    private final float maximumValue;
    private float stepSize;
    private Function<RangeSlider, ColorRGBA> trackColorProvider = rangeSlider -> new ColorRGBA(35.0f, 32.0f, 50.0f);
    private Function<RangeSlider, ColorRGBA> rangeColorProvider = rangeSlider -> ColorPalette.ACCENT_COLOR;
    private float trackHeight = 2.5f;
    private float thumbRadius = 4.0f;
    private float thumbInset = 1.2f;
    private final AnimatedValue lowerAnimatedValue = new AnimatedValue(Motion.motion);
    private final AnimatedValue upperAnimatedValue = new AnimatedValue(Motion.motion);
    private int activeHandle;

    public RangeSlider(ValueProvider valueProvider, ValueConsumer valueConsumer, ValueProvider valueProvider2, ValueConsumer valueConsumer2, float f, float f2) {
        this.lowerValueProvider = valueProvider;
        this.lowerValueConsumer = valueConsumer;
        this.upperValueProvider = valueProvider2;
        this.upperValueConsumer = valueConsumer2;
        this.minimumValue = f;
        this.maximumValue = f2;
        this.setSize(120.0f, 10.0f);
        this.cursor(Cursor.HAND);
        this.onClick(this::handlePointerAction);
    }

    public RangeSlider setStepSize(float f) {
        this.stepSize = f;
        return this;
    }

    public RangeSlider setTrackColor(ColorRGBA colorRGBA) {
        this.trackColorProvider = rangeSlider -> colorRGBA;
        return this;
    }

    public RangeSlider setTrackColorProvider(Function<RangeSlider, ColorRGBA> function) {
        this.trackColorProvider = function;
        return this;
    }

    public RangeSlider setRangeColor(ColorRGBA colorRGBA) {
        this.rangeColorProvider = rangeSlider -> colorRGBA;
        return this;
    }

    public RangeSlider setRangeColorProvider(Function<RangeSlider, ColorRGBA> function) {
        this.rangeColorProvider = function;
        return this;
    }

    public RangeSlider setTrackHeight(float f) {
        this.trackHeight = f;
        return this;
    }

    public RangeSlider setThumbRadius(float f) {
        this.thumbRadius = f;
        return this;
    }

    public RangeSlider setThumbInset(float f) {
        this.thumbInset = f;
        return this;
    }

    public RangeSlider setAnimationMotion(Motion motion) {
        if (motion != null) {
            this.lowerAnimatedValue.motion(motion);
            this.upperAnimatedValue.motion(motion);
        }
        return this;
    }

    public RangeSlider setWidth(float f) {
        super.width(f);
        return this;
    }

    public RangeSlider setHeight(float f) {
        super.height(f);
        return this;
    }

    public RangeSlider setSize(float f, float f2) {
        super.size(f, f2);
        return this;
    }

    public RangeSlider fillWidthNode() {
        super.fillWidth();
        return this;
    }

    public RangeSlider fillHeightNode() {
        super.fillHeight();
        return this;
    }

    private void handlePointerAction(PointerAction pointerAction, float f, float f2) {
        float f3;
        if (pointerAction != PointerAction.LEFT_CLICK) {
            return;
        }
        float f4 = this.valueToPixel(this.lowerValueProvider.get());
        float f5 = this.valueToPixel(this.upperValueProvider.get());
        float f6 = Math.abs(f - f4);
        this.activeHandle = f6 < (f3 = Math.abs(f - f5)) ? 1 : (f3 < f6 ? 2 : (f >= f4 ? 2 : 1));
        this.updateValueFromMouse(f);
    }

    private float valueToPixel(float f) {
        float f2 = this.maximumValue - this.minimumValue;
        return this.x() + this.w() * RangeSlider.clampProgress(f2 <= 0.0f ? 0.0f : (f - this.minimumValue) / f2);
    }

    private void updateValueFromMouse(float f) {
        float f2 = RangeSlider.clampProgress((f - this.x()) / Math.max(1.0f, this.w()));
        float f3 = this.minimumValue + (this.maximumValue - this.minimumValue) * f2;
        if (this.stepSize > 0.0f) {
            f3 = (float)Math.round(f3 / this.stepSize) * this.stepSize;
        }
        if (this.activeHandle == 1) {
            this.lowerValueConsumer.accept(Math.min(f3, this.upperValueProvider.get()));
        } else if (this.activeHandle == 2) {
            this.upperValueConsumer.accept(Math.max(f3, this.lowerValueProvider.get()));
        }
    }

    @Override
    public void mouseReleased(float f, float f2, PointerAction pointerAction) {
        this.activeHandle = 0;
        super.mouseReleased(f, f2, pointerAction);
    }

    @Override
    protected void onTick(float f, float f2, float f3) {
        this.lowerAnimatedValue.setTarget(this.lowerValueProvider.get());
        this.upperAnimatedValue.setTarget(this.upperValueProvider.get());
        this.lowerAnimatedValue.update(f);
        this.upperAnimatedValue.update(f);
        if (this.activeHandle != 0 && !this.pressed()) {
            this.activeHandle = 0;
        }
        if (this.activeHandle != 0) {
            this.updateValueFromMouse(f2);
        }
    }

    private static float clampProgress(float f) {
        return f < 0.0f ? 0.0f : (f > 1.0f ? 1.0f : f);
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        float f2;
        float f3 = this.x();
        float f4 = this.y();
        float f5 = this.w();
        float f6 = this.h();
        float f7 = this.maximumValue - this.minimumValue;
        float f8 = RangeSlider.clampProgress(f7 <= 0.0f ? 0.0f : (this.lowerAnimatedValue.getCurrent() - this.minimumValue) / f7);
        float f9 = RangeSlider.clampProgress(f7 <= 0.0f ? 0.0f : (this.upperAnimatedValue.getCurrent() - this.minimumValue) / f7);
        float f10 = f3 + f5 * Math.min(f8, f9);
        float f11 = f3 + f5 * Math.max(f8, f9);
        float f12 = f4 + f6 / 2.0f - this.trackHeight / 2.0f;
        float f13 = this.trackHeight / 2.0f;
        ColorRGBA colorRGBA = this.trackColorProvider.apply(this);
        ColorRGBA colorRGBA2 = this.rangeColorProvider.apply(this);
        if (colorRGBA != null && colorRGBA.getAlpha() > 0.0f) {
            drawContext.drawRoundedRect(f3, f12, f5, this.trackHeight, WidgetState.uniform(f13), colorRGBA);
        }
        if ((f2 = Math.max(0.0f, f11 - f10 - 5.0f)) > 0.0f && colorRGBA2 != null && colorRGBA2.getAlpha() > 0.0f) {
            drawContext.drawRect(f10 + 2.5f, f12, f2, this.trackHeight, colorRGBA2);
        }
        float f14 = f4 + f6 / 2.0f;
        this.drawThumb(drawContext, f3 + f5 * f8, f14, colorRGBA2, colorRGBA);
        this.drawThumb(drawContext, f3 + f5 * f9, f14, colorRGBA2, colorRGBA);
    }

    private void drawThumb(RockstarDrawContext drawContext, float f, float f2, ColorRGBA colorRGBA, ColorRGBA colorRGBA2) {
        float f3;
        if (colorRGBA != null && colorRGBA.getAlpha() > 0.0f) {
            drawContext.drawRoundedBorder(f - this.thumbRadius, f2 - this.thumbRadius, this.thumbRadius * 2.0f, this.thumbRadius * 2.0f, this.thumbInset / 2.0f, WidgetState.uniform(this.thumbRadius), colorRGBA);
        }
        if ((f3 = this.thumbRadius - this.thumbInset) > 0.0f && colorRGBA2 != null && colorRGBA2.getAlpha() > 0.0f) {
            drawContext.drawRoundedRect(f - f3, f2 - f3, f3 * 2.0f, f3 * 2.0f, WidgetState.uniform(f3), colorRGBA2);
        }
    }






    public static interface ValueProvider {
        public float get();
    }

    public static interface ValueConsumer {
        public void accept(float var1);
    }
}

