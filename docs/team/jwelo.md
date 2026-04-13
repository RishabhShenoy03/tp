# jwelo - Project Portfolio Page

## Project: CG2StocksTracker

CG2StocksTracker is a desktop command-line portfolio tracker built for typing-oriented users who want to manage holdings quickly.
The user interacts with it through a CLI, and it is written in Java.

Given below are my contributions to the project.

### New Features: Implemented persistent storage architecture

- What:
    - Implemented `Storage` for loading/saving `PortfolioBook` state to `data/CG2StocksTracker.txt`.
    - Added support for persisting active portfolio, realized P&L, holdings, and optional last price fields.
    - Implemented CSV bulk price loading (`/setmany`) with partial-success handling and row-level failure reporting.
    - Added watchlist persistence in `data/CG2StocksTracker.txt.watchlist`.
- Why:
    - Portfolio tracking must survive application restarts and handle malformed input data safely.
    - Bulk updates reduce repetitive manual price updates.
- How:
    - Added record-based file parsing (`ACTIVE`, `PORTFOLIO`, `HOLDING`) and validation paths in `Storage`.
    - Added robust IO and corruption handling that surfaces user-friendly `AppException` messages.
    - Kept backward compatibility for legacy holding rows while supporting the newer schema.

### New Features: Implemented watchlist model and end-to-end `/watch` command flow

- What:
    - Created `Watchlist` and `WatchlistItem` classes with validation and ticker normalization.
    - Added `/watch add`, `/watch remove`, `/watch list`, and `/watch buy` command support through parser, app logic, UI, and storage.
    - Added watchlist persistence in `data/CG2StocksTracker.txt.watchlist`.
    - Implemented watchlist-to-portfolio buy flow that:
        - validates target portfolio existence
        - requires a stored watchlist price
        - removes bought items from the watchlist
- Why:
    - Users need a separate place to track potential buys without mixing them into owned holdings.
    - Watchlist-to-portfolio conversion should be fast and integrated with existing portfolio workflows.
    - Buy-side flow needed guardrails to prevent silent failures (missing price, missing portfolio, duplicates).
- How:
    - Implemented watchlist storage using `(type, ticker)` uniqueness and stable insertion order.
    - Implemented `buyItem(...)` logic that buys 1 unit at stored watch price into a chosen portfolio, then removes the watch item.
    - Integrated save/load lifecycle so watchlist state is persisted consistently with portfolio state.

### Testing and stability improvements

- Added targeted unit tests for `Storage` and `Watchlist` behavior, including:
    - watchlist duplicate prevention and buy validation paths
    - watchlist save/load round-trip correctness
    - corrupted watchlist file detection
    - CSV row validation and failure accounting

### Code contributed

- [RepoSense contribution link](https://nus-cs2113-ay2526-s2.github.io/tp-dashboard/?search=jwelo&breakdown=true)

### Documentation

- User Guide:
    - Updated `/watch` command usage and behavior notes to match implementation.
    - Expanded storage and persistence details, including watchlist file behavior.
- Developer Guide:
    - Added/expanded Watchlist and Storage implementation details and command flows.
    - Updated design notes to reflect validation, persistence format, and load/save behavior.
- [#49](https://github.com/AY2526S2-CS2113-W09-4/tp/pull/49), [#47](https://github.com/AY2526S2-CS2113-W09-4/tp/pull/47)

### Project management

- Set up the team repository and initial project structure.
- Contributed to drafting and refining user stories used for planning.
- Participated actively in discussions on overall architecture and feature design direction.

### Contributions to team-based tasks

- Helped prepare and release project milestones.
- Supported feature integration by keeping storage/watchlist behavior aligned across parser, execution, UI, tests, and docs.
- Reviewed implementation details with teammates during integration and bug-fixing phases.
