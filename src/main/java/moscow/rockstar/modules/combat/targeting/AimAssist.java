/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Hand
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Item
 *  net.minecraft.ItemStack
 *  net.minecraft.Vec3d
 *  net.minecraft.Text
 *  net.minecraft.MathHelper
 *  net.minecraft.RegistryEntry$Reference
 */
package moscow.rockstar.modules.combat.targeting;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import moscow.rockstar.combat.AimRotation;
import moscow.rockstar.combat.RotationController;
import moscow.rockstar.combat.RotationSolver;
import moscow.rockstar.core.FriendManager;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.core.TargetFilter;
import moscow.rockstar.entity.EntityProcessor;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.movement.MovementState;
import moscow.rockstar.movement.MovementUtils;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.registry.entry.RegistryEntry;
import pyrock.events.game.AttackEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.render.GameRendererEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Aim Assist", category=ModuleCategory.COMBAT, description="modules.descriptions.aim_assist")
public class AimAssist
extends Module {
    private static final Set<String> candidateTargets = Set.of("sword", "trident", "_axe", "mace", "stick", "pickaxe", "shovel");
    private static final double TWO_PI = Math.PI * 2;
    private static final float MIN_ANGLE_DELTA = 1.0E-4f;
    private static final float MIN_SMOOTHING = 0.05f;
    private static final float MAX_FOV = 90.0f;
    private ModeSetting mode;
    private ModeSetting.Option normalMode;
    private ModeSetting.Option neuroMode;
    private NumberSetting neuroStrength;
    private MultiBooleanSetting targets;
    private MultiBooleanSetting.Option players;
    private MultiBooleanSetting.Option animals;
    private MultiBooleanSetting.Option mobs;
    private MultiBooleanSetting.Option invisibles;
    private MultiBooleanSetting.Option nakedPlayers;
    private MultiBooleanSetting.Option rockUsers;
    private MultiBooleanSetting.Option friends;
    private ModeSetting targetSortMode;
    private ModeSetting.Option distanceSort;
    private ModeSetting.Option healthSort;
    private ModeSetting.Option fovSort;
    private ModeSetting targetLockMode;
    private ModeSetting.Option lockOff;
    private ModeSetting.Option lockOnAttack;
    private ModeSetting.Option lockAutomatic;
    private NumberSetting lockTimeout;
    private NumberSetting aimDistance;
    private NumberSetting strength;
    private NumberSetting aimSpeed;
    private BooleanSetting repitAim;
    private BooleanSetting enableVertical;
    private NumberSetting verticalFactor;
    private NumberSetting motorNoise;
    private BooleanSetting onlyOnWeapon;
    private BooleanSetting yieldToMouse;
    private BooleanSetting inputBased;
    private NumberSetting inputThreshold;
    private NumberSetting predictionTicks;
    private NumberSetting veloPrTicks;
    private NumberSetting predictionChance;
    private BooleanSetting multipoint;
    private BooleanSetting mpAdaptive;
    private NumberSetting mpCount;
    private NumberSetting mpSpread;
    private NumberSetting regen;
    private BooleanSetting overshoot;
    private NumberSetting overshootChance;
    private final MovementState movementState = new MovementState();
    private final RotationSolver rotationSolver = new RotationSolver();
    private final MovementUtils movementUtils = new MovementUtils();
    private final float[] aimVector = new float[2];
    private float currentYaw;
    private float currentPitch;
    private float previousYaw;
    private boolean isAiming;
    private LivingEntity targetEntity;
    private LivingEntity previousTarget;
    private long targetLockStartedAt;
    private long lastAimAt;
    private long lastAttackAt;
    private boolean overlayVisible = true;
    private boolean privilegedMode;
    private float targetYaw;
    private float targetPitch;
    private float smoothedYaw;
    private float smoothedPitch;
    private float desiredYaw;
    private float desiredPitch;
    private float previousTargetYaw;
    private float previousTargetPitch = Float.NaN;
    private float noiseYaw;
    private float noisePitch;
    private float predictionYaw;
    private float predictionPitch;
    private float interpolationProgress;
    private double targetDistance;
    private double aimAngle;
    private double fovRadians;
    private boolean disableLocked;
    private double rawYaw;
    private double clampedYaw;
    private double normalizedYaw;
    private float projectedYaw;
    private float adjustedYaw;
    private float predictedYaw;
    private float serverYaw;
    private float clientYaw;
    private float localYaw;
    private float remoteYaw;
    private float visualYaw;
    private float logicalYaw;
    private boolean alwaysEnabled;
    private float horizontalDelta;
    private float verticalDelta;
    private float forwardDelta;
    private float backwardDelta;
    private float leftDelta;
    private float rightDelta;
    private float topDelta;
    private float bottomDelta;
    private float innerOffset;
    private float outerOffset;
    private float baseOffset;
    private boolean targetSelected;
    private float extraSmoothing;
    private float activeBlend;
    private float passiveBlend;
    private float sourceAngle;
    private long lastPredictionAt;
    private float targetWeight = 0.15f;
    private boolean predictionActive;
    private float originAngle;
    private final float[] destinationAngles = new float[2];
    private final float[] pendingAngles = new float[2];
    private final float[] completedAngles = new float[2];
    private final double[] multipointOffsets = new double[3];
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEvent = clientPlayerTickEvent -> {
        if (this.isCompletionReady()) {
            this.resetTargetState();
            return;
        }
        if (this.onlyOnWeapon.isEnabled() && !this.isCurrentTargetReady()) {
            this.resetTargetState();
            return;
        }
        this.finishTargeting();
        this.restorePreviousAim();
        this.resetTracking();
        if (this.isNeuroModeSelected()) {
            this.resetRotationState();
            return;
        }
        if (this.targetEntity != null) {
            Vec3d VanillaChestLootTableGenerator = this.targetEntity.getVelocity();
            if (VanillaChestLootTableGenerator.lengthSquared() > 1.0E-6) {
                this.movementState.recordSample(this.targetEntity, this.getAimStrength(), 100);
            }
            if (this.repitAim.isEnabled() && !this.privilegedMode) {
                this.targetPitch += 0.05f;
                if (this.targetPitch >= this.smoothedYaw) {
                    this.privilegedMode = true;
                }
            }
        }
    };
    private final EventListener<GameRendererEvent> onGameRendererEvent = gameRendererEvent -> {
        float f;
        float f2;
        float f3;
        float f4;
        float f5;
        float f6;
        float f7;
        float f8;
        if (this.isCompletionReady() || AimAssist.minecraftClient.player == null) {
            return;
        }
        long l = System.nanoTime();
        float f9 = this.calculateLockProgress(l);
        this.lastAttackAt = l;
        if (f9 < 1.0E-4f || f9 > 0.1f) {
            f9 = 0.016666668f;
        }
        if (this.isNeuroModeSelected()) {
            this.applyAimDelta(f9);
            return;
        }
        boolean bl = this.yieldToMouse.isEnabled();
        if (bl) {
            this.resetStateFallback();
        }
        this.interpolationProgress = 0.0f;
        this.predictionPitch = 0.0f;
        if (this.isFallbackReady()) {
            this.resetStateAlternate();
            return;
        }
        Vec3d VanillaChestLootTableGenerator = AimAssist.minecraftClient.player.getEyePos();
        this.rawYaw = VanillaChestLootTableGenerator.x;
        this.clampedYaw = VanillaChestLootTableGenerator.y;
        this.normalizedYaw = VanillaChestLootTableGenerator.z;
        float f10 = this.getAimStrength();
        this.updateTargetAim(this.targetEntity, f10, f9);
        float f11 = this.completedAngles[0];
        float f12 = this.completedAngles[1];
        if (!Float.isFinite(f11) || !Float.isFinite(f12)) {
            this.resetStateAlternate();
            return;
        }
        float f13 = f11 * f11 + f12 * f12;
        if (f13 < 0.01f) {
            this.resetStateAlternate();
            return;
        }
        float f14 = MathHelper.sqrt((float)f13);
        if (this.enableVertical.isEnabled()) {
            f8 = this.calculateSmoothStep(AimAssist.minecraftClient.player.distanceTo((Entity)this.targetEntity));
            f12 += f8;
        }
        f8 = this.calculateRotation(this.targetEntity);
        if (this.repitAim.isEnabled()) {
            f7 = this.calculateRotation(f14, f8);
            f6 = Math.max(f7 / this.aimSpeed.getValue(), 0.01f);
            this.updatePredictedAngles(f14, f8, f9, f6);
            f5 = this.overlayVisible ? this.calculateMagnitude(this.targetYaw) : this.calculateMagnitude(f11, f12);
            f4 = f14 / f6 * f5 * this.strength.getValue();
            f3 = f14 > 1.0E-4f ? 1.0f / f14 : 0.0f;
            f2 = MathHelper.clamp((float)(f11 * f3 * f4 * f9), (float)-20.0f, (float)20.0f);
            f = this.enableVertical.isEnabled() ? this.calculateMagnitude(f12, f14, f8, f9) : 0.0f;
        } else {
            f7 = this.aimSpeed.getValue() * this.strength.getValue() * 10.0f * f9;
            f6 = Math.min(f14 / 10.0f, 1.0f);
            f5 = f14 > 1.0E-4f ? 1.0f / f14 : 0.0f;
            f2 = f11 * f5 * f7 * f6;
            f = this.enableVertical.isEnabled() ? f12 * f5 * f7 * f6 * this.verticalFactor.getValue() : 0.0f;
        }
        f7 = this.motorNoise.getValue();
        f6 = this.interpolateAngle(f14, f9);
        f5 = f7 * f6;
        if (f5 > 1.0E-4f) {
            this.updateRotationState(f2, f, f5, f9);
            f4 = this.calculateMouseDelta(f9);
            f2 += this.destinationAngles[0] + f4 * 0.5f;
            f += this.destinationAngles[1] + f4 * 0.25f;
        }
        if (this.overshoot.isEnabled()) {
            this.updateTargetAngles(f14, f9);
            f2 += this.extraSmoothing;
            f += this.activeBlend;
        }
        if (bl) {
            f2 = this.calculateMagnitude(f2, this.noisePitch, f9);
            f = this.calculateMagnitude(f, this.predictionYaw, f9);
            if (this.inputBased.isEnabled() && !this.isAngleWithinLimit(f11, f12, f9)) {
                f = 0.0f;
                f2 = 0.0f;
            }
        }
        f4 = MathHelper.clamp((float)(1.0f - (float)Math.exp(-20.0f * f9)), (float)0.05f, (float)0.95f);
        this.smoothedPitch = this.calculateWeightedAngle(this.smoothedPitch, f2, f4);
        this.desiredYaw = this.calculateWeightedAngle(this.desiredYaw, f, f4 * 0.75f);
        f3 = Math.abs(f11) * 1.5f + 0.5f;
        float f15 = Math.abs(f12) * 1.5f + 0.5f;
        this.smoothedPitch = MathHelper.clamp((float)this.smoothedPitch, (float)(-f3), (float)f3);
        this.desiredYaw = MathHelper.clamp((float)this.desiredYaw, (float)(-f15), (float)f15);
        this.resetPredictionState();
        this.resetStateAlternate();
    };
    private final EventListener<AttackEvent> onAttackEvent = attackEvent -> {
        LivingEntity class_13092;
        Entity class_12972;
        if (this.isNeuroModeSelected() && (class_12972 = attackEvent.getEntity()) instanceof LivingEntity && (class_13092 = (LivingEntity)class_12972) != AimAssist.minecraftClient.player) {
            this.movementUtils.enableAimAssist();
        }
    };

    public AimAssist() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.mode = new ModeSetting(this, "aimassist.mode");
        this.normalMode = new ModeSetting.Option(this.mode, "aimassist.mode_normal").select();
        this.neuroMode = new ModeSetting.Option(this.mode, "aimassist.mode_neuro");
        this.neuroStrength = new NumberSetting(this, "aimassist.neuro_strength", "aimassist.neuro_strength.desc", () -> !this.isNeuroModeSelected()).setMinValue(0.1f).setMaxValue(1.0f).setStep(0.05f).setValue(1.0f);
        this.targets = new MultiBooleanSetting(this, "modules.settings.aura.targets");
        this.players = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.players").select();
        this.animals = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.animals").select();
        this.mobs = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.mobs").select();
        this.invisibles = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.invisibles").select();
        this.nakedPlayers = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.nakedPlayers").select();
        this.rockUsers = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.rockUsers");
        this.friends = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.friends");
        this.targetSortMode = new ModeSetting(this, "aura.targets_sort");
        this.distanceSort = new ModeSetting.Option(this.targetSortMode, "aura.ts_dist");
        this.healthSort = new ModeSetting.Option(this.targetSortMode, "aura.ts_health");
        this.fovSort = new ModeSetting.Option(this.targetSortMode, "aura.ts_fov").select();
        this.targetLockMode = new ModeSetting((SettingOwner)this, "aimassist.target_lock_mode", this::isNeuroModeSelected);
        this.lockOff = new ModeSetting.Option(this.targetLockMode, "aimassist.lock_off");
        this.lockOnAttack = new ModeSetting.Option(this.targetLockMode, "aimassist.lock_on_attack");
        this.lockAutomatic = new ModeSetting.Option(this.targetLockMode, "aimassist.lock_auto").select();
        this.lockTimeout = new NumberSetting((SettingOwner)this, "aimassist.lock_timeout", this::isNeuroModeSelected).setMinValue(0.0f).setMaxValue(10.0f).setStep(0.1f).setValue(5.0f);
        this.aimDistance = new NumberSetting(this, "modules.settings.aura.aimDistance").setMinValue(3.0f).setMaxValue(10.0f).setStep(0.1f).setValue(4.5f);
        this.strength = new NumberSetting((SettingOwner)this, "aimassist.strength", this::isNeuroModeSelected).setMinValue(0.1f).setMaxValue(2.0f).setStep(0.01f).setValue(1.0f);
        this.aimSpeed = new NumberSetting((SettingOwner)this, "aimassist.aim_speed", this::isNeuroModeSelected).setMinValue(1.0f).setMaxValue(60.0f).setStep(0.5f).setValue(18.0f);
        this.repitAim = new BooleanSetting((SettingOwner)this, "aimassist.repit_aim", this::isNeuroModeSelected).setActiveExtra(false);
        this.enableVertical = new BooleanSetting((SettingOwner)this, "aimassist.enable_vertical", this::isNeuroModeSelected).enable();
        this.verticalFactor = new NumberSetting((SettingOwner)this, "projectile.vertical_factor", this::isModeReady).setMinValue(0.1f).setMaxValue(1.5f).setStep(0.01f).setValue(0.4f);
        this.motorNoise = new NumberSetting(this, "aimassist.motor_noise", "motornoise.desc", this::isNeuroModeSelected).setMinValue(0.0f).setMaxValue(0.4f).setStep(0.01f).setValue(0.15f);
        this.onlyOnWeapon = new BooleanSetting(this, "aimassist.only_on_weapon").enable();
        this.yieldToMouse = new BooleanSetting((SettingOwner)this, "aimassist.yield_to_mouse", this::isNeuroModeSelected).setActiveExtra(false);
        this.inputBased = new BooleanSetting((SettingOwner)this, "aimassist.input_based", this::isPredictionReady).setActiveExtra(false);
        this.inputThreshold = new NumberSetting((SettingOwner)this, "aimassist.input_threshold", this::isAimReady).setMinValue(0.1f).setMaxValue(3.0f).setStep(0.1f).setValue(0.8f);
        this.predictionTicks = new NumberSetting((SettingOwner)this, "aimassist.prediction_ticks", this::isNeuroModeSelected).setMinValue(0.0f).setMaxValue(3.0f).setStep(1.0f).setValue(2.0f);
        this.veloPrTicks = new NumberSetting((SettingOwner)this, "aimassist.velo_pr_ticks", this::isNeuroModeSelected).setMinValue(0.0f).setMaxValue(3.0f).setStep(1.0f).setValue(1.0f);
        this.predictionChance = new NumberSetting(this, "aimassist.prediction_chance", "aimassist.prediction_chance.desc", this::isNeuroModeSelected).setMinValue(0.0f).setMaxValue(100.0f).setStep(5.0f).setValue(65.0f).setUnit(" %");
        this.multipoint = new BooleanSetting((SettingOwner)this, "aimassist.multipoint", () -> this.isNeuroModeSelected() || !this.yieldToMouse.isEnabled()).setActiveExtra(false);
        this.mpAdaptive = new BooleanSetting((SettingOwner)this, "aimassist.mp_adaptive", this::isTargetingReady).enable();
        this.mpCount = new NumberSetting((SettingOwner)this, "aimassist.mp_count", this::isLockReady).setMinValue(3.0f).setMaxValue(50.0f).setStep(1.0f).setValue(8.0f);
        this.mpSpread = new NumberSetting((SettingOwner)this, "aimassist.mp_spread", this::isLockReady).setMinValue(0.2f).setMaxValue(2.0f).setStep(0.05f).setValue(0.7f);
        this.regen = new NumberSetting((SettingOwner)this, "aimassist.regen", this::isTargetingReady).setMinValue(0.005f).setMaxValue(0.2f).setStep(0.005f).setValue(0.025f);
        this.overshoot = new BooleanSetting(this, "aimassist.overshoot", "aimassist.overshoot.desc", this::isNeuroModeSelected).setActiveExtra(true);
        this.overshootChance = new NumberSetting(this, "aimassist.overshoot_chance", "aimassist.overshoot_chance.desc", () -> this.isNeuroModeSelected() || !this.overshoot.isEnabled()).setMinValue(0.0f).setMaxValue(10.0f).setStep(1.0f).setValue(3.0f).setUnit(" %");
    }

    private boolean isNeuroModeSelected() {
        return this.mode.isSelected(this.neuroMode);
    }

    private boolean isModeReady() {
        return this.isNeuroModeSelected() || !this.enableVertical.isEnabled();
    }

    private boolean isTargetingReady() {
        return this.isNeuroModeSelected() || !this.multipoint.isEnabled() || !this.yieldToMouse.isEnabled();
    }

    private boolean isLockReady() {
        return this.isTargetingReady() || this.mpAdaptive.isEnabled();
    }

    private boolean isInputReady() {
        return this.targetLockMode.isSelected(this.lockOff);
    }

    private boolean isPredictionReady() {
        return this.isNeuroModeSelected() || !this.yieldToMouse.isEnabled();
    }

    private boolean isAimReady() {
        return this.isPredictionReady() || !this.inputBased.isEnabled();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.isAiming = false;
        this.clearAimState();
        this.resetTargetState();
        this.movementState.resetSamples();
        this.rotationSolver.reset();
        FriendManager friendManager = RockstarClient.create().getFriendManager();
        if (friendManager != null) {
            friendManager.clearTarget();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.resetTargetState();
        this.movementState.resetSamples();
        this.rotationSolver.reset();
        FriendManager friendManager = RockstarClient.create().getFriendManager();
        if (friendManager != null) {
            friendManager.clearTarget();
        }
    }

    private void clearAimState() {
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        this.horizontalDelta = 0.1f + threadLocalRandom.nextFloat() * 0.5f;
        this.verticalDelta = (0.5f + threadLocalRandom.nextFloat() * 0.75f) * 2.0f;
        this.forwardDelta = 0.5f + threadLocalRandom.nextFloat() * 0.5f;
        this.backwardDelta = 3.0f + threadLocalRandom.nextFloat() * 0.25f;
        this.leftDelta = 0.5f + threadLocalRandom.nextFloat() * 0.7f;
        this.rightDelta = 0.08f + threadLocalRandom.nextFloat() * 0.12f;
        this.topDelta = 0.018f + threadLocalRandom.nextFloat() * 0.005f;
        this.bottomDelta = 0.45f + threadLocalRandom.nextFloat() * 0.05f;
        this.innerOffset = 2.0f + threadLocalRandom.nextFloat();
        this.outerOffset = 8.0f + threadLocalRandom.nextFloat() * 3.0f;
        this.baseOffset = 0.15f + threadLocalRandom.nextFloat() * 0.05f;
        this.visualYaw = MathUtils.interpolateRandomStrategy(this.forwardDelta, this.backwardDelta);
        this.predictedYaw = 0.3f + threadLocalRandom.nextFloat() * 0.4f;
        this.serverYaw = 0.2f + threadLocalRandom.nextFloat() * 0.3f;
        this.clientYaw = 0.04f + threadLocalRandom.nextFloat() * 0.06f;
        this.rotationSolver.setBaseJitterAmount(0.05f + MathUtils.interpolateRandomStrategy(-0.03f, 0.15f));
        this.targetWeight = this.regen.getValue();
    }

    private void resetTargetState() {
        this.previousTarget = null;
        this.targetEntity = null;
        this.targetYaw = 0.0f;
        this.smoothedYaw = 0.0f;
        this.targetPitch = 0.0f;
        this.privilegedMode = false;
        this.disableLocked = false;
        this.overlayVisible = true;
        this.desiredYaw = 0.0f;
        this.smoothedPitch = 0.0f;
        this.previousTargetYaw = 0.0f;
        this.desiredPitch = 0.0f;
        this.previousTargetPitch = Float.NaN;
        this.noiseYaw = 0.0f;
        this.predictionYaw = 0.0f;
        this.noisePitch = 0.0f;
        this.interpolationProgress = 0.0f;
        this.predictionPitch = 0.0f;
        this.logicalYaw = 0.0f;
        this.localYaw = 0.0f;
        this.lastAttackAt = 0L;
        this.targetLockStartedAt = 0L;
        this.lastAimAt = 0L;
        this.fovRadians = 0.0;
        this.aimAngle = 0.0;
        this.targetDistance = 0.0;
        this.lastPredictionAt = 0L;
        this.predictionActive = false;
        this.targetSelected = false;
        this.activeBlend = 0.0f;
        this.extraSmoothing = 0.0f;
        this.sourceAngle = 0.0f;
        this.passiveBlend = 0.0f;
        this.adjustedYaw = 0.0f;
        this.projectedYaw = 0.0f;
        this.destinationAngles[1] = 0.0f;
        this.destinationAngles[0] = 0.0f;
        this.movementUtils.resetAimAssist();
        this.currentPitch = 0.0f;
        this.currentYaw = 0.0f;
        this.previousYaw = 0.0f;
    }

    private void resetRotationState() {
        if (this.targetEntity == null || AimAssist.minecraftClient.player == null || AimAssist.minecraftClient.world == null) {
            this.movementUtils.resetAimAssist();
            this.currentPitch = 0.0f;
            this.currentYaw = 0.0f;
            return;
        }
        if (!this.movementUtils.isDataClientAvailable()) {
            if (!this.isAiming) {
                this.isAiming = true;
                Notification.error(Text.of((String)"Aim Assist: \u043c\u043e\u0434\u0435\u043b\u044c \u043d\u0435\u0439\u0440\u043e \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430 \u2014 \u043e\u0431\u0443\u0447\u0438 \u0435\u0451 \u0447\u0435\u0440\u0435\u0437 .neuro"));
            }
            return;
        }
        if (!this.movementUtils.calculateAimCorrection(AimAssist.minecraftClient.player, AimAssist.minecraftClient.world, this.targetEntity, RotationController.getModelSmoothing(), this.aimVector)) {
            return;
        }
        float f = this.neuroStrength.getValue();
        this.currentYaw = MathHelper.clamp((float)(this.currentYaw + this.aimVector[0] * f), (float)-90.0f, (float)90.0f);
        this.currentPitch = MathHelper.clamp((float)(this.currentPitch + this.aimVector[1] * f), (float)-90.0f, (float)90.0f);
        this.previousYaw = 0.0f;
    }

    private void applyAimDelta(float f) {
        if (AimAssist.minecraftClient.player == null) {
            return;
        }
        if (Math.abs(this.currentYaw) < 1.0E-4f && Math.abs(this.currentPitch) < 1.0E-4f) {
            this.resetStateAlternate();
            return;
        }
        this.previousYaw += f;
        float f2 = Math.max(0.05f - (this.previousYaw - f), f);
        float f3 = MathHelper.clamp((float)(f / f2), (float)0.0f, (float)1.0f);
        float f4 = this.currentYaw * f3;
        float f5 = this.currentPitch * f3;
        this.currentYaw -= f4;
        this.currentPitch -= f5;
        this.updateRotation(f4, f5);
        this.resetStateAlternate();
    }

    private void updateTargetAim(LivingEntity class_13092, float f, float f2) {
        double d;
        double d2;
        float f3;
        double d3;
        Vec3d VanillaChestLootTableGenerator;
        if (class_13092 == null || AimAssist.minecraftClient.player == null) {
            this.completedAngles[1] = 0.0f;
            this.completedAngles[0] = 0.0f;
            return;
        }
        double d4 = MathHelper.lerp((double)f, (double)class_13092.lastRenderX, (double)class_13092.getX());
        double d5 = MathHelper.lerp((double)f, (double)class_13092.lastRenderY, (double)class_13092.getY());
        double d6 = MathHelper.lerp((double)f, (double)class_13092.lastRenderZ, (double)class_13092.getZ());
        int n = (int)this.predictionTicks.getValue();
        if (n > 0 && ThreadLocalRandom.current().nextFloat() < this.predictionChance.getValue() && (VanillaChestLootTableGenerator = this.movementState.getLatestVelocity()) != null && (d3 = VanillaChestLootTableGenerator.lengthSquared()) > 1.0E-6) {
            d4 += VanillaChestLootTableGenerator.x * (double)n;
            d5 += VanillaChestLootTableGenerator.y * (double)n;
            d6 += VanillaChestLootTableGenerator.z * (double)n;
        }
        if ((f3 = AimAssist.minecraftClient.player.getAttackCooldownProgress(1.5f)) > 0.85f && !AimAssist.minecraftClient.player.isUsingItem() && (d2 = (d3 = class_13092.getX() - this.rawYaw) * d3 + (d = class_13092.getZ() - this.normalizedYaw) * d) < 12.25 && d2 > 2.25) {
            float f4 = (f3 - 0.85f) / 0.15f;
            f4 *= f4;
            AimRotation.calculateAimOffset(this.multipointOffsets, this.rawYaw, this.normalizedYaw, AimAssist.minecraftClient.player.getYaw(), class_13092, AimAssist.minecraftClient.player.isSprinting(), AimRotation.getAttributeAsFloat((LivingEntity)AimAssist.minecraftClient.player), (int)this.veloPrTicks.getValue());
            d4 -= this.multipointOffsets[0] * (double)f4;
            d5 -= this.multipointOffsets[1] * (double)f4;
            d6 -= this.multipointOffsets[2] * (double)f4;
        }
        if (this.multipoint.isEnabled() && this.yieldToMouse.isEnabled()) {
            this.applyTargetRotation(class_13092, d4, d5, d6);
            double[] dArray = this.rotationSolver.getSelectedAimCoordinates();
            if (dArray != null) {
                d3 = dArray[0];
                d = dArray[1];
                d2 = dArray[2];
            } else {
                d3 = d4;
                d = d5 + (double)(class_13092.getHeight() * 0.5f);
                d2 = d6;
            }
        } else {
            d3 = d4;
        }
        d = d5 + (double)(class_13092.getHeight() * 0.5f);
        d2 = d6;
        if (!this.disableLocked) {
            this.targetDistance = d3;
            this.aimAngle = d;
            this.fovRadians = d2;
            this.disableLocked = true;
        } else {
            double d7 = MathHelper.clamp((double)(1.0 - Math.exp(-25.0 * (double)f2)), (double)0.05, (double)0.95);
            this.targetDistance += (d3 - this.targetDistance) * d7;
            this.aimAngle += (d - this.aimAngle) * d7;
            this.fovRadians += (d2 - this.fovRadians) * d7;
        }
        double d8 = this.targetDistance - this.rawYaw;
        double d9 = this.aimAngle - this.clampedYaw;
        double d10 = this.fovRadians - this.normalizedYaw;
        double d11 = d8 * d8 + d10 * d10;
        double d12 = Math.sqrt(d11);
        if (d12 < (double)1.0E-4f && Math.abs(d9) < (double)1.0E-4f) {
            this.completedAngles[1] = 0.0f;
            this.completedAngles[0] = 0.0f;
            return;
        }
        float f5 = (float)Math.toDegrees(Math.atan2(d10, d8)) - 90.0f;
        float f6 = MathHelper.clamp((float)((float)(-Math.toDegrees(Math.atan2(d9, d12)))), (float)-90.0f, (float)90.0f);
        this.completedAngles[0] = MathHelper.wrapDegrees((float)(f5 - AimAssist.minecraftClient.player.getYaw()));
        this.completedAngles[1] = f6 - AimAssist.minecraftClient.player.getPitch();
    }

    private void resetPredictionState() {
        this.updateRotation(this.smoothedPitch, this.desiredYaw);
    }

    private void updateRotation(float f, float f2) {
        if (AimAssist.minecraftClient.player == null) {
            return;
        }
        this.updateAimAngles(f, f2);
        float f3 = Float.isFinite(this.pendingAngles[0]) ? this.pendingAngles[0] : 0.0f;
        float f4 = Float.isFinite(this.pendingAngles[1]) ? this.pendingAngles[1] : 0.0f;
        float f5 = AimAssist.minecraftClient.player.getYaw() + f3;
        float f6 = MathHelper.clamp((float)(AimAssist.minecraftClient.player.getPitch() + f4), (float)-90.0f, (float)90.0f);
        AimAssist.minecraftClient.player.setYaw(f5);
        AimAssist.minecraftClient.player.setPitch(f6);
        AimAssist.minecraftClient.player.headYaw = f5;
        this.predictionPitch = f3;
        this.interpolationProgress = f4;
    }

    private void updateAimAngles(float f, float f2) {
        float f3 = this.getAimProgress();
        if (f3 < 1.0E-4f) {
            this.pendingAngles[0] = f;
            this.pendingAngles[1] = f2;
            return;
        }
        this.desiredPitch += f;
        this.previousTargetYaw += f2;
        float f4 = (float)Math.round(this.desiredPitch / f3) * f3;
        float f5 = (float)Math.round(this.previousTargetYaw / f3) * f3;
        this.desiredPitch = MathHelper.clamp((float)(this.desiredPitch - f4), (float)(-f3 * 2.0f), (float)(f3 * 2.0f));
        this.previousTargetYaw = MathHelper.clamp((float)(this.previousTargetYaw - f5), (float)(-f3 * 2.0f), (float)(f3 * 2.0f));
        this.pendingAngles[0] = f4;
        this.pendingAngles[1] = f5;
    }

    private float getAimProgress() {
        double d = (Double)AimAssist.minecraftClient.options.getMouseSensitivity().getValue();
        double d2 = d * 0.6 + 0.2;
        double d3 = d2 * d2 * d2 * 8.0;
        return (float)(d3 * 0.15);
    }

    private void updateRotationState(float f, float f2, float f3, float f4) {
        float f5 = (float)Math.exp(-f4 / 0.04f);
        float f6 = this.getMotionDelta();
        float f7 = this.getMotionDelta();
        float f8 = MathHelper.sqrt((float)(f * f + f2 * f2)) + 1.0E-4f;
        float f9 = f3 * f8 * (1.0f - f5);
        float f10 = f / f8;
        float f11 = f2 / f8;
        float f12 = -f11;
        float f13 = f10;
        float f14 = f6 * f9;
        float f15 = f7 * f9 * 0.4f;
        this.destinationAngles[0] = this.destinationAngles[0] * f5 + f14 * f10 + f15 * f12;
        this.destinationAngles[1] = this.destinationAngles[1] * f5 + f14 * f11 + f15 * f13;
    }

    private float calculateMouseDelta(float f) {
        this.projectedYaw += this.predictedYaw * f;
        this.adjustedYaw += this.serverYaw * f;
        if ((double)this.projectedYaw > Math.PI * 2) {
            this.projectedYaw -= (float)Math.PI * 2;
        }
        if ((double)this.adjustedYaw > Math.PI * 2) {
            this.adjustedYaw -= (float)Math.PI * 2;
        }
        return this.clientYaw * (0.6f * (float)Math.sin(this.projectedYaw) + 0.4f * (float)Math.sin(this.adjustedYaw * 1.618f));
    }

    private float getMotionDelta() {
        if (this.predictionActive) {
            this.predictionActive = false;
            return this.originAngle;
        }
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        double d = Math.max(1.0E-10, threadLocalRandom.nextDouble());
        double d2 = threadLocalRandom.nextDouble();
        double d3 = Math.sqrt(-2.0 * Math.log(d));
        this.originAngle = (float)(d3 * Math.sin(Math.PI * 2 * d2));
        this.predictionActive = true;
        return (float)(d3 * Math.cos(Math.PI * 2 * d2));
    }

    private float interpolateAngle(float f, float f2) {
        this.remoteYaw += f2;
        if (this.remoteYaw >= this.visualYaw) {
            this.alwaysEnabled = !this.alwaysEnabled;
            this.remoteYaw = 0.0f;
            ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
            this.visualYaw = this.alwaysEnabled ? this.horizontalDelta + threadLocalRandom.nextFloat() * (this.verticalDelta - this.horizontalDelta) : this.forwardDelta + threadLocalRandom.nextFloat() * (this.backwardDelta - this.forwardDelta);
        }
        float f3 = this.alwaysEnabled ? 7.0f : 2.5f;
        float f4 = this.alwaysEnabled ? this.leftDelta : this.rightDelta;
        this.localYaw += (f4 - this.localYaw) * (1.0f - (float)Math.exp(-f3 * f2));
        float f5 = this.localYaw;
        float f6 = this.topDelta * (7.0f / Math.max(this.aimSpeed.getValue(), 1.0f));
        this.logicalYaw = MathHelper.clamp((float)(this.logicalYaw + f2 * f6), (float)0.0f, (float)this.bottomDelta);
        f5 += this.logicalYaw;
        if (f < this.innerOffset) {
            f5 *= this.baseOffset;
        } else if (f < this.outerOffset) {
            float f7 = (f - this.innerOffset) / (this.outerOffset - this.innerOffset);
            f5 *= this.baseOffset + (1.0f - this.baseOffset) * f7;
        }
        return Math.max(f5, 0.0f);
    }

    private void updateTargetAngles(float f, float f2) {
        if (this.targetSelected) {
            this.passiveBlend += f2;
            if (this.passiveBlend >= this.sourceAngle) {
                this.targetSelected = false;
                this.activeBlend = 0.0f;
                this.extraSmoothing = 0.0f;
            }
            return;
        }
        if (f < 3.0f && f > 0.5f) {
            if (ThreadLocalRandom.current().nextFloat() < this.overshootChance.getValue() * f2 * 20.0f) {
                this.targetSelected = true;
                this.passiveBlend = 0.0f;
                this.sourceAngle = 0.08f + ThreadLocalRandom.current().nextFloat() * 0.12f;
                float f3 = 1.0f + ThreadLocalRandom.current().nextFloat() * 2.0f;
                float f4 = ThreadLocalRandom.current().nextBoolean() ? 1.0f : -1.0f;
                this.extraSmoothing = f4 * f3 * f2;
                this.activeBlend = (ThreadLocalRandom.current().nextFloat() - 0.5f) * f3 * f2 * 0.3f;
            }
        } else {
            this.activeBlend = 0.0f;
            this.extraSmoothing = 0.0f;
        }
    }

    private float calculateSmoothStep(float f) {
        return -MathHelper.clamp((float)(f * 0.12f), (float)0.0f, (float)1.5f);
    }

    private void updatePredictedAngles(float f, float f2, float f3, float f4) {
        if (f < 0.1f) {
            this.overlayVisible = false;
            this.targetYaw = 1.0f;
            return;
        }
        if (!this.overlayVisible) {
            return;
        }
        this.targetYaw += f3 / Math.max(f4 * 0.4f, 0.01f);
        if (this.targetYaw >= 1.0f || f < f2 * 2.0f) {
            this.overlayVisible = false;
            this.targetYaw = 1.0f;
        }
    }

    private float calculateMagnitude(float f) {
        f = MathHelper.clamp((float)f, (float)0.0f, (float)1.0f);
        return 24.0f * f * f * (1.0f - f) * (1.0f - f);
    }

    private float calculateMagnitude(float f, float f2) {
        float f3 = MathHelper.sqrt((float)(f * f + f2 * f2));
        return f3 < 1.0E-4f ? 0.1f : 0.4f + 0.3f * Math.min(f3 / 5.0f, 1.0f);
    }

    private float calculateMagnitude(float f, float f2, float f3, float f4) {
        float f5 = Math.abs(f);
        float f6 = f > 0.0f ? 0.4f : 0.8f;
        float f7 = this.verticalFactor.getValue();
        if (f5 > 7.0f) {
            float f8 = this.calculateRotation(f5, f3);
            float f9 = Math.max(f8 / (this.aimSpeed.getValue() * 0.4f), 0.02f);
            return MathHelper.clamp((float)(Math.signum(f) * (f5 / f9) * 0.5f * f4 * this.strength.getValue() * f6 * f7), (float)-20.0f, (float)20.0f);
        }
        if (f5 > 0.3f) {
            return MathHelper.clamp((float)(f * 0.6f * f6 * f7 * f4 * 10.0f), (float)-20.0f, (float)20.0f);
        }
        return f * 2.0f * 0.4f * f6 * f7 * f4 * 10.0f;
    }

    private float calculateRotation(float f, float f2) {
        if (f < 1.0E-4f) {
            return 0.0f;
        }
        return (float)(Math.log((double)(f / Math.max(f2, 0.1f)) + 1.0) / Math.log(2.0));
    }

    private float calculateRotation(LivingEntity class_13092) {
        if (class_13092 == null) {
            return 1.0f;
        }
        double d = class_13092.getX() - this.rawYaw;
        double d2 = class_13092.getY() + (double)class_13092.getHeight() * 0.5 - this.clampedYaw;
        double d3 = class_13092.getZ() - this.normalizedYaw;
        double d4 = d * d + d2 * d2 + d3 * d3;
        double d5 = Math.max(Math.sqrt(d4), 0.5);
        return (float)Math.toDegrees(Math.atan2((double)class_13092.getWidth() * 0.5, d5));
    }

    private void resetStateFallback() {
        if (!Float.isNaN(this.previousTargetPitch) && AimAssist.minecraftClient.player != null) {
            this.noisePitch = MathHelper.wrapDegrees((float)(AimAssist.minecraftClient.player.getYaw() - this.previousTargetPitch)) - this.predictionPitch;
            this.predictionYaw = AimAssist.minecraftClient.player.getPitch() - this.noiseYaw - this.interpolationProgress;
        } else {
            this.predictionYaw = 0.0f;
            this.noisePitch = 0.0f;
        }
    }

    private void resetStateAlternate() {
        if (AimAssist.minecraftClient.player != null) {
            this.previousTargetPitch = AimAssist.minecraftClient.player.getYaw();
            this.noiseYaw = AimAssist.minecraftClient.player.getPitch();
        }
    }

    private float calculateMagnitude(float f, float f2, float f3) {
        float f4;
        if (Math.abs(f) < 1.0E-4f) {
            return f;
        }
        float f5 = f4 = f3 > 1.0E-4f ? f2 / f3 : 0.0f;
        if (Math.abs(f4) < 0.1f) {
            return f;
        }
        if (f > 0.0f == f4 > 0.0f) {
            return f * 0.95f;
        }
        float f6 = 1.0f - MathHelper.clamp((float)(Math.abs(f4) / Math.abs(f)), (float)0.0f, (float)0.8f);
        return f * f6;
    }

    private boolean isAngleWithinLimit(float f, float f2, float f3) {
        if (f3 < 1.0E-4f) {
            return false;
        }
        float f4 = this.noisePitch / f3;
        float f5 = this.predictionYaw / f3;
        float f6 = this.inputThreshold.getValue();
        if (Math.abs(f4) < f6 && Math.abs(f5) < f6) {
            return false;
        }
        boolean bl = Math.signum(f) == Math.signum(f4) || Math.abs(f) < 0.5f;
        boolean bl2 = Math.signum(f2) == Math.signum(f5) || Math.abs(f2) < 0.5f;
        return bl || bl2;
    }

    private void applyTargetRotation(LivingEntity class_13092, double d, double d2, double d3) {
        long l;
        if (!this.multipoint.isEnabled() || class_13092 == null || AimAssist.minecraftClient.player == null || AimAssist.minecraftClient.world == null) {
            return;
        }
        long l2 = AimAssist.minecraftClient.world.getTime();
        if (l2 - this.lastPredictionAt < (l = Math.max(1L, (long)(this.regen.getValue() * 20.0f)))) {
            return;
        }
        this.lastPredictionAt = l2;
        Vec3d VanillaChestLootTableGenerator = new Vec3d(d, d2, d3);
        this.rotationSolver.configureSampling((int)this.mpCount.getValue(), this.mpSpread.getValue(), this.mpAdaptive.isEnabled());
        this.rotationSolver.generateAimPointCandidates(VanillaChestLootTableGenerator, class_13092, AimAssist.minecraftClient.player.getPos(), AimAssist.minecraftClient.player.distanceTo((Entity)class_13092));
    }

    private void resetTracking() {
        boolean bl;
        if (this.targetEntity == null || AimAssist.minecraftClient.player == null || AimAssist.minecraftClient.world == null) {
            return;
        }
        Entity class_12972 = AimAssist.minecraftClient.world.getEntityById(this.targetEntity.getId());
        boolean bl2 = bl = class_12972 != this.targetEntity || this.targetEntity.distanceTo((Entity)AimAssist.minecraftClient.player) > this.aimDistance.getValue() + 1.0f || this.targetEntity.isDead() || this.targetEntity.getHealth() <= 0.0f;
        if (bl) {
            if (this.previousTarget == this.targetEntity) {
                this.previousTarget = null;
            }
            this.targetEntity = null;
            this.disableLocked = false;
            this.targetLockStartedAt = System.currentTimeMillis();
        }
    }

    private void finishTargeting() {
        if (this.isInputReady()) {
            this.previousTarget = null;
            return;
        }
        long l = System.currentTimeMillis();
        if (this.previousTarget != null && l - this.lastAimAt > (long)(this.lockTimeout.getValue() * 1000.0f)) {
            this.previousTarget = null;
        }
        if (this.targetLockMode.isSelected(this.lockOnAttack) && this.previousTarget == null && AimAssist.minecraftClient.options.attackKey.isPressed() && this.targetEntity != null) {
            this.previousTarget = this.targetEntity;
            this.lastAimAt = l;
        }
        if (this.targetLockMode.isSelected(this.lockAutomatic) && this.previousTarget == null && this.targetEntity != null && l - this.targetLockStartedAt < 500L) {
            this.previousTarget = this.targetEntity;
            this.lastAimAt = l;
        }
        if (this.previousTarget != null && AimAssist.minecraftClient.player != null && (this.previousTarget.isDead() || this.previousTarget.getHealth() <= 0.0f || this.previousTarget.distanceTo((Entity)AimAssist.minecraftClient.player) > this.aimDistance.getValue())) {
            this.previousTarget = null;
        }
    }

    private void restorePreviousAim() {
        boolean bl;
        LivingEntity class_13092;
        if (AimAssist.minecraftClient.player == null) {
            return;
        }
        if (this.previousTarget != null && !this.isInputReady()) {
            this.targetEntity = this.previousTarget;
            return;
        }
        FriendManager friendManager = RockstarClient.create().getFriendManager();
        if (friendManager == null) {
            return;
        }
        Entity class_12972 = friendManager.getTargetEntity();
        LivingEntity class_13093 = class_12972 instanceof LivingEntity ? (class_13092 = (LivingEntity)class_12972) : null;
        boolean bl2 = bl = class_13093 == null || !class_13093.isAlive() || class_13093.distanceTo((Entity)AimAssist.minecraftClient.player) > this.aimDistance.getValue();
        if (bl) {
            LivingEntity class_13094;
            friendManager.updateTarget(this.buildTargetFilter());
            Entity class_12973 = friendManager.getTargetEntity();
            LivingEntity class_13095 = class_13093 = class_12973 instanceof LivingEntity ? (class_13094 = (LivingEntity)class_12973) : null;
        }
        if (class_13093 != this.targetEntity) {
            this.selectTarget(class_13093);
        }
    }

    private void selectTarget(LivingEntity class_13092) {
        this.targetEntity = class_13092;
        this.targetLockStartedAt = System.currentTimeMillis();
        this.disableLocked = false;
        this.fovRadians = 0.0;
        this.aimAngle = 0.0;
        this.targetDistance = 0.0;
        this.targetPitch = 0.0f;
        this.privilegedMode = false;
        this.smoothedYaw = this.targetEntity != null ? this.getTargetDistance(this.targetEntity) : 0.0f;
        this.overlayVisible = true;
        this.targetYaw = 0.0f;
        this.lastPredictionAt = 0L;
        this.rotationSolver.reset();
        this.movementState.resetSamples();
        this.targetSelected = false;
        this.activeBlend = 0.0f;
        this.extraSmoothing = 0.0f;
        this.desiredYaw = 0.0f;
        this.smoothedPitch = 0.0f;
        this.destinationAngles[1] = 0.0f;
        this.destinationAngles[0] = 0.0f;
    }

    private float getTargetDistance(LivingEntity class_13092) {
        double d;
        double d2;
        float f = 0.5f;
        if (AimAssist.minecraftClient.player == null || class_13092 == null) {
            return f;
        }
        double d3 = class_13092.getX() - AimAssist.minecraftClient.player.getX();
        double d4 = d3 * d3 + (d2 = class_13092.getY() - AimAssist.minecraftClient.player.getY()) * d2 + (d = class_13092.getZ() - AimAssist.minecraftClient.player.getZ()) * d;
        if (d4 > (double)1.0E-4f) {
            double d5 = Math.sqrt(d4);
            Vec3d VanillaChestLootTableGenerator = AimAssist.minecraftClient.player.getRotationVector();
            float f2 = (float)((d3 * VanillaChestLootTableGenerator.x + d2 * VanillaChestLootTableGenerator.y + d * VanillaChestLootTableGenerator.z) / d5);
            f += (1.0f - MathHelper.clamp((float)f2, (float)-1.0f, (float)1.0f)) * 0.08f;
        }
        return f;
    }

    private TargetFilter buildTargetFilter() {
        return new TargetFilter.Builder().players(this.players.isSelected()).animals(this.animals.isSelected()).mobs(this.mobs.isSelected()).invisibles(this.invisibles.isSelected()).nakedPlayers(this.nakedPlayers.isSelected()).friends(this.friends.isSelected()).rockstarUsers(this.rockUsers.isSelected()).range(this.aimDistance.getValue()).sortComparator(this.targetSortMode.isSelected(this.healthSort) ? EntityProcessor.HEALTH_COMPARATOR : (this.targetSortMode.isSelected(this.fovSort) ? EntityProcessor.FIELD_OF_VIEW_COMPARATOR : EntityProcessor.DISTANCE_COMPARATOR)).build();
    }

    private boolean isFallbackReady() {
        if (this.targetEntity == null || AimAssist.minecraftClient.player == null) {
            return true;
        }
        return this.repitAim.isEnabled() && !this.privilegedMode;
    }

    private float calculateLockProgress(long l) {
        if (this.lastAttackAt == 0L) {
            return 0.016666668f;
        }
        long l2 = l - this.lastAttackAt;
        if (l2 <= 0L) {
            return 0.016666668f;
        }
        return Math.min((float)l2 / 1.0E9f, 0.1f);
    }

    private float getAimStrength() {
        try {
            return minecraftClient.getRenderTickCounter().getTickDelta(false);
        }
        catch (NoSuchMethodError noSuchMethodError) {
            return 1.0f;
        }
    }

    private float calculateWeightedAngle(float f, float f2, float f3) {
        return f + (f2 - f) * f3;
    }

    private boolean isCompletionReady() {
        return AimAssist.minecraftClient.player == null || AimAssist.minecraftClient.world == null || AimAssist.minecraftClient.player.isDead();
    }

    public boolean isCurrentTargetReady() {
        if (AimAssist.minecraftClient.player == null) {
            return false;
        }
        ItemStack class_17992 = AimAssist.minecraftClient.player.getStackInHand(Hand.MAIN_HAND);
        if (class_17992.isEmpty()) {
            return false;
        }
        Item class_17922 = class_17992.getItem();
        try {
            RegistryEntry.Reference class_68832 = class_17922.getRegistryEntry();
            if (class_68832 != null) {
                String string = class_68832.getIdAsString();
                for (String string2 : candidateTargets) {
                    if (!string.contains(string2)) continue;
                    return true;
                }
            }
        }
        catch (Exception exception) {
            String string = class_17922.getName().getString().toLowerCase();
            for (String string3 : candidateTargets) {
                if (!string.contains(string3)) continue;
                return true;
            }
        }
        return false;
    }

    @Generated
    public MovementUtils getMovementUtils() {
        return this.movementUtils;
    }
}

