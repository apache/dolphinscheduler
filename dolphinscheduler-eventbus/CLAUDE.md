# CLAUDE.md — dolphinscheduler-eventbus

A **local, in-process** event-bus abstraction with optional delay-queue semantics. Used internally by master/worker/task-executor/alert to decouple producers from consumers without introducing a real message broker.

## What this is NOT

This is **not** a distributed event bus. Events do not cross JVM boundaries. For cross-process notification use the RPC interfaces in `dolphinscheduler-extract`.

## Main package

`org.apache.dolphinscheduler.eventbus`

## Key types

- `IEvent` — marker interface for every event.
- `IEventBus<T extends IEvent>` — producer/consumer contract: `publish`, `poll`, `take`, `peek`, `remove`, `isEmpty`.
- `AbstractDelayEvent` — event with a fire-at timestamp (implements `java.util.concurrent.Delayed`).
- `AbstractDelayEventBus` — default in-memory implementation backed by `DelayQueue` (or plain `BlockingQueue` for non-delayed buses).

## Gotchas

- `AbstractDelayEvent.getDelay` is called by `DelayQueue` on every comparison. Keep it cheap and **side-effect free** — no DB reads, no clock skew corrections.
- Events are lost if the JVM restarts — the bus has no persistence. Consumers must be designed to re-derive state from the DB on startup.
- Subclasses of `AbstractDelayEventBus` are consumed as Spring beans in master/worker. Names (bean type) matter — grep before renaming.

## Typical usage pattern

Each subsystem defines its own bus: `TaskExecutorEventBus` in `task-executor`, `AlertEventLoop` + `AlertEventPendingQueue` in `alert-server`, lifecycle-event bus in `master.engine`. The pattern is: domain-specific `IEvent` hierarchy → dedicated `AbstractDelayEventBus` subclass → single producer thread per bus.

## Tests

Standard `src/test/java`.

## Related modules

- `dolphinscheduler-task-executor` — heaviest consumer; defines the task-lifecycle event hierarchy on top of this.
- `dolphinscheduler-master` — uses it inside the workflow execution engine.
- `dolphinscheduler-alert` — the alert server's event loop is built on top of this.
