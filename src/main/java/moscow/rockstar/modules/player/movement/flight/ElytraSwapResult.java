package moscow.rockstar.modules.player.movement.flight;

import moscow.rockstar.items.rules.ItemRule;

final class ElytraSwapResult {
    int swapStep;
    final ItemRule selectedItemRule;
    final ItemRule equippedArmorRule;

    ElytraSwapResult(ItemRule selectedItemRule, ItemRule equippedArmorRule) {
        this.selectedItemRule = selectedItemRule;
        this.equippedArmorRule = equippedArmorRule;
    }
}
