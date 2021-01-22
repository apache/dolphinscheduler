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

package org.apache.dolphinscheduler.server.worker.task;

import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.cache.impl.TaskExecutionContextCacheManagerImpl;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SpringApplicationContext.class})
public class TaskManagerTest {

    private static Logger logger = LoggerFactory.getLogger(TaskManagerTest.class);

    private TaskExecutionContext taskExecutionContext;

    private Logger taskLogger;

    private TaskExecutionContextCacheManagerImpl taskExecutionContextCacheManager;

    private AlertClientService alertClientService;

    @Before
    public void before() {
        // init task execution context, logger
        taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setProcessId(12345);
        taskExecutionContext.setProcessDefineId(1);
        taskExecutionContext.setProcessInstanceId(1);
        taskExecutionContext.setTaskInstanceId(1);
        taskExecutionContext.setTaskType("");
        taskExecutionContext.setFirstSubmitTime(new Date());
        taskExecutionContext.setDelayTime(0);
        taskExecutionContext.setLogPath("/tmp/test.log");
        taskExecutionContext.setHost("localhost");
        taskExecutionContext.setExecutePath("/tmp/dolphinscheduler/exec/process/1/2/3/4");

        taskLogger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(
                LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                taskExecutionContext.getProcessDefineId(),
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId()
        ));

        taskExecutionContextCacheManager = new TaskExecutionContextCacheManagerImpl();
        taskExecutionContextCacheManager.cacheTaskExecutionContext(taskExecutionContext);

        PowerMockito.mockStatic(SpringApplicationContext.class);
        PowerMockito.when(SpringApplicationContext.getBean(TaskExecutionContextCacheManagerImpl.class))
                .thenReturn(taskExecutionContextCacheManager);

        alertClientService = PowerMockito.mock(AlertClientService.class);
    }

    @Test
    public void testNewTask() {

        taskExecutionContext.setTaskType("SHELL");
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext,taskLogger,alertClientService));
        taskExecutionContext.setTaskType("WATERDROP");
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext,taskLogger,alertClientService));
        taskExecutionContext.setTaskType("HTTP");
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext,taskLogger,alertClientService));
        taskExecutionContext.setTaskType("MR");
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext,taskLogger,alertClientService));
        taskExecutionContext.setTaskType("SPARK");
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext,taskLogger,alertClientService));
        taskExecutionContext.setTaskType("FLINK");
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext,taskLogger,alertClientService));
        taskExecutionContext.setTaskType("PYTHON");
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext,taskLogger,alertClientService));
        taskExecutionContext.setTaskType("DATAX");
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext,taskLogger,alertClientService));
        taskExecutionContext.setTaskType("SQOOP");
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext,taskLogger,alertClientService));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewTaskIsNull() {
        taskExecutionContext.setTaskType(null);
        TaskManager.newTask(taskExecutionContext,taskLogger,alertClientService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewTaskIsNotExists() {
        taskExecutionContext.setTaskType("XXX");
        TaskManager.newTask(taskExecutionContext,taskLogger,alertClientService);
    }
}
