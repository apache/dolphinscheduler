# Parameter Context

DolphinScheduler provides the ability to refer to each other between parameters, including local parameters refer to global parameters and upstream and downstream parameter transfer. Due to the existence of references, it involves the priority of parameters when the parameter names are the same. See also, [Parameter Priority](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/parameter/priority.html).

Local Task Refers to Global Parameter
-------------------------------------

The premise of local tasks referring global parameters is that you have already defined [Global Parameter](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/parameter/global.html). The usage is similar to the usage in [local parameters](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/parameter/local.html), but the value of the parameter needs to be configured as the key of the global parameter.

Pass Parameter From Upstream Task to Downstream
-----------------------------------------------

DolphinScheduler allows parameter transfer between tasks. Currently, transfer direction only supports one-way transfer from upstream to downstream. The task types that support this feature are：

*   [Shell](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/task/shell.html)
*   [SQL](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/task/sql.html)
*   [Procedure](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/task/stored-procedure.html)

When defining an upstream node, if there is a need to transmit the result of that node to a dependency related downstream node. You need to set an `OUT` direction parameter to \[Custom Parameters\] of the \[Current Node Settings\]. At present, we mainly focus on the SQL and shell nodes to pass parameters downstream.

> Note: If there are no dependencies between nodes, local parameters cannot be passed upstream.

### Example

This sample shows how to use the parameter passing function. Create local parameters and assign them to downstream through the SHELL task. The SQL task completes the query operation by obtaining the parameters of the upstream task.

#### Create a SHELL task and set parameters

The user needs to pass the parameter when creating the shell script, the output statement format is `'${setValue(key=value)}'`, the key is the `prop` of the corresponding parameter, and value is the value of the parameter.

Create a Node\_A task, add output and value parameters to the custom parameters, and write the following script:

![context-parameter01](/img/new_ui/dev/parameter/context_parameter01.png)

Parameter Description:

*   value: The direction selection is IN, and the value is 66
*   output: The direction is selected as OUT, assigned through the script`'${setValue(output=1)}'`, and passed to the downstream parameters

When the SHELL node is defined, the log detects the format of `${setValue(output=1)}`, it will assign 1 to output, and the downstream node can directly use the value of the variable output. Similarly, you can find the corresponding node instance on the \[Workflow Instance\] page, and then you can view the value of this variable.

Create the Node\_B task, which is mainly used to test and output the parameters passed by the upstream task Node\_A.

![context-parameter02](/img/new_ui/dev/parameter/context_parameter02.png)

#### Create SQL tasks and use parameters

When the SHELL task is completed, we can use the output passed upstream as the query object for the SQL. The id of the query is renamed to ID and is output as a parameter.

![context-parameter03](/img/new_ui/dev/parameter/context_parameter03.png)

> Note: If the result of the SQL node has only one row, one or multiple fields, the name of the `prop` needs to be the same as the field name. The data type can choose structure except `LIST`. The parameter assigns the value according to the same column name in the SQL query result.
> 
> If the result of the SQL node has multiple rows, one or more fields, the name of the `prop` needs to be the same as the field name. Choose the data type structure as `LIST`, and the SQL query result will be converted to `LIST<VARCHAR>`, and forward to convert to JSON as the parameter value.

#### Save the workflow and set the global parameters

Click on the Save workflow icon and set the global parameters output and value.

![context-parameter03](/img/new_ui/dev/parameter/context_parameter04.png)

#### View results

After the workflow is created, run the workflow online and view its running results.

The result of Node\_A is as follows:

![context-log01](/img/new_ui/dev/parameter/context_log01.png)

The result of Node\_B is as follows:

![context-log02](/img/new_ui/dev/parameter/context_log02.png)

The result of Node\_mysql is as follows:

![context-log03](/img/new_ui/dev/parameter/context_log03.png)

Even though output is assigned a value of 1 in Node\_A's script, the log still shows a value of 100. But according to the principle from [parameter priority](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/parameter/priority.html): `Local Parameter > Parameter Context > Global Parameter`, the output value in Node\_B is 1. It proves that the output parameter is passed in the workflow with reference to the expected value, and the query operation is completed using this value in Node\_mysql.

But the output value 66 only shows in the Node\_A, the reason is that the direction of value is selected as IN, and only when the direction is OUT will it be defined as a variable output.