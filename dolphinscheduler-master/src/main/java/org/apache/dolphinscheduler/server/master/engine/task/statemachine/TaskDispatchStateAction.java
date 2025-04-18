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

import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.task.client.TaskExecutorClient;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskDispatchStateAction extends AbstractTaskStateAction {

    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private TaskExecutorClient taskExecutorClient;

    @Override
    public void startEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                 final ITaskExecutionRunnable taskExecutionRunnable,
                                 final TaskStartLifecycleEvent taskStartEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        taskExecutionRunnable.getWorkflowEventBus().publish(TaskFailoverLifecycleEvent.of(taskExecutionRunnable));
    }

    @Override
    public void startedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                   final ITaskExecutionRunnable taskExecutionRunnable,
                                   final TaskRunningLifecycleEvent taskRunningEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        persistentTaskInstanceStartedEventToDB(taskExecutionRunnable, taskRunningEvent);
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
        taskExecutorClient.pause(taskExecutionRunnable);
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
        taskExecutorClient.kill(taskExecutionRunnable);
    }

    @Override
    public void killedEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                  final ITaskExecutionRunnable taskExecutionRunnable,
                                  final TaskKilledLifecycleEvent event) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        super.killedEventAction(workflowExecutionRunnable, taskExecutionRunnable, event);
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
        super.succeedEventAction(workflowExecutionRunnable, taskExecutionRunnable, taskSuccessEvent);
    }

    @Override
    public void failoverEventAction(final IWorkflowExecutionRunnable workflowExecutionRunnable,
                                    final ITaskExecutionRunnable taskExecutionRunnable,
                                    final TaskFailoverLifecycleEvent taskFailoverEvent) {
        throwExceptionIfStateIsNotMatch(taskExecutionRunnable);
        super.failoverTask(taskExecutionRunnable);
    }

    @Override
    public TaskExecutionStatus matchState() {
        return TaskExecutionStatus.DISPATCH;
    }
}
