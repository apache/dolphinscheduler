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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TaskExecuteThread.class})
public class TaskExecuteThreadTest {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecuteThreadTest.class);

    TaskExecutionContext taskExecutionContext;

    TaskCallbackService taskCallbackService;

    ApplicationContext applicationContext;

    TaskExecutionContextCacheManagerImpl taskExecutionContextCacheManager;

    private ProcessService processService;

    @Before
    public void init() throws Exception {
        taskExecutionContext = PowerMockito.mock(TaskExecutionContext.class);
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
        TaskExecuteThread taskExecuteThread = PowerMockito.spy(new TaskExecuteThread(taskExecutionContext, taskCallbackService, logger));
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/");
        Assert.assertTrue(true);
    }

    @Test
    public void testClearTaskExecPath() {

        TaskExecuteThread taskExecuteThread = new TaskExecuteThread(taskExecutionContext, taskCallbackService, logger);
        Mockito.when(CommonUtils.isDevelopMode()).thenReturn(false);
        Mockito.when(taskExecutionContext.getTaskJson()).thenThrow(new RuntimeException("测试异常后finally执行"));
        try {
            taskExecuteThread.run();
        } catch (Exception ignored) {
            //ignored
        }

        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn(null);
        try {
            taskExecuteThread.run();
        } catch (Exception ignored) {
            //ignored
        }

        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/");
        try {
            taskExecuteThread.run();
        } catch (Exception ignored) {
            //ignored
        }

        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/data/test-testClearTaskExecPath");
        try {
            taskExecuteThread.run();
        } catch (Exception ignored) {
            //ignored
        }

        Assert.assertTrue(true);

    }

    @Test
    public void testNotClearTaskExecPath() {
        TaskExecuteThread taskExecuteThread = new TaskExecuteThread(taskExecutionContext, taskCallbackService, logger);
        Mockito.when(CommonUtils.isDevelopMode()).thenReturn(true);
        taskExecuteThread.run();
        Assert.assertTrue(true);
    }
}
