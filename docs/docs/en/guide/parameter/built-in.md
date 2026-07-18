# Built-in Parameter

DolphinScheduler provides two kinds of built-in parameters that the system fills in automatically at run time, so you do not have to declare them yourself:

- **Basic built-in parameters** — a fixed set of `system.*` variables (task id, workflow name, business date, …), referenced with the `${variable}` syntax.
- **Derived (time) built-in parameters** — date/time expressions referenced with the `$[...]` syntax, which can be freely composed and shifted (e.g. `$[yyyy-MM-dd-1]`).

## Basic Built-in Parameter

|            Variable             |          Declaration Method          |                                           Meaning                                           |
|---------------------------------|--------------------------------------|---------------------------------------------------------------------------------------------|
| system.biz.date                 | `${system.biz.date}`                 | The day before the schedule time of the daily scheduling instance, the format is `yyyyMMdd` |
| system.biz.curdate              | `${system.biz.curdate}`              | The schedule time of the daily scheduling instance, the format is `yyyyMMdd`                |
| system.datetime                 | `${system.datetime}`                 | The schedule time of the daily scheduling instance, the format is `yyyyMMddHHmmss`          |
| system.task.execute.path        | `${system.task.execute.path}`        | The absolute path of current executing task                                                 |
| system.task.instance.id         | `${system.task.instance.id}`         | The instance id of current task                                                             |
| system.task.definition.name     | `${system.task.definition.name}`     | The definition name of current task                                                         |
| system.task.definition.code     | `${system.task.definition.code}`     | The definition code of current task                                                         |
| system.workflow.instance.id     | `${system.workflow.instance.id}`     | The instance id of the workflow to which current task belongs                               |
| system.workflow.definition.name | `${system.workflow.definition.name}` | The definition name of the workflow to which current task belongs                           |
| system.workflow.definition.code | `${system.workflow.definition.code}` | The definition code of the workflow to which current task belongs                           |
| system.project.name             | `${system.project.name}`             | The name of the project to which current task belongs                                       |
| system.project.code             | `${system.project.code}`             | The code of the project to which current task belongs                                       |

### About the business date (`system.biz.date` / `system.biz.curdate`)

`system.biz.curdate` is the **schedule time** of the instance, and `system.biz.date` is **one day before** it. This "minus one day" convention exists because a workflow scheduled to run in the early morning usually processes the *previous* day's data.

The value of the schedule time depends on how the workflow is triggered:

- **Scheduled (timing) run**: the schedule time is the time configured by the timer (not the actual firing time).
- **Complement (backfill) run**: the schedule time is each business date in the backfill range, so `system.biz.date` shifts one day per generated instance.
- **Manual run without a schedule time**: the system uses the current time (`now`) minus one day as the business date.

### Example: printing every basic parameter

Create a Shell task and paste the script below. When it runs, every placeholder is replaced with the concrete value of that instance:

```shell
echo "biz.date        = ${system.biz.date}"
echo "biz.curdate     = ${system.biz.curdate}"
echo "datetime        = ${system.datetime}"
echo "execute.path    = ${system.task.execute.path}"
echo "task.instance   = ${system.task.instance.id}"
echo "task.def.name   = ${system.task.definition.name}"
echo "task.def.code   = ${system.task.definition.code}"
echo "wf.instance.id  = ${system.workflow.instance.id}"
echo "wf.def.name     = ${system.workflow.definition.name}"
echo "wf.def.code     = ${system.workflow.definition.code}"
echo "project.name    = ${system.project.name}"
echo "project.code    = ${system.project.code}"
```

A workflow instance scheduled for `2022-08-26 00:00:00` would print, for example:

```text
biz.date        = 20220825
biz.curdate     = 20220826
datetime        = 20220826000000
execute.path    = /tmp/dolphinscheduler/exec/process/<...>/<taskInstanceId>
task.instance   = 3096
...
```

## Derived (Time) Built-in Parameter

Besides the fixed `system.*` variables, DolphinScheduler supports a compact date/time template language written as `$[...]`. It lets you derive any date relative to the instance's **benchmark time**.

### The benchmark time

Every `$[...]` expression is evaluated against a single **benchmark time**, which is the instance's business/schedule time — the same value exposed as `${system.datetime}`. It is **not** the wall-clock time at which the task actually runs.

That means:

- For a **scheduled** run, `$[yyyyMMdd]` resolves to the scheduled date, so re-running or a delayed start does not change the result.
- For a **complement (backfill)** run, the benchmark time is each backfilled business date in turn, so `$[yyyyMMdd]` walks through the range.
- Only when no schedule time is available does the benchmark fall back to the current time.

This is what makes `$[...]` safe to use in idempotent, backfillable pipelines: the derived dates track the data date, not the run date.

### Format templates

`$[...]` accepts Java-style date patterns supported by DolphinScheduler's date formatter, and you can decompose and combine the pieces freely:

- `$[yyyyMMdd]`, `$[HHmmss]`, `$[yyyy-MM-dd]`, `$[yyyy-MM-dd HH:mm:ss]`, `$[yyyyMMddHHmmss]`, etc.

### Date arithmetic

You can add or subtract directly after the format. The unit is expressed through the arithmetic itself:

|       Goal       |           Expression            |
|------------------|---------------------------------|
| Next N years     | `$[add_months(yyyyMMdd,12*N)]`  |
| N years before   | `$[add_months(yyyyMMdd,-12*N)]` |
| Next N months    | `$[add_months(yyyyMMdd,N)]`     |
| N months before  | `$[add_months(yyyyMMdd,-N)]`    |
| Next N weeks     | `$[yyyyMMdd+7*N]`               |
| N weeks before   | `$[yyyyMMdd-7*N]`               |
| Next N days      | `$[yyyyMMdd+N]`                 |
| N days before    | `$[yyyyMMdd-N]`                 |
| Next N hours     | `$[HHmmss+N/24]`                |
| N hours before   | `$[HHmmss-N/24]`                |
| Next N minutes   | `$[HHmmss+N/24/60]`             |
| N minutes before | `$[HHmmss-N/24/60]`             |

> `N` denotes an integer or arithmetic expression; replace it with a concrete value, for example `$[yyyyMMdd-7*2]` for two weeks before. The `add_months` function takes the date format as its first argument and the month offset as its second, so it is the safe way to shift by months or years (which do not have a fixed number of days). Direct `+/-` arithmetic works on a day/hour/minute basis, which is why hours are expressed as `N/24` and minutes as `N/24/60` of a day.

### Business attribute functions

These functions return calendar-aware dates. Each takes a date format as the first argument; those that accept an offset take it (in the unit noted) as the second argument. Examples assume a benchmark date of `2022-08-26`.

|              Function               |                          Meaning                          |            Example → Result             |
|-------------------------------------|-----------------------------------------------------------|-----------------------------------------|
| `$[this_day(yyyy-MM-dd)]`           | Today (the benchmark date)                                | `2022-08-26` → `2022-08-26`             |
| `$[last_day(yyyy-MM-dd)]`           | Yesterday                                                 | `2022-08-26` → `2022-08-25`             |
| `$[year_week(yyyy-MM-dd)]`          | Week number of the year, week starts on Monday            | `2022-08-26` → `2022-34`                |
| `$[year_week(yyyy-MM-dd,N)]`        | Week number, week starts on day N                         | when `N=5`, `2022-08-26` → `2022-35`    |
| `$[month_first_day(yyyy-MM-dd,-N)]` | First day of the month, shifted by N months (unit: month) | when `N=1`, `2022-08-26` → `2022-07-01` |
| `$[month_last_day(yyyy-MM-dd,-N)]`  | Last day of the month, shifted by N months (unit: month)  | when `N=1`, `2022-08-28` → `2022-07-31` |
| `$[week_first_day(yyyy-MM-dd,-N)]`  | Monday of the week, shifted by N weeks (unit: week)       | when `N=1`, `2022-08-26` → `2022-08-15` |
| `$[week_last_day(yyyy-MM-dd,-N)]`   | Sunday of the week, shifted by N weeks (unit: week)       | when `N=1`, `2022-08-26` → `2022-08-21` |

> For `year_week`, the offset `N` selects the first day of the week (1 = Monday, 2 = Tuesday, …, 7 = Sunday) rather than shifting the date. Because DolphinScheduler requires at least 4 days for the first week of the year, the same date may fall in a different week number depending on which day the week starts on.

### Period boundaries with a day offset

There is a second family of boundary functions — `month_begin` / `month_end` / `week_begin` / `week_end`. They differ from the `*_first_day` / `*_last_day` functions above in two ways: the **second argument is required**, and it is a **day** offset applied *after* the boundary is computed (not a month/week offset). Examples assume a benchmark date of `2022-08-26` (a Friday; that week runs Monday `2022-08-22` to Sunday `2022-08-28`).

|           Function           |                   Meaning                    |            Example → Result             |
|------------------------------|----------------------------------------------|-----------------------------------------|
| `$[month_begin(yyyyMMdd,N)]` | First day of the month, then plus N **days** | `N=0` → `20220801`; `N=3` → `20220804`  |
| `$[month_end(yyyyMMdd,N)]`   | Last day of the month, then plus N **days**  | `N=0` → `20220831`; `N=-1` → `20220830` |
| `$[week_begin(yyyyMMdd,N)]`  | Monday of the week, then plus N **days**     | `N=0` → `20220822`; `N=1` → `20220823`  |
| `$[week_end(yyyyMMdd,N)]`    | Sunday of the week, then plus N **days**     | `N=0` → `20220828`; `N=-2` → `20220826` |

> Rule of thumb: use `month_first_day` / `week_first_day` (offset in months/weeks) to jump *whole periods*; use `month_begin` / `week_begin` (offset in days) to land on a boundary and then nudge by a few days. Omitting the second argument of the `*_begin` / `*_end` functions raises an "expression not valid" error, whereas the `*_first_day` / `*_last_day` functions treat the offset as optional.

### `timestamp()` — Unix epoch seconds

`$[timestamp(...)]` converts a supported date-format, arithmetic, or boundary expression into a **Unix timestamp in seconds**. The inner expression must format its result as a full `yyyyMMddHHmmss` value; `year_week(...)` cannot be used because it returns a year/week value rather than a complete date and time.

|             Expression              |                       Meaning                        |
|-------------------------------------|------------------------------------------------------|
| `$[timestamp(yyyyMMddHHmmss)]`      | The benchmark time as epoch seconds                  |
| `$[timestamp(yyyyMMddHHmmss-1/24)]` | One hour before the benchmark time, as epoch seconds |
| `$[timestamp(yyyyMMddHHmmss-1)]`    | One day before the benchmark time, as epoch seconds  |

For a benchmark time of `2022-08-26 00:00:00`, `$[timestamp(yyyyMMddHHmmss)]` returns `1661443200` when the JVM default time zone is UTC+08:00. The value changes with the runtime time zone.

## Custom Variable Reference

You can also reference the value of any custom parameter (local, global, project-level, or passed from an upstream task) with the same `${variableName}` syntax. See [Local Parameter](local.md), [Global Parameter](global.md) and [Parameter Context](context.md) for how those values are defined and passed.

For how the `${...}` and `$[...]` forms interact — and how to combine built-in parameters with your own — see [Parameter Combination](parameter-combination.md).
