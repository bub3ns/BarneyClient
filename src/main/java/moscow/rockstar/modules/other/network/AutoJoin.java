/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Hand
 *  net.minecraft.PlayerEntity
 *  net.minecraft.GenericContainerScreenHandler
 *  net.minecraft.SlotActionType
 *  net.minecraft.Items
 *  net.minecraft.PlayerInteractItemC2SPacket
 *  net.minecraft.GameMessageS2CPacket
 */
package moscow.rockstar.modules.other.network;

import java.util.Locale;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.ContainerItemCache;
import moscow.rockstar.inventory.HotbarSlot;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.modules.Module;
import moscow.rockstar.modules.ModuleCategory;
import moscow.rockstar.modules.ModuleInfo;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.settings.SettingOwner;
import moscow.rockstar.settings.StringSetting;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.NotificationType;
import moscow.rockstar.util.Timer;
import net.minecraft.util.Hand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import pyrock.events.game.WorldChangeEvent;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

@ModuleInfo(name="Auto Join", category=ModuleCategory.OTHER, description="modules.descriptions.auto_join")
public class AutoJoin
extends Module {
    private ModeSetting joinModeSetting;
    private ModeSetting.Option duelsModeOption;
    private ModeSetting.Option griefModeOption;
    private StringSetting anarchyNumberSetting;
    private static final long JOIN_CONFIRM_DELAY_MILLIS = 300L;
    private static final long JOIN_COMMAND_DELAY_MILLIS = 1500L;
    private static final long JOIN_COOLDOWN_MILLIS = 2500L;
    private static final long SERVER_CHANGE_COOLDOWN_MILLIS = 8000L;
    private final Timer joinCooldownTimer = new Timer();
    private final Timer joinActionTimer = new Timer();
    private final Timer serverJoinTimer = new Timer();
    private final Timer serverChangeTimer = new Timer();
    private long scheduledJoinDelayMillis;
    private boolean joinPending;
    private boolean joinScreenOpen;
    private boolean serverChangePending;
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> this.updateJoinWorkflow();
    private final EventListener<ReceivePacketEvent> receivePacketListener = receivePacketEvent -> {
        Object object = receivePacketEvent.getPacket();
        if (!(object instanceof GameMessageS2CPacket)) {
            return;
        }
        GameMessageS2CPacket class_74392 = (GameMessageS2CPacket)object;
        if (!(ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) || ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD) || ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY))) {
            return;
        }
        object = class_74392.content().getString().toLowerCase(Locale.ROOT);
        if (this.isJoinFailureMessage((String)object)) {
            this.joinScreenOpen = false;
            if (this.duelsModeOption.isSelected()) {
                this.scheduleNextJoin(2500L);
            } else {
                this.joinActionTimer.reset();
            }
            if (!this.serverChangePending) {
                this.serverChangePending = true;
                RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.INFO, Localization.translate("auto_join.retry"), Localization.translate("auto_join.retry.desc"));
            }
            return;
        }
        if (this.griefModeOption.isSelected() && (ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) ? ((String)object).contains(Localization.translate("auto_join.already_connected_ft")) : ((String)object).contains(Localization.translate("auto_join.already_connected")))) {
            this.disable();
        }
    };
    private final EventListener<WorldChangeEvent> worldChangeListener = worldChangeEvent -> {
        if (this.duelsModeOption.isSelected()) {
            this.scheduleNextJoin(1500L);
            return;
        }
        if (this.joinScreenOpen) {
            this.disable();
        }
    };

    public AutoJoin() {
        this.initializeSettings();
    }

    @Compile(obfuscation=4)
    private void initializeSettings() {
        this.joinModeSetting = new ModeSetting(this, "modules.settings.auto_join.mode");
        this.duelsModeOption = new ModeSetting.Option(this.joinModeSetting, "modules.settings.auto_join.mode.duels_st").select();
        this.griefModeOption = new ModeSetting.Option(this.joinModeSetting, "modules.settings.auto_join.mode.grief");
        this.anarchyNumberSetting = new StringSetting((SettingOwner)this, "modules.settings.auto_join.anarchy_number", this.duelsModeOption::isSelected).setValue("306").setNumericOnly(true);
    }

    @Compile(obfuscation=1)
    private void updateJoinWorkflow() {
        Object object;
        Object object2;
        if (ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY) && this.duelsModeOption.isSelected()) {
            ItemRuleCollection<HotbarSlot> itemRuleCollection = ItemRuleSets.getHotbarRules();
            if (this.scheduledJoinDelayMillis > 0L) {
                if (!this.serverChangeTimer.hasElapsed(this.scheduledJoinDelayMillis)) {
                    return;
                }
                this.scheduledJoinDelayMillis = 0L;
            }
            if (itemRuleCollection.findByItem(Items.DIAMOND_SWORD) != null || ServerDetector.isDuelServer()) {
                this.processJoinWorkflow();
                return;
            }
            if (!this.joinCooldownTimer.hasElapsed(300L)) {
                return;
            }
            object2 = AutoJoin.minecraftClient.player.currentScreenHandler;
            if (object2 instanceof GenericContainerScreenHandler) {
                object = (GenericContainerScreenHandler)object2;
                if (AutoJoin.minecraftClient.currentScreen != null && AutoJoin.minecraftClient.currentScreen.getTitle().getString().contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c")) {
                    AutoJoin.minecraftClient.interactionManager.clickSlot(((GenericContainerScreenHandler)object).syncId, 13, 0, SlotActionType.QUICK_MOVE, (PlayerEntity)AutoJoin.minecraftClient.player);
                    this.joinScreenOpen = true;
                    this.scheduleNextJoin(8000L);
                    this.joinCooldownTimer.reset();
                    return;
                }
            }
            if ((object = itemRuleCollection.findByItem(Items.COMPASS)) != null) {
                AutoJoin.minecraftClient.player.getInventory().selectedSlot = ((HotbarSlot)object).getSlotIndex();
                ((moscow.rockstar.mixin.accessors.ClientPlayerInteractionManagerAccessor)(Object)AutoJoin.minecraftClient.interactionManager).rockstar$sendSequencedPacket(AutoJoin.minecraftClient.world, n -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, n, AutoJoin.minecraftClient.player.getYaw(), AutoJoin.minecraftClient.player.getPitch()));
                this.joinCooldownTimer.reset();
            }
        }
        if (this.griefModeOption.isSelected()) {
            int n2 = this.parseAnarchyNumber();
            if (n2 <= 0) {
                this.disable();
                return;
            }
            if (ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) || ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY)) {
                if (ServerDetector.getDetectedServerMode() != n2 || !this.joinScreenOpen) {
                    if (!this.joinActionTimer.hasElapsed(500L)) {
                        return;
                    }
                    AutoJoin.minecraftClient.player.networkHandler.sendChatCommand("an" + this.anarchyNumberSetting.getValue());
                    this.joinScreenOpen = true;
                    this.joinActionTimer.reset();
                } else {
                    this.disable();
                }
            }
            if (ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD)) {
                if (ServerDetector.classicServerIndex == n2 && this.joinScreenOpen) {
                    this.disable();
                    return;
                }
                if (!this.serverJoinTimer.hasElapsed(300L)) {
                    return;
                }
                if (!this.joinPending) {
                    if (ContainerItemCache.openServerSelector()) {
                        this.joinPending = true;
                        this.serverJoinTimer.reset();
                    }
                    return;
                }
                object2 = AutoJoin.minecraftClient.player.currentScreenHandler;
                if (object2 instanceof GenericContainerScreenHandler) {
                    object = (GenericContainerScreenHandler)object2;
                    if (AutoJoin.minecraftClient.currentScreen != null) {
                        object2 = AutoJoin.minecraftClient.currentScreen.getTitle().getString();
                        if (ContainerItemCache.isServerSelectionScreen((String)object2)) {
                            int n3 = ContainerItemCache.findWorldEntrySlot((GenericContainerScreenHandler)object, true);
                            if (n3 != -1) {
                                ContainerItemCache.clickContainerSlot((GenericContainerScreenHandler)object, n3);
                                this.serverJoinTimer.reset();
                            }
                            return;
                        }
                        if (ContainerItemCache.isWorldSelectionScreen((String)object2, true)) {
                            ContainerItemCache.NavigationResult navigationResult = ContainerItemCache.resolveNavigation((GenericContainerScreenHandler)object, (String)object2, n2, true);
                            if (navigationResult == ContainerItemCache.NavigationResult.ITEM_SELECTED) {
                                this.serverJoinTimer.reset();
                                this.joinScreenOpen = true;
                            } else if (navigationResult == ContainerItemCache.NavigationResult.PAGE_NAVIGATION_NEEDED) {
                                this.serverJoinTimer.reset();
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isJoinFailureMessage(String string) {
        return string.contains("\u0441\u0435\u0440\u0432\u0435\u0440 \u0437\u0430\u043f\u043e\u043b\u043d\u0435\u043d") || string.contains("\u0441\u0435\u0440\u0432\u0435\u0440 \u043f\u0435\u0440\u0435\u043f\u043e\u043b\u043d\u0435\u043d") || string.contains("\u043d\u0435\u0442 \u0441\u0432\u043e\u0431\u043e\u0434\u043d\u044b\u0445 \u0441\u043b\u043e\u0442\u043e\u0432") || string.contains("\u043a\u0438\u043a\u043d\u0443\u0442\u044b \u043f\u0440\u0438 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0438") || string.contains("\u0441\u0435\u0440\u0432\u0435\u0440 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d");
    }

    private void scheduleNextJoin(long l) {
        this.scheduledJoinDelayMillis = l;
        this.serverChangeTimer.reset();
    }

    private void processJoinWorkflow() {
        RockstarClient.create().getUiComponentProcessor().enqueueToast(NotificationType.SUCCESS, Localization.translate("auto_join.success"), Localization.translate("auto_join.success.desc"));
        this.disable();
    }

    private int parseAnarchyNumber() {
        try {
            return Integer.parseInt(this.anarchyNumberSetting.getValue());
        }
        catch (NumberFormatException numberFormatException) {
            return -1;
        }
    }

    @Override
    public void onEnable() {
        this.joinPending = false;
        this.joinScreenOpen = false;
        this.serverChangePending = false;
        this.scheduledJoinDelayMillis = 0L;
        this.joinCooldownTimer.reset();
        this.joinActionTimer.reset();
        this.serverJoinTimer.reset();
        this.serverChangeTimer.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.joinPending = false;
        this.joinScreenOpen = false;
        this.serverChangePending = false;
        this.scheduledJoinDelayMillis = 0L;
        this.serverJoinTimer.reset();
        super.onDisable();
    }
}
