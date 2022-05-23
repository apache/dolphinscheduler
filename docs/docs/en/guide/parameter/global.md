# Global Parameter

## Scope

Global parameters are parameters that are valid for all task nodes of the entire workflow. It can be configured on the workflow definition page.

## Usage

The specific use method can be determined according to the actual production situation. This example uses a shell task to print out the date value of yesterday.

### Create a Shell task

Create a shell task and enter `echo ${dt}` in the script content. In this case, dt is the global parameter we need to declare. As shown below:

![global-parameter01](/img/new_ui/dev/parameter/global_parameter01.png)

### Save the workflow and set global parameters

You could follow this guide to set global parameter: On the workflow definition page, click the plus sign to the right of "Set Global", after filling in the variable name and value, then save it

![global-parameter02](/img/new_ui/dev/parameter/global_parameter02.png)

> Note: The dt parameter defined here can be referenced by the local parameters of any other node.

### In task instance view execution result

On the task instance page, you can check the log to verify the execution result of the task and determine whether the parameters are valid.

![global-parameter03](/img/new_ui/dev/parameter/global_parameter03.png)
