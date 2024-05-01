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

package org.apache.dolphinscheduler.server.worker.runner.operator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceDispatchRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceDispatchResponse;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceKillRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstanceKillResponse;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstancePauseRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.TaskInstancePauseResponse;
import org.apache.dolphinscheduler.extract.worker.transportor.UpdateWorkflowHostRequest;
import org.apache.dolphinscheduler.extract.worker.transportor.UpdateWorkflowHostResponse;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.message.MessageRetryRunner;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistryClient;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecutor;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecutorFactoryBuilder;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecutorHolder;
import org.apache.dolphinscheduler.server.worker.runner.WorkerTaskExecutorThreadPool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskInstanceOperationFunctionTest {

    private static final Logger log = LoggerFactory.getLogger(TaskInstanceOperationFunctionTest.class);
    private MessageRetryRunner messageRetryRunner = Mockito.mock(MessageRetryRunner.class);

    private WorkerConfig workerConfig = Mockito.mock(WorkerConfig.class);

    private TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);

    private WorkerTaskExecutorThreadPool workerTaskExecutorThreadPool =
            Mockito.mock(WorkerTaskExecutorThreadPool.class);

    private WorkerTaskExecutor workerTaskExecutor = Mockito.mock(WorkerTaskExecutor.class);

    private AbstractTask task = Mockito.mock(AbstractTask.class);

    private WorkerMessageSender workerMessageSender = Mockito.mock(WorkerMessageSender.class);

    private WorkerTaskExecutorThreadPool workerManager = Mockito.mock(WorkerTaskExecutorThreadPool.class);

    private StorageOperate storageOperate = Mockito.mock(StorageOperate.class);

    private WorkerRegistryClient workerRegistryClient = Mockito.mock(WorkerRegistryClient.class);

    @Test
    public void testTaskInstanceOperationFunctionManager() {
        TaskInstanceKillOperationFunction taskInstanceKillOperationFunction = new TaskInstanceKillOperationFunction(
                workerTaskExecutorThreadPool,
                messageRetryRunner);

        TaskInstancePauseOperationFunction taskInstancePauseOperationFunction =
                new TaskInstancePauseOperationFunction();

        UpdateWorkflowHostOperationFunction updateWorkflowHostOperationFunction =
                new UpdateWorkflowHostOperationFunction(
                        messageRetryRunner);

        WorkerTaskExecutorFactoryBuilder workerTaskExecutorFactoryBuilder = new WorkerTaskExecutorFactoryBuilder(
                workerConfig,
                workerMessageSender,
                workerManager,
                storageOperate,
                workerRegistryClient);

        TaskInstanceDispatchOperationFunction taskInstanceDispatchOperationFunction =
                new TaskInstanceDispatchOperationFunction(
                        workerConfig,
                        workerTaskExecutorFactoryBuilder,
                        workerTaskExecutorThreadPool);

        TaskInstanceOperationFunctionManager taskInstanceOperationFunctionManager =
                new TaskInstanceOperationFunctionManager(
                        taskInstanceKillOperationFunction,
                        updateWorkflowHostOperationFunction,
                        taskInstanceDispatchOperationFunction,
                        taskInstancePauseOperationFunction);

        Assertions.assertEquals(taskInstanceKillOperationFunction,
                taskInstanceOperationFunctionManager.getTaskInstanceKillOperationFunction());
        Assertions.assertEquals(taskInstancePauseOperationFunction,
                taskInstanceOperationFunctionManager.getTaskInstancePauseOperationFunction());
        Assertions.assertEquals(updateWorkflowHostOperationFunction,
                taskInstanceOperationFunctionManager.getUpdateWorkflowHostOperationFunction());
        Assertions.assertEquals(taskInstanceDispatchOperationFunction,
                taskInstanceOperationFunctionManager.getTaskInstanceDispatchOperationFunction());
    }

    @Test
    public void testUpdateWorkflowHostOperationFunction() {
        UpdateWorkflowHostOperationFunction updateWorkflowHostOperationFunction =
                new UpdateWorkflowHostOperationFunction(
                        messageRetryRunner);

        try (MockedStatic<LogUtils> logUtilsMockedStatic = Mockito.mockStatic(LogUtils.class)) {
            logUtilsMockedStatic
                    .when(() -> LogUtils
                            .setTaskInstanceIdMDC(any(Integer.class)))
                    .then(invocationOnMock -> null);
            UpdateWorkflowHostRequest request = new UpdateWorkflowHostRequest();
            request.setTaskInstanceId(1);
            request.setWorkflowHost("host");
            UpdateWorkflowHostResponse taskInstanceDispatchResponse = updateWorkflowHostOperationFunction.operate(
                    request);
            Assertions.assertEquals(taskInstanceDispatchResponse.isSuccess(), false);
        }

        try (MockedStatic<LogUtils> logUtilsMockedStatic = Mockito.mockStatic(LogUtils.class)) {
            logUtilsMockedStatic
                    .when(() -> LogUtils
                            .setTaskInstanceIdMDC(any(Integer.class)))
                    .then(invocationOnMock -> null);

            try (
                    MockedStatic<WorkerTaskExecutorHolder> workerTaskExecutorHolderMockedStatic =
                            Mockito.mockStatic(WorkerTaskExecutorHolder.class)) {
                given(workerTaskExecutor.getTaskExecutionContext()).willReturn(taskExecutionContext);
                workerTaskExecutorHolderMockedStatic
                        .when(() -> WorkerTaskExecutorHolder.get(any(Integer.class)))
                        .thenReturn(workerTaskExecutor);
                int taskInstanceId = 111;
                UpdateWorkflowHostRequest request = new UpdateWorkflowHostRequest();
                request.setTaskInstanceId(taskInstanceId);
                request.setWorkflowHost("host");

                UpdateWorkflowHostResponse taskInstanceDispatchResponse = updateWorkflowHostOperationFunction.operate(
                        request);
                Assertions.assertEquals(taskInstanceDispatchResponse.isSuccess(), true);
            }
        }
    }

    @Test
    public void testTaskInstancePauseOperationFunction() {
        TaskInstancePauseOperationFunction taskInstancePauseOperationFunction =
                new TaskInstancePauseOperationFunction();

        try (MockedStatic<LogUtils> logUtilsMockedStatic = Mockito.mockStatic(LogUtils.class)) {
            logUtilsMockedStatic
                    .when(() -> LogUtils
                            .setTaskInstanceIdMDC(any(Integer.class)))
                    .then(invocationOnMock -> null);
            TaskInstancePauseRequest request = new TaskInstancePauseRequest();
            request.setTaskInstanceId(1);
            TaskInstancePauseResponse taskInstanceDispatchResponse = taskInstancePauseOperationFunction.operate(
                    request);
            Assertions.assertEquals(taskInstanceDispatchResponse.isSuccess(), true);
        }
    }

    @Test
    public void testTaskInstanceDispatchOperationFunction() {
        WorkerTaskExecutorFactoryBuilder workerTaskExecutorFactoryBuilder = new WorkerTaskExecutorFactoryBuilder(
                workerConfig,
                workerMessageSender,
                workerManager,
                storageOperate,
                workerRegistryClient);

        TaskInstanceDispatchOperationFunction taskInstanceDispatchOperationFunction =
                new TaskInstanceDispatchOperationFunction(
                        workerConfig,
                        workerTaskExecutorFactoryBuilder,
                        workerTaskExecutorThreadPool);

        try (MockedStatic<LogUtils> logUtilsMockedStatic = Mockito.mockStatic(LogUtils.class)) {
            logUtilsMockedStatic
                    .when(() -> LogUtils
                            .getTaskInstanceLogFullPath(any(TaskExecutionContext.class)))
                    .thenReturn("test");
            TaskInstanceDispatchResponse taskInstanceDispatchResponse = taskInstanceDispatchOperationFunction.operate(
                    new TaskInstanceDispatchRequest(taskExecutionContext));
            Assertions.assertEquals(taskInstanceDispatchResponse.isDispatchSuccess(), false);
            logUtilsMockedStatic.verify(times(1), () -> LogUtils.removeWorkflowAndTaskInstanceIdMDC());

            given(workerTaskExecutorThreadPool.submitWorkerTaskExecutor(any())).willReturn(true);
            taskInstanceDispatchResponse = taskInstanceDispatchOperationFunction.operate(
                    new TaskInstanceDispatchRequest(taskExecutionContext));
            Assertions.assertEquals(taskInstanceDispatchResponse.isDispatchSuccess(), true);
            logUtilsMockedStatic.verify(times(2), () -> LogUtils.removeWorkflowAndTaskInstanceIdMDC());
        }
    }

    @Test
    public void testTaskInstanceKillOperationFunction() {
        TaskInstanceKillOperationFunction taskInstanceKillOperationFunction = new TaskInstanceKillOperationFunction(
                workerManager,
                messageRetryRunner);

        try (MockedStatic<LogUtils> logUtilsMockedStatic = Mockito.mockStatic(LogUtils.class)) {
            int taskInstanceId = 111;
            logUtilsMockedStatic
                    .when(() -> LogUtils
                            .setTaskInstanceLogFullPathMDC(any(String.class)))
                    .then(invocationOnMock -> null);
            TaskInstanceKillResponse response = taskInstanceKillOperationFunction.operate(
                    new TaskInstanceKillRequest(taskInstanceId));
            Assertions.assertEquals("Cannot find WorkerTaskExecutor", response.getMessage());
        }

        try (MockedStatic<LogUtils> logUtilsMockedStatic = Mockito.mockStatic(LogUtils.class)) {
            int processId = 12;
            int taskInstanceId = 111;
            Mockito.reset(taskExecutionContext);
            given(taskExecutionContext.getProcessId()).willReturn(processId);
            given(taskExecutionContext.getLogPath()).willReturn("logpath");
            logUtilsMockedStatic
                    .when(() -> LogUtils
                            .setTaskInstanceLogFullPathMDC(any(String.class)))
                    .then(invocationOnMock -> null);
            taskInstanceKillOperationFunction.operate(
                    new TaskInstanceKillRequest(taskInstanceId));
            logUtilsMockedStatic.verify(times(1), () -> LogUtils.removeTaskInstanceIdMDC());
            logUtilsMockedStatic.verify(times(1), () -> LogUtils.removeTaskInstanceLogFullPathMDC());
        }

        try (MockedStatic<LogUtils> logUtilsMockedStatic = Mockito.mockStatic(LogUtils.class)) {
            try (
                    MockedStatic<WorkerTaskExecutorHolder> workerTaskExecutorHolderMockedStatic =
                            Mockito.mockStatic(WorkerTaskExecutorHolder.class)) {
                given(workerTaskExecutor.getTaskExecutionContext()).willReturn(taskExecutionContext);
                workerTaskExecutorHolderMockedStatic
                        .when(() -> WorkerTaskExecutorHolder.get(any(Integer.class)))
                        .thenReturn(workerTaskExecutor);
                int processId = 12;
                int taskInstanceId = 111;
                Mockito.reset(taskExecutionContext);
                given(taskExecutionContext.getProcessId()).willReturn(processId);
                given(taskExecutionContext.getLogPath()).willReturn("logpath");
                logUtilsMockedStatic
                        .when(() -> LogUtils
                                .setTaskInstanceLogFullPathMDC(any(String.class)))
                        .then(invocationOnMock -> null);
                when(workerTaskExecutor.getTask()).thenReturn(task);
                // given(workerManager.getTaskExecuteThread(taskInstanceId)).willReturn(workerTaskExecutor);
                taskInstanceKillOperationFunction.operate(
                        new TaskInstanceKillRequest(taskInstanceId));
                verify(task, times(1)).cancel();
            }

        }
    }
}
