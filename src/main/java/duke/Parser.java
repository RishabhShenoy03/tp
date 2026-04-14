//@@author RishabhShenoy03
package duke;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    public ParsedCommand parse(String input) throws AppException {
        if (input == null || input.isBlank()) {
            throw new AppException("Please enter a command.");
        }

        if (!input.startsWith("/")) {
            throw new AppException("Commands must start with '/'.");
        }

        List<String> tokens = tokenise(input);
        if (tokens.isEmpty()) {
            throw new AppException("Please enter a command.");
        }

        String commandWord = tokens.get(0).substring(1).toLowerCase();

        return switch (commandWord) {
        case "create" -> parseCreate(tokens);
        case "use" -> parseUse(tokens);
        case "list" -> parseList(tokens);
        case "add" -> parseAdd(tokens);
        case "remove" -> parseRemove(tokens);
        case "watch" -> parseWatch(tokens);
        case "set" -> parseSet(tokens);
        case "setmany" -> parseSetMany(tokens);
        case "value" -> parseNoArgCommand(tokens, CommandType.VALUE);
        case "insights" -> parseInsights(tokens);
        case "help" -> parseNoArgCommand(tokens, CommandType.HELP);
        case "exit" -> parseNoArgCommand(tokens, CommandType.EXIT);
        default -> throw new AppException("Unknown command: " + commandWord);
        };
    }

    private ParsedCommand parseCreate(List<String> tokens) throws AppException {
        assert tokens != null && !tokens.isEmpty() : "tokens must not be null or empty";
        if (tokens.size() < 2) {
            throw new AppException("Usage: /create NAME");
        }
        String name = joinTail(tokens, 1).strip();
        assert name != null : "portfolio name must not be null after joining";
        validatePortfolioName(name);
        ParsedCommand result = new ParsedCommand(CommandType.CREATE, name, null, null, null, null,
                null, null, null, null, null);
        assert result.type() == CommandType.CREATE : "Command type must be CREATE";
        assert result.name().equals(name) : "Command name must match provided name";
        return result;
    }

    private ParsedCommand parseUse(List<String> tokens) throws AppException {
        assert tokens != null && !tokens.isEmpty() : "tokens must not be null or empty";
        if (tokens.size() < 2) {
            throw new AppException("Usage: /use NAME");
        }
        String name = joinTail(tokens, 1).strip();
        assert name != null : "portfolio name must not be null after joining";
        validatePortfolioName(name);
        ParsedCommand result = new ParsedCommand(CommandType.USE, name, null, null, null, null,
                null, null, null, null, null);
        assert result.type() == CommandType.USE : "Command type must be USE";
        assert result.name().equals(name) : "Command name must match provided name";
        return result;
    }

    private void validatePortfolioName(String name) throws AppException {
        if (name == null || name.isBlank()) {
            throw new AppException("Portfolio name must not be blank");
        }
        if (name.startsWith("/")) {
            throw new AppException("Portfolio name cannot start with '/'");
        }
    }

    private ParsedCommand parseList(List<String> tokens) throws AppException {
        if (tokens.size() == 1) {
            return new ParsedCommand(CommandType.LIST, null, null, null, null, null,
                    null, null, null, null, null);
        }

        if (tokens.size() != 2) {
            throw new AppException("Usage: /list or /list --stock or /list --etf or /list --bond "
                    + "or /list --portfolios");
        }

        String target = tokens.get(1).toLowerCase();
        if (target.equals("--stock") || target.equals("--etf") || target.equals("--bond")) {
            return new ParsedCommand(CommandType.LIST, null, null, null, null, null,
                    null, null, null, target, null);
        }

        if (!target.equals("--portfolios")) {
            throw new AppException("Usage: /list or /list --stock or /list --etf or /list --bond "
                    + "or /list --portfolios");
        }

        return new ParsedCommand(CommandType.LIST, null, null, null, null, null,
                null, null, null, "--portfolios", null);
    }

    private ParsedCommand parseAdd(List<String> tokens) throws AppException {
        Map<String, String> options = parseOptions(tokens);
        //@@author zenweilow
        validateAllowedOptions(options, "--type", "--ticker", "--qty", "--price", "--brokerage", "--fx", "--platform");
        //@@author RishabhShenoy03
        AssetType type = parseAssetType(requireOption(options, "--type"));
        String ticker = normaliseTicker(requireOption(options, "--ticker"));
        double qty = parsePositiveDouble(requireOption(options, "--qty"), "Quantity must be > 0");
        double price = parsePositiveDouble(requireOption(options, "--price"), "Price must be > 0");
        //@@author zenweilow
        return new ParsedCommand(CommandType.ADD, null, type, ticker, qty, price,
                parseOptionalNonNegativeDouble(options.get("--brokerage"), "Brokerage fee must be >= 0"),
                parseOptionalNonNegativeDouble(options.get("--fx"), "FX fee must be >= 0"),
                parseOptionalNonNegativeDouble(options.get("--platform"), "Platform fee must be >= 0"),
                null, null);
        //@@author RishabhShenoy03
    }

    private ParsedCommand parseRemove(List<String> tokens) throws AppException {
        Map<String, String> options = parseOptions(tokens);
        //@@author zenweilow
        validateAllowedOptions(options, "--type", "--ticker", "--qty", "--price", "--brokerage", "--fx", "--platform");
        //@@author RishabhShenoy03
        AssetType type = parseAssetType(requireOption(options, "--type"));
        String ticker = normaliseTicker(requireOption(options, "--ticker"));
        Double qty = parseOptionalPositiveDouble(options.get("--qty"), "Quantity must be > 0");
        Double price = parseOptionalPositiveDouble(options.get("--price"), "Price must be > 0");
        //@@author zenweilow
        return new ParsedCommand(CommandType.REMOVE, null, type, ticker, qty, price,
                parseOptionalNonNegativeDouble(options.get("--brokerage"), "Brokerage fee must be >= 0"),
                parseOptionalNonNegativeDouble(options.get("--fx"), "FX fee must be >= 0"),
                parseOptionalNonNegativeDouble(options.get("--platform"), "Platform fee must be >= 0"),
                null, null);
        //@@author RishabhShenoy03
    }

    private ParsedCommand parseSet(List<String> tokens) throws AppException {
        Map<String, String> options = parseOptions(tokens);
        validateAllowedOptions(options, "--type", "--ticker", "--price");
        AssetType type = options.containsKey("--type") ? parseAssetType(requireOption(options, "--type")) : null;
        String ticker = normaliseTicker(requireOption(options, "--ticker"));
        double price = parsePositiveDouble(requireOption(options, "--price"), "Price must be > 0");
        return new ParsedCommand(CommandType.SET, null, type, ticker, null, price,
                null, null, null, null, null);
    }

    private ParsedCommand parseWatch(List<String> tokens) throws AppException {
        if (tokens.size() < 2) {
            throw new AppException("Usage: /watch add|remove|list|buy ...");
        }

        String action = tokens.get(1).toLowerCase();
        return switch (action) {
        case "list" -> parseWatchList(tokens);
        case "add" -> parseWatchAdd(tokens);
        case "remove" -> parseWatchRemove(tokens);
        case "buy" -> parseWatchBuy(tokens);
        default -> throw new AppException("Unknown watch action: " + action
                + "\nUsage: /watch add|remove|list|buy ...");
        };
    }

    private ParsedCommand parseWatchList(List<String> tokens) throws AppException {
        if (tokens.size() != 2) {
            throw new AppException("Usage: /watch list");
        }
        return new ParsedCommand(CommandType.WATCH, "list", null, null, null, null,
                null, null, null, null, null);
    }

    private ParsedCommand parseWatchAdd(List<String> tokens) throws AppException {
        Map<String, String> options = parseOptions(tokens, 2);
        validateAllowedOptions(options, "--type", "--ticker", "--price");
        AssetType type = parseAssetType(requireOption(options, "--type"));
        String ticker = normaliseTicker(requireOption(options, "--ticker"));
        double price = parsePositiveDouble(requireOption(options, "--price"), "Price must be > 0");
        return new ParsedCommand(CommandType.WATCH, "add", type, ticker, null, price,
                null, null, null, null, null);
    }

    private ParsedCommand parseWatchRemove(List<String> tokens) throws AppException {
        Map<String, String> options = parseOptions(tokens, 2);
        validateAllowedOptions(options, "--type", "--ticker");
        AssetType type = parseAssetType(requireOption(options, "--type"));
        String ticker = normaliseTicker(requireOption(options, "--ticker"));
        return new ParsedCommand(CommandType.WATCH, "remove", type, ticker, null, null,
                null, null, null, null, null);
    }

    private ParsedCommand parseWatchBuy(List<String> tokens) throws AppException {
        Map<String, String> options = parseOptions(tokens, 2);
        validateAllowedOptions(options, "--type", "--ticker", "--portfolio", "--qty");
        AssetType type = parseAssetType(requireOption(options, "--type"));
        String ticker = normaliseTicker(requireOption(options, "--ticker"));
        String portfolioName = requireOption(options, "--portfolio");
        double quantity = parsePositiveDouble(requireOption(options, "--qty"), "Quantity must be > 0");
        return new ParsedCommand(CommandType.WATCH, "buy", type, ticker, quantity, null,
                null, null, null, portfolioName, null);
    }

    private ParsedCommand parseSetMany(List<String> tokens) throws AppException {
        Map<String, String> options = parseOptions(tokens);
        validateAllowedOptions(options, "--file");
        String file = requireOption(options, "--file");
        Path filePath = Paths.get(file);
        return new ParsedCommand(CommandType.SET_MANY, null, null, null, null, null,
                null, null, null, null, filePath);
    }

    private ParsedCommand parseInsights(List<String> tokens) {
        String rawOptions = (tokens.size() > 1) ? joinTail(tokens, 1) : null;
        return new ParsedCommand(CommandType.INSIGHTS, null, null, null, null, null,
                null, null, null, rawOptions, null);
    }

    //@@author zenweilow
    private ParsedCommand parseNoArgCommand(List<String> tokens, CommandType commandType) throws AppException {
        if (tokens.size() != 1) {
            throw new AppException("Usage: /" + commandType.name().toLowerCase());
        }
        return new ParsedCommand(commandType, null, null, null, null, null,
                null, null, null, null, null);
    }

    //@@author RishabhShenoy03
    private Map<String, String> parseOptions(List<String> tokens) throws AppException {
        return parseOptions(tokens, 1);
    }

    private Map<String, String> parseOptions(List<String> tokens, int startIndex) throws AppException {
        Map<String, String> options = new HashMap<>();

        for (int i = startIndex; i < tokens.size();) {
            String key = tokens.get(i);
            if (!key.startsWith("--")) {
                throw new AppException("Invalid option: " + key);
            }
            if (i + 1 >= tokens.size()) {
                throw new AppException("Missing value for option: " + key);
            }

            String value = tokens.get(i + 1);
            if (value.startsWith("--") || value.isBlank()) {
                throw new AppException("Missing value for option: " + key);
            }
            String normalisedKey = key.toLowerCase();
            if (options.containsKey(normalisedKey)) {
                throw new AppException("Duplicate option: " + normalisedKey);
            }
            options.put(normalisedKey, value);
            i += 2;
        }

        return options;
    }

    //@@author zenweilow
    private void validateAllowedOptions(Map<String, String> options, String... allowedKeys) throws AppException {
        List<String> allowed = List.of(allowedKeys);
        for (String key : options.keySet()) {
            if (!allowed.contains(key)) {
                throw new AppException("Unknown option: " + key);
            }
        }
    }

    //@@author RishabhShenoy03
    private String requireOption(Map<String, String> options, String key) throws AppException {
        String value = options.get(key);
        if (value == null || value.isBlank()) {
            throw new AppException("Missing required option: " + key);
        }
        return value;
    }

    private double parsePositiveDouble(String rawValue, String errorMessage) throws AppException {
        try {
            double value = Double.parseDouble(rawValue);
            if (!Double.isFinite(value) || value <= 0) {
                throw new AppException(errorMessage);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new AppException(errorMessage);
        }
    }

    private Double parseOptionalPositiveDouble(String rawValue, String errorMessage) throws AppException {
        if (rawValue == null) {
            return null;
        }
        return parsePositiveDouble(rawValue, errorMessage);
    }

    //@@author zenweilow
    private Double parseOptionalNonNegativeDouble(String rawValue, String errorMessage) throws AppException {
        if (rawValue == null) {
            return null;
        }

        try {
            double value = Double.parseDouble(rawValue);
            if (!Double.isFinite(value) || value < 0) {
                throw new AppException(errorMessage);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new AppException(errorMessage);
        }
    }

    //@@author RishabhShenoy03
    private AssetType parseAssetType(String rawValue) throws AppException {
        try {
            return AssetType.fromString(rawValue);
        } catch (IllegalArgumentException e) {
            throw new AppException(e.getMessage());
        }
    }

    private String normaliseTicker(String rawTicker) {
        return rawTicker.toUpperCase();
    }

    private String joinTail(List<String> tokens, int startIndex) {
        return String.join(" ", tokens.subList(startIndex, tokens.size()));
    }

    private List<String> tokenise(String input) throws AppException {
        assert input != null : "input must not be null";
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            if (ch == '"') {
                inQuotes = !inQuotes;
                continue;
            }

            if (Character.isWhitespace(ch) && !inQuotes) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }

            current.append(ch);
        }

        if (inQuotes) {
            throw new AppException("Unclosed double quote in command.");
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        assert tokens != null : "tokenise result cannot be null";
        return tokens;
    }
}
