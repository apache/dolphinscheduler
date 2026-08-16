# CLAUDE.md — dolphinscheduler-alert

Alert / notification subsystem. Master and worker publish alert events (task failure, timeout, SLA breach, …); this subsystem evaluates them and dispatches via configured channel plugins (email, webhook, Feishu, DingTalk, PagerDuty, …).

**This directory is a Maven parent POM.**

## Sub-modules

- **`dolphinscheduler-alert-server`** — the runnable alert server. `@SpringBootApplication` `AlertServer` + HA coordinator (`AlertHAServer`) + RPC server (`AlertRpcServer`) + event loop.
- **`dolphinscheduler-alert-plugins`** — parent of the concrete channel plugins:
  - `dolphinscheduler-alert-email`, `-http`, `-feishu`, `-dingtalk`, `-wechat`, `-webexteams`, `-pagerduty`, `-telegram`, `-slack`, `-script`, etc. (see directory listing for the current set). Each plugin implements the `AlertPlugin` SPI and is loaded dynamically at runtime.

## How alerting works (end-to-end)

1. Master/worker calls into `dolphinscheduler-extract-alert` RPC with an `AlertRequest`.
2. `AlertRpcServer` receives it, persists/enqueues onto `AlertEventPendingQueue`.
3. `AlertEventFetcher` pulls pending events in `AlertEventLoop`.
4. `AlertSender` looks up the alert group's configured plugins, invokes each.
5. Delivery result is recorded to the DB for audit.

## Extension points

- **`AlertPlugin` SPI** — new channel? Implement the interface in a new sub-module of `dolphinscheduler-alert-plugins` and it will be discovered by `AlertPluginManager`.
- Alert rule logic is currently fixed (type-based); extending rules means editing `AlertEventLoop` / `AlertSender`.

## Gotchas

- **Alert server is separate from master/worker** by design. Running it as an embedded part of master is not supported — it has its own HA, its own port, its own `application.yaml`.
- **`AlertEventPendingQueue` is DB-backed** (not just in-memory) so restarting the alert server does not lose queued alerts.
- **HA pattern mirrors master/worker**: only the leader pulls from the queue; others stand by.
- **Plugin config is per-alert-group**, stored in the DB. Adding a plugin implementation does not enable it for anyone until an admin creates an alert group referencing it.
- **Channel-specific rate limits / retries** live *inside* each plugin, not in the server. Don't add a global retry loop in `AlertSender`.

## Tests

- `alert-server/src/test/java` — unit tests for config, RPC, event queue, sender.
- Each plugin has its own `src/test/java` with mocked channel calls.

## Related modules

- `dolphinscheduler-extract-alert` — RPC contracts this server implements.
- `dolphinscheduler-dao` — persists alert events + audit.
- `dolphinscheduler-registry-all`, `dolphinscheduler-meter`, `dolphinscheduler-spi`.
- Callers: `dolphinscheduler-master`, `dolphinscheduler-worker`.
