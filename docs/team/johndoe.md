# John Doe - Project Portfolio Page

## Overview

CG2StocksTracker is a command-line application for typing-oriented users who want to manage multi-asset investment portfolios quickly.
It helps users track holdings, update prices, and view portfolio valuation and insights without relying on spreadsheets.

My main contributions focused on command parsing and command-flow stability as the team evolved the command signatures.
I implemented and refactored parser logic for core commands, updated tests to preserve correctness during API changes, and kept the documentation and diagrams aligned with the latest behavior.

## Summary of Contributions

### Code contributed

- **[Code Dashboard link](PASTE_YOUR_TP_CODE_DASHBOARD_LINK_HERE)**

### Enhancements implemented

1. **Implemented and refined parser support for `/set`, `/value`, and `/list` command flows**
	 - Added/updated parsing rules and validation paths for these commands in `Parser`.
	 - Ensured command behavior remained consistent when command signatures changed.
	 - Improved robustness of option handling and usage-error reporting to reduce invalid input edge cases.

2. **Refactored parsing-related code when command signatures changed**
	 - Performed broad parser-level refactoring to keep command contracts coherent across parser, command model, and execution layers.
	 - Reduced breakage risk by updating affected tests together with parser changes.
	 - Kept backward behavior expectations explicit in tests after each signature change.

3. **Expanded and maintained parser-focused test coverage**
	 - Added/updated tests in `ParserTest` for changed command signatures and validation behavior.
	 - Ensured parser regression checks covered both valid forms and invalid-usage scenarios.

### Contributions to the User Guide (UG)

- Updated UG command documentation to match latest command signatures and examples.
- Reviewed and revised most UG sections for consistency after feature/refactoring changes.
- Improved command usage wording so users can distinguish similar command variants more easily.

### Contributions to the Developer Guide (DG)

- Updated most DG sections to keep implementation details synchronized with current code behavior.
- Added/updated UML diagrams for:
	- `/set`
	- `/value`
	- `/list`
- Revised DG feature descriptions and command flow explanations after parser/command refactors.

### Contributions to team-based tasks

- Contributed to release preparation and helped deliver **v1.0**.
- Supported integration efforts by aligning parser/test/documentation changes across features.
- Helped maintain project consistency during high-change periods when command APIs evolved.

### Review and mentoring contributions

- Reviewed teammates' pull requests and provided feedback on parser behavior, command syntax consistency, and test quality.
- Helped teammates debug command-related integration issues during refactoring cycles.
- Add evidence links:
	- **[PR review 1](PASTE_LINK)**
	- **[PR review 2](PASTE_LINK)**
	- **[Example mentoring/help thread](PASTE_LINK)**

### Contributions beyond the project team

- Participated in module-level knowledge sharing on command design and parser refactoring practices.
- Add evidence links:
	- **[Bug report/help post in another team's repo](PASTE_LINK)**
	- **[Forum post / technical sharing](PASTE_LINK)**

## Optional: Contributions to the Developer Guide (Extracts)

The following are representative DG contributions I made:

1. Command implementation details and behavior notes for `/set`, `/value`, and `/list`.
2. UML diagrams showing class/sequence interactions for those commands.
3. Post-refactor updates to ensure the DG matches current parser and execution flow.

## Optional: Contributions to the User Guide (Extracts)

The following are representative UG contributions I made:

1. Updated command formats and examples for `/set`, `/value`, and `/list`.
2. Consistency edits to command wording after signature updates.
3. End-user guidance adjustments to reduce ambiguity in command usage.

