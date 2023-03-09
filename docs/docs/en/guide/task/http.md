# HTTP Node

## Overview

This node is used to perform http type tasks such as the common POST and GET request types, and also supports http request validation and other functions.

## Create Task

- Click `Project Management -> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag the <img src="../../../../img/tasks/icons/http.png" width="15"/> from the toolbar to the drawing board.

## Task Parameters

[//]: # (TODO: use the commented anchor below once our website template supports this syntax)
[//]: # (- Please refer to [DolphinScheduler Task Parameters Appendix]&#40;appendix.md#default-task-parameters&#41; `Default Task Parameters` section for default parameters.)

- Please refer to [DolphinScheduler Task Parameters Appendix](appendix.md) `Default Task Parameters` section for default parameters.

|      **Parameter**      |                                                                        **Description**                                                                        |
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Request address         | HTTP request URL.                                                                                                                                             |
| Request type            | Supports GET, POST, HEAD, PUT, DELETE.                                                                                                                        |
| Request parameters      | Supports Parameter, Body, Headers.                                                                                                                            |
| Verification conditions | Supports default response code, custom response code, content included, content not included.                                                                 |
| Verification content    | When the verification condition selects a custom response code, the content contains, and the content does not contain, the verification content is required. |
| Custom parameter        | It is a user-defined parameter of http part, which will replace the content with `${variable}` in the script.                                                 |

## Task Output Parameters

| **Task Parameter** |           **Description**           |
|--------------------|-------------------------------------|
| response           | VARCHAR, http request return result |

Can use `${taskName.response}` to reference task output parameters in downstream tasks.

For example, if the current task1 is a http task, the downstream task can use `${task1.response}` to reference the output parameters of task1.

## Example

HTTP defines the different methods of interacting with the server, the most basic methods are GET, POST, PUT and DELETE. Here we use the http task node to demonstrate the use of POST to send a request to the system's login page to submit data.

The main configuration parameters are as follows:

- URL: Address to access the target resource. Here is the system's login page.
- HTTP Parameters:
  - userName: Username
  - userPassword: User login password

![http_task](../../../../img/tasks/demo/http_task01.png)

