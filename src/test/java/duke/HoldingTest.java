package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HoldingTest {
    @Test
    void addQuantity_withFees_updatesWeightedAverageBuyPrice() {
        Holding holding = new Holding(AssetType.STOCK, "VOO", 2, 100);

        holding.addQuantity(1, 130, 5);

        assertEquals(3.0, holding.getQuantity());
        assertEquals(111.66666666666667, holding.getAverageBuyPrice());
    }

    @Test
    void removeQuantity_withFees_deductsFeesFromRealizedPnl() {
        Holding holding = new Holding(AssetType.STOCK, "VOO", 2, 100);

        double realizedPnl = holding.removeQuantity(1, 130, 5);

        assertEquals(25.0, realizedPnl);
        assertEquals(1.0, holding.getQuantity());
        assertEquals(100.0, holding.getAverageBuyPrice());
    }

    @Test
    void removeQuantity_fullSell_resetsAverageBuyPrice() {
        Holding holding = new Holding(AssetType.STOCK, "VOO", 2, 100);

        double realizedPnl = holding.removeQuantity(2, 110, 4);

        assertEquals(16.0, realizedPnl);
        assertEquals(0.0, holding.getQuantity());
        assertEquals(0.0, holding.getAverageBuyPrice());
    }
}
