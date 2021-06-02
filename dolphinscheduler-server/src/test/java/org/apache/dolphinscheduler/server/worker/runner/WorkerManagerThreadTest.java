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

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.TaskExecuteAckCommand;
import org.apache.dolphinscheduler.remote.command.TaskExecuteResponseCommand;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.cache.impl.TaskExecutionContextCacheManagerImpl;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.processor.TaskCallbackService;
import org.apache.dolphinscheduler.server.worker.task.AbstractTask;
import org.apache.dolphinscheduler.server.worker.task.TaskManager;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * test worker manager thread.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({
        Stopper.class,
        TaskManager.class,
        JSONUtils.class,
        CommonUtils.class,
        SpringApplicationContext.class,
        OSUtils.class})
public class WorkerManagerThreadTest {

    private TaskCallbackService taskCallbackService;

    private WorkerManagerThread workerManager;

    private TaskExecutionContext taskExecutionContext;

    private AlertClientService alertClientService;

    private Logger taskLogger;

    @Before
    public void before() {
        // init task execution context, logger
        taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setProcessId(12345);
        taskExecutionContext.setProcessInstanceId(1);
        taskExecutionContext.setTaskInstanceId(1);
        taskExecutionContext.setProcessDefineCode(1L);
        taskExecutionContext.setProcessDefineVersion(1);
        taskExecutionContext.setTenantCode("test");
        taskExecutionContext.setTaskType(TaskType.SHELL.getDesc());
        taskExecutionContext.setFirstSubmitTime(new Date());
        taskExecutionContext.setDelayTime(0);
        taskExecutionContext.setLogPath("/tmp/test.log");
        taskExecutionContext.setHost("localhost");
        taskExecutionContext.setExecutePath("/tmp/dolphinscheduler/exec/process/1/2/3/4");

        Command ackCommand = new TaskExecuteAckCommand().convert2Command();
        Command responseCommand = new TaskExecuteResponseCommand(taskExecutionContext.getTaskInstanceId()).convert2Command();

        taskLogger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(
                LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                taskExecutionContext.getProcessDefineCode(),
                taskExecutionContext.getProcessDefineVersion(),
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId()
        ));

        TaskExecutionContextCacheManagerImpl taskExecutionContextCacheManager = new TaskExecutionContextCacheManagerImpl();
        taskExecutionContextCacheManager.cacheTaskExecutionContext(taskExecutionContext);

        alertClientService = PowerMockito.mock(AlertClientService.class);
        WorkerConfig workerConfig = PowerMockito.mock(WorkerConfig.class);
        taskCallbackService = PowerMockito.mock(TaskCallbackService.class);
        PowerMockito.doNothing().when(taskCallbackService).sendAck(taskExecutionContext.getTaskInstanceId(), ackCommand);
        PowerMockito.doNothing().when(taskCallbackService).sendResult(taskExecutionContext.getTaskInstanceId(), responseCommand);
        PowerMockito.mockStatic(SpringApplicationContext.class);
        PowerMockito.when(SpringApplicationContext.getBean(TaskExecutionContextCacheManagerImpl.class))
                .thenReturn(taskExecutionContextCacheManager);
        PowerMockito.when(SpringApplicationContext.getBean(WorkerConfig.class))
                .thenReturn(workerConfig);
        PowerMockito.when(SpringApplicationContext.getBean(TaskCallbackService.class))
                .thenReturn(taskCallbackService);
        PowerMockito.when(workerConfig.getWorkerExecThreads()).thenReturn(5);
        workerManager = new WorkerManagerThread();

        PowerMockito.mockStatic(TaskManager.class);
        PowerMockito.when(TaskManager.newTask(taskExecutionContext, taskLogger, alertClientService))
                .thenReturn(new SimpleTask(taskExecutionContext, taskLogger));
        PowerMockito.mockStatic(JSONUtils.class);
        PowerMockito.when(JSONUtils.parseObject(taskExecutionContext.getTaskJson(), TaskNode.class))
                .thenReturn(new TaskNode());
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.getSystemEnvPath()).thenReturn("/user_home/.bash_profile");
        List<String> osUserList = Collections.singletonList("test");
        PowerMockito.mockStatic(OSUtils.class);
        PowerMockito.when(OSUtils.getUserList()).thenReturn(osUserList);
        PowerMockito.mockStatic(Stopper.class);
        PowerMockito.when(Stopper.isRunning()).thenReturn(true, false);
    }

    @Test
    public void testSendTaskKillResponse() {
        TaskExecuteThread taskExecuteThread = new TaskExecuteThread(taskExecutionContext, taskCallbackService, taskLogger, alertClientService);
        workerManager.offer(taskExecuteThread);
        Assert.assertEquals(1, workerManager.getQueueSize());
        workerManager.killTaskBeforeExecuteByInstanceId(1);
        Assert.assertEquals(0, workerManager.getQueueSize());
    }

    @Test
    public void testRun() {
        TaskExecuteThread taskExecuteThread = new TaskExecuteThread(taskExecutionContext, taskCallbackService, taskLogger, alertClientService);
        workerManager.offer(taskExecuteThread);
        Assert.assertEquals(1, workerManager.getQueueSize());
        workerManager.run();
        Assert.assertEquals(0, workerManager.getQueueSize());
    }

    private static class SimpleTask extends AbstractTask {

        protected SimpleTask(TaskExecutionContext taskExecutionContext, Logger logger) {
            super(taskExecutionContext, logger);
            // pid
            this.processId = taskExecutionContext.getProcessId();
        }

        @Override
        public AbstractParameters getParameters() {
            return null;
        }

        @Override
        public void init() {

        }

        @Override
        public void handle() {

        }

        @Override
        public void after() {

        }

        @Override
        public ExecutionStatus getExitStatus() {
            return ExecutionStatus.SUCCESS;
        }
    }
}
