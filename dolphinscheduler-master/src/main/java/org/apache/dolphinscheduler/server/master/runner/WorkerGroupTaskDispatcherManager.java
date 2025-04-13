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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * WorkerGroupTaskDispatcherManager is responsible for managing the task dispatching for worker groups.
 * It maintains a mapping of worker groups to their task dispatchers and priority delay queues,
 * and supports adding tasks, starting and stopping worker groups, as well as cleaning up resources upon shutdown.
 */
@Component
@Slf4j
public class WorkerGroupTaskDispatcherManager implements AutoCloseable {

    @Autowired
    private ITaskExecutorClient taskExecutorClient;

    @Getter
    private final ConcurrentHashMap<String, WorkerGroupTaskDispatcher> dispatchWorkerMap;

    public WorkerGroupTaskDispatcherManager() {
        dispatchWorkerMap = new ConcurrentHashMap<>();
    }

    /**
     * Adds a task to the specified worker group queue and starts or wakes up the corresponding processing loop.
     *
     * @param workerGroup the identifier for the worker group, used to distinguish different task queues
     * @param taskExecutionRunnable an instance of ITaskExecutionRunnable representing the task to be executed
     * @param delayTimeMills the delay time before the task is executed, in milliseconds
     */
    public synchronized void addTaskToWorkerGroup(String workerGroup, ITaskExecutionRunnable taskExecutionRunnable,
                                                  long delayTimeMills) {
        WorkerGroupTaskDispatcher workerGroupTaskDispatcher = dispatchWorkerMap.computeIfAbsent(
                workerGroup, key -> new WorkerGroupTaskDispatcher(workerGroup, taskExecutorClient));
        if (!workerGroupTaskDispatcher.isAlive()) {
            workerGroupTaskDispatcher.start();
        }
        workerGroupTaskDispatcher.addTaskToWorkerGroupQueue(taskExecutionRunnable, delayTimeMills);
    }

    /**
     * Stop all workerGroupTaskDispatchWaitingQueueLooper
     */
    @Override
    public void close() throws Exception {
        log.info("WorkerGroupTaskDispatcherManager start close");
        for (Map.Entry<String, WorkerGroupTaskDispatcher> entry : dispatchWorkerMap.entrySet()) {
            try {
                entry.getValue().close();
            } catch (Exception e) {
                log.error("close worker group error", e);
            }
        }
        log.info("WorkerGroupTaskDispatcherManager closed");
    }
}
