package org.apache.dolphinscheduler.server.master.runner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskExecutionRunnableRepository {

    private final Map<Integer, ITaskExecutionRunnable> taskExecutionRunnableMap = new HashMap<>();

    public void storeTaskExecutionRunnable(ITaskExecutionRunnable taskExecutionRunnable) {
        taskExecutionRunnableMap.put(taskExecutionRunnable.getTaskInstanceId(), taskExecutionRunnable);
    }

    public void removeTaskExecutionRunnable(Integer taskInstanceId) {
        taskExecutionRunnableMap.remove(taskInstanceId);
    }

    public boolean containsTaskExecutionRunnable(Integer taskInstanceId) {
        if (taskInstanceId == null) {
            return false;
        }
        return taskExecutionRunnableMap.containsKey(taskInstanceId);
    }

    public ITaskExecutionRunnable getTaskExecutionRunnable(Integer taskInstanceId) {
        return taskExecutionRunnableMap.get(taskInstanceId);
    }

    public ITaskExecutionRunnable getTaskExecutionRunnable(String taskName) {
        for (ITaskExecutionRunnable taskExecutionRunnable : taskExecutionRunnableMap.values()) {
            if (taskExecutionRunnable.getTaskExecutionRunnableContext().getTaskInstance().getName().equals(taskName)) {
                return taskExecutionRunnable;
            }
        }
        return null;
    }

    public boolean containsTaskExecutionRunnable(String taskName) {
        if (taskName == null) {
            return false;
        }
        return taskExecutionRunnableMap.values().stream().anyMatch(taskExecutionRunnable -> taskName
                .equals(taskExecutionRunnable.getTaskExecutionRunnableContext().getTaskInstance().getName()));
    }

    public Collection<ITaskExecutionRunnable> getActiveTaskExecutionRunnable() {
        return taskExecutionRunnableMap.values();
    }

}
