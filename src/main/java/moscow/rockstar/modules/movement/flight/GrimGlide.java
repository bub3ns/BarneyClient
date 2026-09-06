/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.StatusEffects
 *  net.minecraft.FireworkRocketEntity
 *  net.minecraft.Items
 *  net.minecraft.Vec3d
 *  net.minecraft.Text
 *  net.minecraft.Packet
 *  net.minecraft.PlayerPositionLookS2CPacket
 *  net.minecraft.PlayerMoveC2SPacket
 *  net.minecraft.PlayerMoveC2SPacket$OnGroundOnly
 *  net.minecraft.MathHelper
 */
package moscow.rockstar.modules.movement.flight;

import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.entity.utility.EntityUtils;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.math.MathUtils;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.mixin.accessors.FireworkRocketEntityAccessor;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.player.movement.camera.FreeCamera;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.network.SendPacketEvent;
import pyrock.events.player.EventOnTravelPost;
import pyrock.events.player.EventUpdatePostTick;
import pyrock.events.render.HudRenderEvent;

@ModuleInfo(name="Grim Glide", category=ModuleCategory.MOVEMENT, description="modules.descriptions.grim_glide")
public class GrimGlide
extends Module {
    private final ModeSetting mode = new ModeSetting(this, "modules.settings.grim_glide.mode");
    private final ModeSetting.Option glideOption = new ModeSetting.Option(this.mode, "RWFlag");
    private final ModeSetting.Option airOption = new ModeSetting.Option(this.mode, "ReallyWorld");
    private final ModeSetting.Option groundOption = new ModeSetting.Option(this.mode, "modules.settings.grim_glide.mode.normal");
    private final BooleanSetting climb = new BooleanSetting(this, "modules.settings.grim_glide.climb").enable();
    private final ModeSetting climbTrigger = new ModeSetting((SettingOwner)this, "modules.settings.grim_glide.climb_trigger", () -> !this.climb.isEnabled());
    private final ModeSetting.Option onJump = new ModeSetting.Option(this.climbTrigger, "modules.settings.grim_glide.climb_trigger.on_jump");
    private final ModeSetting.Option always = new ModeSetting.Option(this.climbTrigger, "modules.settings.grim_glide.climb_trigger.always");
    private boolean gliding;
    private boolean overlayVisible;
    private int flightCorrectionTicks;
    private boolean serverCorrectionPending;
    private int glideCheckTicks;
    private boolean disableLocked;
    private final EventListener<ReceivePacketEvent> onReceivePacketEvent = receivePacketEvent -> {
        if (receivePacketEvent.getPacket() instanceof PlayerPositionLookS2CPacket) {
            this.flightCorrectionTicks = 2;
            this.serverCorrectionPending = true;
        }
    };
    private final EventListener<SendPacketEvent> onSendPacketEvent = sendPacketEvent -> {
        if (!this.groundOption.isSelected() || this.disableLocked || !(sendPacketEvent.getPacket() instanceof PlayerMoveC2SPacket)) {
            return;
        }
        if (GrimGlide.minecraftClient.player != null && GrimGlide.minecraftClient.player.isGliding() && this.flightCorrectionTicks == 0 && !this.serverCorrectionPending) {
            this.disableLocked = true;
            try {
                GrimGlide.minecraftClient.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.OnGroundOnly(true, true));
            }
            finally {
                this.disableLocked = false;
            }
            sendPacketEvent.cancel();
        }
        this.serverCorrectionPending = false;
    };
    private final EventListener<EventOnTravelPost> onEventOnTravelPostListener = eventOnTravelPost -> {
        double d;
        if (!this.groundOption.isSelected() || GrimGlide.minecraftClient.player == null || !GrimGlide.minecraftClient.player.isGliding()) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = GrimGlide.minecraftClient.player.getVelocity();
        Vec3d WallPlayerSkullBlock = GrimGlide.minecraftClient.player.getRotationVector();
        float f = GrimGlide.minecraftClient.player.getPitch() * ((float)Math.PI / 180);
        double d2 = Math.sqrt(WallPlayerSkullBlock.x * WallPlayerSkullBlock.x + WallPlayerSkullBlock.z * WallPlayerSkullBlock.z);
        double d3 = Math.sqrt(VanillaChestLootTableGenerator.x * VanillaChestLootTableGenerator.x + VanillaChestLootTableGenerator.z * VanillaChestLootTableGenerator.z);
        boolean bl = VanillaChestLootTableGenerator.y <= 0.0;
        double d4 = bl && GrimGlide.minecraftClient.player.hasStatusEffect(StatusEffects.SLOW_FALLING) ? 0.01 : 0.08;
        double d5 = MathHelper.cos((float)f);
        d5 *= d5;
        VanillaChestLootTableGenerator = VanillaChestLootTableGenerator.add(0.0, d4 * (-1.0 + d5 * 0.75), 0.0);
        if (VanillaChestLootTableGenerator.y < 0.0 && d2 > 0.0) {
            d = VanillaChestLootTableGenerator.y * -0.1 * d5;
            VanillaChestLootTableGenerator = VanillaChestLootTableGenerator.add(WallPlayerSkullBlock.x * d / d2, d, WallPlayerSkullBlock.z * d / d2);
        }
        if (f < 0.0f && d2 > 0.0) {
            d = d3 * (double)(-MathHelper.sin((float)f)) * 0.04;
            VanillaChestLootTableGenerator = VanillaChestLootTableGenerator.add(-WallPlayerSkullBlock.x * d / d2, d * 3.2, -WallPlayerSkullBlock.z * d / d2);
        }
        if (d2 > 0.0) {
            VanillaChestLootTableGenerator = VanillaChestLootTableGenerator.add((WallPlayerSkullBlock.x / d2 * d3 - VanillaChestLootTableGenerator.x) * 0.1, 0.0, (WallPlayerSkullBlock.z / d2 * d3 - VanillaChestLootTableGenerator.z) * 0.1);
        }
        d = Math.toRadians(GrimGlide.minecraftClient.player.getYaw());
        double d6 = -Math.sin(d);
        double d7 = Math.cos(d);
        if (this.flightCorrectionTicks >= 1) {
            eventOnTravelPost.setOldVelocity(VanillaChestLootTableGenerator.multiply(0.99, (double)0.98f, 0.99).add(d6 * 0.09, 0.03, d7 * 0.09));
        } else {
            eventOnTravelPost.setOldVelocity(VanillaChestLootTableGenerator.multiply(0.3, 0.3, 0.3));
        }
    };
    private final EventListener<HudRenderEvent> onHudRenderEvent = hudRenderEvent -> {
        if (!this.airOption.isSelected() || GrimGlide.minecraftClient.player == null || !GrimGlide.minecraftClient.player.isGliding()) {
            return;
        }
        if (!this.serverCorrectionPending) {
            EntityUtils.setMovementFactor(GrimGlide.minecraftClient.player.age % 2 == 0 ? 1.6f : 0.35f);
        } else {
            EntityUtils.resetMovementFactor();
        }
    };
    private final EventListener<EventUpdatePostTick> onEventUpdatePostTickListener = eventUpdatePostTick -> {
        if (!this.glideOption.isSelected() || GrimGlide.minecraftClient.player == null || GrimGlide.minecraftClient.world == null || !GrimGlide.minecraftClient.player.isGliding()) {
            return;
        }
        if (this.isHoldingGlideItem() || this.isGlideReady()) {
            return;
        }
        ++this.glideCheckTicks;
        if (this.glideCheckTicks % 4 != 0) {
            return;
        }
        if (this.calculateGlideMotion() > 49.0) {
            if (!this.gliding) {
                Notification.info(Text.of((String)"\u0421\u0431\u0440\u043e\u0441\u044c \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c, \u043c\u0430\u043b\u044b\u0448\u043a\u0430"));
                this.gliding = true;
            }
            return;
        }
        this.gliding = false;
        float f = GrimGlide.minecraftClient.player.getYaw();
        double d = 0.01;
        double d2 = this.calculateGlideMotion();
        float f2 = ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD) ? 50.0f : 64.0f;
        if (d2 >= (double)f2) {
            d = 0.0;
        }
        double d3 = -Math.sin(Math.toRadians(f)) * d;
        double d4 = Math.cos(Math.toRadians(f)) * d;
        GrimGlide.minecraftClient.player.setVelocity(d3 * (double)MathUtils.interpolateRandomDouble(0.5, 1.15f), GrimGlide.minecraftClient.player.getVelocity().y - (double)0.01f, d4 * (double)MathUtils.interpolateRandomDouble(0.5, 0.9f));
    };

    @Override
    public void onTick() {
        if (GrimGlide.minecraftClient.player == null || GrimGlide.minecraftClient.world == null) {
            return;
        }
        if (this.flightCorrectionTicks > 0) {
            --this.flightCorrectionTicks;
        }
        if (!this.glideOption.isSelected()) {
            return;
        }
        if (!GrimGlide.minecraftClient.player.isGliding()) {
            this.overlayVisible = false;
            return;
        }
        if (!this.climb.isEnabled()) {
            return;
        }
        if (this.onJump.isSelected() && !GrimGlide.minecraftClient.options.jumpKey.isPressed()) {
            this.overlayVisible = false;
            return;
        }
        double d = this.calculateGlideMotion();
        if (this.overlayVisible) {
            if (d >= (double)18.9f) {
                this.overlayVisible = false;
            }
        } else if (d < 14.0) {
            this.overlayVisible = true;
        }
        float f = !this.overlayVisible && GrimGlide.minecraftClient.player.getY() >= 320.0 ? 0.0f : (this.overlayVisible ? 60.0f : -60.0f);
        FreeCamera freeCamera = RockstarClient.create().getModuleRegistry().getModule(FreeCamera.class);
        float f2 = freeCamera != null && freeCamera.isEnabled() ? freeCamera.getCurrentRotation().getYaw() : GrimGlide.minecraftClient.player.getYaw();
        RockstarClient.create().getRotationManager().requestRotation(new Rotation(f2, f), RotationCorrectionMode.DIRECT, 25.0f, 1.0f, 2.0f, RotationPriority.LOW_PRIORITY);
    }

    @Override
    public void onEnable() {
        this.gliding = false;
        this.glideCheckTicks = 0;
        this.overlayVisible = false;
        this.flightCorrectionTicks = 0;
        this.serverCorrectionPending = false;
        this.disableLocked = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        EntityUtils.resetMovementFactor();
        this.gliding = false;
        this.glideCheckTicks = 0;
        this.overlayVisible = false;
        this.flightCorrectionTicks = 0;
        this.serverCorrectionPending = false;
        this.disableLocked = false;
        super.onDisable();
    }

    private double calculateGlideMotion() {
        double d = GrimGlide.minecraftClient.player.getX() - GrimGlide.minecraftClient.player.prevX;
        double d2 = GrimGlide.minecraftClient.player.getZ() - GrimGlide.minecraftClient.player.prevZ;
        return (double)Math.round(Math.sqrt(d * d + d2 * d2) * 2000.0) / 100.0;
    }

    private boolean isHoldingGlideItem() {
        return GrimGlide.minecraftClient.player.isUsingItem() && GrimGlide.minecraftClient.player.getActiveItem().isOf(Items.FIREWORK_ROCKET);
    }

    private boolean isGlideReady() {
        return !GrimGlide.minecraftClient.world.getEntitiesByClass(FireworkRocketEntity.class, GrimGlide.minecraftClient.player.getBoundingBox().expand(2.0), class_16712 -> ((FireworkRocketEntityAccessor)class_16712).getShooter() == GrimGlide.minecraftClient.player).isEmpty();
    }
}
