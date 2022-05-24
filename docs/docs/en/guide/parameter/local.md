# Local Parameter

## Scope

Parameters configured on the task definition page, the scope of this parameter is inside this task only. But if you configure according to [Refer to Parameter Context](context.md), it could pass to downstream tasks.

## Usage

Usage of local parameters is: at the task define page, click the '+' beside the 'Custom Parameters' and fill in the key and value to save.

## Examples

This example shows how to use local parameters to print the current date. Create a Shell task and write a script with the content `echo ${dt}`. Click **custom parameter** in the configuration bar, and the configuration is as follows:

![local-parameter01](/img/new_ui/dev/parameter/local_parameter01.png)

Parameters:

- dt: indicates the parameter name
- in: IN indicates that local parameters can only be used on the current node, and OUT indicates that local parameters can be transmitted to the downstream
- DATE: indicates the DATE of the data type
- $[YYYY-MM-DD] : indicates a built-in parameter derived from a user-defined format

Save the workflow and run it. View Shell task's log.

![local-parameter02](/img/new_ui/dev/parameter/local_parameter02.png)

> Note: The local parameter can be used in the workflow of the current task node. If it is set to OUT, it can be passed to the downstream workflow. Please refer to: [Parameter Context](context.md)
