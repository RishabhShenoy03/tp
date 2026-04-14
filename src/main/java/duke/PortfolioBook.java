//@@author Elegazia
package duke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PortfolioBook {
    private final Map<String, Portfolio> portfolios;
    private String activePortfolioKey;

    public PortfolioBook() {
        this.portfolios = new LinkedHashMap<>();
        this.activePortfolioKey = null;
    }

    public void createPortfolio(String name) throws AppException {
        String displayName = requireDisplayPortfolioName(name);
        assert displayName != null && !displayName.isBlank() : "displayName must not be null or blank";
        String portfolioKey = toPortfolioKey(displayName);
        if (portfolios.containsKey(portfolioKey)) {
            throw new AppException("Portfolio already exists: " + portfolios.get(portfolioKey).getName());
        }

        portfolios.put(portfolioKey, new Portfolio(displayName));
        assert portfolios.containsKey(portfolioKey) : "Created portfolio must be in the book";

        if (activePortfolioKey == null) {
            activePortfolioKey = portfolioKey;
            assert activePortfolioKey != null : "Active portfolio key must be set on first creation";
        }
    }

    public void ensurePortfolioExists(String name) throws AppException {
        String portfolioKey = requirePortfolioKey(name);
        if (!portfolios.containsKey(portfolioKey)) {
            createPortfolio(name);
        }
    }

    public void usePortfolio(String name) throws AppException {
        String displayName = requireDisplayPortfolioName(name);
        assert displayName != null && !displayName.isBlank() : "displayName must not be null or blank";
        String portfolioKey = toPortfolioKey(displayName);
        if (!portfolios.containsKey(portfolioKey)) {
            throw new AppException("Portfolio not found: " + displayName);
        }
        activePortfolioKey = portfolioKey;
        assert activePortfolioKey.equals(portfolioKey) : "Active portfolio key was not updated";
    }

    public boolean hasActivePortfolio() {
        return activePortfolioKey != null;
    }

    public Portfolio getActivePortfolio() throws AppException {
        if (activePortfolioKey == null) {
            throw new AppException("No active portfolio selected. Use: /use NAME");
        }
        return portfolios.get(activePortfolioKey);
    }

    public String getActivePortfolioName() {
        if (activePortfolioKey == null) {
            return null;
        }
        Portfolio active = portfolios.get(activePortfolioKey);
        return active == null ? null : active.getName();
    }

    public List<Portfolio> getPortfolios() {
        List<Portfolio> result = new ArrayList<>(portfolios.values());
        assert result != null : "getPortfolios result cannot be null";
        for (Portfolio p : result) {
            assert p != null : "Portfolios list should not contain null entries";
        }
        return result;
    }

    public Portfolio getPortfolio(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return portfolios.get(toPortfolioKey(name.trim()));
    }

    private String requireDisplayPortfolioName(String name) throws AppException {
        if (name == null || name.isBlank()) {
            throw new AppException("Portfolio name cannot be blank");
        }
        String trimmed = name.trim();
        if (trimmed.contains("|")) {
            throw new AppException("Portfolio name cannot contain '|'");
        }
        return trimmed;
    }

    private String requirePortfolioKey(String name) throws AppException {
        return toPortfolioKey(requireDisplayPortfolioName(name));
    }

    private String toPortfolioKey(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
