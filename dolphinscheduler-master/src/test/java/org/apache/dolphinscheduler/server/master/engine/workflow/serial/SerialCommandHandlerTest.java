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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.model.SerialCommandDto;
import org.apache.dolphinscheduler.dao.repository.CommandDao;
import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.extract.master.IWorkflowControlClient;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;

import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class SerialCommandHandlerTest {

    @Mock
    private SerialCommandDao serialCommandDao;
    @Mock
    private CommandDao commandDao;
    @Mock
    private WorkflowInstanceDao workflowInstanceDao;
    @Mock
    private IWorkflowControlClient workflowControlClient;
    @Mock
    private MasterConfig masterConfig;
    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private TransactionStatus transactionStatus;

    private SerialCommandWaitHandler waitHandler;
    private SerialCommandDiscardHandler discardHandler;
    private SerialCommandPriorityHandler priorityHandler;

    @BeforeEach
    void setUp() {
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(transactionStatus);
        waitHandler = configure(new SerialCommandWaitHandler());
        discardHandler = configure(new SerialCommandDiscardHandler());
        priorityHandler = configure(new SerialCommandPriorityHandler());
    }

    @Test
    void testWaitLaunchesClaimedCommand() {
        SerialCommandDto command = command(1, SerialCommandDto.State.WAITING);
        when(serialCommandDao.launchSerialCommand(eq(1), any(Date.class))).thenReturn(true);
        when(commandDao.insert(command.getCommand())).thenReturn(1);
        waitHandler.handle(group(command));
        assertEquals(SerialCommandDto.State.LAUNCHED, command.getState());
        verify(commandDao).insert(command.getCommand());
        verify(transactionManager).commit(transactionStatus);
    }

    @Test
    void testWaitDoesNotInsertFromStaleWaitingSnapshot() {
        SerialCommandDto command = command(1, SerialCommandDto.State.WAITING);
        when(serialCommandDao.launchSerialCommand(eq(1), any(Date.class))).thenReturn(false);
        waitHandler.handle(group(command));
        verify(commandDao, never()).insert(any());
        assertEquals(SerialCommandDto.State.WAITING, command.getState());
    }

    @Test
    void testInsertReturningZeroRollsBackClaim() {
        SerialCommandDto command = command(1, SerialCommandDto.State.WAITING);
        when(serialCommandDao.launchSerialCommand(eq(1), any(Date.class))).thenReturn(true);
        when(commandDao.insert(command.getCommand())).thenReturn(0);
        assertThrows(IllegalStateException.class, () -> waitHandler.handle(group(command)));
        verify(transactionManager).rollback(transactionStatus);
        verify(transactionManager, never()).commit(any());
        assertEquals(SerialCommandDto.State.WAITING, command.getState());
    }

    @Test
    void testInsertExceptionDoesNotChangeSnapshotState() {
        SerialCommandDto command = command(1, SerialCommandDto.State.WAITING);
        when(serialCommandDao.launchSerialCommand(eq(1), any(Date.class))).thenReturn(true);
        when(commandDao.insert(command.getCommand())).thenThrow(new IllegalStateException("insert failed"));
        assertThrows(IllegalStateException.class, () -> waitHandler.handle(group(command)));
        verify(transactionManager).rollback(transactionStatus);
        assertEquals(SerialCommandDto.State.WAITING, command.getState());
    }

    @Test
    void testDiscardStopsOnlySuccessfullyDeletedWaitingCommand() {
        SerialCommandDto head = command(1, SerialCommandDto.State.LAUNCHED);
        SerialCommandDto waiting = command(2, SerialCommandDto.State.WAITING);
        WorkflowInstance instance = instance(waiting);
        when(serialCommandDao.deleteByIdAndState(2, SerialCommandDto.State.WAITING)).thenReturn(true);
        when(workflowInstanceDao.queryById(waiting.getWorkflowInstanceId())).thenReturn(instance);
        when(workflowInstanceDao.updateById(instance)).thenReturn(true);
        discardHandler.handle(group(head, waiting));
        assertEquals(WorkflowExecutionStatus.STOP, instance.getState());
        verify(workflowInstanceDao).updateById(instance);
        verify(serialCommandDao, never()).deleteById(any());
    }

    @Test
    void testDiscardConflictLeavesRemainingOldSnapshotUntouched() {
        SerialCommandDto head = command(1, SerialCommandDto.State.LAUNCHED);
        SerialCommandDto conflicting = command(2, SerialCommandDto.State.WAITING);
        SerialCommandDto remaining = command(3, SerialCommandDto.State.WAITING);
        when(serialCommandDao.deleteByIdAndState(2, SerialCommandDto.State.WAITING)).thenReturn(false);
        discardHandler.handle(group(head, conflicting, remaining));
        verify(serialCommandDao, never()).deleteByIdAndState(3, SerialCommandDto.State.WAITING);
        verify(workflowInstanceDao, never()).queryById(anyInt());
        verify(commandDao, never()).insert(any());
    }

    @Test
    void testDiscardDoesNotContinueAfterLosingHeadClaim() {
        SerialCommandDto head = command(1, SerialCommandDto.State.WAITING);
        SerialCommandDto remaining = command(2, SerialCommandDto.State.WAITING);
        when(serialCommandDao.launchSerialCommand(eq(1), any(Date.class))).thenReturn(false);
        discardHandler.handle(group(head, remaining));
        verify(serialCommandDao, never()).deleteByIdAndState(anyInt(), any());
        verify(commandDao, never()).insert(any());
    }

    @Test
    void testFailedWorkflowUpdateRollsBackDiscard() {
        SerialCommandDto head = command(1, SerialCommandDto.State.LAUNCHED);
        SerialCommandDto waiting = command(2, SerialCommandDto.State.WAITING);
        WorkflowInstance instance = instance(waiting);
        when(serialCommandDao.deleteByIdAndState(2, SerialCommandDto.State.WAITING)).thenReturn(true);
        when(workflowInstanceDao.queryById(waiting.getWorkflowInstanceId())).thenReturn(instance);
        when(workflowInstanceDao.updateById(instance)).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> discardHandler.handle(group(head, waiting)));
        verify(transactionManager).rollback(transactionStatus);
    }

    @Test
    void testPriorityDoesNotLaunchTailAfterDiscardConflict() {
        SerialCommandDto previous = command(1, SerialCommandDto.State.WAITING);
        SerialCommandDto newest = command(2, SerialCommandDto.State.WAITING);
        when(serialCommandDao.deleteByIdAndState(1, SerialCommandDto.State.WAITING)).thenReturn(false);
        priorityHandler.handle(group(previous, newest));
        verify(serialCommandDao, never()).launchSerialCommand(anyInt(), any());
        verify(workflowInstanceDao, never()).queryById(anyInt());
        verify(commandDao, never()).insert(any());
    }

    @Test
    void testPriorityHandlesFreshSnapshotAfterDiscardConflict() {
        SerialCommandDto previous = command(1, SerialCommandDto.State.WAITING);
        SerialCommandDto newest = command(2, SerialCommandDto.State.WAITING);
        when(serialCommandDao.deleteByIdAndState(1, SerialCommandDto.State.WAITING)).thenReturn(false);
        priorityHandler.handle(group(previous, newest));
        verify(commandDao, never()).insert(any());

        // The next poll sees the predecessor as LAUNCHED and follows the existing stop-RPC path.
        SerialCommandDto launched = command(1, SerialCommandDto.State.LAUNCHED);
        WorkflowInstance instance = instance(launched);
        when(workflowInstanceDao.queryById(launched.getWorkflowInstanceId())).thenReturn(instance);
        when(masterConfig.getMasterAddress()).thenReturn("local-master");
        when(serialCommandDao.launchSerialCommand(eq(2), any(Date.class))).thenReturn(true);
        when(commandDao.insert(newest.getCommand())).thenReturn(1);
        priorityHandler.handle(group(launched, newest));
        verify(workflowControlClient).stopWorkflowInstance(any());
        verify(commandDao).insert(newest.getCommand());
        assertEquals(SerialCommandDto.State.LAUNCHED, newest.getState());
    }

    @Test
    void testPriorityDiscardsWaitingPredecessorThenLaunchesNewest() {
        SerialCommandDto previous = command(1, SerialCommandDto.State.WAITING);
        SerialCommandDto newest = command(2, SerialCommandDto.State.WAITING);
        WorkflowInstance instance = instance(previous);
        when(serialCommandDao.deleteByIdAndState(1, SerialCommandDto.State.WAITING)).thenReturn(true);
        when(workflowInstanceDao.queryById(previous.getWorkflowInstanceId())).thenReturn(instance);
        when(workflowInstanceDao.updateById(instance)).thenReturn(true);
        when(serialCommandDao.launchSerialCommand(eq(2), any(Date.class))).thenReturn(true);
        when(commandDao.insert(newest.getCommand())).thenReturn(1);
        priorityHandler.handle(group(previous, newest));
        assertEquals(WorkflowExecutionStatus.STOP, instance.getState());
        assertEquals(SerialCommandDto.State.LAUNCHED, newest.getState());
        verify(workflowControlClient, never()).stopWorkflowInstance(any());
    }

    private <T extends AbstractSerialCommandHandler> T configure(T handler) {
        handler.serialCommandDao = serialCommandDao;
        handler.commandDao = commandDao;
        handler.workflowInstanceDao = workflowInstanceDao;
        handler.workflowControlClient = workflowControlClient;
        handler.masterConfig = masterConfig;
        handler.transactionTemplate = new TransactionTemplate(transactionManager);
        return handler;
    }

    private SerialCommandsGroup group(SerialCommandDto... commands) {
        return SerialCommandsGroup.builder().serialCommands(Arrays.asList(commands)).build();
    }

    private SerialCommandDto command(int id, SerialCommandDto.State state) {
        return SerialCommandDto.builder()
                .id(id)
                .workflowInstanceId(id + 100)
                .workflowDefinitionCode(1L)
                .workflowDefinitionVersion(1)
                .state(state)
                .command(Command.builder().commandType(CommandType.START_PROCESS)
                        .workflowDefinitionCode(1L).workflowDefinitionVersion(1).workflowInstanceId(id + 100).build())
                .build();
    }

    private WorkflowInstance instance(SerialCommandDto command) {
        WorkflowInstance instance = new WorkflowInstance();
        instance.setId(command.getWorkflowInstanceId());
        instance.setWorkflowDefinitionCode(command.getWorkflowDefinitionCode());
        instance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        instance.setHost("local-master");
        return instance;
    }
}
