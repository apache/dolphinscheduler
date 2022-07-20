# Workflow Definition

## Overview

Work Definition screen shows list of existing workflows and utility to create or import workflows.

![workflow-dag](../../../../img/new_ui/dev/project/workflow-overview.png)

## Create workflow definition

Click `Project Management -> Workflow -> Workflow Definition`, enter the `Workflow Definition` page, and click the `Create Workflow` button to enter the **workflow DAG edit** page, as shown in the following figure:

![workflow-dag](../../../../img/new_ui/dev/project/workflow-dag.png)

### Create Tasks to Workflow

Drag from the toolbar <img src="../../../../img/tasks/icons/shell.png" width="15"/> to the canvas, to add a shell task to the canvas, as shown in the figure below:

![demo-shell-simple](../../../../img/tasks/demo/shell.jpg)

**Add parameter settings for shell task:**

1. Fill in the `Node Name`, `Description` and `Script` fields.
2. Check `Normal` for `Run Flag`. If `Prohibit Execution` is checked, the task will not execute when the workflow runs.
3. Select `Task Priority`: when the number of worker threads is insufficient, high priority tasks will execute first in the execution queue, and tasks with the same priority will execute in the order of first in, first out.
4. Timeout alarm (optional): check the timeout alarm, timeout failure, and fill in the "timeout period". When the task execution time exceeds **timeout period**, an alert email will send and the task timeout fails.
5. Resources (optional). Resources are files create or upload in the `Resource Center -> File Management` page. For example, the file name is `test.sh`, and the command to call the resource in the script is `sh test.sh`.
6. Customize parameters (optional).
7. Click the `Confirm Add` button to save the task settings.

### Set dependencies between tasks
 
Click the plus sign on the right of the task node to connect the task; as shown in the figure below, task Node_B and task Node_C execute in parallel, When task Node_A finished execution, tasks Node_B and Node_C will execute simultaneously.

![workflow-dependent](../../../../img/new_ui/dev/project/workflow-dependent.png)

**Delete dependencies:** Using your mouse to select the connection line, and click the "Delete" icon in the upper right corner <img src= "../../../../img/delete.png" width="35"/>, delete dependencies between tasks.

![workflow-delete](../../../../img/new_ui/dev/project/workflow-delete.png)

### Save workflow definition

Click the `Save` button, and the "Set DAG chart name" window pops up, as shown in the figure below. Enter the workflow definition name, workflow definition description, and set global parameters (optional, refer to [global parameters](../parameter/global.md)), click the `Add` button to finish workflow definition creation.

![workflow-save](../../../../img/new_ui/dev/project/workflow-save.png)

## Workflow Definition Operation Function

Click `Project Management -> Workflow -> Workflow Definition` to enter the workflow definition page, as shown below:

![workflow-list](../../../../img/new_ui/dev/project/workflow-list.png)

Workflow running parameter description:

* **Failure strategy**: When a task node fails to execute, other parallel task nodes need to execute the strategy. "Continue" means: After a task fails, other task nodes execute normally; "End" means: Terminate all tasks being executed, and terminate the entire process. 
* **Notification strategy**: When the process ends, send process execution information notification emails according to the process status, including no status, success, failure, success or failure. 
* **Process priority**: the priority of process operation, divided into five levels: the highest (HIGHEST), high (HIGH), medium (MEDIUM), low (LOW), the lowest (LOWEST). When the number of master threads is insufficient, processes with higher levels will be executed first in the execution queue, and processes with the same priority will be executed in the order of first-in, first-out. 
* **Worker grouping**: This process can only be executed in the specified worker machine group. The default is Default, which can be executed on any worker. 
* **Notification Group**: Select Notification Policy||Timeout Alarm||When fault tolerance occurs, process information or emails will be sent to all members in the notification group. 
* **Recipient**: Select Notification Policy||Timeout Alarm||When fault tolerance occurs, process information or alarm email will be sent to the recipient list. 
* **Cc**: Select Notification Policy||Timeout Alarm||When fault tolerance occurs, the process information or alarm email will be copied to the Cc list. 
* **Startup parameters**: Set or override the value of global parameters when starting a new process instance. 
* **Complement**: There are 2 modes of serial complement and parallel complement. Serial complement: within the specified time range, perform complements in sequence from the start date to the end date, and generate N process instances in turn; parallel complement: within the specified time range, perform multiple complements at the same time, and generate N process instances at the same time . 
  * **Complement**: Execute the workflow definition of the specified date, you can select the time range of the supplement (currently only supports the supplement for consecutive days), for example, the data from May 1st to May 10th needs to be supplemented, as shown in the following figure:

The following are the operation functions of the workflow definition list:

- **Edit:** Only "Offline" workflow definitions can be edited. Workflow DAG editing is the same as [Create Workflow Definition](#create-workflow-definition)
- **Online:** When the workflow status is "Offline", used to make workflow online. Only the workflow in the "Online" state can run, but cannot edit.
- **Offline:** When the workflow status is "Online", used to make workflow offline. Only the workflow in the "Offline" state can be edited, but cannot run.
- **Run:** Only workflow in the online state can run. See [Run Workflow](#run-the-workflow) for the operation steps.
- **Timing:** Timing can only set to online workflows, and the system automatically schedules to run the workflow on time. The status after creating a timing setting is "offline", and the timing must set online on the timing management page to make effect. See [Workflow Timing](#workflow-schedule) for timing operation steps.
- **Timing Management:** The timing management page can edit, online or offline and delete timing.
- **Delete:** Delete the workflow definition. In the same project, only the workflow definition created by yourself can be deleted, and the workflow definition of other users cannot be deleted. If you need to delete it, please contact the user who created it or the administrator.
- **Download:** Download workflow definition to local.
- **Tree Diagram:** Display the task node type and task status in a tree structure, as shown in the figure below:

![workflow-tree](../../../../img/new_ui/dev/project/workflow-tree.png)

## Run the Workflow

- Click `Project Management -> Workflow -> Workflow Definition` to enter the workflow definition page, as shown in the figure below, click the "Go Online" button <img src="../../../../img/online.png" width="35"/>to make workflow online.

![workflow-online](../../../../img/new_ui/dev/project/workflow-online.png)

- Click the `Run` button to pop up the startup parameter setting window, as shown in the figure below, set the startup parameters, click the `Run` button in the pop-up box, the workflow starts running, and the workflow instance page generates a workflow instance.

![workflow-run](../../../../img/new_ui/dev/project/workflow-run.png)
 
  Description of workflow operating parameters: 
       
  * Failure strategy: When a task node fails to execute, other parallel task nodes need to execute this strategy. "Continue" means: after a certain task fails, other task nodes execute normally; "End" means: terminate all tasks execution, and terminate the entire process.
  * Notification strategy: When the process is over, send the process execution result notification email according to the process status, options including no send, send if sucess, send of failure, send whatever result.
  * Process priority: The priority of process operation, divide into five levels: highest (HIGHEST), high (HIGH), medium (MEDIUM), low (LOW), and lowest (LOWEST). When the number of master threads is insufficient, high priority processes will execute first in the execution queue, and processes with the same priority will execute in the order of first in, first out.
  * Worker group: The process can only be executed in the specified worker machine group. The default is `Default`, which can execute on any worker.
  * Notification group: select notification strategy||timeout alarm||when fault tolerance occurs, process result information or email will send to all members in the notification group.
  * Recipient: select notification policy||timeout alarm||when fault tolerance occurs, process result information or alarm email will be sent to the recipient list.
  * Cc: select notification policy||timeout alarm||when fault tolerance occurs, the process result information or warning email will be copied to the CC list.
  * Startup parameter: Set or overwrite global parameter values when starting a new process instance.
  * Complement: includes serial complement and parallel complement and supports manual date input and date selection. Serial complement defines, within the specified time range, executes the complements from the start date to the end date by sequential and N process instances are generated in sequence. Parallel complement defines executing the complement concurrently in the multiple days to generate N process instances within the specified time range. Manual date input defines: manually input the date following the date format `yyyy-MM-dd HH:mm:ss` and separate dates by a comma. Date selection defines: to select dates via UI.
    * Dependent Mode: Trigger the complement of workflow instances that the downstream dependent node depends on the current workflow(It is required that the scheduled status of the workflow instance of the current replenishment is online, and only the downstream supplement directly dependent on the current workflow will be triggered).
    * You can select complement time range (When the scheduled configuration is not online, the daily complement will be performed by default according to the selected time range.If the timing configuration is online, it will be complemented according to the selected time range in combination with the timing configuration) when executing a timing workflow definition. For example, need to fill in the data from 1st May to 10th May, as shown in the figure below:

    ![workflow-date](../../../../img/new_ui/dev/project/workflow-date.png)

    > Serial mode: the complement execute sequentially from 9th May to 10th May, and the process instance page generates 2 process instances;

    > Parallel mode: The tasks from 9th May to 10th May execute simultaneously, and the process instance page generates 2 process instances;
 
    * You can manually enter the complement dates (independent from the timing configuration) to execute the complement data to workflow definitions. The figure below is an example to make complement data from 9th May to 10th May:
 
    ![workflow-date](../../../../img/new_ui/dev/project/workflow_date_manual.png)

    > Serial mode: the complement execute sequentially from 9th May to 10th May, and the process instance page generates 2 process instances;

    > Parallel mode: The tasks from 9th May to 10th May execute simultaneously, and the process instance page generates 2 process instances;

## Run the task alone

- Right-click the task and click the `Start` button (only online tasks can be clicked to run).

![workflow-task-run](../../../../img/new_ui/dev/project/workflow-task-run.png)

- The startup parameter setting dialog box is displayed, and the parameter description is the same as [run the workflow](#run-the-workflow).

![workflow-task-run-config](../../../../img/new_ui/dev/project/workflow-task-run-config.png)

## Workflow Schedule

- Create schedule: Click `Project Management -> Workflow-> Workflow Definition`, enter the workflow definition page, make the workflow online, click the "timing" button <img src="../../../../img/timing.png" width="35"/> , the timing parameter setting dialog box pops up, as shown in the figure below:

  ![workflow-time01](../../../../img/new_ui/dev/project/workflow-time01.png)

- Select a start and end time. Within the start and end time range, the workflow is run regularly; outside the start and end time range, no timed workflow instance will be generated.
- Add a timing that execute 5 minutes once, as shown in the following figure:

  ![workflow-time02](../../../../img/new_ui/dev/project/workflow-time02.png)

- Failure strategy, notification strategy, process priority, worker group, notification group, recipient, and CC are the same as workflow running parameters.
- Click the "Create" button to create the timing. Now the timing status is "**Offline**" and the timing needs to be **Online** to make effect.
- Schedule online: Click the `Timing Management` button <img src="../../../../img/timeManagement.png" width="35"/>, enter the timing management page, click the `online` button, the timing status will change to `online`, as shown in the below figure, the workflow makes effect regularly.

  ![workflow-time03](../../../../img/new_ui/dev/project/workflow-time03.png)

## Import Workflow

Click `Project Management -> Workflow -> Workflow Definition` to enter the workflow definition page, click the `Import Workflow` button to import the local workflow file, the workflow definition list displays the imported workflow and the status is offline.
