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

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.runner.queue.DelayEntry;
import org.apache.dolphinscheduler.server.master.runner.queue.PriorityDelayQueue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DispatchWorker {

    private final ITaskExecutorClient taskExecutorClient;

    private final PriorityDelayQueue<DelayEntry<ITaskExecutionRunnable>> workerGroupQueue;

    public DispatchWorker(ITaskExecutorClient taskExecutorClient,
                          PriorityDelayQueue<DelayEntry<ITaskExecutionRunnable>> workerGroupQueue) {
        this.taskExecutorClient = taskExecutorClient;
        this.workerGroupQueue = workerGroupQueue;
    }

    public void dispatch() {
        ITaskExecutionRunnable taskExecutionRunnable = workerGroupQueue.take().getData();
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        try {
            final TaskExecutionStatus status = taskInstance.getState();
            if (status != TaskExecutionStatus.SUBMITTED_SUCCESS && status != TaskExecutionStatus.DELAY_EXECUTION) {
                log.warn("The TaskInstance {} state is : {}, will not dispatch", taskInstance.getName(), status);
                return;
            }
            taskExecutorClient.dispatch(taskExecutionRunnable);
        } catch (Exception e) {
            // If dispatch failed, will put the task back to the queue
            // The task will be dispatched after waiting time.
            // the waiting time will increase multiple of times, but will not exceed 60 seconds
            long waitingTimeMills = Math.min(
                    taskExecutionRunnable.getTaskExecutionContext().increaseDispatchFailTimes() * 1_000L, 60_000L);
            workerGroupQueue.add(new DelayEntry<>(waitingTimeMills, taskExecutionRunnable));
            log.error("Dispatch Task: {} failed will retry after: {}/ms", taskInstance.getName(), waitingTimeMills, e);
        }
    }

}
