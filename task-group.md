# Task Group Management
The task group is mainly used to control the concurrency of task instances, and is designed to control the pressure of other resources (it can also control the pressure of the Hadoop cluster, but the cluster will have queue control). When creating a new task definition, you can configure the corresponding task group and configure the priority of the task running in the task group.

## Task group configuration

### Create a new task group

![taskGroup](/img/new_ui/dev/resource/taskGroup.png)

The user clicks `Resource Center` -> `Task Group Management` -> `Task Group Configuration` -> `New Task Group`

![create-taskGroup](/img/new_ui/dev/resource/create-taskGroup.png)

You need to enter the following information:

`Task group name`: The name displayed when the task group is used

`Project name`: The project that the task group functions, this item is optional, if not selected, all the projects in the whole system can use this task group.

`Resource capacity`: The maximum number of concurrent task instances allowed

### View task group queue

![view-queue](/img/new_ui/dev/resource/view-queue.png)

Click the button to view task group usage information

![view-queue](/img/new_ui/dev/resource/view-groupQueue.png)

### Use of task groups

Note: The use of task groups is applicable to tasks executed by workers, such as \[switch\] nodes, \[condition\] nodes, \[sub\_process\] and other node types executed by the master are not controlled by the task group.

Let's take the shell node as an example:

![use-queue](/img/new_ui/dev/resource/use-queue.png)

Regarding the configuration of the task group, all you need to do is to configure the part in the red box, where:

`Task group name`: The task group name displayed on the task group configuration page. Here you can only see the task group that the project has permission to (the project is selected when creating a task group), or the task group that acts globally (the new task group is created). when no item is selected)

`Intra-group priority`: When there is a waiting resource, the task with high priority will be distributed to the worker by the master first. The larger the value of this part, the higher the priority.

## Implementation logic of task group

### Get task group resources:

The master judges whether the task is configured with a task group when distributing the task. If the task is not configured, it is normally thrown to the worker to run; if the task group is configured, it checks whether the remaining size of the task group resource pool meets the current task operation before throwing it to the worker for execution. , if the resource pool -1 is satisfied, continue to run; if not, exit the task distribution and wait for other tasks to wake up.

### Release and wake up:

When the task that has obtained the task group resource ends, the task group resource will be released. After the release, it will check whether there is a task waiting in the current task group. If there is, mark the task with the best priority to run, and create a new executable event. . The event stores the task id that is marked to obtain the resource, and then obtains the task group resource and then runs it.

### Task Group Flowchart

![task_group](/img/task_group_process.png)