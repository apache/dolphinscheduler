# External System Task

The External System task is used to call external system APIs and integrate with third-party systems. This task type allows DolphinScheduler to interact with external services, execute remote operations, and retrieve results.

## Introduction

External System tasks provide a way to execute operations in external systems through REST APIs calls. These tasks can be configured to authenticate with external systems, send requests, and process responses.

## Configuration

- **Task Name**: The name of the task.
- **Task Type**: Select "External System" from the task type list.
- **External System**: Select the external system to connect to.
- **API Endpoint**: The API endpoint to call in the external system.
- **Authentication**: Configure authentication parameters for the external system.
- **Request Method**: The HTTP method to use (GET, POST, PUT, DELETE, etc.).
- **Request Parameters**: Parameters to send with the request.
- **Response Processing**: How to process the response from the external system.

## Example

![External System Task](../../../../img/tasks/icons/external_system.png)

This is an example of how to configure an External System task in the workflow.
