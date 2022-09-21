# Task Structure

## Overall Tasks Storage Structure

All tasks in DolphinScheduler are saved in the `t_ds_process_definition` table.

The following shows the `t_ds_process_definition` table structure:

| No. |          field          |     type     |                                 description                                  |
|-----|-------------------------|--------------|------------------------------------------------------------------------------|
| 1   | id                      | int(11)      | primary key                                                                  |
| 2   | name                    | varchar(255) | process definition name                                                      |
| 3   | version                 | int(11)      | process definition version                                                   |
| 4   | release_state           | tinyint(4)   | release status of process definition: 0 not released, 1 released             |
| 5   | project_id              | int(11)      | project id                                                                   |
| 6   | user_id                 | int(11)      | user id of the process definition                                            |
| 7   | process_definition_json | longtext     | process definition JSON                                                      |
| 8   | description             | text         | process definition description                                               |
| 9   | global_params           | text         | global parameters                                                            |
| 10  | flag                    | tinyint(4)   | specify whether the process is available: 0 is not available, 1 is available |
| 11  | locations               | text         | node location information                                                    |
| 12  | connects                | text         | node connectivity info                                                       |
| 13  | receivers               | text         | receivers                                                                    |
| 14  | receivers_cc            | text         | CC receivers                                                                 |
| 15  | create_time             | datetime     | create time                                                                  |
| 16  | timeout                 | int(11)      | timeout                                                                      |
| 17  | tenant_id               | int(11)      | tenant id                                                                    |
| 18  | update_time             | datetime     | update time                                                                  |
| 19  | modify_by               | varchar(36)  | specify the user that made the modification                                  |
| 20  | resource_ids            | varchar(255) | resource ids                                                                 |

The `process_definition_json` field is the core field, which defines the task information in the DAG diagram, and it is stored in JSON format.

The following table describes the common data structure.
No. | field  | type  |  description
-------- | ---------| -------- | ---------
1|globalParams|Array|global parameters
2|tasks|Array|task collections in the process [for the structure of each type, please refer to the following sections]
3|tenantId|int|tenant ID
4|timeout|int|timeout

Data example:

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

## The Detailed Explanation of The Storage Structure of Each Task Type

### Shell Nodes

**The following shows the node data structure:**
No.|parameter name||type|description |notes
-------- | ---------| ---------| -------- | --------- | ---------
1|id | |String| task Id|
2|type | |String |task type |SHELL
3| name| |String|task name |
4| params| |Object|customized parameters |JSON format
5| |rawScript |String| Shell script |
6| | localParams| Array|customized local parameters||
7| | resourceList| Array|resource files||
8|description | |String|description | |
9|runFlag | |String |execution flag| |
10|conditionResult | |Object|condition branch | |
11| | successNode| Array|jump to node if success| |
12| | failedNode|Array|jump to node if failure|
13| dependence| |Object |task dependency |mutual exclusion with params
14|maxRetryTimes | |String|max retry times | |
15|retryInterval | |String |retry interval| |
16|timeout | |Object|timeout | |
17| taskInstancePriority| |String|task priority | |
18|workerGroup | |String |Worker group| |
19|preTasks | |Array|preposition tasks | |

**Node data example:**

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

### SQL Node

Perform data query and update operations on the specified datasource through SQL.

**The following shows the node data structure:**
No.|parameter name||type|description |note
-------- | ---------| ---------| -------- | --------- | ---------
1|id | |String|task id|
2|type ||String |task type |SQL
3| name| |String|task name|
4| params| |Object|customized parameters|JSON format
5| |type |String |database type
6| |datasource |Int |datasource id
7| |sql |String |query SQL statement
8| |udfs | String| udf functions|specify UDF function ids, separate by comma
9| |sqlType | String| SQL node type |0 for query and 1 for none-query SQL
10| |title |String | mail title
11| |receivers |String |receivers
12| |receiversCc |String |CC receivers
13| |showType | String|display type of mail|options: TABLE or ATTACHMENT
14| |connParams | String|connect parameters
15| |preStatements | Array|preposition SQL statements
16| | postStatements| Array|post-position SQL statements||
17| | localParams| Array|customized parameters||
18|description | |String|description | |
19|runFlag | |String |execution flag| |
20|conditionResult | |Object|condition branch  | |
21| | successNode| Array|jump to node if success| |
22| | failedNode|Array|jump to node if failure|
23| dependence| |Object |task dependency |mutual exclusion with params
24|maxRetryTimes | |String|max retry times | |
25|retryInterval | |String |retry interval| |
26|timeout | |Object|timeout | |
27| taskInstancePriority| |String|task priority | |
28|workerGroup | |String |Worker group| |
29|preTasks | |Array|preposition tasks | |

**Node data example:**

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

### Procedure [stored procedures] Node

**The following shows the node data structure:**
**Node data example:**

### Spark Node

**The following shows the node data structure:**

| No. |            parameter name            ||  type  |         description         |            notes             |
|-----|----------------------|----------------|--------|-----------------------------|------------------------------|
| 1   | id                   |                | String | task Id                     |
| 2   | type                                 || String | task type                   | SPARK                        |
| 3   | name                 |                | String | task name                   |
| 4   | params               |                | Object | customized parameters       | JSON format                  |
| 5   |                      | mainClass      | String | main class                  |
| 6   |                      | mainArgs       | String | execution arguments         |
| 7   |                      | others         | String | other arguments             |
| 8   |                      | mainJar        | Object | application jar package     |
| 9   |                      | deployMode     | String | deployment mode             | local,client,cluster         |
| 10  |                      | driverCores    | String | driver cores                |
| 11  |                      | driverMemory   | String | driver memory               |
| 12  |                      | numExecutors   | String | executor count              |
| 13  |                      | executorMemory | String | executor memory             |
| 14  |                      | executorCores  | String | executor cores              |
| 15  |                      | programType    | String | program type                | JAVA,SCALA,PYTHON            |
| 16  |                      | localParams    | Array  | customized local parameters |
| 17  |                      | resourceList   | Array  | resource files              |
| 18  | description          |                | String | description                 |                              |
| 19  | runFlag              |                | String | execution flag              |                              |
| 20  | conditionResult      |                | Object | condition branch            |                              |
| 21  |                      | successNode    | Array  | jump to node if success     |                              |
| 22  |                      | failedNode     | Array  | jump to node if failure     |
| 23  | dependence           |                | Object | task dependency             | mutual exclusion with params |
| 24  | maxRetryTimes        |                | String | max retry times             |                              |
| 25  | retryInterval        |                | String | retry interval              |                              |
| 26  | timeout              |                | Object | timeout                     |                              |
| 27  | taskInstancePriority |                | String | task priority               |                              |
| 28  | workerGroup          |                | String | Worker group                |                              |
| 29  | preTasks             |                | Array  | preposition tasks           |                              |

**Node data example:**

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

### MapReduce(MR) Node

**The following shows the node data structure:**

| No. |           parameter name           ||  type  |         description         |            notes             |
|-----|----------------------|--------------|--------|-----------------------------|------------------------------|
| 1   | id                   |              | String | task Id                     |
| 2   | type                               || String | task type                   | MR                           |
| 3   | name                 |              | String | task name                   |
| 4   | params               |              | Object | customized parameters       | JSON format                  |
| 5   |                      | mainClass    | String | main class                  |
| 6   |                      | mainArgs     | String | execution arguments         |
| 7   |                      | others       | String | other arguments             |
| 8   |                      | mainJar      | Object | application jar package     |
| 9   |                      | programType  | String | program type                | JAVA,PYTHON                  |
| 10  |                      | localParams  | Array  | customized local parameters |
| 11  |                      | resourceList | Array  | resource files              |
| 12  | description          |              | String | description                 |                              |
| 13  | runFlag              |              | String | execution flag              |                              |
| 14  | conditionResult      |              | Object | condition branch            |                              |
| 15  |                      | successNode  | Array  | jump to node if success     |                              |
| 16  |                      | failedNode   | Array  | jump to node if failure     |
| 17  | dependence           |              | Object | task dependency             | mutual exclusion with params |
| 18  | maxRetryTimes        |              | String | max retry times             |                              |
| 19  | retryInterval        |              | String | retry interval              |                              |
| 20  | timeout              |              | Object | timeout                     |                              |
| 21  | taskInstancePriority |              | String | task priority               |                              |
| 22  | workerGroup          |              | String | Worker group                |                              |
| 23  | preTasks             |              | Array  | preposition tasks           |                              |

**Node data example:**

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

### Python Node

**The following shows the node data structure:**
No.|parameter name||type|description |notes
-------- | ---------| ---------| -------- | --------- | ---------
1|id | |String|  task Id|
2|type ||String |task type|PYTHON
3| name| |String|task name|
4| params| |Object|customized parameters |JSON format
5| |rawScript |String| Python script|
6| | localParams| Array|customized local parameters||
7| | resourceList| Array|resource files||
8|description | |String|description | |
9|runFlag | |String |execution flag| |
10|conditionResult | |Object|condition branch| |
11| | successNode| Array|jump to node if success| |
12| | failedNode|Array|jump to node if failure |
13| dependence| |Object |task dependency |mutual exclusion with params
14|maxRetryTimes | |String|max retry times | |
15|retryInterval | |String |retry interval| |
16|timeout | |Object|timeout | |
17| taskInstancePriority| |String|task priority | |
18|workerGroup | |String |Worker group| |
19|preTasks | |Array|preposition tasks| |

**Node data example:**

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

### Flink Node

**The following shows the node data structure:**

| No. |             parameter name              ||  type  |       description       |            notes             |
|-----|----------------------|-------------------|--------|-------------------------|------------------------------|
| 1   | id                   |                   | String | task Id                 |
| 2   | type                                    || String | task type               | FLINK                        |
| 3   | name                 |                   | String | task name               |
| 4   | params               |                   | Object | customized parameters   | JSON format                  |
| 5   |                      | mainClass         | String | main class              |
| 6   |                      | mainArgs          | String | execution arguments     |
| 7   |                      | others            | String | other arguments         |
| 8   |                      | mainJar           | Object | application jar package |
| 9   |                      | deployMode        | String | deployment mode         | local,client,cluster         |
| 10  |                      | slot              | String | slot count              |
| 11  |                      | taskManager       | String | taskManager count       |
| 12  |                      | taskManagerMemory | String | taskManager memory size |
| 13  |                      | jobManagerMemory  | String | jobManager memory size  |
| 14  |                      | programType       | String | program type            | JAVA,SCALA,PYTHON            |
| 15  |                      | localParams       | Array  | local parameters        |
| 16  |                      | resourceList      | Array  | resource files          |
| 17  | description          |                   | String | description             |                              |
| 18  | runFlag              |                   | String | execution flag          |                              |
| 19  | conditionResult      |                   | Object | condition branch        |                              |
| 20  |                      | successNode       | Array  | jump node if success    |                              |
| 21  |                      | failedNode        | Array  | jump node if failure    |
| 22  | dependence           |                   | Object | task dependency         | mutual exclusion with params |
| 23  | maxRetryTimes        |                   | String | max retry times         |                              |
| 24  | retryInterval        |                   | String | retry interval          |                              |
| 25  | timeout              |                   | Object | timeout                 |                              |
| 26  | taskInstancePriority |                   | String | task priority           |                              |
| 27  | workerGroup          |                   | String | Worker group            |                              |
| 38  | preTasks             |                   | Array  | preposition tasks       |                              |

**Node data example:**

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

### HTTP Node

**The following shows the node data structure:**

| No. |              parameter name              ||  type  |          description           |            notes             |
|-----|----------------------|--------------------|--------|--------------------------------|------------------------------|
| 1   | id                   |                    | String | task Id                        |
| 2   | type                                     || String | task type                      | HTTP                         |
| 3   | name                 |                    | String | task name                      |
| 4   | params               |                    | Object | customized parameters          | JSON format                  |
| 5   |                      | url                | String | request url                    |
| 6   |                      | httpMethod         | String | http method                    | GET,POST,HEAD,PUT,DELETE     |
| 7   |                      | httpParams         | Array  | http parameters                |
| 8   |                      | httpCheckCondition | String | validation of HTTP code status | default code 200             |
| 9   |                      | condition          | String | validation conditions          |
| 10  |                      | localParams        | Array  | customized local parameters    |
| 11  | description          |                    | String | description                    |                              |
| 12  | runFlag              |                    | String | execution flag                 |                              |
| 13  | conditionResult      |                    | Object | condition branch               |                              |
| 14  |                      | successNode        | Array  | jump node if success           |                              |
| 15  |                      | failedNode         | Array  | jump node if failure           |
| 16  | dependence           |                    | Object | task dependency                | mutual exclusion with params |
| 17  | maxRetryTimes        |                    | String | max retry times                |                              |
| 18  | retryInterval        |                    | String | retry interval                 |                              |
| 19  | timeout              |                    | Object | timeout                        |                              |
| 20  | taskInstancePriority |                    | String | task priority                  |                              |
| 21  | workerGroup          |                    | String | Worker group                   |                              |
| 22  | preTasks             |                    | Array  | preposition tasks              |                              |

**Node data example:**

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

### DataX Node

**The following shows the node data structure:**
No.|parameter name||type|description |notes
-------- | ---------| ---------| -------- | --------- | ---------
1|id | |String| task Id|
2|type ||String |task type|DATAX
3| name| |String|task name|
4| params| |Object|customized parameters |JSON format
5| |customConfig |Int |specify whether use customized config| 0 none customized, 1 customized
6| |dsType |String | datasource type
7| |dataSource |Int | datasource ID
8| |dtType | String|target database type
9| |dataTarget | Int|target database ID
10| |sql |String | SQL statements
11| |targetTable |String |target table
12| |jobSpeedByte |Int |job speed limiting(bytes)
13| |jobSpeedRecord | Int|job speed limiting(records)
14| |preStatements | Array|preposition SQL
15| | postStatements| Array|post-position SQL
16| | json| String|customized configs|valid if customConfig=1
17| | localParams| Array|customized parameters|valid if customConfig=1
18|description | |String|description| |
19|runFlag | |String |execution flag| |
20|conditionResult | |Object|condition branch| |
21| | successNode| Array|jump node if success| |
22| | failedNode|Array|jump node if failure|
23| dependence| |Object |task dependency |mutual exclusion with params
24|maxRetryTimes | |String|max retry times| |
25|retryInterval | |String |retry interval| |
26|timeout | |Object|timeout | |
27| taskInstancePriority| |String|task priority| |
28|workerGroup | |String |Worker group| |
29|preTasks | |Array|preposition tasks| |

**Node data example:**

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

### Sqoop Node

**The following shows the node data structure:**
No.|parameter name||type|description |notes
-------- | ---------| ---------| -------- | --------- | ---------
1|id | |String|task ID|
2|type ||String |task type|SQOOP
3| name| |String|task name|
4| params| |Object|customized parameters |JSON format
5| | concurrency| Int|concurrency rate
6| | modelType|String |flow direction|import,export
7| |sourceType|String |datasource type|
8| |sourceParams |String|datasource parameters| JSON format
9| | targetType|String |target datasource type
10| |targetParams | String|target datasource parameters|JSON format
11| |localParams |Array |customized local parameters
12|description | |String|description| |
13|runFlag | |String |execution flag| |
14|conditionResult | |Object|condition branch| |
15| | successNode| Array|jump node if success| |
16| | failedNode|Array|jump node if failure|
17| dependence| |Object |task dependency |mutual exclusion with params
18|maxRetryTimes | |String|max retry times| |
19|retryInterval | |String |retry interval| |
20|timeout | |Object|timeout | |
21| taskInstancePriority| |String|task priority| |
22|workerGroup | |String |Worker group| |
23|preTasks | |Array|preposition tasks| |

**Node data example:**

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

### Condition Branch Node

**The following shows the node data structure:**
No.|parameter name||type|description |notes
-------- | ---------| ---------| -------- | --------- | ---------
1|id | |String| task ID|
2|type ||String |task type |SHELL
3| name| |String|task name |
4| params| |Object|customized parameters | null
5|description | |String|description| |
6|runFlag | |String |execution flag| |
7|conditionResult | |Object|condition branch | |
8| | successNode| Array|jump to node if success| |
9| | failedNode|Array|jump to node if failure|
10| dependence| |Object |task dependency |mutual exclusion with params
11|maxRetryTimes | |String|max retry times | |
12|retryInterval | |String |retry interval| |
13|timeout | |Object|timeout | |
14| taskInstancePriority| |String|task priority | |
15|workerGroup | |String |Worker group| |
16|preTasks | |Array|preposition tasks| |

**Node data example:**

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

### Subprocess Node

**The following shows the node data structure:**
No.|parameter name||type|description |notes
-------- | ---------| ---------| -------- | --------- | ---------
1|id | |String| task ID|
2|type ||String |task type|SHELL
3| name| |String|task name|
4| params| |Object|customized parameters |JSON format
5| |processDefinitionId |Int| process definition ID
6|description | |String|description | |
7|runFlag | |String |execution flag| |
8|conditionResult | |Object|condition branch | |
9| | successNode| Array|jump to node if success| |
10| | failedNode|Array|jump to node if failure|
11| dependence| |Object |task dependency |mutual exclusion with params
12|maxRetryTimes | |String|max retry times| |
13|retryInterval | |String |retry interval| |
14|timeout | |Object|timeout| |
15| taskInstancePriority| |String|task priority| |
16|workerGroup | |String |Worker group| |
17|preTasks | |Array|preposition tasks| |

**Node data example:**

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

### DEPENDENT Node

**The following shows the node data structure:**
No.|parameter name||type|description |notes
-------- | ---------| ---------| -------- | --------- | ---------
1|id | |String| task ID|
2|type ||String |task type|DEPENDENT
3| name| |String|task name|
4| params| |Object|customized parameters |JSON format
5| |rawScript |String|Shell script|
6| | localParams| Array|customized local parameters||
7| | resourceList| Array|resource files||
8|description | |String|description| |
9|runFlag | |String |execution flag| |
10|conditionResult | |Object|condition branch| |
11| | successNode| Array|jump to node if success| |
12| | failedNode|Array|jump to node if failure|
13| dependence| |Object |task dependency |mutual exclusion with params
14| | relation|String |relation|AND,OR
15| | dependTaskList|Array |dependent task list|
16|maxRetryTimes | |String|max retry times| |
17|retryInterval | |String |retry interval| |
18|timeout | |Object|timeout| |
19| taskInstancePriority| |String|task priority| |
20|workerGroup | |String |Worker group| |
21|preTasks | |Array|preposition tasks| |

**Node data example:**

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

