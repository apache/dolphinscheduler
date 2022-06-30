# OpenMLDB Node

## Overview

[OpenMLDB](https://openmldb.ai/) is an excellent open source machine learning database, providing a full-stack 
FeatureOps solution for production.

OpenMLDB task plugin used to execute tasks on OpenMLDB cluster.

## Create Task

- Click `Project Management -> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag from the toolbar <img src="../../../../img/tasks/icons/openmldb.png" width="15"/> task node to canvas.

## Task Parameters

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
| Predecessor task | Selecting the predecessor task of the current task will set the selected predecessor task as the upstream of the current task. |
| zookeeper | OpenMLDB cluster zookeeper address, e.g. 127.0.0.1:2181. |
| zookeeper path | OpenMLDB cluster zookeeper path, e.g. /openmldb. |
| Execute Mode | Determine the init mode, offline or online. You can switch it in sql statement. |
| SQL statement | SQL statement. |
| Custom parameters | It is the user-defined parameters of Python, which will replace the content with \${variable} in the script. |

## Task Examples

### Load data

![load data](../../../../img/tasks/demo/openmldb-load-data.png)

We use `LOAD DATA` to load data into OpenMLDB cluster. We select `offline` here, so it will load to offline storage.

### Feature extraction

![fe](../../../../img/tasks/demo/openmldb-feature-extraction.png)

We use `SELECT INTO` to do feature extraction. We select `offline` here, so it will run sql on offline engine.

### Environment to Prepare

#### Start the OpenMLDB Cluster

You should create an OpenMLDB cluster first. If in production env, please check [deploy OpenMLDB](https://openmldb.ai/docs/en/v0.5/deploy/install_deploy.html).

You can follow [run OpenMLDB in docker](https://openmldb.ai/docs/zh/v0.5/quickstart/openmldb_quickstart.html#id11)
to a quick start.

#### Python Environment

The OpenMLDB task will use OpenMLDB Python SDK to connect OpenMLDB cluster. So you should have the Python env.

We will use `python3` by default. You can set `PYTHON_HOME` to use your custom python env.

Make sure you have installed OpenMLDB Python SDK in the host where the worker server running, using `pip install openmldb`.
