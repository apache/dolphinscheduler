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

package org.apache.dolphinscheduler.task.executor;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.task.executor.container.ITaskExecutorContainerProvider;
import org.apache.dolphinscheduler.task.executor.eventbus.TaskExecutorEventBus;
import org.apache.dolphinscheduler.task.executor.eventbus.TaskExecutorEventBusCoordinator;
import org.apache.dolphinscheduler.task.executor.listener.ITaskExecutorLifecycleEventListener;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TaskEngineTest {

    private static final int TASK_INSTANCE_ID = 16789;

    @Test
    public void testKillTaskReportsTheFailureOfTheCancellation() {
        // the kill runs on the calling thread: a task which could not be cancelled is not reported as
        // stopped, the caller, for example the API which stopped the task, gets the failure
        ITaskExecutor taskExecutor = buildTaskExecutor();
        TaskExecutorEventBusCoordinator eventBusCoordinator = buildEventBusCoordinator(taskExecutor);
        ITaskExecutorLifecycleEventListener listener = Mockito.mock(ITaskExecutorLifecycleEventListener.class);
        Mockito.doThrow(new TaskException("Failed to cancel the flink jobs")).when(listener)
                .onTaskExecutorKillLifecycleEvent(Mockito.any());
        eventBusCoordinator.registerTaskExecutorLifecycleEventListener(listener);
        TaskEngine taskEngine = buildTaskEngine(taskExecutor, eventBusCoordinator);

        TaskException exception =
                Assertions.assertThrows(TaskException.class, () -> taskEngine.killTask(TASK_INSTANCE_ID));

        Assertions.assertEquals("Failed to cancel the flink jobs", exception.getMessage());
        assertNoQueuedKillEvent(taskExecutor);
    }

    @Test
    public void testKillTaskReturnsWhenTheTaskIsKilled() {
        ITaskExecutor taskExecutor = buildTaskExecutor();
        TaskExecutorEventBusCoordinator eventBusCoordinator = buildEventBusCoordinator(taskExecutor);
        ITaskExecutorLifecycleEventListener listener = Mockito.mock(ITaskExecutorLifecycleEventListener.class);
        eventBusCoordinator.registerTaskExecutorLifecycleEventListener(listener);
        TaskEngine taskEngine = buildTaskEngine(taskExecutor, eventBusCoordinator);

        taskEngine.killTask(TASK_INSTANCE_ID);

        Mockito.verify(listener).onTaskExecutorKillLifecycleEvent(Mockito.any());
        // the event is not queued, so the queued dispatch cannot kill the task a second time
        assertNoQueuedKillEvent(taskExecutor);
    }

    private TaskEngine buildTaskEngine(ITaskExecutor taskExecutor,
                                       TaskExecutorEventBusCoordinator eventBusCoordinator) {
        ITaskExecutorRepository taskExecutorRepository = Mockito.mock(ITaskExecutorRepository.class);
        Mockito.when(taskExecutorRepository.get(TASK_INSTANCE_ID)).thenReturn(Optional.of(taskExecutor));
        return new TaskEngine(TaskEngineBuilder.builder()
                .engineName("test-task-engine")
                .taskExecutorRepository(taskExecutorRepository)
                .taskExecutorContainerDelegator(Mockito.mock(ITaskExecutorContainerProvider.class))
                .taskExecutorEventBusCoordinator(eventBusCoordinator)
                .build());
    }

    private TaskExecutorEventBusCoordinator buildEventBusCoordinator(ITaskExecutor taskExecutor) {
        ITaskExecutorRepository taskExecutorRepository = Mockito.mock(ITaskExecutorRepository.class);
        Mockito.when(taskExecutorRepository.get(TASK_INSTANCE_ID)).thenReturn(Optional.of(taskExecutor));
        return new TaskExecutorEventBusCoordinator("test-task-engine", taskExecutorRepository);
    }

    private void assertNoQueuedKillEvent(ITaskExecutor taskExecutor) {
        Assertions.assertTrue(taskExecutor.getTaskExecutorEventBus().isEmpty(),
                "the kill event must not be queued as well");
    }

    private ITaskExecutor buildTaskExecutor() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskInstanceId(TASK_INSTANCE_ID);
        taskExecutionContext.setTaskName("test-flink-task");

        ITaskExecutor taskExecutor = Mockito.mock(ITaskExecutor.class);
        Mockito.when(taskExecutor.getId()).thenReturn(TASK_INSTANCE_ID);
        Mockito.when(taskExecutor.getTaskExecutionContext()).thenReturn(taskExecutionContext);
        Mockito.when(taskExecutor.getTaskExecutorEventBus()).thenReturn(new TaskExecutorEventBus());
        return taskExecutor;
    }
}
