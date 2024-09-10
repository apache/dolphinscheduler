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

package org.apache.dolphinscheduler.dao;

import org.apache.dolphinscheduler.common.enums.AlertEvent;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.AlertWarnLevel;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Alert;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.AlertSendStatus;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.ServerAlertContent;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowAlertContent;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertSendStatusMapper;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Component
@Slf4j
public class AlertDao {

    private static final Integer QUERY_ALERT_THRESHOLD = 100;

    private static final int ADMIN_ALERT_GROUP_ID = 1;

    @Value("${alert.alarm-suppression.crash:60}")
    private Integer crashAlarmSuppression;

    @Autowired
    private AlertMapper alertMapper;

    @Autowired
    private AlertPluginInstanceMapper alertPluginInstanceMapper;

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    @Autowired
    private AlertSendStatusMapper alertSendStatusMapper;

    /**
     * insert alert
     *
     * @param alert alert
     * @return add alert result
     */
    public int addAlert(Alert alert) {
        if (null == alert.getAlertGroupId() || NumberUtils.INTEGER_ZERO.equals(alert.getAlertGroupId())) {
            log.warn("the value of alertGroupId is null or 0 ");
            return 0;
        }

        String sign = generateSign(alert);
        alert.setSign(sign);
        int count = alertMapper.insert(alert);
        log.info("add alert to db , alert: {}", alert);
        return count;
    }

    /**
     * update alert sending(execution) status
     *
     * @param alertStatus alertStatus
     * @param log         alert results json
     * @param id          id
     * @return update alert result
     */
    public int updateAlert(AlertStatus alertStatus, String log, int id) {
        Alert alert = new Alert();
        alert.setId(id);
        alert.setAlertStatus(alertStatus);
        alert.setUpdateTime(new Date());
        alert.setLog(log);
        return alertMapper.updateById(alert);
    }

    /**
     * generate sign for alert
     *
     * @param alert alert
     * @return sign's str
     */
    private String generateSign(Alert alert) {
        return Optional.of(alert)
                .map(Alert::getContent)
                .map(DigestUtils::sha1Hex)
                .map(String::toLowerCase)
                .orElse("");
    }

    /**
     * add AlertSendStatus
     *
     * @param sendStatus            alert send status
     * @param log                   log
     * @param alertId               alert id
     * @param alertPluginInstanceId alert plugin instance id
     * @return insert count
     */
    public int addAlertSendStatus(AlertStatus sendStatus, String log, int alertId, int alertPluginInstanceId) {
        AlertSendStatus alertSendStatus = new AlertSendStatus();
        alertSendStatus.setAlertId(alertId);
        alertSendStatus.setAlertPluginInstanceId(alertPluginInstanceId);
        alertSendStatus.setSendStatus(sendStatus);
        alertSendStatus.setLog(log);
        alertSendStatus.setCreateTime(new Date());
        return alertSendStatusMapper.insert(alertSendStatus);
    }

    public int insertAlertSendStatus(List<AlertSendStatus> alertSendStatuses) {
        if (CollectionUtils.isEmpty(alertSendStatuses)) {
            return 0;
        }
        return alertSendStatusMapper.batchInsert(alertSendStatuses);
    }

    /**
     * MasterServer or WorkerServer stopped
     *
     * @param host         host
     * @param serverType   serverType
     */
    public void sendServerStoppedAlert(String host, String serverType) {
        ServerAlertContent serverStopAlertContent = ServerAlertContent.newBuilder().type(serverType)
                .host(host)
                .event(AlertEvent.SERVER_DOWN)
                .warningLevel(AlertWarnLevel.SERIOUS).build();
        String content = JSONUtils.toJsonString(Lists.newArrayList(serverStopAlertContent));

        Alert alert = new Alert();
        alert.setTitle("Fault tolerance warning");
        alert.setWarningType(WarningType.FAILURE);
        alert.setAlertStatus(AlertStatus.WAIT_EXECUTION);
        alert.setContent(content);
        alert.setAlertGroupId(ADMIN_ALERT_GROUP_ID);
        alert.setCreateTime(new Date());
        alert.setUpdateTime(new Date());
        alert.setAlertType(AlertType.FAULT_TOLERANCE_WARNING);
        alert.setSign(generateSign(alert));
        // we use this method to avoid insert duplicate alert(issue #5525)
        // we modified this method to optimize performance(issue #9174)
        Date crashAlarmSuppressionStartTime = Date.from(
                LocalDateTime.now().plusMinutes(-crashAlarmSuppression).atZone(ZoneId.systemDefault()).toInstant());
        alertMapper.insertAlertWhenServerCrash(alert, crashAlarmSuppressionStartTime);
    }

    /**
     * workflow time out alert
     *
     * @param workflowInstance workflowInstance
     * @param projectUser     projectUser
     */
    public void sendWorkflowTimeoutAlert(WorkflowInstance workflowInstance, ProjectUser projectUser) {
        int alertGroupId = workflowInstance.getWarningGroupId();
        Alert alert = new Alert();
        List<WorkflowAlertContent> workflowAlertContentList = new ArrayList<>(1);
        WorkflowAlertContent workflowAlertContent = WorkflowAlertContent.builder()
                .projectCode(projectUser.getProjectCode())
                .projectName(projectUser.getProjectName())
                .owner(projectUser.getUserName())
                .workflowInstanceId(workflowInstance.getId())
                .workflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode())
                .workflowInstanceName(workflowInstance.getName())
                .commandType(workflowInstance.getCommandType())
                .workflowExecutionStatus(workflowInstance.getState())
                .runTimes(workflowInstance.getRunTimes())
                .workflowStartTime(workflowInstance.getStartTime())
                .workflowHost(workflowInstance.getHost())
                .event(AlertEvent.TIME_OUT)
                .warnLevel(AlertWarnLevel.MIDDLE)
                .build();
        workflowAlertContentList.add(workflowAlertContent);
        String content = JSONUtils.toJsonString(workflowAlertContentList);
        alert.setTitle("Workflow Timeout Warn");
        alert.setProjectCode(projectUser.getProjectCode());
        alert.setWorkflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode());
        alert.setWorkflowInstanceId(workflowInstance.getId());
        alert.setAlertType(AlertType.WORKFLOW_INSTANCE_TIMEOUT);
        saveTaskTimeoutAlert(alert, content, alertGroupId);
    }

    private void saveTaskTimeoutAlert(Alert alert, String content, int alertGroupId) {
        alert.setAlertGroupId(alertGroupId);
        alert.setWarningType(WarningType.FAILURE);
        alert.setContent(content);
        alert.setCreateTime(new Date());
        alert.setUpdateTime(new Date());
        String sign = generateSign(alert);
        alert.setSign(sign);
        alertMapper.insert(alert);
    }

    /**
     * task timeout warn
     *
     * @param workflowInstance workflowInstance
     * @param taskInstance    taskInstance
     * @param projectUser     projectUser
     */
    public void sendTaskTimeoutAlert(WorkflowInstance workflowInstance,
                                     TaskInstance taskInstance,
                                     ProjectUser projectUser) {
        Alert alert = new Alert();
        List<WorkflowAlertContent> workflowAlertContentList = new ArrayList<>(1);
        WorkflowAlertContent workflowAlertContent = WorkflowAlertContent.builder()
                .projectCode(projectUser.getProjectCode())
                .projectName(projectUser.getProjectName())
                .owner(projectUser.getUserName())
                .workflowInstanceId(workflowInstance.getId())
                .workflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode())
                .workflowInstanceName(workflowInstance.getName())
                .taskCode(taskInstance.getTaskCode())
                .taskName(taskInstance.getName())
                .taskType(taskInstance.getTaskType())
                .taskStartTime(taskInstance.getStartTime())
                .taskHost(taskInstance.getHost())
                .event(AlertEvent.TIME_OUT)
                .warnLevel(AlertWarnLevel.MIDDLE)
                .build();
        workflowAlertContentList.add(workflowAlertContent);
        String content = JSONUtils.toJsonString(workflowAlertContentList);
        alert.setTitle("Task Timeout Warn");
        alert.setProjectCode(projectUser.getProjectCode());
        alert.setWorkflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode());
        alert.setWorkflowInstanceId(workflowInstance.getId());
        alert.setAlertType(AlertType.TASK_TIMEOUT);
        saveTaskTimeoutAlert(alert, content, workflowInstance.getWarningGroupId());
    }

    /**
     * List pending alerts which id > minAlertId and status = {@link AlertStatus#WAIT_EXECUTION} order by id asc.
     */
    public List<Alert> listPendingAlerts(int minAlertId) {
        return alertMapper.listingAlertByStatus(minAlertId, AlertStatus.WAIT_EXECUTION.getCode(),
                QUERY_ALERT_THRESHOLD);
    }

    public List<Alert> listAlerts(int workflowInstanceId) {
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<Alert>()
                .eq(Alert::getWorkflowInstanceId, workflowInstanceId);
        return alertMapper.selectList(wrapper);
    }

    /**
     * list all alert plugin instance by alert group id
     *
     * @param alertGroupId alert group id
     * @return AlertPluginInstance list
     */
    public List<AlertPluginInstance> listInstanceByAlertGroupId(int alertGroupId) {
        String alertInstanceIdsParam = alertGroupMapper.queryAlertGroupInstanceIdsById(alertGroupId);
        if (!Strings.isNullOrEmpty(alertInstanceIdsParam)) {
            String[] idsArray = alertInstanceIdsParam.split(",");
            List<Integer> ids = Arrays.stream(idsArray)
                    .map(s -> Integer.parseInt(s.trim()))
                    .collect(Collectors.toList());
            return alertPluginInstanceMapper.queryByIds(ids);
        }
        return null;
    }

    public AlertPluginInstanceMapper getAlertPluginInstanceMapper() {
        return alertPluginInstanceMapper;
    }

    public void setAlertPluginInstanceMapper(AlertPluginInstanceMapper alertPluginInstanceMapper) {
        this.alertPluginInstanceMapper = alertPluginInstanceMapper;
    }

    public AlertGroupMapper getAlertGroupMapper() {
        return alertGroupMapper;
    }

    public void setAlertGroupMapper(AlertGroupMapper alertGroupMapper) {
        this.alertGroupMapper = alertGroupMapper;
    }

    public void setCrashAlarmSuppression(Integer crashAlarmSuppression) {
        this.crashAlarmSuppression = crashAlarmSuppression;
    }

    public void deleteByWorkflowInstanceId(Integer workflowInstanceId) {
        if (workflowInstanceId == null) {
            return;
        }
        List<Alert> alertList = alertMapper.selectByWorkflowInstanceId(workflowInstanceId);
        if (CollectionUtils.isEmpty(alertList)) {
            return;
        }
        alertMapper.deleteByWorkflowInstanceId(workflowInstanceId);
        List<Integer> alertIds = alertList
                .stream()
                .map(Alert::getId)
                .collect(Collectors.toList());
        alertSendStatusMapper.deleteByAlertIds(alertIds);
    }
}
