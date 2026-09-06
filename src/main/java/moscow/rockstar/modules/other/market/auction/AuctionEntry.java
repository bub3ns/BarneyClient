package moscow.rockstar.modules.other.market.auction;

public record AuctionEntry(int slotId, long totalPrice, int itemCount, int maxDurability,
                           int currentDurability, double effectivePrice) {
    public int getSlotId() {
        return slotId;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public int getItemCount() {
        return itemCount;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public int getCurrentDurability() {
        return currentDurability;
    }

    public double getEffectivePrice() {
        return effectivePrice;
    }
}
