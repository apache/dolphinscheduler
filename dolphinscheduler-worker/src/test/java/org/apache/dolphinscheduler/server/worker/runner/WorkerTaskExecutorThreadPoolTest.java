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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.config.TaskExecuteThreadsFullPolicy;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistryClient;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;

import org.apache.commons.lang3.RandomUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.common.truth.Truth;

class WorkerTaskExecutorThreadPoolTest {

    private WorkerMessageSender workerMessageSender = Mockito.mock(WorkerMessageSender.class);

    private WorkerTaskExecutorFactoryBuilder workerTaskExecutorFactoryBuilder =
            Mockito.mock(WorkerTaskExecutorFactoryBuilder.class);

    private WorkerTaskExecutorFactory<MockWorkerTaskExecutor> defaultWorkerTaskExecutorFactory =
            Mockito.mock(WorkerTaskExecutorFactory.class);

    @BeforeEach
    public void setUp() {
        WorkerTaskExecutorHolder.clear();
    }

    @Test
    public void testIsOverload() {
        final int execThreadCount = RandomUtils.nextInt(1, 100);
        final int totalTaskCount = RandomUtils.nextInt(1, 10000);
        final WorkerConfig workerConfig = createWorkerConfig(execThreadCount, TaskExecuteThreadsFullPolicy.CONTINUE);
        final WorkerTaskExecutorThreadPool workerTaskExecutorThreadPool =
                new WorkerTaskExecutorThreadPool(workerConfig);

        Mockito.reset(workerTaskExecutorFactoryBuilder);
        Mockito.doReturn(defaultWorkerTaskExecutorFactory).when(workerTaskExecutorFactoryBuilder)
                .createWorkerTaskExecutorFactory(any());

        final TaskCoordinator taskCoordinator = new TaskCoordinator(
                workerConfig,
                workerTaskExecutorThreadPool,
                workerTaskExecutorFactoryBuilder,
                workerMessageSender);

        // submit totalTaskCount task, the thread pool size is execThreadCount, reject policy is CONTINUE
        // after submit execThreadCount task, the thread pool is overload
        for (int i = 1; i <= totalTaskCount; i++) {
            Mockito.reset(defaultWorkerTaskExecutorFactory);
            TaskExecutionContext taskExecutionContext = TaskExecutionContext.builder().taskInstanceId(i).build();
            MockWorkerTaskExecutor mockWorkerTaskExecutor =
                    new MockWorkerTaskExecutor(() -> ThreadUtils.sleep(10_000L), taskExecutionContext);
            given(defaultWorkerTaskExecutorFactory.createWorkerTaskExecutor()).willReturn(mockWorkerTaskExecutor);
            taskCoordinator.register(taskExecutionContext);
            if (i >= execThreadCount) {
                Truth.assertThat(workerTaskExecutorThreadPool.isOverload()).isTrue();
            } else {
                Truth.assertThat(workerTaskExecutorThreadPool.isOverload()).isFalse();
            }
        }
    }

    @Test
    public void testSubmitWorkerTaskExecutorWithContinuePolicy() {
        final int execThreadCount = RandomUtils.nextInt(1, 100);
        final int totalTaskCount = RandomUtils.nextInt(1, 10000);
        final WorkerConfig workerConfig = createWorkerConfig(execThreadCount, TaskExecuteThreadsFullPolicy.CONTINUE);
        final WorkerTaskExecutorThreadPool workerTaskExecutorThreadPool =
                new WorkerTaskExecutorThreadPool(workerConfig);
        // submit totalTaskCount task, the thread pool size is execThreadCount, reject policy is CONTINUE
        // all task will be submitted success
        for (int i = 1; i <= totalTaskCount; i++) {
            TaskExecutionContext taskExecutionContext =
                    TaskExecutionContext.builder().taskInstanceId((int) System.nanoTime()).build();
            MockWorkerTaskExecutor mockWorkerTaskExecutor =
                    new MockWorkerTaskExecutor(() -> ThreadUtils.sleep(10_000L), taskExecutionContext);
            Truth.assertThat(workerTaskExecutorThreadPool.submitWorkerTaskExecutor(mockWorkerTaskExecutor)).isTrue();
        }
    }

    @Test
    public void testSubmitWorkerTaskExecutorWithRejectPolicy() {
        final int execThreadCount = RandomUtils.nextInt(1, 100);
        final int totalTaskCount = RandomUtils.nextInt(1, 10000);
        final WorkerConfig workerConfig = createWorkerConfig(execThreadCount, TaskExecuteThreadsFullPolicy.REJECT);
        final WorkerTaskExecutorThreadPool workerTaskExecutorThreadPool =
                new WorkerTaskExecutorThreadPool(workerConfig);

        Mockito.reset(workerTaskExecutorFactoryBuilder);
        Mockito.doReturn(defaultWorkerTaskExecutorFactory).when(workerTaskExecutorFactoryBuilder)
                .createWorkerTaskExecutorFactory(any());

        final TaskCoordinator taskCoordinator = new TaskCoordinator(
                workerConfig,
                workerTaskExecutorThreadPool,
                workerTaskExecutorFactoryBuilder,
                workerMessageSender);

        // submit totalTaskCount task, the thread pool size is execThreadCount, reject policy is REJECT
        // only the front execThreadCount task will be submitted success
        for (int i = 1; i <= totalTaskCount; i++) {
            Mockito.reset(defaultWorkerTaskExecutorFactory);
            TaskExecutionContext taskExecutionContext = TaskExecutionContext.builder().taskInstanceId(i).build();
            MockWorkerTaskExecutor mockWorkerTaskExecutor =
                    new MockWorkerTaskExecutor(() -> ThreadUtils.sleep(10_000L), taskExecutionContext);
            given(defaultWorkerTaskExecutorFactory.createWorkerTaskExecutor()).willReturn(mockWorkerTaskExecutor);

            boolean registerResult = taskCoordinator.register(taskExecutionContext);
            if (i <= execThreadCount) {
                Assertions.assertTrue(registerResult, "The " + i + " task should submit success");
            } else {
                Assertions.assertFalse(registerResult, "The " + i + " task should submit failed");
            }
        }
    }

    @Test
    public void testGetWaitingTaskExecutorSize() {
        final int execThreadCount = RandomUtils.nextInt(1, 100);
        final int totalTaskCount = RandomUtils.nextInt(1, 10000);
        final WorkerConfig workerConfig = createWorkerConfig(execThreadCount, TaskExecuteThreadsFullPolicy.CONTINUE);
        final WorkerTaskExecutorThreadPool workerTaskExecutorThreadPool =
                new WorkerTaskExecutorThreadPool(workerConfig);

        Mockito.reset(workerTaskExecutorFactoryBuilder);
        Mockito.doReturn(defaultWorkerTaskExecutorFactory).when(workerTaskExecutorFactoryBuilder)
                .createWorkerTaskExecutorFactory(any());

        final TaskCoordinator taskCoordinator = new TaskCoordinator(
                workerConfig,
                workerTaskExecutorThreadPool,
                workerTaskExecutorFactoryBuilder,
                workerMessageSender);

        Truth.assertThat(workerTaskExecutorThreadPool.getWaitingTaskExecutorSize()).isEqualTo(0);
        for (int i = 1; i <= totalTaskCount; i++) {
            Mockito.reset(defaultWorkerTaskExecutorFactory);
            TaskExecutionContext taskExecutionContext = TaskExecutionContext.builder().taskInstanceId(i).build();
            MockWorkerTaskExecutor mockWorkerTaskExecutor =
                    new MockWorkerTaskExecutor(() -> ThreadUtils.sleep(10_000L), taskExecutionContext);
            given(defaultWorkerTaskExecutorFactory.createWorkerTaskExecutor()).willReturn(mockWorkerTaskExecutor);

            taskCoordinator.register(taskExecutionContext);
            if (i <= execThreadCount) {
                Truth.assertThat(workerTaskExecutorThreadPool.getWaitingTaskExecutorSize()).isEqualTo(0);
            } else {
                Truth.assertThat(workerTaskExecutorThreadPool.getWaitingTaskExecutorSize())
                        .isEqualTo(i - execThreadCount);
            }
        }
    }

    @Test
    public void testGetRunningTaskExecutorSize() {
        final int execThreadCount = RandomUtils.nextInt(1, 100);
        final int totalTaskCount = RandomUtils.nextInt(1, 10000);
        WorkerConfig workerConfig = createWorkerConfig(execThreadCount, TaskExecuteThreadsFullPolicy.CONTINUE);
        WorkerTaskExecutorThreadPool workerTaskExecutorThreadPool =
                new WorkerTaskExecutorThreadPool(workerConfig);

        Mockito.reset(workerTaskExecutorFactoryBuilder);
        Mockito.doReturn(defaultWorkerTaskExecutorFactory).when(workerTaskExecutorFactoryBuilder)
                .createWorkerTaskExecutorFactory(any());

        final TaskCoordinator taskCoordinator = new TaskCoordinator(
                workerConfig,
                workerTaskExecutorThreadPool,
                workerTaskExecutorFactoryBuilder,
                workerMessageSender);

        Truth.assertThat(workerTaskExecutorThreadPool.getRunningTaskExecutorSize()).isEqualTo(0);
        for (int i = 1; i <= totalTaskCount; i++) {
            Mockito.reset(defaultWorkerTaskExecutorFactory);
            TaskExecutionContext taskExecutionContext = TaskExecutionContext.builder().taskInstanceId(i).build();
            MockWorkerTaskExecutor mockWorkerTaskExecutor =
                    new MockWorkerTaskExecutor(() -> ThreadUtils.sleep(10_000L), taskExecutionContext);
            given(defaultWorkerTaskExecutorFactory.createWorkerTaskExecutor()).willReturn(mockWorkerTaskExecutor);

            taskCoordinator.register(taskExecutionContext);
            if (i <= execThreadCount) {
                Truth.assertThat(workerTaskExecutorThreadPool.getRunningTaskExecutorSize()).isEqualTo(i);
            } else {
                Truth.assertThat(workerTaskExecutorThreadPool.getRunningTaskExecutorSize()).isEqualTo(execThreadCount);
            }
        }
    }

    static class MockWorkerTaskExecutor extends WorkerTaskExecutor {

        private final Runnable runnable;

        protected MockWorkerTaskExecutor(Runnable runnable, TaskExecutionContext taskExecutionContext) {
            super(taskExecutionContext,
                    new WorkerConfig(),
                    new WorkerMessageSender(),
                    null,
                    new WorkerRegistryClient());
            this.runnable = runnable;
        }

        @Override
        public void run() {
            executeTask(new TaskCallbackImpl(null, null));
        }

        @Override
        protected void executeTask(TaskCallBack taskCallBack) {
            runnable.run();
        }
    }

    private WorkerConfig createWorkerConfig(int execThreads,
                                            TaskExecuteThreadsFullPolicy taskExecuteThreadsFullPolicy) {
        WorkerConfig workerConfig = new WorkerConfig();
        workerConfig.setExecThreads(execThreads);
        workerConfig.setTaskExecuteThreadsFullPolicy(taskExecuteThreadsFullPolicy);
        return workerConfig;
    }

}
