/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.EquipmentSlot
 *  net.minecraft.LivingEntity
 *  net.minecraft.ItemStack
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3d
 *  net.minecraft.MathHelper
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 *  net.minecraft.PlayerListEntry
 *  net.minecraft.ClientPlayerEntity
 */
package moscow.rockstar.modules.combat.targeting;

import lombok.Generated;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.AimTrajectorySearch;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.math.DoubleLookupTable;
import moscow.rockstar.math.Rotation;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.render.particles.ParticlePhysics;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ClientPlayerEntity;
import pyrock.events.player.InputEvent;

public final class TargetMovementController
implements ClientAccess {
    private static final int TRAJECTORY_STEPS = 11;
    private static final float MAX_PITCH_ANGLE = 90.0f;
    private static final int LOOKAHEAD_SAMPLES = 24;
    private static final double SAMPLE_INTERVAL = 2.0;
    private static final double EYE_HEIGHT_OFFSET = 2.5;
    private static final double MAX_RAYCAST_DISTANCE = 24.0;
    private static final double FALLBACK_RAYCAST_DISTANCE = 32.0;
    private static final int JUMP_COOLDOWN_TICKS = 10;
    private final AimTrajectorySearch trajectorySearch = new AimTrajectorySearch(11);
    private final DoubleLookupTable groundHeightSamples = new DoubleLookupTable(24, 2.0);
    private final ParticlePhysics.ParticleState particleState = new ParticlePhysics.ParticleState();
    private boolean jumpInput;
    private boolean sprintInput;
    private boolean movementActive;
    private boolean jumping;
    private boolean movementInitialized;
    private int jumpCooldownTicks;

    public boolean shouldApplyMovement(ClientPlayerEntity class_7462) {
        return this.movementActive && class_7462.input.hasForwardMovement() && (class_7462.getHungerManager().getFoodLevel() > 6 || class_7462.getAbilities().allowFlying);
    }

    public void updateMovementState() {
        this.movementActive = false;
        this.jumpInput = false;
        this.sprintInput = false;
        ClientPlayerEntity class_7462 = TargetMovementController.minecraftClient.player;
        if (class_7462 == null || TargetMovementController.minecraftClient.world == null || class_7462.networkHandler == null) {
            return;
        }
        if (!this.canControlMovement(class_7462)) {
            this.resetJumpState();
            return;
        }
        boolean bl = class_7462.isGliding();
        boolean bl2 = class_7462.isOnGround();
        if (bl) {
            this.jumpCooldownTicks = 10;
        } else if (this.jumpCooldownTicks > 0) {
            --this.jumpCooldownTicks;
        }
        if (!class_7462.input.hasForwardMovement()) {
            this.resetJumpState();
            return;
        }
        boolean bl3 = bl2 || !bl && this.canPredictAirborneMovement(class_7462);
        boolean bl4 = this.movementInitialized ? this.jumping : TargetMovementController.minecraftClient.options.jumpKey.isPressed();
        this.movementActive = true;
        this.movementInitialized = true;
        this.jumpInput = bl3 && !bl4;
        this.sprintInput = true;
        this.jumping = this.jumpInput;
        if (bl || this.jumpCooldownTicks > 0) {
            this.updateTrajectoryAndRotation(class_7462, bl, bl2);
        }
    }

    public void applyInputOverrides(InputEvent inputEvent) {
        if (!this.movementActive) {
            return;
        }
        inputEvent.setJump(this.jumpInput);
        inputEvent.setSprint(inputEvent.isSprint() || this.sprintInput);
    }

    public void resetMovementState() {
        this.movementActive = false;
        this.jumpInput = false;
        this.sprintInput = false;
        this.jumpCooldownTicks = 0;
        this.resetJumpState();
    }

    public void resetJumpState() {
        this.jumping = false;
        this.movementInitialized = false;
    }

    private void updateTrajectoryAndRotation(ClientPlayerEntity class_7462, boolean bl, boolean bl2) {
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        if (rotationManager == null) {
            return;
        }
        float f = rotationManager.isIdle() ? class_7462.getPitch() : rotationManager.getCurrentRotation().getPitch();
        double d = Math.toRadians(class_7462.getYaw());
        double d2 = -Math.sin(d);
        double d3 = Math.cos(d);
        this.sampleGroundHeights(class_7462, d2, d3);
        this.populateParticleState(class_7462, bl, bl2, d2, d3);
        float f2 = this.trajectorySearch.findBestTrajectoryAngle(this.particleState, f, 90.0f, this.groundHeightSamples, this.getLatencyLookahead());
        rotationManager.requestRotation(new Rotation(class_7462.getYaw(), MathHelper.clamp((float)f2, (float)-90.0f, (float)90.0f)), RotationCorrectionMode.STRICT, 180.0f, 180.0f, 180.0f, RotationPriority.STANDARD_PRIORITY);
    }

    private void populateParticleState(ClientPlayerEntity class_7462, boolean bl, boolean bl2, double d, double d2) {
        Vec3d VanillaChestLootTableGenerator = class_7462.getVelocity();
        this.particleState.position = 0.0;
        this.particleState.verticalPosition = class_7462.getY();
        this.particleState.verticalVelocity = Math.max(0.0, VanillaChestLootTableGenerator.x * d + VanillaChestLootTableGenerator.z * d2);
        this.particleState.horizontalVelocity = VanillaChestLootTableGenerator.y;
        this.particleState.airborne = bl;
        this.particleState.grounded = bl2;
        this.particleState.colliding = this.jumping;
        this.particleState.remainingBoostTicks = -1;
    }

    private int getLatencyLookahead() {
        PlayerListEntry ServerSamplerSource;
        int n = 0;
        if (minecraftClient.getNetworkHandler() != null && TargetMovementController.minecraftClient.player != null && (ServerSamplerSource = minecraftClient.getNetworkHandler().getPlayerListEntry(TargetMovementController.minecraftClient.player.getUuid())) != null) {
            n = ServerSamplerSource.getLatency();
        }
        return MathHelper.clamp((int)(1 + n / 50), (int)1, (int)6);
    }

    private void sampleGroundHeights(ClientPlayerEntity class_7462, double d, double d2) {
        this.groundHeightSamples.clear();
        Vec3d VanillaChestLootTableGenerator = class_7462.getPos();
        for (int i = 0; i < this.groundHeightSamples.getCapacity(); ++i) {
            double d3 = (double)i * 2.0;
            this.groundHeightSamples.addSample(this.resolveGroundHeight(class_7462, VanillaChestLootTableGenerator.x + d * d3, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z + d2 * d3));
        }
    }

    private double resolveGroundHeight(ClientPlayerEntity class_7462, double d, double d2, double d3) {
        double d4 = this.raycastGroundHeight(class_7462, d, d2 + 2.5, d3, 32.0);
        if (!Double.isNaN(d4)) {
            return d4;
        }
        if (TargetMovementController.minecraftClient.world.getBlockState(BlockPos.ofFloored((double)d, (double)(d2 + 2.5), (double)d3)).isAir()) {
            return -4096.0;
        }
        double d5 = this.raycastGroundHeight(class_7462, d, d2 + 24.0, d3, 21.5);
        return Double.isNaN(d5) ? d2 + 24.0 : d5;
    }

    private double raycastGroundHeight(ClientPlayerEntity class_7462, double d, double d2, double d3, double d4) {
        Vec3d VanillaChestLootTableGenerator = new Vec3d(d, d2, d3);
        BlockHitResult class_39652 = TargetMovementController.minecraftClient.world.raycast(new RaycastContext(VanillaChestLootTableGenerator, VanillaChestLootTableGenerator.add(0.0, -d4, 0.0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, (Entity)class_7462));
        return class_39652.getType() == HitResult.Type.MISS ? Double.NaN : class_39652.getPos().y;
    }

    private boolean canControlMovement(ClientPlayerEntity class_7462) {
        return TargetMovementController.minecraftClient.currentScreen == null && !class_7462.isSpectator() && !class_7462.hasVehicle() && !class_7462.isClimbing() && !class_7462.isTouchingWater() && !class_7462.isInLava() && !class_7462.getAbilities().flying && !this.findEquippedArmorStack(class_7462).isEmpty();
    }

    private boolean canPredictAirborneMovement(ClientPlayerEntity class_7462) {
        return !class_7462.isOnGround() && !class_7462.isTouchingWater() && !this.findEquippedArmorStack(class_7462).isEmpty();
    }

    private ItemStack findEquippedArmorStack(ClientPlayerEntity class_7462) {
        for (EquipmentSlot class_13042 : EquipmentSlot.VALUES) {
            ItemStack class_17992 = class_7462.getEquippedStack(class_13042);
            if (!LivingEntity.canGlideWith((ItemStack)class_17992, (EquipmentSlot)class_13042)) continue;
            return class_17992;
        }
        return ItemStack.EMPTY;
    }

    @Generated
    public boolean isJumping() {
        return this.jumpInput;
    }

    @Generated
    public boolean isSprinting() {
        return this.sprintInput;
    }

    @Generated
    public boolean isMovementActive() {
        return this.movementActive;
    }
}

