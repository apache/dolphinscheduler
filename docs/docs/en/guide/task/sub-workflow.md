# SubWorkflow Node

## Overview

The sub-workflow node is to execute an external workflow definition as a task node.

## Create Task

- Click `Project Management -> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag from the toolbar <img src="../../../../img/tasks/icons/sub_workflow.png" width="15"/> task node to canvas to create a new SubWorkflow task.

## Task Parameter

[//]: # (TODO: use the commented anchor below once our website template supports this syntax)
[//]: # (- Please refer to [DolphinScheduler Task Parameters Appendix]&#40;appendix.md#default-task-parameters&#41; `Default Task Parameters` section for default parameters.)

- Please refer to [DolphinScheduler Task Parameters Appendix](appendix.md) `Default Task Parameters` section for default parameters.

| **Parameter** |                                                                               **Description**                                                                               |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Child node    | It is the workflow definition of the selected sub-workflow. Enter the child node in the upper right corner to jump to the workflow definition of the selected sub-workflow. |

## Task Example

This example simulates a common task type, here we use a child node task to recall the [Shell](shell.md) to print out "hello". This means executing a shell task as a child node.

### Create a Shell task

Create a shell task to print "hello" and define the workflow as `test_dag01`.

![subworkflow_task01](../../../../img/tasks/demo/subworkflow_task01.png)

## Create the Sub_workflow task

To use the sub_workflow, you need to create the sub-node task, which is the workflow `test_dag01` we created in the first step. After that, as shown in the diagram below, select the corresponding sub-node in position ⑤.

![subworkflow_task02](../../../../img/tasks/demo/subworkflow_task02.png)

After creating the sub_workflow, create a corresponding shell task for printing "world" and link both together. Save the current workflow and run it to get the expected result.

![subworkflow_task03](../../../../img/tasks/demo/subworkflow_task03.png)

## Note

When using `sub_workflow` to recall a sub-node task, you don't need to ensure that the defined sub-node is online status.
