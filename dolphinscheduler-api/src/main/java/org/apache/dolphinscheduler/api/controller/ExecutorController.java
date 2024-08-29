/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.api.enums.Status.BATCH_EXECUTE_WORKFLOW_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.BATCH_START_WORKFLOW_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.EXECUTE_WORKFLOW_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.START_TASK_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.START_WORKFLOW_INSTANCE_ERROR;

import org.apache.dolphinscheduler.api.audit.OperatorLog;
import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowBackFillRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowTriggerRequest;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.api.utils.WorkflowUtils;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.ExecutionOrder;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * executor controller
 */
@Tag(name = "EXECUTOR_TAG")
@RestController
@RequestMapping("projects/{projectCode}/executors")
@Slf4j
public class ExecutorController extends BaseController {

    @Autowired
    private ExecutorService execService;

    /**
     * execute process instance
     *
     * @param loginUser                 login user
     * @param processDefinitionCode     process definition code
     * @param scheduleTime              schedule time when CommandType is COMPLEMENT_DATA  there are two ways to transfer parameters 1.date range, for example:{"complementStartDate":"2022-01-01 12:12:12","complementEndDate":"2022-01-6 12:12:12"} 2.manual input,  for example:{"complementScheduleDateList":"2022-01-01 00:00:00,2022-01-02 12:12:12,2022-01-03 12:12:12"}
     * @param failureStrategy           failure strategy
     * @param startNodeList             start nodes list
     * @param taskDependType            task depend type
     * @param execType                  execute type
     * @param warningType               warning type
     * @param warningGroupId            warning group id
     * @param runMode                   run mode
     * @param processInstancePriority   process instance priority
     * @param workerGroup               worker group
     * @param expectedParallelismNumber the expected parallelism number when execute complement in parallel mode
     * @param testFlag                  testFlag
     * @param executionOrder            complement data in some kind of order
     * @return start process result code
     */
    @Operation(summary = "startProcessInstance", description = "RUN_WORKFLOW_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "processDefinitionCode", description = "WORKFLOW_DEFINITION_CODE", required = true, schema = @Schema(implementation = Long.class), example = "100"),
            @Parameter(name = "scheduleTime", description = "SCHEDULE_TIME", required = true, schema = @Schema(implementation = String.class), example = "2022-04-06 00:00:00,2022-04-06 00:00:00"),
            @Parameter(name = "failureStrategy", description = "FAILURE_STRATEGY", required = true, schema = @Schema(implementation = FailureStrategy.class)),
            @Parameter(name = "startNodeList", description = "START_NODE_LIST", schema = @Schema(implementation = String.class)),
            @Parameter(name = "taskDependType", description = "TASK_DEPEND_TYPE", schema = @Schema(implementation = TaskDependType.class)),
            @Parameter(name = "execType", description = "COMMAND_TYPE", schema = @Schema(implementation = CommandType.class)),
            @Parameter(name = "warningType", description = "WARNING_TYPE", required = true, schema = @Schema(implementation = WarningType.class)),
            @Parameter(name = "warningGroupId", description = "WARNING_GROUP_ID", schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "runMode", description = "RUN_MODE", schema = @Schema(implementation = RunMode.class)),
            @Parameter(name = "processInstancePriority", description = "WORKFLOW_INSTANCE_PRIORITY", required = true, schema = @Schema(implementation = Priority.class)),
            @Parameter(name = "workerGroup", description = "WORKER_GROUP", schema = @Schema(implementation = String.class, example = "default")),
            @Parameter(name = "tenantCode", description = "TENANT_CODE", schema = @Schema(implementation = String.class, example = "default")),
            @Parameter(name = "environmentCode", description = "ENVIRONMENT_CODE", schema = @Schema(implementation = Long.class, example = "-1")),
            @Parameter(name = "timeout", description = "TIMEOUT", schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "expectedParallelismNumber", description = "EXPECTED_PARALLELISM_NUMBER", schema = @Schema(implementation = int.class, example = "8")),
            @Parameter(name = "dryRun", description = "DRY_RUN", schema = @Schema(implementation = int.class, example = "0")),
            @Parameter(name = "testFlag", description = "TEST_FLAG", schema = @Schema(implementation = int.class, example = "0")),
            @Parameter(name = "complementDependentMode", description = "COMPLEMENT_DEPENDENT_MODE", schema = @Schema(implementation = ComplementDependentMode.class)),
            @Parameter(name = "allLevelDependent", description = "ALL_LEVEL_DEPENDENT", schema = @Schema(implementation = boolean.class, example = "false")),
            @Parameter(name = "executionOrder", description = "EXECUTION_ORDER", schema = @Schema(implementation = ExecutionOrder.class))
    })
    @PostMapping(value = "start-process-instance")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(START_WORKFLOW_INSTANCE_ERROR)
    @OperatorLog(auditType = AuditType.WORKFLOW_START)
    public Result<List<Integer>> triggerWorkflowDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                           @RequestParam(value = "processDefinitionCode") long processDefinitionCode,
                                                           @RequestParam(value = "scheduleTime") String scheduleTime,
                                                           @RequestParam(value = "failureStrategy") FailureStrategy failureStrategy,
                                                           @RequestParam(value = "startNodeList", required = false) String startNodeList,
                                                           @RequestParam(value = "taskDependType", required = false, defaultValue = "TASK_POST") TaskDependType taskDependType,
                                                           @RequestParam(value = "execType", required = false, defaultValue = "START_PROCESS") CommandType execType,
                                                           @RequestParam(value = "warningType") WarningType warningType,
                                                           @RequestParam(value = "warningGroupId", required = false) Integer warningGroupId,
                                                           @RequestParam(value = "runMode", required = false) RunMode runMode,
                                                           @RequestParam(value = "processInstancePriority", required = false) Priority processInstancePriority,
                                                           @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
                                                           @RequestParam(value = "tenantCode", required = false, defaultValue = "default") String tenantCode,
                                                           @RequestParam(value = "environmentCode", required = false, defaultValue = "-1") Long environmentCode,
                                                           @RequestParam(value = "startParams", required = false) String startParams,
                                                           @RequestParam(value = "expectedParallelismNumber", required = false) Integer expectedParallelismNumber,
                                                           @RequestParam(value = "dryRun", defaultValue = "0", required = false) int dryRun,
                                                           @RequestParam(value = "testFlag", defaultValue = "0") int testFlag,
                                                           @RequestParam(value = "complementDependentMode", required = false) ComplementDependentMode complementDependentMode,
                                                           @RequestParam(value = "allLevelDependent", required = false, defaultValue = "false") boolean allLevelDependent,
                                                           @RequestParam(value = "executionOrder", required = false) ExecutionOrder executionOrder) {
        switch (execType) {
            case START_PROCESS:
                final WorkflowTriggerRequest workflowTriggerRequest = WorkflowTriggerRequest.builder()
                        .loginUser(loginUser)
                        .workflowDefinitionCode(processDefinitionCode)
                        .startNodes(startNodeList)
                        .failureStrategy(failureStrategy)
                        .taskDependType(taskDependType)
                        .execType(execType)
                        .warningType(warningType)
                        .warningGroupId(warningGroupId)
                        .workflowInstancePriority(processInstancePriority)
                        .workerGroup(workerGroup)
                        .tenantCode(tenantCode)
                        .environmentCode(environmentCode)
                        .startParamList(startParams)
                        .dryRun(Flag.of(dryRun))
                        .testFlag(Flag.of(testFlag))
                        .build();
                return Result
                        .success(Lists.newArrayList(execService.triggerWorkflowDefinition(workflowTriggerRequest)));
            case COMPLEMENT_DATA:
                final WorkflowBackFillRequest workflowBackFillRequest = WorkflowBackFillRequest.builder()
                        .loginUser(loginUser)
                        .workflowDefinitionCode(processDefinitionCode)
                        .startNodes(startNodeList)
                        .failureStrategy(failureStrategy)
                        .taskDependType(taskDependType)
                        .execType(execType)
                        .warningType(warningType)
                        .warningGroupId(warningGroupId)
                        .backfillRunMode(runMode)
                        .workflowInstancePriority(processInstancePriority)
                        .workerGroup(workerGroup)
                        .tenantCode(tenantCode)
                        .environmentCode(environmentCode)
                        .startParamList(startParams)
                        .dryRun(Flag.of(dryRun))
                        .testFlag(Flag.of(testFlag))
                        .backfillTime(WorkflowUtils.parseBackfillTime(scheduleTime))
                        .expectedParallelismNumber(expectedParallelismNumber)
                        .backfillDependentMode(complementDependentMode)
                        .allLevelDependent(allLevelDependent)
                        .executionOrder(executionOrder)
                        .build();
                return Result.success(execService.backfillWorkflowDefinition(workflowBackFillRequest));
            default:
                throw new ServiceException("The execType: " + execType + " is invalid");
        }
    }

    /**
     * batch execute process instance
     * If any processDefinitionCode cannot be found, the failure information is returned and the status is set to
     * failed. The successful task will run normally and will not stop
     *
     * @param loginUser                 login user
     * @param processDefinitionCodes    process definition codes
     * @param scheduleTime              schedule time
     * @param failureStrategy           failure strategy
     * @param startNodeList             start nodes list
     * @param taskDependType            task depend type
     * @param execType                  execute type
     * @param warningType               warning type
     * @param warningGroupId            warning group id
     * @param runMode                   run mode
     * @param processInstancePriority   process instance priority
     * @param workerGroup               worker group
     * @param tenantCode                tenant code
     * @param expectedParallelismNumber the expected parallelism number when execute complement in parallel mode
     * @param testFlag                  testFlag
     * @param executionOrder            complement data in some kind of order
     * @return start process result code
     */
    @Operation(summary = "batchStartProcessInstance", description = "BATCH_RUN_WORKFLOW_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "processDefinitionCodes", description = "WORKFLOW_DEFINITION_CODE_LIST", required = true, schema = @Schema(implementation = String.class, example = "1,2,3")),
            @Parameter(name = "scheduleTime", description = "SCHEDULE_TIME", required = true, schema = @Schema(implementation = String.class, example = "2022-04-06 00:00:00,2022-04-06 00:00:00")),
            @Parameter(name = "failureStrategy", description = "FAILURE_STRATEGY", required = true, schema = @Schema(implementation = FailureStrategy.class)),
            @Parameter(name = "startNodeList", description = "START_NODE_LIST", schema = @Schema(implementation = String.class)),
            @Parameter(name = "taskDependType", description = "TASK_DEPEND_TYPE", schema = @Schema(implementation = TaskDependType.class)),
            @Parameter(name = "execType", description = "COMMAND_TYPE", schema = @Schema(implementation = CommandType.class)),
            @Parameter(name = "warningType", description = "WARNING_TYPE", required = true, schema = @Schema(implementation = WarningType.class)),
            @Parameter(name = "warningGroupId", description = "WARNING_GROUP_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "runMode", description = "RUN_MODE", schema = @Schema(implementation = RunMode.class)),
            @Parameter(name = "processInstancePriority", description = "WORKFLOW_INSTANCE_PRIORITY", required = true, schema = @Schema(implementation = Priority.class)),
            @Parameter(name = "workerGroup", description = "WORKER_GROUP", schema = @Schema(implementation = String.class, example = "default")),
            @Parameter(name = "tenantCode", description = "TENANT_CODE", schema = @Schema(implementation = String.class, example = "default")),
            @Parameter(name = "environmentCode", description = "ENVIRONMENT_CODE", schema = @Schema(implementation = Long.class, example = "-1")),
            @Parameter(name = "expectedParallelismNumber", description = "EXPECTED_PARALLELISM_NUMBER", schema = @Schema(implementation = int.class, example = "8")),
            @Parameter(name = "dryRun", description = "DRY_RUN", schema = @Schema(implementation = int.class, example = "0")),
            @Parameter(name = "testFlag", description = "TEST_FLAG", schema = @Schema(implementation = int.class, example = "0")),
            @Parameter(name = "complementDependentMode", description = "COMPLEMENT_DEPENDENT_MODE", schema = @Schema(implementation = ComplementDependentMode.class)),
            @Parameter(name = "allLevelDependent", description = "ALL_LEVEL_DEPENDENT", schema = @Schema(implementation = boolean.class, example = "false")),
            @Parameter(name = "executionOrder", description = "EXECUTION_ORDER", schema = @Schema(implementation = ExecutionOrder.class))
    })
    @PostMapping(value = "batch-start-process-instance")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_START_WORKFLOW_INSTANCE_ERROR)
    @OperatorLog(auditType = AuditType.WORKFLOW_BATCH_START)
    public Result<List<Integer>> batchTriggerWorkflowDefinitions(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                 @RequestParam(value = "processDefinitionCodes") String processDefinitionCodes,
                                                                 @RequestParam(value = "scheduleTime") String scheduleTime,
                                                                 @RequestParam(value = "failureStrategy") FailureStrategy failureStrategy,
                                                                 @RequestParam(value = "startNodeList", required = false) String startNodeList,
                                                                 @RequestParam(value = "taskDependType", required = false) TaskDependType taskDependType,
                                                                 @RequestParam(value = "execType", required = false) CommandType execType,
                                                                 @RequestParam(value = "warningType") WarningType warningType,
                                                                 @RequestParam(value = "warningGroupId", required = false) Integer warningGroupId,
                                                                 @RequestParam(value = "runMode", required = false) RunMode runMode,
                                                                 @RequestParam(value = "processInstancePriority", required = false) Priority processInstancePriority,
                                                                 @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
                                                                 @RequestParam(value = "tenantCode", required = false, defaultValue = "default") String tenantCode,
                                                                 @RequestParam(value = "environmentCode", required = false, defaultValue = "-1") Long environmentCode,
                                                                 @RequestParam(value = "startParams", required = false) String startParams,
                                                                 @RequestParam(value = "expectedParallelismNumber", required = false) Integer expectedParallelismNumber,
                                                                 @RequestParam(value = "dryRun", defaultValue = "0", required = false) int dryRun,
                                                                 @RequestParam(value = "testFlag", defaultValue = "0") int testFlag,
                                                                 @RequestParam(value = "complementDependentMode", required = false) ComplementDependentMode complementDependentMode,
                                                                 @RequestParam(value = "allLevelDependent", required = false, defaultValue = "false") boolean allLevelDependent,
                                                                 @RequestParam(value = "executionOrder", required = false) ExecutionOrder executionOrder) {
        List<Long> workflowDefinitionCodes = Arrays.stream(processDefinitionCodes.split(Constants.COMMA))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        List<Integer> result = new ArrayList<>();
        for (Long workflowDefinitionCode : workflowDefinitionCodes) {
            Result<List<Integer>> workflowInstanceIds = triggerWorkflowDefinition(loginUser,
                    workflowDefinitionCode,
                    scheduleTime,
                    failureStrategy,
                    startNodeList,
                    taskDependType,
                    execType,
                    warningType,
                    warningGroupId,
                    runMode,
                    processInstancePriority,
                    workerGroup,
                    tenantCode,
                    environmentCode,
                    startParams,
                    expectedParallelismNumber,
                    dryRun,
                    testFlag,
                    complementDependentMode,
                    allLevelDependent,
                    executionOrder);
            result.addAll(workflowInstanceIds.getData());
        }
        return Result.success(result);
    }

    /**
     * do action to process instance: pause, stop, repeat, recover from pause, recover from stop
     */
    @Operation(summary = "execute", description = "EXECUTE_ACTION_TO_WORKFLOW_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "processInstanceId", description = "WORKFLOW_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "executeType", description = "EXECUTE_TYPE", required = true, schema = @Schema(implementation = ExecuteType.class))
    })
    @PostMapping(value = "/execute")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(EXECUTE_WORKFLOW_INSTANCE_ERROR)
    @OperatorLog(auditType = AuditType.WORKFLOW_EXECUTE)
    public Result<Void> controlWorkflowInstance(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                @RequestParam("processInstanceId") Integer processInstanceId,
                                                @RequestParam("executeType") ExecuteType executeType) {
        execService.controlWorkflowInstance(loginUser, processInstanceId, executeType);
        return Result.success();
    }

    /**
     * batch execute and do action to process instance
     *
     * @param loginUser          login user
     * @param processInstanceIds process instance ids, delimiter by "," if more than one id
     * @param executeType        execute type
     * @return execute result code
     */
    @Operation(summary = "batchExecute", description = "BATCH_EXECUTE_ACTION_TO_WORKFLOW_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = int.class)),
            @Parameter(name = "processInstanceIds", description = "PROCESS_INSTANCE_IDS", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "executeType", description = "EXECUTE_TYPE", required = true, schema = @Schema(implementation = ExecuteType.class))
    })
    @PostMapping(value = "/batch-execute")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_EXECUTE_WORKFLOW_INSTANCE_ERROR)
    @OperatorLog(auditType = AuditType.WORKFLOW_BATCH_RERUN)
    public Result<Void> batchControlWorkflowInstance(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @RequestParam("processInstanceIds") String processInstanceIds,
                                                     @RequestParam("executeType") ExecuteType executeType) {

        String[] processInstanceIdArray = processInstanceIds.split(Constants.COMMA);
        List<String> errorMessage = new ArrayList<>();
        for (String strProcessInstanceId : processInstanceIdArray) {
            int processInstanceId = Integer.parseInt(strProcessInstanceId);
            try {
                execService.controlWorkflowInstance(loginUser, processInstanceId, executeType);
                log.info("Success do action {} on workflowInstance: {}", executeType, processInstanceId);
            } catch (Exception e) {
                errorMessage.add("Failed do action " + executeType + " on workflowInstance: " + processInstanceId
                        + "reason: " + e.getMessage());
                log.error("Failed do action {} on workflowInstance: {}, error: {}", executeType, processInstanceId, e);
            }
        }
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(errorMessage)) {
            throw new ServiceException(String.join("\n", errorMessage));
        }
        return Result.success();
    }

    /**
     * execute task instance
     *
     * @param loginUser      login user
     * @param projectCode    project code
     * @param code           taskDefinitionCode
     * @param version        taskDefinitionVersion
     * @param warningGroupId warning group id
     * @param workerGroup    worker group
     * @return start task result code
     */
    @Operation(summary = "startTaskInstance", description = "RUN_TASK_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "version", description = "VERSION", schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "failureStrategy", description = "FAILURE_STRATEGY", required = true, schema = @Schema(implementation = FailureStrategy.class)),
            @Parameter(name = "execType", description = "COMMAND_TYPE", schema = @Schema(implementation = CommandType.class)),
            @Parameter(name = "warningType", description = "WARNING_TYPE", required = true, schema = @Schema(implementation = WarningType.class)),
            @Parameter(name = "warningGroupId", description = "WARNING_GROUP_ID", schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "workerGroup", description = "WORKER_GROUP", schema = @Schema(implementation = String.class, example = "default")),
            @Parameter(name = "tenantCode", description = "TENANT_CODE", schema = @Schema(implementation = String.class, example = "default")),
            @Parameter(name = "environmentCode", description = "ENVIRONMENT_CODE", schema = @Schema(implementation = long.class, example = "-1")),
            @Parameter(name = "timeout", description = "TIMEOUT", schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "dryRun", description = "DRY_RUN", schema = @Schema(implementation = int.class, example = "0")),
    })
    @PostMapping(value = "/task-instance/{code}/start")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(START_TASK_INSTANCE_ERROR)
    @OperatorLog(auditType = AuditType.TASK_START)
    public Result<Boolean> startStreamTaskInstance(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                   @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                   @Parameter(name = "code", description = "TASK_CODE", required = true) @PathVariable long code,
                                                   @RequestParam(value = "version", required = true) int version,
                                                   @RequestParam(value = "warningGroupId", required = false, defaultValue = "0") Integer warningGroupId,
                                                   @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
                                                   @RequestParam(value = "tenantCode", required = false, defaultValue = "default") String tenantCode,
                                                   @RequestParam(value = "environmentCode", required = false, defaultValue = "-1") Long environmentCode,
                                                   @RequestParam(value = "startParams", required = false) String startParams,
                                                   @RequestParam(value = "dryRun", defaultValue = "0", required = false) int dryRun) {

        Map<String, String> startParamMap = null;
        if (startParams != null) {
            startParamMap = JSONUtils.toMap(startParams);
        }

        log.info("Start to execute stream task instance, projectCode:{}, taskDefinitionCode:{}, taskVersion:{}.",
                projectCode, code, version);
        execService.execStreamTaskInstance(loginUser, projectCode, code, version, warningGroupId, workerGroup,
                tenantCode, environmentCode, startParamMap, dryRun);
        return Result.success(true);
    }

    /**
     * do action to process instance: pause, stop, repeat, recover from pause, recover from stop
     *
     * @param loginUser         login user
     * @param projectCode       project code
     * @param processInstanceId process instance id
     * @param startNodeList     start node list
     * @param taskDependType    task depend type
     * @return execute result code
     */
    @Operation(summary = "execute-task", description = "EXECUTE_ACTION_TO_WORKFLOW_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "processInstanceId", description = "WORKFLOW_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "startNodeList", description = "START_NODE_LIST", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "taskDependType", description = "TASK_DEPEND_TYPE", required = true, schema = @Schema(implementation = TaskDependType.class))
    })
    @PostMapping(value = "/execute-task")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(EXECUTE_WORKFLOW_INSTANCE_ERROR)
    @OperatorLog(auditType = AuditType.WORKFLOW_EXECUTE)
    public Result executeTask(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                              @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                              @RequestParam("processInstanceId") Integer processInstanceId,
                              @RequestParam("startNodeList") String startNodeList,
                              @RequestParam("taskDependType") TaskDependType taskDependType) {
        log.info("Start to execute task in process instance, projectCode:{}, processInstanceId:{}.",
                projectCode,
                processInstanceId);
        return execService.executeTask(loginUser, projectCode, processInstanceId, startNodeList, taskDependType);
    }

}
