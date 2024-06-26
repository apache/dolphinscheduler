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

import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.runner.operator.TaskExecuteRunnableOperatorManager;

import org.apache.commons.lang3.time.DateUtils;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GlobalTaskDispatchWaitingQueueTest {

    private GlobalTaskDispatchWaitingQueue globalTaskDispatchWaitingQueue;

    @BeforeEach
    public void setUp() {
        globalTaskDispatchWaitingQueue = new GlobalTaskDispatchWaitingQueue();
    }

    @Test
    void submitTaskExecuteRunnable() {
        TaskExecuteRunnable taskExecuteRunnable = createTaskExecuteRunnable();
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnable(taskExecuteRunnable);
        Awaitility.await()
                .atMost(Duration.ofSeconds(1))
                .untilAsserted(
                        () -> Assertions.assertNotNull(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable()));
    }

    @Test
    void testSubmitTaskExecuteRunnableWithDelay() {
        TaskExecuteRunnable taskExecuteRunnable = createTaskExecuteRunnable();
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnableWithDelay(taskExecuteRunnable, 3_000L);
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnable(taskExecuteRunnable);

        assertThat(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable()).isNotNull();
        Awaitility.await()
                .atLeast(Duration.ofSeconds(2))
                .atMost(Duration.ofSeconds(4))
                .untilAsserted(
                        () -> Assertions.assertNotNull(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable()));
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
    void takeTaskExecuteRunnable_withDifferentTaskInstancePriority() {
        TaskExecuteRunnable taskExecuteRunnable1 = createTaskExecuteRunnable();
        taskExecuteRunnable1.getTaskInstance().setId(1);
        taskExecuteRunnable1.getTaskInstance().setTaskInstancePriority(Priority.MEDIUM);
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnable(taskExecuteRunnable1);

        TaskExecuteRunnable taskExecuteRunnable2 = createTaskExecuteRunnable();
        taskExecuteRunnable2.getTaskInstance().setId(2);
        taskExecuteRunnable2.getTaskInstance().setTaskInstancePriority(Priority.HIGH);
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnable(taskExecuteRunnable2);

        TaskExecuteRunnable taskExecuteRunnable3 = createTaskExecuteRunnable();
        taskExecuteRunnable3.getTaskInstance().setId(3);
        taskExecuteRunnable3.getTaskInstance().setTaskInstancePriority(Priority.LOW);
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnable(taskExecuteRunnable3);

        assertThat(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable().getTaskInstance().getId())
                .isEqualTo(2);
        assertThat(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable().getTaskInstance().getId())
                .isEqualTo(1);
        assertThat(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable().getTaskInstance().getId())
                .isEqualTo(3);
    }

    @Test
    void takeTaskExecuteRunnable_withDifferentTaskGroupPriority() {
        TaskExecuteRunnable taskExecuteRunnable1 = createTaskExecuteRunnable();
        taskExecuteRunnable1.getTaskInstance().setId(1);
        taskExecuteRunnable1.getTaskInstance().setTaskGroupPriority(Priority.MEDIUM.getCode());
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnable(taskExecuteRunnable1);

        TaskExecuteRunnable taskExecuteRunnable2 = createTaskExecuteRunnable();
        taskExecuteRunnable2.getTaskInstance().setId(2);
        taskExecuteRunnable2.getTaskInstance().setTaskGroupPriority(Priority.HIGH.getCode());
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnable(taskExecuteRunnable2);

        TaskExecuteRunnable taskExecuteRunnable3 = createTaskExecuteRunnable();
        taskExecuteRunnable3.getTaskInstance().setId(3);
        taskExecuteRunnable3.getTaskInstance().setTaskGroupPriority(Priority.LOW.getCode());
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnable(taskExecuteRunnable3);

        assertThat(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable().getTaskInstance().getId())
                .isEqualTo(3);
        assertThat(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable().getTaskInstance().getId())
                .isEqualTo(1);
        assertThat(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable().getTaskInstance().getId())
                .isEqualTo(2);
    }

    @Test
    void takeTaskExecuteRunnable_withDifferentSubmitTime() {
        Date now = new Date();

        TaskExecuteRunnable taskExecuteRunnable1 = createTaskExecuteRunnable();
        taskExecuteRunnable1.getTaskInstance().setId(1);
        taskExecuteRunnable1.getTaskInstance().setFirstSubmitTime(now);
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnable(taskExecuteRunnable1);

        TaskExecuteRunnable taskExecuteRunnable2 = createTaskExecuteRunnable();
        taskExecuteRunnable2.getTaskInstance().setId(2);
        taskExecuteRunnable2.getTaskInstance().setFirstSubmitTime(DateUtils.addMinutes(now, 1));
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnable(taskExecuteRunnable2);

        TaskExecuteRunnable taskExecuteRunnable3 = createTaskExecuteRunnable();
        taskExecuteRunnable3.getTaskInstance().setId(3);
        taskExecuteRunnable3.getTaskInstance().setFirstSubmitTime(DateUtils.addMinutes(now, -1));
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnable(taskExecuteRunnable3);

        assertThat(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable().getTaskInstance().getId())
                .isEqualTo(3);
        assertThat(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable().getTaskInstance().getId())
                .isEqualTo(1);
        assertThat(globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable().getTaskInstance().getId())
                .isEqualTo(2);
    }

    @Test
    void getWaitingDispatchTaskNumber() {
        Assertions.assertEquals(0, globalTaskDispatchWaitingQueue.getWaitingDispatchTaskNumber());
        TaskExecuteRunnable taskExecuteRunnable = createTaskExecuteRunnable();
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnable(taskExecuteRunnable);
        Assertions.assertEquals(1, globalTaskDispatchWaitingQueue.getWaitingDispatchTaskNumber());
    }

    private TaskExecuteRunnable createTaskExecuteRunnable() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setProcessInstancePriority(Priority.MEDIUM);

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskInstancePriority(Priority.MEDIUM);
        taskInstance.setFirstSubmitTime(new Date());

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();

        return new DefaultTaskExecuteRunnable(processInstance, taskInstance, taskExecutionContext,
                new TaskExecuteRunnableOperatorManager());
    }
}
