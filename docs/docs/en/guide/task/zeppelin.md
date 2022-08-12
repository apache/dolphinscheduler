# Apache Zeppelin

## Overview

Use `Zeppelin Task` to create a zeppelin-type task and execute zeppelin notebook paragraphs. When the worker executes `Zeppelin Task`,
it will call `Zeppelin Client API` to trigger zeppelin notebook paragraph. Click [here](https://zeppelin.apache.org/) for details about `Apache Zeppelin Notebook`. 

## Create Task

- Click `Project Management -> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag <img src="../../../../img/tasks/icons/zeppelin.png" width="15"/> from the toolbar to the canvas.

## Task Parameters

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
| Zeppelin Note ID | The unique note id for a zeppelin notebook note. |
| Zeppelin Paragraph ID | The unique paragraph id for a zeppelin notebook paragraph. If you want to schedule a whole note at a time, leave this field blank. |
| Zeppelin Production Note Directory | The directory for cloned note in production mode. |
| Zeppelin Rest Endpoint | The REST endpoint of your zeppelin server |
| Zeppelin Parameters | Parameters in json format used for zeppelin dynamic form. |

## Production (Clone) Mode

- Fill in the optional `Zeppelin Production Note Directory` parameter to enable `Production Mode`.
- In `Production Mode`, the target note gets copied to the `Zeppelin Production Note Directory` you choose. 
`Zeppelin Task Plugin` will execute the cloned note instead of the original one. Once execution done, 
`Zeppelin Task Plugin` will delete the cloned note automatically. 
Therefore, it increases the stability as the modification to a running note triggered by `Dolphin Scheduler` 
will not affect the production task.
- If you leave the `Zeppelin Production Note Directory` empty, `Zeppelin Task Plugin` will execute the original note.
- 'Zeppelin Production Note Directory' should both start and end with a `slash`. e.g. `/production_note_directory/`  

## Task Example

### Zeppelin Paragraph Task Example

This example illustrates how to create a zeppelin paragraph task node.

![demo-zeppelin-paragraph](../../../../img/tasks/demo/zeppelin.png)

![demo-get-zeppelin-id](../../../../img/tasks/demo/zeppelin_id.png)

