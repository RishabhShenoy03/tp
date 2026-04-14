# User Guide

## Introduction

CG2StocksTracker is a command-line portfolio tracker for amateur investors who prefer typing commands.

It helps you:

- create and switch portfolios
- track holdings across stock, ETF, and bond assets
- update market prices manually
- monitor realized and unrealized profit/loss
- keep a watchlist for planned buys

It gives you a clear view of your holdings without spreadsheets or large trading platforms.

## Quick Start

1. Ensure Java 17 or above is installed.
2. Download the latest `.jar` file from the project's Releases page.
3. Create an empty folder and place the downloaded `.jar` file inside it.
4. Open a terminal in that folder.
5. Run the app with:

```bash
java -jar tp.jar
```

If your downloaded file has a different name, replace `tp.jar` with that filename.

6. Type commands and press Enter.
7. Use `/help` to see the available commands.

If you run the project from source instead of the release `.jar`, open a terminal in the project root and use:

```bash
# Windows
gradlew.bat run

# macOS/Linux
./gradlew run
```

## Command Format

Notes about command syntax:

- Commands must start with `/`.
- Command words are case-insensitive. Example: `/LIST` is treated as `/list`.
- Option keys are case-insensitive. Example: `--TYPE` is treated as `--type`.
- Asset type values ignore case: `stock`, `etf`, `bond`.
- Tickers are normalized to uppercase in storage and display.
- Tickers must not exceed 10 characters in length.
- For options, each key must be followed by one value. Example: `--qty 10`.
- Duplicate options in one command are rejected.
- Unknown options are rejected.
- Portfolio names can contain spaces when quoted. Example: `/create "Long Term"`.
- Portfolio names are case-insensitive. Example: `/use GROWTH` is treated as `/use growth`.
- Portfolio names preserve their original casing in storage and display.
- Quantity and price fields that require positive values must be finite and `> 0` (for example, `NaN` and `Infinity` are rejected).
- Fee fields (`--brokerage`, `--fx`, `--platform`) must be `>= 0`.

## Data Storage

CG2StocksTracker stores data in plain text files under `data/`.

- Main file: `data/CG2StocksTracker.txt`
- Watchlist file: `data/CG2StocksTracker.txt.watchlist`

The app saves data automatically after successful commands that change data:

- `/create`
- `/use`
- `/add`
- `/remove`
- `/set`
- `/setmany`
- `/watch add`
- `/watch remove`
- `/watch buy`

Read-only commands (`/list`, `/value`, `/insights`, `/help`) do not save data.

## Features

### View help: `/help`

Shows command usage summary.

Format:

`/help`

### Exit application: `/exit`

Exits the application.

Format:

`/exit`

### Create portfolio: `/create`

Creates a portfolio by name.

Format:

`/create NAME`

Examples:

- `/create longterm`
- `/create "Long Term Portfolio"`

Constraint:

- Portfolio name must not start with `/`.
- Portfolio name matching ignores case. The original casing is still shown.

Expected result:

- Portfolio is created.
- If it is the first portfolio, it becomes active automatically.

### Switch active portfolio: `/use`

Switches the active portfolio.

Format:

`/use NAME`

Example:

- `/use longterm`

Expected result:

- The active portfolio changes to `NAME` (case-insensitive match).

### List holdings or portfolios: `/list`

Shows holdings in the active portfolio, with optional filters.

Formats:

- `/list`
- `/list --stock`
- `/list --etf`
- `/list --bond`
- `/list --portfolios`

Notes:

- `/list` shows all holdings in active portfolio.
- If no active portfolio exists, `/list` shows portfolio names.
- A type filter shows the same table for one asset type only.
- `/list --portfolios` shows portfolio-level realized and unrealized P&L in alphabetical order.
- If there are no portfolios yet, `/list --portfolios` shows a message prompting you to create one.
- Holdings table columns are: `TYPE`, `TICKR`, `QTY`, `AVG_BUY`, `MKT_PRICE`, `VALUE`.
- `AVG_BUY` is the cost basis. `MKT_PRICE` is the latest saved market price. `VALUE = QTY x MKT_PRICE`.

### Add holding: `/add`

Adds quantity to a holding in the active portfolio.

Format:

`/add --type TYPE --ticker TICKER --qty QTY --price PRICE [--brokerage FEE] [--fx FEE] [--platform FEE]`

Required:

- `TYPE`: `stock`, `etf`, or `bond`
- `TICKER`: asset ticker
- `QTY > 0`
- `PRICE > 0`

Optional fees:

- `--brokerage FEE`
- `--fx FEE`
- `--platform FEE`

Fee values must be `>= 0`.

Behavior:

- If holding does not exist: a new holding is created.
- If holding exists: quantity increases and average buy price is recalculated using weighted average cost.
- Buy-side fees are included in cost basis.

Examples:

- `/add --type stock --ticker VOO --qty 1 --price 300`
- `/add --type stock --ticker VOO --qty 2 --price 310 --brokerage 1.5 --fx 2.0`

### Remove holding units: `/remove`

Sells part or all of a holding and records realized P&L.

Format:

`/remove --type TYPE --ticker TICKER [--qty QTY] [--price PRICE] [--brokerage FEE] [--fx FEE] [--platform FEE]`

Behavior rules:

- If `--qty` is omitted: sell all units.
- If `--price` is omitted: use holding's saved last price.
- If no `--price` and no saved last price: command fails.
- Sell-side fees are deducted from realized P&L.

Validation:

- `QTY` (if provided) must be `> 0` and cannot exceed current quantity.
- `PRICE` (if provided) must be `> 0`.
- Fee values (if provided) must be `>= 0`.

Examples:

- `/remove --type stock --ticker VOO --qty 0.5 --price 620`
- `/remove --type stock --ticker VOO --qty 0.5`
- `/remove --type stock --ticker VOO --price 590`
- `/remove --type stock --ticker VOO`

### Set market price: `/set`

Sets the market price for holdings. It does not buy or sell anything.

Formats:

- `/set --ticker TICKER --price PRICE`
- `/set --type TYPE --ticker TICKER --price PRICE`

Behavior:

- Without `--type`: updates only if exactly one holding with that ticker is in the active portfolio.
- If the same ticker exists across multiple asset types, command fails and requires `--type`.
- With `--type`: updates one matching holding by `(type, ticker)`.

Validation:

- `PRICE > 0`
- If no holding matches target, command fails.

Examples:

- `/set --ticker VOO --price 600`
- `/set --type stock --ticker VOO --price 600`

### Manage watchlist: `/watch`

Tracks assets you may buy later.

Formats:

- `/watch add --type TYPE --ticker TICKER --price PRICE`
- `/watch remove --type TYPE --ticker TICKER`
- `/watch list`
- `/watch buy --type TYPE --ticker TICKER --qty QTY --portfolio PORTFOLIO_NAME`

Notes:

- `TYPE` must be `stock`, `etf`, or `bond`.
- Watchlist keys are `(type, ticker)`; duplicates are rejected.
- `/watch add` requires a price; adding without `--price` is rejected.
- `/watch buy` buys the given `QTY` at the watchlist price into the target portfolio, then removes the item from the watchlist.
- `/watch buy` fails if watchlist item has no price.

Examples:

- `/watch add --type etf --ticker QQQ --price 450`
- `/watch list`
- `/watch buy --type etf --ticker QQQ --qty 2 --portfolio longterm`

### Bulk set prices from CSV: `/setmany`

Loads ticker prices from a CSV file and updates matching holdings in the active portfolio.

Format:

`/setmany --file FILEPATH`

CSV requirements:

- Header must be exactly `ticker,price` on the first line.
- Each non-blank data row must have exactly 2 columns: ticker, then price.
- Blank lines are ignored.
- `ticker` must not be blank. Tickers are matched case-insensitively.
- `price` must be a positive number greater than `0`.

Behavior:

- Valid rows are still processed even if some rows fail.
- Output shows success count, failure count, and row-level failure reasons.
- A row fails if the ticker does not exist in the active portfolio.

Example:

`prices.csv`

```csv
ticker,price
AAPL,210.50
QQQ,525.00
```

Command:

- `/setmany --file prices.csv`

### View portfolio value and P&L: `/value`

Shows portfolio-level valuation and P&L summary.

Format:

`/value`

Output includes:

- Current total value (priced holdings only)
- Portfolio realized P&L
- Per-holding unrealized P&L with type shown for disambiguation
- Total unrealized P&L

Formula:

`unrealized P&L = (lastPrice - averageBuyPrice) x quantity`

### View insights: `/insights`

Shows holding-level performance insights, with optional filters and a chart.

Format:

`/insights [--type stock|etf|bond] [--top N] [--chart]`

Option behavior:

- `--type` filters to one asset type.
- `--top N` shows top gainers only (positive unrealized P&L), sorted descending.
- `--chart` adds ASCII diverging chart (loss left, gain right).

Validation:

- `N` must be a positive integer.
- `N` must not exceed `10000`.
- Missing values for `--type` and `--top` are rejected.
- Duplicate options are rejected.
- Unsupported options are rejected.

Examples:

- `/insights`
- `/insights --type stock`
- `/insights --top 5`
- `/insights --type etf --top 3 --chart`

## Error Handling

Common reasons a command can fail:

- command does not start with `/`
- unknown command word
- missing required options
- invalid option key
- duplicate option key
- invalid asset type
- invalid quantity/price/fee values
- no active portfolio when one is required
- target holding or portfolio not found
- missing sell price context for `/remove`
- invalid CSV file path, header, or row format for `/setmany`

When a command fails, the app prints an error and waits for the next command.

## Glossary

| Term | Meaning |
|---|---|
| Portfolio | A named container of holdings (for example, `longterm`, `trading`). |
| Holding | One owned asset identified by `(asset type, ticker)`. |
| Ticker | Asset symbol used to identify holdings; normalized to uppercase and limited to 10 characters. |
| Asset type | Category of holding: `stock`, `etf`, or `bond`. |
| Quantity (QTY) | Number of units currently owned for a holding. |
| Average buy price (AVG_BUY) | Weighted average cost per unit, including buy-side fees. |
| Market price (MKT_PRICE) | Latest stored per-unit price set via `/set` or `/setmany`. |
| Current value | `quantity × market price` for a holding. |
| Realized P&L | Profit or loss from completed sells. |
| Unrealized P&L | Profit or loss on open holdings based on latest stored price. |
| Watchlist | List of assets you may buy later, optionally with target price. |
| Active portfolio | The currently selected portfolio used by most commands. |

## FAQ

**Q: Do I get live market prices automatically?**

A: No. Prices are set manually via `/set` or `/setmany`.

**Q: Why did `/remove` fail without `--price`?**

A: If you omit `--price`, the app uses saved last price. If no saved last price exists, provide `--price`.

**Q: How is average buy price computed?**

A: The app uses weighted average cost. Buy-side fees are included in the cost basis.

**Q: Are fees used in P&L calculations?**

A: Yes. Buy fees increase cost basis; sell fees reduce realized P&L.

**Q: Is data persistent across restarts?**

A: Yes. Portfolio and watchlist data are loaded from storage files on startup.

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
- `/set --ticker TICKER --price PRICE`
- `/set --type TYPE --ticker TICKER --price PRICE`
- `/watch add --type TYPE --ticker TICKER --price PRICE`
- `/watch remove --type TYPE --ticker TICKER`
- `/watch list`
- `/watch buy --type TYPE --ticker TICKER --qty QTY --portfolio PORTFOLIO_NAME`
- `/setmany --file FILEPATH`
- `/value`
- `/insights [--type stock|etf|bond] [--top N] [--chart]`
- `/help`
- `/exit`
