package org.apache.dolphinscheduler.server.worker.service;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.remote.dto.WorkerTaskInstanceWaitingDto;

import java.util.List;

public interface TaskInstanceExecutionService {
    List<TaskExecutionContext> listingExecutionContext();

    List<WorkerTaskInstanceWaitingDto> listingWaitingTask();
}
