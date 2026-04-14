//@@author joeLku1
package duke;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Ui {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("0.00");
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.############");
    private static final String DIVIDER = "----------------------------------------";
    private static final int CHART_SIDE_WIDTH = 23;
    private static final int TICKER_WIDTH = 10;

    private void println(String message) {
        System.out.println(message);
    }

    private void printDivider() {
        println(DIVIDER);
    }

    private void printFormatted(String format, Object... args) {
        println(String.format(format, args));
    }

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
        printDivider();
    }

    /**
     * Prints the divider that marks the end of a response block.
     */
    public void endResponse() {
        printDivider();
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
                                     /watch add --type TYPE --ticker TICKER --price PRICE
                   /watch remove --type TYPE --ticker TICKER
                   /watch list
                   /watch buy --type TYPE --ticker TICKER --qty QTY --portfolio NAME
                   /set --ticker TICKER --price PRICE [--type TYPE]
                   /setmany --file FILEPATH
                   /value
                   /insights [--type stock|etf|bond] [--top N] [--chart]
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

        if (portfolios.isEmpty()) {
            System.out.println("No portfolios available. Create one with /create NAME.");
            return;
        }

        System.out.println("Portfolios (alphabetical):");
        for (Portfolio portfolio : portfolios) {
            System.out.println(portfolio.getName()
                    + " realized=" + formatSignedMoney(portfolio.getTotalRealizedPnl())
                    + " unrealized=" + formatSignedMoney(portfolio.getTotalUnrealizedPnl()));
        }
    }
    
    /**
     * Prints all watchlist items with optional target prices.
     *
     * @param watchlist source of watchlist data.
     */
    public void showWatchlist(Watchlist watchlist) {
        assert watchlist != null : "watchlist must not be null";
        List<WatchlistItem> items = watchlist.getItems();

        if (items.isEmpty()) {
            System.out.println("Watchlist is empty.");
            return;
        }

        System.out.println("Watchlist:");
        for (int i = 0; i < items.size(); i++) {
            WatchlistItem item = items.get(i);
            String priceText = item.hasPrice() ? formatMoney(item.targetPrice()) : "-";
            System.out.println((i + 1) + " "
                    + item.assetType().name()
                    + " "
                    + item.ticker()
                    + " "
                    + priceText);
        }
        System.out.println("Total watchlist items: " + items.size());
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

        String header = String.format("%-3s %-5s %-6s %8s %8s %10s %10s",
            "#", "TYPE", "TICKR", "QTY", "AVG_BUY", "MKT_PRICE", "VALUE");
        System.out.println(header);
        System.out.println("----------------------------------------------------------------");

        List<Holding> holdings = portfolio.getHoldings();
        int index = 1;
        double filteredValueTotal = 0.0;
        for (Holding holding : holdings) {
            if (filterType != null && holding.getAssetType() != filterType) {
                continue;
            }

            String priceText = holding.hasPrice() ? formatMoney(holding.getLastPrice()) : "-";
            String valueText = holding.hasPrice() ? formatMoney(holding.getValue()) : "-";

            printFormatted("%-3d %-5s %-6s %8s %8s %10s %10s",
                    index,
                    holding.getAssetType().name(),
                    toMaxTickerWidth(holding.getTicker()),
                    formatNumber(holding.getQuantity()),
                    formatMoney(holding.getAverageBuyPrice()),
                    priceText,
                    valueText);

            if (holding.hasPrice()) {
                filteredValueTotal += holding.getValue();
            }
            index++;
        }

        System.out.println("Total holdings: " + (index - 1));
        double totalValue = (filterType == null) ? portfolio.getPricedTotalValue() : filteredValueTotal;
        System.out.println("Total value: " + formatMoney(totalValue));
        System.out.println("Note: VALUE = QTY x MKT_PRICE. AVG_BUY is cost basis.");
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
        System.out.println("Unrealized P&L by holding:");
        System.out.println(String.format("%-6s %-5s %8s %8s %8s %10s",
            "TICKER", "TYPE", "QTY", "AVG", "LAST", "U_PNL"));
        System.out.println("-------------------------------------------------------");

        List<Holding> holdings = portfolio.getHoldings();
        for (Holding holding : holdings) {
            String lastPriceText = holding.hasPrice() ? formatMoney(holding.getLastPrice()) : "-";
            printFormatted("%-6s %-5s %8s %8s %8s %10s",
                toMaxTickerWidth(holding.getTicker()),
                holding.getAssetType().name(),
                formatNumber(holding.getQuantity()),
                formatMoney(holding.getAverageBuyPrice()),
                lastPriceText,
                formatSignedMoney(holding.getUnrealizedPnl()));
        }

        System.out.println("Total unrealized P&L: " + formatSignedMoney(portfolio.getTotalUnrealizedPnl()));
    }

    /**
     * Prints per-holding performance insights in a text-only table format.
     * This increment focuses on unrealized per-holding metrics and portfolio-level summary.
     *
     * @param portfolio portfolio to inspect.
     */
    public void showInsightsTable(Portfolio portfolio) {
        showInsightsTable(portfolio, null, null, false);
    }

    /**
     * Prints per-holding performance insights with optional type filter and top-N limit.
     *
     * @param portfolio portfolio to inspect.
     * @param filterType optional asset-type filter; null means all holdings.
     * @param topN optional max number of holdings to display; null means all.
     * @param showChart whether to include a diverging P&L chart.
     */
    public void showInsightsTable(Portfolio portfolio, AssetType filterType, Integer topN, boolean showChart) {
        assert portfolio != null : "portfolio must not be null";
        System.out.println("Insights for portfolio: " + portfolio.getName());

        List<Holding> filteredHoldings = portfolio.getHoldings().stream()
                .filter(holding -> filterType == null || holding.getAssetType() == filterType)
                .toList();

        if (filteredHoldings.isEmpty()) {
            System.out.println("No holdings to analyze.");
            return;
        }

        List<Holding> displayHoldings;
        if (topN != null) {
            displayHoldings = filteredHoldings.stream()
                    .sorted(Comparator.comparingDouble(Holding::getUnrealizedPnl).reversed()
                            .thenComparing(Holding::getTicker))
                    .limit(topN)
                    .toList();
        } else {
            displayHoldings = filteredHoldings.stream()
                    .sorted(Comparator.comparing(Holding::getTicker))
                    .toList();
        }

        if (filterType != null || topN != null) {
            String filterText = filterType == null ? "ALL" : filterType.name();
            String topText = topN == null ? "ALL" : String.valueOf(topN);
            System.out.println("View: type=" + filterText + ", top=" + topText);
        }

        String header = String.format("%-3s %-5s %-" + TICKER_WIDTH + "s %8s %8s %8s %10s %8s",
            "#", "TYPE", "TICKER", "QTY", "AVG", "LAST", "U_PNL", "U%");
        System.out.println(header);
        System.out.println("----------------------------------------------------------------------");

        double totalCostBasis = 0.0;
        double totalUnrealized = 0.0;
        int pricedCount = 0;

        Holding bestHolding = null;
        Holding worstHolding = null;

        for (int i = 0; i < displayHoldings.size(); i++) {
            Holding holding = displayHoldings.get(i);
            double quantity = holding.getQuantity();
            double avg = holding.getAverageBuyPrice();
            double costBasis = quantity * avg;

            String lastText = holding.hasPrice() ? formatMoney(holding.getLastPrice()) : "-";
            double unrealized = holding.hasPrice() ? holding.getUnrealizedPnl() : 0.0;
            String unrealizedText = formatSignedMoney(unrealized);
            String unrealizedPctText = holding.hasPrice() && costBasis > 0
                    ? formatSignedPercent(unrealized / costBasis)
                    : "n/a";

                System.out.println(String.format("%-3d %-5s %-" + TICKER_WIDTH + "s %8s %8s %8s %10s %8s",
                    i + 1,
                    holding.getAssetType().name(),
                    toMaxTickerWidth(holding.getTicker()),
                    formatNumber(quantity),
                    formatMoney(avg),
                    lastText,
                    unrealizedText,
                    unrealizedPctText));
        }

        for (Holding holding : filteredHoldings) {
            double quantity = holding.getQuantity();
            double avg = holding.getAverageBuyPrice();
            double costBasis = quantity * avg;
            totalCostBasis += costBasis;

            double unrealized = holding.hasPrice() ? holding.getUnrealizedPnl() : 0.0;
            if (holding.hasPrice()) {
                pricedCount++;
                totalUnrealized += unrealized;

                if (bestHolding == null || unrealized > bestHolding.getUnrealizedPnl()) {
                    bestHolding = holding;
                }
                if (worstHolding == null || unrealized < worstHolding.getUnrealizedPnl()) {
                    worstHolding = holding;
                }
            }
        }

        if (showChart) {
            printInsightsChart(displayHoldings);
        }

        int unpricedCount = filteredHoldings.size() - pricedCount;
        System.out.println("----------------------------------------------------------------------");
        System.out.println("Summary:");
        System.out.println("- Holdings: " + filteredHoldings.size()
                + " (priced: " + pricedCount + ", unpriced: " + unpricedCount + ")");
        System.out.println("- Open cost basis: " + formatMoney(totalCostBasis));
        System.out.println("- Unrealized P&L: " + formatSignedMoney(totalUnrealized)
                + " (" + formatSignedPercent(safeRatio(totalUnrealized, totalCostBasis)) + ")");
        System.out.println("- Realized P&L (portfolio): " + formatSignedMoney(portfolio.getTotalRealizedPnl()));
        System.out.println("- Net P&L: " + formatSignedMoney(portfolio.getTotalRealizedPnl() + totalUnrealized));

        if (bestHolding != null) {
            System.out.println("- Top contributor: " + bestHolding.getTicker()
                    + " " + formatSignedMoney(bestHolding.getUnrealizedPnl()));
        }
        if (worstHolding != null && worstHolding.getUnrealizedPnl() < 0) {
            System.out.println("- Top detractor: " + worstHolding.getTicker()
                    + " " + formatSignedMoney(worstHolding.getUnrealizedPnl()));
        } else {
            System.out.println("- Top detractor: none");
        }
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

    /**
     * Formats a decimal ratio as signed percent (e.g. 0.123 -> +12.30%).
     *
     * @param ratio decimal ratio value.
     * @return string representing signed percent value.
     */
    public static String formatSignedPercent(double ratio) {
        String abs = formatMoney(Math.abs(ratio * 100));
        return (ratio >= 0 ? "+" : "-") + abs + "%";
    }

    private static double safeRatio(double numerator, double denominator) {
        if (denominator == 0) {
            return 0;
        }
        return numerator / denominator;
    }

    private static String toMaxTickerWidth(String ticker) {
        if (ticker == null) {
            return "";
        }
        return ticker.length() <= 10 ? ticker : ticker.substring(0, 10);
    }

    private void printInsightsChart(List<Holding> holdings) {
        double maxAbsPercent = 0.0;
        for (Holding holding : holdings) {
            if (!holding.hasPrice()) {
                continue;
            }
            double costBasis = holding.getQuantity() * holding.getAverageBuyPrice();
            if (costBasis <= 0) {
                continue;
            }
            double unrealizedPercent = holding.getUnrealizedPnl() / costBasis;
            maxAbsPercent = Math.max(maxAbsPercent, Math.abs(unrealizedPercent));
        }

        if (maxAbsPercent == 0.0) {
            maxAbsPercent = 1.0;
        }

        System.out.println("\nP&L chart (loss | gain):");
        System.out.println("Scale: full side width = " + formatSignedPercent(maxAbsPercent));
        printFormatted("%-" + TICKER_WIDTH + "s %s",
                "",
                "-".repeat(CHART_SIDE_WIDTH) + "|" + "+".repeat(CHART_SIDE_WIDTH));
        printFormatted("%-" + TICKER_WIDTH + "s %s",
                "",
                "loss" + " ".repeat(CHART_SIDE_WIDTH - 4)
                        + "|"
                        + " ".repeat(CHART_SIDE_WIDTH - 4) + "gain");
        for (Holding holding : holdings) {
            double costBasis = holding.getQuantity() * holding.getAverageBuyPrice();
            double unrealizedPercent = 0.0;
            if (holding.hasPrice() && costBasis > 0) {
                unrealizedPercent = holding.getUnrealizedPnl() / costBasis;
            }
            String bar = buildDivergingBar(unrealizedPercent, maxAbsPercent);
            printFormatted("%-" + TICKER_WIDTH + "s %s %10s",
                    toMaxTickerWidth(holding.getTicker()),
                    bar,
                    formatSignedPercent(unrealizedPercent));
        }
    }

    private String buildDivergingBar(double value, double maxAbsValue) {
        int scaledUnits = (int) Math.round((Math.abs(value) / maxAbsValue) * CHART_SIDE_WIDTH);
        if (scaledUnits > CHART_SIDE_WIDTH) {
            scaledUnits = CHART_SIDE_WIDTH;
        }

        if (value > 0) {
            return " ".repeat(CHART_SIDE_WIDTH)
                    + "|"
                    + "+".repeat(scaledUnits)
                + " ".repeat(CHART_SIDE_WIDTH - scaledUnits);
        }

        if (value < 0) {
            return " ".repeat(CHART_SIDE_WIDTH - scaledUnits)
                    + "-".repeat(scaledUnits)
                    + "|"
                + " ".repeat(CHART_SIDE_WIDTH);
        }

        return " ".repeat(CHART_SIDE_WIDTH) + "|" + " ".repeat(CHART_SIDE_WIDTH);
    }
}
