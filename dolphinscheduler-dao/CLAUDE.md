# CLAUDE.md — dolphinscheduler-dao

MyBatis-based data-access layer. Holds every entity, mapper, repository, and the SQL migration scripts shipped with a release.

## Main package

`org.apache.dolphinscheduler.dao`

## Key sub-packages

- `dao.entity` — persistence POJOs (`Command`, `WorkflowInstance`, `TaskInstance`, `User`, `Project`, `Tenant`, `DataSource`, `Cluster`, etc.). These map 1:1 to DB tables.
- `dao.mapper` — MyBatis `@Mapper` interfaces. One per table/entity (`CommandMapper`, `WorkflowInstanceMapper`, `TaskInstanceMapper`, …).
- `dao.repository` — higher-level repository abstractions wrapping mappers; what `service`/`master` layers should call, **not** the mappers directly.
- `dao.model` — DAO-specific DTOs (query projections, aggregates).
- `dao.utils` — SQL helpers.

## SQL schema

`src/main/resources/sql/`:

- `dolphinscheduler_mysql.sql`, `dolphinscheduler_postgresql.sql`, `dolphinscheduler_h2.sql` — fresh install.
- `upgrade/<version>/` — version-to-version migration DDL. Pairs with the upgraders in `dolphinscheduler-tools`.

## Gotchas

- **Mappers are not enough**: new code should go through `dao.repository`. Calling mappers directly from `master`/`service` is legacy and should not spread.
- **Three DB dialects supported** (MySQL, PostgreSQL, H2). Any mapper XML must be dialect-neutral or rely on the dialect abstraction in `dolphinscheduler-dao-plugin`. Grep for `<if test="databaseType == ...">` when in doubt — avoid adding more of those.
- **Entity field renames are breaking**: MyBatis maps DB columns to field names via `@TableField` / naming conventions. Rename the column OR the field — never only one.
- **Schema file excluded from jar**: `*.sql` is excluded from the built jar to keep it small; `dolphinscheduler-tools` repackages the SQL separately for the upgrade CLI.
- `HikariCP` is the pool. Don't switch to Druid — datasource-plugin uses Druid internally for its own plugin connections, but the core DolphinScheduler metadata DB uses HikariCP.

## Tests

`src/test/java` — a mix of unit and Testcontainers-based integration tests. The database-dialect-specific paths are also exercised by `dolphinscheduler-dao-plugin/*/src/test`.

## Related modules

- `dolphinscheduler-dao-plugin` — dialect implementations; depended on here as `dolphinscheduler-dao-plugin-all`.
- `dolphinscheduler-common` — utilities.
- `dolphinscheduler-task-api` — task-related entities cross-reference it.
- `dolphinscheduler-tools` — reads the SQL files here to run schema upgrades.
