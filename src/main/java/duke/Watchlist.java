// @@author jwelo
package duke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores assets the user is monitoring for potential future purchase.
 */
public class Watchlist {
    private final Map<String, WatchlistItem> items;

    /**
     * Creates an empty watchlist.
     */
    public Watchlist() {
        this.items = new LinkedHashMap<>();
    }

    /**
     * Adds a watchlist item with an optional target price.
     *
     * @param assetType asset type.
     * @param ticker ticker symbol.
     * @param targetPrice optional target price; may be null.
     */
    public void addItem(AssetType assetType, String ticker, Double targetPrice) {
        String key = makeKey(assetType, ticker);
        if (items.containsKey(key)) {
            throw new IllegalArgumentException("Watchlist item already exists: "
                    + normalizeTicker(ticker) + " (" + assetType.toDisplay() + ")");
        }

        items.put(key, new WatchlistItem(assetType, ticker, targetPrice));
    }

    /**
     * Removes a watchlist item by type and ticker.
     *
     * @param assetType asset type.
     * @param ticker ticker symbol.
     * @return true if removed, false otherwise.
     */
    public boolean removeItem(AssetType assetType, String ticker) {
        String key = makeKey(assetType, ticker);
        return items.remove(key) != null;
    }

    /**
     * Checks whether a watchlist item exists.
     *
     * @param assetType asset type.
     * @param ticker ticker symbol.
     * @return true if found.
     */
    public boolean hasItem(AssetType assetType, String ticker) {
        return items.containsKey(makeKey(assetType, ticker));
    }

    /**
     * Gets a watchlist item by type and ticker.
     *
     * @param assetType asset type.
     * @param ticker ticker symbol.
     * @return matching item, or null if missing.
     */
    public WatchlistItem getItem(AssetType assetType, String ticker) {
        return items.get(makeKey(assetType, ticker));
    }

    /**
     * Returns a copy of all watchlist items.
     *
     * @return list of watchlist items.
     */
    public List<WatchlistItem> getItems() {
        return new ArrayList<>(items.values());
    }

    /**
     * Buys a specified quantity of a watchlist item into a target portfolio.
     * The item must have a price and the target portfolio must exist.
     *
     * @param assetType asset type.
     * @param ticker ticker symbol.
     * @param quantity quantity to buy.
     * @param portfolioName destination portfolio name.
     * @param portfolioBook source of portfolios.
     * @return summary of the buy operation.
     */
    public BuyResult buyItem(AssetType assetType, String ticker, double quantity,
                             String portfolioName, PortfolioBook portfolioBook) {
        if (portfolioBook == null) {
            throw new IllegalArgumentException("portfolioBook must not be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        if (portfolioName == null || portfolioName.isBlank()) {
            throw new IllegalArgumentException("portfolioName must not be null or blank");
        }

        String normalizedTicker = normalizeTicker(ticker);
        String key = makeKey(assetType, normalizedTicker);
        WatchlistItem item = items.get(key);
        if (item == null) {
            throw new IllegalArgumentException("Watchlist item not found: "
                    + normalizedTicker + " (" + assetType.toDisplay() + ")");
        }
        if (!item.hasPrice()) {
            throw new IllegalArgumentException("Watchlist item has no price: "
                    + normalizedTicker + " (" + assetType.toDisplay() + "). "
                    + "Add it again with --price before buying.");
        }

        String trimmedPortfolioName = portfolioName.trim();
        Portfolio portfolio = portfolioBook.getPortfolio(trimmedPortfolioName);
        if (portfolio == null) {
            throw new IllegalArgumentException("Portfolio not found: " + trimmedPortfolioName);
        }

        double buyPrice = item.targetPrice();
        double resultingQuantity = portfolio.addHolding(item.assetType(), item.ticker(), quantity, buyPrice, 0);
        Holding holding = portfolio.getHolding(item.assetType(), item.ticker());
        if (holding != null) {
            holding.setLastPrice(buyPrice);
        }
        items.remove(key);
        return new BuyResult(portfolio.getName(), item.assetType(), item.ticker(), quantity,
                buyPrice, resultingQuantity);
    }

    private String makeKey(AssetType assetType, String ticker) {
        if (assetType == null) {
            throw new IllegalArgumentException("assetType must not be null");
        }
        return assetType.name() + "|" + normalizeTicker(ticker);
    }

    private String normalizeTicker(String ticker) {
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("ticker must not be null or blank");
        }
        return ticker.trim().toUpperCase();
    }

    /**
     * Result summary for a successful watchlist buy operation.
     */
    public record BuyResult(
            String portfolioName,
            AssetType assetType,
            String ticker,
            double boughtQuantity,
            double buyPrice,
            double resultingQuantity
    ) {
    }
}
