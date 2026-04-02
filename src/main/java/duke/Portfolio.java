package duke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Portfolio {
    private final String name;
    private final Map<String, Holding> holdings;
    private double totalRealizedPnl;

    public Portfolio(String name) {
        this.name = name;
        this.holdings = new LinkedHashMap<>();
        this.totalRealizedPnl = 0.0;
    }

    public String getName() {
        return name;
    }

    public boolean hasHolding(AssetType assetType, String ticker) {
        return holdings.containsKey(makeKey(assetType, ticker));
    }

    public double addHolding(AssetType assetType, String ticker, double quantity, double purchasePrice, double fees) {
        String key = makeKey(assetType, ticker);

        if (holdings.containsKey(key)) {
            Holding existing = holdings.get(key);
            existing.addQuantity(quantity, purchasePrice, fees);
            return existing.getQuantity();
        }

        double effectivePurchasePrice = ((purchasePrice * quantity) + fees) / quantity;
        Holding holding = new Holding(assetType, ticker, quantity, effectivePurchasePrice);
        holdings.put(key, holding);
        return holding.getQuantity();
    }

    public Holding getHolding(AssetType assetType, String ticker) {
        return holdings.get(makeKey(assetType, ticker));
    }

    public RemoveResult removeHolding(AssetType assetType, String ticker, Double quantity, Double price, double fees)
            throws IllegalArgumentException {
        String key = makeKey(assetType, ticker);
        if (!holdings.containsKey(key)) {
            throw new IllegalArgumentException("Holding not found: " + ticker + " (" + assetType.toDisplay() + ")");
        }

        Holding holding = holdings.get(key);
        double quantityToRemove = quantity == null ? holding.getQuantity() : quantity;

        if (quantityToRemove <= 0 || quantityToRemove > holding.getQuantity()) {
            throw new IllegalArgumentException("Invalid quantity for remove: " + ticker);
        }

        Double holdingPrice = holding.getLastPrice();
        double effectivePrice;
        if (price != null) {
            effectivePrice = price;
        } else if (holdingPrice != null) {
            effectivePrice = holdingPrice;
        } else {
            throw new IllegalArgumentException("Price required for remove when holding has no last set price: "
                    + ticker);
        }

        double realizedDelta = holding.removeQuantity(quantityToRemove, effectivePrice, fees);
        totalRealizedPnl += realizedDelta;

        if (holding.getQuantity() == 0) {
            holdings.remove(key);
        }

        return new RemoveResult(quantityToRemove, effectivePrice, fees, realizedDelta);
    }

    public int setPriceForTicker(String ticker, double price) {
        int updatedCount = 0;
        String normalizedTicker = ticker == null ? "" : ticker.trim().toUpperCase();

        for (Holding holding : holdings.values()) {
            if (holding.getTicker().trim().toUpperCase().equals(normalizedTicker)) {
                holding.setLastPrice(price);
                updatedCount++;
            }
        }

        return updatedCount;
    }

    public boolean setPriceForHolding(AssetType assetType, String ticker, double price) {
        if (assetType == null) {
            throw new IllegalArgumentException("assetType must not be null");
        }

        String normalizedTicker = ticker == null ? "" : ticker.trim().toUpperCase();
        String key = makeKey(assetType, normalizedTicker);
        Holding holding = holdings.get(key);

        if (holding == null) {
            return false;
        }

        holding.setLastPrice(price);
        return true;
    }

    public List<Holding> getHoldings() {
        return new ArrayList<>(holdings.values());
    }

    public double getTotalRealizedPnl() {
        return totalRealizedPnl;
    }

    //@@author RishabhShenoy03
    public void setTotalRealizedPnl(double totalRealizedPnl) {
        this.totalRealizedPnl = totalRealizedPnl;
    }
    
    //@@author RishabhShenoy03
    public double getTotalUnrealizedPnl() {
        double total = 0.0;
        for (Holding holding : holdings.values()) {
            if (holding.hasPrice()) {
                total += holding.getUnrealizedPnl();
            }
        }
        return total;
    }

    //@@author RishabhShenoy03
    public void restoreHolding(AssetType assetType, String ticker, double quantity,
                               Double lastPrice, double averageBuyPrice) {
        Holding holding = new Holding(assetType, ticker, quantity, averageBuyPrice);
        holding.restoreMarketData(lastPrice, averageBuyPrice);
        holdings.put(makeKey(assetType, ticker), holding);
    }

    public double getPricedTotalValue() {
        return getCurrentTotalValue();
    }

    public double getCurrentTotalValue() {
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

    public record RemoveResult(double soldQuantity, double soldPrice, double fees, double realizedPnl) {
    }
}
