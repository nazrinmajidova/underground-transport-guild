# Issue #38: Goods Production In Facilities

## What was implemented
- Facilities produce goods during integrated game ticks (`Game.update()`).
- Existing mine/farm/forge production logic is exercised continuously.

## Files changed
- `underground_guild/src/main/java/team3/model/Game.java`
- `underground_guild/src/main/java/team3/model/IndustrialFacility.java`

## Testing
- Covered by game loop integration tests and full test suite.
