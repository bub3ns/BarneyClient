/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Hand
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.SharedConstants
 *  net.minecraft.Text
 *  net.minecraft.Packet
 *  net.minecraft.PlayerMoveC2SPacket$Full
 *  net.minecraft.PlayerInteractItemC2SPacket
 */
package moscow.rockstar.modules.combat.aura.rotation;

import lombok.Generated;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.WallMode;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.combat.rotation.RotationType;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.combat.aura.rotation.AuraRotationMode;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.SharedConstants;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import ua.mintantileak.spk.Compile;

public class ReallyWorldRotationMode
extends AuraRotationMode {
    private static final int MIN_SUPPORTED_VERSION = 755;
    private static final int MAX_SUPPORTED_VERSION = 765;
    private Rotation serverRotation = Rotation.ZERO_ROTATION;
    private int lastTargetId = -1;
    private boolean rotationLocked;
    private boolean specialRotationActive;
    private boolean microJitterPositive;

    public ReallyWorldRotationMode(ModeSetting modeSetting) {
        super(modeSetting, "ReallyWorld");
    }

    @Override
    @Compile(obfuscation=1)
    public void rotate(RotationManager rotationManager, float f, boolean bl, boolean bl2, RotationCorrectionMode rotationCorrectionMode, LivingEntity class_13092) {
        Rotation rotation;
        if (!this.isSupportedServerVersion()) {
            Notification.error(Text.of((String)"\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0432\u0435\u0440\u0441\u0438\u044e \u043e\u0442 1.17 \u0434\u043e 1.20.4"));
            this.aura().disable();
            this.specialRotationActive = false;
            this.clearRotationLock();
            return;
        }
        this.serverRotation = rotation = AimRotationMath.calculateAttackRotation(class_13092, this.aura());
        this.specialRotationActive = this.shouldUseSpecialRotation(class_13092);
        if (this.specialRotationActive) {
            this.synchronizeRotationManager(rotationManager);
            this.applyMicroJitter();
            return;
        }
        this.clearRotationLock();
        rotationManager.requestRotation(rotation, rotationCorrectionMode, 180.0f, 180.0f, 180.0f, RotationPriority.TARGET_PRIORITY);
    }

    @Override
    public boolean canAttack() {
        LivingEntity class_13092;
        LivingEntity class_13093;
        Entity class_12972 = RockstarClient.create().getFriendManager().getTargetEntity();
        LivingEntity class_13094 = class_13093 = class_12972 instanceof LivingEntity ? (class_13092 = (LivingEntity)class_12972) : null;
        if (ReallyWorldRotationMode.minecraftClient.player == null || ReallyWorldRotationMode.minecraftClient.world == null || class_13093 == null) {
            this.clearRotationLock();
            return false;
        }
        this.specialRotationActive = this.shouldUseSpecialRotation(class_13093);
        if (!this.specialRotationActive) {
            this.clearRotationLock();
            return true;
        }
        if (this.rotationLocked && this.lastTargetId == class_13093.getId()) {
            if (this.isRotationValid(class_13093)) {
                this.sendRotationAndAttack();
                return true;
            }
            this.updateTargetRotation(class_13093);
            return false;
        }
        this.updateTargetRotation(class_13093);
        return false;
    }

    @Override
    public void onAttack() {
        this.clearRotationLock();
    }

    @Override
    public void onTargetLost() {
        this.specialRotationActive = false;
        this.clearRotationLock();
    }

    public boolean isSpecialRotationActive() {
        return this.specialRotationActive;
    }

    public boolean isRotationValid(LivingEntity class_13092) {
        return this.specialRotationActive && (MathUtils.isRotationPathClear(this.aura().getAttackDistanceSetting().getValue(), this.serverRotation.getYaw(), this.serverRotation.getPitch(), (Entity)ReallyWorldRotationMode.minecraftClient.player, (Entity)class_13092, this.aura().getWallMode()) || !this.aura().getNakedPlayersSetting().isEnabled() || this.aura().getPredictionState().getSavedPlayerState() != null && this.aura().getPredictionState().getAttackCount() > 1);
    }

    private boolean shouldUseSpecialRotation(LivingEntity class_13092) {
        if (class_13092 == null || ReallyWorldRotationMode.minecraftClient.player == null) {
            return false;
        }
        Rotation rotation = AimRotationMath.calculateAttackRotation(class_13092, this.aura());
        return !MathUtils.isRotationPathClear(this.aura().getAttackDistanceSetting().getValue(), rotation.getYaw(), rotation.getPitch(), (Entity)ReallyWorldRotationMode.minecraftClient.player, (Entity)class_13092, WallMode.NONE);
    }

    private void updateTargetRotation(LivingEntity class_13092) {
        Rotation rotation = AimRotationMath.calculateAttackRotation(class_13092, this.aura());
        this.serverRotation = rotation = AimRotationMath.snapRotationToMouseStep(RockstarClient.create().getRotationManager().getPlayerRotation(), rotation);
        this.lastTargetId = class_13092.getId();
        this.rotationLocked = true;
        this.sendRotationAndAttack();
    }

    private void sendRotationAndAttack() {
        ReallyWorldRotationMode.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.Full(ReallyWorldRotationMode.minecraftClient.player.getX(), ReallyWorldRotationMode.minecraftClient.player.getY(), ReallyWorldRotationMode.minecraftClient.player.getZ(), this.serverRotation.getYaw(), this.serverRotation.getPitch(), ReallyWorldRotationMode.minecraftClient.player.isOnGround(), ReallyWorldRotationMode.minecraftClient.player.horizontalCollision));
        ReallyWorldRotationMode.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, this.serverRotation.getYaw(), this.serverRotation.getPitch()));
    }

    private void applyMicroJitter() {
        this.microJitterPositive = !this.microJitterPositive;
        ReallyWorldRotationMode.minecraftClient.player.setYaw(ReallyWorldRotationMode.minecraftClient.player.getYaw() + (this.microJitterPositive ? 0.1f : -0.1f));
    }

    private void synchronizeRotationManager(RotationManager rotationManager) {
        Rotation rotation = rotationManager.getPlayerRotation();
        rotationManager.setRotationResolver(null);
        rotationManager.setRotationState(RotationType.IDLE);
        rotationManager.setCurrentRotation(rotation);
        rotationManager.setPreviousRotation(rotation);
        rotationManager.setAppliedRotation(rotation);
    }

    private void clearRotationLock() {
        this.rotationLocked = false;
        this.lastTargetId = -1;
    }

    private boolean isSupportedServerVersion() {
        int n = this.getServerProtocolVersion();
        return n >= 755 && n <= 765;
    }

    private int getServerProtocolVersion() {
        Integer n = this.getViaFabricTargetVersion();
        if (n != null) {
            return n;
        }
        return SharedConstants.getGameVersion().getProtocolVersion();
    }

    private Integer getViaFabricTargetVersion() {
        try {
            Integer n;
            Class<?> clazz = Class.forName("com.viaversion.viafabricplus.ViaFabricPlus");
            Object object = clazz.getMethod("getImpl", new Class[0]).invoke(null, new Object[0]);
            Object object2 = object.getClass().getMethod("getTargetVersion", new Class[0]).invoke(object, new Object[0]);
            Object object3 = object2.getClass().getMethod("getVersion", new Class[0]).invoke(object2, new Object[0]);
            return object3 instanceof Integer ? (n = (Integer)object3) : null;
        }
        catch (LinkageError | ReflectiveOperationException | RuntimeException throwable) {
            return null;
        }
    }

    @Generated
    public Rotation getRotation() {
        return this.serverRotation;
    }
}

