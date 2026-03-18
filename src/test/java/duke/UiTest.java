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
        assertTrue(output.contains("/setmany --file FILEPATH"));
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
        portfolio.addHolding(AssetType.STOCK, "AAPL", 2);
        portfolio.addHolding(AssetType.ETF, "QQQ", 3);
        portfolio.setPriceForTicker("AAPL", 100);

        ui.showHoldings(portfolio);

        String output = capturedOut.toString();
        assertTrue(output.contains("Portfolio: main"));
        assertTrue(output.contains("1 STOCK AAPL 2 100.00 200.00"));
        assertTrue(output.contains("2 ETF QQQ 3 - -"));
        assertTrue(output.contains("Total holdings: 2"));
        assertTrue(output.contains("Total value: 200.00"));
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
    void showPortfolioValue_printsPricedAndUnpricedCounts() {
        Ui ui = new Ui();
        Portfolio portfolio = new Portfolio("retirement");
        portfolio.addHolding(AssetType.BOND, "BND", 10);
        portfolio.addHolding(AssetType.STOCK, "MSFT", 1);
        portfolio.setPriceForTicker("MSFT", 300);

        ui.showPortfolioValue(portfolio);

        String output = capturedOut.toString();
        assertTrue(output.contains("Portfolio: retirement"));
        assertTrue(output.contains("Total value (priced holdings): 300.00"));
        assertTrue(output.contains("Unpriced holdings: 1"));
    }

    @Test
    void formatHelpers_roundAsExpected() {
        assertEquals("123.46", Ui.formatMoney(123.456));
        assertEquals("12.34", Ui.formatNumber(12.340000));
        assertEquals("1.234567890123", Ui.formatNumber(1.234567890123));
    }

}
