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

package org.apache.dolphinscheduler.task.executor.eventbus;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;
import org.apache.dolphinscheduler.task.executor.ITaskExecutorRepository;
import org.apache.dolphinscheduler.task.executor.events.AbstractTaskExecutorLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.ITaskExecutorLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorDispatchedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorFailedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorFinalizeLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorKillLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorKilledLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorPauseLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorPausedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorRuntimeContextChangedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorStartedLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorSuccessLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.listener.ITaskExecutorLifecycleEventListener;
import org.apache.dolphinscheduler.task.executor.log.TaskExecutorMDCUtils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskExecutorEventBusCoordinator implements ITaskExecutorEventBusCoordinator {

    private final String coordinatorName;

    private final ITaskExecutorRepository taskExecutorRepository;

    private final List<ITaskExecutorLifecycleEventListener> taskExecutorLifecycleEventListeners;

    private static final int DEFAULT_WORKER_SIZE = Runtime.getRuntime().availableProcessors();

    private static final long DEFAULT_FIRE_INTERVAL = 50;

    private static final Set<Integer> firingTaskExecutorIds = ConcurrentHashMap.newKeySet();

    private ScheduledExecutorService mainExecutorThreadPool;

    private ThreadPoolExecutor workerExecutorThreadPool;

    public TaskExecutorEventBusCoordinator(final String coordinatorName,
                                           final ITaskExecutorRepository taskExecutorRepository) {
        this.coordinatorName = coordinatorName;
        this.taskExecutorRepository = taskExecutorRepository;
        this.taskExecutorLifecycleEventListeners = new ArrayList<>();
    }

    public void start() {
        mainExecutorThreadPool = ThreadUtils.newDaemonScheduledExecutorService(
                coordinatorName + "-eventbus-coordinator-main-%d", 1);
        // todo: use a event dispatcher to control a condition to fire the event
        mainExecutorThreadPool.scheduleWithFixedDelay(
                this::fireTaskExecutorEventBus,
                0,
                DEFAULT_FIRE_INTERVAL,
                TimeUnit.MILLISECONDS);

        workerExecutorThreadPool = ThreadUtils.newDaemonFixedThreadExecutor(
                coordinatorName + "-eventbus-coordinator-worker-%d", DEFAULT_WORKER_SIZE);
        log.info("{} started, worker size: {}", coordinatorName, DEFAULT_WORKER_SIZE);
    }

    @Override
    public void registerTaskExecutorLifecycleEventListener(final ITaskExecutorLifecycleEventListener taskExecutorLifecycleEventListener) {
        checkNotNull(taskExecutorLifecycleEventListener);
        taskExecutorLifecycleEventListeners.add(taskExecutorLifecycleEventListener);
    }

    @Override
    public void close() {
        mainExecutorThreadPool.shutdownNow();
        log.info("{} closed", coordinatorName);
    }

    private void fireTaskExecutorEventBus() {
        try {

            final Collection<ITaskExecutor> taskExecutors = taskExecutorRepository.getAll();
            if (CollectionUtils.isEmpty(taskExecutors)) {
                return;
            }
            for (final ITaskExecutor taskExecutor : taskExecutors) {
                if (isFiring(taskExecutor)) {
                    continue;
                }
                final Integer taskExecutorId = taskExecutor.getId();
                CompletableFuture
                        .runAsync(() -> firingTaskExecutorIds.add(taskExecutorId), workerExecutorThreadPool)
                        .thenAccept(v -> doFireTaskExecutorEventBus(taskExecutor))
                        .whenComplete((v, e) -> firingTaskExecutorIds.remove(taskExecutorId));
            }
        } catch (Throwable throwable) {
            log.error("Fire TaskExecutorEventBus error", throwable);
        }
    }

    private void doFireTaskExecutorEventBus(final ITaskExecutor taskExecutor) {
        try (final TaskExecutorMDCUtils.MDCAutoClosable ignored = TaskExecutorMDCUtils.logWithMDC(taskExecutor)) {
            final TaskExecutorEventBus taskExecutorEventBus = taskExecutor.getTaskExecutorEventBus();
            if (taskExecutorEventBus.isEmpty()) {
                return;
            }
            Optional<AbstractTaskExecutorLifecycleEvent> headEventOptional = taskExecutorEventBus.poll();
            if (!headEventOptional.isPresent()) {
                return;
            }
            final ITaskExecutorLifecycleEvent taskExecutorLifecycleEvent = headEventOptional.get();
            try {
                for (final ITaskExecutorLifecycleEventListener taskExecutorLifecycleEventListener : taskExecutorLifecycleEventListeners) {
                    switch (taskExecutorLifecycleEvent.getType()) {
                        case DISPATCHED:
                            taskExecutorLifecycleEventListener.onTaskExecutorDispatchedLifecycleEvent(
                                    ((TaskExecutorDispatchedLifecycleEvent) taskExecutorLifecycleEvent));
                            break;
                        case RUNNING:
                            taskExecutorLifecycleEventListener.onTaskExecutorStartedLifecycleEvent(
                                    ((TaskExecutorStartedLifecycleEvent) taskExecutorLifecycleEvent));
                            break;
                        case RUNTIME_CONTEXT_CHANGE:
                            taskExecutorLifecycleEventListener.onTaskExecutorRuntimeContextChangedEvent(
                                    ((TaskExecutorRuntimeContextChangedLifecycleEvent) taskExecutorLifecycleEvent));
                            break;
                        case PAUSE:
                            taskExecutorLifecycleEventListener.onTaskExecutorPauseLifecycleEvent(
                                    ((TaskExecutorPauseLifecycleEvent) taskExecutorLifecycleEvent));
                            break;
                        case PAUSED:
                            taskExecutorLifecycleEventListener.onTaskExecutorPausedLifecycleEvent(
                                    ((TaskExecutorPausedLifecycleEvent) taskExecutorLifecycleEvent));
                            break;
                        case KILL:
                            taskExecutorLifecycleEventListener.onTaskExecutorKillLifecycleEvent(
                                    ((TaskExecutorKillLifecycleEvent) taskExecutorLifecycleEvent));
                            break;
                        case KILLED:
                            taskExecutorLifecycleEventListener.onTaskExecutorKilledLifecycleEvent(
                                    ((TaskExecutorKilledLifecycleEvent) taskExecutorLifecycleEvent));
                            break;
                        case SUCCESS:
                            taskExecutorLifecycleEventListener.onTaskExecutorSuccessLifecycleEvent(
                                    ((TaskExecutorSuccessLifecycleEvent) taskExecutorLifecycleEvent));
                            break;
                        case FAILED:
                            taskExecutorLifecycleEventListener.onTaskExecutorFailLifecycleEvent(
                                    ((TaskExecutorFailedLifecycleEvent) taskExecutorLifecycleEvent));
                            break;
                        case FINALIZE:
                            taskExecutorLifecycleEventListener.onTaskExecutorFinalizeLifecycleEvent(
                                    ((TaskExecutorFinalizeLifecycleEvent) taskExecutorLifecycleEvent));
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    "Unsupported TaskExecutorLifecycleEvent: " + taskExecutorLifecycleEvent);
                    }
                }
                log.info("Success fire {}: {} ",
                        taskExecutorLifecycleEvent.getClass().getSimpleName(),
                        JSONUtils.toPrettyJsonString(taskExecutorLifecycleEvent));
            } catch (Exception e) {
                log.error("Fire TaskExecutorLifecycleEvent: {} error", taskExecutorLifecycleEvent, e);
            }
        }
    }

    private boolean isFiring(final ITaskExecutor taskExecutor) {
        return firingTaskExecutorIds.contains(taskExecutor.getId());
    }

}
