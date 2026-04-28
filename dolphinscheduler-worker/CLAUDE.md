# CLAUDE.md — dolphinscheduler-worker

The **Worker** server. Receives task-dispatch RPCs from the master, spins up the right task plugin, runs the physical task, and ships lifecycle events back. Multiple workers scale horizontally; each registers with the registry and advertises its worker group.

## Entry point

`WorkerServer` — `@SpringBootApplication`, implements `IStoppable`.

## Main package

`org.apache.dolphinscheduler.server.worker`

## Key sub-packages

- `server.worker.executor` — the bridge to `dolphinscheduler-task-executor`. `PhysicalTaskEngineDelegator`, `PhysicalTaskExecutorFactory`, `PhysicalTaskExecutorLifecycleEventReporter`.
- `server.worker.rpc` — `WorkerRpcServer` + operators implementing `IPhysicalTaskExecutorOperator`, `IStreamingTaskInstanceOperator`, `ITaskExecutorQueryClient` (all from `dolphinscheduler-extract-worker`).
- `server.worker.registry` — `WorkerRegistryClient`: ephemeral registration, health status reporting, worker-group membership.
- `server.worker.task` — task-context utilities shared by all task plugins.
- `server.worker.metrics` — Worker Micrometer metrics.
- `server.worker.config` — worker config + **load protection**.

## Load protection

`WorkerServerLoadProtection` watches CPU / memory / task-count and rejects new dispatches above thresholds (driven by properties in `application.yaml`). Rejected dispatches bounce back to the master, which picks a different worker.

## How a task runs

1. Master sends a `DispatchTaskRequest` → `WorkerRpcServer` receives.
2. `PhysicalTaskExecutorFactory` builds a `PhysicalTaskExecutor` (lives in `task-executor`) around the task plugin (from `task-plugin`).
3. `TaskExecutorContainer` runs it; lifecycle events land on the in-process bus.
4. `PhysicalTaskExecutorLifecycleEventReporter` publishes events back to master over RPC.

## Gotchas

- **Tasks run in the worker JVM** for most plugins (shell, SQL, Python, HTTP, …). Plugins that submit remotely (Spark, Flink, K8s, EMR) still need the worker to stay alive until the remote job completes — a worker restart mid-flight triggers the master's failover path.
- **AWS S3 integration is direct** (via `dolphinscheduler-storage-s3`) for distributed-task artifact handling. Credentials follow `dolphinscheduler-aws-authentication` rules.
- **`dolphinscheduler-yarn-aop`** is on the worker's classpath: it weaves `YarnClientImpl.submitApplication` to capture `ApplicationId`s into `appInfo.log`. If YARN-based task plugins (MR, Spark-on-YARN) lose their application ID, check AspectJ weaving is active.
- **Worker group names are not free-form** in practice: master-side scheduling decisions key off them. Coordinate naming with master operators.
- **Load protection thresholds live in `application.yaml`**, **not** in `dolphinscheduler-meter`. Meter defines the interface; worker owns the numbers.

## Configuration

`src/main/resources`:

- `application.yaml` — worker resource limits, executor thread pools, task-type enable list, load-protection thresholds.
- `logback-spring.xml`, `banner.txt`.

## Tests

`src/test/java` — unit tests for registry, RPC, load protection, connection-state handling. End-to-end workflows are exercised by `dolphinscheduler-e2e` and the master's integration tests.

## Related modules

- `dolphinscheduler-task-executor` — the lifecycle framework.
- `dolphinscheduler-task-plugin` — the actual task implementations (runtime classpath via `dolphinscheduler-task-all`).
- `dolphinscheduler-extract-worker` — RPCs this module implements.
- `dolphinscheduler-extract-master` / `-extract-alert` — RPCs this module calls.
- `dolphinscheduler-yarn-aop` — AspectJ weaving on the classpath.
- `dolphinscheduler-registry-all`, `dolphinscheduler-storage-api`, `dolphinscheduler-datasource-api`.
