package duke;

import java.nio.file.Path;
import java.util.List;

public class CG2StocksTracker {
    private static final String FILE_PATH = "data/CG2StocksTracker.txt";

    private final Ui ui;
    private final Parser parser;
    private final Storage storage;
    private final PortfolioBook portfolioBook;

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
    }

    public static void main(String[] args) {
        new CG2StocksTracker(FILE_PATH).run();
    }

    public void run() {
        ui.showWelcome();

        while (true) {
            String input = ui.readCommand();

            try {
                ParsedCommand command = parser.parse(input);
                boolean shouldContinue = execute(command);
                if (!shouldContinue) {
                    break;
                }
            } catch (AppException e) {
                ui.showError(e.getMessage());
            }
        }
    }

    private boolean execute(ParsedCommand command) throws AppException {
        return switch (command.type()) {
            case CREATE -> {
                handleCreate(command);
                yield true;
            }
            case USE -> {
                handleUse(command);
                yield true;
            }
            case LIST -> {
                handleList(command);
                yield true;
            }
            case ADD -> {
                handleAdd(command);
                yield true;
            }
            case REMOVE -> {
                handleRemove(command);
                yield true;
            }
            case SET -> {
                handleSet(command);
                yield true;
            }
            case SET_MANY -> {
                handleSetMany(command);
                yield true;
            }
            case VALUE -> {
                handleValue();
                yield true;
            }
            case HELP -> {
                ui.showHelp();
                yield true;
            }
            case EXIT -> {
                ui.showGoodbye();
                yield false;
            }
        };
    }

    private void handleCreate(ParsedCommand command) throws AppException {
        String name = command.name();
        portfolioBook.createPortfolio(name);
        save();
        ui.showMessage("Created portfolio: " + name);
        ui.showMessage("Active portfolio: " + portfolioBook.getActivePortfolioName());
    }

    private void handleUse(ParsedCommand command) throws AppException {
        String name = command.name();
        portfolioBook.usePortfolio(name);
        ui.showMessage("Active portfolio: " + name);
    }

    private void handleList(ParsedCommand command) throws AppException {
        String target = command.listTarget();

        if ("portfolios".equals(target)) {
            ui.showPortfolios(portfolioBook);
            return;
        }

        if ("holdings".equals(target)) {
            ui.showHoldings(portfolioBook.getActivePortfolio());
            return;
        }

        if (portfolioBook.hasActivePortfolio()) {
            ui.showHoldings(portfolioBook.getActivePortfolio());
        } else {
            ui.showPortfolios(portfolioBook);
        }
    }

    private void handleAdd(ParsedCommand command) throws AppException {
        Portfolio portfolio = portfolioBook.getActivePortfolio();
        AssetType type = command.assetType();
        String ticker = command.ticker();
        double qty = command.quantity();

        boolean existed = portfolio.hasHolding(type, ticker);
        double newQty = portfolio.addHolding(type, ticker, qty);
        save();

        if (existed) {
            ui.showMessage("Updated holding quantity: " + ticker + " (" + type.toDisplay() + ") = "
                    + Ui.formatNumber(newQty));
        } else {
            Holding holding = portfolio.getHolding(type, ticker);
            ui.showAddedHolding(holding);
        }
    }

    private void handleRemove(ParsedCommand command) throws AppException {
        Portfolio portfolio = portfolioBook.getActivePortfolio();
        AssetType type = command.assetType();
        String ticker = command.ticker();

        portfolio.removeHolding(type, ticker);
        save();
        ui.showMessage("Removed holding: " + ticker + " (" + type.toDisplay() + ")");
    }

    private void handleSet(ParsedCommand command) throws AppException {
        Portfolio portfolio = portfolioBook.getActivePortfolio();
        String ticker = command.ticker();
        double price = command.price();

        int updatedCount = portfolio.setPriceForTicker(ticker, price);
        if (updatedCount == 0) {
            throw new AppException("Holding not found for ticker: " + ticker);
        }

        save();
        ui.showMessage("Updated price: " + ticker + " = " + Ui.formatMoney(price));
    }

    private void handleSetMany(ParsedCommand command) throws AppException {
        Portfolio portfolio = portfolioBook.getActivePortfolio();
        Path filePath = command.filePath();

        Storage.BulkUpdateResult result = storage.loadPriceUpdates(filePath, portfolio);
        save();

        ui.showBulkUpdateResult(result);
    }

    private void handleValue() throws AppException {
        Portfolio portfolio = portfolioBook.getActivePortfolio();
        ui.showPortfolioValue(portfolio);
    }

    private void save() throws AppException {
        storage.save(portfolioBook);
    }
}
