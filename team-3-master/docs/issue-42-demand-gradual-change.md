# Issue #42: Gradual Demand Change Over Time

## What the issue was
Demand should evolve smoothly over time instead of jumping abruptly, while still being slightly different between game sessions.

## What was implemented
- Added gradual demand update logic in `Settlement`:
  - new per-tick demand update method: `advanceDemandTick(Random)`,
  - capped maximum demand change per tick based on population,
  - slight random variation in target demand for goods and passengers.
- Wired the simulation loop in `Game.update()` to call demand updates each tick for all settlements.
- Existing delivery hooks (`receiveGoods`, `receivePassengers`) continue reducing demand immediately when fulfilled.

## Files changed
- `underground_guild/src/main/java/team3/model/Settlement.java`
- `underground_guild/src/main/java/team3/model/Game.java`
- `underground_guild/src/test/java/team3/model/SettlementTest.java`

## How it was tested
- Added tests validating:
  - demand regrows gradually after being fulfilled,
  - unmet demand increases over multiple ticks,
  - different random seeds produce varied demand patterns.

## Notes
- Change-per-tick is intentionally capped to prevent sudden spikes.
- Random variation is small, so overall demand remains stable but not identical every session.
