# CLAUDE.md — dolphinscheduler-registry

Pluggable registry abstraction for service discovery, metadata storage, ephemeral node management, and distributed locks. Backends: Zookeeper (default), Etcd, JDBC.

**This directory is a Maven parent POM — no code lives here directly.**

## Sub-modules

- **`dolphinscheduler-registry-api`** — the `Registry` SPI interface, `Event`, `ConnectionListener`, `SubscribeListener`. Depend on this if you want to *consume* the registry.
- **`dolphinscheduler-registry-plugins`** — parent of the three concrete implementations:
  - `dolphinscheduler-registry-zookeeper` (default, uses Curator)
  - `dolphinscheduler-registry-etcd`
  - `dolphinscheduler-registry-jdbc` (uses the main DolphinScheduler DB; useful when Zookeeper/Etcd aren't available)
  - `dolphinscheduler-registry-it` (integration tests exercising all three)
- **`dolphinscheduler-registry-all`** — uber module that bundles every implementation; depended on by the servers so a user can switch registry via config without touching pom.xml.

## SPI contract

`Registry` (in `-api`):
- Connection lifecycle: `start`, `isConnected`.
- KV: `put`, `get`, `delete`.
- Subscription: `subscribe(path, listener)`.
- Locking: `lock(key)`, `tryLock`, `unlock`.
- Connection state: `addConnectionStateListener`.

## Which backend is active at runtime?

Selected by Spring properties (`registry.type=zookeeper|etcd|jdbc`) — implementations are `@ConditionalOnProperty`. Only one `Registry` bean is created per JVM.

## Gotchas

- **Zookeeper version is profile-driven**: root POM defines `zk-3.8` (default) and `zk-3.4` Maven profiles that flip Curator + Zookeeper versions (see `dolphinscheduler-bom/pom.xml`). Do not pin a Zookeeper version in this module.
- **Ephemeral nodes on disconnect**: worker/master/alert register as ephemeral; a long ZK session loss triggers failover in master. Changing registration TTL semantics here affects the HA story — cross-check with `dolphinscheduler-master`'s `MasterRegistryClient` and the failover package.
- **`jdbc` backend** shares the main DolphinScheduler datasource — do not point it at a separate DB.
- **Distributed lock keys**: whichever backend is active, `lock` semantics must be fair and re-entrant per the tests in `-it`. Don't reimplement lock logic in callers.

## Tests

`dolphinscheduler-registry-it` spins up each backend (Testcontainers for ZK/etcd/MySQL) and runs a shared contract suite. Any new `Registry` method must be exercised in `-it`.

## Related modules

- `dolphinscheduler-master` / `-worker` / `-api` / `-alert-server` — depend on `registry-all` and consume the `Registry` bean.
- `dolphinscheduler-service` — depends on `registry-api`.
