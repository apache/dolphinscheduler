# HTTP Node

## Overview

This node is used to perform http type tasks such as the common POST and GET request types, and also supports http request validation and other functions.

## Create Task

- Click `Project -> Management-Project -> Name-Workflow Definition`, and click the "Create Workflow" button to enter the DAG editing page.
- Drag the <img src="/img/tasks/icons/http.png" width="15"/> from the toolbar to the drawing board.

## Task Parameter

- **Node name**: The node name in a workflow definition is unique.
- **Run flag**: Identifies whether this node can be scheduled normally, if it does not need to be executed, you can turn on the prohibition switch.
- **Descriptive information**: describe the function of the node.
- **Task priority**: When the number of worker threads is insufficient, they are executed in order from high to low, and when the priority is the same, they are executed according to the first-in first-out principle.
- **Worker grouping**: Tasks are assigned to the machines of the worker group to execute. If Default is selected, a worker machine will be randomly selected for execution.
- **Environment Name**: Configure the environment name in which to run the script.
- **Number of failed retry attempts**: The number of times the task failed to be resubmitted.
- **Failed retry interval**: The time, in cents, interval for resubmitting the task after a failed task.
- **Delayed execution time**: the time, in cents, that a task is delayed in execution.
- **Timeout alarm**: Check the timeout alarm and timeout failure. When the task exceeds the "timeout period", an alarm email will be sent and the task execution will fail.
- **Request address**: HTTP request URL.
- **Request type**: Support GET, POSt, HEAD, PUT, DELETE.
- **Request parameters**: Support Parameter, Body, Headers.
- **Verification conditions**: support default response code, custom response code, content included, content not included.
- **Verification content**: When the verification condition selects a custom response code, the content contains, and the content does not contain, the verification content is required.
- **Custom parameter**: It is a user-defined parameter of http part, which will replace the content with `${variable}` in the script.
- **Predecessor task**: Selecting a predecessor task for the current task will set the selected predecessor task as upstream of the current task.

## Example

HTTP defines the different methods of interacting with the server, the most basic methods are GET, POST, PUT and DELETE. Here we use the http task node to demonstrate the use of POST to send a request to the system's login page to submit data.

The main configuration parameters are as follows:

- URL: Address to access the target resource. Here is the system's login page.
- HTTP Parameters:
     - userName: Username
     - userPassword: User login password

![http_task](/img/tasks/demo/http_task01.png)

## Notice

None
