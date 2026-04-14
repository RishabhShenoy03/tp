//@@author jwelo
package duke;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Storage {
    private static final String CORRUPTED_FILE_MESSAGE = "Corrupted storage file.";
    private static final String CORRUPTED_WATCHLIST_FILE_MESSAGE = "Corrupted watchlist storage file.";
    private static final DateTimeFormatter BAD_FILE_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final Logger LOGGER = Logger.getLogger(Storage.class.getName());

    private final Path filePath;
    private final Path watchlistFilePath;

    public Storage(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath must not be null or blank");
        }

        this.filePath = Paths.get(filePath);
        assert this.filePath.getFileName() != null : "Storage path must reference a file";
        this.watchlistFilePath = this.filePath.resolveSibling(this.filePath.getFileName() + ".watchlist");
        assert this.watchlistFilePath.getFileName() != null : "Watchlist storage path must reference a file";
    }

    public PortfolioBook load() throws AppException {
        LOGGER.fine(() -> "Loading portfolio book from " + filePath);
        createStorageFileIfMissing();
        assert Files.exists(filePath) : "Storage file should exist after initialization";

        PortfolioBook portfolioBook = new PortfolioBook();
        String activePortfolioName = null;

        try {
            List<String> lines = Files.readAllLines(filePath);

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);
                String recordType = parts[0].trim().toUpperCase();

                if (recordType.isBlank()) {
                    throw new AppException(CORRUPTED_FILE_MESSAGE);
                }

                switch (recordType) {
                case "ACTIVE":
                    activePortfolioName = parseActivePortfolio(parts);
                    break;
                case "PORTFOLIO":
                    loadPortfolio(parts, portfolioBook);
                    break;
                case "HOLDING":
                    loadHolding(parts, portfolioBook);
                    break;
                default:
                    throw new AppException(CORRUPTED_FILE_MESSAGE);
                }

                assert i >= 0 : "Loop index must be non-negative";
            }

            if (activePortfolioName != null) {
                applyActivePortfolio(portfolioBook, activePortfolioName);
            }

            return portfolioBook;
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Corrupted storage file read from " + filePath, e);
            throw new AppException(CORRUPTED_FILE_MESSAGE);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to read storage file: " + filePath, e);
            throw new AppException("Unable to read storage file.");
        }
    }

    public boolean isCorruptedStorage(AppException exception) {
        if (exception == null) {
            throw new IllegalArgumentException("exception must not be null");
        }
        return CORRUPTED_FILE_MESSAGE.equals(exception.getMessage());
    }

    public Path quarantineCorruptedStorageFile() throws AppException {
        createStorageFileIfMissing();

        Path quarantinedPath = filePath.resolveSibling(filePath.getFileName()
                + ".bad-" + LocalDateTime.now().format(BAD_FILE_TIMESTAMP_FORMAT));

        try {
            Files.move(filePath, quarantinedPath, StandardCopyOption.REPLACE_EXISTING);
            Files.createFile(filePath);
            LOGGER.info(() -> "Quarantined corrupted storage file to " + quarantinedPath);
            return quarantinedPath;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to preserve corrupted storage file.", e);
            throw new AppException("Unable to preserve corrupted storage file.");
        }
    }

    public void save(PortfolioBook portfolioBook) throws AppException {
        if (portfolioBook == null) {
            throw new IllegalArgumentException("portfolioBook must not be null");
        }
        LOGGER.fine(() -> "Saving portfolio book to " + filePath);
        createStorageFileIfMissing();

        List<String> lines = new ArrayList<>();
        lines.add("ACTIVE|" + nullToEmpty(portfolioBook.getActivePortfolioName()));

        for (Portfolio portfolio : portfolioBook.getPortfolios()) {
            assert portfolio != null : "Portfolio list should not contain null entries";
            lines.add("PORTFOLIO|"
                    + portfolio.getName() + "|"
                    + portfolio.getTotalRealizedPnl());
            for (Holding holding : portfolio.getHoldings()) {
                assert holding != null : "Holdings list should not contain null entries";
                String priceText = holding.hasPrice() ? String.valueOf(holding.getLastPrice()) : "";
                lines.add("HOLDING|"
                        + portfolio.getName() + "|"
                        + holding.getAssetType().name() + "|"
                        + holding.getTicker() + "|"
                        + holding.getQuantity() + "|"
                        + holding.getAverageBuyPrice() + "|"
                        + priceText);
            }
        }

        try {
            Files.write(filePath, lines);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to save storage file: " + filePath, e);
            throw new AppException("Unable to save storage file.");
        }
    }

    /**
     * Loads the watchlist from its storage file.
     *
     * @return loaded watchlist.
     * @throws AppException if reading fails or content is invalid.
     */
    public Watchlist loadWatchlist() throws AppException {
        LOGGER.fine(() -> "Loading watchlist from " + watchlistFilePath);
        createWatchlistFileIfMissing();
        assert Files.exists(watchlistFilePath) : "Watchlist storage file should exist after initialization";

        Watchlist watchlist = new Watchlist();

        try {
            List<String> lines = Files.readAllLines(watchlistFilePath);

            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);
                if (parts.length != 4) {
                    throw new AppException(CORRUPTED_WATCHLIST_FILE_MESSAGE);
                }

                String recordType = parts[0].trim().toUpperCase();
                if (!"WATCH".equals(recordType)) {
                    throw new AppException(CORRUPTED_WATCHLIST_FILE_MESSAGE);
                }

                AssetType assetType = parseWatchlistAssetType(parts[1]);
                String ticker = parseWatchlistTicker(parts[2]);
                Double price = parseWatchlistOptionalPrice(parts[3]);

                try {
                    watchlist.addItem(assetType, ticker, price);
                } catch (IllegalArgumentException e) {
                    throw new AppException(CORRUPTED_WATCHLIST_FILE_MESSAGE);
                }
            }

            return watchlist;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to read watchlist storage file: " + watchlistFilePath, e);
            throw new AppException("Unable to read watchlist storage file.");
        }
    }

    /**
     * Saves the watchlist to its storage file.
     *
     * @param watchlist watchlist to persist.
     * @throws AppException if writing fails.
     */
    public void saveWatchlist(Watchlist watchlist) throws AppException {
        if (watchlist == null) {
            throw new IllegalArgumentException("watchlist must not be null");
        }
        LOGGER.fine(() -> "Saving watchlist to " + watchlistFilePath);
        createWatchlistFileIfMissing();

        List<String> lines = new ArrayList<>();
        for (WatchlistItem item : watchlist.getItems()) {
            assert item != null : "Watchlist should not contain null entries";
            String priceText = item.hasPrice() ? String.valueOf(item.targetPrice()) : "";
            lines.add("WATCH|"
                    + item.assetType().name() + "|"
                    + item.ticker() + "|"
                    + priceText);
        }

        try {
            Files.write(watchlistFilePath, lines);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to save watchlist storage file: " + watchlistFilePath, e);
            throw new AppException("Unable to save watchlist storage file.");
        }
    }

    public BulkUpdateResult loadPriceUpdates(Path csvPath, Portfolio portfolio) throws AppException {
        if (csvPath == null) {
            throw new IllegalArgumentException("csvPath must not be null");
        }
        if (portfolio == null) {
            throw new IllegalArgumentException("portfolio must not be null");
        }

        if (!Files.exists(csvPath)) {
            throw new AppException("File not found: " + csvPath);
        }
        if (!Files.isRegularFile(csvPath)) {
            throw new AppException("Not a file: " + csvPath);
        }

        int successCount = 0;
        int failedCount = 0;
        List<String> failures = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(csvPath);
            if (lines.isEmpty()) {
                throw new AppException("CSV file is empty.");
            }

            String header = lines.get(0).trim().toLowerCase();
            if (!header.equals("ticker,price")) {
                throw new AppException("CSV header must be: ticker,price");
            }

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length != 2) {
                    failedCount++;
                    failures.add("line " + (i + 1) + " - ticker: ? reason: invalid CSV row");
                    continue;
                }

                String ticker = parts[0].trim().toUpperCase();
                String priceText = parts[1].trim();
                if (ticker.isBlank()) {
                    failedCount++;
                    failures.add("line " + (i + 1) + " - ticker: ? reason: ticker is blank");
                    continue;
                }

                try {
                    double price = Double.parseDouble(priceText);
                    if (price <= 0) {
                        throw new NumberFormatException();
                    }

                    int updatedCount = portfolio.setPriceForTicker(ticker, price);
                    if (updatedCount == 0) {
                        failedCount++;
                        failures.add("line " + (i + 1) + " - ticker: " + ticker + " reason: holding not found");
                    } else {
                        successCount++;
                    }
                } catch (NumberFormatException e) {
                    failedCount++;
                    failures.add("line " + (i + 1) + " - ticker: " + ticker + " reason: price must be > 0");
                }
            }

            return new BulkUpdateResult(successCount, failedCount, failures);
        } catch (IOException e) {
            throw new AppException("Unable to read CSV file.");
        }
    }

    private void loadHolding(String[] parts, PortfolioBook portfolioBook) throws AppException {
        assert parts != null : "parts must be non-null";
        assert portfolioBook != null : "portfolioBook must be non-null";

        if (parts.length == 6) {
            loadLegacyHolding(parts, portfolioBook);
            return;
        }

        if (parts.length != 7) {
            throw new AppException(CORRUPTED_FILE_MESSAGE);
        }

        String portfolioName = requireNonBlank(parts[1]);
        AssetType assetType = parseAssetType(parts[2]);
        String ticker = requireNonBlank(parts[3]).toUpperCase();
        double quantity = parsePositiveDouble(parts[4]);
        double averageBuyPrice = parsePositiveDouble(parts[5]);
        Double lastPrice = parseOptionalPositiveDouble(parts[6]);

        Portfolio portfolio = getOrCreatePortfolio(portfolioName, portfolioBook);
        portfolio.restoreHolding(assetType, ticker, quantity, lastPrice, averageBuyPrice);
    }

    private void loadLegacyHolding(String[] parts, PortfolioBook portfolioBook) throws AppException {
        String portfolioName = requireNonBlank(parts[1]);
        AssetType assetType = parseAssetType(parts[2]);
        String ticker = requireNonBlank(parts[3]).toUpperCase();
        double quantity = parsePositiveDouble(parts[4]);
        double restoredPrice = parsePositiveDouble(parts[5]);

        Portfolio portfolio = getOrCreatePortfolio(portfolioName, portfolioBook);
        portfolio.restoreHolding(assetType, ticker, quantity, restoredPrice, restoredPrice);
    }

    private void createStorageFileIfMissing() throws AppException {
        createFileIfMissing(filePath, "Unable to create storage file.", "Storage path is a directory.");
    }

    private void createWatchlistFileIfMissing() throws AppException {
        createFileIfMissing(watchlistFilePath, "Unable to create watchlist storage file.",
                "Watchlist storage path is a directory.");
    }

    private void createFileIfMissing(Path targetPath, String createFailureMessage, String directoryMessage)
            throws AppException {
        try {
            Path parent = targetPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            if (!Files.exists(targetPath)) {
                Files.createFile(targetPath);
            }

            if (Files.isDirectory(targetPath)) {
                throw new AppException(directoryMessage);
            }
            assert Files.exists(targetPath) : "Storage file must exist after creation";
        } catch (IOException e) {
            throw new AppException(createFailureMessage);
        }
    }

    private void loadPortfolio(String[] parts, PortfolioBook portfolioBook) throws AppException {
        if (parts.length != 2 && parts.length != 3) {
            throw new AppException(CORRUPTED_FILE_MESSAGE);
        }

        String name = requireNonBlank(parts[1]);
        portfolioBook.createPortfolio(name);

        if (parts.length == 3) {
            Portfolio portfolio = portfolioBook.getPortfolio(name);
            assert portfolio != null : "Portfolio should exist immediately after creation";
            portfolio.setTotalRealizedPnl(parseAnyDouble(parts[2]));
        }
    }

    private String parseActivePortfolio(String[] parts) throws AppException {
        if (parts.length != 2) {
            throw new AppException(CORRUPTED_FILE_MESSAGE);
        }

        String candidate = parts[1].trim();
        return candidate.isBlank() ? null : candidate;
    }

    private void applyActivePortfolio(PortfolioBook portfolioBook, String activePortfolioName) throws AppException {
        try {
            portfolioBook.usePortfolio(activePortfolioName);
        } catch (AppException e) {
            throw new AppException(CORRUPTED_FILE_MESSAGE);
        }
    }

    private String requireNonBlank(String value) throws AppException {
        if (value == null || value.isBlank()) {
            throw new AppException(CORRUPTED_FILE_MESSAGE);
        }
        return value.trim();
    }

    private AssetType parseAssetType(String rawType) throws AppException {
        return onCorrupted(() -> AssetType.fromString(rawType), CORRUPTED_FILE_MESSAGE);
    }

    private double parsePositiveDouble(String rawValue) throws AppException {
        return onCorrupted(() -> {
            double value = Double.parseDouble(rawValue);
            if (value <= 0) {
                throw new IllegalArgumentException();
            }
            return value;
        }, CORRUPTED_FILE_MESSAGE);
    }

    private Double parseOptionalPositiveDouble(String rawValue) throws AppException {
        String trimmed = rawValue == null ? "" : rawValue.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return parsePositiveDouble(trimmed);
    }

    private double parseAnyDouble(String rawValue) throws AppException {
        return onCorrupted(() -> Double.parseDouble(rawValue.trim()), CORRUPTED_FILE_MESSAGE);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private Portfolio getOrCreatePortfolio(String portfolioName, PortfolioBook portfolioBook) throws AppException {
        portfolioBook.ensurePortfolioExists(portfolioName);
        Portfolio portfolio = portfolioBook.getPortfolio(portfolioName);
        assert portfolio != null : "Portfolio should exist after ensurePortfolioExists";
        return portfolio;
    }

    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    private <T> T onCorrupted(CheckedSupplier<T> supplier, String corruptedMessage) throws AppException {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new AppException(corruptedMessage);
        }
    }

    private AssetType parseWatchlistAssetType(String rawType) throws AppException {
        return onCorrupted(() -> AssetType.fromString(rawType), CORRUPTED_WATCHLIST_FILE_MESSAGE);
    }

    private String parseWatchlistTicker(String rawTicker) throws AppException {
        if (rawTicker == null || rawTicker.isBlank()) {
            throw new AppException(CORRUPTED_WATCHLIST_FILE_MESSAGE);
        }
        return rawTicker.trim().toUpperCase();
    }

    private Double parseWatchlistOptionalPrice(String rawPrice) throws AppException {
        String trimmed = rawPrice == null ? "" : rawPrice.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return onCorrupted(() -> {
            double value = Double.parseDouble(trimmed);
            if (value <= 0) {
                throw new IllegalArgumentException();
            }
            return value;
        }, CORRUPTED_WATCHLIST_FILE_MESSAGE);
    }

    public record BulkUpdateResult(int successCount, int failedCount, List<String> failures) {
    }
}
