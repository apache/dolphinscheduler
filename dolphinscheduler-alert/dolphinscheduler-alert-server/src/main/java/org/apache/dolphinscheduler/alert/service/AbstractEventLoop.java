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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractEventLoop<T> extends BaseDaemonThread implements EventLoop<T> {

    private final AbstractEventPendingQueue<T> eventPendingQueue;

    private final AlertHAServer alertHAServer;

    private final int eventHandleWorkerNum;

    private final ThreadPoolExecutor threadPoolExecutor;

    private final AtomicBoolean runningFlag = new AtomicBoolean(false);

    protected AbstractEventLoop(String name,
                                AlertHAServer alertHAServer,
                                ThreadPoolExecutor threadPoolExecutor,
                                AbstractEventPendingQueue<T> eventPendingQueue) {
        super(name);
        this.alertHAServer = alertHAServer;
        this.eventHandleWorkerNum = threadPoolExecutor.getMaximumPoolSize();
        this.threadPoolExecutor = threadPoolExecutor;
        this.eventPendingQueue = eventPendingQueue;
    }

    @Override
    public synchronized void start() {
        if (!runningFlag.compareAndSet(false, true)) {
            throw new IllegalArgumentException(getClass().getName() + " is already started");
        }
        log.info("{} starting...", getClass().getName());
        super.start();
        log.info("{} started...", getClass().getName());
    }

    @Override
    public void run() {
        while (runningFlag.get()) {
            try {
                AbstractEventPendingQueue.EventRun<T> run = eventPendingQueue.getActiveRun();
                if (!alertHAServer.isActive() || run == null
                        || eventPendingQueue.getHandlingEventCount() >= eventHandleWorkerNum) {
                    log.debug("Event dispatch is paused or no event worker is idle, waiting for a while...");
                    Thread.sleep(1000);
                    continue;
                }
                T pendingEvent = run.pendingEvents.poll(1, TimeUnit.SECONDS);
                if (pendingEvent == null || !alertHAServer.isActive() || !eventPendingQueue.reserveDispatch(run)) {
                    continue;
                }
                try {
                    CompletableFuture.runAsync(() -> {
                        synchronized (eventPendingQueue) {
                            if (!runningFlag.get() || !alertHAServer.isActive()
                                    || !eventPendingQueue.isActiveRun(run)) {
                                return;
                            }
                            // A send admitted here may finish after demotion; queued tasks from old runs are discarded.
                        }
                        handleEvent(pendingEvent);
                    }, threadPoolExecutor).whenComplete((aVoid, throwable) -> {
                        if (throwable != null) {
                            log.error("Handle event: {} error", pendingEvent, throwable);
                        }
                        eventPendingQueue.releaseDispatch();
                    });
                } catch (RuntimeException ex) {
                    eventPendingQueue.releaseDispatch();
                    throw ex;
                }
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                log.error("Loop event thread has been interrupted...");
                break;
            } catch (Exception ex) {
                log.error("Loop event error", ex);
            }
        }
    }

    @Override
    public int getHandlingEventCount() {
        return eventPendingQueue.getHandlingEventCount();
    }

    @Override
    public void shutdown() {
        if (!runningFlag.compareAndSet(true, false)) {
            log.warn(getClass().getName() + " is not started");
        }
    }
}
