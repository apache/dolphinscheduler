# DMS Node

## Overview

[Pytorch](https://pytorch.org) is a mainstream Python machine learning library.

[AWS Database Migration Service (AWS DMS)](https://aws.amazon.com/cn/dms) helps you migrate databases to AWS quickly and securely. 
The source database remains fully operational during the migration, minimizing downtime to applications that rely on the database. 
The AWS Database Migration Service can migrate your data to and from the most widely used commercial and open-source databases.

DMS task plugin can help users to create and start DMS tasks in DolphinScheduler more conveniently.

Contains two features:
- Create DMS task and start DMS task
- Restart DMS task

We can create DMS task and start DMS task in two ways:
- Use interface
- Use json data

DolphinScheduler will track the status of the DMS task and set the status to successfully completed when the DMS task is completed. Except for the CDC task without end time.

So, if the `migrationType` is `cdc` or `full-load-and-cdc`, `cdcStopPosition` not be set, DolphinScheduler will set the status to successfully after the DMS task start successfully.

## Create Task

- Click `Project Management -> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag <img src="../../../../img/tasks/icons/dms.png" width="15"/> from the toolbar to the canvas.

## Task Example

The task plugin picture is as follows

**Create and start DMS task by interface**

![dms](../../../../img/tasks/demo/dms_create_and_start.png)


**Restart DMS task by interface**

![dms](../../../../img/tasks/demo/dms_restart.png)


**Create and start DMS task by json data**

![dms](../../../../img/tasks/demo/dms_create_and_start_json.png)

**Restart DMS task by json data**

![dms](../../../../img/tasks/demo/dms_restart_json.png)



### First, introduce some general parameters of DolphinScheduler

- **Node name**: The node name in a workflow definition is unique.
- **Run flag**: Identifies whether this node schedules normally, if it does not need to execute, select
  the `prohibition execution`.
- **Descriptive information**: Describe the function of the node.
- **Task priority**: When the number of worker threads is insufficient, execute in the order of priority from high
  to low, and tasks with the same priority will execute in a first-in first-out order.
- **Worker grouping**: Assign tasks to the machines of the worker group to execute. If `Default` is selected,
  randomly select a worker machine for execution.
- **Environment Name**: Configure the environment name in which run the script.
- **Times of failed retry attempts**: The number of times the task failed to resubmit.
- **Failed retry interval**: The time interval (unit minute) for resubmitting the task after a failed task.
- **Delayed execution time**: The time (unit minute) that a task delays in execution.
- **Timeout alarm**: Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm
  email will send and the task execution will fail.
- **Resource**: Refers to the list of resource files that need to be called in the script, and the files uploaded or created in Resource Center - File Management.
- **User-defined parameters**: It is a user-defined parameter of Shell, which will replace the content with `${variable}` in the script.
- **Predecessor task**: Selecting a predecessor task for the current task, will set the selected predecessor task as
  upstream of the current task.


### Here are some specific parameters for the DMS plugin


- **isRestartTask**：Whether to restart the task. If it is true, the task will be restarted. If it is false, the task will be created and started.
- **isJsonFormat**：Whether to use json data to create and start the task. If it is true, the task will be created and started by json data. If it is false, the task will be created and started by interface.
- **jsonData**：Json data for creating and starting the task. Only when `isJsonFormat` is true, this parameter is valid.

Parameters of creating and starting the task by interface

- **migrationType**：The type of migration. The value can be full-load, cdc, full-load-and-cdc.
- **replicationTaskIdentifier**：The name of the task.
- **replicationInstanceArn**：The ARN of the replication instance.
- **sourceEndpointArn**：The ARN of the source endpoint.
- **targetEndpointArn**：The ARN of the target endpoint.
- **tableMappings**：The mapping of the table.

Parameters of restarting the task by interface

- **replicationTaskArn**：The ARN of the task.


## Environment to prepare

Some AWS configuration is required, modify a field in file `common.properties`
```yaml
# The AWS access key. if resource.storage.type=S3 or use EMR-Task, This configuration is required
resource.aws.access.key.id=<YOUR AWS ACCESS KEY>
# The AWS secret access key. if resource.storage.type=S3 or use EMR-Task, This configuration is required
resource.aws.secret.access.key=<YOUR AWS SECRET KEY>
# The AWS Region to use. if resource.storage.type=S3 or use EMR-Task, This configuration is required
resource.aws.region=<AWS REGION>
```