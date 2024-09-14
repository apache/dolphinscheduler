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

package org.apache.dolphinscheduler.server.master.runner.execute;

import static java.time.Duration.ofSeconds;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AsyncMasterTaskDelayQueueLooperTest {

    @InjectMocks
    private AsyncMasterTaskDelayQueueLooper asyncMasterTaskDelayQueueLooper;
    @Mock
    private AsyncMasterTaskDelayQueue asyncMasterTaskDelayQueue;
    @Mock
    private MasterAsyncTaskExecutorThreadPool masterAsyncTaskExecutorThreadPool;

    private final int workflowInstanceId = 1;
    private final int taskInstanceId = 2;
    private final String logPath = "logs";
    private static final ThreadPoolExecutor threadPoolExecutor = ThreadUtils.newDaemonFixedThreadExecutor(
            "MasterAsyncTaskExecutorThreadPool", 2);

    @Test
    void testTaskExecutionContextIsNotDryRun() throws Exception {
        final TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setWorkflowInstanceId(workflowInstanceId);
        taskExecutionContext.setTaskInstanceId(taskInstanceId);
        taskExecutionContext.setLogPath(logPath);
        taskExecutionContext.setDryRun(Constants.DRY_RUN_FLAG_NO);
        MasterTaskExecutionContextHolder.putTaskExecutionContext(taskExecutionContext);
        AsyncTaskExecuteFunction asyncTaskExecuteFunction = mock(AsyncTaskExecuteFunction.class);
        AsyncTaskCallbackFunction asyncTaskCallbackFunction = mock(AsyncTaskCallbackFunction.class);

        final AsyncTaskExecutionContext asyncTaskExecutionContext = new AsyncTaskExecutionContext(
                taskExecutionContext,
                asyncTaskExecuteFunction,
                asyncTaskCallbackFunction);
        when(asyncTaskExecuteFunction.getAsyncTaskExecutionStatus())
                .thenReturn(AsyncTaskExecuteFunction.AsyncTaskExecutionStatus.FAILED);
        when(asyncMasterTaskDelayQueue.pollAsyncTask()).thenReturn(asyncTaskExecutionContext);
        when(masterAsyncTaskExecutorThreadPool.getThreadPool()).thenReturn(threadPoolExecutor);

        asyncMasterTaskDelayQueueLooper.start();

        await().during(ofSeconds(1)).untilAsserted(() -> {
            verify(masterAsyncTaskExecutorThreadPool, atLeastOnce()).getThreadPool();
            verify(asyncTaskCallbackFunction, atLeastOnce()).executeFailed();
        });
        asyncMasterTaskDelayQueueLooper.close();
    }

    @Test
    void testTaskExecutionContextIsDryRun() throws Exception {
        final TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setWorkflowInstanceId(workflowInstanceId);
        taskExecutionContext.setTaskInstanceId(taskInstanceId);
        taskExecutionContext.setLogPath(logPath);
        taskExecutionContext.setDryRun(Constants.DRY_RUN_FLAG_YES);
        MasterTaskExecutionContextHolder.putTaskExecutionContext(taskExecutionContext);
        AsyncTaskExecuteFunction asyncTaskExecuteFunction = mock(AsyncTaskExecuteFunction.class);
        AsyncTaskCallbackFunction asyncTaskCallbackFunction = mock(AsyncTaskCallbackFunction.class);

        final AsyncTaskExecutionContext asyncTaskExecutionContext = new AsyncTaskExecutionContext(
                taskExecutionContext,
                asyncTaskExecuteFunction,
                asyncTaskCallbackFunction);
        when(asyncTaskExecuteFunction.getAsyncTaskExecutionStatus())
                .thenReturn(AsyncTaskExecuteFunction.AsyncTaskExecutionStatus.RUNNING);
        when(asyncMasterTaskDelayQueue.pollAsyncTask()).thenReturn(asyncTaskExecutionContext);
        when(masterAsyncTaskExecutorThreadPool.getThreadPool()).thenReturn(threadPoolExecutor);

        asyncMasterTaskDelayQueueLooper.start();

        await().during(ofSeconds(1)).untilAsserted(() -> {
            verify(masterAsyncTaskExecutorThreadPool, atLeastOnce()).getThreadPool();
            verify(asyncTaskCallbackFunction, atLeastOnce()).executeSuccess();
        });
        asyncMasterTaskDelayQueueLooper.close();
    }

    @AfterAll
    public static void cleanup() {
        threadPoolExecutor.shutdown();
    }
}
