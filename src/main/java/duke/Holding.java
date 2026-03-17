package duke;

public class Holding {
    private final AssetType assetType;
    private final String ticker;
    private double quantity;
    private Double lastPrice;

    public Holding(AssetType assetType, String ticker, double quantity) {
        this.assetType = assetType;
        this.ticker = ticker.toUpperCase();
        this.quantity = quantity;
        this.lastPrice = null;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public String getTicker() {
        return ticker;
    }

    public double getQuantity() {
        return quantity;
    }

    public Double getLastPrice() {
        return lastPrice;
    }

    public void addQuantity(double quantityToAdd) {
        this.quantity += quantityToAdd;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public boolean hasPrice() {
        return lastPrice != null;
    }

    public double getValue() {
        if (lastPrice == null) {
            return 0.0;
        }
        return quantity * lastPrice;
    }
}
