/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.core;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.animation.Motion;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.input.Cursor;
import moscow.rockstar.ui.text.TextComponent;
import pyrock.utility.render.ColorRGBA;

public class InteractiveComponent
extends TextComponent {
    private Function<InteractiveComponent, ColorRGBA> textProvider = interactiveComponent -> ColorPalette.ACCENT_COLOR;
    private Function<InteractiveComponent, ColorRGBA> baseColorProvider = interactiveComponent -> new ColorRGBA(78.0f, 74.0f, 90.0f);
    private Function<InteractiveComponent, ColorRGBA> hoverColorProvider = null;

    public InteractiveComponent(BooleanSupplier booleanSupplier) {
        this.size(13.0f, 8.0f);
        this.cursor(Cursor.HAND);
        this.bind("on", booleanSupplier, Motion.motion);
    }

    public InteractiveComponent setInactiveTextColor(ColorRGBA colorRGBA) {
        this.textProvider = interactiveComponent -> colorRGBA;
        return this;
    }

    public InteractiveComponent setInactiveTextColorProvider(Supplier<ColorRGBA> supplier) {
        this.textProvider = interactiveComponent -> (ColorRGBA)supplier.get();
        return this;
    }

    public InteractiveComponent setInactiveTextColorProvider(Function<InteractiveComponent, ColorRGBA> function) {
        this.textProvider = function;
        return this;
    }

    public InteractiveComponent setActiveColor(ColorRGBA colorRGBA) {
        this.baseColorProvider = interactiveComponent -> colorRGBA;
        return this;
    }

    public InteractiveComponent setActiveColorProvider(Supplier<ColorRGBA> supplier) {
        this.baseColorProvider = interactiveComponent -> (ColorRGBA)supplier.get();
        return this;
    }

    public InteractiveComponent setActiveColorProvider(Function<InteractiveComponent, ColorRGBA> function) {
        this.baseColorProvider = function;
        return this;
    }

    public InteractiveComponent setHoverColor(ColorRGBA colorRGBA) {
        this.hoverColorProvider = interactiveComponent -> colorRGBA;
        return this;
    }

    public InteractiveComponent setHoverColorProvider(Supplier<ColorRGBA> supplier) {
        this.hoverColorProvider = interactiveComponent -> (ColorRGBA)supplier.get();
        return this;
    }

    public InteractiveComponent setHoverColorProvider(Function<InteractiveComponent, ColorRGBA> function) {
        this.hoverColorProvider = function;
        return this;
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        float f2 = this.sig("on");
        float f3 = this.x();
        float f4 = this.y();
        float f5 = this.w();
        float f6 = this.h();
        float f7 = 1.5f;
        float f8 = f6 - f7 * 2.0f;
        ColorRGBA colorRGBA = this.textProvider.apply(this).mix(this.baseColorProvider.apply(this), 1.0f - f2);
        ColorRGBA colorRGBA2 = this.hoverColorProvider == null ? ColorPalette.blendWithContrastBackground(colorRGBA) : this.hoverColorProvider.apply(this);
        drawContext.drawRoundedRect(f3, f4, f5, f6, WidgetState.uniform(f6 / 2.0f - 0.5f), colorRGBA);
        drawContext.drawRoundedRect(f3 + f7 + (f5 - f8 - f7 * 2.0f) * f2, f4 + f7, f8, f8, WidgetState.uniform(f8 / 2.0f - 0.5f), colorRGBA2);
    }
}

