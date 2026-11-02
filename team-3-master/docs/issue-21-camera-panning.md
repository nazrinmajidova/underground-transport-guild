# Issue #21: Camera Panning And Scrolling

This note records what was done for issue `#21`.

## Issue Summary

Add camera navigation so the rendered underground map can be explored with keyboard and mouse input while staying inside the map boundaries.

## Completed

- Converted the map canvas into a clipped viewport
- Added keyboard panning with arrow keys and `WASD`
- Added mouse-drag panning
- Added camera offset tracking inside `GameMapView`
- Added clamped camera movement so the map cannot scroll past its edges
- Added automated tests for the panning clamp logic

## File Map

- `underground_guild/src/main/java/team3/view/GameMapView.java`: viewport, input handling, and offset clamping
- `underground_guild/src/test/java/team3/view/GameMapViewTest.java`: camera math verification

## Verify

```bash
cd underground_guild
mvn clean compile
mvn test
mvn javafx:run
```

## Notes

- The camera behavior is intentionally kept inside the map view so later route and tunnel interactions do not need to manage viewport math themselves.
- The JavaFX smoke check was started successfully and then stopped manually after launch confirmation.
