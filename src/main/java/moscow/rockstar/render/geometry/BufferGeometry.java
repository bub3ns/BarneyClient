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
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 */
package moscow.rockstar.render.geometry;

import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import moscow.rockstar.render.batch.WidgetBatchRenderer;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.render.text.FontRenderer;
import moscow.rockstar.render.text.icon.SvgIconRegistry;
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
import org.joml.Vector3f;

public class BufferGeometry {
    private static final float ANCHOR_ORIGIN_X = 0.0f;
    private static final float ANCHOR_HALF_SIZE = 0.5f;
    private static final float ANCHOR_ORIGIN_Y = 0.0f;
    private final Map<Anchor, List<Rectangle>> rectangleBatches = new LinkedHashMap<Anchor, List<Rectangle>>();
    private final Map<FontRenderer, Map<Anchor, List<TexturedLabel>>> textLabelBatches = new LinkedHashMap<FontRenderer, Map<Anchor, List<TexturedLabel>>>();
    private final Map<Anchor, List<GlyphQuad>> glyphBatches = new LinkedHashMap<Anchor, List<GlyphQuad>>();
    private Anchor currentAnchor;
    private float cornerRadius;
    private float glowRadius = 3.0f;
    private float glowStrength = 0.3f;
    private float glowSoftness = 0.42f;
    private int lightColor = -1;
    private int darkColor = -15856114;

    public BufferGeometry setCornerRadius(float f) {
        this.cornerRadius = f;
        return this;
    }

    public BufferGeometry setGlowRadius(float f) {
        this.glowRadius = f;
        return this;
    }

    public BufferGeometry setGlowParameters(float f, float f2) {
        this.glowStrength = f;
        this.glowSoftness = f2;
        return this;
    }

    public BufferGeometry setLightAndDarkColors(int n, int n2) {
        this.lightColor = n;
        this.darkColor = n2;
        return this;
    }

    public BufferGeometry setAnchorTransform(Matrix4f matrix4f, float f, float f2) {
        this.currentAnchor = BufferGeometry.createAnchor(matrix4f, f, f2);
        return this;
    }

    public BufferGeometry clearAnchorOverride() {
        this.currentAnchor = null;
        return this;
    }

    public BufferGeometry addTexturedLabel(FontRenderer textLayoutManager2, String string, float f, Matrix4f matrix4f, float f2, float f3, float f4) {
        if (textLayoutManager2 == null || string == null || string.isEmpty() || f4 <= 0.0f) {
            return this;
        }
        Anchor anchor2 = this.getAnchor(matrix4f, f2 + textLayoutManager2.measure(string, f) / 2.0f, f3 + textLayoutManager2.getLineHeight(f) / 2.0f);
        this.textLabelBatches
            .computeIfAbsent(textLayoutManager2, ignored -> new LinkedHashMap<Anchor, List<TexturedLabel>>())
            .computeIfAbsent(anchor2, ignored -> new ArrayList<TexturedLabel>())
            .add(new TexturedLabel(new Matrix4f((Matrix4fc)matrix4f), string, f, f2, f3, f4));
        return this;
    }

    public BufferGeometry addRectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5) {
        if (f3 <= 0.0f || f4 <= 0.0f || f5 <= 0.0f) {
            return this;
        }
        this.rectangleBatches.computeIfAbsent(this.getAnchor(matrix4f, f + f3 / 2.0f, f2 + f4 / 2.0f), anchor -> new ArrayList()).add(new Rectangle(new Matrix4f((Matrix4fc)matrix4f), f, f2, f3, f4, f5));
        return this;
    }

    /**
     * ORIGINAL: {@code rockstar/ilIlil/iIiiiiiiI#I (Ljava/lang/String;Lorg/joml/Matrix4f;FFFF)Lrockstar/ilIlil/iIiiiiiiI;}
     * <pre>
     *   Integer cp = IIiiII.I(name);
     *   if (cp == null || size &lt;= 0F || alpha &lt;= 0F || !IIiiII.I()) return this;
     *   batches.computeIfAbsent(anchor(m, x + size/2, y + size/2), ...).add(new i(new Matrix4f(m), cp.intValue(), x, y, size, alpha));
     * </pre>
     * {@code IIiiII} is {@link SvgIconRegistry}: {@code I(String)Ljava/lang/Integer;} is
     * {@link SvgIconRegistry#getCodePoint(String)} and {@code I()Z} is
     * {@link SvgIconRegistry#isLoaded()}. The argument is an ICON NAME, not text.
     */
    public BufferGeometry addGlyph(String string, Matrix4f matrix4f, float f, float f2, float f3, float f4) {
        Integer n = SvgIconRegistry.getCodePoint(string);
        if (n == null || f3 <= 0.0f || f4 <= 0.0f || !SvgIconRegistry.isLoaded()) {
            return this;
        }
        this.glyphBatches.computeIfAbsent(this.getAnchor(matrix4f, f + f3 / 2.0f, f2 + f3 / 2.0f), anchor -> new ArrayList()).add(new GlyphQuad(new Matrix4f((Matrix4fc)matrix4f), n.intValue(), f, f2, f3, f4));
        return this;
    }

    private Anchor getAnchor(Matrix4f matrix4f, float f, float f2) {
        return this.currentAnchor != null ? this.currentAnchor : BufferGeometry.createAnchor(matrix4f, f, f2);
    }

    private static Anchor createAnchor(Matrix4f matrix4f, float f, float f2) {
        Vector3f vector3f = matrix4f.transformPosition(new Vector3f(f, f2, 0.0f));
        return new Anchor(vector3f.x, vector3f.y);
    }

    public void flushBatches() {
        this.currentAnchor = null;
        if (this.rectangleBatches.isEmpty() && this.textLabelBatches.isEmpty() && this.glyphBatches.isEmpty()) {
            return;
        }
        WidgetBatchRenderer.flushCurrentBatch();
        ShaderRenderer.uiRootComponent.update(this.glowStrength, this.glowSoftness, this.glowRadius);
        WidgetBatchRenderer.prepareBlendState();
        RenderSystem.disableCull();
        moscow.rockstar.render.state.RenderStateSupport.bindGlyphAtlasTextures(0, 2, 3);
        RenderSystem.setShaderTexture((int)1, (int)ShaderRenderer.uiRootComponent.textureId());
        ShaderProgram class_59442 = ShaderRenderer.adaptiveUiShader.bindShaderProgram();
        BufferGeometry.applyColorUniform(class_59442, "LightColor", this.lightColor);
        BufferGeometry.applyColorUniform(class_59442, "DarkColor", this.darkColor);
        class_59442.getUniform("Radius").set(this.cornerRadius, this.cornerRadius, this.cornerRadius, this.cornerRadius);
        class_59442.getUniform("RectSmoothness").set(0.5f);
        for (Map.Entry<Anchor, List<Rectangle>> object2 : this.rectangleBatches.entrySet()) {
            BufferGeometry.applyAnchorUniform(class_59442, object2.getKey());
            BufferBuilder class_2872 = BufferGeometry.beginQuadBuffer();
            this.drawRectangles(class_2872, object2.getValue());
            BufferGeometry.uploadQuadBuffer(class_2872);
        }
        for (Map.Entry<FontRenderer, Map<Anchor, List<TexturedLabel>>> entry : this.textLabelBatches.entrySet()) {
            FontRenderer textLayoutManager = entry.getKey();
            class_59442.getUniform("Thickness").set(0.0f);
            for (Map.Entry<Anchor, List<TexturedLabel>> entry2 : entry.getValue().entrySet()) {
                BufferGeometry.applyAnchorUniform(class_59442, entry2.getKey());
                BufferBuilder object = BufferGeometry.beginQuadBuffer();
                for (TexturedLabel texturedLabel : entry2.getValue()) {
                    textLayoutManager.draw(texturedLabel.transform, object, texturedLabel.text, texturedLabel.fontSize, texturedLabel.x, texturedLabel.y, 0.0f, BufferGeometry.packAlpha(texturedLabel.alpha));
                }
                BufferGeometry.uploadQuadBuffer(object);
            }
        }
        // ORIGINAL label J of iIiiiiiiI#I()V: `if (!II.isEmpty()) { IIiiii f = IIiiII.I(); if (f != null) {...} }`
        // - the glyph batch is drawn with the SVG ICON font, never with the Noto fallback.
        if (!this.glyphBatches.isEmpty()) {
            FontRenderer iconFont = SvgIconRegistry.getFont();
            if (iconFont != null) {
                class_59442.getUniform("Thickness").set(0.0f);
                for (Map.Entry<Anchor, List<GlyphQuad>> entry : this.glyphBatches.entrySet()) {
                    BufferGeometry.applyAnchorUniform(class_59442, entry.getKey());
                    BufferBuilder glyphBuffer = BufferGeometry.beginQuadBuffer();
                    for (GlyphQuad glyph : entry.getValue()) {
                        iconFont.drawGlyph(glyph.transform, glyphBuffer, glyph.codePoint, glyph.x, glyph.y, glyph.fontSize, BufferGeometry.packAlpha(glyph.alpha));
                    }
                    BufferGeometry.uploadQuadBuffer(glyphBuffer);
                }
            }
        }
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.setShaderTexture((int)1, (int)0);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        this.rectangleBatches.clear();
        this.textLabelBatches.clear();
        this.glyphBatches.clear();
    }

    private void drawRectangles(BufferBuilder class_2872, List<Rectangle> list) {
        float f = 0.5f;
        float f2 = -f / 2.0f + f * 2.0f;
        float f3 = f / 2.0f + f;
        for (Rectangle rectangle : list) {
            float f4 = rectangle.x - f2 / 2.0f;
            float f5 = rectangle.y - f3 / 2.0f;
            float f6 = rectangle.width + f2;
            float f7 = rectangle.height + f3;
            int n = BufferGeometry.packAlpha(rectangle.alpha);
            class_2872.vertex(rectangle.transform, f4, f5, 0.0f).texture(rectangle.width, rectangle.height).color(n).light(0);
            class_2872.vertex(rectangle.transform, f4, f5 + f7, 0.0f).texture(rectangle.width, rectangle.height).color(n).light(0);
            class_2872.vertex(rectangle.transform, f4 + f6, f5 + f7, 0.0f).texture(rectangle.width, rectangle.height).color(n).light(0);
            class_2872.vertex(rectangle.transform, f4 + f6, f5, 0.0f).texture(rectangle.width, rectangle.height).color(n).light(0);
        }
    }

    private static BufferBuilder beginQuadBuffer() {
        return Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
    }

    private static void uploadQuadBuffer(BufferBuilder class_2872) {
        BuiltBuffer class_98012 = class_2872.endNullable();
        if (class_98012 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
        }
    }

    private static int packAlpha(float f) {
        int n = Math.round(Math.min(1.0f, Math.max(0.0f, f)) * 255.0f);
        return n << 24 | 0xFFFFFF;
    }

    private static void applyAnchorUniform(ShaderProgram class_59442, Anchor anchor) {
        class_59442.getUniform("Anchor").set(anchor.getX(), anchor.getY());
    }

    private static void applyColorUniform(ShaderProgram class_59442, String string, int n) {
        class_59442.getUniform(string).set((float)(n >> 16 & 0xFF) / 255.0f, (float)(n >> 8 & 0xFF) / 255.0f, (float)(n & 0xFF) / 255.0f, (float)(n >>> 24 & 0xFF) / 255.0f);
    }

    static final class Anchor {
        private final float x;
        private final float y;

        Anchor(float f, float f2) {
            this.x = f;
            this.y = f2;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "x", "y");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "x", "y");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "x", "y");
        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }
    }

    static final class TexturedLabel {
        final Matrix4f transform;
        final String text;
        final float fontSize;
        final float x;
        final float y;
        final float alpha;

        TexturedLabel(Matrix4f matrix4f, String string, float f, float f2, float f3, float f4) {
            this.transform = matrix4f;
            this.text = string;
            this.fontSize = f;
            this.x = f2;
            this.y = f3;
            this.alpha = f4;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "transform", "text", "fontSize", "x", "y", "alpha");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "transform", "text", "fontSize", "x", "y", "alpha");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "transform", "text", "fontSize", "x", "y", "alpha");
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

        public float getAlpha() {
            return this.alpha;
        }
    }

    static final class Rectangle {
        final Matrix4f transform;
        final float x;
        final float y;
        final float width;
        final float height;
        final float alpha;

        Rectangle(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5) {
            this.transform = matrix4f;
            this.x = f;
            this.y = f2;
            this.width = f3;
            this.height = f4;
            this.alpha = f5;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "transform", "x", "y", "width", "height", "alpha");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "transform", "x", "y", "width", "height", "alpha");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "transform", "x", "y", "width", "height", "alpha");
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

        public float getAlpha() {
            return this.alpha;
        }
    }

    static final class GlyphQuad {
        final Matrix4f transform;
        final int codePoint;
        final float x;
        final float y;
        final float fontSize;
        final float alpha;

        GlyphQuad(Matrix4f matrix4f, int n, float f, float f2, float f3, float f4) {
            this.transform = matrix4f;
            this.codePoint = n;
            this.x = f;
            this.y = f2;
            this.fontSize = f3;
            this.alpha = f4;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "transform", "codePoint", "x", "y", "fontSize", "alpha");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "transform", "codePoint", "x", "y", "fontSize", "alpha");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "transform", "codePoint", "x", "y", "fontSize", "alpha");
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

        public float getAlpha() {
            return this.alpha;
        }
    }
}
