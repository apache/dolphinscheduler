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

package org.apache.dolphinscheduler.server.worker.task.procedure;

import org.apache.dolphinscheduler.server.entity.ProcedureTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

@RunWith(PowerMockRunner.class)
public class ProcedureTaskTest {
    private static final Logger logger = LoggerFactory.getLogger(ProcedureTaskTest.class);

    private static final String CONNECTION_PARAMS = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:mysql://127.0.0.1:3306\",\"database\":\"test\",\"jdbcUrl\":\"jdbc:mysql://127.0.0.1:3306/test\"}";

    private ProcedureTask procedureTask;

    private ProcessService processService;

    private ApplicationContext applicationContext;

    private TaskExecutionContext taskExecutionContext;

    @Before
    public void before() throws Exception {
        taskExecutionContext = new TaskExecutionContext();
        processService = PowerMockito.mock(ProcessService.class);
        applicationContext = PowerMockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        PowerMockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        TaskProps props = new TaskProps();
        props.setExecutePath("/tmp");
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTaskInstanceId(1);
        props.setTenantCode("1");
        props.setEnvFile(".dolphinscheduler_env.sh");
        props.setTaskStartTime(new Date());
        props.setTaskTimeout(0);
        props.setTaskParams(
                "{\"localParams\":[],\"type\":\"MYSQL\",\"datasource\":1,\"method\":\"add\"}");

        taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(props.getTaskParams());
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        Mockito.when(taskExecutionContext.getTaskAppId()).thenReturn("1");
        Mockito.when(taskExecutionContext.getTenantCode()).thenReturn("root");
        Mockito.when(taskExecutionContext.getStartTime()).thenReturn(new Date());
        Mockito.when(taskExecutionContext.getTaskTimeout()).thenReturn(10000);
        Mockito.when(taskExecutionContext.getLogPath()).thenReturn("/tmp/dx");

        ProcedureTaskExecutionContext procedureTaskExecutionContext = new ProcedureTaskExecutionContext();
        procedureTaskExecutionContext.setConnectionParams(CONNECTION_PARAMS);
        Mockito.when(taskExecutionContext.getProcedureTaskExecutionContext()).thenReturn(procedureTaskExecutionContext);

        procedureTask = new ProcedureTask(taskExecutionContext, logger);
        procedureTask.init();
    }

    @Test
    public void testGetParameters() {
        Assert.assertNotNull(procedureTask.getParameters());
    }

    @Test
    public void testHandle() {

    }

}
