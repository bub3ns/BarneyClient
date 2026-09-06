package moscow.rockstar.modules.combat.attacks.potions;

import moscow.rockstar.settings.MultiBooleanSetting;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;

final class PotionCandidate extends MultiBooleanSetting.Option {
    final RegistryEntry<StatusEffect> potionType;

    PotionCandidate(MultiBooleanSetting setting, String translationKey, RegistryEntry<StatusEffect> potionType) {
        super(setting, translationKey);
        this.potionType = potionType;
    }
}
