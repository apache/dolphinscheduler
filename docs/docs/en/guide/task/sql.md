# SQL

## Overview

SQL task type used to connect to databases and execute SQL.

## Create DataSource

Refer to [DataSource](../datasource/introduction.md)

## Create Task

- Click `Project -> Management-Project -> Name-Workflow Definition`, and click the "Create Workflow" button to enter the DAG editing page.
- Drag from the toolbar <img src="/img/tasks/icons/sql.png" width="25"/> to the canvas.

## Task Parameter

- Data source: Select the corresponding DataSource.
- SQL type: Supports query and non-query. The query is a `select` type query, which is returned with a result set. You can specify three templates for email notification: form, attachment or form attachment. Non-queries return without a result set, three types of operations are: update, delete and insert.
- SQL parameter: The input parameter format is `key1=value1;key2=value2...`.
- SQL statement: SQL statement.
- UDF function: For Hive DataSources, you can refer to UDF functions created in the resource center, but other DataSource do not support UDF functions.
- Custom parameters: SQL task type, and stored procedure is a custom parameter order, to set customized parameter type and data type for the method is the same as the stored procedure task type. The difference is that the custom parameter of the SQL task type replaces the `${variable}` in the SQL statement.
- Pre-SQL: Pre-SQL executes before the SQL statement.
- Post-SQL: Post-SQL executes after the SQL statement.

## Task Example

### Create a Temporary Table in Hive and Write Data

This example creates a temporary table `tmp_hello_world` in Hive and writes a row of data. Before creating a temporary table, we need to ensure that the table does not exist. So we use custom parameters to obtain the time of the day as the suffix of the table name every time we run, this task can run every different day. The format of the created table name is: `tmp_hello_world_{yyyyMMdd}`.

![hive-sql](/img/tasks/demo/hive-sql.png)

### After Running the Task Successfully, Query the Results in Hive

Log in to the bigdata cluster and use 'hive' command or 'beeline' or 'JDBC' and other methods to connect to the 'Apache Hive' for the query. The query SQL is `select * from tmp_hello_world_{yyyyMMdd}`, please replace `{yyyyMMdd}` with the date of the running day. The following shows the query screenshot:

![hive-sql](/img/tasks/demo/hive-result.png)

## Notice

Pay attention to the selection of SQL type. If it is an insert operation, need to change to "Non-Query" type.
