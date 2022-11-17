# Blocking

Blocking is a node that decides whether or not to suspend the whole running workflow, i.e. blocking. It based on the running status of its predecessor and the parameters configured by the user when adding the node. Users can also set whether to alert when a workflow is blocked. After user intervention, they will decide to continue or re-run the workflow.

## Create Task

- Click `Project -> Management-Project -> Name-Workflow Definition`, and click the "Create Workflow" button to enter the DAG editing page.
- Drag from the toolbar <img src="../../../../img/blocking.png" width="20"/> task node to canvas.

## Parameter

- Node name：The node name in a workflow definition is unique.
- Run flag：Identifies whether this node can be scheduled normally, if it does not need to be executed, you can turn on the prohibition switch.
- Description：describe the function of the node.
- Task priority：When the number of worker threads is insufficient, they are executed in order from high to low, and when the priority is the same, they are executed according to the first-in first-out principle.
- Worker grouping：Tasks are assigned to the machines of the worker group to execute. If Default is selected, a worker machine will be randomly selected for execution.
- Number of failed retry attempts：The number of times the task failed to be resubmitted.
- Failed retry interval：The time interval for resubmitting the task after a failed task.
- Timeout alarm：Check the timeout alarm and timeout failure. When the task exceeds the "timeout period", an alarm email will be sent and the task execution will fail.
- Blocking condition：There are two options, which define the opportunity of blocking the workflow:
  - Blocking on custom param failed：That is, the workflow is blocked when the result of the custom parameter is **false**.
  - Blocking on custom param success：That is, the workflow is blocked when the result of the custom parameter is **success**.
- Alert on blocking：Whether to send alerts to relevant users when a workflow is blocked.
- Add pre task check condition：i.e. **custom parameter**. One or more upstream tasks can be selected for blocking node to build workflow blocking logic.
  - Add the upstream dependency：Use the first parameter to choose task name, and the second parameter for status of the upsteam task.
  - Upstream task relationship：we use `and` and `or`operators to handle complex relationship of upstream when multiple upstream tasks for Conditions task.

## Example

This sample demonstrates the operation of the Condition task by using the [Shell](shell.md) task and [Python](python.md) task.

### Create Workflow

Go to the workflow definition page, and then create the following task nodes:

- Shell-01: Shell task. It will print `Hello word`, its main function is the upstream branch of blocking node.
- Py-01: Python task. The script is `print('Hello Word'`. Note that the syntax error is on purpose,  its main function is the upstream branch of blocking node.
- Blocking-01: Blocking node. The blocking logic: Shell-01 success **AND** Py-01 success. Blocking opportunity: Blocking on condition success. Alert when blocking.
- Shell-02: Shell task. It will print `Process end`.

![blocking-task01](../../../../img/tasks/demo/blocking_task01.png)

![blocking-task02](../../../../img/tasks/demo/blocking_task02.png)

### View the execution result

After you finish creating the workflow, you can run the workflow online. You can view the execution status of each task on the workflow instance page. You may notice the workflow has been blocked. As shown below:

![blocking-task03](../../../../img/tasks/demo/blocking_task04.png)

![blocking-task03](../../../../img/tasks/demo/blocking_task03.png)

Now, you can choose re-run or continue the workflow.

If you have configured the alert plugin (like email), you will receive the alert.
