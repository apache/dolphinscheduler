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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import lombok.Getter;

import org.jetbrains.annotations.NotNull;

public class TimeBasedTaskExecutionRunnableComparableEntry<ITaskExecutionRunnable> implements Delayed {

    private final long triggerTimeMills;
    private final long delayTimeMills;

    @Getter
    private final ITaskExecutionRunnable data;
    public TimeBasedTaskExecutionRunnableComparableEntry(long delayTimeMills, ITaskExecutionRunnable data) {
        this.delayTimeMills = delayTimeMills;
        this.triggerTimeMills = System.currentTimeMillis() + delayTimeMills;
        this.data = checkNotNull(data, "data is null");
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        long remainTimeMills = triggerTimeMills - System.currentTimeMillis();
        if (TimeUnit.MILLISECONDS.equals(unit)) {
            return remainTimeMills;
        }
        return unit.convert(remainTimeMills, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(@NotNull Delayed delayed) {
        if (this == delayed) {
            return 0;
        }
        return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), delayed.getDelay(TimeUnit.MILLISECONDS));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimeBasedTaskExecutionRunnableComparableEntry<?> that = (TimeBasedTaskExecutionRunnableComparableEntry<?>) o;
        return this.getDelay(TimeUnit.MILLISECONDS) == that.getDelay(TimeUnit.MILLISECONDS)
                && Objects.equals(data, that.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
