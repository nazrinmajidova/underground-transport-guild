# Issue #59: Clean up presentation polish across secondary screens

## Issue
Update the supporting screens so they match the same visual style as the rest of the app.

## Implemented
- Replaced bracket-style labels on build, route, vehicle, and technology actions.
- Applied shared title, button, panel, muted text, item, and empty-state styles across supporting views.
- Updated save slots to use card-style rows.
- Updated settings controls to sit inside a panel.
- Cleaned route and vehicle management panels with light dividers and selected item highlighting.
- Improved economy rows with simple table-style alternating backgrounds.

## Files Changed
- `underground_guild/src/main/resources/styles.css`
- `underground_guild/src/main/java/team3/view/WelcomeView.java`
- `underground_guild/src/main/java/team3/view/SettingsView.java`
- `underground_guild/src/main/java/team3/view/LoadSaveView.java`
- `underground_guild/src/main/java/team3/view/BuildView.java`
- `underground_guild/src/main/java/team3/view/EconomyView.java`
- `underground_guild/src/main/java/team3/view/RouteManagementView.java`
- `underground_guild/src/main/java/team3/view/VehicleManagementView.java`
- `underground_guild/src/main/java/team3/view/TechnologyView.java`
- `underground_guild/src/test/java/team3/view/PanelButtonSmokeTest.java`

## Testing
- Run `mvn test` from `underground_guild`.

## Notes
- No new gameplay logic was added.
- The load/save screen keeps the existing load-only behavior because delete logic is not currently present.
- This branch builds on the shared stylesheet and dashboard polish branches.
