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
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.runner.TaskExecuteThread;
import org.apache.dolphinscheduler.server.worker.runner.WorkerManagerThread;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.queue.entity.TaskExecutionContext;

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

    @Before
    public void before() throws Exception {
        // init task execution context
        taskExecutionContext = getTaskExecutionContext();
        workerConfig = new WorkerConfig();
        workerConfig.setWorkerExecThreads(1);
        workerConfig.setListenPort(1234);
        command = new Command();
        command.setType(CommandType.TASK_EXECUTE_REQUEST);
        ackCommand = new TaskExecuteAckCommand().convert2Command();
        taskRequestCommand = new TaskExecuteRequestCommand();
        alertClientService = PowerMockito.mock(AlertClientService.class);
        workerExecService = PowerMockito.mock(ExecutorService.class);
        PowerMockito.when(workerExecService.submit(Mockito.any(TaskExecuteThread.class)))
                .thenReturn(null);

        PowerMockito.mockStatic(ChannelUtils.class);
        PowerMockito.when(ChannelUtils.toAddress(null)).thenReturn(null);

        taskCallbackService = PowerMockito.mock(TaskCallbackService.class);
        PowerMockito.doNothing().when(taskCallbackService).sendAck(taskExecutionContext.getTaskInstanceId(), ackCommand);

        PowerMockito.mockStatic(SpringApplicationContext.class);
        PowerMockito.when(SpringApplicationContext.getBean(TaskCallbackService.class))
                .thenReturn(taskCallbackService);
        PowerMockito.when(SpringApplicationContext.getBean(WorkerConfig.class))
                .thenReturn(workerConfig);

        Logger taskLogger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                taskExecutionContext.getProcessDefineCode(),
                taskExecutionContext.getProcessDefineVersion(),
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId()));

        workerManager = PowerMockito.mock(WorkerManagerThread.class);
        PowerMockito.when(workerManager.offer(new TaskExecuteThread(taskExecutionContext, taskCallbackService, alertClientService))).thenReturn(Boolean.TRUE);

        PowerMockito.when(SpringApplicationContext.getBean(WorkerManagerThread.class))
                .thenReturn(workerManager);

        PowerMockito.mockStatic(ThreadUtils.class);
        PowerMockito.when(ThreadUtils.newDaemonFixedThreadExecutor("Worker-Execute-Thread", workerConfig.getWorkerExecThreads()))
                .thenReturn(workerExecService);

        PowerMockito.mockStatic(JsonSerializer.class);
        PowerMockito.when(JsonSerializer.deserialize(command.getBody(), TaskExecuteRequestCommand.class))
                .thenReturn(taskRequestCommand);

        PowerMockito.mockStatic(JSONUtils.class);
        PowerMockito.when(JSONUtils.parseObject(command.getBody(), TaskExecuteRequestCommand.class))
                .thenReturn(taskRequestCommand);
        PowerMockito.when(JSONUtils.parseObject(taskRequestCommand.getTaskExecutionContext(), TaskExecutionContext.class))
                .thenReturn(taskExecutionContext);

        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.when(FileUtils.getProcessExecDir(taskExecutionContext.getProjectCode(),
                taskExecutionContext.getProcessDefineCode(),
                taskExecutionContext.getProcessDefineVersion(),
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId()))
                .thenReturn(taskExecutionContext.getExecutePath());
        PowerMockito.doNothing().when(FileUtils.class, "createWorkDirIfAbsent", taskExecutionContext.getExecutePath());

        SimpleTaskExecuteThread simpleTaskExecuteThread = new SimpleTaskExecuteThread(null, null, null, alertClientService);
        PowerMockito.whenNew(TaskExecuteThread.class).withAnyArguments()
                .thenReturn(simpleTaskExecuteThread);
    }

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
