# Refer to Parameter Context

DolphinScheduler provides the ability to refer to each other between parameters, including local parameters refer to global parameters and upstream and downstream parameter transfer. Due to the existence of references, it involves the priority of parameters when the parameter names are the same. see also [Parameter Priority](priority.md)

## Local Task Refers to Global Parameter

The premise of local tasks referring global parameters is that you have already defined [Global Parameter](global.md). The usage is similar to the usage in [local parameters](local.md), but the value of the parameter needs to be configured as the key of the global parameter.

![parameter-call-global-in-local](/img/global_parameter.png)

As the figure above shows, `${biz_date}` and `${curdate}` are examples of local parameters that refer to global parameters. Observe the last line of the above figure, `local_param_bizdate` uses `${global_bizdate}` to refer to the global parameter. In the shell script, you can use `${local_param_bizdate}` to refer to the value of the global variable `global_bizdate`, or set the value of `local_param_bizdate` directly through JDBC. Similarly, `local_param` refers to the global parameters defined in the previous section through `${local_param}`. `biz_date`, `biz_curdate`, `system.datetime` are all user-defined parameters, which are assigned value via `${global parameters}`.

## Pass Parameter From Upstream Task to Downstream

DolphinScheduler allows parameter transfer between tasks. Currently, transfer direction only supports one-way transfer from upstream to downstream. The task types that support this feature are：

* [Shell](../task/shell.md)
* [SQL](../task/sql.md)
* [Procedure](../task/stored-procedure.md)

When defining an upstream node, if there is a need to transmit the result of that node to a dependency related downstream node. You need to set an `OUT` direction parameter to [Custom Parameters] of the [Current Node Settings]. At present, we mainly focus on the SQL and shell nodes to pass parameters downstream.

### SQL

`prop` is user-specified; the direction selects `OUT`, and will define as an export parameter only when the direction is `OUT`. Choose data structures for data type according to the scenario, and the leave the value part blank.

If the result of the SQL node has only one row, one or multiple fields, the name of the `prop` needs to be the same as the field name. The data type can choose structure except `LIST`. The parameter assigns the value according to the same column name in the SQL query result.

If the result of the SQL node has multiple rows, one or more fields, the name of the `prop` needs to be the same as the field name. Choose the data type structure as `LIST`, and the SQL query result will be converted to `LIST<VARCHAR>`, and forward to convert to JSON as the parameter value.

Let's make an example of the SQL node process in the above picture:

The following defines the [createParam1] node in the above figure:

![png05](/img/globalParam/image-20210723104957031.png)

The following defines the [createParam2] node:

![png06](/img/globalParam/image-20210723105026924.png)

Find the value of the variable in the [Workflow Instance] page corresponding to the node instance.

The following shows the Node instance [createparam1]:

![png07](/img/globalParam/image-20210723105131381.png)

Here, the value of "id" is 12.

Let's see the case of the node instance [createparam2].

![png08](/img/globalParam/image-20210723105255850.png)

There is only the "id" value. Although the user-defined SQL query both "id" and "database_name" field, only set the `OUT` parameter `id` due to only one parameter "id" is defined for output. The length of the result list is 10 due to display reasons.

### SHELL

`prop` is user-specified and the direction is `OUT`. The output is defined as an export parameter only when the direction is `OUT`. Choose data structures for data type according to the scenario, and leave the value part blank.
The user needs to pass the parameter when creating the shell script, the output statement format is `${setValue(key=value)}`, the key is the `prop` of the corresponding parameter, and value is the value of the parameter.

For example, in the figure below:

![png09](/img/globalParam/image-20210723101242216.png)

When the log detects the `${setValue(key=value1)}` format in the shell node definition, it will assign value1 to the key, and downstream nodes can use the variable key directly. Similarly, you can find the corresponding node instance on the [Workflow Instance] page to see the value of the variable.

![png10](/img/globalParam/image-20210723102522383.png)
