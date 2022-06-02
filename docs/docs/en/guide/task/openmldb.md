# OpenMLDB Node

## Overview

[OpenMLDB](https://openmldb.ai/) is an excellent open source machine learning database, providing a full-stack 
FeatureOps solution for production.

OpenMLDB task plugin used to execute tasks on OpenMLDB cluster.

## Create Task

- Click `Project -> Management-Project -> Name-Workflow Definition`, and click the "Create Workflow" button to enter the
  DAG editing page.
- Drag from the toolbar <img src="../../../../img/tasks/icons/openmldb.png" width="15"/> task node to canvas.

## Task Example

First, introduce some general parameters of DolphinScheduler

- **Node name**: The node name in a workflow definition is unique.
- **Run flag**: Identifies whether this node schedules normally, if it does not need to execute, select
  the `prohibition execution`.
- **Descriptive information**: Describe the function of the node.
- **Task priority**: When the number of worker threads is insufficient, execute in the order of priority from high
  to low, and tasks with the same priority will execute in a first-in first-out order.
- **Worker grouping**: Assign tasks to the machines of the worker group to execute. If `Default` is selected,
  randomly select a worker machine for execution.
- **Environment Name**: Configure the environment name in which run the script.
- **Times of failed retry attempts**: The number of times the task failed to resubmit.
- **Failed retry interval**: The time interval (unit minute) for resubmitting the task after a failed task.
- **Delayed execution time**: The time (unit minute) that a task delays in execution.
- **Timeout alarm**: Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm
  email will send and the task execution will fail.
- **Predecessor task**: Selecting a predecessor task for the current task, will set the selected predecessor task as
  upstream of the current task.

### OpenMLDB Parameters

**Task Parameter**

- **zookeeper** ：OpenMLDB cluster zookeeper address, e.g. 127.0.0.1:2181.
- **zookeeper path** : OpenMLDB cluster zookeeper path, e.g. /openmldb.
- **Execute Mode** ：determine the init mode, offline or online. You can switch it in sql statement.
- **SQL statement** ：SQL statement.
- Custom parameters: It is the user-defined parameters of Python, which will replace the content with \${variable} in the script.

Here are some examples:

#### Load data

![load data](../../../../img/tasks/demo/openmldb-load-data.png)

We use `LOAD DATA` to load data into OpenMLDB cluster. We select `offline` here, so it will load to offline storage.

#### Feature extraction

![fe](../../../../img/tasks/demo/openmldb-feature-extraction.png)

We use `SELECT INTO` to do feature extraction. We select `offline` here, so it will run sql on offline engine.

## Environment to prepare

### Start the OpenMLDB cluster

You should create an OpenMLDB cluster first. If in production env, please check [deploy OpenMLDB](https://openmldb.ai/docs/en/v0.5/deploy/install_deploy.html).

You can follow [run OpenMLDB in docker](https://openmldb.ai/docs/zh/v0.5/quickstart/openmldb_quickstart.html#id11)
to a quick start.

### Python env

The OpenMLDB task will use OpenMLDB Python SDK to connect OpenMLDB cluster. So you should have the Python env.

We will use `python3` by default. You can set `PYTHON_HOME` to use your custom python env.

Make sure you have installed OpenMLDB Python SDK in the host where the worker server running, using `pip install openmldb`.
