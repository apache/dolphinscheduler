# MapReduce Node

## Overview

MapReduce(MR) task type used for executing MapReduce programs. For MapReduce nodes, the worker submits the task by using the Hadoop command `hadoop jar`. See [Hadoop Command Manual](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/CommandsManual.html#jar) for more details.

## Create Task

- Click `Project -> Management-Project -> Name-Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag from the toolbar <img src="/img/tasks/icons/mr.png" width="15"/> to the canvas.

## Task Parameter

- **Node name**: The node name in a workflow definition is unique.
- **Run flag**: Identifies whether this node schedules normally, if it does not need to execute, select the `prohibition execution`.
- **Descriptive information**: Describe the function of the node.
- **Task priority**: When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.
- **Worker grouping**:  Assign tasks to the machines of the worker group to execute. If `Default` is selected, randomly select a worker machine for execution.
- **Environment Name**: Configure the environment name in which run the script.
- **Times of failed retry attempts**: The number of times the task failed to resubmit.
- **Failed retry interval**: The time interval (unit minute) for resubmitting the task after a failed task.
- **Delayed execution time**: The time (unit minute) that a task delays in execution.
- **Timeout alarm**: Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm email will send and the task execution will fail.
- **Resource**: Refers to the list of resource files that called in the script, and upload or create files by the Resource Center file management.
- **Custom parameters**: It is a local user-defined parameter for MapReduce, and will replace the content with `${variable}` in the script.
- **Predecessor task**: Selecting a predecessor task for the current task, will set the selected predecessor task as upstream of the current task.

### JAVA or SCALA Program

- **Program type**: Select JAVA or SCALA program.
- **The class of the main function**: The **full path** of Main Class, the entry point of the MapReduce program.
- **Main jar package**: The jar package of the MapReduce program.
- **Task name** (optional): MapReduce task name.
- **Command line parameters**: Set the input parameters of the MapReduce program and support the substitution of custom parameter variables.
- **Other parameters**: support `-D`, `-files`, `-libjars`, `-archives` format.
- **Resource**: Appoint resource files in the `Resource` if parameters refer to them.
- **User-defined parameter**: It is a local user-defined parameter for MapReduce, and will replace the content with `${variable}` in the script.

## Python Program

- **Program type**: Select Python language.
- **Main jar package**: The Python jar package for running MapReduce.
- **Other parameters**: support `-D`, `-mapper`, `-reducer,` `-input` `-output` format, and you can set the input of user-defined parameters, such as:
- `-mapper "mapper.py 1"` `-file mapper.py` `-reducer reducer.py` `-file reducer.py` `–input /journey/words.txt` `-output /journey/out/mr/\${currentTimeMillis}`
- The `mapper.py 1` after `-mapper` is two parameters, the first parameter is `mapper.py`, and the second parameter is `1`.
- **Resource**: Appoint resource files in the `Resource` if parameters refer to them.
- **User-defined parameter**: It is a local user-defined parameter for MapReduce, and will replace the content with `${variable}` in the script.

## Task Example

### Execute the WordCount Program

This example is a common introductory type of MapReduce application, which used to count the number of identical words in the input text.

#### Configure the MapReduce Environment in DolphinScheduler

If you are using the MapReduce task type in a production environment, it is necessary to configure the required environment first. The following is the configuration file: `bin/env/dolphinscheduler_env.sh`.

![mr_configure](/img/tasks/demo/mr_task01.png)

#### Upload the Main Package

When using the MapReduce task node, you need to use the Resource Centre to upload the jar package for the execution. Refer to the [resource centre](../resource/configuration.md).

After finish the Resource Centre configuration, upload the required target files directly by dragging and dropping.

![resource_upload](/img/tasks/demo/upload_jar.png)

#### Configure MapReduce Nodes

Configure the required content according to the parameter descriptions above.

![demo-mr-simple](/img/tasks/demo/mr_task02.png)
