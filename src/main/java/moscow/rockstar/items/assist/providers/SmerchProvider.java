package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistFeatureAvailability;
import net.minecraft.item.Items;

public final class SmerchProvider extends ServerGatedItemProvider {
    public SmerchProvider() {
        super("modules.settings.assist.smerch", Items.FIRE_CHARGE.getDefaultStack(), ItemCategory.CONSUMABLES, AssistFeatureAvailability::potionLike);
    }
}
