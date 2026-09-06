/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Text
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tessellator
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.DrawContext
 *  net.minecraft.VertexConsumer
 *  net.minecraft.ShaderProgram
 *  net.minecraft.BuiltBuffer
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package moscow.rockstar.render.text;

import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.render.colors.ColorPalette;
import moscow.rockstar.render.shaders.ShaderRenderer;
import net.minecraft.text.Text;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public class WorldTextBatch {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private final FontRenderer normalTextLayout;
    private final FontRenderer shadowTextLayout;
    private final List<WorldTextElement> textElements = new ArrayList<WorldTextElement>();
    private final List<WorldTextElement> shadowTextElements = new ArrayList<WorldTextElement>();
    private boolean hasShadowText;
    private DrawContext drawContext;

    public WorldTextBatch(FontRenderer textLayoutManager, FontRenderer textLayoutManager2) {
        this.normalTextLayout = textLayoutManager;
        this.shadowTextLayout = textLayoutManager2;
    }

    public void setDrawContext(DrawContext ServerConfigException) {
        this.drawContext = ServerConfigException;
    }

    public void queueText(Matrix4f matrix4f, String string, float f, float f2, float f3, float f4, int n) {
        Matrix4f matrix4f2 = new Matrix4f((Matrix4fc)matrix4f);
        String string2 = TextCaptureController.transform(string);
        this.textElements.add(new WorldTextElement(matrix4f2, string2, f, f2, f3, f4, n));
        if (TextCaptureController.isCaptureActive()) {
            this.shadowTextElements.add(string2 == string ? this.textElements.getLast() : new WorldTextElement(matrix4f2, string, f, f2, f3, f4, n));
            this.hasShadowText |= string2 != string;
        }
    }

    public void queueText(Matrix4f matrix4f, Text class_25612, float f, float f2, float f3, float f4) {
        int n = ColorPalette.WHITE.getRGB();
        List<TextComponentRun> runs = TextComponentRuns.flatten(class_25612, n);
        boolean bl = TextCaptureController.isCaptureActive();
        float f5 = f2;
        float f6 = f2;
        Matrix4f matrix4f2 = new Matrix4f((Matrix4fc)matrix4f);
        for (TextComponentRun run : runs) {
            String originalText = run.text();
            String string = TextCaptureController.transform(originalText);
            this.textElements.add(new WorldTextElement(matrix4f2, string, f, f5, f3, f4, run.color()));
            f5 += this.getTextWidthForLayout(string, f);
            if (!bl) continue;
            this.shadowTextElements.add(new WorldTextElement(matrix4f2, originalText, f, f6, f3, f4, run.color()));
            f6 += this.getTextWidthForLayout(originalText, f);
            this.hasShadowText |= !string.equals(originalText);
        }
    }

    public void flush() {
        if (this.textElements.isEmpty()) {
            return;
        }
        if (this.hasShadowText) {
            TextCaptureController.renderClipped(this.getShadowTextBounds(), () -> this.renderTextList(this.shadowTextElements));
        }
        this.renderTextList(this.textElements);
        this.textElements.clear();
        this.shadowTextElements.clear();
        this.hasShadowText = false;
    }

    private int[] getShadowTextBounds() {
        int n = -1;
        for (int i = 0; i < this.shadowTextElements.size() && n < 0; ++i) {
            WorldTextElement worldTextElement;
            WorldTextElement worldTextElement2 = worldTextElement = i < this.textElements.size() ? this.textElements.get(i) : this.shadowTextElements.get(i);
            if (this.shadowTextElements.get((int)i).text.equals(worldTextElement.text)) continue;
            n = i;
        }
        if (n < 0) {
            return null;
        }
        int[] nArray = null;
        for (int i = n; i < this.shadowTextElements.size(); ++i) {
            nArray = TextCaptureController.union(nArray, this.getTextBounds(this.shadowTextElements.get(i)));
            if (i >= this.textElements.size()) continue;
            nArray = TextCaptureController.union(nArray, this.getTextBounds(this.textElements.get(i)));
        }
        return nArray;
    }

    private int[] getTextBounds(WorldTextElement worldTextElement) {
        return TextCaptureController.transformBounds(worldTextElement.matrix, worldTextElement.x - 1.0f, worldTextElement.y - worldTextElement.size * 0.35f, worldTextElement.x + this.getTextWidthForLayout(worldTextElement.text, worldTextElement.size) + 1.0f, worldTextElement.y + worldTextElement.size * 1.45f);
    }

    private void renderTextList(List<WorldTextElement> list) {
        float f = 0.0f;
        float f2 = 0.5f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        moscow.rockstar.render.state.RenderStateSupport.bindGlyphAtlasTextures(0, 1, 2);
        ShaderProgram class_59442 = ShaderRenderer.slugFontShader.bindShaderProgram();
        class_59442.getUniform("Weight").set(0.0f);
        class_59442.getUniform("Softness").set(1.0f);
        class_59442.getUniform("EnableFadeout").set(0);
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        for (WorldTextElement worldTextElement : list) {
            this.normalTextLayout.draw(worldTextElement.matrix, class_2872, worldTextElement.text, worldTextElement.size, worldTextElement.x, worldTextElement.y, worldTextElement.z, worldTextElement.color);
        }
        BuiltBuffer class_98012 = class_2872.endNullable();
        if (class_98012 != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_98012);
        }
        moscow.rockstar.render.state.RenderStateSupport.finishBatch();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        if (this.drawContext != null) {
            this.renderShadowTextList(list);
        }
    }

    private void renderShadowTextList(List<WorldTextElement> list) {
        for (WorldTextElement worldTextElement : list) {
            float f = worldTextElement.size / 9.0f;
            this.normalTextLayout.visitGlyphs(worldTextElement.text, worldTextElement.size, worldTextElement.x, worldTextElement.y, this.shadowTextLayout, (font, glyph, character, glyphX, glyphY) -> {
                if (glyph != null) {
                    return;
                }
                this.drawContext.getMatrices().push();
                this.drawContext.getMatrices().multiplyPositionMatrix(worldTextElement.matrix);
                this.drawContext.getMatrices().scale(f, f, 1.0f);
                float f4 = glyphX / f;
                float f5 = (worldTextElement.y + worldTextElement.size * 0.15f) / f - 0.5f;
                this.drawContext.drawText(WorldTextBatch.CLIENT.textRenderer, String.valueOf(character), (int)f4, (int)f5, worldTextElement.color, false);
                this.drawContext.getMatrices().pop();
            });
        }
    }

    public float getTextWidth(String string, float f) {
        return WorldTextBatch.getTextWidthWithLayouts(this.normalTextLayout, this.shadowTextLayout, string, f);
    }

    public float getTextWidth(Text class_25612, float f) {
        return WorldTextBatch.getTextWidthWithLayouts(this.normalTextLayout, this.shadowTextLayout, class_25612.getString(), f);
    }

    private float getTextWidthForLayout(String string, float f) {
        return this.normalTextLayout.measure(string, f);
    }

    public static float getTextWidthWithLayouts(FontRenderer textLayoutManager, FontRenderer textLayoutManager2, String string, float f) {
        return textLayoutManager.measure(TextCaptureController.transform(string), f);
    }

    static final class WorldTextElement {
        final Matrix4f matrix;
        final String text;
        final float size;
        final float x;
        final float y;
        final float z;
        final int color;

        WorldTextElement(Matrix4f matrix4f, String string, float f, float f2, float f3, float f4, int n) {
            this.matrix = matrix4f;
            this.text = string;
            this.size = f;
            this.x = f2;
            this.y = f3;
            this.z = f4;
            this.color = n;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "matrix", "text", "size", "x", "y", "z", "color");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "matrix", "text", "size", "x", "y", "z", "color");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "matrix", "text", "size", "x", "y", "z", "color");
        }

        public Matrix4f getMatrix() {
            return this.matrix;
        }

        public String getText() {
            return this.text;
        }

        public float getSize() {
            return this.size;
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
}
