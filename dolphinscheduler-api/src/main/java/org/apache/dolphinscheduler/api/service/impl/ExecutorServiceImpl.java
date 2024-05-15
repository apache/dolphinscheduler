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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
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
import org.apache.dolphinscheduler.api.executor.ExecuteClient;
import org.apache.dolphinscheduler.api.executor.ExecuteContext;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.MonitorService;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.WorkerGroupService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ApiTriggerType;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.CycleEnum;
import org.apache.dolphinscheduler.common.enums.ExecutionOrder;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
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
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.master.IStreamingTaskOperator;
import org.apache.dolphinscheduler.extract.master.ITaskInstanceExecutionEventListener;
import org.apache.dolphinscheduler.extract.master.IWorkflowInstanceService;
import org.apache.dolphinscheduler.extract.master.dto.WorkflowExecuteDto;
import org.apache.dolphinscheduler.extract.master.transportor.StreamingTaskTriggerRequest;
import org.apache.dolphinscheduler.extract.master.transportor.StreamingTaskTriggerResponse;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowInstanceStateChangeEvent;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.service.command.CommandService;
import org.apache.dolphinscheduler.service.cron.CronUtils;
import org.apache.dolphinscheduler.service.exceptions.CronParseException;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.process.TriggerRelationService;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Splitter;

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
    private ProcessInstanceDao processInstanceDao;

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired
    private CommandService commandService;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

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

    @Autowired
    private ExecuteClient executeClient;

    @Autowired
    private TenantMapper tenantMapper;

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
     * @param tenantCode                tenant code
     * @param environmentCode           environment code
     * @param runMode                   run mode
     * @param timeout                   timeout
     * @param startParamList               the global param values which pass to new process instance
     * @param expectedParallelismNumber the expected parallelism number when execute complement in parallel mode
     * @param testFlag testFlag
     * @param executionOrder the execution order when complementing data
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
                                                   String tenantCode,
                                                   Long environmentCode, Integer timeout,
                                                   List<Property> startParamList, Integer expectedParallelismNumber,
                                                   int dryRun, int testFlag,
                                                   ComplementDependentMode complementDependentMode, Integer version,
                                                   boolean allLevelDependent, ExecutionOrder executionOrder) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_START);

        Map<String, Object> result = new HashMap<>();
        // timeout is invalid
        if (timeout <= 0 || timeout > MAX_TASK_TIMEOUT) {
            log.warn("Parameter timeout is invalid, timeout:{}.", timeout);
            putMsg(result, Status.TASK_TIMEOUT_PARAMS_ERROR);
            return result;
        }

        if (Objects.nonNull(expectedParallelismNumber) && expectedParallelismNumber <= 0) {
            log.warn("Parameter expectedParallelismNumber is invalid, expectedParallelismNumber:{}.",
                    expectedParallelismNumber);
            putMsg(result, Status.TASK_PARALLELISM_PARAMS_ERROR);
            return result;
        }

        checkValidTenant(tenantCode);
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

        checkScheduleTimeNumExceed(commandType, cronTime);
        checkMasterExists();

        long triggerCode = CodeGenerateUtils.genCode();

        /**
         * create command
         */
        int create =
                this.createCommand(triggerCode, commandType, processDefinition.getCode(), taskDependType,
                        failureStrategy,
                        startNodeList,
                        cronTime, warningType, loginUser.getId(), warningGroupId, runMode, processInstancePriority,
                        workerGroup, tenantCode,
                        environmentCode, startParamList, expectedParallelismNumber, dryRun, testFlag,
                        complementDependentMode, allLevelDependent, executionOrder);

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

    private void checkMasterExists() {
        // check master server exists
        List<Server> masterServers = monitorService.listServer(RegistryNodeType.MASTER);

        // no master
        if (masterServers.isEmpty()) {
            throw new ServiceException(Status.MASTER_NOT_EXISTS);
        }
    }

    private void checkScheduleTimeNumExceed(CommandType complementData, String cronTime) {
        if (!CommandType.COMPLEMENT_DATA.equals(complementData)) {
            return;
        }
        if (cronTime == null) {
            return;
        }
        Map<String, String> cronMap = JSONUtils.toMap(cronTime);
        if (cronMap.containsKey(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST)) {
            String[] stringDates = cronMap.get(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST).split(COMMA);
            if (stringDates.length > SCHEDULE_TIME_MAX_LENGTH) {
                log.warn("Parameter cornTime is bigger than {}.", SCHEDULE_TIME_MAX_LENGTH);
                throw new ServiceException(Status.SCHEDULE_TIME_NUMBER_EXCEED);
            }
        }
    }

    /**
     * check whether the process definition can be executed
     *
     * @param projectCode       project code
     * @param processDefinition process definition
     */
    @Override
    public void checkProcessDefinitionValid(long projectCode, ProcessDefinition processDefinition,
                                            long processDefineCode, Integer version) {
        // check process definition exists
        if (projectCode != processDefinition.getProjectCode()) {
            throw new ServiceException(Status.PROCESS_DEFINE_NOT_EXIST, processDefinition.getCode());
        }
        // check process definition online
        if (processDefinition.getReleaseState() != ReleaseState.ONLINE) {
            throw new ServiceException(Status.PROCESS_DEFINE_NOT_RELEASE, processDefinition.getCode(),
                    processDefinition.getVersion());
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
     * check valid tenant
     *
     * @param tenantCode
     */
    private void checkValidTenant(String tenantCode) {
        if (!Constants.DEFAULT.equals(tenantCode)) {
            Tenant tenant = tenantMapper.queryByTenantCode(tenantCode);
            if (tenant == null) {
                throw new ServiceException(Status.TENANT_NOT_EXIST, tenantCode);
            }
        }
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
    public Map<String, Object> execute(User loginUser,
                                       long projectCode,
                                       Integer processInstanceId,
                                       ExecuteType executeType) {
        checkNotNull(processInstanceId, "workflowInstanceId cannot be null");
        checkNotNull(executeType, "executeType cannot be null");

        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode,
                ApiFuncIdentificationConstant.map.get(executeType));
        checkMasterExists();

        ProcessInstance workflowInstance = processInstanceDao.queryOptionalById(processInstanceId)
                .orElseThrow(() -> new ServiceException(Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId));

        checkState(workflowInstance.getProjectCode() == projectCode,
                "The workflow instance's project code doesn't equals to the given project");
        ProcessDefinition processDefinition = processDefinitionService.queryWorkflowDefinitionThrowExceptionIfNotFound(
                workflowInstance.getProcessDefinitionCode(), workflowInstance.getProcessDefinitionVersion());

        executeClient.executeWorkflowInstance(new ExecuteContext(
                workflowInstance,
                processDefinition,
                loginUser,
                executeType));

        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, Status.SUCCESS);
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
        return execute(loginUser, processInstance.getProjectCode(), workflowInstanceId, executeType);
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

        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode,
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
        ProcessInstance processInstance = processInstanceDao.queryOptionalById(taskGroupQueue.getProcessId())
                .orElseThrow(
                        () -> new ServiceException(Status.PROCESS_INSTANCE_NOT_EXIST, taskGroupQueue.getProcessId()));
        checkMasterExists();

        if (taskGroupQueue.getInQueue() == Flag.NO.getCode()) {
            throw new ServiceException(Status.TASK_GROUP_QUEUE_ALREADY_START);
        }
        taskGroupQueue.setForceStart(Flag.YES.getCode());
        taskGroupQueue.setUpdateTime(new Date());
        taskGroupQueueMapper.updateById(taskGroupQueue);

        result.put(Constants.STATUS, Status.SUCCESS);
        return result;
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
        boolean update = processInstanceDao.updateById(processInstance);

        // determine whether the process is normal
        if (update) {
            log.info("Process instance state is updated to {} in database, processInstanceName:{}.",
                    executionStatus.getDesc(), processInstance.getName());
            // directly send the process instance state change event to target master, not guarantee the event send
            // success
            WorkflowInstanceStateChangeEvent workflowStateEventChangeRequest = new WorkflowInstanceStateChangeEvent(
                    processInstance.getId(), 0, processInstance.getState(), processInstance.getId(), 0);
            ITaskInstanceExecutionEventListener iTaskInstanceExecutionEventListener =
                    SingletonJdkDynamicRpcClientProxyFactory
                            .getProxyClient(processInstance.getHost(), ITaskInstanceExecutionEventListener.class);
            iTaskInstanceExecutionEventListener.onWorkflowInstanceInstanceStateChange(workflowStateEventChangeRequest);
            putMsg(result, Status.SUCCESS);
        } else {
            log.error("Process instance state update error, processInstanceName:{}.", processInstance.getName());
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

        List<Long> codes = processService.findAllSubWorkflowDefinitionCode(processDefinition.getCode());
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
     * @param allLevelDependent       allLevelDependent
     * @param executionOrder          executionOrder
     * @return command id
     */
    private int createCommand(Long triggerCode, CommandType commandType, long processDefineCode, TaskDependType nodeDep,
                              FailureStrategy failureStrategy, String startNodeList, String schedule,
                              WarningType warningType, int executorId, Integer warningGroupId, RunMode runMode,
                              Priority processInstancePriority, String workerGroup, String tenantCode,
                              Long environmentCode,
                              List<Property> startParamList, Integer expectedParallelismNumber, int dryRun,
                              int testFlag, ComplementDependentMode complementDependentMode,
                              boolean allLevelDependent, ExecutionOrder executionOrder) {

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
        if (CollectionUtils.isNotEmpty(startParamList)) {
            cmdParam.put(CMD_PARAM_START_PARAMS, JSONUtils.toJsonString(startParamList));
        }
        command.setCommandParam(JSONUtils.toJsonString(cmdParam));
        command.setExecutorId(executorId);
        command.setWarningGroupId(warningGroupId);
        command.setProcessInstancePriority(processInstancePriority);
        command.setWorkerGroup(workerGroup);
        command.setTenantCode(tenantCode);
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
                        complementDependentMode, allLevelDependent, executionOrder);
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

    private int createComplementCommand(Long triggerCode, Command command, Map<String, String> cmdParam,
                                        List<ZonedDateTime> dateTimeList, List<Schedule> schedules,
                                        ComplementDependentMode complementDependentMode, boolean allLevelDependent) {

        String dateTimeListStr = dateTimeList.stream()
                .map(item -> DateUtils.dateToString(item))
                .collect(Collectors.joining(COMMA));

        cmdParam.put(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST, dateTimeListStr);
        command.setCommandParam(JSONUtils.toJsonString(cmdParam));

        log.info("Creating command, commandInfo:{}.", command);
        int createCount = commandService.createCommand(command);

        if (createCount > 0) {
            log.info("Create {} command complete, processDefinitionCode:{}",
                    command.getCommandType().getDescp(), command.getProcessDefinitionCode());
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
            createComplementDependentCommand(schedules, command, allLevelDependent);
        }

        if (createCount > 0) {
            triggerRelationService.saveTriggerToDb(ApiTriggerType.COMMAND, triggerCode, command.getId());
        }
        return createCount;
    }

    /**
     * create complement command
     * close left and close right
     *
     * @param scheduleTimeParam
     * @param runMode
     * @param executionOrder
     * @return
     */
    protected int createComplementCommandList(Long triggerCode, String scheduleTimeParam, RunMode runMode,
                                              Command command,
                                              Integer expectedParallelismNumber,
                                              ComplementDependentMode complementDependentMode,
                                              boolean allLevelDependent,
                                              ExecutionOrder executionOrder) throws CronParseException {
        int createCount = 0;
        int dependentProcessDefinitionCreateCount = 0;
        runMode = (runMode == null) ? RunMode.RUN_MODE_SERIAL : runMode;
        Map<String, String> cmdParam = JSONUtils.toMap(command.getCommandParam());
        Map<String, String> scheduleParam = JSONUtils.toMap(scheduleTimeParam);

        if (Objects.isNull(executionOrder)) {
            executionOrder = ExecutionOrder.DESC_ORDER;
        }

        List<Schedule> schedules = processService.queryReleaseSchedulerListByProcessDefinitionCode(
                command.getProcessDefinitionCode());

        List<ZonedDateTime> listDate = new ArrayList<>();
        if (scheduleParam.containsKey(CMD_PARAM_COMPLEMENT_DATA_START_DATE) && scheduleParam.containsKey(
                CMD_PARAM_COMPLEMENT_DATA_END_DATE)) {
            String startDate = scheduleParam.get(CMD_PARAM_COMPLEMENT_DATA_START_DATE);
            String endDate = scheduleParam.get(CMD_PARAM_COMPLEMENT_DATA_END_DATE);
            if (startDate != null && endDate != null) {
                listDate = CronUtils.getSelfFireDateList(
                        DateUtils.stringToZoneDateTime(startDate),
                        DateUtils.stringToZoneDateTime(endDate),
                        schedules);
            }
        }

        if (scheduleParam.containsKey(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST)) {
            String dateList = scheduleParam.get(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST);

            if (StringUtils.isNotBlank(dateList)) {
                listDate = Splitter.on(COMMA).splitToStream(dateList)
                        .map(item -> DateUtils.stringToZoneDateTime(item.trim()))
                        .distinct()
                        .collect(Collectors.toList());
            }
        }

        if (CollectionUtils.isEmpty(listDate)) {
            throw new ServiceException(Status.TASK_COMPLEMENT_DATA_DATE_ERROR);
        }

        if (executionOrder.equals(ExecutionOrder.DESC_ORDER)) {
            Collections.sort(listDate, Collections.reverseOrder());
        } else {
            Collections.sort(listDate);
        }

        switch (runMode) {
            case RUN_MODE_SERIAL: {
                log.info("RunMode of {} command is serial run, processDefinitionCode:{}.",
                        command.getCommandType().getDescp(), command.getProcessDefinitionCode());
                createCount = createComplementCommand(triggerCode, command, cmdParam, listDate, schedules,
                        complementDependentMode, allLevelDependent);
                break;
            }
            case RUN_MODE_PARALLEL: {
                log.info("RunMode of {} command is parallel run, processDefinitionCode:{}.",
                        command.getCommandType().getDescp(), command.getProcessDefinitionCode());

                int queueNum = 0;
                if (CollectionUtils.isNotEmpty(listDate)) {
                    queueNum = listDate.size();
                    if (expectedParallelismNumber != null && expectedParallelismNumber != 0) {
                        queueNum = Math.min(queueNum, expectedParallelismNumber);
                    }
                    log.info("Complement command run in parallel mode, current expectedParallelismNumber:{}.",
                            queueNum);
                    List[] queues = new List[queueNum];

                    for (int i = 0; i < listDate.size(); i++) {
                        if (Objects.isNull(queues[i % queueNum])) {
                            queues[i % queueNum] = new ArrayList();
                        }
                        queues[i % queueNum].add(listDate.get(i));
                    }
                    for (List queue : queues) {
                        createCount = createComplementCommand(triggerCode, command, cmdParam, queue, schedules,
                                complementDependentMode, allLevelDependent);
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
    public int createComplementDependentCommand(List<Schedule> schedules, Command command, boolean allLevelDependent) {
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
                        CronUtils.getMaxCycle(schedules.get(0).getCrontab()), dependentCommand.getWorkerGroup(),
                        allLevelDependent);
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
                                                                                  String workerGroup,
                                                                                  boolean allLevelDependent) {
        List<DependentProcessDefinition> dependentProcessDefinitionList =
                checkDependentProcessDefinitionValid(
                        processService.queryDependentProcessDefinitionByProcessDefinitionCode(processDefinitionCode),
                        processDefinitionCycle, workerGroup,
                        processDefinitionCode);

        if (dependentProcessDefinitionList.isEmpty()) {
            return dependentProcessDefinitionList;
        }

        if (allLevelDependent) {
            List<DependentProcessDefinition> childList = new ArrayList<>(dependentProcessDefinitionList);
            while (true) {
                List<DependentProcessDefinition> childDependentList = childList
                        .stream()
                        .flatMap(dependentProcessDefinition -> checkDependentProcessDefinitionValid(
                                processService.queryDependentProcessDefinitionByProcessDefinitionCode(
                                        dependentProcessDefinition.getProcessDefinitionCode()),
                                processDefinitionCycle,
                                workerGroup,
                                dependentProcessDefinition.getProcessDefinitionCode()).stream())
                        .collect(Collectors.toList());
                if (childDependentList.isEmpty()) {
                    break;
                }
                dependentProcessDefinitionList.addAll(childDependentList);
                childList = new ArrayList<>(childDependentList);
            }
        }
        return dependentProcessDefinitionList;
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
        IWorkflowInstanceService iWorkflowInstanceService = SingletonJdkDynamicRpcClientProxyFactory
                .getProxyClient(processInstance.getHost(), IWorkflowInstanceService.class);
        return iWorkflowInstanceService.getWorkflowExecutingData(processInstanceId);
    }

    @Override
    public void execStreamTaskInstance(User loginUser, long projectCode, long taskDefinitionCode,
                                       int taskDefinitionVersion,
                                       int warningGroupId, String workerGroup, String tenantCode,
                                       Long environmentCode,
                                       Map<String, String> startParams, int dryRun) {
        Project project = projectMapper.queryByCode(projectCode);
        // check user access for project
        projectService.checkProjectAndAuth(loginUser, project, projectCode, WORKFLOW_START);

        checkValidTenant(tenantCode);
        checkMasterExists();
        // todo dispatch improvement
        List<Server> masterServerList = monitorService.listServer(RegistryNodeType.MASTER);
        Server server = masterServerList.get(0);

        StreamingTaskTriggerRequest taskExecuteStartMessage = new StreamingTaskTriggerRequest();
        taskExecuteStartMessage.setExecutorId(loginUser.getId());
        taskExecuteStartMessage.setExecutorName(loginUser.getUserName());
        taskExecuteStartMessage.setProjectCode(projectCode);
        taskExecuteStartMessage.setTaskDefinitionCode(taskDefinitionCode);
        taskExecuteStartMessage.setTaskDefinitionVersion(taskDefinitionVersion);
        taskExecuteStartMessage.setWorkerGroup(workerGroup);
        taskExecuteStartMessage.setTenantCode(tenantCode);
        taskExecuteStartMessage.setWarningGroupId(warningGroupId);
        taskExecuteStartMessage.setEnvironmentCode(environmentCode);
        taskExecuteStartMessage.setStartParams(startParams);
        taskExecuteStartMessage.setDryRun(dryRun);

        IStreamingTaskOperator streamingTaskOperator = SingletonJdkDynamicRpcClientProxyFactory
                .getProxyClient(server.getHost() + ":" + server.getPort(), IStreamingTaskOperator.class);
        StreamingTaskTriggerResponse streamingTaskTriggerResponse =
                streamingTaskOperator.triggerStreamingTask(taskExecuteStartMessage);
        if (streamingTaskTriggerResponse.isSuccess()) {
            log.info("Send task execute start command complete, response is {}.", streamingTaskOperator);
            return;
        }
        log.error(
                "Start to execute stream task instance error, projectCode:{}, taskDefinitionCode:{}, taskVersion:{}, response: {}.",
                projectCode, taskDefinitionCode, taskDefinitionVersion, streamingTaskTriggerResponse);
        throw new ServiceException(Status.START_TASK_INSTANCE_ERROR);
    }
}
