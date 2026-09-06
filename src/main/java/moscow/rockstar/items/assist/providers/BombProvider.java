package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistFeatureAvailability;
import moscow.rockstar.items.assist.AssistItemProviderBase;
import net.minecraft.item.Items;

public final class BombProvider extends ServerGatedItemProvider {
    public BombProvider() {
        super("modules.settings.assist.bomb", Items.FIRE_CHARGE.getDefaultStack(), ItemCategory.CONSUMABLES, AssistFeatureAvailability::holyworldOrUnknown);
    }
}
