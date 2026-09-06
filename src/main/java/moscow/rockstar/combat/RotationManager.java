/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Packet
 *  net.minecraft.PlayerInteractItemC2SPacket
 *  net.minecraft.MathHelper
 *  org.jetbrains.annotations.ApiStatus$Internal
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package moscow.rockstar.combat;

import lombok.Generated;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.combat.rotation.RotationRequest;
import moscow.rockstar.combat.rotation.RotationReturnMode;
import moscow.rockstar.combat.rotation.RotationType;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.events.player.PlayerInputBridge;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pyrock.events.network.SendPacketEvent;
import ua.mintantileak.spk.Compile;

public class RotationManager
implements ClientAccess {
    private final PlayerInputBridge inputBridge;
    private Rotation currentRotation = Rotation.ZERO_ROTATION;
    private final Rotation packetRotation = Rotation.ZERO_ROTATION;
    private Rotation previousRotation = Rotation.ZERO_ROTATION;
    private Rotation appliedRotation = Rotation.ZERO_ROTATION;
    private RotationType rotationState = RotationType.IDLE;
    @Nullable
    private RotationRequest rotationResolver;
    private final Timer rotationTimer = new Timer();
    @Nullable
    private Rotation lastSentRotation;
    private final EventListener<SendPacketEvent> packetSendListener = sendPacketEvent -> {
        Packet<?> class_25962;
        if (this.isIdle() || !((class_25962 = sendPacketEvent.getPacket()) instanceof PlayerInteractItemC2SPacket)) {
            return;
        }
        PlayerInteractItemC2SPacket class_28862 = (PlayerInteractItemC2SPacket)class_25962;
        if (this.lastSentRotation == null) {
            this.lastSentRotation = new Rotation(this.currentRotation.getYaw(), this.currentRotation.getPitch());
        }
        this.packetRotation.setYaw(this.lastSentRotation.getYaw());
        this.packetRotation.setPitch(this.lastSentRotation.getPitch());
        if (class_28862.getYaw() != this.lastSentRotation.getYaw() || class_28862.getPitch() != this.lastSentRotation.getPitch()) {
            sendPacketEvent.setPacket((Packet<?>)new PlayerInteractItemC2SPacket(class_28862.getHand(), class_28862.getSequence(), this.lastSentRotation.getYaw(), this.lastSentRotation.getPitch()));
        }
    };

    public RotationManager(PlayerInputBridge playerInputBridge) {
        this.inputBridge = playerInputBridge;
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    @Compile(obfuscation=1)
    public boolean isIdle() {
        return this.rotationState == RotationType.IDLE;
    }

    @ApiStatus.Internal
    @Compile(obfuscation=1)
    public void updateRotation() {
        this.previousRotation = this.currentRotation;
        if (this.rotationResolver == null) {
            this.currentRotation = this.getPlayerRotation();
            return;
        }
        if (this.rotationTimer.hasElapsed(70L)) {
            RotationReturnMode rotationReturnMode = this.rotationResolver.getReturnMode();
            if (rotationReturnMode == RotationReturnMode.CAMERA) {
                float f = Math.clamp(this.currentRotation.getPitch(), -90.0f, 90.0f);
                float f2 = RotationManager.applyPlayerRotation(this.currentRotation.getYaw(), f);
                this.currentRotation = new Rotation(f2, f);
                this.rotationState = RotationType.IDLE;
                this.rotationResolver = null;
                return;
            }
            if (rotationReturnMode == RotationReturnMode.NONE) {
                this.currentRotation = this.getPlayerRotation();
                this.rotationState = RotationType.IDLE;
                this.rotationResolver = null;
                return;
            }
            if (this.getPlayerRotation().angleDistanceTo(this.currentRotation) < Math.max(0.1f, AimRotationMath.getMouseRotationStep())) {
                RotationManager.applyPlayerRotation(this.currentRotation.getYaw(), this.currentRotation.getPitch());
                this.rotationState = RotationType.IDLE;
                this.rotationResolver = null;
            } else {
                Rotation rotation;
                this.rotationState = RotationType.APPLYING;
                RotationManager.minecraftClient.player.setYaw(AimRotationMath.snapYawToMouseStep(RotationManager.minecraftClient.player.getYaw(), AimRotationMath.getNearestWrappedYaw(this.currentRotation.getYaw(), RotationManager.minecraftClient.player.getYaw())));
                Rotation rotation2 = rotation = this.rotationResolver.getRotationStep() == null ? null : this.rotationResolver.getRotationStep().returnStep(this.currentRotation, this.getPlayerRotation());
                if (rotation == null) {
                    float f = 5.0f;
                    float f3 = 88.0f;
                    if (ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY)) {
                        f3 = 45.0f;
                    }
                    rotation = new Rotation(RotationManager.stepRotationToward(this.currentRotation.getYaw(), this.getPlayerRotation().getYaw(), MathUtils.interpolateRandomStrategy(f, f3)), RotationManager.stepRotationToward(this.currentRotation.getPitch(), this.getPlayerRotation().getPitch(), MathUtils.interpolateRandomStrategy(f, f3) / MathUtils.interpolateRandomStrategy(1.9f, 2.2f)));
                }
                this.currentRotation = AimRotationMath.snapRotationToMouseStep(this.currentRotation, rotation);
            }
            return;
        }
        this.rotationState = RotationType.PENDING;
        this.applyPendingRotation();
    }

    @Compile(obfuscation=1)
    public void updateAppliedRotation(float f) {
        if (RotationManager.minecraftClient.player == null) {
            return;
        }
        float f2 = MathUtils.interpolateDouble(this.previousRotation.getYaw(), this.currentRotation.getYaw(), f);
        float f3 = this.previousRotation.getPitch() + (this.currentRotation.getPitch() - this.previousRotation.getPitch()) * f;
        if (f3 <= -85.0f) {
            // empty if block
        }
        this.appliedRotation = new Rotation(f2, f3);
        if (RockstarClient.create().getFriendManager().getTargetEntity() != null) {
            // empty if block
        }
    }

    @Compile(obfuscation=1)
    public void requestRotation(Rotation rotation, RotationCorrectionMode rotationCorrectionMode, float f, float f2, float f3, RotationPriority rotationPriority) {
        this.requestRotationInternal(rotation, rotationCorrectionMode, f, f2, f3, rotationPriority, true);
    }

    @Compile(obfuscation=1)
    public void requestRotationInternal(Rotation rotation, RotationCorrectionMode rotationCorrectionMode, float f, float f2, float f3, RotationPriority rotationPriority, boolean bl) {
        int n = rotationPriority.getPriorityValue();
        if (this.rotationResolver == null || this.rotationResolver.getPriority() <= n || this.rotationState != RotationType.PENDING) {
            rotation.setYaw(AimRotationMath.getNearestWrappedYaw(this.rotationResolver == null ? this.getPlayerRotation().getYaw() : this.rotationResolver.getTargetRotation().getYaw(), rotation.getYaw()));
            this.rotationResolver = new RotationRequest(rotation, rotationCorrectionMode, f, f2, f3, n, bl);
            this.rotationTimer.reset();
            this.rotationState = RotationType.PENDING;
            this.applyPendingRotation();
        }
    }

    @Compile(obfuscation=1)
    public void requestStandardRotation(Rotation rotation, RotationCorrectionMode rotationCorrectionMode, float f, float f2, float f3) {
        this.requestRotation(rotation, rotationCorrectionMode, f, f2, f3, RotationPriority.STANDARD_PRIORITY);
    }

    @Compile(obfuscation=1)
    public void requestRotationWithPriority(Rotation rotation, RotationPriority rotationPriority) {
        this.requestRotation(rotation, RotationCorrectionMode.DIRECT, 180.0f, 180.0f, 180.0f, rotationPriority);
    }

    @Compile(obfuscation=1)
    public void setRotation(Rotation rotation) {
        this.requestRotation(rotation, RotationCorrectionMode.DIRECT, 180.0f, 180.0f, 180.0f, RotationPriority.STANDARD_PRIORITY);
    }

    @Compile(obfuscation=1)
    public void refreshPendingRotation() {
        if (this.rotationResolver != null && this.rotationState == RotationType.PENDING) {
            this.rotationTimer.reset();
        }
    }

    public static float applyPlayerRotation(float f, float f2) {
        if (RotationManager.minecraftClient.player == null) {
            return f;
        }
        float f3 = RotationManager.minecraftClient.player.getYaw() + MathHelper.wrapDegrees((float)(f - RotationManager.minecraftClient.player.getYaw()));
        float f4 = MathHelper.clamp((float)f2, (float)-90.0f, (float)90.0f);
        RotationManager.minecraftClient.player.setYaw(f3);
        RotationManager.minecraftClient.player.setPitch(f4);
        RotationManager.minecraftClient.player.prevYaw = f3;
        RotationManager.minecraftClient.player.prevPitch = f4;
        return f3;
    }

    @Compile(obfuscation=1)
    public static float stepRotationToward(float f, float f2, float f3) {
        float f4 = AimRotationMath.getWrappedAngleDifference(f, f2);
        if (Math.abs(f4) <= f3) {
            return f + f4;
        }
        return f + Math.signum(f4) * f3;
    }

    @Compile(obfuscation=1)
    private void applyPendingRotation() {
        if (this.rotationResolver == null) {
            return;
        }
        Rotation rotation = new Rotation(RotationManager.stepRotationToward(this.currentRotation.getYaw(), this.rotationResolver.getTargetRotation().getYaw(), this.rotationResolver.getYawStep()), RotationManager.stepRotationToward(this.currentRotation.getPitch(), this.rotationResolver.getTargetRotation().getPitch(), this.rotationResolver.getPitchStep()));
        this.currentRotation = this.rotationResolver.isSnapToMouse() ? AimRotationMath.snapRotationToMouseStep(this.currentRotation, rotation) : rotation;
    }

    @Compile(obfuscation=1)
    public void requestEntityRotation(Entity class_12972, long l, long l2, long l3, RotationPriority rotationPriority, RotationCorrectionMode rotationCorrectionMode) {
        if (class_12972 == null || RotationManager.minecraftClient.player == null) {
            return;
        }
        double d = class_12972.getX();
        double d2 = class_12972.getY() + (double)class_12972.getEyeHeight(class_12972.getPose());
        double d3 = class_12972.getZ();
        double d4 = d - RotationManager.minecraftClient.player.getX();
        double d5 = d2 - (RotationManager.minecraftClient.player.getY() + (double)RotationManager.minecraftClient.player.getEyeHeight(RotationManager.minecraftClient.player.getPose()));
        Rotation rotation = RotationManager.calculateRotationToPoint(d3, d4, d5);
        this.requestRotation(rotation, rotationCorrectionMode, l, l2, l3, rotationPriority);
    }

    @Compile(obfuscation=1)
    @NotNull
    private static Rotation calculateRotationToPoint(double d, double d2, double d3) {
        double d4 = d - RotationManager.minecraftClient.player.getZ();
        double d5 = Math.sqrt(d2 * d2 + d4 * d4);
        float f = (float)Math.toDegrees(Math.atan2(d4, d2)) - 90.0f;
        float f2 = (float)(-Math.toDegrees(Math.atan2(d3, d5)));
        Rotation rotation = new Rotation(f, f2);
        return rotation;
    }

    @Compile(obfuscation=1)
    public Rotation getEntityRotation(LivingEntity class_13092) {
        return new Rotation(class_13092.getYaw(), class_13092.getPitch());
    }

    @Compile(obfuscation=1)
    public Rotation getPlayerRotation() {
        if (RotationManager.minecraftClient.player == null) {
            return Rotation.ZERO_ROTATION;
        }
        return this.getEntityRotation((LivingEntity)RotationManager.minecraftClient.player);
    }

    @Compile(obfuscation=1)
    public Rotation getEffectiveRotation() {
        return this.rotationState == RotationType.IDLE ? this.getPlayerRotation() : this.getCurrentRotation();
    }

    @Generated
    public PlayerInputBridge getInputBridge() {
        return this.inputBridge;
    }

    @Generated
    public Rotation getCurrentRotation() {
        return this.currentRotation;
    }

    @Generated
    public Rotation getPacketRotation() {
        return this.packetRotation;
    }

    @Generated
    public Rotation getPreviousRotation() {
        return this.previousRotation;
    }

    @Generated
    public Rotation getAppliedRotation() {
        return this.appliedRotation;
    }

    @Generated
    public RotationType getRotationState() {
        return this.rotationState;
    }

    @Generated
    public Timer getRotationTimer() {
        return this.rotationTimer;
    }

    @Generated
    public EventListener<SendPacketEvent> getPacketSendListener() {
        return this.packetSendListener;
    }

    @Generated
    public void setCurrentRotation(Rotation rotation) {
        this.currentRotation = rotation;
    }

    @Generated
    public void setPreviousRotation(Rotation rotation) {
        this.previousRotation = rotation;
    }

    @Generated
    public void setAppliedRotation(Rotation rotation) {
        this.appliedRotation = rotation;
    }

    @Generated
    public void setRotationState(RotationType rotationType) {
        this.rotationState = rotationType;
    }

    @Generated
    public void setRotationResolver(@Nullable RotationRequest rotationRequest) {
        this.rotationResolver = rotationRequest;
    }

    @Nullable
    @Generated
    public RotationRequest getRotationResolver() {
        return this.rotationResolver;
    }

    @Nullable
    @Generated
    public Rotation getLastSentRotation() {
        return this.lastSentRotation;
    }

    @Generated
    public void setLastSentRotation(@Nullable Rotation rotation) {
        this.lastSentRotation = rotation;
    }
}

