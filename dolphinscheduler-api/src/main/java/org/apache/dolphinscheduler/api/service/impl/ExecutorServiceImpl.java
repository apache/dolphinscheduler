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
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_START_NODES;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_START_PARAMS;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_SUB_PROCESS_DEFINE_CODE;
import static org.apache.dolphinscheduler.common.constants.Constants.COMMA;
import static org.apache.dolphinscheduler.common.constants.Constants.MAX_TASK_TIMEOUT;
import static org.apache.dolphinscheduler.common.constants.Constants.SCHEDULE_TIME_MAX_LENGTH;

import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowExecuteResponse;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.MonitorService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.WorkerGroupService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ApiTriggerType;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.CycleEnum;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
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
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.remote.command.TaskExecuteStartCommand;
import org.apache.dolphinscheduler.remote.command.WorkflowExecutingDataRequestCommand;
import org.apache.dolphinscheduler.remote.command.WorkflowExecutingDataResponseCommand;
import org.apache.dolphinscheduler.remote.command.WorkflowStateEventChangeCommand;
import org.apache.dolphinscheduler.remote.dto.WorkflowExecuteDto;
import org.apache.dolphinscheduler.remote.processor.StateEventCallbackService;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.command.CommandService;
import org.apache.dolphinscheduler.service.cron.CronUtils;
import org.apache.dolphinscheduler.service.exceptions.CronParseException;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.process.TriggerRelationService;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;

/**
 * executor service impl
 */
@Service
@Slf4j
public class ExecutorServiceImpl extends BaseServiceImpl implements ExecutorService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    ProcessDefinitionMapper processDefineMapper;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Lazy()
    @Autowired
    private ProcessService processService;

    @Autowired
    private CommandService commandService;

    @Autowired
    private ProcessInstanceDao processInstanceDao;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private StateEventCallbackService stateEventCallbackService;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Autowired
    private TaskGroupQueueMapper taskGroupQueueMapper;

    @Autowired
    private WorkerGroupService workerGroupService;

    @Autowired
    private TriggerRelationService triggerRelationService;
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
     * @param testFlag testFlag
     * @return execute process instance code
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> execProcessInstance(User loginUser, long projectCode, long processDefinitionCode,
                                                   String cronTime, CommandType commandType,
                                                   FailureStrategy failureStrategy, String startNodeList,
                                                   TaskDependType taskDependType, WarningType warningType,
                                                   Integer warningGroupId, RunMode runMode,
                                                   Priority processInstancePriority, String workerGroup,
                                                   Long environmentCode, Integer timeout,
                                                   Map<String, String> startParams, Integer expectedParallelismNumber,
                                                   int dryRun, int testFlag,
                                                   ComplementDependentMode complementDependentMode, Integer version) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        Map<String, Object> result =
                projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_START);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        // timeout is invalid
        if (timeout <= 0 || timeout > MAX_TASK_TIMEOUT) {
            log.warn("Parameter timeout is invalid, timeout:{}.", timeout);
            putMsg(result, Status.TASK_TIMEOUT_PARAMS_ERROR);
            return result;
        }
        ProcessDefinition processDefinition;
        if (null != version) {
            processDefinition = processService.findProcessDefinition(processDefinitionCode, version);
        } else {
            processDefinition = processDefinitionMapper.queryByCode(processDefinitionCode);
        }
        // check process define release state
        this.checkProcessDefinitionValid(projectCode, processDefinition, processDefinitionCode,
                processDefinition.getVersion());
        // check current version whether include startNodeList
        checkStartNodeList(startNodeList, processDefinitionCode, processDefinition.getVersion());
        if (!checkTenantSuitable(processDefinition)) {
            log.error(
                    "There is not any valid tenant for the process definition, processDefinitionCode:{}, processDefinitionName:{}.",
                    processDefinition.getCode(), processDefinition.getName());
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

        long triggerCode = CodeGenerateUtils.getInstance().genCode();

        /**
         * create command
         */
        int create =
                this.createCommand(triggerCode, commandType, processDefinition.getCode(), taskDependType,
                        failureStrategy,
                        startNodeList,
                        cronTime, warningType, loginUser.getId(), warningGroupId, runMode, processInstancePriority,
                        workerGroup,
                        environmentCode, startParams, expectedParallelismNumber, dryRun, testFlag,
                        complementDependentMode);

        if (create > 0) {
            processDefinition.setWarningGroupId(warningGroupId);
            processDefinitionMapper.updateById(processDefinition);
            log.info("Create command complete, processDefinitionCode:{}, commandCount:{}.",
                    processDefinition.getCode(), create);
            result.put(Constants.DATA_LIST, triggerCode);
            putMsg(result, Status.SUCCESS);
        } else {
            log.error("Start process instance failed because create command error, processDefinitionCode:{}.",
                    processDefinition.getCode());
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
            log.error("Master does not exist.");
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
        if (cronMap.containsKey(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST)) {
            String[] stringDates = cronMap.get(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST).split(COMMA);
            if (stringDates.length > SCHEDULE_TIME_MAX_LENGTH) {
                log.warn("Parameter cornTime is bigger than {}.", SCHEDULE_TIME_MAX_LENGTH);
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
     * @param version           process instance version
     */
    @Override
    public void checkProcessDefinitionValid(long projectCode, ProcessDefinition processDefinition,
                                            long processDefineCode, Integer version) {
        // check process definition exists
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            throw new ServiceException(Status.PROCESS_DEFINE_NOT_EXIST, String.valueOf(processDefineCode));
        }
        // check process definition online
        if (processDefinition.getReleaseState() != ReleaseState.ONLINE) {
            throw new ServiceException(Status.PROCESS_DEFINE_NOT_RELEASE, String.valueOf(processDefineCode), version);
        }
        // check sub process definition online
        if (!checkSubProcessDefinitionValid(processDefinition)) {
            throw new ServiceException(Status.SUB_PROCESS_DEFINE_NOT_RELEASE);
        }
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
                                        CMD_PARAM_SUB_PROCESS_DEFINE_CODE))));
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
     * do action to process instance：pause, stop, repeat, recover from pause, recover from stop，rerun failed task
    
    
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

        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processInstanceId)
                .orElseThrow(() -> new ServiceException(Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId));

        ProcessDefinition processDefinition =
                processService.findProcessDefinition(processInstance.getProcessDefinitionCode(),
                        processInstance.getProcessDefinitionVersion());
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        if (executeType != ExecuteType.STOP && executeType != ExecuteType.PAUSE) {
            this.checkProcessDefinitionValid(projectCode, processDefinition, processInstance.getProcessDefinitionCode(),
                    processInstance.getProcessDefinitionVersion());
        }

        result = checkExecuteType(processInstance, executeType);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }
        if (!checkTenantSuitable(processDefinition)) {
            log.error(
                    "There is not any valid tenant for the process definition, processDefinitionId:{}, processDefinitionCode:{}, ",
                    processDefinition.getId(), processDefinition.getName());
            putMsg(result, Status.TENANT_NOT_SUITABLE);
        }

        // get the startParams user specified at the first starting while repeat running is needed
        Map<String, Object> commandMap =
                JSONUtils.parseObject(processInstance.getCommandParam(), new TypeReference<Map<String, Object>>() {
                });
        String startParams = null;
        if (MapUtils.isNotEmpty(commandMap) && executeType == ExecuteType.REPEAT_RUNNING) {
            Object startParamsJson = commandMap.get(CMD_PARAM_START_PARAMS);
            if (startParamsJson != null) {
                startParams = startParamsJson.toString();
            }
        }

        switch (executeType) {
            case REPEAT_RUNNING:
                result = insertCommand(loginUser, processInstanceId, processDefinition.getCode(),
                        processDefinition.getVersion(), CommandType.REPEAT_RUNNING, startParams,
                        processInstance.getTestFlag());
                break;
            case RECOVER_SUSPENDED_PROCESS:
                result = insertCommand(loginUser, processInstanceId, processDefinition.getCode(),
                        processDefinition.getVersion(), CommandType.RECOVER_SUSPENDED_PROCESS, startParams,
                        processInstance.getTestFlag());
                break;
            case START_FAILURE_TASK_PROCESS:
                result = insertCommand(loginUser, processInstanceId, processDefinition.getCode(),
                        processDefinition.getVersion(), CommandType.START_FAILURE_TASK_PROCESS, startParams,
                        processInstance.getTestFlag());
                break;
            case STOP:
                if (processInstance.getState() == WorkflowExecutionStatus.READY_STOP) {
                    log.warn("Process instance status is already {}, processInstanceName:{}.",
                            WorkflowExecutionStatus.READY_STOP.getDesc(), processInstance.getName());
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
                    log.warn("Process instance status is already {}, processInstanceName:{}.",
                            WorkflowExecutionStatus.READY_STOP.getDesc(), processInstance.getName());
                    putMsg(result, Status.PROCESS_INSTANCE_ALREADY_CHANGED, processInstance.getName(),
                            processInstance.getState());
                } else {
                    result = updateProcessInstancePrepare(processInstance, CommandType.PAUSE,
                            WorkflowExecutionStatus.READY_PAUSE);
                }
                break;
            default:
                log.warn("Unknown execute type for process instance, processInstanceId:{}.",
                        processInstance.getId());
                putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, "unknown execute type");

                break;
        }
        return result;
    }

    /**
     * do action to workflow instance：pause, stop, repeat, recover from pause, recover from stop，rerun failed task
    
    
     *
     * @param loginUser         login user
     * @param workflowInstanceId workflow instance id
     * @param executeType       execute type
     * @return execute result code
     */
    @Override
    public Map<String, Object> execute(User loginUser, Integer workflowInstanceId, ExecuteType executeType) {
        ProcessInstance processInstance = processInstanceMapper.selectById(workflowInstanceId);
        ProcessDefinition processDefinition =
                processDefineMapper.queryByCode(processInstance.getProcessDefinitionCode());

        return execute(loginUser, processDefinition.getProjectCode(), workflowInstanceId, executeType);
    }

    /**
     * do action to execute task in process instance
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processInstanceId process instance id
     * @param startNodeList start node list
     * @param taskDependType task depend type
     * @return execute result code
     */
    @Override
    public WorkflowExecuteResponse executeTask(User loginUser, long projectCode, Integer processInstanceId,
                                               String startNodeList, TaskDependType taskDependType) {

        WorkflowExecuteResponse response = new WorkflowExecuteResponse();

        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project

        projectService.checkProjectAndAuthThrowException(loginUser, project,
                ApiFuncIdentificationConstant.map.get(ExecuteType.EXECUTE_TASK));

        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processInstanceId)
                .orElseThrow(() -> new ServiceException(Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId));

        if (!processInstance.getState().isFinished()) {
            log.error("Can not execute task for process instance which is not finished, processInstanceId:{}.",
                    processInstanceId);
            putMsg(response, Status.WORKFLOW_INSTANCE_IS_NOT_FINISHED);
            return response;
        }

        ProcessDefinition processDefinition =
                processService.findProcessDefinition(processInstance.getProcessDefinitionCode(),
                        processInstance.getProcessDefinitionVersion());
        processDefinition.setReleaseState(ReleaseState.ONLINE);
        this.checkProcessDefinitionValid(projectCode, processDefinition, processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion());

        if (!checkTenantSuitable(processDefinition)) {
            log.error(
                    "There is not any valid tenant for the process definition, processDefinitionId:{}, processDefinitionCode:{}, ",
                    processDefinition.getId(), processDefinition.getName());
            putMsg(response, Status.TENANT_NOT_SUITABLE);
            return response;
        }

        // get the startParams user specified at the first starting while repeat running is needed

        long startNodeListLong;
        try {
            startNodeListLong = Long.parseLong(startNodeList);
        } catch (NumberFormatException e) {
            log.error("startNodeList is not a number");
            putMsg(response, Status.REQUEST_PARAMS_NOT_VALID_ERROR, startNodeList);
            return response;
        }

        if (taskDefinitionLogMapper.queryMaxVersionForDefinition(startNodeListLong) == null) {
            putMsg(response, Status.EXECUTE_NOT_DEFINE_TASK);
            return response;
        }

        // To add startParams only when repeat running is needed
        Map<String, Object> cmdParam = new HashMap<>();
        cmdParam.put(CMD_PARAM_RECOVER_PROCESS_ID_STRING, processInstanceId);
        // Add StartNodeList
        cmdParam.put(CMD_PARAM_START_NODES, startNodeList);

        Command command = new Command();
        command.setCommandType(CommandType.EXECUTE_TASK);
        command.setProcessDefinitionCode(processDefinition.getCode());
        command.setCommandParam(JSONUtils.toJsonString(cmdParam));
        command.setExecutorId(loginUser.getId());
        command.setProcessDefinitionVersion(processDefinition.getVersion());
        command.setProcessInstanceId(processInstanceId);
        command.setTestFlag(processInstance.getTestFlag());

        // Add taskDependType
        command.setTaskDependType(taskDependType);

        if (!commandService.verifyIsNeedCreateCommand(command)) {
            log.warn(
                    "Process instance is executing the command, processDefinitionCode:{}, processDefinitionVersion:{}, processInstanceId:{}.",
                    processDefinition.getCode(), processDefinition.getVersion(), processInstanceId);
            putMsg(response, Status.PROCESS_INSTANCE_EXECUTING_COMMAND,
                    String.valueOf(processDefinition.getCode()));
            return response;
        }

        log.info("Creating command, commandInfo:{}.", command);
        int create = commandService.createCommand(command);

        if (create > 0) {
            log.info("Create {} command complete, processDefinitionCode:{}, processDefinitionVersion:{}.",
                    command.getCommandType().getDescp(), command.getProcessDefinitionCode(),
                    processDefinition.getVersion());
            putMsg(response, Status.SUCCESS);
        } else {
            log.error(
                    "Execute process instance failed because create {} command error, processDefinitionCode:{}, processDefinitionVersion:{}， processInstanceId:{}.",
                    command.getCommandType().getDescp(), command.getProcessDefinitionCode(),
                    processDefinition.getVersion(),
                    processInstanceId);
            putMsg(response, Status.EXECUTE_PROCESS_INSTANCE_ERROR);
        }

        return response;
    }

    @Override
    public Map<String, Object> forceStartTaskInstance(User loginUser, int queueId) {
        Map<String, Object> result = new HashMap<>();
        TaskGroupQueue taskGroupQueue = taskGroupQueueMapper.selectById(queueId);
        // check process instance exist
        ProcessInstance processInstance = processInstanceMapper.selectById(taskGroupQueue.getProcessId());
        if (processInstance == null) {
            log.error("Process instance does not exist, projectCode:{}, processInstanceId:{}.",
                    taskGroupQueue.getProjectCode(), taskGroupQueue.getProcessId());
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

    public void checkStartNodeList(String startNodeList, Long processDefinitionCode, int version) {
        if (StringUtils.isNotEmpty(startNodeList)) {
            List<ProcessTaskRelation> processTaskRelations =
                    processService.findRelationByCode(processDefinitionCode, version);
            List<Long> existsNodes = processTaskRelations.stream().map(ProcessTaskRelation::getPostTaskCode)
                    .collect(Collectors.toList());
            for (String startNode : startNodeList.split(Constants.COMMA)) {
                if (!existsNodes.contains(Long.valueOf(startNode))) {
                    throw new ServiceException(Status.START_NODE_NOT_EXIST_IN_LAST_PROCESS, startNode);
                }
            }
        }
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
                if (executionStatus.isRunning()) {
                    checkResult = true;
                }
                break;
            case STOP:
                if (executionStatus.canStop()) {
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
        processInstance.setStateWithDesc(executionStatus, commandType.getDescp() + "by ui");
        int update = processInstanceDao.updateProcessInstance(processInstance);

        // determine whether the process is normal
        if (update > 0) {
            log.info("Process instance state is updated to {} in database, processInstanceName:{}.",
                    executionStatus.getDesc(), processInstance.getName());
            // directly send the process instance state change event to target master, not guarantee the event send
            // success
            WorkflowStateEventChangeCommand workflowStateEventChangeCommand = new WorkflowStateEventChangeCommand(
                    processInstance.getId(), 0, processInstance.getState(), processInstance.getId(), 0);
            Host host = new Host(processInstance.getHost());
            stateEventCallbackService.sendResult(host, workflowStateEventChangeCommand.convert2Command());
            putMsg(result, Status.SUCCESS);
        } else {
            log.error("Process instance state update error, processInstanceName:{}.", processInstance.getName());
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
            log.warn("Task group queue already starts, taskGroupQueueId:{}.", taskGroupQueue.getId());
            putMsg(result, Status.TASK_GROUP_QUEUE_ALREADY_START);
            return result;
        }

        taskGroupQueue.setForceStart(Flag.YES.getCode());
        processService.updateTaskGroupQueue(taskGroupQueue);
        log.info("Sending force start command to master.");
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
                                              int processVersion, CommandType commandType, String startParams,
                                              int testFlag) {
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
        command.setTestFlag(testFlag);
        if (!commandService.verifyIsNeedCreateCommand(command)) {
            log.warn(
                    "Process instance is executing the command, processDefinitionCode:{}, processDefinitionVersion:{}, processInstanceId:{}.",
                    processDefinitionCode, processVersion, instanceId);
            putMsg(result, Status.PROCESS_INSTANCE_EXECUTING_COMMAND, String.valueOf(processDefinitionCode));
            return result;
        }

        log.info("Creating command, commandInfo:{}.", command);
        int create = commandService.createCommand(command);

        if (create > 0) {
            log.info("Create {} command complete, processDefinitionCode:{}, processDefinitionVersion:{}.",
                    command.getCommandType().getDescp(), command.getProcessDefinitionCode(), processVersion);
            putMsg(result, Status.SUCCESS);
        } else {
            log.error(
                    "Execute process instance failed because create {} command error, processDefinitionCode:{}, processDefinitionVersion:{}， processInstanceId:{}.",
                    command.getCommandType().getDescp(), command.getProcessDefinitionCode(), processVersion,
                    instanceId);
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
            log.error("Process definition is not be found, processDefinitionCode:{}.", processDefinitionCode);
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
                        log.warn("Subprocess definition {} of process definition {} is not {}.",
                                processDefinitionTmp.getName(),
                                processDefinition.getName(), ReleaseState.ONLINE.getDescp());
                        putMsg(result, Status.PROCESS_DEFINE_NOT_RELEASE, processDefinitionTmp.getName());
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
     * @param testFlag                testFlag
     * @param environmentCode         environmentCode
     * @return command id
     */
    private int createCommand(Long triggerCode, CommandType commandType, long processDefineCode, TaskDependType nodeDep,
                              FailureStrategy failureStrategy, String startNodeList, String schedule,
                              WarningType warningType, int executorId, Integer warningGroupId, RunMode runMode,
                              Priority processInstancePriority, String workerGroup, Long environmentCode,
                              Map<String, String> startParams, Integer expectedParallelismNumber, int dryRun,
                              int testFlag, ComplementDependentMode complementDependentMode) {

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
        command.setTestFlag(testFlag);
        ProcessDefinition processDefinition = processService.findProcessDefinitionByCode(processDefineCode);
        if (processDefinition != null) {
            command.setProcessDefinitionVersion(processDefinition.getVersion());
        }
        command.setProcessInstanceId(0);

        // determine whether to complement
        if (commandType == CommandType.COMPLEMENT_DATA) {
            if (schedule == null || StringUtils.isEmpty(schedule)) {
                log.error("Create {} type command error because parameter schedule is invalid.",
                        command.getCommandType().getDescp());
                return 0;
            }
            if (!isValidateScheduleTime(schedule)) {
                return 0;
            }
            try {
                log.info("Start to create {} command, processDefinitionCode:{}.",
                        command.getCommandType().getDescp(), processDefineCode);
                return createComplementCommandList(triggerCode, schedule, runMode, command, expectedParallelismNumber,
                        complementDependentMode);
            } catch (CronParseException cronParseException) {
                // We catch the exception here just to make compiler happy, since we have already validated the schedule
                // cron expression before
                return 0;
            }
        } else {
            command.setCommandParam(JSONUtils.toJsonString(cmdParam));
            int count = commandService.createCommand(command);
            if (count > 0) {
                triggerRelationService.saveTriggerToDb(ApiTriggerType.COMMAND, triggerCode, command.getId());
            }
            return count;
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
    protected int createComplementCommandList(Long triggerCode, String scheduleTimeParam, RunMode runMode,
                                              Command command,
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
        if (scheduleParam.containsKey(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST)) {
            dateList = scheduleParam.get(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST);
            dateList = removeDuplicates(dateList);
        }
        if (scheduleParam.containsKey(CMD_PARAM_COMPLEMENT_DATA_START_DATE) && scheduleParam.containsKey(
                CMD_PARAM_COMPLEMENT_DATA_END_DATE)) {
            startDate = scheduleParam.get(CMD_PARAM_COMPLEMENT_DATA_START_DATE);
            endDate = scheduleParam.get(CMD_PARAM_COMPLEMENT_DATA_END_DATE);
        }
        switch (runMode) {
            case RUN_MODE_SERIAL: {
                log.info("RunMode of {} command is serial run, processDefinitionCode:{}.",
                        command.getCommandType().getDescp(), command.getProcessDefinitionCode());
                if (StringUtils.isNotEmpty(dateList)) {
                    cmdParam.put(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST, dateList);
                    command.setCommandParam(JSONUtils.toJsonString(cmdParam));
                    log.info("Creating command, commandInfo:{}.", command);
                    createCount = commandService.createCommand(command);
                    if (createCount > 0) {
                        log.info("Create {} command complete, processDefinitionCode:{}",
                                command.getCommandType().getDescp(), command.getProcessDefinitionCode());
                    } else {
                        log.error("Create {} command error, processDefinitionCode:{}",
                                command.getCommandType().getDescp(), command.getProcessDefinitionCode());
                    }
                }
                if (startDate != null && endDate != null) {
                    cmdParam.put(CMD_PARAM_COMPLEMENT_DATA_START_DATE, startDate);
                    cmdParam.put(CMD_PARAM_COMPLEMENT_DATA_END_DATE, endDate);
                    command.setCommandParam(JSONUtils.toJsonString(cmdParam));
                    log.info("Creating command, commandInfo:{}.", command);
                    createCount = commandService.createCommand(command);
                    if (createCount > 0) {
                        log.info("Create {} command complete, processDefinitionCode:{}",
                                command.getCommandType().getDescp(), command.getProcessDefinitionCode());
                    } else {
                        log.error("Create {} command error, processDefinitionCode:{}",
                                command.getCommandType().getDescp(), command.getProcessDefinitionCode());
                    }
                    // dependent process definition
                    List<Schedule> schedules = processService.queryReleaseSchedulerListByProcessDefinitionCode(
                            command.getProcessDefinitionCode());

                    if (schedules.isEmpty() || complementDependentMode == ComplementDependentMode.OFF_MODE) {
                        log.info(
                                "Complement dependent mode is off mode or Scheduler is empty, so skip create complement dependent command, processDefinitionCode:{}.",
                                command.getProcessDefinitionCode());
                    } else {
                        log.info(
                                "Complement dependent mode is all dependent and Scheduler is not empty, need create complement dependent command, processDefinitionCode:{}.",
                                command.getProcessDefinitionCode());
                        dependentProcessDefinitionCreateCount += createComplementDependentCommand(schedules, command);
                    }
                }
                if (createCount > 0) {
                    triggerRelationService.saveTriggerToDb(ApiTriggerType.COMMAND, triggerCode, command.getId());
                }
                break;
            }
            case RUN_MODE_PARALLEL: {
                log.info("RunMode of {} command is parallel run, processDefinitionCode:{}.",
                        command.getCommandType().getDescp(), command.getProcessDefinitionCode());
                if (startDate != null && endDate != null) {
                    List<Schedule> schedules = processService.queryReleaseSchedulerListByProcessDefinitionCode(
                            command.getProcessDefinitionCode());
                    List<ZonedDateTime> listDate = CronUtils.getSelfFireDateList(
                            DateUtils.stringToZoneDateTime(startDate),
                            DateUtils.stringToZoneDateTime(endDate),
                            schedules);
                    int listDateSize = listDate.size();
                    createCount = listDate.size();
                    if (!CollectionUtils.isEmpty(listDate)) {
                        if (expectedParallelismNumber != null && expectedParallelismNumber != 0) {
                            createCount = Math.min(createCount, expectedParallelismNumber);
                        }
                        log.info("Complement command run in parallel mode, current expectedParallelismNumber:{}.",
                                createCount);

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

                            cmdParam.put(CMD_PARAM_COMPLEMENT_DATA_START_DATE,
                                    DateUtils.dateToString(listDate.get(startDateIndex)));
                            cmdParam.put(CMD_PARAM_COMPLEMENT_DATA_END_DATE,
                                    DateUtils.dateToString(listDate.get(endDateIndex)));
                            command.setCommandParam(JSONUtils.toJsonString(cmdParam));
                            log.info("Creating command, commandInfo:{}.", command);
                            if (commandService.createCommand(command) > 0) {
                                log.info("Create {} command complete, processDefinitionCode:{}",
                                        command.getCommandType().getDescp(), command.getProcessDefinitionCode());
                                triggerRelationService.saveTriggerToDb(ApiTriggerType.COMMAND, triggerCode,
                                        command.getId());
                            } else {
                                log.error("Create {} command error, processDefinitionCode:{}",
                                        command.getCommandType().getDescp(), command.getProcessDefinitionCode());
                            }
                            if (schedules.isEmpty() || complementDependentMode == ComplementDependentMode.OFF_MODE) {
                                log.info(
                                        "Complement dependent mode is off mode or Scheduler is empty, so skip create complement dependent command, processDefinitionCode:{}.",
                                        command.getProcessDefinitionCode());
                            } else {
                                log.info(
                                        "Complement dependent mode is all dependent and Scheduler is not empty, need create complement dependent command, processDefinitionCode:{}.",
                                        command.getProcessDefinitionCode());
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
                        log.info("Complement command run in parallel mode, current expectedParallelismNumber:{}.",
                                createCount);
                        for (List<String> stringDate : Lists.partition(listDate, createCount)) {
                            cmdParam.put(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST, String.join(COMMA, stringDate));
                            command.setCommandParam(JSONUtils.toJsonString(cmdParam));
                            log.info("Creating command, commandInfo:{}.", command);
                            if (commandService.createCommand(command) > 0) {
                                log.info("Create {} command complete, processDefinitionCode:{}",
                                        command.getCommandType().getDescp(), command.getProcessDefinitionCode());
                            } else {
                                log.error("Create {} command error, processDefinitionCode:{}",
                                        command.getCommandType().getDescp(), command.getProcessDefinitionCode());
                            }
                        }
                    }
                }
                break;
            }
            default:
                break;
        }
        log.info("Create complement command count:{}, Create dependent complement command count:{}", createCount,
                dependentProcessDefinitionCreateCount);
        return createCount;
    }

    /**
     * create complement dependent command
     */
    public int createComplementDependentCommand(List<Schedule> schedules, Command command) {
        int dependentProcessDefinitionCreateCount = 0;
        Command dependentCommand;

        try {
            dependentCommand = (Command) BeanUtils.cloneBean(command);
        } catch (Exception e) {
            log.error("Copy dependent command error.", e);
            return dependentProcessDefinitionCreateCount;
        }

        List<DependentProcessDefinition> dependentProcessDefinitionList =
                getComplementDependentDefinitionList(dependentCommand.getProcessDefinitionCode(),
                        CronUtils.getMaxCycle(schedules.get(0).getCrontab()), dependentCommand.getWorkerGroup());
        dependentCommand.setTaskDependType(TaskDependType.TASK_POST);
        for (DependentProcessDefinition dependentProcessDefinition : dependentProcessDefinitionList) {
            // If the id is Integer, the auto-increment id will be obtained by mybatis-plus
            // and causing duplicate when clone it.
            dependentCommand.setId(null);
            dependentCommand.setProcessDefinitionCode(dependentProcessDefinition.getProcessDefinitionCode());
            dependentCommand.setProcessDefinitionVersion(dependentProcessDefinition.getProcessDefinitionVersion());
            dependentCommand.setWorkerGroup(dependentProcessDefinition.getWorkerGroup());
            Map<String, String> cmdParam = JSONUtils.toMap(dependentCommand.getCommandParam());
            cmdParam.put(CMD_PARAM_START_NODES, String.valueOf(dependentProcessDefinition.getTaskDefinitionCode()));
            dependentCommand.setCommandParam(JSONUtils.toJsonString(cmdParam));
            log.info("Creating complement dependent command, commandInfo:{}.", command);
            dependentProcessDefinitionCreateCount += commandService.createCommand(dependentCommand);
        }

        return dependentProcessDefinitionCreateCount;
    }

    /**
     * get complement dependent online process definition list
     */
    private List<DependentProcessDefinition> getComplementDependentDefinitionList(long processDefinitionCode,
                                                                                  CycleEnum processDefinitionCycle,
                                                                                  String workerGroup) {
        List<DependentProcessDefinition> dependentProcessDefinitionList =
                processService.queryDependentProcessDefinitionByProcessDefinitionCode(processDefinitionCode);

        return checkDependentProcessDefinitionValid(dependentProcessDefinitionList, processDefinitionCycle,
                workerGroup, processDefinitionCode);
    }

    /**
     * Check whether the dependency cycle of the dependent node is consistent with the schedule cycle of
     * the dependent process definition and if there is no worker group in the schedule, use the complement selection's
     * worker group
     */
    private List<DependentProcessDefinition> checkDependentProcessDefinitionValid(
                                                                                  List<DependentProcessDefinition> dependentProcessDefinitionList,
                                                                                  CycleEnum processDefinitionCycle,
                                                                                  String workerGroup,
                                                                                  long upstreamProcessDefinitionCode) {
        List<DependentProcessDefinition> validDependentProcessDefinitionList = new ArrayList<>();

        List<Long> processDefinitionCodeList =
                dependentProcessDefinitionList.stream().map(DependentProcessDefinition::getProcessDefinitionCode)
                        .collect(Collectors.toList());

        Map<Long, String> processDefinitionWorkerGroupMap =
                workerGroupService.queryWorkerGroupByProcessDefinitionCodes(processDefinitionCodeList);

        for (DependentProcessDefinition dependentProcessDefinition : dependentProcessDefinitionList) {
            if (dependentProcessDefinition.getDependentCycle(upstreamProcessDefinitionCode) == processDefinitionCycle) {
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
        if (scheduleResult.containsKey(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST)) {
            if (scheduleResult.get(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST) == null) {
                return false;
            }
        }
        if (scheduleResult.containsKey(CMD_PARAM_COMPLEMENT_DATA_START_DATE)) {
            String startDate = scheduleResult.get(CMD_PARAM_COMPLEMENT_DATA_START_DATE);
            String endDate = scheduleResult.get(CMD_PARAM_COMPLEMENT_DATA_END_DATE);
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
                    log.error(
                            "Complement data parameter error, start time should be before end time, startDate:{}, endDate:{}.",
                            start, end);
                    return false;
                }
            } catch (Exception ex) {
                log.warn("Parse schedule time error, startDate:{}, endDate:{}.", startDate, endDate);
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
            return Arrays.stream(scheduleTimeList.split(COMMA)).map(String::trim).distinct()
                    .collect(Collectors.joining(COMMA));
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
        ProcessInstance processInstance = processService.findProcessInstanceDetailById(processInstanceId).orElse(null);
        if (processInstance == null) {
            log.error("Process instance does not exist, processInstanceId:{}.", processInstanceId);
            return null;
        }
        Host host = new Host(processInstance.getHost());
        WorkflowExecutingDataRequestCommand requestCommand = new WorkflowExecutingDataRequestCommand();
        requestCommand.setProcessInstanceId(processInstanceId);
        org.apache.dolphinscheduler.remote.command.Command command =
                stateEventCallbackService.sendSync(host, requestCommand.convert2Command());
        if (command == null) {
            log.error("Query executing process instance from master error, processInstanceId:{}.",
                    processInstanceId);
            return null;
        }
        WorkflowExecutingDataResponseCommand responseCommand =
                JSONUtils.parseObject(command.getBody(), WorkflowExecutingDataResponseCommand.class);
        return responseCommand.getWorkflowExecuteDto();
    }

    @Override
    public Map<String, Object> execStreamTaskInstance(User loginUser, long projectCode, long taskDefinitionCode,
                                                      int taskDefinitionVersion,
                                                      int warningGroupId, String workerGroup, Long environmentCode,
                                                      Map<String, String> startParams, int dryRun) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
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

        org.apache.dolphinscheduler.remote.command.Command response =
                stateEventCallbackService.sendSync(host, taskExecuteStartCommand.convert2Command());
        if (response != null) {
            log.info("Send task execute start command complete, response is {}.", response);
            putMsg(result, Status.SUCCESS);
        } else {
            log.error(
                    "Start to execute stream task instance error, projectCode:{}, taskDefinitionCode:{}, taskVersion:{}.",
                    projectCode, taskDefinitionCode, taskDefinitionVersion);
            putMsg(result, Status.START_TASK_INSTANCE_ERROR);
        }
        return result;
    }
}
