# Data Quality


Introduction 
--------------------------

The data quality task is used to check the data accuracy during the integration and processing of data. Data quality tasks in this release include single-table checking, single-table custom SQL checking, multi-table accuracy, and two-table value comparisons. The running environment of the data quality task is Spark 2.4.0, and other versions have not been verified, and users can verify by themselves.

*   The execution logic of the data quality task is as follows:

> The user defines the task on the interface, and when the user input value is saved in `TaskParam` the running task, it `Master` will parse `TaskParam` and encapsulate `DataQuality Task`the required parameters and send it to the `Worker。 Worker` running data quality task, and the data quality task will write the statistical results to the specified storage engine after the running. , the current data quality task result is stored in `dolphinscheduler` the `t_ds_dq_execute_result` table `Worker` Send the task result to `Master`, after `Master` receiving it, `TaskResponse` it will judge whether the task type is `DataQualityTask`, if so, it will read the corresponding result `taskInstanceId` from `t_ds_dq_execute_result`

Precautions
-----------

Add configuration information:`<server-name>/conf/common.properties`

```properties
data-quality.jar.name=dolphinscheduler-data-quality-dev-SNAPSHOT.jar
```

`data-quality.jar.name :` Please fill in the name of the actual package here . If you package it separately `data-quality`, remember to modify the package name to be `data-quality.jar.name` consistent. If the old version is upgraded and used, you need to execute the `sql` update script to initialize the database before running. If you want to use the `MySQL` data, you need to `pom.xml` comment out `MySQL` the `scope` current only tested `MySQL` and data sources, `PostgreSQL` and `HIVE` other data sources have not been tested for the time being. You `Spark` need to configure the read `Hive` metadata, `Spark` not `jdbc` the way to read `Hive.`

Detailed Inspection Logic
-------------------------

*   Check formula: \[check method\]\[operator\]\[threshold\], if the result is true, it means that the data does not meet expectations, and the failure strategy is executed.
    
*   Check method:
    
    *   \[Expected-Actual\]\[Expected-Actual\]
    *   \[Actual-Expected\]\[Actual-Expected\]
    *   \[Actual/Expected\]\[Actual/Expected\]x100%
    *   \[(Expected-Actual)/Expected\]\[(Expected-Actual)/Expected\]x100%
*   Operators: =, >, >=, <, <=, !=
    
*   Expected value type
    
    *   Fixed value
    *   daily average
    *   Weekly mean
    *   Monthly mean
    *   Average of last 7 days
    *   Average of last 30 days
    *   The total number of rows in the source table
    *   The total number of rows in the target table
*   Example
    
    *   The verification method is: \[Expected-Actual\]\[Expected value-Actual value\]
    *   \[operator\]: >
    *   \[threshold\]: 0
    *   Expected value type: fixed value=9.
    
    Suppose the actual value is 10, the operator is >, and the expected value is 9, then the result 10 -9 > 0 is true, which means that the row data in the column has exceeded the threshold, and the task is judged to fail.
    

Task Operation Guide
====================

Null value check for single table check
---------------------------------------

### Inspection Introduction

The goal of the null value check is to check the number of empty rows in the specified column. The number of empty rows can be compared with the total number of rows or a specified threshold. If it is greater than a certain threshold, it will be judged as failure.

*   The SQL statement that calculates the null of the specified column is as follows:
    
    ```sql
    SELECT COUNT(*) AS miss FROM ${src_table} WHERE (${src_field} is null or ${src_field} = '') AND (${src_filter})
    ```
    
*   The SQL to calculate the total number of rows in the table is as follows:
    
    ```sql
    SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})
    ```
    

### Interface Operation Guide

![dataquality_null_check](/img/tasks/demo/null_check.png)

<table class="wrapped confluenceTable">
<colgroup><col><col></colgroup>
<tbody>
<tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr>
<tr><td class="confluenceTd">Source data type</td><td class="confluenceTd">Select MySQL, PostgreSQL, etc.</td></tr>
<tr><td class="confluenceTd">Source data source</td><td class="confluenceTd">The corresponding data source under the source data type.</td></tr>
<tr><td class="confluenceTd">Source data table</td><td class="confluenceTd">Drop-down to select the table where the validation data is located.</td></tr>
<tr><td colspan="1" class="confluenceTd">Source filter condition</td><td colspan="1" class="confluenceTd">Such as title, it will also be used when counting the total number of rows in the table, optional.</td></tr>
<tr><td colspan="1" class="confluenceTd">Source table column check</td><td colspan="1" class="confluenceTd">Drop-down to select check column name.</td></tr><tr><td colspan="1" class="confluenceTd">Check method</td><td colspan="1" class="confluenceTd"><ul><li>[Expected-Actual][Expected-Actual]</li><li>[Actual-Expected][Actual-Expected]</li><li>[Actual/Expected][Actual/Expected]x100%</li><li>[(Expected-Actual)/Expected][(Expected-Actual)/Expected]x100%</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Check operators</td><td colspan="1" class="confluenceTd"><p>=, &gt;, &gt;=, &lt;, &lt;=, !<span>&nbsp;</span>=</p></td></tr><tr><td colspan="1" class="confluenceTd">Threshold</td><td colspan="1" class="confluenceTd">The value used in the formula for comparison.</td></tr><tr><td colspan="1" class="confluenceTd">Failure Strategy</td><td colspan="1" class="confluenceTd"><ul><li>Alarm: The data quality task failed, the DolphinScheduler task result is successful, and an alarm is sent.</li><li>Blocking: The data quality task fails, the DolphinScheduler task result is failed, and an alarm is sent.</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Expected value type</td><td colspan="1" class="confluenceTd">Select the desired type from the drop-down menu.</td></tr></tbody></table>

Timeliness check of single table check
--------------------------------------

### Inspection Introduction

The timeliness check is used to check whether the data is processed within the expected time. The start time and end time can be specified to define the time range. If the amount of data within the time range does not reach the set threshold, the check task will be judged as fail.

### Interface Operation Guide

![dataquality_timeliness_check](/img/tasks/demo/timeliness_check.png)

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Source data type</td><td class="confluenceTd">Select MySQL, PostgreSQL, etc.</td></tr><tr><td class="confluenceTd">Source data source</td><td class="confluenceTd">The corresponding data source under the source data type.</td></tr><tr><td class="confluenceTd">Source data table</td><td class="confluenceTd">Drop-down to select the table where the validation data is located.</td></tr><tr><td colspan="1" class="confluenceTd">Source filter condition</td><td colspan="1" class="confluenceTd">Such as title, it will also be used when counting the total number of rows in the table, optional.</td></tr><tr><td colspan="1" class="confluenceTd">Source table column check</td><td colspan="1" class="confluenceTd">Drop-down to select check column name.</td></tr><tr><td colspan="1" class="confluenceTd">Start Time</td><td colspan="1" class="confluenceTd">The start time of a time range.</td></tr><tr><td colspan="1" class="confluenceTd">End Time</td><td colspan="1" class="confluenceTd">The end time of a time range.</td></tr><tr><td colspan="1" class="confluenceTd">Time Format</td><td colspan="1" class="confluenceTd">Set the corresponding time format.</td></tr><tr><td colspan="1" class="confluenceTd">Check method</td><td colspan="1" class="confluenceTd"><ul><li>[Expected-Actual][Expected-Actual]</li><li>[Actual-Expected][Actual-Expected]</li><li>[Actual/Expected][Actual/Expected]x100%</li><li>[(Expected-Actual)/Expected][(Expected-Actual)/Expected]x100%</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Check operators</td><td colspan="1" class="confluenceTd"><p>=, &gt;, &gt;=, &lt;, &lt;=, !<span>&nbsp;</span>=</p></td></tr><tr><td colspan="1" class="confluenceTd">Threshold</td><td colspan="1" class="confluenceTd">The value used in the formula for comparison.</td></tr><tr><td colspan="1" class="confluenceTd">Failure Strategy</td><td colspan="1" class="confluenceTd"><ul><li>Alarm: The data quality task failed, the DolphinScheduler task result is successful, and an alarm is sent.</li><li>Blocking: The data quality task fails, the DolphinScheduler task result is failed, and an alarm is sent.</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Expected value type</td><td colspan="1" class="confluenceTd">Select the desired type from the drop-down menu.</td></tr></tbody></table>

Field length check for single table check
-----------------------------------------

### Inspection Introduction

The goal of field length verification is to check whether the length of the selected field meets the expectations. If there is data that does not meet the requirements, and the number of rows exceeds the threshold, the task will be judged to fail.

### Interface Operation Guide

![dataquality_length_check](/img/tasks/demo/field_length_check.png)

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Source data type</td><td class="confluenceTd">Select MySQL, PostgreSQL, etc.</td></tr><tr><td class="confluenceTd">Source data source</td><td class="confluenceTd">The corresponding data source under the source data type.</td></tr><tr><td class="confluenceTd">Source data table</td><td class="confluenceTd">Drop-down to select the table where the validation data is located.</td></tr><tr><td colspan="1" class="confluenceTd">Source filter condition</td><td colspan="1" class="confluenceTd">Such as title, it will also be used when counting the total number of rows in the table, optional.</td></tr><tr><td colspan="1" class="confluenceTd">Source table column check</td><td colspan="1" class="confluenceTd">Drop-down to select check column name.</td></tr><tr><td colspan="1" class="confluenceTd">Logical operators</td><td colspan="1" class="confluenceTd"><p>=, &gt;, &gt;=, &lt;, &lt;=, !<span>&nbsp;</span>=</p></td></tr><tr><td colspan="1" class="confluenceTd">Field length limit</td><td colspan="1" class="confluenceTd"><p>Such as title.</p></td></tr><tr><td colspan="1" class="confluenceTd">Check method</td><td colspan="1" class="confluenceTd"><ul><li>[Expected-Actual][Expected-Actual]</li><li>[Actual-Expected][Actual-Expected]</li><li>[Actual/Expected][Actual/Expected]x100%</li><li>[(Expected-Actual)/Expected][(Expected-Actual)/Expected]x100%</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Check operators</td><td colspan="1" class="confluenceTd"><p>=, &gt;, &gt;=, &lt;, &lt;=, !<span>&nbsp;</span>=</p></td></tr><tr><td colspan="1" class="confluenceTd">Threshold</td><td colspan="1" class="confluenceTd">The value used in the formula for comparison.</td></tr><tr><td colspan="1" class="confluenceTd">Failure Strategy</td><td colspan="1" class="confluenceTd"><ul><li>Alarm: The data quality task failed, the DolphinScheduler task result is successful, and an alarm is sent.</li><li>Blocking: The data quality task fails, the DolphinScheduler task result is failed, and an alarm is sent.</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Expected value type</td><td colspan="1" class="confluenceTd">Select the desired type from the drop-down menu.</td></tr></tbody></table>

Uniqueness check for single table check
---------------------------------------

### Inspection Introduction

The goal of the uniqueness check is to check whether the fields are duplicated. It is generally used to check whether the primary key is duplicated. If there are duplicates and the threshold is reached, the check task will be judged to be failed.

### Interface Operation Guide

![dataquality_uniqueness_check](/img/tasks/demo/uniqueness_check.png)

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Source data type</td><td class="confluenceTd">Select MySQL, PostgreSQL, etc.</td></tr><tr><td class="confluenceTd">Source data source</td><td class="confluenceTd">The corresponding data source under the source data type.</td></tr><tr><td class="confluenceTd">Source data table</td><td class="confluenceTd">Drop-down to select the table where the validation data is located.</td></tr><tr><td colspan="1" class="confluenceTd">Source filter condition</td><td colspan="1" class="confluenceTd">Such as title, it will also be used when counting the total number of rows in the table, optional.</td></tr><tr><td colspan="1" class="confluenceTd">Source table column check</td><td colspan="1" class="confluenceTd">Drop-down to select check column name.</td></tr><tr><td colspan="1" class="confluenceTd">Check method</td><td colspan="1" class="confluenceTd"><ul><li>[Expected-Actual][Expected-Actual]</li><li>[Actual-Expected][Actual-Expected]</li><li>[Actual/Expected][Actual/Expected]x100%</li><li>[(Expected-Actual)/Expected][(Expected-Actual)/Expected]x100%</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Check operators</td><td colspan="1" class="confluenceTd"><p>=, &gt;, &gt;=, &lt;, &lt;=, !<span>&nbsp;</span>=</p></td></tr><tr><td colspan="1" class="confluenceTd">Threshold</td><td colspan="1" class="confluenceTd">The value used in the formula for comparison.</td></tr><tr><td colspan="1" class="confluenceTd">Failure Strategy</td><td colspan="1" class="confluenceTd"><ul><li>Alarm: The data quality task failed, the DolphinScheduler task result is successful, and an alarm is sent.</li><li>Blocking: The data quality task fails, the DolphinScheduler task result is failed, and an alarm is sent.</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Expected value type</td><td colspan="1" class="confluenceTd">Select the desired type from the drop-down menu.</td></tr></tbody></table>

Regular expression check for single table check
-----------------------------------------------

### Inspection Introduction

The goal of regular expression verification is to check whether the format of the value of a field meets the requirements, such as time format, email format, ID card format, etc. If there is data that does not meet the format and exceeds the threshold, the task will be judged as failed.

### Interface Operation Guide

![dataquality_regex_check](/img/tasks/demo/regexp_check.png)

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Source data type</td><td class="confluenceTd">Select MySQL, PostgreSQL, etc.</td></tr><tr><td class="confluenceTd">Source data source</td><td class="confluenceTd">The corresponding data source under the source data type.</td></tr><tr><td class="confluenceTd">Source data table</td><td class="confluenceTd">Drop-down to select the table where the validation data is located.</td></tr><tr><td colspan="1" class="confluenceTd">Source filter condition</td><td colspan="1" class="confluenceTd">Such as title, it will also be used when counting the total number of rows in the table, optional.</td></tr><tr><td colspan="1" class="confluenceTd">Source table column check</td><td colspan="1" class="confluenceTd">Drop-down to select check column name.</td></tr><tr><td colspan="1" class="confluenceTd">Regular expression</td><td colspan="1" class="confluenceTd">Such as title.</td></tr><tr><td colspan="1" class="confluenceTd">Check method</td><td colspan="1" class="confluenceTd"><ul><li>[Expected-Actual][Expected-Actual]</li><li>[Actual-Expected][Actual-Expected]</li><li>[Actual/Expected][Actual/Expected]x100%</li><li>[(Expected-Actual)/Expected][(Expected-Actual)/Expected]x100%</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Check operators</td><td colspan="1" class="confluenceTd"><p>=, &gt;, &gt;=, &lt;, &lt;=, !<span>&nbsp;</span>=</p></td></tr><tr><td colspan="1" class="confluenceTd">Threshold</td><td colspan="1" class="confluenceTd">The value used in the formula for comparison.</td></tr><tr><td colspan="1" class="confluenceTd">Failure Strategy</td><td colspan="1" class="confluenceTd"><ul><li>Alarm: The data quality task failed, the DolphinScheduler task result is successful, and an alarm is sent.</li><li>Blocking: The data quality task fails, the DolphinScheduler task result is failed, and an alarm is sent.</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Expected value type</td><td colspan="1" class="confluenceTd">Select the desired type from the drop-down menu.</td></tr></tbody></table>

Enumeration value validation for single table check
---------------------------------------------------

### Inspection Introduction

The goal of enumeration value verification is to check whether the value of a field is within the range of the enumeration value. If there is data that is not in the range of the enumeration value and exceeds the threshold, the task will be judged to fail.

### Interface Operation Guide

![dataquality_enum_check](/img/tasks/demo/enumeration_check.png)

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Source data type</td><td class="confluenceTd">Select MySQL, PostgreSQL, etc.</td></tr><tr><td class="confluenceTd">Source data source</td><td class="confluenceTd">The corresponding data source under the source data type.</td></tr><tr><td class="confluenceTd">Source data table</td><td class="confluenceTd">Drop-down to select the table where the validation data is located.</td></tr><tr><td colspan="1" class="confluenceTd">Source filter condition</td><td colspan="1" class="confluenceTd">Such as title, it will also be used when counting the total number of rows in the table, optional.</td></tr><tr><td colspan="1" class="confluenceTd">Source table column check</td><td colspan="1" class="confluenceTd">Drop-down to select check column name.</td></tr><tr><td colspan="1" class="confluenceTd">List of enumeration values</td><td colspan="1" class="confluenceTd"><p>Separated by commas.</p></td></tr><tr><td colspan="1" class="confluenceTd">Check method</td><td colspan="1" class="confluenceTd"><ul><li>[Expected-Actual][Expected-Actual]</li><li>[Actual-Expected][Actual-Expected]</li><li>[Actual/Expected][Actual/Expected]x100%</li><li>[(Expected-Actual)/Expected][(Expected-Actual)/Expected]x100%</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Check operators</td><td colspan="1" class="confluenceTd"><p>=, &gt;, &gt;=, &lt;, &lt;=, !<span>&nbsp;</span>=</p></td></tr><tr><td colspan="1" class="confluenceTd">Threshold</td><td colspan="1" class="confluenceTd">The value used in the formula for comparison.</td></tr><tr><td colspan="1" class="confluenceTd">Failure Strategy</td><td colspan="1" class="confluenceTd"><ul><li>Alarm: The data quality task failed, the DolphinScheduler task result is successful, and an alarm is sent.</li><li>Blocking: The data quality task fails, the DolphinScheduler task result is failed, and an alarm is sent.</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Expected value type</td><td colspan="1" class="confluenceTd">Select the desired type from the drop-down menu.</td></tr></tbody></table>

Table row number verification for single table check
----------------------------------------------------

### Inspection Introduction

The goal of table row number verification is to check whether the number of rows in the table reaches the expected value. If the number of rows does not meet the standard, the task will be judged as failed.

### Interface Operation Guide

![dataquality_count_check](/img/tasks/demo/table_count_check.png)

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Source data type</td><td class="confluenceTd">Select MySQL, PostgreSQL, etc.</td></tr><tr><td class="confluenceTd">Source data source</td><td class="confluenceTd">The corresponding data source under the source data type.</td></tr><tr><td class="confluenceTd">Source data table</td><td class="confluenceTd">Drop-down to select the table where the validation data is located.</td></tr><tr><td colspan="1" class="confluenceTd">Source filter condition</td><td colspan="1" class="confluenceTd">Such as title, it will also be used when counting the total number of rows in the table, optional.</td></tr><tr><td colspan="1" class="confluenceTd">Source table column check</td><td colspan="1" class="confluenceTd">Drop-down to select check column name.</td></tr><tr><td colspan="1" class="confluenceTd">Check method</td><td colspan="1" class="confluenceTd"><ul><li>[Expected-Actual][Expected-Actual]</li><li>[Actual-Expected][Actual-Expected]</li><li>[Actual/Expected][Actual/Expected]x100%</li><li>[(Expected-Actual)/Expected][(Expected-Actual)/Expected]x100%</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Check operators</td><td colspan="1" class="confluenceTd"><p>=, &gt;, &gt;=, &lt;, &lt;=, !<span>&nbsp;</span>=</p></td></tr><tr><td colspan="1" class="confluenceTd">Threshold</td><td colspan="1" class="confluenceTd">The value used in the formula for comparison.</td></tr><tr><td colspan="1" class="confluenceTd">Failure Strategy</td><td colspan="1" class="confluenceTd"><ul><li>Alarm: The data quality task failed, the DolphinScheduler task result is successful, and an alarm is sent.</li><li>Blocking: The data quality task fails, the DolphinScheduler task result is failed, and an alarm is sent.</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Expected value type</td><td colspan="1" class="confluenceTd">Select the desired type from the drop-down menu.</td></tr></tbody></table>

Custom SQL check for single table check
---------------------------------------

### Inspection Introduction

### Interface Operation Guide

![dataquality_custom_sql_check](/img/tasks/demo/custom_sql_check.png)

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Source data type</td><td class="confluenceTd">Select MySQL, PostgreSQL, etc.</td></tr><tr><td class="confluenceTd">Source data source</td><td class="confluenceTd">The corresponding data source under the source data type.</td></tr><tr><td class="confluenceTd">Source data table</td><td class="confluenceTd">Drop-down to select the table where the validation data is located.</td></tr><tr><td colspan="1" class="confluenceTd">Actual value name</td><td colspan="1" class="confluenceTd">Alias ​​in SQL for statistical value calculation, such as max_num.</td></tr><tr><td colspan="1" class="confluenceTd">Actual value calculation SQL</td><td colspan="1" class="confluenceTd"><p>SQL for outputting actual values.</p><ul><li style="list-style-type: none;"><ul><li>Note: The SQL must be statistical SQL, such as counting the number of rows, calculating the maximum value, minimum value, etc.</li><li>select max(a) as max_num from ${src_table}, the table name must be filled like this.</li></ul></li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Source filter condition</td><td colspan="1" class="confluenceTd">Such as title, it will also be used when counting the total number of rows in the table, optional.</td></tr><tr><td colspan="1" class="confluenceTd">Check method</td><td colspan="1" class="confluenceTd"><ul><li>[Expected-Actual][Expected-Actual]</li><li>[Actual-Expected][Actual-Expected]</li><li>[Actual/Expected][Actual/Expected]x100%</li><li>[(Expected-Actual)/Expected][(Expected-Actual)/Expected]x100%</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Check operators</td><td colspan="1" class="confluenceTd"><p>=, &gt;, &gt;=, &lt;, &lt;=, !<span>&nbsp;</span>=</p></td></tr><tr><td colspan="1" class="confluenceTd">Threshold</td><td colspan="1" class="confluenceTd">The value used in the formula for comparison.</td></tr><tr><td colspan="1" class="confluenceTd">Failure Strategy</td><td colspan="1" class="confluenceTd"><ul><li>Alarm: The data quality task failed, the DolphinScheduler task result is successful, and an alarm is sent.</li><li>Blocking: The data quality task fails, the DolphinScheduler task result is failed, and an alarm is sent.</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Expected value type</td><td colspan="1" class="confluenceTd">Select the desired type from the drop-down menu.</td></tr></tbody></table>

Accuracy check of multi-table check
-----------------------------------

### Inspection Introduction

Accuracy checks are performed by comparing the accuracy differences of data records for selected fields between two tables, examples are as follows:

*   table test1

| c1 | c2 |
| --- | --- |
| a | 1 |
| b | 2 |

*   table test2

| c21 | c22 |
| --- | --- |
| a | 1 |
| b | 3 |

If you compare the data in c1 and c21, the tables test1 and test2 are exactly the same. If you compare c2 and c22, the data in table test1 and table test2 are inconsistent.

### Interface Operation Guide

![dataquality_multi_table_accuracy_check](/img/tasks/demo/multi_table_accuracy_check.png)

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Source data type</td><td class="confluenceTd">Select MySQL, PostgreSQL, etc.</td></tr><tr><td class="confluenceTd">Source data source</td><td class="confluenceTd">The corresponding data source under the source data type.</td></tr><tr><td class="confluenceTd">Source data table</td><td class="confluenceTd">Drop-down to select the table where the validation data is located.</td></tr><tr><td colspan="1" class="confluenceTd">Source filter condition</td><td colspan="1" class="confluenceTd">Such as title, it will also be used when counting the total number of rows in the table, optional.</td></tr><tr><td colspan="1" class="confluenceTd">Target data type</td><td colspan="1" class="confluenceTd">Select MySQL, PostgreSQL, etc.</td></tr><tr><td colspan="1" class="confluenceTd">Target data source</td><td colspan="1" class="confluenceTd">The corresponding data source under the source data type.</td></tr><tr><td colspan="1" class="confluenceTd">Target data table</td><td colspan="1" class="confluenceTd">Drop-down to select the table where the data to be verified is located.</td></tr><tr><td colspan="1" class="confluenceTd">Target filter condition</td><td colspan="1" class="confluenceTd"><p>Such as title, it will also be used when counting the total number of rows in the table, optional.</p></td></tr><tr><td colspan="1" class="confluenceTd">Check column</td><td colspan="1" class="confluenceTd"><p>Fill in the source data column, operator, and target data column respectively.</p></td></tr><tr><td colspan="1" class="confluenceTd">Verification method</td><td colspan="1" class="confluenceTd">Select the desired verification method.</td></tr><tr><td colspan="1" class="confluenceTd">Check operators</td><td colspan="1" class="confluenceTd"><p>=, &gt;, &gt;=, &lt;, &lt;=, !<span>&nbsp;</span>=</p></td></tr><tr><td colspan="1" class="confluenceTd">Threshold</td><td colspan="1" class="confluenceTd">The value used in the formula for comparison.</td></tr><tr><td colspan="1" class="confluenceTd">Failure Strategy</td><td colspan="1" class="confluenceTd"><ul><li>Alarm: The data quality task failed, the DolphinScheduler task result is successful, and an alarm is sent.</li><li>Blocking: The data quality task fails, the DolphinScheduler task result is failed, and an alarm is sent.</li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Expected value type</td><td colspan="1" class="confluenceTd">Select the desired type from the drop-down menu. Only SrcTableTotalRow, TargetTableTotalRow and fixed value are suitable for selection here.</td></tr></tbody></table>

Comparison of the values ​​checked by the two tables
----------------------------------------------------

### Inspection introduction

Two-table value comparison allows users to customize different SQL statistics for two tables and compare the corresponding values. For example, the total amount sum1 of a certain column is calculated for the source table A, and the total amount of a certain column is calculated for the target table. value sum2, compare sum1 and sum2 to determine the check result.

### Interface Operation Guide

![dataquality_multi_table_comparison_check](/img/tasks/demo/multi_table_comparison_check.png)

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Source data type</td><td class="confluenceTd">Select MySQL, PostgreSQL, etc.</td></tr><tr><td class="confluenceTd">Source data source</td><td class="confluenceTd">The corresponding data source under the source data type.</td></tr><tr><td class="confluenceTd">Source data table</td><td class="confluenceTd">Drop-down to select the table where the validation data is located.</td></tr><tr><td colspan="1" class="confluenceTd">Actual value name</td><td colspan="1" class="confluenceTd">Calculate the alias in SQL for the actual value, such as max_age1</td></tr><tr><td colspan="1" class="confluenceTd">Actual value calculation SQL</td><td colspan="1" class="confluenceTd"><p>SQL for outputting actual values,</p><ul><li style="list-style-type: none;"><ul><li>Note: The SQL must be statistical SQL, such as counting the number of rows, calculating the maximum value, minimum value, etc.</li><li>select max(age) as max_age1 from ${src_table} The table name must be filled like this</li></ul></li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Target data type</td><td colspan="1" class="confluenceTd">Select MySQL, PostgreSQL, etc.</td></tr><tr><td colspan="1" class="confluenceTd">Target data source</td><td colspan="1" class="confluenceTd">The corresponding data source under the source data type.</td></tr><tr><td colspan="1" class="confluenceTd">Target data table</td><td colspan="1" class="confluenceTd">Drop-down to select the table where the data to be verified is located.</td></tr><tr><td colspan="1" class="confluenceTd">Expected value name</td><td colspan="1" class="confluenceTd">Calculate the alias in SQL for the expected value, such as max_age2</td></tr><tr><td colspan="1" class="confluenceTd">Expected value calculation SQL</td><td colspan="1" class="confluenceTd"><p>SQL for outputting expected value,</p><ul><li style="list-style-type: none;"><ul><li>Note: The SQL must be statistical SQL, such as counting the number of rows, calculating the maximum value, minimum value, etc.</li><li>select max(age) as max_age2 from ${target_table} The table name must be filled like this</li></ul></li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Verification method</td><td colspan="1" class="confluenceTd">Select the desired verification method.</td></tr><tr><td colspan="1" class="confluenceTd">Check operators</td><td colspan="1" class="confluenceTd"><p>=, &gt;, &gt;=, &lt;, &lt;=, !<span>&nbsp;</span>=</p></td></tr><tr><td colspan="1" class="confluenceTd">Failure Strategy</td><td colspan="1" class="confluenceTd"><ul><li>Alarm: The data quality task failed, the DolphinScheduler task result is successful, and an alarm is sent.</li><li>Blocking: The data quality task fails, the DolphinScheduler task result is failed, and an alarm is sent.</li></ul></td></tr></tbody></table>

View the task results
---------------------

![dataquality_result](/img/tasks/demo/result.png)

Rule View
---------

### List of Rules

![dataquality_rule_list](/img/tasks/demo/rule_list.png)

### Rule Details

![dataquality_rule_detail](/img/tasks/demo/rule_detail.png)