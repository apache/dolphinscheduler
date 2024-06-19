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

import static org.apache.dolphinscheduler.api.enums.Status.BATCH_EXECUTE_PROCESS_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.BATCH_START_PROCESS_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.CHECK_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.EXECUTE_PROCESS_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_EXECUTING_WORKFLOW_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.START_PROCESS_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.START_TASK_INSTANCE_ERROR;

import org.apache.dolphinscheduler.api.audit.OperatorLog;
import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.ExecutionOrder;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.extract.master.dto.WorkflowExecuteDto;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCode process definition code
     * @param scheduleTime schedule time when CommandType is COMPLEMENT_DATA  there are two ways to transfer parameters 1.date range, for example:{"complementStartDate":"2022-01-01 12:12:12","complementEndDate":"2022-01-6 12:12:12"} 2.manual input,  for example:{"complementScheduleDateList":"2022-01-01 00:00:00,2022-01-02 12:12:12,2022-01-03 12:12:12"}
     * @param failureStrategy failure strategy
     * @param startNodeList start nodes list
     * @param taskDependType task depend type
     * @param execType execute type
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param runMode run mode
     * @param processInstancePriority process instance priority
     * @param workerGroup worker group
     * @param timeout timeout
     * @param expectedParallelismNumber the expected parallelism number when execute complement in parallel mode
     * @param testFlag testFlag
     * @param executionOrder complement data in some kind of order
     * @return start process result code
     */
    @Operation(summary = "startProcessInstance", description = "RUN_PROCESS_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "processDefinitionCode", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = Long.class), example = "100"),
            @Parameter(name = "scheduleTime", description = "SCHEDULE_TIME", required = true, schema = @Schema(implementation = String.class), example = "2022-04-06 00:00:00,2022-04-06 00:00:00"),
            @Parameter(name = "failureStrategy", description = "FAILURE_STRATEGY", required = true, schema = @Schema(implementation = FailureStrategy.class)),
            @Parameter(name = "startNodeList", description = "START_NODE_LIST", schema = @Schema(implementation = String.class)),
            @Parameter(name = "taskDependType", description = "TASK_DEPEND_TYPE", schema = @Schema(implementation = TaskDependType.class)),
            @Parameter(name = "execType", description = "COMMAND_TYPE", schema = @Schema(implementation = CommandType.class)),
            @Parameter(name = "warningType", description = "WARNING_TYPE", required = true, schema = @Schema(implementation = WarningType.class)),
            @Parameter(name = "warningGroupId", description = "WARNING_GROUP_ID", schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "runMode", description = "RUN_MODE", schema = @Schema(implementation = RunMode.class)),
            @Parameter(name = "processInstancePriority", description = "PROCESS_INSTANCE_PRIORITY", required = true, schema = @Schema(implementation = Priority.class)),
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
    @ApiException(START_PROCESS_INSTANCE_ERROR)
    @OperatorLog(auditType = AuditType.PROCESS_START)
    public Result startProcessInstance(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                       @RequestParam(value = "processDefinitionCode") long processDefinitionCode,
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
                                       @RequestParam(value = "timeout", required = false) Integer timeout,
                                       @RequestParam(value = "startParams", required = false) String startParams,
                                       @RequestParam(value = "expectedParallelismNumber", required = false) Integer expectedParallelismNumber,
                                       @RequestParam(value = "dryRun", defaultValue = "0", required = false) int dryRun,
                                       @RequestParam(value = "testFlag", defaultValue = "0") int testFlag,
                                       @RequestParam(value = "complementDependentMode", required = false) ComplementDependentMode complementDependentMode,
                                       @RequestParam(value = "version", required = false) Integer version,
                                       @RequestParam(value = "allLevelDependent", required = false, defaultValue = "false") boolean allLevelDependent,
                                       @RequestParam(value = "executionOrder", required = false) ExecutionOrder executionOrder) {

        if (timeout == null) {
            timeout = Constants.MAX_TASK_TIMEOUT;
        }

        List<Property> startParamList = PropertyUtils.startParamsTransformPropertyList(startParams);

        if (complementDependentMode == null) {
            complementDependentMode = ComplementDependentMode.OFF_MODE;
        }

        Map<String, Object> result = execService.execProcessInstance(loginUser, projectCode, processDefinitionCode,
                scheduleTime, execType, failureStrategy,
                startNodeList, taskDependType, warningType, warningGroupId, runMode, processInstancePriority,
                workerGroup, tenantCode, environmentCode, timeout, startParamList, expectedParallelismNumber, dryRun,
                testFlag,
                complementDependentMode, version, allLevelDependent, executionOrder);
        return returnDataList(result);
    }

    /**
     * batch execute process instance
     * If any processDefinitionCode cannot be found, the failure information is returned and the status is set to
     * failed. The successful task will run normally and will not stop
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCodes process definition codes
     * @param scheduleTime schedule time
     * @param failureStrategy failure strategy
     * @param startNodeList start nodes list
     * @param taskDependType task depend type
     * @param execType execute type
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param runMode run mode
     * @param processInstancePriority process instance priority
     * @param workerGroup worker group
     * @param tenantCode tenant code
     * @param timeout timeout
     * @param expectedParallelismNumber the expected parallelism number when execute complement in parallel mode
     * @param testFlag testFlag
     * @param executionOrder complement data in some kind of order
     * @return start process result code
     */
    @Operation(summary = "batchStartProcessInstance", description = "BATCH_RUN_PROCESS_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "processDefinitionCodes", description = "PROCESS_DEFINITION_CODE_LIST", required = true, schema = @Schema(implementation = String.class, example = "1,2,3")),
            @Parameter(name = "scheduleTime", description = "SCHEDULE_TIME", required = true, schema = @Schema(implementation = String.class, example = "2022-04-06 00:00:00,2022-04-06 00:00:00")),
            @Parameter(name = "failureStrategy", description = "FAILURE_STRATEGY", required = true, schema = @Schema(implementation = FailureStrategy.class)),
            @Parameter(name = "startNodeList", description = "START_NODE_LIST", schema = @Schema(implementation = String.class)),
            @Parameter(name = "taskDependType", description = "TASK_DEPEND_TYPE", schema = @Schema(implementation = TaskDependType.class)),
            @Parameter(name = "execType", description = "COMMAND_TYPE", schema = @Schema(implementation = CommandType.class)),
            @Parameter(name = "warningType", description = "WARNING_TYPE", required = true, schema = @Schema(implementation = WarningType.class)),
            @Parameter(name = "warningGroupId", description = "WARNING_GROUP_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "runMode", description = "RUN_MODE", schema = @Schema(implementation = RunMode.class)),
            @Parameter(name = "processInstancePriority", description = "PROCESS_INSTANCE_PRIORITY", required = true, schema = @Schema(implementation = Priority.class)),
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
    @PostMapping(value = "batch-start-process-instance")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_START_PROCESS_INSTANCE_ERROR)
    @OperatorLog(auditType = AuditType.PROCESS_BATCH_START)
    public Result batchStartProcessInstance(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
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
                                            @RequestParam(value = "timeout", required = false) Integer timeout,
                                            @RequestParam(value = "startParams", required = false) String startParams,
                                            @RequestParam(value = "expectedParallelismNumber", required = false) Integer expectedParallelismNumber,
                                            @RequestParam(value = "dryRun", defaultValue = "0", required = false) int dryRun,
                                            @RequestParam(value = "testFlag", defaultValue = "0") int testFlag,
                                            @RequestParam(value = "complementDependentMode", required = false) ComplementDependentMode complementDependentMode,
                                            @RequestParam(value = "allLevelDependent", required = false, defaultValue = "false") boolean allLevelDependent,
                                            @RequestParam(value = "executionOrder", required = false) ExecutionOrder executionOrder) {

        if (timeout == null) {
            log.debug("Parameter timeout set to {} due to null.", Constants.MAX_TASK_TIMEOUT);
            timeout = Constants.MAX_TASK_TIMEOUT;
        }

        List<Property> startParamList = PropertyUtils.startParamsTransformPropertyList(startParams);

        if (complementDependentMode == null) {
            log.debug("Parameter complementDependentMode set to {} due to null.", ComplementDependentMode.OFF_MODE);
            complementDependentMode = ComplementDependentMode.OFF_MODE;
        }

        Map<String, Object> result = new HashMap<>();
        List<String> processDefinitionCodeArray = Arrays.asList(processDefinitionCodes.split(Constants.COMMA));
        List<String> startFailedProcessDefinitionCodeList = new ArrayList<>();

        processDefinitionCodeArray = processDefinitionCodeArray.stream().distinct().collect(Collectors.toList());

        for (String strProcessDefinitionCode : processDefinitionCodeArray) {
            long processDefinitionCode = Long.parseLong(strProcessDefinitionCode);
            result = execService.execProcessInstance(loginUser, projectCode, processDefinitionCode, scheduleTime,
                    execType, failureStrategy,
                    startNodeList, taskDependType, warningType, warningGroupId, runMode, processInstancePriority,
                    workerGroup, tenantCode, environmentCode, timeout, startParamList, expectedParallelismNumber,
                    dryRun,
                    testFlag,
                    complementDependentMode, null, allLevelDependent, executionOrder);

            if (!Status.SUCCESS.equals(result.get(Constants.STATUS))) {
                log.error("Process definition start failed, projectCode:{}, processDefinitionCode:{}.", projectCode,
                        processDefinitionCode);
                startFailedProcessDefinitionCodeList.add(String.valueOf(processDefinitionCode));
            } else {
                log.info("Start process definition complete, projectCode:{}, processDefinitionCode:{}.", projectCode,
                        processDefinitionCode);
            }
        }

        if (!startFailedProcessDefinitionCodeList.isEmpty()) {
            putMsg(result, BATCH_START_PROCESS_INSTANCE_ERROR,
                    String.join(Constants.COMMA, startFailedProcessDefinitionCodeList));
        }

        return returnDataList(result);
    }

    /**
     * do action to process instance: pause, stop, repeat, recover from pause, recover from stop
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processInstanceId process instance id
     * @param executeType execute type
     * @return execute result code
     */
    @Operation(summary = "execute", description = "EXECUTE_ACTION_TO_PROCESS_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "processInstanceId", description = "PROCESS_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "executeType", description = "EXECUTE_TYPE", required = true, schema = @Schema(implementation = ExecuteType.class))
    })
    @PostMapping(value = "/execute")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(EXECUTE_PROCESS_INSTANCE_ERROR)
    @OperatorLog(auditType = AuditType.PROCESS_EXECUTE)
    public Result execute(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                          @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                          @RequestParam("processInstanceId") Integer processInstanceId,
                          @RequestParam("executeType") ExecuteType executeType) {
        Map<String, Object> result = execService.execute(loginUser, projectCode, processInstanceId, executeType);
        return returnDataList(result);
    }

    /**
     * batch execute and do action to process instance
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processInstanceIds process instance ids, delimiter by "," if more than one id
     * @param executeType execute type
     * @return execute result code
     */
    @Operation(summary = "batchExecute", description = "BATCH_EXECUTE_ACTION_TO_PROCESS_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = int.class)),
            @Parameter(name = "processInstanceIds", description = "PROCESS_INSTANCE_IDS", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "executeType", description = "EXECUTE_TYPE", required = true, schema = @Schema(implementation = ExecuteType.class))
    })
    @PostMapping(value = "/batch-execute")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_EXECUTE_PROCESS_INSTANCE_ERROR)
    @OperatorLog(auditType = AuditType.PROCESS_BATCH_RERUN)
    public Result batchExecute(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @PathVariable long projectCode,
                               @RequestParam("processInstanceIds") String processInstanceIds,
                               @RequestParam("executeType") ExecuteType executeType) {
        Map<String, Object> result = new HashMap<>();
        List<String> executeFailedIdList = new ArrayList<>();
        if (!StringUtils.isEmpty(processInstanceIds)) {
            String[] processInstanceIdArray = processInstanceIds.split(Constants.COMMA);

            for (String strProcessInstanceId : processInstanceIdArray) {
                int processInstanceId = Integer.parseInt(strProcessInstanceId);
                try {
                    Map<String, Object> singleResult =
                            execService.execute(loginUser, projectCode, processInstanceId, executeType);
                    if (!Status.SUCCESS.equals(singleResult.get(Constants.STATUS))) {
                        log.error("Start to execute process instance error, projectCode:{}, processInstanceId:{}.",
                                projectCode, processInstanceId);
                        executeFailedIdList.add((String) singleResult.get(Constants.MSG));
                    } else
                        log.info("Start to execute process instance complete, projectCode:{}, processInstanceId:{}.",
                                projectCode, processInstanceId);
                } catch (Exception e) {
                    executeFailedIdList
                            .add(MessageFormat.format(Status.PROCESS_INSTANCE_ERROR.getMsg(), strProcessInstanceId));
                }
            }
        }
        if (!executeFailedIdList.isEmpty()) {
            putMsg(result, Status.BATCH_EXECUTE_PROCESS_INSTANCE_ERROR, String.join("\n", executeFailedIdList));
        } else {
            putMsg(result, Status.SUCCESS);
        }
        return returnDataList(result);
    }

    /**
     * check process definition and all the son process definitions is online.
     *
     * @param processDefinitionCode process definition code
     * @return check result code
     */
    @Operation(summary = "startCheckProcessDefinition", description = "START_CHECK_PROCESS_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "processDefinitionCode", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "100"))
    })
    @PostMapping(value = "/start-check")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CHECK_PROCESS_DEFINITION_ERROR)
    public Result startCheckProcessDefinition(@RequestParam(value = "processDefinitionCode") long processDefinitionCode) {
        Map<String, Object> result = execService.startCheckByProcessDefinedCode(processDefinitionCode);
        return returnDataList(result);
    }

    /**
     * query execute data of processInstance from master
     */
    @Operation(summary = "queryExecutingWorkflow", description = "QUERY_WORKFLOW_EXECUTE_DATA")
    @Parameters({
            @Parameter(name = "processInstanceId", description = "PROCESS_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/query-executing-workflow")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_EXECUTING_WORKFLOW_ERROR)
    public Result queryExecutingWorkflow(@RequestParam("id") Integer processInstanceId) {
        WorkflowExecuteDto workflowExecuteDto =
                execService.queryExecutingWorkflowByProcessInstanceId(processInstanceId);
        return Result.success(workflowExecuteDto);
    }

    /**
     * execute task instance
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code taskDefinitionCode
     * @param version taskDefinitionVersion
     * @param warningGroupId warning group id
     * @param workerGroup worker group
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
     * @param loginUser login user
     * @param projectCode project code
     * @param processInstanceId process instance id
     * @param startNodeList start node list
     * @param taskDependType task depend type
     * @return execute result code
     */
    @Operation(summary = "execute-task", description = "EXECUTE_ACTION_TO_PROCESS_INSTANCE_NOTES")
    @Parameters({
            @Parameter(name = "processInstanceId", description = "PROCESS_INSTANCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100")),
            @Parameter(name = "startNodeList", description = "START_NODE_LIST", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "taskDependType", description = "TASK_DEPEND_TYPE", required = true, schema = @Schema(implementation = TaskDependType.class))
    })
    @PostMapping(value = "/execute-task")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(EXECUTE_PROCESS_INSTANCE_ERROR)
    @OperatorLog(auditType = AuditType.PROCESS_EXECUTE)
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
