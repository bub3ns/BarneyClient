/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  lombok.Generated
 *  net.minecraft.Vec2f
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.settings.AbstractSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.ScrollingTextComponent;
import moscow.rockstar.ui.widgets.controls.Vector2Slider;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EasingSetting
extends AbstractSetting {
    private Vec2f startControlPoint = Vec2f.ZERO;
    private Vec2f endControlPoint = new Vec2f(1.0f, 1.0f);

    public EasingSetting(@NotNull SettingOwner settingOwner, String string, String string2, @Nullable BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public EasingSetting(@NotNull SettingOwner settingOwner, String string, @Nullable BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public EasingSetting(@NotNull SettingOwner settingOwner, String string, String string2) {
        super(settingOwner, string);
    }

    public EasingSetting(@NotNull SettingOwner settingOwner, String string) {
        super(settingOwner, string);
    }

    public EasingSetting setStartControlPoint(float f, float f2) {
        return this.setStartControlPoint(new Vec2f(f, f2));
    }

    public EasingSetting setEndControlPoint(float f, float f2) {
        return this.setEndControlPoint(new Vec2f(f, f2));
    }

    public EasingSetting setStartControlPoint(Vec2f VanillaAdventureTabAdvancementGenerator) {
        if (this.startControlPoint.equals(VanillaAdventureTabAdvancementGenerator)) {
            return this;
        }
        this.notifyChange();
        this.startControlPoint = VanillaAdventureTabAdvancementGenerator;
        return this;
    }

    public EasingSetting setEndControlPoint(Vec2f VanillaAdventureTabAdvancementGenerator) {
        if (this.endControlPoint.equals(VanillaAdventureTabAdvancementGenerator)) {
            return this;
        }
        this.notifyChange();
        this.endControlPoint = VanillaAdventureTabAdvancementGenerator;
        return this;
    }

    public Easing getEasing() {
        return Easing.cubicBezier(this.startControlPoint.x, 1.0f - this.startControlPoint.y, this.endControlPoint.x, 1.0f - this.endControlPoint.y);
    }

    @Override
    public JsonElement serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("start_x", (Number)Float.valueOf(this.startControlPoint.x));
        jsonObject.addProperty("start_y", (Number)Float.valueOf(this.startControlPoint.y));
        jsonObject.addProperty("end_x", (Number)Float.valueOf(this.endControlPoint.x));
        jsonObject.addProperty("end_y", (Number)Float.valueOf(this.endControlPoint.y));
        return jsonObject;
    }

    @Override
    public void deserialize(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (jsonObject.has("start_x") && !this.isEasingValueValid(jsonObject.get("start_x")) || jsonObject.has("start_y") && !this.isEasingValueValid(jsonObject.get("start_y")) || jsonObject.has("end_x") && !this.isEasingValueValid(jsonObject.get("end_x")) || jsonObject.has("end_y") && !this.isEasingValueValid(jsonObject.get("end_y"))) {
            return;
        }
        float f = this.startControlPoint.x;
        float f2 = this.startControlPoint.y;
        float f3 = this.endControlPoint.x;
        float f4 = this.endControlPoint.y;
        if (jsonObject.has("start_x") && jsonObject.has("start_y")) {
            f = jsonObject.get("start_x").getAsFloat();
            f2 = jsonObject.get("start_y").getAsFloat();
        }
        if (jsonObject.has("end_x") && jsonObject.has("end_y")) {
            f3 = jsonObject.get("end_x").getAsFloat();
            f4 = jsonObject.get("end_y").getAsFloat();
        }
        if (!(Float.isFinite(f) && Float.isFinite(f2) && Float.isFinite(f3) && Float.isFinite(f4))) {
            return;
        }
        this.setStartControlPoint(new Vec2f(f, f2));
        this.setEndControlPoint(new Vec2f(f3, f4));
    }

    private boolean isEasingValueValid(JsonElement jsonElement) {
        return jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber() && Float.isFinite(jsonElement.getAsFloat());
    }

    @Override
    public boolean isValidJson(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return false;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return jsonObject.has("start_x") && this.isEasingValueValid(jsonObject.get("start_x")) && jsonObject.has("start_y") && this.isEasingValueValid(jsonObject.get("start_y")) && jsonObject.has("end_x") && this.isEasingValueValid(jsonObject.get("end_x")) && jsonObject.has("end_y") && this.isEasingValueValid(jsonObject.get("end_y"));
    }

    @Override
    public Component buildComponent() {
        FontMetrics fontMetrics = Font.REGULAR.metrics(8.0f);
        Component component = new Component().add(new ScrollingTextComponent(fontMetrics, () -> Localization.translate(this.key)).setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover())).fadeOut().fill()).layout(Layout.ROW).alignment(Alignment.CENTER).padding(Insets.of(6.0f, 0.0f, 0.0f, 0.0f)).fillWidth();
        return new Component().layout(Layout.COLUMN).gap(6.0f).add(component).add(new Vector2Slider(this::getStartControlPoint, this::getEndControlPoint, this::setStartControlPoint, this::setEndControlPoint).setGuideColor(ColorPalette.WHITE.mulAlpha(0.25f)).setGuideCornerRadius(6.0f).fillWidth());
    }

    @Generated
    public Vec2f getStartControlPoint() {
        return this.startControlPoint;
    }

    @Generated
    public Vec2f getEndControlPoint() {
        return this.endControlPoint;
    }
}

