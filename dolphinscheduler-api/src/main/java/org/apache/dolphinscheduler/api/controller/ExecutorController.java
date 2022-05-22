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
import static org.apache.dolphinscheduler.api.enums.Status.CHECK_PROCESS_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.EXECUTE_PROCESS_INSTANCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.START_PROCESS_INSTANCE_ERROR;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * executor controller
 */
@Api(tags = "EXECUTOR_TAG")
@RestController
@RequestMapping("projects/{projectCode}/executors")
public class ExecutorController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInstanceController.class);

    @Autowired
    private ExecutorService execService;

    /**
     * execute process instance
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCode process definition code
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
     * @param timeout timeout
     * @param expectedParallelismNumber the expected parallelism number when execute complement in parallel mode
     * @return start process result code
     */
    @ApiOperation(value = "startProcessInstance", notes = "RUN_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionCode", value = "PROCESS_DEFINITION_CODE", required = true, dataType = "Long", example = "100"),
            @ApiImplicitParam(name = "scheduleTime", value = "SCHEDULE_TIME", required = true, dataType = "String", example = "2022-04-06 00:00:00,2022-04-06 00:00:00"),
            @ApiImplicitParam(name = "failureStrategy", value = "FAILURE_STRATEGY", required = true, dataType = "FailureStrategy"),
            @ApiImplicitParam(name = "startNodeList", value = "START_NODE_LIST", dataType = "String"),
            @ApiImplicitParam(name = "taskDependType", value = "TASK_DEPEND_TYPE", dataType = "TaskDependType"),
            @ApiImplicitParam(name = "execType", value = "COMMAND_TYPE", dataType = "CommandType"),
            @ApiImplicitParam(name = "warningType", value = "WARNING_TYPE", required = true, dataType = "WarningType"),
            @ApiImplicitParam(name = "warningGroupId", value = "WARNING_GROUP_ID", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "runMode", value = "RUN_MODE", dataType = "RunMode"),
            @ApiImplicitParam(name = "processInstancePriority", value = "PROCESS_INSTANCE_PRIORITY", required = true, dataType = "Priority"),
            @ApiImplicitParam(name = "workerGroup", value = "WORKER_GROUP", dataType = "String", example = "default"),
            @ApiImplicitParam(name = "environmentCode", value = "ENVIRONMENT_CODE", dataType = "Long", example = "-1"),
            @ApiImplicitParam(name = "timeout", value = "TIMEOUT", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "expectedParallelismNumber", value = "EXPECTED_PARALLELISM_NUMBER", dataType = "Int" , example = "8"),
            @ApiImplicitParam(name = "dryRun", value = "DRY_RUN", dataType = "Int", example = "0"),
            @ApiImplicitParam(name = "complementDependentMode", value = "COMPLEMENT_DEPENDENT_MODE", dataType = "complementDependentMode")
    })
    @PostMapping(value = "start-process-instance")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(START_PROCESS_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result startProcessInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                       @RequestParam(value = "processDefinitionCode") long processDefinitionCode,
                                       @RequestParam(value = "scheduleTime") String scheduleTime,
                                       @RequestParam(value = "failureStrategy") FailureStrategy failureStrategy,
                                       @RequestParam(value = "startNodeList", required = false) String startNodeList,
                                       @RequestParam(value = "taskDependType", required = false) TaskDependType taskDependType,
                                       @RequestParam(value = "execType", required = false) CommandType execType,
                                       @RequestParam(value = "warningType") WarningType warningType,
                                       @RequestParam(value = "warningGroupId", required = false, defaultValue = "0") Integer warningGroupId,
                                       @RequestParam(value = "runMode", required = false) RunMode runMode,
                                       @RequestParam(value = "processInstancePriority", required = false) Priority processInstancePriority,
                                       @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
                                       @RequestParam(value = "environmentCode", required = false, defaultValue = "-1") Long environmentCode,
                                       @RequestParam(value = "timeout", required = false) Integer timeout,
                                       @RequestParam(value = "startParams", required = false) String startParams,
                                       @RequestParam(value = "expectedParallelismNumber", required = false) Integer expectedParallelismNumber,
                                       @RequestParam(value = "dryRun", defaultValue = "0", required = false) int dryRun,
                                       @RequestParam(value = "complementDependentMode", required = false) ComplementDependentMode complementDependentMode) {

        if (timeout == null) {
            timeout = Constants.MAX_TASK_TIMEOUT;
        }
        Map<String, String> startParamMap = null;
        if (startParams != null) {
            startParamMap = JSONUtils.toMap(startParams);
        }

        if (complementDependentMode == null) {
            complementDependentMode = ComplementDependentMode.OFF_MODE;
        }

        Map<String, Object> result = execService.execProcessInstance(loginUser, projectCode, processDefinitionCode,
                scheduleTime, execType, failureStrategy,
                startNodeList, taskDependType, warningType, warningGroupId, runMode, processInstancePriority,
                workerGroup, environmentCode, timeout, startParamMap, expectedParallelismNumber, dryRun, complementDependentMode);
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
     * @param timeout timeout
     * @param expectedParallelismNumber the expected parallelism number when execute complement in parallel mode
     * @return start process result code
     */
    @ApiOperation(value = "batchStartProcessInstance", notes = "BATCH_RUN_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionCodes", value = "PROCESS_DEFINITION_CODES", required = true, dataType = "String", example = "1,2,3"),
            @ApiImplicitParam(name = "scheduleTime", value = "SCHEDULE_TIME", required = true, dataType = "String", example = "2022-04-06 00:00:00,2022-04-06 00:00:00"),
            @ApiImplicitParam(name = "failureStrategy", value = "FAILURE_STRATEGY", required = true, dataType = "FailureStrategy"),
            @ApiImplicitParam(name = "startNodeList", value = "START_NODE_LIST", dataType = "String"),
            @ApiImplicitParam(name = "taskDependType", value = "TASK_DEPEND_TYPE", dataType = "TaskDependType"),
            @ApiImplicitParam(name = "execType", value = "COMMAND_TYPE", dataType = "CommandType"),
            @ApiImplicitParam(name = "warningType", value = "WARNING_TYPE", required = true, dataType = "WarningType"),
            @ApiImplicitParam(name = "warningGroupId", value = "WARNING_GROUP_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "runMode", value = "RUN_MODE", dataType = "RunMode"),
            @ApiImplicitParam(name = "processInstancePriority", value = "PROCESS_INSTANCE_PRIORITY", required = true, dataType = "Priority"),
            @ApiImplicitParam(name = "workerGroup", value = "WORKER_GROUP", dataType = "String", example = "default"),
            @ApiImplicitParam(name = "environmentCode", value = "ENVIRONMENT_CODE", dataType = "Long", example = "-1"),
            @ApiImplicitParam(name = "timeout", value = "TIMEOUT", dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "expectedParallelismNumber", value = "EXPECTED_PARALLELISM_NUMBER", dataType = "Int", example = "8"),
            @ApiImplicitParam(name = "dryRun", value = "DRY_RUN", dataType = "Int", example = "0"),
            @ApiImplicitParam(name = "complementDependentMode", value = "COMPLEMENT_DEPENDENT_MODE", dataType = "complementDependentMode")
    })
    @PostMapping(value = "batch-start-process-instance")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(START_PROCESS_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result batchStartProcessInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                            @RequestParam(value = "processDefinitionCodes") String processDefinitionCodes,
                                            @RequestParam(value = "scheduleTime") String scheduleTime,
                                            @RequestParam(value = "failureStrategy") FailureStrategy failureStrategy,
                                            @RequestParam(value = "startNodeList", required = false) String startNodeList,
                                            @RequestParam(value = "taskDependType", required = false) TaskDependType taskDependType,
                                            @RequestParam(value = "execType", required = false) CommandType execType,
                                            @RequestParam(value = "warningType") WarningType warningType,
                                            @RequestParam(value = "warningGroupId", required = false) int warningGroupId,
                                            @RequestParam(value = "runMode", required = false) RunMode runMode,
                                            @RequestParam(value = "processInstancePriority", required = false) Priority processInstancePriority,
                                            @RequestParam(value = "workerGroup", required = false, defaultValue = "default") String workerGroup,
                                            @RequestParam(value = "environmentCode", required = false, defaultValue = "-1") Long environmentCode,
                                            @RequestParam(value = "timeout", required = false) Integer timeout,
                                            @RequestParam(value = "startParams", required = false) String startParams,
                                            @RequestParam(value = "expectedParallelismNumber", required = false) Integer expectedParallelismNumber,
                                            @RequestParam(value = "dryRun", defaultValue = "0", required = false) int dryRun,
                                            @RequestParam(value = "complementDependentMode", required = false) ComplementDependentMode complementDependentMode) {

        if (timeout == null) {
            timeout = Constants.MAX_TASK_TIMEOUT;
        }

        Map<String, String> startParamMap = null;
        if (startParams != null) {
            startParamMap = JSONUtils.toMap(startParams);
        }

        if (complementDependentMode == null) {
            complementDependentMode = ComplementDependentMode.OFF_MODE;
        }

        Map<String, Object> result = new HashMap<>();
        List<String> processDefinitionCodeArray = Arrays.asList(processDefinitionCodes.split(Constants.COMMA));
        List<String> startFailedProcessDefinitionCodeList = new ArrayList<>();

        processDefinitionCodeArray = processDefinitionCodeArray.stream().distinct().collect(Collectors.toList());

        for (String strProcessDefinitionCode : processDefinitionCodeArray) {
            long processDefinitionCode = Long.parseLong(strProcessDefinitionCode);
            result = execService.execProcessInstance(loginUser, projectCode, processDefinitionCode, scheduleTime, execType, failureStrategy,
                    startNodeList, taskDependType, warningType, warningGroupId, runMode, processInstancePriority,
                    workerGroup, environmentCode, timeout, startParamMap, expectedParallelismNumber, dryRun,
                    complementDependentMode);

            if (!Status.SUCCESS.equals(result.get(Constants.STATUS))) {
                startFailedProcessDefinitionCodeList.add(String.valueOf(processDefinitionCode));
            }
        }

        if (!startFailedProcessDefinitionCodeList.isEmpty()) {
            putMsg(result, Status.BATCH_START_PROCESS_INSTANCE_ERROR, String.join(Constants.COMMA, startFailedProcessDefinitionCodeList));
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
    @ApiOperation(value = "execute", notes = "EXECUTE_ACTION_TO_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "executeType", value = "EXECUTE_TYPE", required = true, dataType = "ExecuteType")
    })
    @PostMapping(value = "/execute")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(EXECUTE_PROCESS_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result execute(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                          @ApiParam(name = "projectCode", value = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                          @RequestParam("processInstanceId") Integer processInstanceId,
                          @RequestParam("executeType") ExecuteType executeType
    ) {
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
    @ApiOperation(value = "batchExecute", notes = "BATCH_EXECUTE_ACTION_TO_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "projectCode", value = "PROJECT_CODE", required = true, dataType = "Int"),
        @ApiImplicitParam(name = "processInstanceIds", value = "PROCESS_INSTANCE_IDS", required = true, dataType = "String"),
        @ApiImplicitParam(name = "executeType", value = "EXECUTE_TYPE", required = true, dataType = "ExecuteType")
    })
    @PostMapping(value = "/batch-execute")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_EXECUTE_PROCESS_INSTANCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result batchExecute(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                               @PathVariable long projectCode,
                               @RequestParam("processInstanceIds") String processInstanceIds,
                               @RequestParam("executeType") ExecuteType executeType
    ) {
        Map<String, Object> result = new HashMap<>();
        List<String> executeFailedIdList = new ArrayList<>();
        if (!StringUtils.isEmpty(processInstanceIds)) {
            String[] processInstanceIdArray = processInstanceIds.split(Constants.COMMA);

            for (String strProcessInstanceId : processInstanceIdArray) {
                int processInstanceId = Integer.parseInt(strProcessInstanceId);
                try {
                    Map<String, Object> singleResult = execService.execute(loginUser, projectCode, processInstanceId, executeType);
                    if (!Status.SUCCESS.equals(singleResult.get(Constants.STATUS))) {
                        executeFailedIdList.add((String) singleResult.get(Constants.MSG));
                        logger.error((String) singleResult.get(Constants.MSG));
                    }
                } catch (Exception e) {
                    executeFailedIdList.add(MessageFormat.format(Status.PROCESS_INSTANCE_ERROR.getMsg(), strProcessInstanceId));
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
    @ApiOperation(value = "startCheckProcessDefinition", notes = "START_CHECK_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionCode", value = "PROCESS_DEFINITION_CODE", required = true, dataType = "Long", example = "100")
    })
    @PostMapping(value = "/start-check")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CHECK_PROCESS_DEFINITION_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result startCheckProcessDefinition(@RequestParam(value = "processDefinitionCode") long processDefinitionCode) {
        Map<String, Object> result = execService.startCheckByProcessDefinedCode(processDefinitionCode);
        return returnDataList(result);
    }
}
