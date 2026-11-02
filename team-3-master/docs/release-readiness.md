# Release readiness review

Reviewed 2026-11-02. Project root: the inner `team-3-master` directory containing this file's parent and `underground_guild/`.

## Project summary

Underground Transport Guild is a Team 3 Java 21/JavaFX course project. Its service layer coordinates procedural maps, tunnels and depots, routes, cargo/passenger vehicles, production and demand, finances, and local saves. Original team attribution and historical records remain intact.

## Changes made

| File | Change |
| --- | --- |
| `README.md` | Replaced setup-only notes with overview, implemented features, stack, directory tree, installation, usage/controls, architecture, output, tests, limitations, future work, and attribution/license status. |
| `.gitignore` | Added Maven cache, save files, logs, crash files, and local credential/environment patterns. Removed blanket exclusions for `scripts/` and `AGENTS.md` so project tooling and guidance can be reviewed. |
| `.github/workflows/ci.yml` | Added Java 21 Maven verification and report uploads; read-only repository permissions. |
| `docs/release-readiness.md` | Added this validation record and publishing checklist. |
| `underground_guild/src/main/java/team3/service/GameService.java` | Updated service description and documented fixed-step frame accumulation and arrival handling. |
| `underground_guild/src/main/java/team3/model/GameMap.java` | Updated generation comment and documented multi-source breadth-first search. |
| `underground_guild/src/main/java/team3/persistence/SaveManager.java` | Documented slots, overwrite behavior, parameters, exceptions, and trusted-input requirement. |
| `underground_guild/src/main/java/team3/model/Depot.java` | Removed a commented-out unused import. |
| `underground_guild/src/main/java/team3/controller/AppController.java` | Replaced outdated skeleton description. |
| `underground_guild/src/main/java/team3/view/WelcomeView.java` | Replaced outdated startup-proof description. |

No gameplay statements, algorithms, dependency versions, public signatures, tests, assets, datasets, or project directories were changed. No source files were moved or removed. The existing POM already declares the actual runtime/test dependencies, so no second dependency manifest or environment example was needed.

## Validation results

- **Passed:** compiled all 37 application source files and 13 test source files with installed JDK 21.0.9.
- **Passed with environment workaround:** 64 tests, 0 failures, 0 errors, using `CI=true` and `-DforkCount=0`. The existing JavaFX toolkit smoke-test class does not execute under `CI=true`; Surefire reports zero tests for that class rather than a skipped-test count.
- **Blocked by environment:** standard `clean verify` compiled successfully but failed before executing tests because Surefire could not launch `cmd.exe`. The ordinary `mvn.cmd` launcher also could not run here. Maven was instead invoked through its installed Java Classworlds launcher. Dependencies required an approved run with access to the user Maven cache. The successful test run then used offline mode.
- **Not executed:** interactive application launch/gameplay, graphical button smoke tests, save round-trip verification, packaging completion, and the new GitHub workflow on a hosted runner.
- **Reviewed:** source comments, declared dependencies, ignore rules, file inventory/sizes, architecture, and placeholders. Static source inspection found no obvious hardcoded credential values or machine-specific user paths. This is a heuristic file review, not a current dependency vulnerability database audit or a full binary image metadata audit.
- **Unavailable:** tracked-file inspection, Git history secret scanning, and normal `git diff`: the supplied directory has no `.git` metadata. Git returned “not a git repository.” A temporary-backup comparison also could not be completed because the temporary location was unavailable in a subsequent execution context. Modified code sections were reviewed directly; source edits are comments only.

Equivalent successful Maven goals/options on a normal installation (PowerShell):

```powershell
$env:CI = 'true'
mvn -B -o -f underground_guild/pom.xml -DforkCount=0 test
Remove-Item Env:CI
```

Offline mode requires dependencies to be cached. Use the normal README commands on a working installation; the in-process option is a validation workaround and has not been added to project configuration.

## Remaining recommendations

1. Run `mvn -B -f underground_guild/pom.xml clean verify` on a normal Java 21/Maven setup, then perform the README's graphical smoke checks.
2. Review save deserialization before exchanging save files. There is no object-input filter or versioned schema; overwrites are not atomic and service subtotals reset after loading.
3. Reconcile vehicle purchase text with effective model values; connect settings and technology UI only as a separate functional change.
4. Check current dependency advisories and enable GitHub dependency alerts after upload. Versions were preserved to avoid an untested runtime upgrade.
5. Confirm team permission, course publication policy, and license choice. No LICENSE was created because the license has not been confirmed.
6. If a full Git clone exists elsewhere, inspect its complete history for secrets before publishing; this archive cannot establish history safety. No rotation was identified as necessary from the inspected text files.

## GitHub publishing checklist

Suggested repository name: `underground-transport-guild`

Suggested description: `JavaFX transport management simulation with procedural maps, route planning, cargo and passenger logistics, and an in-game economy.`

Suggested topics: `java`, `javafx`, `maven`, `simulation`, `transport-management`, `junit`, `course-project`.

Before publishing:

- Confirm contributor attribution, publication permission, and license.
- Complete the validation above and inspect everything being staged, including wireframes and historical planning documents containing team/member references.
- Create an empty GitHub repository without generated files.
- Run the following yourself from the inner project root, replacing `YOUR_USERNAME`. These commands have **not** been executed by this cleanup.

```sh
git init -b main
git add .
git diff --cached --stat
git diff --cached --check
git diff --cached
# Inspect the staged files for secrets and unwanted artifacts before committing.
git commit -m "docs: prepare Underground Transport Guild for public release"
git remote add origin https://github.com/YOUR_USERNAME/underground-transport-guild.git
git push -u origin main
```

If publishing from an existing clone instead, retain its history and follow that repository's branch workflow rather than initializing a new one. After pushing, check GitHub Actions, set the description/topics, enable dependency alerts, and add genuine runtime screenshots after taking them.

## Suggested commit message

```text
docs: prepare Underground Transport Guild for public release

Expand setup, gameplay, architecture, and limitation documentation.
Document simulation and save contracts without changing gameplay.
Add GitHub Maven verification and improve local artifact exclusions.
Record validation evidence and remaining publishing checks.
```
