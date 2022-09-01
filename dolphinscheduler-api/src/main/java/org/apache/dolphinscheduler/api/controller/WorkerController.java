package org.apache.dolphinscheduler.api.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.WorkerRestfulService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.remote.dto.WorkerTaskInstanceWaitingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.apache.dolphinscheduler.api.enums.Status.LISTING_EXECUTING_TASK_EXECUTION_CONTEXT_BY_WORKER_ERROR;

@Api(tags = "WORKER_TAG")
@RestController
@RequestMapping("/monitor/worker")
public class WorkerController {

    @Autowired
    private WorkerRestfulService workerRestfulService;


    @ApiOperation(value = "queryExecutingTaskExecutionContext", notes = "QUERY_EXECUTING_TASK_EXECUTION_CONTEXT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "workerAddress", value = "WORKER_ADDRESS", required = true, dataTypeClass = String.class, example = "127.0.0.1:5679")
    })
    @GetMapping(value = "/{workerAddress}/queryExecutingTaskExecutionContext")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LISTING_EXECUTING_TASK_EXECUTION_CONTEXT_BY_WORKER_ERROR)
    public Result<List<TaskExecutionContext>> listingExecutingTaskExecutionContext(@PathVariable("workerAddress") String workerAddress) {
        List<TaskExecutionContext> taskExecutionContexts = workerRestfulService.listingExecutingTaskExecutionContext(workerAddress);
        return Result.success(taskExecutionContexts);
    }

    @ApiOperation(value = "queryWaitingTask", notes = "QUERY_WAITING_TASK")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "workerAddress", value = "WORKER_ADDRESS", required = true, dataTypeClass = String.class, example = "127.0.0.1:5679")
    })
    @GetMapping(value = "/{workerAddress}/queryWaitingTask")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LISTING_EXECUTING_TASK_EXECUTION_CONTEXT_BY_WORKER_ERROR)
    public Result<List<WorkerTaskInstanceWaitingDto>> listingWaitingTask(@PathVariable("workerAddress") String workerAddress) {
        List<WorkerTaskInstanceWaitingDto> taskExecutionContexts = workerRestfulService.listingWaitingTask(workerAddress);
        return Result.success(taskExecutionContexts);
    }

}
