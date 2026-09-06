package moscow.rockstar.modules.other.market.auction;

record AuctionPrice(long totalPrice, int itemCount, double unitPrice) {
    public long getTotalPrice() {
        return totalPrice;
    }

    public int getItemCount() {
        return itemCount;
    }

    public double getUnitPrice() {
        return unitPrice;
    }
}
