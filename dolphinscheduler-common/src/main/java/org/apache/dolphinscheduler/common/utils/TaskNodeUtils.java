package org.apache.dolphinscheduler.common.utils;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.model.TaskNode;

public class TaskNodeUtils {

    public static String buildTaskId() {
        return String.format("%s-%d", "tasks", 10000 + RandomUtils.nextInt(9999));
    }

    public static TaskNode buildDependTaskNode(String dependProcessName, String dependNodeName, int retryTimes, int retryInterval) {
        TaskNode node = new TaskNode();
        node.setId(buildTaskId());
        node.setName(String.format("%s | %s", dependProcessName, dependNodeName));
        node.setType(TaskType.DEPENDENT.toString());
        node.setRunFlag(Constants.FLOWNODE_RUN_FLAG_NORMAL);
        node.setMaxRetryTimes(retryTimes);
        node.setRetryInterval(retryInterval);
        node.setTimeout("{\"strategy\": \"\", \"interval\": null, \"enable\": false}");
        node.setTaskInstancePriority(Priority.HIGH);
        return node;
    }

}
