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

public abstract class AbstractEventPendingQueue<T> implements EventPendingQueue<T> {

    private final LinkedBlockingQueue<T> pendingAlertQueue;

    private final int capacity;

    protected AbstractEventPendingQueue(int capacity) {
        this.capacity = capacity;
        this.pendingAlertQueue = new LinkedBlockingQueue<>(capacity);
    }

    @Override
    public void put(T alert) throws InterruptedException {
        pendingAlertQueue.put(alert);
    }

    @Override
    public T take() throws InterruptedException {
        return pendingAlertQueue.take();
    }

    @Override
    public int size() {
        return pendingAlertQueue.size();
    }

    @Override
    public int capacity() {
        return capacity;
    }

}
