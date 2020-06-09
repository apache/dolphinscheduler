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

package org.apache.dolphinscheduler.server.worker.task.http;

import static org.apache.dolphinscheduler.common.enums.CommandType.*;

import java.util.Date;

import org.apache.dolphinscheduler.common.task.http.HttpParameters;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.ShellCommandExecutor;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.alibaba.fastjson.JSON;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OSUtils.class)
@PowerMockIgnore({"javax.management.*"})
public class HttpTaskTest {
    private static final Logger logger = LoggerFactory.getLogger(HttpTaskTest.class);



    private HttpTask httpTask;

    private ProcessService processService;

    private ShellCommandExecutor shellCommandExecutor;

    private ApplicationContext applicationContext;
    private TaskExecutionContext taskExecutionContext;

    @Before
    public void before() throws Exception {
        taskExecutionContext = new TaskExecutionContext();

        PowerMockito.mockStatic(OSUtils.class);
        processService = PowerMockito.mock(ProcessService.class);
        shellCommandExecutor = PowerMockito.mock(ShellCommandExecutor.class);

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
                "{\"localParams\":[],\"httpParams\":[],\"url\":\"https://www.baidu.com/\",\"httpMethod\":\"GET\"," +
                        "\"httpCheckCondition\":\"STATUS_CODE_DEFAULT\",\"condition\":\"https://www.baidu.com/\"," +
                        "\"connectTimeout\":\"1000\",\"socketTimeout\":\"1000\"}");


        taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(props.getTaskParams());
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        Mockito.when(taskExecutionContext.getTaskAppId()).thenReturn("1");
        Mockito.when(taskExecutionContext.getTenantCode()).thenReturn("root");
        Mockito.when(taskExecutionContext.getStartTime()).thenReturn(new Date());
        Mockito.when(taskExecutionContext.getTaskTimeout()).thenReturn(10000);
        Mockito.when(taskExecutionContext.getLogPath()).thenReturn("/tmp/dx");

        httpTask = new HttpTask(taskExecutionContext, logger);
        httpTask.init();

    }

    @Test
    public void testGetParameters() {
        Assert.assertNotNull(httpTask.getParameters());
    }


    @Test
    public void testCheckParameters() {
        Assert.assertTrue(httpTask.getParameters().checkParameters());
    }


    @Test
    public void testGenerator(){
        String data1 = "{\"localParams\":[],\"httpParams\":[],\"url\":\"https://www.baidu.com/\"," +
                "\"httpMethod\":\"GET\",\"httpCheckCondition\":\"STATUS_CODE_DEFAULT\",\"condition\":\"http://www" +
                ".baidu.com/\",\"connectTimeout\":\"10000\",\"socketTimeout\":\"10000\"}";
        HttpParameters httpParameters = JSON.parseObject(data1, HttpParameters.class);


        Assert.assertEquals(httpParameters.getConnectTimeout(), 10000);
        Assert.assertEquals(httpParameters.getSocketTimeout(), 10000);
        Assert.assertNotNull(httpParameters.getUrl());
    }

    private ProcessInstance getProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setCommandType(START_PROCESS);
        processInstance.setScheduleTime(new Date());
        return processInstance;
    }
}
