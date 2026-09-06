package moscow.rockstar.util;

import java.util.List;
import moscow.rockstar.items.assist.AssistItemProvider;
import moscow.rockstar.items.assist.AssistItemProviderRegistry;

/** Compatibility facade for the original Assist provider registry. */
public final class SupportProviderRegistry {
    private SupportProviderRegistry() {
    }

    public static List<AssistItemProvider> createSupportProviders() {
        return AssistItemProviderRegistry.createProviders();
    }
}
