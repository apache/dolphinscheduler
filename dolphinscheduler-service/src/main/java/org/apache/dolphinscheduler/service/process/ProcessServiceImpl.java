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

package org.apache.dolphinscheduler.service.process;

import static java.util.stream.Collectors.toSet;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_EMPTY_SUB_PROCESS;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_START_PARAMS;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_SUB_PROCESS;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_SUB_PROCESS_DEFINE_CODE;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_SUB_PROCESS_PARENT_INSTANCE_ID;
import static org.apache.dolphinscheduler.common.constants.Constants.LOCAL_PARAMS;
import static org.apache.dolphinscheduler.plugin.task.api.enums.DataType.VARCHAR;
import static org.apache.dolphinscheduler.plugin.task.api.enums.Direct.IN;
import static org.apache.dolphinscheduler.plugin.task.api.utils.DataQualityConstants.TASK_INSTANCE_ID;

import org.apache.dolphinscheduler.common.constants.CommandKeyConstants;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils.CodeGenerateException;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Cluster;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.DependentProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.DqComparisonType;
import org.apache.dolphinscheduler.dao.entity.DqExecuteResult;
import org.apache.dolphinscheduler.dao.entity.DqRule;
import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.dao.entity.DqTaskStatisticsValue;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ClusterMapper;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.DqComparisonTypeMapper;
import org.apache.dolphinscheduler.dao.mapper.DqExecuteResultMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleExecuteSqlMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleInputEntryMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleMapper;
import org.apache.dolphinscheduler.dao.mapper.DqTaskStatisticsValueMapper;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentMapper;
import org.apache.dolphinscheduler.dao.mapper.ErrorCommandMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.ResourceUserMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkFlowLineageMapper;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceMapDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.utils.DqRuleUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.DqTaskState;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SubProcessParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.TaskTimeoutParameter;
import org.apache.dolphinscheduler.remote.command.workflow.WorkflowStateEventChangeRequest;
import org.apache.dolphinscheduler.remote.processor.StateEventCallbackService;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.command.CommandService;
import org.apache.dolphinscheduler.service.cron.CronUtils;
import org.apache.dolphinscheduler.service.exceptions.CronParseException;
import org.apache.dolphinscheduler.service.exceptions.ServiceException;
import org.apache.dolphinscheduler.service.expand.CuringParamsService;
import org.apache.dolphinscheduler.service.log.LogClient;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.utils.ClusterConfUtils;
import org.apache.dolphinscheduler.service.utils.DagHelper;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * process relative dao that some mappers in this.
 */
@Component
@Slf4j
public class ProcessServiceImpl implements ProcessService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProcessDefinitionMapper processDefineMapper;

    @Autowired
    private ProcessDefinitionLogMapper processDefineLogMapper;

    // todo replace with processInstanceDao
    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Autowired
    private ProcessInstanceDao processInstanceDao;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private TaskDefinitionLogDao taskDefinitionLogDao;

    @Autowired
    private ProcessInstanceMapDao processInstanceMapDao;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private ProcessInstanceMapMapper processInstanceMapMapper;

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private UdfFuncMapper udfFuncMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private ResourceUserMapper resourceUserMapper;

    @Autowired
    private ErrorCommandMapper errorCommandMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private DqExecuteResultMapper dqExecuteResultMapper;

    @Autowired
    private DqRuleMapper dqRuleMapper;

    @Autowired
    private DqRuleInputEntryMapper dqRuleInputEntryMapper;

    @Autowired
    private DqRuleExecuteSqlMapper dqRuleExecuteSqlMapper;

    @Autowired
    private DqComparisonTypeMapper dqComparisonTypeMapper;

    @Autowired
    private DqTaskStatisticsValueMapper dqTaskStatisticsValueMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Autowired
    private ProcessTaskRelationLogMapper processTaskRelationLogMapper;

    @Autowired
    StateEventCallbackService stateEventCallbackService;

    @Autowired
    private EnvironmentMapper environmentMapper;

    @Autowired
    private TaskGroupQueueMapper taskGroupQueueMapper;

    @Autowired
    private TaskGroupMapper taskGroupMapper;

    @Autowired
    private WorkFlowLineageMapper workFlowLineageMapper;

    @Autowired
    private TaskPluginManager taskPluginManager;

    @Autowired
    private ClusterMapper clusterMapper;

    @Autowired
    private CuringParamsService curingGlobalParamsService;

    @Autowired
    private LogClient logClient;

    @Autowired
    private CommandService commandService;

    @Autowired
    private TriggerRelationService triggerRelationService;
    /**
     * todo: split this method
     * handle Command (construct ProcessInstance from Command) , wrapped in transaction
     *
     * @param host    host
     * @param command found command
     * @return process instance
     */
    @Override
    @Transactional
    public @Nullable ProcessInstance handleCommand(String host,
                                                   Command command) throws CronParseException, CodeGenerateException {
        ProcessInstance processInstance = constructProcessInstance(command, host);
        // cannot construct process instance, return null
        if (processInstance == null) {
            log.error("scan command, command parameter is error: {}", command);
            commandService.moveToErrorCommand(command, "process instance is null");
            return null;
        }
        processInstance.setCommandType(command.getCommandType());
        processInstance.addHistoryCmd(command.getCommandType());
        processInstance.setTestFlag(command.getTestFlag());
        // if the processDefinition is serial
        ProcessDefinition processDefinition = this.findProcessDefinition(processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion());
        if (processDefinition.getExecutionType().typeIsSerial()) {
            saveSerialProcess(processInstance, processDefinition);
            if (processInstance.getState() != WorkflowExecutionStatus.RUNNING_EXECUTION) {
                setSubProcessParam(processInstance);
                triggerRelationService.saveProcessInstanceTrigger(command.getId(), processInstance.getId());
                deleteCommandWithCheck(command.getId());
                // todo: this is a bad design to return null here, whether trigger the task
                return null;
            }
        } else {
            processInstanceDao.upsertProcessInstance(processInstance);
        }
        triggerRelationService.saveProcessInstanceTrigger(command.getId(), processInstance.getId());
        setSubProcessParam(processInstance);
        deleteCommandWithCheck(command.getId());
        return processInstance;
    }

    protected void saveSerialProcess(ProcessInstance processInstance, ProcessDefinition processDefinition) {
        processInstance.setStateWithDesc(WorkflowExecutionStatus.SERIAL_WAIT, "wait by serial_wait strategy");
        processInstanceDao.upsertProcessInstance(processInstance);
        // serial wait
        // when we get the running instance(or waiting instance) only get the priority instance(by id)
        if (processDefinition.getExecutionType().typeIsSerialWait()) {
            List<ProcessInstance> runningProcessInstances =
                    this.processInstanceMapper.queryByProcessDefineCodeAndProcessDefinitionVersionAndStatusAndNextId(
                            processInstance.getProcessDefinitionCode(),
                            processInstance.getProcessDefinitionVersion(),
                            org.apache.dolphinscheduler.service.utils.Constants.RUNNING_PROCESS_STATE,
                            processInstance.getId());
            if (CollectionUtils.isEmpty(runningProcessInstances)) {
                processInstance.setStateWithDesc(WorkflowExecutionStatus.RUNNING_EXECUTION,
                        "submit from serial_wait strategy");
                processInstanceDao.upsertProcessInstance(processInstance);
            }
        } else if (processDefinition.getExecutionType().typeIsSerialDiscard()) {
            List<ProcessInstance> runningProcessInstances =
                    this.processInstanceMapper.queryByProcessDefineCodeAndProcessDefinitionVersionAndStatusAndNextId(
                            processInstance.getProcessDefinitionCode(),
                            processInstance.getProcessDefinitionVersion(),
                            org.apache.dolphinscheduler.service.utils.Constants.RUNNING_PROCESS_STATE,
                            processInstance.getId());
            if (CollectionUtils.isNotEmpty(runningProcessInstances)) {
                processInstance.setStateWithDesc(WorkflowExecutionStatus.STOP, "stop by serial_discard strategy");
                processInstanceDao.upsertProcessInstance(processInstance);
                return;
            }
            processInstance.setStateWithDesc(WorkflowExecutionStatus.RUNNING_EXECUTION,
                    "submit from serial_discard strategy");
            processInstanceDao.upsertProcessInstance(processInstance);
        } else if (processDefinition.getExecutionType().typeIsSerialPriority()) {
            List<ProcessInstance> runningProcessInstances =
                    this.processInstanceMapper.queryByProcessDefineCodeAndProcessDefinitionVersionAndStatusAndNextId(
                            processInstance.getProcessDefinitionCode(),
                            processInstance.getProcessDefinitionVersion(),
                            org.apache.dolphinscheduler.service.utils.Constants.RUNNING_PROCESS_STATE,
                            processInstance.getId());
            for (ProcessInstance info : runningProcessInstances) {
                info.setCommandType(CommandType.STOP);
                info.addHistoryCmd(CommandType.STOP);
                info.setStateWithDesc(WorkflowExecutionStatus.READY_STOP, "ready stop by serial_priority strategy");
                boolean update = processInstanceDao.updateById(info);
                // determine whether the process is normal
                if (update) {
                    WorkflowStateEventChangeRequest workflowStateEventChangeRequest =
                            new WorkflowStateEventChangeRequest(
                                    info.getId(), 0, info.getState(), info.getId(), 0);
                    try {
                        Host host = new Host(info.getHost());
                        stateEventCallbackService.sendResult(host, workflowStateEventChangeRequest.convert2Command());
                    } catch (Exception e) {
                        log.error("sendResultError", e);
                    }
                }
            }
            processInstance.setStateWithDesc(WorkflowExecutionStatus.RUNNING_EXECUTION,
                    "submit by serial_priority strategy");
            processInstanceDao.upsertProcessInstance(processInstance);
        }
    }

    /**
     * find process instance detail by id
     *
     * @param processId processId
     * @return process instance
     */
    @Override
    public Optional<ProcessInstance> findProcessInstanceDetailById(int processId) {
        return Optional.ofNullable(processInstanceMapper.queryDetailById(processId));
    }

    /**
     * find process instance by id
     *
     * @param processId processId
     * @return process instance
     */
    @Override
    public ProcessInstance findProcessInstanceById(int processId) {
        return processInstanceMapper.selectById(processId);
    }

    /**
     * find process define by id.
     *
     * @param processDefinitionId processDefinitionId
     * @return process definition
     */
    @Override
    public ProcessDefinition findProcessDefineById(int processDefinitionId) {
        return processDefineMapper.selectById(processDefinitionId);
    }

    /**
     * find process define by code and version.
     *
     * @param processDefinitionCode processDefinitionCode
     * @return process definition
     */
    @Override
    public ProcessDefinition findProcessDefinition(Long processDefinitionCode, int version) {
        ProcessDefinition processDefinition = processDefineMapper.queryByCode(processDefinitionCode);
        if (processDefinition == null || processDefinition.getVersion() != version) {
            processDefinition = processDefineLogMapper.queryByDefinitionCodeAndVersion(processDefinitionCode, version);
            if (processDefinition != null) {
                processDefinition.setId(0);
            }
        }
        return processDefinition;
    }

    /**
     * find process define by code.
     *
     * @param processDefinitionCode processDefinitionCode
     * @return process definition
     */
    @Override
    public ProcessDefinition findProcessDefinitionByCode(Long processDefinitionCode) {
        return processDefineMapper.queryByCode(processDefinitionCode);
    }

    /**
     * delete work process instance by id
     *
     * @param processInstanceId processInstanceId
     * @return delete process instance result
     */
    @Override
    public int deleteWorkProcessInstanceById(int processInstanceId) {
        return processInstanceMapper.deleteById(processInstanceId);
    }

    /**
     * delete all sub process by parent instance id
     *
     * @param processInstanceId processInstanceId
     * @return delete all sub process instance result
     */
    @Override
    public int deleteAllSubWorkProcessByParentId(int processInstanceId) {

        List<Integer> subProcessIdList = processInstanceMapMapper.querySubIdListByParentId(processInstanceId);

        for (Integer subId : subProcessIdList) {
            deleteAllSubWorkProcessByParentId(subId);
            deleteWorkProcessMapByParentId(subId);
            removeTaskLogFile(subId);
            deleteWorkProcessInstanceById(subId);
        }
        return 1;
    }

    /**
     * remove task log file
     *
     * @param processInstanceId processInstanceId
     */
    @Override
    public void removeTaskLogFile(Integer processInstanceId) {
        List<TaskInstance> taskInstanceList = taskInstanceDao.queryByWorkflowInstanceId(processInstanceId);
        if (CollectionUtils.isEmpty(taskInstanceList)) {
            return;
        }
        for (TaskInstance taskInstance : taskInstanceList) {
            String taskLogPath = taskInstance.getLogPath();
            if (StringUtils.isEmpty(taskInstance.getHost()) || StringUtils.isEmpty(taskLogPath)) {
                continue;
            }
            logClient.removeTaskLog(Host.of(taskInstance.getHost()), taskLogPath);
        }
    }

    /**
     * recursive query sub process definition id by parent id.
     *
     * @param parentCode parentCode
     * @param ids        ids
     */
    @Override
    public void recurseFindSubProcess(long parentCode, List<Long> ids) {
        List<TaskDefinition> taskNodeList = taskDefinitionDao.getTaskDefinitionListByDefinition(parentCode);

        if (taskNodeList != null && !taskNodeList.isEmpty()) {

            for (TaskDefinition taskNode : taskNodeList) {
                String parameter = taskNode.getTaskParams();
                ObjectNode parameterJson = JSONUtils.parseObject(parameter);
                if (parameterJson.get(CMD_PARAM_SUB_PROCESS_DEFINE_CODE) != null) {
                    SubProcessParameters subProcessParam = JSONUtils.parseObject(parameter, SubProcessParameters.class);
                    ids.add(subProcessParam.getProcessDefinitionCode());
                    recurseFindSubProcess(subProcessParam.getProcessDefinitionCode(), ids);
                }
            }
        }
    }

    /**
     * get schedule time from command
     *
     * @param command  command
     * @param cmdParam cmdParam map
     * @return date
     */
    private Date getScheduleTime(Command command, Map<String, String> cmdParam) throws CronParseException {
        Date scheduleTime = command.getScheduleTime();
        if (scheduleTime == null && cmdParam != null && cmdParam.containsKey(CMD_PARAM_COMPLEMENT_DATA_START_DATE)) {

            Date start = DateUtils.stringToDate(cmdParam.get(CMD_PARAM_COMPLEMENT_DATA_START_DATE));
            Date end = DateUtils.stringToDate(cmdParam.get(CMD_PARAM_COMPLEMENT_DATA_END_DATE));
            List<Schedule> schedules =
                    queryReleaseSchedulerListByProcessDefinitionCode(command.getProcessDefinitionCode());
            List<Date> complementDateList = CronUtils.getSelfFireDateList(start, end, schedules);

            if (CollectionUtils.isNotEmpty(complementDateList)) {
                scheduleTime = complementDateList.get(0);
            } else {
                log.error("set scheduler time error: complement date list is empty, command: {}",
                        command.toString());
            }
        }
        return scheduleTime;
    }

    /**
     * generate a new work process instance from command.
     *
     * @param processDefinition processDefinition
     * @param command           command
     * @param cmdParam          cmdParam map
     * @return process instance
     */
    private ProcessInstance generateNewProcessInstance(ProcessDefinition processDefinition,
                                                       Command command,
                                                       Map<String, String> cmdParam) {
        ProcessInstance processInstance = new ProcessInstance(processDefinition);
        processInstance.setProcessDefinitionCode(processDefinition.getCode());
        processInstance.setProcessDefinitionVersion(processDefinition.getVersion());
        processInstance.setProjectCode(processDefinition.getProjectCode());
        processInstance.setStateWithDesc(WorkflowExecutionStatus.RUNNING_EXECUTION, "init running");
        processInstance.setRecovery(Flag.NO);
        processInstance.setStartTime(new Date());
        // the new process instance restart time is null.
        processInstance.setRestartTime(null);
        processInstance.setRunTimes(1);
        processInstance.setMaxTryTimes(0);
        processInstance.setCommandParam(command.getCommandParam());
        processInstance.setCommandType(command.getCommandType());
        processInstance.setIsSubProcess(Flag.NO);
        processInstance.setTaskDependType(command.getTaskDependType());
        processInstance.setFailureStrategy(command.getFailureStrategy());
        processInstance.setExecutorId(command.getExecutorId());
        processInstance.setExecutorName(Optional.ofNullable(userMapper.selectById(command.getExecutorId()))
                .map(User::getUserName).orElse(null));
        WarningType warningType = command.getWarningType() == null ? WarningType.NONE : command.getWarningType();
        processInstance.setWarningType(warningType);
        Integer warningGroupId = command.getWarningGroupId() == null ? 0 : command.getWarningGroupId();
        processInstance.setWarningGroupId(warningGroupId);
        processInstance.setDryRun(command.getDryRun());
        processInstance.setTestFlag(command.getTestFlag());

        if (command.getScheduleTime() != null) {
            processInstance.setScheduleTime(command.getScheduleTime());
        }
        processInstance.setCommandStartTime(command.getStartTime());
        processInstance.setLocations(processDefinition.getLocations());

        // reset global params while there are start parameters
        setGlobalParamIfCommanded(processDefinition, cmdParam);

        // curing global params
        Map<String, String> commandParamMap = JSONUtils.toMap(command.getCommandParam());
        String timezoneId = null;
        if (commandParamMap != null) {
            timezoneId = commandParamMap.get(Constants.SCHEDULE_TIMEZONE);
        }

        String globalParams = curingGlobalParamsService.curingGlobalParams(processInstance.getId(),
                processDefinition.getGlobalParamMap(),
                processDefinition.getGlobalParamList(),
                getCommandTypeIfComplement(processInstance, command),
                processInstance.getScheduleTime(), timezoneId);
        processInstance.setGlobalParams(globalParams);

        // set process instance priority
        processInstance.setProcessInstancePriority(command.getProcessInstancePriority());
        String workerGroup = StringUtils.defaultIfEmpty(command.getWorkerGroup(), Constants.DEFAULT_WORKER_GROUP);
        processInstance.setWorkerGroup(workerGroup);
        processInstance
                .setEnvironmentCode(Objects.isNull(command.getEnvironmentCode()) ? -1 : command.getEnvironmentCode());
        processInstance.setTimeout(processDefinition.getTimeout());
        processInstance.setTenantCode(command.getTenantCode());
        return processInstance;
    }

    private void setGlobalParamIfCommanded(ProcessDefinition processDefinition, Map<String, String> cmdParam) {
        // get start params from command param
        Map<String, String> startParamMap = new HashMap<>();
        if (cmdParam != null && cmdParam.containsKey(CommandKeyConstants.CMD_PARAM_START_PARAMS)) {
            String startParamJson = cmdParam.get(CommandKeyConstants.CMD_PARAM_START_PARAMS);
            startParamMap = JSONUtils.toMap(startParamJson);
        }
        Map<String, String> fatherParamMap = new HashMap<>();
        if (cmdParam != null && cmdParam.containsKey(CommandKeyConstants.CMD_PARAM_FATHER_PARAMS)) {
            String fatherParamJson = cmdParam.get(CommandKeyConstants.CMD_PARAM_FATHER_PARAMS);
            fatherParamMap = JSONUtils.toMap(fatherParamJson);
        }
        startParamMap.putAll(fatherParamMap);
        // set start param into global params
        Map<String, String> globalMap = processDefinition.getGlobalParamMap();
        List<Property> globalParamList = processDefinition.getGlobalParamList();
        if (MapUtils.isNotEmpty(startParamMap) && globalMap != null) {
            // start param to overwrite global param
            for (Map.Entry<String, String> param : globalMap.entrySet()) {
                String val = startParamMap.get(param.getKey());
                if (val != null) {
                    param.setValue(val);
                }
            }
            // start param to create new global param if global not exist
            for (Entry<String, String> startParam : startParamMap.entrySet()) {
                if (!globalMap.containsKey(startParam.getKey())) {
                    globalMap.put(startParam.getKey(), startParam.getValue());
                    globalParamList.add(new Property(startParam.getKey(), IN, VARCHAR, startParam.getValue()));
                }
            }
        }
    }

    /**
     * Get workflow runtime tenant
     *
     * the workflow provides a tenant and uses the provided tenant;
     * when no tenant is provided or the provided tenant is the default tenant, \
     * the user's tenant created by the workflow is used
     *
     * @param tenantCode tenantCode
     * @param userId   userId
     * @return tenant code
     */
    @Override
    public String getTenantForProcess(String tenantCode, int userId) {
        if (StringUtils.isNoneBlank(tenantCode) && !Constants.DEFAULT.equals(tenantCode)) {
            return tenantCode;
        }

        if (userId == 0) {
            return null;
        }

        User user = userMapper.selectById(userId);
        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        return tenant.getTenantCode();
    }

    /**
     * get an environment
     * use the code of the environment to find a environment.
     *
     * @param environmentCode environmentCode
     * @return Environment
     */
    @Override
    public Environment findEnvironmentByCode(Long environmentCode) {
        Environment environment = null;
        if (environmentCode >= 0) {
            environment = environmentMapper.queryByEnvironmentCode(environmentCode);
        }
        return environment;
    }

    /**
     * check command parameters is valid
     *
     * @param command  command
     * @param cmdParam cmdParam map
     * @return whether command param is valid
     */
    private Boolean checkCmdParam(Command command, Map<String, String> cmdParam) {
        if (command.getTaskDependType() == TaskDependType.TASK_ONLY
                || command.getTaskDependType() == TaskDependType.TASK_PRE) {
            if (cmdParam == null
                    || !cmdParam.containsKey(CommandKeyConstants.CMD_PARAM_START_NODES)
                    || cmdParam.get(CommandKeyConstants.CMD_PARAM_START_NODES).isEmpty()) {
                log.error("command node depend type is {}, but start nodes is null ", command.getTaskDependType());
                return false;
            }
        }
        return true;
    }

    /**
     * construct process instance according to one command.
     *
     * @param command command
     * @param host    host
     * @return process instance
     */
    @Override
    public @Nullable ProcessInstance constructProcessInstance(Command command,
                                                              String host) throws CronParseException, CodeGenerateException {
        ProcessInstance processInstance;
        ProcessDefinition processDefinition;
        CommandType commandType = command.getCommandType();

        processDefinition =
                this.findProcessDefinition(command.getProcessDefinitionCode(), command.getProcessDefinitionVersion());
        if (processDefinition == null) {
            log.error("cannot find the work process define! define code : {}", command.getProcessDefinitionCode());
            throw new IllegalArgumentException("Cannot find the process definition for this workflowInstance");
        }
        Map<String, String> cmdParam = JSONUtils.toMap(command.getCommandParam());
        if (cmdParam == null) {
            cmdParam = new HashMap<>();
        }
        int processInstanceId = command.getProcessInstanceId();
        if (processInstanceId == 0) {
            processInstance = generateNewProcessInstance(processDefinition, command, cmdParam);
        } else {
            processInstance = this.findProcessInstanceDetailById(processInstanceId).orElse(null);
            setGlobalParamIfCommanded(processDefinition, cmdParam);
            if (processInstance == null) {
                return null;
            }
        }

        CommandType commandTypeIfComplement = getCommandTypeIfComplement(processInstance, command);
        // reset global params while repeat running and recover tolerance fault process is needed by cmdParam
        if (commandTypeIfComplement == CommandType.REPEAT_RUNNING ||
                commandTypeIfComplement == CommandType.RECOVER_TOLERANCE_FAULT_PROCESS ||
                commandTypeIfComplement == CommandType.RECOVER_SERIAL_WAIT) {
            setGlobalParamIfCommanded(processDefinition, cmdParam);
        }

        // time zone
        String timezoneId = cmdParam.get(Constants.SCHEDULE_TIMEZONE);

        // Recalculate global parameters after rerun.
        String globalParams = curingGlobalParamsService.curingGlobalParams(processInstance.getId(),
                processDefinition.getGlobalParamMap(),
                processDefinition.getGlobalParamList(),
                commandTypeIfComplement,
                processInstance.getScheduleTime(), timezoneId);
        processInstance.setGlobalParams(globalParams);
        processInstance.setProcessDefinition(processDefinition);

        // reset command parameter
        if (processInstance.getCommandParam() != null) {
            Map<String, String> processCmdParam = JSONUtils.toMap(processInstance.getCommandParam());
            Map<String, String> finalCmdParam = cmdParam;
            processCmdParam.forEach((key, value) -> {
                if (!finalCmdParam.containsKey(key)) {
                    finalCmdParam.put(key, value);
                }
            });
        }
        // reset command parameter if sub process
        if (cmdParam.containsKey(CommandKeyConstants.CMD_PARAM_SUB_PROCESS)) {
            processInstance.setCommandParam(command.getCommandParam());
        }
        if (Boolean.FALSE.equals(checkCmdParam(command, cmdParam))) {
            log.error("command parameter check failed!");
            return null;
        }
        if (command.getScheduleTime() != null) {
            processInstance.setScheduleTime(command.getScheduleTime());
        }
        processInstance.setHost(host);
        processInstance.setRestartTime(new Date());
        WorkflowExecutionStatus runStatus = WorkflowExecutionStatus.RUNNING_EXECUTION;
        int runTime = processInstance.getRunTimes();
        switch (commandType) {
            case START_PROCESS:
            case DYNAMIC_GENERATION:
                break;
            case START_FAILURE_TASK_PROCESS:
                // find failed tasks and init these tasks
                List<Integer> failedList =
                        this.findTaskIdByInstanceState(processInstance.getId(), TaskExecutionStatus.FAILURE);
                List<Integer> toleranceList = this.findTaskIdByInstanceState(processInstance.getId(),
                        TaskExecutionStatus.NEED_FAULT_TOLERANCE);
                List<Integer> killedList =
                        this.findTaskIdByInstanceState(processInstance.getId(), TaskExecutionStatus.KILL);
                cmdParam.remove(CommandKeyConstants.CMD_PARAM_RECOVERY_START_NODE_STRING);

                failedList.addAll(killedList);
                failedList.addAll(toleranceList);
                for (Integer taskId : failedList) {
                    initTaskInstance(taskInstanceDao.queryById(taskId));
                }
                cmdParam.put(CommandKeyConstants.CMD_PARAM_RECOVERY_START_NODE_STRING,
                        String.join(Constants.COMMA, convertIntListToString(failedList)));
                processInstance.setCommandParam(JSONUtils.toJsonString(cmdParam));
                processInstance.setRunTimes(runTime + 1);
                break;
            case START_CURRENT_TASK_PROCESS:
                break;
            case RECOVER_WAITING_THREAD:
                break;
            case RECOVER_SUSPENDED_PROCESS:
                // find pause tasks and init task's state
                cmdParam.remove(CommandKeyConstants.CMD_PARAM_RECOVERY_START_NODE_STRING);
                List<Integer> stopNodeList = findTaskIdByInstanceState(processInstance.getId(),
                        TaskExecutionStatus.KILL);
                for (Integer taskId : stopNodeList) {
                    // initialize the pause state
                    initTaskInstance(taskInstanceDao.queryById(taskId));
                }
                cmdParam.put(CommandKeyConstants.CMD_PARAM_RECOVERY_START_NODE_STRING,
                        String.join(Constants.COMMA, convertIntListToString(stopNodeList)));
                processInstance.setCommandParam(JSONUtils.toJsonString(cmdParam));
                processInstance.setRunTimes(runTime + 1);
                break;
            case RECOVER_TOLERANCE_FAULT_PROCESS:
                // recover tolerance fault process
                processInstance.setRecovery(Flag.YES);
                processInstance.setRunTimes(runTime + 1);
                runStatus = processInstance.getState();
                break;
            case COMPLEMENT_DATA:
                // delete all the valid tasks when complement data if id is not null
                if (processInstance.getId() != null) {
                    List<TaskInstance> taskInstanceList =
                            taskInstanceDao.queryValidTaskListByWorkflowInstanceId(processInstance.getId(),
                                    processInstance.getTestFlag());
                    for (TaskInstance taskInstance : taskInstanceList) {
                        taskInstance.setFlag(Flag.NO);
                        taskInstanceDao.updateById(taskInstance);
                    }
                }
                break;
            case REPEAT_RUNNING:
                // delete the recover task names from command parameter
                if (cmdParam.containsKey(CommandKeyConstants.CMD_PARAM_RECOVERY_START_NODE_STRING)) {
                    cmdParam.remove(CommandKeyConstants.CMD_PARAM_RECOVERY_START_NODE_STRING);
                    processInstance.setCommandParam(JSONUtils.toJsonString(cmdParam));
                }
                // delete the StartNodeList from command parameter if last execution is only execute specified tasks
                if (processInstance.getCommandType().equals(CommandType.EXECUTE_TASK)) {
                    cmdParam.remove(CommandKeyConstants.CMD_PARAM_START_NODES);
                    processInstance.setCommandParam(JSONUtils.toJsonString(cmdParam));
                    processInstance.setTaskDependType(command.getTaskDependType());
                }
                // delete all the valid tasks when repeat running
                List<TaskInstance> validTaskList =
                        taskInstanceDao.queryValidTaskListByWorkflowInstanceId(processInstance.getId(),
                                processInstance.getTestFlag());
                for (TaskInstance taskInstance : validTaskList) {
                    taskInstance.setFlag(Flag.NO);
                    taskInstanceDao.updateById(taskInstance);
                }
                processInstance.setStartTime(new Date());
                processInstance.setRestartTime(processInstance.getStartTime());
                processInstance.setEndTime(null);
                processInstance.setRunTimes(runTime + 1);
                initComplementDataParam(processDefinition, processInstance, cmdParam);
                break;
            case SCHEDULER:
                break;
            case EXECUTE_TASK:
                processInstance.setRunTimes(runTime + 1);
                processInstance.setTaskDependType(command.getTaskDependType());
                processInstance.setCommandParam(JSONUtils.toJsonString(cmdParam));
                break;
            default:
                break;
        }
        processInstance.setStateWithDesc(runStatus, commandType.getDescp());
        return processInstance;
    }

    /**
     * get process definition by command
     * If it is a fault-tolerant command, get the specified version of ProcessDefinition through ProcessInstance
     * Otherwise, get the latest version of ProcessDefinition
     *
     * @return ProcessDefinition
     */
    private @Nullable ProcessDefinition getProcessDefinitionByCommand(long processDefinitionCode,
                                                                      Map<String, String> cmdParam) {
        if (cmdParam != null) {
            int processInstanceId = 0;
            if (cmdParam.containsKey(CommandKeyConstants.CMD_PARAM_RECOVER_PROCESS_ID_STRING)) {
                processInstanceId =
                        Integer.parseInt(cmdParam.get(CommandKeyConstants.CMD_PARAM_RECOVER_PROCESS_ID_STRING));
            } else if (cmdParam.containsKey(CommandKeyConstants.CMD_PARAM_SUB_PROCESS)) {
                processInstanceId = Integer.parseInt(cmdParam.get(CommandKeyConstants.CMD_PARAM_SUB_PROCESS));
            } else if (cmdParam.containsKey(CommandKeyConstants.CMD_PARAM_RECOVERY_WAITING_THREAD)) {
                processInstanceId =
                        Integer.parseInt(cmdParam.get(CommandKeyConstants.CMD_PARAM_RECOVERY_WAITING_THREAD));
            }

            if (processInstanceId != 0) {
                ProcessInstance processInstance = this.findProcessInstanceDetailById(processInstanceId).orElse(null);
                if (processInstance == null) {
                    return null;
                }

                return processDefineLogMapper.queryByDefinitionCodeAndVersion(
                        processInstance.getProcessDefinitionCode(), processInstance.getProcessDefinitionVersion());
            }
        }

        return processDefineMapper.queryByCode(processDefinitionCode);
    }

    /**
     * return complement data if the process start with complement data
     *
     * @param processInstance processInstance
     * @param command         command
     * @return command type
     */
    private CommandType getCommandTypeIfComplement(ProcessInstance processInstance, Command command) {
        if (CommandType.COMPLEMENT_DATA == processInstance.getCmdTypeIfComplement()) {
            return CommandType.COMPLEMENT_DATA;
        } else {
            return command.getCommandType();
        }
    }

    /**
     * initialize complement data parameters
     *
     * @param processDefinition processDefinition
     * @param processInstance   processInstance
     * @param cmdParam          cmdParam
     */
    private void initComplementDataParam(ProcessDefinition processDefinition,
                                         ProcessInstance processInstance,
                                         Map<String, String> cmdParam) throws CronParseException {
        if (!processInstance.isComplementData()) {
            return;
        }

        Date start = DateUtils.stringToDate(cmdParam.get(CMD_PARAM_COMPLEMENT_DATA_START_DATE));
        Date end = DateUtils.stringToDate(cmdParam.get(CMD_PARAM_COMPLEMENT_DATA_END_DATE));
        List<Date> complementDate = Lists.newLinkedList();
        if (start != null && end != null) {
            List<Schedule> listSchedules =
                    queryReleaseSchedulerListByProcessDefinitionCode(processInstance.getProcessDefinitionCode());
            complementDate = CronUtils.getSelfFireDateList(start, end, listSchedules);
        }
        if (cmdParam.containsKey(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST)) {
            complementDate = CronUtils.getSelfScheduleDateList(cmdParam);
        }

        if (CollectionUtils.isNotEmpty(complementDate) && Flag.NO == processInstance.getIsSubProcess()) {
            processInstance.setScheduleTime(complementDate.get(0));
        }

        // time zone
        String timezoneId = cmdParam.get(Constants.SCHEDULE_TIMEZONE);

        String globalParams = curingGlobalParamsService.curingGlobalParams(processInstance.getId(),
                processDefinition.getGlobalParamMap(),
                processDefinition.getGlobalParamList(),
                CommandType.COMPLEMENT_DATA, processInstance.getScheduleTime(), timezoneId);
        processInstance.setGlobalParams(globalParams);
    }

    /**
     * set sub work process parameters.
     * handle sub work process instance, update relation table and command parameters
     * set sub work process flag, extends parent work process command parameters
     *
     * @param subProcessInstance subProcessInstance
     */
    @Override
    public void setSubProcessParam(ProcessInstance subProcessInstance) {
        String cmdParam = subProcessInstance.getCommandParam();
        if (Strings.isNullOrEmpty(cmdParam)) {
            return;
        }
        Map<String, String> paramMap = JSONUtils.toMap(cmdParam);
        // write sub process id into cmd param.
        if (paramMap.containsKey(CMD_PARAM_SUB_PROCESS)
                && CMD_PARAM_EMPTY_SUB_PROCESS.equals(paramMap.get(CMD_PARAM_SUB_PROCESS))) {
            paramMap.remove(CMD_PARAM_SUB_PROCESS);
            paramMap.put(CMD_PARAM_SUB_PROCESS, String.valueOf(subProcessInstance.getId()));
            subProcessInstance.setCommandParam(JSONUtils.toJsonString(paramMap));
            subProcessInstance.setIsSubProcess(Flag.YES);
            processInstanceDao.upsertProcessInstance(subProcessInstance);
        }
        // copy parent instance user def params to sub process..
        String parentInstanceId = paramMap.get(CMD_PARAM_SUB_PROCESS_PARENT_INSTANCE_ID);
        if (!Strings.isNullOrEmpty(parentInstanceId)) {
            ProcessInstance parentInstance =
                    findProcessInstanceDetailById(Integer.parseInt(parentInstanceId)).orElse(null);
            if (parentInstance != null) {
                subProcessInstance.setGlobalParams(
                        joinGlobalParams(parentInstance.getGlobalParams(), subProcessInstance.getGlobalParams()));
                subProcessInstance
                        .setVarPool(joinVarPool(parentInstance.getVarPool(), subProcessInstance.getVarPool()));
                processInstanceDao.upsertProcessInstance(subProcessInstance);
            } else {
                log.error("sub process command params error, cannot find parent instance: {} ", cmdParam);
            }
        }
        ProcessInstanceMap processInstanceMap = JSONUtils.parseObject(cmdParam, ProcessInstanceMap.class);
        if (processInstanceMap == null || processInstanceMap.getParentProcessInstanceId() == 0) {
            return;
        }
        // update sub process id to process map table
        processInstanceMap.setProcessInstanceId(subProcessInstance.getId());

        processInstanceMapDao.updateById(processInstanceMap);
    }

    /**
     * join parent global params into sub process.
     * only the keys doesn't in sub process global would be joined.
     *
     * @param parentGlobalParams parentGlobalParams
     * @param subGlobalParams    subGlobalParams
     * @return global params join
     */
    private String joinGlobalParams(String parentGlobalParams, String subGlobalParams) {

        // Since JSONUtils.toList return unmodified list, we need to creat a new List here.
        List<Property> parentParams = Lists.newArrayList(JSONUtils.toList(parentGlobalParams, Property.class));
        List<Property> subParams = JSONUtils.toList(subGlobalParams, Property.class);

        Set<String> parentParamKeys = parentParams.stream().map(Property::getProp).collect(toSet());

        // We will combine the params of parent workflow and sub workflow
        // If the params are defined in both, we will use parent's params to override the sub workflow(ISSUE-7962)
        // todo: Do we need to consider the other attribute of Property?
        // e.g. the subProp's type is not equals with parent, or subProp's direct is not equals with parent
        // It's suggested to add node name in property, this kind of problem can be solved.
        List<Property> extraSubParams = subParams.stream()
                .filter(subProp -> !parentParamKeys.contains(subProp.getProp())).collect(Collectors.toList());
        parentParams.addAll(extraSubParams);
        return JSONUtils.toJsonString(parentParams);
    }

    /**
     * join parent var pool params into sub process.
     * only the keys doesn't in sub process global would be joined.
     *
     * @param parentValPool
     * @param subValPool
     * @return
     */
    private String joinVarPool(String parentValPool, String subValPool) {
        List<Property> parentValPools = Lists.newArrayList(JSONUtils.toList(parentValPool, Property.class));
        parentValPools = parentValPools.stream().filter(valPool -> valPool.getDirect() == Direct.OUT)
                .collect(Collectors.toList());

        List<Property> subValPools = Lists.newArrayList(JSONUtils.toList(subValPool, Property.class));

        Set<String> parentValPoolKeys = parentValPools.stream().map(Property::getProp).collect(toSet());
        List<Property> extraSubValPools = subValPools.stream().filter(sub -> !parentValPoolKeys.contains(sub.getProp()))
                .collect(Collectors.toList());
        parentValPools.addAll(extraSubValPools);
        return JSONUtils.toJsonString(parentValPools);
    }

    /**
     * initialize task instance
     *
     * @param taskInstance taskInstance
     */
    private void initTaskInstance(TaskInstance taskInstance) {

        if (!taskInstance.isSubProcess()
                && (taskInstance.getState().isKill() || taskInstance.getState().isFailure())) {
            taskInstance.setFlag(Flag.NO);
            taskInstanceDao.updateById(taskInstance);
            return;
        }
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        taskInstanceDao.updateById(taskInstance);
    }

    /**
     * retry submit task to db
     */
    @Override
    public boolean submitTaskWithRetry(ProcessInstance processInstance, TaskInstance taskInstance,
                                       int commitRetryTimes, long commitInterval) {
        int retryTimes = 1;
        while (retryTimes <= commitRetryTimes) {
            try {
                // submit task to db
                // Only want to use transaction here
                if (submitTask(processInstance, taskInstance)) {
                    return true;
                }
                log.error(
                        "task commit to db failed , taskCode: {} has already retry {} times, please check the database",
                        taskInstance.getTaskCode(),
                        retryTimes);
                Thread.sleep(commitInterval);
            } catch (Exception e) {
                log.error("task commit to db failed", e);
            } finally {
                retryTimes += 1;
            }
        }
        return false;
    }

    /**
     * // todo: This method need to refactor, we find when the db down, but the taskInstanceId is not 0. It's better to change to void, rather than return TaskInstance
     * submit task to db
     * submit sub process to command
     *
     * @param processInstance processInstance
     * @param taskInstance    taskInstance
     * @return task instance
     */
    @Override
    @Transactional
    public boolean submitTask(ProcessInstance processInstance, TaskInstance taskInstance) {
        log.info("Start save taskInstance to database : {}, processInstance id:{}, state: {}",
                taskInstance.getName(),
                taskInstance.getProcessInstanceId(),
                processInstance.getState());
        // submit to db
        if (!taskInstanceDao.submitTaskInstanceToDB(taskInstance, processInstance)) {
            log.error("Save taskInstance to db error, task name:{}, process id:{} state: {} ",
                    taskInstance.getName(),
                    taskInstance.getProcessInstance().getId(),
                    processInstance.getState());
            return false;
        }

        if (!taskInstance.getState().isFinished()) {
            createSubWorkProcess(processInstance, taskInstance);
        }

        log.info(
                "End save taskInstance to db successfully:{}, taskInstanceName: {}, taskInstance state:{}, processInstanceId:{}, processInstanceState: {}",
                taskInstance.getId(),
                taskInstance.getName(),
                taskInstance.getState(),
                processInstance.getId(),
                processInstance.getState());
        return true;
    }

    /**
     * set work process instance map
     * consider o
     * repeat running  does not generate new sub process instance
     * set map {parent instance id, task instance id, 0(child instance id)}
     *
     * @param parentInstance parentInstance
     * @param parentTask     parentTask
     * @param processMap     processMap
     * @return process instance map
     */
    private ProcessInstanceMap setProcessInstanceMap(ProcessInstance parentInstance, TaskInstance parentTask,
                                                     ProcessInstanceMap processMap) {
        if (processMap != null) {
            return processMap;
        }
        if (parentInstance.getCommandType() == CommandType.REPEAT_RUNNING) {
            // update current task id to map
            processMap = findPreviousTaskProcessMap(parentInstance, parentTask);
            if (processMap != null) {
                processMap.setParentTaskInstanceId(parentTask.getId());
                processInstanceMapDao.updateById(processMap);
                return processMap;
            }
        }
        // new task
        processMap = new ProcessInstanceMap();
        processMap.setParentProcessInstanceId(parentInstance.getId());
        processMap.setParentTaskInstanceId(parentTask.getId());
        processInstanceMapDao.insert(processMap);
        return processMap;
    }

    /**
     * find previous task work process map.
     *
     * @param parentProcessInstance parentProcessInstance
     * @param parentTask            parentTask
     * @return process instance map
     */
    private ProcessInstanceMap findPreviousTaskProcessMap(ProcessInstance parentProcessInstance,
                                                          TaskInstance parentTask) {

        Integer preTaskId = 0;
        List<TaskInstance> preTaskList =
                taskInstanceDao.queryPreviousTaskListByWorkflowInstanceId(parentProcessInstance.getId());
        for (TaskInstance task : preTaskList) {
            if (task.getName().equals(parentTask.getName())) {
                preTaskId = task.getId();
                ProcessInstanceMap map =
                        processInstanceMapDao.queryWorkProcessMapByParent(parentProcessInstance.getId(), preTaskId);
                if (map != null) {
                    return map;
                }
            }
        }
        log.info("sub process instance is not found,parent task:{},parent instance:{}",
                parentTask.getId(), parentProcessInstance.getId());
        return null;
    }

    /**
     * create sub work process command
     *
     * @param parentProcessInstance parentProcessInstance
     * @param task                  task
     */
    @Override
    public void createSubWorkProcess(ProcessInstance parentProcessInstance, TaskInstance task) {
        if (!task.isSubProcess()) {
            return;
        }
        // check create sub work flow firstly
        ProcessInstanceMap instanceMap =
                processInstanceMapDao.queryWorkProcessMapByParent(parentProcessInstance.getId(), task.getId());
        if (null != instanceMap
                && CommandType.RECOVER_TOLERANCE_FAULT_PROCESS == parentProcessInstance.getCommandType()) {
            // recover failover tolerance would not create a new command when the sub command already have been created
            return;
        }
        instanceMap = setProcessInstanceMap(parentProcessInstance, task, instanceMap);
        ProcessInstance childInstance = null;
        if (instanceMap.getProcessInstanceId() != 0) {
            childInstance = findProcessInstanceById(instanceMap.getProcessInstanceId());
        }
        if (childInstance != null && childInstance.getState() == WorkflowExecutionStatus.SUCCESS
                && CommandType.START_FAILURE_TASK_PROCESS == parentProcessInstance.getCommandType()) {
            log.info("sub process instance {} status is success, so skip creating command", childInstance.getId());
            return;
        }
        Command subProcessCommand =
                commandService.createSubProcessCommand(parentProcessInstance, childInstance, instanceMap, task);
        if (subProcessCommand == null) {
            log.error("create sub process command failed, so skip creating command");
            return;
        }
        updateSubProcessDefinitionByParent(parentProcessInstance, subProcessCommand.getProcessDefinitionCode());
        initSubInstanceState(childInstance);
        commandService.createCommand(subProcessCommand);
        log.info("sub process command created: {} ", subProcessCommand);
    }

    /**
     * initialize sub work flow state
     * child instance state would be initialized when 'recovery from pause/stop/failure'
     */
    private void initSubInstanceState(ProcessInstance childInstance) {
        if (childInstance != null) {
            childInstance.setStateWithDesc(WorkflowExecutionStatus.RUNNING_EXECUTION, "init sub workflow instance");
            processInstanceDao.updateById(childInstance);
        }
    }

    /**
     * get sub work flow command type
     * child instance exist: child command = fatherCommand
     * child instance not exists: child command = fatherCommand[0]
     */
    private CommandType getSubCommandType(ProcessInstance parentProcessInstance, ProcessInstance childInstance) {
        CommandType commandType = parentProcessInstance.getCommandType();
        if (childInstance == null) {
            String fatherHistoryCommand = parentProcessInstance.getHistoryCmd();
            commandType = CommandType.valueOf(fatherHistoryCommand.split(Constants.COMMA)[0]);
        }
        return commandType;
    }

    /**
     * update sub process definition
     *
     * @param parentProcessInstance parentProcessInstance
     * @param childDefinitionCode   childDefinitionId
     */
    private void updateSubProcessDefinitionByParent(ProcessInstance parentProcessInstance, long childDefinitionCode) {
        ProcessDefinition fatherDefinition =
                this.findProcessDefinition(parentProcessInstance.getProcessDefinitionCode(),
                        parentProcessInstance.getProcessDefinitionVersion());
        ProcessDefinition childDefinition = this.findProcessDefinitionByCode(childDefinitionCode);
        if (childDefinition != null && fatherDefinition != null) {
            childDefinition.setWarningGroupId(fatherDefinition.getWarningGroupId());
            processDefineMapper.updateById(childDefinition);
        }
    }

    /**
     * package task instance
     */
    @Override
    public void packageTaskInstance(TaskInstance taskInstance, ProcessInstance processInstance) {
        taskInstance.setProcessInstance(processInstance);
        taskInstance.setProcessDefine(processInstance.getProcessDefinition());
        taskInstance.setProcessInstancePriority(processInstance.getProcessInstancePriority());
        TaskDefinition taskDefinition = taskDefinitionDao.findTaskDefinition(
                taskInstance.getTaskCode(),
                taskInstance.getTaskDefinitionVersion());
        this.updateTaskDefinitionResources(taskDefinition);
        taskInstance.setTaskDefine(taskDefinition);
        taskInstance.setTestFlag(processInstance.getTestFlag());
    }

    /**
     * Update {@link ResourceInfo} information in {@link TaskDefinition}
     *
     * @param taskDefinition the given {@link TaskDefinition}
     */
    @Override
    public void updateTaskDefinitionResources(TaskDefinition taskDefinition) {
        Map<String, Object> taskParameters = JSONUtils.parseObject(
                taskDefinition.getTaskParams(),
                new TypeReference<Map<String, Object>>() {
                });
        if (taskParameters != null) {
            // if contains mainJar field, query resource from database
            // Flink, Spark, MR
            if (taskParameters.containsKey("mainJar")) {
                Object mainJarObj = taskParameters.get("mainJar");
                ResourceInfo mainJar = JSONUtils.parseObject(
                        JSONUtils.toJsonString(mainJarObj),
                        ResourceInfo.class);
                ResourceInfo resourceInfo =
                        updateResourceInfo(taskDefinitionMapper.queryByCode(taskDefinition.getCode()).getId(), mainJar);
                if (resourceInfo != null) {
                    taskParameters.put("mainJar", resourceInfo);
                }
            }
            // update resourceList information
            if (taskParameters.containsKey("resourceList")) {
                String resourceListStr = JSONUtils.toJsonString(taskParameters.get("resourceList"));
                List<ResourceInfo> resourceInfos = JSONUtils.toList(resourceListStr, ResourceInfo.class);
                List<ResourceInfo> updatedResourceInfos = resourceInfos
                        .stream()
                        .map(resourceInfo -> updateResourceInfo(
                                taskDefinitionMapper.queryByCode(taskDefinition.getCode()).getId(), resourceInfo))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                taskParameters.put("resourceList", updatedResourceInfos);
            }
            // set task parameters
            taskDefinition.setTaskParams(JSONUtils.toJsonString(taskParameters));
        }
    }

    /**
     * update {@link ResourceInfo} by given original ResourceInfo
     *
     * @param res origin resource info
     * @return {@link ResourceInfo}
     */
    protected ResourceInfo updateResourceInfo(int task_id, ResourceInfo res) {
        ResourceInfo resourceInfo = null;
        // only if mainJar is not null and does not contain "resourceName" field
        if (res != null) {
            String resourceFullName = res.getResourceName();
            if (StringUtils.isBlank(resourceFullName)) {
                log.error("invalid resource full name, {}", resourceFullName);
                return new ResourceInfo();
            }
            resourceInfo = new ResourceInfo();
            resourceInfo.setId(-1);
            resourceInfo.setResourceName(resourceFullName);
            log.info("updated resource info {}",
                    JSONUtils.toJsonString(resourceInfo));
        }
        return resourceInfo;
    }

    /**
     * get id list by task state
     *
     * @param instanceId instanceId
     * @param state      state
     * @return task instance states
     */
    @Override
    public List<Integer> findTaskIdByInstanceState(int instanceId, TaskExecutionStatus state) {
        return taskInstanceMapper.queryTaskByProcessIdAndState(instanceId, state.getCode());
    }

    /**
     * delete work process map by parent process id
     *
     * @param parentWorkProcessId parentWorkProcessId
     * @return delete process map result
     */
    @Override
    public int deleteWorkProcessMapByParentId(int parentWorkProcessId) {
        return processInstanceMapMapper.deleteByParentProcessId(parentWorkProcessId);

    }

    /**
     * find sub process instance
     *
     * @param parentProcessId parentProcessId
     * @param parentTaskId    parentTaskId
     * @return process instance
     */
    @Override
    public ProcessInstance findSubProcessInstance(Integer parentProcessId, Integer parentTaskId) {
        ProcessInstance processInstance = null;
        ProcessInstanceMap processInstanceMap = processInstanceMapMapper.queryByParentId(parentProcessId, parentTaskId);
        if (processInstanceMap == null || processInstanceMap.getProcessInstanceId() == 0) {
            return processInstance;
        }
        processInstance = findProcessInstanceById(processInstanceMap.getProcessInstanceId());
        return processInstance;
    }

    /**
     * find parent process instance
     *
     * @param subProcessId subProcessId
     * @return process instance
     */
    @Override
    public ProcessInstance findParentProcessInstance(Integer subProcessId) {
        ProcessInstance processInstance = null;
        ProcessInstanceMap processInstanceMap = processInstanceMapMapper.queryBySubProcessId(subProcessId);
        if (processInstanceMap == null || processInstanceMap.getProcessInstanceId() == 0) {
            return processInstance;
        }
        processInstance = findProcessInstanceById(processInstanceMap.getParentProcessInstanceId());
        return processInstance;
    }

    /**
     * for show in page of taskInstance
     */
    @Override
    public void changeOutParam(TaskInstance taskInstance) {
        if (Strings.isNullOrEmpty(taskInstance.getVarPool())) {
            return;
        }
        List<Property> properties = JSONUtils.toList(taskInstance.getVarPool(), Property.class);
        if (CollectionUtils.isEmpty(properties)) {
            return;
        }
        // if the result more than one line,just get the first .
        Map<String, Object> taskParams =
                JSONUtils.parseObject(taskInstance.getTaskParams(), new TypeReference<Map<String, Object>>() {
                });
        Object localParams = taskParams.get(LOCAL_PARAMS);
        if (localParams == null) {
            return;
        }
        List<Property> allParam = JSONUtils.toList(JSONUtils.toJsonString(localParams), Property.class);
        Map<String, String> outProperty = new HashMap<>();
        for (Property info : properties) {
            if (info.getDirect() == Direct.OUT) {
                outProperty.put(info.getProp(), info.getValue());
            }
        }
        for (Property info : allParam) {
            if (info.getDirect() == Direct.OUT) {
                String paramName = info.getProp();
                info.setValue(outProperty.get(paramName));
            }
        }
        taskParams.put(LOCAL_PARAMS, allParam);
        taskInstance.setTaskParams(JSONUtils.toJsonString(taskParams));
    }

    /**
     * convert integer list to string list
     *
     * @param intList intList
     * @return string list
     */
    private List<String> convertIntListToString(List<Integer> intList) {
        if (intList == null) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>(intList.size());
        for (Integer intVar : intList) {
            result.add(String.valueOf(intVar));
        }
        return result;
    }

    /**
     * query schedule by id
     *
     * @param id id
     * @return schedule
     */
    @Override
    public Schedule querySchedule(int id) {
        return scheduleMapper.selectById(id);
    }

    /**
     * query Schedule by processDefinitionCode
     *
     * @param processDefinitionCode processDefinitionCode
     * @see Schedule
     */
    @Override
    public List<Schedule> queryReleaseSchedulerListByProcessDefinitionCode(long processDefinitionCode) {
        return scheduleMapper.queryReleaseSchedulerListByProcessDefinitionCode(processDefinitionCode);
    }

    /**
     * query dependent process definition by process definition code
     *
     * @param processDefinitionCode processDefinitionCode
     * @see DependentProcessDefinition
     */
    @Override
    public List<DependentProcessDefinition> queryDependentProcessDefinitionByProcessDefinitionCode(long processDefinitionCode) {
        return workFlowLineageMapper.queryDependentProcessDefinitionByProcessDefinitionCode(processDefinitionCode);
    }

    /**
     * query need failover process instance
     *
     * @param host host
     * @return process instance list
     */
    @Override
    public List<ProcessInstance> queryNeedFailoverProcessInstances(String host) {
        return processInstanceMapper.queryByHostAndStatus(host,
                WorkflowExecutionStatus.getNeedFailoverWorkflowInstanceState());
    }

    @Override
    public List<String> queryNeedFailoverProcessInstanceHost() {
        return processInstanceMapper
                .queryNeedFailoverProcessInstanceHost(WorkflowExecutionStatus.getNeedFailoverWorkflowInstanceState());
    }

    /**
     * process need failover process instance
     *
     * @param processInstance processInstance
     */
    @Override
    @Transactional
    public void processNeedFailoverProcessInstances(ProcessInstance processInstance) {
        // updateProcessInstance host is null to mark this processInstance has been failover
        // and insert a failover command
        processInstance.setHost(Constants.NULL);
        processInstanceMapper.updateById(processInstance);

        // 2 insert into recover command
        Command cmd = new Command();
        cmd.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        cmd.setProcessDefinitionVersion(processInstance.getProcessDefinitionVersion());
        cmd.setProcessInstanceId(processInstance.getId());
        cmd.setCommandParam(JSONUtils.toJsonString(createCommandParams(processInstance)));
        cmd.setExecutorId(processInstance.getExecutorId());
        cmd.setCommandType(CommandType.RECOVER_TOLERANCE_FAULT_PROCESS);
        cmd.setProcessInstancePriority(processInstance.getProcessInstancePriority());
        cmd.setTestFlag(processInstance.getTestFlag());
        commandService.createCommand(cmd);
    }

    /**
     * find data source by id
     *
     * @param id id
     * @return datasource
     */
    @Override
    public DataSource findDataSourceById(int id) {
        return dataSourceMapper.selectById(id);
    }

    /**
     * find process instance by the task id
     *
     * @param taskId taskId
     * @return process instance
     */
    @Override
    public ProcessInstance findProcessInstanceByTaskId(int taskId) {
        TaskInstance taskInstance = taskInstanceMapper.selectById(taskId);
        if (taskInstance != null) {
            return processInstanceMapper.selectById(taskInstance.getProcessInstanceId());
        }
        return null;
    }

    /**
     * find udf function list by id list string
     *
     * @param ids ids
     * @return udf function list
     */
    @Override
    public List<UdfFunc> queryUdfFunListByIds(Integer[] ids) {
        return udfFuncMapper.queryUdfByIdStr(ids, null);
    }

    /**
     * find tenant code by resource name
     *
     * @param resName      resource name
     * @param resourceType resource type
     * @return tenant code
     */
    @Override
    public String queryTenantCodeByResName(String resName, ResourceType resourceType) {
        // in order to query tenant code successful although the version is older
        String fullName = resName.startsWith("/") ? resName : String.format("/%s", resName);

        List<Resource> resourceList = resourceMapper.queryResource(fullName, resourceType.ordinal());
        if (CollectionUtils.isEmpty(resourceList)) {
            return "";
        }
        int userId = resourceList.get(0).getUserId();
        User user = userMapper.selectById(userId);
        if (Objects.isNull(user)) {
            return "";
        }
        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (Objects.isNull(tenant)) {
            return "";
        }
        return tenant.getTenantCode();
    }

    /**
     * find schedule list by process define codes.
     *
     * @param codes codes
     * @return schedule list
     */
    @Override
    public List<Schedule> selectAllByProcessDefineCode(long[] codes) {
        return scheduleMapper.selectAllByProcessDefineArray(codes);
    }

    /**
     * query user queue by process instance
     *
     * @param processInstance processInstance
     * @return queue
     */
    @Override
    public String queryUserQueueByProcessInstance(ProcessInstance processInstance) {

        String queue = "";
        if (processInstance == null) {
            return queue;
        }
        User executor = userMapper.selectById(processInstance.getExecutorId());
        if (executor != null) {
            queue = executor.getQueue();
        }
        return queue;
    }

    /**
     * query project name and user name by processInstanceId.
     *
     * @param processInstanceId processInstanceId
     * @return projectName and userName
     */
    @Override
    public ProjectUser queryProjectWithUserByProcessInstanceId(int processInstanceId) {
        return projectMapper.queryProjectWithUserByProcessInstanceId(processInstanceId);
    }

    /**
     * get have perm project list
     *
     * @param userId userId
     * @return project list
     */
    @Override
    public List<Project> getProjectListHavePerm(int userId) {
        List<Project> createProjects = projectMapper.queryProjectCreatedByUser(userId);
        List<Project> authedProjects = projectMapper.queryAuthedProjectListByUserId(userId);

        if (createProjects == null) {
            createProjects = new ArrayList<>();
        }

        if (authedProjects != null) {
            createProjects.addAll(authedProjects);
        }
        return createProjects;
    }

    /**
     * list unauthorized udf function
     *
     * @param userId     user id
     * @param needChecks data source id array
     * @return unauthorized udf function list
     */
    @Override
    public <T> List<T> listUnauthorized(int userId, T[] needChecks, AuthorizationType authorizationType) {
        List<T> resultList = new ArrayList<>();

        if (Objects.nonNull(needChecks) && needChecks.length > 0) {
            Set<T> originResSet = new HashSet<>(Arrays.asList(needChecks));

            switch (authorizationType) {
                case RESOURCE_FILE_ID:
                case UDF_FILE:
                    List<Resource> ownUdfResources = resourceMapper.listAuthorizedResourceById(userId, needChecks);
                    addAuthorizedResources(ownUdfResources, userId);
                    Set<Integer> authorizedResourceFiles =
                            ownUdfResources.stream().map(Resource::getId).collect(toSet());
                    originResSet.removeAll(authorizedResourceFiles);
                    break;
                case RESOURCE_FILE_NAME:
                    List<Resource> ownResources = resourceMapper.listAuthorizedResource(userId, needChecks);
                    addAuthorizedResources(ownResources, userId);
                    Set<String> authorizedResources = ownResources.stream().map(Resource::getFullName).collect(toSet());
                    originResSet.removeAll(authorizedResources);
                    break;
                case DATASOURCE:
                    Set<Integer> authorizedDatasources = dataSourceMapper.listAuthorizedDataSource(userId, needChecks)
                            .stream().map(DataSource::getId).collect(toSet());
                    originResSet.removeAll(authorizedDatasources);
                    break;
                case UDF:
                    Set<Integer> authorizedUdfs = udfFuncMapper.listAuthorizedUdfFunc(userId, needChecks).stream()
                            .map(UdfFunc::getId).collect(toSet());
                    originResSet.removeAll(authorizedUdfs);
                    break;
                default:
                    break;
            }

            resultList.addAll(originResSet);
        }

        return resultList;
    }

    /**
     * get user by user id
     *
     * @param userId user id
     * @return User
     */
    @Override
    public User getUserById(int userId) {
        return userMapper.selectById(userId);
    }

    /**
     * get resource by resource id
     *
     * @param resourceId resource id
     * @return Resource
     */
    @Override
    public Resource getResourceById(int resourceId) {
        return resourceMapper.selectById(resourceId);
    }

    /**
     * list resources by ids
     *
     * @param resIds resIds
     * @return resource list
     */
    @Override
    public List<Resource> listResourceByIds(Integer[] resIds) {
        return resourceMapper.listResourceByIds(resIds);
    }

    /**
     * format task app id in task instance
     */
    @Override
    public String formatTaskAppId(TaskInstance taskInstance) {
        ProcessInstance processInstance = findProcessInstanceById(taskInstance.getProcessInstanceId());
        if (processInstance == null) {
            return "";
        }
        ProcessDefinition definition = findProcessDefinition(processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion());
        if (definition == null) {
            return "";
        }
        return String.format("%s_%s_%s", definition.getId(), processInstance.getId(), taskInstance.getId());
    }

    /**
     * switch process definition version to process definition log version
     */
    @Override
    public int switchVersion(ProcessDefinition processDefinition, ProcessDefinitionLog processDefinitionLog) {
        if (null == processDefinition || null == processDefinitionLog) {
            return Constants.DEFINITION_FAILURE;
        }
        processDefinitionLog.setId(processDefinition.getId());
        processDefinitionLog.setReleaseState(ReleaseState.OFFLINE);
        processDefinitionLog.setFlag(Flag.YES);

        int result = processDefineMapper.updateById(processDefinitionLog);
        if (result > 0) {
            result = switchProcessTaskRelationVersion(processDefinitionLog);
            if (result <= 0) {
                return Constants.EXIT_CODE_FAILURE;
            }
        }
        return result;
    }

    @Override
    public int switchProcessTaskRelationVersion(ProcessDefinition processDefinition) {
        List<ProcessTaskRelation> processTaskRelationList = processTaskRelationMapper
                .queryByProcessCode(processDefinition.getProjectCode(), processDefinition.getCode());
        if (!processTaskRelationList.isEmpty()) {
            processTaskRelationMapper.deleteByCode(processDefinition.getProjectCode(), processDefinition.getCode());
        }
        List<ProcessTaskRelation> processTaskRelationListFromLog = processTaskRelationLogMapper
                .queryByProcessCodeAndVersion(processDefinition.getCode(), processDefinition.getVersion()).stream()
                .map(ProcessTaskRelation::new).collect(Collectors.toList());
        int batchInsert = processTaskRelationMapper.batchInsert(processTaskRelationListFromLog);
        if (batchInsert == 0) {
            return Constants.EXIT_CODE_FAILURE;
        } else {
            int result = 0;
            for (ProcessTaskRelation taskRelation : processTaskRelationListFromLog) {
                int switchResult = switchTaskDefinitionVersion(taskRelation.getPostTaskCode(),
                        taskRelation.getPostTaskVersion());
                if (switchResult != Constants.EXIT_CODE_FAILURE) {
                    result++;
                }
            }
            return result;
        }
    }

    @Override
    public int switchTaskDefinitionVersion(long taskCode, int taskVersion) {
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (taskDefinition == null) {
            return Constants.EXIT_CODE_FAILURE;
        }
        if (taskDefinition.getVersion() == taskVersion) {
            return Constants.EXIT_CODE_SUCCESS;
        }
        TaskDefinitionLog taskDefinitionUpdate =
                taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskCode, taskVersion);
        if (taskDefinitionUpdate == null) {
            return Constants.EXIT_CODE_FAILURE;
        }
        taskDefinitionUpdate.setUpdateTime(new Date());
        taskDefinitionUpdate.setId(taskDefinition.getId());
        return taskDefinitionMapper.updateById(taskDefinitionUpdate);
    }

    /**
     * get resource ids
     *
     * @param taskDefinition taskDefinition
     * @return resource ids
     */
    @Override
    public String getResourceIds(TaskDefinition taskDefinition) {
        Set<Integer> resourceIds = null;
        AbstractParameters params = taskPluginManager.getParameters(ParametersNode.builder()
                .taskType(taskDefinition.getTaskType()).taskParams(taskDefinition.getTaskParams()).build());

        if (params != null && CollectionUtils.isNotEmpty(params.getResourceFilesList())) {
            resourceIds = params.getResourceFilesList().stream()
                    .map(ResourceInfo::getId)
                    .filter(Objects::nonNull)
                    .collect(toSet());
        }
        if (CollectionUtils.isEmpty(resourceIds)) {
            return "";
        }
        return Joiner.on(",").join(resourceIds);
    }

    @Override
    public int saveTaskDefine(User operator, long projectCode, List<TaskDefinitionLog> taskDefinitionLogs,
                              Boolean syncDefine) {
        Date now = new Date();
        List<TaskDefinitionLog> newTaskDefinitionLogs = new ArrayList<>();
        List<TaskDefinitionLog> updateTaskDefinitionLogs = new ArrayList<>();
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
            taskDefinitionLog.setProjectCode(projectCode);
            taskDefinitionLog.setUpdateTime(now);
            taskDefinitionLog.setOperateTime(now);
            taskDefinitionLog.setOperator(operator.getId());
            if (taskDefinitionLog.getCode() == 0) {
                taskDefinitionLog.setCode(CodeGenerateUtils.getInstance().genCode());
            }
            if (taskDefinitionLog.getVersion() == 0) {
                // init first version
                taskDefinitionLog.setVersion(Constants.VERSION_FIRST);
            }

            TaskDefinitionLog definitionCodeAndVersion = taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(
                    taskDefinitionLog.getCode(), taskDefinitionLog.getVersion());
            if (definitionCodeAndVersion == null) {
                taskDefinitionLog.setUserId(operator.getId());
                taskDefinitionLog.setCreateTime(now);
                newTaskDefinitionLogs.add(taskDefinitionLog);
                continue;
            }
            if (taskDefinitionLog.equals(definitionCodeAndVersion)) {
                // do nothing if equals
                continue;
            }
            taskDefinitionLog.setUserId(definitionCodeAndVersion.getUserId());
            Integer version = taskDefinitionLogMapper.queryMaxVersionForDefinition(taskDefinitionLog.getCode());
            taskDefinitionLog.setVersion(version + 1);
            taskDefinitionLog.setCreateTime(definitionCodeAndVersion.getCreateTime());
            updateTaskDefinitionLogs.add(taskDefinitionLog);
        }

        if (CollectionUtils.isNotEmpty(updateTaskDefinitionLogs)) {
            List<Long> taskDefinitionCodes = updateTaskDefinitionLogs
                    .stream()
                    .map(TaskDefinition::getCode)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, TaskDefinition> taskDefinitionMap = taskDefinitionMapper.queryByCodeList(taskDefinitionCodes)
                    .stream()
                    .collect(Collectors.toMap(TaskDefinition::getCode, Function.identity()));
            for (TaskDefinitionLog taskDefinitionToUpdate : updateTaskDefinitionLogs) {
                TaskDefinition task = taskDefinitionMap.get(taskDefinitionToUpdate.getCode());
                if (task == null) {
                    newTaskDefinitionLogs.add(taskDefinitionToUpdate);
                } else {
                    taskDefinitionToUpdate.setId(task.getId());
                }
            }
        }

        // for each taskDefinitionLog, we will insert a new version into db
        // and update the origin one if exist
        int updateResult = 0;
        int insertResult = 0;

        // only insert new task definitions if they not in updateTaskDefinitionLogs
        List<TaskDefinitionLog> newInsertTaskDefinitionLogs = newTaskDefinitionLogs.stream()
                .filter(taskDefinitionLog -> !updateTaskDefinitionLogs.contains(taskDefinitionLog))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(newInsertTaskDefinitionLogs)) {
            insertResult = taskDefinitionLogMapper.batchInsert(newInsertTaskDefinitionLogs);
        }
        if (CollectionUtils.isNotEmpty(updateTaskDefinitionLogs)) {
            insertResult += taskDefinitionLogMapper.batchInsert(updateTaskDefinitionLogs);
        }

        if (CollectionUtils.isNotEmpty(newTaskDefinitionLogs) && Boolean.TRUE.equals(syncDefine)) {
            updateResult += taskDefinitionMapper.batchInsert(newTaskDefinitionLogs);
        }
        if (CollectionUtils.isNotEmpty(updateTaskDefinitionLogs) && Boolean.TRUE.equals(syncDefine)) {
            for (TaskDefinitionLog taskDefinitionLog : updateTaskDefinitionLogs) {
                updateResult += taskDefinitionMapper.updateById(taskDefinitionLog);
            }
        }

        return (insertResult & updateResult) > 0 ? 1 : Constants.EXIT_CODE_SUCCESS;
    }

    /**
     * save processDefinition (including create or update processDefinition)
     */
    @Override
    public int saveProcessDefine(User operator, ProcessDefinition processDefinition, Boolean syncDefine,
                                 Boolean isFromProcessDefine) {
        ProcessDefinitionLog processDefinitionLog = new ProcessDefinitionLog(processDefinition);
        Integer version = processDefineLogMapper.queryMaxVersionForDefinition(processDefinition.getCode());
        int insertVersion = version == null || version == 0 ? Constants.VERSION_FIRST : version + 1;
        processDefinitionLog.setVersion(insertVersion);
        processDefinitionLog
                .setReleaseState(!isFromProcessDefine || processDefinitionLog.getReleaseState() == ReleaseState.ONLINE
                        ? ReleaseState.ONLINE
                        : ReleaseState.OFFLINE);
        processDefinitionLog.setOperator(operator.getId());
        processDefinitionLog.setOperateTime(processDefinition.getUpdateTime());
        processDefinitionLog.setId(null);
        int insertLog = processDefineLogMapper.insert(processDefinitionLog);
        int result = 1;
        if (Boolean.TRUE.equals(syncDefine)) {
            if (processDefinition.getId() == null) {
                result = processDefineMapper.insert(processDefinitionLog);
            } else {
                processDefinitionLog.setId(processDefinition.getId());
                result = processDefineMapper.updateById(processDefinitionLog);
            }
        }
        return (insertLog & result) > 0 ? insertVersion : 0;
    }

    /**
     * save task relations
     */
    @Override
    public int saveTaskRelation(User operator, long projectCode, long processDefinitionCode,
                                int processDefinitionVersion,
                                List<ProcessTaskRelationLog> taskRelationList,
                                List<TaskDefinitionLog> taskDefinitionLogs,
                                Boolean syncDefine) {
        if (taskRelationList.isEmpty()) {
            return Constants.EXIT_CODE_SUCCESS;
        }
        Map<Long, TaskDefinitionLog> taskDefinitionLogMap = null;
        if (CollectionUtils.isNotEmpty(taskDefinitionLogs)) {
            taskDefinitionLogMap = taskDefinitionLogs
                    .stream()
                    .collect(Collectors.toMap(TaskDefinition::getCode, taskDefinitionLog -> taskDefinitionLog));
        }
        Date now = new Date();
        for (ProcessTaskRelationLog processTaskRelationLog : taskRelationList) {
            processTaskRelationLog.setProjectCode(projectCode);
            processTaskRelationLog.setProcessDefinitionCode(processDefinitionCode);
            processTaskRelationLog.setProcessDefinitionVersion(processDefinitionVersion);
            if (taskDefinitionLogMap != null) {
                TaskDefinitionLog preTaskDefinitionLog =
                        taskDefinitionLogMap.get(processTaskRelationLog.getPreTaskCode());
                if (preTaskDefinitionLog != null) {
                    processTaskRelationLog.setPreTaskVersion(preTaskDefinitionLog.getVersion());
                }
                TaskDefinitionLog postTaskDefinitionLog =
                        taskDefinitionLogMap.get(processTaskRelationLog.getPostTaskCode());
                if (postTaskDefinitionLog != null) {
                    processTaskRelationLog.setPostTaskVersion(postTaskDefinitionLog.getVersion());
                }
            }
            processTaskRelationLog.setCreateTime(now);
            processTaskRelationLog.setUpdateTime(now);
            processTaskRelationLog.setOperator(operator.getId());
            processTaskRelationLog.setOperateTime(now);
        }
        int insert = taskRelationList.size();
        if (Boolean.TRUE.equals(syncDefine)) {
            List<ProcessTaskRelation> processTaskRelationList =
                    processTaskRelationMapper.queryByProcessCode(projectCode, processDefinitionCode);
            if (!processTaskRelationList.isEmpty()) {
                Set<Integer> processTaskRelationSet =
                        processTaskRelationList.stream().map(ProcessTaskRelation::hashCode).collect(toSet());
                Set<Integer> taskRelationSet =
                        taskRelationList.stream().map(ProcessTaskRelationLog::hashCode).collect(toSet());
                boolean result = CollectionUtils.isEqualCollection(processTaskRelationSet, taskRelationSet);
                if (result) {
                    return Constants.EXIT_CODE_SUCCESS;
                }
                processTaskRelationMapper.deleteByCode(projectCode, processDefinitionCode);
            }
            List<ProcessTaskRelation> processTaskRelations =
                    taskRelationList.stream().map(ProcessTaskRelation::new).collect(Collectors.toList());
            insert = processTaskRelationMapper.batchInsert(processTaskRelations);
        }
        int resultLog = processTaskRelationLogMapper.batchInsert(taskRelationList);
        return (insert & resultLog) > 0 ? Constants.EXIT_CODE_SUCCESS : Constants.EXIT_CODE_FAILURE;
    }

    @Override
    public boolean isTaskOnline(long taskCode) {
        List<ProcessTaskRelation> processTaskRelationList = processTaskRelationMapper.queryByTaskCode(taskCode);
        if (!processTaskRelationList.isEmpty()) {
            Set<Long> processDefinitionCodes = processTaskRelationList
                    .stream()
                    .map(ProcessTaskRelation::getProcessDefinitionCode)
                    .collect(toSet());
            List<ProcessDefinition> processDefinitionList = processDefineMapper.queryByCodes(processDefinitionCodes);
            // check process definition is already online
            for (ProcessDefinition processDefinition : processDefinitionList) {
                if (processDefinition.getReleaseState() == ReleaseState.ONLINE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generate the DAG Graph based on the process definition id
     * Use temporarily before refactoring taskNode
     *
     * @param processDefinition process definition
     * @return dag graph
     */
    @Override
    public DAG<Long, TaskNode, TaskNodeRelation> genDagGraph(ProcessDefinition processDefinition) {
        List<ProcessTaskRelation> taskRelations =
                this.findRelationByCode(processDefinition.getCode(), processDefinition.getVersion());
        List<TaskNode> taskNodeList = transformTask(taskRelations, Lists.newArrayList());
        ProcessDag processDag = DagHelper.getProcessDag(taskNodeList, new ArrayList<>(taskRelations));
        // Generate concrete Dag to be executed
        return DagHelper.buildDagGraph(processDag);
    }

    /**
     * generate DagData
     */
    @Override
    public DagData genDagData(ProcessDefinition processDefinition) {
        List<ProcessTaskRelation> taskRelations =
                findRelationByCode(processDefinition.getCode(), processDefinition.getVersion());
        List<TaskDefinition> taskDefinitions = taskDefinitionLogDao.queryTaskDefineLogList(taskRelations)
                .stream()
                .map(t -> (TaskDefinition) t)
                .collect(Collectors.toList());
        return new DagData(processDefinition, taskRelations, taskDefinitions);
    }

    /**
     * find process task relation list by process
     */
    @Override
    public List<ProcessTaskRelation> findRelationByCode(long processDefinitionCode, int processDefinitionVersion) {
        List<ProcessTaskRelationLog> processTaskRelationLogList = processTaskRelationLogMapper
                .queryByProcessCodeAndVersion(processDefinitionCode, processDefinitionVersion);
        return processTaskRelationLogList.stream().map(r -> (ProcessTaskRelation) r).collect(Collectors.toList());
    }

    /**
     * add authorized resources
     *
     * @param ownResources own resources
     * @param userId       userId
     */
    private void addAuthorizedResources(List<Resource> ownResources, int userId) {
        List<Integer> relationResourceIds = resourceUserMapper.queryResourcesIdListByUserIdAndPerm(userId, 7);
        List<Resource> relationResources = CollectionUtils.isNotEmpty(relationResourceIds)
                ? resourceMapper.queryResourceListById(relationResourceIds)
                : new ArrayList<>();
        ownResources.addAll(relationResources);
    }

    /**
     * Use temporarily before refactoring taskNode
     */
    @Override
    public List<TaskNode> transformTask(List<ProcessTaskRelation> taskRelationList,
                                        List<TaskDefinitionLog> taskDefinitionLogs) {
        Map<Long, List<Long>> taskCodeMap = new HashMap<>();
        for (ProcessTaskRelation processTaskRelation : taskRelationList) {
            taskCodeMap.compute(processTaskRelation.getPostTaskCode(), (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                if (processTaskRelation.getPreTaskCode() != 0L) {
                    v.add(processTaskRelation.getPreTaskCode());
                }
                return v;
            });
        }
        if (CollectionUtils.isEmpty(taskDefinitionLogs)) {
            taskDefinitionLogs = taskDefinitionLogDao.queryTaskDefineLogList(taskRelationList);
        }
        Map<Long, TaskDefinitionLog> taskDefinitionLogMap = taskDefinitionLogs.stream()
                .collect(Collectors.toMap(TaskDefinitionLog::getCode, taskDefinitionLog -> taskDefinitionLog));
        List<TaskNode> taskNodeList = new ArrayList<>();
        for (Entry<Long, List<Long>> code : taskCodeMap.entrySet()) {
            TaskDefinitionLog taskDefinitionLog = taskDefinitionLogMap.get(code.getKey());
            if (taskDefinitionLog != null) {
                TaskNode taskNode = new TaskNode();
                taskNode.setCode(taskDefinitionLog.getCode());
                taskNode.setVersion(taskDefinitionLog.getVersion());
                taskNode.setName(taskDefinitionLog.getName());
                taskNode.setDesc(taskDefinitionLog.getDescription());
                taskNode.setType(taskDefinitionLog.getTaskType().toUpperCase());
                taskNode.setRunFlag(taskDefinitionLog.getFlag() == Flag.YES ? Constants.FLOWNODE_RUN_FLAG_NORMAL
                        : Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
                taskNode.setMaxRetryTimes(taskDefinitionLog.getFailRetryTimes());
                taskNode.setRetryInterval(taskDefinitionLog.getFailRetryInterval());
                Map<String, Object> taskParamsMap = taskNode.taskParamsToJsonObj(taskDefinitionLog.getTaskParams());
                taskNode.setConditionResult(JSONUtils.toJsonString(taskParamsMap.get(Constants.CONDITION_RESULT)));
                taskNode.setSwitchResult(JSONUtils.toJsonString(taskParamsMap.get(Constants.SWITCH_RESULT)));
                taskNode.setDependence(JSONUtils.toJsonString(taskParamsMap.get(Constants.DEPENDENCE)));
                taskParamsMap.remove(Constants.CONDITION_RESULT);
                taskParamsMap.remove(Constants.DEPENDENCE);
                taskNode.setParams(JSONUtils.toJsonString(taskParamsMap));
                taskNode.setTaskInstancePriority(taskDefinitionLog.getTaskPriority());
                taskNode.setWorkerGroup(taskDefinitionLog.getWorkerGroup());
                taskNode.setEnvironmentCode(taskDefinitionLog.getEnvironmentCode());
                taskNode.setTimeout(JSONUtils
                        .toJsonString(new TaskTimeoutParameter(taskDefinitionLog.getTimeoutFlag() == TimeoutFlag.OPEN,
                                taskDefinitionLog.getTimeoutNotifyStrategy(),
                                taskDefinitionLog.getTimeout())));
                taskNode.setDelayTime(taskDefinitionLog.getDelayTime());
                taskNode.setPreTasks(JSONUtils.toJsonString(code.getValue().stream().map(taskDefinitionLogMap::get)
                        .map(TaskDefinition::getCode).collect(Collectors.toList())));
                taskNode.setTaskGroupId(taskDefinitionLog.getTaskGroupId());
                taskNode.setTaskGroupPriority(taskDefinitionLog.getTaskGroupPriority());
                taskNode.setCpuQuota(taskDefinitionLog.getCpuQuota());
                taskNode.setMemoryMax(taskDefinitionLog.getMemoryMax());
                taskNode.setTaskExecuteType(taskDefinitionLog.getTaskExecuteType());
                taskNode.setIsCache(taskDefinitionLog.getIsCache().getCode());
                taskNodeList.add(taskNode);
            }
        }
        return taskNodeList;
    }

    @Override
    public Map<ProcessInstance, TaskInstance> notifyProcessList(int processId) {
        HashMap<ProcessInstance, TaskInstance> processTaskMap = new HashMap<>();
        // find sub tasks
        ProcessInstanceMap processInstanceMap = processInstanceMapMapper.queryBySubProcessId(processId);
        if (processInstanceMap == null) {
            return processTaskMap;
        }
        ProcessInstance fatherProcess = this.findProcessInstanceById(processInstanceMap.getParentProcessInstanceId());
        TaskInstance fatherTask = taskInstanceDao.queryById(processInstanceMap.getParentTaskInstanceId());

        if (fatherProcess != null) {
            processTaskMap.put(fatherProcess, fatherTask);
        }
        return processTaskMap;
    }

    @Override
    public DqExecuteResult getDqExecuteResultByTaskInstanceId(int taskInstanceId) {
        return dqExecuteResultMapper.getExecuteResultById(taskInstanceId);
    }

    @Override
    public int updateDqExecuteResultUserId(int taskInstanceId) {
        DqExecuteResult dqExecuteResult =
                dqExecuteResultMapper
                        .selectOne(new QueryWrapper<DqExecuteResult>().eq(TASK_INSTANCE_ID, taskInstanceId));
        if (dqExecuteResult == null) {
            return -1;
        }

        ProcessInstance processInstance = processInstanceMapper.selectById(dqExecuteResult.getProcessInstanceId());
        if (processInstance == null) {
            return -1;
        }

        ProcessDefinition processDefinition =
                processDefineMapper.queryByCode(processInstance.getProcessDefinitionCode());
        if (processDefinition == null) {
            return -1;
        }

        dqExecuteResult.setProcessDefinitionId(processDefinition.getId());
        dqExecuteResult.setUserId(processDefinition.getUserId());
        dqExecuteResult.setState(DqTaskState.DEFAULT.getCode());
        return dqExecuteResultMapper.updateById(dqExecuteResult);
    }

    @Override
    public int updateDqExecuteResultState(DqExecuteResult dqExecuteResult) {
        return dqExecuteResultMapper.updateById(dqExecuteResult);
    }

    @Override
    public int deleteDqExecuteResultByTaskInstanceId(int taskInstanceId) {
        return dqExecuteResultMapper.delete(
                new QueryWrapper<DqExecuteResult>()
                        .eq(TASK_INSTANCE_ID, taskInstanceId));
    }

    @Override
    public int deleteTaskStatisticsValueByTaskInstanceId(int taskInstanceId) {
        return dqTaskStatisticsValueMapper.delete(
                new QueryWrapper<DqTaskStatisticsValue>()
                        .eq(TASK_INSTANCE_ID, taskInstanceId));
    }

    @Override
    public DqRule getDqRule(int ruleId) {
        return dqRuleMapper.selectById(ruleId);
    }

    @Override
    public List<DqRuleInputEntry> getRuleInputEntry(int ruleId) {
        return DqRuleUtils.transformInputEntry(dqRuleInputEntryMapper.getRuleInputEntryList(ruleId));
    }

    @Override
    public List<DqRuleExecuteSql> getDqExecuteSql(int ruleId) {
        return dqRuleExecuteSqlMapper.getExecuteSqlList(ruleId);
    }

    @Override
    public DqComparisonType getComparisonTypeById(int id) {
        return dqComparisonTypeMapper.selectById(id);
    }

    /**
     * the first time (when submit the task ) get the resource of the task group
     */
    @Override
    public boolean acquireTaskGroup(int taskInstanceId,
                                    String taskName,
                                    int taskGroupId,
                                    int workflowInstanceId,
                                    int taskGroupPriority) {
        TaskGroup taskGroup = taskGroupMapper.selectById(taskGroupId);
        if (taskGroup == null) {
            // we don't throw exception here, to avoid the task group has been deleted during workflow running
            log.warn("The taskGroup is not exist no need to acquire taskGroup, taskGroupId: {}", taskGroupId);
            return true;
        }
        // if task group is not applicable
        if (taskGroup.getStatus() == Flag.NO.getCode()) {
            log.warn("The taskGroup status is {}, no need to acquire taskGroup, taskGroupId: {}", taskGroup.getStatus(),
                    taskGroupId);
            return true;
        }
        // Create a waiting taskGroupQueue, after acquire resource, we can update the status to ACQUIRE_SUCCESS
        TaskGroupQueue taskGroupQueue = taskGroupQueueMapper.queryByTaskId(taskInstanceId);
        if (taskGroupQueue == null) {
            taskGroupQueue = insertIntoTaskGroupQueue(
                    taskInstanceId,
                    taskName,
                    taskGroupId,
                    workflowInstanceId,
                    taskGroupPriority,
                    TaskGroupQueueStatus.WAIT_QUEUE);
            log.info("Insert TaskGroupQueue: {} successfully", taskGroupQueue.getId());
        } else {
            log.info("The task queue is already exist, taskId: {}", taskInstanceId);
            if (taskGroupQueue.getStatus() == TaskGroupQueueStatus.ACQUIRE_SUCCESS) {
                return true;
            }
        }
        // check if there already exist higher priority tasks
        List<TaskGroupQueue> highPriorityTasks = taskGroupQueueMapper.queryHighPriorityTasks(
                taskGroupId,
                taskGroupPriority,
                TaskGroupQueueStatus.WAIT_QUEUE.getCode());
        if (CollectionUtils.isNotEmpty(highPriorityTasks)) {
            return false;
        }
        // try to get taskGroup
        int availableTaskGroupCount = taskGroupMapper.selectAvailableCountById(taskGroupId);
        if (availableTaskGroupCount < 1) {
            log.info(
                    "Failed to acquire taskGroup, there is no avaliable taskGroup, taskInstanceId: {}, taskGroupId: {}",
                    taskInstanceId, taskGroupId);
            taskGroupQueueMapper.updateInQueue(Flag.NO.getCode(), taskGroupQueue.getId());
            return false;
        }
        return robTaskGroupResource(taskGroupQueue);
    }

    /**
     * try to get the task group resource(when other task release the resource)
     */
    @Override
    public boolean robTaskGroupResource(TaskGroupQueue taskGroupQueue) {
        // set the default max size to avoid dead loop
        for (int i = 0; i < 10; i++) {
            TaskGroup taskGroup = taskGroupMapper.selectById(taskGroupQueue.getGroupId());
            if (taskGroup.getGroupSize() <= taskGroup.getUseSize()) {
                // remove
                taskGroupQueueMapper.updateInQueue(Flag.NO.getCode(), taskGroupQueue.getId());
                log.info("The current task Group is full, taskGroup: {}", taskGroup);
                return false;
            }
            int affectedCount = taskGroupMapper.robTaskGroupResource(
                    taskGroup.getId(),
                    taskGroup.getUseSize(),
                    taskGroupQueue.getId(),
                    TaskGroupQueueStatus.WAIT_QUEUE.getCode());
            if (affectedCount > 0) {
                log.info("Success rob taskGroup, taskInstanceId: {}, taskGroupId: {}", taskGroupQueue.getTaskId(),
                        taskGroupQueue.getId());
                taskGroupQueue.setStatus(TaskGroupQueueStatus.ACQUIRE_SUCCESS);
                this.taskGroupQueueMapper.updateById(taskGroupQueue);
                this.taskGroupQueueMapper.updateInQueue(Flag.NO.getCode(), taskGroupQueue.getId());
                return true;
            }
        }
        log.info("Failed to rob taskGroup, taskGroupQueue: {}", taskGroupQueue);
        taskGroupQueueMapper.updateInQueue(Flag.NO.getCode(), taskGroupQueue.getId());
        return false;
    }

    @Override
    public void releaseAllTaskGroup(int processInstanceId) {
        List<TaskInstance> taskInstances = this.taskInstanceMapper.loadAllInfosNoRelease(processInstanceId,
                TaskGroupQueueStatus.ACQUIRE_SUCCESS.getCode());
        for (TaskInstance info : taskInstances) {
            releaseTaskGroup(info);
        }
    }

    /**
     * release the TGQ resource when the corresponding task is finished.
     *
     * @return the result code and msg
     */
    @Override
    public TaskInstance releaseTaskGroup(TaskInstance taskInstance) {

        TaskGroup taskGroup;
        TaskGroupQueue thisTaskGroupQueue;
        log.info("Begin to release task group: {}", taskInstance.getTaskGroupId());
        try {
            do {
                taskGroup = taskGroupMapper.selectById(taskInstance.getTaskGroupId());
                if (taskGroup == null) {
                    log.error("The taskGroup is not exist no need to release taskGroup, taskGroupId: {}",
                            taskInstance.getTaskGroupId());
                    return null;
                }
                thisTaskGroupQueue = taskGroupQueueMapper.queryByTaskId(taskInstance.getId());
                if (thisTaskGroupQueue.getStatus() == TaskGroupQueueStatus.RELEASE) {
                    log.info("The taskGroupQueue's status is release, taskInstanceId: {}", taskInstance.getId());
                    return null;
                }
                if (thisTaskGroupQueue.getStatus() == TaskGroupQueueStatus.WAIT_QUEUE) {
                    log.info("The taskGroupQueue's status is in waiting, will not need to release task group");
                    break;
                }
            } while (thisTaskGroupQueue.getForceStart() == Flag.NO.getCode()
                    && taskGroupMapper.releaseTaskGroupResource(taskGroup.getId(),
                            taskGroup.getUseSize(),
                            thisTaskGroupQueue.getId(),
                            TaskGroupQueueStatus.ACQUIRE_SUCCESS.getCode()) != 1);
        } catch (Exception e) {
            log.error("release the task group error", e);
            return null;
        }
        log.info("Finished to release task group, taskGroupId: {}", taskInstance.getTaskGroupId());

        log.info("Begin to release task group queue, taskGroupId: {}", taskInstance.getTaskGroupId());
        changeTaskGroupQueueStatus(taskInstance.getId(), TaskGroupQueueStatus.RELEASE);
        TaskGroupQueue taskGroupQueue;
        do {
            taskGroupQueue = taskGroupQueueMapper.queryTheHighestPriorityTasks(
                    taskGroup.getId(),
                    TaskGroupQueueStatus.WAIT_QUEUE.getCode(),
                    Flag.NO.getCode(),
                    Flag.NO.getCode());
            if (taskGroupQueue == null) {
                log.info("There is no taskGroupQueue need to be wakeup taskGroup: {}", taskGroup.getId());
                return null;
            }
        } while (this.taskGroupQueueMapper.updateInQueueCAS(
                Flag.NO.getCode(),
                Flag.YES.getCode(),
                taskGroupQueue.getId()) != 1);
        log.info("Finished to release task group queue: taskGroupId: {}, taskGroupQueueId: {}",
                taskInstance.getTaskGroupId(), taskGroupQueue.getId());
        return taskInstanceMapper.selectById(taskGroupQueue.getTaskId());
    }

    /**
     * release the TGQ resource when the corresponding task is finished.
     *
     * @param taskId task id
     * @return the result code and msg
     */

    @Override
    public void changeTaskGroupQueueStatus(int taskId, TaskGroupQueueStatus status) {
        TaskGroupQueue taskGroupQueue = taskGroupQueueMapper.queryByTaskId(taskId);
        taskGroupQueue.setInQueue(Flag.NO.getCode());
        taskGroupQueue.setStatus(status);
        taskGroupQueue.setUpdateTime(new Date(System.currentTimeMillis()));
        taskGroupQueueMapper.updateById(taskGroupQueue);
    }

    @Override
    public TaskGroupQueue insertIntoTaskGroupQueue(Integer taskInstanceId,
                                                   String taskName,
                                                   Integer taskGroupId,
                                                   Integer workflowInstanceId,
                                                   Integer taskGroupPriority,
                                                   TaskGroupQueueStatus status) {
        Date now = new Date();
        TaskGroupQueue taskGroupQueue = TaskGroupQueue.builder()
                .taskId(taskInstanceId)
                .taskName(taskName)
                .groupId(taskGroupId)
                .processId(workflowInstanceId)
                .priority(taskGroupPriority)
                .status(status)
                .forceStart(Flag.NO.getCode())
                .inQueue(Flag.NO.getCode())
                .createTime(now)
                .updateTime(now)
                .build();
        taskGroupQueueMapper.insert(taskGroupQueue);
        return taskGroupQueue;
    }

    @Override
    public int updateTaskGroupQueueStatus(Integer taskId, int status) {
        return taskGroupQueueMapper.updateStatusByTaskId(taskId, status);
    }

    @Override
    public int updateTaskGroupQueue(TaskGroupQueue taskGroupQueue) {
        return taskGroupQueueMapper.updateById(taskGroupQueue);
    }

    @Override
    public TaskGroupQueue loadTaskGroupQueue(int taskId) {
        return this.taskGroupQueueMapper.queryByTaskId(taskId);
    }

    @Override
    public ProcessInstance loadNextProcess4Serial(long code, int state, int id) {
        return this.processInstanceMapper.loadNextProcess4Serial(code, state, id);
    }

    protected void deleteCommandWithCheck(int commandId) {
        int delete = this.commandMapper.deleteById(commandId);
        if (delete != 1) {
            throw new ServiceException("delete command fail, id:" + commandId);
        }
    }

    /**
     * find k8s config yaml by clusterName
     *
     * @param clusterName clusterName
     * @return datasource
     */

    @Override
    public String findConfigYamlByName(String clusterName) {
        if (Strings.isNullOrEmpty(clusterName)) {
            return null;
        }

        QueryWrapper<Cluster> nodeWrapper = new QueryWrapper<>();
        nodeWrapper.eq("name", clusterName);
        Cluster cluster = clusterMapper.selectOne(nodeWrapper);
        return cluster == null ? null : ClusterConfUtils.getK8sConfig(cluster.getConfig());
    }

    @Override
    public void forceProcessInstanceSuccessByTaskInstanceId(Integer taskInstanceId) {
        TaskInstance task = taskInstanceMapper.selectById(taskInstanceId);
        if (task == null) {
            return;
        }
        ProcessInstance processInstance = findProcessInstanceDetailById(task.getProcessInstanceId()).orElse(null);
        if (processInstance != null
                && (processInstance.getState().isFailure() || processInstance.getState().isStop())) {
            List<TaskInstance> validTaskList =
                    taskInstanceDao.queryValidTaskListByWorkflowInstanceId(processInstance.getId(),
                            processInstance.getTestFlag());
            List<Long> instanceTaskCodeList =
                    validTaskList.stream().map(TaskInstance::getTaskCode).collect(Collectors.toList());
            List<ProcessTaskRelation> taskRelations = findRelationByCode(processInstance.getProcessDefinitionCode(),
                    processInstance.getProcessDefinitionVersion());
            List<TaskDefinitionLog> taskDefinitionLogs = taskDefinitionLogDao.queryTaskDefineLogList(taskRelations);
            List<Long> definiteTaskCodeList =
                    taskDefinitionLogs.stream().filter(definitionLog -> definitionLog.getFlag() == Flag.YES)
                            .map(TaskDefinitionLog::getCode).collect(Collectors.toList());
            // only all tasks have instances
            if (CollectionUtils.isEqualCollection(instanceTaskCodeList,
                    definiteTaskCodeList)) {
                List<Integer> failTaskList = validTaskList.stream()
                        .filter(instance -> instance.getState().isFailure() || instance.getState().isKill())
                        .map(TaskInstance::getId).collect(Collectors.toList());
                if (failTaskList.size() == 1 && failTaskList.contains(taskInstanceId)) {
                    processInstance.setStateWithDesc(WorkflowExecutionStatus.SUCCESS, "success by task force success");
                    processInstanceDao.updateById(processInstance);
                }
            }
        }
    }

    @Override
    public void saveCommandTrigger(Integer commandId, Integer processInstanceId) {
        triggerRelationService.saveCommandTrigger(commandId, processInstanceId);
    }

    private Map<String, Object> createCommandParams(ProcessInstance processInstance) {
        Map<String, Object> commandMap =
                JSONUtils.parseObject(processInstance.getCommandParam(), new TypeReference<Map<String, Object>>() {
                });
        Map<String, Object> recoverFailoverCommandParams = new HashMap<>();
        Optional.ofNullable(MapUtils.getObject(commandMap, CMD_PARAM_START_PARAMS))
                .ifPresent(startParams -> recoverFailoverCommandParams.put(CMD_PARAM_START_PARAMS, startParams));
        recoverFailoverCommandParams.put(CMD_PARAM_RECOVER_PROCESS_ID_STRING, processInstance.getId());
        return recoverFailoverCommandParams;
    }

}
