# Workflow Instance

## View Workflow Instance

- Click Project Management -> Workflow -> Workflow Instance, enter the Workflow Instance page, as shown in the figure below:
     <p align="center">
        <img src="/img/instance-list-en.png" width="80%" />
     </p>
- Click the workflow name to enter the DAG view page, and check the task execution status, as shown in the figure below:
  <p align="center">
    <img src="/img/instance-runs-en.png" width="80%" />
  </p>

## View Task Log

- Enter the workflow instance page, click the workflow name, enter the DAG view page, double-click the task node, as shown in the figure below:
   <p align="center">
     <img src="/img/instanceViewLog-en.png" width="80%" />
   </p>
- Click "View Log", a log window pops up, as shown in the figure below, you can also view the task log on the task instance page, refer to [Task View Log](./task-instance.md)
   <p align="center">
     <img src="/img/task-log-en.png" width="80%" />
   </p>

## View Task History

- Click Project Management -> Workflow -> Workflow Instance, enter the workflow instance page, and click the workflow name to enter the workflow DAG page;
- Double-click the task node, as shown in the figure below, click "View History" to jump to the task instance page, and display a list of task instances running by the workflow instance
   <p align="center">
     <img src="/img/task_history_en.png" width="80%" />
   </p>

## View Operation Parameters

- Click Project Management -> Workflow -> Workflow Instance, enter the workflow instance page, and click the workflow name to enter the workflow DAG page;
- Click the icon in the upper left corner <img src="/img/run_params_button.png" width="35"/>，View the startup parameters of the workflow instance; click the icon <img src="/img/global_param.png" width="35"/>，View the global and local parameters of the workflow instance, as shown in the following figure:
   <p align="center">
     <img src="/img/run_params_en.png" width="80%" />
   </p>

## Workflow Instance Operation Function

Click Project Management -> Workflow -> Workflow Instance, enter the workflow instance page, as shown in the figure below:

  <p align="center">
    <img src="/img/instance-list-en.png" width="80%" />
  </p>

- **Edit：** only can edit terminated processes. Click the "Edit" button or the workflow instance name to enter the DAG edit page. After the edit, click the "Save" button to confirm, as shown in the figure below. In the pop-up box, check "Whether to update to workflow definition" to update the workflow definition; if not checked, no effect on the workflow definition
     <p align="center">
       <img src="/img/editDag-en.png" width="80%" />
     </p>
- **Rerun：** re-execute the terminated process
- **Recovery failed：** for failed processes, you can perform failure recovery operations, starting from the failed node
- **Stop：** to **stop** the running process, the background code will first `kill` the worker process, and then execute `kill -9` operation
- **Pause:** Perform a **pause** operation on the running process, the system status will change to **waiting for execution**, it will wait for the task to finish, and pause the next sequence task.
- **Resume pause:** to resume the paused process, start running directly from the **paused node**
- **Delete:** delete the workflow instance and the task instance under the workflow instance
- **Gantt chart:** the vertical axis of the Gantt chart is the topological sorting of task instances of the workflow instance, and the horizontal axis is the running time of the task instances, as shown in the figure below:
     <p align="center">
         <img src="/img/gantt-en.png" width="80%" />
     </p>
