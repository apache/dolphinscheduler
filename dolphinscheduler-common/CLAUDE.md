# CLAUDE.md — dolphinscheduler-common

Foundation utility library. Every other backend module in the repo transitively depends on this one, so treat it as the lowest layer: no internal dependencies, minimal behavior, stable API.

## What lives here

- Shared utilities: string / date / file / map / JSON helpers, SSL, encryption, thread helpers.
- Global enums and constants (workflow status, task types, flag values, date formats).
- Shell command execution primitives (`ShellExecutor`, `AbstractShell`).
- Property delegation layer (`IPropertyDelegate`, `ImmutablePropertyDelegate`) used to read YAML/properties uniformly.
- Generic DAG data structure (`common.graph.DAG`) — used by master to traverse workflow graphs.
- Lifecycle interface (`IStoppable`) — implemented by every long-lived server (Master, Worker, Alert, API).

## Main package

`org.apache.dolphinscheduler.common`

## Key sub-packages

- `common.utils` — stateless helpers; the most-touched package in the module.
- `common.constants` — `Constants`, `TenantConstants`, `DateConstants`.
- `common.enums` — `WorkflowExecutionStatus`, `TaskExecutionStatus`, `Flag`, etc. Changes here ripple across every module — be careful when renaming.
- `common.config` — property-source abstractions consumed by Spring `@ConfigurationProperties` classes elsewhere.
- `common.shell` — blocking shell executor, used by task plugins.
- `common.graph` — generic DAG; not workflow-specific despite the usage.
- `common.lifecycle` — `IStoppable` only.

## Extension points

None. This module exposes no SPI; it is a leaf utility library.

## Configuration

`src/main/resources`:

- `common.properties` — logging + cloud-storage credentials read by services.
- `remote-logging.yaml` — config for remote log fetch.
- `resource-center.yaml` — resource-center defaults.

## Gotchas

- Cloud-storage SDKs (Aliyun OSS, Huawei OBS, Tencent COS, Azure Blob) are declared with `<optional>true</optional>` / broad exclusions. Do **not** rely on them being on the classpath from here — the concrete storage plugins in `dolphinscheduler-storage-plugin` bring them in.
- Enum renames are breaking: workflow/task status enums are serialized into the database via MyBatis type handlers in `dolphinscheduler-dao`. Always grep for the enum value before renaming.
- `IStoppable` is the canonical shutdown contract across every server; do not invent a new lifecycle interface.

## Tests

Standard `src/test/java`. No special infra.

## Related modules

- `dolphinscheduler-spi` — uses `common` as `provided`; keep new SPI-facing types minimal here.
- Every `server-*` / `dao` / `service` / `plugin` module depends on this.
