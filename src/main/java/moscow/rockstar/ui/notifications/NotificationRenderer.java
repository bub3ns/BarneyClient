/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.VertexFormats
 */
package moscow.rockstar.ui.notifications;

import lombok.Generated;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.modules.visuals.hud.Interface;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.FontRenderContext;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.notifications.TimedNotification;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

public abstract class NotificationRenderer
extends TimedNotification {
    protected final String message;
    protected String highlightedText;
    protected ColorRGBA highlightColor;

    protected NotificationRenderer(String string, String string2, ColorRGBA colorRGBA) {
        super(2500L);
        this.message = string;
        this.highlightedText = string2;
        this.highlightColor = colorRGBA;
    }

    protected abstract void renderContent(CustomDrawContext var1, float var2, float var3, float var4);

    @Override
    public float getHeight() {
        return 25.0f;
    }

    @Override
    public final void render(CustomDrawContext customDrawContext, float f) {
        FontMetrics fontMetrics = Font.MEDIUM.metrics(7.0f);
        float f2 = fontMetrics.measureText(this.message) + 26.0f;
        float f3 = 20.0f;
        float f4 = (this.getHeight() - f3) / 2.0f;
        this.slideAnimation.setEasing(Easing.easeOutOvershootSoft);
        this.slideAnimation.setDuration(300L);
        float f5 = (float)customDrawContext.getScaledWindowWidth() / 2.0f - f2 / 2.0f;
        float f6 = (float)customDrawContext.getScaledWindowHeight() - 87.0f - this.slideAnimation.update(f) + f4;
        float f7 = this.fadeAnimation.getValue();
        int n = (int)(255.0f * f7);
        ItemRenderUtils.translateAndScale(customDrawContext.getMatrices(), f5 + f2 / 2.0f, f6 + 10.0f, 0.5f + 0.5f * f7);
        if (Interface.isLiquidGlassEnabled()) {
            customDrawContext.drawLiquidGlass(f5, f6, f2, 20.0f, 7.0f, 0.08f, WidgetState.uniform(7.0f), ColorRGBA.WHITE.withAlpha(255.0f * f7 * Interface.getLiquidGlassAlpha()));
            customDrawContext.drawSquircle(f5, f6, f2, 20.0f, 7.0f, WidgetState.uniform(7.0f), ColorPalette.getPanelColor().withAlpha(255.0f * MathUtils.interpolateDouble(ColorPalette.getThemeColorSettings().getOverlayAlphaMinimum(), ColorPalette.getThemeColorSettings().getOverlayAlphaMaximum(), Interface.getLiquidGlassAlpha()) * f7));
        } else {
            customDrawContext.drawBlurredRect(f5, f6, f2, 20.0f, 45.0f, 7.0f, WidgetState.uniform(7.0f), ColorRGBA.WHITE.withAlpha(255.0f * f7 * Interface.getBlurAlpha()));
            customDrawContext.drawSquircle(f5, f6, f2, 20.0f, 7.0f, WidgetState.uniform(7.0f), new ColorRGBA(0.0f, 0.0f, 0.0f).withAlpha((int)(140.25f * f7)));
        }
        this.renderContent(customDrawContext, f5 + 5.0f, f6 + 5.0f, f7);
        float f8 = f5 + 20.0f;
        float f9 = f6 + (20.0f - fontMetrics.getFontTopOffset()) / 2.0f;
        FontRenderContext textBatch = new FontRenderContext(net.minecraft.client.render.VertexFormats.POSITION_TEXTURE_COLOR, customDrawContext.getMatrices());
        ColorRGBA colorRGBA = ColorRGBA.WHITE.withAlpha(n);
        if (this.highlightedText != null && this.highlightColor != null && this.message.contains(this.highlightedText)) {
            int n2 = this.message.indexOf(this.highlightedText);
            String string = this.message.substring(0, n2);
            String string2 = this.message.substring(n2 + this.highlightedText.length());
            float f10 = f8;
            if (!string.isEmpty()) {
                customDrawContext.drawText(fontMetrics, string, f10, f9, colorRGBA);
                f10 += fontMetrics.measureText(string);
            }
            customDrawContext.drawText(fontMetrics, this.highlightedText, f10, f9, this.highlightColor.withAlpha(n));
            f10 += fontMetrics.measureText(this.highlightedText);
            if (!string2.isEmpty()) {
                customDrawContext.drawText(fontMetrics, string2, f10, f9, colorRGBA);
            }
        } else {
            customDrawContext.drawText(fontMetrics, this.message, f8, f9, colorRGBA);
        }
        textBatch.render();
        ItemRenderUtils.popMatrix(customDrawContext.getMatrices());
    }

    @Generated
    public String getMessage() {
        return this.message;
    }

    @Generated
    public String getHighlightedText() {
        return this.highlightedText;
    }

    @Generated
    public ColorRGBA getHighlightColor() {
        return this.highlightColor;
    }
}
