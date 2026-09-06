/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.items.rules;

import moscow.rockstar.inventory.InventoryUtils;
import moscow.rockstar.items.rules.ItemRule;
import moscow.rockstar.items.rules.ItemRulePredicate;

public class ItemRulePredicates
implements ItemRulePredicate {
    @Override
    public boolean applySelection(ItemRule itemRule) {
        InventoryUtils.dropItem(itemRule.getClickSlot(), 40);
        return true;
    }

    @Override
    public boolean applySelectionAndConfirm(ItemRule itemRule) {
        InventoryUtils.dropItem(itemRule.getClickSlot(), 40);
        return true;
    }
}

