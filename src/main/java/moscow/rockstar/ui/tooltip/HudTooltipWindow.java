package moscow.rockstar.ui.tooltip;

import lombok.Generated;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.settings.SettingWidget;
import moscow.rockstar.ui.text.FontMetrics;
import pyrock.utility.render.ColorRGBA;

/**
 * The HUD hover-description window (original rockstar/ilIlil/IiIIiIiiI).
 *
 * <p>Cross-fades the previous label out upwards while the new one slides in from
 * {@code -verticalOffset}.  Drawn through the inherited
 * {@link SettingWidget#render(RockstarDrawContext)} entry point.</p>
 */
public class HudTooltipWindow
extends SettingWidget {
    private final float verticalOffset;
    private final FontMetrics font;
    private String previousText = "";
    private String text = "";
    private final Animation animation;
    private boolean centered;

    public HudTooltipWindow(FontMetrics fontMetrics, float f, long l, Easing easing) {
        this.font = fontMetrics;
        this.verticalOffset = f;
        this.animation = new Animation(l, easing);
    }

    @Override
    public void renderContent(RockstarDrawContext drawContext) {
        this.animation.update(1.0f);
        drawContext.drawText(this.font, this.previousText, this.x - (this.centered ? this.font.measureText(this.previousText) / 2.0f : 0.0f), this.y + this.verticalOffset * this.animation.getValue(), ColorRGBA.WHITE.withAlpha(255.0f * (1.0f - this.animation.getValue())));
        drawContext.drawText(this.font, this.text, this.x - (this.centered ? this.font.measureText(this.text) / 2.0f : 0.0f), this.y - this.verticalOffset + this.verticalOffset * this.animation.getValue(), ColorRGBA.WHITE.withAlpha(255.0f * this.animation.getValue()));
    }

    public HudTooltipWindow build() {
        this.centered = true;
        return this;
    }

    public void setText(String string) {
        if (this.text.equals(string)) {
            return;
        }
        this.previousText = this.text;
        this.text = string;
        this.animation.setValue(0.0f);
    }

    @Generated
    public FontMetrics getFont() {
        return this.font;
    }
}
