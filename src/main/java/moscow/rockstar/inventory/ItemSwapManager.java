package moscow.rockstar.inventory;

import java.util.Objects;
import java.util.function.Predicate;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRuleCollection;
import moscow.rockstar.items.rules.ItemRuleSets;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Resolves an item to an inventory rule and makes it the active hotbar item.
 *
 * Assist providers call this synchronously: an item already in the hotbar is
 * selected, while an item in the main inventory is exchanged with the
 * currently selected slot using the existing inventory click path.
 */
public final class ItemSwapManager implements ClientAccess {
    private static final ItemSwapManager INSTANCE = new ItemSwapManager();
    private boolean busy;

    public static ItemSwapManager getInstance() {
        return INSTANCE;
    }

    public boolean isBusy() {
        return this.busy;
    }

    public boolean swap(Item item) {
        return this.swap(item, stack -> stack != null && !stack.isEmpty() && stack.getItem() == item, null);
    }

    public boolean swap(Item item, Predicate<ItemStack> matcher) {
        return this.swap(item, matcher, null);
    }

    public boolean swap(Item item, Predicate<ItemStack> matcher, String displayName) {
        if (item == null || matcher == null || minecraftClient.player == null
            || minecraftClient.interactionManager == null || minecraftClient.getNetworkHandler() == null) {
            return false;
        }
        if (this.busy) {
            return false;
        }

        ItemRuleCollection<ItemRule> rules = ItemRuleSets.getHotbarRules()
            .combineRules(ItemRuleSets.getInventoryRules())
            .combineRules(ItemRuleSets.getOffhandRules());
        ItemRule rule = rules.findByStack(matcher);
        if (rule == null || rule.isEmpty()) {
            return false;
        }

        this.busy = true;
        try {
            if (rule instanceof HotbarSlot hotbarSlot) {
                InventoryUtils.setSelectedHotbarSlot(hotbarSlot);
                return true;
            }
            InventoryUtils.moveItemToHotbar(rule, InventoryUtils.getSelectedHotbarSlot().getSlotIndex());
            return true;
        } finally {
            this.busy = false;
        }
    }

    private ItemSwapManager() {
    }
}
