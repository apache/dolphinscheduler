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

import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.sql.SqlParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.server.entity.SQLTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.cache.impl.TaskExecutionContextCacheManagerImpl;
import org.apache.dolphinscheduler.server.worker.task.shell.ShellTask;
import org.apache.dolphinscheduler.server.worker.task.sql.SqlTask;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final Logger logger = LoggerFactory.getLogger(TaskManagerTest.class);

    private TaskExecutionContext taskExecutionContext;

    private Logger taskLogger;

    private TaskExecutionContextCacheManagerImpl taskExecutionContextCacheManager;

    private AlertClientService alertClientService;

    @Before
    public void before() {
        // init task execution context, logger
        taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setProcessId(12345);
        taskExecutionContext.setProcessInstanceId(1);
        taskExecutionContext.setTaskInstanceId(1);
        taskExecutionContext.setProcessDefineCode(1L);
        taskExecutionContext.setProcessDefineVersion(1);
        taskExecutionContext.setTaskType(TaskType.SHELL.getDesc());
        taskExecutionContext.setFirstSubmitTime(new Date());
        taskExecutionContext.setDelayTime(0);
        taskExecutionContext.setLogPath("/tmp/test.log");
        taskExecutionContext.setHost("localhost");
        taskExecutionContext.setExecutePath("/tmp/dolphinscheduler/exec/process/1/2/3/4");

        taskLogger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(
                LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                taskExecutionContext.getProcessDefineCode(),
                taskExecutionContext.getProcessDefineVersion(),
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

        taskExecutionContext.setTaskType(TaskType.SHELL.getDesc());
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext, taskLogger, alertClientService));
        taskExecutionContext.setTaskType(TaskType.WATERDROP.getDesc());
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext, taskLogger, alertClientService));
        taskExecutionContext.setTaskType(TaskType.HTTP.getDesc());
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext, taskLogger, alertClientService));
        taskExecutionContext.setTaskType(TaskType.MR.getDesc());
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext, taskLogger, alertClientService));
        taskExecutionContext.setTaskType(TaskType.SPARK.getDesc());
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext, taskLogger, alertClientService));
        taskExecutionContext.setTaskType(TaskType.FLINK.getDesc());
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext, taskLogger, alertClientService));
        taskExecutionContext.setTaskType(TaskType.PYTHON.getDesc());
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext, taskLogger, alertClientService));
        taskExecutionContext.setTaskType(TaskType.DATAX.getDesc());
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext, taskLogger, alertClientService));
        taskExecutionContext.setTaskType(TaskType.SQOOP.getDesc());
        Assert.assertNotNull(TaskManager.newTask(taskExecutionContext, taskLogger, alertClientService));

    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewTaskIsNull() {
        taskExecutionContext.setTaskType(null);
        TaskManager.newTask(taskExecutionContext, taskLogger, alertClientService);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewTaskIsNotExists() {
        taskExecutionContext.setTaskType("ttt");
        TaskManager.newTask(taskExecutionContext, taskLogger, alertClientService);
    }

    @Test
    public void testShellTaskReturnString() {
        taskExecutionContext.setTaskInstanceId(1);
        taskExecutionContext.setTaskName("kris test");
        taskExecutionContext.setTaskType(TaskType.SHELL.getDesc());
        taskExecutionContext.setHost("127.0.0.1:1234");
        taskExecutionContext.setExecutePath("/tmp");
        taskExecutionContext.setLogPath("/log");
        taskExecutionContext.setTaskJson(
                "{\"conditionResult\":\"{\\\"successNode\\\":[\\\"\\\"],\\\"failedNode\\\":[\\\"\\\"]}\","
                        + "\"conditionsTask\":false,\"depList\":[],\"dependence\":\"{}\",\"forbidden\":false,\"id\":\""
                        + "tasks-16849\",\"maxRetryTimes\":0,\"name\":\"shell test 001\","
                        + "\"params\":\"{\\\"rawScript\\\":\\\"#!/bin/sh\\\\necho $[yyyy-MM-dd HH:mm:ss +3]\\\\necho \\\\\\\" ?? "
                        + "${time1} \\\\\\\"\\\\necho \\\\\\\" ????? ${time2}\\\\\\\"\\\\n\\\","
                        + "\\\"localParams\\\":[{\\\"prop\\\":\\\"time1\\\",\\\"direct\\\":\\\"OUT\\\",\\\"type\\\":"
                        + "\\\"VARCHAR\\\",\\\"value\\\":\\\"$[yyyy-MM-dd HH:mm:ss]\\\"},"
                        + "{\\\"prop\\\":\\\"time2\\\",\\\"direct\\\":\\\"IN\\\",\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"${time_gb}\\\"}"
                        + "],\\\"resourceList\\\":[]}\",\"preTasks\":\"[]\",\"retryInterval\":1,\"runFlag\":\"NORMAL\",\"taskInstancePriority\":\"MEDIUM\",\"taskTimeoutParameter\":"
                        + "{\"enable\":false,\"interval\":0},\"timeout\":\"{\\\"enable\\\":false,\\\"strategy\\\":\\\"\\\"}\",\"type\":\"SHELL\",\"workerGroup\":\"default\"}");
        taskExecutionContext.setProcessInstanceId(1);
        taskExecutionContext.setGlobalParams("[{\"direct\":\"IN\",\"prop\":\"time_gb\",\"type\":\"VARCHAR\",\"value\":\"2020-12-16 17:18:33\"}]");
        taskExecutionContext.setExecutorId(1);
        taskExecutionContext.setCmdTypeIfComplement(5);
        taskExecutionContext.setTenantCode("roo");
        taskExecutionContext.setScheduleTime(new Date());
        taskExecutionContext.setQueue("default");
        taskExecutionContext.setTaskParams(
                "{\"rawScript\":\"#!/bin/sh\\necho $[yyyy-MM-dd HH:mm:ss +3]\\necho \\\" ?? ${time1} \\\"\\necho \\\" ????? ${time2}\\\"\\n\",\"localParams\":"
                        +
                        "[{\"prop\":\"time1\",\"direct\":\"OUT\",\"type\":\"VARCHAR\",\"value\":\"$[yyyy-MM-dd HH:mm:ss]\"},{\"prop\":\"time2\",\"direct\":\"IN\",\"type\":\"VARCHAR"
                        + "\",\"value\":\"${time_gb}\"}],\"resourceList\":[]}");
        Map<String, String> definedParams = new HashMap<>();
        definedParams.put("time_gb", "2020-12-16 00:00:00");
        taskExecutionContext.setDefinedParams(definedParams);
        ShellTask shellTask = (ShellTask) TaskManager.newTask(taskExecutionContext, taskLogger, alertClientService);
    }

    @Test
    public void testSqlTaskReturnString() {
        String params = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:mysql://127.0.0.1:3306\","
                + "\"database\":\"test\",\"jdbcUrl\":\"jdbc:mysql://127.0.0.1:3306/test\"}";
        taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams("{\"localParams\":[{\"prop\":\"ret\", \"direct\":\"OUT\", \"type\":\"VARCHAR\", \"value\":\"\"}],"
                + "\"type\":\"POSTGRESQL\",\"datasource\":1,\"sql\":\"insert into tb_1 values('1','2')\","
                + "\"sqlType\":1}");
        taskExecutionContext.setExecutePath("/tmp");
        taskExecutionContext.setTaskAppId("1");
        taskExecutionContext.setTenantCode("root");
        taskExecutionContext.setStartTime(new Date());
        taskExecutionContext.setTaskTimeout(10000);
        taskExecutionContext.setLogPath("/tmp/dx");

        SQLTaskExecutionContext sqlTaskExecutionContext = new SQLTaskExecutionContext();
        sqlTaskExecutionContext.setConnectionParams(params);
        taskExecutionContext.setSqlTaskExecutionContext(sqlTaskExecutionContext);
        SqlTask sqlTask = new SqlTask(taskExecutionContext, logger, null);
        SqlParameters sqlParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), SqlParameters.class);
        List<Property> properties = sqlParameters.getLocalParams();
        sqlTask.setNonQuerySqlReturn("sql return", properties);
    }
}
