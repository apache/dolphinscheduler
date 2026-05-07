# CLAUDE.md — dolphinscheduler-master

The **Master** server. Owns workflow orchestration: consumes `Command` rows, runs the workflow state machine, dispatches tasks to workers over RPC, handles failover. Runs as a Spring Boot application, scales horizontally (multiple masters coordinate via the registry).

## Entry point

`MasterServer` — `@SpringBootApplication`, implements `IStoppable`. Default port set in `application.yaml`.

## Main package

`org.apache.dolphinscheduler.server.master`

## Key sub-packages

- `server.master.engine` — the workflow execution engine: command handlers, workflow/task state machines, lifecycle event handlers, event bus. **Where the orchestration logic actually lives.**
- `server.master.rpc` — `MasterRpcServer` + RPC implementations (`TaskInstanceControllerImpl`, workflow control, master-to-master). Implements the contracts from `dolphinscheduler-extract-master`.
- `server.master.cluster` — worker cluster view, load balancing, metadata tracking. Decides *which* worker a task is dispatched to.
- `server.master.registry` — `MasterRegistryClient`: masters' own registration + discovery.
- `server.master.failover` — master and worker failover: detects a dead peer and recovers in-flight workflows.
- `server.master.runner` — workflow and task execution contexts (per-workflow and per-task runtime state holders).
- `server.master.metrics` — Micrometer gauges/counters for master health.

## HA and coordination

- `MasterCoordinator` extends `AbstractHAServer` and elects a leader via the registry. Cron scheduling triggers only fire on the leader.
- `MasterRegistryClient` registers an ephemeral node; peers receive a disconnect event and trigger failover.

## Extension points

Exposed interfaces:

- `ITaskGroupCoordinator`, `IWorkflowSerialCoordinator` — pluggable concurrency/serialization strategies.
- `IWorkflowRepository` — where workflow runtime state lives (defaults to in-memory + DB).
- `ILifecycleEventHandler`, `ILifecycleEventType` — add a new lifecycle event without editing the core state machine.

## Gotchas

- **Command-driven execution**. Nothing runs until a row is inserted into the `t_ds_command` table. If a workflow "does nothing" after a click, follow the trail: controller → `CommandService.insertCommand` → master command consumer → `WorkflowEngine`.
- **State-machine pattern is central**. Do not sneak an ad-hoc state transition into a service; add a lifecycle event and handler so the whole engine sees it.
- **`delight-nashorn-sandbox`** is used for unsafe scripting (e.g. conditional branch expressions). Upgrading the dep is sensitive — test the condition/switch task flows.
- **Scheduler integration via `SchedulerApi`** (from `dolphinscheduler-scheduler-plugin`). The only current impl is Quartz — but do not couple directly to `Scheduler` (Quartz).
- **Cross-master coordination uses the registry**, not RPC. Adding a new coordination primitive → put the key scheme in `server.master.cluster` and document it.
- **Failover is the highest-risk code path**. Any change in `server.master.failover` must be exercised against `AbstractMasterIntegrationTestCase` scenarios.
- **Heavy use of async tasks** via event buses. Do not introduce `Thread.sleep` in handlers — publish a delayed event instead.

## Configuration

`src/main/resources`:

- `application.yaml` — server port, worker-group defaults, timeouts, cluster settings.
- `logback-spring.xml`, `banner.txt`.

## Tests

`src/test/java` — unit + integration. Integration tests extend `AbstractMasterIntegrationTestCase`; they simulate distributed scenarios including failover.

## Related modules

- `dolphinscheduler-extract-master` — the RPCs this module implements.
- `dolphinscheduler-extract-worker` — the RPCs this module calls.
- `dolphinscheduler-task-executor` — shared task-lifecycle event model (lifecycle events received from workers).
- `dolphinscheduler-service` — `ProcessService` + `CommandService`.
- `dolphinscheduler-registry-all`, `dolphinscheduler-scheduler-all`, `dolphinscheduler-storage-api`, `dolphinscheduler-datasource-api` — runtime deps.
- `dolphinscheduler-eventbus` — in-process event bus inside the engine.
