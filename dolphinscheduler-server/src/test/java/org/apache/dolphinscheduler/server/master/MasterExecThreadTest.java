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
import static org.apache.dolphinscheduler.common.Constants.CMD_PARAM_START_NODE_NAMES;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.task.blocking.BlockingParameters;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.*;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.BlockingTaskExecThread;
import org.apache.dolphinscheduler.server.master.runner.MasterBaseTaskExecThread;
import org.apache.dolphinscheduler.server.master.runner.MasterExecThread;
import org.apache.dolphinscheduler.service.alert.ProcessAlertManager;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;

/**
 * test for MasterExecThread
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MasterExecThread.class})
public class MasterExecThreadTest {

    private MasterExecThread masterExecThread;

    private ProcessInstance processInstance;

    private ProcessService processService;

    private int processDefinitionId = 1;

    private MasterConfig config;

    private ApplicationContext applicationContext;

    private Field dag;

    @InjectMocks
    private ProcessAlertManager alertManager;

    @Before
    public void init() throws Exception {
        processService = mock(ProcessService.class);

        applicationContext = mock(ApplicationContext.class);
        alertManager = mock(ProcessAlertManager.class);
        config = new MasterConfig();
        config.setMasterExecTaskNum(1);
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
        processDefinition.setGlobalParamMap(Collections.EMPTY_MAP);
        processDefinition.setGlobalParamList(Collections.EMPTY_LIST);
        Mockito.when(processInstance.getProcessDefinition()).thenReturn(processDefinition);

        masterExecThread = PowerMockito.spy(new MasterExecThread(processInstance, processService, null, alertManager, config));
        // prepareProcess init dag
        dag = MasterExecThread.class.getDeclaredField("dag");
        dag.setAccessible(true);
        dag.set(masterExecThread, new DAG());
        PowerMockito.doNothing().when(masterExecThread, "executeProcess");
        PowerMockito.doNothing().when(masterExecThread, "prepareProcess");
        PowerMockito.doNothing().when(masterExecThread, "endProcess");
    }

    /**
     * without schedule
     */
    @Test
    public void testParallelWithOutSchedule() throws ParseException {
        try {
            PowerMockito.doNothing().when(masterExecThread, "runProcess");
            Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(zeroSchedulerList());
            Method method = MasterExecThread.class.getDeclaredMethod("executeComplementProcess");
            method.setAccessible(true);
            method.invoke(masterExecThread);
            // one create save, and 1-30 for next save, and last day 20 no save
            verify(processService, times(20)).saveProcessInstance(processInstance);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * with schedule
     */
    @Test
    public void testParallelWithSchedule() {
        try {
            PowerMockito.doNothing().when(masterExecThread, "runProcess");
            Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionId(processDefinitionId)).thenReturn(oneSchedulerList());
            Method method = MasterExecThread.class.getDeclaredMethod("executeComplementProcess");
            method.setAccessible(true);
            method.invoke(masterExecThread);
            // one create save, and 9(1 to 20 step 2) for next save, and last day 31 no save
            verify(processService, times(20)).saveProcessInstance(processInstance);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testParseStartNodeName() throws ParseException {
        try {
            PowerMockito.doNothing().when(masterExecThread, "runProcess");
            Map<String, String> cmdParam = new HashMap<>();
            cmdParam.put(CMD_PARAM_START_NODE_NAMES, "t1,t2,t3");
            Mockito.when(processInstance.getCommandParam()).thenReturn(JSONUtils.toJsonString(cmdParam));
            Class<MasterExecThread> masterExecThreadClass = MasterExecThread.class;
            Method method = masterExecThreadClass.getDeclaredMethod("parseStartNodeName", String.class);
            method.setAccessible(true);
            List<String> nodeNames = (List<String>) method.invoke(masterExecThread, JSONUtils.toJsonString(cmdParam));
            Assert.assertEquals(3, nodeNames.size());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testRetryTaskIntervalOverTime() {
        try {
            PowerMockito.doNothing().when(masterExecThread, "runProcess");
            TaskInstance taskInstance = new TaskInstance();
            taskInstance.setId(0);
            taskInstance.setMaxRetryTimes(0);
            taskInstance.setRetryInterval(0);
            taskInstance.setState(ExecutionStatus.FAILURE);
            Class<MasterExecThread> masterExecThreadClass = MasterExecThread.class;
            Method method = masterExecThreadClass.getDeclaredMethod("retryTaskIntervalOverTime", TaskInstance.class);
            method.setAccessible(true);
            Assert.assertTrue((Boolean) method.invoke(masterExecThread, taskInstance));
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testGetStartTaskInstanceList() {
        try {
            PowerMockito.doNothing().when(masterExecThread, "runProcess");
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
            Class<MasterExecThread> masterExecThreadClass = MasterExecThread.class;
            Method method = masterExecThreadClass.getDeclaredMethod("getStartTaskInstanceList", String.class);
            method.setAccessible(true);
            List<TaskInstance> taskInstances = (List<TaskInstance>) method.invoke(masterExecThread, JSONUtils.toJsonString(cmdParam));
            Assert.assertEquals(4, taskInstances.size());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testGetPreVarPool() {
        try {
            PowerMockito.doNothing().when(masterExecThread, "runProcess");
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

            Class<MasterExecThread> masterExecThreadClass = MasterExecThread.class;

            Field field = masterExecThreadClass.getDeclaredField("completeTaskList");
            field.setAccessible(true);
            field.set(masterExecThread, completeTaskList);

            masterExecThread.getPreVarPool(taskInstance, preTaskName);
            Assert.assertNotNull(taskInstance.getVarPool());
            taskInstance2.setVarPool("[{\"direct\":\"OUT\",\"prop\":\"test1\",\"type\":\"VARCHAR\",\"value\":\"2\"}]");
            completeTaskList.put("test2", taskInstance2);
            field.setAccessible(true);
            field.set(masterExecThread, completeTaskList);
            masterExecThread.getPreVarPool(taskInstance, preTaskName);
            Assert.assertNotNull(taskInstance.getVarPool());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testBlockingWithNoBlocked(){
        try{
            Class<MasterExecThread> masterExecThreadClass = MasterExecThread.class;
            dag.set(masterExecThread,genDagForBlockingTest());
            TaskInstance blockingTaskInstance = getBlockingTaskInstance(false);
            initEnvForBlockingTest(false,blockingTaskInstance,masterExecThreadClass);
            // test method
            Method main = masterExecThreadClass.getDeclaredMethod("runProcess");
            main.setAccessible(true);
            main.invoke(masterExecThread);
            verify(processInstance).setBlockingFlag(false);
            verify(processInstance,never()).setState(ExecutionStatus.READY_PAUSE);
            Assert.assertEquals(false,processInstance.getBlockingFlag());
            Assert.assertEquals(ExecutionStatus.SUCCESS,processInstance.getState());
        }catch (Exception e){
            Assert.fail();
        }
    }

    @Test
    public void testBlockingWithBlockedWithoutAlert(){
        try{
            Class<MasterExecThread> masterExecThreadClass = MasterExecThread.class;
            dag.set(masterExecThread,genDagForBlockingTest());
            TaskInstance blockingTaskInstance = getBlockingTaskInstance(false);
            initEnvForBlockingTest(true,blockingTaskInstance,masterExecThreadClass);
            // test method
            Method main = masterExecThreadClass.getDeclaredMethod("runProcess");
            main.setAccessible(true);
            main.invoke(masterExecThread);
            verify(processInstance).setBlockingFlag(true);
            verify(processInstance).setState(ExecutionStatus.READY_PAUSE);
            verify(alertManager,never()).sendProcessBlockingAlert(processInstance,blockingTaskInstance,null);
            Assert.assertEquals(true,processInstance.getBlockingFlag());
            Assert.assertEquals(ExecutionStatus.READY_PAUSE,processInstance.getState());
        }catch (Exception e){
            Assert.fail();
        }
    }

    @Test
    public void testBlockingWithBlockedWithAlert(){
        try{
            Class<MasterExecThread> masterExecThreadClass = MasterExecThread.class;
            dag.set(masterExecThread,genDagForBlockingTest());
            TaskInstance blockingTaskInstance = getBlockingTaskInstance(true);
            initEnvForBlockingTest(true,blockingTaskInstance,masterExecThreadClass);
            // test method
            Method main = masterExecThreadClass.getDeclaredMethod("runProcess");
            main.setAccessible(true);
            main.invoke(masterExecThread);
            verify(processInstance).setBlockingFlag(true);
            verify(processInstance).setState(ExecutionStatus.READY_PAUSE);
            verify(alertManager).sendProcessBlockingAlert(processInstance,blockingTaskInstance,null);
            Assert.assertEquals(true,processInstance.getBlockingFlag());
            Assert.assertEquals(ExecutionStatus.READY_PAUSE,processInstance.getState());
        }catch (Exception e){
            Assert.fail();
        }
    }

    private List<Schedule> zeroSchedulerList() {
        return Collections.EMPTY_LIST;
    }

    private List<Schedule> oneSchedulerList() {
        List<Schedule> schedulerList = new LinkedList<>();
        Schedule schedule = new Schedule();
        schedule.setCrontab("0 0 0 1/2 * ?");
        schedulerList.add(schedule);
        return schedulerList;
    }

    private TaskInstance getBlockingTaskInstance(boolean isAlert){
        // define fake blocking task instance
        TaskInstance blockingTaskInstance = new TaskInstance();
        blockingTaskInstance.setId(1);
        blockingTaskInstance.setName("1");
        blockingTaskInstance.setTaskCode(1L);
        blockingTaskInstance.setTaskDefinitionVersion(1);
        blockingTaskInstance.setTaskType("BLOCKING");
        blockingTaskInstance.setState(ExecutionStatus.SUCCESS);
        // set blocking node params
        BlockingParameters blockingParameters = new BlockingParameters();
        blockingParameters.setAlertWhenBlocking(isAlert);
        // in this section, we focus on the process when the work flow blocked or non-blocked
        // so the blockingCondition is NOT necessary
        // we use isBlocked param to control the work flow
        blockingParameters.setBlockingCondition("");
        blockingTaskInstance.setTaskParams(JSONUtils.toJsonString(blockingParameters));
        return blockingTaskInstance;
    }

    private void initEnvForBlockingTest(boolean isBlocked, TaskInstance blockingTaskInstance, Class<MasterExecThread> masterExecThreadClass){
        try{
            // init Spring Context
            SpringApplicationContext springApplicationContext = new SpringApplicationContext();
            springApplicationContext.setApplicationContext(applicationContext);
            // define task definition for blocking node
            TaskDefinition taskDefinition = new TaskDefinition();
            taskDefinition.setTimeoutFlag(TimeoutFlag.OPEN);
            taskDefinition.setTimeoutNotifyStrategy(TaskTimeoutStrategy.WARN);
            taskDefinition.setTimeout(0);
            // define mock action
            // isBlocked stands for blocking result
            if(isBlocked){
                Mockito.when(processInstance.getBlockingFlag()).thenReturn(true);
            } else {
                Mockito.when(processInstance.getBlockingFlag()).thenReturn(false);
            }
            Mockito.when(SpringApplicationContext.getBean(ProcessService.class)).thenReturn(processService);
            Mockito.when(processService.findTaskDefinition(blockingTaskInstance.getId(),blockingTaskInstance.getTaskDefinitionVersion()))
                    .thenReturn(taskDefinition);
            Mockito.when(processService.findTaskInstanceById(blockingTaskInstance.getId())).thenReturn(blockingTaskInstance);
            Mockito.when(processService.updateProcessInstance(processInstance)).thenReturn(1);
            Mockito.when(processService.findProcessInstanceById(processInstance.getId())).thenReturn(processInstance);
            Mockito.when(processService.queryProjectWithUserByProcessInstanceId(blockingTaskInstance.getId())).thenReturn(null);
            Mockito.doNothing().when(alertManager).sendProcessBlockingAlert(processInstance,blockingTaskInstance,null);
            if(isBlocked){
                Mockito.when(processInstance.getState())
                        .thenReturn(ExecutionStatus.SUCCESS)
                        .thenReturn(ExecutionStatus.READY_PAUSE);
            } else {
                Mockito.when(processInstance.getState()).thenReturn(ExecutionStatus.SUCCESS);
            }

            Mockito.when(processInstance.isProcessInstanceStop())
                    .thenReturn(false)
                    .thenReturn(false)
                    .thenReturn(true);
            // define power mock action
            PowerMockito.doNothing().when(masterExecThread,"submitPostNode",null);
            PowerMockito.doNothing().when(masterExecThread,"submitPostNode","1");
            PowerMockito.doNothing().when(masterExecThread,"submitStandByTask");
            PowerMockito.when(masterExecThread,"canSubmitTaskToQueue").thenReturn(true);
            // define fake active task node map
            Map<MasterBaseTaskExecThread, Future<Boolean>> activeTaskNode = new ConcurrentHashMap<>();
            activeTaskNode.put(new BlockingTaskExecThread(blockingTaskInstance),
                    CompletableFuture.completedFuture(isBlocked));
            Field field = masterExecThreadClass.getDeclaredField("activeTaskNode");
            field.setAccessible(true);
            field.set(masterExecThread,activeTaskNode);

        }catch (Exception e){
            Assert.fail();
        }
    }

    private DAG<String, TaskNode, TaskNodeRelation> genDagForBlockingTest(){
        /**
         * the test dag looks like below
         * 1(blocking)
        */
        DAG<String, TaskNode, TaskNodeRelation> dag = new DAG<>();

        TaskNode blockingNode = new TaskNode();
        blockingNode.setName("1");
        blockingNode.setType("BLOCKING");
        blockingNode.setCode(1L);
        blockingNode.setVersion(1);

        dag.addNode("1",blockingNode);

        return dag;
    }

}