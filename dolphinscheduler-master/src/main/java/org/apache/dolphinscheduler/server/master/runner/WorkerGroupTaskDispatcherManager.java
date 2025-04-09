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

import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.utils.WorkerGroupUtils;
import org.apache.dolphinscheduler.server.master.cluster.WorkerGroupChangeNotifier;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

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
public class WorkerGroupTaskDispatcherManager implements AutoCloseable, WorkerGroupChangeNotifier.WorkerGroupListener {

    @Autowired
    private ITaskExecutorClient taskExecutorClient;

    @Getter
    private final ConcurrentHashMap<String, WorkerGroupTaskDispatcher> dispatchWorkerMap;

    public WorkerGroupTaskDispatcherManager() {
        dispatchWorkerMap = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void init() {
        this.addWorkerGroup(WorkerGroupUtils.getDefaultWorkerGroup());
    }

    /**
     * Adds a task to the specified worker group queue and starts or wakes up the corresponding processing loop.
     *
     * @param workerGroup the identifier for the worker group, used to distinguish different task queues
     * @param taskExecutionRunnable an instance of ITaskExecutionRunnable representing the task to be executed
     * @param delayTimeMills the delay time before the task is executed, in milliseconds
     * @return true if the task is successfully added to the queue, false workerGroupTaskDispatcher not found
     */
    public synchronized boolean addTaskToWorkerGroup(String workerGroup, ITaskExecutionRunnable taskExecutionRunnable,
                                                     long delayTimeMills) {
        WorkerGroupTaskDispatcher workerGroupTaskDispatcher = dispatchWorkerMap.get(workerGroup);
        if (workerGroupTaskDispatcher != null) {
            workerGroupTaskDispatcher.addTaskToWorkerGroupQueue(taskExecutionRunnable, delayTimeMills);
            return true;
        } else {
            log.error("workerGroupTaskDispatcher {} not found, will set task {} fail",
                    workerGroup, taskExecutionRunnable.getTaskInstance().getId());
        }
        return false;
    }

    /**
     * Stops a specific worker group's task dispatch waiting queue looper.
     *
     * @param workerGroup the identifier for the worker group
     */
    private synchronized void deleteWorkerGroup(String workerGroup) {
        WorkerGroupTaskDispatcher workerGroupTaskDispatcher = dispatchWorkerMap.get(workerGroup);
        if (workerGroupTaskDispatcher != null) {
            workerGroupTaskDispatcher.close();
        } else {
            log.warn("workerGroupTaskDispatcher {} not found", workerGroup);
        }
    }

    /**
     * add workerGroup
     *
     * @param workerGroup the identifier for the worker group
     */
    private synchronized void addWorkerGroup(String workerGroup) {
        log.info("add workerGroup: {}", workerGroup);
        WorkerGroupTaskDispatcher workerGroupTaskDispatcher = dispatchWorkerMap.computeIfAbsent(
                workerGroup, key -> new WorkerGroupTaskDispatcher(workerGroup, taskExecutorClient));
        workerGroupTaskDispatcher.start();
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

    @Override
    public void onWorkerGroupAdd(List<WorkerGroup> workerGroups) {
        for (WorkerGroup workerGroup : workerGroups) {
            this.addWorkerGroup(workerGroup.getName());
        }
    }

    @Override
    public void onWorkerGroupChange(List<WorkerGroup> workerGroups) {
        // Worker group changes will trigger add and delete events.
        // There is no need to handle the change events here; just log the records.
        log.info("on change worker groups: {}", workerGroups);
    }

    @Override
    public void onWorkerGroupDelete(List<WorkerGroup> workerGroups) {
        for (WorkerGroup workerGroup : workerGroups) {
            try {
                this.deleteWorkerGroup(workerGroup.getName());
            } catch (Exception e) {
                log.error("Delete worker group: {} from WorkerGroupTaskDispatcherManager error", workerGroup.getName(),
                        e);
            }
        }
    }
}
