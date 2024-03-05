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

import org.apache.dolphinscheduler.server.master.events.WorkflowOperationEvent;
import org.apache.dolphinscheduler.server.master.exception.WorkflowExecuteRunnableNotFoundException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowEngine implements IWorkflowEngine {

    private final WorkflowExecuteRunnableRepository workflowExecuteRunnableRepository;

    public WorkflowEngine(WorkflowExecuteRunnableRepository workflowExecuteRunnableRepository) {
        this.workflowExecuteRunnableRepository = workflowExecuteRunnableRepository;
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
        log.info("Finalizing WorkflowExecutionRunnable: {}",
                workflowExecutionRunnable.getWorkflowExecutionContext().getWorkflowInstanceName());
        workflowExecuteRunnableRepository.removeWorkflowExecutionRunnable(workflowInstanceId);
    }

}
