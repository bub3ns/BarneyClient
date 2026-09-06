package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistItemProviderBase;
import net.minecraft.item.Items;

public final class SnowballProvider extends AssistItemProviderBase {
    public SnowballProvider() {
        super("modules.settings.assist.snow", Items.SNOWBALL.getDefaultStack(), ItemCategory.CONSUMABLES);
    }
}
