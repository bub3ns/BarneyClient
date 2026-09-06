package moscow.rockstar.modules.player.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public record HandSelection(Hand hand, int hotbarSlot, ItemStack itemStack) {
    public boolean isMainHandSelection() {
        return this.hand == Hand.MAIN_HAND && this.hotbarSlot >= 0;
    }

    public Hand getHand() {
        return this.hand;
    }

    public int getHotbarSlot() {
        return this.hotbarSlot;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }
}
