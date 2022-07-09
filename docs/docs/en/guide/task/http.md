# HTTP Node

## Overview

This node is used to perform http type tasks such as the common POST and GET request types, and also supports http request validation and other functions.

## Create Task

-  Click `Project Management -> Project Name -> Workflow Definition`, and click the `Create Workflow` button to enter the DAG editing page.
- Drag the <img src="../../../../img/tasks/icons/http.png" width="15"/> from the toolbar to the drawing board.

## Task Parameters

| **Parameter** | **Description** |
| ------- | ---------- |
| Node Name | Set the name of the task. Node names within a workflow definition are unique. |
| Run flag | Indicates whether the node can be scheduled normally. If it is not necessary to execute, you can turn on the prohibiting execution switch. |
| Description | Describes the function of this node. |
| Task priority | When the number of worker threads is insufficient, they are executed in order from high to low according to the priority, and they are executed according to the first-in, first-out principle when the priority is the same. |
| Worker group | The task is assigned to the machines in the worker group for execution. If Default is selected, a worker machine will be randomly selected for execution. |
| Task group name | The group in Resources, if not configured, it will not be used. | 
| Environment Name | Configure the environment in which to run the script. |
| Number of failed retries | The number of times the task is resubmitted after failure. It supports drop-down and manual filling. | 
| Failure Retry Interval | The time interval for resubmitting the task if the task fails. It supports drop-down and manual filling. | 
| Timeout alarm | Check Timeout Alarm and Timeout Failure. When the task exceeds the "timeout duration", an alarm email will be sent and the task execution will fail. |
| Request address | HTTP request URL. |
| Request type | Supports GET, POSt, HEAD, PUT, DELETE. || Request parameters |Supports Parameter, Body, Headers. || Verification conditions | Supports default response code, custom response code, content included, content not included.|
| Verification content | When the verification condition selects a custom response code, the content contains, and the content does not contain, the verification content is required. |
| Custom parameter | It is a user-defined parameter of http part, which will replace the content with `${variable}` in the script. |
| Pre tasks | Selecting a predecessor task for the current task will set the selected predecessor task as upstream of the current task. |

## Example

HTTP defines the different methods of interacting with the server, the most basic methods are GET, POST, PUT and DELETE. Here we use the http task node to demonstrate the use of POST to send a request to the system's login page to submit data.

The main configuration parameters are as follows:

- URL: Address to access the target resource. Here is the system's login page.
- HTTP Parameters:
     - userName: Username
     - userPassword: User login password

![http_task](../../../../img/tasks/demo/http_task01.png)

