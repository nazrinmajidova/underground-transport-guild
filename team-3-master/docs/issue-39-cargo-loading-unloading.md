# Issue #39: Cargo Loading And Unloading

## What was implemented
- Depot/cargo flow supports loading from depot storage and unloading at destinations.
- Delivery-side unloading integrates with settlement demand and economy income tracking.

## Files changed
- `underground_guild/src/main/java/team3/model/Depot.java`
- `underground_guild/src/main/java/team3/model/CargoTransport.java`
- `underground_guild/src/main/java/team3/service/GameService.java`

## Testing
- Covered by cargo/service tests and full Maven test suite.
