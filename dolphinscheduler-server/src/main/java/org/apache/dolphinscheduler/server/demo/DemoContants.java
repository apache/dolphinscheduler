package org.apache.dolphinscheduler.server.demo;

public class DemoContants {
    public static final String [] SHELL_taskDefinitionJson = {"[{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"Make production order\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"001\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"start\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"Get Information Processing\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"002\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo ${resources}\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"Sell after completion\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"003\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"end\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"}]"};
    public static final String [] SUB_PROCESS_taskDefinitionJson = {"[{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"Enter the demo_shell subnode\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"subprocess node\",\"taskParams\":{\"localParams\":[],\"resourceList\":[],\"processDefinitionCode\":",
        "},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SUB_PROCESS\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"}]"};
    public static final String [] SWITCH_taskDefinitionJson = {"[{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"The global parameter is to execute TaskA for A, and for B to execute TaskB, otherwise the default task is executed\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"switch node\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"\",\"resourceList\":[],\"switchResult\":{\"dependTaskList\":[{\"condition\":\"${switchValue} == \\\"A\\\"\",\"nextNode\":",
        "},{\"condition\":\"${switchValue} == \\\"B\\\"\",\"nextNode\":",
        "}],\"nextNode\":",
        "}},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SWITCH\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"executed default task\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"default\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"default\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"execute TaskA\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"TaskA\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"TaskA\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"execute TaskB\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"TaskB\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"TaskA\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"}]"};
    public static final String [] CONDITION_taskDefinitionJson = {"[{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"head is the status of success, tail is the status of failure\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"Condition\",\"taskParams\":{\"localParams\":[],\"resourceList\":[],\"dependence\":{\"relation\":\"AND\",\"dependTaskList\":[]},\"conditionResult\":{\"successNode\":[",
        "],\"failedNode\":[",
        "]}},\"taskPriority\":\"MEDIUM\",\"taskType\":\"CONDITIONS\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"Toss a coin\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"coin\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"Start\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"Choose to learn if the result is head\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"head\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"Start learning\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"Choose to play if the result is tail\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"tail\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"Start playing\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"}]"};
    public static final String [] DEPENDENT_taskDefinitionJson = {"[{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"The weekly report task requires the demo_shell and demo_switch tasks to be successfully executed every day of the last week\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"weekly report task\",\"taskParams\":{\"localParams\":[],\"resourceList\":[],\"dependence\":{\"relation\":\"AND\",\"dependTaskList\":[{\"relation\":\"AND\",\"dependItemList\":[{\"projectCode\":",
        ",\"definitionCode\":",
        ",\"depTaskCode\":0,\"cycle\":\"day\",\"dateValue\":\"last1Days\",\"state\":null},{\"projectCode\":",
        ",\"definitionCode\":",
        ",\"depTaskCode\":0,\"cycle\":\"day\",\"dateValue\":\"last1Days\",\"state\":null}]}]}},\"taskPriority\":\"MEDIUM\",\"taskType\":\"DEPENDENT\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"Result report after the completion of the weekly report task\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"Weekly Report Task Result\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"end of report\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"}]"};
    public static final String [] PARAMETER_CONTEXT_taskDefinitionJson = {"[{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"Create a local parameter and pass the assignment to the downstream\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"upstream task node\",\"taskParams\":{\"localParams\":[{\"prop\":\"value\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"0\"},{\"prop\":\"output\",\"direct\":\"OUT\",\"type\":\"VARCHAR\",\"value\":\"\"}],\"rawScript\":\"echo \\\"====Node start====\\\"\\r\\necho \'${setValue(output=1)}\'\\r\\n\\r\\necho ${output}\\r\\necho ${value}\\r\\n\\r\\necho \\\"====Node end====\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"Test outputs the parameters passed by the upstream task\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"downstream task node\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"====node start====\\\"\\r\\n\\r\\necho ${output}\\r\\n\\r\\necho ${value}\\r\\n\\r\\necho \\\"====node end====\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"}]"};
    public static final String [] CLEAR_LOG_taskDefinitionJson = {"[{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"clear log node\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"cd ",
        "\\r\\nfind ./logs/ -mtime +30 -name \\\"*.log\\\" -exec rm -rf {} \\\\;\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"}]"};
    public static final String [] SHELL_taskRelationJson = {"[{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}},{\"name\":\"\",\"preTaskCode\":",
        ",\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}},{\"name\":\"\",\"preTaskCode\":",
        ",\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}}]"};
    public static final String [] SUB_PROCESS_taskRelationJson = {"[{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}}]"};
    public static final String [] SWITCH_taskRelationJson = {"[{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}},{\"name\":\"\",\"preTaskCode\":",
        ",\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}},{\"name\":\"\",\"preTaskCode\":",
        ",\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}},{\"name\":\"\",\"preTaskCode\":",
        ",\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}}]"};
    public static final String [] CONDITION_taskRelationJson = {"[{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}},{\"name\":\"\",\"preTaskCode\":",
        ",\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}},{\"name\":\"\",\"preTaskCode\":",
        ",\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}},{\"name\":\"\",\"preTaskCode\":",
        ",\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}}]"};
    public static final String [] DEPENDENT_taskRelationJson = {"[{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}},{\"name\":\"\",\"preTaskCode\":",
        ",\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}}]"};
    public static final String [] PARAMETER_CONTEXT_taskRelationJson = {"[{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}},{\"name\":\"\",\"preTaskCode\":",
        ",\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}}]"};
    public static final String [] CLEAR_LOG_taskRelationJson = {"[{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}}]"};
    public static final String [] SHELL_locations = {"[{\"taskCode\":",
        ",\"x\":-54.79998779296875,\"y\":180.39999389648438},{\"taskCode\":",
        ",\"x\":240.4000244140625,\"y\":180.39999389648438},{\"taskCode\":",
        ",\"x\":529.2000122070312,\"y\":180.39999389648438}]"};
    public static final String [] SWITCH_locations = {"[{\"taskCode\":",
        ",\"x\":100,\"y\":343},{\"taskCode\":",
        ",\"x\":429,\"y\":217},{\"taskCode\":",
        ",\"x\":431,\"y\":343},{\"taskCode\":",
        ",\"x\":429,\"y\":473}]"};
    public static final String [] PARAMETER_CONTEXT_locations = {"[{\"taskCode\":",
        ",\"x\":56,\"y\":465},{\"taskCode\":",
        ",\"x\":406,\"y\":465}]"};
    public static final String [] SUB_PROCESS_locations = {"[{\"taskCode\":",
        ",\"x\":203,\"y\":182}]"};
    public static final String [] CONDITION_locations = {"[{\"taskCode\":",
        ",\"x\":240,\"y\":382},{\"taskCode\":",
        ",\"x\":-20,\"y\":382},{\"taskCode\":",
        ",\"x\":508,\"y\":260},{\"taskCode\":",
        ",\"x\":508,\"y\":510}]"};
    public static final String [] CLEAR_LOG_locations = {"[{\"taskCode\":",
        ",\"x\":270,\"y\":345}]"};
    public static final String [] DEPENDENT_locations = {"[{\"taskCode\":",
        ",\"x\":100,\"y\":355},{\"taskCode\":",
        ",\"x\":350,\"y\":355}]"};
    public static final String SHELL_NAME = "demo_shell";
    public static final String CLEAR_LOG_NAME = "demo_clear_log";
    public static final String DEPENDENT_NAME = "demo_dependent";
    public static final String SUB_PROCESS_NAME = "demo_sub_process";
    public static final String SWITCH_NAME = "demo_switch";
    public static final String CONDITION_NAME = "demo_condition";
    public static final String PARAMETER_CONTEXT_NAME = "demo_parameter_context";

    public static final String CLEAR_LOG_DESCRIPTION = "Clear the DS log files from 30 days ago";
    public static final String PARAMETER_CONTEXT_DESCRIPTION = "Upstream and downstream task node parameter transfer";
    public static final String DEPENDENT_DESCRIPTION = "Check the completion of daily tasks";
    public static final String SWITCH_DESCRIPTION = "Determine which task to perform based on conditions";
    public static final String SUB_PROCESS_DESCRIPTION = "Start the production line";
    public static final String CONDITION_DESCRIPTION = "Coin Toss";
    public static final String SHELL_DESCRIPTION = "Production, processing and sales of a series of processes";
    public static final String GLOBAL_PARAMS = "[]";
    public static final String PARAMETER_CONTEXT_PARAMS = "[{\"prop\":\"output\",\"value\":\"100\",\"direct\":\"IN\",\"type\":\"VARCHAR\"},{\"prop\":\"value\",\"value\":\"99\",\"direct\":\"IN\",\"type\":\"VARCHAR\"}]";
    public static final String SHELL_GLOBAL_PARAMS = "[{\"prop\":\"resources\",\"value\":\"Processing information\",\"direct\":\"IN\",\"type\":\"VARCHAR\"}]";
    public static final String SWITCH_GLOBAL_PARAMS = "[{\"prop\":\"switchValue\",\"value\":\"A\",\"direct\":\"IN\",\"type\":\"VARCHAR\"}]";
    public static final int TIMEOUT = 0;




}
