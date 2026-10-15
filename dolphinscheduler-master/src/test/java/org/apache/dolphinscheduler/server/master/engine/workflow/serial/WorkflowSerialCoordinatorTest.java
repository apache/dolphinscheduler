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

package org.apache.dolphinscheduler.server.master.engine.workflow.serial;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.model.SerialCommandDto;
import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowSerialCoordinatorTest {

    @InjectMocks
    private WorkflowSerialCoordinator workflowSerialCoordinator;

    @Mock
    private SerialCommandDao serialCommandDao;

    @Mock
    private WorkflowDefinitionLogDao workflowDefinitionLogDao;

    @Mock
    private WorkflowInstanceDao workflowInstanceDao;

    @Mock
    private SerialCommandWaitHandler serialCommandWaitHandler;

    @Mock
    private SerialCommandDiscardHandler serialCommandDiscardHandler;

    @Mock
    private SerialCommandPriorityHandler serialCommandPriorityHandler;

    @Test
    void startWhenAlreadyStartedShouldThrow() {
        workflowSerialCoordinator.start();
        assertThrows(IllegalStateException.class, () -> workflowSerialCoordinator.start());
        workflowSerialCoordinator.close();
    }

    @Test
    void startAfterCloseShouldNotThrow() {
        // The master hands the coordinator role back and forth without restarting the
        // JVM -- MasterCoordinatorListener calls close() on changeToStandBy() and
        // start() on changeToActive() against this same instance -- so the coordinator
        // has to be restartable in place. Before the fix, close() left internalThread
        // set and this second start() threw "InternalThread is already started".
        workflowSerialCoordinator.start();
        workflowSerialCoordinator.close();

        assertDoesNotThrow(() -> workflowSerialCoordinator.start());
        workflowSerialCoordinator.close();
    }

    @Test
    void closeShouldBeIdempotent() {
        workflowSerialCoordinator.start();
        workflowSerialCoordinator.close();

        assertDoesNotThrow(() -> workflowSerialCoordinator.close());
    }
    @Test
    void reconcileShouldRecycleSerialCommandWhoseWorkflowInstanceIsInFinalState() {
        final SerialCommandDto staleSerialCommand = serialCommandOf(1, 10);
        // A workflow instance which already reached a final state without its serial
        // command being cleared (#18698): the master died between persisting the
        // failure and the workflowFinish transaction that deletes the serial command.
        when(workflowInstanceDao.queryByIds(any()))
                .thenReturn(Collections.singletonList(workflowInstanceOf(10, WorkflowExecutionStatus.FAILURE)));

        final List<SerialCommandDto> liveSerialCommands =
                workflowSerialCoordinator.reconcileStaleSerialCommands(Collections.singletonList(staleSerialCommand));

        assertEquals(Collections.emptyList(), liveSerialCommands);
        verify(serialCommandDao).deleteById(1);
    }

    @Test
    void reconcileShouldRecycleSerialCommandWhoseWorkflowInstanceIsMissing() {
        final SerialCommandDto staleSerialCommand = serialCommandOf(2, 20);
        when(workflowInstanceDao.queryByIds(any())).thenReturn(Collections.emptyList());

        final List<SerialCommandDto> liveSerialCommands =
                workflowSerialCoordinator.reconcileStaleSerialCommands(Collections.singletonList(staleSerialCommand));

        assertEquals(Collections.emptyList(), liveSerialCommands);
        verify(serialCommandDao).deleteById(2);
    }

    @Test
    void reconcileShouldRecycleSerialCommandWithoutWorkflowInstanceId() {
        final SerialCommandDto staleSerialCommand = serialCommandOf(3, null);

        final List<SerialCommandDto> liveSerialCommands =
                workflowSerialCoordinator.reconcileStaleSerialCommands(Collections.singletonList(staleSerialCommand));

        assertEquals(Collections.emptyList(), liveSerialCommands);
        verify(serialCommandDao).deleteById(3);
        verify(workflowInstanceDao, never()).queryByIds(any());
    }

    @Test
    void reconcileShouldKeepSerialCommandsOfWaitingAndRunningWorkflowInstances() {
        final SerialCommandDto waitingSerialCommand = serialCommandOf(4, 40);
        final SerialCommandDto runningSerialCommand = serialCommandOf(5, 50);
        // While a serial command waits in the queue its instance holds the non-final
        // SERIAL_WAIT state, and while it runs the instance is RUNNING_EXECUTION.
        when(workflowInstanceDao.queryByIds(any())).thenReturn(Arrays.asList(
                workflowInstanceOf(40, WorkflowExecutionStatus.SERIAL_WAIT),
                workflowInstanceOf(50, WorkflowExecutionStatus.RUNNING_EXECUTION)));

        final List<SerialCommandDto> liveSerialCommands = workflowSerialCoordinator
                .reconcileStaleSerialCommands(Arrays.asList(waitingSerialCommand, runningSerialCommand));

        assertEquals(Arrays.asList(waitingSerialCommand, runningSerialCommand), liveSerialCommands);
        verify(serialCommandDao, never()).deleteById(any());
    }

    @Test
    void reconcileShouldNotTouchDaoForEmptySerialCommands() {
        assertEquals(Collections.emptyList(),
                workflowSerialCoordinator.reconcileStaleSerialCommands(Collections.emptyList()));
        verify(serialCommandDao, never()).deleteById(any());
        verify(workflowInstanceDao, never()).queryByIds(any());
    }

    private SerialCommandDto serialCommandOf(final int id, final Integer workflowInstanceId) {
        return SerialCommandDto.builder()
                .id(id)
                .workflowInstanceId(workflowInstanceId)
                .build();
    }

    private WorkflowInstance workflowInstanceOf(final int id, final WorkflowExecutionStatus state) {
        final WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(id);
        workflowInstance.setState(state);
        return workflowInstance;
    }
}
