# Issue #31: Route Creation UI

## What was implemented
- Added route creation mode in the route panel.
- Added depot-by-depot route building from map clicks.
- Added confirm/cancel flow with route-name input.
- Added service-side route creation validation (`GameService.createRoute`).

## Files changed
- `underground_guild/src/main/java/team3/view/RouteManagementView.java`
- `underground_guild/src/main/java/team3/view/MainGameView.java`
- `underground_guild/src/main/java/team3/service/GameService.java`
- `underground_guild/src/main/java/team3/model/Route.java`

## Testing
- Covered by full project build/test run and manual route creation smoke checks.
