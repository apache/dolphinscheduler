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
package org.apache.dolphinscheduler.server.worker.task.dependent;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.model.DateInterval;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.utils.dependent.DependentDateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
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


    @Before
    public void before() throws Exception{
        processService = Mockito.mock(ProcessService.class);
        Mockito.when(processService
                .findLastRunningProcess(4,DependentDateUtils.getTodayInterval(new Date()).get(0)))
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
        applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);
    }

    @Test
    public void test() throws Exception{

        TaskProps taskProps = new TaskProps();
        String dependString = "{\"dependTaskList\":[{\"dependItemList\":[{\"dateValue\":\"today\",\"depTasks\":\"ALL\",\"projectId\":1,\"definitionList\":[{\"label\":\"C\",\"value\":4},{\"label\":\"B\",\"value\":3},{\"label\":\"A\",\"value\":2}],\"cycle\":\"day\",\"definitionId\":4}],\"relation\":\"AND\"}],\"relation\":\"AND\"}";
        taskProps.setTaskInstId(252612);
        taskProps.setDependence(dependString);
        taskProps.setTaskStartTime(new Date());
        DependentTask dependentTask = new DependentTask(taskProps, logger);
        dependentTask.init();
        dependentTask.handle();
        Assert.assertEquals(dependentTask.getExitStatusCode(), Constants.EXIT_CODE_SUCCESS );
    }

    private ProcessInstance findLastProcessInterval(){
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(11);
        processInstance.setState(ExecutionStatus.SUCCESS);
        return  processInstance;
    }

    private List<TaskNode> getTaskNodes(){
        List<TaskNode> list = new ArrayList<>();
        TaskNode taskNode = new TaskNode();
        taskNode.setName("C");
        taskNode.setType("SQL");
        list.add(taskNode);
        return list;
    }

    private List<TaskInstance> getTaskInstances(){
        List<TaskInstance> list = new ArrayList<>();
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setName("C");
        taskInstance.setState(ExecutionStatus.SUCCESS);
        taskInstance.setDependency("1231");
        list.add(taskInstance);
        return list;
    }

    private TaskInstance getTaskInstance(){
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(252612);
        taskInstance.setName("C");
        taskInstance.setState(ExecutionStatus.SUCCESS);
        return taskInstance;
    }

}