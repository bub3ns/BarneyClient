/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonPrimitive
 *  lombok.Generated
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.network.session.BotPacketListener;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.settings.AbstractSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.JustifyContent;
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.ScrollingTextComponent;
import moscow.rockstar.ui.widgets.settings.KeyBindingControl;
import org.jetbrains.annotations.NotNull;

public class IntegerSetting
extends AbstractSetting {
    private int value = -1;

    public IntegerSetting(@NotNull SettingOwner settingOwner, String string, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public IntegerSetting(@NotNull SettingOwner settingOwner, String string) {
        super(settingOwner, string);
    }

    public IntegerSetting withValue(int n) {
        this.setValue(n);
        return this;
    }

    public void setValue(int n) {
        if (this.value == n) {
            return;
        }
        this.notifyChange();
        this.value = n;
    }

    public boolean isIntValid(int n) {
        return this.hasValidSettingValue() && moscow.rockstar.ui.input.KeyBindingUtil.matches(this.value, n);
    }

    public boolean isValueValid() {
        return this.hasValidSettingValue() && moscow.rockstar.ui.input.KeyBindingUtil.isPressed(this.value);
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive((Number)this.value);
    }

    @Override
    public void deserialize(JsonElement jsonElement) {
        if (jsonElement != null && jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber()) {
            this.setValue(-1);
            int n = jsonElement.getAsInt();
            if (n != -1) {
                this.setValue(n);
            }
        }
    }

    @Override
    public boolean isValidJson(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isNumber()) {
            return false;
        }
        double d = jsonElement.getAsDouble();
        return Double.isFinite(d) && d >= -2.147483648E9 && d <= 2.147483647E9;
    }

    @Override
    public Component buildComponent() {
        KeyBindingControl keyBindingControl = new KeyBindingControl(Font.REGULAR.metrics(7.0f), this::getValue, this::setValue);
        return ((Component)new Component().height(17.0f).add(new ScrollingTextComponent(Font.REGULAR.metrics(8.0f), () -> Localization.translate(this.key)).setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover())).fadeOut().fill()).add(keyBindingControl).gap(5.0f).layout(Layout.ROW).overflowMode(JustifyContent.SPACE_BETWEEN).alignment(Alignment.CENTER).onClick(keyBindingControl::handlePointerClick)).cursor(Cursor.HAND);
    }

    @Generated
    public int getValue() {
        return this.value;
    }
}
