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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractEventPendingQueue<T> implements EventPendingQueue<T> {

    private volatile EventRun<T> eventRun;

    private final AtomicInteger handlingEventCount = new AtomicInteger();

    private boolean active;

    private final int capacity;

    protected AbstractEventPendingQueue(int capacity) {
        this.capacity = capacity;
        this.eventRun = new EventRun<>(capacity);
    }

    @Override
    public void put(T alert) throws InterruptedException {
        eventRun.pendingEvents.put(alert);
    }

    @Override
    public T take() throws InterruptedException {
        return eventRun.pendingEvents.take();
    }

    @Override
    public int size() {
        return eventRun.pendingEvents.size();
    }

    @Override
    public int capacity() {
        return capacity;
    }

    synchronized void activate() {
        eventRun = new EventRun<>(capacity);
        active = true;
    }

    synchronized void deactivate() {
        active = false;
        eventRun.pendingEvents.clear();
    }

    synchronized EventRun<T> getActiveRun() {
        return active ? eventRun : null;
    }

    synchronized boolean isActiveRun(EventRun<T> run) {
        return active && eventRun == run;
    }

    synchronized boolean reserveDispatch(EventRun<T> run) {
        if (!isActiveRun(run)) {
            return false;
        }
        handlingEventCount.incrementAndGet();
        return true;
    }

    void releaseDispatch() {
        handlingEventCount.decrementAndGet();
    }

    int getHandlingEventCount() {
        return handlingEventCount.get();
    }

    static final class EventRun<T> {

        final LinkedBlockingQueue<T> pendingEvents;

        int eventOffset = -1;

        boolean initialized;

        EventRun(int capacity) {
            pendingEvents = new LinkedBlockingQueue<>(capacity);
        }
    }
}
