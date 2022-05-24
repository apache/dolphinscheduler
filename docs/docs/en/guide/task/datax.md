# DataX

## Overview

DataX task type for executing DataX programs. For DataX nodes, the worker will execute `${DATAX_HOME}/bin/datax.py` to analyze the input json file.

## Create Task

- Click Project Management -> Project Name -> Workflow Definition, and click the "Create Workflow" button to enter the DAG editing page.
- Drag the <img src="/img/tasks/icons/datax.png" width="15"/> from the toolbar to the drawing board.

## Task Parameter

- **Node name**: The node name in a workflow definition is unique.
- **Run flag**: Identifies whether this node can be scheduled normally, if it does not need to be executed, you can turn on the prohibition switch.
- **Descriptive information**: describe the function of the node.
- **Task priority**: When the number of worker threads is insufficient, they are executed in order from high to low, and when the priority is the same, they are executed according to the first-in first-out principle.
- **Worker grouping**: Tasks are assigned to the machines of the worker group to execute. If Default is selected, a worker machine will be randomly selected for execution.
- **Environment Name**: Configure the environment name in which to run the script.
- **Number of failed retry attempts**: The number of times the task failed to be resubmitted.
- **Failed retry interval**: The time, in cents, interval for resubmitting the task after a failed task.
- **Delayed execution time**: The time, in cents, that a task is delayed in execution.
- **Timeout alarm**: Check the timeout alarm and timeout failure. When the task exceeds the "timeout period", an alarm email will be sent and the task execution will fail.
- **Custom template**: Custom the content of the DataX node's json profile when the default data source provided does not meet the required requirements.
- **json**: json configuration file for DataX synchronization.
- **Custom parameters**: SQL task type, and stored procedure is a custom parameter order to set values for the method. The custom parameter type and data type are the same as the stored procedure task type. The difference is that the SQL task type custom parameter will replace the \${variable} in the SQL statement.
- **Data source**: Select the data source from which the data will be extracted.
- **sql statement**: the sql statement used to extract data from the target database, the sql query column name is automatically parsed when the node is executed, and mapped to the target table synchronization column name. When the source table and target table column names are inconsistent, they can be converted by column alias.
- **Target library**: Select the target library for data synchronization.
- **Pre-sql**: Pre-sql is executed before the sql statement (executed by the target library).
- **Post-sql**: Post-sql is executed after the sql statement (executed by the target library).
- **Stream limit (number of bytes)**: Limits the number of bytes in the query.
- **Limit flow (number of records)**: Limit the number of records for a query.
- **Running memory**: the minimum and maximum memory required can be configured to suit the actual production environment.
- **Predecessor task**: Selecting a predecessor task for the current task will set the selected predecessor task as upstream of the current task.

## Task Example

This example demonstrates importing data from Hive into MySQL.

### Configuring the DataX environment in DolphinScheduler

If you are using the DataX task type in a production environment, it is necessary to configure the required environment first. The configuration file is as follows: `/dolphinscheduler/conf/env/dolphinscheduler_env.sh`.

![datax_task01](/img/tasks/demo/datax_task01.png)

After the environment has been configured, DolphinScheduler needs to be restarted.

### Configuring DataX Task Node

As the default data source does not contain data to be read from Hive, a custom json is required, refer to: [HDFS Writer](https://github.com/alibaba/DataX/blob/master/hdfswriter/doc/hdfswriter.md). Note: Partition directories exist on the HDFS path, when importing data in real world situations, partitioning is recommended to be passed as a parameter, using custom parameters.

After writing the required json file, you can configure the node content by following the steps in the diagram below.

![datax_task02](/img/tasks/demo/datax_task02.png)

### View run results

![datax_task03](/img/tasks/demo/datax_task03.png)

### Notice

If the default data source provided does not meet your needs, you can configure the writer and reader of DataX according to the actual usage environment in the custom template option, available at https://github.com/alibaba/DataX.
