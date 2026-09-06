/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.settings;

import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.settings.Setting;
import moscow.rockstar.ui.animation.Animation;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.color.ColorPickerScreen;
import moscow.rockstar.ui.layout.ItemGrid;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.settings.SettingWidget;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.utility.render.ColorRGBA;

public abstract class SettingComponent<T extends Setting>
extends SettingWidget {
    private final SettingWidget settingWidget;
    protected final T setting;
    private final Animation widthAnimation = new Animation(300L, Easing.easeOutOvershoot);
    protected final Animation contentAnimation = new Animation(300L, Easing.easeInOutCubicBezier);
    /** Horizontal offset used only while scrolling an overlong label. */
    private float scrollOffset;
    private boolean scrollingForward = true;
    private long lastFrameTimeMillis = System.currentTimeMillis();
    private long nextScrollDirectionChangeMillis;
    /** Top of the viewport used when this component is rendered inside a scrollable picker. */
    protected float visibleRegionTop;
    /** Height of the viewport used when this component is rendered inside a scrollable picker. */
    protected float visibleRegionHeight;

    public SettingComponent(T t, SettingWidget settingWidget) {
        this.settingWidget = settingWidget;
        this.setting = t;
    }

    /**
     * ORIGINAL rockstar/ilIlil/IiiIiiIii#II (Lrockstar/ilIlil/III;)V — disassembled. When this row
     * lives inside the HUD right-click popup menu (obf IiIIiiIii, ported here as
     * {@link ColorPickerScreen}) and the mouse is over it, the setting's description is published
     * to the HUD registry, which draws it as the hover tooltip. Note the deliberate double
     * translate: translateOrBlank on the ".description" key, then translate on the result.
     */
    @Override
    public void renderOverlay(RockstarDrawContext drawContext) {
        String string = Localization.translateOrBlank(this.setting.getDescriptionKey());
        if (this.settingWidget instanceof ColorPickerScreen && this.contains(drawContext)) {
            RockstarClient.create().getHudElementRegistry().setHoveredTooltipKey(Localization.translate(string));
        }
        super.renderOverlay(drawContext);
    }

    @Override
    public void tick() {
        super.tick();
    }

    /**
     * Keep the setting widget's measured width as its public geometry.  The
     * width animation belongs to controls that use it for their value chip;
     * it is not the width of the setting row itself.
     */

    public void renderDivider(RockstarDrawContext drawContext) {
    }

    /** Second render pass: the right-aligned value overlay (original IiiIiiIii.Ii). */
    public void renderValue(RockstarDrawContext drawContext) {
    }

    protected void drawScrollableText(RockstarDrawContext drawContext, FontMetrics fontMetrics, String string, float f, float f2, float f3, ColorRGBA colorRGBA, float f4, float f5) {
        float f6;
        boolean bl;
        float f7 = Math.max(1.0f, f3);
        float f8 = fontMetrics.measureText(string);
        long l = System.currentTimeMillis();
        boolean bl2 = this.contains(drawContext.mouseX(), drawContext.mouseY());
        float f9 = Math.max(0.0f, f8 - f7);
        float f10 = (float)(l - this.lastFrameTimeMillis) / 1000.0f;
        this.lastFrameTimeMillis = l;
        boolean bl3 = bl = bl2 && f9 > 0.0f;
        if (f9 <= 0.0f) {
            this.scrollOffset = 0.0f;
        } else if (bl) {
            this.scrollOffset = Math.min(this.scrollOffset, f9);
            if (l >= this.nextScrollDirectionChangeMillis) {
                f6 = f10 * 35.0f;
                if (this.scrollingForward) {
                    this.scrollOffset = Math.min(this.scrollOffset + f6, f9);
                    if (this.scrollOffset >= f9) {
                        this.scrollingForward = false;
                        this.nextScrollDirectionChangeMillis = l + 600L;
                    }
                } else {
                    this.scrollOffset = Math.max(this.scrollOffset - f6, 0.0f);
                    if (this.scrollOffset <= 0.0f) {
                        this.scrollingForward = true;
                        this.nextScrollDirectionChangeMillis = l + 600L;
                    }
                }
            }
        } else if (this.scrollOffset > 0.0f) {
            this.scrollOffset = Math.max(0.0f, this.scrollOffset - f10 * 35.0f);
            if (this.scrollOffset == 0.0f) {
                this.scrollingForward = true;
                this.nextScrollDirectionChangeMillis = l;
            }
        }
        f6 = Math.max(this.height, fontMetrics.getFontTopOffset() + 4.0f);
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)(f - 3.0f), (float)(this.y - 3.0f), (float)(f7 + 6.0f), (float)(f6 + 6.0f));
        drawContext.pushMatrix();
        drawContext.getMatrices().translate(-this.scrollOffset, 0.0f, 0.0f);
        drawContext.drawFadeoutText(fontMetrics, string, f, f2, colorRGBA, 0.95f, f5, f7);
        drawContext.popMatrix();
        moscow.rockstar.render.state.UiScissorStack.pop();
    }

    @Generated
    public SettingWidget getSettingWidget() {
        return this.settingWidget;
    }

    @Generated
    public T getSetting() {
        return this.setting;
    }

    @Generated
    public Animation getWidthAnimation() {
        return this.widthAnimation;
    }

    @Generated
    public Animation getContentAnimation() {
        return this.contentAnimation;
    }

    @Override
    @Generated
    public float getX() {
        return this.x;
    }

    @Generated
    public boolean isScrollingForward() {
        return this.scrollingForward;
    }

    @Generated
    public long getLastFrameTimeMillis() {
        return this.lastFrameTimeMillis;
    }

    @Generated
    public long getDirectionChangeTimeMillis() {
        return this.nextScrollDirectionChangeMillis;
    }

    @Generated
    public float getVisibleRegionTop() {
        return this.visibleRegionTop;
    }

    @Generated
    public float getVisibleRegionHeight() {
        return this.visibleRegionHeight;
    }

    @Generated
    public void setVisibleRegionTop(float f) {
        this.visibleRegionTop = f;
    }

    @Generated
    public void setVisibleRegionHeight(float f) {
        this.visibleRegionHeight = f;
    }
}
