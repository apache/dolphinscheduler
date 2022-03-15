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

import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Stopper.class })
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
        config.setTaskCommitInterval(1000);

        PowerMockito.mockStatic(Stopper.class);
        PowerMockito.when(Stopper.isRunning()).thenReturn(true);

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
    public void testBasicSuccess() {
        TaskInstance taskInstance = testBasicInit(ExecutionStatus.SUCCESS);
        //SubProcessTaskExecThread taskExecThread = new SubProcessTaskExecThread(taskInstance);
        //taskExecThread.call();
        //Assert.assertEquals(ExecutionStatus.SUCCESS, taskExecThread.getTaskInstance().getState());
    }

    @Test
    public void testBasicFailure() {
        TaskInstance taskInstance = testBasicInit(ExecutionStatus.FAILURE);
        //SubProcessTaskExecThread taskExecThread = new SubProcessTaskExecThread(taskInstance);
        //taskExecThread.call();
        //Assert.assertEquals(ExecutionStatus.FAILURE, taskExecThread.getTaskInstance().getState());
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
        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        processInstance.setWarningGroupId(0);
        processInstance.setName("S");
        return processInstance;
    }

    private TaskInstance getTaskInstance() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1000);
        return taskInstance;
    }

    private ProcessInstance getSubProcessInstance(ExecutionStatus executionStatus) {
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
        taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
        return taskInstance;
    }
}
