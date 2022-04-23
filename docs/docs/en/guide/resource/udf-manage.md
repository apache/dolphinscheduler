# UDF Manage

The resource management and file management functions are similar. The difference is that the resource management is the UDF upload function, and the file management uploads the user programs, scripts and configuration files. Operation function: rename, download, delete.

- Upload UDF resources

> Same as uploading files.

### Function Management

- Create UDF function

> Click "Create UDF Function", enter the UDF function parameters, select the UDF resource, and click "Submit" to create the UDF function.
> Currently, only supports temporary UDF functions of Hive.

- UDF function name: enter the name of the UDF function.
- Package name Class name: enter the full path of the UDF function.
- UDF resource: set the resource file corresponding to the created UDF function.

![create-udf](/img/new_ui/dev/resource/create-udf.png)

## Example

### Write UDF functions

You can customize UDF functions based on actual production requirements. Write a function that adds "HelloWorld" to the end of any string. As shown below:

![code-udf](/img/new_ui/dev/resource/demo/udf-demo01.png)

### Configure the UDF function

Before configuring the UDF function, upload the jar package of the UDF function through resource management. Then enter function management and configure related information. As shown below:

![conf-udf](/img/new_ui/dev/resource/demo/udf-demo02.png)

### Use UDF functions

When using UDF functions, you only need to write specific functions and upload the configuration through the resource center. The system automatically configures the create function statement as follows: [SqlTask](https://github.com/apache/dolphinscheduler/blob/923f3f38e3271d7f1d22b3abc3497cecb6957e4a/dolphinscheduler-task-plugin/dolphinscheduler-task-sql/src/main/java/org/apache/dolphinscheduler/plugin/task/sql/SqlTask.java#L507-L531)

Enter the workflow and define an SQL node. Set the data source type to HIVE and the data source instance type to HIVE/IMPALA.

- SQL statement: `select HwUdf("abc");` This function is used in the same way as the built-in functions, and can be accessed directly using the function name.
- UDF function: Select the one configured for the resource center.

![use-udf](/img/new_ui/dev/resource/demo/udf-demo03.png)
