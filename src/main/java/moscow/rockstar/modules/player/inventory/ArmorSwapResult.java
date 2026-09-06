package moscow.rockstar.modules.player.inventory;

import moscow.rockstar.items.rules.ItemRule;

final class ArmorSwapResult {
    int swapStep;
    final ItemRule selectedItemRule;
    final ItemRule equippedArmorRule;

    ArmorSwapResult(ItemRule selectedItemRule, ItemRule equippedArmorRule) {
        this.selectedItemRule = selectedItemRule;
        this.equippedArmorRule = equippedArmorRule;
    }
}
