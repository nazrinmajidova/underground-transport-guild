# Issue #2: Welcome Screen

This note records what was done for issue `#2`.

## Issue Summary

Implement the first visible screen of the game with the required actions and placeholder behavior.

## Completed

- Kept the application starting on the welcome screen
- Added `New Game`, `Load Save`, `Settings`, and `Exit` buttons
- Added title, subtitle, status text, and credits text
- Wired placeholder actions through the controller/service layer
- Made `Exit` close the application window

## Placeholder Behavior

- `New Game`: main game screen not implemented yet
- `Load Save`: load/save not implemented yet
- `Settings`: settings not implemented yet

## File Map

- `underground_guild/src/main/java/team3/view/WelcomeView.java`: welcome screen layout and button wiring
- `underground_guild/src/main/java/team3/controller/AppController.java`: welcome screen action handlers
- `underground_guild/src/main/java/team3/service/GameService.java`: placeholder responses for welcome actions
- `underground_guild/src/main/java/team3/persistence/SaveRepository.java`: load/save placeholder message
- `underground_guild/src/main/java/team3/util/AppMetadata.java`: title, subtitle, credits, and default status text

## Verify

```bash
cd underground_guild
mvn compile
mvn test
mvn javafx:run
```
