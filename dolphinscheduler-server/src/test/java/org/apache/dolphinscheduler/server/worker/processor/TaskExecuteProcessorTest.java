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

package org.apache.dolphinscheduler.server.worker.processor;

import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandType;
import org.apache.dolphinscheduler.remote.command.TaskExecuteAckCommand;
import org.apache.dolphinscheduler.remote.command.TaskExecuteRequestCommand;
import org.apache.dolphinscheduler.remote.utils.ChannelUtils;
import org.apache.dolphinscheduler.remote.utils.JsonSerializer;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.runner.TaskExecuteThread;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import java.util.Date;
import java.util.concurrent.ExecutorService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * test task execute processor
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SpringApplicationContext.class, TaskCallbackService.class, WorkerConfig.class, FileUtils.class,
        JsonSerializer.class, JSONUtils.class, ThreadUtils.class, ExecutorService.class, ChannelUtils.class})
@Ignore
public class TaskExecuteProcessorTest {

    private TaskExecutionContext taskExecutionContext;

    private TaskCallbackService taskCallbackService;

    private ExecutorService workerExecService;

    private WorkerConfig workerConfig;

    private Command command;

    private Command ackCommand;

    private TaskExecuteRequestCommand taskRequestCommand;

    private AlertClientService alertClientService;

    private WorkerManagerThread workerManager;

    @Test
    public void testNormalExecution() {
        TaskExecuteProcessor processor = new TaskExecuteProcessor();
        processor.process(null, command);

        Assert.assertEquals(ExecutionStatus.RUNNING_EXECUTION, taskExecutionContext.getCurrentExecutionStatus());
    }

    @Test
    public void testDelayExecution() {
        taskExecutionContext.setDelayTime(1);
        TaskExecuteProcessor processor = new TaskExecuteProcessor();
        processor.process(null, command);

        Assert.assertEquals(ExecutionStatus.DELAY_EXECUTION, taskExecutionContext.getCurrentExecutionStatus());
    }

    public TaskExecutionContext getTaskExecutionContext() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setProcessId(12345);
        taskExecutionContext.setProcessInstanceId(1);
        taskExecutionContext.setTaskInstanceId(1);
        taskExecutionContext.setProcessDefineCode(1L);
        taskExecutionContext.setProcessDefineVersion(1);
        taskExecutionContext.setTaskType(TaskType.SQL.getDesc());
        taskExecutionContext.setFirstSubmitTime(new Date());
        taskExecutionContext.setDelayTime(0);
        taskExecutionContext.setLogPath("/tmp/test.log");
        taskExecutionContext.setHost("localhost");
        taskExecutionContext.setExecutePath("/tmp/dolphinscheduler/exec/process/1/2/3/4");
        return taskExecutionContext;
    }

    private static class SimpleTaskExecuteThread extends TaskExecuteThread {

        public SimpleTaskExecuteThread(TaskExecutionContext taskExecutionContext, TaskCallbackService taskCallbackService, Logger taskLogger, AlertClientService alertClientService) {
            super(taskExecutionContext, taskCallbackService, alertClientService);
        }

        @Override
        public void run() {
            //
        }
    }
}
