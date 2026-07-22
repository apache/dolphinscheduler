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
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.WorkflowLifecycleEventType;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowTimeoutLifecycleEvent;
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
                       final IWorkflowExecution workflowExecution,
                       final WorkflowTimeoutLifecycleEvent workflowTimeoutLifecycleEvent) {
        final WorkflowInstance workflowInstance = workflowExecution.getWorkflowInstance();
        final boolean shouldSendAlert = workflowInstance.getWarningGroupId() != null;

        if (shouldSendAlert) {
            doWorkflowTimeoutAlert(workflowExecution);
        } else {
            log.info("Skipped sending timeout alert for workflow {} because warningGroupId is null.",
                    workflowInstance.getName());
        }

    }

    @Override
    public ILifecycleEventType matchEventType() {
        return WorkflowLifecycleEventType.TIMEOUT;
    }

    private void doWorkflowTimeoutAlert(final IWorkflowExecution workflowExecution) {
        final WorkflowInstance workflowInstance = workflowExecution.getWorkflowInstance();
        workflowAlertManager.sendWorkflowTimeoutAlert(workflowInstance);
    }
}
