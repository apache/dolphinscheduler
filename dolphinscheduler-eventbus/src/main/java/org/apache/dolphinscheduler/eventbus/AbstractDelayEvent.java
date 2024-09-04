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

/**
 * The abstract class of delay event, the event will be triggered after the delay time.
 * <p> You can extend this class to implement your own delay event.
 */
public abstract class AbstractDelayEvent implements IEvent, Delayed {

    protected long delayTime;

    protected long triggerTimeInMillis;

    public AbstractDelayEvent() {
        this(0);
    }

    public AbstractDelayEvent(long delayTime) {
        if (delayTime == 0) {
            this.triggerTimeInMillis = System.currentTimeMillis();
        } else {
            this.triggerTimeInMillis = System.currentTimeMillis() + delayTime;
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long delay = triggerTimeInMillis - System.currentTimeMillis();
        return unit.convert(delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        return Long.compare(this.triggerTimeInMillis, ((AbstractDelayEvent) other).triggerTimeInMillis);
    }

}
