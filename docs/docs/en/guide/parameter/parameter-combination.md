# Parameter Combination

DolphinScheduler exposes several kinds of parameters — [built-in](built-in.md), [global](global.md), [local](local.md), [project-level](project-parameter.md), and values [passed from upstream tasks](context.md). This page explains how the two placeholder *forms* are resolved and how to combine them safely in real scripts.

## Two placeholder forms

|   Form    |                                               Purpose                                               |           Resolved by            |
|-----------|-----------------------------------------------------------------------------------------------------|----------------------------------|
| `${name}` | Substitute the value of a named parameter (built-in `system.*`, custom, global, local, or upstream) | Name lookup in the parameter map |
| `$[expr]` | Evaluate a date/time expression against the instance benchmark time                                 | Time-expression engine           |

The two forms are not interchangeable: `${...}` looks a name up in a map, while `$[...]` runs the date engine described in [Built-in Parameter](built-in.md#derived-time-built-in-parameter).

## Resolution order (important)

When a value contains both forms, DolphinScheduler always resolves them in a fixed order:

1. **First** every `${name}` found in the parameter map is replaced with its value.
2. **Then** every `$[expr]` is evaluated against the benchmark time.

This ordering has one practical consequence worth remembering: a `${...}` reference is substituted *before* the date engine runs, so a custom parameter can itself expand into a `$[...]` expression and still be evaluated. The reverse does not hold — the output of a `$[...]` expression is never re-scanned for `${...}` names.

## Combining built-in parameters in a Shell task

The most common pattern is deriving several related dates from the same benchmark time. Because all `$[...]` expressions share the instance benchmark time, the dates stay consistent even across a complement (backfill) run:

```shell
# All three derive from the same benchmark time
run_date=$[yyyy-MM-dd]
prev_date=$[yyyy-MM-dd-1]
month_start=$[month_first_day(yyyy-MM-dd,0)]

echo "processing ${system.workflow.definition.name} for ${run_date}"
echo "previous day : ${prev_date}"
echo "month start   : ${month_start}"

# Build an output path from a mix of forms
out_dir="/data/warehouse/${system.project.name}/dt=$[yyyyMMdd]"
echo "writing to ${out_dir}"
```

Here `run_date`, `prev_date`, `month_start`, and `out_dir` are Shell variables. Unless parameters with the same names are defined in DolphinScheduler, the scheduler leaves those `${...}` references unchanged and the Shell expands them when the script runs.

For the instance scheduled on `2022-08-26`, this resolves to:

```text
processing daily_etl for 2022-08-26
previous day : 2022-08-25
month start   : 2022-08-01
writing to /data/warehouse/finance/dt=20220826
```

## Combining with a custom parameter

Define a local or global parameter whose value is itself a time expression, then reference it by name. Because `${...}` is resolved first, the substituted `$[...]` is still evaluated afterwards.

Define a local parameter (Direction `IN`):

- `partition_date` = `$[yyyyMMdd]`

Then in the Shell script:

```shell
echo "partition = ${partition_date}"
```

`${partition_date}` is first replaced by the literal `$[yyyyMMdd]`, which the date engine then turns into `20220826`.

## Combining in a SQL task

The same rules apply to SQL. Built-in and derived parameters are commonly used to scope a query to the business date:

```sql
-- ${system.biz.date} is the benchmark date minus one day (yyyyMMdd)
-- The $[...] range below covers that same previous calendar day
INSERT INTO dws_user_active_di
SELECT
    '${system.biz.date}'           AS stat_date,
    count(DISTINCT user_id)        AS active_users
FROM   dwd_user_event
WHERE  dt = '${system.biz.date}'
  AND  event_time >= '$[yyyy-MM-dd HH:mm:ss-1]'
  AND  event_time <  '$[yyyy-MM-dd HH:mm:ss]';
```

For the instance scheduled on `2022-08-26` this becomes:

```sql
INSERT INTO dws_user_active_di
SELECT
    '20220825'                     AS stat_date,
    count(DISTINCT user_id)        AS active_users
FROM   dwd_user_event
WHERE  dt = '20220825'
  AND  event_time >= '2022-08-25 00:00:00'
  AND  event_time <  '2022-08-26 00:00:00';
```

> Quote date values in SQL. `$[...]` and `${...}` expand to bare text, so `dt = '${system.biz.date}'` needs the surrounding quotes to produce a valid string literal.

## Passing a derived date to a downstream task

You can compute a date in one task, export it with `setValue`, and consume it downstream by name. Combine this with `$[...]` to make the exported value track the business date.

Upstream Shell task (add an `OUT` custom parameter `stat_date`):

```shell
echo '#{setValue(stat_date=$[yyyyMMdd])}'
```

Downstream task references it as an ordinary `${stat_date}`. See [Parameter Context](context.md) for the full `setValue` mechanism and [Parameter Priority](priority.md) for how conflicting names are resolved.

## Things that do not work

- **Producing an invalid time expression after substitution** — variables can compose a time expression; for example, with `offset=-1`, `$[yyyyMMdd${offset}]` first becomes `$[yyyyMMdd-1]` and is then evaluated. The final `$[...]` content must still match the date engine's own format/function grammar.
- **Feeding a date result back into a name** — the output of `$[...]` is never re-scanned for `${...}`, because time evaluation is the last step.
- **Expecting `$[...]` to use the wall-clock run time** — it always uses the instance benchmark time. If you genuinely need the real execution time, use a shell command such as `date` inside the task.

## See also

- [Built-in Parameter](built-in.md) — the full list of `system.*` variables and `$[...]` functions.
- [Parameter Priority](priority.md) — which value wins when the same name comes from several sources.
- [Parameter Context](context.md) — passing parameters between tasks with `setValue`.

