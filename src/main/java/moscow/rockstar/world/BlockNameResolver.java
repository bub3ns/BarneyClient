package moscow.rockstar.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import moscow.rockstar.api.registry.PluginResolver;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/** Resolves user-facing block names against Minecraft's block registry. */
public final class BlockNameResolver {
    private BlockNameResolver() {
    }

    public static Block resolve(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String value = name.trim().toLowerCase(Locale.ROOT);
        Identifier id = value.indexOf(':') >= 0
            ? Identifier.tryParse(value)
            : Identifier.of("minecraft", value);
        if (id == null || !Registries.BLOCK.containsId(id)) {
            return null;
        }
        return Registries.BLOCK.get(id);
    }

    public static PluginResolver resolveArgument(String name) {
        Block block = resolve(name);
        return block == null
            ? PluginResolver.resolveMessage("Блок не найден: " + name)
            : PluginResolver.resolveValue(block);
    }

    public static List<String> suggestions(String prefix) {
        String lowerPrefix = prefix == null ? "" : prefix.trim().toLowerCase(Locale.ROOT);
        ArrayList<String> result = new ArrayList<>();
        for (Identifier id : Registries.BLOCK.getIds()) {
            String value = id.toString();
            if (value.startsWith(lowerPrefix) || id.getPath().startsWith(lowerPrefix)) {
                result.add(value);
            }
        }
        result.sort(Comparator.naturalOrder());
        return result;
    }
}
