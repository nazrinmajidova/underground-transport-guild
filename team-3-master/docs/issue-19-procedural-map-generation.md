# Issue #19: Procedural Map Generation

This note records what was done for issue `#19`.

## Issue Summary

Implement procedural generation so each new game starts with a playable underground map containing settlements, industrial facilities, and starter tunnels.

## Completed

- Added configurable generation settings for settlement counts, facility counts, and starter tunnels
- Added deterministic-testable constructor support with an injected `Random`
- Added settlement generation with `3x3` footprints
- Added Mine, Farm, and Forge generation with `2x2` footprints
- Added overlap-safe placement helpers so generated hubs do not collide
- Added starter tunnel placement near generated hubs
- Added automated tests covering generated counts, footprint overlap safety, and starter tunnel existence

## File Map

- `underground_guild/src/main/java/team3/model/Hub.java`: hub footprint support
- `underground_guild/src/main/java/team3/model/Settlement.java`: default settlement footprint
- `underground_guild/src/main/java/team3/model/IndustrialFacility.java`: default facility footprint
- `underground_guild/src/main/java/team3/model/GameMap.java`: generation settings, placement logic, overlap checks, and starter tunnels
- `underground_guild/src/test/java/team3/model/GameMapTest.java`: procedural generation verification

## Verify

```bash
cd underground_guild
mvn clean compile
mvn test
```

## Notes

- Environmental tile modifiers are still intentionally deferred to the later tile-effects issue.
- The current generator focuses on playability and stable downstream integration rather than visual variety.
