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
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.ILogicTask;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.ILogicTaskPluginFactory;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.LogicTaskPluginFactoryBuilder;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.dependent.DependentLogicTask;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.dependent.DependentTaskTracker;
import org.apache.dolphinscheduler.task.executor.TaskExecutorState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;

class LogicTaskExecutorTest {

    private static final long FRAMEWORK_CHECK_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(10);

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
    @NullSource
    @ValueSource(ints = {1, 5, 10, 20})
    void frameworkUsesFixedCheckInterval(Integer checkInterval) throws Exception {
        LogicTaskExecutor taskExecutor = createDependentTaskExecutor(checkInterval);

        assertThat(taskExecutor.getRemainingTrackDelay()).isZero();
        long beforeTrack = System.currentTimeMillis();
        assertThat(taskExecutor.trackTaskExecutorState()).isEqualTo(TaskExecutorState.RUNNING);
        long remainingDelay = taskExecutor.getRemainingTrackDelay();
        long afterTrack = System.currentTimeMillis();

        assertThat(remainingDelay).isBetween(
                FRAMEWORK_CHECK_INTERVAL_MILLIS - (afterTrack - beforeTrack), FRAMEWORK_CHECK_INTERVAL_MILLIS);
        verify(dependentTaskTrackers.constructed().get(0)).getDependentTaskStatus();
    }

    @Test
    void pauseIsReportedOnNextFrameworkCheck() throws Exception {
        LogicTaskExecutor taskExecutor = createDependentTaskExecutor(20);
        taskExecutor.trackTaskExecutorState();

        taskExecutor.pause();

        assertThat(taskExecutor.getRemainingTrackDelay()).isPositive()
                .isLessThanOrEqualTo(FRAMEWORK_CHECK_INTERVAL_MILLIS);
        assertThat(taskExecutor.trackTaskExecutorState()).isEqualTo(TaskExecutorState.PAUSED);
        DependentTaskTracker tracker = dependentTaskTrackers.constructed().get(0);
        verify(tracker).getDependentTaskStatus();
        verifyNoMoreInteractions(tracker);
    }

    @Test
    void killIsReportedOnNextFrameworkCheck() throws Exception {
        LogicTaskExecutor taskExecutor = createDependentTaskExecutor(20);
        taskExecutor.trackTaskExecutorState();

        taskExecutor.kill();

        assertThat(taskExecutor.getRemainingTrackDelay()).isPositive()
                .isLessThanOrEqualTo(FRAMEWORK_CHECK_INTERVAL_MILLIS);
        assertThat(taskExecutor.trackTaskExecutorState()).isEqualTo(TaskExecutorState.KILLED);
        DependentTaskTracker tracker = dependentTaskTrackers.constructed().get(0);
        verify(tracker).getDependentTaskStatus();
        verifyNoMoreInteractions(tracker);
    }

    private LogicTaskExecutor createDependentTaskExecutor(Integer checkInterval) throws Exception {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskType("DEPENDENT");
        taskExecutionContext.setTaskName("dependent");
        taskExecutionContext.setDryRun(Flag.NO.getCode());
        taskExecutionContext.setTaskParams(checkInterval == null ? "{\"dependence\":{}}"
                : "{\"dependence\":{\"checkInterval\":" + checkInterval + "}}");

        return createTaskExecutor(taskExecutionContext, new DependentLogicTask(
                taskExecutionContext, null, null, null, null, null, null, null));
    }

    @SuppressWarnings("unchecked")
    private LogicTaskExecutor createTaskExecutor(TaskExecutionContext taskExecutionContext,
                                                 ILogicTask<? extends AbstractParameters> logicTask) throws Exception {
        ILogicTaskPluginFactory<? extends ILogicTask<? extends AbstractParameters>> taskPluginFactory =
                mock(ILogicTaskPluginFactory.class);
        when(taskPluginFactory.getTaskType()).thenReturn(taskExecutionContext.getTaskType());
        when(taskPluginFactory.createLogicTask(any())).thenAnswer(invocation -> logicTask);
        List<ILogicTaskPluginFactory<? extends ILogicTask<? extends AbstractParameters>>> taskPluginFactories =
                new ArrayList<>();
        taskPluginFactories.add(taskPluginFactory);
        LogicTaskExecutor taskExecutor = new LogicTaskExecutor(LogicTaskExecutorBuilder.builder()
                .taskExecutionContext(taskExecutionContext)
                .logicTaskPluginFactoryBuilder(new LogicTaskPluginFactoryBuilder(taskPluginFactories))
                .build());
        taskExecutor.start();
        return taskExecutor;
    }
}
