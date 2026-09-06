/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tessellator
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.VertexConsumer
 *  net.minecraft.ShaderProgram
 *  net.minecraft.BuiltBuffer
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package moscow.rockstar.render.batch;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.state.RenderStateSupport;
import moscow.rockstar.render.text.FontRenderer;
import moscow.rockstar.render.ui.WidgetStreamRenderer;
import moscow.rockstar.ui.core.WidgetState;
import moscow.rockstar.ui.text.Font;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import pyrock.utility.render.ColorRGBA;

public final class WidgetBatchRenderer
implements AutoCloseable {
    private static WidgetBatchRenderer currentBatch;
    private static boolean flushing;
    private static final WidgetStreamRenderer sharedGeometryNode;
    public static boolean textureRenderingActive;
    private final WidgetBatchRenderer previousBatch;
    private final WidgetStreamRenderer geometryBatch;
    private final List<RectangleEntry> rectangleEntries = new ArrayList<RectangleEntry>();
    private final Map<WidgetShapeStyle, List<RoundedRectangleEntry>> roundedRectangleBatches = new LinkedHashMap<WidgetShapeStyle, List<RoundedRectangleEntry>>();
    private final Map<WidgetShapeStyle, List<RoundedRectangleEntry>> borderedRectangleBatches = new LinkedHashMap<WidgetShapeStyle, List<RoundedRectangleEntry>>();
    private final Map<FontRenderer, List<TextEntry>> textEntriesByFont = new LinkedHashMap<FontRenderer, List<TextEntry>>();
    private final Map<FontRenderer, List<GlyphEntry>> glyphEntriesByFont = new LinkedHashMap<FontRenderer, List<GlyphEntry>>();
    private final Map<ShapeBatchKey, List<TexturedWidgetEntry>> texturedWidgetBatches = new LinkedHashMap<ShapeBatchKey, List<TexturedWidgetEntry>>();
    private final List<TexturedRectangleEntry> texturedRectangleEntries = new ArrayList<TexturedRectangleEntry>();
    private int activeTextureId;

    private WidgetBatchRenderer(boolean bl) {
        this.previousBatch = currentBatch;
        if (bl) {
            WidgetStreamRenderer widgetStreamRenderer = currentBatch != null && WidgetBatchRenderer.currentBatch.geometryBatch == sharedGeometryNode ? new WidgetStreamRenderer() : sharedGeometryNode;
            WidgetStreamRenderer widgetStreamRenderer2 = this.geometryBatch = widgetStreamRenderer.canRenderBatch() ? widgetStreamRenderer : null;
            if (this.geometryBatch != null) {
                this.geometryBatch.resetBatch();
            }
        } else {
            this.geometryBatch = null;
        }
        currentBatch = this;
    }

    public static WidgetBatchRenderer beginBatch() {
        return new WidgetBatchRenderer(false);
    }

    public static WidgetBatchRenderer beginGeometryBatch() {
        return new WidgetBatchRenderer(true);
    }

    public static WidgetBatchRenderer getCurrentBatch() {
        return currentBatch;
    }

    public static void flushCurrentBatch() {
        if (currentBatch != null) {
            boolean bl = WidgetBatchRenderer.currentBatch.geometryBatch != null;
            currentBatch.flush();
            if (bl) {
                moscow.rockstar.render.state.UiScissorStack.resumeForBatch();
            }
        }
    }

    public static void flushCurrentBatchIfNeeded() {
        if (currentBatch == null) {
            return;
        }
        if (WidgetBatchRenderer.currentBatch.geometryBatch != null) {
            currentBatch.renderGeometryBatches();
        } else {
            currentBatch.flush();
        }
    }

    public static boolean isGeometryBatchActive() {
        return currentBatch != null && WidgetBatchRenderer.currentBatch.geometryBatch != null;
    }

    public static boolean hasPendingBatches() {
        return currentBatch != null && !currentBatch.isBatchEmpty();
    }

    public boolean usesGeometryBatch() {
        return this.geometryBatch != null;
    }

    public void submitGeometryQuad(float f, float f2, float f3, float f4) {
        if (this.geometryBatch != null) {
            this.geometryBatch.pushClipRect(f, f2, f3, f4);
        }
    }

    public void flushGeometryBatch() {
        if (this.geometryBatch != null) {
            this.geometryBatch.popClipRect();
        }
    }

    public static void prepareBlendState() {
        RenderSystem.enableBlend();
        if (textureRenderingActive) {
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        } else {
            RenderSystem.defaultBlendFunc();
        }
    }

    public static boolean isFlushing() {
        return flushing;
    }

    public void queueRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, ColorRGBA colorRGBA) {
        if (WidgetBatchRenderer.hasInvalidSize(f3, f4) || WidgetBatchRenderer.isTransparent(colorRGBA)) {
            return;
        }
        int n = this.applyShaderColor(colorRGBA.getRGB());
        if ((n >>> 24 & 0xFF) == 0) {
            return;
        }
        this.flushTextureBatches();
        this.rectangleEntries.add(new RectangleEntry(new Matrix4f((Matrix4fc)matrix4f), f, f2, f3, f4, n));
    }

    public void queueRoundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, WidgetState widgetState, ColorRGBA colorRGBA) {
        this.queueStyledRoundedRectangle(matrix4f, f, f2, f3, f4, widgetState, 0.5f, 2.0f, colorRGBA);
    }

    public void queueSquircleRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, WidgetState widgetState, ColorRGBA colorRGBA) {
        this.queueStyledRoundedRectangle(matrix4f, f, f2, f3, f4, WidgetBatchRenderer.scaleCornerRadii(widgetState, f5 / 2.0f), 0.5f, f5, colorRGBA);
    }

    public void queueSmoothRoundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, WidgetState widgetState, ColorRGBA colorRGBA) {
        this.queueStyledRoundedRectangle(matrix4f, f, f2, f3, f4, WidgetBatchRenderer.scaleCornerRadii(widgetState, 3.0f), f5, 2.0f, colorRGBA);
    }

    public void queueBorderedRoundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, WidgetState widgetState, ColorRGBA colorRGBA) {
        this.queueStyledRoundedRectangleVariant(matrix4f, f, f2, f3, f4, f5, widgetState, 2.0f, 1.0f, colorRGBA);
    }

    public void queueSquircleBorderRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, WidgetState widgetState, ColorRGBA colorRGBA) {
        this.queueStyledRoundedRectangleVariant(matrix4f, f, f2, f3, f4, f5, WidgetBatchRenderer.scaleCornerRadii(widgetState, f6 / 2.0f), f6, 0.5f, colorRGBA);
    }

    public void queueText(FontRenderer textLayoutManager2, String string, float f, Matrix4f matrix4f, float f2, float f3, float f4, int n) {
        if (textLayoutManager2 == null || string == null || string.isEmpty()) {
            return;
        }
        int n2 = this.applyShaderColor(n);
        if ((n2 >>> 24 & 0xFF) == 0) {
            return;
        }
        this.flushTextureBatches();
        this.textEntriesByFont.computeIfAbsent(textLayoutManager2, textLayoutManager -> new ArrayList()).add(new TextEntry(new Matrix4f((Matrix4fc)matrix4f), string, f, f2, f3, f4, n2));
    }

    public boolean renderTextWithGeometry(FontRenderer textLayoutManager, String string, float f, Matrix4f matrix4f, float f2, float f3, float f4, int n, float f5, float f6, boolean bl, float f7, float f8, float f9, float f10, float f11, float f12) {
        if (this.geometryBatch == null) {
            return false;
        }
        if (textLayoutManager == null || string == null || string.isEmpty()) {
            return true;
        }
        int n2 = this.applyShaderColor(n);
        if ((n2 >>> 24 & 0xFF) == 0) {
            return true;
        }
        this.renderGeometryBatches();
        this.geometryBatch.submitTextLayout(new Matrix4f((Matrix4fc)matrix4f), textLayoutManager, string, f, f2, f3, f4, n2, f5, f6, bl, f7, f8, f9, f10, f11, f12);
        return true;
    }

    public boolean renderRoundedRectangleWithGeometry(Matrix4f matrix4f, float f, float f2, float f3, float f4, WidgetState widgetState, float f5, float f6, ColorRGBA colorRGBA, ColorRGBA colorRGBA2, ColorRGBA colorRGBA3, ColorRGBA colorRGBA4, boolean bl) {
        if (this.geometryBatch == null) {
            return false;
        }
        this.renderGeometryBatches();
        this.geometryBatch.appendRoundedQuadWithColorMask(new Matrix4f((Matrix4fc)matrix4f), f, f2, f3, f4, widgetState, f5, f6, this.applyShaderColor(colorRGBA.getRGB()), this.applyShaderColor(colorRGBA2.getRGB()), this.applyShaderColor(colorRGBA3.getRGB()), this.applyShaderColor(colorRGBA4.getRGB()), bl);
        return true;
    }

    public void queueGlyphFromString(String string, float f, float f2, float f3, Matrix4f matrix4f, ColorRGBA colorRGBA) {
        if (string == null || string.isEmpty() || f3 <= 0.0f || WidgetBatchRenderer.isTransparent(colorRGBA)) {
            return;
        }
        int n = string.codePointAt(0);
        int n2 = this.applyShaderColor(colorRGBA.getRGB());
        if ((n2 >>> 24 & 0xFF) == 0) {
            return;
        }
        FontRenderer textLayoutManager = Font.NOTO;
        if (textLayoutManager == null || !textLayoutManager.canDisplay(n)) {
            return;
        }
        this.flushTextureBatches();
        this.appendGlyphEntry(textLayoutManager, n, f, f2, f3, matrix4f, n2);
    }

    public void queueGlyph(FontRenderer textLayoutManager, int n, float f, float f2, float f3, Matrix4f matrix4f, int n2) {
        if (textLayoutManager == null || f3 <= 0.0f) {
            return;
        }
        int n3 = this.applyShaderColor(n2);
        if ((n3 >>> 24 & 0xFF) == 0) {
            return;
        }
        this.flushTextureBatches();
        this.appendGlyphEntry(textLayoutManager, n, f, f2, f3, matrix4f, n3);
    }

    public void queueIcon(FontRenderer font, int codePoint, float x, float y, float size,
                          Matrix4f matrix, int color) {
        if (font == null || size <= 0.0f || (color >>> 24 & 0xFF) == 0) {
            return;
        }
        int adjustedColor = this.applyShaderColor(color);
        if ((adjustedColor >>> 24 & 0xFF) == 0) {
            return;
        }
        this.flushTextureBatches();
        this.appendGlyphEntry(font, codePoint, x, y, size, matrix, adjustedColor, true);
    }

    public void queueTexturedRectangle(int n, Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, ColorRGBA colorRGBA) {
        if (n == 0 || WidgetBatchRenderer.hasInvalidSize(f3, f4) || WidgetBatchRenderer.isTransparent(colorRGBA)) {
            return;
        }
        int n2 = this.applyShaderColor(colorRGBA.getRGB());
        if ((n2 >>> 24 & 0xFF) == 0) {
            return;
        }
        this.flushShapeBatches();
        this.renderTexturedWidgets();
        if (!this.texturedRectangleEntries.isEmpty() && this.activeTextureId != n) {
            this.renderTexturedRectangles();
        }
        this.activeTextureId = n;
        this.texturedRectangleEntries.add(new TexturedRectangleEntry(new Matrix4f((Matrix4fc)matrix4f), f, f2, f3, f4, f5, f6, f7, f8, n2));
    }

    public void queueTexturedWidget(int n, Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15, float f16, ColorRGBA colorRGBA) {
        this.queueTexturedWidgetInternal(n, true, matrix4f, f, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, f16, colorRGBA);
    }

    public void queueTexturedRectangleWithDefaults(int n, Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15, ColorRGBA colorRGBA) {
        this.queueTexturedWidgetInternal(n, false, matrix4f, f, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15, 2.0f, colorRGBA);
    }

    private void appendGlyphEntry(FontRenderer textLayoutManager2, int n, float f, float f2, float f3, Matrix4f matrix4f, int n2) {
        this.appendGlyphEntry(textLayoutManager2, n, f, f2, f3, matrix4f, n2, false);
    }

    private void appendGlyphEntry(FontRenderer font, int codePoint, float x, float y, float size,
                                  Matrix4f matrix, int color, boolean uiCoordinates) {
        this.glyphEntriesByFont.computeIfAbsent(font, ignored -> new ArrayList<>())
            .add(new GlyphEntry(new Matrix4f((Matrix4fc)matrix), codePoint, x, y, size, color, uiCoordinates));
    }

    public void flush() {
        if (this.geometryBatch != null) {
            if (flushing) {
                return;
            }
            flushing = true;
            try {
                this.renderGeometryBatches();
                this.geometryBatch.flushBatch();
            }
            finally {
                flushing = false;
            }
            return;
        }
        if (flushing || this.isBatchEmpty()) {
            return;
        }
        flushing = true;
        float[] fArray = (float[])RenderSystem.getShaderColor().clone();
        try {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            this.renderRoundedRectangles(this.roundedRectangleBatches, false);
            this.renderRectangles();
            this.renderRoundedRectangles(this.borderedRectangleBatches, true);
            this.renderTextEntries();
            this.renderGlyphEntries();
            this.renderTexturedWidgets();
            this.renderTexturedRectangles();
        }
        finally {
            RenderSystem.setShaderColor((float)fArray[0], (float)fArray[1], (float)fArray[2], (float)fArray[3]);
            this.clearAllQueues();
            flushing = false;
        }
    }

    public void restoreEnabledFlag() {
        this.flush();
        if (currentBatch == this) {
            currentBatch = this.previousBatch;
        }
    }

    @Override
    public void close() {
        this.restoreEnabledFlag();
    }

    private void queueStyledRoundedRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, WidgetState widgetState, float f5, float f6, ColorRGBA colorRGBA) {
        if (WidgetBatchRenderer.hasInvalidSize(f3, f4) || WidgetBatchRenderer.isTransparent(colorRGBA)) {
            return;
        }
        int n = this.applyShaderColor(colorRGBA.getRGB());
        if ((n >>> 24 & 0xFF) == 0) {
            return;
        }
        this.flushTextureBatches();
        WidgetShapeStyle widgetShapeStyle2 = WidgetShapeStyle.fromWidgetState(widgetState, f5, f6);
        this.roundedRectangleBatches.computeIfAbsent(widgetShapeStyle2, widgetShapeStyle -> new ArrayList()).add(new RoundedRectangleEntry(new Matrix4f((Matrix4fc)matrix4f), f, f2, f3, f4, n));
    }

    private void queueStyledRoundedRectangleVariant(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, WidgetState widgetState, float f6, float f7, ColorRGBA colorRGBA) {
        if (WidgetBatchRenderer.hasInvalidSize(f3, f4) || f5 <= 0.0f || WidgetBatchRenderer.isTransparent(colorRGBA)) {
            return;
        }
        int n = this.applyShaderColor(colorRGBA.getRGB());
        if ((n >>> 24 & 0xFF) == 0) {
            return;
        }
        this.flushTextureBatches();
        WidgetShapeStyle widgetShapeStyle2 = WidgetShapeStyle.fromWidgetStateWithSmoothing(widgetState, f5, 0.5f, f7, f6);
        this.borderedRectangleBatches.computeIfAbsent(widgetShapeStyle2, widgetShapeStyle -> new ArrayList()).add(new RoundedRectangleEntry(new Matrix4f((Matrix4fc)matrix4f), f, f2, f3, f4, n));
    }

    private void queueTexturedWidgetInternal(int n, boolean bl, Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15, float f16, ColorRGBA colorRGBA) {
        if (n == 0 || WidgetBatchRenderer.hasInvalidSize(f3, f4) || WidgetBatchRenderer.hasInvalidSize(f9, f10) || WidgetBatchRenderer.isTransparent(colorRGBA)) {
            return;
        }
        int n2 = this.applyShaderColor(colorRGBA.getRGB());
        if ((n2 >>> 24 & 0xFF) == 0) {
            return;
        }
        this.flushShapeBatches();
        this.renderTexturedRectangles();
        ShapeBatchKey shapeBatchKey2 = new ShapeBatchKey(n, bl, f9, f10, f11, f12, f13, f14, f15, f16);
        this.texturedWidgetBatches.computeIfAbsent(shapeBatchKey2, shapeBatchKey -> new ArrayList()).add(new TexturedWidgetEntry(new Matrix4f((Matrix4fc)matrix4f), f, f2, f3, f4, f5, f6, f7, f8, n2));
    }

    private void flushShapeBatches() {
        if (this.areShapeAndTextQueuesEmpty() || flushing) {
            return;
        }
        if (this.geometryBatch != null) {
            this.renderGeometryEntries();
            return;
        }
        flushing = true;
        float[] fArray = (float[])RenderSystem.getShaderColor().clone();
        try {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            this.renderRoundedRectangles(this.roundedRectangleBatches, false);
            this.renderRectangles();
            this.renderRoundedRectangles(this.borderedRectangleBatches, true);
            this.renderTextEntries();
            this.renderGlyphEntries();
            this.clearShapeAndTextQueues();
        }
        finally {
            RenderSystem.setShaderColor((float)fArray[0], (float)fArray[1], (float)fArray[2], (float)fArray[3]);
            flushing = false;
        }
    }

    private void renderRectangles() {
        if (this.rectangleEntries.isEmpty()) {
            return;
        }
        WidgetBatchRenderer.prepareBlendState();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (RectangleEntry rectangleEntry : this.rectangleEntries) {
            class_2872.vertex(rectangleEntry.transform, rectangleEntry.x, rectangleEntry.y + rectangleEntry.height, 0.0f).color(rectangleEntry.color);
            class_2872.vertex(rectangleEntry.transform, rectangleEntry.x + rectangleEntry.width, rectangleEntry.y + rectangleEntry.height, 0.0f).color(rectangleEntry.color);
            class_2872.vertex(rectangleEntry.transform, rectangleEntry.x + rectangleEntry.width, rectangleEntry.y, 0.0f).color(rectangleEntry.color);
            class_2872.vertex(rectangleEntry.transform, rectangleEntry.x, rectangleEntry.y, 0.0f).color(rectangleEntry.color);
        }
        BuiltBuffer class_98012 = class_2872.endNullable();
        if (class_98012 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
        }
        RenderSystem.disableBlend();
    }

    private void renderRoundedRectangles(Map<WidgetShapeStyle, List<RoundedRectangleEntry>> map, boolean bl) {
        if (map.isEmpty()) {
            return;
        }
        WidgetBatchRenderer.prepareBlendState();
        RenderSystem.disableCull();
        moscow.rockstar.render.state.RenderStateSupport.bindGlyphAtlasTextures(0, 2, 3);
        RenderSystem.setShaderTexture((int)1, (int)0);
        for (Map.Entry<WidgetShapeStyle, List<RoundedRectangleEntry>> entry : map.entrySet()) {
            WidgetShapeStyle widgetShapeStyle = entry.getKey();
            ShaderProgram class_59442 = ShaderRenderer.universalUiShader.bindShaderProgram();
            class_59442.getUniform("Radius").set(widgetShapeStyle.topLeftRadius, widgetShapeStyle.bottomLeftRadius, widgetShapeStyle.topRightRadius, widgetShapeStyle.bottomRightRadius);
            class_59442.getUniform("RectSmoothness").set(widgetShapeStyle.outerSmoothness);
            class_59442.getUniform("CornerSmoothness").set(widgetShapeStyle.cornerSmoothness);
            class_59442.getUniform("BorderThickness").set(widgetShapeStyle.borderThickness);
            class_59442.getUniform("BorderSmoothness").set(widgetShapeStyle.innerSmoothness, widgetShapeStyle.outerSmoothness);
            class_59442.getUniform("TextThickness").set(0.0f);
            class_59442.getUniform("HeadSize").set(0.0f, 0.0f);
            class_59442.getUniform("HeadRadius").set(0.0f, 0.0f, 0.0f, 0.0f);
            class_59442.getUniform("HeadSmoothness").set(0.5f);
            BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
            for (RoundedRectangleEntry roundedRectangleEntry : entry.getValue()) {
                this.emitRoundedRectangleVertices(class_2872, roundedRectangleEntry, widgetShapeStyle.outerSmoothness, bl);
            }
            BuiltBuffer class_98012 = class_2872.endNullable();
            if (class_98012 == null) continue;
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
        }
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.setShaderTexture((int)1, (int)0);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderTextEntries() {
        if (this.textEntriesByFont.isEmpty()) {
            return;
        }
        WidgetBatchRenderer.prepareBlendState();
        RenderSystem.disableCull();
        RenderStateSupport.bindGlyphAtlasTextures(0, 1, 2);
        ShaderProgram class_59442 = ShaderRenderer.slugFontShader.bindShaderProgram();
        class_59442.getUniform("Weight").set(0.0f);
        class_59442.getUniform("Softness").set(1.0f);
        class_59442.getUniform("EnableFadeout").set(0);
        class_59442.getUniform("FadeoutStart").set(0.0f);
        class_59442.getUniform("FadeoutEnd").set(1.0f);
        class_59442.getUniform("FadeinStart").set(0.0f);
        class_59442.getUniform("FadeinEnd").set(0.0f);
        class_59442.getUniform("MaxWidth").set(0.0f);
        class_59442.getUniform("TextPosX").set(0.0f);
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        for (Map.Entry<FontRenderer, List<TextEntry>> entry : this.textEntriesByFont.entrySet()) {
            FontRenderer textLayoutManager = entry.getKey();
            for (TextEntry textEntry : entry.getValue()) {
                textLayoutManager.draw(textEntry.transform, class_2872, textEntry.text, textEntry.fontSize, textEntry.x, textEntry.y, textEntry.z, textEntry.color);
            }
        }
        BuiltBuffer class_98012 = class_2872.endNullable();
        if (class_98012 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
        }
        moscow.rockstar.render.state.RenderStateSupport.finishBatch();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderGlyphEntries() {
        for (Map.Entry<FontRenderer, List<GlyphEntry>> entry : this.glyphEntriesByFont.entrySet()) {
            FontRenderer textLayoutManager = entry.getKey();
            WidgetBatchRenderer.prepareBlendState();
            RenderSystem.disableCull();
            RenderStateSupport.bindGlyphAtlasTextures(0, 1, 2);
            ShaderProgram class_59442 = ShaderRenderer.slugFontShader.bindShaderProgram();
            class_59442.getUniform("Weight").set(0.0f);
            class_59442.getUniform("Softness").set(1.0f);
            class_59442.getUniform("EnableFadeout").set(0);
            class_59442.getUniform("FadeoutStart").set(0.0f);
            class_59442.getUniform("FadeoutEnd").set(1.0f);
            class_59442.getUniform("FadeinStart").set(0.0f);
            class_59442.getUniform("FadeinEnd").set(0.0f);
            class_59442.getUniform("MaxWidth").set(0.0f);
            class_59442.getUniform("TextPosX").set(0.0f);
            BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
            for (GlyphEntry glyphEntry : entry.getValue()) {
                if (glyphEntry.uiCoordinates) {
                    moscow.rockstar.render.text.glyph.Glyph glyph = textLayoutManager.getGlyphOrFallback(glyphEntry.codePoint);
                    if (glyph != null) {
                        glyph.appendUiQuad(glyphEntry.transform, class_2872, glyphEntry.x, glyphEntry.y, glyphEntry.fontSize, glyphEntry.color);
                    }
                } else {
                    textLayoutManager.drawGlyph(glyphEntry.transform, class_2872, glyphEntry.codePoint, glyphEntry.x, glyphEntry.y, glyphEntry.fontSize, glyphEntry.color);
                }
            }
            BuiltBuffer class_98012 = class_2872.endNullable();
            if (class_98012 != null) {
                BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
            }
            RenderSystem.setShaderTexture((int)0, (int)0);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderTexturedWidgets() {
        if (this.texturedWidgetBatches.isEmpty()) {
            return;
        }
        if (this.geometryBatch != null) {
            this.renderGeometryTexturedWidgets();
            return;
        }
        boolean bl = flushing;
        flushing = true;
        float[] fArray = (float[])RenderSystem.getShaderColor().clone();
        try {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            for (Map.Entry<ShapeBatchKey, List<TexturedWidgetEntry>> entry : this.texturedWidgetBatches.entrySet()) {
                ShaderRenderer.renderTexturedWidgetBatch(entry.getKey(), entry.getValue());
            }
            this.texturedWidgetBatches.clear();
        }
        finally {
            RenderSystem.setShaderColor((float)fArray[0], (float)fArray[1], (float)fArray[2], (float)fArray[3]);
            flushing = bl;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderTexturedRectangles() {
        if (this.texturedRectangleEntries.isEmpty()) {
            return;
        }
        if (this.geometryBatch != null) {
            this.renderGeometryTexturedRectangles();
            return;
        }
        boolean bl = flushing;
        flushing = true;
        float[] fArray = (float[])RenderSystem.getShaderColor().clone();
        try {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            WidgetBatchRenderer.prepareBlendState();
            RenderSystem.disableCull();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture((int)0, (int)this.activeTextureId);
            BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            for (TexturedRectangleEntry texturedRectangleEntry : this.texturedRectangleEntries) {
                class_2872.vertex(texturedRectangleEntry.transform, texturedRectangleEntry.x, texturedRectangleEntry.y, 0.0f).texture(texturedRectangleEntry.u1, texturedRectangleEntry.v1).color(texturedRectangleEntry.color);
                class_2872.vertex(texturedRectangleEntry.transform, texturedRectangleEntry.x, texturedRectangleEntry.y + texturedRectangleEntry.height, 0.0f).texture(texturedRectangleEntry.u1, texturedRectangleEntry.v2).color(texturedRectangleEntry.color);
                class_2872.vertex(texturedRectangleEntry.transform, texturedRectangleEntry.x + texturedRectangleEntry.width, texturedRectangleEntry.y + texturedRectangleEntry.height, 0.0f).texture(texturedRectangleEntry.u2, texturedRectangleEntry.v2).color(texturedRectangleEntry.color);
                class_2872.vertex(texturedRectangleEntry.transform, texturedRectangleEntry.x + texturedRectangleEntry.width, texturedRectangleEntry.y, 0.0f).texture(texturedRectangleEntry.u2, texturedRectangleEntry.v1).color(texturedRectangleEntry.color);
            }
            BuiltBuffer class_98012 = class_2872.endNullable();
            if (class_98012 != null) {
                BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
            }
            RenderSystem.setShaderTexture((int)0, (int)0);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            this.texturedRectangleEntries.clear();
            this.activeTextureId = 0;
        }
        finally {
            RenderSystem.setShaderColor((float)fArray[0], (float)fArray[1], (float)fArray[2], (float)fArray[3]);
            flushing = bl;
        }
    }

    private void flushTextureBatches() {
        this.renderTexturedWidgets();
        this.renderTexturedRectangles();
    }

    private void emitRoundedRectangleVertices(BufferBuilder class_2872, RoundedRectangleEntry roundedRectangleEntry, float f, boolean bl) {
        float f2 = -f / 2.0f + f * 2.0f;
        float f3 = f / 2.0f + f;
        float f4 = roundedRectangleEntry.x - f2 / 2.0f;
        float f5 = roundedRectangleEntry.y - f3 / 2.0f;
        float f6 = roundedRectangleEntry.width + f2;
        float f7 = roundedRectangleEntry.height + f3;
        float f8 = bl ? -roundedRectangleEntry.width - 4.0f : roundedRectangleEntry.width;
        float f9 = roundedRectangleEntry.height;
        class_2872.vertex(roundedRectangleEntry.transform, f4, f5, 0.0f).texture(f8, f9).color(roundedRectangleEntry.color).light(0);
        class_2872.vertex(roundedRectangleEntry.transform, f4, f5 + f7, 0.0f).texture(f8, f9).color(roundedRectangleEntry.color).light(0);
        class_2872.vertex(roundedRectangleEntry.transform, f4 + f6, f5 + f7, 0.0f).texture(f8, f9).color(roundedRectangleEntry.color).light(0);
        class_2872.vertex(roundedRectangleEntry.transform, f4 + f6, f5, 0.0f).texture(f8, f9).color(roundedRectangleEntry.color).light(0);
    }

    private boolean isBatchEmpty() {
        return this.areShapeAndTextQueuesEmpty() && this.texturedWidgetBatches.isEmpty() && this.texturedRectangleEntries.isEmpty() && (this.geometryBatch == null || !this.geometryBatch.hasQueuedWidgets());
    }

    private void renderGeometryBatches() {
        if (this.geometryBatch == null) {
            return;
        }
        this.renderGeometryEntries();
        this.renderGeometryTexturedWidgets();
        this.renderGeometryTexturedRectangles();
    }

    private void renderGeometryEntries() {
        if (this.geometryBatch == null || this.areShapeAndTextQueuesEmpty()) {
            return;
        }
        for (Map.Entry<WidgetShapeStyle, List<RoundedRectangleEntry>> entry : this.roundedRectangleBatches.entrySet()) {
            WidgetShapeStyle style = entry.getKey();
            WidgetState widgetState = WidgetBatchRenderer.toWidgetState(style);
            for (RoundedRectangleEntry roundedRectangleEntry : entry.getValue()) {
                this.geometryBatch.appendRoundedQuad(roundedRectangleEntry.transform, roundedRectangleEntry.x, roundedRectangleEntry.y, roundedRectangleEntry.width, roundedRectangleEntry.height, widgetState, style.outerSmoothness, style.cornerSmoothness, roundedRectangleEntry.color, roundedRectangleEntry.color, roundedRectangleEntry.color, roundedRectangleEntry.color);
            }
        }
        for (RectangleEntry rectangleEntry : this.rectangleEntries) {
            this.geometryBatch.appendQuad(rectangleEntry.transform, rectangleEntry.x, rectangleEntry.y, rectangleEntry.width, rectangleEntry.height, rectangleEntry.color, rectangleEntry.color, rectangleEntry.color, rectangleEntry.color);
        }
        for (Map.Entry<WidgetShapeStyle, List<RoundedRectangleEntry>> entry : this.borderedRectangleBatches.entrySet()) {
            WidgetShapeStyle style = entry.getKey();
            WidgetState widgetState = WidgetBatchRenderer.toWidgetState(style);
            for (RoundedRectangleEntry roundedRectangleEntry : entry.getValue()) {
                this.geometryBatch.appendTexturedQuad(roundedRectangleEntry.transform, roundedRectangleEntry.x, roundedRectangleEntry.y, roundedRectangleEntry.width, roundedRectangleEntry.height, widgetState, style.borderThickness, style.innerSmoothness, style.outerSmoothness, style.cornerSmoothness, roundedRectangleEntry.color);
            }
        }
        for (Map.Entry<FontRenderer, List<TextEntry>> entry : this.textEntriesByFont.entrySet()) {
            FontRenderer font = entry.getKey();
            for (TextEntry textEntry : entry.getValue()) {
                this.geometryBatch.submitTextLayout(textEntry.transform, font, textEntry.text, textEntry.fontSize, textEntry.x, textEntry.y, textEntry.z, textEntry.color, 0.0f, 0.5f, false, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
            }
        }
        for (Map.Entry<FontRenderer, List<GlyphEntry>> entry : this.glyphEntriesByFont.entrySet()) {
            FontRenderer font = entry.getKey();
            for (GlyphEntry glyphEntry : entry.getValue()) {
                if (glyphEntry.uiCoordinates) {
                    this.geometryBatch.appendIconQuad(glyphEntry.transform, font, glyphEntry.codePoint,
                        glyphEntry.x, glyphEntry.y, glyphEntry.fontSize, glyphEntry.color);
                } else {
                    this.geometryBatch.appendGlyphQuad(glyphEntry.transform, font, glyphEntry.codePoint,
                        glyphEntry.x, glyphEntry.y, glyphEntry.fontSize, glyphEntry.color);
                }
            }
        }
        this.clearShapeAndTextQueues();
    }

    private void renderGeometryTexturedWidgets() {
        if (this.geometryBatch == null || this.texturedWidgetBatches.isEmpty()) {
            return;
        }
        for (Map.Entry<ShapeBatchKey, List<TexturedWidgetEntry>> entry : this.texturedWidgetBatches.entrySet()) {
            ShapeBatchKey shapeBatchKey = entry.getKey();
            WidgetState widgetState = new WidgetState(shapeBatchKey.topLeftRadius, shapeBatchKey.topRightRadius, shapeBatchKey.bottomRightRadius, shapeBatchKey.bottomLeftRadius);
            for (TexturedWidgetEntry texturedWidgetEntry : entry.getValue()) {
                this.geometryBatch.appendGradientQuad(texturedWidgetEntry.transform, shapeBatchKey.textureId, texturedWidgetEntry.x, texturedWidgetEntry.y, texturedWidgetEntry.width, texturedWidgetEntry.height, shapeBatchKey.width, shapeBatchKey.height, widgetState, shapeBatchKey.edgeSmoothness, shapeBatchKey.cornerSmoothness, texturedWidgetEntry.u1, texturedWidgetEntry.v1, texturedWidgetEntry.u2, texturedWidgetEntry.v2, texturedWidgetEntry.color);
            }
        }
        this.texturedWidgetBatches.clear();
    }

    private void renderGeometryTexturedRectangles() {
        if (this.geometryBatch == null || this.texturedRectangleEntries.isEmpty()) {
            return;
        }
        for (TexturedRectangleEntry texturedRectangleEntry : this.texturedRectangleEntries) {
            this.geometryBatch.appendTextureQuad(texturedRectangleEntry.transform, this.activeTextureId, texturedRectangleEntry.x, texturedRectangleEntry.y, texturedRectangleEntry.width, texturedRectangleEntry.height, texturedRectangleEntry.u1, texturedRectangleEntry.v1, texturedRectangleEntry.u2, texturedRectangleEntry.v2, texturedRectangleEntry.color);
        }
        this.texturedRectangleEntries.clear();
        this.activeTextureId = 0;
    }

    private static WidgetState toWidgetState(WidgetShapeStyle widgetShapeStyle) {
        return new WidgetState(widgetShapeStyle.topLeftRadius, widgetShapeStyle.topRightRadius, widgetShapeStyle.bottomRightRadius, widgetShapeStyle.bottomLeftRadius);
    }

    private boolean areShapeAndTextQueuesEmpty() {
        return this.rectangleEntries.isEmpty() && this.roundedRectangleBatches.isEmpty() && this.borderedRectangleBatches.isEmpty() && this.textEntriesByFont.isEmpty() && this.glyphEntriesByFont.isEmpty();
    }

    private void clearAllQueues() {
        this.clearShapeAndTextQueues();
        this.texturedWidgetBatches.clear();
        this.texturedRectangleEntries.clear();
        this.activeTextureId = 0;
    }

    private void clearShapeAndTextQueues() {
        this.rectangleEntries.clear();
        this.roundedRectangleBatches.clear();
        this.borderedRectangleBatches.clear();
        this.textEntriesByFont.clear();
        this.glyphEntriesByFont.clear();
    }

    private int applyShaderColor(int n) {
        float[] fArray = RenderSystem.getShaderColor();
        int n2 = n >>> 24 & 0xFF;
        int n3 = n >>> 16 & 0xFF;
        int n4 = n >>> 8 & 0xFF;
        int n5 = n & 0xFF;
        int n6 = this.clampColorComponent((float)n2 * fArray[3]);
        int n7 = this.clampColorComponent((float)n3 * fArray[0]);
        int n8 = this.clampColorComponent((float)n4 * fArray[1]);
        int n9 = this.clampColorComponent((float)n5 * fArray[2]);
        return n6 << 24 | n7 << 16 | n8 << 8 | n9;
    }

    private int clampColorComponent(float f) {
        return Math.max(0, Math.min(255, Math.round(f)));
    }

    private static boolean hasInvalidSize(float f, float f2) {
        return f <= 0.0f || f2 <= 0.0f;
    }

    private static boolean isTransparent(ColorRGBA colorRGBA) {
        return colorRGBA == null || colorRGBA.getAlpha() <= 0.5f;
    }

    private static WidgetState scaleCornerRadii(WidgetState widgetState, float f) {
        return new WidgetState(widgetState.topLeftRadius() * f, widgetState.topRightRadius() * f, widgetState.bottomRightRadius() * f, widgetState.bottomLeftRadius() * f);
    }

    static {
        sharedGeometryNode = new WidgetStreamRenderer();
        textureRenderingActive = false;
    }

    static final class RectangleEntry {
        final Matrix4f transform;
        final float x;
        final float y;
        final float width;
        final float height;
        final int color;

        RectangleEntry(Matrix4f matrix4f, float f, float f2, float f3, float f4, int n) {
            this.transform = matrix4f;
            this.x = f;
            this.y = f2;
            this.width = f3;
            this.height = f4;
            this.color = n;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "transform", "x", "y", "width", "height", "color");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "transform", "x", "y", "width", "height", "color");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "transform", "x", "y", "width", "height", "color");
        }

        public Matrix4f getTransform() {
            return this.transform;
        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }

        public float getWidth() {
            return this.width;
        }

        public float getHeight() {
            return this.height;
        }

        public int getColor() {
            return this.color;
        }
    }

    static final class TextEntry {
        final Matrix4f transform;
        final String text;
        final float fontSize;
        final float x;
        final float y;
        final float z;
        final int color;

        TextEntry(Matrix4f matrix4f, String string, float f, float f2, float f3, float f4, int n) {
            this.transform = matrix4f;
            this.text = string;
            this.fontSize = f;
            this.x = f2;
            this.y = f3;
            this.z = f4;
            this.color = n;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "transform", "text", "fontSize", "x", "y", "z", "color");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "transform", "text", "fontSize", "x", "y", "z", "color");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "transform", "text", "fontSize", "x", "y", "z", "color");
        }

        public Matrix4f getTransform() {
            return this.transform;
        }

        public String getText() {
            return this.text;
        }

        public float getFontSize() {
            return this.fontSize;
        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }

        public float getZ() {
            return this.z;
        }

        public int getColor() {
            return this.color;
        }
    }

    static final class TexturedRectangleEntry {
        final Matrix4f transform;
        final float x;
        final float y;
        final float width;
        final float height;
        final float u1;
        final float v1;
        final float u2;
        final float v2;
        final int color;

        TexturedRectangleEntry(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int n) {
            this.transform = matrix4f;
            this.x = f;
            this.y = f2;
            this.width = f3;
            this.height = f4;
            this.u1 = f5;
            this.v1 = f6;
            this.u2 = f7;
            this.v2 = f8;
            this.color = n;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "transform", "x", "y", "width", "height", "u1", "v1", "u2", "v2", "color");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "transform", "x", "y", "width", "height", "u1", "v1", "u2", "v2", "color");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "transform", "x", "y", "width", "height", "u1", "v1", "u2", "v2", "color");
        }

        public Matrix4f getTransform() {
            return this.transform;
        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }

        public float getWidth() {
            return this.width;
        }

        public float getHeight() {
            return this.height;
        }

        public float getU1() {
            return this.u1;
        }

        public float getV1() {
            return this.v1;
        }

        public float getU2() {
            return this.u2;
        }

        public float getV2() {
            return this.v2;
        }

        public int getColor() {
            return this.color;
        }
    }

    static final class GlyphEntry {
        final Matrix4f transform;
        final int codePoint;
        final float x;
        final float y;
        final float fontSize;
        final int color;
        final boolean uiCoordinates;

        GlyphEntry(Matrix4f matrix4f, int n, float f, float f2, float f3, int n2, boolean bl) {
            this.transform = matrix4f;
            this.codePoint = n;
            this.x = f;
            this.y = f2;
            this.fontSize = f3;
            this.color = n2;
            this.uiCoordinates = bl;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "transform", "codePoint", "x", "y", "fontSize", "color");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "transform", "codePoint", "x", "y", "fontSize", "color");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "transform", "codePoint", "x", "y", "fontSize", "color");
        }

        public Matrix4f getTransform() {
            return this.transform;
        }

        public int getCodePoint() {
            return this.codePoint;
        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }

        public float getFontSize() {
            return this.fontSize;
        }

        public int getColor() {
            return this.color;
        }

        public boolean isUiCoordinates() {
            return this.uiCoordinates;
        }
    }

    static final class WidgetShapeStyle {
        final float topLeftRadius;
        final float bottomLeftRadius;
        final float topRightRadius;
        final float bottomRightRadius;
        final float borderThickness;
        final float innerSmoothness;
        final float outerSmoothness;
        final float cornerSmoothness;

        private WidgetShapeStyle(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
            this.topLeftRadius = f;
            this.bottomLeftRadius = f2;
            this.topRightRadius = f3;
            this.bottomRightRadius = f4;
            this.borderThickness = f5;
            this.innerSmoothness = f6;
            this.outerSmoothness = f7;
            this.cornerSmoothness = f8;
        }

        static WidgetShapeStyle fromWidgetState(WidgetState widgetState, float f, float f2) {
            return new WidgetShapeStyle(widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius(), 0.0f, f, f, f2);
        }

        static WidgetShapeStyle fromWidgetStateWithSmoothing(WidgetState widgetState, float f, float f2, float f3, float f4) {
            return new WidgetShapeStyle(widgetState.topLeftRadius(), widgetState.bottomLeftRadius(), widgetState.topRightRadius(), widgetState.bottomRightRadius(), f, f2, f3, f4);
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "topLeftRadius", "bottomLeftRadius", "topRightRadius", "bottomRightRadius", "borderThickness", "innerSmoothness", "outerSmoothness", "cornerSmoothness");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "topLeftRadius", "bottomLeftRadius", "topRightRadius", "bottomRightRadius", "borderThickness", "innerSmoothness", "outerSmoothness", "cornerSmoothness");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "topLeftRadius", "bottomLeftRadius", "topRightRadius", "bottomRightRadius", "borderThickness", "innerSmoothness", "outerSmoothness", "cornerSmoothness");
        }

        public float getTopLeftRadius() {
            return this.topLeftRadius;
        }

        public float getBottomLeftRadius() {
            return this.bottomLeftRadius;
        }

        public float getTopRightRadius() {
            return this.topRightRadius;
        }

        public float getBottomRightRadius() {
            return this.bottomRightRadius;
        }

        public float getBorderThickness() {
            return this.borderThickness;
        }

        public float getInnerSmoothness() {
            return this.innerSmoothness;
        }

        public float getOuterSmoothness() {
            return this.outerSmoothness;
        }

        public float getCornerSmoothness() {
            return this.cornerSmoothness;
        }
    }

    static final class RoundedRectangleEntry {
        final Matrix4f transform;
        final float x;
        final float y;
        final float width;
        final float height;
        final int color;

        RoundedRectangleEntry(Matrix4f matrix4f, float f, float f2, float f3, float f4, int n) {
            this.transform = matrix4f;
            this.x = f;
            this.y = f2;
            this.width = f3;
            this.height = f4;
            this.color = n;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "transform", "x", "y", "width", "height", "color");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "transform", "x", "y", "width", "height", "color");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "transform", "x", "y", "width", "height", "color");
        }

        public Matrix4f getTransform() {
            return this.transform;
        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }

        public float getWidth() {
            return this.width;
        }

        public float getHeight() {
            return this.height;
        }

        public int getColor() {
            return this.color;
        }
    }

    public static final class ShapeBatchKey {
        final int textureId;
        private final boolean squircle;
        final float width;
        final float height;
        final float topLeftRadius;
        final float bottomLeftRadius;
        final float topRightRadius;
        final float bottomRightRadius;
        final float edgeSmoothness;
        final float cornerSmoothness;

        public ShapeBatchKey(int n, boolean bl, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
            this.textureId = n;
            this.squircle = bl;
            this.width = f;
            this.height = f2;
            this.topLeftRadius = f3;
            this.bottomLeftRadius = f4;
            this.topRightRadius = f5;
            this.bottomRightRadius = f6;
            this.edgeSmoothness = f7;
            this.cornerSmoothness = f8;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "textureId", "squircle", "width", "height", "topLeftRadius", "bottomLeftRadius", "topRightRadius", "bottomRightRadius", "edgeSmoothness", "cornerSmoothness");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "textureId", "squircle", "width", "height", "topLeftRadius", "bottomLeftRadius", "topRightRadius", "bottomRightRadius", "edgeSmoothness", "cornerSmoothness");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "textureId", "squircle", "width", "height", "topLeftRadius", "bottomLeftRadius", "topRightRadius", "bottomRightRadius", "edgeSmoothness", "cornerSmoothness");
        }

        public int getTextureId() {
            return this.textureId;
        }

        public boolean isSquircle() {
            return this.squircle;
        }

        public float getWidth() {
            return this.width;
        }

        public float getHeight() {
            return this.height;
        }

        public float getTopLeftRadius() {
            return this.topLeftRadius;
        }

        public float getBottomLeftRadius() {
            return this.bottomLeftRadius;
        }

        public float getTopRightRadius() {
            return this.topRightRadius;
        }

        public float getBottomRightRadius() {
            return this.bottomRightRadius;
        }

        public float getEdgeSmoothness() {
            return this.edgeSmoothness;
        }

        public float getCornerSmoothness() {
            return this.cornerSmoothness;
        }
    }

    public static final class TexturedWidgetEntry {
        final Matrix4f transform;
        final float x;
        final float y;
        final float width;
        final float height;
        final float u1;
        final float v1;
        final float u2;
        final float v2;
        final int color;

        public TexturedWidgetEntry(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int n) {
            this.transform = matrix4f;
            this.x = f;
            this.y = f2;
            this.width = f3;
            this.height = f4;
            this.u1 = f5;
            this.v1 = f6;
            this.u2 = f7;
            this.v2 = f8;
            this.color = n;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "transform", "x", "y", "width", "height", "u1", "v1", "u2", "v2", "color");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "transform", "x", "y", "width", "height", "u1", "v1", "u2", "v2", "color");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "transform", "x", "y", "width", "height", "u1", "v1", "u2", "v2", "color");
        }

        public Matrix4f getTransform() {
            return this.transform;
        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }

        public float getWidth() {
            return this.width;
        }

        public float getHeight() {
            return this.height;
        }

        public float getU1() {
            return this.u1;
        }

        public float getV1() {
            return this.v1;
        }

        public float getU2() {
            return this.u2;
        }

        public float getV2() {
            return this.v2;
        }

        public int getColor() {
            return this.color;
        }
    }
}
