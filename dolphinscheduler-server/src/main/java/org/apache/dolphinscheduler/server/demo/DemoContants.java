package org.apache.dolphinscheduler.server.demo;

public class DemoContants {
    public static final String []taskDefinitionJson = {"[{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"001\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"start\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"002\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo ${dt}\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"},{\"code\":",
        ",\"delayTime\":\"0\",\"description\":\"\",\"environmentCode\":-1,\"failRetryInterval\":\"1\",\"failRetryTimes\":\"0\",\"flag\":\"YES\",\"name\":\"003\",\"taskParams\":{\"localParams\":[],\"rawScript\":\"echo \\\"end\\\"\",\"resourceList\":[]},\"taskPriority\":\"MEDIUM\",\"taskType\":\"SHELL\",\"timeout\":0,\"timeoutFlag\":\"CLOSE\",\"timeoutNotifyStrategy\":\"\",\"workerGroup\":\"default\"}]"};

    public static final String []taskRelationJson = {"[{\"name\":\"\",\"preTaskCode\":0,\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}},{\"name\":\"\",\"preTaskCode\":",
        ",\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}},{\"name\":\"\",\"preTaskCode\":",
        ",\"preTaskVersion\":0,\"postTaskCode\":",
        ",\"postTaskVersion\":0,\"conditionType\":\"NONE\",\"conditionParams\":{}}]"};

    public static final String []locations = {"[{\"taskCode\":",
        ",\"x\":-54.79998779296875,\"y\":180.39999389648438},{\"taskCode\":",
        ",\"x\":240.4000244140625,\"y\":180.39999389648438},{\"taskCode\":",
        ",\"x\":529.2000122070312,\"y\":180.39999389648438}]"};

    public static final String name = "demo_shell";

    public static final String description = "";
    public static final String globalParams = "[{\"prop\":\"dt\",\"value\":\"hello world\",\"direct\":\"IN\",\"type\":\"VARCHAR\"}]";

    public static final int timeout = 0;



}
