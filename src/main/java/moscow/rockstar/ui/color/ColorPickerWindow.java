/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Screen
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.ui.color;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Consumer;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.modules.visuals.hud.Interface;
import moscow.rockstar.network.http.client.ReactorNettyClient;
import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.colors.GradientColors;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.ui.animation.AnimatedColor;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.color.ColorPickerWidget.SavedColor;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.DragMode;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.ItemGrid;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.theme.ColorTheme;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.Rect;

public class ColorPickerWindow
extends UiNode
implements WindowMetricsProvider,
WindowHandle {
    private final MinecraftClient minecraftClient = MinecraftClient.getInstance();
    private final Animation openAnimation = new Animation(300L, 0.0f, Easing.easeInOutCubicBezier);
    private final Animation windowAnimation = new Animation(300L, 0.0f, Easing.easeInOutCubicBezier);
    private final Animation sampleTooltipAnimation = new Animation(300L, 0.0f, Easing.easeOutOvershootSoft);
    private final AnimatedColor huePreviewColor = new AnimatedColor(300L);
    private final AnimatedColor displayedColor = new AnimatedColor(200L);
    private final Animation hueAnimation = new Animation(500L, Easing.easeOutOvershoot);
    private final Animation saturationAnimation = new Animation(500L, Easing.easeOutOvershoot);
    private final Animation brightnessAnimation = new Animation(500L, Easing.easeOutOvershoot);
    private final Animation opacityAnimation = new Animation(500L, Easing.easeOutOvershoot);
    private final String title;
    private final boolean showOpacity;
    private final Consumer<ColorRGBA> colorChangeListener;
    private float scalePivot = 6.0f;
    private boolean samplingScreen;
    private boolean adjustingHue;
    private boolean adjustingColor;
    private boolean adjustingOpacity;
    private boolean draggingWindow;
    private float hue;
    private float saturation;
    private float brightness;
    private float opacity;

    public ColorPickerWindow(float f2, float f3, boolean bl, ColorRGBA colorRGBA, String string, Consumer<ColorRGBA> consumer) {
        this.showOpacity = bl;
        this.title = string;
        this.colorChangeListener = consumer;
        this.displayedColor.setCurrentColor(colorRGBA);
        this.size(143.0f, bl ? 160.0f : 136.0f);
        this.at(f2, f3);
        this.transition((f, uiNode, state) -> {
            state.progress = Math.min(1.0f, f);
        });
        this.lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(300L, Easing.easeOutOvershootSoft));
        this.draggable(DragMode.BOTH);
        this.beginEnter(0.0f);
        this.setColor(colorRGBA);
    }

    @Override
    protected void onTick(float f, float f2, float f3) {
        if (this.adjustingHue) {
            this.hue = UiUtils.interpolateClamped(0.0f, 1.0f, this.y() + 22.0f, 66.0f, f3);
        }
        if (this.adjustingColor) {
            this.saturation = 1.0f - UiUtils.interpolateClamped(0.0f, 1.0f, this.x() + 6.0f, 114.0f, f2);
            this.brightness = 1.0f - UiUtils.interpolateClamped(0.0f, 1.0f, this.y() + 20.0f, 70.0f, f3);
        }
        if (this.adjustingOpacity) {
            this.opacity = UiUtils.interpolateClamped(0.0f, 1.0f, this.x() + 7.0f, 88.0f, f2);
        }
        float f4 = this.x();
        float f5 = this.y();
        if (f4 + this.w() + 5.0f > INSTANCE.width()) {
            f4 = INSTANCE.width() - this.w() - 5.0f;
        }
        if (f5 + this.h() + 5.0f > INSTANCE.height()) {
            f5 = INSTANCE.height() - this.h() - 5.0f;
        }
        if (f4 != this.x() || f5 != this.y()) {
            this.at(f4, f5);
        }
        ColorPickerWidget.SAVED_COLORS.removeIf(preset -> preset.visibilityAnimation.getValue() == 0.0f && !preset.enabled);
        this.sampleTooltipAnimation.setEasing(this.samplingScreen ? Easing.easeOutBack : Easing.easeInBack);
        this.sampleTooltipAnimation.setReverse(this.samplingScreen);
        this.openAnimation.setReverse(this.appear() >= 0.6f);
        this.windowAnimation.setReverse(this.draggingWindow);
        this.hueAnimation.update(this.hue);
        this.saturationAnimation.update(1.0f - this.saturation);
        this.brightnessAnimation.update(1.0f - this.brightness);
        this.opacityAnimation.update(this.opacity);
        this.huePreviewColor.setTargetColor(ColorRGBA.fromHSB(this.hue, 1.0f, 1.0f));
        if (this.phase() != UiNode.LifecyclePhase.EXITING && this.colorChangeListener != null) {
            this.colorChangeListener.accept(this.getColor());
        }
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        float f2 = this.x();
        float f3 = this.y();
        float f4 = this.w();
        float f5 = this.h();
        boolean bl = RockstarClient.create().getColorTheme() == ColorTheme.DARK;
        ColorRGBA colorRGBA = ColorPalette.PANEL_COLOR.withAlpha(255.0f * (bl ? 0.9f - 0.6f * Interface.getLiquidGlassAlpha() : 0.7f));
        ColorRGBA colorRGBA2 = ColorRGBA.fromHSB(this.hue, this.saturation, this.brightness);
        ItemRenderUtils.translateAndScale(drawContext.getMatrices(), f2 + f4 / this.scalePivot, f3 + f5 / this.scalePivot, 0.5f + this.appear() * 0.5f);
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)(f2 + 1.0f), (float)(f3 + 1.0f), (float)(f4 - 2.0f), (float)(f5 - 2.0f));
        drawContext.drawShadow(f2 - 5.0f, f3 - 5.0f, f4 + 10.0f, f5 + 10.0f, 15.0f, WidgetState.uniform(6.0f), ColorRGBA.BLACK.withAlpha(255.0f * (0.1f + 0.15f * this.windowAnimation.getValue())));
        moscow.rockstar.render.state.UiScissorStack.pop();
        if (Interface.isBlurEnabled()) {
            drawContext.drawBlurredRect(f2, f3, f4, f5, 45.0f, 7.0f, WidgetState.uniform(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.appear() * Interface.getBlurAlpha()));
        }
        if (Interface.isLiquidGlassEnabled()) {
            drawContext.drawLiquidGlass(f2, f3, f4, f5, 7.0f, 0.05f - 0.03f * this.windowAnimation.getValue(), WidgetState.uniform(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.appear() * Interface.getLiquidGlassAlpha()));
        }
        drawContext.drawSquircle(f2, f3, f4, f5, 7.0f, WidgetState.uniform(6.0f), colorRGBA);
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)f2, (float)f3, (float)f4, (float)f5);
        drawContext.drawCenteredText(Font.MEDIUM.metrics(7.0f), this.title, f2 + f4 / 2.0f, f3 + 7.0f, ColorPalette.getPrimaryTextColor());
        drawContext.drawIcon("colorpicker/pipette", f2 + 7.0f, f3 + 6.0f, 8.0f);
        if (UiUtils.contains((double)(f2 + 7.0f), (double)(f3 + 6.0f), 8.0, 8.0, drawContext.mouseX(), drawContext.mouseY())) {
            moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
        }
        drawContext.drawRoundedRect(f2 + f4 - 15.0f, f3 + 5.0f, 10.0f, 10.0f, WidgetState.uniform(5.0f), ColorPalette.getPanelBackgroundColor());
        drawContext.drawIcon("xmark", f2 + f4 - 15.0f, f3 + 5.0f, 10.0f);
        if (UiUtils.contains((double)(f2 + f4 - 15.0f), (double)(f3 + 5.0f), 10.0, 10.0, drawContext.mouseX(), drawContext.mouseY())) {
            moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
        }
        drawContext.drawRoundedTexture(RockstarClient.resourceId("textures/hue.png"), f2 + f4 - 18.0f, f3 + 20.0f, 12.0f, 70.0f, WidgetState.uniform(4.0f));
        drawContext.drawRoundedRect(f2 + f4 - 16.0f, f3 + 22.0f + 64.0f * this.hueAnimation.getValue(), 8.0f, 2.0f, WidgetState.uniform(0.2f), ColorPalette.WHITE);
        if (UiUtils.contains(f2 + f4 - 18.0f, f3 + 20.0f, 12.0, 70.0, drawContext) || this.adjustingHue) {
            moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.VERTICAL_RESIZE);
        }
        drawContext.drawRoundedRect(f2 + 6.0f, f3 + 20.0f, 114.0f, 70.0f, WidgetState.uniform(4.0f), new GradientColors(this.huePreviewColor.getColor(), ColorPalette.BLACK, ColorPalette.WHITE, ColorPalette.BLACK));
        drawContext.drawRoundedRect(f2 + 6.0f + 114.0f * this.saturationAnimation.getValue() - 3.5f, f3 + 20.0f + 70.0f * this.brightnessAnimation.getValue() - 3.5f, 7.0f, 7.0f, WidgetState.uniform(2.5f), ColorPalette.WHITE);
        drawContext.drawRoundedRect(f2 + 7.0f + 114.0f * this.saturationAnimation.getValue() - 3.5f, f3 + 21.0f + 70.0f * this.brightnessAnimation.getValue() - 3.5f, 5.0f, 5.0f, WidgetState.uniform(1.5f), colorRGBA2);
        if (UiUtils.contains(f2 + 6.0f, f3 + 20.0f, 114.0, 70.0, drawContext) || this.adjustingColor) {
            moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.CROSSHAIR);
        }
        if (this.showOpacity) {
            drawContext.drawText(Font.MEDIUM.metrics(5.0f), Localization.translate("colorpicker.opacity").toUpperCase(), f2 + 6.0f, f3 + 95.0f, ColorPalette.getPrimaryTextColor().withAlpha(191.25f));
            drawContext.drawRoundedTexture(RockstarClient.resourceId("textures/empty.png"), f2 + 6.0f, f3 + 102.0f, 100.0f, 12.0f, WidgetState.uniform(5.0f));
            drawContext.drawRoundedRect(f2 + 6.0f - 0.5f, f3 + 102.0f - 0.5f, 101.0f, 13.0f, WidgetState.uniform(5.0f), new GradientColors(colorRGBA2.withAlpha(0.0f), colorRGBA2));
            drawContext.drawRoundedRect(f2 + f4 - 32.0f, f3 + 102.0f, 26.0f, 12.0f, WidgetState.uniform(2.0f), ColorPalette.MUTED_PANEL_COLOR.withAlpha(255.0f));
            drawContext.drawCenteredText(Font.MEDIUM.metrics(6.0f), (int)(this.opacity * 100.0f) + "%", f2 + f4 - 32.0f + 13.0f, f3 + 106.0f, ColorPalette.getPrimaryTextColor());
            drawContext.drawRoundedBorder(f2 + 7.0f + 88.0f * this.opacityAnimation.getValue(), f3 + 103.0f, 10.0f, 10.0f, 0.5f, WidgetState.uniform(4.0f), ColorPalette.WHITE);
            drawContext.drawRoundedRect(f2 + 8.0f + 88.0f * this.opacityAnimation.getValue(), f3 + 104.0f, 8.0f, 8.0f, WidgetState.uniform(3.0f), this.getColor());
            if (UiUtils.contains(f2 + 6.0f, f3 + 102.0f, 100.0, 12.0, drawContext) || this.adjustingOpacity) {
                moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HORIZONTAL_RESIZE);
            }
        }
        drawContext.drawRoundedRect(f2 + 6.0f, f3 + f5 - 36.0f, 29.0f, 29.0f, WidgetState.uniform(5.0f), this.getColor());
        float f6 = 0.0f;
        float f7 = 0.0f;
        for (SavedColor preset : ColorPickerWidget.SAVED_COLORS) {
            preset.visibilityAnimation.setReverse(preset.enabled);
            preset.selectionAnimation.setReverse(preset.matches(this.hue, this.saturation, this.brightness));
            if (preset.selectionAnimation.getValue() > 0.0f) {
                float f8 = preset.selectionAnimation.getValue();
                drawContext.drawRoundedRect(f2 + 45.0f + f6, f3 + f5 - 36.0f + f7, 11.0f, 11.0f, WidgetState.uniform(4.5f), preset.color.withAlpha(255.0f * preset.visibilityAnimation.getValue()));
                drawContext.drawRoundedBorder(f2 + 45.0f + f6 - 1.0f + 2.0f * f8, f3 + f5 - 36.0f + f7 - 1.0f + 2.0f * f8, 13.0f - 4.0f * f8, 13.0f - 4.0f * f8, 0.5f, WidgetState.uniform(6.5f - 2.0f * f8), ColorPalette.WHITE.withAlpha(255.0f * preset.visibilityAnimation.getValue() * preset.selectionAnimation.getValue()));
            } else {
                drawContext.drawRoundedRect(f2 + 45.0f + f6, f3 + f5 - 36.0f + f7, 11.0f, 11.0f, WidgetState.uniform(4.5f), preset.color.withAlpha(255.0f * preset.visibilityAnimation.getValue()));
            }
            if (UiUtils.contains(f2 + 45.0f + f6, f3 + f5 - 36.0f + f7, 11.0, 11.0, drawContext)) {
                moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
            }
            if (!(45.0f + (f6 += 20.0f * preset.visibilityAnimation.getValue()) > f4)) continue;
            f6 = 0.0f;
            f7 += 18.0f * preset.visibilityAnimation.getValue();
        }
        if (ColorPickerWidget.SAVED_COLORS.size() < 10) {
            drawContext.drawRoundedRect(f2 + 45.0f + f6, f3 + f5 - 36.0f + f7, 11.0f, 11.0f, WidgetState.uniform(4.5f), ColorPalette.getPanelBackgroundColor());
            drawContext.drawIcon("plus", f2 + 45.0f + f6, f3 + f5 - 36.0f + f7, 11.0f);
            if (UiUtils.contains(f2 + 45.0f + f6, f3 + f5 - 36.0f + f7, 11.0, 11.0, drawContext)) {
                moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
            }
        }
        moscow.rockstar.render.state.UiScissorStack.pop();
        ItemRenderUtils.popMatrix(drawContext.getMatrices());
        if (this.sampleTooltipAnimation.getValue() > 0.0f) {
            Object object;
            Rect rect = new Rect(drawContext.mouseX(), drawContext.mouseY() + 10, 45.0f + Font.REGULAR.metrics(6.0f).measureText(Localization.translate("colorpicker.click_to_sample")), 30.0f);
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)Math.min(1.0f, this.sampleTooltipAnimation.getValue()));
            ItemRenderUtils.translateAndScale(drawContext.getMatrices(), rect.getX() + rect.getWidth() / 2.0f, rect.getY() + rect.getHeight() / 2.0f, 0.5f + this.sampleTooltipAnimation.getValue() * 0.5f);
            drawContext.drawBlurredRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), 45.0f, 7.0f, WidgetState.uniform(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.sampleTooltipAnimation.getValue()));
            drawContext.drawSquircle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), 7.0f, WidgetState.uniform(6.0f), ColorPalette.getPanelColor().withAlpha(255.0f * (bl ? 0.8f : 0.7f)));
            object = ColorRGBA.fromPixel((float)((double)drawContext.mouseX() * INSTANCE.scaleFactor()), (float)((double)WINDOW.getHeight() - (double)drawContext.mouseY() * INSTANCE.scaleFactor()));
            drawContext.drawRoundedRect(rect.getX() + 5.0f, rect.getY() + 5.0f, rect.getHeight() - 10.0f, rect.getHeight() - 10.0f, WidgetState.uniform(5.0f), (ColorRGBA)object);
            drawContext.drawIcon("colorpicker/click", rect.getX() + rect.getHeight(), rect.getY() + 16.0f, 6.0f);
            drawContext.drawText(Font.REGULAR.metrics(6.0f), String.format("RGB %s %s %s", (int)((ColorRGBA)object).getRed(), (int)((ColorRGBA)object).getGreen(), (int)((ColorRGBA)object).getBlue()), rect.getX() + rect.getHeight(), rect.getY() + 8.0f, ColorPalette.getPrimaryTextColor());
            drawContext.drawText(Font.REGULAR.metrics(6.0f), Localization.translate("colorpicker.click_to_sample"), rect.getX() + rect.getHeight() + 8.0f, rect.getY() + 17.0f, ColorPalette.getPrimaryTextColor().withAlpha(200.0f));
            ItemRenderUtils.popMatrix(drawContext.getMatrices());
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
    }

    public ColorRGBA getColor() {
        this.displayedColor.setTargetColor(ColorRGBA.fromHSB(this.hue, this.saturation, this.brightness).withAlpha(this.showOpacity ? 255.0f * this.opacity : 255.0f));
        return this.displayedColor.getColor();
    }

    private void setColor(ColorRGBA colorRGBA) {
        this.hue = colorRGBA.getHue();
        this.saturation = colorRGBA.getBrightness();
        this.brightness = colorRGBA.getSaturation();
        this.opacity = colorRGBA.getAlpha() / 255.0f;
        this.displayedColor.setTargetColor(colorRGBA);
    }

    @Override
    public void close() {
        this.scalePivot = 2.0f;
        this.lifeMotion(Motion.resolveMotionMotionFromLongAndEasing(300L, Easing.easeInBack));
        this.samplingScreen = false;
        this.draggingWindow = false;
        this.adjustingHue = false;
        this.adjustingColor = false;
        this.adjustingOpacity = false;
        super.close();
    }

    @Override
    public boolean mouseClicked(float f, float f2, PointerAction pointerAction) {
        if (this.phase() == UiNode.LifecyclePhase.EXITING || this.phase() == UiNode.LifecyclePhase.DISCARDED) {
            return false;
        }
        float f3 = this.x();
        float f4 = this.y();
        float f5 = this.w();
        float f6 = this.h();
        if (this.samplingScreen) {
            if (pointerAction == PointerAction.LEFT_CLICK) {
                ColorRGBA colorRGBA = ColorRGBA.fromPixel((float)((double)f * INSTANCE.scaleFactor()), (float)((double)WINDOW.getHeight() - (double)f2 * INSTANCE.scaleFactor()));
                this.setColor(colorRGBA);
            }
            this.samplingScreen = false;
            return true;
        }
        boolean bl = ColorPickerWidget.SAVED_COLORS.size() < 10;
        float f7 = 0.0f;
        float f8 = 0.0f;
        for (SavedColor preset : ColorPickerWidget.SAVED_COLORS) {
            if (UiUtils.contains((double)(f3 + 45.0f + f7), (double)(f4 + f6 - 36.0f + f8), 11.0, 11.0, f, f2)) {
                if (pointerAction.getButtonCode() != 0) {
                    preset.enabled = false;
                    moscow.rockstar.api.data.ClientConfigManager.getInstance().save("client");
                } else {
                    this.setColor(preset.color);
                }
                return true;
            }
            if (preset.matches(this.hue, this.saturation, this.brightness)) {
                bl = false;
            }
            if (!(45.0f + (f7 += 20.0f) > f5)) continue;
            f7 = 0.0f;
            f8 += 18.0f;
        }
        if (UiUtils.contains((double)(f3 + 45.0f + f7), (double)(f4 + f6 - 36.0f + f8), 11.0, 11.0, f, f2) && bl) {
            ColorPickerWidget.SAVED_COLORS.add(new SavedColor(this.getColor()));
            moscow.rockstar.api.data.ClientConfigManager.getInstance().save("client");
            return true;
        }
        if (UiUtils.contains((double)(f3 + 7.0f), (double)(f4 + 6.0f), 8.0, 8.0, f, f2)) {
            this.samplingScreen = true;
            return true;
        }
        if (UiUtils.contains((double)(f3 + f5 - 15.0f), (double)(f4 + 5.0f), 10.0, 10.0, f, f2)) {
            this.close();
            return true;
        }
        if (UiUtils.contains((double)(f3 + f5 - 18.0f), (double)(f4 + 20.0f), 12.0, 70.0, f, f2)) {
            this.adjustingHue = true;
            return true;
        }
        if (UiUtils.contains((double)(f3 + 6.0f), (double)(f4 + 20.0f), 114.0, 70.0, f, f2)) {
            this.adjustingColor = true;
            return true;
        }
        if (this.showOpacity && UiUtils.contains((double)(f3 + 6.0f), (double)(f4 + 102.0f), 100.0, 12.0, f, f2)) {
            this.adjustingOpacity = true;
            return true;
        }
        if (this.contains(f, f2)) {
            if (pointerAction == PointerAction.LEFT_CLICK) {
                this.draggingWindow = true;
                return super.mouseClicked(f, f2, pointerAction);
            }
            return false;
        }
        this.close();
        return false;
    }

    @Override
    public void mouseReleased(float f, float f2, PointerAction pointerAction) {
        this.draggingWindow = false;
        this.adjustingHue = false;
        this.adjustingColor = false;
        this.adjustingOpacity = false;
        super.mouseReleased(f, f2, pointerAction);
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (Screen.isCopy((int)n)) {
            this.minecraftClient.keyboard.setClipboard(this.getColor().toHex());
            return true;
        }
        if (Screen.isPaste((int)n)) {
            try {
                this.setColor(ColorRGBA.fromHex(this.minecraftClient.keyboard.getClipboard()));
            }
            catch (Exception exception) {
                // empty catch block
            }
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }
}
