# Shell

## Overview

Shell task used to create a shell task type and execute a series of shell scripts. When the worker run the shell task, a temporary shell script is generated, and use the Linux user with the same name as the tenant executes the script.

## Create Task

- Click `Project -> Management-Project -> Name-Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag  from the toolbar <img src="/img/tasks/icons/shell.png" width="15"/> to the canvas.

## Task Parameter

- Node name: The node name in a workflow definition is unique.
- Run flag: Identifies whether this node schedules normally, if it does not need to execute, select the `prohibition execution`.
- Descriptive information: Describe the function of the node.
- Task priority: When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.
- Worker grouping: Assign tasks to the machines of the worker group to execute. If `Default` is selected, randomly select a worker machine for execution.
- Environment Name: Configure the environment name in which run the script.
- Times of failed retry attempts: The number of times the task failed to resubmit. You can select from drop-down or fill-in a number.
- Failed retry interval: The time interval for resubmitting the task after a failed task. You can select from drop-down or fill-in a number.
- Timeout alarm: Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm email will send and the task execution will fail.
- Script: Shell program developed by users.
- Resource: Refers to the list of resource files that called in the script, and upload or create files by the Resource Center file management.
- Custom parameters: It is a user-defined local parameter of Shell, and will replace the content with `${variable}` in the script.
- Predecessor task: Selecting a predecessor task for the current task, will set the selected predecessor task as upstream of the current task.

## Task Example

### Simply Print

We make an example simulate from a common task which runs by one command. The example is to print one line in the log file, as shown in the following figure:
"This is a demo of shell task".

![demo-shell-simple](/img/tasks/demo/shell.jpg)

### Custom Parameters

This example simulates a custom parameter task. We use parameters for reusing existing tasks as template or coping with the dynamic task. In this case,
we declare a custom parameter named "param_key", with the value "param_val". Then we use `echo` to print the parameter "${param_key}" we just declared. 
After running this example, we would see "param_val" print in the log.

![demo-shell-custom-param](/img/tasks/demo/shell_custom_param.jpg)

## Attention
The shell task type resolves whether the task log contains ```application_xxx_xxx``` to determine whether is the yarn task. If so, the corresponding application
will be use to judge the running state of the current shell node. At this time, if stops the operation of the workflow, the corresponding ```application_id```
will be killed.