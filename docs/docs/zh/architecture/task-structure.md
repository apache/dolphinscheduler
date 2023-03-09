# 任务总体存储结构

在dolphinscheduler中创建的所有任务都保存在t_ds_process_definition 表中.

该数据库表结构如下表所示:

| 序号 |           字段            |      类型      |           描述            |
|----|-------------------------|--------------|-------------------------|
| 1  | id                      | int(11)      | 主键                      |
| 2  | name                    | varchar(255) | 流程定义名称                  |
| 3  | version                 | int(11)      | 流程定义版本                  |
| 4  | release_state           | tinyint(4)   | 流程定义的发布状态：0 未上线 ,  1已上线 |
| 5  | project_id              | int(11)      | 项目id                    |
| 6  | user_id                 | int(11)      | 流程定义所属用户id              |
| 7  | process_definition_json | longtext     | 流程定义JSON                |
| 8  | description             | text         | 流程定义描述                  |
| 9  | global_params           | text         | 全局参数                    |
| 10 | flag                    | tinyint(4)   | 流程是否可用：0 不可用，1 可用       |
| 11 | locations               | text         | 节点坐标信息                  |
| 12 | connects                | text         | 节点连线信息                  |
| 13 | receivers               | text         | 收件人                     |
| 14 | receivers_cc            | text         | 抄送人                     |
| 15 | create_time             | datetime     | 创建时间                    |
| 16 | timeout                 | int(11)      | 超时时间                    |
| 17 | tenant_id               | int(11)      | 租户id                    |
| 18 | update_time             | datetime     | 更新时间                    |
| 19 | modify_by               | varchar(36)  | 修改用户                    |
| 20 | resource_ids            | varchar(255) | 资源ids                   |

其中process_definition_json 字段为核心字段, 定义了 DAG 图中的任务信息.该数据以JSON 的方式进行存储.

公共的数据结构如下表.
序号 | 字段  | 类型  |  描述
-------- | ---------| -------- | ---------
1|globalParams|Array|全局参数
2|tasks|Array|流程中的任务集合  [ 各个类型的结构请参考如下章节]
3|tenantId|int|租户id
4|timeout|int|超时时间

数据示例:

```bash
{
    "globalParams":[
        {
            "prop":"golbal_bizdate",
            "direct":"IN",
            "type":"VARCHAR",
            "value":"${system.biz.date}"
        }
    ],
    "tasks":Array[1],
    "tenantId":0,
    "timeout":0
}
```

# 各任务类型存储结构详解

## Shell节点

**节点数据结构如下:**
序号|参数名||类型|描述 |描述
-------- | ---------| ---------| -------- | --------- | ---------
1|id | |String| 任务编码|
2|type ||String |类型 |SHELL
3| name| |String|名称 |
4| params| |Object| 自定义参数 |Json 格式
5| |rawScript |String| Shell脚本 |
6| | localParams| Array|自定义参数||
7| | resourceList| Array|资源文件||
8|description | |String|描述 | |
9|runFlag | |String |运行标识| |
10|conditionResult | |Object|条件分支 | |
11| | successNode| Array|成功跳转节点| |
12| | failedNode|Array|失败跳转节点 |
13| dependence| |Object |任务依赖 |与params互斥
14|maxRetryTimes | |String|最大重试次数 | |
15|retryInterval | |String |重试间隔| |
16|timeout | |Object|超时控制 | |
17| taskInstancePriority| |String|任务优先级 | |
18|workerGroup | |String |Worker 分组| |
19|preTasks | |Array|前置任务 | |

**节点数据样例:**

```bash
{
    "type":"SHELL",
    "id":"tasks-80760",
    "name":"Shell Task",
    "params":{
        "resourceList":[
            {
                "id":3,
                "name":"run.sh",
                "res":"run.sh"
            }
        ],
        "localParams":[

        ],
        "rawScript":"echo "This is a shell script""
    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            ""
        ],
        "failedNode":[
            ""
        ]
    },
    "dependence":{

    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[

    ]
}

```

## SQL节点

通过 SQL对指定的数据源进行数据查询、更新操作.

**节点数据结构如下:**
序号|参数名||类型|描述 |描述
-------- | ---------| ---------| -------- | --------- | ---------
1|id | |String| 任务编码|
2|type ||String |类型 |SQL
3| name| |String|名称 |
4| params| |Object| 自定义参数 |Json 格式
5| |type |String | 数据库类型
6| |datasource |Int | 数据源id
7| |sql |String | 查询SQL语句
8| |udfs | String| udf函数|UDF函数id,以逗号分隔.
9| |sqlType | String| SQL节点类型 |0 查询  , 1 非查询
10| |title |String | 邮件标题
11| |receivers |String | 收件人
12| |receiversCc |String | 抄送人
13| |showType | String| 邮件显示类型|TABLE 表格  ,  ATTACHMENT附件
14| |connParams | String| 连接参数
15| |preStatements | Array| 前置SQL
16| | postStatements| Array|后置SQL||
17| | localParams| Array|自定义参数||
18|description | |String|描述 | |
19|runFlag | |String |运行标识| |
20|conditionResult | |Object|条件分支 | |
21| | successNode| Array|成功跳转节点| |
22| | failedNode|Array|失败跳转节点 |
23| dependence| |Object |任务依赖 |与params互斥
24|maxRetryTimes | |String|最大重试次数 | |
25|retryInterval | |String |重试间隔| |
26|timeout | |Object|超时控制 | |
27| taskInstancePriority| |String|任务优先级 | |
28|workerGroup | |String |Worker 分组| |
29|preTasks | |Array|前置任务 | |

**节点数据样例:**

```bash
{
    "type":"SQL",
    "id":"tasks-95648",
    "name":"SqlTask-Query",
    "params":{
        "type":"MYSQL",
        "datasource":1,
        "sql":"select id , namge , age from emp where id =  ${id}",
        "udfs":"",
        "sqlType":"0",
        "title":"xxxx@xxx.com",
        "receivers":"xxxx@xxx.com",
        "receiversCc":"",
        "showType":"TABLE",
        "localParams":[
            {
                "prop":"id",
                "direct":"IN",
                "type":"INTEGER",
                "value":"1"
            }
        ],
        "connParams":"",
        "preStatements":[
            "insert into emp ( id,name ) value (1,'Li' )"
        ],
        "postStatements":[

        ]
    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            ""
        ],
        "failedNode":[
            ""
        ]
    },
    "dependence":{

    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[

    ]
}
```

## PROCEDURE[存储过程]节点

**节点数据结构如下:**
**节点数据样例:**

## SPARK节点

**节点数据结构如下:**

| 序号 |                 参数名                  ||   类型   |     描述     |          描述          |
|----|----------------------|----------------|--------|------------|----------------------|
| 1  | id                   |                | String | 任务编码       |
| 2  | type                                 || String | 类型         | SPARK                |
| 3  | name                 |                | String | 名称         |
| 4  | params               |                | Object | 自定义参数      | Json 格式              |
| 5  |                      | mainClass      | String | 运行主类       |
| 6  |                      | mainArgs       | String | 运行参数       |
| 7  |                      | others         | String | 其他参数       |
| 8  |                      | mainJar        | Object | 程序 jar 包   |
| 9  |                      | deployMode     | String | 部署模式       | local,client,cluster |
| 10 |                      | driverCores    | String | driver核数   |
| 11 |                      | driverMemory   | String | driver 内存数 |
| 12 |                      | numExecutors   | String | executor数量 |
| 13 |                      | executorMemory | String | executor内存 |
| 14 |                      | executorCores  | String | executor核数 |
| 15 |                      | programType    | String | 程序类型       | JAVA,SCALA,PYTHON    |
| 16 |                      | localParams    | Array  | 自定义参数      |
| 17 |                      | resourceList   | Array  | 资源文件       |
| 18 | description          |                | String | 描述         |                      |
| 19 | runFlag              |                | String | 运行标识       |                      |
| 20 | conditionResult      |                | Object | 条件分支       |                      |
| 21 |                      | successNode    | Array  | 成功跳转节点     |                      |
| 22 |                      | failedNode     | Array  | 失败跳转节点     |
| 23 | dependence           |                | Object | 任务依赖       | 与params互斥            |
| 24 | maxRetryTimes        |                | String | 最大重试次数     |                      |
| 25 | retryInterval        |                | String | 重试间隔       |                      |
| 26 | timeout              |                | Object | 超时控制       |                      |
| 27 | taskInstancePriority |                | String | 任务优先级      |                      |
| 28 | workerGroup          |                | String | Worker 分组  |                      |
| 29 | preTasks             |                | Array  | 前置任务       |                      |

**节点数据样例:**

```bash
{
    "type":"SPARK",
    "id":"tasks-87430",
    "name":"SparkTask",
    "params":{
        "mainClass":"org.apache.spark.examples.SparkPi",
        "mainJar":{
            "id":4
        },
        "deployMode":"cluster",
        "resourceList":[
            {
                "id":3,
                "name":"run.sh",
                "res":"run.sh"
            }
        ],
        "localParams":[

        ],
        "driverCores":1,
        "driverMemory":"512M",
        "numExecutors":2,
        "executorMemory":"2G",
        "executorCores":2,
        "mainArgs":"10",
        "others":"",
        "programType":"SCALA"
    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            ""
        ],
        "failedNode":[
            ""
        ]
    },
    "dependence":{

    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[

    ]
}
```

## MapReduce(MR)节点

**节点数据结构如下:**

| 序号 |                参数名                 ||   类型   |    描述     |     描述      |
|----|----------------------|--------------|--------|-----------|-------------|
| 1  | id                   |              | String | 任务编码      |
| 2  | type                               || String | 类型        | MR          |
| 3  | name                 |              | String | 名称        |
| 4  | params               |              | Object | 自定义参数     | Json 格式     |
| 5  |                      | mainClass    | String | 运行主类      |
| 6  |                      | mainArgs     | String | 运行参数      |
| 7  |                      | others       | String | 其他参数      |
| 8  |                      | mainJar      | Object | 程序 jar 包  |
| 9  |                      | programType  | String | 程序类型      | JAVA,PYTHON |
| 10 |                      | localParams  | Array  | 自定义参数     |
| 11 |                      | resourceList | Array  | 资源文件      |
| 12 | description          |              | String | 描述        |             |
| 13 | runFlag              |              | String | 运行标识      |             |
| 14 | conditionResult      |              | Object | 条件分支      |             |
| 15 |                      | successNode  | Array  | 成功跳转节点    |             |
| 16 |                      | failedNode   | Array  | 失败跳转节点    |
| 17 | dependence           |              | Object | 任务依赖      | 与params互斥   |
| 18 | maxRetryTimes        |              | String | 最大重试次数    |             |
| 19 | retryInterval        |              | String | 重试间隔      |             |
| 20 | timeout              |              | Object | 超时控制      |             |
| 21 | taskInstancePriority |              | String | 任务优先级     |             |
| 22 | workerGroup          |              | String | Worker 分组 |             |
| 23 | preTasks             |              | Array  | 前置任务      |             |

**节点数据样例:**

```bash
{
    "type":"MR",
    "id":"tasks-28997",
    "name":"MRTask",
    "params":{
        "mainClass":"wordcount",
        "mainJar":{
            "id":5
        },
        "resourceList":[
            {
                "id":3,
                "name":"run.sh",
                "res":"run.sh"
            }
        ],
        "localParams":[

        ],
        "mainArgs":"/tmp/wordcount/input /tmp/wordcount/output/",
        "others":"",
        "programType":"JAVA"
    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            ""
        ],
        "failedNode":[
            ""
        ]
    },
    "dependence":{

    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[

    ]
}
```

## Python节点

**节点数据结构如下:**
序号|参数名||类型|描述 |描述
-------- | ---------| ---------| -------- | --------- | ---------
1|id | |String| 任务编码|
2|type ||String |类型 |PYTHON
3| name| |String|名称 |
4| params| |Object| 自定义参数 |Json 格式
5| |rawScript |String| Python脚本 |
6| | localParams| Array|自定义参数||
7| | resourceList| Array|资源文件||
8|description | |String|描述 | |
9|runFlag | |String |运行标识| |
10|conditionResult | |Object|条件分支 | |
11| | successNode| Array|成功跳转节点| |
12| | failedNode|Array|失败跳转节点 |
13| dependence| |Object |任务依赖 |与params互斥
14|maxRetryTimes | |String|最大重试次数 | |
15|retryInterval | |String |重试间隔| |
16|timeout | |Object|超时控制 | |
17| taskInstancePriority| |String|任务优先级 | |
18|workerGroup | |String |Worker 分组| |
19|preTasks | |Array|前置任务 | |

**节点数据样例:**

```bash
{
    "type":"PYTHON",
    "id":"tasks-5463",
    "name":"Python Task",
    "params":{
        "resourceList":[
            {
                "id":3,
                "name":"run.sh",
                "res":"run.sh"
            }
        ],
        "localParams":[

        ],
        "rawScript":"print("This is a python script")"
    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            ""
        ],
        "failedNode":[
            ""
        ]
    },
    "dependence":{

    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[

    ]
}
```

## Flink节点

**节点数据结构如下:**

| 序号 |                   参数名                   ||   类型   |       描述       |          描述          |
|----|----------------------|-------------------|--------|----------------|----------------------|
| 1  | id                   |                   | String | 任务编码           |
| 2  | type                                    || String | 类型             | FLINK                |
| 3  | name                 |                   | String | 名称             |
| 4  | params               |                   | Object | 自定义参数          | Json 格式              |
| 5  |                      | mainClass         | String | 运行主类           |
| 6  |                      | mainArgs          | String | 运行参数           |
| 7  |                      | others            | String | 其他参数           |
| 8  |                      | mainJar           | Object | 程序 jar 包       |
| 9  |                      | deployMode        | String | 部署模式           | local,client,cluster |
| 10 |                      | slot              | String | slot数量         |
| 11 |                      | taskManager       | String | taskManager数量  |
| 12 |                      | taskManagerMemory | String | taskManager内存数 |
| 13 |                      | jobManagerMemory  | String | jobManager内存数  |
| 14 |                      | programType       | String | 程序类型           | JAVA,SCALA,PYTHON    |
| 15 |                      | localParams       | Array  | 自定义参数          |
| 16 |                      | resourceList      | Array  | 资源文件           |
| 17 | description          |                   | String | 描述             |                      |
| 18 | runFlag              |                   | String | 运行标识           |                      |
| 19 | conditionResult      |                   | Object | 条件分支           |                      |
| 20 |                      | successNode       | Array  | 成功跳转节点         |                      |
| 21 |                      | failedNode        | Array  | 失败跳转节点         |
| 22 | dependence           |                   | Object | 任务依赖           | 与params互斥            |
| 23 | maxRetryTimes        |                   | String | 最大重试次数         |                      |
| 24 | retryInterval        |                   | String | 重试间隔           |                      |
| 25 | timeout              |                   | Object | 超时控制           |                      |
| 26 | taskInstancePriority |                   | String | 任务优先级          |                      |
| 27 | workerGroup          |                   | String | Worker 分组      |                      |
| 38 | preTasks             |                   | Array  | 前置任务           |                      |

**节点数据样例:**

```bash
{
    "type":"FLINK",
    "id":"tasks-17135",
    "name":"FlinkTask",
    "params":{
        "mainClass":"com.flink.demo",
        "mainJar":{
            "id":6
        },
        "deployMode":"cluster",
        "resourceList":[
            {
                "id":3,
                "name":"run.sh",
                "res":"run.sh"
            }
        ],
        "localParams":[

        ],
        "slot":1,
        "taskManager":"2",
        "jobManagerMemory":"1G",
        "taskManagerMemory":"2G",
        "executorCores":2,
        "mainArgs":"100",
        "others":"",
        "programType":"SCALA"
    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            ""
        ],
        "failedNode":[
            ""
        ]
    },
    "dependence":{

    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[

    ]
}
```

## HTTP节点

**节点数据结构如下:**

| 序号 |                   参数名                    ||   类型   |    描述     |            描述            |
|----|----------------------|--------------------|--------|-----------|--------------------------|
| 1  | id                   |                    | String | 任务编码      |
| 2  | type                                     || String | 类型        | HTTP                     |
| 3  | name                 |                    | String | 名称        |
| 4  | params               |                    | Object | 自定义参数     | Json 格式                  |
| 5  |                      | url                | String | 请求地址      |
| 6  |                      | httpMethod         | String | 请求方式      | GET,POST,HEAD,PUT,DELETE |
| 7  |                      | httpParams         | Array  | 请求参数      |
| 8  |                      | httpCheckCondition | String | 校验条件      | 默认响应码200                 |
| 9  |                      | condition          | String | 校验内容      |
| 10 |                      | localParams        | Array  | 自定义参数     |
| 11 | description          |                    | String | 描述        |                          |
| 12 | runFlag              |                    | String | 运行标识      |                          |
| 13 | conditionResult      |                    | Object | 条件分支      |                          |
| 14 |                      | successNode        | Array  | 成功跳转节点    |                          |
| 15 |                      | failedNode         | Array  | 失败跳转节点    |
| 16 | dependence           |                    | Object | 任务依赖      | 与params互斥                |
| 17 | maxRetryTimes        |                    | String | 最大重试次数    |                          |
| 18 | retryInterval        |                    | String | 重试间隔      |                          |
| 19 | timeout              |                    | Object | 超时控制      |                          |
| 20 | taskInstancePriority |                    | String | 任务优先级     |                          |
| 21 | workerGroup          |                    | String | Worker 分组 |                          |
| 22 | preTasks             |                    | Array  | 前置任务      |                          |

**节点数据样例:**

```bash
{
    "type":"HTTP",
    "id":"tasks-60499",
    "name":"HttpTask",
    "params":{
        "localParams":[

        ],
        "httpParams":[
            {
                "prop":"id",
                "httpParametersType":"PARAMETER",
                "value":"1"
            },
            {
                "prop":"name",
                "httpParametersType":"PARAMETER",
                "value":"Bo"
            }
        ],
        "url":"https://www.xxxxx.com:9012",
        "httpMethod":"POST",
        "httpCheckCondition":"STATUS_CODE_DEFAULT",
        "condition":""
    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            ""
        ],
        "failedNode":[
            ""
        ]
    },
    "dependence":{

    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[

    ]
}
```

## DataX节点

**节点数据结构如下:**
序号|参数名||类型|描述 |描述
-------- | ---------| ---------| -------- | --------- | ---------
1|id | |String| 任务编码|
2|type ||String |类型 |DATAX
3| name| |String|名称 |
4| params| |Object| 自定义参数 |Json 格式
5| |customConfig |Int | 自定义类型| 0定制 , 1自定义
6| |dsType |String | 源数据库类型
7| |dataSource |Int | 源数据库ID
8| |dtType | String| 目标数据库类型
9| |dataTarget | Int| 目标数据库ID
10| |sql |String | SQL语句
11| |targetTable |String | 目标表
12| |jobSpeedByte |Int | 限流(字节数)
13| |jobSpeedRecord | Int| 限流(记录数)
14| |preStatements | Array| 前置SQL
15| | postStatements| Array|后置SQL
16| | json| String|自定义配置|customConfig=1时生效
17| | localParams| Array|自定义参数|customConfig=1时生效
18|description | |String|描述 | |
19|runFlag | |String |运行标识| |
20|conditionResult | |Object|条件分支 | |
21| | successNode| Array|成功跳转节点| |
22| | failedNode|Array|失败跳转节点 |
23| dependence| |Object |任务依赖 |与params互斥
24|maxRetryTimes | |String|最大重试次数 | |
25|retryInterval | |String |重试间隔| |
26|timeout | |Object|超时控制 | |
27| taskInstancePriority| |String|任务优先级 | |
28|workerGroup | |String |Worker 分组| |
29|preTasks | |Array|前置任务 | |

**节点数据样例:**

```bash
{
    "type":"DATAX",
    "id":"tasks-91196",
    "name":"DataxTask-DB",
    "params":{
        "customConfig":0,
        "dsType":"MYSQL",
        "dataSource":1,
        "dtType":"MYSQL",
        "dataTarget":1,
        "sql":"select id, name ,age from user ",
        "targetTable":"emp",
        "jobSpeedByte":524288,
        "jobSpeedRecord":500,
        "preStatements":[
            "truncate table emp "
        ],
        "postStatements":[
            "truncate table user"
        ]
    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            ""
        ],
        "failedNode":[
            ""
        ]
    },
    "dependence":{

    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[

    ]
}
```

## Sqoop节点

**节点数据结构如下:**
序号|参数名||类型|描述 |描述
-------- | ---------| ---------| -------- | --------- | ---------
1|id | |String| 任务编码|
2|type ||String |类型 |SQOOP
3| name| |String|名称 |
4| params| |Object| 自定义参数 |JSON 格式
5| | concurrency| Int|并发度
6| | modelType|String |流向|import,export
7| |sourceType|String |数据源类型 |
8| |sourceParams |String| 数据源参数| JSON格式
9| | targetType|String |目标数据类型
10| |targetParams | String|目标数据参数|JSON格式
11| |localParams |Array |自定义参数
12|description | |String|描述 | |
13|runFlag | |String |运行标识| |
14|conditionResult | |Object|条件分支 | |
15| | successNode| Array|成功跳转节点| |
16| | failedNode|Array|失败跳转节点 |
17| dependence| |Object |任务依赖 |与params互斥
18|maxRetryTimes | |String|最大重试次数 | |
19|retryInterval | |String |重试间隔| |
20|timeout | |Object|超时控制 | |
21| taskInstancePriority| |String|任务优先级 | |
22|workerGroup | |String |Worker 分组| |
23|preTasks | |Array|前置任务 | |

**节点数据样例:**

```bash
{
            "type":"SQOOP",
            "id":"tasks-82041",
            "name":"Sqoop Task",
            "params":{
                "concurrency":1,
                "modelType":"import",
                "sourceType":"MYSQL",
                "targetType":"HDFS",
                "sourceParams":"{"srcType":"MYSQL","srcDatasource":1,"srcTable":"","srcQueryType":"1","srcQuerySql":"selec id , name from user","srcColumnType":"0","srcColumns":"","srcConditionList":[],"mapColumnHive":[{"prop":"hivetype-key","direct":"IN","type":"VARCHAR","value":"hivetype-value"}],"mapColumnJava":[{"prop":"javatype-key","direct":"IN","type":"VARCHAR","value":"javatype-value"}]}",
                "targetParams":"{"targetPath":"/user/hive/warehouse/ods.db/user","deleteTargetDir":false,"fileType":"--as-avrodatafile","compressionCodec":"snappy","fieldsTerminated":",","linesTerminated":"@"}",
                "localParams":[

                ]
            },
            "description":"",
            "runFlag":"NORMAL",
            "conditionResult":{
                "successNode":[
                    ""
                ],
                "failedNode":[
                    ""
                ]
            },
            "dependence":{

            },
            "maxRetryTimes":"0",
            "retryInterval":"1",
            "timeout":{
                "strategy":"",
                "interval":null,
                "enable":false
            },
            "taskInstancePriority":"MEDIUM",
            "workerGroup":"default",
            "preTasks":[

            ]
        }
```

## 条件分支节点

**节点数据结构如下:**
序号|参数名||类型|描述 |描述
-------- | ---------| ---------| -------- | --------- | ---------
1|id | |String| 任务编码|
2|type ||String |类型 |SHELL
3| name| |String|名称 |
4| params| |Object| 自定义参数 | null
5|description | |String|描述 | |
6|runFlag | |String |运行标识| |
7|conditionResult | |Object|条件分支 | |
8| | successNode| Array|成功跳转节点| |
9| | failedNode|Array|失败跳转节点 |
10| dependence| |Object |任务依赖 |与params互斥
11|maxRetryTimes | |String|最大重试次数 | |
12|retryInterval | |String |重试间隔| |
13|timeout | |Object|超时控制 | |
14| taskInstancePriority| |String|任务优先级 | |
15|workerGroup | |String |Worker 分组| |
16|preTasks | |Array|前置任务 | |

**节点数据样例:**

```bash
{
    "type":"CONDITIONS",
    "id":"tasks-96189",
    "name":"条件",
    "params":{

    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            "test04"
        ],
        "failedNode":[
            "test05"
        ]
    },
    "dependence":{
        "relation":"AND",
        "dependTaskList":[

        ]
    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[
        "test01",
        "test02"
    ]
}
```

## 子流程节点

**节点数据结构如下:**
序号|参数名||类型|描述 |描述
-------- | ---------| ---------| -------- | --------- | ---------
1|id | |String| 任务编码|
2|type ||String |类型 |SHELL
3| name| |String|名称 |
4| params| |Object| 自定义参数 |Json 格式
5| |processDefinitionId |Int| 流程定义id
6|description | |String|描述 | |
7|runFlag | |String |运行标识| |
8|conditionResult | |Object|条件分支 | |
9| | successNode| Array|成功跳转节点| |
10| | failedNode|Array|失败跳转节点 |
11| dependence| |Object |任务依赖 |与params互斥
12|maxRetryTimes | |String|最大重试次数 | |
13|retryInterval | |String |重试间隔| |
14|timeout | |Object|超时控制 | |
15| taskInstancePriority| |String|任务优先级 | |
16|workerGroup | |String |Worker 分组| |
17|preTasks | |Array|前置任务 | |

**节点数据样例:**

```bash
{
            "type":"SUB_PROCESS",
            "id":"tasks-14806",
            "name":"SubProcessTask",
            "params":{
                "processDefinitionId":2
            },
            "description":"",
            "runFlag":"NORMAL",
            "conditionResult":{
                "successNode":[
                    ""
                ],
                "failedNode":[
                    ""
                ]
            },
            "dependence":{

            },
            "timeout":{
                "strategy":"",
                "interval":null,
                "enable":false
            },
            "taskInstancePriority":"MEDIUM",
            "workerGroup":"default",
            "preTasks":[

            ]
        }
```

## 依赖(DEPENDENT)节点

**节点数据结构如下:**
序号|参数名||类型|描述 |描述
-------- | ---------| ---------| -------- | --------- | ---------
1|id | |String| 任务编码|
2|type ||String |类型 |DEPENDENT
3| name| |String|名称 |
4| params| |Object| 自定义参数 |Json 格式
5| |rawScript |String| Shell脚本 |
6| | localParams| Array|自定义参数||
7| | resourceList| Array|资源文件||
8|description | |String|描述 | |
9|runFlag | |String |运行标识| |
10|conditionResult | |Object|条件分支 | |
11| | successNode| Array|成功跳转节点| |
12| | failedNode|Array|失败跳转节点 |
13| dependence| |Object |任务依赖 |与params互斥
14| | relation|String |关系 |AND,OR
15| | dependTaskList|Array |依赖任务清单 |
16|maxRetryTimes | |String|最大重试次数 | |
17|retryInterval | |String |重试间隔| |
18|timeout | |Object|超时控制 | |
19| taskInstancePriority| |String|任务优先级 | |
20|workerGroup | |String |Worker 分组| |
21|preTasks | |Array|前置任务 | |

**节点数据样例:**

```bash
{
            "type":"DEPENDENT",
            "id":"tasks-57057",
            "name":"DenpendentTask",
            "params":{

            },
            "description":"",
            "runFlag":"NORMAL",
            "conditionResult":{
                "successNode":[
                    ""
                ],
                "failedNode":[
                    ""
                ]
            },
            "dependence":{
                "relation":"AND",
                "dependTaskList":[
                    {
                        "relation":"AND",
                        "dependItemList":[
                            {
                                "projectId":1,
                                "definitionId":7,
                                "definitionList":[
                                    {
                                        "value":8,
                                        "label":"MRTask"
                                    },
                                    {
                                        "value":7,
                                        "label":"FlinkTask"
                                    },
                                    {
                                        "value":6,
                                        "label":"SparkTask"
                                    },
                                    {
                                        "value":5,
                                        "label":"SqlTask-Update"
                                    },
                                    {
                                        "value":4,
                                        "label":"SqlTask-Query"
                                    },
                                    {
                                        "value":3,
                                        "label":"SubProcessTask"
                                    },
                                    {
                                        "value":2,
                                        "label":"Python Task"
                                    },
                                    {
                                        "value":1,
                                        "label":"Shell Task"
                                    }
                                ],
                                "depTasks":"ALL",
                                "cycle":"day",
                                "dateValue":"today"
                            }
                        ]
                    },
                    {
                        "relation":"AND",
                        "dependItemList":[
                            {
                                "projectId":1,
                                "definitionId":5,
                                "definitionList":[
                                    {
                                        "value":8,
                                        "label":"MRTask"
                                    },
                                    {
                                        "value":7,
                                        "label":"FlinkTask"
                                    },
                                    {
                                        "value":6,
                                        "label":"SparkTask"
                                    },
                                    {
                                        "value":5,
                                        "label":"SqlTask-Update"
                                    },
                                    {
                                        "value":4,
                                        "label":"SqlTask-Query"
                                    },
                                    {
                                        "value":3,
                                        "label":"SubProcessTask"
                                    },
                                    {
                                        "value":2,
                                        "label":"Python Task"
                                    },
                                    {
                                        "value":1,
                                        "label":"Shell Task"
                                    }
                                ],
                                "depTasks":"SqlTask-Update",
                                "cycle":"day",
                                "dateValue":"today"
                            }
                        ]
                    }
                ]
            },
            "maxRetryTimes":"0",
            "retryInterval":"1",
            "timeout":{
                "strategy":"",
                "interval":null,
                "enable":false
            },
            "taskInstancePriority":"MEDIUM",
            "workerGroup":"default",
            "preTasks":[

            ]
        }
```

