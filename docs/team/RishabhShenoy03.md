# RishabhShenoy03 - Project Portfolio Page

## Project: CG2StocksTracker

CG2StocksTracker is a desktop command-line portfolio tracker built for typing-oriented users who want to manage holdings quickly.
The user interacts with it through a CLI, and it is written in Java.

Given below are my contributions to the project.

### New Features: Implemented and refined parsing for `/set`, `/value`, and `/list`

- What:
	- `/set` supports updating price by `--type + --ticker` or by ticker-only update.
	- `/value` shows valuation for the active portfolio.
	- `/list` supports default listing, type filters (`--stock`, `--etf`, `--bond`), and portfolio summaries (`--portfolios`).
- Why:
	- These are core user workflows for daily usage (update prices, inspect holdings, check value).
	- Strong parser validation prevents invalid command states from propagating to execution logic.
- How:
	- Implemented validation paths and usage enforcement in `Parser`.
	- Coordinated parser behavior with execution-layer expectations to keep command contracts stable.

### Enhancement: Refactored parser and tests after command-signature changes

- What:
	- Refactored parser-related logic and updated tests when command signatures evolved during development.
- Why:
	- Reduced integration breakages and preserved behavior consistency across parser, model, and UI layers.
- How:
	- Updated parser test cases to cover both valid and invalid flows after each signature update.
	- Maintained regression safety while new behavior was introduced. [#24](https://github.com/AY2526S2-CS2113-W09-4/tp/pull/24)

### Code contributed

- [RepoSense contribution link](https://nus-cs2113-ay2526-s2.github.io/tp-dashboard/?search=w09-4&sort=groupTitle&sortWithin=title&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=true&checkedFileTypes=docs~functional-code~other~test-code&since=2026-02-20T00%3A00%3A00&filteredFileName=&tabOpen=true&tabType=authorship&tabAuthor=RishabhShenoy03&tabRepo=AY2526S2-CS2113-W09-4%2Ftp%5Bmaster%5D&authorshipIsMergeGroup=false&authorshipFileTypes=docs~functional-code~other~test-code&authorshipIsBinaryFileTypeChecked=false&authorshipIsIgnoredFilesChecked=false)

### Documentation

- User Guide:
	- Updated most UG sections to match finalized command formats and examples.
	- Revised command usage wording for clarity after signature updates.
- Developer Guide:
	- Updated most DG sections to stay in sync with current implementation.
	- Added/updated UML diagrams for `/set`, `/value`, and `/list`.
- [#34](https://github.com/AY2526S2-CS2113-W09-4/tp/pull/34), [#51](https://github.com/AY2526S2-CS2113-W09-4/tp/pull/51)

### Contributions to team-based tasks

- Helped prepare and release `v1.0`.
- Supported team integration by keeping parser, tests, and documentation aligned during refactoring-heavy phases.
- Took initiative for grouping and delegating tasks, often leading discussions and keeping the team on track for deadlines

### Review and mentoring contributions

- Reviewed teammates' pull requests and gave feedback on parser behavior and command consistency.
- Helped teammates troubleshoot command-related refactoring and test failures.

### Contributions beyond the project team

- Shared implementation/refactoring insights with peers where relevant.