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

package org.apache.dolphinscheduler.server.master.engine.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.ILogicTaskPluginFactory;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.LogicTaskPluginFactoryBuilder;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.dependent.DependentLogicTask;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.dependent.DependentTaskTracker;
import org.apache.dolphinscheduler.task.executor.TaskExecutorState;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedConstruction;

class LogicTaskExecutorTest {

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
    @CsvSource(value = {"null, 10000", "1, 1000", "5, 5000", "10, 10000", "20, 20000"}, nullValues = "null")
    void dependentTaskUsesConfiguredCheckInterval(Integer checkInterval, long expectedInterval) throws Exception {
        LogicTaskExecutor taskExecutor = createDependentTaskExecutor(checkInterval);

        assertThat(taskExecutor.getRemainingTrackDelay()).isZero();
        long beforeTrack = System.currentTimeMillis();
        assertThat(taskExecutor.trackTaskExecutorState()).isEqualTo(TaskExecutorState.RUNNING);
        long remainingDelay = taskExecutor.getRemainingTrackDelay();
        long afterTrack = System.currentTimeMillis();

        assertThat(remainingDelay).isBetween(expectedInterval - (afterTrack - beforeTrack), expectedInterval);
        verify(dependentTaskTrackers.constructed().get(0)).getDependentTaskStatus();
    }

    @Test
    void pauseDoesNotWaitForDependentCheckInterval() throws Exception {
        LogicTaskExecutor taskExecutor = createDependentTaskExecutor(20);
        taskExecutor.trackTaskExecutorState();

        taskExecutor.pause();

        assertThat(taskExecutor.getRemainingTrackDelay()).isZero();
        assertThat(taskExecutor.trackTaskExecutorState()).isEqualTo(TaskExecutorState.PAUSED);
        DependentTaskTracker tracker = dependentTaskTrackers.constructed().get(0);
        verify(tracker).getDependentTaskStatus();
        verifyNoMoreInteractions(tracker);
    }

    @Test
    void killDoesNotWaitForDependentCheckInterval() throws Exception {
        LogicTaskExecutor taskExecutor = createDependentTaskExecutor(20);
        taskExecutor.trackTaskExecutorState();

        taskExecutor.kill();

        assertThat(taskExecutor.getRemainingTrackDelay()).isZero();
        assertThat(taskExecutor.trackTaskExecutorState()).isEqualTo(TaskExecutorState.KILLED);
        DependentTaskTracker tracker = dependentTaskTrackers.constructed().get(0);
        verify(tracker).getDependentTaskStatus();
        verifyNoMoreInteractions(tracker);
    }

    @SuppressWarnings("unchecked")
    private LogicTaskExecutor createDependentTaskExecutor(Integer checkInterval) throws Exception {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskType("DEPENDENT");
        taskExecutionContext.setTaskName("dependent");
        taskExecutionContext.setDryRun(Flag.NO.getCode());
        taskExecutionContext.setTaskParams(checkInterval == null ? "{\"dependence\":{}}"
                : "{\"dependence\":{\"checkInterval\":" + checkInterval + "}}");

        ILogicTaskPluginFactory<DependentLogicTask> taskPluginFactory = mock(ILogicTaskPluginFactory.class);
        when(taskPluginFactory.getTaskType()).thenReturn("DEPENDENT");
        when(taskPluginFactory.createLogicTask(any())).thenAnswer(invocation -> new DependentLogicTask(
                taskExecutionContext, null, null, null, null, null, null, null));
        LogicTaskExecutor taskExecutor = new LogicTaskExecutor(LogicTaskExecutorBuilder.builder()
                .taskExecutionContext(taskExecutionContext)
                .logicTaskPluginFactoryBuilder(new LogicTaskPluginFactoryBuilder(
                        Collections.singletonList(taskPluginFactory)))
                .build());
        taskExecutor.start();
        return taskExecutor;
    }
}
