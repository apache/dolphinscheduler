# 任务结构

## 总体存储结构

工作流不以单个 JSON 文档存储，而是拆分为三张表，每张表都有一张保存所有版本的 `*_log` 表：

|               表               |           版本表（`*_log`）            |          内容          |
|-------------------------------|-----------------------------------|----------------------|
| `t_ds_workflow_definition`    | `t_ds_workflow_definition_log`    | 工作流级别的属性，每个工作流一行     |
| `t_ds_task_definition`        | `t_ds_task_definition_log`        | 任务级别的属性，每个任务一行       |
| `t_ds_workflow_task_relation` | `t_ds_workflow_task_relation_log` | DAG 的边，两个任务之间的每条连线一行 |

- 工作流和任务通过全局唯一的 `code` 加 `version` 标识。主表保存当前版本，`*_log` 表保存所有版本，并额外记录 `operator`（操作用户 id）和 `operate_time`。
- 任务关系通过 `code + version` 引用任务；工作流实例（`t_ds_workflow_instance`）和任务实例（`t_ds_task_instance`）引用创建它们时的具体定义版本。
- 各任务类型特有的配置以 JSON 形式存储在 `t_ds_task_definition.task_params` 中，详见[任务参数](#任务参数)。

表结构以 `dolphinscheduler-dao/src/main/resources/sql` 下的 SQL 文件为准，下文介绍常用字段。

## 工作流定义

表 `t_ds_workflow_definition`：

|        字段        |      类型      |                       描述                        |
|------------------|--------------|-------------------------------------------------|
| id               | int(11)      | 主键                                              |
| code             | bigint(20)   | 工作流定义 code，全局唯一                                 |
| name             | varchar(255) | 工作流定义名称，项目内唯一                                   |
| version          | int(11)      | 工作流定义版本                                         |
| description      | text         | 描述                                              |
| project_code     | bigint(20)   | 所属项目 code                                       |
| release_state    | tinyint(4)   | 发布状态：0 下线，1 上线                                  |
| user_id          | int(11)      | 创建用户 id                                         |
| global_params    | text         | 全局参数，结构为[参数](#公共字段)组成的 JSON 数组                  |
| flag             | tinyint(4)   | 工作流是否可用：0 不可用，1 可用                              |
| locations        | text         | 节点在画布上的坐标，例如 `[{"taskCode":123,"x":59,"y":61}]` |
| warning_group_id | int(11)      | 告警组 id                                          |
| timeout          | int(11)      | 超时时间，单位分钟                                       |
| execution_type   | tinyint(4)   | 执行策略：0 并行，1 串行等待，2 串行抛弃，3 串行优先                  |
| create_time      | datetime     | 创建时间                                            |
| update_time      | datetime     | 更新时间                                            |

租户不属于工作流定义，而是在启动或定时调度工作流时选择，保存在 `t_ds_schedules.tenant_code` 和 `t_ds_workflow_instance.tenant_code` 中。

## 任务定义

表 `t_ds_task_definition`：

|           字段            |      类型      |                  描述                  |
|-------------------------|--------------|--------------------------------------|
| id                      | int(11)      | 主键                                   |
| code                    | bigint(20)   | 任务定义 code，全局唯一                       |
| name                    | varchar(255) | 任务定义名称                               |
| version                 | int(11)      | 任务定义版本                               |
| description             | text         | 描述                                   |
| project_code            | bigint(20)   | 所属项目 code                            |
| user_id                 | int(11)      | 创建用户 id                              |
| task_type               | varchar(50)  | 任务类型，例如 `SHELL`、`SQL`、`SUB_WORKFLOW` |
| task_execute_type       | int(11)      | 任务执行类型：0 批，1 流                       |
| task_params             | longtext     | 任务类型特有的参数，JSON 格式，详见[任务参数](#任务参数)    |
| flag                    | tinyint(2)   | 任务是否正常运行：0 禁止执行，1 正常                 |
| task_priority           | tinyint(4)   | 优先级：0 最高，1 高，2 中，3 低，4 最低            |
| worker_group            | varchar(255) | Worker 分组                            |
| environment_code        | bigint(20)   | 环境 code，-1 表示不使用环境                   |
| fail_retry_times        | int(11)      | 失败重试次数                               |
| fail_retry_interval     | int(11)      | 失败重试间隔，单位分钟                          |
| timeout_flag            | tinyint(2)   | 是否开启超时告警：0 关闭，1 开启                   |
| timeout_notify_strategy | tinyint(4)   | 超时策略：0 告警，1 失败，2 告警并失败               |
| timeout                 | int(11)      | 超时时长，单位分钟                            |
| delay_time              | int(11)      | 延时执行时间，单位分钟                          |
| resource_ids            | text         | 资源 id，逗号分隔                           |
| task_group_id           | int(11)      | 任务组 id                               |
| task_group_priority     | tinyint(4)   | 任务组内优先级                              |
| cpu_quota               | int(11)      | CPU 配额（百分比），-1 表示不限制                 |
| memory_max              | int(11)      | 最大内存（MB），-1 表示不限制                    |
| create_time             | datetime     | 创建时间                                 |
| update_time             | datetime     | 更新时间                                 |

## 任务关系

表 `t_ds_workflow_task_relation`：

|             字段              |      类型      |         描述         |
|-----------------------------|--------------|--------------------|
| id                          | int(11)      | 主键                 |
| name                        | varchar(255) | 关系名称               |
| project_code                | bigint(20)   | 项目 code            |
| workflow_definition_code    | bigint(20)   | 工作流定义 code         |
| workflow_definition_version | int(11)      | 工作流定义版本            |
| pre_task_code               | bigint(20)   | 上游任务 code，没有上游时为 0 |
| pre_task_version            | int(11)      | 上游任务版本，没有上游时为 0    |
| post_task_code              | bigint(20)   | 下游任务 code          |
| post_task_version           | int(11)      | 下游任务版本             |
| condition_type              | tinyint(2)   | 条件类型：0 无，1 判断，2 延时 |
| condition_params            | text         | 条件参数，JSON 格式       |
| create_time                 | datetime     | 创建时间               |
| update_time                 | datetime     | 更新时间               |

工作流中的每个任务都至少作为一行的 `post_task_code` 出现。例如任务 `A` 先执行、随后 `B` 和 `C` 并行执行的工作流，存储为三行：

| pre_task_code | post_task_code |
|---------------|----------------|
| 0             | A              |
| A             | B              |
| A             | C              |

## 任务参数

`t_ds_task_definition.task_params` 以 JSON 对象保存单个任务的参数，结构由 `task_type` 决定，对应任务插件中继承 `dolphinscheduler-task-plugin/dolphinscheduler-task-api` 中 `AbstractParameters` 的参数类。

### 公共字段

|       字段        |   类型   |                 描述                 |
|-----------------|--------|------------------------------------|
| localParams     | Array  | 任务的本地参数                            |
| -- prop         | String | 参数名                                |
| -- direct       | String | 方向：`IN` 或 `OUT`                    |
| -- type         | String | 数据类型，例如 `VARCHAR`、`INTEGER`、`DATE` |
| -- value        | String | 参数值                                |
| resourceList    | Array  | 任务使用的资源文件，仅支持资源的任务类型（如 SHELL）使用    |
| -- resourceName | String | 资源文件全名                             |

工作流定义的 `global_params` 与 `localParams` 结构相同。

### 各任务类型参数

下表列出本文介绍的各任务类型对应的参数类，后续小节逐一说明其字段并给出示例。

|     任务类型     |                           参数类                           |                                  主要字段                                  |
|--------------|---------------------------------------------------------|------------------------------------------------------------------------|
| SHELL        | `dolphinscheduler-task-shell` `ShellParameters`         | rawScript, resourceList                                                |
| PYTHON       | `dolphinscheduler-task-python` `PythonParameters`       | rawScript, resourceList                                                |
| SQL          | `dolphinscheduler-task-api` `SqlParameters`             | type, datasource, sql, sqlType, preStatements, postStatements          |
| PROCEDURE    | `dolphinscheduler-task-procedure` `ProcedureParameters` | type, datasource, method                                               |
| SPARK        | `dolphinscheduler-task-spark` `SparkParameters`         | programType, mainJar, mainClass, master, deployMode, rawScript         |
| MR           | `dolphinscheduler-task-mr` `MapReduceParameters`        | programType, mainJar, mainClass, mainArgs, yarnQueue                   |
| FLINK        | `dolphinscheduler-task-flink` `FlinkParameters`         | programType, mainJar, mainClass, deployMode, flinkVersion              |
| HTTP         | `dolphinscheduler-task-http` `HttpParameters`           | url, httpMethod, httpParams, httpBody, httpCheckCondition              |
| DATAX        | `dolphinscheduler-task-datax` `DataxParameters`         | customConfig, json, dsType, dataSource, dtType, dataTarget, sql        |
| SQOOP        | `dolphinscheduler-task-sqoop` `SqoopParameters`         | jobType, modelType, sourceType, sourceParams, targetType, targetParams |
| SUB_WORKFLOW | `dolphinscheduler-task-api` `SubWorkflowParameters`     | workflowDefinitionCode                                                 |
| DEPENDENT    | `dolphinscheduler-task-api` `DependentParameters`       | dependence                                                             |
| CONDITIONS   | `dolphinscheduler-task-api` `ConditionsParameters`      | dependence, conditionResult                                            |
| SWITCH       | `dolphinscheduler-task-api` `SwitchParameters`          | switchResult                                                           |

其他任务类型同理：在对应的 `dolphinscheduler-task-plugin/dolphinscheduler-task-<type>` 模块中查找 `*Parameters` 类。

### SHELL

在 Worker 上执行 Shell 脚本，`task_params` 由 `dolphinscheduler-task-shell` 模块的 `ShellParameters` 定义。

|      字段      |   类型   |            描述             |
|--------------|--------|---------------------------|
| localParams  | Array  | 本地参数，见 [公共字段](#公共字段)      |
| resourceList | Array  | 脚本使用的资源文件，见 [公共字段](#公共字段) |
| rawScript    | String | Shell 脚本内容                |

**示例：**

```json
{
    "localParams": [
        {
            "prop": "dt",
            "direct": "IN",
            "type": "VARCHAR",
            "value": "${system.datetime}"
        }
    ],
    "rawScript": "echo \"dt=${dt}\"\nsh ./run.sh",
    "resourceList": [
        {
            "resourceName": "file:/tmp/dolphinscheduler/default/resources/run.sh"
        }
    ]
}
```

### PYTHON

在 Worker 上执行 Python 脚本，`task_params` 由 `dolphinscheduler-task-python` 模块的 `PythonParameters` 定义。

|      字段      |   类型   |            描述             |
|--------------|--------|---------------------------|
| localParams  | Array  | 本地参数，见 [公共字段](#公共字段)      |
| resourceList | Array  | 脚本使用的资源文件，见 [公共字段](#公共字段) |
| rawScript    | String | Python 脚本内容               |

**示例：**

```json
{
    "localParams": [
        {
            "prop": "dt",
            "direct": "IN",
            "type": "VARCHAR",
            "value": "${system.datetime}"
        }
    ],
    "rawScript": "import sys\nprint('dt=${dt}')",
    "resourceList": [
        {
            "resourceName": "file:/tmp/dolphinscheduler/default/resources/utils.py"
        }
    ]
}
```

### SQL

对数据源执行 SQL 语句，`task_params` 由 `dolphinscheduler-task-api` 模块的 `SqlParameters` 定义。

|       字段       |   类型    |                      描述                      |
|----------------|---------|----------------------------------------------|
| localParams    | Array   | 本地参数，替换 SQL 中的 `${variable}`，见 [公共字段](#公共字段) |
| resourceList   | Array   | 资源文件，SQL 任务通常为空，见 [公共字段](#公共字段)              |
| type           | String  | 数据源类型，如 `MYSQL`、`POSTGRESQL`、`HIVE`          |
| datasource     | Integer | 数据源 id                                       |
| sql            | String  | SQL 语句，`sqlSource` 为 `SCRIPT` 时使用            |
| sqlSource      | String  | SQL 来源：`SCRIPT` 内联脚本，`FILE` 资源中心文件           |
| sqlResource    | String  | 资源中心文件路径，`sqlSource` 为 `FILE` 时使用            |
| sqlType        | String  | SQL 类型：`0` 查询，`1` 非查询（前端以字符串保存）              |
| preStatements  | Array   | 在主 SQL 之前执行的 SQL 语句                          |
| postStatements | Array   | 在主 SQL 之后执行的 SQL 语句                          |
| sendEmail      | Boolean | 是否通过告警发送查询结果，仅查询类型                           |
| displayRows    | Integer | 日志中展示查询结果的最大行数，仅查询类型                         |
| title          | String  | 告警标题，仅 `sendEmail` 为 true 时保存                |
| groupId        | Integer | 告警组 id，仅 `sendEmail` 为 true 时保存              |

**示例：**

```json
{
    "localParams": [
        {
            "prop": "dt",
            "direct": "IN",
            "type": "VARCHAR",
            "value": "2026-01-01"
        }
    ],
    "resourceList": [],
    "type": "MYSQL",
    "datasource": 1,
    "sql": "select id, name from t_user where dt = '${dt}'",
    "sqlSource": "SCRIPT",
    "sqlResource": "",
    "sqlType": "0",
    "preStatements": [
        "set names utf8mb4"
    ],
    "postStatements": [],
    "sendEmail": true,
    "displayRows": 10,
    "title": "Daily user report",
    "groupId": 1
}
```

### PROCEDURE

调用数据源中的存储过程，`task_params` 由 `dolphinscheduler-task-procedure` 模块的 `ProcedureParameters` 定义。

|      字段      |   类型    |                   描述                   |
|--------------|---------|----------------------------------------|
| localParams  | Array   | 存储过程参数，支持 `IN` 和 `OUT`，见 [公共字段](#公共字段) |
| resourceList | Array   | 资源文件，存储过程任务通常为空，见 [公共字段](#公共字段)        |
| type         | String  | 数据源类型，如 `MYSQL`、`POSTGRESQL`           |
| datasource   | Integer | 数据源 id                                 |
| method       | String  | 存储过程名称                                 |

**示例：**

```json
{
    "localParams": [
        {
            "prop": "in_dt",
            "direct": "IN",
            "type": "VARCHAR",
            "value": "${system.datetime}"
        },
        {
            "prop": "out_cnt",
            "direct": "OUT",
            "type": "INTEGER",
            "value": ""
        }
    ],
    "resourceList": [],
    "type": "MYSQL",
    "datasource": 1,
    "method": "proc_daily_stat"
}
```

### HTTP

发送 HTTP 请求并校验响应，`task_params` 由 `dolphinscheduler-task-http` 模块的 `HttpParameters` 定义。

|          字段           |   类型    |                                            描述                                            |
|-----------------------|---------|------------------------------------------------------------------------------------------|
| localParams           | Array   | 本地参数，见 [公共字段](#公共字段)                                                                     |
| resourceList          | Array   | 资源文件，HTTP 任务通常为空，见 [公共字段](#公共字段)                                                         |
| url                   | String  | 请求地址                                                                                     |
| httpMethod            | String  | 请求方法：`GET`、`POST`、`PUT`、`DELETE`                                                         |
| httpParams            | Array   | 请求参数和请求头                                                                                 |
| -- prop               | String  | 参数或请求头名称                                                                                 |
| -- httpParametersType | String  | 类型：`PARAMETER` 或 `HEADERS`                                                               |
| -- value              | String  | 参数或请求头的值                                                                                 |
| httpBody              | String  | 请求体                                                                                      |
| httpCheckCondition    | String  | 校验条件：`STATUS_CODE_DEFAULT`（200）、`STATUS_CODE_CUSTOM`、`BODY_CONTAINS`、`BODY_NOT_CONTAINS` |
| condition             | String  | 校验内容，条件不是 `STATUS_CODE_DEFAULT` 时必填                                                      |
| connectTimeout        | Integer | 连接超时时间，单位毫秒                                                                              |
| socketKeepAlive       | Boolean | 是否开启 TCP keep-alive                                                                      |

**示例：**

```json
{
    "localParams": [],
    "resourceList": [],
    "url": "https://example.com/api/notify",
    "httpMethod": "POST",
    "httpParams": [
        {
            "prop": "Content-Type",
            "httpParametersType": "HEADERS",
            "value": "application/json"
        },
        {
            "prop": "dt",
            "httpParametersType": "PARAMETER",
            "value": "${dt}"
        }
    ],
    "httpBody": "{\"msg\": \"hello\"}",
    "httpCheckCondition": "STATUS_CODE_CUSTOM",
    "condition": "200",
    "connectTimeout": 60000,
    "socketKeepAlive": false
}
```

### SPARK

通过 `spark-submit` 或 `spark-sql` 运行 Spark 应用。`task_params` 由 `dolphinscheduler-task-spark` 模块的 `SparkParameters` 定义。

|        字段        |   类型    |                                            描述                                             |
|------------------|---------|-------------------------------------------------------------------------------------------|
| localParams      | Array   | 本地参数，见 [公共字段](#公共字段)                                                                      |
| rawScript        | String  | SQL 脚本，仅当 `programType` 为 `SQL` 且 `sqlExecutionType` 为 `SCRIPT` 时使用，否则为空                  |
| resourceList     | Array   | 任务使用的资源文件，见 [公共字段](#公共字段)。`programType` 为 `SQL` 且 `sqlExecutionType` 为 `FILE` 时必填（SQL 文件） |
| programType      | String  | 程序类型：`JAVA`、`SCALA`、`PYTHON` 或 `SQL`                                                      |
| mainClass        | String  | 主函数的完整类名，`PYTHON` 和 `SQL` 不使用                                                             |
| mainJar          | Object  | 主 jar 包或 Python 文件，`SQL` 不使用                                                              |
| -- resourceName  | String  | jar 资源的完整名称                                                                               |
| deployMode       | String  | 部署模式：`cluster`、`client` 或 `local`；`SQL` 支持 `client` 和 `local`                             |
| appName          | String  | 传给 Spark 的任务名称                                                                            |
| mainArgs         | String  | 主程序参数，支持 `${...}` 变量                                                                      |
| others           | String  | 其他 `spark-submit` 选项，如 `--conf`、`--jars`                                                  |
| yarnQueue        | String  | YARN 队列                                                                                   |
| master           | String  | Master 地址，如 `yarn`                                                                        |
| driverCores      | Integer | Driver 核数                                                                                 |
| driverMemory     | String  | Driver 内存，如 `512M`                                                                        |
| numExecutors     | Integer | Executor 数量                                                                               |
| executorMemory   | String  | 每个 Executor 的内存，如 `2G`                                                                    |
| executorCores    | Integer | 每个 Executor 的核数                                                                           |
| sqlExecutionType | String  | `programType` 为 `SQL` 时 SQL 的来源：`SCRIPT`（内联 `rawScript`）或 `FILE`（资源文件）                    |

仅当选择命名空间以提交到原生 Kubernetes 时才会保存 `namespace`（String，Kubernetes 命名空间的 JSON 字符串，如 `{"name":"ns","cluster":"c"}`），提交到 YARN 时没有该字段。

**示例：**

```json
{
    "localParams": [
        {
            "prop": "dt",
            "direct": "IN",
            "type": "VARCHAR",
            "value": "${system.biz.date}"
        }
    ],
    "rawScript": "",
    "resourceList": [],
    "programType": "SCALA",
    "mainClass": "org.apache.spark.examples.SparkPi",
    "mainJar": {
        "resourceName": "file:/tmp/dolphinscheduler/default/resources/spark-examples_2.12-3.2.0.jar"
    },
    "deployMode": "cluster",
    "appName": "spark-pi",
    "mainArgs": "10",
    "others": "--conf spark.sql.shuffle.partitions=200",
    "yarnQueue": "default",
    "master": "yarn",
    "driverCores": 1,
    "driverMemory": "512M",
    "numExecutors": 2,
    "executorMemory": "2G",
    "executorCores": 2,
    "sqlExecutionType": "SCRIPT"
}
```

### MR

运行 Hadoop MapReduce 作业。`task_params` 由 `dolphinscheduler-task-mr` 模块的 `MapReduceParameters` 定义。

|       字段        |   类型   |                       描述                        |
|-----------------|--------|-------------------------------------------------|
| localParams     | Array  | 本地参数，见 [公共字段](#公共字段)                            |
| resourceList    | Array  | 任务使用的资源文件，见 [公共字段](#公共字段)                       |
| programType     | String | 程序类型：`JAVA`、`SCALA` 或 `PYTHON`                  |
| mainClass       | String | 主函数的完整类名                                        |
| mainJar         | Object | MapReduce 程序的主 jar 包（`PYTHON` 时为脚本）             |
| -- resourceName | String | jar 资源的完整名称                                     |
| appName         | String | MapReduce 作业名称                                  |
| mainArgs        | String | 程序的命令行参数，支持 `${...}` 变量                         |
| others          | String | 其他参数，支持 `-D`、`-files`、`-libjars`、`-archives` 格式 |
| yarnQueue       | String | YARN 队列                                         |

**示例：**

```json
{
    "localParams": [
        {
            "prop": "dt",
            "direct": "IN",
            "type": "VARCHAR",
            "value": "${system.biz.date}"
        }
    ],
    "resourceList": [],
    "programType": "JAVA",
    "mainClass": "org.apache.hadoop.examples.WordCount",
    "mainJar": {
        "resourceName": "file:/tmp/dolphinscheduler/default/resources/hadoop-mapreduce-examples.jar"
    },
    "appName": "mr-wordcount",
    "mainArgs": "/input /output",
    "others": "-D mapreduce.job.reduces=1",
    "yarnQueue": "default"
}
```

### FLINK

通过 `flink` 命令行运行 Flink 作业。`task_params` 由 `dolphinscheduler-task-flink` 模块的 `FlinkParameters` 定义。

|        字段         |   类型    |                                    描述                                     |
|-------------------|---------|---------------------------------------------------------------------------|
| localParams       | Array   | 本地参数，见 [公共字段](#公共字段)                                                      |
| initScript        | String  | 初始化 SQL，仅当 `programType` 为 `SQL` 时使用，否则为空                                 |
| rawScript         | String  | SQL 脚本，仅当 `programType` 为 `SQL` 时使用，否则为空                                  |
| resourceList      | Array   | 任务使用的资源文件，见 [公共字段](#公共字段)                                                 |
| programType       | String  | 程序类型：`JAVA`、`SCALA`、`PYTHON` 或 `SQL`                                      |
| mainClass         | String  | 主函数的完整类名，`PYTHON` 和 `SQL` 不使用                                             |
| mainJar           | Object  | 主 jar 包或 Python 文件，`SQL` 不使用                                              |
| -- resourceName   | String  | jar 资源的完整名称                                                               |
| deployMode        | String  | 部署模式：`cluster`、`local`、`application` 或 `standalone`，可选项取决于 `flinkVersion` |
| appName           | String  | YARN 应用名称                                                                 |
| mainArgs          | String  | 主程序参数，支持 `${...}` 变量                                                      |
| others            | String  | 其他 Flink 命令选项，如 `-D`、`-C`、`-yt`                                           |
| yarnQueue         | String  | YARN 队列                                                                   |
| flinkVersion      | String  | 界面选择的 Flink 版本：`<1.10`、`1.11`、`>=1.12` 或 `>=1.13`                         |
| jobManagerMemory  | String  | JobManager 内存，如 `1G`                                                      |
| taskManagerMemory | String  | TaskManager 内存，如 `2G`                                                     |
| slot              | Integer | 每个 TaskManager 的 slot 数                                                   |
| taskManager       | Integer | TaskManager 数量                                                            |
| parallelism       | Integer | 作业并行度                                                                     |

**示例：**

```json
{
    "localParams": [
        {
            "prop": "dt",
            "direct": "IN",
            "type": "VARCHAR",
            "value": "${system.biz.date}"
        }
    ],
    "initScript": "",
    "rawScript": "",
    "resourceList": [],
    "programType": "JAVA",
    "mainClass": "org.apache.flink.streaming.examples.wordcount.WordCount",
    "mainJar": {
        "resourceName": "file:/tmp/dolphinscheduler/default/resources/flink-examples-streaming.jar"
    },
    "deployMode": "cluster",
    "appName": "flink-wordcount",
    "mainArgs": "--input /tmp/in.txt",
    "others": "",
    "yarnQueue": "default",
    "flinkVersion": ">=1.12",
    "jobManagerMemory": "1G",
    "taskManagerMemory": "2G",
    "slot": 1,
    "taskManager": 2,
    "parallelism": 1
}
```

### DATAX

使用 DataX 在数据源之间同步数据。`task_params` 由 `dolphinscheduler-task-datax` 模块的 `DataxParameters` 定义。

|       字段       |   类型    |                            描述                             |
|----------------|---------|-----------------------------------------------------------|
| localParams    | Array   | 本地参数，见 [公共字段](#公共字段)                                      |
| resourceList   | Array   | 辅助文件（如 keytab、xml），自定义配置时也可为 `.json` 作业文件，见 [公共字段](#公共字段) |
| customConfig   | Integer | `0` 表示在界面配置源和目标，`1` 表示使用自定义 DataX JSON                    |
| dsType         | String  | 源数据源类型，如 `MYSQL`；仅 `customConfig` 为 `0` 时存在               |
| dataSource     | Integer | 源数据源 id；仅 `customConfig` 为 `0` 时存在                        |
| dtType         | String  | 目标数据源类型，如 `MYSQL`；仅 `customConfig` 为 `0` 时存在              |
| dataTarget     | Integer | 目标数据源 id；仅 `customConfig` 为 `0` 时存在                       |
| sql            | String  | 在源端执行的查询 SQL；仅 `customConfig` 为 `0` 时存在                   |
| targetTable    | String  | 目标表；仅 `customConfig` 为 `0` 时存在                            |
| jobSpeedByte   | Integer | 字节限速，`0` 表示不限；仅 `customConfig` 为 `0` 时存在                  |
| jobSpeedRecord | Integer | 记录数限速；仅 `customConfig` 为 `0` 时存在                          |
| jobChannel     | Integer | 通道数；仅 `customConfig` 为 `0` 时存在                            |
| preStatements  | Array   | 同步前在目标端执行的 SQL；仅 `customConfig` 为 `0` 时存在                 |
| postStatements | Array   | 同步后在目标端执行的 SQL；仅 `customConfig` 为 `0` 时存在                 |
| json           | String  | DataX 作业 JSON；仅 `customConfig` 为 `1` 时存在                  |
| xms            | Integer | JVM 最小内存，单位 GB                                            |
| xmx            | Integer | JVM 最大内存，单位 GB                                            |

自定义配置（`customConfig` 为 `1`）的示例：

```json
{
    "localParams": [],
    "resourceList": [],
    "customConfig": 1,
    "json": "{\"job\":{\"setting\":{\"speed\":{\"channel\":1}},\"content\":[{\"reader\":{\"name\":\"mysqlreader\",\"parameter\":{}},\"writer\":{\"name\":\"mysqlwriter\",\"parameter\":{}}}]}}",
    "xms": 1,
    "xmx": 1
}
```

**示例：**

```json
{
    "localParams": [
        {
            "prop": "dt",
            "direct": "IN",
            "type": "VARCHAR",
            "value": "${system.biz.date}"
        }
    ],
    "resourceList": [],
    "customConfig": 0,
    "dsType": "MYSQL",
    "dataSource": 1,
    "dtType": "MYSQL",
    "dataTarget": 2,
    "sql": "select id, name from t_source",
    "targetTable": "t_target",
    "jobSpeedByte": 0,
    "jobSpeedRecord": 1000,
    "jobChannel": 1,
    "preStatements": [
        "truncate table t_target"
    ],
    "postStatements": [],
    "xms": 1,
    "xmx": 1
}
```

### SQOOP

使用 Sqoop 在 RDBMS 与 Hadoop 之间传输数据。`task_params` 由 `dolphinscheduler-task-sqoop` 模块的 `SqoopParameters` 定义。

|         字段          |   类型    |                                  描述                                  |
|---------------------|---------|----------------------------------------------------------------------|
| localParams         | Array   | 本地参数，见 [公共字段](#公共字段)                                                 |
| resourceList        | Array   | 任务使用的资源文件，见 [公共字段](#公共字段)                                            |
| jobType             | String  | `TEMPLATE`（界面配置）或 `CUSTOM`（自定义脚本）                                    |
| customShell         | String  | Sqoop 命令，仅 `jobType` 为 `CUSTOM` 时存在                                  |
| jobName             | String  | MapReduce 作业名称，仅 `jobType` 为 `TEMPLATE` 时存在                          |
| hadoopCustomParams  | Array   | Hadoop 自定义参数，与 `localParams` 一样是 `prop`/`direct`/`type`/`value` 对象列表 |
| sqoopAdvancedParams | Array   | Sqoop 高级参数，结构同 `hadoopCustomParams`                                  |
| concurrency         | Integer | 并行度（map 任务数）                                                         |
| splitBy             | String  | 用于切分数据的列                                                             |
| modelType           | String  | 方向：`import`（RDBMS 到 HDFS/Hive）或 `export`（HDFS/Hive 到 RDBMS）          |
| sourceType          | String  | 源类型：`MYSQL`、`ORACLE`、`SQLSERVER`、`HANA`、`HIVE` 或 `HDFS`              |
| targetType          | String  | 目标类型，可选项同 `sourceType`                                               |
| sourceParams        | String  | 源参数的 JSON 字符串，结构取决于 `sourceType`，见下文                                 |
| targetParams        | String  | 目标参数的 JSON 字符串，结构取决于 `targetType`，见下文                                |

`sourceParams` 和 `targetParams` 以 JSON 字符串存储，下表为解析后的内容。`jobType` 为 `CUSTOM` 时只保存 `localParams`、`resourceList`、`jobType` 和 `customShell`，不含仅模板模式使用的字段。

`sourceType` 为 RDBMS（`MYSQL`、`ORACLE`、`SQLSERVER`、`HANA`）时的 `sourceParams`：

|      字段       |   类型    |                           描述                            |
|---------------|---------|---------------------------------------------------------|
| srcDatasource | Integer | 源数据源 id                                                 |
| srcType       | String  | 数据源类型，由界面写入，后端忽略                                        |
| srcQueryType  | String  | `0` 表示表单（表），`1` 表示 SQL                                  |
| srcTable      | String  | 表名，仅 `srcQueryType` 为 `0` 时有值                           |
| srcColumnType | String  | `0` 表示全部列，`1` 表示部分列                                     |
| srcColumns    | String  | 逗号分隔的列名，仅 `srcColumnType` 为 `1` 时有值                     |
| srcQuerySql   | String  | 查询 SQL，仅 `srcQueryType` 为 `1` 时有值                       |
| mapColumnHive | Array   | 覆盖 SQL 到 Hive 的类型映射，`prop`/`direct`/`type`/`value` 对象列表 |
| mapColumnJava | Array   | 覆盖 SQL 到 Java 的类型映射，结构相同                                |

`sourceType` 为 `HIVE` 时的 `sourceParams`：`hiveDatabase`、`hiveTable`、`hivePartitionKey`、`hivePartitionValue`（均为 String）。`sourceType` 为 `HDFS` 时：`exportDir`（String，源目录）。

`targetType` 为 `HIVE` 时的 `targetParams`：

|         字段         |   类型    |             描述              |
|--------------------|---------|-----------------------------|
| hiveDatabase       | String  | Hive 库名                     |
| hiveTable          | String  | Hive 表名                     |
| createHiveTable    | Boolean | 创建 Hive 表，表已存在则任务失败         |
| dropDelimiter      | Boolean | 去除字符串字段中的 `\n`、`\r` 和 `\01` |
| hiveOverWrite      | Boolean | 覆盖已有数据                      |
| hiveTargetDir      | String  | 显式指定目标目录                    |
| replaceDelimiter   | String  | 替换 `\n`、`\r` 和 `\01` 的字符串   |
| hivePartitionKey   | String  | 分区键                         |
| hivePartitionValue | String  | 分区值                         |

`targetType` 为 `HDFS` 时的 `targetParams`：

|        字段        |   类型    |                                      描述                                      |
|------------------|---------|------------------------------------------------------------------------------|
| targetPath       | String  | HDFS 目标目录                                                                    |
| deleteTargetDir  | Boolean | 目标目录存在时先删除                                                                   |
| compressionCodec | String  | `snappy`、`lzo`、`gzip` 或空                                                     |
| fileType         | String  | `--as-avrodatafile`、`--as-sequencefile`、`--as-textfile` 或 `--as-parquetfile` |
| fieldsTerminated | String  | 字段分隔符                                                                        |
| linesTerminated  | String  | 行分隔符                                                                         |

`targetType` 为 RDBMS（`MYSQL`、`ORACLE`、`SQLSERVER`、`HANA`）时的 `targetParams`：

|        字段        |   类型    |              描述              |
|------------------|---------|------------------------------|
| targetDatasource | Integer | 目标数据源 id                     |
| targetType       | String  | 数据源类型，由界面写入，后端忽略             |
| targetTable      | String  | 目标表                          |
| targetColumns    | String  | 目标列                          |
| fieldsTerminated | String  | 字段分隔符                        |
| linesTerminated  | String  | 行分隔符                         |
| isUpdate         | Boolean | 是否更新已有行                      |
| targetUpdateKey  | String  | 更新键列                         |
| targetUpdateMode | String  | `updateonly` 或 `allowinsert` |

**示例：**

```json
{
    "localParams": [],
    "resourceList": [],
    "jobType": "TEMPLATE",
    "jobName": "sqoop_mysql_to_hive",
    "hadoopCustomParams": [],
    "sqoopAdvancedParams": [],
    "concurrency": 1,
    "splitBy": "",
    "modelType": "import",
    "sourceType": "MYSQL",
    "targetType": "HIVE",
    "targetParams": "{\"hiveDatabase\":\"tmp\",\"hiveTable\":\"example\",\"createHiveTable\":true,\"dropDelimiter\":false,\"hiveOverWrite\":true,\"hiveTargetDir\":\"\",\"replaceDelimiter\":\",\",\"hivePartitionKey\":\"\",\"hivePartitionValue\":\"\"}",
    "sourceParams": "{\"srcTable\":\"example\",\"srcColumnType\":\"0\",\"srcColumns\":\"\",\"srcQuerySql\":\"\",\"srcQueryType\":\"0\",\"srcType\":\"MYSQL\",\"srcDatasource\":1,\"mapColumnHive\":[],\"mapColumnJava\":[]}"
}
```

示例为 MySQL 导入 Hive。自定义脚本示例：

```json
{
    "localParams": [],
    "resourceList": [],
    "jobType": "CUSTOM",
    "customShell": "sqoop import --connect jdbc:mysql://localhost:3306/test --username root --table example --target-dir /tmp/example -m 1"
}
```

### SUB_WORKFLOW

SUB_WORKFLOW 任务把另一个工作流作为子工作流运行，`task_params` 由 `SubWorkflowParameters` 定义。

|           字段           |   类型   |         描述          |
|------------------------|--------|---------------------|
| localParams            | Array  | 本地参数，见[公共字段](#公共字段) |
| resourceList           | Array  | 该任务类型不使用，为空数组       |
| workflowDefinitionCode | Number | 子工作流定义的 code，必须大于 0 |

**示例：**

```json
{
    "localParams": [],
    "resourceList": [],
    "workflowDefinitionCode": 137713824398176
}
```

### DEPENDENT

DEPENDENT 任务检查其他工作流或任务在指定时间范围内是否运行成功，`task_params` 由 `DependentParameters` 定义。

|           字段            |   类型    |                                            描述                                            |
|-------------------------|---------|------------------------------------------------------------------------------------------|
| localParams             | Array   | 本地参数，见[公共字段](#公共字段)                                                                      |
| resourceList            | Array   | 该任务类型不使用，为空数组                                                                            |
| dependence              | Object  | 依赖配置，必填                                                                                  |
| -- relation             | String  | `dependTaskList` 中各组之间的关系：`AND` 或 `OR`                                                   |
| -- dependTaskList       | Array   | 依赖组列表，至少一项                                                                               |
| ---- relation           | String  | `dependItemList` 中各项之间的关系：`AND` 或 `OR`                                                   |
| ---- dependItemList     | Array   | 该组的依赖项                                                                                   |
| ------ dependentType    | String  | `DEPENDENT_ON_WORKFLOW`（依赖整个工作流）或 `DEPENDENT_ON_TASK`（依赖单个任务）。缺省时 UI 根据 `depTaskCode` 推断 |
| ------ projectCode      | Number  | 被依赖工作流所属项目的 code                                                                         |
| ------ definitionCode   | Number  | 被依赖工作流定义的 code                                                                           |
| ------ depTaskCode      | Number  | 被依赖任务的 code。依赖整个工作流时为 `0`；UI 在选择 `DEPENDENT_ON_TASK` 但尚未选任务时写入 `-1`，表示依赖工作流中的所有任务        |
| ------ cycle            | String  | 依赖周期：`hour`、`day`、`week` 或 `month`                                                       |
| ------ dateValue        | String  | 周期内的时间范围，取值见下表                                                                           |
| ------ parameterPassing | Boolean | 依赖成功时是否把被依赖方的输出参数传递给下游任务，默认 `false`                                                      |
| -- checkInterval        | Number  | 检查依赖状态的间隔，单位为秒。设置时必须大于 0，缺省为 `10`                                                        |
| -- failurePolicy        | String  | 依赖失败时的策略：`DEPENDENT_FAILURE_FAILURE`（当前任务失败）或 `DEPENDENT_FAILURE_WAITING`（继续等待）          |
| -- failureWaitingTime   | Number  | 等待时长，单位为分钟，仅在 `failurePolicy` 为 `DEPENDENT_FAILURE_WAITING` 时使用                          |

`cycle` 和 `dateValue` 的取值如下。`week` 和 `month` 中的 `thisWeek`、`thisMonth` 表示截至当前的自然周、自然月。

| cycle |                                                        dateValue                                                         |
|-------|--------------------------------------------------------------------------------------------------------------------------|
| hour  | `currentHour`、`last1Hour`、`last2Hours`、`last3Hours`、`last24Hours`                                                        |
| day   | `today`、`last1Days`、`last2Days`、`last3Days`、`last7Days`                                                                  |
| week  | `thisWeek`、`lastWeek`、`lastMonday`、`lastTuesday`、`lastWednesday`、`lastThursday`、`lastFriday`、`lastSaturday`、`lastSunday` |
| month | `thisMonth`、`thisMonthBegin`、`lastMonth`、`lastMonthBegin`、`lastMonthEnd`                                                 |

**示例：**

```json
{
    "localParams": [],
    "resourceList": [],
    "dependence": {
        "relation": "AND",
        "dependTaskList": [
            {
                "relation": "AND",
                "dependItemList": [
                    {
                        "dependentType": "DEPENDENT_ON_WORKFLOW",
                        "projectCode": 137713553063488,
                        "definitionCode": 137713824398176,
                        "depTaskCode": 0,
                        "cycle": "day",
                        "dateValue": "today",
                        "parameterPassing": false
                    },
                    {
                        "dependentType": "DEPENDENT_ON_TASK",
                        "projectCode": 137713553063488,
                        "definitionCode": 137713824398176,
                        "depTaskCode": 137713824398177,
                        "cycle": "week",
                        "dateValue": "lastWeek",
                        "parameterPassing": true
                    }
                ]
            }
        ],
        "checkInterval": 10,
        "failurePolicy": "DEPENDENT_FAILURE_WAITING",
        "failureWaitingTime": 30
    }
}
```

### CONDITIONS

CONDITIONS 任务检查上游任务的状态，并选择成功或失败分支，`task_params` 由 `ConditionsParameters` 定义。

|         字段          |   类型   |                   描述                   |
|---------------------|--------|----------------------------------------|
| localParams         | Array  | 本地参数，见[公共字段](#公共字段)                    |
| resourceList        | Array  | 该任务类型不使用，为空数组                          |
| dependence          | Object | 上游条件，必填                                |
| -- relation         | String | `dependTaskList` 中各组之间的关系：`AND` 或 `OR` |
| -- dependTaskList   | Array  | 条件组列表，至少一项                             |
| ---- relation       | String | `dependItemList` 中各项之间的关系：`AND` 或 `OR` |
| ---- dependItemList | Array  | 该组的条件项                                 |
| ------ depTaskCode  | Number | 需要检查的上游任务的 code                        |
| ------ status       | String | 上游任务的期望状态：`SUCCESS` 或 `FAILURE`        |
| conditionResult     | Object | 要执行的分支，必填                              |
| -- successNode      | Array  | 条件满足时执行的下游任务 code，至少一项                 |
| -- failedNode       | Array  | 条件不满足时执行的下游任务 code，至少一项                |

**示例：**

```json
{
    "localParams": [],
    "resourceList": [],
    "dependence": {
        "relation": "AND",
        "dependTaskList": [
            {
                "relation": "AND",
                "dependItemList": [
                    {
                        "depTaskCode": 137713824398177,
                        "status": "SUCCESS"
                    },
                    {
                        "depTaskCode": 137713824398178,
                        "status": "FAILURE"
                    }
                ]
            }
        ]
    },
    "conditionResult": {
        "successNode": [
            137713824398179
        ],
        "failedNode": [
            137713824398180
        ]
    }
}
```

Master 还会在运行时把 `conditionResult.conditionSuccess` 写入任务实例的 `task_params`，定义时不要设置。

### SWITCH

SWITCH 任务按顺序计算条件，执行第一个为真的条件对应的分支，`task_params` 由 `SwitchParameters` 定义。

|        字段         |   类型   |                               描述                               |
|-------------------|--------|----------------------------------------------------------------|
| localParams       | Array  | 本地参数，见[公共字段](#公共字段)                                            |
| resourceList      | Array  | 该任务类型不使用，为空数组                                                  |
| switchResult      | Object | 分支配置，必填                                                        |
| -- dependTaskList | Array  | 条件列表，按顺序计算，第一个为真的生效                                            |
| ---- condition    | String | 表达式，可引用 `${flag}` 等参数，由 `javax.script.ScriptEngine` 计算。计算出错视为假 |
| ---- nextNode     | Number | 条件为真时执行的下游任务 code                                              |
| -- nextNode       | Number | 默认下游任务 code，所有条件都不满足时执行                                        |

**示例：**

```json
{
    "localParams": [],
    "resourceList": [],
    "switchResult": {
        "dependTaskList": [
            {
                "condition": "${flag} == 1",
                "nextNode": 137713824398179
            },
            {
                "condition": "${flag} == 2",
                "nextNode": 137713824398180
            }
        ],
        "nextNode": 137713824398181
    }
}
```

Master 还会在任务实例的 `task_params` 中写入选中的分支 `nextBranch`，定义时不要设置。
