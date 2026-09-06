/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.Vec2f
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 *  net.minecraft.Camera
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector4f
 */
package moscow.rockstar.render.util;

import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

public final class ProjectionUtils
implements ClientAccess {
    private static Matrix4f viewMatrix;
    private static Matrix4f projectionMatrix;

    public static void setMatrices(Matrix4f matrix4f, Matrix4f matrix4f2) {
        viewMatrix = new Matrix4f((Matrix4fc)matrix4f);
        projectionMatrix = new Matrix4f((Matrix4fc)matrix4f2);
    }

    public static Vec2f projectToScreen(Vec3d VanillaChestLootTableGenerator) {
        Camera class_41842 = ProjectionUtils.minecraftClient.gameRenderer.getCamera();
        Vec3d WallPlayerSkullBlock = VanillaChestLootTableGenerator.subtract(class_41842.getPos());
        Vector4f vector4f = new Vector4f((float)WallPlayerSkullBlock.x, (float)WallPlayerSkullBlock.y, (float)WallPlayerSkullBlock.z, 1.0f);
        vector4f.mul((Matrix4fc)viewMatrix).mul((Matrix4fc)projectionMatrix);
        if (vector4f.w <= 0.0f) {
            return null;
        }
        Vector4f vector4f2 = vector4f.div(vector4f.w);
        float f = (vector4f2.x + 1.0f) / 2.0f * (float)minecraftClient.getWindow().getScaledWidth();
        float f2 = (1.0f - vector4f2.y) / 2.0f * (float)minecraftClient.getWindow().getScaledHeight();
        return new Vec2f(f, f2);
    }

    public static Vec2f projectRayToScreen(Vec3d VanillaChestLootTableGenerator) {
        if (projectionMatrix == null) {
            return null;
        }
        Camera class_41842 = ProjectionUtils.minecraftClient.gameRenderer.getCamera();
        float f = class_41842.getYaw() * ((float)Math.PI / 180);
        float f2 = class_41842.getPitch() * ((float)Math.PI / 180);
        double d = MathHelper.cos((float)f);
        double d2 = MathHelper.sin((float)f);
        double d3 = MathHelper.cos((float)f2);
        double d4 = MathHelper.sin((float)f2);
        Vec3d WallPlayerSkullBlock = new Vec3d(-d2 * d3, -d4, d * d3);
        Vec3d VanillaEntityLootTableGenerator = new Vec3d(-d, 0.0, -d2);
        Vec3d PlayerSkullBlock = VanillaEntityLootTableGenerator.crossProduct(WallPlayerSkullBlock);
        double d5 = VanillaChestLootTableGenerator.dotProduct(WallPlayerSkullBlock);
        if (d5 <= 1.0E-4) {
            return null;
        }
        double d6 = (double)projectionMatrix.m00() * VanillaChestLootTableGenerator.dotProduct(VanillaEntityLootTableGenerator) / d5;
        double d7 = (double)projectionMatrix.m11() * VanillaChestLootTableGenerator.dotProduct(PlayerSkullBlock) / d5;
        return new Vec2f((float)((d6 + 1.0) / 2.0 * (double)minecraftClient.getWindow().getScaledWidth()), (float)((1.0 - d7) / 2.0 * (double)minecraftClient.getWindow().getScaledHeight()));
    }

    public static Vec3d interpolateEntityPosition(Entity class_12972, float f) {
        return new Vec3d(MathHelper.lerp((double)f, (double)class_12972.prevX, (double)class_12972.getX()), MathHelper.lerp((double)f, (double)class_12972.prevY, (double)class_12972.getY()), MathHelper.lerp((double)f, (double)class_12972.prevZ, (double)class_12972.getZ()));
    }

    public static Vec3d interpolatePosition(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, float f) {
        return new Vec3d(MathHelper.lerp((double)f, (double)VanillaChestLootTableGenerator.x, (double)WallPlayerSkullBlock.getX()), MathHelper.lerp((double)f, (double)VanillaChestLootTableGenerator.y, (double)WallPlayerSkullBlock.getY()), MathHelper.lerp((double)f, (double)VanillaChestLootTableGenerator.z, (double)WallPlayerSkullBlock.getZ()));
    }

    @Generated
    private ProjectionUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

