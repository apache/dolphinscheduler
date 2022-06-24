Flink Node
==========

Overview
--------

Flink task type, used to execute Flink programs. For Flink nodes:

1.  When the program type is Java, Scala or Python, the worker submits the task `flink run` using the Flink command. See [flink cli](https://nightlies.apache.org/flink/flink-docs-release-1.14/docs/deployment/cli/) for more details.
    
2.  When the program type is SQL, the worker submit tasks using `sql-client.sh`. See [flink sql client](https://nightlies.apache.org/flink/flink-docs-master/docs/dev/table/sqlclient/) for more details.
    

Create Task
-----------

*   Click `Project Management -> Project Name-> Workflow Definition`, and click the "`Create Workflow`" button to enter the DAG editing page.
*   Drag from the toolbar <img src="/img/tasks/icons/flink.png" width="15"/> task node to canvas.

Task Parameter
--------------

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Node name</td><td class="confluenceTd"><p>The node name in a workflow definition is unique.</p></td></tr><tr><td class="confluenceTd">Run flag</td><td class="confluenceTd"><p>Identifies whether this node schedules normally, if it does not need to execute, select the&nbsp;<code>prohibition execution</code>.</p></td></tr><tr><td class="confluenceTd">Task priority</td><td class="confluenceTd"><p>When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.</p></td></tr><tr><td colspan="1" class="confluenceTd">Description</td><td colspan="1" class="confluenceTd"><p>Describe the function of the node.</p></td></tr><tr><td colspan="1" class="confluenceTd">Worker group</td><td colspan="1" class="confluenceTd"><p>&nbsp;Assign tasks to the machines of the worker group to execute. If&nbsp;<code>Default</code>&nbsp;is selected, randomly select a worker machine for execution.</p></td></tr><tr><td colspan="1" class="confluenceTd">Environment Name</td><td colspan="1" class="confluenceTd"><p>Configure the environment name in which run the script.</p></td></tr><tr><td colspan="1" class="confluenceTd"><p>Number of failed retries</p></td><td colspan="1" class="confluenceTd"><p>The number of times the task failed to resubmit.</p></td></tr><tr><td colspan="1" class="confluenceTd">Failed retry interval</td><td colspan="1" class="confluenceTd"><p>The time interval (unit minute) for resubmitting the task after a failed task.</p></td></tr><tr><td colspan="1" class="confluenceTd">Delayed execution time</td><td colspan="1" class="confluenceTd">The time (unit minute) that a task delays in execution.</td></tr><tr><td colspan="1" class="confluenceTd">Timeout alarm</td><td colspan="1" class="confluenceTd">Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm email will send and the task execution will fail.</td></tr><tr><td colspan="1" class="confluenceTd">Program type</td><td colspan="1" class="confluenceTd">Supports Java, Scala, Python and SQL.</td></tr><tr><td colspan="1" class="confluenceTd">Main Class</td><td colspan="1" class="confluenceTd"><p>The&nbsp;full path&nbsp;of Main Class, the entry point of the Flink program.</p></td></tr><tr><td colspan="1" class="confluenceTd">Main Package</td><td colspan="1" class="confluenceTd">The jar package of the Flink program (upload by Resource Center).</td></tr><tr><td colspan="1" class="confluenceTd">Deployment mode</td><td colspan="1" class="confluenceTd"><p>(1) spark submit supports three modes: yarn-cluster, yarn-client and local.</p><p>(2) spark sql supports yarn-client and local modes.</p></td></tr><tr><td colspan="1" class="confluenceTd">App name</td><td colspan="1" class="confluenceTd">Flink task name.</td></tr><tr><td colspan="1" class="confluenceTd">Initialization script</td><td colspan="1" class="confluenceTd">Script file to initialize session context.</td></tr><tr><td colspan="1" class="confluenceTd">Script</td><td colspan="1" class="confluenceTd">The sql script file developed by the user that should be executed.</td></tr><tr><td colspan="1" class="confluenceTd">Flink version</td><td colspan="1" class="confluenceTd">Select version according to the execution environment.</td></tr><tr><td colspan="1" class="confluenceTd">JobManager memory</td><td colspan="1" class="confluenceTd">Used to set the size of jobManager memories, which can be set according to the actual production environment.</td></tr><tr><td colspan="1" class="confluenceTd">Number of slots</td><td colspan="1" class="confluenceTd">Used to set the number of slots, which can be set according to the actual production environment.</td></tr><tr><td colspan="1" class="confluenceTd">TaskManager memory</td><td colspan="1" class="confluenceTd">Used to set the size of taskManager memories, which can be set according to the actual production environment.</td></tr><tr><td colspan="1" class="confluenceTd">Number of TaskManager</td><td colspan="1" class="confluenceTd">Used to set the number of taskManagers, which can be set according to the actual production environment.</td></tr><tr><td colspan="1" class="confluenceTd">Parallelism</td><td colspan="1" class="confluenceTd">Used to set the degree of parallelism for executing Flink tasks.</td></tr><tr><td colspan="1" class="confluenceTd">Main Arguments</td><td colspan="1" class="confluenceTd"><p>Set the input parameters for the Flink program and support the substitution of custom parameter variables.</p></td></tr><tr><td colspan="1" class="confluenceTd">Optional parameters</td><td colspan="1" class="confluenceTd"><p>Supports <code>--jars</code>,&nbsp;<code>--files</code>,<code>--archives</code>,&nbsp;<code>--conf</code>&nbsp;format.</p></td></tr><tr><td colspan="1" class="confluenceTd"><p>Resources</p></td><td colspan="1" class="confluenceTd"><p>Appoint resource files in the&nbsp;<code>Resource</code>&nbsp;if parameters refer to them.</p></td></tr><tr><td colspan="1" class="confluenceTd">Custom parameter</td><td colspan="1" class="confluenceTd"><p>It is a local user-defined parameter for Flink, and will replace the content with<span>&nbsp;</span><code>${variable}</code><span>&nbsp;</span>in the script.</p></td></tr><tr><td colspan="1" class="confluenceTd">Pre tasks</td><td colspan="1" class="confluenceTd">Selecting a predecessor task for the current task, will set the selected predecessor task as upstream of the current task.</td></tr></tbody></table>

  

Task Example

### Execute the WordCount Program

This is a common introductory case in the big data ecosystem, which often apply to computational frameworks such as MapReduce, Flink and Spark. The main purpose is to count the number of identical words in the input text. (Flink's releases attach this example job)

#### Configure the flink environment in DolphinScheduler

If you are using the flink task type in a production environment, it is necessary to configure the required environment first. The following is the configuration file: `bin/env/dolphinscheduler_env.sh`.

![demo-flink-simple](/img/tasks/demo/flink_task01.png)

#### Upload the Main Package

When using the Flink task node, you need to upload the jar package to the Resource Center for the execution, refer to the [resource center](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/resource/configuration.html).

After finish the Resource Centre configuration, upload the required target files directly by dragging and dropping.

![resource_upload](/img/tasks/demo/upload_jar.png)

#### Configure Flink Nodes

Configure the required content according to the parameter descriptions above.

![demo-flink-simple](/img/tasks/demo/flink_task02.png)

### Execute the FlinkSQL Program

Configure the required content according to the parameter descriptions above.

![demo-flink-sql-simple](/img/tasks/demo/flink_sql_test.png)

Note
----

*   JAVA and Scala only used for identification, there is no difference. If use Python to develop Flink, there is no class of the main function and the rest is the same.
    
*   Use SQL to execute Flink SQL tasks, currently only Flink 1.13 and above are supported.