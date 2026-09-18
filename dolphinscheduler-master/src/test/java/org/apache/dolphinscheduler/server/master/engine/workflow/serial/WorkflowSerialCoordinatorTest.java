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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.dolphinscheduler.dao.repository.SerialCommandDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;

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
}
