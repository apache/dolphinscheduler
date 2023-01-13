package org.apache.dolphinscheduler.plugin.task.datafactory;

import java.util.HashMap;
import java.util.Map;

public enum DatafactoryStatus {
    Queued,
    InProgress,
    Succeeded,
    Failed,
    Canceling,
    Cancelled,
    ;

    /**
     * Gets the status property: The status of a pipeline run. Possible values: Queued, InProgress, Succeeded, Failed,
     * Canceling, Cancelled.
     *
     * @return the status value.
     */
    DatafactoryStatus() {
    }


    private static final Map<String, DatafactoryStatus> CODE_MAP = new HashMap<>();

    static {
        for (DatafactoryStatus executionStatus : DatafactoryStatus.values()) {
            CODE_MAP.put(executionStatus.name(), executionStatus);
        }
    }

    public static DatafactoryStatus of(String status) {
        DatafactoryStatus taskExecutionStatus = CODE_MAP.get(status);
        if (taskExecutionStatus == null) {
            throw new IllegalArgumentException(String.format("The data factory task execution status code: %s is invalidated",
                    status));
        }
        return taskExecutionStatus;
    }
}
