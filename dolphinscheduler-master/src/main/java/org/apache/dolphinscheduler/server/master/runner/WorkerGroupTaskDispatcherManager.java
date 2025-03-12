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
import org.apache.dolphinscheduler.server.master.runner.queue.DelayEntry;
import org.apache.dolphinscheduler.server.master.runner.queue.PriorityDelayQueue;
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
    private final ConcurrentHashMap<String, DispatchWorker> dispatchWorkerMap;
    @Getter
    private final ConcurrentHashMap<String, PriorityDelayQueue<DelayEntry<ITaskExecutionRunnable>>> workerGroupPriorityDelayQueueMap;

    private final ScheduledExecutorService scheduler;

    public WorkerGroupTaskDispatcherManager() {
        dispatchWorkerMap = new ConcurrentHashMap<>();
        workerGroupPriorityDelayQueueMap = new ConcurrentHashMap<>();
        scheduler = MasterThreadFactory.getDefaultSchedulerThreadExecutor();

        scheduler.scheduleAtFixedRate(this::checkDeleteDispatchWorker, 0, 1, TimeUnit.SECONDS);
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
    public void add(String workerGroup, ITaskExecutionRunnable taskExecutionRunnable, long delayTimeMills) {
        PriorityDelayQueue<DelayEntry<ITaskExecutionRunnable>> workerGroupQueue =
                workerGroupPriorityDelayQueueMap.get(workerGroup);
        if (workerGroupQueue != null) {
            workerGroupQueue.add(new DelayEntry<>(delayTimeMills, taskExecutionRunnable));
            log.info("queue size {}", workerGroupQueue.size());
        } else {
            log.error("workerGroup {} not found", workerGroup);
        }
    }

    /**
     * Stops a specific worker group's task dispatch waiting queue looper.
     *
     * @param workerGroup the identifier for the worker group
     */
    public synchronized void deleteWorkerGroup(String workerGroup) throws Exception {
        DispatchWorker dispatchWorker = dispatchWorkerMap.get(workerGroup);
        if (dispatchWorker != null) {
            dispatchWorker.close();
        }
    }

    /**
     * add workerGroup
     *
     * @param workerGroup the identifier for the worker group
     */
    public synchronized void addWorkerGroup(String workerGroup) {
        PriorityDelayQueue<DelayEntry<ITaskExecutionRunnable>> workerGroupQueue =
                workerGroupPriorityDelayQueueMap.computeIfAbsent(workerGroup, k -> new PriorityDelayQueue<>());
        DispatchWorker looper =
                dispatchWorkerMap.computeIfAbsent(workerGroup,
                        k -> new DispatchWorker(workerGroup, taskExecutorClient,
                                workerGroupQueue));
        looper.start();
    }

    /**
     * Stop all workerGroupTaskDispatchWaitingQueueLooper
     */
    @Override
    public void close() throws Exception {
        log.info("WorkerGroupTaskDispatcherManager stopping...");
        scheduler.shutdown();
        if (!scheduler.awaitTermination(SHUTDOWN_WAIT_TIME, TimeUnit.SECONDS)) {
            log.warn("WorkerGroupTaskDispatcherManager did not terminate within 10 seconds, shutting down now");
            scheduler.shutdownNow();
        }
        log.info("WorkerGroupTaskDispatcherManager stopped");
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

    private void checkDeleteDispatchWorker() {
        for (Map.Entry<String, DispatchWorker> entry : dispatchWorkerMap.entrySet()) {
            String workerGroup = entry.getKey();
            DispatchWorker dispatchWorker = entry.getValue();
            switch (dispatchWorker.getStatus()) {
                case DELETING:
                    try (DispatchWorker ignored = dispatchWorker) {
                        log.info("try to delete worker group {}", workerGroup);
                    } catch (Exception e) {
                        log.error("stop worker group error", e);
                    }
                    break;
                case DELETE_SUCCESS:
                    try (DispatchWorker ignored = dispatchWorkerMap.remove(workerGroup)) {
                        log.info("success remove worker group {}", workerGroup);
                    } catch (Exception e) {
                        log.error("stop worker group error", e);
                    }
                    break;
                default:
                    log.debug("worker group {} status {}", workerGroup, dispatchWorker.getStatus());
                    break;
            }
        }
    }

}
