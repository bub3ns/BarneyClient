/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonPrimitive
 *  lombok.Generated
 *  net.minecraft.MathHelper
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.settings.AbstractSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.settings.ValueChangeListener;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.layout.JustifyContent;
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.ScrollingTextComponent;
import moscow.rockstar.ui.text.ValueFormatter;
import moscow.rockstar.ui.widgets.controls.Slider;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class NumberSetting
extends AbstractSetting {
    protected float minValue;
    protected float maxValue;
    protected float step;
    protected float value;
    private Formatter formatter = f -> "";
    private ValueChangeListener<Float> changeListener = f -> f;

    public NumberSetting(@NotNull SettingOwner settingOwner, String string, String string2, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public NumberSetting(@NotNull SettingOwner settingOwner, String string, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public NumberSetting(@NotNull SettingOwner settingOwner, String string, String string2) {
        super(settingOwner, string);
    }

    public NumberSetting(@NotNull SettingOwner settingOwner, String string) {
        super(settingOwner, string);
    }

    public NumberSetting setMinValue(float f) {
        this.minValue = f;
        return this;
    }

    public NumberSetting setMaxValue(float f) {
        this.maxValue = f;
        return this;
    }

    public NumberSetting setStep(float f) {
        this.step = f;
        return this;
    }

    public NumberSetting setFormatter(Formatter formatter) {
        this.formatter = formatter;
        return this;
    }

    public NumberSetting setUnit(String string) {
        this.formatter = f -> string;
        return this;
    }

    public NumberSetting setChangeListener(ValueChangeListener<Float> valueChangeListener) {
        this.changeListener = valueChangeListener;
        return this;
    }

    public NumberSetting setValue(float f) {
        this.updateValue(f);
        return this;
    }

    public String formatValue() {
        return this.formatter.apply(this.getValue()).contains(" ") ? " " + Localization.translate(this.formatter.apply(this.getValue()).replace(" ", "")) : Localization.translate(this.formatter.apply(this.getValue()));
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive((Number)Float.valueOf(this.value));
    }

    @Override
    public void deserialize(JsonElement jsonElement) {
        float f;
        if (jsonElement != null && jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber() && Float.isFinite(f = jsonElement.getAsFloat())) {
            this.updateValue(f);
        }
    }

    @Override
    public boolean isValidJson(JsonElement jsonElement) {
        return jsonElement != null && jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber() && Float.isFinite(jsonElement.getAsFloat());
    }

    public void updateValue(float f) {
        float f2 = MathHelper.clamp((float)((float)((double)Math.round((double)(f = this.changeListener.changed(Float.valueOf(f)).floatValue()) * (1.0 / (double)this.step)) / (1.0 / (double)this.step))), (float)this.minValue, (float)this.maxValue);
        if (this.value == f2) {
            return;
        }
        this.notifyChange();
        this.value = f2;
    }

    @Override
    public Component buildComponent() {
        Component component = new Component().add(new ScrollingTextComponent(Font.REGULAR.metrics(8.0f), () -> Localization.translate(this.key)).setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover())).fadeOut().fill()).add(new ValueFormatter(Font.REGULAR.metrics(7.0f), this::getValue, this::updateValue, this.minValue, this.maxValue).setDisplayValue(() -> moscow.rockstar.util.NumberFormatting.formatDecimal(this.getValue())).setSuffix(this::formatValue).setTextColorProvider(valueFormatter -> ColorPalette.ACCENT_COLOR)).gap(6.0f).layout(Layout.ROW).overflowMode(JustifyContent.SPACE_BETWEEN).alignment(Alignment.CENTER).padding(Insets.of(6.0f, 0.0f, 0.0f, 0.0f)).fillWidth();
        return new Component().layout(Layout.COLUMN).gap(5.0f).add(component).add(new Component().layout(Layout.COLUMN).fillWidth().padding(Insets.of(0.0f, 0.0f, 4.0f, 0.0f)).add(new Slider(this::getValue, this::updateValue, this.getMinValue(), this.getMaxValue()).setStepSize(this.getStep()).fillWidthNode().setHeight(6.0f).setTrackHeight(3.0f).setThumbRadius(3.0f).setThumbInset(1.5f).setTrackColorProvider(slider -> ColorPalette.MUTED_PANEL_COLOR).setFilledTrackColorProvider(slider -> ColorPalette.ACCENT_COLOR.mulAlpha(1.0f - 0.25f * slider.hover())).setAnimationMotion(Motion.resolveMotionMotionFromLongAndEasing(300L, Easing.easeOutOvershoot))));
    }

    @Generated
    public float getMinValue() {
        return this.minValue;
    }

    @Generated
    public float getMaxValue() {
        return this.maxValue;
    }

    @Generated
    public float getStep() {
        return this.step;
    }

    @Generated
    public float getValue() {
        return this.value;
    }

    @Generated
    public ValueChangeListener<Float> getChangeListener() {
        return this.changeListener;
    }

    public static interface Formatter {
        public String apply(float var1);
    }
}
