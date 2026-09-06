/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  lombok.Generated
 *  net.minecraft.Vec2f
 *  net.minecraft.MathHelper
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.settings.AbstractSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.settings.ValueChangeListener;
import moscow.rockstar.ui.animation.AnimatedPanel;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.ScrollingTextComponent;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class Vector2Setting
extends AbstractSetting {
    private float x;
    private float y;
    private float minX = -1.0f;
    private float maxX = 1.0f;
    private float minY = -1.0f;
    private float maxY = 1.0f;
    private float boundX;
    private float boundY;
    private ValueChangeListener<Vec2f> valueChangeListener = VanillaAdventureTabAdvancementGenerator -> VanillaAdventureTabAdvancementGenerator;

    public Vector2Setting(@NotNull SettingOwner settingOwner, String string, String string2, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public Vector2Setting(@NotNull SettingOwner settingOwner, String string, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public Vector2Setting(@NotNull SettingOwner settingOwner, String string) {
        super(settingOwner, string);
    }

    public Vector2Setting setValueChangeListener(ValueChangeListener<Vec2f> valueChangeListener) {
        this.valueChangeListener = valueChangeListener == null ? VanillaAdventureTabAdvancementGenerator -> VanillaAdventureTabAdvancementGenerator : valueChangeListener;
        this.updateValue(this.x, this.y);
        return this;
    }

    public Vector2Setting setX(float f) {
        this.updateValue(f, this.y);
        return this;
    }

    public Vector2Setting setY(float f) {
        this.updateValue(this.x, f);
        return this;
    }

    public Vector2Setting setValue(float f, float f2) {
        this.updateValue(f, f2);
        return this;
    }

    public Vector2Setting setMinX(float f) {
        this.minX = f;
        this.refreshValue();
        return this;
    }

    public Vector2Setting setMaxX(float f) {
        this.maxX = f;
        this.refreshValue();
        return this;
    }

    public Vector2Setting setMinY(float f) {
        this.minY = f;
        this.refreshValue();
        return this;
    }

    public Vector2Setting setMaxY(float f) {
        this.maxY = f;
        this.refreshValue();
        return this;
    }

    public Vector2Setting setBoundX(float f) {
        this.boundX = MathHelper.clamp((float)f, (float)this.minX, (float)this.maxX);
        return this;
    }

    public Vector2Setting setBoundY(float f) {
        this.boundY = MathHelper.clamp((float)f, (float)this.minY, (float)this.maxY);
        return this;
    }

    public Vec2f getValue() {
        return new Vec2f(this.x, this.y);
    }

    public Vec2f getBoundValue() {
        return new Vec2f(this.boundX, this.boundY);
    }

    public void updateValue(float f, float f2) {
        Vec2f VanillaAdventureTabAdvancementGenerator = this.valueChangeListener.changed(new Vec2f(f, f2));
        float f3 = MathHelper.clamp((float)VanillaAdventureTabAdvancementGenerator.x, (float)this.minX, (float)this.maxX);
        float f4 = MathHelper.clamp((float)VanillaAdventureTabAdvancementGenerator.y, (float)this.minY, (float)this.maxY);
        if (this.x == f3 && this.y == f4) {
            return;
        }
        this.notifyChange();
        this.x = f3;
        this.y = f4;
    }

    public void setValue(Vec2f VanillaAdventureTabAdvancementGenerator) {
        this.updateValue(VanillaAdventureTabAdvancementGenerator.x, VanillaAdventureTabAdvancementGenerator.y);
    }

    private void refreshValue() {
        this.updateValue(this.x, this.y);
        this.setBoundX(this.boundX);
        this.setBoundY(this.boundY);
    }

    @Override
    public JsonElement serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", (Number)Float.valueOf(this.x));
        jsonObject.addProperty("y", (Number)Float.valueOf(this.y));
        jsonObject.addProperty("bind_x", (Number)Float.valueOf(this.boundX));
        jsonObject.addProperty("bind_y", (Number)Float.valueOf(this.boundY));
        return jsonObject;
    }

    @Override
    public void deserialize(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (jsonObject.has("x") && !this.isFiniteNumber(jsonObject.get("x")) || jsonObject.has("y") && !this.isFiniteNumber(jsonObject.get("y")) || jsonObject.has("bind_x") && !this.isFiniteNumber(jsonObject.get("bind_x")) || jsonObject.has("bind_y") && !this.isFiniteNumber(jsonObject.get("bind_y"))) {
            return;
        }
        float f = this.x;
        float f2 = this.y;
        float f3 = this.boundX;
        float f4 = this.boundY;
        if (jsonObject.has("x")) {
            f = jsonObject.get("x").getAsFloat();
        }
        if (jsonObject.has("y")) {
            f2 = jsonObject.get("y").getAsFloat();
        }
        if (jsonObject.has("bind_x")) {
            f3 = jsonObject.get("bind_x").getAsFloat();
        }
        if (jsonObject.has("bind_y")) {
            f4 = jsonObject.get("bind_y").getAsFloat();
        }
        if (!(Float.isFinite(f) && Float.isFinite(f2) && Float.isFinite(f3) && Float.isFinite(f4))) {
            return;
        }
        this.updateValue(f, f2);
        this.setBoundX(f3);
        this.setBoundY(f4);
    }

    private boolean isFiniteNumber(JsonElement jsonElement) {
        return jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber() && Float.isFinite(jsonElement.getAsFloat());
    }

    @Override
    public boolean isValidJson(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return false;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return jsonObject.has("x") && this.isFiniteNumber(jsonObject.get("x")) && jsonObject.has("y") && this.isFiniteNumber(jsonObject.get("y")) && jsonObject.has("bind_x") && this.isFiniteNumber(jsonObject.get("bind_x")) && jsonObject.has("bind_y") && this.isFiniteNumber(jsonObject.get("bind_y"));
    }

    @Override
    public Component buildComponent() {
        FontMetrics fontMetrics = Font.REGULAR.metrics(7.0f);
        Component component = new Component().add(new ScrollingTextComponent(Font.REGULAR.metrics(8.0f), () -> Localization.translate(this.key)).setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover())).fadeOut().fill()).layout(Layout.ROW).alignment(Alignment.CENTER).padding(Insets.of(6.0f, 0.0f, 0.0f, 0.0f)).fillWidth();
        return new Component().layout(Layout.COLUMN).gap(6.0f).add(component).add(new AnimatedPanel(this::getX, this::getY, this::updateValue, this.minX, this.maxX, this.minY, this.maxY).setGridSpacing(2.0f).setLabel(fontMetrics, () -> String.format(Locale.ROOT, "%.2f : %.2f", Float.valueOf(this.getX()), Float.valueOf(this.getY()))).fillWidthNode());
    }

    @Generated
    public float getX() {
        return this.x;
    }

    @Generated
    public float getY() {
        return this.y;
    }

    @Generated
    public float getMinX() {
        return this.minX;
    }

    @Generated
    public float getMaxX() {
        return this.maxX;
    }

    @Generated
    public float getMinY() {
        return this.minY;
    }

    @Generated
    public float getMaxY() {
        return this.maxY;
    }

    @Generated
    public float getBoundX() {
        return this.boundX;
    }

    @Generated
    public float getBoundY() {
        return this.boundY;
    }

    @Generated
    public ValueChangeListener<Vec2f> getValueChangeListener() {
        return this.valueChangeListener;
    }
}

