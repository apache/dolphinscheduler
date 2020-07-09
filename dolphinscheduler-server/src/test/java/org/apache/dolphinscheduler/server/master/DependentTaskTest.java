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
import org.apache.dolphinscheduler.common.model.DateInterval;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.utils.dependent.DependentDateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.DependentTaskExecThread;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DependentTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(DependentTaskTest.class);

    private ProcessService processService;
    private ApplicationContext applicationContext;


    private MasterConfig config;

    @Before
    public void before() throws Exception{

        config = new MasterConfig();
        config.setMasterTaskCommitRetryTimes(3);
        config.setMasterTaskCommitInterval(1000);
        processService = Mockito.mock(ProcessService.class);
        DateInterval dateInterval =DependentDateUtils.getTodayInterval(new Date()).get(0);
        Mockito.when(processService
                .findLastRunningProcess(4, dateInterval.getStartTime(),
                        dateInterval.getEndTime()))
                .thenReturn(findLastProcessInterval());



        Mockito.when(processService
                .getTaskNodeListByDefinitionId(4))
                .thenReturn(getTaskNodes());
        Mockito.when(processService
                .findValidTaskListByProcessId(11))
                .thenReturn(getTaskInstances());

        Mockito.when(processService
                .findTaskInstanceById(252612))
                .thenReturn(getTaskInstance());


        Mockito.when(processService.findProcessInstanceById(10111))
                .thenReturn(getProcessInstance());
        Mockito.when(processService.findProcessDefineById(0))
                .thenReturn(getProcessDefinition());
        Mockito.when(processService.saveTaskInstance(getTaskInstance()))
                .thenReturn(true);

        applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);
        Mockito.when(applicationContext.getBean(MasterConfig.class)).thenReturn(config);
    }

    @Test
    public void testDependAll() throws Exception{

        TaskInstance taskInstance = getTaskInstance();
        String dependString = "{\"dependTaskList\":[{\"dependItemList\":[{\"dateValue\":\"today\",\"depTasks\":\"ALL\",\"projectId\":1,\"definitionList\":[{\"label\":\"C\",\"value\":4},{\"label\":\"B\",\"value\":3},{\"label\":\"A\",\"value\":2}],\"cycle\":\"day\",\"definitionId\":4}],\"relation\":\"AND\"}],\"relation\":\"AND\"}";
        taskInstance.setDependency(dependString);

        Mockito.when(processService.submitTask(taskInstance))
                .thenReturn(taskInstance);
        DependentTaskExecThread dependentTask =
                new DependentTaskExecThread(taskInstance);

        dependentTask.call();

        Assert.assertEquals(ExecutionStatus.SUCCESS, dependentTask.getTaskInstance().getState());

        DateInterval dateInterval =DependentDateUtils.getTodayInterval(new Date()).get(0);


        Mockito.when(processService
                .findLastRunningProcess(4, dateInterval.getStartTime(),
                        dateInterval.getEndTime()))
                .thenReturn(findLastStopProcessInterval());
        DependentTaskExecThread dependentFailure = new DependentTaskExecThread(taskInstance);
        dependentFailure.call();
        Assert.assertEquals(ExecutionStatus.FAILURE, dependentFailure.getTaskInstance().getState());
    }

    @Test
    public void testDependTask() throws Exception{

        TaskInstance taskInstance = getTaskInstance();
        String dependString = "{\"dependTaskList\":[{\"dependItemList\":[{\"dateValue\":\"today\",\"depTasks\":\"D\",\"projectId\":1,\"definitionList\":[{\"label\":\"C\",\"value\":4},{\"label\":\"B\",\"value\":3},{\"label\":\"A\",\"value\":2}],\"cycle\":\"day\",\"definitionId\":4}],\"relation\":\"AND\"}],\"relation\":\"AND\"}";
        taskInstance.setDependency(dependString);
        Mockito.when(processService.submitTask(taskInstance))
                .thenReturn(taskInstance);
        DependentTaskExecThread dependentTask =
                new DependentTaskExecThread(taskInstance);

        dependentTask.call();

        Assert.assertEquals(ExecutionStatus.SUCCESS, dependentTask.getTaskInstance().getState());

        DateInterval dateInterval =DependentDateUtils.getTodayInterval(new Date()).get(0);
        Mockito.when(processService
                .findLastRunningProcess(4, dateInterval.getStartTime(),
                        dateInterval.getEndTime()))
                .thenReturn(findLastStopProcessInterval());

        Mockito.when(processService
                .findValidTaskListByProcessId(11))
                .thenReturn(getErrorTaskInstances());
        DependentTaskExecThread dependentFailure = new DependentTaskExecThread(taskInstance);
        dependentFailure.call();
        Assert.assertEquals(ExecutionStatus.FAILURE, dependentFailure.getTaskInstance().getState());
    }

    private ProcessInstance findLastStopProcessInterval(){
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(11);
        processInstance.setProcessDefinitionId(4);
        processInstance.setState(ExecutionStatus.STOP);
        return  processInstance;
    }

    private ProcessInstance findLastProcessInterval(){
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(11);
        processInstance.setProcessDefinitionId(4);
        processInstance.setState(ExecutionStatus.SUCCESS);
        return  processInstance;
    }

    private ProcessDefinition getProcessDefinition(){
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId(0);
        return processDefinition;
    }

    private ProcessInstance getProcessInstance(){
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(10111);
        processInstance.setProcessDefinitionId(0);
        processInstance.setState(ExecutionStatus.RUNNING_EXEUTION);

        return processInstance;
    }


    private List<TaskNode> getTaskNodes(){
        List<TaskNode> list = new ArrayList<>();
        TaskNode taskNode = new TaskNode();
        taskNode.setName("C");
        taskNode.setType("SQL");
        list.add(taskNode);
        return list;
    }

    private List<TaskInstance> getErrorTaskInstances(){
        List<TaskInstance> list = new ArrayList<>();
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setName("C");
        taskInstance.setState(ExecutionStatus.SUCCESS);
        taskInstance.setDependency("1231");
        list.add(taskInstance);
        return list;
    }

    private List<TaskInstance> getTaskInstances(){
        List<TaskInstance> list = new ArrayList<>();
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setName("D");
        taskInstance.setState(ExecutionStatus.SUCCESS);
        taskInstance.setDependency("1231");
        list.add(taskInstance);
        return list;
    }

    private TaskInstance getTaskInstance(){
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskType("DEPENDENT");
        taskInstance.setId(252612);
        taskInstance.setName("C");
        taskInstance.setProcessInstanceId(10111);
        taskInstance.setState(ExecutionStatus.SUBMITTED_SUCCESS);
        return taskInstance;
    }

}