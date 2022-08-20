package org.apache.dolphinscheduler.server.demo;

public class DemoContants {
    public static final String [] SHELL_taskDefinitionJson = {"[{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"001\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"start\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"002\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo ${dt}\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"003\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"end\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"}]"};
    public static final String [] SUB_PROCESS_taskDefinitionJson = {"[{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"sub\",\"taskParams\":{\"localParams\":[],\"resourceList\":[],\"processDefinitionCode\":",
        "},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SUB_PROCESS\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"}]"};
    public static final String [] SWITCH_taskDefinitionJson = {"[{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"switch\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"\",\"resourceList\":[],\"switchResult\":{\"dependTaskList\":[{\"condition\":\"${switchValue} == \\\"A\\\"\",\"nextNode\":",
        "},{\"condition\":\"${switchValue} == \\\"B\\\"\",\"nextNode\":",
        "}],\"nextNode\":",
        "}},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SWITCH\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"default\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"default\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"TaskA\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"TaskA\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"TaskB\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"TaskA\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"}]"};
    public static final String [] CONDITION_taskDefinitionJson = {"[{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"Condition\",\"taskParams\":{\"localParams\":[],\"resourceList\":[],\"dependence\":{\"relation\":\"AND\",\"dependTaskList\":[]},\"conditionResult\":{\"successNode\":[",
        "],\"failedNode\":[",
        "]}},\"taskPriority\":\"MEDIUM\",\"taskType\":\"CONDITIONS\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"Node\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"Hello World\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"Success\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"success\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"False\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"false\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"}]"};
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
    public static final String [] SHELL_locations = {"[{\"taskCode\":",
        ",\"x\":-54.79998779296875,\"y\":180.39999389648438},{\"taskCode\":",
        ",\"x\":240.4000244140625,\"y\":180.39999389648438},{\"taskCode\":",
        ",\"x\":529.2000122070312,\"y\":180.39999389648438}]"};
    public static final String [] SWITCH_locations = {"[{\"taskCode\":",
        ",\"x\":100,\"y\":343},{\"taskCode\":",
        ",\"x\":429,\"y\":217},{\"taskCode\":",
        ",\"x\":431,\"y\":343},{\"taskCode\":",
        ",\"x\":429,\"y\":473}]"};
    public static final String [] SUB_PROCESS_locations = {"[{\"taskCode\":",
        ",\"x\":203,\"y\":182}]"};
    public static final String [] CONDITION_locations = {"[{\"taskCode\":",
        ",\"x\":240,\"y\":382},{\"taskCode\":",
        ",\"x\":-20,\"y\":382},{\"taskCode\":",
        ",\"x\":508,\"y\":260},{\"taskCode\":",
        ",\"x\":508,\"y\":510}]"};
    public static final String SHELL_NAME = "demo_shell";
    public static final String SUB_PROCESS_NAME = "demo_sub_process";
    public static final String SWITCH_NAME = "demo_switch";
    public static final String CONDITION_NAME = "demo_condition";
    public static final String DESCRIPTION = "";
    public static final String GLOBAL_PARAMS = "[]";
    public static final String SHELL_GLOBAL_PARAMS = "[{\"prop\":\"dt\",\"value\":\"hello world\",\"direct\":\"IN\",\"type\":\"VARCHAR\"}]";
    public static final String SWITCH_GLOBAL_PARAMS = "[{\"prop\":\"switchValue\",\"value\":\"A\",\"direct\":\"IN\",\"type\":\"VARCHAR\"}]";
    public static final int TIMEOUT = 0;



}
