package duke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Portfolio {
    private final String name;
    private final Map<String, Holding> holdings;

    public Portfolio(String name) {
        this.name = name;
        this.holdings = new LinkedHashMap<>();
    }

    public String getName() {
        return name;
    }

    public boolean hasHolding(AssetType assetType, String ticker) {
        return holdings.containsKey(makeKey(assetType, ticker));
    }

    public double addHolding(AssetType assetType, String ticker, double quantity) {
        String key = makeKey(assetType, ticker);

        if (holdings.containsKey(key)) {
            Holding existing = holdings.get(key);
            existing.addQuantity(quantity);
            return existing.getQuantity();
        }

        Holding holding = new Holding(assetType, ticker, quantity);
        holdings.put(key, holding);
        return holding.getQuantity();
    }

    public Holding getHolding(AssetType assetType, String ticker) {
        return holdings.get(makeKey(assetType, ticker));
    }

    public void removeHolding(AssetType assetType, String ticker) throws IllegalArgumentException {
        String key = makeKey(assetType, ticker);
        if (!holdings.containsKey(key)) {
            throw new IllegalArgumentException("Holding not found: " + ticker + " (" + assetType.toDisplay() + ")");
        }
        holdings.remove(key);
    }

    public int setPriceForTicker(String ticker, double price) {
        int updatedCount = 0;

        for (Holding holding : holdings.values()) {
            if (holding.getTicker().equals(ticker)) {
                holding.setLastPrice(price);
                updatedCount++;
            }
        }

        return updatedCount;
    }

    public List<Holding> getHoldings() {
        return new ArrayList<>(holdings.values());
    }

    public double getPricedTotalValue() {
        double total = 0.0;
        for (Holding holding : holdings.values()) {
            if (holding.hasPrice()) {
                total += holding.getValue();
            }
        }
        return total;
    }

    public int countUnpricedHoldings() {
        int count = 0;
        for (Holding holding : holdings.values()) {
            if (!holding.hasPrice()) {
                count++;
            }
        }
        return count;
    }

    private String makeKey(AssetType assetType, String ticker) {
        return assetType.name() + "|" + ticker;
    }
}
