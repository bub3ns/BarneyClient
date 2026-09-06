/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tessellator
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.VertexConsumer
 *  net.minecraft.ShaderProgram
 *  net.minecraft.BuiltBuffer
 *  org.joml.Matrix4f
 */
package moscow.rockstar.render.text;

import com.mojang.blaze3d.systems.RenderSystem;
import moscow.rockstar.render.shaders.ShaderRenderer;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.Matrix4f;

public final class TextBatchRenderer {
    private static BufferBuilder vertexBuffer;
    private static boolean batchActive;

    public static void beginBatch() {
        if (batchActive) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        batchActive = true;
    }

    private static BufferBuilder getOrCreateTextBuffer(float f) {
        if (vertexBuffer == null) {
            moscow.rockstar.render.state.RenderStateSupport.bindGlyphAtlasTextures(0, 1, 2);
            ShaderProgram class_59442 = ShaderRenderer.slugFontShader.bindShaderProgram();
            class_59442.getUniform("Weight").set(0.0f);
            class_59442.getUniform("Softness").set(0.5f);
            class_59442.getUniform("EnableFadeout").set(0);
            vertexBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        }
        return vertexBuffer;
    }

    public static void drawText(FontRenderer fontRenderer, String string, float f, int n, Matrix4f matrix4f, float f2, float f3, float f4, float f5, float f6) {
        if (!batchActive) {
            TextBatchRenderer.beginBatch();
        }
        fontRenderer.draw(matrix4f, TextBatchRenderer.getOrCreateTextBuffer(f5), string, f, f2, f3, f4, n);
    }

    public static void drawTextWithFadeout(FontRenderer fontRenderer, String string, float f, int n, Matrix4f matrix4f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10) {
        if (!batchActive) {
            TextBatchRenderer.beginBatch();
        }
        BufferBuilder class_2872 = TextBatchRenderer.getOrCreateTextBuffer(f5);
        ShaderProgram class_59442 = RenderSystem.getShader();
        class_59442.getUniform("EnableFadeout").set(1);
        class_59442.getUniform("FadeoutStart").set(f7);
        class_59442.getUniform("FadeoutEnd").set(f8);
        class_59442.getUniform("MaxWidth").set(f9);
        class_59442.getUniform("TextPosX").set(f10);
        fontRenderer.draw(matrix4f, class_2872, string, f, f2, f3, f4, n);
    }

    public static void endBatch() {
        if (!batchActive) {
            return;
        }
        if (vertexBuffer != null) {
            BuiltBuffer class_98012 = vertexBuffer.endNullable();
            if (class_98012 != null) {
                BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
            }
            vertexBuffer = null;
        }
        moscow.rockstar.render.state.RenderStateSupport.finishBatch();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        batchActive = false;
    }

    static {
        batchActive = false;
    }
}
