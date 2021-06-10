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

package org.apache.dolphinscheduler.server.master.cache.impl;

import static org.apache.dolphinscheduler.common.Constants.CACHE_REFRESH_TIME_MILLIS;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.TaskExecuteAckCommand;
import org.apache.dolphinscheduler.remote.command.TaskExecuteResponseCommand;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TaskInstanceCacheManagerImplTest {

    @InjectMocks
    private TaskInstanceCacheManagerImpl taskInstanceCacheManager;

    @Mock(name = "processService")
    private ProcessService processService;

    @Before
    public void before() {

        TaskExecuteAckCommand taskExecuteAckCommand = new TaskExecuteAckCommand();
        taskExecuteAckCommand.setStatus(1);
        taskExecuteAckCommand.setExecutePath("/dolphinscheduler/worker");
        taskExecuteAckCommand.setHost("worker007");
        taskExecuteAckCommand.setLogPath("/temp/worker.log");
        taskExecuteAckCommand.setStartTime(new Date(1970, Calendar.AUGUST,7));
        taskExecuteAckCommand.setTaskInstanceId(0);

        taskInstanceCacheManager.cacheTaskInstance(taskExecuteAckCommand);

    }

    @Test
    public void testInit() throws InterruptedException {

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(0);
        taskInstance.setState(ExecutionStatus.NEED_FAULT_TOLERANCE);
        taskInstance.setExecutePath("/dolphinscheduler/worker");
        taskInstance.setHost("worker007");
        taskInstance.setLogPath("/temp/worker.log");
        taskInstance.setProcessInstanceId(0);

        Mockito.when(processService.findTaskInstanceById(0)).thenReturn(taskInstance);

        taskInstanceCacheManager.init();
        TimeUnit.MILLISECONDS.sleep(CACHE_REFRESH_TIME_MILLIS + 1000);

        Assert.assertEquals(taskInstance.getState(), taskInstanceCacheManager.getByTaskInstanceId(0).getState());

    }

    @Test
    public void getByTaskInstanceIdFromCache() {
        TaskInstance instanceGot = taskInstanceCacheManager.getByTaskInstanceId(0);

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(0);
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setExecutePath("/dolphinscheduler/worker");
        taskInstance.setHost("worker007");
        taskInstance.setLogPath("/temp/worker.log");
        taskInstance.setStartTime(new Date(1970, Calendar.AUGUST,7));

        Assert.assertEquals(taskInstance.toString(), instanceGot.toString());

    }

    @Test
    public void getByTaskInstanceIdFromDatabase() {

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setExecutePath("/dolphinscheduler/worker");
        taskInstance.setHost("worker007");
        taskInstance.setLogPath("/temp/worker.log");
        taskInstance.setStartTime(new Date(1970, Calendar.AUGUST,7));

        Mockito.when(processService.findTaskInstanceById(1)).thenReturn(taskInstance);

        TaskInstance instanceGot = taskInstanceCacheManager.getByTaskInstanceId(1);

        Assert.assertEquals(taskInstance, instanceGot);

    }

    @Test
    public void cacheTaskInstanceByTaskExecutionContext() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskInstanceId(2);
        taskExecutionContext.setTaskName("blackberrier test");
        taskExecutionContext.setStartTime(new Date(1970, Calendar.AUGUST,7));
        taskExecutionContext.setTaskType(TaskType.SPARK.getDesc());
        taskExecutionContext.setExecutePath("/tmp");

        taskInstanceCacheManager.cacheTaskInstance(taskExecutionContext);

        TaskInstance taskInstance = taskInstanceCacheManager.getByTaskInstanceId(2);

        Assert.assertEquals(taskInstance.getId(), 2);
        Assert.assertEquals(taskInstance.getName(), "blackberrier test");
        Assert.assertEquals(taskInstance.getStartTime(), new Date(1970, Calendar.AUGUST, 7));
        Assert.assertEquals(taskInstance.getTaskType(), TaskType.SPARK.getDesc());
        Assert.assertEquals(taskInstance.getExecutePath(), "/tmp");

    }

    @Test
    public void testCacheTaskInstanceByTaskExecuteAckCommand() {
        TaskInstance taskInstance = taskInstanceCacheManager.getByTaskInstanceId(0);

        Assert.assertEquals(ExecutionStatus.RUNNING_EXECUTION, taskInstance.getState());
        Assert.assertEquals(new Date(1970, Calendar.AUGUST, 7), taskInstance.getStartTime());
        Assert.assertEquals("worker007", taskInstance.getHost());
        Assert.assertEquals("/dolphinscheduler/worker", taskInstance.getExecutePath());
        Assert.assertEquals("/temp/worker.log", taskInstance.getLogPath());

    }

    @Test
    public void testCacheTaskInstanceByTaskExecuteResponseCommand() {
        TaskExecuteResponseCommand responseCommand = new TaskExecuteResponseCommand();
        responseCommand.setTaskInstanceId(0);
        responseCommand.setStatus(9);
        responseCommand.setEndTime(new Date(1970, Calendar.AUGUST, 8));

        taskInstanceCacheManager.cacheTaskInstance(responseCommand);

        TaskInstance taskInstance = taskInstanceCacheManager.getByTaskInstanceId(0);

        Assert.assertEquals(new Date(1970, Calendar.AUGUST, 8), taskInstance.getEndTime());
        Assert.assertEquals(ExecutionStatus.KILL, taskInstance.getState());

    }

    @Test
    public void removeByTaskInstanceId() {
        taskInstanceCacheManager.removeByTaskInstanceId(0);
        Assert.assertNull(taskInstanceCacheManager.getByTaskInstanceId(0));

    }
}