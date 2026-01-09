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

package org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.WorkflowLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowTimeoutLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.statemachine.IWorkflowStateAction;
import org.apache.dolphinscheduler.service.alert.WorkflowAlertManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowTimeoutLifecycleEventHandlerTest {

    @Mock
    private WorkflowAlertManager workflowAlertManager;

    @Mock
    private IWorkflowStateAction workflowStateAction;

    @Mock
    private IWorkflowExecutionRunnable workflowExecutionRunnable;

    @Mock
    private WorkflowTimeoutLifecycleEvent workflowTimeoutEvent;

    private WorkflowTimeoutLifecycleEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new WorkflowTimeoutLifecycleEventHandler(workflowAlertManager);
    }

    @Test
    void testMatchEventType() {
        assertEquals(WorkflowLifecycleEventType.TIMEOUT, handler.matchEventType());
    }

    @Test
    void testHandleWorkflowTimeoutWithRunningWorkflow() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("test-workflow");
        workflowInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        workflowInstance.setWarningGroupId(1);

        when(workflowExecutionRunnable.getWorkflowInstance()).thenReturn(workflowInstance);
        when(workflowExecutionRunnable.getName()).thenReturn("test-workflow");

        handler.handle(workflowStateAction, workflowExecutionRunnable, workflowTimeoutEvent);

        verify(workflowAlertManager).sendWorkflowTimeoutAlert(eq(workflowInstance));
    }

    @Test
    void testHandleWorkflowTimeoutWithFinishedWorkflow() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("test-workflow");
        workflowInstance.setState(WorkflowExecutionStatus.SUCCESS);

        when(workflowExecutionRunnable.getWorkflowInstance()).thenReturn(workflowInstance);
        when(workflowExecutionRunnable.getName()).thenReturn("test-workflow");

        handler.handle(workflowStateAction, workflowExecutionRunnable, workflowTimeoutEvent);

        verify(workflowAlertManager, never()).sendWorkflowTimeoutAlert(any());
    }

    @Test
    void testHandleWorkflowTimeoutWithFailedWorkflow() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("test-workflow");
        workflowInstance.setState(WorkflowExecutionStatus.FAILURE);

        when(workflowExecutionRunnable.getWorkflowInstance()).thenReturn(workflowInstance);
        when(workflowExecutionRunnable.getName()).thenReturn("test-workflow");

        handler.handle(workflowStateAction, workflowExecutionRunnable, workflowTimeoutEvent);

        verify(workflowAlertManager, never()).sendWorkflowTimeoutAlert(any());
    }

    @Test
    void testHandleWorkflowTimeoutWithStoppedWorkflow() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("test-workflow");
        workflowInstance.setState(WorkflowExecutionStatus.STOP);

        when(workflowExecutionRunnable.getWorkflowInstance()).thenReturn(workflowInstance);
        when(workflowExecutionRunnable.getName()).thenReturn("test-workflow");

        handler.handle(workflowStateAction, workflowExecutionRunnable, workflowTimeoutEvent);

        verify(workflowAlertManager, never()).sendWorkflowTimeoutAlert(any());
    }

    @Test
    void testHandleWorkflowTimeoutWithNullWarningGroupId() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("test-workflow");
        workflowInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        workflowInstance.setWarningGroupId(null);

        when(workflowExecutionRunnable.getWorkflowInstance()).thenReturn(workflowInstance);
        when(workflowExecutionRunnable.getName()).thenReturn("test-workflow");

        handler.handle(workflowStateAction, workflowExecutionRunnable, workflowTimeoutEvent);

        verify(workflowAlertManager, never()).sendWorkflowTimeoutAlert(any());
    }
}
