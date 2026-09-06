/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.ui.text;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.layout.ItemGrid;
import moscow.rockstar.ui.text.FontMetrics;
import moscow.rockstar.ui.text.TextComponent;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.utility.render.ColorRGBA;

public class ScrollingTextComponent
extends TextComponent {
    private final FontMetrics fontMetrics;
    private final Supplier<String> textSupplier;
    private Function<ScrollingTextComponent, ColorRGBA> colorProvider = scrollingTextComponent -> ColorRGBA.WHITE;
    private float leftFadeWidth = 8.0f;
    private float rightFadeWidth = 8.0f;
    private float scrollSpeed = 35.0f;
    private float pauseDurationMillis = 600.0f;
    private BooleanSupplier scrollEnabled;
    private boolean useFadeoutShader;
    private float scrollOffset;
    private boolean scrollingForward = true;
    private float pauseRemainingMillis;

    public ScrollingTextComponent(FontMetrics fontMetrics, String string) {
        this(fontMetrics, () -> string);
    }

    public ScrollingTextComponent(FontMetrics fontMetrics, Supplier<String> supplier) {
        this.fontMetrics = fontMetrics;
        this.textSupplier = supplier;
        this.setHeight(fontMetrics.getFontTopOffset());
    }

    public ScrollingTextComponent setColor(ColorRGBA colorRGBA) {
        this.colorProvider = scrollingTextComponent -> colorRGBA;
        return this;
    }

    public ScrollingTextComponent setColorSupplier(Supplier<ColorRGBA> supplier) {
        this.colorProvider = scrollingTextComponent -> (ColorRGBA)supplier.get();
        return this;
    }

    public ScrollingTextComponent setColorProvider(Function<ScrollingTextComponent, ColorRGBA> function) {
        this.colorProvider = function;
        return this;
    }

    public ScrollingTextComponent setFadeWidths(float f, float f2) {
        this.leftFadeWidth = f;
        this.rightFadeWidth = f2;
        return this;
    }

    public ScrollingTextComponent setLeftFadeWidth(float f) {
        this.leftFadeWidth = f;
        return this;
    }

    public ScrollingTextComponent setRightFadeWidth(float f) {
        this.rightFadeWidth = f;
        return this;
    }

    public ScrollingTextComponent setScrollSpeed(float f) {
        this.scrollSpeed = f;
        return this;
    }

    public ScrollingTextComponent setPauseDuration(float f) {
        this.pauseDurationMillis = f;
        return this;
    }

    public ScrollingTextComponent setScrollEnabled(BooleanSupplier booleanSupplier) {
        this.scrollEnabled = booleanSupplier;
        return this;
    }

    public ScrollingTextComponent alwaysScroll() {
        this.scrollEnabled = () -> true;
        return this;
    }

    public ScrollingTextComponent fadeOut() {
        this.useFadeoutShader = true;
        return this;
    }

    public ScrollingTextComponent setWidth(float f) {
        super.width(f);
        return this;
    }

    public ScrollingTextComponent setHeight(float f) {
        super.height(f);
        return this;
    }

    public ScrollingTextComponent setSize(float f, float f2) {
        super.size(f, f2);
        return this;
    }

    @Override
    public ScrollingTextComponent fillWidth() {
        super.fillWidth();
        return this;
    }

    @Override
    public ScrollingTextComponent fillHeight() {
        super.fillHeight();
        return this;
    }

    @Override
    protected void onTick(float f, float f2, float f3) {
        if (!this.inFlow()) {
            return;
        }
        String string = this.textSupplier.get();
        float f4 = Math.max(0.0f, (string == null ? 0.0f : this.fontMetrics.measureText(string)) - this.w());
        if (f4 <= 0.0f) {
            this.scrollOffset = 0.0f;
            this.scrollingForward = true;
            this.pauseRemainingMillis = 0.0f;
            return;
        }
        this.scrollOffset = Math.min(this.scrollOffset, f4);
        float f5 = this.scrollSpeed * f / 1000.0f;
        if (!this.isScrollActive(f2, f3)) {
            this.scrollingForward = true;
            this.pauseRemainingMillis = 0.0f;
            this.scrollOffset = Math.max(0.0f, this.scrollOffset - f5);
            return;
        }
        if (this.pauseRemainingMillis > 0.0f) {
            this.pauseRemainingMillis -= f;
            return;
        }
        if (this.scrollingForward) {
            this.scrollOffset = Math.min(this.scrollOffset + f5, f4);
            if (this.scrollOffset >= f4) {
                this.scrollingForward = false;
                this.pauseRemainingMillis = this.pauseDurationMillis;
            }
        } else {
            this.scrollOffset = Math.max(this.scrollOffset - f5, 0.0f);
            if (this.scrollOffset <= 0.0f) {
                this.scrollingForward = true;
                this.pauseRemainingMillis = this.pauseDurationMillis;
            }
        }
    }

    private boolean isScrollActive(float f, float f2) {
        if (this.scrollEnabled != null) {
            return this.scrollEnabled.getAsBoolean();
        }
        UiNode uiNode = this.parent();
        return (uiNode != null ? uiNode : this).contains(f, f2);
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        String string = this.textSupplier.get();
        if (string == null || string.isEmpty()) {
            return;
        }
        ColorRGBA colorRGBA = this.colorProvider.apply(this);
        float f2 = this.x();
        float f3 = this.y();
        float f4 = this.w();
        float f5 = f3 + this.h() / 2.0f - this.fontMetrics.getFontTopOffset() / 2.0f;
        float f6 = Math.max(0.0f, this.fontMetrics.measureText(string) - f4);
        if (this.useFadeoutShader) {
            UiNode uiNode = this.parent();
            float f7 = uiNode == null ? f3 - 3.0f : uiNode.y() - 3.0f;
            float f8 = Math.max(uiNode == null ? this.h() : uiNode.h(), this.fontMetrics.getFontTopOffset() + 4.0f) + 6.0f;
            moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)drawContext.getMatrices(), (float)(f2 - 3.0f), (float)f7, (float)(f4 + 6.0f), (float)f8);
            drawContext.pushMatrix();
            drawContext.getMatrices().translate(-this.scrollOffset, 0.0f, 0.0f);
            float f9 = Math.max(1.0f, f4 + this.scrollOffset);
            float f10 = (this.scrollOffset + f4 * 0.95f) / f9;
            drawContext.drawFadeoutText(this.fontMetrics, string, f2, f5, colorRGBA, f10, 1.0f, f9);
            drawContext.popMatrix();
            moscow.rockstar.render.state.UiScissorStack.pop();
            return;
        }
        if (f6 <= 0.0f) {
            drawContext.drawText(this.fontMetrics, string, f2, f5, colorRGBA);
            return;
        }
        float f11 = Math.min(this.leftFadeWidth, this.scrollOffset);
        float f12 = Math.min(this.rightFadeWidth, f6 - this.scrollOffset);
        MatrixStack class_45872 = drawContext.getMatrices();
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)class_45872, (float)f2, (float)(f3 - 5.0f), (float)f4, (float)(this.h() + 10.0f));
        class_45872.push();
        class_45872.translate(-this.scrollOffset + 0.5f, 0.0f, 0.0f);
        drawContext.drawFadeText(this.fontMetrics, string, f2, f5, colorRGBA, f11, f12, f4);
        class_45872.pop();
        moscow.rockstar.render.state.UiScissorStack.pop();
    }










}

