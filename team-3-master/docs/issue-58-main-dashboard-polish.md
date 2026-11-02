# Issue #58: Polish the main game dashboard layout

## Issue
Improve the presentation quality of the main gameplay screen while keeping the current functionality.

## Implemented
- Styled the top status bar with a subtle background and light bottom border.
- Made the bottom menu buttons use a consistent width, height, and shared style.
- Wrapped the side information area in a styled panel with a subtle shadow.
- Moved map and hub label styling into shared CSS classes.
- Improved hub label readability with a semi-transparent dark background.

## Files Changed
- `underground_guild/src/main/resources/styles.css`
- `underground_guild/src/main/java/team3/view/MainGameView.java`
- `underground_guild/src/main/java/team3/view/GameMapView.java`

## Testing
- Run `mvn test` from `underground_guild`.

## Notes
- Dashboard behavior, map interaction, route creation, and simulation controls are unchanged.
- This branch builds on the shared stylesheet added for issue #57.
