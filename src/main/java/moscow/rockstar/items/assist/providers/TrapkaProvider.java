package moscow.rockstar.items.assist.providers;

import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistItemProviderBase;
import moscow.rockstar.server.ServerDetector;
import moscow.rockstar.server.ServerProfile;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class TrapkaProvider extends AssistItemProviderBase {
    public TrapkaProvider() {
        super("modules.settings.assist.trapka", Items.NETHERITE_SCRAP.getDefaultStack(), ItemCategory.CONSUMABLES);
    }

    @Override
    public ItemStack getItemStack() {
        if (ServerDetector.isServerProfileSupported(ServerProfile.HOLYWORLD) && !ServerDetector.serverAddressContains("holytime")) {
            return Items.POPPED_CHORUS_FRUIT.getDefaultStack();
        }
        return Items.NETHERITE_SCRAP.getDefaultStack();
    }
}
