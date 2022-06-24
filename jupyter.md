# Jupyter

Use `Jupyter Task` to create a jupyter-type task and execute jupyter notes. When the worker executes `Jupyter Task`, it will use `papermill` to evaluate jupyter notes. Click [here](https://papermill.readthedocs.io/en/latest/) for details about `papermill`.

Conda Configuration
-------------------

*   Config `conda.path` in `common.properties` to the path of your `conda.sh`, which should be the same `conda` you use to manage the python environment of your `papermill` and `jupyter`. Click [here](https://docs.conda.io/en/latest/) for more information about `conda`.
*   `conda.path` is set to `/opt/anaconda3/etc/profile.d/conda.sh` by default. If you have no idea where your `conda` is, simply run `conda info | grep -i 'base environment'`.

Create Task
-----------

*   Click `Project Management-Project Name-Workflow Definition`, and click the "`Create Workflow`" button to enter the DAG editing page.
*   Drag <img src="/img/tasks/icons/jupyter.png" width="15"/> from the toolbar to the canvas.

Task Parameter
--------------

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Node name</td><td class="confluenceTd"><p>The node name in a workflow definition is unique.</p></td></tr><tr><td class="confluenceTd">Run flag</td><td class="confluenceTd"><p>Identifies whether this node schedules normally, if it does not need to execute, select the&nbsp;<code>prohibition execution</code>.</p></td></tr><tr><td class="confluenceTd">Task priority</td><td class="confluenceTd"><p>When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.</p></td></tr><tr><td colspan="1" class="confluenceTd">Description</td><td colspan="1" class="confluenceTd"><p>Describe the function of the node.</p></td></tr><tr><td colspan="1" class="confluenceTd">Worker group</td><td colspan="1" class="confluenceTd"><p>&nbsp;Assign tasks to the machines of the worker group to execute. If&nbsp;<code>Default</code>&nbsp;is selected, randomly select a worker machine for execution.</p></td></tr><tr><td colspan="1" class="confluenceTd">Environment Name</td><td colspan="1" class="confluenceTd"><p>Configure the environment name in which run the script.</p></td></tr><tr><td colspan="1" class="confluenceTd"><p>Number of failed retries</p></td><td colspan="1" class="confluenceTd"><p>The number of times the task failed to resubmit.</p></td></tr><tr><td colspan="1" class="confluenceTd">Failed retry interval</td><td colspan="1" class="confluenceTd"><p>The time interval (unit minute) for resubmitting the task after a failed task.</p></td></tr><tr><td colspan="1" class="confluenceTd">Cpu quota</td><td colspan="1" class="confluenceTd"><p>Assign the specified CPU time quota to the task executed. Takes a percentage value. Default -1 means unlimited. For example, the full CPU load of one core is 100%,and that of 16 cores is 1600%. This function is controlled by<span>&nbsp;</span><a style="text-decoration: none;" href="https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/architecture/configuration.html" class="external-link" rel="nofollow">task.resource.limit.state</a></p></td></tr><tr><td colspan="1" class="confluenceTd">Max memory</td><td colspan="1" class="confluenceTd"><p>Assign the specified max memory to the task executed. Exceeding this limit will trigger oom to be killed and will not automatically retry. Takes an MB value. Default -1 means unlimited. This function is controlled by<span>&nbsp;</span><a href="https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/architecture/configuration.html" style="text-decoration: none;" class="external-link" rel="nofollow">task.resource.limit.state</a></p></td></tr><tr><td colspan="1" class="confluenceTd">Timeout alarm</td><td colspan="1" class="confluenceTd">Check the timeout alarm and timeout failure. When the task exceeds the "timeout period", an alarm email will send and the task execution will fail.</td></tr><tr><td colspan="1" class="confluenceTd">Conda Environment&nbsp; Name</td><td colspan="1" class="confluenceTd">Name of conda environment.</td></tr><tr><td colspan="1" class="confluenceTd">Input Note Path</td><td colspan="1" class="confluenceTd">Path of input jupyter note template.</td></tr><tr><td colspan="1" class="confluenceTd">Out Note Path</td><td colspan="1" class="confluenceTd">Path of output note.</td></tr><tr><td colspan="1" class="confluenceTd">Jupyter Parameters</td><td colspan="1" class="confluenceTd">Parameters in json format used for jupyter note parameterization.</td></tr><tr><td colspan="1" class="confluenceTd">Kernel</td><td colspan="1" class="confluenceTd">Jupyter notebook kernel.</td></tr><tr><td colspan="1" class="confluenceTd">Engine</td><td colspan="1" class="confluenceTd">Engine to evaluate jupyter notes.</td></tr><tr><td colspan="1" class="confluenceTd">Jupyter Execution Timeout</td><td colspan="1" class="confluenceTd">Timeout set for each jupyter notebook cell.</td></tr><tr><td colspan="1" class="confluenceTd">Jupyter Start Timeout</td><td colspan="1" class="confluenceTd">Timeout set for jupyter notebook kernel.</td></tr><tr><td colspan="1" class="confluenceTd">Others</td><td colspan="1" class="confluenceTd">Other command options for papermill.</td></tr></tbody></table>

  

Task Example
------------

### Jupyter Task Example

This example illustrates how to create a jupyter task node.

![demo-jupyter-simple](/img/tasks/demo/jupyter.png)