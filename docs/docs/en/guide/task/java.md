# Overview

This node is used to perform java-type tasks and supports the use of single files and jar packages as program entries.

# Create Tasks

- Click on project management-> Project name-> workflow definition, click on the“Create workflow” button, go to the DAG edit page:

- Drag the toolbar's Java task node to the palette.

# Task Parameters

- Node Name: the name of the set task. The node name in a workflow definition is unique.

- Run Flag: indicates whether the node is scheduled properly and, if it is not needed, turns on the kill switch.

- Description: describes the functionality of the node.

- Task Priority: when the number of worker threads is insufficient, the worker is executed according to the priority from high to low. When the priority is the same, the worker is executed according to the first in, first out principle.

- Worker Group: The machine whose task is assigned to the Worker group executes, and selecting Default will randomly select a Worker machine to execute.

- Environment Name: configure the environment in which the task runs.

- Number Of Failed Retries: number of resubmitted tasks that failed, supported drop-down and hand-fill.

- Failed Retry Interval: the interval between tasks that fail and are resubmitted, supported by drop-down and hand-fill.

- Delayed Execution Time: the amount of time a task is delayed, in units.

- Timeout Alarm: Check timeout warning, timeout failure, when the task exceeds the“Timeout length”, send a warning message and the task execution fails.

- Module Path: turn on the use of Java 9 + 's modularity feature, put all resources into-module-path, and require that the JDK version in your worker support modularity.

- Main Parameter: as a normal Java program main method entry parameter.

- Java VM Parameters: configure startup virtual machine parameters.

- Script: you need to write Java code if you use the Java run type. The public class must exist in the code without writing a package statement.

- Resources: these can be external JAR packages or other resource files that are added to the Classpath or module path and can be easily retrieved in your JAVA script.

- Custom parameter: a user-defined parameter that is part of HTTP and replaces the contents of the script with the ${ variable } .

- Pre Tasks: selecting a pre-task for the current task sets the selected pre-task upstream of the current task.

## Example

HTTP defines the different methods of interacting with the server, and the four most basic methods are GET, POST, PUT, and DELETE. Here we use the HTTP task node to demonstrate the use of POST to send a request to the system's login page and submit data.

The main configuration parameters are as follows:

- Run Type

- Module Path

- Main Parameters

- Java VM Parameters

- Script 

![java_task](../../../../img/tasks/demo/java_task02.png)

## Notice

When you run a type with JAVA, the public class must exist in the code, and you can not write a package statement.
