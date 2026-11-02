# Issue #24: Hub And Depot Rendering

This note records what was done for issue `#24`.

## Issue Summary

Render settlements, industrial facilities, and depots as clear overlays on the map so the player can identify important locations at a glance.

## Completed

- Added a dedicated overlay layer above the base tile canvas
- Added hub outlines and accent fills for all rendered hub types
- Added distinct visual markers for settlements, facilities, and depots
- Added always-visible hub labels with readable formatting
- Seeded starter depots into new games so depot rendering is visible in the current repository state
- Added tests confirming new games include depots alongside generated settlements and facilities

## File Map

- `underground_guild/src/main/java/team3/view/GameMapView.java`: hub overlay drawing, marker rendering, and labels
- `underground_guild/src/main/java/team3/view/MapPalette.java`: hub accent and outline colors
- `underground_guild/src/main/java/team3/model/GameMap.java`: public hub-placement validation used for starter depots
- `underground_guild/src/main/java/team3/model/Game.java`: starter depot seeding
- `underground_guild/src/test/java/team3/model/GameTest.java`: depot and generated-hub verification

## Verify

```bash
cd underground_guild
mvn clean compile
mvn test
mvn javafx:run
```

## Notes

- Starter depots are temporary gameplay scaffolding so depot rendering can be demonstrated before the full depot placement workflow is implemented elsewhere.
- The JavaFX smoke check should confirm settlements, facilities, depots, and labels are all visible together.
