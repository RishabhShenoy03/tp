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
        case "set" -> parseSet(tokens);
        case "setmany" -> parseSetMany(tokens);
        case "value" -> new ParsedCommand(CommandType.VALUE, null, null, null, null, null,
                null, null, null, null, null);
        case "insights" -> parseInsights(tokens);
        case "help" -> new ParsedCommand(CommandType.HELP, null, null, null, null, null,
                null, null, null, null, null);
        case "exit" -> new ParsedCommand(CommandType.EXIT, null, null, null, null, null,
                null, null, null, null, null);
        default -> throw new AppException("Unknown command: " + commandWord);
        };
    }

    private ParsedCommand parseCreate(List<String> tokens) throws AppException {
        if (tokens.size() < 2) {
            throw new AppException("Usage: /create NAME");
        }
        String name = joinTail(tokens, 1);
        return new ParsedCommand(CommandType.CREATE, name, null, null, null, null,
                null, null, null, null, null);
    }

    private ParsedCommand parseUse(List<String> tokens) throws AppException {
        if (tokens.size() < 2) {
            throw new AppException("Usage: /use NAME");
        }
        String name = joinTail(tokens, 1);
        return new ParsedCommand(CommandType.USE, name, null, null, null, null,
                null, null, null, null, null);
    }

    private ParsedCommand parseList(List<String> tokens) throws AppException {
        if (tokens.size() == 1) {
            return new ParsedCommand(CommandType.LIST, null, null, null, null, null,
                    null, null, null, null, null);
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
        AssetType type = parseAssetType(requireOption(options, "--type"));
        String ticker = normaliseTicker(requireOption(options, "--ticker"));
        double qty = parsePositiveDouble(requireOption(options, "--qty"), "Quantity must be > 0");
        double price = parsePositiveDouble(requireOption(options, "--price"), "Price must be > 0");
        return new ParsedCommand(CommandType.ADD, null, type, ticker, qty, price,
                parseOptionalNonNegativeDouble(options.get("--brokerage"), "Brokerage fee must be >= 0"),
                parseOptionalNonNegativeDouble(options.get("--fx"), "FX fee must be >= 0"),
                parseOptionalNonNegativeDouble(options.get("--platform"), "Platform fee must be >= 0"),
                null, null);
    }

    private ParsedCommand parseRemove(List<String> tokens) throws AppException {
        Map<String, String> options = parseOptions(tokens);
        AssetType type = parseAssetType(requireOption(options, "--type"));
        String ticker = normaliseTicker(requireOption(options, "--ticker"));
        Double qty = parseOptionalPositiveDouble(options.get("--qty"), "Quantity must be > 0");
        Double price = parseOptionalPositiveDouble(options.get("--price"), "Price must be > 0");
        return new ParsedCommand(CommandType.REMOVE, null, type, ticker, qty, price,
                parseOptionalNonNegativeDouble(options.get("--brokerage"), "Brokerage fee must be >= 0"),
                parseOptionalNonNegativeDouble(options.get("--fx"), "FX fee must be >= 0"),
                parseOptionalNonNegativeDouble(options.get("--platform"), "Platform fee must be >= 0"),
                null, null);
    }

    private ParsedCommand parseSet(List<String> tokens) throws AppException {
        Map<String, String> options = parseOptions(tokens);
        String ticker = normaliseTicker(requireOption(options, "--ticker"));
        double price = parsePositiveDouble(requireOption(options, "--price"), "Price must be > 0");
        return new ParsedCommand(CommandType.SET, null, null, ticker, null, price,
                null, null, null, null, null);
    }

    private ParsedCommand parseSetMany(List<String> tokens) throws AppException {
        Map<String, String> options = parseOptions(tokens);
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

    private Map<String, String> parseOptions(List<String> tokens) throws AppException {
        Map<String, String> options = new HashMap<>();

        for (int i = 1; i < tokens.size(); i += 2) {
            if (i + 1 >= tokens.size()) {
                throw new AppException("Missing value for option: " + tokens.get(i));
            }
            String key = tokens.get(i);
            String value = tokens.get(i + 1);
            options.put(key.toLowerCase(), value);
        }

        return options;
    }

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
            if (value <= 0) {
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

    private Double parseOptionalNonNegativeDouble(String rawValue, String errorMessage) throws AppException {
        if (rawValue == null) {
            return null;
        }

        try {
            double value = Double.parseDouble(rawValue);
            if (value < 0) {
                throw new AppException(errorMessage);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new AppException(errorMessage);
        }
    }

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

        return tokens;
    }
}
