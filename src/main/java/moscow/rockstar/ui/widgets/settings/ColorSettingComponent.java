/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.widgets.settings;

import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.ColorSetting;
import moscow.rockstar.settings.SettingComponent;
import moscow.rockstar.ui.color.ColorPickerWidget;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.settings.SettingWidget;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import ua.mintantileak.spk.Compile;

public class ColorSettingComponent
extends SettingComponent<ColorSetting> {
    private ColorPickerWidget colorPicker;

    public ColorSettingComponent(ColorSetting colorSetting, SettingWidget settingWidget) {
        super(colorSetting, settingWidget);
    }

    @Override
    public void tick() {
        this.width = 13.0f;
        this.height = 8.0f;
        super.tick();
    }

    @Override
    public void renderOverlay(RockstarDrawContext drawContext) {
        super.renderOverlay(drawContext);
    }

    @Override
    protected void renderContent(RockstarDrawContext drawContext) {
        this.contentAnimation.setReverse(this.contains(drawContext.mouseX(), drawContext.mouseY()));
        if (this.contains(drawContext) && (float)drawContext.mouseY() > this.getVisibleRegionTop() && (float)drawContext.mouseY() < this.getVisibleRegionTop() + this.getVisibleRegionHeight()) {
            moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
        }
        float f = 13.0f;
        FontMetrics fontMetrics = Font.REGULAR.metrics(8.0f);
        float f2 = 10.0f;
        float f3 = 19.0f;
        this.drawScrollableText(drawContext, fontMetrics, Localization.translate(((ColorSetting)this.setting).getName()), this.x + f2, this.y + UiUtils.center(fontMetrics.getFontTopOffset(), f3) - 0.5f, this.width - f - 20.0f, ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.contentAnimation.getValue())), 0.7f, 0.99f);
        drawContext.drawRoundedRect(this.x + this.width - f2 - 9.0f, this.y + 4.0f, 10.0f, 10.0f, WidgetState.uniform(4.5f), ColorPalette.BORDER_COLOR);
        drawContext.drawRoundedRect(this.x + this.width - f2 - 7.0f, this.y + 6.0f, 6.0f, 6.0f, WidgetState.uniform(4.5f), ((ColorSetting)this.setting).getColor());
        if (this.colorPicker != null && this.colorPicker.isOpen()) {
            ((ColorSetting)this.setting).setColor(this.colorPicker.getColor());
        } else if (this.colorPicker != null && !this.colorPicker.isOpen()) {
            this.colorPicker = null;
        }
    }

    @Override
    public void renderDivider(RockstarDrawContext drawContext) {
        float f = 0.5f;
        drawContext.drawRect(this.x, this.y + this.height, this.width, f, ColorPalette.getPrimaryTextColor().withAlpha(5.1f));
    }

    @Override
    @Compile
    public void mouseClicked(double d, double d2, PointerAction pointerAction) {
        if (this.contains(d, d2) && pointerAction == PointerAction.LEFT_CLICK) {
            ColorSetting colorSetting = (ColorSetting)this.setting;
            this.colorPicker = new ColorPickerWidget((float)d, (float)d2, 6.0f, colorSetting.isAlphaEnabled(), colorSetting.getColor(), Localization.translate(colorSetting.getName()));
        }
        super.mouseClicked(d, d2, pointerAction);
    }

    @Override
    public float getHeight() {
        this.height = 18.0f;
        return 18.0f;
    }
}
