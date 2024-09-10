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

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
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
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskStartLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskSuccessLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * The action to be taken when a task is in the Failure state.
 */
@Slf4j
@Component
public class TaskFailureStateAction extends AbstractTaskStateAction {

    @Override
    public void startEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                 final ITaskExecutionRunnable taskExecutionRunnable,
                                 final TaskStartLifecycleEvent taskStartEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        final TaskFailedLifecycleEvent taskFailedEvent = TaskFailedLifecycleEvent.builder()
                .taskExecutionRunnable(taskExecutionRunnable)
                .endTime(taskExecutionRunnable.getTaskInstance().getEndTime())
                .build();
        taskExecutionRunnable.getWorkflowEventBus().publish(taskFailedEvent);
    }

    @Override
    public void startedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                   final ITaskExecutionRunnable taskExecutionRunnable,
                                   final TaskRunningLifecycleEvent taskRunningEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        logWarningIfCannotDoAction(taskExecutionRunnable, taskRunningEvent);
    }

    @Override
    public void retryEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                 final ITaskExecutionRunnable taskExecutionRunnable,
                                 final TaskRetryLifecycleEvent taskRetryEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        // check the retry times
        if (!taskExecutionRunnable.isTaskInstanceCanRetry()) {
            log.info("The task: {} cannot retry, because the retry times: {} is over the max retry times: {}",
                    taskInstance.getName(),
                    taskInstance.getRetryTimes(),
                    taskInstance.getMaxRetryTimes());
            return;
        }
        taskExecutionRunnable.retry();
    }

    @Override
    public void dispatchEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                    final ITaskExecutionRunnable taskExecutionRunnable,
                                    final TaskDispatchLifecycleEvent taskDispatchEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        logWarningIfCannotDoAction(taskExecutionRunnable, taskDispatchEvent);
    }

    @Override
    public void dispatchedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                      final ITaskExecutionRunnable taskExecutionRunnable,
                                      final TaskDispatchedLifecycleEvent taskDispatchedEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        logWarningIfCannotDoAction(taskExecutionRunnable, taskDispatchedEvent);
    }

    @Override
    public void pauseEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                 final ITaskExecutionRunnable taskExecutionRunnable,
                                 final TaskPauseLifecycleEvent taskPauseEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        logWarningIfCannotDoAction(taskExecutionRunnable, taskPauseEvent);
    }

    @Override
    public void pausedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                  final ITaskExecutionRunnable taskExecutionRunnable,
                                  final TaskPausedLifecycleEvent taskPausedEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        final IWorkflowExecutionGraph workflowExecutionGraph = taskExecutionRunnable.getWorkflowExecutionGraph();
        // This case happen when the task is failure but the task is in delay retry queue.
        // We don't remove the event in GlobalWorkflowDelayEventCoordinator the event should be dropped when the task is
        // killed.
        if (taskExecutionRunnable.isTaskInstanceCanRetry()
                && workflowExecutionGraph.isTaskExecutionRunnableActive(taskExecutionRunnable)) {
            workflowExecutionGraph.markTaskExecutionRunnableChainPause(taskExecutionRunnable);
            publishWorkflowInstanceTopologyLogicalTransitionEvent(taskExecutionRunnable);
            return;
        }
        logWarningIfCannotDoAction(taskExecutionRunnable, taskPausedEvent);
    }

    @Override
    public void killEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                final ITaskExecutionRunnable taskExecutionRunnable,
                                final TaskKillLifecycleEvent taskKillEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        logWarningIfCannotDoAction(taskExecutionRunnable, taskKillEvent);
    }

    @Override
    public void killedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                  final ITaskExecutionRunnable taskExecutionRunnable,
                                  final TaskKilledLifecycleEvent taskKilledEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        final IWorkflowExecutionGraph workflowExecutionGraph = taskExecutionRunnable.getWorkflowExecutionGraph();
        // This case happen when the task is failure but the task is in delay retry queue.
        // We don't remove the event in GlobalWorkflowDelayEventCoordinator the event should be dropped when the task is
        // killed.
        if (taskExecutionRunnable.isTaskInstanceCanRetry()
                && workflowExecutionGraph.isTaskExecutionRunnableActive(taskExecutionRunnable)) {
            workflowExecutionGraph.markTaskExecutionRunnableChainKill(taskExecutionRunnable);
            publishWorkflowInstanceTopologyLogicalTransitionEvent(taskExecutionRunnable);
            return;
        }
        logWarningIfCannotDoAction(taskExecutionRunnable, taskKilledEvent);
    }

    @Override
    public void failedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                  final ITaskExecutionRunnable taskExecutionRunnable,
                                  final TaskFailedLifecycleEvent taskFailedEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        super.failedEventAction(workflowExecutionRunnable, taskExecutionRunnable, taskFailedEvent);
    }

    @Override
    public void succeedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                   final ITaskExecutionRunnable taskExecutionRunnable,
                                   final TaskSuccessLifecycleEvent taskSuccessEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        logWarningIfCannotDoAction(taskExecutionRunnable, taskSuccessEvent);
    }

    @Override
    public void failoverEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                    final ITaskExecutionRunnable taskExecutionRunnable,
                                    final TaskFailoverLifecycleEvent taskFailoverEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        logWarningIfCannotDoAction(taskExecutionRunnable, taskFailoverEvent);
    }

    @Override
    public TaskExecutionStatus matchState() {
        return TaskExecutionStatus.FAILURE;
    }
}
