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

package org.apache.dolphinscheduler.server.master.runner.queue;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

public class PriorityAndDelayBasedTaskEntry<V extends Comparable<V>> extends DelayEntry<V> {

    public PriorityAndDelayBasedTaskEntry(long delayTimeMills, V data) {
        super(delayTimeMills, data);
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        return super.getDelay(unit);
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
        return super.compareTo(o);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
