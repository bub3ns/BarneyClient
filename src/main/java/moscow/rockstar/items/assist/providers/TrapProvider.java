package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistFeatureAvailability;
import net.minecraft.item.Items;

public final class TrapProvider extends ServerGatedItemProvider {
    public TrapProvider() {
        super("modules.settings.assist.trap", Items.HEART_OF_THE_SEA.getDefaultStack(), ItemCategory.CONSUMABLES, AssistFeatureAvailability::commonConsumable);
    }
}
