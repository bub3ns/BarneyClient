/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  moscow.rockstar.render.batch.OverlayBatchBuilder$OverlayQuad
 *  moscow.rockstar.render.batch.OverlayBatchBuilder$TextOverlayEntry
 *  net.minecraft.Text
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tessellator
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.VertexConsumer
 *  net.minecraft.ShaderProgram
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package moscow.rockstar.render.batch;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.text.TextCaptureController;
import moscow.rockstar.render.text.TextComponentRun;
import moscow.rockstar.render.text.TextComponentRuns;
import moscow.rockstar.render.text.FontRenderer;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import net.minecraft.text.Text;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import pyrock.utility.render.ColorRGBA;

public class OverlayBatchBuilder {
    private static final float ZERO_OFFSET = 0.0f;
    private static final float HALF_PIXEL = 0.5f;
    private final FontRenderer textLayout;
    private final float cornerRadius;
    private final List<OverlayQuad> coloredQuads = new ArrayList<OverlayQuad>();
    private final List<OverlayVertex> texturedQuads = new ArrayList<OverlayVertex>();
    private final List<TextOverlayEntry> textEntries = new ArrayList<TextOverlayEntry>();
    private final List<TextOverlayEntry> shadowTextEntries = new ArrayList<TextOverlayEntry>();
    private boolean hasShadowText;
    private final List<OverlayQuad> backgroundQuads = new ArrayList<OverlayQuad>();
    private int textureId = 0;
    private float headWidth = 0.0f;
    private float headHeight = 0.0f;

    public OverlayBatchBuilder(FontRenderer textLayoutManager, float f) {
        this.textLayout = textLayoutManager;
        this.cornerRadius = f;
    }

    public void setTextureAndHeadSize(int n, float f, float f2) {
        this.textureId = n;
        this.headWidth = f;
        this.headHeight = f2;
    }

    public void queueColoredOverlay(Matrix4f matrix4f, float f, float f2, float f3, float f4, ColorRGBA colorRGBA) {
        this.queueColoredQuad(matrix4f, f, f2, f3, f4, colorRGBA, f, f3);
    }

    public void queueColoredQuad(Matrix4f matrix4f, float f, float f2, float f3, float f4, ColorRGBA colorRGBA, float f5, float f6) {
        this.coloredQuads.add(new OverlayQuad(new Matrix4f((Matrix4fc)matrix4f), f, f2, f3, f4, colorRGBA.getRGB(), f5, f6));
    }

    public void queueScaledQuad(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5) {
        this.queueTexturedQuad(matrix4f, f, f2, f3, f4, f5, f);
    }

    public void queueBackgroundQuad(Matrix4f matrix4f, float f, float f2, float f3, float f4) {
        this.backgroundQuads.add(new OverlayQuad(new Matrix4f((Matrix4fc)matrix4f), f, f2, f3, f4, 0, f, f3));
    }

    public void queueTexturedQuad(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6) {
        this.texturedQuads.add(new OverlayVertex(new Matrix4f((Matrix4fc)matrix4f), f, f2, f3, f4, f5, f6));
    }

    public void queueText(Matrix4f matrix4f, String string, float f, float f2, float f3, float f4, int n) {
        Matrix4f matrix4f2 = new Matrix4f((Matrix4fc)matrix4f);
        String string2 = TextCaptureController.transform(string);
        this.textEntries.add(new TextOverlayEntry(matrix4f2, string2, f, f2, f3, f4, n));
        if (TextCaptureController.isReplacementActive()) {
            this.shadowTextEntries.add(string2 == string ? this.textEntries.getLast() : new TextOverlayEntry(matrix4f2, string, f, f2, f3, f4, n));
            this.hasShadowText |= string2 != string;
        }
    }

    public void queueTextComponent(Matrix4f matrix4f, Text class_25612, float f, float f2, float f3, float f4) {
        this.queueTextComponentWithShadow(matrix4f, class_25612, f, f2, f3, f4, f2);
    }

    public void queueTextComponentWithShadow(Matrix4f matrix4f, Text class_25612, float f, float f2, float f3, float f4, float f5) {
        Matrix4f matrix4f2 = new Matrix4f((Matrix4fc)matrix4f);
        boolean bl = TextCaptureController.isReplacementActive();
        float f6 = f2;
        float f7 = f5;
        for (TextComponentRun textRun : TextComponentRuns.flatten(class_25612, ColorPalette.WHITE.getRGB())) {
            String string = TextCaptureController.transform(textRun.text());
            this.textEntries.add(new TextOverlayEntry(matrix4f2, string, f, f6, f3, f4, textRun.color()));
            f6 += this.textLayout.measure(string, f);
            if (!bl) continue;
            this.shadowTextEntries.add(new TextOverlayEntry(matrix4f2, textRun.text(), f, f7, f3, f4, textRun.color()));
            f7 += this.textLayout.measure(textRun.text(), f);
            this.hasShadowText |= !string.equals(textRun.text());
        }
    }

    public float measureTextComponent(Text class_25612, float f) {
        return OverlayBatchBuilder.measureTextComponentWithLayout(this.textLayout, class_25612, f);
    }

    public static float measureTextComponentWithLayout(FontRenderer textLayoutManager, Text class_25612, float f) {
        float f2 = 0.0f;
        for (TextComponentRun textRun : TextComponentRuns.flatten(class_25612, ColorPalette.WHITE.getRGB())) {
            f2 += textLayoutManager.measure(TextCaptureController.transform(textRun.text()), f);
        }
        return f2;
    }

    public void flush() {
        if (this.coloredQuads.isEmpty() && this.texturedQuads.isEmpty() && this.textEntries.isEmpty()) {
            return;
        }
        WidgetBatchRenderer.flushCurrentBatch();
        if (this.hasShadowText) {
            TextCaptureController.renderClipped(this.calculateDirtyRegion(), () -> this.renderQueuedOverlays(this.coloredQuads, this.texturedQuads, this.shadowTextEntries, true));
        }
        this.renderQueuedOverlays(this.coloredQuads, this.texturedQuads, this.textEntries, false);
        this.coloredQuads.clear();
        this.texturedQuads.clear();
        this.textEntries.clear();
        this.shadowTextEntries.clear();
        this.backgroundQuads.clear();
        this.hasShadowText = false;
    }

    private int[] calculateDirtyRegion() {
        if (!this.hasShadowText) {
            return null;
        }
        int[] nArray = null;
        for (OverlayQuad object : this.coloredQuads) {
            nArray = TextCaptureController.union(nArray, TextCaptureController.transformBounds(object.transform, object.x - 2.0f, object.y - 2.0f, object.x + object.width + 2.0f, object.y + object.height + 2.0f));
            nArray = TextCaptureController.union(nArray, TextCaptureController.transformBounds(object.transform, object.shadowX - 2.0f, object.y - 2.0f, object.shadowX + object.shadowWidth + 2.0f, object.y + object.height + 2.0f));
        }
        for (OverlayVertex overlayVertex : this.texturedQuads) {
            nArray = TextCaptureController.union(nArray, TextCaptureController.transformBounds(overlayVertex.matrix, overlayVertex.x - 2.0f, overlayVertex.y - 2.0f, overlayVertex.x + this.headWidth + 2.0f, overlayVertex.y + this.headWidth + 2.0f));
            nArray = TextCaptureController.union(nArray, TextCaptureController.transformBounds(overlayVertex.matrix, overlayVertex.realX - 2.0f, overlayVertex.y - 2.0f, overlayVertex.realX + this.headWidth + 2.0f, overlayVertex.y + this.headWidth + 2.0f));
        }
        for (TextOverlayEntry textOverlayEntry : this.textEntries) {
            nArray = TextCaptureController.union(nArray, this.getTextBounds(textOverlayEntry));
        }
        for (TextOverlayEntry textOverlayEntry : this.shadowTextEntries) {
            nArray = TextCaptureController.union(nArray, this.getTextBounds(textOverlayEntry));
        }
        for (OverlayQuad overlayQuad : this.backgroundQuads) {
            nArray = TextCaptureController.union(nArray, TextCaptureController.transformBounds(overlayQuad.transform, overlayQuad.x - 2.0f, overlayQuad.y - 2.0f, overlayQuad.x + overlayQuad.width + 2.0f, overlayQuad.y + overlayQuad.height + 2.0f));
        }
        return nArray;
    }

    private int[] getTextBounds(TextOverlayEntry textOverlayEntry) {
        return TextCaptureController.transformBounds(textOverlayEntry.transform, textOverlayEntry.x - 1.0f, textOverlayEntry.y - textOverlayEntry.fontSize * 0.35f, textOverlayEntry.x + this.textLayout.measure(textOverlayEntry.text, textOverlayEntry.fontSize) + 1.0f, textOverlayEntry.y + textOverlayEntry.fontSize * 1.45f);
    }

    private void renderQueuedOverlays(List<OverlayQuad> list, List<OverlayVertex> list2, List<TextOverlayEntry> list3, boolean bl) {
        float f;
        float f2;
        float f3;
        float f4;
        float f5;
        float f6;
        if (list.isEmpty() && list2.isEmpty() && list3.isEmpty()) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        moscow.rockstar.render.state.RenderStateSupport.bindGlyphAtlasTextures(0, 2, 3);
        RenderSystem.setShaderTexture((int)1, (int)this.textureId);
        ShaderProgram class_59442 = ShaderRenderer.universalUiShader.bindShaderProgram();
        class_59442.getUniform("Radius").set(this.cornerRadius, this.cornerRadius, this.cornerRadius, this.cornerRadius);
        class_59442.getUniform("RectSmoothness").set(0.5f);
        class_59442.getUniform("CornerSmoothness").set(2.0f);
        class_59442.getUniform("TextThickness").set(0.0f);
        class_59442.getUniform("HeadSize").set(this.headWidth, this.headWidth);
        class_59442.getUniform("HeadRadius").set(this.headHeight, this.headHeight, this.headHeight, this.headHeight);
        class_59442.getUniform("HeadSmoothness").set(0.5f);
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        float f7 = 0.5f;
        float f8 = -f7 / 2.0f + f7 * 2.0f;
        float f9 = f7 / 2.0f + f7;
        for (OverlayQuad object : list) {
            f6 = bl ? object.shadowX : object.x;
            f5 = bl ? object.shadowWidth : object.width;
            f4 = f6 - f8 / 2.0f;
            f3 = object.y - f9 / 2.0f;
            f2 = f5 + f8;
            f = object.height + f9;
            class_2872.vertex(object.transform, f4, f3, 0.0f).texture(f5, object.height).color(object.color).light(0);
            class_2872.vertex(object.transform, f4, f3 + f, 0.0f).texture(f5, object.height).color(object.color).light(0);
            class_2872.vertex(object.transform, f4 + f2, f3 + f, 0.0f).texture(f5, object.height).color(object.color).light(0);
            class_2872.vertex(object.transform, f4 + f2, f3, 0.0f).texture(f5, object.height).color(object.color).light(0);
        }
        for (OverlayVertex overlayVertex : list2) {
            f6 = (bl ? overlayVertex.realX : overlayVertex.x) - f8 / 2.0f;
            f5 = overlayVertex.y - f9 / 2.0f;
            f4 = this.headWidth + f8;
            f3 = this.headWidth + f9;
            f2 = overlayVertex.u0 - 2.0f;
            f = overlayVertex.v0 - 2.0f;
            float f10 = overlayVertex.u0 + overlayVertex.uv - 2.0f;
            float f11 = overlayVertex.v0 + overlayVertex.uv - 2.0f;
            class_2872.vertex(overlayVertex.matrix, f6, f5, 0.0f).texture(f2, f).color(-1).light(0);
            class_2872.vertex(overlayVertex.matrix, f6, f5 + f3, 0.0f).texture(f2, f11).color(-1).light(0);
            class_2872.vertex(overlayVertex.matrix, f6 + f4, f5 + f3, 0.0f).texture(f10, f11).color(-1).light(0);
            class_2872.vertex(overlayVertex.matrix, f6 + f4, f5, 0.0f).texture(f10, f).color(-1).light(0);
        }
        for (TextOverlayEntry textOverlayEntry : list3) {
            this.textLayout.draw(textOverlayEntry.transform, class_2872, textOverlayEntry.text, textOverlayEntry.fontSize, textOverlayEntry.x, textOverlayEntry.y, textOverlayEntry.z, textOverlayEntry.color);
        }
        BuiltBuffer builtBuffer = class_2872.endNullable();
        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram(builtBuffer);
        }
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.setShaderTexture((int)1, (int)0);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    static final class OverlayVertex {
        final Matrix4f matrix;
        final float x;
        final float y;
        final float u0;
        final float v0;
        final float uv;
        final float realX;

        OverlayVertex(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6) {
            this.matrix = matrix4f;
            this.x = f;
            this.y = f2;
            this.u0 = f3;
            this.v0 = f4;
            this.uv = f5;
            this.realX = f6;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "matrix", "x", "y", "u0", "v0", "uv", "realX");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "matrix", "x", "y", "u0", "v0", "uv", "realX");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "matrix", "x", "y", "u0", "v0", "uv", "realX");
        }

        public Matrix4f getMatrix() {
            return this.matrix;
        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }

        public float getU0() {
            return this.u0;
        }

        public float getV0() {
            return this.v0;
        }

        public float getUv() {
            return this.uv;
        }

        public float getRealX() {
            return this.realX;
        }
    }

    static final class OverlayQuad {
        final Matrix4f transform;
        final float x;
        final float y;
        final float width;
        final float height;
        final int color;
        final float shadowX;
        final float shadowWidth;

        OverlayQuad(Matrix4f transform, float x, float y, float width, float height, int color, float shadowX, float shadowWidth) {
            this.transform = transform;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.shadowX = shadowX;
            this.shadowWidth = shadowWidth;
        }
    }

    static final class TextOverlayEntry {
        final Matrix4f transform;
        final String text;
        final float fontSize;
        final float x;
        final float y;
        final float z;
        final int color;

        TextOverlayEntry(Matrix4f transform, String text, float fontSize, float x, float y, float z, int color) {
            this.transform = transform;
            this.text = text;
            this.fontSize = fontSize;
            this.x = x;
            this.y = y;
            this.z = z;
            this.color = color;
        }
    }
}
