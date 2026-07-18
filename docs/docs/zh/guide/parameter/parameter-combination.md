# 参数组合使用

DolphinScheduler 提供了多种参数：[内置参数](built-in.md)、[全局参数](global.md)、[本地参数](local.md)、[项目级别参数](project-parameter.md)，以及[上游任务传递](context.md)的值。本文说明两种占位符*形式*各自如何被解析，以及在真实脚本中如何安全地组合使用它们。

## 两种占位符形式

|    形式     |                    用途                    |     解析方式      |
|-----------|------------------------------------------|---------------|
| `${name}` | 替换某个具名参数的值（内置 `system.*`、自定义、全局、本地或上游参数） | 在参数 Map 中按名查找 |
| `$[expr]` | 基于实例基准时间计算一个日期/时间表达式                     | 时间表达式引擎       |

两者不可互换：`${...}` 是在 Map 中按名查找，`$[...]` 则运行 [内置参数](built-in.md#衍生内置参数时间) 中描述的日期引擎。

## 解析顺序（重要）

当一个值同时包含两种形式时，DolphinScheduler 始终按固定顺序解析：

1. **先**将参数 Map 中存在的 `${name}` 替换为其对应的值。
2. **再**基于基准时间计算所有 `$[expr]`。

这个顺序有一个值得记住的实际影响：`${...}` 引用会在日期引擎运行*之前*被替换，因此一个自定义参数的值本身可以是一个 `$[...]` 表达式，替换后仍会被正确计算。反之则不成立——`$[...]` 表达式的计算结果不会再被扫描 `${...}` 变量名。

## 在 Shell 任务中组合内置参数

最常见的用法是从同一个基准时间派生出多个相关日期。因为所有 `$[...]` 表达式共享实例的基准时间，即使在补数据（complement）运行时，这些日期也能保持一致：

```shell
# 三个日期都派生自同一个基准时间
run_date=$[yyyy-MM-dd]
prev_date=$[yyyy-MM-dd-1]
month_start=$[month_first_day(yyyy-MM-dd,0)]

echo "正在处理 ${system.workflow.definition.name}，业务日期 ${run_date}"
echo "前一天    : ${prev_date}"
echo "本月首日  : ${month_start}"

# 用多种形式拼接输出路径
out_dir="/data/warehouse/${system.project.name}/dt=$[yyyyMMdd]"
echo "写入到 ${out_dir}"
```

这里的 `run_date`、`prev_date`、`month_start` 和 `out_dir` 是 Shell 变量。只要 DolphinScheduler 中没有定义同名参数，调度器就会保留这些 `${...}` 引用，交给 Shell 在脚本运行时展开。

对于调度时间为 `2022-08-26` 的实例，解析结果为：

```text
正在处理 daily_etl，业务日期 2022-08-26
前一天    : 2022-08-25
本月首日  : 2022-08-01
写入到 /data/warehouse/finance/dt=20220826
```

## 与自定义参数组合

定义一个本地或全局参数，其值本身是一个时间表达式，然后按名引用它。因为 `${...}` 先被解析，替换出的 `$[...]` 随后仍会被计算。

定义一个本地参数（方向为 `IN`）：

- `partition_date` = `$[yyyyMMdd]`

然后在 Shell 脚本中：

```shell
echo "partition = ${partition_date}"
```

`${partition_date}` 先被替换为字面量 `$[yyyyMMdd]`，随后日期引擎再将其计算为 `20220826`。

## 在 SQL 任务中组合

同样的规则适用于 SQL。内置参数和衍生参数常用于把查询限定到业务日期范围：

```sql
-- ${system.biz.date} 是基准日期减一天（yyyyMMdd）
-- 下面的 $[...] 时间范围覆盖同一个前一自然日
INSERT INTO dws_user_active_di
SELECT
    '${system.biz.date}'           AS stat_date,
    count(DISTINCT user_id)        AS active_users
FROM   dwd_user_event
WHERE  dt = '${system.biz.date}'
  AND  event_time >= '$[yyyy-MM-dd HH:mm:ss-1]'
  AND  event_time <  '$[yyyy-MM-dd HH:mm:ss]';
```

对于调度时间为 `2022-08-26` 的实例，它会变成：

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

> SQL 中的日期值要加引号。`$[...]` 和 `${...}` 展开后都是纯文本，因此 `dt = '${system.biz.date}'` 需要外层引号才能生成合法的字符串字面量。

## 将派生日期传递给下游任务

你可以在一个任务中计算出日期，用 `setValue` 导出，下游任务再按名引用。结合 `$[...]` 可以让导出的值随业务日期变化。

上游 Shell 任务（新增一个 `OUT` 自定义参数 `stat_date`）：

```shell
echo '#{setValue(stat_date=$[yyyyMMdd])}'
```

下游任务按普通的 `${stat_date}` 引用它。完整的 `setValue` 机制见 [参数传递](context.md)，同名冲突的处理规则见 [参数优先级](priority.md)。

## 不支持的用法

- **替换后不是合法时间表达式**——可以用变量拼出时间表达式，例如 `offset=-1` 时 `$[yyyyMMdd${offset}]` 会先变成 `$[yyyyMMdd-1]` 再解析；但替换完成后的 `$[...]` 内容必须符合日期引擎自己的格式/函数语法。
- **把日期结果再当作变量名解析**——`$[...]` 的计算结果不会再被扫描 `${...}`，因为时间计算是最后一步。
- **期望 `$[...]` 使用真实运行时间**——它始终使用实例的基准时间。如果确实需要真实执行时间，请在任务内使用 `date` 等 shell 命令。

## 参见

- [内置参数](built-in.md) —— `system.*` 变量和 `$[...]` 函数的完整列表。
- [参数优先级](priority.md) —— 同名参数来自多个来源时以哪个为准。
- [参数传递](context.md) —— 使用 `setValue` 在任务间传递参数。

