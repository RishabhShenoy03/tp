package duke;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Scanner;

public class Ui {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("0.00");
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.############");
    private static final String DIVIDER = "----------------------------------------";

    /**
     * Reads the next user command from standard input.
     * Returns /exit when input is exhausted so the app can terminate gracefully.
     * @return trimmed user command, or /exit when no more input is available.
     */
    public String readCommand() {
        if (!INPUT.hasNextLine()) {
            return "/exit";
        }
        return INPUT.nextLine().trim();
    }

    /**
     * Prints the application welcome header and welcome message.
     */
    public void showWelcome() {
        System.out.println("Welcome to Stocks Tracker!");
        System.out.println("""
                             ______  ________   ______   __    __  __    __   ______
                            /      \\|        \\ /      \\ |  \\  |  \\|  \\  /  \\ /      \\
                           |  $$$$$$\\\\$$$$$$$$|  $$$$$$\\| $$\\ | $$| $$ /  $$|  $$$$$$\\
                           | $$___\\$$  | $$   | $$  | $$| $$$\\| $$| $$/  $$ | $$___\\$$
                            \\$$    \\   | $$   | $$  | $$| $$$$\\ $$| $$  $$   \\$$    \\
                            _\\$$$$$$\\  | $$   | $$  | $$| $$\\$$ $$| $$$$$\\   _\\$$$$$$\\
                           |  \\__| $$  | $$   | $$__/ $$| $$ \\$$$$| $$ \\$$\\ |  \\__| $$
                            \\$$    $$  | $$    \\$$    $$| $$  \\$$$| $$  \\$$\\ \\$$    $$
                             \\$$$$$$    \\$$     \\$$$$$$  \\$$   \\$$ \\$$   \\$$  \\$$$$$$

                            """);
        System.out.println("Type /help to see available commands.");
    }

    /**
     * Prints the divider that marks the start of a response block.
     */
    public void beginResponse() {
        System.out.println();
        System.out.println(DIVIDER);
    }

    /**
     * Prints the divider that marks the end of a response block.
     */
    public void endResponse() {
        System.out.println(DIVIDER);
        System.out.println();
    }

    /**
     * Prints the farewell message.
     */
    public void showGoodbye() {
        showMessage("Thank you and goodbye.");
    }

    /**
     * Prints a normal message to standard output.
     * Wrapper function for println.
     *
     * @param message text to print.
     */
    public void showMessage(String message) {
        System.out.println(message);
    }

    /**
     * Prints an error message to standard output.
     * Wrapper function for println.
     *
     * @param message error text to print.
     */
    public void showError(String message) {
        System.out.println(message);
    }

    /**
     * Prints the help menu and command usage summary.
     */
    public void showHelp() {
        String s = """
                   Available commands:
                   /create NAME
                   /use NAME
                   /list
                   /list --stock
                   /list --etf
                   /list --bond
                   /list --portfolios
                   /add --type TYPE --ticker TICKER --qty QTY --price PRICE
                     Optional fees: --brokerage FEE --fx FEE --platform FEE
                   /remove --type TYPE --ticker TICKER
                     Optional fields: --qty QTY --price PRICE --brokerage FEE --fx FEE --platform FEE
                   /set --ticker TICKER --price PRICE
                   /setmany --file FILEPATH
                   /value
                   /insights [OPTIONS]
                   /help
                   /exit
                   """;
        System.out.println(s);
    }

    /**
     * Prints all portfolios and marks the active one.
     *
     * @param portfolioBook source of portfolio data.
     */
    public void showPortfolios(PortfolioBook portfolioBook) {
        assert portfolioBook != null : "portfolioBook must not be null";
        assert portfolioBook.getPortfolios() != null : "portfolios must not be null";
        List<Portfolio> portfolios = portfolioBook.getPortfolios();
        System.out.println("Portfolios (" + portfolios.size() + "):");
        for (Portfolio portfolio : portfolios) {
            String suffix = portfolio.getName().equals(portfolioBook.getActivePortfolioName()) ? " (active)" : "";
            System.out.println(portfolio.getName() + suffix);
        }
    }

    /**
     * Prints portfolio-level realized and unrealized profit/loss summaries
     * in alphabetical portfolio-name order.
     *
     * @param portfolioBook source of portfolio data.
     */
    public void showPortfolioSummaries(PortfolioBook portfolioBook) {
        assert portfolioBook != null : "portfolioBook must not be null";
        List<Portfolio> portfolios = portfolioBook.getPortfolios()
                .stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();

        System.out.println("Portfolios (alphabetical):");
        for (Portfolio portfolio : portfolios) {
            System.out.println(portfolio.getName()
                    + " realized=" + formatSignedMoney(portfolio.getTotalRealizedPnl())
                    + " unrealised=" + formatSignedMoney(portfolio.getTotalUnrealizedPnl()));
        }
    }

    /**
     * Prints details of a newly added holding.
     *
     * @param holding holding that was added.
     */
    public void showAddedHolding(Holding holding) {
        assert holding != null : "holding must not be null";
        System.out.println("Added holding:");
        System.out.println("Type: " + holding.getAssetType().toDisplay());
        System.out.println("Ticker: " + holding.getTicker());
        System.out.println("Quantity: " + formatNumber(holding.getQuantity()));
        System.out.println("Average buy price: " + formatMoney(holding.getAverageBuyPrice()));
    }

    /**
     * Prints all holdings in a portfolio.
     *
     * @param portfolio portfolio to display.
     */
    public void showHoldings(Portfolio portfolio) {
        showHoldings(portfolio, null);
    }

    /**
     * Prints holdings in a portfolio, optionally filtered by asset type.
     *
     * @param portfolio portfolio to display.
     * @param filterType optional asset-type filter; null means show all holdings.
     */
    public void showHoldings(Portfolio portfolio, AssetType filterType) {
        assert portfolio != null : "portfolio must not be null";
        System.out.println("Portfolio: " + portfolio.getName());

        List<Holding> holdings = portfolio.getHoldings();
        int index = 1;
        double filteredValueTotal = 0.0;
        for (Holding holding : holdings) {
            if (filterType != null && holding.getAssetType() != filterType) {
                continue;
            }

            String priceText = holding.hasPrice() ? formatMoney(holding.getLastPrice()) : "-";
            String valueText = holding.hasPrice() ? formatMoney(holding.getValue()) : "-";

            System.out.println(index + " "
                    + holding.getAssetType().name()
                    + " "
                    + holding.getTicker()
                    + " "
                    + formatNumber(holding.getQuantity())
                    + " "
                    + priceText
                    + " "
                    + valueText);

            if (holding.hasPrice()) {
                filteredValueTotal += holding.getValue();
            }
            index++;
        }

        System.out.println("Total holdings: " + (index - 1));
        double totalValue = (filterType == null) ? portfolio.getPricedTotalValue() : filteredValueTotal;
        System.out.println("Total value: " + formatMoney(totalValue));
    }

    /**
     * Prints current portfolio value and realized/unrealized profit/loss breakdown.
     *
     * @param portfolio portfolio to value.
     */
    public void showPortfolioValue(Portfolio portfolio) {
        assert portfolio != null : "portfolio must not be null";
        System.out.println("Portfolio: " + portfolio.getName());
        System.out.println("Current total value: " + formatMoney(portfolio.getCurrentTotalValue()));
        System.out.println("Realized P&L: " + formatSignedMoney(portfolio.getTotalRealizedPnl()));
        System.out.println("Unrealised P&L by holding:");

        List<Holding> holdings = portfolio.getHoldings();
        for (Holding holding : holdings) {
            String lastPriceText = holding.hasPrice() ? formatMoney(holding.getLastPrice()) : "-";
            System.out.println(holding.getTicker()
                    + ": Quantity " + formatNumber(holding.getQuantity())
                    + ", Avg. Price = " + formatMoney(holding.getAverageBuyPrice())
                    + ", Last Price = " + lastPriceText
                    + ", Unrealised P&L = " + formatSignedMoney(holding.getUnrealizedPnl()));
        }

        System.out.println("Total unrealised P&L: " + formatSignedMoney(portfolio.getTotalUnrealizedPnl()));
    }

    /**
     * Prints the outcome of a bulk price-update operation.
     *
     * @param result bulk update summary and row-level failures.
     */
    public void showBulkUpdateResult(Storage.BulkUpdateResult result) {
        assert result != null : "result must not be null";
        System.out.println("Updated prices: " + result.successCount() + " succeeded, "
                + result.failedCount() + " failed");

        if (!result.failures().isEmpty()) {
            System.out.println("Failed rows:");
            for (String failure : result.failures()) {
                System.out.println(failure);
            }
        }
    }

    /**
     * Formats a value as money with two decimal places.
     *
     * @param value numeric value.
     * @return string representing the formatted money value.
     */
    public static String formatMoney(double value) {
        return MONEY_FORMAT.format(value);
    }

    /**
     * Formats a number with up to 12 decimal places and no trailing zeros.
     *
     * @param value numeric value.
     * @return string representing the formatted number.
     */
    public static String formatNumber(double value) {
        return NUMBER_FORMAT.format(value);
    }

    /**
     * Formats a value as signed money.
     *
     * @param value numeric value.
     * @return string representing the signed money value.
     */
    public static String formatSignedMoney(double value) {
        String abs = formatMoney(Math.abs(value));
        return (value >= 0 ? "+" : "-") + abs;
    }
}
