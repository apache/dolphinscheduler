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

import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

import java.util.concurrent.ConcurrentHashMap;

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
public class WorkerGroupDispatcherCoordinator implements AutoCloseable {

    @Autowired
    private ITaskExecutorClient taskExecutorClient;

    private final ConcurrentHashMap<String, WorkerGroupDispatcher> workerGroupDispatcherMap;

    public WorkerGroupDispatcherCoordinator() {
        workerGroupDispatcherMap = new ConcurrentHashMap<>();
    }

    public void start() {
        log.info("WorkerGroupTaskDispatcherManager started...");
    }

    /**
     * Dispatch task to the worker group with the specified remaining time.
     */
    public void dispatchTask(final ITaskExecutionRunnable taskExecutionRunnable,
                             final long delayTimeMills) {
        final String workerGroup = taskExecutionRunnable.getTaskInstance().getWorkerGroup();
        getOrCreateWorkerGroupDispatcher(workerGroup).dispatchTask(taskExecutionRunnable, delayTimeMills);
        log.info("Success add Task[id={}] to WorkerGroupDispatcher[name={}]", taskExecutionRunnable.getId(),
                workerGroup);
    }

    /**
     * Remove task from the dispatcher.
     * <p> If the task doesn't exist in the dispatcher, it will return false, this means the task might already be dispatched.
     */
    public boolean removeTask(ITaskExecutionRunnable taskExecutionRunnable) {
        final String workerGroup = taskExecutionRunnable.getTaskInstance().getWorkerGroup();
        boolean removed = getOrCreateWorkerGroupDispatcher(workerGroup).removeTask(taskExecutionRunnable);
        if (removed) {
            log.info("Success removed Task[id={}] from WorkerGroupDispatcher[name={}]",
                    taskExecutionRunnable.getId(), workerGroup);
        } else {
            log.info("Failed to remove Task[id={}] from WorkerGroupDispatcher[name={}], this task has been dispatched",
                    taskExecutionRunnable.getId(), workerGroup);
        }
        return removed;
    }

    public boolean existWorkerGroup(String workerGroup) {
        return workerGroupDispatcherMap.containsKey(workerGroup);
    }

    /**
     * Stop all workerGroupTaskDispatchWaitingQueueLooper
     */
    @Override
    public void close() throws Exception {
        log.info("WorkerGroupDispatcherCoordinator closing");
        for (WorkerGroupDispatcher workerGroupDispatcher : workerGroupDispatcherMap.values()) {
            try {
                workerGroupDispatcher.close();
            } catch (Exception e) {
                log.error("close WorkerGroupDispatcher[name={}] error", workerGroupDispatcher.getName(), e);
            }
        }
        log.info("WorkerGroupDispatcherCoordinator closed...");
    }

    private WorkerGroupDispatcher getOrCreateWorkerGroupDispatcher(String workerGroup) {
        return workerGroupDispatcherMap.computeIfAbsent(workerGroup, wg -> {
            WorkerGroupDispatcher workerGroupDispatcher = new WorkerGroupDispatcher(wg, taskExecutorClient);
            workerGroupDispatcher.start();
            return workerGroupDispatcher;
        });
    }
}
