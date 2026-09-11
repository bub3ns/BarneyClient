/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 *  org.jetbrains.annotations.NotNull
 */
package moscow.rockstar.combat.rotation;

import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.tracking.EntityPositionCache;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.combat.attacks.Aura;
import moscow.rockstar.modules.movement.speed.Speed;
import moscow.rockstar.render.esp.EntityOverlayGeometry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import ua.mintantileak.spk.Compile;

public final class AimRotationMath
implements ClientAccess {
    @Compile(obfuscation=1)
    public static Vec3d getClosestPointOnEntityBounds(Entity class_12972) {
        Vec3d VanillaChestLootTableGenerator = AimRotationMath.minecraftClient.player.getEyePos();
        return new Vec3d(MathHelper.clamp((double)VanillaChestLootTableGenerator.x, (double)class_12972.getBoundingBox().minX, (double)class_12972.getBoundingBox().maxX), MathHelper.clamp((double)VanillaChestLootTableGenerator.y, (double)class_12972.getBoundingBox().minY, (double)class_12972.getBoundingBox().maxY), MathHelper.clamp((double)VanillaChestLootTableGenerator.z, (double)class_12972.getBoundingBox().minZ, (double)class_12972.getBoundingBox().maxZ));
    }

    @Compile(obfuscation=1)
    public static Vec3d translateAimPoint(LivingEntity class_13092, Vec3d VanillaChestLootTableGenerator) {
        return AimRotationMath.getClosestPointOnEntityBounds((Entity)class_13092).subtract(class_13092.getPos()).add(VanillaChestLootTableGenerator);
    }

    @Compile(obfuscation=1)
    public static Rotation getRotationToPoint(Vec3d VanillaChestLootTableGenerator) {
        double d = VanillaChestLootTableGenerator.getX();
        double d2 = VanillaChestLootTableGenerator.getY();
        double d3 = VanillaChestLootTableGenerator.getZ();
        double d4 = d - AimRotationMath.minecraftClient.player.getX();
        double d5 = d2 - (AimRotationMath.minecraftClient.player.getY() + (double)AimRotationMath.minecraftClient.player.getEyeHeight(AimRotationMath.minecraftClient.player.getPose()));
        double d6 = d3 - AimRotationMath.minecraftClient.player.getZ();
        double d7 = Math.sqrt(d4 * d4 + d6 * d6);
        float f = (float)Math.toDegrees(Math.atan2(d6, d4)) - 90.0f;
        float f2 = (float)(-Math.toDegrees(Math.atan2(d5, d7)));
        return new Rotation(f, f2);
    }

    @Compile(obfuscation=1)
    public static Rotation getRotationBetweenPoints(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock) {
        double d = WallPlayerSkullBlock.x - VanillaChestLootTableGenerator.x;
        double d2 = WallPlayerSkullBlock.y - VanillaChestLootTableGenerator.y;
        double d3 = WallPlayerSkullBlock.z - VanillaChestLootTableGenerator.z;
        double d4 = Math.sqrt(d * d + d3 * d3);
        float f = (float)Math.toDegrees(Math.atan2(d3, d)) - 90.0f;
        float f2 = (float)(-Math.toDegrees(Math.atan2(d2, d4)));
        return new Rotation(f, f2);
    }

    @Compile(obfuscation=1)
    public static float getMouseRotationStep() {
        double d = (Double)AimRotationMath.minecraftClient.options.getMouseSensitivity().getValue();
        double d2 = d * (double)0.6f + (double)0.2f;
        return (float)(d2 * d2 * d2 * (double)1.2f);
    }

    @NotNull
    @Compile(obfuscation=1)
    public static Rotation snapRotationToMouseStep(@NotNull Rotation rotation, @NotNull Rotation rotation2) {
        float step = AimRotationMath.getMouseRotationStep();
        if (step <= 0.0001f) {
            return rotation2;
        }
        float deltaYaw = MathHelper.wrapDegrees(rotation2.getYaw() - rotation.getYaw());
        float deltaPitch = rotation2.getPitch() - rotation.getPitch();
        float snappedDeltaYaw = (float)Math.round(deltaYaw / step) * step;
        float snappedDeltaPitch = (float)Math.round(deltaPitch / step) * step;
        return new Rotation(rotation.getYaw() + snappedDeltaYaw, MathHelper.clamp(rotation.getPitch() + snappedDeltaPitch, -90.0f, 90.0f));
    }

    @Compile(obfuscation=1)
    public static float snapYawToMouseStep(float current, float target) {
        float step = AimRotationMath.getMouseRotationStep();
        if (step <= 0.0001f) {
            return target;
        }
        float deltaYaw = MathHelper.wrapDegrees(target - current);
        float snappedDeltaYaw = (float)Math.round(deltaYaw / step) * step;
        return current + snappedDeltaYaw;
    }

    @Compile(obfuscation=1)
    public static float snapPitchToMouseStep(float current, float target) {
        float step = AimRotationMath.getMouseRotationStep();
        if (step <= 0.0001f) {
            return MathHelper.clamp(target, -90.0f, 90.0f);
        }
        float deltaPitch = target - current;
        float snappedDeltaPitch = (float)Math.round(deltaPitch / step) * step;
        return MathHelper.clamp(current + snappedDeltaPitch, -90.0f, 90.0f);
    }

    @Compile(obfuscation=1)
    public static int getWrappedYawStepCount(float f, float f2) {
        float f3 = AimRotationMath.getMouseRotationStep();
        return Math.round(MathHelper.wrapDegrees(f2 - f) / f3);
    }

    @Compile(obfuscation=1)
    public static int getPitchStepCount(float f, float f2) {
        float f3 = AimRotationMath.getMouseRotationStep();
        return Math.round((f2 - f) / f3);
    }

    @Compile(obfuscation=1)
    public static float getWrappedAngleDifference(float f, float f2) {
        return MathHelper.wrapDegrees(f2 - f);
    }

    @Compile(obfuscation=1)
    public static float getClosestWrappedYaw(float current, float target, float maxStep) {
        float delta = MathHelper.wrapDegrees(target - current);
        if (Math.abs(delta) <= maxStep) {
            return current + delta;
        }
        return current + Math.signum(delta) * maxStep;
    }

    @Compile(obfuscation=1)
    public static float getNearestWrappedYaw(float f, float f2) {
        return f + MathHelper.wrapDegrees(f2 - f);
    }

    @Compile(obfuscation=1)
    public static Rotation calculateAttackRotation(LivingEntity class_13092, Aura aura) {
        Vec3d VanillaChestLootTableGenerator = EntityOverlayGeometry.getTargetAimPoint((Entity)class_13092, aura.getResolverOption().isSelected());
        Rotation rotation = AimRotationMath.getRotationToPoint(AimRotationMath.translateAimPoint(class_13092, VanillaChestLootTableGenerator));
        if (AimRotationMath.minecraftClient.player.getEyePos().distanceTo(class_13092.getEyePos()) < 3.0) {
            VanillaChestLootTableGenerator = EntityOverlayGeometry.getTargetAimPoint((Entity)class_13092, aura.getResolverOption().isSelected()).add(0.0, (double)(class_13092.getHeight() / 2.0f), 0.0);
            rotation = AimRotationMath.getRotationToPoint(VanillaChestLootTableGenerator);
            if (RockstarClient.create().getModuleRegistry().getModule(Speed.class).isEnabled()) {
                Vec3d WallPlayerSkullBlock = EntityPositionCache.getTrackedPosition((Entity)class_13092);
            }
        }
        if (rotation.getPitch() == (float)((int)rotation.getPitch())) {
            rotation.setPitch(Math.clamp(rotation.getPitch() + MathUtils.interpolateRandomStrategy(-1.0f, 1.0f), -90.0f, 90.0f));
        }
        if (rotation.getYaw() == (float)((int)rotation.getYaw())) {
            rotation.setYaw(rotation.getYaw() + MathUtils.interpolateRandomStrategy(-1.0f, 1.0f));
        }
        return rotation;
    }

    @Generated
    private AimRotationMath() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

