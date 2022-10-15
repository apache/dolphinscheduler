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

package org.apache.dolphinscheduler.server.master;

import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.lifecycle.ServerLifeCycleManager;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.task.SubTaskProcessor;
import org.apache.dolphinscheduler.server.master.runner.task.TaskAction;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class SubProcessTaskTest {

    /**
     * TaskNode.runFlag : task can be run normally
     */
    public static final String FLOWNODE_RUN_FLAG_NORMAL = "NORMAL";

    private ProcessService processService;

    private ProcessInstance processInstance;

    private MockedStatic<ServerLifeCycleManager> mockedStaticServerLifeCycleManager;
    private MockedStatic<SpringApplicationContext> mockedStaticSpringApplicationContext;

    @BeforeEach
    public void before() {
        MasterConfig config = new MasterConfig();
        config.setTaskCommitRetryTimes(3);
        config.setTaskCommitInterval(Duration.ofSeconds(1));

        mockedStaticSpringApplicationContext = Mockito.mockStatic(SpringApplicationContext.class);
        Mockito.when(SpringApplicationContext.getBean(MasterConfig.class)).thenReturn(config);

        processService = Mockito.mock(ProcessService.class);
        Mockito.when(SpringApplicationContext.getBean(ProcessService.class)).thenReturn(processService);

        mockedStaticServerLifeCycleManager = Mockito.mockStatic(ServerLifeCycleManager.class);
        Mockito.when(ServerLifeCycleManager.isStopped()).thenReturn(false);

        processInstance = getProcessInstance();
        Mockito.when(processService
                .updateTaskInstance(Mockito.any()))
                .thenReturn(true);

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTimeoutFlag(TimeoutFlag.OPEN);
        taskDefinition.setTimeoutNotifyStrategy(TaskTimeoutStrategy.WARN);
        taskDefinition.setTimeout(0);
    }

    @AfterEach
    public void after() {
        mockedStaticServerLifeCycleManager.close();
        mockedStaticSpringApplicationContext.close();
    }

    private TaskInstance testBasicInit(WorkflowExecutionStatus expectResult) {
        TaskInstance taskInstance = getTaskInstance(getTaskNode(), processInstance);

        ProcessInstance subProcessInstance = getSubProcessInstance(expectResult);
        subProcessInstance.setVarPool(getProperty());
        Mockito.when(processService
                .findSubProcessInstance(processInstance.getId(), taskInstance.getId()))
                .thenReturn(subProcessInstance);

        return taskInstance;
    }

    @Test
    public void testBasicSuccess() {
        testBasicInit(WorkflowExecutionStatus.SUCCESS);
    }

    @Test
    public void testFinish() {
        TaskInstance taskInstance = testBasicInit(WorkflowExecutionStatus.SUCCESS);
        taskInstance.setVarPool(getProperty());
        taskInstance.setTaskParams("{\"processDefinitionCode\":110," +
                "\"dependence\":{},\"localParams\":[{\"prop\":\"key\"," +
                "\"direct\":\"out\",\"type\":\"VARCHAR\",\"value\":\"\"}," +
                "{\"prop\":\"database_name\",\"direct\":\"OUT\"," +
                "\"type\":\"VARCHAR\",\"value\":\"\"}]," +
                "\"conditionResult\":{\"successNode\":[],\"failedNode\":[]}," +
                "\"waitStartTimeout\":{},\"switchResult\":{}}");
        SubTaskProcessor subTaskProcessor = new SubTaskProcessor();
        subTaskProcessor.init(taskInstance, processInstance);
        subTaskProcessor.action(TaskAction.RUN);
        TaskExecutionStatus status = taskInstance.getState();
        Assertions.assertEquals(TaskExecutionStatus.SUCCESS, status);
    }

    private String getProperty() {
        List<Property> varPools = new ArrayList<>();
        Property property = new Property();
        property.setProp("key");
        property.setValue("1");
        property.setDirect(Direct.OUT);
        varPools.add(property);
        return JSONUtils.toJsonString(varPools);
    }

    @Test
    public void testBasicFailure() {
        testBasicInit(WorkflowExecutionStatus.FAILURE);
    }

    private TaskNode getTaskNode() {
        TaskNode taskNode = new TaskNode();
        taskNode.setId("tasks-10");
        taskNode.setName("S");
        taskNode.setCode(1L);
        taskNode.setVersion(1);
        taskNode.setType("SUB_PROCESS");
        taskNode.setRunFlag(FLOWNODE_RUN_FLAG_NORMAL);
        return taskNode;
    }

    private ProcessInstance getProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(100);
        processInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        processInstance.setWarningGroupId(0);
        processInstance.setName("S");
        return processInstance;
    }

    private ProcessInstance getSubProcessInstance(WorkflowExecutionStatus executionStatus) {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(102);
        processInstance.setState(executionStatus);

        return processInstance;
    }

    private TaskInstance getTaskInstance(TaskNode taskNode, ProcessInstance processInstance) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1000);
        taskInstance.setName("S");
        taskInstance.setTaskType("SUB_PROCESS");
        taskInstance.setName(taskNode.getName());
        taskInstance.setTaskCode(taskNode.getCode());
        taskInstance.setTaskDefinitionVersion(taskNode.getVersion());
        taskInstance.setTaskType(taskNode.getType().toUpperCase());
        taskInstance.setProcessInstanceId(processInstance.getId());
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        return taskInstance;
    }
}
