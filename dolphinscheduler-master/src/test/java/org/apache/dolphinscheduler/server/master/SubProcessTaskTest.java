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
import org.apache.dolphinscheduler.dao.AlertDao;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServerLifeCycleManager.class})
public class SubProcessTaskTest {

    /**
     * TaskNode.runFlag : task can be run normally
     */
    public static final String FLOWNODE_RUN_FLAG_NORMAL = "NORMAL";

    private ProcessService processService;

    private ProcessInstance processInstance;

    @Before
    public void before() {
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);

        MasterConfig config = new MasterConfig();
        Mockito.when(applicationContext.getBean(MasterConfig.class)).thenReturn(config);
        config.setTaskCommitRetryTimes(3);
        config.setTaskCommitInterval(Duration.ofSeconds(1));

        PowerMockito.mockStatic(ServerLifeCycleManager.class);
        PowerMockito.when(ServerLifeCycleManager.isStopped()).thenReturn(false);

        processService = Mockito.mock(ProcessService.class);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        AlertDao alertDao = Mockito.mock(AlertDao.class);
        Mockito.when(applicationContext.getBean(AlertDao.class)).thenReturn(alertDao);

        processInstance = getProcessInstance();
        TaskInstance taskInstance = getTaskInstance();

        Mockito.when(processService
                .findProcessInstanceById(processInstance.getId()))
                .thenReturn(processInstance);

        // for SubProcessTaskExecThread.setTaskInstanceState
        Mockito.when(processService
                .updateTaskInstance(Mockito.any()))
                .thenReturn(true);

        // for MasterBaseTaskExecThread.submit
        Mockito.when(processService
                .submitTask(processInstance, taskInstance))
                .thenAnswer(t -> t.getArgument(0));

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTimeoutFlag(TimeoutFlag.OPEN);
        taskDefinition.setTimeoutNotifyStrategy(TaskTimeoutStrategy.WARN);
        taskDefinition.setTimeout(0);
        Mockito.when(processService.findTaskDefinition(1L, 1))
                .thenReturn(taskDefinition);
    }

    private TaskInstance testBasicInit(WorkflowExecutionStatus expectResult) {
        TaskInstance taskInstance = getTaskInstance(getTaskNode(), processInstance);

        ProcessInstance subProcessInstance = getSubProcessInstance(expectResult);
        subProcessInstance.setVarPool(getProperty());
        // for SubProcessTaskExecThread.waitTaskQuit
        Mockito.when(processService
                .findProcessInstanceById(subProcessInstance.getId()))
                .thenReturn(subProcessInstance);
        Mockito.when(processService
                .findSubProcessInstance(processInstance.getId(), taskInstance.getId()))
                .thenReturn(subProcessInstance);

        return taskInstance;
    }

    @Test
    public void testBasicSuccess() {
        TaskInstance taskInstance = testBasicInit(WorkflowExecutionStatus.SUCCESS);
        // SubProcessTaskExecThread taskExecThread = new SubProcessTaskExecThread(taskInstance);
        // taskExecThread.call();
        // Assert.assertEquals(ExecutionStatus.SUCCESS, taskExecThread.getTaskInstance().getState());
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
        Assert.assertEquals(TaskExecutionStatus.SUCCESS, status);
    }

    @Test
    public void testStop() {
        TaskInstance taskInstance = testBasicInit(WorkflowExecutionStatus.STOP);
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
        Assert.assertEquals(TaskExecutionStatus.KILL, status);
    }

    @Test
    public void testFail() {
        TaskInstance taskInstance = testBasicInit(WorkflowExecutionStatus.FAILURE);
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
        Assert.assertEquals(TaskExecutionStatus.FAILURE, status);
    }

    @Test
    public void testPAUSE() {
        TaskInstance taskInstance = testBasicInit(WorkflowExecutionStatus.PAUSE);
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
        Assert.assertEquals(TaskExecutionStatus.PAUSE, status);
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
        TaskInstance taskInstance = testBasicInit(WorkflowExecutionStatus.FAILURE);
        // SubProcessTaskExecThread taskExecThread = new SubProcessTaskExecThread(taskInstance);
        // taskExecThread.call();
        // Assert.assertEquals(ExecutionStatus.FAILURE, taskExecThread.getTaskInstance().getState());
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

    private TaskInstance getTaskInstance() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1000);
        return taskInstance;
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
