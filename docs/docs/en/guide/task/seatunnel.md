# Apache SeaTunnel

## Overview

`SeaTunnel` task type for creating and executing `SeaTunnel` tasks. When the worker executes this task, it will parse the config file through the `start-seatunnel-spark.sh` or `start-seatunnel-flink.sh` command.
Click [here](https://seatunnel.apache.org/) for more information about `Apache SeaTunnel`.

## Create Task

- Click Project Management -> Project Name -> Workflow Definition, and click the "Create Workflow" button to enter the DAG editing page.
- Drag the <img src="../../../../img/tasks/icons/seatunnel.png" width="15"/> from the toolbar to the drawing board.

## Task Parameter

- Node name: The node name in a workflow definition is unique.
- Run flag: Identifies whether this node can be scheduled normally, if it does not need to be executed, you can turn on the prohibition switch.
- Descriptive information: describe the function of the node.
- Task priority: When the number of worker threads is insufficient, they are executed in order from high to low, and when the priority is the same, they are executed according to the first-in first-out principle.
- Worker grouping: Tasks are assigned to the machines of the worker group to execute. If Default is selected, a worker machine will be randomly selected for execution.
- Environment Name: Configure the environment name in which to run the script.
- Number of failed retry attempts: The number of times the task failed to be resubmitted.
- Failed retry interval: The time, in cents, interval for resubmitting the task after a failed task.
- Cpu quota: Assign the specified CPU time quota to the task executed. Takes a percentage value. Default -1 means unlimited. For example, the full CPU load of one core is 100%,and that of 16 cores is 1600%. This function is controlled by [task.resource.limit.state](../../architecture/configuration.md)
- Max memory：Assign the specified max memory to the task executed. Exceeding this limit will trigger oom to be killed and will not automatically retry. Takes an MB value. Default -1 means unlimited. This function is controlled by [task.resource.limit.state](../../architecture/configuration.md)
- Delayed execution time: The time, in cents, that a task is delayed in execution.
- Timeout alarm: Check the timeout alarm and timeout failure. When the task exceeds the "timeout period", an alarm email will be sent and the task execution will fail.
- Engine: Supports FLINK and SPARK
    - FLINK
        - Run model: supports `run` and `run-application` modes
        - Option parameters: used to add the parameters of the Flink engine, such as `-m yarn-cluster -ynm seatunnel`
    - SPARK
        - Deployment mode: specify the deployment mode, `cluster` `client` `local`
        - Master: Specify the `Master` model, `yarn` `local` `spark` `mesos`, where `spark` and `mesos` need to specify the `Master` service address, for example: 127.0.0.1:7077
    > Click [here](https://seatunnel.apache.org/docs/2.1.2/command/usage) for more information on the usage of `Apache SeaTunnel command`
- Custom Configuration: Supports custom configuration or select configuration file from Resource Center
    > Click [here](https://seatunnel.apache.org/docs/2.1.2/concept/config) for more information about `Apache SeaTunnel config` file
- Script: Customize configuration information on the task node, including four parts: `env` `source` `transform` `sink`
- Resource file: The configuration file of the resource center can be referenced in the task node, and only one configuration file can be referenced.
- Predecessor task: Selecting a predecessor task for the current task will set the selected predecessor task as upstream of the current task.

## Task Example

This sample demonstrates using the Flink engine to read data from a Fake source and print to the console.

### Configuring the SeaTunnel environment in DolphinScheduler

If you want to use the SeaTunnel task type in the production environment, you need to configure the required environment first. The configuration file is as follows: `/dolphinscheduler/conf/env/dolphinscheduler_env.sh`.

![seatunnel_task01](../../../../img/tasks/demo/seatunnel_task01.png)

### Configuring SeaTunnel Task Node

According to the above parameter description, configure the required content.

![seatunnel_task02](../../../../img/tasks/demo/seatunnel_task02.png)

### Config example

```Config

env {
  execution.parallelism = 1
}

source {
  FakeSource {
    result_table_name = "fake"
    field_name = "name,age"
  }
}

transform {
  sql {
    sql = "select name,age from fake"
  }
}

sink {
  ConsoleSink {}
}

```
