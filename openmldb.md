# OpenMLDB Node
[OpenMLDB](https://openmldb.ai/) is an excellent open source machine learning database, providing a full-stack FeatureOps solution for production.

OpenMLDB task plugin used to execute tasks on OpenMLDB cluster.

Create Task
-----------

*   Click `Project -> Management-Project -> Name-Workflow Definition`, and click the "Create Workflow" button to enter the DAG editing page.
*   Drag from the toolbar <img src="/img/tasks/icons/openmldb.png" width="15"/> task node to canvas.

Task Example with Task Parameters
---------------------------------

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Node name</td><td class="confluenceTd"><p>The node name in a workflow definition is unique.</p></td></tr><tr><td class="confluenceTd">Run flag</td><td class="confluenceTd"><p>Identifies whether this node schedules normally, if it does not need to execute, select the&nbsp;<code>prohibition execution</code>.</p></td></tr><tr><td class="confluenceTd">Task priority</td><td class="confluenceTd"><p>When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.</p></td></tr><tr><td colspan="1" class="confluenceTd">Description</td><td colspan="1" class="confluenceTd"><p>Describe the function of the node.</p></td></tr><tr><td colspan="1" class="confluenceTd">Worker group</td><td colspan="1" class="confluenceTd"><p>&nbsp;Assign tasks to the machines of the worker group to execute. If&nbsp;<code>Default</code>&nbsp;is selected, randomly select a worker machine for execution.</p></td></tr><tr><td colspan="1" class="confluenceTd">Environment Name</td><td colspan="1" class="confluenceTd"><p>Configure the environment name in which run the script.</p></td></tr><tr><td colspan="1" class="confluenceTd"><p>Number of failed retries</p></td><td colspan="1" class="confluenceTd"><p>The number of times the task failed to resubmit.</p></td></tr><tr><td colspan="1" class="confluenceTd">Failed retry interval</td><td colspan="1" class="confluenceTd"><p>The time interval (unit minute) for resubmitting the task after a failed task.</p></td></tr><tr><td colspan="1" class="confluenceTd">Delayed execution time</td><td colspan="1" class="confluenceTd">The time (unit minute) that a task delays in execution.</td></tr><tr><td colspan="1" class="confluenceTd">Timeout alarm</td><td colspan="1" class="confluenceTd">Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm email will send and the task execution will fail.</td></tr><tr><td colspan="1" class="confluenceTd">Pre tasks</td><td colspan="1" class="confluenceTd">Selecting a predecessor task for the current task will set the selected predecessor task as upstream of the current task.</td></tr></tbody></table>

### OpenMLDB Parameters

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">zookeeper</td><td class="confluenceTd"><p>OpenMLDB cluster zookeeper address, e.g. 127.0.0.1:2181.</p></td></tr><tr><td class="confluenceTd">zookeeper path</td><td class="confluenceTd"><p>OpenMLDB cluster zookeeper path, e.g. /openmldb.</p></td></tr><tr><td class="confluenceTd"><p>Execute Mode&nbsp;</p></td><td class="confluenceTd"><p>determine the init mode, offline or online. You can switch it in sql statement.</p></td></tr><tr><td colspan="1" class="confluenceTd">SQL statement</td><td colspan="1" class="confluenceTd"><p>Describe the function of the node.</p></td></tr><tr><td colspan="1" class="confluenceTd">Custom parameters</td><td colspan="1" class="confluenceTd"><p>It is the user-defined parameters of Python, which will replace the content with ${variable} in the script.zookeeper<span style="letter-spacing: 0.0px;">&nbsp;</span></p></td></tr></tbody></table>

Examples
--------

#### Load data

![load data](/img/tasks/demo/openmldb-load-data.png)

We use `LOAD DATA` to load data into OpenMLDB cluster. We select `offline` here, so it will load to offline storage.

#### Feature extraction

![fe](/img/tasks/demo/openmldb-feature-extraction.png)

We use `SELECT INTO` to do feature extraction. We select `offline` here, so it will run sql on offline engine.

Environment to prepare
----------------------

### Start the OpenMLDB cluster

You should create an OpenMLDB cluster first. If in production env, please check [deploy OpenMLDB](https://openmldb.ai/docs/en/v0.5/deploy/install_deploy.html).

You can follow [run OpenMLDB in docker](https://openmldb.ai/docs/zh/v0.5/quickstart/openmldb_quickstart.html#id11) to a quick start.

### Python env

The OpenMLDB task will use OpenMLDB Python SDK to connect OpenMLDB cluster. So you should have the Python env.

We will use `python3` by default. You can set `PYTHON_HOME` to use your custom python env.

Make sure you have installed OpenMLDB Python SDK in the host where the worker server running, using `pip install openmldb`.