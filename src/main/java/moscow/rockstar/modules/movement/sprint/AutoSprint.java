/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Hand
 *  net.minecraft.ItemStack
 *  net.minecraft.SplashPotionItem
 *  net.minecraft.UseAction
 *  net.minecraft.Packet
 *  net.minecraft.UpdateSelectedSlotC2SPacket
 *  net.minecraft.PlayerInteractItemC2SPacket
 */
package moscow.rockstar.modules.movement.sprint;

import lombok.Generated;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.BooleanSetting;
import moscow.rockstar.api.access.PlayerEntityAccess;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import pyrock.events.network.SendPacketEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import pyrock.events.player.KeepSprintEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Sprint", category=ModuleCategory.MOVEMENT, disableLocked=true)
public class AutoSprint
extends Module {
    private BooleanSetting keepSprintSetting;
    private BooleanSetting ignoreHungerSetting;
    private int sprintResetTicks;
    private final EventListener<KeepSprintEvent> keepSprintListener = keepSprintEvent -> {
        if (this.keepSprintSetting.isEnabled()) {
            keepSprintEvent.cancel();
        }
    };
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (this.sprintResetTicks > 0) {
            AutoSprint.minecraftClient.options.sprintKey.setPressed(false);
            --this.sprintResetTicks;
            return;
        }
        AutoSprint.minecraftClient.options.sprintKey.setPressed(true);
    };
    private final EventListener<SendPacketEvent> sendPacketListener = sendPacketEvent -> {
        if (AutoSprint.minecraftClient.player == null || !this.keepSprintSetting.isEnabled()) {
            return;
        }
        if (sendPacketEvent.getPacket() instanceof UpdateSelectedSlotC2SPacket && this.isAutoSprintCooldownReady()) {
            this.resetSprint();
            return;
        }
        Packet<?> class_25962 = sendPacketEvent.getPacket();
        if (class_25962 instanceof PlayerInteractItemC2SPacket) {
            PlayerInteractItemC2SPacket class_28862 = (PlayerInteractItemC2SPacket)class_25962;
            if (ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY) && this.getHeldStack(class_28862.getHand()).getItem() instanceof SplashPotionItem) {
                this.resetSprint();
            }
        }
    };

    public AutoSprint() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.keepSprintSetting = new BooleanSetting(this, "modules.settings.keepSprint");
        this.ignoreHungerSetting = new BooleanSetting(this, "modules.settings.auto_sprint.ignore_hunger");
    }

    private boolean isAutoSprintCooldownReady() {
        if (!AutoSprint.minecraftClient.player.isUsingItem()) {
            return false;
        }
        UseAction class_18392 = AutoSprint.minecraftClient.player.getActiveItem().getUseAction();
        return class_18392 == UseAction.EAT || class_18392 == UseAction.DRINK;
    }

    private ItemStack getHeldStack(Hand class_12682) {
        return class_12682 == Hand.OFF_HAND ? AutoSprint.minecraftClient.player.getOffHandStack() : AutoSprint.minecraftClient.player.getMainHandStack();
    }

    private void resetSprint() {
        this.sprintResetTicks = 2;
        AutoSprint.minecraftClient.options.sprintKey.setPressed(false);
        if (!AutoSprint.minecraftClient.player.isSprinting()) {
            return;
        }
        AutoSprint.minecraftClient.player.setSprinting(false);
        ((PlayerEntityAccess)AutoSprint.minecraftClient.player).rockstar$syncSprinting();
    }

    @Override
    public void onDisable() {
        this.sprintResetTicks = 0;
    }

    @Generated
    public BooleanSetting getIgnoreHungerSetting() {
        return this.ignoreHungerSetting;
    }
}
