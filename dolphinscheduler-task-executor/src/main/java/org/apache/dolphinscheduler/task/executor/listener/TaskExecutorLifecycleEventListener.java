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

package org.apache.dolphinscheduler.task.executor.listener;

import org.apache.dolphinscheduler.plugin.task.api.log.TaskInstanceLogHeader;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;
import org.apache.dolphinscheduler.task.executor.ITaskExecutorRepository;
import org.apache.dolphinscheduler.task.executor.container.ITaskExecutorContainer;
import org.apache.dolphinscheduler.task.executor.container.ITaskExecutorContainerProvider;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;
import org.apache.dolphinscheduler.task.executor.events.IReportableTaskExecutorLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.ITaskExecutorLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorFailedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorFinalizeLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorKillLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorKilledLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorPauseLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorPausedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorRuntimeContextChangedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorStartedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorSuccessLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.exceptions.TaskExecutorNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskExecutorLifecycleEventListener implements ITaskExecutorLifecycleEventListener {

    private final ITaskExecutorContainerProvider taskExecutorContainerDelegator;

    private final ITaskExecutorRepository taskExecutorRepository;

    private final ITaskExecutorLifecycleEventReporter taskExecutorLifecycleEventReporter;

    public TaskExecutorLifecycleEventListener(final ITaskExecutorContainerProvider taskExecutorContainerDelegator,
                                              final ITaskExecutorRepository taskExecutorRepository,
                                              final ITaskExecutorLifecycleEventReporter taskExecutorLifecycleEventReporter) {
        this.taskExecutorContainerDelegator = taskExecutorContainerDelegator;
        this.taskExecutorRepository = taskExecutorRepository;
        this.taskExecutorLifecycleEventReporter = taskExecutorLifecycleEventReporter;
    }

    @Override
    public void onTaskExecutorDispatchedLifecycleEvent(final TaskExecutorDispatchedLifecycleEvent event) {
        reportTaskExecutorLifecycleEventToMaster(event);
    }

    @Override
    public void onTaskExecutorStartedLifecycleEvent(final TaskExecutorStartedLifecycleEvent event) {
        reportTaskExecutorLifecycleEventToMaster(event);
    }

    @Override
    public void onTaskExecutorRuntimeContextChangedEvent(TaskExecutorRuntimeContextChangedLifecycleEvent event) {
        reportTaskExecutorLifecycleEventToMaster(event);
    }

    @Override
    public void onTaskExecutorPauseLifecycleEvent(final TaskExecutorPauseLifecycleEvent event) {
        final ITaskExecutor taskExecutor = getTaskExecutor(event);
        taskExecutor.pause();
    }

    @Override
    public void onTaskExecutorPausedLifecycleEvent(final TaskExecutorPausedLifecycleEvent event) {
        taskExecutorLifecycleEventReporter.reportTaskExecutorLifecycleEvent(event);
    }

    @Override
    public void onTaskExecutorKillLifecycleEvent(final TaskExecutorKillLifecycleEvent event) {
        final ITaskExecutor taskExecutor = getTaskExecutor(event);
        taskExecutor.kill();
    }

    @Override
    public void onTaskExecutorKilledLifecycleEvent(final TaskExecutorKilledLifecycleEvent event) {
        taskExecutorLifecycleEventReporter.reportTaskExecutorLifecycleEvent(event);
    }

    @Override
    public void onTaskExecutorSuccessLifecycleEvent(final TaskExecutorSuccessLifecycleEvent event) {
        taskExecutorLifecycleEventReporter.reportTaskExecutorLifecycleEvent(event);
    }

    @Override
    public void onTaskExecutorFailLifecycleEvent(TaskExecutorFailedLifecycleEvent event) {
        taskExecutorLifecycleEventReporter.reportTaskExecutorLifecycleEvent(event);
    }

    @Override
    public void onTaskExecutorFinalizeLifecycleEvent(final TaskExecutorFinalizeLifecycleEvent event) {
        TaskInstanceLogHeader.printFinalizeTaskHeader();
        final ITaskExecutor taskExecutor = getTaskExecutor(event);

        taskExecutorRepository.remove(taskExecutor.getId());

        final ITaskExecutorContainer executorContainer = taskExecutorContainerDelegator.getExecutorContainer();
        executorContainer.finalize(taskExecutor);
    }

    private void reportTaskExecutorLifecycleEventToMaster(IReportableTaskExecutorLifecycleEvent taskExecutorLifecycleEvent) {
        taskExecutorLifecycleEventReporter.reportTaskExecutorLifecycleEvent(taskExecutorLifecycleEvent);
    }

    private ITaskExecutor getTaskExecutor(final ITaskExecutorLifecycleEvent taskExecutorLifecycleEvent) {
        return taskExecutorRepository.get(taskExecutorLifecycleEvent.getTaskInstanceId()).orElseThrow(
                () -> new TaskExecutorNotFoundException(taskExecutorLifecycleEvent.getTaskInstanceId()));
    }

}
