# Apache Zeppelin

Use `Zeppelin Task` to create a zeppelin-type task and execute zeppelin notebook paragraphs. When the worker executes `Zeppelin Task`, it will call `Zeppelin Client API` to trigger zeppelin notebook paragraph. Click [here](https://zeppelin.apache.org/) for details about `Apache Zeppelin Notebook`.

Create Task
-----------

*   Click `Project Management->Project Name->Workflow Definition`, and click the "`Create Workflow`" button to enter the DAG editing page.
*   Drag <img src="/img/tasks/icons/zeppelin.png" width="15"/> from the toolbar to the canvas.

Task Parameters
---------------

<table class="relative-table wrapped confluenceTable" style="width: 100.0%;"><colgroup><col style="width: 12.8234%;"><col style="width: 87.1905%;"></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Node Name</td><td class="confluenceTd"><p>Set the name of the task.<span>&nbsp;</span>Node names within a workflow definition are unique.</p></td></tr><tr><td class="confluenceTd">Run flag</td><td class="confluenceTd">Indicates whether the node can be scheduled normally. If it is not necessary to execute, you can turn on the prohibiting execution switch.</td></tr><tr><td class="confluenceTd">Description</td><td class="confluenceTd">Describes the function of this node.</td></tr><tr><td colspan="1" class="confluenceTd">Task priority</td><td colspan="1" class="confluenceTd">When the number of worker threads is insufficient, they are executed in order from high to low according to the priority, and they are executed according to the first-in, first-out principle when the priority is the same.</td></tr><tr><td colspan="1" class="confluenceTd">Worker group</td><td colspan="1" class="confluenceTd">The task is assigned to the machines in the worker group for execution. If Default is selected, a worker machine will be randomly selected for execution.</td></tr><tr><td colspan="1" class="confluenceTd">Task group name</td><td colspan="1" class="confluenceTd">The group in Resources, if not configured, it will not be used.</span></td></tr><tr><td colspan="1" class="confluenceTd">Environment Name</td><td colspan="1" class="confluenceTd">Configure the environment in which to run the script.</td></tr><tr><td colspan="1" class="confluenceTd">Number of failed retries</td><td colspan="1" class="confluenceTd">The number of times the task is resubmitted after failure. It supports drop-down and manual filling.</td></tr><tr><td colspan="1" class="confluenceTd">Failure Retry Interval</td><td colspan="1" class="confluenceTd">The time interval for resubmitting the task if the task fails. It supports drop-down and manual filling.</td></tr><tr><td colspan="1" class="confluenceTd">Timeout alarm</td><td colspan="1" class="confluenceTd">Check Timeout Alarm and Timeout Failure. When the task exceeds the "timeout duration", an alarm email will be sent and the task execution will fail.</td></tr><tr><td colspan="1" class="confluenceTd">Zeppelin Note ID</td><td colspan="1" class="confluenceTd">The unique note id for a zeppelin notebook note.</td></tr><tr><td colspan="1" class="confluenceTd">Zeppelin Paragraph ID</td><td colspan="1" class="confluenceTd">The unique paragraph id for a zeppelin notebook paragraph.</td></tr><tr><td colspan="1" class="confluenceTd">Zeppelin Parameters</td><td colspan="1" class="confluenceTd">Parameters in json format used for zeppelin dynamic form.</td></tr></tbody></table>

Task Example
------------

### Zeppelin Paragraph Task Example

This example illustrates how to create a zeppelin paragraph task node.

![demo-zeppelin-paragraph](/img/tasks/demo/zeppelin.png)

![demo-get-zeppelin-id](/img/tasks/demo/zeppelin_id.png)