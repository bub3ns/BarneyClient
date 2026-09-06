/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  moscow.rockstar.modules.visuals.effects.EffectEntry
 */
package moscow.rockstar.ui.widgets.settings;

import java.util.function.Consumer;
import lombok.Generated;
import moscow.rockstar.modules.visuals.effects.EffectEntry;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.particles.AmbientParticleRenderer;
import moscow.rockstar.ui.color.ColorPickerNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.widgets.buttons.TextButton;
import pyrock.utility.render.ColorRGBA;

public class IntegerSettingComponent
implements EffectEntry,
ColorPickerNode {
    private final String displayName;
    private final TextButton valueButton;
    private final int maximumValue;
    private final Consumer<Integer> valueChangeHandler;
    private int currentValue;

    public IntegerSettingComponent(String string, int n, int n2, Consumer<Integer> consumer) {
        this.displayName = string;
        this.maximumValue = n;
        this.currentValue = n2;
        this.valueChangeHandler = consumer;
        this.valueButton = new TextButton("");
        this.valueButton.setText(String.valueOf(n2));
    }

    public float getHeight() {
        return 20.0f;
    }

    public void render(RockstarDrawContext drawContext, float f, float f2, float f3) {
        boolean bl = this.currentValue > 0;
        ColorRGBA colorRGBA = bl ? ColorPalette.getAccentColor().withAlpha(50.0f) : ColorPalette.getPanelBackgroundColor();
        drawContext.drawRoundedRect(f, f2, f3, 17.0f, WidgetState.uniform(3.0f), colorRGBA);
        drawContext.drawText(Font.REGULAR.metrics(7.0f), this.displayName, f + 6.0f, f2 + 6.0f, ColorPalette.getPrimaryTextColor());
        if (bl) {
            float f4 = 18.0f;
            this.valueButton.render(drawContext, f + f3 - f4 - 2.0f, f2 + 2.0f, f4, 14.0f);
        }
    }

    public boolean handleClick(AmbientParticleRenderer ambientParticleRenderer, double d, double d2, int n) {
        boolean bl;
        boolean bl2 = bl = this.currentValue > 0;
        if (bl && this.valueButton.contains(d, d2)) {
            this.valueButton.setSelected(true);
            return true;
        }
        if (bl) {
            this.currentValue = 0;
            this.valueButton.setText("0");
            this.valueChangeHandler.accept(0);
        } else {
            this.currentValue = 1;
            this.valueButton.setText("1");
            this.valueChangeHandler.accept(1);
        }
        return true;
    }

    public void clampAndNotify() {
        int clampedValue = Math.max(0, Math.min(this.maximumValue, this.currentValue));
        this.currentValue = clampedValue;
        this.valueButton.setText(String.valueOf(clampedValue));
        this.valueChangeHandler.accept(clampedValue);
    }

    @Override
    public boolean isEditing() {
        return this.valueButton.isSelected();
    }

    @Override
    public void finishEditing() {
        if (this.valueButton.isSelected()) {
            this.clampAndNotify();
            this.valueButton.setSelected(false);
        }
    }

    @Generated
    public String getDisplayName() {
        return this.displayName;
    }

    @Generated
    public TextButton getValueButton() {
        return this.valueButton;
    }

    @Generated
    public int getMaximumValue() {
        return this.maximumValue;
    }

    @Generated
    public Consumer<Integer> getValueChangeHandler() {
        return this.valueChangeHandler;
    }

    @Generated
    public int getCurrentValue() {
        return this.currentValue;
    }
}
