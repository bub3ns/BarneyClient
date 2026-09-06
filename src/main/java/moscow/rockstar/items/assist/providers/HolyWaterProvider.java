package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.assist.AssistFeatureAvailability;

public final class HolyWaterProvider extends PotionProvider {
    public HolyWaterProvider() {
        super("modules.settings.assist.holy_water", "Святая вода", AssistFeatureAvailability::potionLike);
    }
}
