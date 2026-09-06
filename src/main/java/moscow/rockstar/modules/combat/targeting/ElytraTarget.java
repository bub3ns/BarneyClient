/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.PlayerEntity
 *  net.minecraft.Items
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.ClientCommandC2SPacket
 *  net.minecraft.ClientCommandC2SPacket$Mode
 */
package moscow.rockstar.modules.combat.targeting;

import java.util.Optional;
import lombok.Generated;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.attacks.Aura;
import moscow.rockstar.modules.combat.rotation.ElytraPrediction;
import moscow.rockstar.modules.movement.flight.ElytraStrafe;
import moscow.rockstar.render.esp.EntityOverlayGeometry;
import moscow.rockstar.render.esp.EntityRenderContext;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import pyrock.events.game.PostAttackEvent;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.player.InputEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Elytra Target", category=ModuleCategory.COMBAT, description="modules.descriptions.elytra_target")
public class ElytraTarget
extends Module {
    private BooleanSetting autoFireworks;
    private BooleanSetting smartFireworks;
    private NumberSetting fireworkSlot;
    private NumberSetting fireworkDelay;
    private NumberSetting engageRange;
    private ModeSetting predictionMode;
    private ModeSetting.Option motionOption;
    private ModeSetting.Option serverPos;
    private NumberSetting leadStrength;
    private BooleanSetting airFreeze;
    private NumberSetting freezeDistance;
    private LivingEntity targetEntity;
    private Vec3d position = Vec3d.ZERO;
    private double predictedDistance = Double.NaN;
    private boolean targetLocked = false;
    private Vec3d previousPosition = Vec3d.ZERO;
    private final EventListener<InputEvent> onInputEvent = inputEvent -> {
        boolean bl;
        if (!this.airFreeze.isEnabled()) {
            this.targetLocked = false;
            this.predictedDistance = Double.NaN;
            this.previousPosition = Vec3d.ZERO;
            return;
        }
        if (ElytraTarget.minecraftClient.player == null || !ElytraTarget.minecraftClient.player.isGliding()) {
            this.targetLocked = false;
            this.predictedDistance = Double.NaN;
            this.previousPosition = Vec3d.ZERO;
            return;
        }
        if (this.targetEntity == null) {
            this.targetLocked = false;
            this.predictedDistance = Double.NaN;
            this.previousPosition = Vec3d.ZERO;
            return;
        }
        boolean bl2 = bl = ElytraTarget.minecraftClient.player.distanceTo((Entity)this.targetEntity) < this.freezeDistance.getValue();
        if (!bl) {
            this.targetLocked = false;
            this.predictedDistance = Double.NaN;
            this.previousPosition = Vec3d.ZERO;
            return;
        }
        double d = ElytraTarget.minecraftClient.player.getY();
        if (ElytraTarget.minecraftClient.player.isOnGround()) {
            this.predictedDistance = Double.NaN;
            this.targetLocked = false;
        } else if (!this.targetLocked) {
            if (Double.isNaN(this.predictedDistance)) {
                this.predictedDistance = d;
            } else if (d > this.predictedDistance) {
                this.predictedDistance = d;
            } else if (d < this.predictedDistance) {
                this.targetLocked = true;
                this.previousPosition = ElytraTarget.minecraftClient.player.getPos();
                this.predictedDistance = d;
            }
        }
        if (this.targetLocked) {
            inputEvent.setForward(0.0f);
            inputEvent.setStrafe(0.0f);
            ElytraTarget.minecraftClient.player.setVelocity(Vec3d.ZERO);
            ElytraTarget.minecraftClient.player.setPosition(this.previousPosition);
        }
    };
    private final EventListener<PostAttackEvent> onPostAttackEvent = postAttackEvent -> {
        long l;
        LivingEntity class_13092;
        if (!this.hasElytraInChestSlot()) {
            return;
        }
        if (EntityOverlayGeometry.getAttackHotbarSlot() != null) {
            EntityRenderContext.useTargetingItem(false);
            if (ElytraTarget.minecraftClient.player.isSprinting() && ElytraTarget.minecraftClient.player.input.hasForwardMovement() && ElytraTarget.minecraftClient.player.checkGliding()) {
                minecraftClient.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)ElytraTarget.minecraftClient.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }
        if ((class_13092 = Optional.ofNullable(this.targetEntity).orElseGet(this::findTargetEntity)) != null) {
            EntityRenderContext.setLastVisibleOffset(EntityRenderContext.findVisibleAimOffset(class_13092));
        }
        if (this.autoFireworks.isEnabled() && this.isPredictionCurrent(l = this.calculateFlightTicks(class_13092, class_13092 != null ? (double)ElytraTarget.minecraftClient.player.distanceTo((Entity)class_13092) : Double.MAX_VALUE))) {
            EntityRenderContext.attackTargetWithItem(this.fireworkSlot.getValue());
        }
    };
    private final EventListener<WorldChangeEvent> onWorldChangeEvent = worldChangeEvent -> this.disable();

    public ElytraTarget() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.autoFireworks = new BooleanSetting(this, "modules.settings.elytra_target.auto_fireworks").enable();
        this.smartFireworks = new BooleanSetting((SettingOwner)this, "modules.settings.elytra_target.smart_fireworks", () -> !this.autoFireworks.isEnabled()).enable();
        this.fireworkSlot = new NumberSetting((SettingOwner)this, "modules.settings.elytra_target.fireworkSlot", () -> !this.autoFireworks.isEnabled()).setMinValue(1.0f).setMaxValue(9.0f).setStep(1.0f).setValue(7.0f).setUnit(" slot");
        this.fireworkDelay = new NumberSetting((SettingOwner)this, "modules.settings.elytra_target.fireworkDelay", () -> !this.autoFireworks.isEnabled() && this.smartFireworks.isEnabled()).setMinValue(0.25f).setMaxValue(3.0f).setStep(0.05f).setValue(0.45f).setUnit(" s");
        this.engageRange = new NumberSetting(this, "modules.settings.elytra_target.engageRange").setMinValue(6.0f).setMaxValue(50.0f).setStep(1.0f).setValue(24.0f).setUnit(" blocks");
        this.predictionMode = new ModeSetting((SettingOwner)this, "modules.settings.elytra_target.prediction_mode", "motion");
        this.motionOption = new ModeSetting.Option(this.predictionMode, "modules.settings.elytra_target.prediction_mode.motion");
        this.serverPos = new ModeSetting.Option(this.predictionMode, "modules.settings.elytra_target.prediction_mode.server_pos").select();
        this.leadStrength = new NumberSetting((SettingOwner)this, "modules.settings.elytra_target.lead_strength", () -> !this.predictionMode.isSelected(this.motionOption)).setMinValue(0.0f).setMaxValue(5.0f).setStep(0.1f).setValue(3.0f).setUnit(" ticks");
        this.airFreeze = new BooleanSetting(this, "modules.settings.elytra_target.air_freeze");
        this.freezeDistance = new NumberSetting((SettingOwner)this, "modules.settings.elytra_target.freeze_distance", () -> !this.airFreeze.isEnabled()).setMinValue(1.0f).setMaxValue(10.0f).setStep(0.1f).setValue(3.0f).setUnit(" blocks");
    }

    @Override
    public void onTick() {
        if (!RockstarClient.create().getModuleRegistry().getModule(Aura.class).isEnabled()) {
            this.clearPrediction();
            return;
        }
        if (!this.hasElytraInChestSlot()) {
            this.clearPrediction();
            return;
        }
        this.targetEntity = this.findTargetEntity();
        this.selectTarget(this.targetEntity);
        if (this.autoFireworks.isEnabled()) {
            this.updateTargetMotion(this.targetEntity);
        }
        if (this.targetEntity != null) {
            this.clearTarget();
        }
    }

    private LivingEntity findTargetEntity() {
        LivingEntity class_13092 = RockstarClient.create().getFriendManager().getTargetLivingEntity();
        return class_13092 instanceof PlayerEntity ? class_13092 : null;
    }

    private void updateTargetMotion(LivingEntity class_13092) {
        PlayerEntity class_16572;
        if (ElytraTarget.minecraftClient.player == null || !ElytraTarget.minecraftClient.player.isGliding()) {
            return;
        }
        if (RockstarClient.create().getModuleRegistry().getModule(ElytraStrafe.class).isEnabled()) {
            return;
        }
        double d = class_13092 == null ? Double.MAX_VALUE : (double)ElytraTarget.minecraftClient.player.distanceTo((Entity)class_13092);
        boolean bl2 = class_13092 instanceof PlayerEntity && this.isEntityValid(class_16572 = (PlayerEntity)class_13092);
        boolean bl3 = ElytraTarget.minecraftClient.player.getY() < (class_13092 != null ? class_13092.getY() + 3.0 : ElytraTarget.minecraftClient.player.getY() + 5.0);
        long l = this.calculateFlightTicks(class_13092, d);
        boolean fireworksReady = this.smartFireworks.isEnabled() ? d > 15.0 || bl2 || this.isVerticalTargetReady() || bl3 : d > 15.0 || bl2;
        if (fireworksReady && this.isPredictionCurrent(l)) {
            EntityRenderContext.attackTargetWithItem(this.fireworkSlot.getValue());
        }
    }

    private long calculateFlightTicks(LivingEntity class_13092, double d) {
        PlayerEntity class_16572;
        long l = (long)(this.fireworkDelay.getValue() * 1000.0f);
        if (!this.smartFireworks.isEnabled()) {
            return l;
        }
        if (class_13092 instanceof PlayerEntity && this.isEntityValid(class_16572 = (PlayerEntity)class_13092)) {
            return (long)((float)l * 0.68f);
        }
        if (d < 8.0) {
            return (long)((float)l * 1.35f);
        }
        if (this.isVerticalTargetReady()) {
            return (long)((float)l * 0.78f);
        }
        return l;
    }

    private boolean isVerticalTargetReady() {
        return ElytraTarget.minecraftClient.player.getY() < (this.targetEntity != null ? this.targetEntity.getY() + 2.0 : (double)ElytraTarget.minecraftClient.world.getSeaLevel());
    }

    private void selectTarget(LivingEntity class_13092) {
        if (class_13092 == null || !ElytraTarget.minecraftClient.player.isGliding()) {
            this.position = Vec3d.ZERO;
            return;
        }
        Vec3d VanillaChestLootTableGenerator = ElytraPrediction.predictEntityPosition(class_13092, this.predictionMode, this.motionOption, this.serverPos, this.leadStrength.getValue());
        if (VanillaChestLootTableGenerator == null) {
            this.position = Vec3d.ZERO;
            return;
        }
        this.position = VanillaChestLootTableGenerator;
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        Rotation rotation = AimRotationMath.getRotationToPoint(this.position);
        rotationManager.requestRotation(rotation, RotationCorrectionMode.UNSPECIFIED, 180.0f, 180.0f, 180.0f, RotationPriority.TARGET_PRIORITY);
    }

    private void clearTarget() {
        if (InventoryUtils.chestplateRule().getItem() == Items.ELYTRA && ElytraTarget.minecraftClient.player.isSprinting() && ElytraTarget.minecraftClient.player.input.hasForwardMovement() && ElytraTarget.minecraftClient.player.checkGliding()) {
            minecraftClient.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)ElytraTarget.minecraftClient.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    private boolean isEntityValid(PlayerEntity class_16572) {
        double d;
        double d2 = ElytraTarget.minecraftClient.player.getVelocity().horizontalLengthSquared();
        return d2 + 1.0E-4 < (d = class_16572.getVelocity().horizontalLengthSquared());
    }

    private boolean isPredictionCurrent(long l) {
        return EntityRenderContext.getAttackCooldownTimer().hasElapsed(l);
    }

    private boolean hasElytraInChestSlot() {
        return ElytraTarget.minecraftClient.player.getInventory().getArmorStack(2).getItem() == Items.ELYTRA;
    }

    private void clearPrediction() {
        this.targetEntity = null;
        this.position = Vec3d.ZERO;
    }

    @Override
    public void onDisable() {
        this.clearPrediction();
    }

    @Generated
    public BooleanSetting getAutoFireworks() {
        return this.autoFireworks;
    }

    @Generated
    public BooleanSetting getSmartFireworks() {
        return this.smartFireworks;
    }

    @Generated
    public NumberSetting getFireworkSlot() {
        return this.fireworkSlot;
    }

    @Generated
    public NumberSetting getFireworkDelaySetting() {
        return this.fireworkDelay;
    }

    @Generated
    public NumberSetting getEngageRange() {
        return this.engageRange;
    }

    @Generated
    public ModeSetting getPredictionMode() {
        return this.predictionMode;
    }

    @Generated
    public ModeSetting.Option getMotionOption() {
        return this.motionOption;
    }

    @Generated
    public ModeSetting.Option getServerPositionOption() {
        return this.serverPos;
    }

    @Generated
    public NumberSetting getLeadStrength() {
        return this.leadStrength;
    }

    @Generated
    public BooleanSetting getAirFreezeSetting() {
        return this.airFreeze;
    }

    @Generated
    public NumberSetting getFreezeDistance() {
        return this.freezeDistance;
    }

    @Generated
    public LivingEntity getTargetEntity() {
        return this.targetEntity;
    }

    @Generated
    public double getPredictedDistance() {
        return this.predictedDistance;
    }

    @Generated
    public boolean isTargetLocked() {
        return this.targetLocked;
    }

    @Generated
    public Vec3d getPreviousPosition() {
        return this.previousPosition;
    }

    @Generated
    public EventListener<InputEvent> getInputListener() {
        return this.onInputEvent;
    }

    @Generated
    public EventListener<PostAttackEvent> getPostAttackListener() {
        return this.onPostAttackEvent;
    }

    @Generated
    public EventListener<WorldChangeEvent> getWorldChangeListener() {
        return this.onWorldChangeEvent;
    }

    @Generated
    public Vec3d getPosition() {
        return this.position;
    }
}
