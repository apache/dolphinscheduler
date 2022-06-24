# Switch
The switch is a conditional judgment node, decide the branch executes according to the value of [global variable](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/parameter/global.html) and the expression result written by the user. **Note** Execute expressions using javax.script.ScriptEngine.eval.

Create Task
-----------

Click Project Management -> Project Name -> Workflow Definition, and click the Create Workflow button to enter the DAG editing page. Drag from the toolbar <img src="/img/tasks/icons/switch.png" width="15"/> task node to canvas to create a task.

**Note**: After created a switch task, you must first configure the upstream and downstream, then configure the parameter of task branches.

Task Parameters
---------------

<table class="relative-table wrapped confluenceTable" style="width: 100.0%;"><colgroup><col style="width: 12.8234%;"><col style="width: 87.1905%;"></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Node Name</td><td class="confluenceTd"><p>Set the name of the task.<span>&nbsp;</span>Node names within a workflow definition are unique.</p></td></tr><tr><td class="confluenceTd">Run flag</td><td class="confluenceTd">Indicates whether the node can be scheduled normally. If it is not necessary to execute, you can turn on the prohibiting execution switch.</td></tr><tr><td class="confluenceTd">Description</td><td class="confluenceTd">Describes the function of this node.</td></tr><tr><td colspan="1" class="confluenceTd">Task priority</td><td colspan="1" class="confluenceTd">When the number of worker threads is insufficient, they are executed in order from high to low according to the priority, and they are executed according to the first-in, first-out principle when the priority is the same.</td></tr><tr><td colspan="1" class="confluenceTd">Worker group</td><td colspan="1" class="confluenceTd">The task is assigned to the machines in the worker group for execution. If Default is selected, a worker machine will be randomly selected for execution.</td></tr><tr><td colspan="1" class="confluenceTd">Task group name</td><td colspan="1" class="confluenceTd">The group in Resources, if not configured, it will not be used.</td></tr><tr><td colspan="1" class="confluenceTd">Environment Name</td><td colspan="1" class="confluenceTd">Configure the environment in which to run the script.</td></tr><tr><td colspan="1" class="confluenceTd">Number of failed retries</td><td colspan="1" class="confluenceTd">The number of times the task is resubmitted after failure. It supports drop-down and manual filling.</td></tr><tr><td colspan="1" class="confluenceTd">Failure Retry Interval</td><td colspan="1" class="confluenceTd">The time interval for resubmitting the task if the task fails. It supports drop-down and manual filling.</td></tr><tr><td colspan="1" class="confluenceTd">Delay execution time</td><td colspan="1" class="confluenceTd">Task delay execution time.</td></tr><tr><td colspan="1" class="confluenceTd">Timeout alarm</td><td colspan="1" class="confluenceTd">Check Timeout Alarm and Timeout Failure. When the task exceeds the "timeout duration", an alarm email will be sent and the task execution will fail.</td></tr><tr><td colspan="1" class="confluenceTd">Condition</td><td colspan="1" class="confluenceTd">You can configure multiple conditions for the switch task. When the conditions are satisfied, execute the configured branch. You can configure multiple different conditions to satisfy different businesses.</td></tr><tr><td colspan="1" class="confluenceTd">Branch flow</td><td colspan="1" class="confluenceTd">The default branch flow, when all the conditions are not satisfied, execute this branch flow.</td></tr></tbody></table>

Task Example
------------

This is demonstrated using one switch task and three shell tasks.

### Create a workflow

Create a new switch task, and three shell tasks downstream. The shell task is not required. The switch task needs to be connected with the downstream task to configure the relationship before the downstream task can be selected.

![switch_01](/img/tasks/demo/switch_01.png)

### Set conditions

Configure the conditions and default branches. If the conditions are met, the specified branch will be taken. If the conditions are not met, the default branch will be taken. In the figure, if the value of the variable is "A", the branch taskA is executed, if the value of the variable is "B", the branch taskB is executed, and default is executed if both are not satisfied.

![switch_02](/img/tasks/demo/switch_02.png)

Conditions use global variables, please refer to [Global Parameter](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/parameter/global.html). The value of the global variable configured here is A.

![switch_03](/img/tasks/demo/switch_03.png)

If executed correctly, then taskA will be executed correctly.

### Execute

Execute and see if it works as expected. It can be seen that the specified downstream taskA are executed as expected.

![switch_04](/img/tasks/demo/switch_04.png)