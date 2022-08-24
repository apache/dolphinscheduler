# Python Node

## Overview

Use `Python Task` to create a python-type task and execute python scripts. When the worker executes `Python Task`,
it will generate a temporary python script, and executes the script by the Linux user with the same name as the tenant.

## Create Task

- Click `Project Management -> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag <img src="../../../../img/tasks/icons/python.png" width="15"/> from the toolbar to the canvas.

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
| Cpu quota | Assign the specified CPU time quota to the task executed. Takes a percentage value. Default -1 means unlimited. For example, the full CPU load of one core is 100%,and that of 16 cores is 1600%. This function is controlled by [task.resource.limit.state](../../architecture/configuration.md). |
| Max memory | Assign the specified max memory to the task executed. Exceeding this limit will trigger oom to be killed and will not automatically retry. Takes an MB value. Default -1 means unlimited. This function is controlled by [task.resource.limit.state](../../architecture/configuration.md). |
| Timeout alarm | Check the timeout alarm and timeout failure. When the task exceeds the "timeout period", an alarm email will send and the task execution will fail. |
| Script | Python program developed by the user. |
| Resource | Refers to the list of resource files that need to be called in the script, and the files uploaded or created by the resource center-file management. |
| Custom parameters | It is the user-defined parameters of Python, which will replace the content with \${variable} in the script. |

## Task Example

### Simply Print

This example simulates a common task that runs by a simple command. The example is to print one line in the log file, as shown in the following figure:
"This is a demo of python task".

![demo-python-simple](../../../../img/tasks/demo/python_ui_next.jpg)

```python
print("This is a demo of python task")
```

### Custom Parameters

This example simulates a custom parameter task. We use parameters for reusing existing tasks as template or coping with the dynamic task. In this case,
we declare a custom parameter named "param_key", with the value "param_val". Then we use echo to print the parameter "${param_key}" we just declared.
After running this example, we would see "param_val" print in the log.

![demo-python-custom-param](../../../../img/tasks/demo/python_custom_param_ui_next.jpg)

```python
print("${param_key}")
```

