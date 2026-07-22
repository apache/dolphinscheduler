# Parameter Priority

DolphinScheduler has six parameter types:

* [Built-in Parameter](built-in.md): parameters built into the system
* [Project-level Parameter](project-parameter.md): parameters defined at the project management page.
* [Global Parameter](global.md): parameters defined at the workflow define page.
* [Startup Parameter](startup-parameter.md): parameters defined at the workflow launch page.
* [Parameter Context](context.md): parameters passed by upstream task nodes.
* [Local Parameter](local.md): parameters belong to its node, which is the parameters defined by the user in [Custom Parameters].

The user can define part of the parameters when creating workflow definitions.

As there are multiple sources of the parameter value, it will raise parameter priority issues when the parameter name is the same. The priority of DolphinScheduler parameters from high to low is: `Parameter Context > Startup Parameter > Local Parameter >  Global Parameter > Project-level Parameter > Built-in Parameter`.

In the case of upstream tasks can pass parameters to the downstream, there may be multiple tasks upstream that pass the same parameter name:

* Downstream nodes prefer to use parameters with non-empty values
* If there are multiple parameters with non-empty values, select the value from the upstream task with the latest completion time

## Example

The following are examples showing task parameters priority problems:

1: Use Shell nodes to explain the first case.

![priority-parameter01](../../../../img/new_ui/dev/parameter/priority_parameter01.png)

The [useParam] node can use the parameters which are set in the [createParam] node. The [useParam] node cannot obtain the parameters from the [noCreateParam] node due to there is no dependency between them. Other task node types have the same usage rules with the Shell example here.

![priority-parameter02](../../../../img/new_ui/dev/parameter/priority_parameter02.png)

The [createParam] node creates an OUT parameter 'key1' with a value of '1'.

![priority-parameter03](../../../../img/new_ui/dev/parameter/priority_parameter03.png)

The [useParam] node creates two parameters named 'key1' and 'key2'. The parameter 'key1' shares the same name as the one passed by the upstream node and is assigned a value of '11'. However, due to priority rules, this local value ('11') is discarded, and the final assigned value becomes '1', which is passed by the upstream node.

2: Use Shell and SQL nodes to explain complex combined cases.

![priority-parameter04](../../../../img/new_ui/dev/parameter/priority_parameter04.png)

The following shows the definition of the [createParam1] node:

![priority-parameter05](../../../../img/new_ui/dev/parameter/priority_parameter05.png)

The [createParam1] node creates an OUT parameter named 'id' and assigns it the value '11'.

The following shows the definition of the [createParam2] node:

![priority-parameter06](../../../../img/new_ui/dev/parameter/priority_parameter06.png)

The [createParam2] node creates an OUT parameter named 'id' and assigns it the value '22'.  A "sleep 20" logic has been added to this node to ensure it completes after the [createParam1] node.

![priority-parameter07](../../../../img/new_ui/dev/parameter/priority_parameter07.png)

'id' is a project-level parameter with an assigned value of '1'.

The following shows the definition of the [useParam] node:

![priority-parameter08](../../../../img/new_ui/dev/parameter/priority_parameter08.png)

'id' is a local parameter of this node, assigned a value of '3' by the current node.

![priority-parameter09](../../../../img/new_ui/dev/parameter/priority_parameter09.png)

However, the user also sets the 'id' parameter (global parameter) when saving the process definition, assigning it a value of 2.

![priority-parameter10](../../../../img/new_ui/dev/parameter/priority_parameter10.png)

"id" can be configured on the task launch page (startup parameter) and assign its value to 4.

![priority-parameter11](../../../../img/new_ui/dev/parameter/priority_parameter11.png)

The execution results meet expectations, with the Parameter Context taking the highest priority. The user configured a parameter named 'id' for both the [createParam1] and [createParam2] nodes. Consequently, the [useParam] node consumes the value from [createParam2], which completed later.
