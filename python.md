# Python Node
Overview
--------

Use `Python Task` to create a python-type task and execute python scripts. When the worker executes `Python Task`, it will generate a temporary python script, and executes the script by the Linux user with the same name as the tenant.

Create Task
-----------

*   Click `Project Management->Project Name->Workflow Definition`, and click the "`Create Workflow`" button to enter the DAG editing page.
*   Drag <img src="/img/tasks/icons/python.png" width="15"/> from the toolbar to the canvas.

Task Parameter
--------------

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td colspan="1" class="confluenceTd">Node Name</td><td colspan="1" class="confluenceTd">The node name in a workflow definition is unique.</td></tr><tr><td colspan="1" class="confluenceTd">Run flag</td><td colspan="1" class="confluenceTd">Identifies whether this node can be scheduled normally, if it does not need to be executed, you can turn on the prohibition switch.</td></tr><tr><td colspan="1" class="confluenceTd">Description</td><td colspan="1" class="confluenceTd">Describe the function of the node.</td></tr><tr><td colspan="1" class="confluenceTd">Task priority</td><td colspan="1" class="confluenceTd">When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.</td></tr><tr><td colspan="1" class="confluenceTd">Worker group</td><td colspan="1" class="confluenceTd"><p>Assign tasks to the machines of the worker group to execute. If<span>&nbsp;</span><code>Default</code><span>&nbsp;</span>is selected, randomly select a worker machine for execution.</p></td></tr><tr><td colspan="1" class="confluenceTd">Environment Name</td><td colspan="1" class="confluenceTd">Configure the environment name in which to run the script.</td></tr><tr><td colspan="1" class="confluenceTd">Number of failed retries</td><td colspan="1" class="confluenceTd">The failure task resubmitting times. It supports drop-down and hand-filling.</td></tr><tr><td colspan="1" class="confluenceTd">Failed retry interval</td><td colspan="1" class="confluenceTd">The time interval for resubmitting the task after a failed task. It supports drop-down and hand-filling.</td></tr><tr><td colspan="1" class="confluenceTd">Timeout alarm</td><td colspan="1" class="confluenceTd">Check the timeout alarm and timeout failure. When the task exceeds the "timeout period", an alarm email will send and the task execution will fail.</td></tr><tr><td colspan="1" class="confluenceTd">Script</td><td colspan="1" class="confluenceTd">Python program developed by the user.</td></tr><tr><td class="confluenceTd">Resource</td><td class="confluenceTd">Refers to the list of resource files that need to be called in the script, and the files uploaded or created by the resource center-file management.</td></tr><tr><td class="confluenceTd">Custom parameters</td><td class="confluenceTd">It is the user-defined parameters of Python, which will replace the content with ${variable} in the script.</td></tr></tbody></table>

  

Task Example
------------

### Simple Print Task

This example simulates a common task that runs by a simple command. The example is to print one line in the log file, as shown in the following figure: "This is a demo of python task".

![demo-python-simple](/img/tasks/demo/python_ui_next.jpg)

```python
print("This is a demo of python task")
```

### Custom Parameters

This example simulates a custom parameter task. We use parameters for reusing existing tasks as template or coping with the dynamic task. In this case, we declare a custom parameter named "param\_key", with the value "param\_val". Then we use echo to print the parameter "${param\_key}" we just declared. After running this example, we would see "param\_val" print in the log.

![demo-python-custom-param](/img/tasks/demo/python_custom_param_ui_next.jpg)

```python
print("${param_key}")
```