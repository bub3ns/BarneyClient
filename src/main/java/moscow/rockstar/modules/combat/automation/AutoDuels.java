/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.PlayerEntity
 *  net.minecraft.GenericContainerScreenHandler
 *  net.minecraft.SlotActionType
 *  net.minecraft.PlayerListEntry
 *  net.minecraft.GameMessageS2CPacket
 */
package moscow.rockstar.modules.combat.automation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.util.Timer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.network.ReceivePacketEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Duels", category=ModuleCategory.OTHER, description="modules.descriptions.auto_duels")
public class AutoDuels
extends Module {
    private ModeSetting preferredDifficulty;
    private ModeSetting.Option softDifficultyOption;
    private ModeSetting.Option antiSoftDifficultyOption;
    private ModeSetting.Option randomDifficultyOption;
    private ModeSetting kitSelection;
    private ModeSetting.Option shieldKitOption;
    private ModeSetting.Option thornsThreeKitOption;
    private ModeSetting.Option bowKitOption;
    private ModeSetting.Option totemKitOption;
    private ModeSetting.Option noDebuffKitOption;
    private ModeSetting.Option ballsKitOption;
    private ModeSetting.Option classicKitOption;
    private ModeSetting.Option cheatsKitOption;
    private ModeSetting.Option netherKitOption;
    private final Timer duelRequestCooldown = new Timer();
    private final List<String> processedMessages = new ArrayList<String>();
    private final EventListener<ReceivePacketEvent> receivePacketListener = receivePacketEvent -> {
        Object object = receivePacketEvent.getPacket();
        if (object instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket class_74392 = (GameMessageS2CPacket)object;
            if (((String)(object = class_74392.content().getString())).contains("\u043f\u0440\u0438\u043d\u044f\u043b") && !((String)object).contains("\u043d\u0435 \u043f\u0440\u0438\u043d\u044f\u043b") || ((String)object).contains("\u043a\u043e\u043c\u0430\u043d\u0434\u044b")) {
                this.processedMessages.clear();
                this.toggle();
            }
            if (((String)object).contains("\u0411\u0430\u043b\u0430\u043d\u0441") || ((String)object).contains("\u043e\u0442\u043a\u043b\u044e\u0447\u0438\u043b \u0437\u0430\u043f\u0440\u043e\u0441\u044b")) {
                receivePacketEvent.cancel();
            }
        }
    };
    private final EventListener<WorldChangeEvent> worldChangeListener = worldChangeEvent -> this.disable();

    public AutoDuels() {
        this.initializeDuelSettings();
    }

    @Compile(obfuscation=4)
    private void initializeDuelSettings() {
        this.preferredDifficulty = new ModeSetting(this, "modules.settings.auto_duels.prefer");
        this.softDifficultyOption = new ModeSetting.Option(this.preferredDifficulty, "modules.settings.auto_duels.prefer.soft");
        this.antiSoftDifficultyOption = new ModeSetting.Option(this.preferredDifficulty, "modules.settings.auto_duels.prefer.ansoft");
        this.randomDifficultyOption = new ModeSetting.Option(this.preferredDifficulty, "modules.settings.auto_duels.prefer.random");
        this.kitSelection = new ModeSetting(this, "modules.settings.auto_duels.kit");
        this.shieldKitOption = new ModeSetting.Option(this.kitSelection, "modules.settings.auto_duels.kit.shield");
        this.thornsThreeKitOption = new ModeSetting.Option(this.kitSelection, "modules.settings.auto_duels.kit.thorns3");
        this.bowKitOption = new ModeSetting.Option(this.kitSelection, "modules.settings.auto_duels.kit.bow");
        this.totemKitOption = new ModeSetting.Option(this.kitSelection, "modules.settings.auto_duels.kit.totem");
        this.noDebuffKitOption = new ModeSetting.Option(this.kitSelection, "modules.settings.auto_duels.kit.nodebuff");
        this.ballsKitOption = new ModeSetting.Option(this.kitSelection, "modules.settings.auto_duels.kit.balls");
        this.classicKitOption = new ModeSetting.Option(this.kitSelection, "modules.settings.auto_duels.kit.classic");
        this.cheatsKitOption = new ModeSetting.Option(this.kitSelection, "modules.settings.auto_duels.kit.cheats");
        this.netherKitOption = new ModeSetting.Option(this.kitSelection, "modules.settings.auto_duels.kit.nether");
    }

    @Override
    @Compile(obfuscation=1)
    public void onTick() {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (PlayerListEntry object : AutoDuels.minecraftClient.player.networkHandler.getPlayerList()) {
            arrayList.add(object.getProfile().getName());
        }
        if (this.randomDifficultyOption.isSelected()) {
            Collections.shuffle(arrayList);
        } else if (this.softDifficultyOption.isSelected()) {
            Collections.reverse(arrayList);
        }
        for (String string : arrayList) {
            if (!this.duelRequestCooldown.hasElapsed(750L) || this.processedMessages.contains(string) || string.equals(AutoDuels.minecraftClient.player.getNameForScoreboard())) continue;
            AutoDuels.minecraftClient.player.networkHandler.sendChatCommand("duel " + string);
            this.processedMessages.add(string);
            this.duelRequestCooldown.reset();
        }
        if (AutoDuels.minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
            String string = AutoDuels.minecraftClient.currentScreen.getTitle().getString();
            if (string.contains("\u0412\u044b\u0431\u043e\u0440 \u043d\u0430\u0431\u043e\u0440\u0430")) {
                AutoDuels.minecraftClient.interactionManager.clickSlot(AutoDuels.minecraftClient.player.currentScreenHandler.syncId, this.kitSelection.getOptions().indexOf(this.kitSelection.getRandomSelectedOption()), 0, SlotActionType.PICKUP, (PlayerEntity)AutoDuels.minecraftClient.player);
                AutoDuels.minecraftClient.player.currentScreenHandler.onSlotClick(this.kitSelection.getOptions().indexOf(this.kitSelection.getRandomSelectedOption()), 0, SlotActionType.PICKUP, (PlayerEntity)AutoDuels.minecraftClient.player);
            } else if (string.contains("\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430 \u043f\u043e\u0435\u0434\u0438\u043d\u043a\u0430")) {
                AutoDuels.minecraftClient.interactionManager.clickSlot(AutoDuels.minecraftClient.player.currentScreenHandler.syncId, 0, 0, SlotActionType.PICKUP, (PlayerEntity)AutoDuels.minecraftClient.player);
                AutoDuels.minecraftClient.player.currentScreenHandler.onSlotClick(0, 0, SlotActionType.PICKUP, (PlayerEntity)AutoDuels.minecraftClient.player);
            }
        }
        super.onTick();
    }

    @Override
    public void onEnable() {
        this.duelRequestCooldown.reset();
        super.onEnable();
    }
}
