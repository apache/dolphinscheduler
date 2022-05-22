# Conditions

Condition is a conditional node, that determines which downstream task should run based on the condition of the upstream task. Currently, the Conditions support multiple upstream tasks, but only two downstream tasks. When the number of upstream tasks exceeds one, achieve complex upstream dependencies by through `and` and `or` operators.

## Create Task

- Click `Project -> Management-Project -> Name-Workflow Definition`, and click the "Create Workflow" button to enter the DAG editing page.
- Drag from the toolbar <img src="/img/conditions.png" width="20"/> task node to canvas.

## Parameter

- Node name: The node name in a workflow definition is unique.
- Run flag: Identifies whether this node schedules normally, if it does not need to execute, select the `prohibition execution`.
- Descriptive information: Describe the function of the node.
- Task priority: When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.
- Worker grouping: Assign tasks to the machines of the worker group to execute. If `Default` is selected, randomly select a worker machine for execution.
- Times of failed retry attempts: The number of times the task failed to resubmit. You can select from drop-down or fill-in a number.
- Failed retry interval: The time interval for resubmitting the task after a failed task. You can select from drop-down or fill-in a number.
- Timeout alarm: Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm email will send and the task execution will fail.
- Downstream tasks selection: Depending on the status of the predecessor task, you can jump to the corresponding branch, currently two branches are supported: success, failure
  - Success: When the upstream task runs successfully, run the success branch.
  - Failure: When the upstream task runs failed, run the failure branch.
- Upstream condition selection: can select one or more upstream tasks for conditions.
  - Add an upstream dependency: the first parameter is to choose a specified task name, and the second parameter is to choose the upstream task status to trigger conditions.
  - Select upstream task relationship: use `and` and `or` operators to handle the complex relationship of upstream when there are multiple upstream tasks for conditions.

## Related Task

[switch](switch.md): Conditions task mainly executes the corresponding branch based on the execution status (success, failure) of the upstream nodes. The [Switch](switch.md) task node mainly executes the corresponding branch based on the value of the [global parameter](../parameter/global.md) and the result of user written expression.

## Example

This sample demonstrates the operation of the Condition task by using the [Shell](shell.md) task.

### 1. Create workflow

Go to the workflow definition page, and then create the following task nodes:

- Node_A: Shell task, prints out "hello world", its main function is the upstream branch of Condition, and triggers the corresponding branch node according to whether its execution is successful or not.
- Condition: The Conditions task executes the corresponding branch according to the execution status of the upstream task.
- Node_Success: Shell task, print out "success", Node_A executes the successful branch.
- Node_False: Shell task, print out "false", Node_A executes the failed branch.

![condition_task01](/img/tasks/demo/condition_task01.png)

### 2. View the execution result

After you finish creating the workflow, you can run the workflow online. You can view the execution status of each task on the workflow instance page. As shown below:

![condition_task02](/img/tasks/demo/condition_task02.png)

In the above figure, the task status marked with a green check mark is the successfully executed task node.

## Notice

- The Conditions task supports multiple upstream tasks, but only two downstream tasks.
- The Conditions task and the workflow that contain it do not support copy operations.
- The predecessor task of Conditions cannot connect to its branch nodes, which will cause logical confusion and does not conform to DAG scheduling. The situation shown below is **wrong**.

![condition_task03](/img/tasks/demo/condition_task03.png)
![condition_task04](/img/tasks/demo/condition_task04.png)
