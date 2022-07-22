# SageMaker Node

## Overview

[Amazon SageMaker](https://docs.aws.amazon.com/sagemaker/index.html) is a fully managed machine learning service. With Amazon SageMaker, data scientists and developers can quickly build and train machine learning models, and then deploy them into a production-ready hosted environment.

[Amazon SageMaker Model Building Pipelines](https://docs.aws.amazon.com/sagemaker/latest/dg/pipelines.html) is a tool for building machine learning pipelines that take advantage of direct SageMaker integration.


For users using big data and machine learning, SageMaker task plugin help users connect big data workflows with SageMaker usage scenarios.

DolphinScheduler SageMaker task plugin features are as follows:

- Start a SageMaker pipeline execution. Continuously get the execution status until the pipeline completes execution.

## Create Task

- Click `Project -> Management-Project -> Name-Workflow Definition`, and click the "Create Workflow" button to enter the
  DAG editing page.
- Drag from the toolbar <img src="../../../../img/tasks/icons/sagemaker.png" width="15"/> task node to canvas.

## Task Example

First, introduce some general parameters of DolphinScheduler:

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

Here are some specific parameters for the SagaMaker plugin:

- **SagemakerRequestJson**: Request parameters of StartPipelineExecution，see also [AWS API](https://docs.aws.amazon.com/sagemaker/latest/APIReference/API_StartPipelineExecution.html)


The task plugin are shown as follows:

![sagemaker_pipeline](../../../../img/tasks/demo/sagemaker_pipeline.png)



## Environment to prepare

Some AWS configuration is required, modify a field in file `common.properties`
```yaml
# The AWS access key. if resource.storage.type=S3 or use EMR-Task, This configuration is required
resource.aws.access.key.id=<YOUR AWS ACCESS KEY>
# The AWS secret access key. if resource.storage.type=S3 or use EMR-Task, This configuration is required
resource.aws.secret.access.key=<YOUR AWS SECRET KEY>
# The AWS Region to use. if resource.storage.type=S3 or use EMR-Task, This configuration is required
resource.aws.region=<AWS REGION>
```