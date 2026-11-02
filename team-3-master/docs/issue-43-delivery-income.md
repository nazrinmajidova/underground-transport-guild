# Issue #43: Delivery Income

## What the issue was
Implement economy income when cargo or passengers are delivered to settlement destinations.

## What was implemented
- Added delivery-aware unloading for cargo units:
  - Settlement demand is fulfilled first.
  - Only fulfilled demand counts as delivered quantity.
  - Remaining cargo is still unloaded into depot storage.
- Added delivery-aware passenger disembark behavior:
  - Passengers disembarking at settlement-linked depots now fulfill settlement passenger demand.
- Added simulation tick handling in `GameService`:
  - Detects depot arrivals.
  - Resolves delivery unloads.
  - Calculates and applies delivery income via `Economy.earn(...)`.
  - Tracks delivery income total for UI breakdown.
- Updated economy UI panel breakdown to show live delivery income.
- Wired gameplay timer to advance simulation ticks continuously.

## Files changed
- `underground_guild/src/main/java/team3/model/CargoTransport.java`
- `underground_guild/src/main/java/team3/model/PassengerTransport.java`
- `underground_guild/src/main/java/team3/service/GameService.java`
- `underground_guild/src/main/java/team3/view/MainGameView.java`
- `underground_guild/src/main/java/team3/view/EconomyView.java`
- `underground_guild/src/test/java/team3/service/GameServiceTest.java`

## How it was tested
- Added/updated unit tests in `GameServiceTest`:
  - Goods delivery to settlement demand increases income and reduces demand.
  - Passenger delivery to settlement increases income and reduces passenger demand.
  - Financial snapshot default/live expectations updated for new breakdown fields.

## Notes
- Delivery income uses a base-per-unit value with a small route-distance bonus.
- Expense-side detailed maintenance tracking remains part of issue #44.
