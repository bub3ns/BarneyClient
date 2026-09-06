package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistFeatureAvailability;
import net.minecraft.item.Items;

public final class StunProvider extends ServerGatedItemProvider {
    public StunProvider() {
        super("modules.settings.assist.stan", Items.NETHER_STAR.getDefaultStack(), ItemCategory.CONSUMABLES, AssistFeatureAvailability::holyworldOrUnknown);
    }
}
