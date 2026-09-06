/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 */
package moscow.rockstar.ui.widgets.settings;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.network.http.client.ReactorNettyClient;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.RangeSetting;
import moscow.rockstar.settings.SettingComponent;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.settings.SettingWidget;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import pyrock.utility.render.ColorRGBA;

public class RangeSettingComponent
extends SettingComponent<RangeSetting> {
    private final Animation firstValueAnimation = new Animation(500L, Easing.easeOutOvershoot);
    private final Animation secondValueAnimation = new Animation(500L, Easing.easeOutOvershoot);
    private boolean draggingFirstHandle;
    private boolean draggingSecondHandle;
    private int pressedMouseButton;

    public RangeSettingComponent(RangeSetting rangeSetting, SettingWidget settingWidget) {
        super(rangeSetting, settingWidget);
    }

    @Override
    protected void renderContent(RockstarDrawContext drawContext) {
        float f;
        float f2 = this.x + 9.0f;
        float f3 = this.y + 2.0f;
        float f4 = this.width - 18.0f;
        FontMetrics fontMetrics = Font.REGULAR.metrics(8.0f);
        float f5 = 10.0f;
        float f6 = Font.REGULAR.metrics(7.0f).getFontTopOffset();
        float f7 = ((RangeSetting)this.setting).getFirstValue();
        if (f7 >= (f = ((RangeSetting)this.setting).getSecondValue())) {
            f7 = ((RangeSetting)this.setting).getSecondValue();
            f = ((RangeSetting)this.setting).getFirstValue();
        }
        this.firstValueAnimation.update(f7);
        this.secondValueAnimation.update(f);
        this.contentAnimation.setReverse(this.contains(drawContext.mouseX(), drawContext.mouseY()));
        drawContext.drawRoundedRect(f2, f3 + this.height - 12.0f, f4, 2.0f, WidgetState.uniform(0.25f), ColorPalette.getPanelBackgroundColor().withAlpha(178.5f));
        drawContext.drawRoundedRect(f2 + f4 * UiUtils.normalize(this.firstValueAnimation.getValue(), ((RangeSetting)this.setting).getMinimum(), ((RangeSetting)this.setting).getMaximum()), f3 + this.height - 12.0f, f4 * UiUtils.normalize(this.secondValueAnimation.getValue(), ((RangeSetting)this.setting).getMinimum(), ((RangeSetting)this.setting).getMaximum()) - f4 * UiUtils.normalize(this.firstValueAnimation.getValue(), ((RangeSetting)this.setting).getMinimum(), ((RangeSetting)this.setting).getMaximum()), 2.0f, WidgetState.uniform(0.25f), ColorPalette.getAccentColor());
        drawContext.drawShadow(f2 + f4 * UiUtils.normalize(this.firstValueAnimation.getValue(), ((RangeSetting)this.setting).getMinimum(), ((RangeSetting)this.setting).getMaximum()) - 3.0f, f3 + this.height - 14.0f, 6.0f, 6.0f, 10.0f, WidgetState.uniform(3.0f), ColorRGBA.BLACK.withAlpha(63.75f));
        drawContext.drawRoundedRect(f2 + f4 * UiUtils.normalize(this.firstValueAnimation.getValue(), ((RangeSetting)this.setting).getMinimum(), ((RangeSetting)this.setting).getMaximum()) - 3.0f, f3 + this.height - 14.0f, 6.0f, 6.0f, WidgetState.uniform(3.0f), ColorRGBA.WHITE);
        drawContext.drawShadow(f2 + f4 * UiUtils.normalize(this.firstValueAnimation.getValue(), ((RangeSetting)this.setting).getMinimum(), ((RangeSetting)this.setting).getMaximum()) + f4 * UiUtils.normalize(this.secondValueAnimation.getValue(), ((RangeSetting)this.setting).getMinimum(), ((RangeSetting)this.setting).getMaximum()) - f4 * UiUtils.normalize(this.firstValueAnimation.getValue(), ((RangeSetting)this.setting).getMinimum(), ((RangeSetting)this.setting).getMaximum()) - 3.0f, f3 + this.height - 14.0f, 6.0f, 6.0f, 10.0f, WidgetState.uniform(3.0f), ColorRGBA.BLACK.withAlpha(63.75f));
        drawContext.drawRoundedRect(f2 + f4 * UiUtils.normalize(this.firstValueAnimation.getValue(), ((RangeSetting)this.setting).getMinimum(), ((RangeSetting)this.setting).getMaximum()) + f4 * UiUtils.normalize(this.secondValueAnimation.getValue(), ((RangeSetting)this.setting).getMinimum(), ((RangeSetting)this.setting).getMaximum()) - f4 * UiUtils.normalize(this.firstValueAnimation.getValue(), ((RangeSetting)this.setting).getMinimum(), ((RangeSetting)this.setting).getMaximum()) - 3.0f, f3 + this.height - 14.0f, 6.0f, 6.0f, WidgetState.uniform(3.0f), ColorRGBA.WHITE);
        String string = Localization.translateFormatted("ui.range_format", moscow.rockstar.util.NumberFormatting.formatOneDecimal(this.firstValueAnimation.getValue()), moscow.rockstar.util.NumberFormatting.formatOneDecimal(this.secondValueAnimation.getValue()));
        this.drawScrollableText(drawContext, fontMetrics, Localization.translate(((RangeSetting)this.setting).getName()), this.x + f5, f3 + 11.0f - fontMetrics.getFontTopOffset(), this.getSettingWidget().getWidth() - f5 - Font.REGULAR.metrics(7.0f).measureText(string) - 10.0f, ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.contentAnimation.getValue())), 0.8f, 1.0f);
        if (this.contains(drawContext) && (float)drawContext.mouseY() > this.getVisibleRegionTop() && (float)drawContext.mouseY() < this.getVisibleRegionTop() + this.getVisibleRegionHeight()) {
            moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
        }
        if ((this.draggingFirstHandle || this.draggingSecondHandle) && !UiUtils.isMouseButtonDown(this.pressedMouseButton)) {
            this.draggingFirstHandle = false;
            this.draggingSecondHandle = false;
        }
        if (this.draggingFirstHandle) {
            float f8 = UiUtils.interpolateClamped(((RangeSetting)this.setting).getMinimum(), ((RangeSetting)this.setting).getMaximum(), f2, f4, drawContext.mouseX());
            ((RangeSetting)this.setting).updateFirstValue(f8);
            moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HORIZONTAL_RESIZE);
        } else if (this.draggingSecondHandle) {
            float f9 = UiUtils.interpolateClamped(((RangeSetting)this.setting).getMinimum(), ((RangeSetting)this.setting).getMaximum(), f2, f4, drawContext.mouseX());
            ((RangeSetting)this.setting).updateSecondValue(f9);
            moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HORIZONTAL_RESIZE);
        }
    }

    @Override
    public void renderValue(RockstarDrawContext drawContext) {
        float f = this.x + 9.0f;
        float f2 = this.y + 2.0f;
        float f3 = this.width - 18.0f;
        float f4 = Font.REGULAR.metrics(7.0f).getFontTopOffset();
        String string = Localization.translateFormatted("ui.range_format", moscow.rockstar.util.NumberFormatting.formatOneDecimal(this.firstValueAnimation.getValue()), moscow.rockstar.util.NumberFormatting.formatOneDecimal(this.secondValueAnimation.getValue()));
        drawContext.drawRightText(Font.REGULAR.metrics(7.0f), string, f + f3, f2 + 11.0f - f4, ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.contentAnimation.getValue()) * RenderSystem.getShaderColor()[3]));
    }

    @Override
    public void renderDivider(RockstarDrawContext drawContext) {
        float f = 0.5f;
        drawContext.drawRect(this.x, this.y + this.height, this.width, f, ColorPalette.getPrimaryTextColor().withAlpha(5.1f));
    }

    @Override
    public void mouseClicked(double d, double d2, PointerAction pointerAction) {
        float f = this.x + 9.0f;
        float f2 = this.width - 18.0f;
        if (this.contains(d, d2)) {
            float f3;
            float f4 = (float)Math.abs(d - (double)(f + f2 * UiUtils.normalize(((RangeSetting)this.setting).getFirstValue(), ((RangeSetting)this.setting).getMinimum(), ((RangeSetting)this.setting).getMaximum())));
            if (f4 < (f3 = (float)Math.abs(d - (double)(f + f2 * UiUtils.normalize(((RangeSetting)this.setting).getSecondValue(), ((RangeSetting)this.setting).getMinimum(), ((RangeSetting)this.setting).getMaximum()))))) {
                this.draggingFirstHandle = true;
            } else {
                this.draggingSecondHandle = true;
            }
            this.pressedMouseButton = pointerAction.getButtonCode();
        }
        super.mouseClicked(d, d2, pointerAction);
    }

    @Override
    public void mouseReleased(double d, double d2, PointerAction pointerAction) {
        this.draggingFirstHandle = false;
        this.draggingSecondHandle = false;
        super.mouseReleased(d, d2, pointerAction);
    }

    @Override
    public float getHeight() {
        this.height = 29.0f;
        return 29.0f;
    }
}
