package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.assist.AssistFeatureAvailability;

public final class PaladinPotionProvider extends PotionProvider {
    public PaladinPotionProvider() {
        super("modules.settings.assist.paladin_potion", "Зелье паладина", AssistFeatureAvailability::potionLike);
    }
}
