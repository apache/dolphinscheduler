# Shell

## Overview

Shell task used to create a shell task type and execute a series of shell scripts. When the worker run the shell task, a temporary shell script is generated, and use the Linux user with the same name as the tenant executes the script.

## Create Task

- Click `Project Management -> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag  from the toolbar <img src="/img/tasks/icons/shell.png" width="15"/> to the canvas.

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
| Script | A SHELL program developed by the user. |
| Resource | Refers to the list of resource files that need to be called in the script, and the files uploaded or created in Resource Center - File Management.| 
| User-defined parameter | It is a user-defined parameter of Shell, which will replace the content with `${variable}` in the script. |
| Predecessor task | Selecting the predecessor task of the current task will set the selected predecessor task as the upstream of the current task. |

## Task Example

### Simply Print

We make an example simulate from a common task which runs by one command. The example is to print one line in the log file, as shown in the following figure:
"This is a demo of shell task".

![demo-shell-simple](/img/tasks/demo/shell.jpg)

### Custom Parameters

This example simulates a custom parameter task. We use parameters for reusing existing tasks as template or coping with the dynamic task. In this case,
we declare a custom parameter named "param_key", with the value "param_val". Then we use `echo` to print the parameter "${param_key}" we just declared. 
After running this example, we would see "param_val" print in the log.

![demo-shell-custom-param](/img/tasks/demo/shell_custom_param.jpg)

## Attention
The shell task type resolves whether the task log contains ```application_xxx_xxx``` to determine whether is the yarn task. If so, the corresponding application
will be use to judge the running state of the current shell node. At this time, if stops the operation of the workflow, the corresponding ```application_id```
will be killed.
