# Parameter Context

DolphinScheduler provides the ability to refer to each other between parameters, including local parameters refer to global parameters and upstream and downstream parameter transfer. Due to the existence of references, it involves the priority of parameters when the parameter names are the same. see also [Parameter Priority](priority.md)

## Local Task Refers to Global Parameter

The premise of local tasks referring global parameters is that you have already defined [Global Parameter](global.md). The usage is similar to the usage in [local parameters](local.md), but the value of the parameter needs to be configured as the key of the global parameter.

## Pass Parameter From Upstream Task to Downstream

DolphinScheduler allows parameter transfer between tasks. Currently, transfer direction only supports one-way transfer from upstream to downstream. The task types that support this feature are：

* [Shell](../task/shell.md)
* [SQL](../task/sql.md)
* [Procedure](../task/stored-procedure.md)
* [Python](../task/python.md)
* [SubProcess](../task/sub-process.md)

When defining an upstream node, if there is a need to transmit the result of that node to a dependency related downstream node. You need to set an `OUT` direction parameter to [Custom Parameters] of the [Current Node Settings]. If it is a SubProcess node, there is no need to set a parameter in [Current Node Settings], but an `OUT` direction parameter needs to be set in the workflow definition of the subprocess.

The value of upstream parameter can be updated in downstream node in the same way as [setting parameter](#create-a-shell-task-and-set-parameters).

Upstream parameter will be override when defining parameter with the same name in downstream node.

> Note: If there are no dependencies between nodes, local parameters cannot be passed upstream.

### Example

This sample shows how to use the parameter passing function. Create local parameters and assign them to downstream through the SHELL task. The SQL task completes the query operation by obtaining the parameters of the upstream task.

#### Create a SHELL task and set parameters

The user needs to pass the parameter when creating the shell script, the output statement format is `'${setValue(key=value)}'`, the key is the `prop` of the corresponding parameter, and value is the value of the parameter.

Create a Node_A task, add output and value parameters to the custom parameters, and write the following script:

![context-parameter01](../../../../img/new_ui/dev/parameter/context_parameter01.png)

Parameter Description:

- value: The direction selection is IN, and the value is 66
- output: The direction is selected as OUT, assigned through the script`'${setValue(output=1)}'`, and passed to the downstream parameters

When the SHELL node is defined, the log detects the format of `${setValue(output=1)}`, it will assign 1 to output, and the downstream node can directly use the value of the variable output. Similarly, you can find the corresponding node instance on the [Workflow Instance] page, and then you can view the value of this variable.

Create the Node_B task, which is mainly used to test and output the parameters passed by the upstream task Node_A.

![context-parameter02](../../../../img/new_ui/dev/parameter/context_parameter02.png)

#### Create SQL tasks and use parameters

When the SHELL task is completed, we can use the output passed upstream as the query object for the SQL. The id of the query is renamed to ID and is output as a parameter.

![context-parameter03](../../../../img/new_ui/dev/parameter/context_parameter03.png)

> Note: If the result of the SQL node has only one row, one or multiple fields, the name of the `prop` needs to be the same as the field name. The data type can choose structure except `LIST`. The parameter assigns the value according to the same column name in the SQL query result.
>
> If the result of the SQL node has multiple rows, one or more fields, the name of the `prop` needs to be the same as the field name. Choose the data type structure as `LIST`, and the SQL query result will be converted to `LIST<VARCHAR>`, and forward to convert to JSON as the parameter value.

#### Save the workflow and set the global parameters

Click on the Save workflow icon and set the global parameters output and value.

![context-parameter04](../../../../img/new_ui/dev/parameter/context_parameter04.png)

#### View results

After the workflow is created, run the workflow online and view its running results.

The result of Node_A is as follows:

![context-log01](../../../../img/new_ui/dev/parameter/context_log01.png)

The result of Node_B is as follows:

![context-log02](../../../../img/new_ui/dev/parameter/context_log02.png)

The result of Node_mysql is as follows:

![context-log03](../../../../img/new_ui/dev/parameter/context_log03.png)

Even though output is assigned a value of 1 in Node_A's script, the log still shows a value of 100. But according to the principle from [parameter priority](priority.md): `Startup Parameter > Local Parameter > Parameter Context > Global Parameter`, the output value in Node_B is 1. It proves that the output parameter is passed in the workflow with reference to the expected value, and the query operation is completed using this value in Node_mysql.

But the output value 66 only shows in the Node_A, the reason is that the direction of value is selected as IN, and only when the direction is OUT will it be defined as a variable output.

#### Pass parameter from Python task to downstream

Use `print('${setValue(key=%s)}' % value)`, DolphinScheduler will capture the `${setValue(key=value}` in the output to capture the parameters and pass them downstream.

For example

![python_context_param](../../../../img/new_ui/dev/parameter/python_context_param.png)

#### Pass parameter from SubProcess task to downstream

In the workflow definition of the subprocess, define `OUT` direction parameters as output parameters, and these parameters can be passed to the downstream tasks of the subprocess node.

Create an A task in the workflow definition of the subprocess, add var1 and var2 parameters to the custom parameters, and write the following script:

![context-subprocess01](../../../../img/new_ui/dev/parameter/context-subprocess01.png)

Save the subprocess_example1 workflow and set the global parameters var1.

![context-subprocess02](../../../../img/new_ui/dev/parameter/context-subprocess02.png)

Create a sub_process task in a new workflow, and use the subprocess_example1 workflow as the sub-node.

![context-subprocess03](../../../../img/new_ui/dev/parameter/context-subprocess03.png)

Create a shell task as a downstream task of the sub_process task, and write the following script:

![context-subprocess04](../../../../img/new_ui/dev/parameter/context-subprocess04.png)

Save the workflow and run it. The result of the downstream task is as follows:

![context-subprocess05](../../../../img/new_ui/dev/parameter/context-subprocess05.png)

Although the two parameters var1 and var2 are output in the A task, only the `OUT` parameter var1 is defined in the workflow definition, and the downstream task successfully outputs var1. It proves that the var1 parameter is passed in the workflow with reference to the expected value.
