# Elegazia - Project Portfolio Page

## Project: CG2StocksTracker

CG2StocksTracker is a desktop command-line portfolio tracker built for typing-oriented users who want to manage holdings quickly.
The user interacts with it through a CLI, and it is written in Java.

Given below are my contributions to the project.

---

### New Features: Implemented portfolio tracking and gains calculation

* What:

    * Implemented tracking of holdings with quantity, average buy price, and price updates.
    * Supports correct computation of realized and unrealized gains across `/add`, `/remove`, and `/value`.
* Why:

    * Core functionality of a portfolio tracker requires accurate tracking of investments and returns.
    * Ensures users can correctly evaluate performance of their holdings.
* How:

    * Extended `Holding` to maintain `averageBuyPrice` and compute P&L.
    * Implemented weighted average updates on buy and correct realized P&L on sell.
    * Aggregated gains at portfolio level and exposed via `/value`.

---

### New Features: Designed and implemented command parsing system

* What:

    * Implemented parsing for commands including `/add`, `/remove`, `/set`, `/list`, and `/value`.
    * Enforced structured command formats using flags such as `--type`, `--ticker`, `--qty`, and `--price`.
* Why:

    * Prevents invalid command states from propagating to execution logic.
    * Ensures consistent and predictable CLI behaviour.
* How:

    * Centralised parsing logic in `Parser`.
    * Implemented option parsing, required field validation, and input normalization.
    * Converted user input into `ParsedCommand` objects for execution.

---

### Enhancement: Implemented support for transaction fees

* What:

    * Extended `/add` and `/remove` to support optional fee inputs:

        * `--brokerage`
        * `--fx`
        * `--platform`
* Why:

    * Reflects real-world investing scenarios where fees affect returns.
    * Improves accuracy of portfolio performance calculations.
* How:

    * Parsed optional fee fields and aggregated them into a single value.
    * Integrated fee handling into execution logic without breaking existing behaviour.

---

### Enhancement: Refactored parser and tests after command-signature changes

* What:

    * Refactored parser-related logic and updated tests when command signatures evolved.
* Why:

    * Reduced integration breakages and preserved behaviour consistency across parser, model, and UI layers.
* How:

    * Updated parser test cases to cover both valid and invalid flows after each change.
    * Maintained regression safety while introducing new behaviour.

---

### Code contributed

* [RepoSense contribution link](https://nus-cs2113-ay2526-s2.github.io/tp-dashboard/?search=w09-4&sort=groupTitle&sortWithin=title&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=true&checkedFileTypes=docs~functional-code~other~test-code&since=2026-02-20T00%3A00%3A00&filteredFileName=&tabOpen=true&tabType=authorship&tabAuthor=YOUR_USERNAME&tabRepo=AY2526S2-CS2113-W09-4%2Ftp%5Bmaster%5D&authorshipIsMergeGroup=false&authorshipFileTypes=docs~functional-code~other~test-code&authorshipIsBinaryFileTypeChecked=false&authorshipIsIgnoredFilesChecked=false)

---

### Documentation

* User Guide:

    * Updated `/add`, `/remove`, `/set`, `/list`, and `/value` command formats to reflect final flag-based syntax (e.g. `--type`, `--ticker`, `--qty`, `--price`).
    * Revised command examples to align with actual parser behaviour, including required and optional arguments.
    * Clarified usage for filtered listing (`--stock`, `--etf`, `--bond`) and portfolio summaries (`--portfolios`).

* Developer Guide:

    * Updated DG sections for command flow to reflect the actual parsing → execution pipeline (`Parser` → `ParsedCommand` → `CG2StocksTracker.execute`).
    * Refined descriptions of parser validation logic, including option parsing and error handling paths.
    * Added/updated UML sequence diagrams for `/set`, `/value`, and `/list` to match implemented behaviour.
    * Ensured DG explanations are consistent with current command contracts and no longer reflect outdated command signatures.

---


### Contributions to team-based tasks

* Helped prepare and release `v1.0`.
* Supported team integration during refactoring-heavy phases.
* Contributed to task coordination and keeping the team aligned with deadlines.

---

### Review and mentoring contributions

* Reviewed teammates' pull requests and provided feedback on command behaviour and parser consistency.
* Helped troubleshoot parsing issues and integration bugs.

---

### Contributions beyond the project team

* Shared implementation and refactoring insights with peers where relevant.
