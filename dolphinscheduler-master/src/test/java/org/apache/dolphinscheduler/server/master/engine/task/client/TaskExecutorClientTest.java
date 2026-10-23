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

package org.apache.dolphinscheduler.server.master.engine.task.client;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.utils.TaskTypeUtils;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorLifecycleEventType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class TaskExecutorClientTest {

    private final PhysicalTaskExecutorClientDelegator physicalClient = mock(PhysicalTaskExecutorClientDelegator.class);
    private final LogicTaskExecutorClientDelegator logicClient = mock(LogicTaskExecutorClientDelegator.class);
    private final TaskExecutorClient client = new TaskExecutorClient();
    private final TaskInstance task = new TaskInstance();
    private final TaskExecutorLifecycleEventAck ack =
            new TaskExecutorLifecycleEventAck(7, TaskExecutorLifecycleEventType.SUCCESS);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(client, "physicalTaskExecutorClientDelegator", physicalClient);
        ReflectionTestUtils.setField(client, "logicTaskExecutorClientDelegator", logicClient);
        task.setId(7);
        task.setHost("executor.test:1234");
        task.setTaskType("SHELL");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldRouteAPersistedTaskAckUsingItsTaskType(final boolean logicTask) {
        try (MockedStatic<TaskTypeUtils> taskTypes = mockStatic(TaskTypeUtils.class)) {
            taskTypes.when(() -> TaskTypeUtils.isLogicTask("SHELL")).thenReturn(logicTask);

            client.ackTaskExecutorLifecycleEvent(task, ack);

            if (logicTask) {
                verify(logicClient).ackTaskExecutorLifecycleEvent(task, ack);
                verifyNoInteractions(physicalClient);
            } else {
                verify(physicalClient).ackTaskExecutorLifecycleEvent(task, ack);
                verifyNoInteractions(logicClient);
            }
        }
    }

    @Test
    void shouldPreserveTheExecutionBasedAckEntryPoint() {
        final ITaskExecution execution = mock(ITaskExecution.class);
        when(execution.getTaskInstance()).thenReturn(task);
        try (MockedStatic<TaskTypeUtils> taskTypes = mockStatic(TaskTypeUtils.class)) {
            taskTypes.when(() -> TaskTypeUtils.isLogicTask("SHELL")).thenReturn(false);

            client.ackTaskExecutorLifecycleEvent(execution, ack);

            verify(physicalClient).ackTaskExecutorLifecycleEvent(task, ack);
            verifyNoInteractions(logicClient);
        }
    }
}
