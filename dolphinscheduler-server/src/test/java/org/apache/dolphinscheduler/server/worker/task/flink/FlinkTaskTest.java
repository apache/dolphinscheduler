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
package org.apache.dolphinscheduler.server.worker.task.flink;

import org.apache.dolphinscheduler.common.task.flink.FlinkParameters;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Date;

import org.junit.Assert;
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
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
public class FlinkTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(FlinkTaskTest.class);

    @Test
    public void testFlinkTaskInitWithQueue() {

        String param = "{\"mainClass\":\"com.test.main\",\"mainJar\":{\"id\":1},\"deployMode\":\"cluster\",\"resourceList\":[],\"localParams\":[],\"flinkVersion\":\"<1.10\",\"slot\":1,\"taskManager\":\"2\",\"jobManagerMemory\":\"1G\",\"taskManagerMemory\":\"2G\",\"mainArgs\":\"\",\"others\":\"\",\"programType\":\"SCALA\",\"queue\":\"queueA\"}";

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();

        PowerMockito.mockStatic(OSUtils.class);
        ProcessService processService = PowerMockito.mock(ProcessService.class);

        Resource resource = PowerMockito.mock(Resource.class);
        Mockito.when(resource.getFullName()).thenReturn("/");
        Mockito.when(processService.getResourceById(1)).thenReturn(resource);


        ApplicationContext applicationContext = PowerMockito.mock(ApplicationContext.class);
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
        props.setTaskParams(param);


        taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(props.getTaskParams());
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        Mockito.when(taskExecutionContext.getTaskAppId()).thenReturn("1");
        Mockito.when(taskExecutionContext.getTenantCode()).thenReturn("root");
        Mockito.when(taskExecutionContext.getStartTime()).thenReturn(new Date());
        Mockito.when(taskExecutionContext.getTaskTimeout()).thenReturn(10000);
        Mockito.when(taskExecutionContext.getLogPath()).thenReturn("/tmp/dx");
        Mockito.when(taskExecutionContext.getQueue()).thenReturn("testQueue");
        FlinkTask flinkTask = new FlinkTask(taskExecutionContext, logger);


        flinkTask.init();
        FlinkParameters flinkParameters = (FlinkParameters) flinkTask.getParameters();
        Assert.assertEquals("queueA", flinkParameters.getQueue());

    }

    @Test
    public void testFlinkTaskInitWithNoQueue() {

        String param = "{\"mainClass\":\"com.test.main\",\"mainJar\":{\"id\":1},\"deployMode\":\"cluster\",\"resourceList\":[],\"localParams\":[],\"flinkVersion\":\"<1.10\",\"slot\":1,\"taskManager\":\"2\",\"jobManagerMemory\":\"1G\",\"taskManagerMemory\":\"2G\",\"mainArgs\":\"\",\"others\":\"\",\"programType\":\"SCALA\"}";

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();

        PowerMockito.mockStatic(OSUtils.class);
        ProcessService processService = PowerMockito.mock(ProcessService.class);

        Resource resource = PowerMockito.mock(Resource.class);
        Mockito.when(resource.getFullName()).thenReturn("/");
        Mockito.when(processService.getResourceById(1)).thenReturn(resource);


        ApplicationContext applicationContext = PowerMockito.mock(ApplicationContext.class);
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
        props.setTaskParams(param);


        taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(props.getTaskParams());
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        Mockito.when(taskExecutionContext.getTaskAppId()).thenReturn("1");
        Mockito.when(taskExecutionContext.getTenantCode()).thenReturn("root");
        Mockito.when(taskExecutionContext.getStartTime()).thenReturn(new Date());
        Mockito.when(taskExecutionContext.getTaskTimeout()).thenReturn(10000);
        Mockito.when(taskExecutionContext.getLogPath()).thenReturn("/tmp/dx");
        Mockito.when(taskExecutionContext.getQueue()).thenReturn("testQueue");
        FlinkTask flinkTask = new FlinkTask(taskExecutionContext, logger);

        flinkTask.init();
        FlinkParameters flinkParameters = (FlinkParameters) flinkTask.getParameters();
        Assert.assertEquals("testQueue", flinkParameters.getQueue());

    }

}
