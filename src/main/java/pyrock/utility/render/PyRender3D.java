/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  jep.python.PyObject
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Entity
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tessellator
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.Identifier
 *  net.minecraft.MatrixStack
 *  net.minecraft.MatrixStack$Entry
 *  net.minecraft.VertexConsumer
 *  net.minecraft.ShaderProgram
 *  net.minecraft.RotationAxis
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package pyrock.utility.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import jep.python.PyObject;
import moscow.rockstar.render.item.ItemRenderUtils;
import moscow.rockstar.render.util.RenderUtils;
import moscow.rockstar.render.text.FontRenderer;
import moscow.rockstar.render.shaders.ShaderRenderer;
import moscow.rockstar.ui.text.Font;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pyrock.events.render.Render3DEvent;
import pyrock.utility.render.ColorRGBA;
import pyrock.utility.render.PyAssets;

public class PyRender3D {
    private static final int BILLBOARD_STRIDE = 8;
    private static final int LINE_STRIDE = 10;
    private static final int TEXT_STRIDE = 11;
    private static final float TEXT_SIZE = 32.0f;
    private static final float TEXT_SOFTNESS = 0.5f;
    private static final float[] OUTLINE_STEPS = new float[]{-1.0f, 0.0f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, -0.7071f, -0.7071f, 0.7071f, -0.7071f, -0.7071f, 0.7071f, 0.7071f, 0.7071f};
    private static final double[] EMPTY = new double[0];

    public void line(Render3DEvent render3DEvent, double d, double d2, double d3, double d4, double d5, double d6, ColorRGBA colorRGBA) {
        this.withLines(render3DEvent, true, class_2872 -> RenderUtils.drawLineSegment(render3DEvent.getMatrices(), class_2872, new Vec3d(d, d2, d3), new Vec3d(d4, d5, d6), this.safeColor(colorRGBA)));
    }

    public void marker(Render3DEvent render3DEvent, double d, double d2, double d3, double d4, ColorRGBA colorRGBA) {
        double d5 = Math.max(0.01, d4);
        ColorRGBA colorRGBA2 = this.safeColor(colorRGBA);
        this.withLines(render3DEvent, true, class_2872 -> {
            MatrixStack class_45872 = render3DEvent.getMatrices();
            Vec3d VanillaChestLootTableGenerator = new Vec3d(d, d2, d3);
            RenderUtils.drawLineSegment(class_45872, class_2872, VanillaChestLootTableGenerator.add(-d5, 0.0, 0.0), VanillaChestLootTableGenerator.add(d5, 0.0, 0.0), colorRGBA2);
            RenderUtils.drawLineSegment(class_45872, class_2872, VanillaChestLootTableGenerator.add(0.0, -d5, 0.0), VanillaChestLootTableGenerator.add(0.0, d5, 0.0), colorRGBA2);
            RenderUtils.drawLineSegment(class_45872, class_2872, VanillaChestLootTableGenerator.add(0.0, 0.0, -d5), VanillaChestLootTableGenerator.add(0.0, 0.0, d5), colorRGBA2);
        });
    }

    public void box(Render3DEvent render3DEvent, Object object, ColorRGBA colorRGBA) {
        Box HorizontalFacingBlock = this.boxOf(object);
        if (HorizontalFacingBlock == null) {
            return;
        }
        this.withLines(render3DEvent, true, class_2872 -> RenderUtils.drawBoxOutline(render3DEvent.getMatrices(), class_2872, HorizontalFacingBlock, this.safeColor(colorRGBA)));
    }

    public void boxGradient(Render3DEvent render3DEvent, Object object, ColorRGBA colorRGBA, ColorRGBA colorRGBA2) {
        Box HorizontalFacingBlock = this.boxOf(object);
        if (HorizontalFacingBlock == null) {
            return;
        }
        this.withLines(render3DEvent, true, class_2872 -> RenderUtils.drawGradientBoxOutline(render3DEvent.getMatrices(), class_2872, HorizontalFacingBlock, this.safeColor(colorRGBA), this.safeColor(colorRGBA2)));
    }

    public void filledBox(Render3DEvent render3DEvent, Object object, ColorRGBA colorRGBA) {
        Box HorizontalFacingBlock = this.boxOf(object);
        if (HorizontalFacingBlock == null) {
            return;
        }
        this.withQuads(render3DEvent, false, class_2872 -> RenderUtils.drawFilledBox(render3DEvent.getMatrices(), class_2872, HorizontalFacingBlock, this.safeColor(colorRGBA)));
    }

    public void filledBoxGradient(Render3DEvent render3DEvent, Object object, ColorRGBA colorRGBA, ColorRGBA colorRGBA2) {
        Box HorizontalFacingBlock = this.boxOf(object);
        if (HorizontalFacingBlock == null) {
            return;
        }
        this.withQuads(render3DEvent, false, class_2872 -> RenderUtils.drawGradientBox(render3DEvent.getMatrices(), class_2872, HorizontalFacingBlock, this.safeColor(colorRGBA), this.safeColor(colorRGBA2)));
    }

    public void glowingBox(Render3DEvent render3DEvent, Object object, ColorRGBA colorRGBA) {
        Box HorizontalFacingBlock = this.boxOf(object);
        if (HorizontalFacingBlock == null) {
            return;
        }
        this.withQuads(render3DEvent, true, class_2872 -> RenderUtils.drawGlowingBox(render3DEvent.getMatrices(), class_2872, HorizontalFacingBlock, this.safeColor(colorRGBA)));
    }

    public void boxAt(Render3DEvent render3DEvent, double d, double d2, double d3, double d4, double d5, double d6, ColorRGBA colorRGBA) {
        this.box(render3DEvent, new Box(d, d2, d3, d + d4, d2 + d5, d3 + d6), colorRGBA);
    }

    public void filledBoxAt(Render3DEvent render3DEvent, double d, double d2, double d3, double d4, double d5, double d6, ColorRGBA colorRGBA) {
        this.filledBox(render3DEvent, new Box(d, d2, d3, d + d4, d2 + d5, d3 + d6), colorRGBA);
    }

    public void ring(Render3DEvent render3DEvent, Object object, double d, double d2, int n, ColorRGBA colorRGBA) {
        Vec3d VanillaChestLootTableGenerator = this.centerOf(object);
        if (VanillaChestLootTableGenerator == null) {
            return;
        }
        int n2 = Math.max(8, n);
        double radius = Math.max(0.01, d);
        double ringHeight = VanillaChestLootTableGenerator.y + d2;
        ColorRGBA colorRGBA2 = this.safeColor(colorRGBA);
        this.withLines(render3DEvent, true, class_2872 -> {
            MatrixStack class_45872 = render3DEvent.getMatrices();
            for (int i = 0; i < n2; ++i) {
                double startAngle = Math.PI * 2 * (double)i / (double)n2;
                double endAngle = Math.PI * 2 * (double)(i + 1) / (double)n2;
                Vec3d startPoint = new Vec3d(VanillaChestLootTableGenerator.x + Math.cos(startAngle) * radius, ringHeight, VanillaChestLootTableGenerator.z + Math.sin(startAngle) * radius);
                Vec3d endPoint = new Vec3d(VanillaChestLootTableGenerator.x + Math.cos(endAngle) * radius, ringHeight, VanillaChestLootTableGenerator.z + Math.sin(endAngle) * radius);
                RenderUtils.drawLineSegment(class_45872, class_2872, startPoint, endPoint, colorRGBA2);
            }
        });
    }

    public void target(Render3DEvent render3DEvent, Object object, ColorRGBA colorRGBA) {
        Box HorizontalFacingBlock = this.boxOf(object);
        if (HorizontalFacingBlock == null) {
            return;
        }
        ColorRGBA colorRGBA2 = this.safeColor(colorRGBA);
        ColorRGBA colorRGBA3 = colorRGBA2.mulAlpha(0.16f);
        this.filledBox(render3DEvent, HorizontalFacingBlock, colorRGBA3);
        this.box(render3DEvent, HorizontalFacingBlock, colorRGBA2);
        this.marker(render3DEvent, HorizontalFacingBlock.getCenter().x, HorizontalFacingBlock.maxY + 0.25, HorizontalFacingBlock.getCenter().z, 0.22, colorRGBA2);
        this.ring(render3DEvent, HorizontalFacingBlock, Math.max(HorizontalFacingBlock.getLengthX(), HorizontalFacingBlock.getLengthZ()) * 0.75, -HorizontalFacingBlock.getLengthY() * 0.5 + 0.04, 48, colorRGBA2);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void billboard(Render3DEvent render3DEvent, Identifier class_29602, double d, double d2, double d3, double d4, double d5, double d6, ColorRGBA colorRGBA, boolean bl) {
        if (render3DEvent == null || class_29602 == null || d4 <= 0.0 || d5 <= 0.0) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        class_45872.push();
        try {
            ItemRenderUtils.beginOverlayRendering(bl);
            ItemRenderUtils.translateToCamera(class_45872);
            class_45872.translate(d, d2, d3);
            class_45872.multiply(render3DEvent.getCamera().getRotation());
            if (d6 != 0.0) {
                class_45872.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)d6));
            }
            RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
            BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
            float f = (float)(d4 / 2.0);
            float f2 = (float)(d5 / 2.0);
            int n = this.safeColor(colorRGBA).getRGB();
            class_2872.vertex(matrix4f, -f, -f2, 0.0f).texture(0.0f, 1.0f).color(n);
            class_2872.vertex(matrix4f, f, -f2, 0.0f).texture(1.0f, 1.0f).color(n);
            class_2872.vertex(matrix4f, f, f2, 0.0f).texture(1.0f, 0.0f).color(n);
            class_2872.vertex(matrix4f, -f, f2, 0.0f).texture(0.0f, 0.0f).color(n);
            ItemRenderUtils.flushVertexConsumer(class_2872);
        }
        finally {
            RenderSystem.setShaderTexture((int)0, (int)0);
            ItemRenderUtils.endOverlayRendering();
            class_45872.pop();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void billboards(Render3DEvent render3DEvent, Identifier class_29602, Object object, boolean bl) {
        if (render3DEvent == null || class_29602 == null) {
            return;
        }
        double[] dArray = PyRender3D.unpack(object, "billboards");
        if (dArray.length < 8) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        class_45872.push();
        try {
            ItemRenderUtils.beginOverlayRendering(bl);
            ItemRenderUtils.translateToCamera(class_45872);
            RenderSystem.setShaderTexture((int)0, (Identifier)class_29602);
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
            Quaternionf quaternionf = render3DEvent.getCamera().getRotation();
            Vector3f vector3f = quaternionf.transform(new Vector3f(1.0f, 0.0f, 0.0f));
            Vector3f vector3f2 = quaternionf.transform(new Vector3f(0.0f, 1.0f, 0.0f));
            Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
            BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            int n = 0;
            while (n + 8 <= dArray.length) {
                int n2;
                float f = (float)dArray[n + 3] * 0.5f;
                if (!(f <= 0.0f) && (n2 = PyRender3D.packColor(dArray[n + 4], dArray[n + 5], dArray[n + 6], dArray[n + 7])) >>> 24 != 0) {
                    float f2 = (float)dArray[n];
                    float f3 = (float)dArray[n + 1];
                    float f4 = (float)dArray[n + 2];
                    float f5 = vector3f.x * f;
                    float f6 = vector3f.y * f;
                    float f7 = vector3f.z * f;
                    float f8 = vector3f2.x * f;
                    float f9 = vector3f2.y * f;
                    float f10 = vector3f2.z * f;
                    class_2872.vertex(matrix4f, f2 - f5 - f8, f3 - f6 - f9, f4 - f7 - f10).texture(0.0f, 1.0f).color(n2);
                    class_2872.vertex(matrix4f, f2 + f5 - f8, f3 + f6 - f9, f4 + f7 - f10).texture(1.0f, 1.0f).color(n2);
                    class_2872.vertex(matrix4f, f2 + f5 + f8, f3 + f6 + f9, f4 + f7 + f10).texture(1.0f, 0.0f).color(n2);
                    class_2872.vertex(matrix4f, f2 - f5 + f8, f3 - f6 + f9, f4 - f7 + f10).texture(0.0f, 0.0f).color(n2);
                }
                n += 8;
            }
            ItemRenderUtils.flushVertexConsumer(class_2872);
        }
        finally {
            RenderSystem.setShaderTexture((int)0, (int)0);
            ItemRenderUtils.endOverlayRendering();
            class_45872.pop();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void lines(Render3DEvent render3DEvent, Object object, boolean bl) {
        if (render3DEvent == null) {
            return;
        }
        double[] dArray = PyRender3D.unpack(object, "lines");
        if (dArray.length < 10) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        class_45872.push();
        try {
            ItemRenderUtils.beginOverlayRendering(bl);
            ItemRenderUtils.translateToCamera(class_45872);
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            MatrixStack.Entry class_46652 = class_45872.peek();
            Matrix4f matrix4f = class_46652.getPositionMatrix();
            BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            int n = 0;
            while (n + 10 <= dArray.length) {
                float f;
                float f2;
                float f3;
                float f4;
                float f5;
                float f6;
                float f7;
                float f8;
                float f9;
                float f10;
                int n2 = PyRender3D.packColor(dArray[n + 6], dArray[n + 7], dArray[n + 8], dArray[n + 9]);
                if (n2 >>> 24 != 0 && !((f10 = (float)Math.sqrt((f9 = (f8 = (float)dArray[n + 3]) - (f7 = (float)dArray[n])) * f9 + (f6 = (f5 = (float)dArray[n + 4]) - (f4 = (float)dArray[n + 1])) * f6 + (f3 = (f2 = (float)dArray[n + 5]) - (f = (float)dArray[n + 2])) * f3)) <= 0.0f)) {
                    class_2872.vertex(matrix4f, f7, f4, f).color(n2).normal(class_46652, f9 /= f10, f6 /= f10, f3 /= f10);
                    class_2872.vertex(matrix4f, f8, f5, f2).color(n2).normal(class_46652, f9, f6, f3);
                }
                n += 10;
            }
            ItemRenderUtils.flushVertexConsumer(class_2872);
        }
        finally {
            ItemRenderUtils.endOverlayRendering();
            class_45872.pop();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void texts(Render3DEvent render3DEvent, Object object, Object object2, Object object3, ColorRGBA colorRGBA, double d, int n, ColorRGBA colorRGBA2, double d2, double d3, int n2, int n3, boolean bl, boolean bl2) {
        if (render3DEvent == null) {
            return;
        }
        List<String> list = PyRender3D.strings(object);
        if (list.isEmpty()) {
            return;
        }
        double[] dArray = PyRender3D.unpack(object2, "texts");
        int n4 = Math.min(list.size(), dArray.length / 11);
        if (n4 <= 0) {
            return;
        }
        FontRenderer fontRenderer = this.fontOf(object3);
        float f = fontRenderer.getLineHeight(32.0f);
        if (f <= 0.0f) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = render3DEvent.getCamera().getPos();
        Vector3f vector3f = render3DEvent.getCamera().getRotation().transform(new Vector3f(0.0f, 0.0f, -1.0f));
        boolean[] blArray = new boolean[n4];
        float[] fArray = new float[n4];
        for (int i = 0; i < n4; ++i) {
            int n5 = i * 11;
            double d4 = dArray[n5] - VanillaChestLootTableGenerator.x;
            double d5 = dArray[n5 + 1] - VanillaChestLootTableGenerator.y;
            double d6 = dArray[n5 + 2] - VanillaChestLootTableGenerator.z;
            double d7 = dArray[n5 + 3];
            if (d4 * (double)vector3f.x + d5 * (double)vector3f.y + d6 * (double)vector3f.z < -d7 * 2.0 - 0.5) continue;
            blArray[i] = true;
            fArray[i] = fontRenderer.measure(list.get(i), 32.0f);
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        class_45872.push();
        try {
            ItemRenderUtils.beginOverlayRendering(bl2);
            if (!bl) {
                RenderSystem.enableDepthTest();
            }
            ItemRenderUtils.translateToCamera(class_45872);
            moscow.rockstar.render.state.RenderStateSupport.bindGlyphAtlasTextures(0, 1, 2);
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
            float f2 = (float)(d * (double)f);
            int n6 = Math.min(OUTLINE_STEPS.length, Math.max(0, n) * 2);
            boolean bl3 = colorRGBA != null && f2 > 0.0f && n6 > 0;
            boolean bl4 = colorRGBA2 != null;
            for (int i = 0; i < n4; ++i) {
                if (!blArray[i]) continue;
                int n7 = i * 11;
                String string = list.get(i);
                if (string.isEmpty()) continue;
                float f3 = (float)dArray[n7 + 3];
                int n8 = PyRender3D.packColor(dArray[n7 + 4], dArray[n7 + 5], dArray[n7 + 6], dArray[n7 + 7]);
                if (f3 <= 0.0f || n8 >>> 24 == 0) continue;
                class_45872.push();
                try {
                    class_45872.translate(dArray[n7], dArray[n7 + 1], dArray[n7 + 2]);
                    this.face(class_45872, render3DEvent, n2, dArray[n7 + 8], dArray[n7 + 9]);
                    float f4 = (float)dArray[n7 + 10];
                    if (f4 != 0.0f) {
                        class_45872.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f4));
                    }
                    float f5 = f3 / f;
                    class_45872.scale(f5, -f5, f5);
                    Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
                    float f6 = n3 == 1 ? 0.0f : (n3 == 2 ? -fArray[i] : -fArray[i] * 0.5f);
                    float f7 = -f * 0.5f;
                    float f8 = (float)(n8 >>> 24 & 0xFF) / 255.0f;
                    if (bl4) {
                        fontRenderer.draw(matrix4f, (VertexConsumer)class_2872, string, 32.0f, f6 + (float)(d2 * (double)f), f7 + (float)(d3 * (double)f), 0.0f, PyRender3D.faded(colorRGBA2, f8), 0.5f);
                    }
                    if (bl3) {
                        int n9 = PyRender3D.faded(colorRGBA, f8);
                        for (int j = 0; j < n6; j += 2) {
                            fontRenderer.draw(matrix4f, (VertexConsumer)class_2872, string, 32.0f, f6 + OUTLINE_STEPS[j] * f2, f7 + OUTLINE_STEPS[j + 1] * f2, 0.0f, n9, 0.5f);
                        }
                    }
                    fontRenderer.draw(matrix4f, (VertexConsumer)class_2872, string, 32.0f, f6, f7, 0.0f, n8, 0.5f);
                    continue;
                }
                finally {
                    class_45872.pop();
                }
            }
            ItemRenderUtils.flushVertexConsumer(class_2872);
        }
        finally {
            moscow.rockstar.render.state.RenderStateSupport.finishBatch();
            ItemRenderUtils.endOverlayRendering();
            class_45872.pop();
        }
    }

    public double textWidth(String string, double d, Object object) {
        FontRenderer fontRenderer = this.fontOf(object);
        float f = fontRenderer.getLineHeight(32.0f);
        if (f <= 0.0f || string == null || string.isEmpty()) {
            return 0.0;
        }
        return (double)(fontRenderer.measure(string, 32.0f) / f) * d;
    }

    public double lineHeight(double d, Object object) {
        FontRenderer fontRenderer = this.fontOf(object);
        float f = fontRenderer.getLineHeight(32.0f);
        if (f <= 0.0f) {
            return d;
        }
        return (double)(fontRenderer.getAscent() * 32.0f / f) * d;
    }

    public double[] letters(String string, double d, Object object) {
        if (string == null || string.isEmpty()) {
            return EMPTY;
        }
        FontRenderer fontRenderer = this.fontOf(object);
        float f3 = fontRenderer.getLineHeight(32.0f);
        if (f3 <= 0.0f) {
            return EMPTY;
        }
        double d2 = d / (double)f3;
        ArrayList arrayList = new ArrayList();
        fontRenderer.visitGlyphs(string, 32.0f, 0.0f, 0.0f, null, (renderer, glyph, character, glyphX, glyphY) -> {
            if (glyph == null) {
                return;
            }
            arrayList.add(new double[]{glyphX, (double)glyphY * d2, (double)glyph.getAdvanceWidth() * 32.0d * d2});
        });
        double[] dArray = new double[arrayList.size() * 3];
        for (int i = 0; i < arrayList.size(); ++i) {
            double[] dArray2 = (double[])arrayList.get(i);
            dArray[i * 3] = dArray2[0];
            dArray[i * 3 + 1] = dArray2[1];
            dArray[i * 3 + 2] = dArray2[2];
        }
        return dArray;
    }

    private void face(MatrixStack class_45872, Render3DEvent render3DEvent, int n, double d, double d2) {
        switch (n) {
            case 1: {
                class_45872.multiply(render3DEvent.getCamera().getRotation());
                break;
            }
            case 2: {
                class_45872.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - render3DEvent.getCamera().getYaw()));
                break;
            }
            default: {
                class_45872.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - (float)d));
                if (d2 == 0.0) break;
                class_45872.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-((float)d2)));
            }
        }
    }

    private static int faded(ColorRGBA colorRGBA, float f) {
        return colorRGBA.mulAlpha(f).getRGB();
    }

    private FontRenderer fontOf(Object object) {
        String string;
        if (object instanceof FontRenderer) {
            return (FontRenderer)object;
        }
        if (object instanceof FontMetrics) {
            FontMetrics fontMetrics = (FontMetrics)object;
            return fontMetrics.getFontRenderer();
        }
        if (object instanceof String && !(string = (String)object).isBlank()) {
            return PyAssets.slugFont(string);
        }
        return Font.MEDIUM;
    }

    private static List<String> strings(Object object) {
        if (object == null) {
            return List.of();
        }
        if (object instanceof String) {
            String string = (String)object;
            return List.of(string);
        }
        if (object instanceof List) {
            List list = (List)object;
            ArrayList<String> arrayList = new ArrayList<String>(list.size());
            for (Object e : list) {
                arrayList.add(e == null ? "" : e.toString());
            }
            return arrayList;
        }
        if (object instanceof Object[]) {
            Object[] objectArray = (Object[])object;
            ArrayList<String> arrayList = new ArrayList<String>(objectArray.length);
            for (Object object2 : objectArray) {
                arrayList.add(object2 == null ? "" : object2.toString());
            }
            return arrayList;
        }
        if (object instanceof Iterable) {
            Iterable iterable = (Iterable)object;
            ArrayList<String> arrayList = new ArrayList<String>();
            for (Object t : iterable) {
                arrayList.add(t == null ? "" : t.toString());
            }
            return arrayList;
        }
        if (object instanceof PyObject) {
            PyObject pyObject = (PyObject)object;
            try {
                return PyRender3D.strings(pyObject.as(List.class));
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        throw new IllegalArgumentException("texts \u0436\u0434\u0451\u0442 \u0441\u043f\u0438\u0441\u043e\u043a \u0441\u0442\u0440\u043e\u043a, \u0430 \u043f\u043e\u043b\u0443\u0447\u0438\u043b " + object.getClass().getName());
    }

    private static double[] unpack(Object object, String string) {
        if (object == null) {
            return EMPTY;
        }
        if (object instanceof double[]) {
            double[] dArray = (double[])object;
            return dArray;
        }
        if (object instanceof byte[]) {
            byte[] byArray = (byte[])object;
            FloatBuffer floatBuffer = ByteBuffer.wrap(byArray).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
            double[] dArray = new double[floatBuffer.remaining()];
            for (int i = 0; i < dArray.length; ++i) {
                dArray[i] = floatBuffer.get(i);
            }
            return dArray;
        }
        if (object instanceof float[]) {
            float[] fArray = (float[])object;
            double[] dArray = new double[fArray.length];
            for (int i = 0; i < fArray.length; ++i) {
                dArray[i] = fArray[i];
            }
            return dArray;
        }
        if (object instanceof int[]) {
            int[] nArray = (int[])object;
            double[] dArray = new double[nArray.length];
            for (int i = 0; i < nArray.length; ++i) {
                dArray[i] = nArray[i];
            }
            return dArray;
        }
        if (object instanceof List) {
            List list = (List)object;
            double[] dArray = new double[list.size()];
            for (int i = 0; i < dArray.length; ++i) {
                dArray[i] = PyRender3D.number(list.get(i), string);
            }
            return dArray;
        }
        if (object instanceof Object[]) {
            Object[] objectArray = (Object[])object;
            double[] dArray = new double[objectArray.length];
            for (int i = 0; i < objectArray.length; ++i) {
                dArray[i] = PyRender3D.number(objectArray[i], string);
            }
            return dArray;
        }
        if (object instanceof Iterable<?> iterable) {
            ArrayList<Object> values = new ArrayList<>();
            for (Object value : iterable) {
                values.add(value);
            }
            double[] numbers = new double[values.size()];
            for (int i = 0; i < numbers.length; ++i) {
                numbers[i] = PyRender3D.number(values.get(i), string);
            }
            return numbers;
        }
        if (object instanceof PyObject) {
            PyObject pyObject = (PyObject)object;
            List list = null;
            try {
                list = (List)pyObject.as(List.class);
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (list != null) {
                return PyRender3D.unpack(list, string);
            }
        }
        throw new IllegalArgumentException(string + " \u0436\u0434\u0451\u0442 \u043f\u043b\u043e\u0441\u043a\u0438\u0439 \u0441\u043f\u0438\u0441\u043e\u043a \u0447\u0438\u0441\u0435\u043b, \u0430 \u043f\u043e\u043b\u0443\u0447\u0438\u043b " + object.getClass().getName());
    }

    private static double number(Object object, String string) {
        if (object instanceof Number) {
            Number number = (Number)object;
            return number.doubleValue();
        }
        throw new IllegalArgumentException(string + " \u0436\u0434\u0451\u0442 \u043f\u043b\u043e\u0441\u043a\u0438\u0439 \u0441\u043f\u0438\u0441\u043e\u043a \u0447\u0438\u0441\u0435\u043b, \u0430 \u0432 \u043d\u0451\u043c \u043b\u0435\u0436\u0438\u0442 " + (object == null ? "None" : object.getClass().getName()));
    }

    private static int packColor(double d, double d2, double d3, double d4) {
        return PyRender3D.channel(d4) << 24 | PyRender3D.channel(d) << 16 | PyRender3D.channel(d2) << 8 | PyRender3D.channel(d3);
    }

    private static int channel(double d) {
        int n = (int)Math.round(d);
        return n < 0 ? 0 : Math.min(n, 255);
    }

    private void withLines(Render3DEvent render3DEvent, boolean bl, Consumer<BufferBuilder> consumer) {
        this.withBuffer(render3DEvent, VertexFormat.DrawMode.DEBUG_LINES, bl, consumer);
    }

    private void withQuads(Render3DEvent render3DEvent, boolean bl, Consumer<BufferBuilder> consumer) {
        this.withBuffer(render3DEvent, VertexFormat.DrawMode.QUADS, bl, consumer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void withBuffer(Render3DEvent render3DEvent, VertexFormat.DrawMode class_55962, boolean bl, Consumer<BufferBuilder> consumer) {
        if (render3DEvent == null) {
            return;
        }
        MatrixStack class_45872 = render3DEvent.getMatrices();
        class_45872.push();
        try {
            ItemRenderUtils.beginOverlayRendering(bl);
            ItemRenderUtils.translateToCamera(class_45872);
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder class_2872 = RenderSystem.renderThreadTesselator().begin(class_55962, VertexFormats.POSITION_COLOR);
            consumer.accept(class_2872);
            ItemRenderUtils.flushVertexConsumer(class_2872);
        }
        finally {
            ItemRenderUtils.endOverlayRendering();
            class_45872.pop();
        }
    }

    private Box boxOf(Object object) {
        if (object instanceof Box) {
            Box HorizontalFacingBlock = (Box)object;
            return HorizontalFacingBlock;
        }
        if (object instanceof Entity) {
            Entity class_12972 = (Entity)object;
            return class_12972.getBoundingBox();
        }
        if (object instanceof Vec3d) {
            Vec3d VanillaChestLootTableGenerator = (Vec3d)object;
            return new Box(VanillaChestLootTableGenerator, VanillaChestLootTableGenerator).expand(0.1);
        }
        return null;
    }

    private Vec3d centerOf(Object object) {
        if (object instanceof Vec3d) {
            Vec3d VanillaChestLootTableGenerator = (Vec3d)object;
            return VanillaChestLootTableGenerator;
        }
        Box HorizontalFacingBlock = this.boxOf(object);
        return HorizontalFacingBlock == null ? null : HorizontalFacingBlock.getCenter();
    }

    private ColorRGBA safeColor(ColorRGBA colorRGBA) {
        return colorRGBA == null ? ColorRGBA.WHITE : colorRGBA;
    }
}
