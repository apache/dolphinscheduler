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
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowFailedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowFinalizeLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowPauseLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowPausedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowStartLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowStopLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowStoppedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowSucceedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.event.WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowReadyPauseStateAction extends AbstractWorkflowStateAction {

    @Override
    public void startEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                 final WorkflowStartLifecycleEvent workflowStartEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        final IWorkflowExecutionGraph workflowExecutionGraph =
                workflowExecutionRunnable.getWorkflowExecuteContext().getWorkflowExecutionGraph();
        triggerTasks(workflowExecutionRunnable, workflowExecutionGraph.getStartNodes());
    }

    @Override
    public void topologyLogicalTransitionEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                                     final WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent workflowTopologyLogicalTransitionWithTaskFinishEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        super.onTaskFinish(workflowExecutionRunnable,
                workflowTopologyLogicalTransitionWithTaskFinishEvent.getTaskExecutionRunnable());
    }

    @Override
    public void pauseEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                 final WorkflowPauseLifecycleEvent workflowPauseEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        logWarningIfCannotDoAction(workflowExecutionRunnable, workflowPauseEvent);
    }

    @Override
    public void pausedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                  final WorkflowPausedLifecycleEvent workflowPausedEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        workflowFinish(workflowExecutionRunnable, WorkflowExecutionStatus.PAUSE);

    }

    @Override
    public void stopEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                final WorkflowStopLifecycleEvent workflowStopEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        logWarningIfCannotDoAction(workflowExecutionRunnable, workflowStopEvent);
    }

    @Override
    public void stoppedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                   final WorkflowStoppedLifecycleEvent workflowStoppedEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        logWarningIfCannotDoAction(workflowExecutionRunnable, workflowStoppedEvent);
    }

    @Override
    public void succeedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                   final WorkflowSucceedLifecycleEvent workflowSucceedEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        super.workflowFinish(workflowExecutionRunnable, WorkflowExecutionStatus.SUCCESS);
    }

    @Override
    public void failedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                  final WorkflowFailedLifecycleEvent workflowFailedEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        super.workflowFinish(workflowExecutionRunnable, WorkflowExecutionStatus.FAILURE);
    }

    @Override
    public void finalizeEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                    final WorkflowFinalizeLifecycleEvent workflowFinalizeEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        logWarningIfCannotDoAction(workflowExecutionRunnable, workflowFinalizeEvent);
    }

    @Override
    public WorkflowExecutionStatus matchState() {
        return WorkflowExecutionStatus.READY_PAUSE;
    }

    @Override
    protected void emitWorkflowFinishedEventIfApplicable(final IWorkflowExecutionRunnable workflowExecutionRunnable) {
        final IWorkflowExecutionGraph workflowExecutionGraph = workflowExecutionRunnable.getWorkflowExecutionGraph();
        if (!workflowExecutionGraph.isAllTaskExecutionRunnableChainFinish()) {
            log.debug("There exist task which is not finish, don't need to emit workflow finished event");
            return;
        }

        final WorkflowEventBus workflowEventBus = workflowExecutionRunnable.getWorkflowEventBus();
        if (workflowExecutionGraph.isExistFailureTaskExecutionRunnableChain()) {
            workflowEventBus.publish(WorkflowFailedLifecycleEvent.of(workflowExecutionRunnable));
            return;
        }

        if (workflowExecutionGraph.isExistPauseTaskExecutionRunnableChain()) {
            workflowEventBus.publish(WorkflowPausedLifecycleEvent.of(workflowExecutionRunnable));
            return;
        }

        if (workflowExecutionGraph.isAllTaskExecutionRunnableChainSuccess()) {
            workflowEventBus.publish(WorkflowSucceedLifecycleEvent.of(workflowExecutionRunnable));
            return;
        }

        throw new IllegalStateException(
                "The workflow: " + workflowExecutionRunnable.getName() + " state is "
                        + workflowExecutionRunnable.getState()
                        + " can only finish with success/failed/paused but exist task chain which state is not success/failure/pause");
    }
}
