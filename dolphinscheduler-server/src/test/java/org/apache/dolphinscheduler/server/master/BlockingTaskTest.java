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

import org.apache.dolphinscheduler.common.enums.DependentRelation;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.model.DependentItem;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.blocking.BlockingParameters;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BlockingTaskTest {
//    /**
//     * TaskNode.runFlag : task can be run normally
//     */
//    public static final String FLOW_NODE_RUN_FLAG_NORMAL = "NORMAL";
//
//    private ProcessService processService;
//
//    private ProcessInstance processInstance;
//
//    private MasterConfig config;
//
//    @Before
//    public void before() {
//        // init spring context
//        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
//        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
//        springApplicationContext.setApplicationContext(applicationContext);
//
//        // mock master
//        config = new MasterConfig();
//        Mockito.when(applicationContext.getBean(MasterConfig.class)).thenReturn(config);
//        config.setMasterTaskCommitRetryTimes(3);
//        config.setMasterTaskCommitInterval(1000);
//
//        // mock process service
//        processService = Mockito.mock(ProcessService.class);
//        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);
//
//        // mock process instance
//        processInstance = getProcessInstance();
//        Mockito.when(processService
//                .findProcessInstanceById(processInstance.getId()))
//                .thenReturn(processInstance);
//
//        TaskDefinition taskDefinition = new TaskDefinition();
//        taskDefinition.setTimeoutFlag(TimeoutFlag.OPEN);
//        taskDefinition.setTimeoutNotifyStrategy(TaskTimeoutStrategy.WARN);
//        taskDefinition.setTimeout(0);
//        Mockito.when(processService.findTaskDefinition(1L,1))
//                .thenReturn(taskDefinition);
//    }
//
//    private ProcessInstance getProcessInstance() {
//        // mock process instance
//        ProcessInstance processInstance = new ProcessInstance();
//        processInstance.setId(1000);
//        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
//
//        return processInstance;
//    }
//
//    private TaskInstance getTaskInstance(TaskNode taskNode, ProcessInstance processInstance) {
//        // wrap taskNode
//        TaskInstance taskInstance = new TaskInstance();
//        taskInstance.setId(100);
//        taskInstance.setName(taskNode.getName());
//        taskInstance.setTaskType(taskNode.getType().toUpperCase());
//        taskInstance.setTaskCode(taskNode.getCode());
//        taskInstance.setTaskDefinitionVersion(taskNode.getVersion());
//        taskInstance.setProcessInstanceId(processInstance.getId());
//        taskInstance.setTaskParams(taskNode.getTaskParams());
//        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
//        return taskInstance;
//    }
//
//    private TaskNode getTaskNode(String blockingCondition) {
//        // mock task nodes
//        // 1----\
//        // 2-----4(Blocking Node)
//        // 3----/
//        // blocking logic: 1-->SUCCESS 2-->SUCCESS 3-->SUCCESS
//        TaskNode taskNode = new TaskNode();
//        taskNode.setId("tasks-1000");
//        taskNode.setName("4");
//        taskNode.setCode(1L);
//        taskNode.setVersion(1);
//        taskNode.setType(TaskType.BLOCKING.getDesc());
//        taskNode.setRunFlag(FLOW_NODE_RUN_FLAG_NORMAL);
//
//        DependentItem dependentItemA = new DependentItem();
//        dependentItemA.setDepTasks("1");
//        dependentItemA.setStatus(ExecutionStatus.SUCCESS);
//
//        DependentItem dependentItemB = new DependentItem();
//        dependentItemB.setDepTasks("2");
//        dependentItemB.setStatus(ExecutionStatus.SUCCESS);
//
//        DependentItem dependentItemC = new DependentItem();
//        dependentItemC.setDepTasks("3");
//        dependentItemC.setStatus(ExecutionStatus.SUCCESS);
//
//        // build relation
//        DependentTaskModel dependentTaskModel = new DependentTaskModel();
//        // Java 8 NEW FEATURE stream.of(T..)
//        dependentTaskModel.setDependItemList(Stream.of(dependentItemA, dependentItemB, dependentItemC)
//                .collect(Collectors.toList()));
//        dependentTaskModel.setRelation(DependentRelation.AND);
//
//        DependentParameters dependentParameters = new DependentParameters();
//        dependentParameters.setDependTaskList(Stream.of(dependentTaskModel).collect(Collectors.toList()));
//        dependentParameters.setRelation(DependentRelation.AND);
//
//        taskNode.setDependence(JSONUtils.toJsonString(dependentParameters));
//
//        // set blocking node params
//        BlockingParameters blockingParameters = new BlockingParameters();
//        blockingParameters.setAlertWhenBlocking(false);
//        blockingParameters.setBlockingCondition(blockingCondition);
//
//        taskNode.setParams(JSONUtils.toJsonString(blockingParameters));
//
//        return taskNode;
//    }
//
//    private TaskInstance testBasicInit(String blockingCondition,ExecutionStatus... expectResults) {
//
//        TaskInstance taskInstance = getTaskInstance(getTaskNode(blockingCondition),processInstance);
//
//        Mockito.when(processService.submitTask(taskInstance,
//                config.getMasterTaskCommitRetryTimes(),
//                config.getMasterTaskCommitInterval()))
//                .thenReturn(taskInstance);
//
//        Mockito.when(processService
//                .findTaskInstanceById(taskInstance.getId()))
//                .thenReturn(taskInstance);
//
//        // for BlockingTaskExecThread.initTaskParameters
//        Mockito.when(processService
//                .saveTaskInstance(taskInstance))
//                .thenReturn(true);
//
//        // for BlockingTaskExecThread.updateTaskState
//        Mockito.when(processService
//                .updateTaskInstance(taskInstance))
//                .thenReturn(true);
//
//        // for BlockingTaskExecThread.waitTaskQuit
//        List<TaskInstance> conditions = getTaskInstanceForValidTaskList(expectResults);
//        Mockito.when(processService.
//                findValidTaskListByProcessId(processInstance.getId()))
//                .thenReturn(conditions);
//        return taskInstance;
//    }
//
//    /**
//     * mock task instance and its execution result in front of blocking node
//     */
//    private List<TaskInstance> getTaskInstanceForValidTaskList(ExecutionStatus... status) {
//        List<TaskInstance> taskInstanceList = new ArrayList<>();
//        for (int i = 1; i <= status.length; i++) {
//            TaskInstance taskInstance = new TaskInstance();
//            taskInstance.setId(i);
//            taskInstance.setName(String.valueOf(i));
//            taskInstance.setState(status[i - 1]);
//            taskInstanceList.add(taskInstance);
//        }
//        return taskInstanceList;
//    }
//
//    @Test
//    public void testBlockingTaskSubmit() {
//        TaskInstance taskInstance = testBasicInit("BlockingOnFailed",
//                ExecutionStatus.SUCCESS, ExecutionStatus.FAILURE, ExecutionStatus.SUCCESS);
//        BlockingTaskProcessor blockingTaskProcessor = new BlockingTaskProcessor();
//        boolean res = blockingTaskProcessor.submit(taskInstance,
//                processInstance,config.getMasterTaskCommitRetryTimes(),config.getMasterTaskCommitInterval());
//        Assert.assertEquals(true,res);
//    }
//
//    @Test
//    public void testGetTaskStatus() {
//        TaskInstance taskInstance = testBasicInit("BlockingOnFailed",
//                ExecutionStatus.SUCCESS, ExecutionStatus.FAILURE, ExecutionStatus.SUCCESS);
//        BlockingTaskProcessor blockingTaskProcessor = new BlockingTaskProcessor();
//        blockingTaskProcessor.submit(taskInstance,
//                processInstance,config.getMasterTaskCommitRetryTimes(),config.getMasterTaskCommitInterval());
//        ExecutionStatus status = blockingTaskProcessor.taskState();
//        Assert.assertEquals(status,ExecutionStatus.RUNNING_EXECUTION);
//    }
//
//    /**
//     * Blocking node may be failed when DB or Master Server crashed.
//     * The former, execution result will not be written into DB.
//     * The latter, fault-tolerant will take over.
//     */
//
//    @Test
//    public void testBlockingLogicFailed() {
//        TaskInstance taskInstance = testBasicInit("BlockingOnFailed",
//                ExecutionStatus.SUCCESS, ExecutionStatus.FAILURE, ExecutionStatus.SUCCESS);
//        ITaskProcessor taskProcessor = TaskProcessorFactory.getTaskProcessor(taskInstance.getTaskType());
//        taskProcessor.submit(taskInstance,
//                processInstance, config.getMasterTaskCommitRetryTimes(), config.getMasterTaskCommitInterval());
//        taskProcessor.run();
//        Boolean res = (Boolean)taskProcessor.taskExtraInfo();
//        Assert.assertEquals(true, res);
//    }
//
//    @Test
//    public void testBlockingLogicSuccess() {
//        TaskInstance taskInstance = testBasicInit("BlockingOnSuccess",
//                ExecutionStatus.SUCCESS, ExecutionStatus.SUCCESS, ExecutionStatus.SUCCESS);
//        ITaskProcessor taskProcessor = TaskProcessorFactory.getTaskProcessor(taskInstance.getTaskType());
//        taskProcessor.submit(taskInstance,
//                processInstance, config.getMasterTaskCommitRetryTimes(), config.getMasterTaskCommitInterval());
//        taskProcessor.run();
//        Boolean res = (Boolean)taskProcessor.taskExtraInfo();
//        Assert.assertEquals(true, res);
//    }
}
