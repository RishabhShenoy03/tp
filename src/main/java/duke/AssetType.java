package duke;

public enum AssetType {
    STOCK,
    ETF,
    BOND;

    public static AssetType fromString(String rawValue) throws IllegalArgumentException {
        try {
            return AssetType.valueOf(rawValue.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid type. Allowed: stock, etf, bond");
        }
    }

    public String toDisplay() {
        return name().toLowerCase();
    }
}
