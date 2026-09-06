package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistFeatureAvailability;
import net.minecraft.item.Items;

public final class DisorientationProvider extends ServerGatedItemProvider {
    public DisorientationProvider() {
        super("modules.settings.assist.dezorent", Items.ENDER_EYE.getDefaultStack(), ItemCategory.CONSUMABLES, AssistFeatureAvailability::potionLike);
    }

}
