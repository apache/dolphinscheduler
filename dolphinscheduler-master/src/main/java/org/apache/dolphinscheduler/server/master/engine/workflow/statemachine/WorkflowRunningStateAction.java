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
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
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

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowRunningStateAction extends AbstractWorkflowStateAction {

    @Override
    public void onStartEvent(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                             final WorkflowStartLifecycleEvent workflowStartEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        final IWorkflowExecutionGraph workflowExecutionGraph =
                workflowExecutionRunnable.getWorkflowExecuteContext().getWorkflowExecutionGraph();
        final List<ITaskExecutionRunnable> startNodes = workflowExecutionGraph.getStartNodes();
        if (startNodes.isEmpty()) {
            log.info("Workflow start node is empty, try to emit workflow finished event");
            emitWorkflowFinishedEventIfApplicable(workflowExecutionRunnable);
            return;
        }
        triggerTasks(workflowExecutionRunnable, startNodes);
    }

    @Override
    public void onTopologyLogicalTransitionEvent(
                                                 final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                                 final WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent workflowTopologyLogicalTransitionWithTaskFinishEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        final ITaskExecutionRunnable taskExecutionRunnable =
                workflowTopologyLogicalTransitionWithTaskFinishEvent.getTaskExecutionRunnable();
        workflowExecutionRunnable.getWorkflowExecutionGraph().markTaskExecutionRunnableInActive(taskExecutionRunnable);
        super.tryToTriggerSuccessorsAfterTaskFinish(workflowExecutionRunnable, taskExecutionRunnable);
    }

    @Override
    public void onPauseEvent(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                             final WorkflowPauseLifecycleEvent workflowPauseEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        super.transformWorkflowInstanceState(workflowExecutionRunnable, WorkflowExecutionStatus.READY_PAUSE);
        super.pauseActiveTask(workflowExecutionRunnable);
    }

    @Override
    public void onPausedEvent(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                              final WorkflowPausedLifecycleEvent workflowPausedEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        logWarningIfCannotDoAction(workflowExecutionRunnable, workflowPausedEvent);
    }

    @Override
    public void onStopEvent(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                            final WorkflowStopLifecycleEvent workflowStopEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        super.transformWorkflowInstanceState(workflowExecutionRunnable, WorkflowExecutionStatus.READY_STOP);
        super.killActiveTask(workflowExecutionRunnable);
    }

    @Override
    public void onStoppedEvent(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                               final WorkflowStoppedLifecycleEvent workflowStoppedEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        // [Fix-17354]
        if (!workflowExecutionRunnable.getWorkflowExecutionGraph().isExistKilledTaskExecutionRunnableChain()) {
            throw new IllegalStateException(
                    "The workflow: " + workflowExecutionRunnable.getName()
                            + " does not exist tasks chain which is killed");
        }
        super.workflowFinish(workflowExecutionRunnable, WorkflowExecutionStatus.STOP);
    }

    @Override
    public void onSucceedEvent(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                               final WorkflowSucceedLifecycleEvent workflowSucceedEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        final IWorkflowExecutionGraph workflowExecutionGraph = workflowExecutionRunnable.getWorkflowExecutionGraph();
        if (!workflowExecutionGraph.isAllTaskExecutionRunnableChainSuccess()) {
            throw new IllegalStateException(
                    "The workflow: " + workflowExecutionRunnable.getName() + "exist tasks chain which is not success");
        }
        workflowFinish(workflowExecutionRunnable, WorkflowExecutionStatus.SUCCESS);
    }

    @Override
    public void onFailedEvent(IWorkflowExecutionRunnable workflowExecutionRunnable,
                              WorkflowFailedLifecycleEvent workflowFailedEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        final IWorkflowExecutionGraph workflowExecutionGraph = workflowExecutionRunnable.getWorkflowExecutionGraph();
        if (!workflowExecutionGraph.isExistFailureTaskExecutionRunnableChain()) {
            throw new IllegalStateException(
                    "The workflow: " + workflowExecutionRunnable.getName()
                            + " does not exist tasks chain which is failed");
        }
        workflowFinish(workflowExecutionRunnable, WorkflowExecutionStatus.FAILURE);
    }

    @Override
    public void onFinalizeEvent(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                final WorkflowFinalizeLifecycleEvent workflowFinalizeEvent) {
        throwExceptionIfStateIsNotMatch(workflowExecutionRunnable);
        logWarningIfCannotDoAction(workflowExecutionRunnable, workflowFinalizeEvent);
    }

    @Override
    public WorkflowExecutionStatus matchState() {
        return WorkflowExecutionStatus.RUNNING_EXECUTION;
    }

    /**
     * The running state can only finish with success/failure.
     */
    @Override
    protected void emitWorkflowFinishedEventIfApplicable(IWorkflowExecutionRunnable workflowExecutionRunnable) {
        if (!isWorkflowFinishable(workflowExecutionRunnable)) {
            log.debug("There exist task which is not finish, don't need to emit workflow finished event");
            return;
        }
        final IWorkflowExecutionGraph workflowExecutionGraph = workflowExecutionRunnable.getWorkflowExecutionGraph();
        final WorkflowEventBus workflowEventBus = workflowExecutionRunnable.getWorkflowEventBus();
        if (workflowExecutionGraph.isExistFailureTaskExecutionRunnableChain()) {
            workflowEventBus.publish(WorkflowFailedLifecycleEvent.of(workflowExecutionRunnable));
            return;
        }

        // [Fix-17354]
        // If there exist tasks which has set timeout failed, then will publish a kill event to kill the task.
        // So there might exist task which is killed, and the workflow instance state is running.
        // This is a special case, the workflow instance can transform from running to stop state.
        // Is there better way to handle this case?
        if (workflowExecutionGraph.isExistKilledTaskExecutionRunnableChain()) {
            workflowEventBus.publish(WorkflowStoppedLifecycleEvent.of(workflowExecutionRunnable));
            return;
        }

        if (workflowExecutionGraph.isAllTaskExecutionRunnableChainSuccess()) {
            workflowEventBus.publish(WorkflowSucceedLifecycleEvent.of(workflowExecutionRunnable));
            return;
        }

        throw new IllegalStateException("The workflow: " + workflowExecutionRunnable.getName() +
                " state is " + workflowExecutionRunnable.getState()
                + " can only finish with task success/failed/killed but exist task which state is not success、failure、killed");
    }
}
