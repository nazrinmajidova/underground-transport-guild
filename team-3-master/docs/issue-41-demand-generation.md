# Issue #41: Settlement Demand Generation

## What was implemented
- Added per-tick settlement demand generation/update hook in game loop.
- Demand is updated every simulation tick from `Game.update()` via settlement demand tick logic.

## Files changed
- `underground_guild/src/main/java/team3/model/Game.java`
- `underground_guild/src/main/java/team3/model/Settlement.java`

## Testing
- Covered by settlement/game integration tests and full Maven test suite.
