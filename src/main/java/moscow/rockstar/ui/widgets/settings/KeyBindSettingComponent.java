/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.ui.widgets.settings;

import lombok.Generated;
import moscow.rockstar.network.http.client.ReactorNettyClient;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.IntegerSetting;
import moscow.rockstar.settings.SettingComponent;
import moscow.rockstar.ui.animation.AnimatedColor;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.ItemGrid;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.settings.SettingWidget;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

public class KeyBindSettingComponent
extends SettingComponent<IntegerSetting> {
    private final AnimatedColor keyColor = new AnimatedColor(300L, new ColorRGBA(24.0f, 24.0f, 27.0f), Easing.easeInOutCubicBezier);
    private final Animation widthAnimation = new Animation(300L, Easing.easeInOutCubicBezier);
    private Animation fadeAnimation = new Animation(300L, 1.0f, Easing.easeInOutCubicBezier);
    private int previousKey;
    private boolean capturingKey;

    public boolean isCapturingKey() {
        return this.capturingKey;
    }

    public KeyBindSettingComponent(IntegerSetting integerSetting, SettingWidget settingWidget) {
        super(integerSetting, settingWidget);
    }

    @Override
    protected void renderContent(RockstarDrawContext drawContext) {
        FontMetrics fontMetrics = Font.REGULAR.metrics(8.0f);
        FontMetrics fontMetrics2 = Font.REGULAR.metrics(7.0f);
        float f = 10.0f;
        float f2 = 19.0f;
        this.keyColor.setTargetColor(this.capturingKey ? ColorPalette.getAccentColor() : ColorPalette.getPrimaryTextColor());
        this.fadeAnimation.setDuration(500L);
        this.fadeAnimation.update(1.0f);
        String string = moscow.rockstar.ui.input.KeyDisplayFormatter.formatKey(((IntegerSetting)this.setting).getValue());
        String string2 = moscow.rockstar.ui.input.KeyDisplayFormatter.formatKey(this.previousKey);
        float f3 = fontMetrics2.measureText(string) + 7.0f;
        this.widthAnimation.update(f3);
        drawContext.drawRoundedRect(this.x + this.width - 9.0f - this.widthAnimation.getValue(), this.y + 4.0f, this.widthAnimation.getValue(), 11.0f, WidgetState.uniform(3.0f), ColorPalette.getPanelBackgroundColor());
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)(this.x + this.width - 9.0f - this.widthAnimation.getValue()), (float)(this.y + 4.0f), (float)this.widthAnimation.getValue(), (float)11.0f);
        drawContext.drawText(fontMetrics2, string2, this.x + this.width - 9.0f - this.widthAnimation.getValue() + 4.0f + 4.0f * this.fadeAnimation.getValue(), this.y + 7.0f, this.keyColor.getColor().withAlpha(255.0f * (0.75f + 0.25f * this.contentAnimation.getValue()) * (1.0f - this.fadeAnimation.getValue())));
        drawContext.drawText(fontMetrics2, string, this.x + this.width - 9.0f - this.widthAnimation.getValue() + 4.0f - 4.0f + 4.0f * this.fadeAnimation.getValue(), this.y + 7.0f, this.keyColor.getColor().withAlpha(255.0f * (0.75f + 0.25f * this.contentAnimation.getValue()) * this.fadeAnimation.getValue()));
        moscow.rockstar.render.state.UiScissorStack.pop();
        this.drawScrollableText(drawContext, fontMetrics, Localization.translate(((IntegerSetting)this.setting).getName()), this.x + f, this.y + UiUtils.center(fontMetrics.getFontTopOffset(), f2), this.width - this.widthAnimation.getValue() - 20.0f, ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.contentAnimation.getValue())), 0.7f, 0.99f);
        if (this.contains(drawContext) && (float)drawContext.mouseY() > this.getVisibleRegionTop() && (float)drawContext.mouseY() < this.getVisibleRegionTop() + this.getVisibleRegionHeight()) {
            moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
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
            boolean bl = this.capturingKey = !this.capturingKey;
        }
        if (this.capturingKey && pointerAction != PointerAction.LEFT_CLICK) {
            int n = pointerAction.getButtonCode();
            ((IntegerSetting)this.setting).setValue(n);
            this.capturingKey = false;
        }
        super.mouseClicked(d, d2, pointerAction);
    }

    @Override
    @Compile
    public void keyPressed(int n, int n2, int n3) {
        if (this.capturingKey) {
            this.previousKey = ((IntegerSetting)this.setting).getValue();
            if (n == 256 || n == 261) {
                ((IntegerSetting)this.setting).setValue(-1);
            } else {
                ((IntegerSetting)this.setting).setValue(n);
            }
            this.fadeAnimation = new Animation(500L, 0.0f, Easing.easeInOutCubicBezier);
            this.capturingKey = false;
            return;
        }
        super.keyPressed(n, n2, n3);
    }

    @Override
    public float getHeight() {
        this.height = 19.0f;
        return 19.0f;
    }

    @Generated
    public void setCapturingKey(boolean bl) {
        this.capturingKey = bl;
    }
}
