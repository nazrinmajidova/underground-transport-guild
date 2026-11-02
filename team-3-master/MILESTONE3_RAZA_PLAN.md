# Focused Milestone 3 Execution Plan for Raza

## Scope And Issue Order

Only these issues are in scope for the current execution batch:

1. `#18` Implement the GameMap class
2. `#19` Implement procedural map generation
3. `#20` Render map tiles on screen
4. `#21` Implement camera panning and scrolling
5. `#54` Set up CI pipeline and automated build checks
6. `#22` Implement tunnel construction logic
7. `#24` Render hubs and depots on the map
8. `#34` Implement route visualization on the map

## Progress Checkpoint

- Completed and pushed: `#18`, `#19`, `#20`, `#21`, `#54`, `#22`, `#24`, `#34`
- Remaining issues from the original wider Milestone 3 assignment are intentionally out of scope for this focused batch

## Shared Repo Rules

- Never implement directly on `master`
- Every issue gets its own branch
- Every issue gets at least `5-6` commits using phase-based commits
- Every issue must be locally verified before the final commit and before push
- Every completed issue must add a short note under `docs/`

## Shared Branch Workflow

1. Pull the latest `master`
2. Create the issue branch
3. Implement the issue in phases
4. Run verification after each significant phase
5. Add the issue note in `docs/`
6. Run full verification
7. Push the issue branch
8. Open a merge request into `master`

## Shared Testing Gate

- After Java/model changes: `mvn compile`
- After behavior changes already covered by tests: `mvn test`
- After UI changes: `mvn javafx:run`
- Before the final issue commit: `mvn clean compile`, `mvn test`, and UI smoke checks where relevant

## Per-Issue Branches

- `#18`: `feature/gamemap-class`
- `#19`: `feature/map-generation`
- `#20`: `feature/map-rendering`
- `#21`: `feature/camera-panning`
- `#54`: `chore/gitlab-ci-pipeline`
- `#22`: `feature/tunnel-construction`
- `#24`: `feature/hub-depot-rendering`
- `#34`: `feature/route-visualization`

## Per-Issue Docs Notes

- `docs/issue-18-gamemap-class.md`
- `docs/issue-19-procedural-map-generation.md`
- `docs/issue-20-map-rendering.md`
- `docs/issue-21-camera-panning.md`
- `docs/issue-54-ci-pipeline.md`
- `docs/issue-22-tunnel-construction.md`
- `docs/issue-24-hub-depot-rendering.md`
- `docs/issue-34-route-visualization.md`

## Push Checklist

- Branch created from `master`
- Issue scope implemented only
- Commit sequence complete
- Local verification passed
- Docs note added
- Branch pushed to origin
