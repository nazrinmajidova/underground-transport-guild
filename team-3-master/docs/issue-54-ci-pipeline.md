# Issue #54: CI Pipeline And Build Checks

This note records what was done for issue `#54`.

## Issue Summary

Set up a GitLab CI pipeline so pushes and merge requests automatically run the Maven build and test flow for the project.

## Completed

- Added a root `.gitlab-ci.yml`
- Configured the pipeline to use Java 21 with Maven
- Added separate build and test stages
- Added Maven dependency caching
- Added workflow rules for push and merge request pipelines
- Added artifacts for build outputs and Surefire test reports
- Updated the README so the repo workflow matches the CI behavior
- Validated the pipeline through the GitLab project CI lint API

## File Map

- `.gitlab-ci.yml`: GitLab CI pipeline definition
- `README.md`: short note about CI running on pushes and merge requests

## Verify

```bash
cd underground_guild
mvn clean compile
mvn test
```

## Notes

- The project-scoped GitLab CI lint API returned a valid pipeline configuration before the branch was pushed.
- The branch push should trigger the actual remote pipeline for final GitLab-side confirmation.
