/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.widgets.settings;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.network.http.client.ReactorNettyClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.texture.TextureAnimationAtlas;
import moscow.rockstar.render.texture.TextureRegion;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.SettingComponent;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.settings.SettingWidget;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import ua.mintantileak.spk.Compile;

public class ModeSettingComponent
extends SettingComponent<ModeSetting> {
    private boolean scrollingForward;

    public ModeSettingComponent(ModeSetting modeSetting, SettingWidget settingWidget) {
        super(modeSetting, settingWidget);
    }

    @Override
    protected void renderContent(RockstarDrawContext drawContext) {
        if (!this.scrollingForward) {
            for (ModeSetting.Option option : ((ModeSetting)this.setting).getOptions()) {
                this.dispatchFromOption(option);
            }
            this.scrollingForward = true;
        }
        float f = this.x + 9.0f;
        float f2 = this.y + 1.0f;
        float f3 = this.width - 18.0f;
        FontMetrics fontMetrics = Font.REGULAR.metrics(8.0f);
        float f4 = 10.0f;
        float f5 = 19.0f;
        this.contentAnimation.setReverse(this.contains(drawContext.mouseX(), drawContext.mouseY()));
        this.drawScrollableText(drawContext, fontMetrics, Localization.translate(((ModeSetting)this.getSetting()).getName()), this.x + f4, f2 - 1.0f + UiUtils.center(fontMetrics.getFontTopOffset(), f5), this.getSettingWidget().getWidth() - f4, ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.contentAnimation.getValue())), 0.8f, 1.0f);
        drawContext.drawRoundedRect(f - 1.0f, f2 + 17.0f, f3 + 2.0f, 8.0f + this.readAmountCached(), WidgetState.uniform(6.0f), ColorPalette.getPanelColor().withAlpha(76.5f));
        float f6 = 0.0f;
        for (ModeSetting.Option option : ((ModeSetting)this.setting).getOptions()) {
            if (option.isHidden()) continue;
            this.dispatchFromOption(option);
            boolean bl = option.isSelected();
            if (bl != option.wasSelected()) {
                if (bl) {
                    option.setCurrentAnimation(option.getEnableAnimation());
                } else {
                    option.setCurrentAnimation(option.getDisableAnimation());
                }
                option.setLastSelected(bl);
            }
            boolean bl2 = UiUtils.contains((double)(f - 1.0f), (double)(f2 + 20.0f + f6), (double)(f3 + 2.0f), 12.0, drawContext.mouseX(), drawContext.mouseY());
            if (bl2 && (float)drawContext.mouseY() > this.getVisibleRegionTop() && (float)drawContext.mouseY() < this.getVisibleRegionTop() + this.getVisibleRegionHeight()) {
                moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
            }
            option.getHoverAnimation().setReverse(bl2);
            option.getActiveAnimation().setReverse(option.isSelected());
            drawContext.drawFadeoutText(Font.MEDIUM.metrics(7.0f), Localization.translate(option.getName()), f + 7.0f, f2 + 24.5f + f6, ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.75f + 0.25f * option.getHoverAnimation().getValue() + 0.25f * option.getActiveAnimation().getValue())), 0.8f, 1.0f, f3 - 12.0f - option.getActiveAnimation().getValue() * 10.0f);
            TextureRegion animationFrame = option.getCurrentAnimationFrame();
            if ((option.getActiveAnimation().getValue() > 0.0f || option.isAnimationPlaying()) && animationFrame != null) {
                ShaderRenderer.drawTextureRegion(drawContext.getMatrices(), animationFrame, f + f3 - 11.0f - option.getActiveAnimation().getValue() * 2.0f, f2 + 24.0f + f6, 6.0f, 6.0f, ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.1f + 0.9f * option.getActiveAnimation().getValue()));
            }
            f6 += 12.0f;
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
        if (pointerAction != PointerAction.LEFT_CLICK) {
            return;
        }
        float f = 0.0f;
        for (ModeSetting.Option option : ((ModeSetting)this.setting).getOptions()) {
            if (option.isHidden()) continue;
            boolean bl = UiUtils.contains((double)(this.x - 1.0f), (double)(this.y + 20.0f + f), (double)(this.width - 2.0f), 12.0, d, d2);
            if (bl) {
                option.select();
            }
            f += 12.0f;
        }
        super.mouseClicked(d, d2, pointerAction);
    }

    @Override
    public float getHeight() {
        this.height = 31.0f + this.readAmountCached();
        return this.height;
    }

    private float readAmountCached() {
        float f = 0.0f;
        for (ModeSetting.Option option : ((ModeSetting)this.setting).getOptions()) {
            if (option.isHidden()) continue;
            f += 12.0f;
        }
        return f;
    }

    private void dispatchFromOption(ModeSetting.Option option) {
        if (option.getEnableAnimation() != null && option.getDisableAnimation() != null && option.getCurrentAnimation() != null) {
            return;
        }
        option.setEnableAnimation(TextureAnimationAtlas.findAnimation(RockstarClient.resourceId("penises/check_enable.penis")));
        option.setDisableAnimation(TextureAnimationAtlas.findAnimation(RockstarClient.resourceId("penises/check_disable.penis")));
        option.setLastSelected(option.isSelected());
        option.setCurrentAnimation(option.wasSelected() ? option.getEnableAnimation() : option.getDisableAnimation());
        if (!option.wasSelected()) {
            option.parkCurrentAnimation();
        }
    }
}
