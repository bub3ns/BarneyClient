package moscow.rockstar.items.assist.providers;

import java.util.List;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.inventory.ItemSwapManager;
import moscow.rockstar.items.ItemCategory;
import moscow.rockstar.items.assist.AssistItemProviderBase;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.NotificationType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class BackpackProvider extends AssistItemProviderBase {
    private static final List<Item> REQUIRED_ITEM_TYPES = List.of(
        Items.MAGENTA_SHULKER_BOX,
        Items.PURPLE_SHULKER_BOX,
        Items.RED_SHULKER_BOX,
        Items.PINK_SHULKER_BOX,
        Items.BLUE_SHULKER_BOX
    );

    public BackpackProvider() {
        super("modules.settings.assist.backpack", Items.SHULKER_BOX.getDefaultStack(), ItemCategory.OTHER);
    }

    public void swapRequiredItems() {
        ItemRuleCollection<ItemRule> rules = ItemRuleSets.getHotbarRules()
            .combineRules(ItemRuleSets.getInventoryRules())
            .combineRules(ItemRuleSets.getOffhandRules());
        boolean found = REQUIRED_ITEM_TYPES.stream().anyMatch(item -> rules.findByStack(
            stack -> stack != null && !stack.isEmpty() && stack.getItem() == item
        ) != null);
        if (!found) {
            RockstarClient.create().getUiComponentProcessor().enqueueToast(
                NotificationType.ERROR,
                Localization.translate("swap.item_not_found"),
                Localization.translateFormatted("swap.item_required", this.getDisplayName().toLowerCase())
            );
            return;
        }
        for (Item item : REQUIRED_ITEM_TYPES) {
            ItemSwapManager.getInstance().swap(item);
        }
    }

}
