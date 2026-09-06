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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.modules.config.ModuleConfigurationStore;
import moscow.rockstar.modules.visuals.hud.Interface;
import moscow.rockstar.platform.WindowHandle;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.colors.GradientColors;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.state.UiScissorStack;
import moscow.rockstar.ui.animation.AnimatedColor;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.CursorManager;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.WindowMetricsProvider;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.settings.SettingWidget;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.theme.ColorTheme;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.Rect;
import ua.mintantileak.spk.Compile;

public class ColorPickerWidget
extends SettingWidget
implements WindowMetricsProvider,
WindowHandle {
    public static final List<SavedColor> SAVED_COLORS = new CopyOnWriteArrayList<SavedColor>(List.of(
            new SavedColor(new ColorRGBA(0.0f, 122.0f, 255.0f)),
            new SavedColor(new ColorRGBA(52.0f, 199.0f, 89.0f)),
            new SavedColor(new ColorRGBA(255.0f, 204.0f, 0.0f)),
            new SavedColor(new ColorRGBA(255.0f, 59.0f, 48.0f)),
            new SavedColor(new ColorRGBA(151.0f, 71.0f, 255.0f))));

    private final Animation appearAnimation = new Animation(300L, 0.0f, Easing.easeOutOvershootSoft);
    private final Animation contentRevealAnimation = new Animation(300L, 0.0f, Easing.easeInOutCubicBezier);
    protected final Animation windowAnimation = new Animation(300L, 0.0f, Easing.easeInOutCubicBezier);
    private final Animation sampleTooltipAnimation = new Animation(300L, 0.0f, Easing.easeOutOvershootSoft);
    private final AnimatedColor huePreviewColor = new AnimatedColor(300L);
    private final AnimatedColor displayedColor = new AnimatedColor(200L);
    private final Animation hueAnimation = new Animation(500L, Easing.easeOutOvershoot);
    private final Animation saturationAnimation = new Animation(500L, Easing.easeOutOvershoot);
    private final Animation brightnessAnimation = new Animation(500L, Easing.easeOutOvershoot);
    private final Animation opacityAnimation = new Animation(500L, Easing.easeOutOvershoot);
    private final String title;
    private final boolean showOpacity;
    private boolean open;
    private float scalePivot;
    private boolean draggingWindow;
    private boolean samplingScreen;
    private float dragOffsetX;
    private float dragOffsetY;
    private boolean adjustingHue;
    private boolean adjustingColor;
    private boolean adjustingOpacity;
    private float hue;
    private float saturation;
    private float brightness;
    private float opacity;

    public ColorPickerWidget(float x, float y, float scalePivot, boolean showOpacity, ColorRGBA colorRGBA, String title) {
        super(x, y, 143.0f, showOpacity ? 160.0f : 136.0f);
        this.scalePivot = scalePivot;
        this.showOpacity = showOpacity;
        this.open = true;
        this.displayedColor.setCurrentColor(colorRGBA);
        this.title = title;
        this.setColor(colorRGBA);
    }

    public static void setSavedColors(List<SavedColor> list) {
        SAVED_COLORS.clear();
        SAVED_COLORS.addAll(list);
    }

    @Override
    protected void renderContent(RockstarDrawContext drawContext) {
        if (this.adjustingHue) {
            this.hue = UiUtils.interpolateClamped(0.0f, 1.0f, this.y + 22.0f, 66.0f, drawContext.mouseY());
        }
        if (this.adjustingColor) {
            this.saturation = 1.0f - UiUtils.interpolateClamped(0.0f, 1.0f, this.x + 6.0f, 114.0f, drawContext.mouseX());
            this.brightness = 1.0f - UiUtils.interpolateClamped(0.0f, 1.0f, this.y + 20.0f, 70.0f, drawContext.mouseY());
        }
        if (this.adjustingOpacity) {
            this.opacity = UiUtils.interpolateClamped(0.0f, 1.0f, this.x + 7.0f, 88.0f, drawContext.mouseX());
        }
        if (this.draggingWindow) {
            this.x = (float)drawContext.mouseX() - this.dragOffsetX;
            this.y = (float)drawContext.mouseY() - this.dragOffsetY;
        }
        if (this.x + this.width + 5.0f > INSTANCE.width()) {
            this.x = INSTANCE.width() - this.width - 5.0f;
        }
        if (this.y + this.height + 5.0f > INSTANCE.height()) {
            this.y = INSTANCE.height() - this.height - 5.0f;
        }
        SAVED_COLORS.removeIf(saved -> saved.visibilityAnimation.getValue() == 0.0f && !saved.enabled);
        this.sampleTooltipAnimation.setEasing(this.samplingScreen ? Easing.easeOutBack : Easing.easeInBack);
        this.sampleTooltipAnimation.setReverse(this.samplingScreen);
        this.appearAnimation.setEasing(this.open ? Easing.easeOutBack : Easing.easeInBack);
        this.appearAnimation.setReverse(this.open);
        this.contentRevealAnimation.setReverse(this.appearAnimation.getValue() >= 0.6f);
        this.windowAnimation.setReverse(this.draggingWindow);
        this.hueAnimation.update(this.hue);
        this.saturationAnimation.update(1.0f - this.saturation);
        this.brightnessAnimation.update(1.0f - this.brightness);
        this.opacityAnimation.update(this.opacity);
        this.huePreviewColor.setTargetColor(ColorRGBA.fromHSB(this.hue, 1.0f, 1.0f));
        boolean bl = RockstarClient.create().getColorTheme() == ColorTheme.DARK;
        ColorRGBA colorRGBA = ColorPalette.getPanelColor().withAlpha(255.0f * (bl ? 0.9f - 0.6f * Interface.getLiquidGlassAlpha() : 0.7f));
        ColorRGBA colorRGBA2 = ColorRGBA.fromHSB(this.hue, this.saturation, this.brightness);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)Math.min(1.0f, this.appearAnimation.getValue()));
        ItemRenderUtils.translateAndScale(drawContext.getMatrices(), this.x + this.width / this.scalePivot, this.y + this.height / this.scalePivot, 0.5f + this.appearAnimation.getValue() * 0.5f);
        UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)(this.x + 1.0f), (float)(this.y + 1.0f), (float)(this.width - 2.0f), (float)(this.height - 2.0f));
        drawContext.drawShadow(this.x - 5.0f, this.y - 5.0f, this.width + 10.0f, this.height + 10.0f, 15.0f, WidgetState.uniform(6.0f), ColorRGBA.BLACK.withAlpha(255.0f * (0.1f + 0.15f * this.windowAnimation.getValue())));
        UiScissorStack.pop();
        if (Interface.isBlurEnabled()) {
            drawContext.drawBlurredRect(this.x, this.y, this.width, this.height, 45.0f, 7.0f, WidgetState.uniform(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.appearAnimation.getValue() * Interface.getBlurAlpha()));
        }
        if (Interface.isLiquidGlassEnabled()) {
            drawContext.drawLiquidGlass(this.x, this.y, this.width, this.height, 7.0f, 0.05f - 0.03f * this.windowAnimation.getValue(), WidgetState.uniform(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.appearAnimation.getValue() * Interface.getLiquidGlassAlpha()));
        }
        drawContext.drawSquircle(this.x, this.y, this.width, this.height, 7.0f, WidgetState.uniform(6.0f), colorRGBA);
        UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)this.x, (float)this.y, (float)this.width, (float)this.height);
        drawContext.drawCenteredText(Font.MEDIUM.metrics(7.0f), this.title, this.x + this.width / 2.0f, this.y + 7.0f, ColorPalette.getPrimaryTextColor());
        drawContext.drawIcon("colorpicker/pipette", this.x + 7.0f, this.y + 6.0f, 8.0f);
        if (UiUtils.contains((double)(this.x + 7.0f), (double)(this.y + 6.0f), 8.0, 8.0, drawContext.mouseX(), drawContext.mouseY())) {
            CursorManager.request(Cursor.HAND);
        }
        drawContext.drawRoundedRect(this.x + this.width - 15.0f, this.y + 5.0f, 10.0f, 10.0f, WidgetState.uniform(5.0f), ColorPalette.getPanelBackgroundColor());
        drawContext.drawIcon("xmark", this.x + this.width - 15.0f, this.y + 5.0f, 10.0f);
        if (UiUtils.contains((double)(this.x + this.width - 15.0f), (double)(this.y + 5.0f), 10.0, 10.0, drawContext.mouseX(), drawContext.mouseY())) {
            CursorManager.request(Cursor.HAND);
        }
        drawContext.drawRoundedTexture(RockstarClient.resourceId("textures/hue.png"), this.x + this.width - 18.0f, this.y + 20.0f, 12.0f, 70.0f, WidgetState.uniform(4.0f));
        drawContext.drawRoundedRect(this.x + this.width - 16.0f, this.y + 22.0f + 64.0f * this.hueAnimation.getValue(), 8.0f, 2.0f, WidgetState.uniform(0.2f), ColorPalette.WHITE);
        if (UiUtils.contains(this.x + this.width - 18.0f, this.y + 20.0f, 12.0, 70.0, drawContext) || this.adjustingHue) {
            CursorManager.request(Cursor.VERTICAL_RESIZE);
        }
        drawContext.drawRoundedRect(this.x + 6.0f, this.y + 20.0f, 114.0f, 70.0f, WidgetState.uniform(4.0f), new GradientColors(this.huePreviewColor.getColor(), ColorPalette.BLACK, ColorPalette.WHITE, ColorPalette.BLACK));
        drawContext.drawRoundedRect(this.x + 6.0f + 114.0f * this.saturationAnimation.getValue() - 3.5f, this.y + 20.0f + 70.0f * this.brightnessAnimation.getValue() - 3.5f, 7.0f, 7.0f, WidgetState.uniform(2.5f), ColorPalette.WHITE);
        drawContext.drawRoundedRect(this.x + 7.0f + 114.0f * this.saturationAnimation.getValue() - 3.5f, this.y + 21.0f + 70.0f * this.brightnessAnimation.getValue() - 3.5f, 5.0f, 5.0f, WidgetState.uniform(1.5f), colorRGBA2);
        if (UiUtils.contains(this.x + 6.0f, this.y + 20.0f, 114.0, 70.0, drawContext) || this.adjustingColor) {
            CursorManager.request(Cursor.CROSSHAIR);
        }
        if (this.showOpacity) {
            drawContext.drawText(Font.MEDIUM.metrics(5.0f), Localization.translate("colorpicker.opacity").toUpperCase(), this.x + 6.0f, this.y + 95.0f, ColorPalette.getPrimaryTextColor().withAlpha(191.25f));
            drawContext.drawRoundedTexture(RockstarClient.resourceId("textures/empty.png"), this.x + 6.0f, this.y + 102.0f, 100.0f, 12.0f, WidgetState.uniform(5.0f));
            drawContext.drawRoundedRect(this.x + 6.0f - 0.5f, this.y + 102.0f - 0.5f, 101.0f, 13.0f, WidgetState.uniform(5.0f), new GradientColors(colorRGBA2.withAlpha(0.0f), colorRGBA2));
            drawContext.drawRoundedRect(this.x + this.width - 32.0f, this.y + 102.0f, 26.0f, 12.0f, WidgetState.uniform(2.0f), ColorPalette.getPanelBackgroundColor().withAlpha(255.0f));
            drawContext.drawCenteredText(Font.MEDIUM.metrics(6.0f), (int)(this.opacity * 100.0f) + "%", this.x + this.width - 32.0f + 13.0f, this.y + 106.0f, ColorPalette.getPrimaryTextColor());
            drawContext.drawRoundedBorder(this.x + 7.0f + 88.0f * this.opacityAnimation.getValue(), this.y + 103.0f, 10.0f, 10.0f, 0.5f, WidgetState.uniform(4.0f), ColorPalette.WHITE);
            drawContext.drawRoundedRect(this.x + 8.0f + 88.0f * this.opacityAnimation.getValue(), this.y + 104.0f, 8.0f, 8.0f, WidgetState.uniform(3.0f), this.getColor());
            if (UiUtils.contains(this.x + 6.0f, this.y + 102.0f, 100.0, 12.0, drawContext) || this.adjustingOpacity) {
                CursorManager.request(Cursor.HORIZONTAL_RESIZE);
            }
        }
        drawContext.drawRoundedRect(this.x + 6.0f, this.y + this.height - 36.0f, 29.0f, 29.0f, WidgetState.uniform(5.0f), this.getColor());
        float f = 0.0f;
        float f2 = 0.0f;
        for (SavedColor saved : SAVED_COLORS) {
            saved.visibilityAnimation.setReverse(saved.enabled);
            saved.selectionAnimation.setReverse(saved.matches(this.hue, this.saturation, this.brightness));
            if (saved.selectionAnimation.getValue() > 0.0f) {
                float f3 = saved.selectionAnimation.getValue();
                drawContext.drawRoundedRect(this.x + 45.0f + f, this.y + this.height - 36.0f + f2, 11.0f, 11.0f, WidgetState.uniform(4.5f), saved.color.withAlpha(255.0f * saved.visibilityAnimation.getValue()));
                drawContext.drawRoundedBorder(this.x + 45.0f + f - 1.0f + 2.0f * f3, this.y + this.height - 36.0f + f2 - 1.0f + 2.0f * f3, 13.0f - 4.0f * f3, 13.0f - 4.0f * f3, 0.5f, WidgetState.uniform(6.5f - 2.0f * f3), ColorPalette.WHITE.withAlpha(255.0f * saved.visibilityAnimation.getValue() * saved.selectionAnimation.getValue()));
            } else {
                drawContext.drawRoundedRect(this.x + 45.0f + f, this.y + this.height - 36.0f + f2, 11.0f, 11.0f, WidgetState.uniform(4.5f), saved.color.withAlpha(255.0f * saved.visibilityAnimation.getValue()));
            }
            if (UiUtils.contains(this.x + 45.0f + f, this.y + this.height - 36.0f + f2, 11.0, 11.0, drawContext)) {
                CursorManager.request(Cursor.HAND);
            }
            if (!(45.0f + (f += 20.0f * saved.visibilityAnimation.getValue()) > this.width)) continue;
            f = 0.0f;
            f2 += 18.0f * saved.visibilityAnimation.getValue();
        }
        if (SAVED_COLORS.size() < 10) {
            drawContext.drawRoundedRect(this.x + 45.0f + f, this.y + this.height - 36.0f + f2, 11.0f, 11.0f, WidgetState.uniform(4.5f), ColorPalette.getPanelBackgroundColor());
            drawContext.drawIcon("plus", this.x + 45.0f + f, this.y + this.height - 36.0f + f2, 11.0f);
            if (UiUtils.contains(this.x + 45.0f + f, this.y + this.height - 36.0f + f2, 11.0, 11.0, drawContext)) {
                CursorManager.request(Cursor.HAND);
            }
        }
        UiScissorStack.pop();
        ItemRenderUtils.popMatrix(drawContext.getMatrices());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        if (this.sampleTooltipAnimation.getValue() > 0.0f) {
            Rect rect = new Rect(drawContext.mouseX(), drawContext.mouseY() + 10, 45.0f + Font.REGULAR.metrics(6.0f).measureText(Localization.translate("colorpicker.click_to_sample")), 30.0f);
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)Math.min(1.0f, this.sampleTooltipAnimation.getValue()));
            ItemRenderUtils.translateAndScale(drawContext.getMatrices(), rect.getX() + rect.getWidth() / 2.0f, rect.getY() + rect.getHeight() / 2.0f, 0.5f + this.sampleTooltipAnimation.getValue() * 0.5f);
            drawContext.drawBlurredRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), 45.0f, 7.0f, WidgetState.uniform(6.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.sampleTooltipAnimation.getValue()));
            drawContext.drawSquircle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), 7.0f, WidgetState.uniform(6.0f), ColorPalette.getPanelColor().withAlpha(255.0f * (bl ? 0.8f : 0.7f)));
            ColorRGBA sampled = ColorRGBA.fromPixel((float)((double)drawContext.mouseX() * INSTANCE.scaleFactor()), (float)((double)WINDOW.getHeight() - (double)drawContext.mouseY() * INSTANCE.scaleFactor()));
            drawContext.drawRoundedRect(rect.getX() + 5.0f, rect.getY() + 5.0f, rect.getHeight() - 10.0f, rect.getHeight() - 10.0f, WidgetState.uniform(5.0f), sampled);
            drawContext.drawIcon("colorpicker/click", rect.getX() + rect.getHeight(), rect.getY() + 16.0f, 6.0f);
            drawContext.drawText(Font.REGULAR.metrics(6.0f), String.format("RGB %s %s %s", (int)sampled.getRed(), (int)sampled.getGreen(), (int)sampled.getBlue()), rect.getX() + rect.getHeight(), rect.getY() + 8.0f, ColorPalette.getPrimaryTextColor());
            drawContext.drawText(Font.REGULAR.metrics(6.0f), Localization.translate("colorpicker.click_to_sample"), rect.getX() + rect.getHeight() + 8.0f, rect.getY() + 17.0f, ColorPalette.getPrimaryTextColor().withAlpha(200.0f));
            ItemRenderUtils.popMatrix(drawContext.getMatrices());
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
    }

    public ColorRGBA getColor() {
        this.displayedColor.setTargetColor(ColorRGBA.fromHSB(this.hue, this.saturation, this.brightness).withAlpha(this.showOpacity ? 255.0f * this.opacity : 255.0f));
        return this.displayedColor.getColor();
    }

    public void setColor(ColorRGBA colorRGBA) {
        this.hue = colorRGBA.getHue();
        this.saturation = colorRGBA.getBrightness();
        this.brightness = colorRGBA.getSaturation();
        this.opacity = colorRGBA.getAlpha() / 255.0f;
        this.displayedColor.setTargetColor(colorRGBA);
    }

    @Override
    public void keyPressed(int n, int n2, int n3) {
        if (Screen.isCopy((int)n)) {
            minecraftClient.keyboard.setClipboard(this.getColor().toHex());
        } else if (Screen.isPaste((int)n)) {
            String string = minecraftClient.keyboard.getClipboard();
            try {
                this.setColor(ColorRGBA.fromHex(string));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        super.keyPressed(n, n2, n3);
    }

    @Override
    @Compile(obfuscation=1)
    public void mouseClicked(double d, double d2, PointerAction pointerAction) {
        boolean bl = SAVED_COLORS.size() < 10;
        float f = 0.0f;
        float f2 = 0.0f;
        for (SavedColor saved : SAVED_COLORS) {
            if (UiUtils.contains((double)(this.x + 45.0f + f), (double)(this.y + this.height - 36.0f + f2), 11.0, 11.0, d, d2)) {
                if (pointerAction.getButtonCode() != 0) {
                    saved.enabled = false;
                    ModuleConfigurationStore.saveConfiguration();
                } else {
                    this.setColor(saved.color);
                }
                return;
            }
            if (saved.matches(this.hue, this.saturation, this.brightness)) {
                bl = false;
            }
            if (!(45.0f + (f += 20.0f) > this.width)) continue;
            f = 0.0f;
            f2 += 18.0f;
        }
        if (UiUtils.contains((double)(this.x + 45.0f + f), (double)(this.y + this.height - 36.0f + f2), 11.0, 11.0, d, d2) && bl) {
            SAVED_COLORS.add(new SavedColor(this.getColor()));
            ModuleConfigurationStore.saveConfiguration();
            return;
        }
        if (pointerAction.getButtonCode() != 0) {
            this.samplingScreen = false;
            return;
        }
        if (this.samplingScreen) {
            ColorRGBA sampled = ColorRGBA.fromPixel((float)(d * INSTANCE.scaleFactor()), (float)((double)WINDOW.getHeight() - d2 * INSTANCE.scaleFactor()));
            this.setColor(sampled);
            this.samplingScreen = false;
        }
        if (UiUtils.contains((double)(this.x + 7.0f), (double)(this.y + 6.0f), 8.0, 8.0, d, d2)) {
            this.samplingScreen = true;
            return;
        }
        if (UiUtils.contains((double)(this.x + this.width - 15.0f), (double)(this.y + 5.0f), 10.0, 10.0, d, d2)) {
            this.open = false;
            this.scalePivot = 2.0f;
            return;
        }
        if (UiUtils.contains((double)(this.x + this.width - 18.0f), (double)(this.y + 20.0f), 12.0, 70.0, d, d2)) {
            this.adjustingHue = true;
            return;
        }
        if (UiUtils.contains((double)(this.x + 6.0f), (double)(this.y + 20.0f), 114.0, 70.0, d, d2)) {
            this.adjustingColor = true;
            return;
        }
        if (UiUtils.contains((double)(this.x + 6.0f), (double)(this.y + 102.0f), 100.0, 12.0, d, d2)) {
            this.adjustingOpacity = true;
            return;
        }
        if (this.contains(d, d2)) {
            this.draggingWindow = true;
            this.dragOffsetX = (float)(d - (double)this.x);
            this.dragOffsetY = (float)(d2 - (double)this.y);
        }
    }

    @Override
    public void mouseReleased(double d, double d2, PointerAction pointerAction) {
        this.draggingWindow = false;
        this.adjustingColor = false;
        this.adjustingHue = false;
        this.adjustingOpacity = false;
    }

    public Animation getAppearAnimation() {
        return this.appearAnimation;
    }

    public boolean isOpen() {
        return this.open;
    }

    public void setOpen(boolean bl) {
        this.open = bl;
    }

    public boolean isDraggingWindow() {
        return this.draggingWindow;
    }

    public boolean isSamplingScreen() {
        return this.samplingScreen;
    }

    public static final class SavedColor {
        public final ColorRGBA color;
        public final Animation selectionAnimation = new Animation(300L, 0.0f, Easing.easeInOutCubicBezier);
        public final Animation visibilityAnimation = new Animation(300L, 0.0f, Easing.easeInOutCubicBezier);
        public boolean enabled = true;

        public SavedColor(ColorRGBA color) {
            this.color = color;
        }

        public boolean matches(float hue, float saturation, float brightness) {
            return this.color.getHue() == hue
                && this.color.getSaturation() == brightness
                && this.color.getBrightness() == saturation;
        }

        public ColorRGBA getColor() {
            return this.color;
        }

        public Animation getSelectionAnimation() {
            return this.selectionAnimation;
        }

        public Animation getVisibilityAnimation() {
            return this.visibilityAnimation;
        }

        public boolean isEnabled() {
            return this.enabled;
        }
    }
}
