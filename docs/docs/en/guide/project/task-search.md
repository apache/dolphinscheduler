# Task Search

Open a project and select **Task → Task Search**. Enter part of a task name and click **Search**, or press Enter, to find the workflows that contain matching tasks in that project. Clear the input to list all tasks referenced by the project's current workflow definitions.

The paginated results show the task name, task type, and owning workflow. Click the workflow name to open its definition. A task with multiple upstream dependencies appears once per workflow; a task referenced by multiple workflows has one result for each workflow.

Search uses the task versions referenced by the current workflow definitions. It includes tasks that have never run and tasks whose execution instances have been removed. Historical workflow versions, deleted workflows, and tasks without an owning workflow are excluded. The page only searches definitions and provides no standalone task creation, editing, or execution actions. Users with project read permission can search.

Task names are matched by literal substring: `_`, `%`, and `!` are treated as ordinary characters.

## REST API

`GET /projects/{projectCode}/task-definition/search`

| Parameter | Required | Description |
| --- | --- | --- |
| `projectCode` | Yes | Project code in the URL; the user must have access to the project. |
| `searchVal` | No | Task name substring. Empty or omitted values list all matching project tasks. |
| `pageNo` | Yes | Page number, starting at 1. |
| `pageSize` | Yes | Positive number of results per page. |

The response follows the standard `Result<PageInfo>` format. `data.total` is the number of task/workflow pairs, and `data.totalList` contains `taskCode`, `taskName`, `taskType`, `workflowDefinitionCode`, and `workflowDefinitionName`.
