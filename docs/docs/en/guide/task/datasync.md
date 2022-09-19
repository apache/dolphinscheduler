# DataSync Node

## Overview

[AWS DataSync](https://console.aws.amazon.com/datasync/) is an online data transfer service that simplifies, automates, and accelerates moving data between on-premises storage systems and AWS Storage services, as well as between AWS Storage services.

DataSync can copy data to and from:

- Network File System (NFS) file servers
- Server Message Block (SMB) file servers
- Hadoop Distributed File System (HDFS)
- Object storage systems
- Amazon Simple Storage Service (Amazon S3) buckets
- Amazon EFS file systems
- Amazon FSx for Windows File Server file systems
- Amazon FSx for Lustre file systems
- Amazon FSx for OpenZFS file systems
- Amazon FSx for NetApp ONTAP file systems
- AWS Snowcone devices

The follow shows the DolphinScheduler DataSync task plugin features:

- Create an AWS DataSync task and execute, continuously get the execution status until the task completes.

## Create Task

- Click `Project -> Management-Project -> Name-Workflow Definition`, and click the "Create Workflow" button to enter the
  DAG editing page.
- Drag from the toolbar <img src="../../../../img/tasks/icons/datasync.png" width="15"/> task node to canvas.

## Task Example

First, introduce some general parameters of DolphinScheduler:

- **Node name**: The name of the task. Node names within the same workflow must be unique.
- **Run flag**: Indicating whether to schedule the task. If you do not need to execute the task, you can turn on the `Prohibition execution` switch.
- **Description**: Describing the function of this node.
- **Task priority**: When the number of the worker threads is insufficient, the worker executes task according to the priority. When two tasks have the same priority, the worker will execute them in `first come first served` fashion.
- **Worker group**: Machines which execute the tasks. If you choose `default`, scheduler will send the task to a random worker.
- **Task group name**: Resource group of tasks. It will not take effect if not configured.
- **Environment name**: Environment to execute the task.
- **Number of failed retries**: The number of task retries for failures. You could select it by drop-down menu or fill it manually.
- **Failure retry interval**: Interval of task retries for failures. You could select it by drop-down menu or fill it manually.
- **CPU quota**: Assign the specified CPU time quota to the task executed. Takes a percentage value. Default -1 means unlimited. For example, the full CPU load of one core is 100%, and that of 16 cores is 1600%. You could configure it by [task.resource.limit.state](../../architecture/configuration.md).
- **Max memory**: Assign the specified max memory to the task executed. Exceeding this limit will trigger oom to be killed and will not automatically retry. Takes an MB value. Default -1 means unlimited. You could configure it by [task.resource.limit.state](../../architecture/configuration.md).
- **Timeout alarm**: Alarm for task timeout. When the task exceeds the "timeout threshold", an alarm email will send.
- **Delayed execution time**: The time that a task delays for execution in minutes.
- **Resources**: 	Resources which your task node uses.
- **Predecessor task**: 	The upstream task of the current task node.

Here are some specific parameters for the DataSync plugin:

- **name**: Task name
- **destinationLocationArn**: The Amazon Resource Name (ARN) of an AWS storage resource's location. Visit [AWS API](https://docs.aws.amazon.com/datasync/latest/userguide/API_CreateTask.html#DataSync-CreateTask-request-DestinationLocationArn)
- **sourceLocationArn**: The Amazon Resource Name (ARN) of the source location for the task. Visit [AWS API](https://docs.aws.amazon.com/datasync/latest/userguide/API_CreateTask.html#DataSync-CreateTask-request-SourceLocationArn)
- **cloudWatchLogGroupArn**: The Amazon Resource Name (ARN) of the Amazon CloudWatch log group that is used to monitor and log events in the task. Visit [AWS API](https://docs.aws.amazon.com/datasync/latest/userguide/API_CreateTask.html#DataSync-CreateTask-request-CloudWatchLogGroupArn)

  or

- **json**: JSON parameters to construct datasync task, and supports parameters like options, etc. Visit: [AWS CreateTask API] 的 Request Syntax (https://docs.aws.amazon.com/datasync/latest/userguide/API_CreateTask.html)

- The following shows the task plugin example:

![datasync](../../../../img/tasks/demo/datasync_task02.png)

## Environment to prepare

The task requires AWS configurations, modify the value in file `common.properties`

```yaml
# Defines AWS access key and is required
resource.aws.access.key.id=<YOUR AWS ACCESS KEY>
# Defines AWS secret access key and is required
resource.aws.secret.access.key=<YOUR AWS SECRET KEY>
# Defines  AWS Region to use and is required
resource.aws.region=<AWS REGION>
```