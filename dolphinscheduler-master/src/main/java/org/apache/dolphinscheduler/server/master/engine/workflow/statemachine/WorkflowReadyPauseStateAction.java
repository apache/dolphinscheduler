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
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
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

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowReadyPauseStateAction extends AbstractWorkflowStateAction {

    @Override
    public void onStartEvent(final IWorkflowExecution workflowExecution,
                             final WorkflowStartLifecycleEvent workflowStartEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecution);
        final IWorkflowExecutionGraph workflowExecutionGraph =
                workflowExecution.getWorkflowExecuteContext().getWorkflowExecutionGraph();
        final List<ITaskExecution> startNodes = workflowExecutionGraph.getStartNodes();
        if (startNodes.isEmpty()) {
            log.info("Workflow start node is empty, try to emit workflow finished event");
            emitWorkflowFinishedEventIfApplicable(workflowExecution);
            return;
        }
        triggerTasks(workflowExecution, startNodes);
    }

    @Override
    public void onTopologyLogicalTransitionEvent(final IWorkflowExecution workflowExecution,
                                                 final WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent workflowTopologyLogicalTransitionWithTaskFinishEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecution);
        final ITaskExecution taskExecution =
                workflowTopologyLogicalTransitionWithTaskFinishEvent.getTaskExecution();
        workflowExecution.getWorkflowExecutionGraph().markTaskExecutionInActive(taskExecution);
        super.tryToTriggerSuccessorsAfterTaskFinish(workflowExecution, taskExecution);
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
        workflowFinish(workflowExecution, WorkflowExecutionStatus.PAUSE);

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
        super.workflowFinish(workflowExecution, WorkflowExecutionStatus.SUCCESS);
    }

    @Override
    public void onFailedEvent(final IWorkflowExecution workflowExecution,
                              final WorkflowFailedLifecycleEvent workflowFailedEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecution);
        super.workflowFinish(workflowExecution, WorkflowExecutionStatus.FAILURE);
    }

    @Override
    public void onFinalizeEvent(final IWorkflowExecution workflowExecution,
                                final WorkflowFinalizeLifecycleEvent workflowFinalizeEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecution);
        logWarningIfCannotDoAction(workflowExecution, workflowFinalizeEvent);
    }

    @Override
    public WorkflowExecutionStatus matchState() {
        return WorkflowExecutionStatus.READY_PAUSE;
    }

    @Override
    protected void emitWorkflowFinishedEventIfApplicable(final IWorkflowExecution workflowExecution) {
        if (!isWorkflowFinishable(workflowExecution)) {
            log.debug("There exist task which is not finish, don't need to emit workflow finished event");
            return;
        }
        final IWorkflowExecutionGraph workflowExecutionGraph = workflowExecution.getWorkflowExecutionGraph();
        final WorkflowEventBus workflowEventBus = workflowExecution.getWorkflowEventBus();
        if (workflowExecutionGraph.isExistPausedTaskExecutionChain()) {
            workflowEventBus.publish(WorkflowPausedLifecycleEvent.of(workflowExecution));
            return;
        }

        if (workflowExecutionGraph.isExistFailureTaskExecutionChain()) {
            workflowEventBus.publish(WorkflowFailedLifecycleEvent.of(workflowExecution));
            return;
        }

        if (workflowExecutionGraph.isAllTaskExecutionChainSuccess()) {
            workflowEventBus.publish(WorkflowSucceedLifecycleEvent.of(workflowExecution));
            return;
        }

        throw new IllegalStateException(
                "The workflow: " + workflowExecution.getName() + " state is "
                        + workflowExecution.getState()
                        + " can only finish with success/failed/paused but exist task chain which state is not success/failure/pause");
    }
}
