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

import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBusCoordinator;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.WorkflowLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowFinalizeLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.statemachine.IWorkflowStateAction;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * This event handler will used to clear the WorkflowExecuteRunnable instance
 */
@Slf4j
@Component
public class WorkflowFinalizeLifecycleEventHandler
        extends
            AbstractWorkflowLifecycleEventHandler<WorkflowFinalizeLifecycleEvent> {

    @Lazy
    @Autowired
    private WorkflowEventBusCoordinator workflowEventBusCoordinator;

    @Autowired
    private IWorkflowRepository workflowRepository;

    @Override
    public void handle(final IWorkflowStateAction workflowStateAction,
                       final IWorkflowExecutionRunnable workflowExecutionRunnable,
                       final WorkflowFinalizeLifecycleEvent workflowFinalizeEvent) {
        workflowStateAction.finalizeEventAction(workflowExecutionRunnable, workflowFinalizeEvent);
    }

    @Override
    public ILifecycleEventType matchEventType() {
        return WorkflowLifecycleEventType.FINALIZE;
    }
}
