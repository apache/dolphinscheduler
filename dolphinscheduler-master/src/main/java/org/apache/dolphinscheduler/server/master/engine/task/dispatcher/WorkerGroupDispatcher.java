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
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.dispatcher.event.TaskDispatchableEvent;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.task.executor.log.TaskExecutorMDCUtils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

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

    private final TaskDispatchableEventBus<TaskDispatchableEvent<ITaskExecutionRunnable>, ITaskExecutionRunnable> workerGroupEventBus;

    private final Set<Integer> waitingDispatchTaskIds;

    private final AtomicBoolean runningFlag = new AtomicBoolean(false);

    public WorkerGroupDispatcher(String workerGroupName, ITaskExecutorClient taskExecutorClient) {
        super("WorkerGroupTaskDispatcher-" + workerGroupName);
        this.taskExecutorClient = taskExecutorClient;
        this.workerGroupEventBus = new TaskDispatchableEventBus<>();
        this.waitingDispatchTaskIds = ConcurrentHashMap.newKeySet();
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
            TaskDispatchableEvent<ITaskExecutionRunnable> taskEntry = workerGroupEventBus.take();
            ITaskExecutionRunnable taskExecutionRunnable = taskEntry.getData();
            try (
                    TaskExecutorMDCUtils.MDCAutoClosable ignore =
                            TaskExecutorMDCUtils.logWithMDC(taskExecutionRunnable.getId())) {
                LogUtils.setWorkflowInstanceIdMDC(taskExecutionRunnable.getTaskInstance().getWorkflowInstanceId());
                doDispatchTask(taskExecutionRunnable);
            } finally {
                LogUtils.removeWorkflowInstanceIdMDC();
            }
        }
    }

    private void doDispatchTask(ITaskExecutionRunnable taskExecutionRunnable) {
        try {
            if (!waitingDispatchTaskIds.remove(taskExecutionRunnable.getId())) {
                log.info(
                        "The task: {} doesn't exist in waitingDispatchTaskIds(it might be paused or killed), will skip dispatch",
                        taskExecutionRunnable.getId());
                return;
            }
            taskExecutorClient.dispatch(taskExecutionRunnable);
        } catch (Exception e) {
            // If dispatch failed, will put the task back to the queue
            // The task will be dispatched after waiting time.
            // the waiting time will increase multiple of times, but will not exceed 60 seconds
            long waitingTimeMills = Math.min(
                    taskExecutionRunnable.getTaskExecutionContext().increaseDispatchFailTimes() * 1_000L, 60_000L);
            dispatchTask(taskExecutionRunnable, waitingTimeMills);
            log.error("Dispatch Task: {} failed will retry after: {}/ms", taskExecutionRunnable.getId(),
                    waitingTimeMills, e);
        }
    }

    /**
     * Adds a task to the worker group queue.
     * This method wraps the given task execution object into a priority and delay-based task entry and adds it to the worker group queue.
     * The task is only added if the current dispatcher status is either STARTED or INIT. If the dispatcher is in any other state,
     * the task addition will fail, and a warning message will be logged.
     *
     * @param taskExecutionRunnable The task execution object to add to the queue, which implements the {@link ITaskExecutionRunnable} interface.
     * @param delayTimeMills        The delay time in milliseconds before the task should be executed.
     */
    public void dispatchTask(final ITaskExecutionRunnable taskExecutionRunnable, final long delayTimeMills) {
        waitingDispatchTaskIds.add(taskExecutionRunnable.getId());
        workerGroupEventBus.add(new TaskDispatchableEvent<>(delayTimeMills, taskExecutionRunnable,
                taskExecutionRunnable.getTaskExecutionContext().getDispatchFailTimes()));
    }

    public boolean removeTask(ITaskExecutionRunnable taskExecutionRunnable) {
        return waitingDispatchTaskIds.remove(taskExecutionRunnable.getId());
    }

    public boolean existTask(ITaskExecutionRunnable taskExecutionRunnable) {
        return waitingDispatchTaskIds.contains(taskExecutionRunnable.getId());
    }

    public synchronized void close() {
        // todo WorkerGroupTaskDispatcher thread needs to be shut down after the WorkerGroup is deleted.
        if (runningFlag.compareAndSet(true, false)) {
            log.info("WorkerGroupDispatcher {} closed", this.getName());
        } else {
            log.warn("The WorkerGroupDispatcher: {} doesn't started", this.getName());
        }
    }

    int queueSize() {
        return this.workerGroupEventBus.size();
    }
}
