Click `Project Management -> Workflow -> Workflow Instance` to enter the Workflow Instance page, as shown in the following figure:

![workflow-instance](/img/new_ui/dev/project/workflow-instance.png)

Click the workflow name to enter the DAG view page to view the task execution status, as shown in the following figure:

![instance-state](/img/new_ui/dev/project/instance-state.png)

View Task Log
-------------

Enter the workflow instance page, click the workflow name, enter the DAG view page, and double-click the task node, as shown in the following figure:

![instance-log01](/img/new_ui/dev/project/instance-log01.png)

Click "`View Log`", a log pop-up box will pop up, as shown in the figure below, you can also view the task log on the task instance page.

![instance-log02](/img/new_ui/dev/project/instance-log02.png)

View Task History
-----------------

*   Click `Project Management->Workflow->Workflow Instance` to enter the workflow instance page, click the workflow name to enter the workflow DAG page
*   Double-click the task node, click "`View History`" to jump to the task instance page, and display the list of task instances run by the workflow instance

![instance-history](/img/new_ui/dev/project/instance-history.png)

View Running Parameters
-----------------------

*   Click `Project Management->Workflow->Workflow Instance` to enter the workflow instance page, click the workflow name to enter the workflow DAG page;
*   Click the icon in the upper left corner ![](attachments/2798136/2798134.png)to view the startup parameters of the workflow instance; click the icon ![](attachments/2798136/2798133.png)to view the global parameters and local parameters of the workflow instance, as shown in the following figure:

![instance-parameter](/img/new_ui/dev/project/instance-parameter.png)

  

Workflow Instance Action Function
---------------------------------

Click `Project Management -> Workflow -> Workflow Instance` to enter the Workflow Instance page.

![workflow-instance](/img/new_ui/dev/project/workflow-instance.png)

*   **Edit:** Only a terminated process can be edited. Click the "`Edit`" button or the name of the workflow instance to enter the DAG editing page, click the "`Save`" button after editing, and a pop-up box to save the DAG will pop up, as shown in the figure below, check "Update to workflow definition" in the pop-up box, save After that, the workflow definition will be updated; if not checked, the workflow definition will not be updated.
    
    ![](/img/editDag.png)
    
*   **Rerun:** Re-execute a terminated process.
*   **Recovery Failure:** For the failed process, the recovery failure operation can be performed, starting from the failed node.
*   **Stop: Stop** **the** running process , the background will first kill worker process, and then perform the kill operation
*   **Pause: Pause** **the** running process , the system state changes to **waiting for execution** , it will wait for the task being executed to end, and suspend the next task to be executed.
*   **Resume Suspension: resume** the suspended process and start running directly from the **suspended node**
*   **Delete:** Delete the workflow instance and the task instance under the workflow instance
*   **Gantt Chart:** The vertical axis of the Gantt chart is the topological ordering of task instances under a certain workflow instance, and the horizontal axis is the running time of the task instances, as shown in the figure:

![instance-gantt](/img/new_ui/dev/project/instance-gantt.png)