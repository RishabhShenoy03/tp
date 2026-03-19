package duke;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Scanner;

public class Ui {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("0.00");
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.############");

    public String readCommand() {
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

    public void showGoodbye() {
        System.out.println("Thank you and goodbye.");
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
                   /list portfolios
                   /list holdings
                   /add --type TYPE --ticker TICKER --qty QTY
                   /remove --type TYPE --ticker TICKER
                   /set --ticker TICKER --price PRICE
                   /setmany --file FILEPATH
                   /value
                   /help
                   /exit
                   """;
        System.out.println(s);
    }

    public void showPortfolios(PortfolioBook portfolioBook) {
        List<Portfolio> portfolios = portfolioBook.getPortfolios();
        System.out.println("Portfolios (" + portfolios.size() + "):");
        for (Portfolio portfolio : portfolios) {
            String suffix = portfolio.getName().equals(portfolioBook.getActivePortfolioName()) ? " (active)" : "";
            System.out.println(portfolio.getName() + suffix);
        }
    }

    public void showAddedHolding(Holding holding) {
        System.out.println("Added holding:");
        System.out.println("Type: " + holding.getAssetType().toDisplay());
        System.out.println("Ticker: " + holding.getTicker());
        System.out.println("Quantity: " + formatNumber(holding.getQuantity()));
    }

    public void showHoldings(Portfolio portfolio) {
        System.out.println("Portfolio: " + portfolio.getName());

        List<Holding> holdings = portfolio.getHoldings();
        int index = 1;
        for (Holding holding : holdings) {
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
            index++;
        }

        System.out.println("Total holdings: " + holdings.size());
        System.out.println("Total value: " + formatMoney(portfolio.getPricedTotalValue()));
    }

    public void showPortfolioValue(Portfolio portfolio) {
        System.out.println("Portfolio: " + portfolio.getName());
        System.out.println("Total value (priced holdings): " + formatMoney(portfolio.getPricedTotalValue()));
        System.out.println("Unpriced holdings: " + portfolio.countUnpricedHoldings());
    }

    public void showBulkUpdateResult(Storage.BulkUpdateResult result) {
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
}
