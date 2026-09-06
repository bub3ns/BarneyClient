package moscow.rockstar.modules.other.market.auction;

record AuctionSlot(int currentCount, int totalCount) {
    public int getCurrentCount() {
        return currentCount;
    }

    public int getTotalCount() {
        return totalCount;
    }
}
