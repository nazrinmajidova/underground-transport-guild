# Issue #36: Smooth Continuous Movement

## What was implemented
- Vehicle rendering now interpolates along route tunnel paths using travel progress.
- Added direction-aware interpolation using transport movement direction.
- Kept fallback to tile-center rendering if route/path data is unavailable.

## Files changed
- `underground_guild/src/main/java/team3/view/GameMapView.java`
- `underground_guild/src/main/java/team3/model/TransportUnit.java`

## Testing
- Covered by full project build/test run and manual in-game movement smoke checks.
