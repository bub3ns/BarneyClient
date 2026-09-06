/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.widgets.buttons;

import lombok.Generated;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.TextInputField;

public class TextButton {
    private final TextInputField label = new TextInputField(Font.REGULAR.metrics(7.0f));

    public TextButton(String string) {
        this.label.setPlaceholder(string);
    }

    public void render(RockstarDrawContext drawContext, float f, float f2, float f3, float f4) {
        this.label.setX(f);
        this.label.setY(f2);
        this.label.setWidth(f3);
        this.label.setHeight(f4);
        drawContext.drawRoundedRect(f, f2, f3, f4, WidgetState.uniform(4.0f), this.contains(drawContext.mouseX(), drawContext.mouseY()) ? ColorPalette.getPanelBackgroundColor().withAlpha(170.0f) : ColorPalette.getPanelBackgroundColor().withAlpha(150.0f));
        this.label.render(drawContext);
    }

    public boolean contains(double d, double d2) {
        return this.label.contains(d, d2);
    }

    public String getText() {
        return this.label.getText();
    }

    public void setText(String string) {
        this.label.setText(string);
    }

    public void setSelected(boolean bl) {
        this.label.setFocused(bl);
    }

    public boolean isSelected() {
        return this.label.isFocused();
    }

    @Generated
    public TextInputField getLabelLayout() {
        return this.label;
    }
}
