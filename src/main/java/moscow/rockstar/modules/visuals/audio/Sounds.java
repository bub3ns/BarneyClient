/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Entity
 *  net.minecraft.LivingEntity
 *  net.minecraft.World
 *  net.minecraft.EntityStatusS2CPacket
 *  net.minecraft.GameMessageS2CPacket
 */
package moscow.rockstar.modules.visuals.audio;

import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import pyrock.events.game.EntityDeathEvent;
import pyrock.events.network.ReceivePacketEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Sounds", category=ModuleCategory.OTHER, disableLocked=true, description="modules.descriptions.sounds")
public class Sounds
extends Module {
    private NumberSetting volumeSetting;
    private MultiBooleanSetting soundOptions;
    private MultiBooleanSetting.Option killSoundOption;
    private MultiBooleanSetting.Option totemSoundOption;
    private MultiBooleanSetting.Option startSoundOption;
    private MultiBooleanSetting.Option leaveSoundOption;
    private MultiBooleanSetting.Option deathSoundOption;
    private LivingEntity trackedTarget;
    private final EventListener<ReceivePacketEvent> packetListener = receivePacketEvent -> {
        Object packet = receivePacketEvent.getPacket();
        if (packet instanceof GameMessageS2CPacket messagePacket) {
            String messageText = messagePacket.content().getString();
            if (messageText.contains("\u0412\u044b \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043a\u0443\u043f\u0438\u043b\u0438") || messageText.contains("\u043e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u043e \u0438\u0433\u0440\u043e\u043a\u0443")) {
            SoundEffectPlayer.playPurchase(this.getVolume());
            }
        }
        if (packet instanceof EntityStatusS2CPacket statusPacket && Sounds.minecraftClient.world != null && statusPacket.getStatus() == 35) {
            Entity entity = statusPacket.getEntity((World)Sounds.minecraftClient.world);
            if (entity != null && Sounds.minecraftClient.player.distanceTo(entity) < 6.0f && this.totemSoundOption.isSelected()) {
                SoundEffectPlayer.playTotem(this.getVolume());
            }
        }
    };
    private final EventListener<EntityDeathEvent> entityDeathListener = entityDeathEvent -> {
        if (Sounds.minecraftClient.player == entityDeathEvent.getEntity() && this.deathSoundOption.isSelected()) {
            SoundEffectPlayer.playDeath(this.getVolume());
            this.trackedTarget = null;
        }
    };

    public Sounds() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.volumeSetting = new NumberSetting(this, "modules.settings.sounds.volume").setStep(5.0f).setMinValue(10.0f).setMaxValue(100.0f).setValue(80.0f).setUnit("%");
        this.soundOptions = new MultiBooleanSetting(this, "modules.settings.sounds.voice");
        this.killSoundOption = new MultiBooleanSetting.Option(this.soundOptions, "modules.settings.sounds.voice.kill");
        this.totemSoundOption = new MultiBooleanSetting.Option(this.soundOptions, "modules.settings.sounds.voice.totem");
        this.startSoundOption = new MultiBooleanSetting.Option(this.soundOptions, "modules.settings.sounds.voice.start");
        this.leaveSoundOption = new MultiBooleanSetting.Option(this.soundOptions, "modules.settings.sounds.voice.leave");
        this.deathSoundOption = new MultiBooleanSetting.Option(this.soundOptions, "modules.settings.sounds.voice.death");
    }

    public float getVolume() {
        return this.volumeSetting.getValue() / 100.0f;
    }

    @Override
    public void onTick() {
        LivingEntity class_13092;
        Entity class_12972;
        LivingEntity class_13093;
        if (Sounds.minecraftClient.player == null || Sounds.minecraftClient.player.isDead()) {
            this.trackedTarget = null;
            return;
        }
        if (this.trackedTarget != null) {
            if (Sounds.minecraftClient.player.distanceTo((Entity)this.trackedTarget) > 6.0f) {
                this.trackedTarget = null;
                return;
            }
            if (!(this.trackedTarget.isAlive() && Sounds.minecraftClient.world.hasEntity((Entity)this.trackedTarget) || !this.killSoundOption.isSelected())) {
                SoundEffectPlayer.playKill(this.getVolume());
                this.trackedTarget = null;
            }
        }
        LivingEntity class_13094 = class_13093 = (class_12972 = RockstarClient.create().getFriendManager().getTargetEntity()) instanceof LivingEntity ? (class_13092 = (LivingEntity)class_12972) : null;
        if (class_13093 != null) {
            this.trackedTarget = class_13093;
        }
    }

    @Generated
    public MultiBooleanSetting getVoiceSettings() {
        return this.soundOptions;
    }

    @Generated
    public MultiBooleanSetting.Option getKillSoundOption() {
        return this.killSoundOption;
    }

    @Generated
    public MultiBooleanSetting.Option getTotemSoundOption() {
        return this.totemSoundOption;
    }

    @Generated
    public MultiBooleanSetting.Option getStartSoundOption() {
        return this.startSoundOption;
    }

    @Generated
    public MultiBooleanSetting.Option getLeaveSoundOption() {
        return this.leaveSoundOption;
    }

    @Generated
    public MultiBooleanSetting.Option getDeathSoundOption() {
        return this.deathSoundOption;
    }

    @Generated
    public LivingEntity getTargetEntity() {
        return this.trackedTarget;
    }

    @Generated
    public EventListener<ReceivePacketEvent> getPacketListener() {
        return this.packetListener;
    }

    @Generated
    public EventListener<EntityDeathEvent> getEntityDeathListener() {
        return this.entityDeathListener;
    }
}
