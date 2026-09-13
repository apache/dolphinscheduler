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

package org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskRetryLifecycleEventTest {

    private static final int RETRY_INTERVAL_MINUTES = 5;

    private static final int MAX_RETRY_TIMES = 3;

    @Mock
    private ITaskExecution taskExecution;

    @Test
    @DisplayName("Test the retry is delayed until (endTime + retryInterval) when the interval has not elapsed")
    void testOf_retryIntervalNotElapsed_delayUntilRetryIntervalIsOver() {
        final long elapsedTime = TimeUnit.SECONDS.toMillis(30);
        when(taskExecution.getTaskInstance()).thenReturn(createFailedTaskInstance(elapsedTime));

        final TaskRetryLifecycleEvent event = TaskRetryLifecycleEvent.of(taskExecution);

        final long expectedDelay = TimeUnit.MINUTES.toMillis(RETRY_INTERVAL_MINUTES) - elapsedTime;
        assertThat(event.getDelay(TimeUnit.MILLISECONDS)).isAtMost(expectedDelay);
        assertThat(event.getDelay(TimeUnit.MILLISECONDS))
                .isAtLeast(expectedDelay - TimeUnit.SECONDS.toMillis(10));
    }

    @Test
    @DisplayName("Test the retry is triggered immediately when the retry interval has already elapsed")
    void testOf_retryIntervalAlreadyElapsed_triggerRetryImmediately() {
        // This happens when the retry event is recreated long after the task failed, e.g. the master which owned
        // the workflow crashed and another master takes the workflow over by failover.
        final long elapsedTime = TimeUnit.HOURS.toMillis(2);
        when(taskExecution.getTaskInstance()).thenReturn(createFailedTaskInstance(elapsedTime));

        final TaskRetryLifecycleEvent event = TaskRetryLifecycleEvent.of(taskExecution);

        assertThat(event.getDelay(TimeUnit.MILLISECONDS)).isAtMost(0L);
    }

    @Test
    @DisplayName("Test creating the retry event failed when the task has no remaining retry times")
    void testOf_retryTimesExhausted_throwIllegalStateException() {
        final TaskInstance taskInstance = createFailedTaskInstance(0);
        taskInstance.setRetryTimes(MAX_RETRY_TIMES);
        when(taskExecution.getTaskInstance()).thenReturn(taskInstance);

        assertThrows(IllegalStateException.class, () -> TaskRetryLifecycleEvent.of(taskExecution));
    }

    private TaskInstance createFailedTaskInstance(final long elapsedTimeSinceTaskEnded) {
        final TaskInstance taskInstance = new TaskInstance();
        taskInstance.setRetryTimes(0);
        taskInstance.setMaxRetryTimes(MAX_RETRY_TIMES);
        taskInstance.setRetryInterval(RETRY_INTERVAL_MINUTES);
        taskInstance.setEndTime(new Date(System.currentTimeMillis() - elapsedTimeSinceTaskEnded));
        return taskInstance;
    }
}
