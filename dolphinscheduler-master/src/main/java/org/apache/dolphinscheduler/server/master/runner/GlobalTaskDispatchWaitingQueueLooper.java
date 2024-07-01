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
import org.apache.dolphinscheduler.server.master.runner.dispatcher.TaskDispatchFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GlobalTaskDispatchWaitingQueueLooper extends BaseDaemonThread implements AutoCloseable {

    @Autowired
    private GlobalTaskDispatchWaitingQueue globalTaskDispatchWaitingQueue;

    @Autowired
    private TaskDispatchFactory taskDispatchFactory;

    private final AtomicBoolean RUNNING_FLAG = new AtomicBoolean(false);

    public GlobalTaskDispatchWaitingQueueLooper() {
        super("GlobalTaskDispatchWaitingQueueLooper");
    }

    @Override
    public synchronized void start() {
        if (!RUNNING_FLAG.compareAndSet(false, true)) {
            log.error("The GlobalTaskDispatchWaitingQueueLooper already started, will not start again");
            return;
        }
        log.info("GlobalTaskDispatchWaitingQueueLooper starting...");
        super.start();
        log.info("GlobalTaskDispatchWaitingQueueLooper started...");
    }

    @Override
    public void run() {
        while (RUNNING_FLAG.get()) {
            doDispatch();
        }
    }

    void doDispatch() {
        final TaskExecuteRunnable taskExecuteRunnable = globalTaskDispatchWaitingQueue.takeTaskExecuteRunnable();
        TaskInstance taskInstance = taskExecuteRunnable.getTaskInstance();
        if (taskInstance == null) {
            // This case shouldn't happen, but if it does, log an error and continue
            log.error("The TaskInstance is null, drop it(This case shouldn't happen)");
            return;
        }
        try {
            TaskExecutionStatus status = taskInstance.getState();
            if (status != TaskExecutionStatus.SUBMITTED_SUCCESS && status != TaskExecutionStatus.DELAY_EXECUTION) {
                log.warn("The TaskInstance {} state is : {}, will not dispatch", taskInstance.getName(), status);
                return;
            }
            taskDispatchFactory.getTaskDispatcher(taskInstance).dispatchTask(taskExecuteRunnable);
        } catch (Exception e) {
            // If dispatch failed, will put the task back to the queue
            // The task will be dispatched after waiting time.
            // the waiting time will increase multiple of times, but will not exceed 60 seconds
            long waitingTimeMills = Math.max(
                    taskExecuteRunnable.getTaskExecutionContext().increaseDispatchFailTimes() * 1_000L, 60_000L);
            globalTaskDispatchWaitingQueue.dispatchTaskExecuteRunnableWithDelay(taskExecuteRunnable, waitingTimeMills);
            log.error("Dispatch Task: {} failed will retry after: {}/ms", taskInstance.getName(), waitingTimeMills, e);
        }
    }

    @Override
    public void close() throws Exception {
        if (RUNNING_FLAG.compareAndSet(true, false)) {
            log.info("GlobalTaskDispatchWaitingQueueLooper stopping...");
            log.info("GlobalTaskDispatchWaitingQueueLooper stopped...");
        } else {
            log.error("GlobalTaskDispatchWaitingQueueLooper is not started");
        }
    }
}
