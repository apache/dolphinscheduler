# Dependent

Overview
--------

Dependent nodes are **dependency check nodes**. For example, process A depends on the successful execution of process B from yesterday, and the dependent node will check whether process B run successful yesterday.

Create Task
-----------

*   Click `Project Management -> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
*   Drag `DEPENDENT` task from the toolbar <img src="/img/tasks/icons/dependent.png" width="15"/> task node to canvas.

Task Parameter
--------------

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Node name</td><td class="confluenceTd">Unique name of node in workflow definition.</td></tr><tr><td class="confluenceTd">Run flag</td><td class="confluenceTd">Identifies whether this node schedules normally.</td></tr><tr><td class="confluenceTd">Description</td><td class="confluenceTd">Describe the function of the node.</td></tr><tr><td colspan="1" class="confluenceTd">Task priority</td><td colspan="1" class="confluenceTd">When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.</td></tr><tr><td colspan="1" class="confluenceTd">Worker group</td><td colspan="1" class="confluenceTd"><p>Assign tasks to the machines of the worker group to execute. If<span>&nbsp;</span><code>Default</code><span>&nbsp;</span>is selected, randomly select a worker machine for execution.</p></td></tr><tr><td colspan="1" class="confluenceTd">Task group name</td><td colspan="1" class="confluenceTd">The group in Resources, if not configured, it will not be used.</td></tr><tr><td colspan="1" class="confluenceTd">Environment Name</td><td colspan="1" class="confluenceTd">Configure the environment name in which run the script.</td></tr><tr><td colspan="1" class="confluenceTd">Number of failed retries</td><td colspan="1" class="confluenceTd">The number of times the task failed to resubmit.</td></tr><tr><td colspan="1" class="confluenceTd">Failed retry interval</td><td colspan="1" class="confluenceTd">The time interval (unit minute) for resubmitting the task after a failed task.</td></tr><tr><td colspan="1" class="confluenceTd">Delayed execution time</td><td colspan="1" class="confluenceTd">The time (unit minute) that a task delays in execution.</td></tr><tr><td colspan="1" class="confluenceTd">Pre task</td><td colspan="1" class="confluenceTd">Selecting a predecessor task for the current task, will set the selected predecessor task as upstream of the current task.</td></tr></tbody></table>

Task Examples
-------------

The Dependent node provides a logical judgment function, which can detect the execution of the dependent node according to the logic.

For example, process A is a weekly task, processes B and C are daily tasks, and task A requires tasks B and C to be successfully executed every day of the last week.

![dependent_task01](/img/tasks/demo/dependent_task01.png)

And another example is that process A is a weekly report task, processes B and C are daily tasks, and task A requires tasks B or C to be successfully executed every day of the last week:

![dependent_task02](/img/tasks/demo/dependent_task02.png)

If the weekly report A also needs to be executed successfully last Tuesday:

![dependent_task03](/img/tasks/demo/dependent_task03.png)