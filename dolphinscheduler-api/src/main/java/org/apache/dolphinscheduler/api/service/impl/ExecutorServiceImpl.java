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
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_START_NODES;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_SUB_PROCESS_DEFINE_CODE;
import static org.apache.dolphinscheduler.common.constants.Constants.COMMA;

import org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowBackFillRequest;
import org.apache.dolphinscheduler.api.dto.workflow.WorkflowTriggerRequest;
import org.apache.dolphinscheduler.api.dto.workflowInstance.WorkflowExecuteResponse;
import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.executor.workflow.ExecutorClient;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.MonitorService;
import org.apache.dolphinscheduler.api.service.WorkflowDefinitionService;
import org.apache.dolphinscheduler.api.service.WorkflowLineageService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.service.WorkerGroupService;
import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowDTO;
import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowDTOValidator;
import org.apache.dolphinscheduler.api.validator.workflow.BackfillWorkflowRequestTransformer;
import org.apache.dolphinscheduler.api.validator.workflow.TriggerWorkflowDTO;
import org.apache.dolphinscheduler.api.validator.workflow.TriggerWorkflowDTOValidator;
import org.apache.dolphinscheduler.api.validator.workflow.TriggerWorkflowRequestTransformer;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.common.enums.CycleEnum;
import org.apache.dolphinscheduler.common.enums.ExecutionOrder;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.DependentProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
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
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.service.command.CommandService;
import org.apache.dolphinscheduler.service.cron.CronUtils;
import org.apache.dolphinscheduler.service.exceptions.CronParseException;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
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
    private WorkflowDefinitionService workflowDefinitionService;

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
    private TenantMapper tenantMapper;

    @Autowired
    private WorkflowLineageService workflowLineageService;

    @Autowired
    private TriggerWorkflowRequestTransformer triggerWorkflowRequestTransformer;

    @Autowired
    private TriggerWorkflowDTOValidator triggerWorkflowDTOValidator;

    @Autowired
    private BackfillWorkflowRequestTransformer backfillWorkflowRequestTransformer;

    @Autowired
    private BackfillWorkflowDTOValidator backfillWorkflowDTOValidator;

    @Autowired
    private ExecutorClient executorClient;

    @Override
    @Transactional
    public Integer triggerWorkflowDefinition(final WorkflowTriggerRequest triggerRequest) {
        final TriggerWorkflowDTO triggerWorkflowDTO = triggerWorkflowRequestTransformer.transform(triggerRequest);
        triggerWorkflowDTOValidator.validate(triggerWorkflowDTO);
        return executorClient.triggerWorkflowDefinition().execute(triggerWorkflowDTO);
    }

    @Override
    @Transactional
    public List<Integer> backfillWorkflowDefinition(final WorkflowBackFillRequest workflowBackFillRequest) {
        final BackfillWorkflowDTO backfillWorkflowDTO =
                backfillWorkflowRequestTransformer.transform(workflowBackFillRequest);
        backfillWorkflowDTOValidator.validate(backfillWorkflowDTO);
        return executorClient.backfillWorkflowDefinition().execute(backfillWorkflowDTO);
    }

    /**
     * check whether the process definition can be executed
     *
     * @param projectCode       project code
     * @param workflowDefinition process definition
     */
    @Override
    public void checkProcessDefinitionValid(long projectCode, WorkflowDefinition workflowDefinition,
                                            long processDefineCode, Integer version) {
        // check process definition exists
        if (projectCode != workflowDefinition.getProjectCode()) {
            throw new ServiceException(Status.PROCESS_DEFINE_NOT_EXIST, workflowDefinition.getCode());
        }
        // check process definition online
        if (workflowDefinition.getReleaseState() != ReleaseState.ONLINE) {
            throw new ServiceException(Status.PROCESS_DEFINE_NOT_RELEASE, workflowDefinition.getCode(),
                    workflowDefinition.getVersion());
        }
        // check sub process definition online
        if (!checkSubProcessDefinitionValid(workflowDefinition)) {
            throw new ServiceException(Status.SUB_PROCESS_DEFINE_NOT_RELEASE);
        }
    }

    /**
     * check whether the current process has subprocesses and validate all subprocesses
     *
     * @param workflowDefinition
     * @return check result
     */
    @Override
    public boolean checkSubProcessDefinitionValid(WorkflowDefinition workflowDefinition) {
        // query all subprocesses under the current process
        List<WorkflowTaskRelation> workflowTaskRelations =
                processTaskRelationMapper.queryDownstreamByProcessDefinitionCode(workflowDefinition.getCode());
        if (workflowTaskRelations.isEmpty()) {
            return true;
        }
        Set<Long> relationCodes =
                workflowTaskRelations.stream().map(WorkflowTaskRelation::getPostTaskCode).collect(Collectors.toSet());
        List<TaskDefinition> taskDefinitions = taskDefinitionMapper.queryByCodeList(relationCodes);

        // find out the process definition code
        Set<Long> processDefinitionCodeSet = new HashSet<>();
        taskDefinitions.stream()
                .filter(task -> TaskTypeUtils.isSubWorkflowTask(task.getTaskType())).forEach(
                        taskDefinition -> processDefinitionCodeSet.add(Long.valueOf(
                                JSONUtils.getNodeString(taskDefinition.getTaskParams(),
                                        CMD_PARAM_SUB_PROCESS_DEFINE_CODE))));
        if (processDefinitionCodeSet.isEmpty()) {
            return true;
        }

        // check sub releaseState
        List<WorkflowDefinition> workflowDefinitions = processDefinitionMapper.queryByCodes(processDefinitionCodeSet);
        return workflowDefinitions.stream()
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

    @Override
    public void controlWorkflowInstance(User loginUser, Integer workflowInstanceId, ExecuteType executeType) {
        checkNotNull(workflowInstanceId, "workflowInstanceId cannot be null");
        checkNotNull(executeType, "executeType cannot be null");

        WorkflowInstance workflowInstance = processInstanceDao
                .queryOptionalById(workflowInstanceId)
                .orElseThrow(() -> new ServiceException(Status.PROCESS_INSTANCE_NOT_EXIST, workflowInstanceId));

        // check user access for project
        projectService.checkProjectAndAuthThrowException(
                loginUser,
                workflowInstance.getProjectCode(),
                ApiFuncIdentificationConstant.map.get(executeType));

        switch (executeType) {
            case REPEAT_RUNNING:
                executorClient
                        .repeatRunningWorkflowInstance()
                        .onWorkflowInstance(workflowInstance)
                        .byUser(loginUser)
                        .execute();
                return;
            case START_FAILURE_TASK_PROCESS:
                executorClient.recoverFailureTaskInstance()
                        .onWorkflowInstance(workflowInstance)
                        .byUser(loginUser)
                        .execute();
                return;
            case RECOVER_SUSPENDED_PROCESS:
                executorClient.recoverSuspendedWorkflowInstanceOperation()
                        .onWorkflowInstance(workflowInstance)
                        .byUser(loginUser)
                        .execute();
                return;
            case PAUSE:
                executorClient.pauseWorkflowInstance()
                        .onWorkflowInstance(workflowInstance)
                        .byUser(loginUser)
                        .execute();
                return;
            case STOP:
                executorClient.stopWorkflowInstance()
                        .onWorkflowInstance(workflowInstance)
                        .byUser(loginUser)
                        .execute();
                return;
            default:
                throw new ServiceException("Unsupported executeType: " + executeType);
        }
    }

    /**
     * do action to execute task in process instance
     *
     * @param loginUser         login user
     * @param projectCode       project code
     * @param processInstanceId process instance id
     * @param startNodeList     start node list
     * @param taskDependType    task depend type
     * @return execute result code
     */
    @Override
    public WorkflowExecuteResponse executeTask(User loginUser,
                                               long projectCode,
                                               Integer processInstanceId,
                                               String startNodeList,
                                               TaskDependType taskDependType) {

        WorkflowExecuteResponse response = new WorkflowExecuteResponse();

        // check user access for project
        projectService.checkProjectAndAuthThrowException(loginUser, projectCode,
                ApiFuncIdentificationConstant.map.get(ExecuteType.EXECUTE_TASK));

        WorkflowInstance workflowInstance = processService.findProcessInstanceDetailById(processInstanceId)
                .orElseThrow(() -> new ServiceException(Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId));

        if (!workflowInstance.getState().isFinished()) {
            log.error("Can not execute task for process instance which is not finished, processInstanceId:{}.",
                    processInstanceId);
            putMsg(response, Status.WORKFLOW_INSTANCE_IS_NOT_FINISHED);
            return response;
        }

        WorkflowDefinition workflowDefinition =
                processService.findProcessDefinition(workflowInstance.getProcessDefinitionCode(),
                        workflowInstance.getProcessDefinitionVersion());
        workflowDefinition.setReleaseState(ReleaseState.ONLINE);
        this.checkProcessDefinitionValid(projectCode, workflowDefinition, workflowInstance.getProcessDefinitionCode(),
                workflowInstance.getProcessDefinitionVersion());

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
        command.setProcessDefinitionCode(workflowDefinition.getCode());
        command.setCommandParam(JSONUtils.toJsonString(cmdParam));
        command.setExecutorId(loginUser.getId());
        command.setProcessDefinitionVersion(workflowDefinition.getVersion());
        command.setProcessInstanceId(processInstanceId);
        command.setTestFlag(workflowInstance.getTestFlag());

        // Add taskDependType
        command.setTaskDependType(taskDependType);

        if (!commandService.verifyIsNeedCreateCommand(command)) {
            log.warn(
                    "Process instance is executing the command, processDefinitionCode:{}, processDefinitionVersion:{}, processInstanceId:{}.",
                    workflowDefinition.getCode(), workflowDefinition.getVersion(), processInstanceId);
            putMsg(response, Status.PROCESS_INSTANCE_EXECUTING_COMMAND,
                    String.valueOf(workflowDefinition.getCode()));
            return response;
        }

        log.info("Creating command, commandInfo:{}.", command);
        int create = commandService.createCommand(command);

        if (create > 0) {
            log.info("Create {} command complete, processDefinitionCode:{}, processDefinitionVersion:{}.",
                    command.getCommandType().getDescp(), command.getProcessDefinitionCode(),
                    workflowDefinition.getVersion());
            putMsg(response, Status.SUCCESS);
        } else {
            log.error(
                    "Execute process instance failed because create {} command error, processDefinitionCode:{}, processDefinitionVersion:{}， processInstanceId:{}.",
                    command.getCommandType().getDescp(), command.getProcessDefinitionCode(),
                    workflowDefinition.getVersion(),
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
        processInstanceDao.queryOptionalById(taskGroupQueue.getProcessId())
                .orElseThrow(
                        () -> new ServiceException(Status.PROCESS_INSTANCE_NOT_EXIST, taskGroupQueue.getProcessId()));

        if (taskGroupQueue.getInQueue() == Flag.NO.getCode()) {
            throw new ServiceException(Status.TASK_GROUP_QUEUE_ALREADY_START);
        }
        taskGroupQueue.setForceStart(Flag.YES.getCode());
        taskGroupQueue.setUpdateTime(new Date());
        taskGroupQueueMapper.updateById(taskGroupQueue);

        result.put(Constants.STATUS, Status.SUCCESS);
        return result;
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

        return createCount;
    }

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
                        workflowLineageService.queryDownstreamDependentProcessDefinitions(processDefinitionCode),
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
                                workflowLineageService.queryDownstreamDependentProcessDefinitions(
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

    @Override
    public void execStreamTaskInstance(User loginUser,
                                       long projectCode,
                                       long taskDefinitionCode,
                                       int taskDefinitionVersion,
                                       int warningGroupId,
                                       String workerGroup,
                                       String tenantCode,
                                       Long environmentCode,
                                       Map<String, String> startParams,
                                       int dryRun) {
        throw new ServiceException("Not supported");
    }
}
