# Issue #34: Route Visualization

This note records what was done for issue `#34`.

## Issue Summary

Render selected transport routes on the map so players can see route endpoints, tunnel paths, and direction of travel.

## Completed

- Added route overlay support to the map view on top of the tile and hub layers
- Replaced the placeholder route panel list with live route selection from the current game
- Seeded starter routes between generated depots so the repository now contains visible route data
- Added tunnel-path lookup in `GameMap` and used it to draw routes only along connected tunnel tiles
- Added route endpoint ordering, color differentiation, direction arrows, and selected-route labels
- Added tests covering route seeding, tunnel path lookup, and route palette state

## File Map

- `underground_guild/src/main/java/team3/model/GameMap.java`: tunnel-path and buildable-path search for routes
- `underground_guild/src/main/java/team3/model/Game.java`: starter route creation and starter tunnel carving between depots
- `underground_guild/src/main/java/team3/view/GameMapView.java`: route overlay drawing, endpoint highlighting, arrows, and labels
- `underground_guild/src/main/java/team3/view/RouteManagementView.java`: dynamic route list and route selection handling
- `underground_guild/src/main/java/team3/view/MainGameView.java`: route panel and selected-route integration
- `underground_guild/src/main/java/team3/view/MapPalette.java`: route color palette
- `underground_guild/src/test/java/team3/model/GameMapTest.java`: route path search tests
- `underground_guild/src/test/java/team3/model/GameTest.java`: starter route seeding test
- `underground_guild/src/test/java/team3/view/MapPaletteTest.java`: route color test

## Verify

```bash
cd underground_guild
mvn clean compile
mvn test
mvn javafx:run
```

## Notes

- Starter routes are temporary repository scaffolding so route visualization can be demonstrated before full route creation tooling is implemented elsewhere.
- Route lines are drawn from actual tunnel connectivity rather than hardcoded straight lines.
