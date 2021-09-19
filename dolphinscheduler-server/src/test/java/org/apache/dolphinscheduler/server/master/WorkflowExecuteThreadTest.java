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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.task.blocking.BlockingParameters;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread;
import org.apache.dolphinscheduler.server.master.runner.task.BlockingTaskProcessor;
import org.apache.dolphinscheduler.server.master.runner.task.ITaskProcessor;
import org.apache.dolphinscheduler.server.master.runner.task.TaskProcessorFactory;
import org.apache.dolphinscheduler.service.alert.ProcessAlertManager;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
@PrepareForTest({WorkflowExecuteThread.class,TaskProcessorFactory.class})
public class WorkflowExecuteThreadTest {

    private WorkflowExecuteThread workflowExecuteThread;

    private ProcessInstance processInstance;

    private ProcessService processService;

    private int processDefinitionId = 1;

    private MasterConfig config;

    private ApplicationContext applicationContext;

    private Field dag;

    private ProcessAlertManager alertManager;

    @Before
    public void init() throws Exception {
        processService = mock(ProcessService.class);
        alertManager = mock(ProcessAlertManager.class);

        applicationContext = mock(ApplicationContext.class);
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
        processDefinition.setGlobalParamMap(Collections.emptyMap());
        processDefinition.setGlobalParamList(Collections.emptyList());
        Mockito.when(processInstance.getProcessDefinition()).thenReturn(processDefinition);

        ConcurrentHashMap<Integer, TaskInstance> taskTimeoutCheckList = new ConcurrentHashMap<>();
        workflowExecuteThread = PowerMockito.spy(new WorkflowExecuteThread(processInstance, processService, null, alertManager, config, taskTimeoutCheckList));
        // prepareProcess init dag
        dag = WorkflowExecuteThread.class.getDeclaredField("dag");
        dag.setAccessible(true);
        dag.set(workflowExecuteThread, new DAG());
        // PowerMockito.doNothing().when(workflowExecuteThread, "executeProcess");
        // PowerMockito.doNothing().when(workflowExecuteThread, "prepareProcess");
        // PowerMockito.doNothing().when(workflowExecuteThread, "runProcess");
        // PowerMockito.doNothing().when(workflowExecuteThread, "endProcess");
    }

    /**
     * without schedule
     */
    @Test
    public void testParallelWithOutSchedule() throws ParseException {
        // try {
        //    Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionCode(processDefinitionId)).thenReturn(zeroSchedulerList());
        //    Method method = WorkflowExecuteThread.class.getDeclaredMethod("executeComplementProcess");
        //    method.setAccessible(true);
        //    method.invoke(workflowExecuteThread);
        //    one create save, and 1-30 for next save, and last day 20 no save
        //    verify(processService, times(20)).saveProcessInstance(processInstance);
        // } catch (Exception e) {
        //    e.printStackTrace();
        //    Assert.fail();
        // }
    }

    /**
     * with schedule
     */
    @Test
    public void testParallelWithSchedule() {
        // try {
        //    Mockito.when(processService.queryReleaseSchedulerListByProcessDefinitionCode(processDefinitionId)).thenReturn(oneSchedulerList());
        //    Method method = WorkflowExecuteThread.class.getDeclaredMethod("executeComplementProcess");
        //    method.setAccessible(true);
        //    method.invoke(workflowExecuteThread);
        //    // one create save, and 9(1 to 20 step 2) for next save, and last day 31 no save
        //    verify(processService, times(20)).saveProcessInstance(processInstance);
        // } catch (Exception e) {
        //    Assert.fail();
        // }
    }

    @Test
    public void testParseStartNodeName() throws ParseException {
        // try {
        //    Map<String, String> cmdParam = new HashMap<>();
        //    cmdParam.put(CMD_PARAM_START_NODE_NAMES, "t1,t2,t3");
        //    Mockito.when(processInstance.getCommandParam()).thenReturn(JSONUtils.toJsonString(cmdParam));
        //    Class<WorkflowExecuteThread> masterExecThreadClass = WorkflowExecuteThread.class;
        //    Method method = masterExecThreadClass.getDeclaredMethod("parseStartNodeName", String.class);
        //    method.setAccessible(true);
        //    List<String> nodeNames = (List<String>) method.invoke(workflowExecuteThread, JSONUtils.toJsonString(cmdParam));
        //    Assert.assertEquals(3, nodeNames.size());
        // } catch (Exception e) {
        //    Assert.fail();
        // }
    }

    @Test
    public void testRetryTaskIntervalOverTime() {
        // try {
        //    TaskInstance taskInstance = new TaskInstance();
        //    taskInstance.setId(0);
        //    taskInstance.setMaxRetryTimes(0);
        //    taskInstance.setRetryInterval(0);
        //    taskInstance.setState(ExecutionStatus.FAILURE);
        //    Class<WorkflowExecuteThread> masterExecThreadClass = WorkflowExecuteThread.class;
        //    Method method = masterExecThreadClass.getDeclaredMethod("retryTaskIntervalOverTime", TaskInstance.class);
        //    method.setAccessible(true);
        //    Assert.assertTrue((Boolean) method.invoke(workflowExecuteThread, taskInstance));
        // } catch (Exception e) {
        //    Assert.fail();
        // }
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

    private DAG<String, TaskNode, TaskNodeRelation> genDagForBlockingTest() {
        /**
         * the test dag looks like below
         * 1(blocking)
         */
        DAG<String, TaskNode, TaskNodeRelation> dag = new DAG<>();

        TaskNode blockingNode = new TaskNode();
        blockingNode.setName("0");
        blockingNode.setType("BLOCKING");
        blockingNode.setCode(0L);
        blockingNode.setVersion(1);

        dag.addNode("0",blockingNode);

        return dag;
    }

    private TaskInstance getBlockingTaskInstance(boolean isAlert) {
        // define fake blocking task instance
        TaskInstance blockingTaskInstance = new TaskInstance();
        blockingTaskInstance.setId(0);
        blockingTaskInstance.setName("0");
        blockingTaskInstance.setTaskCode(0L);
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

    private void initEnvForBlockingTest(boolean isBlocked, boolean isAlert) {
        try {
            // create dag
            dag.set(workflowExecuteThread,genDagForBlockingTest());
            // create spring context
            SpringApplicationContext springApplicationContext = new SpringApplicationContext();
            springApplicationContext.setApplicationContext(applicationContext);
            // task instance
            TaskInstance blockingTaskInstance = getBlockingTaskInstance(isAlert);
            // mock class
            ITaskProcessor processor = mock(BlockingTaskProcessor.class);
            PowerMockito.mockStatic(TaskProcessorFactory.class);
            // mock action
            Mockito.when(processService.findTaskInstanceById(any(Integer.class)))
                    .thenReturn(blockingTaskInstance);
            Mockito.when(processService.findProcessInstanceById(processInstance.getId()))
                    .thenReturn(processInstance);
            PowerMockito.doNothing().when(processService,"saveProcessInstance",processInstance);
            PowerMockito.doNothing().when(processor,"run");
            Mockito.when(SpringApplicationContext.getBean(ProcessService.class))
                    .thenReturn(processService);
            PowerMockito.when(TaskProcessorFactory.getTaskProcessor(blockingTaskInstance.getTaskType()))
                    .thenReturn(processor);
            Mockito.when(processor.submit(any(TaskInstance.class),
                    any(ProcessInstance.class),
                    any(Integer.class),
                    any(Integer.class)))
                    .thenReturn(true);
            Mockito.when(processor.taskExtraInfo())
                    .thenReturn(isBlocked);
            Mockito.when(processor.taskState())
                    .thenReturn(ExecutionStatus.SUCCESS);
            Mockito.when(processService.findTaskDefinition(blockingTaskInstance.getTaskCode(),
                    blockingTaskInstance.getTaskDefinitionVersion()))
                    .thenReturn(new TaskDefinition());
            Mockito.when(processService.updateProcessInstanceState(blockingTaskInstance.getId(),
                    ExecutionStatus.READY_PAUSE))
                    .thenReturn(1);
            if(isBlocked){
                Mockito.when(processInstance.getState())
                        .thenReturn(ExecutionStatus.RUNNING_EXECUTION)
                        .thenReturn(ExecutionStatus.READY_PAUSE);
            } else {
                Mockito.when(processInstance.getState())
                        .thenReturn(ExecutionStatus.RUNNING_EXECUTION)
                        .thenReturn(ExecutionStatus.SUCCESS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testBlockingWithNoBlocked(){
        try {
            initEnvForBlockingTest(false,false);
            // test method
            Method main = WorkflowExecuteThread.class.getDeclaredMethod("run");
            main.setAccessible(true);
            main.invoke(workflowExecuteThread);
            Field isProcessBlocked = WorkflowExecuteThread.class.getDeclaredField("isProcessBlocked");
            isProcessBlocked.setAccessible(true);
            Assert.assertEquals(Flag.NO,isProcessBlocked.get(workflowExecuteThread));
            Assert.assertEquals(ExecutionStatus.SUCCESS,processInstance.getState());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testBlockingWithBlockedWithoutAlert(){
        try{
            initEnvForBlockingTest(true,false);
            // test method
            Method main = WorkflowExecuteThread.class.getDeclaredMethod("run");
            main.setAccessible(true);
            main.invoke(workflowExecuteThread);
            Field isProcessBlocked = WorkflowExecuteThread.class.getDeclaredField("isProcessBlocked");
            isProcessBlocked.setAccessible(true);
            Assert.assertEquals(Flag.YES,isProcessBlocked.get(workflowExecuteThread));
            Assert.assertEquals(ExecutionStatus.READY_PAUSE,processInstance.getState());
            verify(alertManager,never()).sendProcessBlockingAlert(processInstance,null);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void testBlockingWithBlockedWithAlert(){
        try {
            initEnvForBlockingTest(true,true);
            // test method
            Method main = WorkflowExecuteThread.class.getDeclaredMethod("run");
            main.setAccessible(true);
            main.invoke(workflowExecuteThread);
            Field isProcessBlocked = WorkflowExecuteThread.class.getDeclaredField("isProcessBlocked");
            isProcessBlocked.setAccessible(true);
            Assert.assertEquals(Flag.YES,isProcessBlocked.get(workflowExecuteThread));
            Assert.assertEquals(ExecutionStatus.READY_PAUSE,processInstance.getState());
            verify(alertManager,times(1)).sendProcessBlockingAlert(processInstance,null);
        } catch (Exception e) {
            Assert.fail();
        }
    }

}