/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.DrawContext
 */
package moscow.rockstar.render.core;

import lombok.Generated;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.ui.core.WidgetState;
import net.minecraft.client.gui.DrawContext;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.CustomDrawContext;

public class RockstarDrawContext
extends CustomDrawContext {
    private final int mouseX;
    private final int mouseY;
    private final float tickDelta;

    protected RockstarDrawContext(DrawContext originalContext, int n, int n2, float f) {
        super(originalContext);
        this.mouseX = n;
        this.mouseY = n2;
        this.tickDelta = f;
    }

    public static RockstarDrawContext create(DrawContext originalContext, int n, int n2, float f) {
        return new RockstarDrawContext(originalContext, n, n2, f);
    }

    public void drawShader(int n, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, WidgetState widgetState, ColorRGBA colorRGBA) {
        ShaderRenderer.drawBackdropBlur(this.getMatrices(), n, f, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, widgetState, colorRGBA);
    }

    @Generated
    public int mouseX() {
        return this.mouseX;
    }

    @Generated
    public int mouseY() {
        return this.mouseY;
    }

    @Generated
    public float tickDelta() {
        return this.tickDelta;
    }
}
