# Issue #20: Map Rendering

This note records what was done for issue `#20`.

## Issue Summary

Render the generated map on the main game screen so the player can see underground tiles, tunnels, and generated hub types in the actual JavaFX interface.

## Completed

- Replaced the placeholder map area in `MainGameView`
- Added a dedicated `GameMapView` canvas-based renderer
- Added a reusable `MapPalette` for consistent tile and hub colors
- Rendered the full tile grid from the active `GameMap`
- Added distinct colors for rock, tunnels, settlements, depots, mines, farms, and forges
- Hardened refresh behavior so redraws clear and resize cleanly
- Added automated palette tests for the map color rules

## File Map

- `underground_guild/src/main/java/team3/view/MainGameView.java`: wired the main screen to the active rendered map
- `underground_guild/src/main/java/team3/view/GameMapView.java`: canvas-based map rendering component
- `underground_guild/src/main/java/team3/view/MapPalette.java`: rendering palette for tiles and hub types
- `underground_guild/src/test/java/team3/view/MapPaletteTest.java`: color mapping verification

## Verify

```bash
cd underground_guild
mvn clean compile
mvn test
mvn javafx:run
```

## Notes

- This issue focuses on rendering the map itself; camera movement is handled separately in issue `#21`.
- The JavaFX smoke check was started successfully and then stopped manually after launch confirmation.
