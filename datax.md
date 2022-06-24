# DataX

Overview
--------

DataX task type for executing DataX programs. For DataX nodes, the worker will execute `${DATAX_HOME}/bin/datax.py` to analyze the input json file.

Create Task
-----------

*   Click Project Management -> Project Name -> Workflow Definition, and click the "Create Workflow" button to enter the DAG editing page.
*   Drag the <img src="/img/tasks/icons/datax.png" width="15"/> from the toolbar to the drawing board.

Task Parameter
--------------

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Node name</td><td class="confluenceTd"><p>The node name in a workflow definition is unique.</p></td></tr><tr><td class="confluenceTd">Run flag</td><td class="confluenceTd"><p>Identifies whether this node schedules normally, if it does not need to execute, select the&nbsp;<code>prohibition execution</code>.</p></td></tr><tr><td class="confluenceTd">Task priority</td><td class="confluenceTd"><p>When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.</p></td></tr><tr><td colspan="1" class="confluenceTd">Description</td><td colspan="1" class="confluenceTd"><p>Describe the function of the node.</p></td></tr><tr><td colspan="1" class="confluenceTd">Worker group</td><td colspan="1" class="confluenceTd"><p>&nbsp;Assign tasks to the machines of the worker group to execute. If&nbsp;<code>Default</code>&nbsp;is selected, randomly select a worker machine for execution.</p></td></tr><tr><td colspan="1" class="confluenceTd">Environment Name</td><td colspan="1" class="confluenceTd"><p>Configure the environment name in which run the script.</p></td></tr><tr><td colspan="1" class="confluenceTd"><p>Number of failed retries</p></td><td colspan="1" class="confluenceTd"><p>The number of times the task failed to resubmit.</p></td></tr><tr><td colspan="1" class="confluenceTd">Failed retry interval</td><td colspan="1" class="confluenceTd"><p>The time interval (unit minute) for resubmitting the task after a failed task.</p></td></tr><tr><td colspan="1" class="confluenceTd">Cpu quota</td><td colspan="1" class="confluenceTd"><p>Assign the specified CPU time quota to the task executed. Takes a percentage value. Default -1 means unlimited. For example, the full CPU load of one core is 100%,and that of 16 cores is 1600%. This function is controlled by&nbsp;<a style="text-decoration: none;" href="https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/architecture/configuration.html" class="external-link" rel="nofollow">task.resource.limit.state</a></p></td></tr><tr><td colspan="1" class="confluenceTd">Max memory</td><td colspan="1" class="confluenceTd"><p>Assign the specified max memory to the task executed. Exceeding this limit will trigger oom to be killed and will not automatically retry. Takes an MB value. Default -1 means unlimited. This function is controlled by&nbsp;<a href="https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/architecture/configuration.html" style="text-decoration: none;" class="external-link" rel="nofollow">task.resource.limit.state</a>.</p></td></tr><tr><td colspan="1" class="confluenceTd">Delayed execution time</td><td colspan="1" class="confluenceTd">The time (unit minute) that a task delays in execution.</td></tr><tr><td colspan="1" class="confluenceTd">Timeout alarm</td><td colspan="1" class="confluenceTd">Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm email will send and the task execution will fail.</td></tr><tr><td colspan="1" class="confluenceTd">Custom template</td><td colspan="1" class="confluenceTd">Custom the content of the DataX node's json profile when the default data source provided does not meet the required requirements.</td></tr><tr><td colspan="1" class="confluenceTd">json</td><td colspan="1" class="confluenceTd"><p>json configuration file for DataX synchronization.</p></td></tr><tr><td colspan="1" class="confluenceTd">Custom parameters</td><td colspan="1" class="confluenceTd">SQL task type, and stored procedure is a custom parameter order to set values for the method. The custom parameter type and data type are the same as the stored procedure task type. The difference is that the SQL task type custom parameter will replace the ${variable} in the SQL statement.</td></tr><tr><td colspan="1" class="confluenceTd">Data source</td><td colspan="1" class="confluenceTd"><p>Select the data source from which the data will be extracted.</p></td></tr><tr><td colspan="1" class="confluenceTd">SQL statement</td><td colspan="1" class="confluenceTd">The sql statement used to extract data from the target database, the sql query column name is automatically parsed when the node is executed, and mapped to the target table</td></tr><tr><td colspan="1" class="confluenceTd">Target library</td><td colspan="1" class="confluenceTd">Select the target library for data synchronization.</td></tr><tr><td colspan="1" class="confluenceTd">Pre-sql</td><td colspan="1" class="confluenceTd">Pre-sql is executed before the sql statement (executed by the target library).</td></tr><tr><td colspan="1" class="confluenceTd">Post-sql</td><td colspan="1" class="confluenceTd">Post-sql is executed after the sql statement (executed by the target library).</td></tr><tr><td colspan="1" class="confluenceTd">Stream limit (number of bytes)</td><td colspan="1" class="confluenceTd">Limits the number of bytes in the query.</td></tr><tr><td colspan="1" class="confluenceTd">Limit flow (number of records)</td><td colspan="1" class="confluenceTd">Limit the number of records for a query.</td></tr><tr><td colspan="1" class="confluenceTd">Running memory</td><td colspan="1" class="confluenceTd"><p>The minimum and maximum memory required can be configured to suit the actual production environment.</p></td></tr><tr><td colspan="1" class="confluenceTd">Pre tasks</td><td colspan="1" class="confluenceTd">Selecting a predecessor task for the current task, will set the selected predecessor task as upstream of the current task.</td></tr></tbody></table>

  

Task Example
------------

This example demonstrates importing data from Hive into MySQL.

### Configuring the DataX environment in DolphinScheduler

If you are using the DataX task type in a production environment, it is necessary to configure the required environment first. The configuration file is as follows: `/dolphinscheduler/conf/env/dolphinscheduler_env.sh`.

![datax_task01](/img/tasks/demo/datax_task01.png)

After the environment has been configured, DolphinScheduler needs to be restarted.

### Configuring DataX Task Node

As the default data source does not contain data to be read from Hive, a custom json is required, refer to: [HDFS Writer](https://github.com/alibaba/DataX/blob/master/hdfswriter/doc/hdfswriter.md). Note: Partition directories exist on the HDFS path, when importing data in real world situations, partitioning is recommended to be passed as a parameter, using custom parameters.

After writing the required json file, you can configure the node content by following the steps in the diagram below.

![datax_task02](/img/tasks/demo/datax_task02.png)

### View run results

![datax_task03](/img/tasks/demo/datax_task03.png)

### Note

If the default data source provided does not meet your needs, you can configure the writer and reader of DataX according to the actual usage environment in the custom template option, available at [https://github.com/alibaba/DataX](https://github.com/alibaba/DataX).