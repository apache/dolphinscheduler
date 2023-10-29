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

import static org.mockito.ArgumentMatchers.any;

import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ListenerEventMapper;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProcessAlertManager Test
 */
@ExtendWith(MockitoExtension.class)
public class ListenerEventAlertManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(ListenerEventAlertManagerTest.class);

    @InjectMocks
    ListenerEventAlertManager listenerEventAlertManager;

    @Mock
    AlertPluginInstanceMapper alertPluginInstanceMapper;

    @Mock
    ListenerEventMapper listenerEventMapper;

    @Test
    public void sendServerDownListenerEventTest() {
        String host = "127.0.0.1";
        String type = "WORKER";
        List<AlertPluginInstance> globalPluginInstanceList = new ArrayList<>();
        AlertPluginInstance instance = new AlertPluginInstance(1, "instanceParams", "instanceName");
        globalPluginInstanceList.add(instance);
        Mockito.when(alertPluginInstanceMapper.queryAllGlobalAlertPluginInstanceList())
                .thenReturn(globalPluginInstanceList);
        Mockito.doNothing().when(listenerEventMapper).insertServerDownEvent(any(), any());
        listenerEventAlertManager.publishServerDownListenerEvent(host, type);
    }

    @Test
    public void sendProcessDefinitionCreatedListenerEvent() {
        User user = Mockito.mock(User.class);
        ProcessDefinition processDefinition = Mockito.mock(ProcessDefinition.class);
        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        List<ProcessTaskRelationLog> processTaskRelationLogs = new ArrayList<>();
        AlertPluginInstance instance = new AlertPluginInstance(1, "instanceParams", "instanceName");
        List<AlertPluginInstance> globalPluginInstanceList = new ArrayList<>();
        globalPluginInstanceList.add(instance);
        Mockito.when(alertPluginInstanceMapper.queryAllGlobalAlertPluginInstanceList())
                .thenReturn(globalPluginInstanceList);
        Mockito.when(listenerEventMapper.insert(any())).thenReturn(1);
        listenerEventAlertManager.publishProcessDefinitionCreatedListenerEvent(user, processDefinition,
                taskDefinitionLogs, processTaskRelationLogs);
    }
    @Test
    public void sendProcessDefinitionUpdatedListenerEvent() {
        User user = new User();
        ProcessDefinition processDefinition = new ProcessDefinition();
        List<TaskDefinitionLog> taskDefinitionLogs = new ArrayList<>();
        List<ProcessTaskRelationLog> processTaskRelationLogs = new ArrayList<>();
        listenerEventAlertManager.publishProcessDefinitionUpdatedListenerEvent(user, processDefinition,
                taskDefinitionLogs, processTaskRelationLogs);
    }

    @Test
    public void sendProcessDefinitionDeletedListenerEvent() {
        User user = new User();
        Project project = new Project();
        ProcessDefinition processDefinition = new ProcessDefinition();
        listenerEventAlertManager.publishProcessDefinitionDeletedListenerEvent(user, project, processDefinition);
    }

    @Test
    public void sendProcessStartListenerEvent() {
        ProcessInstance processInstance = new ProcessInstance();
        ProjectUser projectUser = new ProjectUser();
        listenerEventAlertManager.publishProcessStartListenerEvent(processInstance, projectUser);
    }
    @Test
    public void sendProcessEndListenerEvent() {
        ProcessInstance processInstance = new ProcessInstance();
        ProjectUser projectUser = new ProjectUser();
        listenerEventAlertManager.publishProcessEndListenerEvent(processInstance, projectUser);
    }
    @Test
    public void sendProcessFailListenerEvent() {
        ProcessInstance processInstance = new ProcessInstance();
        ProjectUser projectUser = new ProjectUser();
        listenerEventAlertManager.publishProcessFailListenerEvent(processInstance, projectUser);
    }
    @Test
    public void sendTaskStartListenerEvent() {
        ProcessInstance processInstance = Mockito.mock(ProcessInstance.class);
        TaskInstance taskInstance = Mockito.mock(TaskInstance.class);
        ProjectUser projectUser = Mockito.mock(ProjectUser.class);
        listenerEventAlertManager.publishTaskStartListenerEvent(processInstance, taskInstance, projectUser);
    }
    @Test
    public void sendTaskEndListenerEvent() {
        ProcessInstance processInstance = Mockito.mock(ProcessInstance.class);
        TaskInstance taskInstance = Mockito.mock(TaskInstance.class);
        ProjectUser projectUser = Mockito.mock(ProjectUser.class);
        listenerEventAlertManager.publishTaskEndListenerEvent(processInstance, taskInstance, projectUser);
    }
    @Test
    public void sendTaskFailListenerEvent() {
        ProcessInstance processInstance = Mockito.mock(ProcessInstance.class);
        TaskInstance taskInstance = Mockito.mock(TaskInstance.class);
        ProjectUser projectUser = Mockito.mock(ProjectUser.class);
        listenerEventAlertManager.publishTaskFailListenerEvent(processInstance, taskInstance, projectUser);
    }
}
