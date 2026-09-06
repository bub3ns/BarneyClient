package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.assist.AssistFeatureAvailability;

public final class AssassinPotionProvider extends PotionProvider {
    public AssassinPotionProvider() {
        super("modules.settings.assist.assassin_potion", "Зелье ассасина", AssistFeatureAvailability::potionLike);
    }
}
