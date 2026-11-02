# Issue #52: Main Game Loop Integration

## What the issue was
Integrate core gameplay systems so one `Game.update()` tick advances the full simulation.

## What was implemented
- Expanded `Game.update()` into an integrated simulation loop with explicit order:
  1. `SimulationClock.tick()`
  2. Facility production (`IndustrialFacility.produce()`)
  3. Settlement demand progression (`Settlement.updateDemandTick(...)`)
  4. Transport movement (`TransportUnit.move()`)
  5. Maintenance deduction (`Economy.charge(...)`)
  6. Bankruptcy check (`Economy.isBankrupt()`)
- Added `Game.getGameState()` so integrated game-over transitions can be verified and used safely.
- Added `GameService.advanceSimulationTick()` as the service entrypoint for one simulation step.
- Wired `MainGameView` timer to call `advanceSimulationTick()` before refreshing map/UI.

## Files changed
- `underground_guild/src/main/java/team3/model/Game.java`
- `underground_guild/src/main/java/team3/model/Settlement.java`
- `underground_guild/src/main/java/team3/service/GameService.java`
- `underground_guild/src/main/java/team3/view/MainGameView.java`
- `underground_guild/src/test/java/team3/model/GameTest.java`

## How it was tested
- Added game-loop integration tests:
  - clock + production + demand + movement + maintenance update in one tick,
  - bankruptcy transition to `GAMEOVER` after maintenance deduction,
  - empty units/routes edge case does not crash.
- Full Maven test suite run with `mvn test`.

## Notes
- Loading/unloading remains handled by `TransportUnit.move()` when a unit reaches its route endpoint depot.
