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

package org.apache.dolphinscheduler.server.worker.runner;

import static org.powermock.api.mockito.PowerMockito.mock;

import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.cache.impl.TaskExecutionContextCacheManagerImpl;
import org.apache.dolphinscheduler.server.worker.processor.TaskCallbackService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TaskExecuteThread.class})
public class TaskExecuteThreadTest {


    TaskCallbackService taskCallbackService;

    ApplicationContext applicationContext;

    TaskExecutionContextCacheManagerImpl taskExecutionContextCacheManager;

    private ProcessService processService;

    @Before
    public void init() throws Exception {

        taskCallbackService = PowerMockito.mock(TaskCallbackService.class);
        applicationContext = PowerMockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        taskExecutionContextCacheManager = new TaskExecutionContextCacheManagerImpl();
        Mockito.when(applicationContext.getBean(TaskExecutionContextCacheManagerImpl.class)).thenReturn(taskExecutionContextCacheManager);
    }

    @Test
    public void testTaskClearExecPath() throws Exception {
        processService = mock(ProcessService.class);

        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        TaskCallbackService taskCallbackService = Mockito.mock(TaskCallbackService.class);
        TaskExecuteThread taskExecuteThread = PowerMockito.spy(new TaskExecuteThread(taskExecutionContext, taskCallbackService));
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/");

    }

    @Test
    public void testClearTaskExecPath() {

        String taskJson = "{\n"
                + "  \"conditionResult\": {\n"
                + "    \"failedNode\": [\n"
                + "      \"\"\n"
                + "    ],\n"
                + "    \"successNode\": [\n"
                + "      \"\"\n"
                + "    ]\n"
                + "  },\n"
                + "  \"delayTime\": 0,\n"
                + "  \"depList\": [\n"
                + "    \"testlog1\"\n"
                + "  ],\n"
                + "  \"dependence\": {},\n"
                + "  \"desc\": null,\n"
                + "  \"extras\": null,\n"
                + "  \"id\": \"tasks-31779\",\n"
                + "  \"loc\": null,\n"
                + "  \"maxRetryTimes\": 0,\n"
                + "  \"name\": \"testlog2\",\n"
                + "  \"params\": {\n"
                + "    \"localParams\": [],\n"
                + "    \"rawScript\": \"echo \\\"123123\\\"\",\n"
                + "    \"resourceList\": []\n"
                + "  },\n"
                + "  \"preTasks\": [\n"
                + "    \"testlog1\"\n"
                + "  ],\n"
                + "  \"retryInterval\": 1,\n"
                + "  \"runFlag\": \"NORMAL\",\n"
                + "  \"taskInstancePriority\": \"MEDIUM\",\n"
                + "  \"timeout\": {\n"
                + "    \"enable\": false,\n"
                + "    \"interval\": null,\n"
                + "    \"strategy\": \"\"\n"
                + "  },\n"
                + "  \"type\": \"SHELL\",\n"
                + "  \"workerGroup\": \"default\",\n"
                + "  \"workerGroupId\": null\n"
                + "}";
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskJson(taskJson);
        TaskExecuteThread taskExecuteThread = new TaskExecuteThread(taskExecutionContext, taskCallbackService);

        try {
            taskExecuteThread.run();
        } catch (Exception ignore) {
            //ignore
        }
        Mockito.when(CommonUtils.isDevelopMode()).thenReturn(false);

        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn(null);
        try {
            taskExecuteThread.run();
        } catch (Exception ignore) {
            //ignore
        }
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/");
        try {
            taskExecuteThread.run();
        } catch (Exception ignore) {
            //ignore
        }
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/data/test-testClearTaskExecPath");
        try {
            taskExecuteThread.run();
        } catch (Exception ignore) {
            //ignore
        }

    }

    @Test
    public void testNotClearTaskExecPath() {
        String taskJson = "{\n"
                + "  \"conditionResult\": {\n"
                + "    \"failedNode\": [\n"
                + "      \"\"\n"
                + "    ],\n"
                + "    \"successNode\": [\n"
                + "      \"\"\n"
                + "    ]\n"
                + "  },\n"
                + "  \"delayTime\": 0,\n"
                + "  \"depList\": [\n"
                + "    \"testlog1\"\n"
                + "  ],\n"
                + "  \"dependence\": {},\n"
                + "  \"desc\": null,\n"
                + "  \"extras\": null,\n"
                + "  \"id\": \"tasks-31779\",\n"
                + "  \"loc\": null,\n"
                + "  \"maxRetryTimes\": 0,\n"
                + "  \"name\": \"testlog2\",\n"
                + "  \"params\": {\n"
                + "    \"localParams\": [],\n"
                + "    \"rawScript\": \"echo \\\"123123\\\"\",\n"
                + "    \"resourceList\": []\n"
                + "  },\n"
                + "  \"preTasks\": [\n"
                + "    \"testlog1\"\n"
                + "  ],\n"
                + "  \"retryInterval\": 1,\n"
                + "  \"runFlag\": \"NORMAL\",\n"
                + "  \"taskInstancePriority\": \"MEDIUM\",\n"
                + "  \"timeout\": {\n"
                + "    \"enable\": false,\n"
                + "    \"interval\": null,\n"
                + "    \"strategy\": \"\"\n"
                + "  },\n"
                + "  \"type\": \"SHELL\",\n"
                + "  \"workerGroup\": \"default\",\n"
                + "  \"workerGroupId\": null\n"
                + "}";
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskJson(taskJson);
        TaskExecuteThread taskExecuteThread = new TaskExecuteThread(taskExecutionContext, taskCallbackService);
        Mockito.when(CommonUtils.isDevelopMode()).thenReturn(true);
        try {
            taskExecuteThread.run();
        } catch (Exception ignore) {
            //ignore
        }
    }
}
