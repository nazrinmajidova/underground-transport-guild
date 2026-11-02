# Issue #46: Bankruptcy And Game Over

## What was implemented
- Added game-over callback path from core game state when bankruptcy is reached.
- Added `GameOverView` overlay and wiring from controller/main view.
- On game-over, simulation clock is paused and overlay is shown.

## Files changed
- `underground_guild/src/main/java/team3/model/Game.java`
- `underground_guild/src/main/java/team3/controller/AppController.java`
- `underground_guild/src/main/java/team3/view/MainGameView.java`
- `underground_guild/src/main/java/team3/view/GameOverView.java`

## Testing
- Covered by game loop bankruptcy tests and manual UI smoke checks.
