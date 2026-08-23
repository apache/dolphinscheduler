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

package org.apache.dolphinscheduler.server.master.engine.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.server.master.engine.exceptions.CommandDuplicateHandleException;
import org.apache.dolphinscheduler.service.command.CommandService;

import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommandEngineTest {

    @Mock
    private WorkflowInstanceDao workflowInstanceDao;

    @Mock
    private CommandService commandService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private CommandEngine commandEngine;

    /**
     * Execute the transaction callback inline, so that reaching the non-duplicate branch produces a
     * clear verification failure instead of a NullPointerException.
     */
    @BeforeEach
    void setUp() {
        when(transactionTemplate.execute(any()))
                .thenAnswer(invocation -> {
                    final TransactionCallback<?> callback = invocation.getArgument(0);
                    return callback.doInTransaction(null);
                });
    }

    private static Command duplicatedCommand() {
        final Command command = new Command();
        command.setId(520266);
        command.setWorkflowInstanceId(516982);
        return command;
    }

    private void invokeBootstrapError(Command command, Throwable throwable) {
        ReflectionTestUtils.invokeMethod(commandEngine, "bootstrapError", command, throwable);
    }

    private void assertWorkflowWasNotForceFailed() {
        verify(workflowInstanceDao, never())
                .forceUpdateWorkflowInstanceState(anyInt(), any(WorkflowExecutionStatus.class));
        verify(commandService, never()).moveToErrorCommand(any(Command.class), anyString());
    }

    /**
     * The duplicate-handle exception is raised inside a CompletableFuture chain and therefore reaches
     * bootstrapError wrapped in CompletionException. It must still be recognised, otherwise the healthy
     * first execution has its workflow instance force-failed underneath it, can never complete, never
     * leaves the in-memory workflow repository, and permanently pins the instance count that
     * MasterServerLoadProtection uses - deadlocking command consumption. See #18570.
     */
    @Test
    void bootstrapErrorShouldNotFailWorkflowWhenDuplicateExceptionIsWrapped() {
        final Command command = duplicatedCommand();

        invokeBootstrapError(command, new CompletionException(new CommandDuplicateHandleException(command)));

        assertWorkflowWasNotForceFailed();
    }

    /**
     * Deeply nested wrapping must be handled as well.
     */
    @Test
    void bootstrapErrorShouldNotFailWorkflowWhenDuplicateExceptionIsNestedTwice() {
        final Command command = duplicatedCommand();

        invokeBootstrapError(command,
                new CompletionException(new RuntimeException(new CommandDuplicateHandleException(command))));

        assertWorkflowWasNotForceFailed();
    }

    /**
     * The unwrapped case must keep behaving exactly as before.
     */
    @Test
    void bootstrapErrorShouldNotFailWorkflowWhenDuplicateExceptionIsDirect() {
        final Command command = duplicatedCommand();

        invokeBootstrapError(command, new CommandDuplicateHandleException(command));

        assertWorkflowWasNotForceFailed();
    }
}
