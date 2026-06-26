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

package org.apache.dolphinscheduler.server.master.engine.task.dispatcher;

import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.config.TaskDispatchPolicy;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.dispatcher.event.TaskDispatchableEvent;
import org.apache.dolphinscheduler.server.master.engine.task.execution.ITaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.event.TaskFailedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.log.TaskExecutorMDCUtils;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

import com.google.common.annotations.VisibleForTesting;

/**
 * WorkerGroupTaskDispatcher is responsible for dispatching tasks from the task queue.
 * The main responsibilities include:
 * 1. Continuously fetching tasks from the {@link TaskDispatchableEvent} for dispatch.
 * 2. Re-queuing tasks that fail to dispatch according to retry logic.
 * 3. Ensuring thread safety and correct state transitions during task processing.
 */
@Slf4j
public class WorkerGroupDispatcher extends BaseDaemonThread {

    private final ITaskExecutorClient taskExecutorClient;

    private final TaskDispatchableEventBus<TaskDispatchableEvent<ITaskExecution>, ITaskExecution> workerGroupEventBus;

    private final Set<Integer> waitingDispatchTaskIds;

    private final AtomicBoolean runningFlag = new AtomicBoolean(false);

    private final TaskDispatchPolicy taskDispatchPolicy;

    private final long maxTaskDispatchMillis;

    public WorkerGroupDispatcher(String workerGroupName, ITaskExecutorClient taskExecutorClient,
                                 TaskDispatchPolicy taskDispatchPolicy) {
        super("WorkerGroupTaskDispatcher-" + workerGroupName);
        this.taskExecutorClient = taskExecutorClient;
        this.workerGroupEventBus = new TaskDispatchableEventBus<>();
        this.waitingDispatchTaskIds = ConcurrentHashMap.newKeySet();
        this.taskDispatchPolicy = taskDispatchPolicy;
        if (taskDispatchPolicy.isDispatchTimeoutEnabled()) {
            this.maxTaskDispatchMillis = taskDispatchPolicy.getMaxTaskDispatchDuration().toMillis();
        } else {
            this.maxTaskDispatchMillis = 0L;
        }
        log.info("Initialize WorkerGroupDispatcher: {}", this.getName());
    }

    @Override
    public synchronized void start() {
        if (runningFlag.compareAndSet(false, true)) {
            log.info("The {} starting...", this.getName());
            super.start();
            log.info("The {}  started", this.getName());
        } else {
            log.error("The {} status is {}, will not start again", this.getName(), runningFlag.get());
        }
    }

    @Override
    public void run() {
        while (runningFlag.get()) {
            final TaskDispatchableEvent<ITaskExecution> taskEntry;
            try {
                taskEntry = workerGroupEventBus.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("{} interrupted, exiting run loop", this.getName());
                return;
            }
            ITaskExecution taskExecution = taskEntry.getData();
            try (
                    TaskExecutorMDCUtils.MDCAutoClosable ignore =
                            TaskExecutorMDCUtils.logWithMDC(taskExecution.getId())) {
                LogUtils.setWorkflowInstanceIdMDC(taskExecution.getTaskInstance().getWorkflowInstanceId());
                doDispatchTask(taskExecution);
            } finally {
                LogUtils.removeWorkflowInstanceIdMDC();
            }
        }
    }

    private void doDispatchTask(ITaskExecution taskExecution) {
        final int taskInstanceId = taskExecution.getId();
        final TaskExecutionContext taskExecutionContext = taskExecution.getTaskExecutionContext();
        try {
            if (!waitingDispatchTaskIds.remove(taskInstanceId)) {
                log.info(
                        "The task: {} doesn't exist in waitingDispatchTaskIds(it might be paused or killed), will skip dispatch",
                        taskInstanceId);
                return;
            }
            taskExecutorClient.dispatch(taskExecution);
        } catch (Exception ex) {
            if (taskDispatchPolicy.isDispatchTimeoutEnabled()) {
                // If a dispatch timeout occurs, the task will not be put back into the queue.
                long elapsed = System.currentTimeMillis() - taskExecutionContext.getFirstDispatchTime();
                if (elapsed > maxTaskDispatchMillis) {
                    onDispatchTimeout(taskExecution, ex, elapsed, maxTaskDispatchMillis);
                    return;
                }
            }

            // If dispatch failed, will put the task back to the queue
            // The task will be dispatched after waiting time.
            // the waiting time grows with the fail count, bounded between 1 and 60 seconds
            long waitingTimeMillis = Math.max(1_000L, Math.min(
                    taskExecution.getTaskExecutionContext().increaseDispatchFailTimes() * 1_000L, 60_000L));
            dispatchTask(taskExecution, waitingTimeMillis);
            log.warn("Dispatch Task: {} failed will retry after: {}/ms", taskInstanceId,
                    waitingTimeMillis, ex);
        }
    }

    /**
     * Marks a task as permanently failed due to dispatch timeout.
     * Once called, the task is considered permanently failed and will not be retried.
     */
    private void onDispatchTimeout(ITaskExecution taskExecution, Exception ex,
                                   long elapsed, long timeout) {
        String taskName = taskExecution.getName();
        log.error("Task: {} dispatch timeout after {}ms (limit: {}ms)",
                taskName, elapsed, timeout, ex);

        final TaskFailedLifecycleEvent taskFailedEvent = TaskFailedLifecycleEvent.builder()
                .taskExecution(taskExecution)
                .endTime(new Date())
                .build();
        taskExecution.getWorkflowEventBus().publish(taskFailedEvent);
    }

    /**
     * Adds a task to the worker group queue.
     * This method wraps the given task execution object into a priority and delay-based task entry and adds it to the worker group queue.
     * The task is only added if the current dispatcher status is either STARTED or INIT. If the dispatcher is in any other state,
     * the task addition will fail, and a warning message will be logged.
     *
     * @param taskExecution The task execution object to add to the queue, which implements the {@link ITaskExecution} interface.
     * @param delayTimeMills        The delay time in milliseconds before the task should be executed.
     */
    public void dispatchTask(final ITaskExecution taskExecution, final long delayTimeMills) {
        waitingDispatchTaskIds.add(taskExecution.getId());
        workerGroupEventBus.add(new TaskDispatchableEvent<>(delayTimeMills, taskExecution,
                taskExecution.getTaskExecutionContext().getDispatchFailTimes()));
    }

    public boolean removeTask(ITaskExecution taskExecution) {
        return waitingDispatchTaskIds.remove(taskExecution.getId());
    }

    public boolean existTask(ITaskExecution taskExecution) {
        return waitingDispatchTaskIds.contains(taskExecution.getId());
    }

    public synchronized void close() {
        // todo WorkerGroupTaskDispatcher thread needs to be shut down after the WorkerGroup is deleted.
        if (runningFlag.compareAndSet(true, false)) {
            log.info("WorkerGroupDispatcher {} closed", this.getName());
        } else {
            log.warn("The WorkerGroupDispatcher: {} doesn't started", this.getName());
        }
    }

    @VisibleForTesting
    public int dispatchEventCount() {
        return this.workerGroupEventBus.size();
    }

    @VisibleForTesting
    public int waitingDispatchTaskCount() {
        return this.waitingDispatchTaskIds.size();
    }
}
