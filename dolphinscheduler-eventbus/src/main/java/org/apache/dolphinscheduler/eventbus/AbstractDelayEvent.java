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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import lombok.Builder;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * The abstract class of delay event, the event will be triggered after the delay time.
 * <p> You can extend this class to implement your own delay event.
 */
@ToString
@SuperBuilder
public abstract class AbstractDelayEvent implements IEvent, Delayed {

    private static final long DEFAULT_DELAY_TIME = 0;

    protected long delayTime;

    @Builder.Default
    protected long createTimeInNano = System.nanoTime();

    public AbstractDelayEvent() {
        this(DEFAULT_DELAY_TIME);
    }

    public AbstractDelayEvent(final long delayTime) {
        this(delayTime, System.nanoTime());
    }

    public AbstractDelayEvent(final long delayTime, final long createTimeInNano) {
        this.delayTime = delayTime;
        this.createTimeInNano = createTimeInNano;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long delay = createTimeInNano + delayTime * 1_000_000 - System.nanoTime();
        return unit.convert(delay, TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        return Long.compare(this.createTimeInNano, ((AbstractDelayEvent) other).createTimeInNano);
    }

}
