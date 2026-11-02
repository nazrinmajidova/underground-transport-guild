# Issue #57: Centralize JavaFX styling in one stylesheet

## Issue
Add a shared stylesheet for the JavaFX views and move repeated inline colors, font sizes, padding, and button styles into reusable CSS classes.

## Implemented
- Added `styles.css` under `underground_guild/src/main/resources`.
- Attached the stylesheet once from `Main.java`.
- Added shared classes for titles, muted text, panels, buttons, form controls, dividers, status text, and money states.
- Moved straightforward title, screen, panel, button, muted text, and money styling to shared CSS classes.

## Files Changed
- `underground_guild/src/main/resources/styles.css`
- `underground_guild/src/main/java/team3/Main.java`
- `underground_guild/src/main/java/team3/view/WelcomeView.java`
- `underground_guild/src/main/java/team3/view/EconomyView.java`

## Testing
- Run `mvn test` from `underground_guild`.

## Notes
- Existing navigation and gameplay behavior is unchanged.
- More screen-specific polish is handled in the follow-up UI issues.
