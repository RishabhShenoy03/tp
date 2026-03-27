package duke;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Scanner;

public class Ui {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("0.00");
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.############");
    private static final String DIVIDER = "----------------------------------------";

    public String readCommand() {
        if (!INPUT.hasNextLine()) {
            return "/exit";
        }
        return INPUT.nextLine().trim();
    }

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

    public void beginResponse() {
        System.out.println();
        System.out.println(DIVIDER);
    }

    public void endResponse() {
        System.out.println(DIVIDER);
        System.out.println();
    }

    public void showGoodbye() {
        showMessage("Thank you and goodbye.");
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public void showError(String message) {
        System.out.println(message);
    }

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
                   /help
                   /exit
                   """;
        System.out.println(s);
    }

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

    public void showAddedHolding(Holding holding) {
        assert holding != null : "holding must not be null";
        System.out.println("Added holding:");
        System.out.println("Type: " + holding.getAssetType().toDisplay());
        System.out.println("Ticker: " + holding.getTicker());
        System.out.println("Quantity: " + formatNumber(holding.getQuantity()));
        System.out.println("Average buy price: " + formatMoney(holding.getAverageBuyPrice()));
    }

    public void showHoldings(Portfolio portfolio) {
        showHoldings(portfolio, null);
    }

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

    public static String formatMoney(double value) {
        return MONEY_FORMAT.format(value);
    }

    public static String formatNumber(double value) {
        return NUMBER_FORMAT.format(value);
    }

    public static String formatSignedMoney(double value) {
        String abs = formatMoney(Math.abs(value));
        return (value >= 0 ? "+" : "-") + abs;
    }
}
