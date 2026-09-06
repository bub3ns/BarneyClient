package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistFeatureAvailability;
import moscow.rockstar.items.assist.KeywordItemProvider;
import net.minecraft.item.Items;

public final class DarkPulseProvider extends KeywordItemProvider {
    public DarkPulseProvider() {
        super("modules.settings.assist.dark_pulse", Items.FIREWORK_STAR, ItemCategory.CONSUMABLES, "темн", "пульс");
    }

    @Override
    public boolean isAvailable() {
        return AssistFeatureAvailability.commonConsumable();
    }
}
