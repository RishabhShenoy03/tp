package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class PortfolioTest {
    @Test
    void addHolding_recomputesWeightedAverageCost() {
        Portfolio portfolio = new Portfolio("demo");

        portfolio.addHolding(AssetType.STOCK, "VOO", 1, 300, 0);
        portfolio.addHolding(AssetType.STOCK, "VOO", 1, 500, 0);

        Holding holding = portfolio.getHolding(AssetType.STOCK, "VOO");
        assertEquals(2.0, holding.getQuantity());
        assertEquals(400.0, holding.getAverageBuyPrice());
    }

    @Test
    void addHolding_withFees_includesFeesInAverageCost() {
        Portfolio portfolio = new Portfolio("demo");

        portfolio.addHolding(AssetType.STOCK, "VOO", 2, 100, 10);

        Holding holding = portfolio.getHolding(AssetType.STOCK, "VOO");
        assertEquals(105.0, holding.getAverageBuyPrice());
    }

    @Test
    void removeHolding_partialSell_updatesRealizedPnl() {
        Portfolio portfolio = new Portfolio("demo");
        portfolio.addHolding(AssetType.STOCK, "VOO", 2, 400, 0);

        Portfolio.RemoveResult result = portfolio.removeHolding(AssetType.STOCK, "VOO", 0.5, 600.0, 0);

        assertEquals(0.5, result.soldQuantity());
        assertEquals(600.0, result.soldPrice());
        assertEquals(100.0, result.realizedPnl());
        assertEquals(100.0, portfolio.getTotalRealizedPnl());
        assertEquals(1.5, portfolio.getHolding(AssetType.STOCK, "VOO").getQuantity());
    }

    @Test
    void removeHolding_withoutPrice_usesLastSetPrice() {
        Portfolio portfolio = new Portfolio("demo");
        portfolio.addHolding(AssetType.STOCK, "VOO", 2, 400, 0);
        portfolio.setPriceForTicker("VOO", 600);

        Portfolio.RemoveResult result = portfolio.removeHolding(AssetType.STOCK, "VOO", 1.0, null, 0);

        assertEquals(200.0, result.realizedPnl());
        assertEquals(200.0, portfolio.getTotalRealizedPnl());
    }

    @Test
    void removeHolding_withoutAnyPrice_usesInitialAddPrice() {
        Portfolio portfolio = new Portfolio("demo");
        portfolio.addHolding(AssetType.STOCK, "VOO", 2, 400, 0);

        Portfolio.RemoveResult result = portfolio.removeHolding(AssetType.STOCK, "VOO", 1.0, null, 0);
        assertEquals(400.0, result.soldPrice());
        assertEquals(0.0, result.realizedPnl());
    }

    @Test
    void removeHolding_withFees_reducesRealizedPnl() {
        Portfolio portfolio = new Portfolio("demo");
        portfolio.addHolding(AssetType.STOCK, "VOO", 2, 400, 20);

        Portfolio.RemoveResult result = portfolio.removeHolding(AssetType.STOCK, "VOO", 1.0, 600.0, 15);

        assertEquals(175.0, result.realizedPnl());
        assertEquals(175.0, portfolio.getTotalRealizedPnl());
        assertEquals(15.0, result.fees());
    }

    @Test
    void removeHolding_fullSell_removesHoldingFromPortfolio() {
        Portfolio portfolio = new Portfolio("demo");
        portfolio.addHolding(AssetType.STOCK, "VOO", 2, 400, 0);

        portfolio.removeHolding(AssetType.STOCK, "VOO", 2.0, 450.0, 0);

        assertFalse(portfolio.hasHolding(AssetType.STOCK, "VOO"));
    }

    @Test
    void addHolding_existingHoldingWithFees_recomputesAverageCostAcrossTrades() {
        Portfolio portfolio = new Portfolio("demo");

        portfolio.addHolding(AssetType.STOCK, "VOO", 2, 100, 10);
        portfolio.addHolding(AssetType.STOCK, "VOO", 1, 130, 5);

        Holding holding = portfolio.getHolding(AssetType.STOCK, "VOO");
        assertEquals(3.0, holding.getQuantity());
        assertEquals(115.0, holding.getAverageBuyPrice());
    }

    @Test
    void getCurrentTotalValue_sumsQuantityTimesUnitPriceAcrossHoldings() {
        Portfolio portfolio = new Portfolio("demo");
        portfolio.addHolding(AssetType.STOCK, "VOO", 1.5, 320, 0);
        portfolio.addHolding(AssetType.ETF, "QQQ", 2, 400, 0);
        portfolio.setPriceForTicker("VOO", 600);

        assertEquals(1700.0, portfolio.getCurrentTotalValue());
    }
}
