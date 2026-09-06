/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Vector2f
 */
package moscow.rockstar.ui.widgets.settings;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.modules.visuals.hud.Interface;
import moscow.rockstar.network.http.client.ReactorNettyClient;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.settings.NumberSetting;
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
import moscow.rockstar.ui.text.TextInputField;
import moscow.rockstar.util.Timer;
import net.minecraft.client.util.math.Vector2f;
import pyrock.utility.render.ColorRGBA;
import ua.mintantileak.spk.Compile;

public class NumberSettingComponent
extends SettingComponent<NumberSetting> {
    private final Animation valueAnimation = new Animation(500L, Easing.easeOutOvershoot);
    private final Animation dragAnimation = new Animation(500L, Easing.easeInOutCubicBezier);
    private final Timer shaderRefreshTimer = new Timer();
    private boolean dragging;
    private int dragButton;
    private boolean editing;
    private TextInputField textEditor;
    private static NumberSettingComponent keyboardAdjustmentTarget;
    private static NumberSettingComponent hoveredComponent;

    public NumberSettingComponent(NumberSetting numberSetting, SettingWidget settingWidget) {
        super(numberSetting, settingWidget);
    }

    @Override
    public void tick() {
        this.textEditor = new TextInputField(Font.REGULAR.metrics(7.0f));
        this.textEditor.setNumericOnly(true);
        this.syncTextEditor();
        this.valueAnimation.setValue(((NumberSetting)this.setting).getValue());
        super.tick();
    }

    @Override
    public void renderOverlay(RockstarDrawContext drawContext) {
        if (this.contains(drawContext.mouseX(), drawContext.mouseY())) {
            hoveredComponent = this;
        }
        if (this.editing && this.textEditor != null) {
            this.textEditor.render(drawContext);
            if (!this.textEditor.isFocused()) {
                this.commitTextEdit();
            }
        } else {
            this.refreshTextEditor();
        }
        super.renderOverlay(drawContext);
    }

    @Override
    protected void renderContent(RockstarDrawContext drawContext) {
        float f;
        float f2 = this.x + 9.0f;
        float f3 = this.y + 2.0f;
        if (hoveredComponent == this && (float)drawContext.mouseY() > this.getVisibleRegionTop() && (float)drawContext.mouseY() < this.getVisibleRegionTop() + this.getVisibleRegionHeight()) {
            hoveredComponent = null;
        }
        float f4 = this.width - 18.0f;
        FontMetrics fontMetrics = Font.REGULAR.metrics(8.0f);
        float f5 = 10.0f;
        float f6 = Font.REGULAR.metrics(7.0f).getFontTopOffset();
        this.valueAnimation.update(((NumberSetting)this.setting).getValue());
        this.contentAnimation.setReverse(this.contains(drawContext.mouseX(), drawContext.mouseY()));
        drawContext.drawRoundedRect(f2, f3 + this.height - 12.0f, f4, 2.0f, WidgetState.uniform(0.25f), ColorPalette.getPanelBackgroundColor().withAlpha((255.0f - 100.0f * Interface.getLiquidGlassAlpha()) * 0.7f));
        drawContext.drawRoundedRect(f2, f3 + this.height - 12.0f, f4 * UiUtils.normalize(this.valueAnimation.getValue(), ((NumberSetting)this.setting).getMinValue(), ((NumberSetting)this.setting).getMaxValue()), 2.0f, WidgetState.uniform(0.25f), ColorPalette.getAccentColor());
        if (this.shaderRefreshTimer.hasElapsed(50L)) {
            ShaderRenderer.renderDefaultFramebuffer();
            this.shaderRefreshTimer.reset();
        }
        if (Interface.isLiquidGlassEnabled()) {
            drawContext.drawShadow(f2 + f4 * UiUtils.normalize(this.valueAnimation.getValue(), ((NumberSetting)this.setting).getMinValue(), ((NumberSetting)this.setting).getMaxValue()) - 4.5f - 3.0f * this.dragAnimation.getValue(), f3 + this.height - 11.0f - 3.0f - 2.0f * this.dragAnimation.getValue(), 9.0f + 6.0f * this.dragAnimation.getValue(), 6.0f + 4.0f * this.dragAnimation.getValue(), 10.0f, WidgetState.uniform(3.0f + this.dragAnimation.getValue() * 2.0f), ColorRGBA.BLACK.withAlpha(255.0f * (0.25f + 0.2f * this.dragAnimation.getValue()) * Interface.getLiquidGlassAlpha()));
            drawContext.drawSquircle(f2 + f4 * UiUtils.normalize(this.valueAnimation.getValue(), ((NumberSetting)this.setting).getMinValue(), ((NumberSetting)this.setting).getMaxValue()) - 4.5f - 3.0f * this.dragAnimation.getValue(), f3 + this.height - 11.0f - 3.0f - 2.0f * this.dragAnimation.getValue(), 9.0f + 6.0f * this.dragAnimation.getValue(), 6.0f + 4.0f * this.dragAnimation.getValue(), 7.0f, WidgetState.uniform(3.0f + this.dragAnimation.getValue()), ColorRGBA.WHITE.withAlpha(255.0f * (1.0f - this.dragAnimation.getValue()) * Interface.getLiquidGlassAlpha()));
            drawContext.drawLiquidGlass(f2 + f4 * UiUtils.normalize(this.valueAnimation.getValue(), ((NumberSetting)this.setting).getMinValue(), ((NumberSetting)this.setting).getMaxValue()) - 4.5f - 3.0f * this.dragAnimation.getValue(), f3 + this.height - 11.0f - 3.0f - 2.0f * this.dragAnimation.getValue(), 9.0f + 6.0f * this.dragAnimation.getValue(), 6.0f + 4.0f * this.dragAnimation.getValue(), 7.0f, WidgetState.uniform(3.0f + this.dragAnimation.getValue()), ColorRGBA.WHITE.withAlpha(255.0f * this.dragAnimation.getValue() * Interface.getLiquidGlassAlpha()), false);
        }
        if (Interface.isBlurEnabled()) {
            drawContext.drawShadow(f2 + f4 * UiUtils.normalize(this.valueAnimation.getValue(), ((NumberSetting)this.setting).getMinValue(), ((NumberSetting)this.setting).getMaxValue()) - 3.0f, f3 + this.height - 14.0f + this.dragAnimation.getValue(), 6.0f, 6.0f - this.dragAnimation.getValue() * 2.0f, 10.0f, WidgetState.uniform(3.0f - this.dragAnimation.getValue() * 2.0f), ColorRGBA.BLACK.withAlpha(63.75f * Interface.getBlurAlpha()));
            drawContext.drawRoundedRect(f2 + f4 * UiUtils.normalize(this.valueAnimation.getValue(), ((NumberSetting)this.setting).getMinValue(), ((NumberSetting)this.setting).getMaxValue()) - 3.0f, f3 + this.height - 14.0f + this.dragAnimation.getValue(), 6.0f, 6.0f - this.dragAnimation.getValue() * 2.0f, WidgetState.uniform(3.0f - this.dragAnimation.getValue() * 2.0f), ColorRGBA.WHITE.withAlpha(255.0f * Interface.getBlurAlpha()));
        }
        String string = moscow.rockstar.util.NumberFormatting.formatDecimal(Math.clamp(this.valueAnimation.getValue(), ((NumberSetting)this.setting).getMinValue(), ((NumberSetting)this.setting).getMaxValue())) + ((NumberSetting)this.setting).formatValue();
        float f7 = Font.REGULAR.metrics(7.0f).measureText(string);
        float f8 = f2 + f4 - f7;
        float f9 = f3 + 11.0f - f6;
        float f10 = Font.REGULAR.metrics(7.0f).getFontTopOffset();
        this.drawScrollableText(drawContext, fontMetrics, Localization.translate(((NumberSetting)this.setting).getName()), this.x + f5, f3 + 11.0f - fontMetrics.getFontTopOffset(), this.getSettingWidget().getWidth() - f5 - Font.REGULAR.metrics(7.0f).measureText(string) - 10.0f, ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.contentAnimation.getValue())), 0.8f, 1.0f);
        if (this.editing && this.textEditor != null) {
            f = f9 - 1.0f;
            float f11 = f7 + 5.0f;
            float f12 = f10 + 2.0f;
            this.textEditor.setBounds(f8, f, f11, f12);
            this.textEditor.setOpacity(1.0f);
            this.textEditor.setTextColor(ColorPalette.getPrimaryTextColor());
            this.textEditor.render(drawContext);
        }
        if (this.contains(drawContext) && (float)drawContext.mouseY() > this.getVisibleRegionTop() && (float)drawContext.mouseY() < this.getVisibleRegionTop() + this.getVisibleRegionHeight()) {
            moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
        }
        if (this.dragging && !UiUtils.isMouseButtonDown(this.dragButton)) {
            this.dragging = false;
        }
        this.dragAnimation.setDuration(200L);
        this.dragAnimation.update(this.dragging ? 1.0f : 0.0f);
        if (this.dragging) {
            f = UiUtils.interpolateClamped(((NumberSetting)this.setting).getMinValue(), ((NumberSetting)this.setting).getMaxValue(), f2, f4, drawContext.mouseX());
            ((NumberSetting)this.setting).updateValue(f);
            moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HORIZONTAL_RESIZE);
            keyboardAdjustmentTarget = this;
        }
    }

    @Override
    public void renderValue(RockstarDrawContext drawContext) {
        if (this.editing && this.textEditor != null) {
            return;
        }
        float f = this.x + 9.0f;
        float f2 = this.y + 2.0f;
        float f3 = this.width - 18.0f;
        float f4 = Font.REGULAR.metrics(7.0f).getFontTopOffset();
        String string = moscow.rockstar.util.NumberFormatting.formatDecimal(Math.clamp(this.valueAnimation.getValue(), ((NumberSetting)this.setting).getMinValue(), ((NumberSetting)this.setting).getMaxValue())) + ((NumberSetting)this.setting).formatValue();
        drawContext.drawRightText(Font.REGULAR.metrics(7.0f), string, f + f3, f2 + 11.0f - f4, ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.contentAnimation.getValue()) * RenderSystem.getShaderColor()[3]));
    }

    @Override
    public void renderDivider(RockstarDrawContext drawContext) {
        float f = 0.5f;
        drawContext.drawRect(this.x, this.y + this.height, this.width, f, ColorPalette.getPrimaryTextColor().withAlpha(5.1f));
    }

    @Override
    @Compile
    public void mouseClicked(double d, double d2, PointerAction pointerAction) {
        boolean bl = this.editing;
        if (this.contains(d, d2)) {
            String string = moscow.rockstar.util.NumberFormatting.formatDecimal(((NumberSetting)this.setting).getValue()) + ((NumberSetting)this.setting).formatValue();
            float f = Font.REGULAR.metrics(7.0f).measureText(string);
            float f2 = this.x + 9.0f + (this.width - 18.0f) - f;
            float f3 = this.y + 2.0f + 11.0f - Font.REGULAR.metrics(7.0f).getFontTopOffset();
            float f4 = Font.REGULAR.metrics(7.0f).getFontTopOffset();
            if (d >= (double)f2 && d <= (double)(f2 + f) && d2 >= (double)f3 && d2 <= (double)(f3 + f4)) {
                this.beginTextEdit();
            } else {
                if (this.editing) {
                    this.commitTextEdit();
                }
                this.dragging = true;
                this.dragButton = pointerAction.getButtonCode();
            }
        }
        if (this.editing && this.textEditor != null && bl) {
            this.textEditor.mouseClicked(d, d2, pointerAction);
            if (pointerAction == PointerAction.LEFT_CLICK && !this.textEditor.contains(d, d2) && !this.contains(d, d2)) {
                this.commitTextEdit();
            }
        }
        super.mouseClicked(d, d2, pointerAction);
    }

    @Override
    @Compile
    public void mouseReleased(double d, double d2, PointerAction pointerAction) {
        this.dragging = false;
        if (this.editing && this.textEditor != null) {
            this.textEditor.mouseReleased(d, d2, pointerAction);
        }
        super.mouseReleased(d, d2, pointerAction);
    }

    @Override
    @Compile
    public void keyPressed(int n, int n2, int n3) {
        if (this.editing && this.textEditor != null) {
            this.textEditor.keyPressed(n, n2, n3);
            if (n == 257 || n == 335) {
                this.commitTextEdit();
            } else if (n == 256) {
                this.cancelTextEdit();
            }
        } else if (n == 262 || n == 263) {
            Vector2f class_56112 = UiUtils.mousePosition();
            if (hoveredComponent == this && this.contains(class_56112.getX(), class_56112.getY())) {
                ((NumberSetting)this.getSetting()).updateValue(((NumberSetting)this.getSetting()).getValue() + ((NumberSetting)this.getSetting()).getStep() * 0.7f * (float)(n == 262 ? 1 : -1));
            } else if (hoveredComponent == null && keyboardAdjustmentTarget == this) {
                ((NumberSetting)keyboardAdjustmentTarget.getSetting()).updateValue(((NumberSetting)keyboardAdjustmentTarget.getSetting()).getValue() + ((NumberSetting)keyboardAdjustmentTarget.getSetting()).getStep() * 0.7f * (float)(n == 262 ? 1 : -1));
            }
        }
    }

    @Override
    public boolean charTyped(char c, int n) {
        if (this.editing && this.textEditor != null) {
            return this.textEditor.charTyped(c, n);
        }
        return false;
    }

    @Override
    public void mouseScrolled(double d, double d2, double d3, double d4) {
        if (this.editing || d4 == 0.0) {
            return;
        }
        if (this.contains(d, d2)) {
            // empty if block
        }
    }

    private void commitTextEdit() {
        if (this.textEditor != null) {
            try {
                String string = this.textEditor.getText().replace(',', '.');
                if (!(string.isEmpty() || string.equals("-") || string.equals("."))) {
                    float f = Float.parseFloat(string);
                    f = Math.max(((NumberSetting)this.setting).getMinValue(), Math.min(((NumberSetting)this.setting).getMaxValue(), f));
                    ((NumberSetting)this.setting).updateValue(f);
                }
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
            this.editing = false;
            this.textEditor.setFocused(false);
            this.syncTextEditor();
        }
    }

    @Compile
    private void beginTextEdit() {
        this.editing = true;
        if (this.textEditor != null) {
            String string = moscow.rockstar.util.NumberFormatting.formatDecimal(((NumberSetting)this.setting).getValue());
            this.textEditor.setText(string);
            this.textEditor.setPlaceholder(string);
            this.textEditor.setFocused(true);
        }
    }

    @Compile
    private void cancelTextEdit() {
        this.editing = false;
        if (this.textEditor != null) {
            this.textEditor.setFocused(false);
            this.syncTextEditor();
        }
    }

    @Compile
    private void syncTextEditor() {
        if (this.textEditor == null) {
            return;
        }
        String string = moscow.rockstar.util.NumberFormatting.formatDecimal(((NumberSetting)this.setting).getValue());
        this.textEditor.setPlaceholder(string);
        if (!this.editing) {
            this.textEditor.setText(string);
        }
    }

    @Compile
    private void refreshTextEditor() {
        if (this.textEditor != null && !this.editing) {
            this.textEditor.setPlaceholder(moscow.rockstar.util.NumberFormatting.formatDecimal(((NumberSetting)this.setting).getValue()));
        }
    }

    public static void clearInteractionTargets() {
        if (keyboardAdjustmentTarget != null) {
            NumberSettingComponent.keyboardAdjustmentTarget.dragging = false;
            keyboardAdjustmentTarget = null;
        }
        hoveredComponent = null;
    }

    @Override
    public float getHeight() {
        this.height = 29.0f;
        return 29.0f;
    }
}
