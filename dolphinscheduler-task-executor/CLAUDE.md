# CLAUDE.md — dolphinscheduler-task-executor

Reusable task-execution framework. Defines how a worker **runs**, **tracks**, and **reports** a task instance — independent of any specific task type (shell, Spark, …). The worker (`dolphinscheduler-worker`) embeds this module; the task-type behavior comes from `dolphinscheduler-task-plugin`.

## Main package

`org.apache.dolphinscheduler.task.executor`

## Key sub-packages

- `task.executor.container` — execution models: `ExclusiveThreadTaskExecutorContainer` (one thread per task) and `SharedThreadTaskExecutorContainer` (thread-pooled, for lightweight tasks). The container owns the lifecycle.
- `task.executor.eventbus` — `TaskExecutorEventBus` + `TaskExecutorEventBusCoordinator`; an in-process delay bus for task-lifecycle transitions (built on `dolphinscheduler-eventbus`).
- `task.executor.listener` — lifecycle listeners (start, finish, fail, timeout, kill).
- `task.executor.operations` — operation requests/responses carried over RPC (dispatch, kill, pause, reassign). Sibling to the wire types in `dolphinscheduler-extract-worker`.
- `task.executor.dto` — task state, execution context DTOs.
- `task.executor.exceptions` — `TaskExecutionException` and friends.
- `task.executor.worker` — worker-thread implementations backing the containers.

## Key types

- `ITaskEngine` + `TaskEngine` — facade the worker uses to submit/control tasks.
- `TaskEngineBuilder` — constructs a configured `TaskEngine` at startup.
- `TaskExecutorRepository` — in-memory registry of running tasks.
- `AbstractTaskExecutor`, `AbstractTaskExecutorContainer` — extension points for new execution strategies.
- `TaskExecutorLifecycleEventRemoteReporter` — ships lifecycle events back to master via RPC.

## Extension points / SPI

Exposed interfaces (consumed by `dolphinscheduler-worker`):

- `ITaskExecutor`, `ITaskExecutorContainer`, `ITaskExecutorContainerProvider`, `ITaskExecutorFactory`.
- `ITaskExecutorEventBusCoordinator`, `ITaskExecutorLifecycleEventListener`.
- `ITaskExecutorRepository`, `ITaskExecutorStateTracker`, `ITaskExecutorWorker`.

## Gotchas

- **No Spring here**. This module is a plain library with no `@Component` / `@Configuration`. All beans are wired inside `dolphinscheduler-worker`. Do not add Spring deps.
- **State transitions are table-driven** via `TaskExecutorStateMappings`. Adding a new state or transition in one place without updating the mapping table will silently drop events.
- **No `src/main/resources`** — adding config here is wrong; config belongs in the hosting server (worker).
- **Lifecycle events are the source of truth** for master's view of task state. If master thinks a task is stuck, the most likely cause is a lifecycle event that was never published from the executor here.
- **Module has no tests** — coverage comes from `dolphinscheduler-worker` integration tests. Changes here need worker-side verification.

## Related modules

- `dolphinscheduler-eventbus` — underlies the task-lifecycle bus.
- `dolphinscheduler-task-api` — task DTOs/contracts.
- `dolphinscheduler-common` — utilities.
- `dolphinscheduler-worker` — the one and only consumer today.
- `dolphinscheduler-master` — receives lifecycle events (via RPC) from this module's reporter.
