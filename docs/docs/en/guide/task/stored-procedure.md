# Stored Procedure

- Execute the stored procedure according to the selected DataSource.

> Drag from the toolbar ![PNG](https://analysys.github.io/easyscheduler_docs_cn/images/toolbar_PROCEDURE.png) task node into the canvas, as shown in the figure below:

<p align="center">
   <img src="/img/procedure-en.png" width="80%" />
 </p>

- Node name: The node name in a workflow definition is unique.
- Run flag: Identifies whether this node schedules normally, if it does not need to execute, select the `prohibition execution`.
- Descriptive information: Describe the function of the node.
- Task priority: When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.
- Worker grouping: Assign tasks to the machines of the worker group to execute. If `Default` is selected, randomly select a worker machine for execution.
- Environment Name: Configure the environment name in which run the script.
- Times of failed retry attempts: The number of times the task failed to resubmit.
- Failed retry interval: The time interval (unit minute) for resubmitting the task after a failed task.
- Delayed execution time: The time (unit minute) that a task delays in execution.
- Timeout alarm: Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm email will send and the task execution will fail.
- DataSource: The DataSource type of the stored procedure supports MySQL,POSTGRESQL,HIVE,SPARK,CLICKHOUSE,ORACLE,SQLSERVER,DB2 and PRESTO, select the corresponding DataSource.
- SQL statement: SQL statement.
- Custom parameters: The custom parameter types of the stored procedure support `IN` and `OUT`, and the data types support: VARCHAR,INTEGER,LONG,FLOAT,DOUBLE,DATE,TIME,TIMESTAMP and BOOLEAN.

## Task Example

### Execute the MYSQL Procedure

#### Create Data Source

This sample is a common entry type in MySQL data sources, mainly for calling stored procedures in MySQL.

![resource_upload](/img/tasks/demo/proceduce_task01.png)

#### Configure PROCEDURE Nodes

Configure the required content according to the parameter descriptions above.

![demo-mr-simple](/img/tasks/demo/proceduce_task01.png)