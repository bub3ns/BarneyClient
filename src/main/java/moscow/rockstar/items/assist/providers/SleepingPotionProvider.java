package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.assist.AssistFeatureAvailability;

public final class SleepingPotionProvider extends PotionProvider {
    public SleepingPotionProvider() {
        super("modules.settings.assist.sleeping_potion", "Снотворное", AssistFeatureAvailability::potionLike);
    }
}
