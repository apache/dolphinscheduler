# Apache Linkis

## Overview

`Linkis` task type for creating and executing `Linkis` tasks. When the worker executes this task, it will parse the shell parameters through the `linkis-cli` command.
Click [here](https://linkis.apache.org/) for more information about `Apache Linkis`.

## Create Task

- Click Project Management -> Project Name -> Workflow Definition, and click the "Create Workflow" button to enter the DAG editing page.
- Drag the <img src="../../../../img/tasks/icons/linkis.png" width="15"/> from the toolbar to the drawing board.

## Task Parameter

[//]: # (TODO: use the commented anchor below once our website template supports this syntax)
[//]: # (- Please refer to [DolphinScheduler Task Parameters Appendix]&#40;appendix.md#default-task-parameters&#41; `Default Task Parameters` section for default parameters.)

- Please refer to [DolphinScheduler Task Parameters Appendix](appendix.md) `Default Task Parameters` section for default parameters.
- Please refer to [Linkis-Cli Task Parameters](https://linkis.apache.org/zh-CN/docs/latest/user-guide/linkiscli-manual) `Linkis Support Parameters` section for Linkis parameters.

## Task Example

This sample demonstrates using the Spark engine to execute sql script.

### Configuring the Linkis environment in DolphinScheduler

If you want to use the Linkis task type in the production environment, you need to configure the required environment first. The configuration file is as follows: `/dolphinscheduler/conf/env/dolphinscheduler_env.sh`.

![linkis_task01](../../../../img/tasks/demo/linkis_task01.png)

### Configuring Linkis Task Node

According to the above parameter description, configure the required content.

![linkis_task02](../../../../img/tasks/demo/linkis_task02.png)

### Config example

```

sh ./bin/linkis-cli -engineType spark-2.4.3 -codeType sql -code "select count(*) from testdb.test;"  -submitUser hadoop -proxyUser hadoop 

```

### Attention

- No need to fill `sh ./bin/linkis-cli` in the configuration column, it has been configured in advance.
- The default configuration is asynchronous submission. You do not need to configure the `--async` parameter.

