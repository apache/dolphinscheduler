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
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.TaskAlertContent;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowAlertContent;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionLogMapper;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.DqTaskState;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkflowAlertManager {

    /**
     * alert dao
     */
    @Autowired
    private AlertDao alertDao;

    @Autowired
    private WorkflowDefinitionLogMapper workflowDefinitionLogMapper;

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
                return "recover fault tolerance workflow";
            case RECOVER_SUSPENDED_PROCESS:
                return "recover suspended workflow";
            case START_CURRENT_TASK_PROCESS:
                return "start current task workflow";
            case START_FAILURE_TASK_PROCESS:
                return "start failure task workflow";
            case START_PROCESS:
                return "start workflow";
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
     * get workflow instance content
     *
     * @param workflowInstance workflow instance
     * @param taskInstances task instance list
     * @return workflow instance format content
     */
    public String getContentWorkflowInstance(WorkflowInstance workflowInstance,
                                             List<TaskInstance> taskInstances,
                                             ProjectUser projectUser) {

        String res = "";
        WorkflowDefinitionLog workflowDefinitionLog = workflowDefinitionLogMapper
                .queryByDefinitionCodeAndVersion(workflowInstance.getWorkflowDefinitionCode(),
                        workflowInstance.getWorkflowDefinitionVersion());

        String modifyBy = "";
        if (workflowDefinitionLog != null) {
            User operator = userMapper.selectById(workflowDefinitionLog.getOperator());
            modifyBy = operator == null ? "" : operator.getUserName();
        }

        if (workflowInstance.getState().isSuccess()) {
            List<WorkflowAlertContent> successTaskList = new ArrayList<>(1);
            WorkflowAlertContent workflowAlertContent = WorkflowAlertContent.builder()
                    .projectCode(projectUser.getProjectCode())
                    .projectName(projectUser.getProjectName())
                    .owner(projectUser.getUserName())
                    .workflowInstanceId(workflowInstance.getId())
                    .workflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode())
                    .workflowInstanceName(workflowInstance.getName())
                    .commandType(workflowInstance.getCommandType())
                    .workflowExecutionStatus(workflowInstance.getState())
                    .modifyBy(modifyBy)
                    .recovery(workflowInstance.getRecovery())
                    .runTimes(workflowInstance.getRunTimes())
                    .workflowStartTime(workflowInstance.getStartTime())
                    .workflowEndTime(workflowInstance.getEndTime())
                    .workflowHost(workflowInstance.getHost())
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
                        .workflowInstanceId(workflowInstance.getId())
                        .workflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode())
                        .workflowInstanceName(workflowInstance.getName())
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
     * @param workflowInstance workflow instance
     * @param toleranceTaskList tolerance task list
     * @return worker tolerance content
     */
    private String getWorkerToleranceContent(WorkflowInstance workflowInstance, List<TaskInstance> toleranceTaskList) {

        List<WorkflowAlertContent> toleranceTaskInstanceList = new ArrayList<>();

        WorkflowDefinitionLog workflowDefinitionLog = workflowDefinitionLogMapper
                .queryByDefinitionCodeAndVersion(workflowInstance.getWorkflowDefinitionCode(),
                        workflowInstance.getWorkflowDefinitionVersion());
        String modifyBy = "";
        if (workflowDefinitionLog != null) {
            User operator = userMapper.selectById(workflowDefinitionLog.getOperator());
            modifyBy = operator == null ? "" : operator.getUserName();
        }

        for (TaskInstance taskInstance : toleranceTaskList) {
            WorkflowAlertContent workflowAlertContent = WorkflowAlertContent.builder()
                    .workflowInstanceId(workflowInstance.getId())
                    .workflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode())
                    .workflowInstanceName(workflowInstance.getName())
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
     * @param workflowInstance workflow instance
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
     * send workflow instance alert
     *
     * @param workflowInstance workflow instance
     * @param taskInstances task instance list
     */
    public void sendAlertWorkflowInstance(WorkflowInstance workflowInstance,
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
        String content = getContentWorkflowInstance(workflowInstance, taskInstances, projectUser);
        alert.setContent(content);
        alert.setAlertGroupId(workflowInstance.getWarningGroupId());
        alert.setCreateTime(new Date());
        alert.setProjectCode(projectUser.getProjectCode());
        alert.setWorkflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode());
        alert.setWorkflowInstanceId(workflowInstance.getId());
        alert.setAlertType(workflowInstance.getState().isSuccess() ? AlertType.WORKFLOW_INSTANCE_SUCCESS
                : AlertType.WORKFLOW_INSTANCE_FAILURE);
        alertDao.addAlert(alert);
    }

    /**
     * check if need to be sent warning
     *
     * @param workflowInstance
     * @return
     */
    public boolean isNeedToSendWarning(WorkflowInstance workflowInstance) {
        if (Flag.YES == workflowInstance.getIsSubWorkflow()) {
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
     * Send a close alert event, if the workflowInstance has sent alert before, then will insert a closed event.
     *
     * @param workflowInstance success workflow instance
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
        alert.setWorkflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode());
        alert.setWorkflowInstanceId(workflowInstance.getId());
        alert.setAlertType(AlertType.CLOSE_ALERT);
        alertDao.addAlert(alert);
    }

    /**
     * send workflow timeout alert
     *
     * @param workflowInstance workflow instance
     * @param projectUser     projectUser
     */
    public void sendWorkflowTimeoutAlert(WorkflowInstance workflowInstance, ProjectUser projectUser) {
        alertDao.sendWorkflowTimeoutAlert(workflowInstance, projectUser);
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
        alert.setWorkflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode());
        alert.setWorkflowInstanceId(workflowInstance.getId());
        // might need to change to data quality status
        alert.setAlertType(workflowInstance.getState().isSuccess() ? AlertType.WORKFLOW_INSTANCE_SUCCESS
                : AlertType.WORKFLOW_INSTANCE_FAILURE);
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
        alert.setWorkflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode());
        alert.setWorkflowInstanceId(workflowInstance.getId());
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
                .processDefinitionId(result.getWorkflowDefinitionId())
                .processDefinitionName(result.getWorkflowDefinitionName())
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
                .processInstanceName(taskInstance.getWorkflowInstanceName())
                .processInstanceId(taskInstance.getWorkflowInstanceId())
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
     * check node type and workflow blocking flag, then insert a block record into db
     *
     * @param workflowInstance workflow instance
     * @param projectUser the project owner
     */
    public void sendWorkflowBlockingAlert(WorkflowInstance workflowInstance,
                                          ProjectUser projectUser) {
        Alert alert = new Alert();
        String cmdName = getCommandCnName(workflowInstance.getCommandType());
        List<WorkflowAlertContent> blockingNodeList = new ArrayList<>(1);

        WorkflowDefinitionLog workflowDefinitionLog = workflowDefinitionLogMapper
                .queryByDefinitionCodeAndVersion(workflowInstance.getWorkflowDefinitionCode(),
                        workflowInstance.getWorkflowDefinitionVersion());

        String modifyBy = "";
        if (workflowDefinitionLog != null) {
            User operator = userMapper.selectById(workflowDefinitionLog.getOperator());
            modifyBy = operator == null ? "" : operator.getUserName();
        }

        WorkflowAlertContent workflowAlertContent = WorkflowAlertContent.builder()
                .projectCode(projectUser.getProjectCode())
                .projectName(projectUser.getProjectName())
                .owner(projectUser.getUserName())
                .workflowInstanceId(workflowInstance.getId())
                .workflowInstanceName(workflowInstance.getName())
                .commandType(workflowInstance.getCommandType())
                .workflowExecutionStatus(workflowInstance.getState())
                .modifyBy(modifyBy)
                .runTimes(workflowInstance.getRunTimes())
                .workflowStartTime(workflowInstance.getStartTime())
                .workflowEndTime(workflowInstance.getEndTime())
                .workflowHost(workflowInstance.getHost())
                .build();
        blockingNodeList.add(workflowAlertContent);
        String content = JSONUtils.toJsonString(blockingNodeList);
        alert.setTitle(cmdName + " Blocked");
        alert.setContent(content);
        alert.setAlertGroupId(workflowInstance.getWarningGroupId());
        alert.setCreateTime(new Date());
        alert.setProjectCode(projectUser.getProjectCode());
        alert.setWorkflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode());
        alert.setWorkflowInstanceId(workflowInstance.getId());
        alert.setAlertType(AlertType.WORKFLOW_INSTANCE_BLOCKED);
        alertDao.addAlert(alert);
    }
}
