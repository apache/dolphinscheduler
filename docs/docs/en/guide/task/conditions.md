# Conditions

Condition is a conditional node, that determines which downstream task should run based on the condition of the upstream task. Currently, the Conditions support multiple upstream tasks, but only two downstream tasks. When the number of upstream tasks exceeds one, achieve complex upstream dependencies by through `and` and `or` operators.

## Create Task

Drag from the toolbar <img src="/img/conditions.png" width="20"/> task node to canvas to create a new Conditions task, as shown in the figure below:

  <p align="center">
   <img src="/img/condition_dag_en.png" width="80%" />
  </p>

  <p align="center">
   <img src="/img/condition_task_en.png" width="80%" />
  </p>

## Parameter

- Node name: The node name in a workflow definition is unique.
- Run flag: Identifies whether this node schedules normally, if it does not need to execute, select the `prohibition execution`.
- Descriptive information: Describe the function of the node.
- Task priority: When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.
- Worker grouping: Assign tasks to the machines of the worker group to execute. If `Default` is selected, randomly select a worker machine for execution.
- Times of failed retry attempts: The number of times the task failed to resubmit. You can select from drop-down or fill-in a number.
- Failed retry interval: The time interval for resubmitting the task after a failed task. You can select from drop-down or fill-in a number.
- Timeout alarm: Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm email will send and the task execution will fail.
- Downstream tasks selection: supports two branches success and failure.
  - Success: When the upstream task runs successfully, run the success branch.
  - Failure: When the upstream task runs failed, run the failure branch.
- Upstream condition selection: can select one or more upstream tasks for conditions.
  - Add an upstream dependency: the first parameter is to choose a specified task name, and the second parameter is to choose the upstream task status to trigger conditions.
  - Select upstream task relationship: use `and` and `or` operators to handle the complex relationship of upstream when there are multiple upstream tasks for conditions.

## Related Task

[switch](switch.md): Conditions task mainly executes the corresponding branch based on the execution status (success, failure) of the upstream nodes. The [Switch](switch.md) task node mainly executes the corresponding branch based on the value of the [global parameter](../parameter/global.md) and the result of user written expression.
