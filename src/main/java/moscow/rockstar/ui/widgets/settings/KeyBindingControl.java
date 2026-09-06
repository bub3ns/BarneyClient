/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.MatrixStack
 */
package moscow.rockstar.ui.widgets.settings;

import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.animation.AnimatedValue;
import moscow.rockstar.ui.animation.Easing;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.KeyBindingUtil;
import moscow.rockstar.ui.input.KeyDisplayFormatter;
import moscow.rockstar.ui.input.PointerAction;
import moscow.rockstar.ui.layout.ItemGrid;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.client.util.math.MatrixStack;
import pyrock.utility.render.ColorRGBA;

public class KeyBindingControl
extends UiNode {
    private final FontMetrics font;
    private final IntSupplier keyCodeSupplier;
    private final IntConsumer keyCodeConsumer;
    private boolean capturing;
    private int lastKeyCode = -1;
    private static KeyBindingControl activeControl;
    private final AnimatedValue pressAnimation = new AnimatedValue(1.0f, Motion.resolveMotionMotionFromLongAndEasing(380L, Easing.easeOutQuart));
    private final AnimatedValue focusAnimation = new AnimatedValue(0.0f, Motion.motion5);
    private final AnimatedValue widthAnimation = new AnimatedValue(Motion.resolveMotionMotionFromLongAndEasing(300L, Easing.easeOutQuart));
    private Function<KeyBindingControl, ColorRGBA> colorProvider = keyBindingControl -> ColorPalette.MUTED_PANEL_COLOR;
    private ColorRGBA backgroundColor = ColorPalette.PRIMARY_TEXT_COLOR;
    private ColorRGBA foregroundColor = ColorPalette.ACCENT_COLOR;
    private float cornerRadius = 3.0f;
    private float horizontalPadding = 4.0f;
    private float verticalPadding = 8.0f;
    private IntFunction<String> keyNameProvider = KeyDisplayFormatter::formatKey;
    private String placeholderText;

    public KeyBindingControl(FontMetrics fontMetrics, IntSupplier intSupplier, IntConsumer intConsumer) {
        this.font = fontMetrics;
        this.keyCodeSupplier = intSupplier;
        this.keyCodeConsumer = intConsumer;
        this.setHeight(11.0f);
        this.snapSize();
        this.widthAnimation.snapTo(this.calculatePreferredWidth());
    }

    public KeyBindingControl withColor(ColorRGBA colorRGBA) {
        this.colorProvider = keyBindingControl -> colorRGBA;
        return this;
    }

    public KeyBindingControl withColorProvider(Function<KeyBindingControl, ColorRGBA> function) {
        this.colorProvider = function;
        return this;
    }

    public KeyBindingControl withBackgroundColor(ColorRGBA colorRGBA) {
        this.backgroundColor = colorRGBA;
        return this;
    }

    public KeyBindingControl withForegroundColor(ColorRGBA colorRGBA) {
        this.foregroundColor = colorRGBA;
        return this;
    }

    public KeyBindingControl withCornerRadius(float f) {
        this.cornerRadius = f;
        return this;
    }

    public KeyBindingControl withHorizontalPadding(float f) {
        this.horizontalPadding = f;
        return this;
    }

    public KeyBindingControl withVerticalPadding(float f) {
        this.verticalPadding = f;
        return this;
    }

    public KeyBindingControl withKeyNameProvider(IntFunction<String> intFunction) {
        if (intFunction != null) {
            this.keyNameProvider = intFunction;
        }
        return this;
    }

    public KeyBindingControl withPlaceholder(String string) {
        this.placeholderText = string;
        return this;
    }

    public KeyBindingControl withPressMotion(Motion motion) {
        if (motion != null) {
            this.pressAnimation.motion(motion);
        }
        return this;
    }

    public KeyBindingControl withWidthMotion(Motion motion) {
        if (motion != null) {
            this.widthAnimation.motion(motion);
        }
        return this;
    }

    public boolean isCapturing() {
        return this.capturing;
    }

    public static boolean isControlActive() {
        return activeControl != null && KeyBindingControl.activeControl.capturing;
    }

    public void cancelCapture() {
        this.capturing = false;
        if (activeControl == this) {
            activeControl = null;
        }
    }

    public boolean capturePointerAction(PointerAction pointerAction) {
        if (!this.capturing || pointerAction == null) {
            return false;
        }
        this.acceptKeyCode(KeyBindingUtil.withCurrentModifiers(pointerAction.getButtonCode()));
        return true;
    }

    public static boolean handlePointerAction(PointerAction pointerAction) {
        KeyBindingControl keyBindingControl = activeControl;
        if (keyBindingControl == null) {
            return false;
        }
        if (!keyBindingControl.capturing) {
            activeControl = null;
            return false;
        }
        return keyBindingControl.capturePointerAction(pointerAction);
    }

    public static void cancelActiveControl() {
        if (activeControl != null) {
            activeControl.cancelCapture();
        }
    }

    public KeyBindingControl setHeight(float f) {
        super.height(f);
        return this;
    }

    public KeyBindingControl setWidth(float f) {
        super.width(f);
        return this;
    }

    public KeyBindingControl setSize(float f, float f2) {
        super.size(f, f2);
        return this;
    }

    public void handlePointerClick(PointerAction pointerAction) {
        if (!this.capturing && pointerAction == PointerAction.LEFT_CLICK) {
            this.capturing = true;
            activeControl = this;
        } else if (this.capturing) {
            this.acceptKeyCode(KeyBindingUtil.withCurrentModifiers(pointerAction.getButtonCode()));
        }
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (!this.capturing) {
            return false;
        }
        if (n == 256 || n == 261) {
            this.acceptKeyCode(-1);
            return true;
        }
        int n4 = KeyBindingUtil.encodeKeyPress(n, n3);
        if (n4 == Integer.MIN_VALUE) {
            return true;
        }
        this.acceptKeyCode(n4);
        return true;
    }

    @Override
    public boolean keyReleased(int n, int n2, int n3) {
        if (!this.capturing) {
            return false;
        }
        int n4 = KeyBindingUtil.encodeKeyRelease(n, n3);
        if (n4 == Integer.MIN_VALUE) {
            return false;
        }
        this.acceptKeyCode(n4);
        return true;
    }

    private void acceptKeyCode(int n) {
        this.lastKeyCode = this.keyCodeSupplier.getAsInt();
        this.keyCodeConsumer.accept(n);
        this.pressAnimation.snapTo(0.0f);
        this.pressAnimation.setTarget(1.0f);
        this.capturing = false;
        if (activeControl == this) {
            activeControl = null;
        }
    }

    @Override
    protected void measure() {
        if (!this.explicitW) {
            this.prefW = this.widthAnimation.getCurrent();
        }
    }

    @Override
    protected void onTick(float f, float f2, float f3) {
        this.widthAnimation.setTarget(this.calculatePreferredWidth());
        this.widthAnimation.update(f);
        this.focusAnimation.setTarget(this.capturing ? 1.0f : 0.0f);
        this.focusAnimation.update(f);
        this.pressAnimation.update(f);
    }

    private float calculatePreferredWidth() {
        return this.font.measureText(this.getDisplayedKeyName()) + this.horizontalPadding * 2.0f;
    }

    private String getDisplayedKeyName() {
        if (this.capturing) {
            int n = KeyBindingUtil.currentModifiers();
            String string = n != 0 ? "..." : (this.placeholderText != null ? this.placeholderText : this.keyNameProvider.apply(this.keyCodeSupplier.getAsInt()));
            return KeyBindingUtil.modifierPrefix(n) + (string == null ? "" : string);
        }
        String string = this.keyNameProvider.apply(this.keyCodeSupplier.getAsInt());
        return string == null ? "" : string;
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        String string;
        float f2 = this.x();
        float f3 = this.y();
        float f4 = this.w();
        float f5 = this.h();
        ColorRGBA colorRGBA = this.colorProvider.apply(this);
        if (colorRGBA != null && colorRGBA.getAlpha() > 0.0f) {
            drawContext.drawRoundedRect(f2, f3, f4, f5, WidgetState.uniform(this.cornerRadius), colorRGBA);
        }
        float f6 = this.pressAnimation.getCurrent();
        float f7 = f3 + f5 / 2.0f - this.font.getFontTopOffset() / 2.0f;
        float f8 = f2 + this.horizontalPadding;
        ColorRGBA colorRGBA2 = this.backgroundColor.mix(this.foregroundColor, this.focusAnimation.getCurrent()).mulAlpha(0.75f + 0.25f * Math.max(this.hover(), this.focusAnimation.getCurrent()));
        MatrixStack class_45872 = drawContext.getMatrices();
        moscow.rockstar.render.state.UiScissorStack.push((MatrixStack)class_45872, (float)f2, (float)f3, (float)f4, (float)f5);
        drawContext.drawText(this.font, this.getDisplayedKeyName(), f8 + (f6 - 1.0f) * this.w(), f7, colorRGBA2.mulAlpha(f6));
        if (f6 < 0.999f && (string = this.keyNameProvider.apply(this.lastKeyCode)) != null) {
            drawContext.drawText(this.font, string, f8 + f6 * this.w(), f7, colorRGBA2.mulAlpha(1.0f - f6));
        }
        moscow.rockstar.render.state.UiScissorStack.pop();
    }



}
