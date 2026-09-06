package moscow.rockstar.ui.text;

import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.animation.AnimatedValue;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.core.UiNode;
import pyrock.utility.render.ColorRGBA;

/**
 * Two-digit rolling number node (original rockstar/ilIlil/Iiii).
 *
 * The value is clamped to 0..99 and split into two digit slots. When a slot's
 * glyph changes the old glyph rolls out while the new one rolls in; each slot
 * owns its own {@link AnimatedValue} driven by a 500ms easeOutBack motion.
 */
public class RollingNumberComponent extends UiNode {
    private final FontMetrics font;
    private final IntSupplier valueSupplier;
    private float rollHeight;
    private boolean padLeadingZero;
    private Function<RollingNumberComponent, ColorRGBA> colorProvider;
    private final String[] previousDigits = new String[]{"", ""};
    private final String[] digits;
    private final AnimatedValue[] rollAnimations;
    private int lastValue;

    public RollingNumberComponent(FontMetrics fontMetrics, IntSupplier intSupplier) {
        this.digits = new String[]{"", ""};
        this.rollAnimations = new AnimatedValue[]{
            new AnimatedValue(1.0f, Motion.resolveMotionMotionFromLongAndEasing(500L, Easing.easeOutBack)),
            new AnimatedValue(1.0f, Motion.resolveMotionMotionFromLongAndEasing(500L, Easing.easeOutBack))};
        this.lastValue = Integer.MIN_VALUE;
        this.font = fontMetrics;
        this.valueSupplier = intSupplier;
        this.height(fontMetrics.getFontTopOffset());
    }

    /** Original I(F) - the vertical distance a digit travels while rolling. */
    public RollingNumberComponent rollHeight(float f) {
        this.rollHeight = f;
        return this;
    }

    /** Original I() - always render the tens digit, even when it is zero. */
    public RollingNumberComponent padLeadingZero() {
        this.padLeadingZero = true;
        return this;
    }

    public RollingNumberComponent padLeadingZero(boolean bl) {
        this.padLeadingZero = bl;
        return this;
    }

    public RollingNumberComponent setColor(ColorRGBA colorRGBA) {
        this.colorProvider = rollingNumberComponent -> colorRGBA;
        return this;
    }

    public RollingNumberComponent setColorProvider(Supplier<ColorRGBA> supplier) {
        this.colorProvider = rollingNumberComponent -> (ColorRGBA)supplier.get();
        return this;
    }

    public RollingNumberComponent setColorProvider(Function<RollingNumberComponent, ColorRGBA> function) {
        this.colorProvider = function;
        return this;
    }

    /** Original I(IIii) - replaces the motion of both digit animations. */
    public RollingNumberComponent rollMotion(Motion motion) {
        if (motion != null) {
            this.rollAnimations[0].motion(motion);
            this.rollAnimations[1].motion(motion);
        }
        return this;
    }

    @Override
    public RollingNumberComponent width(float f) {
        super.width(f);
        return this;
    }

    @Override
    public RollingNumberComponent height(float f) {
        super.height(f);
        return this;
    }

    @Override
    public RollingNumberComponent size(float f, float f2) {
        super.size(f, f2);
        return this;
    }

    @Override
    public RollingNumberComponent fillWidth() {
        super.fillWidth();
        return this;
    }

    @Override
    public RollingNumberComponent fillHeight() {
        super.fillHeight();
        return this;
    }

    private void updateDigits() {
        int value = this.valueSupplier.getAsInt();
        if (value == this.lastValue) {
            return;
        }
        this.lastValue = value;
        int clamped = Math.max(0, Math.min(99, value));
        String tens = String.valueOf(clamped / 10);
        String first = this.padLeadingZero ? tens : (tens.equals("0") ? "" : tens);
        String second = String.valueOf(clamped % 10);
        if (!first.equals(this.digits[0])) {
            this.previousDigits[0] = this.digits[0];
            this.digits[0] = first;
            this.rollAnimations[0].snapTo(0.0f);
            this.rollAnimations[0].setTarget(1.0f);
        }
        if (!second.equals(this.digits[1])) {
            this.previousDigits[1] = this.digits[1];
            this.digits[1] = second;
            this.rollAnimations[1].snapTo(0.0f);
            this.rollAnimations[1].setTarget(1.0f);
        }
    }

    @Override
    protected void measure() {
        this.updateDigits();
        if (!this.explicitW) {
            this.prefW = this.font.measureText(this.digits[0] + this.digits[1]);
        }
        if (!this.explicitH) {
            this.prefH = this.font.getFontTopOffset();
        }
    }

    @Override
    protected void onTick(float f, float f2, float f3) {
        this.updateDigits();
        this.rollAnimations[0].update(f);
        this.rollAnimations[1].update(f);
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        ColorRGBA colorRGBA = this.colorProvider.apply(this);
        if (colorRGBA == null) {
            return;
        }
        float f2 = this.x();
        float f3 = this.y();
        float f4 = f3 + this.h() / 2.0f - this.font.getFontTopOffset() / 2.0f;
        float f5 = this.rollAnimations[0].getCurrent();
        float f6 = this.rollAnimations[1].getCurrent();
        float f7 = this.font.measureText(this.digits[0]);
        float f8 = this.font.measureText(this.previousDigits[0]);
        this.drawDigit(drawContext, this.previousDigits[0], f2, f4, colorRGBA, f5, true);
        this.drawDigit(drawContext, this.digits[0], f2, f4, colorRGBA, f5, false);
        this.drawDigit(drawContext, this.previousDigits[1], f2 + f8, f4, colorRGBA, f6, true);
        this.drawDigit(drawContext, this.digits[1], f2 + f7, f4, colorRGBA, f6, false);
    }

    private void drawDigit(RockstarDrawContext drawContext, String string, float f, float f2,
                           ColorRGBA colorRGBA, float f3, boolean bl) {
        if (string == null || string.isEmpty()) {
            return;
        }
        float f4 = bl ? 1.0f - f3 : f3;
        if (f4 <= 0.001f) {
            return;
        }
        float f5 = bl ? this.rollHeight * f3 : -this.rollHeight + this.rollHeight * f3;
        drawContext.drawText(this.font, string, f, f2 + f5, colorRGBA.withAlpha(colorRGBA.getAlpha() * f4));
    }
}
