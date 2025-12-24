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

package org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.WorkflowLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteContext;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowTimeoutLifecycleEventTest {

    @Test
    void testCreateEventWithValidTimeout() {
        IWorkflowExecutionRunnable workflowExecutionRunnable = mock(IWorkflowExecutionRunnable.class);
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("test-workflow");
        workflowInstance.setTimeout(60);
        workflowInstance.setStartTime(new Date(System.currentTimeMillis() - 30 * 60 * 1000));

        when(workflowExecutionRunnable.getWorkflowInstance()).thenReturn(workflowInstance);

        WorkflowTimeoutLifecycleEvent event = WorkflowTimeoutLifecycleEvent.of(workflowExecutionRunnable);

        assertNotNull(event);
        assertEquals(WorkflowLifecycleEventType.TIMEOUT, event.getEventType());
        assertEquals(workflowExecutionRunnable, event.getWorkflowExecutionRunnable());
    }

    @Test
    void testCreateEventWithZeroTimeout() {
        IWorkflowExecutionRunnable workflowExecutionRunnable = mock(IWorkflowExecutionRunnable.class);
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("test-workflow");
        workflowInstance.setTimeout(0);
        workflowInstance.setStartTime(new Date());

        when(workflowExecutionRunnable.getWorkflowInstance()).thenReturn(workflowInstance);

        WorkflowTimeoutLifecycleEvent event = WorkflowTimeoutLifecycleEvent.of(workflowExecutionRunnable);

        assertNotNull(event);
    }

    @Test
    void testCreateEventWithAlreadyTimeout() {
        IWorkflowExecutionRunnable workflowExecutionRunnable = mock(IWorkflowExecutionRunnable.class);
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("test-workflow");
        workflowInstance.setTimeout(30);
        workflowInstance.setStartTime(new Date(System.currentTimeMillis() - 60 * 60 * 1000));

        when(workflowExecutionRunnable.getWorkflowInstance()).thenReturn(workflowInstance);

        WorkflowTimeoutLifecycleEvent event = WorkflowTimeoutLifecycleEvent.of(workflowExecutionRunnable);

        assertNotNull(event);
    }

    @Test
    void testCreateEventWithNullWorkflowInstance() {
        IWorkflowExecutionRunnable workflowExecutionRunnable = mock(IWorkflowExecutionRunnable.class);
        when(workflowExecutionRunnable.getWorkflowInstance()).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> WorkflowTimeoutLifecycleEvent.of(workflowExecutionRunnable));
    }

    @Test
    void testCreateEventWithNegativeTimeout() {
        IWorkflowExecutionRunnable workflowExecutionRunnable = mock(IWorkflowExecutionRunnable.class);
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("test-workflow");
        workflowInstance.setTimeout(-1);
        workflowInstance.setStartTime(new Date());

        when(workflowExecutionRunnable.getWorkflowInstance()).thenReturn(workflowInstance);

        assertThrows(IllegalStateException.class,
                () -> WorkflowTimeoutLifecycleEvent.of(workflowExecutionRunnable));
    }

    @Test
    void testToString() {
        IWorkflowExecutionRunnable workflowExecutionRunnable = mock(IWorkflowExecutionRunnable.class);
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("test-workflow");
        workflowInstance.setTimeout(60);
        workflowInstance.setStartTime(new Date());

        WorkflowExecuteContext context = mock(WorkflowExecuteContext.class);
        when(context.getWorkflowInstance()).thenReturn(workflowInstance);
        when(workflowExecutionRunnable.getWorkflowInstance()).thenReturn(workflowInstance);
        when(workflowExecutionRunnable.getWorkflowExecuteContext()).thenReturn(context);

        WorkflowTimeoutLifecycleEvent event = WorkflowTimeoutLifecycleEvent.of(workflowExecutionRunnable);

        String toString = event.toString();
        assertTrue(toString.contains("WorkflowTimeoutLifecycleEvent"));
        assertTrue(toString.contains("test-workflow"));
    }
}
