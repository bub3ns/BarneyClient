/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Text
 *  net.minecraft.PlayerListEntry
 */
package moscow.rockstar.api.data;
import moscow.rockstar.ui.localization.Localization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.TreeMap;
import moscow.rockstar.api.commands.CommandBuilder;
import moscow.rockstar.api.events.DispatchContext;
import moscow.rockstar.api.registry.PluginResolver;
import moscow.rockstar.api.registry.ServiceRegistry;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;
import net.minecraft.client.network.PlayerListEntry;
import pyrock.events.player.ClientPlayerTickEvent;
import ua.mintantileak.spk.Compile;

public class RecordQueue
implements ClientAccess {
    private final Queue<ParseJob> pendingJobs = new ArrayDeque<ParseJob>();
    private ParseJob currentJob;
    private boolean running;
    private int lastPlayerCount = -1;
    private int stablePlayerCountTicks;
    private final EventListener<ClientPlayerTickEvent> clientTickListener = clientPlayerTickEvent -> {
        if (!this.running || this.currentJob == null || RecordQueue.minecraftClient.player == null || RecordQueue.minecraftClient.world == null) {
            return;
        }
        Integer n = this.getServerIndex(this.currentJob.mode);
        if (n == null || n.intValue() != this.currentJob.getServerNumber()) {
            return;
        }
        int n2 = minecraftClient.getNetworkHandler().getPlayerList().size();
        if (n2 < 30) {
            this.resetPlayerStability();
            return;
        }
        if (n2 == this.lastPlayerCount) {
            ++this.stablePlayerCountTicks;
        } else {
            this.stablePlayerCountTicks = 0;
            this.lastPlayerCount = n2;
        }
        if (this.stablePlayerCountTicks >= 20) {
            this.saveParsedPlayerNames(this.currentJob.mode, this.currentJob.getServerNumber());
            this.advanceQueue();
        }
    };

    public RecordQueue() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    public ServiceRegistry createParserCommand() {
        return CommandBuilder.command("parse")
            .aliases("parser")
            .description("commands.parse.description")
            .argument("args", argument -> argument.optional().vararg().validator(PluginResolver::resolveValue))
            .handler(this::executeParserCommand)
            .build();
    }

    @Compile
    private void executeParserCommand(DispatchContext dispatchContext) {
        List list = dispatchContext.getArguments().isEmpty() ? Collections.emptyList() : this.coerceArguments(dispatchContext.getArguments().getFirst());
        ParserMode parserMode = ParserMode.AUTO_MODE;
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        if (!list.isEmpty()) {
            ParserMode parserMode2 = ParserMode.parseParserMode((String)list.getFirst());
            int n = 0;
            if (parserMode2 != null) {
                parserMode = parserMode2;
                ++n;
            }
            for (int i = n; i < list.size(); ++i) {
                Integer n2 = this.parseServerNumber((String)list.get(i));
                if (n2 == null) {
                    Notification.error(Text.of((String)("Invalid server number: " + (String)list.get(i))));
                    return;
                }
                arrayList.add(n2);
            }
        }
        if (!arrayList.isEmpty()) {
            this.startParser(parserMode, arrayList);
        } else {
            this.saveParsedPlayerNames(this.resolveParserMode(parserMode), null);
        }
    }

    private List<String> coerceArguments(Object object) {
        return object == null ? Collections.emptyList() : (List)object;
    }

    private Integer parseServerNumber(String string) {
        try {
            int n = Integer.parseInt(string);
            return n > 0 ? Integer.valueOf(n) : null;
        }
        catch (Exception exception) {
            return null;
        }
    }

    private void startParser(ParserMode parserMode, List<Integer> list) {
        if (this.running) {
            Notification.error(Text.of((String)"Parser already running"));
            return;
        }
        this.pendingJobs.clear();
        ParserMode parserMode2 = this.resolveParserMode(parserMode);
        for (Integer n : list) {
            this.pendingJobs.add(new ParseJob(parserMode2, n));
        }
        this.running = true;
        this.currentJob = null;
        this.advanceQueue();
    }

    private void advanceQueue() {
        this.currentJob = this.pendingJobs.poll();
        this.resetPlayerStability();
        if (this.currentJob == null) {
            this.running = false;
            Notification.info(Text.of((String)Localization.translate("parser.finished")));
            return;
        }
        this.sendParseCommand(this.currentJob);
    }

    private void sendParseCommand(ParseJob parseJob) {
        String string = RockstarClient.create().getNavigationCommandService().getCommandPrefix();
        StringBuilder stringBuilder = new StringBuilder(string).append("rct");
        if (parseJob.mode != ParserMode.AUTO_MODE) {
            stringBuilder.append(" ").append(parseJob.mode.getDisplayName());
        }
        stringBuilder.append(" ").append(parseJob.serverNumber);
        RockstarClient.create().getNavigationCommandService().executeCommand(stringBuilder.toString());
    }

    private void resetPlayerStability() {
        this.lastPlayerCount = -1;
        this.stablePlayerCountTicks = 0;
    }

    private void saveParsedPlayerNames(ParserMode parserMode, Integer n) {
        String string;
        if (minecraftClient.isInSingleplayer()) {
            Notification.error(Text.of((String)Localization.translate("parser.singleplayer")));
            return;
        }
        if (minecraftClient.getNetworkHandler() == null || minecraftClient.getNetworkHandler().getServerInfo() == null) {
            Notification.error(Text.of((String)Localization.translate("parser.bad_server")));
            return;
        }
        File file = new File(moscow.rockstar.core.ClientPaths.gameDirectory(), "parser");
        if (!file.exists() && !file.mkdirs()) {
            Notification.error(Text.of((String)Localization.translate("parser.folder_create")));
            return;
        }
        String string2 = this.sanitizePathComponent(RecordQueue.minecraftClient.getNetworkHandler().getServerInfo().address);
        File file2 = new File(new File(file, string2), string = this.formatParserDirectory(parserMode, n));
        if (!file2.exists() && !file2.mkdirs()) {
            Notification.error(Text.of((String)Localization.translate("parser.folder_error")));
            return;
        }
        Map<String, List<String>> map = this.groupPlayersByDonation(minecraftClient.getNetworkHandler().getPlayerList());
        if (map.isEmpty()) {
            Notification.error(Text.of((String)Localization.translate("parser.server_lag")));
            return;
        }
        try {
            this.deleteParserOutputFiles(file2);
            this.writePlayerNameFiles(file2, map);
            Notification.info(Text.of((String)Localization.translateFormatted("parser.parsed", map.values().stream().mapToInt(List::size).sum())));
        }
        catch (Exception exception) {
            Notification.error(Text.of((String)("Save error: " + exception.getMessage())));
        }
    }

    private Map<String, List<String>> groupPlayersByDonation(Collection<PlayerListEntry> collection) {
        TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>();
        for (PlayerListEntry ServerSamplerSource : collection) {
            if (ServerSamplerSource.getScoreboardTeam() == null) continue;
            String string2 = ServerSamplerSource.getScoreboardTeam().getPrefix().getString().trim();
            if (string2.isBlank()) {
                string2 = Localization.translate("parser.no_donate");
            }
            treeMap.computeIfAbsent(string2, string -> new ArrayList()).add(ServerSamplerSource.getProfile().getName());
        }
        return treeMap;
    }

    private void deleteParserOutputFiles(File file2) throws IOException {
        File[] fileArray = file2.listFiles((file, string) -> string.endsWith(".txt"));
        if (fileArray == null) {
            return;
        }
        for (File file3 : fileArray) {
            if (file3.delete()) continue;
            throw new IOException("Failed delete " + file3.getName());
        }
    }

    private void writePlayerNameFiles(File file, Map<String, List<String>> map) throws IOException {
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            File file2 = new File(file, this.sanitizePathComponent(this.normalizeDonationName(entry.getKey())) + ".txt");
            try (FileWriter fileWriter = new FileWriter(file2);){
                for (String string : entry.getValue()) {
                    fileWriter.write(string + "\n");
                }
            }
        }
    }

    private String formatParserDirectory(ParserMode parserMode, Integer n) {
        int n2 = n != null ? n : Optional.ofNullable(this.getServerIndex(parserMode)).orElse(-1);
        return parserMode.getCommandPrefix() + String.valueOf(n2 > 0 ? Integer.valueOf(n2) : "unknown");
    }

    private String sanitizePathComponent(String string) {
        return string.replace(':', '_').replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String normalizeDonationName(String string) {
        return string.replace("[", "").replace("]", "").trim().replaceAll("\\s+", "_").toLowerCase();
    }

    private ParserMode resolveParserMode(ParserMode parserMode) {
        if (parserMode != ParserMode.AUTO_MODE) {
            return parserMode;
        }
        if (ServerDetector.isServerProfileSupported(ServerProfile.REALLYWORLD)) {
            return ParserMode.GRIEF_MODE;
        }
        if ((ServerDetector.isServerProfileSupported(ServerProfile.FUNTIME) || ServerDetector.isServerProfileSupported(ServerProfile.SPOOKY)) && ServerDetector.griefServerDetected) {
            return ParserMode.GRIEF_MODE;
        }
        return ParserMode.ANARCHY_MODE;
    }

    private Integer getServerIndex(ParserMode parserMode) {
        return switch (parserMode.ordinal()) {
            case 2 -> ServerDetector.classicServerIndex;
            case 1 -> ServerDetector.defaultServerIndex;
            default -> null;
        };
    }

    static enum ParserMode {
        AUTO_MODE("auto", "an"),
        ANARCHY_MODE("an", "an"),
        GRIEF_MODE("grief", "grif");
        private final String displayName;
        private final String commandPrefix;

        private ParserMode(String string2, String string3) {
            this.displayName = string2;
            this.commandPrefix = string3;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String getCommandPrefix() {
            return this.commandPrefix;
        }

        @Compile
        public static ParserMode parseParserMode(String string) {
            if (string == null) {
                return null;
            }
            if ((string = string.toLowerCase()).startsWith("an")) {
                return ANARCHY_MODE;
            }
            if (string.startsWith("gr")) {
                return GRIEF_MODE;
            }
            return null;
        }
}

    static final class ParseJob {
        final ParserMode mode;
        final int serverNumber;

        ParseJob(ParserMode parserMode, int n) {
            this.mode = parserMode;
            this.serverNumber = n;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "mode", "serverNumber");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "mode", "serverNumber");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "mode", "serverNumber");
        }

        public ParserMode getMode() {
            return this.mode;
        }

        public int getServerNumber() {
            return this.serverNumber;
        }
    }
}

