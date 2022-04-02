# Python Node

## Overview

Use `Python Task` to create a python-type task and execute python scripts. When the worker executes `Python Task`,
it will generate a temporary python script, and executes the script by the Linux user with the same name as the tenant.

## Create Task

- Click Project Management-Project Name-Workflow Definition, and click the "Create Workflow" button to enter the DAG editing page.
- Drag <img src="/img/tasks/icons/python.png" width="15"/> from the toolbar to the canvas.

## Task Parameter

- Node name: The node name in a workflow definition is unique.
- Run flag: Identifies whether this node can be scheduled normally, if it does not need to be executed, you can turn on the prohibition switch.
- Descriptive information: Describe the function of the node.
- Task priority: When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.
- Worker grouping: Assign tasks to the machines of the worker group to execute. If `Default` is selected, randomly select a worker machine for execution.
- Environment Name: Configure the environment name in which to run the script.
- Number of failed retry attempts: The failure task resubmitting times. It supports drop-down and hand-filling.
- Failed retry interval: The time interval for resubmitting the task after a failed task. It supports drop-down and hand-filling.
- Timeout alarm: Check the timeout alarm and timeout failure. When the task exceeds the "timeout period", an alarm email will send and the task execution will fail.
- Script: Python program developed by the user.
- Resource: Refers to the list of resource files that need to be called in the script, and the files uploaded or created by the resource center-file management.
- Custom parameters: It is the user-defined parameters of Python, which will replace the content with \${variable} in the script.

## Task Example

### Simply Print

This example simulates a common task that runs by a simple command. The example is to print one line in the log file, as shown in the following figure:
"This is a demo of python task".

![demo-python-simple](/img/tasks/demo/python_ui_next.jpg)

```python
print("This is a demo of python task")
```

### Custom Parameters

This example simulates a custom parameter task. We use parameters for reusing existing tasks as template or coping with the dynamic task. In this case,
we declare a custom parameter named "param_key", with the value "param_val". Then we use echo to print the parameter "${param_key}" we just declared.
After running this example, we would see "param_val" print in the log.

![demo-python-custom-param](/img/tasks/demo/python_custom_param_ui_next.jpg)

```python
print("${param_key}")
```

## Notice

None
