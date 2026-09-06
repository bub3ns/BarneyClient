package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistFeatureAvailability;
import net.minecraft.item.Items;

public final class BoomTrapProvider extends ServerGatedItemProvider {
    public BoomTrapProvider() {
        super("modules.settings.assist.boom_trap", Items.PRISMARINE_SHARD.getDefaultStack(), ItemCategory.CONSUMABLES, AssistFeatureAvailability::holyworldOrUnknown);
    }
}
