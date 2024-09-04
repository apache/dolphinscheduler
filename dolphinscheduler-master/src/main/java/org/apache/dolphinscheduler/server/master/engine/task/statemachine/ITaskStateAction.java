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

package org.apache.dolphinscheduler.server.master.engine.task.statemachine;

import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskDispatchLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFailedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFailoverLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskKillLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskKilledLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskPauseLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskPausedLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRetryLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRunningLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskRuntimeContextChangedEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskStartLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskSuccessLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;

/**
 * Represents the action to be taken when a task is in a certain state and receive a target event.
 * <p> Each {@link TaskExecutionStatus} should have a corresponding {@link ITaskStateAction} implementation.
 *
 * @see TaskSubmittedStateAction
 * @see TaskDelayExecutionStateAction
 * @see TaskDispatchStateAction
 * @see TaskRunningStateAction
 * @see TaskPauseStateAction
 * @see TaskKillStateAction
 * @see TaskFailureStateAction
 * @see TaskSuccessStateAction
 * @see TaskForceSuccessStateAction
 * @see TaskFailoverStateAction
 */
public interface ITaskStateAction {

    /**
     * Perform the necessary actions when the task in a certain state receive a {@link TaskStartLifecycleEvent}.
     * <p> This method is called when you want to start a task.
     */
    void startEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                          final ITaskExecutionRunnable taskExecutionRunnable,
                          final TaskStartLifecycleEvent taskStartEvent);

    /**
     * Perform the necessary actions when the task in a certain state receive a {@link TaskRunningLifecycleEvent}.
     * <p> This method is called when the master receive task running event from executor.
     */
    void startedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                            final ITaskExecutionRunnable taskExecutionRunnable,
                            final TaskRunningLifecycleEvent taskRunningEvent);

    /**
     * Perform the necessary actions when the task in a certain state receive a {@link TaskRuntimeContextChangedEvent}.
     * <p> This method is called when the master receive task runtime context changed event from executor.
     */
    void runtimeContextChangedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                          final ITaskExecutionRunnable taskExecutionRunnable,
                                          final TaskRuntimeContextChangedEvent taskRuntimeContextChangedEvent);

    /**
     * Perform the necessary actions when the task in a certain state receive a {@link TaskRetryLifecycleEvent}.
     * <p> This method is called when the task need to retry.
     */
    void retryEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                          final ITaskExecutionRunnable taskExecutionRunnable,
                          final TaskRetryLifecycleEvent taskRetryEvent);

    /**
     * Perform the necessary actions when the task in a certain state receive a {@link TaskDispatchLifecycleEvent}.
     * <p> This method is called when you want to dispatch a task.
     */
    void dispatchEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                             final ITaskExecutionRunnable taskExecutionRunnable,
                             final TaskDispatchLifecycleEvent taskDispatchEvent);

    /**
     * Perform the necessary actions when the task in a certain state receive a {@link TaskDispatchedLifecycleEvent}.
     * <p> This method is called when the task has been dispatched to executor.
     */
    void dispatchedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                               final ITaskExecutionRunnable taskExecutionRunnable,
                               final TaskDispatchedLifecycleEvent taskDispatchedEvent);

    /**
     * Perform the necessary actions when the task in a certain state receive a {@link TaskPauseLifecycleEvent}.
     * <p> This method is called when you want to pause a task.
     */
    void pauseEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                          final ITaskExecutionRunnable taskExecutionRunnable,
                          final TaskPauseLifecycleEvent taskPauseEvent);

    /**
     * Perform the necessary actions when the task in a certain state receive a {@link TaskPausedLifecycleEvent}.
     * <p> This method is called when the task has been paused.
     */
    void pausedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                           final ITaskExecutionRunnable taskExecutionRunnable,
                           final TaskPausedLifecycleEvent taskPausedEvent);

    /**
     * Perform the necessary actions when the task in a certain state receive a {@link TaskKillLifecycleEvent}.
     * <p> This method is called when you want to kill a task.
     */
    void killEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                         final ITaskExecutionRunnable taskExecutionRunnable,
                         final TaskKillLifecycleEvent taskKillEvent);

    /**
     * Perform the necessary actions when the task in a certain state receive a {@link TaskKilledLifecycleEvent}.
     * <p> This method is called when the task has been killed.
     */
    void killedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                           final ITaskExecutionRunnable taskExecutionRunnable,
                           final TaskKilledLifecycleEvent taskKilledEvent);

    /**
     * Perform the necessary actions when the task in a certain state receive a {@link TaskFailedLifecycleEvent}.
     * <p> This method is called when the task has been failed.
     */
    void failedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                           final ITaskExecutionRunnable taskExecutionRunnable,
                           final TaskFailedLifecycleEvent taskFailedEvent);

    /**
     * Perform the necessary actions when the task in a certain state receive a {@link TaskSuccessLifecycleEvent}.
     * <p> This method is called when the task has been success.
     */
    void succeedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                            final ITaskExecutionRunnable taskExecutionRunnable,
                            final TaskSuccessLifecycleEvent taskSuccessEvent);

    /**
     * Perform the necessary actions when the task in a certain state receive a {@link TaskFailoverLifecycleEvent}.
     * <p> This method is called when the task need to failover.
     */
    void failoverEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                             final ITaskExecutionRunnable taskExecutionRunnable,
                             final TaskFailoverLifecycleEvent taskFailoverEvent);

    /**
     * Get the {@link TaskExecutionStatus} that this action match.
     */
    TaskExecutionStatus matchState();
}
