package moscow.rockstar.ui.widgets.settings;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.render.core.TextureRenderContext;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.texture.TextureAnimationAtlas;
import moscow.rockstar.render.texture.TextureRegion;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.SettingComponent;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.settings.SettingWidget;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.util.Timer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.utility.render.ColorRGBA;

/** Drop-down list of check rows for a multi-select setting. */
public class MultiBooleanSettingComponent
extends SettingComponent<MultiBooleanSetting> {
    private RollingCountBadge countBadge;
    private MultiBooleanSetting.Option draggedOption;
    private final Map<MultiBooleanSetting.Option, MarqueeState> marqueeStates = new HashMap<MultiBooleanSetting.Option, MarqueeState>();
    private final Timer reorderTimer = new Timer();
    private boolean spritesSeeded;

    public MultiBooleanSettingComponent(MultiBooleanSetting multiBooleanSetting, SettingWidget settingWidget) {
        super(multiBooleanSetting, settingWidget);
        ArrayList<MultiBooleanSetting.Option> arrayList = new ArrayList<MultiBooleanSetting.Option>();
        multiBooleanSetting.getOptions().forEach(option -> {
            if (option.isSelected()) {
                arrayList.add(option);
            }
        });
        multiBooleanSetting.getSelectedOptions().clear();
        multiBooleanSetting.getSelectedOptions().addAll(arrayList);
    }

    @Override
    protected void renderContent(RockstarDrawContext drawContext) {
        if (!this.spritesSeeded) {
            for (MultiBooleanSetting.Option option : this.setting.getOptions()) {
                option.setEnableAnimation(TextureAnimationAtlas.findAnimation(RockstarClient.resourceId("penises/check_enable.penis")));
                option.setDisableAnimation(TextureAnimationAtlas.findAnimation(RockstarClient.resourceId("penises/check_disable.penis")));
                option.setLastSelected(option.isSelected());
                option.setCurrentAnimation(option.wasSelected() ? option.getEnableAnimation() : option.getDisableAnimation());
                if (option.wasSelected()) continue;
                option.parkCurrentAnimation();
            }
            this.spritesSeeded = true;
        }
        float f = this.x + 9.0f;
        float f2 = this.y + 1.0f;
        float f3 = this.width - 18.0f;
        FontMetrics fontMetrics = Font.REGULAR.metrics(8.0f);
        float f4 = 10.0f;
        float f5 = Font.REGULAR.metrics(7.0f).getFontTopOffset();
        float f6 = 19.0f;
        this.contentAnimation.setReverse(this.contains(drawContext.mouseX(), drawContext.mouseY()));
        long l = this.setting.getOptions().stream().filter(option -> !option.isHidden()).count();
        int n = Math.toIntExact(this.setting.getSelectedOptions().stream().filter(option -> !option.isHidden()).count());
        String string = String.format(" %s", Localization.translate("setting_of") + " " + l);
        if (this.countBadge == null) {
            this.countBadge = new RollingCountBadge(Font.MEDIUM.metrics(7.0f), 5.0f, 500L, Easing.easeOutBack);
        }
        this.drawScrollableText(drawContext, fontMetrics, Localization.translate(this.setting.getName()), this.x + f4, f2 - 1.0f + UiUtils.center(fontMetrics.getFontTopOffset(), f6), this.getSettingWidget().getWidth() - f4 - Font.REGULAR.metrics(7.0f).measureText(string) - this.countBadge.getWidth() - 10.0f, ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.contentAnimation.getValue())), 0.8f, 1.0f);
        this.countBadge.configure(false, ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.contentAnimation.getValue())));
        this.countBadge.setValue(n);
        this.countBadge.setPosition(f + f3 - Font.REGULAR.metrics(7.0f).measureText(string) - this.countBadge.getWidth(), f2 - 1.0f + UiUtils.center(f5, f6));
        this.countBadge.render(drawContext);
        drawContext.drawRoundedRect(f - 1.0f, f2 + 17.0f, f3 + 2.0f, 8.0f + this.visibleRowsHeight(), WidgetState.uniform(6.0f), ColorPalette.getPanelColor().withAlpha(76.5f));
        float f7 = 0.0f;
        for (MultiBooleanSetting.Option option : this.setting.getOptions()) {
            if (option.isHidden()) continue;
            boolean bl = option.isSelected();
            if (bl != option.wasSelected()) {
                if (bl) {
                    option.setCurrentAnimation(option.getEnableAnimation());
                } else {
                    option.setCurrentAnimation(option.getDisableAnimation());
                }
                option.setLastSelected(bl);
            }
            float f8 = this.draggedOption == option ? Math.clamp((float)(drawContext.mouseY() - 2), f2 + 18.0f, f2 + 20.0f + this.visibleRowsHeight()) : f2 + 24.0f + f7;
            boolean bl2 = UiUtils.contains((double)(f - 1.0f), (double)(f8 - 4.0f), (double)(f3 + 2.0f), 12.0, drawContext.mouseX(), drawContext.mouseY());
            option.getPositionAnimation().setEasing(Easing.easeOutBackSoft);
            option.getPositionAnimation().update(f8 - f2);
            option.setLayoutY(f8);
            if (bl2 && this.draggedOption != option && !option.isAlwaysEnabled() && (float)drawContext.mouseY() > this.getVisibleRegionTop() && (float)drawContext.mouseY() < this.getVisibleRegionTop() + this.getVisibleRegionHeight()) {
                moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.HAND);
            }
            option.getHoverAnimation().setReverse(bl2);
            option.getActiveAnimation().setReverse(option.isSelected());
            if ((UiUtils.contains((double)f, (double)(f8 - 2.0f), 17.0, 10.0, drawContext) || option == this.draggedOption) && this.setting.isPreserveOrder()) {
                moscow.rockstar.ui.input.CursorManager.request((Cursor)Cursor.VERTICAL_RESIZE);
            }
            this.drawOptionText(drawContext, option, Font.MEDIUM.metrics(7.0f), Localization.translate(option.getName()), f + (float)(this.setting.isPreserveOrder() ? 18 : 7), f2 + option.getPositionAnimation().getValue() + 0.5f, f3 - (float)(this.setting.isPreserveOrder() ? 30 : 19) - option.getActiveAnimation().getValue() * 9.0f, ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.75f + 0.25f * option.getHoverAnimation().getValue() + 0.25f * option.getActiveAnimation().getValue())), bl2);
            TextureRegion animationFrame = option.getCurrentAnimationFrame();
            if ((option.getActiveAnimation().getValue() > 0.0f || option.isAnimationPlaying()) && animationFrame != null) {
                ShaderRenderer.drawTextureRegion(drawContext.getMatrices(), animationFrame, f + f3 - 11.0f - option.getActiveAnimation().getValue() * 2.0f, f2 + option.getPositionAnimation().getValue(), 6.0f, 6.0f, ColorPalette.PRIMARY_TEXT_COLOR.mulAlpha(0.1f + 0.9f * option.getActiveAnimation().getValue()));
            }
            f7 += 12.0f;
        }
        if (this.setting.isPreserveOrder()) {
            TextureRenderContext textureRenderContext = new TextureRenderContext(VertexFormats.POSITION_TEXTURE_COLOR, drawContext.getMatrices());
            for (MultiBooleanSetting.Option option : this.setting.getOptions()) {
                if (option.isHidden()) continue;
                drawContext.drawIcon("hud/drag", f + 7.0f, f2 + option.getPositionAnimation().getValue(), 6.0f, ColorPalette.getPrimaryTextColor());
            }
            textureRenderContext.render();
        }
        if (this.draggedOption != null && this.reorderTimer.hasElapsed(100L) && this.setting.isPreserveOrder()) {
            this.setting.notifyChange();
            this.setting.getOptions().sort(Comparator.comparingDouble(MultiBooleanSetting.Option::getLayoutY));
            this.reorderTimer.reset();
        }
    }

    @Override
    public void renderValue(RockstarDrawContext drawContext) {
        if (this.countBadge == null) {
            return;
        }
        float f = this.x + 9.0f;
        float f2 = this.y + 1.0f;
        float f3 = this.width - 18.0f;
        float f4 = Font.REGULAR.metrics(7.0f).getFontTopOffset();
        float f5 = 19.0f;
        long l = this.setting.getOptions().stream().filter(option -> !option.isHidden()).count();
        String string = String.format(" %s", Localization.translate("setting_of") + " " + l);
        drawContext.drawRightText(Font.REGULAR.metrics(7.0f), string, f + f3, f2 - 1.0f + UiUtils.center(f4, f5), ColorPalette.getPrimaryTextColor().withAlpha(255.0f * (0.75f + 0.25f * this.contentAnimation.getValue()) * RenderSystem.getShaderColor()[3]));
    }

    private void drawOptionText(RockstarDrawContext drawContext, MultiBooleanSetting.Option option, FontMetrics fontMetrics, String string, float f, float f2, float f3, ColorRGBA colorRGBA, boolean bl) {
        float f4 = Math.max(1.0f, f3);
        float f5 = fontMetrics.measureText(string);
        long l = System.currentTimeMillis();
        MarqueeState marqueeState = this.marqueeStates.computeIfAbsent(option, key -> new MarqueeState());
        float f6 = Math.max(0.0f, f5 - f4);
        float f7 = (float)(l - marqueeState.lastFrameTimeMillis) / 1000.0f;
        marqueeState.lastFrameTimeMillis = l;
        if (f6 <= 0.0f) {
            marqueeState.reset(l);
        } else if (bl) {
            marqueeState.offset = Math.min(marqueeState.offset, f6);
            if (l >= marqueeState.nextDirectionChangeMillis) {
                float f8 = f7 * 35.0f;
                if (marqueeState.forward) {
                    marqueeState.offset = Math.min(marqueeState.offset + f8, f6);
                    if (marqueeState.offset >= f6) {
                        marqueeState.forward = false;
                        marqueeState.nextDirectionChangeMillis = l + 600L;
                    }
                } else {
                    marqueeState.offset = Math.max(marqueeState.offset - f8, 0.0f);
                    if (marqueeState.offset <= 0.0f) {
                        marqueeState.forward = true;
                        marqueeState.nextDirectionChangeMillis = l + 600L;
                    }
                }
            }
        } else if (marqueeState.offset > 0.0f) {
            marqueeState.offset = Math.max(0.0f, marqueeState.offset - f7 * 35.0f);
            if (marqueeState.offset == 0.0f) {
                marqueeState.forward = true;
                marqueeState.nextDirectionChangeMillis = l;
            }
        }
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)(f - 2.0f), (float)(f2 - 2.0f), (float)(f4 + 4.0f), (float)(fontMetrics.getFontTopOffset() + 4.0f));
        drawContext.pushMatrix();
        drawContext.getMatrices().translate(-marqueeState.offset, 0.0f, 0.0f);
        drawContext.drawFadeoutText(fontMetrics, string, f, f2, colorRGBA, bl && f6 > 0.0f ? 0.98f : 0.8f, 1.0f, f4 + marqueeState.offset);
        drawContext.popMatrix();
        moscow.rockstar.render.state.UiScissorStack.pop();
    }

    @Override
    public void renderDivider(RockstarDrawContext drawContext) {
        float f = 0.5f;
        drawContext.drawRect(this.x, this.y + this.height, this.width, f, ColorPalette.getPrimaryTextColor().withAlpha(5.1f));
    }

    @Override
    public void mouseClicked(double d, double d2, PointerAction pointerAction) {
        if (pointerAction != PointerAction.LEFT_CLICK) {
            return;
        }
        float f = this.x + 9.0f;
        float f2 = this.y + 1.0f;
        float f3 = 0.0f;
        for (MultiBooleanSetting.Option option : this.setting.getOptions()) {
            if (option.isHidden()) continue;
            boolean bl = UiUtils.contains((double)(f - 1.0f), (double)(f2 + 20.0f + f3), (double)(this.width - 2.0f), 12.0, d, d2);
            if (UiUtils.contains((double)f, (double)(f2 + 22.0f + f3), 17.0, 10.0, d, d2) && this.setting.isPreserveOrder()) {
                this.draggedOption = option;
            } else if (bl) {
                option.toggle();
            }
            f3 += 12.0f;
        }
        super.mouseClicked(d, d2, pointerAction);
    }

    @Override
    public void mouseReleased(double d, double d2, PointerAction pointerAction) {
        this.draggedOption = null;
        super.mouseReleased(d, d2, pointerAction);
    }

    @Override
    public float getHeight() {
        this.height = 31.0f + this.visibleRowsHeight();
        return this.height;
    }

    private float visibleRowsHeight() {
        float f = 0.0f;
        for (MultiBooleanSetting.Option option : this.setting.getOptions()) {
            if (option.isHidden()) continue;
            f += 12.0f;
        }
        return f;
    }

    /** Per-option marquee state (original rockstar/ilIlil/IiiiIIiii$I). */
    static class MarqueeState {
        float offset;
        boolean forward = true;
        long lastFrameTimeMillis = System.currentTimeMillis();
        long nextDirectionChangeMillis;

        MarqueeState() {
        }

        void reset(long l) {
            this.offset = 0.0f;
            this.forward = true;
            this.nextDirectionChangeMillis = l;
        }
    }
}
