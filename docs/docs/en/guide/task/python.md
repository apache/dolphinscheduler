# Python Node

## Overview

Use `Python Task` to create a python-type task and execute python scripts. When the worker executes `Python Task`,
it will generate a temporary python script, and executes the script by the Linux user with the same name as the tenant.

## Create Task

- Click `Project Management -> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag <img src="../../../../img/tasks/icons/python.png" width="15"/> from the toolbar to the canvas.

## Task Parameter

[//]: # (TODO: use the commented anchor below once our website template supports this syntax)
[//]: # (- Please refer to [DolphinScheduler Task Parameters Appendix]&#40;appendix.md#default-task-parameters&#41; `Default Task Parameters` section for default parameters.)

- Please refer to [DolphinScheduler Task Parameters Appendix](appendix.md) `Default Task Parameters` section for default parameters.

|   **Parameter**   |                                               **Description**                                                |
|-------------------|--------------------------------------------------------------------------------------------------------------|
| Script            | Python program developed by the user.                                                                        |
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

