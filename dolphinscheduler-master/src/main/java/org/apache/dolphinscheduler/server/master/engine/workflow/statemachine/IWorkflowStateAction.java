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

/**
 * Represents the action to be taken when a workflow is in a certain state and receive a target event.
 * <p> Each {@link WorkflowExecutionStatus} should have a corresponding {@link IWorkflowStateAction} implementation.
 *
 * @see WorkflowSubmittedStateAction
 * @see WorkflowRunningStateAction
 * @see WorkflowReadyPauseStateAction
 * @see WorkflowPausedStateAction
 * @see WorkflowReadyStopStateAction
 * @see WorkflowStoppedStateAction
 * @see WorkflowSerialWaitStateAction
 * @see WorkflowFailedStateAction
 * @see WorkflowSuccessStateAction
 * @see WorkflowFailoverStateAction
 * @see WorkflowWaitToRunStateAction
 */
public interface IWorkflowStateAction {

    /**
     * Perform the necessary actions when the workflow in a certain state receive a {@link WorkflowStartLifecycleEvent}.
     */
    void startEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                          final WorkflowStartLifecycleEvent workflowStartEvent);

    /**
     * Perform the necessary actions when the workflow in a certain state receive a {@link WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent}.
     */
    void topologyLogicalTransitionEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                              final WorkflowTopologyLogicalTransitionWithTaskFinishLifecycleEvent workflowTopologyLogicalTransitionWithTaskFinishEvent);

    /**
     * Perform the necessary actions when the workflow in a certain state receive a {@link WorkflowPauseLifecycleEvent}.
     */
    void pauseEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                          final WorkflowPauseLifecycleEvent workflowPauseEvent);

    /**
     * Perform the necessary actions when the workflow in a certain state receive a {@link WorkflowStopLifecycleEvent}.
     */
    void pausedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                           final WorkflowPausedLifecycleEvent workflowPausedEvent);

    /**
     * Perform the necessary actions when the workflow in a certain state receive a {@link WorkflowStopLifecycleEvent}.
     */
    void stopEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                         final WorkflowStopLifecycleEvent workflowStopEvent);

    /**
     * Perform the necessary actions when the workflow in a certain state receive a {@link WorkflowStoppedLifecycleEvent}.
     */
    void stoppedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                            final WorkflowStoppedLifecycleEvent workflowStoppedEvent);

    /**
     * Perform the necessary actions when the workflow in a certain state receive a {@link WorkflowSucceedLifecycleEvent}.
     */
    void succeedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                            final WorkflowSucceedLifecycleEvent workflowSucceedEvent);

    /**
     * Perform the necessary actions when the workflow in a certain state receive a {@link WorkflowFailedLifecycleEvent}.
     */
    void failedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                           final WorkflowFailedLifecycleEvent workflowFailedEvent);

    /**
     * Perform the necessary actions when the workflow in a certain state receive a {@link WorkflowFinalizeLifecycleEvent}.
     */
    void finalizeEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                             final WorkflowFinalizeLifecycleEvent workflowFinalizeEvent);

    /**
     * Get the {@link WorkflowExecutionStatus} that this action match.
     */
    WorkflowExecutionStatus matchState();
}
