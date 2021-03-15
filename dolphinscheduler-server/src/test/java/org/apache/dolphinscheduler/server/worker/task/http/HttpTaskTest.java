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

import java.io.IOException;
import java.util.Date;

import org.apache.dolphinscheduler.common.enums.HttpCheckCondition;
import org.apache.dolphinscheduler.common.enums.HttpMethod;
import org.apache.dolphinscheduler.common.task.http.HttpParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.ShellCommandExecutor;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
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


@RunWith(PowerMockRunner.class)
@PrepareForTest(OSUtils.class)
@PowerMockIgnore({"javax.management.*","javax.net.ssl.*"})
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
                "{\"localParams\":[],\"httpParams\":[],\"url\":\"https://github.com/\",\"httpMethod\":\"GET\"," +
                        "\"httpCheckCondition\":\"STATUS_CODE_DEFAULT\",\"condition\":\"https://github.com/\"," +
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
        String paramJson = "{\"localParams\":[],\"httpParams\":[],\"url\":\"https://github.com/\"," +
                "\"httpMethod\":\"GET\",\"httpCheckCondition\":\"STATUS_CODE_DEFAULT\",\"condition\":\"\",\"connectTimeout\":\"10000\",\"socketTimeout\":\"10000\"}";
        HttpParameters httpParameters = JSONUtils.parseObject(paramJson, HttpParameters.class);


        Assert.assertEquals(10000,httpParameters.getConnectTimeout() );
        Assert.assertEquals(10000,httpParameters.getSocketTimeout());
        Assert.assertEquals("https://github.com/",httpParameters.getUrl());
        Assert.assertEquals(HttpMethod.GET,httpParameters.getHttpMethod());
        Assert.assertEquals(HttpCheckCondition.STATUS_CODE_DEFAULT,httpParameters.getHttpCheckCondition());
        Assert.assertEquals("",httpParameters.getCondition());

    }

    @Test
    public void testHandle(){
        boolean flag = true ;
        try {
            httpTask.handle();
        } catch (Exception e) {
            flag = false ;
            e.printStackTrace();
        }

        Assert.assertTrue(flag);

    }

    @Test
    public void testSendRequest(){

        CloseableHttpClient client = httpTask.createHttpClient();

        String statusCode = null;
        String body = null;

        try {

            CloseableHttpResponse response = httpTask.sendRequest(client) ;
            statusCode = String.valueOf(httpTask.getStatusCode(response));
            body = httpTask.getResponseBody(response);
            int exitStatusCode = httpTask.validResponse(body, statusCode);

            Assert.assertNotEquals(-1,exitStatusCode);

        } catch (IOException e) {
            e.printStackTrace();
        };
    }

    @Test
    public void testValidResponse(){
        String body = "body";
        String statusCode = "200" ;

        int exitStatusCode = httpTask.validResponse(body,statusCode);
        Assert.assertNotEquals(-1,exitStatusCode);

    }

    @Test
    public void testAppendMessage(){
        httpTask.appendMessage("message");

        Assert.assertEquals("message",httpTask.getOutput());
    }

    @Test
    public void testCreateHttpClient(){
        Assert.assertNotNull(httpTask.createHttpClient());
    }

    @Test
    public void testCreateRequestBuilder(){
        RequestBuilder  requestBuilder = httpTask.createRequestBuilder();
        Assert.assertEquals(RequestBuilder.get().getMethod(),requestBuilder.getMethod());
    }

    private ProcessInstance getProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setCommandType(START_PROCESS);
        processInstance.setScheduleTime(new Date());
        return processInstance;
    }
}
