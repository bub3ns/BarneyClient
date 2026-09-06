/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Identifier
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.ui.text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.animation.Transition;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.input.DragMode;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.Alignment;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.layout.ItemGrid;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.utility.render.ColorRGBA;

public class TextComponent
extends UiNode {
    private Function<TextComponent, ColorRGBA> bg;
    private WidgetState radius = WidgetState.NONE;
    private float squircle = 0.0f;
    private Insets padding = Insets.NONE;
    private float borderWidth = 0.0f;
    private Function<TextComponent, ColorRGBA> border;
    private Supplier<String> text;
    private FontMetrics font;
    private Function<TextComponent, ColorRGBA> textColor;
    private Alignment textAlign = Alignment.START;
    private float textInset = 0.0f;
    private boolean textFade = false;
    private float marqueeOffset = 0.0f;
    private boolean marqueeForward = true;
    private float marqueeHold = 0.0f;
    private ColorRGBA shadowColor;
    private float shadowOffX;
    private float shadowOffY;
    private float shadowBlur;
    private String iconName;
    private float iconSize = 0.0f;
    private Function<TextComponent, ColorRGBA> iconColor;
    private Identifier image;
    private float imageSize = 0.0f;
    private float imageRadius = 0.0f;
    private Function<TextComponent, ColorRGBA> imageColor;
    private RenderHook painter;

    public TextComponent background(Function<TextComponent, ColorRGBA> function) {
        this.bg = function;
        return this;
    }

    public TextComponent background(ColorRGBA colorRGBA) {
        this.bg = textComponent -> colorRGBA;
        return this;
    }

    public TextComponent radius(float f) {
        this.radius = WidgetState.uniform(f);
        return this;
    }

    public TextComponent radius(WidgetState widgetState) {
        this.radius = widgetState == null ? WidgetState.NONE : widgetState;
        return this;
    }

    public TextComponent squircle(float f) {
        this.squircle = f;
        return this;
    }

    @Override
    protected float backdropRadius() {
        return this.radius.topLeftRadius();
    }

    @Override
    protected WidgetState shapeRadius() {
        return this.radius;
    }

    @Override
    protected float shapeSquircle() {
        return this.squircle;
    }

    @Override
    public TextComponent blur(float f) {
        super.blur(f);
        return this;
    }

    @Override
    public TextComponent blur(float f, ColorRGBA colorRGBA) {
        super.blur(f, colorRGBA);
        return this;
    }

    @Override
    public TextComponent glass() {
        super.glass();
        return this;
    }

    @Override
    public TextComponent glass(float f, boolean bl) {
        super.glass(f, bl);
        return this;
    }

    public TextComponent border(float f, Function<TextComponent, ColorRGBA> function) {
        this.borderWidth = f;
        this.border = function;
        return this;
    }

    public TextComponent border(float f, ColorRGBA colorRGBA) {
        this.borderWidth = f;
        this.border = textComponent -> colorRGBA;
        return this;
    }

    public TextComponent padding(Insets insets) {
        this.padding = insets == null ? Insets.NONE : insets;
        return this;
    }

    public TextComponent padding(float f) {
        this.padding = Insets.uniform(f);
        return this;
    }

    public TextComponent padding(float f, float f2) {
        this.padding = Insets.symmetric(f, f2);
        return this;
    }

    public TextComponent text(FontMetrics fontMetrics, String string, Function<TextComponent, ColorRGBA> function) {
        this.font = fontMetrics;
        this.text = () -> string;
        this.textColor = function;
        return this;
    }

    public TextComponent text(FontMetrics fontMetrics, Supplier<String> supplier, Function<TextComponent, ColorRGBA> function) {
        this.font = fontMetrics;
        this.text = supplier;
        this.textColor = function;
        return this;
    }

    public TextComponent text(FontMetrics fontMetrics, String string, ColorRGBA colorRGBA) {
        return this.text(fontMetrics, string, (TextComponent textComponent) -> colorRGBA);
    }

    public TextComponent textAlign(Alignment alignment) {
        this.textAlign = alignment;
        return this;
    }

    public TextComponent textInset(float f) {
        this.textInset = f;
        return this;
    }

    public TextComponent fade() {
        this.textFade = true;
        return this;
    }

    public TextComponent fade(boolean bl) {
        this.textFade = bl;
        return this;
    }

    public TextComponent textShadow(ColorRGBA colorRGBA, float f, float f2, float f3) {
        this.shadowColor = colorRGBA;
        this.shadowOffX = f;
        this.shadowOffY = f2;
        this.shadowBlur = f3;
        return this;
    }

    public TextComponent icon(String string, float f, Function<TextComponent, ColorRGBA> function) {
        this.iconName = string;
        this.iconSize = f;
        this.iconColor = function;
        return this;
    }

    public TextComponent icon(String string, float f, ColorRGBA colorRGBA) {
        return this.icon(string, f, (TextComponent textComponent) -> colorRGBA);
    }

    public TextComponent image(Identifier class_29602, float f, float f2, Function<TextComponent, ColorRGBA> function) {
        this.image = class_29602;
        this.imageSize = f;
        this.imageRadius = f2;
        this.imageColor = function;
        return this;
    }

    public TextComponent image(Identifier class_29602, float f, float f2, ColorRGBA colorRGBA) {
        return this.image(class_29602, f, f2, (TextComponent textComponent) -> colorRGBA);
    }

    public TextComponent paint(RenderHook renderHook) {
        this.painter = renderHook;
        return this;
    }

    @Override
    public TextComponent motion(Motion motion) {
        super.motion(motion);
        return this;
    }

    @Override
    public TextComponent width(float f) {
        super.width(f);
        return this;
    }

    @Override
    public TextComponent height(float f) {
        super.height(f);
        return this;
    }

    @Override
    public TextComponent size(float f, float f2) {
        super.size(f, f2);
        return this;
    }

    @Override
    public TextComponent minSize(float f, float f2) {
        super.minSize(f, f2);
        return this;
    }

    @Override
    public TextComponent maxSize(float f, float f2) {
        super.maxSize(f, f2);
        return this;
    }

    @Override
    public TextComponent minWidth(float f) {
        super.minWidth(f);
        return this;
    }

    @Override
    public TextComponent minHeight(float f) {
        super.minHeight(f);
        return this;
    }

    @Override
    public TextComponent fillWidth() {
        super.fillWidth();
        return this;
    }

    @Override
    public TextComponent fillHeight() {
        super.fillHeight();
        return this;
    }

    @Override
    public TextComponent fill() {
        super.fill();
        return this;
    }

    @Override
    public TextComponent at(float f, float f2) {
        super.at(f, f2);
        return this;
    }

    @Override
    public TextComponent enter(Transition transition) {
        super.enter(transition);
        return this;
    }

    @Override
    public TextComponent exit(Transition transition) {
        super.exit(transition);
        return this;
    }

    @Override
    public TextComponent transition(Transition transition) {
        super.transition(transition);
        return this;
    }

    @Override
    public TextComponent lifeMotion(Motion motion) {
        super.lifeMotion(motion);
        return this;
    }

    @Override
    public TextComponent onClick(Runnable runnable) {
        super.onClick(runnable);
        return this;
    }

    @Override
    public TextComponent onClick(Consumer<PointerAction> consumer) {
        super.onClick(consumer);
        return this;
    }

    @Override
    public TextComponent onClick(UiNode.PointerClickHandler pointerClickHandler) {
        super.onClick(pointerClickHandler);
        return this;
    }

    @Override
    public TextComponent interactive(boolean bl) {
        super.interactive(bl);
        return this;
    }

    @Override
    public TextComponent draggable(DragMode dragMode) {
        super.draggable(dragMode);
        return this;
    }

    @Override
    public TextComponent draggable(boolean bl) {
        super.draggable(bl);
        return this;
    }

    @Override
    public TextComponent cursor(Cursor cursor) {
        super.cursor(cursor);
        return this;
    }

    @Override
    public TextComponent hoverMotion(Motion motion) {
        super.hoverMotion(motion);
        return this;
    }

    @Override
    public TextComponent center() {
        super.center();
        return this;
    }

    @Override
    public TextComponent centerX() {
        super.centerX();
        return this;
    }

    @Override
    public TextComponent centerY() {
        super.centerY();
        return this;
    }

    @Override
    public TextComponent visibleWhen(BooleanSupplier booleanSupplier) {
        super.visibleWhen(booleanSupplier);
        return this;
    }

    @Override
    public TextComponent visibleWhen(BooleanSupplier booleanSupplier, Motion motion) {
        super.visibleWhen(booleanSupplier, motion);
        return this;
    }

    @Override
    public TextComponent visibleWhen(BooleanSupplier booleanSupplier, Easing easing, long l) {
        super.visibleWhen(booleanSupplier, easing, l);
        return this;
    }

    @Override
    public TextComponent snapPosition(BooleanSupplier booleanSupplier) {
        super.snapPosition(booleanSupplier);
        return this;
    }

    @Override
    public TextComponent snapPosition() {
        super.snapPosition();
        return this;
    }

    @Override
    public TextComponent animatePosition() {
        super.animatePosition();
        return this;
    }

    @Override
    public TextComponent sticky(BooleanSupplier booleanSupplier) {
        super.sticky(booleanSupplier);
        return this;
    }

    @Override
    public TextComponent sticky() {
        super.sticky();
        return this;
    }

    @Override
    public TextComponent collapse() {
        super.collapse();
        return this;
    }

    @Override
    public TextComponent collapse(boolean bl) {
        super.collapse(bl);
        return this;
    }

    @Override
    public TextComponent bind(String string, BooleanSupplier booleanSupplier) {
        super.bind(string, booleanSupplier);
        return this;
    }

    @Override
    public TextComponent bind(String string, UiNode.SignalValueProvider signalValueProvider) {
        super.bind(string, signalValueProvider);
        return this;
    }

    @Override
    public TextComponent bind(String string, BooleanSupplier booleanSupplier, Motion motion) {
        super.bind(string, booleanSupplier, motion);
        return this;
    }

    @Override
    public TextComponent bind(String string, UiNode.SignalValueProvider signalValueProvider, Motion motion) {
        super.bind(string, signalValueProvider, motion);
        return this;
    }

    @Override
    public TextComponent bind(String string, BooleanSupplier booleanSupplier, long l) {
        super.bind(string, booleanSupplier, l);
        return this;
    }

    @Override
    public TextComponent bind(String string, UiNode.SignalValueProvider signalValueProvider, long l) {
        super.bind(string, signalValueProvider, l);
        return this;
    }

    @Override
    public TextComponent signalMotion(String string, Motion motion) {
        super.signalMotion(string, motion);
        return this;
    }

    @Override
    protected void onTick(float f, float f2, float f3) {
        if (!this.textFade || this.font == null || this.text == null) {
            return;
        }
        String string = this.text.get();
        if (string == null || string.isEmpty()) {
            this.marqueeOffset = 0.0f;
            this.marqueeForward = true;
            this.marqueeHold = 0.0f;
            return;
        }
        float f4 = Math.max(1.0f, this.w.getCurrent() - this.padding.horizontalSize() - this.textInset * 2.0f);
        float f5 = Math.max(0.0f, this.font.measureText(string) - f4);
        if (f5 <= 0.0f) {
            this.marqueeOffset = 0.0f;
            this.marqueeForward = true;
            this.marqueeHold = 0.0f;
            return;
        }
        if (this.contains(f2, f3) && !ANY_DRAGGING) {
            this.marqueeOffset = Math.min(this.marqueeOffset, f5);
            if (this.marqueeHold > 0.0f) {
                this.marqueeHold -= f;
            } else {
                float f6 = f / 1000.0f * 35.0f;
                if (this.marqueeForward) {
                    this.marqueeOffset = Math.min(this.marqueeOffset + f6, f5);
                    if (this.marqueeOffset >= f5) {
                        this.marqueeForward = false;
                        this.marqueeHold = 600.0f;
                    }
                } else {
                    this.marqueeOffset = Math.max(this.marqueeOffset - f6, 0.0f);
                    if (this.marqueeOffset <= 0.0f) {
                        this.marqueeForward = true;
                        this.marqueeHold = 600.0f;
                    }
                }
            }
        } else if (this.marqueeOffset > 0.0f) {
            this.marqueeOffset = Math.max(0.0f, this.marqueeOffset - f / 1000.0f * 35.0f);
        }
    }

    @Override
    protected void measure() {
        float f;
        if (this.image != null && this.imageSize > 0.0f) {
            if (!this.explicitW) {
                this.prefW = this.imageSize + this.padding.horizontalSize();
            }
            if (!this.explicitH) {
                this.prefH = this.imageSize + this.padding.verticalSize();
            }
        }
        if (this.text == null || this.font == null) {
            return;
        }
        String string = this.text.get();
        float f2 = f = string == null || string.isEmpty() ? 0.0f : this.font.measureText(string);
        if (!this.explicitW) {
            this.prefW = (this.textFade ? 0.0f : f) + this.textInset * 2.0f + this.padding.horizontalSize();
        }
        if (!this.explicitH) {
            this.prefH = this.font.getFontTopOffset() + this.padding.verticalSize();
        }
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        float f2;
        Object object;
        ColorRGBA colorRGBA;
        float f3 = this.x.getCurrent();
        float f4 = this.y.getCurrent();
        float f5 = this.w.getCurrent();
        float f6 = this.h.getCurrent();
        if (this.bg != null && (colorRGBA = this.bg.apply(this)) != null && colorRGBA.getAlpha() > 0.0f) {
            if (this.squircle > 0.0f) {
                drawContext.drawSquircle(f3, f4, f5, f6, this.squircle, this.radius, colorRGBA);
            } else {
                drawContext.drawRoundedRect(f3, f4, f5, f6, this.radius, colorRGBA);
            }
        }
        if (this.border != null && this.borderWidth > 0.0f && (colorRGBA = this.border.apply(this)) != null && colorRGBA.getAlpha() > 0.0f) {
            if (this.squircle > 0.0f) {
                drawContext.drawSquircleBorder(f3, f4, f5, f6, this.borderWidth, this.squircle, this.radius, colorRGBA);
            } else {
                drawContext.drawRoundedBorder(f3, f4, f5, f6, this.borderWidth, this.radius, colorRGBA);
            }
        }
        float f7 = f3 + this.padding.left;
        float f8 = f4 + this.padding.top;
        float f9 = f5 - this.padding.horizontalSize();
        float f10 = f6 - this.padding.verticalSize();
        if (this.iconName != null && this.iconSize > 0.0f) {
            object = this.iconColor != null ? this.iconColor.apply(this) : ColorRGBA.WHITE;
            drawContext.drawIcon(this.iconName, f7 + this.textInset, f8 + f10 / 2.0f - this.iconSize / 2.0f, this.iconSize, (ColorRGBA)object);
        }
        if (this.image != null && this.imageSize > 0.0f) {
            Object object2 = object = this.imageColor != null ? this.imageColor.apply(this) : ColorRGBA.WHITE;
            if (object == null) {
                object = ColorRGBA.WHITE;
            }
            float f11 = f7 + f9 / 2.0f - this.imageSize / 2.0f;
            f2 = f8 + f10 / 2.0f - this.imageSize / 2.0f;
            if (this.imageRadius > 0.0f) {
                drawContext.drawRoundedTexture(this.image, f11, f2, this.imageSize, this.imageSize, WidgetState.uniform(this.imageRadius), (ColorRGBA)object);
            } else {
                drawContext.drawTexture(this.image, f11, f2, this.imageSize, this.imageSize, (ColorRGBA)object);
            }
        }
        if (this.text != null && this.font != null && (object = this.text.get()) != null && !((String)object).isEmpty()) {
            ColorRGBA colorRGBA2 = this.textColor != null ? this.textColor.apply(this) : ColorRGBA.WHITE;
            f2 = f8 + f10 / 2.0f - this.font.getFontTopOffset() / 2.0f;
            if (this.textFade) {
                boolean bl;
                float f12 = Math.max(1.0f, f9 - this.textInset * 2.0f);
                boolean bl2 = bl = this.marqueeOffset > 0.01f;
                if (bl) {
                    float f13 = Math.max(f6, this.font.getFontTopOffset() + 4.0f);
                    moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)(f7 + this.textInset - 3.0f), (float)(f4 - 3.0f), (float)(f12 + 6.0f), (float)(f13 + 6.0f));
                    drawContext.pushMatrix();
                    drawContext.getMatrices().translate(-this.marqueeOffset, 0.0f, 0.0f);
                }
                drawContext.drawFadeoutText(this.font, (String)object, f7 + this.textInset, f2, colorRGBA2, 0.95f, 1.0f, f12);
                if (bl) {
                    drawContext.popMatrix();
                    moscow.rockstar.render.state.UiScissorStack.pop();
                }
                if (this.painter != null) {
                    this.painter.render(drawContext, this);
                }
                return;
            }
            boolean bl = this.shadowColor != null && this.shadowColor.getAlpha() > 0.0f;
            switch (this.textAlign) {
                case CENTER: {
                    if (bl) {
                        drawContext.drawCenteredTextWithShadow(this.font, (String)object, f7 + f9 / 2.0f, f2, colorRGBA2, this.shadowColor, this.shadowOffX, this.shadowOffY, this.shadowBlur);
                        break;
                    }
                    drawContext.drawCenteredText(this.font, (String)object, f7 + f9 / 2.0f, f2, colorRGBA2);
                    break;
                }
                case END: {
                    drawContext.drawRightText(this.font, (String)object, f7 + f9 - this.textInset, f2, colorRGBA2);
                    break;
                }
                default: {
                    if (bl) {
                        drawContext.drawTextWithShadow(this.font, (String)object, f7 + this.textInset, f2, colorRGBA2, this.shadowColor, this.shadowOffX, this.shadowOffY, this.shadowBlur);
                        break;
                    }
                    drawContext.drawText(this.font, (String)object, f7 + this.textInset, f2, colorRGBA2);
                }
            }
        }
        if (this.painter != null) {
            this.painter.render(drawContext, this);
        }
    }

    public static interface RenderHook {
        public void render(RockstarDrawContext var1, TextComponent var2);
    }
}
