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
 *  net.minecraft.BufferBuilder
 *  net.minecraft.Camera
 *  net.minecraft.MatrixStack
 *  net.minecraft.MatrixStack$Entry
 *  net.minecraft.VertexConsumer
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package moscow.rockstar.render.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import pyrock.utility.render.ColorRGBA;

public final class RenderUtils
implements ClientAccess {
    public static void drawGlowingBox(MatrixStack class_45872, BufferBuilder class_2872, Box HorizontalFacingBlock, ColorRGBA colorRGBA) {
        float f = colorRGBA.getRed();
        float f2 = colorRGBA.getGreen();
        float f3 = colorRGBA.getBlue();
        float f4 = colorRGBA.getAlpha();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        int n = 3;
        float f5 = 0.1f;
        for (int i = n; i >= 1; --i) {
            float f6 = (float)i * f5;
            float f7 = f4 * (0.15f / (float)i);
            RenderUtils.drawFilledBox(class_45872, class_2872, HorizontalFacingBlock.expand((double)f6), new ColorRGBA(f, f2, f3, f7));
        }
        RenderUtils.drawFilledBox(class_45872, class_2872, HorizontalFacingBlock, new ColorRGBA(f, f2, f3, f4));
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void drawFilledBox(MatrixStack class_45872, BufferBuilder class_2872, Box HorizontalFacingBlock, ColorRGBA colorRGBA) {
        float f = colorRGBA.getRed() / 255.0f;
        float f2 = colorRGBA.getGreen() / 255.0f;
        float f3 = colorRGBA.getBlue() / 255.0f;
        float f4 = colorRGBA.getAlpha() / 255.0f;
        RenderUtils.drawFilledBox(class_45872, class_2872, HorizontalFacingBlock, f, f2, f3, f4);
    }

    public static void drawBoxCorners(MatrixStack class_45872, BufferBuilder class_2872, Box HorizontalFacingBlock, ColorRGBA colorRGBA) {
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
        class_2872.vertex(matrix4f, f8, f9, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f6, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f9, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f6, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f9, f7).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f8, f6, f10).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f5, f9, f7).color(f, f2, f3, f4);
    }

    public static void drawFilledBox(MatrixStack class_45872, BufferBuilder class_2872, Box HorizontalFacingBlock, float f, float f2, float f3, float f4) {
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

    public static void drawGradientBox(MatrixStack class_45872, BufferBuilder class_2872, Box HorizontalFacingBlock, ColorRGBA colorRGBA, ColorRGBA colorRGBA2) {
        float f = colorRGBA.getRed() / 255.0f;
        float f2 = colorRGBA.getGreen() / 255.0f;
        float f3 = colorRGBA.getBlue() / 255.0f;
        float f4 = colorRGBA.getAlpha() / 255.0f;
        float f5 = colorRGBA2.getRed() / 255.0f;
        float f6 = colorRGBA2.getGreen() / 255.0f;
        float f7 = colorRGBA2.getBlue() / 255.0f;
        float f8 = colorRGBA2.getAlpha() / 255.0f;
        float f9 = (float)HorizontalFacingBlock.minX;
        float f10 = (float)HorizontalFacingBlock.minY;
        float f11 = (float)HorizontalFacingBlock.minZ;
        float f12 = (float)HorizontalFacingBlock.maxX;
        float f13 = (float)HorizontalFacingBlock.maxY;
        float f14 = (float)HorizontalFacingBlock.maxZ;
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        class_2872.vertex(matrix4f, f9, f10, f11).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f12, f10, f11).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f12, f10, f14).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f9, f10, f14).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f9, f13, f11).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f12, f13, f11).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f12, f13, f14).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f9, f13, f14).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f9, f10, f14).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f12, f10, f14).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f12, f13, f14).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f9, f13, f14).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f9, f10, f11).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f12, f10, f11).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f12, f13, f11).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f9, f13, f11).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f9, f10, f11).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f9, f10, f14).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f9, f13, f14).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f9, f13, f11).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f12, f10, f11).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f12, f10, f14).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f12, f13, f14).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f12, f13, f11).color(f5, f6, f7, f8);
    }

    public static void drawGradientBoxOutline(MatrixStack class_45872, BufferBuilder class_2872, Box HorizontalFacingBlock, ColorRGBA colorRGBA, ColorRGBA colorRGBA2) {
        float f = colorRGBA.getRed() / 255.0f;
        float f2 = colorRGBA.getGreen() / 255.0f;
        float f3 = colorRGBA.getBlue() / 255.0f;
        float f4 = colorRGBA.getAlpha() / 255.0f;
        float f5 = colorRGBA2.getRed() / 255.0f;
        float f6 = colorRGBA2.getGreen() / 255.0f;
        float f7 = colorRGBA2.getBlue() / 255.0f;
        float f8 = colorRGBA2.getAlpha() / 255.0f;
        float f9 = (float)HorizontalFacingBlock.minX;
        float f10 = (float)HorizontalFacingBlock.minY;
        float f11 = (float)HorizontalFacingBlock.minZ;
        float f12 = (float)HorizontalFacingBlock.maxX;
        float f13 = (float)HorizontalFacingBlock.maxY;
        float f14 = (float)HorizontalFacingBlock.maxZ;
        Matrix4f matrix4f = class_45872.peek().getPositionMatrix();
        class_2872.vertex(matrix4f, f9, f10, f11).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f12, f10, f11).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f12, f10, f11).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f12, f10, f14).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f12, f10, f14).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f9, f10, f14).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f9, f10, f14).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f9, f10, f11).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f9, f13, f11).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f12, f13, f11).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f12, f13, f11).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f12, f13, f14).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f12, f13, f14).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f9, f13, f14).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f9, f13, f14).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f9, f13, f11).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f9, f10, f11).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f9, f13, f11).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f12, f10, f11).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f12, f13, f11).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f12, f10, f14).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f12, f13, f14).color(f5, f6, f7, f8);
        class_2872.vertex(matrix4f, f9, f10, f14).color(f, f2, f3, f4);
        class_2872.vertex(matrix4f, f9, f13, f14).color(f5, f6, f7, f8);
    }

    public static void drawBoxOutline(MatrixStack class_45872, BufferBuilder class_2872, Box HorizontalFacingBlock, ColorRGBA colorRGBA) {
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

    public static void drawLineSegment(MatrixStack class_45872, VertexConsumer class_45882, Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, ColorRGBA colorRGBA) {
        MatrixStack.Entry class_46652 = class_45872.peek();
        Vec3d VanillaEntityLootTableGenerator = WallPlayerSkullBlock.subtract(VanillaChestLootTableGenerator).normalize();
        Vector3f vector3f = new Vector3f((float)VanillaChestLootTableGenerator.x, (float)VanillaChestLootTableGenerator.y, (float)VanillaChestLootTableGenerator.z);
        class_45882.vertex(class_46652, vector3f).color(colorRGBA.getRGB()).normal(class_46652, (float)VanillaEntityLootTableGenerator.x, (float)VanillaEntityLootTableGenerator.y, (float)VanillaEntityLootTableGenerator.z);
        class_45882.vertex(class_46652, (float)WallPlayerSkullBlock.x, (float)WallPlayerSkullBlock.y, (float)WallPlayerSkullBlock.z).color(colorRGBA.getRGB()).normal(class_46652, (float)VanillaEntityLootTableGenerator.x, (float)VanillaEntityLootTableGenerator.y, (float)VanillaEntityLootTableGenerator.z);
    }

    public static void drawLineSegment(MatrixStack class_45872, BufferBuilder class_2872, Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, ColorRGBA colorRGBA) {
        MatrixStack.Entry class_46652 = class_45872.peek();
        Matrix4f matrix4f = class_46652.getPositionMatrix();
        Vec3d VanillaEntityLootTableGenerator = WallPlayerSkullBlock.subtract(VanillaChestLootTableGenerator).normalize();
        class_2872.vertex(matrix4f, (float)VanillaChestLootTableGenerator.x, (float)VanillaChestLootTableGenerator.y, (float)VanillaChestLootTableGenerator.z).color(colorRGBA.getRGB()).normal(class_46652, (float)VanillaEntityLootTableGenerator.x, (float)VanillaEntityLootTableGenerator.y, (float)VanillaEntityLootTableGenerator.z);
        class_2872.vertex(matrix4f, (float)WallPlayerSkullBlock.x, (float)WallPlayerSkullBlock.y, (float)WallPlayerSkullBlock.z).color(colorRGBA.getRGB()).normal(class_46652, (float)VanillaEntityLootTableGenerator.x, (float)VanillaEntityLootTableGenerator.y, (float)VanillaEntityLootTableGenerator.z);
    }

    public static void drawWorldLineToPoint(MatrixStack class_45872, BufferBuilder class_2872, Vec3d VanillaChestLootTableGenerator, ColorRGBA colorRGBA) {
        Camera class_41842 = RenderUtils.minecraftClient.gameRenderer.getCamera();
        Vec3d WallPlayerSkullBlock = class_41842.getPos();
        Vec3d VanillaEntityLootTableGenerator = new Vec3d(0.0, 0.0, 27.0).rotateX((float)(-Math.toRadians(class_41842.getPitch()))).rotateY((float)(-Math.toRadians(class_41842.getYaw())));
        Vec3d PlayerSkullBlock = VanillaChestLootTableGenerator.subtract(WallPlayerSkullBlock);
        Vec3d RedstoneBlock = new Vec3d(VanillaEntityLootTableGenerator.getX(), VanillaEntityLootTableGenerator.getY(), VanillaEntityLootTableGenerator.getZ());
        RenderUtils.drawLineSegment(class_45872, class_2872, RedstoneBlock, PlayerSkullBlock, colorRGBA);
    }

    @Generated
    private RenderUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
