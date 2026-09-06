package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.assist.AssistFeatureAvailability;

public final class FirecrackerPotionProvider extends PotionProvider {
    public FirecrackerPotionProvider() {
        super("modules.settings.assist.hlopushka", "Хлопушка", AssistFeatureAvailability::potionLike);
    }
}
