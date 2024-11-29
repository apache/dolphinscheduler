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

import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.task.executor.events.IReportableTaskExecutorLifecycleEvent;
import org.apache.dolphinscheduler.task.executor.events.TaskExecutorLifecycleEventType;
import org.apache.dolphinscheduler.task.executor.log.TaskExecutorMDCUtils;

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

    public TaskExecutorLifecycleEventRemoteReporter(final String reporterName,
                                                    final ITaskExecutorEventRemoteReporterClient taskExecutorEventRemoteReporterClient) {
        super(reporterName);
        this.reporterName = reporterName;
        this.taskExecutorEventRemoteReporterClient = taskExecutorEventRemoteReporterClient;
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
            log.debug("Report : {}", JSONUtils.toPrettyJsonString(reportableTaskExecutorLifecycleEvent));
            int taskInstanceId = reportableTaskExecutorLifecycleEvent.getTaskInstanceId();
            eventChannels.computeIfAbsent(
                    taskInstanceId,
                    k -> new ReportableTaskExecutorLifecycleEventChannel(taskInstanceId))
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
    public boolean reassignWorkflowInstanceHost(int taskInstanceId, String workflowHost) {
        eventChannelsLock.lock();
        try {
            final ReportableTaskExecutorLifecycleEventChannel eventChannel = eventChannels.get(taskInstanceId);
            if (eventChannel == null) {
                return false;
            }
            eventChannel.taskExecutionEventsQueue.forEach(event -> event.setWorkflowInstanceHost(workflowHost));
            return true;
        } finally {
            eventChannelsLock.unlock();
        }
    }

    @Override
    public void close() {
        // shutdown the thread
        runningFlag = false;
        log.info("{} closed", reporterName);
    }

    @VisibleForTesting
    public Map<Integer, ReportableTaskExecutorLifecycleEventChannel> getEventChannels() {
        return eventChannels;
    }

    private void handleTaskExecutionEventChannel(final ReportableTaskExecutorLifecycleEventChannel reportableTaskExecutorLifecycleEventChannel) {
        if (reportableTaskExecutorLifecycleEventChannel.isEmpty()) {
            return;
        }
        while (!reportableTaskExecutorLifecycleEventChannel.isEmpty()) {
            final IReportableTaskExecutorLifecycleEvent headEvent = reportableTaskExecutorLifecycleEventChannel.peek();
            try (
                    final TaskExecutorMDCUtils.MDCAutoClosable ignore =
                            TaskExecutorMDCUtils.logWithMDC(headEvent.getTaskInstanceId())) {
                try {
                    if (isTaskExecutorEventNeverSent(headEvent) || isRetryIntervalExceeded(headEvent)) {
                        taskExecutorEventRemoteReporterClient.reportTaskExecutionEventToMaster(headEvent);
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
        while (isAllTaskExecutorEventChannelEmpty()) {
            taskExecutionEventEmptyCondition.await();
        }
        eventChannelsLock.unlock();
    }

    private void waitIfAnyTaskExecutionEventChannelRetryIntervalPassed() throws InterruptedException {
        eventChannelsLock.lock();
        try {
            final long waitInterval =
                    (getOldestReportTime() + DEFAULT_TASK_EXECUTOR_EVENT_RETRY_INTERVAL) - System.currentTimeMillis();
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

        // todo: remove the master address from the channel, we need to get the master address from the TaskExecutor
        public ReportableTaskExecutorLifecycleEventChannel(int taskExecutorId) {
            this.taskExecutorId = taskExecutorId;
            this.taskExecutionEventsQueue = new LinkedBlockingQueue<>();
        }

        public void addTaskExecutionEvent(final IReportableTaskExecutorLifecycleEvent reportableTaskExecutorLifecycleEvent) {
            taskExecutionEventsQueue.add(reportableTaskExecutorLifecycleEvent);
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
