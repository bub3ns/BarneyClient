/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.PlayerPositionLookS2CPacket
 *  net.minecraft.MathHelper
 *  net.minecraft.ClientPlayerEntity
 */
package moscow.rockstar.events.player;

import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationRequest;
import moscow.rockstar.combat.critical.CriticalHitTiming;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.combat.attacks.Aura;
import moscow.rockstar.modules.combat.aura.attack.AuraAttackController;
import moscow.rockstar.modules.movement.speed.Speed;
import moscow.rockstar.render.esp.EntityOverlayGeometry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.player.InputEvent;
import pyrock.events.player.TraceEvent;
import pyrock.events.render.Render3DEvent;
import ua.mintantileak.spk.Compile;

public class PlayerInputBridge
implements ClientAccess {
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        Rotation rotation = RockstarClient.create().getRotationManager().getCurrentRotation() != null ? RockstarClient.create().getRotationManager().getCurrentRotation() : new Rotation(0.0f, 0.0f);
        RockstarClient.create().getRotationManager().updateRotation();
        if (RockstarClient.create().getRotationManager().getCurrentRotation() != null) {
            Rotation rotation2 = rotation.differenceTo(RockstarClient.create().getRotationManager().getCurrentRotation());
        }
    };
    private final EventListener<Render3DEvent> renderListener = render3DEvent -> {
        RotationManager rotationManager = RockstarClient.create().getRotationManager();
        rotationManager.updateAppliedRotation(render3DEvent.getTickDelta());
        this.applyCameraRotation(rotationManager);
    };
    private final EventListener<InputEvent> inputListener = inputEvent -> {
        LivingEntity class_13092;
        Aura aura = RockstarClient.create().getModuleRegistry().getModule(Aura.class);
        RotationManager rotationManager = RockstarClient.INSTANCE.getRotationManager();
        RotationRequest rotationRequest = rotationManager.getRotationResolver();
        Entity class_12972 = RockstarClient.create().getFriendManager().getTargetEntity();
        if (class_12972 instanceof LivingEntity) {
            class_13092 = (LivingEntity)class_12972;
        } else {
            class_13092 = null;
        }
        if (!rotationManager.isIdle() && rotationRequest != null) {
            RotationCorrectionMode correctionMode = rotationRequest.getCorrectionMode();
            Rotation rotation = rotationManager.getCurrentRotation();
            if (correctionMode.appliesDirectYaw()) {
                inputEvent.setYaw(rotation.getYaw());
            }
            if (correctionMode.appliesSmoothYaw()) {
                inputEvent.setYawSmooth(rotation.getYaw());
            }
            if (correctionMode == RotationCorrectionMode.TARGETED && class_13092 != null) {
                Speed speed = RockstarClient.create().getModuleRegistry().getModule(Speed.class);
                if (speed.isEnabled() && speed.getCollisionTarget().isSelected()) {
                    Vec3d VanillaChestLootTableGenerator = class_13092.getPos().add(class_13092.getPos().subtract(new Vec3d(class_13092.prevX, class_13092.prevY, class_13092.prevZ)).multiply((double)speed.getDistanceSetting().getValue()));
                    inputEvent.setYaw(rotation.getYaw(), AimRotationMath.getRotationToPoint(VanillaChestLootTableGenerator).getYaw());
                } else {
                    inputEvent.setYaw(rotation.getYaw(), AimRotationMath.getRotationToPoint(class_13092.getPos()).getYaw());
                }
                if (PlayerInputBridge.minecraftClient.player.isSwimming()) {
                    if (class_13092.getY() > PlayerInputBridge.minecraftClient.player.getY()) {
                        inputEvent.setJump(true);
                        inputEvent.setSneak(false);
                    } else {
                        inputEvent.setSneak(true);
                        inputEvent.setJump(false);
                    }
                }
            }
        }
        if (!rotationManager.isIdle() && rotationRequest != null && rotationRequest.getCorrectionMode() == RotationCorrectionMode.TARGETED && aura.isEnabled() && class_13092 != null) {
            if (aura.isInvisiblesEnabled() && aura.getSmartCriticalsSetting().isEnabled()) {
                double d = Math.toDegrees(Math.atan2(PlayerInputBridge.minecraftClient.player.getZ() - class_13092.getZ(), PlayerInputBridge.minecraftClient.player.getX() - class_13092.getX()));
                float f = class_13092.getYaw() + 180.0f;
                float f2 = MathHelper.wrapDegrees((float)((float)(d - (double)f)));
                float f3 = MathHelper.clamp((float)(-f2 / 90.0f), (float)-1.0f, (float)1.0f);
                float f4 = Math.abs(f2) < 15.0f ? 0.6f : 1.0f;
                inputEvent.setForward(f4);
                inputEvent.setStrafe(f3);
            }
            inputEvent.setSprint(true);
        }
        if (aura.getSortingSetting().getSelectedOption() instanceof AuraAttackController && aura.shouldResetSprint() && class_13092 != null && CriticalHitTiming.isCriticalWindowReady(PlayerInputBridge.minecraftClient.player, EntityOverlayGeometry.getEntityHeight(class_13092), 2) && aura.isWithinRange(class_13092) && PlayerInputBridge.minecraftClient.player.fallDistance < 0.4f) {
            inputEvent.setForward(0.0f);
            inputEvent.setStrafe(0.0f);
        }
    };
    private final EventListener<ReceivePacketEvent> rotationPacketListener = receivePacketEvent -> {
        Packet<?> class_25962 = receivePacketEvent.getPacket();
        if (class_25962 instanceof PlayerPositionLookS2CPacket) {
            PlayerPositionLookS2CPacket class_27082 = (PlayerPositionLookS2CPacket)class_25962;
            RotationManager rotationManager = RockstarClient.create().getRotationManager();
            Rotation rotation = rotationManager.getCurrentRotation();
            float f = class_27082.change().yaw();
            if (rotation != null) {
                f = rotation.getYaw() + MathHelper.wrapDegrees((float)(f - rotation.getYaw()));
            }
            rotationManager.setCurrentRotation(new Rotation(f, class_27082.change().pitch()));
        }
    };
    private final EventListener<TraceEvent> traceListener = traceEvent -> {
        RotationManager rotationManager = RockstarClient.INSTANCE.getRotationManager();
        RotationRequest rotationRequest = rotationManager.getRotationResolver();
        if (!rotationManager.isIdle() && rotationRequest != null) {
            traceEvent.setYaw(rotationManager.getCurrentRotation().getYaw());
            traceEvent.setPitch(rotationManager.getCurrentRotation().getPitch());
            traceEvent.cancel();
        }
    };
    private final EventListener<ReceivePacketEvent> packetListener = receivePacketEvent -> {};

    public PlayerInputBridge() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    @Compile(obfuscation=1)
    private void applyCameraRotation(RotationManager rotationManager) {
        if (PlayerInputBridge.minecraftClient.player == null || rotationManager.isIdle()) {
            return;
        }
        RotationRequest rotationRequest = rotationManager.getRotationResolver();
        if (rotationRequest == null || !rotationRequest.getCorrectionMode().changesCameraLook()) {
            return;
        }
        Rotation rotation = rotationManager.getAppliedRotation();
        RotationManager.applyPlayerRotation(rotation.getYaw(), rotation.getPitch());
    }
}
