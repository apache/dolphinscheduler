# Workflow Definition

Work Definition screen shows list of existing workflows and utility to create or import workflows.

![workflow-dag](/img/new_ui/dev/project/project-list-1.png)

### Create Workflow

Create workflow button takes you to workflow DAG edit page. You can drag various tasks from the toolbar on the blank canvas to form a DAG.

![workflow-dag](/img/new_ui/dev/project/workflow-dag.png)

Drag from the toolbar <img src="/img/tasks/icons/shell.png" width="15"/> to the canvas, to add a shell task to the canvas, as shown in the figure below.

*   **Add parameter settings for shell task:**
    

1.  Fill in the "Node Name", "Description" and "Script" fields.
2.  Check “Normal” for “Run Flag”. If “Prohibit Execution” is checked, the task will not execute when the workflow runs.
3.  Select "Task Priority": when the number of worker threads is insufficient, high priority tasks will execute first in the execution queue, and tasks with the same priority will execute in the order of first in, first out(FIFO).
4.  Timeout alarm (optional): check the timeout alarm, timeout failure, and fill in the "timeout period". When the task execution time exceeds **timeout period**, an alert email will send and the task timeout fails
5.  Resources (optional). Resources are files create or upload in the Resource Center -> File Management page. For example, the file name is `test.sh`, and the command to call the resource in the script is `sh test.sh`
6.  Customize parameters (optional)
7.  Click the "Confirm Add" button to save the task settings.

![demo-shell-simple](/img/tasks/demo/shell.jpg)

*   **Set dependencies between tasks:** Click the icon in the upper right corner to connect the task; as shown in the figure below, task 2 and task 3 execute in parallel, When task 1 finished execution, tasks 2 and 3 will execute simultaneously.

![workflow-dependent](/img/new_ui/dev/project/workflow-dependent.png)

**Delete dependencies:** Click the "arrow" icon in the upper right corner <img src="/img/arrow.png" width="35"/>, select the connection line, and click the "Delete" icon in the upper right corner <img src= "/img/delete.png" width="35"/>), delete dependencies between tasks.

![workflow-delete](/img/new_ui/dev/project/workflow-delete.png)

**Save workflow definition:** Click the "`Save`" button, and the "Set DAG chart name" window pops up, as shown in the figure below. Enter the workflow definition name, workflow definition description, and set global parameters (optional, refer to [global parameters](https://dolphinscheduler.apache.org/en-us/docs/latest/user_doc/guide/parameter/global.html)), click the "Add" button to finish workflow definition creation.

![workflow-save](/img/new_ui/dev/project/workflow-save.png)

### Workflow Definition Action Function

Go to Workflow definition under Workflow menu to see list of all the workflows and their details.

![workflow-list](/img/new_ui/dev/project/workflow-list.png) 

Workflow running parameter description:

*   Failure strategy: When a task node fails to execute, other parallel task nodes need to execute the strategy. "Continue" means: After a task fails, other task nodes execute normally; "End" means: Terminate all tasks being executed, and terminate the entire process.
    
*   Notification strategy: When the process ends, send process execution information notification emails according to the process status, including no status, success, failure, success or failure.
    
*   Process priority: the priority of process operation, divided into five levels: the highest (HIGHEST), high (HIGH), medium (MEDIUM), low (LOW), the lowest (LOWEST). When the number of master threads is insufficient, processes with higher levels will be executed first in the execution queue, and processes with the same priority will be executed in the order of first-in, first-out.
    
*   Worker grouping: This process can only be executed in the specified worker machine group. The default is Default, which can be executed on any worker.
    
*   Notification Group: Select Notification Policy||Timeout Alarm||When fault tolerance occurs, process information or emails will be sent to all members in the notification group.
    
*   Recipient: Select Notification Policy||Timeout Alarm||When fault tolerance occurs, process information or alarm email will be sent to the recipient list.
    
*   Cc: Select Notification Policy||Timeout Alarm||When fault tolerance occurs, the process information or alarm email will be copied to the Cc list.
    
*   Startup parameters: Set or override the value of global parameters when starting a new process instance.
    
*   Complement: There are 2 modes of serial complement and parallel complement. Serial complement: within the specified time range, perform complements in sequence from the start date to the end date, and generate N process instances in turn; parallel complement: within the specified time range, perform multiple complements at the same time, and generate N process instances at the same time .
    
    *   Complement: Execute the workflow definition of the specified date, you can select the time range of the supplement (currently only supports the supplement for consecutive days), for example, the data from May 1st to May 10th needs to be supplemented, as shown in the following figure:

The operation functions of the workflow definition list are as follows:

 to

*   **Edit:** Only "offline" workflow definitions can be edited. Workflow DAG editing is the same as creating a workflow definition.
*   **Go Online:** When the workflow status is "Offline", the workflow goes online. Only the workflow in the "Online" status can run, but cannot be edited.
*   **Offline:** When the workflow status is "online", the offline workflow and the offline workflow can be edited but cannot be run.
*   **Run:** Only the online workflow can run. For operation steps, see Running Workflow
*   **Timing:** Only the online workflow can set the timing, and the system automatically schedules the workflow to run on a regular basis. The status after creating a timer is "offline", and the timer will take effect only when it is online on the timer management page. For timing operation steps, see Workflow Timing
*   **Timing management:** The timing management page can edit, go online/offline, and delete timings.
*   **Delete:** Delete the workflow definition.
*   **Download:** Download the workflow definition to the local.
*   **Tree Diagram:** Displays the type and task status of task nodes in a tree structure as shown in figure below:

![workflow-tree](/img/new_ui/dev/project/workflow-tree.png)

### Run Workflow

In Workflow definition page, click on Go Online button to go online. Click the "Start" button to pop up the startup parameter setting pop-up box, as shown in the figure below, set the startup parameters, click the "Run" button in the pop-up box, the workflow starts to run, and the workflow instance page generates a workflow instance.

![workflow-online](/img/new_ui/dev/project/workflow-online.png)

![workflow-run](/img/new_ui/dev/project/workflow-run.png)

Workflow running parameter description:

*   Failure strategy: When a task node fails to execute, other parallel task nodes need to execute the strategy. "Continue" means: After a task fails, other task nodes execute normally; "End" means: Terminate all tasks being executed, and terminate the entire process.
    
*   Notification strategy: When the process ends, send process execution information notification emails according to the process status, including no status, success, failure, success or failure.
    
*   Process priority: the priority of process operation, divided into five levels: the highest (HIGHEST), high (HIGH), medium (MEDIUM), low (LOW), the lowest (LOWEST). When the number of master threads is insufficient, processes with higher levels will be executed first in the execution queue, and processes with the same priority will be executed in the order of first-in, first-out.
    
*   Worker grouping: This process can only be executed in the specified worker machine group. The default is Default, which can be executed on any worker.
    
*   Notification Group: Select Notification Policy||Timeout Alarm||When fault tolerance occurs, process information or emails will be sent to all members in the notification group.
    
*   Recipient: Select Notification Policy||Timeout Alarm||When fault tolerance occurs, process information or alarm email will be sent to the recipient list.
    
*   Cc: Select Notification Policy||Timeout Alarm||When fault tolerance occurs, the process information or alarm email will be copied to the Cc list.
    
*   Startup parameters: Set or override the value of global parameters when starting a new process instance.
    
*   Complement: There are 2 modes of serial complement and parallel complement. Serial complement: within the specified time range, perform complements in sequence from the start date to the end date, and generate N process instances in turn; parallel complement: within the specified time range, perform multiple complements at the same time, and generate N process instances at the same time .
    
    *   Complement: Execute the workflow definition of the specified date, you can select the time range of the supplement (currently only supports the supplement for consecutive days), for example, the data from May 1st to May 10th needs to be supplemented, as shown in the following figure:

 ![workflow-date](/img/new_ui/dev/project/workflow-date.png)

*   *   > Serial mode: Complement numbers are executed sequentially from May 1st to May 10th, and ten process instances are generated in sequence on the process instance page;
        
        > Parallel mode: Execute tasks from May 1st to May 10th at the same time, and generate ten process instances on the process instance page at the same time.
        
          
        

### Workflow Timing

Create a schedule: Click Project Management->Workflow->Workflow Definition, enter the workflow definition page, go online with the workflow, click the "Scheduled" button <img src="/img/timing.png" width="35"/>, and the timing parameter setting pop-up box will pop up, as shown in the following figure:

![workflow-time01](/img/new_ui/dev/project/workflow-time01.png)

  

*   Select a start and end time. Within the start and end time range, the workflow is run regularly; outside the start and end time range, no timed workflow instance will be generated.
*   Add a timer that is executed once a day at 5 am, as shown in the following figure:

![workflow-time02](/img/new_ui/dev/project/workflow-time02.png)

*   Failure policy, notification policy, process priority, worker group, notification group, recipient, and CC are the same as workflow running parameters.
*   Click the "Create" button to create a timer successfully. At this time, the timer status is " **offline** ", and the timer needs **to be online** to take effect.
*   Scheduled online: Click the "Timing Management" button <img src="/img/timeManagement.png" width="35"/> to enter the timing management page, click the "Online" button, and the timing status will change to "Online", as shown in the figure below, the workflow timing will take effect.

  ![workflow-time03](/img/new_ui/dev/project/workflow-time03.png)

Import Workflow
---------------

Click Project Management->Workflow->Workflow Definition to enter the workflow definition page, click the "Import Workflow" button to import the local workflow file, the workflow definition list displays the imported workflow, and the status is offline.