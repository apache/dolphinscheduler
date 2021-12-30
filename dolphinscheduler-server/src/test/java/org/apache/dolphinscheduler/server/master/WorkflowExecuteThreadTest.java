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

import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_END_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMDPARAM_COMPLEMENT_DATA_START_DATE;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_RECOVERY_START_NODE_STRING;
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_START_NODES;

import static org.powermock.api.mockito.PowerMockito.mock;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;

/**
 * test for WorkflowExecuteThread
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({WorkflowExecuteThread.class})
public class WorkflowExecuteThreadTest {

    private WorkflowExecuteThread workflowExecuteThread;

    private ProcessInstance processInstance;

    private ProcessService processService;

    private int processDefinitionId = 1;

    private MasterConfig config;

    private ApplicationContext applicationContext;

    @Before
    public void init() throws Exception {
        processService = mock(ProcessService.class);

        applicationContext = mock(ApplicationContext.class);
        config = new MasterConfig();
        Mockito.when(applicationContext.getBean(MasterConfig.class)).thenReturn(config);

        processInstance = mock(ProcessInstance.class);
        Mockito.when(processInstance.getState()).thenReturn(ExecutionStatus.SUCCESS);
        Mockito.when(processInstance.getHistoryCmd()).thenReturn(CommandType.COMPLEMENT_DATA.toString());
        Mockito.when(processInstance.getIsSubProcess()).thenReturn(Flag.NO);
        Mockito.when(processInstance.getScheduleTime()).thenReturn(DateUtils.stringToDate("2020-01-01 00:00:00"));
        Map<String, String> cmdParam = new HashMap<>();
        cmdParam.put(CMDPARAM_COMPLEMENT_DATA_START_DATE, "2020-01-01 00:00:00");
        cmdParam.put(CMDPARAM_COMPLEMENT_DATA_END_DATE, "2020-01-20 23:00:00");
        Mockito.when(processInstance.getCommandParam()).thenReturn(JSONUtils.toJsonString(cmdParam));
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setGlobalParamMap(Collections.emptyMap());
        processDefinition.setGlobalParamList(Collections.emptyList());
        Mockito.when(processInstance.getProcessDefinition()).thenReturn(processDefinition);

        ConcurrentHashMap<Integer, TaskInstance> taskTimeoutCheckList = new ConcurrentHashMap<>();
        ConcurrentHashMap<Integer, TaskInstance> taskRetryCheckList = new ConcurrentHashMap<>();
        workflowExecuteThread = PowerMockito.spy(new WorkflowExecuteThread(processInstance, null,processService, null, null, config, taskTimeoutCheckList, taskRetryCheckList));
        // prepareProcess init dag
        Field dag = WorkflowExecuteThread.class.getDeclaredField("dag");
        dag.setAccessible(true);
        dag.set(workflowExecuteThread, new DAG());
        PowerMockito.doNothing().when(workflowExecuteThread, "endProcess");
    }


    @Test
    public void testParseStartNodeName() throws ParseException {
        try {
            Map<String, String> cmdParam = new HashMap<>();
            cmdParam.put(CMD_PARAM_START_NODES, "1,2,3");
            Mockito.when(processInstance.getCommandParam()).thenReturn(JSONUtils.toJsonString(cmdParam));
            Class<WorkflowExecuteThread> masterExecThreadClass = WorkflowExecuteThread.class;
            Method method = masterExecThreadClass.getDeclaredMethod("parseStartNodeName", String.class);
            method.setAccessible(true);
            List<String> nodeNames = (List<String>) method.invoke(workflowExecuteThread, JSONUtils.toJsonString(cmdParam));
            Assert.assertEquals(3, nodeNames.size());
        } catch (Exception e) {
            Assert.fail();
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
            Mockito.when(processService.findTaskInstanceById(1)).thenReturn(taskInstance1);
            Mockito.when(processService.findTaskInstanceById(2)).thenReturn(taskInstance2);
            Mockito.when(processService.findTaskInstanceById(3)).thenReturn(taskInstance3);
            Mockito.when(processService.findTaskInstanceById(4)).thenReturn(taskInstance4);
            Class<WorkflowExecuteThread> masterExecThreadClass = WorkflowExecuteThread.class;
            Method method = masterExecThreadClass.getDeclaredMethod("getStartTaskInstanceList", String.class);
            method.setAccessible(true);
            List<TaskInstance> taskInstances = (List<TaskInstance>) method.invoke(workflowExecuteThread, JSONUtils.toJsonString(cmdParam));
            Assert.assertEquals(4, taskInstances.size());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testGetPreVarPool() {
        try {
            Set<String> preTaskName = new HashSet<>();
            preTaskName.add("test1");
            preTaskName.add("test2");
            Map<String, TaskInstance> completeTaskList = new ConcurrentHashMap<>();

            TaskInstance taskInstance = new TaskInstance();

            TaskInstance taskInstance1 = new TaskInstance();
            taskInstance1.setId(1);
            taskInstance1.setName("test1");
            taskInstance1.setVarPool("[{\"direct\":\"OUT\",\"prop\":\"test1\",\"type\":\"VARCHAR\",\"value\":\"1\"}]");
            taskInstance1.setEndTime(new Date());

            TaskInstance taskInstance2 = new TaskInstance();
            taskInstance2.setId(2);
            taskInstance2.setName("test2");
            taskInstance2.setVarPool("[{\"direct\":\"OUT\",\"prop\":\"test2\",\"type\":\"VARCHAR\",\"value\":\"2\"}]");
            taskInstance2.setEndTime(new Date());

            completeTaskList.put("test1", taskInstance1);
            completeTaskList.put("test2", taskInstance2);

            Class<WorkflowExecuteThread> masterExecThreadClass = WorkflowExecuteThread.class;

            Field field = masterExecThreadClass.getDeclaredField("completeTaskList");
            field.setAccessible(true);
            field.set(workflowExecuteThread, completeTaskList);

            workflowExecuteThread.getPreVarPool(taskInstance, preTaskName);
            Assert.assertNotNull(taskInstance.getVarPool());
            taskInstance2.setVarPool("[{\"direct\":\"OUT\",\"prop\":\"test1\",\"type\":\"VARCHAR\",\"value\":\"2\"}]");
            completeTaskList.put("test2", taskInstance2);
            field.setAccessible(true);
            field.set(workflowExecuteThread, completeTaskList);
            workflowExecuteThread.getPreVarPool(taskInstance, preTaskName);
            Assert.assertNotNull(taskInstance.getVarPool());
        } catch (Exception e) {
            Assert.fail();
        }
    }

}
