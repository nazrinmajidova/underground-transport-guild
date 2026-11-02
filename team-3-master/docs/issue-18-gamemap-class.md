# Issue #18: GameMap Class

This note records what was done for issue `#18`.

## Issue Summary

Implement the `GameMap` class so the project has a real 2D world grid with safe tile access, tunnel building, and hub connectivity checks.

## Completed

- Reworked `GameMap` to own a generated `Tile[][]` grid filled with underground rock tiles
- Added safe bounds checking with `isInBounds()` and a null-safe `getTileAt()`
- Added tunnel-building validation through `buildTunnel()`
- Added hub registration through `addHub()`
- Added tunnel-based hub connectivity checks through `isConnected()`
- Added automated JUnit tests for the GameMap behaviors
- Added root plan and todo trackers for the approved Milestone 3 scope

## File Map

- `MILESTONE3_RAZA_PLAN.md`: approved execution plan for Raza's focused Milestone 3 batch
- `TODO_MILESTONE3_RAZA.md`: current status tracker for the approved issues
- `underground_guild/pom.xml`: JUnit and Surefire test support
- `underground_guild/src/main/java/team3/model/Tile.java`: underground rock tile support and tunnel-build validation
- `underground_guild/src/main/java/team3/model/GameMap.java`: map storage, access, tunnel logic, and connectivity traversal
- `underground_guild/src/test/java/team3/model/GameMapTest.java`: automated checks for map behavior

## Verify

```bash
cd underground_guild
mvn clean compile
mvn test
mvn javafx:run
```

## Notes

- The richer procedural generation is intentionally left for issue `#19`.
- The JavaFX smoke check was started successfully and then stopped manually after launch confirmation.
