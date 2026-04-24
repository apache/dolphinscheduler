# CLAUDE.md — dolphinscheduler-spi

Service-Provider-Interface contracts shared between the core and all plugin families (task, datasource, alert, scheduler, storage, dao, registry). Intentionally kept **tiny** so that plugin authors can depend on it without pulling in the whole backend.

## Main package

`org.apache.dolphinscheduler.spi`

## Key sub-packages

- `spi.plugin` — `PrioritySPI`, `SPIIdentify`, `PrioritySPIFactory`. The priority-loading mechanism every plugin opts into.
- `spi.datasource` — `DataSourceClient`, `BaseConnectionParam`, `DataSourceChannel`, `DataSourceChannelFactory`. Core datasource abstractions (the plugin modules implement these).
- `spi.enums` — `DbType`, `DbConnectType`, `Flag`, `ResourceType`. Cross-module enums with wire-format implications.
- `spi.params` — `PluginParamsTransfer`, `InputParam`, `RadioParam`, `SelectParam`, etc. Describe the UI form a plugin exposes to the frontend.

## Key interfaces / classes

- `PrioritySPI` — root marker interface for everything loaded via `PrioritySPIFactory`; subtypes return a priority so multiple implementations of the same SPI can coexist and the highest priority wins.
- `DataSourceChannelFactory` — how datasource plugins register themselves.
- `PluginParamsTransfer` — used by API + UI to render plugin configuration forms dynamically.

## Gotchas

- **Minimal dependencies on purpose**: only `slf4j-api` is `compile`; `dolphinscheduler-common` is `provided`. Resist the urge to add rich deps here — plugin authors consume this module transitively and each dep forces a version on them.
- **Plugin priority semantics**: higher integer wins. When you see two plugin implementations collide at runtime, check each impl's `getIdentify().getPriority()`.
- **`DbType` is exposed to end users** via stored configuration; renaming a value is a DB migration, not just a refactor.

## Extension points

This **is** the extension-point module. Every plugin module consumes an SPI defined here or in `*-api` sub-modules of the plugin families (`task-api`, `datasource-api`, `dao-api`, etc.).

## Tests

Standard `src/test/java`.

## Related modules

- `dolphinscheduler-common` — consumed `provided` only.
- `dolphinscheduler-task-plugin` / `dolphinscheduler-datasource-plugin` / `dolphinscheduler-storage-plugin` / `dolphinscheduler-dao-plugin` / `dolphinscheduler-scheduler-plugin` — all implement SPIs rooted here.
