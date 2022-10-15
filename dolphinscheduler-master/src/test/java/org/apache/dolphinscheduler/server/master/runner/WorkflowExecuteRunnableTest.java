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

import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_RECOVERY_START_NODE_STRING;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_START_NODES;

import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.executor.NettyExecutorManager;
import org.apache.dolphinscheduler.service.alert.ProcessAlertManager;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.expand.CuringParamsService;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WorkflowExecuteRunnableTest {

    private WorkflowExecuteRunnable workflowExecuteThread;

    private ProcessInstance processInstance;

    private ProcessService processService;

    private ProcessInstanceDao processInstanceDao;

    private MasterConfig config;

    private ApplicationContext applicationContext;

    private StateWheelExecuteThread stateWheelExecuteThread;

    private CuringParamsService curingGlobalParamsService;

    @BeforeEach
    public void init() throws Exception {
        applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);

        config = new MasterConfig();
        processService = Mockito.mock(ProcessService.class);
        processInstanceDao = Mockito.mock(ProcessInstanceDao.class);
        processInstance = Mockito.mock(ProcessInstance.class);
        Map<String, String> cmdParam = new HashMap<>();
        cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE, "2020-01-01 00:00:00");
        cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE, "2020-01-20 23:00:00");
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setGlobalParamMap(Collections.emptyMap());
        processDefinition.setGlobalParamList(Collections.emptyList());
        Mockito.when(processInstance.getProcessDefinition()).thenReturn(processDefinition);

        stateWheelExecuteThread = Mockito.mock(StateWheelExecuteThread.class);
        curingGlobalParamsService = Mockito.mock(CuringParamsService.class);
        NettyExecutorManager nettyExecutorManager = Mockito.mock(NettyExecutorManager.class);
        ProcessAlertManager processAlertManager = Mockito.mock(ProcessAlertManager.class);
        workflowExecuteThread = Mockito.spy(
                new WorkflowExecuteRunnable(processInstance, processService, processInstanceDao, nettyExecutorManager,
                        processAlertManager, config, stateWheelExecuteThread, curingGlobalParamsService));
        Field dag = WorkflowExecuteRunnable.class.getDeclaredField("dag");
        dag.setAccessible(true);
        dag.set(workflowExecuteThread, new DAG());
    }

    @Test
    public void testParseStartNodeName() throws ParseException {
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
            Mockito.when(processService.findTaskInstanceByIdList(
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
    public void testGetPreVarPool() {
        try {
            Set<String> preTaskName = new HashSet<>();
            preTaskName.add(Long.toString(1));
            preTaskName.add(Long.toString(2));

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

            Map<Long, Integer> completeTaskList = new ConcurrentHashMap<>();
            completeTaskList.put(taskInstance1.getTaskCode(), taskInstance1.getId());
            completeTaskList.put(taskInstance2.getTaskCode(), taskInstance2.getId());

            Class<WorkflowExecuteRunnable> masterExecThreadClass = WorkflowExecuteRunnable.class;

            Field completeTaskMapField = masterExecThreadClass.getDeclaredField("completeTaskMap");
            completeTaskMapField.setAccessible(true);
            completeTaskMapField.set(workflowExecuteThread, completeTaskList);

            Field taskInstanceMapField = masterExecThreadClass.getDeclaredField("taskInstanceMap");
            taskInstanceMapField.setAccessible(true);
            taskInstanceMapField.set(workflowExecuteThread, taskInstanceMap);

            workflowExecuteThread.getPreVarPool(taskInstance, preTaskName);
            Assertions.assertNotNull(taskInstance.getVarPool());

            taskInstance2.setVarPool("[{\"direct\":\"OUT\",\"prop\":\"test1\",\"type\":\"VARCHAR\",\"value\":\"2\"}]");
            completeTaskList.put(taskInstance2.getTaskCode(), taskInstance2.getId());

            completeTaskMapField.setAccessible(true);
            completeTaskMapField.set(workflowExecuteThread, completeTaskList);
            taskInstanceMapField.setAccessible(true);
            taskInstanceMapField.set(workflowExecuteThread, taskInstanceMap);

            workflowExecuteThread.getPreVarPool(taskInstance, preTaskName);
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
            Assertions.fail();
        }
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

}
