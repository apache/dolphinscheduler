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

package org.apache.dolphinscheduler.server.master.runner;

import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_RECOVERY_START_NODE_STRING;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_START_NODES;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.graph.IWorkflowGraph;
import org.apache.dolphinscheduler.server.master.runner.execute.DefaultTaskExecuteRunnableFactory;
import org.apache.dolphinscheduler.server.master.runner.taskgroup.TaskGroupCoordinator;
import org.apache.dolphinscheduler.service.alert.ListenerEventAlertManager;
import org.apache.dolphinscheduler.service.alert.ProcessAlertManager;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.command.CommandService;
import org.apache.dolphinscheduler.service.expand.CuringParamsService;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WorkflowExecuteRunnableTest {

    private WorkflowExecuteRunnable workflowExecuteThread;

    private ProcessInstance processInstance;

    private TaskInstanceDao taskInstanceDao;

    private TaskDefinitionLogDao taskDefinitionLogDao;
    private ProcessService processService;

    private CommandService commandService;

    private ProcessInstanceDao processInstanceDao;

    private MasterConfig config;

    private ApplicationContext applicationContext;

    private StateWheelExecuteThread stateWheelExecuteThread;

    private CuringParamsService curingGlobalParamsService;

    private DefaultTaskExecuteRunnableFactory defaultTaskExecuteRunnableFactory;

    private WorkflowExecuteContextFactory workflowExecuteContextFactory;

    private ListenerEventAlertManager listenerEventAlertManager;

    private TaskGroupCoordinator taskGroupCoordinator;

    private WorkflowExecuteContext workflowExecuteContext;

    @BeforeEach
    public void init() throws Exception {
        applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);

        config = new MasterConfig();
        processService = Mockito.mock(ProcessService.class);
        commandService = Mockito.mock(CommandService.class);
        processInstanceDao = Mockito.mock(ProcessInstanceDao.class);
        processInstance = Mockito.mock(ProcessInstance.class);
        taskInstanceDao = Mockito.mock(TaskInstanceDao.class);
        taskDefinitionLogDao = Mockito.mock(TaskDefinitionLogDao.class);
        defaultTaskExecuteRunnableFactory = Mockito.mock(DefaultTaskExecuteRunnableFactory.class);
        workflowExecuteContextFactory = Mockito.mock(WorkflowExecuteContextFactory.class);
        listenerEventAlertManager = Mockito.mock(ListenerEventAlertManager.class);

        Map<String, String> cmdParam = new HashMap<>();
        cmdParam.put(CMD_PARAM_COMPLEMENT_DATA_START_DATE, "2020-01-01 00:00:00");
        cmdParam.put(CMD_PARAM_COMPLEMENT_DATA_END_DATE, "2020-01-20 23:00:00");
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setGlobalParamMap(Collections.emptyMap());
        processDefinition.setGlobalParamList(Collections.emptyList());
        Mockito.when(processInstance.getProcessDefinition()).thenReturn(processDefinition);

        stateWheelExecuteThread = Mockito.mock(StateWheelExecuteThread.class);
        curingGlobalParamsService = Mockito.mock(CuringParamsService.class);
        ProcessAlertManager processAlertManager = Mockito.mock(ProcessAlertManager.class);
        workflowExecuteContext = Mockito.mock(WorkflowExecuteContext.class);
        Mockito.when(workflowExecuteContext.getWorkflowInstance()).thenReturn(processInstance);
        IWorkflowGraph workflowGraph = Mockito.mock(IWorkflowGraph.class);
        Mockito.when(workflowExecuteContext.getWorkflowGraph()).thenReturn(workflowGraph);
        Mockito.when(workflowGraph.getDag()).thenReturn(new DAG<>());

        taskGroupCoordinator = Mockito.mock(TaskGroupCoordinator.class);

        workflowExecuteThread = Mockito.spy(
                new WorkflowExecuteRunnable(
                        workflowExecuteContext,
                        commandService,
                        processService,
                        processInstanceDao,
                        processAlertManager,
                        config,
                        stateWheelExecuteThread,
                        curingGlobalParamsService,
                        taskInstanceDao,
                        defaultTaskExecuteRunnableFactory,
                        listenerEventAlertManager,
                        taskGroupCoordinator));
    }

    @Test
    public void testParseStartNodeName() {
        try {
            Map<String, String> cmdParam = new HashMap<>();
            cmdParam.put(CMD_PARAM_START_NODES, "1,2,3");
            Class<WorkflowExecuteRunnable> masterExecThreadClass = WorkflowExecuteRunnable.class;
            Method method = masterExecThreadClass.getDeclaredMethod("parseStartNodeName", String.class);
            method.setAccessible(true);
            List<String> nodeNames =
                    (List<String>) method.invoke(workflowExecuteThread, JSONUtils.toJsonString(cmdParam));
            Assertions.assertEquals(3, nodeNames.size());
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void testGetStartTaskInstanceList() {
        try {
            TaskInstance taskInstance1 = new TaskInstance();
            taskInstance1.setId(1);
            TaskInstance taskInstance2 = new TaskInstance();
            taskInstance2.setId(2);
            TaskInstance taskInstance3 = new TaskInstance();
            taskInstance3.setId(3);
            TaskInstance taskInstance4 = new TaskInstance();
            taskInstance4.setId(4);
            Map<String, String> cmdParam = new HashMap<>();
            cmdParam.put(CMD_PARAM_RECOVERY_START_NODE_STRING, "1,2,3,4");
            Mockito.when(taskInstanceDao.queryByIds(
                    Arrays.asList(taskInstance1.getId(), taskInstance2.getId(), taskInstance3.getId(),
                            taskInstance4.getId())))
                    .thenReturn(Arrays.asList(taskInstance1, taskInstance2, taskInstance3, taskInstance4));
            Class<WorkflowExecuteRunnable> masterExecThreadClass = WorkflowExecuteRunnable.class;
            Method method = masterExecThreadClass.getDeclaredMethod("getRecoverTaskInstanceList", String.class);
            method.setAccessible(true);
            List<TaskInstance> taskInstances =
                    workflowExecuteThread.getRecoverTaskInstanceList(JSONUtils.toJsonString(cmdParam));
            Assertions.assertEquals(4, taskInstances.size());

            cmdParam.put(CMD_PARAM_RECOVERY_START_NODE_STRING, "1");
            List<TaskInstance> taskInstanceEmpty =
                    (List<TaskInstance>) method.invoke(workflowExecuteThread, JSONUtils.toJsonString(cmdParam));
            Assertions.assertTrue(taskInstanceEmpty.isEmpty());

        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void testInitializeTaskInstanceVarPool() {
        try {
            IWorkflowGraph workflowGraph = Mockito.mock(IWorkflowGraph.class);
            Mockito.when(workflowExecuteContext.getWorkflowGraph()).thenReturn(workflowGraph);
            TaskNode taskNode = Mockito.mock(TaskNode.class);
            Mockito.when(workflowGraph.getTaskNodeByCode(Mockito.anyLong())).thenReturn(taskNode);
            Mockito.when(taskNode.getPreTasks()).thenReturn(JSONUtils.toJsonString(Lists.newArrayList(1L, 2L)));

            TaskInstance taskInstance = new TaskInstance();

            TaskInstance taskInstance1 = new TaskInstance();
            taskInstance1.setId(1);
            taskInstance1.setTaskCode(1);
            taskInstance1.setVarPool("[{\"direct\":\"OUT\",\"prop\":\"test1\",\"type\":\"VARCHAR\",\"value\":\"1\"}]");
            taskInstance1.setEndTime(new Date());

            TaskInstance taskInstance2 = new TaskInstance();
            taskInstance2.setId(2);
            taskInstance2.setTaskCode(2);
            taskInstance2.setVarPool("[{\"direct\":\"OUT\",\"prop\":\"test2\",\"type\":\"VARCHAR\",\"value\":\"2\"}]");
            taskInstance2.setEndTime(new Date());

            Map<Integer, TaskInstance> taskInstanceMap = new ConcurrentHashMap<>();
            taskInstanceMap.put(taskInstance1.getId(), taskInstance1);
            taskInstanceMap.put(taskInstance2.getId(), taskInstance2);

            Map<Long, TaskInstance> taskCodeInstanceMap = new ConcurrentHashMap<>();
            taskCodeInstanceMap.put(taskInstance1.getTaskCode(), taskInstance1);
            taskCodeInstanceMap.put(taskInstance2.getTaskCode(), taskInstance2);

            Set<Long> completeTaskSet = Sets.newConcurrentHashSet();
            completeTaskSet.add(taskInstance1.getTaskCode());
            completeTaskSet.add(taskInstance2.getTaskCode());

            Class<WorkflowExecuteRunnable> masterExecThreadClass = WorkflowExecuteRunnable.class;

            Field completeTaskSetField = masterExecThreadClass.getDeclaredField("completeTaskSet");
            completeTaskSetField.setAccessible(true);
            completeTaskSetField.set(workflowExecuteThread, completeTaskSet);

            Field taskInstanceMapField = masterExecThreadClass.getDeclaredField("taskInstanceMap");
            taskInstanceMapField.setAccessible(true);
            taskInstanceMapField.set(workflowExecuteThread, taskInstanceMap);

            Field taskCodeInstanceMapField = masterExecThreadClass.getDeclaredField("taskCodeInstanceMap");
            taskCodeInstanceMapField.setAccessible(true);
            taskCodeInstanceMapField.set(workflowExecuteThread, taskCodeInstanceMap);

            workflowExecuteThread.initializeTaskInstanceVarPool(taskInstance);
            Assertions.assertNotNull(taskInstance.getVarPool());

            taskInstance2.setVarPool("[{\"direct\":\"OUT\",\"prop\":\"test1\",\"type\":\"VARCHAR\",\"value\":\"2\"}]");
            completeTaskSet.add(taskInstance2.getTaskCode());

            completeTaskSetField.setAccessible(true);
            completeTaskSetField.set(workflowExecuteThread, completeTaskSet);
            taskInstanceMapField.setAccessible(true);
            taskInstanceMapField.set(workflowExecuteThread, taskInstanceMap);

            workflowExecuteThread.initializeTaskInstanceVarPool(taskInstance);
            Assertions.assertNotNull(taskInstance.getVarPool());
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void testCheckSerialProcess() {
        try {
            ProcessDefinition processDefinition1 = new ProcessDefinition();
            processDefinition1.setId(123);
            processDefinition1.setName("test");
            processDefinition1.setVersion(1);
            processDefinition1.setCode(11L);
            processDefinition1.setExecutionType(ProcessExecutionTypeEnum.SERIAL_WAIT);
            Mockito.when(processInstance.getId()).thenReturn(225);
            workflowExecuteThread.checkSerialProcess(processDefinition1);

            Mockito.when(processInstance.getNextProcessInstanceId()).thenReturn(222);
            ProcessInstance processInstance9 = new ProcessInstance();
            processInstance9.setId(222);
            processInstance9.setProcessDefinitionCode(11L);
            processInstance9.setProcessDefinitionVersion(1);
            processInstance9.setState(WorkflowExecutionStatus.SERIAL_WAIT);

            Mockito.when(processService.findProcessInstanceById(222)).thenReturn(processInstance9);
            workflowExecuteThread.checkSerialProcess(processDefinition1);
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void testClearDataIfExecuteTask() throws NoSuchFieldException, IllegalAccessException {
        TaskInstance taskInstance1 = new TaskInstance();
        taskInstance1.setId(1);
        taskInstance1.setTaskCode(1);

        TaskInstance taskInstance2 = new TaskInstance();
        taskInstance2.setId(2);
        taskInstance2.setTaskCode(2);

        Map<Integer, TaskInstance> taskInstanceMap = new ConcurrentHashMap<>();
        taskInstanceMap.put(taskInstance1.getId(), taskInstance1);
        taskInstanceMap.put(taskInstance2.getId(), taskInstance2);

        Map<Long, TaskInstance> taskCodeInstanceMap = new ConcurrentHashMap<>();
        taskCodeInstanceMap.put(taskInstance1.getTaskCode(), taskInstance1);
        taskCodeInstanceMap.put(taskInstance2.getTaskCode(), taskInstance2);

        Set<Long> completeTaskSet = Sets.newConcurrentHashSet();
        completeTaskSet.add(taskInstance1.getTaskCode());
        completeTaskSet.add(taskInstance2.getTaskCode());

        Class<WorkflowExecuteRunnable> masterExecThreadClass = WorkflowExecuteRunnable.class;

        Field completeTaskMapField = masterExecThreadClass.getDeclaredField("completeTaskSet");
        completeTaskMapField.setAccessible(true);
        completeTaskMapField.set(workflowExecuteThread, completeTaskSet);

        Field taskInstanceMapField = masterExecThreadClass.getDeclaredField("taskInstanceMap");
        taskInstanceMapField.setAccessible(true);
        taskInstanceMapField.set(workflowExecuteThread, taskInstanceMap);

        Field taskCodeInstanceMapField = masterExecThreadClass.getDeclaredField("taskCodeInstanceMap");
        taskCodeInstanceMapField.setAccessible(true);
        taskCodeInstanceMapField.set(workflowExecuteThread, taskCodeInstanceMap);

        Mockito.when(processInstance.getCommandType()).thenReturn(CommandType.EXECUTE_TASK);
        Mockito.when(processInstance.getId()).thenReturn(123);

        DAG<Long, TaskNode, TaskNodeRelation> dag = Mockito.mock(DAG.class);
        Set<Long> taskCodesString = new HashSet<>();
        taskCodesString.add(1L);
        taskCodesString.add(2L);
        Mockito.when(dag.getAllNodesList()).thenReturn(taskCodesString);
        Mockito.when(dag.containsNode(1L)).thenReturn(true);
        Mockito.when(dag.containsNode(2L)).thenReturn(false);

        WorkflowExecuteContext workflowExecuteContext = Mockito.mock(WorkflowExecuteContext.class);
        Mockito.when(workflowExecuteContext.getWorkflowInstance()).thenReturn(processInstance);
        IWorkflowGraph workflowGraph = Mockito.mock(IWorkflowGraph.class);
        Mockito.when(workflowExecuteContext.getWorkflowGraph()).thenReturn(workflowGraph);
        Mockito.when(workflowGraph.getDag()).thenReturn(dag);

        Field dagField = masterExecThreadClass.getDeclaredField("workflowExecuteContext");
        dagField.setAccessible(true);
        dagField.set(workflowExecuteThread, workflowExecuteContext);

        Mockito.when(taskInstanceDao.queryByWorkflowInstanceIdAndTaskCode(processInstance.getId(),
                taskInstance1.getTaskCode()))
                .thenReturn(taskInstance1);
        Mockito.when(taskInstanceDao.queryByWorkflowInstanceIdAndTaskCode(processInstance.getId(),
                taskInstance2.getTaskCode()))
                .thenReturn(null);

        workflowExecuteThread.clearDataIfExecuteTask();

        Assertions.assertEquals(1, taskInstanceMap.size());
        Assertions.assertEquals(1, completeTaskSet.size());

    }

    private List<Schedule> zeroSchedulerList() {
        return Collections.emptyList();
    }

    private List<Schedule> oneSchedulerList() {
        List<Schedule> schedulerList = new LinkedList<>();
        Schedule schedule = new Schedule();
        schedule.setCrontab("0 0 0 1/2 * ?");
        schedulerList.add(schedule);
        return schedulerList;
    }

    @Test
    void testTryToDispatchTaskInstance() {
        // task instance already finished, not dispatch
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.PAUSE);
        Mockito.when(processInstance.isBlocked()).thenReturn(true);
        TaskExecuteRunnable taskExecuteRunnable = Mockito.mock(TaskExecuteRunnable.class);
        workflowExecuteThread.tryToDispatchTaskInstance(taskInstance, taskExecuteRunnable);
        Mockito.verify(taskExecuteRunnable, Mockito.never()).dispatch();

        // submit success should dispatch
        taskInstance = new TaskInstance();
        taskInstance.setState(TaskExecutionStatus.SUBMITTED_SUCCESS);
        workflowExecuteThread.tryToDispatchTaskInstance(taskInstance, taskExecuteRunnable);
        Mockito.verify(taskExecuteRunnable).dispatch();
    }
}
