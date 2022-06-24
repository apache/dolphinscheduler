# Conditions

Condition is a conditional node, that determines which downstream task should run based on the condition of the upstream task. Currently, the `CONDITIONS` support multiple upstream tasks, but only two downstream tasks. When the number of upstream tasks exceeds one, achieve complex upstream dependencies by through `and` and `or` operators.

Create Task
-----------

*   Click `Project Management -> Project Name -> Workflow Definition`, and click the "`Create Workflow`" button to enter the DAG editing page.
*   Drag from the toolbar <img src="/img/conditions.png" width="20"/> task node to canvas.

Task Parameters
---------------

<table class="relative-table wrapped confluenceTable" style="width: 100.0%;"><colgroup><col style="width: 12.8234%;"><col style="width: 87.1905%;"></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Node Name</td><td class="confluenceTd"><p>Set the name of the task.<span>&nbsp;</span>Node names within a workflow definition are unique.</p></td></tr><tr><td class="confluenceTd">Run flag</td><td class="confluenceTd">Indicates whether the node can be scheduled normally. If it is not necessary to execute, you can turn on the prohibiting execution switch.</td></tr><tr><td class="confluenceTd">Description</td><td class="confluenceTd">Describes the function of this node.</td></tr><tr><td colspan="1" class="confluenceTd">Task priority</td><td colspan="1" class="confluenceTd">When the number of worker threads is insufficient, they are executed in order from high to low according to the priority, and they are executed according to the first-in, first-out principle when the priority is the same.</td></tr><tr><td colspan="1" class="confluenceTd">Worker group</td><td colspan="1" class="confluenceTd">The task is assigned to the machines in the worker group for execution. If Default is selected, a worker machine will be randomly selected for execution.</td></tr><tr><td colspan="1" class="confluenceTd">Task group name</td><td colspan="1" class="confluenceTd">The group in Resources, if not configured, it will not be used.</td></tr><tr><td colspan="1" class="confluenceTd">Environment Name</td><td colspan="1" class="confluenceTd">Configure the environment in which to run the script.</td></tr><tr><td colspan="1" class="confluenceTd">Number of failed retries</td><td colspan="1" class="confluenceTd">The number of times the task is resubmitted after failure. It supports drop-down and manual filling.</td></tr><tr><td colspan="1" class="confluenceTd">Failure Retry Interval</td><td colspan="1" class="confluenceTd">The time interval for resubmitting the task if the task fails. It supports drop-down and manual filling.</td></tr><tr><td colspan="1" class="confluenceTd">Timeout alarm</td><td colspan="1" class="confluenceTd">Check Timeout Alarm and Timeout Failure. When the task exceeds the "timeout duration", an alarm email will be sent and the task execution will fail.</td></tr><tr><td colspan="1" class="confluenceTd">Downstream tasks selection</td><td colspan="1" class="confluenceTd"><p>Depending on the status of the predecessor task, you can jump to the corresponding branch, currently two branches are supported: success, failure</p><ul><li style="list-style-type: none;"><ul><li>Success: When the upstream task runs successfully, run the success branch.</li><li>Failure: When the upstream task runs failed, run the failure branch.</li></ul></li></ul></td></tr><tr><td colspan="1" class="confluenceTd">Upstream condition selection</td><td colspan="1" class="confluenceTd"><p>Can select one or more upstream tasks for conditions.</p><ul><li style="list-style-type: none;"><ul><li>Add an upstream dependency: the first parameter is to choose a specified task name, and the second parameter is to choose the upstream task status to trigger conditions.</li><li>Select upstream task relationship: use<span>&nbsp;</span><code>and</code><span>&nbsp;</span>and<span>&nbsp;</span><code>or</code><span>&nbsp;</span>operators to handle the complex relationship of upstream when there are multiple upstream tasks for conditions.</li></ul></li></ul></td></tr></tbody></table>

Related Task
------------

[switch](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/task/switch.html): Conditions task mainly executes the corresponding branch based on the execution status (success, failure) of the upstream nodes. The [Switch](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/task/switch.html) task node mainly executes the corresponding branch based on the value of the [global parameter](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/parameter/global.html) and the result of user written expression.

Example
-------

This sample demonstrates the operation of the Condition task by using the [Shell](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/task/shell.html) task.

### Create workflow

Go to the workflow definition page, and then create the following task nodes:

*   Node\_A: Shell task, prints out "hello world", its main function is the upstream branch of Condition, and triggers the corresponding branch node according to whether its execution is successful or not.
*   Condition: The Conditions task executes the corresponding branch according to the execution status of the upstream task.
*   Node\_Success: Shell task, print out "success", Node\_A executes the successful branch.
*   Node\_False: Shell task, print out "false", Node\_A executes the failed branch.

![condition_task01](/img/tasks/demo/condition_task01.png)

### View the execution result

After you finish creating the workflow, you can run the workflow online. You can view the execution status of each task on the workflow instance page. As shown below:

![condition_task02](/img/tasks/demo/condition_task02.png)

In the above figure, the task status marked with a green check mark is the successfully executed task node.

Note
----

*   The Conditions task supports multiple upstream tasks, but only two downstream tasks.
*   The Conditions task and the workflow that contain it do not support copy operations.
*   The predecessor task of Conditions cannot connect to its branch nodes, which will cause logical confusion and does not conform to DAG scheduling. The situation shown below is **wrong**.

![condition_task03](/img/tasks/demo/condition_task03.png) 

![condition_task04](/img/tasks/demo/condition_task04.png)