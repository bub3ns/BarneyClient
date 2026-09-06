package moscow.rockstar.modules.combat.defense.totem;

import moscow.rockstar.inventory.selection.ItemSelectionState;
import moscow.rockstar.modules.combat.defense.AutoTotem;
import net.minecraft.item.ItemStack;

/**
 * Selection callback retained from the client.  The original callback does
 * not add an animation side effect; it only provides the selection hook.
 */
public final class TotemAnimation implements ItemSelectionState.SelectionCallback {
    public TotemAnimation(AutoTotem autoTotem) {
    }

    @Override
    public void setCallbackValue(float value) {
    }

    @Override
    public void setCallbackStack(ItemStack stack) {
    }
}
