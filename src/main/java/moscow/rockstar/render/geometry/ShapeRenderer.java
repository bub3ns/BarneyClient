/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$class_4534
 *  com.mojang.blaze3d.platform.GlStateManager$class_4535
 *  com.mojang.blaze3d.systems.RenderSystem
 *  lombok.Generated
 *  net.minecraft.ShaderProgramKeys
 *  net.minecraft.ShaderProgramKey
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.BufferRenderer
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Tessellator
 *  net.minecraft.VertexFormats
 *  net.minecraft.VertexFormat$DrawMode
 *  net.minecraft.MatrixStack
 *  net.minecraft.BuiltBuffer
 *  org.joml.Matrix4f
 */
package moscow.rockstar.render.geometry;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.BuiltBuffer;
import org.joml.Matrix4f;
import pyrock.utility.render.ColorRGBA;

public final class ShapeRenderer {
    private static void beginShapeRender() {
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
    }

    private static void endShapeRender() {
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void drawFilledBox(MatrixStack class_45872, Box HorizontalFacingBlock, ColorRGBA colorRGBA) {
        ShapeRenderer.beginShapeRender();
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        ShapeRenderer.addFilledBoxVertices(class_45872, class_2872, HorizontalFacingBlock, colorRGBA);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShapeRenderer.endShapeRender();
    }

    public static void drawOutlinedBox(MatrixStack class_45872, Box HorizontalFacingBlock, ColorRGBA colorRGBA) {
        ShapeRenderer.beginShapeRender();
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        ShapeRenderer.addOutlinedBoxVertices(class_45872, class_2872, HorizontalFacingBlock, colorRGBA);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShapeRenderer.endShapeRender();
    }

    public static void drawLine(MatrixStack class_45872, Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, ColorRGBA colorRGBA) {
        ShapeRenderer.beginShapeRender();
        BufferBuilder class_2872 = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        float f = colorRGBA.getRed() / 255.0f;
        float f2 = colorRGBA.getGreen() / 255.0f;
        float f3 = colorRGBA.getBlue() / 255.0f;
        float f4 = colorRGBA.getAlpha() / 255.0f;
        class_2872.vertex(matrix4f, (float)VanillaChestLootTableGenerator.x, (float)VanillaChestLootTableGenerator.y, (float)VanillaChestLootTableGenerator.z).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, (float)WallPlayerSkullBlock.x, (float)WallPlayerSkullBlock.y, (float)WallPlayerSkullBlock.z).color(f, f2, f3, f4);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)class_2872.end());
        ShapeRenderer.endShapeRender();
    }

    public static void drawVisibleFaces(MatrixStack matrices, Box box, boolean up, boolean down, boolean north, boolean south, boolean west, boolean east, ColorRGBA colorRGBA) {
        if (box == null || (!up && !down && !north && !south && !west && !east)) {
            return;
        }
        ShapeRenderer.beginShapeRender();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        if (down) addFilledFaceVertices(matrices, buffer, box, Direction.DOWN, colorRGBA);
        if (up) addFilledFaceVertices(matrices, buffer, box, Direction.UP, colorRGBA);
        if (north) addFilledFaceVertices(matrices, buffer, box, Direction.NORTH, colorRGBA);
        if (south) addFilledFaceVertices(matrices, buffer, box, Direction.SOUTH, colorRGBA);
        if (west) addFilledFaceVertices(matrices, buffer, box, Direction.WEST, colorRGBA);
        if (east) addFilledFaceVertices(matrices, buffer, box, Direction.EAST, colorRGBA);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        ShapeRenderer.endShapeRender();
    }

    public static void drawVisibleEdges(MatrixStack matrices, Box box, boolean up, boolean down, boolean north, boolean south, boolean west, boolean east, ColorRGBA colorRGBA) {
        if (box == null || (!up && !down && !north && !south && !west && !east)) {
            return;
        }
        ShapeRenderer.beginShapeRender();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        float r = colorRGBA.getRed() / 255.0f;
        float g = colorRGBA.getGreen() / 255.0f;
        float b = colorRGBA.getBlue() / 255.0f;
        float a = colorRGBA.getAlpha() / 255.0f;
        float x1 = (float)box.minX;
        float y1 = (float)box.minY;
        float z1 = (float)box.minZ;
        float x2 = (float)box.maxX;
        float y2 = (float)box.maxY;
        float z2 = (float)box.maxZ;
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        if (up || north) { buffer.vertex(matrix4f, x1, y2, z1).color(r, g, b, a); buffer.vertex(matrix4f, x2, y2, z1).color(r, g, b, a); }
        if (up || south) { buffer.vertex(matrix4f, x1, y2, z2).color(r, g, b, a); buffer.vertex(matrix4f, x2, y2, z2).color(r, g, b, a); }
        if (up || west)  { buffer.vertex(matrix4f, x1, y2, z1).color(r, g, b, a); buffer.vertex(matrix4f, x1, y2, z2).color(r, g, b, a); }
        if (up || east)  { buffer.vertex(matrix4f, x2, y2, z1).color(r, g, b, a); buffer.vertex(matrix4f, x2, y2, z2).color(r, g, b, a); }

        if (down || north) { buffer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a); buffer.vertex(matrix4f, x2, y1, z1).color(r, g, b, a); }
        if (down || south) { buffer.vertex(matrix4f, x1, y1, z2).color(r, g, b, a); buffer.vertex(matrix4f, x2, y1, z2).color(r, g, b, a); }
        if (down || west)  { buffer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a); buffer.vertex(matrix4f, x1, y1, z2).color(r, g, b, a); }
        if (down || east)  { buffer.vertex(matrix4f, x2, y1, z1).color(r, g, b, a); buffer.vertex(matrix4f, x2, y1, z2).color(r, g, b, a); }

        if (west || north) { buffer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a); buffer.vertex(matrix4f, x1, y2, z1).color(r, g, b, a); }
        if (east || north) { buffer.vertex(matrix4f, x2, y1, z1).color(r, g, b, a); buffer.vertex(matrix4f, x2, y2, z1).color(r, g, b, a); }
        if (west || south) { buffer.vertex(matrix4f, x1, y1, z2).color(r, g, b, a); buffer.vertex(matrix4f, x1, y2, z2).color(r, g, b, a); }
        if (east || south) { buffer.vertex(matrix4f, x2, y1, z2).color(r, g, b, a); buffer.vertex(matrix4f, x2, y2, z2).color(r, g, b, a); }

        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        ShapeRenderer.endShapeRender();
    }

    public static void drawFilledFace(MatrixStack matrices, Box box, Direction direction, ColorRGBA colorRGBA) {
        if (direction == null || box == null) {
            return;
        }
        ShapeRenderer.beginShapeRender();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        ShapeRenderer.addFilledFaceVertices(matrices, buffer, box, direction, colorRGBA);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        ShapeRenderer.endShapeRender();
    }

    public static void drawOutlinedFace(MatrixStack matrices, Box box, Direction direction, ColorRGBA colorRGBA) {
        if (direction == null || box == null) {
            return;
        }
        ShapeRenderer.beginShapeRender();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        ShapeRenderer.addOutlinedFaceVertices(matrices, buffer, box, direction, colorRGBA);
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        ShapeRenderer.endShapeRender();
    }

    private static void addFilledFaceVertices(MatrixStack matrices, BufferBuilder buffer, Box box, Direction direction, ColorRGBA colorRGBA) {
        float r = colorRGBA.getRed() / 255.0f;
        float g = colorRGBA.getGreen() / 255.0f;
        float b = colorRGBA.getBlue() / 255.0f;
        float a = colorRGBA.getAlpha() / 255.0f;
        float x1 = (float)box.minX;
        float y1 = (float)box.minY;
        float z1 = (float)box.minZ;
        float x2 = (float)box.maxX;
        float y2 = (float)box.maxY;
        float z2 = (float)box.maxZ;
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        switch (direction) {
            case DOWN -> {
                buffer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y1, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y1, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y1, z2).color(r, g, b, a);
            }
            case UP -> {
                buffer.vertex(matrix4f, x1, y2, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z1).color(r, g, b, a);
            }
            case NORTH -> {
                buffer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y2, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y1, z1).color(r, g, b, a);
            }
            case SOUTH -> {
                buffer.vertex(matrix4f, x1, y1, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y1, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y2, z2).color(r, g, b, a);
            }
            case WEST -> {
                buffer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y1, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y2, z1).color(r, g, b, a);
            }
            case EAST -> {
                buffer.vertex(matrix4f, x2, y1, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y1, z2).color(r, g, b, a);
            }
        }
    }

    private static void addOutlinedFaceVertices(MatrixStack matrices, BufferBuilder buffer, Box box, Direction direction, ColorRGBA colorRGBA) {
        float r = colorRGBA.getRed() / 255.0f;
        float g = colorRGBA.getGreen() / 255.0f;
        float b = colorRGBA.getBlue() / 255.0f;
        float a = colorRGBA.getAlpha() / 255.0f;
        float x1 = (float)box.minX;
        float y1 = (float)box.minY;
        float z1 = (float)box.minZ;
        float x2 = (float)box.maxX;
        float y2 = (float)box.maxY;
        float z2 = (float)box.maxZ;
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        switch (direction) {
            case DOWN -> {
                buffer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y1, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y1, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y1, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y1, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y1, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y1, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a);
            }
            case UP -> {
                buffer.vertex(matrix4f, x1, y2, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y2, z1).color(r, g, b, a);
            }
            case NORTH -> {
                buffer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y2, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y2, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y1, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y1, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a);
            }
            case SOUTH -> {
                buffer.vertex(matrix4f, x1, y1, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y1, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y1, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y1, z2).color(r, g, b, a);
            }
            case WEST -> {
                buffer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y1, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y1, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y2, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y2, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x1, y1, z1).color(r, g, b, a);
            }
            case EAST -> {
                buffer.vertex(matrix4f, x2, y1, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y1, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y1, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z2).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y2, z1).color(r, g, b, a);
                buffer.vertex(matrix4f, x2, y1, z1).color(r, g, b, a);
            }
        }
    }

    private static void addFilledBoxVertices(MatrixStack class_45872, BufferBuilder class_2872, Box HorizontalFacingBlock, ColorRGBA colorRGBA) {
        float f = colorRGBA.getRed() / 255.0f;
        float f2 = colorRGBA.getGreen() / 255.0f;
        float f3 = colorRGBA.getBlue() / 255.0f;
        float f4 = colorRGBA.getAlpha() / 255.0f;
        float f5 = (float)HorizontalFacingBlock.minX;
        float f6 = (float)HorizontalFacingBlock.minY;
        float f7 = (float)HorizontalFacingBlock.minZ;
        float f8 = (float)HorizontalFacingBlock.maxX;
        float f9 = (float)HorizontalFacingBlock.maxY;
        float f10 = (float)HorizontalFacingBlock.maxZ;
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        class_2872.vertex(matrix4f, f5, f6, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f6, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f6, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f6, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f9, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f9, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f9, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f9, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f6, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f6, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f9, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f9, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f6, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f6, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f9, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f9, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f6, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f6, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f9, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f9, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f6, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f6, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f9, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f9, f7).color(f, f2, f3, f4);
    }

    private static void addOutlinedBoxVertices(MatrixStack class_45872, BufferBuilder class_2872, Box HorizontalFacingBlock, ColorRGBA colorRGBA) {
        float f = colorRGBA.getRed() / 255.0f;
        float f2 = colorRGBA.getGreen() / 255.0f;
        float f3 = colorRGBA.getBlue() / 255.0f;
        float f4 = colorRGBA.getAlpha() / 255.0f;
        float f5 = (float)HorizontalFacingBlock.minX;
        float f6 = (float)HorizontalFacingBlock.minY;
        float f7 = (float)HorizontalFacingBlock.minZ;
        float f8 = (float)HorizontalFacingBlock.maxX;
        float f9 = (float)HorizontalFacingBlock.maxY;
        float f10 = (float)HorizontalFacingBlock.maxZ;
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        class_2872.vertex(matrix4f, f5, f6, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f6, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f6, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f6, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f6, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f6, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f6, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f6, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f9, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f9, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f9, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f9, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f9, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f9, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f9, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f9, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f6, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f9, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f6, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f9, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f6, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f9, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f6, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f9, f10).color(f, f2, f3, f4);
    }

    @Generated
    private ShapeRenderer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

