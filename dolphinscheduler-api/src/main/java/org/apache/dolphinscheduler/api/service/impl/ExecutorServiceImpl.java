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

package org.apache.dolphinscheduler.api.service.impl;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKFLOW_START;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_START_NODES;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_START_PARAMS;
import static org.apache.dolphinscheduler.common.Constants.COMMA;
import static org.apache.dolphinscheduler.common.Constants.MAX_TASK_TIMEOUT;
import static org.apache.dolphinscheduler.common.Constants.SCHEDULE_TIME_MAX_LENGTH;

import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.MonitorService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.DependentProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.remote.command.TaskExecuteStartCommand;
import org.apache.dolphinscheduler.remote.command.WorkflowStateEventChangeCommand;
import org.apache.dolphinscheduler.remote.command.WorkflowExecutingDataRequestCommand;
import org.apache.dolphinscheduler.remote.command.WorkflowExecutingDataResponseCommand;
import org.apache.dolphinscheduler.remote.dto.WorkflowExecuteDto;
import org.apache.dolphinscheduler.remote.processor.StateEventCallbackService;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.cron.CronUtils;
import org.apache.dolphinscheduler.service.exceptions.CronParseException;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;

/**
 * executor service impl
 */
@Service
public class ExecutorServiceImpl extends BaseServiceImpl implements ExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceImpl.class);

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Autowired
    private ProcessService processService;

    @Autowired
    private StateEventCallbackService stateEventCallbackService;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Autowired
    private TaskGroupQueueMapper taskGroupQueueMapper;

    /**
     * execute process instance
     *
     * @param loginUser                 login user
     * @param projectCode               project code
     * @param processDefinitionCode     process definition code
     * @param cronTime                  cron time
     * @param commandType               command type
     * @param failureStrategy           failure strategy
     * @param startNodeList             start nodelist
     * @param taskDependType            node dependency type
     * @param warningType               warning type
     * @param warningGroupId            notify group id
     * @param processInstancePriority   process instance priority
     * @param workerGroup               worker group name
     * @param environmentCode           environment code
     * @param runMode                   run mode
     * @param timeout                   timeout
     * @param startParams               the global param values which pass to new process instance
     * @param expectedParallelismNumber the expected parallelism number when execute complement in parallel mode
     * @return execute process instance code
     */
    @Override
    public Map<String, Object> execProcessInstance(User loginUser, long projectCode, long processDefinitionCode,
                                                   String cronTime, CommandType commandType,
                                                   FailureStrategy failureStrategy, String startNodeList,
                                                   TaskDependType taskDependType, WarningType warningType,
                                                   int warningGroupId, RunMode runMode,
                                                   Priority processInstancePriority, String workerGroup,
                                                   Long environmentCode, Integer timeout,
                                                   Map<String, String> startParams, Integer expectedParallelismNumber,
                                                   int dryRun, ComplementDependentMode complementDependentMode) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_START);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        // timeout is invalid
        if (timeout <= 0 || timeout > MAX_TASK_TIMEOUT) {
            putMsg(result, Status.TASK_TIMEOUT_PARAMS_ERROR);
            return result;
        }

        // check process define release state
        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);
        result = checkProcessDefinitionValid(projectCode, processDefinition, processDefinitionCode,
                processDefinition.getVersion());
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        if (!checkTenantSuitable(processDefinition)) {
            logger.error("there is not any valid tenant for the process definition: id:{},name:{}, ",
                    processDefinition.getId(), processDefinition.getName());
            putMsg(result, Status.TENANT_NOT_SUITABLE);
            return result;
        }

        if (!checkScheduleTimeNum(commandType, cronTime)) {
            putMsg(result, Status.SCHEDULE_TIME_NUMBER);
            return result;
        }

        // check master exists
        if (!checkMasterExists(result)) {
            return result;
        }
        /**
         * create command
         */
        int create =
                this.createCommand(commandType, processDefinition.getCode(), taskDependType, failureStrategy,
                        startNodeList,
                        cronTime, warningType, loginUser.getId(), warningGroupId, runMode, processInstancePriority,
                        workerGroup,
                        environmentCode, startParams, expectedParallelismNumber, dryRun, complementDependentMode);

        if (create > 0) {
            processDefinition.setWarningGroupId(warningGroupId);
            processDefinitionMapper.updateById(processDefinition);
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.START_PROCESS_INSTANCE_ERROR);
        }
        return result;
    }

    /**
     * check whether master exists
     *
     * @param result result
     * @return master exists return true , otherwise return false
     */
    private boolean checkMasterExists(Map<String, Object> result) {
        // check master server exists
        List<Server> masterServers = monitorService.getServerListFromRegistry(true);

        // no master
        if (masterServers.isEmpty()) {
            putMsg(result, Status.MASTER_NOT_EXISTS);
            return false;
        }
        return true;
    }

    /**
     * @param complementData
     * @param cronTime
     * @return CommandType is COMPLEMENT_DATA and cronTime's number is not greater than 100 return true , otherwise return false
     */
    private boolean checkScheduleTimeNum(CommandType complementData, String cronTime) {
        if (!CommandType.COMPLEMENT_DATA.equals(complementData)) {
            return true;
        }
        if (cronTime == null) {
            return true;
        }
        Map<String, String> cronMap = JSONUtils.toMap(cronTime);
        if (cronMap.containsKey(CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST)) {
            String[] stringDates = cronMap.get(CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST).split(COMMA);
            if (stringDates.length > SCHEDULE_TIME_MAX_LENGTH) {
                return false;
            }
        }
        return true;
    }

    /**
     * check whether the process definition can be executed
     *
     * @param projectCode       project code
     * @param processDefinition process definition
     * @param processDefineCode process definition code
     * @param version           process instance verison
     * @return check result code
     */
    @Override
    public Map<String, Object> checkProcessDefinitionValid(long projectCode, ProcessDefinition processDefinition,
                                                           long processDefineCode, Integer version) {
        Map<String, Object> result = new HashMap<>();
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            // check process definition exists
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(processDefineCode));
        } else if (processDefinition.getReleaseState() != ReleaseState.ONLINE) {
            // check process definition online
            putMsg(result, Status.PROCESS_DEFINE_NOT_RELEASE, String.valueOf(processDefineCode), version);
        } else if (!checkSubProcessDefinitionValid(processDefinition)) {
            // check sub process definition online
            putMsg(result, Status.SUB_PROCESS_DEFINE_NOT_RELEASE);
        } else {
            result.put(Constants.STATUS, Status.SUCCESS);
        }
        return result;
    }

    /**
     * check whether the current process has subprocesses and validate all subprocesses
     *
     * @param processDefinition
     * @return check result
     */
    @Override
    public boolean checkSubProcessDefinitionValid(ProcessDefinition processDefinition) {
        // query all subprocesses under the current process
        List<ProcessTaskRelation> processTaskRelations =
                processTaskRelationMapper.queryDownstreamByProcessDefinitionCode(processDefinition.getCode());
        if (processTaskRelations.isEmpty()) {
            return true;
        }
        Set<Long> relationCodes =
                processTaskRelations.stream().map(ProcessTaskRelation::getPostTaskCode).collect(Collectors.toSet());
        List<TaskDefinition> taskDefinitions = taskDefinitionMapper.queryByCodeList(relationCodes);

        // find out the process definition code
        Set<Long> processDefinitionCodeSet = new HashSet<>();
        taskDefinitions.stream()
                .filter(task -> TaskConstants.TASK_TYPE_SUB_PROCESS.equalsIgnoreCase(task.getTaskType())).forEach(
                        taskDefinition -> processDefinitionCodeSet.add(Long.valueOf(
                                JSONUtils.getNodeString(taskDefinition.getTaskParams(),
                                        Constants.CMD_PARAM_SUB_PROCESS_DEFINE_CODE))));
        if (processDefinitionCodeSet.isEmpty()) {
            return true;
        }

        // check sub releaseState
        List<ProcessDefinition> processDefinitions = processDefinitionMapper.queryByCodes(processDefinitionCodeSet);
        return processDefinitions.stream()
                .filter(definition -> definition.getReleaseState().equals(ReleaseState.OFFLINE))
                .collect(Collectors.toSet())
                .isEmpty();
    }

    /**
     * do action to process instance：pause, stop, repeat, recover from pause, recover from stop
     *
     * @param loginUser         login user
     * @param projectCode       project code
     * @param processInstanceId process instance id
     * @param executeType       execute type
     * @return execute result code
     */
    @Override
    public Map<String, Object> execute(User loginUser, long projectCode, Integer processInstanceId,
                                       ExecuteType executeType) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project

        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode,
                ApiFuncIdentificationConstant.map.get(executeType));
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        // check master exists
        if (!checkMasterExists(result)) {
            return result;
        }

        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processInstanceId);
        if (processInstance == null) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId);
            return result;
        }

        ProcessDefinition processDefinition =
                processService.findProcessDefinition(processInstance.getProcessDefinitionCode(),
                        processInstance.getProcessDefinitionVersion());
        if (executeType != ExecuteType.STOP && executeType != ExecuteType.PAUSE) {
            result =
                    checkProcessDefinitionValid(projectCode, processDefinition,
                            processInstance.getProcessDefinitionCode(),
                            processInstance.getProcessDefinitionVersion());
            if (result.get(Constants.STATUS) != Status.SUCCESS) {
                return result;
            }
        }

        result = checkExecuteType(processInstance, executeType);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (!checkTenantSuitable(processDefinition)) {
            logger.error("there is not any valid tenant for the process definition: id:{},name:{}, ",
                    processDefinition.getId(), processDefinition.getName());
            putMsg(result, Status.TENANT_NOT_SUITABLE);
        }

        // get the startParams user specified at the first starting while repeat running is needed
        Map<String, Object> commandMap =
                JSONUtils.parseObject(processInstance.getCommandParam(), new TypeReference<Map<String, Object>>() {
                });
        String startParams = null;
        if (MapUtils.isNotEmpty(commandMap) && executeType == ExecuteType.REPEAT_RUNNING) {
            Object startParamsJson = commandMap.get(Constants.CMD_PARAM_START_PARAMS);
            if (startParamsJson != null) {
                startParams = startParamsJson.toString();
            }
        }

        switch (executeType) {
            case REPEAT_RUNNING:
                result = insertCommand(loginUser, processInstanceId, processDefinition.getCode(),
                        processDefinition.getVersion(), CommandType.REPEAT_RUNNING, startParams);
                break;
            case RECOVER_SUSPENDED_PROCESS:
                result = insertCommand(loginUser, processInstanceId, processDefinition.getCode(),
                        processDefinition.getVersion(), CommandType.RECOVER_SUSPENDED_PROCESS, startParams);
                break;
            case START_FAILURE_TASK_PROCESS:
                result = insertCommand(loginUser, processInstanceId, processDefinition.getCode(),
                        processDefinition.getVersion(), CommandType.START_FAILURE_TASK_PROCESS, startParams);
                break;
            case STOP:
                if (processInstance.getState() == WorkflowExecutionStatus.READY_STOP) {
                    putMsg(result, Status.PROCESS_INSTANCE_ALREADY_CHANGED, processInstance.getName(),
                            processInstance.getState());
                } else {
                    result =
                            updateProcessInstancePrepare(processInstance, CommandType.STOP,
                                    WorkflowExecutionStatus.READY_STOP);
                }
                break;
            case PAUSE:
                if (processInstance.getState() == WorkflowExecutionStatus.READY_PAUSE) {
                    putMsg(result, Status.PROCESS_INSTANCE_ALREADY_CHANGED, processInstance.getName(),
                            processInstance.getState());
                } else {
                    result = updateProcessInstancePrepare(processInstance, CommandType.PAUSE,
                            WorkflowExecutionStatus.READY_PAUSE);
                }
                break;
            default:
                logger.error("unknown execute type : {}", executeType);
                putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, "unknown execute type");

                break;
        }
        return result;
    }

    @Override
    public Map<String, Object> forceStartTaskInstance(User loginUser, int queueId) {
        Map<String, Object> result = new HashMap<>();
        TaskGroupQueue taskGroupQueue = taskGroupQueueMapper.selectById(queueId);
        // check process instance exist
        ProcessInstance processInstance = processInstanceMapper.selectById(taskGroupQueue.getProcessId());
        if (processInstance == null) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, taskGroupQueue.getProcessId());
            return result;
        }

        // check master exists
        if (!checkMasterExists(result)) {
            return result;
        }
        return forceStart(processInstance, taskGroupQueue);
    }

    /**
     * check tenant suitable
     *
     * @param processDefinition process definition
     * @return true if tenant suitable, otherwise return false
     */
    private boolean checkTenantSuitable(ProcessDefinition processDefinition) {
        Tenant tenant =
                processService.getTenantForProcess(processDefinition.getTenantId(), processDefinition.getUserId());
        return tenant != null;
    }

    /**
     * Check the state of process instance and the type of operation match
     *
     * @param processInstance process instance
     * @param executeType     execute type
     * @return check result code
     */
    private Map<String, Object> checkExecuteType(ProcessInstance processInstance, ExecuteType executeType) {

        Map<String, Object> result = new HashMap<>();
        WorkflowExecutionStatus executionStatus = processInstance.getState();
        boolean checkResult = false;
        switch (executeType) {
            case PAUSE:
            case STOP:
                if (executionStatus.isRunning()) {
                    checkResult = true;
                }
                break;
            case REPEAT_RUNNING:
                if (executionStatus.isFinished()) {
                    checkResult = true;
                }
                break;
            case START_FAILURE_TASK_PROCESS:
                if (executionStatus.isFailure()) {
                    checkResult = true;
                }
                break;
            case RECOVER_SUSPENDED_PROCESS:
                if (executionStatus.isPause() || executionStatus.isStop()) {
                    checkResult = true;
                }
                break;
            default:
                break;
        }
        if (!checkResult) {
            putMsg(result, Status.PROCESS_INSTANCE_STATE_OPERATION_ERROR, processInstance.getName(),
                    executionStatus.toString(), executeType.toString());
        } else {
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * prepare to update process instance command type and status
     *
     * @param processInstance process instance
     * @param commandType     command type
     * @param executionStatus execute status
     * @return update result
     */
    private Map<String, Object> updateProcessInstancePrepare(ProcessInstance processInstance, CommandType commandType,
                                                             WorkflowExecutionStatus executionStatus) {
        Map<String, Object> result = new HashMap<>();

        processInstance.setCommandType(commandType);
        processInstance.addHistoryCmd(commandType);
        processInstance.setState(executionStatus);
        int update = processService.updateProcessInstance(processInstance);

        // determine whether the process is normal
        if (update > 0) {
            // directly send the process instance state change event to target master, not guarantee the event send
            // success
            WorkflowStateEventChangeCommand workflowStateEventChangeCommand = new WorkflowStateEventChangeCommand(
                    processInstance.getId(), 0, processInstance.getState(), processInstance.getId(), 0);
            Host host = new Host(processInstance.getHost());
            stateEventCallbackService.sendResult(host, workflowStateEventChangeCommand.convert2Command());
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.EXECUTE_PROCESS_INSTANCE_ERROR);
        }
        return result;
    }

    /**
     * prepare to update process instance command type and status
     *
     * @param processInstance process instance
     * @return update result
     */
    private Map<String, Object> forceStart(ProcessInstance processInstance, TaskGroupQueue taskGroupQueue) {
        Map<String, Object> result = new HashMap<>();
        if (taskGroupQueue.getStatus() != TaskGroupQueueStatus.WAIT_QUEUE) {
            putMsg(result, Status.TASK_GROUP_QUEUE_ALREADY_START);
            return result;
        }

        taskGroupQueue.setForceStart(Flag.YES.getCode());
        processService.updateTaskGroupQueue(taskGroupQueue);
        processService.sendStartTask2Master(processInstance, taskGroupQueue.getTaskId(),
                org.apache.dolphinscheduler.remote.command.CommandType.TASK_FORCE_STATE_EVENT_REQUEST);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * insert command, used in the implementation of the page, rerun, recovery (pause / failure) execution
     *
     * @param loginUser             login user
     * @param instanceId            instance id
     * @param processDefinitionCode process definition code
     * @param processVersion
     * @param commandType           command type
     * @return insert result code
     */
    private Map<String, Object> insertCommand(User loginUser, Integer instanceId, long processDefinitionCode,
                                              int processVersion, CommandType commandType, String startParams) {
        Map<String, Object> result = new HashMap<>();

        // To add startParams only when repeat running is needed
        Map<String, Object> cmdParam = new HashMap<>();
        cmdParam.put(CMD_PARAM_RECOVER_PROCESS_ID_STRING, instanceId);
        if (!StringUtils.isEmpty(startParams)) {
            cmdParam.put(CMD_PARAM_START_PARAMS, startParams);
        }

        Command command = new Command();
        command.setCommandType(commandType);
        command.setProcessDefinitionCode(processDefinitionCode);
        command.setCommandParam(JSONUtils.toJsonString(cmdParam));
        command.setExecutorId(loginUser.getId());
        command.setProcessDefinitionVersion(processVersion);
        command.setProcessInstanceId(instanceId);

        if (!processService.verifyIsNeedCreateCommand(command)) {
            putMsg(result, Status.PROCESS_INSTANCE_EXECUTING_COMMAND, String.valueOf(processDefinitionCode));
            return result;
        }

        int create = processService.createCommand(command);

        if (create > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.EXECUTE_PROCESS_INSTANCE_ERROR);
        }

        return result;
    }

    /**
     * check whether sub processes are offline before starting process definition
     *
     * @param processDefinitionCode process definition code
     * @return check result code
     */
    @Override
    public Map<String, Object> startCheckByProcessDefinedCode(long processDefinitionCode) {
        Map<String, Object> result = new HashMap<>();

        ProcessDefinition processDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);

        if (processDefinition == null) {
            logger.error("process definition is not found");
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, "processDefinitionCode");
            return result;
        }

        List<Long> codes = new ArrayList<>();
        processService.recurseFindSubProcess(processDefinition.getCode(), codes);
        if (!codes.isEmpty()) {
            List<ProcessDefinition> processDefinitionList = processDefinitionMapper.queryByCodes(codes);
            if (processDefinitionList != null) {
                for (ProcessDefinition processDefinitionTmp : processDefinitionList) {
                    /**
                     * if there is no online process, exit directly
                     */
                    if (processDefinitionTmp.getReleaseState() != ReleaseState.ONLINE) {
                        putMsg(result, Status.PROCESS_DEFINE_NOT_RELEASE, processDefinitionTmp.getName());
                        logger.info("not release process definition id: {} , name : {}", processDefinitionTmp.getId(),
                                processDefinitionTmp.getName());
                        return result;
                    }
                }
            }
        }
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * create command
     *
     * @param commandType             commandType
     * @param processDefineCode       processDefineCode
     * @param nodeDep                 nodeDep
     * @param failureStrategy         failureStrategy
     * @param startNodeList           startNodeList
     * @param schedule                schedule
     * @param warningType             warningType
     * @param executorId              executorId
     * @param warningGroupId          warningGroupId
     * @param runMode                 runMode
     * @param processInstancePriority processInstancePriority
     * @param workerGroup             workerGroup
     * @param environmentCode         environmentCode
     * @return command id
     */
    private int createCommand(CommandType commandType, long processDefineCode, TaskDependType nodeDep,
                              FailureStrategy failureStrategy, String startNodeList, String schedule,
                              WarningType warningType, int executorId, int warningGroupId, RunMode runMode,
                              Priority processInstancePriority, String workerGroup, Long environmentCode,
                              Map<String, String> startParams, Integer expectedParallelismNumber, int dryRun,
                              ComplementDependentMode complementDependentMode) {

        /**
         * instantiate command schedule instance
         */
        Command command = new Command();

        Map<String, String> cmdParam = new HashMap<>();
        if (commandType == null) {
            command.setCommandType(CommandType.START_PROCESS);
        } else {
            command.setCommandType(commandType);
        }
        command.setProcessDefinitionCode(processDefineCode);
        if (nodeDep != null) {
            command.setTaskDependType(nodeDep);
        }
        if (failureStrategy != null) {
            command.setFailureStrategy(failureStrategy);
        }

        if (!StringUtils.isEmpty(startNodeList)) {
            cmdParam.put(CMD_PARAM_START_NODES, startNodeList);
        }
        if (warningType != null) {
            command.setWarningType(warningType);
        }
        if (startParams != null && startParams.size() > 0) {
            cmdParam.put(CMD_PARAM_START_PARAMS, JSONUtils.toJsonString(startParams));
        }
        command.setCommandParam(JSONUtils.toJsonString(cmdParam));
        command.setExecutorId(executorId);
        command.setWarningGroupId(warningGroupId);
        command.setProcessInstancePriority(processInstancePriority);
        command.setWorkerGroup(workerGroup);
        command.setEnvironmentCode(environmentCode);
        command.setDryRun(dryRun);
        ProcessDefinition processDefinition = processService.findProcessDefinitionByCode(processDefineCode);
        if (processDefinition != null) {
            command.setProcessDefinitionVersion(processDefinition.getVersion());
        }
        command.setProcessInstanceId(0);

        // determine whether to complement
        if (commandType == CommandType.COMPLEMENT_DATA) {
            if (schedule == null || StringUtils.isEmpty(schedule)) {
                return 0;
            }
            if (!isValidateScheduleTime(schedule)) {
                return 0;
            }
            try {
                return createComplementCommandList(schedule, runMode, command, expectedParallelismNumber,
                        complementDependentMode);
            } catch (CronParseException cronParseException) {
                // We catch the exception here just to make compiler happy, since we have already validated the schedule
                // cron expression before
                return 0;
            }
        } else {
            command.setCommandParam(JSONUtils.toJsonString(cmdParam));
            return processService.createCommand(command);
        }
    }

    /**
     * create complement command
     * close left and close right
     *
     * @param scheduleTimeParam
     * @param runMode
     * @return
     */
    protected int createComplementCommandList(String scheduleTimeParam, RunMode runMode, Command command,
                                              Integer expectedParallelismNumber,
                                              ComplementDependentMode complementDependentMode) throws CronParseException {
        int createCount = 0;
        String startDate = null;
        String endDate = null;
        String dateList = null;
        int dependentProcessDefinitionCreateCount = 0;
        runMode = (runMode == null) ? RunMode.RUN_MODE_SERIAL : runMode;
        Map<String, String> cmdParam = JSONUtils.toMap(command.getCommandParam());
        Map<String, String> scheduleParam = JSONUtils.toMap(scheduleTimeParam);
        if (scheduleParam.containsKey(CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST)) {
            dateList = scheduleParam.get(CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST);
            dateList = removeDuplicates(dateList);
        }
        if (scheduleParam.containsKey(CMDPARAM_COMPLEMENT_DATA_START_DATE) && scheduleParam.containsKey(
                CMDPARAM_COMPLEMENT_DATA_END_DATE)) {
            startDate = scheduleParam.get(CMDPARAM_COMPLEMENT_DATA_START_DATE);
            endDate = scheduleParam.get(CMDPARAM_COMPLEMENT_DATA_END_DATE);
        }
        switch (runMode) {
            case RUN_MODE_SERIAL: {
                if (StringUtils.isNotEmpty(dateList)) {
                    cmdParam.put(CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST, dateList);
                    command.setCommandParam(JSONUtils.toJsonString(cmdParam));
                    createCount = processService.createCommand(command);
                }
                if (startDate != null && endDate != null) {
                    cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE, startDate);
                    cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE, endDate);
                    command.setCommandParam(JSONUtils.toJsonString(cmdParam));
                    createCount = processService.createCommand(command);

                    // dependent process definition
                    List<Schedule> schedules = processService.queryReleaseSchedulerListByProcessDefinitionCode(
                            command.getProcessDefinitionCode());

                    if (schedules.isEmpty() || complementDependentMode == ComplementDependentMode.OFF_MODE) {
                        logger.info("process code: {} complement dependent in off mode or schedule's size is 0, skip "
                                + "dependent complement data", command.getProcessDefinitionCode());
                    } else {
                        dependentProcessDefinitionCreateCount += createComplementDependentCommand(schedules, command);
                    }
                }
                break;
            }
            case RUN_MODE_PARALLEL: {
                if (startDate != null && endDate != null) {
                    List<Schedule> schedules = processService.queryReleaseSchedulerListByProcessDefinitionCode(
                            command.getProcessDefinitionCode());
                    List<ZonedDateTime> listDate = new ArrayList<>(
                            CronUtils.getSelfFireDateList(DateUtils.stringToZoneDateTime(startDate),
                                    DateUtils.stringToZoneDateTime(endDate), schedules));
                    int listDateSize = listDate.size();
                    createCount = listDate.size();
                    if (!CollectionUtils.isEmpty(listDate)) {
                        if (expectedParallelismNumber != null && expectedParallelismNumber != 0) {
                            createCount = Math.min(createCount, expectedParallelismNumber);
                        }
                        logger.info("In parallel mode, current expectedParallelismNumber:{}", createCount);

                        // Distribute the number of tasks equally to each command.
                        // The last command with insufficient quantity will be assigned to the remaining tasks.
                        int itemsPerCommand = (listDateSize / createCount);
                        int remainingItems = (listDateSize % createCount);
                        int startDateIndex = 0;
                        int endDateIndex = 0;

                        for (int i = 1; i <= createCount; i++) {
                            int extra = (i <= remainingItems) ? 1 : 0;
                            int singleCommandItems = (itemsPerCommand + extra);

                            if (i == 1) {
                                endDateIndex += singleCommandItems - 1;
                            } else {
                                startDateIndex = endDateIndex + 1;
                                endDateIndex += singleCommandItems;
                            }

                            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE,
                                    DateUtils.dateToString(listDate.get(startDateIndex)));
                            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE,
                                    DateUtils.dateToString(listDate.get(endDateIndex)));
                            command.setCommandParam(JSONUtils.toJsonString(cmdParam));
                            processService.createCommand(command);

                            if (schedules.isEmpty() || complementDependentMode == ComplementDependentMode.OFF_MODE) {
                                logger.info(
                                        "process code: {} complement dependent in off mode or schedule's size is 0, skip "
                                                + "dependent complement data",
                                        command.getProcessDefinitionCode());
                            } else {
                                dependentProcessDefinitionCreateCount +=
                                        createComplementDependentCommand(schedules, command);
                            }
                        }
                    }
                }
                if (StringUtils.isNotEmpty(dateList)) {
                    List<String> listDate = Arrays.asList(dateList.split(COMMA));
                    createCount = listDate.size();
                    if (!CollectionUtils.isEmpty(listDate)) {
                        if (expectedParallelismNumber != null && expectedParallelismNumber != 0) {
                            createCount = Math.min(createCount, expectedParallelismNumber);
                        }
                        logger.info("In parallel mode, current expectedParallelismNumber:{}", createCount);
                        for (List<String> stringDate : Lists.partition(listDate, createCount)) {
                            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST, String.join(COMMA, stringDate));
                            command.setCommandParam(JSONUtils.toJsonString(cmdParam));
                            processService.createCommand(command);
                        }
                    }
                }
                break;
            }
            default:
                break;
        }
        logger.info("create complement command count: {}, create dependent complement command count: {}", createCount,
                dependentProcessDefinitionCreateCount);
        return createCount;
    }

    /**
     * create complement dependent command
     */
    protected int createComplementDependentCommand(List<Schedule> schedules, Command command) {
        int dependentProcessDefinitionCreateCount = 0;
        Command dependentCommand;

        try {
            dependentCommand = (Command) BeanUtils.cloneBean(command);
        } catch (Exception e) {
            logger.error("copy dependent command error: ", e);
            return dependentProcessDefinitionCreateCount;
        }

        List<DependentProcessDefinition> dependentProcessDefinitionList =
                getComplementDependentDefinitionList(dependentCommand.getProcessDefinitionCode(),
                        CronUtils.getMaxCycle(schedules.get(0).getCrontab()), dependentCommand.getWorkerGroup());

        dependentCommand.setTaskDependType(TaskDependType.TASK_POST);
        for (DependentProcessDefinition dependentProcessDefinition : dependentProcessDefinitionList) {
            dependentCommand.setProcessDefinitionCode(dependentProcessDefinition.getProcessDefinitionCode());
            dependentCommand.setWorkerGroup(dependentProcessDefinition.getWorkerGroup());
            Map<String, String> cmdParam = JSONUtils.toMap(dependentCommand.getCommandParam());
            cmdParam.put(CMD_PARAM_START_NODES, String.valueOf(dependentProcessDefinition.getTaskDefinitionCode()));
            dependentCommand.setCommandParam(JSONUtils.toJsonString(cmdParam));
            dependentProcessDefinitionCreateCount += processService.createCommand(dependentCommand);
        }

        return dependentProcessDefinitionCreateCount;
    }

    /**
     * get complement dependent process definition list
     */
    private List<DependentProcessDefinition> getComplementDependentDefinitionList(long processDefinitionCode,
                                                                                  CycleEnum processDefinitionCycle,
                                                                                  String workerGroup) {
        List<DependentProcessDefinition> dependentProcessDefinitionList =
                processService.queryDependentProcessDefinitionByProcessDefinitionCode(processDefinitionCode);

        return checkDependentProcessDefinitionValid(dependentProcessDefinitionList, processDefinitionCycle,
                workerGroup);
    }

    /**
     * Check whether the dependency cycle of the dependent node is consistent with the schedule cycle of
     * the dependent process definition and if there is no worker group in the schedule, use the complement selection's
     * worker group
     */
    private List<DependentProcessDefinition> checkDependentProcessDefinitionValid(
                                                                                  List<DependentProcessDefinition> dependentProcessDefinitionList,
                                                                                  CycleEnum processDefinitionCycle,
                                                                                  String workerGroup) {
        List<DependentProcessDefinition> validDependentProcessDefinitionList = new ArrayList<>();

        List<Long> processDefinitionCodeList =
                dependentProcessDefinitionList.stream().map(DependentProcessDefinition::getProcessDefinitionCode)
                        .collect(Collectors.toList());

        Map<Long, String> processDefinitionWorkerGroupMap =
                processService.queryWorkerGroupByProcessDefinitionCodes(processDefinitionCodeList);

        for (DependentProcessDefinition dependentProcessDefinition : dependentProcessDefinitionList) {
            if (dependentProcessDefinition.getDependentCycle() == processDefinitionCycle) {
                if (processDefinitionWorkerGroupMap
                        .get(dependentProcessDefinition.getProcessDefinitionCode()) == null) {
                    dependentProcessDefinition.setWorkerGroup(workerGroup);
                }

                validDependentProcessDefinitionList.add(dependentProcessDefinition);
            }
        }

        return validDependentProcessDefinitionList;
    }

    /**
     * @param schedule
     * @return check error return 0, otherwise 1
     */
    private boolean isValidateScheduleTime(String schedule) {
        Map<String, String> scheduleResult = JSONUtils.toMap(schedule);
        if (scheduleResult == null) {
            return false;
        }
        if (scheduleResult.containsKey(CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST)) {
            if (scheduleResult.get(CMDPARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST) == null) {
                return false;
            }
        }
        if (scheduleResult.containsKey(CMDPARAM_COMPLEMENT_DATA_START_DATE)) {
            String startDate = scheduleResult.get(CMDPARAM_COMPLEMENT_DATA_START_DATE);
            String endDate = scheduleResult.get(CMDPARAM_COMPLEMENT_DATA_END_DATE);
            if (startDate == null || endDate == null) {
                return false;
            }
            try {
                ZonedDateTime start = DateUtils.stringToZoneDateTime(startDate);
                ZonedDateTime end = DateUtils.stringToZoneDateTime(endDate);
                if (start == null || end == null) {
                    return false;
                }
                if (start.isAfter(end)) {
                    logger.error("complement data error, wrong date start:{} and end date:{} ", start, end);
                    return false;
                }
            } catch (Exception ex) {
                logger.warn("Parse schedule time error, startDate: {}, endDate: {}", startDate, endDate);
                return false;
            }
        }
        return true;
    }

    /**
     * @param scheduleTimeList
     * @return remove duplicate date list
     */
    private String removeDuplicates(String scheduleTimeList) {
        if (StringUtils.isNotEmpty(scheduleTimeList)) {
            Set<String> dateSet =
                    Arrays.stream(scheduleTimeList.split(COMMA)).map(String::trim).collect(Collectors.toSet());
            return String.join(COMMA, dateSet);
        }
        return null;
    }

    /**
     * query executing data of processInstance by master
     * @param processInstanceId
     * @return
     */
    @Override
    public WorkflowExecuteDto queryExecutingWorkflowByProcessInstanceId(Integer processInstanceId) {
        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processInstanceId);
        if (processInstance == null) {
            return null;
        }
        Host host = new Host(processInstance.getHost());
        WorkflowExecutingDataRequestCommand requestCommand = new WorkflowExecutingDataRequestCommand();
        requestCommand.setProcessInstanceId(processInstanceId);
        org.apache.dolphinscheduler.remote.command.Command command =
                stateEventCallbackService.sendSync(host, requestCommand.convert2Command());
        if (command == null) {
            return null;
        }
        WorkflowExecutingDataResponseCommand responseCommand =
                JSONUtils.parseObject(command.getBody(), WorkflowExecutingDataResponseCommand.class);
        return responseCommand.getWorkflowExecuteDto();
    }

    @Override
    public Map<String, Object> execStreamTaskInstance(User loginUser, long projectCode, long taskDefinitionCode, int taskDefinitionVersion,
                                                int warningGroupId, String workerGroup, Long environmentCode, Map<String, String> startParams, int dryRun) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result =
            projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_START);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        // check master exists
        if (!checkMasterExists(result)) {
            return result;
        }

        // todo dispatch improvement
        List<Server> masterServerList = monitorService.getServerListFromRegistry(true);
        Host host = new Host(masterServerList.get(0).getHost(), masterServerList.get(0).getPort());

        TaskExecuteStartCommand taskExecuteStartCommand = new TaskExecuteStartCommand();
        taskExecuteStartCommand.setExecutorId(loginUser.getId());
        taskExecuteStartCommand.setExecutorName(loginUser.getUserName());
        taskExecuteStartCommand.setProjectCode(projectCode);
        taskExecuteStartCommand.setTaskDefinitionCode(taskDefinitionCode);
        taskExecuteStartCommand.setTaskDefinitionVersion(taskDefinitionVersion);
        taskExecuteStartCommand.setWorkerGroup(workerGroup);
        taskExecuteStartCommand.setWarningGroupId(warningGroupId);
        taskExecuteStartCommand.setEnvironmentCode(environmentCode);
        taskExecuteStartCommand.setStartParams(startParams);
        taskExecuteStartCommand.setDryRun(dryRun);

        org.apache.dolphinscheduler.remote.command.Command response = stateEventCallbackService.sendSync(host, taskExecuteStartCommand.convert2Command());
        if (response != null) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.START_TASK_INSTANCE_ERROR);
        }
        return result;
    }
}
