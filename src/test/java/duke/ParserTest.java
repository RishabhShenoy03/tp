package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ParserTest {
    private final Parser parser = new Parser();

    @Test
    void parseAdd_requiresPrice() {
        assertThrows(AppException.class, () -> parser.parse("/add --type STOCK --ticker VOO --qty 1"));
    }

    @Test
    void parseAdd_withPrice_parsesAllFields() throws AppException {
        ParsedCommand command = parser.parse("/add --type STOCK --ticker voo --qty 1 --price 300");

        assertEquals(CommandType.ADD, command.type());
        assertEquals(AssetType.STOCK, command.assetType());
        assertEquals("VOO", command.ticker());
        assertEquals(1.0, command.quantity());
        assertEquals(300.0, command.price());
        assertEquals(0.0, command.totalFees());
    }

    @Test
    void parseAdd_withFees_parsesFeeFields() throws AppException {
        ParsedCommand command = parser.parse(
                "/add --type STOCK --ticker voo --qty 1 --price 300 --brokerage 1.5 --fx 2 --platform 0.5");

        assertEquals(1.5, command.brokerageFee());
        assertEquals(2.0, command.fxFee());
        assertEquals(0.5, command.platformFee());
        assertEquals(4.0, command.totalFees());
    }

    @Test
    void parseAdd_withNegativeFee_throws() {
        assertThrows(AppException.class, () ->
                parser.parse("/add --type STOCK --ticker voo --qty 1 --price 300 --brokerage -1"));
    }

    @Test
    void parseAdd_withUnknownOption_throws() {
        assertThrows(AppException.class, () ->
                parser.parse("/add --type STOCK --ticker VOO --qty 1 --price 300 --unknown 5"));
    }

    @Test
    void parseAdd_withDuplicateOption_throws() {
        assertThrows(AppException.class, () ->
                parser.parse("/add --type STOCK --ticker VOO --ticker AAPL --qty 1 --price 300"));
    }

    @Test
    void parseAdd_withBlankTicker_throws() {
        assertThrows(AppException.class, () ->
                parser.parse("/add --type STOCK --ticker \"\" --qty 1 --price 300"));
    }

    @Test
    void parseAdd_withInvalidQuantity_throws() {
        assertThrows(AppException.class, () ->
                parser.parse("/add --type STOCK --ticker VOO --qty abc --price 300"));
    }

    @Test
    void parseRemove_withQtyAndPrice_parsesOptionalFields() throws AppException {
        ParsedCommand command = parser.parse(
                "/remove --type STOCK --ticker VOO --qty 0.5 --price 600 --brokerage 1 --fx 2 --platform 3");

        assertEquals(CommandType.REMOVE, command.type());
        assertEquals(0.5, command.quantity());
        assertEquals(600.0, command.price());
        assertEquals(6.0, command.totalFees());
    }

    @Test
    void parseRemove_withQtyOnly_parsesQtyAndNullPrice() throws AppException {
        ParsedCommand command = parser.parse("/remove --type STOCK --ticker VOO --qty 0.5");

        assertEquals(0.5, command.quantity());
        assertNull(command.price());
    }

    @Test
    void parseRemove_withPriceOnly_parsesPriceAndNullQty() throws AppException {
        ParsedCommand command = parser.parse("/remove --type STOCK --ticker VOO --price 600");

        assertNull(command.quantity());
        assertEquals(600.0, command.price());
    }

    @Test
    void parseRemove_withNeitherQtyNorPrice_parsesNullOptionals() throws AppException {
        ParsedCommand command = parser.parse("/remove --type STOCK --ticker VOO");

        assertNull(command.quantity());
        assertNull(command.price());
    }

    @Test
    void parseRemove_withNegativeFxFee_throws() {
        assertThrows(AppException.class, () ->
                parser.parse("/remove --type STOCK --ticker VOO --fx -2"));
    }

    @Test
    void parseSet_withRequiredOptions_parsesFields() throws AppException {
        ParsedCommand command = parser.parse("/set --type etf --ticker qqq --price 450");

        assertEquals(CommandType.SET, command.type());
        assertEquals(AssetType.ETF, command.assetType());
        assertEquals("QQQ", command.ticker());
        assertEquals(450.0, command.price());
    }

    @Test
    void parseSet_withoutType_parsesTickerLevelUpdate() throws AppException {
        ParsedCommand command = parser.parse("/set --ticker VOO --price 600");

        assertEquals(CommandType.SET, command.type());
        assertNull(command.assetType());
        assertEquals("VOO", command.ticker());
        assertEquals(600.0, command.price());
    }

    @Test
    void parseWatchList_parsesWatchListAction() throws AppException {
        ParsedCommand command = parser.parse("/watch list");

        assertEquals(CommandType.WATCH, command.type());
        assertEquals("list", command.name());
    }

    @Test
    void parseWatchAdd_withoutPrice_throws() {
        AppException ex = assertThrows(AppException.class, () ->
                parser.parse("/watch add --type stock --ticker voo"));
        assertTrue(ex.getMessage().contains("Missing required option: --price"));
    }

    @Test
    void parseWatchBuy_withPortfolio_parsesFields() throws AppException {
        ParsedCommand command = parser.parse("/watch buy --type etf --ticker qqq --qty 3 --portfolio retirement");

        assertEquals(CommandType.WATCH, command.type());
        assertEquals("buy", command.name());
        assertEquals(AssetType.ETF, command.assetType());
        assertEquals("QQQ", command.ticker());
        assertEquals(3.0, command.quantity());
        assertEquals("retirement", command.listTarget());
    }

    @Test
    void parseWatchBuy_missingQty_throws() {
        assertThrows(AppException.class, () ->
                parser.parse("/watch buy --type etf --ticker qqq --portfolio retirement"));
    }

    @Test
    void parseList_withStockFilter_parsesListTarget() throws AppException {
        ParsedCommand command = parser.parse("/list --stock");

        assertEquals(CommandType.LIST, command.type());
        assertEquals("--stock", command.listTarget());
    }

    @Test
    void parseList_withPortfoliosFlag_parsesListTarget() throws AppException {
        ParsedCommand command = parser.parse("/list --portfolios");

        assertEquals(CommandType.LIST, command.type());
        assertEquals("--portfolios", command.listTarget());
    }

    @Test
    void parseList_withHoldingsTarget_throws() {
        assertThrows(AppException.class, () -> parser.parse("/list holdings"));
    }

    @Test
    void parseList_withExtraTrailingInput_throws() {
        assertThrows(AppException.class, () -> parser.parse("/list --stock extra"));
    }

    @Test
    void parseCreate_withQuotedName_parsesName() throws AppException {
        ParsedCommand command = parser.parse("/create \"long term portfolio\"");

        assertEquals(CommandType.CREATE, command.type());
        assertEquals("long term portfolio", command.name());
    }

    @Test
    void parseCreate_withSlashPrefixedName_throws() {
        AppException ex = assertThrows(AppException.class, () -> parser.parse("/create /trading"));
        assertEquals("Portfolio name cannot start with '/'", ex.getMessage());
    }

    @Test
    void parseCreate_withBlankQuotedName_throws() {
        AppException ex = assertThrows(AppException.class, () -> parser.parse("/create \" \""));
        assertEquals("Portfolio name cannot be blank", ex.getMessage());
    }

    @Test
    void parseUse_withBlankQuotedName_throws() {
        AppException ex = assertThrows(AppException.class, () -> parser.parse("/use \" \""));
        assertEquals("Portfolio name cannot be blank", ex.getMessage());
    }

    @Test
    void parseCreate_withUnclosedQuote_throws() {
        assertThrows(AppException.class, () -> parser.parse("/create \"long term portfolio"));
    }

    @Test
    void parseHelp_withExtraInput_throws() {
        assertThrows(AppException.class, () -> parser.parse("/help now"));
    }

    @Test
    void parseInsights_withoutOptions_parsesInsightsType() throws AppException {
        ParsedCommand command = parser.parse("/insights");

        assertEquals(CommandType.INSIGHTS, command.type());
        assertNull(command.listTarget());
    }

    @Test
    void parseInsights_withOptions_storesRawOptions() throws AppException {
        ParsedCommand command = parser.parse("/insights --top 3 --chart");

        assertEquals(CommandType.INSIGHTS, command.type());
        assertEquals("--top 3 --chart", command.listTarget());
    }

    @Test
    void parseAdd_withMissingQtyValue_reportsCorrectOption() {
        AppException ex = assertThrows(AppException.class, () ->
                parser.parse("/add --type etf --ticker asd --qty --price 100"));
        assertEquals("Missing value for option: --qty", ex.getMessage());
    }

    @Test
    void parseAdd_withMissingQtyAndPriceValues_reportsFirstMissingOption() {
        AppException ex = assertThrows(AppException.class, () ->
                parser.parse("/add --type etf --ticker asd --qty --price"));
        assertEquals("Missing value for option: --qty", ex.getMessage());
    }

    @Test
    void parseAdd_withNaNQuantity_throws() {
        assertThrows(AppException.class, () ->
                parser.parse("/add --type stock --ticker VOO --qty NaN --price 100"));
    }

    @Test
    void parseAdd_withInfinityQuantity_throws() {
        assertThrows(AppException.class, () ->
                parser.parse("/add --type stock --ticker VOO --qty Infinity --price 100"));
    }

    @Test
    void parseAdd_withNaNPrice_throws() {
        assertThrows(AppException.class, () ->
                parser.parse("/add --type stock --ticker VOO --qty 1 --price NaN"));
    }

    @Test
    void parseAdd_withInfinityPrice_throws() {
        assertThrows(AppException.class, () ->
                parser.parse("/add --type stock --ticker VOO --qty 1 --price Infinity"));
    }

    @Test
    void parseAdd_withInfinityFee_throws() {
        assertThrows(AppException.class, () ->
                parser.parse("/add --type stock --ticker VOO --qty 1 --price 100 --brokerage Infinity"));
    }

    @Test
    void parseAdd_withMissingTickerValue_reportsTickerOption() {
        AppException ex = assertThrows(AppException.class, () ->
                parser.parse("/add --type 1 --ticker --qty 3"));
        assertEquals("Missing value for option: --ticker", ex.getMessage());
    }

    @Test
    void parseAdd_withMissingTypeValue_reportsTypeOption() {
        AppException ex = assertThrows(AppException.class, () ->
                parser.parse("/add --type --ticker AAPL --qty 3"));
        assertEquals("Missing value for option: --type", ex.getMessage());
    }
}
