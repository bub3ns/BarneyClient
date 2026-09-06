package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistFeatureAvailability;
import net.minecraft.item.Items;

public final class PilbProvider extends ServerGatedItemProvider {
    public PilbProvider() {
        super("modules.settings.assist.pilb", Items.SUGAR.getDefaultStack(), ItemCategory.CONSUMABLES, AssistFeatureAvailability::potionLike);
    }
}
