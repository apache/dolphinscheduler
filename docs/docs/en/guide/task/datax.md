# DataX

## Overview

DataX task type for executing DataX programs. For DataX nodes, the worker will execute `${DATAX_HOME}/bin/datax.py` to analyze the input json file.

## Create Task

- Click `Project -> Management-Project -> Name-Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag from the toolbar <img src="/img/tasks/icons/datax.png" width="15"/> task node to canvas.

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
- **Custom template**: Customize the content of the DataX node's JSON profile when the default DataSource provided does not meet the requirements.
- **JSON**: JSON configuration file for DataX synchronization.
- **Custom parameters**: SQL task type, and stored procedure is a custom parameter order, to set customized parameter type and data type for the method is the same as the stored procedure task type. The difference is that the custom parameter of the SQL task type replaces the `${variable}` in the SQL statement.
- **Data source**: Select the data source to extract data.
- **SQL statement**: The SQL statement used to extract data from the target database, the SQL query column name is automatically parsed when execute the node, and mapped to the target table to synchronize column name. When the column names of the source table and the target table are inconsistent, they can be converted by column alias (as)
- **Target library**: Select the target library for data synchronization.
- **Pre-SQL**: Pre-SQL executes before the SQL statement (executed by the target database).
- **Post-SQL**: Post-SQL executes after the SQL statement (executed by the target database).
- **Stream limit (number of bytes)**: Limit the number of bytes for a query.
- **Limit flow (number of records)**: Limit the number of records for a query.
- **Running memory**: Set the minimum and maximum memory required, which can be set according to the actual production environment.
- **Predecessor task**: Selecting a predecessor task for the current task, will set the selected predecessor task as upstream of the current task.

## Task Example

This example demonstrates how to import data from Hive into MySQL.

### Configure the DataX environment in DolphinScheduler

If you are using the DataX task type in a production environment, it is necessary to configure the required environment first. The following is the configuration file: `bin/env/dolphinscheduler_env.sh`.

![datax_task01](/img/tasks/demo/datax_task01.png)

After finish the environment configuration, need to restart DolphinScheduler.

### Configure DataX Task Node

As the default DataSource does not contain data read from Hive, require a custom JSON, refer to: [HDFS Writer](https://github.com/alibaba/DataX/blob/master/hdfswriter/doc/hdfswriter.md). Note: Partition directories exist on the HDFS path, when importing data in real world situations, partitioning is recommended to be passed as a parameter, using custom parameters.

After finish the required JSON file, you can configure the node by following the steps in the diagram below:

![datax_task02](/img/tasks/demo/datax_task02.png)

### View Execution Result

![datax_task03](/img/tasks/demo/datax_task03.png)

### Notice

If the default DataSource provided does not meet your needs, you can configure the writer and reader of the DataX according to the actual usage environment in the custom template options, available at [DataX](https://github.com/alibaba/DataX).
