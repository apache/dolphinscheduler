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
package cn.escheduler.api.controller;


import cn.escheduler.api.enums.ExecuteType;
import cn.escheduler.api.enums.Status;
import cn.escheduler.api.service.ExecutorService;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.enums.*;
import cn.escheduler.dao.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static cn.escheduler.api.enums.Status.*;


/**
 * execute task controller
 */
@RestController
@RequestMapping("projects/{projectName}/executors")
public class ExecutorController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorController.class);

    @Autowired
    private ExecutorService execService;

    /**
     * execute process instance
     */
    @PostMapping(value = "start-process-instance")
    @ResponseStatus(HttpStatus.OK)
    public Result startProcessInstance(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @PathVariable String projectName,
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
                    loginUser.getUserName(), projectName, processDefinitionId, scheduleTime, failureStrategy,
                    taskDependType, warningType, warningGroupId,receivers,receiversCc,runMode,processInstancePriority,
                    workerGroupId, timeout);

            if (timeout == null) {
                timeout = cn.escheduler.common.Constants.MAX_TASK_TIMEOUT;
            }

            Map<String, Object> result = execService.execProcessInstance(loginUser, projectName, processDefinitionId, scheduleTime, execType, failureStrategy,
                            startNodeList, taskDependType, warningType,
                    warningGroupId,receivers,receiversCc, runMode,processInstancePriority, workerGroupId, timeout);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(START_PROCESS_INSTANCE_ERROR.getMsg(),e);
            return error(Status.START_PROCESS_INSTANCE_ERROR.getCode(), Status.START_PROCESS_INSTANCE_ERROR.getMsg());
        }
    }


    /**
     * do action to process instance：pause, stop, repeat, recover from pause, recover from stop
     *
     * @param loginUser
     * @param projectName
     * @param processInstanceId
     * @return
     */
    @PostMapping(value = "/execute")
    @ResponseStatus(HttpStatus.OK)
    public Result execute(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @PathVariable String projectName,
                                                  @RequestParam("processInstanceId") Integer processInstanceId,
                                                  @RequestParam("executeType") ExecuteType executeType
    ) {
        try {
            logger.info("execute command, login user: {}, project:{}, process instance id:{}, execute type:{}",
                    loginUser.getUserName(), projectName, processInstanceId, executeType.toString());
            Map<String, Object> result = execService.execute(loginUser, projectName, processInstanceId, executeType);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(EXECUTE_PROCESS_INSTANCE_ERROR.getMsg(),e);
            return error(EXECUTE_PROCESS_INSTANCE_ERROR.getCode(), EXECUTE_PROCESS_INSTANCE_ERROR.getMsg());
        }
    }

    /**
     * check process definition and all of the son process definitions is on line.
     *
     * @param loginUser
     * @param processDefinitionId
     * @return
     */
    @PostMapping(value = "/start-check")
    @ResponseStatus(HttpStatus.OK)
    public Result startCheckProcessDefinition(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                             @RequestParam(value = "processDefinitionId") int processDefinitionId) {
        logger.info("login user {}, check process definition", loginUser.getUserName(), processDefinitionId);
        try {
            Map<String, Object> result = execService.startCheckByProcessDefinedId(processDefinitionId);
            return returnDataList(result);

        } catch (Exception e) {
            logger.error(CHECK_PROCESS_DEFINITION_ERROR.getMsg(),e);
            return error(CHECK_PROCESS_DEFINITION_ERROR.getCode(), CHECK_PROCESS_DEFINITION_ERROR.getMsg());
        }
    }

    /**
     * query recipients and copyers by process definition ID
     *
     * @param loginUser
     * @param processDefinitionId
     * @return
     */
    @GetMapping(value = "/get-receiver-cc")
    @ResponseStatus(HttpStatus.OK)
    public Result getReceiverCc(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                @RequestParam(value = "processDefinitionId",required = false) Integer processDefinitionId,
                                @RequestParam(value = "processInstanceId",required = false) Integer processInstanceId) {
        logger.info("login user {}, get process definition receiver and cc", loginUser.getUserName());
        try {
            Map<String, Object> result = execService.getReceiverCc(processDefinitionId,processInstanceId);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(QUERY_RECIPIENTS_AND_COPYERS_BY_PROCESS_DEFINITION_ERROR.getMsg(),e);
            return error(QUERY_RECIPIENTS_AND_COPYERS_BY_PROCESS_DEFINITION_ERROR.getCode(), QUERY_RECIPIENTS_AND_COPYERS_BY_PROCESS_DEFINITION_ERROR.getMsg());
        }
    }


}
