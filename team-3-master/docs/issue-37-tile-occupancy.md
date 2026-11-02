# Issue #37: Tile Occupancy Enforcement

## What the issue was
Prevent multiple transport units from occupying the same tunnel tile at the same time.

## What was implemented
- Added occupancy-aware movement inside `Game.update()`.
- Units now:
  - claim their current tile with `Tile.setOccupiedBy(...)`,
  - check the next tunnel tile before moving,
  - wait if the next tile is already occupied by another unit,
  - release previous tile occupancy when movement succeeds.
- Added internal movement state per unit to support round-trip traversal along route paths.

## Files changed
- `underground_guild/src/main/java/team3/model/Game.java`
- `underground_guild/src/test/java/team3/model/GameTest.java`

## How it was tested
- Added tests to verify:
  - a trailing unit waits when the next tile is blocked,
  - no two units end up on the same tunnel tile after simulation update,
  - tile `occupiedBy` references are updated correctly.

## Notes
- Movement remains deadlock-safe in normal linear route flow because blocked units wait and continue once tiles free up.
