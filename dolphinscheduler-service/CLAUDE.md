# CLAUDE.md — dolphinscheduler-service

Business-logic layer sitting **between** `dao` and the server modules (`api`, `master`, `worker`, `alert-server`). Owns orchestration concerns: workflow lifecycle, command processing, cron scheduling, alert dispatch, parameter expansion.

## Main package

`org.apache.dolphinscheduler.service`

## Key sub-packages

- `service.process` — `ProcessServiceImpl`: the single largest service, used by master & api. It is the de-facto orchestration facade; nearly every workflow operation passes through it.
- `service.command` — `CommandServiceImpl`: enqueue/consume `Command` rows that trigger workflow runs (start, restart, pause, recover).
- `service.cron` — `CronService`: cron parsing + next-fire calculation (uses the `cron-utils` lib, **not** Quartz expressions directly).
- `service.alert` — `AlertService`, `AlertNotificationService`: bridge between workflow/task state changes and the alert server.
- `service.expand` — `CuringParamsServiceImpl`: expand `${paramName}` placeholders in task parameters at execution time.
- `service.utils` — general service-layer helpers.
- `service.model` — service-layer DTOs.

## Gotchas

- **`ProcessServiceImpl` is a god-class** by design; it survived multiple refactor attempts. If you are about to add a new method, consider whether a thinner, purpose-specific service would be more appropriate — but expect most historical changes to land here.
- **Service methods are transactional**: `@Transactional(rollbackFor = Exception.class)` is sprinkled on write methods. Adding a new write method without this annotation is almost always a bug.
- **Depends on RPC interfaces, not implementations**: this module pulls `dolphinscheduler-extract-master` / `-extract-worker` — it can *call* master/worker RPCs but does not implement them. The real implementations live in those server modules.
- **Cron semantics**: DolphinScheduler uses Quartz under the hood for triggering, but cron *parsing* here uses `cron-utils`. The two have slightly different DOW (day-of-week) conventions; see `CronService` for the normalization.
- **Parameter expansion order**: project params → workflow params → task params → built-in params. Changing the precedence in `CuringParamsServiceImpl` is a contract change visible to every task.

## Configuration

None shipped; consumers pass standard Spring profiles.

## Tests

Standard `src/test/java`. Some tests mock `curator-test` for registry; most are pure unit.

## Related modules

- `dolphinscheduler-dao` — primary dependency.
- `dolphinscheduler-spi`, `dolphinscheduler-registry-api` — plugin + service discovery.
- `dolphinscheduler-extract-master` / `-extract-worker` — RPC clients.
- `dolphinscheduler-task-api` — task-related DTOs.
- Consumers: `dolphinscheduler-api`, `dolphinscheduler-master`.
