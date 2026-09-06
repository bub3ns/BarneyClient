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
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.TextComponent;
import org.jetbrains.annotations.NotNull;

public class ActionSetting
extends AbstractSetting {
    private Runnable action = System.out::println;

    public ActionSetting(@NotNull SettingOwner settingOwner, String string, String string2, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public ActionSetting(@NotNull SettingOwner settingOwner, String string, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public ActionSetting(@NotNull SettingOwner settingOwner, String string, String string2) {
        super(settingOwner, string);
    }

    public ActionSetting(@NotNull SettingOwner settingOwner, String string) {
        super(settingOwner, string);
    }

    public ActionSetting withAction(Runnable runnable) {
        this.action = runnable;
        return this;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive("\u0441\u0443\u043a\u0430 \u043a\u0430\u043a \u0441\u0434\u0435\u043b\u0430\u0442\u044c \u0442\u0430\u043a \u0447\u0442\u043e\u0431\u044b \u0434\u043b\u044f \u043d\u0435\u0433\u043e \u043d\u0435 \u0431\u044b\u043b\u043e \u043a\u0444\u0433");
    }

    @Override
    public void deserialize(JsonElement jsonElement) {
    }

    @Override
    public Component buildComponent() {
        FontMetrics fontMetrics = Font.REGULAR.metrics(8.0f);
        return new Component().layout(Layout.COLUMN).overflowMode(JustifyContent.CENTER).height(18.0f).add(new TextComponent().fillWidth().height(16.0f).radius(6.0f).background(textComponent -> ColorPalette.PANEL_COLOR.mulAlpha(0.3f + 0.2f * textComponent.hover() + 0.1f * textComponent.press())).text(fontMetrics, () -> Localization.translate(this.key), textComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * textComponent.hover())).textAlign(Alignment.CENTER).cursor(Cursor.HAND).onClick(() -> this.action.run()));
    }

    @Generated
    public Runnable getAction() {
        return this.action;
    }

    @Generated
    public void setAction(Runnable runnable) {
        this.action = runnable;
    }
}

