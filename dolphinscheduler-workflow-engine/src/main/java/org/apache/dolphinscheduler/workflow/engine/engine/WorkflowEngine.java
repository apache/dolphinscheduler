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

package org.apache.dolphinscheduler.workflow.engine.engine;

import org.apache.dolphinscheduler.workflow.engine.event.WorkflowOperationEvent;
import org.apache.dolphinscheduler.workflow.engine.exception.WorkflowExecuteRunnableNotFoundException;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionContext;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionRunnableRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkflowEngine implements IWorkflowEngine {

    private final IWorkflowExecutionRunnableRepository workflowExecuteRunnableRepository;

    private final IEventEngine eventEngine;

    public WorkflowEngine(IWorkflowExecutionRunnableRepository workflowExecuteRunnableRepository,
                          IEventEngine eventEngine) {
        this.workflowExecuteRunnableRepository = workflowExecuteRunnableRepository;
        this.eventEngine = eventEngine;
    }

    @Override
    public void start() {
        eventEngine.start();
    }

    @Override
    public void triggerWorkflow(IWorkflowExecutionRunnable workflowExecuteRunnable) {
        IWorkflowExecutionContext workflowExecutionContext = workflowExecuteRunnable.getWorkflowExecutionContext();
        Integer workflowInstanceId = workflowExecutionContext.getWorkflowInstanceId();
        log.info("Triggering WorkflowExecutionRunnable: {}", workflowExecutionContext.getWorkflowInstanceName());
        workflowExecuteRunnableRepository.storeWorkflowExecutionRunnable(workflowExecuteRunnable);
        workflowExecuteRunnable.storeEventToTail(WorkflowOperationEvent.triggerEvent(workflowInstanceId));
    }

    @Override
    public void pauseWorkflow(Integer workflowInstanceId) {
        IWorkflowExecutionRunnable workflowExecuteRunnable =
                workflowExecuteRunnableRepository.getWorkflowExecutionRunnableById(workflowInstanceId);
        if (workflowExecuteRunnable == null) {
            throw new WorkflowExecuteRunnableNotFoundException(workflowInstanceId);
        }
        log.info("Pausing WorkflowExecutionRunnable: {}",
                workflowExecuteRunnable.getWorkflowExecutionContext().getWorkflowInstanceName());
        workflowExecuteRunnable.storeEventToTail(WorkflowOperationEvent.pauseEvent(workflowInstanceId));
    }

    @Override
    public void killWorkflow(Integer workflowInstanceId) {
        IWorkflowExecutionRunnable workflowExecuteRunnable =
                workflowExecuteRunnableRepository.getWorkflowExecutionRunnableById(workflowInstanceId);
        if (workflowExecuteRunnable == null) {
            throw new WorkflowExecuteRunnableNotFoundException(workflowInstanceId);
        }
        log.info("Killing WorkflowExecutionRunnable: {}",
                workflowExecuteRunnable.getWorkflowExecutionContext().getWorkflowInstanceName());
        workflowExecuteRunnable.storeEventToTail(WorkflowOperationEvent.killEvent(workflowInstanceId));
    }

    @Override
    public void finalizeWorkflow(Integer workflowInstanceId) {
        IWorkflowExecutionRunnable workflowExecutionRunnable =
                workflowExecuteRunnableRepository.getWorkflowExecutionRunnableById(workflowInstanceId);
        if (workflowExecutionRunnable == null) {
            return;
        }
        // todo: If the workflowExecutionRunnable is not finished, we cannot finalize it.
        log.info("Finalizing WorkflowExecutionRunnable: {}",
                workflowExecutionRunnable.getWorkflowExecutionContext().getWorkflowInstanceName());
        workflowExecuteRunnableRepository.removeWorkflowExecutionRunnable(workflowInstanceId);
    }

    @Override
    public void shutdown() {
        eventEngine.shutdown();
    }

}
