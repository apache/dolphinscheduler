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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.log.TaskInstanceLogHeader;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;
import org.apache.dolphinscheduler.task.executor.ITaskExecutorRepository;
import org.apache.dolphinscheduler.task.executor.container.ITaskExecutorContainer;
import org.apache.dolphinscheduler.task.executor.container.ITaskExecutorContainerProvider;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;
import org.apache.dolphinscheduler.task.executor.exceptions.TaskExecutorNotFoundException;
import org.apache.dolphinscheduler.task.executor.utils.CommonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Strings;

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
        reportTaskExecutorLifecycleEventToMaster(event);
    }

    @Override
    public void onTaskExecutorKillLifecycleEvent(final TaskExecutorKillLifecycleEvent event) {
        final ITaskExecutor taskExecutor = getTaskExecutor(event);
        taskExecutor.kill();
    }

    @Override
    public void onTaskExecutorKilledLifecycleEvent(final TaskExecutorKilledLifecycleEvent event) {
        reportTaskExecutorLifecycleEventToMaster(event);
    }

    @Override
    public void onTaskExecutorSuccessLifecycleEvent(final TaskExecutorSuccessLifecycleEvent event) {
        reportTaskExecutorLifecycleEventToMaster(event);
    }

    @Override
    public void onTaskExecutorFailLifecycleEvent(TaskExecutorFailedLifecycleEvent event) {
        reportTaskExecutorLifecycleEventToMaster(event);
    }

    @Override
    public void onTaskExecutorFinalizeLifecycleEvent(final TaskExecutorFinalizeLifecycleEvent event) {
        TaskInstanceLogHeader.printFinalizeTaskHeader();

        final ITaskExecutor taskExecutor = getTaskExecutor(event);
        clearTaskExecPathIfNeeded(taskExecutor.getTaskExecutionContext());
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

    /**
     * Clears the local execution path directory for a task if needed.
     * Skips in develop mode, validates path safety, and handles deletion errors gracefully.
     *
     * @param taskExecutionContext context containing task execution details, nullable
     */
    private void clearTaskExecPathIfNeeded(TaskExecutionContext taskExecutionContext) {
        if (taskExecutionContext == null) {
            log.warn("TaskExecutionContext is null, cannot clear execution path");
            return;
        }

        String execLocalPath = taskExecutionContext.getExecutePath();

        // Skip cleanup in development mode to preserve files for debugging
        if (CommonUtils.isDevelopMode()) {
            log.info("Running in develop mode, skip clearing path: {}", execLocalPath);
            return;
        }

        log.info("Clearing task execution path: {}", execLocalPath);

        if (Strings.isNullOrEmpty(execLocalPath)) {
            log.warn("Execution path is null or empty for task: {}", taskExecutionContext.getTaskName());
            return;
        }

        File execFile = new File(execLocalPath);
        Path path;

        try {
            // Resolve the canonical path (follow symlinks, remove ../)
            path = execFile.toPath().toRealPath();
            // Prevent deletion of root directories (e.g., "/", "C:\") to avoid system damage
            if (path.getRoot().equals(path)) {
                log.warn("Refusing to delete root directory: {}", path);
                return;
            }

            // Only attempt deletion if the path is an existing directory
            if (path.toFile().isDirectory()) {
                org.apache.commons.io.FileUtils.deleteDirectory(execFile);
                log.info("Successfully cleared task execution path: {}", execLocalPath);
            } else {
                log.debug("Path is not a directory or does not exist: {}", execLocalPath);
            }
        } catch (NoSuchFileException ex) {
            log.warn("Path does not exist or already deleted: {}", execLocalPath, ex);
        } catch (IOException ex) {
            log.error("Failed to delete task execution path: {}", execLocalPath, ex);
        } catch (SecurityException ex) {
            log.error("Permission denied when accessing or deleting path: {}", execLocalPath, ex);
        }
    }

}
