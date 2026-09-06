package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistFeatureAvailability;
import net.minecraft.item.Items;

public final class GodAuraProvider extends ServerGatedItemProvider {
    public GodAuraProvider() {
        super("modules.settings.assist.aura", Items.PHANTOM_MEMBRANE.getDefaultStack(), ItemCategory.CONSUMABLES, AssistFeatureAvailability::potionLike);
    }
}
