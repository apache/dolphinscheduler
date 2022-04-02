# Switch

The switch is a conditional judgment node, decide the branch executes according to the value of [global variable](../parameter/global.md) and the expression result written by the user.

## Create

Drag from the toolbar <img src="/img/switch.png" width="20"/>  task node to canvas to create a task. 
**Note** After created a switch task, you must first configure the upstream and downstream, then configure the parameter of task branches.

## Parameter

- Node name: The node name in a workflow definition is unique.
- Run flag: Identifies whether this node schedules normally, if it does not need to execute, select the `prohibition execution`.
- Descriptive information: Describe the function of the node.
- Task priority: When the number of worker threads is insufficient, execute in the order of priority from high to low, and tasks with the same priority will execute in a first-in first-out order.
- Worker grouping: Assign tasks to the machines of the worker group to execute. If `Default` is selected, randomly select a worker machine for execution.
- Times of failed retry attempts: The number of times the task failed to resubmit. You can select from drop-down or fill-in a number.
- Failed retry interval: The time interval for resubmitting the task after a failed task. You can select from drop-down or fill-in a number.
- Timeout alarm: Check the timeout alarm and timeout failure. When the task runs exceed the "timeout", an alarm email will send and the task execution will fail.
- Condition: You can configure multiple conditions for the switch task. When the conditions are satisfied, execute the configured branch. You can configure multiple different conditions to satisfy different businesses.
- Branch flow: The default branch flow, when all the conditions are not satisfied, execute this branch flow.

## Detail

Here we have three tasks, the dependencies are `A -> B -> [C, D]`, and `task_a` is a shell task and `task_b` is a switch task

- In task A, a global variable named `id` is defined through [global variable](../parameter/global.md), and the declaration method is `${setValue(id=1)}`
- Task B adds conditions and uses global variables declared upstream to achieve conditional judgment (Note: switch can get the global variables value, as long as its direct or indirect upstream have already assigned the global variables before switch acquires). We want to execute task C when `id = 1`, otherwise run task D
  - Configure task C to run when the global variable `id=1`. Then edit `${id} == 1` in the condition of task B, and select `C` as branch flow
  - For other tasks, select `D` as branch flow

The following shows the switch task configuration:

![task-switch-configure](/img/switch_configure.jpg)

## Related Task

[condition](conditions.md)：[Condition](conditions.md)task mainly executes the corresponding branch based on the execution result status (success, failure) of the upstream node. 
The [Switch](switch.md) task mainly executes the corresponding branch based on the value of the [global parameter](../parameter/global.md) and the judgment expression result written by the user.