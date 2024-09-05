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
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_SUB_WORKFLOW_DEFINITION_CODE;
import static org.apache.dolphinscheduler.common.constants.Constants.LOCAL_PARAMS;

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
import org.apache.dolphinscheduler.dao.entity.DqComparisonType;
import org.apache.dolphinscheduler.dao.entity.DqRule;
import org.apache.dolphinscheduler.dao.entity.DqRuleExecuteSql;
import org.apache.dolphinscheduler.dao.entity.DqRuleInputEntry;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstanceRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelationLog;
import org.apache.dolphinscheduler.dao.mapper.ClusterMapper;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.apache.dolphinscheduler.dao.mapper.DqComparisonTypeMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleExecuteSqlMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleInputEntryMapper;
import org.apache.dolphinscheduler.dao.mapper.DqRuleMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskGroupQueueMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowTaskRelationMapper;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceMapDao;
import org.apache.dolphinscheduler.dao.utils.DqRuleUtils;
import org.apache.dolphinscheduler.dao.utils.EnvironmentUtils;
import org.apache.dolphinscheduler.dao.utils.WorkerGroupUtils;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.common.ILogService;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SubWorkflowParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.TaskTimeoutParameter;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.service.command.CommandService;
import org.apache.dolphinscheduler.service.cron.CronUtils;
import org.apache.dolphinscheduler.service.exceptions.CronParseException;
import org.apache.dolphinscheduler.service.exceptions.ServiceException;
import org.apache.dolphinscheduler.service.expand.CuringParamsService;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.utils.ClusterConfUtils;
import org.apache.dolphinscheduler.service.utils.DagHelper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private WorkflowDefinitionMapper workflowDefinitionMapper;

    @Autowired
    private WorkflowDefinitionLogMapper workflowDefinitionLogMapper;

    // todo replace with workflowInstanceDao
    @Autowired
    private WorkflowInstanceMapper workflowInstanceMapper;

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private TaskDefinitionDao taskDefinitionDao;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private TaskDefinitionLogDao taskDefinitionLogDao;

    @Autowired
    private WorkflowInstanceMapDao workflowInstanceMapDao;

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private WorkflowInstanceRelationMapper workflowInstanceRelationMapper;

    @Autowired
    private CommandMapper commandMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private DqRuleMapper dqRuleMapper;

    @Autowired
    private DqRuleInputEntryMapper dqRuleInputEntryMapper;

    @Autowired
    private DqRuleExecuteSqlMapper dqRuleExecuteSqlMapper;

    @Autowired
    private DqComparisonTypeMapper dqComparisonTypeMapper;

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private WorkflowTaskRelationMapper workflowTaskRelationMapper;

    @Autowired
    private WorkflowTaskRelationLogMapper workflowTaskRelationLogMapper;

    @Autowired
    private TaskGroupQueueMapper taskGroupQueueMapper;

    @Autowired
    private ClusterMapper clusterMapper;

    @Autowired
    private CuringParamsService curingGlobalParamsService;

    @Autowired
    private CommandService commandService;

    /**
     * find workflow instance detail by id
     *
     * @param workflowInstanceId workflowInstanceId
     * @return workflow instance
     */
    @Override
    public Optional<WorkflowInstance> findWorkflowInstanceDetailById(int workflowInstanceId) {
        return Optional.ofNullable(workflowInstanceMapper.queryDetailById(workflowInstanceId));
    }

    /**
     * find workflow instance by id
     *
     * @param workflowInstanceId workflowInstanceId
     * @return workflow instance
     */
    @Override
    public WorkflowInstance findWorkflowInstanceById(int workflowInstanceId) {
        return workflowInstanceMapper.selectById(workflowInstanceId);
    }

    /**
     * find workflow define by code and version.
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @return workflow definition
     */
    @Override
    public WorkflowDefinition findWorkflowDefinition(Long workflowDefinitionCode, int workflowDefinitionVersion) {
        WorkflowDefinition workflowDefinition = workflowDefinitionMapper.queryByCode(workflowDefinitionCode);
        if (workflowDefinition == null || workflowDefinition.getVersion() != workflowDefinitionVersion) {
            workflowDefinition = workflowDefinitionLogMapper.queryByDefinitionCodeAndVersion(workflowDefinitionCode,
                    workflowDefinitionVersion);
            if (workflowDefinition != null) {
                workflowDefinition.setId(0);
            }
        }
        return workflowDefinition;
    }

    /**
     * find workflow define by code.
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @return workflow definition
     */
    @Override
    public WorkflowDefinition findWorkflowDefinitionByCode(Long workflowDefinitionCode) {
        return workflowDefinitionMapper.queryByCode(workflowDefinitionCode);
    }

    /**
     * delete work workflow instance by id
     *
     * @param workflowInstanceId workflowInstanceId
     * @return delete workflow instance result
     */
    @Override
    public int deleteWorkflowInstanceById(int workflowInstanceId) {
        return workflowInstanceMapper.deleteById(workflowInstanceId);
    }

    /**
     * delete all sub workflow by parent instance id
     *
     * @param workflowInstanceId workflowInstanceId
     * @return delete all sub workflow instance result
     */
    @Override
    public int deleteAllSubWorkflowByParentId(int workflowInstanceId) {

        List<Integer> subWorkflowIdList = workflowInstanceRelationMapper.querySubIdListByParentId(workflowInstanceId);

        for (Integer subId : subWorkflowIdList) {
            deleteAllSubWorkflowByParentId(subId);
            deleteWorkflowMapByParentId(subId);
            removeTaskLogFile(subId);
            deleteWorkflowInstanceById(subId);
        }
        return 1;
    }

    /**
     * remove task log file
     *
     * @param workflowInstanceId workflowInstanceId
     */
    @Override
    public void removeTaskLogFile(Integer workflowInstanceId) {
        List<TaskInstance> taskInstanceList = taskInstanceDao.queryByWorkflowInstanceId(workflowInstanceId);
        if (CollectionUtils.isEmpty(taskInstanceList)) {
            return;
        }
        for (TaskInstance taskInstance : taskInstanceList) {
            String taskLogPath = taskInstance.getLogPath();
            if (StringUtils.isEmpty(taskInstance.getHost()) || StringUtils.isEmpty(taskLogPath)) {
                continue;
            }
            Clients
                    .withService(ILogService.class)
                    .withHost(taskInstance.getHost())
                    .removeTaskInstanceLog(taskLogPath);
        }
    }

    /**
     * recursive query sub workflow definition id by parent id.
     *
     * @param parentCode parentCode
     */
    @Override
    public List<Long> findAllSubWorkflowDefinitionCode(long parentCode) {
        List<TaskDefinition> taskNodeList = taskDefinitionDao.getTaskDefinitionListByDefinition(parentCode);
        if (CollectionUtils.isEmpty(taskNodeList)) {
            return Collections.emptyList();
        }
        List<Long> subWorkflowDefinitionCodes = new ArrayList<>();

        for (TaskDefinition taskNode : taskNodeList) {
            String parameter = taskNode.getTaskParams();
            ObjectNode parameterJson = JSONUtils.parseObject(parameter);
            if (parameterJson.get(CMD_PARAM_SUB_WORKFLOW_DEFINITION_CODE) != null) {
                SubWorkflowParameters subProcessParam = JSONUtils.parseObject(parameter, SubWorkflowParameters.class);
                long subWorkflowDefinitionCode = subProcessParam.getWorkflowDefinitionCode();
                subWorkflowDefinitionCodes.add(subWorkflowDefinitionCode);
                subWorkflowDefinitionCodes.addAll(findAllSubWorkflowDefinitionCode(subWorkflowDefinitionCode));
            }
        }
        return subWorkflowDefinitionCodes;
    }

    /**
     * generate a new work workflow instance from command.
     *
     * @param workflowDefinition workflowDefinition
     * @param command           command
     * @param cmdParam          cmdParam map
     * @return workflow instance
     */
    private WorkflowInstance generateNewWorkflowInstance(WorkflowDefinition workflowDefinition,
                                                         Command command,
                                                         Map<String, String> cmdParam) {
        WorkflowInstance workflowInstance = new WorkflowInstance(workflowDefinition);
        workflowInstance.setWorkflowDefinitionCode(workflowDefinition.getCode());
        workflowInstance.setWorkflowDefinitionVersion(workflowDefinition.getVersion());
        workflowInstance.setProjectCode(workflowDefinition.getProjectCode());
        workflowInstance.setStateWithDesc(WorkflowExecutionStatus.RUNNING_EXECUTION, "init running");
        workflowInstance.setRecovery(Flag.NO);
        workflowInstance.setStartTime(new Date());
        // the new workflow instance restart time is null.
        workflowInstance.setRestartTime(null);
        workflowInstance.setRunTimes(1);
        workflowInstance.setMaxTryTimes(0);
        workflowInstance.setCommandParam(command.getCommandParam());
        workflowInstance.setCommandType(command.getCommandType());
        workflowInstance.setIsSubWorkflow(Flag.NO);
        workflowInstance.setTaskDependType(command.getTaskDependType());
        workflowInstance.setFailureStrategy(command.getFailureStrategy());
        workflowInstance.setExecutorId(command.getExecutorId());
        workflowInstance.setExecutorName(Optional.ofNullable(userMapper.selectById(command.getExecutorId()))
                .map(User::getUserName).orElse(null));
        WarningType warningType = command.getWarningType() == null ? WarningType.NONE : command.getWarningType();
        workflowInstance.setWarningType(warningType);
        Integer warningGroupId = command.getWarningGroupId() == null ? 0 : command.getWarningGroupId();
        workflowInstance.setWarningGroupId(warningGroupId);
        workflowInstance.setDryRun(command.getDryRun());
        workflowInstance.setTestFlag(command.getTestFlag());

        if (command.getScheduleTime() != null) {
            workflowInstance.setScheduleTime(command.getScheduleTime());
        }
        workflowInstance.setCommandStartTime(command.getStartTime());
        workflowInstance.setLocations(workflowDefinition.getLocations());

        // reset global params while there are start parameters
        setGlobalParamIfCommanded(workflowDefinition, cmdParam);

        // curing global params
        Map<String, String> commandParamMap = JSONUtils.toMap(command.getCommandParam());
        String timezoneId = null;
        if (commandParamMap != null) {
            timezoneId = commandParamMap.get(Constants.SCHEDULE_TIMEZONE);
        }

        String globalParams = curingGlobalParamsService.curingGlobalParams(workflowInstance.getId(),
                workflowDefinition.getGlobalParamMap(),
                workflowDefinition.getGlobalParamList(),
                getCommandTypeIfComplement(workflowInstance, command),
                workflowInstance.getScheduleTime(), timezoneId);
        workflowInstance.setGlobalParams(globalParams);

        // set workflow instance priority
        workflowInstance.setWorkflowInstancePriority(command.getWorkflowInstancePriority());
        workflowInstance.setWorkerGroup(WorkerGroupUtils.getWorkerGroupOrDefault(command.getWorkerGroup()));
        workflowInstance.setEnvironmentCode(EnvironmentUtils.getEnvironmentCodeOrDefault(command.getEnvironmentCode()));
        workflowInstance.setTimeout(workflowDefinition.getTimeout());
        workflowInstance.setTenantCode(command.getTenantCode());
        return workflowInstance;
    }

    @Override
    public void setGlobalParamIfCommanded(WorkflowDefinition workflowDefinition, Map<String, String> cmdParam) {

        // get start params from command param
        Map<String, Property> fatherParam = curingGlobalParamsService.parseWorkflowFatherParam(cmdParam);
        Map<String, Property> startParamMap = new HashMap<>(fatherParam);

        Map<String, Property> currentStartParamMap = curingGlobalParamsService.parseWorkflowStartParam(cmdParam);
        startParamMap.putAll(currentStartParamMap);

        // set start param into global params
        Map<String, String> globalMap = workflowDefinition.getGlobalParamMap();
        List<Property> globalParamList = workflowDefinition.getGlobalParamList();
        if (MapUtils.isNotEmpty(startParamMap) && globalMap != null) {
            // start param to overwrite global param
            for (Map.Entry<String, String> param : globalMap.entrySet()) {
                String globalKey = param.getKey();
                if (startParamMap.containsKey(globalKey)) {
                    String val = startParamMap.get(globalKey).getValue();
                    if (val != null) {
                        param.setValue(val);
                    }
                }
            }
            // start param to create new global param if global not exist
            for (Entry<String, Property> startParam : startParamMap.entrySet()) {
                if (!globalMap.containsKey(startParam.getKey())) {
                    globalMap.put(startParam.getKey(), startParam.getValue().getValue());
                    globalParamList.add(startParam.getValue());
                }
            }
        }
    }

    /**
     * Get workflow runtime tenant
     * <p>
     * the workflow provides a tenant and uses the provided tenant;
     * when no tenant is provided or the provided tenant is the default tenant, \
     * the user's tenant created by the workflow is used
     *
     * @param tenantCode tenantCode
     * @param userId     userId
     * @return tenant code
     */
    @Override
    public String getTenantForWorkflow(String tenantCode, int userId) {
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
     * construct workflow instance according to one command.
     *
     * @param command command
     * @param host    host
     * @return workflow instance
     */
    @Override
    public @Nullable WorkflowInstance constructWorkflowInstance(Command command,
                                                                String host) throws CronParseException, CodeGenerateException {
        WorkflowInstance workflowInstance;
        WorkflowDefinition workflowDefinition;
        CommandType commandType = command.getCommandType();

        workflowDefinition =
                this.findWorkflowDefinition(command.getWorkflowDefinitionCode(),
                        command.getWorkflowDefinitionVersion());
        if (workflowDefinition == null) {
            log.error("cannot find the work workflow define! define code : {}", command.getWorkflowDefinitionCode());
            throw new IllegalArgumentException("Cannot find the workflow definition for this workflowInstance");
        }
        Map<String, String> cmdParam = JSONUtils.toMap(command.getCommandParam());
        if (cmdParam == null) {
            cmdParam = new HashMap<>();
        }
        int workflowInstanceId = command.getWorkflowInstanceId();
        if (workflowInstanceId == 0) {
            workflowInstance = generateNewWorkflowInstance(workflowDefinition, command, cmdParam);
        } else {
            workflowInstance = this.findWorkflowInstanceDetailById(workflowInstanceId).orElse(null);
            setGlobalParamIfCommanded(workflowDefinition, cmdParam);
            if (workflowInstance == null) {
                return null;
            }
        }

        CommandType commandTypeIfComplement = getCommandTypeIfComplement(workflowInstance, command);
        // reset global params while repeat running and recover tolerance fault workflow is needed by cmdParam
        if (commandTypeIfComplement == CommandType.REPEAT_RUNNING ||
                commandTypeIfComplement == CommandType.RECOVER_TOLERANCE_FAULT_PROCESS ||
                commandTypeIfComplement == CommandType.RECOVER_SERIAL_WAIT) {
            setGlobalParamIfCommanded(workflowDefinition, cmdParam);
        }

        // time zone
        String timezoneId = cmdParam.get(Constants.SCHEDULE_TIMEZONE);

        // Recalculate global parameters after rerun.
        String globalParams = curingGlobalParamsService.curingGlobalParams(workflowInstance.getId(),
                workflowDefinition.getGlobalParamMap(),
                workflowDefinition.getGlobalParamList(),
                commandTypeIfComplement,
                workflowInstance.getScheduleTime(), timezoneId);
        workflowInstance.setGlobalParams(globalParams);
        workflowInstance.setWorkflowDefinition(workflowDefinition);

        // reset command parameter
        if (workflowInstance.getCommandParam() != null) {
            Map<String, String> workflowCmdParam = JSONUtils.toMap(workflowInstance.getCommandParam());
            Map<String, String> finalCmdParam = cmdParam;
            workflowCmdParam.forEach((key, value) -> {
                if (!finalCmdParam.containsKey(key)) {
                    finalCmdParam.put(key, value);
                }
            });
        }
        // reset command parameter if sub workflow
        if (cmdParam.containsKey(CommandKeyConstants.CMD_PARAM_SUB_WORKFLOW)) {
            workflowInstance.setCommandParam(command.getCommandParam());
        }
        if (Boolean.FALSE.equals(checkCmdParam(command, cmdParam))) {
            log.error("command parameter check failed!");
            return null;
        }
        if (command.getScheduleTime() != null) {
            workflowInstance.setScheduleTime(command.getScheduleTime());
        }
        workflowInstance.setHost(host);
        workflowInstance.setRestartTime(new Date());
        WorkflowExecutionStatus runStatus = WorkflowExecutionStatus.RUNNING_EXECUTION;
        int runTime = workflowInstance.getRunTimes();
        switch (commandType) {
            case START_PROCESS:
            case DYNAMIC_GENERATION:
                break;
            case START_FAILURE_TASK_PROCESS:
            case RECOVER_SUSPENDED_PROCESS:
                List<TaskInstance> needToStartTaskInstances = taskInstanceDao
                        .queryValidTaskListByWorkflowInstanceId(workflowInstance.getId(),
                                workflowInstance.getTestFlag())
                        .stream()
                        .filter(taskInstance -> {
                            TaskExecutionStatus state = taskInstance.getState();
                            return state == TaskExecutionStatus.FAILURE
                                    || state == TaskExecutionStatus.PAUSE
                                    || state == TaskExecutionStatus.NEED_FAULT_TOLERANCE
                                    || state == TaskExecutionStatus.KILL;
                        })
                        .collect(Collectors.toList());

                for (TaskInstance taskInstance : needToStartTaskInstances) {
                    initTaskInstance(taskInstance);
                }
                String startTaskInstanceIds = needToStartTaskInstances.stream()
                        .map(TaskInstance::getId)
                        .map(String::valueOf)
                        .collect(Collectors.joining(Constants.COMMA));
                cmdParam.put(CommandKeyConstants.CMD_PARAM_RECOVERY_START_NODE_STRING, startTaskInstanceIds);
                workflowInstance.setCommandParam(JSONUtils.toJsonString(cmdParam));
                workflowInstance.setRunTimes(runTime + 1);
                break;
            case START_CURRENT_TASK_PROCESS:
                break;
            case RECOVER_TOLERANCE_FAULT_PROCESS:
                // recover tolerance fault workflow
                // If the workflow instance is in ready state, we will change to running, this can avoid the workflow
                // instance
                // status is not correct with taskInstance status
                if (workflowInstance.getState() == WorkflowExecutionStatus.READY_PAUSE
                        || workflowInstance.getState() == WorkflowExecutionStatus.READY_STOP) {
                    // todo: If we handle the ready state in WorkflowExecuteRunnable then we can remove below code
                    workflowInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
                }
                workflowInstance.setRecovery(Flag.YES);
                workflowInstance.setRunTimes(runTime + 1);
                runStatus = workflowInstance.getState();
                break;
            case COMPLEMENT_DATA:
                // delete all the valid tasks when complement data if id is not null
                if (workflowInstance.getId() != null) {
                    List<TaskInstance> taskInstanceList =
                            taskInstanceDao.queryValidTaskListByWorkflowInstanceId(workflowInstance.getId(),
                                    workflowInstance.getTestFlag());
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
                    workflowInstance.setCommandParam(JSONUtils.toJsonString(cmdParam));
                }
                // delete the StartNodeList from command parameter if last execution is only execute specified tasks
                if (workflowInstance.getCommandType().equals(CommandType.EXECUTE_TASK)) {
                    cmdParam.remove(CommandKeyConstants.CMD_PARAM_START_NODES);
                    workflowInstance.setCommandParam(JSONUtils.toJsonString(cmdParam));
                    workflowInstance.setTaskDependType(command.getTaskDependType());
                }
                // delete all the valid tasks when repeat running
                List<TaskInstance> validTaskList =
                        taskInstanceDao.queryValidTaskListByWorkflowInstanceId(workflowInstance.getId(),
                                workflowInstance.getTestFlag());
                for (TaskInstance taskInstance : validTaskList) {
                    taskInstance.setFlag(Flag.NO);
                    taskInstanceDao.updateById(taskInstance);
                }
                workflowInstance.setStartTime(new Date());
                workflowInstance.setRestartTime(workflowInstance.getStartTime());
                workflowInstance.setEndTime(null);
                workflowInstance.setRunTimes(runTime + 1);
                initComplementDataParam(workflowDefinition, workflowInstance, cmdParam);
                break;
            case SCHEDULER:
                break;
            case EXECUTE_TASK:
                workflowInstance.setRunTimes(runTime + 1);
                workflowInstance.setTaskDependType(command.getTaskDependType());
                workflowInstance.setCommandParam(JSONUtils.toJsonString(cmdParam));
                break;
            default:
                break;
        }
        workflowInstance.setStateWithDesc(runStatus, commandType.getDescp());
        return workflowInstance;
    }

    /**
     * return complement data if the workflow start with complement data
     *
     * @param workflowInstance workflowInstance
     * @param command         command
     * @return command type
     */
    private CommandType getCommandTypeIfComplement(WorkflowInstance workflowInstance, Command command) {
        if (CommandType.COMPLEMENT_DATA == workflowInstance.getCmdTypeIfComplement()) {
            return CommandType.COMPLEMENT_DATA;
        } else {
            return command.getCommandType();
        }
    }

    /**
     * initialize complement data parameters
     *
     * @param workflowDefinition workflowDefinition
     * @param workflowInstance   workflowInstance
     * @param cmdParam          cmdParam
     */
    private void initComplementDataParam(WorkflowDefinition workflowDefinition,
                                         WorkflowInstance workflowInstance,
                                         Map<String, String> cmdParam) throws CronParseException {
        if (!workflowInstance.isComplementData()) {
            return;
        }

        Date start = DateUtils.stringToDate(cmdParam.get(CMD_PARAM_COMPLEMENT_DATA_START_DATE));
        Date end = DateUtils.stringToDate(cmdParam.get(CMD_PARAM_COMPLEMENT_DATA_END_DATE));
        List<Date> complementDate = Lists.newLinkedList();
        if (start != null && end != null) {
            List<Schedule> listSchedules =
                    queryReleaseSchedulerListByWorkflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode());
            complementDate = CronUtils.getSelfFireDateList(start, end, listSchedules);
        }
        if (cmdParam.containsKey(CMD_PARAM_COMPLEMENT_DATA_SCHEDULE_DATE_LIST)) {
            complementDate = CronUtils.getSelfScheduleDateList(cmdParam);
        }

        if (CollectionUtils.isNotEmpty(complementDate) && Flag.NO == workflowInstance.getIsSubWorkflow()) {
            workflowInstance.setScheduleTime(complementDate.get(0));
        }

        // time zone
        String timezoneId = cmdParam.get(Constants.SCHEDULE_TIMEZONE);

        String globalParams = curingGlobalParamsService.curingGlobalParams(workflowInstance.getId(),
                workflowDefinition.getGlobalParamMap(),
                workflowDefinition.getGlobalParamList(),
                CommandType.COMPLEMENT_DATA, workflowInstance.getScheduleTime(), timezoneId);
        workflowInstance.setGlobalParams(globalParams);
    }

    /**
     * initialize task instance
     *
     * @param taskInstance taskInstance
     */
    private void initTaskInstance(TaskInstance taskInstance) {

        if (!TaskTypeUtils.isSubWorkflowTask(taskInstance.getTaskType())
                && (taskInstance.getState().isKill() || taskInstance.getState().isFailure())) {
            taskInstance.setFlag(Flag.NO);
            taskInstanceDao.updateById(taskInstance);
            return;
        }
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        taskInstanceDao.updateById(taskInstance);
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
            resourceInfo.setResourceName(resourceFullName);
            log.info("updated resource info {}",
                    JSONUtils.toJsonString(resourceInfo));
        }
        return resourceInfo;
    }

    /**
     * delete work workflow map by parent workflow id
     *
     * @param parentWorkflowId parentWorkflowId
     * @return delete workflow map result
     */
    @Override
    public int deleteWorkflowMapByParentId(int parentWorkflowId) {
        return workflowInstanceRelationMapper.deleteByParentWorkflowInstanceId(parentWorkflowId);

    }

    /**
     * find sub workflow instance
     *
     * @param parentWorkflowInstanceId parentWorkflowInstanceId
     * @param parentTaskId    parentTaskId
     * @return workflow instance
     */
    @Override
    public WorkflowInstance findSubWorkflowInstance(Integer parentWorkflowInstanceId, Integer parentTaskId) {
        WorkflowInstance workflowInstance = null;
        WorkflowInstanceRelation workflowInstanceRelation =
                workflowInstanceRelationMapper.queryByParentId(parentWorkflowInstanceId, parentTaskId);
        if (workflowInstanceRelation == null || workflowInstanceRelation.getWorkflowInstanceId() == 0) {
            return workflowInstance;
        }
        workflowInstance = findWorkflowInstanceById(workflowInstanceRelation.getWorkflowInstanceId());
        return workflowInstance;
    }

    /**
     * find parent workflow instance
     *
     * @param subWorkflowInstanceId subWorkflowId
     * @return workflow instance
     */
    @Override
    public WorkflowInstance findParentWorkflowInstance(Integer subWorkflowInstanceId) {
        WorkflowInstance workflowInstance = null;
        WorkflowInstanceRelation workflowInstanceRelation =
                workflowInstanceRelationMapper.queryBySubWorkflowId(subWorkflowInstanceId);
        if (workflowInstanceRelation == null || workflowInstanceRelation.getWorkflowInstanceId() == 0) {
            return workflowInstance;
        }
        workflowInstance = findWorkflowInstanceById(workflowInstanceRelation.getParentWorkflowInstanceId());
        return workflowInstance;
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
     * query Schedule by workflowDefinitionCode
     *
     * @param workflowDefinitionCode workflowDefinitionCode
     * @see Schedule
     */
    @Override
    public List<Schedule> queryReleaseSchedulerListByWorkflowDefinitionCode(long workflowDefinitionCode) {
        return scheduleMapper.queryReleaseSchedulerListByWorkflowDefinitionCode(workflowDefinitionCode);
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
     * format task app id in task instance
     */
    @Override
    public String formatTaskAppId(TaskInstance taskInstance) {
        WorkflowInstance workflowInstance = findWorkflowInstanceById(taskInstance.getWorkflowInstanceId());
        if (workflowInstance == null) {
            return "";
        }
        WorkflowDefinition definition = findWorkflowDefinition(workflowInstance.getWorkflowDefinitionCode(),
                workflowInstance.getWorkflowDefinitionVersion());
        if (definition == null) {
            return "";
        }
        return String.format("%s_%s_%s", definition.getId(), workflowInstance.getId(), taskInstance.getId());
    }

    /**
     * list unauthorized
     *
     * @param userId     user id
     * @param needChecks data source id array
     * @return unauthorized
     */
    @Override
    public <T> List<T> listUnauthorized(int userId, T[] needChecks, AuthorizationType authorizationType) {
        List<T> resultList = new ArrayList<>();

        if (Objects.nonNull(needChecks) && needChecks.length > 0) {
            Set<T> originResSet = new HashSet<>(Arrays.asList(needChecks));

            switch (authorizationType) {
                case DATASOURCE:
                    Set<Integer> authorizedDatasources = dataSourceMapper.listAuthorizedDataSource(userId, needChecks)
                            .stream().map(DataSource::getId).collect(toSet());
                    originResSet.removeAll(authorizedDatasources);
                    break;
                default:
                    break;
            }

            resultList.addAll(originResSet);
        }

        return resultList;
    }

    /**
     * switch workflow definition version to workflow definition log version
     */
    @Override
    public int switchVersion(WorkflowDefinition workflowDefinition, WorkflowDefinitionLog workflowDefinitionLog) {
        if (null == workflowDefinition || null == workflowDefinitionLog) {
            return Constants.DEFINITION_FAILURE;
        }
        workflowDefinitionLog.setId(workflowDefinition.getId());
        workflowDefinitionLog.setReleaseState(ReleaseState.OFFLINE);
        workflowDefinitionLog.setFlag(Flag.YES);

        int result = workflowDefinitionMapper.updateById(workflowDefinitionLog);
        if (result > 0) {
            result = switchWorkflowTaskRelationVersion(workflowDefinitionLog);
            if (result <= 0) {
                return Constants.EXIT_CODE_FAILURE;
            }
        }
        return result;
    }

    @Override
    public int switchWorkflowTaskRelationVersion(WorkflowDefinition workflowDefinition) {
        List<WorkflowTaskRelation> workflowTaskRelationList =
                workflowTaskRelationMapper.queryByWorkflowDefinitionCode(workflowDefinition.getCode());
        if (!workflowTaskRelationList.isEmpty()) {
            workflowTaskRelationMapper.deleteByWorkflowDefinitionCode(workflowDefinition.getProjectCode(),
                    workflowDefinition.getCode());
        }
        List<WorkflowTaskRelation> workflowTaskRelationListFromLog = workflowTaskRelationLogMapper
                .queryByWorkflowCodeAndVersion(workflowDefinition.getCode(), workflowDefinition.getVersion()).stream()
                .map(WorkflowTaskRelation::new).collect(Collectors.toList());
        int batchInsert = workflowTaskRelationMapper.batchInsert(workflowTaskRelationListFromLog);
        if (batchInsert == 0) {
            return Constants.EXIT_CODE_FAILURE;
        } else {
            int result = 0;
            for (WorkflowTaskRelation taskRelation : workflowTaskRelationListFromLog) {
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
    @Deprecated
    @Override
    public String getResourceIds(TaskDefinition taskDefinition) {
        return "";
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
                taskDefinitionLog.setCode(CodeGenerateUtils.genCode());
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
     * save workflowDefinition (including create or update workflowDefinition)
     */
    @Override
    public int saveWorkflowDefine(User operator, WorkflowDefinition workflowDefinition, Boolean syncDefine,
                                  Boolean isFromWorkflowDefinition) {
        WorkflowDefinitionLog workflowDefinitionLog = new WorkflowDefinitionLog(workflowDefinition);
        Integer version = workflowDefinitionLogMapper.queryMaxVersionForDefinition(workflowDefinition.getCode());
        int insertVersion = version == null || version == 0 ? Constants.VERSION_FIRST : version + 1;
        workflowDefinitionLog.setVersion(insertVersion);
        workflowDefinitionLog
                .setReleaseState(
                        !isFromWorkflowDefinition || workflowDefinitionLog.getReleaseState() == ReleaseState.ONLINE
                                ? ReleaseState.ONLINE
                                : ReleaseState.OFFLINE);
        workflowDefinitionLog.setOperator(operator.getId());
        workflowDefinitionLog.setOperateTime(workflowDefinition.getUpdateTime());
        workflowDefinitionLog.setId(null);
        int insertLog = workflowDefinitionLogMapper.insert(workflowDefinitionLog);
        int result = 1;
        if (Boolean.TRUE.equals(syncDefine)) {
            if (workflowDefinition.getId() == null) {
                result = workflowDefinitionMapper.insert(workflowDefinitionLog);
                workflowDefinition.setId(workflowDefinitionLog.getId());
            } else {
                workflowDefinitionLog.setId(workflowDefinition.getId());
                result = workflowDefinitionMapper.updateById(workflowDefinitionLog);
            }
        }
        return (insertLog & result) > 0 ? insertVersion : 0;
    }

    /**
     * save task relations
     */
    @Override
    public int saveTaskRelation(User operator, long projectCode, long workflowDefinitionCode,
                                int workflowDefinitionVersion,
                                List<WorkflowTaskRelationLog> taskRelationList,
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
        for (WorkflowTaskRelationLog workflowTaskRelationLog : taskRelationList) {
            workflowTaskRelationLog.setProjectCode(projectCode);
            workflowTaskRelationLog.setWorkflowDefinitionCode(workflowDefinitionCode);
            workflowTaskRelationLog.setWorkflowDefinitionVersion(workflowDefinitionVersion);
            if (taskDefinitionLogMap != null) {
                TaskDefinitionLog preTaskDefinitionLog =
                        taskDefinitionLogMap.get(workflowTaskRelationLog.getPreTaskCode());
                if (preTaskDefinitionLog != null) {
                    workflowTaskRelationLog.setPreTaskVersion(preTaskDefinitionLog.getVersion());
                }
                TaskDefinitionLog postTaskDefinitionLog =
                        taskDefinitionLogMap.get(workflowTaskRelationLog.getPostTaskCode());
                if (postTaskDefinitionLog != null) {
                    workflowTaskRelationLog.setPostTaskVersion(postTaskDefinitionLog.getVersion());
                }
            }
            workflowTaskRelationLog.setCreateTime(now);
            workflowTaskRelationLog.setUpdateTime(now);
            workflowTaskRelationLog.setOperator(operator.getId());
            workflowTaskRelationLog.setOperateTime(now);
        }
        int insert = taskRelationList.size();
        if (Boolean.TRUE.equals(syncDefine)) {
            List<WorkflowTaskRelation> workflowTaskRelationList =
                    workflowTaskRelationMapper.queryByWorkflowDefinitionCode(workflowDefinitionCode);
            if (!workflowTaskRelationList.isEmpty()) {
                Set<Integer> workflowTaskRelationSet =
                        workflowTaskRelationList.stream().map(WorkflowTaskRelation::hashCode).collect(toSet());
                Set<Integer> taskRelationSet =
                        taskRelationList.stream().map(WorkflowTaskRelationLog::hashCode).collect(toSet());
                boolean result = CollectionUtils.isEqualCollection(workflowTaskRelationSet, taskRelationSet);
                if (result) {
                    return Constants.EXIT_CODE_SUCCESS;
                }
                workflowTaskRelationMapper.deleteByWorkflowDefinitionCode(projectCode, workflowDefinitionCode);
            }
            List<WorkflowTaskRelation> workflowTaskRelations =
                    taskRelationList.stream().map(WorkflowTaskRelation::new).collect(Collectors.toList());
            insert = workflowTaskRelationMapper.batchInsert(workflowTaskRelations);
        }
        int resultLog = workflowTaskRelationLogMapper.batchInsert(taskRelationList);
        return (insert & resultLog) > 0 ? Constants.EXIT_CODE_SUCCESS : Constants.EXIT_CODE_FAILURE;
    }

    @Override
    public boolean isTaskOnline(long taskCode) {
        List<WorkflowTaskRelation> workflowTaskRelationList = workflowTaskRelationMapper.queryByTaskCode(taskCode);
        if (!workflowTaskRelationList.isEmpty()) {
            Set<Long> processDefinitionCodes = workflowTaskRelationList
                    .stream()
                    .map(WorkflowTaskRelation::getWorkflowDefinitionCode)
                    .collect(toSet());
            List<WorkflowDefinition> workflowDefinitionList =
                    workflowDefinitionMapper.queryByCodes(processDefinitionCodes);
            // check process definition is already online
            for (WorkflowDefinition workflowDefinition : workflowDefinitionList) {
                if (workflowDefinition.getReleaseState() == ReleaseState.ONLINE) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generate the DAG Graph based on the workflow definition id
     * Use temporarily before refactoring taskNode
     *
     * @param workflowDefinition workflow definition
     * @return dag graph
     */
    @Override
    public DAG<Long, TaskNode, TaskNodeRelation> genDagGraph(WorkflowDefinition workflowDefinition) {
        List<WorkflowTaskRelation> taskRelations =
                this.findRelationByCode(workflowDefinition.getCode(), workflowDefinition.getVersion());
        List<TaskNode> taskNodeList = transformTask(taskRelations, Lists.newArrayList());
        WorkflowDag workflowDag = DagHelper.getWorkflowDag(taskNodeList, new ArrayList<>(taskRelations));
        // Generate concrete Dag to be executed
        return DagHelper.buildDagGraph(workflowDag);
    }

    /**
     * generate DagData
     */
    @Override
    public DagData genDagData(WorkflowDefinition workflowDefinition) {
        List<WorkflowTaskRelation> taskRelations =
                findRelationByCode(workflowDefinition.getCode(), workflowDefinition.getVersion());
        List<TaskDefinition> taskDefinitions = taskDefinitionLogDao.queryTaskDefineLogList(taskRelations)
                .stream()
                .map(t -> (TaskDefinition) t)
                .collect(Collectors.toList());
        return new DagData(workflowDefinition, taskRelations, taskDefinitions);
    }

    /**
     * find workflow task relation list by workflow
     */
    @Override
    public List<WorkflowTaskRelation> findRelationByCode(long workflowDefinitionCode, int workflowDefinitionVersion) {
        List<WorkflowTaskRelationLog> workflowTaskRelationLogList = workflowTaskRelationLogMapper
                .queryByWorkflowCodeAndVersion(workflowDefinitionCode, workflowDefinitionVersion);
        return workflowTaskRelationLogList.stream().map(r -> (WorkflowTaskRelation) r).collect(Collectors.toList());
    }

    /**
     * Use temporarily before refactoring taskNode
     */
    @Override
    public List<TaskNode> transformTask(List<WorkflowTaskRelation> taskRelationList,
                                        List<TaskDefinitionLog> taskDefinitionLogs) {
        Map<Long, List<Long>> taskCodeMap = new HashMap<>();
        for (WorkflowTaskRelation workflowTaskRelation : taskRelationList) {
            taskCodeMap.compute(workflowTaskRelation.getPostTaskCode(), (k, v) -> {
                if (v == null) {
                    v = new ArrayList<>();
                }
                if (workflowTaskRelation.getPreTaskCode() != 0L) {
                    v.add(workflowTaskRelation.getPreTaskCode());
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
                taskNode.setParams(taskDefinitionLog.getTaskParams());
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
                .workflowInstanceId(workflowInstanceId)
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
    public void forceWorkflowInstanceSuccessByTaskInstanceId(TaskInstance task) {
        WorkflowInstance workflowInstance = findWorkflowInstanceDetailById(task.getWorkflowInstanceId()).orElse(null);
        if (workflowInstance != null
                && (workflowInstance.getState().isFailure() || workflowInstance.getState().isStop())) {
            List<TaskInstance> validTaskList =
                    taskInstanceDao.queryValidTaskListByWorkflowInstanceId(workflowInstance.getId(),
                            workflowInstance.getTestFlag());
            List<Long> instanceTaskCodeList =
                    validTaskList.stream().map(TaskInstance::getTaskCode).collect(Collectors.toList());
            List<WorkflowTaskRelation> taskRelations = findRelationByCode(workflowInstance.getWorkflowDefinitionCode(),
                    workflowInstance.getWorkflowDefinitionVersion());
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
                if (failTaskList.size() == 1 && failTaskList.contains(task.getId())) {
                    workflowInstance.setStateWithDesc(WorkflowExecutionStatus.SUCCESS, "success by task force success");
                    workflowInstanceDao.updateById(workflowInstance);
                }
            }
        }
    }

}
