# Issue #45: Financial Status Display UI

This note records what was done for issue `#45`.

## Issue Summary

Show live financial status in the UI so the player can always see balance, income, and expenses, with a warning when funds are low.

## Implemented

- Added a `FinancialSnapshot` read model in `GameService` for UI-friendly financial data.
- Added low-balance warning logic using a configurable threshold.
- Updated `MainGameView` status bar to show live:
  - `Balance`
  - `Income`
  - `Expenses`
- Added color-coded balance rendering:
  - green when healthy
  - red when low/critical
- Added periodic UI refresh (`Timeline`) so values update while the simulation is running.
- Updated `EconomyView` to show dynamic totals instead of hardcoded placeholders.
- Added concise breakdown placeholders for income/expense categories while detailed tracking issues are still open.
- Added service-level tests for financial snapshot behavior.

## Files Changed

- `underground_guild/src/main/java/team3/service/GameService.java`
- `underground_guild/src/main/java/team3/view/MainGameView.java`
- `underground_guild/src/main/java/team3/view/EconomyView.java`
- `underground_guild/src/test/java/team3/service/GameServiceTest.java`

## Verification

```bash
cd underground_guild
mvn clean compile
mvn test
mvn javafx:run
```

## Notes

- Detailed income-source and expense-type breakdown wiring is intentionally lightweight here and can be expanded with issues `#43` and `#44`.
- UI now reads from `GameService` financial snapshots instead of static economy placeholders.
