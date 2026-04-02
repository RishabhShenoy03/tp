package duke;

import java.nio.file.Path;

/**
 * Main application controller for the Stocks Tracker command-line program.
 * Coordinates command parsing, execution, persistence, and UI output.
 */
public class CG2StocksTracker {
    private static final String FILE_PATH = "data/CG2StocksTracker.txt";

    private final Ui ui;
    private final Parser parser;
    private final Storage storage;
    private final PortfolioBook portfolioBook;
    private final Watchlist watchlist;

    /**
     * Creates the application and loads persisted portfolio data from storage.
     *
     * @param filePath path to the storage file.
     */
    public CG2StocksTracker(String filePath) {
        this.ui = new Ui();
        this.parser = new Parser();
        this.storage = new Storage(filePath);

        PortfolioBook loadedBook;
        try {
            loadedBook = storage.load();
        } catch (AppException e) {
            ui.showMessage("Storage load failed: " + e.getMessage());
            loadedBook = new PortfolioBook();
        }
        this.portfolioBook = loadedBook;

        Watchlist loadedWatchlist;
        try {
            loadedWatchlist = storage.loadWatchlist();
        } catch (AppException e) {
            ui.showMessage("Watchlist load failed: " + e.getMessage());
            loadedWatchlist = new Watchlist();
        }
        this.watchlist = loadedWatchlist;
    }

    /**
     * Starts the application using the default storage file path.
     *
     * @param args command-line arguments (unused).
     */
    public static void main(String[] args) {
        new CG2StocksTracker(FILE_PATH).run();
    }

    /**
     * Runs the main application loop until the user exits.
     */
    public void run() {
        ui.showWelcome();

        while (true) {
            String input = ui.readCommand();

            try {
                ParsedCommand command = parser.parse(input);
                ui.beginResponse();
                boolean shouldContinue = execute(command);
                ui.endResponse();
                if (!shouldContinue) {
                    break;
                }
            } catch (AppException e) {
                ui.beginResponse();
                ui.showError(e.getMessage());
                ui.endResponse();
            }
        }
    }

    /**
     * Executes a parsed command and returns whether the main loop should continue.
     *
     * @param command parsed user command.
     * @return true to continue running; false to exit the application.
     * @throws AppException if command execution fails.
     */
    private boolean execute(ParsedCommand command) throws AppException {
        switch (command.type()) {
        case CREATE:
            handleCreate(command);
            return true;
        case USE:
            handleUse(command);
            return true;
        case LIST:
            handleList(command);
            return true;
        case ADD:
            handleAdd(command);
            return true;
        case REMOVE:
            handleRemove(command);
            return true;
        case WATCH:
            handleWatch(command);
            return true;
        case SET:
            handleSet(command);
            return true;
        case SET_MANY:
            handleSetMany(command);
            return true;
        case VALUE:
            handleValue();
            return true;
        case INSIGHTS:
            handleInsights(command);
            return true;
        case HELP:
            ui.showHelp();
            return true;
        case EXIT:
            ui.showGoodbye();
            return false;
        default:
            throw new AppException("Unknown command type: " + command.type());
        }
    }

    /**
     * Creates a new portfolio and persists the updated portfolio book.
     *
     * @param command parsed create command.
     * @throws AppException if portfolio creation or saving fails.
     */
    private void handleCreate(ParsedCommand command) throws AppException {
        String name = command.name();
        portfolioBook.createPortfolio(name);
        save();
        ui.showMessage("Created portfolio: " + name);
        ui.showMessage("Active portfolio: " + portfolioBook.getActivePortfolioName());
    }

    /**
     * Switches the active portfolio.
     *
     * @param command parsed use command.
     * @throws AppException if the target portfolio does not exist.
     */
    private void handleUse(ParsedCommand command) throws AppException {
        String name = command.name();
        portfolioBook.usePortfolio(name);
        ui.showMessage("Active portfolio: " + name);
    }

    /**
     * Displays portfolios or holdings based on the list target supplied.
     *
     * @param command parsed list command.
     * @throws AppException if an active portfolio is required but unavailable.
     */
    //@@author 
    private void handleList(ParsedCommand command) throws AppException {
        String target = command.listTarget();

        if ("--portfolios".equals(target)) {
            ui.showPortfolioSummaries(portfolioBook);
            return;
        }

        if ("--stock".equals(target)) {
            ui.showHoldings(portfolioBook.getActivePortfolio(), AssetType.STOCK);
            return;
        }

        if ("--etf".equals(target)) {
            ui.showHoldings(portfolioBook.getActivePortfolio(), AssetType.ETF);
            return;
        }

        if ("--bond".equals(target)) {
            ui.showHoldings(portfolioBook.getActivePortfolio(), AssetType.BOND);
            return;
        }

        if (portfolioBook.hasActivePortfolio()) {
            ui.showHoldings(portfolioBook.getActivePortfolio());
        } else {
            ui.showPortfolios(portfolioBook);
        }
    }

    /**
     * Adds a holding to the active portfolio and persists the change.
     * Fees are included in the holding cost basis via the parsed total fee amount.
     *
     * @param command parsed add command.
     * @throws AppException if there is no active portfolio or saving fails.
     */
    private void handleAdd(ParsedCommand command) throws AppException {
        Portfolio portfolio = portfolioBook.getActivePortfolio();
        AssetType type = command.assetType();
        String ticker = command.ticker();
        double qty = command.quantity();
        double price = command.price();
        double fees = command.totalFees();

        boolean existed = portfolio.hasHolding(type, ticker);
        double newQty = portfolio.addHolding(type, ticker, qty, price, fees);
        save();

        Holding holding = portfolio.getHolding(type, ticker);
        if (existed) {
            ui.showMessage("Updated holding quantity: " + ticker + " (" + type.toDisplay() + ") = "
                    + Ui.formatNumber(newQty)
                    + ", avg buy price = " + Ui.formatMoney(holding.getAverageBuyPrice()));
        } else {
            ui.showAddedHolding(holding);
        }
    }

    /**
     * Removes quantity from a holding in the active portfolio and persists the change.
     * Fees are deducted from realized profit/loss via the parsed total fee amount.
     *
     * @param command parsed remove command.
     * @throws AppException if the holding cannot be removed or saving fails.
     */
    private void handleRemove(ParsedCommand command) throws AppException {
        Portfolio portfolio = portfolioBook.getActivePortfolio();
        AssetType type = command.assetType();
        String ticker = command.ticker();
        Double qty = command.quantity();
        Double price = command.price();
        double fees = command.totalFees();

        Portfolio.RemoveResult result;
        try {
            result = portfolio.removeHolding(type, ticker, qty, price, fees);
        } catch (IllegalArgumentException e) {
            throw new AppException(e.getMessage());
        }
        save();
        String feeText = result.fees() > 0 ? ", fees = " + Ui.formatMoney(result.fees()) : "";
        ui.showMessage("Sold " + Ui.formatNumber(result.soldQuantity())
            + " of " + ticker + " (" + type.toDisplay() + ") at " + Ui.formatMoney(result.soldPrice())
            + feeText + ", realized P&L = " + Ui.formatSignedMoney(result.realizedPnl()));
    }

    /**
     * Updates last price(s) in the active portfolio and saves the result.
     * If type is provided, updates one holding by type+ticker.
     * Otherwise, updates all holdings with the same ticker.
     *
     * @param command parsed set command.
     * @throws AppException if no matching holding is found or saving fails.
     */
    private void handleSet(ParsedCommand command) throws AppException {
        Portfolio portfolio = portfolioBook.getActivePortfolio();
        AssetType type = command.assetType();
        String ticker = command.ticker();
        double price = command.price();

        if (type != null) {
            boolean updated = portfolio.setPriceForHolding(type, ticker, price);
            if (!updated) {
                throw new AppException("Holding not found: " + ticker + " (" + type.toDisplay() + ")");
            }

            save();
            ui.showMessage("Updated price: " + ticker + " (" + type.toDisplay() + ") = " + Ui.formatMoney(price));
            return;
        }

        int updatedCount = portfolio.setPriceForTicker(ticker, price);
        if (updatedCount == 0) {
            throw new AppException("Holding not found for ticker: " + ticker);
        }
        save();
        ui.showMessage("Updated price: " + ticker + " = " + Ui.formatMoney(price));
    }

    private void handleWatch(ParsedCommand command) throws AppException {
        String action = command.name();
        if (action == null || action.isBlank()) {
            throw new AppException("Watch action is required.");
        }

        switch (action) {
        case "list":
            ui.showWatchlist(watchlist);
            break;
        case "add":
            handleWatchAdd(command);
            break;
        case "remove":
            handleWatchRemove(command);
            break;
        case "buy":
            handleWatchBuy(command);
            break;
        default:
            throw new AppException("Unknown watch action: " + action);
        }
    }

    private void handleWatchAdd(ParsedCommand command) throws AppException {
        AssetType type = command.assetType();
        String ticker = command.ticker();
        Double price = command.price();

        try {
            watchlist.addItem(type, ticker, price);
        } catch (IllegalArgumentException e) {
            throw new AppException(e.getMessage());
        }

        save();
        String priceText = price == null ? "-" : Ui.formatMoney(price);
        ui.showMessage("Added to watchlist: " + ticker + " (" + type.toDisplay() + "), price = " + priceText);
    }

    private void handleWatchRemove(ParsedCommand command) throws AppException {
        AssetType type = command.assetType();
        String ticker = command.ticker();

        boolean removed;
        try {
            removed = watchlist.removeItem(type, ticker);
        } catch (IllegalArgumentException e) {
            throw new AppException(e.getMessage());
        }

        if (!removed) {
            throw new AppException("Watchlist item not found: " + ticker + " (" + type.toDisplay() + ")");
        }

        save();
        ui.showMessage("Removed from watchlist: " + ticker + " (" + type.toDisplay() + ")");
    }

    private void handleWatchBuy(ParsedCommand command) throws AppException {
        AssetType type = command.assetType();
        String ticker = command.ticker();
        String portfolioName = command.listTarget();

        Watchlist.BuyResult result;
        try {
            result = watchlist.buyItem(type, ticker, portfolioName, portfolioBook);
        } catch (IllegalArgumentException e) {
            throw new AppException(e.getMessage());
        }

        save();
        ui.showMessage("Bought " + Ui.formatNumber(result.boughtQuantity())
                + " of " + result.ticker()
                + " (" + result.assetType().toDisplay() + ") into portfolio " + result.portfolioName()
                + " at " + Ui.formatMoney(result.buyPrice())
                + ". New quantity = " + Ui.formatNumber(result.resultingQuantity()));
    }

    /**
     * Loads multiple price updates from a CSV file for the active portfolio and saves the result.
     *
     * @param command parsed setmany command.
     * @throws AppException if loading updates or saving fails.
     */
    private void handleSetMany(ParsedCommand command) throws AppException {
        Portfolio portfolio = portfolioBook.getActivePortfolio();
        Path filePath = command.filePath();

        Storage.BulkUpdateResult result = storage.loadPriceUpdates(filePath, portfolio);
        save();

        ui.showBulkUpdateResult(result);
    }

    /**
     * Displays the current valuation summary for the active portfolio.
     *
     * @throws AppException if no active portfolio is selected.
     */
    private void handleValue() throws AppException {
        Portfolio portfolio = portfolioBook.getActivePortfolio();
        ui.showPortfolioValue(portfolio);
    }

    /**
     * Displays insights for the active portfolio based on optional filter settings.
     *
     * @param command parsed insights command.
     * @throws AppException if the insights options are invalid or no active portfolio is selected.
     */
    private void handleInsights(ParsedCommand command) throws AppException {
        InsightsOptions options = parseInsightsOptions(command.listTarget());
        Portfolio portfolio = portfolioBook.getActivePortfolio();
        ui.showInsightsTable(portfolio, options.filterType(), options.topN(), options.showChart());
    }

    /**
     * Parses optional arguments for the /insights command.
     *
     * @param rawOptions raw option string captured from the parser.
     * @return parsed insights options.
     * @throws AppException if the options are malformed or unsupported.
     */
    private InsightsOptions parseInsightsOptions(String rawOptions) throws AppException {
        if (rawOptions == null || rawOptions.isBlank()) {
            return new InsightsOptions(null, null, false);
        }

        String[] tokens = rawOptions.trim().split("\\s+");
        AssetType filterType = null;
        Integer topN = null;
        boolean showChart = false;

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].toLowerCase();

            switch (token) {
            case "--type":
                if (i + 1 >= tokens.length) {
                    throw new AppException("Usage: /insights [--type stock|etf|bond] [--top N] [--chart]");
                }
                try {
                    filterType = AssetType.fromString(tokens[++i]);
                } catch (IllegalArgumentException e) {
                    throw new AppException(e.getMessage());
                }
                break;
            case "--top":
                if (i + 1 >= tokens.length) {
                    throw new AppException("Usage: /insights [--type stock|etf|bond] [--top N] [--chart]");
                }
                try {
                    topN = Integer.parseInt(tokens[++i]);
                } catch (NumberFormatException e) {
                    throw new AppException("Top count must be a positive integer.");
                }
                if (topN <= 0) {
                    throw new AppException("Top count must be a positive integer.");
                }
                break;
            case "--chart":
                showChart = true;
                break;
            default:
                throw new AppException("Unknown /insights option: " + tokens[i]
                        + "\nUsage: /insights [--type stock|etf|bond] [--top N] [--chart]");
            }
        }

        return new InsightsOptions(filterType, topN, showChart);
    }

    private record InsightsOptions(AssetType filterType, Integer topN, boolean showChart) {
    }

    /**
     * Saves portfolio and watchlist state to persistent storage.
     *
     * @throws AppException if saving fails.
     */
    private void save() throws AppException {
        storage.save(portfolioBook);
        storage.saveWatchlist(watchlist);
    }
}
