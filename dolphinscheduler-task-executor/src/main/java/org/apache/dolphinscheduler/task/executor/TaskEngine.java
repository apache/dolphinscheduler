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

package org.apache.dolphinscheduler.task.executor;

import org.apache.dolphinscheduler.task.executor.container.ITaskExecutorContainer;
import org.apache.dolphinscheduler.task.executor.container.ITaskExecutorContainerProvider;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorEventBusCoordinator;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorKillLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorPauseLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.exceptions.TaskExecutorNotFoundException;
import org.apache.dolphinscheduler.task.executor.exceptions.TaskExecutorRuntimeException;
import org.apache.dolphinscheduler.task.executor.log.TaskExecutorMDCUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskEngine implements ITaskEngine {

    private final String engineName;

    private final ITaskExecutorRepository taskExecutorRepository;

    private final ITaskExecutorContainerProvider taskExecutorContainerDelegator;

    private final ITaskExecutorEventBusCoordinator taskExecutorEventBusCoordinator;

    public TaskEngine(final TaskEngineBuilder taskEngineBuilder) {
        this.engineName = taskEngineBuilder.getEngineName();
        this.taskExecutorContainerDelegator = taskEngineBuilder.getTaskExecutorContainerDelegator();
        this.taskExecutorRepository = taskEngineBuilder.getTaskExecutorRepository();
        this.taskExecutorEventBusCoordinator = taskEngineBuilder.getTaskExecutorEventBusCoordinator();
    }

    @Override
    public void start() {
        taskExecutorEventBusCoordinator.start();
        log.info("{} started", engineName);
    }

    @Override
    public void submitTask(final ITaskExecutor taskExecutor) throws TaskExecutorRuntimeException {
        try (final TaskExecutorMDCUtils.MDCAutoClosable ignore = TaskExecutorMDCUtils.logWithMDC(taskExecutor)) {
            final ITaskExecutorContainer executorContainer = taskExecutorContainerDelegator.getExecutorContainer();
            executorContainer.dispatch(taskExecutor);
            taskExecutorRepository.put(taskExecutor);
            taskExecutor.getTaskExecutorEventBus().publish(TaskExecutorDispatchedLifecycleEvent.of(taskExecutor));
            executorContainer.start(taskExecutor);
        }
    }

    @Override
    public void pauseTask(final int taskExecutorId) throws TaskExecutorNotFoundException {
        final ITaskExecutor taskExecutor = getTaskExecutor(taskExecutorId);
        try (final TaskExecutorMDCUtils.MDCAutoClosable ignore = TaskExecutorMDCUtils.logWithMDC(taskExecutor)) {
            taskExecutor.getTaskExecutorEventBus().publish(TaskExecutorPauseLifecycleEvent.of(taskExecutor));
        }
    }

    @Override
    public void killTask(final int taskExecutorId) throws TaskExecutorNotFoundException {
        final ITaskExecutor taskExecutor = getTaskExecutor(taskExecutorId);
        try (final TaskExecutorMDCUtils.MDCAutoClosable ignore = TaskExecutorMDCUtils.logWithMDC(taskExecutor)) {
            taskExecutor.getTaskExecutorEventBus().publish(TaskExecutorKillLifecycleEvent.of(taskExecutor));
        }
    }

    @Override
    public void close() {
        try (final ITaskExecutorEventBusCoordinator ignore1 = taskExecutorEventBusCoordinator) {
            // Ignore
            log.info("{} closed", engineName);
        }
    }

    private ITaskExecutor getTaskExecutor(final int taskId) throws TaskExecutorNotFoundException {
        return taskExecutorRepository.get(taskId).orElseThrow(() -> new TaskExecutorNotFoundException(taskId));
    }

}
