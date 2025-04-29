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

package org.apache.dolphinscheduler.server.master.runner.events;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.eventbus.AbstractDelayEvent;

import lombok.Getter;

import org.jetbrains.annotations.NotNull;

@Getter
public abstract class AbstractTaskDispatchEntryEvent<V extends Comparable<V>> extends AbstractDelayEvent {

    protected final V data;

    public AbstractTaskDispatchEntryEvent(long delayTimeMills, V data) {
        super(delayTimeMills);
        this.data = checkNotNull(data, "data is null");
    }

    public int compareTo(@NotNull AbstractTaskDispatchEntryEvent<V> other) {
        return super.compareTo(other);
    }
}
