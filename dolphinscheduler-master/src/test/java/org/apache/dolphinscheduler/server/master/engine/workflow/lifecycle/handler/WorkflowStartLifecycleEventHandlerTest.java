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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.WorkflowLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowStartLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowTimeoutLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.statemachine.IWorkflowStateAction;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowStartLifecycleEventHandlerTest {

    @Mock
    private IWorkflowStateAction workflowStateAction;

    @Mock
    private IWorkflowExecutionRunnable workflowExecutionRunnable;

    @Mock
    private WorkflowStartLifecycleEvent workflowStartEvent;

    @Mock
    private WorkflowEventBus workflowEventBus;

    private WorkflowStartLifecycleEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new WorkflowStartLifecycleEventHandler();
    }

    @Test
    void testMatchEventType() {
        assertEquals(WorkflowLifecycleEventType.START, handler.matchEventType());
    }

    @Test
    void testHandleWithTimeoutConfigured() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("test-workflow");
        workflowInstance.setTimeout(60);
        workflowInstance.setStartTime(new Date());

        when(workflowExecutionRunnable.getWorkflowInstance()).thenReturn(workflowInstance);
        when(workflowExecutionRunnable.getWorkflowEventBus()).thenReturn(workflowEventBus);

        handler.handle(workflowStateAction, workflowExecutionRunnable, workflowStartEvent);

        verify(workflowEventBus).publish(any(WorkflowTimeoutLifecycleEvent.class));
        verify(workflowStateAction).onStartEvent(workflowExecutionRunnable, workflowStartEvent);
    }

    @Test
    void testHandleWithNoTimeout() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("test-workflow");
        workflowInstance.setTimeout(0);
        workflowInstance.setStartTime(new Date());

        when(workflowExecutionRunnable.getWorkflowInstance()).thenReturn(workflowInstance);

        handler.handle(workflowStateAction, workflowExecutionRunnable, workflowStartEvent);

        verify(workflowStateAction).onStartEvent(workflowExecutionRunnable, workflowStartEvent);
    }

    @Test
    void testHandleWithNegativeTimeout() {
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("test-workflow");
        workflowInstance.setTimeout(-1);
        workflowInstance.setStartTime(new Date());

        when(workflowExecutionRunnable.getWorkflowInstance()).thenReturn(workflowInstance);

        handler.handle(workflowStateAction, workflowExecutionRunnable, workflowStartEvent);

        verify(workflowStateAction).onStartEvent(workflowExecutionRunnable, workflowStartEvent);
    }
}
