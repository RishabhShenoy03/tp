package duke;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class StorageTest {
    @Test
    void load_emptyFile_returnsEmptyPortfolioBook(@TempDir Path tempDir) {
        Path file = tempDir.resolve("data.txt");
        Storage storage = new Storage(file.toString());

        PortfolioBook book = storage.load();

        assertTrue(book.getPortfolios().isEmpty());
        assertFalse(book.hasActivePortfolio());
    }

    @Test
    void save_thenLoad_restoresPortfolioData(@TempDir Path tempDir) {
        Path file = tempDir.resolve("data.txt");
        Storage storage = new Storage(file.toString());

        PortfolioBook book = new PortfolioBook();
        book.createPortfolio("tech");
        book.usePortfolio("tech");

        Portfolio portfolio = book.getActivePortfolio();
        portfolio.addHolding(AssetType.STOCK, "AAPL", 10);

        storage.save(book);

        PortfolioBook loaded = storage.load();

        assertEquals("tech", loaded.getActivePortfolioName());
        Portfolio loadedPortfolio = loaded.getPortfolio("tech");

        assertNotNull(loadedPortfolio);
        assertTrue(loadedPortfolio.hasHolding(AssetType.STOCK, "AAPL"));
    }

    @Test
    void loadPriceUpdates_validCsv_updatesPrices(@TempDir Path tempDir) throws Exception {
        Path storageFile = tempDir.resolve("data.txt");
        Storage storage = new Storage(storageFile.toString());

        Portfolio portfolio = new Portfolio("test");
        portfolio.addHolding(AssetType.STOCK, "AAPL", 5);

        Path csv = tempDir.resolve("prices.csv");

        Files.write(csv, List.of(
                "ticker,price",
                "AAPL,200"
        ));

        Storage.BulkUpdateResult result = storage.loadPriceUpdates(csv, portfolio);

        assertEquals(1, result.successCount());
        assertEquals(0, result.failedCount());

        Holding holding = portfolio.getHolding(AssetType.STOCK, "AAPL");
        assertEquals(200, holding.getLastPrice());
    }

    @Test
    void loadPriceUpdates_invalidRow_recordsFailure(@TempDir Path tempDir) throws Exception {
        Path storageFile = tempDir.resolve("data.txt");
        Storage storage = new Storage(storageFile.toString());

        Portfolio portfolio = new Portfolio("test");
        portfolio.addHolding(AssetType.STOCK, "AAPL", 5);

        Path csv = tempDir.resolve("prices.csv");

        Files.write(csv, List.of(
                "ticker,price",
                "AAPL,abc"
        ));

        Storage.BulkUpdateResult result = storage.loadPriceUpdates(csv, portfolio);

        assertEquals(0, result.successCount());
        assertEquals(1, result.failedCount());
    }

    @Test
    void loadPriceUpdates_missingFile_throwsException(@TempDir Path tempDir) {
        Storage storage = new Storage(tempDir.resolve("data.txt").toString());

        Portfolio portfolio = new Portfolio("test");

        Path missing = tempDir.resolve("missing.csv");

        assertThrows(IllegalArgumentException.class, () ->
                storage.loadPriceUpdates(missing, portfolio)
        );
    }
}
