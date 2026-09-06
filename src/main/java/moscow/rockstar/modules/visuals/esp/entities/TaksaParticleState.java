/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.BlockView
 *  net.minecraft.Box
 *  net.minecraft.Vec3d
 *  net.minecraft.BlockState
 *  net.minecraft.MathHelper
 *  net.minecraft.ClientPlayerEntity
 */
package moscow.rockstar.modules.visuals.esp.entities;

import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.ui.animation.Animation;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.network.ClientPlayerEntity;

public class TaksaParticleState
implements ClientAccess {
    private Vec3d particlePosition;
    private Vec3d particleVelocity = Vec3d.ZERO;
    private float wanderAngle = MathUtils.interpolateRandomDouble(0.0, 360.0);
    private float targetYaw;
    private float smoothedYaw;
    private int interpolationSteps = 50;
    private final Animation xPositionAnimation = new Animation(150L, 0.0f, moscow.rockstar.ui.animation.Easing.easeOutCubic);
    private final Animation yPositionAnimation = new Animation(150L, 0.0f, moscow.rockstar.ui.animation.Easing.easeOutCubic);
    private final Animation zPositionAnimation = new Animation(150L, 0.0f, moscow.rockstar.ui.animation.Easing.easeOutCubic);
    private final Animation pitchAnimation = new Animation(100L, 0.0f, moscow.rockstar.ui.animation.Easing.easeOutCubic);
    private final Animation yawAnimation = new Animation(100L, 0.0f, moscow.rockstar.ui.animation.Easing.easeOutCubic);
    private final Animation targetYawAnimation = new Animation(80L, 0.0f, moscow.rockstar.ui.animation.Easing.easeOutCubic);
    private boolean movementRecentlyChanged;
    private final Timer movementChangeTimer = new Timer();
    private final Timer jumpTimer = new Timer();
    private boolean trackingPlayerTarget;
    private boolean collidingWithTarget;
    private final Timer collisionTimer = new Timer();
    public float previousOcclusionProgress;
    public float occlusionProgress;
    public float occlusionPhase;
    private PlayerEntity trackedPlayer;

    public void updateParticleMotion() {
        if (this.trackedPlayer == null) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = this.trackedPlayer.getPos();
        if (this.particlePosition == null || this.particlePosition.distanceTo(VanillaChestLootTableGenerator) > 10.0) {
            this.particlePosition = VanillaChestLootTableGenerator;
            this.xPositionAnimation.setValue((float)this.particlePosition.x);
            this.yPositionAnimation.setValue((float)this.particlePosition.y);
            this.zPositionAnimation.setValue((float)this.particlePosition.z);
        }
        boolean bl = this.isBlockCollision(this.particlePosition.x, this.particlePosition.y - 0.1, this.particlePosition.z);
        boolean bl2 = this.isBlockCollision(this.particlePosition.x, this.particlePosition.y + 0.11, this.particlePosition.z);
        if (bl) {
            this.particleVelocity = new Vec3d(this.particleVelocity.x, 0.0, this.particleVelocity.z);
            if (bl2) {
                this.particleVelocity = new Vec3d(this.particleVelocity.x, 0.42, this.particleVelocity.z);
            }
        } else {
            this.particleVelocity = this.particleVelocity.add(0.0, -0.08, 0.0);
        }
        Vec3d WallPlayerSkullBlock = this.particlePosition.add(this.particleVelocity);
        if (this.isBlockCollision(WallPlayerSkullBlock.x, WallPlayerSkullBlock.y - 0.01, WallPlayerSkullBlock.z) && this.particleVelocity.y < 0.0) {
            BlockPos object = BlockPos.ofFloored((double)WallPlayerSkullBlock.x, (double)(WallPlayerSkullBlock.y - 0.1), (double)WallPlayerSkullBlock.z);
            WallPlayerSkullBlock = new Vec3d(WallPlayerSkullBlock.x, (double)(object.getY() + 1), WallPlayerSkullBlock.z);
            this.particleVelocity = new Vec3d(this.particleVelocity.x, 0.0, this.particleVelocity.z);
        }
        LivingEntity targetEntity = RockstarClient.create().getFriendManager().getTargetEntity() instanceof LivingEntity livingEntity ? livingEntity : null;
        boolean bl3 = this.trackingPlayerTarget = targetEntity != null && this.trackedPlayer instanceof ClientPlayerEntity;
        if (this.trackingPlayerTarget) {
            Box particleBox = new Box(this.getParticlePosition().subtract(0.4, 0.0, 0.4), this.getParticlePosition().add(0.4, 0.4, 0.4));
            Box targetBox = targetEntity.getBoundingBox().expand((double)-0.1f, 0.0, (double)-0.1f);
            this.particleVelocity = this.particleVelocity.add(targetEntity.getPos().subtract(WallPlayerSkullBlock).normalize().multiply(0.3));
            boolean colliding = particleBox.maxX > targetBox.minX && particleBox.maxY > targetBox.minY && particleBox.maxZ > targetBox.minZ && particleBox.minX < targetBox.maxX && particleBox.minY < targetBox.maxY && particleBox.minZ < targetBox.maxZ;
            if (colliding) {
                this.particleVelocity = this.particleVelocity.multiply(-1.0, 1.0, -1.0);
                this.collidingWithTarget = true;
                this.collisionTimer.reset();
            }
            if (bl && this.jumpTimer.hasElapsed(400L)) {
                this.particleVelocity = new Vec3d(this.particleVelocity.x, 0.35, this.particleVelocity.z);
                WallPlayerSkullBlock = WallPlayerSkullBlock.add(0.0, 0.35, 0.0);
                this.jumpTimer.reset();
            }
        } else if (WallPlayerSkullBlock.distanceTo(VanillaChestLootTableGenerator) > 2.0) {
            this.particleVelocity = this.particleVelocity.add(VanillaChestLootTableGenerator.subtract(WallPlayerSkullBlock).normalize().multiply(0.1));
        }
        if (this.collisionTimer.hasElapsed(500L)) {
            this.collidingWithTarget = false;
        }
        this.updateLookAtTarget(targetEntity);
        this.particlePosition = WallPlayerSkullBlock;
        if (this.particlePosition.distanceTo(VanillaChestLootTableGenerator) < (double)0.1f) {
            this.wanderAngle = MathUtils.interpolateRandomDouble(0.0, 360.0);
            double d = -Math.sin(Math.toRadians(this.wanderAngle)) * 0.1;
            double d2 = Math.cos(Math.toRadians(this.wanderAngle)) * 0.1;
            this.particleVelocity = this.particleVelocity.add(d, 0.0, d2);
        }
        this.particleVelocity = new Vec3d(this.particleVelocity.x * 0.9, this.particleVelocity.y, this.particleVelocity.z * 0.9);
        this.interpolationSteps = 150;
        this.xPositionAnimation.update((float)this.particlePosition.x);
        this.yPositionAnimation.update((float)this.particlePosition.y);
        this.zPositionAnimation.update((float)this.particlePosition.z);
        this.updateOcclusionProgress();
        if (Math.abs(this.particlePosition.x - (double)this.xPositionAnimation.getValue()) > (double)0.1f || Math.abs(this.particlePosition.z - (double)this.zPositionAnimation.getValue()) > (double)0.1f) {
            this.movementChangeTimer.reset();
        }
        this.movementRecentlyChanged = this.movementChangeTimer.hasElapsed(1000L);
    }

    private void updateLookAtTarget(LivingEntity class_13092) {
        Vec3d VanillaChestLootTableGenerator = this.trackedPlayer.getPos().add(0.0, (double)this.trackedPlayer.getEyeHeight(this.trackedPlayer.getPose()), 0.0);
        if (class_13092 != null && this.trackedPlayer instanceof ClientPlayerEntity) {
            VanillaChestLootTableGenerator = class_13092.getPos().add(0.0, (double)class_13092.getEyeHeight(class_13092.getPose()), 0.0);
            Vec3d WallPlayerSkullBlock = this.getParticlePosition();
            double d = VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x;
            double d2 = VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z;
            this.targetYaw = (float)Math.toDegrees(Math.atan2(-d, d2));
        } else if (Math.abs(this.particleVelocity.x) > 0.01 || Math.abs(this.particleVelocity.z) > 0.01) {
            double d = Math.toDegrees(Math.atan2(-this.particleVelocity.x, this.particleVelocity.z));
            this.targetYaw = (float)d;
        }
        if (this.trackedPlayer instanceof ClientPlayerEntity) {
            VanillaChestLootTableGenerator = TaksaParticleState.minecraftClient.gameRenderer.getCamera().getPos();
        }
        float f = MathHelper.wrapDegrees((float)(this.targetYaw - this.smoothedYaw));
        this.smoothedYaw += f * 0.3f;
        this.targetYawAnimation.update(this.smoothedYaw);
        Vec3d VanillaEntityLootTableGenerator = this.getParticlePosition();
        double d = VanillaChestLootTableGenerator.x - VanillaEntityLootTableGenerator.x;
        double d3 = VanillaChestLootTableGenerator.y - VanillaEntityLootTableGenerator.y;
        double d4 = VanillaChestLootTableGenerator.z - VanillaEntityLootTableGenerator.z;
        double d5 = Math.sqrt(d * d + d4 * d4);
        float f2 = (float)Math.toDegrees(Math.atan2(-d, d4));
        float f3 = (float)(-Math.toDegrees(Math.atan2(d3, d5)));
        float f4 = MathHelper.wrapDegrees((float)(f2 - this.targetYawAnimation.getValue()));
        float f5 = this.movementRecentlyChanged ? 90.0f : 70.0f;
        f4 = MathHelper.clamp((float)f4, (float)(-f5), (float)f5);
        f3 = MathHelper.clamp((float)f3, (float)-30.0f, (float)30.0f);
        this.pitchAnimation.update(f4);
        this.yawAnimation.update(f3);
    }

    public void updateOcclusionProgress() {
        double d;
        double d2;
        this.previousOcclusionProgress = this.occlusionProgress;
        double d3 = (double)this.xPositionAnimation.getValue() - this.particlePosition.x;
        float f = MathHelper.sqrt((float)((float)(d3 * d3 + (d2 = 0.0) * d2 + (d = (double)this.zPositionAnimation.getValue() - this.particlePosition.z) * d))) * 4.0f;
        if (f > 1.0f) {
            f = 1.0f;
        }
        this.occlusionProgress += (f - this.occlusionProgress) * 0.4f;
        this.occlusionPhase += this.occlusionProgress;
    }

    private boolean isBlockCollision(double d, double d2, double d3) {
        if (TaksaParticleState.minecraftClient.world == null) {
            return false;
        }
        BlockPos adminsky = BlockPos.ofFloored((double)d, (double)d2, (double)d3);
        BlockState class_26802 = TaksaParticleState.minecraftClient.world.getBlockState(adminsky);
        if (class_26802.isAir()) {
            return false;
        }
        return !class_26802.getCollisionShape((BlockView)TaksaParticleState.minecraftClient.world, adminsky).isEmpty();
    }

    public float getTargetYaw() {
        return this.targetYawAnimation.getValue();
    }

    public float getPitch() {
        return this.pitchAnimation.getValue();
    }

    public float getYaw() {
        return this.yawAnimation.getValue();
    }

    public Vec3d getParticlePosition() {
        return new Vec3d((double)this.xPositionAnimation.getValue(), (double)this.yPositionAnimation.getValue(), (double)this.zPositionAnimation.getValue());
    }

    @Generated
    public boolean isMovementRecentlyChanged() {
        return this.movementRecentlyChanged;
    }

    @Generated
    public boolean isTrackingPlayerTarget() {
        return this.trackingPlayerTarget;
    }

    @Generated
    public boolean isCollidingWithTarget() {
        return this.collidingWithTarget;
    }

    @Generated
    public void setTrackedPlayer(PlayerEntity class_16572) {
        this.trackedPlayer = class_16572;
    }
}
