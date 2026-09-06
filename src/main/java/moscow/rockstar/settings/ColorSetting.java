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

public class ColorSetting
extends AbstractSetting {
    private ColorRGBA color;
    private boolean alphaEnabled = true;
    private transient ColorPickerWindow picker;

    public ColorSetting(@NotNull SettingOwner settingOwner, String string, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public ColorSetting(@NotNull SettingOwner settingOwner, String string) {
        super(settingOwner, string);
    }

    public ColorSetting setColor(ColorRGBA colorRGBA) {
        this.updateColor(colorRGBA);
        return this;
    }

    public void updateColor(ColorRGBA colorRGBA) {
        if (Objects.equals(this.color, colorRGBA)) {
            return;
        }
        this.notifyChange();
        this.color = colorRGBA;
    }

    public ColorSetting setAlphaEnabled(boolean bl) {
        this.alphaEnabled = bl;
        return this;
    }

    @Override
    public JsonElement serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("r", (Number)Float.valueOf(this.color.getRed()));
        jsonObject.addProperty("g", (Number)Float.valueOf(this.color.getGreen()));
        jsonObject.addProperty("b", (Number)Float.valueOf(this.color.getBlue()));
        jsonObject.addProperty("a", (Number)Float.valueOf(this.color.getAlpha()));
        return jsonObject;
    }

    @Override
    public void deserialize(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (!(this.isValidChannel(jsonObject, "r") && this.isValidChannel(jsonObject, "g") && this.isValidChannel(jsonObject, "b") && this.isValidChannel(jsonObject, "a"))) {
            return;
        }
        double d = jsonObject.get("r").getAsDouble();
        double d2 = jsonObject.get("g").getAsDouble();
        double d3 = jsonObject.get("b").getAsDouble();
        double d4 = jsonObject.get("a").getAsDouble();
        if (!(Double.isFinite(d) && Double.isFinite(d2) && Double.isFinite(d3) && Double.isFinite(d4))) {
            return;
        }
        this.updateColor(new ColorRGBA(this.clampChannel((int)d), this.clampChannel((int)d2), this.clampChannel((int)d3), this.clampChannel((int)d4)));
    }

    private boolean isValidChannel(JsonObject jsonObject, String string) {
        return jsonObject.has(string) && jsonObject.get(string).isJsonPrimitive() && jsonObject.get(string).getAsJsonPrimitive().isNumber() && Double.isFinite(jsonObject.get(string).getAsDouble());
    }

    @Override
    public boolean isValidJson(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return false;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return this.isValidChannel(jsonObject, "r") && this.isValidChannel(jsonObject, "g") && this.isValidChannel(jsonObject, "b") && this.isValidChannel(jsonObject, "a");
    }

    private int clampChannel(int n) {
        return MathHelper.clamp((int)n, (int)0, (int)255);
    }

    @Override
    public Component buildComponent() {
        return new Component().height(17.0f).add(new ScrollingTextComponent(Font.REGULAR.metrics(8.0f), () -> Localization.translate(this.key)).setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover())).fadeOut().fill()).add(new ColorPicker(this::getColor).setSize(10.0f, 10.0f).interactive(false)).gap(5.0f).layout(Layout.ROW).overflowMode(JustifyContent.SPACE_BETWEEN).alignment(Alignment.CENTER).onClick((pointerAction, f, f2) -> {
            if (pointerAction == PointerAction.LEFT_CLICK) {
                this.openPicker();
            }
        }).cursor(Cursor.HAND);
    }

    public void closePicker() {
        if (this.picker != null && this.picker.alive()) {
            this.picker.close();
        }
    }

    private void openPicker() {
        ColorPickerWindow colorPickerWindow;
        MinecraftClient client = MinecraftClient.getInstance();
        if (this.picker != null && this.picker.alive()) {
            return;
        }
        double d = client.getWindow().getScaleFactor();
        float f = (float)(client.mouse.getX() / d);
        float f2 = (float)(client.mouse.getY() / d);
        ColorRGBA colorRGBA = this.color != null ? this.color : ColorRGBA.WHITE;
        ColorPickerWindow colorPickerWindow2 = new ColorPickerWindow(f, f2, this.alphaEnabled, colorRGBA, Localization.translate(this.key), this::setColor);
        Screen class_4372 = client.currentScreen;
        if (class_4372 instanceof ColorPickerHost) {
            ColorPickerHost colorPickerHost = (ColorPickerHost)class_4372;
            colorPickerWindow = colorPickerHost.openWindow(colorPickerWindow2);
        } else {
            colorPickerWindow = ColorPickerScreen.registerTransientNode(colorPickerWindow2);
        }
        this.picker = colorPickerWindow;
    }

    @Generated
    public ColorRGBA getColor() {
        return this.color;
    }

    @Generated
    public boolean isAlphaEnabled() {
        return this.alphaEnabled;
    }

    @Generated
    public ColorPickerWindow getPicker() {
        return this.picker;
    }
}

