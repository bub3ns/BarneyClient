package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistFeatureAvailability;
import net.minecraft.item.Items;

public final class PlastProvider extends ServerGatedItemProvider {
    public PlastProvider() {
        super("modules.settings.assist.plast", Items.DRIED_KELP.getDefaultStack(), ItemCategory.CONSUMABLES, AssistFeatureAvailability::potionLike);
    }
}
