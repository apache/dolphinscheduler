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
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.ConditionsTaskExecThread;
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
import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ConditionsTaskTest {


    private static final Logger logger = LoggerFactory.getLogger(DependentTaskTest.class);

    private ProcessService processService;
    private ApplicationContext applicationContext;


    private MasterConfig config;

    @Before
    public void before() {
        config = new MasterConfig();
        config.setMasterTaskCommitRetryTimes(3);
        config.setMasterTaskCommitInterval(1000);
        processService = Mockito.mock(ProcessService.class);
        applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);
        Mockito.when(applicationContext.getBean(MasterConfig.class)).thenReturn(config);

        Mockito.when(processService
                .findTaskInstanceById(252612))
                .thenReturn(getTaskInstance());

        Mockito.when(processService.saveTaskInstance(getTaskInstance()))
                .thenReturn(true);

        Mockito.when(processService.findProcessInstanceById(10112))
                .thenReturn(getProcessInstance());

        Mockito.when(processService
                .findValidTaskListByProcessId(10112))
                .thenReturn(getTaskInstances());
    }

    @Test
    public void testCondition(){
        TaskInstance taskInstance = getTaskInstance();
        String dependString = "{\"dependTaskList\":[{\"dependItemList\":[{\"depTasks\":\"1\",\"status\":\"SUCCESS\"}],\"relation\":\"AND\"}],\"relation\":\"AND\"}";
        String conditionResult = "{\"successNode\":[\"2\"],\"failedNode\":[\"3\"]}";

        taskInstance.setDependency(dependString);
        Mockito.when(processService.submitTask(taskInstance))
                .thenReturn(taskInstance);
        ConditionsTaskExecThread conditions =
                new ConditionsTaskExecThread(taskInstance);

        try {
            conditions.call();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals(ExecutionStatus.SUCCESS, conditions.getTaskInstance().getState());
    }


    private TaskInstance getTaskInstance(){
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(252612);
        taskInstance.setName("C");
        taskInstance.setTaskType("CONDITIONS");
        taskInstance.setProcessInstanceId(10112);
        taskInstance.setProcessDefinitionId(100001);
        return taskInstance;
    }



    private List<TaskInstance> getTaskInstances(){
        List<TaskInstance> list = new ArrayList<>();
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(199999);
        taskInstance.setName("1");
        taskInstance.setState(ExecutionStatus.SUCCESS);
        list.add(taskInstance);
        return list;
    }

    private ProcessInstance getProcessInstance(){
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(10112);
        processInstance.setProcessDefinitionId(100001);
        processInstance.setState(ExecutionStatus.RUNNING_EXEUTION);

        return processInstance;
    }

}
