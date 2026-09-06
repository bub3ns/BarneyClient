/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonPrimitive
 *  lombok.Generated
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.function.BooleanSupplier;
import lombok.Generated;
import moscow.rockstar.modules.ModuleToggleListener;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.settings.AbstractSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.InteractiveComponent;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.JustifyContent;
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.ScrollingTextComponent;
import org.jetbrains.annotations.NotNull;

public class BooleanSetting
extends AbstractSetting
implements ModuleToggleListener {
    private boolean activeStateExtra;

    public BooleanSetting(@NotNull SettingOwner settingOwner, String string, String string2, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public BooleanSetting(@NotNull SettingOwner settingOwner, String string, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public BooleanSetting(@NotNull SettingOwner settingOwner, String string, String string2) {
        super(settingOwner, string);
    }

    public BooleanSetting(@NotNull SettingOwner settingOwner, String string) {
        super(settingOwner, string);
    }

    public BooleanSetting setActiveExtra(boolean bl) {
        this.setValueInternal(bl);
        return this;
    }

    public BooleanSetting enable() {
        this.setValueInternal(true);
        return this;
    }

    public void setValueInternal(boolean bl) {
        if (this.activeStateExtra == bl) {
            return;
        }
        this.notifyChange();
        this.activeStateExtra = bl;
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(Boolean.valueOf(this.activeStateExtra));
    }

    @Override
    public void deserialize(JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return;
        }
        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isBoolean()) {
            this.setValueInternal(jsonElement.getAsBoolean());
            return;
        }
        if (jsonElement.isJsonObject()) {
            JsonElement jsonElement2;
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonElement jsonElement3 = jsonElement2 = jsonObject.has("enabled") ? jsonObject.get("enabled") : jsonObject.get("value");
            if (jsonElement2 != null && jsonElement2.isJsonPrimitive() && jsonElement2.getAsJsonPrimitive().isBoolean()) {
                this.setValueInternal(jsonElement2.getAsBoolean());
            }
        }
    }

    @Override
    public boolean isValidJson(JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return false;
        }
        if (jsonElement.isJsonPrimitive()) {
            return jsonElement.getAsJsonPrimitive().isBoolean();
        }
        if (!jsonElement.isJsonObject()) {
            return false;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonElement jsonElement2 = jsonObject.has("enabled") ? jsonObject.get("enabled") : jsonObject.get("value");
        return jsonElement2 != null && jsonElement2.isJsonPrimitive() && jsonElement2.getAsJsonPrimitive().isBoolean();
    }

    @Override
    public Component buildComponent() {
        return new Component().height(18.0f).add(new ScrollingTextComponent(Font.REGULAR.metrics(8.0f), () -> Localization.translate(this.key)).setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover())).fadeOut().fill()).add(new InteractiveComponent(this::isEnabled).setActiveColorProvider(() -> ColorPalette.MUTED_PANEL_COLOR).size(13.0f, 8.0f).minSize(13.0f, 8.0f).snapSize()).gap(5.0f).layout(Layout.ROW).overflowMode(JustifyContent.SPACE_BETWEEN).alignment(Alignment.CENTER).onClick(this::toggle).cursor(Cursor.HAND);
    }

    @Override
    public void toggle() {
        this.setValueInternal(!this.activeStateExtra);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Generated
    public boolean isEnabled() {
        return this.activeStateExtra;
    }
}

