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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.runner.queue.PriorityAndDelayBasedTaskEntry;
import org.apache.dolphinscheduler.server.master.runner.queue.PriorityDelayQueue;

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

/**
 * WorkerGroupTaskDispatcher is responsible for dispatching tasks from the task queue.
 * The main responsibilities include:
 * 1. Continuously fetching tasks from the {@link PriorityDelayQueue} for dispatch.
 * 2. Re-queuing tasks that fail to dispatch according to retry logic.
 * 3. Ensuring thread safety and correct state transitions during task processing.
 */
@Slf4j
public class WorkerGroupTaskDispatcher extends BaseDaemonThread {

    private final ITaskExecutorClient taskExecutorClient;

    // TODO The current queue is flawed. When a high-priority task fails,
    // it will be delayed and will not return to the first or second position.
    // Tasks with the same priority will preempt its position.
    // If it needs to be placed at the front of the queue, the queue needs to be re-implemented.
    private final PriorityDelayQueue<PriorityAndDelayBasedTaskEntry<ITaskExecutionRunnable>> workerGroupQueue;

    private final AtomicBoolean runningFlag = new AtomicBoolean(false);

    public WorkerGroupTaskDispatcher(String workerGroupName, ITaskExecutorClient taskExecutorClient) {
        super("WorkerGroupTaskDispatcher-" + workerGroupName);
        this.taskExecutorClient = taskExecutorClient;
        this.workerGroupQueue = new PriorityDelayQueue<>();
    }

    /**
     * Adds a task to the worker group queue.
     * This method wraps the given task execution object into a priority and delay-based task entry and adds it to the worker group queue.
     * The task is only added if the current dispatcher status is either STARTED or INIT. If the dispatcher is in any other state,
     * the task addition will fail, and a warning message will be logged.
     *
     * @param taskExecutionRunnable The task execution object to add to the queue, which implements the {@link ITaskExecutionRunnable} interface.
     * @param delayTimeMills The delay time in milliseconds before the task should be executed.
     */
    public void addTaskToWorkerGroupQueue(ITaskExecutionRunnable taskExecutionRunnable,
                                          long delayTimeMills) {
        workerGroupQueue.add(new PriorityAndDelayBasedTaskEntry<>(delayTimeMills, taskExecutionRunnable));
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

    public synchronized void close() {
        log.info("The {} closed called but not implemented", this.getName());
        // todo WorkerGroupTaskDispatcher thread needs to be shut down after the WorkerGroup is deleted.
    }

    @Override
    public void run() {
        while (runningFlag.get()) {
            dispatch();
        }
    }

    private void dispatch() {
        PriorityAndDelayBasedTaskEntry<ITaskExecutionRunnable> taskEntry = workerGroupQueue.take();
        ITaskExecutionRunnable taskExecutionRunnable = taskEntry.getData();
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        try {
            final TaskExecutionStatus taskStatus = taskInstance.getState();
            if (taskStatus != TaskExecutionStatus.SUBMITTED_SUCCESS
                    && taskStatus != TaskExecutionStatus.DELAY_EXECUTION) {
                log.warn("The TaskInstance {} state is : {}, will not dispatch", taskInstance.getName(), taskStatus);
                return;
            }
            taskExecutorClient.dispatch(taskExecutionRunnable);
        } catch (Exception e) {
            // If dispatch failed, will put the task back to the queue
            // The task will be dispatched after waiting time.
            // the waiting time will increase multiple of times, but will not exceed 60 seconds
            long waitingTimeMills = Math.min(
                    taskExecutionRunnable.getTaskExecutionContext().increaseDispatchFailTimes() * 1_000L, 60_000L);
            workerGroupQueue.add(new PriorityAndDelayBasedTaskEntry<>(waitingTimeMills, taskExecutionRunnable));
            log.error("Dispatch Task: {} failed will retry after: {}/ms", taskInstance.getName(), waitingTimeMills, e);
        }
    }

    /**
     * ony use unit test
     * @return size
     */
    protected int queueSize() {
        return this.workerGroupQueue.size();
    }
}
