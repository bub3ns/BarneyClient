/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package moscow.rockstar.network.bot;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.events.EventListener;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.network.bot.BotBehaviorStrategy;
import moscow.rockstar.network.bot.BotControlState;
import moscow.rockstar.network.bot.BotController;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.NotificationToast;
import moscow.rockstar.ui.notifications.NotificationType;
import pyrock.events.game.WorldChangeEvent;

public class BotSessionManager {
    private static BotSessionManager INSTANCE;
    private final Map<String, BotController> botsByAddress = new ConcurrentHashMap<String, BotController>();
    private final BotControlState botControlState = new BotControlState();
    private final Map<String, BotBinding> pendingPayments = new ConcurrentHashMap<String, BotBinding>();
    private final Set<String> pollingBots = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> lastPollTimes = new ConcurrentHashMap<String, Long>();
    private final Pattern balancePattern = Pattern.compile("(?:(?:\u0431\u0430\u043b\u0430\u043d\u0441)|balance)\\s*[:=]?\\s*\\$?\\s*([\\d\\s.,]+)", 66);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, new ThreadFactory(){
        private int threadNumber;

        @Override
        public synchronized Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "Barney-BotManager-" + ++this.threadNumber);
            thread.setDaemon(true);
            return thread;
        }
    });
    private long lastMovementToggleAt;
    private boolean movementDirectionForward = true;
    private volatile BotController simulationTarget;
    private final EventListener<WorldChangeEvent> worldChangeListener = worldChangeEvent -> this.handleWorldChange();

    private BotSessionManager() {
        RockstarClient.create().getEventBus().registerListeners(this);
    }

    public static BotSessionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BotSessionManager();
        }
        return INSTANCE;
    }

    public BotController connectBotToCurrentServer(String string) {
        ServerEndpoint serverEndpoint = this.getCurrentServerEndpoint();
        if (serverEndpoint == null) {
            return null;
        }
        return this.connectBot(string, serverEndpoint.getAddress(), serverEndpoint.getPort());
    }

    public BotController connectBot(String string, String string2, int n) {
        if (string == null || string.isBlank() || string2 == null || string2.isBlank()) {
            return null;
        }
        String string4 = this.normalizeBotAddress(string);
        if (this.botsByAddress.containsKey(string4)) {
            return null;
        }
        BotControlState botControlState = new BotControlState();
        botControlState.copyFrom(this.botControlState);
        BotController botController = new BotController(string, botControlState);
        String string5 = string2.trim();
        int n2 = n;
        botController.getConnection().setConnectedCallback(void_ -> {
            if (this.botControlState.isPositionStateReady()) {
                RockstarClient.create().getUiComponentProcessor().enqueueNotification(new NotificationToast(NotificationType.SUCCESS, Localization.translate("bot.notification.title"), Localization.translateFormatted("bot.notification.connected", string)));
            }
        });
        botController.getConnection().setErrorCallback(string3 -> {
            RockstarClient.LOGGER.error("Bot {} error: {}", (Object)string, string3);
            this.botsByAddress.remove(string4);
            if (this.botControlState.isPositionStateReady()) {
                RockstarClient.create().getUiComponentProcessor().enqueueNotification(new NotificationToast(NotificationType.ERROR, Localization.translate("bot.notification.title"), Localization.translateFormatted("bot.notification.error", string, string3)));
            }
        });
        botController.getConnection().setDisconnectedCallback(string3 -> {
            this.botsByAddress.remove(string4);
            if (botController.isPrimaryActionActive() && !botController.isSecondaryActionActive()) {
                this.scheduleBotReconnect(botController);
            }
            if (this.botControlState.isPositionStateReady()) {
                RockstarClient.create().getUiComponentProcessor().enqueueNotification(new NotificationToast(NotificationType.ERROR, Localization.translate("bot.notification.title"), Localization.translateFormatted("bot.notification.disconnected", string)));
            }
        });
        this.botsByAddress.put(string4, botController);
        botController.connect(string5, n2);
        return botController;
    }

    public void disconnectBot(String string) {
        String string2 = this.normalizeBotAddress(string);
        this.pollingBots.remove(string2);
        this.lastPollTimes.remove(string2);
        BotController botController = this.botsByAddress.remove(string2);
        if (botController != null) {
            botController.disconnect();
        }
    }

    public void disconnectAllBots() {
        for (BotController botController : this.botsByAddress.values()) {
            botController.disconnect();
        }
        this.botsByAddress.clear();
    }

    public Optional<BotController> findBot(String string) {
        return Optional.ofNullable(this.botsByAddress.get(this.normalizeBotAddress(string)));
    }

    public Collection<BotController> getBots() {
        return Collections.unmodifiableCollection(this.botsByAddress.values());
    }

    public int getBotCount() {
        return this.botsByAddress.size();
    }

    public void tick() {
        if (this.simulationTarget != null && !this.simulationTarget.isConnected()) {
            this.stopBotSimulation();
        }
        for (BotController botController : this.botsByAddress.values()) {
            botController.tick();
        }
        this.pollMarkedBots();
        this.updateBotIdleState();
        this.toggleBotVerticalMovement();
    }

    public void handleWorldChange() {
        this.stopBotSimulation();
        this.disconnectAllBots();
    }

    public void forEachBot(Consumer<BotController> consumer) {
        this.botsByAddress.values().forEach(consumer);
    }

    public void forEachConnectedBot(Consumer<BotController> consumer) {
        this.botsByAddress.values().stream().filter(BotController::isConnected).forEach(consumer);
    }

    public void setMovementOverrideEnabled(boolean bl) {
        this.botControlState.setEnvironmentReady(bl);
    }

    public boolean isBotControlReady() {
        return this.botControlState.isEnvironmentReady();
    }

    public void setSimulationEnabled(boolean bl) {
        this.botControlState.setScreenReady(bl);
        this.lastMovementToggleAt = System.currentTimeMillis();
        this.movementDirectionForward = true;
    }

    public boolean isBotControlVisible() {
        return this.botControlState.isScreenReady();
    }

    public void setBotCommand(String string) {
        if (string != null && !string.isBlank()) {
            this.botControlState.setIdleCommand(string.trim());
        }
    }

    public void setIdleDurationMinutes(int n) {
        if (n > 0) {
            this.botControlState.setActionCooldownMillis(TimeUnit.MINUTES.toMillis(n));
        }
    }

    public void setPaymentRecipient(String string) {
        if (string != null && !string.isBlank()) {
            this.botControlState.setBackupServerName(string);
        }
    }

    public void startBotNavigation(String string) {
        this.findBot(string).ifPresent(botController -> botController.setBehaviorStrategy(new IdleBotBehaviorStrategy()));
    }

    public boolean startBotSimulation(String string) {
        Optional<BotController> optional = this.findBot(string);
        if (optional.isEmpty() || !optional.get().isConnected()) {
            return false;
        }
        this.stopBotSimulation();
        this.simulationTarget = optional.get();
        this.simulationTarget.setBehaviorStrategy(new IdleBotBehaviorStrategy());
        if (!this.simulationTarget.getInventoryController().startSimulation(MinecraftClient.getInstance())) {
            this.simulationTarget = null;
            return false;
        }
        return true;
    }

    public boolean stopBotSimulation() {
        if (this.simulationTarget == null) {
            return false;
        }
        this.simulationTarget.getInventoryController().stopSimulation(MinecraftClient.getInstance());
        this.simulationTarget = null;
        return true;
    }

    public Optional<BotController> getSimulationTarget() {
        return Optional.ofNullable(this.simulationTarget);
    }

    public boolean markBotForPolling(String string) {
        Optional<BotController> optional = this.findBot(string);
        if (optional.isEmpty() || !optional.get().isConnected()) {
            return false;
        }
        return this.pollingBots.add(this.normalizeBotAddress(string));
    }

    public boolean unmarkBotForPolling(String string) {
        String string2 = this.normalizeBotAddress(string);
        this.lastPollTimes.remove(string2);
        return this.pollingBots.remove(string2);
    }

    public boolean isBotMarkedForPolling(String string) {
        return this.pollingBots.contains(this.normalizeBotAddress(string));
    }

    public void registerPaymentTarget(String string, String string2) {
        if (string == null || string2 == null || string2.isBlank()) {
            return;
        }
        this.pendingPayments.put(this.normalizeBotAddress(string), new BotBinding(string, string2));
    }

    public boolean handleBalanceMessage(BotController botController, String string) {
        if (botController == null || string == null) {
            return false;
        }
        BotBinding botBinding = this.pendingPayments.get(this.normalizeBotAddress(botController.getBotName()));
        if (botBinding == null) {
            return false;
        }
        Long l = this.parseBalance(string);
        if (l == null) {
            return false;
        }
        this.pendingPayments.remove(this.normalizeBotAddress(botController.getBotName()));
        if (l <= 0L || botBinding.getTargetPlayer().equalsIgnoreCase(botController.getBotName())) {
            return true;
        }
        this.scheduler.schedule(() -> botController.sendChatOrCommand("/pay " + botBinding.getTargetPlayer() + " " + l), 5L, TimeUnit.SECONDS);
        return true;
    }

    public static ServerEndpoint parseServerEndpoint(String string) {
        if (string == null || string.isBlank()) {
            return null;
        }
        String string2 = string.trim();
        int n = 25565;
        int n2 = string2.lastIndexOf(58);
        if (n2 > 0 && n2 < string2.length() - 1) {
            try {
                n = Integer.parseInt(string2.substring(n2 + 1));
                string2 = string2.substring(0, n2);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        if (string2.isBlank() || n < 1 || n > 65535) {
            return null;
        }
        return new ServerEndpoint(string2, n);
    }

    private void updateBotIdleState() {
        if (!this.botControlState.isEnvironmentReady()) {
            return;
        }
        long l = System.currentTimeMillis();
        this.forEachConnectedBot(botController -> {
            if (!botController.isActionCooldownElapsed(l)) {
                return;
            }
            botController.sendChatOrCommand(this.botControlState.getIdleCommand());
            botController.setLastActionTime(l);
        });
    }

    private void toggleBotVerticalMovement() {
        if (!this.botControlState.isScreenReady()) {
            return;
        }
        long l = System.currentTimeMillis();
        if (l - this.lastMovementToggleAt < this.botControlState.getMovementToggleIntervalMillis()) {
            return;
        }
        this.lastMovementToggleAt = l;
        double d = this.botControlState.getIdleVerticalOffset() * (this.movementDirectionForward ? 1.0 : -1.0);
        this.forEachConnectedBot(botController -> botController.applyVerticalOffset(d));
        this.movementDirectionForward = !this.movementDirectionForward;
    }

    private void pollMarkedBots() {
        if (this.pollingBots.isEmpty()) {
            return;
        }
        long l = System.currentTimeMillis();
        this.pollingBots.removeIf(string -> {
            BotController botController = this.botsByAddress.get(string);
            if (botController == null || !botController.isConnected()) {
                this.lastPollTimes.remove(string);
                return true;
            }
            long l2 = this.lastPollTimes.getOrDefault(string, 0L);
            if (l - l2 >= botController.getControlState().getBotPollIntervalMillis()) {
                botController.attackTargetAtCrosshair();
                this.lastPollTimes.put((String)string, l);
            }
            return false;
        });
    }

    private void scheduleBotReconnect(BotController botController) {
        if (botController.getServerAddress() == null) {
            return;
        }
        long l = Math.max(50L, (long)botController.getActionSequence() * 50L);
        this.scheduler.schedule(() -> {
            if (this.botsByAddress.containsKey(this.normalizeBotAddress(botController.getBotName()))) {
                return;
            }
            BotController botController2 = this.connectBot(botController.getBotName(), botController.getServerAddress(), botController.getServerPort());
            if (botController2 != null) {
                botController2.setActive(true);
                botController2.setActionSequence(botController.getActionSequence());
            }
        }, l, TimeUnit.MILLISECONDS);
    }

    private ServerEndpoint getCurrentServerEndpoint() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null || client.getCurrentServerEntry() == null) {
            return null;
        }
        return BotSessionManager.parseServerEndpoint(client.getCurrentServerEntry().address);
    }

    private Long parseBalance(String string) {
        Matcher matcher = this.balancePattern.matcher(string);
        if (!matcher.find()) {
            return null;
        }
        String string2 = matcher.group(1);
        if (string2 == null) {
            return null;
        }
        if ((string2 = string2.replaceAll("[^0-9]", "")).isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(string2);
        }
        catch (NumberFormatException numberFormatException) {
            return null;
        }
    }

    private String normalizeBotAddress(String string) {
        return string == null ? "" : string.trim().toLowerCase(Locale.ROOT);
    }

    @Generated
    public Map<String, BotController> getBotsByAddress() {
        return this.botsByAddress;
    }

    @Generated
    public BotControlState getBotControlState() {
        return this.botControlState;
    }

    public static final class ServerEndpoint {
        private final String address;
        private final int port;

        public ServerEndpoint(String string, int n) {
            this.address = string;
            this.port = n;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "address", "port");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "address", "port");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "address", "port");
        }

        public String getAddress() {
            return this.address;
        }

        public int getPort() {
            return this.port;
        }
    }

    static final class BotBinding {
        private final String botName;
        private final String targetPlayer;

        BotBinding(String string, String string2) {
            this.botName = string;
            this.targetPlayer = string2;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "botName", "targetPlayer");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "botName", "targetPlayer");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "botName", "targetPlayer");
        }

        public String getBotName() {
            return this.botName;
        }

        public String getTargetPlayer() {
            return this.targetPlayer;
        }
    }
}
