/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.widgets.settings;

import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.ColorRangeSetting;
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
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

public class ColorRangeSettingComponent
extends SettingComponent<ColorRangeSetting> {
    private static final int NO_ACTIVE_HANDLE = -1;
    private static final int LOWER_COLOR_HANDLE = 0;
    private static final int UPPER_COLOR_HANDLE = 1;
    private static final float OUTER_SWATCH_SIZE = 10.0f;
    private static final float SWATCH_GAP = 3.0f;
    private static final float INNER_SWATCH_SIZE = 10.0f;
    private ColorPickerWidget colorPicker;
    private int selectedHandle = -1;

    public ColorRangeSettingComponent(ColorRangeSetting colorRangeSetting, SettingWidget settingWidget) {
        super(colorRangeSetting, settingWidget);
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
        float f = 26.0f;
        FontMetrics fontMetrics = Font.REGULAR.metrics(8.0f);
        float f2 = 19.0f;
        this.drawScrollableText(drawContext, fontMetrics, Localization.translate(((ColorRangeSetting)this.setting).getName()), this.x + 10.0f, this.y + UiUtils.center(fontMetrics.getFontTopOffset(), f2) - 0.5f, this.width - f - 20.0f, ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.contentAnimation.getValue())), 0.7f, 0.99f);
        this.renderSwatchBorder(drawContext, this.getLowerSwatchX());
        this.renderSwatchBorder(drawContext, this.getUpperSwatchX());
        this.renderSwatch(drawContext, this.getLowerSwatchX(), ((ColorRangeSetting)this.setting).getColorRangeSettingColorRGBA());
        this.renderSwatch(drawContext, this.getUpperSwatchX(), ((ColorRangeSetting)this.setting).getSecondColor());
        if (this.colorPicker != null && this.colorPicker.isOpen()) {
            if (this.selectedHandle == 0) {
                ((ColorRangeSetting)this.setting).setFirstColor(this.colorPicker.getColor());
            } else if (this.selectedHandle == 1) {
                ((ColorRangeSetting)this.setting).setSecondColor(this.colorPicker.getColor());
            }
        } else if (this.colorPicker != null && !this.colorPicker.isOpen()) {
            this.colorPicker = null;
            this.selectedHandle = -1;
        }
    }

    private void renderSwatchBorder(RockstarDrawContext drawContext, float f) {
        drawContext.drawRoundedRect(f, this.y + 4.0f, 10.0f, 10.0f, WidgetState.uniform(4.5f), ColorPalette.BORDER_COLOR);
    }

    private void renderSwatch(RockstarDrawContext drawContext, float f, ColorRGBA colorRGBA) {
        drawContext.drawRoundedRect(f + 2.0f, this.y + 6.0f, 6.0f, 6.0f, WidgetState.uniform(4.5f), colorRGBA);
    }

    private float getUpperSwatchX() {
        return this.x + this.width - 10.0f - 9.0f;
    }

    private float getLowerSwatchX() {
        return this.getUpperSwatchX() - 13.0f;
    }

    private boolean isSwatchHit(double d, double d2, float f) {
        return d >= (double)f && d <= (double)(f + 10.0f) && d2 >= (double)(this.y + 4.0f) && d2 <= (double)(this.y + 4.0f + 10.0f);
    }

    @Override
    public void renderDivider(RockstarDrawContext drawContext) {
        float f = 0.5f;
        drawContext.drawRect(this.x, this.y + this.height, this.width, f, ColorPalette.getPrimaryTextColor().withAlpha(5.1f));
    }

    @Override
    @Compile
    public void mouseClicked(double d, double d2, PointerAction pointerAction) {
        if (pointerAction == PointerAction.LEFT_CLICK) {
            if (this.isSwatchHit(d, d2, this.getLowerSwatchX())) {
                this.openColorPicker(d, d2, 0, ((ColorRangeSetting)this.setting).getColorRangeSettingColorRGBA());
            } else if (this.isSwatchHit(d, d2, this.getUpperSwatchX())) {
                this.openColorPicker(d, d2, 1, ((ColorRangeSetting)this.setting).getSecondColor());
            }
        }
        super.mouseClicked(d, d2, pointerAction);
    }

    private void openColorPicker(double d, double d2, int n, ColorRGBA colorRGBA) {
        this.selectedHandle = n;
        ColorRangeSetting colorRangeSetting = (ColorRangeSetting)this.setting;
        this.colorPicker = new ColorPickerWidget((float)d, (float)d2, 6.0f, colorRangeSetting.isFirstColorSelected(), colorRGBA, Localization.translate(colorRangeSetting.getName()));
    }

    @Override
    public float getHeight() {
        this.height = 18.0f;
        return 18.0f;
    }
}
