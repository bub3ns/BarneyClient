/*
 * Stable locations used by the client for user-owned data.
 */
package moscow.rockstar.core;

import java.io.File;
import net.minecraft.client.MinecraftClient;

/** Resolves client data relative to Minecraft's configured run directory. */
public final class ClientPaths {
    private ClientPaths() {
    }

    public static File gameDirectory() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client == null ? new File(".") : client.runDirectory;
    }

    public static File resolve(String first, String... children) {
        File result = new File(gameDirectory(), first);
        if (children != null) {
            for (String child : children) {
                result = new File(result, child);
            }
        }
        return result;
    }
}
