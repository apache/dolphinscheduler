# K8S Node

## Overview

K8S task type used to execute a batch task. In this task, the worker submits the task by using a k8s client.

## Create Task

- Click `Project -> Management-Project -> Name-Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag from the toolbar <img src="/img/tasks/icons/kubernetes.png" width="15"/> to the canvas.

## Task Parameter

- **Node name**: The node name in a workflow definition is unique.
- **Run flag**: Identifies whether this node schedules normally, if it does not need to execute, select the `prohibition execution`.
- **Descriptive information**: Describe the function of the node.
- **Task priority**: When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.
- **Worker grouping**: Assign tasks to the machines of the worker group to execute. If `Default` is selected, randomly select a worker machine for execution.
- **Environment Name**: Configure the environment name in which to run the task.
- **Times of failed retry attempts**: The number of times the task failed to resubmit.
- **Failed retry interval**: The time interval (unit minute) for resubmitting the task after a failed task.
- **Delayed execution time**: The time (unit minute) that a task delays in execution.
- **Timeout alarm**: Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm email will send and the task execution will fail.
- **Namespace**:：the namespace for running k8s task
- **Min CPU**：min CPU requirement for running k8s task
- **Min Memory**：min memory requirement for running k8s task
- **Image**：the registry url for image 
- **Custom parameter**: It is a local user-defined parameter for K8S task, these params will pass to container as environment variables.
- **Predecessor task**: Selecting a predecessor task for the current task, will set the selected predecessor task as upstream of the current task.
## Task Example

### Configure the K8S Environment in DolphinScheduler

If you are using the K8S task type in a production environment, the K8S cluster environment is required.

### Configure K8S Nodes

Configure the required content according to the parameter descriptions above.

![K8S](/img/tasks/demo/kubernetes-task-en.png)

## Notice

Task name contains only lowercase alphanumeric characters or '-'
