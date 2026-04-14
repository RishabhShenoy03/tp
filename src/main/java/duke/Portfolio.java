//@@author Elegazia
package duke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Portfolio {
    private static final double QUANTITY_EPSILON = 1e-12;
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
        assert assetType != null : "assetType must not be null";
        assert ticker != null && !ticker.isBlank() : "ticker must not be null or blank";
        assert quantity > 0 && purchasePrice > 0 : "quantity and purchasePrice must be positive";

        String key = makeKey(assetType, ticker);

        if (holdings.containsKey(key)) {
            Holding existing = holdings.get(key);
            existing.addQuantity(quantity, purchasePrice, fees);
            existing.setLastPrice(purchasePrice);
            double resultQty = existing.getQuantity();
            assert resultQty > 0 : "Updated holding quantity must be positive";
            return resultQty;
        }

        double effectivePurchasePrice = ((purchasePrice * quantity) + fees) / quantity;
        Holding holding = new Holding(assetType, ticker, quantity, effectivePurchasePrice);
        holding.setLastPrice(purchasePrice);
        holdings.put(key, holding);
        assert holdings.containsKey(key) : "Holding was not added to portfolio";
        double resultQty = holding.getQuantity();
        assert resultQty == quantity : "Added holding quantity should match requested quantity";
        return resultQty;
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
        double currentQuantity = holding.getQuantity();
        double quantityToRemove = quantity == null ? currentQuantity : quantity;

        boolean matchesFullQuantity = areEquivalentQuantities(quantityToRemove, currentQuantity);
        if (quantityToRemove <= 0 || (quantityToRemove > currentQuantity && !matchesFullQuantity)) {
            throw new IllegalArgumentException("Invalid quantity for remove: " + ticker);
        }
        double effectiveQuantityToRemove = matchesFullQuantity ? currentQuantity : quantityToRemove;

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

        double realizedDelta = holding.removeQuantity(effectiveQuantityToRemove, effectivePrice, fees);
        totalRealizedPnl += realizedDelta;

        if (holding.getQuantity() == 0) {
            holdings.remove(key);
        }

        return new RemoveResult(effectiveQuantityToRemove, effectivePrice, fees, realizedDelta);
    }

    private boolean areEquivalentQuantities(double left, double right) {
        double magnitude = Math.max(Math.abs(left), Math.abs(right));
        double tolerance = Math.max(QUANTITY_EPSILON, 8 * Math.ulp(magnitude));
        return Math.abs(left - right) <= tolerance;
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

    public int countHoldingsForTicker(String ticker) {
        String normalizedTicker = ticker == null ? "" : ticker.trim().toUpperCase();
        int matchedCount = 0;
        for (Holding holding : holdings.values()) {
            if (holding.getTicker().trim().toUpperCase().equals(normalizedTicker)) {
                matchedCount++;
            }
        }
        return matchedCount;
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
        List<Holding> result = new ArrayList<>(holdings.values());
        assert result != null : "getHoldings result cannot be null";
        for (Holding h : result) {
            assert h != null : "Holdings list should not contain null entries";
        }
        return result;
    }

    public double getTotalRealizedPnl() {
        return totalRealizedPnl;
    }

    public void setTotalRealizedPnl(double totalRealizedPnl) {
        this.totalRealizedPnl = totalRealizedPnl;
    }
    
    public double getTotalUnrealizedPnl() {
        double total = 0.0;
        for (Holding holding : holdings.values()) {
            if (holding.hasPrice()) {
                total += holding.getUnrealizedPnl();
            }
        }
        return total;
    }

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
