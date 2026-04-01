# User Guide

## Introduction

CG2 Stocks Tracker is a command-line portfolio tracker for recording holdings, updating market prices, and viewing realized/unrealized P&L.

Prices are entered manually via `/set` or `/setmany` (there is no automatic live market feed). The app also supports a watchlist for assets you plan to buy but do not yet own.

Application data, including portfolios, holdings, watchlist items, average buy prices, latest saved prices, and realized P&L, is automatically saved and restored across restarts.

## Quick Start

1. Ensure Java 17 or above is installed.
2. Build and run from the project root:

```bash
# Windows
gradlew.bat run

# macOS/Linux
./gradlew run
```

Use `/help` in the app to list all commands.

## Features

### Create Portfolio: `/create`

Creates a portfolio.

**Format:** `/create NAME`

**Example:** `/create retirement`

### Switch Active Portfolio: `/use`

Switches the active portfolio.

**Format:** `/use NAME`

**Example:** `/use retirement`

### List Portfolios/Holdings: `/list`

**Formats:**

- `/list`
- `/list --stock`
- `/list --etf`
- `/list --bond`
- `/list --portfolios`

**Notes:**

- `/list` shows all holdings in the active portfolio.
- Type filters show only that asset type in the same tabular format.
- `/list --portfolios` shows each portfolio's realized and unrealised P&L in alphabetical order.

### Add Holding: `/add`

Adds units to a holding and requires purchase price per unit.

**Format:** `/add --type TYPE --ticker TICKER --qty QTY --price PRICE`

**Notes:**

- `TYPE` must be one of `STOCK`, `ETF`, `BOND`.
- `QTY > 0`, `PRICE > 0`.
- Optional fee fields: `--brokerage FEE`, `--fx FEE`, `--platform FEE`.
- Fee values (when provided) must be `>= 0`.
- If holding already exists, quantity is increased and average buy price is recalculated using weighted average.

**Example:** `/add --type STOCK --ticker VOO --qty 1 --price 300`

**Example with fees:**

`/add --type STOCK --ticker VOO --qty 1 --price 300 --brokerage 1.50 --fx 2.00 --platform 0.50`

**Output contains:**

- Added/updated holding details.
- Quantity.
- Average buy price.

### Remove Holding Units (Partial or Full): `/remove`

Sells part or all of a holding and records realized P&L.

**Format:** `/remove --type TYPE --ticker TICKER [--qty QTY] [--price PRICE]`

**Optional behavior:**

- If `--qty` is omitted: sell all units of the holding.
- If `--price` is omitted: use the holding's saved price (from `/set` or the holding's stored price).
- If both are omitted: sell all units using the holding's saved price.
- Optional fee fields: `--brokerage FEE`, `--fx FEE`, `--platform FEE`.

**Notes:**

- If no saved price exists, provide `--price`.
- `QTY` (when provided) must be `> 0` and not exceed current holding quantity.
- Fee values (when provided) must be `>= 0`.
- If `/set` was never used for a holding, remove can still use the holding's stored price.

**Examples:**

- Sell specific qty at explicit price:
  `/remove --type STOCK --ticker VOO --qty 0.5 --price 620`
- Sell specific qty at last `/set` price:
  `/remove --type STOCK --ticker VOO --qty 0.5`
- Sell all at explicit price:
  `/remove --type STOCK --ticker VOO --price 590`
- Sell all at last `/set` price:
  `/remove --type STOCK --ticker VOO`
- Sell with fees:
  `/remove --type STOCK --ticker VOO --qty 0.5 --price 620 --brokerage 1.50 --platform 0.50`

**Output contains:**

- Sold quantity.
- Effective sell price used.
- Total fees used for that sale (when non-zero).
- Realized P&L for that sale (signed `+` / `-`).

### Set Market Price: `/set`

Sets the latest market price for holdings. This does not sell anything.

**Formats:**

- `/set --ticker TICKER --price PRICE`
- `/set --type TYPE --ticker TICKER --price PRICE`

**Notes:**

- `PRICE > 0`.
- Without `--type`: updates all holdings with matching ticker in the active portfolio.
- With `--type`: updates only the matching `TYPE + TICKER` holding.
- If `--type` is provided, `TYPE` must be one of `STOCK`, `ETF`, `BOND`.
- Used by `/value` for unrealized P&L and by `/remove` as fallback sell price.
- Latest saved prices are restored after restarting the application.

**Example:** `/set --type STOCK --ticker VOO --price 600`

**Output contains:**

- Price update confirmation only.

### Manage Watchlist: `/watch`

Tracks assets you may want to buy later.

**Formats:**

- `/watch add --type TYPE --ticker TICKER [--price PRICE]`
- `/watch remove --type TYPE --ticker TICKER`
- `/watch list`
- `/watch buy --type TYPE --ticker TICKER --portfolio PORTFOLIO_NAME`

**Notes:**

- `TYPE` must be one of `STOCK`, `ETF`, `BOND`.
- `/watch add` can store an item with or without a price.
- `/watch buy` only works if that watchlist item already has a price.
- `/watch buy` requires `--portfolio` and buys into that portfolio.
- `/watch buy` buys 1 unit at the watchlist price and removes that item from the watchlist.

### Bulk Set Prices from CSV: `/setmany`

Loads prices from CSV into active portfolio.

**Format:** `/setmany --file FILEPATH`

CSV header must be: `ticker,price`

**Notes:**

- Header matching is case-insensitive.
- Invalid rows are reported as failed rows; valid rows are still processed.

### View Valuation and P&L: `/value`

Shows portfolio-level current total value, realized P&L, and unrealized P&L by holding.

**Format:** `/value`

**Output contains:**

- Current total value: sum of `quantity * current unit price` across holdings in the active portfolio.
- Realized P&L: cumulative P&L from completed sells (positive or negative).
- Unrealised P&L by holding: one row per holding.
- Total unrealised P&L: sum across holdings.

**Example scenario:**

```text
/add --type STOCK --ticker VOO --qty 1 --price 300
/set --type STOCK --ticker VOO --price 600
/value
```

**Expected result summary:**

- Realized P&L = `+0.00` (nothing sold yet)
- Unrealized P&L for VOO = `+300.00`
- Total unrealized P&L = `+300.00`

Unrealized P&L is calculated using:

`(last price - average buy price) × quantity`

All values remain consistent after restarting the application because the required storage fields are saved and restored.

### Insights View: `/insights`

Shows per-holding insights (including optional filters and top gainers).

**Format:** `/insights [--type stock|etf|bond] [--top N] [--chart]`

**Notes:**

- `--top N` shows top gainers only (holdings with positive unrealized P&L).
- `--chart` adds an ASCII diverging chart (loss on the left, gain on the right).
- Chart bars are scaled by unrealized percentage, not raw dollar P&L.
- Chart scale is relative to the largest absolute unrealized percentage in the current view.
- Invalid or duplicate options are rejected with a usage/error message.

**Examples:**

- `/insights`
- `/insights --type stock`
- `/insights --top 5`
- `/insights --type etf --top 3 --chart`

### Help: `/help`

Shows the command list.

### Exit: `/exit`

Exits the application.

## FAQ

**Q: Why does `/remove` fail when I omit `--price`?**

A: If you omit `--price`, the app uses the holding's saved price. If no saved price is available, provide `--price`.

**Q: How is average buy price calculated?**

A: Weighted average cost basis is used. If fees are provided for `/add`, they are included in purchase cost before the new average is computed.

**Q: Will my data persist after I exit the app?**

A: Yes. Portfolios, holdings, average buy prices, latest saved prices, and realized P&L are automatically saved and restored when the application restarts.

**Q: What happens to older save files?**

A: Older save files are supported. Legacy holding rows can still be loaded.

**Q: Why can a smaller dollar gain look larger in the `/insights --chart` output?**

A: The chart is percentage-based and normalized to the largest absolute unrealized percentage in the current view. This is intentional so positions of different sizes remain comparable in relative return terms.

## Command Summary

- `/create NAME`
- `/use NAME`
- `/list`
- `/list --stock`
- `/list --etf`
- `/list --bond`
- `/list --portfolios`
- `/add --type TYPE --ticker TICKER --qty QTY --price PRICE [--brokerage FEE] [--fx FEE] [--platform FEE]`
- `/remove --type TYPE --ticker TICKER [--qty QTY] [--price PRICE] [--brokerage FEE] [--fx FEE] [--platform FEE]`
- `/watch add --type TYPE --ticker TICKER [--price PRICE]`
- `/watch remove --type TYPE --ticker TICKER`
- `/watch list`
- `/watch buy --type TYPE --ticker TICKER --portfolio PORTFOLIO_NAME`
- `/set --ticker TICKER --price PRICE [--type TYPE]`
- `/setmany --file FILEPATH`
- `/value`
- `/insights [--type stock|etf|bond] [--top N] [--chart]`
- `/help`
- `/exit`
