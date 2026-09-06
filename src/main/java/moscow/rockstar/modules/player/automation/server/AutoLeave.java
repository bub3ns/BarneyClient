/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Entity
 *  net.minecraft.Text
 *  net.minecraft.Packet
 *  net.minecraft.GameMessageS2CPacket
 */
package moscow.rockstar.modules.player.automation.server;

import moscow.rockstar.core.TargetFilter;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.network.http.client.UrlConnectionClient;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.MultiBooleanSetting;
import moscow.rockstar.settings.NumberSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Leave", category=ModuleCategory.PLAYER)
public class AutoLeave
extends Module {
    private ModeSetting leaveMode;
    private ModeSetting.Option distanceOption;
    private ModeSetting.Option healthOption;
    private ModeSetting.Option banOption;
    private NumberSetting distance;
    private NumberSetting health;
    private NumberSetting delay;
    private MultiBooleanSetting targets;
    private MultiBooleanSetting.Option players;
    private MultiBooleanSetting.Option mobs;
    private MultiBooleanSetting.Option invisiblesOption;
    private MultiBooleanSetting.Option nakedplayers;
    private MultiBooleanSetting.Option rockusersOption;
    private MultiBooleanSetting.Option friends;
    private ModeSetting destinationMode;
    private ModeSetting.Option hubOption;
    private ModeSetting.Option serverOption;
    private ModeSetting.Option spawnOption;
    private final Timer cooldownTimer = new Timer();
    private boolean leaveScheduled;
    private final EventListener<ClientPlayerTickEvent> onClientPlayerTickEventListener = clientPlayerTickEvent -> {
        if (this.distanceOption.isSelected()) {
            TargetFilter targetFilter = new TargetFilter.Builder().players(this.players.isSelected()).mobs(this.mobs.isSelected()).invisibles(this.invisiblesOption.isSelected()).nakedPlayers(this.nakedplayers.isSelected()).friends(this.friends.isSelected()).rockstarUsers(this.rockusersOption.isSelected()).range(this.distance.getValue()).build();
            for (Entity class_12972 : AutoLeave.minecraftClient.world.getEntities()) {
                if (class_12972 == null || class_12972 == AutoLeave.minecraftClient.player || AutoLeave.minecraftClient.player == null || ServerDetector.enabled || !targetFilter.acceptsEntity(class_12972)) continue;
                if (this.hubOption.isSelected()) {
                    AutoLeave.minecraftClient.player.networkHandler.sendChatCommand("hub");
                } else if (this.serverOption.isSelected()) {
                    AutoLeave.minecraftClient.player.networkHandler.getConnection().disconnect(Text.of((String)Localization.translate("modules.auto_leave.near_player")));
                } else if (this.spawnOption.isSelected()) {
                    AutoLeave.minecraftClient.player.networkHandler.sendChatCommand("spawn");
                }
                this.toggle();
                break;
            }
        }
        if (this.healthOption.isSelected() && AutoLeave.minecraftClient.player != null && AutoLeave.minecraftClient.player.getHealth() + AutoLeave.minecraftClient.player.getAbsorptionAmount() <= this.health.getValue()) {
            if (this.hubOption.isSelected()) {
                AutoLeave.minecraftClient.player.networkHandler.sendChatCommand("hub");
            } else if (this.serverOption.isSelected()) {
                AutoLeave.minecraftClient.player.networkHandler.getConnection().disconnect(Text.of((String)Localization.translate("modules.auto_leave.low_health")));
            } else if (this.spawnOption.isSelected()) {
                AutoLeave.minecraftClient.player.networkHandler.sendChatCommand("spawn");
            }
            this.toggle();
        }
        if (!this.leaveScheduled) {
            return;
        }
        if (this.cooldownTimer.hasElapsed((long)this.delay.getValue() * 1000L)) {
            AutoLeave.minecraftClient.player.networkHandler.sendChatCommand("an" + ServerDetector.defaultServerIndex);
            this.leaveScheduled = false;
        }
    };
    private final EventListener<ReceivePacketEvent> onReceivePacketEventListener = receivePacketEvent -> {
        GameMessageS2CPacket class_74392;
        Packet<?> class_25962 = receivePacketEvent.getPacket();
        if (class_25962 instanceof GameMessageS2CPacket && (class_74392 = (GameMessageS2CPacket)class_25962).content().getString().contains(Localization.translate("modules.auto_leave.banned_word")) && this.banOption.isSelected()) {
            AutoLeave.minecraftClient.player.networkHandler.sendChatCommand("hub");
            this.cooldownTimer.reset();
            this.leaveScheduled = true;
        }
    };

    public AutoLeave() {
        this.processLeave();
    }

    @Compile(obfuscation=4)
    private void processLeave() {
        this.leaveMode = new ModeSetting(this, "modules.settings.auto_leave.leave");
        this.distanceOption = new ModeSetting.Option(this.leaveMode, "modules.settings.auto_leave.leave.distance");
        this.healthOption = new ModeSetting.Option(this.leaveMode, "modules.settings.auto_leave.leave.health");
        this.banOption = new ModeSetting.Option(this.leaveMode, "modules.settings.auto_leave.leave.ban");
        this.distance = new NumberSetting((SettingOwner)this, "modules.settings.auto_leave.distance", () -> this.healthOption.isSelected() || this.banOption.isSelected()).setFormatter(f -> " %s".formatted(Localization.translate("block")) + moscow.rockstar.util.NumberFormatting.formatOneDecimal((float)f)).setStep(1.0f).setMinValue(1.0f).setMaxValue(150.0f).setValue(30.0f);
        this.health = new NumberSetting((SettingOwner)this, "modules.settings.auto_leave.health", () -> this.distanceOption.isSelected() || this.banOption.isSelected()).setStep(1.0f).setMinValue(1.0f).setMaxValue(20.0f).setValue(10.0f);
        this.delay = new NumberSetting((SettingOwner)this, "modules.settings.auto_leave.delay", () -> !this.banOption.isSelected() || this.distanceOption.isSelected() || this.healthOption.isSelected()).setUnit(Localization.translate("sec") + ".").setStep(1.0f).setMinValue(1.0f).setMaxValue(60.0f).setValue(40.0f);
        this.targets = new MultiBooleanSetting((SettingOwner)this, "targets", () -> this.healthOption.isSelected() || this.banOption.isSelected());
        this.players = new MultiBooleanSetting.Option(this.targets, "players").select();
        this.mobs = new MultiBooleanSetting.Option(this.targets, "mobs");
        this.invisiblesOption = new MultiBooleanSetting.Option(this.targets, "invisibles").select();
        this.nakedplayers = new MultiBooleanSetting.Option(this.targets, "nakedPlayers").select();
        this.rockusersOption = new MultiBooleanSetting.Option(this.targets, "rockUsers").select();
        this.friends = new MultiBooleanSetting.Option(this.targets, "friends");
        this.destinationMode = new ModeSetting(this, "modules.settings.auto_leave.mode");
        this.hubOption = new ModeSetting.Option(this.destinationMode, "modules.settings.auto_leave.mode.hub");
        this.serverOption = new ModeSetting.Option(this.destinationMode, "modules.settings.auto_leave.mode.server");
        this.spawnOption = new ModeSetting.Option(this.destinationMode, "modules.settings.auto_leave.mode.spawn");
    }
}
