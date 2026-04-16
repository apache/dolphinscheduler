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

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.task.dispatcher.WorkerGroupDispatcherCoordinator;
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
import org.apache.dolphinscheduler.server.master.exception.TaskExecutionContextCreateException;
import org.apache.dolphinscheduler.server.master.utils.ExceptionUtils;

import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The action to be taken when a task is in the SUBMITTED state.
 */
@Slf4j
@Component
public class TaskSubmittedStateAction extends AbstractTaskStateAction {

    @Autowired
    private WorkerGroupDispatcherCoordinator workerGroupDispatcherCoordinator;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Override
    public void onStartEvent(final IWorkflowExecution workflowExecution,
                             final ITaskExecution taskExecution,
                             final TaskStartLifecycleEvent taskStartEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);

        if (workflowExecution.isWorkflowReadyPause()) {
            workflowExecution.getWorkflowEventBus().publish(TaskPausedLifecycleEvent.of(taskExecution));
            return;
        }

        if (workflowExecution.isWorkflowReadyStop()) {
            workflowExecution.getWorkflowEventBus().publish(TaskKilledLifecycleEvent.of(taskExecution));
            return;
        }

        tryToDispatchTask(taskExecution);
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
        logWarningIfCannotDoAction(taskExecution, taskRetryEvent);
    }

    @Override
    public void onDispatchEvent(final IWorkflowExecution workflowExecution,
                                final ITaskExecution taskExecution,
                                final TaskDispatchLifecycleEvent taskDispatchEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        final TaskInstance taskInstance = taskExecution.getTaskInstance();
        long remainTimeMills = DateUtils.getRemainTime(
                taskInstance.getFirstSubmitTime(),
                taskInstance.getDelayTime() * 60L) * 1_000;
        if (remainTimeMills > 0) {
            taskInstance.setState(TaskExecutionStatus.DELAY_EXECUTION);
            taskInstanceDao.updateById(taskInstance);
            log.info("Current taskInstance: {} is choose delay execution, delay time: {}/min, remainTime: {}/ms",
                    taskInstance.getName(),
                    taskInstance.getDelayTime(),
                    remainTimeMills);
        }

        try {
            taskExecution.initializeTaskExecutionContext();
        } catch (Exception ex) {
            if (ExceptionUtils.isDatabaseConnectedFailedException(ex)) {
                throw ex;
            }
            log.error("Failed to initialize task execution context, taskName: {}", taskInstance.getName(), ex);
            throw new TaskExecutionContextCreateException(ex.getMessage());
        }

        workerGroupDispatcherCoordinator.dispatchTask(taskExecution, remainTimeMills);
    }

    @Override
    public void onDispatchedEvent(final IWorkflowExecution workflowExecution,
                                  final ITaskExecution taskExecution,
                                  final TaskDispatchedLifecycleEvent taskDispatchedEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        super.onDispatchedEvent(workflowExecution, taskExecution, taskDispatchedEvent);
    }

    @Override
    public void onPauseEvent(final IWorkflowExecution workflowExecution,
                             final ITaskExecution taskExecution,
                             final TaskPauseLifecycleEvent taskPauseEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        if (workerGroupDispatcherCoordinator.removeTask(taskExecution)) {
            log.info("Success pause task: {} before dispatch", taskExecution.getName());
            taskExecution.getWorkflowEventBus().publish(TaskPausedLifecycleEvent.of(taskExecution));
            return;
        }
        log.info("The task[id={}] is submitted and already dispatched, cannot pause, will try to pause it after 5s",
                taskExecution.getId());
        taskExecution.getWorkflowEventBus()
                .publish(TaskPauseLifecycleEvent.of(taskExecution, TimeUnit.SECONDS.toMillis(5)));
    }

    @Override
    public void onPausedEvent(final IWorkflowExecution workflowExecution,
                              final ITaskExecution taskExecution,
                              final TaskPausedLifecycleEvent taskPausedEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        super.onPausedEvent(workflowExecution, taskExecution, taskPausedEvent);
    }

    @Override
    public void onKillEvent(final IWorkflowExecution workflowExecution,
                            final ITaskExecution taskExecution,
                            final TaskKillLifecycleEvent taskKillEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        if (workerGroupDispatcherCoordinator.removeTask(taskExecution)) {
            log.info("Success kill task[id={}] before dispatch", taskExecution.getId());
            taskExecution.getWorkflowEventBus().publish(TaskKilledLifecycleEvent.of(taskExecution));
            return;
        }
        log.info("The task[id={}] is submitted and already dispatched, cannot kill, will kill it after 5s",
                taskExecution.getId());
        taskExecution.getWorkflowEventBus()
                .publish(TaskKillLifecycleEvent.of(taskExecution, TimeUnit.SECONDS.toMillis(5)));
    }

    @Override
    public void onKilledEvent(final IWorkflowExecution workflowExecution,
                              final ITaskExecution taskExecution,
                              final TaskKilledLifecycleEvent taskKilledEvent) {
        throwExceptionIfStateIsNotMatch(taskExecution);
        super.onKilledEvent(workflowExecution, taskExecution, taskKilledEvent);
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
        return TaskExecutionStatus.SUBMITTED_SUCCESS;
    }

}
