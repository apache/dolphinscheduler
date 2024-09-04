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

package org.apache.dolphinscheduler.task.executor.container;

import static ch.qos.logback.classic.ClassicConstants.FINALIZE_SESSION_MARKER;

import org.apache.dolphinscheduler.common.log.remote.RemoteLogUtils;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;
import org.apache.dolphinscheduler.task.executor.exceptions.TaskExecutorRuntimeException;
import org.apache.dolphinscheduler.task.executor.worker.TaskExecutorWorker;
import org.apache.dolphinscheduler.task.executor.worker.TaskExecutorWorkers;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;

import com.google.common.annotations.VisibleForTesting;

@Slf4j
public abstract class AbstractTaskExecutorContainer implements ITaskExecutorContainer {

    private final TaskExecutorAssignmentTable taskExecutorAssignmentTable;

    protected final ThreadPoolExecutor taskExecutorThreadPool;

    protected final TaskExecutorWorkers taskExecutorWorkers;

    public AbstractTaskExecutorContainer(final TaskExecutorContainerConfig containerConfig) {
        final String threadPoolFormat = containerConfig.getContainerName() + "-worker-%d";
        final int threadPoolSize = containerConfig.getTaskExecutorThreadPoolSize();
        this.taskExecutorThreadPool = ThreadUtils.newDaemonFixedThreadExecutor(threadPoolFormat, threadPoolSize);
        this.taskExecutorWorkers = new TaskExecutorWorkers(threadPoolSize);
        this.taskExecutorAssignmentTable = new TaskExecutorAssignmentTable();
        startAllThreadTaskExecutorWorker();
    }

    @Override
    public void dispatch(final ITaskExecutor taskExecutor) {
        synchronized (this) {
            Optional<TaskExecutorWorker> taskExecutorWorkerCandidate = getTaskExecutorWorkerCandidate(taskExecutor);
            if (!taskExecutorWorkerCandidate.isPresent()) {
                log.info("All ExclusiveThreadTaskExecutorWorker are busy, cannot submit taskExecutor(id={})",
                        taskExecutor.getId());
                throw new TaskExecutorRuntimeException("All ExclusiveThreadTaskExecutorWorker are busy");
            }
            final TaskExecutorWorker taskExecutorWorker = taskExecutorWorkerCandidate.get();
            taskExecutorWorker.registerTaskExecutor(taskExecutor);
            taskExecutorAssignmentTable.registerTaskExecutor(taskExecutor, taskExecutorWorker);
        }
    }

    @Override
    public void start(final ITaskExecutor taskExecutor) {
        final Integer workerId = taskExecutorAssignmentTable.getTaskExecutorWorkerId(taskExecutor);
        if (workerId == null) {
            throw new IllegalStateException(
                    "The taskExecutor: " + taskExecutor.getId() + " is not registered to any worker");
        }
        final TaskExecutorWorker taskExecutorWorker = taskExecutorWorkers.getTaskExecutorWorkerById(workerId);
        taskExecutorWorker.fireTaskExecutor(taskExecutor);
    }

    @Override
    public void pause(final ITaskExecutor taskExecutor) {
        taskExecutor.pause();
    }

    @Override
    public void kill(final ITaskExecutor taskExecutor) {
        taskExecutor.kill();
    }

    @Override
    public void finalize(final ITaskExecutor taskExecutor) {
        if (taskExecutorAssignmentTable.isTaskExecutorRegistered(taskExecutor)) {
            final Integer taskExecutorWorkerId = taskExecutorAssignmentTable.getTaskExecutorWorkerId(taskExecutor);
            taskExecutorWorkers.getTaskExecutorWorkerById(taskExecutorWorkerId).unRegisterTaskExecutor(taskExecutor);
            taskExecutorAssignmentTable.unregisterTaskExecutor(taskExecutor);
        }
        log.info(FINALIZE_SESSION_MARKER, FINALIZE_SESSION_MARKER.toString());
        pushTaskExecutorLogToRemote(taskExecutor);
    }

    @Override
    public double slotUsage() {
        final List<TaskExecutorWorker> allWorkers = taskExecutorWorkers.getTaskExecutorWorkers();
        final long activeWorkerCount = allWorkers
                .stream()
                .filter(taskExecutorWorker -> taskExecutorWorker.getRegisteredTaskExecutorSize() != 0)
                .count();
        return activeWorkerCount / (double) allWorkers.size();
    }

    @VisibleForTesting
    public TaskExecutorAssignmentTable getTaskExecutorAssignmentTable() {
        return taskExecutorAssignmentTable;
    }

    @VisibleForTesting
    public TaskExecutorWorkers getTaskExecutorWorkers() {
        return taskExecutorWorkers;
    }

    private void startAllThreadTaskExecutorWorker() {
        for (final TaskExecutorWorker taskExecutorWorker : taskExecutorWorkers.getTaskExecutorWorkers()) {
            taskExecutorThreadPool.submit(taskExecutorWorker::start);
        }
    }

    /**
     * Get the candidate worker to execute the task executor.
     * <p> If the container is full, return empty.
     */
    protected abstract Optional<TaskExecutorWorker> getTaskExecutorWorkerCandidate(final ITaskExecutor taskExecutor);

    private void pushTaskExecutorLogToRemote(final ITaskExecutor taskExecutor) {
        // todo: move this to customer listener, e.g. RemoteLogApaptor
        final TaskExecutionContext taskExecutionContext = taskExecutor.getTaskExecutionContext();
        try {
            if (RemoteLogUtils.isRemoteLoggingEnable()) {
                RemoteLogUtils.sendRemoteLog(taskExecutionContext.getLogPath());
                log.info("Send task log {} to remote storage successfully", taskExecutionContext.getLogPath());
            }
        } catch (Exception ex) {
            log.error("Send task log {} to remote storage failed", taskExecutionContext.getLogPath(), ex);
        }
    }

}
