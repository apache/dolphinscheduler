package org.apache.dolphinscheduler.api.utils.exportprocess;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * task node param factory
 */
public class TaskNodeParamFactory {

    private static Map<String, exportProcessAddTaskParam> taskServices = new ConcurrentHashMap<>();

    public static exportProcessAddTaskParam getByTaskType(String taskType){
        return taskServices.get(taskType);
    }

    static void register(String taskType, exportProcessAddTaskParam addSpecialTaskParam){
        if (null != taskType) {
            taskServices.put(taskType, addSpecialTaskParam);
        }
    }
}
