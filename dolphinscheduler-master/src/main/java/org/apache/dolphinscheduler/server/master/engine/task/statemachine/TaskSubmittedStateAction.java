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
import org.apache.dolphinscheduler.server.master.engine.TaskGroupCoordinator;
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
import org.apache.dolphinscheduler.server.master.runner.GlobalTaskDispatchWaitingQueue;

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
    private GlobalTaskDispatchWaitingQueue globalTaskDispatchWaitingQueue;

    @Autowired
    private TaskGroupCoordinator taskGroupCoordinator;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Override
    public void startEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                 final ITaskExecutionRunnable taskExecutionRunnable,
                                 final TaskStartLifecycleEvent taskStartEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);

        if (workflowExecutionRunnable.isWorkflowReadyPause()) {
            workflowExecutionRunnable.getWorkflowEventBus().publish(TaskPausedLifecycleEvent.of(taskExecutionRunnable));
            return;
        }

        if (workflowExecutionRunnable.isWorkflowReadyStop()) {
            workflowExecutionRunnable.getWorkflowEventBus().publish(TaskKilledLifecycleEvent.of(taskExecutionRunnable));
            return;
        }

        tryToDispatchTask(taskExecutionRunnable);
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
        logWarningIfCannotDoAction(taskExecutionRunnable, taskRetryEvent);
    }

    @Override
    public void dispatchEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                    final ITaskExecutionRunnable taskExecutionRunnable,
                                    final TaskDispatchLifecycleEvent taskDispatchEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
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
        globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnableWithDelay(taskExecutionRunnable, remainTimeMills);
    }

    @Override
    public void dispatchedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                      final ITaskExecutionRunnable taskExecutionRunnable,
                                      final TaskDispatchedLifecycleEvent taskDispatchedEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        super.dispatchedEventAction(workflowExecutionRunnable, taskExecutionRunnable, taskDispatchedEvent);
    }

    @Override
    public void pauseEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                 final ITaskExecutionRunnable taskExecutionRunnable,
                                 final TaskPauseLifecycleEvent taskPauseEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        if (globalTaskDispatchWaitingQueue.markTaskExecutionRunnableRemoved(taskExecutionRunnable)) {
            log.info("Success pause task: {} before dispatch", taskExecutionRunnable.getName());
            taskExecutionRunnable.getWorkflowEventBus().publish(TaskPausedLifecycleEvent.of(taskExecutionRunnable));
            return;
        }
        logWarningIfCannotDoAction(taskExecutionRunnable, taskPauseEvent);
    }

    @Override
    public void pausedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                  final ITaskExecutionRunnable taskExecutionRunnable,
                                  final TaskPausedLifecycleEvent taskPausedEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        super.pausedEventAction(workflowExecutionRunnable, taskExecutionRunnable, taskPausedEvent);
    }

    @Override
    public void killEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                final ITaskExecutionRunnable taskExecutionRunnable,
                                final TaskKillLifecycleEvent taskKillEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        if (globalTaskDispatchWaitingQueue.markTaskExecutionRunnableRemoved(taskExecutionRunnable)) {
            log.info("Success kill task: {} before dispatch", taskExecutionRunnable.getName());
            taskExecutionRunnable.getWorkflowEventBus().publish(TaskKilledLifecycleEvent.of(taskExecutionRunnable));
            return;
        }
        logWarningIfCannotDoAction(taskExecutionRunnable, taskKillEvent);
    }

    @Override
    public void killedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                  final ITaskExecutionRunnable taskExecutionRunnable,
                                  final TaskKilledLifecycleEvent taskKilledEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        super.killedEventAction(workflowExecutionRunnable, taskExecutionRunnable, taskKilledEvent);
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
        return TaskExecutionStatus.SUBMITTED_SUCCESS;
    }

}
