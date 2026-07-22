# CLAUDE.md — dolphinscheduler-meter

Metrics collection + Prometheus exposure + server-load-protection primitives. Auto-configured via Spring Boot so that any server (master, worker, api, alert) gets `/actuator/prometheus` for free just by depending on this module.

## Main package

`org.apache.dolphinscheduler.meter`

## Key sub-packages

- `meter.metrics` — `MetricsProvider`, `SystemMetrics`, `DefaultMetricsProvider`.
- `meter.loadprotection` (if present) — `ServerLoadProtection`, `BaseServerLoadProtection`.

## Key classes

- `MeterAutoConfiguration` — Spring Boot auto-configuration entry; pulled in by any server that puts this module on the classpath.
- `MetricsProvider` — SPI for custom metrics suppliers.
- `SystemMetrics` — CPU / memory / disk via Micrometer.
- `ServerLoadProtection` — interface the worker uses to reject new tasks under load; the default impl reads thresholds from config.

## Gotchas

- **Jetty, not Tomcat**. This module (and everything depending on it) excludes `spring-boot-starter-tomcat` and brings `spring-boot-starter-jetty`. Introducing a dependency that transitively re-adds Tomcat will trigger port-binding conflicts at runtime — use `<exclusions>`.
- Grafana dashboards live under `grafana/` and `grafana-demo/` — **example-only**, excluded from the jar. Production operators customize their own.
- The server-load-protection thresholds are consumed by `dolphinscheduler-worker` (`WorkerServerLoadProtection`). Changes to the interface here cascade to worker-side overrides.
- Prometheus scrape endpoint is at `/actuator/prometheus` (Spring Boot Actuator default). Auth is applied by `dolphinscheduler-actuator-authentication` when present on the classpath.

## Configuration

Consumers provide `management.endpoints.web.exposure.include=prometheus,health,info,metrics` in their `application.yaml`. This module does not ship its own YAML.

## Tests

Minimal — mostly auto-config wiring.

## Related modules

- `dolphinscheduler-common` — compile dep.
- `dolphinscheduler-actuator-authentication` (in `dolphinscheduler-authentication/`) — secures the actuator endpoints this module exposes.
- Every server module includes meter for metrics.
