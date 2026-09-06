package moscow.rockstar.ui.widgets.settings;

import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.settings.SettingWidget;
import moscow.rockstar.ui.text.FontMetrics;
import pyrock.utility.render.ColorRGBA;

/**
 * Two digit rolling counter badge (original rockstar/ilIlil/IiIIiIiIi).
 *
 * Each digit keeps its own animation; when a digit changes the previous glyph
 * rolls down out of the box while the new one rolls in from above.
 */
public class RollingCountBadge
extends SettingWidget {
    private boolean padLeadingZero;
    private ColorRGBA color;
    private final float rollHeight;
    private final FontMetrics fontMetrics;
    private final String[] digits = new String[]{"", ""};
    private final String[] previousDigits = new String[]{"", ""};
    private final Animation[] rollAnimations;
    private float secondDigitOffset;

    public RollingCountBadge(FontMetrics fontMetrics, float f, long l, Easing easing) {
        this.fontMetrics = fontMetrics;
        this.rollHeight = f;
        this.rollAnimations = new Animation[2];
        for (int i = 0; i < this.rollAnimations.length; ++i) {
            this.rollAnimations[i] = new Animation(l, easing);
        }
    }

    @Override
    protected void renderContent(RockstarDrawContext drawContext) {
        for (Animation animation : this.rollAnimations) {
            animation.update(1.0f);
        }
        drawContext.drawText(this.fontMetrics, this.previousDigits[0], this.x, this.y + this.rollHeight * this.rollAnimations[0].getValue(), this.color.withAlpha(this.color.getAlpha() * (1.0f - this.rollAnimations[0].getValue())));
        drawContext.drawText(this.fontMetrics, this.digits[0], this.x, this.y - this.rollHeight + this.rollHeight * this.rollAnimations[0].getValue(), this.color.withAlpha(this.color.getAlpha() * this.rollAnimations[0].getValue()));
        drawContext.drawText(this.fontMetrics, this.previousDigits[1], this.x + this.secondDigitOffset, this.y + this.rollHeight * this.rollAnimations[1].getValue(), this.color.withAlpha(this.color.getAlpha() * (1.0f - this.rollAnimations[1].getValue())));
        drawContext.drawText(this.fontMetrics, this.digits[1], this.x + this.fontMetrics.measureText(this.digits[0]), this.y - this.rollHeight + this.rollHeight * this.rollAnimations[1].getValue(), this.color.withAlpha(this.color.getAlpha() * this.rollAnimations[1].getValue()));
    }

    @Override
    public float getWidth() {
        return this.fontMetrics.measureText(this.digits[0] + this.digits[1]);
    }

    public void setValue(int n) {
        String string = String.valueOf(n / 10);
        String string2 = String.valueOf(n % 10);
        if (!string2.equals(this.digits[1])) {
            this.secondDigitOffset = this.fontMetrics.measureText(this.digits[0]);
            this.previousDigits[1] = this.digits[1];
            this.digits[1] = string2;
            this.rollAnimations[1].setValue(0.0f);
        }
        if (!string.equals(this.digits[0])) {
            this.previousDigits[0] = this.digits[0];
            this.digits[0] = this.padLeadingZero ? string : (string.equals("0") ? "" : string);
            this.rollAnimations[0].setValue(0.0f);
        }
    }

    public void configure(boolean bl, ColorRGBA colorRGBA) {
        this.padLeadingZero = bl;
        this.color = colorRGBA;
    }
}
