/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Hand
 *  net.minecraft.StatusEffects
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Items
 *  net.minecraft.SwordItem
 *  net.minecraft.UseAction
 *  net.minecraft.Block
 *  net.minecraft.DoorBlock
 *  net.minecraft.Direction
 *  net.minecraft.Box
 *  net.minecraft.HitResult$Type
 *  net.minecraft.Vec3d
 *  net.minecraft.TrapdoorBlock
 *  net.minecraft.Packet
 *  net.minecraft.PlayerActionC2SPacket
 *  net.minecraft.PlayerActionC2SPacket$Action
 *  net.minecraft.PlayerInteractItemC2SPacket
 *  net.minecraft.Identifier
 *  net.minecraft.MathHelper
 *  net.minecraft.RaycastContext
 *  net.minecraft.RaycastContext$FluidHandling
 *  net.minecraft.RaycastContext$ShapeType
 *  net.minecraft.BlockHitResult
 *  net.minecraft.InventoryScreen
 *  net.minecraft.ClientPlayerEntity
 */
package moscow.rockstar.modules.combat.attacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Generated;
import moscow.rockstar.combat.RotationController;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.WallMode;
import moscow.rockstar.combat.critical.AttackCriticalHandler;
import moscow.rockstar.combat.critical.CriticalHitTiming;
import moscow.rockstar.combat.critical.MeleeDamage;
import moscow.rockstar.combat.critical.SprintResetPolicy;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.combat.rotation.RotationRequest;
import moscow.rockstar.combat.rotation.RotationReturnMode;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.core.TargetFilter;
import moscow.rockstar.entity.EntityProcessor;
import moscow.rockstar.entity.targeting.TargetActionQueue;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.HotbarActionService;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.mixin.accessors.ItemCooldownEntryAccessor;
import moscow.rockstar.mixin.accessors.ItemCooldownManagerAccessor;
import moscow.rockstar.api.access.RotationStepAccess;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.attacks.Criticals;
import moscow.rockstar.modules.combat.aura.rotation.AuraRotationMode;
import moscow.rockstar.modules.combat.aura.rotation.SimpleRotationMode;
import moscow.rockstar.modules.combat.defense.KnockbackTweaks;
import moscow.rockstar.modules.combat.targeting.AntiBot;
import moscow.rockstar.modules.combat.targeting.ElytraTarget;
import moscow.rockstar.modules.movement.jump.AirStuck;
import net.minecraft.util.math.BlockPos;
import moscow.rockstar.modules.player.blink.BlinkEventListener;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.render.esp.EntityOverlayGeometry;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.RangeSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.block.Block;
import net.minecraft.block.DoorBlock;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import pyrock.events.game.EntityJumpEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Aura", category=ModuleCategory.COMBAT, description="modules.descriptions.aura")
public class Aura
extends Module {
    private NumberSetting attackDistance;
    private NumberSetting aimDistance;
    private MultiBooleanSetting targets;
    private MultiBooleanSetting.Option players;
    private MultiBooleanSetting.Option animals;
    private MultiBooleanSetting.Option mobs;
    private MultiBooleanSetting.Option invisibles;
    private MultiBooleanSetting.Option nakedPlayers;
    private MultiBooleanSetting.Option friends;
    private MultiBooleanSetting.Option rockUsers;
    private ModeSetting sorting;
    private ModeSetting.Option distanceSorting;
    private ModeSetting.Option healthSorting;
    private ModeSetting.Option fovSorting;
    private ModeSetting rotationMode;
    private ModeSetting.Option smoothRotation;
    private ModeSetting.Option noRotation;
    private ModeSetting returnMode;
    private ModeSetting.Option noReturnOption;
    private ModeSetting.Option smooth;
    private ModeSetting.Option camera;
    private ModeSetting moveCorrectionMode;
    private ModeSetting.Option noMoveCorrection;
    private ModeSetting.Option directMoveCorrection;
    private ModeSetting.Option silentMoveCorrection;
    private ModeSetting.Option targetedMoveCorrection;
    private BooleanSetting forceTargetedRanged;
    private BooleanSetting forceBehindTargeted;
    private ModeSetting styleAttack;
    private ModeSetting.Option legacyAttackStyle;
    private ModeSetting.Option modernAttackStyle;
    private RangeSetting cpsLimiter;
    private BooleanSetting onlyCrits;
    private BooleanSetting smartCriticals;
    private BooleanSetting rayTrace;
    private BooleanSetting onlyWeapon;
    private BooleanSetting autoMace;
    private BooleanSetting targeting;
    private BooleanSetting noHitInv;
    private ModeSetting walls;
    private ModeSetting.Option noWallsOption;
    private ModeSetting.Option allWallsOption;
    private ModeSetting.Option doorsAndTrapdoorsOption;
    private ModeSetting.Option reachableWallsOption;
    private ModeSetting.Option nonSolidBlocksOption;
    private ModeSetting critCalc;
    private ModeSetting.Option oldCriticalsOption;
    private ModeSetting.Option newCriticalsOption;
    private ModeSetting.Option airCriticalsOption;
    private ModeSetting sprintReset;
    private ModeSetting.Option smartSprintResetOption;
    private ModeSetting.Option normalSprintResetOption;
    private ModeSetting.Option packetSprintResetOption;
    private MultiBooleanSetting utilities;
    private MultiBooleanSetting.Option resolver;
    private MultiBooleanSetting.Option useHit;
    private MultiBooleanSetting.Option sync;
    private MultiBooleanSetting.Option syncTps;
    private MultiBooleanSetting.Option excludeTeammatesOption;
    private Timer cooldownTimer;
    private long lastAttackTime;
    private float attackCooldown;
    boolean attackReady;
    boolean targetVisible;
    boolean privilegedMode;
    int legacyAttackState;
    private final BlinkEventListener predictionState = new BlinkEventListener();
    private RotationController rotationController;
    private int attackCycle;
    private Rotation currentRotation;
    private boolean disableLocked;
    private boolean alwaysEnabled;
    private int targetIndex = -1;
    private boolean targetSelected;
    private static final float ATTACK_RANGE_MULTIPLIER = 1.5f;
    private static final int MAX_TARGET_COUNT = 4;
    private static final long ATTACK_RETRY_DELAY = 200L;
    private float attackRange = MathUtils.interpolateRandomStrategy(0.0f, 1.0f);
    private long nextAttackTime;
    private double lastVelocityY = 0.0;
    private final Map<String, Integer> attackStatistics = new LinkedHashMap<String, Integer>();
    private final EventListener<EntityJumpEvent> onEntityJumpEvent = entityJumpEvent -> {
        if (Aura.minecraftClient.player != entityJumpEvent.getEntity()) {
            return;
        }
        if (this.newCriticalsOption != null && this.critCalc.isSelected(this.newCriticalsOption) && Aura.minecraftClient.player.isOnGround() && Aura.minecraftClient.player.getMainHandStack().getItem() instanceof SwordItem) {
            LivingEntity class_13092 = RockstarClient.create().getFriendManager().getTargetLivingEntity();
            if (CriticalHitTiming.shouldCancelJump(Aura.minecraftClient.player, EntityOverlayGeometry.getEntityHeight(class_13092))) {
                entityJumpEvent.cancel();
            }
        }
    };

    public Aura() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.rotationMode = new ModeSetting(this, "modules.settings.aura.rotationMode");
        this.noRotation = new ModeSetting.Option(this.rotationMode, "modules.settings.aura.noRotation");
        this.smoothRotation = new SimpleRotationMode(this.rotationMode).select();
        this.returnMode = new ModeSetting((SettingOwner)this, "modules.settings.aura.returnMode", () -> this.rotationMode.isSelected(this.noRotation));
        this.noReturnOption = new ModeSetting.Option(this.returnMode, "modules.settings.aura.returnMode.none");
        this.smooth = new ModeSetting.Option(this.returnMode, "modules.settings.aura.returnMode.smooth").select();
        this.camera = new ModeSetting.Option(this.returnMode, "modules.settings.aura.returnMode.camera");
        this.attackDistance = new NumberSetting(this, "modules.settings.aura.attackDistance").setMinValue(0.1f).setMaxValue(6.0f).setStep(0.1f).setValue(3.0f).setUnit(" block").setChangeListener(f -> {
            if (this.aimDistance != null && this.aimDistance.getValue() < f.floatValue()) {
                this.aimDistance.setValue(f.floatValue());
            }
            return f;
        });
        this.aimDistance = new NumberSetting(this, "modules.settings.aura.aimDistance").setMinValue(0.1f).setMaxValue(9.0f).setStep(0.1f).setValue(3.0f).setUnit(" block").setChangeListener(f -> Float.valueOf(this.attackDistance == null ? f.floatValue() : Math.max(this.attackDistance.getValue(), f.floatValue())));
        this.onlyCrits = new BooleanSetting((SettingOwner)this, "modules.settings.aura.onlyCrits", () -> true);
        this.smartCriticals = new BooleanSetting((SettingOwner)this, "modules.settings.aura.smart_criticals", () -> true).enable();
        this.walls = new ModeSetting(this, "modules.settings.aura.walls");
        this.noWallsOption = new ModeSetting.Option(this.walls, "modules.settings.aura.walls.none").select();
        this.allWallsOption = new ModeSetting.Option(this.walls, "modules.settings.aura.walls.all");
        this.doorsAndTrapdoorsOption = new ModeSetting.Option(this.walls, "modules.settings.aura.walls.doors");
        this.reachableWallsOption = new ModeSetting.Option(this.walls, "modules.settings.aura.walls.rw");
        this.nonSolidBlocksOption = new ModeSetting.Option(this.walls, "modules.settings.aura.walls.ft");
        this.rayTrace = new BooleanSetting((SettingOwner)this, "modules.settings.aura.rayTrace", () -> true).enable();
        this.targeting = new BooleanSetting(this, "modules.settings.aura.targeting").enable();
        this.onlyWeapon = new BooleanSetting(this, "modules.settings.aura.onlyWeapon");
        this.autoMace = new BooleanSetting((SettingOwner)this, "modules.settings.aura.auto_mace", "modules.settings.aura.auto_mace.description");
        this.noHitInv = new BooleanSetting(this, "modules.settings.aura.no_hit_inv");
        this.targets = new MultiBooleanSetting(this, "modules.settings.aura.targets");
        this.players = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.players").select();
        this.animals = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.animals").select();
        this.mobs = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.mobs").select();
        this.invisibles = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.invisibles").select();
        this.nakedPlayers = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.nakedPlayers").select();
        this.rockUsers = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.rockUsers");
        this.friends = new MultiBooleanSetting.Option(this.targets, "modules.settings.aura.targets.friends");
        this.sorting = new ModeSetting(this, "modules.settings.aura.sorting");
        this.distanceSorting = new ModeSetting.Option(this.sorting, "modules.settings.aura.distanceSorting").select();
        this.healthSorting = new ModeSetting.Option(this.sorting, "modules.settings.aura.healthSorting");
        this.fovSorting = new ModeSetting.Option(this.sorting, "modules.settings.aura.fovSorting");
        this.moveCorrectionMode = new ModeSetting(this, "modules.settings.aura.moveCorrectionMode");
        this.noMoveCorrection = new ModeSetting.Option(this.moveCorrectionMode, "modules.settings.aura.noMoveCorrection");
        this.directMoveCorrection = new ModeSetting.Option(this.moveCorrectionMode, "modules.settings.aura.directMoveCorrection");
        this.silentMoveCorrection = new ModeSetting.Option(this.moveCorrectionMode, "modules.settings.aura.silentMoveCorrection").select();
        this.targetedMoveCorrection = new ModeSetting.Option(this.moveCorrectionMode, "modules.settings.aura.targeted_move_correction");
        this.forceTargetedRanged = new BooleanSetting((SettingOwner)this, "modules.settings.aura.force_targeted_ranged", () -> this.moveCorrectionMode.isSelected(this.targetedMoveCorrection));
        this.forceBehindTargeted = new BooleanSetting((SettingOwner)this, "modules.settings.aura.force_behind_targeted", () -> !this.forceTargetedRanged.isEnabled() || this.moveCorrectionMode.isSelected(this.targetedMoveCorrection));
        this.styleAttack = new ModeSetting((SettingOwner)this, "modules.settings.aura.styleAttack", () -> true);
        this.modernAttackStyle = new ModeSetting.Option(this.styleAttack, "1.9").select();
        this.legacyAttackStyle = new ModeSetting.Option(new ModeSetting((SettingOwner)this, "unused_legacy", () -> true), "1.8");
        this.cpsLimiter = new RangeSetting((SettingOwner)this, "modules.settings.aura.cps_limiter", () -> true).setMinimum(1.0f).setMaximum(20.0f).setStep(1.0f).setFirstValue(8.0f).setSecondValue(12.0f);
        this.critCalc = new ModeSetting((SettingOwner)this, "modules.settings.aura.crit_calc", () -> true);
        this.airCriticalsOption = new ModeSetting.Option(this.critCalc, "modules.settings.aura.crit_calc.air").select();
        this.sprintReset = new ModeSetting((SettingOwner)this, "modules.settings.aura.sprint_reset", () -> true);
        this.smartSprintResetOption = new ModeSetting.Option(this.sprintReset, "modules.settings.aura.sprint_reset.smart");
        this.normalSprintResetOption = new ModeSetting.Option(this.sprintReset, "modules.settings.aura.sprint_reset.normal").select();
        this.packetSprintResetOption = new ModeSetting.Option(this.sprintReset, "modules.settings.aura.sprint_reset.packet");
        this.utilities = new MultiBooleanSetting(this, "modules.settings.aura.utilities");
        this.resolver = new MultiBooleanSetting.Option(this.utilities, "modules.settings.aura.resolver");
        this.useHit = new MultiBooleanSetting.Option(this.utilities, "modules.settings.aura.useHit");
        this.excludeTeammatesOption = new MultiBooleanSetting.Option(this.utilities, "modules.settings.aura.no_teammates_1_8", () -> true);
        this.sync = new MultiBooleanSetting.Option(this.utilities, "modules.settings.aura.sync");
        this.syncTps = new MultiBooleanSetting.Option(this.utilities, "modules.settings.aura.sync_tps");
        this.cooldownTimer = new Timer();
    }

    public WallMode getWallMode() {
        if (this.walls == null || this.walls.isSelected(this.noWallsOption)) {
            return WallMode.NONE;
        }
        if (this.walls.isSelected(this.allWallsOption)) {
            return WallMode.ALL;
        }
        if (this.walls.isSelected(this.doorsAndTrapdoorsOption)) {
            return WallMode.DOORS_AND_TRAPDOORS;
        }
        if (this.walls.isSelected(this.nonSolidBlocksOption)) {
            return WallMode.NON_SOLID_BLOCKS;
        }
        if (this.walls.isSelected(this.reachableWallsOption)) {
            return WallMode.REACHABLE_WALLS;
        }
        return WallMode.NONE;
    }

    @Override
    public void onTick() {
        LivingEntity class_13092;
        boolean bl;
        ClientAccess clientAccess;
        if (this.aimDistance.getValue() < this.attackDistance.getValue()) {
            this.aimDistance.setValue(this.attackDistance.getValue());
        }
        if (Aura.minecraftClient.player == null) {
            return;
        }
        ModeSetting.Option option = this.rotationMode.getSelectedOption();
        if (option instanceof AuraRotationMode) {
            clientAccess = (AuraRotationMode)option;
            ((AuraRotationMode)clientAccess).tick();
        }
        float f = (bl = ((Module)(clientAccess = RockstarClient.create().getModuleRegistry().getModule(ElytraTarget.class))).isEnabled()) ? ((ElytraTarget)clientAccess).getEngageRange().getValue() : Math.max(this.aimDistance.getValue(), this.getAttackProgress());
        TargetFilter.Builder builder = new TargetFilter.Builder().players(this.players.isSelected()).animals(!bl && this.animals.isSelected()).mobs(!bl && this.mobs.isSelected()).invisibles(this.invisibles.isSelected()).nakedPlayers(this.nakedPlayers.isSelected()).friends(this.friends.isSelected()).rockstarUsers(this.rockUsers.isSelected()).excludeTeammates(this.excludeTeammatesOption.isSelected()).range(f);
        if (bl || this.sorting.isSelected(this.distanceSorting)) {
            builder.sortComparator(EntityProcessor.DISTANCE_COMPARATOR);
        } else if (this.sorting.isSelected(this.healthSorting)) {
            builder.sortComparator(EntityProcessor.HEALTH_COMPARATOR);
        } else if (this.sorting.isSelected(this.fovSorting)) {
            builder.sortComparator(EntityProcessor.FIELD_OF_VIEW_COMPARATOR);
        }
        TargetFilter targetFilter = builder.build();
        Entity class_12972 = RockstarClient.create().getFriendManager().getTargetEntity();
        if (class_12972 instanceof LivingEntity) {
            class_13092 = (LivingEntity)class_12972;
        } else {
            class_13092 = null;
        }
        if (!this.targeting.isEnabled() || class_13092 == null || !targetFilter.acceptsEntity((Entity)class_13092) || MathHelper.sqrt((float)((float)Aura.minecraftClient.player.squaredDistanceTo(AimRotationMath.getClosestPointOnEntityBounds((Entity)class_13092)))) > f || !Aura.minecraftClient.world.hasEntity((Entity)class_13092) || !class_13092.isAlive() || AntiBot.isEntityValid(class_13092)) {
            RockstarClient.create().getFriendManager().updateTarget(targetFilter);
            class_12972 = RockstarClient.create().getFriendManager().getTargetEntity();
            if (class_12972 instanceof LivingEntity) {
                class_13092 = (LivingEntity)class_12972;
            } else {
                class_13092 = null;
            }
        }
        if (class_13092 != null) {
            this.performEmergencyAttack(class_13092);
            this.targetVisible = false;
            for (PlayerEntity class_16572 : Aura.minecraftClient.world.getPlayers()) {
                if (!(Aura.minecraftClient.player.distanceTo((Entity)class_16572) < 4.0f) || !RockstarClient.create().getFriendListManager().containsFriend(class_16572.getNameForScoreboard())) continue;
                this.targetVisible = true;
            }
            this.performCriticalAttack(class_13092);
            if (this.isEntityValid(class_13092, true)) {
                if (this.isEntityValid(class_13092)) {
                    this.attackStatistics.merge("\u0441\u043f\u0440\u0438\u043d\u0442-\u0440\u0435\u0441\u0435\u0442 (\u0432\u043c\u0435\u0441\u0442\u043e \u0443\u0434\u0430\u0440\u0430)", 1, Integer::sum);
                    return;
                }
                this.attackStatistics.merge("\u2605 \u0423\u0414\u0410\u0420 \u0412\u042b\u041f\u041e\u041b\u041d\u0415\u041d", 1, Integer::sum);
                this.performPrimaryAttack(class_13092);
            }
        } else {
            this.resetAttackState();
            ModeSetting.Option option2 = this.rotationMode.getSelectedOption();
            if (option2 instanceof AuraRotationMode auraRotationMode) {
                this.disableLocked = false;
                auraRotationMode.onTargetLost();
            }
        }
        if (Aura.minecraftClient.player != null) {
            this.lastVelocityY = Aura.minecraftClient.player.getVelocity().y;
        }
    }

    public boolean isEntityValid(LivingEntity class_13092, boolean bl) {
        ClientAccess clientAccess;
        Criticals criticals;
        if (!this.isAttackCooldownReady()) {
            return this.isProtectedName("\u043a\u0443\u043b\u0434\u0430\u0443\u043d \u0430\u0443\u0440\u044b");
        }
        if (AntiBot.isEntityValid(class_13092)) {
            return this.isProtectedName("antibot");
        }
        if (this.excludeTeammatesOption.isSelected() && class_13092 instanceof PlayerEntity && TargetFilter.areTeammates((PlayerEntity)Aura.minecraftClient.player, (PlayerEntity)class_13092)) {
            return this.isProtectedName("\u0441\u043e\u044e\u0437\u043d\u0438\u043a");
        }
        criticals = RockstarClient.create().getModuleRegistry().getModule(Criticals.class);
        if (criticals.isCriticalsEnvironmentReady() && !criticals.isCriticalsTargetReady()) {
            return this.isProtectedName("\u043c\u043e\u0434\u0443\u043b\u044c Criticals");
        }
        if (this.onlyWeapon.isEnabled() && !EntityUtils.isHoldingMiningTool()) {
            return this.isProtectedName("\u043d\u0435 \u043e\u0440\u0443\u0436\u0438\u0435 \u0432 \u0440\u0443\u043a\u0435");
        }
        if (this.isUsingItemAttackReady()) {
            return this.isProtectedName("\u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0435\u0442\u0441\u044f \u043f\u0440\u0435\u0434\u043c\u0435\u0442");
        }
        ModeSetting.Option option = this.rotationMode.getSelectedOption();
        if (option instanceof AuraRotationMode && !((AuraRotationMode)(clientAccess = (AuraRotationMode)option)).canAttack()) {
            return this.isProtectedName("\u0440\u0435\u0436\u0438\u043c \u043d\u0435 \u0445\u043e\u0447\u0435\u0442 \u0431\u0438\u0442\u044c");
        }
        if (Aura.minecraftClient.currentScreen instanceof InventoryScreen && this.noHitInv.isEnabled()) {
            return this.isProtectedName("\u043e\u0442\u043a\u0440\u044b\u0442 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c");
        }
        if (this.sync.isSelected() && Aura.minecraftClient.player.hurtTime > 0 && this.targetVisible) {
            return this.isProtectedName("sync \u043f\u043e hurtTime");
        }
        if (!this.isWithinAttackRange(class_13092)) {
            return this.isProtectedName("\u0434\u0430\u043b\u0435\u043a\u043e (attackDistance)");
        }
        if (this.isAutoMaceTargetValid(class_13092) && EntityOverlayGeometry.getAttackHotbarSlot() != null) {
            if (!this.isFallingForCritical()) {
                return this.isProtectedName("\u043e\u0436\u0438\u0434\u0430\u0435\u043c \u0432\u044b\u0441\u043e\u0442\u0443 \u0434\u043b\u044f \u0431\u0443\u043b\u0430\u0432\u044b");
            }
            if (ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) && !this.isMaceReady()) {
                return this.isProtectedName("\u043e\u0436\u0438\u0434\u0430\u0435\u043c \u0441\u0432\u0430\u043f \u043d\u0430 \u0431\u0443\u043b\u0430\u0432\u0443");
            }
        }
        boolean bl2 = ((Module)(clientAccess = RockstarClient.create().getModuleRegistry().getModule(ElytraTarget.class))).isEnabled() && Aura.minecraftClient.player.isGliding() && Aura.minecraftClient.player.getVelocity().length() < 6.0;
        if (bl2) {
            return true;
        }
        if (!this.isRaycastPassing(class_13092, bl)) {
            return this.isProtectedName("\u0440\u0435\u0439\u0442\u0440\u0435\u0439\u0441 \u043d\u0435 \u043f\u0440\u043e\u0445\u043e\u0434\u0438\u0442");
        }
        if (!AttackCriticalHandler.getInstance().allowsAttack(this.getCriticalMode(), class_13092)) {
            return this.isProtectedName("ждём крит");
        }
        this.attackStatistics.merge("проверки пройдены", 1, Integer::sum);
        return true;
    }

    public boolean isAirCriticalReady() {
        LivingEntity target = RockstarClient.create().getFriendManager().getTargetLivingEntity();
        return AttackCriticalHandler.getInstance().allowsAttack(this.getCriticalMode(), target);
    }

    private boolean isRaycastPassing(LivingEntity class_13092, boolean bl) {
        if (!this.rayTrace.isEnabled() || !bl) {
            return true;
        }
        if (this.privilegedMode) {
            return true;
        }
        ClientPlayerEntity player = Aura.minecraftClient.player;
        if (player == null || class_13092 == null) {
            return false;
        }
        if (Aura.minecraftClient.targetedEntity == class_13092) {
            return true;
        }
        if (MathUtils.isRotationPathClear(this.getAttackProgress(), player.getYaw(), player.getPitch(), (Entity)player, (Entity)class_13092, this.getWallMode())) {
            return true;
        }
        Rotation rotation = RockstarClient.create().getRotationManager().getCurrentRotation();
        return MathUtils.isRotationPathClear(this.getAttackProgress(), rotation.getYaw(), rotation.getPitch(), (Entity)player, (Entity)class_13092, this.getWallMode());
    }

    private boolean isProtectedName(String string) {
        this.attackStatistics.merge(string, 1, Integer::sum);
        return false;
    }

    public boolean isWithinRange(LivingEntity class_13092) {
        return this.isWithinRange(class_13092, false);
    }

    public boolean isWithinRange(LivingEntity class_13092, boolean bl) {
        AuraRotationMode auraRotationMode;
        Criticals criticals = RockstarClient.create().getModuleRegistry().getModule(Criticals.class);
        if (criticals.isCriticalsEnvironmentReady() && !criticals.isCriticalsTargetReady()) {
            return false;
        }
        if (this.onlyWeapon.isEnabled() && !EntityUtils.isHoldingMiningTool()) {
            return false;
        }
        if (this.isUsingItemAttackReady()) {
            return false;
        }
        ModeSetting.Option option = this.rotationMode.getSelectedOption();
        if (option instanceof AuraRotationMode && !(auraRotationMode = (AuraRotationMode)option).canAttack()) {
            return false;
        }
        if (Aura.minecraftClient.currentScreen instanceof InventoryScreen && this.noHitInv.isEnabled()) {
            return false;
        }
        if (this.sync.isSelected() && Aura.minecraftClient.player.hurtTime > 0 && this.targetVisible) {
            return false;
        }
        if (bl ? Aura.minecraftClient.player.getEyePos().add(0.0, -1.0, 0.0).distanceTo(AimRotationMath.translateAimPoint(class_13092, EntityOverlayGeometry.getTargetAimPoint((Entity)class_13092, this.resolver.isSelected()))) > (double)this.getAttackProgress() : !this.isWithinAttackRange(class_13092)) {
            return false;
        }
        return AttackCriticalHandler.getInstance().allowsAttack(this.getCriticalMode(), class_13092);
    }

    private boolean isWithinRangePrimary(LivingEntity class_13092) {
        float f = this.calculateAttackRotation(class_13092);
        return f <= class_13092.getHealth();
    }

    public boolean isAttackCooldownReady() {
        if (Aura.minecraftClient.player == null) {
            return false;
        }
        if (Aura.minecraftClient.player.isSubmergedInWater() && ServerDetector.isInventoryServer()) {
            return this.isAttackDelayReady();
        }
        return MeleeDamage.isFullStrength(Aura.minecraftClient.player);
    }

    private boolean isAttackDelayReady() {
        if (Aura.minecraftClient.player.getAttackCooldownProgress(0.5f) < 1.0f) {
            this.nextAttackTime = 0L;
            return false;
        }
        if (this.nextAttackTime == 0L) {
            this.nextAttackTime = System.currentTimeMillis();
        }
        long l = Math.round(Math.clamp(this.attackRange, 0.0f, 1.0f) * 200.0f);
        return System.currentTimeMillis() - this.nextAttackTime >= l;
    }

    public float calculateAttackRotation(LivingEntity class_13092) {
        return 0.0f;
    }

    private void attackTarget(LivingEntity class_13092) {
        List<BlockHitResult> list;
        if (this.getWallMode() != WallMode.REACHABLE_WALLS || Aura.minecraftClient.player == null || Aura.minecraftClient.world == null || class_13092 == null) {
            return;
        }
        Rotation rotation = AimRotationMath.calculateAttackRotation(class_13092, this);
        list = this.collectAttackHitPoints(class_13092, rotation);
        if (list.isEmpty()) {
            return;
        }
        for (BlockHitResult class_39652 : list) {
            Direction class_23502 = class_39652.getSide();
            Aura.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, class_39652.getBlockPos(), class_23502));
            Aura.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, class_39652.getBlockPos(), class_23502));
        }
    }

    private List<BlockHitResult> collectAttackHitPoints(LivingEntity class_13092, Rotation rotation) {
        ArrayList<BlockHitResult> arrayList = new ArrayList<BlockHitResult>();
        if (class_13092 == null || Aura.minecraftClient.player == null || Aura.minecraftClient.world == null) {
            return arrayList;
        }
        float f = minecraftClient.getRenderTickCounter().getTickDelta(false);
        Vec3d VanillaChestLootTableGenerator = Aura.minecraftClient.player.getCameraPosVec(f);
        Vec3d WallPlayerSkullBlock = MathUtils.directionFromYawPitch(rotation.getPitch(), rotation.getYaw());
        double d = WallPlayerSkullBlock.lengthSquared();
        if (d < 1.0E-8) {
            return arrayList;
        }
        Vec3d VanillaEntityLootTableGenerator = WallPlayerSkullBlock.multiply(1.0 / Math.sqrt(d));
        double d2 = this.getAttackProgress();
        Vec3d PlayerSkullBlock = VanillaChestLootTableGenerator.add(VanillaEntityLootTableGenerator.multiply(d2));
        double d3 = Aura.calculateRayTraceDistance(VanillaChestLootTableGenerator, VanillaEntityLootTableGenerator, PlayerSkullBlock, d2, class_13092);
        LinkedHashSet<Vec3d> linkedHashSet = new LinkedHashSet<Vec3d>();
        linkedHashSet.add(VanillaChestLootTableGenerator);
        Vec3d RedstoneBlock = Aura.getPosition(VanillaEntityLootTableGenerator);
        if (RedstoneBlock.lengthSquared() > 1.0E-8) {
            RedstoneBlock = RedstoneBlock.normalize().multiply(0.09);
            linkedHashSet.add(VanillaChestLootTableGenerator.add(RedstoneBlock));
            linkedHashSet.add(VanillaChestLootTableGenerator.subtract(RedstoneBlock));
        }
        ArrayList<BlockHitResult> arrayList2 = new ArrayList<BlockHitResult>();
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        for (Vec3d VanillaFishingLootTableGenerator : linkedHashSet) {
            Vec3d LootTableProvider = VanillaFishingLootTableGenerator.add(VanillaEntityLootTableGenerator.multiply(d2));
            for (BlockHitResult class_39653 : this.collect1(VanillaFishingLootTableGenerator, LootTableProvider, VanillaEntityLootTableGenerator, VanillaChestLootTableGenerator, d3)) {
                if (!hashSet.add(class_39653.getBlockPos())) continue;
                arrayList2.add(class_39653);
            }
        }
        arrayList2.sort(Comparator.comparingDouble(class_39652 -> class_39652.getPos().subtract(VanillaChestLootTableGenerator).dotProduct(VanillaEntityLootTableGenerator)));
        arrayList.addAll(arrayList2);
        return arrayList;
    }

    private static Vec3d getPosition(Vec3d VanillaChestLootTableGenerator) {
        Vec3d WallPlayerSkullBlock = new Vec3d(VanillaChestLootTableGenerator.x, 0.0, VanillaChestLootTableGenerator.z);
        if (WallPlayerSkullBlock.lengthSquared() < 1.0E-8) {
            return Vec3d.ZERO;
        }
        WallPlayerSkullBlock = WallPlayerSkullBlock.normalize();
        return new Vec3d(-WallPlayerSkullBlock.z, 0.0, WallPlayerSkullBlock.x);
    }

    private static double calculateRayTraceDistance(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, Vec3d VanillaEntityLootTableGenerator, double d, LivingEntity class_13092) {
        Box HorizontalFacingBlock = class_13092.getBoundingBox();
        Optional optional = HorizontalFacingBlock.raycast(VanillaChestLootTableGenerator, VanillaEntityLootTableGenerator);
        if (optional.isPresent()) {
            return ((Vec3d)optional.get()).subtract(VanillaChestLootTableGenerator).dotProduct(WallPlayerSkullBlock);
        }
        double d2 = class_13092.getEyePos().subtract(VanillaChestLootTableGenerator).dotProduct(WallPlayerSkullBlock);
        if (d2 > 0.0 && d2 <= d) {
            return d2;
        }
        return d;
    }

    private List<BlockHitResult> collect1(Vec3d VanillaChestLootTableGenerator, Vec3d WallPlayerSkullBlock, Vec3d VanillaEntityLootTableGenerator, Vec3d PlayerSkullBlock, double d) {
        BlockHitResult class_39652;
        double d2;
        BlockHitResult class_39653;
        ArrayList<BlockHitResult> arrayList = new ArrayList<BlockHitResult>();
        Vec3d RedstoneBlock = VanillaChestLootTableGenerator;
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        double d3 = 1.0E-4;
        for (int i = 0; i < 40 && (class_39653 = Aura.minecraftClient.world.raycast(new RaycastContext(RedstoneBlock, WallPlayerSkullBlock, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)Aura.minecraftClient.player))).getType() == HitResult.Type.BLOCK && !((d2 = (class_39652 = class_39653).getPos().subtract(PlayerSkullBlock).dotProduct(VanillaEntityLootTableGenerator)) >= d - 1.0E-4); ++i) {
            Block class_22482 = Aura.minecraftClient.world.getBlockState(class_39652.getBlockPos()).getBlock();
            if (class_22482 instanceof DoorBlock || class_22482 instanceof TrapdoorBlock) {
                RedstoneBlock = class_39652.getPos().add(VanillaEntityLootTableGenerator.multiply(0.01));
                continue;
            }
            BlockPos adminsky = class_39652.getBlockPos();
            if (!hashSet.add(adminsky)) {
                RedstoneBlock = class_39652.getPos().add(VanillaEntityLootTableGenerator.multiply(0.02));
                continue;
            }
            arrayList.add(class_39652);
            RedstoneBlock = class_39652.getPos().add(VanillaEntityLootTableGenerator.multiply(0.01));
        }
        return arrayList;
    }

    @Compile(obfuscation=1)
    private void performPrimaryAttack(LivingEntity class_13092) {
        ModeSetting.Option option;
        Object object;
        HotbarSlot hotbarSlot;
        if (Aura.minecraftClient.interactionManager == null || Aura.minecraftClient.player == null) {
            return;
        }
        this.attackTarget(class_13092);
        Hand class_12682 = null;
        this.attackReady = this.isUsingAttackItem();
        if (this.attackReady) {
            class_12682 = Aura.minecraftClient.player.getActiveHand();
            Aura.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
        }
        if (EntityOverlayGeometry.isEntityTargetable(class_13092) && EntityOverlayGeometry.isEntityAlive(class_13092)) {
            EntityOverlayGeometry.isEntityVisible(class_13092);
        }
        HotbarSlot hotbarSlot2 = hotbarSlot = this.isAutoMaceTargetValid(class_13092) && this.isFallingForCritical() ? EntityOverlayGeometry.getAttackHotbarSlot() : null;
        if (hotbarSlot != null && !ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME)) {
            HotbarActionService.withTemporaryHotbarSlot(hotbarSlot, () -> Aura.minecraftClient.interactionManager.attackEntity((PlayerEntity)Aura.minecraftClient.player, (Entity)class_13092));
        } else {
            Aura.minecraftClient.interactionManager.attackEntity((PlayerEntity)Aura.minecraftClient.player, (Entity)class_13092);
        }
        Aura.minecraftClient.player.swingHand(Hand.MAIN_HAND);
        if (this.attackReady && class_12682 != null) {
            object = class_12682;
            Aura.minecraftClient.interactionManager.interactItem(Aura.minecraftClient.player, (Hand)object);
        }
        if ((option = this.rotationMode.getSelectedOption()) instanceof AuraRotationMode) {
            object = (AuraRotationMode)option;
            ((AuraRotationMode)object).onAttack();
        }
        this.currentRotation = new Rotation(MathUtils.interpolateRandomDouble(5.0, 20.0), MathUtils.interpolateRandomDouble(5.0, 10.0));
        this.cooldownTimer.reset();
        this.attackRange = MathUtils.interpolateRandomStrategy(0.0f, 1.0f);
        this.nextAttackTime = 0L;
        ++this.attackCycle;
        this.attackCooldown = this.attackCycle % 9 == 0 ? 0.08f : 0.0f;
    }

    @Compile(obfuscation=1)
    private void performCriticalAttack(LivingEntity class_13092) {
        boolean bl;
        if (this.onlyWeapon.isEnabled() && !EntityUtils.isHoldingMiningTool()) {
            return;
        }
        this.disableLocked = bl = this.forceTargetedRanged.isEnabled() && class_13092 != null && this.isHoldingDefensiveItem(class_13092) && !this.moveCorrectionMode.isSelected(this.targetedMoveCorrection);
        RotationCorrectionMode rotationCorrectionMode = this.moveCorrectionMode.isSelected(this.silentMoveCorrection) ? RotationCorrectionMode.UNSPECIFIED : (this.moveCorrectionMode.isSelected(this.directMoveCorrection) ? RotationCorrectionMode.DIRECT : (this.moveCorrectionMode.isSelected(this.targetedMoveCorrection) || bl ? RotationCorrectionMode.TARGETED : RotationCorrectionMode.NONE));
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        if (this.rotationMode.isSelected(this.noRotation)) {
            if (rotationCorrectionMode == RotationCorrectionMode.TARGETED && class_13092 != null) {
                rotationManager.requestRotation(rotationManager.getPlayerRotation(), RotationCorrectionMode.TARGETED, 180.0f, 180.0f, 180.0f, RotationPriority.TARGET_PRIORITY);
            }
            return;
        }
        Object object = this.rotationMode.getSelectedOption();
        if (object instanceof AuraRotationMode) {
            AuraRotationMode auraRotationMode = (AuraRotationMode)object;
            object = rotationManager.getRotationResolver();
            auraRotationMode.rotate(rotationManager, this.getAttackProgress(), this.getWallMode().usesDirectRaycast(), this.rayTrace.isEnabled(), rotationCorrectionMode, class_13092);
            RotationRequest rotationRequest = rotationManager.getRotationResolver();
            if (rotationRequest != null && rotationRequest != object) {
                rotationRequest.setReturnMode(this.getRotationReturnMode());
                rotationRequest.setRotationStep(null);
            }
        }
    }

    private RotationReturnMode getRotationReturnMode() {
        if (this.returnMode.isSelected(this.noReturnOption)) {
            return RotationReturnMode.NONE;
        }
        if (this.returnMode.isSelected(this.camera)) {
            return RotationReturnMode.CAMERA;
        }
        return RotationReturnMode.SMOOTH;
    }

    public AttackCriticalHandler.Mode getCriticalMode() {
        if (this.onlyCrits != null && this.onlyCrits.isEnabled()) {
            return AttackCriticalHandler.Mode.ONLY;
        }
        if (this.smartCriticals != null && this.smartCriticals.isEnabled()) {
            return AttackCriticalHandler.Mode.PRIORITIZE;
        }
        return AttackCriticalHandler.Mode.NONE;
    }

    private boolean isSmartCriticalReady() {
        return this.getCriticalMode() != AttackCriticalHandler.Mode.NONE;
    }

    private boolean prepareAttackTarget() {
        return false;
    }

    private boolean isEntityValid(LivingEntity class_13092) {
        if (RockstarClient.create().getModuleRegistry().getModule(KnockbackTweaks.class).isEnabled()) {
            return false;
        }
        if (Aura.minecraftClient.player == null) {
            return false;
        }
        if (TargetActionQueue.hasDeferredAction() || TargetActionQueue.isCurrentTarget((Entity)Aura.minecraftClient.player)) {
            return true;
        }
        if (!Aura.minecraftClient.player.isSprinting()) {
            TargetActionQueue.clearTarget((Entity)Aura.minecraftClient.player);
            return false;
        }
        TargetActionQueue.queueTargetAction((PlayerEntity)Aura.minecraftClient.player, () -> this.tryAttackTarget(class_13092), false);
        return true;
    }

    private void tryAttackTarget(LivingEntity class_13092) {
        if (!this.isEnabled() || Aura.minecraftClient.player == null || Aura.minecraftClient.interactionManager == null || class_13092 == null || class_13092.isRemoved() || !class_13092.isAlive()) {
            return;
        }
        if (this.isWithinAttackRange(class_13092) && this.isRaycastPassing(class_13092, true)) {
            this.performPrimaryAttack(class_13092);
        }
    }

    public boolean shouldResetSprint() {
        return false;
    }

    public float getAttackProgress() {
        float f = RockstarClient.create().getModuleRegistry().getModule(AirStuck.class).getAuraDistance();
        return f > 0.0f ? f : this.attackDistance.getValue();
    }

    public boolean isWithinAttackRange(LivingEntity class_13092) {
        return Aura.minecraftClient.player.getEyePos().distanceTo(AimRotationMath.translateAimPoint(class_13092, EntityOverlayGeometry.getTargetAimPoint((Entity)class_13092, this.resolver.isSelected()))) <= (double)this.getAttackProgress();
    }

    @Override
    public void onEnable() {
        ModeSetting.Option option = this.rotationMode.getSelectedOption();
        if (option instanceof AuraRotationMode) {
            AuraRotationMode auraRotationMode = (AuraRotationMode)option;
            auraRotationMode.enabled();
        }
        this.alwaysEnabled = false;
        this.resetAttackState();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.resetAttackState();
        RockstarClient.create().getFriendManager().clearTarget();
        if (Aura.minecraftClient.player != null) {
            TargetActionQueue.clearTarget((Entity)Aura.minecraftClient.player);
        }
        if (this.rotationController != null) {
            this.rotationController.onTargetLost();
        }
        super.onDisable();
    }

    private void performEmergencyAttack(LivingEntity class_13092) {
        if (!ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) || !this.isAutoMaceAttackValid(class_13092)) {
            this.resetAttackState();
            return;
        }
        if (Aura.minecraftClient.player.getMainHandStack().isOf(Items.MACE)) {
            if (Aura.minecraftClient.player.getItemCooldownManager().isCoolingDown(Items.MACE.getDefaultStack())) {
                this.targetSelected = true;
            }
            return;
        }
        HotbarSlot hotbarSlot = EntityOverlayGeometry.getAttackHotbarSlot();
        if (hotbarSlot != null) {
            this.targetIndex = Aura.minecraftClient.player.age;
            this.targetSelected = Aura.minecraftClient.player.getItemCooldownManager().isCoolingDown(hotbarSlot.getItemStack());
        }
    }

    private boolean isAutoMaceAttackValid(LivingEntity class_13092) {
        if (!this.autoMace.isEnabled() || class_13092 == null || Aura.minecraftClient.player == null || Aura.minecraftClient.player.isOnGround() || Aura.minecraftClient.player.isGliding() || Aura.minecraftClient.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            return false;
        }
        if (this.isAutoMaceTargetValid(class_13092)) {
            return true;
        }
        HotbarSlot hotbarSlot = EntityOverlayGeometry.getAttackHotbarSlot();
        if (hotbarSlot == null) {
            return false;
        }
        int n = this.getWeaponSlot(class_13092);
        return n >= 0 && this.getWeaponSlotIndex(hotbarSlot) >= n - 4;
    }

    private int getWeaponSlot(LivingEntity class_13092) {
        double d = Aura.minecraftClient.player.getY();
        double d2 = Aura.minecraftClient.player.getVelocity().y;
        float f = Aura.minecraftClient.player.fallDistance;
        double d3 = class_13092.getBoundingBox().maxY;
        for (int i = 1; i <= 40; ++i) {
            d += d2;
            f = d2 < 0.0 ? (f -= (float)d2) : 0.0f;
            if (d2 < 0.0 && (double)f + Math.max(0.0, d - d3) > 1.5) {
                return i;
            }
            if (d2 < 0.0 && d < class_13092.getBoundingBox().minY - 2.0) {
                return -1;
            }
            d2 = (d2 - 0.08) * 0.98;
        }
        return -1;
    }

    private int getWeaponSlotIndex(HotbarSlot hotbarSlot) {
        if (!Aura.minecraftClient.player.getItemCooldownManager().isCoolingDown(hotbarSlot.getItemStack())) {
            return 0;
        }
        ItemCooldownManagerAccessor itemCooldownManagerAccessor = (ItemCooldownManagerAccessor)Aura.minecraftClient.player.getItemCooldownManager();
        Identifier class_29602 = itemCooldownManagerAccessor.rockstar$getGroup(hotbarSlot.getItemStack());
        Object object = itemCooldownManagerAccessor.rockstar$getEntries().get(class_29602);
        if (object == null) {
            return 0;
        }
        return Math.max(0, ((ItemCooldownEntryAccessor)object).rockstar$getEndTick() - itemCooldownManagerAccessor.rockstar$getTick());
    }

    private void resetAttackState() {
        this.targetIndex = -1;
        this.targetSelected = false;
    }

    private boolean isAutoMaceTargetValid(LivingEntity class_13092) {
        if (!this.autoMace.isEnabled() || class_13092 == null || Aura.minecraftClient.player == null || Aura.minecraftClient.player.isOnGround() || Aura.minecraftClient.player.isGliding() || Aura.minecraftClient.player.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            return false;
        }
        double d = Math.max(0.0, Aura.minecraftClient.player.getY() - class_13092.getBoundingBox().maxY);
        return (double)Aura.minecraftClient.player.fallDistance + d > 1.5;
    }

    private boolean isFallingForCritical() {
        return Aura.minecraftClient.player.fallDistance > 1.5f && Aura.minecraftClient.player.getVelocity().y < 0.0;
    }

    private boolean isMaceReady() {
        if (!Aura.minecraftClient.player.getMainHandStack().isOf(Items.MACE) || Aura.minecraftClient.player.getItemCooldownManager().isCoolingDown(Aura.minecraftClient.player.getMainHandStack())) {
            return false;
        }
        return this.targetIndex < 0 || this.targetSelected || Aura.minecraftClient.player.age - this.targetIndex >= 4;
    }

    private long getAttackCooldown() {
        int n;
        int n2;
        int n3 = (int)this.cpsLimiter.getFirstValue();
        if (n3 > (n2 = (int)this.cpsLimiter.getSecondValue())) {
            n = n3;
            n3 = n2;
            n2 = n;
        }
        n = MathUtils.RANDOM.nextInt(n3, n2 + 1);
        return Math.max(1L, 1000L / (long)n);
    }

    private float getCooldownProgress() {
        if (!this.syncTps.isSelected()) {
            return 1.0f;
        }
        float f = RockstarClient.create().getServerTickRateTracker().getTicksPerSecond();
        if (f <= 0.0f || Float.isNaN(f)) {
            return 1.0f;
        }
        if (f >= 19.0f) {
            return 1.0f;
        }
        return Math.max(25.0f / f, 1.0f);
    }

    private boolean isAttackWeaponReady() {
        if (Aura.minecraftClient.player == null || !Aura.minecraftClient.player.isUsingItem()) {
            return false;
        }
        UseAction class_18392 = Aura.minecraftClient.player.getActiveItem().getItem().getUseAction(Aura.minecraftClient.player.getActiveItem());
        return class_18392 == UseAction.EAT || class_18392 == UseAction.DRINK;
    }

    private boolean isUsingAttackItem() {
        if (Aura.minecraftClient.player == null || !Aura.minecraftClient.player.isUsingItem()) {
            return false;
        }
        UseAction class_18392 = Aura.minecraftClient.player.getActiveItem().getItem().getUseAction(Aura.minecraftClient.player.getActiveItem());
        return class_18392 == UseAction.BLOCK;
    }

    private boolean isUsingItemAttackReady() {
        if (Aura.minecraftClient.player == null || !Aura.minecraftClient.player.isUsingItem()) {
            return false;
        }
        if (!this.useHit.isSelected()) {
            return false;
        }
        if (this.isAttackWeaponReady()) {
            return true;
        }
        return Aura.minecraftClient.player.getActiveHand() == Hand.OFF_HAND && !this.isUsingAttackItem();
    }

    private boolean isHoldingDefensiveItem(LivingEntity class_13092) {
        return class_13092.getMainHandStack().isOf(Items.CROSSBOW) || class_13092.getOffHandStack().isOf(Items.CROSSBOW) || class_13092.getMainHandStack().isOf(Items.TRIDENT) || class_13092.getOffHandStack().isOf(Items.TRIDENT);
    }

    private long getNextAttackTime() {
        return Math.round(500.0f * this.getCooldownProgress());
    }

    @Generated
    public NumberSetting getAttackDistanceSetting() {
        return this.attackDistance;
    }

    @Generated
    public ModeSetting getSortingSetting() {
        return this.rotationMode;
    }

    @Generated
    public BooleanSetting getOnlyCritsSetting() {
        return this.forceTargetedRanged;
    }

    @Generated
    public BooleanSetting getSmartCriticalsSetting() {
        return this.forceBehindTargeted;
    }

    @Generated
    public ModeSetting.Option getRotationModeOption() {
        return this.legacyAttackStyle;
    }

    @Generated
    public ModeSetting.Option getReturnModeOption() {
        return this.modernAttackStyle;
    }

    @Generated
    public RangeSetting getCpsLimiterSetting() {
        return this.cpsLimiter;
    }

    @Generated
    public BooleanSetting getMobsSetting() {
        return this.onlyCrits;
    }

    @Generated
    public BooleanSetting getInvisiblesSetting() {
        return this.smartCriticals;
    }

    @Generated
    public BooleanSetting getNakedPlayersSetting() {
        return this.rayTrace;
    }

    @Generated
    public BooleanSetting getFriendsSetting() {
        return this.onlyWeapon;
    }

    @Generated
    public BooleanSetting getRockUsersSetting() {
        return this.autoMace;
    }

    @Generated
    public BooleanSetting getCameraSetting() {
        return this.targeting;
    }

    @Generated
    public BooleanSetting getNoMoveCorrectionSetting() {
        return this.noHitInv;
    }

    @Generated
    public ModeSetting.Option getOldCriticalsOption() {
        return this.oldCriticalsOption;
    }

    @Generated
    public ModeSetting.Option getNewCriticalsOption() {
        return this.newCriticalsOption;
    }

    @Generated
    public ModeSetting.Option getAirCriticalsOption() {
        return this.airCriticalsOption;
    }

    @Generated
    public MultiBooleanSetting.Option getResolverOption() {
        return this.resolver;
    }

    @Generated
    public MultiBooleanSetting.Option getUseHitOption() {
        return this.useHit;
    }

    @Generated
    public MultiBooleanSetting.Option getDirectMoveCorrection() {
        return this.sync;
    }

    @Generated
    public MultiBooleanSetting.Option getSilentMoveCorrection() {
        return this.syncTps;
    }

    @Generated
    public MultiBooleanSetting.Option getTargetedMoveCorrection() {
        return this.excludeTeammatesOption;
    }

    @Generated
    public Timer getCooldownTimer() {
        return this.cooldownTimer;
    }

    @Generated
    public float getAttackCooldownValue() {
        return this.attackCooldown;
    }

    @Generated
    public BlinkEventListener getPredictionState() {
        return this.predictionState;
    }

    @Generated
    public RotationController getRotationController() {
        return this.rotationController;
    }

    @Generated
    public int getAttackCycle() {
        return this.attackCycle;
    }

    @Generated
    public Rotation getCurrentRotation() {
        return this.currentRotation;
    }

    @Generated
    public void setCurrentRotation(Rotation rotation) {
        this.currentRotation = rotation;
    }

    @Generated
    public boolean isInvisiblesEnabled() {
        return this.disableLocked;
    }

    @Generated
    public Map<String, Integer> getAttackStatistics() {
        return this.attackStatistics;
    }

    private static /* synthetic */ Packet createUseItemPacket(Hand class_12682, int n) {
        return new PlayerInteractItemC2SPacket(class_12682, n, RockstarClient.create().getRotationManager().getCurrentRotation().getYaw(), RockstarClient.create().getRotationManager().getCurrentRotation().getPitch());
    }
}
