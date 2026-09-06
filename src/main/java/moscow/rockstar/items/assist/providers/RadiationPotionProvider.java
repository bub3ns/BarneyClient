package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.assist.AssistFeatureAvailability;

public final class RadiationPotionProvider extends PotionProvider {
    public RadiationPotionProvider() {
        super("modules.settings.assist.radiation_potion", "Зелье Радиации", AssistFeatureAvailability::potionLike);
    }
}
