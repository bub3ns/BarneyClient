/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  lombok.Generated
 *  net.minecraft.MathHelper
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.settings.AbstractSetting;
import moscow.rockstar.settings.SettingOwner;
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
import moscow.rockstar.ui.text.TextComponent;
import moscow.rockstar.ui.text.ValueFormatter;
import moscow.rockstar.ui.widgets.controls.RangeSlider;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class RangeSetting
extends AbstractSetting {
    private float firstValue;
    private float secondValue;
    private float minimum;
    private float maximum;
    private float step;
    private float defaultMinimum = Float.NaN;

    public RangeSetting(@NotNull SettingOwner settingOwner, String string, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public RangeSetting(@NotNull SettingOwner settingOwner, String string) {
        super(settingOwner, string);
    }

    public RangeSetting setFirstValue(float f) {
        this.updateFirstValue(f);
        return this;
    }

    public RangeSetting setSecondValue(float f) {
        this.updateSecondValue(f);
        return this;
    }

    public RangeSetting setMinimum(float f) {
        this.minimum = f;
        return this;
    }

    public RangeSetting setMaximum(float f) {
        this.maximum = f;
        return this;
    }

    public RangeSetting setStep(float f) {
        this.step = f;
        return this;
    }

    public RangeSetting setDefaultMinimum(float f) {
        this.defaultMinimum = f;
        if (this.secondValue < this.getEffectiveMinimum()) {
            this.secondValue = this.getEffectiveMinimum();
        }
        return this;
    }

    public float getEffectiveMinimum() {
        return Float.isNaN(this.defaultMinimum) ? this.minimum : this.defaultMinimum;
    }

    @Override
    public JsonElement serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("first", (Number)Float.valueOf(this.firstValue));
        jsonObject.addProperty("second", (Number)Float.valueOf(this.secondValue));
        return jsonObject;
    }

    @Override
    public void deserialize(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (!(!jsonObject.has("first") || jsonObject.get("first").isJsonPrimitive() && jsonObject.get("first").getAsJsonPrimitive().isNumber())) {
            return;
        }
        if (!(!jsonObject.has("second") || jsonObject.get("second").isJsonPrimitive() && jsonObject.get("second").getAsJsonPrimitive().isNumber())) {
            return;
        }
        float f = this.firstValue;
        float f2 = this.secondValue;
        if (jsonObject.has("first")) {
            f = jsonObject.get("first").getAsFloat();
        }
        if (jsonObject.has("second")) {
            f2 = jsonObject.get("second").getAsFloat();
        }
        if (!Float.isFinite(f) || !Float.isFinite(f2)) {
            return;
        }
        this.updateFirstValue(f);
        this.updateSecondValue(f2);
    }

    @Override
    public boolean isValidJson(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return false;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return jsonObject.has("first") && jsonObject.get("first").isJsonPrimitive() && jsonObject.get("first").getAsJsonPrimitive().isNumber() && Float.isFinite(jsonObject.get("first").getAsFloat()) && jsonObject.has("second") && jsonObject.get("second").isJsonPrimitive() && jsonObject.get("second").getAsJsonPrimitive().isNumber() && Float.isFinite(jsonObject.get("second").getAsFloat());
    }

    public void updateFirstValue(float f) {
        float f2 = (float)MathHelper.clamp((double)((double)Math.round((double)f * (1.0 / (double)this.step)) / (1.0 / (double)this.step)), (double)this.minimum, (double)this.maximum);
        if (this.firstValue == f2) {
            return;
        }
        this.notifyChange();
        this.firstValue = f2;
    }

    public void updateSecondValue(float f) {
        float f2 = (float)MathHelper.clamp((double)((double)Math.round((double)f * (1.0 / (double)this.step)) / (1.0 / (double)this.step)), (double)this.getEffectiveMinimum(), (double)this.maximum);
        if (this.secondValue == f2) {
            return;
        }
        this.notifyChange();
        this.secondValue = f2;
    }

    @Override
    public Component buildComponent() {
        Component component = new Component().add(new ScrollingTextComponent(Font.REGULAR.metrics(8.0f), () -> Localization.translate(this.key)).setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover())).fadeOut().fill()).add(new Component().layout(Layout.ROW).alignment(Alignment.CENTER).gap(3.0f).add(new ValueFormatter(Font.REGULAR.metrics(7.0f), this::getFirstValue, this::updateFirstValue, this.minimum, this.maximum).setDisplayValue(() -> moscow.rockstar.util.NumberFormatting.formatDecimal(this.getFirstValue())).setTextColorProvider(valueFormatter -> ColorPalette.ACCENT_COLOR)).add(new TextComponent().text(Font.REGULAR.metrics(7.0f), "-", ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.5f)).interactive(false)).add(new ValueFormatter(Font.REGULAR.metrics(7.0f), this::getSecondValue, this::updateSecondValue, this.getEffectiveMinimum(), this.maximum).setDisplayValue(() -> moscow.rockstar.util.NumberFormatting.formatDecimal(this.getSecondValue())).setTextColorProvider(valueFormatter -> ColorPalette.ACCENT_COLOR))).gap(6.0f).layout(Layout.ROW).overflowMode(JustifyContent.SPACE_BETWEEN).alignment(Alignment.CENTER).padding(Insets.of(6.0f, 0.0f, 0.0f, 0.0f)).fillWidth();
        return new Component().layout(Layout.COLUMN).gap(5.0f).add(component).add(new Component().layout(Layout.COLUMN).fillWidth().padding(Insets.of(0.0f, 0.0f, 4.0f, 0.0f)).add(new RangeSlider(this::getFirstValue, this::updateFirstValue, this::getSecondValue, this::updateSecondValue, this.minimum, this.maximum).setStepSize(this.step).fillWidthNode().setHeight(6.0f).setTrackHeight(3.0f).setThumbRadius(3.0f).setThumbInset(1.5f).setTrackColorProvider(rangeSlider -> ColorPalette.MUTED_PANEL_COLOR).setRangeColorProvider(rangeSlider -> ColorPalette.ACCENT_COLOR.mulAlpha(1.0f - 0.25f * rangeSlider.hover())).setAnimationMotion(Motion.resolveMotionMotionFromLongAndEasing(300L, Easing.easeOutOvershoot))));
    }

    @Generated
    public float getFirstValue() {
        return this.firstValue;
    }

    @Generated
    public float getSecondValue() {
        return this.secondValue;
    }

    @Generated
    public float getMinimum() {
        return this.minimum;
    }

    @Generated
    public float getMaximum() {
        return this.maximum;
    }

    @Generated
    public float getStep() {
        return this.step;
    }
}
