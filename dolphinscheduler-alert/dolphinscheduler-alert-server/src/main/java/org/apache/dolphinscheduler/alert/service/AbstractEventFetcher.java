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

package org.apache.dolphinscheduler.alert.service;

import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractEventFetcher<T> extends BaseDaemonThread implements EventFetcher<T> {

    protected static final int FETCH_SIZE = 100;

    protected static final long FETCH_INTERVAL = 5_000;

    protected final AlertHAServer alertHAServer;

    private final AbstractEventPendingQueue<T> eventPendingQueue;

    private final AtomicBoolean runningFlag = new AtomicBoolean(false);

    protected AbstractEventFetcher(String fetcherName,
                                   AlertHAServer alertHAServer,
                                   AbstractEventPendingQueue<T> eventPendingQueue) {
        super(fetcherName);
        this.alertHAServer = alertHAServer;
        this.eventPendingQueue = eventPendingQueue;
    }

    @Override
    public synchronized void start() {
        if (!runningFlag.compareAndSet(false, true)) {
            throw new IllegalArgumentException("AlertEventFetcher is already started");
        }
        log.info("AlertEventFetcher starting...");
        super.start();
        log.info("AlertEventFetcher started...");
    }

    @Override
    public void run() {
        while (runningFlag.get()) {
            try {
                AbstractEventPendingQueue.EventRun<T> run = eventPendingQueue.getActiveRun();
                if (!alertHAServer.isActive() || run == null) {
                    log.debug("The current node is not active, will not loop Alert");
                    Thread.sleep(FETCH_INTERVAL);
                    continue;
                }
                if (!run.initialized) {
                    // Let sends admitted by an earlier activation finish before reloading pending rows.
                    if (eventPendingQueue.getHandlingEventCount() != 0) {
                        Thread.sleep(FETCH_INTERVAL);
                        continue;
                    }
                    run.initialized = true;
                }
                List<T> pendingEvents = fetchPendingEvent(run.eventOffset);
                if (CollectionUtils.isEmpty(pendingEvents)) {
                    log.debug("No pending events found");
                    Thread.sleep(FETCH_INTERVAL);
                    continue;
                }
                for (T alert : pendingEvents) {
                    if (!runningFlag.get() || !alertHAServer.isActive() || !eventPendingQueue.isActiveRun(run)) {
                        break;
                    }
                    // A delayed query or blocked put can only write to the queue of its own activation.
                    run.pendingEvents.put(alert);
                }
                run.eventOffset = Math.max(run.eventOffset,
                        pendingEvents.stream().map(this::getEventOffset).max(Integer::compareTo).get());
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                log.error("AlertEventFetcher error", ex);
            }
        }
    }

    protected abstract int getEventOffset(T event);

    @Override
    public void shutdown() {
        if (!runningFlag.compareAndSet(true, false)) {
            log.warn("The AlertEventFetcher is not started");
        }
    }

}
