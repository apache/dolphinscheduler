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

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.SubProcessTaskExecThread;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
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

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        Stopper.class,
})
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
        config.setMasterTaskCommitRetryTimes(3);
        config.setMasterTaskCommitInterval(1000);

        PowerMockito.mockStatic(Stopper.class);
        PowerMockito.when(Stopper.isRunning()).thenReturn(true);

        processService = Mockito.mock(ProcessService.class);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        processInstance = getProcessInstance();
        Mockito.when(processService
                .findProcessInstanceById(processInstance.getId()))
                .thenReturn(processInstance);

        // for SubProcessTaskExecThread.setTaskInstanceState
        Mockito.when(processService
                .updateTaskInstance(Mockito.any()))
                .thenReturn(true);

        // for MasterBaseTaskExecThread.submit
        Mockito.when(processService
                .submitTask(Mockito.any()))
                .thenAnswer(t -> t.getArgument(0));
    }

    private TaskInstance testBasicInit(ExecutionStatus expectResult) {
        TaskInstance taskInstance = getTaskInstance(getTaskNode(), processInstance);

        ProcessInstance subProcessInstance = getSubProcessInstance(expectResult);
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
    public void testBasicSuccess() throws Exception {
        TaskInstance taskInstance = testBasicInit(ExecutionStatus.SUCCESS);
        SubProcessTaskExecThread taskExecThread = new SubProcessTaskExecThread(taskInstance);
        taskExecThread.call();
        Assert.assertEquals(ExecutionStatus.SUCCESS, taskExecThread.getTaskInstance().getState());
    }

    @Test
    public void testBasicFailure() throws Exception {
        TaskInstance taskInstance = testBasicInit(ExecutionStatus.FAILURE);
        SubProcessTaskExecThread taskExecThread = new SubProcessTaskExecThread(taskInstance);
        taskExecThread.call();
        Assert.assertEquals(ExecutionStatus.FAILURE, taskExecThread.getTaskInstance().getState());
    }

    private TaskNode getTaskNode() {
        TaskNode taskNode = new TaskNode();
        taskNode.setId("tasks-10");
        taskNode.setName("S");
        taskNode.setType(TaskType.SUB_PROCESS.toString());
        taskNode.setRunFlag(FLOWNODE_RUN_FLAG_NORMAL);
        return taskNode;
    }

    private ProcessInstance getProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(100);
        processInstance.setProcessDefinitionId(1);
        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);

        return processInstance;
    }

    private ProcessInstance getSubProcessInstance(ExecutionStatus executionStatus) {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(102);
        processInstance.setProcessDefinitionId(2);
        processInstance.setState(executionStatus);

        return processInstance;
    }

    private TaskInstance getTaskInstance(TaskNode taskNode, ProcessInstance processInstance) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1000);
        taskInstance.setTaskJson(JSONUtils.toJsonString(taskNode));
        taskInstance.setName(taskNode.getName());
        taskInstance.setTaskType(taskNode.getType());
        taskInstance.setProcessInstanceId(processInstance.getId());
        taskInstance.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
        return taskInstance;
    }
}
