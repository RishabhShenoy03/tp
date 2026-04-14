//@@author jwelo
package duke;

/**
 * Represents one asset in the watchlist with an optional target price.
 */
public record WatchlistItem(AssetType assetType, String ticker, Double targetPrice) {
    /**
     * Creates a validated watchlist item.
     */
    public WatchlistItem {
        if (assetType == null) {
            throw new IllegalArgumentException("assetType must not be null");
        }
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("ticker must not be null or blank");
        }
        if (targetPrice != null && targetPrice <= 0) {
            throw new IllegalArgumentException("targetPrice must be > 0");
        }

        ticker = ticker.trim().toUpperCase();
    }

    /**
     * Returns whether this item has a target price.
     *
     * @return true if target price is set.
     */
    public boolean hasPrice() {
        return targetPrice != null;
    }
}
