# Task Structure

## Overall Storage Structure

A workflow is not stored as a single JSON document. Its definition is split into three tables, and each table has a
`*_log` table that keeps every version:

|             Table             |      Version table (`*_log`)      |                         Content                          |
|-------------------------------|-----------------------------------|----------------------------------------------------------|
| `t_ds_workflow_definition`    | `t_ds_workflow_definition_log`    | Workflow level attributes, one row per workflow          |
| `t_ds_task_definition`        | `t_ds_task_definition_log`        | Task level attributes, one row per task                  |
| `t_ds_workflow_task_relation` | `t_ds_workflow_task_relation_log` | The edges of the DAG, one row per edge between two tasks |

- Workflows and tasks are identified by a globally unique `code` plus a `version`. The main tables hold the current
  version, and the `*_log` tables hold all versions with two extra columns: `operator` (user id) and `operate_time`.
- Relations reference tasks by `code + version`, and a workflow instance (`t_ds_workflow_instance`) or task instance
  (`t_ds_task_instance`) references the exact definition version it was created from.
- The task type specific configuration is stored as JSON in `t_ds_task_definition.task_params`, see
  [Task Params](#task-params).

The table schema is defined by the SQL files under `dolphinscheduler-dao/src/main/resources/sql`, which are the source of
truth. The following sections describe the commonly used columns. The column types
shown follow `dolphinscheduler_mysql.sql`; see `dolphinscheduler_postgresql.sql` and `dolphinscheduler_h2.sql` for
PostgreSQL and H2.

## Workflow Definition

Table `t_ds_workflow_definition`:

|      Column      |     Type     |                                        Description                                         |
|------------------|--------------|--------------------------------------------------------------------------------------------|
| id               | int(11)      | Primary key                                                                                |
| code             | bigint(20)   | Workflow definition code, globally unique                                                  |
| name             | varchar(255) | Workflow definition name, unique within a project                                          |
| version          | int(11)      | Workflow definition version                                                                |
| description      | text         | Description                                                                                |
| project_code     | bigint(20)   | Code of the project the workflow belongs to                                                |
| release_state    | tinyint(4)   | Release state: 0 offline, 1 online                                                         |
| user_id          | int(11)      | Creator id                                                                                 |
| global_params    | text         | Global parameters, a JSON array of [parameters](#common-fields)                            |
| flag             | tinyint(4)   | Whether the workflow is available: 0 no, 1 yes                                             |
| locations        | text         | Node coordinates on the canvas, e.g. `[{"taskCode":123,"x":59,"y":61}]`                    |
| warning_group_id | int(11)      | Alert group copied from the last created or updated schedule; not cleared if it is deleted |
| timeout          | int(11)      | Timeout, in minutes                                                                        |
| execution_type   | tinyint(4)   | Execution type: 0 parallel, 1 serial wait, 2 serial discard, 3 serial priority             |
| create_time      | datetime     | Create time                                                                                |
| update_time      | datetime     | Update time                                                                                |

The tenant is not part of the workflow definition. It is chosen when the workflow is started or scheduled, and is stored
in `t_ds_schedules.tenant_code` and `t_ds_workflow_instance.tenant_code`.

## Task Definition

Table `t_ds_task_definition`:

|         Column          |     Type     |                              Description                               |
|-------------------------|--------------|------------------------------------------------------------------------|
| id                      | int(11)      | Primary key                                                            |
| code                    | bigint(20)   | Task definition code, globally unique                                  |
| name                    | varchar(255) | Task definition name                                                   |
| version                 | int(11)      | Task definition version                                                |
| description             | text         | Description                                                            |
| project_code            | bigint(20)   | Code of the project the task belongs to                                |
| user_id                 | int(11)      | Creator id                                                             |
| task_type               | varchar(50)  | Task type, e.g. `SHELL`, `SQL`, `SUB_WORKFLOW`                         |
| task_execute_type       | int(11)      | Task execute type: 0 batch, 1 stream                                   |
| task_params             | longtext     | Task type specific parameters in JSON, see [Task Params](#task-params) |
| flag                    | tinyint(2)   | Whether the task runs normally: 0 forbidden, 1 normal                  |
| task_priority           | tinyint(4)   | Priority: 0 highest, 1 high, 2 medium, 3 low, 4 lowest                 |
| worker_group            | varchar(255) | Worker group                                                           |
| environment_code        | bigint(20)   | Environment code, -1 means no environment                              |
| fail_retry_times        | int(11)      | Number of retries after failure                                        |
| fail_retry_interval     | int(11)      | Retry interval, in minutes                                             |
| timeout_flag            | tinyint(2)   | Whether timeout alarm is enabled: 0 close, 1 open                      |
| timeout_notify_strategy | tinyint(4)   | Timeout strategy: 0 warn, 1 failed, 2 warn and failed                  |
| timeout                 | int(11)      | Timeout, in minutes                                                    |
| delay_time              | int(11)      | Delay execution time, in minutes                                       |
| resource_ids            | text         | Resource ids, separated by commas                                      |
| task_group_id           | int(11)      | Task group id                                                          |
| task_group_priority     | tinyint(4)   | Priority inside the task group                                         |
| cpu_quota               | int(11)      | CPU quota in percent, -1 means unlimited                               |
| memory_max              | int(11)      | Max memory in MB, -1 means unlimited                                   |
| create_time             | datetime     | Create time                                                            |
| update_time             | datetime     | Update time                                                            |

## Task Relation

Table `t_ds_workflow_task_relation`:

|           Column            |     Type     |                         Description                         |
|-----------------------------|--------------|-------------------------------------------------------------|
| id                          | int(11)      | Primary key                                                 |
| name                        | varchar(255) | Relation name                                               |
| project_code                | bigint(20)   | Project code                                                |
| workflow_definition_code    | bigint(20)   | Workflow definition code                                    |
| workflow_definition_version | int(11)      | Workflow definition version                                 |
| pre_task_code               | bigint(20)   | Code of the upstream task, 0 if the task has no upstream    |
| pre_task_version            | int(11)      | Version of the upstream task, 0 if the task has no upstream |
| post_task_code              | bigint(20)   | Code of the downstream task                                 |
| post_task_version           | int(11)      | Version of the downstream task                              |
| condition_type              | tinyint(2)   | Condition type: 0 none, 1 judge, 2 delay                    |
| condition_params            | text         | Condition parameters in JSON                                |
| create_time                 | datetime     | Create time                                                 |
| update_time                 | datetime     | Update time                                                 |

Every task in a workflow appears as `post_task_code` in at least one row. For example, a workflow where task `A` runs
first and then `B` and `C` run in parallel is stored as three rows:

| pre_task_code | post_task_code |
|---------------|----------------|
| 0             | A              |
| A             | B              |
| A             | C              |

## Task Params

`t_ds_task_definition.task_params` stores the parameters of one task as a JSON object. Its structure depends on
`task_type` and is defined by the parameters class of the task plugin, which extends `AbstractParameters` in
`dolphinscheduler-task-plugin/dolphinscheduler-task-api`.

### Common Fields

|      Field      |  Type   |                                                                                                                Description                                                                                                                 |
|-----------------|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| localParams     | Array   | Local parameters of the task                                                                                                                                                                                                               |
| -- prop         | String  | Parameter name                                                                                                                                                                                                                             |
| -- direct       | String  | Direction: `IN` or `OUT`                                                                                                                                                                                                                   |
| -- type         | String  | Data type, e.g. `VARCHAR`, `INTEGER`, `DATE`                                                                                                                                                                                               |
| -- value        | String  | Parameter value                                                                                                                                                                                                                            |
| -- sensitive    | Boolean | Whether the value is sensitive, default `false`, see below                                                                                                                                                                                 |
| resourceList    | Array   | Resource files used by the task. Not a field of `AbstractParameters`: only task types that use resource files define it (e.g. SHELL, PYTHON, SPARK, MR, FLINK, DATAX). The UI sends it for every task type, and other task types ignore it |
| -- resourceName | String  | The exact `fullName` returned by the resource center, e.g. `/tmp/dolphinscheduler/default/resources/run.sh` with the default LOCAL storage                                                                                                 |

`global_params` of the workflow definition uses the same structure as `localParams`.

The backend omits `sensitive` when it is `false`, so a missing `sensitive` means `false` (a client may still send
`"sensitive": false`). The value of a sensitive parameter is stored in plain text in the database, and query APIs return
it as `******`:

- On create, `******` is rejected as a value.
- On update, `******` keeps the stored value only for a parameter that is marked sensitive and already exists with the
  same name. It is rejected for a new sensitive parameter, or when `sensitive` is switched off.
- When a workflow is started or backfilled, a start parameter whose value is `******` takes the value of the global
  parameter with the same name.

### Parameters of Each Task Type

The table below lists the parameters class of each task type described on this page. The following subsections describe
their fields and give an example of each.

|  Task type   |                    Parameters class                     |                              Main fields                               |
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

Other task types follow the same pattern: find the `*Parameters` class in the corresponding
`dolphinscheduler-task-plugin/dolphinscheduler-task-<type>` module.

### SHELL

Runs a shell script on the worker. `task_params` is defined by `ShellParameters` in `dolphinscheduler-task-shell`.

|    Field     |  Type  |                              Description                               |
|--------------|--------|------------------------------------------------------------------------|
| localParams  | Array  | Local parameters, see [Common Fields](#common-fields)                  |
| resourceList | Array  | Resource files used by the script, see [Common Fields](#common-fields) |
| rawScript    | String | Shell script content                                                   |

**Example:**

```json
{
    "localParams": [
        {
            "prop": "dt",
            "direct": "IN",
            "type": "VARCHAR",
            "value": "${system.datetime}"
        },
        {
            "prop": "db_password",
            "direct": "IN",
            "type": "VARCHAR",
            "value": "my_secret",
            "sensitive": true
        }
    ],
    "rawScript": "echo \"dt=${dt}\"\nsh ./run.sh",
    "resourceList": [
        {
            "resourceName": "/tmp/dolphinscheduler/default/resources/run.sh"
        }
    ]
}
```

### PYTHON

Runs a Python script on the worker. `task_params` is defined by `PythonParameters` in `dolphinscheduler-task-python`.

|    Field     |  Type  |                              Description                               |
|--------------|--------|------------------------------------------------------------------------|
| localParams  | Array  | Local parameters, see [Common Fields](#common-fields)                  |
| resourceList | Array  | Resource files used by the script, see [Common Fields](#common-fields) |
| rawScript    | String | Python script content                                                  |

**Example:**

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
            "resourceName": "/tmp/dolphinscheduler/default/resources/utils.py"
        }
    ]
}
```

### SQL

Runs SQL statements against a datasource. `task_params` is defined by `SqlParameters` in `dolphinscheduler-task-api`.

|     Field      |  Type   |                                                       Description                                                       |
|----------------|---------|-------------------------------------------------------------------------------------------------------------------------|
| localParams    | Array   | Local parameters, replace `${variable}` in the SQL, see [Common Fields](#common-fields)                                 |
| resourceList   | Array   | Sent by the UI for every task type, not used by this task. The SQL file of `sqlSource` `FILE` is given by `sqlResource` |
| type           | String  | Datasource type, e.g. `MYSQL`, `POSTGRESQL`, `HIVE`                                                                     |
| datasource     | Integer | Datasource id                                                                                                           |
| sql            | String  | SQL statements; run when not empty, taking precedence over `sqlResource`                                                |
| sqlSource      | String  | SQL source chosen in the UI: `SCRIPT` inline text, `FILE` resource center file. Not read by the worker                  |
| sqlResource    | String  | Resource center file path, read only when `sql` is empty                                                                |
| sqlType        | Integer | SQL type: `0` query, `1` non-query. The UI sends it as the string `"0"` or `"1"`, and both forms are accepted           |
| preStatements  | Array   | SQL statements executed before the main SQL                                                                             |
| postStatements | Array   | SQL statements executed after the main SQL                                                                              |
| sendEmail      | Boolean | Whether to send the query result by alert, query type only                                                              |
| displayRows    | Integer | Max rows of the query result shown in the log, query type only                                                          |
| title          | String  | Alert title, only when `sendEmail` is true                                                                              |
| groupId        | Integer | Alert group id, only when `sendEmail` is true                                                                           |
| limit          | Integer | Max rows fetched by a query, the default is used when it is less than or equal to 0                                     |

**Example:**

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

Calls a stored procedure of a datasource. `task_params` is defined by `ProcedureParameters` in `dolphinscheduler-task-procedure`.

|    Field     |  Type   |                                                                        Description                                                                         |
|--------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| localParams  | Array   | Procedure parameters, `IN` and `OUT`, see [Common Fields](#common-fields)                                                                                  |
| resourceList | Array   | Sent by the UI for every task type, not used by this task                                                                                                  |
| type         | String  | Datasource type, e.g. `MYSQL`, `POSTGRESQL`                                                                                                                |
| datasource   | Integer | Datasource id                                                                                                                                              |
| method       | String  | Call statement of the stored procedure. `${param}` placeholders are bound to the `IN`/`OUT` localParams, e.g. `call proc_daily_stat(${in_dt}, ${out_cnt})` |

**Example:**

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
    "method": "call proc_daily_stat(${in_dt}, ${out_cnt})"
}
```

### HTTP

Sends an HTTP request and checks the response. `task_params` is defined by `HttpParameters` in `dolphinscheduler-task-http`.

|         Field         |  Type   |                                                                                          Description                                                                                           |
|-----------------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| localParams           | Array   | Local parameters, see [Common Fields](#common-fields)                                                                                                                                          |
| resourceList          | Array   | Sent by the UI for every task type, not used by this task                                                                                                                                      |
| url                   | String  | Request URL                                                                                                                                                                                    |
| httpMethod            | String  | Request method: `GET`, `POST`, `PUT`, `DELETE`                                                                                                                                                 |
| httpParams            | Array   | Request parameters and headers                                                                                                                                                                 |
| -- prop               | String  | Parameter or header name                                                                                                                                                                       |
| -- httpParametersType | String  | Kind: `PARAMETER` (sent as a URL query parameter) or `HEADERS`. A `Content-Type` header is not sent as is: it only selects `application/json` (default) or `application/x-www-form-urlencoded` |
| -- value              | String  | Parameter or header value                                                                                                                                                                      |
| httpBody              | String  | Request body, must be a JSON object; `${param}` placeholders are replaced. Only sent for `POST` and `PUT`                                                                                      |
| httpCheckCondition    | String  | Check condition: `STATUS_CODE_DEFAULT` (200), `STATUS_CODE_CUSTOM`, `BODY_CONTAINS`, `BODY_NOT_CONTAINS`                                                                                       |
| condition             | String  | Check content, required unless the condition is `STATUS_CODE_DEFAULT`                                                                                                                          |
| connectTimeout        | Integer | Timeout in milliseconds, applied to connect, read and write; must be greater than 0                                                                                                            |
| socketTimeout         | Integer | Submitted by the UI but ignored by the backend; `connectTimeout` is used for connect, read and write                                                                                           |
| socketKeepAlive       | Boolean | Whether to enable TCP keep-alive                                                                                                                                                               |

**Example:**

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
    "socketTimeout": 60000,
    "socketKeepAlive": false
}
```

### SPARK

Runs a Spark application with `spark-submit` or `spark-sql`. `task_params` is defined by `SparkParameters` in `dolphinscheduler-task-spark`.

|      Field       |  Type   |                                                                        Description                                                                         |
|------------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| localParams      | Array   | Local parameters, see [Common Fields](#common-fields)                                                                                                      |
| rawScript        | String  | SQL script, used only when `programType` is `SQL` and `sqlExecutionType` is `SCRIPT`; empty otherwise                                                      |
| resourceList     | Array   | Resource files used by the task, see [Common Fields](#common-fields). Required when `programType` is `SQL` and `sqlExecutionType` is `FILE` (the SQL file) |
| programType      | String  | Program type: `JAVA`, `SCALA`, `PYTHON` or `SQL`                                                                                                           |
| mainClass        | String  | Full class name of the main function, not used for `PYTHON` and `SQL`                                                                                      |
| mainJar          | Object  | Main jar or Python file, not used for `SQL`                                                                                                                |
| -- resourceName  | String  | Full name of the jar resource                                                                                                                              |
| deployMode       | String  | Deployment mode: `cluster`, `client` or `local`, empty means `local`; `SQL` supports `client` and `local`                                                  |
| appName          | String  | Task name passed to Spark                                                                                                                                  |
| mainArgs         | String  | Arguments of the main program, supports `${...}` variables                                                                                                 |
| others           | String  | Other `spark-submit` options, e.g. `--conf`, `--jars`                                                                                                      |
| yarnQueue        | String  | YARN queue, ignored in `local` mode or when `--queue` is given in `others`                                                                                 |
| master           | String  | Master URL, optional. If empty, it is derived as `local` (local mode), `k8s://...` (`namespace` set) or `yarn`                                             |
| driverCores      | Integer | Number of driver cores                                                                                                                                     |
| driverMemory     | String  | Driver memory, e.g. `512M`                                                                                                                                 |
| numExecutors     | Integer | Number of executors                                                                                                                                        |
| executorMemory   | String  | Memory per executor, e.g. `2G`                                                                                                                             |
| executorCores    | Integer | Cores per executor                                                                                                                                         |
| sqlExecutionType | String  | How the SQL is provided when `programType` is `SQL`: `SCRIPT` (inline `rawScript`) or `FILE` (a resource file)                                             |

`namespace` (String, a JSON string holding the Kubernetes namespace, e.g. `{"name":"ns","cluster":"c"}`) is stored only when a namespace is selected to submit to native Kubernetes; it is omitted for YARN.

**Example:**

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
        "resourceName": "/tmp/dolphinscheduler/default/resources/spark-examples_2.12-3.2.0.jar"
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

Runs a Hadoop MapReduce job. `task_params` is defined by `MapReduceParameters` in `dolphinscheduler-task-mr`.

|      Field      |  Type  |                               Description                               |
|-----------------|--------|-------------------------------------------------------------------------|
| localParams     | Array  | Local parameters, see [Common Fields](#common-fields)                   |
| resourceList    | Array  | Resource files used by the task, see [Common Fields](#common-fields)    |
| programType     | String | Program type: `JAVA`, `SCALA` or `PYTHON`                               |
| mainClass       | String | Full class name of the main function, not used for `PYTHON`             |
| mainJar         | Object | Jar run by `hadoop jar`; the Hadoop streaming jar for `PYTHON`          |
| -- resourceName | String | Full name of the jar resource                                           |
| appName         | String | MapReduce job name                                                      |
| mainArgs        | String | Command line arguments of the program, supports `${...}` variables      |
| others          | String | Other options in `-D`, `-files`, `-libjars`, `-archives` format         |
| yarnQueue       | String | YARN queue, ignored when `mapreduce.job.queuename` is given in `others` |

**Example:**

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
        "resourceName": "/tmp/dolphinscheduler/default/resources/hadoop-mapreduce-examples.jar"
    },
    "appName": "mr-wordcount",
    "mainArgs": "/input /output",
    "others": "-D mapreduce.job.reduces=1",
    "yarnQueue": "default"
}
```

### FLINK

Runs a Flink job with the `flink` command line. `task_params` is defined by `FlinkParameters` in `dolphinscheduler-task-flink`.

|       Field       |  Type   |                                                                                                  Description                                                                                                  |
|-------------------|---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| localParams       | Array   | Local parameters, see [Common Fields](#common-fields)                                                                                                                                                         |
| initScript        | String  | Initialization SQL, used only when `programType` is `SQL`; empty otherwise                                                                                                                                    |
| rawScript         | String  | SQL script, used only when `programType` is `SQL`; empty otherwise                                                                                                                                            |
| resourceList      | Array   | Resource files used by the task, see [Common Fields](#common-fields)                                                                                                                                          |
| programType       | String  | Program type: `JAVA`, `SCALA`, `PYTHON` or `SQL`                                                                                                                                                              |
| mainClass         | String  | Full class name of the main function, not used for `PYTHON` and `SQL`                                                                                                                                         |
| mainJar           | Object  | Main jar or Python file, not used for `SQL`                                                                                                                                                                   |
| -- resourceName   | String  | Full name of the jar resource                                                                                                                                                                                 |
| deployMode        | String  | Deployment mode: `cluster`, `local`, `application` or `standalone`. `SQL`: `cluster`, `local` or `standalone`; other program types: `cluster` or `local`, plus `application` unless `flinkVersion` is `<1.10` |
| appName           | String  | YARN application name                                                                                                                                                                                         |
| mainArgs          | String  | Arguments of the main program, supports `${...}` variables                                                                                                                                                    |
| others            | String  | Other Flink command options, e.g. `-D`, `-C`, `-yt`                                                                                                                                                           |
| yarnQueue         | String  | YARN queue, only used in `cluster` and `application` modes (for `SQL`, not used in `local` and `standalone` modes)                                                                                            |
| flinkVersion      | String  | Flink version selected in the UI: `<1.10`, `1.11` or `>=1.12` for `JAVA`/`SCALA`/`PYTHON`; always `>=1.13` for `SQL`                                                                                          |
| jobManagerMemory  | String  | JobManager memory, e.g. `1G`                                                                                                                                                                                  |
| taskManagerMemory | String  | TaskManager memory, e.g. `2G`                                                                                                                                                                                 |
| slot              | Integer | Number of slots per TaskManager                                                                                                                                                                               |
| taskManager       | Integer | Number of TaskManagers (`-yn`), only used when `flinkVersion` is `<1.10` and `deployMode` is not `local`                                                                                                      |
| parallelism       | Integer | Parallelism of the job                                                                                                                                                                                        |

**Example:**

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
        "resourceName": "/tmp/dolphinscheduler/default/resources/flink-examples-streaming.jar"
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

Synchronizes data between datasources with DataX. `task_params` is defined by `DataxParameters` in `dolphinscheduler-task-datax`.

|     Field      |  Type   |                                                                                        Description                                                                                         |
|----------------|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| localParams    | Array   | Local parameters, see [Common Fields](#common-fields)                                                                                                                                      |
| resourceList   | Array   | Auxiliary files (e.g. keytab, xml). For a custom config without inline `json`, it must contain exactly one `.json` file that holds the job definition; see [Common Fields](#common-fields) |
| customConfig   | Integer | `0` to configure source and target in the UI, `1` to use a custom DataX JSON                                                                                                               |
| dsType         | String  | Source datasource type, e.g. `MYSQL`; only when `customConfig` is `0`                                                                                                                      |
| dataSource     | Integer | Source datasource id; only when `customConfig` is `0`                                                                                                                                      |
| dtType         | String  | Target datasource type, e.g. `MYSQL`; only when `customConfig` is `0`                                                                                                                      |
| dataTarget     | Integer | Target datasource id; only when `customConfig` is `0`                                                                                                                                      |
| sql            | String  | Query SQL on the source; only when `customConfig` is `0`                                                                                                                                   |
| targetTable    | String  | Target table; only when `customConfig` is `0`                                                                                                                                              |
| jobSpeedByte   | Integer | Speed limit in bytes, `0` for no limit; only when `customConfig` is `0`                                                                                                                    |
| jobSpeedRecord | Integer | Speed limit in records, `0` for no limit; only when `customConfig` is `0`                                                                                                                  |
| jobChannel     | Integer | Number of channels; only when `customConfig` is `0`                                                                                                                                        |
| preStatements  | Array   | SQL statements executed on the target before the sync; only when `customConfig` is `0`                                                                                                     |
| postStatements | Array   | SQL statements executed on the target after the sync; only when `customConfig` is `0`                                                                                                      |
| batchSize      | Integer | Writer batch size, written to the DataX writer only when greater than 0; only when `customConfig` is `0`. The UI form does not save it currently                                           |
| json           | String  | DataX job JSON; only when `customConfig` is `1`. If it is blank or `{}`, the job definition is read from the single `.json` file in `resourceList`                                         |
| xms            | Integer | Minimum JVM memory in GB, values below 1 are treated as 1                                                                                                                                  |
| xmx            | Integer | Maximum JVM memory in GB                                                                                                                                                                   |

Example with a custom config (`customConfig` is `1`):

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

**Example:**

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

Transfers data between RDBMS and Hadoop with Sqoop. `task_params` is defined by `SqoopParameters` in `dolphinscheduler-task-sqoop`.

|        Field        |  Type   |                                                  Description                                                  |
|---------------------|---------|---------------------------------------------------------------------------------------------------------------|
| localParams         | Array   | Local parameters, see [Common Fields](#common-fields)                                                         |
| resourceList        | Array   | Sent by the UI, not used: `SqoopParameters` does not define it                                                |
| jobType             | String  | `TEMPLATE` (configure in the UI) or `CUSTOM` (custom shell)                                                   |
| customShell         | String  | Sqoop command, only when `jobType` is `CUSTOM`                                                                |
| jobName             | String  | MapReduce job name, only when `jobType` is `TEMPLATE`                                                         |
| hadoopCustomParams  | Array   | Hadoop custom parameters, a list of `prop`/`direct`/`type`/`value` objects like `localParams`                 |
| sqoopAdvancedParams | Array   | Sqoop advanced parameters, same structure as `hadoopCustomParams`                                             |
| concurrency         | Integer | Parallelism (`-m`, number of map tasks), must not be `0` for `TEMPLATE`                                       |
| splitBy             | String  | Column used to split the data across map tasks (`--split-by`), only used when `concurrency` is greater than 1 |
| modelType           | String  | Direction: `import` (RDBMS to HDFS/Hive) or `export` (HDFS/Hive to RDBMS)                                     |
| sourceType          | String  | Source type: `MYSQL`, `ORACLE`, `SQLSERVER`, `HANA`, `HIVE` or `HDFS`                                         |
| targetType          | String  | Target type, same options as `sourceType`                                                                     |
| sourceParams        | String  | JSON string of source parameters, depends on `sourceType`, see below                                          |
| targetParams        | String  | JSON string of target parameters, depends on `targetType`, see below                                          |

`sourceParams` and `targetParams` are stored as JSON strings. The fields below are the content after parsing. For `jobType` `CUSTOM`, only `localParams`, `resourceList`, `jobType` and `customShell` are stored; the template-only fields are omitted.

`sourceParams` when `sourceType` is an RDBMS (`MYSQL`, `ORACLE`, `SQLSERVER`, `HANA`):

|     Field     |  Type   |                                Description                                |
|---------------|---------|---------------------------------------------------------------------------|
| srcDatasource | Integer | Source datasource id                                                      |
| srcType       | String  | Datasource type, written by the UI and ignored by the backend             |
| srcQueryType  | String  | `0` for form (table), `1` for SQL                                         |
| srcTable      | String  | Table name, only when `srcQueryType` is `0`                               |
| srcColumnType | String  | `0` for all columns, `1` for some columns                                 |
| srcColumns    | String  | Comma separated columns, only when `srcColumnType` is `1`                 |
| srcQuerySql   | String  | Query SQL, only when `srcQueryType` is `1`                                |
| mapColumnHive | Array   | Override SQL-to-Hive type mapping, `prop`/`direct`/`type`/`value` objects |
| mapColumnJava | Array   | Override SQL-to-Java type mapping, same structure                         |

`sourceParams` when `sourceType` is `HIVE`: `hiveDatabase`, `hiveTable`, `hivePartitionKey`, `hivePartitionValue` (all String). When `sourceType` is `HDFS`: `exportDir` (String, source directory).

`targetParams` when `targetType` is `HIVE`:

|       Field        |  Type   |                           Description                            |
|--------------------|---------|------------------------------------------------------------------|
| hiveDatabase       | String  | Hive database                                                    |
| hiveTable          | String  | Hive table                                                       |
| createHiveTable    | Boolean | Create the Hive table, the job fails if it exists                |
| dropDelimiter      | Boolean | Drop `\n`, `\r` and `\01` from string fields                     |
| hiveOverWrite      | Boolean | Overwrite existing data (`--hive-overwrite --delete-target-dir`) |
| hiveTargetDir      | String  | Explicit target directory                                        |
| replaceDelimiter   | String  | Replacement for `\n`, `\r` and `\01`                             |
| hivePartitionKey   | String  | Partition key                                                    |
| hivePartitionValue | String  | Partition value                                                  |

`targetParams` when `targetType` is `HDFS`:

|      Field       |  Type   |                                   Description                                   |
|------------------|---------|---------------------------------------------------------------------------------|
| targetPath       | String  | HDFS target directory                                                           |
| deleteTargetDir  | Boolean | Delete the target directory if it exists                                        |
| compressionCodec | String  | `snappy`, `lzo`, `gzip` or empty                                                |
| fileType         | String  | `--as-avrodatafile`, `--as-sequencefile`, `--as-textfile` or `--as-parquetfile` |
| fieldsTerminated | String  | Field separator                                                                 |
| linesTerminated  | String  | Line separator                                                                  |

`targetParams` when `targetType` is an RDBMS (`MYSQL`, `ORACLE`, `SQLSERVER`, `HANA`):

|      Field       |  Type   |                          Description                          |
|------------------|---------|---------------------------------------------------------------|
| targetDatasource | Integer | Target datasource id                                          |
| targetType       | String  | Datasource type, written by the UI and ignored by the backend |
| targetTable      | String  | Target table                                                  |
| targetColumns    | String  | Target columns                                                |
| fieldsTerminated | String  | Field separator                                               |
| linesTerminated  | String  | Line separator                                                |
| isUpdate         | Boolean | Update existing rows                                          |
| targetUpdateKey  | String  | Update key columns                                            |
| targetUpdateMode | String  | `updateonly` or `allowinsert`                                 |

**Example:**

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

The example is an import from MySQL to Hive. Custom shell example:

```json
{
    "localParams": [],
    "resourceList": [],
    "jobType": "CUSTOM",
    "customShell": "sqoop import --connect jdbc:mysql://localhost:3306/test --username root --table example --target-dir /tmp/example -m 1"
}
```

### SUB_WORKFLOW

The SUB_WORKFLOW task runs another workflow as a child workflow. Its `task_params` is defined by `SubWorkflowParameters`.

|         Field          |  Type  |                          Description                          |
|------------------------|--------|---------------------------------------------------------------|
| localParams            | Array  | Local parameters, see [Common Fields](#common-fields)         |
| resourceList           | Array  | Not used by this task type, an empty array                    |
| workflowDefinitionCode | Number | Code of the child workflow definition, must be greater than 0 |

**Example:**

```json
{
    "localParams": [],
    "resourceList": [],
    "workflowDefinitionCode": 137713824398176
}
```

### DEPENDENT

The DEPENDENT task checks whether other workflows or tasks have run successfully in a given time range. Its `task_params` is defined by `DependentParameters`.

|          Field          |  Type   |                                                                                                                                    Description                                                                                                                                    |
|-------------------------|---------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| localParams             | Array   | Local parameters, see [Common Fields](#common-fields)                                                                                                                                                                                                                             |
| resourceList            | Array   | Not used by this task type, an empty array                                                                                                                                                                                                                                        |
| dependence              | Object  | Dependency configuration, required                                                                                                                                                                                                                                                |
| -- relation             | String  | Relation between the groups in `dependTaskList`: `AND` or `OR`                                                                                                                                                                                                                    |
| -- dependTaskList       | Array   | Dependency groups, at least one                                                                                                                                                                                                                                                   |
| ---- relation           | String  | Relation between the items in `dependItemList`: `AND` or `OR`                                                                                                                                                                                                                     |
| ---- dependItemList     | Array   | Dependency items of the group                                                                                                                                                                                                                                                     |
| ------ dependentType    | String  | `DEPENDENT_ON_WORKFLOW` (the whole workflow) or `DEPENDENT_ON_TASK` (one task). Only used by the UI, which infers it from `depTaskCode` when it is omitted                                                                                                                        |
| ------ projectCode      | Number  | Code of the project of the dependent workflow                                                                                                                                                                                                                                     |
| ------ definitionCode   | Number  | Code of the dependent workflow definition                                                                                                                                                                                                                                         |
| ------ depTaskCode      | Number  | `0`: the whole workflow (the workflow instance succeeded). `-1` (`ALL` in the UI): the workflow instance succeeded and every enabled task in it has a successful instance. Any other value: the code of one task                                                                  |
| ------ cycle            | String  | Dependency cycle: `hour`, `day`, `week` or `month`. Only used by the UI to offer `dateValue` options                                                                                                                                                                              |
| ------ dateValue        | String  | Time range inside the cycle, see the table below                                                                                                                                                                                                                                  |
| ------ parameterPassing | Boolean | Whether to pass the `OUT` parameters in the varPool of the dependent workflow instance to downstream tasks when the dependency succeeds. Only takes effect when `depTaskCode` is `0` or `-1`, or the dependent task is a streaming task. Default `false`                          |
| -- checkInterval        | Number  | Interval of checking the dependency status, in seconds. Must be greater than 0 if set, default `10` when omitted                                                                                                                                                                  |
| -- failurePolicy        | String  | What to do when a dependency failed: `DEPENDENT_FAILURE_FAILURE` (the task fails) or `DEPENDENT_FAILURE_WAITING` (still waiting until `failureWaitingTime` minutes after the base time, then fail). If `failurePolicy` or `failureWaitingTime` is omitted, the task fails at once |
| -- failureWaitingTime   | Number  | Waiting time in minutes, only used when `failurePolicy` is `DEPENDENT_FAILURE_WAITING`                                                                                                                                                                                            |

A failed dependency result is cached and not checked again, so `DEPENDENT_FAILURE_WAITING` only delays the failure; a
later successful instance does not change it.

`cycle` and `dateValue` take the following values. All ranges are relative to the schedule time of the current workflow
instance, or the time the DEPENDENT task started if it was not scheduled. Ranges are split into days: `thisWeek` and `thisMonth` cover each day from Monday or the first day of the month through the base day, and every day must have a successful instance. The master only uses `dateValue` and `depTaskCode`. The backend also accepts `thisMonthEnd`,
which the UI does not offer.

A dependency on the task's own workflow (`-1` or the task itself) is treated as successful for the first instance of that workflow (the first scheduled instance if the workflow has any, otherwise the first started instance).

| cycle |                                                            dateValue                                                             |
|-------|----------------------------------------------------------------------------------------------------------------------------------|
| hour  | `currentHour`, `last1Hour`, `last2Hours`, `last3Hours`, `last24Hours`                                                            |
| day   | `today`, `last1Days`, `last2Days`, `last3Days`, `last7Days`                                                                      |
| week  | `thisWeek`, `lastWeek`, `lastMonday`, `lastTuesday`, `lastWednesday`, `lastThursday`, `lastFriday`, `lastSaturday`, `lastSunday` |
| month | `thisMonth`, `thisMonthBegin`, `lastMonth`, `lastMonthBegin`, `lastMonthEnd`                                                     |

**Example:**

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
                        "parameterPassing": false
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

The CONDITIONS task checks the status of upstream tasks and chooses the success or the failure branch. Its `task_params` is defined by `ConditionsParameters`.

|        Field        |  Type  |                                                                                                       Description                                                                                                        |
|---------------------|--------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| localParams         | Array  | Local parameters, see [Common Fields](#common-fields)                                                                                                                                                                    |
| resourceList        | Array  | Not used by this task type, an empty array                                                                                                                                                                               |
| dependence          | Object | Upstream conditions, required                                                                                                                                                                                            |
| -- relation         | String | Relation between the groups in `dependTaskList`: `AND` or `OR`                                                                                                                                                           |
| -- dependTaskList   | Array  | Condition groups, at least one                                                                                                                                                                                           |
| ---- relation       | String | Relation between the items in `dependItemList`: `AND` or `OR`                                                                                                                                                            |
| ---- dependItemList | Array  | Condition items of the group                                                                                                                                                                                             |
| ------ depTaskCode  | Number | Code of the upstream task to check                                                                                                                                                                                       |
| ------ status       | String | A `TaskExecutionStatus` value compared exactly with the upstream task state; the UI offers only `SUCCESS` and `FAILURE` (`FORCED_SUCCESS` does not match `SUCCESS`). An upstream task without an instance does not match |
| conditionResult     | Object | Branches to run, required                                                                                                                                                                                                |
| -- successNode      | Array  | Codes of the downstream tasks to run when the condition is met, at least one                                                                                                                                             |
| -- failedNode       | Array  | Codes of the downstream tasks to run when the condition is not met, at least one                                                                                                                                         |

**Example:**

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

The master also writes `conditionResult.conditionSuccess` into `task_params` of the task instance at runtime; do not set it in the definition.
The CONDITIONS task itself always succeeds, and the branch that is not chosen is skipped.

### SWITCH

The SWITCH task evaluates conditions in order and runs the branch of the first one that is true. Its `task_params` is defined by `SwitchParameters`.

|       Field       |  Type  |                                                                                                                             Description                                                                                                                             |
|-------------------|--------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| localParams       | Array  | Local parameters, see [Common Fields](#common-fields)                                                                                                                                                                                                               |
| resourceList      | Array  | Not used by this task type, an empty array                                                                                                                                                                                                                          |
| switchResult      | Object | Branch configuration, required                                                                                                                                                                                                                                      |
| -- dependTaskList | Array  | Conditions, evaluated in order; the first true one wins                                                                                                                                                                                                             |
| ---- condition    | String | JavaScript expression evaluated in a Nashorn sandbox; true only when it evaluates to `true`. `${name}` is replaced from global/startup parameters and the varPool, and string values are quoted automatically. An expression that fails to evaluate counts as false |
| ---- nextNode     | Number | Code of the downstream task to run when the condition is true                                                                                                                                                                                                       |
| -- nextNode       | Number | Code of the default downstream task, used when no condition is true. If it is not set and no condition is true, the task fails                                                                                                                                      |

**Example:**

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

The master also writes the chosen branch into `task_params` of the task instance as `nextBranch`; do not set it in the definition.
The chosen branch must be a task of the workflow DAG, otherwise the task fails. The master only checks that the task exists in the DAG, not that it is downstream of the SWITCH task; configure the branches as downstream tasks. The branches that are not chosen are skipped.
