# 内置参数

DolphinScheduler 提供两类内置参数，系统会在运行时自动填充，无需用户自行声明：

- **基础内置参数** —— 一组固定的 `system.*` 变量（任务 ID、工作流名称、业务日期等），使用 `${变量名}` 语法引用。
- **衍生（时间）内置参数** —— 使用 `$[...]` 语法引用的日期/时间表达式，可以任意组合与偏移（例如 `$[yyyy-MM-dd-1]`）。

## 基础内置参数

|               变量名               |                 声明方式                 |                含义                |
|---------------------------------|--------------------------------------|----------------------------------|
| system.biz.date                 | `${system.biz.date}`                 | 日常调度实例定时的定时时间前一天，格式为 yyyyMMdd    |
| system.biz.curdate              | `${system.biz.curdate}`              | 日常调度实例定时的定时时间，格式为 yyyyMMdd       |
| system.datetime                 | `${system.datetime}`                 | 日常调度实例定时的定时时间，格式为 yyyyMMddHHmmss |
| system.task.execute.path        | `${system.task.execute.path}`        | 当前任务执行的绝对路径                      |
| system.task.instance.id         | `${system.task.instance.id}`         | 当前任务实例的ID                        |
| system.task.definition.name     | `${system.task.definition.name}`     | 当前任务所属任务定义的名称                    |
| system.task.definition.code     | `${system.task.definition.code}`     | 当前任务所属任务定义的code                  |
| system.workflow.instance.id     | `${system.workflow.instance.id}`     | 当前任务所属工作流实例ID                    |
| system.workflow.definition.name | `${system.workflow.definition.name}` | 当前任务所属工作流定义的名称                   |
| system.workflow.definition.code | `${system.workflow.definition.code}` | 当前任务所属工作流定义的code                 |
| system.project.name             | `${system.project.name}`             | 当前任务所在项目的名称                      |
| system.project.code             | `${system.project.code}`             | 当前任务所在项目的code                    |

### 关于业务日期（`system.biz.date` / `system.biz.curdate`）

`system.biz.curdate` 是实例的**调度时间**，`system.biz.date` 则是它的**前一天**。之所以要“减一天”，是因为在凌晨调度运行的工作流通常处理的是*前一天*的数据。

调度时间的取值取决于工作流的触发方式：

- **定时调度运行**：调度时间为定时器配置的时间（而非实际触发时刻）。
- **补数据（回填）运行**：调度时间为回填区间内的每一个业务日期，因此每生成一个实例，`system.biz.date` 就顺移一天。
- **手动运行且无调度时间**：系统使用当前时间（`now`）减一天作为业务日期。

### 示例：打印全部基础参数

新建一个 Shell 任务并粘贴下面的脚本。运行时，每个占位符都会被替换为该实例的具体取值：

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

一个调度时间为 `2022-08-26 00:00:00` 的工作流实例，输出示例如下：

```text
biz.date        = 20220825
biz.curdate     = 20220826
datetime        = 20220826000000
execute.path    = /tmp/dolphinscheduler/exec/process/<...>/<taskInstanceId>
task.instance   = 3096
...
```

## 衍生（时间）内置参数

除了固定的 `system.*` 变量，DolphinScheduler 还支持一套写作 `$[...]` 的紧凑日期/时间模板语言，可以基于实例的**基准时间**推导出任意日期。

### 基准时间

每个 `$[...]` 表达式都基于同一个**基准时间**求值，该基准时间是实例的业务/调度时间 —— 与 `${system.datetime}` 暴露的值相同。它**不是**任务实际运行的墙上时间。

这意味着：

- 对于**定时调度**运行，`$[yyyyMMdd]` 解析为调度日期，因此重跑或延迟启动都不会改变结果。
- 对于**补数据（回填）**运行，基准时间依次是每一个被回填的业务日期，因此 `$[yyyyMMdd]` 会遍历整个区间。
- 只有在没有调度时间可用时，基准时间才回退为当前时间。

正是这一点让 `$[...]` 可以安全地用于幂等、可回填的数据管道：推导出的日期跟随数据日期，而非运行日期。

### 格式模板

`$[...]` 接受 DolphinScheduler 日期格式化器支持的 Java 风格日期模式，各部分可以自由拆分组合：

- `$[yyyyMMdd]`、`$[HHmmss]`、`$[yyyy-MM-dd]`、`$[yyyy-MM-dd HH:mm:ss]`、`$[yyyyMMddHHmmss]` 等。

### 日期运算

可以在格式后直接加减，单位通过运算本身来表达：

|   目标   |               表达式               |
|--------|---------------------------------|
| 后 N 年  | `$[add_months(yyyyMMdd,12*N)]`  |
| 前 N 年  | `$[add_months(yyyyMMdd,-12*N)]` |
| 后 N 月  | `$[add_months(yyyyMMdd,N)]`     |
| 前 N 月  | `$[add_months(yyyyMMdd,-N)]`    |
| 后 N 周  | `$[yyyyMMdd+7*N]`               |
| 前 N 周  | `$[yyyyMMdd-7*N]`               |
| 后 N 天  | `$[yyyyMMdd+N]`                 |
| 前 N 天  | `$[yyyyMMdd-N]`                 |
| 后 N 小时 | `$[HHmmss+N/24]`                |
| 前 N 小时 | `$[HHmmss-N/24]`                |
| 后 N 分钟 | `$[HHmmss+N/24/60]`             |
| 前 N 分钟 | `$[HHmmss-N/24/60]`             |

> `N` 表示整数或算术表达式，实际使用时要替换为具体值，例如前两周写作 `$[yyyyMMdd-7*2]`。`add_months` 函数的第一个参数是日期格式，第二个参数是月份偏移量，因此它是按月或按年偏移（这两者没有固定天数）的安全方式。直接 `+/-` 运算以天/小时/分钟为基础，这也是为什么小时表示为一天的 `N/24`、分钟表示为一天的 `N/24/60`。

### 业务属性函数

这些函数返回具有日历语义的日期。每个函数的第一个参数是日期格式；接受偏移量的函数，其第二个参数是偏移量（单位见备注）。以下示例均假设基准日期为 `2022-08-26`。

|                 函数                  |          含义           |                示例 → 结果                |
|-------------------------------------|-----------------------|---------------------------------------|
| `$[this_day(yyyy-MM-dd)]`           | 当天（基准日期）              | `2022-08-26` → `2022-08-26`           |
| `$[last_day(yyyy-MM-dd)]`           | 昨天                    | `2022-08-26` → `2022-08-25`           |
| `$[year_week(yyyy-MM-dd)]`          | 年的第几周，以周一为一周起点        | `2022-08-26` → `2022-34`              |
| `$[year_week(yyyy-MM-dd,N)]`        | 年的第几周，以周 N 为一周起点      | 当 `N=5` 时，`2022-08-26` → `2022-35`    |
| `$[month_first_day(yyyy-MM-dd,-N)]` | 偏移 N 月后所在月的第一天（单位：月）  | 当 `N=1` 时，`2022-08-26` → `2022-07-01` |
| `$[month_last_day(yyyy-MM-dd,-N)]`  | 偏移 N 月后所在月的最后一天（单位：月） | 当 `N=1` 时，`2022-08-28` → `2022-07-31` |
| `$[week_first_day(yyyy-MM-dd,-N)]`  | 偏移 N 周后所在周的周一（单位：周）   | 当 `N=1` 时，`2022-08-26` → `2022-08-15` |
| `$[week_last_day(yyyy-MM-dd,-N)]`   | 偏移 N 周后所在周的周日（单位：周）   | 当 `N=1` 时，`2022-08-26` → `2022-08-21` |

> 对于 `year_week`，偏移量 `N` 用于选择一周的起始日（1 = 周一，2 = 周二，……，7 = 周日），而不是偏移日期。由于 DolphinScheduler 要求一年的第一周至少包含 4 天，同一日期在不同的一周起始日下可能落入不同的周序号。

### 以天为偏移单位的周期边界函数

还有第二组边界函数——`month_begin` / `month_end` / `week_begin` / `week_end`。它们与上面的 `*_first_day` / `*_last_day` 系列有两点不同：**第二个参数必填**，且它是在计算出边界之后再施加的**天**偏移（而不是月/周偏移）。以下示例假设基准日期为 `2022-08-26`（周五；该周为周一 `2022-08-22` 到周日 `2022-08-28`）。

|              函数              |         含义          |                示例 → 结果                 |
|------------------------------|---------------------|----------------------------------------|
| `$[month_begin(yyyyMMdd,N)]` | 所在月的第一天，再加 N **天**  | `N=0` → `20220801`；`N=3` → `20220804`  |
| `$[month_end(yyyyMMdd,N)]`   | 所在月的最后一天，再加 N **天** | `N=0` → `20220831`；`N=-1` → `20220830` |
| `$[week_begin(yyyyMMdd,N)]`  | 所在周的周一，再加 N **天**   | `N=0` → `20220822`；`N=1` → `20220823`  |
| `$[week_end(yyyyMMdd,N)]`    | 所在周的周日，再加 N **天**   | `N=0` → `20220828`；`N=-2` → `20220826` |

> 经验法则：用 `month_first_day` / `week_first_day`（偏移单位为月/周）来跨越*整个周期*；用 `month_begin` / `week_begin`（偏移单位为天）先落到某个边界、再微调几天。`*_begin` / `*_end` 函数如果省略第二个参数会抛出 “expression not valid” 错误，而 `*_first_day` / `*_last_day` 函数的偏移量是可选的。

### `timestamp()` —— Unix 时间戳（秒）

`$[timestamp(...)]` 可以把受支持的日期格式、日期运算或周期边界表达式转换为**以秒为单位的 Unix 时间戳**。内层表达式必须以完整的 `yyyyMMddHHmmss` 格式输出；`year_week(...)` 只返回年和周序号，因此不能用于 `timestamp(...)`。

|                 表达式                 |          含义          |
|-------------------------------------|----------------------|
| `$[timestamp(yyyyMMddHHmmss)]`      | 基准时间对应的 Unix 秒级时间戳   |
| `$[timestamp(yyyyMMddHHmmss-1/24)]` | 基准时间前一小时的 Unix 秒级时间戳 |
| `$[timestamp(yyyyMMddHHmmss-1)]`    | 基准时间前一天的 Unix 秒级时间戳  |

当基准时间为 `2022-08-26 00:00:00` 且 JVM 默认时区为 UTC+08:00 时，`$[timestamp(yyyyMMddHHmmss)]` 返回 `1661443200`；运行时区不同，结果也会不同。

## 引用自定义变量

你同样可以用 `${变量名}` 语法引用任意自定义参数的值（本地参数、全局参数、项目级参数，或上游任务传递而来的参数）。这些值如何定义与传递，参见[本地参数](local.md)、[全局参数](global.md)与[参数传递](context.md)。

关于 `${...}` 与 `$[...]` 两种写法如何相互作用，以及如何将内置参数与自定义参数组合使用，参见[参数组合使用](parameter-combination.md)。
