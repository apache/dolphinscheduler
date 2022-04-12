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
import org.apache.dolphinscheduler.dao.entity.ProcessAlertContent;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.ServerAlertContent;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertSendStatusMapper;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;

@Component
public class AlertDao {

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
        String sign = generateSign(alert);
        alert.setSign(sign);
        return alertMapper.insert(alert);
    }

    /**
     * update alert sending(execution) status
     *
     * @param alertStatus alertStatus
     * @param log log
     * @param id id
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
    private String generateSign (Alert alert) {
        return Optional.of(alert)
                .map(Alert::getContent)
                .map(DigestUtils::sha1Hex)
                .map(String::toLowerCase)
                .orElse(StringUtils.EMPTY);
    }

    /**
     * add AlertSendStatus
     *
     * @param sendStatus alert send status
     * @param log log
     * @param alertId alert id
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

    /**
     * MasterServer or WorkerServer stopped
     *
     * @param alertGroupId alertGroupId
     * @param host host
     * @param serverType serverType
     */
    public void sendServerStoppedAlert(int alertGroupId, String host, String serverType) {
        ServerAlertContent serverStopAlertContent = ServerAlertContent.newBuilder().
                type(serverType)
                .host(host)
                .event(AlertEvent.SERVER_DOWN)
                .warningLevel(AlertWarnLevel.SERIOUS).
                build();
        String content = JSONUtils.toJsonString(Lists.newArrayList(serverStopAlertContent));

        Alert alert = new Alert();
        alert.setTitle("Fault tolerance warning");
        alert.setWarningType(WarningType.FAILURE);
        alert.setAlertStatus(AlertStatus.WAIT_EXECUTION);
        alert.setContent(content);
        alert.setAlertGroupId(alertGroupId);
        alert.setCreateTime(new Date());
        alert.setUpdateTime(new Date());
        alert.setAlertType(AlertType.FAULT_TOLERANCE_WARNING);
        alert.setSign(generateSign(alert));
        // we use this method to avoid insert duplicate alert(issue #5525)
        // we modified this method to optimize performance(issue #9174)
        Date crashAlarmSuppressionStartTime = DateTime.now().plusMinutes(-crashAlarmSuppression).toDate();
        alertMapper.insertAlertWhenServerCrash(alert, crashAlarmSuppressionStartTime);
    }

    /**
     * process time out alert
     *
     * @param processInstance processInstance
     * @param projectUser projectUser
     */
    public void sendProcessTimeoutAlert(ProcessInstance processInstance, ProjectUser projectUser) {
        int alertGroupId = processInstance.getWarningGroupId();
        Alert alert = new Alert();
        List<ProcessAlertContent> processAlertContentList = new ArrayList<>(1);
        ProcessAlertContent processAlertContent = ProcessAlertContent.newBuilder()
                .projectCode(projectUser.getProjectCode())
                .projectName(projectUser.getProjectName())
                .owner(projectUser.getUserName())
                .processId(processInstance.getId())
                .processDefinitionCode(processInstance.getProcessDefinitionCode())
                .processName(processInstance.getName())
                .processType(processInstance.getCommandType())
                .processState(processInstance.getState())
                .runTimes(processInstance.getRunTimes())
                .processStartTime(processInstance.getStartTime())
                .processHost(processInstance.getHost())
                .event(AlertEvent.TIME_OUT)
                .warningLevel(AlertWarnLevel.MIDDLE)
                .build();
        processAlertContentList.add(processAlertContent);
        String content = JSONUtils.toJsonString(processAlertContentList);
        alert.setTitle("Process Timeout Warn");
        alert.setProjectCode(projectUser.getProjectCode());
        alert.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        alert.setProcessInstanceId(processInstance.getId());
        alert.setAlertType(AlertType.PROCESS_INSTANCE_TIMEOUT);
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
     * @param processInstance processInstanceId
     * @param taskInstance taskInstance
     * @param projectUser projectUser
     */
    public void sendTaskTimeoutAlert(ProcessInstance processInstance, TaskInstance taskInstance, ProjectUser projectUser) {
        Alert alert = new Alert();
        List<ProcessAlertContent> processAlertContentList = new ArrayList<>(1);
        ProcessAlertContent processAlertContent = ProcessAlertContent.newBuilder()
                .projectCode(projectUser.getProjectCode())
                .projectName(projectUser.getProjectName())
                .owner(projectUser.getUserName())
                .processId(processInstance.getId())
                .processDefinitionCode(processInstance.getProcessDefinitionCode())
                .processName(processInstance.getName())
                .taskCode(taskInstance.getTaskCode())
                .taskName(taskInstance.getName())
                .taskType(taskInstance.getTaskType())
                .taskStartTime(taskInstance.getStartTime())
                .taskHost(taskInstance.getHost())
                .event(AlertEvent.TIME_OUT)
                .warningLevel(AlertWarnLevel.MIDDLE)
                .build();
        processAlertContentList.add(processAlertContent);
        String content = JSONUtils.toJsonString(processAlertContentList);
        alert.setTitle("Task Timeout Warn");
        alert.setProjectCode(projectUser.getProjectCode());
        alert.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        alert.setProcessInstanceId(processInstance.getId());
        alert.setAlertType(AlertType.TASK_TIMEOUT);
        saveTaskTimeoutAlert(alert, content, processInstance.getWarningGroupId());
    }

    /**
     * List alerts that are pending for execution
     */
    public List<Alert> listPendingAlerts() {
        LambdaQueryWrapper<Alert> wrapper = new QueryWrapper<>(new Alert()).lambda()
                .eq(Alert::getAlertStatus, AlertStatus.WAIT_EXECUTION);
        return alertMapper.selectList(wrapper);
    }

    /**
     * for test
     *
     * @return AlertMapper
     */
    public AlertMapper getAlertMapper() {
        return alertMapper;
    }

    /**
     * list all alert plugin instance by alert group id
     *
     * @param alertGroupId alert group id
     * @return AlertPluginInstance list
     */
    public List<AlertPluginInstance> listInstanceByAlertGroupId(int alertGroupId) {
        String alertInstanceIdsParam = alertGroupMapper.queryAlertGroupInstanceIdsById(alertGroupId);
        if (StringUtils.isNotBlank(alertInstanceIdsParam)) {
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
}
