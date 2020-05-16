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

    public static TaskNode buildDependTaskNode(int dependProcessId, String dependProcessName, String dependNodeId, String dependNodeName) {
        TaskNode node = new TaskNode();
        node.setId(String.format("%d:%s", dependProcessId, dependNodeId));
        node.setName(String.format("%s:%s", dependProcessName, dependNodeName));
        node.setType(TaskType.DEPENDENT.toString());
        node.setRunFlag(Constants.FLOWNODE_RUN_FLAG_NORMAL);
        node.setMaxRetryTimes(Constants.DEPEND_CHECK_MAX_RETRY_TIMES);
        node.setRetryInterval(Constants.DEPEND_CHECK_RETRY_INTERVAL_MINUTE);
        node.setTimeout("{\"strategy\": \"\", \"interval\": null, \"enable\": false}");
        node.setTaskInstancePriority(Priority.MEDIUM);
        return node;
    }

}
