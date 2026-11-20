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

package org.apache.dolphinscheduler.server.worker.rpc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.apache.dolphinscheduler.extract.base.exception.RemoteException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.executor.PhysicalTaskEngineDelegator;
import org.apache.dolphinscheduler.task.executor.exceptions.TaskExecutorCapacityExceededException;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorDispatchRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PhysicalTaskExecutorOperatorImplTest {

    private final PhysicalTaskExecutorOperatorImpl operator = new PhysicalTaskExecutorOperatorImpl();
    private final PhysicalTaskEngineDelegator engine = mock(PhysicalTaskEngineDelegator.class);
    private final TaskExecutionContext context = new TaskExecutionContext();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(operator, "physicalTaskEngineDelegator", engine);
    }

    @Test
    void shouldReturnRejectionWhenNoSlotIsAvailable() {
        doThrow(new TaskExecutorCapacityExceededException("no slot available")).when(engine).dispatchLogicTask(context);

        assertFalse(operator.dispatchTask(TaskExecutorDispatchRequest.of(context)).isDispatchSuccess());
    }

    @Test
    void shouldNotReportUnexpectedFailureAsDefiniteRejection() {
        doThrow(new IllegalStateException("unexpected submission failure")).when(engine).dispatchLogicTask(context);

        assertThrows(RemoteException.class, () -> operator.dispatchTask(TaskExecutorDispatchRequest.of(context)));
    }
}
