# MapReduce Node

Overview
--------

MapReduce(MR) task type used for executing MapReduce programs. For MapReduce nodes, the worker submits the task by using the Hadoop command `hadoop jar`. See [Hadoop Command Manual](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/CommandsManual.html#jar) for more details.

Create Task
-----------

*   Click `Project Management -> Project Name -> Workflow Definition`, and click the "`Create Workflow"` button to enter the DAG editing page.
*   Drag from the toolbar <img src="/img/tasks/icons/mr.png" width="15"/> to the canvas.

Task Parameter
--------------

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Node name</td><td class="confluenceTd">The node name in a workflow definition is unique.</td></tr><tr><td colspan="1" class="confluenceTd">Run flag</td><td colspan="1" class="confluenceTd">Identifies whether this node schedules normally, if it does not need to execute, select the&nbsp;<code>prohibition execution</code>.</td></tr><tr><td colspan="1" class="confluenceTd">Description</td><td colspan="1" class="confluenceTd">Describe the function of the node.</td></tr><tr><td colspan="1" class="confluenceTd">Task priority</td><td colspan="1" class="confluenceTd"><p>When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.</p></td></tr><tr><td colspan="1" class="confluenceTd">Worker group</td><td colspan="1" class="confluenceTd"><p>Assign tasks to the machines of the worker group to execute. If&nbsp;<code>Default</code>&nbsp;is selected, randomly select a worker machine for execution.</p></td></tr><tr><td colspan="1" class="confluenceTd">Environment Name</td><td colspan="1" class="confluenceTd">Configure the environment name in which run the script.</td></tr><tr><td colspan="1" class="confluenceTd">Number of failed retries</td><td colspan="1" class="confluenceTd">The number of times the task failed to resubmit.</td></tr><tr><td colspan="1" class="confluenceTd">Failed retry interval</td><td colspan="1" class="confluenceTd">The time interval (unit minute) for resubmitting the task after a failed task.</td></tr><tr><td colspan="1" class="confluenceTd">Delayed execution time</td><td colspan="1" class="confluenceTd">The time (unit minute) that a task delays in execution.</td></tr><tr><td colspan="1" class="confluenceTd">Timeout alarm</td><td colspan="1" class="confluenceTd"><p>Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm email will send and the task execution will fail.</p></td></tr><tr><td colspan="1" class="confluenceTd">Resource</td><td colspan="1" class="confluenceTd">Refers to the list of resource files that called in the script, and upload or create files by the Resource Center file management.</td></tr><tr><td colspan="1" class="confluenceTd">Custom parameters</td><td colspan="1" class="confluenceTd"><p>It is a local user-defined parameter for MapReduce, and will replace the content with&nbsp;<code>${variable}</code>&nbsp;in the script.</p></td></tr><tr><td colspan="1" class="confluenceTd">Pre task</td><td colspan="1" class="confluenceTd">Selecting a predecessor task for the current task, will set the selected predecessor task as upstream of the current task.</td></tr><tr><td style="text-align: center;" colspan="2" class="confluenceTd"><strong><span class="inline-comment-marker" data-ref="2f1523cf-80c1-4a55-bba2-11d99338936f">JAVA or SCALA Program</span></strong></td></tr><tr><td colspan="1" class="confluenceTd">Program type</td><td colspan="1" class="confluenceTd"><p>Select JAVA or SCALA program.</p></td></tr><tr><td colspan="1" class="confluenceTd">Main Class</td><td colspan="1" class="confluenceTd"><p>The&nbsp;full path&nbsp;of Main Class, the entry point of the MapReduce program.</p></td></tr><tr><td colspan="1" class="confluenceTd">Main Package</td><td colspan="1" class="confluenceTd">The jar package of the MapReduce program.</td></tr><tr><td colspan="1" class="confluenceTd">App Name</td><td colspan="1" class="confluenceTd">MapReduce task name.</td></tr><tr><td colspan="1" class="confluenceTd">Main Arguments</td><td colspan="1" class="confluenceTd">Set the input parameters of the MapReduce program and support the substitution of custom parameter variables.</td></tr><tr><td colspan="1" class="confluenceTd">Option parameters</td><td colspan="1" class="confluenceTd"><p>Supports&nbsp;<code>-D</code>,&nbsp;<code>-files</code>,&nbsp;<code>-libjars</code>,&nbsp;<code>-archives</code>&nbsp;format.</p></td></tr><tr><td colspan="1" class="confluenceTd">Resource</td><td colspan="1" class="confluenceTd"><p>Appoint resource files in the&nbsp;<code>Resource</code>&nbsp;if parameters refer to them.</p></td></tr><tr><td colspan="1" class="confluenceTd">User-defined parameter</td><td colspan="1" class="confluenceTd"><p>It is a local user-defined parameter for MapReduce, and will replace the content with&nbsp;<code>${variable}</code>&nbsp;in the script.</p></td></tr><tr><td style="text-align: center;" colspan="2" class="confluenceTd"><strong>Python Program</strong></td></tr><tr><td colspan="1" class="confluenceTd">Program type</td><td colspan="1" class="confluenceTd">Select Python language.</td></tr><tr><td colspan="1" class="confluenceTd">Main Package</td><td colspan="1" class="confluenceTd">The Python jar package for running MapReduce.</td></tr><tr><td colspan="1" class="confluenceTd">Option parameters</td><td colspan="1" class="confluenceTd"><p>Supports&nbsp;<code>-D</code>,&nbsp;<code>-mapper</code>,&nbsp;<code>-reducer,</code>&nbsp;<code>-input</code>&nbsp;<code>-output</code>&nbsp;format, and you can set the input of user-defined parameters, such as:</p><ul><li><code>-mapper "mapper.py 1"</code>&nbsp;<code>-file mapper.py</code>&nbsp;<code>-reducer reducer.py</code>&nbsp;<code>-file reducer.py</code>&nbsp;<code>–input /journey/words.txt</code>&nbsp;<code>-output /journey/out/mr/\${currentTimeMillis}</code></li></ul><p>The&nbsp;<code>mapper.py 1</code>&nbsp;after&nbsp;<code>-mapper</code>&nbsp;is two parameters, the first parameter is&nbsp;<code>mapper.py</code>, and the second parameter is&nbsp;<code>1</code>.</p></td></tr><tr><td colspan="1" class="confluenceTd">Resource</td><td colspan="1" class="confluenceTd"><p>Appoint resource files in the&nbsp;<code>Resource</code>&nbsp;if parameters refer to them.</p></td></tr><tr><td colspan="1" class="confluenceTd">User-defined parameter</td><td colspan="1" class="confluenceTd"><p>It is a local user-defined parameter for MapReduce, and will replace the content with&nbsp;<code>${variable}</code>&nbsp;in the script.</p></td></tr></tbody></table>

Task Example
------------

### Execute the WordCount Program

This example is a common introductory type of MapReduce application, which used to count the number of identical words in the input text.

#### Configure the MapReduce Environment in DolphinScheduler

If you are using the MapReduce task type in a production environment, it is necessary to configure the required environment first. The following is the configuration file: `bin/env/dolphinscheduler_env.sh`.

![mr_configure](/img/tasks/demo/mr_task01.png)

#### Upload the Main Package

When using the MapReduce task node, you need to use the Resource Centre to upload the jar package for the execution. Refer to the [resource centre](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/resource/configuration.html).

After finish the Resource Centre configuration, upload the required target files directly by dragging and dropping.

![resource_upload](/img/tasks/demo/upload_jar.png)

#### Configure MapReduce Nodes

Configure the required content according to the parameter descriptions above.

![demo-mr-simple](/img/tasks/demo/mr_task02.png)