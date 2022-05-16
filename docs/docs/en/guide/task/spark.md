# Spark Node

## Overview

Spark task type for executing Spark application. When executing the Spark task, the worker will submits a job to the Spark cluster by following commands:

(1) `spark submit` method to submit tasks. See [spark-submit](https://spark.apache.org/docs/3.2.1/submitting-applications.html#launching-applications-with-spark-submit) for more details.

(2) `spark sql` method to submit tasks. See [spark sql](https://spark.apache.org/docs/3.2.1/sql-ref-syntax.html) for more details.

## Create Task

- Click `Project -> Management-Project -> Name-Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag from the toolbar <img src="/img/tasks/icons/spark.png" width="15"/> to the canvas.

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
- **Program type**: Supports Java, Scala, Python and SQL.
- **Spark version**: Support Spark1 and Spark2.
- **The class of main function**: The **full path** of Main Class, the entry point of the Spark program.
- **Main jar package**: The Spark jar package (upload by Resource Center).
- **SQL scripts**: SQL statements in .sql files that Spark sql runs.
- **Deployment mode**: (1) spark submit supports three modes: yarn-clusetr, yarn-client and local.
                       (2) spark sql supports yarn-client and local modes.
- **Task name** (optional): Spark task name.
- **Driver core number**: Set the number of Driver core, which can be set according to the actual production environment.
- **Driver memory size**: Set the size of Driver memories, which can be set according to the actual production environment.
- **Number of Executor**: Set the number of Executor, which can be set according to the actual production environment.
- **Executor memory size**: Set the size of Executor memories, which can be set according to the actual production environment.
- **Main program parameters**: Set the input parameters of the Spark program and support the substitution of custom parameter variables.
- **Optional parameters**: support `--jars`, `--files`,` --archives`, `--conf` format.
- **Resource**: Appoint resource files in the `Resource` if parameters refer to them.
- **Custom parameter**: It is a local user-defined parameter for Spark, and will replace the content with `${variable}` in the script.
- **Predecessor task**: Selecting a predecessor task for the current task, will set the selected predecessor task as upstream of the current task.

## Task Example

### spark submit

#### Execute the WordCount Program

This is a common introductory case in the big data ecosystem, which often apply to computational frameworks such as MapReduce, Flink and Spark. The main purpose is to count the number of identical words in the input text. (Flink's releases attach this example job)

##### Configure the Spark Environment in DolphinScheduler

If you are using the Spark task type in a production environment, it is necessary to configure the required environment first. The following is the configuration file: `bin/env/dolphinscheduler_env.sh`.

![spark_configure](/img/tasks/demo/spark_task01.png)

##### Upload the Main Package

When using the Spark task node, you need to upload the jar package to the Resource Centre for the execution, refer to the [resource center](../resource/configuration.md).

After finish the Resource Centre configuration, upload the required target files directly by dragging and dropping.

![resource_upload](/img/tasks/demo/upload_jar.png)

##### Configure Spark Nodes

Configure the required content according to the parameter descriptions above.

![demo-spark-simple](/img/tasks/demo/spark_task02.png)

### spark sql

#### Execute DDL and DML statements

This case is to create a view table terms and write three rows of data and a table wc in parquet format and determine whether the table exists. The program type is SQL. Insert the data of the view table terms into the table wc in parquet format.

![spark_sql](/img/tasks/demo/spark_sql.png)

## Notice

JAVA and Scala are only used for identification, and there is no difference when you use the Spark task. If your application is developed by Python, you could just ignore the parameter **Main Class** in the form. Parameter **SQL scripts** is only for SQL type and could be ignored in JAVA, Scala and Python.

SQL does not currently support cluster mode.
