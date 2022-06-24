# SQL Node
Overview
--------

SQL task type used to connect to databases and execute SQL.

Create DataSource
-----------------

Refer to [DataSource](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/datasource/introduction.html)

Create Task
-----------

*   Click `Project Management -> Project Name -> Workflow Definition`, and click the "`Create Workflow`" button to enter the DAG editing page.
*   Drag from the toolbar <img src="/img/tasks/icons/sql.png" width="15"/> to the canvas.

Task Parameter
--------------

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Datasource Types</td><td class="confluenceTd">Select the corresponding datasource.</td></tr><tr><td colspan="1" class="confluenceTd">Datasource Instances</td><td colspan="1" class="confluenceTd">Select datasource instance.&nbsp;</td></tr><tr><td colspan="1" class="confluenceTd">SQL Type</td><td colspan="1" class="confluenceTd"><p>Supports query and non-query.</p><ul><li>Query: supports<span>&nbsp;</span><code>DML select</code><span>&nbsp;</span>type commands, which return a result set. You can specify three templates for email notification as form, attachment or form attachment;</li><li>Non-query: support<span>&nbsp;</span><code>DDL</code><span>&nbsp;</span>all commands and<span>&nbsp;</span><code>DML update, delete, insert</code><span>&nbsp;</span>three types of commands;<ul><li>Segmented execution symbol: When the data source does not support executing multiple SQL statements at a time, the symbol for splitting SQL statements is provided to call the data source execution method multiple times. Example: 1. When the Hive data source is selected as the data source, this parameter does not need to be filled in. Because the Hive data source itself supports executing multiple SQL statements at one time; 2. When the MySQL data source is selected as the data source, and multi-segment SQL statements are to be executed, this parameter needs to be filled in with a semicolon<span>&nbsp;</span><code>;</code>. Because the MySQL data source does not support executing multiple SQL statements at one time.</li></ul></li></ul></td></tr><tr><td colspan="1" class="confluenceTd">SQL parameter</td><td colspan="1" class="confluenceTd"><p>The input parameter format is<span>&nbsp;</span><code>key1=value1;key2=value2...</code>.</p></td></tr><tr><td colspan="1" class="confluenceTd">SQL statement</td><td colspan="1" class="confluenceTd">SQL statements.</td></tr><tr><td colspan="1" class="confluenceTd">UDF function</td><td colspan="1" class="confluenceTd">For Hive datasources, you can refer to UDF functions created in the resource center, but other datasource do not support UDF functions.</td></tr><tr><td class="confluenceTd">Custom parameters</td><td class="confluenceTd"><p>SQL task type, and stored procedure is a custom parameter order, to set customized parameter type and data type for the method is the same as the stored procedure task type. The difference is that the custom parameter of the SQL task type replaces the<span>&nbsp;</span><code>${variable}</code><span>&nbsp;</span>in the SQL statement.</p></td></tr><tr><td class="confluenceTd">Pre-SQL</td><td class="confluenceTd">Pre-SQL executes before the SQL statement.</td></tr><tr><td colspan="1" class="confluenceTd">Post-SQL</td><td colspan="1" class="confluenceTd">Post-SQL executes after the SQL statement.</td></tr></tbody></table>

Task Example
------------

### Create a Temporary Table in Hive and Write Data

This example creates a temporary table `tmp_hello_world` in Hive and writes a row of data. Before creating a temporary table, we need to ensure that the table does not exist. So we use custom parameters to obtain the time of the day as the suffix of the table name every time we run, this task can run every different day. The format of the created table name is: `tmp_hello_world_{yyyyMMdd}`.

![hive-sql](/img/tasks/demo/hive-sql.png)

### After Running the Task Successfully, Query the Results in Hive

Log in to the bigdata cluster and use 'hive' command or 'beeline' or 'JDBC' and other methods to connect to the 'Apache Hive' for the query. The query SQL is `select * from tmp_hello_world_{yyyyMMdd}`, please replace `{yyyyMMdd}` with the date of the running day. The following shows the query screenshot:

![hive-sql](/img/tasks/demo/hive-result.png)

Note
----

Pay attention to the selection of SQL type. If it is an insert operation, need to change to "Non-Query" type.

To compatible with long session,UDF function are created by the syntax(CREATE OR REPLACE)