/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.server;

import java.security.SecureRandom;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import moscow.rockstar.network.bot.BotController;
import moscow.rockstar.network.bot.BotSessionManager;

public final class AuthenticationManager {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final ScheduledExecutorService refreshExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory(){

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "Barney-BotStarter");
            thread.setDaemon(true);
            return thread;
        }
    });
    private static final AtomicBoolean refreshInProgress = new AtomicBoolean(false);
    private static final AtomicBoolean refreshInvalidated = new AtomicBoolean(false);
    private static final AtomicLong refreshGeneration = new AtomicLong();

    private AuthenticationManager() {
    }

    public static BotController resolveAuthenticationManagerShaderProgramProviderFromStringAndString(String string, String string2) {
        BotSessionManager.ServerEndpoint serverEndpoint = BotSessionManager.parseServerEndpoint(string2);
        return serverEndpoint == null ? null : AuthenticationManager.resolveAuthenticationManagerShaderProgramProviderFromStringAndStringAndInt(string, serverEndpoint.getAddress(), serverEndpoint.getPort());
    }

    public static BotController resolveAuthenticationManagerShaderProgramProviderFromStringAndStringAndInt(String string, String string2, int n) {
        return BotSessionManager.getInstance().connectBot(string, string2, n);
    }

    public static void dispatchFromIntAndString(int n, String string) {
        BotSessionManager.ServerEndpoint serverEndpoint = BotSessionManager.parseServerEndpoint(string);
        if (serverEndpoint != null) {
            AuthenticationManager.dispatchFromIntAndStringAndInt(n, serverEndpoint.getAddress(), serverEndpoint.getPort());
        }
    }

    public static void dispatchFromIntAndStringAndInt(int n, String string, int n2) {
        if (n <= 0) {
            return;
        }
        BotSessionManager botSessionManager = BotSessionManager.getInstance();
        refreshInvalidated.set(false);
        long l = refreshGeneration.incrementAndGet();
        int n3 = Math.max(1, botSessionManager.getBotControlState().getBotCreationIntervalSeconds());
        String string2 = botSessionManager.getBotControlState().getServerName();
        ConcurrentHashMap.KeySetView keySetView = ConcurrentHashMap.newKeySet();
        for (BotController botController : botSessionManager.getBots()) {
            keySetView.add(botController.getBotName());
        }
        int n4 = 0;
        while (n4 < n) {
            int n5 = n4++;
            refreshExecutor.schedule(() -> {
                if (!AuthenticationManager.isRefreshGenerationCurrent(l)) {
                    return;
                }
                String string3 = AuthenticationManager.resolveAuthenticationManagerStringFromStringAndSetAndInt(string2, keySetView, n5);
                keySetView.add(string3);
                botSessionManager.connectBot(string3, string, n2);
            }, (long)n5 * (long)n3, TimeUnit.SECONDS);
        }
    }

    public static void refreshAuthenticationState() {
        refreshInvalidated.set(true);
        refreshInProgress.set(false);
        refreshGeneration.incrementAndGet();
    }

    public static boolean isRefreshPending() {
        return refreshInProgress.compareAndSet(false, true);
    }

    public static boolean isRefreshReady() {
        return refreshInProgress.compareAndSet(true, false);
    }

    public static boolean isRefreshInvalidated() {
        return refreshInProgress.get();
    }

    private static boolean isRefreshGenerationCurrent(long l) {
        while (refreshInProgress.get()) {
            if (refreshInvalidated.get() || refreshGeneration.get() != l) {
                return false;
            }
            try {
                Thread.sleep(150L);
            }
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return !refreshInvalidated.get() && refreshGeneration.get() == l;
    }

    private static String resolveAuthenticationManagerStringFromStringAndSetAndInt(String string, Set<String> set, int n) {
        String string2;
        String string3;
        String string4 = string3 = string == null ? "Bot" : string.replaceAll("[^A-Za-z0-9_]", "");
        if (string3.isBlank()) {
            string3 = "Bot";
        }
        String string5 = String.valueOf(n % 1000);
        int n2 = 16 - string3.length();
        if (n2 <= string5.length()) {
            string3 = string3.substring(0, Math.max(1, 16 - string5.length() - 1));
            n2 = 16 - string3.length();
        }
        int n3 = Math.max(1, n2 - string5.length());
        int n4 = 0;
        while (set.contains(string2 = string3 + AuthenticationManager.resolveAuthenticationManagerStringFromInt(n3) + string5) && ++n4 < 25) {
        }
        return string2;
    }

    private static String resolveAuthenticationManagerStringFromInt(int n) {
        String string = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder stringBuilder = new StringBuilder(n);
        for (int i = 0; i < n; ++i) {
            stringBuilder.append(string.charAt(secureRandom.nextInt(string.length())));
        }
        return stringBuilder.toString();
    }
}

