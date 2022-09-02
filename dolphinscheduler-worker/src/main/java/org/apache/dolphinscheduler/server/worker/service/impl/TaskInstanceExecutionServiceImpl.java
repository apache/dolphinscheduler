package org.apache.dolphinscheduler.server.worker.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.remote.dto.WorkerTaskInstanceWaitingDto;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.server.worker.service.TaskInstanceExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskInstanceExecutionServiceImpl implements TaskInstanceExecutionService {

    @Autowired
    private WorkerManagerThread workerManagerThread;

    @Override
    public List<TaskExecutionContext> listingExecutionContext() {
        return new ArrayList<>(TaskExecutionContextCacheManager.getAllTaskRequestList());
    }

    @Override
    public List<WorkerTaskInstanceWaitingDto> listingWaitingTask() {
        long currentTimeMillis = System.currentTimeMillis();
        return workerManagerThread.getWaitingTask().stream()
                .map(workerTaskExecuteRunnable -> {
                    TaskExecutionContext taskExecutionContext = workerTaskExecuteRunnable.getTaskExecutionContext();
                    return WorkerTaskInstanceWaitingDto.builder()
                            .taskInstanceId(taskExecutionContext.getTaskInstanceId())
                            .taskInstanceName(taskExecutionContext.getTaskName())
                            .submitTime(taskExecutionContext.getFirstSubmitTime())
                            .WaitingTime(currentTimeMillis - taskExecutionContext.getFirstSubmitTime().getTime())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
