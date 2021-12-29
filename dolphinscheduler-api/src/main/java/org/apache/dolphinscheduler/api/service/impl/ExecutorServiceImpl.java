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

import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_RECOVER_PROCESS_ID_STRING;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_START_NODES;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_START_PARAMS;
import static org.apache.dolphinscheduler.common.Constants.MAX_TASK_TIMEOUT;

import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.service.MonitorService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.RunMode;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskGroupQueue;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.remote.command.StateEventChangeCommand;
import org.apache.dolphinscheduler.remote.processor.StateEventCallbackService;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.quartz.cron.CronUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

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
    StateEventCallbackService stateEventCallbackService;

    /**
     * execute process instance
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processDefinitionCode process definition code
     * @param cronTime cron time
     * @param commandType command type
     * @param failureStrategy failure strategy
     * @param startNodeList start nodelist
     * @param taskDependType node dependency type
     * @param warningType warning type
     * @param warningGroupId notify group id
     * @param processInstancePriority process instance priority
     * @param workerGroup worker group name
     * @param environmentCode environment code
     * @param runMode run mode
     * @param timeout timeout
     * @param startParams the global param values which pass to new process instance
     * @param expectedParallelismNumber the expected parallelism number when execute complement in parallel mode
     * @return execute process instance code
     */
    @Override
    public Map<String, Object> execProcessInstance(User loginUser, long projectCode,
                                                   long processDefinitionCode, String cronTime, CommandType commandType,
                                                   FailureStrategy failureStrategy, String startNodeList,
                                                   TaskDependType taskDependType, WarningType warningType, int warningGroupId,
                                                   RunMode runMode,
                                                   Priority processInstancePriority, String workerGroup, Long environmentCode,Integer timeout,
                                                   Map<String, String> startParams, Integer expectedParallelismNumber,
                                                   int dryRun) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
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
        result = checkProcessDefinitionValid(projectCode, processDefinition, processDefinitionCode);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return result;
        }

        if (!checkTenantSuitable(processDefinition)) {
            logger.error("there is not any valid tenant for the process definition: id:{},name:{}, ",
                    processDefinition.getId(), processDefinition.getName());
            putMsg(result, Status.TENANT_NOT_SUITABLE);
            return result;
        }

        // check master exists
        if (!checkMasterExists(result)) {
            return result;
        }

        /**
         * create command
         */
        int create = this.createCommand(commandType, processDefinition.getCode(),
                taskDependType, failureStrategy, startNodeList, cronTime, warningType, loginUser.getId(),
                warningGroupId, runMode, processInstancePriority, workerGroup, environmentCode, startParams, expectedParallelismNumber, dryRun);

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
     * check whether the process definition can be executed
     *
     * @param projectCode project code
     * @param processDefinition process definition
     * @param processDefineCode process definition code
     * @return check result code
     */
    @Override
    public Map<String, Object> checkProcessDefinitionValid(long projectCode, ProcessDefinition processDefinition, long processDefineCode) {
        Map<String, Object> result = new HashMap<>();
        if (processDefinition == null || projectCode != processDefinition.getProjectCode()) {
            // check process definition exists
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST, processDefineCode);
        } else if (processDefinition.getReleaseState() != ReleaseState.ONLINE) {
            // check process definition online
            putMsg(result, Status.PROCESS_DEFINE_NOT_RELEASE, processDefineCode);
        } else {
            result.put(Constants.STATUS, Status.SUCCESS);
        }
        return result;
    }

    /**
     * do action to process instance：pause, stop, repeat, recover from pause, recover from stop
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param processInstanceId process instance id
     * @param executeType execute type
     * @return execute result code
     */
    @Override
    public Map<String, Object> execute(User loginUser, long projectCode, Integer processInstanceId, ExecuteType executeType) {
        Project project = projectMapper.queryByCode(projectCode);
        //check user access for project
        Map<String, Object> result = projectService.checkProjectAndAuth(loginUser, project, projectCode);
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

        ProcessDefinition processDefinition = processService.findProcessDefinition(processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion());
        if (executeType != ExecuteType.STOP && executeType != ExecuteType.PAUSE) {
            result = checkProcessDefinitionValid(projectCode, processDefinition, processInstance.getProcessDefinitionCode());
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

        //get the startParams user specified at the first starting while repeat running is needed
        Map<String, Object> commandMap = JSONUtils.parseObject(processInstance.getCommandParam(), new TypeReference<Map<String, Object>>() {});
        String startParams = null;
        if (MapUtils.isNotEmpty(commandMap) && executeType == ExecuteType.REPEAT_RUNNING) {
            Object startParamsJson = commandMap.get(Constants.CMD_PARAM_START_PARAMS);
            if (startParamsJson != null) {
                startParams = startParamsJson.toString();
            }
        }

        switch (executeType) {
            case REPEAT_RUNNING:
                result = insertCommand(loginUser, processInstanceId, processDefinition.getCode(), processDefinition.getVersion(), CommandType.REPEAT_RUNNING, startParams);
                break;
            case RECOVER_SUSPENDED_PROCESS:
                result = insertCommand(loginUser, processInstanceId, processDefinition.getCode(), processDefinition.getVersion(), CommandType.RECOVER_SUSPENDED_PROCESS, startParams);
                break;
            case START_FAILURE_TASK_PROCESS:
                result = insertCommand(loginUser, processInstanceId, processDefinition.getCode(), processDefinition.getVersion(), CommandType.START_FAILURE_TASK_PROCESS, startParams);
                break;
            case STOP:
                if (processInstance.getState() == ExecutionStatus.READY_STOP) {
                    putMsg(result, Status.PROCESS_INSTANCE_ALREADY_CHANGED, processInstance.getName(), processInstance.getState());
                } else {
                    result = updateProcessInstancePrepare(processInstance, CommandType.STOP, ExecutionStatus.READY_STOP);
                }
                break;
            case PAUSE:
                if (processInstance.getState() == ExecutionStatus.READY_PAUSE) {
                    putMsg(result, Status.PROCESS_INSTANCE_ALREADY_CHANGED, processInstance.getName(), processInstance.getState());
                } else {
                    result = updateProcessInstancePrepare(processInstance, CommandType.PAUSE, ExecutionStatus.READY_PAUSE);
                }
                break;
            default:
                logger.error("unknown execute type : {}", executeType);
                putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, "unknown execute type");

                break;
        }
        return result;
    }

    /**
     * check tenant suitable
     *
     * @param processDefinition process definition
     * @return true if tenant suitable, otherwise return false
     */
    private boolean checkTenantSuitable(ProcessDefinition processDefinition) {
        Tenant tenant = processService.getTenantForProcess(processDefinition.getTenantId(),
                processDefinition.getUserId());
        return tenant != null;
    }

    /**
     * Check the state of process instance and the type of operation match
     *
     * @param processInstance process instance
     * @param executeType execute type
     * @return check result code
     */
    private Map<String, Object> checkExecuteType(ProcessInstance processInstance, ExecuteType executeType) {

        Map<String, Object> result = new HashMap<>();
        ExecutionStatus executionStatus = processInstance.getState();
        boolean checkResult = false;
        switch (executeType) {
            case PAUSE:
            case STOP:
                if (executionStatus.typeIsRunning()) {
                    checkResult = true;
                }
                break;
            case REPEAT_RUNNING:
                if (executionStatus.typeIsFinished()) {
                    checkResult = true;
                }
                break;
            case START_FAILURE_TASK_PROCESS:
                if (executionStatus.typeIsFailure()) {
                    checkResult = true;
                }
                break;
            case RECOVER_SUSPENDED_PROCESS:
                if (executionStatus.typeIsPause() || executionStatus.typeIsCancel()) {
                    checkResult = true;
                }
                break;
            default:
                break;
        }
        if (!checkResult) {
            putMsg(result, Status.PROCESS_INSTANCE_STATE_OPERATION_ERROR, processInstance.getName(), executionStatus.toString(), executeType.toString());
        } else {
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * prepare to update process instance command type and status
     *
     * @param processInstance process instance
     * @param commandType command type
     * @param executionStatus execute status
     * @return update result
     */
    private Map<String, Object> updateProcessInstancePrepare(ProcessInstance processInstance, CommandType commandType, ExecutionStatus executionStatus) {
        Map<String, Object> result = new HashMap<>();

        processInstance.setCommandType(commandType);
        processInstance.addHistoryCmd(commandType);
        processInstance.setState(executionStatus);
        int update = processService.updateProcessInstance(processInstance);

        // determine whether the process is normal
        if (update > 0) {
            String host = processInstance.getHost();
            String address = host.split(":")[0];
            int port = Integer.parseInt(host.split(":")[1]);
            StateEventChangeCommand stateEventChangeCommand = new StateEventChangeCommand(
                    processInstance.getId(), 0, processInstance.getState(), processInstance.getId(), 0
            );
            stateEventCallbackService.sendResult(address, port, stateEventChangeCommand.convert2Command());
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
    private Map<String, Object> forceStartTaskInstance(ProcessInstance processInstance, int taskId) {
        Map<String, Object> result = new HashMap<>();
        TaskGroupQueue taskGroupQueue = processService.loadTaskGroupQueue(taskId);
        if (taskGroupQueue.getStatus() != TaskGroupQueueStatus.WAIT_QUEUE) {
            putMsg(result, Status.TASK_GROUP_QUEUE_ALREADY_START);
            return result;
        }
        taskGroupQueue.setForceStart(Flag.YES.getCode());
        processService.updateTaskGroupQueue(taskGroupQueue);
        processService.sendStartTask2Master(processInstance,taskId
                ,org.apache.dolphinscheduler.remote.command.CommandType.TASK_FORCE_STATE_EVENT_REQUEST);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * insert command, used in the implementation of the page, re run, recovery (pause / failure) execution
     *
     * @param loginUser           login user
     * @param instanceId          instance id
     * @param processDefinitionCode process definition code
     * @param processVersion
     * @param commandType         command type
     * @return insert result code
     */
    private Map<String, Object> insertCommand(User loginUser, Integer instanceId, long processDefinitionCode, int processVersion, CommandType commandType, String startParams) {
        Map<String, Object> result = new HashMap<>();

        //To add startParams only when repeat running is needed
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
            putMsg(result, Status.PROCESS_INSTANCE_EXECUTING_COMMAND, processDefinitionCode);
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
     * check if sub processes are offline before starting process definition
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
                        logger.info("not release process definition id: {} , name : {}",
                                processDefinitionTmp.getId(), processDefinitionTmp.getName());
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
     * @param commandType commandType
     * @param processDefineCode processDefineCode
     * @param nodeDep nodeDep
     * @param failureStrategy failureStrategy
     * @param startNodeList startNodeList
     * @param schedule schedule
     * @param warningType warningType
     * @param executorId executorId
     * @param warningGroupId warningGroupId
     * @param runMode runMode
     * @param processInstancePriority processInstancePriority
     * @param workerGroup workerGroup
     * @param environmentCode environmentCode
     * @return command id
     */
    private int createCommand(CommandType commandType, long processDefineCode,
                              TaskDependType nodeDep, FailureStrategy failureStrategy,
                              String startNodeList, String schedule, WarningType warningType,
                              int executorId, int warningGroupId,
                              RunMode runMode, Priority processInstancePriority, String workerGroup, Long environmentCode,
                              Map<String, String> startParams, Integer expectedParallelismNumber, int dryRun) {

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

        Date start = null;
        Date end = null;
        if (!StringUtils.isEmpty(schedule)) {
            String[] interval = schedule.split(",");
            if (interval.length == 2) {
                start = DateUtils.getScheduleDate(interval[0]);
                end = DateUtils.getScheduleDate(interval[1]);
                if (start.after(end)) {
                    logger.info("complement data error, wrong date start:{} and end date:{} ",
                            start, end
                    );
                    return 0;
                }
            }
        }
        // determine whether to complement
        if (commandType == CommandType.COMPLEMENT_DATA) {
            if (start == null || end == null) {
                return 0;
            }
            return createComplementCommandList(start, end, runMode, command, expectedParallelismNumber);
        } else {
            command.setCommandParam(JSONUtils.toJsonString(cmdParam));
            return processService.createCommand(command);
        }
    }

    /**
     * create complement command
     * close left open right
     *
     * @param start
     * @param end
     * @param runMode
     * @return
     */
    private int createComplementCommandList(Date start, Date end, RunMode runMode, Command command, Integer expectedParallelismNumber) {
        int createCount = 0;
        runMode = (runMode == null) ? RunMode.RUN_MODE_SERIAL : runMode;
        Map<String, String> cmdParam = JSONUtils.toMap(command.getCommandParam());
        switch (runMode) {
            case RUN_MODE_SERIAL: {
                if (start.after(end)) {
                    logger.warn("The startDate {} is later than the endDate {}", start, end);
                    break;
                }
                cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE, DateUtils.dateToString(start));
                cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE, DateUtils.dateToString(end));
                command.setCommandParam(JSONUtils.toJsonString(cmdParam));
                createCount = processService.createCommand(command);
                break;
            }
            case RUN_MODE_PARALLEL: {
                if (start.after(end)) {
                    logger.warn("The startDate {} is later than the endDate {}", start, end);
                    break;
                }

                LinkedList<Date> listDate = new LinkedList<>();
                List<Schedule> schedules = processService.queryReleaseSchedulerListByProcessDefinitionCode(command.getProcessDefinitionCode());
                listDate.addAll(CronUtils.getSelfFireDateList(start, end, schedules));
                int listDateSize = listDate.size();
                createCount = listDate.size();
                if (!CollectionUtils.isEmpty(listDate)) {
                    if (expectedParallelismNumber != null && expectedParallelismNumber != 0) {
                        createCount = Math.min(listDate.size(), expectedParallelismNumber);
                        if (listDateSize < createCount) {
                            createCount = listDateSize;
                        }
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

                        cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE, DateUtils.dateToString(listDate.get(startDateIndex)));
                        cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE, DateUtils.dateToString(listDate.get(endDateIndex)));
                        command.setCommandParam(JSONUtils.toJsonString(cmdParam));
                        processService.createCommand(command);
                    }
                }
                break;
            }
            default:
                break;
        }
        logger.info("create complement command count: {}", createCount);
        return createCount;
    }
}
