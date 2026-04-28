# CLAUDE.md — dolphinscheduler-datasource-plugin

Plugin family for **datasources** — the configurable DB/warehouse/query-engine connections a user can register (MySQL, PostgreSQL, Hive, Trino, Redshift, Snowflake, …). Used by the SQL-style task plugins, the API for connection testing, and the data-lineage tool.

**This directory is a Maven parent POM.**

## Sub-modules

### Framework

- **`dolphinscheduler-datasource-api`** — SPI and base types: `DataSourceProcessor`, `AbstractDataSourceProcessor`, `BaseDataSourceParamDTO`, `BaseHdfsConnectionParam`, and the `DataSourcePluginManager` that loads plugins.
- **`dolphinscheduler-datasource-all`** — uber module bundling every plugin; depended on by `master`, `worker`, `api` so they can use any datasource at runtime.

### Concrete plugins (28 sub-modules)

Relational: `-mysql`, `-postgresql`, `-oracle`, `-sqlserver`, `-db2`, `-oceanbase`, `-dameng`, `-hana`, `-azure-sql`, `-vertica`.

OLAP / warehouses: `-clickhouse`, `-doris`, `-starrocks`, `-redshift`, `-snowflake`, `-databend`, `-dolphindb`.

Big-data engines: `-hive`, `-spark`, `-presto`, `-trino`, `-kyuubi`, `-athena`.

Cloud / other: `-sagemaker`, `-k8s`, `-aliyunserverlessspark`, `-ssh`, `-zeppelin`.

## SPI contract

Two interfaces are **both** implemented by each plugin:

1. `DataSourceChannelFactory` (from `dolphinscheduler-spi`, loaded via `PrioritySPIFactory`) — creates `DataSourceChannel`s for low-level connection handling.
2. `DataSourceProcessor` (from `datasource-api`, loaded via standard `ServiceLoader`) — validates + builds connection params, handles the UI-form contract.

Both are wired into plugins with `@AutoService(...)`. Discovery happens in `DataSourcePluginManager`.

## How it plugs into the rest of the system

- **API**: when the user opens the "Create Datasource" dialog, the API asks each `DataSourceProcessor` for its param descriptor and serves it to the UI.
- **Worker**: SQL-family task plugins (`task-sql`, `task-procedure`, `task-datax`, …) look up the configured datasource by id, ask `datasource-api` for a JDBC connection via the channel, run the statement.

## Gotchas

- **Dual SPI loading is intentional**: channels are priority-ordered (multiple providers can coexist); processors are keyed by `DbType` (one per type). Don't merge them.
- **`DbType` enum lives in `dolphinscheduler-spi`**. Adding a new datasource means: new enum value in spi + new plugin sub-module. Leaving the enum unchanged will cause `DataSourcePluginManager` to silently ignore the plugin.
- **Passwords are encrypted** via `PasswordUtils` in `common`. Never store or log the plaintext form; always go through `PasswordUtils.encodePassword` / `decodePassword`.
- **JDBC connection pooling** is **Druid** (not HikariCP). This is different from the metadata DB (`dolphinscheduler-dao` uses HikariCP). Be careful with which you're tuning.
- **Hive / Spark / Presto plugins pull massive dep trees** — exclusions in their poms are load-bearing. Adding a new version bump here can blow up the classpath.

## Tests

Each plugin has `src/test/java`. Most use an embedded H2 or a mocked driver; a few use Testcontainers (e.g. `-postgresql`, `-mysql`).

## Related modules

- `dolphinscheduler-spi` — base SPI (`DataSourceChannelFactory`, `DbType`).
- `dolphinscheduler-task-plugin` — primary consumer (SQL family plugins).
- `dolphinscheduler-api`, `dolphinscheduler-service` — consumers for configuration + validation.
- `dolphinscheduler-common` — `PasswordUtils`.
