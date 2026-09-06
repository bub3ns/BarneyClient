/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tessellator
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.ShaderProgram
 *  net.minecraft.BuiltBuffer
 *  org.joml.Matrix4f
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.opengl.GL11
 */
package moscow.rockstar.render.ui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.gl.OpenGlStateReset;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.state.RenderStateSupport;
import moscow.rockstar.render.state.UiScissorStack;
import moscow.rockstar.render.text.FontRenderer;
import moscow.rockstar.render.text.glyph.Glyph;
import moscow.rockstar.render.text.glyph.GlyphAtlas;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.layout.ItemGrid;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class WidgetStreamRenderer
implements FontRenderer.GlyphRenderSink {
    private static final int FLOATS_PER_WIDGET = 48;
    private static final int POSITION_FLOATS_PER_QUAD = 12;
    private static final int COLOR_VALUES_PER_QUAD = 12;
    private static final int COLOR_COMPONENTS = 4;
    private static final int MAX_TEXTURE_SLOTS = 8;
    private static final int POSITION_COMPONENTS = 3;
    private static final float UNINITIALIZED_MIN = -4096.0f;
    private static final float UNINITIALIZED_MAX = 4096.0f;
    private static final float ZERO_VALUE = 0.0f;
    private static final float UNIT_VALUE = 1.0f;
    private static final float ATTRIBUTE_INDEX_TWO = 2.0f;
    private static final float ATTRIBUTE_INDEX_THREE = 3.0f;
    private static final float ATTRIBUTE_INDEX_FOUR = 4.0f;
    private static final float ATTRIBUTE_INDEX_FIVE = 5.0f;
    private static final float[] ATTRIBUTE_MINIMUMS = new float[]{0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -4096.0f, -4096.0f, -4096.0f, -4096.0f, -2.0f, -2.0f, -2.0f, -2.0f, 0.0f, -4096.0f, 0.0f, -16.0f, -16.0f, -16.0f, -16.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
    private static final float[] ATTRIBUTE_MAXIMUMS = new float[]{8.0f, 16.0f, 32768.0f, 1.0f, 4096.0f, 4096.0f, 64.0f, 64.0f, 512.0f, 512.0f, 512.0f, 512.0f, 64.0f, 64.0f, 64.0f, 1.0f, 4096.0f, 4096.0f, 4096.0f, 4096.0f, 2.0f, 2.0f, 2.0f, 2.0f, 1.0f, 4096.0f, 4096.0f, 16.0f, 16.0f, 16.0f, 16.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f};
    private float[] widgetAttributeValues = new float[12288];
    private float[] vertexPositions = new float[3072];
    private int[] vertexColors = new int[1024];
    private int quadCount;
    private final int[] boundTextureIds = new int[8];
    private int boundTextureCount;
    private float[] clipRectStack = new float[32];
    private int clipRectDepth;
    private float clipLeft = -4096.0f;
    private float clipTop = -4096.0f;
    private float clipRight = 4096.0f;
    private float clipBottom = 4096.0f;
    private final int[] uploadTextureIds = new int[3];
    private int currentTextureSlot = -1;
    private int allocatedTextureHeight;
    private ShortBuffer attributeUploadBuffer;
    private boolean batchingEnabled = true;
    private Matrix4f currentTransform;
    private float matrixScale = 1.0f;
    private float textFadeStart;
    private float textFadeEnd;
    private float textClipEnabled;
    private float textClipCoordinate;
    private float textClipExtent;
    private float textClipLimit;
    private float textOpacity = 1.0f;
    private float textFadeInStart = -1.0f;
    private float textFadeInEnd = -1.0f;
    public WidgetStreamRenderer() {
    }

    public boolean hasQueuedWidgets() {
        return this.quadCount > 0;
    }

    public boolean canRenderBatch() {
        if (!this.batchingEnabled) {
            return false;
        }
        RenderSystem.assertOnRenderThread();
        if (ShaderRenderer.uiStreamShader == null || ShaderRenderer.uiStreamShader.bindShaderProgram() == null) {
            return false;
        }
        return this.ensureUploadTextures(1);
    }

    public void resetBatch() {
        this.quadCount = 0;
        this.boundTextureCount = 0;
        this.clipRectDepth = 0;
        this.clipLeft = -4096.0f;
        this.clipTop = -4096.0f;
        this.clipRight = 4096.0f;
        this.clipBottom = 4096.0f;
    }

    public void pushClipRect(float f, float f2, float f3, float f4) {
        this.ensureClipStackCapacity((this.clipRectDepth + 1) * 4);
        int n = this.clipRectDepth++ * 4;
        this.clipRectStack[n] = this.clipLeft;
        this.clipRectStack[n + 1] = this.clipTop;
        this.clipRectStack[n + 2] = this.clipRight;
        this.clipRectStack[n + 3] = this.clipBottom;
        this.clipLeft = f;
        this.clipTop = f2;
        this.clipRight = f + f3;
        this.clipBottom = f2 + f4;
    }

    public void popClipRect() {
        if (this.clipRectDepth <= 0) {
            return;
        }
        int n = --this.clipRectDepth * 4;
        this.clipLeft = this.clipRectStack[n];
        this.clipTop = this.clipRectStack[n + 1];
        this.clipRight = this.clipRectStack[n + 2];
        this.clipBottom = this.clipRectStack[n + 3];
    }

    public void appendQuad(Matrix4f matrix4f, float f, float f2, float f3, float f4, int n, int n2, int n3, int n4) {
        int n5 = this.allocateQuad(matrix4f, f, f2, f3, f4, n, n2, n3, n4);
        this.setWidgetAttribute(n5, 0, 0.0f);
    }

    public void appendRoundedQuad(Matrix4f matrix4f, float f, float f2, float f3, float f4, WidgetState widgetState, float f5, float f6, int n, int n2, int n3, int n4) {
        float f7 = f5 * 1.5f;
        int n5 = this.allocateQuad(matrix4f, f - f7 * 0.5f, f2 - f7 * 0.5f, f3 + f7, f4 + f7, n, n2, n3, n4);
        this.setQuadStyle(n5, 1.0f, f3, f4, widgetState, f5, f6);
    }

    public void appendRoundedQuadWithColorMask(Matrix4f matrix4f, float f, float f2, float f3, float f4, WidgetState widgetState, float f5, float f6, int n, int n2, int n3, int n4, boolean bl) {
        this.appendRoundedQuad(matrix4f, f, f2, f3, f4, widgetState, f5, f6, n, n2, n3, n4);
        int n5 = this.quadCount - 1;
        if (bl) {
            this.setWidgetAttribute(n5, 15, 1.0f);
            this.setQuadColor(n5, 32, n);
            this.setQuadColor(n5, 36, n2);
            this.setQuadColor(n5, 40, n3);
            this.setQuadColor(n5, 44, n4);
        }
    }

    public void appendTexturedQuad(Matrix4f matrix4f, float f, float f2, float f3, float f4, WidgetState widgetState, float f5, float f6, float f7, float f8, int n) {
        float f9 = f7 * 1.5f;
        int n2 = this.allocateQuad(matrix4f, f - f9 * 0.5f, f2 - f9 * 0.5f, f3 + f9, f4 + f9, n, n, n, n);
        this.setQuadStyle(n2, 2.0f, f3, f4, widgetState, f7, f8);
        this.setWidgetAttribute(n2, 12, f5);
        this.setWidgetAttribute(n2, 13, f6);
        this.setWidgetAttribute(n2, 14, f7);
    }

    public void appendGradientQuad(Matrix4f matrix4f, int n, float f, float f2, float f3, float f4, float f5, float f6, WidgetState widgetState, float f7, float f8, float f9, float f10, float f11, float f12, int n2) {
        int n3 = this.bindTextureSlot(n);
        int n4 = this.allocateQuad(matrix4f, f, f2, f3, f4, n2, n2, n2, n2);
        this.setQuadStyle(n4, 4.0f, f5, f6, widgetState, f7, f8);
        this.setWidgetAttribute(n4, 1, n3);
        this.setQuadTextureCoordinates(n4, f9, f10, f11, f12);
    }

    public void appendTextureQuad(Matrix4f matrix4f, int n, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int n2) {
        int n3 = this.bindTextureSlot(n);
        int n4 = this.allocateQuad(matrix4f, f, f2, f3, f4, n2, n2, n2, n2);
        this.setWidgetAttribute(n4, 0, 5.0f);
        this.setWidgetAttribute(n4, 1, n3);
        this.setQuadTextureCoordinates(n4, f5, f6, f7, f8);
    }

    public void submitTextLayout(Matrix4f matrix4f, FontRenderer fontRenderer, String string, float f, float f2, float f3, float f4, int n, float f5, float f6, boolean bl, float f7, float f8, float f9, float f10, float f11, float f12) {
        this.currentTransform = matrix4f;
        this.matrixScale = Glyph.getMatrixScale(matrix4f);
        this.textFadeStart = f5;
        this.textFadeEnd = f6;
        this.textClipEnabled = bl ? 1.0f : 0.0f;
        this.textClipCoordinate = this.transformX(matrix4f, f7, f3, f4);
        this.textClipExtent = f8;
        this.textClipLimit = f9;
        this.textOpacity = f10;
        boolean bl2 = f12 > 0.001f;
        this.textFadeInStart = bl2 ? f11 : -1.0f;
        this.textFadeInEnd = bl2 ? f12 : -1.0f;
        fontRenderer.drawToSink(string, f, f2, f3, f4, n, this);
    }

    public void appendGlyphQuad(Matrix4f matrix4f, FontRenderer fontRenderer, int n, float f, float f2, float f3, int n2) {
        Glyph glyph = fontRenderer.getGlyphOrFallback(n);
        if (glyph == null || glyph.isWhitespace()) {
            return;
        }
        float padding = 1.0f / (f3 * Glyph.getMatrixScale(matrix4f));
        float minX = glyph.getMinX() - padding;
        float maxX = glyph.getMaxX() + padding;
        float maxY = glyph.getMaxY() + padding;
        float minY = glyph.getMinY() - padding;
        int n3 = this.allocateQuad(matrix4f, f + minX * f3, f2 + (1.0f - maxY) * f3,
                (maxX - minX) * f3, (maxY - minY) * f3, n2, n2, n2, n2);
        this.setWidgetAttribute(n3, 0, 3.0f);
        this.setWidgetAttribute(n3, 2, glyph.getPackedAtlasIndex());
        this.setWidgetAttribute(n3, 3, 0.0f);
        this.setWidgetAttribute(n3, 7, 0.5f);
        this.setQuadTextureCoordinates(n3, minX, maxY, maxX, minY);
    }

    public void appendIconQuad(Matrix4f matrix, FontRenderer font, int codePoint,
                               float x, float y, float size, int color) {
        Glyph glyph = font.getGlyphOrFallback(codePoint);
        if (glyph == null || glyph.isWhitespace()) {
            return;
        }
        float padding = 1.0f / (size * Glyph.getMatrixScale(matrix));
        float left = glyph.getMinX() - padding;
        float right = glyph.getMaxX() + padding;
        float top = glyph.getMaxY() + padding;
        float bottom = glyph.getMinY() - padding;
        int quad = this.allocateQuad(matrix,
            x + left * size, y + (1.0f - top) * size,
            (right - left) * size, (top - bottom) * size,
            color, color, color, color);
        this.setWidgetAttribute(quad, 0, 3.0f);
        this.setWidgetAttribute(quad, 2, glyph.getPackedAtlasIndex());
        this.setWidgetAttribute(quad, 3, 0.0f);
        this.setWidgetAttribute(quad, 7, 0.5f);
        this.setQuadTextureCoordinates(quad, left, top, right, bottom);
    }

    @Override
    public void renderGlyph(Glyph glyph, float scale, float glyphX, float baselineY, float z, int color) {
        if (glyph == null || glyph.isWhitespace()) {
            return;
        }
        float left = glyph.getMinX() - 1.0f / (scale * this.matrixScale);
        float right = glyph.getMaxX() + 1.0f / (scale * this.matrixScale);
        float top = glyph.getMaxY() + 1.0f / (scale * this.matrixScale);
        float bottom = glyph.getMinY() - 1.0f / (scale * this.matrixScale);
        int n2 = this.allocateQuad(this.currentTransform,
                glyphX + left * scale,
                baselineY - top * scale,
                (right - left) * scale,
                (top - bottom) * scale,
                color, color, color, color);
        this.setWidgetAttribute(n2, 0, 3.0f);
        this.setWidgetAttribute(n2, 2, glyph.getPackedAtlasIndex());
        this.setWidgetAttribute(n2, 3, this.textFadeStart);
        this.setWidgetAttribute(n2, 7, this.textFadeEnd);
        this.setQuadTextureCoordinates(n2, left, top, right, bottom);
        this.setWidgetAttribute(n2, 24, this.textClipEnabled);
        this.setWidgetAttribute(n2, 25, this.textClipCoordinate);
        this.setWidgetAttribute(n2, 26, this.textClipExtent);
        this.setWidgetAttribute(n2, 27, this.textClipLimit);
        this.setWidgetAttribute(n2, 28, this.textOpacity);
        this.setWidgetAttribute(n2, 29, this.textFadeInStart);
        this.setWidgetAttribute(n2, 30, this.textFadeInEnd);
    }

    public void flushBatch() {
        int n;
        ShaderProgram class_59442;
        int n2;
        int n3;
        if (this.quadCount == 0) {
            return;
        }
        RenderSystem.assertOnRenderThread();
        UiScissorStack.disableForBatch();
        if (!this.ensureUploadTextures(this.quadCount)) {
            this.quadCount = 0;
            this.boundTextureCount = 0;
            return;
        }
        this.attributeUploadBuffer.clear();
        int n4 = this.quadCount * 48;
        for (n3 = 0; n3 < n4; ++n3) {
            n2 = n3 % 48;
            this.attributeUploadBuffer.put(this.normalizeAttribute(this.widgetAttributeValues[n3], ATTRIBUTE_MINIMUMS[n2], ATTRIBUTE_MAXIMUMS[n2]));
        }
        this.attributeUploadBuffer.flip();
        n3 = this.nextUploadTexture();
        GlStateManager._activeTexture((int)33984);
        GlStateManager._bindTexture((int)n3);
        this.resetPixelUnpackState();
        this.clearGlErrors();
        GL11.glTexSubImage2D((int)3553, (int)0, (int)0, (int)0, (int)12, (int)this.quadCount, (int)6408, (int)5123, (ShortBuffer)this.attributeUploadBuffer);
        n2 = GL11.glGetError();
        if (n2 != 0) {
            this.disableBatchingAfterGlError("upload", n2);
            this.quadCount = 0;
            this.boundTextureCount = 0;
            return;
        }
        float[] fArray = (float[])RenderSystem.getShaderColor().clone();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        WidgetBatchRenderer.prepareBlendState();
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture((int)0, (int)n3);
        RenderStateSupport.bindGlyphAtlasTextures(9, 10, 11);
        for (int i = 0; i < 8; ++i) {
            RenderSystem.setShaderTexture((int)(i + 1), (int)(i < this.boundTextureCount ? this.boundTextureIds[i] : 0));
        }
        ShaderProgram class_59443 = class_59442 = ShaderRenderer.uiStreamShader == null ? null : ShaderRenderer.uiStreamShader.bindShaderProgram();
        if (class_59442 == null) {
            this.disableBatchingAfterGlError("shader", 0);
            this.quadCount = 0;
            this.boundTextureCount = 0;
            for (int i = 0; i <= 11; ++i) {
                RenderSystem.setShaderTexture((int)i, (int)0);
            }
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor((float)fArray[0], (float)fArray[1], (float)fArray[2], (float)fArray[3]);
            return;
        }
        class_59442.addSamplerTexture("Sampler0", n3);
        GlyphAtlas glyphAtlas = GlyphAtlas.getInstance();
        class_59442.addSamplerTexture("Sampler9", glyphAtlas.getCurveTexture());
        class_59442.addSamplerTexture("Sampler10", glyphAtlas.getBandTexture());
        class_59442.addSamplerTexture("Sampler11", glyphAtlas.getGlyphTableTexture());
        for (int i = 0; i < 8; ++i) {
            class_59442.addSamplerTexture("Sampler" + (i + 1), i < this.boundTextureCount ? this.boundTextureIds[i] : 0);
        }
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (int i = 0; i < this.quadCount; ++i) {
            n = i * 12;
            int n5 = i * 4;
            float f = i;
            class_2872.vertex(this.vertexPositions[n], this.vertexPositions[n + 1], this.vertexPositions[n + 2]).texture(f, 0.0f).color(this.vertexColors[n5]);
            class_2872.vertex(this.vertexPositions[n + 3], this.vertexPositions[n + 4], this.vertexPositions[n + 5]).texture(f, 1.0f).color(this.vertexColors[n5 + 1]);
            class_2872.vertex(this.vertexPositions[n + 6], this.vertexPositions[n + 7], this.vertexPositions[n + 8]).texture(f, 2.0f).color(this.vertexColors[n5 + 2]);
            class_2872.vertex(this.vertexPositions[n + 9], this.vertexPositions[n + 10], this.vertexPositions[n + 11]).texture(f, 3.0f).color(this.vertexColors[n5 + 3]);
        }
        BuiltBuffer class_98012 = class_2872.endNullable();
        if (class_98012 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
        }
        for (n = 0; n <= 11; ++n) {
            RenderSystem.setShaderTexture((int)n, (int)0);
        }
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor((float)fArray[0], (float)fArray[1], (float)fArray[2], (float)fArray[3]);
        this.quadCount = 0;
        this.boundTextureCount = 0;
    }

    private int allocateQuad(Matrix4f matrix4f, float f, float f2, float f3, float f4, int n, int n2, int n3, int n4) {
        this.ensureQuadCapacity(this.quadCount + 1);
        int n5 = this.quadCount++;
        int n6 = n5 * 12;
        this.writeVertexPosition(matrix4f, f, f2, 0.0f, n6);
        this.writeVertexPosition(matrix4f, f, f2 + f4, 0.0f, n6 + 3);
        this.writeVertexPosition(matrix4f, f + f3, f2 + f4, 0.0f, n6 + 6);
        this.writeVertexPosition(matrix4f, f + f3, f2, 0.0f, n6 + 9);
        int n7 = n5 * 4;
        this.vertexColors[n7] = n;
        this.vertexColors[n7 + 1] = n2;
        this.vertexColors[n7 + 2] = n3;
        this.vertexColors[n7 + 3] = n4;
        int n8 = n5 * 48;
        Arrays.fill(this.widgetAttributeValues, n8, n8 + 48, 0.0f);
        this.setWidgetAttribute(n5, 16, this.clipLeft);
        this.setWidgetAttribute(n5, 17, this.clipTop);
        this.setWidgetAttribute(n5, 18, this.clipRight);
        this.setWidgetAttribute(n5, 19, this.clipBottom);
        return n5;
    }

    private void setQuadStyle(int n, float f, float f2, float f3, WidgetState widgetState, float f4, float f5) {
        this.setWidgetAttribute(n, 0, f);
        this.setWidgetAttribute(n, 4, f2);
        this.setWidgetAttribute(n, 5, f3);
        this.setWidgetAttribute(n, 6, f5);
        this.setWidgetAttribute(n, 7, f4);
        this.setWidgetAttribute(n, 8, widgetState.topLeftRadius());
        this.setWidgetAttribute(n, 9, widgetState.bottomLeftRadius());
        this.setWidgetAttribute(n, 10, widgetState.topRightRadius());
        this.setWidgetAttribute(n, 11, widgetState.bottomRightRadius());
    }

    private void setQuadTextureCoordinates(int n, float f, float f2, float f3, float f4) {
        this.setWidgetAttribute(n, 20, f);
        this.setWidgetAttribute(n, 21, f2);
        this.setWidgetAttribute(n, 22, f3);
        this.setWidgetAttribute(n, 23, f4);
    }

    private void setQuadColor(int n, int n2, int n3) {
        this.setWidgetAttribute(n, n2, (float)(n3 >>> 16 & 0xFF) / 255.0f);
        this.setWidgetAttribute(n, n2 + 1, (float)(n3 >>> 8 & 0xFF) / 255.0f);
        this.setWidgetAttribute(n, n2 + 2, (float)(n3 & 0xFF) / 255.0f);
        this.setWidgetAttribute(n, n2 + 3, (float)(n3 >>> 24 & 0xFF) / 255.0f);
    }

    private int bindTextureSlot(int n) {
        for (int i = 0; i < this.boundTextureCount; ++i) {
            if (this.boundTextureIds[i] != n) continue;
            return i;
        }
        if (this.boundTextureCount == 8) {
            this.flushBatch();
        }
        this.boundTextureIds[this.boundTextureCount] = n;
        return this.boundTextureCount++;
    }

    private void setWidgetAttribute(int n, int n2, float f) {
        this.widgetAttributeValues[n * 48 + n2] = f;
    }

    private short normalizeAttribute(float f, float f2, float f3) {
        float f4 = (f - f2) / (f3 - f2);
        int n = Math.round(Math.max(0.0f, Math.min(1.0f, f4)) * 65535.0f);
        return (short)n;
    }

    private void writeVertexPosition(Matrix4f matrix4f, float f, float f2, float f3, int n) {
        this.vertexPositions[n] = matrix4f.m00() * f + matrix4f.m10() * f2 + matrix4f.m20() * f3 + matrix4f.m30();
        this.vertexPositions[n + 1] = matrix4f.m01() * f + matrix4f.m11() * f2 + matrix4f.m21() * f3 + matrix4f.m31();
        this.vertexPositions[n + 2] = matrix4f.m02() * f + matrix4f.m12() * f2 + matrix4f.m22() * f3 + matrix4f.m32();
    }

    private float transformX(Matrix4f matrix4f, float f, float f2, float f3) {
        return matrix4f.m00() * f + matrix4f.m10() * f2 + matrix4f.m20() * f3 + matrix4f.m30();
    }

    private void ensureQuadCapacity(int n) {
        int n2 = this.widgetAttributeValues.length / 48;
        if (n <= n2) {
            return;
        }
        int n3 = Integer.highestOneBit(n - 1) << 1;
        this.widgetAttributeValues = Arrays.copyOf(this.widgetAttributeValues, n3 * 48);
        this.vertexPositions = Arrays.copyOf(this.vertexPositions, n3 * 12);
        this.vertexColors = Arrays.copyOf(this.vertexColors, n3 * 4);
    }

    private void ensureClipStackCapacity(int n) {
        if (n <= this.clipRectStack.length) {
            return;
        }
        this.clipRectStack = Arrays.copyOf(this.clipRectStack, Integer.highestOneBit(n - 1) << 1);
    }

    private boolean ensureUploadTextures(int n) {
        if (!this.batchingEnabled) {
            return false;
        }
        if (this.uploadTextureIds[0] == 0) {
            for (int i = 0; i < this.uploadTextureIds.length; ++i) {
                this.uploadTextureIds[i] = GL11.glGenTextures();
                this.configureTextureParameters(this.uploadTextureIds[i]);
            }
        }
        if (n > this.allocatedTextureHeight) {
            this.clearGlErrors();
            this.allocatedTextureHeight = Integer.highestOneBit(n - 1) << 1;
            if (this.allocatedTextureHeight < 256) {
                this.allocatedTextureHeight = 256;
            }
            for (int n2 : this.uploadTextureIds) {
                int n3;
                GlStateManager._activeTexture((int)33984);
                GlStateManager._bindTexture((int)n2);
                this.resetPixelUnpackState();
                GL11.glTexImage2D((int)3553, (int)0, (int)32859, (int)12, (int)this.allocatedTextureHeight, (int)0, (int)6408, (int)5123, (ByteBuffer)null);
                int n4 = GL11.glGetError();
                int n5 = n4 == 0 ? GL11.glGetTexLevelParameteri((int)3553, (int)0, (int)4096) : 0;
                int n6 = n3 = n4 == 0 ? GL11.glGetTexLevelParameteri((int)3553, (int)0, (int)4097) : 0;
                if (n4 == 0 && n5 == 12 && n3 == this.allocatedTextureHeight) continue;
                this.disableBatchingAfterGlError("allocation", n4);
                return false;
            }
            this.attributeUploadBuffer = BufferUtils.createShortBuffer((int)(this.allocatedTextureHeight * 48));
        }
        return true;
    }

    private void configureTextureParameters(int n) {
        GlStateManager._activeTexture((int)33984);
        GlStateManager._bindTexture((int)n);
        GL11.glTexParameteri((int)3553, (int)10241, (int)9728);
        GL11.glTexParameteri((int)3553, (int)10240, (int)9728);
        GL11.glTexParameteri((int)3553, (int)10242, (int)33071);
        GL11.glTexParameteri((int)3553, (int)10243, (int)33071);
        GL11.glTexParameteri((int)3553, (int)33084, (int)0);
        GL11.glTexParameteri((int)3553, (int)33085, (int)0);
    }

    private int nextUploadTexture() {
        this.currentTextureSlot = (this.currentTextureSlot + 1) % this.uploadTextureIds.length;
        return this.uploadTextureIds[this.currentTextureSlot];
    }

    private void disableBatchingAfterGlError(String string, int n) {
        this.batchingEnabled = false;
        System.err.println("[Barney] Low-draw UI batching disabled after command texture " + string + " failure (OpenGL error " + n + ").");
    }

    private void clearGlErrors() {
        while (GL11.glGetError() != 0) {
        }
    }

    private void resetPixelUnpackState() {
        OpenGlStateReset.resetPixelUnpackState();
    }
}
