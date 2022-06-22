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

import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_EMPTY_SUB_PROCESS;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_FATHER_PARAMS;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_SUB_PROCESS;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_SUB_PROCESS_DEFINE_CODE;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_SUB_PROCESS_PARENT_INSTANCE_ID;
import static org.apache.dolphinscheduler.common.Constants.LOCAL_PARAMS;

import static java.util.stream.Collectors.toSet;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Direct;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.DateInterval;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.process.ProcessDag;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.task.TaskTimeoutParameter;
import org.apache.dolphinscheduler.common.task.subprocess.SubProcessParameters;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils.CodeGenerateException;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.TaskParametersUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.DagData;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.ErrorCommand;
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
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
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
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.utils.DagHelper;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.exceptions.ServiceException;
import org.apache.dolphinscheduler.service.log.LogClientService;
import org.apache.dolphinscheduler.service.quartz.cron.CronUtils;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;

/**
 * process relative dao that some mappers in this.
 */
@Component
public class ProcessService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int[] stateArray = new int[]{ExecutionStatus.SUBMITTED_SUCCESS.ordinal(),
            ExecutionStatus.RUNNING_EXECUTION.ordinal(),
            ExecutionStatus.DELAY_EXECUTION.ordinal(),
            ExecutionStatus.READY_PAUSE.ordinal(),
            ExecutionStatus.READY_STOP.ordinal()};

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProcessDefinitionMapper processDefineMapper;

    @Autowired
    private ProcessDefinitionLogMapper processDefineLogMapper;

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

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
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private TaskDefinitionLogMapper taskDefinitionLogMapper;

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Autowired
    private ProcessTaskRelationLogMapper processTaskRelationLogMapper;

    @Autowired
    private EnvironmentMapper environmentMapper;

    /**
     * handle Command (construct ProcessInstance from Command) , wrapped in transaction
     *
     * @param logger  logger
     * @param host    host
     * @param command found command
     * @return process instance
     */
    @Transactional
    public ProcessInstance handleCommand(Logger logger, String host, Command command) {
        ProcessInstance processInstance = constructProcessInstance(command, host);
        // cannot construct process instance, return null
        if (processInstance == null) {
            logger.error("scan command, command parameter is error: {}", command);
            moveToErrorCommand(command, "process instance is null");
            return null;
        }
        processInstance.setCommandType(command.getCommandType());
        processInstance.addHistoryCmd(command.getCommandType());
        saveProcessInstance(processInstance);
        this.setSubProcessParam(processInstance);
        this.deleteCommandWithCheck(command.getId());
        return processInstance;
    }

    /**
     * save error command, and delete original command
     *
     * @param command command
     * @param message message
     */
    public void moveToErrorCommand(Command command, String message) {
        ErrorCommand errorCommand = new ErrorCommand(command, message);
        this.errorCommandMapper.insert(errorCommand);
        this.commandMapper.deleteById(command.getId());
    }

    /**
     * set process waiting thread
     *
     * @param command         command
     * @param processInstance processInstance
     * @return process instance
     */
    private ProcessInstance setWaitingThreadProcess(Command command, ProcessInstance processInstance) {
        processInstance.setState(ExecutionStatus.WAITING_THREAD);
        if (command.getCommandType() != CommandType.RECOVER_WAITING_THREAD) {
            processInstance.addHistoryCmd(command.getCommandType());
        }
        saveProcessInstance(processInstance);
        this.setSubProcessParam(processInstance);
        createRecoveryWaitingThreadCommand(command, processInstance);
        return null;
    }

    /**
     * insert one command
     *
     * @param command command
     * @return create result
     */
    public int createCommand(Command command) {
        int result = 0;
        if (command != null) {
            result = commandMapper.insert(command);
        }
        return result;
    }

    /**
     * get command page
     */
    public List<Command> findCommandPage(int pageSize, int pageNumber) {
        return commandMapper.queryCommandPage(pageSize, pageNumber * pageSize);
    }

    /**
     * check the input command exists in queue list
     *
     * @param command command
     * @return create command result
     */
    public boolean verifyIsNeedCreateCommand(Command command) {
        boolean isNeedCreate = true;
        EnumMap<CommandType, Integer> cmdTypeMap = new EnumMap<>(CommandType.class);
        cmdTypeMap.put(CommandType.REPEAT_RUNNING, 1);
        cmdTypeMap.put(CommandType.RECOVER_SUSPENDED_PROCESS, 1);
        cmdTypeMap.put(CommandType.START_FAILURE_TASK_PROCESS, 1);
        CommandType commandType = command.getCommandType();

        if (cmdTypeMap.containsKey(commandType)) {
            ObjectNode cmdParamObj = JSONUtils.parseObject(command.getCommandParam());
            int processInstanceId = cmdParamObj.path(CMD_PARAM_RECOVER_PROCESS_ID_STRING).asInt();

            List<Command> commands = commandMapper.selectList(null);
            // for all commands
            for (Command tmpCommand : commands) {
                if (cmdTypeMap.containsKey(tmpCommand.getCommandType())) {
                    ObjectNode tempObj = JSONUtils.parseObject(tmpCommand.getCommandParam());
                    if (tempObj != null && processInstanceId == tempObj.path(CMD_PARAM_RECOVER_PROCESS_ID_STRING).asInt()) {
                        isNeedCreate = false;
                        break;
                    }
                }
            }
        }
        return isNeedCreate;
    }

    /**
     * find process instance detail by id
     *
     * @param processId processId
     * @return process instance
     */
    public ProcessInstance findProcessInstanceDetailById(int processId) {
        return processInstanceMapper.queryDetailById(processId);
    }

    /**
     * get task node list by definitionId
     */
    public List<TaskDefinition> getTaskNodeListByDefinition(long defineCode) {
        ProcessDefinition processDefinition = processDefineMapper.queryByCode(defineCode);
        if (processDefinition == null) {
            logger.error("process define not exists");
            return Lists.newArrayList();
        }
        List<ProcessTaskRelationLog> processTaskRelations = processTaskRelationLogMapper.queryByProcessCodeAndVersion(processDefinition.getCode(), processDefinition.getVersion());
        Set<TaskDefinition> taskDefinitionSet = new HashSet<>();
        for (ProcessTaskRelationLog processTaskRelation : processTaskRelations) {
            if (processTaskRelation.getPostTaskCode() > 0) {
                taskDefinitionSet.add(new TaskDefinition(processTaskRelation.getPostTaskCode(), processTaskRelation.getPostTaskVersion()));
            }
        }
        if (taskDefinitionSet.isEmpty()) {
            return Lists.newArrayList();
        }
        List<TaskDefinitionLog> taskDefinitionLogs = taskDefinitionLogMapper.queryByTaskDefinitions(taskDefinitionSet);
        return Lists.newArrayList(taskDefinitionLogs);
    }

    /**
     * find process instance by id
     *
     * @param processId processId
     * @return process instance
     */
    public ProcessInstance findProcessInstanceById(int processId) {
        return processInstanceMapper.selectById(processId);
    }

    /**
     * find process define by id.
     *
     * @param processDefinitionId processDefinitionId
     * @return process definition
     */
    public ProcessDefinition findProcessDefineById(int processDefinitionId) {
        return processDefineMapper.selectById(processDefinitionId);
    }

    /**
     * find process define by code and version.
     *
     * @param processDefinitionCode processDefinitionCode
     * @return process definition
     */
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
    public ProcessDefinition findProcessDefinitionByCode(Long processDefinitionCode) {
        return processDefineMapper.queryByCode(processDefinitionCode);
    }

    /**
     * delete work process instance by id
     *
     * @param processInstanceId processInstanceId
     * @return delete process instance result
     */
    public int deleteWorkProcessInstanceById(int processInstanceId) {
        return processInstanceMapper.deleteById(processInstanceId);
    }

    /**
     * delete all sub process by parent instance id
     *
     * @param processInstanceId processInstanceId
     * @return delete all sub process instance result
     */
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
    public void removeTaskLogFile(Integer processInstanceId) {
        List<TaskInstance> taskInstanceList = findValidTaskListByProcessId(processInstanceId);
        if (CollectionUtils.isEmpty(taskInstanceList)) {
            return;
        }
        try (LogClientService logClient = new LogClientService()) {
            for (TaskInstance taskInstance : taskInstanceList) {
                String taskLogPath = taskInstance.getLogPath();
                if (StringUtils.isEmpty(taskInstance.getHost())) {
                    continue;
                }
                int port = Constants.RPC_PORT;
                String ip = "";
                try {
                    ip = Host.of(taskInstance.getHost()).getIp();
                } catch (Exception e) {
                    // compatible old version
                    ip = taskInstance.getHost();
                }
                // remove task log from loggerserver
                logClient.removeTaskLog(ip, port, taskLogPath);
            }
        }
    }

    /**
     * recursive delete all task instance by process instance id
     *
     * @param processInstanceId
     */
    public void deleteWorkTaskInstanceByProcessInstanceId(int processInstanceId) {
        List<TaskInstance> taskInstanceList = findValidTaskListByProcessId(processInstanceId);
        if (CollectionUtils.isEmpty(taskInstanceList)) {
            return;
        }

        List<Integer> taskInstanceIdList = new ArrayList<>();

        for (TaskInstance taskInstance : taskInstanceList) {
            taskInstanceIdList.add(taskInstance.getId());
        }

        taskInstanceMapper.deleteBatchIds(taskInstanceIdList);
    }

    /**
     * recursive query sub process definition id by parent id.
     *
     * @param parentCode parentCode
     * @param ids        ids
     */
    public void recurseFindSubProcess(long parentCode, List<Long> ids) {
        List<TaskDefinition> taskNodeList = this.getTaskNodeListByDefinition(parentCode);

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
     * create recovery waiting thread command when thread pool is not enough for the process instance.
     * sub work process instance need not to create recovery command.
     * create recovery waiting thread  command and delete origin command at the same time.
     * if the recovery command is exists, only update the field update_time
     *
     * @param originCommand   originCommand
     * @param processInstance processInstance
     */
    public void createRecoveryWaitingThreadCommand(Command originCommand, ProcessInstance processInstance) {

        // sub process doesnot need to create wait command
        if (processInstance.getIsSubProcess() == Flag.YES) {
            if (originCommand != null) {
                commandMapper.deleteById(originCommand.getId());
            }
            return;
        }
        Map<String, String> cmdParam = new HashMap<>();
        cmdParam.put(Constants.CMD_PARAM_RECOVERY_WAITING_THREAD, String.valueOf(processInstance.getId()));
        // process instance quit by "waiting thread" state
        if (originCommand == null) {
            Command command = new Command(
                    CommandType.RECOVER_WAITING_THREAD,
                    processInstance.getTaskDependType(),
                    processInstance.getFailureStrategy(),
                    processInstance.getExecutorId(),
                    processInstance.getProcessDefinition().getCode(),
                    JSONUtils.toJsonString(cmdParam),
                    processInstance.getWarningType(),
                    processInstance.getWarningGroupId(),
                    processInstance.getScheduleTime(),
                    processInstance.getWorkerGroup(),
                    processInstance.getEnvironmentCode(),
                    processInstance.getProcessInstancePriority(),
                    processInstance.getDryRun(),
                    processInstance.getId(),
                    processInstance.getProcessDefinitionVersion()
            );
            saveCommand(command);
            return;
        }

        // update the command time if current command if recover from waiting
        if (originCommand.getCommandType() == CommandType.RECOVER_WAITING_THREAD) {
            originCommand.setUpdateTime(new Date());
            saveCommand(originCommand);
        } else {
            // delete old command and create new waiting thread command
            commandMapper.deleteById(originCommand.getId());
            originCommand.setId(0);
            originCommand.setCommandType(CommandType.RECOVER_WAITING_THREAD);
            originCommand.setUpdateTime(new Date());
            originCommand.setCommandParam(JSONUtils.toJsonString(cmdParam));
            originCommand.setProcessInstancePriority(processInstance.getProcessInstancePriority());
            saveCommand(originCommand);
        }
    }

    /**
     * get schedule time from command
     *
     * @param command  command
     * @param cmdParam cmdParam map
     * @return date
     */
    private Date getScheduleTime(Command command, Map<String, String> cmdParam) {
        Date scheduleTime = command.getScheduleTime();
        if (scheduleTime == null
                && cmdParam != null
                && cmdParam.containsKey(CMDPARAM_COMPLEMENT_DATA_START_DATE)) {

            Date start = DateUtils.stringToDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_START_DATE));
            Date end = DateUtils.stringToDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_END_DATE));
            List<Schedule> schedules = queryReleaseSchedulerListByProcessDefinitionCode(command.getProcessDefinitionCode());
            List<Date> complementDateList = CronUtils.getSelfFireDateList(start, end, schedules);

            if (complementDateList.size() > 0) {
                scheduleTime = complementDateList.get(0);
            } else {
                logger.error("set scheduler time error: complement date list is empty, command: {}",
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
        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        processInstance.setRecovery(Flag.NO);
        processInstance.setStartTime(new Date());
        processInstance.setRestartTime(processInstance.getStartTime());
        processInstance.setRunTimes(1);
        processInstance.setMaxTryTimes(0);
        processInstance.setCommandParam(command.getCommandParam());
        processInstance.setCommandType(command.getCommandType());
        processInstance.setIsSubProcess(Flag.NO);
        processInstance.setTaskDependType(command.getTaskDependType());
        processInstance.setFailureStrategy(command.getFailureStrategy());
        processInstance.setExecutorId(command.getExecutorId());
        WarningType warningType = command.getWarningType() == null ? WarningType.NONE : command.getWarningType();
        processInstance.setWarningType(warningType);
        Integer warningGroupId = command.getWarningGroupId() == null ? 0 : command.getWarningGroupId();
        processInstance.setWarningGroupId(warningGroupId);
        processInstance.setDryRun(command.getDryRun());

        if (command.getScheduleTime() != null) {
            processInstance.setScheduleTime(command.getScheduleTime());
        }
        processInstance.setCommandStartTime(command.getStartTime());
        processInstance.setLocations(processDefinition.getLocations());

        // reset global params while there are start parameters
        setGlobalParamIfCommanded(processDefinition, cmdParam);

        // curing global params
        processInstance.setGlobalParams(ParameterUtils.curingGlobalParams(
                processDefinition.getGlobalParamMap(),
                processDefinition.getGlobalParamList(),
                getCommandTypeIfComplement(processInstance, command),
                processInstance.getScheduleTime()));

        // set process instance priority
        processInstance.setProcessInstancePriority(command.getProcessInstancePriority());
        String workerGroup = StringUtils.isBlank(command.getWorkerGroup()) ? Constants.DEFAULT_WORKER_GROUP : command.getWorkerGroup();
        processInstance.setWorkerGroup(workerGroup);
        processInstance.setEnvironmentCode(Objects.isNull(command.getEnvironmentCode()) ? -1 : command.getEnvironmentCode());
        processInstance.setTimeout(processDefinition.getTimeout());
        processInstance.setTenantId(processDefinition.getTenantId());
        return processInstance;
    }

    private void setGlobalParamIfCommanded(ProcessDefinition processDefinition, Map<String, String> cmdParam) {
        // get start params from command param
        Map<String, String> startParamMap = new HashMap<>();
        if (cmdParam != null && cmdParam.containsKey(Constants.CMD_PARAM_START_PARAMS)) {
            String startParamJson = cmdParam.get(Constants.CMD_PARAM_START_PARAMS);
            startParamMap = JSONUtils.toMap(startParamJson);
        }
        Map<String, String> fatherParamMap = new HashMap<>();
        if (cmdParam != null && cmdParam.containsKey(Constants.CMD_PARAM_FATHER_PARAMS)) {
            String fatherParamJson = cmdParam.get(Constants.CMD_PARAM_FATHER_PARAMS);
            fatherParamMap = JSONUtils.toMap(fatherParamJson);
        }
        startParamMap.putAll(fatherParamMap);
        // set start param into global params
        if (startParamMap.size() > 0
                && processDefinition.getGlobalParamMap() != null) {
            for (Map.Entry<String, String> param : processDefinition.getGlobalParamMap().entrySet()) {
                String val = startParamMap.get(param.getKey());
                if (val != null) {
                    param.setValue(val);
                }
            }
        }
    }

    /**
     * get process tenant
     * there is tenant id in definition, use the tenant of the definition.
     * if there is not tenant id in the definiton or the tenant not exist
     * use definition creator's tenant.
     *
     * @param tenantId tenantId
     * @param userId   userId
     * @return tenant
     */
    public Tenant getTenantForProcess(int tenantId, int userId) {
        Tenant tenant = null;
        if (tenantId >= 0) {
            tenant = tenantMapper.queryById(tenantId);
        }

        if (userId == 0) {
            return null;
        }

        if (tenant == null) {
            User user = userMapper.selectById(userId);
            tenant = tenantMapper.queryById(user.getTenantId());
        }
        return tenant;
    }

    /**
     * get an environment
     * use the code of the environment to find a environment.
     *
     * @param environmentCode environmentCode
     * @return Environment
     */
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
        if (command.getTaskDependType() == TaskDependType.TASK_ONLY || command.getTaskDependType() == TaskDependType.TASK_PRE) {
            if (cmdParam == null
                    || !cmdParam.containsKey(Constants.CMD_PARAM_START_NODES)
                    || cmdParam.get(Constants.CMD_PARAM_START_NODES).isEmpty()) {
                logger.error("command node depend type is {}, but start nodes is null ", command.getTaskDependType());
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
    private ProcessInstance constructProcessInstance(Command command, String host) {
        ProcessInstance processInstance;
        ProcessDefinition processDefinition;
        CommandType commandType = command.getCommandType();

        processDefinition = this.findProcessDefinition(command.getProcessDefinitionCode(), command.getProcessDefinitionVersion());
        if (processDefinition == null) {
            logger.error("cannot find the work process define! define code : {}", command.getProcessDefinitionCode());
            return null;
        }
        Map<String, String> cmdParam = JSONUtils.toMap(command.getCommandParam());
        int processInstanceId = command.getProcessInstanceId();
        if (processInstanceId == 0) {
            processInstance = generateNewProcessInstance(processDefinition, command, cmdParam);
        } else {
            processInstance = this.findProcessInstanceDetailById(processInstanceId);
            if (processInstance == null) {
                return processInstance;
            }
        }
        if (cmdParam != null) {
            CommandType commandTypeIfComplement = getCommandTypeIfComplement(processInstance, command);
            // reset global params while repeat running is needed by cmdParam
            if (commandTypeIfComplement == CommandType.REPEAT_RUNNING) {
                setGlobalParamIfCommanded(processDefinition, cmdParam);
            }

            // Recalculate global parameters after rerun.
            processInstance.setGlobalParams(ParameterUtils.curingGlobalParams(
                    processDefinition.getGlobalParamMap(),
                    processDefinition.getGlobalParamList(),
                    commandTypeIfComplement,
                    processInstance.getScheduleTime()));
            processInstance.setProcessDefinition(processDefinition);
        }
        //reset command parameter
        if (processInstance.getCommandParam() != null) {
            Map<String, String> processCmdParam = JSONUtils.toMap(processInstance.getCommandParam());
            for (Map.Entry<String, String> entry : processCmdParam.entrySet()) {
                if (!cmdParam.containsKey(entry.getKey())) {
                    cmdParam.put(entry.getKey(), entry.getValue());
                }
            }
        }
        // reset command parameter if sub process
        if (cmdParam != null && cmdParam.containsKey(Constants.CMD_PARAM_SUB_PROCESS)) {
            processInstance.setCommandParam(command.getCommandParam());
        }
        if (Boolean.FALSE.equals(checkCmdParam(command, cmdParam))) {
            logger.error("command parameter check failed!");
            return null;
        }
        if (command.getScheduleTime() != null) {
            processInstance.setScheduleTime(command.getScheduleTime());
        }
        processInstance.setHost(host);
        processInstance.setRestartTime(new Date());
        ExecutionStatus runStatus = ExecutionStatus.RUNNING_EXECUTION;
        int runTime = processInstance.getRunTimes();
        switch (commandType) {
            case START_PROCESS:
                break;
            case START_FAILURE_TASK_PROCESS:
                // find failed tasks and init these tasks
                List<Integer> failedList = this.findTaskIdByInstanceState(processInstance.getId(), ExecutionStatus.FAILURE);
                List<Integer> toleranceList = this.findTaskIdByInstanceState(processInstance.getId(), ExecutionStatus.NEED_FAULT_TOLERANCE);
                List<Integer> killedList = this.findTaskIdByInstanceState(processInstance.getId(), ExecutionStatus.KILL);
                cmdParam.remove(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING);

                failedList.addAll(killedList);
                failedList.addAll(toleranceList);
                for (Integer taskId : failedList) {
                    initTaskInstance(this.findTaskInstanceById(taskId));
                }
                cmdParam.put(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING,
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
                cmdParam.remove(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING);
                List<Integer> suspendedNodeList = this.findTaskIdByInstanceState(processInstance.getId(), ExecutionStatus.PAUSE);
                List<Integer> stopNodeList = findTaskIdByInstanceState(processInstance.getId(),
                        ExecutionStatus.KILL);
                suspendedNodeList.addAll(stopNodeList);
                for (Integer taskId : suspendedNodeList) {
                    // initialize the pause state
                    initTaskInstance(this.findTaskInstanceById(taskId));
                }
                cmdParam.put(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING, String.join(",", convertIntListToString(suspendedNodeList)));
                processInstance.setCommandParam(JSONUtils.toJsonString(cmdParam));
                processInstance.setRunTimes(runTime + 1);
                break;
            case RECOVER_TOLERANCE_FAULT_PROCESS:
                // recover tolerance fault process
                processInstance.setRecovery(Flag.YES);
                runStatus = processInstance.getState();
                break;
            case COMPLEMENT_DATA:
                // delete all the valid tasks when complement data if id is not null
                if (processInstance.getId() != 0) {
                    List<TaskInstance> taskInstanceList = this.findValidTaskListByProcessId(processInstance.getId());
                    for (TaskInstance taskInstance : taskInstanceList) {
                        taskInstance.setFlag(Flag.NO);
                        this.updateTaskInstance(taskInstance);
                    }
                }
                break;
            case REPEAT_RUNNING:
                // delete the recover task names from command parameter
                if (cmdParam.containsKey(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING)) {
                    cmdParam.remove(Constants.CMD_PARAM_RECOVERY_START_NODE_STRING);
                    processInstance.setCommandParam(JSONUtils.toJsonString(cmdParam));
                }
                // delete all the valid tasks when repeat running
                List<TaskInstance> validTaskList = findValidTaskListByProcessId(processInstance.getId());
                for (TaskInstance taskInstance : validTaskList) {
                    taskInstance.setFlag(Flag.NO);
                    updateTaskInstance(taskInstance);
                }
                processInstance.setStartTime(new Date());
                processInstance.setRestartTime(processInstance.getStartTime());
                processInstance.setEndTime(null);
                processInstance.setRunTimes(runTime + 1);
                initComplementDataParam(processDefinition, processInstance, cmdParam);
                break;
            case SCHEDULER:
                break;
            default:
                break;
        }
        processInstance.setState(runStatus);
        return processInstance;
    }

    /**
     * get process definition by command
     * If it is a fault-tolerant command, get the specified version of ProcessDefinition through ProcessInstance
     * Otherwise, get the latest version of ProcessDefinition
     *
     * @return ProcessDefinition
     */
    private ProcessDefinition getProcessDefinitionByCommand(long processDefinitionCode, Map<String, String> cmdParam) {
        if (cmdParam != null) {
            int processInstanceId = 0;
            if (cmdParam.containsKey(Constants.CMD_PARAM_RECOVER_PROCESS_ID_STRING)) {
                processInstanceId = Integer.parseInt(cmdParam.get(Constants.CMD_PARAM_RECOVER_PROCESS_ID_STRING));
            } else if (cmdParam.containsKey(Constants.CMD_PARAM_SUB_PROCESS)) {
                processInstanceId = Integer.parseInt(cmdParam.get(Constants.CMD_PARAM_SUB_PROCESS));
            } else if (cmdParam.containsKey(Constants.CMD_PARAM_RECOVERY_WAITING_THREAD)) {
                processInstanceId = Integer.parseInt(cmdParam.get(Constants.CMD_PARAM_RECOVERY_WAITING_THREAD));
            }

            if (processInstanceId != 0) {
                ProcessInstance processInstance = this.findProcessInstanceDetailById(processInstanceId);
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
                                         Map<String, String> cmdParam) {
        if (!processInstance.isComplementData()) {
            return;
        }

        Date start = DateUtils.stringToDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_START_DATE));
        Date end = DateUtils.stringToDate(cmdParam.get(CMDPARAM_COMPLEMENT_DATA_END_DATE));
        List<Schedule> listSchedules = queryReleaseSchedulerListByProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        List<Date> complementDate = CronUtils.getSelfFireDateList(start, end, listSchedules);

        if (complementDate.size() > 0
                && Flag.NO == processInstance.getIsSubProcess()) {
            processInstance.setScheduleTime(complementDate.get(0));
        }
        processInstance.setGlobalParams(ParameterUtils.curingGlobalParams(
                processDefinition.getGlobalParamMap(),
                processDefinition.getGlobalParamList(),
                CommandType.COMPLEMENT_DATA, processInstance.getScheduleTime()));
    }

    /**
     * set sub work process parameters.
     * handle sub work process instance, update relation table and command parameters
     * set sub work process flag, extends parent work process command parameters
     *
     * @param subProcessInstance subProcessInstance
     */
    public void setSubProcessParam(ProcessInstance subProcessInstance) {
        String cmdParam = subProcessInstance.getCommandParam();
        if (StringUtils.isEmpty(cmdParam)) {
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
            this.saveProcessInstance(subProcessInstance);
        }
        // copy parent instance user def params to sub process..
        String parentInstanceId = paramMap.get(CMD_PARAM_SUB_PROCESS_PARENT_INSTANCE_ID);
        if (StringUtils.isNotEmpty(parentInstanceId)) {
            ProcessInstance parentInstance = findProcessInstanceDetailById(Integer.parseInt(parentInstanceId));
            if (parentInstance != null) {
                subProcessInstance.setGlobalParams(
                        joinGlobalParams(parentInstance.getGlobalParams(), subProcessInstance.getGlobalParams()));
                this.saveProcessInstance(subProcessInstance);
            } else {
                logger.error("sub process command params error, cannot find parent instance: {} ", cmdParam);
            }
        }
        ProcessInstanceMap processInstanceMap = JSONUtils.parseObject(cmdParam, ProcessInstanceMap.class);
        if (processInstanceMap == null || processInstanceMap.getParentProcessInstanceId() == 0) {
            return;
        }
        // update sub process id to process map table
        processInstanceMap.setProcessInstanceId(subProcessInstance.getId());

        this.updateWorkProcessInstanceMap(processInstanceMap);

        TaskInstance subTask = this.findTaskInstanceById(processInstanceMap.getParentTaskInstanceId());
        if (subTask != null && subTask.isSubProcess() && subTask.getState() == ExecutionStatus.SUBMITTED_SUCCESS) {
            subTask.setState(ExecutionStatus.RUNNING_EXECUTION);
            subTask.setStartTime(new Date());
            this.updateTaskInstance(subTask);
        }
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
        //      e.g. the subProp's type is not equals with parent, or subProp's direct is not equals with parent
        //      It's suggested to add node name in property, this kind of problem can be solved.
        List<Property> extraSubParams = subParams.stream()
                .filter(subProp -> !parentParamKeys.contains(subProp.getProp())).collect(Collectors.toList());
        parentParams.addAll(extraSubParams);
        return JSONUtils.toJsonString(parentParams);
    }

    /**
     * initialize task instance
     *
     * @param taskInstance taskInstance
     */
    private void initTaskInstance(TaskInstance taskInstance) {

        if (!taskInstance.isSubProcess()
                && (taskInstance.getState().typeIsCancel() || taskInstance.getState().typeIsFailure())) {
            taskInstance.setFlag(Flag.NO);
            updateTaskInstance(taskInstance);
            return;
        }
        taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
        updateTaskInstance(taskInstance);
        logger.debug("update task instance, task instance id:{}", taskInstance.getId());
    }

    /**
     * retry submit task to db
     */
    public TaskInstance submitTask(TaskInstance taskInstance, int commitRetryTimes, int commitInterval) {

        int retryTimes = 1;
        boolean submitDB = false;
        TaskInstance task = null;
        while (retryTimes <= commitRetryTimes) {
            try {
                if (!submitDB) {
                    // submit task to db
                    task = submitTask(taskInstance);
                    if (task != null && task.getId() != 0) {
                        submitDB = true;
                        break;
                    }
                }
                if (!submitDB) {
                    logger.error("task commit to db failed , taskId {} has already retry {} times, please check the database", taskInstance.getId(), retryTimes);
                }
                Thread.sleep(commitInterval);
            } catch (Exception e) {
                logger.error("task commit to db failed", e);
            }
            retryTimes += 1;
        }
        return task;
    }

    /**
     * submit task to db
     * submit sub process to command
     *
     * @param taskInstance taskInstance
     * @return task instance
     */
    @Transactional(rollbackFor = Exception.class)
    public TaskInstance submitTask(TaskInstance taskInstance) {
        ProcessInstance processInstance = this.findProcessInstanceDetailById(taskInstance.getProcessInstanceId());
        logger.info("start submit task : {}, instance id:{}, state: {}",
                taskInstance.getName(), taskInstance.getProcessInstanceId(), processInstance.getState());
        //submit to db
        TaskInstance task = submitTaskInstanceToDB(taskInstance, processInstance);
        if (task == null) {
            logger.error("end submit task to db error, task name:{}, process id:{} state: {} ",
                    taskInstance.getName(), taskInstance.getProcessInstance(), processInstance.getState());
            return task;
        }
        if (!task.getState().typeIsFinished()) {
            createSubWorkProcess(processInstance, task);
        }

        logger.info("end submit task to db successfully:{} {} state:{} complete, instance id:{} state: {}  ",
                taskInstance.getId(), taskInstance.getName(), task.getState(), processInstance.getId(), processInstance.getState());
        return task;
    }

    /**
     * set work process instance map
     * consider o
     * repeat running  does not generate new sub process instance
     * set map {parent instance id, task instance id, 0(child instance id)}
     *
     * @param parentInstance parentInstance
     * @param parentTask     parentTask
     * @return process instance map
     */
    private ProcessInstanceMap setProcessInstanceMap(ProcessInstance parentInstance, TaskInstance parentTask) {
        ProcessInstanceMap processMap = findWorkProcessMapByParent(parentInstance.getId(), parentTask.getId());
        if (processMap != null) {
            return processMap;
        }
        if (parentInstance.getCommandType() == CommandType.REPEAT_RUNNING) {
            // update current task id to map
            processMap = findPreviousTaskProcessMap(parentInstance, parentTask);
            if (processMap != null) {
                processMap.setParentTaskInstanceId(parentTask.getId());
                updateWorkProcessInstanceMap(processMap);
                return processMap;
            }
        }
        // new task
        processMap = new ProcessInstanceMap();
        processMap.setParentProcessInstanceId(parentInstance.getId());
        processMap.setParentTaskInstanceId(parentTask.getId());
        createWorkProcessInstanceMap(processMap);
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
        List<TaskInstance> preTaskList = this.findPreviousTaskListByWorkProcessId(parentProcessInstance.getId());
        for (TaskInstance task : preTaskList) {
            if (task.getName().equals(parentTask.getName())) {
                preTaskId = task.getId();
                ProcessInstanceMap map = findWorkProcessMapByParent(parentProcessInstance.getId(), preTaskId);
                if (map != null) {
                    return map;
                }
            }
        }
        logger.info("sub process instance is not found,parent task:{},parent instance:{}",
                parentTask.getId(), parentProcessInstance.getId());
        return null;
    }

    /**
     * create sub work process command
     *
     * @param parentProcessInstance parentProcessInstance
     * @param task                  task
     */
    public void createSubWorkProcess(ProcessInstance parentProcessInstance, TaskInstance task) {
        if (!task.isSubProcess()) {
            return;
        }
        //check create sub work flow firstly
        ProcessInstanceMap instanceMap = findWorkProcessMapByParent(parentProcessInstance.getId(), task.getId());
        if (null != instanceMap && CommandType.RECOVER_TOLERANCE_FAULT_PROCESS == parentProcessInstance.getCommandType()) {
            // recover failover tolerance would not create a new command when the sub command already have been created
            return;
        }
        instanceMap = setProcessInstanceMap(parentProcessInstance, task);
        ProcessInstance childInstance = null;
        if (instanceMap.getProcessInstanceId() != 0) {
            childInstance = findProcessInstanceById(instanceMap.getProcessInstanceId());
        }
        Command subProcessCommand = createSubProcessCommand(parentProcessInstance, childInstance, instanceMap, task);
        updateSubProcessDefinitionByParent(parentProcessInstance, subProcessCommand.getProcessDefinitionCode());
        initSubInstanceState(childInstance);
        createCommand(subProcessCommand);
        logger.info("sub process command created: {} ", subProcessCommand);
    }

    /**
     * complement data needs transform parent parameter to child.
     */
    private String getSubWorkFlowParam(ProcessInstanceMap instanceMap, ProcessInstance parentProcessInstance, Map<String, String> fatherParams) {
        // set sub work process command
        String processMapStr = JSONUtils.toJsonString(instanceMap);
        Map<String, String> cmdParam = JSONUtils.toMap(processMapStr);
        if (parentProcessInstance.isComplementData()) {
            Map<String, String> parentParam = JSONUtils.toMap(parentProcessInstance.getCommandParam());
            String endTime = parentParam.get(CMDPARAM_COMPLEMENT_DATA_END_DATE);
            String startTime = parentParam.get(CMDPARAM_COMPLEMENT_DATA_START_DATE);
            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE, endTime);
            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE, startTime);
            processMapStr = JSONUtils.toJsonString(cmdParam);
        }
        if (fatherParams.size() != 0) {
            cmdParam.put(CMD_PARAM_FATHER_PARAMS, JSONUtils.toJsonString(fatherParams));
            processMapStr = JSONUtils.toJsonString(cmdParam);
        }
        return processMapStr;
    }

    public Map<String, String> getGlobalParamMap(String globalParams) {
        List<Property> propList;
        Map<String, String> globalParamMap = new HashMap<>();
        if (StringUtils.isNotEmpty(globalParams)) {
            propList = JSONUtils.toList(globalParams, Property.class);
            globalParamMap = propList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
        }

        return globalParamMap;
    }

    /**
     * create sub work process command
     */
    public Command createSubProcessCommand(ProcessInstance parentProcessInstance,
                                           ProcessInstance childInstance,
                                           ProcessInstanceMap instanceMap,
                                           TaskInstance task) {
        CommandType commandType = getSubCommandType(parentProcessInstance, childInstance);
        Map<String, String> subProcessParam = JSONUtils.toMap(task.getTaskParams());
        long childDefineCode = 0L;
        if (subProcessParam.containsKey(Constants.CMD_PARAM_SUB_PROCESS_DEFINE_CODE)) {
            childDefineCode = Long.parseLong(subProcessParam.get(Constants.CMD_PARAM_SUB_PROCESS_DEFINE_CODE));
        }
        ProcessDefinition subProcessDefinition = processDefineMapper.queryByCode(childDefineCode);

        Object localParams = subProcessParam.get(Constants.LOCAL_PARAMS);
        List<Property> allParam = JSONUtils.toList(JSONUtils.toJsonString(localParams), Property.class);
        Map<String, String> globalMap = this.getGlobalParamMap(parentProcessInstance.getGlobalParams());
        Map<String, String> fatherParams = new HashMap<>();
        if (CollectionUtils.isNotEmpty(allParam)) {
            for (Property info : allParam) {
                fatherParams.put(info.getProp(), globalMap.get(info.getProp()));
            }
        }
        String processParam = getSubWorkFlowParam(instanceMap, parentProcessInstance, fatherParams);
        int subProcessInstanceId = childInstance == null ? 0 : childInstance.getId();
        return new Command(
                commandType,
                TaskDependType.TASK_POST,
                parentProcessInstance.getFailureStrategy(),
                parentProcessInstance.getExecutorId(),
                subProcessDefinition.getCode(),
                processParam,
                parentProcessInstance.getWarningType(),
                parentProcessInstance.getWarningGroupId(),
                parentProcessInstance.getScheduleTime(),
                task.getWorkerGroup(),
                task.getEnvironmentCode(),
                parentProcessInstance.getProcessInstancePriority(),
                parentProcessInstance.getDryRun(),
                subProcessInstanceId,
                subProcessDefinition.getVersion()
        );
    }

    /**
     * initialize sub work flow state
     * child instance state would be initialized when 'recovery from pause/stop/failure'
     */
    private void initSubInstanceState(ProcessInstance childInstance) {
        if (childInstance != null) {
            childInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
            updateProcessInstance(childInstance);
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
        ProcessDefinition fatherDefinition = this.findProcessDefinition(parentProcessInstance.getProcessDefinitionCode(),
                parentProcessInstance.getProcessDefinitionVersion());
        ProcessDefinition childDefinition = this.findProcessDefinitionByCode(childDefinitionCode);
        if (childDefinition != null && fatherDefinition != null) {
            childDefinition.setWarningGroupId(fatherDefinition.getWarningGroupId());
            processDefineMapper.updateById(childDefinition);
        }
    }

    /**
     * submit task to mysql
     *
     * @param taskInstance    taskInstance
     * @param processInstance processInstance
     * @return task instance
     */
    public TaskInstance submitTaskInstanceToDB(TaskInstance taskInstance, ProcessInstance processInstance) {
        ExecutionStatus processInstanceState = processInstance.getState();
        if (processInstanceState.typeIsFinished() || processInstanceState == ExecutionStatus.READY_STOP) {
            logger.warn("processInstance {} was {}, skip submit task", processInstance.getProcessDefinitionCode(), processInstanceState);
            return null;
        }
        if (processInstanceState == ExecutionStatus.READY_PAUSE) {
            taskInstance.setState(ExecutionStatus.PAUSE);
        }
        taskInstance.setProcessInstancePriority(processInstance.getProcessInstancePriority());
        taskInstance.setExecutorId(processInstance.getExecutorId());
        taskInstance.setState(getSubmitTaskState(taskInstance, processInstanceState));
        if (taskInstance.getSubmitTime() == null) {
            taskInstance.setSubmitTime(new Date());
        }
        if (taskInstance.getFirstSubmitTime() == null) {
            taskInstance.setFirstSubmitTime(taskInstance.getSubmitTime());
        }
        boolean saveResult = saveTaskInstance(taskInstance);
        if (!saveResult) {
            return null;
        }
        return taskInstance;
    }

    /**
     * get submit task instance state by the work process state
     * cannot modify the task state when running/kill/submit success, or this
     * task instance is already exists in task queue .
     * return pause if work process state is ready pause
     * return stop if work process state is ready stop
     * if all of above are not satisfied, return submit success
     *
     * @param taskInstance         taskInstance
     * @param processInstanceState processInstanceState
     * @return process instance state
     */
    public ExecutionStatus getSubmitTaskState(TaskInstance taskInstance, ExecutionStatus processInstanceState) {
        ExecutionStatus state = taskInstance.getState();
        // running, delayed or killed
        // the task already exists in task queue
        // return state
        if (
                state == ExecutionStatus.RUNNING_EXECUTION
                        || state == ExecutionStatus.DELAY_EXECUTION
                        || state == ExecutionStatus.KILL
        ) {
            return state;
        }
        //return pasue /stop if process instance state is ready pause / stop
        // or return submit success
        if (processInstanceState == ExecutionStatus.READY_PAUSE) {
            state = ExecutionStatus.PAUSE;
        } else if (processInstanceState == ExecutionStatus.READY_STOP
                || !checkProcessStrategy(taskInstance)) {
            state = ExecutionStatus.KILL;
        } else {
            state = ExecutionStatus.SUBMITTED_SUCCESS;
        }
        return state;
    }

    /**
     * check process instance strategy
     *
     * @param taskInstance taskInstance
     * @return check strategy result
     */
    private boolean checkProcessStrategy(TaskInstance taskInstance) {
        ProcessInstance processInstance = this.findProcessInstanceById(taskInstance.getProcessInstanceId());
        FailureStrategy failureStrategy = processInstance.getFailureStrategy();
        if (failureStrategy == FailureStrategy.CONTINUE) {
            return true;
        }
        List<TaskInstance> taskInstances = this.findValidTaskListByProcessId(taskInstance.getProcessInstanceId());

        for (TaskInstance task : taskInstances) {
            if (task.getState() == ExecutionStatus.FAILURE
                    && task.getRetryTimes() >= task.getMaxRetryTimes()) {
                return false;
            }
        }
        return true;
    }

    /**
     * insert or update work process instance to data base
     *
     * @param processInstance processInstance
     */
    public void saveProcessInstance(ProcessInstance processInstance) {
        if (processInstance == null) {
            logger.error("save error, process instance is null!");
            return;
        }
        if (processInstance.getId() != 0) {
            processInstanceMapper.updateById(processInstance);
        } else {
            processInstanceMapper.insert(processInstance);
        }
    }

    /**
     * insert or update command
     *
     * @param command command
     * @return save command result
     */
    public int saveCommand(Command command) {
        if (command.getId() != 0) {
            return commandMapper.updateById(command);
        } else {
            return commandMapper.insert(command);
        }
    }

    /**
     * insert or update task instance
     *
     * @param taskInstance taskInstance
     * @return save task instance result
     */
    public boolean saveTaskInstance(TaskInstance taskInstance) {
        if (taskInstance.getId() != 0) {
            return updateTaskInstance(taskInstance);
        } else {
            return createTaskInstance(taskInstance);
        }
    }

    public boolean updateHostAndSubmitTimeById(int id, String host, Date date) {
        int count = taskInstanceMapper.updateHostAndSubmitTimeById(id, host, date);
        return count > 0;
    }

    /**
     * insert task instance
     *
     * @param taskInstance taskInstance
     * @return create task instance result
     */
    public boolean createTaskInstance(TaskInstance taskInstance) {
        int count = taskInstanceMapper.insert(taskInstance);
        return count > 0;
    }

    /**
     * update task instance
     *
     * @param taskInstance taskInstance
     * @return update task instance result
     */
    public boolean updateTaskInstance(TaskInstance taskInstance) {
        int count = taskInstanceMapper.updateById(taskInstance);
        logger.debug("updateTaskInstance, task instance id:{}, state;{}", taskInstance.getId(), taskInstance.getState());
        return count > 0;
    }

    /**
     * find task instance by id
     *
     * @param taskId task id
     * @return task intance
     */
    public TaskInstance findTaskInstanceById(Integer taskId) {
        return taskInstanceMapper.selectById(taskId);
    }

    /**
     * package task instance，associate processInstance and processDefine
     *
     * @param taskInstId taskInstId
     * @return task instance
     */
    public TaskInstance getTaskInstanceDetailByTaskId(int taskInstId) {
        // get task instance
        TaskInstance taskInstance = findTaskInstanceById(taskInstId);
        if (taskInstance == null) {
            return null;
        }
        setTaskInstanceDetail(taskInstance);
        return taskInstance;
    }

    /**
     * package task instance，associate processInstance and processDefine
     *
     * @param taskInstance taskInstance
     * @return task instance
     */
    public void setTaskInstanceDetail(TaskInstance taskInstance) {
        // get process instance
        ProcessInstance processInstance = findProcessInstanceDetailById(taskInstance.getProcessInstanceId());
        // get process define
        ProcessDefinition processDefine = findProcessDefinition(processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion());
        taskInstance.setProcessInstance(processInstance);
        taskInstance.setProcessDefine(processDefine);
        TaskDefinition taskDefinition = taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(
                taskInstance.getTaskCode(),
                taskInstance.getTaskDefinitionVersion());
        updateTaskDefinitionResources(taskDefinition);
        taskInstance.setTaskDefine(taskDefinition);
    }

    /**
     * Update {@link ResourceInfo} information in {@link TaskDefinition}
     *
     * @param taskDefinition the given {@link TaskDefinition}
     */
    private void updateTaskDefinitionResources(TaskDefinition taskDefinition) {
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
                ResourceInfo resourceInfo = updateResourceInfo(mainJar);
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
                        .map(this::updateResourceInfo)
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
    private ResourceInfo updateResourceInfo(ResourceInfo res) {
        ResourceInfo resourceInfo = null;
        // only if mainJar is not null and does not contains "resourceName" field
        if (res != null) {
            int resourceId = res.getId();
            if (resourceId <= 0) {
                logger.error("invalid resourceId, {}", resourceId);
                return null;
            }
            resourceInfo = new ResourceInfo();
            // get resource from database, only one resource should be returned
            Resource resource = getResourceById(resourceId);
            resourceInfo.setId(resourceId);
            resourceInfo.setRes(resource.getFileName());
            resourceInfo.setResourceName(resource.getFullName());
            if (logger.isInfoEnabled()) {
                logger.info("updated resource info {}",
                        JSONUtils.toJsonString(resourceInfo));
            }
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
    public List<Integer> findTaskIdByInstanceState(int instanceId, ExecutionStatus state) {
        return taskInstanceMapper.queryTaskByProcessIdAndState(instanceId, state.ordinal());
    }

    /**
     * find valid task list by process definition id
     *
     * @param processInstanceId processInstanceId
     * @return task instance list
     */
    public List<TaskInstance> findValidTaskListByProcessId(Integer processInstanceId) {
        return taskInstanceMapper.findValidTaskListByProcessId(processInstanceId, Flag.YES);
    }

    /**
     * find previous task list by work process id
     *
     * @param processInstanceId processInstanceId
     * @return task instance list
     */
    public List<TaskInstance> findPreviousTaskListByWorkProcessId(Integer processInstanceId) {
        return taskInstanceMapper.findValidTaskListByProcessId(processInstanceId, Flag.NO);
    }

    /**
     * update work process instance map
     *
     * @param processInstanceMap processInstanceMap
     * @return update process instance result
     */
    public int updateWorkProcessInstanceMap(ProcessInstanceMap processInstanceMap) {
        return processInstanceMapMapper.updateById(processInstanceMap);
    }

    /**
     * create work process instance map
     *
     * @param processInstanceMap processInstanceMap
     * @return create process instance result
     */
    public int createWorkProcessInstanceMap(ProcessInstanceMap processInstanceMap) {
        int count = 0;
        if (processInstanceMap != null) {
            return processInstanceMapMapper.insert(processInstanceMap);
        }
        return count;
    }

    /**
     * find work process map by parent process id and parent task id.
     *
     * @param parentWorkProcessId parentWorkProcessId
     * @param parentTaskId        parentTaskId
     * @return process instance map
     */
    public ProcessInstanceMap findWorkProcessMapByParent(Integer parentWorkProcessId, Integer parentTaskId) {
        return processInstanceMapMapper.queryByParentId(parentWorkProcessId, parentTaskId);
    }

    /**
     * delete work process map by parent process id
     *
     * @param parentWorkProcessId parentWorkProcessId
     * @return delete process map result
     */
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
     * change task state
     *
     * @param state       state
     * @param startTime   startTime
     * @param host        host
     * @param executePath executePath
     * @param logPath     logPath
     * @param taskInstId  taskInstId
     * @reutrn
     */
    public boolean changeTaskState(TaskInstance taskInstance, ExecutionStatus state, Date startTime, String host,
                                   String executePath,
                                   String logPath,
                                   int taskInstId) {
        taskInstance.setState(state);
        taskInstance.setStartTime(startTime);
        taskInstance.setHost(host);
        taskInstance.setExecutePath(executePath);
        taskInstance.setLogPath(logPath);
        return saveTaskInstance(taskInstance);
    }

    /**
     * update process instance
     *
     * @param processInstance processInstance
     * @return update process instance result
     */
    public int updateProcessInstance(ProcessInstance processInstance) {
        return processInstanceMapper.updateById(processInstance);
    }

    /**
     * change task state
     *
     * @param state      state
     * @param endTime    endTime
     * @param taskInstId taskInstId
     * @param varPool    varPool
     * @return
     */
    public boolean changeTaskState(TaskInstance taskInstance, ExecutionStatus state,
                                   Date endTime,
                                   int processId,
                                   String appIds,
                                   int taskInstId,
                                   String varPool) {
        taskInstance.setPid(processId);
        taskInstance.setAppLink(appIds);
        taskInstance.setState(state);
        taskInstance.setEndTime(endTime);
        taskInstance.setVarPool(varPool);
        changeOutParam(taskInstance);
        return saveTaskInstance(taskInstance);
    }

    /**
     * for show in page of taskInstance
     */
    public void changeOutParam(TaskInstance taskInstance) {
        if (StringUtils.isEmpty(taskInstance.getVarPool())) {
            return;
        }
        List<Property> properties = JSONUtils.toList(taskInstance.getVarPool(), Property.class);
        if (CollectionUtils.isEmpty(properties)) {
            return;
        }
        //if the result more than one line,just get the first .
        Map<String, Object> taskParams = JSONUtils.parseObject(taskInstance.getTaskParams(), new TypeReference<Map<String, Object>>() {
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
    public List<String> convertIntListToString(List<Integer> intList) {
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
    public Schedule querySchedule(int id) {
        return scheduleMapper.selectById(id);
    }

    /**
     * query Schedule by processDefinitionCode
     *
     * @param processDefinitionCode processDefinitionCode
     * @see Schedule
     */
    public List<Schedule> queryReleaseSchedulerListByProcessDefinitionCode(long processDefinitionCode) {
        return scheduleMapper.queryReleaseSchedulerListByProcessDefinitionCode(processDefinitionCode);
    }

    /**
     * query need failover process instance
     *
     * @param host host
     * @return process instance list
     */
    public List<ProcessInstance> queryNeedFailoverProcessInstances(String host) {
        return processInstanceMapper.queryByHostAndStatus(host, stateArray);
    }

    public List<String> queryNeedFailoverProcessInstanceHost() {
        return processInstanceMapper.queryNeedFailoverProcessInstanceHost(stateArray);
    }

    /**
     * process need failover process instance
     *
     * @param processInstance processInstance
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public void processNeedFailoverProcessInstances(ProcessInstance processInstance) {
        //1 update processInstance host is null
        processInstance.setHost(Constants.NULL);
        processInstanceMapper.updateById(processInstance);

        ProcessDefinition processDefinition = findProcessDefinition(processInstance.getProcessDefinitionCode(), processInstance.getProcessDefinitionVersion());

        //2 insert into recover command
        Command cmd = new Command();
        cmd.setProcessDefinitionCode(processDefinition.getCode());
        cmd.setProcessDefinitionVersion(processDefinition.getVersion());
        cmd.setProcessInstanceId(processInstance.getId());
        cmd.setCommandParam(String.format("{\"%s\":%d}", Constants.CMD_PARAM_RECOVER_PROCESS_ID_STRING, processInstance.getId()));
        cmd.setExecutorId(processInstance.getExecutorId());
        cmd.setCommandType(CommandType.RECOVER_TOLERANCE_FAULT_PROCESS);
        createCommand(cmd);
    }

    /**
     * query all need failover task instances by host
     *
     * @param host host
     * @return task instance list
     */
    public List<TaskInstance> queryNeedFailoverTaskInstances(String host) {
        return taskInstanceMapper.queryByHostAndStatus(host,
                stateArray);
    }

    /**
     * find data source by id
     *
     * @param id id
     * @return datasource
     */
    public DataSource findDataSourceById(int id) {
        return dataSourceMapper.selectById(id);
    }

    /**
     * update process instance state by id
     *
     * @param processInstanceId processInstanceId
     * @param executionStatus   executionStatus
     * @return update process result
     */
    public int updateProcessInstanceState(Integer processInstanceId, ExecutionStatus executionStatus) {
        ProcessInstance instance = processInstanceMapper.selectById(processInstanceId);
        instance.setState(executionStatus);
        return processInstanceMapper.updateById(instance);
    }

    /**
     * find process instance by the task id
     *
     * @param taskId taskId
     * @return process instance
     */
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
    public List<UdfFunc> queryUdfFunListByIds(int[] ids) {
        return udfFuncMapper.queryUdfByIdStr(ids, null);
    }

    /**
     * find tenant code by resource name
     *
     * @param resName      resource name
     * @param resourceType resource type
     * @return tenant code
     */
    public String queryTenantCodeByResName(String resName, ResourceType resourceType) {
        // in order to query tenant code successful although the version is older
        String fullName = resName.startsWith("/") ? resName : String.format("/%s", resName);

        List<Resource> resourceList = resourceMapper.queryResource(fullName, resourceType.ordinal());
        if (CollectionUtils.isEmpty(resourceList)) {
            return StringUtils.EMPTY;
        }
        int userId = resourceList.get(0).getUserId();
        User user = userMapper.selectById(userId);
        if (Objects.isNull(user)) {
            return StringUtils.EMPTY;
        }
        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (Objects.isNull(tenant)) {
            return StringUtils.EMPTY;
        }
        return tenant.getTenantCode();
    }

    /**
     * find schedule list by process define codes.
     *
     * @param codes codes
     * @return schedule list
     */
    public List<Schedule> selectAllByProcessDefineCode(long[] codes) {
        return scheduleMapper.selectAllByProcessDefineArray(codes);
    }

    /**
     * find last task instance in the date interval
     *
     * @param taskCode     taskCode
     * @param dateInterval dateInterval
     * @return task instance
     */
    public TaskInstance findLastTaskInstanceInterval(long taskCode, DateInterval dateInterval) {
        return taskInstanceMapper.queryLastTaskInstance(taskCode, dateInterval.getStartTime(), dateInterval.getEndTime());
    }

    /**
     * find last task instance list in the date interval
     *
     * @param taskCodes    taskCode list
     * @param dateInterval dateInterval
     * @return task instance
     */
    public List<TaskInstance> findLastTaskInstanceListInterval(Set<Long> taskCodes, DateInterval dateInterval) {
        return taskInstanceMapper.queryLastTaskInstanceList(taskCodes, dateInterval.getStartTime(), dateInterval.getEndTime());
    }

    /**
     * find last scheduler process instance in the date interval
     *
     * @param definitionCode definitionCode
     * @param dateInterval   dateInterval
     * @return process instance
     */
    public ProcessInstance findLastSchedulerProcessInterval(Long definitionCode, DateInterval dateInterval) {
        return processInstanceMapper.queryLastSchedulerProcess(definitionCode,
                dateInterval.getStartTime(),
                dateInterval.getEndTime());
    }

    /**
     * find last manual process instance interval
     *
     * @param definitionCode process definition code
     * @param dateInterval   dateInterval
     * @return process instance
     */
    public ProcessInstance findLastManualProcessInterval(Long definitionCode, DateInterval dateInterval) {
        return processInstanceMapper.queryLastManualProcess(definitionCode,
                dateInterval.getStartTime(),
                dateInterval.getEndTime());
    }

    /**
     * find last running process instance
     *
     * @param definitionCode process definition code
     * @param startTime      start time
     * @param endTime        end time
     * @return process instance
     */
    public ProcessInstance findLastRunningProcess(Long definitionCode, Date startTime, Date endTime) {
        return processInstanceMapper.queryLastRunningProcess(definitionCode,
                startTime,
                endTime,
                stateArray);
    }

    /**
     * query user queue by process instance id
     *
     * @param processInstanceId processInstanceId
     * @return queue
     */
    public String queryUserQueueByProcessInstanceId(int processInstanceId) {

        String queue = "";
        ProcessInstance processInstance = processInstanceMapper.selectById(processInstanceId);
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
    public ProjectUser queryProjectWithUserByProcessInstanceId(int processInstanceId) {
        return projectMapper.queryProjectWithUserByProcessInstanceId(processInstanceId);
    }

    /**
     * get task worker group
     *
     * @param taskInstance taskInstance
     * @return workerGroupId
     */
    public String getTaskWorkerGroup(TaskInstance taskInstance) {
        String workerGroup = taskInstance.getWorkerGroup();

        if (StringUtils.isNotBlank(workerGroup)) {
            return workerGroup;
        }
        int processInstanceId = taskInstance.getProcessInstanceId();
        ProcessInstance processInstance = findProcessInstanceById(processInstanceId);

        if (processInstance != null) {
            return processInstance.getWorkerGroup();
        }
        logger.info("task : {} will use default worker group", taskInstance.getId());
        return Constants.DEFAULT_WORKER_GROUP;
    }

    /**
     * get have perm project list
     *
     * @param userId userId
     * @return project list
     */
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
    public <T> List<T> listUnauthorized(int userId, T[] needChecks, AuthorizationType authorizationType) {
        List<T> resultList = new ArrayList<>();

        if (Objects.nonNull(needChecks) && needChecks.length > 0) {
            Set<T> originResSet = new HashSet<>(Arrays.asList(needChecks));

            switch (authorizationType) {
                case RESOURCE_FILE_ID:
                case UDF_FILE:
                    List<Resource> ownUdfResources = resourceMapper.listAuthorizedResourceById(userId, needChecks);
                    addAuthorizedResources(ownUdfResources, userId);
                    Set<Integer> authorizedResourceFiles = ownUdfResources.stream().map(Resource::getId).collect(toSet());
                    originResSet.removeAll(authorizedResourceFiles);
                    break;
                case RESOURCE_FILE_NAME:
                    List<Resource> ownResources = resourceMapper.listAuthorizedResource(userId, needChecks);
                    addAuthorizedResources(ownResources, userId);
                    Set<String> authorizedResources = ownResources.stream().map(Resource::getFullName).collect(toSet());
                    originResSet.removeAll(authorizedResources);
                    break;
                case DATASOURCE:
                    Set<Integer> authorizedDatasources = dataSourceMapper.listAuthorizedDataSource(userId, needChecks).stream().map(DataSource::getId).collect(toSet());
                    originResSet.removeAll(authorizedDatasources);
                    break;
                case UDF:
                    Set<Integer> authorizedUdfs = udfFuncMapper.listAuthorizedUdfFunc(userId, needChecks).stream().map(UdfFunc::getId).collect(toSet());
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
    public User getUserById(int userId) {
        return userMapper.selectById(userId);
    }

    /**
     * get resource by resource id
     *
     * @param resourceId resource id
     * @return Resource
     */
    public Resource getResourceById(int resourceId) {
        return resourceMapper.selectById(resourceId);
    }

    /**
     * list resources by ids
     *
     * @param resIds resIds
     * @return resource list
     */
    public List<Resource> listResourceByIds(Integer[] resIds) {
        return resourceMapper.listResourceByIds(resIds);
    }

    /**
     * format task app id in task instance
     */
    public String formatTaskAppId(TaskInstance taskInstance) {
        ProcessInstance processInstance = findProcessInstanceById(taskInstance.getProcessInstanceId());
        if (processInstance == null) {
            return "";
        }
        ProcessDefinition definition = findProcessDefinition(processInstance.getProcessDefinitionCode(), processInstance.getProcessDefinitionVersion());
        if (definition == null) {
            return "";
        }
        return String.format("%s_%s_%s", definition.getId(), processInstance.getId(), taskInstance.getId());
    }

    /**
     * switch process definition version to process definition log version
     */
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

    public int switchProcessTaskRelationVersion(ProcessDefinition processDefinition) {
        List<ProcessTaskRelation> processTaskRelationList = processTaskRelationMapper.queryByProcessCode(processDefinition.getProjectCode(), processDefinition.getCode());
        if (!processTaskRelationList.isEmpty()) {
            processTaskRelationMapper.deleteByCode(processDefinition.getProjectCode(), processDefinition.getCode());
        }
        List<ProcessTaskRelationLog> processTaskRelationLogList = processTaskRelationLogMapper.queryByProcessCodeAndVersion(processDefinition.getCode(), processDefinition.getVersion());
        int batchInsert = processTaskRelationMapper.batchInsert(processTaskRelationLogList);
        if (batchInsert == 0) {
            return Constants.EXIT_CODE_FAILURE;
        } else {
            int result = 0;
            for (ProcessTaskRelationLog taskRelationLog : processTaskRelationLogList) {
                int switchResult = switchTaskDefinitionVersion(taskRelationLog.getPostTaskCode(), taskRelationLog.getPostTaskVersion());
                if (switchResult != Constants.EXIT_CODE_FAILURE) {
                    result++;
                }
            }
            return result;
        }
    }

    public int switchTaskDefinitionVersion(long taskCode, int taskVersion) {
        TaskDefinition taskDefinition = taskDefinitionMapper.queryByCode(taskCode);
        if (taskDefinition == null) {
            return Constants.EXIT_CODE_FAILURE;
        }
        if (taskDefinition.getVersion() == taskVersion) {
            return Constants.EXIT_CODE_SUCCESS;
        }
        TaskDefinitionLog taskDefinitionUpdate = taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskCode, taskVersion);
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
    public String getResourceIds(TaskDefinition taskDefinition) {
        Set<Integer> resourceIds = null;
        AbstractParameters params = TaskParametersUtils.getParameters(taskDefinition.getTaskType(), taskDefinition.getTaskParams());
        if (params != null && CollectionUtils.isNotEmpty(params.getResourceFilesList())) {
            resourceIds = params.getResourceFilesList().
                    stream()
                    .filter(t -> t.getId() != 0)
                    .map(ResourceInfo::getId)
                    .collect(Collectors.toSet());
        }
        if (CollectionUtils.isEmpty(resourceIds)) {
            return StringUtils.EMPTY;
        }
        return StringUtils.join(resourceIds, ",");
    }

    public int saveTaskDefine(User operator, long projectCode, List<TaskDefinitionLog> taskDefinitionLogs, Boolean syncDefine) {
        Date now = new Date();
        List<TaskDefinitionLog> newTaskDefinitionLogs = new ArrayList<>();
        List<TaskDefinitionLog> updateTaskDefinitionLogs = new ArrayList<>();
        for (TaskDefinitionLog taskDefinitionLog : taskDefinitionLogs) {
            taskDefinitionLog.setProjectCode(projectCode);
            taskDefinitionLog.setUpdateTime(now);
            taskDefinitionLog.setOperateTime(now);
            taskDefinitionLog.setOperator(operator.getId());
            taskDefinitionLog.setResourceIds(getResourceIds(taskDefinitionLog));
            if (taskDefinitionLog.getCode() > 0 && taskDefinitionLog.getVersion() > 0) {
                TaskDefinitionLog definitionCodeAndVersion = taskDefinitionLogMapper
                        .queryByDefinitionCodeAndVersion(taskDefinitionLog.getCode(), taskDefinitionLog.getVersion());
                if (definitionCodeAndVersion != null) {
                    if (!taskDefinitionLog.equals(definitionCodeAndVersion)) {
                        taskDefinitionLog.setUserId(definitionCodeAndVersion.getUserId());
                        Integer version = taskDefinitionLogMapper.queryMaxVersionForDefinition(taskDefinitionLog.getCode());
                        taskDefinitionLog.setVersion(version + 1);
                        taskDefinitionLog.setCreateTime(definitionCodeAndVersion.getCreateTime());
                        updateTaskDefinitionLogs.add(taskDefinitionLog);
                    }
                    continue;
                }
            }
            taskDefinitionLog.setUserId(operator.getId());
            taskDefinitionLog.setVersion(Constants.VERSION_FIRST);
            taskDefinitionLog.setCreateTime(now);
            if (taskDefinitionLog.getCode() == 0) {
                try {
                    taskDefinitionLog.setCode(CodeGenerateUtils.getInstance().genCode());
                } catch (CodeGenerateException e) {
                    logger.error("Task code get error, ", e);
                    return Constants.DEFINITION_FAILURE;
                }
            }
            newTaskDefinitionLogs.add(taskDefinitionLog);
        }
        int insertResult = 0;
        int updateResult = 0;
        for (TaskDefinitionLog taskDefinitionToUpdate : updateTaskDefinitionLogs) {
            TaskDefinition task = taskDefinitionMapper.queryByCode(taskDefinitionToUpdate.getCode());
            if (task == null) {
                newTaskDefinitionLogs.add(taskDefinitionToUpdate);
            } else {
                insertResult += taskDefinitionLogMapper.insert(taskDefinitionToUpdate);
                if (Boolean.TRUE.equals(syncDefine)) {
                    taskDefinitionToUpdate.setId(task.getId());
                    updateResult += taskDefinitionMapper.updateById(taskDefinitionToUpdate);
                } else {
                    updateResult++;
                }
            }
        }
        if (!newTaskDefinitionLogs.isEmpty()) {
            insertResult += taskDefinitionLogMapper.batchInsert(newTaskDefinitionLogs);
            if (Boolean.TRUE.equals(syncDefine)) {
                updateResult += taskDefinitionMapper.batchInsert(newTaskDefinitionLogs);
            } else {
                updateResult += newTaskDefinitionLogs.size();
            }
        }
        return (insertResult & updateResult) > 0 ? 1 : Constants.EXIT_CODE_SUCCESS;
    }

    /**
     * save processDefinition (including create or update processDefinition)
     */
    public int saveProcessDefine(User operator, ProcessDefinition processDefinition, Boolean syncDefine, Boolean isFromProcessDefine) {
        ProcessDefinitionLog processDefinitionLog = new ProcessDefinitionLog(processDefinition);
        Integer version = processDefineLogMapper.queryMaxVersionForDefinition(processDefinition.getCode());
        int insertVersion = version == null || version == 0 ? Constants.VERSION_FIRST : version + 1;
        processDefinitionLog.setVersion(insertVersion);
        processDefinitionLog.setReleaseState(!isFromProcessDefine || processDefinitionLog.getReleaseState() == ReleaseState.ONLINE ? ReleaseState.ONLINE : ReleaseState.OFFLINE);
        processDefinitionLog.setOperator(operator.getId());
        processDefinitionLog.setOperateTime(processDefinition.getUpdateTime());
        int insertLog = processDefineLogMapper.insert(processDefinitionLog);
        int result = 1;
        if (Boolean.TRUE.equals(syncDefine)) {
            if (0 == processDefinition.getId()) {
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
    public int saveTaskRelation(User operator, long projectCode, long processDefinitionCode, int processDefinitionVersion,
                                List<ProcessTaskRelationLog> taskRelationList, List<TaskDefinitionLog> taskDefinitionLogs,
                                Boolean syncDefine) {
        if (taskRelationList.isEmpty()) {
            return Constants.EXIT_CODE_SUCCESS;
        }
        Map<Long, TaskDefinitionLog> taskDefinitionLogMap = null;
        if (CollectionUtils.isNotEmpty(taskDefinitionLogs)) {
            taskDefinitionLogMap = taskDefinitionLogs.stream()
                    .collect(Collectors.toMap(TaskDefinition::getCode, taskDefinitionLog -> taskDefinitionLog));
        }
        Date now = new Date();
        for (ProcessTaskRelationLog processTaskRelationLog : taskRelationList) {
            processTaskRelationLog.setProjectCode(projectCode);
            processTaskRelationLog.setProcessDefinitionCode(processDefinitionCode);
            processTaskRelationLog.setProcessDefinitionVersion(processDefinitionVersion);
            if (taskDefinitionLogMap != null) {
                TaskDefinitionLog preTaskDefinitionLog = taskDefinitionLogMap.get(processTaskRelationLog.getPreTaskCode());
                if (preTaskDefinitionLog != null) {
                    processTaskRelationLog.setPreTaskVersion(preTaskDefinitionLog.getVersion());
                }
                TaskDefinitionLog postTaskDefinitionLog = taskDefinitionLogMap.get(processTaskRelationLog.getPostTaskCode());
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
            List<ProcessTaskRelation> processTaskRelationList = processTaskRelationMapper.queryByProcessCode(projectCode, processDefinitionCode);
            if (!processTaskRelationList.isEmpty()) {
                Set<Integer> processTaskRelationSet = processTaskRelationList.stream().map(ProcessTaskRelation::hashCode).collect(toSet());
                Set<Integer> taskRelationSet = taskRelationList.stream().map(ProcessTaskRelationLog::hashCode).collect(toSet());
                boolean result = CollectionUtils.isEqualCollection(processTaskRelationSet, taskRelationSet);
                if (result) {
                    return Constants.EXIT_CODE_SUCCESS;
                }
                processTaskRelationMapper.deleteByCode(projectCode, processDefinitionCode);
            }
            insert = processTaskRelationMapper.batchInsert(taskRelationList);
        }
        int resultLog = processTaskRelationLogMapper.batchInsert(taskRelationList);
        return (insert & resultLog) > 0 ? Constants.EXIT_CODE_SUCCESS : Constants.EXIT_CODE_FAILURE;
    }

    public boolean isTaskOnline(long taskCode) {
        List<ProcessTaskRelation> processTaskRelationList = processTaskRelationMapper.queryByTaskCode(taskCode);
        if (!processTaskRelationList.isEmpty()) {
            Set<Long> processDefinitionCodes = processTaskRelationList
                    .stream()
                    .map(ProcessTaskRelation::getProcessDefinitionCode)
                    .collect(Collectors.toSet());
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
    public DAG<String, TaskNode, TaskNodeRelation> genDagGraph(ProcessDefinition processDefinition) {
        List<ProcessTaskRelation> taskRelations = this.findRelationByCode(processDefinition.getCode(), processDefinition.getVersion());
        List<TaskNode> taskNodeList = transformTask(taskRelations, Lists.newArrayList());
        ProcessDag processDag = DagHelper.getProcessDag(taskNodeList, new ArrayList<>(taskRelations));
        // Generate concrete Dag to be executed
        return DagHelper.buildDagGraph(processDag);
    }

    /**
     * generate DagData
     */
    public DagData genDagData(ProcessDefinition processDefinition) {
        List<ProcessTaskRelation> taskRelations = this.findRelationByCode(processDefinition.getCode(), processDefinition.getVersion());
        List<TaskDefinitionLog> taskDefinitionLogList = genTaskDefineList(taskRelations);
        List<TaskDefinition> taskDefinitions = taskDefinitionLogList.stream().map(t -> (TaskDefinition) t).collect(Collectors.toList());
        return new DagData(processDefinition, taskRelations, taskDefinitions);
    }

    public List<TaskDefinitionLog> genTaskDefineList(List<ProcessTaskRelation> processTaskRelations) {
        Set<TaskDefinition> taskDefinitionSet = new HashSet<>();
        for (ProcessTaskRelation processTaskRelation : processTaskRelations) {
            if (processTaskRelation.getPreTaskCode() > 0) {
                taskDefinitionSet.add(new TaskDefinition(processTaskRelation.getPreTaskCode(), processTaskRelation.getPreTaskVersion()));
            }
            if (processTaskRelation.getPostTaskCode() > 0) {
                taskDefinitionSet.add(new TaskDefinition(processTaskRelation.getPostTaskCode(), processTaskRelation.getPostTaskVersion()));
            }
        }
        if (taskDefinitionSet.isEmpty()) {
            return Lists.newArrayList();
        }
        return taskDefinitionLogMapper.queryByTaskDefinitions(taskDefinitionSet);
    }

    public List<TaskDefinitionLog> getTaskDefineLogListByRelation(List<ProcessTaskRelation> processTaskRelations) {
        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        Map<Long, Integer> taskCodeVersionMap = new HashMap<>();
        for (ProcessTaskRelation processTaskRelation : processTaskRelations) {
            if (processTaskRelation.getPreTaskCode() > 0) {
                taskCodeVersionMap.put(processTaskRelation.getPreTaskCode(), processTaskRelation.getPreTaskVersion());
            }
            if (processTaskRelation.getPostTaskCode() > 0) {
                taskCodeVersionMap.put(processTaskRelation.getPostTaskCode(), processTaskRelation.getPostTaskVersion());
            }
        }
        taskCodeVersionMap.forEach((code, version) -> {
            taskDefinitionLogs.add((TaskDefinitionLog) this.findTaskDefinition(code, version));
        });
        return taskDefinitionLogs;
    }

    /**
     * find task definition by code and version
     */
    public TaskDefinition findTaskDefinition(long taskCode, int taskDefinitionVersion) {
        return taskDefinitionLogMapper.queryByDefinitionCodeAndVersion(taskCode, taskDefinitionVersion);
    }

    /**
     * find process task relation list by process
     */
    public List<ProcessTaskRelation> findRelationByCode(long processDefinitionCode, int processDefinitionVersion) {
        List<ProcessTaskRelationLog> processTaskRelationLogList = processTaskRelationLogMapper.queryByProcessCodeAndVersion(processDefinitionCode, processDefinitionVersion);
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
        List<Resource> relationResources = CollectionUtils.isNotEmpty(relationResourceIds) ? resourceMapper.queryResourceListById(relationResourceIds) : new ArrayList<>();
        ownResources.addAll(relationResources);
    }

    /**
     * Use temporarily before refactoring taskNode
     */
    public List<TaskNode> transformTask(List<ProcessTaskRelation> taskRelationList, List<TaskDefinitionLog> taskDefinitionLogs) {
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
            taskDefinitionLogs = genTaskDefineList(taskRelationList);
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
                taskNode.setRunFlag(taskDefinitionLog.getFlag() == Flag.YES ? Constants.FLOWNODE_RUN_FLAG_NORMAL : Constants.FLOWNODE_RUN_FLAG_FORBIDDEN);
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
                taskNode.setTimeout(JSONUtils.toJsonString(new TaskTimeoutParameter(taskDefinitionLog.getTimeoutFlag() == TimeoutFlag.OPEN,
                        taskDefinitionLog.getTimeoutNotifyStrategy(),
                        taskDefinitionLog.getTimeout())));
                taskNode.setDelayTime(taskDefinitionLog.getDelayTime());
                taskNode.setPreTasks(JSONUtils.toJsonString(code.getValue().stream().map(taskDefinitionLogMap::get).map(TaskDefinition::getCode).collect(Collectors.toList())));
                taskNodeList.add(taskNode);
            }
        }
        return taskNodeList;
    }

    public Map<ProcessInstance, TaskInstance> notifyProcessList(int processId) {
        HashMap<ProcessInstance, TaskInstance> processTaskMap = new HashMap<>();
        //find sub tasks
        ProcessInstanceMap processInstanceMap = processInstanceMapMapper.queryBySubProcessId(processId);
        if (processInstanceMap == null) {
            return processTaskMap;
        }
        ProcessInstance fatherProcess = this.findProcessInstanceById(processInstanceMap.getParentProcessInstanceId());
        TaskInstance fatherTask = this.findTaskInstanceById(processInstanceMap.getParentTaskInstanceId());

        if (fatherProcess != null) {
            processTaskMap.put(fatherProcess, fatherTask);
        }
        return processTaskMap;
    }

    private void deleteCommandWithCheck(int commandId) {
        int delete = this.commandMapper.deleteById(commandId);
        if (delete != 1) {
            throw new ServiceException("delete command fail, id:" + commandId);
        }
    }
}
