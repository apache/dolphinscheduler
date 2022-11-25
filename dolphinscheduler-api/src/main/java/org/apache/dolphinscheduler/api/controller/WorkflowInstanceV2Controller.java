package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.api.enums.Status.*;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowInstanceListPagingResponse;
import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowInstanceQueryRequest;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.ProcessInstanceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * workflow instance controller
 */
@Tag(name = "WORKFLOW_INSTANCE_TAG_V2")
@RestController
@RequestMapping("/v2/workflow-instances")
public class WorkflowInstanceV2Controller extends BaseController {

    @Autowired
    private ProcessInstanceService processInstanceService;

    @Autowired
    private ExecutorService execService;

    /**
     * query workflow instance list paging
     * @param loginUser login user
     * @param workflowInstanceQueryRequest workflowInstanceQueryRequest
     * @return workflow instance list
     */
    @Operation(summary = "queryWorkflowInstanceListPaging", description = "QUERY_PROCESS_INSTANCE_LIST_NOTES")
    @GetMapping(consumes = {"application/json"})
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_INSTANCE_LIST_PAGING_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public WorkflowInstanceListPagingResponse queryWorkflowInstanceListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                              @RequestBody WorkflowInstanceQueryRequest workflowInstanceQueryRequest) {
        Result result =
                checkPageParams(workflowInstanceQueryRequest.getPageNo(), workflowInstanceQueryRequest.getPageSize());
        if (!result.checkResult()) {
            return new WorkflowInstanceListPagingResponse(result);
        }

        String searchVal = ParameterUtils.handleEscapes(workflowInstanceQueryRequest.getSearchVal());
        result = processInstanceService.queryProcessInstanceList(loginUser,
                workflowInstanceQueryRequest.getProjectName(),
                workflowInstanceQueryRequest.getWorkflowName(), workflowInstanceQueryRequest.getStartTime(),
                workflowInstanceQueryRequest.getEndTime(), searchVal,
                workflowInstanceQueryRequest.getExecutorName(), workflowInstanceQueryRequest.getStateType(),
                workflowInstanceQueryRequest.getHost(), workflowInstanceQueryRequest.getPageNo(),
                workflowInstanceQueryRequest.getPageSize());
        return new WorkflowInstanceListPagingResponse(result);
    }

    /**
     * Query workflowInstance by id
     *
     * @param loginUser login user
     * @param workflowInstanceId      workflow instance id
     * @return Result result object query
     */
    @Operation(summary = "queryWorkflowInstanceById", description = "QUERY_WORKFLOW_INSTANCE_BY_ID")
    @Parameters({
            @Parameter(name = "workflowInstanceId", description = "WORKFLOW_INSTANCE_ID", schema = @Schema(implementation = Integer.class, example = "123456", required = true))
    })
    @GetMapping(value = "/{workflowInstanceId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_INSTANCE_BY_ID_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public WorkflowInstanceListPagingResponse queryWorkflowInstanceById(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                        @PathVariable("workflowInstanceId") Integer workflowInstanceId) {
        Map<String, Object> result = processInstanceService.queryProcessInstanceById(loginUser, workflowInstanceId);
        return new WorkflowInstanceListPagingResponse(returnDataList(result));
    }

    /**
     * Delete workflowInstance by id
     *
     * @param loginUser login user
     * @param workflowInstanceId      workflow instance code
     * @return Result result object delete
     */
    @Operation(summary = "delete", description = "DELETE_WORKFLOWS_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "workflowInstanceId", description = "WORKFLOW_INSTANCE_ID", schema = @Schema(implementation = Integer.class, example = "123456", required = true))
    })
    @DeleteMapping(value = "/{workflowInstanceId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_DEFINE_BY_CODE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public WorkflowInstanceListPagingResponse deleteWorkflowInstance(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                     @PathVariable("workflowInstanceId") Integer workflowInstanceId) {
        processInstanceService.deleteProcessInstanceById(loginUser, workflowInstanceId);
        return new WorkflowInstanceListPagingResponse(Result.success());
    }

    /**
     * do action to workflow instance: pause, stop, repeat, recover from pause, recover from stop
     *
     * @param loginUser login user
     * @param workflowInstanceId workflow instance id
     * @param executeType execute type
     * @return execute result code
     */
    @Operation(summary = "execute", description = "EXECUTE_ACTION_TO_WORKFLOW_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "workflowInstanceId", description = "WORKFLOW_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "executeType", description = "EXECUTE_TYPE", required = true, schema = @Schema(implementation = ExecuteType.class))
    })
    @PostMapping(value = "/{workflowInstanceId}/execute/{executeType}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(EXECUTE_PROCESS_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public WorkflowInstanceListPagingResponse execute(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                      @PathVariable("workflowInstanceId") Integer workflowInstanceId,
                                                      @PathVariable("executeType") ExecuteType executeType) {
        Map<String, Object> result = execService.execute(loginUser, workflowInstanceId, executeType);
        return new WorkflowInstanceListPagingResponse(returnDataList(result));
    }
}
