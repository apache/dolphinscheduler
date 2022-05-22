# Dependent

## Overview

Dependent nodes are **dependency check nodes**. For example, process A depends on the successful execution of process B from yesterday, and the dependent node will check whether process B run successful yesterday.


## Create Task

- Click `Project -> Management-Project -> Name-Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag from the toolbar <img src="/img/tasks/icons/dependent.png" width="15"/> task node to canvas.

## Task Parameter

- **Node name**: The node name in a workflow definition is unique.
- **Run flag**: Identifies whether this node schedules normally, if it does not need to execute, select the `prohibition execution`.
- **Descriptive information**: Describe the function of the node.
- **Task priority**: When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.
- **Worker grouping**: Assign tasks to the machines of the worker group to execute. If `Default` is selected, randomly select a worker machine for execution.
- **Environment Name**: Configure the environment name in which run the script.
- **Times of failed retry attempts**: The number of times the task failed to resubmit.
- **Failed retry interval**: The time interval (unit minute) for resubmitting the task after a failed task.
- **Delayed execution time**: The time (unit minute) that a task delays in execution.

## Examples

The Dependent node provides a logical judgment function, which can detect the execution of the dependent node according to the logic.

For example, process A is a weekly task, processes B and C are daily tasks, and task A requires tasks B and C to be successfully executed every day of the last week.

![dependent_task01](/img/tasks/demo/dependent_task01.png)

And another example is that process A is a weekly report task, processes B and C are daily tasks, and task A requires tasks B or C to be successfully executed every day of the last week:

![dependent_task02](/img/tasks/demo/dependent_task02.png)

If the weekly report A also needs to be executed successfully last Tuesday:

![dependent_task03](/img/tasks/demo/dependent_task03.png)
