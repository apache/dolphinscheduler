# Sub-Process Node
Overview
--------

The sub-process node is to execute an external workflow definition as a task node.

Create Task
-----------

*   Click `Project Management-> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
*   Drag from the toolbar <img src="/img/tasks/icons/sub_process.png" width="15"/> task node to canvas to create a new `SUB_PROCESS` task.

Task Parameter
--------------

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Node name</td><td class="confluenceTd">Unique name of node in workflow definition.</td></tr><tr><td class="confluenceTd">Run flag</td><td class="confluenceTd">Identifies whether this node schedules normally.</td></tr><tr><td class="confluenceTd">Description</td><td class="confluenceTd">Describe the function of the node.</td></tr><tr><td colspan="1" class="confluenceTd">Task priority</td><td colspan="1" class="confluenceTd">When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.</td></tr><tr><td colspan="1" class="confluenceTd">Worker group</td><td colspan="1" class="confluenceTd"><p>Assign tasks to the machines of the worker group to execute. If<span>&nbsp;</span><code>Default</code><span>&nbsp;</span>is selected, randomly select a worker machine for execution.</p></td></tr><tr><td colspan="1" class="confluenceTd">Task group name</td><td colspan="1" class="confluenceTd">The group in Resources, if not configured, it will not be used.</td></tr><tr><td colspan="1" class="confluenceTd">Environment Name</td><td colspan="1" class="confluenceTd">Configure the environment name in which run the script.</td></tr><tr><td colspan="1" class="confluenceTd">Timeout alarm</td><td colspan="1" class="confluenceTd">Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm email will send and the task execution will fail.</td></tr><tr><td colspan="1" class="confluenceTd">Child node</td><td colspan="1" class="confluenceTd">It is the workflow definition of the selected sub-process. Enter the child node in the upper right corner to jump to the workflow definition of the selected sub-process.</td></tr><tr><td colspan="1" class="confluenceTd">Pre task</td><td colspan="1" class="confluenceTd">Selecting a predecessor task for the current task, will set the selected predecessor task as upstream of the current task.</td></tr></tbody></table>

Task Example
------------

This example simulates a common task type, here we use a child node task to recall the [Shell](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/task/shell.html) to print out "hello". The shell task acts as a child node.

### Create a Shell task

Create a shell task to print "hello" and define the workflow as `test_dag01`.

![subprocess_task01](/img/tasks/demo/subprocess_task01.png)

### Create the Sub\_process task

To use the sub\_process, you need to create the sub-node task, which is the shell task we created in the first step. After that, as shown in the diagram below, select the corresponding sub-node in position ⑤.

![subprocess_task02](/img/tasks/demo/subprocess_task02.png)

After creating the sub\_process, create a corresponding shell task for printing "world" and link both together. Save the current workflow and run it to get the expected result.

![subprocess_task03](/img/tasks/demo/subprocess_task03.png)

Note
----

When using `sub_process` to recall a sub-node task, you need to ensure that the defined sub-node is online status, otherwise, the sub\_process workflow will not work properly.