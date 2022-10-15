# Task Definition

## Batch Task Definition

Task definition allows to modify or operate tasks at the task level rather than modifying them in the workflow definition.
We already have workflow level task editor in [workflow definition](workflow-definition.md) which you can click the specific
workflow and then edit its task definition. It is depressing when you want to edit the task definition but do not remember
which workflow it belongs to. So we decide to add `Task Definition` view under `Task` menu.

![task-definition](../../../../img/new_ui/dev/project/batch-task-definition.png)

In this view, you can create, query, update, delete task definition by click the related button in `operation` column. The
most exciting thing is you could query task by task name in the wildcard, and it is useful when you only remember the task
name but forget which workflow it belongs to. It is also supported query by the task name alone with `Task Type` or
`Workflow Name`

## Stream Task Definition

Stream task definitions are created in the workflow definition, and can be modified and executed.

![task-definition](../../../../img/new_ui/dev/project/stream-task-definition.png)

Click the execute button, check the execution parameters and click Confirm to submit the stream task.

![task-definition](../../../../img/new_ui/dev/project/stream-task-execute.png)
