# Flink Node

## Overview

Flink task type, used to execute Flink programs. For Flink nodes:

1. When the program type is Java, Scala or Python, the worker submits the task `flink run` using the Flink command. See [flink cli](https://nightlies.apache.org/flink/flink-docs-release-1.14/docs/deployment/cli/) for more details.

2. When the program type is SQL, the worker submit tasks using `sql-client.sh`. See [flink sql client](https://nightlies.apache.org/flink/flink-docs-master/docs/dev/table/sqlclient/) for more details.

## Create Task

- Click `Project -> Management-Project -> Name-Workflow Definition`, and click the "Create Workflow" button to enter the DAG editing page.
- Drag from the toolbar <img src="/img/tasks/icons/flink.png" width="15"/>task node to canvas.

## Task Parameter

- **Node name**: The node name in a workflow definition is unique.
- **Run flag**: Identifies whether this node schedules normally, if it does not need to execute, select the `prohibition execution`.
- **Descriptive information**: Describe the function of the node.
- **Task priority**: When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.
- **Worker grouping**: Assign tasks to the machines of the worker group to execute. If `Default` is selected, randomly select a worker machine for execution.
- **Environment Name**: Configure the environment name in which run the script.
- **Times of failed retry attempts**: The number of times the task failed to resubmit.
- **Failed retry interval**: The time interval (unit minute) for resubmitting the task after a failed task.
- **Delayed execution time**: The time (unit minute) that a task delays in execution.
- **Timeout alarm**: Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm email will send and the task execution will fail.
- **Program type**: Support Java, Scala, Python and SQL four languages.
- **The class of main function**: The **full path** of Main Class, the entry point of the Flink program.
- **Main jar package**: The jar package of the Flink program (upload by Resource Center).
- **Deployment mode**: Support 2 deployment modes: cluster and local.
- **Initialization script**: Script file to initialize session context.
- **Script**: The sql script file developed by the user that should be executed.
- **Flink version**: Select version according to the execution env.
- **Task name** (optional): Flink task name.
- **JobManager memory size**: Used to set the size of jobManager memories, which can be set according to the actual production environment.
- **Number of slots**: Used to set the number of slots, which can be set according to the actual production environment.
- **TaskManager memory size**: Used to set the size of taskManager memories, which can be set according to the actual production environment.
- **Number of TaskManager**: Used to set the number of taskManagers, which can be set according to the actual production environment.
- **Parallelism**: Used to set the degree of parallelism for executing Flink tasks.
- **Main program parameters**: Set the input parameters for the Flink program and support the substitution of custom parameter variables.
- **Optional parameters**: Support `--jar`, `--files`,` --archives`, `--conf` format.
- **Resource**: Appoint resource files in the `Resource` if parameters refer to them.
- **Custom parameter**: It is a local user-defined parameter for Flink, and will replace the content with `${variable}` in the script.
- **Predecessor task**: Selecting a predecessor task for the current task, will set the selected predecessor task as upstream of the current task.

## Task Example

### Execute the WordCount Program

This is a common introductory case in the big data ecosystem, which often apply to computational frameworks such as MapReduce, Flink and Spark. The main purpose is to count the number of identical words in the input text. (Flink's releases attach this example job)

#### Configure the flink environment in DolphinScheduler

If you are using the flink task type in a production environment, it is necessary to configure the required environment first. The following is the configuration file: `bin/env/dolphinscheduler_env.sh`.

![demo-flink-simple](/img/tasks/demo/flink_task01.png)

#### Upload the Main Package

When using the Flink task node, you need to upload the jar package to the Resource Center for the execution, refer to the [resource center](../resource/configuration.md).

After finish the Resource Centre configuration, upload the required target files directly by dragging and dropping.

![resource_upload](/img/tasks/demo/upload_jar.png)

#### Configure Flink Nodes

Configure the required content according to the parameter descriptions above.

![demo-flink-simple](/img/tasks/demo/flink_task02.png)

### Execute the FlinkSQL Program

Configure the required content according to the parameter descriptions above.

![demo-flink-sql-simple](/img/tasks/demo/flink_sql_test.png)

## Notice

- JAVA and Scala only used for identification, there is no difference. If use Python to develop Flink, there is no class of the main function and the rest is the same.

- Use SQL to execute Flink SQL tasks, currently only Flink 1.13 and above are supported.
