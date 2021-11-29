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

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.dolphinscheduler.common.enums.AlertEvent;
import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.AlertWarnLevel;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AlertDao {
    @Autowired
    private AlertMapper alertMapper;

    @Autowired
    private AlertPluginInstanceMapper alertPluginInstanceMapper;

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    /**
     * insert alert
     *
     * @param alert alert
     * @return add alert result
     */
    public int addAlert(Alert alert) {
        return alertMapper.insert(alert);
    }

    /**
     * update alert
     *
     * @param alertStatus alertStatus
     * @param log log
     * @param id id
     * @return update alert result
     */
    public int updateAlert(AlertStatus alertStatus, String log, int id) {
        Alert alert = alertMapper.selectById(id);
        alert.setAlertStatus(alertStatus);
        alert.setUpdateTime(new Date());
        alert.setLog(log);
        return alertMapper.updateById(alert);
    }

    /**
     * MasterServer or WorkerServer stoped
     *
     * @param alertGroupId alertGroupId
     * @param host host
     * @param serverType serverType
     */
    public void sendServerStopedAlert(int alertGroupId, String host, String serverType) {
        ServerAlertContent serverStopAlertContent = ServerAlertContent.newBuilder().
                type(serverType)
                .host(host)
                .event(AlertEvent.SERVER_DOWN)
                .warningLevel(AlertWarnLevel.SERIOUS).
                build();
        String content = JSONUtils.toJsonString(Lists.newArrayList(serverStopAlertContent));

        Alert alert = new Alert();
        alert.setTitle("Fault tolerance warning");
        alert.setAlertStatus(AlertStatus.WAIT_EXECUTION);
        alert.setContent(content);
        alert.setAlertGroupId(alertGroupId);
        alert.setCreateTime(new Date());
        alert.setUpdateTime(new Date());
        // we use this method to avoid insert duplicate alert(issue #5525)
        alertMapper.insertAlertWhenServerCrash(alert);
    }

    /**
     * process time out alert
     *
     * @param processInstance processInstance
     * @param processDefinition processDefinition
     */
    public void sendProcessTimeoutAlert(ProcessInstance processInstance, ProcessDefinition processDefinition) {
        int alertGroupId = processInstance.getWarningGroupId();
        Alert alert = new Alert();
        List<ProcessAlertContent> processAlertContentList = new ArrayList<>(1);
        ProcessAlertContent processAlertContent = ProcessAlertContent.newBuilder()
                .processId(processInstance.getId())
                .processName(processInstance.getName())
                .event(AlertEvent.TIME_OUT)
                .warningLevel(AlertWarnLevel.MIDDLE)
                .build();
        processAlertContentList.add(processAlertContent);
        String content = JSONUtils.toJsonString(processAlertContentList);
        alert.setTitle("Process Timeout Warn");
        saveTaskTimeoutAlert(alert, content, alertGroupId);
    }

    private void saveTaskTimeoutAlert(Alert alert, String content, int alertGroupId) {
        alert.setAlertGroupId(alertGroupId);
        alert.setContent(content);
        alert.setCreateTime(new Date());
        alert.setUpdateTime(new Date());
        alertMapper.insert(alert);
    }

    /**
     * task timeout warn
     *
     * @param alertGroupId alertGroupId
     * @param processInstanceId processInstanceId
     * @param processInstanceName processInstanceName
     * @param taskId taskId
     * @param taskName taskName
     */
    public void sendTaskTimeoutAlert(int alertGroupId, int processInstanceId,
                                     String processInstanceName, int taskId, String taskName) {
        Alert alert = new Alert();
        List<ProcessAlertContent> processAlertContentList = new ArrayList<>(1);
        ProcessAlertContent processAlertContent = ProcessAlertContent.newBuilder()
                .processId(processInstanceId)
                .processName(processInstanceName)
                .taskId(taskId)
                .taskName(taskName)
                .event(AlertEvent.TIME_OUT)
                .warningLevel(AlertWarnLevel.MIDDLE)
                .build();
        processAlertContentList.add(processAlertContent);
        String content = JSONUtils.toJsonString(processAlertContentList);
        alert.setTitle("Task Timeout Warn");
        saveTaskTimeoutAlert(alert, content, alertGroupId);
    }

    /**
     * List alerts that are pending for execution
     */
    public List<Alert> listPendingAlerts() {
        return alertMapper.listAlertByStatus(AlertStatus.WAIT_EXECUTION);
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
}
