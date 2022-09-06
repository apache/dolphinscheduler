package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.remote.dto.WorkerTaskInstanceWaitingDto;

import java.util.List;

public interface WorkerRestfulService {

    List<TaskExecutionContext> listingExecutingTaskExecutionContext(String workerAddress);

    List<WorkerTaskInstanceWaitingDto> listingWaitingTask(String workerAddress);
}
