# Issue #44: Expense Tracking and Maintenance

## What the issue was
Implement recurring maintenance expenses so active transport units cost money over time and those expenses are tracked.

## What was implemented
- Added periodic maintenance charging in simulation ticks:
  - Every active unit (assigned to a route) is charged once per tick.
  - Cost source is `TransportUnit.calculateMaintenanceCost()`.
- Applied charges via `Economy.charge(...)` with a maintenance reason string.
- Added dedicated maintenance-expense accumulation in `GameService` for breakdown display.
- Kept integration compatible with tile maintenance modifiers via existing `calculateMaintenanceCost()` logic.

## Files changed
- `underground_guild/src/main/java/team3/service/GameService.java`
- `underground_guild/src/test/java/team3/service/GameServiceTest.java`

## How it was tested
- Added test in `GameServiceTest` to verify:
  - Assigned (active) units incur maintenance each tick.
  - Unassigned (inactive) units do not incur maintenance.
  - Balance and total expenses update correctly.
  - Maintenance breakdown value is tracked in snapshot.

## Notes
- This branch is intended to pair with the delivery simulation tick flow from issue `#43`.
