package moscow.rockstar.combat;

import lombok.Generated;
import moscow.rockstar.api.rotation.ContinuousRotationMath;
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
        PlayerInteractItemC2SPacket class_28862 = (PlayerInteractItemC2SPacket) class_25962;
        if (this.lastSentRotation == null) {
            this.lastSentRotation = new Rotation(this.currentRotation.getYaw(), this.currentRotation.getPitch());
        }
        this.packetRotation.setYaw(this.lastSentRotation.getYaw());
        this.packetRotation.setPitch(this.lastSentRotation.getPitch());
        if (class_28862.getYaw() != this.lastSentRotation.getYaw()
                || class_28862.getPitch() != this.lastSentRotation.getPitch()) {
            sendPacketEvent.setPacket((Packet<?>) new PlayerInteractItemC2SPacket(class_28862.getHand(),
                    class_28862.getSequence(), this.lastSentRotation.getYaw(), this.lastSentRotation.getPitch()));
        }
    };

    public RotationManager(PlayerInputBridge playerInputBridge) {
        this.inputBridge = playerInputBridge;
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    @Compile(obfuscation = 1)
    public boolean isIdle() {
        return this.rotationState == RotationType.IDLE;
    }

    @ApiStatus.Internal
    @Compile(obfuscation = 1)
    public void updateRotation() {
        this.previousRotation = new Rotation(this.currentRotation.getYaw(), this.currentRotation.getPitch());

        if (RotationManager.minecraftClient.player == null) {
            return;
        }

        Rotation playerRotation = this.getPlayerRotation();

        // 1. IDLE STATE: Keep currentRotation unwrapped and continuous with the player
        // view
        if (this.rotationResolver == null) {
            this.rotationState = RotationType.IDLE;
            float unwrappedPlayerYaw = ContinuousRotationMath.unwrapYaw(this.currentRotation.getYaw(),
                    playerRotation.getYaw());
            this.currentRotation = new Rotation(unwrappedPlayerYaw, playerRotation.getPitch());
            return;
        }

        // 2. TIMEOUT / RETURN STATE: The active rotation request has expired or
        // finished
        if (this.rotationTimer.hasElapsed(70L)) {
            RotationReturnMode returnMode = this.rotationResolver.getReturnMode();

            // Return Mode: CAMERA (Force-snaps camera directly to rotation)
            if (returnMode == RotationReturnMode.CAMERA) {
                float pitch = MathHelper.clamp(this.currentRotation.getPitch(), -90.0f, 90.0f);
                float yaw = RotationManager.applyPlayerRotation(this.currentRotation.getYaw(), pitch);
                this.currentRotation = new Rotation(yaw, pitch);
                this.rotationState = RotationType.IDLE;
                this.rotationResolver = null;
                return;
            }

            // Return Mode: NONE (Immediately finish, keeping continuous yaw without delta
            // spikes)
            if (returnMode == RotationReturnMode.NONE) {
                float unwrappedPlayerYaw = ContinuousRotationMath.unwrapYaw(this.currentRotation.getYaw(),
                        playerRotation.getYaw());
                this.currentRotation = new Rotation(unwrappedPlayerYaw, playerRotation.getPitch());
                this.syncPlayerYawRevolutions(unwrappedPlayerYaw);
                this.rotationState = RotationType.IDLE;
                this.rotationResolver = null;
                return;
            }

            // Return Mode: SMOOTH (Smoothly steps currentRotation back to the player's true
            // look angle)
            this.rotationState = RotationType.APPLYING;

            float targetPlayerYaw = ContinuousRotationMath.unwrapYaw(this.currentRotation.getYaw(),
                    playerRotation.getYaw());
            float targetPlayerPitch = playerRotation.getPitch();

            float deltaYaw = Math.abs(targetPlayerYaw - this.currentRotation.getYaw());
            float deltaPitch = Math.abs(targetPlayerPitch - this.currentRotation.getPitch());
            float mouseStep = Math.max(0.1f, AimRotationMath.getMouseRotationStep());

            // Target reached: finalize return
            if (deltaYaw <= mouseStep && deltaPitch <= mouseStep) {
                this.currentRotation = new Rotation(targetPlayerYaw, targetPlayerPitch);
                this.syncPlayerYawRevolutions(targetPlayerYaw);

                // Only apply look if the request correctionMode explicitly changes camera look!
                if (this.rotationResolver.getCorrectionMode().changesCameraLook()) {
                    RotationManager.applyPlayerRotation(this.currentRotation.getYaw(), this.currentRotation.getPitch());
                }

                this.rotationState = RotationType.IDLE;
                this.rotationResolver = null;
                return;
            }

            // Step toward player rotation
            Rotation nextStep = null;
            if (this.rotationResolver.getRotationStep() != null) {
                nextStep = this.rotationResolver.getRotationStep().returnStep(this.currentRotation,
                        new Rotation(targetPlayerYaw, targetPlayerPitch));
            }

            if (nextStep == null) {
                float returnSpeed = this.rotationResolver.getRotationSpeed();
                if (returnSpeed <= 0.0f || returnSpeed > 80.0f) {
                    float minSpeed = 8.0f;
                    float maxSpeed = 35.0f;
                    if (ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY)) {
                        maxSpeed = 25.0f;
                    }
                    returnSpeed = MathUtils.interpolateRandomStrategy(minSpeed, maxSpeed);
                }
                float stepYaw = ContinuousRotationMath.stepYawToward(this.currentRotation.getYaw(), targetPlayerYaw,
                        returnSpeed);
                float stepPitch = ContinuousRotationMath.stepPitchToward(this.currentRotation.getPitch(),
                        targetPlayerPitch, returnSpeed / MathUtils.interpolateRandomStrategy(1.8f, 2.2f));
                nextStep = new Rotation(stepYaw, stepPitch);
            }

            this.currentRotation = this.rotationResolver.isSnapToMouse()
                    ? ContinuousRotationMath.snapToMouseStep(this.currentRotation, nextStep)
                    : nextStep;
            return;
        }

        // 3. PENDING STATE: Actively aiming at target
        this.rotationState = RotationType.PENDING;
        this.applyPendingRotation();
    }

    /**
     * Aligns player.yaw revolutions by multiples of 360 to eliminate any boundary
     * jump
     * when switching between active rotation and idle player state.
     */
    private void syncPlayerYawRevolutions(float continuousYaw) {
        if (RotationManager.minecraftClient.player == null) {
            return;
        }
        float playerYaw = RotationManager.minecraftClient.player.getYaw();
        float diff = continuousYaw - playerYaw;
        float fullTurns = Math.round(diff / 360.0f) * 360.0f;
        if (Math.abs(fullTurns) > 0.01f) {
            RotationManager.minecraftClient.player.setYaw(playerYaw + fullTurns);
            RotationManager.minecraftClient.player.prevYaw += fullTurns;
        }
    }

    @Compile(obfuscation = 1)
    public void updateAppliedRotation(float f) {
        if (RotationManager.minecraftClient.player == null) {
            return;
        }
        float deltaYaw = MathHelper.wrapDegrees(this.currentRotation.getYaw() - this.previousRotation.getYaw());
        float f2 = this.previousRotation.getYaw() + deltaYaw * f;
        float f3 = MathHelper.clamp(this.previousRotation.getPitch()
                + (this.currentRotation.getPitch() - this.previousRotation.getPitch()) * f, -90.0f, 90.0f);
        this.appliedRotation = new Rotation(f2, f3);
    }

    @Compile(obfuscation = 1)
    public void requestRotation(Rotation rotation, RotationCorrectionMode rotationCorrectionMode, float f, float f2,
            float f3, RotationPriority rotationPriority) {
        this.requestRotationInternal(rotation, rotationCorrectionMode, f, f2, f3, rotationPriority, true);
    }

    @Compile(obfuscation = 1)
    public void requestRotationInternal(Rotation rotation, RotationCorrectionMode rotationCorrectionMode, float f,
            float f2, float f3, RotationPriority rotationPriority, boolean bl) {
        int priorityValue = rotationPriority.getPriorityValue();
        if (this.rotationResolver == null || this.rotationResolver.getPriority() <= priorityValue
                || this.rotationState != RotationType.PENDING) {
            // Guarantee currentRotation is initialized
            if (this.currentRotation == Rotation.ZERO_ROTATION && RotationManager.minecraftClient.player != null) {
                this.currentRotation = this.getPlayerRotation();
            }

            // Always unwrap target yaw relative to the continuous currentRotation
            float referenceYaw = this.currentRotation.getYaw();
            float unwrappedTargetYaw = ContinuousRotationMath.unwrapYaw(referenceYaw, rotation.getYaw());
            rotation.setYaw(unwrappedTargetYaw);

            this.rotationResolver = new RotationRequest(rotation, rotationCorrectionMode, f, f2, f3, priorityValue, bl);
            this.rotationTimer.reset();
            this.rotationState = RotationType.PENDING;
            this.applyPendingRotation();
        }
    }

    @Compile(obfuscation = 1)
    public void requestStandardRotation(Rotation rotation, RotationCorrectionMode rotationCorrectionMode, float f,
            float f2, float f3) {
        this.requestRotation(rotation, rotationCorrectionMode, f, f2, f3, RotationPriority.STANDARD_PRIORITY);
    }

    @Compile(obfuscation = 1)
    public void requestRotationWithPriority(Rotation rotation, RotationPriority rotationPriority) {
        this.requestRotation(rotation, RotationCorrectionMode.DIRECT, 180.0f, 180.0f, 180.0f, rotationPriority);
    }

    @Compile(obfuscation = 1)
    public void setRotation(Rotation rotation) {
        this.requestRotation(rotation, RotationCorrectionMode.DIRECT, 180.0f, 180.0f, 180.0f,
                RotationPriority.STANDARD_PRIORITY);
    }

    @Compile(obfuscation = 1)
    public void refreshPendingRotation() {
        if (this.rotationResolver != null && this.rotationState == RotationType.PENDING) {
            this.rotationTimer.reset();
        }
    }

    /**
     * Signals the rotation manager that current rotation request has completed,
     * triggering a smooth return to the player look direction.
     */
    public void finishRotation() {
        if (this.rotationResolver != null) {
            this.rotationTimer.setLastResetTimeMillis(0L);
        }
    }

    /**
     * Cancels the active rotation, optionally performing a smooth return to player
     * look.
     */
    public void cancelRotation(boolean smooth) {
        if (this.rotationResolver == null) {
            return;
        }
        if (smooth) {
            this.finishRotation();
        } else {
            if (RotationManager.minecraftClient.player != null) {
                float unwrappedPlayerYaw = ContinuousRotationMath.unwrapYaw(this.currentRotation.getYaw(),
                        this.getPlayerRotation().getYaw());
                this.currentRotation = new Rotation(unwrappedPlayerYaw, this.getPlayerRotation().getPitch());
                this.syncPlayerYawRevolutions(unwrappedPlayerYaw);
            }
            this.rotationState = RotationType.IDLE;
            this.rotationResolver = null;
        }
    }

    public static float applyPlayerRotation(float f, float f2) {
        if (RotationManager.minecraftClient.player == null) {
            return f;
        }
        float f3 = RotationManager.minecraftClient.player.getYaw()
                + MathHelper.wrapDegrees(f - RotationManager.minecraftClient.player.getYaw());
        float f4 = MathHelper.clamp(f2, -90.0f, 90.0f);
        RotationManager.minecraftClient.player.setYaw(f3);
        RotationManager.minecraftClient.player.setPitch(f4);
        return f3;
    }

    @Compile(obfuscation = 1)
    public static float stepRotationToward(float f, float f2, float f3) {
        float f4 = AimRotationMath.getWrappedAngleDifference(f, f2);
        if (Math.abs(f4) <= f3) {
            return f + f4;
        }
        return f + Math.signum(f4) * f3;
    }

    @Compile(obfuscation = 1)
    private void applyPendingRotation() {
        if (this.rotationResolver == null) {
            return;
        }
        Rotation target = this.rotationResolver.getTargetRotation();

        // Ensure target is unwrapped continuous with currentRotation
        float unwrappedTargetYaw = ContinuousRotationMath.unwrapYaw(this.currentRotation.getYaw(), target.getYaw());
        target.setYaw(unwrappedTargetYaw);

        float stepYaw = ContinuousRotationMath.stepYawToward(this.currentRotation.getYaw(), unwrappedTargetYaw,
                this.rotationResolver.getYawStep());
        float stepPitch = ContinuousRotationMath.stepPitchToward(this.currentRotation.getPitch(), target.getPitch(),
                this.rotationResolver.getPitchStep());
        Rotation nextRotation = new Rotation(stepYaw, stepPitch);

        this.currentRotation = this.rotationResolver.isSnapToMouse()
                ? ContinuousRotationMath.snapToMouseStep(this.currentRotation, nextRotation)
                : nextRotation;
    }

    @Compile(obfuscation = 1)
    public void requestEntityRotation(Entity class_12972, long l, long l2, long l3, RotationPriority rotationPriority,
            RotationCorrectionMode rotationCorrectionMode) {
        if (class_12972 == null || RotationManager.minecraftClient.player == null) {
            return;
        }
        double d = class_12972.getX();
        double d2 = class_12972.getY() + (double) class_12972.getEyeHeight(class_12972.getPose());
        double d3 = class_12972.getZ();
        double d4 = d - RotationManager.minecraftClient.player.getX();
        double d5 = d2
                - (RotationManager.minecraftClient.player.getY() + (double) RotationManager.minecraftClient.player
                        .getEyeHeight(RotationManager.minecraftClient.player.getPose()));
        Rotation rotation = RotationManager.calculateRotationToPoint(d3, d4, d5);
        this.requestRotation(rotation, rotationCorrectionMode, l, l2, l3, rotationPriority);
    }

    @Compile(obfuscation = 1)
    @NotNull
    private static Rotation calculateRotationToPoint(double d, double d2, double d3) {
        double d4 = d - RotationManager.minecraftClient.player.getZ();
        double d5 = Math.sqrt(d2 * d2 + d4 * d4);
        float f = (float) Math.toDegrees(Math.atan2(d4, d2)) - 90.0f;
        float f2 = (float) (-Math.toDegrees(Math.atan2(d3, d5)));
        return new Rotation(f, f2);
    }

    @Compile(obfuscation = 1)
    public Rotation getEntityRotation(LivingEntity class_13092) {
        return new Rotation(class_13092.getYaw(), class_13092.getPitch());
    }

    @Compile(obfuscation = 1)
    public Rotation getPlayerRotation() {
        if (RotationManager.minecraftClient.player == null) {
            return Rotation.ZERO_ROTATION;
        }
        return this.getEntityRotation((LivingEntity) RotationManager.minecraftClient.player);
    }

    @Compile(obfuscation = 1)
    public Rotation getEffectiveRotation() {
        return this.currentRotation != null ? this.currentRotation : this.getPlayerRotation();
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
