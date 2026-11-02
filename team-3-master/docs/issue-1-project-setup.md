# Issue #1: Project Setup And Repository Rules

This note records what was done for issue `#1`.

## Issue Summary

Set up the initial project structure so the team can build, run, and extend the game safely.

## Completed

- Added the Maven JavaFX project in `underground_guild/`
- Standardized the project on Java 21
- Added a root `.gitignore`
- Created the starter layered package structure under `team3`
- Updated the repository README

## Out Of Scope

- Gameplay logic
- Real navigation flow
- Full domain model
- Save/load implementation

## File Map

- `README.md`: quick project overview and run commands
- `.gitignore`: ignored local/build files
- `underground_guild/pom.xml`: Maven and JavaFX config
- `underground_guild/src/main/java/team3/Main.java`: app entrypoint
- `team3.model`: domain/state classes
- `team3.view`: UI classes
- `team3.controller`: UI flow handlers
- `team3.service`: application services
- `team3.persistence`: save/load code
- `team3.util`: shared helpers/constants

## Verify

```bash
cd underground_guild
mvn compile
mvn test
```
