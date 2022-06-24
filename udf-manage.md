Resource Management
====================

*   The functions of resource management and file management are similar, the difference is that resource management is an uploaded UDF function, and file management uploads user programs, scripts and configuration files.
*   It mainly includes the following operations: rename, download, delete, etc.

*   Upload UDF resource: Same as upload file.

Function Management
-------------------

*   Create UDF function:
    
    Click "`Create UDF Function`", enter the parameters of the UDF function, select the udf resource, and click "`Submit`" to create the UDF function. Currently only temporary UDF functions for HIVE are supported.
    

*   UDF function name: The name when entering the UDF function.
*   Package Name Class Name: Enter the full path of the UDF function.
*   UDF resource: Set the resource file corresponding to the created UDF.

![create-udf](/img/new_ui/dev/resource/create-udf.png)

Task Example
------------

### Writing UDF functions

Users can customize the desired UDF function according to actual production requirements. Here's a function that appends "HelloWorld" to the end of any string. As shown below:

![code-udf](/img/new_ui/dev/resource/demo/udf-demo01.png)

### Configure UDF functions

Before configuring UDF functions, you need to upload the required function jar package through resource management. Then enter the function management and configure the relevant information. As shown below:

![conf-udf](/img/new_ui/dev/resource/demo/udf-demo02.png)

### Using UDF functions

In the process of using UDF functions, users only need to pay attention to the specific function writing, and upload the configuration through the resource center. The system will automatically configure the create function statement, refer to the following: [SqlTask](https://github.com/apache/dolphinscheduler/blob/923f3f38e3271d7f1d22b3abc3497cecb6957e4a/dolphinscheduler-task-plugin/dolphinscheduler-task-sql/src/main/java/org/apache/dolphinscheduler/plugin/task/sql/SqlTask.java#L507-L531)

Enter the workflow to define an SQL node, the data source type is HIVE, and the data source instance type is HIVE/IMPALA.

*   SQL statement: `select HwUdf("abc");`This function is used in the same way as the built-in function, and can be accessed directly by using the function name.
*   UDF function: select the one configured in the resource center.

![use-udf](/img/new_ui/dev/resource/demo/udf-demo03.png)