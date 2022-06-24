# Kubernetes Node
K8S task type used to execute a batch task. In this task, the worker submits the task by using a k8s client.

Create Task
-----------

*   Click `Project Management -> Project Name -> Workflow Definition`, and click the "`Create Workflow"` button to enter the DAG editing page.
*   Drag from the toolbar <img src="/img/tasks/icons/kubernetes.png" width="15"/> to the canvas.

Task Parameter
--------------

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Node name</td><td class="confluenceTd"><p>The node name in a workflow definition is unique.</p></td></tr><tr><td class="confluenceTd">Run flag</td><td class="confluenceTd"><p>Identifies whether this node schedules normally, if it does not need to execute, select the&nbsp;<code>prohibition execution</code>.</p></td></tr><tr><td class="confluenceTd">Task priority</td><td class="confluenceTd"><p>When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.</p></td></tr><tr><td colspan="1" class="confluenceTd">Description</td><td colspan="1" class="confluenceTd"><p>Describe the function of the node.</p></td></tr><tr><td colspan="1" class="confluenceTd">Worker group</td><td colspan="1" class="confluenceTd"><p>&nbsp;Assign tasks to the machines of the worker group to execute. If&nbsp;<code>Default</code>&nbsp;is selected, randomly select a worker machine for execution.</p></td></tr><tr><td colspan="1" class="confluenceTd">Environment Name</td><td colspan="1" class="confluenceTd"><p>Configure the environment name in which run the script.</p></td></tr><tr><td colspan="1" class="confluenceTd"><p>Number of failed retries</p></td><td colspan="1" class="confluenceTd"><p>The number of times the task failed to resubmit.</p></td></tr><tr><td colspan="1" class="confluenceTd">Failed retry interval</td><td colspan="1" class="confluenceTd"><p>The time interval (unit minute) for resubmitting the task after a failed task.</p></td></tr><tr><td colspan="1" class="confluenceTd">Delayed execution time</td><td colspan="1" class="confluenceTd">The time (unit minute) that a task delays in execution.</td></tr><tr><td colspan="1" class="confluenceTd">Timeout alarm</td><td colspan="1" class="confluenceTd">Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm email will send and the task execution will fail.</td></tr><tr><td colspan="1" class="confluenceTd">Namespace</td><td colspan="1" class="confluenceTd">The namespace for running k8s task.</td></tr><tr><td colspan="1" class="confluenceTd">Minimum CPU</td><td colspan="1" class="confluenceTd">Minimum CPU requirement for running k8s task.</td></tr><tr><td colspan="1" class="confluenceTd">Minimum Memory</td><td colspan="1" class="confluenceTd">Minimum memory requirement for running k8s task.</td></tr><tr><td colspan="1" class="confluenceTd">Image</td><td colspan="1" class="confluenceTd">The registry url for image.</td></tr><tr><td colspan="1" class="confluenceTd">Custom parameter</td><td colspan="1" class="confluenceTd">It is a local user-defined parameter for K8S task, these params will pass to container as environment variables.</td></tr><tr><td colspan="1" class="confluenceTd">Predecessor task</td><td colspan="1" class="confluenceTd">Selecting a predecessor task for the current task, will set the selected predecessor task as upstream of the current task.</td></tr></tbody></table>

Task Example
------------

### Configure the K8S Environment in DolphinScheduler

If you are using the K8S task type in a production environment, the K8S cluster environment is required.

### Configure K8S Nodes

Configure the required content according to the parameter descriptions above.

![K8S](/img/tasks/demo/kubernetes-task-en.png)

Notice
------

Task name contains only lowercase alphanumeric characters or '-'