package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.assist.AssistFeatureAvailability;

public final class WrathPotionProvider extends PotionProvider {
    public WrathPotionProvider() {
        super("modules.settings.assist.wrath_potion", "Зелье гнева", AssistFeatureAvailability::potionLike);
    }
}
