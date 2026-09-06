/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 */
package moscow.rockstar.ui.widgets.controls;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Function;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.animation.AnimatedValue;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiUtils;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.ScrollMode;
import pyrock.utility.render.ColorRGBA;

public class ScrollBar {
    private final Component component;
    private float thickness = 3.0f;
    private float offset = 1.5f;
    private float paddingBefore = 0.0f;
    private float paddingAfter = 0.0f;
    private float minThumbLength = 16.0f;
    private float cornerRadius = -1.0f;
    private float hideDelay = 900.0f;
    private ScrollMode mode = ScrollMode.AUTO;
    private Function<ScrollBar, ColorRGBA> trackColor = scrollBar -> ColorRGBA.WHITE.withAlpha(0.0f);
    private Function<ScrollBar, ColorRGBA> thumbColor = scrollBar -> ColorRGBA.WHITE.withAlpha(255.0f * (0.32f + 0.28f * scrollBar.hoverProgress() + 0.3f * scrollBar.dragProgress()));
    private Renderer renderer;
    private final AnimatedValue visibilityAnimation = new AnimatedValue(0.0f, Motion.resolveMotionMotionFromLongAndEasing(220L, Easing.easeOutQuart));
    private final AnimatedValue hoverAnimation = new AnimatedValue(0.0f, Motion.motion5);
    private float hideTimer = 0.0f;
    private boolean dragging = false;
    private float dragOffset = 0.0f;
    private boolean vertical = true;
    private float thumbX;
    private float thumbY;
    private float thumbWidth;
    private float thumbHeight;
    private float trackX;
    private float trackY;
    private float trackWidth;
    private float trackHeight;
    private float trackLength;
    private float thumbLength;
    private float thumbOffset;

    public ScrollBar(Component component) {
        this.component = component;
    }

    public ScrollBar mode(ScrollMode scrollMode) {
        this.mode = scrollMode == null ? ScrollMode.AUTO : scrollMode;
        return this;
    }

    public ScrollBar thickness(float f) {
        this.thickness = Math.max(1.0f, f);
        return this;
    }

    public ScrollBar offset(float f) {
        this.offset = f;
        return this;
    }

    public ScrollBar padding(float f) {
        this.paddingBefore = this.paddingAfter = f;
        return this;
    }

    public ScrollBar padding(float f, float f2) {
        this.paddingBefore = f;
        this.paddingAfter = f2;
        return this;
    }

    public ScrollBar minThumbLength(float f) {
        this.minThumbLength = Math.max(4.0f, f);
        return this;
    }

    public ScrollBar cornerRadius(float f) {
        this.cornerRadius = f;
        return this;
    }

    public ScrollBar hideDelay(float f) {
        this.hideDelay = Math.max(0.0f, f);
        return this;
    }

    public ScrollBar trackColor(Function<ScrollBar, ColorRGBA> function) {
        this.trackColor = function;
        return this;
    }

    public ScrollBar trackColor(ColorRGBA colorRGBA) {
        this.trackColor = scrollBar -> colorRGBA;
        return this;
    }

    public ScrollBar thumbColor(Function<ScrollBar, ColorRGBA> function) {
        this.thumbColor = function;
        return this;
    }

    public ScrollBar thumbColor(ColorRGBA colorRGBA) {
        this.thumbColor = scrollBar -> colorRGBA;
        return this;
    }

    public ScrollBar renderer(Renderer renderer) {
        this.renderer = renderer;
        return this;
    }

    public float hoverProgress() {
        return this.hoverAnimation.getCurrent();
    }

    public float dragProgress() {
        return this.dragging ? 1.0f : 0.0f;
    }

    public float visibilityProgress() {
        return this.visibilityAnimation.getCurrent();
    }

    public boolean isVertical() {
        return this.vertical;
    }

    public float effectiveCornerRadius() {
        return this.cornerRadius < 0.0f ? this.thickness / 2.0f - 0.5f : this.cornerRadius;
    }

    public float trackX() {
        return this.trackX;
    }

    public float trackY() {
        return this.trackY;
    }

    public float trackWidth() {
        return this.trackWidth;
    }

    public float trackHeight() {
        return this.trackHeight;
    }

    public float thumbX() {
        return this.thumbX;
    }

    public float thumbY() {
        return this.thumbY;
    }

    public float thumbWidth() {
        return this.thumbWidth;
    }

    public float thumbHeight() {
        return this.thumbHeight;
    }

    public ScrollMode getMode() {
        return this.mode;
    }

    public void show() {
        this.hideTimer = this.hideDelay;
    }

    public void tick(float f, float f2, float f3) {
        boolean bl;
        boolean bl2 = bl = this.component.isScrollOverflowing() && this.mode != ScrollMode.NEVER;
        if (bl) {
            this.updateGeometry();
        }
        boolean bl3 = bl && this.contains(f2, f3, this.trackX, this.trackY, this.trackWidth, this.trackHeight);
        boolean bl4 = bl && this.isMouseOver(f2, f3);
        this.hoverAnimation.setTarget(bl3 ? 1.0f : 0.0f);
        if (this.hideTimer > 0.0f) {
            this.hideTimer = Math.max(0.0f, this.hideTimer - f);
        }
        if (this.dragging && !UiUtils.isMouseButtonDown(0)) {
            this.dragging = false;
        }
        if (this.dragging) {
            this.dragTo(f2, f3);
        }
        boolean bl5 = this.mode == ScrollMode.ALWAYS || this.hideTimer > 0.0f || bl4 || this.dragging;
        this.visibilityAnimation.setTarget(bl && bl5 ? 1.0f : 0.0f);
        this.visibilityAnimation.update(f);
        this.hoverAnimation.update(f);
    }

    private void updateGeometry() {
        this.vertical = this.component.getLayout().isVertical();
        float f = this.component.contentX();
        float f2 = this.component.contentY();
        float f3 = this.component.viewportWidth();
        float f4 = this.component.viewportHeight();
        float f5 = this.component.stickySpace();
        float f6 = Math.max(0.0f, this.component.viewportSize() - f5);
        float f7 = Math.max(0.0f, this.component.measuredHeight() - f5);
        float f8 = this.component.maxScrollOffset();
        this.trackLength = Math.max(0.0f, f6 - this.paddingBefore - this.paddingAfter);
        float f9 = this.thumbLength = f7 > 0.0f ? Math.max(this.minThumbLength, this.trackLength * (f6 / f7)) : this.trackLength;
        if (this.thumbLength > this.trackLength) {
            this.thumbLength = this.trackLength;
        }
        float f10 = this.trackLength - this.thumbLength;
        float f11 = f8 > 0.0f ? ScrollBar.clamp(this.component.scrollOffset() / f8, 0.0f, 1.0f) : 0.0f;
        this.thumbOffset = f11 * f10;
        if (this.vertical) {
            this.thumbWidth = this.thickness;
            this.thumbHeight = this.trackLength;
            this.thumbX = f + f3 - this.thickness - this.offset;
            this.thumbY = f2 + f5 + this.paddingBefore;
            this.trackX = this.thumbX;
            this.trackY = this.thumbY + this.thumbOffset;
            this.trackWidth = this.thickness;
            this.trackHeight = this.thumbLength;
        } else {
            this.thumbWidth = this.trackLength;
            this.thumbHeight = this.thickness;
            this.thumbX = f + f5 + this.paddingBefore;
            this.thumbY = f2 + f4 - this.thickness - this.offset;
            this.trackX = this.thumbX + this.thumbOffset;
            this.trackY = this.thumbY;
            this.trackWidth = this.thumbLength;
            this.trackHeight = this.thickness;
        }
    }

    private void dragTo(float f, float f2) {
        float f3 = this.trackLength - this.thumbLength;
        float f4 = (this.vertical ? f2 - this.thumbY : f - this.thumbX) - this.dragOffset;
        f4 = ScrollBar.clamp(f4, 0.0f, f3);
        float f5 = f3 > 0.0f ? f4 / f3 : 0.0f;
        this.component.scrollTo(f5 * this.component.maxScrollOffset());
        this.hideTimer = this.hideDelay;
    }

    public void render(RockstarDrawContext drawContext, float f) {
        if (this.mode == ScrollMode.NEVER || !this.component.isScrollOverflowing()) {
            return;
        }
        float f2 = this.visibilityAnimation.getCurrent();
        if (f2 <= 0.01f) {
            return;
        }
        this.updateGeometry();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(f * f2));
        if (this.renderer != null) {
            this.renderer.render(drawContext, this);
        } else {
            ColorRGBA colorRGBA;
            float f3 = this.effectiveCornerRadius();
            ColorRGBA colorRGBA2 = this.trackColor.apply(this);
            if (colorRGBA2 != null && colorRGBA2.getAlpha() > 0.0f) {
                drawContext.drawRoundedRect(this.thumbX, this.thumbY, this.thumbWidth, this.thumbHeight, WidgetState.uniform(f3), colorRGBA2);
            }
            if ((colorRGBA = this.thumbColor.apply(this)) != null && colorRGBA.getAlpha() > 0.0f) {
                drawContext.drawRoundedRect(this.trackX, this.trackY, this.trackWidth, this.trackHeight, WidgetState.uniform(f3), colorRGBA);
            }
        }
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)f);
    }

    public boolean handleClick(float f, float f2, boolean bl) {
        if (!bl || this.mode == ScrollMode.NEVER || !this.component.isScrollOverflowing()) {
            return false;
        }
        if (this.visibilityAnimation.getCurrent() < 0.05f && !this.isMouseOver(f, f2)) {
            return false;
        }
        this.updateGeometry();
        if (this.contains(f, f2, this.trackX, this.trackY, this.trackWidth, this.trackHeight)) {
            this.dragging = true;
            this.dragOffset = this.vertical ? f2 - this.trackY : f - this.trackX;
            this.hideTimer = this.hideDelay;
            return true;
        }
        if (this.contains(f, f2, this.thumbX, this.thumbY, this.thumbWidth, this.thumbHeight)) {
            float f3 = this.vertical ? f2 - this.thumbY : f - this.thumbX;
            float f4 = ScrollBar.clamp(f3 / Math.max(1.0f, this.trackLength), 0.0f, 1.0f);
            this.component.scrollTo(f4 * this.component.maxScrollOffset());
            this.hideTimer = this.hideDelay;
            return true;
        }
        return false;
    }

    public void hide() {
        this.dragging = false;
    }

    private boolean contains(float f, float f2, float f3, float f4, float f5, float f6) {
        return f >= f3 && f <= f3 + f5 && f2 >= f4 && f2 <= f4 + f6;
    }

    private boolean isMouseOver(float f, float f2) {
        float f3 = this.thickness + this.offset + 6.0f;
        if (this.vertical) {
            float f4 = Math.max(this.thumbX + this.thumbWidth, this.thumbX + this.thumbWidth + this.offset);
            return f >= Math.min(this.thumbX, this.thumbX - f3) && f <= f4 && f2 >= this.thumbY && f2 <= this.thumbY + this.trackLength;
        }
        float f5 = Math.max(this.thumbY + this.thumbHeight, this.thumbY + this.thumbHeight + this.offset);
        return f2 >= Math.min(this.thumbY, this.thumbY - f3) && f2 <= f5 && f >= this.thumbX && f <= this.thumbX + this.trackLength;
    }

    private static float clamp(float f, float f2, float f3) {
        return Math.max(f2, Math.min(f3, f));
    }

    public static interface Renderer {
        public void render(RockstarDrawContext var1, ScrollBar var2);
    }
}
