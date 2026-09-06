/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec3d
 *  net.minecraft.Packet
 *  net.minecraft.ExplosionS2CPacket
 *  net.minecraft.EntityVelocityUpdateS2CPacket
 */
package moscow.rockstar.modules.combat.defense.velocity;

import moscow.rockstar.events.EventListener;
import moscow.rockstar.mixin.accessors.EntityVelocityUpdateAccessor;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.modules.combat.defense.velocity.VelocityState;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import pyrock.events.network.ReceivePacketEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Velocity", category=ModuleCategory.COMBAT, description="modules.descriptions.velocity")
public class Velocity
extends Module {
    private ModeSetting mode;
    private ModeSetting.Option defaultMode;
    private ModeSetting.Option compensation;
    private ModeSetting.Option grimOption;
    private ModeSetting.Option modify;
    private NumberSetting velocityX;
    private NumberSetting velocityY;
    private NumberSetting velocityZ;
    private NumberSetting grimHitsBeforeCancel;
    private final VelocityState velocityState = new VelocityState();
    private final EventListener<ReceivePacketEvent> packetListener = receivePacketEvent -> {
        EntityVelocityUpdateS2CPacket class_27432;
        EntityVelocityUpdateS2CPacket class_27433;
        EntityVelocityUpdateS2CPacket class_27434;
        if (Velocity.minecraftClient.player == null || Velocity.minecraftClient.player.isDead()) {
            return;
        }
        Packet<?> class_25962 = receivePacketEvent.getPacket();
        boolean bl = class_25962 instanceof EntityVelocityUpdateS2CPacket && (class_27434 = (EntityVelocityUpdateS2CPacket)class_25962).getEntityId() == Velocity.minecraftClient.player.getId();
        boolean bl2 = class_25962 instanceof ExplosionS2CPacket;
        if (this.mode.isSelected(this.grimOption)) {
            EntityVelocityUpdateS2CPacket class_27435;
            if (class_25962 instanceof EntityVelocityUpdateS2CPacket && (class_27435 = (EntityVelocityUpdateS2CPacket)class_25962).getEntityId() == Velocity.minecraftClient.player.getId()) {
                this.velocityState.setGrimHitCount(this.velocityState.getGrimHitCount() + 1);
                int n = Math.max(1, (int)this.grimHitsBeforeCancel.getValue());
                if (this.velocityState.getGrimHitCount() > n) {
                    receivePacketEvent.cancel();
                    this.velocityState.setGrimHitCount(0);
                }
            }
            return;
        }
        if (bl || bl2) {
            if (this.mode.isSelected(this.defaultMode) && class_25962 instanceof EntityVelocityUpdateS2CPacket && (class_27433 = (EntityVelocityUpdateS2CPacket)class_25962).getEntityId() == Velocity.minecraftClient.player.getId()) {
                receivePacketEvent.cancel();
            } else if (this.mode.isSelected(this.modify) && class_25962 instanceof EntityVelocityUpdateS2CPacket && (class_27432 = (EntityVelocityUpdateS2CPacket)class_25962).getEntityId() == Velocity.minecraftClient.player.getId()) {
                int n = (int)(class_27432.getVelocityX() * 8000.0 * (double)this.velocityX.getValue() / 100.0);
                int n2 = (int)(class_27432.getVelocityY() * 8000.0 * (double)this.velocityY.getValue() / 100.0);
                int n3 = (int)(class_27432.getVelocityZ() * 8000.0 * (double)this.velocityZ.getValue() / 100.0);
                EntityVelocityUpdateAccessor entityVelocityUpdateAccessor = (EntityVelocityUpdateAccessor)class_27432;
                entityVelocityUpdateAccessor.setVelocityX(n);
                entityVelocityUpdateAccessor.setVelocityY(n2);
                entityVelocityUpdateAccessor.setVelocityZ(n3);
            }
        }
        Packet<?> receivedPacket = receivePacketEvent.getPacket();
        if (this.compensation.isSelected() && receivedPacket instanceof EntityVelocityUpdateS2CPacket velocityPacket && velocityPacket.getEntityId() == Velocity.minecraftClient.player.getId()) {
            this.velocityState.position = new Vec3d(velocityPacket.getVelocityX() / 8000.0, velocityPacket.getVelocityY() / 8000.0, velocityPacket.getVelocityZ() / 8000.0);
            this.velocityState.compensationTicksRemaining = 4 + Velocity.minecraftClient.player.getRandom().nextInt(4);
        }
    };

    public Velocity() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.mode = new ModeSetting(this, "modules.settings.velocity.mode");
        this.defaultMode = new ModeSetting.Option(this.mode, "modules.settings.velocity.default");
        this.compensation = new ModeSetting.Option(this.mode, "modules.settings.velocity.compensation");
        this.grimOption = new ModeSetting.Option(this.mode, "Grim");
        this.modify = new ModeSetting.Option(this.mode, "modules.settings.velocity.modify");
        this.velocityX = new NumberSetting((SettingOwner)this, "modules.settings.velocity.velocity_x", () -> !this.mode.isSelected(this.modify)).setUnit("%").setValue(50.0f).setMinValue(0.0f).setMaxValue(100.0f).setStep(1.0f);
        this.velocityY = new NumberSetting((SettingOwner)this, "modules.settings.velocity.velocity_y", () -> !this.mode.isSelected(this.modify)).setUnit("%").setValue(50.0f).setMinValue(0.0f).setMaxValue(100.0f).setStep(1.0f);
        this.velocityZ = new NumberSetting((SettingOwner)this, "modules.settings.velocity.velocity_z", () -> !this.mode.isSelected(this.modify)).setUnit("%").setValue(50.0f).setMinValue(0.0f).setMaxValue(100.0f).setStep(1.0f);
        this.grimHitsBeforeCancel = new NumberSetting((SettingOwner)this, "modules.settings.velocity.grim_hits_before_cancel", () -> !this.mode.isSelected(this.grimOption)).setMinValue(1.0f).setMaxValue(10.0f).setStep(1.0f).setValue(3.0f);
    }

    @Override
    public final void onTick() {
        if (Velocity.minecraftClient.player == null || Velocity.minecraftClient.player.isDead()) {
            this.velocityState.reset();
            return;
        }
        if (!this.compensation.isSelected() || this.velocityState.compensationTicksRemaining <= 0) {
            return;
        }
        Vec3d VanillaChestLootTableGenerator = Velocity.minecraftClient.player.getVelocity();
        double d = (double)this.velocityState.compensationTicksRemaining / 6.0;
        double d2 = 0.3 + (1.0 - d) * 0.35;
        Vec3d WallPlayerSkullBlock = new Vec3d(-this.velocityState.position.x * (d2 += Velocity.minecraftClient.player.getRandom().nextDouble() * 0.05), 0.0, -this.velocityState.position.z * d2);
        Vec3d VanillaEntityLootTableGenerator = VanillaChestLootTableGenerator.add(WallPlayerSkullBlock);
        Velocity.minecraftClient.player.setVelocity(VanillaEntityLootTableGenerator);
        --this.velocityState.compensationTicksRemaining;
        super.onTick();
    }

    @Override
    public void onDisable() {
        this.velocityState.reset();
    }
}
