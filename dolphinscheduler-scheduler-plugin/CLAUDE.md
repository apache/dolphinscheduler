# CLAUDE.md — dolphinscheduler-scheduler-plugin

Plugin family for the **cron-trigger scheduler** that fires workflows at their scheduled times. Only one implementation today (Quartz), but the SPI is in place so alternatives can be added.

**This directory is a Maven parent POM.**

## Sub-modules

- **`dolphinscheduler-scheduler-api`** — the `SchedulerApi` interface (`start`, `insertOrUpdateScheduleTask`, `deleteScheduleTask`, `close`) + related DTOs.
- **`dolphinscheduler-scheduler-quartz`** — Quartz-based implementation. `QuartzScheduler` (SchedulerApi impl), `QuartzSchedulerAutoConfiguration`, `QuartzSchedulerDataSourceAutoConfiguration`, `QuartzTriggerBuilder`.
- **`dolphinscheduler-scheduler-all`** — uber module consumed by master.

## Who triggers what

1. User defines a schedule in the UI → API persists a row.
2. Master's leader starts the scheduler; `SchedulerApi.insertOrUpdateScheduleTask` registers the Quartz job.
3. When Quartz fires, it inserts a `t_ds_command` row (WorkflowScheduler → CommandService). Master's command consumer picks it up and runs the workflow.

## Gotchas

- **Only the master leader runs the scheduler**. Non-leader masters hold `SchedulerApi.close()`-like quiet state. Electing a new leader must re-register all schedules.
- **Separate Quartz datasource**: `QuartzSchedulerDataSourceAutoConfiguration` configures its own datasource pointing at the same DB as DolphinScheduler but with Quartz's own tables (`QRTZ_*`). Upgrades must run Quartz's own DDL as well as DolphinScheduler's.
- **Job key scheme**: `jobKey` concatenates `projectId_scheduleId`. Renaming this scheme breaks in-flight scheduled tasks — avoid.
- **Cron parsing happens in two places**: `dolphinscheduler-service/cron` uses `cron-utils` for *display* (next fire time in UI); Quartz internally uses Quartz cron syntax for *firing*. The two are mostly compatible but DOW conventions differ slightly — always validate with both.
- **Do not couple to Quartz types outside this module**. Callers must depend on `SchedulerApi`, never on `org.quartz.Scheduler` directly.

## Extension points

A new scheduler implementation would: (1) create a sibling sub-module; (2) implement `SchedulerApi`; (3) register via Spring Boot `spring.factories` / `AutoConfiguration.imports`; (4) add itself to `scheduler-all`.

## Tests

Inside `-quartz/src/test/java`.

## Related modules

- `dolphinscheduler-master` — consumes `scheduler-all`.
- `dolphinscheduler-service` — uses `CronService` for next-fire-time display (independent of this module's runtime).
