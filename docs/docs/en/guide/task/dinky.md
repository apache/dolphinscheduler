# Dinky

## Overview

Use `Dinky Task` to create a dinky-type task and support one-stop development, debugging, operation and maintenance of FlinkSql, Flink jar and SQL. When the worker executes `Dinky Task`,
it will call `Dinky API` to trigger dinky task. Click [here](http://www.dlink.top/) for details about `Dinky`.

## Create Task

- Click Project Management-Project Name-Workflow Definition, and click the "Create Workflow" button to enter the DAG editing page.
- Drag <img src="../../../../img/tasks/icons/dinky.png" width="15"/> from the toolbar to the canvas.

## Task Parameter

| **Parameter** | **Description** |
| ------- | ---------- |
| Node Name | Set the name of the task. Node names within a workflow definition are unique. |
| Run flag | Indicates whether the node can be scheduled normally. If it is not necessary to execute, you can turn on the prohibiting execution switch. |
| Description | Describes the function of this node. |
| Task priority | When the number of worker threads is insufficient, they are executed in order from high to low according to the priority, and they are executed according to the first-in, first-out principle when the priority is the same. |
| Worker group | The task is assigned to the machines in the worker group for execution. If Default is selected, a worker machine will be randomly selected for execution. |
| Task group name | The group in Resources, if not configured, it will not be used. | 
| Environment Name | Configure the environment in which to run the script. |
| Number of failed retries | The number of times the task is resubmitted after failure. It supports drop-down and manual filling. | 
| Failure Retry Interval | The time interval for resubmitting the task if the task fails. It supports drop-down and manual filling. | 
| Timeout alarm | Check Timeout Alarm and Timeout Failure. When the task exceeds the "timeout duration", an alarm email will be sent and the task execution will fail. |
| Dinky Address | The url for a dinky server. |
| Dinky Task ID | The unique task id for a dinky task. |
| Online Task | Specify whether the current dinky job is online. If yes, the submitted job can only be submitted successfully when it is published and there is no corresponding Flink job instance running. |

## Task Example

### Dinky Task Example

This example illustrates how to create a dinky task node.

![demo-dinky](../../../../img/tasks/demo/dinky.png)

![demo-get-dinky-task-id](../../../../img/tasks/demo/dinky_task_id.png)

