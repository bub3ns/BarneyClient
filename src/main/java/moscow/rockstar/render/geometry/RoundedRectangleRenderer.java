/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.BufferRenderer
 *  net.minecraft.VertexFormats
 *  net.minecraft.BuiltBuffer
 *  org.joml.Matrix4f
 */
package moscow.rockstar.render.geometry;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.render.shaders.ShaderProgramBase;
import moscow.rockstar.render.shaders.ShaderRenderer;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.Matrix4f;

public class RoundedRectangleRenderer
extends VertexQuadBatch {
    private final ShaderProgramBase renderProcessor = ShaderRenderer.getRectangleShader();
    private final float DEFAULT_SMOOTHNESS;
    private final float cornerSmoothness;

    public RoundedRectangleRenderer(float f) {
        super(VertexFormats.POSITION_COLOR);
        this.DEFAULT_SMOOTHNESS = 0.5f;
        this.cornerSmoothness = f;
    }

    public void beginRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        this.renderProcessor.bindShaderProgram();
        this.renderProcessor.getUniform("Smoothness").set(this.DEFAULT_SMOOTHNESS);
        this.renderProcessor.getUniform("CornerSmoothness").set(this.cornerSmoothness);
        BuiltBuffer class_98012 = this.getBuffer().endNullable();
        if (class_98012 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
        }
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        this.resetBuffer();
    }

    public void drawRoundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int n) {
        this.renderProcessor.getUniform("Size").set(f3, f4);
        this.renderProcessor.getUniform("Radius").set(f5, f6, f7, f8);
        float f9 = -this.DEFAULT_SMOOTHNESS / 2.0f + this.DEFAULT_SMOOTHNESS * 2.0f;
        float f10 = this.DEFAULT_SMOOTHNESS / 2.0f + this.DEFAULT_SMOOTHNESS;
        float f11 = f - f9 / 2.0f;
        float f12 = f2 - f10 / 2.0f;
        float f13 = f3 + f9;
        float f14 = f4 + f10;
        this.getBuffer().vertex(matrix4f, f11, f12, 0.0f).color(n);
        this.getBuffer().vertex(matrix4f, f11, f12 + f14, 0.0f).color(n);
        this.getBuffer().vertex(matrix4f, f11 + f13, f12 + f14, 0.0f).color(n);
        this.getBuffer().vertex(matrix4f, f11 + f13, f12, 0.0f).color(n);
    }
}
