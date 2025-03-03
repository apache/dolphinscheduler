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
import org.apache.dolphinscheduler.server.master.runner.queue.WorkerGroupQueueMap;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkerGroupQueueLooper extends BaseDaemonThread implements AutoCloseable {

    @Autowired
    private WorkerGroupQueueMap workerGroupQueueMap;

    @Autowired
    private ITaskExecutorClient taskExecutorClient;

    private final AtomicBoolean RUNNING_FLAG = new AtomicBoolean(false);

    public WorkerGroupQueueLooper() {
        super("WorkerGroupQueueLooper");
    }

    @Override
    public synchronized void start() {
        if (!RUNNING_FLAG.compareAndSet(false, true)) {
            log.error("The WorkerGroupQueueLooper already started, will not start again");
            return;
        }
        log.info("WorkerGroupQueueLooper starting...");
        super.start();
        log.info("WorkerGroupQueueLooper started...");
    }

    @Override
    public void close() throws Exception {
        if (RUNNING_FLAG.compareAndSet(true, false)) {
            log.info("WorkerGroupQueueLooper stopping...");
            log.info("WorkerGroupQueueLooper stopped...");
        } else {
            log.error("WorkerGroupQueueLooper is not started");
        }
    }

    @Override
    public void run() {
        while (RUNNING_FLAG.get()) {
            doDispatch();
        }
    }

    void doDispatch() {
        final Map<String, ITaskExecutionRunnable> taskExecutionRunnablesMap = workerGroupQueueMap.poll();
        for (String workerGroup : taskExecutionRunnablesMap.keySet()) {
            final ITaskExecutionRunnable workerGroupExecutionRunnable = taskExecutionRunnablesMap.get(workerGroup);
            if (workerGroupExecutionRunnable != null) {
                final TaskInstance taskInstance = workerGroupExecutionRunnable.getTaskInstance();
                try {
                    final TaskExecutionStatus status = taskInstance.getState();
                    if (status != TaskExecutionStatus.SUBMITTED_SUCCESS
                            && status != TaskExecutionStatus.DELAY_EXECUTION) {
                        log.warn("The TaskInstance {} state is : {}, will not dispatch", taskInstance.getName(),
                                status);
                        return;
                    }
                    log.debug("dispatch task:{}, workflowInstancePriority:{}, taskPriority:{}, ",
                            workerGroupExecutionRunnable.getTaskInstance().getName(),
                            workerGroupExecutionRunnable.getWorkflowInstance().getWorkflowInstancePriority(),
                            workerGroupExecutionRunnable.getTaskInstance().getTaskInstancePriority());
                    taskExecutorClient.dispatch(workerGroupExecutionRunnable);
                } catch (Exception e) {
                    // If dispatch failed, will put the task back to the queue
                    // The task will be dispatched after waiting time.
                    // the waiting time will increase multiple of times, but will not exceed 60 seconds
                    long waitingTimeMills = Math.min(
                            workerGroupExecutionRunnable.getTaskExecutionContext().increaseDispatchFailTimes() * 1_000L,
                            60_000L);
                    workerGroupQueueMap.add(workerGroup, workerGroupExecutionRunnable, waitingTimeMills);
                    log.error("Dispatch Task: {} failed will retry after: {}/ms", taskInstance.getName(),
                            waitingTimeMills, e);
                }
            }

        }
    }

}
