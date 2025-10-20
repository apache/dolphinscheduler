# GRPC Node

## Overview

This node is used to execute gRPC tasks and supports checking gRPC status codes, SSL/TLS, and other features.

## Create Task

- Click Project Management -> Project Name -> Workflow Definition, then click the "Create Workflow" button to enter the DAG editing page:

- Drag the <img src="../../../../img/tasks/icons/grpc.png" width="15"/> task node from the toolbar to the canvas.

## Task Parameters

[//]: # (TODO: use the commented anchor below once our website template supports this syntax)
[//]: # (- For default parameter descriptions, please refer to [DolphinScheduler Task Parameter Appendix]&#40;appendix.md#默认任务参数&#41; under `Default Task Parameters`.)

- For default parameter descriptions, please refer to [DolphinScheduler Task Parameter Appendix](appendix.md) under `Default Task Parameters`.

|  **Task Parameter**  |                                                                         **Description**                                                                         |
|----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| gRPC Url             | gRPC request URL, must use the `hostname:port` format                                                                                                           |
| gRPC Credential Type | Supports None (insecure), and client default SSL/TLS credential types                                                                                           |
| Protobuf Definition  | Protobuf code defining the service in the `.proto` file, will be converted to JSON Descriptor when saved                                                        |
| gRPC Method          | The rpc method to call, must be defined in the service definition, written as `Greeter/SayHello` format                                                         |
| Message Content      | Request message defined in JSON, will be merged into the service definition when requesting                                                                     |
| gRPC Check Condition | Supports default gRPC status code (OK), custom status codes                                                                                                     |
| gRPC Condition       | When check condition is set to custom response code, must fill in validation content, must match [gRPC status codes](https://grpc.io/docs/guides/status-codes/) |
| Custom Parameters    | User-defined parameters local to gRPC, will replace content in the script like ${variable}                                                                      |

## Task Output Parameters

| **Task Parameter** |                              **Description**                               |
|--------------------|----------------------------------------------------------------------------|
| response           | VARCHAR, conforms to ProtoJS format, the return result of the gRPC request |

You can reference task output parameters in downstream tasks using ${taskName.response}.

For example, if the current task1 is a gRPC task, downstream tasks can use `${task1.response}` to reference the output parameter of task1.

## Task Example

gRPC definition
Main configuration parameters are as follows (all parameters can be replaced by built-in parameters):

- gRPC Url: The address of the target gRPC service, here is the local 50051 port.
- gRPC Credential Type: Insecure
- Protobuf Definition: The protobuf definition used by the gRPC service
- gRPC Method: The rpc method to call, in `Greeter/SayHello` format
- Message: Request message defined in JSON
- gRPC Check Condition: Default gRPC status code OK, custom status code
- gRPC Condition: When the validation condition is a custom status code, must fill in the validation content, which should exactly match the gRPC status code string

![grpc_task](../../../../img/tasks/demo/grpc_task01.png)
![grpc_task](../../../../img/tasks/demo/grpc_task02.png)

