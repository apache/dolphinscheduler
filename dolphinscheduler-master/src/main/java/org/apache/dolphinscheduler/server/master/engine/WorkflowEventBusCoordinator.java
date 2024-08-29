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

package org.apache.dolphinscheduler.server.master.engine;

import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.runner.IWorkflowExecuteContext;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowEventBusCoordinator implements AutoCloseable {

    @Lazy
    @Autowired
    private WorkflowEventBusFireWorkers workflowEventBusFireWorkers;

    public void start() {
        workflowEventBusFireWorkers.start();
        log.info("WorkflowEventBusCoordinator started");
    }

    /**
     * Register a WorkflowExecuteRunnable to the corresponding WorkflowEventBusFireWorker, once the WorkflowExecuteRunnable has been registered,
     * then the event will auto handler by the WorkflowEventBusFireWorker
     */
    public void registerWorkflowEventBus(IWorkflowExecutionRunnable workflowExecutionRunnable) {
        final int workerSlot = calculateWorkflowEventBusFireWorkerSlot(workflowExecutionRunnable);
        final WorkflowEventBusFireWorker workflowEventBusFireWorker = workflowEventBusFireWorkers.getWorker(workerSlot);
        workflowEventBusFireWorker.registerWorkflowEventBus(workflowExecutionRunnable);
    }

    /**
     * UeRegister a WorkflowExecuteRunnable to the corresponding WorkflowEventBusFireWorker, once the WorkflowExecuteRunnable has been deregistered,
     * then the EventBus will be removed from the WorkflowEventBusFireWorker.
     */
    public void unRegisterWorkflowEventBus(IWorkflowExecutionRunnable workflowExecutionRunnable) {
        final int workerSlot = calculateWorkflowEventBusFireWorkerSlot(workflowExecutionRunnable);
        final WorkflowEventBusFireWorker workflowEventBusFireWorker = workflowEventBusFireWorkers.getWorker(workerSlot);
        workflowEventBusFireWorker.unRegisterWorkflowEventBus(workflowExecutionRunnable);
    }

    /**
     * Calculate the slot of the WorkflowEventBusFireWorker which the WorkflowExecuteRunnable should be registered.
     * <p> The slot is calculated by the workflowInstanceId % workerSize.
     * <p> e.g. If the workflowInstanceId is 1, and the workerSize is 3, then the slot is 1, the workflow will be registered to the worker[1].
     * <p> If the workflowInstanceIds are not consecutive numbers, these will cause some worker busy.
     */
    private int calculateWorkflowEventBusFireWorkerSlot(IWorkflowExecutionRunnable workflowExecutionRunnable) {
        final IWorkflowExecuteContext workflowExecuteContext = workflowExecutionRunnable.getWorkflowExecuteContext();
        final WorkflowInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        final Integer workflowInstanceId = workflowInstance.getId();
        return workflowInstanceId % workflowEventBusFireWorkers.getWorkerSize();
    }

    @Override
    public void close() throws Exception {
        workflowEventBusFireWorkers.close();
    }
}
