# User Guide

## Introduction

CG2StocksTracker is a command-line portfolio tracker for amateur investors who prefer fast typing workflows.

It helps you:

- create and switch portfolios
- track holdings across stock, ETF, and bond assets
- update market prices manually
- monitor realized and unrealized profit/loss
- keep a watchlist for planned buys

CG2StocksTracker is designed to give a clear, accurate view of your holdings without spreadsheets or heavy trading platforms.

## Quick Start

1. Ensure Java 17 or above is installed.
2. Open a terminal in the project root.
3. Run the app:

```bash
# Windows
gradlew.bat run

# macOS/Linux
./gradlew run
```

4. Type commands and press Enter.
5. Use `/help` to view available commands.

## Command Format

Notes about command syntax:

- Commands must start with `/`.
- Command words are case-insensitive. Example: `/LIST` is treated as `/list`.
- Option keys are case-insensitive. Example: `--TYPE` is treated as `--type`.
- Asset type values are case-insensitive: `stock`, `etf`, `bond`.
- Tickers are normalized to uppercase in storage and display.
- For options, each key must be followed by one value. Example: `--qty 10`.
- Duplicate options in one command are rejected.
- Unknown options are rejected.
- Portfolio names can contain spaces when quoted. Example: `/create "Long Term"`.
- Quantity and price fields that require positive values must be `> 0`.
- Fee fields (`--brokerage`, `--fx`, `--platform`) must be `>= 0`.

## Data Storage

CG2StocksTracker stores data in plain text files under `data/`.

- Main file: `data/CG2StocksTracker.txt`
- Watchlist file: `data/CG2StocksTracker.txt.watchlist`

Data is saved automatically after successful state-changing commands:

- `/create`
- `/add`
- `/remove`
- `/set`
- `/setmany`
- `/watch add`
- `/watch remove`
- `/watch buy`

Read-only commands (`/list`, `/value`, `/insights`, `/help`) do not trigger save.

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

- Active portfolio changes to `NAME`.

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
- Type-filtered list keeps the same table style but only one asset type.
- `/list --portfolios` shows portfolio-level realized and unrealized P&L in alphabetical order.

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

Sets market price for holdings. This does not buy or sell.

Formats:

- `/set --ticker TICKER --price PRICE`
- `/set --type TYPE --ticker TICKER --price PRICE`

Behavior:

- Without `--type`: update all holdings in active portfolio with matching ticker.
- With `--type`: update only one matching holding by `(type, ticker)`.

Validation:

- `PRICE > 0`
- If no holding matches target, command fails.

Examples:

- `/set --ticker VOO --price 600`
- `/set --type stock --ticker VOO --price 600`

### Manage watchlist: `/watch`

Tracks assets you may buy later.

Formats:

- `/watch add --type TYPE --ticker TICKER [--price PRICE]`
- `/watch remove --type TYPE --ticker TICKER`
- `/watch list`
- `/watch buy --type TYPE --ticker TICKER --portfolio PORTFOLIO_NAME`

Notes:

- `TYPE` must be `stock`, `etf`, or `bond`.
- Watchlist keys are `(type, ticker)`; duplicates are rejected.
- `/watch buy` buys exactly 1 unit at watchlist price, into target portfolio, then removes item from watchlist.
- `/watch buy` fails if watchlist item has no price.

Examples:

- `/watch add --type etf --ticker QQQ --price 450`
- `/watch list`
- `/watch buy --type etf --ticker QQQ --portfolio longterm`

### Bulk set prices from CSV: `/setmany`

Loads ticker prices from CSV and updates matching holdings in active portfolio.

Format:

`/setmany --file FILEPATH`

CSV requirements:

- Header must be `ticker,price` (case-insensitive compare after lowercasing).
- Each data row must have exactly 2 columns.
- `price` must be a positive number.

Behavior:

- Valid rows are processed even if some rows fail.
- Output shows success count, failure count, and row-level failure reasons.

Example:

- `/setmany --file text-ui-test/prices.csv`

### View portfolio value and P&L: `/value`

Shows portfolio-level valuation and P&L summary.

Format:

`/value`

Output includes:

- Current total value (priced holdings only)
- Portfolio realized P&L
- Per-holding unrealized P&L
- Total unrealized P&L

Formula:

`unrealized P&L = (lastPrice - averageBuyPrice) x quantity`

### View insights: `/insights`

Shows holding-level performance insights, with optional filters and chart.

Format:

`/insights [--type stock|etf|bond] [--top N] [--chart]`

Option behavior:

- `--type` filters to one asset type.
- `--top N` shows top gainers only (positive unrealized P&L), sorted descending.
- `--chart` adds ASCII diverging chart (loss left, gain right).

Validation:

- `N` must be a positive integer.
- Duplicate options are rejected.
- Unsupported options are rejected.

Examples:

- `/insights`
- `/insights --type stock`
- `/insights --top 5`
- `/insights --type etf --top 3 --chart`

## Error Handling

Common reasons commands fail:

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

## FAQ

**Q: Do I get live market prices automatically?**

A: No. Prices are set manually via `/set` or `/setmany`.

**Q: Why did `/remove` fail without `--price`?**

A: If you omit `--price`, the app uses saved last price. If no saved last price exists, provide `--price`.

**Q: How is average buy price computed?**

A: Weighted average cost is used. Buy-side fees are included in cost basis.

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
- `/watch add --type TYPE --ticker TICKER [--price PRICE]`
- `/watch remove --type TYPE --ticker TICKER`
- `/watch list`
- `/watch buy --type TYPE --ticker TICKER --portfolio PORTFOLIO_NAME`
- `/setmany --file FILEPATH`
- `/value`
- `/insights [--type stock|etf|bond] [--top N] [--chart]`
- `/help`
- `/exit`
