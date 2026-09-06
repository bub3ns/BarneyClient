/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.api.commands;

import java.util.Collections;
import java.util.List;
import moscow.rockstar.api.registry.PluginResolver;

@FunctionalInterface
public interface CommandSuggestionProvider {
    public PluginResolver validate(String var1);

    default public List<String> suggestions(String string) {
        return Collections.emptyList();
    }
}

