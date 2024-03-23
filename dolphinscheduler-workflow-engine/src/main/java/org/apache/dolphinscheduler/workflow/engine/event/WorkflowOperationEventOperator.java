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

package org.apache.dolphinscheduler.workflow.engine.event;

import org.apache.dolphinscheduler.workflow.engine.utils.ExceptionUtils;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionContext;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionRunnableRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkflowOperationEventOperator implements IWorkflowEventOperator<WorkflowOperationEvent> {

    private final IWorkflowExecutionRunnableRepository workflowExecuteRunnableRepository;

    public WorkflowOperationEventOperator(IWorkflowExecutionRunnableRepository workflowExecuteRunnableRepository) {
        this.workflowExecuteRunnableRepository = workflowExecuteRunnableRepository;
    }

    @Override
    public void handleEvent(WorkflowOperationEvent event) {
        IWorkflowExecutionRunnable workflowExecutionRunnable =
                workflowExecuteRunnableRepository.getWorkflowExecutionRunnableById(event.getWorkflowInstanceId());
        if (workflowExecutionRunnable == null) {
            log.warn("WorkflowExecutionRunnable not found: {}", event);
            return;
        }
        switch (event.getWorkflowOperationType()) {
            case TRIGGER:
                triggerWorkflow(workflowExecutionRunnable);
                break;
            case PAUSE:
                pauseWorkflow(workflowExecutionRunnable);
                break;
            case KILL:
                killWorkflow(workflowExecutionRunnable);
                break;
            default:
                log.error("Unknown operationType for event: {}", event);
        }
    }

    private void triggerWorkflow(IWorkflowExecutionRunnable workflowExecutionRunnable) {
        try {
            workflowExecutionRunnable.start();
        } catch (Throwable exception) {
            if (ExceptionUtils.isDatabaseConnectedFailedException(exception)) {
                throw exception;
            }
            IWorkflowExecutionContext workflowExecutionContext =
                    workflowExecutionRunnable.getWorkflowExecutionContext();
            log.error("Trigger workflow: {} failed", workflowExecutionContext.getWorkflowInstanceName(), exception);
            WorkflowFailedEvent workflowExecutionRunnableFailedEvent = WorkflowFailedEvent.builder()
                    .workflowInstanceId(workflowExecutionContext.getWorkflowInstanceId())
                    .failedReason(exception.getMessage())
                    .build();
            workflowExecutionRunnable.storeEventToTail(workflowExecutionRunnableFailedEvent);
        }
    }

    private void pauseWorkflow(IWorkflowExecutionRunnable workflowExecutionRunnable) {
        workflowExecutionRunnable.pause();
    }

    private void killWorkflow(IWorkflowExecutionRunnable workflowExecutionRunnable) {
        workflowExecutionRunnable.kill();
    }
}
