# CLAUDE.md — dolphinscheduler-tools

CLI tools shipped alongside the server. Each tool is a separate `@SpringBootApplication` main class, selected at runtime via a Spring profile passed on the command line. Wrapped by shell scripts in the tarball (`bin/`).

## Shipped tools

| Tool | Main class | Profile | Script |
|------|------------|---------|--------|
| Schema init / upgrade | `UpgradeDolphinScheduler` | `upgrade` | `bin/upgrade-schema.sh` |
| Lineage data migration | `MigrateLineage` | `migrate-lineage` | `bin/migrate-lineage.sh` |
| Resource data migration | `MigrateResource` | `migrate-resource` | `bin/migrate-resource.sh` |

## Main package

`org.apache.dolphinscheduler.tools`

## Key sub-packages

- `tools.datasource` — `UpgradeDolphinScheduler`, `DolphinSchedulerManager` (the upgrade brain).
- `tools.datasource.upgrader` — version-specific upgraders, one class per version bump (e.g. `V320DolphinSchedulerUpgrader`). Implements `DolphinSchedulerUpgrader`.
- `tools.lineage` — `MigrateLineage` + supporting code.
- `tools.resource` — `MigrateResource` + supporting code.

## How schema upgrade works

1. `DolphinSchedulerManager` checks whether the metadata DB has existing DolphinScheduler tables.
2. If empty → **init** path: runs `sql/dolphinscheduler_<dialect>.sql` from `dolphinscheduler-dao/src/main/resources/sql/`.
3. If populated → **upgrade** path: inspects the current version row, runs each `<version>DolphinSchedulerUpgrader` in sequence up to the target version.

Upgraders are discovered by scanning the `tools.datasource.upgrader` package.

## Gotchas

- **Adding a release version** means adding a new `V<version>DolphinSchedulerUpgrader` plus any DDL under `dolphinscheduler-dao/src/main/resources/sql/upgrade/<version>/`. Skipping either half silently produces a broken upgrade.
- **Upgraders must be idempotent in principle** but in practice are not — operators run them exactly once. Design accordingly; add guards only when re-running is a realistic scenario.
- **Do not reach into `dolphinscheduler-dao` entities from upgraders**. Upgrades run against older schemas where those entities may not map; use raw SQL through the dialect-aware helpers.
- **Spring profiles are required on the command line**. Running the jar with no profile boots an empty Spring context and does nothing useful.
- **Shell scripts source `dolphinscheduler_env.sh`** for env vars; when debugging, check whether the expected JDBC URL is exported.

## Configuration

`application.yaml` — primarily datasource config so the tool can connect.

## Tests

`src/test/java` uses Testcontainers (MySQL + PostgreSQL) to run end-to-end schema init + upgrade against a real DB. `SchemaUtilsTest` is the main suite.

## Related modules

- `dolphinscheduler-dao` — source of SQL scripts + entities (read-only here).
- `dolphinscheduler-storage-all` — needed by `MigrateResource` to read/write stored artifacts.
- `dolphinscheduler-dao-plugin` — dialect selection.
- `dolphinscheduler-dist` — packages the tool jar + scripts into the tarball.
