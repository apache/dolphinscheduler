# Shell
Overview
--------

Shell task type, used to create a shell type task and execute a series of shell scripts. When the worker executes this task, a temporary shell script is generated and executed using the linux user with the same name as the tenant.

Create Task
-----------

*   Click `Project Management -> Project Name -> Workflow Definition`, click the "`Create Workflow`" button to enter the DAG editing page.
*   Drag `SHELL` task from the toolbar <img src="/img/tasks/icons/shell.png" width="15"/> to the artboard to complete the creation.

Task Parameters
---------------

<table class="relative-table wrapped confluenceTable" style="width: 100.0%;"><colgroup><col style="width: 12.8234%;"><col style="width: 87.1905%;"></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Node Name</td><td class="confluenceTd"><p>Set the name of the task.<span>&nbsp;</span>Node names within a workflow definition are unique.</p></td></tr><tr><td class="confluenceTd">Run flag</td><td class="confluenceTd">Indicates whether the node can be scheduled normally. If it is not necessary to execute, you can turn on the prohibiting execution switch.</td></tr><tr><td class="confluenceTd">Description</td><td class="confluenceTd">Describes the function of this node.</td></tr><tr><td colspan="1" class="confluenceTd">Task priority</td><td colspan="1" class="confluenceTd">When the number of worker threads is insufficient, they are executed in order from high to low according to the priority, and they are executed according to the first-in, first-out principle when the priority is the same.</td></tr><tr><td colspan="1" class="confluenceTd">Worker group</td><td colspan="1" class="confluenceTd">The task is assigned to the machines in the worker group for execution. If Default is selected, a worker machine will be randomly selected for execution.</td></tr><tr><td colspan="1" class="confluenceTd">Task group name</td><td colspan="1" class="confluenceTd">The group in Resources, if not configured, it will not be used.</td></tr><tr><td colspan="1" class="confluenceTd">Environment Name</td><td colspan="1" class="confluenceTd">Configure the environment in which to run the script.</td></tr><tr><td colspan="1" class="confluenceTd">Number of failed retries</td><td colspan="1" class="confluenceTd">The number of times the task is resubmitted after failure. It supports drop-down and manual filling.</td></tr><tr><td colspan="1" class="confluenceTd">Failure Retry Interval</td><td colspan="1" class="confluenceTd">The time interval for resubmitting the task if the task fails. It supports drop-down and manual filling.</td></tr><tr><td colspan="1" class="confluenceTd">Timeout alarm</td><td colspan="1" class="confluenceTd">Check Timeout Alarm and Timeout Failure. When the task exceeds the "timeout duration", an alarm email will be sent and the task execution will fail.</td></tr><tr><td colspan="1" class="confluenceTd">Script</td><td colspan="1" class="confluenceTd">A SHELL program developed by the user.</td></tr><tr><td colspan="1" class="confluenceTd">Resource</td><td colspan="1" class="confluenceTd">Refers to the list of resource files that need to be called in the script, and the files uploaded or created in Resource Center - File Management.</td></tr><tr><td colspan="1" class="confluenceTd">User-defined parameter</td><td colspan="1" class="confluenceTd"><p>It is a user-defined parameter of Shell, which will replace<span>&nbsp;</span><code>${variable}</code>&nbsp;in the script.</p></td></tr><tr><td colspan="1" class="confluenceTd">Predecessor task</td><td colspan="1" class="confluenceTd">Selecting the predecessor task of the current task will set the selected predecessor task as the upstream of the current task.</td></tr></tbody></table>

Task Example
------------

### Print a Line 

This example shows how to simulate simple tasks with just one or more simple lines of command. In this example, we will see how to print a line in a log file.

![demo-shell-simple](/img/tasks/demo/shell.jpg)

### Use custom parameters

This example simulates a custom parameter task. In order to reuse existing tasks more conveniently, or when faced with dynamic requirements, we will use variables to ensure the reusability of scripts. In this example, we first define the parameter "param\_key" in the custom script and set its value to "param\_val". Then the echo command is declared in the "script", and the parameter "param\_key" is printed out. When we save and run the task, we will see in the log that the value "param\_val" corresponding to the parameter "param\_key" is printed out.

![demo-shell-custom-param](/img/tasks/demo/shell_custom_param.jpg)

Note
----

The shell task type resolves whether the task log contains `application_xxx_xxx` to determine whether is the yarn task. If so, the corresponding application will be use to judge the running state of the current shell node. At this time, if stops the operation of the workflow, the corresponding `application_id` will be killed.

If you want to use resource files in Shell tasks, you can upload corresponding files through the resource center and then use the resources in the Shell task. Reference: File Management.