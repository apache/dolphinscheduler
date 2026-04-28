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

package org.apache.dolphinscheduler.server.master.engine.workflow.statemachine;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowFailedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowFinalizeLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowPauseLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowPausedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowStartLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowStopLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowStoppedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowSucceedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowPausedStateAction extends AbstractWorkflowStateAction {

    @Override
    public void onStartEvent(final IWorkflowExecution workflowExecution,
                             final WorkflowStartLifecycleEvent workflowStartEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecution);
        logWarningIfCannotDoAction(workflowExecution, workflowStartEvent);
    }

    @Override
    public void onTopologyLogicalTransitionEvent(
                                                 final IWorkflowExecution workflowExecution,
                                                 final WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent workflowTopologyLogicalTransitionWithTaskFinishEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecution);
        logWarningIfCannotDoAction(workflowExecution, workflowTopologyLogicalTransitionWithTaskFinishEvent);
    }

    @Override
    public void onPauseEvent(final IWorkflowExecution workflowExecution,
                             final WorkflowPauseLifecycleEvent workflowPauseEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecution);
        logWarningIfCannotDoAction(workflowExecution, workflowPauseEvent);
    }

    @Override
    public void onPausedEvent(final IWorkflowExecution workflowExecution,
                              final WorkflowPausedLifecycleEvent workflowPausedEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecution);
        logWarningIfCannotDoAction(workflowExecution, workflowPausedEvent);
    }

    @Override
    public void onStopEvent(final IWorkflowExecution workflowExecution,
                            final WorkflowStopLifecycleEvent workflowStopEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecution);
        logWarningIfCannotDoAction(workflowExecution, workflowStopEvent);
    }

    @Override
    public void onStoppedEvent(final IWorkflowExecution workflowExecution,
                               final WorkflowStoppedLifecycleEvent workflowStoppedEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecution);
        logWarningIfCannotDoAction(workflowExecution, workflowStoppedEvent);
    }

    @Override
    public void onSucceedEvent(final IWorkflowExecution workflowExecution,
                               final WorkflowSucceedLifecycleEvent workflowSucceedEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecution);
        logWarningIfCannotDoAction(workflowExecution, workflowSucceedEvent);
    }

    @Override
    public void onFailedEvent(final IWorkflowExecution workflowExecution,
                              final WorkflowFailedLifecycleEvent workflowFailedEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecution);
        logWarningIfCannotDoAction(workflowExecution, workflowFailedEvent);
    }

    @Override
    public void onFinalizeEvent(final IWorkflowExecution workflowExecution,
                                final WorkflowFinalizeLifecycleEvent workflowFinalizeEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecution);
        super.finalizeEventAction(workflowExecution);
    }

    @Override
    public WorkflowExecutionStatus matchState() {
        return WorkflowExecutionStatus.PAUSE;
    }

    /**
     * The running state can only finish with success/failure.
     */
    @Override
    protected void emitWorkflowFinishedEventIfApplicable(IWorkflowExecution workflowExecution) {
        throw new IllegalStateException(
                "The workflow " + workflowExecution.getName() +
                        "is paused, shouldn't emit workflow finished event");
    }
}
