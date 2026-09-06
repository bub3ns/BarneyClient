/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.ActionResult
 *  net.minecraft.Entity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.BlockItem
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.BlockView
 *  net.minecraft.BlockWithEntity
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.FallingBlock
 *  net.minecraft.Direction
 *  net.minecraft.Direction$Axis
 *  net.minecraft.Position
 *  net.minecraft.Box
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.BlockState
 *  net.minecraft.PlayerMoveC2SPacket$Full
 *  net.minecraft.MathHelper
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 */
package moscow.rockstar.modules.player.placement;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.targeting.TargetActionQueue;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.player.interaction.InputListener;
import moscow.rockstar.modules.player.inventory.HandSelection;
import moscow.rockstar.modules.player.movement.MotionSnapshot;
import moscow.rockstar.modules.player.placement.BlockFaceCandidate;
import moscow.rockstar.modules.player.placement.BlockPositionData;
import moscow.rockstar.modules.player.placement.PlacementContext;
import moscow.rockstar.modules.player.placement.PlacementFaceMode;
import moscow.rockstar.modules.player.placement.PlacementSettings;
import moscow.rockstar.modules.player.placement.PlacementTarget;
import moscow.rockstar.modules.player.placement.PlacementTickListener;
import moscow.rockstar.modules.player.placement.PlacementUpdateListener;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.RangeSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.BlockView;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.FallingBlock;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.player.InputEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Scaffold", description="modules.descriptions.scaffold", category=ModuleCategory.PLAYER)
public class Scaffold
extends Module {
    private static final int PENDING_PLACEMENT_QUEUE_CAPACITY = 4;
    private static final int DEFAULT_EXPAND_LENGTH = 4;
    private static final double MIN_PLACE_DISTANCE = 0.001;
    private static final double MAX_PLACE_DISTANCE = 0.02;
    private RangeSetting delay;
    private NumberSetting minDist;
    private ModeSetting technique;
    ModeSetting.Option normal;
    private ModeSetting.Option expand;
    private ModeSetting.Option godBridge;
    ModeSetting.Option breezily;
    private ModeSetting sameY;
    private ModeSetting.Option off;
    private ModeSetting.Option on;
    private ModeSetting.Option falling;
    private ModeSetting.Option hypixel;
    private ModeSetting tower;
    private ModeSetting.Option none;
    private ModeSetting.Option motionMode;
    private ModeSetting.Option pulldown;
    private ModeSetting.Option karhu;
    private ModeSetting.Option vulcan;
    private ModeSetting.Option hypixelMotion;
    private ModeSetting rotationMode;
    private ModeSetting.Option center;
    private ModeSetting.Option random;
    private ModeSetting.Option stabilized;
    private ModeSetting.Option nearestRotation;
    private ModeSetting.Option reverseYaw;
    private ModeSetting.Option diagonalYaw;
    private ModeSetting.Option angleYaw;
    private ModeSetting.Option edgePoint;
    private ModeSetting rotationTiming;
    private ModeSetting.Option tickRotation;
    private ModeSetting.Option onTick;
    private ModeSetting.Option onTickSnap;
    private NumberSetting rotationSpeed;
    private NumberSetting aimTolerance;
    private NumberSetting stableTicks;
    private BooleanSetting considerInventory;
    private ModeSetting moveCorrection;
    private ModeSetting.Option moveCorrectionOff;
    private ModeSetting.Option strict;
    private ModeSetting.Option silent;
    private ModeSetting.Option changeLook;
    private BooleanSetting autoBlock;
    private BooleanSetting always;
    private NumberSetting slotResetDelay;
    private NumberSetting doNotUseBelow;
    private BooleanSetting ledge;
    BooleanSetting eagle;
    private RangeSetting blocks;
    private RangeSetting edgeDistance;
    private BooleanSetting onlyOnGround;
    BooleanSetting down;
    BooleanSetting stabilizeMovement;
    private BooleanSetting ceiling;
    private BooleanSetting headHitter;
    BooleanSetting speedLimiter;
    NumberSetting speed;
    private NumberSetting length;
    private RangeSetting sneakDistance;
    private RangeSetting sneakTime;
    private NumberSetting forceSneakBelow;
    private NumberSetting motionConfig;
    private NumberSetting triggerHeight;
    private NumberSetting slow;
    private NumberSetting pulldownTrigger;
    private NumberSetting karhuTrigger;
    private BooleanSetting karhuPulldown;
    private BooleanSetting resetSprint;
    private BooleanSetting simulatePlacementAttempts;
    private BooleanSetting failedOnly;
    private final Timer cooldownTimer = new Timer();
    private final ArrayDeque<BlockPos> pendingPlacements = new ArrayDeque(4);
    private final ArrayDeque<Vec3d> queuedPlacements = new ArrayDeque(5);
    private PlacementTarget currentPlacement;
    private HandSelection currentHand;
    private BlockPos targetBlock;
    private BlockPos previousBlock;
    private Direction placementFace;
    MotionSnapshot motionSnapshot;
    PlacementContext placementContext = PlacementContext.NO_INPUT;
    private BlockPositionData blockPositionData;
    private BlockPos placedBlock;
    private float rotationProgress = Float.NaN;
    private int placementAttempts;
    private int failedAttempts;
    private int placementCount;
    int slotIndex;
    private int currentSlot;
    private int previousSlot;
    private int blockCount;
    private float placementOffset;
    private int retryCount;
    private double lastPlacementTime = Double.NaN;
    private boolean placementActive;
    private boolean overlayVisible;
    private boolean privilegedMode;
    private boolean disableLocked;
    private float rotationDelta;
    private long lastPlacementTick;
    private float movementCorrection;
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = new PlacementTickListener(this);
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent1 = new PlacementUpdateListener(this);
    private final EventListener<InputEvent> onInputEvent = new InputListener(this);
    private final EventListener<WorldChangeEvent> onWorldChangeEvent = worldChangeEvent -> this.updatePlacementState();

    public Scaffold() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.delay = new RangeSetting(this, "modules.settings.scaffold.delay").setMinimum(0.0f).setMaximum(40.0f).setStep(1.0f).setFirstValue(0.0f).setSecondValue(0.0f);
        this.minDist = new NumberSetting(this, "modules.settings.scaffold.min_dist").setMinValue(0.0f).setMaxValue(0.25f).setStep(0.01f).setValue(0.0f);
        this.technique = new ModeSetting(this, "modules.settings.scaffold.technique");
        this.normal = new ModeSetting.Option(this.technique, "modules.settings.scaffold.technique.normal").select();
        this.expand = new ModeSetting.Option(this.technique, "modules.settings.scaffold.technique.expand");
        this.godBridge = new ModeSetting.Option(this.technique, "modules.settings.scaffold.technique.god_bridge");
        this.breezily = new ModeSetting.Option(this.technique, "modules.settings.scaffold.technique.breezily");
        this.sameY = new ModeSetting(this, "modules.settings.scaffold.same_y");
        this.off = new ModeSetting.Option(this.sameY, "modules.settings.scaffold.same_y.off").select();
        this.on = new ModeSetting.Option(this.sameY, "modules.settings.scaffold.same_y.on");
        this.falling = new ModeSetting.Option(this.sameY, "modules.settings.scaffold.same_y.falling");
        this.hypixel = new ModeSetting.Option(this.sameY, "modules.settings.scaffold.same_y.hypixel");
        this.tower = new ModeSetting(this, "modules.settings.scaffold.tower");
        this.none = new ModeSetting.Option(this.tower, "modules.settings.scaffold.tower.none").select();
        this.motionMode = new ModeSetting.Option(this.tower, "modules.settings.scaffold.tower.motion_mode");
        this.pulldown = new ModeSetting.Option(this.tower, "modules.settings.scaffold.tower.pulldown");
        this.karhu = new ModeSetting.Option(this.tower, "modules.settings.scaffold.tower.karhu");
        this.vulcan = new ModeSetting.Option(this.tower, "modules.settings.scaffold.tower.vulcan");
        this.hypixelMotion = new ModeSetting.Option(this.tower, "modules.settings.scaffold.tower.hypixel");
        this.rotationMode = new ModeSetting((SettingOwner)this, "modules.settings.scaffold.rotation_mode", () -> !this.technique.isSelected(this.normal));
        this.center = new ModeSetting.Option(this.rotationMode, "modules.settings.scaffold.rotation_mode.center");
        this.random = new ModeSetting.Option(this.rotationMode, "modules.settings.scaffold.rotation_mode.random");
        this.stabilized = new ModeSetting.Option(this.rotationMode, "modules.settings.scaffold.rotation_mode.stabilized").select();
        this.nearestRotation = new ModeSetting.Option(this.rotationMode, "modules.settings.scaffold.rotation_mode.nearest_rotation");
        this.reverseYaw = new ModeSetting.Option(this.rotationMode, "modules.settings.scaffold.rotation_mode.reverse_yaw");
        this.diagonalYaw = new ModeSetting.Option(this.rotationMode, "modules.settings.scaffold.rotation_mode.diagonal_yaw");
        this.angleYaw = new ModeSetting.Option(this.rotationMode, "modules.settings.scaffold.rotation_mode.angle_yaw");
        this.edgePoint = new ModeSetting.Option(this.rotationMode, "modules.settings.scaffold.rotation_mode.edge_point");
        this.rotationTiming = new ModeSetting(this, "modules.settings.scaffold.rotation_timing");
        this.tickRotation = new ModeSetting.Option(this.rotationTiming, "modules.settings.scaffold.rotation_timing.normal").select();
        this.onTick = new ModeSetting.Option(this.rotationTiming, "modules.settings.scaffold.rotation_timing.on_tick");
        this.onTickSnap = new ModeSetting.Option(this.rotationTiming, "modules.settings.scaffold.rotation_timing.on_tick_snap");
        this.rotationSpeed = new NumberSetting(this, "modules.settings.scaffold.rotation_speed").setStep(5.0f).setMinValue(30.0f).setMaxValue(180.0f).setValue(180.0f);
        this.aimTolerance = new NumberSetting(this, "modules.settings.scaffold.aim_tolerance").setStep(0.5f).setMinValue(1.0f).setMaxValue(20.0f).setValue(10.0f);
        this.stableTicks = new NumberSetting(this, "modules.settings.scaffold.stable_ticks").setStep(1.0f).setMinValue(0.0f).setMaxValue(5.0f).setValue(0.0f);
        this.considerInventory = new BooleanSetting(this, "modules.settings.scaffold.consider_inventory").setActiveExtra(false);
        this.moveCorrection = new ModeSetting(this, "modules.settings.scaffold.move_correction");
        this.moveCorrectionOff = new ModeSetting.Option(this.moveCorrection, "modules.settings.scaffold.move_correction.off");
        this.strict = new ModeSetting.Option(this.moveCorrection, "modules.settings.scaffold.move_correction.strict");
        this.silent = new ModeSetting.Option(this.moveCorrection, "modules.settings.scaffold.move_correction.silent").select();
        this.changeLook = new ModeSetting.Option(this.moveCorrection, "modules.settings.scaffold.move_correction.change_look");
        this.autoBlock = new BooleanSetting(this, "modules.settings.scaffold.auto_block").enable();
        this.always = new BooleanSetting((SettingOwner)this, "modules.settings.scaffold.auto_block.always", () -> !this.technique.isSelected(this.normal)).setActiveExtra(false);
        this.slotResetDelay = new NumberSetting((SettingOwner)this, "modules.settings.scaffold.auto_block.slot_reset_delay", () -> !this.technique.isSelected(this.normal)).setMinValue(0.0f).setMaxValue(40.0f).setStep(1.0f).setValue(5.0f);
        this.doNotUseBelow = new NumberSetting((SettingOwner)this, "modules.settings.scaffold.auto_block.do_not_use_below", () -> !this.technique.isSelected(this.normal)).setMinValue(0.0f).setMaxValue(64.0f).setStep(1.0f).setValue(1.0f);
        this.ledge = new BooleanSetting(this, "modules.settings.scaffold.ledge").enable();
        this.eagle = new BooleanSetting((SettingOwner)this, "modules.settings.scaffold.eagle", () -> !this.technique.isSelected(this.normal)).setActiveExtra(false);
        this.blocks = new RangeSetting(this, "modules.settings.scaffold.eagle.blocks", () -> !this.technique.isSelected(this.normal)).setMinimum(0.0f).setMaximum(10.0f).setStep(1.0f).setFirstValue(0.0f).setSecondValue(0.0f);
        this.edgeDistance = new RangeSetting(this, "modules.settings.scaffold.eagle.edge_distance", () -> !this.eagle.isEnabled()).setMinimum(0.01f).setMaximum(1.3f).setStep(0.01f).setFirstValue(0.01f).setSecondValue(0.05f);
        this.onlyOnGround = new BooleanSetting((SettingOwner)this, "modules.settings.scaffold.eagle.only_on_ground", () -> !this.eagle.isEnabled()).enable();
        this.down = new BooleanSetting((SettingOwner)this, "modules.settings.scaffold.down", () -> !this.technique.isSelected(this.normal)).setActiveExtra(false);
        this.stabilizeMovement = new BooleanSetting((SettingOwner)this, "modules.settings.scaffold.stabilize_movement", () -> !this.technique.isSelected(this.normal)).enable();
        this.ceiling = new BooleanSetting((SettingOwner)this, "modules.settings.scaffold.ceiling", () -> !this.technique.isSelected(this.normal)).setActiveExtra(false);
        this.headHitter = new BooleanSetting((SettingOwner)this, "modules.settings.scaffold.head_hitter", () -> !this.technique.isSelected(this.normal)).setActiveExtra(false);
        this.speedLimiter = new BooleanSetting(this, "modules.settings.scaffold.speed_limiter").setActiveExtra(false);
        this.speed = new NumberSetting((SettingOwner)this, "modules.settings.scaffold.speed_limiter.speed", () -> !this.speedLimiter.isEnabled()).setMinValue(0.01f).setMaxValue(0.4f).setStep(0.01f).setValue(0.11f);
        this.length = new NumberSetting((SettingOwner)this, "modules.settings.scaffold.expand.length", () -> !this.technique.isSelected(this.expand)).setMinValue(1.0f).setMaxValue(10.0f).setStep(1.0f).setValue(4.0f);
        this.sneakDistance = new RangeSetting(this, "modules.settings.scaffold.breezily.edge_distance", () -> !this.technique.isSelected(this.breezily)).setMinimum(0.25f).setMaximum(0.5f).setStep(0.01f).setFirstValue(0.45f).setSecondValue(0.5f);
        this.sneakTime = new RangeSetting(this, "modules.settings.scaffold.god_bridge.sneak_time", () -> !this.technique.isSelected(this.godBridge)).setMinimum(1.0f).setMaximum(10.0f).setStep(1.0f).setFirstValue(1.0f).setSecondValue(1.0f);
        this.forceSneakBelow = new NumberSetting((SettingOwner)this, "modules.settings.scaffold.god_bridge.force_sneak_below", () -> !this.technique.isSelected(this.godBridge)).setMinValue(0.0f).setMaxValue(10.0f).setStep(1.0f).setValue(3.0f);
        this.motionConfig = new NumberSetting((SettingOwner)this, "modules.settings.scaffold.tower.motion", () -> !this.tower.isSelected(this.motionMode)).setMinValue(0.0f).setMaxValue(1.0f).setStep(0.01f).setValue(0.42f);
        this.triggerHeight = new NumberSetting((SettingOwner)this, "modules.settings.scaffold.tower.trigger_height", () -> !this.tower.isSelected(this.motionMode)).setMinValue(0.76f).setMaxValue(1.0f).setStep(0.01f).setValue(0.78f);
        this.slow = new NumberSetting((SettingOwner)this, "modules.settings.scaffold.tower.slow", () -> !this.tower.isSelected(this.motionMode)).setMinValue(0.0f).setMaxValue(3.0f).setStep(0.05f).setValue(1.0f);
        this.pulldownTrigger = new NumberSetting((SettingOwner)this, "modules.settings.scaffold.tower.pulldown_trigger", () -> !this.tower.isSelected(this.pulldown)).setMinValue(0.0f).setMaxValue(0.2f).setStep(0.01f).setValue(0.1f);
        this.karhuTrigger = new NumberSetting((SettingOwner)this, "modules.settings.scaffold.tower.karhu_trigger", () -> !this.tower.isSelected(this.karhu)).setMinValue(0.0f).setMaxValue(0.2f).setStep(0.01f).setValue(0.06f);
        this.karhuPulldown = new BooleanSetting((SettingOwner)this, "modules.settings.scaffold.tower.karhu_pulldown", () -> !this.tower.isSelected(this.karhu)).enable();
        this.resetSprint = new BooleanSetting(this, "modules.settings.scaffold.reset_sprint").enable();
        this.simulatePlacementAttempts = new BooleanSetting(this, "modules.settings.scaffold.simulate_placement_attempts").setActiveExtra(false);
        this.failedOnly = new BooleanSetting((SettingOwner)this, "modules.settings.scaffold.simulate_placement_attempts.failed_only", () -> !this.simulatePlacementAttempts.isEnabled()).enable();
    }

    @Override
    public void onEnable() {
        if (Scaffold.minecraftClient.player != null) {
            this.placementAttempts = Scaffold.minecraftClient.player.getBlockPos().getY() - 1;
            this.failedAttempts = Scaffold.minecraftClient.player.getBlockPos().getY();
            this.placementCount = 2;
            this.overlayVisible = Scaffold.minecraftClient.player.isOnGround();
        }
        this.updatePlacementState();
        this.updateMovementState();
        this.movementCorrection = this.getRangeProgress(this.sneakDistance);
        this.cooldownTimer.reset();
    }

    @Override
    public void onDisable() {
        this.updatePlacementState();
    }

    void resetScaffold() {
        boolean bl;
        if (Scaffold.minecraftClient.player == null) {
            return;
        }
        boolean bl2 = Scaffold.minecraftClient.player.isOnGround();
        if (bl2) {
            this.placementAttempts = Scaffold.minecraftClient.player.getBlockPos().getY() - 1;
            ++this.placementCount;
            this.placementActive = false;
            this.retryCount = 0;
        } else {
            ++this.retryCount;
        }
        if (Scaffold.minecraftClient.options.jumpKey.isPressed()) {
            this.failedAttempts = Scaffold.minecraftClient.player.getBlockPos().getY();
            this.placementCount = 2;
        }
        boolean bl3 = bl = this.overlayVisible && !bl2 && Scaffold.minecraftClient.player.getVelocity().y > 0.0;
        if (bl) {
            this.lastPlacementTime = Scaffold.minecraftClient.player.getY();
        }
        this.overlayVisible = bl2;
    }

    void resetPlacementState() {
        if (Scaffold.minecraftClient.player == null) {
            return;
        }
        if (this.headHitter.isEnabled() && this.normal.isSelected() && this.isPlacementWorldReady() && this.isPlacementTargetReady()) {
            Scaffold.minecraftClient.player.jump();
        }
    }

    void resetTargetState() {
        if (Scaffold.minecraftClient.player == null || this.tower.isSelected(this.none) || !this.isBlockFeatureReady() || this.getSelectedBlockCount() <= 0 || !this.isPlacementPlayerReady()) {
            this.lastPlacementTime = Double.NaN;
            return;
        }
        Vec3d VanillaChestLootTableGenerator = Scaffold.minecraftClient.player.getVelocity();
        if (this.tower.isSelected(this.motionMode)) {
            if (Double.isNaN(this.lastPlacementTime)) {
                return;
            }
            if (Scaffold.minecraftClient.player.getY() > this.lastPlacementTime + (double)this.triggerHeight.getValue()) {
                Scaffold.minecraftClient.player.setPosition(Scaffold.minecraftClient.player.getX(), Math.floor(Scaffold.minecraftClient.player.getY()), Scaffold.minecraftClient.player.getZ());
                Vec3d WallPlayerSkullBlock = Scaffold.minecraftClient.player.getVelocity();
                double d = this.slow.getValue();
                Scaffold.minecraftClient.player.setVelocity(WallPlayerSkullBlock.x * d, (double)this.motionConfig.getValue(), WallPlayerSkullBlock.z * d);
                this.lastPlacementTime = Scaffold.minecraftClient.player.getY();
            }
            return;
        }
        if (this.tower.isSelected(this.pulldown)) {
            if (!Scaffold.minecraftClient.player.isOnGround() && VanillaChestLootTableGenerator.y < (double)this.pulldownTrigger.getValue()) {
                Scaffold.minecraftClient.player.setVelocity(VanillaChestLootTableGenerator.x, -1.0, VanillaChestLootTableGenerator.z);
            }
            return;
        }
        if (this.tower.isSelected(this.karhu)) {
            if (this.karhuPulldown.isEnabled() && !Scaffold.minecraftClient.player.isOnGround() && VanillaChestLootTableGenerator.y < (double)this.karhuTrigger.getValue()) {
                Scaffold.minecraftClient.player.setVelocity(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.y - 1.0, VanillaChestLootTableGenerator.z);
            }
            return;
        }
        if (this.tower.isSelected(this.vulcan)) {
            if (Scaffold.minecraftClient.player.age % 2 == 0) {
                Scaffold.minecraftClient.player.setVelocity(VanillaChestLootTableGenerator.x, 0.7, VanillaChestLootTableGenerator.z);
            } else {
                Scaffold.minecraftClient.player.setVelocity(VanillaChestLootTableGenerator.x, this.isPlacementTargetReady() ? 0.42 : 0.6, VanillaChestLootTableGenerator.z);
            }
            return;
        }
        if (this.tower.isSelected(this.hypixelMotion)) {
            if (Scaffold.minecraftClient.player.getX() % 1.0 != 0.0 && !this.isPlacementTargetReady()) {
                Scaffold.minecraftClient.player.setVelocity(Math.min((double)Math.round(Scaffold.minecraftClient.player.getX()) - Scaffold.minecraftClient.player.getX(), 0.281), VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z);
            }
            if (this.retryCount > 14) {
                Scaffold.minecraftClient.player.setVelocity(VanillaChestLootTableGenerator.x * 0.6, VanillaChestLootTableGenerator.y - 0.09, VanillaChestLootTableGenerator.z * 0.6);
                return;
            }
            if (this.retryCount % 3 == 0) {
                Scaffold.minecraftClient.player.setVelocity(VanillaChestLootTableGenerator.x, 0.42, VanillaChestLootTableGenerator.z);
                this.updatePlacementDistance(0.247 - (double)(ThreadLocalRandom.current().nextFloat() / 100.0f));
            } else if (this.retryCount % 3 == 2) {
                Scaffold.minecraftClient.player.setVelocity(VanillaChestLootTableGenerator.x, 1.0 - Scaffold.minecraftClient.player.getY() % 1.0, VanillaChestLootTableGenerator.z);
            }
        }
    }

    void resetMotionState() {
        Rotation rotation;
        if (!EntityUtils.isClientWorldReady()) {
            this.resetPlacementContext();
            return;
        }
        HandSelection handSelection = this.getCurrentHand();
        if (handSelection == null) {
            this.resetPlacementContext();
            return;
        }
        if (this.always.isEnabled() && handSelection.isMainHandSelection()) {
            this.processPlacementSlot(handSelection.getHotbarSlot());
        }
        this.currentHand = handSelection;
        PlacementTarget placementTarget = this.getCurrentPlacement();
        if (placementTarget == null) {
            this.resetPlacementContext();
            this.currentHand = handSelection;
            return;
        }
        this.resetPlacementTarget(placementTarget);
        if (this.rotationTiming.isSelected(this.tickRotation) && (rotation = this.getTargetRotation(placementTarget)) != null) {
            this.applyFallbackRotation(rotation);
        }
    }

    void resetFallbackState() {
        if (!EntityUtils.isClientWorldReady()) {
            this.resetPlacementContext();
            return;
        }
        this.resetAlternateState();
        if (this.currentPlacement == null || this.currentHand == null) {
            return;
        }
        HandSelection handSelection = this.getCurrentHand();
        if (handSelection == null) {
            this.resetPlacementContext();
            return;
        }
        this.currentHand = handSelection;
        PlacementTarget placementTarget = this.currentPlacement;
        if (!this.isTargetSelected(placementTarget)) {
            this.resetPlacementContext();
            return;
        }
        Rotation rotation = this.getTargetRotation(placementTarget);
        if (rotation == null) {
            this.currentSlot = 0;
            return;
        }
        if (this.rotationTiming.isSelected(this.onTick) || this.rotationTiming.isSelected(this.onTickSnap)) {
            this.applyPlacementRotation(rotation);
            RockstarClient.create().getRotationManager().setLastSentRotation(rotation);
            if (this.rotationTiming.isSelected(this.onTickSnap)) {
                this.applyFallbackRotation(rotation);
            }
        } else if (!this.isRotationValid(rotation)) {
            this.currentSlot = 0;
            return;
        }
        ++this.currentSlot;
        if (this.currentSlot < (int)this.stableTicks.getValue()) {
            return;
        }
        if (!this.cooldownTimer.hasElapsed(this.getLastRotationTime())) {
            return;
        }
        BlockHitResult class_39652 = this.raycastPlacement(placementTarget, rotation);
        if (class_39652 == null) {
            this.currentSlot = 0;
            this.resetTargetHandState(placementTarget, handSelection);
            return;
        }
        this.processPlacement(handSelection, placementTarget, class_39652);
    }

    private void resetAlternateState() {
        this.privilegedMode = false;
        if (!this.resetSprint.isEnabled() || Scaffold.minecraftClient.player == null) {
            return;
        }
        this.privilegedMode = Scaffold.minecraftClient.player.isSprinting();
        Scaffold.minecraftClient.options.sprintKey.setPressed(false);
        Scaffold.minecraftClient.player.setSprinting(false);
    }

    private void applyPlacementRotation(Rotation rotation) {
        Scaffold.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.Full(Scaffold.minecraftClient.player.getX(), Scaffold.minecraftClient.player.getY(), Scaffold.minecraftClient.player.getZ(), rotation.getYaw(), MathHelper.clamp((float)rotation.getPitch(), (float)-90.0f, (float)90.0f), Scaffold.minecraftClient.player.isOnGround(), Scaffold.minecraftClient.player.horizontalCollision));
        RockstarClient.create().getRotationManager().getPacketRotation().setYaw(rotation.getYaw());
        RockstarClient.create().getRotationManager().getPacketRotation().setPitch(rotation.getPitch());
    }

    private PlacementTarget getCurrentPlacement() {
        ModeSetting.Option option;
        Vec3d VanillaChestLootTableGenerator = this.getMotionPosition(this.motionSnapshot);
        if (VanillaChestLootTableGenerator == null) {
            VanillaChestLootTableGenerator = Scaffold.minecraftClient.player.getPos();
        }
        if ((option = this.getPlacementModeOption()) == this.expand) {
            return this.findPlacementTarget(VanillaChestLootTableGenerator);
        }
        PlacementTarget placementTarget = this.findCandidatePlacement(this.getNearbyBlocks(VanillaChestLootTableGenerator), option == this.normal);
        if (placementTarget == null && this.currentPlacement != null && this.isTargetSelected(this.currentPlacement)) {
            return this.adjustPlacementTarget(this.currentPlacement, Scaffold.minecraftClient.player.getEyePos());
        }
        return placementTarget;
    }

    private PlacementTarget findPlacementTarget(Vec3d VanillaChestLootTableGenerator) {
        int n = (int)this.length.getValue();
        for (int i = 0; i <= n; ++i) {
            BlockPos adminsky = this.getBlockAtOffset(VanillaChestLootTableGenerator, i);
            PlacementTarget placementTarget = this.findCandidatePlacement(this.getPlacementFaces(this.getNeighborBlock(adminsky), PlacementFaceMode.STANDARD), false);
            if (placementTarget == null) continue;
            return placementTarget.withFallbackHit(true);
        }
        return null;
    }

    private PlacementTarget findCandidatePlacement(Set<BlockPos> set, boolean bl) {
        Vec3d VanillaChestLootTableGenerator = Scaffold.minecraftClient.player.getEyePos();
        Rotation rotation = RockstarClient.create().getRotationManager().getCurrentRotation();
        PlacementTarget placementTarget = null;
        double d = Double.MAX_VALUE;
        ArrayList<BlockPos> arrayList = new ArrayList<BlockPos>(set);
        arrayList.sort(this.createPlacementComparator(Scaffold.minecraftClient.player.getPos(), this.motionSnapshot));
        for (BlockPos adminsky : arrayList) {
            if (!this.isBlockPositionSelected(adminsky)) continue;
            for (Direction class_23502 : Direction.values()) {
                Direction class_23503;
                BlockPos adminsky2;
                if (class_23502 == Direction.UP && !bl || !this.isFaceSelected(adminsky2 = adminsky.offset(class_23502), class_23503 = class_23502.getOpposite())) continue;
                for (Vec3d WallPlayerSkullBlock : this.collectPlacementCandidates(adminsky2, class_23503, VanillaChestLootTableGenerator)) {
                    double d2 = Scaffold.minecraftClient.player.getBlockInteractionRange();
                    double d3 = VanillaChestLootTableGenerator.squaredDistanceTo(WallPlayerSkullBlock);
                    if (d3 > d2 * d2) continue;
                    PlacementTarget placementTarget2 = new PlacementTarget(adminsky, adminsky2, class_23503, WallPlayerSkullBlock, this.calculateRotationBetween(VanillaChestLootTableGenerator, WallPlayerSkullBlock), !bl || this.isPlacementFeatureReady());
                    PlacementTarget placementTarget3 = this.adjustPlacementTarget(placementTarget2, VanillaChestLootTableGenerator);
                    if (placementTarget3 == null) continue;
                    Rotation rotation2 = placementTarget3.getRotation();
                    double d4 = d3 + this.calculateRotationDifference(rotation, rotation2) * 0.15;
                    if (this.isHitResultValid(this.raycastRotation(rotation), placementTarget3)) {
                        d4 -= 2.0;
                    }
                    if (this.motionSnapshot != null) {
                        d4 += this.motionSnapshot.getDistanceTo(placementTarget3.getPlacedBlock().toCenterPos()) * 0.25;
                    }
                    if (!(d4 < d)) continue;
                    d = d4;
                    placementTarget = placementTarget3;
                }
            }
        }
        return placementTarget;
    }

    private PlacementTarget adjustPlacementTarget(PlacementTarget placementTarget, Vec3d VanillaChestLootTableGenerator) {
        if (!this.isTargetSelected(placementTarget)) {
            return null;
        }
        Rotation rotation = this.calculateFallbackRotation(placementTarget, VanillaChestLootTableGenerator);
        PlacementTarget placementTarget2 = placementTarget.withRotation(rotation);
        if (placementTarget2.allowsFallbackHit() || this.isHitResultValid(this.raycastRotationFrom(rotation, VanillaChestLootTableGenerator), placementTarget2)) {
            return placementTarget2;
        }
        Rotation rotation2 = this.calculatePlacementRotation(placementTarget, VanillaChestLootTableGenerator);
        if (rotation2 == null) {
            return null;
        }
        return placementTarget.withRotation(rotation2);
    }

    private Rotation calculatePlacementRotation(PlacementTarget placementTarget, Vec3d VanillaChestLootTableGenerator) {
        Vec3d WallPlayerSkullBlock = Scaffold.minecraftClient.player.getVelocity();
        int n = Math.max(1, (int)Math.ceil(2.0));
        for (int i = 1; i <= n; ++i) {
            Vec3d VanillaEntityLootTableGenerator = VanillaChestLootTableGenerator.add(WallPlayerSkullBlock.x * (double)i, 0.0, WallPlayerSkullBlock.z * (double)i);
            Rotation rotation = this.calculateFallbackRotation(placementTarget, VanillaEntityLootTableGenerator);
            PlacementTarget placementTarget2 = placementTarget.withRotation(rotation);
            if (!placementTarget2.allowsFallbackHit() && !this.isHitResultValid(this.raycastRotationFrom(rotation, VanillaEntityLootTableGenerator), placementTarget2)) continue;
            return rotation;
        }
        return null;
    }

    private Rotation calculateFallbackRotation(PlacementTarget placementTarget, Vec3d VanillaChestLootTableGenerator) {
        if (this.technique.isSelected(this.godBridge)) {
            return this.getNearestRotation(placementTarget);
        }
        if (this.technique.isSelected(this.breezily)) {
            return this.getStableRotation(placementTarget);
        }
        if (this.technique.isSelected(this.expand)) {
            return this.calculateRotationBetween(VanillaChestLootTableGenerator, placementTarget.getPlacedBlock().toCenterPos());
        }
        return placementTarget.getRotation();
    }

    private Rotation getTargetRotation(PlacementTarget placementTarget) {
        if (placementTarget == null) {
            return null;
        }
        if (this.godBridge.isSelected()) {
            return this.getNearestRotation(placementTarget);
        }
        if (this.breezily.isSelected()) {
            return this.getStableRotation(placementTarget);
        }
        if (this.expand.isSelected()) {
            return this.calculateRotationBetween(Scaffold.minecraftClient.player.getEyePos(), placementTarget.getPlacedBlock().toCenterPos());
        }
        return placementTarget.getRotation();
    }

    private Rotation getNearestRotation(PlacementTarget placementTarget) {
        boolean bl;
        if (!this.placementContext.hasMovementInput()) {
            return this.getEdgeRotation(placementTarget);
        }
        float f = this.calculateMovementScale(this.calculateContextWeight(this.placementContext) + 180.0f);
        boolean bl2 = bl = Math.floorMod((int)f, 90) == 0;
        if (!bl) {
            return new Rotation(f, 75.6f);
        }
        if (Scaffold.minecraftClient.player.isOnGround()) {
            double d = Math.toRadians(f);
            this.disableLocked = Math.floor(Scaffold.minecraftClient.player.getX() + Math.cos(d) * 0.5) != Math.floor(Scaffold.minecraftClient.player.getX()) || Math.floor(Scaffold.minecraftClient.player.getZ() + Math.sin(d) * 0.5) != Math.floor(Scaffold.minecraftClient.player.getZ());
            Vec3d VanillaChestLootTableGenerator = Scaffold.minecraftClient.player.getPos().add(Math.cos(d) * 0.6, 0.0, Math.sin(d) * 0.6);
            boolean bl3 = Scaffold.minecraftClient.world.getBlockState(Scaffold.minecraftClient.player.getBlockPos().down()).isAir();
            boolean bl4 = Scaffold.minecraftClient.world.getBlockState(BlockPos.ofFloored((Position)VanillaChestLootTableGenerator).down()).isAir();
            if (bl3 && bl4) {
                this.disableLocked = !this.disableLocked;
            }
        }
        return new Rotation(f + (float)(this.disableLocked ? 45 : -45), 75.7f);
    }

    private Rotation getStableRotation(PlacementTarget placementTarget) {
        if (!this.placementContext.hasMovementInput()) {
            return this.getEdgeRotation(placementTarget);
        }
        float f = this.calculateMovementScale(this.calculateContextWeight(this.placementContext) + 180.0f);
        boolean bl = Math.floorMod((int)f, 90) == 0;
        return new Rotation(f, bl ? 80.0f : 75.6f);
    }

    private Rotation getEdgeRotation(PlacementTarget placementTarget) {
        float f = (float)Math.floor(placementTarget.getRotation().getYaw() / 90.0f) * 90.0f;
        return new Rotation(f + 45.0f, 75.0f);
    }

    private BlockHitResult raycastPlacement(PlacementTarget placementTarget, Rotation rotation) {
        BlockHitResult class_39652 = this.raycastRotation(rotation);
        if (class_39652 != null && this.isPlacementHitValid(class_39652, placementTarget)) {
            return class_39652;
        }
        if (placementTarget.allowsFallbackHit()) {
            return placementTarget.createHitResult();
        }
        return null;
    }

    private boolean isPlacementHitValid(BlockHitResult class_39652, PlacementTarget placementTarget) {
        return this.isHitResultValid(class_39652, placementTarget) && this.isHitResultAllowed(class_39652);
    }

    private boolean isHitResultAllowed(BlockHitResult class_39652) {
        Vec3d VanillaChestLootTableGenerator = class_39652.getPos().subtract(Scaffold.minecraftClient.player.getEyePos());
        Direction class_23502 = class_39652.getSide();
        if (class_23502.getAxis() != Direction.Axis.Y) {
            double d = class_23502 == Direction.NORTH || class_23502 == Direction.SOUTH ? VanillaChestLootTableGenerator.z : VanillaChestLootTableGenerator.x;
            return Math.abs(d) >= (double)this.minDist.getValue();
        }
        return true;
    }

    private Set<BlockPos> getNearbyBlocks(Vec3d VanillaChestLootTableGenerator) {
        PlacementFaceMode placementFaceMode;
        BlockPos adminsky = this.getNeighborBlock(BlockPos.ofFloored((Position)VanillaChestLootTableGenerator));
        PlacementFaceMode placementFaceMode2 = placementFaceMode = this.isPlacementFeatureReady() ? PlacementFaceMode.ALL_FACES : PlacementFaceMode.STANDARD;
        if (this.ceiling.isEnabled() && this.normal.isSelected() && this.isMovementFeatureReady()) {
            placementFaceMode = PlacementFaceMode.ADJACENT;
        }
        Set<BlockPos> set = this.getPlacementFaces(adminsky, placementFaceMode);
        Vec3d WallPlayerSkullBlock = Scaffold.minecraftClient.player.getVelocity();
        this.updateCandidateDistances(set, VanillaChestLootTableGenerator.x + WallPlayerSkullBlock.x, adminsky.getY(), VanillaChestLootTableGenerator.z + WallPlayerSkullBlock.z);
        Box HorizontalFacingBlock = Scaffold.minecraftClient.player.getBoundingBox().offset(WallPlayerSkullBlock.x, 0.0, WallPlayerSkullBlock.z);
        this.updateCandidateDistances(set, HorizontalFacingBlock.minX, adminsky.getY(), HorizontalFacingBlock.minZ);
        this.updateCandidateDistances(set, HorizontalFacingBlock.minX, adminsky.getY(), HorizontalFacingBlock.maxZ);
        this.updateCandidateDistances(set, HorizontalFacingBlock.maxX, adminsky.getY(), HorizontalFacingBlock.minZ);
        this.updateCandidateDistances(set, HorizontalFacingBlock.maxX, adminsky.getY(), HorizontalFacingBlock.maxZ);
        return set;
    }

    private Set<BlockPos> getPlacementFaces(BlockPos adminsky, PlacementFaceMode placementFaceMode) {
        LinkedHashSet<BlockPos> linkedHashSet = new LinkedHashSet<BlockPos>();
        linkedHashSet.add(adminsky);
        linkedHashSet.add(adminsky.north());
        linkedHashSet.add(adminsky.south());
        linkedHashSet.add(adminsky.east());
        linkedHashSet.add(adminsky.west());
        if (placementFaceMode == PlacementFaceMode.ADJACENT) {
            linkedHashSet.add(adminsky.north().east());
            linkedHashSet.add(adminsky.north().west());
            linkedHashSet.add(adminsky.south().east());
            linkedHashSet.add(adminsky.south().west());
            linkedHashSet.add(adminsky.up());
            linkedHashSet.add(adminsky.down());
        } else if (placementFaceMode == PlacementFaceMode.ALL_FACES) {
            linkedHashSet.add(adminsky.down());
            linkedHashSet.add(adminsky.down().north());
            linkedHashSet.add(adminsky.down().south());
            linkedHashSet.add(adminsky.down().east());
            linkedHashSet.add(adminsky.down().west());
        }
        return linkedHashSet;
    }

    private void updateCandidateDistances(Set<BlockPos> set, double d, double d2, double d3) {
        set.add(BlockPos.ofFloored((double)d, (double)d2, (double)d3));
    }

    private BlockPos getNeighborBlock(BlockPos adminsky) {
        if (this.isBlockFeatureReady() || this.placementActive) {
            return this.getSupportBlock(adminsky);
        }
        if (this.isPlacementFeatureReady()) {
            return adminsky.add(0, -2, 0);
        }
        if (this.ceiling.isEnabled() && this.normal.isSelected() && this.isMovementFeatureReady()) {
            return adminsky.add(0, 3, 0);
        }
        if (Scaffold.minecraftClient.player.input.playerInput.sneak() && (!this.isPlacementTargetReady() || Scaffold.minecraftClient.player.horizontalCollision)) {
            return adminsky.down();
        }
        if (this.sameY.isSelected(this.on)) {
            return new BlockPos(adminsky.getX(), this.placementAttempts, adminsky.getZ());
        }
        if (this.sameY.isSelected(this.falling)) {
            return Scaffold.minecraftClient.player.getVelocity().y < 0.2 ? new BlockPos(adminsky.getX(), this.placementAttempts, adminsky.getZ()) : adminsky.down();
        }
        if (this.sameY.isSelected(this.hypixel)) {
            if (Scaffold.minecraftClient.player.getVelocity().y == -0.15233518685055708 && this.placementCount >= 2) {
                this.placementCount = 0;
                return new BlockPos(adminsky.getX(), this.failedAttempts, adminsky.getZ());
            }
            return new BlockPos(adminsky.getX(), this.failedAttempts - 1, adminsky.getZ());
        }
        return adminsky.down();
    }

    private BlockPos getSupportBlock(BlockPos adminsky) {
        if (this.tower.isSelected(this.hypixelMotion) && !this.isPlacementTargetReady()) {
            BlockPos[] adminskyArray = new BlockPos[]{adminsky.add(0, 0, 1), adminsky.add(0, 0, -1), adminsky.add(1, 0, 0), adminsky.add(-1, 0, 0)};
            BlockPos adminsky2 = null;
            double d = Double.MAX_VALUE;
            for (BlockPos adminsky3 : adminskyArray) {
                double d2 = this.getBlockCenter(adminsky3).squaredDistanceTo(Scaffold.minecraftClient.player.getPos());
                if (!(d2 < d)) continue;
                d = d2;
                adminsky2 = adminsky3.down();
            }
            if (adminsky2 != null && !Scaffold.minecraftClient.world.getBlockState(adminsky2).isSideSolidFullSquare((BlockView)Scaffold.minecraftClient.world, adminsky2, Direction.UP)) {
                return adminsky2;
            }
        }
        return adminsky.down();
    }

    private BlockPos getBlockAtOffset(Vec3d VanillaChestLootTableGenerator, int n) {
        float f = Scaffold.minecraftClient.player.getYaw();
        return BlockPos.ofFloored((Position)VanillaChestLootTableGenerator).add((int)(-Math.sin(Math.toRadians(f)) * (double)n), 0, (int)(Math.cos(Math.toRadians(f)) * (double)n));
    }

    private Comparator<BlockPos> createPlacementComparator(Vec3d VanillaChestLootTableGenerator, MotionSnapshot motionSnapshot) {
        return (adminsky, adminsky2) -> {
            int n;
            if (motionSnapshot != null && (n = Double.compare(motionSnapshot.getDistanceTo(this.getBlockCenter((BlockPos)adminsky)), motionSnapshot.getDistanceTo(this.getBlockCenter((BlockPos)adminsky2)))) != 0) {
                return n;
            }
            return Double.compare(this.getBlockCenter((BlockPos)adminsky).squaredDistanceTo(VanillaChestLootTableGenerator), this.getBlockCenter((BlockPos)adminsky2).squaredDistanceTo(VanillaChestLootTableGenerator));
        };
    }

    private HandSelection getCurrentHand() {
        if (Scaffold.minecraftClient.player == null || Scaffold.minecraftClient.world == null) {
            return null;
        }
        int n = Scaffold.minecraftClient.player.getInventory().selectedSlot;
        ItemStack class_17992 = Scaffold.minecraftClient.player.getInventory().getStack(n);
        if (this.isBlockItemSelected(class_17992)) {
            return new HandSelection(Hand.MAIN_HAND, n, class_17992);
        }
        if (this.isBlockItemSelected(Scaffold.minecraftClient.player.getOffHandStack())) {
            return new HandSelection(Hand.OFF_HAND, -1, Scaffold.minecraftClient.player.getOffHandStack());
        }
        if (!this.autoBlock.isEnabled()) {
            return null;
        }
        return this.getFallbackHand();
    }

    private HandSelection getFallbackHand() {
        HandSelection handSelection = null;
        HandSelection handSelection2 = null;
        int n = (int)this.doNotUseBelow.getValue();
        for (int i = 0; i < 9; ++i) {
            ItemStack class_17992 = Scaffold.minecraftClient.player.getInventory().getStack(i);
            if (!this.isBlockItemSelected(class_17992)) continue;
            HandSelection handSelection3 = new HandSelection(Hand.MAIN_HAND, i, class_17992);
            if (handSelection2 == null || this.compareHands(handSelection3, handSelection2) > 0) {
                handSelection2 = handSelection3;
            }
            if (class_17992.getCount() <= n || handSelection != null && this.compareHands(handSelection3, handSelection) <= 0) continue;
            handSelection = handSelection3;
        }
        return handSelection != null ? handSelection : handSelection2;
    }

    private int compareHands(HandSelection handSelection, HandSelection handSelection2) {
        return Integer.compare(this.getBlockCount(handSelection.getItemStack(), true), this.getBlockCount(handSelection2.getItemStack(), true));
    }

    private int getBlockCount(ItemStack class_17992, boolean bl) {
        Block class_22482 = ((BlockItem)class_17992.getItem()).getBlock();
        BlockState class_26802 = class_22482.getDefaultState();
        int n = 0;
        if (!this.isHeldBlockValid(class_17992)) {
            n += 1000000;
        }
        if (class_26802.isSideSolidFullSquare((BlockView)Scaffold.minecraftClient.world, BlockPos.ORIGIN, Direction.UP)) {
            n += 100000;
        }
        if (!class_26802.getCollisionShape((BlockView)Scaffold.minecraftClient.world, BlockPos.ORIGIN).isEmpty()) {
            n += 10000;
        }
        return n += bl ? class_17992.getCount() : 64 - class_17992.getCount();
    }

    private boolean isBlockItemSelected(ItemStack class_17992) {
        if (class_17992 == null || class_17992.isEmpty()) {
            return false;
        }
        Item class_17922 = class_17992.getItem();
        if (!(class_17922 instanceof BlockItem)) {
            return false;
        }
        BlockItem class_17472 = (BlockItem)class_17922;
        Block class_22482 = class_17472.getBlock();
        if (this.isBlockAllowed(class_22482)) {
            return false;
        }
        BlockState class_26802 = class_22482.getDefaultState();
        return class_26802.isSideSolidFullSquare((BlockView)Scaffold.minecraftClient.world, BlockPos.ORIGIN, Direction.UP) && !class_26802.getCollisionShape((BlockView)Scaffold.minecraftClient.world, BlockPos.ORIGIN).isEmpty();
    }

    private boolean isBlockAllowed(Block class_22482) {
        return class_22482 instanceof FallingBlock || class_22482 == Blocks.TNT || class_22482 == Blocks.COBWEB || class_22482 == Blocks.NETHER_PORTAL || class_22482 == Blocks.POWDER_SNOW;
    }

    private boolean isHeldBlockValid(ItemStack class_17992) {
        Item class_17922 = class_17992.getItem();
        if (!(class_17922 instanceof BlockItem)) {
            return true;
        }
        BlockItem class_17472 = (BlockItem)class_17922;
        Block heldBlock = class_17472.getBlock();
        BlockState class_26802 = heldBlock.getDefaultState();
        return heldBlock.getSlipperiness() > 0.6f || heldBlock instanceof BlockWithEntity || class_26802.getCollisionShape((BlockView)Scaffold.minecraftClient.world, BlockPos.ORIGIN).isEmpty() || heldBlock == Blocks.CRAFTING_TABLE || heldBlock == Blocks.SMITHING_TABLE || heldBlock == Blocks.FLETCHING_TABLE || heldBlock == Blocks.ENCHANTING_TABLE || heldBlock == Blocks.CAULDRON || heldBlock == Blocks.MAGMA_BLOCK;
    }

    private int getSelectedBlockCount() {
        int n;
        int n2 = n = this.isBlockItemSelected(Scaffold.minecraftClient.player.getOffHandStack()) ? Scaffold.minecraftClient.player.getOffHandStack().getCount() : 0;
        if (!this.autoBlock.isEnabled()) {
            ItemStack class_17992 = Scaffold.minecraftClient.player.getInventory().getStack(Scaffold.minecraftClient.player.getInventory().selectedSlot);
            return n + (this.isBlockItemSelected(class_17992) ? class_17992.getCount() : 0);
        }
        for (int i = 0; i < 9; ++i) {
            ItemStack class_17993 = Scaffold.minecraftClient.player.getInventory().getStack(i);
            if (!this.isBlockItemSelected(class_17993)) continue;
            n += class_17993.getCount();
        }
        return n;
    }

    private boolean isBlockPositionSelected(BlockPos adminsky) {
        BlockState class_26802 = Scaffold.minecraftClient.world.getBlockState(adminsky);
        return class_26802.isAir() || class_26802.getCollisionShape((BlockView)Scaffold.minecraftClient.world, adminsky).isEmpty() && Scaffold.minecraftClient.world.getFluidState(adminsky).isEmpty();
    }

    private boolean isFaceSelected(BlockPos adminsky, Direction class_23502) {
        BlockState class_26802 = Scaffold.minecraftClient.world.getBlockState(adminsky);
        return !class_26802.isAir() && !class_26802.getCollisionShape((BlockView)Scaffold.minecraftClient.world, adminsky).isEmpty() && class_26802.isSideSolidFullSquare((BlockView)Scaffold.minecraftClient.world, adminsky, class_23502);
    }

    private boolean isTargetSelected(PlacementTarget placementTarget) {
        double d = Scaffold.minecraftClient.player.getBlockInteractionRange();
        return this.isBlockPositionSelected(placementTarget.getPlacedBlock()) && this.isFaceSelected(placementTarget.getSupportBlock(), placementTarget.getPlacementSide()) && placementTarget.getSupportBlock().offset(placementTarget.getPlacementSide()).equals(placementTarget.getPlacedBlock()) && Scaffold.minecraftClient.player.getEyePos().squaredDistanceTo(placementTarget.getHitPosition()) <= d * d;
    }

    private void applyFallbackRotation(Rotation rotation) {
        float f = this.rotationSpeed.getValue();
        RockstarClient.create().getRotationManager().requestRotation(new Rotation(rotation.getYaw(), rotation.getPitch()), this.getAttackType(), f, f, f, RotationPriority.ITEM_USE_PRIORITY);
    }

    private RotationCorrectionMode getAttackType() {
        if (this.moveCorrection.isSelected(this.strict)) {
            return RotationCorrectionMode.STRICT;
        }
        if (this.moveCorrection.isSelected(this.silent)) {
            return RotationCorrectionMode.UNSPECIFIED;
        }
        if (this.moveCorrection.isSelected(this.changeLook)) {
            return RotationCorrectionMode.CHANGE_LOOK;
        }
        return RotationCorrectionMode.NONE;
    }

    private boolean isRotationValid(Rotation rotation) {
        Rotation rotation2 = RockstarClient.create().getRotationManager().getCurrentRotation();
        float f = Math.abs(MathHelper.wrapDegrees((float)(rotation2.getYaw() - rotation.getYaw())));
        float f2 = Math.abs(rotation2.getPitch() - rotation.getPitch());
        float f3 = this.aimTolerance.getValue();
        return f <= f3 && f2 <= f3;
    }

    private BlockHitResult raycastRotation(Rotation rotation) {
        return this.raycastRotationFrom(rotation, Scaffold.minecraftClient.player.getEyePos());
    }

    private BlockHitResult raycastRotationFrom(Rotation rotation, Vec3d VanillaChestLootTableGenerator) {
        Vec3d WallPlayerSkullBlock = VanillaChestLootTableGenerator.add(Scaffold.minecraftClient.player.getRotationVector(rotation.getPitch(), rotation.getYaw()).multiply(Scaffold.minecraftClient.player.getBlockInteractionRange()));
        return Scaffold.minecraftClient.world.raycast(new RaycastContext(VanillaChestLootTableGenerator, WallPlayerSkullBlock, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)Scaffold.minecraftClient.player));
    }

    private Rotation calculateRotationBetween(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock) {
        double d = WallPlayerSkullBlock.x - VanillaChestLootTableGenerator.x;
        double d2 = WallPlayerSkullBlock.y - VanillaChestLootTableGenerator.y;
        double d3 = WallPlayerSkullBlock.z - VanillaChestLootTableGenerator.z;
        double d4 = Math.sqrt(d * d + d3 * d3);
        float f = (float)Math.toDegrees(Math.atan2(d3, d)) - 90.0f;
        float f2 = (float)(-Math.toDegrees(Math.atan2(d2, d4)));
        return new Rotation(f, MathHelper.clamp((float)f2, (float)-90.0f, (float)90.0f));
    }

    private boolean isHitResultValid(BlockHitResult class_39652, PlacementTarget placementTarget) {
        if (class_39652 == null || class_39652.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos adminsky = class_39652.getBlockPos();
        Direction class_23502 = class_39652.getSide();
        BlockPos adminsky2 = adminsky.offset(class_23502);
        if (!adminsky.equals(placementTarget.getSupportBlock()) || class_23502 != placementTarget.getPlacementSide() || !adminsky2.equals(placementTarget.getPlacedBlock())) {
            return false;
        }
        return this.isBlockPositionSelected(adminsky2) && this.isFaceSelected(adminsky, class_23502);
    }

    private List<Vec3d> collectPlacementCandidates(BlockPos adminsky, Direction class_23502, Vec3d VanillaChestLootTableGenerator) {
        ArrayList<Vec3d> arrayList = new ArrayList<Vec3d>();
        double d = this.rotationMode.isSelected(this.edgePoint) ? 0.04 : 0.18;
        double d2 = this.rotationMode.isSelected(this.edgePoint) ? 0.96 : 0.82;
        double d3 = MathHelper.clamp((double)(VanillaChestLootTableGenerator.x - (double)adminsky.getX()), (double)d, (double)d2);
        double d4 = MathHelper.clamp((double)(VanillaChestLootTableGenerator.y - (double)adminsky.getY()), (double)d, (double)d2);
        double d5 = MathHelper.clamp((double)(VanillaChestLootTableGenerator.z - (double)adminsky.getZ()), (double)d, (double)d2);
        double d6 = 0.5;
        double d7 = 0.82;
        if (this.rotationMode.isSelected(this.center)) {
            d3 = d6;
            d4 = d6;
            d5 = d6;
        } else if (this.rotationMode.isSelected(this.random)) {
            d3 = this.calculateDistance(d, d2);
            d4 = this.calculateDistance(d, d2);
            d5 = this.calculateDistance(d, d2);
        } else if (this.rotationMode.isSelected(this.reverseYaw)) {
            d3 = 1.0 - d3;
            d5 = 1.0 - d5;
        } else if (this.rotationMode.isSelected(this.diagonalYaw)) {
            d3 = d3 < 0.5 ? d : d2;
            d5 = d5 < 0.5 ? d : d2;
        } else if (this.rotationMode.isSelected(this.angleYaw)) {
            float f = MathHelper.wrapDegrees((float)Scaffold.minecraftClient.player.getYaw());
            d3 = Math.sin(Math.toRadians(f)) > 0.0 ? d2 : d;
            double d8 = d5 = Math.cos(Math.toRadians(f)) > 0.0 ? d2 : d;
        }
        if (class_23502.getAxis() == Direction.Axis.X) {
            double faceCoordinate = adminsky.getX() + (class_23502 == Direction.EAST ? 1 : 0);
            this.updateCandidateRotations(arrayList, faceCoordinate, (double)adminsky.getY() + d4, (double)adminsky.getZ() + d5);
            this.updateCandidateRotations(arrayList, faceCoordinate, (double)adminsky.getY() + d7, (double)adminsky.getZ() + d5);
            this.updateCandidateRotations(arrayList, faceCoordinate, (double)adminsky.getY() + d4, (double)adminsky.getZ() + d6);
            this.updateCandidateRotations(arrayList, faceCoordinate, (double)adminsky.getY() + d7, (double)adminsky.getZ() + d6);
            this.updateCandidateRotations(arrayList, faceCoordinate, (double)adminsky.getY() + d6, (double)adminsky.getZ() + d6);
        } else if (class_23502.getAxis() == Direction.Axis.Y) {
            double faceCoordinate = adminsky.getY() + (class_23502 == Direction.UP ? 1 : 0);
            this.updateCandidateRotations(arrayList, (double)adminsky.getX() + d3, faceCoordinate, (double)adminsky.getZ() + d5);
            this.updateCandidateRotations(arrayList, (double)adminsky.getX() + d3, faceCoordinate, (double)adminsky.getZ() + d6);
            this.updateCandidateRotations(arrayList, (double)adminsky.getX() + d6, faceCoordinate, (double)adminsky.getZ() + d5);
            this.updateCandidateRotations(arrayList, (double)adminsky.getX() + d6, faceCoordinate, (double)adminsky.getZ() + d6);
        } else {
            double faceCoordinate = adminsky.getZ() + (class_23502 == Direction.SOUTH ? 1 : 0);
            this.updateCandidateRotations(arrayList, (double)adminsky.getX() + d3, (double)adminsky.getY() + d4, faceCoordinate);
            this.updateCandidateRotations(arrayList, (double)adminsky.getX() + d3, (double)adminsky.getY() + d7, faceCoordinate);
            this.updateCandidateRotations(arrayList, (double)adminsky.getX() + d6, (double)adminsky.getY() + d4, faceCoordinate);
            this.updateCandidateRotations(arrayList, (double)adminsky.getX() + d6, (double)adminsky.getY() + d7, faceCoordinate);
            this.updateCandidateRotations(arrayList, (double)adminsky.getX() + d6, (double)adminsky.getY() + d6, faceCoordinate);
        }
        if (this.rotationMode.isSelected(this.nearestRotation)) {
            Rotation rotation = RockstarClient.create().getRotationManager().getCurrentRotation();
            arrayList.sort(Comparator.comparingDouble(WallPlayerSkullBlock -> this.calculateRotationDifference(rotation, this.calculateRotationBetween(VanillaChestLootTableGenerator, (Vec3d)WallPlayerSkullBlock))));
        } else if (this.rotationMode.isSelected(this.stabilized) && this.motionSnapshot != null) {
            arrayList.sort(Comparator.comparingDouble(WallPlayerSkullBlock -> this.motionSnapshot.getDistanceTo((Vec3d)WallPlayerSkullBlock) + WallPlayerSkullBlock.squaredDistanceTo(VanillaChestLootTableGenerator) * 0.05));
        } else {
            arrayList.sort(Comparator.comparingDouble(WallPlayerSkullBlock -> WallPlayerSkullBlock.squaredDistanceTo(VanillaChestLootTableGenerator)));
        }
        return arrayList;
    }

    private void updateCandidateRotations(List<Vec3d> list, double d, double d2, double d3) {
        Vec3d VanillaChestLootTableGenerator = new Vec3d(d, d2, d3);
        if (!list.contains(VanillaChestLootTableGenerator)) {
            list.add(VanillaChestLootTableGenerator);
        }
    }

    private double calculateRotationDifference(Rotation rotation, Rotation rotation2) {
        return Math.abs(MathHelper.wrapDegrees((float)(rotation.getYaw() - rotation2.getYaw()))) + Math.abs(rotation.getPitch() - rotation2.getPitch());
    }

    private void processPlacement(HandSelection handSelection, PlacementTarget placementTarget, BlockHitResult class_39652) {
        if (this.resetSprint.isEnabled()) {
            Scaffold.minecraftClient.options.sprintKey.setPressed(false);
            if (this.privilegedMode || Scaffold.minecraftClient.player.isSprinting()) {
                TargetActionQueue.queueTargetAction((PlayerEntity)Scaffold.minecraftClient.player, () -> this.resetPlacementAttempt(handSelection, placementTarget, class_39652), true);
                this.privilegedMode = false;
                return;
            }
            Scaffold.minecraftClient.player.setSprinting(false);
        }
        this.resetPlacementAttempt(handSelection, placementTarget, class_39652);
    }

    private void resetPlacementAttempt(HandSelection handSelection, PlacementTarget placementTarget, BlockHitResult class_39652) {
        ActionResult class_12692;
        if (!EntityUtils.isClientWorldReady()) {
            return;
        }
        if (handSelection.isMainHandSelection()) {
            this.processPlacementSlot(handSelection.getHotbarSlot());
        }
        if ((class_12692 = Scaffold.minecraftClient.interactionManager.interactBlock(Scaffold.minecraftClient.player, handSelection.getHand(), class_39652)).isAccepted()) {
            Scaffold.minecraftClient.player.swingHand(handSelection.getHand());
            this.resetBlockState(placementTarget.getPlacedBlock());
        }
    }

    private void resetBlockState(BlockPos adminsky) {
        this.processPlacementBlock(adminsky);
        this.processMotion(this.motionSnapshot, this.motionSnapshot == null ? null : this.getPreviousMotionPosition(this.motionSnapshot));
        this.updateRotationState();
        this.cooldownTimer.reset();
        this.resetPlacementContext();
    }

    private void resetTargetHandState(PlacementTarget placementTarget, HandSelection handSelection) {
        if (!this.simulatePlacementAttempts.isEnabled() || !this.isPlacementTargetReady() || handSelection == null || placementTarget == null) {
            return;
        }
        if (this.failedOnly.isEnabled() && placementTarget.allowsFallbackHit()) {
            return;
        }
        if (ThreadLocalRandom.current().nextInt(3) == 0) {
            Scaffold.minecraftClient.player.swingHand(handSelection.getHand());
        }
    }

    private void resetPlacementTarget(PlacementTarget placementTarget) {
        if (!placementTarget.getPlacedBlock().equals(this.targetBlock) || !placementTarget.getSupportBlock().equals(this.previousBlock) || placementTarget.getPlacementSide() != this.placementFace) {
            this.currentSlot = 0;
        }
        this.currentPlacement = placementTarget;
        this.targetBlock = placementTarget.getPlacedBlock();
        this.previousBlock = placementTarget.getSupportBlock();
        this.placementFace = placementTarget.getPlacementSide();
    }

    private void resetPlacementContext() {
        this.currentPlacement = null;
        this.currentHand = null;
        this.targetBlock = null;
        this.previousBlock = null;
        this.placementFace = null;
        this.currentSlot = 0;
    }

    private void updatePlacementState() {
        this.resetPlacementContext();
        this.pendingPlacements.clear();
        this.queuedPlacements.clear();
        this.motionSnapshot = null;
        this.placementContext = PlacementContext.NO_INPUT;
        this.blockPositionData = null;
        this.placedBlock = null;
        this.rotationProgress = Float.NaN;
        this.slotIndex = 0;
        this.previousSlot = 0;
        this.retryCount = 0;
        this.lastPlacementTime = Double.NaN;
        this.placementActive = false;
        this.disableLocked = false;
        this.rotationDelta = 0.0f;
        this.lastPlacementTick = 0L;
    }

    private MotionSnapshot getMotionSnapshot(PlacementContext placementContext) {
        Vec3d VanillaChestLootTableGenerator = this.getInterpolatedPosition(this.calculateContextWeight(placementContext));
        BlockPositionData blockPositionData = this.getBlockPositionData();
        if (blockPositionData == null) {
            return null;
        }
        this.blockPositionData = blockPositionData;
        MotionSnapshot motionSnapshot = this.getCurrentMotion();
        Vec3d WallPlayerSkullBlock = motionSnapshot != null && motionSnapshot.getPositionPrimary().dotProduct(VanillaChestLootTableGenerator) >= 0.5 ? motionSnapshot.interpolatePosition(Scaffold.minecraftClient.player.getPos()) : new Vec3d((double)blockPositionData.getBlockPosition().getX() + 0.5 + blockPositionData.getOffsetX(), Scaffold.minecraftClient.player.getY(), (double)blockPositionData.getBlockPosition().getZ() + 0.5 + blockPositionData.getOffsetZ());
        return new MotionSnapshot(new Vec3d(WallPlayerSkullBlock.x, Scaffold.minecraftClient.player.getY(), WallPlayerSkullBlock.z), VanillaChestLootTableGenerator);
    }

    public void updateInputContext(PlacementContext context) {
        this.placementContext = context == null ? PlacementContext.NO_INPUT : context;
        this.motionSnapshot = this.placementContext.hasMovementInput() ? this.getMotionSnapshot(this.placementContext) : null;
    }

    public void processInput(InputEvent inputEvent) {
        this.updateInputContext(PlacementContext.fromInput(inputEvent));
        if (this.breezily.isSelected()) {
            this.handlePlacementInput(inputEvent);
        }
        if (this.stabilizeMovement.isEnabled() && this.normal.isSelected()) {
            this.onInput(inputEvent);
        }
        if (this.speedLimiter.isEnabled() && this.getTargetDistance() > this.speed.getValue()) {
            inputEvent.setForward(0.0f);
            inputEvent.setStrafe(0.0f);
        }
        if (this.slotIndex > 0) {
            inputEvent.setSneak(true);
            --this.slotIndex;
        }
        if (this.eagle.isEnabled() && this.normal.isSelected() && this.isPlacementInputActive(inputEvent)) {
            inputEvent.setSneak(true);
        }
        PlacementSettings settings = this.getPlacementSettings();
        if (settings.shouldJump()) {
            inputEvent.setJump(true);
        }
        if (settings.shouldStopInput()) {
            inputEvent.setForward(0.0f);
            inputEvent.setStrafe(0.0f);
        }
        if (settings.shouldStepBack()) {
            inputEvent.setForward(-1.0f);
            inputEvent.setStrafe(0.0f);
        }
        if (settings.getSneakTime() > 0) {
            inputEvent.setSneak(true);
            this.slotIndex = Math.max(this.slotIndex, settings.getSneakTime());
        }
        if (this.down.isEnabled() && this.normal.isSelected() && this.isRotationFeatureReady()) {
            inputEvent.setSneak(false);
        }
    }

    private MotionSnapshot getCurrentMotion() {
        if (this.pendingPlacements.size() < 2) {
            return null;
        }
        BlockPos adminsky2 = null;
        BlockPos adminsky3 = null;
        for (BlockPos adminsky4 : this.pendingPlacements) {
            adminsky3 = adminsky2;
            adminsky2 = adminsky4;
        }
        if (adminsky2 == null || adminsky3 == null) {
            return null;
        }
        Vec3d firstEdgePosition = this.getBlockEdgePosition(adminsky3);
        Vec3d secondEdgePosition = this.getBlockEdgePosition(adminsky2);
        Vec3d movementDirection = secondEdgePosition.subtract(firstEdgePosition).normalize();
        Vec3d midpoint = firstEdgePosition.add(secondEdgePosition).multiply(0.5);
        return new MotionSnapshot(midpoint, movementDirection);
    }

    private BlockPositionData getBlockPositionData() {
        List<BlockFaceCandidate> list = this.collectPendingBlocks();
        if (list.isEmpty()) {
            this.blockPositionData = null;
            this.placedBlock = null;
            return null;
        }
        list.sort(null);
        BlockFaceCandidate blockFaceCandidate = list.getFirst();
        BlockFaceCandidate blockFaceCandidate2 = this.selectBlockFace(list, blockFaceCandidate);
        this.placedBlock = blockFaceCandidate2.getBlockPosition();
        return new BlockPositionData(blockFaceCandidate2.getBlockPosition(), Scaffold.minecraftClient.player.getX() - ((double)blockFaceCandidate2.getBlockPosition().getX() + 0.5), Scaffold.minecraftClient.player.getZ() - ((double)blockFaceCandidate2.getBlockPosition().getZ() + 0.5));
    }

    private List<BlockFaceCandidate> collectPendingBlocks() {
        double[] dArray;
        ArrayList<BlockFaceCandidate> arrayList = new ArrayList<BlockFaceCandidate>();
        LinkedHashSet<BlockPos> linkedHashSet = new LinkedHashSet<BlockPos>();
        for (double d : dArray = new double[]{0.301, 0.0, -0.301}) {
            for (double d2 : dArray) {
                BlockPos adminsky = BlockPos.ofFloored((double)(Scaffold.minecraftClient.player.getX() + d), (double)(Scaffold.minecraftClient.player.getY() - 1.0), (double)(Scaffold.minecraftClient.player.getZ() + d2));
                if (!linkedHashSet.add(adminsky) || Scaffold.minecraftClient.world.getBlockState(adminsky).getCollisionShape((BlockView)Scaffold.minecraftClient.world, adminsky).isEmpty()) continue;
                arrayList.add(this.createBlockFaceCandidate(adminsky));
            }
        }
        return arrayList;
    }

    private BlockFaceCandidate selectBlockFace(List<BlockFaceCandidate> list, BlockFaceCandidate blockFaceCandidate) {
        BlockFaceCandidate blockFaceCandidate2 = null;
        BlockFaceCandidate blockFaceCandidate3 = null;
        BlockPos adminsky = this.pendingPlacements.peekLast();
        for (BlockFaceCandidate blockFaceCandidate4 : list) {
            if (Objects.equals(blockFaceCandidate4.getBlockPosition(), adminsky)) {
                blockFaceCandidate2 = blockFaceCandidate4;
            }
            if (!Objects.equals(blockFaceCandidate4.getBlockPosition(), this.placedBlock)) continue;
            blockFaceCandidate3 = blockFaceCandidate4;
        }
        if (blockFaceCandidate2 != null && blockFaceCandidate2.isWithinTolerance(blockFaceCandidate)) {
            return blockFaceCandidate2;
        }
        if (blockFaceCandidate3 != null && blockFaceCandidate3.isWithinTolerance(blockFaceCandidate)) {
            return blockFaceCandidate3;
        }
        return blockFaceCandidate;
    }

    private BlockFaceCandidate createBlockFaceCandidate(BlockPos adminsky) {
        Box HorizontalFacingBlock = Scaffold.minecraftClient.player.getBoundingBox();
        List<Box> collisionBoxes = Scaffold.minecraftClient.world.getBlockState(adminsky).getCollisionShape((BlockView)Scaffold.minecraftClient.world, adminsky).getBoundingBoxes();
        double d = Double.POSITIVE_INFINITY;
        double d2 = 0.0;
        for (Box collisionBox : collisionBoxes) {
            Box offsetBox = collisionBox.offset(adminsky);
            double d3 = Math.min(HorizontalFacingBlock.maxX, offsetBox.maxX) - Math.max(HorizontalFacingBlock.minX, offsetBox.minX);
            double d4 = Math.min(HorizontalFacingBlock.maxZ, offsetBox.maxZ) - Math.max(HorizontalFacingBlock.minZ, offsetBox.minZ);
            if (d3 <= 0.0 || d4 <= 0.0) continue;
            double d5 = Math.abs(HorizontalFacingBlock.minY - offsetBox.maxY);
            double d6 = d3 * d4;
            if (d5 + 0.001 < d) {
                d = d5;
                d2 = d6;
                continue;
            }
            if (!(Math.abs(d5 - d) <= 0.001)) continue;
            d2 += d6;
        }
        return new BlockFaceCandidate(adminsky, d2, d, this.calculateDistanceBetween(this.getBlockCenter(adminsky), Scaffold.minecraftClient.player.getPos()));
    }

    private Vec3d getInterpolatedPosition(float f) {
        float f2;
        if (!Float.isNaN(this.rotationProgress) && MathHelper.angleBetween((float)f, (float)this.rotationProgress) <= 30.0f) {
            return this.getPredictedPosition(this.rotationProgress);
        }
        float f3 = f / 180.0f * 4.0f + 4.0f;
        float f4 = Math.round(f3);
        this.rotationProgress = f2 = MathHelper.wrapDegrees((float)((f4 - 4.0f) / 4.0f * 180.0f));
        return this.getPredictedPosition(f2);
    }

    private void processPlacementBlock(BlockPos adminsky) {
        if (adminsky.equals(this.pendingPlacements.peekLast())) {
            return;
        }
        while (this.pendingPlacements.size() >= 4) {
            this.pendingPlacements.removeFirst();
        }
        this.pendingPlacements.add(adminsky);
    }

    private Vec3d getMotionPosition(MotionSnapshot motionSnapshot) {
        if (motionSnapshot == null || this.isDistanceValid(0.05)) {
            return null;
        }
        Vec3d VanillaChestLootTableGenerator = this.getPreviousMotionPosition(motionSnapshot);
        if (VanillaChestLootTableGenerator == null) {
            return null;
        }
        Vec3d WallPlayerSkullBlock = Scaffold.minecraftClient.player.getPos();
        Vec3d VanillaEntityLootTableGenerator = VanillaChestLootTableGenerator.subtract(WallPlayerSkullBlock);
        Vec3d PlayerSkullBlock = this.interpolatePosition(VanillaChestLootTableGenerator, VanillaEntityLootTableGenerator);
        Vec3d RedstoneBlock = this.getPlayerPosition();
        if (RedstoneBlock == null) {
            if (this.blockPositionData != null) {
                return PlayerSkullBlock.add(this.blockPositionData.getOffsetX(), 0.0, this.blockPositionData.getOffsetZ());
            }
            return PlayerSkullBlock;
        }
        float f = (float)Math.atan2(motionSnapshot.getPositionPrimary().z, motionSnapshot.getPositionPrimary().x);
        Vec3d VanillaFishingLootTableGenerator = VanillaChestLootTableGenerator.add(this.offsetPosition(RedstoneBlock, -f));
        return this.interpolateOffsetPosition(PlayerSkullBlock, VanillaFishingLootTableGenerator, this.getPlacementDistance());
    }

    private void processMotion(MotionSnapshot motionSnapshot, Vec3d VanillaChestLootTableGenerator) {
        if (motionSnapshot == null || VanillaChestLootTableGenerator == null) {
            return;
        }
        float f = (float)Math.atan2(motionSnapshot.getPositionPrimary().z, motionSnapshot.getPositionPrimary().x);
        Vec3d WallPlayerSkullBlock = this.offsetPosition(Scaffold.minecraftClient.player.getPos().subtract(VanillaChestLootTableGenerator), f);
        this.queuedPlacements.addLast(WallPlayerSkullBlock);
        while (this.queuedPlacements.size() > 4) {
            this.queuedPlacements.removeFirst();
        }
    }

    private Vec3d getPlayerPosition() {
        if (this.queuedPlacements.isEmpty()) {
            return null;
        }
        double d = 0.0;
        double d2 = 0.0;
        double d3 = 0.0;
        for (Vec3d VanillaChestLootTableGenerator : this.queuedPlacements) {
            d += VanillaChestLootTableGenerator.x;
            d2 += VanillaChestLootTableGenerator.y;
            d3 += VanillaChestLootTableGenerator.z;
        }
        double d4 = this.queuedPlacements.size();
        return new Vec3d(d / d4, d2 / d4, d3 / d4);
    }

    private Vec3d getPreviousMotionPosition(MotionSnapshot motionSnapshot) {
        Vec3d VanillaChestLootTableGenerator = motionSnapshot.interpolatePosition(Scaffold.minecraftClient.player.getPos()).add(0.0, -0.1, 0.0);
        Vec3d WallPlayerSkullBlock = motionSnapshot.getPositionPrimary().normalize();
        Vec3d VanillaEntityLootTableGenerator = VanillaChestLootTableGenerator;
        for (double d = 0.0; d <= 3.0; d += 0.05) {
            Vec3d PlayerSkullBlock = VanillaChestLootTableGenerator.add(WallPlayerSkullBlock.multiply(d));
            if (!this.isDistanceRangeValid(PlayerSkullBlock.x, PlayerSkullBlock.z)) {
                return new Vec3d(VanillaEntityLootTableGenerator.x, Scaffold.minecraftClient.player.getY(), VanillaEntityLootTableGenerator.z);
            }
            VanillaEntityLootTableGenerator = PlayerSkullBlock;
        }
        return null;
    }

    private Vec3d interpolatePosition(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock) {
        double d = 0.2;
        if (d <= 0.0 || WallPlayerSkullBlock.lengthSquared() < 1.0E-6) {
            return VanillaChestLootTableGenerator;
        }
        return VanillaChestLootTableGenerator.subtract(WallPlayerSkullBlock.normalize().multiply(d));
    }

    private double getPlacementDistance() {
        int n = 2;
        return MathHelper.clamp((double)((double)this.queuedPlacements.size() / (double)n), (double)0.0, (double)1.0);
    }

    PlacementSettings getPlacementSettings() {
        int n;
        if (!this.ledge.isEnabled() || Scaffold.minecraftClient.player == null) {
            return PlacementSettings.DEFAULT;
        }
        Rotation rotation = this.getTargetRotation(this.currentPlacement);
        if (rotation == null) {
            rotation = RockstarClient.create().getRotationManager().getEffectiveRotation();
        }
        if (this.isDistanceValid(0.05)) {
            boolean bl;
            n = this.getRotationIndex(rotation);
            boolean bl2 = this.getSelectedBlockCount() <= 0;
            boolean bl3 = bl = n >= 1;
            if (bl2 || bl) {
                return new PlacementSettings(false, Math.max(1, n), false, false);
            }
        }
        if (this.godBridge.isSelected() && this.isScaffoldReady()) {
            if ((float)this.getSelectedBlockCount() < this.forceSneakBelow.getValue()) {
                return new PlacementSettings(false, this.getRangeValue(this.sneakTime), false, false);
            }
            n = ThreadLocalRandom.current().nextInt(4);
            return switch (n) {
                case 0 -> new PlacementSettings(true, 0, false, false);
                case 1 -> new PlacementSettings(false, this.getRangeValue(this.sneakTime), false, false);
                case 2 -> new PlacementSettings(false, 0, true, false);
                default -> new PlacementSettings(false, 0, false, true);
            };
        }
        return PlacementSettings.DEFAULT;
    }

    private int getRotationIndex(Rotation rotation) {
        Rotation rotation2 = RockstarClient.create().getRotationManager().getCurrentRotation();
        float f = Math.max(1.0f, this.rotationSpeed.getValue());
        double d = this.calculateRotationDifference(rotation2, rotation);
        return (int)Math.ceil(d / (double)f);
    }

    private boolean isScaffoldReady() {
        Vec3d VanillaChestLootTableGenerator = Scaffold.minecraftClient.player.getVelocity();
        Vec3d WallPlayerSkullBlock = Scaffold.minecraftClient.player.getPos().add(VanillaChestLootTableGenerator.x, 0.0, VanillaChestLootTableGenerator.z);
        return !this.isDistanceRangeValid(WallPlayerSkullBlock.x, WallPlayerSkullBlock.z);
    }

    boolean isPlacementInputActive(InputEvent inputEvent) {
        if (this.isRotationFeatureReady()) {
            return false;
        }
        if (!Scaffold.minecraftClient.player.isOnGround() && this.onlyOnGround.isEnabled()) {
            return false;
        }
        if (Scaffold.minecraftClient.player.getAbilities().flying || this.previousSlot != 0) {
            return false;
        }
        PlacementContext placementContext = PlacementContext.fromInput(inputEvent);
        return this.isContextDistanceValid(placementContext, this.placementOffset);
    }

    private void updateRotationState() {
        if (!this.eagle.isEnabled()) {
            return;
        }
        ++this.previousSlot;
        if (this.previousSlot > this.blockCount) {
            this.updateMovementState();
        }
    }

    private void updateMovementState() {
        this.previousSlot = 0;
        this.blockCount = this.getRangeValue(this.blocks);
        this.placementOffset = this.getRangeProgress(this.edgeDistance);
    }

    void onInput(InputEvent inputEvent) {
        double d;
        Vec3d VanillaChestLootTableGenerator;
        if (this.motionSnapshot == null || inputEvent.isJump() && Scaffold.minecraftClient.player.isOnGround()) {
            return;
        }
        Vec3d WallPlayerSkullBlock = this.motionSnapshot.interpolatePosition(Scaffold.minecraftClient.player.getPos());
        Vec3d VanillaEntityLootTableGenerator = WallPlayerSkullBlock.subtract(Scaffold.minecraftClient.player.getPos());
        boolean bl = VanillaEntityLootTableGenerator.dotProduct(VanillaChestLootTableGenerator = new Vec3d(Scaffold.minecraftClient.player.getVelocity().x, 0.0, Scaffold.minecraftClient.player.getVelocity().z)) > 0.0;
        double d2 = d = bl ? 0.075 : 0.2;
        if (WallPlayerSkullBlock.squaredDistanceTo(Scaffold.minecraftClient.player.getPos()) < d * d) {
            return;
        }
        PlacementContext placementContext = PlacementContext.fromInput(inputEvent);
        PlacementContext placementContext2 = this.createPlacementContext(VanillaEntityLootTableGenerator, Scaffold.minecraftClient.player.getYaw());
        boolean bl2 = placementContext.isMovingForward() || placementContext.isMovingBackward();
        boolean bl3 = placementContext.isStrafingLeft() || placementContext.isStrafingRight();
        inputEvent.setForward(bl2 ? inputEvent.getForward() : placementContext2.getForwardAxis());
        inputEvent.setStrafe(bl3 ? inputEvent.getStrafe() : placementContext2.getStrafeAxis());
    }

    void handlePlacementInput(InputEvent inputEvent) {
        if (!inputEvent.isSneak() && inputEvent.getForward() > 0.0f) {
            if (Scaffold.minecraftClient.world.getBlockState(Scaffold.minecraftClient.player.getBlockPos().down()).isAir()) {
                this.lastPlacementTick = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - this.lastPlacementTick > 500L) {
                return;
            }
            double d = Scaffold.minecraftClient.player.getX() - Math.floor(Scaffold.minecraftClient.player.getX());
            double d2 = Scaffold.minecraftClient.player.getZ() - Math.floor(Scaffold.minecraftClient.player.getZ());
            double d3 = this.movementCorrection;
            double d4 = 1.0 - d3;
            float f = 0.0f;
            Direction class_23502 = this.getPlacementDirection(Scaffold.minecraftClient.player.getYaw());
            if (class_23502 == Direction.SOUTH) {
                if (d > d4) {
                    f = 1.0f;
                }
                if (d < d3) {
                    f = -1.0f;
                }
            } else if (class_23502 == Direction.NORTH) {
                if (d > d4) {
                    f = -1.0f;
                }
                if (d < d3) {
                    f = 1.0f;
                }
            } else if (class_23502 == Direction.EAST) {
                if (d2 > d4) {
                    f = -1.0f;
                }
                if (d2 < d3) {
                    f = 1.0f;
                }
            } else if (class_23502 == Direction.WEST) {
                if (d2 > d4) {
                    f = 1.0f;
                }
                if (d2 < d3) {
                    f = -1.0f;
                }
            }
            if (this.rotationDelta != f && f != 0.0f) {
                this.rotationDelta = f;
                this.movementCorrection = this.getRangeProgress(this.sneakDistance);
            }
            inputEvent.setStrafe(this.rotationDelta);
        }
    }

    private boolean isPlacementFeatureReady() {
        return this.down.isEnabled() && this.normal.isSelected() && Scaffold.minecraftClient.options.sneakKey.isPressed();
    }

    boolean isRotationFeatureReady() {
        BlockPos adminsky = Scaffold.minecraftClient.player.getBlockPos().add(0, -2, 0);
        return this.isPlacementFeatureReady() && Scaffold.minecraftClient.world.getBlockState(adminsky).isSideSolidFullSquare((BlockView)Scaffold.minecraftClient.world, adminsky, Direction.UP);
    }

    private boolean isMovementFeatureReady() {
        return !Scaffold.minecraftClient.world.getBlockState(Scaffold.minecraftClient.player.getBlockPos().down()).isAir();
    }

    private boolean isPlacementWorldReady() {
        BlockPos adminsky = Scaffold.minecraftClient.player.getBlockPos().up(2);
        return !Scaffold.minecraftClient.world.getBlockState(adminsky).getCollisionShape((BlockView)Scaffold.minecraftClient.world, adminsky).isEmpty() && Scaffold.minecraftClient.player.isOnGround();
    }

    private boolean isBlockFeatureReady() {
        boolean bl;
        boolean bl2 = bl = !this.tower.isSelected(this.none) && Scaffold.minecraftClient.options.jumpKey.isPressed();
        if (bl) {
            this.placementActive = true;
        }
        return bl;
    }

    private ModeSetting.Option getPlacementModeOption() {
        return this.isBlockFeatureReady() ? this.normal : this.technique.getSelectedOption();
    }

    private boolean isPlacementPlayerReady() {
        Box HorizontalFacingBlock = Scaffold.minecraftClient.player.getBoundingBox().expand(0.5, 0.0, 0.5).offset(0.0, -1.05, 0.0);
        return this.isBoundingBoxValid(HorizontalFacingBlock);
    }

    private boolean isDistanceValid(double d) {
        if (Scaffold.minecraftClient.player == null || Scaffold.minecraftClient.world == null) {
            return false;
        }
        Box HorizontalFacingBlock = Scaffold.minecraftClient.player.getBoundingBox();
        return !this.isBoundingBoxValid(HorizontalFacingBlock.offset(d, -0.05, 0.0)) || !this.isBoundingBoxValid(HorizontalFacingBlock.offset(-d, -0.05, 0.0)) || !this.isBoundingBoxValid(HorizontalFacingBlock.offset(0.0, -0.05, d)) || !this.isBoundingBoxValid(HorizontalFacingBlock.offset(0.0, -0.05, -d));
    }

    private boolean isContextDistanceValid(PlacementContext placementContext, double d) {
        Vec3d VanillaChestLootTableGenerator = placementContext.getMovementDirection(Scaffold.minecraftClient.player.getYaw());
        if (VanillaChestLootTableGenerator.lengthSquared() < 1.0E-6) {
            return this.isDistanceValid(d);
        }
        Box HorizontalFacingBlock = Scaffold.minecraftClient.player.getBoundingBox().offset(VanillaChestLootTableGenerator.x * d, -0.05, VanillaChestLootTableGenerator.z * d);
        return !this.isBoundingBoxValid(HorizontalFacingBlock);
    }

    private boolean isBoundingBoxValid(Box HorizontalFacingBlock) {
        double d = (double)Scaffold.minecraftClient.player.getWidth() / 6.0;
        double d2 = (HorizontalFacingBlock.minX + HorizontalFacingBlock.maxX) * 0.5;
        double d3 = (HorizontalFacingBlock.minZ + HorizontalFacingBlock.maxZ) * 0.5;
        return this.isDistanceRangeValid(HorizontalFacingBlock.minX + d, HorizontalFacingBlock.minZ + d) || this.isDistanceRangeValid(d2, HorizontalFacingBlock.minZ + d) || this.isDistanceRangeValid(HorizontalFacingBlock.maxX - d, HorizontalFacingBlock.minZ + d) || this.isDistanceRangeValid(HorizontalFacingBlock.minX + d, d3) || this.isDistanceRangeValid(d2, d3) || this.isDistanceRangeValid(HorizontalFacingBlock.maxX - d, d3) || this.isDistanceRangeValid(HorizontalFacingBlock.minX + d, HorizontalFacingBlock.maxZ - d) || this.isDistanceRangeValid(d2, HorizontalFacingBlock.maxZ - d) || this.isDistanceRangeValid(HorizontalFacingBlock.maxX - d, HorizontalFacingBlock.maxZ - d);
    }

    private boolean isDistanceRangeValid(double d, double d2) {
        BlockPos adminsky = BlockPos.ofFloored((double)d, (double)(Scaffold.minecraftClient.player.getBoundingBox().minY - 0.001), (double)d2);
        BlockState class_26802 = Scaffold.minecraftClient.world.getBlockState(adminsky);
        return !class_26802.isAir() && !class_26802.getCollisionShape((BlockView)Scaffold.minecraftClient.world, adminsky).isEmpty();
    }

    private void updatePlacementDistance(double d) {
        if (Scaffold.minecraftClient.player == null) {
            return;
        }
        double d2 = Scaffold.minecraftClient.player.input.movementForward;
        double d3 = Scaffold.minecraftClient.player.input.movementSideways;
        float f = Scaffold.minecraftClient.player.getYaw();
        if (d2 == 0.0 && d3 == 0.0) {
            return;
        }
        if (d2 != 0.0) {
            if (d3 > 0.0) {
                f += d2 > 0.0 ? -45.0f : 45.0f;
            } else if (d3 < 0.0) {
                f += d2 > 0.0 ? 45.0f : -45.0f;
            }
            d3 = 0.0;
            d2 = d2 > 0.0 ? 1.0 : -1.0;
        }
        double d4 = Math.sin(Math.toRadians((double)f + 90.0));
        double d5 = Math.cos(Math.toRadians((double)f + 90.0));
        double d6 = d2 * d * d5 + d3 * d * d4;
        double d7 = d2 * d * d4 - d3 * d * d5;
        Scaffold.minecraftClient.player.setVelocity(d6, Scaffold.minecraftClient.player.getVelocity().y, d7);
    }

    private boolean isPlacementTargetReady() {
        return Scaffold.minecraftClient.player != null && (Scaffold.minecraftClient.player.input.movementForward != 0.0f || Scaffold.minecraftClient.player.input.movementSideways != 0.0f);
    }

    double getTargetDistance() {
        Vec3d VanillaChestLootTableGenerator = Scaffold.minecraftClient.player.getVelocity();
        return Math.hypot(VanillaChestLootTableGenerator.x, VanillaChestLootTableGenerator.z);
    }

    private float calculateContextWeight(PlacementContext placementContext) {
        double d = placementContext.getForwardAxis();
        double d2 = placementContext.getStrafeAxis();
        if (d == 0.0 && d2 == 0.0) {
            return Scaffold.minecraftClient.player.getYaw();
        }
        return MathHelper.wrapDegrees((float)((float)Math.toDegrees(EntityUtils.getDirectionRadians(Scaffold.minecraftClient.player.getYaw(), d, d2))));
    }

    private PlacementContext createPlacementContext(Vec3d VanillaChestLootTableGenerator, float f) {
        if (VanillaChestLootTableGenerator.lengthSquared() < 1.0E-6) {
            return PlacementContext.NO_INPUT;
        }
        double d = Math.toRadians(f);
        double d2 = Math.sin(d);
        double d3 = Math.cos(d);
        double d4 = VanillaChestLootTableGenerator.x;
        double d5 = VanillaChestLootTableGenerator.z;
        double d6 = d5 * d3 - d4 * d2;
        double d7 = d4 * d3 + d5 * d2;
        double d8 = Math.max(Math.abs(d6), Math.abs(d7));
        if (d8 > 1.0) {
            d6 /= d8;
            d7 /= d8;
        }
        return PlacementContext.fromAxes((float)d6, (float)d7);
    }

    private Vec3d getPredictedPosition(float f) {
        double d = Math.toRadians(f);
        return new Vec3d(-Math.sin(d), 0.0, Math.cos(d)).normalize();
    }

    private Direction getPlacementDirection(float f) {
        int n = MathHelper.floor((double)((double)(MathHelper.wrapDegrees((float)f) / 90.0f) + 0.5)) & 3;
        return switch (n) {
            case 0 -> Direction.SOUTH;
            case 1 -> Direction.WEST;
            case 2 -> Direction.NORTH;
            default -> Direction.EAST;
        };
    }

    private float calculateMovementScale(float f) {
        return (float)Math.round(f / 45.0f) * 45.0f;
    }

    private void processPlacementSlot(int n) {
        if (n >= 0 && n < 9 && Scaffold.minecraftClient.player.getInventory().selectedSlot != n) {
            Scaffold.minecraftClient.player.getInventory().selectedSlot = n;
        }
    }

    private long getLastRotationTime() {
        return (long)this.getRangeValue(this.delay) * 50L;
    }

    private int getRangeValue(RangeSetting rangeSetting) {
        int n = Math.round(Math.min(rangeSetting.getFirstValue(), rangeSetting.getSecondValue()));
        int n2 = Math.round(Math.max(rangeSetting.getFirstValue(), rangeSetting.getSecondValue()));
        if (n2 <= n) {
            return n;
        }
        return ThreadLocalRandom.current().nextInt(n, n2 + 1);
    }

    private float getRangeProgress(RangeSetting rangeSetting) {
        float f = Math.min(rangeSetting.getFirstValue(), rangeSetting.getSecondValue());
        float f2 = Math.max(rangeSetting.getFirstValue(), rangeSetting.getSecondValue());
        if (f2 <= f) {
            return f;
        }
        return (float)this.calculateDistance(f, f2);
    }

    private double calculateDistance(double d, double d2) {
        return ThreadLocalRandom.current().nextDouble(d, d2);
    }

    private Vec3d interpolateOffsetPosition(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, double d) {
        return new Vec3d(MathHelper.lerp((double)d, (double)VanillaChestLootTableGenerator.x, (double)WallPlayerSkullBlock.x), MathHelper.lerp((double)d, (double)VanillaChestLootTableGenerator.y, (double)WallPlayerSkullBlock.y), MathHelper.lerp((double)d, (double)VanillaChestLootTableGenerator.z, (double)WallPlayerSkullBlock.z));
    }

    private Vec3d offsetPosition(Vec3d VanillaChestLootTableGenerator, float f) {
        double d = Math.cos(f);
        double d2 = Math.sin(f);
        return new Vec3d(VanillaChestLootTableGenerator.x * d + VanillaChestLootTableGenerator.z * d2, VanillaChestLootTableGenerator.y, VanillaChestLootTableGenerator.z * d - VanillaChestLootTableGenerator.x * d2);
    }

    private Vec3d getBlockCenter(BlockPos adminsky) {
        return new Vec3d((double)adminsky.getX() + 0.5, (double)adminsky.getY() + 0.5, (double)adminsky.getZ() + 0.5);
    }

    private Vec3d getBlockEdgePosition(BlockPos adminsky) {
        return new Vec3d((double)adminsky.getX() + 0.5, (double)adminsky.getY(), (double)adminsky.getZ() + 0.5);
    }

    private double calculateDistanceBetween(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock) {
        double d = VanillaChestLootTableGenerator.x - WallPlayerSkullBlock.x;
        double d2 = VanillaChestLootTableGenerator.z - WallPlayerSkullBlock.z;
        return d * d + d2 * d2;
    }






}

