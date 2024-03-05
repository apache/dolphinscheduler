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

package org.apache.dolphinscheduler.server.master.dag;

import static org.apache.dolphinscheduler.server.master.dag.WorkflowExecutionRunnableAssertions.workflowExecutionRunnable;
import static org.apache.dolphinscheduler.server.master.dag.WorkflowExecutionRunnableRepositoryAssertions.workflowExecutionRunnableRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.dolphinscheduler.server.master.events.WorkflowOperationEvent;
import org.apache.dolphinscheduler.server.master.exception.WorkflowExecuteRunnableNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkflowEngineTest {

    private WorkflowExecuteRunnableRepository workflowExecuteRunnableRepository;
    private WorkflowEngine workflowEngine;

    @BeforeEach
    public void before() {
        workflowExecuteRunnableRepository = new WorkflowExecuteRunnableRepository();
        workflowEngine = new WorkflowEngine(workflowExecuteRunnableRepository);
    }

    @Test
    void triggerWorkflow() {
        IWorkflowExecutionRunnable emptyWorkflowExecuteRunnable =
                MockWorkflowExecutionRunnableFactory.createWorkflowExecuteRunnable();
        Integer workflowInstanceId = emptyWorkflowExecuteRunnable.getWorkflowExecutionContext().getWorkflowInstanceId();

        workflowEngine.triggerWorkflow(emptyWorkflowExecuteRunnable);
        workflowExecutionRunnable(emptyWorkflowExecuteRunnable)
                .existEvent(WorkflowOperationEvent.triggerEvent(workflowInstanceId));
    }

    @Test
    void pauseWorkflow_WorkflowNotExist() {
        WorkflowExecuteRunnableNotFoundException exception =
                assertThrows(WorkflowExecuteRunnableNotFoundException.class, () -> workflowEngine.pauseWorkflow(1));
        assertEquals("WorkflowExecuteRunnable not found: [id=1]", exception.getMessage());
    }

    @Test
    void pauseWorkflow_WorkflowExist() {
        IWorkflowExecutionRunnable emptyWorkflowExecuteRunnable =
                MockWorkflowExecutionRunnableFactory.createWorkflowExecuteRunnable();
        Integer workflowInstanceId =
                emptyWorkflowExecuteRunnable.getWorkflowExecutionContext().getWorkflowInstanceId();
        workflowExecuteRunnableRepository.storeWorkflowExecutionRunnable(emptyWorkflowExecuteRunnable);

        workflowEngine.pauseWorkflow(workflowInstanceId);
        workflowExecutionRunnable(emptyWorkflowExecuteRunnable)
                .existEvent(WorkflowOperationEvent.pauseEvent(workflowInstanceId));
    }

    @Test
    void killWorkflow_WorkflowNotExist() {
        WorkflowExecuteRunnableNotFoundException exception =
                assertThrows(WorkflowExecuteRunnableNotFoundException.class,
                        () -> workflowEngine.killWorkflow(1));
        assertEquals("WorkflowExecuteRunnable not found: [id=1]", exception.getMessage());
    }

    @Test
    void killWorkflow_WorkflowExist() {
        IWorkflowExecutionRunnable emptyWorkflowExecuteRunnable =
                MockWorkflowExecutionRunnableFactory.createWorkflowExecuteRunnable();
        Integer workflowInstanceId =
                emptyWorkflowExecuteRunnable.getWorkflowExecutionContext().getWorkflowInstanceId();
        workflowExecuteRunnableRepository.storeWorkflowExecutionRunnable(emptyWorkflowExecuteRunnable);

        workflowEngine.killWorkflow(workflowInstanceId);
        workflowExecutionRunnable(emptyWorkflowExecuteRunnable)
                .existEvent(WorkflowOperationEvent.killEvent(workflowInstanceId));
    }

    @Test
    void finalizeWorkflow_WorkflowNotExist() {
        workflowEngine.finalizeWorkflow(1);
    }

    @Test
    void finalizeWorkflow_WorkflowExist() {
        IWorkflowExecutionRunnable emptyWorkflowExecuteRunnable =
                MockWorkflowExecutionRunnableFactory.createWorkflowExecuteRunnable();
        Integer workflowInstanceId =
                emptyWorkflowExecuteRunnable.getWorkflowExecutionContext().getWorkflowInstanceId();
        workflowExecuteRunnableRepository.storeWorkflowExecutionRunnable(emptyWorkflowExecuteRunnable);
        workflowExecutionRunnableRepository(workflowExecuteRunnableRepository)
                .existWorkflowExecutionRunnable(workflowInstanceId);

        workflowEngine.finalizeWorkflow(workflowInstanceId);
        workflowExecutionRunnableRepository(workflowExecuteRunnableRepository)
                .notExistWorkflowExecutionRunnable(workflowInstanceId);
    }

}
