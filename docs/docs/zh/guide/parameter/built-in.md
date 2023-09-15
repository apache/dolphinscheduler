# 内置参数

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

## 衍生内置参数

- 支持代码中自定义变量名，声明方式：${变量名}。可以是引用 "系统参数"

- 我们定义这种基准变量为 \$[...] 格式的，\$[yyyyMMddHHmmss] 是可以任意分解组合的，比如：\$[yyyyMMdd], \$[HHmmss], \$[yyyy-MM-dd] 等

- 也可以通过以下两种方式：

  1.使用add_months()函数，该函数用于加减月份，
  第一个入口参数为[yyyyMMdd]，表示返回时间的格式
  第二个入口参数为月份偏移量，表示加减多少个月
  * 后 N 年：$[add_months(yyyyMMdd,12*N)]
  * 前 N 年：$[add_months(yyyyMMdd,-12*N)]
  * 后 N 月：$[add_months(yyyyMMdd,N)]
  * 前 N 月：$[add_months(yyyyMMdd,-N)]
  *******************************************
  2.直接加减数字
  在自定义格式后直接“+/-”数字
  * 后 N 周：$[yyyyMMdd+7*N]
  * 前 N 周：$[yyyyMMdd-7*N]
  * 后 N 天：$[yyyyMMdd+N]
  * 前 N 天：$[yyyyMMdd-N]
  * 后 N 小时：$[HHmmss+N/24]
  * 前 N 小时：$[HHmmss-N/24]
  * 后 N 分钟：$[HHmmss+N/24/60]
  * 前 N 分钟：$[HHmmss-N/24/60]
  *******************************************
  3.业务属性方式
  在自定义格式后直接“+/-”数字
  支持日志格式：所有日期表达式，例如：yyyy-MM-dd/yyyyMMddHHmmss
  * 当天：$[this_day(yyyy-MM-dd)]，如：2022-08-26 => 2022-08-26
  * 昨天：$[last_day(yyyy-MM-dd)]，如：2022-08-26 => 2022-08-25
  * 年的第N周，以周一为起点：$[year_week(yyyy-MM-dd)]，如：2022-08-26 => 2022-34
  * 年的第N周，以周N为起点：$[year_week(yyyy-MM-dd,N)]，如：N=5时 2022-08-26 => 2022-35
  * 前(-)/后(+) N 月第一天：$[month_first_day(yyyy-MM-dd,-N)]，如：N=1时 2022-08-26 => 2022-07-01
  * 前(-)/后(+) N 月最后一天：$[month_last_day(yyyy-MM-dd,-N)]，如：N=1时 2022-08-28 => 2022-07-31
  * 前(-)/后(+) N 周的周一：$[week_first_day(yyyy-MM-dd,-N)]，如：N=1 2022-08-26 => 2022-08-15
  * 前(-)/后(+) N 周的周日：$[week_last_day(yyyy-MM-dd,-N)]，如：N=1 2022-08-26 => 2022-08-21

