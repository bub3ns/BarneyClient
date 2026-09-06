package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistFeatureAvailability;
import moscow.rockstar.items.assist.KeywordItemProvider;
import net.minecraft.item.Items;

public final class AntiFlightProvider extends KeywordItemProvider {
    public AntiFlightProvider() {
        super("modules.settings.assist.anti_flight", Items.FIREWORK_STAR, ItemCategory.CONSUMABLES, "анти", "полет");
    }

    @Override
    public boolean isAvailable() {
        return AssistFeatureAvailability.commonConsumable();
    }
}
