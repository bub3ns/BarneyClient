/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.ui.notifications;

import lombok.Generated;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.modules.visuals.hud.Interface;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.notifications.NotificationType;
import moscow.rockstar.ui.notifications.TimedNotification;
import moscow.rockstar.ui.text.Font;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

public class NotificationToast
extends TimedNotification {
    private final NotificationType notificationType;
    private final String title;
    private final String subtitle;

    public NotificationToast(NotificationType notificationType, String string, String string2) {
        super(2500L);
        this.notificationType = notificationType;
        this.title = string;
        this.subtitle = string2;
    }

    @Override
    public final void render(CustomDrawContext customDrawContext, float f) {
        float f2 = Math.max(Font.BOLD.metrics(7.0f).measureText(this.title), Font.MEDIUM.metrics(6.0f).measureText(this.subtitle));
        float f3 = f2 + 32.0f;
        this.slideAnimation.setEasing(Easing.easeOutOvershootSoft);
        this.slideAnimation.setDuration(300L);
        float f4 = (float)customDrawContext.getScaledWindowWidth() / 2.0f - f3 / 2.0f;
        float f5 = (float)customDrawContext.getScaledWindowHeight() - 90.0f - this.slideAnimation.update(f);
        float f6 = 26.0f;
        int n = (int)(255.0f * this.fadeAnimation.getValue());
        ItemRenderUtils.translateAndScale(customDrawContext.getMatrices(), f4 + f3 / 2.0f, f5 + 12.0f + f6 / 2.0f, 0.5f + 0.5f * this.fadeAnimation.getValue());
        if (Interface.isLiquidGlassEnabled()) {
            customDrawContext.drawLiquidGlass(f4, f5, f3, f6, 7.0f, 0.08f, WidgetState.uniform(7.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.fadeAnimation.getValue() * Interface.getLiquidGlassAlpha()));
            customDrawContext.drawSquircle(f4, f5, f3, f6, 7.0f, WidgetState.uniform(7.0f), ColorPalette.getPanelColor().withAlpha(255.0f * MathUtils.interpolateDouble(ColorPalette.getThemeColorSettings().getOverlayAlphaMinimum(), ColorPalette.getThemeColorSettings().getOverlayAlphaMaximum(), Interface.getLiquidGlassAlpha()) * this.fadeAnimation.getValue()));
        } else {
            customDrawContext.drawBlurredRect(f4, f5, f3, f6, 45.0f, 7.0f, WidgetState.uniform(7.0f), ColorRGBA.WHITE.withAlpha(255.0f * this.fadeAnimation.getValue() * Interface.getBlurAlpha()));
            customDrawContext.drawSquircle(f4, f5, f3, f6, 7.0f, WidgetState.uniform(7.0f), new ColorRGBA(0.0f, 0.0f, 0.0f).withAlpha((int)(140.25f * this.fadeAnimation.getValue())));
            customDrawContext.drawRoundedRect(f4 + f6 / 2.0f - 9.0f, f5 + f6 / 2.0f - 9.0f, 18.0f, 18.0f, WidgetState.uniform(4.0f), new ColorRGBA(0.0f, 0.0f, 0.0f).withAlpha((int)(51.0f * this.fadeAnimation.getValue())));
        }
        customDrawContext.drawIcon(this.notificationType.getKey(), f4 + f6 / 2.0f - 5.0f, f5 + f6 / 2.0f - 5.0f, 10.0f, this.notificationType.getAccentColor().withAlpha((float)n * 0.8f));
        customDrawContext.drawText(Font.BOLD.metrics(7.0f), this.title, f4 + 27.0f, f5 + 7.0f, ColorRGBA.WHITE.withAlpha(n));
        customDrawContext.drawText(Font.MEDIUM.metrics(6.0f), this.subtitle, f4 + 27.0f, f5 + 15.0f, ColorRGBA.WHITE.withAlpha(n));
        ItemRenderUtils.popMatrix(customDrawContext.getMatrices());
    }

    @Generated
    public NotificationType getNotificationType() {
        return this.notificationType;
    }

    @Generated
    public String getTitle() {
        return this.title;
    }

    @Generated
    public String getSubtitle() {
        return this.subtitle;
    }
}
