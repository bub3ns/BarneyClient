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
import java.util.function.Supplier;
import lombok.Generated;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.settings.AbstractSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.MultilineTextComponent;
import moscow.rockstar.ui.text.TextComponent;
import org.jetbrains.annotations.NotNull;
import pyrock.utility.render.ColorRGBA;

/*
 * Duplicate member names - consider using --renamedupmembers true
 */
public class TextLabelSetting
extends AbstractSetting {
    private int fontSizeOffset = 1;
    private boolean multiline;
    private boolean centered;

    public TextLabelSetting(@NotNull SettingOwner settingOwner, String string, String string2, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public TextLabelSetting(@NotNull SettingOwner settingOwner, String string, @NotNull BooleanSupplier booleanSupplier) {
        super(settingOwner, string, booleanSupplier);
    }

    public TextLabelSetting(@NotNull SettingOwner settingOwner, String string, String string2) {
        super(settingOwner, string);
    }

    public TextLabelSetting(@NotNull SettingOwner settingOwner, String string) {
        super(settingOwner, string);
    }

    public TextLabelSetting setFontSizeOffset(int n) {
        this.fontSizeOffset = n;
        return this;
    }

    public TextLabelSetting asMultiline() {
        this.multiline = true;
        return this;
    }

    public TextLabelSetting asCentered() {
        this.centered = true;
        return this;
    }

    @Override
    public final JsonElement serialize() {
        return new JsonPrimitive("\u043a\u043e\u0441\u0442\u044b\u043b\u044c");
    }

    @Override
    public final void deserialize(JsonElement jsonElement) {
    }

    @Override
    public Component buildComponent() {
        FontMetrics fontMetrics = (this.multiline ? Font.REGULAR : Font.SEMIBOLD).metrics(8.0f + this.fontSizeOffset);
        Supplier<String> supplier = () -> Localization.translate(this.key);
        Component content = new Component().fillWidth();
        if (this.multiline) {
            content.add(new MultilineTextComponent(fontMetrics, supplier)
                .setCentered(this.centered)
                .setColorProvider(multilineTextComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.9f))
                .fillWidthNode());
        } else {
            content.add(new TextComponent().fillWidth().height(fontMetrics.getFontTopOffset())
                .text(fontMetrics, supplier, textComponent -> ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.9f))
                .textAlign(this.centered ? Alignment.CENTER : Alignment.START)
                .fade(!this.centered)
                .textShadow(this.centered ? ColorRGBA.BLACK.withAlpha(100.0f) : null, 0.0f, 1.0f, 0.0f)
                .interactive(false));
        }
        return new Component().vertical().fillWidth()
            .add(new Component().vertical().fillWidth()
                .padding(Insets.of(10.0f, 0.0f, 5.0f, 0.0f)).add(content));
    }

    @Generated
    public int getFontSizeOffset() {
        return this.fontSizeOffset;
    }

    @Generated
    public boolean isMultiline() {
        return this.multiline;
    }

    @Generated
    public boolean isCentered() {
        return this.centered;
    }

    @Generated
    public void applyFontSizeOffset(int n) {
        this.fontSizeOffset = n;
    }

    @Generated
    public void setMultiline(boolean bl) {
        this.multiline = bl;
    }

    @Generated
    public void setCentered(boolean bl) {
        this.centered = bl;
    }
}
