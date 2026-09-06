/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.render.assets;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.core.RockstarClient;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.network.http.HttpResourceClient;

public final class RemoteAssetCache {
    private static final long FAILURE_BACKOFF_MILLIS = 30000L;
    private static final Set<String> downloadsInProgress = ConcurrentHashMap.newKeySet();
    private static final Map<String, Long> retryAfterByUrl = new ConcurrentHashMap<String, Long>();

    private RemoteAssetCache() {
    }

    public static boolean isRemoteUrl(String string) {
        if (string == null) {
            return false;
        }
        String string2 = string.toLowerCase(Locale.ROOT);
        return string2.startsWith("http://") || string2.startsWith("https://");
    }

    public static Path findCachedAsset(String string) {
        Path path = RemoteAssetCache.getCachedAssetPath(string);
        return Files.isRegularFile(path, new LinkOption[0]) ? path : null;
    }

    public static void fetchAsset(String string, Consumer<Path> consumer) {
        Path path = RemoteAssetCache.findCachedAsset(string);
        if (path != null) {
            consumer.accept(path);
            return;
        }
        Long l = retryAfterByUrl.get(string);
        if (l != null && System.currentTimeMillis() < l) {
            return;
        }
        if (!downloadsInProgress.add(string)) {
            return;
        }
        Thread thread = new Thread(() -> {
            try {
                byte[] byArray = HttpResourceClient.getBytes(string, new String[0]);
                Path cachedPath = RemoteAssetCache.getCachedAssetPath(string);
                Files.createDirectories(cachedPath.getParent(), new FileAttribute[0]);
                Path partialPath = cachedPath.resolveSibling(String.valueOf(cachedPath.getFileName()) + ".part");
                Files.write(partialPath, byArray, new OpenOption[0]);
                Files.move(partialPath, cachedPath, StandardCopyOption.REPLACE_EXISTING);
                retryAfterByUrl.remove(string);
                MinecraftClient.getInstance().execute(() -> consumer.accept(cachedPath));
            }
            catch (Exception exception) {
                retryAfterByUrl.put(string, System.currentTimeMillis() + 30000L);
                RockstarClient.LOGGER.warn("\u0410\u0441\u0441\u0435\u0442\u044b: \u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0441\u043a\u0430\u0447\u0430\u0442\u044c {}: {}", (Object)string, (Object)exception.getMessage());
            }
            finally {
                downloadsInProgress.remove(string);
            }
        }, "rockstar-web-asset");
        thread.setDaemon(true);
        thread.start();
    }

    public static Path getAssetCacheDirectory() {
        return Path.of(moscow.rockstar.core.ClientPaths.gameDirectory().toURI()).resolve("cache").resolve("web");
    }

    private static Path getCachedAssetPath(String string) {
        return RemoteAssetCache.getAssetCacheDirectory().resolve(RemoteAssetCache.hashUrl(string) + RemoteAssetCache.extractFileExtension(string));
    }

    private static String hashUrl(String string) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(messageDigest.digest(string.getBytes(StandardCharsets.UTF_8)));
        }
        catch (Exception exception) {
            return Integer.toHexString(string.hashCode());
        }
    }

    private static String extractFileExtension(String string) {
        String string2 = string;
        int n = string2.indexOf(63);
        if (n >= 0) {
            string2 = string2.substring(0, n);
        }
        int n2 = string2.lastIndexOf(47);
        int n3 = string2.lastIndexOf(46);
        if (n3 <= n2 || n3 == string2.length() - 1) {
            return "";
        }
        String string3 = string2.substring(n3).toLowerCase(Locale.ROOT);
        return string3.matches("\\.[a-z0-9]{1,8}") ? string3 : "";
    }
}
