/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Text
 */
package moscow.rockstar.ui.text;

import lombok.Generated;
import moscow.rockstar.render.text.FontRenderer;
import moscow.rockstar.ui.widgets.buttons.TextButton;
import net.minecraft.text.Text;

public class FontMetrics {
    private FontRenderer fontRenderer;
    private float fontScale;

    public float getFontMetricsFloat() {
        return this.fontRenderer.getLineHeight(this.fontScale);
    }

    public float getFontAscent() {
        return this.fontRenderer.getAscent() * this.fontScale;
    }

    public float getFontTopOffset() {
        return this.fontRenderer.getBaseline() * this.fontScale;
    }

    public float getFontBottomOffset() {
        return -this.fontRenderer.getDescent() * this.fontScale;
    }

    public float measureText(String string) {
        return this.fontRenderer.measure(moscow.rockstar.render.text.TextCaptureController.transform(string), this.fontScale);
    }

    public float measureTextComponent(Text class_25612) {
        return this.fontRenderer.measure(class_25612, this.fontScale);
    }

    public float measureCharacter(char c) {
        return this.fontRenderer.measureCharacter(c, this.fontScale);
    }

    @Generated
    public FontRenderer getFontRenderer() {
        return this.fontRenderer;
    }

    @Generated
    public float getFontScale() {
        return this.fontScale;
    }

    @Generated
    public FontMetrics(FontRenderer fontRenderer, float f) {
        this.fontRenderer = fontRenderer;
        this.fontScale = f;
    }
}
