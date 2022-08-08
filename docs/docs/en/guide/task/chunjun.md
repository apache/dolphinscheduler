# ChunJun

## Overview

ChunJun task type for executing ChunJun programs. For ChunJun nodes, the worker will execute `${CHUNJUN_HOME}/bin/start-chunjun` to analyze the input json file.

## Create Task

- Click `Project Management -> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag the <img src="../../../../img/tasks/icons/chunjun.png" width="15"/> from the toolbar to the drawing board.

## Task Parameters

| **Parameter** | **Description** |
| ------- | ---------- |
| Node name | The node name in a workflow definition is unique. |
| Run flag | Identifies whether this node schedules normally, if it does not need to execute, select the prohibition execution. |
| Task priority | When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order. |
| Description | Describe the function of the node. |
| Worker group | Assign tasks to the machines of the worker group to execute. If `Default` is selected, randomly select a worker machine for execution. |
| Environment Name | Configure the environment name in which run the script. |
| Number of failed retries | The number of times the task failed to resubmit. |
| Failed retry interval | The time interval (unit minute) for resubmitting the task after a failed task. |
| Task group name | The task group name. |
| Priority | The task priority. |
| Delayed execution time |  The time, in minutes, that a task is delayed in execution. |
| Timeout alarm | Check the timeout alarm and timeout failure. When the task exceeds the "timeout period", an alarm email will be sent and the task execution will fail. |
| Custom template | Custom the content of the ChunJun node's json profile. |
| json | json configuration file for ChunJun synchronization. |
| Custom parameters | It is a user-defined parameter, and will replace the content with `${variable}` in the script.
| Deploy mode | Execute chunjun task mode, eg local standalone. |
| Option Parameters | Support such as `-confProp "{\"flink.checkpoint.interval\":60000}"` |
| Predecessor task | Selecting a predecessor task for the current task will set the selected predecessor task as upstream of the current task. |

## Task Example

This example demonstrates importing data from Hive into MySQL.

### Configuring the ChunJun environment in DolphinScheduler

If you are using the ChunJun task type in a production environment, it is necessary to configure the required environment first. The configuration file is as follows: `/dolphinscheduler/conf/env/dolphinscheduler_env.sh`.

![chunjun_task01](../../../../img/tasks/demo/chunjun_task01.png)

After the environment has been configured, DolphinScheduler needs to be restarted.

### Configuring ChunJun Task Node

As the data to be read from Hive, a custom json is required, refer to: [Hive Json Template](https://github.com/DTStack/chunjun/blob/master/chunjun-examples/json/hive/binlog_hive.json).

After writing the required json file, you can configure the node content by following the steps in the diagram below.

![chunjun_task02](../../../../img/tasks/demo/chunjun_task02.png)

### View run results

![chunjun_task03](../../../../img/tasks/demo/chunjun_task03.png)

### Note

Before execute ${CHUNJUN_HOME}/bin/start-chunjun, need to change the shell ${CHUNJUN_HOME}/bin/start-chunjun, remove '&' in order to run in front. 

 such as:

```shell
nohup $JAVA_RUN -cp $JAR_DIR $CLASS_NAME $@ &
```

update to following:

```shell
nohup $JAVA_RUN -cp $JAR_DIR $CLASS_NAME $@
```