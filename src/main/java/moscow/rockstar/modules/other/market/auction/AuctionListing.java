package moscow.rockstar.modules.other.market.auction;

record AuctionListing(int lots, int units, long totalPrice, int skippedLots) {
    double getAveragePrice() {
        return units <= 0 ? 0.0 : (double) totalPrice / units;
    }

    public int getLots() {
        return lots;
    }

    public int getUnits() {
        return units;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public int getSkippedLots() {
        return skippedLots;
    }
}
