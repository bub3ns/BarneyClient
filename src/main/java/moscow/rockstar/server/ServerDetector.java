/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.minecraft.Difficulty
 *  net.minecraft.GenericContainerScreenHandler
 *  net.minecraft.World
 *  net.minecraft.Blocks
 *  net.minecraft.Block
 *  net.minecraft.Text
 *  net.minecraft.ScoreboardObjective
 *  net.minecraft.Team
 *  net.minecraft.Scoreboard
 *  net.minecraft.ConnectScreen
 *  net.minecraft.Screen
 *  net.minecraft.TitleScreen
 *  net.minecraft.HandledScreen
 *  net.minecraft.MultiplayerScreen
 *  net.minecraft.ServerAddress
 *  net.minecraft.ServerInfo
 *  net.minecraft.ServerInfo$ServerType
 *  net.minecraft.ScoreboardDisplaySlot
 *  net.minecraft.ScoreboardEntry
 */
package moscow.rockstar.server;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.Generated;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.inventory.ContainerSlotResolver;
import moscow.rockstar.mixin.accessors.PlayerListHudAccessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.server.PartySize;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.server.ServerProfileEntry;
import moscow.rockstar.server.ServerSelectionContext;
import moscow.rockstar.server.ServerSelectionStep;
import moscow.rockstar.util.Timer;
import net.minecraft.world.Difficulty;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.text.Text;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import pyrock.events.game.GameTickEvent;

public final class ServerDetector
implements ClientAccess {
    public static boolean enabled;
    public static int requestedServerNumber;
    public static int defaultServerIndex;
    public static int griefServerIndex;
    public static int classicServerIndex;
    public static int anarchyPvpServerNumber;
    public static int anarchyServerNumber;
    public static boolean griefServerDetected;
    public static boolean duelServerDetected;
    public static boolean version121ServerDetected;
    public static boolean ratingSidebarDetected;
    public static String anarchyServerName;
    private static String requestedServerAddress;
    private static String requestedServerCommand;
    private static boolean connectionPending;
    private static ServerProfileEntry selectedServerProfile;
    private static boolean selectionWorkflowActive;
    private static ServerSelectionStep selectionStep;
    private static ServerSelectionContext selectionContext;
    private static boolean hubCommandSent;
    private static boolean menuCommandSent;
    private static boolean slotSelectionStarted;
    private static int categorySlot;
    private static int serverSlot;
    private static final List<Integer> clickedSlots;
    private static int lightServerSlot;
    private static int classicServerSlot;
    private static final Timer selectionCooldown;
    static final EventListener<GameTickEvent> tickListener;
    private static final ListenerRegistration listenerRegistration;

    public static boolean isServerProfileSupported(ServerProfile serverProfile) {
        return serverProfile.matchesAddress(ServerDetector.getConnectedServerAddress());
    }

    public static String getConnectedServerAddress() {
        if (ServerDetector.minecraftClient.player == null || ServerDetector.minecraftClient.player.networkHandler.getServerInfo() == null) {
            return "single";
        }
        return ServerDetector.minecraftClient.player.networkHandler.getServerInfo().address;
    }

    public static String resolveConnectedServerAddress() {
        if (ServerDetector.minecraftClient.player == null || ServerDetector.minecraftClient.player.networkHandler.getServerInfo() == null) {
            return "single";
        }
        String string = ServerDetector.minecraftClient.player.networkHandler.getServerInfo().address;
        try {
            InetAddress inetAddress = InetAddress.getByName(string);
            return inetAddress.getHostAddress();
        }
        catch (UnknownHostException unknownHostException) {
            return string;
        }
    }

    public static boolean serverAddressContains(String string) {
        return ServerDetector.getConnectedServerAddress().toLowerCase().contains(string.toLowerCase());
    }

    public static boolean isDuelServer() {
        Text class_25612;
        if (!ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY)) {
            return false;
        }
        if (ServerDetector.minecraftClient.inGameHud != null && (class_25612 = ((PlayerListHudAccessor)ServerDetector.minecraftClient.inGameHud.getPlayerListHud()).getHeader()) != null && class_25612.getString().toLowerCase(Locale.ROOT).contains("\u0434\u0443\u044d\u043b")) {
            return true;
        }
        boolean bl = false;
        boolean bl2 = false;
        for (String string : ServerDetector.getScoreboardEntries()) {
            String string2 = string.toLowerCase(Locale.ROOT);
            if (string2.contains("\u0434\u0443\u044d\u043b")) {
                return true;
            }
            if (string2.contains("\u0443\u0431\u0438\u0439\u0441\u0442\u0432")) {
                bl = true;
            }
            if (!string2.contains("\u0441\u043c\u0435\u0440\u0442")) continue;
            bl2 = true;
        }
        return bl && bl2;
    }

    public static List<String> getScoreboardEntries() {
        if (ServerDetector.minecraftClient.world == null) {
            return List.of();
        }
        Scoreboard CachedBlockPosition = ServerDetector.minecraftClient.world.getScoreboard();
        ScoreboardObjective class_2662 = CachedBlockPosition.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (class_2662 == null) {
            return List.of();
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        for (ScoreboardEntry class_90112 : CachedBlockPosition.getScoreboardEntries(class_2662)) {
            String owner = class_90112.owner();
            String displayText = class_90112.display() == null ? owner : class_90112.display().getString();
            Team team = CachedBlockPosition.getScoreHolderTeam(owner);
            if (team != null) {
                displayText = team.getPrefix().getString() + displayText + team.getSuffix().getString();
            }
            arrayList.add(displayText);
        }
        return arrayList;
    }

    public static boolean isFuntimeServer() {
        if (ServerDetector.minecraftClient.inGameHud == null) {
            return false;
        }
        Text class_25612 = ((PlayerListHudAccessor)ServerDetector.minecraftClient.inGameHud.getPlayerListHud()).getHeader();
        if (class_25612 == null) {
            return false;
        }
        String string = class_25612.getString().toLowerCase(Locale.ROOT);
        return string.contains("funtime") || string.contains("fun time") || string.contains("\u0444\u0430\u043d\u0442\u0430\u0439\u043c");
    }

    public static String getFormattedServerName(boolean bl) {
        return ServerDetector.formatServerName(ServerDetector.getConnectedServerAddress(), bl);
    }

    public static String formatServerName(String string, boolean bl) {
        String[] stringArray = string.split("\\.");
        if (ServerDetector.serverAddressContains("liquidproxy")) {
            return bl ? "LP" : "LiquidProxy";
        }
        if (minecraftClient.isInSingleplayer()) {
            return ServerDetector.formatKnownServerName(string, bl);
        }
        if (stringArray.length == 3) {
            return ServerDetector.formatKnownServerName(stringArray[1], bl);
        }
        if (stringArray.length == 2) {
            return ServerDetector.formatKnownServerName(stringArray[0], bl);
        }
        if (string.contains(":")) {
            return string.split(":")[0];
        }
        return string;
    }

    private static String formatKnownServerName(String object, boolean bl) {
        object = ((String)object).replace("-", "");
        ArrayList<ServerNameVariant> arrayList = new ArrayList<ServerNameVariant>();
        String[] stringArray = new String[]{"legacy", "bars", "world", "best", "times", "time", "shine", "sky", "lands", "land", "trainer", "server", "blaze", "mine", "lord", "cube", "grief", "craft", "rise", "force", "project", "lite", "client"};
        Arrays.stream(stringArray).forEach(string -> arrayList.add(ServerDetector.createServerNameVariant(string)));
        arrayList.addAll(Arrays.asList(new ServerNameVariant("mc", "MC", "-MC"), new ServerNameVariant("hvh", "HVH", "-HVH"), new ServerNameVariant("pvp", "PVP", "PVP")));
        if (minecraftClient.isInSingleplayer() && !bl) {
            object = "LocalHost";
        }
        if (ServerDetector.serverAddressContains("sunmc")) {
            Object object2 = object = bl ? "SR" : "SunRise";
        }
        if (ServerDetector.serverAddressContains("saturn")) {
            Object object3 = object = bl ? "S-X" : "SaturnX";
        }
        if (ServerDetector.serverAddressContains("sunw")) {
            object = bl ? "SW" : "SunWay";
        }
        for (ServerNameVariant serverNameVariant : arrayList) {
            if (!((String)object).contains(serverNameVariant.originalName)) continue;
            if (bl) {
                object = ((String)object).substring(0, 1).toUpperCase() + serverNameVariant.shortName;
            } else {
                object = ((String)object).replace(serverNameVariant.originalName, serverNameVariant.displayName);
                object = ((String)object).substring(0, 1).toUpperCase() + ((String)object).substring(1);
            }
            return object;
        }
        object = ((String)object).substring(0, 1).toUpperCase() + ((String)object).substring(1);
        return object;
    }

    public static boolean isServerEnvironmentReady() {
        if (ServerDetector.minecraftClient.player == null || ServerDetector.minecraftClient.world == null) {
            return false;
        }
        BlockPos adminsky = ServerDetector.minecraftClient.player.getBlockPos();
        Block class_22482 = ServerDetector.minecraftClient.world.getBlockState(adminsky.down(1)).getBlock();
        Block class_22483 = ServerDetector.minecraftClient.world.getBlockState(new BlockPos(adminsky.getX(), 0, adminsky.getZ())).getBlock();
        Block class_22484 = ServerDetector.minecraftClient.world.getBlockState(new BlockPos(adminsky.getX(), 6, adminsky.getZ())).getBlock();
        if (ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) && ServerDetector.minecraftClient.world.getDifficulty() == Difficulty.NORMAL) {
            return false;
        }
        if (ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) || ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY)) {
            return class_22482 == Blocks.AIR || class_22483 == Blocks.AIR;
        }
        if (ServerDetector.isServerProfileSupported(ServerProfile.HOLYWORLD)) {
            return class_22484 == Blocks.SAND;
        }
        if (ServerDetector.minecraftClient.world.getRegistryKey() == World.OVERWORLD) {
            return class_22483 == Blocks.BEDROCK || class_22482 == Blocks.BEDROCK;
        }
        return false;
    }

    private static ServerNameVariant createServerNameVariant(String string) {
        return new ServerNameVariant(string, string.substring(0, 1).toUpperCase() + string.substring(1), string.substring(0, 1).toUpperCase());
    }

    public static int getDetectedServerMode() {
        if (ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME)) {
            if (griefServerDetected) {
                return griefServerIndex;
            }
            if (ServerDetector.minecraftClient.world.getDifficulty() == Difficulty.EASY) {
                return -1;
            }
            return defaultServerIndex;
        }
        if (ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY)) {
            if (duelServerDetected) {
                return 1;
            }
            return defaultServerIndex;
        }
        return ServerDetector.isServerProfileSupported(ServerProfile.HOLYWORLD) ? anarchyServerNumber : -1;
    }

    public static void connectToServer(String string, String string2) {
        String string3;
        Object object;
        int n;
        String string4;
        if (minecraftClient == null) {
            return;
        }
        requestedServerAddress = ServerDetector.normalizeServerAddress(string);
        requestedServerCommand = string4 = ServerDetector.normalizeServerCommand(string2);
        ServerDetector.resetSelectionWorkflow();
        boolean bl = false;
        if (ServerDetector.isLightServerRequest(requestedServerAddress, string4) && (n = ContainerSlotResolver.getPartyOptionSlot((Integer)(object = ServerDetector.parseServerNumber(string4)))) >= 0) {
            ServerDetector.beginLightServerSelection(n);
            bl = true;
        }
        if (!bl && ServerDetector.isClassicServerRequest(requestedServerAddress, string4) && (n = ContainerSlotResolver.getPartyMemberSlot((Integer)(object = ServerDetector.parseServerNumber(string4)))) >= 0) {
            ServerDetector.beginClassicServerSelection(n);
            bl = true;
        }
        if (!bl && ServerDetector.isLiteServerAddress(requestedServerAddress, string4) && (object = ServerDetector.parseServerProfile(string4)) != null) {
            ServerDetector.beginLightAnarchySelection((ServerProfileEntry)object);
            bl = true;
        }
        connectionPending = true;
        boolean bl2 = ServerDetector.isConnectedToServer(requestedServerAddress);
        if (!bl2 && minecraftClient.getCurrentServerEntry() != null && (string3 = ServerDetector.minecraftClient.getCurrentServerEntry().address) != null) {
            bl2 = ServerDetector.normalizeServerAddress(string3).equals(requestedServerAddress);
        }
        if (bl2) {
            if (ServerDetector.minecraftClient.player != null && ServerDetector.isServerJoinScreenReady()) {
                ServerDetector.minecraftClient.player.networkHandler.sendChatCommand(requestedServerCommand);
                ServerDetector.resetConnectionAttempt();
            }
            return;
        }
        String string5 = ServerDetector.removePort(requestedServerAddress);
        ServerInfo class_6422 = new ServerInfo(string5, requestedServerAddress, ServerInfo.ServerType.OTHER);
        minecraftClient.disconnect();
        minecraftClient.execute(() -> ConnectScreen.connect((Screen)new MultiplayerScreen((Screen)new TitleScreen()), (MinecraftClient)minecraftClient, (ServerAddress)ServerAddress.parse((String)requestedServerAddress), (ServerInfo)class_6422, (boolean)false, null));
    }

    private static void resetConnectionAttempt() {
        requestedServerAddress = null;
        requestedServerCommand = null;
        connectionPending = false;
        ServerDetector.resetSelectionWorkflow();
    }

    private static boolean isConnectedToServer(String string) {
        if (string == null || string.isEmpty()) {
            return false;
        }
        String string2 = ServerDetector.getConnectedServerAddress();
        if (string2 == null || string2.equals("single")) {
            return false;
        }
        return ServerDetector.normalizeServerAddress(string2).equals(string);
    }

    private static void advanceSelectionWorkflow() {
        if (!selectionWorkflowActive || ServerDetector.minecraftClient.player == null) {
            return;
        }
        if (selectionContext == ServerSelectionContext.UNRESOLVED) {
            ServerDetector.resetConnectionAttempt();
            return;
        }
        if (selectionContext == ServerSelectionContext.LIGHT_ANARCHY && selectedServerProfile == null) {
            ServerDetector.resetConnectionAttempt();
            return;
        }
        if (selectionContext == ServerSelectionContext.LIGHT_SERVER && lightServerSlot < 0) {
            ServerDetector.resetConnectionAttempt();
            return;
        }
        if (selectionContext == ServerSelectionContext.CLASSIC_SERVER && classicServerSlot < 0) {
            ServerDetector.resetConnectionAttempt();
            return;
        }
        if (!hubCommandSent) {
            ServerDetector.minecraftClient.player.networkHandler.sendChatCommand("hub");
            hubCommandSent = true;
            selectionCooldown.reset();
            return;
        }
        if (!menuCommandSent) {
            if (selectionCooldown.hasElapsed(600L)) {
                ServerDetector.minecraftClient.player.networkHandler.sendChatCommand("menu");
                menuCommandSent = true;
                selectionCooldown.reset();
            }
            return;
        }
        if (ServerDetector.minecraftClient.currentScreen == null) {
            ServerSelectionStep serverSelectionStep = ServerDetector.getInitialSelectionStep();
            if (selectionStep != serverSelectionStep) {
                selectionStep = serverSelectionStep;
                slotSelectionStarted = false;
            }
            if (selectionCooldown.hasElapsed(2000L)) {
                ServerDetector.minecraftClient.player.networkHandler.sendChatCommand("menu");
                selectionCooldown.reset();
            }
            return;
        }
        Screen currentScreen = ServerDetector.minecraftClient.currentScreen;
        if (!(currentScreen instanceof HandledScreen handledScreen)) {
            return;
        }
        if (!(ServerDetector.minecraftClient.player.currentScreenHandler instanceof GenericContainerScreenHandler containerHandler)) {
            return;
        }
        String screenTitle = handledScreen.getTitle().getString();
        if (selectionContext == ServerSelectionContext.LIGHT_ANARCHY && screenTitle.contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c") && selectionStep != ServerSelectionStep.MODE_MENU) {
            slotSelectionStarted = false;
            selectionStep = ServerSelectionStep.MODE_MENU;
        }
        if (screenTitle.contains("\u0412\u044b\u0431\u043e\u0440 \u041b\u0430\u0439\u0442 \u0430\u043d\u0430\u0440\u0445\u0438\u0438") && selectionStep == ServerSelectionStep.MODE_MENU) {
            selectionStep = ServerSelectionStep.LIGHT_SERVER_MENU;
            slotSelectionStarted = false;
        }
        if (selectionContext == ServerSelectionContext.LIGHT_ANARCHY) {
            if (selectionStep == ServerSelectionStep.MODE_MENU) {
                if (screenTitle.contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c")) {
                    ContainerSlotResolver.clickContainerSlot(containerHandler, 12);
                    selectionStep = ServerSelectionStep.LIGHT_SERVER_MENU;
                    selectionCooldown.reset();
                }
                return;
            }
            if (selectionStep == ServerSelectionStep.LIGHT_SERVER_MENU) {
                if (screenTitle.contains("\u0412\u044b\u0431\u043e\u0440 \u041b\u0430\u0439\u0442 \u0430\u043d\u0430\u0440\u0445\u0438\u0438")) {
                    int n = ContainerSlotResolver.findNumberedLightItem(containerHandler, "", selectedServerProfile.getNumber());
                    if (n >= 0) {
                        ContainerSlotResolver.clickContainerSlot(containerHandler, n);
                        ServerDetector.resetConnectionAttempt();
                        return;
                    }
                    if (!slotSelectionStarted) {
                        if (categorySlot < 0) {
                            ServerDetector.resetConnectionAttempt();
                            return;
                        }
                        ContainerSlotResolver.clickContainerSlot(containerHandler, categorySlot);
                        clickedSlots.add(categorySlot);
                        slotSelectionStarted = true;
                        selectionStep = ServerSelectionStep.LIGHT_SERVER_SELECTED;
                        selectionCooldown.reset();
                    }
                }
                return;
            }
            if (selectionStep == ServerSelectionStep.LIGHT_SERVER_SELECTED && screenTitle.contains("\u0412\u044b\u0431\u043e\u0440 \u041b\u0430\u0439\u0442 \u0430\u043d\u0430\u0440\u0445\u0438\u0438")) {
                int n = ContainerSlotResolver.findNumberedLightItem(containerHandler, "", selectedServerProfile.getNumber());
                if (n >= 0) {
                    ContainerSlotResolver.clickContainerSlot(containerHandler, n);
                    ServerDetector.resetConnectionAttempt();
                    return;
                }
                if (!selectionCooldown.hasElapsed(250L)) {
                    return;
                }
                int n2 = ServerDetector.findAvailableServerSlot(containerHandler);
                if (n2 < 0) {
                    ServerDetector.resetConnectionAttempt();
                    return;
                }
                ContainerSlotResolver.clickContainerSlot(containerHandler, n2);
                clickedSlots.add(n2);
                selectionCooldown.reset();
            }
            return;
        }
        if (selectionContext == ServerSelectionContext.LIGHT_SERVER) {
            if (selectionStep == ServerSelectionStep.CLASSIC_MODE_PENDING) {
                if (screenTitle.contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c")) {
                    ContainerSlotResolver.clickContainerSlot(containerHandler, 10);
                    selectionStep = ServerSelectionStep.LIGHT_SERVER_CONFIRMED;
                    selectionCooldown.reset();
                }
                return;
            }
            if (selectionStep == ServerSelectionStep.LIGHT_SERVER_CONFIRMED && screenTitle.contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0441\u0435\u0440\u0432\u0435\u0440 \u041b\u0430\u0439\u0442")) {
                if (lightServerSlot < 0) {
                    ServerDetector.resetConnectionAttempt();
                    return;
                }
                if (lightServerSlot < containerHandler.slots.size() && containerHandler.getSlot(lightServerSlot).hasStack()) {
                    ContainerSlotResolver.clickContainerSlot(containerHandler, lightServerSlot);
                    ServerDetector.resetConnectionAttempt();
                }
            }
            return;
        }
        if (selectionContext == ServerSelectionContext.CLASSIC_SERVER) {
            if (selectionStep == ServerSelectionStep.CLASSIC_MODE_MENU) {
                if (screenTitle.contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c")) {
                    ContainerSlotResolver.clickContainerSlot(containerHandler, 15);
                    selectionStep = ServerSelectionStep.CLASSIC_SERVER_SELECTED;
                    selectionCooldown.reset();
                }
                return;
            }
            if (selectionStep == ServerSelectionStep.CLASSIC_SERVER_SELECTED && screenTitle.contains("\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u041a\u043b\u0430\u0441\u0441\u0438\u043a")) {
                if (classicServerSlot < 0) {
                    ServerDetector.resetConnectionAttempt();
                    return;
                }
                if (classicServerSlot < containerHandler.slots.size() && containerHandler.getSlot(classicServerSlot).hasStack()) {
                    ContainerSlotResolver.clickContainerSlot(containerHandler, classicServerSlot);
                    ServerDetector.resetConnectionAttempt();
                }
            }
        }
    }

    private static void resetSelectionWorkflow() {
        selectedServerProfile = null;
        selectionWorkflowActive = false;
        selectionStep = ServerSelectionStep.RESET;
        hubCommandSent = false;
        selectionContext = ServerSelectionContext.UNRESOLVED;
        menuCommandSent = false;
        slotSelectionStarted = false;
        categorySlot = -1;
        clickedSlots.clear();
        lightServerSlot = -1;
        classicServerSlot = -1;
        serverSlot = -1;
        selectionCooldown.reset();
    }

    private static ServerSelectionStep getInitialSelectionStep() {
        return switch (selectionContext) {
            case ServerSelectionContext.LIGHT_ANARCHY -> ServerSelectionStep.MODE_MENU;
            case ServerSelectionContext.LIGHT_SERVER -> ServerSelectionStep.CLASSIC_MODE_PENDING;
            case ServerSelectionContext.CLASSIC_SERVER -> ServerSelectionStep.CLASSIC_MODE_MENU;
            default -> ServerSelectionStep.RESET;
        };
    }

    private static void beginLightAnarchySelection(ServerProfileEntry serverProfileEntry) {
        selectedServerProfile = serverProfileEntry;
        selectionWorkflowActive = true;
        selectionStep = ServerSelectionStep.MODE_MENU;
        hubCommandSent = false;
        menuCommandSent = false;
        slotSelectionStarted = false;
        selectionContext = ServerSelectionContext.LIGHT_ANARCHY;
        categorySlot = serverProfileEntry.getCategorySlot();
        serverSlot = serverProfileEntry.getServerSlot();
        clickedSlots.clear();
        selectionCooldown.reset();
        requestedServerCommand = null;
    }

    private static boolean isLiteServerAddress(String string, String string2) {
        if (string == null || string2 == null) {
            return false;
        }
        return ServerDetector.isSupportedServerHost(string) && string2.toLowerCase().startsWith("lite");
    }

    private static ServerProfileEntry parseServerProfile(String string) {
        int n;
        if (string == null) {
            return null;
        }
        String string2 = string.replaceAll("[^0-9]", "");
        if (string2.isEmpty()) {
            return null;
        }
        try {
            n = Integer.parseInt(string2);
        }
        catch (NumberFormatException numberFormatException) {
            return null;
        }
        PartySize partySize = ServerDetector.getPartySize(n);
        if (partySize == PartySize.UNKNOWN) {
            return null;
        }
        int n2 = ContainerSlotResolver.getPartySizeIndex(partySize);
        int n3 = ServerDetector.getPartyServerSlot(partySize, n);
        if (n2 < 0 || n3 < 0) {
            return null;
        }
        return new ServerProfileEntry(n, partySize, n2, n3);
    }

    private static PartySize getPartySize(int n) {
        return ContainerSlotResolver.getPartySizeForSlot(n);
    }

    private static int getPartyServerSlot(PartySize partySize, int n) {
        return ContainerSlotResolver.getPartyMemberSlot(partySize, n);
    }

    private static int findAvailableServerSlot(GenericContainerScreenHandler class_17072) {
        for (Integer slot : ContainerSlotResolver.findPartyOptionSlots(class_17072)) {
            int slotIndex = slot;
            if (clickedSlots.contains(slotIndex)) continue;
            return slotIndex;
        }
        for (PartySize partySize : PartySize.values()) {
            int n;
            if (partySize == PartySize.UNKNOWN || clickedSlots.contains(n = ContainerSlotResolver.getPartySizeIndex(partySize)) || n >= class_17072.slots.size() || !class_17072.getSlot(n).hasStack()) continue;
            return n;
        }
        return -1;
    }

    private static boolean isSupportedServerHost(String string) {
        if (string == null || string.isEmpty()) {
            return false;
        }
        String string2 = ServerDetector.removePort(string).toLowerCase();
        return string2.contains("holy") || string2.contains("holly") || string2.contains("playhw") || string2.equals("hw") || string2.startsWith("hw.");
    }

    private static void beginLightServerSelection(int n) {
        selectedServerProfile = null;
        selectionWorkflowActive = true;
        selectionStep = ServerSelectionStep.CLASSIC_MODE_PENDING;
        selectionContext = ServerSelectionContext.LIGHT_SERVER;
        hubCommandSent = false;
        menuCommandSent = false;
        slotSelectionStarted = false;
        categorySlot = -1;
        serverSlot = -1;
        lightServerSlot = n;
        classicServerSlot = -1;
        selectionCooldown.reset();
        requestedServerCommand = null;
    }

    private static void beginClassicServerSelection(int n) {
        selectedServerProfile = null;
        selectionWorkflowActive = true;
        selectionStep = ServerSelectionStep.CLASSIC_MODE_MENU;
        selectionContext = ServerSelectionContext.CLASSIC_SERVER;
        hubCommandSent = false;
        menuCommandSent = false;
        slotSelectionStarted = false;
        categorySlot = -1;
        serverSlot = -1;
        lightServerSlot = -1;
        classicServerSlot = n;
        selectionCooldown.reset();
        requestedServerCommand = null;
    }

    private static String normalizeServerAddress(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        String normalizedAddress = string.trim().toLowerCase(Locale.ROOT);
        if (!normalizedAddress.contains(":")) {
            normalizedAddress += ":25565";
        }
        return normalizedAddress;
    }

    private static boolean isLightServerRequest(String string, String string2) {
        if (string == null || string2 == null) {
            return false;
        }
        String string3 = string2.toLowerCase();
        return ServerDetector.isSupportedServerHost(string) && (string3.startsWith("lite120") || string3.startsWith("lite 120"));
    }

    private static boolean isClassicServerRequest(String string, String string2) {
        if (string == null || string2 == null) {
            return false;
        }
        String string3 = string2.toLowerCase();
        return ServerDetector.isSupportedServerHost(string) && (string3.startsWith("classik") || string3.startsWith("classic"));
    }

    private static String normalizeServerCommand(String string) {
        if (string == null) {
            return null;
        }
        String string2 = string.trim();
        if (string2.startsWith("/")) {
            string2 = string2.substring(1);
        }
        return string2;
    }

    private static boolean isServerJoinScreenReady() {
        return requestedServerCommand != null && !requestedServerCommand.isEmpty() && (ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) || ServerDetector.isServerProfileSupported(ServerProfile.HOLYWORLD));
    }

    private static Integer parseServerNumber(String string) {
        if (string == null) {
            return null;
        }
        String string2 = string.replaceAll("[^0-9]", "");
        if (string2.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(string2);
        }
        catch (NumberFormatException numberFormatException) {
            return null;
        }
    }

    private static String removePort(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }
        int n = string.lastIndexOf(58);
        if (n >= 0) {
            return string.substring(0, n);
        }
        return string;
    }

    public static boolean isInventoryServer() {
        Text class_25612 = ((PlayerListHudAccessor)ServerDetector.minecraftClient.inGameHud.getPlayerListHud()).getHeader();
        String string = class_25612 != null ? class_25612.getString() : "\u0445\u0443\u0439";
        return ServerDetector.isServerProfileSupported(ServerProfile.HOLYWORLD) || string.toLowerCase().contains("holyworld") || string.toLowerCase().contains("\u0440\u0435\u0436\u0438\u043cpvp");
    }

    @Generated
    private ServerDetector() {
        RockstarClient.create().getEventBus().registerListeners(listenerRegistration);
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static void setEnabled(boolean bl) {
        enabled = bl;
    }

    @Generated
    public static void setRequestedServerNumber(int n) {
        requestedServerNumber = n;
    }

    static {
        defaultServerIndex = -1;
        griefServerIndex = -1;
        classicServerIndex = -1;
        anarchyPvpServerNumber = -1;
        anarchyServerNumber = -1;
        anarchyServerName = "";
        selectionStep = ServerSelectionStep.RESET;
        selectionContext = ServerSelectionContext.UNRESOLVED;
        categorySlot = -1;
        serverSlot = -1;
        clickedSlots = new ArrayList<Integer>();
        lightServerSlot = -1;
        classicServerSlot = -1;
        selectionCooldown = new Timer();
        tickListener = gameTickEvent -> {
            Text class_25612;
            if (connectionPending && ServerDetector.minecraftClient.player != null && ServerDetector.isConnectedToServer(requestedServerAddress)) {
                if (selectionWorkflowActive) {
                    ServerDetector.advanceSelectionWorkflow();
                } else {
                    if (ServerDetector.isServerJoinScreenReady()) {
                        ServerDetector.minecraftClient.player.networkHandler.sendChatCommand(requestedServerCommand);
                    }
                    ServerDetector.resetConnectionAttempt();
                }
            }
            if ((class_25612 = ((PlayerListHudAccessor)ServerDetector.minecraftClient.inGameHud.getPlayerListHud()).getHeader()) == null) {
                return;
            }
            String string = class_25612.getString();
            if (string.contains("\u0433\u0440\u0438\u0444") && ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME)) {
                griefServerDetected = true;
            } else if (string.contains("\u0434\u0443\u044d\u043b\u0438") && ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY)) {
                duelServerDetected = true;
            } else if (string.contains("1.21") && ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD)) {
                version121ServerDetected = true;
            } else if (string.contains("\u0420\u0435\u0439\u0442\u0438\u043d\u0433") && ServerDetector.isServerProfileSupported(ServerProfile.FUNSKY)) {
                ratingSidebarDetected = true;
            }
            if (ServerDetector.isServerProfileSupported(ServerProfile.FUNSKY)) {
                try {
                    anarchyPvpServerNumber = Integer.parseInt(string.split("\u0410\u043d\u0430\u0440\u0445\u0438\u044f PvP #")[1].trim());
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (ServerDetector.isServerProfileSupported(ServerProfile.HOLYWORLD)) {
                String string2 = string.split("\u25b6")[1].replace("\u0410\u043d\u0430\u0440\u0445\u0438\u044f", "").trim();
                try {
                    anarchyServerNumber = Integer.parseInt(string2.split("#")[1].trim());
                    anarchyServerName = string2.split("#")[0].trim();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        };
        listenerRegistration = new ListenerRegistration();
    }

    private static final class ListenerRegistration {
        private final EventListener<GameTickEvent> tickListener = ServerDetector.tickListener;
    }

    static final class ServerNameVariant {
        final String originalName;
        final String displayName;
        final String shortName;

        ServerNameVariant(String string, String string2, String string3) {
            this.originalName = string;
            this.displayName = string2;
            this.shortName = string3;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "originalName", "displayName", "shortName");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "originalName", "displayName", "shortName");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "originalName", "displayName", "shortName");
        }

        public String getOriginalName() {
            return this.originalName;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String getShortName() {
            return this.shortName;
        }
    }
}
