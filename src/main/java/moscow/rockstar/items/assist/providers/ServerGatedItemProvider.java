package moscow.rockstar.items.assist.providers;

import java.util.function.BooleanSupplier;
import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistItemProviderBase;
import net.minecraft.item.ItemStack;

abstract class ServerGatedItemProvider extends AssistItemProviderBase {
    private final BooleanSupplier availability;

    protected ServerGatedItemProvider(String settingKey, ItemStack itemStack, ItemCategory category, BooleanSupplier availability) {
        super(settingKey, itemStack, category);
        this.availability = availability;
    }

    @Override
    public final boolean isAvailable() {
        return this.availability.getAsBoolean();
    }
}
