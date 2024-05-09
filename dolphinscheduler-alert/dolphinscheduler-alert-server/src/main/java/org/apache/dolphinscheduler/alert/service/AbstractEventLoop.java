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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractEventLoop<T> extends BaseDaemonThread implements EventLoop<T> {

    private final EventPendingQueue<T> eventPendingQueue;

    private final AtomicInteger handlingEventCount;

    private final int eventHandleWorkerNum;

    private final ThreadPoolExecutor threadPoolExecutor;

    private final AtomicBoolean runningFlag = new AtomicBoolean(false);

    protected AbstractEventLoop(String name,
                                ThreadPoolExecutor threadPoolExecutor,
                                EventPendingQueue<T> eventPendingQueue) {
        super(name);
        this.handlingEventCount = new AtomicInteger(0);
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
                if (handlingEventCount.get() >= eventHandleWorkerNum) {
                    log.debug("There is no idle event worker, waiting for a while...");
                    Thread.sleep(1000);
                    continue;
                }
                T pendingEvent = eventPendingQueue.take();
                handlingEventCount.incrementAndGet();
                CompletableFuture.runAsync(() -> handleEvent(pendingEvent), threadPoolExecutor)
                        .whenComplete((aVoid, throwable) -> {
                            if (throwable != null) {
                                log.error("Handle event: {} error", pendingEvent, throwable);
                            }
                            handlingEventCount.decrementAndGet();
                        });
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
        return handlingEventCount.get();
    }

    @Override
    public void shutdown() {
        if (!runningFlag.compareAndSet(true, false)) {
            log.warn(getClass().getName() + " is not started");
        }
    }
}
