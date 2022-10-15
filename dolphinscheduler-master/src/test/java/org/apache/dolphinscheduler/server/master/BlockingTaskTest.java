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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_BLOCKING;

import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependentRelation;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.parameters.BlockingParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.task.BlockingTaskProcessor;
import org.apache.dolphinscheduler.server.master.runner.task.TaskAction;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class BlockingTaskTest {

    /**
     * TaskNode.runFlag : task can be run normally
     */
    public static final String FLOW_NODE_RUN_FLAG_NORMAL = "NORMAL";

    private ProcessService processService;

    private ProcessInstance processInstance;

    private MasterConfig config;

    private MockedStatic<SpringApplicationContext> mockedStaticSpringApplicationContext;

    @BeforeEach
    public void before() {
        // mock master
        config = new MasterConfig();
        config.setTaskCommitRetryTimes(3);
        config.setTaskCommitInterval(Duration.ofSeconds(1));

        mockedStaticSpringApplicationContext = Mockito.mockStatic(SpringApplicationContext.class);
        Mockito.when(SpringApplicationContext.getBean(MasterConfig.class)).thenReturn(config);

        // mock process service
        processService = Mockito.mock(ProcessService.class);

        Mockito.when(SpringApplicationContext.getBean(ProcessService.class)).thenReturn(processService);

        // mock process instance
        processInstance = getProcessInstance();
        Mockito.when(processService
                .findProcessInstanceById(processInstance.getId()))
                .thenReturn(processInstance);

        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setTimeoutFlag(TimeoutFlag.OPEN);
        taskDefinition.setTimeoutNotifyStrategy(TaskTimeoutStrategy.WARN);
        taskDefinition.setTimeout(0);
        Mockito.when(processService.findTaskDefinition(1L, 1))
                .thenReturn(taskDefinition);
    }

    @AfterEach
    public void after() {
        mockedStaticSpringApplicationContext.close();
    }

    private ProcessInstance getProcessInstance() {
        // mock process instance
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1000);
        processInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        processInstance.setProcessDefinitionCode(1L);

        return processInstance;
    }

    private TaskInstance getTaskInstance(TaskNode taskNode, ProcessInstance processInstance) {
        // wrap taskNode
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(100);
        taskInstance.setName(taskNode.getName());
        taskInstance.setTaskType(taskNode.getType().toUpperCase());
        taskInstance.setTaskCode(taskNode.getCode());
        taskInstance.setTaskDefinitionVersion(taskNode.getVersion());
        taskInstance.setProcessInstanceId(processInstance.getId());
        taskInstance.setTaskParams(taskNode.getTaskParams());
        taskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setFirstSubmitTime(new Date());
        Mockito.when(processService
                .submitTaskWithRetry(Mockito.any(ProcessInstance.class), Mockito.any(TaskInstance.class),
                        Mockito.any(Integer.class), Mockito.any(Long.class)))
                .thenReturn(taskInstance);
        return taskInstance;
    }

    private TaskNode getTaskNode(String blockingCondition) {
        // mock task nodes
        // 1----\
        // 2-----4(Blocking Node)
        // 3----/
        // blocking logic: 1-->SUCCESS 2-->SUCCESS 3-->SUCCESS
        TaskNode taskNode = new TaskNode();
        taskNode.setId("tasks-1000");
        taskNode.setName("4");
        taskNode.setCode(1L);
        taskNode.setVersion(1);
        taskNode.setType(TASK_TYPE_BLOCKING);
        taskNode.setRunFlag(FLOW_NODE_RUN_FLAG_NORMAL);

        DependentItem dependentItemA = new DependentItem();
        dependentItemA.setDepTaskCode(1L);
        dependentItemA.setStatus(TaskExecutionStatus.SUCCESS);

        DependentItem dependentItemB = new DependentItem();
        dependentItemB.setDepTaskCode(2L);
        dependentItemB.setStatus(TaskExecutionStatus.SUCCESS);

        DependentItem dependentItemC = new DependentItem();
        dependentItemC.setDepTaskCode(3L);
        dependentItemC.setStatus(TaskExecutionStatus.SUCCESS);

        // build relation
        DependentTaskModel dependentTaskModel = new DependentTaskModel();
        dependentTaskModel.setDependItemList(Stream.of(dependentItemA, dependentItemB, dependentItemC)
                .collect(Collectors.toList()));
        dependentTaskModel.setRelation(DependentRelation.AND);

        DependentParameters dependentParameters = new DependentParameters();
        dependentParameters.setDependTaskList(Stream.of(dependentTaskModel).collect(Collectors.toList()));
        dependentParameters.setRelation(DependentRelation.AND);

        taskNode.setDependence(JSONUtils.toJsonString(dependentParameters));

        // set blocking node params
        BlockingParameters blockingParameters = new BlockingParameters();
        blockingParameters.setAlertWhenBlocking(false);
        blockingParameters.setBlockingCondition(blockingCondition);

        taskNode.setParams(JSONUtils.toJsonString(blockingParameters));

        return taskNode;
    }

    private TaskInstance testBasicInit(String blockingCondition, TaskExecutionStatus... expectResults) {

        TaskInstance taskInstance = getTaskInstance(getTaskNode(blockingCondition), processInstance);

        Mockito.when(processService
                .submitTask(processInstance, taskInstance))
                .thenReturn(taskInstance);

        Mockito.when(processService
                .findTaskInstanceById(taskInstance.getId()))
                .thenReturn(taskInstance);

        // for BlockingTaskExecThread.initTaskParameters
        Mockito.when(processService
                .saveTaskInstance(taskInstance))
                .thenReturn(true);

        // for BlockingTaskExecThread.updateTaskState
        Mockito.when(processService
                .updateTaskInstance(taskInstance))
                .thenReturn(true);

        // for BlockingTaskExecThread.waitTaskQuit
        List<TaskInstance> conditions = getTaskInstanceForValidTaskList(expectResults);
        Mockito.when(
                processService.findValidTaskListByProcessId(processInstance.getId(), processInstance.getTestFlag()))
                .thenReturn(conditions);
        return taskInstance;
    }

    /**
     * mock task instance and its execution result in front of blocking node
     */
    private List<TaskInstance> getTaskInstanceForValidTaskList(TaskExecutionStatus... status) {
        List<TaskInstance> taskInstanceList = new ArrayList<>();
        for (int i = 1; i <= status.length; i++) {
            TaskInstance taskInstance = new TaskInstance();
            taskInstance.setId(i);
            taskInstance.setName(String.valueOf(i));
            taskInstance.setState(status[i - 1]);
            taskInstanceList.add(taskInstance);
        }
        return taskInstanceList;
    }

    @Test
    public void testBlockingTaskSubmit() {
        TaskInstance taskInstance = testBasicInit("BlockingOnFailed",
                TaskExecutionStatus.SUCCESS, TaskExecutionStatus.FAILURE, TaskExecutionStatus.SUCCESS);
        BlockingTaskProcessor blockingTaskProcessor = new BlockingTaskProcessor();
        blockingTaskProcessor.init(taskInstance, processInstance);
        boolean res = blockingTaskProcessor.action(TaskAction.SUBMIT);
        Assertions.assertTrue(res);
    }

    @Test
    public void testPauseTask() {
        TaskInstance taskInstance = testBasicInit("BlockingOnFailed",
                TaskExecutionStatus.SUCCESS, TaskExecutionStatus.FAILURE, TaskExecutionStatus.SUCCESS);
        BlockingTaskProcessor blockingTaskProcessor = new BlockingTaskProcessor();
        blockingTaskProcessor.init(taskInstance, processInstance);
        blockingTaskProcessor.action(TaskAction.SUBMIT);
        blockingTaskProcessor.action(TaskAction.PAUSE);
        TaskExecutionStatus status = taskInstance.getState();
        Assertions.assertEquals(TaskExecutionStatus.PAUSE, status);
    }

    @Test
    public void testBlocking() {
        TaskInstance taskInstance = testBasicInit("BlockingOnFailed",
                TaskExecutionStatus.SUCCESS, TaskExecutionStatus.FAILURE, TaskExecutionStatus.SUCCESS);
        BlockingTaskProcessor blockingTaskProcessor = new BlockingTaskProcessor();
        blockingTaskProcessor.init(taskInstance, processInstance);
        blockingTaskProcessor.action(TaskAction.SUBMIT);
        blockingTaskProcessor.action(TaskAction.RUN);
        WorkflowExecutionStatus status = processInstance.getState();
        Assertions.assertEquals(WorkflowExecutionStatus.READY_BLOCK, status);
    }

    @Test
    public void testNoneBlocking() {
        TaskInstance taskInstance = testBasicInit("BlockingOnSuccess",
                TaskExecutionStatus.SUCCESS, TaskExecutionStatus.SUCCESS, TaskExecutionStatus.SUCCESS);
        BlockingTaskProcessor blockingTaskProcessor = new BlockingTaskProcessor();
        blockingTaskProcessor.init(taskInstance, processInstance);
        blockingTaskProcessor.action(TaskAction.SUBMIT);
        blockingTaskProcessor.action(TaskAction.RUN);
        WorkflowExecutionStatus status = processInstance.getState();
        Assertions.assertEquals(WorkflowExecutionStatus.RUNNING_EXECUTION, status);
    }
}
