# MapReduce Node

## Overview

MapReduce(MR) task type used for executing MapReduce programs. For MapReduce nodes, the worker submits the task by using the Hadoop command `hadoop jar`. See [Hadoop Command Manual](https://hadoop.apache.org/docs/r3.2.4/hadoop-project-dist/hadoop-common/CommandsManual.html#jar) for more details.

## Create Task

- Click `Project Management -> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag from the toolbar <img src="../../../../img/tasks/icons/mr.png" width="15"/> to the canvas.

## Task Parameters

### General

| **Parameter** | **Description** |
| ------- | ---------- |
| Node Name | Set the name of the task. Node names within a workflow definition are unique. |
| Run flag | Indicates whether the node can be scheduled normally. If it is not necessary to execute, you can turn on the prohibiting execution switch. |
| Description | Describes the function of this node. |
| Task priority | When the number of worker threads is insufficient, they are executed in order from high to low according to the priority, and they are executed according to the first-in, first-out principle when the priority is the same. |
| Worker group | The task is assigned to the machines in the worker group for execution. If Default is selected, a worker machine will be randomly selected for execution. |
| Task group name | The group in Resources, if not configured, it will not be used. | 
| Environment Name | Configure the environment in which to run the script. |
| Number of failed retries | The number of times the task is resubmitted after failure. It supports drop-down and manual filling. | 
| Failure Retry Interval | The time interval for resubmitting the task if the task fails. It supports drop-down and manual filling. | 
| Timeout alarm | Check Timeout Alarm and Timeout Failure. When the task exceeds the "timeout duration", an alarm email will be sent and the task execution will fail. |
| Resource | Refers to the list of resource files that called in the script, and upload or create files by the Resource Center file management. |
| Custom parameters | It is a local user-defined parameter for MapReduce, and will replace the content with `${variable}` in the script. |
| Predecessor task | Selecting a predecessor task for the current task, will set the selected predecessor task as upstream of the current task. |

### JAVA or SCALA Program

| **Parameter** | **Description** |
| ------- | ---------- |
| Program type | Select JAVA or SCALA program. |
| The class of the main function | The **full path** of Main Class, the entry point of the MapReduce program. |
| Main jar package | The jar package of the MapReduce program. |
| Task name | MapReduce task name. |
| Command line parameters | Set the input parameters of the MapReduce program and support the substitution of custom parameter variables. |
| Other parameters | Support `-D`, `-files`, `-libjars`, `-archives` format. |
| Resource | Appoint resource files in the `Resource` if parameters refer to them. |
| User-defined parameter | It is a local user-defined parameter for MapReduce, and will replace the content with `${variable}` in the script. |

### Python Program

| **Parameter** | **Description** |
| ------- | ---------- |
| Program type | Select Python language. |
| Main jar package | The Python jar package for running MapReduce. |
| Other parameters | Support `-D`, `-mapper`, `-reducer,` `-input` `-output` format, and you can set the input of user-defined parameters, such as:<ul><li>`-mapper "mapper.py 1"` `-file mapper.py` `-reducer reducer.py` `-file reducer.py` `–input /journey/words.txt` `-output /journey/out/mr/${currentTimeMillis}`</li><li>The `mapper.py 1` after `-mapper` is two parameters, the first parameter is `mapper.py`, and the second parameter is `1`. </li></ul> |
| Resource | Appoint resource files in the `Resource` if parameters refer to them. |
| User-defined parameter | It is a local user-defined parameter for MapReduce, and will replace the content with `${variable}` in the script. |

## Task Example

### Execute the WordCount Program

This example is a common introductory type of MapReduce application, which used to count the number of identical words in the input text.

#### Configure the MapReduce Environment in DolphinScheduler

If you are using the MapReduce task type in a production environment, it is necessary to configure the required environment first. The following is the configuration file: `bin/env/dolphinscheduler_env.sh`.

![mr_configure](../../../../img/tasks/demo/mr_task01.png)

#### Upload the Main Package

When using the MapReduce task node, you need to use the Resource Centre to upload the jar package for the execution. Refer to the [resource centre](../resource/configuration.md).

After finish the Resource Centre configuration, upload the required target files directly by dragging and dropping.

![resource_upload](../../../../img/tasks/demo/upload_jar.png)

#### Configure MapReduce Nodes

Configure the required content according to the parameter descriptions above.

![demo-mr-simple](../../../../img/tasks/demo/mr_task02.png)
