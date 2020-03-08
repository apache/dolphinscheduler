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


import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import io.swagger.annotations.*;
import org.apache.dolphinscheduler.common.enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;


/**
 * execute process controller
 */
@Api(tags = "PROCESS_INSTANCE_EXECUTOR_TAG", position = 1)
@RestController
@RequestMapping("projects/{projectName}/executors")
public class ExecutorController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorController.class);

    @Autowired
    private ExecutorService execService;

    /**
     * execute process instance
     * @param loginUser login user
     * @param projectName project name
     * @param processDefinitionId process definition id
     * @param scheduleTime schedule time
     * @param failureStrategy failure strategy
     * @param startNodeList start nodes list
     * @param taskDependType task depend type
     * @param execType execute type
     * @param warningType warning type
     * @param warningGroupId warning group id
     * @param receivers receivers
     * @param receiversCc receivers cc
     * @param runMode run mode
     * @param processInstancePriority process instance priority
     * @param workerGroupId worker group id
     * @param timeout timeout
     * @return start process result code
     */
    @ApiOperation(value = "startProcessInstance", notes= "RUN_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "scheduleTime", value = "SCHEDULE_TIME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "failureStrategy", value = "FAILURE_STRATEGY", required = true, dataType ="FailureStrategy"),
            @ApiImplicitParam(name = "startNodeList", value = "START_NODE_LIST", dataType ="String"),
            @ApiImplicitParam(name = "taskDependType", value = "TASK_DEPEND_TYPE", dataType ="TaskDependType"),
            @ApiImplicitParam(name = "execType", value = "COMMAND_TYPE", dataType ="CommandType"),
            @ApiImplicitParam(name = "warningType", value = "WARNING_TYPE",required = true, dataType ="WarningType"),
            @ApiImplicitParam(name = "warningGroupId", value = "WARNING_GROUP_ID",required = true, dataType ="Int", example = "100"),
            @ApiImplicitParam(name = "receivers", value = "RECEIVERS",dataType ="String" ),
            @ApiImplicitParam(name = "receiversCc", value = "RECEIVERS_CC",dataType ="String" ),
            @ApiImplicitParam(name = "runMode", value = "RUN_MODE",dataType ="RunMode" ),
            @ApiImplicitParam(name = "processInstancePriority", value = "PROCESS_INSTANCE_PRIORITY", required = true, dataType = "Priority" ),
            @ApiImplicitParam(name = "workerGroupId", value = "WORKER_GROUP_ID", dataType = "Int",example = "100"),
            @ApiImplicitParam(name = "timeout", value = "TIMEOUT", dataType = "Int",example = "100"),
    })
    @PostMapping(value = "start-process-instance")
    @ResponseStatus(HttpStatus.OK)
    public Result startProcessInstance(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                       @RequestParam(value = "processDefinitionId") int processDefinitionId,
                                       @RequestParam(value = "scheduleTime", required = false) String scheduleTime,
                                       @RequestParam(value = "failureStrategy", required = true) FailureStrategy failureStrategy,
                                       @RequestParam(value = "startNodeList", required = false) String startNodeList,
                                       @RequestParam(value = "taskDependType", required = false) TaskDependType taskDependType,
                                       @RequestParam(value = "execType", required = false) CommandType execType,
                                       @RequestParam(value = "warningType", required = true) WarningType warningType,
                                       @RequestParam(value = "warningGroupId", required = false) int warningGroupId,
                                       @RequestParam(value = "receivers", required = false) String receivers,
                                       @RequestParam(value = "receiversCc", required = false) String receiversCc,
                                       @RequestParam(value = "runMode", required = false) RunMode runMode,
                                       @RequestParam(value = "processInstancePriority", required = false) Priority processInstancePriority,
                                       @RequestParam(value = "workerGroupId", required = false, defaultValue = "-1") int workerGroupId,
                                       @RequestParam(value = "timeout", required = false) Integer timeout) {
        try {
            logger.info("login user {}, start process instance, project name: {}, process definition id: {}, schedule time: {}, "
                            + "failure policy: {}, node name: {}, node dep: {}, notify type: {}, "
                            + "notify group id: {},receivers:{},receiversCc:{}, run mode: {},process instance priority:{}, workerGroupId: {}, timeout: {}",
                    loginUser.getUserName(), projectName, processDefinitionId, scheduleTime,
                    failureStrategy, startNodeList, taskDependType, warningType, warningGroupId,receivers,receiversCc,runMode,processInstancePriority,
                    workerGroupId, timeout);

            if (timeout == null) {
                timeout = Constants.MAX_TASK_TIMEOUT;
            }

            Map<String, Object> result = execService.execProcessInstance(loginUser, projectName, processDefinitionId, scheduleTime, execType, failureStrategy,
                            startNodeList, taskDependType, warningType,
                    warningGroupId,receivers,receiversCc, runMode,processInstancePriority, workerGroupId, timeout);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(Status.START_PROCESS_INSTANCE_ERROR.getMsg(),e);
            return error(Status.START_PROCESS_INSTANCE_ERROR.getCode(), Status.START_PROCESS_INSTANCE_ERROR.getMsg());
        }
    }


    /**
     * do action to process instance：pause, stop, repeat, recover from pause, recover from stop
     *
     * @param loginUser login user
     * @param projectName project name
     * @param processInstanceId process instance id
     * @param executeType execute type
     * @return execute result code
     */
    @ApiOperation(value = "execute", notes= "EXECUTE_ACTION_TO_PROCESS_INSTANCE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "executeType", value = "EXECUTE_TYPE", required = true, dataType = "ExecuteType")
    })
    @PostMapping(value = "/execute")
    @ResponseStatus(HttpStatus.OK)
    public Result execute(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                          @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                          @RequestParam("processInstanceId") Integer processInstanceId,
                          @RequestParam("executeType") ExecuteType executeType
    ) {
        try {
            logger.info("execute command, login user: {}, project:{}, process instance id:{}, execute type:{}",
                    loginUser.getUserName(), projectName, processInstanceId, executeType);
            Map<String, Object> result = execService.execute(loginUser, projectName, processInstanceId, executeType);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(Status.EXECUTE_PROCESS_INSTANCE_ERROR.getMsg(),e);
            return error(Status.EXECUTE_PROCESS_INSTANCE_ERROR.getCode(), Status.EXECUTE_PROCESS_INSTANCE_ERROR.getMsg());
        }
    }

    /**
     * check process definition and all of the son process definitions is on line.
     *
     * @param loginUser login user
     * @param processDefinitionId process definition id
     * @return check result code
     */
    @ApiOperation(value = "startCheckProcessDefinition", notes= "START_CHECK_PROCESS_DEFINITION_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100")
    })
    @PostMapping(value = "/start-check")
    @ResponseStatus(HttpStatus.OK)
    public Result startCheckProcessDefinition(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                             @RequestParam(value = "processDefinitionId") int processDefinitionId) {
        logger.info("login user {}, check process definition {}", loginUser.getUserName(), processDefinitionId);
        try {
            Map<String, Object> result = execService.startCheckByProcessDefinedId(processDefinitionId);
            return returnDataList(result);

        } catch (Exception e) {
            logger.error(Status.CHECK_PROCESS_DEFINITION_ERROR.getMsg(),e);
            return error(Status.CHECK_PROCESS_DEFINITION_ERROR.getCode(), Status.CHECK_PROCESS_DEFINITION_ERROR.getMsg());
        }
    }

    /**
     * query recipients and copyers by process definition ID
     *
     * @param loginUser login user
     * @param processDefinitionId process definition id
     * @param processInstanceId process instance id
     * @return receivers cc list
     */
    @ApiIgnore
    @ApiOperation(value = "getReceiverCc", notes= "GET_RECEIVER_CC_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processDefinitionId", value = "PROCESS_DEFINITION_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int", example = "100")

    })
    @GetMapping(value = "/get-receiver-cc")
    @ResponseStatus(HttpStatus.OK)
    public Result getReceiverCc(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value = "processDefinitionId",required = false) Integer processDefinitionId,
                                @RequestParam(value = "processInstanceId",required = false) Integer processInstanceId) {
        logger.info("login user {}, get process definition receiver and cc", loginUser.getUserName());
        try {
            Map<String, Object> result = execService.getReceiverCc(processDefinitionId,processInstanceId);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(Status.QUERY_RECIPIENTS_AND_COPYERS_BY_PROCESS_DEFINITION_ERROR.getMsg(),e);
            return error(Status.QUERY_RECIPIENTS_AND_COPYERS_BY_PROCESS_DEFINITION_ERROR.getCode(), Status.QUERY_RECIPIENTS_AND_COPYERS_BY_PROCESS_DEFINITION_ERROR.getMsg());
        }
    }


}
