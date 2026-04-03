# joeLku1 - Project Portfolio Page

## Project: CG2StocksTracker

CG2StocksTracker is a desktop command-line portfolio tracker built for typing-oriented users who want to manage holdings quickly.
The user interacts with it through a CLI, and it is written in Java.

Given below are my contributions to the project.

### New Features: Implemented insights command variants and chart view

#### Core Insights Table with Filtering
- What:
  - Added the `/insights` command flow from skeleton to working CLI output.
  - Implemented `--type` and `--top N` filtering behavior to show holdings by asset type or top N gainers.
  - Extended `showInsightsTable()` in [Ui.java](../../src/main/java/duke/Ui.java) with overloaded variants supporting optional filtering and chart rendering.
- Why:
  - Investors need a fast way to inspect performance beyond raw holdings lists.
  - Filtering by type (stocks, crypto, etc.) helps focus on portfolio segments.
  - Top-gainers view highlights winners for quick portfolio review.
- How:
  - Extended command parsing in CG2StocksTracker to validate `--type` and `--top N` options.
  - Implemented stream-based filtering and sorting in `showInsightsTable()`.
  - Sorted holdings by ticker for all-holdings view, sorted by unrealized P&L (descending) to show top-N holdings.

#### P&L Chart: Visual Performance at a Glance
- What:
  - Added optional `--chart` flag to render a diverging bar chart showing per-holding performance.
  - Losses displayed on the left (with `-`), gains on the right (with `+`), centered on `|`.
- Why:
  - Visual comparison is easier than scanning percentage numbers, especially across different position sizes.
- How:
  - Uses percentage-based scaling: `scaledUnits = Math.round((Math.abs(value) / maxAbsValue) * CHART_SIDE_WIDTH)`.
  - Relative normalization ensures small gains/losses remain visible while scaling to the maximum P&L in the current view.

### Enhancement: Built and stabilized UI/testing foundations

- What:
  - Implemented core UI output in [Ui.java](../../src/main/java/duke/Ui.java) for holdings, portfolio valuations, insights, and charts.
  - Added UI test suite in [UiTest.java](../../src/test/java/duke/UiTest.java) with 7 test cases and synchronized golden outputs.
- Why:
  - CLI-first project requires robust output rendering and consistent formatting for correctness.
  - Stable text output is critical for CI checks and team integration testing.
- How:
  - Built Ui.java incrementally with consistent formatting helpers and test coverage.
  - Maintained alignment between code behavior, tests, and golden outputs whenever changes were made.
  - Addressed edge cases such as missing prices and extreme P&L values.

### Technical Approach and Code Quality

**Design Patterns:**
- **Method Overloading**: `showInsightsTable()` variants support gradual complexity (no args → all holdings; with filters → filtered and rendered).
- **Stream-based Filtering**: Clean, declarative filtering by type and top-N.
- **Configurable Constants**: `CHART_SIDE_WIDTH` and `TICKER_WIDTH` centralize UI dimensions.
- **Separation of Concerns**: Bar-rendering logic isolated in `buildDivergingBar()`.

**Quality Practices:**
- Defensive programming with null checks to prevent NullPointerExceptions.
- Edge-case handling: holdings without prices, zero P&L, extreme return values.
- Assertion validation for method contracts.
- Consistent numeric formatting across all output.
- Comprehensive testing: 7 UI unit tests + text-ui-test golden file validation.

### Documentation and diagrams

- What:
  - Added PNG diagrams to Developer Guide.
  - Updated Developer Guide and User Guide to match final behavior.
- Why:
  - Diagrams improve maintainability and speed up onboarding for reviewers and teammates.
  - Keeping docs in sync prevents confusion during testing and grading.
- How:
  - Produced first-round and full-round diagram sets under `docs/diagrams`.
  - Reworked guide sections for command behavior, usage, and examples.

### Code contributed
- [RepoSense contribution link](https://nus-cs2113-ay2526-s2.github.io/tp-dashboard/?search=&sort=groupTitle%20dsc&sortWithin=title&since=2026-02-20T00%3A00%3A00&timeframe=commit&mergegroup=&groupSelect=groupByRepos&breakdown=false&filteredFileName=&tabOpen=true&tabType=authorship&tabAuthor=joeLku1&tabRepo=AY2526S2-CS2113-W09-4%2Ftp%5Bmaster%5D&authorshipIsMergeGroup=false&authorshipFileTypes=docs~functional-code~test-code~other&authorshipIsBinaryFileTypeChecked=false&authorshipIsIgnoredFilesChecked=false)

### Contributions to team-based tasks

- Helped drive UI and output consistency during periods of frequent command evolution.
- Supported integration by aligning parser/execution/UI output with tests and documentation.
- Contributed to feature completion and release readiness by addressing CI failures quickly.

### Review and mentoring contributions

- Reviewed teammates' work related to command behavior and output consistency.
- Helped troubleshoot failing tests and output mismatches during integration.

### Contributions beyond the project team

- Shared practical debugging and CLI-output testing approaches with peers where relevant.
