/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  lombok.Generated
 *  net.minecraft.MathHelper
 *  net.minecraft.Screen
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.settings.AbstractSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.color.ColorPicker;
import moscow.rockstar.ui.color.ColorPickerHost;
import moscow.rockstar.ui.color.ColorPickerScreen;
import moscow.rockstar.ui.color.ColorPickerWindow;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.JustifyContent;
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.ScrollingTextComponent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.NotNull;
import pyrock.utility.render.ColorRGBA;

public class ColorRangeSetting
extends AbstractSetting {
    private ColorRGBA firstColor;
    private ColorRGBA secondColor;
    private boolean firstColorSelected = true;
    private transient ColorPickerWindow firstColorPicker;
    private transient ColorPickerWindow secondColorPicker;

    public ColorRangeSetting(@NotNull SettingOwner settingOwner, String string, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public ColorRangeSetting(@NotNull SettingOwner settingOwner, String string) {
        super(settingOwner, string);
    }

    public ColorRangeSetting setColorRange(ColorRGBA colorRGBA, ColorRGBA colorRGBA2) {
        if (Objects.equals(this.firstColor, colorRGBA) && Objects.equals(this.secondColor, colorRGBA2)) {
            return this;
        }
        this.notifyChange();
        this.firstColor = colorRGBA;
        this.secondColor = colorRGBA2;
        return this;
    }

    public ColorRangeSetting setFirstColorAndReturn(ColorRGBA colorRGBA) {
        this.setFirstColor(colorRGBA);
        return this;
    }

    public void setFirstColor(ColorRGBA colorRGBA) {
        if (Objects.equals(this.firstColor, colorRGBA)) {
            return;
        }
        this.notifyChange();
        this.firstColor = colorRGBA;
    }

    public ColorRangeSetting setSecondColorAndReturn(ColorRGBA colorRGBA) {
        this.setSecondColor(colorRGBA);
        return this;
    }

    public void setSecondColor(ColorRGBA colorRGBA) {
        if (Objects.equals(this.secondColor, colorRGBA)) {
            return;
        }
        this.notifyChange();
        this.secondColor = colorRGBA;
    }

    public ColorRangeSetting setFirstColorSelected(boolean bl) {
        this.firstColorSelected = bl;
        return this;
    }

    @Override
    public JsonElement serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("first", (JsonElement)this.serializeColor(this.firstColor));
        jsonObject.add("second", (JsonElement)this.serializeColor(this.secondColor));
        return jsonObject;
    }

    @Override
    public void deserialize(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (!(jsonObject.has("first") && jsonObject.get("first").isJsonObject() && jsonObject.has("second") && jsonObject.get("second").isJsonObject())) {
            return;
        }
        ColorRGBA colorRGBA = this.deserializeColor(jsonObject.getAsJsonObject("first"));
        ColorRGBA colorRGBA2 = this.deserializeColor(jsonObject.getAsJsonObject("second"));
        if (colorRGBA != null && colorRGBA2 != null) {
            this.setColorRange(colorRGBA, colorRGBA2);
        }
    }

    private JsonObject serializeColor(ColorRGBA colorRGBA) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("r", (Number)Float.valueOf(colorRGBA.getRed()));
        jsonObject.addProperty("g", (Number)Float.valueOf(colorRGBA.getGreen()));
        jsonObject.addProperty("b", (Number)Float.valueOf(colorRGBA.getBlue()));
        jsonObject.addProperty("a", (Number)Float.valueOf(colorRGBA.getAlpha()));
        return jsonObject;
    }

    private ColorRGBA deserializeColor(JsonObject jsonObject) {
        if (!(this.isJsonObjectAndStringValid(jsonObject, "r") && this.isJsonObjectAndStringValid(jsonObject, "g") && this.isJsonObjectAndStringValid(jsonObject, "b") && this.isJsonObjectAndStringValid(jsonObject, "a"))) {
            return null;
        }
        double d = jsonObject.get("r").getAsDouble();
        double d2 = jsonObject.get("g").getAsDouble();
        double d3 = jsonObject.get("b").getAsDouble();
        double d4 = jsonObject.get("a").getAsDouble();
        if (!(Double.isFinite(d) && Double.isFinite(d2) && Double.isFinite(d3) && Double.isFinite(d4))) {
            return null;
        }
        return new ColorRGBA(this.clampColorChannel((int)d), this.clampColorChannel((int)d2), this.clampColorChannel((int)d3), this.clampColorChannel((int)d4));
    }

    private boolean isJsonObjectAndStringValid(JsonObject jsonObject, String string) {
        return jsonObject.has(string) && jsonObject.get(string).isJsonPrimitive() && jsonObject.get(string).getAsJsonPrimitive().isNumber() && Double.isFinite(jsonObject.get(string).getAsDouble());
    }

    @Override
    public boolean isValidJson(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return false;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return jsonObject.has("first") && jsonObject.get("first").isJsonObject() && jsonObject.has("second") && jsonObject.get("second").isJsonObject() && this.deserializeColor(jsonObject.getAsJsonObject("first")) != null && this.deserializeColor(jsonObject.getAsJsonObject("second")) != null;
    }

    private int clampColorChannel(int n) {
        return MathHelper.clamp((int)n, (int)0, (int)255);
    }

    @Override
    public Component buildComponent() {
        Component component = new Component().layout(Layout.ROW).alignment(Alignment.CENTER).gap(3.0f).add(new ColorPicker(this::getColorRangeSettingColorRGBA).setSize(10.0f, 10.0f).cursor(Cursor.HAND).onClick((pointerAction, f, f2) -> {
            if (pointerAction == PointerAction.LEFT_CLICK) {
                this.openColorPicker(true);
            }
        })).add(new ColorPicker(this::getSecondColor).setSize(10.0f, 10.0f).cursor(Cursor.HAND).onClick((pointerAction, f, f2) -> {
            if (pointerAction == PointerAction.LEFT_CLICK) {
                this.openColorPicker(false);
            }
        }));
        return new Component().height(17.0f).add(new ScrollingTextComponent(Font.REGULAR.metrics(8.0f), () -> Localization.translate(this.key)).setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover())).fadeOut().fill()).add(component).gap(5.0f).layout(Layout.ROW).overflowMode(JustifyContent.SPACE_BETWEEN).alignment(Alignment.CENTER);
    }

    private void openColorPicker(boolean bl) {
        MinecraftClient client = MinecraftClient.getInstance();
        double d = client.getWindow().getScaleFactor();
        float f = (float)(client.mouse.getX() / d);
        float f2 = (float)(client.mouse.getY() / d);
        if (bl) {
            if (this.firstColorPicker != null && this.firstColorPicker.alive()) {
                return;
            }
            ColorRGBA colorRGBA = this.firstColor != null ? this.firstColor : ColorRGBA.WHITE;
            this.firstColorPicker = ColorRangeSetting.bringColorPickerToFront(new ColorPickerWindow(f, f2, this.firstColorSelected, colorRGBA, Localization.translate(this.key), this::setFirstColorAndReturn));
        } else {
            if (this.secondColorPicker != null && this.secondColorPicker.alive()) {
                return;
            }
            ColorRGBA colorRGBA = this.secondColor != null ? this.secondColor : ColorRGBA.WHITE;
            this.secondColorPicker = ColorRangeSetting.bringColorPickerToFront(new ColorPickerWindow(f, f2, this.firstColorSelected, colorRGBA, Localization.translate(this.key), this::setSecondColorAndReturn));
        }
    }

    private static ColorPickerWindow bringColorPickerToFront(ColorPickerWindow colorPickerWindow) {
        ColorPickerWindow colorPickerWindow2;
        Screen class_4372 = MinecraftClient.getInstance().currentScreen;
        if (class_4372 instanceof ColorPickerHost) {
            ColorPickerHost colorPickerHost = (ColorPickerHost)class_4372;
            colorPickerWindow2 = colorPickerHost.openWindow(colorPickerWindow);
        } else {
            colorPickerWindow2 = ColorPickerScreen.registerTransientNode(colorPickerWindow);
        }
        return colorPickerWindow2;
    }

    @Generated
    public ColorRGBA getColorRangeSettingColorRGBA() {
        return this.firstColor;
    }

    @Generated
    public ColorRGBA getSecondColor() {
        return this.secondColor;
    }

    @Generated
    public boolean isFirstColorSelected() {
        return this.firstColorSelected;
    }

    @Generated
    public ColorPickerWindow getColorRangeSettingColorPickerWindow() {
        return this.firstColorPicker;
    }

    @Generated
    public ColorPickerWindow getSecondColorPicker() {
        return this.secondColorPicker;
    }
}

