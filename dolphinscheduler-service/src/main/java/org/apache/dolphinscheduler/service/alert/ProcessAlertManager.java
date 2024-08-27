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

package org.apache.dolphinscheduler.service.alert;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.DqExecuteResult;
import org.apache.dolphinscheduler.dao.entity.DqExecuteResultAlertContent;
import org.apache.dolphinscheduler.dao.entity.WorkflowAlertContent;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.TaskAlertContent;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.DqTaskState;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * process alert manager
 */
@Component
@Slf4j
public class ProcessAlertManager {

    /**
     * alert dao
     */
    @Autowired
    private AlertDao alertDao;

    @Autowired
    private ProcessDefinitionLogMapper processDefinitionLogMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * command type convert chinese
     *
     * @param commandType command type
     * @return command name
     */
    private String getCommandCnName(CommandType commandType) {
        switch (commandType) {
            case RECOVER_TOLERANCE_FAULT_PROCESS:
                return "recover tolerance fault process";
            case RECOVER_SUSPENDED_PROCESS:
                return "recover suspended process";
            case START_CURRENT_TASK_PROCESS:
                return "start current task process";
            case START_FAILURE_TASK_PROCESS:
                return "start failure task process";
            case START_PROCESS:
                return "start process";
            case REPEAT_RUNNING:
                return "repeat running";
            case SCHEDULER:
                return "scheduler";
            case COMPLEMENT_DATA:
                return "complement data";
            case PAUSE:
                return "pause";
            case STOP:
                return "stop";
            default:
                return "unknown type";
        }
    }

    /**
     * get process instance content
     *
     * @param workflowInstance process instance
     * @param taskInstances task instance list
     * @return process instance format content
     */
    public String getContentProcessInstance(WorkflowInstance workflowInstance,
                                            List<TaskInstance> taskInstances,
                                            ProjectUser projectUser) {

        String res = "";
        WorkflowDefinitionLog processDefinitionLog = processDefinitionLogMapper
                .queryByDefinitionCodeAndVersion(workflowInstance.getProcessDefinitionCode(),
                        workflowInstance.getProcessDefinitionVersion());

        String modifyBy = "";
        if (processDefinitionLog != null) {
            User operator = userMapper.selectById(processDefinitionLog.getOperator());
            modifyBy = operator == null ? "" : operator.getUserName();
        }

        if (workflowInstance.getState().isSuccess()) {
            List<WorkflowAlertContent> successTaskList = new ArrayList<>(1);
            WorkflowAlertContent workflowAlertContent = WorkflowAlertContent.builder()
                    .projectCode(projectUser.getProjectCode())
                    .projectName(projectUser.getProjectName())
                    .owner(projectUser.getUserName())
                    .processId(workflowInstance.getId())
                    .processDefinitionCode(workflowInstance.getProcessDefinitionCode())
                    .processName(workflowInstance.getName())
                    .processType(workflowInstance.getCommandType())
                    .processState(workflowInstance.getState())
                    .modifyBy(modifyBy)
                    .recovery(workflowInstance.getRecovery())
                    .runTimes(workflowInstance.getRunTimes())
                    .processStartTime(workflowInstance.getStartTime())
                    .processEndTime(workflowInstance.getEndTime())
                    .processHost(workflowInstance.getHost())
                    .build();
            successTaskList.add(workflowAlertContent);
            res = JSONUtils.toJsonString(successTaskList);
        } else if (workflowInstance.getState().isFailure()) {

            List<WorkflowAlertContent> failedTaskList = new ArrayList<>();
            for (TaskInstance task : taskInstances) {
                if (task.getState().isSuccess()) {
                    continue;
                }
                WorkflowAlertContent workflowAlertContent = WorkflowAlertContent.builder()
                        .projectCode(projectUser.getProjectCode())
                        .projectName(projectUser.getProjectName())
                        .owner(projectUser.getUserName())
                        .processId(workflowInstance.getId())
                        .processDefinitionCode(workflowInstance.getProcessDefinitionCode())
                        .processName(workflowInstance.getName())
                        .modifyBy(modifyBy)
                        .taskCode(task.getTaskCode())
                        .taskName(task.getName())
                        .taskType(task.getTaskType())
                        .taskState(task.getState())
                        .taskStartTime(task.getStartTime())
                        .taskEndTime(task.getEndTime())
                        .taskHost(task.getHost())
                        .taskPriority(task.getTaskInstancePriority().getDescp())
                        .logPath(task.getLogPath())
                        .build();
                failedTaskList.add(workflowAlertContent);
            }
            res = JSONUtils.toJsonString(failedTaskList);
        }

        return res;
    }

    /**
     * getting worker fault tolerant content
     *
     * @param workflowInstance process instance
     * @param toleranceTaskList tolerance task list
     * @return worker tolerance content
     */
    private String getWorkerToleranceContent(WorkflowInstance workflowInstance, List<TaskInstance> toleranceTaskList) {

        List<WorkflowAlertContent> toleranceTaskInstanceList = new ArrayList<>();

        WorkflowDefinitionLog processDefinitionLog = processDefinitionLogMapper
                .queryByDefinitionCodeAndVersion(workflowInstance.getProcessDefinitionCode(),
                        workflowInstance.getProcessDefinitionVersion());
        String modifyBy = "";
        if (processDefinitionLog != null) {
            User operator = userMapper.selectById(processDefinitionLog.getOperator());
            modifyBy = operator == null ? "" : operator.getUserName();
        }

        for (TaskInstance taskInstance : toleranceTaskList) {
            WorkflowAlertContent workflowAlertContent = WorkflowAlertContent.builder()
                    .processId(workflowInstance.getId())
                    .processDefinitionCode(workflowInstance.getProcessDefinitionCode())
                    .processName(workflowInstance.getName())
                    .modifyBy(modifyBy)
                    .taskCode(taskInstance.getTaskCode())
                    .taskName(taskInstance.getName())
                    .taskHost(taskInstance.getHost())
                    .taskPriority(taskInstance.getTaskInstancePriority().getDescp())
                    .retryTimes(taskInstance.getRetryTimes())
                    .build();
            toleranceTaskInstanceList.add(workflowAlertContent);
        }
        return JSONUtils.toJsonString(toleranceTaskInstanceList);
    }

    /**
     * send worker alert fault tolerance
     *
     * @param workflowInstance process instance
     * @param toleranceTaskList tolerance task list
     */
    public void sendAlertWorkerToleranceFault(WorkflowInstance workflowInstance, List<TaskInstance> toleranceTaskList) {
        try {
            Alert alert = new Alert();
            alert.setTitle("worker fault tolerance");
            String content = getWorkerToleranceContent(workflowInstance, toleranceTaskList);
            alert.setContent(content);
            alert.setWarningType(WarningType.FAILURE);
            alert.setCreateTime(new Date());
            alert.setAlertGroupId(
                    workflowInstance.getWarningGroupId() == null ? 1 : workflowInstance.getWarningGroupId());
            alert.setAlertType(AlertType.FAULT_TOLERANCE_WARNING);
            alertDao.addAlert(alert);

        } catch (Exception e) {
            log.error("send alert failed:{} ", e.getMessage());
        }

    }

    /**
     * send process instance alert
     *
     * @param workflowInstance process instance
     * @param taskInstances task instance list
     */
    public void sendAlertProcessInstance(WorkflowInstance workflowInstance,
                                         List<TaskInstance> taskInstances,
                                         ProjectUser projectUser) {
        if (!isNeedToSendWarning(workflowInstance)) {
            return;
        }
        Alert alert = new Alert();
        String cmdName = getCommandCnName(workflowInstance.getCommandType());
        String success = workflowInstance.getState().isSuccess() ? "success" : "failed";
        alert.setTitle(cmdName + " " + success);
        alert.setWarningType(workflowInstance.getState().isSuccess() ? WarningType.SUCCESS : WarningType.FAILURE);
        String content = getContentProcessInstance(workflowInstance, taskInstances, projectUser);
        alert.setContent(content);
        alert.setAlertGroupId(workflowInstance.getWarningGroupId());
        alert.setCreateTime(new Date());
        alert.setProjectCode(projectUser.getProjectCode());
        alert.setProcessDefinitionCode(workflowInstance.getProcessDefinitionCode());
        alert.setProcessInstanceId(workflowInstance.getId());
        alert.setAlertType(workflowInstance.getState().isSuccess() ? AlertType.PROCESS_INSTANCE_SUCCESS
                : AlertType.PROCESS_INSTANCE_FAILURE);
        alertDao.addAlert(alert);
    }

    /**
     * check if need to be send warning
     *
     * @param workflowInstance
     * @return
     */
    public boolean isNeedToSendWarning(WorkflowInstance workflowInstance) {
        if (Flag.YES == workflowInstance.getIsSubProcess()) {
            return false;
        }
        boolean sendWarning = false;
        WarningType warningType = workflowInstance.getWarningType();
        switch (warningType) {
            case ALL:
                if (workflowInstance.getState().isFinished()) {
                    sendWarning = true;
                }
                break;
            case SUCCESS:
                if (workflowInstance.getState().isSuccess()) {
                    sendWarning = true;
                }
                break;
            case FAILURE:
                if (workflowInstance.getState().isFailure()) {
                    sendWarning = true;
                }
                break;
            default:
        }
        return sendWarning;
    }

    /**
     * Send a close alert event, if the processInstance has sent alert before, then will insert a closed event.
     *
     * @param workflowInstance success process instance
     */
    public void closeAlert(WorkflowInstance workflowInstance) {
        if (!PropertyUtils.getBoolean(Constants.AUTO_CLOSE_ALERT, false)) {
            return;
        }
        List<Alert> alerts = alertDao.listAlerts(workflowInstance.getId());
        if (CollectionUtils.isEmpty(alerts)) {
            // no need to close alert
            return;
        }

        Alert alert = new Alert();
        alert.setAlertGroupId(workflowInstance.getWarningGroupId());
        alert.setUpdateTime(new Date());
        alert.setCreateTime(new Date());
        alert.setProjectCode(workflowInstance.getWorkflowDefinition().getProjectCode());
        alert.setProcessDefinitionCode(workflowInstance.getProcessDefinitionCode());
        alert.setProcessInstanceId(workflowInstance.getId());
        alert.setAlertType(AlertType.CLOSE_ALERT);
        alertDao.addAlert(alert);
    }

    /**
     * send process timeout alert
     *
     * @param workflowInstance process instance
     * @param projectUser     projectUser
     */
    public void sendProcessTimeoutAlert(WorkflowInstance workflowInstance, ProjectUser projectUser) {
        alertDao.sendProcessTimeoutAlert(workflowInstance, projectUser);
    }

    /**
     * send data quality task alert
     */
    public void sendDataQualityTaskExecuteResultAlert(DqExecuteResult result, WorkflowInstance workflowInstance) {
        Alert alert = new Alert();
        String state = DqTaskState.of(result.getState()).getDescription();
        alert.setTitle("DataQualityResult [" + result.getTaskName() + "] " + state);
        String content = getDataQualityAlterContent(result);
        alert.setContent(content);
        alert.setAlertGroupId(workflowInstance.getWarningGroupId());
        alert.setCreateTime(new Date());
        alert.setProjectCode(result.getProjectCode());
        alert.setProcessDefinitionCode(workflowInstance.getProcessDefinitionCode());
        alert.setProcessInstanceId(workflowInstance.getId());
        // might need to change to data quality status
        alert.setAlertType(workflowInstance.getState().isSuccess() ? AlertType.PROCESS_INSTANCE_SUCCESS
                : AlertType.PROCESS_INSTANCE_FAILURE);
        alertDao.addAlert(alert);
    }

    /**
     * send data quality task error alert
     */
    public void sendTaskErrorAlert(TaskInstance taskInstance, WorkflowInstance workflowInstance) {
        Alert alert = new Alert();
        alert.setTitle("Task [" + taskInstance.getName() + "] Failure Warning");
        String content = getTaskAlterContent(taskInstance);
        alert.setContent(content);
        alert.setAlertGroupId(workflowInstance.getWarningGroupId());
        alert.setCreateTime(new Date());
        alert.setProcessDefinitionCode(workflowInstance.getProcessDefinitionCode());
        alert.setProcessInstanceId(workflowInstance.getId());
        alert.setAlertType(AlertType.TASK_FAILURE);
        alertDao.addAlert(alert);
    }

    /**
     * getDataQualityAlterContent
     * @param result DqExecuteResult
     * @return String String
     */
    public String getDataQualityAlterContent(DqExecuteResult result) {

        DqExecuteResultAlertContent content = DqExecuteResultAlertContent.newBuilder()
                .processDefinitionId(result.getProcessDefinitionId())
                .processDefinitionName(result.getProcessDefinitionName())
                .processInstanceId(result.getProcessInstanceId())
                .processInstanceName(result.getProcessInstanceName())
                .taskInstanceId(result.getTaskInstanceId())
                .taskName(result.getTaskName())
                .ruleType(result.getRuleType())
                .ruleName(result.getRuleName())
                .statisticsValue(result.getStatisticsValue())
                .comparisonValue(result.getComparisonValue())
                .checkType(result.getCheckType())
                .threshold(result.getThreshold())
                .operator(result.getOperator())
                .failureStrategy(result.getFailureStrategy())
                .userId(result.getUserId())
                .userName(result.getUserName())
                .state(result.getState())
                .errorDataPath(result.getErrorOutputPath())
                .build();

        return JSONUtils.toJsonString(content);
    }

    /**
     * getTaskAlterContent
     * @param taskInstance TaskInstance
     * @return String String
     */
    public String getTaskAlterContent(TaskInstance taskInstance) {

        TaskAlertContent content = TaskAlertContent.builder()
                .processInstanceName(taskInstance.getProcessInstanceName())
                .processInstanceId(taskInstance.getProcessInstanceId())
                .taskInstanceId(taskInstance.getId())
                .taskName(taskInstance.getName())
                .taskType(taskInstance.getTaskType())
                .state(taskInstance.getState())
                .startTime(taskInstance.getStartTime())
                .endTime(taskInstance.getEndTime())
                .host(taskInstance.getHost())
                .taskPriority(taskInstance.getTaskInstancePriority().getDescp())
                .logPath(taskInstance.getLogPath())
                .build();

        return JSONUtils.toJsonString(content);
    }

    public void sendTaskTimeoutAlert(WorkflowInstance workflowInstance,
                                     TaskInstance taskInstance,
                                     ProjectUser projectUser) {
        alertDao.sendTaskTimeoutAlert(workflowInstance, taskInstance, projectUser);
    }

    /**
     *
     * check node type and process blocking flag, then insert a block record into db
     *
     * @param workflowInstance process instance
     * @param projectUser the project owner
     */
    public void sendProcessBlockingAlert(WorkflowInstance workflowInstance,
                                         ProjectUser projectUser) {
        Alert alert = new Alert();
        String cmdName = getCommandCnName(workflowInstance.getCommandType());
        List<WorkflowAlertContent> blockingNodeList = new ArrayList<>(1);

        WorkflowDefinitionLog processDefinitionLog = processDefinitionLogMapper
                .queryByDefinitionCodeAndVersion(workflowInstance.getProcessDefinitionCode(),
                        workflowInstance.getProcessDefinitionVersion());

        String modifyBy = "";
        if (processDefinitionLog != null) {
            User operator = userMapper.selectById(processDefinitionLog.getOperator());
            modifyBy = operator == null ? "" : operator.getUserName();
        }

        WorkflowAlertContent workflowAlertContent = WorkflowAlertContent.builder()
                .projectCode(projectUser.getProjectCode())
                .projectName(projectUser.getProjectName())
                .owner(projectUser.getUserName())
                .processId(workflowInstance.getId())
                .processName(workflowInstance.getName())
                .processType(workflowInstance.getCommandType())
                .processState(workflowInstance.getState())
                .modifyBy(modifyBy)
                .runTimes(workflowInstance.getRunTimes())
                .processStartTime(workflowInstance.getStartTime())
                .processEndTime(workflowInstance.getEndTime())
                .processHost(workflowInstance.getHost())
                .build();
        blockingNodeList.add(workflowAlertContent);
        String content = JSONUtils.toJsonString(blockingNodeList);
        alert.setTitle(cmdName + " Blocked");
        alert.setContent(content);
        alert.setAlertGroupId(workflowInstance.getWarningGroupId());
        alert.setCreateTime(new Date());
        alert.setProjectCode(projectUser.getProjectCode());
        alert.setProcessDefinitionCode(workflowInstance.getProcessDefinitionCode());
        alert.setProcessInstanceId(workflowInstance.getId());
        alert.setAlertType(AlertType.PROCESS_INSTANCE_BLOCKED);
        alertDao.addAlert(alert);
    }
}
