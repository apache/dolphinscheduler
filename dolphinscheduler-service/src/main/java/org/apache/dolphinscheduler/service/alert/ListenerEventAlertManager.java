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

import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.common.enums.ListenerEventType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.event.AbstractListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ProcessDefinitionCreatedListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ProcessDefinitionDeletedListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ProcessDefinitionUpdatedListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ProcessEndListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ProcessFailListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ProcessStartListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.ServerDownListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.TaskEndListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.TaskFailListenerEvent;
import org.apache.dolphinscheduler.dao.entity.event.TaskStartListenerEvent;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ListenerEventMapper;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ListenerEventAlertManager {

    @Value("${alert.alarm-suppression.crash:60}")
    private int crashAlarmSuppression;

    @Autowired
    private ListenerEventMapper listenerEventMapper;

    @Autowired
    private AlertPluginInstanceMapper alertPluginInstanceMapper;

    public void publishServerDownListenerEvent(String host, String type) {
        ServerDownListenerEvent event = new ServerDownListenerEvent();
        event.setEventTime(new Date());
        event.setHost(host);
        event.setType(type);
        this.saveEvent(event);
    }

    public void publishProcessDefinitionCreatedListenerEvent(User user,
                                                             ProcessDefinition processDefinition,
                                                             List<TaskDefinitionLog> taskDefinitionLogs,
                                                             List<ProcessTaskRelationLog> processTaskRelationLogs) {
        ProcessDefinitionCreatedListenerEvent event = new ProcessDefinitionCreatedListenerEvent(processDefinition);
        event.setUserName(user.getUserName());
        event.setModifyBy(user.getUserName());
        event.setTaskDefinitionLogs(taskDefinitionLogs);
        event.setTaskRelationList(processTaskRelationLogs);
        this.saveEvent(event);
    }

    public void publishProcessDefinitionUpdatedListenerEvent(User user, ProcessDefinition processDefinition,
                                                             List<TaskDefinitionLog> taskDefinitionLogs,
                                                             List<ProcessTaskRelationLog> processTaskRelationLogs) {
        ProcessDefinitionUpdatedListenerEvent event = new ProcessDefinitionUpdatedListenerEvent(processDefinition);
        event.setTaskDefinitionLogs(taskDefinitionLogs);
        event.setTaskRelationList(processTaskRelationLogs);
        event.setUserName(user.getUserName());
        event.setModifyBy(user.getUserName());
        this.saveEvent(event);
    }

    public void publishProcessDefinitionDeletedListenerEvent(User user, Project project,
                                                             ProcessDefinition processDefinition) {
        ProcessDefinitionDeletedListenerEvent event = new ProcessDefinitionDeletedListenerEvent();
        event.setProjectId(project.getId());
        event.setProjectCode(project.getCode());
        event.setProjectName(project.getName());
        event.setOwner(processDefinition.getUserName());
        event.setId(processDefinition.getId());
        event.setCode(processDefinition.getCode());
        event.setName(processDefinition.getName());
        event.setEventTime(new Date());
        event.setUserId(user.getId());
        event.setModifiedBy(user.getUserName());
        this.saveEvent(event);
    }

    public void publishProcessStartListenerEvent(ProcessInstance processInstance, ProjectUser projectUser) {
        ProcessStartListenerEvent event = new ProcessStartListenerEvent();
        event.setProjectCode(projectUser.getProjectCode());
        event.setProjectName(projectUser.getProjectName());
        event.setOwner(projectUser.getUserName());
        event.setProcessId(processInstance.getId());
        event.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        event.setProcessName(processInstance.getName());
        event.setProcessType(processInstance.getCommandType());
        event.setProcessState(processInstance.getState());
        event.setRunTimes(processInstance.getRunTimes());
        event.setRecovery(processInstance.getRecovery());
        event.setProcessStartTime(processInstance.getStartTime());
        this.saveEvent(event);
    }

    public void publishProcessEndListenerEvent(ProcessInstance processInstance, ProjectUser projectUser) {
        ProcessEndListenerEvent event = new ProcessEndListenerEvent();
        event.setProjectCode(projectUser.getProjectCode());
        event.setProjectName(projectUser.getProjectName());
        event.setOwner(projectUser.getUserName());
        event.setProcessId(processInstance.getId());
        event.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        event.setProcessName(processInstance.getName());
        event.setProcessType(processInstance.getCommandType());
        event.setProcessState(processInstance.getState());
        event.setRecovery(processInstance.getRecovery());
        event.setRunTimes(processInstance.getRunTimes());
        event.setProcessStartTime(processInstance.getStartTime());
        event.setProcessEndTime(processInstance.getEndTime());
        event.setProcessHost(processInstance.getHost());
        this.saveEvent(event);
    }

    public void publishProcessFailListenerEvent(ProcessInstance processInstance,
                                                ProjectUser projectUser) {
        ProcessFailListenerEvent event = new ProcessFailListenerEvent();
        event.setProjectCode(projectUser.getProjectCode());
        event.setProjectName(projectUser.getProjectName());
        event.setOwner(projectUser.getUserName());
        event.setProcessId(processInstance.getId());
        event.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        event.setProcessName(processInstance.getName());
        event.setProcessType(processInstance.getCommandType());
        event.setProcessState(processInstance.getState());
        event.setRecovery(processInstance.getRecovery());
        event.setRunTimes(processInstance.getRunTimes());
        event.setProcessStartTime(processInstance.getStartTime());
        event.setProcessEndTime(processInstance.getEndTime());
        event.setProcessHost(processInstance.getHost());
        this.saveEvent(event);
    }

    public void publishTaskStartListenerEvent(ProcessInstance processInstance,
                                              TaskInstance taskInstance,
                                              ProjectUser projectUser) {
        TaskStartListenerEvent event = new TaskStartListenerEvent();
        event.setProjectCode(projectUser.getProjectCode());
        event.setProjectName(projectUser.getProjectName());
        event.setOwner(projectUser.getUserName());
        event.setProcessId(processInstance.getId());
        event.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        event.setProcessName(processInstance.getName());
        event.setTaskCode(taskInstance.getTaskCode());
        event.setTaskName(taskInstance.getName());
        event.setTaskType(taskInstance.getTaskType());
        event.setTaskState(taskInstance.getState());
        event.setTaskStartTime(taskInstance.getStartTime());
        event.setTaskEndTime(taskInstance.getEndTime());
        event.setTaskHost(taskInstance.getHost());
        event.setLogPath(taskInstance.getLogPath());
        this.saveEvent(event);
    }

    public void publishTaskEndListenerEvent(ProcessInstance processInstance,
                                            TaskInstance taskInstance,
                                            ProjectUser projectUser) {
        TaskEndListenerEvent event = new TaskEndListenerEvent();
        event.setProjectCode(projectUser.getProjectCode());
        event.setProjectName(projectUser.getProjectName());
        event.setOwner(projectUser.getUserName());
        event.setProcessId(processInstance.getId());
        event.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        event.setProcessName(processInstance.getName());
        event.setTaskCode(taskInstance.getTaskCode());
        event.setTaskName(taskInstance.getName());
        event.setTaskType(taskInstance.getTaskType());
        event.setTaskState(taskInstance.getState());
        event.setTaskStartTime(taskInstance.getStartTime());
        event.setTaskEndTime(taskInstance.getEndTime());
        event.setTaskHost(taskInstance.getHost());
        event.setLogPath(taskInstance.getLogPath());
        this.saveEvent(event);
    }

    public void publishTaskFailListenerEvent(ProcessInstance processInstance,
                                             TaskInstance taskInstance,
                                             ProjectUser projectUser) {
        TaskFailListenerEvent event = new TaskFailListenerEvent();
        event.setProjectCode(projectUser.getProjectCode());
        event.setProjectName(projectUser.getProjectName());
        event.setOwner(projectUser.getUserName());
        event.setProcessId(processInstance.getId());
        event.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        event.setProcessName(processInstance.getName());
        event.setTaskCode(taskInstance.getTaskCode());
        event.setTaskName(taskInstance.getName());
        event.setTaskType(taskInstance.getTaskType());
        event.setTaskState(taskInstance.getState());
        event.setTaskStartTime(taskInstance.getStartTime());
        event.setTaskEndTime(taskInstance.getEndTime());
        event.setTaskHost(taskInstance.getHost());
        event.setLogPath(taskInstance.getLogPath());
        this.saveEvent(event);
    }

    private void saveEvent(AbstractListenerEvent event) {
        if (!needSendGlobalListenerEvent()) {
            return;
        }
        ListenerEvent listenerEvent = new ListenerEvent();
        String content = JSONUtils.toJsonString(event);
        listenerEvent.setContent(content);
        listenerEvent.setPostStatus(AlertStatus.WAIT_EXECUTION);
        listenerEvent.setSign(generateSign(content));
        listenerEvent.setCreateTime(new Date());
        listenerEvent.setUpdateTime(new Date());
        listenerEvent.setEventType(event.getEventType());
        if (event.getEventType() == ListenerEventType.SERVER_DOWN) {
            saveServerDownEvent(listenerEvent);
        } else {
            saveNormalEvent(listenerEvent);
        }
    }

    private void saveNormalEvent(ListenerEvent listenerEvent) {
        int insert = listenerEventMapper.insert(listenerEvent);
        if (insert < 1) {
            log.error("insert listener event failed: {}", listenerEvent);
        }
    }

    private void saveServerDownEvent(ListenerEvent listenerEvent) {
        Date crashAlarmSuppressionStartTime = Date.from(
                LocalDateTime.now().plusMinutes(-crashAlarmSuppression).atZone(ZoneId.systemDefault()).toInstant());
        listenerEventMapper.insertServerDownEvent(listenerEvent, crashAlarmSuppressionStartTime);
    }

    private String generateSign(String content) {
        return DigestUtils.sha256Hex(content).toLowerCase();
    }

    private boolean needSendGlobalListenerEvent() {
        List<AlertPluginInstance> globalPluginInstanceList =
                alertPluginInstanceMapper.queryAllGlobalAlertPluginInstanceList();
        return CollectionUtils.isNotEmpty(globalPluginInstanceList);
    }
}
