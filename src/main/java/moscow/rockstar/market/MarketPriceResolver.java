package moscow.rockstar.market;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the reference price used by percentage-based purchase rules.
 */
public final class MarketPriceResolver {
    private final Map<String, Double> referencePrices = new HashMap<>();

    public void refresh(List<?> configuredEntries) {
        // Reference prices are populated from observed market listings. A refresh
        // starts empty so stale prices cannot influence a new server session.
        this.referencePrices.clear();
    }

    public double resolve(MarketInventoryAnalyzer.MarketItem marketItem) {
        if (marketItem == null || marketItem.getStack() == null || marketItem.getStack().isEmpty()) {
            return 0.0;
        }
        String itemId = marketItem.getStack().getItem().toString();
        Double referencePrice = this.referencePrices.get(itemId);
        if (referencePrice != null) {
            return referencePrice;
        }
        return marketItem.getCount() <= 0
            ? marketItem.getPrice()
            : (double) marketItem.getPrice() / marketItem.getCount();
    }

    public double parse(String text) {
        return MarketInventoryAnalyzer.parsePriceText(text);
    }

    public void observe(MarketInventoryAnalyzer.MarketItem marketItem) {
        if (marketItem == null || marketItem.getStack() == null || marketItem.getStack().isEmpty()) {
            return;
        }
        String itemId = marketItem.getStack().getItem().toString();
        double unitPrice = marketItem.getCount() <= 0
            ? marketItem.getPrice()
            : (double) marketItem.getPrice() / marketItem.getCount();
        this.referencePrices.merge(itemId, unitPrice, (oldPrice, newPrice) -> (oldPrice + newPrice) / 2.0);
    }
}
