/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec3d
 *  net.minecraft.Text
 *  net.minecraft.Packet
 *  net.minecraft.PlayerMoveC2SPacket
 *  net.minecraft.PlayerMoveC2SPacket$LookAndOnGround
 *  net.minecraft.PlayerActionC2SPacket
 *  net.minecraft.PlayerActionC2SPacket$Action
 *  net.minecraft.PlayerInteractItemC2SPacket
 */
package moscow.rockstar.modules.movement.jump;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.util.Timer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.network.SendPacketEvent;
import pyrock.events.player.EventMotion;
import pyrock.events.player.InputEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Air Stuck", category=ModuleCategory.MOVEMENT)
public class AirStuck
extends Module {
    private ModeSetting modeSetting;
    private ModeSetting.Option normalModeOption;
    private ModeSetting.Option reallyWorldModeOption;
    private ModeSetting.Option funTimeModeOption;
    private BooleanSetting changeAuraDistanceSetting;
    private NumberSetting auraDistanceSetting;
    private BooleanSetting catchMomentSetting;
    private BooleanSetting fallCheckSetting;
    private double airHeight = Double.NaN;
    private boolean stuckInPlace;
    private Vec3d stuckPosition = Vec3d.ZERO;
    private Vec3d savedVelocity = Vec3d.ZERO;
    private boolean packetBypassActive;
    private final Queue<Packet<?>> deferredPackets = new ConcurrentLinkedQueue();
    private final Timer activationTimer = new Timer();
    private boolean flightControlActive;
    private final EventListener<InputEvent> inputListener = inputEvent -> {
        if (AirStuck.minecraftClient.player == null || !this.reallyWorldModeOption.isSelected() && !this.funTimeModeOption.isSelected()) {
            return;
        }
        inputEvent.setForward(0.0f);
        inputEvent.setStrafe(0.0f);
        inputEvent.setJump(false);
        inputEvent.setSneak(false);
        inputEvent.setSprint(false);
    };
    private final EventListener<EventMotion> motionListener = eventMotion -> {
        if (!this.normalModeOption.isSelected() || !this.stuckInPlace || AirStuck.minecraftClient.player == null) {
            return;
        }
        eventMotion.setX(this.stuckPosition.x);
        eventMotion.setY(this.stuckPosition.y);
        eventMotion.setZ(this.stuckPosition.z);
        eventMotion.setOnGround(false);
        AirStuck.minecraftClient.player.fallDistance = 0.0f;
    };
    private final EventListener<SendPacketEvent> sendPacketListener = sendPacketEvent -> {
        PlayerActionC2SPacket class_28462;
        boolean bl;
        if (AirStuck.minecraftClient.player == null || this.packetBypassActive) {
            return;
        }
        if (this.funTimeModeOption.isSelected()) {
            if (!(sendPacketEvent.getPacket() instanceof PlayerMoveC2SPacket)) {
                this.deferredPackets.add(sendPacketEvent.getPacket());
            }
            sendPacketEvent.cancel();
            return;
        }
        if (this.reallyWorldModeOption.isSelected()) {
            if (sendPacketEvent.getPacket() instanceof PlayerMoveC2SPacket) {
                sendPacketEvent.cancel();
            }
            return;
        }
        if (!this.stuckInPlace) {
            return;
        }
        if (sendPacketEvent.getPacket() instanceof PlayerMoveC2SPacket) {
            sendPacketEvent.cancel();
            return;
        }
        Packet<?> class_25962 = sendPacketEvent.getPacket();
        boolean bl2 = bl = class_25962 instanceof PlayerActionC2SPacket && (class_28462 = (PlayerActionC2SPacket)class_25962).getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM;
        if (sendPacketEvent.getPacket() instanceof PlayerInteractItemC2SPacket || bl) {
            this.packetBypassActive = true;
            try {
                AirStuck.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.LookAndOnGround(AirStuck.minecraftClient.player.getYaw(), AirStuck.minecraftClient.player.getPitch(), false, AirStuck.minecraftClient.player.horizontalCollision));
            }
            finally {
                this.packetBypassActive = false;
            }
        }
    };
    private final EventListener<WorldChangeEvent> worldChangeListener = worldChangeEvent -> {
        this.deferredPackets.clear();
        this.flightControlActive = false;
        this.disable();
    };

    public AirStuck() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.modeSetting = new ModeSetting(this, "modules.settings.air_stuck.mode");
        this.normalModeOption = new ModeSetting.Option(this.modeSetting, "modules.settings.air_stuck.mode.normal");
        this.reallyWorldModeOption = new ModeSetting.Option(this.modeSetting, "modules.settings.air_stuck.mode.reallyworld");
        this.funTimeModeOption = new ModeSetting.Option(this.modeSetting, "modules.settings.air_stuck.mode.funtime");
        this.changeAuraDistanceSetting = new BooleanSetting(this, "modules.settings.air_stuck.change_aura_distance");
        this.auraDistanceSetting = new NumberSetting((SettingOwner)this, "modules.settings.air_stuck.aura_distance", () -> !this.changeAuraDistanceSetting.isEnabled()).setMinValue(2.0f).setMaxValue(6.0f).setStep(0.1f).setValue(3.0f).setFormatter(f -> " %s".formatted(Localization.translate("block")) + moscow.rockstar.util.NumberFormatting.formatOneDecimal((float)f));
        this.catchMomentSetting = new BooleanSetting((SettingOwner)this, "modules.settings.air_stuck.catch_moment", () -> !this.normalModeOption.isSelected()).enable();
        this.fallCheckSetting = new BooleanSetting((SettingOwner)this, "modules.settings.air_stuck.fall_check", () -> !this.funTimeModeOption.isSelected()).enable();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.stuckInPlace = false;
        this.stuckPosition = Vec3d.ZERO;
        this.airHeight = Double.NaN;
        this.savedVelocity = Vec3d.ZERO;
        this.deferredPackets.clear();
        this.activationTimer.reset();
        this.flightControlActive = false;
        if (AirStuck.minecraftClient.player == null) {
            return;
        }
        if (this.funTimeModeOption.isSelected()) {
            if (this.fallCheckSetting.isEnabled() && AirStuck.minecraftClient.player.isOnGround()) {
                Notification.error((Text)Text.literal((String)Localization.translate("modules.messages.air_stuck.air_required")));
                this.disable();
                return;
            }
            this.savedVelocity = AirStuck.minecraftClient.player.getVelocity();
            AirStuck.minecraftClient.player.setNoGravity(true);
            this.flightControlActive = true;
            return;
        }
        if (this.reallyWorldModeOption.isSelected()) {
            this.savedVelocity = AirStuck.minecraftClient.player.getVelocity();
            AirStuck.minecraftClient.player.setNoGravity(true);
            return;
        }
        if (this.catchMomentSetting.isEnabled()) {
            this.airHeight = AirStuck.minecraftClient.player.isOnGround() ? Double.NaN : AirStuck.minecraftClient.player.getY();
            return;
        }
        this.airHeight = AirStuck.minecraftClient.player.getY();
        this.engageAirStuck();
    }

    @Override
    public void onDisable() {
        if (AirStuck.minecraftClient.player != null) {
            Packet<?> class_25962;
            if (this.flightControlActive) {
                AirStuck.minecraftClient.player.setVelocity(this.savedVelocity);
            } else if (this.reallyWorldModeOption.isSelected() && !AirStuck.minecraftClient.player.isOnGround()) {
                AirStuck.minecraftClient.player.setVelocity(this.savedVelocity);
            }
            if (this.flightControlActive || this.reallyWorldModeOption.isSelected()) {
                AirStuck.minecraftClient.player.setNoGravity(false);
            }
            while ((class_25962 = this.deferredPackets.poll()) != null) {
                if (minecraftClient.getNetworkHandler() == null) continue;
                minecraftClient.getNetworkHandler().sendPacket(class_25962);
            }
        } else {
            this.deferredPackets.clear();
        }
        this.stuckInPlace = false;
        this.airHeight = Double.NaN;
        this.stuckPosition = Vec3d.ZERO;
        this.savedVelocity = Vec3d.ZERO;
        this.flightControlActive = false;
        super.onDisable();
    }

    public float getAuraDistance() {
        return this.isEnabled() && this.changeAuraDistanceSetting.isEnabled() ? this.auraDistanceSetting.getValue() : 0.0f;
    }

    @Override
    public void onTick() {
        if (AirStuck.minecraftClient.player == null) {
            this.stuckInPlace = false;
            this.airHeight = Double.NaN;
            super.onTick();
            return;
        }
        if (AirStuck.minecraftClient.player.isDead()) {
            this.disable();
            return;
        }
        if (this.funTimeModeOption.isSelected()) {
            if (this.activationTimer.hasElapsed(28000L)) {
                Notification.error((Text)Text.literal((String)Localization.translate("modules.messages.air_stuck.timeout")));
                this.disable();
                return;
            }
            AirStuck.minecraftClient.player.setVelocity(Vec3d.ZERO);
            AirStuck.minecraftClient.player.setNoGravity(true);
            AirStuck.minecraftClient.player.fallDistance = 0.0f;
            super.onTick();
            return;
        }
        if (this.reallyWorldModeOption.isSelected()) {
            AirStuck.minecraftClient.player.setVelocity(Vec3d.ZERO);
            AirStuck.minecraftClient.player.setNoGravity(true);
            AirStuck.minecraftClient.player.fallDistance = 0.0f;
            super.onTick();
            return;
        }
        if (this.catchMomentSetting.isEnabled()) {
            double d = AirStuck.minecraftClient.player.getY();
            if (AirStuck.minecraftClient.player.isOnGround()) {
                this.airHeight = Double.NaN;
                this.stuckInPlace = false;
            } else if (!this.stuckInPlace) {
                if (Double.isNaN(this.airHeight)) {
                    this.airHeight = d;
                } else if (d > this.airHeight) {
                    this.airHeight = d;
                } else if (d < this.airHeight) {
                    this.engageAirStuck();
                }
            }
        }
        if (this.stuckInPlace) {
            AirStuck.minecraftClient.player.setVelocity(Vec3d.ZERO);
            AirStuck.minecraftClient.player.setPosition(this.stuckPosition);
            AirStuck.minecraftClient.player.fallDistance = 0.0f;
            if (AirStuck.minecraftClient.player.input != null) {
                AirStuck.minecraftClient.player.input.movementForward = 0.0f;
                AirStuck.minecraftClient.player.input.movementSideways = 0.0f;
            }
        }
        super.onTick();
    }

    private void engageAirStuck() {
        if (AirStuck.minecraftClient.player == null) {
            return;
        }
        this.stuckInPlace = true;
        this.stuckPosition = AirStuck.minecraftClient.player.getPos();
        this.airHeight = AirStuck.minecraftClient.player.getY();
    }
}
