/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Difficulty
 *  net.minecraft.GenericContainerScreenHandler
 *  net.minecraft.Items
 *  net.minecraft.Text
 *  net.minecraft.OpenScreenS2CPacket
 *  net.minecraft.HandledScreen
 *  net.minecraft.PlayerListEntry
 */
package moscow.rockstar.events;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.api.commands.CommandBuilder;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.ContainerItemCache;
import moscow.rockstar.inventory.ContainerSlotResolver;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.mixin.accessors.PlayerListHudAccessor;
import moscow.rockstar.server.PartySize;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import moscow.rockstar.util.Timer;
import net.minecraft.world.Difficulty;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.PlayerListEntry;
import pyrock.events.network.ReceivePacketEvent;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

public class EventHandlerFactory
implements ClientAccess {
    private static final ItemRuleCollection<ItemRule> RECONNECT_RULES = ItemRuleSets.getInventoryRules().combineRules(ItemRuleSets.getHotbarRules()).combineRules(ItemRuleSets.getOffhandRules());
    private final ReconnectWorkflowState workflowState = new ReconnectWorkflowState();
    private final Timer workflowTimer = new Timer();
    private final EventListener<ClientPlayerTickEvent> tickListener = clientPlayerTickEvent -> {
        if (!this.workflowState.active || EventHandlerFactory.minecraftClient.world == null || EventHandlerFactory.minecraftClient.player == null) {
            return;
        }
        if (ServerDetector.isInventoryServer()) {
            this.processContainerScreen();
        } else if (ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY) || ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME)) {
            this.sendAnarchyReconnectCommand();
        } else if (ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD)) {
            this.advanceContainerSelection();
        }
    };
    private final EventListener<ReceivePacketEvent> packetListener = receivePacketEvent -> {
        if (!this.workflowState.active || EventHandlerFactory.minecraftClient.player == null || EventHandlerFactory.minecraftClient.world == null) {
            return;
        }
        if (ServerDetector.isInventoryServer()
            && !(receivePacketEvent.getPacket() instanceof OpenScreenS2CPacket)
            && RECONNECT_RULES.findByStack(stack -> stack.getItem() == Items.COMPASS) != null) {
            if (this.workflowState.mode == ReconnectMode.LIGHT_PARTY_MODE) {
                if (!this.workflowState.liteCommandSent) {
                    this.workflowState.liteCommandSent = true;
                    this.resetMenuNavigation();
                    EventHandlerFactory.minecraftClient.player.networkHandler.sendChatCommand("lite");
                }
            } else if (!this.workflowState.menuCommandSent) {
                this.workflowState.menuCommandSent = true;
                EventHandlerFactory.minecraftClient.player.networkHandler.sendChatCommand("menu");
            }
        }
        if (ServerDetector.isInventoryServer() && receivePacketEvent.getPacket() instanceof OpenScreenS2CPacket screenPacket) {
            String object = screenPacket.getName().getString();
            if (this.workflowState.mode == ReconnectMode.LIGHT_PARTY_MODE && ((String)object).contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c")) {
                this.workflowState.navigationStep = ReconnectNavigationStep.LIGHT_MODE_MENU;
            } else if (this.workflowState.mode == ReconnectMode.LIGHT_PARTY_MODE && ((String)object).contains("\u0412\u044b\u0431\u043e\u0440 \u041b\u0430\u0439\u0442 \u0430\u043d\u0430\u0440\u0445\u0438\u0438") && this.workflowState.navigationStep != ReconnectNavigationStep.LIGHT_ANARCHY_MENU) {
                this.resetMenuNavigation();
            } else if (this.workflowState.mode == ReconnectMode.LIGHT_MODE && ((String)object).contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c")) {
                this.workflowState.navigationStep = ReconnectNavigationStep.LIGHT_SERVER_MENU;
            } else if (this.workflowState.mode == ReconnectMode.LIGHT_MODE && ((String)object).contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0441\u0435\u0440\u0432\u0435\u0440 \u041b\u0430\u0439\u0442")) {
                this.workflowState.navigationStep = ReconnectNavigationStep.LIGHT_SERVER_SELECTION;
            } else if (this.workflowState.mode == ReconnectMode.CLASSIC_MODE && ((String)object).contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c")) {
                this.workflowState.navigationStep = ReconnectNavigationStep.CLASSIC_MODE_MENU;
            } else if (this.workflowState.mode == ReconnectMode.CLASSIC_MODE && ((String)object).contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u041a\u043b\u0430\u0441\u0441\u0438\u0447")) {
                this.workflowState.navigationStep = ReconnectNavigationStep.CLASSIC_SERVER_SELECTION;
            }
        }
    };

    public EventHandlerFactory() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    public final ServiceRegistry getCommandRegistration() {
        return CommandBuilder.command("rct")
            .aliases("reconnect")
            .description("commands.rehub.description")
            .argument("args", argument -> argument.optional().vararg().validator(PluginResolver::resolveValue))
            .handler(this::handleReconnectCommand)
            .build();
    }

    @Compile
    private void handleReconnectCommand(DispatchContext dispatchContext) {
        Object object;
        if (EventHandlerFactory.minecraftClient.player == null || EventHandlerFactory.minecraftClient.world == null) {
            return;
        }
        List list = dispatchContext.getArguments().isEmpty() ? List.of() : this.getCommandArguments(dispatchContext.getArguments().getFirst());
        ReconnectServerType reconnectServerType = ReconnectServerType.UNSPECIFIED;
        Integer n = null;
        if (!list.isEmpty()) {
            ReconnectServerType parsedServerType = ReconnectServerType.parseServerType((String)list.getFirst());
            int n2 = 0;
            if (parsedServerType != null) {
                reconnectServerType = parsedServerType;
                ++n2;
            }
            if (n2 < list.size() && (n = this.parsePositiveInteger((String)list.get(n2))) == null) {
                Notification.error(Text.of((String)Localization.translate("commands_rehub.invalid_number")));
                return;
            }
        }
        if (!dispatchContext.getArguments().isEmpty() && (object = dispatchContext.getArguments().getFirst()) instanceof Integer) {
            Integer n3;
            n = n3 = (Integer)object;
        }
        this.workflowState.resetWorkflowState();
        this.workflowState.serverType = this.resolveServerType(reconnectServerType);
        this.detectServerModeFromHeader();
        if (n != null) {
            if (ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD)) {
                if (this.workflowState.serverType == ReconnectServerType.ANARCHY || this.workflowState.serverType == ReconnectServerType.GRIEF) {
                    this.workflowState.selectedNumber = n;
                }
            } else if (ServerDetector.isInventoryServer()) {
                if (this.workflowState.mode == ReconnectMode.UNKNOWN_MODE) {
                    this.workflowState.mode = ReconnectMode.LIGHT_PARTY_MODE;
                }
                if (!this.validateReconnectNumber(n)) {
                    this.workflowState.resetWorkflowState();
                    return;
                }
            } else {
                this.workflowState.selectedNumber = ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) ? n.intValue() : n.intValue();
            }
        } else if (ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD) && this.workflowState.serverType == ReconnectServerType.ANARCHY) {
            Notification.error(Text.of((String)Localization.translate("commands_rehub.invalid_number")));
            return;
        }
        if (ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) || ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY)) {
            this.workflowState.selectedNumber = this.getRequestedServerNumber();
            if (this.workflowState.selectedNumber <= 0) {
                Notification.error(Text.of((String)Localization.translate("commands_rehub.invalid_number")));
                this.workflowState.resetWorkflowState();
                return;
            }
        }
        this.workflowTimer.reset();
        EventHandlerFactory.minecraftClient.player.networkHandler.sendChatCommand("hub");
        this.workflowState.active = true;
    }

    private void sendAnarchyReconnectCommand() {
        int n;
        if (!this.workflowTimer.hasElapsed(1000L)) {
            return;
        }
        int n2 = n = this.workflowState.selectedNumber > 0 ? this.workflowState.selectedNumber : this.getRequestedServerNumber();
        if (n <= 0) {
            Notification.error(Text.of((String)Localization.translate("commands_rehub.invalid_number")));
            this.stopReconnectProcess();
            return;
        }
        if (EventHandlerFactory.minecraftClient.world.getDifficulty() != Difficulty.EASY && !ServerDetector.isServerEnvironmentReady() && !this.workflowTimer.hasElapsed(4000L)) {
            return;
        }
        EventHandlerFactory.minecraftClient.player.networkHandler.sendChatCommand("an" + n);
        this.workflowTimer.reset();
        this.workflowState.active = false;
    }

    private void advanceContainerSelection() {
        int n;
        boolean bl;
        GenericContainerScreenHandler class_17072;
        Object object;
        block14: {
            block13: {
                if (!this.workflowTimer.hasElapsed(300L)) {
                    return;
                }
                if (!this.workflowState.inventoryScanActive) {
                    if (ContainerItemCache.openServerSelector()) {
                        this.workflowState.inventoryScanActive = true;
                        this.workflowTimer.reset();
                    }
                    return;
                }
                object = EventHandlerFactory.minecraftClient.player.currentScreenHandler;
                if (!(object instanceof GenericContainerScreenHandler)) break block13;
                class_17072 = (GenericContainerScreenHandler)object;
                if (EventHandlerFactory.minecraftClient.currentScreen != null) break block14;
            }
            return;
        }
        object = EventHandlerFactory.minecraftClient.currentScreen.getTitle().getString();
        boolean bl2 = bl = this.workflowState.serverType == ReconnectServerType.GRIEF;
        if (ContainerItemCache.isServerSelectionScreen((String)object)) {
            int n2 = ContainerItemCache.findWorldEntrySlot(class_17072, bl);
            if (n2 != -1) {
                ContainerItemCache.clickContainerSlot(class_17072, n2);
                this.workflowTimer.reset();
            }
            return;
        }
        if (!ContainerItemCache.isWorldSelectionScreen((String)object, bl)) {
            return;
        }
        int n3 = n = this.workflowState.selectedNumber > 0 ? this.workflowState.selectedNumber : this.getFallbackServerCount();
        if (n <= 0) {
            Notification.error(Text.of((String)Localization.translate("commands_rehub.invalid_number")));
            this.stopReconnectProcess();
            return;
        }
        ContainerItemCache.NavigationResult navigationResult = ContainerItemCache.resolveNavigation(class_17072, (String)object, n, bl);
        if (navigationResult == ContainerItemCache.NavigationResult.ITEM_SELECTED) {
            this.stopReconnectProcess();
        } else if (navigationResult == ContainerItemCache.NavigationResult.PAGE_NAVIGATION_NEEDED) {
            this.workflowTimer.reset();
        }
    }

    private void detectServerModeFromHeader() {
        CharSequence charSequence;
        this.workflowState.mode = ReconnectMode.UNKNOWN_MODE;
        this.workflowState.serverName = "";
        this.workflowState.selectedNumber = -1;
        if (!ServerDetector.isInventoryServer() || EventHandlerFactory.minecraftClient.player == null || EventHandlerFactory.minecraftClient.inGameHud == null) {
            return;
        }
        Text class_25612 = null;
        try {
            class_25612 = ((PlayerListHudAccessor)EventHandlerFactory.minecraftClient.inGameHud.getPlayerListHud()).getHeader();
        }
        catch (RuntimeException runtimeException) {
            RockstarClient.LOGGER.debug("Failed to read tab header for reconnect mode detection", (Throwable)runtimeException);
        }
        if ((class_25612 == null || class_25612.getString().isBlank()) && EventHandlerFactory.minecraftClient.player.networkHandler != null && EventHandlerFactory.minecraftClient.player.networkHandler.getPlayerList() != null) {
            charSequence = new StringBuilder();
            StringBuilder playerNames = new StringBuilder();
            EventHandlerFactory.minecraftClient.player.networkHandler.getPlayerList().forEach(arg_0 -> EventHandlerFactory.appendPlayerName(playerNames, arg_0));
            charSequence = playerNames;
            if (!charSequence.isEmpty()) {
                class_25612 = Text.of((String)((StringBuilder)charSequence).toString());
            }
        }
        if (class_25612 == null) {
            return;
        }
        charSequence = class_25612.getString();
        if (((String)charSequence).isBlank()) {
            return;
        }
        if (((String)charSequence).contains("\u041a\u043b\u0430\u0441\u0441\u0438\u043a")) {
            this.workflowState.mode = ReconnectMode.CLASSIC_MODE;
            this.parseServerHeader((String)charSequence, "\u041a\u043b\u0430\u0441\u0441\u0438\u043a");
        } else if (!(!((String)charSequence).contains("\u041b\u0430\u0439\u0442") || ((String)charSequence).contains("\u0421\u043e\u043b\u043e\u041b\u0430\u0439\u0442") || ((String)charSequence).contains("\u0414\u0443\u043e\u041b\u0430\u0439\u0442") || ((String)charSequence).contains("\u0422\u0440\u0438\u043e\u041b\u0430\u0439\u0442") || ((String)charSequence).contains("\u041a\u043b\u0430\u043d\u041b\u0430\u0439\u0442"))) {
            this.workflowState.mode = ReconnectMode.LIGHT_MODE;
            this.parseServerHeader((String)charSequence, "\u041b\u0430\u0439\u0442");
        } else if (((String)charSequence).contains("\u041b\u0430\u0439\u0442")) {
            this.workflowState.mode = ReconnectMode.LIGHT_PARTY_MODE;
            this.parseServerHeader((String)charSequence, "\u041b\u0430\u0439\u0442");
        }
    }

    private void parseServerHeader(String string, String string2) {
        try {
            String[] stringArray = string.split("\u25b6");
            if (stringArray.length < 2) {
                return;
            }
            String string3 = stringArray[1].replace("\u0410\u043d\u0430\u0440\u0445\u0438\u044f", "").trim();
            String[] stringArray2 = string3.split("#");
            if (stringArray2.length < 2) {
                return;
            }
            this.workflowState.serverName = string3.replace(string2, "").replaceAll("#\\d+", "").trim();
            String string4 = stringArray2[1].replaceAll("[^0-9]", "").trim();
            if (string4.isEmpty()) {
                return;
            }
            this.workflowState.selectedNumber = Integer.parseInt(string4);
        }
        catch (Exception exception) {
            this.workflowState.serverName = "";
            this.workflowState.selectedNumber = -1;
        }
    }

    private void resetMenuNavigation() {
        this.workflowState.navigationStep = ReconnectNavigationStep.LIGHT_ANARCHY_MENU;
        this.workflowState.slotClickPending = false;
        this.workflowState.clickedSlots.clear();
        this.workflowState.slotRetryCount = 0;
    }

    private void processContainerScreen() {
        HandledScreen BlockStateProviderType;
        Object object;
        block8: {
            block7: {
                object = EventHandlerFactory.minecraftClient.currentScreen;
                if (!(object instanceof HandledScreen)) break block7;
                BlockStateProviderType = (HandledScreen)object;
                object = EventHandlerFactory.minecraftClient.player.currentScreenHandler;
                if (object instanceof GenericContainerScreenHandler) break block8;
            }
            return;
        }
        GenericContainerScreenHandler class_17072 = (GenericContainerScreenHandler)object;
        object = BlockStateProviderType.getTitle().getString();
        switch (this.workflowState.mode.ordinal()) {
            case 1: {
                this.processLightModeScreen(class_17072, (String)object);
                break;
            }
            case 2: {
                this.processLightServerScreen(class_17072, (String)object);
                break;
            }
            case 3: {
                this.processClassicServerScreen(class_17072, (String)object);
            }
        }
    }

    private void processLightModeScreen(GenericContainerScreenHandler class_17072, String string) {
        if (string.contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c")) {
            ContainerSlotResolver.clickContainerSlot(class_17072, 12);
            this.resetMenuNavigation();
            return;
        }
        if (this.workflowState.navigationStep == ReconnectNavigationStep.LIGHT_ANARCHY_MENU && string.contains("\u0412\u044b\u0431\u043e\u0440 \u041b\u0430\u0439\u0442 \u0430\u043d\u0430\u0440\u0445\u0438\u0438")) {
            int n = this.findPartySlot(class_17072);
            if (this.isContainerSlotValid(class_17072, n)) {
                ContainerSlotResolver.clickContainerSlot(class_17072, n);
                this.stopReconnectProcess();
                return;
            }
            if (this.workflowState.slotRetryCount > 0) {
                --this.workflowState.slotRetryCount;
                return;
            }
            int n2 = this.findNextUnclickedSlot(class_17072);
            if (n2 == -1) {
                this.workflowState.navigationStep = ReconnectNavigationStep.NONE;
                return;
            }
            if (!this.workflowState.slotClickPending) {
                ContainerSlotResolver.clickContainerSlot(class_17072, n2);
                this.workflowState.slotClickPending = true;
                this.workflowState.clickedSlots.add(n2);
                this.workflowState.slotRetryCount = 5;
                return;
            }
            this.workflowState.slotClickPending = false;
        }
    }

    private boolean validateReconnectNumber(int n) {
        return switch (this.workflowState.mode.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                Notification.error(Text.of((String)Localization.translate("commands_rehub.mode_unknown")));
                yield false;
            }
            case 1 -> {
                PartySize var2_2 = this.partySizeFromIndex(n);
                if (var2_2 == PartySize.UNKNOWN) {
                    Notification.error(Text.of((String)Localization.translate("commands_rehub.invalid_number")));
                    yield false;
                }
                this.workflowState.selectedNumber = n;
                this.workflowState.serverName = this.formatPartySize(var2_2);
                yield true;
            }
            case 2 -> {
                if (n < 1 || n > 3) {
                    Notification.error(Text.of((String)Localization.translate("commands_rehub.invalid_number")));
                    yield false;
                }
                this.workflowState.selectedNumber = n;
                yield true;
            }
            case 3 -> {
                if (ContainerSlotResolver.getPartyMemberSlot(n) == -1) {
                    Notification.error(Text.of((String)Localization.translate("commands_rehub.invalid_number")));
                    yield false;
                }
                this.workflowState.selectedNumber = n;
                yield true;
            }
        };
    }

    private void processLightServerScreen(GenericContainerScreenHandler class_17072, String string) {
        int n;
        if (this.workflowState.navigationStep == ReconnectNavigationStep.LIGHT_SERVER_MENU && string.contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c")) {
            ContainerSlotResolver.clickContainerSlot(class_17072, 10);
            this.workflowState.navigationStep = ReconnectNavigationStep.LIGHT_SERVER_SELECTION;
            return;
        }
        if (this.workflowState.navigationStep == ReconnectNavigationStep.LIGHT_SERVER_SELECTION && string.contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0441\u0435\u0440\u0432\u0435\u0440 \u041b\u0430\u0439\u0442") && this.isContainerSlotValid(class_17072, n = ContainerSlotResolver.getPartyOptionSlot(this.workflowState.selectedNumber))) {
            ContainerSlotResolver.clickContainerSlot(class_17072, n);
            this.stopReconnectProcess();
        }
    }

    private void processClassicServerScreen(GenericContainerScreenHandler class_17072, String string) {
        int n;
        if (this.workflowState.navigationStep == ReconnectNavigationStep.CLASSIC_MODE_MENU && string.contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c")) {
            ContainerSlotResolver.clickContainerSlot(class_17072, 15);
            this.workflowState.navigationStep = ReconnectNavigationStep.CLASSIC_SERVER_SELECTION;
            return;
        }
        if (this.workflowState.navigationStep == ReconnectNavigationStep.CLASSIC_SERVER_SELECTION && string.contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u041a\u043b\u0430\u0441\u0441\u0438\u0447") && this.isContainerSlotValid(class_17072, n = ContainerSlotResolver.getPartyMemberSlot(this.workflowState.selectedNumber))) {
            ContainerSlotResolver.clickContainerSlot(class_17072, n);
            this.stopReconnectProcess();
        }
    }

    private boolean isContainerSlotValid(GenericContainerScreenHandler class_17072, int n) {
        return n >= 0 && n < class_17072.slots.size() && class_17072.getSlot(n).hasStack();
    }

    private int findNextUnclickedSlot(GenericContainerScreenHandler class_17072) {
        if (!this.workflowState.serverName.isBlank()) {
            int n = ContainerSlotResolver.findLightItemSlot(class_17072, this.workflowState.serverName);
            if (n != -1 && !this.workflowState.clickedSlots.contains(n)) {
                return n;
            }
            n = ContainerSlotResolver.getPartySizeIndex(this.parsePartySize(this.workflowState.serverName));
            if (n != -1 && !this.workflowState.clickedSlots.contains(n)) {
                return n;
            }
        }
        for (int n : ContainerSlotResolver.findPartyOptionSlots(class_17072)) {
            if (this.workflowState.clickedSlots.contains(n)) continue;
            return n;
        }
        return -1;
    }

    private int findPartySlot(GenericContainerScreenHandler class_17072) {
        return ContainerSlotResolver.findNumberedLightItem(class_17072, "", this.workflowState.selectedNumber);
    }

    private PartySize partySizeFromIndex(int n) {
        return ContainerSlotResolver.getPartySizeForSlot(n);
    }

    private PartySize parsePartySize(String string) {
        if (string == null) {
            return PartySize.UNKNOWN;
        }
        if (string.contains("\u0421\u043e\u043b\u043e")) {
            return PartySize.SOLO;
        }
        if (string.contains("\u0414\u0443\u043e")) {
            return PartySize.DUO;
        }
        if (string.contains("\u0422\u0440\u0438\u043e")) {
            return PartySize.TRIO;
        }
        if (string.contains("\u041a\u043b\u0430\u043d")) {
            return PartySize.CLAN;
        }
        return PartySize.UNKNOWN;
    }

    private String formatPartySize(PartySize partySize) {
        return switch (partySize) {
            case PartySize.SOLO -> "\u0421\u043e\u043b\u043e";
            case PartySize.DUO -> "\u0414\u0443\u043e";
            case PartySize.TRIO -> "\u0422\u0440\u0438\u043e";
            case PartySize.CLAN -> "\u041a\u043b\u0430\u043d";
            default -> "";
        };
    }

    private void stopReconnectProcess() {
        this.workflowState.resetWorkflowState();
        this.workflowTimer.reset();
    }

    private int getFallbackServerCount() {
        if (this.workflowState.serverType == ReconnectServerType.GRIEF) {
            return ServerDetector.classicServerIndex;
        }
        return -1;
    }

    private int getServerCountLimit() {
        if (this.workflowState.serverType == ReconnectServerType.GRIEF) {
            return ServerDetector.griefServerIndex;
        }
        return ServerDetector.defaultServerIndex;
    }

    private int getRequestedServerNumber() {
        if (this.workflowState.selectedNumber > 0) {
            return this.workflowState.selectedNumber;
        }
        int n = ServerDetector.getDetectedServerMode();
        if (n > 0) {
            return n;
        }
        return this.getServerCountLimit();
    }

    private ReconnectServerType resolveServerType(ReconnectServerType reconnectServerType) {
        if (reconnectServerType != ReconnectServerType.UNSPECIFIED) {
            return reconnectServerType;
        }
        if (ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD)) {
            return ReconnectServerType.GRIEF;
        }
        if ((ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) || ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY)) && ServerDetector.griefServerDetected) {
            return ReconnectServerType.GRIEF;
        }
        return ReconnectServerType.ANARCHY;
    }

    private List<String> getCommandArguments(Object object) {
        return object == null ? List.of() : (List)object;
    }

    private Integer parsePositiveInteger(String string) {
        try {
            int n = Integer.parseInt(string);
            return n > 0 ? Integer.valueOf(n) : null;
        }
        catch (NumberFormatException numberFormatException) {
            return null;
        }
    }

    private static /* synthetic */ void appendPlayerName(StringBuilder stringBuilder, PlayerListEntry ServerSamplerSource) {
        if (ServerSamplerSource.getProfile() != null && ServerSamplerSource.getProfile().getName() != null) {
            stringBuilder.append(ServerSamplerSource.getProfile().getName()).append(" ");
        }
    }

    static class ReconnectWorkflowState {
        boolean active;
        ReconnectMode mode = ReconnectMode.UNKNOWN_MODE;
        String serverName = "";
        int selectedNumber = -1;
        ReconnectNavigationStep navigationStep = ReconnectNavigationStep.NONE;
        boolean slotClickPending;
        int slotRetryCount;
        boolean menuCommandSent;
        boolean inventoryScanActive;
        ReconnectServerType serverType = ReconnectServerType.UNSPECIFIED;
        boolean liteCommandSent;
        final List<Integer> clickedSlots = new ArrayList<Integer>();

        ReconnectWorkflowState() {
        }

        final void resetWorkflowState() {
            this.active = false;
            this.mode = ReconnectMode.UNKNOWN_MODE;
            this.serverName = "";
            this.selectedNumber = -1;
            this.inventoryScanActive = false;
            this.navigationStep = ReconnectNavigationStep.NONE;
            this.serverType = ReconnectServerType.UNSPECIFIED;
            this.slotClickPending = false;
            this.slotRetryCount = 0;
            this.menuCommandSent = false;
            this.clickedSlots.clear();
            this.liteCommandSent = false;
        }
    }

    static enum ReconnectServerType {
        UNSPECIFIED,
        ANARCHY,
        GRIEF;

        public static ReconnectServerType parseServerType(String string) {
            if (string == null) {
                return null;
            }
            String string2 = string.toLowerCase();
            if (string2.startsWith("an") || string2.contains("\u0430\u043d\u0430\u0440\u0445")) {
                return ANARCHY;
            }
            if (string2.startsWith("gr") || string2.contains("\u0433\u0440\u0438\u0444")) {
                return GRIEF;
            }
            return null;
        }
}

    static enum ReconnectMode {
        UNKNOWN_MODE,
        LIGHT_PARTY_MODE,
        LIGHT_MODE,
        CLASSIC_MODE;
}

    static enum ReconnectNavigationStep {
        NONE,
        LIGHT_MODE_MENU,
        LIGHT_ANARCHY_MENU,
        LIGHT_SERVER_MENU,
        LIGHT_SERVER_SELECTION,
        CLASSIC_MODE_MENU,
        CLASSIC_SERVER_SELECTION;
}
}

