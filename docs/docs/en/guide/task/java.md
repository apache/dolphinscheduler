# overview

This node is used to perform java-type tasks and supports the use of single files and jar packages as program entries.

# create tasks

- click on project management-> Project name-> workflow definition, click on the“Create workflow” button, go to the DAG edit page:

- drag the toolbar's Java task node to the palette.

# task parameters

- node name: the name of the set task. The node name in a workflow definition is unique.

- run flag: indicates whether the node is scheduled properly and, if it is not needed, turns on the kill switch.

- description: describes the functionality of the node.

- task priority: when the number of worker threads is insufficient, the worker is executed according to the priority from high to low. When the priority is the same, the worker is executed according to the first in, first out principle.

- Worker group: The machine whose task is assigned to the Worker group executes, and selecting Default will randomly select a Worker machine to execute.

- environment name: configure the environment in which the task runs.

- number of failed retries: number of resubmitted tasks that failed, supported drop-down and hand-fill.

- failed retry interval: the interval between tasks that fail and are resubmitted, supported by drop-down and hand-fill.

- delayed execution time: the amount of time a task is delayed, in units.

- timeout alarm: Check timeout warning, timeout failure, when the task exceeds the“Timeout length”, send a warning message and the task execution fails.

- module path: turn on the use of Java 9 + 's modularity feature, put all resources into-module-path, and require that the JDK version in your worker support modularity.

- main parameter: as a normal Java program main method entry parameter.

- java vm parameters: configure startup virtual machine parameters.

- script: you need to write Java code if you use the Java run type. The public class must exist in the code without writing a package statement.

- resources: these can be external JAR packages or other resource files that are added to the Classpath or module path and can be easily retrieved in your JAVA script.

- custom parameter: a user-defined parameter that is part of HTTP and replaces the contents of the script with the ${ variable } .

- pre tasks: selecting a pre-task for the current task sets the selected pre-task upstream of the current task.

Sample # # task

HTTP defines the different methods of interacting with the server, and the four most basic methods are GET, POST, PUT, and DELETE. Here we use the HTTP task node to demonstrate the use of POST to send a request to the system's login page and submit data.

The main configuration parameters are as follows:

- run type

- module path

- main parameters

- java vm parameters

- script 

![java_task](../../../../img/tasks/demo/java_task02.png)

## 注意事项

When you run a type with JAVA, the public class must exist in the code, and you can not write a package statement.
