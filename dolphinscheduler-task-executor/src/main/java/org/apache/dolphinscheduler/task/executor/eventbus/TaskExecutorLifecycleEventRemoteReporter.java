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

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.log.TaskLogMarkers;
import org.apache.dolphinscheduler.task.executor.ITaskExecutorRepository;
import org.apache.dolphinscheduler.task.executor.events.IReportableTaskExecutorLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorLifecycleEventType;
import org.apache.dolphinscheduler.task.executor.log.TaskExecutorMDCUtils;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import com.google.common.annotations.VisibleForTesting;

@Slf4j
public class TaskExecutorLifecycleEventRemoteReporter extends BaseDaemonThread
        implements
            ITaskExecutorLifecycleEventReporter {

    private static final Long DEFAULT_TASK_EXECUTOR_EVENT_RETRY_INTERVAL = TimeUnit.MINUTES.toMillis(3);

    private final String reporterName;

    private final Map<Integer, ReportableTaskExecutorLifecycleEventChannel> eventChannels = new ConcurrentHashMap<>();

    private final ITaskExecutorEventRemoteReporterClient taskExecutorEventRemoteReporterClient;

    private volatile boolean runningFlag;

    private final Lock eventChannelsLock = new ReentrantLock();

    private final Condition taskExecutionEventEmptyCondition = eventChannelsLock.newCondition();

    private final ITaskExecutorRepository taskExecutorRepository;

    private final long taskEventReportMaxDurationMillis;

    public TaskExecutorLifecycleEventRemoteReporter(final String reporterName,
                                                    final ITaskExecutorEventRemoteReporterClient taskExecutorEventRemoteReporterClient,
                                                    final ITaskExecutorRepository taskExecutorRepository,
                                                    final Duration taskEventReportMaxDuration) {
        super(reporterName);
        checkArgument(taskEventReportMaxDuration != null && taskEventReportMaxDuration.toMillis() > 0,
                "Task event report max duration must be positive");
        this.reporterName = reporterName;
        this.taskExecutorEventRemoteReporterClient = taskExecutorEventRemoteReporterClient;
        this.taskExecutorRepository = taskExecutorRepository;
        this.taskEventReportMaxDurationMillis = taskEventReportMaxDuration.toMillis();
    }

    @Override
    public void start() {
        // start a thread to send the events
        this.runningFlag = true;
        super.start();
        log.info("{} started", reporterName);
    }

    @Override
    public void run() {
        while (runningFlag) {
            try {
                for (final ReportableTaskExecutorLifecycleEventChannel eventChannel : eventChannels.values()) {
                    if (expireFinishedTaskEventChannel(eventChannel)) {
                        continue;
                    }
                    if (eventChannel.isEmpty()) {
                        continue;
                    }
                    handleTaskExecutionEventChannel(eventChannel);
                }
                tryToWaitIfAllTaskExecutionEventChannelEmpty();
                waitIfAnyTaskExecutionEventChannelRetryIntervalPassed();
            } catch (InterruptedException e) {
                log.info("{} interrupted", reporterName);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                log.error("Fire ReportableTaskExecutorLifecycleEventChannel error", ex);
            }
        }
        log.info("{} break loop", reporterName);
    }

    @Override
    public void reportTaskExecutorLifecycleEvent(final IReportableTaskExecutorLifecycleEvent reportableTaskExecutorLifecycleEvent) {
        eventChannelsLock.lock();
        try {
            log.info(TaskLogMarkers.excludeInTaskLog(), "Report : {}",
                    JSONUtils.toPrettyJsonString(reportableTaskExecutorLifecycleEvent));
            int taskInstanceId = reportableTaskExecutorLifecycleEvent.getTaskInstanceId();
            eventChannels.computeIfAbsent(
                    taskInstanceId,
                    k -> new ReportableTaskExecutorLifecycleEventChannel(taskInstanceId,
                            taskExecutorRepository.get(taskInstanceId)
                                    .orElseThrow(
                                            () -> new IllegalArgumentException("The TaskExecutor id " + taskInstanceId
                                                    + " is not exist."))
                                    .getTaskExecutionContext().getWorkflowInstanceHost()))
                    .addTaskExecutionEvent(reportableTaskExecutorLifecycleEvent);
            taskExecutionEventEmptyCondition.signalAll();
        } finally {
            eventChannelsLock.unlock();
        }

    }

    @Override
    public void receiveTaskExecutorLifecycleEventACK(final TaskExecutorLifecycleEventAck eventAck) {
        final int taskExecutorId = eventAck.getTaskExecutorId();
        eventChannelsLock.lock();
        try {
            final ReportableTaskExecutorLifecycleEventChannel eventChannel = eventChannels.get(taskExecutorId);
            if (eventChannel == null) {
                return;
            }
            final IReportableTaskExecutorLifecycleEvent removed =
                    eventChannel.remove(eventAck.getTaskExecutorLifecycleEventType());
            if (removed != null) {
                log.info("Success removed {} by ack: {}", removed, eventAck);
            } else {
                log.info("Failed removed ReportableTaskExecutorLifecycleEvent by ack: {}", eventAck);
            }
            if (eventChannel.isEmpty()) {
                eventChannels.remove(taskExecutorId);
                log.debug("Removed ReportableTaskExecutorLifecycleEventChannel: {}", taskExecutorId);
            }
            taskExecutionEventEmptyCondition.signalAll();
        } finally {
            eventChannelsLock.unlock();
        }
    }

    @Override
    public boolean onWorkflowInstanceHostChanged(int taskInstanceId, String workflowInstanceHost) {
        eventChannelsLock.lock();
        try {
            final ReportableTaskExecutorLifecycleEventChannel eventChannel = eventChannels.get(taskInstanceId);
            if (eventChannel != null) {
                eventChannel.workflowInstanceHost = workflowInstanceHost;
                eventChannel.taskExecutionEventsQueue.forEach(event -> event.setLatestReportTime(null));
                taskExecutionEventEmptyCondition.signalAll();
                return true;
            }
            return false;
        } finally {
            eventChannelsLock.unlock();
        }
    }

    @Override
    public void close() {
        // shutdown the thread
        runningFlag = false;
        interrupt();
        log.info("{} closed", reporterName);
    }

    @VisibleForTesting
    public Map<Integer, ReportableTaskExecutorLifecycleEventChannel> getEventChannels() {
        return eventChannels;
    }

    private boolean expireFinishedTaskEventChannel(final ReportableTaskExecutorLifecycleEventChannel eventChannel) {
        eventChannelsLock.lock();
        try {
            if (eventChannel.finishedTime == null
                    || System.currentTimeMillis() - eventChannel.finishedTime < taskEventReportMaxDurationMillis) {
                return false;
            }
            if (eventChannels.remove(eventChannel.getTaskExecutorId(), eventChannel)) {
                log.error("Task executor {} result was not acknowledged within {} ms after completion. "
                        + "Discarding pending events for master {}",
                        eventChannel.getTaskExecutorId(), taskEventReportMaxDurationMillis,
                        eventChannel.workflowInstanceHost);
            }
            return true;
        } finally {
            eventChannelsLock.unlock();
        }
    }

    private void handleTaskExecutionEventChannel(final ReportableTaskExecutorLifecycleEventChannel reportableTaskExecutorLifecycleEventChannel) {
        if (reportableTaskExecutorLifecycleEventChannel.isEmpty()) {
            return;
        }
        while (!reportableTaskExecutorLifecycleEventChannel.isEmpty()) {
            final IReportableTaskExecutorLifecycleEvent headEvent = reportableTaskExecutorLifecycleEventChannel.peek();
            if (headEvent == null) {
                return;
            }
            try (
                    final TaskExecutorMDCUtils.MDCAutoClosable ignore =
                            TaskExecutorMDCUtils.logWithMDC(headEvent.getTaskInstanceId())) {
                try {
                    if (isTaskExecutorEventNeverSent(headEvent) || isRetryIntervalExceeded(headEvent)) {
                        taskExecutorEventRemoteReporterClient.reportTaskExecutionEventToMaster(
                                reportableTaskExecutorLifecycleEventChannel.workflowInstanceHost,
                                headEvent);
                        continue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "The ReportableTaskExecutorLifecycleEvent: {} latest send time: {} doesn't exceeded retry interval",
                                headEvent,
                                headEvent.getLatestReportTime());
                    }
                    break;
                } catch (Exception ex) {
                    log.error("Send TaskExecutionEvent: {} to master error will retry after {} mills",
                            headEvent,
                            DEFAULT_TASK_EXECUTOR_EVENT_RETRY_INTERVAL,
                            ex);
                    break;

                }
            }
        }
    }

    private boolean isAllTaskExecutorEventChannelEmpty() {
        return eventChannels
                .values()
                .stream()
                .allMatch(ReportableTaskExecutorLifecycleEventChannel::isEmpty);
    }

    private long getOldestReportTime() {
        return eventChannels.values()
                .stream()
                .filter(ReportableTaskExecutorLifecycleEventChannel::isNotEmpty)
                .map(ReportableTaskExecutorLifecycleEventChannel::peek)
                .filter(event -> !isTaskExecutorEventNeverSent(event))
                .map(IReportableTaskExecutorLifecycleEvent::getLatestReportTime)
                .min(Long::compareTo)
                .orElse(0L);
    }

    private boolean isTaskExecutorEventNeverSent(final IReportableTaskExecutorLifecycleEvent headEvent) {
        return headEvent.getLatestReportTime() == null;
    }

    private boolean isRetryIntervalExceeded(final IReportableTaskExecutorLifecycleEvent reportableTaskExecutorLifecycleEvent) {
        if (isTaskExecutorEventNeverSent(reportableTaskExecutorLifecycleEvent)) {
            return true;
        }
        long currentTime = System.currentTimeMillis();
        return currentTime - reportableTaskExecutorLifecycleEvent
                .getLatestReportTime() > DEFAULT_TASK_EXECUTOR_EVENT_RETRY_INTERVAL;
    }

    private void tryToWaitIfAllTaskExecutionEventChannelEmpty() throws InterruptedException {
        eventChannelsLock.lock();
        try {
            while (runningFlag && isAllTaskExecutorEventChannelEmpty()) {
                taskExecutionEventEmptyCondition.await();
            }
        } finally {
            eventChannelsLock.unlock();
        }
    }

    private void waitIfAnyTaskExecutionEventChannelRetryIntervalPassed() throws InterruptedException {
        eventChannelsLock.lock();
        try {
            long waitInterval =
                    (getOldestReportTime() + DEFAULT_TASK_EXECUTOR_EVENT_RETRY_INTERVAL) - System.currentTimeMillis();
            for (final ReportableTaskExecutorLifecycleEventChannel eventChannel : eventChannels.values()) {
                if (eventChannel.finishedTime != null) {
                    waitInterval = Math.min(waitInterval,
                            eventChannel.finishedTime + taskEventReportMaxDurationMillis - System.currentTimeMillis());
                }
            }
            if (waitInterval <= 0) {
                return;
            }
            taskExecutionEventEmptyCondition.await(waitInterval, TimeUnit.MILLISECONDS);
        } finally {
            eventChannelsLock.unlock();
        }
    }

    public static class ReportableTaskExecutorLifecycleEventChannel {

        @Getter
        private final int taskExecutorId;

        private final LinkedBlockingQueue<IReportableTaskExecutorLifecycleEvent> taskExecutionEventsQueue;

        private volatile String workflowInstanceHost;

        private volatile Long finishedTime;

        public ReportableTaskExecutorLifecycleEventChannel(int taskExecutorId, String workflowInstanceHost) {
            this.taskExecutorId = taskExecutorId;
            this.workflowInstanceHost = workflowInstanceHost;
            this.taskExecutionEventsQueue = new LinkedBlockingQueue<>();
        }

        public void addTaskExecutionEvent(final IReportableTaskExecutorLifecycleEvent reportableTaskExecutorLifecycleEvent) {
            taskExecutionEventsQueue.add(reportableTaskExecutorLifecycleEvent);
            if (reportableTaskExecutorLifecycleEvent.getType().isFinished() && finishedTime == null) {
                finishedTime = System.currentTimeMillis();
            }
        }

        public IReportableTaskExecutorLifecycleEvent peek() {
            return taskExecutionEventsQueue.peek();
        }

        public IReportableTaskExecutorLifecycleEvent remove(TaskExecutorLifecycleEventType type) {
            final AtomicReference<IReportableTaskExecutorLifecycleEvent> removed = new AtomicReference<>();
            taskExecutionEventsQueue.removeIf(event -> {
                if (event.getType() == type) {
                    removed.set(event);
                    return true;
                }
                return false;
            });
            return removed.get();
        }

        public boolean isEmpty() {
            return taskExecutionEventsQueue.isEmpty();
        }

        public boolean isNotEmpty() {
            return !isEmpty();
        }

    }
}
