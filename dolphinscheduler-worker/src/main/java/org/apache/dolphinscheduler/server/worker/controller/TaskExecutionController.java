package org.apache.dolphinscheduler.server.worker.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.remote.dto.WorkerTaskInstanceWaitingDto;
import org.apache.dolphinscheduler.server.worker.service.TaskInstanceExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/task")
public class TaskExecutionController {

    @Autowired
    private TaskInstanceExecutionService taskInstanceExecutionService;

    @GetMapping("/listingExecutionContext")
    public List<TaskExecutionContext> listingExecutionContext() {
        return taskInstanceExecutionService.listingExecutionContext();
    }

    @GetMapping("/listingWaitingTask")
    public List<WorkerTaskInstanceWaitingDto> listingWaitingTask() {
        return taskInstanceExecutionService.listingWaitingTask();
    }
}
