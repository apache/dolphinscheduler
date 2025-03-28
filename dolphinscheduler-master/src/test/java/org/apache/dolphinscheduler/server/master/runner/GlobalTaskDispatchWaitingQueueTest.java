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

package org.apache.dolphinscheduler.server.master.runner;

import static com.google.common.truth.Truth.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.WorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnableBuilder;

import org.apache.commons.lang3.RandomUtils;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class GlobalTaskDispatchWaitingQueueTest {

    @InjectMocks
    private GlobalTaskDispatchWaitingQueue globalTaskDispatchWaitingQueue;

    @Test
    void submitTaskExecuteRunnable() {
        ITaskExecutionRunnable iTaskExecutionRunnable = createTaskExecuteRunnable();
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnableWithDelay(iTaskExecutionRunnable, 500);
        Awaitility.await()
                .atMost(Duration.ofSeconds(1))
                .untilAsserted(
                        () -> Assertions.assertNotNull(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable()));
    }

    @Test
    void testSubmitTaskExecuteRunnableWithDelay() {
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnableWithDelay(createTaskExecuteRunnable(), 3_000L);
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnableWithDelay(createTaskExecuteRunnable(), 500);

        assertThat(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable()).isNotNull();
        assertThat(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable()).isNotNull();
    }

    @Test
    void takeTaskExecuteRunnable_NoElementShouldBlock() {
        CompletableFuture<Void> completableFuture =
                CompletableFuture.runAsync(() -> globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable());
        assertThrowsExactly(ConditionTimeoutException.class,
                () -> await()
                        .atLeast(Duration.ofSeconds(2))
                        .timeout(Duration.ofSeconds(3))
                        .until(completableFuture::isDone));
    }

    @Test
    void takeTaskExecuteRunnable_withDifferentTaskInstanceDelay() {
        ITaskExecutionRunnable taskExecutionRunnable1 = createTaskExecuteRunnable();
        taskExecutionRunnable1.getTaskInstance().setId(1);
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnableWithDelay(taskExecutionRunnable1, 0);

        ITaskExecutionRunnable iTaskExecutionRunnable2 = createTaskExecuteRunnable();
        iTaskExecutionRunnable2.getTaskInstance().setId(2);
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnableWithDelay(iTaskExecutionRunnable2, 1);

        ITaskExecutionRunnable iTaskExecutionRunnable3 = createTaskExecuteRunnable();
        iTaskExecutionRunnable3.getTaskInstance().setId(3);
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnableWithDelay(iTaskExecutionRunnable3, 2);

        assertThat(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable().getTaskInstance().getId())
                .isEqualTo(1);
        assertThat(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable().getTaskInstance().getId())
                .isEqualTo(2);
        assertThat(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable().getTaskInstance().getId())
                .isEqualTo(3);
    }

    @Test
    void getWaitingDispatchTaskNumber() {
        Assertions.assertEquals(0, globalTaskDispatchWaitingQueue.getWaitingDispatchTaskNumber());
        ITaskExecutionRunnable iTaskExecutionRunnable = createTaskExecuteRunnable();
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnableWithDelay(iTaskExecutionRunnable, 500);
        Assertions.assertEquals(1, globalTaskDispatchWaitingQueue.getWaitingDispatchTaskNumber());
    }

    private ITaskExecutionRunnable createTaskExecuteRunnable() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setWorkflowInstancePriority(Priority.MEDIUM);

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(RandomUtils.nextInt());
        taskInstance.setTaskInstancePriority(Priority.MEDIUM);
        taskInstance.setFirstSubmitTime(new Date());

        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(TaskExecutionContextFactory.class))
                .thenReturn(mock(TaskExecutionContextFactory.class));
        final TaskExecutionRunnableBuilder taskExecutionRunnableBuilder = TaskExecutionRunnableBuilder.builder()
                .applicationContext(applicationContext)
                .workflowInstance(workflowInstance)
                .taskInstance(taskInstance)
                .workflowExecutionGraph(new WorkflowExecutionGraph())
                .workflowDefinition(new WorkflowDefinition())
                .project(new Project())
                .taskDefinition(new TaskDefinition())
                .workflowEventBus(new WorkflowEventBus())
                .build();
        return new TaskExecutionRunnable(taskExecutionRunnableBuilder);
    }
}
