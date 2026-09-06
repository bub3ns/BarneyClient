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
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.settings.AbstractSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.layout.Layout;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.ScrollingTextComponent;
import moscow.rockstar.ui.text.EditableTextComponent;
import org.jetbrains.annotations.NotNull;

public class StringSetting
extends AbstractSetting {
    private static final Pattern GRAPHEME_PATTERN = Pattern.compile("\\X");
    private String value;
    private boolean numericOnly;
    private int maxLength;

    public StringSetting(@NotNull SettingOwner settingOwner, String string, String string2, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public StringSetting(@NotNull SettingOwner settingOwner, String string, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public StringSetting(@NotNull SettingOwner settingOwner, String string, String string2) {
        super(settingOwner, string);
    }

    public StringSetting(@NotNull SettingOwner settingOwner, String string) {
        super(settingOwner, string);
    }

    public final StringSetting setValue(String string) {
        String string2 = this.limitToMaxLength(string);
        if (Objects.equals(this.value, string2)) {
            return this;
        }
        this.notifyChange();
        this.value = string2;
        return this;
    }

    public void updateValue(String string) {
        this.setValue(string);
    }

    public final StringSetting setMaxLength(int n) {
        this.maxLength = n;
        this.value = this.limitToMaxLength(this.value);
        return this;
    }

    private String limitToMaxLength(String string) {
        if (this.maxLength <= 0 || string == null) {
            return string;
        }
        Matcher matcher = GRAPHEME_PATTERN.matcher(string);
        int n = 0;
        while (matcher.find()) {
            if (++n <= this.maxLength) continue;
            return string.substring(0, matcher.start());
        }
        return string;
    }

    public final StringSetting setNumericOnly(boolean bl) {
        this.numericOnly = bl;
        return this;
    }

    @Override
    public final JsonElement serialize() {
        return new JsonPrimitive(this.value);
    }

    @Override
    public final void deserialize(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isString()) {
            return;
        }
        this.setValue(jsonElement.getAsString());
    }

    @Override
    public Component buildComponent() {
        Component component = new Component().add(new ScrollingTextComponent(Font.REGULAR.metrics(8.0f), () -> Localization.translate(this.key)).setColorProvider(scrollingTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.75f + 0.25f * scrollingTextComponent.hover())).fadeOut().fill()).layout(Layout.ROW).alignment(Alignment.CENTER).padding(Insets.of(6.0f, 0.0f, 0.0f, 0.0f)).fillWidth();
        EditableTextComponent editor = new EditableTextComponent(Font.REGULAR.metrics(7.0f), this.value, this::setValue);
        editor.numericOnly(this.numericOnly);
        editor.maxLength(this.maxLength);
        editor.placeholder(() -> Localization.translate("type_text"));
        editor.background(ColorPalette.MUTED_PANEL_COLOR);
        editor.radius(4.0f);
        editor.padding(4.0f);
        editor.height(15.0f);
        return new Component().layout(Layout.COLUMN).gap(4.0f).add(component).add(editor);
    }

    @Generated
    public String getValue() {
        return this.value;
    }

    @Generated
    public boolean isNumericOnly() {
        return this.numericOnly;
    }

    @Generated
    public int getMaxLength() {
        return this.maxLength;
    }
}
