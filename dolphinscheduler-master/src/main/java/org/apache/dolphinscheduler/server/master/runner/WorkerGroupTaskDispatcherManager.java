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
import org.apache.dolphinscheduler.server.master.utils.MasterThreadFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    private static final int SHUTDOWN_WAIT_TIME = 5;

    @Autowired
    private ITaskExecutorClient taskExecutorClient;

    @Getter
    private final ConcurrentHashMap<String, WorkerGroupTaskDispatcher> dispatchWorkerMap;

    private final ScheduledExecutorService scheduler;

    private boolean shutDownFlag;

    public WorkerGroupTaskDispatcherManager() {
        dispatchWorkerMap = new ConcurrentHashMap<>();
        scheduler = MasterThreadFactory.getDefaultSchedulerThreadExecutor();
        shutDownFlag = false;
        scheduler.scheduleAtFixedRate(this::checkDeleteDispatchWorkerComplete, 0, 1, TimeUnit.SECONDS);
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
     */
    public Boolean add(String workerGroup, ITaskExecutionRunnable taskExecutionRunnable, long delayTimeMills) {
        WorkerGroupTaskDispatcher workerGroupTaskDispatcher = dispatchWorkerMap.get(workerGroup);
        if (workerGroupTaskDispatcher != null) {
            workerGroupTaskDispatcher.add(taskExecutionRunnable, delayTimeMills);
            log.info("queue size {}", workerGroupTaskDispatcher.size());
            return true;
        } else {
            log.error("workerGroupTaskDispatcher {} not found", workerGroup);
        }
        return false;
    }

    /**
     * Stops a specific worker group's task dispatch waiting queue looper.
     *
     * @param workerGroup the identifier for the worker group
     */
    public synchronized void deleteWorkerGroup(String workerGroup) throws Exception {
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
    public synchronized void addWorkerGroup(String workerGroup) {
        WorkerGroupTaskDispatcher looper =
                dispatchWorkerMap.computeIfAbsent(workerGroup,
                        k -> new WorkerGroupTaskDispatcher(workerGroup, taskExecutorClient));
        looper.start();
    }

    /**
     * Stop all workerGroupTaskDispatchWaitingQueueLooper
     */
    @Override
    public void close() throws Exception {
        log.info("WorkerGroupTaskDispatcherManager ready close...");
        shutDownFlag = true;
        for (Map.Entry<String, WorkerGroupTaskDispatcher> entry : dispatchWorkerMap.entrySet()) {
            try {
                entry.getValue().close();
            } catch (Exception e) {
                log.error("stop worker group error", e);
            }
        }
    }

    @Override
    public void onWorkerGroupAdd(List<WorkerGroup> workerGroups) {
        for (WorkerGroup workerGroup : workerGroups) {
            this.addWorkerGroup(workerGroup.getName());
        }
    }

    @Override
    public void onWorkerGroupChange(List<WorkerGroup> workerGroups) {
        String workerGroupsString = workerGroups.stream()
                .map(WorkerGroup::getName)
                .collect(Collectors.joining(", "));
        log.info("Worker groups: {}", workerGroupsString);
    }

    @Override
    public void onWorkerGroupDelete(List<WorkerGroup> workerGroups) {
        for (WorkerGroup workerGroup : workerGroups) {
            try {
                this.deleteWorkerGroup(workerGroup.getName());
            } catch (Exception e) {
                log.error("stop worker group error", e);
            }
        }
    }

    private void checkDeleteDispatchWorkerComplete() {
        boolean complete = true;
        for (Map.Entry<String, WorkerGroupTaskDispatcher> entry : dispatchWorkerMap.entrySet()) {
            String workerGroup = entry.getKey();
            WorkerGroupTaskDispatcher workerGroupTaskDispatcher = entry.getValue();
            switch (workerGroupTaskDispatcher.getStatus()) {
                case DELETING:
                    try (WorkerGroupTaskDispatcher ignored = workerGroupTaskDispatcher) {
                        log.info("try to delete worker group {}", workerGroup);
                    } catch (Exception e) {
                        log.error("stop worker group error", e);
                    }
                    complete = false;
                    break;
                case DELETE_SUCCESS:
                    try (WorkerGroupTaskDispatcher ignored = dispatchWorkerMap.remove(workerGroup)) {
                        log.info("success remove worker group {}", workerGroup);
                    } catch (Exception e) {
                        log.error("stop worker group error", e);
                    }
                    break;
                default:
                    complete = false;
                    log.debug("worker group {} status {}", workerGroup, workerGroupTaskDispatcher.getStatus());
                    break;
            }
        }
        if (shutDownFlag && complete) {
            this.shutdown();
        }
    }

    private void shutdown() {
        log.info("WorkerGroupTaskDispatcherManager start close...");
        dispatchWorkerMap.clear();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(SHUTDOWN_WAIT_TIME, TimeUnit.SECONDS)) {
                log.warn(
                        "WorkerGroupTaskDispatcherManager did not terminate within SHUTDOWN_WAIT_TIME seconds, shutting down now");
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        } catch (InterruptedException e) {
            log.info("WorkerGroupTaskDispatcherManager error: ", e);
        }
        log.info("WorkerGroupTaskDispatcherManager closed");
    }

}
