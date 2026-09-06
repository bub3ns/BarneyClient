package moscow.rockstar.modules.other.market.purchase;

import net.minecraft.item.ItemStack;

public record PurchaseEntry(String itemName, int purchaseAmount, long totalPrice, ItemStack itemStack) {
    public String getItemName() {
        return itemName;
    }

    public int getPurchaseAmount() {
        return purchaseAmount;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
