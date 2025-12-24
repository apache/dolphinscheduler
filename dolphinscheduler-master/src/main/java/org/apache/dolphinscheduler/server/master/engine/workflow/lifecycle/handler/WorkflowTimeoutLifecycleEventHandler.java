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

import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.WorkflowLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowTimeoutLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.statemachine.IWorkflowStateAction;
import org.apache.dolphinscheduler.service.alert.WorkflowAlertManager;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowTimeoutLifecycleEventHandler
        extends
            AbstractWorkflowLifecycleEventHandler<WorkflowTimeoutLifecycleEvent> {

    private final WorkflowAlertManager workflowAlertManager;

    public WorkflowTimeoutLifecycleEventHandler(final WorkflowAlertManager workflowAlertManager) {
        this.workflowAlertManager = workflowAlertManager;
    }

    @Override
    public void handle(final IWorkflowStateAction workflowStateAction,
                       final IWorkflowExecutionRunnable workflowExecutionRunnable,
                       final WorkflowTimeoutLifecycleEvent workflowTimeoutEvent) {
        final WorkflowInstance workflowInstance = workflowExecutionRunnable.getWorkflowInstance();
        final String workflowName = workflowExecutionRunnable.getName();

        // Check if workflow is still active (not finished)
        if (workflowInstance.getState().isFinalState()) {
            log.info("The workflow {} has been finished with state: {}, skip timeout alert.",
                    workflowName,
                    workflowInstance.getState().name());
            return;
        }

        log.info("The workflow {} has timeout, try to send a timeout alert.", workflowName);
        doWorkflowTimeoutAlert(workflowInstance);
    }

    private void doWorkflowTimeoutAlert(final WorkflowInstance workflowInstance) {
        // ProjectUser will be built in WorkflowAlertManager
        workflowAlertManager.sendWorkflowTimeoutAlert(workflowInstance, null);
    }

    @Override
    public ILifecycleEventType matchEventType() {
        return WorkflowLifecycleEventType.TIMEOUT;
    }
}
