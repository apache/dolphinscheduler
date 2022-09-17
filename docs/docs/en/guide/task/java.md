# Overview

This node is for executing java-type tasks and supports using files and jar packages as program entries.

# Create Tasks

- Click on `Project Management` -> `Project Name` -> `Workflow Definition`, click on the “Create workflow” button, go to the DAG edit page:

- Drag the toolbar's Java task node to the palette.

# Task Parameters

|      **Parameter**       |                                                                              **Description**                                                                               |
|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Node Name                | The name of the set task. The node name in a workflow definition is unique.                                                                                                |
| Run Flag                 | Indicates whether the node is scheduled properly and turns on the kill switch, if not needed.                                                                              |
| Description              | Describes the functionality of the node.                                                                                                                                   |
| Task Priority            | When the number of worker threads is insufficient, the worker executes tasks according to the priority. When the priority is the same, the worker executes tasks by order. |
| Worker Group             | The group of machines who execute the tasks. If selecting `Default`, DolphinScheduler will randomly choose a worker machine to execute the task.                           |
| Environment Name         | Configure the environment in which the task runs.                                                                                                                          |
| Number Of Failed Retries | Number of resubmitted tasks that failed. You can choose the number in the drop-down menu or fill it manually.                                                              |
| Failed Retry Interval    | the interval between the failure and resubmission of a task. You can choose the number in the drop-down menu or fill it manually.                                          |
| Delayed Execution Time   | the amount of time a task is delayed, in units.                                                                                                                            |
| Timeout Alarm            | Check timeout warning, timeout failure, when the task exceeds the“Timeout length”, send a warning message and the task execution fails.                                    |
| Module Path              | pick Java 9 + 's modularity feature, put all resources into-module-path, and require that the JDK version in your worker supports modularity.                              |
| Main Parameter           | Java program main method entry parameter.                                                                                                                                  |
| Java VM Parameters       | JVM startup parameters.                                                                                                                                                    |
| Script                   | You need to write Java code if you use the Java run type. The public class must exist in the code without writing a package statement.                                     |
| Resources                | External JAR packages or other resource files that are added to the classpath or module path and can be easily retrieved in your JAVA script.                              |
| Custom parameter         | A user-defined parameter that is part of HTTP and replaces `${ variable }` in the script .                                                                                 |
| Pre Tasks                | Selects a pre-task for the current task and sets the pre-task as the upstream of the current task.                                                                         |

## Example

Java type tasks have two modes of execution, here is a demonstration of executing tasks in Java mode.

The main configuration parameters are as follows:
- Run Type
- Module Path
- Main Parameters
- Java VM Parameters
- Script

![java_task](../../../../img/tasks/demo/java_task02.png)

## Note

When you run the task in JAVA execution mode, the public class must exist in the code, and you could omit writing a package statement.
