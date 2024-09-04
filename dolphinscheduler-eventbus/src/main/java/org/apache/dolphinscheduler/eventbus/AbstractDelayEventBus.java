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

package org.apache.dolphinscheduler.eventbus;

import java.util.Optional;
import java.util.concurrent.DelayQueue;

/**
 * The event bus that supports delay event.
 */
public abstract class AbstractDelayEventBus<T extends AbstractDelayEvent> implements IEventBus<T> {

    protected final DelayQueue<T> delayEventQueue = new DelayQueue<>();

    @Override
    public void publish(final T event) {
        delayEventQueue.add(event);
    }

    @Override
    public Optional<T> poll() {
        return Optional.ofNullable(delayEventQueue.poll());
    }

    @Override
    public Optional<T> peek() {
        return Optional.ofNullable(delayEventQueue.peek());
    }

    @Override
    public Optional<T> remove() {
        return Optional.ofNullable(delayEventQueue.remove());
    }

    @Override
    public boolean isEmpty() {
        return delayEventQueue.isEmpty();
    }
}
