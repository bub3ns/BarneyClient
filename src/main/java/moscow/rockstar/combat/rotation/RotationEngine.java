/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  moscow.rockstar.modules.other.admin.BlockPos$Mutable
 *  net.minecraft.Hand
 *  net.minecraft.Entity
 *  net.minecraft.EquipmentSlot
 *  net.minecraft.PlayerEntity
 *  net.minecraft.PlayerInventory
 *  net.minecraft.ItemStack
 *  net.minecraft.Items
 *  net.minecraft.BlockView
 *  net.minecraft.World
 *  net.minecraft.Blocks
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.BlockState
 *  net.minecraft.ClientCommandC2SPacket
 *  net.minecraft.ClientCommandC2SPacket$Mode
 *  net.minecraft.Heightmap$Type
 *  net.minecraft.FluidTags
 *  net.minecraft.MathHelper
 *  net.minecraft.FluidState
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 *  net.minecraft.ClientPlayerEntity
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.combat.rotation;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import moscow.rockstar.api.validation.ScreenStateService;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationRequestHelper;
import moscow.rockstar.combat.rotation.RotationState;
import moscow.rockstar.core.ClientServiceRegistry;
import moscow.rockstar.math.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.ui.notifications.NotificationBridge;
import moscow.rockstar.world.BlockDropResolver;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.world.Heightmap;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.fluid.FluidState;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class RotationEngine
implements ScreenStateService {
    private static final double GRAVITY_ACCELERATION = 0.08;
    private final int targetBlockX;
    private final int targetBlockY;
    private final int targetBlockZ;
    private final boolean targetHasFixedHeight;
    private final NavigationTuning navigationTuning = new NavigationTuning();
    private boolean paused;
    private boolean targetReached;
    @Nullable
    private String errorMessage;
    private boolean navigationStarted;
    private boolean takeoffStarted;
    private int navigationTick;
    private int progressStallTicks;
    private int takeoffPhaseTicks;
    private int lastTakeoffPacketTick = -100;
    private int lastFireworkTick = -1000;
    private double targetDistanceSquared = Double.MAX_VALUE;
    private double initialHeight;
    private float currentYaw;
    private float currentPitch;
    private boolean flightPathValid;
    private NavigationState navigationState;
    private int takeoffPhaseIndex;
    private boolean jumpPressedLastTick;
    private int initializationTicks;
    private int turnaroundTicksRemaining;
    private boolean turningAroundObstacle;
    private boolean turnLeft;
    private boolean previousTurnLeft;
    private int recoveryNavigationTick;
    private int pathStallTicks;
    private boolean altitudeRecoveryActive;
    private int altitudeRecoveryTicks;
    private double recoveryAltitude;
    private int altitudeRecoveryAttempts;
    private boolean landingRecoveryActive;
    private int landingRecoveryTicks;
    @Nullable
    private Vec3d recoveryTarget;
    private long recoveryTargetTick;
    @Nullable
    private CompletableFuture<List<Vec3d>> candidatePathFuture;
    private List<Vec3d> candidatePath;
    private int candidatePathIndex;
    private int candidatePathTick = -10000;
    private volatile List<Vec3d> trajectoryPoints;

    public RotationEngine(int n, int n2, int n3, boolean bl) {
        this.navigationState = NavigationState.GROUNDED;
        this.recoveryTargetTick = Long.MIN_VALUE;
        this.candidatePath = List.of();
        this.trajectoryPoints = List.of();
        this.targetBlockX = n;
        this.targetBlockY = n2;
        this.targetBlockZ = n3;
        this.targetHasFixedHeight = bl;
    }

    public RotationEngine(BlockPos adminsky) {
        this(adminsky.getX(), adminsky.getY(), adminsky.getZ(), true);
    }

    @Override
    public String getCommandName() {
        return "elytra";
    }

    @Override
    public String getStatusMessage() {
        if (this.paused) {
            return "\u043f\u0430\u0443\u0437\u0430";
        }
        if (this.targetReached) {
            return "\u0433\u043e\u0442\u043e\u0432\u043e";
        }
        if (this.landingRecoveryActive) {
            return "\u043f\u043e\u0441\u0430\u0434\u043a\u0430";
        }
        int n = (int)Math.round(Math.sqrt(Math.max(0.0, this.targetDistanceSquared)));
        if (!this.navigationStarted) {
            return "\u0441\u0442\u0430\u0440\u0442";
        }
        if (this.turningAroundObstacle) {
            return "\u043e\u0431\u0445\u043e\u0434 \u0442\u0443\u043f\u0438\u043a\u0430";
        }
        if (this.altitudeRecoveryActive) {
            return "\u043e\u0431\u0445\u043e\u0434 \u043f\u043e \u0432\u044b\u0441\u043e\u0442\u0435";
        }
        return (this.flightPathValid ? "\u0430\u0432\u0430\u0440\u0438\u0439\u043d\u044b\u0439 \u043d\u0430\u0431\u043e\u0440 \u0432\u044b\u0441\u043e\u0442\u044b, " : "") + "\u0434\u043e \u0446\u0435\u043b\u0438 " + n + "\u043c";
    }

    public List<Vec3d> getTrajectoryPoints() {
        return this.trajectoryPoints;
    }

    public Vec3d getDestinationPosition() {
        return new Vec3d((double)this.targetBlockX + 0.5, this.targetHasFixedHeight ? (double)this.targetBlockY + 0.5 : this.getTargetHeight(), (double)this.targetBlockZ + 0.5);
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public boolean tickNavigation() {
        if (this.paused) {
            return false;
        }
        if (this.targetReached) {
            return true;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return false;
        }
        ClientPlayerEntity player = client.player;
        World world = client.world;
        ++this.navigationTick;
        RotationRequestHelper.refreshRotation();
        if (!this.hasElytraEquipped(player)) {
            NotificationBridge.showPersistentMessage("\u042d\u043b\u0438\u0442\u0440\u0430 \u043d\u0435 \u043d\u0430\u0434\u0435\u0442\u0430");
            this.errorMessage = "\u044d\u043b\u0438\u0442\u0440\u0430 \u043d\u0435 \u043d\u0430\u0434\u0435\u0442\u0430";
            this.stopNavigation();
            return true;
        }
        if (!this.landingRecoveryActive && this.hasUsableFirework(player)) {
            this.beginLandingRecovery("\u042d\u043b\u0438\u0442\u0440\u0430 \u043f\u043e\u0447\u0442\u0438 \u0441\u043b\u043e\u043c\u0430\u043d\u0430");
        }
        if (!this.navigationStarted) {
            this.currentYaw = player.getYaw();
            this.currentPitch = MathHelper.clamp((float)player.getPitch(), (float)(-this.navigationTuning.MAX_PITCH_ANGLE), (float)this.navigationTuning.MAX_PITCH_ANGLE);
            boolean flatWorld = world.getDimension().hasSkyLight();
            this.initialHeight = flatWorld ? MathHelper.clamp(player.getY(), this.navigationTuning.MIN_FLAT_WORLD_HEIGHT, this.navigationTuning.MAX_FLAT_WORLD_HEIGHT) : MathHelper.clamp(player.getY(), this.navigationTuning.MIN_WORLD_HEIGHT, this.navigationTuning.MAX_WORLD_HEIGHT);
            this.navigationStarted = true;
        }
        if (this.landingRecoveryActive) {
            return this.updateLandingRecovery(client, player, world);
        }
        double distanceSquared = this.getDistanceSquaredToTarget(player);
        if (this.isWithinApproachRange(player)) {
            NotificationBridge.showMessage("\u0414\u043e\u043b\u0435\u0442\u0435\u043b\u0438 \u0434\u043e \u0446\u0435\u043b\u0438");
            this.targetReached = true;
            this.resetNavigation();
            return true;
        }
        RotationState rotationState = ClientServiceRegistry.getInstance().getRotationState();
        rotationState.enableMovementOverride();
        rotationState.setBackwardPressed(false);
        rotationState.setStrafeRightPressed(false);
        rotationState.setStrafeLeftPressed(false);
        rotationState.setSneakPressed(false);
        if (!player.isGliding()) {
            this.handleElytraTakeoff(client, player, world, rotationState);
            return false;
        }
        this.takeoffPhaseTicks = 0;
        this.navigationState = NavigationState.GROUNDED;
        this.takeoffStarted = false;
        rotationState.setForwardPressed(true);
        rotationState.setSprintPressed(true);
        rotationState.setJumpPressed(false);
        this.updateDistanceProgress(distanceSquared);
        if (this.progressStallTicks >= this.navigationTuning.MAX_NAVIGATION_TICKS) {
            this.beginLandingRecovery("\u041d\u0435 \u043f\u043e\u043b\u0443\u0447\u0430\u0435\u0442\u0441\u044f \u043f\u0440\u0438\u0431\u043b\u0438\u0437\u0438\u0442\u044c\u0441\u044f \u043a \u0446\u0435\u043b\u0438");
            return this.updateLandingRecovery(client, player, world);
        }
        this.requestCandidatePath(world, player);
        this.updateRecoveryState(world, player);
        RotationResult desiredRotation = this.calculateBestRotation(world, player);
        desiredRotation = this.applyObstacleAvoidance(world, player, desiredRotation);
        this.flightPathValid = desiredRotation.isValid();
        if (desiredRotation.isValid() && desiredRotation.getFlightTicks() <= 8) {
            ++this.pathStallTicks;
        } else {
            this.pathStallTicks = Math.max(0, this.pathStallTicks - 2);
        }
        Objects.requireNonNull(this.navigationTuning);
        if (this.pathStallTicks > 12) {
            this.beginLandingRecovery("\u0417\u0430\u0436\u0430\u0442 \u0432 \u0443\u0437\u043a\u043e\u043c \u043f\u0440\u043e\u0441\u0442\u0440\u0430\u043d\u0441\u0442\u0432\u0435");
            return this.updateLandingRecovery(client, player, world);
        }
        float yawStep = this.flightPathValid ? this.navigationTuning.MAX_YAW_ROTATION_STEP : 11.0f;
        float pitchStep = this.flightPathValid ? 11.0f : 6.0f;
        this.currentYaw = RotationEngine.stepAngleTowards(this.currentYaw, desiredRotation.getYaw(), yawStep);
        this.currentPitch = RotationEngine.stepAngleTowards(this.currentPitch, desiredRotation.getPitch(), pitchStep);
        this.currentPitch = MathHelper.clamp((float)this.currentPitch, (float)(-this.navigationTuning.MAX_PITCH_ANGLE), (float)this.navigationTuning.MAX_PITCH_ANGLE);
        Rotation rotation = new Rotation(this.currentYaw, this.currentPitch);
        RotationRequestHelper.requestDirectRotation(rotation, 45.0f, this.navigationTuning.ROTATION_PITCH_LIMIT, 45.0f);
        this.handleFlightResult(client, player, desiredRotation);
        return false;
    }

    private void handleElytraTakeoff(MinecraftClient client, ClientPlayerEntity class_7462, World class_19372, RotationState rotationState) {
        float f;
        boolean bl;
        ++this.takeoffPhaseTicks;
        if (!this.takeoffStarted) {
            NotificationBridge.showMessage("\u0412\u0437\u043b\u0435\u0442\u0430\u044e \u043d\u0430 \u044d\u043b\u0438\u0442\u0440\u0435...");
            this.takeoffStarted = true;
        }
        if (this.takeoffPhaseTicks > this.navigationTuning.TAKEOFF_WINDOW_TICKS) {
            if (class_7462.isOnGround()) {
                NotificationBridge.showPersistentMessage("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0432\u0437\u043b\u0435\u0442\u0435\u0442\u044c (\u043d\u0435\u0442 \u043c\u0435\u0441\u0442\u0430 \u043d\u0430\u0434 \u0433\u043e\u043b\u043e\u0432\u043e\u0439 \u0438\u043b\u0438 \u0444\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a\u043e\u0432)");
                this.stopNavigation();
            } else {
                this.beginLandingRecovery("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0440\u0430\u0437\u043e\u0433\u043d\u0430\u0442\u044c\u0441\u044f \u043d\u0430 \u044d\u043b\u0438\u0442\u0440\u0435");
            }
            return;
        }
        if (class_7462.isOnGround() && !this.hasTakeoffClearance(class_19372, class_7462)) {
            NotificationBridge.showPersistentMessage("\u041d\u0430\u0434 \u0433\u043e\u043b\u043e\u0432\u043e\u0439 \u043d\u0435\u0442 \u043c\u0435\u0441\u0442\u0430 \u0434\u043b\u044f \u0432\u0437\u043b\u0451\u0442\u0430 \u043d\u0430 \u044d\u043b\u0438\u0442\u0440\u0435");
            this.stopNavigation();
            return;
        }
        rotationState.setForwardPressed(false);
        rotationState.setSprintPressed(false);
        if (class_7462.isOnGround()) {
            this.navigationState = NavigationState.GROUNDED;
            this.takeoffPhaseIndex = 0;
            bl = true;
        } else {
            switch (this.navigationState.ordinal()) {
                case 0: {
                    this.navigationState = NavigationState.TAKEOFF;
                    this.takeoffPhaseIndex = 0;
                    bl = false;
                    break;
                }
                case 1: {
                    ++this.takeoffPhaseIndex;
                    bl = false;
                    Objects.requireNonNull(this.navigationTuning);
                    if (this.takeoffPhaseIndex < 2) break;
                    this.navigationState = NavigationState.GLIDING;
                    this.takeoffPhaseIndex = 0;
                    break;
                }
                case 2: {
                    bl = true;
                    if (client.getNetworkHandler() != null && this.navigationTick - this.lastTakeoffPacketTick >= 2) {
                        client.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)class_7462, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                        this.lastTakeoffPacketTick = this.navigationTick;
                    }
                    ++this.takeoffPhaseIndex;
                    if (this.takeoffPhaseIndex < this.navigationTuning.JUMP_HOLD_TICKS) break;
                    this.navigationState = NavigationState.TAKEOFF;
                    this.takeoffPhaseIndex = 0;
                    bl = false;
                    break;
                }
                default: {
                    bl = false;
                }
            }
        }
        rotationState.setJumpPressed(bl && !this.jumpPressedLastTick || bl && class_7462.isOnGround());
        this.jumpPressedLastTick = bl;
        this.currentYaw = f = this.getTargetYaw(class_7462);
        this.currentPitch = this.navigationTuning.TAKEOFF_PITCH;
        Rotation rotation = new Rotation(f, this.navigationTuning.TAKEOFF_PITCH);
        Objects.requireNonNull(this.navigationTuning);
        float f2 = this.navigationTuning.ROTATION_PITCH_LIMIT;
        Objects.requireNonNull(this.navigationTuning);
        RotationRequestHelper.requestDirectRotation(rotation, 45.0f, f2, 45.0f);
    }

    private boolean hasTakeoffClearance(World class_19372, ClientPlayerEntity class_7462) {
        BlockPos.Mutable class_23392 = new BlockPos.Mutable();
        int n = MathHelper.floor((double)class_7462.getX());
        int n2 = MathHelper.floor((double)class_7462.getZ());
        int n3 = MathHelper.floor((double)class_7462.getY());
        for (int i = 2; i <= 3; ++i) {
            class_23392.set(n, n3 + i, n2);
            if (class_19372.getBlockState((BlockPos)class_23392).getCollisionShape((BlockView)class_19372, (BlockPos)class_23392).isEmpty()) continue;
            return false;
        }
        return true;
    }

    private void handleFlightResult(MinecraftClient client, ClientPlayerEntity class_7462, RotationResult rotationResult) {
        block13: {
            block12: {
                if (this.landingRecoveryActive) {
                    return;
                }
                if (this.turningAroundObstacle) {
                    return;
                }
                Objects.requireNonNull(this.navigationTuning);
                if (this.navigationTick - this.lastFireworkTick < 12) {
                    return;
                }
                if (this.altitudeRecoveryActive && this.recoveryAltitude > class_7462.getY() + 4.0) {
                    if (rotationResult.getFlightTicks() >= this.navigationTuning.MAX_ROTATION_SAMPLES || rotationResult.getPitch() < -this.navigationTuning.MAX_APPROACH_SPEED) {
                        this.useFirework(client, class_7462);
                    }
                    return;
                }
                if (rotationResult.isValid()) {
                    if (rotationResult.getPitch() < -this.navigationTuning.MAX_APPROACH_SPEED) {
                        this.useFirework(client, class_7462);
                    }
                    return;
                }
                if (rotationResult.getFlightTicks() < this.navigationTuning.MAX_ROTATION_SAMPLES) {
                    return;
                }
                if (this.navigationTick - this.lastFireworkTick < this.navigationTuning.PROGRESS_CHECK_INTERVAL) {
                    return;
                }
                double d = this.getHorizontalSpeed(class_7462);
                Objects.requireNonNull(this.navigationTuning);
                if (d < (double)1.05f) break block12;
                Objects.requireNonNull(this.navigationTuning);
                if (!(this.currentPitch < -6.0f)) break block13;
            }
            this.useFirework(client, class_7462);
        }
    }

    private void useFirework(MinecraftClient client, ClientPlayerEntity class_7462) {
        Hand class_12682;
        if (client.interactionManager == null) {
            return;
        }
        if (class_7462.getOffHandStack().isOf(Items.FIREWORK_ROCKET)) {
            class_12682 = Hand.OFF_HAND;
        } else if (this.hasFirework(class_7462)) {
            class_12682 = Hand.MAIN_HAND;
        } else {
            return;
        }
        client.interactionManager.interactItem((PlayerEntity)class_7462, class_12682);
        this.lastFireworkTick = this.navigationTick;
    }

    private boolean hasFirework(ClientPlayerEntity class_7462) {
        if (class_7462.getMainHandStack().isOf(Items.FIREWORK_ROCKET)) {
            return true;
        }
        PlayerInventory class_16612 = class_7462.getInventory();
        for (int i = 0; i < 9; ++i) {
            if (!class_16612.getStack(i).isOf(Items.FIREWORK_ROCKET)) continue;
            BlockDropResolver.setSelectedToolSlot(i);
            return true;
        }
        return false;
    }

    private boolean isWithinCooldownWindow() {
        Objects.requireNonNull(this.navigationTuning);
        return this.navigationTick - this.lastFireworkTick < 30;
    }

    private void requestCandidatePath(World class_19372, ClientPlayerEntity class_7462) {
        boolean bl;
        if (this.candidatePathFuture != null) {
            if (this.candidatePathFuture.isDone()) {
                List list = this.candidatePathFuture.getNow(List.of());
                this.candidatePathFuture = null;
                if (!list.isEmpty()) {
                    this.candidatePath = list;
                    this.candidatePathIndex = 0;
                }
            }
            return;
        }
        boolean bl2 = bl = this.candidatePath.isEmpty() || this.candidatePathIndex >= this.candidatePath.size() || this.navigationTick - this.candidatePathTick > this.navigationTuning.CANDIDATE_REFRESH_TICKS || this.candidatePath.get(this.candidatePathIndex).distanceTo(class_7462.getPos()) > this.navigationTuning.CANDIDATE_MAX_DISTANCE;
        if (!bl) {
            return;
        }
        this.candidatePathTick = this.navigationTick;
            this.candidatePathFuture = FlightPathPlanner.plan(class_19372, class_7462.getPos(), this.getDestinationPosition());
    }

    @Nullable
    private Vec3d selectNextCandidatePoint(World class_19372, ClientPlayerEntity class_7462) {
        while (this.candidatePathIndex < this.candidatePath.size() && this.candidatePath.get(this.candidatePathIndex).distanceTo(class_7462.getPos()) < this.navigationTuning.CANDIDATE_MIN_DISTANCE) {
            ++this.candidatePathIndex;
        }
        if (this.candidatePathIndex + 1 < this.candidatePath.size()) {
            Vec3d VanillaChestLootTableGenerator = this.candidatePath.get(this.candidatePathIndex + 1);
            Vec3d WallPlayerSkullBlock = class_7462.getEyePos();
            BlockHitResult class_39652 = class_19372.raycast(new RaycastContext(WallPlayerSkullBlock, VanillaChestLootTableGenerator, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, (Entity)class_7462));
            if (class_39652.getType() == HitResult.Type.MISS) {
                ++this.candidatePathIndex;
            }
        }
        if (this.candidatePathIndex >= this.candidatePath.size()) {
            return null;
        }
        return this.candidatePath.get(this.candidatePathIndex);
    }

    /*
     * WARNING - void declaration
     */
    private RotationResult calculateBestRotation(World class_19372, ClientPlayerEntity class_7462) {
        double d;
        double d2;
        double d3;
        int n;
        Vec3d VanillaChestLootTableGenerator;
        Vec3d WallPlayerSkullBlock = class_7462.getPos();
        Vec3d VanillaEntityLootTableGenerator = class_7462.getVelocity();
        Vec3d PlayerSkullBlock = null;
        float f = this.navigationTuning.TAKEOFF_PITCH_LIMIT;
        if (!(this.landingRecoveryActive || this.altitudeRecoveryActive || this.turningAroundObstacle)) {
            Vec3d RedstoneBlock = VanillaChestLootTableGenerator = this.candidatePath.isEmpty() ? null : this.selectNextCandidatePoint(class_19372, class_7462);
            if (VanillaChestLootTableGenerator != null) {
                PlayerSkullBlock = VanillaChestLootTableGenerator;
                if (VanillaChestLootTableGenerator.y > class_7462.getY() + 2.0) {
                    f = this.navigationTuning.RECOVERY_PITCH;
                }
            }
        }
        if (PlayerSkullBlock == null) {
            PlayerSkullBlock = this.adjustFlightTarget(class_19372, class_7462, this.getActiveNavigationTarget());
        }
        VanillaChestLootTableGenerator = PlayerSkullBlock.subtract(class_7462.getEyePos());
        double d4 = Math.hypot(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.z);
        float f2 = (float)Math.toDegrees(Math.atan2(VanillaChestLootTableGenerator.z, VanillaChestLootTableGenerator.x)) - 90.0f;
        float f3 = MathHelper.clamp((float)((float)(-Math.toDegrees(Math.atan2(VanillaChestLootTableGenerator.y, Math.max(0.001, d4))))), (float)(-f), (float)f);
        if (this.turningAroundObstacle) {
            f2 += this.turnLeft ? 90.0f : -90.0f;
            f3 = MathHelper.clamp((float)f3, (float)(-this.navigationTuning.TAKEOFF_PITCH_LIMIT), (float)0.0f);
        }
        if (class_19372.getDimension().hasSkyLight() && class_7462.getY() > (double)this.navigationTuning.HIGH_ALTITUDE_THRESHOLD) {
            f3 = Math.max(f3, 8.0f);
        }
        if (this.landingRecoveryActive && this.recoveryTarget != null) {
            float f4;
            double d5 = class_7462.getY() - this.recoveryTarget.y;
            if (d5 < this.navigationTuning.MIN_CLEARANCE_DISTANCE) {
                Objects.requireNonNull(this.navigationTuning);
                f4 = 6.0f;
            } else {
                f4 = this.navigationTuning.RECOVERY_YAW_ANGLE;
            }
            float f5 = f4;
            f3 = MathHelper.clamp((float)f3, (float)(-this.navigationTuning.MAX_PITCH_ANGLE), (float)f5);
        }
        if (this.isWithinCooldownWindow()) {
            Objects.requireNonNull(this.navigationTuning);
            n = 30 - (this.navigationTick - this.lastFireworkTick);
        } else {
            n = 0;
        }
        int n2 = n;
        RotationResult rotationResult = null;
        PathProbeResult pathProbeResult = null;
        double d6 = Double.NEGATIVE_INFINITY;
        for (float f5 : this.navigationTuning.PITCH_SAMPLE_OFFSETS) {
            for (float f6 : this.navigationTuning.YAW_SAMPLE_OFFSETS) {
                double d5;
                float f7 = f2 + f5;
                float f8 = MathHelper.clamp((float)(f3 + f6), (float)(-this.navigationTuning.MAX_PITCH_ANGLE), (float)this.navigationTuning.MAX_PITCH_ANGLE);
                PathProbeResult pathProbeResult2 = this.simulateFlightPath(class_19372, WallPlayerSkullBlock, VanillaEntityLootTableGenerator, this.currentYaw, this.currentPitch, f7, f8, this.navigationTuning.NORMAL_YAW_STEP, this.navigationTuning.NORMAL_PITCH_STEP, n2, this.navigationTuning.PATH_SIMULATION_STEPS);
                if (!pathProbeResult2.collisionDetected) {
                    d5 = (double)this.navigationTuning.PATH_SIMULATION_STEPS * 1000.0;
                } else {
                    int n3 = this.navigationTuning.PATH_SIMULATION_STEPS - pathProbeResult2.stepsCompleted;
                    d5 = (double)pathProbeResult2.stepsCompleted * 100.0 - (double)(n3 * n3) * 5.0;
                }
                double d7 = (double)Math.abs(AimRotationMath.getWrappedAngleDifference(f7, f2)) * this.navigationTuning.YAW_ERROR_WEIGHT + (double)Math.abs(f8 - f3) * this.navigationTuning.PITCH_ERROR_WEIGHT;
                d3 = (double)Math.abs(AimRotationMath.getWrappedAngleDifference(this.currentYaw, f7)) * this.navigationTuning.CURRENT_ROTATION_WEIGHT + (double)Math.abs(this.currentPitch - f8) * this.navigationTuning.CURRENT_ROTATION_WEIGHT;
                d2 = (Math.sqrt(this.targetDistanceSquared) - pathProbeResult2.finalPosition.distanceTo(this.getDestinationPosition())) * this.navigationTuning.DISTANCE_WEIGHT;
                d = d5 - d7 - d3 + d2;
                if (rotationResult != null && !(d > d6)) continue;
                d6 = d;
                rotationResult = new RotationResult(f7, f8, pathProbeResult2.stepsCompleted, false);
                pathProbeResult = pathProbeResult2;
            }
        }
        if (rotationResult == null) {
            this.trajectoryPoints = List.of();
            return new RotationResult(this.currentYaw, -this.navigationTuning.MAX_PITCH_ANGLE, 0, true);
        }
        if (pathProbeResult.collisionDetected) {
            RotationResult emergencyRotation = null;
            PathProbeResult pathProbeResult3 = null;
            double d8 = Double.NEGATIVE_INFINITY;
            for (float f6 : this.navigationTuning.EMERGENCY_YAW_OFFSETS) {
                for (float f9 : this.navigationTuning.EMERGENCY_PITCH_OFFSETS) {
                    float f10 = f2 + f6;
                    PathProbeResult pathProbeResult4 = this.simulateFlightPath(class_19372, WallPlayerSkullBlock, VanillaEntityLootTableGenerator, this.currentYaw, this.currentPitch, f10, f9, this.navigationTuning.EMERGENCY_YAW_STEP, this.navigationTuning.EMERGENCY_PITCH_STEP, n2, this.navigationTuning.PATH_SIMULATION_STEPS);
                    int n4 = pathProbeResult4.collisionDetected ? pathProbeResult4.stepsCompleted : this.navigationTuning.PATH_SIMULATION_STEPS;
                    d3 = Math.abs(AimRotationMath.getWrappedAngleDifference(this.currentYaw, f10));
                    d2 = Math.abs(AimRotationMath.getWrappedAngleDifference(f10, f2));
                    d = (double)n4 * 10000.0 - d3 * 10.0 - d2;
                    if (emergencyRotation != null && !(d > d8)) continue;
                    d8 = d;
                    emergencyRotation = new RotationResult(f10, f9, n4, true);
                    pathProbeResult3 = pathProbeResult4;
                }
            }
            int n5 = emergencyRotation != null ? emergencyRotation.getFlightTicks() : 0;
            Objects.requireNonNull(this.navigationTuning);
            if (n5 <= 8) {
                this.trajectoryPoints = List.copyOf(pathProbeResult.trajectoryPoints);
                return new RotationResult(this.currentYaw, -this.navigationTuning.MAX_PITCH_ANGLE, n5, true);
            }
            if (emergencyRotation != null && emergencyRotation.getFlightTicks() >= rotationResult.getFlightTicks()) {
                this.trajectoryPoints = List.copyOf(pathProbeResult3.trajectoryPoints);
                return emergencyRotation;
            }
            this.trajectoryPoints = List.copyOf(pathProbeResult.trajectoryPoints);
            return new RotationResult(rotationResult.getYaw(), Math.min(rotationResult.getPitch(), -25.0f), rotationResult.getFlightTicks(), true);
        }
        this.trajectoryPoints = List.copyOf(pathProbeResult.trajectoryPoints);
        return rotationResult;
    }

    private void updateRecoveryState(World class_19372, ClientPlayerEntity class_7462) {
        if (this.turningAroundObstacle) {
            --this.turnaroundTicksRemaining;
            if (this.turnaroundTicksRemaining <= 0) {
                this.turningAroundObstacle = false;
                this.initializationTicks = 0;
            }
            return;
        }
        if (this.altitudeRecoveryActive) {
            boolean bl;
            --this.altitudeRecoveryTicks;
            boolean bl2 = bl = Math.abs(class_7462.getY() - this.recoveryAltitude) < 4.0;
            if (this.altitudeRecoveryTicks <= 0 || bl) {
                this.altitudeRecoveryActive = false;
                this.initializationTicks = 0;
            }
            return;
        }
        this.initializationTicks = this.flightPathValid ? ++this.initializationTicks : Math.max(0, this.initializationTicks - 2);
        if (this.initializationTicks < this.navigationTuning.INITIALIZATION_TICKS) {
            return;
        }
        if (this.navigationTick >= this.navigationTuning.MIN_STEERING_TICKS) {
            Objects.requireNonNull(this.navigationTuning);
            if (this.altitudeRecoveryAttempts < 2) {
                boolean bl = class_19372.getDimension().hasSkyLight();
                this.recoveryAltitude = bl ? Math.max((double)this.navigationTuning.MIN_FLAT_WORLD_HEIGHT, class_7462.getY() - 24.0) : Math.min((double)this.navigationTuning.MAX_WORLD_HEIGHT, Math.max(class_7462.getY() + 24.0, this.estimateTerrainHeight(class_19372, class_7462) + 18.0));
                this.altitudeRecoveryActive = true;
                this.altitudeRecoveryTicks = this.navigationTuning.RECOVERY_PITCH_TICKS;
                ++this.altitudeRecoveryAttempts;
                this.recoveryNavigationTick = 0;
                this.initializationTicks = 0;
                NotificationBridge.showMessage("\u041e\u0431\u0445\u043e\u0434 \u043d\u0435 \u043f\u043e\u043c\u043e\u0433 \u2014 \u043c\u0435\u043d\u044f\u044e \u0432\u044b\u0441\u043e\u0442\u0443 (" + (int)this.recoveryAltitude + ")");
                return;
            }
            this.beginLandingRecovery("\u041d\u0435 \u043f\u043e\u043b\u0443\u0447\u0430\u0435\u0442\u0441\u044f \u043d\u0430\u0439\u0442\u0438 \u043f\u0440\u043e\u0445\u043e\u0434");
            return;
        }
        if (this.navigationTick > 0) {
            this.turnLeft = !this.previousTurnLeft;
        } else {
            Vec3d VanillaChestLootTableGenerator = this.getDestinationPosition().subtract(class_7462.getEyePos());
            float f = (float)Math.toDegrees(Math.atan2(VanillaChestLootTableGenerator.z, VanillaChestLootTableGenerator.x)) - 90.0f;
            PathProbeResult pathProbeResult = this.simulateFlightPath(class_19372, class_7462.getPos(), class_7462.getVelocity(), this.currentYaw, this.currentPitch, f + 90.0f, 0.0f, this.navigationTuning.EMERGENCY_YAW_STEP, this.navigationTuning.EMERGENCY_PITCH_STEP, 0, this.navigationTuning.PATH_SIMULATION_STEPS);
            PathProbeResult pathProbeResult2 = this.simulateFlightPath(class_19372, class_7462.getPos(), class_7462.getVelocity(), this.currentYaw, this.currentPitch, f - 90.0f, 0.0f, this.navigationTuning.EMERGENCY_YAW_STEP, this.navigationTuning.EMERGENCY_PITCH_STEP, 0, this.navigationTuning.PATH_SIMULATION_STEPS);
            this.turnLeft = pathProbeResult.stepsCompleted >= pathProbeResult2.stepsCompleted;
        }
        this.previousTurnLeft = this.turnLeft;
        this.turningAroundObstacle = true;
        Objects.requireNonNull(this.navigationTuning);
        this.turnaroundTicksRemaining = 30;
        ++this.navigationTick;
        NotificationBridge.showMessage("\u041a\u0440\u0443\u0436\u0443 \u0432 \u0442\u0443\u043f\u0438\u043a\u0435 \u2014 \u043b\u0435\u0447\u0443 \u043f\u0435\u0440\u043f\u0435\u043d\u0434\u0438\u043a\u0443\u043b\u044f\u0440\u043d\u043e \u0446\u0435\u043b\u0438");
    }

    private Vec3d adjustFlightTarget(World class_19372, ClientPlayerEntity class_7462, Vec3d VanillaChestLootTableGenerator) {
        if (this.landingRecoveryActive) {
            return VanillaChestLootTableGenerator;
        }
        double d = class_7462.getX();
        double d2 = class_7462.getZ();
        double d3 = VanillaChestLootTableGenerator.x - d;
        double d4 = VanillaChestLootTableGenerator.z - d2;
        double d5 = Math.hypot(d3, d4);
        if (this.altitudeRecoveryActive) {
            if (d5 < 1.0) {
                return new Vec3d(VanillaChestLootTableGenerator.x, this.recoveryAltitude, VanillaChestLootTableGenerator.z);
            }
            double d6 = d3 / d5;
            double d7 = d4 / d5;
            return new Vec3d(d + d6 * 16.0, this.recoveryAltitude, d2 + d7 * 16.0);
        }
        boolean bl = class_19372.getDimension().hasSkyLight();
        if (bl) {
            if (d5 > 32.0) {
                double d8 = MathHelper.clamp((double)VanillaChestLootTableGenerator.y, (double)this.navigationTuning.MIN_FLAT_WORLD_HEIGHT, (double)this.navigationTuning.MAX_FLAT_WORLD_HEIGHT);
                return new Vec3d(VanillaChestLootTableGenerator.x, d8, VanillaChestLootTableGenerator.z);
            }
            return VanillaChestLootTableGenerator;
        }
        if (d5 > this.navigationTuning.MAX_OBSTACLE_DISTANCE) {
            double d9 = this.estimateTerrainHeight(class_19372, class_7462);
            Objects.requireNonNull(this.navigationTuning);
            double d10 = d9 + 14.0;
            d10 = MathHelper.clamp((double)d10, (double)this.navigationTuning.MIN_WORLD_HEIGHT, (double)this.navigationTuning.MAX_WORLD_HEIGHT);
            if (VanillaChestLootTableGenerator.y < d10) {
                return new Vec3d(VanillaChestLootTableGenerator.x, d10, VanillaChestLootTableGenerator.z);
            }
        }
        return VanillaChestLootTableGenerator;
    }

    private double estimateTerrainHeight(World class_19372, ClientPlayerEntity class_7462) {
        Vec3d VanillaChestLootTableGenerator = this.getDestinationPosition().subtract(class_7462.getPos());
        double d = Math.hypot(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.z);
        double d2 = d > 1.0 ? VanillaChestLootTableGenerator.x / d : 0.0;
        double d3 = d > 1.0 ? VanillaChestLootTableGenerator.z / d : 0.0;
        double d4 = class_19372.getBottomY();
        for (int n : this.navigationTuning.ALTITUDE_CORRECTION_THRESHOLDS) {
            int n2;
            int n3;
            int n4 = MathHelper.floor((double)(class_7462.getX() + d2 * (double)n));
            if (!class_19372.isPosLoaded(n4, n3 = MathHelper.floor((double)(class_7462.getZ() + d3 * (double)n))) || !((double)(n2 = class_19372.getTopY(Heightmap.Type.MOTION_BLOCKING, n4, n3)) > d4)) continue;
            d4 = n2;
        }
        return d4;
    }

    private RotationResult applyObstacleAvoidance(World class_19372, ClientPlayerEntity class_7462, RotationResult rotationResult) {
        double d;
        Vec3d VanillaChestLootTableGenerator = class_7462.getVelocity();
        double d2 = VanillaChestLootTableGenerator.length();
        if (d2 < 0.4) {
            return rotationResult;
        }
        Vec3d WallPlayerSkullBlock = this.getDestinationPosition();
        double d3 = Math.hypot(WallPlayerSkullBlock.x - class_7462.getX(), WallPlayerSkullBlock.z - class_7462.getZ());
        if (d3 < 24.0) {
            return rotationResult;
        }
        Vec3d VanillaEntityLootTableGenerator = class_7462.getEyePos();
        BlockHitResult class_39652 = class_19372.raycast(new RaycastContext(VanillaEntityLootTableGenerator, VanillaEntityLootTableGenerator.add(VanillaChestLootTableGenerator.multiply((d = Math.min(48.0, d2 * 28.0 + 4.0)) / d2)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)class_7462));
        if (class_39652.getType() != HitResult.Type.BLOCK) {
            return rotationResult;
        }
        if (class_39652.getPos().distanceTo(WallPlayerSkullBlock) < 12.0) {
            return rotationResult;
        }
        double d4 = class_39652.getPos().distanceTo(VanillaEntityLootTableGenerator) / Math.max(0.1, d2);
        Objects.requireNonNull(this.navigationTuning);
        if (d4 < 14.0) {
            return new RotationResult(rotationResult.getYaw(), Math.min(rotationResult.getPitch(), -35.0f), (int)d4, true);
        }
        return rotationResult;
    }

    private Vec3d getActiveNavigationTarget() {
        return this.landingRecoveryActive && this.recoveryTarget != null ? this.recoveryTarget : this.getDestinationPosition();
    }

    private void beginLandingRecovery(String string) {
        if (this.landingRecoveryActive || this.targetReached) {
            return;
        }
        NotificationBridge.showPersistentMessage(string + " \u2014 \u0437\u0430\u0445\u043e\u0436\u0443 \u043d\u0430 \u043f\u043e\u0441\u0430\u0434\u043a\u0443");
        this.landingRecoveryActive = true;
        this.landingRecoveryTicks = 0;
        this.recoveryTarget = null;
        this.recoveryTargetTick = Long.MIN_VALUE;
        this.turningAroundObstacle = false;
        this.altitudeRecoveryActive = false;
        this.flightPathValid = false;
        this.progressStallTicks = 0;
    }

    private boolean updateLandingRecovery(MinecraftClient client, ClientPlayerEntity class_7462, World class_19372) {
        float f;
        block12: {
            block11: {
                ++this.landingRecoveryTicks;
                if (class_7462.isOnGround()) {
                    NotificationBridge.showMessage("\u0421\u0435\u043b \u043d\u0430 \u0437\u0435\u043c\u043b\u044e");
                    this.targetReached = true;
                    this.resetNavigation();
                    return true;
                }
                if (this.landingRecoveryTicks > this.navigationTuning.MAX_RECOVERY_TICKS) {
                    NotificationBridge.showPersistentMessage("\u041f\u043e\u0441\u0430\u0434\u043a\u0430 \u043d\u0435 \u0443\u0434\u0430\u043b\u0430\u0441\u044c \u0437\u0430 \u043b\u0438\u043c\u0438\u0442 \u2014 \u043e\u0442\u043c\u0435\u043d\u044f\u044e");
                    this.errorMessage = "\u043f\u043e\u0441\u0430\u0434\u043a\u0430 \u043d\u0435 \u0443\u0434\u0430\u043b\u0430\u0441\u044c";
                    this.stopNavigation();
                    return true;
                }
                if (this.recoveryTarget == null) break block11;
                long l = (long)this.navigationTick - this.recoveryTargetTick;
                Objects.requireNonNull(this.navigationTuning);
                if (l <= 20L) break block12;
            }
            Vec3d landingPosition = this.findSafeLandingPosition(class_19372, class_7462);
            if (landingPosition != null) {
                this.recoveryTarget = landingPosition;
            }
            this.recoveryTargetTick = this.navigationTick;
        }
        RotationState rotationState = ClientServiceRegistry.getInstance().getRotationState();
        rotationState.enableMovementOverride();
        rotationState.setBackwardPressed(false);
        rotationState.setStrafeRightPressed(false);
        rotationState.setStrafeLeftPressed(false);
        rotationState.setSneakPressed(false);
        rotationState.setJumpPressed(false);
        if (!class_7462.isGliding()) {
            rotationState.setForwardPressed(false);
            rotationState.setSprintPressed(false);
            return false;
        }
        if (this.recoveryTarget == null) {
            rotationState.setForwardPressed(false);
            rotationState.setSprintPressed(false);
            float f2 = -this.navigationTuning.MAX_PITCH_ANGLE;
            Objects.requireNonNull(this.navigationTuning);
            this.currentPitch = RotationEngine.stepAngleTowards(this.currentPitch, f2, 11.0f);
            Rotation rotation = new Rotation(this.currentYaw, this.currentPitch);
            Objects.requireNonNull(this.navigationTuning);
            float f3 = this.navigationTuning.ROTATION_PITCH_LIMIT;
            Objects.requireNonNull(this.navigationTuning);
            RotationRequestHelper.requestDirectRotation(rotation, 45.0f, f3, 45.0f);
            this.trajectoryPoints = List.of();
            return false;
        }
        rotationState.setForwardPressed(true);
        rotationState.setSprintPressed(true);
        Vec3d VanillaChestLootTableGenerator = this.recoveryTarget.subtract(class_7462.getEyePos());
        double d = Math.hypot(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.z);
        float f4 = (float)Math.toDegrees(Math.atan2(VanillaChestLootTableGenerator.z, VanillaChestLootTableGenerator.x)) - 90.0f;
        float f5 = (float)(-Math.toDegrees(Math.atan2(VanillaChestLootTableGenerator.y, Math.max(0.001, d))));
        double d2 = class_7462.getY() - this.recoveryTarget.y;
        if (d2 < this.navigationTuning.MIN_CLEARANCE_DISTANCE) {
            Objects.requireNonNull(this.navigationTuning);
            f = 6.0f;
        } else {
            f = this.navigationTuning.RECOVERY_YAW_ANGLE;
        }
        float f6 = f;
        f5 = MathHelper.clamp((float)f5, (float)(-this.navigationTuning.MAX_PITCH_ANGLE), (float)f6);
        if (d2 < this.navigationTuning.MIN_CLEARANCE_DISTANCE && d < 2.5) {
            rotationState.setForwardPressed(false);
            rotationState.setSprintPressed(false);
            f5 = Math.min(f5, -3.0f);
        }
        Objects.requireNonNull(this.navigationTuning);
        this.currentYaw = RotationEngine.stepAngleTowards(this.currentYaw, f4, 11.0f);
        Objects.requireNonNull(this.navigationTuning);
        this.currentPitch = RotationEngine.stepAngleTowards(this.currentPitch, f5, 6.0f);
        this.currentPitch = MathHelper.clamp((float)this.currentPitch, (float)(-this.navigationTuning.MAX_PITCH_ANGLE), (float)this.navigationTuning.MAX_PITCH_ANGLE);
        Rotation rotation = new Rotation(this.currentYaw, this.currentPitch);
        Objects.requireNonNull(this.navigationTuning);
        float f7 = this.navigationTuning.ROTATION_PITCH_LIMIT;
        Objects.requireNonNull(this.navigationTuning);
        RotationRequestHelper.requestDirectRotation(rotation, 45.0f, f7, 45.0f);
        this.trajectoryPoints = List.of(class_7462.getPos(), this.recoveryTarget);
        return false;
    }

    @Nullable
    private Vec3d findSafeLandingPosition(World class_19372, ClientPlayerEntity class_7462) {
        BlockPos.Mutable class_23392 = new BlockPos.Mutable();
        int n = MathHelper.floor((double)class_7462.getX());
        int n2 = MathHelper.floor((double)class_7462.getZ());
        int n3 = Math.min(class_19372.getTopYInclusive() - 1, MathHelper.floor((double)class_7462.getY()));
        Vec3d VanillaChestLootTableGenerator = null;
        double d = Double.NEGATIVE_INFINITY;
        Objects.requireNonNull(this.navigationTuning);
        int n4 = 8;
        for (int i = -n4; i <= n4; ++i) {
            for (int j = -n4; j <= n4; ++j) {
                int n5 = n + i;
                int n6 = n2 + j;
                boolean bl = false;
                for (int k = n3; k > class_19372.getBottomY() + 2; --k) {
                    double d2;
                    double d3;
                    class_23392.set(n5, k, n6);
                    if (!class_19372.isPosLoaded((BlockPos)class_23392)) {
                        bl = true;
                        break;
                    }
                    BlockState class_26802 = class_19372.getBlockState((BlockPos)class_23392);
                    FluidState class_36102 = class_26802.getFluidState();
                    if (class_36102.isIn(FluidTags.LAVA) || RotationEngine.isHazardousBlock(class_26802)) {
                        bl = true;
                        break;
                    }
                    if (class_26802.isAir() || class_26802.isReplaceable() || class_26802.getCollisionShape((BlockView)class_19372, (BlockPos)class_23392).isEmpty()) continue;
                    BlockPos adminsky = new BlockPos(n5, k + 1, n6);
                    BlockPos adminsky2 = new BlockPos(n5, k + 2, n6);
                    if (!class_19372.getBlockState(adminsky).getCollisionShape((BlockView)class_19372, adminsky).isEmpty() || !class_19372.getBlockState(adminsky2).getCollisionShape((BlockView)class_19372, adminsky2).isEmpty()) break;
                    double d4 = (double)n5 + 0.5;
                    double d5 = (double)k + 1.1;
                    double d6 = (double)n6 + 0.5;
                    double d7 = Math.hypot(d4 - class_7462.getX(), d6 - class_7462.getZ());
                    double d8 = class_7462.getY() - d5;
                    if (d8 < 2.0 || !((d3 = -Math.abs((d2 = Math.toDegrees(Math.atan2(d8, Math.max(0.5, d7)))) - 20.0) * 2.0 - d7 * 0.3 - d8 * 0.1) > d)) break;
                    d = d3;
                    VanillaChestLootTableGenerator = new Vec3d(d4, d5, d6);
                    break;
                }
                if (!bl) continue;
            }
        }
        return VanillaChestLootTableGenerator;
    }

    private PathProbeResult simulateFlightPath(World class_19372, Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, float f, float f2, float f3, float f4, float f5, float f6, int n, int n2) {
        Vec3d VanillaEntityLootTableGenerator = VanillaChestLootTableGenerator;
        Vec3d PlayerSkullBlock = WallPlayerSkullBlock;
        float f7 = f;
        float f8 = f2;
        int n3 = n;
        ArrayList<Vec3d> arrayList = new ArrayList<Vec3d>(n2);
        for (int i = 0; i < n2; ++i) {
            double d;
            f7 = RotationEngine.stepAngleTowards(f7, f3, f5);
            f8 = RotationEngine.stepAngleTowards(f8, f4, f6);
            Vec3d RedstoneBlock = RotationEngine.directionFromRotation(f7, f8);
            double d2 = Math.toRadians(f8);
            double d3 = Math.hypot(RedstoneBlock.x, RedstoneBlock.z);
            double d4 = Math.hypot(PlayerSkullBlock.x, PlayerSkullBlock.z);
            double d5 = RedstoneBlock.length();
            double d6 = Math.cos(d2);
            d6 = d6 * d6 * Math.min(1.0, d5 / 0.4);
            PlayerSkullBlock = PlayerSkullBlock.add(0.0, 0.08 * (-1.0 + d6 * 0.75), 0.0);
            if (PlayerSkullBlock.y < 0.0 && d3 > 0.0) {
                d = PlayerSkullBlock.y * -0.1 * d6;
                PlayerSkullBlock = PlayerSkullBlock.add(RedstoneBlock.x * d / d3, d, RedstoneBlock.z * d / d3);
            }
            if (d2 < 0.0 && d3 > 0.0) {
                d = d4 * -Math.sin(d2) * 0.04;
                PlayerSkullBlock = PlayerSkullBlock.add(-RedstoneBlock.x * d / d3, d * 3.2, -RedstoneBlock.z * d / d3);
            }
            if (d3 > 0.0) {
                PlayerSkullBlock = PlayerSkullBlock.add((RedstoneBlock.x / d3 * d4 - PlayerSkullBlock.x) * 0.1, 0.0, (RedstoneBlock.z / d3 * d4 - PlayerSkullBlock.z) * 0.1);
            }
            if (n3 > 0) {
                d = 1.5;
                PlayerSkullBlock = PlayerSkullBlock.add(RedstoneBlock.x * 0.1 + (RedstoneBlock.x * d - PlayerSkullBlock.x) * 0.5, RedstoneBlock.y * 0.1 + (RedstoneBlock.y * d - PlayerSkullBlock.y) * 0.5, RedstoneBlock.z * 0.1 + (RedstoneBlock.z * d - PlayerSkullBlock.z) * 0.5);
                --n3;
            }
            PlayerSkullBlock = new Vec3d(PlayerSkullBlock.x * 0.99, PlayerSkullBlock.y * 0.98, PlayerSkullBlock.z * 0.99);
            Vec3d VanillaFishingLootTableGenerator = VanillaEntityLootTableGenerator;
            VanillaEntityLootTableGenerator = VanillaEntityLootTableGenerator.add(PlayerSkullBlock);
            arrayList.add(VanillaEntityLootTableGenerator);
            double d7 = PlayerSkullBlock.length();
            int n4 = Math.max(1, (int)Math.ceil(d7 / 0.6));
            for (int j = 1; j <= n4; ++j) {
                CollisionResult collisionResult;
                Vec3d LootTableProvider = VanillaFishingLootTableGenerator.add(PlayerSkullBlock.multiply((double)j / (double)n4));
                CollisionResult collisionResult2 = collisionResult = j == n4 ? this.checkFlightCollision(class_19372, LootTableProvider, false) : this.checkFlightCollision(class_19372, LootTableProvider, true);
                if (collisionResult == CollisionResult.COLLISION) {
                    return new PathProbeResult(i, true, VanillaEntityLootTableGenerator, arrayList);
                }
                if (collisionResult != CollisionResult.UNKNOWN) continue;
                return new PathProbeResult(i, true, VanillaEntityLootTableGenerator, arrayList);
            }
        }
        return new PathProbeResult(n2, false, VanillaEntityLootTableGenerator, arrayList);
    }

    private CollisionResult checkFlightCollision(World class_19372, Vec3d VanillaChestLootTableGenerator, boolean bl) {
        double[][] dArray;
        BlockPos.Mutable class_23392 = new BlockPos.Mutable();
        boolean bl2 = false;
        for (double[] dArray2 : dArray = bl ? this.navigationTuning.TIGHT_COLLISION_OFFSETS : this.navigationTuning.NORMAL_COLLISION_OFFSETS) {
            int n = MathHelper.floor((double)(VanillaChestLootTableGenerator.x + dArray2[0]));
            int n2 = MathHelper.floor((double)(VanillaChestLootTableGenerator.y + dArray2[1]));
            int n3 = MathHelper.floor((double)(VanillaChestLootTableGenerator.z + dArray2[2]));
            if (n2 <= class_19372.getBottomY() + 2 || n2 >= class_19372.getTopYInclusive() - 2) {
                return CollisionResult.COLLISION;
            }
            class_23392.set(n, n2, n3);
            if (!class_19372.isPosLoaded((BlockPos)class_23392)) {
                bl2 = true;
                continue;
            }
            BlockState class_26802 = class_19372.getBlockState((BlockPos)class_23392);
            if (RotationEngine.isHazardousBlock(class_26802)) {
                return CollisionResult.COLLISION;
            }
            FluidState class_36102 = class_26802.getFluidState();
            if (class_36102.isIn(FluidTags.LAVA)) {
                return CollisionResult.COLLISION;
            }
            if (class_26802.isAir() || class_26802.isReplaceable() || class_26802.getCollisionShape((BlockView)class_19372, (BlockPos)class_23392).isEmpty()) continue;
            return CollisionResult.COLLISION;
        }
        return bl2 ? CollisionResult.UNKNOWN : CollisionResult.CLEAR;
    }

    private static boolean isHazardousBlock(BlockState class_26802) {
        return class_26802.isOf(Blocks.LAVA) || class_26802.isOf(Blocks.FIRE) || class_26802.isOf(Blocks.SOUL_FIRE) || class_26802.isOf(Blocks.MAGMA_BLOCK) || class_26802.isOf(Blocks.CAMPFIRE) || class_26802.isOf(Blocks.SOUL_CAMPFIRE);
    }

    private boolean hasElytraEquipped(ClientPlayerEntity class_7462) {
        ItemStack class_17992 = class_7462.getEquippedStack(EquipmentSlot.CHEST);
        return !class_17992.isEmpty() && class_17992.isOf(Items.ELYTRA);
    }

    private boolean hasUsableFirework(ClientPlayerEntity class_7462) {
        ItemStack class_17992 = class_7462.getEquippedStack(EquipmentSlot.CHEST);
        if (class_17992.isEmpty() || !class_17992.isOf(Items.ELYTRA)) {
            return false;
        }
        if (!class_17992.isDamageable()) {
            return false;
        }
        return class_17992.getMaxDamage() - class_17992.getDamage() <= this.navigationTuning.MAX_FIREWORK_DAMAGE;
    }

    private void updateDistanceProgress(double d) {
        if (this.turningAroundObstacle || this.altitudeRecoveryActive || this.landingRecoveryActive) {
            return;
        }
        if (this.flightPathValid) {
            return;
        }
        if (d + this.navigationTuning.TARGET_REACH_MARGIN < this.targetDistanceSquared) {
            this.targetDistanceSquared = d;
            this.progressStallTicks = 0;
            this.initializationTicks = 0;
            this.navigationTick = 0;
            this.altitudeRecoveryAttempts = 0;
        } else {
            ++this.progressStallTicks;
        }
    }

    private double getHorizontalSpeed(ClientPlayerEntity class_7462) {
        return Math.hypot(class_7462.getVelocity().x, class_7462.getVelocity().z);
    }

    private float getTargetYaw(ClientPlayerEntity class_7462) {
        Vec3d VanillaChestLootTableGenerator = this.getDestinationPosition().subtract(class_7462.getEyePos());
        return (float)Math.toDegrees(Math.atan2(VanillaChestLootTableGenerator.z, VanillaChestLootTableGenerator.x)) - 90.0f;
    }

    private double getDistanceSquaredToTarget(ClientPlayerEntity class_7462) {
        double d = class_7462.getX() - ((double)this.targetBlockX + 0.5);
        double d2 = class_7462.getZ() - ((double)this.targetBlockZ + 0.5);
        if (!this.targetHasFixedHeight) {
            return d * d + d2 * d2;
        }
        double d3 = class_7462.getY() - ((double)this.targetBlockY + 0.5);
        return d * d + d3 * d3 + d2 * d2;
    }

    private boolean isWithinApproachRange(ClientPlayerEntity class_7462) {
        double d;
        double d2;
        double d3 = class_7462.getX() - ((double)this.targetBlockX + 0.5);
        double d4 = Math.hypot(d3, d2 = class_7462.getZ() - ((double)this.targetBlockZ + 0.5));
        return d4 <= (d = MathHelper.clamp((double)(this.getHorizontalSpeed(class_7462) * 5.0 + 3.0), (double)this.navigationTuning.MIN_APPROACH_DISTANCE, (double)this.navigationTuning.MAX_APPROACH_DISTANCE));
    }

    private double getTargetHeight() {
        return this.targetHasFixedHeight ? (double)this.targetBlockY + 0.5 : this.initialHeight;
    }

    private static Vec3d directionFromRotation(float f, float f2) {
        float f3 = (float)Math.toRadians(f);
        float f4 = (float)Math.toRadians(f2);
        float f5 = MathHelper.cos((float)f4);
        return new Vec3d((double)(-MathHelper.sin((float)f3) * f5), (double)(-MathHelper.sin((float)f4)), (double)(MathHelper.cos((float)f3) * f5)).normalize();
    }

    private static float stepAngleTowards(float f, float f2, float f3) {
        float f4 = AimRotationMath.getWrappedAngleDifference(f, f2);
        if (Math.abs(f4) <= f3) {
            return f2;
        }
        return f + Math.signum(f4) * f3;
    }

    @Override
    public void stopNavigation() {
        this.resetNavigation();
    }

    @Override
    public void pauseNavigation() {
        this.paused = true;
        ClientServiceRegistry.getInstance().getRotationState().disableMovementOverride();
    }

    @Override
    public void resumeNavigation() {
        this.paused = false;
    }

    @Override
    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public boolean isTargetReached() {
        return this.targetReached;
    }

    @Override
    @Nullable
    public String getErrorMessage() {
        return this.errorMessage;
    }

    private void resetNavigation() {
        this.trajectoryPoints = List.of();
        this.candidatePath = List.of();
        this.candidatePathFuture = null;
        ClientServiceRegistry.getInstance().getRotationState().disableMovementOverride();
    }

    static final class NavigationTuning {
        final double MIN_APPROACH_DISTANCE = 8.0;
        final double MAX_APPROACH_DISTANCE = 12.0;
        final double TARGET_REACH_MARGIN = 9.0;
        final int MAX_NAVIGATION_TICKS = 500;
        final int TAKEOFF_WINDOW_TICKS = 600;
        final int MAX_ROUTE_RETRIES = 2;
        final int JUMP_HOLD_TICKS = 4;
        final float TAKEOFF_PITCH = -6.0f;
        final int PROGRESS_CHECK_INTERVAL = 32;
        final int MAX_RECOVERY_ATTEMPTS = 12;
        final int MAX_TURN_ATTEMPTS = 30;
        final int MAX_ROTATION_SAMPLES = 38;
        final float MIN_FLIGHT_SPEED = 1.05f;
        final float MIN_APPROACH_SPEED = 6.0f;
        final float MAX_APPROACH_SPEED = 12.0f;
        final int MAX_FIREWORK_DAMAGE = 10;
        final float MAX_PITCH_ANGLE = 50.0f;
        final float TAKEOFF_PITCH_LIMIT = 20.0f;
        final float[] PITCH_SAMPLE_OFFSETS = new float[]{-65.0f, -38.0f, -18.0f, 0.0f, 18.0f, 38.0f, 65.0f};
        final float[] YAW_SAMPLE_OFFSETS = new float[]{-38.0f, -25.0f, -14.0f, -5.0f, 6.0f, 16.0f};
        final float[] EMERGENCY_YAW_OFFSETS = new float[]{0.0f, -35.0f, 35.0f, -70.0f, 70.0f, -110.0f, 110.0f, -150.0f, 150.0f, 180.0f};
        final float[] EMERGENCY_PITCH_OFFSETS = new float[]{-50.0f, -35.0f, -20.0f, -8.0f, 6.0f};
        final float MIN_SAFE_ALTITUDE = 11.0f;
        final float MIN_GLIDE_ALTITUDE = 6.0f;
        final float BRAKE_PITCH_ANGLE = 18.0f;
        final float BRAKE_ALTITUDE_MARGIN = 11.0f;
        final float MAX_YAW_ROTATION_STEP = 45.0f;
        final float ROTATION_PITCH_LIMIT = 35.0f;
        final float ROTATION_YAW_TOLERANCE = 45.0f;
        final int PATH_SIMULATION_STEPS = 50;
        final float NORMAL_YAW_STEP = 7.5f;
        final float NORMAL_PITCH_STEP = 4.5f;
        final float EMERGENCY_YAW_STEP = 14.0f;
        final float EMERGENCY_PITCH_STEP = 8.5f;
        final double YAW_ERROR_WEIGHT = 0.22;
        final double PITCH_ERROR_WEIGHT = 0.16;
        final double CURRENT_ROTATION_WEIGHT = 0.1;
        final double DISTANCE_WEIGHT = 0.5;
        final int MIN_WORLD_HEIGHT = 16;
        final int MAX_WORLD_HEIGHT = 118;
        final int MIN_FLAT_WORLD_HEIGHT = 45;
        final int MAX_FLAT_WORLD_HEIGHT = 100;
        final int HIGH_ALTITUDE_THRESHOLD = 112;
        final double MAX_OBSTACLE_DISTANCE = 40.0;
        final double EMERGENCY_DISTANCE_THRESHOLD = 14.0;
        final int[] ALTITUDE_CORRECTION_THRESHOLDS = new int[]{0, 8, 20, 36, 56};
        final double RECOVERY_DISTANCE_THRESHOLD = 14.0;
        final int CANDIDATE_REFRESH_TICKS = 40;
        final double CANDIDATE_MAX_DISTANCE = 28.0;
        final double CANDIDATE_MIN_DISTANCE = 6.0;
        final float RECOVERY_PITCH = 34.0f;
        final int MIN_RECOVERY_STEPS = 2;
        final int RECOVERY_PITCH_TICKS = 70;
        final int INITIALIZATION_TICKS = 25;
        final int MAX_INITIALIZATION_TICKS = 30;
        final int MIN_STEERING_TICKS = 3;
        final int MAX_STEERING_TICKS = 8;
        final int STEERING_COOLDOWN_TICKS = 12;
        final int MAX_RECOVERY_TICKS = 300;
        final int MIN_ALTITUDE_STEP = 20;
        final int MAX_ALTITUDE_STEP = 8;
        final float RECOVERY_YAW_ANGLE = 22.0f;
        final float RECOVERY_PITCH_ANGLE = 6.0f;
        final double MIN_CLEARANCE_DISTANCE = 5.0;
        final double[][] NORMAL_COLLISION_OFFSETS = new double[][]{{0.0, 0.1, 0.0}, {0.0, 0.9, 0.0}, {0.0, 1.7, 0.0}, {0.0, 2.3, 0.0}, {0.0, -0.5, 0.0}, {0.8, 0.1, 0.0}, {0.8, 0.9, 0.0}, {0.8, 1.7, 0.0}, {-0.8, 0.1, 0.0}, {-0.8, 0.9, 0.0}, {-0.8, 1.7, 0.0}, {0.0, 0.1, 0.8}, {0.0, 0.9, 0.8}, {0.0, 1.7, 0.8}, {0.0, 0.1, -0.8}, {0.0, 0.9, -0.8}, {0.0, 1.7, -0.8}, {0.6, 0.1, 0.6}, {-0.6, 0.1, 0.6}, {0.6, 0.1, -0.6}, {-0.6, 0.1, -0.6}, {0.6, 0.9, 0.6}, {-0.6, 0.9, 0.6}, {0.6, 0.9, -0.6}, {-0.6, 0.9, -0.6}, {0.6, 1.7, 0.6}, {-0.6, 1.7, 0.6}, {0.6, 1.7, -0.6}, {-0.6, 1.7, -0.6}};
        final double[][] TIGHT_COLLISION_OFFSETS = new double[][]{{0.0, 0.1, 0.0}, {0.0, 0.9, 0.0}, {0.0, 1.7, 0.0}, {0.7, 0.9, 0.0}, {-0.7, 0.9, 0.0}, {0.0, 0.9, 0.7}, {0.0, 0.9, -0.7}, {0.7, 0.1, 0.0}, {-0.7, 0.1, 0.0}, {0.0, 0.1, 0.7}, {0.0, 0.1, -0.7}, {0.7, 1.7, 0.0}, {-0.7, 1.7, 0.0}, {0.0, 1.7, 0.7}, {0.0, 1.7, -0.7}, {0.5, 0.9, 0.5}, {-0.5, 0.9, 0.5}, {0.5, 0.9, -0.5}, {-0.5, 0.9, -0.5}};

        NavigationTuning() {
        }
    }

    static enum NavigationState {
        GROUNDED,
        TAKEOFF,
        GLIDING;
}

    static final class RotationResult {
        private final float yaw;
        private final float pitch;
        private final int flightTicks;
        private final boolean valid;

        RotationResult(float f, float f2, int n, boolean bl) {
            this.yaw = f;
            this.pitch = f2;
            this.flightTicks = n;
            this.valid = bl;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "yaw", "pitch", "flightTicks", "valid");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "yaw", "pitch", "flightTicks", "valid");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "yaw", "pitch", "flightTicks", "valid");
        }

        public float getYaw() {
            return this.yaw;
        }

        public float getPitch() {
            return this.pitch;
        }

        public int getFlightTicks() {
            return this.flightTicks;
        }

        public boolean isValid() {
            return this.valid;
        }
    }

    static final class PathProbeResult {
        final int stepsCompleted;
        final boolean collisionDetected;
        final Vec3d finalPosition;
        final List<Vec3d> trajectoryPoints;

        PathProbeResult(int n, boolean bl, Vec3d VanillaChestLootTableGenerator, List<Vec3d> list) {
            this.stepsCompleted = n;
            this.collisionDetected = bl;
            this.finalPosition = VanillaChestLootTableGenerator;
            this.trajectoryPoints = list;
        }
    }

    static enum CollisionResult {
        CLEAR,
        COLLISION,
        UNKNOWN;
}
}
