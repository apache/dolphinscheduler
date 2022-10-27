# Apache Zeppelin

## Overview

Use `Zeppelin Task` to create a zeppelin-type task and execute zeppelin notebook paragraphs. When the worker executes `Zeppelin Task`,
it will call `Zeppelin Client API` to trigger zeppelin notebook paragraph. Click [here](https://zeppelin.apache.org/) for details about `Apache Zeppelin Notebook`.

## Create Task

- Click `Project Management -> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag <img src="../../../../img/tasks/icons/zeppelin.png" width="15"/> from the toolbar to the canvas.

## Task Parameters

[//]: # (TODO: use the commented anchor below once our website template supports this syntax)
[//]: # (- Please refer to [DolphinScheduler Task Parameters Appendix]&#40;appendix.md#default-task-parameters&#41; `Default Task Parameters` section for default parameters.)

- Please refer to [DolphinScheduler Task Parameters Appendix](appendix.md) `Default Task Parameters` section for default parameters.

|           **Parameter**            |                                                          **Description**                                                           |
|------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| Zeppelin Note ID                   | The unique note id for a zeppelin notebook note.                                                                                   |
| Zeppelin Paragraph ID              | The unique paragraph id for a zeppelin notebook paragraph. If you want to schedule a whole note at a time, leave this field blank. |
| Zeppelin Production Note Directory | The directory for cloned note in production mode.                                                                                  |
| Zeppelin Rest Endpoint             | The REST endpoint of your zeppelin server                                                                                          |
| Zeppelin Parameters                | Parameters in json format used for zeppelin dynamic form.                                                                          |

## Production (Clone) Mode

- Fill in the optional `Zeppelin Production Note Directory` parameter to enable `Production Mode`.
- In `Production Mode`, the target note gets copied to the `Zeppelin Production Note Directory` you choose.
  `Zeppelin Task Plugin` will execute the cloned note instead of the original one. Once execution done,
  `Zeppelin Task Plugin` will delete the cloned note automatically.
  Therefore, it increases the stability as the modification to a running note triggered by `Dolphin Scheduler`
  will not affect the production task.
- If you leave the `Zeppelin Production Note Directory` empty, `Zeppelin Task Plugin` will execute the original note.
- 'Zeppelin Production Note Directory' should both start and end with a `slash`. e.g. `/production_note_directory/`

## Task Example

### Zeppelin Paragraph Task Example

This example illustrates how to create a zeppelin paragraph task node.

![demo-zeppelin-paragraph](../../../../img/tasks/demo/zeppelin.png)

![demo-get-zeppelin-id](../../../../img/tasks/demo/zeppelin_id.png)

