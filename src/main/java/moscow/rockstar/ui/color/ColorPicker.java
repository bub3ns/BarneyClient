/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.ui.color;

import java.util.function.Supplier;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.core.WidgetState;
import pyrock.utility.render.ColorRGBA;

public class ColorPicker
extends UiNode {
    private final Supplier<ColorRGBA> colorProvider;
    private float padding = 2.0f;

    public ColorPicker(Supplier<ColorRGBA> supplier) {
        this.colorProvider = supplier;
        this.setSize(10.0f, 10.0f);
    }

    public ColorPicker setPadding(float f) {
        this.padding = f;
        return this;
    }

    public ColorPicker setWidth(float f) {
        super.width(f);
        return this;
    }

    public ColorPicker setHeight(float f) {
        super.height(f);
        return this;
    }

    public ColorPicker setSize(float f, float f2) {
        super.size(f, f2);
        return this;
    }

    @Override
    protected void drawSelf(RockstarDrawContext drawContext, float f) {
        float f2 = this.x();
        float f3 = this.y();
        float f4 = this.w();
        float f5 = this.h();
        drawContext.drawRoundedRect(f2, f3, f4, f5, WidgetState.uniform(f4 / 2.0f), ColorPalette.BORDER_COLOR);
        float f6 = f2 + this.padding;
        float f7 = f3 + this.padding;
        float f8 = f4 - this.padding * 2.0f;
        float f9 = f5 - this.padding * 2.0f;
        ColorRGBA colorRGBA = this.colorProvider.get();
        if (f8 > 0.0f && f9 > 0.0f && colorRGBA != null) {
            drawContext.drawRoundedRect(f6, f7, f8, f9, WidgetState.uniform(f8 / 2.0f), colorRGBA);
        }
    }



}

