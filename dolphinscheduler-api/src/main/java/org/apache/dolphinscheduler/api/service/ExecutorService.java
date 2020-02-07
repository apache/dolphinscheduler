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
package org.apache.dolphinscheduler.api.service;


import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.ProcessDao;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.utils.cron.CronUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

import static org.apache.dolphinscheduler.common.Constants.*;

/**
 * executor service
 */
@Service
public class ExecutorService extends BaseService{

    private static final Logger logger = LoggerFactory.getLogger(ExecutorService.class);

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private ProcessDefinitionService processDefinitionService;


    @Autowired
    private ProcessInstanceMapper processInstanceMapper;


    @Autowired
    private ProcessDao processDao;

    /**
     * execute process instance
     *
     * @param loginUser             login user
     * @param projectName           project name
     * @param processDefinitionId   process Definition Id
     * @param cronTime              cron time
     * @param commandType           command type
     * @param failureStrategy       failuer strategy
     * @param startNodeList         start nodelist
     * @param taskDependType        node dependency type
     * @param warningType           warning type
     * @param warningGroupId         notify group id
     * @param receivers             receivers
     * @param receiversCc           receivers cc
     * @param processInstancePriority process instance priority
     * @param workerGroupId worker group id
     * @param runMode run mode
     * @param timeout               timeout
     * @return execute process instance code
     * @throws ParseException Parse Exception
     */
    public Map<String, Object> execProcessInstance(User loginUser, String projectName,
                                                   int processDefinitionId, String cronTime, CommandType commandType,
                                                   FailureStrategy failureStrategy, String startNodeList,
                                                   TaskDependType taskDependType, WarningType warningType, int warningGroupId,
                                                   String receivers, String receiversCc, RunMode runMode,
                                                   Priority processInstancePriority, int workerGroupId, Integer timeout) throws ParseException {
        Map<String, Object> result = new HashMap<>(5);
        // timeout is valid
        if (timeout <= 0 || timeout > MAX_TASK_TIMEOUT) {
            putMsg(result,Status.TASK_TIMEOUT_PARAMS_ERROR);
            return result;
        }
        Project project = projectMapper.queryByName(projectName);
        Map<String, Object> checkResultAndAuth = checkResultAndAuth(loginUser, projectName, project);
        if (checkResultAndAuth != null){
            return checkResultAndAuth;
        }

        // check process define release state
        ProcessDefinition processDefinition = processDefinitionMapper.selectById(processDefinitionId);
        result = checkProcessDefinitionValid(processDefinition, processDefinitionId);
        if(result.get(Constants.STATUS) != Status.SUCCESS){
            return result;
        }

        if (!checkTenantSuitable(processDefinition)){
            logger.error("there is not any vaild tenant for the process definition: id:{},name:{}, ",
                    processDefinition.getId(), processDefinition.getName());
            putMsg(result, Status.TENANT_NOT_SUITABLE);
            return result;
        }

        /**
         * create command
         */
        int create = this.createCommand(commandType, processDefinitionId,
                taskDependType, failureStrategy, startNodeList, cronTime, warningType, loginUser.getId(),
                warningGroupId, runMode,processInstancePriority, workerGroupId);
        if(create > 0 ){
            /**
             * according to the process definition ID updateProcessInstance and CC recipient
             */
            processDefinition.setReceivers(receivers);
            processDefinition.setReceiversCc(receiversCc);
            processDefinitionMapper.updateById(processDefinition);
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.START_PROCESS_INSTANCE_ERROR);
        }
        return result;
    }



    /**
     * check whether the process definition can be executed
     *
     * @param processDefinition process definition
     * @param processDefineId process definition id
     * @return check result code
     */
    public Map<String, Object> checkProcessDefinitionValid(ProcessDefinition processDefinition, int processDefineId){
        Map<String, Object> result = new HashMap<>(5);
        if (processDefinition == null) {
            // check process definition exists
            putMsg(result, Status.PROCESS_DEFINE_NOT_EXIST,processDefineId);
        } else if (processDefinition.getReleaseState() != ReleaseState.ONLINE) {
            // check process definition online
            putMsg(result, Status.PROCESS_DEFINE_NOT_RELEASE,processDefineId);
        }else{
            result.put(Constants.STATUS, Status.SUCCESS);
        }
        return result;
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
    public Map<String, Object> execute(User loginUser, String projectName, Integer processInstanceId, ExecuteType executeType) {
        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = checkResultAndAuth(loginUser, projectName, project);
        if (checkResult != null) {
            return checkResult;
        }

        ProcessInstance processInstance = processDao.findProcessInstanceDetailById(processInstanceId);
        if (processInstance == null) {
            putMsg(result, Status.PROCESS_INSTANCE_NOT_EXIST, processInstanceId);
            return result;
        }

        ProcessDefinition processDefinition = processDao.findProcessDefineById(processInstance.getProcessDefinitionId());
        if(executeType != ExecuteType.STOP && executeType != ExecuteType.PAUSE){
            result = checkProcessDefinitionValid(processDefinition, processInstance.getProcessDefinitionId());
            if (result.get(Constants.STATUS) != Status.SUCCESS) {
                return result;
            }
        }

        checkResult = checkExecuteType(processInstance, executeType);
        Status status = (Status) checkResult.get(Constants.STATUS);
        if (status != Status.SUCCESS) {
            return checkResult;
        }
        if (!checkTenantSuitable(processDefinition)){
            logger.error("there is not any vaild tenant for the process definition: id:{},name:{}, ",
                    processDefinition.getId(), processDefinition.getName());
            putMsg(result, Status.TENANT_NOT_SUITABLE);
        }

        switch (executeType) {
            case REPEAT_RUNNING:
                result = insertCommand(loginUser, processInstanceId, processDefinition.getId(), CommandType.REPEAT_RUNNING);
                break;
            case RECOVER_SUSPENDED_PROCESS:
                result = insertCommand(loginUser, processInstanceId, processDefinition.getId(), CommandType.RECOVER_SUSPENDED_PROCESS);
                break;
            case START_FAILURE_TASK_PROCESS:
                result = insertCommand(loginUser, processInstanceId, processDefinition.getId(), CommandType.START_FAILURE_TASK_PROCESS);
                break;
            case STOP:
                if (processInstance.getState() == ExecutionStatus.READY_STOP) {
                    putMsg(result, Status.PROCESS_INSTANCE_ALREADY_CHANGED, processInstance.getName(), processInstance.getState());
                } else {
                    processInstance.setCommandType(CommandType.STOP);
                    processInstance.addHistoryCmd(CommandType.STOP);
                    processDao.updateProcessInstance(processInstance);
                    result = updateProcessInstanceState(processInstanceId, ExecutionStatus.READY_STOP);
                }
                break;
            case PAUSE:
                if (processInstance.getState() == ExecutionStatus.READY_PAUSE) {
                    putMsg(result, Status.PROCESS_INSTANCE_ALREADY_CHANGED, processInstance.getName(), processInstance.getState());
                } else {
                    processInstance.setCommandType(CommandType.PAUSE);
                    processInstance.addHistoryCmd(CommandType.PAUSE);
                    processDao.updateProcessInstance(processInstance);
                    result = updateProcessInstanceState(processInstanceId, ExecutionStatus.READY_PAUSE);
                }
                break;
            default:
                logger.error(String.format("unknown execute type : %s", executeType.toString()));
                putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, "unknown execute type");

                break;
        }
        return result;
    }

    /**
     * check tenant suitable
     * @param processDefinition process definition
     * @return true if tenant suitable, otherwise return false
     */
    private boolean checkTenantSuitable(ProcessDefinition processDefinition) {
        // checkTenantExists();
        Tenant tenant = processDao.getTenantForProcess(processDefinition.getTenantId(),
                processDefinition.getUserId());
        if(tenant == null){
            return false;
        }
        return true;
    }

    /**
     * Check the state of process instance and the type of operation match
     *
     * @param processInstance process instance
     * @param executeType execute type
     * @return check result code
     */
    private Map<String, Object> checkExecuteType(ProcessInstance processInstance, ExecuteType executeType) {

        Map<String, Object> result = new HashMap<>(5);
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
                if (executionStatus.typeIsPause()|| executionStatus.typeIsCancel()) {
                    checkResult = true;
                }
            default:
                break;
        }
        if (!checkResult) {
            putMsg(result,Status.PROCESS_INSTANCE_STATE_OPERATION_ERROR, processInstance.getName(), executionStatus.toString(), executeType.toString());
        } else {
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * update process instance state
     *
     * @param processInstanceId process instance id
     * @param executionStatus execute status
     * @return update result
     */
    private Map<String, Object> updateProcessInstanceState(Integer processInstanceId, ExecutionStatus executionStatus) {
        Map<String, Object> result = new HashMap<>(5);

        int update = processDao.updateProcessInstanceState(processInstanceId, executionStatus);
        if (update > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.EXECUTE_PROCESS_INSTANCE_ERROR);
        }

        return result;
    }

    /**
     * insert command, used in the implementation of the page, re run, recovery (pause / failure) execution
     *
     * @param loginUser login user
     * @param instanceId instance id
     * @param processDefinitionId process definition id
     * @param commandType command type
     * @return insert result code
     */
    private Map<String, Object> insertCommand(User loginUser, Integer instanceId, Integer processDefinitionId, CommandType commandType) {
        Map<String, Object> result = new HashMap<>(5);
        Command command = new Command();
        command.setCommandType(commandType);
        command.setProcessDefinitionId(processDefinitionId);
        command.setCommandParam(String.format("{\"%s\":%d}",
                CMDPARAM_RECOVER_PROCESS_ID_STRING, instanceId));
        command.setExecutorId(loginUser.getId());

        if(!processDao.verifyIsNeedCreateCommand(command)){
            putMsg(result, Status.PROCESS_INSTANCE_EXECUTING_COMMAND,processDefinitionId);
            return result;
        }

        int create = processDao.createCommand(command);

        if (create > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.EXECUTE_PROCESS_INSTANCE_ERROR);
        }

        return result;
    }

    /**
     * check if sub processes are offline before starting process definition
     * @param processDefineId process definition id
     * @return check result code
     */
    public Map<String, Object> startCheckByProcessDefinedId(int processDefineId) {
        Map<String, Object> result = new HashMap<String, Object>();

        if (processDefineId == 0){
            logger.error("process definition id is null");
            putMsg(result,Status.REQUEST_PARAMS_NOT_VALID_ERROR,"process definition id");
        }
        List<Integer> ids = new ArrayList<>();
        processDao.recurseFindSubProcessId(processDefineId, ids);
        Integer[] idArray = ids.toArray(new Integer[ids.size()]);
        if (ids.size() > 0){
            List<ProcessDefinition> processDefinitionList;
            processDefinitionList = processDefinitionMapper.queryDefinitionListByIdList(idArray);
            if (processDefinitionList != null && processDefinitionList.size() > 0){
                for (ProcessDefinition processDefinition : processDefinitionList){
                    /**
                     * if there is no online process, exit directly
                     */
                    if (processDefinition.getReleaseState() != ReleaseState.ONLINE){
                        putMsg(result,Status.PROCESS_DEFINE_NOT_RELEASE, processDefinition.getName());
                        logger.info("not release process definition id: {} , name : {}",
                                processDefinition.getId(), processDefinition.getName());
                        return result;
                    }
                }
            }
        }
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query recipients and copyers by process definition id or processInstanceId
     *
     * @param processDefineId process definition id
     * @param processInstanceId process instance id
     * @return receivers cc list
     */
    public Map<String, Object> getReceiverCc(Integer processDefineId,Integer processInstanceId) {
        Map<String, Object> result = new HashMap<>();
        logger.info("processInstanceId {}",processInstanceId);
        if(processDefineId == null && processInstanceId == null){
            throw new RuntimeException("You must set values for parameters processDefineId or processInstanceId");
        }
        if(processDefineId == null && processInstanceId != null) {
            ProcessInstance processInstance = processInstanceMapper.selectById(processInstanceId);
            if (processInstance == null) {
                throw new RuntimeException("processInstanceId is not exists");
            }
            processDefineId = processInstance.getProcessDefinitionId();
        }
        ProcessDefinition processDefinition = processDefinitionMapper.selectById(processDefineId);
        if (processDefinition == null){
            throw new RuntimeException(String.format("processDefineId %d is not exists",processDefineId));
        }

        String receivers = processDefinition.getReceivers();
        String receiversCc = processDefinition.getReceiversCc();
        Map<String,String> dataMap = new HashMap<>();
        dataMap.put(Constants.RECEIVERS,receivers);
        dataMap.put(Constants.RECEIVERS_CC,receiversCc);

        result.put(Constants.DATA_LIST, dataMap);
        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     * create command
     *
     * @param commandType
     * @param processDefineId
     * @param nodeDep
     * @param failureStrategy
     * @param startNodeList
     * @param schedule
     * @param warningType
     * @param excutorId
     * @param warningGroupId
     * @param runMode
     * @return
     * @throws ParseException
     */
    private int createCommand(CommandType commandType, int processDefineId,
                              TaskDependType nodeDep, FailureStrategy failureStrategy,
                              String startNodeList, String schedule, WarningType warningType,
                              int excutorId, int warningGroupId,
                              RunMode runMode,Priority processInstancePriority, int workerGroupId) throws ParseException {

        /**
         * instantiate command schedule instance
         */
        Command command = new Command();

        Map<String,String> cmdParam = new HashMap<>();
        if(commandType == null){
            command.setCommandType(CommandType.START_PROCESS);
        }else{
            command.setCommandType(commandType);
        }
        command.setProcessDefinitionId(processDefineId);
        if(nodeDep != null){
            command.setTaskDependType(nodeDep);
        }
        if(failureStrategy != null){
            command.setFailureStrategy(failureStrategy);
        }

        if(StringUtils.isNotEmpty(startNodeList)){
            cmdParam.put(CMDPARAM_START_NODE_NAMES, startNodeList);
        }
        if(warningType != null){
            command.setWarningType(warningType);
        }
        command.setCommandParam(JSONUtils.toJson(cmdParam));
        command.setExecutorId(excutorId);
        command.setWarningGroupId(warningGroupId);
        command.setProcessInstancePriority(processInstancePriority);
        command.setWorkerGroupId(workerGroupId);

        Date start = null;
        Date end = null;
        if(StringUtils.isNotEmpty(schedule)){
            String[] interval = schedule.split(",");
            if(interval.length == 2){
                start = DateUtils.getScheduleDate(interval[0]);
                end = DateUtils.getScheduleDate(interval[1]);
            }
        }

        if(commandType == CommandType.COMPLEMENT_DATA){
            runMode = (runMode == null) ? RunMode.RUN_MODE_SERIAL : runMode;
            if(null != start && null != end && start.before(end)){
                if(runMode == RunMode.RUN_MODE_SERIAL){
                    cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE, DateUtils.dateToString(start));
                    cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE, DateUtils.dateToString(end));
                    command.setCommandParam(JSONUtils.toJson(cmdParam));
                    return processDao.createCommand(command);
                }else if (runMode == RunMode.RUN_MODE_PARALLEL){
                    List<Schedule> schedules = processDao.queryReleaseSchedulerListByProcessDefinitionId(processDefineId);
                    List<Date> listDate = new LinkedList<>();
                    if(!CollectionUtils.isEmpty(schedules)){
                        for (Schedule item : schedules) {
                            listDate.addAll(CronUtils.getSelfFireDateList(start, end, item.getCrontab()));
                        }
                    }
                    if(!CollectionUtils.isEmpty(listDate)){
                        // loop by schedule date
                        for (Date date : listDate) {
                            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE, DateUtils.dateToString(date));
                            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE, DateUtils.dateToString(date));
                            command.setCommandParam(JSONUtils.toJson(cmdParam));
                            processDao.createCommand(command);
                        }
                        return listDate.size();
                    }else{
                        // loop by day
                        int runCunt = 0;
                        while(!start.after(end)) {
                            runCunt += 1;
                            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE, DateUtils.dateToString(start));
                            cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE, DateUtils.dateToString(start));
                            command.setCommandParam(JSONUtils.toJson(cmdParam));
                            processDao.createCommand(command);
                            start = DateUtils.getSomeDay(start, 1);
                        }
                        return runCunt;
                    }
                }
            }else{
                logger.error("there is not vaild schedule date for the process definition: id:{},date:{}",
                        processDefineId, schedule);
            }
        }else{
            command.setCommandParam(JSONUtils.toJson(cmdParam));
            return processDao.createCommand(command);
        }

        return 0;
    }

    /**
     * check result and auth
     *
     * @param loginUser
     * @param projectName
     * @param project
     * @return
     */
    private Map<String, Object> checkResultAndAuth(User loginUser, String projectName, Project project) {
        // check project auth
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status status = (Status) checkResult.get(Constants.STATUS);
        if (status != Status.SUCCESS) {
            return checkResult;
        }
        return null;
    }

}
