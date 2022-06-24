# Script

If you need to use `Shell script` for alerting, create an alert instance in the alert instance management and select the `Script` plugin. The following shows the `Script` configuration example:

![dingtalk-plugin](/img/alert/script-plugin.png)

Parameter Configuration
-----------------------

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">User Params</td><td class="confluenceTd">User defined parameters will pass to the script.</td></tr><tr><td class="confluenceTd">Script Path</td><td class="confluenceTd">The file location path in the server.</td></tr><tr><td class="confluenceTd">Type</td><td class="confluenceTd">Support<span>&nbsp;</span><code>Shell</code><span>&nbsp;</span>script.</td></tr></tbody></table>

> **Note:** Please consider the script file access privileges with the executing tenant.