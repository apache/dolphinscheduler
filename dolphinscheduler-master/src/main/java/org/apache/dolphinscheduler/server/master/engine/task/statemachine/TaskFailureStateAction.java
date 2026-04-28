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
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
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
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * The action to be taken when a task is in the Failure state.
 */
@Slf4j
@Component
public class TaskFailureStateAction extends AbstractTaskStateAction {

    @Override
    public void onStartEvent(final IWorkflowExecution workflowExecution,
                             final ITaskExecution taskExecution,
                             final TaskStartLifecycleEvent taskStartEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        final TaskFailedLifecycleEvent taskFailedEvent = TaskFailedLifecycleEvent.builder()
                .taskExecution(taskExecution)
                .endTime(taskExecution.getTaskInstance().getEndTime())
                .build();
        taskExecution.getWorkflowEventBus().publish(taskFailedEvent);
    }

    @Override
    public void onStartedEvent(final IWorkflowExecution workflowExecution,
                               final ITaskExecution taskExecution,
                               final TaskRunningLifecycleEvent taskRunningEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        logWarningIfCannotDoAction(taskExecution, taskRunningEvent);
    }

    @Override
    public void onRetryEvent(final IWorkflowExecution workflowExecution,
                             final ITaskExecution taskExecution,
                             final TaskRetryLifecycleEvent taskRetryEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        // check the retry times
        if (!taskExecution.isTaskInstanceCanRetry()) {
            log.info("The task: {} cannot retry, because the retry times: {} is over the max retry times: {}",
                    taskInstance.getName(),
                    taskInstance.getRetryTimes(),
                    taskInstance.getMaxRetryTimes());
            return;
        }
        taskExecution.retry();
    }

    @Override
    public void onDispatchEvent(final IWorkflowExecution workflowExecution,
                                final ITaskExecution taskExecution,
                                final TaskDispatchLifecycleEvent taskDispatchEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        logWarningIfCannotDoAction(taskExecution, taskDispatchEvent);
    }

    @Override
    public void onDispatchedEvent(final IWorkflowExecution workflowExecution,
                                  final ITaskExecution taskExecution,
                                  final TaskDispatchedLifecycleEvent taskDispatchedEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        logWarningIfCannotDoAction(taskExecution, taskDispatchedEvent);
    }

    @Override
    public void onPauseEvent(final IWorkflowExecution workflowExecution,
                             final ITaskExecution taskExecution,
                             final TaskPauseLifecycleEvent taskPauseEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        // When the failed task is awaiting retry, we can mark it as 'paused' to ignore the retry event.
        if (isTaskRetrying(taskExecution)) {
            super.onPausedEvent(workflowExecution, taskExecution,
                    TaskPausedLifecycleEvent.of(taskExecution));
            return;
        }
        logWarningIfCannotDoAction(taskExecution, taskPauseEvent);
    }

    @Override
    public void onPausedEvent(final IWorkflowExecution workflowExecution,
                              final ITaskExecution taskExecution,
                              final TaskPausedLifecycleEvent taskPausedEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        // This case happen when the task is failure but the task is in delay retry queue.
        // We don't remove the event in GlobalWorkflowDelayEventCoordinator the event should be dropped when the task is
        // killed.
        if (isTaskRetrying(taskExecution)) {
            super.onPausedEvent(workflowExecution, taskExecution, taskPausedEvent);
            return;
        }
        logWarningIfCannotDoAction(taskExecution, taskPausedEvent);
    }

    @Override
    public void onKillEvent(final IWorkflowExecution workflowExecution,
                            final ITaskExecution taskExecution,
                            final TaskKillLifecycleEvent taskKillEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        // When the failed task is awaiting retry, we can mark it as 'killed' to ignore the retry event.
        if (isTaskRetrying(taskExecution)) {
            super.onKilledEvent(workflowExecution, taskExecution,
                    TaskKilledLifecycleEvent.of(taskExecution));
            return;
        }
        logWarningIfCannotDoAction(taskExecution, taskKillEvent);
    }

    @Override
    public void onKilledEvent(final IWorkflowExecution workflowExecution,
                              final ITaskExecution taskExecution,
                              final TaskKilledLifecycleEvent taskKilledEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        // This case happen when the task is failure but the task is in delay retry queue.
        // We don't remove the event in GlobalWorkflowDelayEventCoordinator the event should be dropped when the task is
        // killed.
        if (isTaskRetrying(taskExecution)) {
            super.onKilledEvent(workflowExecution, taskExecution, taskKilledEvent);
            return;
        }
        logWarningIfCannotDoAction(taskExecution, taskKilledEvent);
    }

    @Override
    public void onFailedEvent(final IWorkflowExecution workflowExecution,
                              final ITaskExecution taskExecution,
                              final TaskFailedLifecycleEvent taskFailedEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        super.onFailedEvent(workflowExecution, taskExecution, taskFailedEvent);
    }

    @Override
    public void onSucceedEvent(final IWorkflowExecution workflowExecution,
                               final ITaskExecution taskExecution,
                               final TaskSuccessLifecycleEvent taskSuccessEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        logWarningIfCannotDoAction(taskExecution, taskSuccessEvent);
    }

    @Override
    public void onFailoverEvent(final IWorkflowExecution workflowExecution,
                                final ITaskExecution taskExecution,
                                final TaskFailoverLifecycleEvent taskFailoverEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        logWarningIfCannotDoAction(taskExecution, taskFailoverEvent);
    }

    @Override
    public TaskExecutionStatus matchState() {
        return TaskExecutionStatus.FAILURE;
    }

    private boolean isTaskRetrying(final ITaskExecution taskExecution) {
        final IWorkflowExecutionGraph workflowExecutionGraph = taskExecution.getWorkflowExecutionGraph();
        return workflowExecutionGraph.isTaskExecutionRetrying(taskExecution);
    }
}
