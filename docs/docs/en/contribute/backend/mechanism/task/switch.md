# SWITCH Task development

Switch task workflow step as follows

* User-defined expressions and branch information are stored in `taskParams` in `taskdefinition`. When the switch is executed, it will be formatted as `SwitchParameters`
* `SwitchTaskExecThread` processes the expressions defined in `switch` from top to bottom, obtains the value of the variable from `varPool`, and parses the expression through `javascript`. If the expression returns true, stop checking and record The order of the expression, here we record as resultConditionLocation. The task of SwitchTaskExecThread is over
* After the `switch` task runs, if there is no error (more commonly, the user-defined expression is out of specification or there is a problem with the parameter name), then `MasterExecThread.submitPostNode` will obtain the downstream node of the `DAG` to continue execution.
* If it is found in `DagHelper.parsePostNodes` that the current node (the node that has just completed the work) is a `switch` node, the `resultConditionLocation` will be obtained, and all branches except `resultConditionLocation` in the SwitchParameters will be skipped. In this way, only the branches that need to be executed are left

