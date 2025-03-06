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

import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.runner.queue.DelayEntry;
import org.apache.dolphinscheduler.server.master.runner.queue.PriorityDelayQueue;

import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkerGroupTaskDispatchManager implements AutoCloseable {

    @Autowired
    private ITaskExecutorClient taskExecutorClient;

    private ConcurrentHashMap<String, WorkerGroupTaskDispatchWaitingQueueLooper> workerGroupTaskDispatchWaitingQueueLooperMap =
            new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, PriorityDelayQueue<DelayEntry<ITaskExecutionRunnable>>> workerGroupPriorityDelayQueueMap =
            new ConcurrentHashMap<>();

    public WorkerGroupTaskDispatchManager() {
        workerGroupTaskDispatchWaitingQueueLooperMap = new ConcurrentHashMap<>();
        workerGroupPriorityDelayQueueMap = new ConcurrentHashMap<>();
    }

    /**
     * Adds a task to the specified worker group queue and starts or wakes up the corresponding processing loop.
     *
     * @param workerGroup the identifier for the worker group, used to distinguish different task queues
     * @param taskExecutionRunnable an instance of ITaskExecutionRunnable representing the task to be executed
     * @param delayTimeMills the delay time before the task is executed, in milliseconds
     */
    public void add(String workerGroup, ITaskExecutionRunnable taskExecutionRunnable, long delayTimeMills) {
        PriorityDelayQueue<DelayEntry<ITaskExecutionRunnable>> workerGroupQueue =
                workerGroupPriorityDelayQueueMap.computeIfAbsent(workerGroup, j -> new PriorityDelayQueue<>());

        workerGroupQueue.add(new DelayEntry<>(delayTimeMills, taskExecutionRunnable));
        try (
                WorkerGroupTaskDispatchWaitingQueueLooper looper =
                        workerGroupTaskDispatchWaitingQueueLooperMap.computeIfAbsent(
                                workerGroup,
                                k -> new WorkerGroupTaskDispatchWaitingQueueLooper(workerGroup, this.taskExecutorClient,
                                        workerGroupQueue))) {
            looper.start();
        } catch (Exception e) {
            log.error("Error occurred while shutting down the thread manager: {}", e.getMessage(), e);
        }
    }

    /**
     * 停止所有workerGroupTaskDispatchWaitingQueueLooperMaps 里所有AutoCloseable
     */
    @Override
    public void close() throws Exception {
        // Iterate over all worker group task dispatch waiting queue loopers
        for (WorkerGroupTaskDispatchWaitingQueueLooper looper : workerGroupTaskDispatchWaitingQueueLooperMap.values()) {
            // Close each looper to stop the task dispatching process
            if (looper != null) {
                looper.close();
            }
        }
    }
}
