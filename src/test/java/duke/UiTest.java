package duke;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UiTest {
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream capturedOut;

    @BeforeEach
    void setUp() {
        capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void showHelp_printsAvailableCommands() {
        Ui ui = new Ui();

        ui.showHelp();

        String output = capturedOut.toString();
        assertTrue(output.contains("Available commands:"));
        assertTrue(output.contains("/create NAME"));
        assertTrue(output.contains("/list --stock"));
        assertTrue(output.contains("/list --portfolios"));
        assertTrue(output.contains("/watch remove --type TYPE --ticker TICKER"));
        assertTrue(output.contains("/watch list"));
        assertTrue(output.contains("/watch add --type TYPE --ticker TICKER --price PRICE"));
        assertTrue(output.contains("/watch buy --type TYPE --ticker TICKER --qty QTY --portfolio NAME"));
        assertTrue(output.contains("/set --ticker TICKER --price PRICE [--type TYPE]"));
        assertTrue(output.contains("/setmany --file FILEPATH"));
        assertTrue(output.contains("/insights [--type stock|etf|bond] [--top N] [--chart]"));
        assertTrue(output.contains("/exit"));
    }

    @Test
    void showPortfolios_marksActivePortfolio() throws AppException {
        Ui ui = new Ui();
        PortfolioBook book = new PortfolioBook();
        book.createPortfolio("growth");
        book.createPortfolio("income");
        book.usePortfolio("income");

        ui.showPortfolios(book);

        String output = capturedOut.toString();
        assertTrue(output.contains("Portfolios (2):"));
        assertTrue(output.contains("growth"));
        assertTrue(output.contains("income (active)"));
    }

    @Test
    void showHoldings_printsPriceAndTotals() {
        Ui ui = new Ui();
        Portfolio portfolio = new Portfolio("main");
        portfolio.addHolding(AssetType.STOCK, "AAPL", 2, 90, 0);
        portfolio.addHolding(AssetType.ETF, "QQQ", 3, 350, 0);
        portfolio.setPriceForTicker("AAPL", 100);

        ui.showHoldings(portfolio);

        String output = capturedOut.toString();
        assertTrue(output.contains("Portfolio: main"));
        assertTrue(output.contains("#   TYPE  TICKR"));
        assertTrue(output.contains("AAPL"));
        assertTrue(output.contains("QQQ"));
        assertTrue(output.contains("100.00"));
        assertTrue(output.contains("350.00"));
        assertTrue(output.contains("1050.00"));
        assertTrue(output.contains("Total holdings: 2"));
        assertTrue(output.contains("Total value: 1250.00"));
        assertTrue(output.contains("Note: VALUE = QTY x MKT_PRICE"));
    }

    @Test
    void showHoldings_withFilter_printsOnlyMatchingType() {
        Ui ui = new Ui();
        Portfolio portfolio = new Portfolio("main");
        portfolio.addHolding(AssetType.STOCK, "AAPL", 2, 90, 0);
        portfolio.addHolding(AssetType.ETF, "QQQ", 3, 350, 0);

        ui.showHoldings(portfolio, AssetType.STOCK);

        String output = capturedOut.toString();
        assertTrue(output.contains("Portfolio: main"));
        assertTrue(output.contains("AAPL"));
        assertTrue(output.contains("180.00"));
        assertTrue(!output.contains("QQQ"));
        assertTrue(output.contains("Total holdings: 1"));
        assertTrue(output.contains("Total value: 180.00"));
    }

    @Test
    void showPortfolioSummaries_sortsAlphabeticallyAndPrintsPnL() throws AppException {
        Ui ui = new Ui();
        PortfolioBook book = new PortfolioBook();
        book.createPortfolio("zeta");
        book.createPortfolio("alpha");

        Portfolio alpha = book.getPortfolio("alpha");
        alpha.addHolding(AssetType.STOCK, "VOO", 1, 300, 0);
        alpha.setPriceForTicker("VOO", 350);

        Portfolio zeta = book.getPortfolio("zeta");
        zeta.addHolding(AssetType.BOND, "BND", 2, 70, 0);

        ui.showPortfolioSummaries(book);

        String output = capturedOut.toString();
        assertTrue(output.contains("Portfolios (alphabetical):"));
        int alphaIdx = output.indexOf("alpha realized=+0.00 unrealized=+50.00");
        int zetaIdx = output.indexOf("zeta realized=+0.00 unrealized=+0.00");
        assertTrue(alphaIdx >= 0);
        assertTrue(zetaIdx > alphaIdx);
    }

    @Test
    void showPortfolioSummaries_withNoPortfolios_printsHelpfulMessage() {
        Ui ui = new Ui();
        PortfolioBook book = new PortfolioBook();

        ui.showPortfolioSummaries(book);

        String output = capturedOut.toString();
        assertTrue(output.contains("No portfolios available. Create one with /create NAME."));
        assertTrue(!output.contains("Portfolios (alphabetical):"));
    }

    @Test
    void showBulkUpdateResult_withFailures_printsFailureRows() {
        Ui ui = new Ui();
        Storage.BulkUpdateResult result = new Storage.BulkUpdateResult(
                1,
                2,
                List.of("line 2 - ticker: AAPL reason: price must be > 0")
        );

        ui.showBulkUpdateResult(result);

        String output = capturedOut.toString();
        assertTrue(output.contains("Updated prices: 1 succeeded, 2 failed"));
        assertTrue(output.contains("Failed rows:"));
        assertTrue(output.contains("line 2 - ticker: AAPL reason: price must be > 0"));
    }

    @Test
    void showWatchlist_printsItemsAndTotals() {
        Ui ui = new Ui();
        Watchlist watchlist = new Watchlist();
        watchlist.addItem(AssetType.STOCK, "VOO", 600.0);
        watchlist.addItem(AssetType.ETF, "QQQ", null);

        ui.showWatchlist(watchlist);

        String output = capturedOut.toString();
        assertTrue(output.contains("Watchlist:"));
        assertTrue(output.contains("1 STOCK VOO 600.00"));
        assertTrue(output.contains("2 ETF QQQ -"));
        assertTrue(output.contains("Total watchlist items: 2"));
    }

    @Test
    void showPortfolioValue_printsPricedAndUnpricedCounts() {
        Ui ui = new Ui();
        Portfolio portfolio = new Portfolio("retirement");
        portfolio.addHolding(AssetType.BOND, "BND", 10, 70, 0);
        portfolio.addHolding(AssetType.STOCK, "MSFT", 1, 250, 0);
        portfolio.setPriceForTicker("MSFT", 300);

        ui.showPortfolioValue(portfolio);

        String output = capturedOut.toString();
        assertTrue(output.contains("Portfolio: retirement"));
        assertTrue(output.contains("Current total value: 1000.00"));
        assertTrue(output.contains("Realized P&L: +0.00"));
        assertTrue(output.contains("Unrealized P&L by holding:"));
        assertTrue(output.contains("TICKR  TYPE"));
        assertTrue(output.contains("BND"));
        assertTrue(output.contains("BOND"));
        assertTrue(output.contains("MSFT"));
        assertTrue(output.contains("STOCK"));
        assertTrue(output.contains("+50.00"));
        assertTrue(output.contains("Total unrealized P&L: +50.00"));
    }

    @Test
    void showInsightsTable_withTopN_keepsLosersAndUsesFullSummary() {
        Ui ui = new Ui();
        Portfolio portfolio = new Portfolio("t1");
        portfolio.addHolding(AssetType.STOCK, "AAA", 2, 100, 0);
        portfolio.addHolding(AssetType.STOCK, "BBB", 1, 50, 0);
        portfolio.addHolding(AssetType.ETF, "CCC", 5, 20, 0);
        portfolio.addHolding(AssetType.BOND, "DDD", 3, 1000, 0);

        portfolio.setPriceForHolding(AssetType.STOCK, "AAA", 150);
        portfolio.setPriceForHolding(AssetType.STOCK, "BBB", 30);
        portfolio.setPriceForHolding(AssetType.ETF, "CCC", 25);
        portfolio.setPriceForHolding(AssetType.BOND, "DDD", 950);

        ui.showInsightsTable(portfolio, null, 10, false);

        String output = capturedOut.toString();
        assertTrue(output.contains("View: type=ALL, top=10"));
        assertTrue(output.contains("AAA"));
        assertTrue(output.contains("BBB"));
        assertTrue(output.contains("CCC"));
        assertTrue(output.contains("DDD"));
        assertTrue(output.contains("- Holdings: 4 (priced: 4, unpriced: 0)"));
        assertTrue(output.contains("- Open cost basis: 3350.00"));
        assertTrue(output.contains("- Unrealized P&L: -45.00 (-1.34%)"));
        assertTrue(output.contains("- Net P&L: -45.00"));
        assertTrue(output.contains("- Top contributor: AAA +100.00"));
        assertTrue(output.contains("- Top detractor: DDD -150.00"));
    }

    @Test
    void showInsightsTable_allPositiveHoldings_showsNoTopDetractor() {
        Ui ui = new Ui();
        Portfolio portfolio = new Portfolio("t1");
        portfolio.addHolding(AssetType.STOCK, "AAA", 1, 100, 0);
        portfolio.addHolding(AssetType.STOCK, "BBB", 1, 100, 0);

        portfolio.setPriceForHolding(AssetType.STOCK, "AAA", 110);
        portfolio.setPriceForHolding(AssetType.STOCK, "BBB", 105);

        ui.showInsightsTable(portfolio, null, null, false);

        String output = capturedOut.toString();
        assertTrue(output.contains("- Holdings: 2 (priced: 2, unpriced: 0)"));
        assertTrue(output.contains("- Top contributor: AAA +10.00"));
        assertTrue(output.contains("- Top detractor: none"));
    }

    @Test
    void formatHelpers_roundAsExpected() {
        assertEquals("123.46", Ui.formatMoney(123.456));
        assertEquals("12.34", Ui.formatNumber(12.340000));
        assertEquals("1.234567890123", Ui.formatNumber(1.234567890123));
        assertEquals("+12.30", Ui.formatSignedMoney(12.3));
        assertEquals("-12.30", Ui.formatSignedMoney(-12.3));
    }

}
