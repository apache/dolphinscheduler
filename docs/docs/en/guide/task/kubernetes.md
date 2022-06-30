# K8S Node

## Overview

K8S task type used to execute a batch task. In this task, the worker submits the task by using a k8s client.

## Create Task

- Click `Project Management -> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag from the toolbar <img src="../../../../img/tasks/icons/kubernetes.png" width="15"/> to the canvas.

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
| Namespace | The namespace for running k8s task. |
| Min CPU | Minimum CPU requirement for running k8s task. |
| Min Memory | Minimum memory requirement for running k8s task. |
| Image | The registry url for image. |
| Custom parameter | It is a local user-defined parameter for K8S task, these params will pass to container as environment variables. |
| Predecessor task | Selecting a predecessor task for the current task, will set the selected predecessor task as upstream of the current task. |


## Task Example

### Configure the K8S Environment in DolphinScheduler

If you are using the K8S task type in a production environment, the K8S cluster environment is required.

### Configure K8S Nodes

Configure the required content according to the parameter descriptions above.

![K8S](../../../../img/tasks/demo/kubernetes-task-en.png)

## Note

Task name contains only lowercase alphanumeric characters or '-'
