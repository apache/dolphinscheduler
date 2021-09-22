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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.DependentRelation;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.model.DependentItem;
import org.apache.dolphinscheduler.common.model.DependentTaskModel;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

/**
 * DependentTaskTest
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class DependentTaskTest {

    /**
     * TaskNode.runFlag : task can be run normally
     */
    public static final String FLOWNODE_RUN_FLAG_NORMAL = "NORMAL";


    public static final Long TASK_CODE = 1111L;
    public static final int TASK_VERSION = 1;

    private ProcessService processService;

    /**
     * the dependent task to be tested
     * ProcessDefinition  id=1
     * Task               id=task-10, name=D
     * ProcessInstance    id=100
     * TaskInstance       id=1000
     * notice: must be initialized by setupTaskInstance() on each test case
     */
    private ProcessInstance processInstance;
    private TaskInstance taskInstance;

    @Before
    public void before() {
        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);

        MasterConfig config = new MasterConfig();
        config.setMasterTaskCommitRetryTimes(3);
        config.setMasterTaskCommitInterval(1000);
        Mockito.when(applicationContext.getBean(MasterConfig.class)).thenReturn(config);

        processService = Mockito.mock(ProcessService.class);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        processInstance = getProcessInstance();

        // for MasterBaseTaskExecThread.call
        // for DependentTaskExecThread.waitTaskQuit
        Mockito.when(processService
                .findProcessInstanceById(100))
                .thenAnswer(i -> processInstance);

        // for MasterBaseTaskExecThread.submit
        Mockito.when(processService
                .submitTask(Mockito.argThat(taskInstance -> taskInstance.getId() == 1000)))
                .thenAnswer(i -> taskInstance);

        // for DependentTaskExecThread.initTaskParameters
        Mockito.when(processService
                .updateTaskInstance(Mockito.any()))
                .thenReturn(true);
        // for DependentTaskExecThread.updateTaskState
        Mockito.when(processService
                .saveTaskInstance(Mockito.any()))
                .thenReturn(true);

        // for DependentTaskExecThread.waitTaskQuit
        Mockito.when(processService
                .findTaskInstanceById(1000))
                .thenAnswer(i -> taskInstance);

        Mockito.when(processService.findTaskDefinition(TASK_CODE, TASK_VERSION))
                .thenReturn(getTaskDefinition());
    }

    private void testBasicInit() {
        TaskNode taskNode = getDependantTaskNode();
        DependentTaskModel dependentTaskModel = new DependentTaskModel();
        dependentTaskModel.setRelation(DependentRelation.AND);
        dependentTaskModel.setDependItemList(Stream.of(
                getDependentItemFromTaskNode(2L, "A", "today", "day")
        ).collect(Collectors.toList()));

        DependentParameters dependentParameters = new DependentParameters();
        dependentParameters.setRelation(DependentRelation.AND);
        dependentParameters.setDependTaskList(Stream.of(dependentTaskModel).collect(Collectors.toList()));

        // dependence: AND(AND(2-A-day-today))
        taskNode.setDependence(JSONUtils.toJsonString(dependentParameters));

        setupTaskInstance(taskNode);
    }

    @Test
    public void testBasicSuccess() {
        testBasicInit();
        ProcessInstance dependentProcessInstance =
                getProcessInstanceForFindLastRunningProcess(200, ExecutionStatus.FAILURE);
        // for DependentExecute.findLastProcessInterval
        Mockito.when(processService
                .findLastRunningProcess(Mockito.eq(2L), Mockito.any(), Mockito.any()))
                .thenReturn(dependentProcessInstance);

        // for DependentExecute.getDependTaskResult
        Mockito.when(processService
                .findValidTaskListByProcessId(200))
                .thenReturn(Stream.of(
                        getTaskInstanceForValidTaskList(2000, ExecutionStatus.SUCCESS, "A", dependentProcessInstance),
                        getTaskInstanceForValidTaskList(2000, ExecutionStatus.FAILURE, "B", dependentProcessInstance)
                ).collect(Collectors.toList()));

    }

    @Test
    public void testBasicFailure() {
        testBasicInit();
        ProcessInstance dependentProcessInstance =
                getProcessInstanceForFindLastRunningProcess(200, ExecutionStatus.SUCCESS);
        // for DependentExecute.findLastProcessInterval
        Mockito.when(processService
                .findLastRunningProcess(Mockito.eq(2L), Mockito.any(), Mockito.any()))
                .thenReturn(dependentProcessInstance);

        // for DependentExecute.getDependTaskResult
        Mockito.when(processService
                .findValidTaskListByProcessId(200))
                .thenReturn(Stream.of(
                        getTaskInstanceForValidTaskList(2000, ExecutionStatus.FAILURE, "A", dependentProcessInstance),
                        getTaskInstanceForValidTaskList(2000, ExecutionStatus.SUCCESS, "B", dependentProcessInstance)
                ).collect(Collectors.toList()));
    }

    @Test
    public void testDependentRelation() {
        DependentTaskModel dependentTaskModel1 = new DependentTaskModel();
        dependentTaskModel1.setRelation(DependentRelation.AND);
        dependentTaskModel1.setDependItemList(Stream.of(
                getDependentItemFromTaskNode(2L, "A", "today", "day"),
                getDependentItemFromTaskNode(3L, "B", "today", "day")
        ).collect(Collectors.toList()));

        DependentTaskModel dependentTaskModel2 = new DependentTaskModel();
        dependentTaskModel2.setRelation(DependentRelation.OR);
        dependentTaskModel2.setDependItemList(Stream.of(
                getDependentItemFromTaskNode(2L, "A", "today", "day"),
                getDependentItemFromTaskNode(3L, "C", "today", "day")
        ).collect(Collectors.toList()));

        /*
         * OR   AND 2-A-day-today 3-B-day-today
         *      OR  2-A-day-today 3-C-day-today
         */
        DependentParameters dependentParameters = new DependentParameters();
        dependentParameters.setRelation(DependentRelation.OR);
        dependentParameters.setDependTaskList(Stream.of(
                dependentTaskModel1,
                dependentTaskModel2
        ).collect(Collectors.toList()));

        TaskNode taskNode = getDependantTaskNode();
        taskNode.setDependence(JSONUtils.toJsonString(dependentParameters));
        setupTaskInstance(taskNode);

        ProcessInstance processInstance200 =
                getProcessInstanceForFindLastRunningProcess(200, ExecutionStatus.FAILURE);
        ProcessInstance processInstance300 =
                getProcessInstanceForFindLastRunningProcess(300, ExecutionStatus.SUCCESS);

        // for DependentExecute.findLastProcessInterval
        Mockito.when(processService
                .findLastRunningProcess(Mockito.eq(2L), Mockito.any(), Mockito.any()))
                .thenReturn(processInstance200);
        Mockito.when(processService
                .findLastRunningProcess(Mockito.eq(3L), Mockito.any(), Mockito.any()))
                .thenReturn(processInstance300);

        // for DependentExecute.getDependTaskResult
        Mockito.when(processService
                .findValidTaskListByProcessId(200))
                .thenReturn(Stream.of(
                        getTaskInstanceForValidTaskList(2000, ExecutionStatus.FAILURE, "A", processInstance200)
                ).collect(Collectors.toList()));
        Mockito.when(processService
                .findValidTaskListByProcessId(300))
                .thenReturn(Stream.of(
                        getTaskInstanceForValidTaskList(3000, ExecutionStatus.SUCCESS, "B", processInstance300),
                        getTaskInstanceForValidTaskList(3001, ExecutionStatus.SUCCESS, "C", processInstance300)
                ).collect(Collectors.toList()));

        //DependentTaskExecThread taskExecThread = new DependentTaskExecThread(taskInstance);
        //taskExecThread.call();
        //Assert.assertEquals(ExecutionStatus.SUCCESS, taskExecThread.getTaskInstance().getState());
    }

    /**
     * test when dependent on ALL tasks in another process
     */
    private void testDependentOnAllInit() {
        TaskNode taskNode = getDependantTaskNode();
        DependentTaskModel dependentTaskModel = new DependentTaskModel();
        dependentTaskModel.setRelation(DependentRelation.AND);
        dependentTaskModel.setDependItemList(Stream.of(
                getDependentItemFromTaskNode(2L, Constants.DEPENDENT_ALL, "today", "day")
        ).collect(Collectors.toList()));

        DependentParameters dependentParameters = new DependentParameters();
        dependentParameters.setRelation(DependentRelation.AND);
        dependentParameters.setDependTaskList(Stream.of(dependentTaskModel).collect(Collectors.toList()));

        // dependence: AND(AND(2:ALL today day))
        taskNode.setDependence(JSONUtils.toJsonString(dependentParameters));

        setupTaskInstance(taskNode);
    }

    @Test
    public void testDependentOnAllSuccess() {
        testDependentOnAllInit();
        // for DependentExecute.findLastProcessInterval
        Mockito.when(processService
                .findLastRunningProcess(Mockito.eq(2L), Mockito.any(), Mockito.any()))
                .thenReturn(getProcessInstanceForFindLastRunningProcess(200, ExecutionStatus.SUCCESS));

        //DependentTaskExecThread taskExecThread = new DependentTaskExecThread(taskInstance);
        //taskExecThread.call();
        //Assert.assertEquals(ExecutionStatus.SUCCESS, taskExecThread.getTaskInstance().getState());
    }

    @Test
    public void testDependentOnAllFailure() {
        testDependentOnAllInit();
        // for DependentExecute.findLastProcessInterval
        Mockito.when(processService
                .findLastRunningProcess(Mockito.eq(2L), Mockito.any(), Mockito.any()))
                .thenReturn(getProcessInstanceForFindLastRunningProcess(200, ExecutionStatus.FAILURE));

        //DependentTaskExecThread dependentTask = new DependentTaskExecThread(taskInstance);
        //dependentTask.call();
        //Assert.assertEquals(ExecutionStatus.FAILURE, dependentTask.getTaskInstance().getState());
    }

    /**
     * test whether waitTaskQuit has been well impl
     */
    @Test
    public void testWaitAndCancel() {
        // for the poor independence of UT, error on other place may causes the condition happens
        if (!Stopper.isRunning()) {
            return;
        }

        TaskNode taskNode = getDependantTaskNode();
        DependentTaskModel dependentTaskModel = new DependentTaskModel();
        dependentTaskModel.setRelation(DependentRelation.AND);
        dependentTaskModel.setDependItemList(Stream.of(
                getDependentItemFromTaskNode(2L, "A", "today", "day")
        ).collect(Collectors.toList()));

        DependentParameters dependentParameters = new DependentParameters();
        dependentParameters.setRelation(DependentRelation.AND);
        dependentParameters.setDependTaskList(Stream.of(dependentTaskModel).collect(Collectors.toList()));

        // dependence: AND(AND(2:A today day))
        taskNode.setDependence(JSONUtils.toJsonString(dependentParameters));

        setupTaskInstance(taskNode);

        ProcessInstance dependentProcessInstance =
                getProcessInstanceForFindLastRunningProcess(200, ExecutionStatus.RUNNING_EXECUTION);
        // for DependentExecute.findLastProcessInterval
        Mockito.when(processService
                .findLastRunningProcess(Mockito.eq(2L), Mockito.any(), Mockito.any()))
                .thenReturn(dependentProcessInstance);

        //DependentTaskExecThread taskExecThread = new DependentTaskExecThread(taskInstance);

        // for DependentExecute.getDependTaskResult
        Mockito.when(processService
                .findValidTaskListByProcessId(200))
                .thenAnswer(i -> {
                    processInstance.setState(ExecutionStatus.READY_STOP);
                    return Stream.of(
                            getTaskInstanceForValidTaskList(2000, ExecutionStatus.RUNNING_EXECUTION, "A", dependentProcessInstance)
                    ).collect(Collectors.toList());
                })
                .thenThrow(new IllegalStateException("have not been stopped as expected"));

        //taskExecThread.call();
        //Assert.assertEquals(ExecutionStatus.KILL, taskExecThread.getTaskInstance().getState());
    }

    private ProcessInstance getProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(100);
        processInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        return processInstance;
    }

    /**
     * task that dependent on others (and to be tested here)
     * notice: should be filled with setDependence() and be passed to setupTaskInstance()
     */
    private TaskNode getDependantTaskNode() {
        TaskNode taskNode = new TaskNode();
        taskNode.setId("tasks-10");
        taskNode.setName("D");
        taskNode.setType(TaskType.DEPENDENT.getDesc());
        taskNode.setRunFlag(FLOWNODE_RUN_FLAG_NORMAL);
        return taskNode;
    }

    private TaskDefinition getTaskDefinition() {
        TaskDefinition taskDefinition = new TaskDefinition();
        taskDefinition.setCode(TASK_CODE);
        taskDefinition.setVersion(TASK_VERSION);
        taskDefinition.setTimeoutFlag(TimeoutFlag.CLOSE);
        taskDefinition.setTimeout(0);
        return taskDefinition;
    }

    private void setupTaskInstance(TaskNode taskNode) {
        taskInstance = new TaskInstance();
        taskInstance.setId(1000);
        taskInstance.setTaskCode(TASK_CODE);
        taskInstance.setTaskDefinitionVersion(TASK_VERSION);
        taskInstance.setProcessInstanceId(processInstance.getId());
        taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
        taskInstance.setTaskType(taskNode.getType().toUpperCase());
        taskInstance.setDependency(JSONUtils.parseObject(taskNode.getDependence(), DependentParameters.class));
        taskInstance.setName(taskNode.getName());
    }

    /**
     * DependentItem defines the condition for the dependent
     */
    private DependentItem getDependentItemFromTaskNode(Long processDefinitionCode, String taskName, String date, String cycle) {
        DependentItem dependentItem = new DependentItem();
        dependentItem.setDefinitionCode(processDefinitionCode);
        dependentItem.setDepTasks(taskName);
        dependentItem.setDateValue(date);
        dependentItem.setCycle(cycle);
        // so far, the following fields have no effect
        dependentItem.setDependResult(DependResult.SUCCESS);
        dependentItem.setStatus(ExecutionStatus.SUCCESS);
        return dependentItem;
    }

    private ProcessInstance getProcessInstanceForFindLastRunningProcess(int processInstanceId, ExecutionStatus state) {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(processInstanceId);
        processInstance.setState(state);
        return processInstance;
    }

    private TaskInstance getTaskInstanceForValidTaskList(
            int taskInstanceId, ExecutionStatus state,
            String taskName, ProcessInstance processInstance
    ) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskType(TaskType.DEPENDENT.getDesc());
        taskInstance.setId(taskInstanceId);
        taskInstance.setName(taskName);
        taskInstance.setProcessInstanceId(processInstance.getId());
        taskInstance.setState(state);
        return taskInstance;
    }
}
