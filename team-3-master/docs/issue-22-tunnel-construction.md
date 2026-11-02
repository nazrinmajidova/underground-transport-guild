# Issue #22: Tunnel Construction

This note records what was done for issue `#22`.

## Issue Summary

Allow the player to click valid rock tiles on the rendered map to build tunnels, reject invalid placements, and charge the tunnel cost through the game economy.

## Completed

- Added map tile click handling in the rendered viewport
- Routed tunnel build requests through `MainGameView`, `AppController`, and `GameService`
- Added detailed tunnel validation statuses in `GameMap`
- Prevented builds outside the map, inside hub tiles, on non-rock tiles, and on already tunneled tiles
- Added a tunnel cost and deducted it from the economy on successful builds
- Surfaced success and failure messages in the main screen status area
- Added automated tests for successful, invalid, and insufficient-funds tunnel builds

## File Map

- `underground_guild/src/main/java/team3/view/GameMapView.java`: map tile click handling
- `underground_guild/src/main/java/team3/view/MainGameView.java`: UI feedback and tunnel build wiring
- `underground_guild/src/main/java/team3/controller/AppController.java`: action routing from the view
- `underground_guild/src/main/java/team3/service/GameService.java`: tunnel build orchestration and economy charging
- `underground_guild/src/main/java/team3/model/GameMap.java`: tunnel validation rules
- `underground_guild/src/test/java/team3/service/GameServiceTest.java`: tunnel construction verification

## Verify

```bash
cd underground_guild
mvn clean compile
mvn test
mvn javafx:run
```

## Notes

- Tunnel construction currently uses a fixed cost constant in `GameService`.
- The JavaFX smoke check was started successfully and then stopped manually after launch confirmation.
