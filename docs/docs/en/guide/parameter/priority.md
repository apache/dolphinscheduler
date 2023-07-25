# Parameter Priority

DolphinScheduler has three parameter types:

* [Project-level Parameter](project-parameter.md): parameters defined at the project management page.
* [Global Parameter](global.md): parameters defined at the workflow define page.
* [Startup Parameter](startup-parameter.md): parameters defined at the workflow launch page.
* [Parameter Context](context.md): parameters passed by upstream task nodes.
* [Local Parameter](local.md): parameters belong to its node, which is the parameters defined by the user in [Custom Parameters].

The user can define part of the parameters when creating workflow definitions.

As there are multiple sources of the parameter value, it will raise parameter priority issues when the parameter name is the same. The priority of DolphinScheduler parameters from high to low is: `Startup Parameter > Local Parameter > Parameter Context > Global Parameter > Project-level Parameter`.

In the case of upstream tasks can pass parameters to the downstream, there may be multiple tasks upstream that pass the same parameter name:

* Downstream nodes prefer to use parameters with non-empty values
* If there are multiple parameters with non-empty values, select the value from the upstream task with the earliest completion time

## Example

The following are examples showing task parameters priority problems:

1: Use shell nodes to explain the first case.

![priority-parameter01](../../../../img/new_ui/dev/parameter/priority_parameter01.png)

The [useParam] node can use the parameters which are set in the [createParam] node. The [useParam] node cannot obtain the parameters from the [noUseParam] node due to there is no dependency between them. Other task node types have the same usage rules with the Shell example here.

![priority-parameter02](../../../../img/new_ui/dev/parameter/priority_parameter02.png)

The [createParam] node can use parameters directly. In addition, the node creates two parameters named "key" and "key1", and "key1" has the same name as the one passed by the upstream node and assign value "12". However, due to the priority rules, the value assignment will assign "12" and the value from the upstream node is discarded.

2: Use SQL nodes to explain another case.

![priority-parameter03](../../../../img/new_ui/dev/parameter/priority_parameter03.png)

The following shows the definition of the [use_create] node:

![priority-parameter04](../../../../img/new_ui/dev/parameter/priority_parameter04.png)

"status" is own parameter of the node set by the current node. However, the user also sets the "status" parameter (global parameter) when saving the process definition and assign its value to -1. Then the value of status will be 2, with higher priority when the SQL executes. The global parameter value is discarded.

The "ID" here is the parameter set by the upstream node. The user sets the parameters of the same parameter name "ID" for the [createparam1] node and [createparam2] node. And the [use_create] node uses the value of [createParam1] which is finished first.
