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

package org.apache.dolphinscheduler.server.master.engine.executor.plugin.dependent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedConstruction;

class DependentLogicTaskTest {

    private static final int ASYNC_TEST_TIMEOUT_SECONDS = 5;

    private final AtomicLong currentTimeNanos = new AtomicLong();

    private MockedConstruction<DependentTaskTracker> dependentTaskTrackers;

    @BeforeEach
    void setUp() {
        dependentTaskTrackers = mockConstruction(DependentTaskTracker.class,
                (tracker, context) -> when(tracker.getDependentTaskStatus())
                        .thenReturn(TaskExecutionStatus.RUNNING_EXECUTION));
    }

    @AfterEach
    void tearDown() {
        dependentTaskTrackers.close();
    }

    @ParameterizedTest
    @CsvSource(value = {"null, 10", "1, 1", "5, 5", "10, 10", "20, 20"}, nullValues = "null")
    void dependencyQueriesUseConfiguredInterval(Integer checkInterval, long expectedIntervalSeconds) {
        DependentLogicTask logicTask = createDependentLogicTask(checkInterval);
        DependentTaskTracker tracker = dependentTaskTrackers.constructed().get(0);
        when(tracker.getDependentTaskStatus())
                .thenReturn(TaskExecutionStatus.RUNNING_EXECUTION, TaskExecutionStatus.SUCCESS);

        assertThat(logicTask.getTaskExecutionState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
        assertThat(logicTask.getTaskExecutionState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);

        long expectedIntervalNanos = TimeUnit.SECONDS.toNanos(expectedIntervalSeconds);
        currentTimeNanos.set(expectedIntervalNanos - 1);
        assertThat(logicTask.getTaskExecutionState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
        verify(tracker).getDependentTaskStatus();

        currentTimeNanos.set(expectedIntervalNanos);
        assertThat(logicTask.getTaskExecutionState()).isEqualTo(TaskExecutionStatus.SUCCESS);
        verify(tracker, times(2)).getDependentTaskStatus();
        verifyNoMoreInteractions(tracker);
    }

    @ParameterizedTest
    @CsvSource({"10, 2, 3", "15, 1, 2", "20, 1, 2"})
    void frameworkChecksReturnCachedStateUntilQueryIsDue(int checkInterval, int queriesAtTenSeconds,
                                                         int queriesAtTwentySeconds) {
        DependentLogicTask logicTask = createDependentLogicTask(checkInterval);
        DependentTaskTracker tracker = dependentTaskTrackers.constructed().get(0);

        assertThat(logicTask.getTaskExecutionState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
        verify(tracker).getDependentTaskStatus();

        currentTimeNanos.set(TimeUnit.SECONDS.toNanos(10));
        assertThat(logicTask.getTaskExecutionState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
        verify(tracker, times(queriesAtTenSeconds)).getDependentTaskStatus();

        currentTimeNanos.set(TimeUnit.SECONDS.toNanos(20));
        assertThat(logicTask.getTaskExecutionState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);
        verify(tracker, times(queriesAtTwentySeconds)).getDependentTaskStatus();
        verifyNoMoreInteractions(tracker);
    }

    @ParameterizedTest
    @EnumSource(value = TaskExecutionStatus.class, names = {"PAUSE", "KILL"})
    void controlStateIsReturnedBeforeNextDependencyQuery(TaskExecutionStatus controlState) throws Exception {
        DependentLogicTask logicTask = createDependentLogicTask(20);
        DependentTaskTracker tracker = dependentTaskTrackers.constructed().get(0);
        assertThat(logicTask.getTaskExecutionState()).isEqualTo(TaskExecutionStatus.RUNNING_EXECUTION);

        currentTimeNanos.set(TimeUnit.SECONDS.toNanos(10));
        controlTask(logicTask, controlState);
        assertThat(logicTask.getTaskExecutionState()).isEqualTo(controlState);

        currentTimeNanos.set(TimeUnit.SECONDS.toNanos(40));
        assertThat(logicTask.getTaskExecutionState()).isEqualTo(controlState);
        verify(tracker).getDependentTaskStatus();
        verifyNoMoreInteractions(tracker);
    }

    @ParameterizedTest
    @EnumSource(value = TaskExecutionStatus.class, names = {"SUCCESS", "FAILURE"})
    void finishedTaskDoesNotQueryDependenciesAgain(TaskExecutionStatus finalState) {
        DependentLogicTask logicTask = createDependentLogicTask(20);
        DependentTaskTracker tracker = dependentTaskTrackers.constructed().get(0);
        when(tracker.getDependentTaskStatus()).thenReturn(finalState);

        assertThat(logicTask.getTaskExecutionState()).isEqualTo(finalState);
        currentTimeNanos.set(TimeUnit.SECONDS.toNanos(20));
        assertThat(logicTask.getTaskExecutionState()).isEqualTo(finalState);
        verify(tracker).getDependentTaskStatus();
        verifyNoMoreInteractions(tracker);
    }

    @ParameterizedTest
    @CsvSource({"PAUSE, RUNNING_EXECUTION", "PAUSE, SUCCESS", "PAUSE, FAILURE",
            "KILL, RUNNING_EXECUTION", "KILL, SUCCESS", "KILL, FAILURE"})
    void dependentCheckDoesNotOverwriteConcurrentControlState(TaskExecutionStatus expectedState,
                                                              TaskExecutionStatus dependentState) throws Exception {
        DependentLogicTask logicTask = createDependentLogicTask(20);
        DependentTaskTracker tracker = dependentTaskTrackers.constructed().get(0);
        CountDownLatch checkStarted = new CountDownLatch(1);
        CountDownLatch finishCheck = new CountDownLatch(1);
        when(tracker.getDependentTaskStatus()).thenAnswer(invocation -> {
            checkStarted.countDown();
            assertThat(finishCheck.await(ASYNC_TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            return dependentState;
        });

        ExecutorService stateTracker = Executors.newSingleThreadExecutor();
        try {
            Future<TaskExecutionStatus> trackedState = stateTracker.submit(logicTask::getTaskExecutionState);
            assertThat(checkStarted.await(ASYNC_TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();

            controlTask(logicTask, expectedState);
            assertThat(logicTask.getTaskExecutionState()).isEqualTo(expectedState);
            finishCheck.countDown();

            assertThat(trackedState.get(ASYNC_TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)).isEqualTo(expectedState);
            assertThat(logicTask.getTaskExecutionState()).isEqualTo(expectedState);
            verify(tracker).getDependentTaskStatus();
            verifyNoMoreInteractions(tracker);
        } finally {
            finishCheck.countDown();
            stateTracker.shutdownNow();
            assertThat(stateTracker.awaitTermination(ASYNC_TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
        }
    }

    private void controlTask(DependentLogicTask logicTask, TaskExecutionStatus controlState) throws Exception {
        if (controlState == TaskExecutionStatus.PAUSE) {
            logicTask.pause();
        } else {
            logicTask.kill();
        }
    }

    private DependentLogicTask createDependentLogicTask(Integer checkInterval) {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskName("dependent");
        taskExecutionContext.setTaskParams(checkInterval == null ? "{\"dependence\":{}}"
                : "{\"dependence\":{\"checkInterval\":" + checkInterval + "}}");

        return new DependentLogicTask(taskExecutionContext, null, null, null, null, null, null, null,
                currentTimeNanos::get);
    }
}
